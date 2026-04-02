---
document: Tech Rehearsal Mode — Architecture
area: Plugin-wide
status: Design draft — v3, updated 2026-04-01
---

# Tech Rehearsal Mode — Architecture

> This document defines the design for Tech Rehearsal Mode in ScaenaShows: an
> interactive, stateful, department-aware in-world rehearsal surface.
>
> **Design document only. No code changes authorized until open questions in §8
> are resolved.**

---

## 1. The Problem

Standing at a position in-world and typing commands is high-friction. You're navigating
with WASD, reading the scene, making spatial judgments — the chat bar is a context
switch every time. For scouting, where the workflow is linear and the codes are short,
it's manageable. For tech rehearsal — toggling departments, adjusting param values,
repositioning marks, stepping cue to cue — it doesn't work.

The goal of this system is to bring the full show-params and show YAML surfaces into
the live world, interactively, with as close to zero typing as possible.

---

## 2. The Metaphor

A technical rehearsal is where all departments work together on stage with the ability
to stop time. The director calls "hold," the action freezes. They call a department,
it fires in isolation. The stage manager calls "go," and the scene resumes.

The player is the director. Standing in the scene is the point.

| Theatre role | ScaenaShows equivalent |
|---|---|
| Director | The player — watches, calls holds, gives notes |
| Stage manager | The TechSession — tracks state, coordinates cleanup |
| Department heads | Per-department asset toggles in the Tech Panel |

---

## 3. Core UX Model

### The constraint
During gameplay the keyboard is occupied by movement. Any workflow that depends on
frequent typing is high-friction. Common actions should require zero typing.

### Two input surfaces

**Surface 1 — Clickable chat panel (primary interactive menu)**
The plugin maintains a persistent menu in chat. Each action is a clickable text
component that runs a command server-side. The player sees labeled brackets and clicks
them — `[CAST ✓]`, `[GO ▶]`, `[Focus Mark...]` — no typing. The panel refreshes
automatically when state changes.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  TECH  showcase.01 · Site A — Active
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Departments:
  [CAST ✓]  [SET ✓]  [WARDROBE ✓]  [LIGHTS ✓]
  [SOUND ✓]  [CAMERA ✓]  [EFFECTS ✓]  [FX ✓]

  Marks:
  [Focus Mark...]  [Save]  [Discard]

  [Exit Tech]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

The panel handles department toggles and param/mark actions. Navigation lives in the
hotbar.

**Surface 2 — Hotbar slots 5–9 (navigation, capture, and parameter adjustment)**
Slots 1–4 remain free for in-world block work during tech sessions.

All five slots are active from Phase 1:

| Slot | Item | Function |
|------|------|----------|
| 5 | ← Prev | Previous scene (Phase 1) / previous cue (Phase 2) |
| 6 | Hold | Freeze current state — hold before advancing |
| 7 | Go → | Next scene (Phase 1) / fire next cue (Phase 2) |
| 8 | Mark Capture | Right-click to capture current position as the focused mark |
| 9 | Parameter tool | Adjust non-positional param values (see §3 below) |

**Supporting displays**
- **Actionbar** (always-on): current scene label; focused mark name and live coordinates
  when capture mode is active; confirmation messages on save
- **Sidebar (scoreboard)**: department status, active entity count, modified params count

### Mark repositioning
Positional marks are captured by standing somewhere and right-clicking the Mark Capture
item. The focused mark is selected from the `[Focus Mark...]` panel. Actionbar shows
live coordinates as you walk. The spawned entity (if present) teleports immediately
to the new position as you capture it.

### Non-positional parameter adjustment
Two mechanisms, depending on value type:

- **Numeric values** (multipliers, tick counts, durations): holding the Parameter tool
  (slot 9) and selecting a param from the clickable panel enters scroll-wheel mode —
  scroll up/down to increment/decrement; the entity or effect updates live in-world.
  Click to confirm.

