---
department: Choreographer / Movement Director
owner: Choreographer / Movement Director
kb_version: 1.0
updated: 2026-03-25
---

# Choreographer / Movement Director — Technical Knowledgebase

> Technical reference for the Choreography department. Documents what the ScaenaShows Java plugin
> can do for movement — entities, players, velocity, flight — and how to access those capabilities
> through YAML.
>
> Creative direction for this role lives in `docs/production-team.md §3. Choreographer / Movement Director`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| CROSS_TO | Bar | Move entity or player to a mark or offset |
| ENTER | Bar | Spawn entity at wing mark and move to destination (semantic shorthand) |
| EXIT | Bar | Move entity to wing mark, optionally despawn (semantic shorthand) |
| HOLD | Point | Freeze entity/player at current position |
| FACE | Point | Turn entity/player to face a mark, entity, or compass direction (yaw only — see Gaps) |
| RETURN_HOME | Bar | Return participants to their pre-show captured location |
| ENTITY_SPEED | Point | Scale an entity's movement speed |
| ENTITY_VELOCITY | Point | Apply a vector impulse to an entity |
| PLAYER_VELOCITY | Point | Apply a vector impulse to players |
| PLAYER_FLIGHT | Point | Engage or release server-side flight (hover / release states) |

---

## Capabilities & YAML Syntax

---

### CROSS_TO

Moves an entity or player from their current position to a destination. With `duration_ticks > 0`, movement is gradual. With `duration_ticks: 0` or omitted, movement is instant.

```yaml
type: CROSS_TO
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:groupname | group_1 | group_2 | player
destination: mark:stage_left    # mark:Name | home | home+{x:5,z:0} | {x:-6,z:0} (inline offset)
duration_ticks: 40              # 0 or omitted = instant teleport
facing: mark:center             # optional: orient on arrival (yaw only)
```

**Behavioral notes — entities:**
- Uses Bukkit's pathfinder API. Arrival is not guaranteed on complex terrain. Design mob crosses for open space, or use short distances.
- `entity_group:` targeting moves all members simultaneously. Each member pathfinds independently to the same destination.
- Puppet entities (ENTITY_AI disabled) do not pathfind. A CROSS_TO on a puppet entity with duration_ticks > 0 may not move. Use instant CROSS_TO (no duration) for puppets, or re-enable AI first.

**Behavioral notes — players:**
- Uses `smoothMovePlayer` — a tick-exact server-side interpolation. Player movement is precise and reliable.
- The player cannot cancel a smooth move by walking during it. Their input is effectively overridden.
- For players, `home+{x:5,z:0}` syntax creates a relative offset from the player's pre-show capture location.

**Approximate timing guide (normal speed):**

| Distance | Duration |
|----------|----------|
| 5 blocks (stage cross) | ~25t |
| 10 blocks (half stage) | ~50t |
| 20 blocks (full stage) | ~100t |
| 5 blocks (slow, dramatic) at 0.4 speed | ~60t |

---

### HOLD

Freezes an entity or player at their current position by zeroing velocity. Instant — does not prevent future movement.

```yaml
type: HOLD
target: entity:spawned:Herald   # or entity_group:chorus | group_1 | player
```

**Behavioral notes:**
- HOLD zeroes velocity at the moment it fires. For entities, it also disables AI briefly to prevent the pathfinder from resuming immediately. For persistent freezing, use ENTITY_AI enabled: false instead.
- For players in flight, HOLD does not prevent horizontal drift. Use PLAYER_FLIGHT hover instead to genuinely lock altitude.

---

### FACE

Turns an entity or player to face a mark, a compass direction, or another entity. Yaw only — horizontal rotation.

```yaml
type: FACE
target: entity:spawned:Herald   # or group_1 | player | entity_group:chorus
look_at: mark:center            # mark:Name | player | compass:south | entity:spawned:OtherName
```

**Valid `look_at` values:**

