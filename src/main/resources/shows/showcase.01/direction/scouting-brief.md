---
show_id: showcase.01
document: Scouting Brief
author: Michael C. (Set Director)
updated: 2026-03-28
status: open — scouting not yet started
---

# Zarathale Scouting Brief — showcase.01 "The Cabinet"

> This is your in-game task list. Six locations. Take notes as you go — coordinates,
> what you saw, what made the place feel right or wrong. Bring those back and Michael C.
> will translate them into stage registry entries and Environment Notes.
>
> **The lens for all six locations:** Every space in this show is something the Allay
> found and thought worth keeping. Ask yourself, at each candidate: would a small,
> eccentric creature collect this place? If yes, you're probably in the right spot.

---

## What to Record at Every Location

Use F3 screen. At the player arrival point, capture:

- **XYZ** (the exact coordinates where the player will arrive)
- **Yaw** (facing direction — visible in F3 as "Facing")
- **World name** (top of F3 screen — matters most for the nether and end)
- **Biome** (shown in F3)
- **Ceiling** — is the sky open? If not, how many blocks to the ceiling?
- **Sight line** — what does the player actually see when they arrive facing that yaw?
- **Any quirks** — lava nearby, mobs spawning, anything unexpected

Then walk the space. Note anything worth flagging for other departments.

---

## Task 1 — Home Base

**The premise:** This is where the Allay lives. The player arrives here first and returns
here between every vignette — five or six times. It must feel genuinely inhabited, not
scenic. The Allay is not in a nice place; it's in *its* place.

**What to look for:**
- Not a viewpoint or a landmark. A place where someone works and wanders.
- Enough open space for: the Allay at center, 2–3 collected creatures nearby, and the
  player at a comfortable arrival point roughly 8–12 blocks from center.
- Sheltered but not enclosed — the sky visible, but edges that make it feel like a room,
  not a field. Trees, rock formations, terrain dips — something that makes it feel bounded.
- A single distinctive feature at close range. Something the Allay might have noticed and
  decided to stay near. A peculiar tree, a patch of glowing moss, a small waterfall feeding
  a pool, a cluster of flowers in an unexpected place.
- Works in evening light (the show will set TIME_OF_DAY ~13000 here — golden, pre-dark).
  Check that the space looks good in that register, not just at noon.

**What to record:**
- Player arrival point: XYZ + yaw (facing toward center)
- Center of the usable space (where the Allay will stand): approximate XYZ
- The distinctive feature in one sentence: "There's a ___ at ___, which ___."
- Ceiling status: open sky? How open?
- Any enclosure notes (trees, rock walls, terrain)

**Slug proposal:** Name it based on what you find. `home_base_[feature]` — e.g.,
`home_base_mossy_hollow`, `home_base_stone_ring`, `home_base_allay_pool`.

---

## Task 2 — "The Still Water" (Vignette B)

**The premise:** The player arrives here and is left alone. No events tell them where to
look. The exhibit IS the place — the water, the light, the specific quality of being here.
The Allay found this place and brings the player here to just... be in it for a while.

**What to look for:**
- Something genuinely worth noticing — not built, found. A lake with an odd bank. A
  waterfall falling into a cave entrance. A narrow river valley. A spring between rocks.
- Water present and visible on arrival. It doesn't have to be the only thing, but it should
  be the first thing.
- Open enough to walk around in — the player has free exploration, so 15–20 block radius
  at minimum.
- Check it at dusk (TIME_OF_DAY ~13000–14000). The brief calls for dusk. Walk to the water's
  edge and look around — does the light do something here? If the light is uninteresting, the
  vignette has nothing.
- Arrival sight line: the player should face into the most interesting part of the space.
  Don't face a cliff wall.

**Avoid:** Flooded caves (too enclosed for free exploration). Ocean biome (featureless at
ground level). Flat plains with a puddle. The space should reward looking around.

