---
show: demo.archetype_sampler
version: 2.9.0 (R7)
updated: 2026-03-25
---

# Archetype Sampler — Run Sheet · Revision 7
## `/show play demo.archetype_sampler`  ·  ~3500 ticks (~2m55s)  ·  13 cues

> **Status: Calibration / diagnostic show.** This is not a gold standard reference for any department. It exists to surface what works, what doesn't, and what needs further development. Findings inform department KBs and calibration backlogs — not to be treated as production patterns until explicitly promoted.

> Open this on a second screen while running the show in-game.
> Take notes by cue number (C1–C13). Debrief with Claude after.
> **R7 core change:** altitude arc rewritten — height withheld until C7, then earned.
> C2 hovers near ground. C3–C6 climb slowly. C7 is the moment.

---

## Environment Notes

- **Space:** Outdoor, open sky — requires 40+ blocks of vertical clearance above player
- **Ceiling height:** Open sky (none)
- **Sky:** Open
- **Ambient light level:** Starts mid-afternoon (T=8000), steps to night by end of C1
- **Key vertical positions:** Ground Y=player, hover Y+2, mid Y+7–14, peak Y+25, finale Y+35+
- **Weather at start:** Forced CLEAR at T=0
- **Time of day at start:** 8000 (mid-afternoon) — dims to night by T=440

---

## Sections

---

### C1 — Houselights Down

**Tick range:** T=0 – T=440
**Intention:** Establish presence. The world is ordinary. Then it starts to change.
**Function:** Orient the player, force clear weather, step the sky from day to night while dialogue fires during the dimming. Ground state established before anything rises.
**Mechanics:** WEATHER clear at T=0. Three TIME_OF_DAY steps dim the sky over ~280 ticks. ShowSprite messages fire while dimming is actively visible. BOSSBAR C1 (WHITE) tracks section.
**Watch question:** Does dialogue land while the sky is visibly dimming — or does the sky snap to night before the first line, making the atmosphere feel redundant?

**Notes (R7):** TIME_OF_DAY multi-step reads as two visible bumps — jarring for houselights-down. Direction note: let the sky do the work; if you must bump, bump once and coordinate it with something else happening simultaneously (sound hit, levitation, voice line) so the light change feels motivated rather than mechanical. More steps over longer intervals would read as atmospheric rather than commanded. Filed to Lighting calibration backlog.

Voice timing vs. sky dimming: not explicitly evaluated in R7 debrief — watch question remains open for R8.

---

### C2 — The Ground Beneath You

**Tick range:** T=440 – T=900 (460 ticks / ~23s)
**Altitude:** 0–2 blocks (HOVER near ground)
**Intention:** The first lift — barely perceptible. Something has changed, but the player isn't sure what.
**Function:** Introduce levitation at body level. No drama. Embers surround the player, not below them. This section must feel like gentle bubbling, not a reveal. The reveal is C7.
**Mechanics:** HOVER pattern (amp=0, lev=20t gap=8t, cycle=28t). `atmos.ambient.ember_drift` fires at body level. NO amp-9 burst. ShowSprite: 3 lines max. BOSSBAR C2.
**Watch question:** Does C2 read as "gentle bubbling"? Or does it feel like levitation has been announced? If the player is clearly floating, the drama has been spent too early.

**Notes (R7):** Generally effective — minimum levitation function. Accomplishes the cue's intention. Baseline is confirmed.
Next-step-up: initial burst to get player slightly off ground, then rapid-tempo hover pulses — as quick as possible while maintaining approximate Y (not ascending or descending). Goal: player stays airborne between pulses rather than touching ground. That continuous-airborne-near-ground feel is the true "bubbling." Filed to Effects calibration backlog.
Additional finding: levitation sensation perceived as continuing through approximately C7 with no change or motivation. The "bubbling" texture carried across multiple sections (C2–C6) without perceptual variation. Seasick-feeling if sustained too long. Needs clear perceptible variation at section boundaries — or explicit sectional contrast in levitation parameters (amplitude, gap, pattern) rather than uniform amp=0/28t across C2–C6.

