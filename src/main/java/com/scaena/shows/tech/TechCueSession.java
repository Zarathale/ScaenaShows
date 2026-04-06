package com.scaena.shows.tech;

import com.scaena.shows.runtime.RunningShow;
import com.scaena.shows.runtime.ShowScheduler;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

/**
 * Stateful core of a single player's active Phase 2 (Timeline Editor) session.
 *
 * Phase 2 requires an owning Phase 1 TechSession. Created by TechManager.enterPhase2();
 * destroyed by TechManager.exitPhase2() or when the owning TechSession is dismissed.
 *
 * Two modes of operation:
 *   Edit mode    — in-memory YAML open; cue panel visible; nothing executing.
 *   Preview mode — RunningShow + ShowScheduler in step mode; player navigates
 *                  with hotbar Go/Hold/Back items.
 */
public final class TechCueSession {

    // ---- Identity ----
    private final String          showId;
    private final Player          player;
    private final PromptBook      book;   // shared reference from owning TechSession

    // ---- In-memory YAML ----
    /** Raw show YAML loaded from disk on Phase 2 entry; mutated in place by ShowYamlEditor. */
    private Map<String, Object>   rawYaml;
    /** File path for [Save] write-back. */
    private File                  sourceFile;

    // ---- Edit mode navigation ----
    private String                currentSceneId;
    /** Index of the focused cue within the current scene's CUE ref list. -1 = none focused. */
    private int                   currentCueIndex = -1;

    // ---- Preview mode ----
    private RunningShow           previewShow;       // non-null while in preview mode
    private ShowScheduler         previewScheduler;  // non-null while in preview mode
    private boolean               holdActive;

    /**
     * Stack of ticks dispatched so far in the current preview pass.
     * Supports the Back (previous cue) operation: pop the top and re-seek.
     */
    private final Deque<Long>     visitedTicks = new ArrayDeque<>();

    // ---- Department edit ----
    private DeptEditSession       activeEditSession;   // null when not editing
    private final Set<String>     dirtyCueIds = new LinkedHashSet<>();

    // ---- Session toggles ----
    /** If true, param changes immediately refire the relevant event (Sound, Effects, etc.) */
    private boolean               autoPreview  = true;
    private WorldPreviewMode      worldPreview = WorldPreviewMode.LIVE;

    // ---- Display ----
    /** Boss bar shown for the duration of any department edit session. */
    private BossBar               editBossBar;
    /** Periodic task re-sending [Save] / [Cancel] so they don't scroll off chat. */
    private BukkitTask            buttonRefreshTask;

    // ---- Dirty flag ----
    private boolean               dirty = false;   // true if rawYaml has unsaved mutations

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public TechCueSession(String showId, Player player, PromptBook book) {
        this.showId = showId;
        this.player = player;
        this.book   = book;
    }

    // -----------------------------------------------------------------------
    // WorldPreviewMode
    // -----------------------------------------------------------------------

    public enum WorldPreviewMode {
        /** Every param change immediately applies to the world. */
        LIVE,
        /** Edits update rawYaml only; use [▶ Apply to world] to spot-check. */
        VALUES_ONLY
    }

    // -----------------------------------------------------------------------
    // Identity
    // -----------------------------------------------------------------------

    public String    getShowId()     { return showId; }
    public Player    getPlayer()     { return player; }
    public PromptBook getBook()      { return book; }

    // -----------------------------------------------------------------------
    // Raw YAML
    // -----------------------------------------------------------------------

    public Map<String, Object> getRawYaml()                        { return rawYaml; }
    public void                setRawYaml(Map<String, Object> m)   { this.rawYaml = m; }
    public File                getSourceFile()                     { return sourceFile; }
    public void                setSourceFile(File f)               { this.sourceFile = f; }

    // -----------------------------------------------------------------------
    // Edit mode navigation
    // -----------------------------------------------------------------------

    public String getCurrentSceneId()                  { return currentSceneId; }
    public void   setCurrentSceneId(String id)         { this.currentSceneId = id; }

