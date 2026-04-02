---
document: Tech Rehearsal Mode — Phase 2 Spec
area: Plugin-wide
status: Design spec — v1, 2026-04-01
---

# Tech Rehearsal Mode — Phase 2: Cue-to-Cue

> This document specifies Phase 2 of Tech Rehearsal Mode: an in-world, cue-to-cue
> authoring and rehearsal surface that runs on the same execution stack as production.
>
> **Spec only. No code authorized until open questions in §8 are resolved.**
>
> Prerequisite: Phase 1 shipped (OPS-027). Phase 2 builds on top of TechSession and
> TechManager without replacing them.

---

## 1. The Problem Phase 2 Solves

Phase 1 answered: *Do we have the scene assembled correctly?* Toggle departments,
reposition marks, adjust params. The show YAML is not involved.

Phase 2 answers: *Does the show play correctly, moment to moment?* Step through
authored cues, preview individual event sequences live, edit timing and parameters
in-world, and write changes directly back to the YAML — without leaving the game
and without a separate execution path.

---

## 2. The Theatrical Model

In theatre, "cue-to-cue" is the tech rehearsal mode where the director steps through
the show cue by cue rather than running it at tempo. Between cues the stage is held.
The director can call the same cue multiple times, adjust departments, call individual
cues out of sequence, and eventually run a sequence at full tempo once satisfied.

Key properties of the theatrical model that must carry through:

**Sequences auto-follow.** A single director's call (GO) may fire a burst of dependent
cues — CAST enters, SET shifts, LIGHT snaps — all authored as one moment. Phase 2
must respect this: GO advances to the next authored pause point, which may consume
many events along the way.

**You sit in a moment.** Between GOs the stage is live and frozen. The director
walks through it, inspects it, calls individual effects, adjusts params. This is not
brief — it may be the primary working mode for the entire session.

**Preview and edit are separate states.** Editing the show YAML during live execution
would produce undefined results. Work happens in one of two modes, never both at once.

---

## 3. Two Modes

### Edit Mode

The authoring surface. The show YAML is loaded into a mutable in-memory representation
(`TechCueSession`). The player edits using the clickable chat panel and in-game tools
(scroll wheel for numeric params, anvil GUI for strings). Nothing is running. Changes
are dirty-tracked. The player saves explicitly.

Edit mode entry: automatic when Phase 2 starts, or when the player returns from
Preview mode.

### Preview Mode

Execution surface. A `RunningShow` + `ShowScheduler` pair is constructed from the
current (possibly dirty) in-memory YAML and run in **step mode**. The scheduler
dispatches events on demand rather than on a game-tick clock. The player navigates
with the hotbar items and sees results live in the world.

Preview mode entry: player presses GO from edit mode, or explicitly via the panel.

**Exit from preview:** hold the PREV item or select `[Exit Preview]` from the panel.
RunningShow stop-safety fires on exit (entity cleanup, block restore).

### The Edit → Preview → Edit Cycle

```
[Edit Mode]
    │  GO → (slot 7) or [Preview] in panel
    ▼
[Preview Mode]
    │  step through cues at will
    │  see results in world
    │  PREV (slot 5) exits preview, or [Exit Preview]
    ▼
[Edit Mode]  ← dirty flag set if in-world edits were made during preview
    │  [Save to YAML]  or  [Discard]
```

A save writes the in-memory state back to the show's YAML file on disk. No
intermediate file, no merge step — same contract as Phase 1's write-back to the
prompt-book.

---

## 4. System Architecture

### The Execution Invariant

> Phase 2 preview execution **is** the show. It uses `RunningShow`, `ShowScheduler`,
> and `ExecutorRegistry` without modification or subclassing. No adapter. No
> TechRunningShow. The same path that plays the show in production plays it in tech.

This is load-bearing. If Phase 2 grows its own execution path, bugs found in tech
may not exist in production and vice versa. The invariant prevents that.

What changes is **how the scheduler is driven**: production uses the BukkitRunnable
game-tick clock; Phase 2 uses demand-driven dispatch.

### ShowScheduler — Step Mode

Two additions to `ShowScheduler`:

```java
// Dispatch all events at the next scheduled tick, advance cursor to that tick.
// Returns the tick that was dispatched, or -1 if no more events remain.
public long dispatchNextEventTick();

// Fast-forward: dispatch all events from current tick up to (and including) targetTick.
// Does not fire the BukkitRunnable — synchronous, single-threaded.
public void dispatchEventsUpTo(long targetTick);
```

A `boolean steppingMode` flag on `ShowScheduler` controls whether the BukkitRunnable
advances the tick counter autonomously (production) or waits for `dispatchNextEventTick()`
calls (step mode).

When stepping mode is active, the BukkitRunnable loop still runs (so the task stays
alive) but its body is gated: if `steppingMode` is true, skip the normal dispatch
and tick-advance logic.

### TechCueSession

Lightweight. Holds editor state only — no execution state.

