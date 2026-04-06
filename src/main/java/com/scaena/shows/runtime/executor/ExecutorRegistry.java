package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.EventType;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.registry.FireworkRegistry;
import com.scaena.shows.runtime.RunningShow;
import com.scaena.shows.runtime.ShowManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps each EventType to its executor and dispatches events.
 * All executors are wired up once at plugin startup.
 */
public final class ExecutorRegistry {

    private final Map<EventType, EventExecutor> executors = new EnumMap<>(EventType.class);
    private final Logger log;

    /** Retained so ShowManager can be injected after construction (avoids circular dep). */
    private final TextEventExecutor textExecutor;

    public ExecutorRegistry(JavaPlugin plugin, FireworkRegistry fireworkRegistry, CueRegistry cueRegistry) {
        this.log = plugin.getLogger();

        TextEventExecutor  text      = new TextEventExecutor(plugin);
        this.textExecutor = text;
        SoundEventExecutor sound     = new SoundEventExecutor(plugin);
        VisualEventExecutor visual   = new VisualEventExecutor(plugin);
        TeamEventExecutor  team      = new TeamEventExecutor(plugin, cueRegistry, log);
        FireworkEventExecutor fw     = new FireworkEventExecutor(plugin, fireworkRegistry);
        WorldEventExecutor world     = new WorldEventExecutor(plugin);
        EntityEventExecutor entity   = new EntityEventExecutor(log);
        StageEventExecutor stage     = new StageEventExecutor(plugin);
        PlayerEventExecutor player   = new PlayerEventExecutor(plugin);
        UtilityEventExecutor utility = new UtilityEventExecutor(log);

        // Break construction-time circularity: TeamEventExecutor needs ExecutorRegistry to dispatch
        // GROUP_EVENT sub-cue events. Set it via setter after this registry is constructed.
        team.setExecutorRegistry(this);

        // §6.1 Text and Display
        executors.put(EventType.MESSAGE,         text);
        executors.put(EventType.TITLE,           text);
        executors.put(EventType.TITLE_CLEAR,     text);
        executors.put(EventType.ACTION_BAR,      text);
        executors.put(EventType.BOSSBAR,         text);
        executors.put(EventType.PLAYER_CHOICE,   text);
        executors.put(EventType.BOSS_HEALTH_BAR, entity); // entity-linked — routed to EntityEventExecutor

        // §6.2 Sound
        executors.put(EventType.SOUND,      sound);
        executors.put(EventType.STOP_SOUND, sound);

        // §6.3 Visual Effects
        executors.put(EventType.PARTICLE,  visual);
        executors.put(EventType.EFFECT,    visual);
        executors.put(EventType.GLOW,      visual);
        executors.put(EventType.CAMERA,    visual);
        executors.put(EventType.LIGHTNING, visual);

        // §6.4 Team and Groups
        executors.put(EventType.GROUP_ASSIGN, team);
        executors.put(EventType.TEAM_COLOR,   team);
        executors.put(EventType.GROUP_EVENT,  team);

        // §6.5 Fireworks
        executors.put(EventType.FIREWORK,        fw);
        executors.put(EventType.FIREWORK_CIRCLE, fw);
        executors.put(EventType.FIREWORK_LINE,   fw);
        executors.put(EventType.FIREWORK_FAN,    fw);
        executors.put(EventType.FIREWORK_RANDOM, fw);

        // §6.6 World and Environment
        executors.put(EventType.WEATHER,         world);
        executors.put(EventType.TIME_OF_DAY,     world);
        executors.put(EventType.REDSTONE,        world);
        executors.put(EventType.BLOCK_PLACE,     world);
        executors.put(EventType.BLOCK_REMOVE,    world);
        executors.put(EventType.BLOCK_STATE,     world);
        executors.put(EventType.SET_ITEM_FRAME,  world);   // OPS-007

        // §6.7 Entity Management
        executors.put(EventType.SPAWN_ENTITY,     entity);
        executors.put(EventType.DESPAWN_ENTITY,   entity);
        executors.put(EventType.CAPTURE_ENTITIES, entity);
        executors.put(EventType.RELEASE_ENTITIES, entity);

        // §6.8 Entity Behavior
        executors.put(EventType.ENTITY_AI,        entity);
        executors.put(EventType.ENTITY_SPEED,     entity);
        executors.put(EventType.ENTITY_EFFECT,    entity);
        executors.put(EventType.ENTITY_EQUIP,     entity);
        executors.put(EventType.ENTITY_INVISIBLE, entity);
        executors.put(EventType.ENTITY_VELOCITY,  entity);

        // §6.9 Stage Directions
        executors.put(EventType.HOLD,        stage);
        executors.put(EventType.FACE,        stage);
        executors.put(EventType.ROTATE,      stage);   // OPS-005
        executors.put(EventType.CROSS_TO,    stage);
        executors.put(EventType.RETURN_HOME, stage);
        executors.put(EventType.ENTER,       stage);
        executors.put(EventType.EXIT,        stage);

        // §6.10 Player Movement
        executors.put(EventType.PLAYER_TELEPORT,     player);
        executors.put(EventType.PLAYER_VELOCITY,     player);
        executors.put(EventType.PLAYER_SPECTATE,     player);
        executors.put(EventType.PLAYER_SPECTATE_END, player);
        executors.put(EventType.PLAYER_MOUNT,        player);
        executors.put(EventType.PLAYER_DISMOUNT,     player);
        executors.put(EventType.PLAYER_FLIGHT,       player);

        // §6.11 Utility
        executors.put(EventType.REST,    utility);
        executors.put(EventType.COMMAND, utility);
        executors.put(EventType.CUE,     utility);

        // §6.12 Tech Rehearsal
        executors.put(EventType.PAUSE,   utility);   // OPS-029: no-op executor
    }

    /**
     * Wire ShowManager into TextEventExecutor after both are fully constructed.
     * Must be called from ScaenaShowsPlugin after ShowManager is built.
     */
    public void setShowManager(ShowManager showManager) {
        textExecutor.setShowManager(showManager);
    }

    /**
     * Dispatch a single event to its registered executor.
     * Logs a warning if no executor is found (should not happen in normal operation).
     */
    public void dispatch(ShowEvent event, RunningShow show) {
        EventExecutor executor = executors.get(event.type());
        if (executor == null) {
            log.warning("[ScaenaShows] No executor registered for event type: " + event.type());
            return;
        }
        try {
            executor.execute(event, show);
        } catch (Exception ex) {
            log.severe("[ScaenaShows] Error executing " + event.type()
                + " at tick " + event.at + " in show '" + show.show.id + "': " + ex.getMessage());
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                ex.printStackTrace();
            }
        }
    }
}
