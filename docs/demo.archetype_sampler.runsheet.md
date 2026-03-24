# ARCHETYPE SAMPLER — Run Sheet
## `/show play demo.archetype_sampler`  ·  ~2:25 runtime

Use this on a second screen while watching in-game.
Reference cues by number in notes. ("C3 — warm/cool contrast not legible.")

---

## CUE 1  ·  0:00  ·  HOUSELIGHTS DOWN

**Cue ID:** `world.time.night` (3-step inline sequence)
**Tags:** `standalone` `transition` `silent` `mysterious` `low`

**Intention:** The lights are literally going down — the same way houselights fade before a theatre performance begins. The sky change is the signal that something intentional is starting. The three-step sequence (dusk → deep dusk → night) is meant to feel like gradual anticipation, not an instant toggle.

**Function:** Every firework show should ideally run at night. This opening also establishes TIME_OF_DAY as a legitimate staging tool — a set change, not just a sky setting.

**Mechanics:**
- 0:00 — Sky shifts to dusk (12500)
- 0:04 — Chat: `...`
- 0:08 — Sky shifts to deep dusk (15000)
- 0:10 — Chat: *"The lights are going down."*
- 0:14 — Sky shifts to full night (18000)
- 0:16 — Chat: *"We begin."*

**Watch:** Does 3 steps feel like anticipation or just fussiness? Is the pacing between steps right — too fast, too slow? Do the messages add to the ritual or interrupt it? Would 2 steps + 2 messages work better?

**Notes:**

---

## CUE 2  ·  0:23  ·  EMBER DRIFT

**Cue ID:** `atmos.ambient.ember_drift`
**Tags:** `particle-bed` `silent` `still` `dreamy` `ethereal` `serene` `low` `breath` `transition` `ambient` `enveloping` `subtle` `moment`

**Intention:** The room is alive. End_rod particles drifting at body height should feel like embers or arcane motes — persistent, quiet, almost not-there. The goal is atmosphere as a *bed*, not as an event. If it reads correctly, you barely notice it as a discrete thing — it just makes the space feel inhabited.

**Function:** An atmosphere layer meant to run *under* other things, not stand alone. Demonstrated solo here specifically to test whether it has enough presence on its own, or whether it only works in combination. Also tests whether the particle-bed concept reads as intended at all.

**Mechanics:** End_rod particles, count 2, at 2.5-block spread, repeat every 10 ticks for 100 ticks (~5 seconds). No sound.

**Watch:** Does it read as atmosphere, or visual noise? Does it feel like something that should be running under other events, or is it too present to be a background layer? Does it hold interest for the full 5 seconds, or does it feel long?

**Notes:**

---

## CUE 3  ·  0:36  ·  WARM LIGHT WASH

**Cue ID:** `atmos.lights.warm_bloom`
**Tags:** `fireworks-light` `warm` `intimate` `tender` `low` `slow` `breath` `transition` `flash` `scatter` `radial` `subtle` `ambient`

**Intention:** Candlelight or torchlight blooming across a stage — warm, low-energy fireworks at surface level reading as *light*, not as celebration. The goal is a tonal shift in the room's palette: from cool night to warm presence. This is not a fireworks event; it's a lighting state.

**Function:** One half of the warm/cool palette pair. The primary question here is whether low-count, low-altitude fireworks can function as *atmosphere* rather than *event* — whether they register as "the room got warmer" rather than "some fireworks went off."

**Mechanics:** 3 warm-toned FIREWORK_RANDOM (preset `scae_star_warm`), radius 14, surface level +1. Instant, no sound.

**Watch:** Does it read as warm light or as a small fireworks display? Is 3 the right count — does it need more to establish a lighting state, or is the sparseness intentional? Does the effect persist long enough visually to register as "mood," or does it disappear before it lands?

**Notes:**

---

## CUE 4  ·  0:47  ·  COOL LIGHT WASH

**Cue ID:** `atmos.lights.cool_bloom`
**Tags:** `fireworks-light` `icy` `mysterious` `melancholic` `ethereal` `low` `slow` `breath` `transition` `flash` `scatter` `radial` `subtle` `ambient`

