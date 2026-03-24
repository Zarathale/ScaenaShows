package com.scaena.shows.model;

public record TitlePayload(
        AudienceMode audience,
        String titleMiniMessage,
        String subtitleMiniMessage,
        int fadeInTicks,
        int stayTicks,
        int fadeOutTicks
) {}
