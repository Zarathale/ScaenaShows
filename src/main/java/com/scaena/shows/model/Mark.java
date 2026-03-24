package com.scaena.shows.model;

import java.util.Map;

/**
 * A named XZ offset position relative to the show spatial anchor.
 * Defined under marks: in the show YAML.
 */
public record Mark(String name, double x, double z) {

    @SuppressWarnings("unchecked")
    public static Mark from(String name, Object raw) {
        if (!(raw instanceof Map<?, ?> mRaw)) return new Mark(name, 0, 0);
        Map<String, Object> m = (Map<String, Object>) mRaw;
        double x = m.containsKey("x") ? ((Number) m.get("x")).doubleValue() : 0;
        double z = m.containsKey("z") ? ((Number) m.get("z")).doubleValue() : 0;
        return new Mark(name, x, z);
    }
}