```
TechCueSession {
  show_id          String
  source_file      File               // the YAML file on disk
  raw_yaml         Map<String,Object> // mutable in-memory parse
  cursor_tick      long               // current position in the event map
  dirty            boolean            // unsaved changes exist
  preview_running  RunningShow?       // non-null while in Preview mode
  step_scheduler   ShowScheduler?     // non-null while in Preview mode
}
```

`TechCueSession` is owned by `TechManager` alongside the Phase 1 `TechSession`.
They may coexist: a Phase 1 scene can be materialized (set, lighting, marks) while
Phase 2 steps through the timeline on top of it.

### TechManager additions

`TechManager` gains:

- `startPhase2(player, showId)` — parse YAML into `TechCueSession`, enter edit mode
- `enterPreview(player)` — construct `RunningShow` from current in-memory YAML,
  build event map, start `ShowScheduler` in step mode at tick 0
- `stepForward(player)` — call `dispatchNextEventTick()`, update sidebar
- `stepBack(player)` — rewind cursor (see §5 below)
- `exitPreview(player)` — stop RunningShow (triggers stop-safety), return to edit mode
- `saveYaml(player)` — write `raw_yaml` back to `source_file`

### ShowYamlEditor

Thin helper that knows how to read and write the `raw_yaml` map for specific edits:
event timing adjustments, param value changes, event addition/removal. Provides the
"structured YAML edit" surface that `TechManager` calls when the player uses slot 9
or the clickable panel.

`ShowYamlEditor` does not execute anything. It only mutates `TechCueSession.raw_yaml`
and sets `dirty = true`.

---

## 5. Navigation Model

### Pause Points

Navigation jumps between **pause points** — moments where the director would naturally
call "hold" in a theatrical cue-to-cue. A pause point is:

1. A top-level `CUE` reference in the show's `timeline` (each authored cue is one
   navigation step).
2. A `HOLD` event within a cue (see §6 on HOLD duality).
3. The end of the show.

Individual events *within* a cue are not pause points — they execute as a burst when
the director calls GO, reproducing the auto-follow behavior of tight cue sequences in
theatre.

### GO (slot 7)

Dispatches all events up to and including the next pause point, then holds.
Implemented as `dispatchEventsUpTo(nextPausePointTick)`.

If the current position is already at a pause point, GO advances to the *following*
pause point — it does not re-fire the current moment.

### HOLD (slot 6)

Interrupts at the current tick mid-sequence if the player needs to stop between
auto-follow events. Sets `steppingMode = true` immediately, deferring any further
auto-advance.

### PREV (slot 5)

Rewind to the previous pause point. Because events may have side effects (entities
spawned, blocks changed), a true rewind requires stop-safety + re-execution from
tick 0 up to the target pause point.

**PREV implementation:** exit preview (fires full stop-safety), construct a fresh
`RunningShow`, fast-forward via `dispatchEventsUpTo(prevPausePointTick)` without
yielding to the player between ticks (invisible replay), then hold. The world will
reflect the state at that moment.

This is exact — it uses the same execution stack, so the world state is bit-for-bit
what it would be if the show had run naturally to that point.

### Jump to Cue

From the clickable panel: `[Jump to: (cue name)]` list, generated from the show's
timeline. Implemented as a PREV-style replay to the target tick.

---

## 6. The HOLD Event Duality

`HOLD` as currently implemented is an entity-freeze event in `StageEventExecutor`:
it zeroes velocity on all living entities in the show audience. It is **not** a
show-level timeline pause. This distinction matters for Phase 2.

**Decision (2026-04-01):** New `PAUSE` event type. See §8, Q1 for the full resolution.

`HOLD` retains its current meaning — entity freeze — in both production and Phase 2
preview. `PAUSE` is a new event type with a no-op executor; its only job is to mark a
navigation stop for the step scheduler. Pre-authoring `PAUSE` events in a production
YAML is safe and harmless.

```yaml
- at: 120
  type: PAUSE
  label: "After the declaration"   # optional — shown in sidebar and panel
```

---

## 7. UX Surface

### Hotbar (same slots as Phase 1, expanded semantics)

| Slot | Item | Phase 2 function |
|------|------|-----------------|
| 5 | ← Back | Previous pause point (rewind + replay) |
| 6 | Hold | Interrupt mid-sequence |
| 7 | Go → | Advance to next pause point |
| 8 | Mark Capture | Same as Phase 1 (mark repositioning, no change) |
| 9 | Parameter tool | Adjust event params (scroll or anvil GUI) |

### Clickable Chat Panel (Phase 2 additions)

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  TECH  showcase.01 · Cue-to-Cue
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Mode: [Preview ▶]  |  [Edit ✎]

  Timeline:
    C1 · intro_setup                ← current
    C2 · cast.arrival.zarathale
    C3 · lights.snap.battle_ready
    ...
  [Jump to...]

  Params: (slot 9 to edit)
    armor_tier: DIAMOND
    arrival_delay: 20t

  [Save YAML]  [Discard]  [Exit Tech]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

