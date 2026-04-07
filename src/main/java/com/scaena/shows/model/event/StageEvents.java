package com.scaena.shows.model.event;

import java.util.Map;

/** §6.9 — HOLD, FACE, CROSS_TO, RETURN_HOME, ENTER, EXIT */
public final class StageEvents {

    private StageEvents() {}

    public static final class HoldEvent extends ShowEvent {
        public final String target;

        public HoldEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target = str(m, "target", "");
        }

        @Override public EventType type() { return EventType.HOLD; }
    }

    public static final class FaceEvent extends ShowEvent {
        public final String target;
        public final String lookAt; // mark:Name | player | compass:south | entity:spawned:Other

        public FaceEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target = str(m, "target", "");
            this.lookAt = str(m, "look_at", "mark:center");
        }

        @Override public EventType type() { return EventType.FACE; }
    }

    // ------------------------------------------------------------------
    // ROTATE — smoothly rotate target's yaw to a destination angle (OPS-005)
    // OPS-040 adds optional pitch interpolation via delta_pitch or pitch.
    // ------------------------------------------------------------------
    public static final class RotateEvent extends ShowEvent {
        public final String target;
        public final float yaw;           // absolute target yaw; NaN if using delta
        public final float delta;         // relative yaw change; NaN if using yaw
        public final int durationTicks;   // 0 or omitted = instant (same as FACE)
        public final double deltaPitch;   // relative pitch change in degrees; NaN if using pitch
        public final double pitch;        // absolute target pitch; NaN if using deltaPitch or absent

        public RotateEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target        = str(m, "target", "player");
            this.durationTicks = intVal(m, "duration_ticks", 0);
            // delta wins over yaw if both are present — per spec
            boolean hasDelta = m.containsKey("delta");
            boolean hasYaw   = m.containsKey("yaw");
            if (hasDelta) {
                this.delta = fltVal(m, "delta", 0f);
                this.yaw   = Float.NaN;
            } else if (hasYaw) {
                this.yaw   = fltVal(m, "yaw", 0f);
                this.delta = Float.NaN;
            } else {
                // Neither supplied — treat as no-op; delta=0 instant
                this.delta = 0f;
                this.yaw   = Float.NaN;
            }
            // pitch wins over delta_pitch if both are present — OPS-040
            boolean hasPitch      = m.containsKey("pitch");
            boolean hasDeltaPitch = m.containsKey("delta_pitch");
            if (hasPitch) {
                this.pitch      = dblVal(m, "pitch", 0.0);
                this.deltaPitch = Double.NaN;
            } else if (hasDeltaPitch) {
                this.deltaPitch = dblVal(m, "delta_pitch", 0.0);
                this.pitch      = Double.NaN;
            } else {
                // Neither supplied — no pitch change
                this.deltaPitch = 0.0;
                this.pitch      = Double.NaN;
            }
        }

        /** True if this event uses relative yaw delta; false = absolute yaw. */
        public boolean isDelta() { return !Float.isNaN(delta); }

        /** True if pitch interpolation is configured. */
        public boolean hasPitchChange() { return !Double.isNaN(pitch) || !Double.isNaN(deltaPitch); }

        /** True if using relative delta_pitch; false = absolute pitch. */
        public boolean isPitchDelta() { return !Double.isNaN(deltaPitch); }

        @Override public EventType type() { return EventType.ROTATE; }
    }

    public static final class CrossToEvent extends ShowEvent {
        public final String target;
        public final String destination; // mark:Name | home | home+{...} | {x,z}
        public final int durationTicks;  // 0 = instant
        public final String facing;      // optional

        public CrossToEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target        = str(m, "target", "");
            this.destination   = str(m, "destination", "mark:center");
            this.durationTicks = intVal(m, "duration_ticks", 0);
            this.facing        = str(m, "facing", null);
        }

        @Override public EventType type() { return EventType.CROSS_TO; }
    }

    public static final class ReturnHomeEvent extends ShowEvent {
        public final String target;
        public final int durationTicks;

        public ReturnHomeEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target        = str(m, "target", "");
            this.durationTicks = intVal(m, "duration_ticks", 20);
        }

        @Override public EventType type() { return EventType.RETURN_HOME; }
    }

    public static final class EnterEvent extends ShowEvent {
        public final String entityType;
        public final String name;
        public final String from;        // mark:wing_left etc.
        public final String destination;
        public final int durationTicks;
        public final String facing;
        public final boolean baby;
        public final String helmetItem;
        public final String chestplateItem;
        public final String leggingsItem;
        public final String bootsItem;
        public final String mainHandItem;
        public final String offHandItem;

        public EnterEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.entityType    = str(m, "entity_type", "VILLAGER");
            this.name          = str(m, "name", "");
            this.from          = str(m, "from", "mark:wing_left");
            this.destination   = str(m, "destination", "mark:center");
            this.durationTicks = intVal(m, "duration_ticks", 30);
            this.facing        = str(m, "facing", null);
            this.baby          = boolVal(m, "baby", false);
            Map<String, Object> eq = mapVal(m, "equipment");
            this.helmetItem     = str(eq, "helmet", "");
            this.chestplateItem = str(eq, "chestplate", "");
            this.leggingsItem   = str(eq, "leggings", "");
            this.bootsItem      = str(eq, "boots", "");
            this.mainHandItem   = str(eq, "main_hand", "");
            this.offHandItem    = str(eq, "off_hand", "");
        }

        @Override public EventType type() { return EventType.ENTER; }
    }

    public static final class ExitEvent extends ShowEvent {
        public final String target;
        public final String to;          // mark:wing_right etc.
        public final int durationTicks;
        public final boolean despawnOnArrival;

        public ExitEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target          = str(m, "target", "");
            this.to              = str(m, "to", "mark:wing_right");
            this.durationTicks   = intVal(m, "duration_ticks", 30);
            this.despawnOnArrival= boolVal(m, "despawn_on_arrival", true);
        }

        @Override public EventType type() { return EventType.EXIT; }
    }
}