| Value | What it targets |
|-------|----------------|
| `mark:Name` | Named mark in the show's mark table |
| `player` | The spatial anchor player |
| `compass:south` | Compass direction (north/south/east/west) |
| `entity:spawned:Name` | A named show-spawned entity |

**Behavioral notes:**
- FACE is instantaneous — there is no gradual rotation. See Gaps below for smooth rotation.
- Yaw only. The entity faces the horizontal direction of the target but is not pitched up or down toward it. See Gaps below.
- A FACE event fires and immediately returns; subsequent movement events can override the facing.

---

### RETURN_HOME

Moves participants back to their pre-show capture location (the position they were standing when `/show play` was invoked).

```yaml
type: RETURN_HOME
target: group_1 | player | entity:spawned:Herald
duration_ticks: 20   # gradual smooth move back to home; 0 = instant teleport
```

**Behavioral notes:**
- `home` is always a runtime value — it's captured at invocation, not set in YAML.
- Used at show end to return players to where they came from. Pair with PLAYER_FLIGHT release if players were in hover state.
- An entity can also be returned to home (wherever the spatial anchor was at spawn).

---

### ENTITY_SPEED

Scales the movement speed of a named entity.

```yaml
type: ENTITY_SPEED
target: entity:spawned:Herald
speed: 0.4   # 0.0 = stopped | 0.05 = very slow creep | 1.0 = normal | 2.0 = fast
```

**Behavioral notes:**
- Applied to the entity's attribute — affects all movement (pathfinder, player-initiated, etc.).
- `speed: 0.0` effectively freezes the entity without disabling AI. The entity will still try to pathfind but won't move. Use ENTITY_AI disabled for a cleaner freeze.
- Does not affect player speed — use EFFECT `slowness` or `speed` for player speed modification.

---

### ENTITY_VELOCITY

Applies a one-time vector impulse to an entity. Use for launches, knocks, dramatic exits.

```yaml
type: ENTITY_VELOCITY
target: entity:spawned:Herald   # or entity_group:chorus
vector: {x: 0.0, y: 0.8, z: 0.0}   # velocity vector in blocks/tick; y > 0 = upward
```

**Behavioral notes:**
- The impulse is applied once at the tick it fires. Physics then applies naturally (gravity, air resistance).
- `y: 0.8` is a moderate upward launch. `y: 1.5` is a dramatic throw. Values > 2.0 can send an entity very high.
- Combine with `ENTITY_EFFECT slow_falling` immediately after to create a controlled, floating arc rather than a ballistic trajectory.
- Does not affect players — use PLAYER_VELOCITY for players.

---

### PLAYER_VELOCITY

Applies a one-time vector impulse to players.

```yaml
type: PLAYER_VELOCITY
audience: private | group_1 | group_2 | participants
vector: {x: 0.0, y: 1.2, z: 0.0}
```

**Behavioral notes:**
- Same physics model as ENTITY_VELOCITY. Combine with EFFECT `slow_falling` for a controlled float.
- `y: 1.2` is a noticeable upward push. At `y: 0.4`, it reads as a gentle lift.
- The player can counteract the impulse by jumping or moving. For a purely scripted arc, follow immediately with PLAYER_FLIGHT hover to lock altitude after the impulse.

---

### PLAYER_FLIGHT

Server-side flight control. Two states: `hover` (engage flight, lock altitude) and `release` (disengage, apply transition effect).

```yaml
# Engage hover — locks player at current altitude
type: PLAYER_FLIGHT
state: hover          # hover | release
audience: participants

# Release with transition — applies release_effect before disabling flight
type: PLAYER_FLIGHT
state: release
release_effect: slow_falling     # slow_falling (default) | levitate | none
release_duration_ticks: 300      # duration of the release effect
audience: participants
```

