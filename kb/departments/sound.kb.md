---
department: Sound Designer
owner: Sound Designer
kb_version: 2.1
updated: 2026-03-25
---

# Sound Designer — Technical Knowledgebase

> Technical reference for the Sound department. Documents what the ScaenaShows Java plugin
> can do for audio playback and control — and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §7. Sound Designer`.

## Sound Department Subfolder

Extended references for Sound department specialists live in `kb/departments/sound/`:

| File | Contents |
|------|----------|
| `sound/music-director.md` | Music Director role — note block instrument palette, pitch/chord/melody authoring, redstone arrangement workflow, starter motif library, ensemble deployment |

---

## Role Summary

- Owns SOUND and STOP_SOUND events throughout the show timeline
- Owns the audio arc: what the player hears, how sounds layer, and where silence falls
- Owns ambient bed management: establishing environmental layers and clearing them on scene change
- Owns the deployment of the Sound Ensemble — the named theatrical musicians whose gesture cues are available throughout every production
- The question every decision traces back to: **what does the player hear at this moment, and what does the silence between sounds mean?**

---

## Instrument Inventory

---

### Instrument 1: The Point Hit
**Java grounding:** `SOUND`

A single, non-looping sound fired at a specific tick. The punctuation instrument: arrivals, emotional accents, transitional strikes, and any moment where a sound is meant to land once and stop.

**What it does:** Plays a named Minecraft sound for each participant. Every participant hears the sound as if it originates from their own position — the plugin calls `p.playSound(p.getLocation(), ...)`, not from the show anchor. Volume controls perceived loudness (0.0–1.0); values above 1.0 expand the broadcast radius so nearby non-participants may also hear it, but do not increase loudness for the participant.

**How to dial it:**

```yaml
# Minimal: a single bell strike
- at: 120
  type: SOUND
  sound_id: minecraft:block.note_block.bell
  category: master
  volume: 0.8
  pitch: 1.0

# Pitched down for weight and gravity
- at: 240
  type: SOUND
  sound_id: minecraft:entity.warden.heartbeat
  category: hostile
  volume: 1.0
  pitch: 0.6            # heavier, more ancient — further from natural

# Capped duration: play a long sound but cut it after 2 seconds
- at: 300
  type: SOUND
  sound_id: minecraft:entity.elder_guardian.curse
  category: hostile
  volume: 1.0
  pitch: 1.0
  max_duration_ticks: 40   # hard cut at 2s — not a fade
```

**Strengths:**
- Pitch is the most expressive parameter. A single sound ID covers a wide emotional range depending on how far from 1.0 the pitch is set. See §Pitch as a Creative Tool.
- `max_duration_ticks` allows truncating a long sound to serve a specific pacing need — useful when a sound's natural length is too long for the beat.
- Low-volume single hits (`volume: 0.3–0.5`) feel intimate and interior — almost as if the player is hearing something in their own mind.

**Limitations and gaps:**
- Sound always plays at the player's location. There is no offset or world-position parameter — a sound from "across the stage" is not currently achievable for participants. (📋 Aspirational — would require an `offset:` field added to the YAML model.)
- `max_duration_ticks` is a hard cut; no fade. Plan for this in pacing.
- No loop control on non-looping sounds — they play once and stop naturally.

**Storytelling contexts:**
- A single beacon.activate at the moment the player crosses a threshold — the sound that confirms the arrival
- Gracie's plink (two notes, low volume) acknowledging a clever line with a quiet nod — sound as aside
- A deep warden heartbeat pitched to 0.55 as a large figure appears — the presence announced before anything moves

---

### Instrument 2: The Ambient Bed
**Java grounding:** `SOUND` (looping sounds) + `STOP_SOUND`

A sustained environmental audio layer established at a scene's opening and held beneath everything that follows. The most powerful atmospheric instrument the Sound department has — the layer the player feels without consciously noticing.

**What it does:** Fires a looping ambient sound at the start of a scene. The sound continues indefinitely until a STOP_SOUND event clears it. Multiple beds can run simultaneously. The Sound Designer owns the decision of when each bed starts, stops, and whether beds overlap during transitions.

**How to dial it:**

```yaml
# Open a scene with a cave ambience bed
- at: 0
  type: SOUND
  sound_id: minecraft:ambient.cave
  category: ambient
  volume: 0.6
  pitch: 1.0