YAML verified (2026-03-28): C2 is genuinely 460 ticks / ~23s. The 24s stopwatch reading is correct — not a bug. The identical amp=0 / lev=20t / gap=8t pattern runs continuously from T=440 (C2) through T=1708 (last C6 pulse) = ~64 seconds of undifferentiated levitation texture before C7 fires. This is the root of the seasick observation, not C2 alone.

---

### C3 — Separation

**Tick range:** T=900 – T=1120 (220 ticks / ~11s)
**Altitude:** 2–6 blocks (CLIMB begins)
**Intention:** The ground is leaving. Warmth is below you now.
**Function:** First real vertical movement. Warm bloom fires at/below player — the player is now *above* the warmth, not inside it. This is the first moment where altitude has spatial consequence.
**Mechanics:** CLIMB pattern (lev=24t per cycle). `atmos.lights.warm_bloom` centered below. ShowSprite: the warmth is named, not explained. BOSSBAR C3.
**Watch question:** Is the warm light visibly below the player — not surrounding them? Does the separation feel gradual, or is there a sudden jump?

**Notes:** *(filled in after in-game test)*

---

### C4 — The Cool Shift

**Tick range:** T=1120 – T=1340 (220 ticks / ~11s)
**Altitude:** ~6 blocks (HOVER)
**Intention:** A tonal change, not a spatial one. Same height; different mood.
**Function:** Cool bloom enters at player level — the emotional register shifts from warm to something quieter. This is mood, not movement. The player holds altitude while the atmosphere changes around them.
**Mechanics:** HOVER (28t cycle). `atmos.lights.cool_bloom` at/near player level. ShowSprite: brief, present-tense. BOSSBAR C4.
**Watch question:** Does the cool shift read as a distinct change from C3, or does it blur together? Is the altitude genuinely holding — no perceptible drift?

**Notes:** *(filled in after in-game test)*

---

### C5 — What Rises From Below

**Tick range:** T=1340 – T=1540 (200 ticks / ~10s)
**Altitude:** ~7–9 blocks (HOVER, slight drift up)
**Intention:** Sound arrives from below — the cave, the deep, something underneath.
**Function:** Cave ambient sound ascends toward the player from below. The player is positioned above the source of the sound. The spatial relationship (player above, sound rising) is the storytelling.
**Mechanics:** HOVER. Cave sound event (ascending from lower Y). ShowSprite: one or two lines about what's below. BOSSBAR C5.
**Watch question:** Does the cave sound feel like it's coming *up* toward the player, or just playing ambient? Is the player's position relative to the sound source legible?

**Notes:** *(filled in after in-game test)*

---

### C6 — Silent Foreshadowing

**Tick range:** T=1540 – T=1720 (180 ticks / ~9s)
**Altitude:** 9–14 blocks (CLIMB, unannounced)
**Intention:** The show is rising, but hasn't said so. No dialogue. No announcement. The player just notices they're higher.
**Function:** Silent climb from 9 to 14 blocks. No ShowSprite lines. No new cues announced. This section withholds — the player should feel something preparing without knowing what. The silence is the foreshadowing.
**Mechanics:** CLIMB (lev=24t cycles). Minimal or no particle events. BOSSBAR C6 (neutral color). ShowSprite: silent, or one cryptic line at most.
**Watch question:** Does the player notice they've climbed without being told? Does the silence feel intentional, or does it feel like nothing is happening? Is 14 blocks noticeably higher than where C2 started?

**Notes:** *(filled in after in-game test)*

---

### C7 — The Arrival  ←  *The Moment*

**Tick range:** T=1720 – T=1940 (220 ticks / ~11s)
**Altitude:** 14 → 25 blocks (AMP-9 LIFT EARNED)
**Intention:** The show's fulcrum. The player has been brought here — now they are *taken*.
**Function:** First amp-9 burst. Player lifts from 14 to 25 blocks in a single dramatic movement. Thunder lands *with* the lift — not before, not after. This is the moment every preceding section has been building toward. The player should know something has happened to them.
**Mechanics:** AMP-9 levitation event. Thunder/wind sound fires at the same tick as the lift. ShowSprite: one line, earned. BOSSBAR C7 (RED or peak color).
**Watch question:** Does the lift feel EARNED — like an arrival, not a mechanism? Does the thunder land with the physical lift (same tick)? Is 25 blocks dramatically higher than 14 — does the player feel transported?

