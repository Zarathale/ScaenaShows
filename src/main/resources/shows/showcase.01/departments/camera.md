---
show_id: showcase.01
department: Camera Director
document: Department Brief
updated: 2026-03-26
---

# Camera — showcase.01 "The Cabinet"

## What This Department Serves

Camera (Mark) is an orientation instrument in this show — not a dramatic one. The player
teleports into six distinct spaces across the course of the show. At each teleport, they arrive
facing whatever the `set:` coordinates specify. Camera's job is to confirm that arrival facing
serves the opening image of each vignette, and then to step back.

This is not a show that calls for dramatic camera sweeps or sustained reorientation. The player
is an explorer. Let them explore. Camera gives them the right first view, then releases them.

**This is the baseline — not the ceiling.** The restrained footprint in the brief is Camera's
conservative starting position. As the show develops through Watch Rounds, Mark is standing by
to take more cues and suggest moments where a camera intervention would serve the show: a
creature entrance that lands harder if the player is already facing left, a sound hit that pulls
focus somewhere worth orienting toward, a moment in Vignette E: The Suspension that the
orientation philosophy doesn't fully account for. The brief says "do less"; Watch Rounds are
where "do more here" gets discovered.

The one planned exception at brief stage: Vignette E: The Suspension. The player is elevated
into Mira's fireworks sequence. Mark has a specific question to answer for that moment: does
the player face into the sky, or downward into the burst below them? This is a philosophy
question, not a technical one, and Mark brings the answer to intake.

---

## Per-Section Direction

### A-sections — Home Base
Arrival facing is defined by Set's coordinates. Mark confirms (with Set) that the arrival
facing puts the Allay in the center of the frame.

Once the player is at Home Base, no camera adjustment is required. The Home Base is familiar
by the second return — the player knows where they are. Trust that.

The exception: the very first A-section arrival (show opening). This is the player's introduction
to the world. If the arrival facing doesn't immediately present the Allay and the space, Mark
may add a single `FACE` adjustment after a short pause (60 ticks) to let the player settle,
then orient. This is optional — if Set's coordinates land the Allay in frame naturally, skip it.

### Vignette B — "The Still Water"
Arrival facing is toward the most interesting part of the water space (Set documents this).
After the player arrives, no camera intervention. This vignette is about free exploration — any
reorientation after arrival interrupts the player's agency in a space designed for it.

### Vignette C — "The Theater"
Arrival facing is toward the natural stage area where the two creatures are staged.
Mark should confirm with Set that the arrival coordinates produce a sight line to the "stage"
from the player's entry position.

After the initial orientation: no adjustment. The player watches what the creatures do. If the
creatures wander out of frame, that is the creature theater's authentic nature — do not try to
compensate by reorienting the player.

### Vignette D — "The Nether Valley"
The player arrives slightly elevated (Effects' light levitation). The arrival facing should
present the nether landscape and the Strider in the same frame.

If the Strider is on lava and the player is 3–4 blocks up, a slight downward pitch on arrival
(negative pitch value in coordinates) helps the Strider and the lava read in the same view.
Mark coordinates with Set on whether to adjust the `pitch:` field in the stage registry entry
for this.

No reorientation after arrival — the player floats and observes.

### Vignette E — "The Suspension"
Mark's primary question for this show: **what does the player look at during Mira's fireworks?**

Three options:
- **Option A — Face the sky above:** Player looks up, slightly downward pitch so the burst
  radius is in view. The experience is "I am floating and the sky is doing something."
- **Option B — Face downward:** Player looks at the world below, fireworks firing between
  them and the ground. The experience is "I am above everything."
- **Option C — Hold the arrival facing:** Player stays oriented as they arrived (horizontal,
  looking at the landscape). Fireworks occur at peripheral altitude. Less directed, more ambient.

Mark brings a recommendation to intake. Effects and Mira should hear the recommendation before
intake closes — the altitude agreement and the facing decision are coupled. A player facing down
at altitude 20 has a different experience than a player facing up at altitude 20.

If a `FACE` event is used: fire it at the start of the levitation climb (before reaching full
altitude), not after. The reorientation and the lift should work together.

### Vignette F — "The Contraption"
Arrival facing is toward the contraption's "before" state. The player sees the setup.
After the REDSTONE trigger fires the reveal, no camera adjustment is needed — the reveal happens
in front of them if Set's approach is designed correctly.

If the punchline creature appears somewhere that isn't in the arrival frame, Mark may add a
single `FACE` to redirect toward it immediately after the REDSTONE event. Coordinate with Set
and Casting at intake on whether this is needed.

---

## Show-Level Constraints

- The `set:` registry coordinates include arrival `yaw:` and `pitch:` — Camera's primary tool
  is coordinating with Set on what those values should be, not necessarily issuing separate
  `FACE` events.
- `FACE` events (velocity-based smooth turns) take time. A `FACE` issued immediately on arrival
  is jarring. If reorientation is needed after teleport, wait 40–60 ticks before issuing it.
- This show has six distinct arrival moments. Mark reviews each stage registry entry and signs
  off (or proposes adjustments) on each `yaw:` and `pitch:` value before YAML is authored.

---

## Intake Questions for Camera

1. **Vignette E philosophy:** Option A (look up), Option B (look down), or Option C (hold
   arrival facing)? Mark brings a recommendation.
2. **Opening A-section:** Does Set's arrival facing naturally frame the Allay? Or does Mark
   need a single `FACE` event 60 ticks in?
3. **Vignette C sight line:** Confirm with Set that the arrival coordinates produce a clear
   sight line to the "stage" area where the creature pair is staged.
4. **Vignette F reveal:** Is the punchline creature visible from the arrival facing after the
   REDSTONE trigger? Or does Mark need to issue a `FACE`?

---

## Decisions
*Filled at intake — Vignette E: The Suspension orientation philosophy answered first.*

## Revision Notes
*Added after each in-game test.*
