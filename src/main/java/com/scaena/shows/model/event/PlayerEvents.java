package com.scaena.shows.model.event;

import java.util.Map;

/** §6.10 — PLAYER_TELEPORT, PLAYER_VELOCITY, PLAYER_SPECTATE, PLAYER_SPECTATE_END,
 *           PLAYER_MOUNT, PLAYER_DISMOUNT, PLAYER_FLIGHT */
public final class PlayerEvents {

    private PlayerEvents() {}

    public static final class PlayerTeleportEvent extends ShowEvent {
        public final String audience;
        public final String destination;  // set:Name OR null if using offset
        public final double offsetX, offsetY, offsetZ;
        public final boolean hasOffset;
        public final float yaw;
        public final float pitch;

        public PlayerTeleportEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience    = str(m, "audience", "participants");
            this.destination = str(m, "destination", null);
            Map<String, Object> off = mapVal(m, "offset");
            this.hasOffset   = m.containsKey("offset");
            this.offsetX     = dblVal(off, "x", 0);
            this.offsetY     = dblVal(off, "y", 0);
            this.offsetZ     = dblVal(off, "z", 0);
            this.yaw         = fltVal(m, "yaw", Float.NaN);
            this.pitch       = fltVal(m, "pitch", Float.NaN);
        }

        @Override public EventType type() { return EventType.PLAYER_TELEPORT; }
    }

    public static final class PlayerVelocityEvent extends ShowEvent {
        public final String audience;
        public final double vecX, vecY, vecZ;

        public PlayerVelocityEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience = str(m, "audience", "participants");
            Map<String, Object> v = mapVal(m, "vector");
            this.vecX = dblVal(v, "x", 0);
            this.vecY = dblVal(v, "y", 0);
            this.vecZ = dblVal(v, "z", 0);
        }

        @Override public EventType type() { return EventType.PLAYER_VELOCITY; }
    }

    public static final class PlayerSpectateEvent extends ShowEvent {
        public final String entity;       // entity:spawned:Name
        public final String audience;
        public final int durationTicks;   // -1 = until PLAYER_SPECTATE_END

        public PlayerSpectateEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.entity        = str(m, "entity", "");
            this.audience      = str(m, "audience", "participants");
            this.durationTicks = intVal(m, "duration_ticks", -1);
        }

        @Override public EventType type() { return EventType.PLAYER_SPECTATE; }
    }

    public static final class PlayerSpectateEndEvent extends ShowEvent {
        public final String audience;

        public PlayerSpectateEndEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience = str(m, "audience", "participants");
        }

        @Override public EventType type() { return EventType.PLAYER_SPECTATE_END; }
    }

    public static final class PlayerMountEvent extends ShowEvent {
        public final String entity;
        public final String audience;

        public PlayerMountEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.entity   = str(m, "entity", "");
            this.audience = str(m, "audience", "participants");
        }

        @Override public EventType type() { return EventType.PLAYER_MOUNT; }
    }

    public static final class PlayerDismountEvent extends ShowEvent {
        public final String audience;

        public PlayerDismountEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience = str(m, "audience", "participants");
        }

        @Override public EventType type() { return EventType.PLAYER_DISMOUNT; }
    }

    /**
     * PLAYER_FLIGHT — engage or release server-side flight for participants.
     *
     * state: hover   — calls setAllowFlight(true) + setFlying(true); freezes the player
     *                   at their current Y. Pre-show flight state captured on first hover.
     * state: release — applies release_effect first (slow descent), then restores the
     *                   player's pre-show flight state. Safe to call even if hover was
     *                   never fired (no-op on flight state if nothing was recorded).
     */
    public static final class PlayerFlightEvent extends ShowEvent {
        public final String audience;
        /** "hover" | "release" */
        public final String state;
        /** Effect to apply on release before disabling flight: "slow_falling" | "levitate" | "none" */
        public final String releaseEffect;
        /** Duration of the release effect in ticks (default 300). */
        public final int    releaseDurationTicks;

        public PlayerFlightEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience             = str(m, "audience", "participants");
            this.state                = str(m, "state", "hover");
            this.releaseEffect        = str(m, "release_effect", "slow_falling");
            this.releaseDurationTicks = intVal(m, "release_duration_ticks", 300);
        }

        @Override public EventType type() { return EventType.PLAYER_FLIGHT; }
    }
}
