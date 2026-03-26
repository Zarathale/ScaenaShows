---
show_id: showcase.02
department: Choreography Director
document: Department Brief
updated: 2026-03-26
---

# Choreography — showcase.02 "The Long Night"

## What This Department Serves

Choreography's contribution to this show is minimal and deliberate. Atmosphere, not character.
There is one location; there are no dramatic scenes; the creatures move because that's what
they do in the world at night.

The work: spawn the nocturnal creatures in the right positions, release them to performer state,
and let AI determine everything else. Do not over-direct. Choreography's discipline in this
show is restraint — knowing when to set a creature down and walk away.

---

## Per-Section Direction

### Section B — Bats (aerial, above the player)
Spawn location: 15–25 blocks above the player's arrival position, in open sky.
Initial spawn cluster: within a 5-block radius of a single point above the player.
State: performer state (AI on). Bats will fly naturally from this initial cluster.
Number: 3–5, per Casting's recommendation.
No additional choreography. They fly. That's the scene.

### Section B — Wolf patrol (distance)
Spawn location: 30–50 blocks from the player arrival point, in the midground of the
visual space (toward the tree line, across the field, at the edge of visibility).
Initial facing: not toward the player — parallel to the horizon or angled away.
State: performer state (AI on). Wolves will wander.
The player should hear the Wolf before (or instead of) seeing it clearly. Spawn it in
a location where its AI wandering keeps it mostly at distance — near cover, not in open field.

### Section C — Creature thinning (if Casting recommends)
If Casting decides creatures thin or disappear before the storm:
- Bats: DESPAWN_ENTITY at Casting's designated thinning tick
- Wolf: DESPAWN_ENTITY at Casting's designated thinning tick (or leave)
Choreography executes; Casting makes the call on timing.

### Section D–E — No creature staging
No creatures are spawned or moved during the storm or aftermath. The LIGHTNING strike owns
this moment. Choreography stays out.

### Section A' — Dawn (optional)
If Casting decides a creature returns in the aftermath for dawn: Choreography places it.
Position: consistent with where it was in Section B — the same creature returning to the
same area of the space. Spawn it at dawn's beginning (Lighting signals this tick) and
release to performer state. One moment of life returning.

---

## Show-Level Constraints

- No creature should be spawned closer than 20 blocks to the player. These are environmental
  presences, not companions. Distance is part of their contribution.
- Phantom (if Casting chooses): puppet state, placed at altitude. No behavioral design —
  it is a shape in the sky.
- Creature count is low (2–6 total across the show). No performance concerns.
- All creatures despawned cleanly before show end.

---

## Intake Questions for Choreography

1. **Bat spawn altitude:** Exact Y value above player arrival, based on Set's confirmed
   location coordinates.
2. **Wolf spawn position:** Approximate XYZ in the midground after Set confirms the location.
   What's at 30–50 blocks from arrival in the direction of the most interesting visual layer?
3. **Creature thinning tick:** If Casting recommends thinning, at what tick in Section C?
4. **Dawn creature (if applicable):** Which creature? Spawn position? Tick?

---

## Decisions
*Filled at intake — after Set confirms location and Casting confirms creature selection.*

## Revision Notes
*Added after each in-game test.*
