---
document: Phase 2 Department Panel Specifications
status: Locked — all 10 departments complete
source: Extracted from ops-029-design-session-2026-04-05.md (2026-04-06)
scope: Per-department Phase 2 edit mode panel design: fields, interaction model, presets, auto-preview policy
---

# Phase 2 Department Panel Specifications

All 10 departments locked (2026-04-05). This document is the authoritative Phase 2 panel
design reference. For the universal shell that wraps every department (boss bar, save/cancel
buttons, hotbar behaviour, world preview, text input policy, auto-preview toggle), see
§9 of `ops-029-design-session-2026-04-05.md`.

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

**`[▶ Preview]`** — applies the effect to the designer for duration_ticks. Auto-preview
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
type: PHRASE
audience: participants
tempo_bpm: 120           # optional — enables at_beat: addressing
subdivision: eighth      # optional
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

### ✅ Fireworks — locked (2026-04-05)

**Orientation confirmed (2026-04-05 department scan):**

- The existing spatial types (FIREWORK_CIRCLE, FIREWORK_LINE, etc.) are formally
  FIREWORK_PATTERN subtypes. They were always generative primitives (shape rules in,
  individual firework positions out) — now they carry the correct type name.
- This is a schema migration: type names in `fireworks.yml` and any show YAMLs that
  reference these types will need updating. Assess migration scope before walk.
- FIREWORK_PHRASE confirmed: salvos and volleys are explicitly authored burst sequences.
  Examples already documented in §12a.
- Both PATTERN and PHRASE apply cleanly to Fireworks from the gate.

**Orientation confirmed (2026-04-05):**

#### ✅ Dual-anchor model

All FIREWORK event types carry `anchor: scene_origin | player`. Field name matches LIGHTNING (§11).

- **Default: `scene_origin`** — offset computed from stage mark. Standard authoring posture for
  all show-level pyrotechnics. Most fireworks shows are staged relative to a fixed world position.
- **`player`** — offset computed from player's position at cue invocation time. Static; does not
  interpolate per-tick. Architectural decision: anchor locked at invocation, same as all other
  player-anchored events. Use for one-offs where the burst should originate near wherever the
  player is standing when the cue fires.
- **FIREWORK_PHRASE**: anchor lives at phrase level. All steps (volleys) share the single locked
  origin resolved at invocation. No per-step anchor override.
- **Panel**: when `anchor: player` is selected, panel shows OPS-034 dependency warning.
  Authoring and saving is fully valid; live execution requires OPS-034 to resolve correctly.
- **Java gap**: player-anchor fireworks require the same capability as OPS-034 (player position
  resolved at invocation time). Not a new gap — OPS-034 dependency. (See item 24.)

```yaml
# FIREWORK (single) — player-anchored one-off
type: FIREWORK
anchor: player          # default: scene_origin
preset: scae_star_warm
offset: {x: 0, y: 2, z: 0}
y_mode: relative

# FIREWORK_CIRCLE — stage-anchored (typical show use)
type: FIREWORK_CIRCLE
anchor: scene_origin    # explicit; this is the default
preset: scae_large_fanfare
radius: 8
count: 8
origin_offset: {x: 0, z: 0}
y_mode: relative
y_offset: 0

# FIREWORK_PHRASE (salvo) — anchor at phrase level
type: PHRASE
anchor: player          # all volleys share this locked origin
tempo_bpm: 120
subdivision: quarter
steps:
  - at_beat: 1.0
    events:
      - {type: FIREWORK, preset: scae_star_warm}
  - at_beat: 2.0
    events:
      - {type: FIREWORK, preset: scae_burst_leaf}
      - {type: FIREWORK, preset: scae_star_warm}
```

#### ✅ Panel design

**FIREWORK (single) — point panel**

Simplest Fireworks panel. Fields: anchor selector, preset picker (single ID from
fireworks.yml), offset {x / y / z} as three numeric fields, y_mode dropdown
(relative | surface). No power_variation, no color_variation — those are PATTERN
features. Auto-preview ON: fires the rocket once on demand.