# Begin a second bed at scene transition — two beds momentarily overlap
- at: 400
  type: SOUND
  sound_id: minecraft:ambient.soul_sand_valley.loop
  category: ambient
  volume: 0.5
  pitch: 1.0

# Cut the first bed cleanly at the same tick the new bed opens
- at: 400
  type: STOP_SOUND
  source: ambient         # stops all ambient-channel sounds — including the cave bed

# ⚠️  The above STOP_SOUND clears all ambient-channel sounds simultaneously,
#     which would also cut the new soul_sand_valley bed just opened.
#     To preserve a new bed while cutting the old one, fire the new SOUND a
#     tick before the STOP_SOUND, or pair with a master-channel transitional hit.
#     See §Bed Transition Technique below.
```

**Bed Transition Technique:**

Because STOP_SOUND clears by channel, not by individual sound ID, overlapping two beds on the same channel requires care:

```yaml
# Scene A's bed (ambient)
- at: 0
  type: SOUND
  sound_id: minecraft:ambient.cave
  category: ambient
  volume: 0.7
  pitch: 1.0

# Route Scene B's bed to a DIFFERENT category to allow clean crossfade
- at: 380
  type: SOUND
  sound_id: minecraft:ambient.crimson_forest.loop
  category: hostile         # intentionally in hostile channel, not ambient
  volume: 0.5
  pitch: 1.0

# Now clear the ambient channel cleanly, leaving Scene B running
- at: 400
  type: STOP_SOUND
  source: ambient

# Clear hostile channel at end of Scene B
- at: 600
  type: STOP_SOUND
  source: hostile
```

**Strengths:**
- Looping ambient sounds are among Minecraft's most expressive atmospheric tools. They're designed for immersion and benefit from low volume (they don't need to call attention to themselves).
- A bed established before any dialogue fires sets the emotional ground the Voice Director's text lands on.
- Two beds from different world registers (cave + nether, or warped_forest + portal) create a compound texture neither achieves alone.

**Limitations and gaps:**
- STOP_SOUND stops by channel, not by sound ID. You cannot stop a single named sound without stopping everything on its channel. Plan bed placement around this constraint — use different channels for sounds you may need to stop independently. (⚠️ STOP_SOUND by specific `sound_id` is not implemented — see §Capability Status Summary)
- A looping sound that is never stopped will continue playing past the show's end. Every bed opened must have a STOP_SOUND in the timeline, including at show end. This is structural, not optional.

**Storytelling contexts:**
- `ambient.cave` under an entire grief sequence: the world below acknowledging what is happening above
- `ambient.warped_forest.loop` at low pitch to set an alien register before a wonder cue fires — the world has already shifted before anything is said
- Two overlapping ambient beds during a transition, one fading via simulated fade (Instrument 4) while the other opens, creating a sound crossfade

---

### Instrument 3: The Channel Cut
**Java grounding:** `STOP_SOUND`

The silence instrument. Clears all sounds on a channel (or all channels) instantly. Used to establish deliberate silence, to mark a scene break, or to clear the audio stage before a new layer opens.

**What it does:** For each participant, calls `stopSound(SoundStop.source(src))` on the specified channel, or `stopSound(SoundStop.all())` for a full clear.

**How to dial it:**

```yaml
# Clear the ambient channel (all looping ambient beds)
- at: 400
  type: STOP_SOUND
  source: ambient

# Clear hostile channel (mob-register sounds)
- at: 400
  type: STOP_SOUND
  source: hostile

# Clear the music channel
- at: 200
  type: STOP_SOUND
  source: music

# Nuclear option — cut everything simultaneously
- at: 400
  type: STOP_SOUND
  source: all
