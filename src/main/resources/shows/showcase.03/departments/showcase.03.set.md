---
show_id: showcase.03
department: Set Director
document: Department Brief
updated: 2026-03-26
---

# Set — showcase.03 "Welcome"

## What This Department Serves

The Welcome Stage is the most important physical deliverable in the Showcase Series. It is a
permanent installation — designed not for this show but for all shows, and for the actual
onboarding of new ScaenaCraft players. Every block placed, every decision made about
approach axis and threshold and sight line, is a decision that will be seen by every new
player who ever comes to this server.

Design for longevity. Document for strangers.

Set is a convergence lead for this show. The threshold definition, the approach axis, and
the facing on arrival are all Arrival Coordination Contract items — they must be agreed with
Camera and Effects before any other department authors the Arrival. Set delivers these first.

---

## The Three Spatial Requirements

### 1. The Approach Corridor
**20–30 blocks of approach space** before the threshold. The player teleports to the start
of the approach, not into the welcome area. They walk toward something. That walking-toward
is the build; what they're approaching is the show's central image.

The approach should feel like arriving, not like appearing. When the player materializes at
the approach start, they should immediately understand: there is somewhere to go, and it is
ahead of them.

What the approach corridor communicates about the player's arrival is determined by its visual
character — an open-air path says something different from a forest clearing approach, which
says something different from a corridor between walls. Set makes this decision as part of
the overall Welcome Stage design. Document the design rationale.

### 2. The Threshold
A legible marker between "approaching" and "arrived." The player crosses it; the show responds.

The threshold must be legible without being explained. Options in the world:
- A gate (wood or iron) — universal doorway signal
- A natural arch or rock formation
- A line of blocks or path material that clearly marks an edge
- A raised step or terrain change
- A water feature the player crosses

Set designs and builds the threshold. It must exist as a physical feature in the world —
not just a coordinate. The Arrival fires at the player's teleport to the welcome area (the
far side of the threshold), but the physical marker tells the player they've crossed into
something.

**Document:** The threshold XYZ (the marker itself, not just the welcome area coordinates).
Camera and Effects need to know where it is — this is the physical anchor of the Arrival.

### 3. The Welcome Area
**20×20 blocks minimum** on the far side of the threshold. This is where:
- The welcoming committee waits (Casting + Choreography stage them here)
- Mira's fireworks fire (sky clearance of 25+ blocks above this area)
- The Celebration unfolds
- The player stands as everything happens

The welcome area center coordinate is the Arrival's spatial anchor — the coordinates where the
player teleports for the Arrival moment. This is what goes in the show's `set:` block for the
welcome area.

**Sky clearance:** 25+ blocks of open sky above the welcome area. Mira needs this. Document
the approximate ceiling height (or "open sky" if unlimited).

---

## Scouting Task (Task 8 in Stage Registry)

The scouting task list at `kb/departments/set/set.stage-registry.md` Task 8 defines the full
scouting and design requirements. Key documentation items:

**Approach corridor:**
- Approach start coordinates (where the player teleports for the Build)
- Approach axis direction (which compass direction is the player walking)
- What the player sees ahead of them as they walk

**Threshold:**
- Threshold coordinates (the marker's position)
- What the threshold marker IS (gate, arch, line, etc.)

**Welcome area:**
- Welcome area center coordinates (Arrival teleport position)
- Facing for the Arrival (what the player is looking at when they teleport in — this is the
  Arrival's opening image and is set together with Camera)
- Welcoming committee staging positions (approximate — where Choreography will place entities)
- Sky clearance in blocks (or "open sky")

---

## The Arrival Coordination Contract — Set's Role

Three items Set must deliver before the Arrival is authored:

**1. The threshold (Set defines it):** What physical marker signals "you've arrived"? Where
is it? Set builds it; Documents it; confirms it.

**2. The facing on arrival (Set + Camera agree):** When the player teleports to the welcome
area, what are they looking at? This is the `yaw:` and `pitch:` in the stage registry. Set
proposes based on the welcome area's visual design; Camera agrees or refines.

**3. Sky clearance for Fireworks (Set → Mira):** The ceiling height above the welcome area
is handed to Mira. She cannot calibrate y_offsets until this number is known.

---

## Design Principles

The tone brief says: "The Welcome stage should feel like a place that was made to receive
people. Not a stage designed to look impressive — a space designed to make someone feel
received."

The approach axis matters: the player should be approaching something. There should be a
*there* there. When Zarathale scouts or builds, the question is: *does walking into this
space feel like arriving somewhere?*

This means the welcome area should have a clear visual focal point from the approach
direction. The player should walk toward something — the threshold, the committee, the
sky beyond — that has gravity. Not a flat clearing in every direction.

Build with longevity in mind. Use blocks that won't look dated. Document every significant
block choice in the registry's scouting notes for future productions.

---

## Intake Questions for Set

1. **Approach start coordinates + facing?**
2. **What does the player see as they walk the approach?**
3. **Threshold marker type and coordinates?**
4. **Welcome area center coordinates?**
5. **Proposed `yaw:` and `pitch:` for Arrival facing?** (Camera reviews and confirms.)
6. **Sky clearance above welcome area?** (Mira's number.)
7. **Welcoming committee staging area:** Where in the welcome area are entities pre-staged?
   (Approximate — Choreography refines.)

---

## Decisions
*Filled at intake — Set delivers first, before Mira and Camera can author Arrival.*

## Revision Notes
*Added after each in-game test.*