**FIREWORK_PATTERN — subtype panel with selector**

Subtype selector at top: `CIRCLE | LINE | RANDOM`. (FAN deferred — possible Phase 3
addition. Note in panel as unavailable.) Panel body changes per subtype. Common header
across all three:

- anchor (scene_origin | player — OPS-034 warning if player)
- y_mode (relative | surface)
- y_offset (numeric)
- power_variation (UNIFORM | RAMP_UP | RAMP_DOWN | ALTERNATE | RANDOM)
- color_variation (UNIFORM | RAINBOW | GRADIENT | ALTERNATE)
  - when GRADIENT selected: gradient_from + gradient_to hex pickers appear

Subtype-specific fields below the common header:

| Field | CIRCLE | LINE | RANDOM |
|---|---|---|---|
| radius | ✅ | — | ✅ |
| count | ✅ | ✅ | ✅ |
| origin_offset {x, z} | ✅ | — | ✅ |
| start_offset {x, z} | — | ✅ | — |
| angle (compass bearing) | — | ✅ | — |
| length | — | ✅ | — |
| chase (enabled / interval_ticks / direction) | ✅ | ✅ | — |
| y_variation | — | — | ✅ (new) |
| preset (single) | ✅ | ✅ | ✅ |
| presets (pool list) | — | — | ✅ (new) |
| seed | — | — | ✅ |

**Known-gap warning on LINE + GRADIENT:** when subtype is LINE and color_variation
is GRADIENT, panel displays an inline warning: "gradient_from/to not currently applied
on LINE — always defaults to red→blue (ops-inbox gap)." Authoring still permitted;
warning is informational.

**FIREWORK_RANDOM — two new fields (Java work required):**

`y_variation` — randomizes each rocket's Y within a range. Floor is `y_offset`;
ceiling is `y_offset + y_variation`. Rockets spawn at random heights in that band.
Flat behavior (current) is y_variation absent or 0.

```yaml
y_offset: 2
y_variation: 4     # rockets spawn between Y+2 and Y+6
```

`presets` (pool) — list of preset IDs; one drawn at random per rocket. Mutually
exclusive with `preset` (single); if `presets:` is present, it wins. `color_variation`
stacks on top — applies per-rocket after the preset is drawn from the pool, overriding
that preset's primary colors per the normal color_variation rules.

```yaml
# pool + color_variation stacked
presets:
  - scae_star_warm
  - bday_confetti_ball
  - pride_burst_rainbow
color_variation: RAINBOW    # overrides primary colors of whichever preset is drawn
```

Panel behavior: `preset` and `presets` are mutually exclusive selector modes — toggle
or radio (single preset | preset pool). Pool mode shows a multi-select list of
fireworks.yml entries; single mode shows a single picker. `color_variation` row always
visible regardless of mode; stacking behavior is the runtime rule, no warning needed.

Both `y_variation` and `presets` pool are new Java fields — not currently implemented
in `FireworkEventExecutor`. Filed in ops-inbox (items below). Phase 2 panel authors
and saves valid YAML; execution of these fields requires Java work.

**FIREWORK_PHRASE (salvo) — step-list panel**

Same model as EFFECT_PHRASE. Anchor at phrase level (top of panel). Steps addressed
by beat (tempo_bpm + subdivision). Each step is a volley — a list of FIREWORK events
firing simultaneously. Panel rows: add step, add event to step, reorder steps.
Individual FIREWORK events within a step reference a single preset (no pool per
step-event — pool is a RANDOM-mode feature only).

Auto-preview ON: fires the phrase from the beginning on demand.

#### ✅ Named FIREWORK_PATTERN presets — all three subtypes

CIRCLE, LINE, and RANDOM all get named configuration presets. Presets capture the
*character* of the arrangement — spatial density, sequencing, variation. The show event
provides placement and context. All three are reusable across shows once you separate
those concerns.

**Field split — what belongs in the preset vs. the show event:**

