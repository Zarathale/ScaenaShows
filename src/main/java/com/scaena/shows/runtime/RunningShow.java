package com.scaena.shows.runtime;

import com.scaena.shows.model.Show;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Rotation;
import org.bukkit.scheduler.BukkitTask;

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
    // Tick counter and show state
    // -----------------------------------------------------------------------
    private long currentTick = 0;
    private boolean running  = true;

    // -----------------------------------------------------------------------
    // PLAYER_CHOICE suspension
    // -----------------------------------------------------------------------

    /** True while the show is suspended waiting for a PLAYER_CHOICE resolution. */
    private boolean suspended = false;

    /** The live ChoiceSession while suspended; null at all other times. */
    private ChoiceSession activeChoice = null;

    /**
     * Override for the show's effective duration — set when a branch cue is injected
     * so the scheduler knows when to end the show after branching.
     * -1 means use show.durationTicks as normal.
     */
    private int durationOverride = -1;

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

    // -----------------------------------------------------------------------
    // Entity-linked boss health bars  (OPS-026)
    // Keyed by entity UUID; each tracker holds the bar, max HP snapshot,
    // and optional death-line / victory-cue config.
    // -----------------------------------------------------------------------

    /**
     * Tracks one BOSS_HEALTH_BAR event: the live bar, the entity UUID it follows,
     * the max HP at registration (used to compute progress), and optional death
     * line / victory cue fields fired by EntityCombatListener on entity death.
     */
    public record BossHealthBarTracker(
        UUID   entityUuid,
        BossBar bar,
        double  maxHealth,
        String  deathLine,
        String  deathLineColor,
        int     deathLinePauseTicks,
        String  victoryCue
    ) {}

    private final Map<UUID, BossHealthBarTracker> bossHealthBars = new LinkedHashMap<>();

    /** Track scoreboard team names created by this show (for cleanup) */
    private final Set<String> ownedTeams = new HashSet<>();

    /** Track spectate prior gamemodes per player (UUID → prior GameMode) */
    private final Map<UUID, org.bukkit.GameMode> spectateRestoreMap = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Active ROTATE tasks (OPS-005)
    // Each smooth yaw-rotation BukkitTask registers here so stopShow() can
    // cancel them explicitly in addition to the show.isRunning() self-cancel.
    // -----------------------------------------------------------------------
    private final Set<BukkitTask> activeRotateTasks = new LinkedHashSet<>();

    // -----------------------------------------------------------------------
    // Item frame restore (OPS-007)
    // Snapshot taken before SET_ITEM_FRAME modifies an entity:world item frame.
    // entity:spawned frames are despawned at show end — no snapshot needed.
    // -----------------------------------------------------------------------
    public record ItemFrameSnapshot(ItemStack item, boolean visible, boolean fixed,
                                    Rotation rotation) {}
    private final Map<UUID, ItemFrameSnapshot> itemFrameRestoreMap = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Live entity group capture parameters (OPS-006)
    // When capture_mode: live, sweep params are stored here rather than a UUID
    // snapshot. resolveEntityGroup() performs a fresh getNearbyEntities() sweep
    // from the capture-time anchor on every call.
    // -----------------------------------------------------------------------
    public record LiveGroupSpec(String entityType, double radius, int maxCount,
                                Location captureAnchor) {}
    private final Map<String, LiveGroupSpec> liveEntityGroups = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Block state restore (OPS-004, OPS-008)
    // Keyed by "world:x:y:z" — original BlockData before this show touched the block.
    // putIfAbsent semantics: only the first modification is recorded so the true
    // pre-show state is preserved even if the same block is modified multiple times.
    // -----------------------------------------------------------------------
    private final Map<String, BlockData> blockStateRestoreMap = new LinkedHashMap<>();

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

    /** Directly position the tick counter — used by step mode during TechCueSession preview. */
    public void setCurrentTick(long tick) {
        this.currentTick = tick;
    }

    /** Signal that the show has ended or been stopped. */
    public void stop() {
        running = false;
    }

    // Suspension (PLAYER_CHOICE)
    public boolean isSuspended()                         { return suspended; }
    public void setSuspended(boolean s)                  { this.suspended = s; }
    public ChoiceSession getActiveChoice()               { return activeChoice; }
    public void setActiveChoice(ChoiceSession cs)        { this.activeChoice = cs; }

    // Duration override — used by branch cue injection to extend show lifetime
    public int getDurationOverride()                     { return durationOverride; }
    public void setDurationOverride(int ticks)           { this.durationOverride = ticks; }

    /**
     * Returns the effective duration for the scheduler's end-of-show check.
     * Returns durationOverride when set; falls back to show.durationTicks.
     */
    public int getEffectiveDuration() {
        return durationOverride >= 0 ? durationOverride : show.durationTicks;
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

    // releaseEntityGroup() — see accessor section below (handles both live and snapshot modes, OPS-006)

    // -----------------------------------------------------------------------
    // Bossbars
    // -----------------------------------------------------------------------

    public BossBar getShowBossBar()                  { return showBossBar; }
    public void setShowBossBar(BossBar bar)          { this.showBossBar = bar; }
    public void addActiveBossBar(BossBar bar)        { activeBossBars.add(bar); }
    public List<BossBar> getActiveBossBars()         { return activeBossBars; }

    // -----------------------------------------------------------------------
    // Entity-linked boss health bars  (OPS-026)
    // -----------------------------------------------------------------------

    public void registerBossHealthBar(UUID entityUuid, BossHealthBarTracker tracker) {
        bossHealthBars.put(entityUuid, tracker);
    }

    /** Returns the tracker for the given entity UUID, or null if not tracked. */
    public BossHealthBarTracker getBossHealthBarTracker(UUID entityUuid) {
        return bossHealthBars.get(entityUuid);
    }

    public void removeBossHealthBar(UUID entityUuid) {
        bossHealthBars.remove(entityUuid);
    }

    /** All active boss-health-bar trackers (for cleanup on show end). */
    public Map<UUID, BossHealthBarTracker> getBossHealthBars() {
        return Collections.unmodifiableMap(bossHealthBars);
    }

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
    // Block state restore (OPS-004, OPS-008)
    // -----------------------------------------------------------------------

    /**
     * Record a block's original BlockData before this show first modifies it.
     * Uses putIfAbsent — only the true pre-show state is retained even if the
     * same block is targeted more than once during the show.
     */
    public void recordBlockRestore(World world, int x, int y, int z, BlockData originalData) {
        String key = world.getName() + ":" + x + ":" + y + ":" + z;
        blockStateRestoreMap.putIfAbsent(key, originalData.clone());
    }

    /** All blocks that need to be restored on show end. Key = "world:x:y:z". */
    public Map<String, BlockData> getBlockStateRestoreMap() {
        return Collections.unmodifiableMap(blockStateRestoreMap);
    }

    // -----------------------------------------------------------------------
    // ROTATE task tracking (OPS-005)
    // -----------------------------------------------------------------------

    public void addRotateTask(BukkitTask task) { activeRotateTasks.add(task); }

    /** Cancels and clears all active ROTATE tasks. Called from ShowManager.stopShow(). */
    public void cancelRotateTasks() {
        for (BukkitTask task : activeRotateTasks) {
            try { task.cancel(); } catch (Exception ignored) {}
        }
        activeRotateTasks.clear();
    }

    // -----------------------------------------------------------------------
    // Item frame restore (OPS-007)
    // -----------------------------------------------------------------------

    /**
     * Record pre-show item frame state before this show first modifies it.
     * putIfAbsent semantics — only the true pre-show state is preserved.
     */
    public void recordItemFrameRestore(UUID frameUuid, ItemFrameSnapshot snapshot) {
        itemFrameRestoreMap.putIfAbsent(frameUuid, snapshot);
    }

    public Map<UUID, ItemFrameSnapshot> getItemFrameRestoreMap() {
        return Collections.unmodifiableMap(itemFrameRestoreMap);
    }

    // -----------------------------------------------------------------------
    // Live entity group capture (OPS-006)
    // -----------------------------------------------------------------------

    /** Register a live-mode entity group — sweep params stored instead of UUID list. */
    public void setLiveEntityGroup(String groupName, LiveGroupSpec spec) {
        liveEntityGroups.put(groupName, spec);
        // Ensure no stale snapshot entry exists for this name
        entityGroups.remove(groupName);
    }

    /**
     * Resolve an entity group by name: returns a fresh live sweep if the group
     * was captured in live mode; otherwise returns the stored UUID snapshot list.
     * Callers use this instead of getEntityGroup() for entity_group: targets.
     */
    public List<UUID> resolveEntityGroup(String groupName) {
        LiveGroupSpec spec = liveEntityGroups.get(groupName);
        if (spec != null) {
            // Live sweep: re-query at the frozen capture-time anchor
            EntityType type;
            try { type = EntityType.valueOf(spec.entityType().toUpperCase()); }
            catch (IllegalArgumentException e) { return List.of(); }

            Location anchor = spec.captureAnchor();
            if (anchor == null || anchor.getWorld() == null) return List.of();

            List<UUID> out = new ArrayList<>();
            for (Entity ent : anchor.getWorld().getNearbyEntities(
                    anchor, spec.radius(), spec.radius(), spec.radius())) {
                if (ent.getType() == type) {
                    out.add(ent.getUniqueId());
                    if (out.size() >= spec.maxCount()) break;
                }
            }
            return out;
        }
        // Snapshot mode
        return entityGroups.getOrDefault(groupName, List.of());
    }

    /** Remove a group from show tracking (covers both live and snapshot modes). */
    public void releaseEntityGroup(String groupName) {
        entityGroups.remove(groupName);
        liveEntityGroups.remove(groupName);
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