```

**Valid `source` values:**

| Value | What it clears |
|-------|---------------|
| `ambient` | Ambient/cave sounds |
| `music` | Music disc / background music |
| `hostile` | Hostile mob sounds |
| `neutral` | Neutral mob sounds |
| `block` | Block sounds |
| `player` | Player sounds |
| `master` | Master channel sounds |
| `all` | Every channel simultaneously |

**Note on defaults:** If `source:` is omitted, the model defaults to `"music"` — this is almost certainly not what is intended. Always specify `source:` explicitly.

**The cut is abrupt. Make it feel intentional:**

STOP_SOUND has no fade. An uncontextualized cut can feel like a technical failure rather than a compositional choice. Two techniques for making it feel intentional:

```yaml
# Technique A: Pair the cut with a transitional hit on the same tick
# The new sound "masks" the cut — the ear accepts the cut as the transition itself
- at: 400
  type: STOP_SOUND
  source: ambient
- at: 400
  type: SOUND
  sound_id: minecraft:entity.lightning_bolt.thunder
  category: master
  volume: 1.0
  pitch: 0.7

# Technique B: Silence is the point — let the cut land
# When deliberate silence is the dramatic intention, the abruptness IS the effect.
# Mark it in the run sheet: "deliberate audio silence — do not fill."
- at: 400
  type: STOP_SOUND
  source: all
```

**Storytelling contexts:**
- Full cut (`source: all`) before the show's most important voice line — the text lands into complete silence
- Ambient cut at a grief peak, leaving only the Voice Director's words in an empty room
- Music cut on a specific beat to confirm an arrival — the player hears silence before the next sound layer opens, which makes the new layer feel earned

---

### Instrument 4: The Simulated Fade
**Java grounding:** Multiple `SOUND` events descending in volume, followed by `STOP_SOUND`

Minecraft has no native audio fade. This instrument simulates one by playing the same sound at progressively lower volumes over a series of ticks, ending with a channel cut. The ear perceives the sequence as a fade even though each step is technically a new sound event layered over the previous.

**How to dial it:**

```yaml
# Fade out an ambient bed over ~2 seconds (40 ticks)
# The descending SOUND events overlap with the looping bed and pull focus downward
- at: 360
  type: SOUND
  sound_id: minecraft:ambient.cave
  category: ambient
  volume: 0.5
  pitch: 1.0
- at: 370
  type: SOUND
  sound_id: minecraft:ambient.cave
  category: ambient
  volume: 0.3
  pitch: 1.0
- at: 380
  type: SOUND
  sound_id: minecraft:ambient.cave
  category: ambient
  volume: 0.1
  pitch: 1.0
- at: 390
  type: STOP_SOUND
  source: ambient
```

**Limitations:**
- This is a simulation, not a true fade. The ear is forgiving at low volumes but the technique has diminishing returns at high volumes — a loud sound faded this way may feel steppy.
- The re-fired SOUND events start the sound from the beginning of its clip, which can cause audible restart artifacts for short sounds. Works best with ambient loops (which continuously re-play anyway) and sustained tones.
- Use 3–4 steps spaced 10–15 ticks apart. Fewer steps is more abrupt; more steps with too-short spacing can muddy.

---

### Instrument 5: The Sound Ensemble
**Java grounding:** `SOUND` (via `minecraft:block.note_block.*` instruments), deployed as named cues

The Sound department maintains a resident ensemble of theatrical musicians who stand by throughout every production. Their gestures are scripted as named cues in the library (`gracie.*` namespace) and can be summoned by the Show Director at any point in the show. See §Sound Department Personnel for the full roster and gesture reference.

**What it does:** Ensemble cues are pre-authored `SOUND` events in specific pitch and volume combinations, packaged as reusable cues and referenced in the show timeline via `type: CUE`. The musician's identity gives each gesture a legible tone and working style.

```yaml
# Summon Gracie for a wry acknowledgment
- at: 120
  type: CUE
  cue_id: gracie.plink.fourth

# A dreamy float before the next voice line
- at: 180
  type: CUE
  cue_id: gracie.glissando.dreamy

