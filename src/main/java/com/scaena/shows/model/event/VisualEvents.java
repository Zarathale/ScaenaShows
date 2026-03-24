package com.scaena.shows.model.event;

import java.util.List;
import java.util.Map;

/** §6.3 — PARTICLE, EFFECT, GLOW, CAMERA, LIGHTNING */
public final class VisualEvents {

    private VisualEvents() {}

    // ------------------------------------------------------------------
    // PARTICLE
    // ------------------------------------------------------------------
    public static final class ParticleEvent extends ShowEvent {
        public final String particleId;
        public final int count;
        public final double offsetX, offsetY, offsetZ;
        public final double extra;
        public final boolean force;
        public final int durationTicks;   // -1 = point-in-time
        public final int intervalTicks;

        @SuppressWarnings("unchecked")
        public ParticleEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.particleId   = str(m, "particle_id", "minecraft:heart");
            this.count        = intVal(m, "count", 1);
            this.extra        = dblVal(m, "extra", 0.0);
            this.force        = boolVal(m, "force", false);
            this.durationTicks= intVal(m, "duration_ticks", -1);
            this.intervalTicks= intVal(m, "interval_ticks", 20);
            List<?> off = (List<?>) m.getOrDefault("offset", List.of(0.5, 0.5, 0.5));
            this.offsetX = off.size() > 0 ? ((Number) off.get(0)).doubleValue() : 0.5;
            this.offsetY = off.size() > 1 ? ((Number) off.get(1)).doubleValue() : 0.5;
            this.offsetZ = off.size() > 2 ? ((Number) off.get(2)).doubleValue() : 0.5;
        }

        @Override public EventType type() { return EventType.PARTICLE; }
    }

    // ------------------------------------------------------------------
    // EFFECT (potion effect on players)
    // ------------------------------------------------------------------
    public static final class EffectEvent extends ShowEvent {
        public final String effectId;
        public final int durationTicks;
        public final int amplifier;
        public final boolean hideParticles;
        public final String audience;

        public EffectEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.effectId      = str(m, "effect_id", "night_vision");
            this.durationTicks = intVal(m, "duration_ticks", 200);
            this.amplifier     = intVal(m, "amplifier", 0);
            this.hideParticles = boolVal(m, "hide_particles", true);
            this.audience      = str(m, "audience", "participants");
        }

        @Override public EventType type() { return EventType.EFFECT; }
    }

    // ------------------------------------------------------------------
    // GLOW
    // ------------------------------------------------------------------
    public static final class GlowEvent extends ShowEvent {
        public final String color;
        public final int durationTicks;
        public final String audience;

        public GlowEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.color         = str(m, "color", "WHITE");
            this.durationTicks = intVal(m, "duration_ticks", 200);
            this.audience      = str(m, "audience", "participants");
        }

        @Override public EventType type() { return EventType.GLOW; }
    }

    // ------------------------------------------------------------------
    // CAMERA
    // ------------------------------------------------------------------
    public static final class CameraEvent extends ShowEvent {
        public final String effect;   // sway | blackout | flash | float
        public final int intensity;
        public final int durationTicks;
        public final String audience;

        public CameraEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.effect        = str(m, "effect", "sway");
            this.intensity     = intVal(m, "intensity", 1);
            this.durationTicks = intVal(m, "duration_ticks", 40);
            this.audience      = str(m, "audience", "participants");
        }

        @Override public EventType type() { return EventType.CAMERA; }
    }

    // ------------------------------------------------------------------
    // LIGHTNING (cosmetic only — strikeLightningEffect, no damage)
    // ------------------------------------------------------------------
    public static final class LightningEvent extends ShowEvent {
        public final double offsetX, offsetY, offsetZ;

        public LightningEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            Map<String, Object> off = mapVal(m, "offset");
            this.offsetX = dblVal(off, "x", 0);
            this.offsetY = dblVal(off, "y", 0);
            this.offsetZ = dblVal(off, "z", 0);
        }

        @Override public EventType type() { return EventType.LIGHTNING; }
    }
}
