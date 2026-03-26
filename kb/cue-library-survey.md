---
status: out-of-date
created: 2026-03-24
owner: Alan
notes: >
  Baseline survey from SCENA-004. Mapped existing cues against the §10 tag taxonomy
  and identified coverage gaps. Superseded by ongoing cue library growth — use as
  historical reference only; not current inventory.
---

# Cue Library Survey — Phase 8 Baseline

This document maps the current cue library, identifies gaps against the target arc-role
coverage, and proposes the expansion plan to reach 30+ production cues.

Run a new survey pass any time the library grows by 10+ cues or a show authoring session
reveals a repeated "nothing fits" moment.

---

## Current Inventory

**Production cues: 8**
**Test cues: 4** (not counted toward the 30-cue target)
**Total: 12**

---

## Production Cue Map

### `atmos.ambient.ember_drift`
> Slow-drifting end_rod particles. Background atmosphere during quiet or transitional moments.

| Dimension | Tags |
|---|---|
| Emotional tone | `dreamy` `ethereal` `serene` |
| Energy | `low` |
| Pace | `still` |
| Arc position | `breath` `transition` `ambient` |
| Effect character | `particle-bed` `silent` |
| Duration class | `moment` (~100 ticks / 5s) |
| Pattern type | `ambient` `enveloping` |
| Intensity | `subtle` |
| Context | `ambient` |

**Notes:** No `tags:` field yet — needs retroactive tagging. One of the most versatile
cues in the library. Works as a persistent bed under narration.

---

### `atmos.lights.warm_bloom`
> Three warm FIREWORK_RANDOM at low altitude. Candlelight or torchlight reading.

| Dimension | Tags |
|---|---|
| Emotional tone | `warm` `intimate` `tender` |
| Energy | `low` |
| Pace | `slow` |
| Arc position | `breath` `transition` |
| Effect character | `fireworks-light` |
| Duration class | `flash` (instant scatter) |
| Pattern type | `scatter` `radial` |
| Intensity | `subtle` |
| Context | `ambient` `celebration` |

**Notes:** No `tags:` yet. The warm/cool bloom pair is well-positioned as the
emotional lighting palette — two cues doing a lot of tonal work.

---

### `atmos.lights.cool_bloom`
> Cool-toned FIREWORK_RANDOM scatter. Moonlight, digital ambiance, cold theatre blue.

| Dimension | Tags |
|---|---|
| Emotional tone | `mysterious` `melancholic` `ethereal` `icy` |
| Energy | `low` |
| Pace | `slow` |
| Arc position | `breath` `transition` |
| Effect character | `fireworks-light` |
| Duration class | `flash` (~10 ticks — two staggered events) |
| Pattern type | `scatter` `radial` |
| Intensity | `subtle` |
| Context | `ambient` |

**Notes:** No `tags:` yet. The only cue that gestures at melancholy/mystery.
Needs companions — the cool palette has almost no depth in the current library.

---

### `fx.confetti_burst`
> SOUND + FIREWORK_RANDOM x10 in tight radius. Celebratory, immediate, festive.

| Dimension | Tags |
|---|---|
| Emotional tone | `joyful` `celebratory` `playful` |
| Energy | `high` |
| Pace | `fast` |
| Arc position | `peak` `release` |
| Effect character | `fireworks-heavy` `sound-accented` |
| Duration class | `flash` (instant) |
| Pattern type | `scatter` `radial` |
| Intensity | `intense` |
| Context | `birthday` `celebration` `achievement` |

**Notes:** No `tags:` yet. Currently the only "peak joy" cue. Works well but
needs companions — a `peak` sustained over time, not just a single burst.

---

### `fx.levitate_and_drift`
> Levitation → slow_falling sequence. ~7 seconds total air time.

| Dimension | Tags |
|---|---|
| Emotional tone | `dreamy` `ethereal` `reverent` |
| Energy | `medium` |
| Pace | `slow` |
| Arc position | `peak` `transition` |
| Effect character | `effect-driven` `sound-accented` |
| Duration class | `moment` (~140 ticks / 7s) |
| Pattern type | `single` (player effect) |
| Intensity | `moderate` |
| Context | `revelation` `ceremony` |

**Notes:** No `tags:` yet. The only effect-driven cue in the library. Unique
because it acts on the *player's body* rather than the world around them.

---

### `mood.arrival`
> Lightning flash + thunder + quiet ender dragon. Dramatic entrance.

