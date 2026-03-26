---
department: Effects Director
owner: Effects Director
kb_version: 1.1
updated: 2026-03-26
---

# Effects Director — Technical Knowledgebase

> Technical reference for the Effects department. Documents what the ScaenaShows Java plugin
> can do to the target player: forced movement, perceptual alteration, and particles — and how
> to access those capabilities through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §5. Effects Director`.
>
> Camera is a peer department. Orientation, cinematic perspective, and screen distortion
> reference: `kb/departments/camera.kb.md`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| PLAYER_TELEPORT | Point | Move target player to an absolute destination or relative offset. Destination is Effects authority; yaw/pitch fields are Camera authority — coordinate on any teleport that also resets orientation. |
| PLAYER_FLIGHT | Point | Engage hover (lock altitude) or release (transition to descent) for target player |
| PLAYER_VELOCITY | Point | Apply one-time vector impulse to target player |
| CROSS_TO (target player) | Bar | Smooth scripted movement of the target player to a mark (Effects authority when the *subject* is the target player). The `facing:` field on arrival is Camera authority. |
| EFFECT | Bar | Apply potion effect to players: levitation, slow_falling, night_vision, blindness, darkness, nausea, speed, slowness |
| PARTICLE | Point or Bar | Spawn particles at an offset from the anchor; repeating or single burst |

**Boundary with Camera:** If the event concerns where the player's *body* is (position, altitude, physics) → Effects. If the event concerns where the player's *eyes* are pointing, or their cinematic perspective → Camera. PLAYER_TELEPORT is the key joint event: Effects owns the destination; Camera owns the facing. See `kb/departments/camera.kb.md`.

**Boundary with Choreography:** If the subject being moved is the show's target player → Effects. If the subject is a performer, NPC, or cast entity → Choreography.

**Boundary with Lighting:** If the effect is applied onto a specific player → Effects. If the event changes the world state visible to all players (TIME_OF_DAY, WEATHER, LIGHTNING) → Lighting.

---

## Capabilities & YAML Syntax

---

### PLAYER_TELEPORT — forced movement

The primary tool for set transitions and positional resets. Moves the player to a destination. The `yaw` and `pitch` fields are Camera department authority — coordinate with Mark on any teleport that also resets orientation.

```yaml
type: PLAYER_TELEPORT
audience: participants
destination: set:main_stage   # named set
# OR
offset: {x: 0, y: 0, z: 0}   # relative offset
yaw: 180.0                     # Camera authority: 0=south, 90=west, 180=north, 270=east
pitch: -30.0                   # Camera authority: -90=straight up, 0=horizontal, 90=straight down
```

**Scene transition pattern:** Camera fires `CAMERA blackout` to cover the seam; Effects authors the `PLAYER_TELEPORT` during the dark; Camera sets the `yaw`/`pitch` on arrival. Both departments coordinate on the sequence.

---

### PLAYER_FLIGHT — hover and release

Server-side flight control. Two states that must be used together: hover to lock altitude, release to end flight gracefully.

```yaml
# Engage hover — locks player at current altitude
type: PLAYER_FLIGHT
state: hover
audience: participants

