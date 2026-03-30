package com.scaena.shows.runtime;

import com.scaena.shows.model.Show;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Live state for one /show play invocation.
 * One RunningShow per invocation; all participants share this instance.
 */
public final class RunningShow {

    // -----------------------------------------------------------------------
    // Identity
    // -----------------------------------------------------------------------
    public final String instanceId;          // UUID string for uniqueness
    public final Show show;

    // -----------------------------------------------------------------------
    // Invocation options
    // -----------------------------------------------------------------------
    public final boolean privateMode;        // --private flag
    public final boolean scenesMode;         // --scenes (director mode)
    public final String followMode;          // "follow" | "static"

    // -----------------------------------------------------------------------
    // Participants (keyed by UUID)
    // -----------------------------------------------------------------------
    private final Map<UUID, ParticipantState> participants;
    private UUID spatialAnchorUuid;
    private UUID invokerUuid;

    // -----------------------------------------------------------------------
    // Tick counter
    // -----------------------------------------------------------------------
    private long currentTick = 0;
    private boolean running  = true;

    // -----------------------------------------------------------------------
    // Group assignments (group 1..N → list of UUIDs)
    // -----------------------------------------------------------------------
    private final Map<Integer, List<UUID>> groups = new HashMap<>();

    // -----------------------------------------------------------------------
    // Named entities spawned by this show (name → Entity)
    // -----------------------------------------------------------------------
    private final Map<String, Entity> spawnedEntities = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Spawn-home locations for named entities (name → Location at spawn time)
    // Used by RETURN_HOME to send non-Player entities back to origin.
    // -----------------------------------------------------------------------
    private final Map<String, Location> spawnedEntityHomes = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Named entity groups captured by CAPTURE_ENTITIES (group_name → list of Entity UUIDs)
    // -----------------------------------------------------------------------
    private final Map<String, List<UUID>> entityGroups = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Active duration events (tracked for cleanup and bossbar animation)
    // -----------------------------------------------------------------------

    /** Show-level bossbar (may be null if show.bossbar.enabled = false) */
    private BossBar showBossBar = null;

    /** Per-event bossbars for inline BOSSBAR events */
    private final List<BossBar> activeBossBars = new ArrayList<>();

    /** Track scoreboard team names created by this show (for cleanup) */
    private final Set<String> ownedTeams = new HashSet<>();

    /** Track spectate prior gamemodes per player (UUID → prior GameMode) */
    private final Map<UUID, org.bukkit.GameMode> spectateRestoreMap = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Flight state (captured on first PLAYER_FLIGHT hover per participant)
    // -----------------------------------------------------------------------

