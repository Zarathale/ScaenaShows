---
show_id: showcase.01
department: Lighting & Atmosphere Designer
document: Department Brief
updated: 2026-03-26
---

# Lighting — showcase.01 "The Cabinet"

## What This Department Serves

Lighting owns the atmospheric register of every space. In a rondo show that visits six distinct
locations, Lighting's job is to make each space feel like itself — not to impose a unified
lighting design across the show, but to *preserve* what each location is.

The tone brief says: "each vignette preserves its own atmosphere." Lighting is the department
that makes this real. The Still Water at dusk is different from the Nether's amber glow is
different from the near-dark void of the Suspension. Lighting holds those differences rather
than flattening them.

Three instruments: TIME_OF_DAY, WEATHER, and LIGHTNING. This show uses the first two. LIGHTNING
is not called for here — that belongs to showcase.02. Lighting exercises TIME_OF_DAY and WEATHER
across six distinct spaces.

---

## Per-Section Direction

### Show-wide time baseline
The show needs a starting TIME_OF_DAY that works for Home Base and establishes a sensible
beginning. The Home Base is visited 5–6 times. If time of day changes across vignettes (which
it will), each A-return may require a TIME_OF_DAY reset back to Home Base's established register.

Decision for intake: **What is Home Base's canonical time of day?** Recommendation: early
evening (TIME_OF_DAY ~13000) — warm, golden, the world still has color but knows something
is coming. The Allay's hovering reads better in this light than in bright day. Confirm after
Set provides the location.

### Vignette B — "The Still Water"
Set's scouting notes will specify what time of day looks best at this location. The brief
asks for dusk — TIME_OF_DAY ~13000–14000. Lighting sets this on arrival at Vignette B.

If dusk is confirmed: the water catches the light. The exploration has warmth but also the
quality of ending-day. This is the emotional register for this vignette — the player in a
beautiful place at a moment when you'd want to stay.

On departure from Vignette B: if the A-section Home Base has a different TIME_OF_DAY than
dusk, reset before or shortly after the Home Base teleport.

### Vignette C — "The Theater"
The creature theater is daylight or early afternoon — TIME_OF_DAY ~6000. The comedy reads
better in clear light: nothing hidden, everything visible, the absurdity fully exposed.

WEATHER: clear (no rain). The theater needs good sight lines and nothing atmospheric competing
with the creature behavior.

### Vignette D — "The Nether Valley"
The nether has its own ambient light — TIME_OF_DAY events have no effect in the nether dimension.
Lighting's contribution to this vignette is documentation of what the ambient light condition
IS, so other departments can make decisions around it.

Nether biome ambient light varies: basalt delta is darker, soul sand valley has its eerie blue
tones, nether wastes is orange-amber at all times. After Set confirms the biome, Lighting
documents the ambient register for this vignette. No events to author for this section.

### Vignette E — "The Suspension"
Near-dark. The fireworks must be visible — ambient light should be low enough that Mira's
bursts pop against the sky.

**If End islands:** The End has its own ambient light — perma-night equivalent. No TIME_OF_DAY
work needed; Lighting documents the existing condition.

**If high overworld:** TIME_OF_DAY must be set to night before or on arrival (TIME_OF_DAY ~18000
or deeper). Coordinate with Mira on what darkness level makes the palette she's planning most
visible. The fireworks' color palette and the ambient light level are coupled decisions.

### Vignette F — "The Contraption"
The comedy beat works in normal light — no special time of day required. Default to mid-day or
whatever the Home Base canonical time is.

WEATHER: clear. Nothing should obscure or dampen the reveal moment.

### A-section returns
Each return to Home Base after a vignette with a shifted TIME_OF_DAY requires a reset. Lighting
owns this reset. The canonical Home Base time (established at intake) fires within 60 ticks of
each A-section arrival.

The A-sections should not each feel like a different time of day — this would make Home Base
feel unstable. The reset is part of the A-section's "you're home" signal.

---

## Show-Level Constraints

- TIME_OF_DAY and WEATHER own the atmospheric register. No conflicts from other departments
  without Director sign-off.
- The nether (Vignette D) does not respond to TIME_OF_DAY. Lighting's contribution there is
  documentation only.
- LIGHTNING is not used in showcase.01 — that is showcase.02's primary instrument. Don't touch it.
- Each vignette's time and weather resets cleanly. If a vignette ends in darkness (Vignette E)
  and the next A-return is in warm evening light, the TIME_OF_DAY reset must fire before the
  player arrives at Home Base (as part of the pre-teleport sequence), or within the first few
  ticks after arrival — not delayed.
- WEATHER clear may need to be explicitly maintained if the server's natural weather would
  otherwise fire rain during the show. Consider whether a show-opening WEATHER clear event is
  needed and sustained.

---

## Intake Questions for Lighting

1. **Home Base canonical time:** Recommendation is ~13000 (early evening). Confirm after
   Set provides location — what time of day looks best there?
2. **Vignette B time:** Dusk is the brief's recommendation. Set's scouting notes should confirm
   whether dusk is what looks best at the specific location. Confirm and lock.
3. **Vignette E (if overworld):** What TIME_OF_DAY value produces sufficient darkness for
   Mira's palette? Coordinate with Mira at intake.
4. **A-section TIME_OF_DAY resets:** At what tick relative to the Home Base teleport does the
   reset fire? Before the teleport (part of the vignette's outro) or after (part of the A
   intro)? Establish a consistent pattern.
5. **WEATHER baseline:** Should a WEATHER clear event fire at show start and be sustained?
   Or managed per-vignette?

---

## Decisions
*Filled at intake.*

## Revision Notes
*Added after each in-game test.*
