---
show_id: showcase.01
document: Scouting Brief
author: Set Director (Zarathale)
updated: 2026-03-30 (v4 — plugin commands noted; consistent with 5-expedition/no-shield structure)
status: Active
---

# Scouting Brief — showcase.01 "Preparing for Battle"

> Zarathale: this is your before-you-go briefing. All five expedition sites are a fresh slate.
> The question is always: *does this feel like where this piece was waiting?*
> For in-game procedure (plugin commands, mark codes, step-by-step): see the **Field Guide**.

---

## Before You Scout

Everything downstream is now confirmed. Read this section before going in-world.

**The show:**
An Armorer Villager equips a Vindicator across five expeditions — one armor slot per trip.
The player travels with the Armorer, watches the armor stand fill, and in the finale the
Vindicator arrives fully equipped.

**The secret (Zarathale knows this; the player does not until the finale):**
The Armorer is preparing the Vindicator to fight the player. The player has been
helping source the kit for their own opponent. Locations should feel like places a
devoted professional would bring someone on a procurement errand — not sinister,
not foreboding. The irony belongs to the finale, not to the locations.

**The kit (Wardrobe confirmed — 5 pieces, no shield):**

| Slot | Item | Character |
|------|------|-----------|
| Helmet | Chain helmet | Mobile, not ceremonial |
| Chestplate | Iron chestplate | Weight and authority |
| Leggings | Iron leggings | Matched to chestplate |
| Boots | Dark leather boots | The Armorer's specific choice |
| Weapon | Iron axe (Sharpness I) | One good edge — the final piece |

Fill order on the armor stand: boots → leggings → chestplate → helmet → axe (weapon last,
right before the Vindicator arrives). The expedition order (B → C → D → E → F) does not
match the fill order — the stand fills in dramatic sequence independent of expedition sequence.

**The voice (script v1 confirmed):**
The Armorer speaks in ambiguous address — "you" throughout could be the Vindicator
or the player. Voice lines are included per site below. These are your primary creative
lens for each location. The location needs to earn the line that will be spoken in it.

---

## In-Game Scouting — Quick Reference

Use the plugin to capture positions. Full workflow in the **Field Guide**.

```
/scaena scout load showcase.01        — start with all marks loaded
/scaena set 1.1                       — capture your position for that mark code
/scaena scout status                  — see what's captured vs. outstanding
/scaena scout save                    — write captures to the server
/scaena scout dismiss                 — end session
```

---

## Scouting Report Format

For every location (home base + 5 expedition sites), file a report entry with:

```
Location: [Site letter and name]
World: [overworld / nether / end]
Coordinates: x=__, y=__, z=__   [from plugin capture or F3]
Arrival facing: yaw=__, pitch=__
Biome: __
Sky clearance: __ blocks (or "obstructed by [material]")
Terrain notes: [floor material, hazards, notable features]
Hazard notes: [hostile mobs in range, fall risk, lava proximity, etc.]
Theatrical read: [1–2 sentences — why this place fits the piece]
Candidate status: [recommended / alternate / rejected with reason]
```

Provide at least one **recommended** candidate per site. Include an **alternate**
if you found more than one strong option.

Every confirmed location enters the permanent stage registry at
`kb/departments/set/set.stage-registry.md` before the show enters tech.

---

## Site A — Home Base: "The Workshop"

**Purpose:** The Armorer's workplace. The armor stand lives here. The player arrives
six times — at show open and after each of the five expeditions.

**The voice lines here:**
> *"Five pieces. Everything I need is out there — I know where each one is."*
> *"You'll want to be properly prepared. I've made sure of that."*
> *"Helmet first. Keep up."*

The Armorer says these lines in this space. The space must make them feel earned —
someone who actually works here saying things they actually mean. Not staged. Not
atmospheric. A place where an Armorer Villager would live and work.

**What to find:** Functional, specific, slightly worn. A structure or enclosed space
that reads as a craftsperson's workshop — stone floor, contained walls, a sense of
purposeful occupation. The armor stand goes center-frame. When it's bare at show open
it reads as absence; when it fills slot by slot on each return, the player needs to
notice each change. Clear sightline to the stand is non-negotiable.

**Requirements:**
- Enclosed enough to feel like a specific place — not an open field
- Flat surface for the armor stand entity, visible immediately on arrival
- No hostile mob spawning in range
- Safe for 6 repeated player TPs with identical arrival facing
- Consider proximity to a village structure (the Armorer lives near one) or a
  standalone smithy/forge building — something that has occupational identity

**What to document:** Coordinates, arrival facing (yaw/pitch centered on armor stand
position), proposed armor stand coordinates, enclosure type, biome/structure, iron door
position, Vindicator holding area notes.

---

## Site B — The Helmet: "High Ground"

**Slot:** Chain helmet — mobile, light, built for a warrior who moves fast.

**The voice lines here:**
> *"I like this spot. High up. Everything below looks manageable from here."*
> *"There it is. Chain. I thought about something heavier, but you move too fast for heavy."*
> *"One. Let's get back — the forge is next."*

"Everything below looks manageable" — that's the quality. The player arrives and
looks out over something. The Armorer is already there, calm, in command of the view.
The chain helmet belongs here because chain is for someone who stays moving — and
this place, you can see everything coming. Find a location where that sentence lands
as true rather than described.