Cue list is auto-generated from the show timeline. Current position is highlighted.
In edit mode, params for the focused cue are editable in-panel.

### Sidebar (scoreboard) — Preview Mode

The sidebar shows the **timeline cursor**: a rolling display of the last few events
dispatched, plus what's coming next.

```
─ CUE-TO-CUE ─────────────────
  ✓ cast.arrival.zarathale   C2
  ✓ ENTITY_EQUIP × 4
  ✓ ACTION_BAR  "Warrior."
  ━ HOLDING ─────────────────
  ▷ lights.snap.battle_ready  C3
────────────────────────────────
  Events: 12   Tick: 340
```

`✓` = fired this run. `━ HOLDING` = current position. `▷` = next on GO.

### Actionbar

In preview mode: `[PREVIEW]  C2 · cast.arrival.zarathale  ·  tick 340`

In edit mode: same Phase 1 behavior (mark coordinates when capture mode active, etc.)

---

## 8. Open Design Questions

### Q1 — HOLD event duality (RESOLVED 2026-04-01)

**Decision: new `PAUSE` event type.**

`HOLD` retains its production meaning: entity freeze (zero velocity on living entities
in the show audience). No behavior change in production or in Phase 2 preview.

`PAUSE` is a new event type recognized only by the Phase 2 step scheduler as a
navigation stop. It has no execution side effect — firing it does nothing to the
world. Its only job is to mark a moment in the timeline where the director would
naturally call "hold" during a cue-to-cue.

Schema:
```yaml
- at: 120
  type: PAUSE
  label: "After the declaration"   # optional — shown in sidebar and panel
```

`EventType.java` gets `PAUSE` in the appropriate section. `EventParser` maps it to a
minimal model class (tick + optional label string). `ShowScheduler` step mode
recognizes `PAUSE` events as pause points. `ExecutorRegistry` registers `PAUSE` with
a no-op executor so it is safe to fire in production without effect.

Implementation note: the no-op executor registration means `PAUSE` events in a
production show YAML are harmless — they simply fire and do nothing. This allows
show authors to pre-author narrative pause points without any production risk.

---

### Q2 — In-game cue creation (deferred from Phase 1 — design TBD)

Alan's direction: Phase 2 should support **creating new cues** from within the tech
environment, not just editing existing ones. Design deferred to Phase 2 development.
Questions to answer before the OPS item is filed:

- What is the in-game creation flow? (Walk through department → event type →
  parameter entry? Template-based? Wizard steps?)
- How does the new cue get named and placed in the show YAML structure?
- Can the player fire the new cue immediately to preview it before committing?

These questions do not block Phase 2 step navigation or param editing. Cue creation
can be a Phase 2.1 follow-on.

---

### Q3 — PREV rewind cost (OPEN — performance question)

PREV rewinds by stopping the current RunningShow and replaying from tick 0 to the
target tick synchronously. For a short show this is cheap. For a long show with
hundreds of entity spawns, particle bursts, and block changes, the invisible replay
may take several game ticks and cause a visible stutter.

Options:
- **Accept it** — tech mode is not performance-critical; a brief stutter on PREV
  is fine
- **Snapshot model** — TechManager saves world snapshots at each pause point; PREV
  restores from the nearest snapshot rather than replaying

Snapshot model is significantly more complex. Recommend accepting the stutter until
it becomes a real problem in practice.

---

## 9. Phase 2 Deliverables Summary

When Phase 2 ships:

- `ShowScheduler`: `steppingMode` flag, `dispatchNextEventTick()`, `dispatchEventsUpTo()`
- `TechCueSession`: editor state model
- `ShowYamlEditor`: structured YAML mutation helper
- `TechManager`: Phase 2 lifecycle methods (startPhase2, enterPreview, stepForward,
  stepBack, exitPreview, saveYaml)
- `TechCuePanel`: clickable chat panel renderer for Phase 2 state
- Sidebar scoreboard: timeline cursor display in preview mode
- Resolution of Q1 (HOLD duality) before Java is filed

Phase 2 does **not** change the production execution path. All changes are additive
or confined to the TechManager / TechCueSession layer.

---

## 10. Relationship to Phase 1

Phase 1 and Phase 2 coexist under the same `/scaena tech` entry point. Phase 1
materialized the scene; Phase 2 steps through the timeline.

A full tech rehearsal session would naturally flow:
1. `/scaena tech showcase.01 site_a` — Phase 1 materializes the scene, director
   inspects marks, toggles departments, adjusts params
2. Player selects `[Cue-to-Cue]` from the panel — Phase 2 session begins, inheriting
   the current in-world state
3. Director steps through the timeline, tweaks params, saves
4. `[Exit Tech]` — full stop-safety on both Phase 1 and Phase 2 state

The Phase 1 TechSession and Phase 2 TechCueSession have independent lifecycles and
independent stop-safety contracts. TechManager coordinates both.
