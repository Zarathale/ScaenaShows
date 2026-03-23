package com.scaena.shows.config;

public record ScaenaShowsConfig(
        int defaultCooldownSeconds,
        String cooldownBypassPermission,
        int resumeWindowSeconds,
        int stopSafetySlowFallingSeconds,
        String stopMessageMiniMessage,
        SoundSpec stopSound,
        int maxEventsPerShow,
        int maxFireworksPerSecond,
        int maxParticlesPerEvent,
        String defaultMode,            // follow|static (string to allow validation)
        boolean defaultBroadcast,
        boolean defaultBossbarEnabled,
        String defaultBossbarColor,
        String defaultBossbarOverlay,
        String adminPermission,
        String playPermission,
        String targetOthersPermission,
        String targetAllPermission,
        String privateFlagPermission,
        int maxFireworksPerShot,
        int maxSequenceNestingDepth
) {}