**What to find:** A high point with a view. Not necessarily dramatic architecture —
a natural peak with a commanding sightline is better than a grand tower if the tower
isn't genuinely remarkable. The player should feel above things, not just technically
elevated. The sky close, the ground visible below.

**Requirements:**
- Player TP-in position is safe — no immediate fall risk on arrival
- Arrival facing toward the view or the drop, not a wall
- Biome: mountains, savanna plateau, mesa buttes, tall taiga, windswept hills
- Sky clearance: document blocks above TP position
- Document Y coordinate (altitude relative to sea level)

---

## Site C — The Chestplate: "The Forge"

**Slot:** Iron chestplate — the heaviest piece, widest at the shoulders, the center
of the kit. The Armorer measured for this specifically.

**The voice lines here:**
> *"This place knows what it's for. You can still feel the old work in it."*
> *"Iron. Wide at the shoulders — I measured for this. You know I measured."*
> *"Heavier than it looks. Come on."*

"This place knows what it's for." That's the test. Find a place that has residual
purpose — somewhere that has been used for making, or that carries the quality of
having been. Badlands, lava-adjacent, exposed mineshaft as visual anchor.

**Requirements:**
- Biome: badlands, overworld lava-adjacent (nether excluded per pre-scout lock)
- Lava proximity: document distance to nearest lava source
- Ceiling clearance: document blocks above TP position
- No immediate fire/lava hazard at player TP-in position
- Arrival facing INTO the space — the player enters the forge, not walks past it

---

## Site D — The Leggings: "The Long Road"

**Slot:** Iron leggings — matched to the chestplate, built for movement, made to
cover distance.

**The voice lines here:**
> *"You'd cover a lot of ground in a place like this. I thought about that."*
> *"Iron. These were made to move. I want you moving — right until the end."*
> *"Three. Boots next — I know exactly where."*

"You'd cover a lot of ground in a place like this" — the location should make that
sentence feel obvious. A place where ground-covering is the point.

"Right until the end" is this scene's loaded line. Zarathale should find a place
where the horizon implies continuation — somewhere that asks you to keep going.

**Requirements:**
- Arrival facing DOWN the path or TOWARD the horizon — never across or into a wall
- Biome: plains, forest path, river valley, savanna road, open taiga (savanna/plains village)
- No competing dramatic terrain that fights the "road" quality
- Hostile mob density: note spawners or mob-heavy patches in range
- Sky clearance: document (lower priority, but file it)

---

## Site E — The Boots: "The Swamp Floor"

**Slot:** Dark leather boots, dyed by the Armorer — specific, personal, the piece
that says someone paid attention.

**Terrain locked 2026-03-29: Swamp.** The show stays in the overworld.

**The voice lines here:**
> *"Feel that. This is why boots matter. Most people never think about the ground."*
> *"Leather. Dark — I dyed these myself. I know what you walk toward."*
> *"Four. The hard ones are still ahead."*

"Feel that." — the swamp floor is immediately legible: earthy, wet, specific
underfoot. Clay and mud, shallow water patches.

**Requirements:**
- Confirmed swamp biome (mud/clay floor, water present)
- Terrain legibility immediate on arrival
- Safe player TP altitude: document blocks above terrain floor
- Exact floor material for Effects assessment
- Sky clearance + canopy density: Steve N. uses this to decide campfire type

---

## Site F — The Weapon: "The Choice" *(final expedition)*

**Slot:** Iron axe, Sharpness I — one edge, made deliberate, the most charged piece.
This is the last expedition. The show returns home from here.

**The voice lines here:**
> *"I kept coming back to this place. It answers something."*
> *"Iron axe. I had it sharpened once. That's all you need — one good edge."*
> *"[silence or 'Five. Let's go home.' — Voice decides at intake]"*

"I kept coming back to this place. It answers something." — this is the hardest site
to find. A ruin with a cleared central space. Flat, open ground surrounded by ruin
structure. The cleared space is the scene's visual center.

**What to find:** Stillness with weight. A ruin where some event or force cleared the
center — a courtyard, a collapsed interior, a blast-cleared area within broken walls.
The surrounding structure frames the space; the clearing is what you arrive into.

**Requirements:**
- Cleared central flat space with simple ground material
- **Hostile mobs must be answered.** This is the show's most important silence — active
  spawning in-frame kills it. Note patrol AI range if pillager outpost.
- No ambient spectacle — no lava, no existing particles
- Arrival facing toward the visual anchor
- Document cleared space dimensions and surrounding structure for Effects assessment

**⚠ NO FIREWORK AT THIS SITE.** The find-firework fired at B, C, D, E. The absence
here is the beat. Camera holds; Sound steps back; Effects does nothing.

---

## After Scouting

1. `/scaena scout save` — write captures to server
2. Tell Alan you're done — he pulls coordinates and merges into show-params
3. Fill descriptive fields in `showcase.01.scouting-report.md` and commit
4. Michael C. translates to Environment Notes in `departments/showcase.01.set.md`
5. Stage registry entries go in `kb/departments/set/set.stage-registry.md`
6. Direction closes Gate 3 and schedules intake conversation

**Six locations total:** home base (A) + five expedition sites (B through F).

The TBA flags in script-v1 update to match confirmed site character once scouting
delivers. Voice revises to v2 at that point.