| Field | CIRCLE | LINE | RANDOM |
|---|---|---|---|
| radius | preset | — | preset |
| count | preset | preset | preset |
| length | — | preset | — |
| chase (enabled / interval_ticks / direction) | preset | preset | — |
| power_variation | preset | preset | preset |
| color_variation | preset | preset | preset |
| y_mode | preset | preset | preset |
| y_offset | preset | preset | preset |
| y_variation | — | — | preset |
| origin_offset {x, z} | show event | — | show event |
| start_offset {x, z} | — | show event | — |
| angle | — | show event | — |
| anchor | show event | show event | show event |
| rocket appearance preset | show event | show event | show event |
| gradient_from / gradient_to | show event | show event | — |
| presets pool | — | — | show event |
| seed | — | — | show event |

`anchor` is always show-level — it's a deployment decision, not a character decision.
A "victory crown" isn't inherently `scene_origin` or `player`.

Rocket appearance preset (`preset: id`) stays show-level — the pattern preset defines
the shape and energy of the arrangement; which rocket fires is a separate creative call.

**Preset naming:** `fireworks.circle.[slug]`, `fireworks.line.[slug]`,
`fireworks.random.[slug]` — distinct from rocket appearance presets in `fireworks.yml`
(which retain their existing IDs or get migrated separately).

**Examples:**

```yaml
# fireworks.circle.victory_crown
id: fireworks.circle.victory_crown
radius: 8
count: 8
chase:
  enabled: true
  interval_ticks: 3
  direction: FL
power_variation: UNIFORM
color_variation: UNIFORM
y_mode: relative
y_offset: 0

# fireworks.line.slow_sweep
id: fireworks.line.slow_sweep
count: 6
length: 14
chase:
  enabled: true
  interval_ticks: 5
  direction: FL
power_variation: RAMP_UP
color_variation: UNIFORM
y_mode: relative
y_offset: 1

# fireworks.random.celebration_scatter
id: fireworks.random.celebration_scatter
count: 12
radius: 6
y_variation: 3
power_variation: RANDOM
color_variation: UNIFORM
y_mode: surface
y_offset: 0
```

These presets live in `fireworks.yml` alongside rocket appearance presets (⚑5 closed — cue-show-yaml-schema.md §22).
They are distinct entries: rocket appearance presets and FIREWORK_PATTERN presets coexist in the
same file, distinguished by their field sets.

#### ✅ Auto-preview

ON for all Fireworks event types — FIREWORK (single), all FIREWORK_PATTERN subtypes,
FIREWORK_PHRASE. Consistent with all other departments. Rocket travel time before burst
is a player expectation, not a panel design issue. No special handling.

---

**Summary of locked decisions:**
- Dual-anchor model: `anchor: scene_origin | player`, default `scene_origin`
- FIREWORK_PHRASE anchor is phrase-level; no per-step override
- Player-anchor panel shows OPS-034 dependency warning (item 24)
- Panel: FIREWORK (single) = point panel; FIREWORK_PATTERN = subtype selector shell
  (CIRCLE | LINE | RANDOM; FAN deferred); FIREWORK_PHRASE = step-list panel
- power_variation + color_variation on CIRCLE, LINE, and RANDOM
- LINE + GRADIENT shows known-gap warning in panel
- FIREWORK_RANDOM: `y_variation` (floor → floor+variation) — OPS-035
- FIREWORK_RANDOM: `presets` pool, stacks with `color_variation` — OPS-036
- Pool is RANDOM-mode only; step-events in FIREWORK_PHRASE use single preset
- Named FIREWORK_PATTERN presets: all three subtypes (CIRCLE, LINE, RANDOM)
  — presets capture character; show event provides placement + rocket appearance
  — naming: `fireworks.circle.[slug]`, `fireworks.line.[slug]`, `fireworks.random.[slug]`
- Auto-preview ON across all Fireworks event types

---

### ✅ Camera — locked (2026-04-05)

**Show-Relative Spatial Vocabulary (cross-department — Camera and Choreography):**