**Intention:** Moonlight, digital ambiance, cold theatre blue — the same lighting-state concept as Cue 3, but shifting the room's emotional register to something introspective, melancholy, or mysterious. The two-event structure (wide wave-star scatter, then tighter glow-ball cluster) is meant to feel like light arriving in two layers.

**Function:** The other half of the warm/cool palette pair. The critical question: is the contrast between Cues 3 and 4 *emotionally* legible in-game, or do both just read as "fireworks that look different"? If the contrast isn't readable, the palette tool doesn't work.

**Mechanics:** 3 cool FIREWORK_RANDOM wide (radius 14) then 2 glow-ball tight (radius 7), ~0.5s apart. No sound.

**Watch:** Is the shift from warm (Cue 3) to cool noticeably different in register — does it feel like *the room changed mood*? Or does it just feel like "different colored fireworks"? Does the 2-layer structure (wide then tight) add depth?

**Notes:**

---

## CUE 5  ·  0:59  ·  CAVE SOUND

**Cue ID:** `grief.sound.cave`
**Tags:** `sound-driven` `melancholic` `mysterious` `tense` `low` `still` `breath` `transition` `beat` `centered` `subtle` `ambient`

**Intention:** Cave ambient at 0.55 pitch is grief. This is the line directly from `showsprite.context.md` — the idea that a specific sound at a specific pitch establishes an emotional register without any visual support. The pitch shift is what matters: at 0.55 instead of 1.0, it becomes slow and subterranean, something heavy underneath the world.

**Function:** Tests whether sound-only cues have enough presence to be compositionally useful. Also the first representative of the grief/melancholy register — a palette the library has almost nothing in. Two Sprite messages ("Nothing to see here." / "Listen.") prime the attention before the sound fires.

**Mechanics:** Cave ambient sound, category: ambient, volume 0.35, pitch 0.55. No visuals.

**Watch:** Does the sound *establish a feeling* on its own, or does it feel like nothing happened? Is the priming ("Listen.") necessary, or would the sound land without it? Is the pitch right — does 0.55 read as melancholy, or just as a weird cave sound? Is the volume correct — present enough to register?

**Notes:**

---

## CUE 6  ·  1:04  ·  LAYERED PAUSE

**Cue ID:** *(no cue — deliberate silence with 2 timed messages)*
**Tags:** `breath` `still` `text-forward` `silent` `subtle` `transition`

**Intention:** A pause is a composition tool, not dead air. The first message (`...`) names the silence without breaking it. The second message, arriving ~5 seconds later, reframes what just happened: "The pause is also a cue." — a meta-moment that's also true. The pause sits after four consecutive quiet cues and before the dramatic arrival; its job is to reset the audience's attention and create contrast.

**Function:** Calibrating how a pause with 2 messages across 8 seconds actually feels in-game. The showsprite.context.md describes deliberate silence as one of Sprite's strongest tools. This tests whether the timing and layering of messages within silence works compositionally.

**Mechanics:**
- 1:04 — Chat: `...`
- 1:09 — Chat: `The pause is also a cue.`
*(~8s total silence, no events)*

**Watch:** Does the first message feel like rest beginning, or is it too early? Does the second message arrive as a quiet insight — or does it interrupt the silence just when it was starting to work? Would the pause be better with just one message, or none? How does it feel transitioning into Cue 7 (the dramatic arrival) immediately after?

**Notes:**

---

## CUE 7  ·  1:16  ·  DRAMATIC ARRIVAL

**Cue ID:** `mood.arrival`
**Tags:** `explosive` `flash` `dramatic` `tense` `grand` `fast` `opener` `standalone` `sound-driven` `single` `centered` `intense` `arrival` `ceremony`

**Intention:** Something arrives. The lightning strike is instantaneous visual information — impossible to miss — but the *resonance* is in the sound: thunder rolling, then a quiet ender dragon growl at 0.25 volume 4 ticks later. The growl is designed to make you feel like the lightning meant something, not just that it happened.

