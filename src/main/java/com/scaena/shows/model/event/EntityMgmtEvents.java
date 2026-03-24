package com.scaena.shows.model.event;

import java.util.Map;

/** §6.7 — SPAWN_ENTITY, DESPAWN_ENTITY, CAPTURE_ENTITIES, RELEASE_ENTITIES */
public final class EntityMgmtEvents {

    private EntityMgmtEvents() {}

    // ------------------------------------------------------------------
    // SPAWN_ENTITY
    // ------------------------------------------------------------------
    public static final class SpawnEntityEvent extends ShowEvent {
        public final String entityType;
        public final double offsetX, offsetY, offsetZ;
        public final String name;          // required for subsequent targeting
        public final boolean baby;
        public final String variant;       // optional
        public final String profession;    // optional (villagers)
        public final String helmetItem;
        public final String chestplateItem;
        public final String leggingsItem;
        public final String bootsItem;
        public final String mainHandItem;
        public final String offHandItem;
        public final boolean despawnOnEnd;

        @SuppressWarnings("unchecked")
        public SpawnEntityEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.entityType  = str(m, "entity_type", "VILLAGER");
            Map<String, Object> off = mapVal(m, "offset");
            this.offsetX     = dblVal(off, "x", 0);
            this.offsetY     = dblVal(off, "y", 0);
            this.offsetZ     = dblVal(off, "z", 0);
            this.name        = str(m, "name", "");
            this.baby        = boolVal(m, "baby", false);
            this.variant     = str(m, "variant", null);
            this.profession  = str(m, "profession", null);
            this.despawnOnEnd= boolVal(m, "despawn_on_end", true);
            Map<String, Object> eq = mapVal(m, "equipment");
            this.helmetItem     = str(eq, "helmet", "");
            this.chestplateItem = str(eq, "chestplate", "");
            this.leggingsItem   = str(eq, "leggings", "");
            this.bootsItem      = str(eq, "boots", "");
            this.mainHandItem   = str(eq, "main_hand", "");
            this.offHandItem    = str(eq, "off_hand", "");
        }

        @Override public EventType type() { return EventType.SPAWN_ENTITY; }
    }

    // ------------------------------------------------------------------
    // DESPAWN_ENTITY
    // ------------------------------------------------------------------
    public static final class DespawnEntityEvent extends ShowEvent {
        public final String target;
        public final boolean particleBurst;

        public DespawnEntityEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target        = str(m, "target", "");
            this.particleBurst = boolVal(m, "particle_burst", false);
        }

        @Override public EventType type() { return EventType.DESPAWN_ENTITY; }
    }

    // ------------------------------------------------------------------
    // CAPTURE_ENTITIES — dispatched second at any tick (after GROUP_ASSIGN)
    // ------------------------------------------------------------------
    public static final class CaptureEntitiesEvent extends ShowEvent {
        public final String entityType;
        public final double radius;
        public final int maxCount;
        public final String groupName;
        public final String captureMode; // snapshot | live

        public CaptureEntitiesEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.entityType  = str(m, "entity_type", "COW");
            this.radius      = dblVal(m, "radius", 15);
            this.maxCount    = intVal(m, "max_count", 8);
            this.groupName   = str(m, "group_name", "");
            this.captureMode = str(m, "capture_mode", "snapshot");
        }

        @Override public EventType type() { return EventType.CAPTURE_ENTITIES; }
    }

    // ------------------------------------------------------------------
    // RELEASE_ENTITIES — releases a captured entity group from show control.
    // Removes the group from the show's entity group registry.
    // Optionally re-enables AI on all entities in the group.
    // Silent no-op if the group doesn't exist or has zero members.
    // ------------------------------------------------------------------
    public static final class ReleaseEntitiesEvent extends ShowEvent {
        public final String target;     // entity_group:<name>
        public final boolean restoreAi; // default true

        public ReleaseEntitiesEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target    = str(m, "target", "");
            this.restoreAi = boolVal(m, "restore_ai", true);
        }

        @Override public EventType type() { return EventType.RELEASE_ENTITIES; }
    }
}