- **Text strings** (`death_line: "WORTHY."`, `choice_prompt: "What now?"`, etc.):
  selecting a text param opens an anvil GUI. The player renames the item to the new
  string and confirms. Clean, in-game, no chat prompt.

---

## 4. System Architecture

### TechSession

`TechSession` is the stateful core of the system.

```
TechSession {
  show_id              String
  current_scene        SceneRef                  // "site_a", "site_b", etc.
  department_mask      Set<Department>           // which departments are active
  active_entities      Map<UUID, EntityRecord>   // spawned entities + owning dept
  block_snapshots      Map<Location, BlockData>  // original block state → restore on dismiss
  modified_params      Map<ParamName, Value>     // param values adjusted this session
  modified_marks       Map<MarkName, Position>   // marks repositioned this session
  player_state         PlayerStateSnapshot       // gamemode, flight → restore on exit
  focused_mark         String?                   // currently selected for capture
  capture_mode         Boolean
  // Phase 2 additions:
  current_cue_index    int
  hold_active          Boolean
}
```

One TechSession per player at a time. Loading a new scene while one is active
auto-dismisses the current session (with full cleanup) first.

### The four core operations

**LOAD(scene, departments)**
Materialize a scene. For each active department, fire its setup assets immediately
(no tick delay). Set block states, spawn entities at their marks, set time of day.
Record all entity UUIDs and original block states in the session.

**DISMISS()**
Apply full stop-safety. Despawn all session entities, restore all block states, restore
player state. If modified_params or modified_marks is non-empty, trigger the save prompt
before completing cleanup.

**TOGGLE(department)**
- Off → despawn/restore that department's assets immediately. Remove from mask.
- On → re-fire that department's setup events against the live world. Add to mask.

**CAPTURE(mark_name, position)**
Update modified_marks with new position. If an entity is spawned at this mark,
teleport it immediately. Does not write to disk until SAVE is called.

### Write-back
Changes accumulate in `TechSession.modified_params` and `modified_marks` during the
session. Nothing writes to disk until the player explicitly saves.

The save prompt appears on DISMISS (if changes exist) and is also available any time
via `[Save]` in the clickable panel:

```
  Tech session complete.
  3 params updated  |  1 mark repositioned
  [Save to show-params.md]  [Discard changes]
```

**Phase 1** writes directly to show-params.md. No intermediate file, no merge step.

**Phase 2** writes directly to show YAML. Same save-on-dismiss contract.

---

## 5. Phase Descriptions

### Phase 1 — Show-params as the live editing surface

Phase 1 brings show-params into the world. You enter tech mode, the scene materializes
from the latest show-params data, and you interact with it directly — toggle departments,
reposition marks, adjust param values — then write those changes back to show-params.md
without leaving the game.

Data source for Phase 1: show-params.md + scout_captures YAML. No show YAML required.

**Phase 1 delivers:**
- `/scaena tech [show_id] [scene?]` — entry command
- TechSession lifecycle: init, track, dismiss, stop-safety
- Scene-level navigation (slots 5 and 7)
- Hold between scenes (slot 6)
- Clickable chat panel: department toggles, mark focus, save/discard
- Actionbar + sidebar status display
- Mark Capture (slot 8): right-click to capture position for focused mark
- Parameter tool (slot 9): scroll-wheel for numeric params, anvil GUI for text strings
- Direct write-back to show-params.md on save
- Full stop-safety on dismiss

When Phase 1 ships, `/scaena scout` is retired. The mark-capture workflow in tech mode
replaces it entirely. The scouting field guide is updated to use `/scaena tech` as the
in-world scouting entry point.

### Phase 2 — Show YAML as the live editing surface

Phase 2 shifts the data source from show-params to the show YAML. The workflow becomes
a live YAML authoring surface: step through authored cues, read and adjust parameters,
fire individual cues, and create new cues from within the environment.

