---
show_id: showcase.03
department: Camera Director
document: Department Brief
updated: 2026-03-26
---

# Camera — showcase.03 "Welcome"

## What This Department Serves

Camera is a convergence lead for this show. Mark must answer one question before any other
department authors the Arrival: **what is the player looking at when the Arrival fires?**

This is the Arrival Coordination Contract's second item (alongside altitude and the threshold).
The answer determines the first image the player sees at the moment everything happens at once.
It might be the welcoming committee. It might be the sky above the Welcome Stage where Mira
fires. It might be the threshold itself, seen from just past it. Whatever it is, the player
should feel they're seeing it at exactly the right moment.

The show direction says: "not oriented by force, but by the feeling that this is where to look."

---

## The Arrival: Mark's Primary Decision

The Arrival fires everything simultaneously. Camera's contribution is the frame that makes
the Arrival visible — the one orientation the player needs to have.

**Three candidates:**

**Option A — The welcoming committee:** The player arrives facing the committee head-on. They
see the entities waiting for them. The fireworks fire above and around the committee. Sprite's
word arrives with the committee in frame. This is the most direct "welcome" image — faces
(or whatever faces look like on these creatures) turned toward you.

**Option B — The sky above the Welcome Stage:** The player arrives facing upward, or with a
facing that centers the sky where Mira fires. The committee is in peripheral vision; the sky
is the feature. The fireworks are the primary sensory event, and the player is oriented to
see them at their best. This requires coordination with Mira on where exactly in the sky her
first burst fires.

**Option C — The threshold itself:** The player is oriented toward where they just crossed —
looking back at the threshold from inside the welcome area. What they're looking at is the
line they crossed. This is the most conceptual choice: the image of the threshold as something
you've passed through. The committee is behind them (they'll turn); the sky is above them.

Mark brings one recommendation and reasoning to intake. Effects and Fireworks review the
recommendation before the contract is locked — altitude, facing, and threshold are coupled.

---

## Per-Section Direction

### Build — "The Overture"
The player walks the approach. Camera orients the player toward the threshold on arrival at
the approach start — a `FACE` within 60 ticks of the initial teleport, aimed at the threshold
or the welcome area beyond it.

During the walk: no camera events. The player is moving; let them move. The act of walking
toward the threshold is already the right experience. Camera intervention during the approach
risks interrupting the player's own momentum.

One exception: if the welcoming committee is visible from the approach (they should be —
they're staged before the player arrives), a single `FACE` midway through the Build to confirm
the committee is in frame. Optional; only if the approach geometry doesn't naturally present
them.

### Arrival — "The Threshold"
The `FACE` event (if needed) fires at the Arrival. If the approach geometry already has the
player facing the right direction, no FACE is needed — the teleport places them with Set's
`yaw:` and `pitch:` values. Mark reviews those values and signs off.

If a FACE is needed: it fires simultaneously with or within 5 ticks of the Arrival teleport.
The Arrival is one moment — Camera does not lag behind.

### Celebration — "The Occasion"
No sustained camera intervention during the Celebration. The joy should feel free, not directed.
The player looks wherever they look; the Celebration is big enough to fill their vision from
any direction.

One optional `FACE` late in the Celebration — as Mira's arc begins to peak — could direct
the player toward the sky if the fireworks are above them and they've been looking at the
committee. Optional; assess in first test.

### Coda — "The After"
The Coda is the player standing in the world. No camera intervention. If Mark issued a
Celebration `FACE` toward the sky, let the player hold that view into the Coda. The world
is quiet now. Wherever they're looking is the right place.

---

## Coordination Notes

**With Set:** Mark reviews the welcome area's `yaw:` and `pitch:` in the stage registry
entry. If these values produce the wrong Arrival image (not the committee, not the sky, or
into a wall), Mark proposes adjustments. Set builds with the agreed facing in mind.

**With Effects:** If the player is lifted at Arrival, their vertical orientation changes.
A player lifted 5 blocks will have a different sight line to the committee than one at
ground level. Camera's recommended facing (yaw, pitch) must account for the player's altitude.
This is why altitude and facing are agreed together.

**With Fireworks (Mira):** If Option B (sky above) is Mark's recommendation, Mira needs
to know which part of the sky — approximately what azimuth and elevation angle — so her
first burst fires into the player's frame. Mark and Mira coordinate on this at intake.

---

## Show-Level Constraints

- **Mark answers the Arrival facing question before any other department authors Arrival.**
  This is the convergence lead responsibility.
- The approach walk should not have more than one camera event. Restraint here amplifies
  the Arrival's impact.
- The Arrival facing (`FACE` or Set's `yaw:`/`pitch:`) is locked at intake. It does not
  change without Director sign-off — other departments have authored around it.

---

## Intake Questions for Camera

1. **Arrival facing:** Option A (committee), Option B (sky), or Option C (threshold)?
   Reasoning required. Effects and Mira must review before lock.
2. **Approach walk `FACE`:** One initial orientation on arrival at approach start. What
   is the facing — toward the threshold? Toward the committee visible behind it?
3. **`yaw:` and `pitch:` sign-off:** After Set provides the welcome area's registry
   coordinates, Mark reviews and confirms (or proposes changes to) the facing values.
4. **Celebration `FACE` toward sky:** Yes (late in Celebration, toward fireworks peak)
   or no?

---

## Decisions
*Arrival facing decided at intake — convergence leads (Effects, Fireworks, Camera) agree first.*

## Revision Notes
*Added after each in-game test.*
