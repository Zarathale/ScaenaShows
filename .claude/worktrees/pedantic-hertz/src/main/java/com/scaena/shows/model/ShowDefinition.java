package com.scaena.shows.model;

import java.util.List;

public record ShowDefinition(
        String id,
        String name,
        String description,
        ShowMode defaultMode,
        AudienceMode defaultAudience,
        ShowBossbarDefaults bossbar,
        List<ShowTimelineEntry> timeline
) {}
