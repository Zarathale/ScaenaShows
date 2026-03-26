---
role: Music Director
department: Sound
owner: Music Director
kb_version: 1.0
updated: 2026-03-25
---

# Music Director — Technical Knowledgebase

> The Music Director is a specialist within the Sound department — not a standalone department.
> The Sound Designer retains authority over the audio arc. The Music Director advises on and
> authors the *musical layer specifically*: note block arrangements, motifs, underscore, and
> ensemble deployment. The Sound Designer calls on the Music Director when a scene needs
> something with pitch, rhythm, and musical identity beyond ambient beds and point-hit sounds.
>
> Creative direction for the Sound department lives in `kb/production-team.md §7. Sound Designer`.
> Instrument capabilities and event YAML live in `kb/departments/sound.kb.md`.

---

## Role Summary

- Advises the Sound Designer on *when* music is the right layer and *what form* it should take
- Owns the note block instrument palette — the range, timbre, and register of each instrument
- Authors musical gestures: motifs, riffs, underscore figures, chords — either inline as YAML events or as named cues in the library
- Calls the ensemble musicians (Gracie and others) — knows which gesture fits which moment
- Designs world-built redstone note block arrangements and hands them off to the Set Director for construction
- The question this role asks: **does this scene need music, and if so: what instrument, what shape, what is it doing to the story?**

---

## The Two Approaches

The Music Director has two fundamentally different tools for putting music into a show. The choice between them shapes not just the sound but the production workflow.

---

### Approach A: YAML Sound Events (Inline Music)

Musical phrases authored directly as `SOUND` events in the show YAML — or packaged as reusable cues in the `motif.*` or `gracie.*` namespace and called via `type: CUE`.

**Characteristics:**
- Portable — works in any world, any set, with no pre-build
- Precise — each note fires at an exact show tick
- Iterable — changing the music means editing YAML, not rebuilding a redstone circuit
- Visible only as sound — players hear the music but cannot see it happening
- Limited polyphony — each note is a separate SOUND event; dense chords and fast runs accumulate YAML lines quickly

**When to use it:**
- Short gestures: a 3–5 note riff, a single chord, a brief underscore figure
- When the music needs to sync precisely with a voice line or visual moment
- When the show plays in multiple environments and portability matters
- For all ensemble musician gestures (Gracie always works this way)

**How to author:**
See §Authoring Technique below. Motifs can be inlined in the show YAML or called as cues:

```yaml
# Inline motif (few events — fine to embed directly)
- at: 120
  type: SOUND
  sound_id: minecraft:block.note_block.bell
  category: master
  volume: 0.7
  pitch: 1.189    # A4
- at: 124
  type: SOUND
  sound_id: minecraft:block.note_block.bell
  category: master
  volume: 0.75
  pitch: 1.335    # B4
- at: 128
  type: SOUND
  sound_id: minecraft:block.note_block.bell
  category: master
  volume: 0.8
  pitch: 1.498    # C#5

# Or: call a pre-authored cue — cleaner timeline
- at: 120
  type: CUE
  cue_id: motif.arrival.bell
```

---

### Approach B: World-Built Redstone Arrangement

Note blocks placed in the world, tuned to specific pitches, wired to a redstone circuit triggered by the show's `REDSTONE` event. The music is physically present in the world — players can see and hear it playing.

**Characteristics:**
- Visual — the note blocks are visible playing in the scene; this is "music *in* the world"
- Supports longer, more complex arrangements without YAML bloat
- Can loop natively (redstone clock circuits)
- World-specific — not portable; requires a specific build at specific coordinates
- Requires pre-show state verification before every run
- REDSTONE stop-safety gap applies — the circuit must be explicitly shut off

**When to use it:**
- Longer continuous musical passages (16+ notes, multi-instrument arrangements)
- When the visual element of note blocks playing is part of the scene's identity
- When a looping musical bed is needed (ambient music that runs for an entire section)
- When the music is deeply tied to the world environment (a magical machine, a living shrine)

**The production workflow for a world-built arrangement:**

1. **Music Director designs the arrangement** — writes the score: which instruments, what notes, what timing (redstone tick spacing), what loop structure. Delivers a written spec to Set.

2. **Set Director builds the circuit** — places note blocks at the agreed coordinates, tunes each block, wires the redstone trigger, documents the trigger point coordinates for the show YAML.

3. **Stage Manager verifies state pre-show** — checks that all note blocks are still tuned correctly (they can be accidentally re-tuned by player right-clicks or environmental damage), circuit is intact, and trigger block is in a known starting state.

4. **Show timeline triggers with REDSTONE** — paired on/off events:

