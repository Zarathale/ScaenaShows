package com.scaena.shows.model.event;

/**
 * All supported event types. Used to dispatch YAML parsing and runtime execution.
 * Organised by spec §6 category groupings.
 */
public enum EventType {

    // §6.1 Text and Display
    MESSAGE, TITLE, TITLE_CLEAR, ACTION_BAR, BOSSBAR, PLAYER_CHOICE, BOSS_HEALTH_BAR,

    // §6.2 Sound
    SOUND, STOP_SOUND,

    // §6.3 Visual Effects
    PARTICLE, EFFECT, GLOW, CAMERA, LIGHTNING,

    // §6.4 Team and Groups
    GROUP_ASSIGN, TEAM_COLOR, GROUP_EVENT,

    // §6.5 Fireworks and Spatial Patterns
    FIREWORK, FIREWORK_CIRCLE, FIREWORK_LINE, FIREWORK_FAN, FIREWORK_RANDOM,

    // §6.6 World and Environment
    WEATHER, TIME_OF_DAY, REDSTONE,
    BLOCK_PLACE, BLOCK_REMOVE, BLOCK_STATE,
    SET_ITEM_FRAME,

    // §6.7 Entity Management
    SPAWN_ENTITY, DESPAWN_ENTITY, CAPTURE_ENTITIES, RELEASE_ENTITIES,

    // §6.8 Entity Behavior
    ENTITY_AI, ENTITY_SPEED, ENTITY_EFFECT, ENTITY_EQUIP,
    ENTITY_INVISIBLE, ENTITY_VELOCITY,

    // §6.9 Stage Directions
    HOLD, FACE, ROTATE, CROSS_TO, RETURN_HOME, ENTER, EXIT,

    // §6.10 Player Movement
    PLAYER_TELEPORT, PLAYER_VELOCITY, PLAYER_SPECTATE,
    PLAYER_SPECTATE_END, PLAYER_MOUNT, PLAYER_DISMOUNT,
    PLAYER_FLIGHT,

    // §6.11 Utility
    REST, COMMAND, CUE;

    /** Parse from YAML string; throws IllegalArgumentException for unknown types. */
    public static EventType fromString(String raw) {
        try {
            return valueOf(raw.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: '" + raw + "'");
        }
    }
}
