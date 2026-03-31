---
show_id: showcase.01
document: Scouting Field Guide
version: v6
updated: 2026-03-30 (site-based loading as primary workflow; 24 marks; env auto-capture)
use: In-game reference — keep open on second screen while scouting
---

# Scouting Field Guide — showcase.01 "Preparing for Battle"

> Six locations. One question per site: *does this feel like where this piece was waiting?*
> Work one site at a time. Load all marks for the site you're at, capture them, move on.

---

## Plugin Workflow — How to Scout

Work **one site at a time**. Load all marks for wherever you're standing, capture them all, then move to the next site.

**Load all marks for a site:**
```
/scaena scout load showcase.01 site_a   — home base (6 marks: arrival, Armorer, Vindicator, armor stand, door, furnace)
/scaena scout load showcase.01 site_b   — High Ground (3 marks: arrival, Armorer, drone)
/scaena scout load showcase.01 site_c   — The Forge (3 marks: arrival, Armorer, drone)
/scaena scout load showcase.01 site_d   — The Long Road (4 marks: arrival, Armorer, drone, campfire)
/scaena scout load showcase.01 site_e   — The Swamp Floor (4 marks: arrival, Armorer, drone, campfire)
/scaena scout load showcase.01 site_f   — The Choice (4 marks: arrival, Armorer, drone, campfire)
```
The sidebar shows the site name ("Site A — scout"). Captures the full set for that location.

**Type-based loading (for revisiting a specific category):**
```
/scaena scout load showcase.01 spawns   — all cast spawn positions
/scaena scout load showcase.01 blocks   — all BLOCK_STATE targets (furnace + campfires)
/scaena scout load showcase.01 armorer  — all Armorer positions
/scaena scout load showcase.01 drone    — all drone start positions
```

**Check what's loaded:**
```
/scaena scout status   — shows all marks with codes; captured ones show coordinates
```

**Capture a mark:**
Stand at the position. Type:
```
/scaena set 1.1       — captures your exact position + environmental data for that code
```
Chat confirms position and environmental snapshot:
```
✓ 1.1 home_base — (x.x, y.y, z.z)
   biome:PLAINS  light:8 (sky:8 blk:0)  ceiling:open [open_sky]
```
Mark disappears from sidebar. You can re-capture by running `/scaena set <code>` again — it overwrites.

**Save when done:**
```
/scaena scout save    — writes all captured marks to the server
                        File: plugins/ScaenaShows/scout_captures/showcase.01/[date].yml
                        Session stays active — keep going if you want to add more.
```

**End session:**
```
/scaena scout dismiss — hides sidebar, ends session
```

After saving: tell Alan you're done. He pulls the capture file from the server and merges coordinates into show-params. You do not need to copy any coordinates manually.

**In-game hints:** `/scaena scout status` shows a hint line (↳) under each mark — positioning guidance, what to face, key constraints.

**Environmental data is now auto-captured** at every mark. When you run `/scaena set <code>`, the plugin captures biome, light level (combined, sky, and block), ceiling height, and sky type automatically — alongside your coordinates. The capture file includes all of it. You no longer need F3 notes for those fields.

