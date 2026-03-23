package com.scaena.shows.model;

import java.util.List;

public record Scene(
        String id,
        String name,
        int durationTicks,
        List<SceneEvent> events
) {}
