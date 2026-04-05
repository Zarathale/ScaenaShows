---
document: OPS-029 Phase 2 Design Session Notes
date: 2026-04-05
status: Working document — decisions in progress
scope: Tech Rehearsal Mode Phase 2 architecture, department edit modes, preset library
---

# OPS-029 Phase 2 Design Session — 2026-04-05

This document captures all decisions made and open items identified during the 2026-04-05
design session. It supplements the existing Phase 2 spec (`tech-rehearsal-phase2-spec.md`)
and will be incorporated into the building spec once the department walk is complete.

---

## Legend

- ✅ Locked — decision closed, build from this
- ⚑ Open — needs resolution before Java
- 📋 Deferred — acknowledged, not blocking Phase 2

---

## 1. Session Architecture

### ✅ Director Mode set aside

Director Mode (`timeline-editor-ux.md`) was evaluated and rejected for Phase 2.
The 7-slot cue page hotbar does not scale to shows of 50-100 cues. Phase 2 extends
TechSession as originally specified.

### ✅ Session model

- `TechCueSession` lives alongside `TechSession` — independent lifecycle, independent
  stop-safety contracts. `TechManager` coordinates both.
- Phase 2 requires Phase 1 active. The in-world scene context (entities, marks, block
  states from Phase 1 LOAD) is the reason to be in-game for Phase 2 work.
- Phase 2 cannot be entered standalone.

### ✅ YAML loading

- Load the **whole show YAML** into `TechCueSession.raw_yaml` on Phase 2 entry.
- **Display filtered to the current scene** — the panel shows only the current scene's
  cues, not the full show.
- Player navigates between scenes deliberately via panel action (not hotbar).

### ✅ Execution invariant

Phase 2 preview uses the real `RunningShow` + `ShowScheduler` unchanged. No separate
tech execution path. What changes is how the scheduler is driven: step mode vs.
production tick clock.

---

## 2. Hotbar

✅ Slots 5-9. Same layout as Phase 1, expanded semantics:

| Slot | Item | Function |
|------|------|----------|
| 5 | Back | Previous cue (within current scene only) |
| 6 | Hold | Interrupt mid-sequence |
| 7 | Go | Advance to next cue |
| 8 | Mark Capture | Same as Phase 1 (carried over) |
| 9 | Parameter tool | Adjust event params via scroll wheel |

### ✅ Hotbar during department edit modes

Hotbar is completely untouched during any department edit session. Player retains full
inventory and hotbar access. All Phase 2 controls are suspended while editing.

---

## 3. Navigation

### ✅ Scene-scoped cue-to-cue

Navigation is cue-by-cue within the current scene only. Not show-wide.

### ✅ End-of-scene pause point

The end of a scene is an automatic pause point. The system holds there. No typed
command or authored PAUSE event required.

### ✅ Scene transitions

Prev scene / Next scene are panel actions (clickable text in chat). Not hotbar.
Deliberate and slower than within-scene navigation — intentional.

### ✅ Same-tick cues: one step, grouped display

Multiple CUE references at the same `at` tick are treated as one navigation step —
they fire together on GO. In the panel they appear as a named group, each with its
own `[Edit ▸]` button:

```
  tick 340: warrior_enters_scene
    casting.zombie.warrior_enter         [Edit ▸]
    wardrobe.diamond.battle_ready        [Edit ▸]
```

### ✅ End-of-scene in Preview mode

When GO reaches the scene's automatic end-of-scene pause point, the system stays in
Preview mode. `RunningShow` remains live. World state is preserved. Player can observe
the result of the final cue before acting.

Panel offers: `[Exit Preview]` and `[Next Scene]`. Either triggers Phase 2 stop-safety
explicitly. Player controls when the world resets.

---

## 4. ShowYamlEditor — Two-Layer Scope

### ✅ Operates on both layers

`ShowYamlEditor` explicitly handles two distinct layers:

**Layer 1 — Show-level timeline entries:**
- Tick shift: adjust the `at` value of a CUE reference in the show's top-level timeline
- (Effectively: "fire this cue earlier or later in the show")

**Layer 2 — Event-level content within cues:**
- Param patch: change a param value on an existing event
- Tick shift within cue: move an event within its cue's own timeline
- Event insert: add a single event into an existing cue
- Event remove: delete a single event from an existing cue

