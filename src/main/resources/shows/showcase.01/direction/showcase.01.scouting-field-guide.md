---
show_id: showcase.01
document: Scouting Field Guide
version: v3
updated: 2026-03-30 (plugin commands added; consistent with 5-expedition structure)
use: In-game reference — keep open on second screen while scouting
---

# Scouting Field Guide — showcase.01 "Preparing for Battle"

> Six locations. One question per site: *does this feel like where this piece was waiting?*
> Walk to each mark. Capture coordinates with the plugin. Note everything else in F3 and the report.

---

## Plugin Workflow — How to Scout

The plugin handles coordinate capture. You do not need to manually copy F3 coordinates.

**Start your session:**
```
/scaena scout load showcase.01          — loads all 10 marks, shows sidebar
/scaena scout load showcase.01 sites    — just the 6 site marks (start here)
/scaena scout load showcase.01 spawns  — cast positions (Armorer + Vindicator)
/scaena scout load showcase.01 props   — set piece positions (armor stand + iron door)
```
Recommended: start with `sites` tag, then reload with `spawns` and `props` once you have the home base set up.

**Check what's loaded:**
```
/scaena scout status   — shows all marks with codes; captured ones show coordinates
```

**Capture a mark:**
Stand at the position. Type:
```
/scaena set 1.1       — captures your exact position for that code
```
Chat confirms: `✓ 1.1 home_base — (x.x, y.y, z.z)`. Mark disappears from sidebar.
You can re-capture a mark by running `/scaena set <code>` again — it overwrites.

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

**Still use F3 for:** biome name, sky type, ceiling height, canopy density, light level, visible hazards. Those are descriptive notes — they go in the scouting report, not the plugin.

---

## Mark Reference — showcase.01

| Code | Name | What it is |
|------|------|-----------|
| 1.1 | home_base | Site A — player arrival position at The Workshop |
| 1.2 | high_ground | Site B — player arrival position at High Ground |
| 1.3 | the_forge | Site C — player arrival position at The Forge |
| 1.4 | the_long_road | Site D — player arrival position at The Long Road |
| 1.5 | the_swamp_floor | Site E — player arrival position at The Swamp Floor |
| 1.6 | the_choice | Site F — player arrival position at The Choice |
| 2.1 | companion_spawn | Armorer Villager — opening position at home base |
| 2.2 | vindicator_spawn | Vindicator — behind-wall holding position at home base |
| 3.1 | armor_stand | Armor stand staging position |
| 3.2 | iron_door | Iron door target (stand in the doorway) |

**Not yet in the plugin (capture manually for now):** Armorer mark per expedition site (2–4 blocks from your arrival mark) and drone start position per expedition site (15–25 blocks back from Armorer mark). Note these in the scouting report as separate coordinates.

---

## What to capture at every site

```
Site code + /scaena set:   ← captures arrival position
Armorer mark position:      x=    y=    z=    ← note manually (2–4 blocks from arrival)
Drone start position (far): x=    y=    z=    ← note manually (15–25 blocks back)
Biome (F3):
Sky:           open / partial / enclosed / underground
Ceiling:       open / __ blocks
Sky clearance above TP: __ blocks  ← Fireworks need this at every site
Ambient light:
Hazards:
Theatrical read (1–2 sentences — why this place fits the piece):
Status:        recommended / alternate / rejected: [reason]
```

**Drone start position:** Stand where the camera "opens" — 15–25 blocks back from the
Armorer mark, in the direction the camera will travel FROM. This is where the invisible
drone spawns. The drone moves from here toward the Armorer. Note F3 coordinates the same
way as the arrival mark.

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

**Slot:** Iron axe, Sharpness I — the piece that implies intent. The last piece. The show
ends here before coming home to the reveal.

**Key lines spoken here:**
> *"I kept coming back to this place. It answers something."*
>
> *"Iron axe. I had it sharpened once. That's all you need — one good edge."*
>
> *"[silence or 'Five. Let's go home.' — Voice decides at intake]*"

**Must-haves:**
- **Hostile mobs must be answered.** This scene holds the show's most important silence.
  Active spawning in-frame kills it. If pillager outpost: note patrol AI range.
- No ambient spectacle — no lava glow, no particle effects, nothing competing with quiet
- Arrival facing toward the visual anchor (ruin, cleared site, whatever reads as the reason)

**PRE-SCOUT DIRECTION LOCKED 2026-03-29:** Ruin with a cleared central space.
Flat, open ground surrounded by ruin structure. The cleared space is the scene's
visual center — the Armorer is in it, the axe is in it, and (pending Effects
confirmation) particles fire into it at the discovery moment.

**What Zarathale is looking for:** A ruin where some event or force cleared the
center — a courtyard, a collapsed interior, a blast-cleared area within broken walls.
The surrounding structure frames the space; the clearing is what you arrive into.
The ground in the cleared area should be flat and visually simple (dirt, stone,
ash-covered — nothing that creates visual noise competing with particles).

**Strong candidates:** Pillager outpost courtyard (taking their axe from their own
ground is its own statement), a surface ruin with collapsed walls, a cleared site
of past conflict with surviving wall fragments framing it.

**⚠ NO FIREWORK AT THIS SITE.** The find-firework pattern fired at B, C, D, and E.
The absence here is intentional and now carries maximum force: this is the FINAL expedition.
Do not compensate. Camera holds; Sound steps back; Effects does nothing. The silence
after line 2 is the beat — and the last expedition beat the player will feel before the reveal.

**Extra capture:** Every notable structural or terrain feature. Cleared space
dimensions (width × depth approximately). Ground material in the cleared area.
Surrounding structure height and framing quality. Mob AI answer (can mobs be
contained, or is natural spawning suppressed here?). Drone start position.
Sky clearance (no firework here, but document regardless).

---

## After scouting

1. `/scaena scout save` — write the capture file to the server
2. Tell Alan you're done — he pulls the file and merges coordinates into show-params
3. Fill in the descriptive fields in `showcase.01.scouting-report.md` and commit
4. Michael C. translates to Environment Notes in `departments/showcase.01.set.md`
5. Stage registry entries go in `kb/departments/set/stage-registry.md`
6. Direction closes Gate 3 and schedules intake conversation

**Six locations total:** home base (A) + five expedition sites (B through F).

**The TBA flags in script-v1** will update to match the confirmed site character
once scouting delivers. Voice revises to v2 at that point.
