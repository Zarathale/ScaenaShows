---
show: demo.archetype_sampler
status: in-test
version: 2.9.0
created: 2026-03-23
director: Show Director   # Claude acting as Show Director
builder: Zara             # primary in-game builder/tester
---

# Show Brief — Archetype Sampler

> This is the first production-quality test show for the Scaena cue library.
> Its purpose is to demonstrate the emotional range of the archetype families
> in a single coherent experience — not a feature tour, but a real journey.

---

## The Arc

The player begins at ground level in dusk, surrounded by the ordinary world. The show gently separates them from the ground — first barely, then more — until an earned lift at the midpoint carries them fully into the sky. From that height, the show moves through joy, grief, wonder, and a return. The player lands where they started, changed by the distance traveled.

---

## The Player

A solitary witness, slowly lifted out of the world they know and shown it from a distance, then returned.

---

## Tone

Atmospheric and earned. Intimate in its address, large in its gestures. The uncanny warmth of being held by something you don't fully understand.

---

## What They Carry Away

The feeling of having been somewhere real — the sense that height was not a spectacle but a consequence of something that happened to them.

---

## Show Direction

**Non-negotiables:**
- The altitude arc must be emotionally earned — height withheld until C7, then released. Any change that moves the dramatic lift earlier breaks the show's premise.
- C2 must feel like "gentle bubbling," not a mechanical float. If it reads as arbitrary, the whole arc loses its ground.
- The C7 lift is the show's fulcrum. Choreography, Sound, and Lighting must all converge here — it is the moment the player knows something is happening to them.
- The show ends in the air. The player does not land cleanly. This is intentional and must not be resolved into comfort.

**Watch for:**
- Choreography and Sound at C7: the thunder hit must land *with* the lift, not before or after. A few ticks of offset destroys the convergence.
- Voice and altitude at C2–C6: text must not explain what is happening. The player should feel the ground leaving before Sprite names it.
- Lighting at C8–C9: joy burst and fireworks happen at altitude — if the player is above the effects rather than inside them, the height has been misused.

**Departments with elevated priority this show:**
Choreography carries the primary dramatic instrument (altitude arc). Sound is the emotional confirmation layer — every major altitude beat should have a corresponding audio signal. Voice sets the frame without explaining the mechanics. Casting, Wardrobe, and Camera are minimal in this show.

---

## Constraints

- **Duration:** ~1300 ticks (~65 seconds at 20t/s)
- **Setting:** Outdoor space, open sky, sufficient altitude clearance (40+ blocks)
- **Cue library:** atmos.*, mood.*, ramp.*, grief.*, coda.*, fx.levitate_*, world.time.*
- **Technical:** Calibrated levitation patterns in use (HOVER=28t, CLIMB=24t, RELEASE=44t)
- **Alan notes:** Altitude arc must be earned — height withheld, then released. C2 hover near ground. C7 is the moment the player truly lifts.

---

## Department Briefings

**Casting:** No performers. This show is entirely environmental and player-focused.

**Wardrobe:** No wardrobe work.

**Choreography:** Full aerial arc. Player starts on ground, hover begins at C2 (0–2 blocks), climbs through C3–C6 (2–14 blocks), dramatic lift at C7 to 25 blocks, pressure-release descent through fireworks at C9, returns to ground by C13. Calibrated levitation patterns in use throughout. See choreography.md for full altitude arc.

**Set:** Portable show. Standard 9-mark grid centered on player. Open outdoor space with sky access. No block modifications.

**Camera:** Player-free camera mode. FACE used at opening for initial orientation. No cinematic sequences — the player's own perspective is the camera throughout.

**Lighting:** Opens at T=8000 (mid-afternoon). Three TIME_OF_DAY steps to night by end of C1. WEATHER clear forced at T=0. Night sky remains throughout until show end.

**Sound:** Cave ambience as emotional undercurrent. Thunder and wind for presence/arrival beats. Allay and nature sounds for warmth sections. Sound arc follows altitude arc.

**Voice:** ShowSprite dialogue throughout — 3–4 lines per section maximum. Chat (MESSAGE) is the primary mode. BOSSBAR per section for structure (C1–C13, color-coded by mood). Minimal TITLE use — reserved for 1–2 peak moments only.

**Stage Manager:** No block modifications. No world-resident entities. All spawned entities use despawn_on_end: true. PLAYER_FLIGHT used — release event at show end. Safe to interrupt at any tick.
