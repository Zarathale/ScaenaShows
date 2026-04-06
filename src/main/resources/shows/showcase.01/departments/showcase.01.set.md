---
show_id: showcase.01
department: Set Director
document: Department Brief
updated: 2026-03-30
---

<!-- SCENE A — Set Department Update filed 2026-03-30. See section below. -->

# Set — showcase.01 "Preparing for Battle"

## What This Department Serves

Set scouts and establishes all six locations: home base and five expedition sites (B
through F). The shield site (G) is cut — five expeditions total. No YAML is written
until Zarathale has scouted and documented real-world coordinates for every location.

Set is the third mover in this show's production order, after Casting (companion + Hero)
and Wardrobe (armor kit). Wardrobe's kit design informs the scouting lens — what the
armor looks like affects what "where this piece belongs" should feel like in-world.

---

## The Scouting Lens

Each expedition site is selected for its resonance with a specific armor slot. The logic
is poetic, not literal. The forge doesn't need a crafting table. The height location
doesn't need to be labeled. The connection between place and piece should be felt —
experienced — not explained.

The question for every candidate location: *does this feel like where this piece was
waiting?*

Not: is this an impressive build? Not: is this easy to navigate? The question is always
about the piece and the place belonging to each other.

---

## Home Base

The companion's workshop. The armor stand is the centerpiece. The player arrives into
this space six times — at show open and after each expedition. They must recognize it
immediately on every return.

Home base should read as a craftsperson's space: functional, specific, slightly worn.
Not a staged room. A place someone actually works in. The armor stand is placed
prominently — the player should be able to see it clearly on arrival and notice when a
new slot fills.

**What Set documents for home base:**
- Coordinates (x, y, z) and world
- Arrival facing (compass direction or yaw/pitch) — must put the armor stand in the
  center of the player's first view
- The armor stand's exact position (Stage Management places this; Set confirms the space)
- Any containment or staging concerns (mob paths, nearby AI interference)
- **Blast furnace placement:** Home base requires a blast furnace as a practical set piece —
  it is the workshop's ambient sound source and visual anchor (lit glow). Scout for a space
  that either already contains one or has room to place one in a natural workshop position.
  The furnace should be visible to the player on arrival but not competing with the armor
  stand as the primary focal point.

---

## Expedition Sites

### Site B — The Helmet: "High Ground"
**Slot:** Helmet (head protection, vision, sky-facing)

Scout for a location with height and exposure. A mountain peak, a watchtower, a stone
formation at altitude, a mesa clifftop. The quality to find: the player should feel
above things. The sky should be close. The helmet was left here — or found here — by
someone who understood that protection begins with being able to see what's coming.

The space doesn't need to be large. It needs to feel like the top of something.

**What Set documents:** Coordinates, arrival facing, safe player position (no fall risk
at TP-in), ceiling clearance if relevant, biome, any notable terrain features.

### Site C — The Chestplate: "The Forge"
**Slot:** Chestplate (core protection, weight, the largest piece)

Scout for a location with heat, depth, or the memory of making. An active lava forge, an
abandoned nether-adjacent structure, a blaze-adjacent ruin, an overgrown smithy in a
badlands or nether biome. The chestplate is the heaviest piece and the show's most
weighted expedition. The place should have gravity in it — not dramatic decoration, but
the feeling of something important having been done here.

Nether is likely for this site — netherite calls for it. But if a surface forge exists
with the right quality, it is worth considering.

**What Set documents:** Coordinates, arrival facing, biome (including nether biome if
applicable), lava proximity (Effects needs this for levitation safety assessment),
ceiling clearance, ambient light level.

### Site D — The Leggings: "The Long Road"
**Slot:** Leggings (movement, distance, the sustained effort of going)

Scout for a traversal space — a path through a biome that implies distance. A river
valley with a visible horizon. A road or cleared path through a forest. A plains stretch
with something at its end. The leggings protect movement; the place should feel like
movement happens here, like distance is the point.