**Notes (R7):** Lightning strike during levitation in the dark was noted as "effective — disorienting, fully pulls focus to the moment." This may correspond to C7 or the dark sections leading into it (C5–C6). Verify in YAML whether a LIGHTNING event fires at or near C7 tick, or whether this was the thunder sound event. Whether EARNED question was met not explicitly evaluated in R7 debrief — confirm in R8.

---

### C8 — Elevated Observer

**Tick range:** T=1940 – T=2160 (220 ticks / ~11s)
**Altitude:** ~25 blocks (HOVER)
**Intention:** Joy from above. The player watches the celebration below them.
**Function:** Joy/confetti cue fires *below* the player — the player observes from altitude. This is the payoff of the elevation: the player is above the spectacle, not inside it. Allay sounds, warmth, celebration — all happening below.
**Mechanics:** HOVER (28t cycle). Joy cues centered 10–15 blocks below player Y. ShowSprite: wonder or quiet joy. BOSSBAR C8 (YELLOW or joy color).
**Watch question:** Is the joy burst visibly *below* the player? Does the player feel like an elevated observer, or are they inside the effect? (If they're inside it, the altitude hasn't been used correctly.)

**Notes:** *(filled in after in-game test)*

---

### C9 — Through the Fireworks

**Tick range:** T=2160 – T=2420 (260 ticks / ~13s)
**Altitude:** 25 → 18 blocks (RELEASE — descent through fireworks)
**Intention:** Controlled descent through the spectacle. The player passes *through*, not *past*.
**Function:** Pressure-release descent pattern. Fireworks fire at y_offsets 26/20/12/24/16 — bracketing the player's descent path from 25 to 18 blocks. Player should be inside the firework volume, not watching from outside.
**Mechanics:** RELEASE pattern (amp=0, lev=20t gap=24t, cycle=44t). Fireworks with y_offsets 26/20/12/24/16 relative to anchor. ShowSprite: silent or single word. BOSSBAR C9.
**Watch question:** Is the player inside the fireworks, or passing by them? Do the y_offsets place bursts around the player as they descend from 25 to 18 blocks?

**Notes (R7):** ✅ Confirmed — player was inside the fireworks for at least a portion of the descent. y_offset placement validated. No change needed. C9–end overall noted as "very effective, fun, engaging." This is the reference pattern for descent-through-fireworks. *(Closes known issue: C9 firework y_offset direction.)*

---

### C10 — Building  ←  *Reference section from R6*

**Tick range:** T=2420 – T=2740 (320 ticks / ~16s)
**Altitude:** 18 → 8 blocks (RELEASE continuing)
**Intention:** The descent continues while something builds below. Return to the ground is not simple.
**Function:** Pressure-release descent continues from 18 to 8 blocks. Ramp cue fires — tension or anticipation building. This was the strongest section in R6. Model for spatial clarity: player descending, build happening below/around them.
**Mechanics:** RELEASE pattern. `ramp.*` cue active. ShowSprite: sparse, present. BOSSBAR C10.
**Watch question:** Does the build feel like it's growing *toward* the player as they descend? Is 8 blocks noticeably closer to the ground than 18? Does momentum feel like it's gathering?

**Notes:** *(filled in after in-game test)*

---

### C11 — Pre-Finale Rise

**Tick range:** T=2740 – T=2960 (220 ticks / ~11s)
**Altitude:** 8 → 14 blocks (CLIMB)
**Intention:** A second rise — not the same as C7, but an echo of it. The player is going up again.
**Function:** Climb from 8 to 14 blocks. Wonder cue. The player is being repositioned for the finale. ShowSprite narrates from inside the ascent.
**Mechanics:** CLIMB pattern. `mood.wonder.single` or equivalent. ShowSprite: present tense, wonder register. BOSSBAR C11 (PURPLE or wonder color).
**Watch question:** Does the climb feel distinct from C7's lift — smaller, more contemplative? Is there a sense that something is being prepared?

