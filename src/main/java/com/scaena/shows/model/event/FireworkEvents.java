package com.scaena.shows.model.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** §6.5 — FIREWORK, FIREWORK_CIRCLE, FIREWORK_LINE, FIREWORK_FAN, FIREWORK_RANDOM */
public final class FireworkEvents {

    private FireworkEvents() {}

    // ------------------------------------------------------------------
    // Shared sub-structures
    // ------------------------------------------------------------------

    public record Chase(boolean enabled, String direction /* FL | LF */, int intervalTicks, String mode) {
        @SuppressWarnings("unchecked")
        public static Chase from(Object raw) {
            if (!(raw instanceof Map<?, ?> mRaw)) return new Chase(false, "FL", 4, "sequential");
            Map<String, Object> m = (Map<String, Object>) mRaw;
            boolean en  = ShowEvent.boolVal(m, "enabled", false);
            String dir  = ShowEvent.str(m, "direction", "FL");
            int interval= ShowEvent.intVal(m, "interval_ticks", 4);
            String mode = ShowEvent.str(m, "mode", "sequential");
            return new Chase(en, dir, interval, mode);
        }
    }

    public record XZOffset(double x, double z) {
        @SuppressWarnings("unchecked")
        public static XZOffset from(Object raw, String xKey, String zKey) {
            if (!(raw instanceof Map<?, ?> mRaw)) return new XZOffset(0, 0);
            Map<String, Object> m = (Map<String, Object>) mRaw;
            return new XZOffset(
                ShowEvent.dblVal(m, xKey != null ? xKey : "x", 0),
                ShowEvent.dblVal(m, zKey != null ? zKey : "z", 0)
            );
        }
        public static XZOffset fromMap(Map<String, Object> m) {
            return new XZOffset(ShowEvent.dblVal(m, "x", 0), ShowEvent.dblVal(m, "z", 0));
        }
    }

    // ------------------------------------------------------------------
    // FIREWORK (single launch)
    // ------------------------------------------------------------------
    public static final class FireworkEvent extends ShowEvent {
        public final String preset;
        public final double offsetX, offsetY, offsetZ;
        public final String yMode;        // relative | surface
        public final int minClearance;    // -1 = not set

        public FireworkEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.preset  = str(m, "preset", "");
            Map<String, Object> off = mapVal(m, "offset");
            this.offsetX = dblVal(off, "x", 0);
            this.offsetY = dblVal(off, "y", 2);
            this.offsetZ = dblVal(off, "z", 0);
            this.yMode         = str(m, "y_mode", "relative");
            this.minClearance  = intVal(m, "min_clearance", -1);
        }