These are documented as distinct operations. ShowYamlEditor knows which layer it is
operating on for every mutation.

### ⚑ Edit target clarification needed

Ambiguity from session: when a player edits a cue in Phase 2, does `TechCueSession`
hold only the show YAML, or does it also load the referenced cue file from `cues/*.yml`?

Two models:
- **Show YAML only**: edits are written to the show's own timeline entries. `[Save as Preset]`
  is the step that writes to `cues/*.yml`. Simple — one file in memory.
- **Cue file loaded**: `TechCueSession` also loads the cue's own YAML from `cues/*.yml`.
  Edits go directly to that file. Affects all shows referencing that cue.

**Must be resolved before Java begins on ShowYamlEditor.**

---

## 5. Timing UX

### ✅ Scroll wheel nudge

While focused on a cue, scroll wheel adjusts the `at` tick:
- Per notch: ±20 ticks (±1 second)
- Shift+scroll: ±100 ticks (±5 seconds)
- Actionbar shows updated tick and gap to neighbors live

### ✅ Snap to same tick

When nudging brings a cue's tick within ~10 ticks of another cue, the actionbar offers
a snap: `[snap to: warrior_enter — same tick]`. Click to lock them at the same tick.
Enables simultaneous firing without tick arithmetic.

### ✅ "After [cue]" relative reference

Panel offers `[After: (cue name) →]` for setting a tick relative to the end of another
cue. Handles "fire this 1 second after that lands."

---

## 6. Universal Slug

### ✅ Slug is required at show timeline level for all departments

Every CUE reference at the show's top-level timeline has a slug — a short, human-readable
descriptor. 2-3 words. Required for all departments (not just Set).

### ✅ Set cue stubs: slug required and enforced

Set cue stubs specifically require a slug. Phase 2 validates on entry to set edit mode:
no slug = no entry, player is prompted to add one before proceeding.

### ✅ Slug travels bidirectionally

- Library → show: when a preset is pulled from the library into a show, the slug
  comes with it.
- Show → library: when an in-game edit is pushed to the library as a preset, the
  cue's slug goes with it.

### ✅ Slug is the human interface everywhere

Wherever a human sees a cue — panel display, sidebar, boss bar, save confirmation,
run sheet, library browser — the slug is shown. Technical IDs stay in YAML.

---

## 7. Universal Preset Naming Convention

### ✅ Pattern: `[dept].[auto-components].[slugified-slug]`

- **Department prefix** always first.
- **Auto-components**: 1-2 technical descriptors inferred from cue content (dominant
  block type, instrument type, material, effect quality, etc.).
- **Slugified slug**: the cue's slug, slugified. Caps at 2-3 words.
- **Sequential suffix** (`.01`, `.02`) on conflict.

Examples:

| Department | Result |
|---|---|
| Set | `set.stone_bricks.ceiling_open` |
| Sound | `sound.horn.goat.battle_call` |
| Effects | `effects.levitate.high.warrior_rise` |
| Wardrobe | `wardrobe.diamond.battle_ready` |
| Lighting | `lighting.dawn.first_light` |
| Fireworks | `fireworks.burst.gold.victory_burst` |
| Casting | `casting.zombie.warrior_enter` |
| Voice | `voice.title.silence_coming` |

### ✅ Slug uniqueness in the library

Preset ID is the unique key. Slug is human metadata — not required to be unique.
Multiple "ceiling opens" presets with different IDs are valid. Library browser shows
all; player distinguishes by ID or creation date.

---

## 8. Universal Preset Save Model

### ✅ Three consistent buttons across all departments, all edit sessions

```
[Save]   [Save as Preset]   [Cancel]
```

- **`[Save]`**: writes changes to the cue being edited. Default action.
- **`[Save as Preset]`**: saves configuration to the department's reusable preset
  library with the auto-generated name (player can edit before confirming).
- **`[Cancel]`**: discards all edits, restores entry state.

Same labels, same order, every department, every time.

### ✅ No partial presets

Every preset is a complete configuration snapshot for its department. No partial saves.

---

## 9. Universal Department Edit Mode Shell

### ✅ Entry

`[Edit ▸]` button in the Phase 2 cue detail panel. Same label every department.

### ✅ Boss bar: persistent mode indicator

Boss bar shows the full cue name for the entire edit session:

```
✎  casting.zombie.warrior_enter
```

