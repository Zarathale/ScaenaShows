---
department: Camera Specialty (Effects)
owner: Effects Director
kb_version: 1.1
updated: 2026-03-25
---

# Camera — Technical Reference

> Technical reference for the Camera specialty within the Effects department. Documents what the
> ScaenaShows Java plugin can do for player orientation, cinematic camera control, riding
> perspective, and screen distortion effects — and how to access those capabilities through YAML.
>
> Camera is not a top-level department. It is a specialty discipline within Effects — same
> relationship as Gracie is to Sound. The Effects Director holds Camera authority; this file
> exists to develop Camera technique in depth.
>
> Effects department overview: `kb/production-team.md §5. Effects Director`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| FACE | Point | Rotate player/entity to face a mark or direction (yaw only — see Gaps) |
| PLAYER_TELEPORT (`yaw:`, `pitch:`) | Point | Set precise camera orientation when teleporting |
| CROSS_TO (`facing:`) | Bar | Set orientation on arrival at a destination |
| PLAYER_SPECTATE | Bar | Attach player camera to a named entity |
| PLAYER_SPECTATE_END | Point | Return camera to player's own body |
| PLAYER_MOUNT | Point | Put player in riding perspective on an entity |
| PLAYER_DISMOUNT | Point | End riding perspective |
| CAMERA | Bar | Apply screen-level perceptual distortion (sway, blackout, flash, float) |

CAMERA effects are fully owned by Effects. The Lighting & Atmosphere Designer is aware of them as tone tools, but does not author them — they are applied to the player's perceptual experience, not to the world state.

---

## Capabilities & YAML Syntax

---

### FACE

Turns an entity or player to face a target. Instant — no duration. Yaw only.

```yaml
type: FACE
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:Name | group_1 | group_2 | player
look_at: mark:center            # mark:Name | player | compass:south | entity:spawned:Name
```

**Valid `look_at` values:**

| Value | Targets |
|-------|---------|
| `mark:Name` | Named mark in the show's mark table |
| `player` | The spatial anchor player |
| `compass:north` | Cardinal direction (north/south/east/west) |
| `entity:spawned:Name` | A named show-spawned entity |

**Behavioral notes:**
- Instant — fires and completes at the tick it appears.
- Yaw only. Pitch (up/down) is not set. See Gaps.
- FACE can target entity groups — all members orient simultaneously toward the same target.
- When used on a player mid-show (e.g., to orient them toward a performer entering from stage_left), FACE snaps the camera. Use this for punctuation, not for gentle reorientation. For smooth reorientation, use the PLAYER_SPECTATE + moving entity pattern instead.

---

### PLAYER_TELEPORT — precise orientation

The most reliable tool for setting both yaw and pitch at once. Best used when a scene transition and a camera reset coincide.

```yaml
type: PLAYER_TELEPORT
audience: participants
destination: set:main_stage   # OR
offset: {x: 0, y: 0, z: 0}  # relative offset (use offset: {x:0,y:0,z:0} for orientation-only)
yaw: 180.0                    # facing direction (0=south, 90=west, 180=north, 270=east)
pitch: -30.0                  # vertical angle (-90=straight up, 0=horizontal, 90=straight down)
```

**Yaw reference:**

| Yaw value | Facing direction |
|-----------|-----------------|
| 0.0 | South (+Z) |
| 90.0 | West (-X) |
| 180.0 | North (-Z) |
| 270.0 | East (+X) |

**Pitch reference:**

| Pitch value | Looking |
|-------------|---------|
| -90.0 | Straight up |
| -45.0 | Up at 45° |
| 0.0 | Horizontal |
| 45.0 | Down at 45° |
| 90.0 | Straight down |

**Behavioral notes:**
- To use PLAYER_TELEPORT purely for orientation (without moving the player), use `offset: {x:0, y:0, z:0}` with the desired yaw/pitch values. This rotates the camera without changing position.
- `pitch` requires PLAYER_TELEPORT — FACE does not set pitch. This is the primary workaround for the FACE pitch gap.

---

### CROSS_TO `facing:` — orientation on arrival

Sets the player's or entity's yaw when they arrive at a destination. Yaw only.

```yaml
type: CROSS_TO
target: player
destination: mark:stage_left
duration_ticks: 40
facing: mark:center   # face this direction on arrival
```

**Behavioral notes:**
- Applies the facing at arrival, not during movement. The player walks to the mark and then snaps to face `center`.
- Yaw only — no pitch control. See Gaps.

---

### PLAYER_SPECTATE — cinematic camera / drone pattern

Attaches the player's camera to a named entity. The player sees from that entity's perspective for the duration.

```yaml
type: PLAYER_SPECTATE
entity: entity:spawned:CinematicCamera
audience: private | group_1 | group_2 | participants
duration_ticks: 200   # optional; auto-ends spectate after N ticks
```

**The drone pattern — step by step:**

1. Spawn an invisible entity at the desired camera position:
```yaml
- at: 0
  type: SPAWN_ENTITY
  entity_type: ARMOR_STAND
  name: "CinematicCamera"
  offset: {x: 10, y: 5, z: 0}
  despawn_on_end: true

- at: 0
  type: ENTITY_INVISIBLE
  target: entity:spawned:CinematicCamera
  duration_ticks: 9999
```

2. Optionally script the camera's movement for a traveling shot:
```yaml
- at: 10
  type: CROSS_TO
  target: entity:spawned:CinematicCamera
  destination: {x: -10, y: 5, z: 0}
  duration_ticks: 100
```

