package com.scaena.shows.model;

public record BossbarPayload(
        AudienceMode audience,
        Action action,
        String titleMiniMessage,
        String color,
        String overlay
) {
    public enum Action { SHOW, HIDE }
}
