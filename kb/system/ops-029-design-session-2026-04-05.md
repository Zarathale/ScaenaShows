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

## Where We Left Off (pickup point for next session)

**Last action:** §12 Span architecture updated — multi-param interpolations, `equal_temperament`
curve, glissando. §12a PHRASE primitive added. MELODY_SPAN superseded. Then: naming discussion
— SPAN → PATTERN rename proposed and almost rejected (fireworks conflict), but Alan's reframe
clarified the concept. See ⚑16 below — this is the active open decision.

**Active naming decision (⚑16) — do not build until resolved:**

The current doc uses "SPAN" throughout. Alan has proposed renaming SPAN → PATTERN based on this
reframe: the existing fireworks spatial patterns (FIREWORK_CIRCLE, FIREWORK_LINE, etc.) are
already generative/computed primitives — you define rules/endpoints and the engine generates
individual events. A musical "pattern" (pitch sweep from start to end) is the same concept over
a different axis (time/pitch instead of space). PATTERN unifies these as one architectural idea.

Two-primitive model under the proposed rename:

| Primitive | What you define | Engine generates | Examples |
|---|---|---|---|
| **PATTERN** (was: SPAN) | Rules / endpoints (start, end, curve, shape) | Individual events | FIREWORK_CIRCLE → burst positions; SOUND_PATTERN → note pitches in a sweep |
| **PHRASE** | Every step explicitly | Nothing — you wrote it | Authored melody; motif cue; firework salvo with explicit volleys |

This naming is consistent with (not in conflict with) the existing fireworks terminology —
spatial patterns and temporal/pitch patterns are both instances of the generative primitive.

**To resume next session:** Confirm PATTERN rename (or keep SPAN), then do a find/replace
through the session doc, then continue the department walk at Effects.

**Department walk status:**

| Department | Status |
|---|---|
| Set | ✅ Locked |
| Casting | ✅ Locked |
| Wardrobe | ✅ Locked |
| Sound | ✅ Locked (2026-04-05) |
| Lighting | ✅ Locked (2026-04-05) |
| Effects | 📋 Not yet walked |
| Fireworks | 📋 Not yet walked |
| Camera | 📋 Not yet walked |
| Voice | 📋 Not yet walked |
| Choreography | 📋 Not yet walked |

**Next department to walk:** Effects (Mira) — when ready.

**Key architectural decisions locked this session:**
- Span is a YAML primitive (§12) — SOUND_SPAN, EFFECT_SPAN, TIME_OF_DAY_SPAN in Phase 2
- Levitation calibrated patterns (HOVER/CLIMB/RELEASE) migrate to EFFECT_SPAN presets
- Text input: text GUI, not anvil (cross-plugin preference, §9)
- Auto-preview mode: session-level toggle, cross-department (§9)
- Melody/motif cues (`motif.*`, `gracie.*`) are NOT Spans — discrete authored sequences, untouched
- `world_preview: LIVE | VALUES_ONLY` — session-level param for server-wide instruments (§9)
- LIGHTNING dual-anchor model: `scene_origin` (current behavior) + `player` (OPS-034) (§11)
- TIME_OF_DAY_SPAN presets: yes. WEATHER presets: no. Individual TIME_OF_DAY snap presets: no. (§11)
- LIGHTNING presets: yes — anchor type + offset is a repeatable pattern worth naming (§11)
- SPAN field set updated: `interpolations:` map replaces single `interpolated_param`; supports simultaneous multi-param interpolation; `equal_temperament` curve added for pitch (§12)
- PHRASE primitive added (§12a): explicit authored sequence — Steps with vertical grouping via events array; cross-department (Sound: chord, Fireworks: volley/salvo); tempo_bpm + subdivision for beat-based addressing
- MELODY_SPAN concept superseded: glissando → SOUND_SPAN; explicit melody → PHRASE (§12a, item 13 closed)

**Blocking open items before any Java starts (see §15 for full list):**
⚑ 1 Edit target (show YAML vs. cue file loaded)
⚑ 2 Partial YAML handling
⚑ 3 Panel mockup
⚑ 4 Department walk (5 departments remain)
⚑ 5 Preset library file structure
⚑ 6 Span schema section in spec.md
⚑ 7 Span type list confirmation
⚑ 8 Cross-plugin text input UI pattern
**To resume:** Read this file top-to-bottom, then read `tech-rehearsal-phase2-spec.md`
for the underlying spec context. First: confirm ⚑16 (SPAN → PATTERN rename), then resume
Effects walk.

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