**Function:** Contrast after the quiet sequence (Cues 2-6). The dramatic arrival lands harder because of what preceded it — this is the payoff of 90 seconds of quiet. It's also testing whether a flash event (duration ~4 ticks of visual) can have lasting emotional weight, or whether it evaporates the moment it ends.

**Mechanics:** Lightning at anchor, thunder at 0.6/0.9, ender dragon growl at 0.25/1.9 (4 ticks later). All instant. No particles.

**Watch:** Does the flash have *resonance* — does the thunder and growl sustain the impact after the visual is gone? Or is it just "loud thing happened"? Does the contrast after the pause make it hit harder? Does the dragon growl at that pitch register as ominous or just strange?

**Notes:**

---

## CUE 8  ·  1:27  ·  JOY BURST

**Cue ID:** `fx.confetti_burst`
**Tags:** `joyful` `celebratory` `playful` `high` `fast` `peak` `release` `fireworks-heavy` `sound-accented` `flash` `scatter` `radial` `intense` `birthday` `celebration` `achievement`

**Intention:** The opposite end of the emotional spectrum from the cave sound and arrival. This is immediate, unambiguous joy — 10 birthday-ball fireworks in a tight radius at the same moment as a level-up sound. It should feel like the room just celebrated something for you. It's tight to the anchor intentionally: it should feel like *this* space, not fireworks over there.

**Function:** The peak of the joy/celebration register. After the dramatic arrival (which is tense-exciting), this should read as warm-exciting — a completely different flavor of high energy. Also tests whether the level-up sound is the right choice, or whether something else would feel less game-y.

**Mechanics:** Level-up sound (0.8/1.1) + 10 birthday-ball FIREWORK_RANDOM in radius 8, surface +2. Instant.

**Watch:** Does it read as joyful and warm, or just loud and busy? Does the tight radius (8) feel like it's happening *to you*, or does it feel like it's happening nearby? Is the level-up sound the right call — does it feel celebratory, or does it feel like you just got XP?

**Notes:**

---

## CUE 9  ·  1:37  ·  WARM RAMP

**Cue ID:** `ramp.pulse.warm`
**Tags:** `building` `ramp` `warm` `medium` `fireworks-light` `scene` `scatter` `radial` `moderate`

**Intention:** Four waves of warm fireworks, each tighter and denser than the last, over ~8.5 seconds. The intention is *felt momentum* — the sense that the room is waking up, that something is about to happen. Not "more fireworks" — that feeling of gathering pressure when the energy in a room changes.

**Function:** The only ramp cue in the current show, and the only "scene" duration cue (all others are flash or beat). Tests whether the wave-progression concept actually reads as narrative — does a 4-wave sequence over 8+ seconds communicate *arc*, or do the individual moments feel disconnected?

**Mechanics:**
- 0s: 2 fireworks, radius 16 (wide, sparse)
- ~2.75s: 3 fireworks, radius 13
- ~5.25s: 4 fireworks, radius 10
- ~7.4s: 5 fireworks, radius 8 (tighter, denser)

**Watch:** Do the 4 waves feel like a single building thing, or like 4 separate events? Does the tightening radius register as convergence — does it feel like things are drawing toward something? Is the gap between waves right (~2.75s), or does the momentum break between pulses? Should each wave be louder too, or is visual-only buildup enough?

**Notes:**

---

## CUE 10  ·  1:53  ·  WONDER SINGLE

**Cue ID:** `mood.wonder.single`
**Tags:** `reverent` `grand` `dreamy` `medium` `slow` `peak` `breath` `standalone` `fireworks-light` `sound-accented` `beat` `overhead` `single` `subtle` `revelation` `ceremony`

**Intention:** One large firework launched 18 blocks overhead. The soft ascending tone fires first, the firework launches a beat later, and then a chime arrives ~1 second after the launch — the chime is timed to land at the moment the firework would burst. The pause before and after the chime *is the point*. This is about sustained attention on a single thing, not spectacle.