**Hover state — behavioral notes:**
- Records each participant's pre-show flight state (once, via `putIfAbsent`) before enabling flight.
- Calls `setAllowFlight(true)` + `setFlying(true)`. The player is locked vertically at their current Y.
- Players can still move horizontally while hovering. Use `EFFECT slowness` alongside hover to discourage wandering if the show requires vertical-only constraint.
- **Always use a lift event (EFFECT levitation) before hover** — hover freezes at *current* altitude, not a target altitude. Get the player to the right height first, then lock it with hover.

**Release state — behavioral notes:**
- Applies `release_effect` first (so the player doesn't free-fall when flight drops), then restores the participant's pre-show flight state.
- `slow_falling` (default): gentle descent. `levitate`: brief upward drift before transition. `none`: immediate drop — only safe if the player is already near the ground.
- **Always use `state: release` before show end** rather than relying on stop-safety — this gives authoring control over when the player starts descending and what the transition feels like.
- Stop-safety does handle PLAYER_FLIGHT cleanup if the show is interrupted, but the transition won't be choreographed.

**Calibrated flight patterns (from levitation calibration sessions):**

| Pattern | YAML | Effect |
|---------|------|--------|
| HOVER | lev=20t gap=8t cycle=28t | Clean hover — altitude holds |
| CLIMB | lev=24t per cycle | Gradual upward drift |
| RELEASE | lev=20t gap=24t cycle=44t | Slow controlled descent — "blood pressure release" feel |

These use `ENTITY_EFFECT levitation` (amp 0) applied to the player in a repeating pattern. The cycle timing was derived from in-game calibration (see session notes 2026-03-24).

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Choreography department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `docs/departments/stage-manager.kb.md` → Active Gap Registry.

---

### Gap: FACE — yaw only, no pitch control

**Status:** Open. Filed in `ops-inbox.md`.

`FACE` computes the horizontal angle (yaw) to the look_at target but does not compute or set pitch (vertical angle). An entity or player turned with FACE faces the horizontal direction of the target but is not oriented up or down toward it.

**Impact:** Cannot author "look up at the overhead fireworks" or "look down at the performer below" using FACE alone.

**Workaround:**
- For players: use `PLAYER_TELEPORT` with explicit `yaw:` and `pitch:` to set both axes at once. This also moves the player — design the teleport so the position change is intentional or imperceptible.
- For a camera-look-up effect without position change: use `PLAYER_SPECTATE` attached to an entity positioned above the player. The camera attaches to that entity's perspective rather than rotating the player's view directly.

---

### Gap: No smooth yaw rotation (no ROTATE event)

**Status:** Open. Filed in `ops-inbox.md`.

`FACE` is instant. There is no first-class event for gradual yaw rotation (camera pan) without also changing XYZ position.

**Impact:** Cinematic slow pans require workarounds; rapid PLAYER_TELEPORT sequences are imprecise.

**Workaround:** Use `PLAYER_SPECTATE` on a slowly moving entity. Spawn an invisible entity, script a `CROSS_TO` arc for that entity, and attach the player's camera to it. The player's view follows the entity's travel path — this is the current "smooth pan" technique.

---

### Limitation: No ORBIT event

Smooth continuous circular movement (orbit around a center point) is not implemented in v2. The pathfinder API doesn't support it reliably.

**Workaround:** Compose a series of `CROSS_TO` events targeting marks arranged around a circle (12 marks at 30° intervals = 12 short crosses that approximate a circle). Each CROSS_TO has `duration_ticks` equal to the orbit period divided by the number of steps.

---

### Limitation: Mob pathfinding on complex terrain

CROSS_TO for mobs uses Bukkit's pathfinder. On complex terrain (hills, obstacles, enclosed spaces), arrival is not guaranteed. Mobs may get stuck or take indirect routes.

**Workaround:** Design mob movement for open, flat surfaces. Keep cross distances short. Use PLAYER_TELEPORT (instant) rather than CROSS_TO duration for player marks where precision is required.