```yaml
# Start the circuit at section 3 opening
- at: 240
  type: REDSTONE
  world_specific: true
  target: {x: 1042, y: 64, z: -334}    # trigger block coordinates — from Set's build notes
  state: on

# Stop the circuit at section 3 close
- at: 600
  type: REDSTONE
  world_specific: true
  target: {x: 1042, y: 64, z: -334}
  state: off
```

5. **Run sheet documents the circuit** — what it plays, what coordinates it's at, what state it needs to be in pre-show, and what the Music Director's design intent was (so Stage Manager can troubleshoot if something sounds wrong).

**Critical: every REDSTONE `state: on` requires a paired `state: off` in the timeline.** The REDSTONE event is not inside the plugin's stop-safety contract — if the show is interrupted, the circuit stays on. The Music Director designs the off; Stage Manager verifies it exists before the show runs. See `kb/departments/stage-manager.kb.md` for the full stop-safety protocol.

---

## Note Block Instrument Palette

Every instrument below is available via the corresponding `minecraft:block.note_block.*` sound ID. All have the same pitch range (F#3–F#5, pitch values 0.5–2.0). Differences are entirely in timbre.

> In a world-built arrangement, the instrument is determined by the block placed under the note
> block. In YAML, specify the sound ID directly — no block context required.

---

### Register I — Melodic Lead Instruments

These carry melody lines clearly. Use for motifs, riffs, and anything the ear should follow.

| Sound ID | Timbre | Emotional register | Best pitch range |
|----------|--------|-------------------|-----------------|
| `minecraft:block.note_block.harp` | Warm, mellow pluck; short decay | General purpose; lyrical; familiar | Full range |
| `minecraft:block.note_block.bell` | Bright, long ring; carries over other sounds | Ceremony, clarity, arrival, the sky | Mid-high (C5–F#5) |
| `minecraft:block.note_block.flute` | Breathy, airy, sustained-feeling | Longing, pastoral, something at distance | Mid-high |
| `minecraft:block.note_block.chime` | Crystalline, precise, short decay | Wonder, height, delicacy, the uncanny | High (C5–F#5) |
| `minecraft:block.note_block.xylophone` | Bright, bouncy, cheerful | Joy, playfulness, lightness, movement | Mid-high |
| `minecraft:block.note_block.bit` | Chiptune square wave; retro | Digital, synthetic, nostalgic, precise | Full range |

---

### Register II — Harmonic / Pad Instruments

Best for chords, sustained textures, and underscore. Can carry melody but shine as color.

| Sound ID | Timbre | Emotional register | Best pitch range |
|----------|--------|-------------------|-----------------|
| `minecraft:block.note_block.pling` | Electric piano; warm, intimate | Contemplative, jazz, interior, grief | Mid (F#4–D5) |
| `minecraft:block.note_block.iron_xylophone` | Harder, colder; more mechanical than xylophone | Precision, structure, slightly unsettling | Mid |
| `minecraft:block.note_block.guitar` | Warm strum with body | Earthiness, narrative, comfort, folk | Low-mid |
| `minecraft:block.note_block.banjo` | Bright twang; character | Folk warmth, community, the ordinary world | Mid |

---

### Register III — Bass and Foundation

Anchor the harmonic foundation. Often used as a single sustained note or slow moving bass line.

| Sound ID | Timbre | Emotional register | Best pitch range |
|----------|--------|-------------------|-----------------|
| `minecraft:block.note_block.bass` | Deep, resonant low pluck | Foundation, grounding, low weight, warmth | Low (F#3–C4) |
| `minecraft:block.note_block.didgeridoo` | Deep, droning, overtone-rich | Earth, ancient, indigenous, primal grounding | Low (F#3–B3) |

---

### Register IV — Percussion

No traditional pitch variation — rhythm and accent, not melody. These are used for timing, emphasis, and texture.

| Sound ID | Timbre | Best use |
|----------|--------|----------|
| `minecraft:block.note_block.basedrum` | Punchy kick | Rhythmic downbeat, emphasis under a moment |
| `minecraft:block.note_block.snare` | Crisp snap | Accent, punctuation, rhythmic texture |
| `minecraft:block.note_block.hat` | Light metallic | Fine rhythmic texture; subtle background pulse |
| `minecraft:block.note_block.cow_bell` | Loud metallic accent | Quirky emphasis; used sparingly |

> Note: Pitch values still affect the sound of percussion instruments (higher pitch = faster/higher
> transient), but the effect is subtle. These instruments are primarily textural rather than tonal.

---

## Pitch and Harmony Reference

All note block sounds use the same pitch scale. The SOUND event `pitch:` field controls where in
the two-octave range the note plays.

### Chromatic Scale — Full Reference (F#3 to F#5)

| Note | Pitch value | Note | Pitch value |
|------|-------------|------|-------------|
| F#3 | 0.500 | F#4 | 1.000 |
| G3 | 0.530 | G4 | 1.059 |
| G#3 / Ab3 | 0.561 | G#4 / Ab4 | 1.122 |
| A3 | 0.595 | A4 | 1.189 |
| A#3 / Bb3 | 0.630 | A#4 / Bb4 | 1.260 |
| B3 | 0.667 | B4 | 1.335 |
| C4 | 0.707 | C5 | 1.414 |
| C#4 / Db4 | 0.749 | C#5 / Db5 | 1.498 |
| D4 | 0.794 | D5 | 1.587 |
| D#4 / Eb4 | 0.841 | D#5 / Eb5 | 1.682 |
| E4 | 0.891 | E5 | 1.782 |
| F4 | 0.944 | F5 | 1.888 |
| | | F#5 | 2.000 |

**Interval multipliers** (apply to any root to find the interval above it):

| Interval | Semitones | Multiplier |
|----------|-----------|-----------|
| Minor second | 1 | × 1.0595 |
| Major second | 2 | × 1.1225 |
| Minor third | 3 | × 1.1892 |
| Major third | 4 | × 1.2599 |
| Perfect fourth | 5 | × 1.3348 |
| Perfect fifth | 7 | × 1.4983 |
| Minor sixth | 8 | × 1.5874 |
| Major sixth | 9 | × 1.6818 |
| Minor seventh | 10 | × 1.7818 |
| Major seventh | 11 | × 1.8877 |
| Octave | 12 | × 2.0000 |

---

## Authoring Technique

### Melody

A melody is a sequence of SOUND events at different pitches, spaced across ticks. Spacing determines tempo feel:

| Tick spacing | Approx. duration | Feel |
|---|---|---|
| 2 ticks | 0.1s | Very fast — urgency, rushing, excitement |
| 4 ticks | 0.2s | Brisk — active, conversational |
| 6 ticks | 0.3s | Moderate — walking pace, narrative |
| 8 ticks | 0.4s | Deliberate — weight, ceremony |
| 12–16 ticks | 0.6–0.8s | Slow — contemplative, grief, the deep |

For instruments with short decay (harp, chime, bell), notes spaced 3–4 ticks apart will slightly
overlap in their decay, creating a sense of legato. Notes spaced 8+ ticks apart read as separate,
deliberate gestures.

```yaml
# A slow, deliberate 4-note bass line — weight and ceremony
- at: 0
  type: SOUND
  sound_id: minecraft:block.note_block.bass
  category: master
  volume: 0.8
  pitch: 0.891    # E4
- at: 8
  type: SOUND
  sound_id: minecraft:block.note_block.bass
  category: master
  volume: 0.8
  pitch: 0.841    # D#4
- at: 16
  type: SOUND
  sound_id: minecraft:block.note_block.bass
  category: master
  volume: 0.85
  pitch: 0.794    # D4
- at: 24
  type: SOUND
  sound_id: minecraft:block.note_block.bass
  category: master
  volume: 0.9
  pitch: 0.749    # C#4 — settled on the low C#, grounded
```

### Chords

A chord is multiple SOUND events at the same `at:` value, using different pitches. The Minecraft
sound engine has no limit on simultaneous sounds from the same source.

Standard chord formulas (using the interval table above):

| Chord type | Intervals above root |
|---|---|
| Major | root + major third (×1.2599) + perfect fifth (×1.4983) |
| Minor | root + minor third (×1.1892) + perfect fifth (×1.4983) |
| Sus2 | root + major second (×1.1225) + perfect fifth (×1.4983) |
| Sus4 | root + perfect fourth (×1.3348) + perfect fifth (×1.4983) |
| Major 7th | root + major third + perfect fifth + major seventh (×1.8877) |

```yaml
# A minor chord in mid-register — A minor (A4 + C5 + E5) on pling
# All three fire at the same tick
- at: 0
  type: SOUND
  sound_id: minecraft:block.note_block.pling
  category: master
  volume: 0.5
  pitch: 1.189    # A4 — root
- at: 0
  type: SOUND
  sound_id: minecraft:block.note_block.pling
  category: master
  volume: 0.45
  pitch: 1.414    # C5 — minor third
- at: 0
  type: SOUND
  sound_id: minecraft:block.note_block.pling
  category: master
  volume: 0.40
  pitch: 1.782    # E5 — fifth
```

**Voicing tip:** Taper volume downward from root to highest note (root loudest). This gives the
chord body without the high notes overpowering. For airy, open chords, invert this — root quieter,
upper notes brighter.

### Riff / Motif Shape Vocabulary

A motif is a 3–8 note figure with a recognizable shape. The shape — not the notes — is what
makes it recognizable and reusable.

| Shape | Description | Register |
|---|---|---|
| **Rising step** | Two or three ascending notes, stepwise | Arrival, warmth, opening |
| **Rising leap** | Two notes with a large interval (fifth, octave) | Surprise, announcement, joy |
| **Descending step** | Stepwise descent, usually chromatic | Unease, grief, weight, descent |
| **Arch** | Up then down (or down then up) | Journey, return, completion |
| **Neighbor** | Root → one step up or down → return | Restlessness, questioning, unease |
| **Pedal point** | Repeated same pitch with varying upper notes | Insistence, inevitability, tension |
| **Fall** | A fast descending run ending on a low note | Collapse, deflation, loss |

---

## Starter Motif Library

Five named motifs across different emotional registers. Each exists as a cue in the library.
Call them via `type: CUE` — do not re-author inline when the library version serves.

---

### `motif.arrival.bell`

**Shape:** Rising step (A4 → B4 → C#5). Three notes. Bell instrument.
**Register:** Ceremony, arrival, a threshold crossed with grace. Something good is here.
**Spacing:** 4 ticks per note. Total: 8 ticks (0.4 seconds).
**When to use:** Under an entrance, after a voice line that resolves something, when a quiet arrival needs a musical confirmation without announcement.

```yaml
- at: [T]
  type: CUE
  cue_id: motif.arrival.bell
```

---

### `motif.unease.descend`

**Shape:** Descending chromatic (E4 → D#4 → D4). Three notes. Bass instrument.
**Register:** Dread, something wrong, a slow darkening. The weight increasing.
**Spacing:** 8 ticks per note. Total: 16 ticks (0.8 seconds). Slow by design — the descent earns its weight.
**When to use:** Under a reveal, when something shifts to darker register, before a WARDEN or PHANTOM enters the scene. Let it land before the next event fires.

```yaml
- at: [T]
  type: CUE
  cue_id: motif.unease.descend
```

---

### `motif.wonder.chime`

**Shape:** Ascending stepwise run (C5 → D5 → E5 → F#5). Four notes. Chime instrument. High register.
**Register:** Discovery, surprise, the beautiful uncanny. Something rare seen.
**Spacing:** 2 ticks per note. Total: 6 ticks (0.3 seconds). Brisk — wonder arrives fast.
**When to use:** Under a particle burst, when a set transition reveals something new, when the player arrives somewhere extraordinary. Follows the visual — fire it 2–4 ticks *after* the moment so it confirms rather than announces.

```yaml
- at: [T]
  type: CUE
  cue_id: motif.wonder.chime
```

---

### `motif.grief.chord`

**Shape:** Sustained A minor chord (A4 + C5 + E5 simultaneously). Pling instrument.
**Register:** Quiet devastation. The room's response to loss. Not loud — interior.
**Duration:** `max_duration_ticks: 60` (3 seconds). A single color, held until it fades.
**When to use:** After a grief peak, not during it. The chord is the room exhaling. Fire after the Voice Director's text has landed and silence has held for at least 20 ticks — the chord confirms the weight, it doesn't compete with it.

```yaml
- at: [T]
  type: CUE
  cue_id: motif.grief.chord
```

---

### `motif.warmth.banjo`

**Shape:** Pentatonic arch (D4 → F#4 → A4 → B4 → A4). Five notes. Banjo instrument.
**Register:** Warmth, community, the ordinary world at its best. Folk-character.
**Spacing:** 4 ticks per note. Total: 16 ticks (0.8 seconds). Unhurried.
**When to use:** Under a moment of domestic peace, when villagers or allies are present, as an underscore to a warmth cue or a voice line about togetherness. Don't overuse — this motif has strong character and can draw attention if the moment isn't earned.

```yaml
- at: [T]
  type: CUE
  cue_id: motif.warmth.banjo
```

---

## Calling the Ensemble

Gracie and future ensemble musicians are under the Music Director's direction during a show.
The Music Director calls the right gesture for the moment; the Sound Designer approves the
placement in the audio arc.

**When to call Gracie instead of a motif cue:**
- The moment needs a harp specifically — its timbre is the instrument, not just pitch content
- A glissando gesture (dreamy float, sharp accent) is exactly what the scene requires
- The moment needs a quiet, highly personal musical comment rather than a stated theme

**When to use a motif cue instead:**
- The show needs a recurring theme to be recognized across multiple uses
- The instrument family matters (chime for wonder, bass for dread, banjo for warmth)
- The moment needs a stronger gesture than Gracie's subtle vocabulary

**When to combine them:**
A motif cue and a Gracie gesture can fire in sequence — Gracie's accent glissando at T, then
`motif.arrival.bell` at T+10. The glissando acknowledges the moment; the motif names it.

---

## Cross-Department Coordination

**With Sound Designer:**
The Music Director advises; the Sound Designer approves. If a proposed musical moment competes
with an ambient bed the Sound Designer has established, that's a scheduling conflict — not an
automatic Music Director override. Bring the conflict to the Sound Designer: "the motif will
read better if the cave bed is stopped 20 ticks before. Is that acceptable for the audio arc?"

**With Set Director — world-built arrangements:**
The Music Director delivers a written arrangement spec to Set at brief time:
- Instrument list (which note block types are needed)
- Note sequence with tick spacing (converted to redstone repeater delays)
- Trigger block location request (Music Director proposes; Set places based on build constraints)
- Loop or one-shot (does the circuit need a clock, or a single-trigger)
- Any world-space constraints (sight lines, whether players can see the blocks)

Do not ask Set to build an arrangement and then change the notes after the circuit is wired.
Changing note block tuning requires manual right-clicking each block — field changes are costly.
Finalize the arrangement before the build handoff.

**With Stage Manager:**
Every world-built arrangement adds to the pre-show tech check. The Music Director documents:
- The arrangement ID and what show section it supports
- Trigger block coordinates
- Expected starting state (circuit off before show begins)
- How to quickly verify tuning is intact (e.g., "the first note should sound like a low G")
Stage Manager verifies this during the tech check, not during the show.

---

## Department Principles

**Music asks if the scene actually needs it.** Silence is the Music Director's first option, not
the last resort. Before calling for any musical gesture, the question is: is the scene better with
music or without it? If the answer is "either would work," the answer is probably "without."

**The motif is a commitment.** A recurring motif builds expectation. If `motif.arrival.bell` fires
three times in a show, the fourth time it fires will be heard as a callback — whether or not that
was intended. Use motifs consistently or not at all; accidental recurrences undermine their weight.

**Music does not compete with Voice.** A SOUND event fired at the same tick as a TITLE or MESSAGE
can muddy the text moment. Default: music fires 4–8 ticks *before* a quiet text moment (setting
the register), or 4–8 ticks *after* (confirming it). Simultaneous firing is a deliberate choice,
not a default.

**Short is usually right.** A 3–5 note motif heard clearly is more powerful than a 16-note phrase
the player missed because something else was happening. Brevity gives the moment room.

**Redstone arrangements are pre-production decisions, not rehearsal decisions.** Building a
note block circuit takes meaningful Set time. Don't request world-built music during a revision
cycle — only at brief. YAML motifs can be revised freely between runs; circuits cannot.

**Escalation:** The Music Director resolves instrument choice, gesture selection, motif library
deployment, and arrangement design independently. Escalate to the Sound Designer when a musical
decision conflicts with the established audio arc. Escalate to the Show Director when a musical
moment requires a tone call that changes what a scene *means* (music can change a scene from
grief to acceptance without a word of text changing — that is a directorial decision).

---

## Capability Status Summary

| Capability | Status | Notes |
|---|---|---|
| YAML note block sound events (all 16 instruments) | ✅ Verified | Via SOUND event; pitch 0.5–2.0; full chromatic range |
| Simultaneous chord (multiple SOUND at same tick) | ✅ Verified | No engine limit on concurrent sounds confirmed |
| Motif cues (`motif.*` namespace) | ✅ Verified | 5 starter cues in library; callable via type: CUE |
| Ensemble deployment (Gracie, `gracie.*`) | ✅ Verified | 5 gestures in library; see sound.kb.md §Sound Department Personnel |
| World-built redstone arrangement (note block circuit) | ✅ Verified | Via REDSTONE event; world-specific; requires Set build + SM state check |
| REDSTONE stop-safety | ⚠️ Gapped | Not inside plugin cleanup contract; every state: on requires explicit state: off. Filed in ops-inbox.md. |
| Per-instrument polyphony limit | 📋 Unknown | No documented engine limit found; monitor for client-side audio saturation in dense arrangements |
| Note block pitch beyond F#3–F#5 | ⚠️ Platform limit | Minecraft note block range is fixed; no workaround via plugin |
| Fade-out on note block sounds | 📋 Aspirational | No native fade; simulate via descending-volume SOUND sequence. See sound.kb.md §Instrument 4. |
