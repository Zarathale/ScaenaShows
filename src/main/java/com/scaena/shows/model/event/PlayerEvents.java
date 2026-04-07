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

    /**
     * PLAYER_SPECTATE — Phase 2 extended version.
     *
     * Two mutually exclusive modes:
     *   spawn: mode — entity is born at spectate time (invisible by default for camera drones).
     *   entity: mode — references an entity already present in the scene.
     *
     * If the {@code spawn:} key is present in the YAML map, spawn mode is active.
     * Otherwise entity mode is used (original behaviour).
     */
    public static final class PlayerSpectateEvent extends ShowEvent {
        // ---- entity: mode ----
        public final String entity;       // entity:spawned:Name (or "" in spawn mode)

        // ---- spawn: mode ----
        public final boolean spawnMode;
        public final String  spawnName;
        public final String  spawnType;   // EntityType name, e.g. "ARMOR_STAND"
        public final double  spawnOffX;
        public final double  spawnOffY;
        public final double  spawnOffZ;
        public final boolean despawnOnEnd;

        // ---- shared ----
        public final String audience;
        public final int    durationTicks;   // -1 = until PLAYER_SPECTATE_END (Phase 2 shortcut)

        @SuppressWarnings("unchecked")
        public PlayerSpectateEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience      = str(m, "audience", "participants");
            this.durationTicks = intVal(m, "duration_ticks", -1);

            if (m.containsKey("spawn")) {
                // spawn: mode
                this.spawnMode = true;
                Map<String, Object> sp = m.get("spawn") instanceof Map<?, ?> spRaw
                    ? (Map<String, Object>) spRaw : Map.of();
                this.spawnName = str(sp, "name", "CinematicCamera");
                this.spawnType = str(sp, "type", "ARMOR_STAND").toUpperCase();
                Map<String, Object> off = sp.get("offset") instanceof Map<?, ?> offRaw
                    ? (Map<String, Object>) offRaw : Map.of();
                this.spawnOffX      = dblVal(off, "x", 0);
                this.spawnOffY      = dblVal(off, "y", 0);
                this.spawnOffZ      = dblVal(off, "z", 0);
                this.despawnOnEnd   = boolVal(sp, "despawn_on_end", true);
                this.entity         = "";
            } else {
                // entity: mode (original)
                this.spawnMode    = false;
                this.spawnName    = "";
                this.spawnType    = "ARMOR_STAND";
                this.spawnOffX    = 0;
                this.spawnOffY    = 0;
                this.spawnOffZ    = 0;
                this.despawnOnEnd = false;
                this.entity       = str(m, "entity", "");
            }
        }

        @Override public EventType type() { return EventType.PLAYER_SPECTATE; }
    }

    /**
     * PLAYER_SPECTATE_END — Phase 2 extended version.
     *
     * destination: restore (default) | mark:Name | entity:spawned:Name
     */
    public static final class PlayerSpectateEndEvent extends ShowEvent {
        public final String audience;
        /**
         * Where to place the player when spectate ends.
         * "restore"           — return to pre-spectate position (default)
         * "mark:Name"         — teleport to a defined mark
         * "entity:spawned:N"  — teleport to the drone's current position
         */
        public final String destination;

        public PlayerSpectateEndEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience    = str(m, "audience", "participants");
            this.destination = str(m, "destination", "restore");
        }

        @Override public EventType type() { return EventType.PLAYER_SPECTATE_END; }
    }

    /**
     * PLAYER_MOUNT — Phase 2 extended version.
     *
     * Two mutually exclusive modes:
     *   spawn: mode — entity is born at mount time.
     *   entity: mode — references an entity already present in the scene.
     */
    public static final class PlayerMountEvent extends ShowEvent {
        // ---- entity: mode ----
        public final String  entity;

        // ---- spawn: mode ----
        public final boolean spawnMode;
        public final String  spawnName;
        public final String  spawnType;
        public final double  spawnOffX;
        public final double  spawnOffY;
        public final double  spawnOffZ;
        public final boolean spawnInvisible;
        public final boolean despawnOnDismount;

        // ---- shared ----
        public final String audience;
        public final int    durationTicks;   // -1 = until PLAYER_DISMOUNT (Phase 2 shortcut)

        @SuppressWarnings("unchecked")
        public PlayerMountEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience      = str(m, "audience", "participants");
            this.durationTicks = intVal(m, "duration_ticks", -1);

            if (m.containsKey("spawn")) {
                this.spawnMode = true;
                Map<String, Object> sp = m.get("spawn") instanceof Map<?, ?> spRaw
                    ? (Map<String, Object>) spRaw : Map.of();
                this.spawnName          = str(sp, "name", "GuideEntity");
                this.spawnType          = str(sp, "type", "HORSE").toUpperCase();
                Map<String, Object> off = sp.get("offset") instanceof Map<?, ?> offRaw
                    ? (Map<String, Object>) offRaw : Map.of();
                this.spawnOffX          = dblVal(off, "x", 0);
                this.spawnOffY          = dblVal(off, "y", 0);
                this.spawnOffZ          = dblVal(off, "z", 0);
                this.spawnInvisible     = boolVal(sp, "invisible", false);
                this.despawnOnDismount  = boolVal(sp, "despawn_on_dismount", true);
                this.entity             = "";
            } else {
                this.spawnMode         = false;
                this.spawnName         = "";
                this.spawnType         = "HORSE";
                this.spawnOffX         = 0;
                this.spawnOffY         = 0;
                this.spawnOffZ         = 0;
                this.spawnInvisible    = false;
                this.despawnOnDismount = false;
                this.entity            = str(m, "entity", "");
            }
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
