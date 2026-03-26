---
department: Camera Director
owner: Mark
kb_version: 1.3
updated: 2026-03-26
---

# Camera Director — Technical Knowledgebase

> Technical reference for the Camera department. Documents what the ScaenaShows Java plugin
> can do for player orientation, cinematic camera control, riding perspective, and screen
> distortion effects — and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §11. Camera Director`.

---

## Mark

Mark runs the Camera department with one overriding question: where is the player looking right
now, and is that the right place for them to be? He keeps a mental map of every active beat and
every competing claim on the player's attention. He's assertive — when a firework is about to
detonate overhead and the player is facing downhill, Mark puts in a recommendation. He doesn't
wait for the Show Director to notice the attention gap.

His escalation posture is proactive: he surfaces focus-point conflicts early, during show
planning, not as post-hoc notes after something lands wrong in-game. If another department is
placing a key beat somewhere the camera can't cleanly reach, that conversation happens before the
YAML gets written.

---

## Role Summary

- Owns all player viewpoint and orientation events: FACE, PLAYER_TELEPORT yaw/pitch, CROSS_TO `facing:`
- Owns all cinematic perspective modes: PLAYER_SPECTATE, PLAYER_SPECTATE_END, PLAYER_MOUNT, PLAYER_DISMOUNT
- Owns all screen-level perceptual distortion: CAMERA (sway, blackout, flash, float)
- Owns the Focus Point Doctrine — asserting where the player's attention is at every key beat
- Coordinates with Effects on PLAYER_TELEPORT: Effects decides the destination; Camera decides the facing on arrival. Orientation-only teleports (offset {x:0,y:0,z:0}) are Camera-authored.
- Coordinates with Effects on scene transitions: blackout timing and camera orientation reset are Camera's contribution to a transition that Effects is scripting positionally
- Coordinates with Fireworks and Choreography on focus-point assertions: if a burst or entrance requires a camera orientation call, Camera authors it; the originating department flags the need in their brief

---

## The Focus Point Doctrine

**Every beat has an intended focus point. Mark's job is to make sure the player is looking at it.**

This is the core principle of Camera work. A firework burst means nothing if the player is staring
at the ground. A performer entrance from stage_left is invisible if the player is facing right.
The player's camera is not a passive observer — it is an authored instrument, and Mark is
responsible for authoring it.

### Mark's standing questions for every beat

For each significant event in the show timeline, Mark asks:

1. **Where is the intended focus point?** (The firework burst? The entering performer? The text message? The world transformation?)
2. **Where is the player likely looking at this moment?** (Based on prior orientation events, the last FACE or teleport, what has been drawing their eye.)
3. **Is there a gap?** (If the player is likely looking somewhere other than the focus point, can Mark do something about it?)
4. **What is the cost of asserting attention?** (A sudden FACE snap is disorienting. Is the beat worth it? Or is gentle orientation during a transition a better moment?)

### When to assert attention

Mark assertively recommends camera orientation calls when:

- A high-impact firework pattern fires at an altitude or position the player may not be looking
- A performer enters or exits — especially from a side or behind the player's likely eyeline
- A world transformation (weather, time, lightning) occurs and the player needs to see it to register it
- A section transition happens and the intended view for the next section differs from where the player left off
- The player has been elevated (levitated or flight) and may be looking down rather than at the designed horizon

Mark does NOT assert attention for:
- Ambient effects that work peripherally (sound, ambient particles)
- Text-forward beats where the player can read regardless of orientation
- Beats where player freedom is part of the design (the "look wherever you want" section)

### Assertive attention patterns in YAML

**Pattern 1 — The snap (instant, upward):**
Use when a key firework fires above the player's expected eyeline and they need to see it.
PLAYER_TELEPORT orientation-only is better than FACE here because it also sets pitch.
```yaml
- at: 470     # 10 ticks before the firework at 480
  type: PLAYER_TELEPORT
  audience: participants
  offset: {x: 0, y: 0, z: 0}   # orientation-only — no position change
  yaw: 180.0                     # face the show's front direction
  pitch: -50.0                   # look up at a steep angle