        @Override public EventType type() { return EventType.FIREWORK; }
    }

    // ------------------------------------------------------------------
    // FIREWORK_CIRCLE
    // ------------------------------------------------------------------
    public static final class FireworkCircleEvent extends ShowEvent {
        public final String preset;
        public final XZOffset originOffset;
        public final double radius;
        public final int count;
        public final String yMode;
        public final double yOffset;
        public final Chase chase;
        public final String powerVariation;   // UNIFORM | RAMP_UP | RAMP_DOWN | ALTERNATE | RANDOM
        public final String colorVariation;   // UNIFORM | RAINBOW | GRADIENT | ALTERNATE
        public final String gradientFrom;
        public final String gradientTo;

        public FireworkCircleEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.preset          = str(m, "preset", "");
            this.originOffset    = XZOffset.from(m.get("origin_offset"), "x", "z");
            this.radius          = dblVal(m, "radius", 10);
            this.count           = intVal(m, "count", 8);
            this.yMode           = str(m, "y_mode", "surface");
            this.yOffset         = dblVal(m, "y_offset", 2);
            this.chase           = Chase.from(m.get("chase"));
            this.powerVariation  = str(m, "power_variation", "UNIFORM").toUpperCase();
            this.colorVariation  = str(m, "color_variation", "UNIFORM").toUpperCase();
            this.gradientFrom    = str(m, "gradient_from", "#FF0000");
            this.gradientTo      = str(m, "gradient_to", "#0000FF");
        }

        @Override public EventType type() { return EventType.FIREWORK_CIRCLE; }
    }

    // ------------------------------------------------------------------
    // FIREWORK_LINE
    // ------------------------------------------------------------------
    public static final class FireworkLineEvent extends ShowEvent {
        public final String preset;
        public final XZOffset startOffset;
        public final double length;
        /**
         * Compass/bearing angle — clockwise from north.
         * 0=north(-Z), 90=east(+X), 180=south(+Z), 270=west(-X).
         * Convert to radians: radians = (90 - degrees) * PI / 180
         */
        public final double angle;
        public final int count;
        public final String yMode;
        public final double yOffset;
        public final Chase chase;
        public final String powerVariation;
        public final String colorVariation;
        public final String gradientFrom;
        public final String gradientTo;

        public FireworkLineEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.preset        = str(m, "preset", "");
            this.startOffset   = XZOffset.from(m.get("start_offset"), "x", "z");
            this.length        = dblVal(m, "length", 10);
            this.angle         = dblVal(m, "angle", 0);
            this.count         = intVal(m, "count", 6);
            this.yMode         = str(m, "y_mode", "surface");
            this.yOffset       = dblVal(m, "y_offset", 2);
            this.chase         = Chase.from(m.get("chase"));
            this.powerVariation= str(m, "power_variation", "UNIFORM").toUpperCase();
            this.colorVariation= str(m, "color_variation", "UNIFORM").toUpperCase();
            this.gradientFrom  = str(m, "gradient_from", "#FF0000");
            this.gradientTo    = str(m, "gradient_to", "#0000FF");
        }

        @Override public EventType type() { return EventType.FIREWORK_LINE; }
    }

    // ------------------------------------------------------------------
    // FIREWORK_FAN
    // ------------------------------------------------------------------
    public static final class FireworkFanEvent extends ShowEvent {
        public final XZOffset originOffset;
        public final List<FanArm> arms;
        public final String yMode;
        public final double yOffset;
        public final Chase chase;

        public record FanArm(double angle, double length, int count, String preset) {}

        @SuppressWarnings("unchecked")
        public FireworkFanEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.originOffset = XZOffset.from(m.get("origin_offset"), "x", "z");
            this.yMode        = str(m, "y_mode", "surface");
            this.yOffset      = dblVal(m, "y_offset", 2);
            this.chase        = Chase.from(m.get("chase"));

            List<Map<String, Object>> armMaps = (List<Map<String, Object>>) m.getOrDefault("arms", List.of());
            List<FanArm> armList = new ArrayList<>();
            for (Map<String, Object> a : armMaps) {
                armList.add(new FanArm(
                    dblVal(a, "angle", 0),
                    dblVal(a, "length", 8),
                    intVal(a, "count", 4),
                    str(a, "preset", "")
                ));
            }
            this.arms = List.copyOf(armList);
        }

        @Override public EventType type() { return EventType.FIREWORK_FAN; }
    }

    // ------------------------------------------------------------------
    // FIREWORK_RANDOM — scatter pattern; N fireworks at random XZ positions within a radius.
    // All fireworks launch simultaneously (no chase).
    // ------------------------------------------------------------------
    public static final class FireworkRandomEvent extends ShowEvent {
        public final String preset;
        public final XZOffset originOffset;
        public final double radius;
        public final int count;
        public final String yMode;
        public final double yOffset;
        public final Long seed; // null = different every run

        public FireworkRandomEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.preset       = str(m, "preset", "");
            this.originOffset = XZOffset.from(m.get("origin_offset"), "x", "z");
            this.radius       = dblVal(m, "radius", 10);
            this.count        = intVal(m, "count", 6);
            this.yMode        = str(m, "y_mode", "surface");
            this.yOffset      = dblVal(m, "y_offset", 2);
            Object seedRaw    = m.get("seed");
            this.seed         = (seedRaw instanceof Number n) ? n.longValue() : null;
        }

        @Override public EventType type() { return EventType.FIREWORK_RANDOM; }
    }
}