Navigation shifts from scene-level to cue-level. Hold (slot 6) now freezes between
cues rather than between scenes. The five-slot layout carries forward without remapping.

**See `kb/system/tech-rehearsal-phase2-spec.md` for the full Phase 2 architecture.**

Key architectural invariant: Phase 2 preview execution uses the same `RunningShow` +
`ShowScheduler` + `ExecutorRegistry` stack as production — no adapter, no separate
path. `ShowScheduler` gains a step mode with demand-driven dispatch.

**Phase 2 adds:**
- YAML as the data source for scene loading
- Cue-to-cue navigation: Back (rewind+replay) / Hold (interrupt) / Go (next pause point)
- `TechCueSession` — lightweight editor state (mutable raw YAML, cursor, dirty flag)
- `ShowYamlEditor` — structured YAML mutation helper
- Sidebar timeline cursor: which event just fired, what's coming next
- Cue parameter adjustment via slot 9 and clickable panel
- New cue creation from within the tech environment (see §8, Q2; deferred to Phase 2.1)
- Direct write-back to show YAML on save

### Phase 3 — Variant management

Phase 3 introduces named variants: alternative values for show-params entries and
alternative versions of cues. The purpose is side-by-side calibration — test variant A
against variant B in-world and commit the winner.

Variants are embedded in their respective files (show-params.md and show YAML) rather
than in separate files. Library-scale alternatives (department KB patterns, archetype
options) stay in department KBs and are not variant-tracked at the show level.

The data model and in-game variant management approach will be designed during Phase 2
development.

---

## 6. Department Toggle Behavior

| Department | Assets owned |
|---|---|
| Casting | Entity spawns (SPAWN_ENTITY) |
| Wardrobe | Equipment on entities (ENTITY_EQUIP) — requires Casting active |
| Set | Block state changes (BLOCK_STATE); mark indicators |
| Lighting | TIME_OF_DAY; light-source block states |
| Camera | Drone entity at drone start mark |
| Sound | Ambient sound events — one-shot on load |
| Effects | Particle events — one-shot on load |
| Fireworks | One-shot test burst at computed position |

Wardrobe depends on Casting. If Casting is toggled off, entities despawn; when Casting
comes back on, entities respawn and Wardrobe re-applies automatically.

Isolating a single department (all others off) is the equivalent of calling one
department for a spot check while the stage is otherwise dark.

---

## 7. Entry Point Summary

```
/scaena tech [show_id]            — enter tech mode, load first scene
/scaena tech [show_id] [scene]    — enter tech mode, load specific scene
```

Everything after entry is hotbar items and clickable chat. Typing ends at the entry
command.

---

## 8. Open Design Questions

### Q1 — Hotbar slot assignments (OPEN — needs confirmation)

The five-slot layout is defined in §3. Confirming assignments before Phase 1 is filed:

| Slot | Phase 1 | Phase 2 |
|------|---------|---------|
| 5 | ← Prev Scene | ← Back (prev cue / prev scene) |
| 6 | Hold | Hold |
| 7 | Go → (next scene) | Go → (next cue) |
| 8 | Mark Capture | Mark Capture |
| 9 | Parameter tool | Parameter tool |

The layout is the same across both phases — navigation expands from scene-level to
cue-level without remapping slots. Confirm or adjust before the Phase 1 OPS item
is filed.

---

### Q2 — In-game cue creation (Phase 2 design — TBD)

Alan's direction for Phase 2: not just adjusting existing cues, but **creating new
cues** from within the tech environment. The design for this is deferred to Phase 2
development, but needs answers before Phase 2 is filed:

- What does the in-game cue creation flow look like? (Walk through department
  selection → event type → parameter entry? Template-based? Something else?)
- How does the new cue get named and placed in the show YAML structure?
- Can the player fire the new cue immediately to preview it before committing?

These questions do not block Phase 1.
