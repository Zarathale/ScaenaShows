---
show_id: showcase.03
department: Effects Director
document: Department Brief
updated: 2026-03-26
---

# Effects — showcase.03 "Welcome"

## What This Department Serves

Effects is a convergence lead for this show. The altitude agreement between Effects and
Fireworks (Mira) is the Arrival Coordination Contract's first item — it must be locked before
either department authors, and before any other department makes decisions that depend on
where the player is during the Arrival.

Beyond the altitude contract, Effects owns the physical experience of crossing the threshold.
The show direction says: "The physical sensation of Arrival should be unmistakable but not
disorienting. The player crosses the threshold and their body knows something has changed."

That change is Effects' instrument.

---

## The Arrival Coordination Contract — Effects' Role

**The altitude agreement with Mira:**
Effects proposes the player's altitude at the Arrival moment. Mira accepts or counters. One
number is locked. Neither department authors Arrival YAML until this is established.

The altitude determines:
- Mira's y_offsets for all firework events at Arrival and Celebration
- The physical sensation of the threshold crossing (a lift, a float, a gentle elevation)
- Camera's framing context for the Arrival (above, below, or level with the fireworks)

**Effects' altitude proposal for showcase.03:**
The tone brief says the sensation should feel like "a door opening" — a change of state,
not a dramatic event. This suggests: moderate elevation, not high altitude. The player
should be lifted enough to feel it, not enough to feel disoriented.

Recommendation to bring to intake: **3–6 blocks of elevation** at the Arrival threshold.
This reads as "lifted slightly — I am now in a different kind of space" without the dramatic
quality of 15–25 block lifts (that's showcase.01 Vignette E's territory). The Celebration
that follows is full and joyful; the player's physical state during it should be buoyant,
not commanding.

This is a proposal. If Mira's Celebration design requires the player to be at a different
altitude for her y_offsets to work as intended, the number is negotiated at intake.

---

## Per-Section Direction

### Build — "The Overture"
The player arrives at the approach start. No effects during the approach walk. The Build is
the player's own movement toward the threshold — they are walking, not being acted upon.

One possible exception: a very subtle `EFFECT slow_falling` (amplitude 0) during the walk
that gives the player a slightly lighter step. Optional and extremely subtle if used — the
approach should feel like walking toward something, not floating toward it.

### Arrival — "The Threshold"
The player teleports to the welcome area. Effects fires on or within 5 ticks of the teleport.

The physical sensation of crossing the threshold:
- **Option A — A lift:** `EFFECT levitation` at low amplitude for 20–40 ticks, then
  `EFFECT slow_falling` to settle. The player rises slightly — the threshold elevated them.
- **Option B — A settling:** `EFFECT slow_falling` only, at amplitude 0. The world becomes
  slightly weightless; the player feels they've entered a different register of gravity.
  More subtle than a lift; still unmistakable.
- **Option C — A clearing:** No altitude shift. A brief `EFFECT nausea` reverse would be
  wrong; a brief visual clarity (no standard effect accomplishes this in YAML). This option
  is essentially "the threshold is physical in Set's design, not in Effects' tools."

Effects brings a recommendation with reasoning to intake. The recommendation must be
coordinated with Mira — the altitude at Arrival is the shared decision.

**Duration:** Whatever is chosen, the sensation should resolve within 60–80 ticks. The
Celebration follows; the player should be settled (or at their maintained Celebration
altitude) by the time the Celebration section begins.

### Celebration — "The Occasion"
The player is at whatever altitude Effects established at Arrival. Effects holds this
state through the Celebration — not changing it, just maintaining it.

If the Arrival established a levitated state, Effects must continue levitation events
through the Celebration or the player will descend mid-celebration. Coordinate with Stage
Management on maintaining the levitation pattern during Celebration.

Or: if Arrival used a brief lift that resolves to ground level, the Celebration is
ground-level. Either is valid.

### Coda — "The After"
No effects. The Coda is the world settling. The player should be grounded, present, still.
If any levitation is active at Celebration's end, resolve it (slow_falling or natural descent)
before the Coda begins.

---

## Show-Level Constraints

- **Altitude agreement with Mira is a hard prerequisite.** Neither authors until this number
  is locked.
- The threshold sensation must be "unmistakable but not disorienting." If a test shows the
  player is confused or destabilized by the effect, reduce it. The feeling is a door opening,
  not a jump scare.
- Effects coordinates with Camera on the Arrival: if the player is lifted, Camera's Arrival
  framing should account for the player's new vertical orientation relative to the committee
  and the sky.
- No effects during the approach walk unless the subtle slow_falling is approved at intake.

---

## Intake Questions for Effects

1. **Altitude at Arrival:** Effects' proposed number (recommendation: 3–6 blocks). Mira
   must agree before this is locked.
2. **Threshold sensation:** Option A (lift), Option B (settling/slow_falling), or Option C
   (no altitude sensation)? Reasoning required — Director review.
3. **Celebration maintenance:** If levitated at Arrival, how is levitation maintained through
   the Celebration section?
4. **Coda resolution:** When does levitation resolve? What is the mechanism?
5. **Approach walk effect:** Subtle slow_falling during the walk? Yes or no?

---

## Decisions
*Altitude agreement with Fireworks (Mira) comes first. Both departments hold until the number is locked.*

## Revision Notes
*Added after each in-game test.*
