package com.scaena.shows.model;

public enum AudienceMode {
    BROADCAST,
    PRIVATE;

    public static AudienceMode fromString(String s, AudienceMode def) {
        if (s == null) return def;
        return switch (s.toLowerCase()) {
            case "broadcast" -> BROADCAST;
            case "private" -> PRIVATE;
            default -> def;
        };
    }
}
