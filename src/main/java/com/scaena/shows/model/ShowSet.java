package com.scaena.shows.model;

import java.util.Map;

/**
 * A named world-specific teleport destination (show.sets in YAML).
 * return_on_end: true ensures players are never stranded.
 */
public record ShowSet(
    String name,
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch,
    boolean returnOnEnd
) {
    @SuppressWarnings("unchecked")
    public static ShowSet from(String name, Object raw) {
        if (!(raw instanceof Map<?, ?> mRaw)) return null;
        Map<String, Object> m = (Map<String, Object>) mRaw;
        return new ShowSet(
            name,
            str(m, "world", "world"),
            dbl(m, "x", 0),
            dbl(m, "y", 64),
            dbl(m, "z", 0),
            (float) dbl(m, "yaw", 0),
            (float) dbl(m, "pitch", 0),
            bool(m, "return_on_end", false)
        );
    }

    private static String  str(Map<String, Object> m, String k, String  d) { Object v=m.get(k); return v!=null?v.toString():d; }
    private static double  dbl(Map<String, Object> m, String k, double  d) { Object v=m.get(k); return v instanceof Number n?n.doubleValue():d; }
    private static boolean bool(Map<String, Object> m, String k, boolean d) { Object v=m.get(k); if(v==null)return d; if(v instanceof Boolean b)return b; return Boolean.parseBoolean(v.toString()); }
}