```

**Pattern 2 — The face-then-land (snap to face a performer's entrance):**
```yaml
- at: 590     # just before performer enters from stage_left at 600
  type: FACE
  target: player
  look_at: mark:stage_left
```
FACE is appropriate here because the target is horizontal. No pitch required.

**Pattern 3 — The pre-lift orientation (before levitation climbs above horizon):**
Before a levitation sequence begins, orient the player to look slightly up and toward
the show's intended horizon. As they rise, the scene will fill their view correctly.
```yaml
- at: 195     # a few ticks before levitation begins at 200
  type: PLAYER_TELEPORT
  audience: participants
  offset: {x: 0, y: 0, z: 0}
  yaw: 0.0      # faces whatever is north (align to show front)
  pitch: -20.0  # slight upward tilt — ready to see overhead events
```

**Pattern 4 — The gradual redirect via spectate drone:**
When a gentle attention redirect is needed without a jarring snap, move an invisible drone
entity from the player's current eyeline toward the intended focus, then briefly spectate it.
The player's view smoothly travels to the focus point. See the Drone Pattern section below.

---

## What Mark Actually Controls — The Honest Accounting

Before listing YAML events, it helps to name the camera moves and say clearly what exists and
what doesn't.

### Pan (horizontal rotation)
Turning left or right in place.

| Technique | Quality | How |
|-----------|---------|-----|
| **Instant snap** | ✅ Available | `FACE` — yaw only, no pitch |
| **Instant snap with pitch** | ✅ Available | `PLAYER_TELEPORT` with `offset: {x:0,y:0,z:0}` + yaw + pitch |
| **Smooth pan** | ✅ Available (via drone) | `PLAYER_SPECTATE` on an entity moving horizontally via `CROSS_TO` |
| **First-class smooth pan** | ⚠️ Gapped | No `ROTATE` event. Workaround is the drone pattern. Filed: ops-inbox.md |

### Tilt (vertical rotation — pitch)
Looking up or down.

| Technique | Quality | How |
|-----------|---------|-----|
| **Instant tilt** | ✅ Available | `PLAYER_TELEPORT` with `pitch:` field |
| **Smooth tilt** | ✅ Available (via drone) | `PLAYER_SPECTATE` on an entity moving vertically via `CROSS_TO` |
| **FACE for tilt** | ❌ Not available | `FACE` is yaw only — filed in ops-inbox.md |

> **Mark's primary tool for "look up at something":** `PLAYER_TELEPORT` with
> `offset: {x:0,y:0,z:0}` + explicit `pitch:` value. This is the fastest path.
> The drone pattern can smooth it if the show needs a gentle upward tilt.

### Zoom
Changing field of view, pulling in or pushing out.

| Technique | Quality | How |
|-----------|---------|-----|
| **Zoom** | ❌ Not available | Paper 1.21.x has no API for FOV control. No NMS path exists cleanly. Not filed — platform limit, not a plugin gap. |

Zoom is not in Mark's toolkit. What can substitute for zoom: dollying the player's position closer or further from the subject (PLAYER_TELEPORT / CROSS_TO position change), or adjusting the spectate drone's proximity to the subject.

### Dolly / Follow (camera tracks a moving subject)
The camera moves with or behind a moving performer or effect.

| Technique | Quality | How |
|-----------|---------|-----|
| **Auto-follow** | ❌ Not available | No automatic tracking. The engine has no "look at entity" live update. |
| **Scripted follow via drone** | ✅ Available | Spawn invisible entity, script its path via `CROSS_TO`, attach player via `PLAYER_SPECTATE`. The player's view follows the drone's path. |
| **Mount follow** | ✅ Available (limited) | `PLAYER_MOUNT` — player rides the entity, camera follows with it. Requires rideable entity type. |
| **Bump approximation** | ⚠️ Imprecise | Repeated `FACE` or `PLAYER_TELEPORT` calls at short intervals. Works but choppy. Use only as a last resort. |

**The honest answer on "follow action":** There is no real-time auto-follow. Mark scripts the
camera path in advance. The drone pattern is the closest approximation — it works well when the
performer's path is known and scripted. For unscripted or emergent action, the camera is fixed.

---

## Instrument Inventory

---

### Instrument 1 — The Snap (FACE)

**Java grounding:** `FACE` event → `StageEventExecutor.handleFace()`

**What it does:** Instantly rotates an entity or player to face a target. The rotation fires at
the tick it appears and is complete — one frame of perceived change. It's a camera cut, not a pan.

```yaml
type: FACE
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:Name | group_1 | group_2 | player
look_at: mark:center            # mark:Name | player | compass:south | entity:spawned:Name
```

**Valid `look_at` values:**

| Value | Resolves to |
|-------|-------------|
| `mark:Name` | Named mark in the show's mark table |
| `player` | The spatial anchor player's position |
| `compass:north` | Cardinal direction (north/south/east/west) |
| `entity:spawned:Name` | A named show-spawned entity's position |

**Confirmed behavior from source:** `handleFace()` computes yaw from XZ delta only — `Math.atan2(-dx, dz)`. Pitch is NOT set. The player or entity snaps to the correct horizontal direction but does not tilt vertically toward the target.

**Strengths:** Fast, cheap, and reliable for horizontal orientation. Good for punctuation — the moment a performer steps on stage, snap the player's attention to them. Works on entity groups simultaneously.

**Key limitation:** Yaw only. If the focus point is above the player's horizontal line (fireworks,
overhead performer, aerial burst), FACE will orient them correctly horizontally but they'll still
be looking at the horizon. Use `PLAYER_TELEPORT` with `pitch:` for upward focus points.

**Storytelling contexts:** "Look at the performer entering stage left." "Face the mark just before the light changes." Any beat where the focus point is roughly at eye level.

---

### Instrument 2 — Precise Orientation (PLAYER_TELEPORT)

**Java grounding:** `PLAYER_TELEPORT` event → `PlayerEventExecutor.handleTeleport()`

**What it does:** Moves a player to a destination. With `offset: {x:0,y:0,z:0}`, it rotates
the camera without changing position — a full yaw + pitch reset.

**Orientation-only pattern:**
```yaml
type: PLAYER_TELEPORT
audience: participants
offset: {x: 0, y: 0, z: 0}   # zero offset = stay in place
yaw: 180.0                     # facing direction (see table below)
pitch: -45.0                   # vertical angle: negative = looking up
```

**Yaw reference (Minecraft convention — different from compass bearing):**

| Yaw | Facing |
|-----|--------|
| 0.0 | South (+Z) |
| 90.0 | West (-X) |
| 180.0 | North (-Z) |
| 270.0 | East (+X) |

**Pitch reference:**

| Pitch | Looking |
|-------|---------|
| -90.0 | Straight up |
| -45.0 | Up at 45° |
| 0.0 | Horizontal |
| 45.0 | Down at 45° |
| 90.0 | Straight down |

**Strengths:** The only tool that sets both yaw AND pitch in a single event. Best used at section transitions (when a teleport is already happening) or as a pure orientation reset in the ticks before a key beat. Confirmed working in-game.

**Notes:**
- Unlike FACE, this is a player-only event — it cannot orient spawned entities.
- `smoothMovePlayer()` in CROSS_TO preserves the player's current yaw/pitch during movement.
  The `facing:` field on CROSS_TO only applies at **arrival**, not during the move. So
  CROSS_TO does not smoothly rotate the camera during travel — orientation snaps at the end.

---

### Instrument 3 — Orientation on Arrival (CROSS_TO `facing:`)

**Java grounding:** `CROSS_TO` event with `facing:` field → `StageEventExecutor.applyFacing()`

**What it does:** Sets player yaw when they arrive at a destination. The camera snaps to face
the target at the end of movement. Combines position and orientation in one event.

```yaml
type: CROSS_TO
target: player
destination: mark:stage_left
duration_ticks: 40
facing: mark:center   # face this direction on arrival
```

**Confirmed behavior:** `applyFacing()` computes yaw from XZ delta only — same as FACE. Yaw only, no pitch. The `facing:` field is a horizontal orientation tool.

**When to use:** Choreography moves the player; Camera adds facing intent to the arrival. Good
for scene setup transitions where both position and orientation need to change together.

---

### Instrument 4 — Spectate Drone / Cinematic Camera (PLAYER_SPECTATE)

**Java grounding:** `PLAYER_SPECTATE` → `PlayerEventExecutor.handleSpectate()`

**What it does:** Attaches the player's camera to a named entity. The player sees from that
entity's perspective for the duration. If the entity moves (via CROSS_TO), the player's view
smoothly follows.

**This is Mark's most powerful tool.** It enables: smooth pan, smooth tilt, traveling shots,
drone reveals, and the closest approximation to a follow shot that the engine supports.

**The drone pattern — full sequence:**

```yaml
# 1. Spawn the drone at the desired camera start position
- at: 0
  type: SPAWN_ENTITY
  entity_type: ARMOR_STAND
  name: "CinematicCamera"
  offset: {x: 10, y: 5, z: 0}
  despawn_on_end: true

