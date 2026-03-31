---
show_id: showcase.01
department: Effects Director
document: Department Brief
updated: 2026-03-31
---

# Effects — showcase.01 "Preparing for Battle"

## What This Department Serves

Effects applies player-sensation events — levitation, slow falling, speed, blindness,
nausea — where the location's physical qualities warrant it. Effects is not present in
every scene. The test for whether Effects belongs in a given expedition: *does this
terrain or space have a physical quality that the player would feel if they were actually
there?* If yes, Effects represents that quality. If no, Effects is not in the scene.

Effects must not read as "the plugin doing something." It must read as "the world
doing something."

---

## Scene Assessment

### Site B — The Helmet (high ground)
Height may warrant a brief slow-falling effect on arrival if the location involves any
drop or altitude exposure. This is optional and Set-dependent — if the player TP-in
is on a safe ledge, no effect is needed. If the site involves the player standing near
an edge at altitude, a brief resistance-to-falling quality could serve the place.

Effects proposes after Set confirms the altitude and terrain at the helmet site.

### Site C — The Chestplate (the forge / nether)
If nether: fire resistance is thematically appropriate and mechanically protective. A
brief fire resistance application serves both the story (the companion is in a hot place)
and the player (no fire damage if they step on lava-adjacent terrain).

If surface forge: fire resistance may still apply depending on terrain proximity to lava.
Set documents lava proximity at this site; Effects assesses accordingly.

No levitation. No speed. The forge site is about weight and depth, not motion.

### Site D — The Leggings (the long road)
No Effects involvement likely. The long road is about natural movement, not altered
movement. A speed effect here might seem logical thematically but would feel like a
plugin demonstration. Effects is not in this scene unless Set scouts something that
genuinely warrants it.

If the traversal site involves deep water or swamp terrain: water breathing or
slow-falling adjustments are possible. Effects holds until Set delivers the scouting report.

### Site E — The Boots (the terrain site)
**This is Effects' primary scene.** The boots are about what's underfoot, and the terrain
type should be felt.

**Terrain locked 2026-03-29: Swamp.** The show stays in the overworld. Swamp terrain
is earthy, specific, distinctive underfoot — clay and mud floor, shallow water patches.

**Effects assessment for swamp:** No levitation, no fire resistance. The swamp floor
is traversable but particular. Effects proposes at intake whether any subtle movement
quality serves the scene — the earthy resistance of shallow water, the heaviness of
soft ground. If no specific effect reads as "world doing this," Effects passes. The
terrain itself may be sufficient. Effects waits for Set to confirm exact terrain detail
before deciding.

### Site F — The Weapon (the choice — final expedition)
**Updated 2026-03-29.** Previous guidance: Effects passes entirely. New direction:
particles as the find-beat substitute.

Since there is no firework at this scene, and since F is now the final expedition,
a subtle particle effect at the discovery moment may serve as the scene's visual beat
without adding spectacle. The concept: at the moment the Armorer picks up the axe,
ground particles fire in the cleared ruins space — something that reads as *the ground
recognizing this*, not as a celebration. Embers. Ash disturbed from the earth.
Something that has been waiting.

**Requirements for this to work:**
- Set must scout a ruin with a cleared, flat central space — open enough that particles
  read against the ground, not lost in clutter
- The surrounding ruin structure should frame the cleared space so particles are visible
  and atmospheric, not competing with ambient environment detail
- No lava glow, no active fire, no ambient particle sources that would drown the effect

**This is a proposal for intake, not a locked decision.** If Set's location makes it
feel like the world is doing it, Effects authors it. If it reads as the plugin doing
something: Effects passes and the silence holds alone. Both are valid. Direction
decides after Set scouts and confirms the environment quality.

---

## Home Base

No Effects involvement at home base unless Set identifies a specific physical quality
that warrants representation. Home base is a craftsperson's workshop — grounded,
interior, functional. No player-sensation events here.

---

## The Reveal (A-Final door open)

No Effects at the reveal. The Hero's arrival is a visual and staging event. No physical
sensation should pull the player's attention from what they're seeing.

---

## Victory Coda — Fight Branch

**New 2026-03-31.** If the player chooses Fight and defeats the Vindicator, an 8-second
victory coda fires. Effects owns the levitation beat.

**Brief:** The player is lifted — the world celebrates the outcome. Not a tutorial float.
Not a demonstration. The ground releasing the player upward is the world's acknowledgment.
It should feel momentary and earned, not prolonged or disorienting.

**Parameters (from show-params §Battle Sequence):**

| Param | Status |
|-------|--------|
| Duration | 160 ticks (8 seconds) |
| Effect | `ENTITY_EFFECT: levitation` applied to player |
| Amplifier | **TBD — Effects to propose after confirming ceiling clearance** |
| Descent handling | **TBD — see safety note below** |

**Ceiling clearance — critical question:**
The fight occurs at the Vindicator's holding position. Per scouting (2026-03-30), this
position is open sky (vindicator_spawn: open sky). If the fight stays in that area,
levitation is unconstrained. However, if the player can drift back under the workshop
ceiling (0–4 blocks) during or after the coda, levitation will pin them against the
ceiling for the duration. Effects must confirm: *is the fight area open sky for the full
8-second coda?*

**Fall safety:** At amplifier 1, levitation 1 floats the player ~7 blocks upward over 8
seconds. When the effect expires, the player falls. Without a soft landing, this is a
survival fall. Effects proposes one of:
- Low amplifier (0 = ~0.9 blocks/tick upward, more modest height)
- `ENTITY_EFFECT: slow_falling` applied at the end of the levitation window to cushion
  the descent (coda cue ends with a slow_fall tail)
- Player teleported back to ground at coda end (Stage Management handles the TP)

Effects proposes the amplifier and descent approach at intake, informed by the fight
location's open-sky confirmation from Set scouting.

---

## Coordination Requirements

Effects cannot finalize any expedition scene until Set delivers:
- Terrain type (exact material/biome) — Site E now confirmed as swamp
- Safe player TP altitude (blocks above terrain floor)
- Lava proximity notes for forge site (C)
- Sky/ceiling clearance at relevant sites
- **Site F:** Cleared space dimensions and surrounding ruin structure — needed to
  assess whether the particle beat is viable in this environment

Effects brings confirmed proposals to the intake conversation. No YAML until intake.