# A sharp accent to button an arrival
- at: 240
  type: CUE
  cue_id: gracie.glissando.accent
```

---

## Sound ID Reference — Show Library

A curated selection organized by emotional function. For the full list, see `minecraft.wiki/w/Sounds.json`. All IDs below are verified present in Minecraft 1.21.x.

---

### Atmosphere — Environmental Beds

| Sound ID | Register | Notes |
|----------|----------|-------|
| `minecraft:ambient.cave` | Unease, something below, the underground | Loops automatically |
| `minecraft:ambient.basalt_deltas.loop` | Alien, volcanic, another world entirely | Loops; harsh texture |
| `minecraft:ambient.crimson_forest.loop` | Otherworldly, unsettling, corrupted depth | Loops |
| `minecraft:ambient.warped_forest.loop` | Teal-shift, strange calm, alien stillness | Loops |
| `minecraft:ambient.soul_sand_valley.loop` | Howling void, the place of the dead | Loops; haunting |
| `minecraft:ambient.nether_wastes.loop` | Heat and emptiness | Loops |
| `minecraft:weather.rain` | Rain without a WEATHER storm change | Does not loop; refire to sustain |

---

### Presence — Arrival, Weight, Significance

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.elder_guardian.curse` | The curse lands — weight, significance, something unavoidable |
| `minecraft:entity.warden.heartbeat` | Dread proximity, something large and near |
| `minecraft:entity.warden.roar` | Primal, overwhelming, the deep darkness asserts itself |
| `minecraft:entity.ender_dragon.growl` | Ancient power, something that predates the player |
| `minecraft:entity.ender_dragon.ambient` | Continuous ancient presence, something overhead that does not leave |
| `minecraft:block.portal.ambient` | The threshold, liminal space, in-between |
| `minecraft:entity.lightning_bolt.thunder` | Consequence, punctuation, the sky responding |

---

### Warmth — Joy, Community, Comfort

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.allay.ambient_with_item` | Small delight, something attending to you |
| `minecraft:entity.allay.item_given` | A gift exchanged — intimacy, the small generosity |
| `minecraft:entity.villager.celebrate` | Community, festivity, the ordinary world at its best |
| `minecraft:block.note_block.bell` | Clarity, ceremony, punctuation that lands clean |
| `minecraft:block.note_block.chime` | Lighter clarity, a reminder, something high and bright |
| `minecraft:entity.player.levelup` | Arrival, completion, a threshold crossed |
| `minecraft:block.amethyst_cluster.hit` | Crystalline brightness, something rare touched |

---

### Transition — Things Beginning or Ending

| Sound ID | Register |
|----------|----------|
| `minecraft:block.beacon.activate` | Rising, something beginning, emergence from waiting |
| `minecraft:block.beacon.deactivate` | Withdrawal, something ending, a light going out |
| `minecraft:entity.experience_orb.pickup` | Completion, the small satisfying close |
| `minecraft:block.end_portal.frame.fill` | The uncanny, a threshold crossed, the irreversible |
| `minecraft:block.conduit.activate` | Underwater awakening, something ancient starting |
| `minecraft:ui.toast.challenge_complete` | Recognition, a moment of public acknowledgment |

---

### Grief — Weight, Loss, Quiet Devastation

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.iron_golem.hurt` | The protector wounded — loyalty under strain |
| `minecraft:entity.iron_golem.death` | Devotion extinguished |
| `minecraft:entity.wolf.whine` | Distress, loyalty through pain, the companion suffering |
| `minecraft:entity.ghast.moan` | Something in anguish at great distance |
| `minecraft:entity.ghast.scream` | Raw grief erupting |
| `minecraft:block.glass.break` | Fragility shattered — the irreversible small loss |

---