All show-relative directions resolve at runtime from the origin mark's stored facing direction
(yaw captured at snap time). This is a new requirement: the origin mark must store yaw in
addition to position (x, y, z). Other marks remain pure positions.

| Term | Definition |
|------|------------|
| Show forward | The direction the show designer faces when snapping the origin mark |
| Behind | Opposite of show forward |
| Stage Left | Performer's left when facing the audience (= House Right) |
| Stage Right | Performer's right when facing the audience (= House Left) |
| House Left | Audience's left when facing the stage (= Stage Right) |
| House Right | Audience's right when facing the stage (= Stage Left) |

Clock positions (12, 3, 6, 9 and intermediates) are valid shorthand: Show forward = 12,
Behind = 6, Stage Right = 3, Stage Left = 9.

**FACE — ✅ Locked**

Two authorship patterns with distinct panel behavior (department implicit — not shown to user):

- Camera-authored FACE: always targets player. Target not shown in panel.
- Choreo-authored FACE: always targets a non-player entity. Target shown as read-only label.

`look_at` picker offers three buckets: show-relative shortcuts, scene marks, spawned entities
in current scene. Defaults differ by department:

- Camera defaults: Right 90°, Left 90°, Show forward, Behind
- Choreo defaults: Stage Left, Stage Right

Both departments have access to the full mark picker and entity picker.

No FACE presets — FACE calls are too show-specific. Camera's preset library lives with
CAMERA_PATTERN and CAMERA_PHRASE.

**CAMERA_LOCK / MOVEMENT_LOCK — ✅ Locked**

Two independent cross-department show-state flags on RunningShow. Either can be set or
released by any department. Stop-safety always resets both to unlocked at show or
tech rehearsal end, regardless of current state.

```yaml
type: CAMERA_LOCK
state: ON | OFF

type: MOVEMENT_LOCK
state: ON | OFF
```

Camera typically authors CAMERA_LOCK. Effects typically authors MOVEMENT_LOCK (e.g., during
levitation). Authorship is intentionally open — any department can use either. Phase 2 panel
for any event can include lock state as a visible indicator when relevant.

**Conditional Primitive Pattern — ✅ Locked (first conditionals in the engine)**

A conditional primitive evaluates show state at fire-time and branches on the result. This is
the first conditional execution model in ScaenaShows — everything prior fires unconditionally.

General pattern:
- **Condition** — what to check (position radius, angular deviation; extensible to other state)
- **Tolerance** — dead zone; no action fires while within tolerance
- **Corrective branch** — fires when condition is out of tolerance
- **Optional in-tolerance branch** — fires when condition is within tolerance; omit for "do nothing"

Designed as bespoke for Camera but intentionally structured for extension to other departments.

*BOUNDARY_CHECK — position-based conditional:*

```yaml
type: BOUNDARY_CHECK
center: mark:stage_center    # or explicit xyz
radius: 10                   # blocks
out_of_range:
  - type: PLAYER_TELEPORT
    destination: mark:stage_center
    audience: participants
in_range:
  # optional — omit if no in-range behavior needed
```

*VIEW_CHECK — orientation-based conditional:*

```yaml
type: VIEW_CHECK
target: mark:center          # or entity:spawned:Name
tolerance: 30                # degrees of angular deviation allowed before triggering
out_of_view:
  duration_ticks: 20
  interpolation: EASE_OUT
in_view:
  # optional — omit if no in-view behavior needed
```

VIEW_CHECK corrective action is **always a smooth pan, never a snap.** This is a hard
constraint, not a style preference — a snap correction is worse than no correction at all.
Duration and interpolation are the tuning parameters; destination is always computed from
`target` at fire time.

Tolerance is expressed in degrees of angular deviation. Not as a percentage of FOV — FOV is
player-configurable in Minecraft, so degree-based tolerance gives consistent behavior
regardless of client settings.

**PLAYER_SPECTATE / PLAYER_SPECTATE_END — ✅ Locked**

PLAYER_SPECTATE is a self-contained cinematic primitive. Entity lifecycle is folded in via
an optional `spawn:` block. Two mutually exclusive modes:

