---
document: OPS-029 Phase 2 Implementation Plan
date: 2026-04-06
status: Working plan — ready to build
scope: TechCueSession, ShowYamlEditor, and related work sequenced from ops-inbox
---

# OPS-029 Phase 2 — Implementation Plan

All architecture decisions are locked. This document is the build roadmap:
new classes, build sequence, one open design question, and OPS items sequenced
in from the inbox.

---

## Open Design Question — Scene / Timeline Mapping

**Must resolve before writing `TechCueSession` or `CuePanelBuilder`.**

The Phase 2 panel filters the show timeline to the current scene. But `PromptBook.SceneSpec`
has no tick range. The show YAML has a flat `timeline:` — no scene boundaries.

**Proposed resolution:** Add `tick_start` to `SceneSpec` in the prompt book. Each scene
declares the tick at which it begins. Phase 2 buckets timeline CUE refs between
`scene[n].tick_start` and `scene[n+1].tick_start` (last scene runs to `duration_ticks`).

Changes required:
- `PromptBook.SceneSpec` — add `int tickStart` field
- `PromptBookLoader` — read `tick_start:` from YAML
- Prompt book YAML files — add `tick_start:` to each scene entry

This is a small, isolated addition. It's the cleaner alternative to embedding scene
markers in the show YAML.

**Confirm this before Java begins.** If rejected, the alternative is CUE refs carry
their scene ID as an authoring field (slug-based, fragile) or Phase 2 doesn't filter
by scene (shows the full timeline flat). Neither alternative is preferred.

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

### Group 4 — CuePanelBuilder + command surface ✅ committed (11a3ad6, 2026-04-06) — version bump PENDING
Depends on: Group 3.
- [x] `CuePanelBuilder.java` — Edit mode panel (§4c mockup), end-of-scene panel
- [x] `CuePanelBuilder` — preview mode sidebar timeline cursor
- [x] `ShowCommand` — add `/scaena tech2` entry command (or `/scaena tech <showId> cues`)
- [x] Command subcommands: `go`, `hold`, `prev`, `scene next`, `scene prev`, `edit <cueId>`, `panel`

**Version bump: MINOR — NOT YET APPLIED. Bump build.gradle.kts 2.31.0 → 2.32.0 before next build.**

### Group 5 — Department edit sessions (iterative)
Depends on: Group 4. Implement one department at a time; each ships independently.

**Priority order (simplest → most complex):**
1. [ ] Casting — panel only; live swap on param change; no world capture
2. [ ] Wardrobe — slot selector panel; leather color sub-panel
3. [ ] Sound — param panel; auto-preview (refire SOUND event on change)
4. [ ] Voice — script line text input; BOSSBAR/ACTION_BAR/TITLE sub-panel
5. [ ] Effects — potion effect selector; auto-preview
6. [ ] Fireworks — firework builder panel; auto-preview (refire burst)
7. [ ] Lighting — TIME_OF_DAY / WEATHER; world_preview param toggle
8. [ ] Camera — ROTATE/TELEPORT/CAMERA selectors
9. [ ] Choreography — DANCE selector panel
10. [ ] Set — block diff capture (most complex; in-world; full stop-motion model)

Each department ships as its own MINOR bump.

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

Current: `2.31.0` in build.gradle.kts — **bump to 2.32.0 required** (Group 4 committed without bump)

| Group | Expected version | Actual |
|---|---|---|
| Group 1 (PAUSE) | 2.29.0 | ✅ 2.29.0 |
| Group 2 (step mode) | 2.30.0 | ✅ 2.30.0 |
| Group 3 (TechCueSession core) | 2.31.0 | ✅ 2.31.0 |
| Group 4 (panel + commands) | 2.32.0 | ⚠️ committed, bump pending |
| Group 5 — first dept (Casting) | 2.33.0 | not started |
| … each subsequent dept | +0.1.0 | |
| OPS-043 + OPS-040 | slots into any Group 1–2 window | |
