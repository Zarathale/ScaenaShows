package com.scaena.shows.model;

import java.util.Map;

/**
 * Launch parameters for a firework preset.
 * Controls where the rocket spawns relative to the target.
 */
public record FireworkLaunch(
    String mode,       // above | random | feet
    double yOffset,
    double spread      // only relevant for mode=random
) {
    @SuppressWarnings("unchecked")
    public static FireworkLaunch from(Object raw) {
        if (!(raw instanceof Map<?, ?> mRaw)) return defaults();
        Map<String, Object> m = (Map<String, Object>) mRaw;
        String mode   = str(m, "mode", "above");
        double yOff   = dbl(m, "y_offset", 1.0);
        double spread = dbl(m, "spread", 0.0);
        return new FireworkLaunch(mode, yOff, spread);
    }

    public static FireworkLaunch defaults() {
        return new FireworkLaunch("above", 1.0, 0.0);
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    private static double dbl(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        return v instanceof Number n ? n.doubleValue() : def;
    }
}
