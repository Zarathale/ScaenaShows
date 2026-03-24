package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.VisualEvents.*;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles PARTICLE, EFFECT, GLOW, CAMERA, LIGHTNING events.
 */
public final class VisualEventExecutor implements EventExecutor {

    private final JavaPlugin plugin;
    private final Logger log;

    public VisualEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case PARTICLE  -> handleParticle((ParticleEvent) event, show);
            case EFFECT    -> handleEffect((EffectEvent) event, show);
            case GLOW      -> handleGlow((GlowEvent) event, show);
            case CAMERA    -> handleCamera((CameraEvent) event, show);
            case LIGHTNING -> handleLightning((LightningEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // PARTICLE
    // ------------------------------------------------------------------
    private void handleParticle(ParticleEvent e, RunningShow show) {
        Particle particle;
        try { particle = Particle.valueOf(
                e.particleId.replace("minecraft:", "").toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] Unknown particle: " + e.particleId);
            return;
        }

        List<Player> audience = show.getOnlineParticipants();
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        Runnable spawnOnce = () -> {
            for (Player p : audience) {
                anchor.getWorld().spawnParticle(particle, anchor,
                    e.count, e.offsetX, e.offsetY, e.offsetZ, e.extra, null, e.force);
            }
        };

        spawnOnce.run();

        if (e.durationTicks > 0 && e.intervalTicks > 0) {
            new BukkitRunnable() {
                long elapsed = 0;
                @Override
                public void run() {
                    elapsed += e.intervalTicks;
                    if (!show.isRunning() || elapsed >= e.durationTicks) { cancel(); return; }
                    for (Player p : show.getOnlineParticipants()) {
                        show.getAnchorLocation().getWorld().spawnParticle(
                            particle, show.getAnchorLocation(),
                            e.count, e.offsetX, e.offsetY, e.offsetZ, e.extra, null, e.force);
                    }
                }
            }.runTaskTimer(plugin, e.intervalTicks, e.intervalTicks);
        }
    }

    // ------------------------------------------------------------------
    // EFFECT (potion effect on players)
    // ------------------------------------------------------------------
    private void handleEffect(EffectEvent e, RunningShow show) {
        PotionEffectType type = PotionEffectType.getByName(e.effectId.toUpperCase());
        if (type == null) {
            log.warning("[ScaenaShows] Unknown potion effect: " + e.effectId);
            return;
        }
        PotionEffect effect = new PotionEffect(type, e.durationTicks, e.amplifier, false, !e.hideParticles);
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            p.addPotionEffect(effect);
        }
    }

    // ------------------------------------------------------------------
    // GLOW
    // ------------------------------------------------------------------
    private void handleGlow(GlowEvent e, RunningShow show) {
        // Use a show-scoped scoreboard team for colored glow.
        // Note: TAB 5.x conflict is a known issue (SCENA-002). See spec §15.
        ChatColor teamColor;
        try { teamColor = ChatColor.valueOf(e.color.toUpperCase()); }
        catch (Exception ex) { teamColor = ChatColor.WHITE; }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName  = "scae_" + show.show.id.replaceAll("[^a-zA-Z0-9]", "_")
                           .substring(0, Math.min(8, show.show.id.length()))
                           + "_" + show.instanceId;
        // Truncate team name to 16 chars (Bukkit limit)
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        team.setColor(teamColor);
        show.registerOwnedTeam(teamName);

        List<Player> audience = AudienceResolver.resolve(e.audience, show);
        for (Player p : audience) {
            p.setGlowing(true);
            team.addEntry(p.getName());
        }

        // Remove glow after duration
        String finalTeamName = teamName;
        Team finalTeam = team;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : AudienceResolver.resolve(e.audience, show)) {
                    p.setGlowing(false);
                    finalTeam.removeEntry(p.getName());
                }
                if (finalTeam.getEntries().isEmpty()) {
                    finalTeam.unregister();
                    show.getOwnedTeams().remove(finalTeamName);
                }
            }
        }.runTaskLater(plugin, e.durationTicks);
    }

    // ------------------------------------------------------------------
    // CAMERA
    // ------------------------------------------------------------------
    private void handleCamera(CameraEvent e, RunningShow show) {
        // Map effect names to potion types (spec §6.3)
        PotionEffectType type = switch (e.effect.toLowerCase()) {
            case "sway"     -> PotionEffectType.NAUSEA;
            case "blackout" -> PotionEffectType.DARKNESS;
            case "flash"    -> PotionEffectType.BLINDNESS;
            case "float"    -> PotionEffectType.LEVITATION;
            default         -> PotionEffectType.NAUSEA;
        };

        PotionEffect effect = new PotionEffect(type, e.durationTicks, e.intensity, false, false);
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            p.addPotionEffect(effect);
            // "float" also adds SLOW_FALLING for controlled arc
            if ("float".equalsIgnoreCase(e.effect)) {
                p.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING, e.durationTicks, 0, false, false));
            }
        }
    }

    // ------------------------------------------------------------------
    // LIGHTNING (cosmetic only — no damage, no fire per spec §15)
    // ------------------------------------------------------------------
    private void handleLightning(LightningEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        Location strike = anchor.clone().add(e.offsetX, e.offsetY, e.offsetZ);
        anchor.getWorld().strikeLightningEffect(strike);
    }
}
