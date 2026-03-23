package com.scaena.shows.model;

import java.util.List;

public record ItemPayload(
        String material,
        int amount,
        String nameMiniMessage,
        List<String> loreMiniMessage
) {}
