# ARCHETYPE SAMPLER — Run Sheet  ·  Revision 5
## `/show play demo.archetype_sampler`  ·  ~2:52 runtime  ·  13 cues

Use this on a second screen while watching in-game.
Reference cues by number in notes (e.g. "C2 — couldn't see the particles until I rose into them").
Show ends while you're still airborne — you land on your own after the stage goes quiet.

---

## Key Changes in Revision 5

- **Boss bar** — per-cue progress bar replaces static header. Shows `C# — Cue Name · cue.id.path`. Color reflects mood. Progress fills over section duration.
- **Weather** — clear forced at T=0. Rain was killing particle visibility and muddying sound design.
- **Levitation as primary staging** — you are moved through vertical space across the show:
  - C2: gentle rise ~7 blocks + slow drift down through C3/C4
  - C9: brief burst into daylight fireworks (existing)
  - C11: fresh rise ~7 blocks into Wonder Single firework space
  - C12: full finale lift to ~20–25 blocks
- **C7**: dragon growl replaced with rolling distant thunder (lower pitch, quieter second hit)
- **C11**: "Look up." replaced with "[Sprite] The ceiling opens." — player is being placed into the space; no instruction needed
- **All ↳ section labels removed** — boss bar handles identity and context now

---

## C1  ·  0:00  ·  HOUSELIGHTS DOWN

**Boss bar:** `C1 — Houselights Down` (WHITE, 340t / ~17s)

**Type:** PRE environment + sky snap + ritual messages

**What happens:**
- 0:00 — Music killed. Weather forced clear. Sky snaps to dusk (13500).
- 0:00 — Chat: `demo.archetype_sampler v5  ·  minimap off — Xaero's: F8`
- 0:01 — Chat: `...`
- 0:06 — Chat: *"The lights are going down."*
- 0:14 — Chat: *"We begin."*

**Watch:**
- Does the version line + minimap prompt feel skimmable? Is it actionable in time (you have ~6 seconds before the first Sprite line)?
- Single sky snap — does it feel like a deliberate theatrical choice, or abrupt?
- Was there rain when the show started? If so, did the WEATHER clear work? If it was already clear, note that too.

**Notes:**

---

## C2  ·  0:19  ·  EMBER DRIFT  *(three-voice + gentle levitation)*

**Cue ID:** `atmos.ambient.ember_drift` (fired three times)
**Boss bar:** `C2 — Ember Drift · atmos.ambient.ember_drift` (WHITE, 460t / ~23s)
**Tags:** `particle-bed` `still` `breath` `dreamy` `elevated`

**What happens:**
- **Beat 1** (0:19): MESSAGE alone — *"Something settles around you."* — silence as particles arrive
- **Beat 1** (0:23): Levitation begins. You rise ~7 blocks over the next 3 seconds.
- **Beat 2** (0:24): ACTION_BAR alone — *"let it settle."* — 4 seconds, nothing else competing
- **Beat 3** (0:28): MESSAGE — *"Nothing here is asking for your attention."* — then ACTION_BAR — *"you noticed anyway."*
- **Beat 4** (0:34): CUE only. Deliberate silence.
- **Beat 5** (0:37): MESSAGE — *"Stay as long as you need."*
- (0:42): C3 begins — you are still elevated and slowly drifting down

**Staging note:** The slow_falling from this rise carries you through C3 and C4. You'll be elevated (~7 blocks descending to ~3 blocks) while the warm and cool light cues fire. This is intentional.

**Watch:**
- Do you notice the levitation beginning during the ember drift? Does the upward drift feel like part of the atmosphere, or like an imposed effect?
- Are the particles denser and more present than before (count 8, taller spread)? Do you see them from both ground-level and elevated positions?
- Does the ACTION_BAR register at the bottom of screen? Note whether you saw each one.
- Beat 4 silence — does it feel like a deliberate breath, or like something was supposed to happen?
- Does the three-voice text feel layered, or cluttered?

**Notes:**

---

## C3  ·  0:42  ·  WARM LIGHT WASH  *(elevated)*