| Dimension | Tags |
|---|---|
| Emotional tone | `dramatic` `tense` `grand` |
| Energy | `explosive` |
| Pace | `fast` |
| Arc position | `opener` `standalone` |
| Effect character | `sound-driven` |
| Duration class | `flash` (~4 ticks) |
| Pattern type | `single` `centered` |
| Intensity | `intense` |
| Context | `arrival` `ceremony` |

**Notes:** No `tags:` yet. The most dramatic cue in the library — but single-use
by nature. Needs a softer `arrival` variant and a sustained `tension` companion.

---

### `mood.wonder.single`
> Single firework high overhead + ascending tone. The pause is the point.

| Dimension | Tags |
|---|---|
| Emotional tone | `reverent` `grand` `dreamy` |
| Energy | `medium` |
| Pace | `slow` |
| Arc position | `peak` `breath` `standalone` |
| Effect character | `fireworks-light` `sound-accented` |
| Duration class | `beat` (~20 ticks, but designed for a longer held moment) |
| Pattern type | `overhead` `single` |
| Intensity | `subtle` |
| Context | `revelation` `ceremony` |

**Notes:** No `tags:` yet. The only "wonder" cue. Works beautifully but is
the only option in that emotional register. Needs scale variation (small/large).

---

### `overture.theme_teaser`
> 600-tick all-capability showcase. Every event type in 30 seconds.

| Dimension | Tags |
|---|---|
| Emotional tone | `grand` `triumphant` `celebratory` |
| Energy | `explosive` |
| Pace | `building` |
| Arc position | `opener` `standalone` |
| Effect character | `fireworks-heavy` `text-forward` `sound-driven` |
| Duration class | `act` (600 ticks / 30s) |
| Pattern type | `circle` `line` `fan` `scatter` `overhead` |
| Intensity | `overwhelming` |
| Context | `celebration` `ceremony` |

**Notes:** No `tags:` yet. Not designed to be a reusable component — it is a
show-within-a-show. Its emotional arc is complete on its own. Rarely composable
into other shows.

---

## Gap Analysis

### Coverage by arc role

| Arc role | Target | Current | Gaps |
|---|---|---|---|
| `opener` / `arrival` | Strong | `mood.arrival`, `overture.theme_teaser` | Missing: soft arrival, reverent opener |
| `ramp` / `buildup` | Strong | ❌ none | Full gap — no building momentum cues at all |
| `peak` | Strong | `fx.confetti_burst` (joy), `mood.wonder.single` (awe) | Missing: sustained peak, grand salvo, player-body climax |
| `breath` / `transition` | Strong | `atmos.ambient.ember_drift`, warm/cool bloom | Reasonable — could use a text-forward pause marker |
| `release` | Moderate | `fx.confetti_burst` | Missing: a pure `release` that isn't also a `peak` |
| `coda` / `resolution` | Strong | ❌ none | Full gap — nothing to land a show |
| `tension` | Moderate | Implicitly: `mood.arrival` | Missing: sustained tension (not a flash) |
| `wonder` | Strong | `mood.wonder.single` | Understocked — needs scale variants |
| `celebration` (sustained) | Moderate | `fx.confetti_burst` | Needs multi-beat celebration sequences |
| `grief` / `melancholy` | Moderate | `atmos.lights.cool_bloom` (weak) | Near-total gap — the cave ambient line in showsprite.context.md has no cue yet |
| World (time/weather) | Moderate | ❌ none | No TIME_OF_DAY or WEATHER cues at all |
| Stage (movement) | Low | `fx.levitate_and_drift` | No FACE, CROSS_TO, RETURN_HOME cues |

### Coverage by emotional tone

**Well-served:** `dreamy`, `ethereal`, `warm`, `joyful`, `celebratory`, `grand`

**Under-served:** `tense`, `melancholic`, `nostalgic`, `intimate`, `triumphant`, `serene`

**Absent:** `grief`, `mysterious` (only barely), `reverent` (only one cue)

**Added 2026-03-25:** `whimsical` now served by Gracie plink cues; `ominous` now served by `gracie.chord.low_ominous`

### Tagging status

No production cue has a `tags:` field. This is a retroactive task — existing cues
need tags added before they can be surfaced by tag query in ShowSprite authoring.

The four test cues use a namespaced format (`effect:fireworks-heavy`) that diverges
from the spec §10 free-form approach. When tagging production cues, use plain free-form
strings per spec: `tags: [dreamy, ethereal, breath, particle-bed, subtle, moment]`.