**What to record:**
- Arrival point: XYZ + yaw (facing into the interesting part)
- Water type: lake / river / waterfall / spring
- What the player sees on arrival (one sentence)
- What it looks like at dusk — good or meh?
- Exploration radius estimate

**Slug proposal:** `still_water_[type]` — e.g., `still_water_falls`, `still_water_spring`.

---

## Task 3 — "The Theater" (Vignette C)

**The premise:** Two creatures are here when the player arrives. The player watches. The
vignette is whatever the creatures do. This space is a stage — it should feel like something
happens in it, not just near it.

**What to look for:**
- A clear "stage" area: slightly elevated ground, a clearing with something behind it (cliff
  face, tree line, boulder — a backdrop), a natural amphitheater dip, a flat area with
  defined edges. The player should read "stage" without being told.
- Clear sight line from the arrival point to the stage center (~10–15 blocks).
- Enough room for 2 entities and the visual relationship between them to read clearly.
- Daylight works well here — the comedy of the creature theater reads better in clear,
  unambiguous light.
- Don't reject a candidate because it's open or unenclosed. If the creatures need
  containment to stay in frame, Set will design it — a fence line, a low wall, invisible
  barrier blocks, whatever fits the space with least intrusion. Scout for theatrical feel;
  containment is a Set design task, not a scouting filter.

**What to record:**
- Player arrival point: XYZ + yaw (facing toward the stage center)
- Stage center: approximate XYZ
- What's naturally behind the stage (backdrop note — cliff, trees, open air, etc.)
- Approximate wing positions: where is there room for entities to be placed off to the
  sides of the stage area? (Rough XYZ or a directional note)