- **`spawn:` mode** — entity is born at spectate time, invisible by default (always implied
  for camera drones), optionally despawned when END fires.
- **`entity:` mode** — references an entity already present in the scene.

```yaml
type: PLAYER_SPECTATE

# Mode A: spawn new
spawn:
  name: CinematicCamera
  type: ARMOR_STAND
  offset: {x: 10, y: 5, z: 0}
  despawn_on_end: true        # entity despawns when matching PLAYER_SPECTATE_END fires

# Mode B: use existing
entity: entity:spawned:GuideEntity

audience: participants
```

After PLAYER_SPECTATE fires, `entity:spawned:CinematicCamera` is available for CROSS_TO
calls in the same cue — the drone pattern is: PLAYER_SPECTATE (spawn + attach) →
CROSS_TO (path) → PLAYER_SPECTATE_END (land + despawn).

**Duration shortcut:** `duration_ticks` is a Phase 2 authoring convenience only — not
stored in YAML. When filled in on the PLAYER_SPECTATE panel, Phase 2 auto-creates a
matching PLAYER_SPECTATE_END cue at `(spectate_tick + duration_ticks)`. The generated
END cue is a real cue in the show YAML, editable like any other. If left blank, the
END cue is placed manually. PLAYER_SPECTATE always requires a corresponding
PLAYER_SPECTATE_END — the duration field is the shortcut to create it, not a substitute.

**Phase 2 panel:**

```
Camera entity:
  ○ Use existing:  [entity:spawned:... ▾]
  ● Spawn new:     Name: [CinematicCamera     ]
                   Type: [ARMOR_STAND         ▾]
                   Offset: x[10] y[5] z[0]
                   Despawn on end: [✓]

Audience: participants  [Change ▾]

Auto-create END cue: [  60  ] ticks  (leave blank to place manually)

[▶ Preview]  [Save]  [Save as Preset]  [Cancel]
```

Preview (spawn mode): spawns entity at offset, makes invisible, attaches spectate.
No pre-spawning required. Preview (entity mode): requires entity already present —
panel shows warning if not.

**PLAYER_SPECTATE_END — destination field:**

```yaml
type: PLAYER_SPECTATE_END
audience: participants
destination: restore                      # default: return to pre-spectate position
# destination: mark:near_stage_center     # teleport to a defined mark
# destination: entity:spawned:CinematicCamera  # teleport to drone's current position
```

Three destination options:
- `restore` — default. Player body returns to where it was when spectate began.
- `mark:Name` — teleport to a defined mark. Author this for planned scene arrivals.
- `entity:spawned:Name` — teleport to wherever the drone is at END time. The "cinematic
  arrival" pattern: drone flies to destination, END fires, player materializes where drone
  is, drone despawns. Java order of operations: record entity position → teleport player →
  despawn entity (prevents race between teleport and despawn).

**Phase 2 panel (END):**

```
Return player to:
  ● Restore pre-spectate position
  ○ Mark: [... ▾]
  ○ Entity position: [entity:spawned:... ▾]

Audience: participants  [Change ▾]
[Save]  [Cancel]
```

No preview on END — it returns the player to their body, which happens naturally on save.
No presets for PLAYER_SPECTATE or PLAYER_SPECTATE_END — entity names are show-specific.

**PLAYER_MOUNT / PLAYER_DISMOUNT — ✅ Locked**

Same spawn: fold as PLAYER_SPECTATE. Key differences: player retains game mode (no
SPECTATOR switch), player orientation is player-controlled for the duration of the mount.
Typical entity types: HORSE, BOAT, MINECART, STRIDER. Armor stands are inconsistent
as mounts — prefer rideable entity types.

```yaml
type: PLAYER_MOUNT

# Mode A: spawn new
spawn:
  name: GuideEntity
  type: HORSE
  offset: {x: 0, y: 0, z: 0}
  invisible: false           # optional — default false (unlike camera drones, mounts
                             # are usually visible)
  despawn_on_dismount: true

# Mode B: use existing
entity: entity:spawned:GuideEntity

audience: participants
```

