package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.EventType;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.TeamEvents.*;
import com.scaena.shows.model.event.UtilityEvents.CueRefEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.ParticipantState;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.logging.Logger;

/**
 * Handles GROUP_ASSIGN, TEAM_COLOR, and GROUP_EVENT events.
 */
public final class TeamEventExecutor implements EventExecutor {

    private final JavaPlugin plugin;
    private final CueRegistry cueRegistry;
    private final Logger log;

    public TeamEventExecutor(JavaPlugin plugin, CueRegistry cueRegistry, Logger log) {
        this.plugin      = plugin;
        this.cueRegistry = cueRegistry;
        this.log         = log;
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case GROUP_ASSIGN -> handleGroupAssign((GroupAssignEvent) event, show);
            case TEAM_COLOR   -> handleTeamColor((TeamColorEvent) event, show);
            case GROUP_EVENT  -> handleGroupEvent((GroupEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // GROUP_ASSIGN
    // Assigns participants to groups 1..N using the show's group_strategy.
    // If fewer participants than groups: extra groups are empty (silent skip on events).
    // ------------------------------------------------------------------
    private void handleGroupAssign(GroupAssignEvent e, RunningShow show) {
        int groupCount    = show.show.groupCount;
        String strategy   = show.show.groupStrategy;

        List<ParticipantState> participants = new ArrayList<>(show.getParticipants().values());

        // Apply group strategy ordering
        switch (strategy.toLowerCase()) {
            case "alphabetical" -> participants.sort(Comparator.comparing(ps -> ps.name));
            case "random"       -> Collections.shuffle(participants);
            // join_order = natural insertion order (already preserved by LinkedHashMap)
        }

        // Distribute round-robin into groups
        Map<Integer, List<UUID>> groups = new LinkedHashMap<>();
        for (int g = 1; g <= groupCount; g++) groups.put(g, new ArrayList<>());

        for (int i = 0; i < participants.size(); i++) {
            int group = (i % groupCount) + 1;
            groups.get(group).add(participants.get(i).uuid);
        }

        show.setGroups(groups);

        log.fine("[ScaenaShows] GROUP_ASSIGN for show '" + show.show.id + "': "
            + participants.size() + " participants → " + groupCount + " group(s).");
    }

    // ------------------------------------------------------------------
    // GROUP_EVENT — fires a named Cue scoped to one player group.
    // Events in the Cue have their "broadcast" and "participants" audience
    // resolved only against the target group's members (via activeGroupScope).
    // ------------------------------------------------------------------
    private void handleGroupEvent(GroupEvent e, RunningShow show) {
        // Parse group number from "group_1" .. "group_4"
        int groupNum = parseGroupNum(e.target);
        if (groupNum < 1) {
            log.warning("[ScaenaShows] GROUP_EVENT: invalid target '" + e.target + "' in show '" + show.show.id + "'");
            return;
        }

        // Silent skip if the group is empty
        List<Player> groupPlayers = show.getGroupPlayers(groupNum);
        if (groupPlayers.isEmpty()) return;

        // Look up the sub-cue
        Cue cue = cueRegistry.get(e.cueId);
        if (cue == null) {
            log.warning("[ScaenaShows] GROUP_EVENT: unknown cue ID '" + e.cueId + "' in show '" + show.show.id + "'");
            return;
        }

        // Flatten the sub-cue timeline (non-recursively for Phase 1; CUE refs inside are skipped)
        List<ShowEvent> timeline = flattenTimeline(cue, 0, new HashSet<>());

        // Dispatch each event: point-in-time events at relative tick 0 fire immediately;
        // others are scheduled via BukkitRunnable with the appropriate delay.
        for (ShowEvent subEvent : timeline) {
            long delay = Math.max(0, subEvent.at);
            if (delay == 0) {
                show.setActiveGroupScope(groupNum);
                try {
                    dispatchFromParent(subEvent, show);
                } finally {
                    show.setActiveGroupScope(0);
                }
            } else {
                new BukkitRunnable() {
                    @Override public void run() {
                        if (!show.isRunning()) { cancel(); return; }
                        show.setActiveGroupScope(groupNum);
                        try {
                            dispatchFromParent(subEvent, show);
                        } finally {
                            show.setActiveGroupScope(0);
                        }
                    }
                }.runTaskLater(plugin, delay);
            }
        }
    }

    /**
     * Recursively flatten a Cue's timeline into a list of concrete (non-CUE) events,
     * respecting at-offsets and expanding nested CUE references.
     */
    private List<ShowEvent> flattenTimeline(Cue cue, long baseAt, Set<String> visited) {
        List<ShowEvent> result = new ArrayList<>();
        if (visited.contains(cue.id)) return result; // cycle guard
        visited.add(cue.id);

        for (ShowEvent ev : cue.timeline) {
            if (ev.type() == EventType.CUE) {
                CueRefEvent ref = (CueRefEvent) ev;
                Cue child = cueRegistry.get(ref.cueId);
                if (child != null) {
                    result.addAll(flattenTimeline(child, baseAt + ev.at, new HashSet<>(visited)));
                }
            } else {
                // Return event with adjusted at
                result.add(adjustedAt(ev, baseAt + ev.at));
            }
        }
        return result;
    }

    /**
     * Returns the event with its `at` field reflecting the adjusted absolute offset.
     * Since ShowEvent.at is final, we return the original event but rely on the scheduler
     * to use the passed delay — so we carry the adjusted tick in the event itself.
     * We use a wrapper trick: we schedule using the adjusted tick as the delay param.
     * The event's own `at` field isn't used at dispatch time; only delay matters.
     */
    private ShowEvent adjustedAt(ShowEvent ev, long adjustedAt) {
        // We return the event as-is; the caller uses the adjusted tick from flattenTimeline
        // via the loop index. We store the adjusted tick by wrapping the event.
        return new AdjustedShowEvent(ev, adjustedAt);
    }

    /**
     * Minimal wrapper that carries an adjusted absolute tick alongside the original event.
     * Used only within GROUP_EVENT scheduling; not stored in the main event map.
     */
    private static final class AdjustedShowEvent extends ShowEvent {
        private final ShowEvent wrapped;
        AdjustedShowEvent(ShowEvent wrapped, long adjustedAt) {
            super((int) Math.min(adjustedAt, Integer.MAX_VALUE));
            this.wrapped = wrapped;
        }
        @Override public com.scaena.shows.model.event.EventType type() { return wrapped.type(); }
        public ShowEvent unwrap() { return wrapped; }
    }

    /**
     * Set after ExecutorRegistry is built to break the construction-time circularity.
     * Required before GROUP_EVENT can dispatch sub-cue events.
     */
    private ExecutorRegistry executorRegistry;
    public void setExecutorRegistry(ExecutorRegistry er) { this.executorRegistry = er; }

    private void dispatchFromParent(ShowEvent event, RunningShow show) {
        ShowEvent actual = (event instanceof AdjustedShowEvent adj) ? adj.unwrap() : event;
        if (executorRegistry == null) {
            log.warning("[ScaenaShows] GROUP_EVENT: ExecutorRegistry not set — sub-event skipped.");
            return;
        }
        executorRegistry.dispatch(actual, show);
    }

    private static int parseGroupNum(String target) {
        if (target == null) return -1;
        return switch (target.toLowerCase()) {
            case "group_1" -> 1;
            case "group_2" -> 2;
            case "group_3" -> 3;
            case "group_4" -> 4;
            default -> -1;
        };
    }

    // ------------------------------------------------------------------
    // TEAM_COLOR
    // ------------------------------------------------------------------
    private void handleTeamColor(TeamColorEvent e, RunningShow show) {
        ChatColor color;
        try { color = ChatColor.valueOf(e.color.toUpperCase()); }
        catch (Exception ex) { color = ChatColor.WHITE; }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        List<Player> targets = AudienceResolver.resolve(e.target, show);
        if (targets.isEmpty()) return;

        String teamName = "scae_tc_" + show.instanceId;
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        team.setColor(color);
        show.registerOwnedTeam(teamName);

        for (Player p : targets) {
            team.addEntry(p.getName());
        }
    }
}