# Release with transition
type: PLAYER_FLIGHT
state: release
release_effect: slow_falling     # slow_falling (default) | levitate | none
release_duration_ticks: 300
audience: participants
```

**Always lift before hovering.** Hover locks the player at their *current* altitude — use EFFECT levitation or PLAYER_VELOCITY first to get them to the target height, then lock with hover.

**Always release before show end.** Use `state: release` as an authored event rather than relying on stop-safety, so the transition is choreographed. Stop-safety will handle PLAYER_FLIGHT cleanup on interrupted shows, but the descent won't be controlled.

**Calibrated levitation patterns** (from calibration sessions 2026-03-24):

| Pattern | Configuration | Effect |
|---------|--------------|--------|
| HOVER | EFFECT levitation amp 0 + slow_falling, lev=20t gap=8t cycle=28t | Clean altitude hold |
| CLIMB | EFFECT levitation amp 0, lev=24t per cycle | Gradual upward drift |
| RELEASE | EFFECT levitation amp 0, lev=20t gap=24t cycle=44t | Slow controlled descent — "blood pressure release" feel |

These patterns use inline `EFFECT levitation` events in a repeating cycle. See `kb/departments/choreography.kb.md` → PLAYER_FLIGHT section for the original calibration data (pre-restructure reference).

---

### PLAYER_VELOCITY — impulse

One-time vector impulse applied to the target player. Physics then applies naturally.

```yaml
type: PLAYER_VELOCITY
audience: participants
vector: {x: 0.0, y: 1.2, z: 0.0}   # y > 0 = upward
```

**Controlled arc:** combine PLAYER_VELOCITY with EFFECT `slow_falling` immediately after to create a floating arc rather than a ballistic trajectory.

**`y: 0.4`** — a gentle lift. **`y: 1.2`** — noticeable upward push. **`y: 2.0+`** — dramatic launch.

The player can partially counteract the impulse by moving. For a purely scripted arc, follow with PLAYER_FLIGHT hover to lock altitude after the impulse settles.

---

### EFFECT — Perceptual alteration on players

Applies potion effects to the target player. Alters how they move or perceive the world.

```yaml
type: EFFECT
effect_id: night_vision    # see table below
duration_ticks: 200
amplifier: 0
hide_particles: true       # suppress particle indicators over the player's head (always true for show use)
audience: participants | group_1 | group_2 | private
```

**Always set `hide_particles: true`** for show EFFECT events. The particle puffs that normally appear around a player when an effect is active break immersion.

**Effect reference:**

| effect_id | What the player experiences | Notes |
|-----------|----------------------------|-------|
| `levitation` | Upward drift | Primary lift instrument; amp 0 = gentle, amp 1+ = climb, amp 9 = surge |
| `slow_falling` | Gravity reduced, float sensation | Used post-levitation and on PLAYER_FLIGHT release |
| `night_vision` | Full brightness in darkness | ⚠️ Cross-discipline: coordinate with Lighting — reveals the environment Lighting crafted |
| `blindness` | Severe dark vignette, short sight distance | Disorientation, fog of experience, transition |
| `darkness` | Pulsing dark vignette (deepens rhythmically) | Dread, weight, the Warden's domain |
| `nausea` | Screen wobble/spiral | Disorientation, vertigo, unreality |
| `speed` | Movement faster | Chase, urgency, tension |
| `slowness` | Movement slower | Weight, grief, time stretching |

---

### PARTICLE — Environmental particles applied to player space

Spawns particles at an offset from the spatial anchor. With `duration_ticks` and `interval_ticks`, becomes a repeating atmospheric layer surrounding the player.

```yaml
# Single burst:
type: PARTICLE
particle_id: minecraft:heart
count: 12
offset: [0.5, 0.8, 0.5]   # x/y/z spread radius around the anchor
extra: 0.0                  # speed multiplier for directional particles
force: true                 # true: render for all players regardless of particle settings

