---
show_id: showcase.01
department: Set Director
document: Department Brief
updated: 2026-03-26
---

# Set — showcase.01 "The Cabinet"

## What This Department Serves

Set is the first mover for this show. No other department can author YAML until Set has confirmed
real-world coordinates for each location. The show has six distinct spaces: the Home Base (A),
and five vignette destinations (B–F). All six must be scouted or built and entered into the
stage registry before intake closes.

The show's premise is that the Allay has been collecting things — places as much as items. Every
space the player visits is something the Allay found and thought worth keeping. Set's job is to
find (or build) spaces that actually earn that premise. The player should feel, at each stop,
that a small eccentric creature chose this place for a reason.

---

## Key Decisions

**1. All six locations must be confirmed before anything else moves.**
The scouting task list in `kb/departments/set/stage-registry.md` defines what each space needs.
That document is the authoritative brief for Zarathale's in-game work.

**2. Home Base personality shapes the show's entire register.**
Home Base is visited 5–6 times. It must have a distinctive quality — a feature, a formation,
a light condition — that makes it feel like a *chosen* place, not a random clearing. Identify
that quality during scouting and document it. Casting will build around it. Voice will refer to it.

**3. Each vignette space must have a clear player arrival orientation.**
Document `yaw` and `pitch` for each arrival point. The player spawns-in facing something specific.
That first view IS the vignette's opening image — it must be intentional.

**4. "The Contraption" (Vignette F) requires building.**
This is the only task requiring active construction in-game. Design guidance is in the stage
registry Task 6. The trigger XYZ must be known so the REDSTONE YAML event can be authored.
Design the contraption with the show's comedy register in mind: small, specific, slightly absurd.
A door opens; a piston places one flower; a chicken was waiting there the whole time.

---

## Show-Level Constraints

- All six stages must be registered in `kb/departments/set/stage-registry.md` using the standard
  registry format (slug, coordinates, facing, biome/type, ceiling height, ambient light,
  sight lines, scouting notes).
- YAML `set:` block entries use the registry slug as the identifier. Consistency here prevents
  errors downstream.
- The Nether Valley (Vignette D) requires documenting the world name as the plugin sees it —
  not just "nether." Confirm the actual world name on this server.
- "The Suspension" (Vignette E) requires sky clearance data: what is the maximum safe altitude
  for player levitation at this location? That number goes to Effects and Fireworks at intake.
- `return_on_end: true` is required for all six stages (the show always returns the player home).
- The Welcome Stage (Task 8, showcase.03) is also on Zarathale's task list. That stage is a
  separate show's deliverable but shares the same scouting window — if opportunistic, scout both.

---

## Intake Questions for Set

1. **Home Base:** What is the distinctive feature that makes this space feel chosen? Describe
   it in one sentence as Zarathale experienced it.
2. **Vignette B ("The Still Water"):** What time of day looks best here? Is the water a lake,
   river, or waterfall? What does the player see on arrival?
3. **Vignette C ("The Theater"):** What is the natural "stage" element — a clearing in trees,
   a cliff face behind, a slight elevation? Where do the wing entry points sit?
4. **Vignette D ("The Nether Valley"):** What is the exact world name? What is the safe floor
   elevation on arrival? What biome variant (basalt delta, soul sand valley, nether wastes)?
5. **Vignette E ("The Suspension"):** End islands or high overworld? What is the floor altitude
   (player arrival) and the maximum safe ceiling? Is there clear downward space for fireworks?
6. **Vignette F ("The Contraption"):** What does the contraption do? What are the trigger
   coordinates? What is the before-state and after-state visible from the player arrival point?
   Is a REDSTONE OFF event needed for cleanup?
7. **Registry slugs:** Propose a slug for each location at confirmation. (Example:
   `home_base_meadow`, `still_water_falls`, `theater_grove`, etc.)

---

## Decisions
*Filled at intake — after Zarathale confirms all six locations in-game.*

## Revision Notes
*Added after each in-game test.*
