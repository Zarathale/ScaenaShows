package com.scaena.shows.runtime;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Listens for entity damage and death events to drive BOSS_HEALTH_BAR updates.
 *
 * Registered once at plugin startup. On each damage event it looks up whether
 * the damaged entity is tracked by any running show and updates that show's
 * entity-linked bossbar. On entity death it hides the bar, fires the death
 * line, and (after the configured pause) injects the victory cue.
 *
 * OPS-026 implementation.
 */
public final class EntityCombatListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ShowManager showManager;
    private final JavaPlugin  plugin;
    private final Logger      log;

    public EntityCombatListener(ShowManager showManager, JavaPlugin plugin) {
        this.showManager = showManager;
        this.plugin      = plugin;
        this.log         = plugin.getLogger();
    }

    // ------------------------------------------------------------------
    // EntityDamageByEntityEvent — update bossbar progress after damage lands
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        UUID entityUuid = living.getUniqueId();
        RunningShow show = findShowForEntity(entityUuid);
        if (show == null) return;

        RunningShow.BossHealthBarTracker tracker = show.getBossHealthBarTracker(entityUuid);
        if (tracker == null) return;

        // Damage hasn't been applied yet at MONITOR priority — schedule 1-tick delay
        // so we read the post-damage HP when computing progress.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (living.isDead() || !show.isRunning()) return;
            float progress = (float) (living.getHealth() / tracker.maxHealth());
            tracker.bar().progress(Math.max(0f, Math.min(1f, progress)));
        }, 1L);
    }

    // ------------------------------------------------------------------
    // EntityDeathEvent — hide bar, fire death line, inject victory cue
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        UUID entityUuid = event.getEntity().getUniqueId();
        RunningShow show = findShowForEntity(entityUuid);
        if (show == null) return;

        RunningShow.BossHealthBarTracker tracker = show.getBossHealthBarTracker(entityUuid);
        if (tracker == null) return;

        // 1. Animate bar to zero and hide from all online players
        BossBar bar = tracker.bar();
        bar.progress(0f);
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.hideBossBar(bar);
        }
        show.removeBossHealthBar(entityUuid);

        // 2. Fire death line (if configured) — delivered in Vindicator's text format
        if (tracker.deathLine() != null && !tracker.deathLine().isEmpty()) {
            String colorTag = (tracker.deathLineColor() != null && !tracker.deathLineColor().isEmpty())
                ? "<color:" + tracker.deathLineColor() + ">"
                : "";
            String closeTag = colorTag.isEmpty() ? "" : "</color>";
            var deathMsg = MM.deserialize(colorTag + tracker.deathLine() + closeTag);
            for (Player p : show.getOnlineParticipants()) {
                p.sendMessage(deathMsg);
            }
        }

        // 3. Schedule victory cue injection (after death_line_pause_ticks)
        String victoryCue = tracker.victoryCue();
        if (victoryCue != null && !victoryCue.isEmpty()) {
            int pause = Math.max(1, tracker.deathLinePauseTicks());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (show.isRunning()) {
                    showManager.injectCue(show, victoryCue);
                }
            }, pause);
        }

        log.fine("[ScaenaShows] BOSS_HEALTH_BAR entity '" + entityUuid
            + "' died in show '" + show.show.id + "'. Death sequence fired.");
    }

    // ------------------------------------------------------------------
    // Helper — find a running show that tracks the given entity UUID
    // ------------------------------------------------------------------

    private RunningShow findShowForEntity(UUID entityUuid) {
        for (Map.Entry<String, RunningShow> entry : showManager.getRunningShows().entrySet()) {
            RunningShow show = entry.getValue();
            if (show.getBossHealthBarTracker(entityUuid) != null) {
                return show;
            }
        }
        return null;
    }
}