This is the show's middle beat — not the most dramatic, not the lightest. Set should
find a place with a sense of continuity to it, not a destination but a passage.

**What Set documents:** Coordinates, arrival facing (should orient toward the path or
horizon, not away from it), path direction if applicable, biome.

### Site E — The Boots: "The Swamp Floor" *(locked 2026-03-29)*
**Slot:** Boots (terrain, what's underfoot, the tactile ground)

Scout for a location with distinctive, challenging terrain. Magma field, soul sand valley,
snowfield, deep swamp, clay riverbed, ash-covered basalt delta floor. The boots are about
the ground — the place should make you aware of what you're standing on.

This site has potential for Effects involvement (slow/fast/levitation relative to terrain
type). Set must document the terrain type in detail so Effects can assess whether a
movement sensation is warranted and safe.

**What Set documents:** Coordinates, arrival facing, exact terrain type (material/biome),
safe altitude above terrain for player TP, any terrain hazard notes for Effects.

### Site F — The Weapon: "The Choice" *(final expedition)*
**Slot:** Main hand weapon (intent, will, the decision to fight) — the last piece

Scout for a location that has the quality of a decision having been made here. A ruin of
something that was defended. A pillager outpost (claimed from an adversary — the
companion took their weapon, which is its own statement). A cleared battlefield. A place
with a monument, a crater, a broken wall. The weapon is the piece that implies intent.
The place should share that quality.

This is the show's most emotionally weighted expedition AND its final one. The location
should have stillness to it — not emptiness, but the specific quiet of a place where
something significant happened. Set should find a place that earns that quiet. The
player departs from here to witness the reveal.

**What Set documents:** Coordinates, arrival facing, any AI-active hostile mobs in
range (needs clear answer — this scene holds the show's most important silence), biome,
notable structures or features, sky clearance (fireworks explicitly excluded here, but
document regardless).

---

## Scouting Format

For each location, Zarathale files a scouting report with:

```
Location: [Site letter and name]
World: [overworld / nether / end]
Coordinates: x=__, y=__, z=__
Arrival facing: yaw=__, pitch=__
Biome: __
Sky clearance: __ blocks (or "obstructed")
Terrain notes: __
Hazard notes: __
Theatrical read: [1-2 sentences — why this place fits the piece]
Candidate status: [recommended / alternate / rejected with reason]
```

Every location confirmed by Set enters the permanent stage registry in
`kb/departments/set/set.stage-registry.md` before the show enters tech.

---

---

## SCENE A — Set Department Update
*Filed: 2026-03-30. Site A scouted this session. Coordinates confirmed. Spatial story partially resolved — narrative layer TBD pending Alan's in-game observations.*

---

### What Is Confirmed

Zarathale scouted Site A on 2026-03-30. Six marks captured with full coordinates, yaw/pitch, and sky access. These are the settled facts.

| Mark | Role | Coords (x/y/z) | Sky Type | Ceiling |
|------|------|----------------|----------|---------|
| home_base | Player arrival | 192.98 / 80.0 / 306.16 | Underground | 2 blocks |
| armor_stand | Armor stand position | 194.54 / 81.0 / 300.49 | Enclosed | 4 blocks |
| iron_door | Iron door block | 190.57 / 80.0 / 300.51 | Underground | 0 blocks |
| companion_spawn | Armorer Villager | 196.55 / 80.0 / 302.31 | Open sky | — |
| vindicator_spawn | Vindicator offstage | 193.52 / 84.0 / 296.38 | Open sky | — |
| blast_furnace | Blast furnace | 197.70 / 80.0 / 304.29 | Open sky | — |

**Blast furnace block coords (pre-BLOCK_STATE authoring):** x=198, y=79, z=304 — confirm in-game before writing.

---

### What the Data Is Telling Us

The coordinates reveal a spatial arrangement more complex than the original brief assumed. Several findings carry direct direction implications.

**1. The player's space is genuinely tight.**
home_base ceiling: 2. iron_door ceiling: 0 (at an exterior face or thick wall). armor_stand ceiling: 4 — the highest enclosed point. This is a very low-ceilinged interior — not a spacious craftsperson's workshop, but a small outbuilding or ground-floor chamber. The player arriving here is in a cramped, enclosed space, not a room they can survey.

**2. The blast furnace is outside — or at an exterior wall.**
Open sky reading at the blast_furnace mark means it is not inside the enclosed play space. It is either at an exterior wall of the structure, in an adjacent courtyard, or in a semi-open attached area. This directly affects Lighting's plan: the furnace is the proposed ambient light source for the workshop interior, but if it's outside, the warm glow it produces enters the enclosed space from outside (through a window? an opening? a doorway?) rather than filling a room the player is inside.

**3. The companion spawns outdoors.**
companion_spawn is open sky. The Armorer does not start inside the enclosed chamber with the player — he is outside, in the same open-sky zone as the blast furnace. This changes the staging of Scene A: the companion is not a workshop presence, he is someone who enters from, or calls from, the adjacent exterior.

**4. The Vindicator is elevated and outside.**
vindicator_spawn at Y=84 is 4 blocks above the workshop floor (Y=80). Open sky. He is not behind a door at player level — he is above the structure, on a ledge, a rampart, a second floor, or the outpost platform. "Heard through the wall" may need to become "heard from above" or "heard from outside, above."

**5. The iron door ceiling reads as 0.**
ceiling: 0 at the door mark means it is either in a wall at grade level, or blocked entirely above — the door is not in an open room but embedded in a structure with no space above it from that standing point. This likely means the door opens from the enclosed chamber to the exterior, not between two equal interior rooms.

**Working spatial picture (draft — needs narrative confirmation):**
The player is placed in a small, low-ceilinged interior chamber within or attached to a pillager outpost structure. The armor stand is in the deepest/most enclosed part of that chamber (slightly higher ceiling — 4 blocks). The iron door is in the exterior wall of that chamber. Outside: the companion, the blast furnace, and open Sparse Jungle sky. The Vindicator is above — on an upper level or rampart of the outpost structure, 4 blocks up, not at ground level.

---

### What Has Evolved From the Original Brief

The original direction described "a craftsperson's shop — functional, specific, slightly worn" as a unified interior space. The scouted space appears to be something different in structure: the player is enclosed in a small interior chamber, and the companion's workspace (furnace, activity) is outside or adjacent, not in the same room.

This is not a problem. It may be a better story. But it changes how we stage Scene A:

- **Original assumption:** companion and player share the workshop interior; furnace is their ambient.
- **Emerging picture:** player is in the enclosed chamber (armor stand visible, door closed); companion is the figure in the exterior/courtyard; furnace light enters from outside.

This also has a strong implication for the iron door mechanics. If the door opens to the outside (not between two indoor rooms), the Vindicator being on the other side of it — now also outside and above — becomes a different spatial relationship. The door may separate the player's enclosed chamber from a shared exterior where both the companion and the Vindicator are present (one at grade, one elevated).

**Direction will need to address:** how the staging of the companion's presence, the furnace light, and the Vindicator's position above now combine into a coherent spatial story for Scene A.

---

### Environment Notes — Site A (Draft — awaiting narrative input)

```
Scene: A — Home Base "The Workshop"
World: world
Biome: Sparse Jungle
Player arrival (home_base): x=192.98, y=80.0, z=306.16 | yaw=-133.65, pitch=0.00
Sky access: Underground (enclosed outbuilding interior)
Ceiling height: 2 blocks at arrival point; 4 blocks at armor stand position
Ambient light: Cave/block-light only in player's main space (no sky light)
Block light anchor: Blast furnace at x=198, y=79, z=304 — open sky position; warm
  glow enters enclosed space from exterior (direction/opening TBD — needs narrative)
Weather visible: No (interior)
Key sight line on arrival: [TBD — Alan to confirm what player sees facing NE at -133.65 yaw]
Companion position: Open sky, exterior side of structure — enters from / calls from outside
Vindicator position: Open sky, Y=84 — elevated 4 blocks above workshop floor —
  ledge/rampart/upper platform (structure TBD — Alan to confirm)
Iron door: Exterior wall of chamber (ceiling 0 at door face) — opens to exterior, not
  to second interior room
Special notes:
  - Nighttime mob spawn risk LOW in main interior (no sky light; block light from furnace
    reaches in if sight line confirmed — verify coverage of player space)
  - Companion and blast furnace both outside: Scene A staging is exterior/interior split,
    not shared-room
  - Vindicator above, not beside: sound "through the wall" may read as sound from above
  - All six marks confirmed; yaw/pitch available in scout_captures/showcase.01/2026-03-30.yml
```

---

### What Is Still TBD — Narrative Inputs Needed

These cannot be resolved from coordinates alone. The answers come from Alan's in-game experience:

| # | Question | What it unblocks |
|---|----------|-----------------|
| 1 | What does the player see when they arrive facing NE (yaw -133.65)? What is the first impression of the interior? | Key sight line for Camera; first atmospheric read for Sound and Lighting |
| 2 | Is the blast furnace visible through an opening, window, or door from inside the chamber? Or is it around a corner / fully exterior? | Lighting block-light reach plan; whether warm glow reads in the player's space at all |
| 3 | What is the companion's relationship to the interior — does he step through a door on arrival, appear in a window, call from a courtyard? | Staging of companion's opening presence; Voice line delivery register |
| 4 | What is the Vindicator standing on at Y=84? Is this the outpost upper platform, a balcony, a ledge? Is it visible from inside the chamber, or fully out of sight? | Vindicator's "heard/readable but not seen" staging; whether iron door is the only separation or if there's structural separation too |
| 5 | Does the enclosed chamber feel like a holding room, a storeroom, an antechamber — or something else entirely? | Direction tone for Scene A; whether the player feels "placed here" vs. "arrived here as a craftsperson's space" |
| 6 | What is the Sparse Jungle canopy doing to light? Dense overhead? Gaps? | Sound ambient baseline; sky light bleed into exterior area (companion and vindicator positions) |

---

## Mob Containment

If any expedition site requires mob containment (creature AI wandering off stage), Set
designs it as needed — fence line, low wall, terrain modification. Containment is a
design task, not a scouting filter. Find the place with the right theatrical feel first;
design whatever containment is needed afterward.

**Nighttime spawning — this show specifically:** showcase.01 runs from MC 12,500
(twilight) to MC 22,000+ (pre-dawn). Deep-night expedition sites (D, E, F) are in full
darkness by the time the player arrives. In Java 1.18+, hostile mobs spawn when block
light is 0 — sky light no longer prevents surface spawning at night.

The Armorer stands at each expedition site. In open terrain (Sites D, F) the clear-sky
light floor of 4 provides marginal protection in open areas. In swamp terrain (Site E),
heavy canopy can reduce effective sky light to 0 at surface level — this site has active
mob spawn risk.

**Campfire as dual-purpose set piece:** The campfires proposed by Lighting at Sites D,
E, and F (see `departments/showcase.01.lighting.md`) serve double duty — atmosphere AND mob
containment. A campfire at light level 15 creates a ~14-block no-spawn zone from its
center. A soul campfire at level 10 creates a ~9-block zone. If campfire placement is
confirmed by Lighting, the spawn risk at the Armorer's position is managed.

Set documents campfire placement in the scouting report alongside coordinates.
If Lighting adjusts campfire position, update the spawn-safety assessment to confirm
the Armorer's mark remains within the no-spawn radius.
