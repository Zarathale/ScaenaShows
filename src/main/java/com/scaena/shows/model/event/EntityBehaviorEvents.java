package com.scaena.shows.model.event;

import java.util.Map;

/** §6.8 — ENTITY_AI, ENTITY_SPEED, ENTITY_EFFECT, ENTITY_EQUIP, ENTITY_INVISIBLE, ENTITY_VELOCITY */
public final class EntityBehaviorEvents {

    private EntityBehaviorEvents() {}

    public static final class EntityAiEvent extends ShowEvent {
        public final String target;
        public final boolean enabled;

        public EntityAiEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target  = str(m, "target", "");
            this.enabled = boolVal(m, "enabled", true);
        }

        @Override public EventType type() { return EventType.ENTITY_AI; }
    }

    public static final class EntitySpeedEvent extends ShowEvent {
        public final String target;
        public final double speed;

        public EntitySpeedEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target = str(m, "target", "");
            this.speed  = dblVal(m, "speed", 1.0);
        }

        @Override public EventType type() { return EventType.ENTITY_SPEED; }
    }

    public static final class EntityEffectEvent extends ShowEvent {
        public final String target;
        public final String effectId;
        public final int durationTicks;
        public final int amplifier;

        public EntityEffectEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target        = str(m, "target", "");
            this.effectId      = str(m, "effect_id", "slowness");
            this.durationTicks = intVal(m, "duration_ticks", 100);
            this.amplifier     = intVal(m, "amplifier", 0);
        }

        @Override public EventType type() { return EventType.ENTITY_EFFECT; }
    }

    public static final class EntityEquipEvent extends ShowEvent {
        public final String target;
        public final String helmet, chestplate, leggings, boots, mainHand, offHand;

        public EntityEquipEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target     = str(m, "target", "");
            this.helmet     = str(m, "helmet", "");
            this.chestplate = str(m, "chestplate", "");
            this.leggings   = str(m, "leggings", "");
            this.boots      = str(m, "boots", "");
            this.mainHand   = str(m, "main_hand", "");
            this.offHand    = str(m, "off_hand", "");
        }

        @Override public EventType type() { return EventType.ENTITY_EQUIP; }
    }

    public static final class EntityInvisibleEvent extends ShowEvent {
        public final String target;
        public final int durationTicks;

        public EntityInvisibleEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target        = str(m, "target", "");
            this.durationTicks = intVal(m, "duration_ticks", 60);
        }

        @Override public EventType type() { return EventType.ENTITY_INVISIBLE; }
    }

    public static final class EntityVelocityEvent extends ShowEvent {
        public final String target;
        public final double vecX, vecY, vecZ;

        public EntityVelocityEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target = str(m, "target", "");
            Map<String, Object> v = mapVal(m, "vector");
            this.vecX = dblVal(v, "x", 0);
            this.vecY = dblVal(v, "y", 0);
            this.vecZ = dblVal(v, "z", 0);
        }

        @Override public EventType type() { return EventType.ENTITY_VELOCITY; }
    }
}