### ✅ World preview param — server-wide effect behavior (cross-department)

A session-level param `world_preview: LIVE | VALUES_ONLY` controls behavior for instruments that
affect the entire server (TIME_OF_DAY, WEATHER).

- **LIVE** — every param change immediately applies to the world. The world is the preview.
- **VALUES_ONLY** — editing updates raw_yaml only. World is not touched during edit. An explicit
  `[▶ Apply to world]` button is available for spot-checking the current value.

Plugin config default: `LIVE` (you're already in-scene; see the world as it actually is by default).

The contextual toggle appears **only** in edit panels for affected instruments (TIME_OF_DAY, WEATHER):

```
World preview: LIVE  [Toggle]  [Remember this]
```

- `[Toggle]` — flips LIVE ↔ VALUES_ONLY immediately within the current session
- `[Remember this]` — writes the setting to plugin config as the default for all future Phase 2 sessions

LIGHTNING is **not** subject to this param — strikes fire and disappear; their `[▶ Preview]` button
is the normal auto-preview mechanism and has no server-wide persistence.

Departments where this param is relevant: TIME_OF_DAY, TIME_OF_DAY_SPAN, WEATHER.
All other departments: param not shown.

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

### ✅ Lighting (Steve N.) — locked 2026-04-05

**Instruments:** TIME_OF_DAY (point), TIME_OF_DAY_SPAN (fade-type Span), WEATHER (bar), LIGHTNING (point).

**Server-wide constraint:** TIME_OF_DAY and WEATHER affect all players on the server — not just
show participants. This is Steve's central discipline constraint in production and carries into Phase 2
unchanged. The `world_preview` param (§9) is the mechanism for managing this during editing.

---

#### TIME_OF_DAY edit mode

**Interaction model:** Panel-based, inherently live (subject to `world_preview` setting). No game
mode switch.

**Panel fields:**
- `time` — scroll wheel. Per notch: ±1000 ticks. Shift+scroll: ±100 ticks (fine-tune between
  named points).
- As you scroll, the actionbar shows value and nearest named sky state:
  `time: 13200 — near Dusk (13000)`
- Snap offer: when within ~200 ticks of a named point, chat shows
  `[Snap to: Dusk (13000)]` — click to lock. Same snap mechanic as timing nudge (§5).
- World preview toggle line shown in panel (§9):
  `World preview: VALUES_ONLY  [Toggle]  [Remember this]`

**No preview button** — in LIVE mode, every scroll notch is a world change. In VALUES_ONLY mode,
`[▶ Apply to world]` fires the current value once.

**Exit:** `[Save]  [Save as Preset]  [Cancel]`

**Preset:** No preset for individual TIME_OF_DAY snaps — single field, too trivial to name.
Span presets cover the reusable use case (see TIME_OF_DAY_SPAN below).

---

#### TIME_OF_DAY_SPAN edit mode

**Interaction model:** Panel-based. Auto-preview OFF by default for Spans (a full transition fires
on every param change — disruptive). Explicit `[▶ Preview]` only.

**Panel fields:**
- `start_value` — scroll wheel, same arc label display as TIME_OF_DAY
- `end_value` — scroll wheel, same arc label display
- `steps` — scroll wheel; actionbar shows step interval: `every 27t (~1.3s)`
- `total_duration` — scroll wheel ±20t per notch, Shift ±100t; shown as `240t (12s)`
- `curve` — panel selector: `linear / ease_in / ease_out`
- World preview toggle line shown in panel (§9)

**`[▶ Preview]`** — fires the full Span expansion. World actually transitions through all N steps
in real time. Steve watches the sky move and judges whether it reads as atmospheric.

**Preset:** ✅ Yes — full Span config (start_value, end_value, steps, total_duration, curve) is
reusable and worth naming.

**Preset naming:** `lighting.[direction].[slug]`
- Direction auto-component inferred from start/end: `dawn` (toward 0), `dusk` (toward 18000),
  `midnight` (landing at 18000), `noon` (toward 6000)
- Examples: `lighting.dawn.earned_sunrise`, `lighting.dusk.long_night_open`, `lighting.dusk.battle_open`

---

#### WEATHER edit mode

**Interaction model:** Panel-based, inherently live (subject to `world_preview` setting).
Single field.

**Panel fields:**
- `state` — panel selector: `clear / storm / thunder`. Single click to change.
- `duration_ticks` — scroll wheel (optional). `[Clear]` removes the field (persistent weather).
- World preview toggle line shown in panel (§9)

**No preset for WEATHER** — single field, same reasoning as STOP_SOUND. Too trivial to name.

**Note:** Every weather change is also a Sound design event. Sound Designer coordination is a
production concern, not a Phase 2 UI concern — documented in the Lighting KB.

---

#### LIGHTNING edit mode

**Interaction model:** Panel-based. No world-state persistence — strike fires and disappears.
Auto-preview is meaningful here.

**Dual-anchor model:**
LIGHTNING supports two anchor types:

| Anchor | Behavior |
|---|---|
| `scene_origin` | Offset from scene set origin mark — consistent world position |
| `player` | Offset from player's position at event-fire time — follows the player |

`anchor: player` requires a Java capability not yet implemented (OPS-034). The Phase 2 panel
supports both anchor types; `player` anchor is editable and saveable to presets, but fires in
production only after OPS-034 is resolved.

**Panel fields:**
- `anchor` — panel selector: `scene_origin / player`
- `x`, `y`, `z` — scroll wheel per field. Per notch: ±1 tick. Shift+scroll: ±5.
- `[▶ Preview]` — fires a test strike at current offset relative to current anchor. In
  `player` anchor mode, fires relative to player's current standing position — useful for
  walking the bolt around to dial in proximity.
- Auto-preview toggle applies (§9): when ON, a strike fires on each scroll notch change.

**Exit:** `[Save]  [Save as Preset]  [Cancel]`

**Preset:** ✅ Yes — anchor type + offset is a reusable named pattern.

**Preset naming:** `lighting.lightning.[anchor_type].[slug]`
- Examples:
  - `lighting.lightning.player.surprise_close` — `{anchor: player, x: 1, y: 0, z: 1}`
  - `lighting.lightning.player.side_crack` — `{anchor: player, x: 4, y: 0, z: 0}`
  - `lighting.lightning.scene.throne_strike` — `{anchor: scene_origin, x: -3, y: 0, z: 2}`

**Preset captures:** `anchor` + `x`, `y`, `z`. Mark is not stored (scene-specific context
resolved at fire time by anchor type).

**Multi-strike patterns:** Each LIGHTNING event in a cue is edited independently. There is no
pattern-group edit mode for lightning sequences. A repeatable chaos burst is a cue-level authoring
choice; named presets cover the individual-event level.

---

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

### ✅ Three behavioral modes

**Fade-type Span** (any param where `start ≠ end`): interpolates one or more params from
start to end across N steps. Examples: Sound simulated fade (volume), Lighting time
transition (time).

**Glissando-type Span** (pitch interpolated with `equal_temperament` curve): a fade-type
Span whose pitch param uses logarithmic (multiplicative) spacing so each step sounds like
an equal musical interval. Pitch sounds like an even glide up or down; arithmetic linear
spacing sounds uneven to the ear and should not be used for pitch. Volume may be
interpolated simultaneously (crescendo into or out of the glissando).

**Pulse-type Span** (all params with `start == end`): repeats a fixed config N times at
calibrated cycle timing. Example: Effects levitation patterns (HOVER, CLIMB, RELEASE).

### ✅ Span field set

`interpolated_param`, `start_value`, and `end_value` are replaced by an `interpolations:`
map, supporting simultaneous interpolation of any number of params. Each param entry
specifies its own `start`, `end`, and optional `curve`.

| Field | Required | Description |
|---|---|---|
| `interpolations` | Yes | Map of `param: {start, end, curve}` — one entry per interpolated param |
| `steps` | Yes | Number of events to distribute |
| `total_duration` | Yes | Total tick length of the Span |
| `curve` | No | Span-level default curve: `linear` (default) \| `ease_in` \| `ease_out` \| `equal_temperament` — overridden per-param |
| `step_duration` | No | For event types with internal duration (EFFECT): how long each step event lasts |
| `gap` | No | Ticks between step end and next step start (interval = step_duration + gap) |

**Per-param curve options:**

| Curve | Behavior | Use for |
|---|---|---|
| `linear` | Equal arithmetic steps (default) | Volume, amplifier, time |
| `ease_in` | Slow start, accelerating change | Swells, tension builds |
| `ease_out` | Fast start, decelerating change | Release, fading |
| `equal_temperament` | Multiplicative steps — equal musical intervals | Pitch only — sounds like a smooth glide |

**`equal_temperament` math:** SpanExpander multiplies the previous value by
`(end/start)^(1/(steps-1))` at each step rather than adding a linear increment.
This produces semitone-spaced values when spanning the full two-octave note block range
(pitch 0.5 → 2.0 = 24 semitones). Default and recommended curve for any pitch
interpolation.

**Note block pitch reference:**

Minecraft note blocks span exactly two octaves: F#3 (pitch 0.5) to F#5 (pitch 2.0),
25 discrete semitone positions. Formula: `pitch = 2^((semitone - 12) / 12)` where
semitone 0 = F#3, 12 = F#4 (natural), 24 = F#5.

| Scale | Semitone step | Pitches in 2 octaves | Notes |
|---|---|---|---|
| Chromatic | 1 | 25 | Full range — maximum resolution |
| Whole-tone | 2 | 13 | Gracie's native vocabulary — characteristic sound |
| Major / minor | varies | 8 per octave | Tonal glissando — specific key feel |

**Glissando example — whole-tone harp sweep (ascending):**

```yaml
type: SOUND_SPAN
sound_id: minecraft:block.note_block.harp
steps: 13                  # 13 whole-tone steps across 2 octaves
total_duration: 130        # 10 ticks per note (~120 BPM eighth note feel)
interpolations:
  pitch:
    start: 0.5             # F#3 — lowest note block pitch
    end: 2.0               # F#5 — highest note block pitch
    curve: equal_temperament
  volume:
    start: 0.9
    end: 0.9               # constant — omit for same effect
    curve: linear
```

**Crescendo glissando — pitch rises while volume builds:**

```yaml
type: SOUND_SPAN
sound_id: minecraft:block.note_block.harp
steps: 13
total_duration: 130
interpolations:
  pitch:
    start: 0.5
    end: 2.0
    curve: equal_temperament
  volume:
    start: 0.3
    end: 1.0
    curve: ease_in         # volume accelerates into the peak
```

**Simulated volume fade (original SOUND_SPAN use case — unchanged):**

```yaml
type: SOUND_SPAN
sound_id: minecraft:block.note_block.harp
steps: 6
total_duration: 120
interpolations:
  volume:
    start: 0.9
    end: 0.1
    curve: linear
```

### ✅ Phase 2 Span types

Three Span types ship in Phase 2:

| Type | Department | Interpolatable params | Primary use |
|---|---|---|---|
| `SOUND_SPAN` | Sound | `pitch` (equal_temperament), `volume` (linear/ease) | Glissando, simulated fade, crescendo |
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

### ✅ Melodic content: SPAN vs. PHRASE

The previous position ("melody/motif cues are NOT Spans") is corrected. The model is now:

**A melody CAN be a Span** — when the pitch sequence is computed (interpolated across steps).
A glissando is the clearest example: define start pitch, end pitch, step count, and
`equal_temperament` curve; the engine generates every note. Gracie's whole-tone sweeps are
natural SOUND_SPANs. Any musical figure whose notes follow a smooth, calculable arc belongs here.

**A melody is a PHRASE** — when each note is explicitly authored and intentional. A specific
melodic line where note 3 is a deliberate D# and note 5 is an intentional rest is not
computable; it must be written out. PHRASEs are the in-game authoring primitive for this.
See §12a for the PHRASE specification.

**Existing `motif.*` and `gracie.*` cues** remain as-is — hand-authored sequences of SOUND
events in cue YAML. They are valid, complete, and do not need migration. SPAN and PHRASE are
new authoring primitives; they don't replace working cues.

**The design question going forward:** when adding new melodic material to the library,
choose the right primitive:
- Interpolated sweep (glissando, chromatic scale, whole-tone run) → SOUND_SPAN
- Specific authored line (riff, motif, melody) → PHRASE
- Repeating rhythmic figure (pulse, arpeggio with fixed pitches) → PHRASE with repeated steps

### Java layer sizing estimate (updated)

| Layer | Work | Rough size |
|---|---|---|
| Spec (spec.md Span section) | Documentation | 1 session |
| Model classes (SpanEvent + 3 subtypes) | Java | ~250 lines (multi-param interpolations map) |
| SpanExpander (expansion math + curves, incl. equal_temperament) | Java | ~300 lines |
| EventParser routing | Java | ~50 lines |
| ShowYamlEditor 5th operation | Java | ~150 lines |
| TechCuePanel Span display + edit | Java/UI | ~300 lines |
| PHRASE model + PhraseExpander (§12a) | Java | ~300 lines |
| PHRASE in TechCuePanel (step editor, beat grid) | Java/UI | ~400 lines |
| **Total** | | **~6–8 weeks** |

---

## 12a. PHRASE Event Architecture

### ✅ PHRASE is a YAML primitive — the explicit sequence counterpart to SPAN

A PHRASE is a first-class event type in the ScaenaShows YAML schema. Where SPAN computes
its steps (interpolated from start/end), PHRASE contains explicitly authored steps — each
event is intentional. The engine expands a PHRASE to individual events at show-load time,
the same way SPANs are expanded. Scheduler and executors see only the expanded events.

**The two primitives, distinguished:**

| | SPAN | PHRASE |
|---|---|---|
| Step values | Computed (interpolated from start/end) | Explicit (authored per step) |
| Author's work | Define endpoints and let the engine fill the middle | Write each event |
| Use for | Glissandi, fades, levitation cycles, time transitions | Melodies, rhythmic figures, salvos, volleys |
| Expressiveness | Low — defined by endpoints and curve | High — fully specified |

### ✅ PHRASE field set

| Field | Required | Description |
|---|---|---|
| `tempo_bpm` | No | Enables beat-based step addressing (`at_beat:`). If absent, steps use `at:` in raw ticks |
| `subdivision` | No | Smallest rhythmic unit available in the beat grid: `4` = quarter notes, `8` = eighth notes (default), `16` = sixteenth notes |
| `steps` | Yes | Ordered list of step entries |

**Step entry fields:**

| Field | Required | Description |
|---|---|---|
| `at:` | Yes (if no tempo_bpm) | Step offset in ticks from PHRASE start |
| `at_beat:` | Yes (if tempo_bpm set) | Step offset in beats (e.g., `1.5` = eighth note after beat 1) |
| `events:` | Yes | Array of one or more event configs — multiple = vertical grouping |

### ✅ Tempo, subdivision, and tick math

When `tempo_bpm` is present, PhraseExpander converts `at_beat:` positions to ticks at
load time: `tick = (at_beat - 1) × (60 / tempo_bpm × 20)`.

| BPM | Quarter note | Eighth note | Sixteenth note |
|---|---|---|---|
| 60 | 20t | 10t | 5t |
| 90 | 13t | 7t | 3t |
| 120 | 10t | 5t | 3t (floor) |
| 180 | 7t | 3t | 2t (floor) |

Note: at fast tempos, sixteenth notes approach the 1t minimum. The Minecraft tick is the
hard floor for all timing.

`subdivision` constrains the Phase 2 beat-grid editor — in-game step placement snaps to
the available positions for that subdivision. At `subdivision: 8`, beat positions of `1.25`
(sixteenth) are unavailable until subdivision is changed.

**Rests** are implicit — the absence of a step at a beat position is silence.

### ✅ Vertical grouping — events array

A step with a single entry in `events:` fires one event. A step with multiple entries fires
all of them at the same tick. This is the vertical grouping mechanism — the structural
equivalent of a chord in music or a volley in fireworks.

No special keyword is required. The array length is the signal:

```yaml
# Single note
- at_beat: 1.0
  events:
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.0, volume: 0.8}

# Chord — three notes at once (vertical grouping)
- at_beat: 2.0
  events:
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.0, volume: 0.7}
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.26, volume: 0.6}
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.5, volume: 0.5}
```

### ✅ PHRASE vocabulary by department

| Department | Single event | Vertical grouping | Container |
|---|---|---|---|
| Sound | Note | Chord | Phrase |
| Fireworks | Burst | Volley | Salvo |
| Effects (particle) | Pulse | Cluster | Pattern |
| General | Event | Grouping | Phrase |

The vocabulary is documentation and UI language only — the YAML structure is identical
across departments.

### ✅ SOUND PHRASE example — explicit melodic line

```yaml
type: PHRASE
tempo_bpm: 120
subdivision: 8
steps:
  - at_beat: 1.0
    events:
      - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.0, volume: 0.8}
  - at_beat: 1.5
    events:
      - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.12, volume: 0.8}
  - at_beat: 2.0    # chord
    events:
      - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.0, volume: 0.7}
      - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.26, volume: 0.6}
      - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.5, volume: 0.5}
  - at_beat: 2.5
    events:
      - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 0.89, volume: 0.9}
```

### ✅ FIREWORKS PHRASE example — salvo with volleys

```yaml
type: PHRASE
tempo_bpm: 80
steps:
  - at_beat: 1.0         # single burst
    events:
      - {type: FIREWORK, preset_id: fireworks.burst.gold.high}
  - at_beat: 2.0         # volley — simultaneous bursts
    events:
      - {type: FIREWORK, preset_id: fireworks.burst.gold.high}
      - {type: FIREWORK, preset_id: fireworks.burst.silver.mid}
      - {type: FIREWORK, preset_id: fireworks.burst.red.low}
  - at_beat: 3.0
    events:
      - {type: FIREWORK, preset_id: fireworks.burst.white.finale}
```

### ✅ SPAN vs. PHRASE decision guide for melodic content

| Musical figure | Primitive | Reasoning |
|---|---|---|
| Harp glissando (ascending whole-tone sweep) | SOUND_SPAN | Pitches are computed — equal_temperament curve |
| Chromatic scale passage | SOUND_SPAN | Computed — linear semitone steps |
| Specific authored riff (each note intentional) | PHRASE | Not computable |
| Arpeggio with fixed pitches | PHRASE | Pitches are specific, not interpolated |
| Firework salvo (timed sequence of bursts) | PHRASE | Each burst explicitly placed |
| Levitation hover/climb/release | EFFECT_SPAN | Repeating pulse — computed cycling |
| Volume fade | SOUND_SPAN | Single param interpolated — no note pitch needed |
| Crescendo glissando (pitch + volume both change) | SOUND_SPAN | Both params interpolated simultaneously |

### ✅ PHRASE in TechCueSession (Phase 2)

Same invariant as SPAN: `TechCueSession.raw_yaml` stores PHRASEs in unexpanded form.
PhraseExpander runs at `enterPreview()`. ShowYamlEditor gains a **6th operation:**
`editPhrase(phraseYamlNode, stepIndex, PhraseStepParams)` — mutates a specific step's
events in raw_yaml.

Phase 2 panel displays PHRASEs as expandable groups:

```
  tick 240: arrival_motif                         [Phrase ▾]
    beat 1.0   harp  pitch: 1.0  vol: 0.8         [Edit ▸]
    beat 1.5   harp  pitch: 1.12 vol: 0.8         [Edit ▸]
    beat 2.0   CHORD  3 notes                     [Edit ▸]
    beat 2.5   harp  pitch: 0.89 vol: 0.9         [Edit ▸]
```

`[Edit ▸]` on any step opens the step editor. For a chord step, the editor shows all
events in the vertical grouping with individual `[Edit]` and `[Remove]` per event, plus
`[+ Add note]`.

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
| ✅ 13 | MELODY_SPAN concept superseded (2026-04-05): melodic content is now covered by two primitives. SOUND_SPAN with `equal_temperament` curve handles interpolated pitch sequences (glissandi, whole-tone sweeps). PHRASE (§12a) handles explicitly authored note sequences. No standalone MELODY_SPAN type needed. | Closed |
| ✅ 14 | `world_preview` param: default confirmed as `LIVE` (2026-04-05). In-scene editing shows reality by default; toggle available contextually to suppress when needed. | Closed |
| 📋 15 | OPS-034 (player-anchored LIGHTNING): Java capability gap filed 2026-04-05. Player anchor presets can be authored and saved in Phase 2; they require OPS-034 to fire correctly in production. Not blocking Phase 2 panel work. | No |
| ⚑ 16 | SPAN → PATTERN rename: confirm before updating all references throughout this doc, the building spec, and the Java model names. Alan's reframe: PATTERN is consistent with existing fireworks spatial patterns — both are generative primitives (rules in, events out). If confirmed: find/replace SPAN→PATTERN throughout, update §12 and §12a headers, update Java class name estimates. | Yes — before any Java work on SpanExpander / SpanEvent |

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

*Session paused 2026-04-05 — account transition. Sound locked. Lighting locked. Span
architecture updated (multi-param, equal_temperament, glissando). PHRASE primitive added.
OPS-034 filed. Active decision: ⚑16 SPAN → PATTERN rename. Department walk paused at
Effects. Remaining: Effects, Fireworks, Camera, Voice, Choreography.*