**Notes:** *(filled in after in-game test)*

---

### C12 — The Finale

**Tick range:** T=2960 – T=3100 (140 ticks / ~7s)
**Altitude:** 14 → 35+ blocks (AMP-9 LIFT — highest point in show)
**Intention:** The full release. Everything at once, from the highest point in the show.
**Function:** Second amp-9 lift — takes the player to 35+ blocks. Full coda cue, finale fireworks, ShowSprite final lines. Largest gesture in the show.
**Mechanics:** AMP-9 levitation. `coda.*` family cue. Full firework pattern. ShowSprite: final address to the player. BOSSBAR C12 (WHITE or finale color).
**Watch question:** Does 35+ blocks feel dramatically larger than the 25-block peak in C7? Does the finale feel like culmination — or like a repeat of C7?

**Notes:** *(filled in after in-game test)*

---

### C13 — Coda

**Tick range:** T=3100 – T=3500 (400 ticks / ~20s)
**Altitude:** Drifting down (slow_falling — show ends in the air)
**Intention:** The show ends while you are still airborne. There is no clean landing.
**Function:** Persistent `slow_falling` (set at T=380, duration 3200t) carries the player down gently as the show closes. ShowSprite final message. BOSSBAR fades. The player is left mid-air, show gone quiet. They land on their own.
**Mechanics:** slow_falling baseline active through show end. ShowSprite coda line. BOSSBAR C13 (GRAY).
**Watch question:** Does the show end feel like a deliberate close — or does it feel like the show just stopped? Is the player still airborne when the final line fires? Do they drift down gently after the show ends, or drop suddenly?

**Notes:** *(filled in after in-game test)*

---

---

---

## Revision Log

| Version | Date | Changes |
|---------|------|---------|
| 2.9.0 (R7) | 2026-03-25 | Altitude dramaturgy rewrite. Height withheld until C7. C2 hovers near ground (0–2b). C3–C6 climb slowly. C7 is the earned amp-9 lift. C8 joy burst fires below elevated player. C9 descent through fireworks. |
| 2.9.0 (R7 test) | 2026-03-28 | In-game test completed. Total runtime 2:57. C9 y_offset confirmed — inside fireworks, C9–end "very effective." Issues: C2 23s actual (460t by design — levitation texture continuous C2–C6 = ~64s undifferentiated); C12 only 7s for the finale; C13 coda long at ~20s; C1 TIME_OF_DAY multi-step bumpy; mob spawning risk in dark. Voice timing undirected. Findings filed: Effects KB (hover calibration), Voice KB (timing modes), Lighting KB (houselights-down principle), Stage Mgmt KB (mob spawning checklist). |
| 2.8.1 (R6) | 2026-03-25 | Full aerial rewrite — player aloft from C2. 68 inline levitation events. C9 redesigned: descent through fireworks from above. |
| 2.7.0 (R5) | 2026-03-24 | Per-cue BOSSBAR events (C1–C13). WEATHER clear at T=0. Levitation as primary staging. dragon growl → thunder. |

---

## Known Issues

*Open items for R8 attention. Closed items removed — see revision log.*

| Issue | Observed in | Status |
|-------|-------------|--------|
| C2–C6 levitation texture undifferentiated (~64s of same amp=0/28t pattern) — seasick | R7 debrief | Open — Effects dept; needs sectional variation or contrast at boundaries |
| C12 (Finale) is only 7s — too short for peak moment | R7 stopwatch | Open — section needs more room |
| C10 running ~16s — pacing lag region | R7 stopwatch | Open — watch in R8 |
| C13 coda ~20s — long; slight drag at end | R7 stopwatch | Open — voice edit or additional element could help |
| C1 TIME_OF_DAY multi-step reads as mechanical bumps | R7 debrief | Open — Lighting redesign pass |
| Voice timing not directed per-section — early/with/after/silent unspecified | R7 debrief | Open — Direction to specify in next voice brief |
| C7 EARNED question not explicitly evaluated | R7 debrief | Open — confirm in R8 |
| LIGHTNING event location not verified — which cue fires it? | R7 debrief | Open — verify YAML |
