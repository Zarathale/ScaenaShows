---
show_id: showcase.01
department: Sound Designer
document: Department Brief
updated: 2026-03-29
---

# Sound — showcase.01 "Preparing for Battle"

## What This Department Serves

Sound (Gracie) scores each location with its own sonic identity — true to where it
actually is — and manages the emotional pacing of the show through what it plays and
what it withholds. Sound is present in nearly every scene but should register as
atmosphere, not score. The player should feel like they've arrived somewhere, not like
a soundtrack has been cued.

The show's most important sound task: hold the silence in the weapon scene (F) without
filling it. Sound owns the weapon scene's restraint — and F is now the final expedition,
which makes this restraint the show's last sonic act before home and the reveal.

---

## Home Base

Home base is the Armorer's workshop — a craftsperson's space where the preparation
actually happens. The player is the observer here; the antechamber between the Armorer
and the Vindicator behind the wall.

**The ambient bed is the blast furnace.** A blast furnace is a set piece at home base.
When it is activated (lit=true via BLOCK_STATE — see ops-inbox.md), it produces both
the glow and the native crackle sound in-world. That crackle is the home base ambient
layer. No authored SOUND bed needed — the block does the work.

**Java gap bridge:** Until BLOCK_STATE is implemented, Sound authors
`minecraft:block.blastfurnace.fire_crackle` as a SOUND event at show open (low volume,
ambient channel, max_duration_ticks to match scene length or restated per section).
Once BLOCK_STATE ships, the authored SOUND events are removed and the live block
carries the bed.

**Consistency requirement:** The home base ambient must be identical on every A-section
return. The player recognizes home by its sound — the same furnace, the same room.
If the ambient state risks changing (weather, time-of-day), Sound issues a state-restore
on each A-section arrival. If using the live block (post-BLOCK_STATE), the furnace stays
lit for the full show and no restore is needed.

---

## Expedition Sites

### Site B — The Helmet (high ground)
Wind is the primary sonic signature of altitude. If the location is genuinely high,
native Minecraft wind ambient may be sufficient — Sound documents whether it is and
whether any authored layer adds to it or competes with it.

This is the show's first expedition. Sound should be clean and purposeful here — no
mood-building, just where you are.

### Site C — The Chestplate (the forge)
The forge or nether site has its own ambient: blaze sounds, lava, the ambient drone of
the nether. Sound documents what the native bed is and assesses whether it already
carries the scene. Nether ambient is usually strong enough on its own.

If this is a surface forge: lava sounds, the creak of a structure, ambient heat if
achievable. Sound should aim for "working heat," not "dramatic lair."

### Site D — The Leggings (the long road)
A traversal biome has a lighter ambient touch than the forge or the height site. Wind,
grass, water if a river is present, birds if in a forest. This is the show's middle
beat — Sound should not escalate here. Let the environment breathe.

### Site E — The Boots (the terrain site)
**Terrain locked 2026-03-29: Swamp.** Sound documents what swamp ambient gives —
water, frogs, the specific wet quiet of a swamp floor — and assesses whether it
serves the scene without modification. Swamp ambient is distinctive and earthy;
it may be sufficient on its own.

This scene is tactile. The sound should feel like something underfoot, not overhead.
Do not try to score the terrain; let it speak.

### Site F — The Weapon (the choice — final expedition)
This is the show's most emotionally weighted stop AND its final expedition. Voice
may be silent here. Lighting will not escalate. Sound must hold the space with
equal restraint.

**The brief for this scene is: less.** Whatever ambient the location provides, Sound
may thin it — not silence it (silence is its own event and should be used deliberately),
but reduce it to something that feels like a held breath. The player should feel the
weight of the place without being pushed toward a feeling by a soundtrack.

The location is a ruin with a cleared central space. Sound documents what the native
ambient is at that kind of site and assesses whether to thin it or hold it.

If Effects authors a particle beat at the discovery moment: Sound must not compete
with it. The particle beat is a visual event; Sound steps back and holds the existing
ambient underneath it.

After the companion collects the weapon: hold the ambient until the TP home fires.
No resolve, no swell, no punctuation. The player is going home to the reveal.

---

## The Finale

The Hero arrives. Sound does not cue a fanfare.

If anything: hold the home base ambient and let the visual do the work. If Sound can
find a moment where the world sounds like it recognized what just happened — the quality
of a door opening onto something expected — that is the correct register. Not dramatic.
Acknowledged.

The silence after the Hero appears is part of the show. Hold it.

---

## What Sound Does NOT Do

- Does not provide scored music in the traditional sense (no leitmotif, no swell)
- Does not fill the weapon scene with underscore
- Does not compete with Mira's fireworks in any shared scene
- Does not use sound events to signal emotional content ("now feel something")

Sound establishes place. The emotion is the player's.
