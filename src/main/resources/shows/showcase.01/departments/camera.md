---
show_id: showcase.01
department: Camera Director
document: Department Brief
updated: 2026-03-29
---

# Camera — showcase.01 "Preparing for Battle"

## What This Department Serves

Camera (Mark) is an orientation tool in this show. The player teleports into seven
distinct spaces (home base, six expedition sites). At each arrival, Camera confirms
the player is looking at the right thing — then releases. The show does not call for
sustained reorientation or dramatic sweeps.

The one exception is the finale. The Hero's reveal requires that the player is facing
the right direction at the right moment. Camera owns that.

---

## Per-Scene Direction

### A — Home Base (all returns)

On arrival: orient the player toward the armor stand. The armor stand is the show's
visual spine; the player must be looking at it when they land. Mark coordinates with
Set on the exact arrival facing — the armor stand position drives this.

On each return, the armor stand has one more slot filled. Camera does not re-orient
after arrival; the player will notice the change naturally. Camera lands them facing
it and releases.

### B — Site B: The Helmet (high ground)

Orient the player toward height — toward the sky, or toward the view that makes the
elevation legible. If the location has a drop, orient the player toward the drop (they
should feel height, not just be on it). Then release.

### C — Site C: The Chestplate (the forge)

Orient the player toward depth — the interior of the forge, the heat source, whatever
gives this space its gravity. The player should arrive feeling like they are entering
something rather than standing next to it. Then release.

### D — Site D: The Leggings (the long road)

Orient the player down the road or path — toward the horizon or toward the direction
of distance. The player should arrive looking *along* the traversal space, not *across*
it. Then release.

### E — Site E: The Boots (the terrain site)

Orient toward the most distinctive feature of the terrain underfoot — the magma field,
the snowfield, whatever makes this place tactile. Looking down at a slight angle can
serve this scene if the terrain warrants it. Then release.

### F — Site F: The Weapon (the choice)

This scene holds the show's most important silence. Camera delivers the arrival facing
and holds out. Do not re-orient mid-scene. The player should land looking at whatever
Set has placed as the scene's visual anchor (the ruin, the relic, the cleared site)
and be allowed to stay there.

### A-Final — The Reveal

This is Camera's most important moment in the show.

The Hero will arrive/spawn at a position Stage Management confirms at intake. Mark must
ensure the player is facing that position when the Hero appears. If the arrival facing
from Set already puts the spawn point in frame: Camera holds. If the Hero enters from
a direction the player isn't facing on TP-in: a single FACE redirect fires at the moment
of the Hero's spawn — not before, not after.

Do not reorient the player after the Hero is visible. Let the player look.

---

## Expedition Camera Template — ALL 5 Scenes (added 2026-03-29)

**This is a cross-show pattern.** All five expedition scenes (B through F) use the
same camera opening. Mark designs this as a single reusable template cue with
site-specific drone path parameters.

### The concept: walking up on the Armorer

The player arrives at the expedition site and the Armorer is *already working there*.
The camera opens from a distance and tracks smoothly toward him — as if the player
is walking up on the scene. The camera arrives close as the Armorer begins speaking.
The find-beat (firework + item in hand) lands with the camera in close.

### How it works (drone spectate pattern)

1. Player TPs to expedition site. View immediately spectates an invisible drone entity
   positioned at the "far" starting position.
2. Drone has a scripted CROSS_TO path moving from the far mark toward the Armorer's
   mark — smooth, continuous movement. Duration should feel like a natural walk-up,
   not a rush (calibrate per site, roughly 3–5 seconds).
3. Drone arrives near the Armorer's position as lines begin. Player has "walked up"
   on the scene.
4. Lines 1–2 fire. Item appears in Armorer's hand. Firework fires.
5. Line 3 fires (count + departure).
6. PLAYER_SPECTATE_END. Player TP'd back to home base.

### Scouting requirement

Zarathale documents a **drone start position** at each expedition site — the "far"
coordinate that Camera uses as the beginning of the dolly path. This is separate
from the player arrival mark and the Armorer mark. Three positions per site:
drone start → Armorer position → (drone arrives near Armorer mark).

### Site F exception *(and finale lead-in)*

Site F (The Weapon) uses the same template but with adjusted pacing: the drone
moves slower, and there is a deliberate hold after the drone arrives before lines
begin. This is now both the show's most emotionally weighted stop AND the final
expedition. The camera arriving slowly and then holding is the right read. The
absence of the firework after line 2 — on the last expedition piece, the weapon —
will be felt against that held frame as the player has never felt it before.

No firework at Site F. Camera does not compensate — hold the frame. The TP home
from this scene leads directly to the reveal sequence.

---

## What Camera Does NOT Do

- No dramatic sweeps or sudden reorientation during scenes
- No mid-scene reorientation except the one allowed FACE redirect in the finale
- No camera work that calls attention to itself as camera work

The show earns its reveals through location, staging, and restraint. Camera's job
is to make sure the player is positioned to receive them — not to perform the reveal.

---

## Coordination Gate

Mark brings one question to intake: does Set's home base arrival facing already put the
armor stand in center frame? If yes: Camera holds on home base. If no: a single
arrival-facing adjustment is warranted, coordinated with Set before YAML is authored.
