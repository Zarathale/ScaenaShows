package com.scaena.shows.runtime;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.EventType;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.UtilityEvents.CueRefEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.executor.ExecutorRegistry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Logger;

/**
 * Drives the tick-by-tick execution of a RunningShow.
 *
 * At each tick:
 *  1. Updates the bossbar progress (if enabled)
 *  2. Dispatches all events scheduled at currentTick,
 *     expanding CUE references inline
 *  3. Advances the tick counter
 *  4. Stops the show when durationTicks is reached
 */
public final class ShowScheduler {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final CueRegistry cueRegistry;
    private final ExecutorRegistry executors;
    private final ShowManager showManager;
    private final Logger log;

    /** Flattened event map: absolute tick → events, built at show start. */
    private final NavigableMap<Long, List<ShowEvent>> eventMap = new TreeMap<>();

    /**
     * When true, the BukkitRunnable tick loop does nothing — all dispatch is
     * demand-driven via dispatchNextEventTick() / dispatchEventsUpTo().
     * Set by TechCueSession when entering Phase 2 preview mode.
     */
    private boolean steppingMode = false;

    private BukkitTask task;
    private final RunningShow show;

    public ShowScheduler(
        JavaPlugin plugin,
        CueRegistry cueRegistry,
        ExecutorRegistry executors,
        ShowManager showManager,
        RunningShow show
    ) {
        this.plugin      = plugin;
        this.cueRegistry = cueRegistry;
        this.executors   = executors;
        this.showManager = showManager;
        this.show        = show;
        this.log         = plugin.getLogger();
    }

    /** Build the event map and start the scheduler task. */
    public void start() {
        buildEventMap(show.show.timeline, 0, 0);
        startBossBar();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!show.isRunning()) {
                    cancel();
                    return;
                }

                // Step mode: all dispatch is demand-driven; tick loop is a no-op
                if (steppingMode) return;

                // Hard-suspended for PLAYER_CHOICE — hold until resolved
                if (show.isSuspended()) return;

                long tick = show.getCurrentTick();

                // Dispatch events at this tick
                List<ShowEvent> events = eventMap.get(tick);
                if (events != null) {
                    for (ShowEvent e : events) {
                        if (e.type() == EventType.CUE) continue; // already expanded
                        executors.dispatch(e, show);

                        // Director mode: show cue name in action bar for CUE refs
                        // (handled during build — the cue label is inserted as ACTION_BAR)
                    }
                }

                // Update bossbar progress
                tickBossBar(tick);

                show.tick();

