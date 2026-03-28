---
department: Sound Designer
owner: Brian
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Sound — Brief to Direction

> Brian's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for Sound instruments, pitch, beds, and the ensemble
> lives in `kb/departments/sound/sound.kb.md`.

---

## Standing Requirements

*What Brian cannot start without. These answers come from the brief or the intake conversation — no audio work begins until they're resolved.*

**The show's emotional arc in two or three words.** Brian designs the audio arc around the emotional shape of the show, not around individual cues. "Quiet to overwhelming," "strange to tender," "pressure to release" — those phrases are enough to begin. A show with no stated arc gets a generic atmospheric treatment, which is not the same thing.

**Whether any WEATHER effects are planned.** A storm state (Effects dept) activates a rain sound bed that Brian didn't author and cannot stop via STOP_SOUND. Brian needs to know this in advance so the audio arc accounts for a bed he doesn't control. If weather isn't planned, say so — the absence is as useful as the presence.

**Whether any LIGHTNING effects are planned.** Thunder is inseparable from the lightning strike — it cannot be stopped or tuned by Sound. Brian builds the surrounding audio arc around LIGHTNING events; he needs to see them in the rough timeline before the show is authored.

**The rough timing of any Voice narration.** Sound and text compete for the player's attention when they fire simultaneously. Brian needs to know where the major Voice moments are so he can clear space, offset hits, or coordinate simultaneous intent. This isn't about avoiding conflict — it's about deciding which department leads at each beat.

**Any sections that should be complete silence.** Deliberate silence is a Sound instrument. If Direction has a moment in mind where nothing plays — no ambient bed, no music, no hit — say so explicitly. Brian will mark it in his audio map as authored silence. An unmarked gap looks like a mistake.

---

## Intake Questions

*Questions Brian will ask at the intake meeting or in the brief. These are the ones he doesn't guess about.*

- What is the world environment for this show — biome, interior or exterior, any existing ambient sounds the player might already be hearing when the show starts?
- Is there a sonic register you have in mind, even loosely? A specific sound that captures the show's mood, or a sound you'd want to avoid?
- How talkative is this show sonically — ambient bed throughout, or should it breathe with silence between moments?
- Does the show have a musical layer (note blocks, Gracie ensemble), an atmospheric layer (environmental beds only), or both?
- Is there a moment in the show that should be the quietest? And a moment that should be the loudest? Knowing the two poles of the audio arc helps Brian place everything in between.

---

## Requests

*Nice-to-haves. These improve the work but don't stop it.*

- Knowing the player's altitude arc (from Effects) helps Brian choose which sounds to layer in aerial vs. ground sections. Some ambient registers read very differently at 20 blocks than at ground level.
- Knowing any TIME_OF_DAY changes planned by Lighting — the ambient world shifts at day/night transitions and Brian wants to use those shifts, not fight them.
- If Choreography has performer arrivals or exits in mind, flagging the tick helps Brian plan sound hits around those moments. An entrance without a sound is often an entrance that doesn't land.

---

## Sound's Palette

Brian's four instruments, in brief:

**Point hits** — a single sound event at a specific tick. The punctuation tool: arrivals, accents, confirmations. Pitch is the primary expressive control — the same sound ID covers a wide emotional range depending on how far from 1.0 the pitch is set.

**Ambient beds** — looping environmental sounds established at scene open and cleared on scene change. The most powerful atmospheric layer; the player feels it without consciously noticing it. Every bed that opens must have a STOP_SOUND to close it. This is structural, not creative.

**Channel cuts** — STOP_SOUND clearing a channel or all channels simultaneously. Deliberate silence as an instrument. The cut is abrupt — Brian makes it feel intentional by pairing it with a transitional hit or by letting the abruptness be the effect.

**Simulated fades** — a sequence of descending SOUND events approximating a fade-out. Works best on looping ambient sounds. Minecraft has no native audio fade.

**The Sound Ensemble** — Gracie and resident musicians, available as named cues throughout every production. Gracie plays piano/harp commentary: flourishes, plonks, chords, winks. See `sound.kb.md §Sound Department Personnel` for the full gesture vocabulary.

For the complete sound ID library, pitch reference, and channel discipline, see `sound.kb.md`.

---

## What Sound Can and Can't Do

**Verified and working:**
- Ambient beds (looping sounds), single hits, volume and pitch control, max_duration_ticks hard cut
- STOP_SOUND by channel (ambient, hostile, music, master, all, etc.) — clears everything on that channel simultaneously
- Simulated fade (multi-event descending volume sequence)
- Named ensemble cues (gracie.* namespace) — call via `type: CUE`

**Current gaps — flag these at intake if they're relevant to the show:**
- STOP_SOUND cannot target a specific named sound — it stops everything on a channel. Plan bed placement so beds you may need to stop independently are on different channels.
- No positional audio from a fixed world location for participants. Sound always plays at the participant's position, not from "across the stage." A sound from a performer's side of the stage isn't achievable for show participants.
- No native fade. The simulated fade works, but it's a simulation — expect some steppiness at high volumes.

---

## Document Status

*First pass — 2026-03-28*

This covers the core of what Brian needs from Direction and what Direction needs to know about Sound's palette. What's still needed:

- Examples of how Sound's questions surface into per-show department intake records (that writing will happen show by show as Showcase Series briefs are authored)
- Calibration patterns confirmed and named (see `sound.kb.md §Calibration Backlog`) — the `bed.dark`, `bed.wonder`, and button patterns are still proposed, not confirmed. The brief-to-direction should eventually point to confirmed patterns, not just proposed ones.
- Music Director section (motif library, note block composition) — referenced in `sound/music-director.md`, not yet integrated here.