    /**
     * Pre-show flight state per participant — captured the first time PLAYER_FLIGHT
     * hover fires. putIfAbsent semantics ensure the true pre-show state is preserved
     * even if hover fires multiple times.
     */
    public record FlightState(boolean allowFlight, boolean wasFlying) {}
    private final Map<UUID, FlightState> flightRestoreMap = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Spatial anchor position (used for follow mode + static fallback)
    // -----------------------------------------------------------------------
    private Location anchorLocation;   // updated each tick in follow mode

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public RunningShow(
        Show show,
        List<Player> participants,
        Player invoker,
        boolean privateMode,
        boolean scenesMode,
        String followMode
    ) {
        this.instanceId  = UUID.randomUUID().toString().substring(0, 8);
        this.show        = show;
        this.privateMode = privateMode;
        this.scenesMode  = scenesMode;
        this.followMode  = followMode;
        this.invokerUuid = invoker != null ? invoker.getUniqueId() : null;

        Map<UUID, ParticipantState> pmap = new LinkedHashMap<>();
        boolean firstDone = false;
        for (Player p : participants) {
            boolean isAnchor = !firstDone;
            ParticipantState ps = new ParticipantState(p, isAnchor);
            pmap.put(p.getUniqueId(), ps);
            if (isAnchor) {
                spatialAnchorUuid = p.getUniqueId();
                anchorLocation    = p.getLocation().clone();
                firstDone = true;
            }
        }
        this.participants = Collections.unmodifiableMap(pmap);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public Map<UUID, ParticipantState> getParticipants() { return participants; }
    public long getCurrentTick()  { return currentTick; }
    public boolean isRunning()    { return running; }
    public boolean isPrivate()    { return privateMode; }
    public boolean isScenesMode() { return scenesMode; }
    public String getFollowMode() { return followMode; }

    public Player getInvoker() {
        if (invokerUuid == null) return null;
        return org.bukkit.Bukkit.getPlayer(invokerUuid);
    }

    public Player getSpatialAnchor() {
        if (spatialAnchorUuid == null) return null;
        return org.bukkit.Bukkit.getPlayer(spatialAnchorUuid);
    }

    public Location getAnchorLocation() {
        if ("follow".equalsIgnoreCase(followMode)) {
            Player anchor = getSpatialAnchor();
            if (anchor != null && anchor.isOnline()) {
                anchorLocation = anchor.getLocation().clone();
            }
            // If anchor disconnected, fall back to last known (static fallback per spec §15)
        }
        return anchorLocation;
    }

    public void setAnchorLocation(Location loc) {
        this.anchorLocation = loc;
    }

    /** All participants that are currently online. */
    public List<Player> getOnlineParticipants() {
        List<Player> out = new ArrayList<>();
        for (ParticipantState ps : participants.values()) {
            if (!ps.isConnected()) continue;
            Player p = org.bukkit.Bukkit.getPlayer(ps.uuid);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }

    /** Returns participants assigned to the given group number (1-based). */
    public List<Player> getGroupPlayers(int groupNum) {
        List<UUID> uuids = groups.getOrDefault(groupNum, List.of());
        List<Player> out = new ArrayList<>();
        for (UUID uid : uuids) {
            Player p = org.bukkit.Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }

    /** Assign group membership (called by GroupAssignExecutor at tick 0). */
    public void setGroups(Map<Integer, List<UUID>> groupMap) {
        groups.clear();
        groups.putAll(groupMap);
        // Write group number back onto each ParticipantState
        for (Map.Entry<Integer, List<UUID>> entry : groupMap.entrySet()) {
            for (UUID uid : entry.getValue()) {
                ParticipantState ps = participants.get(uid);
                if (ps != null) ps.groupNumber = entry.getKey();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Tick management
    // -----------------------------------------------------------------------

    /** Advance the internal tick counter by 1. */
    public void tick() {
        currentTick++;
    }

    /** Signal that the show has ended or been stopped. */
    public void stop() {
        running = false;
    }

    // -----------------------------------------------------------------------
    // Group scope override (used by GROUP_EVENT executor)
    // When non-zero, AudienceResolver restricts "broadcast" and "participants"
    // to only players in the specified group number (1–4).
    // Set immediately before and cleared immediately after each event dispatch.
    // Safe because all Bukkit tasks run on the main thread (no concurrency).
    // -----------------------------------------------------------------------
    private int activeGroupScope = 0; // 0 = no override; 1..4 = restrict to group N

    public int getActiveGroupScope()         { return activeGroupScope; }
    public void setActiveGroupScope(int scope) { this.activeGroupScope = scope; }

    // -----------------------------------------------------------------------
    // Spawned entities
    // -----------------------------------------------------------------------

    public void registerSpawnedEntity(String name, Entity entity) {
        spawnedEntities.put(name, entity);
        // Record spawn location as the entity's home for RETURN_HOME support
        spawnedEntityHomes.put(name, entity.getLocation().clone());
    }

    /** Returns the spawn-time home location for a named entity, or null if unknown. */
    public Location getEntityHome(String name) {
        return spawnedEntityHomes.get(name);
    }

    public Entity getSpawnedEntity(String name) {
        return spawnedEntities.get(name);
    }

    public Map<String, Entity> getSpawnedEntities() {
        return Collections.unmodifiableMap(spawnedEntities);
    }

    // -----------------------------------------------------------------------
    // Entity groups
    // -----------------------------------------------------------------------

    public void setEntityGroup(String groupName, List<UUID> entityUuids) {
        entityGroups.put(groupName, List.copyOf(entityUuids));
    }

    public List<UUID> getEntityGroup(String groupName) {
        return entityGroups.getOrDefault(groupName, List.of());
    }

    /** Remove an entity group from show tracking (called by RELEASE_ENTITIES). Entities remain in the world. */
    public void releaseEntityGroup(String groupName) {
        entityGroups.remove(groupName);
    }

    // -----------------------------------------------------------------------
    // Bossbars
    // -----------------------------------------------------------------------

    public BossBar getShowBossBar()                  { return showBossBar; }
    public void setShowBossBar(BossBar bar)          { this.showBossBar = bar; }
    public void addActiveBossBar(BossBar bar)        { activeBossBars.add(bar); }
    public List<BossBar> getActiveBossBars()         { return activeBossBars; }

    // -----------------------------------------------------------------------
    // Teams
    // -----------------------------------------------------------------------

    public void registerOwnedTeam(String teamName)   { ownedTeams.add(teamName); }
    public Set<String> getOwnedTeams()               { return ownedTeams; }

    // -----------------------------------------------------------------------
    // Spectate restore
    // -----------------------------------------------------------------------

    public void recordSpectateRestore(UUID uuid, org.bukkit.GameMode prior) {
        spectateRestoreMap.put(uuid, prior);
    }

    public Map<UUID, org.bukkit.GameMode> getSpectateRestoreMap() {
        return spectateRestoreMap;
    }

    // -----------------------------------------------------------------------
    // Flight restore
    // -----------------------------------------------------------------------

    /**
     * Record the pre-show flight state for a participant.
     * Uses putIfAbsent — only the first call (true pre-show state) is retained.
     */
    public void recordFlightRestore(UUID uuid, boolean allowFlight, boolean wasFlying) {
        flightRestoreMap.putIfAbsent(uuid, new FlightState(allowFlight, wasFlying));
    }

    /** Returns the recorded pre-show flight state, or null if hover was never fired. */
    public FlightState getFlightRestore(UUID uuid) {
        return flightRestoreMap.get(uuid);
    }

    public Map<UUID, FlightState> getFlightRestoreMap() {
        return flightRestoreMap;
    }

    // -----------------------------------------------------------------------
    // Participant state lookup
    // -----------------------------------------------------------------------

    public ParticipantState getParticipant(UUID uuid) {
        return participants.get(uuid);
    }

    public boolean isParticipant(UUID uuid) {
        return participants.containsKey(uuid);
    }
}