**Duration shortcut:** Same model as PLAYER_SPECTATE — fill in ticks to auto-create
a PLAYER_DISMOUNT cue at `(mount_tick + duration_ticks)`. Not stored in YAML. Leave
blank to place the DISMOUNT cue manually.

**PLAYER_DISMOUNT:** Player lands where the entity is at dismount time — default
behavior, no destination field. Just `audience:`. No preview needed.

```yaml
type: PLAYER_DISMOUNT
audience: participants
```

**Phase 2 panel (MOUNT):**

```
Mount entity:
  ○ Use existing:  [entity:spawned:... ▾]
  ● Spawn new:     Name: [GuideEntity       ]
                   Type: [HORSE             ▾]
                   Offset: x[0] y[0] z[0]
                   Invisible: [ ]
                   Despawn on dismount: [✓]

Audience: participants  [Change ▾]

Auto-create DISMOUNT cue: [  80  ] ticks  (leave blank to place manually)

[▶ Preview]  [Save]  [Save as Preset]  [Cancel]
```

**Phase 2 panel (DISMOUNT):** Audience selector only. No preview, no destination.

**KB note (camera.kb.md):** While mounted, Camera orientation calls (FACE, VIEW_CHECK,
orientation-only PLAYER_TELEPORT) are ineffective — player looks around freely.
Mark should not plan assertive camera calls during a mount sequence. If a specific
initial facing is needed, author a FACE call in the ticks immediately before the mount,
not during it.

No presets for PLAYER_MOUNT or PLAYER_DISMOUNT — entity names are show-specific.

**Camera walk — ✅ Complete (2026-04-05)**

**CAMERA_PHRASE — ✅ Locked**

Phase 2 confirmed. PHRASE primitive applied to Camera's event set. Department ownership: Phase 2 panel offers Camera events in the step editor; Camera presets populate the preset picker.

```yaml
type: PHRASE
audience: participants
tempo_bpm: 120
subdivision: quarter    # quarter | eighth | sixteenth

steps:
  - at_beat: 1.0
    events:
      - type: FACE
        target: player
        look_at: mark:stage_center

  - at_beat: 2.0
    events:
      - type: ROTATE
        target: player
        delta_pitch: -40.0    # tilt up (OPS-040 — pending)
        duration_ticks: 20

  - at_beat: 3.0
    events:
      - type: CAMERA
        effect: nausea
        intensity: 1
        duration_ticks: 30

  - at_beat: 4.0
    events:
      - type: CAMERA
        effect: darkness
        audience: participants
      - type: FACE
        target: player
        look_at: mark:act_two_center

  - at_beat: 6.0
    events:
      - type: CAMERA
        effect: darkness_return
        audience: participants
```

Event durations run independently — phrase continues stepping while effects play out. `darkness` / `darkness_return` pairing is author responsibility; panel prompts for the return step when `darkness` is added.

Named presets: `camera.phrase.reveal_tilt`, `camera.phrase.entrance_redirect`, `camera.phrase.transition_blackout`, `camera.phrase.battle_sweep`, `camera.phrase.unease`.

**Resolved during walk:**
- CAMERA screen effects ✅ Locked: nausea / darkness / blindness / levitation / slow_falling. darkness uses matched-pair contract (darkness + darkness_return). OPS-039 filed for snow blindness exploration.
- CAMERA_PATTERN ✅ Dead: ROTATE (OPS-005, 2.26.0) covers smooth pan. Pitch extension filed as OPS-040. No CAMERA_PATTERN type needed.
- camera.kb.md ✅ Updated: ROTATE marked verified; OPS-040 gap noted; CAMERA effects row corrected.

---

### ✅ Voice — Locked (2026-04-05)

**Single event panels — ✅ Locked**

All four instruments confirmed. Panels: text field (text GUI, not anvil), audience picker,
format-specific fields, preview, save-as-preset for all four.

