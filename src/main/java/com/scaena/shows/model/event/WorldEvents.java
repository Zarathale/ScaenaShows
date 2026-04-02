package com.scaena.shows.model.event;

import java.util.LinkedHashMap;
import java.util.Map;

/** §6.6 — WEATHER, TIME_OF_DAY, REDSTONE, BLOCK_PLACE, BLOCK_REMOVE, BLOCK_STATE */
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

    // ------------------------------------------------------------------
    // BLOCK_PLACE  (OPS-004)
    // Places a fully specified block at an absolute world coordinate.
    // On show end, the original block is restored by stop-safety.
    //
    // YAML:
    //   type: BLOCK_PLACE
    //   at: 10
    //   world_specific: true
    //   target: {x: 100, y: 64, z: 200}
    //   block: "minecraft:blast_furnace[facing=north,lit=false]"
    // ------------------------------------------------------------------
    public static final class BlockPlaceEvent extends ShowEvent {
        public final boolean worldSpecific;
        public final int targetX, targetY, targetZ;
        public final String block; // full blockstate string

        public BlockPlaceEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.worldSpecific = boolVal(m, "world_specific", true);
            Map<String, Object> tgt = mapVal(m, "target");
            this.targetX = intVal(tgt, "x", 0);
            this.targetY = intVal(tgt, "y", 64);
            this.targetZ = intVal(tgt, "z", 0);
            this.block   = str(m, "block", "minecraft:air");
        }

        @Override public EventType type() { return EventType.BLOCK_PLACE; }
    }

    // ------------------------------------------------------------------
    // BLOCK_REMOVE  (OPS-004)
    // Sets a block to AIR at an absolute world coordinate.
    // On show end, the original block is restored by stop-safety.
    //
    // YAML:
    //   type: BLOCK_REMOVE
    //   at: 20
    //   world_specific: true
    //   target: {x: 100, y: 64, z: 200}
    // ------------------------------------------------------------------
    public static final class BlockRemoveEvent extends ShowEvent {
        public final boolean worldSpecific;
        public final int targetX, targetY, targetZ;

        public BlockRemoveEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.worldSpecific = boolVal(m, "world_specific", true);
            Map<String, Object> tgt = mapVal(m, "target");
            this.targetX = intVal(tgt, "x", 0);
            this.targetY = intVal(tgt, "y", 64);
            this.targetZ = intVal(tgt, "z", 0);
        }

        @Override public EventType type() { return EventType.BLOCK_REMOVE; }
    }

    // ------------------------------------------------------------------
    // BLOCK_STATE  (OPS-008)
    // Patches one or more state properties on an existing block without
    // changing its block type (e.g., lit=true on a blast furnace).
    // On show end, the original block state is restored by stop-safety.
    //
    // YAML:
    //   type: BLOCK_STATE
    //   at: 5
    //   world_specific: true
    //   target: {x: 100, y: 64, z: 200}
    //   state:
    //     lit: true
    //     powered: false
    // ------------------------------------------------------------------
    public static final class BlockStateEvent extends ShowEvent {
        public final boolean worldSpecific;
        public final int targetX, targetY, targetZ;
        /** State key/value pairs — all values normalised to String. */
        public final Map<String, String> state;

        @SuppressWarnings("unchecked")
        public BlockStateEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.worldSpecific = boolVal(m, "world_specific", true);
            Map<String, Object> tgt = mapVal(m, "target");
            this.targetX = intVal(tgt, "x", 0);
            this.targetY = intVal(tgt, "y", 64);
            this.targetZ = intVal(tgt, "z", 0);

            // Parse state map; normalise all values to String
            Map<String, String> parsed = new LinkedHashMap<>();
            Object rawState = m.get("state");
            if (rawState instanceof Map<?, ?> rawMap) {
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        parsed.put(entry.getKey().toString(),
                                   entry.getValue().toString().toLowerCase());
                    }
                }
            }
            this.state = Map.copyOf(parsed);
        }

        @Override public EventType type() { return EventType.BLOCK_STATE; }
    }
}
