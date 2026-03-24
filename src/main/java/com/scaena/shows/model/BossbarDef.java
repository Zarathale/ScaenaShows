package com.scaena.shows.model;

import java.util.Map;

/**
 * Show-level bossbar definition (show.bossbar in YAML).
 * Same animation logic as the inline BOSSBAR event.
 */
public record BossbarDef(
    boolean enabled,
    String title,
    String color,
    String overlay,
    String audience,
    int fadeInTicks,
    int fadeOutTicks
) {
    @SuppressWarnings("unchecked")
    public static BossbarDef from(Object raw) {
        if (!(raw instanceof Map<?, ?> mRaw)) return disabled();
        Map<String, Object> m = (Map<String, Object>) mRaw;
        boolean enabled   = bool(m, "enabled", false);
        String title      = str(m, "title", "");
        String color      = str(m, "color", "WHITE");
        String overlay    = str(m, "overlay", "PROGRESS");
        String audience   = str(m, "audience", "broadcast");
        int fadeIn        = intVal(m, "fade_in_ticks", 10);
        int fadeOut       = intVal(m, "fade_out_ticks", 20);
        return new BossbarDef(enabled, title, color, overlay, audience, fadeIn, fadeOut);
    }

    public static BossbarDef disabled() {
        return new BossbarDef(false, "", "WHITE", "PROGRESS", "broadcast", 10, 20);
    }

    private static String  str(Map<String, Object> m, String k, String  d) { Object v=m.get(k); return v!=null?v.toString():d; }
    private static boolean bool(Map<String, Object> m, String k, boolean d) { Object v=m.get(k); if(v==null)return d; if(v instanceof Boolean b)return b; return Boolean.parseBoolean(v.toString()); }
    private static int     intVal(Map<String, Object> m, String k, int    d) { Object v=m.get(k); return v instanceof Number n?n.intValue():d; }
}