- MESSAGE: text + audience. Presets: `[Sprite]` prefix format variants.
- TITLE: title + subtitle (optional) + fade_in / stay / fade_out timing. Presets: timing configs.
- ACTION_BAR: text + duration_ticks (plugin re-sends every 20t to persist). Presets: yes.
- BOSSBAR: title + color + overlay + duration_ticks + fade_in_ticks + fade_out_ticks + audience.
  Presets: color/overlay/timing combinations.

**BOSSBAR — what the Java actually does (from source):**
- Always starts at 0 (hardcoded), fills to 1.0 during fade_in_ticks, holds, empties during
  fade_out_ticks, then hides.
- `duration_ticks` = total life of the bar. Hold phase = duration - fade_in - fade_out.
- No start_progress, end_progress, or freeze capability. OPS-043 filed.
- darken_sky → OPS-041 (Lighting). create_fog → OPS-042 (Effects). Not in BOSSBAR panel.
- BOSS_HEALTH_BAR: separate event type (OPS-026, shipped); entity-linked live HP bar.
  Noted in voice.kb.md capability summary.

**VOICE_PHRASE — ✅ Locked**

Primary Phase 2 authoring surface for Voice. Script editor model, not beat sequencer.

```yaml
type: PHRASE
audience: participants      # phrase-level default; per-step can override

steps:
  - at: 0
    events:
      - type: BOSSBAR
        title: "<gold>Preparing for Battle</gold>"
        color: YELLOW
        overlay: PROGRESS
        duration_ticks: 400

  - after: 40               # relative: N ticks after previous step
    events:
      - type: MESSAGE
        message: "<light_purple>[Sprite]</light_purple><white> The ceiling opens.</white>"

  - after: 80
    events:
      - type: MESSAGE
        message: "<light_purple>[Sprite]</light_purple><white> Something has been waiting.</white>"

  - after: 60
    events:
      - type: TITLE
        title: "<gold><bold>You are here.</bold></gold>"
        fade_in: 20
        stay: 60
        fade_out: 20
```

Timing: `at:` (absolute tick) and `after:` (relative to previous step) coexist — author
picks per step. `after:` is the natural authoring mode for dialogue that needs to breathe.

Phase 2 panel — script editor:
```
VOICE PHRASE  [ + Add Line ]

  1.  [at: 0     ]  BOSSBAR    "Preparing for Battle"         [Edit] [↑] [↓] [✕]
  2.  [after: 40 ]  MESSAGE    "[Sprite] The ceiling opens."  [Edit] [↑] [↓] [✕]
  3.  [after: 80 ]  MESSAGE    "[Sprite] Something has been…" [Edit] [↑] [↓] [✕]
  4.  [after: 60 ]  TITLE      "You are here."                [Edit] [↑] [↓] [✕]

  [ Insert Line Above ▾ ]

[▶ Preview Phrase]  [Save]  [Save as Preset]  [Cancel]
```

Clicking Edit opens the individual event panel inline. ↑/↓ reorders. Named presets:
`voice.phrase.sprite_intro`, `voice.phrase.revelation`, `voice.phrase.section_close`.

**VOICE_PATTERN — ✅ Deferred**

No clear Phase 2 use case. No meaningful param to interpolate for text. Deferred until
a concrete show need surfaces it.

**KB updates completed:**
- voice.kb.md: TITLE_CLEAR corrected from gap → ✅ Verified (OPS-016, shipped)
- voice.kb.md: BOSSBAR progress gap row added (OPS-043)
- voice.kb.md: BOSS_HEALTH_BAR row added to capability summary
- voice.kb.md: Stage Manager cross-department note updated (no more workaround reference)

---

### ✅ Choreography — locked (2026-04-05)

**Orientation confirmed (2026-04-05 department scan):**
- Dual-anchor model (scene_origin / player) confirmed — per-action field, not session-level default.
- CHOREO_PATTERN and CHOREO_PHRASE both apply.

