package com.scaena.shows.runtime;

import com.scaena.shows.config.ScaenaConfig;
import com.scaena.shows.model.Cue;
import com.scaena.shows.model.Show;
import com.scaena.shows.model.event.TextEvents.PlayerChoiceEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.executor.ExecutorRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages all running show instances.
 * Handles play, stop, stopall, and participant lifecycle.
 */
public final class ShowManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final ScaenaConfig config;
    private final CueRegistry cueRegistry;
    private final ExecutorRegistry executors;
    private final Logger log;

    /** All currently-running shows, keyed by instanceId. */
    private final Map<String, RunningShow>  runningShows    = new LinkedHashMap<>();
    /** Scheduler paired with each running show. */
    private final Map<String, ShowScheduler> schedulers     = new LinkedHashMap<>();
    /** Participant UUID → instanceId, for fast "is player in a show?" lookup. */
    private final Map<UUID, String>          participantIndex = new LinkedHashMap<>();
    /** Cooldown tracking: invoker UUID → system time of last /show play (ms). */
    private final Map<UUID, Long>            cooldowns       = new LinkedHashMap<>();

    public ShowManager(
        JavaPlugin plugin,
        ScaenaConfig config,
        CueRegistry cueRegistry,
        ExecutorRegistry executors
    ) {
        this.plugin      = plugin;
        this.config      = config;
        this.cueRegistry = cueRegistry;
        this.executors   = executors;
        this.log         = plugin.getLogger();
    }

    // ------------------------------------------------------------------
    // /show play
    // ------------------------------------------------------------------

    /**
     * Attempt to start a show.
     *
     * @param show        the Show definition
     * @param participants ordered list of participants (first = spatial anchor)
     * @param invoker     the player who ran the command
     * @param privateMode --private flag
     * @param scenesMode  --scenes flag
     * @param forceMode   "follow" | "static" | null (use show default)
     * @return error message string, or null on success
     */
    public String startShow(
        Show show,
        List<Player> participants,
        Player invoker,
        boolean privateMode,
        boolean scenesMode,
        String forceMode
    ) {
        // Check: any participant already in a show?
        for (Player p : participants) {
            if (participantIndex.containsKey(p.getUniqueId())) {
                return p.getName() + " is already in a running show. Use /show stop " + p.getName() + " first.";
            }
        }

        // Check cooldown (skip if admin or bypass perm)
        if (!invoker.hasPermission("scae.shows.admin") && !invoker.hasPermission("scae.shows.cooldown.bypass")) {
            Long lastPlay = cooldowns.get(invoker.getUniqueId());
            if (lastPlay != null) {
                long elapsed = System.currentTimeMillis() - lastPlay;
                long cooldownMs = config.getCooldownSeconds() * 1000L;
                if (elapsed < cooldownMs) {
                    long remaining = (cooldownMs - elapsed) / 1000;
                    return "The stage isn't ready yet. You can run another show in " + remaining + " second(s).";
                }
            }
        }

        String followMode = (forceMode != null) ? forceMode : show.defaultMode;

        RunningShow running = new RunningShow(
            show, participants, invoker, privateMode, scenesMode, followMode);

        ShowScheduler scheduler = new ShowScheduler(
            plugin, cueRegistry, executors, this, running);

        runningShows.put(running.instanceId, running);
        schedulers.put(running.instanceId, scheduler);

        for (Player p : participants) {
            participantIndex.put(p.getUniqueId(), running.instanceId);
        }

        cooldowns.put(invoker.getUniqueId(), System.currentTimeMillis());

        log.info("[ScaenaShows] Starting show '" + show.id + "' (" + running.instanceId + ")"
            + " with " + participants.size() + " participant(s).");

        scheduler.start();
        return null; // success
    }

    // ------------------------------------------------------------------
    // /show stop <target>
    // ------------------------------------------------------------------

    /**
     * Stop the show for one participant.
     * If the show has other participants still running, the show continues for them.
     * If this was the last participant, the show ends.
     *
     * @param player the participant to stop
     * @return true if a show was stopped
     */
    public boolean stopForPlayer(Player player) {
        String instanceId = participantIndex.get(player.getUniqueId());
        if (instanceId == null) return false;

        RunningShow running = runningShows.get(instanceId);
        if (running == null) return false;

        // Remove this player from the participant index
        participantIndex.remove(player.getUniqueId());

        // Apply stop safety to this player — always user-initiated here
        applyStopSafety(player, running, true);

        // Check if all participants are gone
        boolean anyLeft = false;
        for (UUID uid : running.getParticipants().keySet()) {
            if (participantIndex.containsKey(uid)) { anyLeft = true; break; }
        }

        if (!anyLeft) {
            stopShow(running, true);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // /show stopall
    // ------------------------------------------------------------------

    public void stopAll() {
        List<RunningShow> all = new ArrayList<>(runningShows.values());
        for (RunningShow rs : all) stopShow(rs, true);
    }

    // ------------------------------------------------------------------
    // Internal stop — called by StopAll, scheduler completion, or stopForPlayer
    // ------------------------------------------------------------------

    /**
     * Fully stop a show: cancel scheduler, apply safety cleanup, remove from registry.
     *
     * @param running     the show to stop
     * @param userStopped true if stopped by command (vs. natural completion)
     */
    public void stopShow(RunningShow running, boolean userStopped) {
        if (!running.isRunning()) return; // already stopping
        running.stop();

        // Cancel any active PLAYER_CHOICE session before cleaning up the show
        ChoiceSession activeChoice = running.getActiveChoice();
        if (activeChoice != null) {
            activeChoice.cancel();
            running.setActiveChoice(null);
        }

        ShowScheduler scheduler = schedulers.remove(running.instanceId);
        if (scheduler != null) {
            scheduler.cancel();
            scheduler.hideBossBar();
        }

        // Safety cleanup for all remaining online participants
        for (ParticipantState ps : running.getParticipants().values()) {
            Player p = Bukkit.getPlayer(ps.uuid);
            if (p != null && p.isOnline()) {
                applyStopSafety(p, running, userStopped);
            }
            participantIndex.remove(ps.uuid);
        }

        // Remove all show-scoped scoreboard teams
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (String teamName : running.getOwnedTeams()) {
            Team t = board.getTeam(teamName);
            if (t != null) t.unregister();
        }

        // Despawn entities tagged despawn_on_end
        for (var entry : running.getSpawnedEntities().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isDead()) {
                entry.getValue().remove();
            }
        }

        // Restore spectate gamemodes
        for (var entry : running.getSpectateRestoreMap().entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                p.setSpectatorTarget(null);
                p.setGameMode(entry.getValue());
            }
        }

        runningShows.remove(running.instanceId);
        log.info("[ScaenaShows] Show '" + running.show.id + "' (" + running.instanceId + ") ended.");
    }

    // ------------------------------------------------------------------
    // Stop safety (spec §15)
    // Idempotent — safe to call multiple times per player.
    // ------------------------------------------------------------------

    /**
     * Apply stop-safety cleanup to a single participant.
     *
     * @param userStopped true if the show was explicitly stopped by a command
     *                    (/show stop, /show stopall, or per-player stop).
     *                    false if the show reached its natural end via the scheduler.
     *
     * Slow falling + levitation removal always apply (protect against mid-air orphaning).
     * Sound cut, stop_message, and stop_sound only fire on user-interrupted stops —
     * on natural completion the show's own coda handles the artistic ending.
     */
    private void applyStopSafety(Player p, RunningShow running, boolean userStopped) {
        // Always: remove levitation and apply slow falling regardless of stop cause
        p.removePotionEffect(PotionEffectType.LEVITATION);
        int slowFallTicks = config.getStopSlowFallSeconds() * 20;
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, slowFallTicks, 0, false, false));

        // Restore flight state if PLAYER_FLIGHT was used in this show.
        // Apply slow_falling first (already done above) so the player gets a soft
        // landing when flight is removed — no gravity snap.
        RunningShow.FlightState flightState = running.getFlightRestore(p.getUniqueId());
        if (flightState != null) {
            p.setFlying(flightState.wasFlying());
            p.setAllowFlight(flightState.allowFlight());
        }

        // Interruption only: cut sound, notify the player, play the stop cue.
        // On natural end, the show's coda sound and closing messages handle this.
        if (userStopped) {
            p.stopAllSounds();
            p.sendMessage(MM.deserialize(config.getStopMessage()));
            try {
                p.playSound(p.getLocation(),
                    config.getStopSoundId(),
                    SoundCategory.MASTER,
                    config.getStopSoundVolume(),
                    config.getStopSoundPitch());
            } catch (Exception ignored) {} // graceful degradation if sound id is invalid
        }
    }

    // ------------------------------------------------------------------
    // Participant disconnect / rejoin (pause/resume)
    // ------------------------------------------------------------------

    public void onPlayerDisconnect(Player player) {
        String instanceId = participantIndex.get(player.getUniqueId());
        if (instanceId == null) return;
        RunningShow running = runningShows.get(instanceId);
        if (running == null) return;

        ParticipantState ps = running.getParticipant(player.getUniqueId());
        if (ps != null) ps.markDisconnected(running.getCurrentTick());

        // If this was the spatial anchor and show is in follow mode,
        // switch to static at the last known position (spec §15)
        if (ps != null && ps.isSpatialAnchor && "follow".equalsIgnoreCase(running.getFollowMode())) {
            running.setAnchorLocation(player.getLocation().clone());
            // Note: followMode is final on RunningShow; "static fallback" is handled
            // in getAnchorLocation() by checking if anchor player is offline.
        }

        log.fine("[ScaenaShows] " + player.getName() + " disconnected from show " + instanceId);
    }

    public void onPlayerReconnect(Player player) {
        String instanceId = participantIndex.get(player.getUniqueId());
        if (instanceId == null) return;
        RunningShow running = runningShows.get(instanceId);
        if (running == null) return;

        ParticipantState ps = running.getParticipant(player.getUniqueId());
        if (ps == null) return;

        // Check resume window
        long elapsed = System.currentTimeMillis() - ps.disconnectedAtMs;
        long windowMs = (long) config.getResumeWindowSeconds() * 1000;

        if (ps.disconnectedAtMs >= 0 && elapsed > windowMs) {
            // Window expired — remove participant slot
            participantIndex.remove(player.getUniqueId());
            log.info("[ScaenaShows] " + player.getName() + "'s resume window expired for show " + instanceId);
        } else {
            // Rejoin: new home at reconnect location
            ps.markReconnected(player);
            log.fine("[ScaenaShows] " + player.getName() + " rejoined show " + instanceId);
        }
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public boolean isInShow(UUID uuid) {
        return participantIndex.containsKey(uuid);
    }

    public RunningShow getShowForPlayer(UUID uuid) {
        String id = participantIndex.get(uuid);
        return id != null ? runningShows.get(id) : null;
    }

    public Map<String, RunningShow> getRunningShows() {
        return Collections.unmodifiableMap(runningShows);
    }

    public void shutdown() {
        stopAll();
    }

    // ------------------------------------------------------------------
    // PLAYER_CHOICE — suspend and resume
    // ------------------------------------------------------------------

    /**
     * Suspend a running show for a PLAYER_CHOICE event.
     * Creates and starts a ChoiceSession, attaches it to the RunningShow,
     * and sets the suspended flag so the scheduler halts event dispatch.
     *
     * Called by TextEventExecutor when it encounters a PLAYER_CHOICE event.
     */
    public void suspendForChoice(RunningShow running, PlayerChoiceEvent event) {
        if (!running.isRunning()) return;

        ChoiceSession session = new ChoiceSession(event, running, this, plugin, log);
        running.setActiveChoice(session);
        running.setSuspended(true);
        session.start();

        log.info("[ScaenaShows] Show '" + running.show.id + "' ("
            + running.instanceId + ") suspended for PLAYER_CHOICE: \""
            + event.prompt + "\"");
    }

    /**
     * Resume a suspended show by injecting the chosen branch cue.
     * Called by ChoiceSession.resolve() after a valid option is selected.
     *
     * Looks up the branch cue, validates it, and delegates to the scheduler
     * to expand it into the event map and resume ticking.
     */
    public void resumeWithBranch(RunningShow running, String cueId) {
        if (!running.isRunning()) return;

        Cue branchCue = cueRegistry.get(cueId);
        if (branchCue == null) {
            log.warning("[ScaenaShows] PLAYER_CHOICE branch cue '" + cueId
                + "' not found in show '" + running.show.id
                + "' — stopping show instead.");
            stopShow(running, true);
            return;
        }

        ShowScheduler scheduler = schedulers.get(running.instanceId);
        if (scheduler == null) {
            log.warning("[ScaenaShows] No scheduler found for show '"
                + running.show.id + "' during branch resume.");
            stopShow(running, true);
            return;
        }

        scheduler.injectBranchCue(branchCue);
        // Note: injectBranchCue() clears suspension — scheduler resumes on next tick
    }

    /**
     * Inject a cue into a running show without requiring it to be suspended first.
     * Used by EntityCombatListener after entity death to fire the victory coda.
     *
     * Clears all remaining scheduled events and replaces them with the named cue,
     * extending the show's effective duration accordingly.
     * Silent no-op if the cue is not found (logs a warning) or the show is not running.
     *
     * @param running  the show to inject into
     * @param cueId    the cue ID to inject
     */
    public void injectCue(RunningShow running, String cueId) {
        if (!running.isRunning()) return;

        Cue cue = cueRegistry.get(cueId);
        if (cue == null) {
            log.warning("[ScaenaShows] injectCue: cue '" + cueId
                + "' not found in show '" + running.show.id + "' — skipping.");
            return;
        }

        ShowScheduler scheduler = schedulers.get(running.instanceId);
        if (scheduler == null) {
            log.warning("[ScaenaShows] injectCue: no scheduler for show '"
                + running.show.id + "' — skipping.");
            return;
        }

        scheduler.injectBranchCue(cue);
        log.info("[ScaenaShows] Injected cue '" + cueId
            + "' into show '" + running.show.id + "' (" + running.instanceId + ")");
    }

    /**
     * Route a /scaena choose command from a participant.
     * Validates the player is in a suspended show, then resolves the choice.
     *
     * @param player      the player who clicked a choice link
     * @param rawChoice   the raw arg string — a digit (0-based index) or "stop"
     * @return error message, or null on success
     */
    public String handleChoiceCommand(Player player, String rawChoice) {
        RunningShow running = getShowForPlayer(player.getUniqueId());
        if (running == null) {
            return "You're not in a running show.";
        }

        ChoiceSession session = running.getActiveChoice();
        if (session == null) {
            return "No choice is active in your current show.";
        }

        if ("stop".equalsIgnoreCase(rawChoice)) {
            session.resolve(ChoiceSession.STOP_INDEX);
            return null;
        }

        int index;
        try {
            index = Integer.parseInt(rawChoice);
        } catch (NumberFormatException e) {
            return "Invalid choice: '" + rawChoice + "'. Click one of the options in chat.";
        }

        session.resolve(index);
        return null;
    }
}
