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

    // ------------------------------------------------------------------
    // SET_ITEM_FRAME — point-in-time item frame content and state control (OPS-007)
    //
    // target: entity:world:Name | entity:spawned:Name
    // item:   namespaced item ID (minecraft:saddle), or minecraft:air to clear
    // visible: show/hide frame border; omitted = no change
    // fixed:   lock frame against player interaction; omitted = no change
    // rotation: 0–7 mapped to Rotation enum; omitted = no change
    //
    // Stop-safety: entity:world targets are snapshotted and restored on show end.
    //              entity:spawned targets are despawned on show end — no restore needed.
    // ------------------------------------------------------------------
    public static final class SetItemFrameEvent extends ShowEvent {
        public final String target;
        public final String item;        // namespaced item ID; null = no change
        public final Boolean visible;    // null = no change
        public final Boolean fixed;      // null = no change
        public final Integer rotation;   // null = no change; 0–7 → Rotation enum

        public SetItemFrameEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target   = str(m, "target", "");
            this.item     = m.containsKey("item") ? str(m, "item", null) : null;
            this.visible  = m.containsKey("visible")  ? boolVal(m, "visible", true)  : null;
            this.fixed    = m.containsKey("fixed")     ? boolVal(m, "fixed", false)   : null;
            this.rotation = m.containsKey("rotation")  ? intVal(m,  "rotation", 0)    : null;
        }

        @Override public EventType type() { return EventType.SET_ITEM_FRAME; }
    }
}
