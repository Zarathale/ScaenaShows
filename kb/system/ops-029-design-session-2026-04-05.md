---
document: OPS-029 Phase 2 Design Session Notes
date: 2026-04-05
status: Working document — architecture locked; 3 blocking items before Java
scope: Tech Rehearsal Mode Phase 2 architecture, department edit modes, preset library
---

# OPS-029 Phase 2 Design Session — 2026-04-05

All architecture and department panel design decisions are locked. Three blocking items remain
before Java begins. This document covers the session architecture (§1–10). Detailed content
has been extracted to standalone docs:

- **Department panel specs** → `phase2-department-panels.md`
- **PATTERN / PHRASE / Tempo architecture** → `pattern-phrase-spec.md`
- **MUSIC event types, HARP_SWEEP, pattern library** → `music-event-types.md`

---

## Where We Left Off

**Status (2026-04-06):** All blocking items resolved. Java can begin.

⚑1 ✅ **Edit target** — Show YAML only. See §4.
⚑2 ✅ **Partial YAML / scaffold handling** — Phase 2 enters fine with stubs. See §4b.
⚑3 ✅ **Panel mockup** — Locked. See §4c.

**Next action:** Java begins on Phase 2. Start with `TechCueSession` and `ShowYamlEditor`.

**Completed this session (summary):**
All 10 departments walked and locked (2026-04-05). PATTERN / PHRASE primitives designed.
MUSIC event type family fully specced including MUSIC_CYCLE named pattern library, harpify
system, and HARP_SWEEP articulation primitive. Tempo Architecture locked (tick-first).
PHRASE unified as single schema type (`type: PHRASE`). `equal_temperament` curve confirmed
as MUSIC_PATTERN only. Text input: text GUI not anvil. Auto-preview: session-level toggle.
OPS-034 through OPS-045 filed. ⚑16 ⚑18 ⚑29 ⚑4 ⚑5 ⚑6 ⚑7 ⚑8 ⚑17 all closed.

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

### ✅ Edit target — Show YAML only

`TechCueSession` holds only the show YAML. All edits write to the show's own timeline
entries. `[Save as Preset]` is the explicit step that promotes a configuration to
`cues/*.yml`. One file in memory. No silent cross-show mutations.

---

## 4b. Scaffold / Stub Handling

### ✅ Phase 2 enters fine with stubs

A CUE reference pointing to a stub cue (valid file, empty or minimal content) does not
block Phase 2 entry. The stub loads; the panel displays it like any other cue.

**Missing cue files** (no file at all in `cues/*.yml`) fail at show load time per the
existing architectural decision — Phase 2 cannot be entered if the show won't load.
This case is already handled and requires no additional Phase 2 logic.

### ✅ Stub visual treatment

Stub cues render with a dim gray `(empty)` label inline next to the cue ID in the panel.
`[Edit ▸]` is still present — authoring content into a stub via Phase 2, then promoting
with `[Save as Preset]`, is a valid and intentional workflow.

---

## 4c. Panel Mockup

### ✅ Phase 2 cue panel — locked design

Standard scene view:

```
─────────────────────────────────────────────
  TECH REHEARSAL — Scene 2: The Approach
  [◀ Prev Scene]                [Next Scene ▶]
─────────────────────────────────────────────

  6s | 120t  scouting_begins
    casting.zombie.scout_enter              [Edit ▸]

  10s | 200t  weather_shifts
    lighting.overcast.dread_builds          [Edit ▸]
    sound.wind.low_moan                     [Edit ▸]

  17s | 340t  warrior_enters_scene
    casting.zombie.warrior_enter            [Edit ▸]
    wardrobe.diamond.battle_ready           [Edit ▸]

  24s | 480t  victory_coda  (empty)
    effects.levitate.high.stub              [Edit ▸]

─────────────────────────────────────────────
  ▶ Now: 10s | 200t   Next: 17s | 340t
─────────────────────────────────────────────
```

End-of-scene pause point:

```
─────────────────────────────────────────────
  End of Scene 2.
  [Exit Preview]              [Next Scene ▶]
─────────────────────────────────────────────
```

### ✅ Time/tick display format — universal convention

Everywhere a position, marker, or tick is shown to the player, use: `Xs | Nt`

- Drop the decimal when the seconds value is a clean integer: `6s | 120t`
- Retain the decimal only when fractional seconds are meaningful: `6.25s | 125t`
- Applies to: panel cue rows, position indicator, actionbar nudge feedback,
  snap offers, "After [cue]" relative references, and any other player-facing
  tick or time display throughout Phase 2.

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


---

## 11. Department Edit Modes

**→ Extracted to `phase2-department-panels.md`**

All 10 department panel specs live there: Set, Casting, Wardrobe, Sound, Lighting, Effects,
Fireworks, Camera, Voice, Choreography. That document is the authoritative Phase 2 panel
design reference. The universal edit shell (boss bar, save/cancel, hotbar, auto-preview,
world preview, text input) is documented in §9 of this file.


---

## 12. Pattern / PHRASE / Tempo Architecture

**→ Extracted to `pattern-phrase-spec.md`**

Covers: PATTERN field set and behavioral modes (fade, glissando, pulse); PHRASE field set
and timing model; vertical grouping (events array); department PHRASE vocabulary; Tempo
Architecture (tick-first, preferred anchors, subdivision constraints, loop integrity).

---

## 12b / 12d. MUSIC Event Types and HARP_SWEEP

**→ Extracted to `music-event-types.md`**

Covers: MUSIC, MUSIC_PATTERN, MUSIC_CYCLE (with full named pattern library and harpify
system); PHRASE with instrument shorthand for melodic authoring; HARP_SWEEP articulation
primitive (technique library, phase 2 panel); Phase 2 edit panels for all MUSIC types;
OPS-044 (cue migration) and OPS-045 (HARP_SWEEP Java).

