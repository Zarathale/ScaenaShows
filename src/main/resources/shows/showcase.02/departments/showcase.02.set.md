---
show_id: showcase.02
department: Set Director
document: Department Brief
updated: 2026-03-26
---

# Set — showcase.02 "The Long Night"

## What This Department Serves

Set provides one location. The entire 10–12 minute show takes place here. The player never
leaves. The location must sustain that duration through its own inherent interest.

The scouting task (Task 7 in `kb/departments/set/set.stage-registry.md`) defines what the Night
Location must have: open sky, visual layers across foreground/midground/background, weather
event visibility, nocturnal creature plausibility, and the quality of feeling right at dusk,
midnight, and dawn.

One location. Get it right.

---

## Key Decisions

**The location must have visual layers.** The player will spend 10–12 minutes here. A flat
plain with no visual variation will feel like a void by minute 6. The space needs:
- Foreground: something within 5 blocks of the player arrival point
- Midground: something at 15–25 block viewing distance
- Background: a horizon, a tree line, a cliff face, a distant silhouette

These layers don't need to be dramatic. A field with a nearby pond (foreground water), a
stand of trees at mid-distance, and a mountain on the horizon is enough. The layers give
the player's eye somewhere to travel through the show's duration.

**The location must have clear sky for LIGHTNING.** A LIGHTNING event fires in the world at
a specific XYZ — it must be visible from the player's arrival point. If the sky is obstructed
(cave, heavy overhang, dense tree canopy above), the LIGHTNING strike will not be seen.
Lighting will choose the strike XYZ; Set must confirm the sky is clear enough that any
reasonable nearby point is visible from the player.

**Weather events must register.** WEATHER rain must be visible and audible at this location.
An enclosed or heavily sheltered space may not convey rain convincingly. If Lighting chooses
rain for Section C, the location must let the player see and hear it.

**The arrival facing matters more here than anywhere else.** The player will spend the entire
show facing (roughly) the direction they arrive facing. If the arrival facing is toward a
featureless wall, or directly into dense foliage with no depth, or at an angle that misses
all three visual layers — the show suffers for its entire duration. Camera will refine, but
Set's initial facing decision carries most of the weight.

---

## Documentation Requirements

For the stage registry entry (Task 7):
- Coordinates + facing for player arrival point
- Sky obstruction notes (clear sky? partial tree cover? what angle is fully open?)
- Biome name
- Weather event visibility (explicitly confirmed)
- Environmental layers description — foreground, midground, background
- Best time of day for visual character (Lighting will need this)
- Any quirks or concerns (mob spawns, nearby structures that would break atmosphere, etc.)

Additionally: identify the approximate XYZ of one or two locations within 20–30 blocks of
the player arrival that would make a visually compelling LIGHTNING strike point. Lighting
will choose; Set suggests candidates based on what looks good from the arrival facing.

---

## Show-Level Constraints

- One location. No teleports. No construction required (this is a scouting task, not a
  building task).
- The location's visual layers must work at dusk, at midnight, and at dawn — three
  fundamentally different light states. Some locations read beautifully at dusk but feel
  featureless at night. Test the space at night before confirming.
- The location must be far enough from any settlements or structures that the show's atmospheric
  premise isn't broken by an unexpected building in the midground.
- `return_on_end: true` — player returns to their pre-show position when the show ends.

---

## Intake Questions for Set

1. **Location confirmed:** What is the registry slug? Coordinates and facing?
2. **Sky clearance:** Is the entire sky above the player's arrival point open? Or partial
   coverage? What angle is fully clear?
3. **Visual layers:** Describe foreground, midground, background in one sentence each.
4. **LIGHTNING candidates:** What 1–2 XYZ locations near the player would make a compelling
   strike point visible from arrival facing?
5. **Night behavior:** Does the location feel right at midnight, or does it become a
   featureless dark? Any specific qualities that emerge at night?

---

## Decisions
*Filled at intake — Stage 7 scouting confirmation.*

## Revision Notes
*Added after each in-game test.*
