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

**Last action (2026-04-05):** Effects (Felix) department walk complete and locked. Covers:
EFFECT (point, preset library), PLAYER_TELEPORT (panel, no preset), CROSS_TO (panel, no
preset), PLAYER_VELOCITY (panel, named vector presets), PLAYER_FLIGHT (panel, release presets),
EFFECT_PATTERN (cycle_ticks field set locked, 3 calibrated levitation presets: hover/climb/release),
PARTICLE (single + atmospheric inline, deferred PARTICLE_PATTERN, named atmospheric presets),
EFFECT_PHRASE (mixed EFFECT + PARTICLE steps, cluster support, step-list panel). Preset naming
`effects.[type].[slug]`. Auto-preview ON for EFFECT/PARTICLE, OFF for movement types.
PARTICLE_PATTERN deferred. EFFECT_PATTERN amplifier sweep deferred to Phase 3.

Prior actions same session: PATTERN rename confirmed (⚑16), department scan complete,
Effects PHRASE vocabulary resolved (⚑18), MUSIC event type spec written into §12b (⚑17
design complete), OPS-035 migration scoped.

**To resume next session:** Department walk at Fireworks. Read this file top-to-bottom, then
read `kb/departments/fireworks/fireworks.kb.md` for instrument context.

**Department walk status:**

| Department | Status |
|---|---|
| Set | ✅ Locked |
| Casting | ✅ Locked |
| Wardrobe | ✅ Locked |
| Sound | ✅ Locked (2026-04-05) |
| Lighting | ✅ Locked (2026-04-05) |
| Effects | ✅ Locked (2026-04-05) |
| Fireworks | 📋 Orientation captured — walk pending |
| Camera | 📋 Orientation captured — walk pending |
| Voice | 📋 Orientation captured — walk pending |
| Choreography | 📋 Orientation captured — walk pending |

**Next department to walk:** Fireworks.

**Key architectural decisions locked this session:**
- Pattern is a YAML primitive (§12) — SOUND_PATTERN, EFFECT_PATTERN, TIME_OF_DAY_PATTERN in Phase 2
- PATTERN rename confirmed (⚑16 closed 2026-04-05): SPAN → PATTERN throughout. Find/replace
  complete in this doc. Building spec and Java model names to follow when Java begins.
- PATTERN two-primitive model: PATTERN (rules in → events computed) / PHRASE (every step explicit)
- Fireworks spatial types (FIREWORK_CIRCLE, FIREWORK_LINE, etc.) are formally FIREWORK_PATTERN
  subtypes — schema migration flagged (see ⚑19)
- MUSIC: new first-class event type, distinct from SOUND (⚑17 opened). Parallel consideration
  principle: changes to MUSIC warrant checking SOUND for applicability, and vice versa.
  `equal_temperament` curve migrates from SOUND_PATTERN to MUSIC_PATTERN when MUSIC is specced.
- Levitation calibrated patterns (HOVER/CLIMB/RELEASE) migrate to EFFECT_PATTERN presets
- Text input: text GUI, not anvil (cross-plugin preference, §9)
- Auto-preview mode: session-level toggle, cross-department (§9)
- Melody/motif cues (`motif.*`, `gracie.*`) are NOT Patterns — discrete authored sequences, untouched
- `world_preview: LIVE | VALUES_ONLY` — session-level param for server-wide instruments (§9)
- LIGHTNING dual-anchor model: `scene_origin` (current behavior) + `player` (OPS-034) (§11)
- TIME_OF_DAY_PATTERN presets: yes. WEATHER presets: no. Individual TIME_OF_DAY snap presets: no. (§11)
- LIGHTNING presets: yes — anchor type + offset is a repeatable pattern worth naming (§11)
- Pattern field set: `interpolations:` map replaces single `interpolated_param`; supports
  simultaneous multi-param interpolation; `equal_temperament` curve added for pitch (§12)
- PHRASE primitive added (§12a): explicit authored sequence — Steps with vertical grouping via
  events array; cross-department (Sound: chord, Fireworks: volley/salvo); tempo_bpm + subdivision
  for beat-based addressing
- MELODY_PATTERN concept superseded: glissando → SOUND_PATTERN; explicit melody → PHRASE (§12a, item 13 closed)

**Blocking open items before any Java starts (see §15 for full list):**
⚑ 1 Edit target (show YAML vs. cue file loaded)
⚑ 2 Partial YAML handling
⚑ 3 Panel mockup
⚑ 4 Department walk (4 departments remain — Fireworks next)
⚑ 5 Preset library file structure
⚑ 6 Pattern schema section in spec.md
⚑ 7 ✅ Pattern type list confirmed (2026-04-05): SOUND_PATTERN, EFFECT_PATTERN, TIME_OF_DAY_PATTERN
⚑ 8 Cross-plugin text input UI pattern
⚑ 17 MUSIC event type spec — field set, pitch notation, instrument list
✅ 18 Effects PHRASE vocabulary confirmed: Pulse / Cluster / Phrase (container). Pattern = type only.

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

Departments where this param is relevant: TIME_OF_DAY, TIME_OF_DAY_PATTERN, WEATHER.
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

**Note on Patterns:** Simulated fades (multiple descending-volume SOUND events) are Pattern
candidates — see §12 Pattern Event Architecture. Each individual SOUND event is still
editable independently, but Phase 2 offers Pattern group edit when the pattern is detected
as a group. Motifs and melodies (`motif.*`, `gracie.*`) are NOT Patterns — they are discrete
authored note sequences and remain as named cues. No conflict.

**Preset captures (SOUND only):** `sound_id`, `category`, `volume`, `pitch`,
`max_duration_ticks` (if set). Complete snapshot. No preset for STOP_SOUND.

**Preset naming:** `sound.[category].[pitch_register].[slug]`
- Examples: `sound.hostile.low.warden_presence`, `sound.ambient.natural.cave_under`,
  `sound.master.high.arrival_bell`

**Note on MUSIC (new type — 2026-04-05):** MUSIC is a new first-class event type, distinct
from SOUND, confirmed during department scan. The distinction:

- `SOUND` / `SOUND_PATTERN` / `SOUND_PHRASE` — any Minecraft sound ID; pitch is a continuous
  multiplier (0.5–2.0) applied to the sound's natural pitch. General sound effects.