    public int  getCurrentCueIndex()                   { return currentCueIndex; }
    public void setCurrentCueIndex(int idx)            { this.currentCueIndex = idx; }

    // -----------------------------------------------------------------------
    // Preview mode
    // -----------------------------------------------------------------------

    public boolean isPreviewActive()                              { return previewShow != null; }

    public RunningShow    getPreviewShow()                        { return previewShow; }
    public void           setPreviewShow(RunningShow rs)          { this.previewShow = rs; }

    public ShowScheduler  getPreviewScheduler()                   { return previewScheduler; }
    public void           setPreviewScheduler(ShowScheduler s)    { this.previewScheduler = s; }

    public boolean isHoldActive()                                 { return holdActive; }
    public void    setHoldActive(boolean h)                       { this.holdActive = h; }

    /** Record that we just dispatched up to (or at) the given tick. */
    public void pushVisitedTick(long tick)                        { visitedTicks.push(tick); }

    /**
     * Return and remove the most recently dispatched tick, or -1 if none.
     * Used by stepBack() to find where to re-seek.
     */
    public long popVisitedTick() {
        return visitedTicks.isEmpty() ? -1L : visitedTicks.pop();
    }

    /** Peek at the previous tick without removing it (the one BEFORE the current position). */
    public long peekPreviousTick() {
        if (visitedTicks.size() < 2) return -1L;
        Iterator<Long> it = visitedTicks.iterator();
        it.next(); // current (top of deque)
        return it.next(); // previous
    }

    public void clearVisitedTicks()                               { visitedTicks.clear(); }

    // -----------------------------------------------------------------------
    // Department edit
    // -----------------------------------------------------------------------

    public DeptEditSession getActiveEditSession()                          { return activeEditSession; }
    public void            setActiveEditSession(DeptEditSession s)         { this.activeEditSession = s; }
    public boolean         isEditing()                                     { return activeEditSession != null; }

    public Set<String>     getDirtyCueIds()                                { return dirtyCueIds; }
    public void            markCueDirty(String cueId)                      { dirtyCueIds.add(cueId); }

    // -----------------------------------------------------------------------
    // Session toggles
    // -----------------------------------------------------------------------

    public boolean         isAutoPreview()                                 { return autoPreview; }
    public void            setAutoPreview(boolean v)                       { this.autoPreview = v; }

    public WorldPreviewMode getWorldPreview()                              { return worldPreview; }
    public void             setWorldPreview(WorldPreviewMode m)            { this.worldPreview = m; }

    // -----------------------------------------------------------------------
    // Display
    // -----------------------------------------------------------------------

    public BossBar    getEditBossBar()                                     { return editBossBar; }
    public void       setEditBossBar(BossBar bar)                         { this.editBossBar = bar; }

    public BukkitTask getButtonRefreshTask()                               { return buttonRefreshTask; }
    public void       setButtonRefreshTask(BukkitTask t)                  { this.buttonRefreshTask = t; }

    // -----------------------------------------------------------------------
    // Dirty flag
    // -----------------------------------------------------------------------

    public boolean isDirty()                                              { return dirty; }
    public void    markDirty()                                            { this.dirty = true; }
    public void    clearDirty()                                           { this.dirty = false; }

    // -----------------------------------------------------------------------
    // Stop-safety cleanup
    // -----------------------------------------------------------------------

    /**
     * Release all display resources associated with this session.
     * Called by TechManager.exitPhase2() before discarding this object.
     * Does NOT stop a live previewShow — caller must call exitPreview() first.
     */
    public void cleanup() {
        if (buttonRefreshTask != null && !buttonRefreshTask.isCancelled()) {
            buttonRefreshTask.cancel();
            buttonRefreshTask = null;
        }
        if (editBossBar != null) {
            player.hideBossBar(editBossBar);
            editBossBar = null;
        }
        dirtyCueIds.clear();
        visitedTicks.clear();
    }
}