**Function:** The quietest "peak" in the library — wonder rather than celebration. Coming after the ramp, it should feel like the energy gathered somewhere, then released upward and slowly. Tests whether a single rocket, given the right framing, reads as contemplative rather than underwhelming.

**Mechanics:** Ascending tone (0.7/0.7) → FIREWORK at y+18 relative (2 ticks later) → amethyst chime (0.6/1.4) at 20 ticks.

**Watch:** Does the single overhead firework feel *reverent*, or does it feel thin after the warm ramp? Does the chime land at the right moment — does it feel like a resolution to the firework, or does the timing feel off? Is looking up at one thing enough, or does this need a companion?

**Notes:**

---

## CUE 11  ·  2:04  ·  LIFT

**Cue ID:** `fx.lift_to_height`
**Tags:** `dreamy` `ethereal` `reverent` `medium` `slow` `transition` `effect-driven` `sound-accented` `moment` `single` `centered` `subtle` `revelation` `ceremony`

**Intention:** The player rises ~6-8 blocks and stays there — not as spectacle, but as perspective shift. Being elevated during a closing sequence changes what it feels like to watch the sky change. The slow_falling that follows carries them down through the entire sunrise so the landing is gradual, not sudden.

**Function:** Structural transition into the finale. The lift is a compositional tool: it ends with the player floating, which gives the houselights-up sequence something to resolve against. The player drifting back to earth during sunrise is the metaphor completing itself.

**Mechanics:** Soft tone (0.55/0.60) → levitation II (60 ticks, ~3s rise) → chime at peak (58 ticks) → slow_falling begins at 55 ticks, lasts 600 ticks (~30s). Player should be 6-8 blocks up as the sunrise begins.

**Watch:** Does the lift feel intentional and smooth, or disorienting? Is the height right — present enough to feel like perspective shift? Does the chime at peak work, or is it unnecessary? Most importantly: does floating down during the sunrise feel like a *landing*?

**Notes:**

---

## CUE 12  ·  2:08  ·  HOUSELIGHTS UP

**Cue ID:** `world.time.dawn` (3-step inline sequence) + `coda.curtain.quiet`
**Tags:** `standalone` `transition` `silent` `tender` `nostalgic` `low` `coda` `ceremony`

**Intention:** The mirror of Cue 1 — the lights coming back up as gently as they went down. Three sky states track the player's descent from height: they start floating in the dark, drift through pre-dawn, and touch down somewhere around full sunrise. The messages are the close of ShowSprite's voice for this session. `coda.curtain.quiet` fires last: the `✦ the stage is dark ✦` message + a single descending chime.

**Function:** Tests the sunrise/close timing, message count, and whether the player-floating-down-during-sunrise concept actually lands as a coherent experience. Also calibrating whether 3 messages across ~17 seconds is the right density for a closing sequence.

**Mechanics:**
- 2:08 — Sky: pre-dawn (22000)
- 2:10 — Chat: *"The stage returns."*
- 2:14 — Sky: dawn (23500)
- 2:16 — Chat: *"You were here for all of it."*
- 2:20 — Sky: sunrise (0)
- 2:22 — Chat: *"Until next time."*
- 2:24 — `✦   the stage is dark   ✦` + amethyst chime

**Watch:** Does 3 messages feel right for a close — does each one land before the next arrives? Does *"You were here for all of it."* feel meaningful or too grand? Does the player touch down near when the show ends, or is the timing off? Does the `✦` message followed by a chime feel like a clean close?

**Notes:**

---

## Quick-note shorthand

```
C2 — reads as noise not atmo
C3 — YES. warm light works
C4 — same as C3, can't tell diff
C5 — volume too low, missed it
C6 — 2nd msg interrupted, prefer just "..."
C7 — thunder works, growl too quiet
C9 — gaps between waves too long
C12 — chime timing perfect, but "you were here for all of it" too much
```

---

*Show: `demo.archetype_sampler`  ·  25 production cues in library  ·  Built 2026-03-24*
