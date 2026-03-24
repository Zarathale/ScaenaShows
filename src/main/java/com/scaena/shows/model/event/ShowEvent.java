package com.scaena.shows.model.event;

import java.util.Map;

/**
 * Abstract base for every timeline event.
 * Concrete subclasses carry the event-specific fields parsed from YAML.
 */
public abstract class ShowEvent {

    /** The tick at which this event fires (relative to show start). */
    public final int at;

    protected ShowEvent(int at) {
        this.at = at;
    }

    /** The discriminating event type — used for executor dispatch. */
    public abstract EventType type();

    // -----------------------------------------------------------------------
    // Helpers for YAML map parsing (used by subclass constructors)
    // -----------------------------------------------------------------------

    protected static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    protected static int intVal(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v == null) return def;
        return ((Number) v).intValue();
    }

    protected static double dblVal(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        if (v == null) return def;
        return ((Number) v).doubleValue();
    }

    protected static float fltVal(Map<String, Object> m, String key, float def) {
        return (float) dblVal(m, key, def);
    }

    protected static boolean boolVal(Map<String, Object> m, String key, boolean def) {
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, Object> mapVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Map<?, ?> mv) return (Map<String, Object>) mv;
        return Map.of();
    }
}