# 2. Make it invisible
- at: 0
  type: ENTITY_INVISIBLE
  target: entity:spawned:CinematicCamera
  duration_ticks: 9999

# 3. Script the camera's movement path
- at: 10
  type: CROSS_TO
  target: entity:spawned:CinematicCamera
  destination: {x: -10, y: 5, z: 0}
  duration_ticks: 100

# 4. Attach the player's camera
- at: 10
  type: PLAYER_SPECTATE
  entity: entity:spawned:CinematicCamera
  audience: participants
  duration_ticks: 100

# 5. Return camera to body
- at: 110
  type: PLAYER_SPECTATE_END
  audience: participants
```

**Smooth pan:** Move the drone entity horizontally (CROSS_TO at same Y, changing X or Z).
The player's view sweeps across the scene as the drone travels.

**Smooth tilt:** Move the drone entity vertically (CROSS_TO at same XZ, changing Y). The
player's view rises or falls with it. This is the only smooth tilt available.

**Follow shot approximation:** Spawn a drone alongside or ahead of a moving performer. Give
the drone the same CROSS_TO path as the performer, offset by the desired camera distance.
Player spectates the drone — the drone follows the performer's trajectory, camera tracks with it.

**Confirmed behavior from source:**
- PLAYER_SPECTATE puts the player into `SPECTATOR` game mode. While spectating: HUD clears,
  player passes through blocks, cannot interact with the world.
- Stop-safety restores the prior game mode (`recordSpectateRestore` → restored on show end).
- The drone entity's facing direction at spectate time becomes the initial camera orientation.
  If the entity has never been FACEd or oriented, it may be looking at a default direction.
  Pre-orient the drone before spectating.

**Limitations:**
- Spectate changes game mode — this is a significant player experience shift. Use for
  intentional cinematic moments, not casual orientation nudges.
- `entity:spawned:` prefix is required. `entity:world:` targeting is not implemented (ops-inbox).

---

### Instrument 5 — Riding Perspective (PLAYER_MOUNT / PLAYER_DISMOUNT)

**Java grounding:** `PLAYER_MOUNT` → `PlayerEventExecutor.handleMount()` (addPassenger)

**What it does:** Puts the player in first-person riding perspective on a named entity. The
camera tracks with the entity's movement. More visceral than spectate — the player is inside
the experience.

```yaml
type: PLAYER_MOUNT
entity: entity:spawned:GuideEntity
audience: private | group_1 | group_2 | participants
```

```yaml
type: PLAYER_DISMOUNT
audience: private | group_1 | group_2 | participants
```

**Confirmed behavior from source:** Uses `mountEntity.addPassenger(p)`. Player does NOT enter
SPECTATOR mode — they retain their current game mode.

**Strengths:** Intimate and visceral. Good for the sensation of being carried — a river, a
vehicle, a guide. The player feels physically attached to the entity rather than observing it.

**Limitations:**
- The entity must reliably accept passengers. Standard rideable types: HORSE, BOAT, MINECART,
  STRIDER. Armor stands are used as camera drones but their riding behavior may be inconsistent.
- The player can look around freely while mounted — the mount controls position, not orientation.
  Use FACE before mounting to set the initial look direction.
- For pure cinematic effect without riding mechanics, PLAYER_SPECTATE on a moving drone is more
  controllable.

---

### Instrument 6 — Screen Distortion (CAMERA)

**Java grounding:** `CAMERA` event → `VisualEventExecutor` (potion effects applied to players)

**What it does:** Applies perceptual distortion to the player's screen. Not camera direction —
this is the screen-level experiential layer. It distorts how the player perceives what they're
looking at.

```yaml
type: CAMERA
effect: sway       # sway | blackout | flash | float
intensity: 1       # amplifier (0–3; not all effects use it meaningfully)
duration_ticks: 40
audience: participants | group_1 | group_2 | invoker
```

**Effect reference:**

| Effect | Mechanism | Visual result | Duration notes |
|--------|-----------|---------------|----------------|
| `sway` | NAUSEA | Screen wobble/spiral — disorientation | 40–80t subtle; 100t+ pronounced |
| `blackout` | DARKNESS | Smooth fade to near-black and back | Scene transition standard |
| `flash` | BLINDNESS | Immediate white flash | Keep short: 10–20t |
| `float` | LEVITATION (amp 0) + SLOW_FALLING | Upward drift sensation | 20–60t for brief lift |

**`blackout` as scene transition — the standard technique:**
```yaml
- at: 200
  type: CAMERA
  effect: blackout
  duration_ticks: 40
  audience: participants

