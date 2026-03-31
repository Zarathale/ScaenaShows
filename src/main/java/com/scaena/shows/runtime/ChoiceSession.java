package com.scaena.shows.runtime;

import com.scaena.shows.model.event.TextEvents.ChoiceOption;
import com.scaena.shows.model.event.TextEvents.PlayerChoiceEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.logging.Logger;

/**
 * Owns the live state of a PLAYER_CHOICE suspension: the bossbar countdown,
 * the sound pulse, the timeout, and the resolution path.
 *
 * Lifecycle:
 *   ShowManager.suspendForChoice() → new ChoiceSession(...).start()
 *   First player click → resolve(optionIndex) [idempotent — first call wins]
 *   Show stopped externally → cancel()
 *
 * Resolution fires the chosen branch cue inside the same RunningShow via
 * ShowScheduler.injectBranchCue(), maintaining full show context.
 *
 * Stop (always auto-injected as the last option) calls ShowManager.stopShow().
 */
public final class ChoiceSession {

    /** Sentinel index value meaning the player chose to stop the show entirely. */
    public static final int STOP_INDEX = -1;

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Letter labels for chat options: [A], [B], [C], ...
    private static final String[] OPTION_LETTERS = {"A", "B", "C", "D", "E", "F"};

    private final PlayerChoiceEvent event;
    private final RunningShow       show;
    private final ShowManager       showManager;
    private final JavaPlugin        plugin;
    private final Logger            log;

    private BossBar    waitBossBar;
    private BukkitTask pulseTask;
    private BukkitTask timeoutTask;

    private volatile boolean resolved = false;

    public ChoiceSession(
        PlayerChoiceEvent event,
        RunningShow show,
        ShowManager showManager,
        JavaPlugin plugin,
        Logger log
    ) {
        this.event       = event;
        this.show        = show;
        this.showManager = showManager;
        this.plugin      = plugin;
        this.log         = log;
    }

    // -----------------------------------------------------------------------
    // Start
    // -----------------------------------------------------------------------

    /**
     * Display the bossbar + chat prompt to all participants and start the
     * pulse + timeout tasks. Called once immediately when PLAYER_CHOICE fires.
     */
    public void start() {
        List<Player> audience = show.getOnlineParticipants();

        // ---- Bossbar ----
        waitBossBar = BossBar.bossBar(
            MM.deserialize("<yellow>❓  " + event.prompt + "</yellow>"),
            event.timeoutTicks > 0 ? 1.0f : 1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS
        );
        for (Player p : audience) p.showBossBar(waitBossBar);

        // ---- Chat prompt ----
        sendChoicePrompt(audience);

        // ---- Sound pulse — every 40 ticks (~2 seconds) ----
        pulseTask = new BukkitRunnable() {
            @Override public void run() {
                if (!show.isRunning() || resolved) { cancel(); return; }
                for (Player p : show.getOnlineParticipants()) {
                    try {
                        p.playSound(p.getLocation(),
                            event.waitingSound, SoundCategory.MASTER, 0.6f, 1.2f);
                    } catch (Exception ignored) {}
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);

        // ---- Bossbar countdown — every tick ----
        if (event.timeoutTicks > 0) {
            final int total = event.timeoutTicks;
            BukkitTask[] countdownHolder = new BukkitTask[1];
            countdownHolder[0] = new BukkitRunnable() {
                int elapsed = 0;
                @Override public void run() {
                    if (!show.isRunning() || resolved) { cancel(); return; }
                    elapsed++;
                    float progress = Math.max(0f, 1f - (float) elapsed / total);
                    waitBossBar.progress(progress);
                    if (elapsed >= total) {
                        cancel();
                        // Timeout fires — resolve with the default option
                        resolve(event.defaultOption);
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }
        // (When timeout_ticks == 0, the bar stays full and no auto-resolve fires)

        // ---- Timeout task (fires once, only needed when timeoutTicks > 0) ----
        // Already handled inside the countdown runnable above; no separate task needed.
    }

    // -----------------------------------------------------------------------
    // Resolve
    // -----------------------------------------------------------------------

    /**
     * Resolve this choice session. Idempotent — first call wins, subsequent
     * calls (e.g. late clicks from other participants) are no-ops.
     *
     * @param optionIndex 0-based index into event.options, or STOP_INDEX (-1) to stop show
     */
    public void resolve(int optionIndex) {
        synchronized (this) {
            if (resolved) return;
            resolved = true;
        }

        // Cancel waiting machinery
        cancelTasks();
        hideBossBar();
        show.setActiveChoice(null);

        if (optionIndex == STOP_INDEX) {
            // Player chose to stop
            log.info("[ScaenaShows] Choice resolved: STOP in show '" + show.show.id + "'");
            showManager.stopShow(show, true);
            return;
        }

        if (optionIndex < 0 || optionIndex >= event.options.size()) {
            log.warning("[ScaenaShows] Choice resolved: invalid option index "
                + optionIndex + " in show '" + show.show.id + "' — using default");
            optionIndex = Math.max(0, Math.min(event.defaultOption, event.options.size() - 1));
        }

        ChoiceOption chosen = event.options.get(optionIndex);
        log.info("[ScaenaShows] Choice resolved: option " + optionIndex
            + " '" + chosen.label() + "' (cue: " + chosen.cueId()
            + ") in show '" + show.show.id + "'");

        // Announce to participants
        Component announcement = MM.deserialize(
            "<gold>▶ " + chosen.label() + "</gold>");
        for (Player p : show.getOnlineParticipants()) p.sendMessage(announcement);

        // Resume show with the chosen branch
        showManager.resumeWithBranch(show, chosen.cueId());
    }

    // -----------------------------------------------------------------------
    // Cancel (called on external show stop)
    // -----------------------------------------------------------------------

    /** Hard cancel — no branch fires. Called when the show is stopped externally. */
    public void cancel() {
        resolved = true; // prevent any pending timeout from firing
        cancelTasks();
        hideBossBar();
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    private void sendChoicePrompt(List<Player> audience) {
        // Header line
        Component header = MM.deserialize("<yellow>❓  " + event.prompt + "</yellow>");
        for (Player p : audience) p.sendMessage(header);

        // Build the option buttons on a single line
        Component line = Component.empty();
        List<ChoiceOption> opts = event.options;
        for (int i = 0; i < opts.size(); i++) {
            String letter = i < OPTION_LETTERS.length ? OPTION_LETTERS[i] : String.valueOf(i + 1);
            Component btn = Component.text("  [" + letter + "] " + opts.get(i).label())
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/scaena choose " + i))
                .hoverEvent(HoverEvent.showText(
                    Component.text("Choose: " + opts.get(i).label())));
            line = line.append(btn);
        }

        // Always append Stop
        Component stopBtn = Component.text("  [■ Stop]")
            .color(TextColor.color(0xFF5555))
            .clickEvent(ClickEvent.runCommand("/scaena choose stop"))
            .hoverEvent(HoverEvent.showText(Component.text("Stop the show")));
        line = line.append(stopBtn);

        for (Player p : audience) p.sendMessage(line);
    }

    private void cancelTasks() {
        if (pulseTask  != null && !pulseTask.isCancelled())   pulseTask.cancel();
        if (timeoutTask != null && !timeoutTask.isCancelled()) timeoutTask.cancel();
    }

    private void hideBossBar() {
        if (waitBossBar == null) return;
        for (Player p : show.getOnlineParticipants()) p.hideBossBar(waitBossBar);
        // Also sweep all online players in case non-participants somehow received it
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) p.hideBossBar(waitBossBar);
        waitBossBar = null;
    }
}
