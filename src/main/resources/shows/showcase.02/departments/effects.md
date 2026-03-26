---
show_id: showcase.02
department: Effects Director
document: Department Brief
updated: 2026-03-26
---

# Effects — showcase.02 "The Long Night"

## What This Department Serves

Effects has one peak contribution in this show: the physical sensation of the LIGHTNING strike.

This is a show about atmosphere, patience, and one irreversible event. Effects' full arsenal
(levitation, slow_falling, particle effects, status effects) is available — but the show's
restraint means most of it should not be used. The physical experience of 10 minutes of
patient atmospheric buildup should not be cluttered with sensory interventions.

The exception is the strike. At Section D, when LIGHTNING fires, Effects may introduce one
brief disorientation: darkness for 2–3 seconds, slow_falling for 2–3 seconds, or a brief
combined effect. The show direction gives explicit permission for this — "if it serves the
lightning's physical weight." The question Effects must answer at intake: **what, specifically,
serves the strike's physical weight without competing with it?**

---

## Per-Scene Direction

### A — Dusk Arrival
No effects. The player arrives and orients. Sensation should be neutral — the world as it is.

### B — The Night Deepens
No effects. The deepening is atmospheric (Lighting, Sound). Physical sensation would
intrude on the patience of this section.

### C — Something Builds
No effects. The building pressure is environmental — WEATHER, Sound, the player's accumulated
sense of something coming. Adding physical sensation to this section (nausea, slow, darkness)
would over-announce the storm before it arrives.

One narrow exception: if the chosen location has particular environmental features (water
nearby, specific biome), a particle effect that reinforces the pre-storm quality could work.
This is not a directive — Effects should assess after Set confirms the location and bring a
proposal to intake only if something genuinely serves the moment.

### D — The Storm Breaks
**The LIGHTNING tick.** Effects' primary contribution.

The show direction gives this range: "one brief disorientation — slow_falling or darkness for
2–3 seconds maximum." Options:

**Option A — Darkness only:**
`EFFECT darkness duration:40 amplifier:1` — 2 seconds of sudden visual blackout at the strike.
The flash illuminates the world, then darkness. Then recovery. Physically this reads as: the
strike was overwhelming. The world went away for a moment.

**Option B — Slow_falling only:**
`EFFECT slow_falling duration:60 amplifier:0` — 3 seconds of weightlessness. No visual
change. The physical sensation of gravity loosening — the body's response to shock.
This is more subtle and stranger than darkness.

**Option C — Brief particle:**
A `PARTICLE` event at the player's location or at the strike coordinates — a flash of sparks,
a brief visual disturbance. Less body-sensation, more world-event.

**Option D — None:**
The LIGHTNING itself is visually and sonically sufficient. The flash, the thunder, the
world-illumination. Adding a physical effect is redundant. Effects sits this moment out.

Bring a recommendation to intake. Coordinate with Lighting (the flash is already dramatic)
and Sound (the thunder is the audio weight). Effects' addition must add, not compete.

**Duration constraint:** Whatever is chosen — maximum 60 ticks (3 seconds). The strike is
an event, not a sustained state. The player should be recovered before Sprite's line arrives.

### E — The Aftermath
No effects unless a specific decision is made at intake about the aftermath's physical quality.

One optional candidate: `EFFECT slow_falling duration:40 amplifier:0` in the first 2–3
seconds of the aftermath — the body still settling from the strike. This is optional and
mild. If used, it fires within 20 ticks of the strike effect clearing, before the player
has fully re-established physical normalcy. Discuss at intake.

### A' — Dawn
No effects. The dawn is the world's instrument. No physical sensation is appropriate here —
the show ends in the world as it is, not as something Effects has altered.

---

## Show-Level Constraints

- Effects' primary contribution is one moment in Section D. The discipline is everything else.
- "One brief disorientation, 2–3 seconds maximum" — this is the ceiling, not a target.
  Zero disorientation is a valid choice if the other departments' contributions are sufficient.
- No levitation in this show. The player stays on the ground throughout — this is a show
  about being present in a place, not elevated above it.
- No ongoing status effects (night vision, etc.) — the natural darkness of a night sky is
  the show's lighting condition and should not be circumvented.
- Coordinate with Sound on the LIGHTNING tick so the darkness/slow_falling fires simultaneously
  with the flash, not delayed.

---

## Intake Questions for Effects

1. **Section D disorientation:** Option A (darkness), B (slow_falling), C (particle), or D
   (none)? Reasoning required — this is a Director-reviewed decision.
2. **Section E aftermath echo:** Slow_falling for 2–3 seconds after recovery? Yes or no?
3. **Pre-storm particle (Section C):** Any specific particle effect that serves the pre-storm
   building? Or nothing?
4. **Tick coordination:** When the LIGHTNING fires, what tick does Effects' event fire?
   Confirm with Stage Management and Sound.

---

## Decisions
*Effects' Section D choice reviewed by Director before YAML is written.*

## Revision Notes
*Added after each in-game test.*
