package com.scaena.shows.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A named firework preset from fireworks.yml.
 * Supports multiple stars per rocket (all detonate simultaneously).
 */
public record FireworkPreset(
    String id,
    String displayName,
    int power,
    List<FireworkStar> stars
) {
    @SuppressWarnings("unchecked")
    public static FireworkPreset from(String id, Map<String, Object> m) {
        String displayName = str(m, "display_name", id);
        int power = ((Number) m.getOrDefault("power", 1)).intValue();

        List<FireworkStar> stars = new ArrayList<>();
        Object starsRaw = m.get("stars");
        if (starsRaw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> sm) {
                    stars.add(FireworkStar.from((Map<String, Object>) sm));
                }
            }
        }

        return new FireworkPreset(id, displayName, power, List.copyOf(stars));
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }
}