- `MUSIC` / `MUSIC_PATTERN` / `MUSIC_PHRASE` — one noteblock instrument for the whole
  pattern or phrase; pitch steps expressed in musical notation (instrument-relative semitone
  names, e.g. F#3, A4). Produces actual musical intervals.

The `equal_temperament` curve established in §12 is correctly a MUSIC_PATTERN feature
(logarithmic pitch spacing for musical intervals) — it should migrate from SOUND_PATTERN
to MUSIC_PATTERN when the MUSIC type is specced. See ⚑17.

**Parallel consideration principle (locked 2026-04-05):** MUSIC and SOUND are sister types.
Changes to one warrant explicit consideration of the other: does the same change apply?
Should it be consistent? This applies to field additions, preset format, Phase 2 edit panel
design, and spec sections.

MUSIC type spec, field set, and Phase 2 edit panel are not yet designed. See ⚑17.

### ✅ Lighting (Steve N.) — locked 2026-04-05

**Instruments:** TIME_OF_DAY (point), TIME_OF_DAY_PATTERN (fade-type Pattern), WEATHER (bar), LIGHTNING (point).

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
Pattern presets cover the reusable use case (see TIME_OF_DAY_PATTERN below).

---

#### TIME_OF_DAY_PATTERN edit mode

**Interaction model:** Panel-based. Auto-preview OFF by default for Patterns (a full transition fires
on every param change — disruptive). Explicit `[▶ Preview]` only.

**Panel fields:**
- `start_value` — scroll wheel, same arc label display as TIME_OF_DAY
- `end_value` — scroll wheel, same arc label display
- `steps` — scroll wheel; actionbar shows step interval: `every 27t (~1.3s)`
- `total_duration` — scroll wheel ±20t per notch, Shift ±100t; shown as `240t (12s)`
- `curve` — panel selector: `linear / ease_in / ease_out`
- World preview toggle line shown in panel (§9)

**`[▶ Preview]`** — fires the full Pattern expansion. World actually transitions through all N steps
in real time. Steve watches the sky move and judges whether it reads as atmospheric.

**Preset:** ✅ Yes — full Pattern config (start_value, end_value, steps, total_duration, curve) is
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

### ✅ Effects (Felix) — locked 2026-04-05

**Department overview:** Felix owns four instrument categories: scripted movement
(PLAYER_TELEPORT, CROSS_TO, PLAYER_VELOCITY), aerial physics (PLAYER_FLIGHT + EFFECT
levitation), perceptual alteration (EFFECT potions), and atmospheric presence (PARTICLE).
All are point events. Generative types: EFFECT_PATTERN (calibrated levitation pulse cadences)
and EFFECT_PHRASE (authored sequences mixing EFFECT + PARTICLE steps). PARTICLE_PATTERN
stays deferred — the inline `duration_ticks + interval_ticks` form handles Phase 2.

**Vocabulary locked (carried from orientation scan):**
- **Pulse** — single EFFECT or PARTICLE event
- **Cluster** — multiple events at the same step (`events:` array)
- **Phrase** — EFFECT_PHRASE container (authored sequence, each step intentional)
- **Pattern** — EFFECT_PATTERN type (computed repeating pulse)

**Effects + Camera joint authority:** PLAYER_TELEPORT `yaw`/`pitch` and CROSS_TO `facing`
are Camera's domain — Felix positions the body, Camera positions the eyes. Phase 2 shows
these fields in the Effects panel but labels them `(Camera authority)`. Editable, but
editorial responsibility is documented.

**Auto-preview policy:**
- EFFECT and PARTICLE events: auto-preview ON by default (re-fires on each param change)
- Movement events (PLAYER_TELEPORT, CROSS_TO, PLAYER_VELOCITY, PLAYER_FLIGHT): auto-preview
  OFF by default — physically moving the player on each scroll notch is disruptive.
  Explicit `[▶ Preview]` only.

---

#### EFFECT event (potion effects — point)

**Interaction model:** Panel-based. Auto-preview ON.

**Panel fields:**
- `effect_id` — panel list by KB category (Aerial: `levitation`, `slow_falling`; Perceptual:
  `blindness`, `darkness`, `nausea`, `slowness`, `speed`, `night_vision`) + `[Enter custom ID →]`
- `amplifier` — scroll wheel, integer steps. KB-derived descriptor alongside value:
  `amp 0 — subtle` / `amp 1 — pronounced` / `amp 9 — surge (arrival)`
- `duration_ticks` — scroll wheel ±10t per notch, Shift ±100t. Shown as `20t (1s)`
- `hide_particles` — toggle. Default: true (always hidden in show context)
- `audience` — selector: `participants / group_1 / group_2`

**`[▶ Hear it]`** — applies the effect to the designer for duration_ticks. Auto-preview
re-applies on each param change when ON.

**Preset:** ✅ Yes — named effect configurations. Audience not captured (set at call time).

**Preset naming:** `effects.[effect_id].[slug]`
- `effects.levitation.amp9_surge` — `{effect_id: levitation, amplifier: 9, duration_ticks: 40, hide_particles: true}`
- `effects.blindness.revelation_flash` — `{effect_id: blindness, amplifier: 0, duration_ticks: 15, hide_particles: true}`
- `effects.darkness.deep_dread` — `{effect_id: darkness, amplifier: 0, duration_ticks: 200, hide_particles: true}`

---

#### PLAYER_TELEPORT event (point)

**Interaction model:** Panel-based. Auto-preview OFF.

**Panel fields:**
- `destination` — panel selector: `set:Name` (from show's `sets:` block) or `offset mode`
- `offset` (if offset mode) — x/y/z scroll wheels ±0.5 per notch, Shift ±5
- `yaw` — scroll wheel ±5° per notch. Labeled `(Camera authority)`
- `pitch` — scroll wheel ±5° per notch. Labeled `(Camera authority)`

**`[▶ Preview]`** — fires the teleport once. Standard blackout technique (Camera fires
blackout 10t before teleport) is a production-level pattern, not encoded as a Phase 2 preset.

**Preset:** ✗ No — destination is scene-specific (`set:Name` resolves to a show-specific mark);
yaw/pitch are Camera-coordinated per scene.

---

#### CROSS_TO event (point)

**Interaction model:** Panel-based. Auto-preview OFF.

**Panel fields:**
- `destination` — panel selector: `mark:Name` (from show's `marks:` block)
- `duration_ticks` — scroll wheel ±20t per notch, Shift ±100t. Shown as `60t (3s)`
- `facing` — optional. Selector: `compass:north / east / south / west / none`. Labeled
  `(Camera authority)`

**`[▶ Preview]`** — executes the cross from current player position to destination mark.

**Preset:** ✗ No — `mark:Name` is scene-specific; duration and facing are scene-dependent.

---

#### PLAYER_VELOCITY event (point)

**Interaction model:** Panel-based. Auto-preview OFF. Repeated test fires are needed to
dial vector values — always use explicit `[▶ Preview]`.

**Panel fields:**
- `x`, `y`, `z` — scroll wheel per axis, ±0.1 per notch, Shift ±0.5
- KB-derived y descriptor shown alongside: `0.4 — gentle lift` / `1.2 — clear push` /
  `2.0 — dramatic launch` / `3.0+ — extreme`

**`[▶ Preview]`** — applies the vector impulse to the player once.

**Preset:** ✅ Yes — named vector profiles are reusable across scenes.

**Preset naming:** `effects.velocity.[slug]`
- `effects.velocity.gentle_lift` — `{vector: {x: 0, y: 0.4, z: 0}}`
- `effects.velocity.clear_push` — `{vector: {x: 0, y: 1.2, z: 0}}`
- `effects.velocity.dramatic_launch` — `{vector: {x: 0, y: 2.0, z: 0}}`

---

#### PLAYER_FLIGHT event (point)

**Interaction model:** Panel-based. Auto-preview OFF for both states.

**Panel fields — state: hover:**
- `state` — selector: `hover / release`
- No additional fields. The event is: lock altitude now.

**Panel fields — state: release:**
- `state` — selector: `hover / release`
- `release_effect` — selector: `slow_falling / levitate / none`
- `release_duration_ticks` — scroll wheel ±20t, Shift ±100t

**`[▶ Preview]`** — fires the event once.

**Preset:**
- `hover`: ✗ No — no params; trivial to name.
- `release`: ✅ Yes — named release configurations.

**Preset naming:** `effects.flight.release.[slug]`
- `effects.flight.release.slow_landing` — `{release_effect: slow_falling, release_duration_ticks: 300}`
- `effects.flight.release.abrupt` — `{release_effect: none, release_duration_ticks: 0}`

---

#### EFFECT_PATTERN — levitation cadence (Pattern type)

The calibrated levitation patterns (HOVER, CLIMB, RELEASE) are all EFFECT_PATTERN presets.
An EFFECT_PATTERN auto-repeats a potion effect at a defined pulse cadence for a total duration.

**Field set:**
```yaml
type: EFFECT_PATTERN
effect_id: levitation
amplifier: 0
duration_ticks: 20      # per-pulse: how long each effect application lasts
cycle_ticks: 28         # interval between pulse starts (every N ticks)
                        #   implicit gap = cycle_ticks - duration_ticks
                        #   HOVER: gap=8t (gravity returns briefly between pulses)
                        #   CLIMB: cycle==duration → 0t gap (continuous, no rest)
                        #   RELEASE: gap=24t (long gravity window → controlled descent)
total_duration: 300     # total runtime in ticks
hide_particles: true
audience: participants
```

**`cycle_ticks` adoption note:** The cadence field is named `cycle_ticks` (time between pulse
*starts*). Implicit gap = `cycle_ticks - duration_ticks`. If `cycle_ticks < duration_ticks`,
pulses overlap — engine warns at author time (overlapping levitation is undefined behavior).

**Interpolation in Phase 2:** Fixed cadence pulse only. Amplifier sweep (e.g., 0→9 crescendo)
is a Phase 3 candidate — deferred.

**Interaction model:** Panel-based. Auto-preview ON — runs a short 100t preview window so
Felix can feel the cadence immediately.

**Panel fields:**
- `effect_id` — panel list (same curated list as EFFECT event)
- `amplifier` — scroll wheel (integer), with KB descriptor labels
- `duration_ticks` — scroll wheel ±4t. Actionbar: `20t — each pulse lasts 1s`
- `cycle_ticks` — scroll wheel ±4t. Actionbar: `28t — fires every 1.4s (8t gap)` or
  `24t — fires every 1.2s (continuous)` or `⚠ cycle < duration — overlapping pulses`
- `total_duration` — scroll wheel ±20t, Shift ±100t. Shown as `300t (15s)`
- `hide_particles` — toggle (default true)

**`[▶ Preview]`** — runs a 100t preview window on the designer.

**Preset:** ✅ Yes — three calibrated levitation patterns ship as named presets.

**Preset naming:** `effects.levitation.[slug]`

| Preset | duration_ticks | cycle_ticks | Sensation |
|---|---|---|---|
| `effects.levitation.hover` | 20 | 28 | Clean altitude hold — "gentle bubbling" |
| `effects.levitation.climb` | 24 | 24 | Continuous drift — "separation from earth" |
| `effects.levitation.release` | 20 | 44 | Slow controlled descent — "blood pressure release" |

**Preset file:** `effect-configs.yml`

---

#### PARTICLE event (point + repeating atmospheric)

PARTICLE has two inline forms — single burst (no `duration_ticks`) and repeating atmospheric
(with `duration_ticks + interval_ticks`). Both are the same event type; the optional fields
are what distinguish them.

**PARTICLE_PATTERN status: deferred.** The inline atmospheric form handles Phase 2 use cases.
Sweep-style interpolation of count/spread is not needed in Phase 2. PARTICLE_PATTERN stays
in the deferred list.

**Interaction model:** Panel-based. Auto-preview ON — fires a test burst at the designer's
position on each param change. For atmospheric, preview runs a 100t window.

**Panel fields:**
- `particle_id` — panel list organized by KB atmospheric category:
  - Quiet devastation: `ash`, `smoke`
  - Heat/danger: `flame`, `soul_fire_flame`
  - Arrival/ethereal: `end_rod`, `enchant`, `totem_of_undying`
  - Nature/alien: `warped_spore`, `crimson_spore`, `snowflake`
  - Expressive: `heart`, `note`
  - `[Enter custom ID →]` text GUI
- `count` — scroll wheel ±1, Shift ±10
- `offset` — x/y/z scroll wheels, ±0.5 per axis per notch. Shown as `[3.0, 2.0, 3.0]`
- `extra` — scroll wheel ±0.01 (speed multiplier for directional particles)
- `force` — toggle. Default: true
- `duration_ticks` — optional; scroll wheel ±20t, Shift ±100t. `[Clear]` removes (→ single burst)
- `interval_ticks` — shown only when duration_ticks is set; scroll wheel ±2t per notch.
  Actionbar: `every 10t — 20 bursts over 200t`

**`[▶ Preview]`** — fires one test burst (single) or runs 100t atmospheric window.

**Preset:** ✅ Yes — atmospheric layer configurations are highly reusable.

**Preset naming:** `effects.particle.[slug]`
- `effects.particle.ash_quiet` — ash, count 8, offset [3,2,3], interval 10, duration 200
- `effects.particle.ember_field` — flame, count 12, force true, interval 8, duration 160
- `effects.particle.arrival_sparkle` — end_rod, count 20, offset [2,2,2], interval 5

**Altitude note (confirmed KB behavior):** Particles spawn at the spatial anchor (typically
ground level), not at the player's current altitude. At height, ground-level particles read
as below. Use `offset.y` to place particles at the player's altitude — coordinate with the
show's set origin mark and agree player height with Fireworks before authoring.

**Preset captures:** full field set including duration/interval if set.

---

#### EFFECT_PHRASE — authored effects sequence (Phrase type)

Explicitly authored sequence of EFFECT and PARTICLE events. Each step intentional.
The "big moment" tool: a revelation sequence, a physical arc written step by step.
Cluster support (multiple events at one step) via `events:` array.

**Field set:**
```yaml
type: EFFECT_PHRASE
audience: participants
tempo_bpm: 120           # optional — enables at_beat: addressing
subdivision: 8           # optional
steps:
  - at: 0               # tick offset (or at_beat: if tempo set)
    type: EFFECT
    effect_id: blindness
    duration_ticks: 15
    amplifier: 0
    hide_particles: true
  - at: 15
    type: PARTICLE
    particle_id: minecraft:enchant
    count: 30
    offset: [2, 2, 2]
    extra: 0.0
    force: true
  - at: 30
    events:              # cluster: multiple events at one step
      - {type: EFFECT, effect_id: night_vision, duration_ticks: 200, amplifier: 0, hide_particles: true}
      - {type: PARTICLE, particle_id: minecraft:end_rod, count: 20, offset: [0, 1, 0], force: true}
```

**Audience:** Set at the phrase level. All steps inherit.

**Timing model:** `at:` in ticks by default. With `tempo_bpm + subdivision`, `at_beat:` is
supported. Mix of both allowed within one phrase (same model as §12a PHRASE).

**Interaction model:** Step-list panel. Step list is the primary view.

**Phase 2 panel:**
- Step list: each step shows `[tick/beat]  [type icon]  [brief descriptor]`
  - EFFECT step: `0t  ⚗  blindness amp:0 — 15t`
  - PARTICLE step: `15t  ✦  enchant ×30`
  - Cluster step: `30t  [cluster: 2 events]` → expands on click
- `[+ Add Step]` — appends at end; prompts type (EFFECT / PARTICLE), then opens field editor
- Per step: `[Edit]  [Insert Before]  [Delete]`  ·  `[↑] [↓]` reorder
- Click step → per-step field editor (same fields as the point event type)
- `[▶ Preview]` — runs the full phrase sequence on the designer

**Preset:** ✗ No phrase-level presets — sequences are too scene-specific. Steps may internally
reference effect/particle presets, but the phrase as a whole is authored per-show.

---

**Preset file:** `effect-configs.yml`

**Preset naming summary:**

| Category | Format | Examples |
|---|---|---|
| Levitation patterns (EFFECT_PATTERN) | `effects.levitation.[slug]` | `.hover` / `.climb` / `.release` |
| Single effect (EFFECT) | `effects.[effect_id].[slug]` | `effects.levitation.amp9_surge` / `effects.blindness.revelation_flash` |
| Velocity vectors | `effects.velocity.[slug]` | `.gentle_lift` / `.dramatic_launch` |
| Flight release configs | `effects.flight.release.[slug]` | `.slow_landing` / `.abrupt` |
| Particle layers | `effects.particle.[slug]` | `.ash_quiet` / `.arrival_sparkle` |

**Deferred:** PARTICLE_PATTERN, amplifier sweep in EFFECT_PATTERN, GIVE_ITEM, GIVE_XP.

---

### 📋 Fireworks — not yet walked

**Orientation confirmed (2026-04-05 department scan):**

- The existing spatial types (FIREWORK_CIRCLE, FIREWORK_LINE, etc.) are formally
  FIREWORK_PATTERN subtypes. They were always generative primitives (shape rules in,
  individual firework positions out) — now they carry the correct type name.
- This is a schema migration: type names in `fireworks.yml` and any show YAMLs that
  reference these types will need updating. Assess migration scope before walk.
- FIREWORK_PHRASE confirmed: salvos and volleys are explicitly authored burst sequences.
  Examples already documented in §12a.
- Both PATTERN and PHRASE apply cleanly to Fireworks from the gate.

**Walk pending.**

---

### 📋 Camera — not yet walked

**Orientation confirmed (2026-04-05 department scan):**

- CAMERA_PATTERN: PTZ (pan/tilt/zoom) start/end values — interpolated camera move.
  Defines start and end positions; engine generates the arc. Natural Pattern fit.
- CAMERA_PHRASE: explicitly authored sequence of camera positions (each position intentional).
- Both Pattern and Phrase apply. Full field set (what constitutes a "camera position" —
  pan, tilt, zoom values; anchor type; etc.) TBD during walk.

**Walk pending.**

---

### 📋 Voice — not yet walked

**Orientation confirmed (2026-04-05 department scan):**

- VOICE_PHRASE: the primary primitive for dropping text lines into a scene. Each step
  of the phrase is one text line element with: timing, location, color, intensity, duration.
- Scene editing mode required: ability to Add a New Line, Insert a line, Reorder lines.
  This is a meaningful UX surface — own walk required.
- VOICE_PATTERN: could handle repeating/pulsing text elements (flashing text, etc.).
- Both Pattern and Phrase apply.

**Walk pending.**

---

### 📋 Choreography — not yet walked

**Orientation confirmed (2026-04-05 department scan):**

- Works like Fireworks and Lightning: choreography events are anchored either to the
  scene center point or the player's current position (dual-anchor model, same as LIGHTNING).
- CHOREO_PATTERN and CHOREO_PHRASE both apply.
- Pattern: computed movement sweep (rules/endpoints in, movement events out).
- Phrase: explicitly authored sequence of positions/movements.

**Walk pending.**

---

## 12. Pattern Event Architecture

### ✅ Pattern is a YAML primitive

A Pattern is a first-class event type in the ScaenaShows YAML schema. It defines an
interpolated or pulsed sequence of events as a single authored unit. The engine expands
a Pattern to N individual events at show-load time (in EventParser via PatternExpander).
The human and Phase 2 editor work with the Pattern; the scheduler works with expanded events.

**Execution invariant preserved.** Patterns are a parse-time concept only. ShowScheduler,
ExecutorRegistry, and RunningShow never see Patterns — they receive only expanded individual
events. The production execution path is unchanged.

### ✅ Three behavioral modes

**Fade-type Pattern** (any param where `start ≠ end`): interpolates one or more params from
start to end across N steps. Examples: Sound simulated fade (volume), Lighting time
transition (time).

**Glissando-type Pattern** (pitch interpolated with `equal_temperament` curve): a fade-type
Pattern whose pitch param uses logarithmic (multiplicative) spacing so each step sounds like
an equal musical interval. Pitch sounds like an even glide up or down; arithmetic linear
spacing sounds uneven to the ear and should not be used for pitch. Volume may be
interpolated simultaneously (crescendo into or out of the glissando).

**Pulse-type Pattern** (all params with `start == end`): repeats a fixed config N times at
calibrated cycle timing. Example: Effects levitation patterns (HOVER, CLIMB, RELEASE).

### ✅ Pattern field set

`interpolated_param`, `start_value`, and `end_value` are replaced by an `interpolations:`
map, supporting simultaneous interpolation of any number of params. Each param entry
specifies its own `start`, `end`, and optional `curve`.

| Field | Required | Description |
|---|---|---|
| `interpolations` | Yes | Map of `param: {start, end, curve}` — one entry per interpolated param |
| `steps` | Yes | Number of events to distribute |
| `total_duration` | Yes | Total tick length of the Pattern |
| `curve` | No | Pattern-level default curve: `linear` (default) \| `ease_in` \| `ease_out` \| `equal_temperament` — overridden per-param |
| `step_duration` | No | For event types with internal duration (EFFECT): how long each step event lasts |
| `gap` | No | Ticks between step end and next step start (interval = step_duration + gap) |

**Per-param curve options:**

| Curve | Behavior | Use for |
|---|---|---|
| `linear` | Equal arithmetic steps (default) | Volume, amplifier, time |
| `ease_in` | Slow start, accelerating change | Swells, tension builds |
| `ease_out` | Fast start, decelerating change | Release, fading |
| `equal_temperament` | Multiplicative steps — equal musical intervals | Pitch only — sounds like a smooth glide |

**`equal_temperament` math:** PatternExpander multiplies the previous value by
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
type: SOUND_PATTERN
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
type: SOUND_PATTERN
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

**Simulated volume fade (original SOUND_PATTERN use case — unchanged):**

```yaml
type: SOUND_PATTERN
sound_id: minecraft:block.note_block.harp
steps: 6
total_duration: 120
interpolations:
  volume:
    start: 0.9
    end: 0.1
    curve: linear
```

### ✅ Phase 2 Pattern types

Three Pattern types ship in Phase 2:

| Type | Department | Interpolatable params | Primary use |
|---|---|---|---|
| `SOUND_PATTERN` | Sound | `pitch` (equal_temperament), `volume` (linear/ease) | Glissando, simulated fade, crescendo |
| `EFFECT_PATTERN` | Effects | `amplifier` | Levitation hover/climb/release cycles |
| `TIME_OF_DAY_PATTERN` | Lighting | `time` | Gradual time transition |

Additional Pattern types (PARTICLE_PATTERN, ENTITY_EFFECT_PATTERN, etc.) are deferred until a
concrete show need drives them.

### ✅ Levitation calibrated patterns migrate to EFFECT_PATTERN presets

The three calibrated levitation patterns move from KB documentation to named EFFECT_PATTERN
presets in `effect-configs.yml`:

| Preset ID | step_duration | gap | interval | Confirmed behavior |
|---|---|---|---|---|
| `effects.levitate.hover` | 20t | 8t | 28t | "gentle bubbling" — clean altitude hold |
| `effects.levitate.climb` | 24t | 0t | 24t | "separation from earth" — gradual drift |
| `effects.levitate.release` | 20t | 24t | 44t | "blood pressure release" — slow descent |

### ✅ Pattern in TechCueSession (Phase 2)

`TechCueSession.raw_yaml` stores Patterns in unexpanded form. Expansion happens fresh each
time `enterPreview()` is called. This is the load-bearing invariant: raw_yaml is always
what was authored; RunningShow is always what was expanded from it. If these diverge,
edits will be silently lost on the next preview cycle. TechManager must enforce this.

ShowYamlEditor gains a **5th operation:** `editPattern(patternYamlNode, PatternParams)` — mutates
Pattern fields in raw_yaml. Does not expand. Expansion happens at preview time.

### ✅ Pattern display in Phase 2 panel

Patterns appear as collapsed groups in the cue detail panel:

```
  tick 360: ambient_fade                          [Pattern ▾]
    SOUND_PATTERN  volume: 0.8 → 0.1  4 steps  40t  [Edit ▸]
```

`[Edit ▸]` on any step within a Pattern opens Pattern edit mode (not single-event edit).
Pattern edit mode shows step context: `Step 3 of 6 in pattern`. `[▶ Preview]` fires 1–2 cycles.

### ✅ Melodic content: PATTERN vs. PHRASE

The previous position ("melody/motif cues are NOT Patterns") is corrected. The model is now:

**A melody CAN be a Pattern** — when the pitch sequence is computed (interpolated across steps).
A glissando is the clearest example: define start pitch, end pitch, step count, and
`equal_temperament` curve; the engine generates every note. Gracie's whole-tone sweeps are
natural SOUND_PATTERNs. Any musical figure whose notes follow a smooth, calculable arc belongs here.

**A melody is a PHRASE** — when each note is explicitly authored and intentional. A specific
melodic line where note 3 is a deliberate D# and note 5 is an intentional rest is not
computable; it must be written out. PHRASEs are the in-game authoring primitive for this.
See §12a for the PHRASE specification.

**Existing `motif.*` and `gracie.*` cues** remain as-is — hand-authored sequences of SOUND
events in cue YAML. They are valid, complete, and do not need migration. PATTERN and PHRASE are
new authoring primitives; they don't replace working cues.

**The design question going forward:** when adding new melodic material to the library,
choose the right primitive:
- Interpolated sweep (glissando, chromatic scale, whole-tone run) → SOUND_PATTERN
- Specific authored line (riff, motif, melody) → PHRASE
- Repeating rhythmic figure (pulse, arpeggio with fixed pitches) → PHRASE with repeated steps

### Java layer sizing estimate (updated)

| Layer | Work | Rough size |
|---|---|---|
| Spec (spec.md Pattern section) | Documentation | 1 session |
| Model classes (PatternEvent + 3 subtypes) | Java | ~250 lines (multi-param interpolations map) |
| PatternExpander (expansion math + curves, incl. equal_temperament) | Java | ~300 lines |
| EventParser routing | Java | ~50 lines |
| ShowYamlEditor 5th operation | Java | ~150 lines |
| TechCuePanel Pattern display + edit | Java/UI | ~300 lines |
| PHRASE model + PhraseExpander (§12a) | Java | ~300 lines |
| PHRASE in TechCuePanel (step editor, beat grid) | Java/UI | ~400 lines |
| **Total** | | **~6–8 weeks** |

---

## 12a. PHRASE Event Architecture

### ✅ PHRASE is a YAML primitive — the explicit sequence counterpart to PATTERN

A PHRASE is a first-class event type in the ScaenaShows YAML schema. Where PATTERN computes
its steps (interpolated from start/end), PHRASE contains explicitly authored steps — each
event is intentional. The engine expands a PHRASE to individual events at show-load time,
the same way SPANs are expanded. Scheduler and executors see only the expanded events.

**The two primitives, distinguished:**

| | PATTERN | PHRASE |
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
| Effects (particle) | Pulse | Cluster | Phrase |
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

### ✅ PATTERN vs. PHRASE decision guide for melodic content

| Musical figure | Primitive | Reasoning |
|---|---|---|
| Harp glissando (ascending whole-tone sweep) | SOUND_PATTERN | Pitches are computed — equal_temperament curve |
| Chromatic scale passage | SOUND_PATTERN | Computed — linear semitone steps |
| Specific authored riff (each note intentional) | PHRASE | Not computable |
| Arpeggio with fixed pitches | PHRASE | Pitches are specific, not interpolated |
| Firework salvo (timed sequence of bursts) | PHRASE | Each burst explicitly placed |
| Levitation hover/climb/release | EFFECT_PATTERN | Repeating pulse — computed cycling |
| Volume fade | SOUND_PATTERN | Single param interpolated — no note pitch needed |
| Crescendo glissando (pitch + volume both change) | SOUND_PATTERN | Both params interpolated simultaneously |

### ✅ PHRASE in TechCueSession (Phase 2)

Same invariant as PATTERN: `TechCueSession.raw_yaml` stores PHRASEs in unexpanded form.
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

## 12b. MUSIC Event Type — Complete Specification

### ✅ Overview and relationship to SOUND

MUSIC is a first-class event type distinct from SOUND, confirmed 2026-04-05. Both live in the
Sound department. The distinction is architectural:

- **SOUND** — any Minecraft sound ID; pitch is a continuous multiplier (0.5–2.0) applied to
  the sound's natural pitch. General sound effects, ambient beds, point hits.
- **MUSIC** — one noteblock instrument for the whole event or sequence; pitch expressed in
  musical notation (note names: A4, F#3, C5) resolving to the chromatic scale. Produces
  actual musical intervals.

**Parallel consideration principle (locked 2026-04-05):** Changes to MUSIC type warrant
explicit consideration of SOUND for parallel applicability, and vice versa. Sister types.

MUSIC has four forms: `MUSIC` (single event), `MUSIC_PATTERN` (interpolated sweep),
`MUSIC_CYCLE` (arpeggiator / glissando), `MUSIC_PHRASE` (explicit authored sequence).

---

### ✅ Instrument shorthand

The `instrument:` field takes the last segment of the Minecraft note block sound ID.

| Shorthand | Resolves to | Register | Character |
|---|---|---|---|
| `harp` | `minecraft:block.note_block.harp` | Full range | Warm pluck — general purpose |
| `bell` | `minecraft:block.note_block.bell` | Mid-high | Ceremony, clarity, arrival |
| `flute` | `minecraft:block.note_block.flute` | Mid-high | Longing, pastoral, distance |
| `chime` | `minecraft:block.note_block.chime` | High | Wonder, delicacy, uncanny |
| `xylophone` | `minecraft:block.note_block.xylophone` | Mid-high | Joy, lightness, movement |
| `bit` | `minecraft:block.note_block.bit` | Full range | Digital, synthetic, retro |
| `pling` | `minecraft:block.note_block.pling` | Mid | Contemplative, jazz, interior |
| `iron_xylophone` | `minecraft:block.note_block.iron_xylophone` | Mid | Precise, mechanical |
| `guitar` | `minecraft:block.note_block.guitar` | Low-mid | Earthiness, folk, comfort |
| `banjo` | `minecraft:block.note_block.banjo` | Mid | Folk warmth, community |
| `bass` | `minecraft:block.note_block.bass` | Low | Foundation, grounding, weight |
| `didgeridoo` | `minecraft:block.note_block.didgeridoo` | Low | Earth, ancient, primal |
| `basedrum` | `minecraft:block.note_block.basedrum` | Percussion | Rhythmic downbeat |
| `snare` | `minecraft:block.note_block.snare` | Percussion | Accent, punctuation |
| `hat` | `minecraft:block.note_block.hat` | Percussion | Fine rhythmic texture |
| `cow_bell` | `minecraft:block.note_block.cow_bell` | Percussion | Quirky emphasis |

Percussion instruments (`basedrum`, `snare`, `hat`, `cow_bell`): `pitch:` is optional
(defaults to `1.0`). Pitch affects transient character but these are primarily textural.

---

### ✅ Pitch notation

The `pitch:` field accepts either:
- **Note name** (authoring default): `A4`, `F#3`, `C5`, `Bb4`, `D#4` — resolves via the
  chromatic scale table in `music-director.md §Pitch and Harmony Reference`
- **Float value** (compatibility / precision): `1.189` — used directly

Both forms are valid anywhere pitch appears in MUSIC types. Note names are preferred for
authoring clarity. The engine resolves names → floats at parse time.

**Two-octave range:** F#3 (0.500) to F#5 (2.000). 25 discrete semitone positions.

---

### ✅ Octave fold — universal behavior for all MUSIC types

When a computed pitch falls outside [0.5, 2.0], the engine folds it to the nearest
in-range octave: multiply or divide by 2 until in range. Note name is preserved; register
shifts. This is always-on — no field or flag. It is the correct behavior in every case.

**Register displacement:** When fold changes the interval relationship between two notes
(e.g., a fifth folds to a fourth), this is documented as "register displacement." It is
musically intentional when authoring near range boundaries. The spec should call it out;
the Phase 2 panel should show the actual folded pitch alongside the authored value.

Fold applies in: MUSIC single event (on authored pitch), MUSIC_PATTERN (on start/end before
interpolation), MUSIC_CYCLE (on each computed step), MUSIC_PHRASE (on authored step pitches
and exception harmony notes).

---

### ✅ MUSIC (single event)

```yaml
type: MUSIC
instrument: harp
pitch: A4                  # note name or float — resolves to 1.189
volume: 0.8
category: master           # same channel discipline as SOUND
max_duration_ticks: 40     # optional — hard cut, same behavior as SOUND
```

All fields parallel SOUND except `instrument:` replaces `sound_id:`, and `pitch:` accepts
note names. No other SOUND fields are removed or added.

---

### ✅ MUSIC_PATTERN (interpolated sweep)

Generates an interpolated series of MUSIC events from start to end pitch. The `equal_temperament`
curve — previously on SOUND_PATTERN — lives here. `equal_temperament` is the default and
recommended curve for any pitch interpolation in MUSIC_PATTERN; linear pitch spacing sounds
musically uneven and should not be used for pitch sweeps.

```yaml
type: MUSIC_PATTERN
instrument: harp
category: master
steps: 13
total_duration: 130
interpolations:
  pitch:
    start: F#3             # note name — resolves to 0.500
    end: F#5               # note name — resolves to 2.000
    curve: equal_temperament   # default for pitch; produces even musical intervals
  volume:
    start: 0.3
    end: 1.0
    curve: ease_in
```

SOUND_PATTERN retains `linear`, `ease_in`, `ease_out` for volume and arbitrary pitch
manipulation. `equal_temperament` is MUSIC_PATTERN only.

Field set: identical to SOUND_PATTERN (`interpolations:`, `steps:`, `total_duration:`,
`curve:`, `step_duration:`, `gap:`) plus `instrument:` replacing `sound_id:`, and note
names accepted in `interpolations.pitch.start` / `end`.

---

### ✅ MUSIC_CYCLE (arpeggiator / glissando)

Generates a repeating sequence of MUSIC events by cycling through a named or explicit
interval pattern from a declared root. Two rendering modes controlled by `harpify:`.

```yaml
type: MUSIC_CYCLE
instrument: harp
category: master
root: Ab3                  # transposable anchor — note name or float
pattern: maj6_9            # named preset or explicit interval array
harpify: true              # false = unique pitches only; true = full 7-string pedal tuning
cycles: 3                  # how many times the interval sequence repeats
step_duration: 15          # ticks per note
volume: 0.8                # scalar (constant) or envelope:
# volume:
#   start: 0.4
#   end: 0.9
#   curve: ease_in
```

**`pattern:`** accepts:
- A named preset string: `pattern: maj6_9`
- An explicit semitone interval array: `pattern: [0, 4, 7, 9]`
- Descending figures use negative intervals: `pattern: [0, -2, -4, -5]`

**`root:`** transposes the entire pattern. Change root, the pattern follows.

**`harpify: false`** (default) — unique pitch classes only. Suitable for any instrument.
Use for clean chord arpeggios, melodic figures, bass lines.

**`harpify: true`** — full 7-string-per-octave pedal tuning. The pattern resolves to
a complete harp tuning (one assignment per diatonic letter: C D E F G A B). Wherever
two adjacent strings produce the same pitch, that doubling fires in sequence — both steps
sound. Doublings are intentional texture. Works on any instrument; most idiomatic on harp.

**Volume envelope:** `volume:` accepts either a scalar (`0.8`) or an envelope map with
`start:`, `end:`, and `curve:` — same curves as MUSIC_PATTERN.

**Fold:** always applied to every computed step. Patterns that traverse the range boundary
wrap to the nearest in-range octave.

---

#### Named Pattern Library

Organized into four families. All patterns are root-relative semitone interval arrays.
Harpify behavior noted per family.

---

**Family 1 — Scales and Modes** (7 unique pitches per octave; harpify has no effect — all
7 strings produce distinct pitches; `harpify: true` = `harpify: false`)

| Pattern | Intervals | Modes / aliases |
|---|---|---|
| `major` | [0,2,4,5,7,9,11] | Ionian |
| `natural_minor` | [0,2,3,5,7,8,10] | Aeolian |
| `harmonic_minor` | [0,2,3,5,7,8,11] | Raised 7th — tension + resolution |
| `melodic_minor` | [0,2,3,5,7,9,11] | Ascending form — jazz, lyrical |
| `chromatic` | [0,1,2,3,4,5,6,7,8,9,10,11,12] | All semitones |
| `dorian` | [0,2,3,5,7,9,10] | Minor with bright 6th — jazz, ancient |
| `phrygian` | [0,1,3,5,7,8,10] | Dark, Spanish, Iberian gravity |
| `lydian` | [0,2,4,6,7,9,11] | Raised 4th — otherworldly, dreaming |
| `mixolydian` | [0,2,4,5,7,9,10] | Flat 7th — folk warmth, unresolved |
| `locrian` | [0,1,3,5,6,8,10] | Diminished root — unstable |

---

**Family 2 — Whole-Tone** (6 unique pitches; 7-string harp has exactly 1 doubling; two
harp variants based on where the doubling falls in the sweep)

`harpify: false` → use `whole_tone` (6 unique pitches, no doubling):

| Pattern | Intervals | Notes |
|---|---|---|
| `whole_tone` | [0,2,4,6,8,10] | Gracie's native scale — floating, directionless |

`harpify: true` → use a specific variant (doubling position is musically meaningful):

| Pattern | Harpified intervals | Doubling | Character |
|---|---|---|---|
| `whole_tone_bc` | [0,2,4,6,8,10,12] | B♯=C at top of sweep — pattern ends on repeated note | Cycle "lands" on the doubled pitch — rhythmic emphasis at phrase end |
| `whole_tone_bcb` | [0,0,2,4,6,8,10] | B=C♭ at bottom of sweep — pattern starts on repeated note | Cycle "launches" from the doubled pitch — rhythmic emphasis at phrase start |

Both presets produce the same 6 unique pitches. Difference is which string-pair creates
the doubling and therefore where in each cycle the repetition falls.

---

**Family 3 — Pentatonic** (5 unique pitches; harpify adds 2 doublings)

| Pattern | Harpify: false | Harpify: true | Doublings (harpified) |
|---|---|---|---|
| `pentatonic_major` | [0,2,4,7,9] | [0,2,4,4,7,9,12] | M3 (E/F♭) and root octave (C/B♯) |
| `pentatonic_minor` | [0,3,5,7,10] | [0,3,3,5,7,10,10] | m3 (D♯/E♭) and m7 (A♯/B♭) |

---

**Family 4 — Chord Arpeggios** (fewer than 7 pitches; harpify meaningful;
`harpify: false` is the natural default for these — use on any instrument)

| Pattern | Intervals (harpify: false) | Doublings (harpify: true) | Character |
|---|---|---|---|
| `major_triad` | [0,4,7] | root, M3, P5 each doubled | Bright, resolved |
| `minor_triad` | [0,3,7] | root, m3, P5 each doubled | Shadow, interiority |
| `sus2` | [0,2,7] | root, M2, P5 each doubled | Open, floating — no third |
| `sus4` | [0,5,7] | root, P4, P5 each doubled | Tension without resolution |
| `dom_7th` | [0,4,7,10] | doublings vary by voicing | Wants resolution |
| `major_7th` | [0,4,7,11] | doublings vary | Lush, complex rest |
| `minor_7th` | [0,3,7,10] | doublings vary | Dark-warm, jazz |
| `dim_triad` | [0,3,6] | doublings vary | Instability, dread |
| `aug_triad` | [0,4,8] | doublings vary | Uneasy symmetry |
| `six_nine` | [0,2,4,7,9] | doublings vary | Root+M2+M3+P5+M6 — open, no 4th or 7th |

---

**Family 5 — Motion Patterns** (short repeating figures; harpify generally not applicable)

| Pattern | Intervals | Character |
|---|---|---|
| `I_V` | [0,7] | Tonic-dominant bass pump |
| `major_arch` | [0,4,7,4,0] | Rise and return — major |
| `minor_arch` | [0,3,7,3,0] | Rise and return — minor |
| `do_ti_la_sol` | [0,-1,-3,-5] | Descending major — gravity, arrival |
| `pendulum` | [0,7,12,7] | Tonic-fifth-octave-fifth — open motion |
| `pedal` | [0,0,0,0] | Repeated root — insistence, pulse |

---

#### Harpify System — Full Specification

**`harpify: false`** (default):
- Engine uses the pattern's unique pitch class list only
- Each step fires once
- Works identically on any instrument

**`harpify: true`**:
- Engine resolves the pattern to a full 7-string-per-octave pedal tuning
- One diatonic letter (C D E F G A B) is assigned sharp, natural, or flat to match the scale
- Wherever two adjacent strings produce the same pitch (enharmonic pair), both steps fire
  in sequence — the doubled pitch plays twice
- Doublings are intentional texture, not redundancy — they create thickness, shimmer,
  and rhythmic emphasis depending on position in the cycle
- Works on any instrument. Most idiomatic on harp; creates emphasis/accent on others.

**Harp preset documentation format** — each `harpify: true` entry documents:
```
Pattern: harp name
Pedal:   C♮/♯/♭  D♮/♯/♭  E♮/♯/♭  F♮/♯/♭  G♮/♯/♭  A♮/♯/♭  B♮/♯/♭
Strings: [the 7 pitch names that result]
Harpified intervals: [full 7-position array including doublings]
Doublings: [which pitch classes appear twice and at which string pair]
Non-harpified intervals: [unique pitches only — same as harpify: false]
Character: [storytelling description]
```

**Key harp preset entries:**

```
Pattern: maj6_9  (harpify: true)
Pedal:   C♮  D♯  E♭  F♮  G♯  A♭  B♯
Strings: G#(=Ab), Ab, B#(=C), C, D#(=Eb), Eb, F
Harpified intervals: [0, 0, 4, 4, 7, 7, 9]  (from Ab root)
Doublings: root G#/Ab, M3 B#/C, P5 D#/Eb
Non-harpified: [0, 4, 7, 9]  (Ab major 6th chord)
Character: Lush, shimmering ambiguity. The defining harp voicing. Most idiomatic from Ab root.
           Three doublings create dense resonance. Sustained, unresolved, atmospheric.
```

```
Pattern: min6_9  (harpify: true)
Pedal:   C♮  D♯  E♭  F♮  G♯  A♭  B♮ (minor 3rd variant)
Strings: G#(=Ab), Ab, B(=Cb), Cb, D#(=Eb), Eb, F
Harpified intervals: [0, 0, 3, 3, 7, 7, 9]  (from Ab root)
Doublings: root, m3, P5
Non-harpified: [0, 3, 7, 9]
Character: Darker resonance. Modal depth. Interior, unresolved.
```

```
Pattern: pentatonic_major  (harpify: true)
Pedal:   C♮  D♮  E♮  F♭  G♮  A♮  B♯
Strings: C, D, E, Fb(=E), G, A, B#(=C)
Harpified intervals: [0, 2, 4, 4, 7, 9, 12]  (from C root; 12 = C one octave up)
Doublings: M3 E/Fb, root octave C/B#
Non-harpified: [0, 2, 4, 7, 9]
Character: Open, folk warmth. Two doublings create gentle emphasis without density.
```

```
Pattern: whole_tone_bc  (harpify: true)
Pedal:   C♮  D♮  E♮  F♯  G♯  A♯  B♯
Strings: C, D, E, F#, G#, A#, B#(=C)
Harpified intervals: [0, 2, 4, 6, 8, 10, 12]  (12 = C one octave up = same as root)
Doubling: B♯/C — root pitch appears at both ends of sweep
Non-harpified: [0, 2, 4, 6, 8, 10]
Character: Gracie's scale. Pattern ends where it started — cycle "lands" on the repeated
           pitch. Use for floating, self-completing gestures.
```

```
Pattern: whole_tone_bcb  (harpify: true)
Pedal:   C♭  D♮  E♮  F♯  G♯  A♯  B♮
Strings: Cb(=B), D, E, F#, G#, A#, B
Harpified intervals: [0, 0, 2, 4, 6, 8, 10]  (from Cb/B root; opens with doubling)
Doubling: B/Cb — root pitch appears at bottom of sweep, cycle launches from repeated pitch
Non-harpified: [0, 2, 4, 6, 8, 10]
Character: Same whole-tone content as bc; rhythmic accent falls at cycle start instead of end.
           Use when the gesture should feel like it launches rather than resolves.
```

---

### ✅ MUSIC_PHRASE (explicit authored sequence)

Single-instrument authored sequence. Simplifies step authoring — instrument and category
declared once at the top; steps need only pitch and volume.

```yaml
type: MUSIC_PHRASE
instrument: harp
category: master
tempo_bpm: 120
subdivision: 8
steps:
  - at_beat: 1.0
    pitch: A4              # shorthand — single note step
    volume: 0.8
  - at_beat: 1.5
    pitch: B4
    volume: 0.8
  - at_beat: 2.0           # chord step — events array for vertical grouping
    events:
      - {pitch: A4, volume: 0.7}
      - {pitch: C5, volume: 0.6}   # M3 above — minor third would be C5
      - {pitch: E5, volume: 0.5}   # P5 above
  - at_beat: 3.0           # exception harmony — single melody note + added interval
    events:
      - {pitch: C#5, volume: 0.8}  # melody note
      - {pitch: A4,  volume: 0.6}  # sixth below — exception harmony, manually added
```

**Shorthand form:** `pitch:` + `volume:` at the step level = syntactic sugar for a
single-event step. Any step can use the full `events:` array instead.

**Exception harmonies:** `events:` array accepts any number of notes. The instrument
declared at the top applies to all. This is how you add a doubled octave, sixth, or any
interval to a specific step of an otherwise single-line melody.

**Range constraint note:** Doubling at the octave is only safe if the root note is in the
lower register. Doubling A4 (1.189) at the octave would require A5 (pitch 2.378 —
out of range; folds to A4, producing unison rather than octave). Safe harmony intervals
in the upper register: sixth (9 semitones), fifth (7), third (3–4). The Phase 2 panel
should show a range indicator when an exception harmony is added.

**Fold on exception harmonies:** Same fold rules apply. Out-of-range notes fold to
nearest in-range octave. Panel shows the folded pitch.

**`tempo_bpm` and `subdivision`:** Same as generic PHRASE (§12a). `at_beat:` addresses
beat positions; `at:` in raw ticks if no tempo_bpm.

**Existing motif cues:** `motif.*` and `gracie.*` cues remain as-is — valid, tested, not
migrated automatically. MUSIC_PHRASE is the authoring primitive for new musical content
going forward. See OPS-035.

---

### ✅ Phase 2 edit panel (MUSIC types)

MUSIC edit modes follow the universal edit shell (§9). Instrument-specific affordances:

**MUSIC single event:**
- `instrument:` — panel list organized by register (Melodic Lead / Harmonic / Bass / Percussion)
- `pitch:` — note name display with keyboard-style reference; scroll wheel steps by semitone;
  actionbar shows note name + float value: `A4  (1.189)`
- `volume:`, `category:`, `max_duration_ticks:` — same controls as SOUND edit mode

**MUSIC_PATTERN:**
- `instrument:` — panel list
- `start:` / `end:` — note name scroll; `equal_temperament` pre-selected for pitch interpolations
- Other interpolation fields: same as SOUND_PATTERN panel

**MUSIC_CYCLE:**
- `instrument:` — panel list
- `root:` — note name scroll; shows current root in panel header
- `pattern:` — panel browser organized by family (Scales/Modes, Whole-Tone, Pentatonic,
  Chord Arpeggios, Motion Patterns); custom interval array via `[Enter custom →]` text GUI
- `harpify:` — toggle in panel: `Harpify: OFF  [Toggle]`; when ON, shows doubling summary:
  `Doublings: root, M3, P5 (3 notes doubled)`
- `cycles:`, `step_duration:` — scroll wheel
- `volume:` — scalar or envelope toggle

**MUSIC_PHRASE:**
- Beat grid display with note names at each step
- Exception harmony: `[+ Harmony]` button on any step opens note selector; added note
  shown with fold indicator if out of range
- Chord step shows all events with individual `[Edit]` / `[Remove]` per note; `[+ Add note]`

---

### 📋 OPS-035: Migration of motif.* and gracie.*

**Scope:** Re-author existing named cues in the `motif.*` and `gracie.*` namespaces as
MUSIC_PHRASE cues following the new naming convention. Update any show YAMLs that reference
old IDs.

**New naming convention:** `music.[instrument].[shape].[slug]`

| Old ID | New ID | Notes |
|---|---|---|
| `motif.arrival.bell` | `music.bell.rise.arrival` | 3-note rising step |
| `motif.unease.descend` | `music.bass.descent.unease` | 3-note chromatic descent |
| `motif.wonder.chime` | `music.chime.ascend.wonder` | 4-note ascending run |
| `motif.still.chord` | `music.pling.chord.still` | A minor chord, sustained |
| `motif.warmth.banjo` | `music.banjo.arch.warmth` | 5-note pentatonic arch |

Gracie's gestures move under `music.harp.*`. Gracie remains as a character concept in the
KB and production team docs; cue IDs no longer named after her.

**Prerequisite:** MUSIC type spec formally entered into `spec.md` (same prerequisite as ⚑6).
**Scope assessment:** 5 motif cues + Gracie's gesture library (~5–8 cues) + show YAML updates.
File as OPS-035 before migration begins.

---

### Java layer sizing estimate (MUSIC types)

| Layer | Work | Rough size |
|---|---|---|
| MusicEvent model | Java | ~80 lines |
| InstrumentResolver (shorthand → sound_id) | Java | ~50 lines |
| PitchResolver (note name → float + fold) | Java | ~80 lines |
| MusicPatternEvent model | Java | ~100 lines |
| MusicCycleEvent model | Java | ~150 lines |
| MusicCycleExpander (pattern → steps, harpify, fold) | Java | ~250 lines |
| Named pattern registry (all 5 families) | Java | ~150 lines |
| MusicPhraseEvent model | Java | ~120 lines |
| PhraseExpander extensions for MUSIC_PHRASE | Java | ~80 lines |
| PatternExpander extensions for MUSIC_PATTERN | Java | ~50 lines |
| Phase 2 panel: all MUSIC edit modes | Java/UI | ~350 lines |
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
| ⚑ 1 | Edit target: show YAML only vs. also loading cues/*.yml — Pattern reinforces "cue file loaded" model: if a Pattern lives in a cue file, TechCueSession must have that file loaded to edit Pattern params | Yes — before ShowYamlEditor Java |
| ⚑ 2 | Q4 (partial YAML / scaffold handling): what does Phase 2 do when a CUE reference can't resolve at preview time? What is the minimum viable YAML to enter Phase 2? | Yes — before TechCueSession Java |
| ⚑ 3 | Panel design / mockup: full Phase 2 panel with all modes and states including Pattern display | Yes — before Java |
| ⚑ 4 | Department walk incomplete: Fireworks, Camera, Voice, Choreography edit modes not yet defined. Orientation context captured for all four (2026-04-05). Effects locked 2026-04-05. Fireworks is next. | Yes — before building spec is final |
| ⚑ 5 | Preset library file structure: formal location and format for each department's preset file | Yes — before ShowYamlEditor Java |
| ⚑ 6 | Pattern schema spec section: field definitions, expansion rules, and validation for SOUND_PATTERN, EFFECT_PATTERN, TIME_OF_DAY_PATTERN must be written into spec.md before any Pattern Java work | Yes — prerequisite for all Pattern Java |
| ✅ 7 | Pattern type list confirmed (2026-04-05): SOUND_PATTERN, EFFECT_PATTERN, TIME_OF_DAY_PATTERN ship in Phase 2. MUSIC_PATTERN added as new type pending spec (⚑17). Fireworks types confirmed as FIREWORK_PATTERN subtypes (⚑19). | Closed |
| ⚑ 8 | Cross-plugin text input UI pattern: text GUI (not anvil) established as preference during Sound walk — needs formal design decision for how text input modal works across all departments | Yes — before any department Java that requires string input |
| 📋 9 | OPS item for universal preset library: file as separate ticket once department walk complete | No |
| 📋 10 | Auto-name fallback logic: per-department inference rules when slug is absent | No — slug is required; fallback is a safety net only |
| 📋 11 | Leather color palette: define the curated named color list for Wardrobe | No — design asset, not blocking |
| 📋 12 | OPS-033 (display noise cleanup): still blocked on Phase 2 architecture decision — that decision is now made (extend TechSession). OPS-033 Part B can proceed. | No |
| ✅ 13 | MELODY_PATTERN concept superseded (2026-04-05): melodic content is now covered by two primitives. SOUND_PATTERN with `equal_temperament` curve handles interpolated pitch sequences (glissandi, whole-tone sweeps). PHRASE (§12a) handles explicitly authored note sequences. No standalone MELODY_PATTERN type needed. | Closed |
| ✅ 14 | `world_preview` param: default confirmed as `LIVE` (2026-04-05). In-scene editing shows reality by default; toggle available contextually to suppress when needed. | Closed |
| 📋 15 | OPS-034 (player-anchored LIGHTNING): Java capability gap filed 2026-04-05. Player anchor presets can be authored and saved in Phase 2; they require OPS-034 to fire correctly in production. Not blocking Phase 2 panel work. | No |
| ✅ 16 | SPAN → PATTERN rename confirmed (2026-04-05). Find/replace complete throughout this doc. Building spec and Java model names to be updated when Java work begins. PATTERN and PHRASE are the two generative primitives. Fireworks spatial types are FIREWORK_PATTERN subtypes. | Closed |
| ⚑ 17 | MUSIC event type spec: Design locked in session doc (§12b, 2026-04-05). Covers MUSIC, MUSIC_PATTERN, MUSIC_CYCLE (harpify toggle, named pattern library, 5 families), MUSIC_PHRASE (exception harmonies, fold). OPS-035 migration scoped. Remaining: formal entry into spec.md (same prerequisite as ⚑6 — Pattern schema section). | Yes — spec.md entry before any Music Java work |
| 📋 23 | OPS-035: Migration of motif.* and gracie.* to MUSIC_PHRASE format. New naming convention: music.[instrument].[shape].[slug]. ~10–13 cues to re-author. Prerequisite: MUSIC spec in spec.md. Scope assessment in §12b. | No — after spec.md |
| ✅ 18 | Effects PHRASE container vocabulary word (closed 2026-04-05): Effects vocab redefined to align with universal model. Pulse (single event) / Cluster (vertical grouping) / Phrase (EFFECT_PHRASE container). "Pattern" correctly refers to the EFFECT_PATTERN type only — no collision. | Closed |
| 📋 19 | Fireworks schema migration: FIREWORK_CIRCLE, FIREWORK_LINE, etc. are confirmed as FIREWORK_PATTERN subtypes (2026-04-05). Type names in `fireworks.yml` and show YAMLs need updating. Assess migration scope before Fireworks walk. | No — not blocking walk, but must be done before Java |
| 📋 20 | Camera walk: orientation captured (2026-04-05). CAMERA_PATTERN (PTZ start/end interpolation) and CAMERA_PHRASE (authored position sequence) confirmed. Full field set TBD. | No |
| 📋 21 | Voice walk: orientation captured (2026-04-05). VOICE_PHRASE (lines as steps with timing, location, color, intensity, duration) confirmed. Scene editing mode (Add/Insert/Reorder lines) scoped. | No |
| 📋 22 | Choreography walk: orientation captured (2026-04-05). Dual-anchor model (scene_origin / player). CHOREO_PATTERN and CHOREO_PHRASE confirmed. | No |

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

*Session paused 2026-04-05 — account transition. Sound locked. Lighting locked. Pattern
architecture updated (multi-param, equal_temperament, glissando). PHRASE primitive added.
OPS-034 filed. PATTERN rename confirmed (⚑16 closed). MUSIC confirmed as new event type
(⚑17 opened). Department orientation scan complete for all remaining departments. Effects
(Felix) locked 2026-04-05. Remaining: Fireworks, Camera, Voice, Choreography.*
