package com.scaena.shows.model.event;

import java.util.Map;

/** §6.6 — WEATHER, TIME_OF_DAY, REDSTONE */
public final class WorldEvents {

    private WorldEvents() {}

    // ------------------------------------------------------------------
    // WEATHER
    // ------------------------------------------------------------------
    public static final class WeatherEvent extends ShowEvent {
        public final String state;         // clear | storm | thunder
        public final int durationTicks;    // -1 = persistent

        public WeatherEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.state         = str(m, "state", "clear");
            this.durationTicks = intVal(m, "duration_ticks", -1);
        }

        @Override public EventType type() { return EventType.WEATHER; }
    }

    // ------------------------------------------------------------------
    // TIME_OF_DAY
    // ------------------------------------------------------------------
    public static final class TimeOfDayEvent extends ShowEvent {
        public final long time; // 0=sunrise, 6000=noon, 12000=sunset, 18000=midnight

        public TimeOfDayEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.time = ((Number) m.getOrDefault("time", 6000)).longValue();
        }

        @Override public EventType type() { return EventType.TIME_OF_DAY; }
    }

    // ------------------------------------------------------------------
    // REDSTONE
    // ------------------------------------------------------------------
    public static final class RedstoneEvent extends ShowEvent {
        public final boolean worldSpecific;
        public final int targetX, targetY, targetZ;
        public final boolean state; // true = on

        public RedstoneEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.worldSpecific = boolVal(m, "world_specific", true);
            Map<String, Object> tgt = mapVal(m, "target");
            this.targetX = intVal(tgt, "x", 0);
            this.targetY = intVal(tgt, "y", 64);
            this.targetZ = intVal(tgt, "z", 0);
            this.state   = "on".equalsIgnoreCase(str(m, "state", "on"));
        }

        @Override public EventType type() { return EventType.REDSTONE; }
    }
}
