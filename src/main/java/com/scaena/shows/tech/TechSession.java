package com.scaena.shows.tech;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Stateful core of a single player's active tech rehearsal session.
 *
 * Created by TechManager.enterTech(); removed on DISMISS or disconnect.
 * All mutation goes through setters here; display tasks read fields directly.
 *
 * Phase 2 stubs (currentCueIndex, holdActive) are declared but unused.
 */
public final class TechSession {

    // ---- Identity ----
    private final String          showId;
    private final Player          player;
    private final PromptBook      book;
    private final PlayerStateSnapshot playerState;

    // ---- Scene navigation ----
    private String                currentSceneId;

    // ---- Department mask — which departments are currently active ----
    private final Set<String>     departmentMask = new LinkedHashSet<>();

    // ---- Live assets ----
    /** uuid → EntityRecord(markName, department) */
    private final Map<UUID, EntityRecord> activeEntities = new LinkedHashMap<>();
    /** original BlockData keyed by location string, for restoration on dismiss */
    private final Map<String, BlockSnapshot> blockSnapshots = new LinkedHashMap<>();

    // ---- Pending write-back ----
    /** param.name → new value (post-edit) */
    private final Map<String, Object> modifiedParams = new LinkedHashMap<>();
    /** mark name → new Location (from CAPTURE) */
    private final Map<String, Location> modifiedMarks = new LinkedHashMap<>();

    // ---- Known captured marks (loaded from scout_captures at scene load; updated on confirm) ----
    private final Set<String> capturedMarkNames = new LinkedHashSet<>();

    // ---- In-world TextDisplay markers at captured mark positions ----
    private final Map<String, TextDisplay> markMarkers = new LinkedHashMap<>();

    // ---- Build mode (SURVIVAL toggle for set-dressing work) ----
    private boolean         buildMode         = false;
    private SetBuildSession activeBuildSession = null;

    // ---- Pending bbox corners (in-memory until written to prompt-book on save) ----
    private Location pendingBboxMin = null;
    private Location pendingBboxMax = null;

    // ---- Capture mode ----
    private String  focusedMark  = null;
    private boolean captureMode  = false;

    // ---- Text param input ----
    private String  pendingTextParam = null;   // non-null when awaiting chat input

    // ---- Param scroll mode ----
    private boolean paramScrollMode = false;
    private String  focusedParam    = null;

    // ---- Display tasks ----
    private BukkitTask actionbarTask = null;
    private BukkitTask confirmTask   = null;   // clears post-action confirmation after 2s
    private String     confirmMessage = null;  // shown in actionbar instead of normal text

    // ---- Phase 2 stubs (declared, unused) ----
    @SuppressWarnings("unused") private int     currentCueIndex = 0;
    @SuppressWarnings("unused") private boolean holdActive      = false;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public TechSession(String showId, Player player, PromptBook book,
                       PlayerStateSnapshot playerState) {
        this.showId      = showId;
        this.player      = player;
        this.book        = book;
        this.playerState = playerState;
    }

    // -----------------------------------------------------------------------
    // Accessors — identity
    // -----------------------------------------------------------------------

    public String     showId()      { return showId; }
    public Player     player()      { return player; }
    public PromptBook book()        { return book; }
    public PlayerStateSnapshot playerState() { return playerState; }

    // -----------------------------------------------------------------------
    // Accessors — scene
    // -----------------------------------------------------------------------

    public String  currentSceneId()               { return currentSceneId; }
    public void    setCurrentSceneId(String id)   { this.currentSceneId = id; }

    // -----------------------------------------------------------------------
    // Accessors — department mask
    // -----------------------------------------------------------------------

    public Set<String> departmentMask()            { return departmentMask; }
    public boolean isDeptActive(String dept)       { return departmentMask.contains(dept); }

    public void activateDept(String dept)          { departmentMask.add(dept); }
    public void deactivateDept(String dept)        { departmentMask.remove(dept); }

    // -----------------------------------------------------------------------
    // Accessors — entities
    // -----------------------------------------------------------------------

    public Map<UUID, EntityRecord> activeEntities() { return activeEntities; }

    public void addEntity(UUID uuid, EntityRecord rec) { activeEntities.put(uuid, rec); }
    public void removeEntity(UUID uuid)                { activeEntities.remove(uuid); }

