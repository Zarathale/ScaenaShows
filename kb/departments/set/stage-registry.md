---
department: Set Director
document: Stage Registry
kb_version: 1.0
updated: 2026-03-26
owner: Set Director (Zarathale in-game)
---

# Stage Registry — ScaenaShows Permanent Stages

> This is the authoritative record of all established stages across the ScaenaShows
> world. Every stage scouted or built for any show enters this registry. Once a stage
> is registered, it is available to all future productions.
>
> **How stages become available in YAML:** Once coordinates are confirmed and documented
> here, a corresponding entry can be added to any show's `sets:` block. The YAML
> `set:` name should match the registry slug for consistency.
>
> Zarathale executes all scouting and building in-game. Claude documents the registry
> from scouting notes provided by Zarathale.

---

## Registry Format

Each stage entry includes:

```
### [Stage Slug]
- Working name: [human-readable name]
- World: [world name as Minecraft recognizes it]
- Coordinates: x: ___, y: ___, z: ___
- Facing on arrival: yaw: ___, pitch: ___
- return_on_end: true/false
- First used: [show_id]
- Biome/type: [overworld / nether / end / built / natural]
- Ceiling height: [open sky / N blocks / cave]
- Ambient light: [full sun / dusk typical / deep dark / nether glow / end void]
- Sight lines: [brief note — what the player sees when they arrive facing the stated yaw]
- Special notes: [redstone present / water / lava / known quirks]
- Scouting notes: [Zarathale's field notes on why this place was selected]
```

---

## Stage Registry

*No stages registered yet. Scouting for showcase.01 is the first action.*

---

## Scouting Task List — showcase.01 "The Cabinet"

Zarathale's in-game task list. Complete these before showcase.01 YAML is authored.
Document findings in the registry above when each location is confirmed.

---

### Task 1 — Home Base
**What we need:** A grounded overworld location that will serve as the Allay's home and
the show's anchor. The player begins and returns here between every vignette.

**What to look for:**
- Feels like a place where someone lives and works, not a landmark or a viewpoint
- Enough open space for: the Allay at center, 2-3 collected creatures, and the player
  at a comfortable viewing distance (~8-12 blocks from center to player arrival point)
- Sheltered but not enclosed — the sky should be visible, but the space should have
  edges that give it a sense of room
- Interesting ambient light or notable feature at close range (a unique block, a tree
  formation, water nearby) — something the Allay might have noticed and chosen
- Works at multiple times of day (will be visited 5-6 times during the show)

**What to document:** Coordinates for player spawn-in point, facing toward center.
Coordinates for Allay placement. Any notable environmental features.

---

### Task 2 — "The Still Water" (Vignette B)
**What we need:** An overworld natural location — lake edge, waterfall, or forest clearing
with water nearby. Player has free exploration; the exhibit IS the place.

**What to look for:**
- Something genuinely beautiful or strange in the natural terrain — not built
- Water present (lake, river, waterfall — any)
- Open enough for the player to walk around (~15-20 block exploration radius)
- Works at dusk TIME_OF_DAY (the show will set this — confirm the space looks good at dusk)
- Sight line from arrival point into the most interesting part of the space
- Avoid: flooded caves (too enclosed), ocean (featureless), flat plains (nothing to notice)

**What to document:** Arrival coordinates + facing. Brief description of what the player
sees when they arrive. What time-of-day looks best here.

---

### Task 3 — "The Theater" (Vignette C)
**What we need:** A small arena or cleared space with a natural "stage" and a clear player
viewing position. Two entities will be spawned here; the player watches.

**What to look for:**
- A space that reads as a stage — slightly elevated ground, a clearing in trees, a
  natural amphitheater shape, a flat area with something behind it (cliff face, tree line)
- Clear sight line from player arrival point to the "stage" area (~10-15 blocks)
- Enough room for 2 entities to move and interact and for their relationship to read clearly
- Don't filter on natural enclosure — if containment is needed to keep entities in frame,
  Set builds it (fence line, low wall, terrain modification). Scout for theatrical feel.

**What to document:** Arrival coordinates + facing. Stage area coordinates (center).
Approximate wing positions (where entities could be placed off to the sides). Whether the
space is naturally enclosed or open — Set designs containment either way, but knowing
what's there helps Michael C. plan the build.

---

### Task 4 — "The Nether Valley" (Vignette D)
**What we need:** A nether biome location that feels like a Strider's habitat. Player
will be lightly levitated (2-4 blocks) and see a Strider in its natural environment.