                // Check if show is complete (uses durationOverride when a branch was injected)
                if (show.getEffectiveDuration() > 0 && show.getCurrentTick() >= show.getEffectiveDuration()) {
                    showManager.stopShow(show, false /* completed naturally */);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * Inject a branch cue's events into the running event map, replacing all
     * future events (hard fork — the main timeline ends at the choice point).
     *
     * Called by ShowManager.resumeWithBranch() after a PLAYER_CHOICE resolution.
     * Sets the show's durationOverride to the last event tick + 40 ticks buffer,
     * then clears the suspension flag so the scheduler resumes on its next tick.
     */
    public void injectBranchCue(Cue branchCue) {
        long currentTick = show.getCurrentTick();

        // Clear all future events from the main timeline (hard fork)
        eventMap.tailMap(currentTick, false).clear();

        // Expand the branch cue's timeline into the map, rooted at currentTick
        buildEventMap(branchCue.timeline, currentTick, 0);

        // Compute the last event tick and extend the show's effective duration
        long lastEventTick = eventMap.isEmpty() ? currentTick : eventMap.lastKey();
        show.setDurationOverride((int) (lastEventTick + 40)); // +40 ticks (~2s buffer)

        // Resume
        show.setSuspended(false);
        log.fine("[ScaenaShows] Branch '" + branchCue.id
            + "' injected at tick " + currentTick
            + " (new duration: " + show.getDurationOverride() + ")");
    }

    /** Cancel the scheduler task without triggering full stop (called by ShowManager). */
    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    // ------------------------------------------------------------------
    // Step mode (Phase 2 preview)
    // ------------------------------------------------------------------

    /**
     * Enable or disable step mode.
     *
     * When true, the BukkitRunnable tick loop is a no-op and all event
     * dispatch is driven by dispatchNextEventTick() / dispatchEventsUpTo().
     * When false (default), the scheduler returns to normal production
     * tick-clock behaviour.
     */
    public void setSteppingMode(boolean stepping) {
        this.steppingMode = stepping;
    }

    public boolean isSteppingMode() {
        return steppingMode;
    }

    /**
     * Dispatch all events at the next event tick after the show's current tick.
     * Advances the tick cursor to that tick and holds there.
     *
     * This is the "Go" operation in Phase 2 preview mode.
     *
     * PAUSE events are dispatched as a no-op (per their executor) — the step
     * naturally holds at that tick, so authored PAUSE markers behave as expected
     * without any special-case logic here.
     *
     * @return the tick at which events were dispatched,
     *         or -1 if no further events exist in the map
     */
    public long dispatchNextEventTick() {
        long currentTick = show.getCurrentTick();
        Long nextTick = eventMap.higherKey(currentTick);
        if (nextTick == null) return -1;

        show.setCurrentTick(nextTick);
        dispatchAt(nextTick);
        tickBossBar(nextTick);
        return nextTick;
    }

    /**
     * Dispatch all events from currentTick (exclusive) through targetTick (inclusive),
     * then advance the tick cursor to targetTick.
     *
     * Used for seeking to a specific timeline position — e.g. jumping to a cue's
     * tick to set up the world state for that moment without navigating step-by-step.
     *
     * @param targetTick the tick to seek to (must be >= show.getCurrentTick())
     */
    public void dispatchEventsUpTo(long targetTick) {
        long current = show.getCurrentTick();
        NavigableMap<Long, List<ShowEvent>> range =
            eventMap.subMap(current, false, targetTick, true);
        for (long tick : range.keySet()) {
            dispatchAt(tick);
        }
        show.setCurrentTick(targetTick);
        tickBossBar(targetTick);
    }

    /** Dispatch all events at the given absolute tick (internal helper). */
    private void dispatchAt(long tick) {
        List<ShowEvent> events = eventMap.get(tick);
        if (events == null) return;
        for (ShowEvent e : events) {
            if (e.type() == EventType.CUE) continue; // already expanded at build time
            executors.dispatch(e, show);
        }
    }

    // ------------------------------------------------------------------
    // Event map construction
    // ------------------------------------------------------------------

    /**
     * Recursively expand the timeline into the event map.
     * CUE references are expanded inline: each event in the child cue
     * is placed at (parentOffset + cueRefAt + childEvent.at).
     *
     * @param timeline    the timeline to flatten
     * @param baseOffset  tick offset applied to all events in this timeline
     * @param depth       current recursion depth (for nesting limit)
     */
    void buildEventMap(List<ShowEvent> timeline, long baseOffset, int depth) {
        if (depth > 10) {
            log.warning("[ScaenaShows] Max cue nesting depth reached in show '" + show.show.id + "'");
            return;
        }

        for (ShowEvent event : timeline) {
            long absoluteTick = baseOffset + event.at;

            if (event.type() == EventType.CUE) {
                // Expand child cue inline
                CueRefEvent cueRef = (CueRefEvent) event;
                Cue child = cueRegistry.get(cueRef.cueId);
                if (child == null) continue; // validated at load time; shouldn't happen

                // Director mode: inject cue-start action bar for invoker
                if (show.isScenesMode()) {
                    showDirectorLabel(child.name, absoluteTick);
                }

                buildEventMap(child.timeline, absoluteTick, depth + 1);
            } else {
                eventMap.computeIfAbsent(absoluteTick, k -> new ArrayList<>()).add(event);
            }
        }
    }

    private void showDirectorLabel(String cueName, long atTick) {
        long delay = atTick - show.getCurrentTick();
        if (delay < 0) delay = 0;
        final long d = delay;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player invoker = show.getInvoker();
            if (invoker != null && invoker.isOnline()) {
                invoker.sendActionBar(MM.deserialize("<gray>[Scene: " + cueName + "]</gray>"));
            }
        }, d);
    }

    // ------------------------------------------------------------------
    // Bossbar
    // ------------------------------------------------------------------

    private void startBossBar() {
        if (!show.show.bossbar.enabled()) return;

        BossBar.Color color;
        BossBar.Overlay overlay;
        try { color   = BossBar.Color.valueOf(show.show.bossbar.color().toUpperCase()); }
        catch (Exception e) { color = BossBar.Color.WHITE; }
        try { overlay = BossBar.Overlay.valueOf(show.show.bossbar.overlay().toUpperCase()); }
        catch (Exception e) { overlay = BossBar.Overlay.PROGRESS; }

        BossBar bar = BossBar.bossBar(
            MM.deserialize(show.show.bossbar.title()),
            0f, color, overlay);

        show.setShowBossBar(bar);

        // Show to audience
        for (Player p : AudienceResolver.resolve(show.show.bossbar.audience(), show)) {
            p.showBossBar(bar);
        }
    }

    private void tickBossBar(long tick) {
        BossBar bar = show.getShowBossBar();
        if (bar == null || show.show.durationTicks <= 0) return;

        int totalTicks  = show.show.durationTicks;
        int fadeIn      = show.show.bossbar.fadeInTicks();
        int fadeOut     = show.show.bossbar.fadeOutTicks();
        long fadeOutStart = totalTicks - fadeOut;

        float progress;
        if (tick <= fadeIn && fadeIn > 0) {
            progress = (float) tick / fadeIn;
        } else if (tick >= fadeOutStart && fadeOut > 0) {
            progress = (float) (totalTicks - tick) / fadeOut;
        } else {
            progress = 1f;
        }
        bar.progress(Math.max(0f, Math.min(1f, progress)));
    }

    public void hideBossBar() {
        BossBar bar = show.getShowBossBar();
        if (bar == null) return;
        for (Player p : show.getOnlineParticipants()) p.hideBossBar(bar);
        // Also hide from broadcast if bossbar audience was broadcast
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.hideBossBar(bar);
        }
        // Hide inline bossbars
        for (BossBar b : show.getActiveBossBars()) {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                p.hideBossBar(b);
            }
        }
    }
}
