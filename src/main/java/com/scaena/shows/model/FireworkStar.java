package com.scaena.shows.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * One star within a firework preset.
 * Multiple stars fire simultaneously from the same rocket.
 */
public record FireworkStar(
    String shape,           // BALL | BALL_LARGE | STAR | BURST | CREEPER
    List<String> colors,    // hex strings e.g. "#FECB00"
    List<String> fades,     // hex strings for fade colors
    boolean trail,
    boolean flicker
) {
    @SuppressWarnings("unchecked")
    public static FireworkStar from(Map<String, Object> m) {
        String shape = str(m, "type",   "BALL");     // spec uses "type" inside stars
        if (shape.isEmpty()) shape = str(m, "shape", "BALL"); // fallback to "shape"

        List<String> colors = toStringList(m.get("colors"));
        List<String> fades  = toStringList(m.get("fades"));
        boolean trail   = bool(m, "trail",   false);
        boolean flicker = bool(m, "flicker", false);

        return new FireworkStar(shape.toUpperCase(), colors, fades, trail, flicker);
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) if (item != null) out.add(item.toString());
            return List.copyOf(out);
        }
        return List.of();
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    private static boolean bool(Map<String, Object> m, String key, boolean def) {
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }
}
