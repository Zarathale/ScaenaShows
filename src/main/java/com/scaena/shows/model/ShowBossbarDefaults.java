package com.scaena.shows.model;

public record ShowBossbarDefaults(
        boolean enabled,
        String titleMiniMessage,
        String color,
        String overlay,
        AudienceMode audience
) {}
