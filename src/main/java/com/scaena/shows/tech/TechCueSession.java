package com.scaena.shows.tech;

import com.scaena.shows.runtime.RunningShow;
import com.scaena.shows.runtime.ShowScheduler;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Stateful core of a Phase 2 Tech Cue Session.
 *
 * Created by TechManager.enterPhase2(); removed on exitPhase2() or Phase 1 dismiss.
 * Phase 2 requires an active TechSession (Phase 1) — the two live side-by-side.
 *
 * Two modes:
 *   Edit mode    — rawYaml open in ShowYamlEditor; cue panel displayed; nothing executing.
 *   Preview mode — RunningShow + ShowScheduler in step mode; player navigates with Go/Hold/Prev.
 */
public final class TechCueSession {

    // ---- Identity ----
    private final Player          player;
    private final PromptBook      book;       // re-used from owning TechSession
    private final ShowYamlEditor  editor;

    // ---- Navigation (edit mode) ----
    private String  currentSceneId;
    private int     currentCueIndex  = 0;    // index within current scene's cue ref list

    // ---- Preview mode ----
    private RunningShow   previewShow      = null;   // non-null while in preview mode
    private ShowScheduler previewScheduler = null;   // paired with previewShow
    private boolean       holdActive       = false;

    /**
     * Ordered list of ticks dispatched in the current preview session.
     * Used by stepBack to navigate to the previous cue without re-dispatching.
     */
    private final List<Long> stepHistory = new ArrayList<>();

    // ---- Department edit ----
    private DeptEditSession   activeEditSession = null;
    private final Set<String> dirtyCueIds       = new LinkedHashSet<>();

    // ---- Session toggles ----
    private boolean          autoPreview  = true;
    private WorldPreviewMode worldPreview = WorldPreviewMode.LIVE;

    // ---- Display ----
    private BossBar    editBossBar       = null;  // shown during dept edit sessions
    private BukkitTask buttonRefreshTask = null;  // periodic re-send of [Save]/[Cancel]

    /** Controls how instruments with server-wide effects behave during edit. */
    public enum WorldPreviewMode { LIVE, VALUES_ONLY }

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public TechCueSession(Player player, PromptBook book, ShowYamlEditor editor,
                          String initialSceneId) {
        this.player         = player;
        this.book           = book;
        this.editor         = editor;
        this.currentSceneId = initialSceneId;
    }

    // -----------------------------------------------------------------------
    // Accessors — identity
    // -----------------------------------------------------------------------

    public String          showId()  { return book.showId(); }
    public Player          player()  { return player; }
    public PromptBook      book()    { return book; }
    public ShowYamlEditor  editor()  { return editor; }

    // -----------------------------------------------------------------------
    // Accessors — navigation
    // -----------------------------------------------------------------------

    public String currentSceneId()              { return currentSceneId; }
    public int    currentCueIndex()             { return currentCueIndex; }

    /** Change scene and reset cue index to 0. */
    public void setCurrentSceneId(String id) {
        this.currentSceneId = id;
        this.currentCueIndex = 0;
    }

    public void setCurrentCueIndex(int idx)     { this.currentCueIndex = idx; }

    // -----------------------------------------------------------------------
    // Accessors — preview mode
    // -----------------------------------------------------------------------

    public boolean        isInPreview()         { return previewShow != null; }
    public RunningShow    previewShow()          { return previewShow; }
    public ShowScheduler  previewScheduler()     { return previewScheduler; }
    public boolean        holdActive()           { return holdActive; }
    public void           setHoldActive(boolean h) { this.holdActive = h; }

    /** Register the RunningShow and ShowScheduler for preview mode. Clears step history. */
    public void setPreview(RunningShow show, ShowScheduler scheduler) {
        this.previewShow      = show;
        this.previewScheduler = scheduler;
        this.stepHistory.clear();
        this.holdActive = false;
    }

    /** Clear preview state (call after stopping RunningShow + cancelling scheduler). */
    public void clearPreview() {
        this.previewShow      = null;
        this.previewScheduler = null;
        this.holdActive       = false;
        this.stepHistory.clear();
    }

    // ---- Step history (for Prev navigation) ----

    /** Record a tick that was dispatched in the current preview. */
    public void recordStep(long tick) { stepHistory.add(tick); }

    /** The tick of the last dispatched step, or -1L if nothing has been dispatched. */
    public long lastStepTick() {
        return stepHistory.isEmpty() ? -1L : stepHistory.get(stepHistory.size() - 1);
    }

    /** Remove the last recorded step (called on stepBack). */
    public void popLastStep() {
        if (!stepHistory.isEmpty()) stepHistory.remove(stepHistory.size() - 1);
    }

    /** True if at least one step has been dispatched in the current preview. */
    public boolean hasSteps() { return !stepHistory.isEmpty(); }

    // -----------------------------------------------------------------------
    // Accessors — department edit
    // -----------------------------------------------------------------------

    public DeptEditSession activeEditSession()           { return activeEditSession; }
    public void  setActiveEditSession(DeptEditSession s) { this.activeEditSession = s; }
    public void  clearActiveEditSession()                { this.activeEditSession = null; }
    public boolean isEditing()                           { return activeEditSession != null; }

    public Set<String> dirtyCueIds()                     { return dirtyCueIds; }
    public void        markCueDirty(String cueId)        { dirtyCueIds.add(cueId); }

    // -----------------------------------------------------------------------
    // Accessors — toggles
    // -----------------------------------------------------------------------

    public boolean          autoPreview()                     { return autoPreview; }
    public void             setAutoPreview(boolean on)        { this.autoPreview = on; }
    public WorldPreviewMode worldPreview()                    { return worldPreview; }
    public void             setWorldPreview(WorldPreviewMode m){ this.worldPreview = m; }

    // -----------------------------------------------------------------------
    // Accessors — display
    // -----------------------------------------------------------------------

    public BossBar    editBossBar()                         { return editBossBar; }
    public void       setEditBossBar(BossBar bar)           { this.editBossBar = bar; }
    public BukkitTask buttonRefreshTask()                   { return buttonRefreshTask; }
    public void       setButtonRefreshTask(BukkitTask t)    { this.buttonRefreshTask = t; }
}
