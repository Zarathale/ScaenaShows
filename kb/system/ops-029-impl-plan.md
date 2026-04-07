---
document: OPS-029 Phase 2 Implementation Plan
date: 2026-04-06
status: Active — Groups 0–4 committed, Group 5 next
scope: TechCueSession, ShowYamlEditor, and related work sequenced from ops-inbox
---

# OPS-029 Phase 2 — Implementation Plan

All architecture decisions are locked. This document is the build roadmap:
new classes, build sequence, and OPS items sequenced in from the inbox.

---

## Current State — Read This First

**Groups 0–4 + Group 5 Casting and Wardrobe are all committed on `main` at v2.35.0. Git is clean. Ready to build.**

~~Step 1 — Version bump~~ ✅ Done. `build.gradle.kts` is at `2.35.0`, build confirmed clean.
~~Step 2 — Git cleanup~~ ✅ Done. `.claude/` worktrees untracked, `.gitattributes` added.

Group 5 progress:
- ~~Casting~~ ✅ Committed. Initial at v2.33.0; revised at v2.35.0 (PR #5).
- ~~Wardrobe~~ ✅ Committed at v2.34.0 (PR #4).

### Git workflow rule for all Code sessions

**Always branch before any Java work. Never commit directly to `main`.**

```bash
git checkout -b claude/ops-029-group5-sound
```

Commit message format: `OPS-029 Group 5 [Dept]: [what] (vX.Y.Z)`
Push the branch. Stop there. Alan reviews and merges via GitHub Desktop.

### Begin Group 5 Sound

Next department is **Sound** — param panel with auto-preview (refire SOUND event on change).
Target version: **2.36.0**
See § Build Sequence → Group 5 below.

---

## Design Question — Scene / Timeline Mapping ✅ RESOLVED

`tick_start` in `PromptBook.SceneSpec` confirmed. Changes shipped in Group 0 (v2.29.0):
- `PromptBook.SceneSpec` — `tickStart` field added
- `PromptBookLoader` — reads `tick_start:` from YAML
- Prompt book YAML files — add `tick_start:` per show as scenes are ticked (hand-authored)

---

## Two Modes

Phase 2 operates in two distinct modes. Both live inside `TechCueSession`.

**Edit mode** — in-memory YAML open, panel showing the cue list. Nothing executing.
Dirty-tracked. Player clicks `[Edit ▸]` to open a department edit session. `[Save]`
writes back to disk.

**Preview mode** — `RunningShow` + `ShowScheduler` constructed from current in-memory
YAML. Scheduler in step mode. Player navigates with hotbar items (Go / Hold / Prev).
Exit triggers full stop-safety.

The panel mockup in `ops-029-design-session-2026-04-05.md §4c` is the Edit mode view.
Preview mode uses the sidebar timeline cursor (see OPS-029 ops-inbox entry).

---

## New Classes

All in `com.scaena.shows.tech` unless noted.

| Class | File | Role |
|---|---|---|
| `TechCueSession` | `tech/TechCueSession.java` | Phase 2 session state — mirrors TechSession pattern |
| `ShowYamlEditor` | `tech/ShowYamlEditor.java` | In-memory show YAML mutation (Layer 1 + Layer 2) |
| `CuePanelBuilder` | `tech/CuePanelBuilder.java` | Phase 2 chat panel renderer — mirrors TechPanelBuilder |
| `DeptEditSession` | `tech/DeptEditSession.java` | Interface for per-department edit sessions |

Plus additions to existing classes — see § Build Sequence below.

---

## TechCueSession — State Model

```java
public final class TechCueSession {

    // Identity
    private final String     showId;
    private final Player     player;
    private final PromptBook book;   // re-used from owning TechSession

    // Raw YAML — loaded from show file on Phase 2 entry, mutated in place
    private Map<String, Object> rawYaml;
    private File                sourceFile;   // path written to on [Save]

    // Navigation (edit mode)
    private String  currentSceneId;
    private int     currentCueIndex;  // within current scene's cue list

    // Preview mode
    private RunningShow  previewShow;     // non-null when in preview mode
    private boolean      holdActive;

    // Department edit
    private DeptEditSession activeEditSession;  // null when not editing
    private final Set<String> dirtyCueIds = new LinkedHashSet<>();

    // Session toggles
    private boolean          autoPreview   = true;
    private WorldPreviewMode worldPreview  = WorldPreviewMode.LIVE;

    // Display
    private BossBar    editBossBar;        // shown during dept edit sessions
    private BukkitTask buttonRefreshTask;  // periodic re-send of [Save]/[Cancel]

    public enum WorldPreviewMode { LIVE, VALUES_ONLY }
}
```

---

## ShowYamlEditor — Mutation Model

Two-layer scope per `ops-029-design-session-2026-04-05.md §4`.

```java
public final class ShowYamlEditor {

    // Layer 1 — show timeline CUE reference mutations
    void shiftCueRefTick(int timelineIndex, int deltaTicks);

    // Layer 2 — event-level mutations within a cue (written to rawYaml, not cues/*.yml)
    void patchEventParam(String cueId, int eventIndex, String paramKey, Object newValue);
    void shiftEventTick(String cueId, int eventIndex, int deltaTicks);
    void insertEvent(String cueId, int atTick, Map<String, Object> eventYaml);
    void removeEvent(String cueId, int eventIndex);

    // Persistence
    boolean saveToShowYaml();                              // rawYaml → show file on disk
    boolean saveAsPreset(String cueId, String presetId);  // cue content → cues/*.yml

    // Query helpers
    List<Map<String, Object>> getSceneCueRefs(String sceneId);  // filtered timeline
    boolean isCueStub(String cueId);   // true if cue has no events
    String  formatTick(int tick);      // "6s | 120t" (drop .0 unless fractional)
}
```

---

## CuePanelBuilder — Panel Renderer

Mirrors `TechPanelBuilder`. Pure static methods; no state.

```java
public final class CuePanelBuilder {

    // Main scene view (Edit mode) — sends the §4c mockup panel
    static void sendScenePanel(Player p, TechCueSession session, ShowYamlEditor editor);

    // End-of-scene pause point panel
    static void sendEndOfScenePanel(Player p, TechCueSession session);

    // Scene navigation (Prev Scene / Next Scene)
    static void sendSceneNav(Player p, TechCueSession session);

    // Edit session shell — boss bar on entry, periodic save/cancel button re-send
    static void showEditBossBar(Player p, String fullCueId);
    static void clearEditBossBar(Player p, TechCueSession session);
    static void sendSaveCancelButtons(Player p);

    // Preview mode sidebar — timeline cursor
    static void updatePreviewSidebar(Player p, TechCueSession session);
}
```

---

## DeptEditSession — Interface

Thin interface. Each department implements its own edit session. Start with one
(Casting is simplest — panel only, no world capture), stub the rest.

```java
public interface DeptEditSession {
    String cueId();
    String department();
    void onSave();
    void onSaveAsPreset();
    void onCancel();
}
```

---

## Build Sequence

Dependencies flow top to bottom. Each group can ship as a discrete commit.

### Group 0 — Design question (prerequisite) ✅ 2.29.0
- [x] Resolve scene/tick mapping — `tick_start` in prompt book confirmed
- [x] Add `tick_start` to `PromptBook.SceneSpec` + `PromptBookLoader`
- [ ] Add `tick_start:` to prompt book YAML files (hand-authored per show as scenes are ticked)

### Group 1 — New event type: PAUSE ✅ 2.29.0
- [x] `EventType` — add `PAUSE` (§6.12 Tech Rehearsal)
- [x] `UtilityEvents.PauseEvent` — data class (`at`, optional `label`)
- [x] `EventParser` — `case PAUSE -> new UtilityEvents.PauseEvent(m)`
- [x] `UtilityEventExecutor` — `case PAUSE -> {}` no-op
- [x] `ExecutorRegistry` — `PAUSE` registered to `utility`

### Group 2 — ShowScheduler step mode ✅ 2.30.0
Depends on: nothing new (adds to existing class).
- [x] `ShowScheduler` — add `steppingMode` flag, `dispatchNextEventTick()`, `dispatchEventsUpTo(long)`
- [x] Step mode gates the BukkitRunnable tick loop; demand-driven dispatch when `steppingMode = true`
- [x] `RunningShow` — add `setCurrentTick(long)` to support direct tick positioning

**Version bump: MINOR**

### Group 3 — TechCueSession + ShowYamlEditor ✅ 2.31.0
Depends on: Group 0 (scene/tick mapping resolved), Group 1 (PAUSE), Group 2 (step mode).
- [x] `TechCueSession.java` — session state model (edit + preview mode, step history, dept edit)
- [x] `DeptEditSession.java` — interface (save/cancel contract for Group 5 dept sessions)
- [x] `ShowYamlEditor.java` — mutation model (Layer 1 tick shift; Layer 2 inline events; save/preset)
- [x] `TechManager` — `enterPhase2()`, `exitPhase2()`, `getTechCueSession()`, `startPreview()`, `exitPreview()`
- [x] `TechManager` — `stepForward()`, `holdPreview()`, `stepBack()` — hotbar routing in preview mode
- [x] `TechManager` — `saveYaml()`, `saveAsPreset()` — persistence operations
- [x] `TechManager` — Phase 2 cleanup wired into `forceDismiss()` and `onPlayerQuit()`
- [x] `TechManager` — `setCueRegistry()`, `setExecutorRegistry()` setters for preview construction
- [x] `ShowScheduler` — `show()` accessor for Phase 2 step navigation
- [x] `TechHotbarListener` — route slots 5–7 to Phase 2 handlers when `TechCueSession` is active
- [x] `ScaenaShowsPlugin` — wire `cueRegistry` + `executors` into `TechManager`

**Version bump: MINOR**

### Group 4 — CuePanelBuilder + command surface ✅ 2.32.0
Depends on: Group 3.
- [x] `CuePanelBuilder.java` — Edit mode panel (§4c mockup), end-of-scene panel
- [x] `CuePanelBuilder` — preview mode sidebar timeline cursor
- [x] `ShowCommand` — add `/scaena tech2` entry command (or `/scaena tech <showId> cues`)
- [x] Command subcommands: `go`, `hold`, `prev`, `scene next`, `scene prev`, `edit <cueId>`, `panel`

**Version bump: MINOR ✅**

### Group 5 — Department edit sessions (iterative)
Depends on: Groups 1–4 (all committed). Implement one department at a time; each ships independently.

Each department implements `DeptEditSession` and ships as its own MINOR bump on a feature branch.
Branch naming: `claude/ops-029-group5-[dept]`

**Priority order (simplest → most complex):**

1. [x] **Casting** — panel only; live param swap on param change; no world capture ✅ v2.33.0 / v2.35.0
   - `CastingEditSession.java`, `CastingPanelBuilder.java` committed on `main`

2. [x] **Wardrobe** — slot selector panel; leather color sub-panel ✅ v2.34.0
   - `WardrobeEditSession.java`, `WardrobePanelBuilder.java` committed on `main`

3. [ ] **Sound** — param panel; auto-preview (refire SOUND event on change)
   - Read `kb/system/phase2-department-panels.md §Sound`
   - New class: `SoundEditSession implements DeptEditSession`
   - Version: 2.35.0

4. [ ] **Voice** — script line text input; BOSSBAR/ACTION_BAR/TITLE sub-panel
   - Read `kb/system/phase2-department-panels.md §Voice`
   - New class: `VoiceEditSession implements DeptEditSession`
   - Version: 2.36.0

5. [ ] **Effects** — potion effect selector; auto-preview
   - Read `kb/system/phase2-department-panels.md §Effects`
   - New class: `EffectsEditSession implements DeptEditSession`
   - Version: 2.37.0

6. [ ] **Fireworks** — firework builder panel; auto-preview (refire burst)
   - Read `kb/system/phase2-department-panels.md §Fireworks`
   - New class: `FireworksEditSession implements DeptEditSession`
   - Version: 2.38.0

7. [ ] **Lighting** — TIME_OF_DAY / WEATHER; world_preview param toggle
   - Read `kb/system/phase2-department-panels.md §Lighting`
   - New class: `LightingEditSession implements DeptEditSession`
   - Version: 2.39.0

8. [ ] **Camera** — ROTATE/TELEPORT/CAMERA selectors
   - Read `kb/system/phase2-department-panels.md §Camera`
   - New class: `CameraEditSession implements DeptEditSession`
   - Version: 2.40.0

9. [ ] **Choreography** — DANCE selector panel
   - Read `kb/system/phase2-department-panels.md §Choreography`
   - New class: `ChoreographyEditSession implements DeptEditSession`
   - Version: 2.41.0

10. [ ] **Set** — Phase 2 panel wrapper only; block diff capture is already done
    - Read `kb/system/phase2-department-panels.md §Set`
    - `SetBuildSession.java`, `BlockBuildListener.java`, `SetBuildWriter.java` are **fully
      implemented and live in Phase 1** (`TechSession`/`TechManager`). The in-world block
      tracking, bounding box logic, and YAML write are all working. Do not rewrite them.
    - New class: `SetEditSession implements DeptEditSession` — wires the existing Phase 1
      capture into the Phase 2 panel interface. The hard work is already done.
    - Version: 2.42.0

---

## OPS Items Sequenced In

Items from `ops-inbox.md` that slot naturally into the Phase 2 build window.

### Ship alongside Group 1–2 (small, independent)

**OPS-043 — BOSSBAR `start_progress` / `end_progress` / static mode**
Priority: Medium. Targeted change to `BossbarEvent` + `handleBossbar()`. No new event
type. ~80 lines. Can be picked up in parallel with Group 1.

**OPS-040 — ROTATE `delta_pitch` / `pitch` fields**
Priority: Medium. Targeted change to `RotateEvent` + `handleRotate()`. ~60 lines.
No dependencies. Natural pair with OPS-043 in a "executor polish" commit.

### Ship after Group 5, Set (depends on scene bounding box)

**OPS-033 Part B — Phase 1 display redesign**
Priority: Medium. Unblocked once Phase 2 architecture is in. Suppress or redesign
`TechSidebarDisplay` and `TechActionbarTask` mode 4 (normal mode) to fit the new
Phase 2 display model. Part A already shipped in 2.28.0.

### Low priority — queue for after Phase 2 stable

**OPS-030 — Scaffold generator** (`/scaena scaffold <showId>`)
Claude handles this in the interim. Implement once Phase 2 is stable.

**OPS-034 — LIGHTNING player anchor**
Targeted executor change. Not blocking Phase 2.

**OPS-035 — FIREWORK_RANDOM `y_variation`**
~20 lines. Queue for a fireworks polish pass.

**OPS-036 — FIREWORK_RANDOM preset pool**
~30 lines. Pair with OPS-035.

**OPS-044 — MUSIC cue migration** (`motif.*` → `music.*`)
Prerequisite: MUSIC event type in schema. Cleanup only; no playback impact.

**OPS-045 — HARP_SWEEP event type**
Prerequisite: MUSIC event type shipped. ~780 lines of new code. Separate planning
session before Java begins.

### Needs research before any Java

**OPS-039 — Snow blindness / frozen-feet screen effect**
Research first. Determine if Paper API exposes this programmatically before any code.

### Future / not sequenced

**OPS-037 — Land ownership plugin integration** — design questions unresolved
**OPS-038 — Scene safety / mob exclusion** — design questions unresolved
**OPS-041 — DARKEN_SKY** — design questions unresolved (BossBar API only?)
**OPS-042 — CREATE_FOG** — design questions unresolved (BossBar API only?)

---

## Rough Size Estimate

| Group | New code | Complexity |
|---|---|---|
| 0 — Scene/tick mapping | ~30 lines | Low |
| 1 — PAUSE event type | ~40 lines | Low |
| 2 — ShowScheduler step mode | ~80 lines | Medium |
| 3 — TechCueSession + ShowYamlEditor + TechManager Phase 2 | ~400 lines | High |
| 4 — CuePanelBuilder + command surface | ~300 lines | Medium |
| 5 — Dept edit sessions (all 10) | ~1,500 lines est. | High (iterative) |
| OPS-043 + OPS-040 | ~140 lines | Low |

Total to fully ship Phase 2: ~2,500 lines across ~12 commits.

---

## Version Progression (rough)

Current: `2.35.0` ✅

| Group | Version | Status |
|---|---|---|
| Group 1 (PAUSE) | 2.29.0 | ✅ |
| Group 2 (step mode) | 2.30.0 | ✅ |
| Group 3 (TechCueSession core) | 2.31.0 | ✅ |
| Group 4 (panel + commands) | 2.32.0 | ✅ |
| Group 5 — Casting | 2.33.0 / 2.35.0 | ✅ |
| Group 5 — Wardrobe | 2.34.0 | ✅ |
| Group 5 — Sound | 2.36.0 | next |
| Group 5 — Voice | 2.37.0 | |
| Group 5 — Effects | 2.38.0 | |
| Group 5 — Fireworks | 2.39.0 | |
| Group 5 — Lighting | 2.40.0 | |
| Group 5 — Camera | 2.41.0 | |
| Group 5 — Choreography | 2.42.0 | |
| Group 5 — Set | 2.43.0 | |
| OPS-043 + OPS-040 | slots into any Group 5 window | |