### Tension — Dread, the Unannounced

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.creeper.primed` | The countdown, the thing about to happen |
| `minecraft:entity.enderman.stare` | Being observed, the uncanny watcher |
| `minecraft:entity.phantom.ambient` | Consequence overhead, the cost of avoidance |
| `minecraft:entity.spider.ambient` | Unease, something in the dark that is patient |
| `minecraft:block.sculk_sensor.clicking` | Detection, proximity to consequence |

---

## Pitch as a Creative Tool

Pitch changes frequency and speed simultaneously. A pitched-down sound is heavier and slower; a pitched-up sound is brighter and faster.

| Pitch value | Effect |
|-------------|--------|
| 0.5 | Octave down, half speed — very heavy, ancient, subterranean |
| 0.6–0.7 | Deep and ominous — thunder becomes tectonic, presence becomes godlike |
| 0.8–0.9 | Slightly lower — gravity, weight, seriousness |
| 1.0 | Natural pitch |
| 1.2–1.3 | Slightly brighter — urgency, fragility, a touch of lightness |
| 1.5–1.6 | Higher, faster — excitement, alarm, something small and quick |
| 2.0 | Octave up, double speed — tiny, urgent, mechanical |

Pitch is the most accessible expressive control Sound has. A single sound ID covers significant emotional range depending on where pitch is set. A warden.heartbeat at 1.0 is dread; at 0.55 it becomes mythic. An allay note at 1.0 is warm; at 1.8 it becomes urgent.

---

## Sound Department Personnel

The Sound department is staffed by named theatrical musicians who stand by throughout every production. They respond to the show's needs — punctuating moments, supporting transitions, providing live musical texture beneath scripted SOUND events. Their gestures are pre-authored as named cues in the library and can be called by any show at any point.

Additional instrumentalists will be added to this roster as they are defined.

---

### Gracie the Harpist

Gracie is always in the wings, instrument at the ready. She doesn't score scenes — she *comments* on them. Her gestures are small, precise, and perfectly timed: a flourish to greet an arrival, a low note that turns a room ominous, a quick plink to punctuate a wry moment.

Her primary instrument is `minecraft:block.note_block.harp` — the closest vanilla analog to a concert harp. Short, clean decay. Gracie works pitch as her register control: 1.4–2.0 is her high register (bright, crystalline); 0.5–0.8 is her low register (resonant, weighty). All gestures use category `master`.

**Gracie's gesture vocabulary** — all available as named cues in the `gracie.*` namespace:

---

#### `gracie.glissando.dreamy` — High-register whole-tone glissando, unhurried

Gracie's thinking gesture. A five-note ascending whole-tone run in the high register, one note every 3 ticks (0.15 seconds). Used when a mood is shifting, when something wistful is arriving, or when the show needs to breathe and float for a beat. The notes don't announce themselves — they arrive.

| Note | Tick | Pitch | Interval |
|------|------|-------|----------|
| 1 | 0 | 1.20 | — |
| 2 | 3 | 1.35 | +whole tone |
| 3 | 6 | 1.51 | +whole tone |
| 4 | 9 | 1.70 | +whole tone |
| 5 | 12 | 1.91 | +whole tone |

Duration: 12 ticks (0.6 seconds). Volumes arc 0.50 → 0.62 → 0.58 (peak at note 3, taper out).

---

#### `gracie.glissando.accent` — High-register whole-tone glissando, sharp

The same five-note whole-tone run, zipped through in 2 ticks per step (8 ticks total, 0.4 seconds). Used to button an appearance, accent a line landing, or close a moment with a crisp flourish. The difference from the dreamy version is entirely in tempo — same pitches, decisively faster.

| Note | Tick | Pitch |
|------|------|-------|
| 1 | 0 | 1.20 |
| 2 | 2 | 1.35 |
| 3 | 4 | 1.51 |
| 4 | 6 | 1.70 |
| 5 | 8 | 1.91 |

---

#### `gracie.chord.low_ominous` — Low-register whole-note, ominous sustain

A single low note at pitch 0.55 (between a half and full octave below natural). It blooms open and sustains. Used under dread, before a revelation, or when something large and unavoidable has entered the room. Gracie plays this one and holds it — the silence after is part of the gesture. `max_duration_ticks: 60` (3 seconds).

---

#### `gracie.plink.fourth` — Two-note ascending fourth, percussive

Two notes, quick and percussive, a fourth apart. The "wink" gesture. Gracie uses this to nod at something, acknowledge a clever moment, or give light punctuation that doesn't call attention to itself. The interval of a fourth (pitch ratio 1.335) gives it a light, open-question quality — less resolved than a fifth, slightly more mischievous.

| Note | Tick | Pitch |
|------|------|-------|
| 1 | 0 | 1.50 |
| 2 | 3 | 2.00 |

---

#### `gracie.plink.fifth` — Two-note ascending fifth, percussive

Same two-note structure, a fifth interval (pitch ratio 1.498). Slightly more open and conclusive than the fourth. The nod rather than the wink — the moment being acknowledged is more resolved.

| Note | Tick | Pitch |
|------|------|-------|
| 1 | 0 | 1.33 |
| 2 | 3 | 2.00 |

---

#### Pitch Math Reference (for authoring new Gracie variants)

| Interval | Semitones | Pitch multiplier |
|----------|-----------|-----------------|
| Whole tone | 2 | × 1.1225 |
| Minor third | 3 | × 1.1892 |
| Major third | 4 | × 1.2599 |
| Perfect fourth | 5 | × 1.3348 |
| Perfect fifth | 7 | × 1.4983 |
| Octave | 12 | × 2.0000 |

The note block harp has a short decay — it does not sustain naturally except at low pitches. The dreamy glissando relies on the slight overlap of decays between notes spaced 3 ticks apart; tighter spacing muddies articulation; wider spacing reads as separate notes, not a run.

---

## Tone Translation

The Show Director communicates tone as experience. The Sound department translates those phrases into YAML.

**When the Director says "tender":**
A single sound rather than a layer. Low volume (0.4–0.6). No competing bed. Gracie's plink, or a single allay note. The silence after the note is part of the gesture — don't fill it.

**When the Director says "overwhelming":**
Multiple simultaneous sound layers. Sounds that build — an ambient bed opened first, then a presence sound added, then a third layer if the scene supports it. The player doesn't need to distinguish sources; the accumulation is the instrument.

**When the Director says "ancient" or "heavy":**
Pitch down (0.5–0.7). Portal ambient or cave loop as bed. Warden heartbeat or dragon ambient as presence layer. Long sounds — things that don't move quickly. Gracie's low_ominous chord if the moment needs punctuation.

**When the Director says "earned" or "arrival":**
The sound was withheld. Silence first — a deliberate channel cut (STOP_SOUND `source: all`) or a REST with no sounds firing — then the hit lands. beacon.activate or experience_orb.pickup. The sound comes *after* the moment, not *with* it: a half-beat late so it confirms rather than announces.

**When the Director says "quiet devastation":**
The room going quiet after something loud. STOP_SOUND on the ambient layer, then deliberate silence, then one thing: wolf.whine, iron_golem.hurt, or Gracie's low chord. Less is more. Do not fill the silence.

**When the Director says "uncanny" or "strange":**
Familiar sounds in wrong contexts. ambient.cave when the player is clearly outdoors. A note_block harp at a grief beat. Pitch-shifted familiar sounds. enderman.stare under a warm scene. The wrongness is the register.

**When the Director says "intimate":**
volume: 0.3–0.5, master channel, no ambient bed, no layering. One sound at a time. Gracie at low volume. The space between sounds is as long as, or longer than, the sounds themselves.

**When the Director says "joy":**
Brighter pitches (1.2–1.6). Allay sounds, villager.celebrate, note_block.bell. Gracie's accent glissando to button a moment. Multiple layers that complement rather than compete — warmth and clarity, not loudness.

**When a tone phrase doesn't translate without more context:**
Signal back to the Director with a specific question: "What is the sound *doing* here — announcing the moment, confirming it, or unsettling it?" The register changes significantly depending on the answer. Don't guess; ask once, then proceed.

---

## Department Principles

**Sound is the layer the player feels even when they are not consciously noticing it.** The ambient bed sets the emotional register of a scene before a single voice line fires. The Sound Designer's first question is always: what is the world sounding like before the show begins — and is that what the scene needs?

**The default is not silence.** The world already has ambient sounds playing. Sound Designer decides whether to use them, layer over them, or clear them and establish new ones. Doing nothing is also a choice, but it should be made deliberately.

**Every looping sound that opens must have a corresponding STOP_SOUND in the timeline.** This is structural, not creative. An ambient bed that runs past the scene's end is a production error. Stage Manager verifies loop closure during the pre-show tech check.

**The cut is abrupt. Make it feel intentional.** STOP_SOUND has no fade. When silence is the dramatic intent, the abruptness is the effect — lean into it. When a clean transition is the intent, pair a transitional hit (thunder, chime, boom) at the same tick to mask the cut.

**STOP_SOUND stops by channel, not by sound ID.** Place beds you may need to stop independently on separate channels. Design the channel assignment of a show's audio map before writing YAML.

**Silence is a composition decision.** A deliberate silence — a REST with no sounds, following something loud — is one of the most powerful instruments Sound has. Mark intentional silences in the run sheet: "deliberate audio silence — do not fill." If it isn't marked, Stage Management will ask whether it was intentional.

**Coordinate with Lighting on LIGHTNING.** Thunder is inherent to the visual strike and cannot be separated. Any show that uses LIGHTNING has an implicit audio moment. Plan the surrounding audio arc around it, not after.

**Coordinate with Effects on WEATHER.** A `storm` state activates a rain sound bed the Sound Designer did not author and cannot stop with STOP_SOUND. Account for the storm's ambient layer in the audio arc whenever Effects calls for weather changes.

**The ensemble musicians are on call throughout every production.** Gracie and future instrumentalists are available on cue. Their gestures are named cues in the library — call them directly from the show timeline. They should not be re-authored inline in show YAMLs; always use the `gracie.*` cue reference.

**Escalation:** Sound resolves audio arc, loop management, bed layering, and ensemble deployment independently. Escalate to the Show Director when: a sound would compete with a Voice Director text moment and the tradeoff requires a creative call; a tone phrase is ambiguous enough that guessing feels reckless; or a scene requires a capability (positional audio from offset, per-sound-ID stopping) that is currently gapped with no acceptable workaround.

---

## Capability Status Summary

| Instrument / Feature | Status | Notes |
|---|---|---|
| SOUND — play at participant location | ✅ Verified | Plays at `p.getLocation()` — participant's position, not anchor |
| SOUND — volume (0.0–1.0) | ✅ Verified | Controls loudness for participant |
| SOUND — volume (> 1.0) | ✅ Verified | Expands broadcast radius for nearby non-participants; no loudness increase for participant at distance 0 |
| SOUND — pitch (0.5–2.0) | ✅ Verified | Full range confirmed |
| SOUND — max_duration_ticks | ✅ Verified | Hard cut via BukkitRunnable; no fade |
| SOUND — play from anchor offset | 📋 Aspirational | No offset field in model; would require YAML model enhancement |
| STOP_SOUND — stop by channel (`source:`) | ✅ Verified | source: ambient \| hostile \| music \| neutral \| block \| player \| master \| all |
| STOP_SOUND — stop by specific sound_id | ⚠️ Gapped | `StopSoundEvent` model has no `sound_id` field; a `sound_id:` key in YAML is silently ignored. Not yet filed — hold for ops-inbox review. |
| STOP_SOUND — default source | ⚠️ Note | Defaults to `"music"` if `source:` is omitted. Always specify explicitly. |
| SOUND — audience targeting | ⚠️ Note | No `audience:` field; executor hardcodes participants. `audience: participants` in YAML is silently ignored. |
| Simulated fade (multi-SOUND descending) | ✅ Verified | Works via sequential SOUND events; ear accepts as fade at low volumes |
| Native fade-out | 📋 Aspirational | Not supported by Minecraft sound system |
| Named ensemble cues (gracie.*) | ✅ Verified | Pre-authored cue library; invoke via type: CUE |
