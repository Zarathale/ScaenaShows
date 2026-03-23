package com.scaena.shows.model;

import java.util.List;

public record Sequence(
        String id,
        String name,
        String description,
        int durationTicks,
        List<Shot> shots
) {}