    /** Find the UUID of an entity spawned at a specific mark. Returns null if none. */
    public UUID findEntityAtMark(String markName) {
        return activeEntities.entrySet().stream()
            .filter(e -> markName.equals(e.getValue().markName()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    /** Find all entity UUIDs belonging to a specific department. */
    public List<UUID> entitiesForDept(String dept) {
        return activeEntities.entrySet().stream()
            .filter(e -> dept.equals(e.getValue().department()))
            .map(Map.Entry::getKey)
            .toList();
    }

    // -----------------------------------------------------------------------
    // Accessors — block snapshots
    // -----------------------------------------------------------------------

    public Map<String, BlockSnapshot> blockSnapshots() { return blockSnapshots; }

    public void addBlockSnapshot(BlockSnapshot snap) {
        blockSnapshots.put(snap.locationKey(), snap);
    }

    /** Snapshots belonging to a specific department. */
    public List<BlockSnapshot> blockSnapshotsForDept(String dept) {
        return blockSnapshots.values().stream()
            .filter(s -> dept.equals(s.department()))
            .toList();
    }

    // -----------------------------------------------------------------------
    // Accessors — pending write-back
    // -----------------------------------------------------------------------

    public Map<String, Object>   modifiedParams() { return modifiedParams; }
    public Map<String, Location> modifiedMarks()  { return modifiedMarks; }

    public boolean hasUnsavedChanges() {
        return !modifiedParams.isEmpty() || !modifiedMarks.isEmpty();
    }

    // -----------------------------------------------------------------------
    // Accessors — capture mode
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Accessors — known captured marks
    // -----------------------------------------------------------------------

    public Set<String> capturedMarkNames() { return capturedMarkNames; }

    public void setCapturedMarkNames(java.util.Collection<String> marks) {
        capturedMarkNames.clear();
        capturedMarkNames.addAll(marks);
    }

    public void addCapturedMark(String mark) { capturedMarkNames.add(mark); }

    // -----------------------------------------------------------------------
    // Accessors — mark markers
    // -----------------------------------------------------------------------

    public void addMarkMarker(String markName, TextDisplay display) {
        TextDisplay old = markMarkers.put(markName, display);
        if (old != null && old.isValid()) old.remove();
    }

    public void removeMarkMarker(String markName) {
        TextDisplay old = markMarkers.remove(markName);
        if (old != null && old.isValid()) old.remove();
    }

    public void clearMarkMarkers() {
        markMarkers.values().forEach(d -> { if (d.isValid()) d.remove(); });
        markMarkers.clear();
    }

    // -----------------------------------------------------------------------
    // Accessors — build mode
    // -----------------------------------------------------------------------

    public boolean buildMode()              { return buildMode; }
    public void    setBuildMode(boolean on) { this.buildMode = on; }

    public SetBuildSession activeBuildSession()                    { return activeBuildSession; }
    public void            setActiveBuildSession(SetBuildSession s){ this.activeBuildSession = s; }
    public void            clearBuildSession()                     { this.activeBuildSession = null; }

    public Location pendingBboxMin()               { return pendingBboxMin; }
    public Location pendingBboxMax()               { return pendingBboxMax; }
    public void     setPendingBboxMin(Location loc){ this.pendingBboxMin = loc != null ? loc.clone() : null; }
    public void     setPendingBboxMax(Location loc){ this.pendingBboxMax = loc != null ? loc.clone() : null; }

    // -----------------------------------------------------------------------
    // Accessors — capture mode
    // -----------------------------------------------------------------------

    public String  focusedMark()              { return focusedMark; }
    public boolean captureMode()              { return captureMode; }

    public void enterCaptureMode(String markName) {
        this.focusedMark = markName;
        this.captureMode = true;
    }
    public void exitCaptureMode() {
        this.focusedMark = null;
        this.captureMode = false;
    }

    // -----------------------------------------------------------------------
    // Accessors — text param input
    // -----------------------------------------------------------------------

    public String  pendingTextParam()            { return pendingTextParam; }
    public boolean awaitingTextInput()           { return pendingTextParam != null; }
    public void    setPendingTextParam(String p) { this.pendingTextParam = p; }
    public void    clearPendingTextParam()       { this.pendingTextParam = null; }

    // -----------------------------------------------------------------------
    // Accessors — param scroll
    // -----------------------------------------------------------------------

    public boolean paramScrollMode()                    { return paramScrollMode; }
    public String  focusedParam()                       { return focusedParam; }
    public void    setParamScrollMode(boolean on, String paramName) {
        this.paramScrollMode = on;
        this.focusedParam    = on ? paramName : null;
    }

    // -----------------------------------------------------------------------
    // Accessors — display tasks
    // -----------------------------------------------------------------------

    public BukkitTask actionbarTask()             { return actionbarTask; }
    public void       setActionbarTask(BukkitTask t) { this.actionbarTask = t; }

    public String  confirmMessage()               { return confirmMessage; }
    public void    setConfirmMessage(String msg)  { this.confirmMessage = msg; }
    public void    clearConfirmMessage()          { this.confirmMessage = null; }

    public BukkitTask confirmTask()               { return confirmTask; }
    public void       setConfirmTask(BukkitTask t){ this.confirmTask = t; }

    // -----------------------------------------------------------------------
    // Inner records
    // -----------------------------------------------------------------------

    /**
     * Tracks a spawned entity back to its mark and owning department.
     * Used for stop-safety despawn and TOGGLE operations.
     */
    public record EntityRecord(String markName, String department) {}

    /**
     * Snapshot of a block's original state before tech mode modified it.
     * Used for stop-safety block restoration and TOGGLE off.
     */
    public record BlockSnapshot(
        String    locationKey,  // "world:x:y:z"
        org.bukkit.World world,
        int       x,
        int       y,
        int       z,
        BlockData originalData,
        String    department
    ) {
        public static String key(org.bukkit.World world, int x, int y, int z) {
            return world.getName() + ":" + x + ":" + y + ":" + z;
        }
    }
}
