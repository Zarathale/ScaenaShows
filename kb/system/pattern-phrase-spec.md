---
document: Pattern and Phrase Architecture — Phase 2
status: Locked (2026-04-05/06)
source: Extracted from ops-029-design-session-2026-04-05.md (2026-04-06)
scope: PATTERN and PHRASE YAML primitives, field sets, expansion model, Tempo Architecture
---

# Pattern and Phrase Architecture

These are the two generative YAML primitives introduced in Phase 2. Both expand to individual
events at parse time. Schedulers and executors never see unexpanded content.

**PATTERN** — computes its steps (interpolated from start/end endpoints or pulsed at fixed cadence).
**PHRASE** — contains explicitly authored steps; each event is intentional.

For MUSIC-specific uses of PATTERN and PHRASE (MUSIC_PATTERN, HARP_SWEEP, melodic phrases), see
`music-event-types.md`. For how Patterns and Phrases are edited in the Phase 2 panel, see
`phase2-department-panels.md` §Pattern/Phrase display and §department edit modes.

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
| `duration_ticks` | No | For event types with internal duration (EFFECT): how long each step event lasts |
| `cycle_ticks` | No | Ticks from one pulse start to the next. Implicit gap = `cycle_ticks - duration_ticks`. If `cycle_ticks < duration_ticks`, pulses overlap (engine warns at author time). |

**Per-param curve options:**

| Curve | Behavior | Use for | Available on |
|---|---|---|---|
| `linear` | Equal arithmetic steps (default) | Volume, amplifier, time | All Pattern types |
| `ease_in` | Slow start, accelerating change | Swells, tension builds | All Pattern types |
| `ease_out` | Fast start, decelerating change | Release, fading | All Pattern types |
| `equal_temperament` | Multiplicative steps — equal musical intervals | Pitch only — sounds like a smooth glide | **MUSIC_PATTERN only** |

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
type: MUSIC_PATTERN          # equal_temperament is MUSIC_PATTERN only
instrument: harp
steps: 13                    # 13 whole-tone steps across 2 octaves
total_duration: 130          # 10 ticks per note (~120 BPM eighth note feel)
interpolations:
  pitch:
    start: F#3               # note name — resolves to 0.5
    end: F#5                 # note name — resolves to 2.0
    curve: equal_temperament
  volume:
    start: 0.9
    end: 0.9                 # constant — omit for same effect
    curve: linear
```

**Crescendo glissando — pitch rises while volume builds:**

```yaml
type: MUSIC_PATTERN
instrument: harp
steps: 13
total_duration: 130
interpolations:
  pitch:
    start: F#3
    end: F#5
    curve: equal_temperament
  volume:
    start: 0.3
    end: 1.0
    curve: ease_in           # volume accelerates into the peak
