package com.scaena.shows.model;

public enum ShowMode {
    FOLLOW,
    STATIC;

    public static ShowMode fromString(String s, ShowMode def) {
        if (s == null) return def;
        return switch (s.toLowerCase()) {
            case "follow" -> FOLLOW;
            case "static" -> STATIC;
            default -> def;
        };
    }
}