**Still use F3 / manual notes for:** canopy density description (Site E — Steve's qualitative read for campfire decision), lava proximity in blocks (Site C — Effects assessment), hostile mob hazard notes, theatrical read.

---

## Mark Reference — showcase.01 (24 marks total)

**Sites — player arrival positions (tag: `sites`)**

| Code | Name | What it is |
|------|------|-----------|
| 1.1 | home_base | Site A — The Workshop |
| 1.2 | high_ground | Site B — The Helmet |
| 1.3 | the_forge | Site C — The Chestplate |
| 1.4 | the_long_road | Site D — The Leggings |
| 1.5 | the_swamp_floor | Site E — The Boots |
| 1.6 | the_choice | Site F — The Weapon |

**Cast positions at home base (tag: `spawns`)**

| Code | Name | What it is |
|------|------|-----------|
| 2.1 | companion_spawn | Armorer Villager — opening position |
| 2.2 | vindicator_spawn | Vindicator — behind-wall holding position |

**Entity props at home base (tag: `props`)**

| Code | Name | What it is |
|------|------|-----------|
| 3.1 | armor_stand | Armor stand — on 1-block pedestal, 6–8 blocks from arrival |
| 3.2 | iron_door | Iron door — stand in the doorway |

**World block targets — BLOCK_STATE events (tag: `blocks`)**
*Stand ON TOP of the block when capturing. Plugin derives block coordinates.*

| Code | Name | What it is |
|------|------|-----------|
| 3.3 | blast_furnace | Blast furnace at Site A — BLOCK_STATE lit=true at show open |
| 6.1 | campfire_long_road | Campfire at Site D — BLOCK_STATE + mob containment anchor |
| 6.2 | campfire_swamp_floor | Campfire at Site E — BLOCK_STATE (soul vs. regular TBD) |
| 6.3 | campfire_the_choice | Campfire at Site F — BLOCK_STATE + particle beat anchor |

**Armorer standing positions at expedition sites (tag: `armorer`)**

| Code | Name | What it is |
|------|------|-----------|
| 4.1 | armorer_high_ground | Armorer at Site B — 2–4 blocks from 1.2 |
| 4.2 | armorer_the_forge | Armorer at Site C — 2–4 blocks from 1.3 |
| 4.3 | armorer_long_road | Armorer at Site D — 2–4 blocks from 1.4 |
| 4.4 | armorer_swamp_floor | Armorer at Site E — 2–4 blocks from 1.5 |
| 4.5 | armorer_the_choice | Armorer at Site F — 2–4 blocks from 1.6 |

**Drone start positions (tag: `drone`)**

| Code | Name | What it is |
|------|------|-----------|
| 5.1 | drone_high_ground | Drone spawn at Site B — 15–25 blocks back from 4.1, facing Armorer |
| 5.2 | drone_the_forge | Drone spawn at Site C — 15–25 blocks back from 4.2, facing Armorer |
| 5.3 | drone_long_road | Drone spawn at Site D — 15–25 blocks back from 4.3, facing Armorer |
| 5.4 | drone_swamp_floor | Drone spawn at Site E — 15–25 blocks back from 4.4, facing Armorer |
| 5.5 | drone_the_choice | Drone spawn at Site F — 15–25 blocks back from 4.5, facing Armorer |

---

## What to capture at every site

```
Plugin captures (auto — no F3 needed for these):
  Arrival + env:        /scaena set 1.x   → captures xyz + biome, light, ceiling, sky type
  Armorer position:     /scaena set 4.x   (2–4 blocks from arrival, facing arrival)
  Drone start:          /scaena set 5.x   (15–25 blocks back from Armorer, facing Armorer)
  Block marks (D/E/F):  /scaena set 6.x   (stand ON TOP of campfire block first)
                        /scaena set 3.3   (at Site A — stand ON TOP of blast furnace)

Manual notes (still go in the scouting report):
  Canopy density:  open / sparse / moderate / dense  ← Site E especially — Steve's campfire decision
  Lava proximity:  __ blocks                          ← Site C — Effects fire resistance assessment
  Hazards / mob notes:
  Theatrical read (1–2 sentences — why this place fits the piece):
  Status:        recommended / alternate / rejected: [reason]
```

**Environmental data captured automatically at every mark:** biome, combined light level, sky light, block light, ceiling height (blocks), sky type (open_sky / partial / enclosed / underground). All written to the capture YAML — no need to read these from F3.

**Drone start position:** Stand where the camera "opens" — 15–25 blocks back from the
Armorer mark, in the direction the camera will travel FROM. Face toward the Armorer when
you run `/scaena set 5.x` — the plugin captures yaw/pitch, and the drone's opening
facing is exactly what you capture here.

---

## Site A — Home Base: "The Workshop"

**One quality:** A craftsperson's workplace. Functional, slightly worn. Not staged.
The Vindicator is behind a wall here from show open — heard, not seen.

**LOCKED PRE-SCOUT DECISIONS:**
- Near a naturally-generated pillager outpost. Tower must be visible from inside as
  background context — do not obscure it.
- Small enclosed outbuilding near the tower (natural structure or light build).
  Windows/openings oriented toward tower.
- Armor stand on a 1-block pedestal, 6–8 blocks from player arrival, center-frame.
- Armorer Villager fenced in the space with a Blast Furnace job site block (place it —
  not naturally generated at outposts).
- **Iron door in one wall:** separates the workshop from the Vindicator's holding space.
  Door closes at show start, opens at finale. Vindicator waits on the other side.
- **Spawning safety:** >72 blocks from tower center. Lit interior (light ≥8).

**Plugin marks at home base:** 1.1 (arrival) + 2.1 (Armorer) + 2.2 (Vindicator) + 3.1 (armor stand) + 3.2 (iron door). Run all four mark sets here.

**What to document beyond standard fields:**
- Iron door position (x, y, z) — Stage Management needs this for REDSTONE event
- Vindicator holding area — enough room for one entity, not visible from arrival
- Distance to tower center (confirm >72 blocks)

**Note:** No drone start position needed for home base — camera pattern applies to
expedition scenes only.

---

## Site B — The Helmet: "High Ground"

**One quality:** The player feels *above* things. Sky close. Ground visible below.

**LOCKED PRE-SCOUT DECISIONS:**
- Natural mountain peak, open sky. No structure needed.
- Zarathale digs a small divot at the summit for precise mark placement.
- Two marks: player arrival and Armorer position. Tight spacing.

**Key lines:**
> *"I like this spot. High up. Everything below looks manageable from here."*
> *"There it is. Chain. I thought about something heavier, but you move too fast for heavy."*

**Must-haves:**
- Player TP position safe — no fall risk on arrival
- Arrival facing toward the view or the drop (not a wall)
- Biome: mountains, windswept hills, mesa buttes, savanna plateau, tall taiga
- Open sky confirmed ✓ (assumed for this biome type)

**Extra capture:** Y altitude. Drone start position (stand 15–25 blocks back from
Armorer mark, at height, facing toward him — this is where the camera dolly begins).

---

## Site C — The Chestplate: "The Forge"

**One quality:** *"This place knows what it's for."* Residual purpose. Heat, or the
memory of it.

**LOCKED PRE-SCOUT DECISIONS:**
- Badlands biome, overworld. Lava present. Exposed mineshaft nearby as the visual anchor.
- Open sky confirmed ✓ — Mira's find-firework fully available here.
- Not nether (badlands earns the line without going underground).

**Key lines:**
> *"This place knows what it's for. You can still feel the old work in it."*
> *"Iron. Wide at the shoulders — I measured for this. You know I measured."*

**Must-haves:**
- No immediate fire/lava hazard at player TP-in position
- Arrival facing INTO the space — player enters the forge, doesn't walk past it
- Exposed mineshaft visible or accessible from scene position

**Extra capture:** Lava proximity in blocks (Effects fire resistance assessment).
Sky clearance above TP. Drone start position.

---

## Site D — The Leggings: "The Long Road"

**One quality:** Distance implied. A horizon. A path that asks you to keep going.

**LOCKED PRE-SCOUT DECISIONS:**
- Savanna or open plains village setting.
- Small armorer shop or building beside an open road.
- Open sky confirmed ✓ — Mira's find-firework fully available here.

**Key lines:**
> *"You'd cover a lot of ground in a place like this. I thought about that."*
> *"Iron. These were made to move. I want you moving — right until the end."*

**Must-haves:**
- Arrival facing DOWN the road or TOWARD the horizon — not across or into a wall
- The road quality is legible immediately — a place where distance is the point
- Village context nearby but not so close that NPCs distract or mob AI interferes

**Extra capture:** Hostile mob density in range. Drone start position (down the road,
behind the player arrival point — camera travels along the road toward the Armorer).

---

## Site E — The Boots: "The Swamp Floor"

**One quality:** *"Feel that."* The ground is earthy, wet, specific underfoot.

**TERRAIN LOCKED 2026-03-29: Swamp.** The show stays in the overworld. Swamp terrain
is the confirmed direction — clay and mud floor, shallow water, distinctive ground
that is immediately legible without being hazardous.

**Key lines:**
> *"Feel that. This is why boots matter. Most people never think about the ground."*
> *"Leather. Dark — I dyed these myself. I know what you walk toward."*

**Must-haves:**
- Terrain legibility: swamp floor material immediately obvious on arrival
- Safe player TP altitude — no fall damage
- Swamp biome confirmed (frog presence, muddy palette, water patches)
- Effects will assess whether any subtle sensation applies — swamp is traversable;
  they may pass this scene if no specific effect reads as "world doing it"

**Extra capture:** Exact floor material (mud, clay, water depth). Blocks above terrain
floor. Drone start position. Sky clearance.

**Lighting note — capture for Steve:** How dense is the canopy overhead at the Armorer
mark? Can you see the sky from ground level, or is it mostly covered? Roughly what
height is the tree canopy? Steve uses this to decide regular campfire (warm orange) vs.
soul campfire (cold blue-teal) at this site. Decision after scouting report is in.

---

## Site F — The Weapon: "The Choice" *(final expedition)*

**One quality:** *"I kept coming back to this place. It answers something."* Stillness
with weight — not emptiness, but a place that has earned its quiet.