# Repeating layer:
type: PARTICLE
particle_id: minecraft:ash
count: 8
offset: [3.0, 2.0, 3.0]
extra: 0.02
force: true
duration_ticks: 200
interval_ticks: 10          # fire every 10 ticks for 200 ticks = 20 bursts
```

**Always use `force: true`** for atmospheric particles — it bypasses client-side particle distance limits so particles are visible across the stage.

**Useful atmospheric particles:**

| particle_id | Effect |
|-------------|--------|
| `minecraft:ash` | Drifting ash/dust — industrial, post-apocalyptic, quiet devastation |
| `minecraft:smoke` | Thin smoke wisps — fire aftermath, fog of memory |
| `minecraft:flame` | Flame sparks — warmth, danger |
| `minecraft:soul_fire_flame` | Blue-tinted flame — grief, the spectral, the underworld |
| `minecraft:heart` | Hearts — joy, affection (use sparingly; can read as comic) |
| `minecraft:note` | Musical notes — playfulness, the sonic made visible |
| `minecraft:enchant` | Enchantment glyphs — magic, mystery, the arcane |
| `minecraft:totem_of_undying` | Radiant burst — survival, vitality, rebirth |
| `minecraft:end_rod` | White sparkle — ethereal, arrival, the transcendent |
| `minecraft:snowflake` | Snowfall texture — cold, stillness, isolation |
| `minecraft:warped_spore` | Teal drifting spores — alien landscape, the uncanny |
| `minecraft:crimson_spore` | Red drifting spores — corruption, blood, the hostile biome |

---

### CAMERA — Screen distortion effects

Applies potion-effect-based perceptual distortion to the player's screen. Does not control camera direction — that is FACE, PLAYER_TELEPORT, and PLAYER_SPECTATE.

```yaml
type: CAMERA
effect: blackout    # sway | blackout | flash | float
intensity: 1        # amplifier (0–3; not all effects use it meaningfully)
duration_ticks: 40
audience: participants
```

| Effect | Potion applied | Visual result | Typical duration |
|--------|----------------|---------------|-----------------|
| `blackout` | DARKNESS | Smooth fade to near-black and back | 40t for transition |
| `sway` | NAUSEA | Screen wobble/spiral | 40–80t subtle; 100t+ pronounced |
| `flash` | BLINDNESS | Immediate white flash | 10–20t |
| `float` | LEVITATION + SLOW_FALLING | Brief upward drift sensation | 20–60t |

**`blackout` as scene transition — standard technique:**
Fire `blackout` 10 ticks before a teleport. During the black: run PLAYER_TELEPORT, swap sets, change environment. Player returns from black to find themselves somewhere new.

---

---

## Cross-Department Coordination

**Effects + Camera:** The closest working partnership. Effects decides where the player's body is; Camera decides where their eyes are pointing. On every PLAYER_TELEPORT that also resets orientation, both departments coordinate — Effects sets the destination, Camera sets the yaw/pitch. On every levitation or flight sequence, Camera asserts the player's pitch before liftoff so they see the intended horizon as they rise.

**Effects + Fireworks:** The Effects Director owns the player's altitude when fireworks detonate. The Fireworks Director designs the burst altitude to match where the player will be. Agree upfront on player height at each pyrotechnic moment.

**Effects + Lighting:** `night_vision` is Effects authority but reveals the environment Lighting crafted. Any scene using `night_vision` requires joint planning — does the revealed space support the intended atmosphere?

**Effects + Choreography:** CROSS_TO on a performer moving *toward* the player is Choreography. CROSS_TO forcing the *target player* to a position is Effects. When planning scenes where both happen simultaneously, Effects and Choreography coordinate so the player's scripted movement doesn't conflict with a performer cross.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Effects department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `kb/departments/stage-manager.kb.md` → Active Gap Registry.

**Limitations to be aware of:**
- PLAYER_FLIGHT hover freezes at *current* altitude — must lift first, then hover.
- PLAYER_VELOCITY impulse can be partially counteracted by the player — for a scripted arc, follow with PLAYER_FLIGHT hover to lock altitude.
- `night_vision` requires coordination with Lighting to ensure the revealed environment is intentional.
- Camera orientation gaps (FACE pitch, no ROTATE) are Camera department concerns — see `kb/departments/camera.kb.md §Gaps and Limitations`.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| PLAYER_TELEPORT | ✅ Verified | All fields; yaw/pitch confirmed; orientation-only (offset 0,0,0) confirmed |
| PLAYER_FLIGHT hover | ✅ Verified | Altitude lock confirmed in calibration sessions |
| PLAYER_FLIGHT release + slow_falling | ✅ Verified | Controlled descent confirmed |
| PLAYER_VELOCITY | ✅ Verified | Vector impulse confirmed |
| EFFECT levitation (HOVER/CLIMB/RELEASE patterns) | ✅ Verified | Calibration data from 2026-03-24 |
| EFFECT slow_falling | ✅ Verified | ~2 b/s descent rate confirmed |
| EFFECT night_vision | ✅ Verified | Cross-discipline coordination note applies |
| EFFECT blindness / darkness / nausea | ✅ Verified | |
| EFFECT speed / slowness | ✅ Verified | |
| PARTICLE (single burst) | ✅ Verified | |
| PARTICLE (repeating bar with interval) | ✅ Verified | force: true bypasses client particle settings |
| GIVE_ITEM to player | 📋 Aspirational | Not yet implemented or filed |
| GIVE_XP to player | 📋 Aspirational | Not yet implemented or filed |

> Camera department events (CAMERA, PLAYER_SPECTATE, PLAYER_MOUNT, FACE) are tracked in
> `kb/departments/camera.kb.md §Capability Status Summary`.