---

## Expansion Plan — 30 New Cues

Organized by emotional function. Each cue is described by purpose first, mechanics second.

---

### RAMP — Buildup (4 cues)

These cues build energy toward a moment. They should feel like momentum, not decoration.

| ID | Description |
|---|---|
| `ramp.pulse.warm` | Slow repeating FIREWORK_RANDOM at moderate density, warm palette — the room begins to come alive |
| `ramp.pulse.cool` | Same pattern, cool palette — melancholy or mystery building |
| `ramp.sound.build` | Layered sound events in ascending pitch — the music swells before the image does |
| `ramp.particle.gather` | End_rod particles at increasing density, closing in — something is about to happen |

---

### PEAK — Climax (5 cues)

A show's peak should feel earned. These land the moment that was built toward.

| ID | Description |
|---|---|
| `peak.grand_salvo` | FIREWORK_CIRCLE + FIREWORK_RANDOM + FIREWORK_FAN simultaneously — full vocabulary, maximum output |
| `peak.sky_burst` | Single FIREWORK very high + wide FIREWORK_CIRCLE overhead — scale above the player |
| `peak.lightning_salvo` | Three rapid lightning strikes, staggered, with layered thunder — weather as spectacle |
| `peak.title_gold` | TITLE event in gold/white + fanfare sound — text-forward climax for narrative moments |
| `peak.player_ascent` | Enhanced variant of `fx.levitate_and_drift` — fireworks fire during the lift |

---

### CODA — Resolution (4 cues)

A coda does not end things — it hands them back. The stage returns to the player.

| ID | Description |
|---|---|
| `coda.curtain.quiet` | Dark-gray ✦ message + soft amethyst chime — the stage goes quiet |
| `coda.ember_last` | Single PARTICLE event, very sparse — the last ember drifts away |
| `coda.sound.resolve` | Two descending tones + long silence — the music finishes |
| `coda.bloom_fade` | Cool bloom at reduced count — the lights dim slowly |

---

### TENSION — Unease (3 cues)

Tension is not drama. It is the space before something happens, held too long.

| ID | Description |
|---|---|
| `tension.drone` | Ender dragon growl at very low volume (0.15), no visuals — subterranean unease |
| `tension.flicker` | Lightning at `{x:8,z:4}` with no thunder sound — something in the peripheral |
| `tension.sound.low` | Cave ambient sound at 0.55 pitch — the line from showsprite.context.md. Grief lives here too. |

---

### CELEBRATION — Sustained (4 cues)

Celebration over time. Not a single burst — a sequence the audience lives inside.

| ID | Description |
|---|---|
| `celebration.fanfare.warm` | FIREWORK_FAN three-arm spread, warm palette — triumphant and directed |
| `celebration.shower.random` | Extended FIREWORK_RANDOM, generous count, wide radius — player is inside the fireworks |
| `celebration.salvo.pride` | Rainbow palette FIREWORK_CIRCLE + scatter — joyful, inclusive, full spectrum |
| `celebration.cheer.sound` | Layered sounds: levelup + challenge_complete + chime — sound-only celebration marker |

---

### WONDER — Awe (4 cues)

Wonder is quiet. The biggest mistake is to make it loud.

| ID | Description |
|---|---|
| `wonder.overhead.slow` | Two large fireworks, widely spaced in time — the second arrives when you've stopped expecting it |
| `wonder.overhead.grand` | A FIREWORK_CIRCLE at high altitude — the sky becomes the stage |
| `wonder.particle.bloom` | Dense particle bloom from center outward — something blooming in the world |
| `wonder.sound.chime` | Three ascending amethyst chimes, spaced deliberately — sound as architecture |

---

### GRIEF — Melancholy (4 cues)

Grief cues must earn their weight. These are the most demanding to author correctly.

| ID | Description |
|---|---|
| `grief.sound.cave` | Cave ambient at 0.55 pitch, low volume — this is what melancholy sounds like here |
| `grief.particle.ash` | Dark gray particles drifting down — the world is letting something go |
| `grief.message.still` | `<dark_gray>...</dark_gray>` followed by silence — deliberate emptiness |
| `grief.bloom.cold` | Minimal cool_bloom, single event, half the usual count — light in a dark room |

---

### BREATH — Space (3 cues)