**Honest assessment of mob movement (established during walk):**
- INSTANT CROSS = teleport. Reliable, precise, works on puppets (AI off).
- AI CROSS = pathfinder walk. Entity navigates toward mark using Bukkit pathfinder. Uncontrolled
  path, uncontrolled timing, no arrival guarantee. No callback when entity reaches destination —
  show has no awareness of arrival. Entity resumes default AI behavior on arrival.
- No smooth interpolated mob movement exists. Imprecision can be used creatively (see calibration).
- Player CROSS_TO = tick-exact smooth interpolation. Fully controlled and reliable.

**Panel taxonomy locked:**

| Panel name | Mode / notes |
|---|---|
| ENTRANCE | Appear (SPAWN_ENTITY at mark) / Arrive (ENTER from wing → pathfinds to destination) |
| CHARACTER EXIT | Exit (path to wing, despawn on arrival) / Vanish (immediate despawn, optional particle burst) |
| CHARACTER CROSS | Instant (teleport) / AI (pathfinder, includes speed field). Anchor field on both. |
| CHARACTER LOOK | Compound panel: FACE (snap yaw, pitch gapped for entities) → ENTITY_AI off → resume after N ticks. Target: mark / character / player / compass / yaw value. |
| PERFORMER STATE | AI toggle (Puppet / Performer). HOLD note in panel: "For sustained stillness, use Puppet. Hold is for momentary pauses only." |
| CHARACTER VELOCITY | Standalone. Target + vector (x/y/z) + named presets: gentle bounce / dramatic launch / float arc. Anchor field. |

**AI = freeze. No separate freeze toggle needed.** AI off = puppet = still. HOLD only zeroes
velocity at a single tick; AI-on entity resumes pathfinding immediately after.

**AI CROSS speed named presets:**
- Creep (0.05–0.1), Slow (0.2–0.3), Normal (1.0), Fast (1.5–2.0), Sprint (2.5+)
- Speed set before cross fires. Persists until changed. To change speed alone, write another
  CROSS to same destination at new speed (instant mode, just updates the speed attribute).

**CAPTURE/RELEASE:** Out of scope for Phase 2. Author-time YAML only.

**The honest core of Choreography:** Choreography is a mark management and pathfinding system.
The creative work is: which entity, which mark, when, what state. Everything else serves that.

**Mark types — two kinds, both supported:**
- Named marks: fixed world positions from Set's mark table. `mark:stage_left`, `mark:center`.
- Relative marks: inline offset from anchor. "3 blocks that way." Syntax: `{x: 3, z: 0}`.
  Already present in CROSS_TO. Phase 2 panel exposes an offset picker (x/z fields, y optional)
  alongside the named mark picker. Which is used more in practice — deferred to calibration.

**CHOREO_PATTERN:** In scope. Concept: compute a set of mark positions from a geometric rule
(circle, line, grid, arc) — rules in, positions out. Entities use those computed marks as
destinations via AI CROSS or ENTRANCE. Consistent with SOUND_PATTERN, EFFECT_PATTERN model.
Specific field set deferred to calibration — in-game formation experiments will determine
which geometric configurations are worth computing vs. placing named marks manually.

**PHRASE container field set — locked (⚑29 closed):**
```yaml
type: PHRASE
name: "phrase_id"           # required
anchor: scene_origin        # optional — scene_origin | player
ticks_per_quarter: 12       # optional — or tempo_bpm; inherits from show if absent
steps:
  - at: 0                   # tick offset or beat reference (beat 2, beat 2.5)
    events:
      - type: [EVENT_TYPE]  # any event type, any department
        [fields...]
```
Step builder UI: dept picker (10 options) → event type picker → panel. Multiple event slots
per step via vertical grouping. Change-dept action on any slot (fields clear on change).
Phase 2 entry points pre-select dept for first step; no restriction on subsequent steps.

**Calibration backlog:** `formation.rotate.clockwise` added to choreography.kb.md. N entities
at geometric marks, all AI CROSS to next mark clockwise simultaneously. Also: side-by-side
movement test with two entities to adjacent marks.

---