**What to look for:**
- Basalt delta, soul sand valley, or nether wastes — something alien and textured
- Lava visible but not directly underfoot (Strider context without player danger)
- Enough open space for the player to be elevated without hitting a ceiling
- Safe floor area for player arrival (not lava)
- The background/environment visible on arrival should feel genuinely different from
  the overworld — the exhibit's point is this biome as a place where different things live

**What to document:** Arrival coordinates + facing. Safe altitude ceiling for levitation.
What world name the nether uses on this server. Environmental description.

---

### Task 5 — "The Suspension" (Vignette E)
**What we need:** A high-altitude location or outer End islands where the player can be
elevated 15-25 blocks with fireworks firing below and around them. Near-dark.

**What to look for:**

*Option A — End islands:*
- Outer End (not the main island) — chorus trees, endstone, the void below
- A flat area or island where the player can be elevated over open void
- Fireworks below player: Mira needs clear downward space of 15-20 blocks

*Option B — High overworld:*
- A mountain peak or platform at y>160 with open sky below on at least one side
- Player elevated another 15-20 blocks from the surface
- Dark enough at the chosen time-of-day that fireworks are visible

**What to document:** World name and coordinates. Altitude floor (where player arrives).
Maximum safe elevation (ceiling or sky). Ambient light level at chosen time of day.
Confirmation that fireworks have clear space in the downward direction.

---

### Task 6 — "The Contraption" (Vignette F)
**What we need:** A space with a working redstone contraption that can be triggered by
a REDSTONE YAML event. The contraption activates something during the show. The reveal
should be small, specific, and slightly absurd — the comedy comes from what it does.

**This task requires building.** Zarathale must construct the contraption.

**Design guidance:**
- The contraption should have a clear "before" and "after" state visible from the player's
  arrival position
- The trigger mechanism must be at a known absolute coordinate (REDSTONE event fires at
  a specific XYZ)
- What the contraption does should be immediately legible — the player should understand
  what happened without narration
- "Small and absurd" examples: a door opens to a single chicken standing there; a piston
  places one flower; a note block plays; a sign is revealed; a single dispenser fires one
  item. Scale DOWN from what feels obvious.
- A creature can be in or behind the contraption as the punchline — coordinate with Casting
  at intake on what creature, what position

**What to document:** Trigger coordinates (XYZ). What the REDSTONE event activates.
Before-state and after-state description. Any cleanup requirements (REDSTONE OFF needed?).

---

## Scouting Task List — showcase.02 "The Long Night"

*One deep location. The show takes place entirely here.*

### Task 7 — "The Night Location"
**What we need:** One overworld location with open sky that will host the entire show
from dusk to dawn. The space must be interesting enough to sustain 10-12 minutes of
attention without teleportation.

**What to look for:**
- Open sky (no cave, no indoor space) — Lighting's full arc requires sky visibility
- The location should have VISUAL LAYERS: foreground (immediate), midground (~20 blocks),
  background (far distance or tree line) — something to look at in multiple directions
- Weather events will be visible here — sky must be unobstructed enough for LIGHTNING
  to register as a real event
- Enough environmental texture that nocturnal creatures make sense here
- The space should feel right at dusk, at midnight, and at dawn — different textures
  of the same place

**What to document:** Coordinates + facing for player arrival. Sky obstruction notes.
Biome. Whether weather events are visible. Environmental layers description.

---

## Scouting Task List — showcase.03 "Welcome"

*One permanent stage. Designed for longevity and repeated use.*

### Task 8 — "The Welcome Stage" ⭐ Priority build
**What we need:** The most important physical deliverable in the Showcase Series. This
stage will be used for actual onboarding of new ScaenaCraft players. Design for longevity.

**What to look for / design:**
- An arrival axis: the player should be approaching something — walking TOWARD the stage,
  not appearing inside it. A clear "before the threshold" and "after the threshold."
- The threshold itself: a gate, a frame, a natural feature, a marked line — something
  that the player crosses when they "arrive." It must be legible without explanation.
- Space for the welcoming committee: 3-5 entities staged on the far side of the threshold,
  facing the approach axis
- Sky visible for Mira's fireworks — know the ceiling height
- Approach feel: the player should feel like they're arriving somewhere, not being
  teleported to a coordinate

**Spatial requirements:**
- Approach corridor: 20-30 blocks of approach space before the threshold
- Welcome area (past threshold): 20x20 blocks minimum for the committee and celebration
- Fireworks clearance: 25+ blocks of open sky above the welcome area
- Return capability: `return_on_end: true` — player returns to their pre-show position

**What to document:** Approach axis coordinates. Threshold coordinates. Welcome area center.
Committee staging positions (approximate). Sky clearance. Architectural notes for future
productions.