3. Attach the player's camera:
```yaml
- at: 10
  type: PLAYER_SPECTATE
  entity: entity:spawned:CinematicCamera
  audience: participants
  duration_ticks: 100
```

4. End spectate and return camera to body:
```yaml
- at: 110
  type: PLAYER_SPECTATE_END
  audience: participants
```

**Behavioral notes:**
- PLAYER_SPECTATE requires the player to enter SPECTATOR game mode. While spectating: the player passes through blocks, their HUD clears, and they cannot interact with the world.
- Stop-safety restores the player's pre-show game mode when the show ends — SPECTATOR mode is not left active after show end.
- A PLAYER_SPECTATE with `duration_ticks` will auto-end spectate after that many ticks. Use PLAYER_SPECTATE_END to end it explicitly.
- The camera entity's Y position and facing direction set the camera angle. Position it and face it deliberately.
- **Smooth pan technique:** Move the camera entity with CROSS_TO while it's being spectated. The player's view smoothly tracks the entity's movement path.

---

### PLAYER_SPECTATE_END

Returns the player's camera from a spectated entity back to their own body.

```yaml
type: PLAYER_SPECTATE_END
audience: private | group_1 | group_2 | participants
```

**Behavioral notes:**
- Use this rather than waiting for the `duration_ticks` on PLAYER_SPECTATE to expire — it gives authoring control over exactly when the camera returns.
- After PLAYER_SPECTATE_END, the player's camera is back in their own body at their current position. The player's orientation may have shifted during spectate.

---

### PLAYER_MOUNT

Puts the player in first-person riding perspective on a named entity. More visceral than spectate — the player is inside the experience rather than observing it.

```yaml
type: PLAYER_MOUNT
entity: entity:spawned:GuideEntity
audience: private | group_1 | group_2 | participants
```

### PLAYER_DISMOUNT

Ends riding perspective.

```yaml
type: PLAYER_DISMOUNT
audience: private | group_1 | group_2 | participants
```

**Behavioral notes (MOUNT / DISMOUNT):**
- While mounted: the player's camera tracks with the entity's movement. The player sees from the riding position.
- Unlike spectate, the player does not enter SPECTATOR mode — they retain their normal game mode.
- The entity must be rideable by the player. Standard rideable entities: HORSE, BOAT, MINECART, STRIDER. Non-rideable entity types may not produce the riding perspective reliably.
- For a purely cinematic effect without actual riding mechanics, PLAYER_SPECTATE on an invisible moving entity is more flexible.

---

### CAMERA — Screen distortion effects

Applies potion-effect-based perceptual distortion. Not camera direction — perceptual alteration of the player's view.

```yaml
type: CAMERA
effect: sway       # sway | blackout | flash | float
intensity: 1       # amplifier (0–3; not all effects use it meaningfully)
duration_ticks: 40
audience: participants | group_1 | group_2 | invoker
```

**Effect reference:**

| Effect | Potion applied | Visual result | Duration notes |
|--------|----------------|---------------|----------------|
| `sway` | NAUSEA | Screen wobble/spiral | 40–80t for subtle; 100t+ for pronounced |
| `blackout` | DARKNESS | Smooth fade to near-black and back | Scene transition standard |
| `flash` | BLINDNESS | Immediate white flash | Keep short: 10–20t |
| `float` | LEVITATION (amp 0) + SLOW_FALLING | Upward drift sensation | 20–60t for a brief lift feel |

**`blackout` as scene transition — standard technique:**
```yaml
- at: 200
  type: CAMERA
  effect: blackout
  duration_ticks: 40
  audience: participants

# While black: teleport, change environment, swap sets, etc.
- at: 210
  type: PLAYER_TELEPORT
  audience: participants
  destination: set:act_two

# Player returns from black to find themselves somewhere new
```

**Behavioral notes:**
- Effects can be stacked: a DARKNESS followed immediately by a MESSAGE creates a dramatic text-in-darkness reveal.
- `float` combines LEVITATION amp 0 with SLOW_FALLING. It gives a brief sensation of rising without significantly altering altitude. Duration 20–40t is subtle; 80t+ becomes a full float arc.
- All CAMERA effects are Effects department authority. Lighting coordinates on tone intent when a screen effect accompanies a world-state moment (e.g., blackout during a storm climax), but Effects authors the CAMERA event.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Camera specialty within Effects needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `kb/departments/stage-manager.kb.md` → Active Gap Registry.

---

### Gap: FACE — yaw only, no pitch

**Status:** Open. Filed in `ops-inbox.md`.

FACE cannot orient players or entities up or down. Looking up at an aerial performer, overhead fireworks, or an elevated camera position requires a workaround.

**Workaround:** Use PLAYER_TELEPORT with explicit `pitch:` to set both axes. Combine with `offset: {x:0,y:0,z:0}` to rotate without moving. Alternatively: PLAYER_SPECTATE on an entity positioned above, so the camera itself is already elevated.

---

### Gap: No smooth yaw rotation (no ROTATE event)

**Status:** Open. Filed in `ops-inbox.md`.

FACE is instant. No first-class primitive for gradual yaw rotation without position change.

**Workaround:** PLAYER_SPECTATE on a slowly moving entity. Spawn an invisible entity, give it a CROSS_TO arc, attach the player's camera. The camera pans as the entity travels. This is the established smooth-pan technique.

**Proposed fix:** A `ROTATE` bar event that interpolates yaw over `duration_ticks` without moving the entity's XYZ position.
