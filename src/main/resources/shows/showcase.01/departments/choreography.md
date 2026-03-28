---
show_id: showcase.01
department: Choreography Director
document: Department Brief
updated: 2026-03-26
---

# Choreography — showcase.01 "The Cabinet"

## What This Department Serves

Choreography has a narrow brief in this show — and a clear principle behind it: **get the
entities staged, then let them be.**

The Allay at Home Base is not a puppet in this show. It is present, genuinely, as a creature
that wanders and hovers and does Allay things. Choreography's job for the Allay is to place it
correctly on arrival and then step back. AI does the rest.

The creature theater in Vignette C is specifically designed to let entity AI create the
scene. Choreography stages the two creatures in the right positions (facing the right direction,
near the right spatial relationship) and then releases them. What happens next is the show.

The punchline creature in Vignette F is placed by the contraption — its position is determined
by the contraption design, not by Choreography's blocking.

Choreography's actual work in this show is small and specific: initial placement precision.
Where does the Allay start? Where do the Vignette C creatures start? Do those positions produce
the right first image? That's the question.

---

## Per-Section Direction

### Home Base — Allay placement
The Allay is spawned (or persists) at Home Base for each A-section. Its initial placement
should put it:
- Near center of the usable space (not at the edge of the frame)
- Facing loosely toward the player's arrival direction — not specifically, just generally
  in the same half of the space
- At hover height (the Allay naturally hovers — no levitation events needed; this is AI behavior)

After placement, AI is on. The Allay wanders. Choreography has no further responsibility here.

If the Allay has companions at Home Base (Casting's decision): position them at 3–5 blocks
from the Allay, spread loosely. Not a lineup. A loose gathering of creatures that are near
each other without being arranged.

### Vignette C — "The Theater"
Two creatures. Placed at the "stage" area (Set's designation). Initial positions:

- Creature 1: at the stage center, facing toward the player arrival point.
- Creature 2: 3–4 blocks from Creature 1, also roughly facing the player arrival direction
  or facing Creature 1 — whichever Casting's pairing suggests creates a more dynamic start.

After placement: AI on (if safe per Casting's recommendation). Let them interact.

The constraint of mob movement is a design question, not a scouting filter. If AI-on
creatures wander out of the stage area, Set designs containment — a fence line, a low wall,
terrain modification, whatever fits the space with least intrusion. Choreography raises the
question at intake (does this pairing need containment?); Set answers it with construction.
If early tests show wander-out-of-frame and containment isn't solving it, Choreography can
discuss puppet state with the Director as a fallback.

### Vignette E — Aerial presence (if Casting decides yes)
If Casting includes Phantoms or Bats in Vignette E:

- **Phantoms (puppet state required):** Placed at specific XYZ coordinates below and around
  the player's altitude. Their `ENTITY_AI: false` must be set before placement — AI-on Phantoms
  attack. Position them for visual composition (swooping shapes in void or air below the player)
  rather than any behavioral logic.
- **Bats (performer state):** Spawn them in the void/open-air space below the player's altitude.
  They will fly naturally. Position them at a cluster point and release.

### Vignette F — Punchline creature
Choreography does not block the punchline creature. Its position is determined by the
contraption design (Set's domain) — it is placed inside the contraption's "before" state,
revealed by the REDSTONE event. After the reveal: AI on, and whatever the creature does
naturally is the coda of the joke.

---

## Show-Level Constraints

- **Initial placement is Choreography's instrument; sustained behavior is AI's.** Don't
  over-script. Trust the creatures.
- All SPAWN_ENTITY events for performers are issued close to (or exactly at) the start of
  their vignette, not pre-loaded at show start. Creatures spawned before they're visible
  risk wandering, despawning, or creating world pollution.
- Vignette D (Strider): the Strider spawns on or near lava in the nether. Choreography
  notes the spawn position only — performer state, AI on. No behavioral scripting needed.
  The Strider walking on lava is its authentic behavior.
- Entity counts: this show has low total entity count per section — one Allay + 2–3
  companions at Home Base; two creatures at Vignette C; one Strider at Vignette D; optional
  Bats/Phantoms at Vignette E; one punchline at Vignette F. This is well within any
  performance concern.

---

## Intake Questions for Choreography

1. **Home Base Allay position:** Where exactly within the scouted Home Base space does
   the Allay spawn? (This requires Set's coordinates first — answered at intake after
   Set confirms.)
2. **Home Base companions:** How many companions? Where positioned relative to the Allay?
   (Casting answers the who; Choreography answers the where.)
3. **Vignette C initial formation:** Creature 1 and Creature 2 — face toward player or
   face each other? Starting distance? (Informed by Casting's species recommendation.)
4. **Vignette E aerial presence:** If Casting says yes — Phantoms (puppet) or Bats
   (performer)? Where positioned relative to the player's altitude?

---

## Decisions
*Filled at intake — after Set confirms coordinates and Casting confirms performers.*

## Revision Notes
*Added after each in-game test.*