Full cue name fits comfortably (25-45 chars typical). No abbreviation. Player always
knows exactly what they are editing.

### ✅ No hotbar interference

Hotbar is fully available to the player during any edit session. All Phase 2 navigation
controls are suspended. The action bar is also left untouched.

### ✅ Exit trigger

Clicking `[Save]`, `[Save as Preset]`, or `[Cancel]` in chat IS the exit trigger.
There is no separate "I'm done" signal. The save/cancel decision exits the edit session.

Chat buttons are re-sent periodically so they do not scroll away during long edit
sessions.

### ✅ No em dashes

Colons used throughout, not em dashes. Example: `tick 340: warrior_enters_scene`

### ✅ Text input: text GUI, not anvil (cross-plugin preference)

Anywhere the player needs to enter a short string (sound ID, custom slug, etc.), use a
text GUI modal — not the anvil GUI. The anvil has labeling limitations that make it
unsuitable as a general text input. This is a plugin-wide UI preference established
during the Sound department walk. Applies to all departments wherever string input is
needed (sound_id, custom ID entry, etc.).

### ✅ Auto-preview mode toggle (cross-department)

A session-level toggle (`auto-preview: on/off`) persists across cues for the full Phase 2
session. Applies to any department edit mode where a param change has a perceptible
in-game result.

- **ON:** any param change immediately refires the relevant event so the designer hears
  or sees the result in real time.
- **OFF:** an explicit `[▶ Preview]` button fires the event once on demand.

Departments where this toggle applies: Sound (hear param changes), Effects (re-apply
potion effects), Fireworks (refire burst), Particle (refire burst). Camera and Wardrobe
are inherently live-swap-on-change and are unaffected by the toggle.

---

## 10. Editor Mode: Survival vs. Creative

### ✅ Configurable plugin setting

```yaml
editor_mode: SURVIVAL   # SURVIVAL | CREATIVE
```

Default: `SURVIVAL`. Not exposed as an in-game command — plugin config only.
Planned for flexibility; current production default is Survival.

---

## 11. Department Edit Modes

### ✅ Set

**Interaction model:** In-world block placement and removal. Player builds freely.

**Game mode:** Survival (follows `editor_mode` plugin setting).

**Block tracking:** Plugin tracks `BlockBreakEvent` and `BlockPlaceEvent` within the
scene area during the edit session.

**Exit sequence (triggered by Save/Save as Preset/Cancel):**
1. Plugin captures block diff from entry state
2. Converts to relative coordinates (from scene set origin mark — see §12)
3. Writes BLOCK_STATE events to the cue
4. Flash: previous cue's preset state (~1 second), then new preset state
5. World stays in new preset state
6. Confirmation: "Preset [name] saved" or "Cue saved"

**"Previous state" in flash:** the set state after the preceding set change cue in
the sequence — not just the state before this edit session. Shows the transition
between consecutive set presets. Stop-motion review.

**Stop-motion animation:** Multiple sequential set change cues with different block
configs create stop-motion. Diffs stored relative to scene baseline (Phase 1
materialized state). PREV rewind restores to baseline naturally via stop-safety + replay.

**Slug:** Required and enforced. Phase 2 blocks entry to set edit mode if no slug
is present. Player is prompted to add one before proceeding.

**Preset captures:** Full block diff as BLOCK_STATE events with relative coordinates.

---

### ✅ Casting

**Interaction model:** Panel-based param changes. No game mode switch. No world capture.

**Panel fields:**
- Entity type (selectable from panel list)
- Display name (text field)
- Mark assignment (which mark this entity stands at)

**Live swap:** On every param change, plugin immediately despawns the current entity
and respawns with the new configuration at the same mark. No before/after flash needed —
live swap is the visual feedback.

**Territory boundary:** Casting owns entity type and display name. Wardrobe owns
equipment. These are always separate edit sessions, even when both cues fire at the
same tick.

**Preset captures:** Entity type + display name only. Mark is scene-specific and is
not stored in the preset.

**Preset naming:** `casting.[entity_type].[slug]` e.g. `casting.zombie.warrior_enter`

---

### ✅ Wardrobe

**Interaction model:** Panel-based equipment slot editor. No game mode switch.

**Panel structure:** Six equipment slots displayed. Click any slot to open an item
type panel list. Selection cascade:

- Any slot selected: item type panel list
- Leather item selected: leather color sub-panel appears automatically
  (curated named colors, not raw RGB)
- Any item: enchanted toggle (on/off)
  - On = visual glow only, one fixed enchant per slot type, no gameplay mechanics
- Any armor piece: trim toggle (on/off)
  - On = two independent selectors:
    - Pattern: panel list of available trim patterns
    - Material: panel list of trim materials
  - Default: Emerald material, Flow pattern

**Live swap:** Entity re-equips immediately on every selection change.

**No partial presets:** All six slots captured in every preset. Complete loadout only.

**Preset captures:** All six slots including enchant state, leather color if applicable,
trim state (pattern + material). Complete snapshot.

**Preset naming:** `wardrobe.[primary_material].[slug]` e.g. `wardrobe.diamond.battle_ready`

---

### ✅ Sound

**Interaction model:** Panel-based param changes. No game mode switch. No world capture.
Two distinct panel views depending on event type.

**SOUND event panel fields:**
- `sound_id` — panel list organized by Sound KB curated registers (Presence, Atmosphere,
  Warmth, Tension, etc.) + `[Enter custom ID →]` opens text GUI (not anvil — see §9)
- `category` — panel selector: `ambient / hostile / music / record / weather / block /
  master / player / voice`
- `volume` — scroll wheel, 0.1 increments
- `pitch` — scroll wheel, 0.05 increments; auto-inferred pitch register label shown
  alongside the numeric value: `low / mid-low / natural / mid-high / high`
- `max_duration_ticks` — scroll wheel (optional; `[Clear]` removes the field)

**Pitch register inference ranges:**

| Label | Pitch range |
|---|---|
| `low` | 0.5–0.7 |
| `mid-low` | 0.7–0.9 |
| `natural` | 0.9–1.1 |
| `mid-high` | 1.1–1.5 |
| `high` | 1.5–2.0 |

