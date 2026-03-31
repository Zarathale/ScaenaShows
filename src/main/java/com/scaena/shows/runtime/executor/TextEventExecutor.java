package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.*;
import com.scaena.shows.model.event.TextEvents.*;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.RunningShow;
import com.scaena.shows.runtime.ShowManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;

/**
 * Handles MESSAGE, TITLE, ACTION_BAR, BOSSBAR events.
 */
public final class TextEventExecutor implements EventExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final JavaPlugin plugin;

    /**
     * ShowManager is set post-construction (same pattern as TeamEventExecutor)
     * to avoid a circular dependency at ExecutorRegistry build time.
     */
    private ShowManager showManager;

    public TextEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Called by ExecutorRegistry after ShowManager is constructed. */
    public void setShowManager(ShowManager showManager) {
        this.showManager = showManager;
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case MESSAGE       -> handleMessage((MessageEvent) event, show);
            case TITLE         -> handleTitle((TitleEvent) event, show);
            case TITLE_CLEAR   -> handleTitleClear((TextEvents.TitleClearEvent) event, show);
            case ACTION_BAR    -> handleActionBar((ActionBarEvent) event, show);
            case BOSSBAR       -> handleBossbar((BossbarEvent) event, show);
            case PLAYER_CHOICE -> handlePlayerChoice((PlayerChoiceEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // MESSAGE
    // ------------------------------------------------------------------
    private void handleMessage(MessageEvent e, RunningShow show) {
        Component component = MM.deserialize(e.message);
        List<Player> audience = AudienceResolver.resolve(e.audience, show);
        for (Player p : audience) {
            p.sendMessage(component);
        }
    }

    // ------------------------------------------------------------------
    // TITLE
    // ------------------------------------------------------------------
    private void handleTitle(TitleEvent e, RunningShow show) {
        Component title    = MM.deserialize(e.title);
        Component subtitle = MM.deserialize(e.subtitle);
        Title.Times times  = Title.Times.times(
            Duration.ofMillis(e.fadeIn * 50L),
            Duration.ofMillis(e.stay   * 50L),
            Duration.ofMillis(e.fadeOut * 50L)
        );
        Title titleObj = Title.title(title, subtitle, times);
        List<Player> audience = AudienceResolver.resolve(e.audience, show);
        for (Player p : audience) {
            p.showTitle(titleObj);
        }
    }

    // ------------------------------------------------------------------
    // TITLE_CLEAR — send empty title with zero stay; fade_out controls the wipe speed
    // ------------------------------------------------------------------
    private void handleTitleClear(TextEvents.TitleClearEvent e, RunningShow show) {
        Title.Times times = Title.Times.times(
            Duration.ofMillis(0),
            Duration.ofMillis(0),
            Duration.ofMillis(e.fadeOut * 50L)
        );
        Title titleObj = Title.title(Component.empty(), Component.empty(), times);
        List<Player> audience = AudienceResolver.resolve(e.audience, show);
        for (Player p : audience) {
            p.showTitle(titleObj);
        }
    }

    // ------------------------------------------------------------------
    // ACTION_BAR
    // ------------------------------------------------------------------
    private void handleActionBar(ActionBarEvent e, RunningShow show) {
        Component msg = MM.deserialize(e.message);
        List<Player> audience = AudienceResolver.resolve(e.audience, show);

        // Send immediately
        for (Player p : audience) p.sendActionBar(msg);

        // Re-send every 20 ticks for durationTicks to persist the bar.
        // The Minecraft action bar clears after ~40t without a refresh.
        if (e.durationTicks > 20) {
            int refreshes = e.durationTicks / 20;
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (!show.isRunning() || ++count >= refreshes) {
                        cancel();
                        return;
                    }
                    // Re-resolve audience each time in case players disconnected
                    for (Player p : AudienceResolver.resolve(e.audience, show)) {
                        p.sendActionBar(msg);
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
    }

    // ------------------------------------------------------------------
    // BOSSBAR (inline event)
    // ------------------------------------------------------------------
    private void handleBossbar(BossbarEvent e, RunningShow show) {
        BossBar.Color color;
        BossBar.Overlay overlay;
        try { color   = BossBar.Color.valueOf(e.color.toUpperCase()); }
        catch (Exception ex) { color = BossBar.Color.WHITE; }
        try { overlay = BossBar.Overlay.valueOf(e.overlay.toUpperCase()); }
        catch (Exception ex) { overlay = BossBar.Overlay.PROGRESS; }

        Component title = MM.deserialize(e.title);
        BossBar bar = BossBar.bossBar(title, 0f, color, overlay);

        List<Player> audience = AudienceResolver.resolve(e.audience, show);
        for (Player p : audience) p.showBossBar(bar);
        show.addActiveBossBar(bar);

        // Animate: fade in (0→1) over fadeInTicks, hold at 1, fade out (1→0) over fadeOutTicks
        long totalTicks = e.durationTicks;
        long fadeInEnd  = e.fadeInTicks;
        long fadeOutStart = totalTicks - e.fadeOutTicks;

        new BukkitRunnable() {
            long tick = 0;
            @Override
            public void run() {
                tick++;
                if (!show.isRunning() || tick > totalTicks) {
                    for (Player p : AudienceResolver.resolve(e.audience, show)) p.hideBossBar(bar);
                    show.getActiveBossBars().remove(bar);
                    cancel();
                    return;
                }
                float progress;
                if (tick <= fadeInEnd && fadeInEnd > 0) {
                    progress = (float) tick / fadeInEnd;
                } else if (tick >= fadeOutStart && e.fadeOutTicks > 0) {
                    progress = (float) (totalTicks - tick) / e.fadeOutTicks;
                } else {
                    progress = 1f;
                }
                bar.progress(Math.max(0f, Math.min(1f, progress)));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // ------------------------------------------------------------------
    // PLAYER_CHOICE — suspend and hand off to ShowManager
    // ------------------------------------------------------------------
    private void handlePlayerChoice(PlayerChoiceEvent e, RunningShow show) {
        if (showManager == null) {
            plugin.getLogger().warning(
                "[ScaenaShows] PLAYER_CHOICE fired but ShowManager not wired "
                + "into TextEventExecutor — skipping.");
            return;
        }
        if (e.options.isEmpty()) {
            plugin.getLogger().warning(
                "[ScaenaShows] PLAYER_CHOICE has no options in show '"
                + show.show.id + "' — skipping.");
            return;
        }
        showManager.suspendForChoice(show, e);
    }
}