**Cue ID:** `atmos.lights.warm_bloom`
**Boss bar:** `C3 — Warm Light · atmos.lights.warm_bloom` (YELLOW, 220t / ~11s)
**Tags:** `fireworks-light` `warm` `intimate` `low` `elevated`

**What happens:**
- 0:42 — Chat: *"Warmth at ground level."*
- 0:46 — CUE fires. 3 warm fireworks, surface level +1.
- 0:46 — Soft guitar note at low volume (new in R5 — helps the light shift register)

**Staging note:** You are elevated from the C2 lift (~6-7 blocks when C3 starts). The warm light blooms below and around you.

**Watch:**
- From elevation, does the warm bloom read as environmental light or as small fireworks at your feet?
- Does the guitar accent help the light shift register, or is it inaudible/unnecessary?
- Has the staging change (elevated vs. grounded) changed how this cue reads compared to R4?

**Notes:**

---

## C4  ·  0:53  ·  COOL LIGHT WASH  *(elevated descent)*

**Cue ID:** `atmos.lights.cool_bloom`
**Boss bar:** `C4 — Cool Light · atmos.lights.cool_bloom` (BLUE, 220t / ~11s)
**Tags:** `fireworks-light` `icy` `mysterious` `low` `elevated descent`

**What happens:**
- 0:53 — Chat: *"The room shifts register."*
- 0:57 — CUE fires. Wide scatter (radius 14) then tight glow cluster (radius 7).
- 0:57 — Soft harp note at low volume (new in R5 — contrasts with C3's guitar)

**Staging note:** You're still elevated (~3-5 blocks) and descending. The slow_falling from C2 expires around mid-C5 (~1:15).

**Watch:**
- Is the warm-to-cool shift emotionally legible from elevation? Does it feel like the room changed, or like different-colored effects below you?
- Did the harp accent land differently from the guitar in C3? Together, do they create a warm/cool contrast in sound as well as visuals?

**Notes:**

---

## C5  ·  1:04  ·  CAVE SOUND  *(near ground)*

**Cue ID:** `still.sound.cave`
**Boss bar:** `C5 — Cave Sound · still.sound.cave` (PURPLE, 200t / ~10s)
**Tags:** `sound-driven` `melancholic` `mysterious` `no visuals`

**What happens:**
- 1:04 — Chat: *"Nothing to see here."*
- 1:07 — Chat: *"Listen."*
- 1:09 — CUE: cave ambient, volume 0.60, pitch 0.55 (low, slow, subterranean)

**Staging note:** You may still be slightly elevated (slow_falling expires ~1:15), but near ground. Weather is clear and music was killed at T=0 — the ambient channel has clean space.

**Watch:**
- Does the pitch-shifted cave sound establish a feeling, or is it too subtle even now?
- Does the environmental clarity (no rain, no background music) make a difference vs. R4?
- Does "Listen." feel necessary — would the sound land without the priming?

**Notes:**

---

## C6  ·  1:14  ·  PAUSE

**Boss bar:** `C6 — Pause` (WHITE, 180t / ~9s)
**Tags:** `breath` `silent` `transition`

**What happens:**
- 1:14 — Chat: `...`
- *(9 seconds of silence — cave ambient may still be fading)*
- 1:23 — C7 arrives

**Watch:**
- Does `...` feel like rest beginning, or too early?
- Does the silence feel intentional and weighty, or like dead air?
- How does C7 hit after that much quiet?

**Notes:**

---

## C7  ·  1:23  ·  DRAMATIC ARRIVAL

**Cue ID:** `mood.arrival`
**Boss bar:** `C7 — Dramatic Arrival · mood.arrival` (RED, 220t / ~11s)
**Tags:** `explosive` `flash` `dramatic`

**What happens:**
- 1:23 — Chat: *"Something arrives."*
- 1:26 — CUE: Lightning + thunder (0.6 / 0.9)
- 1:26 — 0.3s later: second thunder hit, softer, pitch 0.55 (the strike rolling away)

**Note:** Dragon growl removed in R5. Replaced with second thunder at lower pitch — sounds like the sound traveling across the sky after the strike.

**Watch:**
- Does the two-hit thunder (initial strike → rolling echo) feel resonant, or just like the same sound twice?
- Is the rolling-echo effect legible — does the second hit feel like the strike fading, or is it confusing?
- Does the flash still consistently land as the show's strongest visual surprise at this point?

**Notes:**

---

## C8  ·  1:34  ·  JOY BURST

**Cue ID:** `fx.confetti_burst`
**Boss bar:** `C8 — Joy Burst · fx.confetti_burst` (PINK, 220t / ~11s)
**Tags:** `joyful` `celebratory` `playful` `high` `fast`

**What happens:**
- 1:34 — Chat: *"Yes."*
- 1:37 — CUE: level-up sound (0.8/1.1) + 10 birthday-ball fireworks, radius 8, surface +2

**Watch:**
- Does it read as joyful and warm, or loud and busy?
- Does the tight radius feel like it's happening *to you*, or nearby?
- No staging changes in R5 — same as R4. Any notes for comparison?

**Notes:**

---

## C9  ·  1:45  ·  DAYLIGHT BURST  *(surprise section — elevated)*

**Boss bar:** `C9 — Daylight Burst` (YELLOW, 260t / ~13s)
**Tags:** `time-snap` `STOP_SOUND` `rainbow` `wry`

**What happens:**
- 1:45 — Snap to noon (6000) + dragon flap hit
- 1:45 — Levitation II begins (amp 1, 40t / ~2s → ~5 blocks)
- 1:45–1:49 — Five firework waves (see R4 run sheet for detail — unchanged)
- 1:47 — Slow_falling begins (280t / ~14s descent)
- 1:53 — Snap back to night. Cave ambient re-grounds.
- 1:55 — Chat: *"Dark again. As if nothing happened."*

**Watch:**
- Unchanged from R4 except weather is now clear. Does the burst hit differently with guaranteed clear sky?
- Does the brief levitation into the burst feel like intentional staging, or still goes unnoticed?

**Notes:**

---

## C10  ·  1:58  ·  WARM RAMP  *(elevated → grounded)*

**Cue ID:** `ramp.pulse.warm`
**Boss bar:** `C10 — Warm Ramp · ramp.pulse.warm` (YELLOW, 320t / ~16s)
**Tags:** `building` `ramp` `warm` `scene` `scatter`

**What happens:**
- 1:58 — Chat: *"Something building."*
- 2:01 — CUE: 4 waves of warm fireworks, each closer and denser (~8.5s total)

**Staging note:** You're descending from C9 levitation — roughly 2 blocks elevated at start, near ground by first or second wave. The ramp builds as you settle.

**Watch:**
- Do the 4 waves feel like a single building thing or 4 separate events? Does momentum hold between pulses?
- Does experiencing the first waves slightly elevated change anything vs. R4 (all from ground)?
- Does the tightening radius register as convergence?

**Notes:**

---

## C11  ·  2:14  ·  WONDER SINGLE  *(elevated into firework space)*

**Cue ID:** `mood.wonder.single`
**Boss bar:** `C11 — Wonder Single · mood.wonder.single` (PURPLE, 220t / ~11s)
**Tags:** `reverent` `overhead` `elevated` `subtle`

**What happens:**
- 2:14 — Levitation II begins (amp 1, 60t / ~3s → ~7-8 blocks)
- 2:14 — Chat: *"The ceiling opens."*
- 2:17 — slow_falling begins (220t / ~11s)
- 2:17 — CUE: ascending tone → firework at y+18 (2 ticks later) → amethyst chime at 20t

**Staging note:** You're rising as the firework launches. Peak height (~7-8 blocks) is roughly within the visual range of the burst at y+18. The firework is overhead and close, not a tiny dot.

**Watch:**
- Is the elevation legible — do you feel placed *into* the firework space rather than watching from below?
- Does the chime land at the right moment — the top of the burst?
- Does *"The ceiling opens."* land, or feel detached from what's happening?
- Compare to R4 "Look up." — is non-directive narration + movement better than instruction?

**Notes:**

---

## C12  ·  2:25  ·  LIFT TO HEIGHT

**Cue ID:** `fx.lift_to_height`
**Boss bar:** `C12 — Lift to Height · fx.lift_to_height` (WHITE, 140t / ~7s)
**Tags:** `dreamy` `ethereal` `reverent` `revelation`

**What happens:**
- 2:25 — Chat: *"One more thing."*
- 2:28 — CUE: soft tone (0.55/0.60) + Levitation III (amp 2, 120t / ~6s → ~20-25 blocks)
- ~2:34 — Peak. Amethyst chime (quiet, 0.40/1.30).
- slow_falling: 800 ticks (40s) — carries through entire closing sequence and landing

**Note:** C12 unchanged from R4. Remains the strongest spatial moment.

**Watch:**
- Does the new height feel genuinely different from the C11 lift — a clear step up?
- Does the show's vertical arc (C2 gentle rise → C9 brief burst → C11 pre-lift → C12 full lift) feel like a logical progression, or disconnected?
- Does the sky brightening mid-ascent (C13 snap) feel like it arrives with the altitude?

**Notes:**

---

## C13  ·  2:32  ·  DESCENT + CODA

**Cue ID:** `coda.curtain.quiet` (at end)
**Boss bar:** `C13 — Descent · coda` (WHITE, 360t / ~18s, slow fade)
**Tags:** `transition` `tender` `nostalgic` `coda`

**What happens:**
- 2:32 — Sky snaps to 22500 (horizon glow) — you're still rising
- ~2:34 — Peak. Chime from C12.
- 2:36 — Chat: *"The stage returns."*
- 2:41 — Chat: *"You were here for all of it."*
- 2:47 — Chat: *"Until next time."*
- 2:50 — `coda.curtain.quiet` fires — show ends here, in the air
- *(you continue drifting down — no further events)*

**Watch:**
- Does the sky brightening mid-ascent feel like it arrives with the altitude, or goes unnoticed?
- Do the three narration lines feel correctly spaced during descent? Does each land before the next?
- Does *"You were here for all of it."* feel meaningful or too grand?
- How long after the coda do you actually land? Does the drift feel earned?
- Does the show ending IN THE AIR feel like an artistic choice or unfinished?

**Notes:**

---

## Quick-note shorthand

```
C1   — weather: was already clear / cleared by show / no rain noticed
C1   — version line: saw it / missed it / too long / just right
C1   — sky snap: deliberate / felt abrupt
C2   — levitation: felt it / didn't notice / too aggressive for the moment
C2   — particles: denser / still sparse / visible from air / visible from ground
C2   — ACTION_BAR: saw both / saw one / missed both
C2   — Beat 4 silence: breath / dead air
C3   — warm light from elevation: reads as light / reads as fireworks below me
C3   — guitar accent: heard it / didn't notice / right call / unnecessary
C4   — warm→cool contrast: emotionally legible / just different colors
C4   — harp accent: heard it / didn't notice
C5   — cave sound: felt something / missed it / volume right / still quiet
C6   — silence: intentional weight / dead air
C7   — rolling thunder: resonant / sounds like same sound twice / confused
C7   — flash still lands: yes / feels weaker now with thunder change
C8   — joy burst: warm-exciting / too loud / level-up sound right / off
C9   — noon snap: right / too jarring / different with clear weather
C9   — levitation: noticed / didn't notice / felt right
C10  — waves: connected / separate / gaps too long / elevation during first waves: matters / doesn't
C11  — ceiling opens: narration landed / felt detached
C11  — elevation: felt placed in firework space / still too low / didn't notice lift
C11  — chime: right timing / not needed
C12  — arc progression: logical / disconnected / this is clearly the peak
C12  — height: genuinely different perspective / still similar to C11
C13  — narration spacing: right / too fast / too slow
C13  — show ends in air: intentional / unfinished
C13  — "you were here for all of it": meaningful / too grand
C13  — landing time: too long / too short / about right
```

---

*Show: `demo.archetype_sampler`  ·  v2.7.0  ·  25 production cues in library  ·  Revision 5, 2026-03-24*
