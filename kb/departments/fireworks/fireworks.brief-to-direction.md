---
department: Fireworks Director
owner: Mira
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Fireworks — Brief to Direction

> Mira's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for all firework instrument types, preset library,
> and pattern design lives in `kb/departments/fireworks/fireworks.kb.md`.

---

## Standing Requirements

*What Mira cannot start without. Get altitude and palette wrong and it doesn't matter how good the pattern is.*

**Player altitude at each intended pyrotechnic moment.** This is non-negotiable and it comes from Effects. Altitude determines the entire experience of a firework burst: the player looking up at a burst is an audience member; the player surrounded by it at the same altitude is a participant; the player looking down at it from above is an elevated observer. All three are valid artistic choices — but they are choices, made at brief time, not discovered when the show runs wrong. Mira and Felix agree on player altitude at every pyrotechnic moment before either department authors events.

**Color and palette direction.** Warm (gold, amber — ceremonial, earned), cool (blue, cyan — wonder, strangeness), celebratory (rainbow or pastel — abundance, community), or neutral? Mira can work with any register, but she needs a direction. A show whose entire pyrotechnic arc runs in the wrong palette is not a color-timing problem — it's a brief problem.

**The emotional register of each major pyrotechnic beat.** Arrival? Finale? Punctuation after a quiet moment? Threat or menace? Each calls for a different pattern type and density. A single intimate rocket means something very different from a FIREWORK_CIRCLE crown at radius 8. The pattern is a compositional choice; Mira makes it when she knows what the beat is trying to do.

---

## Intake Questions

*Questions Mira asks at intake. She brings proposals, not problems — these are the questions that shape the proposal.*

- How many distinct pyrotechnic moments does this show have? And where do they fall in the arc — early, at the peak, at the close, or distributed throughout?
- Is there a culminating or finale pyrotechnic moment — a beat where Fireworks is the primary instrument? Mira designs the rest of the show's pyrotechnic budget around the finale so the earlier moments don't desensitize the player to it.
- Should the player feel surrounded by the fireworks (close range, low altitude) or watch them at a distance (mid-stage or overhead)? Or is the relationship meant to change across the show's arc?
- Are there any moments that should be a single punctuation mark — one rocket, one beat — rather than a pattern? Restraint is one of Mira's most powerful tools, and she uses it intentionally.
- Is there a pyrotechnic element that's never been tested at the required player altitude? Mira flags calibration risk early so a test round can be scheduled before the full show is authored.

---

## Requests

*Nice-to-haves. These prevent the most common cross-department misses.*

- Camera confirmation that orientation calls are planned for each major burst. A firework that fires while the player is looking at the ground is a wasted moment. Mira doesn't own orientation — that's Mark's — but she wants to know it's being handled before the show goes to a run sheet.
- Early warning if the required color palette isn't in the current preset library. Adding a new preset to `fireworks.yml` takes preparation time. If a show needs a cool-blue palette and the library only has warm gold and celebration pastels, Mira needs to know at the brief stage.
- The rough Fireworks timeline (which sections have bursts) shared with Sound early. Fireworks are loud. Brian accounts for pyrotechnic moments in the audio arc; surprises at run-sheet time force last-minute adjustments from both departments.

---

## Fireworks' Palette

Five instrument types, with brief register notes:

**Single rocket (FIREWORK)** — one launch, one burst, one position. The intimate use: a single rocket at close range, low power. The formal use: one gold star after a long silence. Maximum precision, minimum noise.

**Ring (FIREWORK_CIRCLE)** — N rockets in a circle. Chase reads as a rotating crown; simultaneous reads as an event horizon. Small radius (2–4 blocks): surrounded. Large radius (12–16 blocks): the sky opening. Power variation and color variation available.

**Line (FIREWORK_LINE)** — N rockets along a directional path. Sweep, wave, wall, cascade. The directionality is legible to the player. RAMP_UP power creates a visible altitude arc across the line.

**Fan (FIREWORK_FAN)** — multiple arms radiating from a center, each with its own preset, angle, count, and length. The most compositionally flexible pattern. V-shapes, X-shapes, irregular blooms. Per-arm preset variety enables multi-color asymmetric designs.

**Scatter (FIREWORK_RANDOM)** — N rockets at random positions within a radius. Simultaneous. Organic, festive, unpredictable. Fixed seed for reproducible patterns; no seed for organic variety each performance.

**Preset library** — named rocket definitions in `fireworks.yml`. Current families: Scaena brand (gold/green/white, ceremonial), Birthday Pastels (warm celebration), Pride (rainbow/trans). Full preset list: see `fireworks.yml` directly. For guidelines on when to add a new preset vs. use color_variation, see `fireworks.kb.md §The Preset Library`.

---

## What Fireworks Can and Can't Do

**Verified and working:**
- All five pattern types (single, circle, line, fan, scatter)
- Chase sequencing on circle, line, and fan — both clockwise (FL) and counter-clockwise (LF)
- Power variation: UNIFORM, RAMP_UP, RAMP_DOWN, ALTERNATE, RANDOM (available on circle and line)
- Color variation: UNIFORM, RAINBOW, GRADIENT (circle only), ALTERNATE (circle and line)
- Multi-star presets (multiple burst layers per rocket)
- Reproducible scatter patterns via seed field
- Confirmed named arrangement: `descent-through-fireworks` (archetype sampler C9 — RELEASE descent with fireworks bracketing the player's altitude range)

**Current gaps — flag at intake if they're relevant:**
- `color_variation: GRADIENT` on FIREWORK_LINE is gapped — gradient_from/to fields are not parsed on LINE events; the executor always defaults to red→blue. Use RAINBOW or ALTERNATE for multi-color line effects.
- `power_variation` and `color_variation` are not available on FIREWORK_FAN — all arm positions fire at their preset's unmodified power and colors.
- `min_clearance` field is parsed but never enforced at runtime — silently ignored.
- Preset `launch:` mode (above / random / feet) is parsed into the model but the executor never consults it. Rocket spawn position comes entirely from the event's `offset` / `y_mode` fields. The launch block has no runtime effect.

---

## Document Status

*First pass — 2026-03-28*

Core structure is complete. What's still needed:

- Named arrangements from the calibration backlog (`arrangement.punctuation`, `arrangement.crown`, `arrangement.sweep`, `arrangement.burst.joy`, `arrangement.finale.intimate`, `arrangement.finale.full`) are all proposed, not yet confirmed. Once confirmed in-game, this document should reference them by name as the standard reference patterns.
- The `descent-through-fireworks` arrangement is confirmed — it should be the first named pattern referenced here once this document grows a "Confirmed Patterns" section.