```

**Simulated volume fade (SOUND_PATTERN — volume interpolation, no pitch):**

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
| `SOUND_PATTERN` | Sound | `volume` (linear/ease), `pitch` (linear/ease — arbitrary effect only) | Simulated fade, volume swell, pitch shift effect |
| `EFFECT_PATTERN` | Effects | `amplifier` | Levitation hover/climb/release cycles |
| `TIME_OF_DAY_PATTERN` | Lighting | `time` | Gradual time transition |

Additional Pattern types (PARTICLE_PATTERN, ENTITY_EFFECT_PATTERN, etc.) are deferred until a
concrete show need drives them.

### ✅ Levitation calibrated patterns migrate to EFFECT_PATTERN presets

The three calibrated levitation patterns move from KB documentation to named EFFECT_PATTERN
presets in `effect-configs.yml`:

| Preset ID | duration_ticks | cycle_ticks | Sensation |
|---|---|---|---|
| `effects.levitation.hover` | 20t | 28t | "gentle bubbling" — clean altitude hold |
| `effects.levitation.climb` | 24t | 24t | "separation from earth" — gradual drift |
| `effects.levitation.release` | 20t | 44t | "blood pressure release" — slow descent |

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
| Spec (cue-show-yaml-schema.md Pattern section) | Documentation | 1 session |
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
| `tempo_bpm` | No | Enables beat-based step addressing (`at_beat:`). Converted to `ticks_per_quarter` internally — may approximate for non-anchor BPM values. See §12c. |
| `ticks_per_quarter` | No | Alternative to `tempo_bpm`. Always exact — no conversion rounding. Mutually exclusive with `tempo_bpm`; takes precedence if both present. |
| `subdivision` | No | Smallest rhythmic unit available in the beat grid: `quarter` \| `eighth` (default) \| `sixteenth`. Names a rhythmic value — not a tick count. The actual tick count per subdivision is determined by `ticks_per_quarter`. Subdivisions that produce fractional ticks are unavailable in the Phase 2 panel. |
| `instrument` | No | Phrase-level default instrument for MUSIC step shorthand. When set, steps may use `pitch:` + `volume:` directly rather than a full `events:` array. The shorthand resolves as `{type: MUSIC, instrument: [value], pitch: ..., volume: ...}`. Any step can override with a full `events:` array mixing any dept. |
| `category` | No | Phrase-level default audio category; inherited by all shorthand MUSIC steps. |
| `anchor` | No | Spatial anchor for event types that support it (e.g. Fireworks, Choreography). Phrase-level; per-step override not available on anchor — set at invocation time. |
| `steps` | Yes | Ordered list of step entries |

**Step entry fields:**

| Field | Required | Description |
|---|---|---|
| `at:` | Yes (if no tempo_bpm) | Step offset in ticks from PHRASE start |
| `at_beat:` | Yes (if tempo_bpm set) | Step offset in beats (e.g., `1.5` = eighth note after beat 1) |
| `events:` | Yes | Array of one or more event configs — multiple = vertical grouping |

### ✅ Tempo, subdivision, and tick math

See §12c for the full Tempo Architecture: preferred anchors, subdivision constraints,
loop integrity rules, and PATTERN quantization.

When `tempo_bpm` is present, PhraseExpander converts `at_beat:` positions to ticks:
`tick = (at_beat - 1) × (1200 / tempo_bpm)`. Result is rounded to nearest whole tick.
Use `ticks_per_quarter:` instead for exact conversion with no rounding.

| ticks_per_quarter | BPM | Quarter | Eighth | Sixteenth | Triplet (eighth) |
|---|---|---|---|---|---|
| 20 | 60 | 20t | 10t | 5t | — |
| 12 | 100 | 12t | 6t | 3t | 4t ✅ |
| 10 | 120 | 10t | 5t | 2.5t ⚠️ | — |
| 8 | 150 | 8t | 4t | 2t | — |

Note: at fast tempos, sixteenth notes approach the 1t minimum. The Minecraft tick is the
hard floor. Subdivisions marked ⚠️ produce fractional ticks and are unavailable in the
Phase 2 beat-grid editor.

`subdivision` constrains the Phase 2 beat-grid editor — in-game step placement snaps to
the available positions for that subdivision. At `subdivision: eighth`, beat positions of `1.25`
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
subdivision: eighth
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
      - {type: FIREWORK, preset: fireworks.burst.gold.high}
  - at_beat: 2.0         # volley — simultaneous bursts
    events:
      - {type: FIREWORK, preset: fireworks.burst.gold.high}
      - {type: FIREWORK, preset: fireworks.burst.silver.mid}
      - {type: FIREWORK, preset: fireworks.burst.red.low}
  - at_beat: 3.0
    events:
      - {type: FIREWORK, preset: fireworks.burst.white.finale}
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


---

## 12c. Tempo Architecture

### ✅ Guiding principle: tick-first design

Minecraft's simulation clock runs at exactly 20 ticks per second. All timing in
ScaenaShows — events, patterns, phrases, loops — resolves to whole tick offsets.
BPM is a derived, human-readable label, not the primary quantity.

**Design for coherence over precision:** A tempo anchor that produces perfect integer
subdivisions will feel better in play than a mathematically precise BPM that forces
fractional tick rounding throughout. Subdivision integrity is more important than BPM
accuracy.

### ✅ Primary conversion relationship

```
ticks_per_quarter = 1200 / BPM
BPM ≈ 1200 / ticks_per_quarter
```

Ticks are integers. BPM is frequently irrational. Tick-first design means choosing
`ticks_per_quarter` first, then accepting the resulting BPM as the "musical equivalent."

### ✅ Preferred tempo anchors

These values produce the most usable rhythmic systems and are the default options
offered in Phase 2 tempo pickers:

| ticks_per_quarter | Approx BPM | Character |
|---|---|---|
| 24 | 50 | Spacious, atmospheric |
| 20 | 60 | Slow, ceremonial |
| 16 | 75 | Lyrical, flowing |
| 15 | 80 | Moderate, flexible |
| 12 | 100 | Highly versatile — recommended default |
| 10 | 120 | Energetic, standard |
| 8 | 150 | Bright, rhythmic clarity |
| 6 | 200 | Pulse-driven, grid-heavy |

**Strong recommendation:** Default to 12, 16, or 8 ticks per quarter when designing
reusable systems. 12 is the most versatile — it cleanly supports all common subdivisions
including triplets.

### ✅ Subdivision constraint — integer ticks required

All rhythmic subdivisions must resolve to integer ticks to remain stable and repeatable.
Any subdivision that produces a fractional tick must be either rounded (introduces drift)
or avoided (preferred).

**12 ticks/quarter — the gold standard system:**

| Note value | Ticks | Status |
|---|---|---|
| Whole | 48 | exact |
| Half | 24 | exact |
| Quarter | 12 | exact |
| Eighth | 6 | exact |
| Sixteenth | 3 | exact |
| Dotted quarter | 18 | exact |
| Dotted eighth | 9 | exact |
| Triplet (quarter) | 8 | exact |
| Triplet (eighth) | 4 | exact |

12 ticks/quarter supports every common rhythmic structure cleanly, including triplets.
It is the only standard anchor where triplets produce exact integers.

**10 ticks/quarter — less stable:**

| Note value | Ticks | Status |
|---|---|---|
| Eighth | 5 | exact |
| Sixteenth | 2.5 | unusable |
| Dotted eighth | 7.5 | unusable |
| Triplets | non-integer | unusable |

When a non-anchor BPM is specified via `tempo_bpm:`, the Phase 2 panel shows the
computed `ticks_per_quarter` value and flags any subdivisions that would produce
fractional ticks as unavailable in the beat grid.

### ✅ `ticks_per_quarter` as a PHRASE field (alternative to `tempo_bpm`)

PHRASE currently uses `tempo_bpm:` for beat-based addressing. `ticks_per_quarter:` is
added as an alternative field. When specified, it bypasses BPM conversion entirely —
tick math is exact, no rounding. The two fields are mutually exclusive; `ticks_per_quarter`
takes precedence if both are present (error logged).

```yaml
# BPM form — familiar, may approximate
type: PHRASE
instrument: harp       # phrase-level default for shorthand steps
tempo_bpm: 100         # internally converts to 12 ticks/quarter — exact here
subdivision: eighth

# Tick-first form — always exact
type: PHRASE
instrument: harp
ticks_per_quarter: 12  # declared directly — no conversion, no rounding
subdivision: eighth
```

The Phase 2 panel always shows both representations: the entry field (BPM or
ticks_per_quarter) and the computed equivalent. Switching entry mode is a panel toggle.

### ✅ PATTERN quantization rule

When PatternExpander generates N events over `total_duration` ticks, step spacing is:

```
step_spacing = total_duration / (steps - 1)
```

This must be an integer. If fractional, PatternExpander rounds each step tick to the
nearest whole tick — small drift accumulates at the phrase end. The Phase 2 panel
warns when this condition is detected:

```
