package com.scaena.shows.registry;

import com.scaena.shows.model.event.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Parses a raw YAML timeline list into typed ShowEvent objects.
 * Dispatches on the "type" field, then sorts events at the same tick
 * with GROUP_ASSIGN first, CAPTURE_ENTITIES second, rest in YAML order.
 */
public final class EventParser {

    private EventParser() {}

    /**
     * Parse a list of raw YAML event maps into ShowEvent objects.
     * Preserves YAML order within the same tick category.
     */
    @SuppressWarnings("unchecked")
    public static List<ShowEvent> parseTimeline(List<?> rawTimeline) {
        if (rawTimeline == null) return List.of();

        List<ShowEvent> events = new ArrayList<>();
        for (Object raw : rawTimeline) {
            if (!(raw instanceof Map<?, ?> mRaw)) continue;
            Map<String, Object> m = (Map<String, Object>) mRaw;
            String typeStr = m.containsKey("type") ? m.get("type").toString() : "";
            if (typeStr.isEmpty()) continue;

            EventType type;
            try {
                type = EventType.fromString(typeStr);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown event type '" + typeStr + "' in timeline");
            }

            ShowEvent event = switch (type) {
                // §6.1 Text and Display
                case MESSAGE     -> new TextEvents.MessageEvent(m);
                case TITLE       -> new TextEvents.TitleEvent(m);
                case TITLE_CLEAR -> new TextEvents.TitleClearEvent(m);
                case ACTION_BAR  -> new TextEvents.ActionBarEvent(m);
                case BOSSBAR          -> new TextEvents.BossbarEvent(m);
                case PLAYER_CHOICE    -> new TextEvents.PlayerChoiceEvent(m);
                case BOSS_HEALTH_BAR  -> new TextEvents.BossHealthBarEvent(m);
                // §6.2 Sound
                case SOUND      -> new SoundEvents.SoundEvent(m);
                case STOP_SOUND -> new SoundEvents.StopSoundEvent(m);
                // §6.3 Visual
                case PARTICLE   -> new VisualEvents.ParticleEvent(m);
                case EFFECT     -> new VisualEvents.EffectEvent(m);
                case GLOW       -> new VisualEvents.GlowEvent(m);
                case CAMERA     -> new VisualEvents.CameraEvent(m);
                case LIGHTNING  -> new VisualEvents.LightningEvent(m);
                // §6.4 Team
                case GROUP_ASSIGN -> new TeamEvents.GroupAssignEvent(m);
                case TEAM_COLOR   -> new TeamEvents.TeamColorEvent(m);
                case GROUP_EVENT  -> new TeamEvents.GroupEvent(m);
                // §6.5 Fireworks
                case FIREWORK        -> new FireworkEvents.FireworkEvent(m);
                case FIREWORK_CIRCLE -> new FireworkEvents.FireworkCircleEvent(m);
                case FIREWORK_LINE   -> new FireworkEvents.FireworkLineEvent(m);
                case FIREWORK_FAN    -> new FireworkEvents.FireworkFanEvent(m);
                case FIREWORK_RANDOM -> new FireworkEvents.FireworkRandomEvent(m);
                // §6.6 World
                case WEATHER      -> new WorldEvents.WeatherEvent(m);
                case TIME_OF_DAY  -> new WorldEvents.TimeOfDayEvent(m);
                case REDSTONE     -> new WorldEvents.RedstoneEvent(m);
                case BLOCK_PLACE    -> new WorldEvents.BlockPlaceEvent(m);
                case BLOCK_REMOVE   -> new WorldEvents.BlockRemoveEvent(m);
                case BLOCK_STATE    -> new WorldEvents.BlockStateEvent(m);
                case SET_ITEM_FRAME -> new WorldEvents.SetItemFrameEvent(m);    // OPS-007
                // §6.7 Entity management
                case SPAWN_ENTITY      -> new EntityMgmtEvents.SpawnEntityEvent(m);
                case DESPAWN_ENTITY    -> new EntityMgmtEvents.DespawnEntityEvent(m);
                case CAPTURE_ENTITIES  -> new EntityMgmtEvents.CaptureEntitiesEvent(m);
                case RELEASE_ENTITIES  -> new EntityMgmtEvents.ReleaseEntitiesEvent(m);
                // §6.8 Entity behavior
                case ENTITY_AI        -> new EntityBehaviorEvents.EntityAiEvent(m);
                case ENTITY_SPEED     -> new EntityBehaviorEvents.EntitySpeedEvent(m);
                case ENTITY_EFFECT    -> new EntityBehaviorEvents.EntityEffectEvent(m);
                case ENTITY_EQUIP     -> new EntityBehaviorEvents.EntityEquipEvent(m);
                case ENTITY_INVISIBLE -> new EntityBehaviorEvents.EntityInvisibleEvent(m);
                case ENTITY_VELOCITY  -> new EntityBehaviorEvents.EntityVelocityEvent(m);
                // §6.9 Stage directions
                case HOLD        -> new StageEvents.HoldEvent(m);
                case FACE        -> new StageEvents.FaceEvent(m);
                case ROTATE      -> new StageEvents.RotateEvent(m);              // OPS-005
                case CROSS_TO    -> new StageEvents.CrossToEvent(m);
                case RETURN_HOME -> new StageEvents.ReturnHomeEvent(m);
                case ENTER       -> new StageEvents.EnterEvent(m);
                case EXIT        -> new StageEvents.ExitEvent(m);
                // §6.10 Player movement
                case PLAYER_TELEPORT     -> new PlayerEvents.PlayerTeleportEvent(m);
                case PLAYER_VELOCITY     -> new PlayerEvents.PlayerVelocityEvent(m);
                case PLAYER_SPECTATE     -> new PlayerEvents.PlayerSpectateEvent(m);
                case PLAYER_SPECTATE_END -> new PlayerEvents.PlayerSpectateEndEvent(m);
                case PLAYER_MOUNT        -> new PlayerEvents.PlayerMountEvent(m);
                case PLAYER_DISMOUNT     -> new PlayerEvents.PlayerDismountEvent(m);
                case PLAYER_FLIGHT       -> new PlayerEvents.PlayerFlightEvent(m);
                // §6.11 Utility
                case REST    -> new UtilityEvents.RestEvent(m);
                case COMMAND -> new UtilityEvents.CommandEvent(m);
                case CUE     -> new UtilityEvents.CueRefEvent(m);
            };
            events.add(event);
        }

        // Sort: stable sort preserving YAML order within a tick,
        // but GROUP_ASSIGN always first, CAPTURE_ENTITIES second.
        events.sort(Comparator
            .comparingInt((ShowEvent e) -> e.at)
            .thenComparingInt(EventParser::dispatchPriority));

        return List.copyOf(events);
    }

    /** Lower = higher priority within the same tick. */
    private static int dispatchPriority(ShowEvent e) {
        return switch (e.type()) {
            case GROUP_ASSIGN     -> 0;
            case CAPTURE_ENTITIES -> 1;
            default               -> 2;
        };
    }
}
