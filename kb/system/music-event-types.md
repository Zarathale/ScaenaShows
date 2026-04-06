---
document: MUSIC Event Types — Complete Specification
status: Design complete (2026-04-05/06) — pending formal entry into cue-show-yaml-schema.md (⚑6)
source: Extracted from ops-029-design-session-2026-04-05.md (2026-04-06)
scope: MUSIC, MUSIC_PATTERN, MUSIC_CYCLE, HARP_SWEEP; instrument shorthand; pitch notation;
       octave fold; named pattern library (5 families); harpify system; Phase 2 panels
---

# MUSIC Event Types

MUSIC is a first-class event family in the Sound department, distinct from SOUND.
SOUND handles any Minecraft sound ID with a continuous pitch multiplier.
MUSIC handles noteblock instruments with pitch expressed in musical notation.

**Parallel consideration principle (locked 2026-04-05):** Changes to MUSIC warrant checking
SOUND for applicability, and vice versa. Sister types.

**Migration:** Existing `motif.*` and `gracie.*` cues remain as-is. Migration to PHRASE with
instrument shorthand is tracked under OPS-044.

**Prerequisites for Java work:** MUSIC type formally entered into cue-show-yaml-schema.md (⚑6 prerequisite).

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
  chromatic scale table in `sound.music-director.md §Pitch and Harmony Reference`
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
| `maj6_9` | [0,4,7,9] | root, M3, P5 each doubled | Major 6th chord — lush, shimmering, unresolved. The defining harp voicing; most idiomatic from Ab root |
| `min6_9` | [0,3,7,9] | root, m3, P5 each doubled | Minor 6th chord — darker resonance, modal depth, interior and unresolved |
| `six_nine` | [0,2,4,7,9] | doublings vary | Root+M2+M3+P5+M6 — five-voice open voicing; no 4th or 7th |

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
type: PHRASE
instrument: harp           # phrase-level default — steps using pitch/volume shorthand inherit this
category: master           # phrase-level default for all MUSIC steps
tempo_bpm: 120
subdivision: eighth
steps:
  - at_beat: 1.0
    pitch: A4              # shorthand — single note step; inherits instrument + category from above
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
single-event MUSIC step. Requires `instrument:` declared at the phrase level. Any step can
use the full `events:` array instead, mixing any dept event types at that step.

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
migrated automatically. PHRASE with instrument shorthand is the authoring primitive for new
musical content going forward. See OPS-044.

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

### 📋 OPS-044: Migration of motif.* and gracie.*

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

**Prerequisite:** MUSIC type spec formally entered into `cue-show-yaml-schema.md` (same prerequisite as ⚑6).
**Scope assessment:** 5 motif cues + Gracie's gesture library (~5–8 cues) + show YAML updates.
OPS-044 filed (2026-04-06).

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

## 12d. HARP_SWEEP — Harp Articulation Primitive

### ✅ Overview

`HARP_SWEEP` is a distinct MUSIC event type for harpists — it models the physical gesture of
fingers sweeping across a tuned set of strings, not just an arpeggio cycling through intervals.
Where `MUSIC_CYCLE` defines *what harmonic pattern* cycles, `HARP_SWEEP` defines *how a harpist
moves through the strings*: direction, speed profile, touch weight, note overlap.

The two types are complementary, not redundant:

| | MUSIC_CYCLE | HARP_SWEEP |
|---|---|---|
| Concept | Arpeggio pattern cycling | Physical string sweep gesture |
| Primary param | Pattern interval array | Technique (motion + dynamics) |
| Speed | Fixed `step_duration` | Variable — shaped by speed profile |
| Use for | Chord arpeggios, rhythmic figures | Glissandi, broadway sweeps, bisbigliando |

### ✅ Field set

```yaml
type: HARP_SWEEP
root: Ab3                    # transposable anchor — note name or float
pattern: maj6_9              # any named pattern or explicit interval array — defines the tuning
harpify: true                # resolve to full 7-string pedal tuning (recommended: true for sweeps)
range_low: Ab3               # optional — lowest string to include; defaults to full pattern range
range_high: F#5              # optional — highest string to include
technique: broadway_finale   # named preset (see library below) OR inline block
cycles: 1                    # how many complete passes through the range
volume: 0.9                  # scalar or envelope {start, end, curve} — overall level
category: master
audience: participants
```

**Inline technique block** (when no named preset fits):

```yaml
technique:
  direction: up_down         # ascending | descending | up_down | down_up | bisbigliando
  speed: accelerando         # steady | accelerando | ritardando | swell
  weight: heavy              # light | medium | heavy
  overlap_ticks: 0           # ticks a note rings before release; 0 = clean gliss; 3+ = blur
```