**STOP_SOUND event panel fields:**
Single-field panel: `source` channel selector (`ambient / hostile / music / record /
weather / block / master / player / voice / all`). Single click to change. No preview
button. No preset (too trivial to warrant naming — it's one field).

**Live preview:** Governed by the session-level auto-preview toggle (§9). When ON, any
SOUND param change refires the sound event so the designer hears it immediately. When OFF,
explicit `[▶ Hear it]` button fires on demand. Applies to SOUND only — STOP_SOUND has no
meaningful preview in edit mode.

**Note on Spans:** Simulated fades (multiple descending-volume SOUND events) are Span
candidates — see §12 Span Event Architecture. Each individual SOUND event is still
editable independently, but Phase 2 offers Span group edit when the pattern is detected
as a group. Motifs and melodies (`motif.*`, `gracie.*`) are NOT Spans — they are discrete
authored note sequences and remain as named cues. No conflict.

**Preset captures (SOUND only):** `sound_id`, `category`, `volume`, `pitch`,
`max_duration_ticks` (if set). Complete snapshot. No preset for STOP_SOUND.

**Preset naming:** `sound.[category].[pitch_register].[slug]`
- Examples: `sound.hostile.low.warden_presence`, `sound.ambient.natural.cave_under`,
  `sound.master.high.arrival_bell`

### 📋 Lighting — not yet walked

### 📋 Effects — not yet walked

### 📋 Fireworks — not yet walked

### 📋 Camera — not yet walked

### 📋 Voice — not yet walked

### 📋 Choreography — not yet walked

---

## 12. Span Event Architecture

### ✅ Span is a YAML primitive

A Span is a first-class event type in the ScaenaShows YAML schema. It defines an
interpolated or pulsed sequence of events as a single authored unit. The engine expands
a Span to N individual events at show-load time (in EventParser via SpanExpander).
The human and Phase 2 editor work with the Span; the scheduler works with expanded events.

**Execution invariant preserved.** Spans are a parse-time concept only. ShowScheduler,
ExecutorRegistry, and RunningShow never see Spans — they receive only expanded individual
events. The production execution path is unchanged.

### ✅ Two behavioral modes

**Fade-type Span** (`start_value ≠ end_value`): interpolates a param from start to end
across N steps. Examples: Sound simulated fade (volume), Lighting time transition (time).

**Pulse-type Span** (`start_value == end_value`): repeats a fixed config N times at
calibrated cycle timing. Example: Effects levitation patterns (HOVER, CLIMB, RELEASE).

### ✅ Span field set

| Field | Required | Description |
|---|---|---|
| `interpolated_param` | Yes | Which param varies (e.g., `volume`, `amplifier`, `time`) |
| `start_value` | Yes | Value at step 0 |
| `end_value` | Yes | Value at final step (== start_value for pulse-type) |
| `steps` | Yes | Number of events to distribute |
| `total_duration` | Yes | Total tick length of the Span |
| `curve` | Yes | `linear` (default) \| `ease_in` \| `ease_out` |
| `step_duration` | No | For event types with internal duration (EFFECT): how long each step event lasts |
| `gap` | No | Ticks between step end and next step start (interval = step_duration + gap) |

### ✅ Phase 2 Span types

Three Span types ship in Phase 2:

| Type | Department | Interpolated param | Primary use |
|---|---|---|---|
| `SOUND_SPAN` | Sound | `volume` | Simulated fade |
| `EFFECT_SPAN` | Effects | `amplifier` | Levitation hover/climb/release cycles |
| `TIME_OF_DAY_SPAN` | Lighting | `time` | Gradual time transition |

Additional Span types (PARTICLE_SPAN, ENTITY_EFFECT_SPAN, etc.) are deferred until a
concrete show need drives them.

### ✅ Levitation calibrated patterns migrate to EFFECT_SPAN presets

The three calibrated levitation patterns move from KB documentation to named EFFECT_SPAN
presets in `effect-configs.yml`:

| Preset ID | step_duration | gap | interval | Confirmed behavior |
|---|---|---|---|---|
| `effects.levitate.hover` | 20t | 8t | 28t | "gentle bubbling" — clean altitude hold |
| `effects.levitate.climb` | 24t | 0t | 24t | "separation from earth" — gradual drift |
| `effects.levitate.release` | 20t | 24t | 44t | "blood pressure release" — slow descent |

### ✅ Span in TechCueSession (Phase 2)

`TechCueSession.raw_yaml` stores Spans in unexpanded form. Expansion happens fresh each
time `enterPreview()` is called. This is the load-bearing invariant: raw_yaml is always
what was authored; RunningShow is always what was expanded from it. If these diverge,
edits will be silently lost on the next preview cycle. TechManager must enforce this.

ShowYamlEditor gains a **5th operation:** `editSpan(spanYamlNode, SpanParams)` — mutates
Span fields in raw_yaml. Does not expand. Expansion happens at preview time.

### ✅ Span display in Phase 2 panel

Spans appear as collapsed groups in the cue detail panel:

```
  tick 360: ambient_fade                          [Span ▾]
    SOUND_SPAN  volume: 0.8 → 0.1  4 steps  40t  [Edit ▸]
```

`[Edit ▸]` on any step within a Span opens Span edit mode (not single-event edit).
Span edit mode shows step context: `Step 3 of 6 in span`. `[▶ Preview]` fires 1–2 cycles.

### ✅ Melody/motif cues are NOT Spans

Musical riffs, melodies, and Gracie gestures (`motif.*`, `gracie.*`) are discrete authored
note sequences — each pitch is a specific authored value that cannot be computed by
interpolation. They remain as named cues (sequences of SOUND events). The existing motif
cue library is complete and aligned. Span does not replace or conflict with it.

A `MELODY_SPAN` type (explicit note array primitive for authoring melodies in-game) is
acknowledged as a future aspirational item — not Phase 2 scope.

### Java layer sizing estimate

| Layer | Work | Rough size |
|---|---|---|
| Spec (spec.md Span section) | Documentation | 1 session |
| Model classes (SpanEvent + 3 subtypes) | Java | ~200 lines |
| SpanExpander (expansion math + curves) | Java | ~250 lines |
| EventParser routing | Java | ~50 lines |
| ShowYamlEditor 5th operation | Java | ~150 lines |
| TechCuePanel Span display + edit | Java/UI | ~300 lines |
| **Total** | | **~4–6 weeks** |

---

## 13. Set Coordinate System

### ✅ Scene set origin mark

Block coordinates in set presets are stored relative to a dedicated **scene set origin
mark** — a named mark established once per scene, separate from the arrival mark.

Properties:
- Does not move when the arrival mark is repositioned
- Established during initial set design; stable thereafter
- Defines `(0, 0, 0)` for all block coordinates in that scene's set presets
- Portability: any set preset can be reused at any world location by placing the
  origin mark at the desired position

---

## 14. Universal Preset Library

### ✅ Established pattern

Fireworks.yml is the existing model. Each department has its own preset file or folder.
Presets are named library assets referenced by ID from events in cues.

### ✅ Department preset files (proposed, not yet formally specified)

| Department | Preset file |
|---|---|
| Fireworks | `fireworks.yml` (exists) |
| Set | `block-configs.yml` or `sets/[id].yml` |
| Sound | `sound-configs.yml` |
| Effects | `effect-configs.yml` |
| Wardrobe | `wardrobe-configs.yml` |
| Lighting | `lighting-configs.yml` |
| Camera | `camera-configs.yml` |
| Casting | `casting-configs.yml` |
| Voice | `voice-configs.yml` |

### 📋 OPS item not yet filed

The universal preset library is bigger than Phase 2 alone. Phase 2 is the first
implementation (Set being the first department with in-game capture). A separate
OPS item should be filed once the department walk is complete and the full scope
is clear.

---

## 15. Open Items

| # | Item | Blocking? |
|---|------|-----------|
| ⚑ 1 | Edit target: show YAML only vs. also loading cues/*.yml — Span reinforces "cue file loaded" model: if a Span lives in a cue file, TechCueSession must have that file loaded to edit Span params | Yes — before ShowYamlEditor Java |
| ⚑ 2 | Q4 (partial YAML / scaffold handling): what does Phase 2 do when a CUE reference can't resolve at preview time? What is the minimum viable YAML to enter Phase 2? | Yes — before TechCueSession Java |
| ⚑ 3 | Panel design / mockup: full Phase 2 panel with all modes and states including Span display | Yes — before Java |
| ⚑ 4 | Department walk incomplete: Lighting, Effects, Fireworks, Camera, Voice, Choreography edit modes not yet defined (Sound locked 2026-04-05) | Yes — before building spec is final |
| ⚑ 5 | Preset library file structure: formal location and format for each department's preset file | Yes — before ShowYamlEditor Java |
| ⚑ 6 | Span schema spec section: field definitions, expansion rules, and validation for SOUND_SPAN, EFFECT_SPAN, TIME_OF_DAY_SPAN must be written into spec.md before any Span Java work | Yes — prerequisite for all Span Java |
| ⚑ 7 | Span type list confirmation: three Phase 2 types proposed (SOUND_SPAN, EFFECT_SPAN, TIME_OF_DAY_SPAN) — confirm before parser/expander work begins | Yes — before Span Java |
| ⚑ 8 | Cross-plugin text input UI pattern: text GUI (not anvil) established as preference during Sound walk — needs formal design decision for how text input modal works across all departments | Yes — before any department Java that requires string input |
| 📋 9 | OPS item for universal preset library: file as separate ticket once department walk complete | No |
| 📋 10 | Auto-name fallback logic: per-department inference rules when slug is absent | No — slug is required; fallback is a safety net only |
| 📋 11 | Leather color palette: define the curated named color list for Wardrobe | No — design asset, not blocking |
| 📋 12 | OPS-033 (display noise cleanup): still blocked on Phase 2 architecture decision — that decision is now made (extend TechSession). OPS-033 Part B can proceed. | No |
| 📋 13 | MELODY_SPAN type: explicit note array primitive for authoring melodies in-game — acknowledged as future aspirational item, not Phase 2 scope | No |

---

## 16. What Has Not Changed from the Existing Phase 2 Spec

These items from `tech-rehearsal-phase2-spec.md` remain as-is:

- PAUSE event type: no-op executor, safe in production YAML (§6, Q1 resolved)
- PREV rewind: accept the stutter, replay from tick 0 (Q3 resolved)
- In-game cue creation: out of scope for Phase 2 (Q2 resolved)
- ShowScheduler additions: `steppingMode`, `dispatchNextEventTick()`, `dispatchEventsUpTo()`
- TechCueSession structure (§4)
- TechManager Phase 2 lifecycle methods (§4)
- The theatrical model and two-mode design (§2, §3)
- Phase 1 + Phase 2 coexistence model (§10)

---

*Session continues — department walk in progress. Sound locked 2026-04-05. Span
architecture locked 2026-04-05. Remaining: Lighting, Effects, Fireworks, Camera,
Voice, Choreography.*
