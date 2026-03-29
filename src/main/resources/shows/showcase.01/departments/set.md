---
show_id: showcase.01
department: Set Director
document: Department Brief
updated: 2026-03-29
---

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

### Site E — The Boots: "The Burning Floor"
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
`kb/departments/set/stage-registry.md` before the show enters tech.

---

## Mob Containment

If any expedition site requires mob containment (creature AI wandering off stage), Set
designs it as needed — fence line, low wall, terrain modification. Containment is a
design task, not a scouting filter. Find the place with the right theatrical feel first;
design whatever containment is needed afterward.