**Field notes:**
- `direction: up_down` — ascending sweep, then descending sweep, as one continuous gesture
- `direction: bisbigliando` — rapid back-and-forth, very short range, notes heavily overlapping; `overlap_ticks` should be 3–6t
- `speed: swell` — slow start, accelerates to midpoint, decelerates back (the classical big harp arc)
- `weight` — governs the dynamic shape *within a single pass*, independent of `volume`: heavy = notes swell and ring; light = notes are brief and even; medium = standard gliss character
- `overlap_ticks` — how long each note continues to ring before the next string fires its note. Distinct from `step_duration` in MUSIC_CYCLE; the engine computes per-note durations from technique + speed profile
- `range_low` / `range_high` can be set to explore only a portion of the tuning — a low-register rumble or a high-register shimmer without full-range traversal

### ✅ Named Technique Library

| Technique | Direction | Speed | Weight | Overlap | Character |
|---|---|---|---|---|---|
| `broadway_sweep` | ascending | accelerando | heavy | 0t | The big ascending pull — energy builds as fingers move up |
| `broadway_pull` | descending | ritardando | heavy | 0t | Pulling back down — gravity and resolution after the peak |
| `broadway_finale` | up_down | swell | heavy | 0t | The complete arc — ascends with acceleration, sweeps back with breadth |
| `bisbigliando` | up_down | steady | light | 4t | Rapid back-and-forth shimmer; notes blur and overlap; trembling texture |
| `glissando_legato` | ascending | steady | medium | 2t | Clean ascending gliss; notes just touch before releasing |
| `glissando_staccato` | ascending | steady | light | 0t | Clean ascending gliss; notes are crisp and separate |
| `soft_whisper` | ascending | ritardando | light | 0t | Slow, delicate ascent; very light touch; nearly inaudible at top |
| `thunder_pull` | descending | ritardando | heavy | 0t | Heavy descending gliss slowing to a deep resolution |
| `shimmer_flutter` | up_down | steady | light | 3t | Fast, soft oscillation over a narrow range; atmospheric shimmer |

### ✅ Named presets (full sweeps)

Named HARP_SWEEP presets capture a complete configuration: tuning + range + technique + dynamics.
Naming convention: `harp.sweep.[technique_family].[slug]`

Examples:

```yaml
# harp.sweep.broadway.victory_pull
id: harp.sweep.broadway.victory_pull
root: Ab3
pattern: maj6_9
harpify: true
technique: broadway_finale
cycles: 1
volume: {start: 0.5, end: 1.0, curve: ease_in}

# harp.sweep.bisbigliando.shimmer_above
id: harp.sweep.bisbigliando.shimmer_above
root: F#4
pattern: whole_tone_bc
harpify: true
range_low: F#4
range_high: F#5
technique: bisbigliando
cycles: 3
volume: 0.6

# harp.sweep.whisper.opening_mist
id: harp.sweep.whisper.opening_mist
root: C4
pattern: pentatonic_major
harpify: true
range_low: C4
range_high: A5
technique: soft_whisper
cycles: 1
volume: 0.4
```

### ✅ Phase 2 panel

```
HARP SWEEP

  Tuning
    Pattern: [ maj6_9         ▾ ]   Root: [ Ab3 ]   Harpify: [✓]

  Range
    Low: [ Ab3 ]   High: [ F#5 ]   [ Full range ]

  Technique
    [ broadway_finale         ▾ ]   (or [ Custom... ])
    → Direction: up_down   Speed: swell   Weight: heavy   Overlap: 0t
    (shown as read-only descriptors when a named technique is selected;
     editable fields when Custom is selected)

  Dynamics
    Volume: [ 0.9 ]   Cycles: [ 1 ]

  [▶ Preview]  [Save]  [Save as Preset]  [Cancel]
```

When `[▶ Preview]` fires, the engine runs the full sweep once at the designer's current
position. Cycles constrain the preview to 1 regardless of authored value — the sound of one
pass is sufficient for dialing in tuning and technique.

Auto-preview: OFF by default — a full sweep is a significant perceptible event. Explicit
`[▶ Preview]` only.

### ✅ Java implementation note (OPS-045)

`HarpSweepExpander` generates N MUSIC events with variable inter-event tick spacing computed
from the technique's speed profile and the total tick count derived from the range + cycles.
The expander applies the weight profile to per-note volume envelopes and the overlap model to
per-note max_duration_ticks. `PatternExpander` / `PhraseExpander` are not used — this is a
distinct expander with its own math.

OPS-045 filed (2026-04-06): `HARP_SWEEP` event type, `HarpSweepExpander`, named technique
registry, Phase 2 panel.

---