Breath cues are the punctuation of a show. They give the audience time to feel what just happened.

| ID | Description |
|---|---|
| `breath.pause.dark` | `<dark_gray>...</dark_gray>` message only — 0 events, pure timing anchor |
| `breath.sound.ambient` | Very soft, long-decay ambient sound — the room hums |
| `breath.ember.single` | One-shot PARTICLE, 5 count — a single breath of atmosphere |

---

### WORLD — Time and Weather (4 cues)

World cues change the stage itself. Use once per show, at a defining moment.

| ID | Description |
|---|---|
| `world.time.night` | TIME_OF_DAY to midnight (18000) — darkness as a set change |
| `world.time.dawn` | TIME_OF_DAY to dawn (23000) — the world wakes |
| `world.time.golden` | TIME_OF_DAY to golden hour (~12000) — warmth and late afternoon |
| `world.weather.rain` | WEATHER rain — the ceiling drops; intimacy or grief |

---

## Expansion Priority Order

These gaps are blocking show authoring most immediately:

1. **`coda.*` cues** — every show being written has no clean ending tool
2. **`ramp.*` cues** — buildup is the most compositionally useful family; nothing exists
3. **`grief.*` cues** — the cave-ambient line in the context doc has no cue; this is a named gap
4. **`world.time.*` + `world.weather.rain`** — shows cannot shift the world's palette without these
5. **Retroactive `tags:` on all 8 production cues** — needed before ShowSprite can surface them by function

---

## Next Steps

- [x] Add `tags:` to all 8 production cues (retroactive, non-breaking) — **done 2026-03-24**
- [x] Write `coda.*` family (4 cues) — `coda.curtain.quiet`, `coda.ember_last`, `coda.sound.resolve`, `coda.bloom_fade` — **done 2026-03-24**
- [x] Write `ramp.*` family (4 cues) — `ramp.pulse.warm`, `ramp.pulse.cool`, `ramp.sound.build`, `ramp.particle.gather` — **done 2026-03-24**
- [x] Write `grief.*` family (4 cues) — `grief.sound.cave`, `grief.particle.ash`, `grief.message.still`, `grief.bloom.cold` — **done 2026-03-24**
- [x] Write `world.time.*` + `world.weather.rain` (4 cues) — **done 2026-03-24**
- [ ] Write `peak.*` family (5 cues) — grand salvo, sky burst, lightning salvo, title moment, player ascent
- [ ] Write `celebration.*` family (4 cues) — fanfare, shower, salvo, sound-only cheer
- [ ] Write `wonder.*` family (4 cues) — overhead variants, particle bloom, chime layers
- [ ] Write `tension.*` family (3 cues) — drone, flicker, sound
- [ ] Write `breath.*` family (3 cues) — pause marker, ambient sound, single ember
- [ ] Write `world.weather.clear` companion to `world.weather.rain`
- [ ] Test all new cues in-game; mark status in this document
- [ ] Update this survey at 30+ production cues

---

## Current Count (2026-03-24)

| Category | Cues | IDs |
|---|---|---|
| atmos | 3 | ember_drift, warm_bloom, cool_bloom |
| coda | 4 | curtain.quiet, ember_last, sound.resolve, bloom_fade |
| fx | 2 | confetti_burst, levitate_and_drift |
| gracie | 5 | glissando.dreamy, glissando.accent, chord.low_ominous, plink.fourth, plink.fifth |
| grief | 4 | sound.cave, particle.ash, message.still, bloom.cold |
| mood | 2 | arrival, wonder.single |
| overture | 1 | theme_teaser |
| ramp | 4 | pulse.warm, pulse.cool, sound.build, particle.gather |
| world | 4 | time.night, time.dawn, time.golden, weather.rain |
| **Total production** | **29** | — |
| test (excluded) | 4 | — |

**Still needed to reach 30+ production:** 1+ more cue. Remaining families (peak, celebration, wonder, tension, breath) cover 19 planned cues — well beyond target.

**Note on `gracie.*` cues:** These are sound-only gesture cues authored by Gracie the Harpist (Sound department). They are composable punctuation and accent tools — they do not belong to a show arc role in the traditional sense, but serve as gesture vocabulary for the live sound team. See `kb/departments/sound.kb.md §Sound Department Personnel` for Gracie's full gesture reference.

---

*Survey baseline: 2026-03-24. Updated 2026-03-25 after Gracie the Harpist defined (24→29 production cues).*