# While black: teleport, change environment, change orientation
- at: 210
  type: PLAYER_TELEPORT
  audience: participants
  destination: set:act_two
  yaw: 0.0
  pitch: 0.0

# Player returns from black in a new location, already oriented
```

**Coordination note:** CAMERA screen distortion effects are Camera department authority. Lighting
coordinates on tone intent when a screen effect accompanies a world-state moment (e.g., blackout
during a storm climax), but Camera authors the CAMERA event. Mark uses `blackout` as a seam
between scenes — the view goes dark, the world resets behind the black, the player wakes up
somewhere intentional.

---

## Working with the Bump Approximation (and when not to use it)

Repeated `FACE` or `PLAYER_TELEPORT` calls at short intervals can approximate continuous
tracking, but this technique has significant downsides:

```yaml
# Bump-every-20-ticks tracking — illustrative only, use sparingly
- at: 100
  type: FACE
  target: player
  look_at: entity:spawned:MovingPerformer
- at: 120
  type: FACE
  target: player
  look_at: entity:spawned:MovingPerformer
- at: 140
  type: FACE
  target: player
  look_at: entity:spawned:MovingPerformer
```

**The problems:**
- Each FACE is a hard snap — the player experiences the view jumping every 20 ticks rather
  than smoothly tracking.
- The player cannot look freely between bumps without being immediately corrected — feels
  like forced attention.
- If the performer moves faster than the bump interval, the camera lags.

**When it's acceptable:** Very slow-moving subjects (1–2 blocks per second or less), where
the interval can be long enough (40–60t) that the snap is imperceptible. Also works as
rhythmic beat emphasis — a FACE on the downbeat of a repeating musical structure.

**When to use the drone instead:** Any time smooth, natural-feeling follow is required.
The drone pattern (PLAYER_SPECTATE + CROSS_TO) produces genuinely smooth tracking and
the player isn't fighting the camera between bumps.

---

## Cross-Department Coordination

**Camera + Effects:** The most active coordination pair. Effects decides where the player's body
goes (position, altitude, flight); Camera decides which direction their head is pointing when
they get there. Key touchpoints: every PLAYER_TELEPORT that also sets yaw/pitch (Camera owns
the facing fields), every CAMERA blackout used as a transition seam (Camera authors the timing),
every levitation sequence (Camera asserts the player's pitch before liftoff so they're looking
at the right horizon as they rise).

**Camera + Fireworks:** Before any significant fireworks beat, Mira flags to Mark where the
burst will be relative to the player's position and altitude. Mark determines whether the
player's current orientation will put that burst in their field of view. If not, Mark authors
an orientation call. Neither overrides the other — the firework location is set before the
camera call, and the camera call is designed around the firework location.

**Camera + Choreography:** If a performer enters from a direction the player is unlikely to
be facing, Camera puts in a FACE or orientation-only PLAYER_TELEPORT just before the entrance.
Choreography owns the entrance timing and path; Camera owns the orientation that reveals it.

**Camera + Voice:** On-screen text (chat, title, action_bar) reads regardless of orientation.
Mark does not need to assert player attention for text-forward beats. But if a TITLE fires
and the backdrop matters (the player should be looking at a specific world element when they
read it), Camera coordinates with Voice to ensure the orientation is set before the title.

**Camera + Lighting:** A blackout transition (Camera's tool) and a TIME_OF_DAY change
(Lighting's tool) are often authored at the same moment — the lights go out, the world resets,
the lights come back up. Camera and Lighting agree on the sequence and timing so the player
returns from black into the world Lighting has prepared.

---

## Gaps and Limitations

> Stage Management owns the full gap registry. This section documents what Mark needs to
> know for show authoring. File new gaps via Stage Management — `ops-inbox.md`.

---

### Gap: FACE — yaw only, no pitch

**Status:** Open. Filed in `ops-inbox.md`.

FACE cannot orient players or entities upward or downward. Looking up at an aerial performer,
overhead fireworks, or an elevated mark requires a workaround.

**Workaround:** `PLAYER_TELEPORT` with explicit `pitch:` and `offset: {x:0,y:0,z:0}` for
orientation-only. Or PLAYER_SPECTATE on an entity pre-positioned at the intended view angle.

---

### Gap: No smooth yaw rotation (no ROTATE event)

**Status:** Open. Filed in `ops-inbox.md`.

FACE is instant. No first-class primitive for gradual yaw rotation without position change.

**Workaround:** PLAYER_SPECTATE on a drone moving horizontally via CROSS_TO. This is the
established smooth-pan technique and works well. The cost is the spectate mode switch.

**Proposed fix:** A `ROTATE` bar event that interpolates yaw over `duration_ticks` without
changing XYZ position — parallel to `smoothMovePlayer`.

---

### Platform limit: No zoom

Paper 1.21.x has no accessible API for FOV (field of view) control. This is a Minecraft platform
constraint, not a plugin gap. No workaround exists. Zoom is not in Mark's toolkit.

---

### Platform limit: No real-time follow / look-at-entity tracking

The plugin has no mechanism to update the player's camera orientation dynamically based on an
entity's live position. All focus direction is either static (set at a tick) or scripted in
advance (drone path). Unscripted or emergent action cannot be reliably tracked by the camera.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| FACE (yaw snap) | ✅ Verified | Confirmed in source; instant, yaw only |
| FACE pitch | ⚠️ Gapped | Yaw-only confirmed in handleFace(). Pitch workaround: PLAYER_TELEPORT. Filed: ops-inbox.md |
| PLAYER_TELEPORT orientation-only (pitch + yaw) | ✅ Verified | Confirmed; use offset: {x:0,y:0,z:0} to avoid position change |
| CROSS_TO facing: (yaw on arrival) | ✅ Verified | Confirmed in applyFacing(); yaw only, snaps at end of movement |
| PLAYER_SPECTATE (drone pattern) | ✅ Verified | Confirmed in source; puts player in SPECTATOR mode; stop-safety restores game mode |
| PLAYER_SPECTATE smooth pan (via drone CROSS_TO) | ✅ Verified | Smooth movement confirmed via smoothMovePlayer() |
| PLAYER_SPECTATE_END | ✅ Verified | Confirmed; restores game mode from spectate restore map |
| PLAYER_MOUNT | ✅ Verified | Uses addPassenger(); confirmed. Rideable entity types required. |
| PLAYER_DISMOUNT | ✅ Verified | Confirmed |
| CAMERA: blackout / sway / flash / float | ✅ Verified | Confirmed via potion effects |
| ROTATE event (smooth yaw) | ⚠️ Gapped | Not implemented. Workaround: spectate drone. Filed: ops-inbox.md |
| Zoom (FOV control) | ❌ Platform limit | No Paper API. Not fileable — platform constraint. |
| Real-time auto-follow / look-at-entity | ❌ Platform limit | No live entity tracking. Scripted drone is the best approximation. |
