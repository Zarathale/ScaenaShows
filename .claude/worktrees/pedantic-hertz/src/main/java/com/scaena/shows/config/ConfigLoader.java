package com.scaena.shows.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.logging.Level;

public final class ConfigLoader {

    private final JavaPlugin plugin;
    private volatile ScaenaShowsConfig config = defaults();

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        int cooldown = c.getInt("cooldowns.default_seconds", 60);
        String cooldownBypassPerm = c.getString("cooldowns.bypass_permission", "scae.shows.cooldown.bypass");

        int resumeWindowSeconds = c.getInt("pause_resume.resume_window_seconds", 900);

        int stopSlowFallSeconds = c.getInt("safety.stop_applies_slow_falling_seconds", 10);
        String stopMessage = c.getString("safety.stop_message", "<gold>Show stopped.</gold> <gray>Landing safely.</gray>");
        ConfigurationSection stopSound = c.getConfigurationSection("safety.stop_sound");

        SoundSpec stopSoundSpec = SoundSpec.fromSection(stopSound);

        int maxEventsPerShow = c.getInt("limits.max_events_per_show", 2000);
        int maxFireworksPerSecond = c.getInt("limits.max_fireworks_per_second", 30);
        int maxParticlesPerEvent = c.getInt("limits.max_particles_per_event", 200);

        // Defaults block
        String mode = c.getString("defaults.mode", "follow").toLowerCase(Locale.ROOT);
        boolean broadcast = c.getBoolean("defaults.broadcast", true);
        boolean bossbarEnabled = c.getBoolean("defaults.bossbar.enabled", true);
        String bossbarColor = c.getString("defaults.bossbar.color", "YELLOW");
        String bossbarOverlay = c.getString("defaults.bossbar.overlay", "PROGRESS");

        // Permissions block (string values)
        String adminPerm = c.getString("permissions.admin_permission", "scae.shows.admin");
        String playPerm = c.getString("permissions.play_permission", "scae.shows.play");
        String targetOthersPerm = c.getString("permissions.target_others_permission", "scae.shows.target.others");
        String targetAllPerm = c.getString("permissions.target_all_permission", "scae.shows.target.all");
        String privateFlagPerm = c.getString("permissions.private_flag_permission", "scae.shows.private");

        this.config = new ScaenaShowsConfig(
                Math.max(0, cooldown),
                cooldownBypassPerm,
                Math.max(1, resumeWindowSeconds),
                Math.max(0, stopSlowFallSeconds),
                stopMessage,
                stopSoundSpec,
                Math.max(1, maxEventsPerShow),
                Math.max(1, maxFireworksPerSecond),
                Math.max(0, maxParticlesPerEvent),
                mode,
                broadcast,
                bossbarEnabled,
                bossbarColor,
                bossbarOverlay,
                adminPerm,
                playPerm,
                targetOthersPerm,
                targetAllPerm,
                privateFlagPerm,
                8,
                5
        );

        plugin.getLogger().log(Level.INFO, "[ScaenaShows] Loaded config.yml");
    }

    public ScaenaShowsConfig get() {
        return config;
    }

    private static ScaenaShowsConfig defaults() {
        return new ScaenaShowsConfig(
                60, "scae.shows.cooldown.bypass",
                900, 10,
                "<gold>Show stopped.</gold> <gray>Landing safely.</gray>",
                new SoundSpec("minecraft:entity.experience_orb.pickup", "master", 1.0f, 1.0f),
                2000, 30, 200,
                "follow", true,
                true, "YELLOW", "PROGRESS",
                "scae.shows.admin", "scae.shows.play",
                "scae.shows.target.others", "scae.shows.target.all", "scae.shows.private",
                8, 5
        );
    }
}
