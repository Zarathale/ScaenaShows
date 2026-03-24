package com.scaena.shows.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Wrapper around config.yml. All values have plugin-side defaults;
 * config.yml only needs entries for overrides.
 */
public final class ScaenaConfig {

    // Defaults
    public static final int DEFAULT_COOLDOWN_SECONDS    = 30;
    public static final int DEFAULT_RESUME_WINDOW       = 900;
    public static final int DEFAULT_STOP_SLOW_FALL_SECS = 10;
    public static final int DEFAULT_MAX_CUE_NESTING     = 10;
    public static final int DEFAULT_MAX_FW_PER_SECOND   = 30;
    public static final int DEFAULT_MAX_FW_PER_SHOT     = 12;
    public static final int DEFAULT_MAX_PARTICLES       = 200;

    private final int cooldownSeconds;
    private final int resumeWindowSeconds;
    private final int stopSlowFallSeconds;
    private final String stopMessage;
    private final String stopSoundId;
    private final String stopSoundCategory;
    private final float stopSoundVolume;
    private final float stopSoundPitch;
    private final int maxCueNestingDepth;
    private final int maxFireworksPerSecond;
    private final int maxFireworksPerShot;
    private final int maxParticlesPerEvent;

    public ScaenaConfig(FileConfiguration cfg) {
        cooldownSeconds    = cfg.getInt("default_cooldown_seconds", DEFAULT_COOLDOWN_SECONDS);
        resumeWindowSeconds= cfg.getInt("resume_window_seconds",    DEFAULT_RESUME_WINDOW);
        stopSlowFallSeconds= cfg.getInt("safety.stop_applies_slow_falling_seconds", DEFAULT_STOP_SLOW_FALL_SECS);
        stopMessage        = cfg.getString("safety.stop_message",
                "<gold>Show stopped.</gold> <gray>Landing safely.</gray>");
        stopSoundId        = cfg.getString("safety.stop_sound.id",
                "minecraft:entity.experience_orb.pickup");
        stopSoundCategory  = cfg.getString("safety.stop_sound.category", "master");
        stopSoundVolume    = (float) cfg.getDouble("safety.stop_sound.volume", 1.0);
        stopSoundPitch     = (float) cfg.getDouble("safety.stop_sound.pitch",  1.0);
        maxCueNestingDepth = cfg.getInt("limits.max_cue_nesting_depth",    DEFAULT_MAX_CUE_NESTING);
        maxFireworksPerSecond = cfg.getInt("limits.max_fireworks_per_second", DEFAULT_MAX_FW_PER_SECOND);
        maxFireworksPerShot   = cfg.getInt("limits.max_fireworks_per_shot",   DEFAULT_MAX_FW_PER_SHOT);
        maxParticlesPerEvent  = cfg.getInt("limits.max_particles_per_event",  DEFAULT_MAX_PARTICLES);
    }

    public int getCooldownSeconds()     { return cooldownSeconds; }
    public int getResumeWindowSeconds() { return resumeWindowSeconds; }
    public int getStopSlowFallSeconds() { return stopSlowFallSeconds; }
    public String getStopMessage()      { return stopMessage; }
    public String getStopSoundId()      { return stopSoundId; }
    public String getStopSoundCategory(){ return stopSoundCategory; }
    public float  getStopSoundVolume()  { return stopSoundVolume; }
    public float  getStopSoundPitch()   { return stopSoundPitch; }
    public int getMaxCueNestingDepth()  { return maxCueNestingDepth; }
    public int getMaxFireworksPerSecond(){ return maxFireworksPerSecond; }
    public int getMaxFireworksPerShot() { return maxFireworksPerShot; }
    public int getMaxParticlesPerEvent(){ return maxParticlesPerEvent; }
}