- Whether the space is naturally enclosed or open (Set will design containment either way,
  but knowing what's there helps)

**Slug proposal:** `theater_[feature]` — e.g., `theater_grove`, `theater_amphitheater`,
`theater_cliff_face`.

---

## Task 4 — "The Nether Valley" (Vignette D)

**The premise:** The Strider lives here. The player is the visitor. Lightly levitated
(2–4 blocks off the safe floor), they float above the terrain and observe a creature that
is completely at home in a place where the player is not. The nether's ambient register —
its light, its sound, its specific wrongness — is the exhibit.

**What to look for:**
- A nether biome with texture: basalt delta, soul sand valley, or nether wastes. Not a
  generic nether wastes flat. Something with visual interest — basalt columns, soul flame
  lighting, irregular terrain.
- Lava visible from the arrival point. It doesn't need to be directly underfoot; the Strider
  needs lava in-frame or nearby. A lava lake in the midground is ideal.
- Safe floor for player arrival — solid ground, not lava.
- **Ceiling height is critical.** The player will be levitated 2–4 blocks. The nether
  often has low ceilings. Stand at the arrival point and look up — how many blocks to the
  ceiling? Need at least 6 blocks clear above the player arrival point. More is better.
- Open enough in the foreground that a Strider wandering on lava will be visible.

**What to record:**
- Player arrival point: XYZ + yaw
- **World name as it appears in F3** (not just "nether" — the plugin needs the exact name)
- Biome: basalt delta / soul sand valley / nether wastes
- Safe floor elevation (Y value where the player arrives)
- Ceiling height: blocks from player arrival point to ceiling
- Lava visible from arrival? Where? (Brief note)
- Ambient light description: what does it look like?

**Slug proposal:** `nether_[biome_short]` — e.g., `nether_basalt`, `nether_soul_valley`,
`nether_wastes_valley`.

---

## Task 5 — "The Suspension" (Vignette E)

**The premise:** The player is elevated 15–25 blocks above where they arrived. Mira's
fireworks fire below and around them. The experience is being *inside* a firework display,
not watching one from the ground. Near-dark. The world below is visible. This moment
belongs to Mira.

**Two options — scout both if you can and bring back a recommendation:**

**Option A — Outer End islands:**
- Not the main island. The outer End — chorus trees, endstone, void below.
- Find a flat island or near-flat area large enough for the player to stand.
- Look down: how much void is there? Mira needs ~15–20 blocks of clear space below the
  player's elevated position for fireworks to fire through. The void is ideal.
- Perma-dark ambient (no TIME_OF_DAY needed). Fireworks will pop hard.

**Option B — High overworld:**
- Mountain peak or built platform at Y > 160 with open sky below on at least one side.
- The player arrives at the peak, then gets elevated another 15–20 blocks.
- Must be dark enough that fireworks are visible — check at night (TIME_OF_DAY ~18000).
- If there's a cliff edge, the player elevated over open air with ground visible below
  is a strong image.

**What to record (both options if applicable):**
- Option chosen (A or B) and why
- World name (if End)
- Player arrival point: XYZ + yaw
- Floor altitude (the Y at arrival — this is Mira and Effects' planning anchor)
- Maximum safe elevation above that: how many blocks up before hitting ceiling or running
  out of clear space?
- Clear downward space: is there 15–20 blocks of open space below where the player will be
  elevated? What's at the bottom (ground, void, water)?
- Ambient light: what does it look like at the relevant time of day?

**Slug proposal:** `suspension_end_[island]` or `suspension_peak_[feature]`.

---

## Task 6 — "The Contraption" (Vignette F)

**The premise:** A redstone contraption already exists in this space. The show activates
it. What it does is small, specific, and slightly absurd. The comedy is in the gap between
the setup (Sprite and Lighting building mild expectation) and the scale of the reveal.

**This task requires building.** You're constructing the contraption, not finding it.

**Design principle — scale DOWN:**
The contraption should do exactly one small thing. The smaller the thing, the funnier the
gap. A door opens to reveal one chicken. A piston places one flower. A note block plays
once. A dispenser fires one egg. A sign becomes visible. The temptation is to build
something impressive — resist it. The show's register is a natural history documentary.
The joke is that the Allay made this.

**What to build:**
- A clear "before" state visible from the player's arrival point. The player sees the
  contraption in its initial state and understands something is there.
- A trigger: one block that, when powered, causes the reveal. This block's XYZ is the
  REDSTONE event's target — you must know this coordinate.
- An "after" state that is immediately legible — the player understands what happened
  without narration.
- A creature (the punchline, if used) can be inside or behind the contraption in the
  "before" state — visible only after the reveal. If so, it needs a space to be in.

**Design ideas (pick one or bring your own):**
- A door (iron door, wooden door) at the end of a short path, with a single chicken behind
  it. Setup: the door is closed, a button is nearby. Reveal: door opens, chicken looks at
  the player.
- A piston pointing toward a flower pot, with the flower not yet placed. Reveal: piston
  activates, flower appears in the pot.
- A dispenser aimed at a glass platform at knee height. Reveal: fires one egg (it may
  hatch; it may not — either is funny).
- A lectern with a book, behind a wall panel that retracts on REDSTONE.

**What to record:**
- Trigger coordinates: XYZ of the REDSTONE-powered block
- Before-state description: what does the player see on arrival?
- After-state description: what happens when the REDSTONE fires?
- Is a REDSTONE OFF cleanup needed after the show? (Does the contraption reset, or does
  it stay in the "after" state until manually reset?)
- Any creature integration: what creature, where does it start, what does it do after?

**Slug proposal:** `contraption_[what_it_does]` — e.g., `contraption_chicken_door`,
`contraption_piston_flower`.

---

## Opportunistic: Task 7 — showcase.03 Welcome Stage

If you have time and the scouting session is going well, the Welcome Stage for showcase.03
is on the longer-term list. Brief is in `kb/departments/set/stage-registry.md §Task 8`.
Not required for this session — but if a location presents itself, note it.

---

## When You're Done

Bring back your notes — coordinates, descriptions, what you found, what you built. Michael
C. will take it from there: stage registry entries, Environment Notes, and the go signal
for the rest of the departments.

Don't worry about format. Raw notes are fine. The coordinates are what matters most.
