---
status: active
owner: Alan
notes: >
  Authoritative YAML schema and plugin design reference for ScaenaShows v2.
  ScaenaComposer sections are deferred (Phases 2–6 skipped) but the YAML schema
  remains current and accurate. This is the definitive source of truth for all
  Cue, Show, and event-type YAML syntax.
---

# ScaenaShows v2 — Design Specification

This document is the authoritative YAML schema reference for the ScaenaShows v2 plugin. All Cue authoring, Show authoring, and event-type syntax derives from this document.

---

## 1. Scope

**In scope:**
- Full rewrite of the ScaenaShows Java plugin (Paper 1.21.x)
- New YAML schema for shows, cues, and firework presets
- ScaenaComposer — local Node.js web application for authoring
- In-tool sketch preview (canvas animation + browser audio)
- In-game live preview via RCON
- ShowSprite — AI creative assistant embedded in ScaenaComposer
- Cue library with naming conventions, tags, and archetypes

**Out of scope:**
- RCON credentials and network setup (separate session; connection is confirmed available)
- Resource pack or custom sound integration
- Multi-server hosting of ScaenaComposer

**Project home:** ScaenaShows repo. This spec lives in `scaenacraft-ops/artifacts/` as the design reference.

---

## 2. Core Design Decisions

| Decision | Choice |
|---|---|
| Schema approach | Plugin and schema co-designed as a unit (not a retrofit) |
| Container model | One universal recursive type: **Cue** |
| Reuse model | Reference by ID — Cues are named assets stored independently |
| Multi-target model | One `RunningShow` instance per `/show play`; multiple participants; spatial anchor = first named target |
| UI navigation | Hybrid — top 1–2 levels expand in place; deeper drills down with breadcrumbs |
| Server runtime | Node.js — single language across front and back end |
| File access model | ScaenaComposer reads/writes local ScaenaShows repo checkout; RCON live preview is a separate connection to the live server |
| YAML library (Java) | SnakeYAML directly (bundled with Paper) — not Paper's `FileConfiguration` API, which is not suited to deeply nested heterogeneous schemas |
| Spatial view | 2D top-down interactive canvas — the center of the tool |
| Preview tiers | Sketch (in-tool), Composer (in-tool + log), Live (RCON in-game) |
| AI assistant | ShowSprite — tool-calling Claude via Anthropic API |
| Schema versioning | No version field on cue/show YAML files in v2 — no backward compatibility with v1 files is required or desired |

---

## 3. Data Model

### 3.1 The Cue

The Cue is the single universal container type. Everything is either a Cue or a leaf event inside a Cue. Nesting is unlimited in depth.

Every Cue carries:
- `id` — unique identifier; follows the `[category].[archetype].[variant]` convention (Section 8)
- `name` — human-readable display label
- `description` — optional; used by ShowSprite when reasoning about this Cue
- `duration_ticks` — declared length
- `tags` — multi-valued list drawn from the taxonomy in Section 9
- `meta` — authorship block
- `timeline` — ordered list of events and/or child Cue references, each at an `at` tick

**Authorship block (on every Cue and Show):**
```yaml
meta:
  created_by: Zara        # Zara | Smitty
  created_at: 2026-03-23
  last_edited_by: Zara
  last_edited_at: 2026-03-23
```

### 3.2 The Show

A Show is a Cue with additional show-level fields. The plugin treats it as a runtime entry point.

Additional Show fields:
- `portable` — boolean; show is intended for reuse across worlds and locations
- `default_mode` — `follow` (show center tracks the spatial anchor player) or `static` (fixed at invocation location)
- `default_audience` — `broadcast` or `participants`
- `group_count` — how many player groups to auto-assign at show start (default 2)
- `group_strategy` — `join_order` | `alphabetical` | `random`
- `front` — the compass direction that defines downstage (default: primary target's initial facing direction). Accepted: `north` | `south` | `east` | `west` | degrees 0–359.
- `marks` — named positions in the show's coordinate space (Section 10)
- `sets` — named world locations for player teleportation (Section 10)
- `bossbar` — show-wide bossbar definition

**Multi-participant model:**

`/show play <showId> <target> [<target2> ...]` creates **one** `RunningShow` instance. All named targets (or all players matched by a selector like `@a[distance=..20]`) become **participants** — they are simultaneously actors and audience within the same show. Each participant gets their own `home` position captured at invocation. The first named target (or invoker if a selector is used) is the **spatial anchor** — the origin for mark offsets, firework positions, and `follow` mode movement.

There is no concept of parallel independent instances. The show runs once, affecting all participants as a single coordinated event. This is immersive theatre: the show moves around its cast, not just its audience.

### 3.3 Firework Presets

Firework presets live in `fireworks.yml` as a named library. They define a rocket's power and one or more stars. Referenced by ID from any firework event or pattern.

### 3.4 File Layout

```
plugins/ScaenaShows/
  config.yml
  fireworks.yml
  cues/
    *.yml
  shows/
    *.yml

ScaenaComposer/
  composer.config.json      # local only — gitignored (RCON host/port/password, builder names, asset path)
  showsprite.context.md     # shared vocabulary and creator profiles — committed to repo
  history/
    [show_id].jsonl         # conversation history per show — committed to repo
```

### 3.5 config.yml structure

```yaml
# plugins/ScaenaShows/config.yml
default_cooldown_seconds: 30     # Cooldown per invoker between /show play uses
resume_window_seconds: 900       # How long a disconnected participant has to rejoin before their slot is dropped
```

Simple by design. Additional keys may be added as needed. All values have defaults baked into the plugin; `config.yml` only needs to contain overrides.

---

`composer.config.json` structure (values stored locally, never committed):
```json
{
  "rcon": {
    "host": "...",
    "port": 0,
    "password": "..."
  },
  "builders": ["Zarathale", "Smitty2020"],
  "minecraft_assets_path": ""
}
```

---

## 4. YAML Schema

### 4.1 Cue

```yaml
id: ramp.warm_gold.01
name: "Warm Gold Ramp"
description: "Building fireworks sequence in warm gold tones. Designed for use before a peak."
duration_ticks: 180
tags: [energy:high, pace:building, tone:warm, arc:ramp, effect:fireworks-heavy, pattern:circle, duration:moment, intensity:intense]

meta:
  created_by: Zara
  created_at: 2026-03-23
  last_edited_by: Zara
  last_edited_at: 2026-03-23

timeline:
  - at: 0
    type: SOUND
    sound_id: minecraft:block.note_block.chime
    category: master
    volume: 1.0
    pitch: 1.0

  - at: 0
    type: FIREWORK_CIRCLE
    preset: scaena_gold
    radius: 6
    count: 8
    y_mode: surface
    y_offset: 2
    chase:
      enabled: true
      direction: FL
      interval_ticks: 6
    power_variation: RAMP_UP
    color_variation: UNIFORM

  - at: 80
    type: CUE
    cue: ramp.warm_gold.01.peak_burst
```

### 4.2 Show

```yaml
id: show_celebration
name: "Celebration"
description: "General-purpose celebratory show."
portable: true
default_mode: follow
default_audience: broadcast
group_count: 2
group_strategy: join_order
front: south

marks:
  center:      {x:  0, z:  0}
  stage_left:  {x: -6, z:  0}
  stage_right: {x:  6, z:  0}
  upstage:     {x:  0, z: -8}
  downstage:   {x:  0, z:  6}
  wing_left:   {x:-15, z:  0}
  wing_right:  {x: 15, z:  0}

sets:
  main_stage:
    world: world
    x: 142
    y: 68
    z: -304
    yaw: 180.0
    pitch: 0.0
    return_on_end: true

tags: [tone:celebratory, context:celebration]

meta:
  created_by: Zara
  created_at: 2026-03-23
  last_edited_by: Smitty
  last_edited_at: 2026-03-23

bossbar:
  enabled: true
  title: "<gold><bold>Celebration</bold></gold>"
  color: YELLOW
  overlay: PROGRESS
  audience: broadcast
  fade_in_ticks: 10
  fade_out_ticks: 20

timeline:
  - at: 0
    type: GROUP_ASSIGN

  - at: 0
    type: CUE
    cue: open.shimmer.tender

  - at: 220
    type: CUE
    cue: ramp.warm_gold.01

  - at: 460
    type: REST
    duration_ticks: 40
    name: "Breath"

  - at: 500
    type: CUE
    cue: finish.starburst.big
```

### 4.3 Firework Preset

A preset supports multiple stars per rocket. Stars detonate simultaneously.

```yaml
version: 2
presets:
  scaena_gold:
    display_name: "Scaena Gold"
    power: 2
    stars:
      - shape: BALL_LARGE    # BALL | BALL_LARGE | STAR | BURST | CREEPER
        colors: ["#FECB00", "#FFFFFF"]
        fades: ["#EA7125"]
        trail: true
        flicker: false
      - shape: BURST
        colors: ["#FFFFFF"]
        fades: []
        trail: false
        flicker: true

  texture_white_gold:
    display_name: "White + Gold Texture"
    power: 2
    stars:
      - shape: BALL
        colors: ["#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFD700"]
        # 3 white + 1 gold: all colors appear simultaneously; ratio determines particle density
        fades: []
        trail: false
        flicker: false
```

---

## 5. Invocation

### Command syntax

```
/show play <showId> <target> [<target2> ...] [--follow|--static] [--private] [--scenes]
/show stop <player|@a>
/show stopall
/show list
/show reload
```

All named targets (or selector matches) become participants in **one** `RunningShow` instance. The first named target is the spatial anchor. Multiple participants are actors and audience within the same show — immersive theatre, not parallel instances.

```
/show play celebration Smitty2020
/show play celebration Zarathale Smitty2020
/show play celebration @a
/show play celebration @a[distance=..20]
```

`--private` restricts all `broadcast`-audience events to the invoker only. Useful for testing a show solo without affecting other players.

`--scenes` enables director mode (admin-only): cue name shown in the invoker's action bar at the start of each Cue. Uses `audience: invoker` — does not appear to participants.

### Cooldown and permissions

| Permission | Capability |
|---|---|
| `scae.shows.play` | Play shows targeting self |
| `scae.shows.target.others` | Target other players by name |
| `scae.shows.target.all` | Target via selectors (`@a`, `@a[distance=..]`) |
| `scae.shows.admin` | Everything; bypasses cooldown |
| `scae.shows.cooldown.bypass` | Bypass cooldown only |
| `scae.shows.private` | Use `--private` flag |

Default cooldown: 30s per invoker. Configurable via `default_cooldown_seconds` in `config.yml`.

---

## 6. Event Type Reference

All events carry `at` (start tick) and `type`. Duration events render as **colored bars** on the timeline. Point-in-time events render as **narrow icon markers**.

---

### 6.1 Text and Display

**Text formatting:** All text fields use `&` color codes. `&r` resets formatting. See `kb/departments/voice/voice.kb.md §Text Formatting Reference` for the full code table including server-specific `&y` (wavy) and `&u` (rainbow).

**MESSAGE** — point-in-time
```yaml
type: MESSAGE
audience: broadcast | private | group_1 | group_2 | group_3 | group_4 | target
message: "&6Text here."
```

**TITLE** — bar (fade_in + stay + fade_out ticks)
```yaml
type: TITLE
audience: broadcast | private | group_1 | group_2 | target
title: "&6&lTitle"
subtitle: "&7Subtitle here."
fade_in: 10
stay: 40
fade_out: 10
```

**ACTION_BAR** — bar
```yaml
type: ACTION_BAR
audience: broadcast | private | group_1 | group_2 | target
message: "&7Action bar text."
duration_ticks: 60
# Plugin re-sends every 20 ticks to persist; action bar clears after ~40 ticks if not refreshed.
```

**BOSSBAR** — bar
```yaml
type: BOSSBAR
title: "&6Bossbar text."
color: YELLOW       # PINK | BLUE | RED | GREEN | YELLOW | PURPLE | WHITE
overlay: PROGRESS   # PROGRESS | NOTCHED_6 | NOTCHED_10 | NOTCHED_12 | NOTCHED_20
audience: broadcast | private | group_1 | group_2 | target
duration_ticks: 200
fade_in_ticks: 10   # plugin animates progress 0→1
fade_out_ticks: 20  # plugin animates progress 1→0
```

---

### 6.2 Sound

**SOUND** — bar (natural duration from database; shortened by max_duration_ticks if set)
```yaml
type: SOUND
sound_id: minecraft:block.bell.use
category: master | music | record | weather | block | hostile | neutral | player | ambient | voice
volume: 1.0
pitch: 1.0           # 0.5–2.0; 1.0 is the sound's natural pitch
max_duration_ticks: 32  # optional; plugin calls stopSound after N ticks
                        # Truncation only — the Minecraft sound system does not support seek/start-offset.
```

---

### 6.3 Visual Effects

**PARTICLE** — point-in-time with fade tail; bar if duration + interval set
```yaml
type: PARTICLE
particle_id: minecraft:heart
count: 12
offset: [0.5, 0.8, 0.5]
extra: 0.0
force: true
duration_ticks: 100   # optional; enables repeating
interval_ticks: 20    # fire every N ticks for duration
```

**EFFECT** — bar
```yaml
type: EFFECT
effect_id: night_vision
duration_ticks: 200
amplifier: 0
hide_particles: true
audience: broadcast | private | group_1 | group_2 | target
```

**GLOW** — bar
```yaml
type: GLOW
color: GOLD
# WHITE | ORANGE | MAGENTA | LIGHT_BLUE | YELLOW | LIME | PINK | GRAY
# LIGHT_GRAY | CYAN | PURPLE | BLUE | BROWN | GREEN | RED | BLACK
duration_ticks: 200
audience: broadcast | private | group_1 | group_2 | target | entity:spawned:Name | entity:world:Name
# Plugin manages a show-scoped team (scae_[show_id]_g[n]), sets team color,
# applies GLOWING effect. Both are cleaned up at show end.
```

**CAMERA** — bar; maps to actual Paper 1.21.x potion effects applied to players. No NMS required.

The Minecraft client has no general-purpose camera shake API accessible via Paper. All CAMERA effects are implemented via potion effects that produce perceptual camera distortion. The `effect` field maps to a specific Paper `PotionEffectType`.

```yaml
type: CAMERA
effect: sway         # sway | blackout | flash | float
                     # sway      → NAUSEA (amplifier scales intensity; classic camera wobble)
                     # blackout  → DARKNESS (smooth fade to near-black and back)
                     # flash     → BLINDNESS (immediate blackout; brief — set duration_ticks 10–20)
                     # float     → LEVITATION (amp 0) + SLOW_FALLING; brief upward drift sensation
intensity: 1         # amplifier passed to the potion effect (0–3; not all effects use it meaningfully)
duration_ticks: 40
audience: participants | group_1 | group_2 | invoker
```

These are the full palette of camera-perceptual effects achievable without NMS in Paper 1.21.11. They can be stacked: a DARKNESS followed immediately by a MESSAGE creates a dramatic reveal. A LEVITATION + SLOW_FALLING combo creates a controlled float arc.

**LIGHTNING** — point-in-time (cosmetic; no damage, no fire)
```yaml
type: LIGHTNING
offset: {x: 5, y: 0, z: 0}
```

---

### 6.4 Team and Groups

**GROUP_ASSIGN** — point-in-time; place at tick 0
```yaml
type: GROUP_ASSIGN
# Uses show-level group_count and group_strategy.
# If fewer players are online than groups defined, extra groups are assigned zero members.
# Any event targeting an empty group executes with zero targets — silent skip, no error.
```

**TEAM_COLOR** — point-in-time
```yaml
type: TEAM_COLOR
target: group_1 | group_2 | group_3 | group_4 | broadcast
color: GOLD
# Rhythmic color cycling: place multiple TEAM_COLOR events in sequence.
```

**GROUP_EVENT** — point-in-time; fires a named Cue scoped to a specific player group
```yaml
type: GROUP_EVENT
at: 40
target: group_1        # group_1 | group_2 | group_3 | group_4
cue_id: ramp.warm_gold.buildup
# Fires the named Cue inline, but restricts all audience resolution within that Cue
# to participants assigned to the target group. Events in the Cue that use
# audience: broadcast or audience: participants will resolve only against the group's
# members for the duration of this invocation.
# If the target group has zero members (GROUP_ASSIGN underflow), this is a silent skip.
# The Cue must exist in the Cue registry — fail at load time if the ID is unknown.
```

---

### 6.5 Fireworks and Spatial Patterns

**FIREWORK** — estimated-duration bar
```yaml
type: FIREWORK
preset: scaena_gold
offset:
  x: 0.0
  y: 2.0
  z: 0.0
y_mode: relative   # relative: Y offset from target's feet
                   # surface: Y offset from highest block at this XZ position
min_clearance: 3   # optional; minimum height above target's feet regardless of y_mode
```

Timeline bar: launch marker → flight bar (P1≈25t, P2≈40t, P3≈55t) → bloom zone (~20t).

**FIREWORK_CIRCLE** — pattern generator
```yaml
type: FIREWORK_CIRCLE
preset: scaena_gold
origin_offset: {x: 0, z: 0}
radius: 10
count: 12
y_mode: surface
y_offset: 2
chase:
  enabled: true
  direction: FL        # FL (first-to-last) | LF (last-to-first)
  interval_ticks: 4
power_variation: UNIFORM   # UNIFORM | RAMP_UP | RAMP_DOWN | ALTERNATE | RANDOM
color_variation: RAINBOW   # UNIFORM | RAINBOW | GRADIENT | ALTERNATE
                           # UNIFORM   → all positions use the preset's colors unchanged
                           # RAINBOW   → cycles through fixed 7-color array [red, orange, yellow, green, cyan, blue, violet]
                           #             overrides preset primary colors; applies per position in sequence
                           # GRADIENT  → interpolates between gradient_from and gradient_to across N positions
                           #             overrides preset primary colors per position
                           # ALTERNATE → flips between the preset's first and second star primary colors per position
                           #             if preset has only one star, falls back to UNIFORM
gradient_from: "#FF0000"
gradient_to: "#0000FF"
```

`color_variation` overrides the preset's primary star colors. Trail, flicker, fade colors, and shape are always taken from the preset unchanged.

**FIREWORK_LINE** — pattern generator
```yaml
type: FIREWORK_LINE
preset: scaena_gold
start_offset: {x: -5, z: 0}
length: 10
# ANGLE CONVENTION: compass/bearing — clockwise from north.
#   0 = north (−Z),  90 = east (+X),  180 = south (+Z),  270 = west (−X)
# This is the OPPOSITE of standard math/trig angles (which are counterclockwise from east).
# Always use compass convention here. Do not confuse.
angle: 0
count: 6
y_mode: surface
y_offset: 2
chase:
  enabled: true
  direction: FL
  interval_ticks: 3
power_variation: RAMP_UP
color_variation: UNIFORM
```

**FIREWORK_FAN** — pattern generator; V = 2 arms, W = 4, fan = N
```yaml
type: FIREWORK_FAN
origin_offset: {x: 0, z: 0}
arms:
  - angle: 315
    length: 8
    count: 4
    preset: scaena_gold
  - angle: 45
    length: 8
    count: 4
    preset: scaena_gold
y_mode: surface
y_offset: 2
chase:
  enabled: true
  direction: FL
  interval_ticks: 3
  mode: sequential     # sequential: numbering continues across all arms in order
                       # simultaneous: each arm chases independently at the same time
```

The `mode` field on fan chase is a creator-facing toggle in the UI.

**FIREWORK_RANDOM** — pattern generator; N fireworks at random XZ positions within a radius
```yaml
type: FIREWORK_RANDOM
preset: scaena_gold
origin_offset: {x: 0, z: 0}   # XZ offset from spatial anchor before scattering
radius: 12                      # scatter radius in blocks
count: 8                        # number of fireworks to launch
y_mode: surface                 # surface | relative (same as other firework patterns)
y_offset: 2
seed: null                      # optional integer; if set, same random positions each run
                                # if null, positions are different on every play
```

Positions are chosen using uniform random sampling within the radius circle. No two fireworks
are guaranteed to land at the same XZ. Unlike FIREWORK_CIRCLE and FIREWORK_LINE, there is no
`chase` field — all FIREWORK_RANDOM launches fire simultaneously.

---

### 6.6 World and Environment

**REDSTONE** — point-in-time; always world-specific
```yaml
type: REDSTONE
world_specific: true
target: {x: 100, y: 64, z: 200}
state: on   # on | off
```

**TIME_OF_DAY** — point-in-time
```yaml
type: TIME_OF_DAY
time: 6000   # 0=sunrise, 6000=noon, 12000=sunset, 18000=midnight
```

**WEATHER** — bar if duration set
```yaml
type: WEATHER
state: clear   # clear | storm | thunder
duration_ticks: 400
```

---

### 6.7 Entity Management

**SPAWN_ENTITY** — bar (spawn to despawn)
```yaml
type: SPAWN_ENTITY
entity_type: VILLAGER   # any Minecraft EntityType
offset: {x: 5, y: 0, z: 5}
name: "Herald"          # required for subsequent targeting
baby: false
variant: PLAINS         # optional; villager biome, horse color, sheep wool color, etc.
profession: CLERIC      # optional; for villagers
equipment:              # optional; items applied at spawn
  helmet: GOLDEN_HELMET
  main_hand: STICK
despawn_on_end: true
```

**DESPAWN_ENTITY** — point-in-time
```yaml
type: DESPAWN_ENTITY
target: entity:spawned:Herald | entity_group:chorus
particle_burst: true    # optional visual effect on despawn
```

**CAPTURE_ENTITIES** — point-in-time; creates a named entity group from world entities
```yaml
type: CAPTURE_ENTITIES
entity_type: COW
radius: 15
max_count: 8
group_name: chorus
capture_mode: snapshot  # snapshot: fixed group captured once at this tick
                        # live: group re-sweeps each time a targeting event fires
```

**RELEASE_ENTITIES** — point-in-time; releases a captured entity group from show control
```yaml
type: RELEASE_ENTITIES
target: entity_group:chorus   # entity group name (must have been created by CAPTURE_ENTITIES)
restore_ai: true              # default true — re-enables AI (reverses ENTITY_AI enabled: false)
                              # set to false to release tracking only without touching AI state
```

Removes the entity group from the show's internal entity group registry. After RELEASE_ENTITIES,
the group name can no longer be used as a target within this show. Entities remain in the world
(they are not despawned). If `restore_ai: true`, an AI-enable call is issued to each entity in
the group. If the group does not exist or has zero members, this is a silent no-op.

---

### 6.8 Entity Behavior

These events target named spawned entities, captured entity groups, or world-resident named entities.

**ENTITY_AI** — point-in-time
```yaml
type: ENTITY_AI
target: entity:spawned:Herald | entity_group:chorus
enabled: false   # false = puppet state (full freeze); true = restore AI
```

**ENTITY_SPEED** — point-in-time
```yaml
type: ENTITY_SPEED
target: entity:spawned:Herald
speed: 0.0   # 0.0=stopped, 0.05=creep, 1.0=normal, 2.0=fast
```

**ENTITY_EFFECT** — bar
```yaml
type: ENTITY_EFFECT
target: entity:spawned:Herald | entity_group:chorus
effect_id: levitation
duration_ticks: 100
amplifier: 0
```

**ENTITY_EQUIP** — point-in-time
```yaml
type: ENTITY_EQUIP
target: entity:spawned:Herald
helmet: GOLDEN_HELMET
chestplate: GOLDEN_CHESTPLATE
leggings: ""
boots: ""
main_hand: STICK
off_hand: ""
```

**ENTITY_INVISIBLE** — bar (shorthand for INVISIBILITY effect)
```yaml
type: ENTITY_INVISIBLE
target: entity:spawned:Herald
duration_ticks: 60
```

**ENTITY_VELOCITY** — point-in-time
```yaml
type: ENTITY_VELOCITY
target: entity:spawned:Herald | entity_group:chorus
vector: {x: 0.0, y: 0.8, z: 0.0}
# Combine with ENTITY_EFFECT slow_falling for a controlled arc.
```

---

### 6.9 Stage Directions

Stage directions apply to players, named entities, or groups. They use the theatrical vocabulary defined in Section 10. All positions reference the mark system and home positions.

**HOLD** — point-in-time; freeze at current position
```yaml
type: HOLD
target: entity:spawned:Herald | group_1 | player | entity_group:chorus
```

**FACE** — point-in-time; turn to look at a mark, entity, or compass direction
```yaml
type: FACE
target: entity:spawned:Herald | group_1
look_at: mark:center   # mark:Name | player | compass:south | entity:spawned:Other
```

**CROSS_TO** — bar; move to a destination
```yaml
type: CROSS_TO
target: entity:spawned:Herald | group_1 | player
destination: mark:stage_left   # mark:Name | home | home+{x:5,z:0} | {x:-6,z:0}
duration_ticks: 40             # 0 or omitted = instant; with ticks: entity uses pathfinder,
                               # player uses repeated relative teleports
facing: mark:center            # optional; face this direction on arrival
```

**RETURN_HOME** — bar; return to home position captured at show invocation
```yaml
type: RETURN_HOME
target: group_1 | player | entity:spawned:Herald
duration_ticks: 20
```

**ENTER** — bar; spawn entity at a wing mark and move it to a destination (semantic shorthand)
```yaml
type: ENTER
entity_type: VILLAGER
name: Herald
from: mark:wing_left
destination: mark:stage_left
duration_ticks: 30
facing: mark:center
baby: false
```

**EXIT** — bar; move entity to a wing mark and optionally despawn (semantic shorthand)
```yaml
type: EXIT
target: entity:spawned:Herald
to: mark:wing_right
duration_ticks: 30
despawn_on_arrival: true
```

**Note — ORBIT not implemented in v2:** Smooth continuous circular entity movement requires per-tick position computation and teleport, which does not work reliably via the standard pathfinder API. This is a v3 candidate.

In v2, circular or looping movement patterns are composed in the Cue authoring tool using a sequence of `CROSS_TO` events targeting marks arranged at positions around a circle. ShowSprite can generate this sequence automatically: "Move Herald in a slow clockwise circle around center at radius 5, 12 steps." The result is a series of CROSS_TO events with short `duration_ticks` values that approximate orbital motion within the Cue's tick budget.

---

### 6.10 Player Movement

**PLAYER_TELEPORT** — point-in-time
```yaml
type: PLAYER_TELEPORT
audience: private | group_1 | group_2 | target
destination: set:main_stage     # set:Name (world-specific)
# OR
offset: {x: 0, y: 0, z: 20}    # relative from current position (portable)
yaw: 180.0                       # optional facing override
pitch: 0.0
```

**PLAYER_VELOCITY** — point-in-time
```yaml
type: PLAYER_VELOCITY
audience: private | group_1 | group_2 | target
vector: {x: 0.0, y: 1.2, z: 0.0}
# Combine with an immediate EFFECT slow_falling for controlled float.
```

**PLAYER_SPECTATE** — bar; attaches player camera to a named entity
```yaml
type: PLAYER_SPECTATE
entity: entity:spawned:CinematicCamera
audience: private | group_1 | group_2 | target
duration_ticks: 200   # optional; auto-ends spectate after N ticks
```

**PLAYER_SPECTATE_END** — point-in-time; returns camera to player body
```yaml
type: PLAYER_SPECTATE_END
audience: private | group_1 | group_2 | target
```

**PLAYER_MOUNT** — point-in-time; player rides a named entity
```yaml
type: PLAYER_MOUNT
entity: entity:spawned:GuideEntity
audience: private | group_1 | group_2 | target
```

**PLAYER_DISMOUNT** — point-in-time
```yaml
type: PLAYER_DISMOUNT
audience: private | group_1 | group_2 | target
```

**PLAYER_FLIGHT** — point-in-time; engage or release server-side flight for participants.

Two states:

`state: hover` — records each participant's pre-show flight state (once, via `putIfAbsent`), then calls `setAllowFlight(true)` + `setFlying(true)`. The player is frozen at their current Y until released or the show stops. A flying player can still move horizontally — add `EFFECT slowness` to discourage wandering if needed.

`state: release` — applies `release_effect` first (so the player never free-falls when flight drops), then restores the participant's pre-show flight state. Pre-show flight (e.g., creative mode) is fully preserved.

Stop safety extended: if `PLAYER_FLIGHT hover` was fired, `applyStopSafety` restores the pre-show flight state in addition to the standard slow_falling cleanup.

```yaml
# Engage hover — lock player at current altitude
type: PLAYER_FLIGHT
state: hover                        # hover | release
audience: participants

# Release with transition effect before disabling flight
type: PLAYER_FLIGHT
state: release
release_effect: slow_falling        # slow_falling (default) | levitate | none
release_duration_ticks: 300         # duration of the release effect (default 300)
audience: participants
```

**Authoring notes:**
- Always use a lift event (levitation via `EFFECT`) before hover — hover freezes at *current* altitude, not a target altitude.
- Always use `state: release` before show end rather than letting stop safety handle it — this gives you control over when the player starts descending.
- The `release_effect: levitate` option applies amp 0 levitation briefly, giving a slight upward drift before the transition. Use for dramatic "one last breath before the fall" moments.
- Horizontal drift while hovering: expected. The player is in flight mode. Design the show to occupy their attention or add a brief `EFFECT slowness` alongside hover.

**Future: PATH** — reserved event type for spline-curve player or entity movement along a multi-waypoint path with easing. Not implemented in v2.

---

### 6.11 Utility

**REST** — bar; silence. Holds time on the timeline with no effect.
```yaml
type: REST
duration_ticks: 40
name: "Breath"   # optional label
```

**COMMAND** — point-in-time; raw server command (escape hatch)
```yaml
type: COMMAND
command: "/title @a title {\"text\":\"Go\"}"
```

**CUE** — nested Cue reference; bar spanning child's duration_ticks
```yaml
type: CUE
cue: child_cue_id
```

---

## 7. Audience Targeting

| Value | Targets |
|---|---|
| `broadcast` | All online players (server-wide) |
| `participants` | All players in this show instance |
| `invoker` | The player who ran `/show play` (director only) |
| `private` | Alias for `invoker` |
| `target` | Alias for `invoker` (legacy) |
| `group_1` – `group_4` | Subset of participants auto-assigned at show start |
| `entity:spawned:Name` | Named entity spawned by this show |
| `entity:world:Name` | Named world-resident entity — resolved by display-name scan at show load time; fails fast if ambiguous (>1 match); UUID cached in RunningShow |
| `entity_group:Name` | Group captured by CAPTURE_ENTITIES |

---

## 8. Portability

Shows marked `portable: true` are designed for reuse across worlds and locations. All position offsets are relative to the show target by default.

World-specific events that trigger a portability warning:
- REDSTONE — `world_specific: true` always
- PLAYER_TELEPORT with `destination: set:*`
- `entity:world:Name` audience targets
- Any named set defined in the show YAML

The tool shows a ⚠ banner on portable shows containing world-specific events. No hard block — the warning is informational. `return_on_end: true` on sets ensures players are never stranded.

---

## 9. Naming Conventions

### Cue IDs

Format: `[category].[archetype].[variant]`

```
ramp.warm_gold.01         finish.starburst.big
ramp.cool_blue.02         finish.confetti.soft
open.shimmer.quiet        breath.silence.short
open.fanfare.bold         breath.glow_hold.tender
transition.sweep.left     moment.heart_burst.close
```

### Show IDs

Format: `show_[descriptor]` — e.g., `show_birthday_full`, `show_celebration`, `show_welcome`

### Firework Preset IDs

Format: `[palette]_[shape/character]` — e.g., `scaena_gold`, `texture_white_gold`, `pride_star`

---

## 10. Tag Taxonomy

Tags are multi-valued on every Cue. Values are free-form strings drawn from the dimensions below. ShowSprite suggests tags on create and edit. Any tag not in this list is valid.

### Emotional Tone
`dreamy` `triumphant` `tender` `playful` `mysterious` `reverent` `joyful` `melancholic` `tense` `celebratory` `ethereal` `warm` `icy` `nostalgic` `whimsical` `dramatic` `serene` `intimate` `grand`

### Energy
`low` `medium` `high` `explosive`

### Pace
`still` `slow` `moderate` `fast` `frantic` `pulsing` `building` `erratic`

### Arc Position
`opener` `buildup` `ramp` `peak` `release` `coda` `transition` `breath` `standalone` `bridge`

*`standalone` — functions outside any larger arc. `bridge` — connects two adjacent sections stylistically.*

### Effect Character
`fireworks-heavy` `fireworks-light` `particle-bed` `particle-burst` `text-forward` `text-accented` `sound-driven` `sound-accented` `silent` `effect-driven` `spatial`

### Duration Class
| Tag | Approximate range |
|---|---|
| `flash` | under 2s / ~40 ticks |
| `beat` | 2–4s / 40–80 ticks |
| `moment` | 4–8s / 80–160 ticks |
| `scene` | 8–30s / 160–600 ticks |
| `act` | 30s+ / 600+ ticks |

### Pattern Type
`single` `circle` `line` `fan` `scatter` `cluster` `overhead` `surrounding` `ground-level` `ambient`

### Intensity
`subtle` `moderate` `intense` `overwhelming`

### Spatial Character
`centered` `radial` `directional` `overhead` `enveloping`

### Context / Occasion
`birthday` `welcome` `farewell` `seasonal` `milestone` `achievement` `ambient` `celebration` `ceremony` `arrival` `revelation`

### Creator
`Zara` `Smitty` — auto-set from authorship metadata; do not tag manually

---

## 11. Sets, Marks, and Stage Space

### Home position

At show invocation, before any events fire, the plugin records each participant's world location as their `home`. Home is a runtime value — stored in the running show, not in YAML. Each participant has their own home. `RETURN_HOME target:group_1` returns every group_1 member to their own home independently.

Home is available as a mark reference in any positional event: `destination: home` or as an offset origin: `destination: home+{x:5,z:0}`.

### Stage front

The `front` field on the show defines which compass direction is downstage — the audience's perspective. This gives stage directions their meaning. Without a defined front, `stage_left` and `upstage` have no orientation.

Default: primary target player's initial facing direction at invocation.

### Marks

Marks are named XZ offset positions relative to the show target's home. Defined under `marks:` in the show YAML. All marks are relative — portable by default.

The standard 9-position stage grid can be constructed from marks:

```
UL (upstage_left) | UC (upstage)  | UR (upstage_right)
SL (stage_left)   | CC (center)   | SR (stage_right)
DL (down_left)    | DC (downstage)| DR (down_right)
```

Wings (entry/exit zones) are placed beyond the playing area boundary.

### Sets

Sets are named world-specific locations for PLAYER_TELEPORT. Defined under `sets:` in the show YAML.

```yaml
sets:
  main_stage:
    world: world
    x: 142
    y: 68
    z: -304
    yaw: 180.0
    pitch: 0.0
    return_on_end: true
```

`return_on_end: true` returns players to their pre-show world location when the show ends, stops, or the player disconnects. Always recommended.

### Stage directions vocabulary

| Term | Meaning in show context |
|---|---|
| **home** | Participant's captured position at show invocation |
| **mark** | A named position in the show's XZ coordinate space |
| **front** | The defined downstage direction |
| **upstage / downstage** | Away from / toward the defined front |
| **stage left / stage right** | From performer's perspective facing front |
| **wings** | Off the playing area; entry and exit zones |
| **cross** | Move from current position to a mark (CROSS_TO) |
| **hold** | Freeze at current position (HOLD) |
| **face** | Turn to look at a target (FACE) |
| **enter** | Arrive from a wing position (ENTER) |
| **exit** | Leave to a wing position (EXIT) |
| **return** | Go back to home position (RETURN_HOME) |
| **puppet** | Entity with AI disabled — under full show control |
| **chorus** | A named group of entities acting as visual ensemble |
| **cast member** | Any entity (player or mob) participating in a show |
| **set piece** | Armor stand or display entity used as a static prop |
| **mark:center** | Shorthand for center mark; similarly mark:SL, mark:DR, etc. |

ShowSprite uses this vocabulary. "Want Herald to enter from wing left, cross to center, hold, then exit stage right?" maps directly to a sequence of events.

---

## 12. Cue Library

### The library as a creative project

The Cue library is a designed artifact, not a byproduct of using the tool. Sessions dedicated to building out the library are normal workflow. ShowSprite participates as a collaborator in those sessions.

### Seeded starter set

The library ships with a curated initial set of Cue files covering common archetypes. These are real `.yml` files, immediately draggable onto any timeline, authored by Alan to establish the aesthetic baseline. ShowSprite uses these as reference examples when generating variations.

Suggested seeds (to be authored in a dedicated library session):

| Category | Archetypes |
|---|---|
| `open.*` | `shimmer.quiet`, `fanfare.bold`, `shimmer.grand`, `heartbeat.tender` |
| `ramp.*` | `warm_gold.01`, `cool_blue.01`, `cascade.fast`, `swell.slow` |
| `peak.*` | `starburst.full`, `circle.wide`, `overhead.burst`, `intimate.close` |
| `finish.*` | `starburst.big`, `confetti.soft`, `glow_hold.warm`, `cascade.long` |
| `breath.*` | `silence.short`, `silence.long`, `glow_hold.tender`, `particle_drift.slow` |
| `transition.*` | `sweep.left`, `sweep.right`, `fade.soft`, `snap.hard` |
| `moment.*` | `heart_burst.close`, `text_reveal.grand`, `levitate.dreamy`, `shimmer.ethereal` |

### ShowSprite-generated variations

ShowSprite generates new Cue variations on request, informed by library seeds as style references, the current show's arc, and the creator's aesthetic profile. Generated Cues are proposed as diffs and require approval before being added to the library. ShowSprite suggests tags, a naming convention ID, and a description.

---

## 13. ShowSprite

### Identity

ShowSprite is the AI creative assistant embedded in ScaenaComposer. It/they. A distinct entity — not ZaraSprite, not Claude presented neutrally. A creative collaborator who lives in the tool: fluent in structural Cue operations and atmospheric creative language, familiar with ScaenaCraft's theatrical identity, a buddy in the room.

### Interaction model

**Reactive by default.** ShowSprite listens and responds when addressed. It does not interrupt the building process.

**"Review this Cue" button.** Triggers a proactive pass on the currently-selected container. ShowSprite reads the Cue and offers observations, pacing notes, tag suggestions, and ideas without being asked.

**Tool-calling with diff/approve.** When ShowSprite proposes a change — add a Cue, insert an event, shift timing, generate a pattern — the proposal appears as a structured diff. The creator approves or rejects. No change is applied without approval.

**Context = the selected container.** ShowSprite sees and acts on the currently-focused Cue only. To give ShowSprite broader context, the creator selects a larger container before asking. The container is the scope.

### Technical architecture

- API: Anthropic (`claude-sonnet-4-6`), streaming
- API key: stored in local `composer.config.json` — never committed
- Context injection: on every message, the Node server packages the selected Cue's full YAML + a structured summary (duration, child count, event type breakdown, tags) into the system prompt
- Streaming responses appear in real time in the chat panel

**ShowSprite tools:**
`add_cue_ref` `insert_event` `move_event` `set_duration` `rename_cue` `set_tags` `generate_pattern` `generate_cue` `preview_sketch` `suggest_library_addition`

### Creator personalization

ShowSprite adapts to each creator's working style over time. It tracks aesthetic preferences, vocabulary mappings, and patterns of acceptance and rejection for both Zara and Smitty separately.

This knowledge lives in `showsprite.context.md` — committed to the shared repo, readable by both creators, loaded into ShowSprite's system prompt at every session start. ShowSprite can propose additions to this file.

`showsprite.context.md` covers:
- ScaenaCraft's theatrical identity and aesthetic values
- Zara's aesthetic preferences and vocabulary (built up over sessions)
- Smitty's working patterns and vocabulary (built up over sessions)
- Archetype descriptions and ShowSprite vocabulary reference
- Vocabulary shortcuts and known preferences

### Conversation history

History persists per show in `history/[show_id].jsonl`, committed to the shared repo. Each message is tagged with the author (Zara or Smitty, determined by the configured builder username at session start). Both creators read from and contribute to the same history.

**Compact history operation:** ShowSprite monitors its own context window usage. When the loaded history for a show approaches a soft token ceiling (configurable; default ~12,000 tokens), ShowSprite proposes a compaction: it summarizes older conversation history into a structured digest block, replaces the oldest raw messages with that digest, and preserves the most recent N turns verbatim. The compacted `.jsonl` is written back to disk and committed. ShowSprite can also be triggered manually: "Compact this show's history." The digest format includes: decisions made, cues created or significantly changed, and aesthetic direction agreed on. Compaction is non-destructive — the original file is archived as `history/[show_id].[timestamp].bak.jsonl` before overwrite.

### Authorship as an operand

Every Cue carries `created_by` and `last_edited_by`. ShowSprite can filter and operate on authorship:
- "Show me everything Smitty built in this container"
- "Apply this timing fix to all of Zara's cues in this act"
- "What has been touched since I last worked on this show?"

---

## 14. ScaenaComposer

### Overview

ScaenaComposer is a local Node.js Express server + browser UI. It reads and writes YAML from/to a **local checkout of the ScaenaShows git repo** — it does not connect directly to the server's file system. The workflow is: author in ScaenaComposer → files save to local repo → push to GitHub → SFTP sync to Bisect Hosting to deploy.

RCON live preview is a **separate connection** from file editing. When RCON is connected, `/show play` commands are sent to the live server to preview the current state in-game. File state and live server state are independent — a file save does not trigger a live reload unless the builder explicitly triggers `/show reload` via RCON.

ScaenaComposer manages the RCON connection, serves Minecraft sound assets from the local CurseForge asset cache, and proxies Anthropic API calls. Opens at `http://localhost:3000`. macOS and Windows compatible (CurseForge launcher).

Started with: `npm start`

### UI layout

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ TOOLBAR  [New Cue] [New Show] [Save] [Reload]                                │
│          Preview: [▶ Cue] [▶ Show] [■ Stop]   Builder: Zarathale ▾           │
│          RCON: ● connected                                                    │
├──────────────┬──────────────────────────────────────┬─────────────────────── │
│              │ BREADCRUMB: Show > Opening Act        │                        │
│   LIBRARY    │                                       │   PROPERTIES           │
│  Cues        │  TIMELINE (multi-lane, scrollable)    │                        │
│  Shows       │  ┌────────────────────────────────┐   │  [selected event       │
│  Fireworks   │  │ Messages   ░░▓▓░░░░░░░░░░░░░░  │   │   or Cue ref fields]  │
│              │  │ Titles     ░░░░▓▓▓▓▓░░░░░░░░░  │   │                        │
│  [search]    │  │ Bossbar    ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │   │                        │
│  [tag filter]│  │ Sounds     ░▓░░░▓░░░░░▓░░░░░░  │   │                        │
│              │  │ Particles  ░▓▓░░▓▓░░░▓▓░░░░░░  │   │                        │
│              │  │ Effects    ▓▓▓▓▓▓▓▓░░░░░░░░░░  │   │                        │
│              │  │ Glow/Team  ░░▓▓▓▓░░░░░░░░░░░░  │   │                        │
│              │  │ Fireworks  ░░░║▓▓░░░░║▓▓░░░░░  │   │                        │
│              │  │ Patterns   ░░░░░░░░░░░░░░░░░░  │   │                        │
│              │  │ Entities   ╔═══════╗░░░░░░░░░  │   │                        │
│              │  │ Movement   ░░░░░░░░░░░░░░░░░░  │   │                        │
│              │  │ Cue refs   ╔══════════╗░░╔═══  │   │                        │
│              │  │ Utility    ░░░░░░░░░░░░░░░░░░  │   │                        │
│              │  └────────────────────────────────┘   │                        │
│              │  [◀◀] [▶/⏸] [■]  Tick: 0  ~0.0s      │                        │
│              ├──────────────────────────────────────  │                        │
│              │  SPATIAL VIEW (fireworks canvas)       │                        │
│              │  [2D top-down canvas + Y strip]        │                        │
├──────────────┴──────────────────────────────────────┴─────────────────────── │
│ SHOWSPRITE  [Review this Cue ▶]   [chat input...]                    [Send]  │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Library panel

Left panel. Tabbed: Cues / Shows / Fireworks.

- Assets listed with ID, name, tags, and creator chips
- Search by name, ID, or tag; filter by any tag dimension
- Drag a Cue onto the timeline to place a CUE reference at that tick
- Right-click: duplicate, rename, delete, add tags, preview in-game
- Authorship visible per card

### Timeline

**Lanes:** one per event category; individually shown/hidden. Lane labels on the left.

**Tick ruler:** ticks above, estimated seconds below. Major gridlines configurable (default: every 20t = 1s).

**Zoom:** scroll-wheel zooms the tick axis; middle-click or horizontal scroll to pan.

**Duration events:** colored rectangles from start to end tick. Color-coded by category.

**Firework bars:** launch marker → flight bar (scaled by power) → bloom zone. Colored by first star's primary color.

**Point-in-time markers:** thin vertical bars with category icon.

**Playhead:** draggable. Transport controls: rewind, play/pause, stop. Live tick counter and time display.

**Hybrid navigation:** top two Cue levels expand in place — CUE refs show as labeled blocks, clicking expands inline. Deeper nesting opens the nested Cue as the main timeline view with breadcrumb updated.

### Spatial view

A 2D top-down canvas. Show target player at center. Positioned below the timeline.

**Renders:**
- FIREWORK events: shape tile at computed XZ offset. Y offset shown as numeric label.
- FIREWORK_CIRCLE: ring at defined radius with numbered position tiles. Chase order shown.
- FIREWORK_LINE: line with evenly-spaced numbered tiles.
- FIREWORK_FAN: origin point with arms. Chase numbering continues across arms (or per-arm, per creator toggle).
- Marks: named position labels on the canvas.
- SPAWN_ENTITY positions: entity type icons at their offsets.
- LIGHTNING positions: bolt icons.
- Reference grid: faint 1-block grid, toggleable. Concentric rings at 5-block intervals for scale.

**Firework tile visual language:**
- `BALL` → filled circle (small); `BALL_LARGE` → filled circle (large); `STAR` → five-point polygon; `BURST` → asterisk; `CREEPER` → pixel face icon
- Multiple primary colors: proportional radial segments sized by color count. `["#FFF","#FFF","#FFF","#FFD700"]` = ¾ white, ¼ gold segment. Communicates presence and relative weight of each color.
- **Twinkle:** opacity strobes rapidly during pulse
- **Trail:** directional blur toward show target at pulse time
- **Fade rate:** globally configurable preview preference — not a Minecraft property; the game's firework animation is client-side and cannot be parameterized via the API
- **Chase sequences:** tiles pulse in numbered order, each delayed by `interval_ticks`

**Y side-profile strip:** thin strip below main canvas showing X vs Y (side elevation). Provides height context.

**Direct editing:** drag a tile to update its offset. Drag a circle ring to change radius. Drag a line endpoint to change length. Changes propagate to properties panel and YAML live.

**Playhead sync:** active fireworks highlight as the playhead enters their window.

### Properties panel

Right panel. Context-sensitive — shows fields for the selected event or Cue reference.

- All event fields editable inline
- Enum fields as button groups or dropdowns
- Color fields with picker (swatches + hex)
- **Sound pitch:** vertical slider 0.5–2.0, natural default pitch marked as a notch. Real-time preview via RCON with ~200ms debounce when connected; "Preview at pitch" button when not.
- **Firework power:** segmented toggle `1 | 2 | 3`
- **Firework star editor:** star list (add/remove/reorder). Per star: shape picker (5 icons), primary color swatches with count (proportional segment preview updates live), fade color swatches, trail + flicker toggles.
- **Pattern variation controls:** power variation selector, color variation selector, gradient pickers, rainbow button.
- **Chase mode toggle (fans):** sequential / simultaneous.
- **Tag editor:** tag chips with add/remove from taxonomy suggestions or free-form.

### Preview system

**Tier 1 — Sketch (in-tool, instant).** Canvas animation + browser audio. No server required. ShowSprite uses this tier when proposing a look. Playhead moves; firework tiles pulse in sequence; particle bursts appear; text events flash as canvas overlays.

**Tier 2 — Composer (in-tool + log).** Tier 1 plus a scrolling event log: `🎆 FIREWORK scaena_gold at +0,2,0` / `🎵 SOUND block.bell.use` / etc.

**Tier 3 — Live (in-game via RCON).** The real thing. Requires logged-in builder.

**Sound preview (Tiers 1 and 2):**
CurseForge uses standard Minecraft asset paths:
- macOS: `~/Library/Application Support/minecraft/assets/`
- Windows: `%APPDATA%\.minecraft\assets\`

ScaenaComposer reads the asset index JSON to map sound names to hash paths and serves OGG files from the Node server. Browser plays via Web Audio API. Pitch uses `AudioBuffer.playbackRate` — identical math to Minecraft's pitch parameter (0.5–2.0, 1.0=natural).

**Builder controls (toolbar):** builder username selector (Zarathale / Smitty2020), `▶ Preview Cue`, `▶ Preview Show`, `■ Stop`, RCON status indicator.

### ShowSprite panel

Anchored to the bottom. Persistent chat.

- Streaming responses appear in real time
- "Review this Cue" triggers proactive analysis of the current container
- Tool-call proposals appear as diff cards above the chat — approve or reject each independently
- Show conversation history loaded on session start
- Creator name recorded per message from the configured builder username
- `showsprite.context.md` loaded into system prompt every session

---

## 15. Plugin Runtime

### YAML library
The plugin uses **SnakeYAML directly** (bundled with Paper). Do not use Paper's `FileConfiguration` API — it is not designed for the deeply nested, heterogeneous schema in this plugin (event union types, recursive Cue references, per-event field variation). Use SnakeYAML with a custom deserializer per event type, dispatched on the `type` field.

### Pattern resolution
FIREWORK_CIRCLE, FIREWORK_LINE, FIREWORK_FAN resolved at show runtime. Plugin computes per-position offsets, power, and color from pattern definition. No pre-expansion in YAML.

### Angle convention (FIREWORK_LINE, FIREWORK_FAN)
Angles are **compass/bearing convention** — clockwise from north. 0 = north (−Z), 90 = east (+X), 180 = south (+Z), 270 = west (−X). This is the inverse of standard math/trig angles. Implement as: `radians = (90 - degrees) * PI / 180`. Add a comment to this effect wherever the conversion is written.

### Y surface sampling
For `y_mode: surface`: plugin calls `World.getHighestBlockYAt(x, z)` at event execution time. With `follow` mode, moving targets get fresh terrain sampling continuously. **Surface mode is designed for surface terrain only. Underwater environments (ocean, cave ceilings) will return unexpected values — the highest non-air block at that XZ is the water surface, not the ground. This is a known limitation; do not use surface mode for underwater shows.**

### Safety buffer at show start
Before tick 0 fires, the plugin executes a short internal setup pass (home capture, participant group registration, entity:world targeting scan, group assignment if at tick 0). All setup completes before the first timeline event dispatches. Shows authored in ScaenaComposer always have a minimum of 10 ticks before the first non-setup event, enforced by a composer-side soft warning. Tick 0 events are reserved for setup-class events: GROUP_ASSIGN, CAPTURE_ENTITIES. This eliminates race conditions between setup and early timeline events.

### Cycle detection
At YAML load time, the plugin builds the full Cue dependency graph (following all `type: CUE` references recursively). Any cycle — a Cue that directly or transitively references itself — causes the containing show to **fail to load** with an explicit error naming the cycle: `[ERROR] Show 'show_celebration' contains a Cue cycle: ramp.warm_gold.01 → ramp.warm_gold.01.peak_burst → ramp.warm_gold.01`. Cycle detection runs for every show reload.

### Unknown Cue ID
If a `type: CUE` event references an ID that does not exist in the cues folder, the containing show **fails to load** at reload time with an explicit error: `[ERROR] Show 'show_celebration' references unknown Cue ID: 'ramp.warm_gold.missing'`. Same fail-fast pattern as cycle detection. Shows with unresolvable references do not enter the runtime.

### Execution order for same-tick events
When multiple events share the same `at` tick, the plugin dispatches them in this priority order:
1. `GROUP_ASSIGN` (always first)
2. `CAPTURE_ENTITIES` (targeting setup)
3. All other events in YAML declaration order

### PLAYER_SPECTATE and gamemode
`PLAYER_SPECTATE` temporarily switches the target player to `SPECTATOR` gamemode (required for `player.setSpectatorTarget(entity)` to function). The player's prior gamemode is recorded at the time of the event. On show end, stop, `PLAYER_SPECTATE_END`, or player disconnect, the player is restored to their prior gamemode automatically. This is a sanctioned gamemode change within the show's safe cleanup contract.

### GLOW and TAB conflict
TAB 5.4.0 is confirmed active on ScaenaCraft with scoreboard teams and nametags enabled. TAB manages scoreboard team assignments for all players for prefix/suffix rendering. ScaenaShows GLOW creates short-lived scoreboard teams (`scae_[show_id]_g[n]`) for colored glow. These can conflict: TAB may overwrite the player's team assignment, dropping the glow color.

**Mitigation strategy:** Before implementing GLOW, investigate whether TAB 5.x exposes a developer API to pause team management for specific players during show runtime. If available, use it to suspend TAB team updates for show participants during GLOW events, then re-enable on cleanup. If unavailable, use `Entity.setGlowing(true)` for uncolored glow (white/default), and document colored GLOW as a known best-effort feature. See `SCENA-002` in `issues/open.md`.

### One show per target player
If any named participant already has a running show, `/show play` refuses for the entire invocation: `Cannot start: [player] is already in a running show. Use /show stop <player> first.` This is per-participant, not per-show.

### Pause/resume
When a participant disconnects during a show, the show continues for remaining participants. The disconnected participant's show events targeting them specifically are silently skipped. If the disconnected player is the **spatial anchor** and the show is in `follow` mode, the show switches to `static` mode at the anchor's last known position. The disconnected player may rejoin within `resume_window_seconds` (default 900s); if so, they are re-added as a participant at their reconnect location with a new home capture.

### Stop safety
`/show stop` always: removes levitation, applies slow_falling for 10 seconds, hides show bossbar, restores PLAYER_SPECTATE gamemodes, executes `return_on_end` set teleports, removes all show-scoped scoreboard teams. Cleanup is idempotent — running it twice has no side effects.

### Team lifecycle
GLOW and TEAM_COLOR use a show-scoped team namespace: `scae_[show_id]_g[n]`. Teams created at show start, fully removed at show end (stop, completion, or crash cleanup). Players are re-assigned to their prior team if applicable.

### Home capture
At invocation, before tick 0, the plugin records each participating player's location as their home. Stored in the RunningShow object. Available as `mark:home` or as an offset origin in positional events.

### Do-no-harm defaults
- LIGHTNING uses `strikeLightningEffect()` — no damage, no fire
- No teleport, game mode change, health modification, or inventory clear events in this design
- EFFECT events exclude harmful status effects (wither, poison, instant damage) without an explicit override guard — reserved for future consideration
- PLAYER_TELEPORT with `return_on_end: true` on the referenced set always executes on show end

---

## 16. Open Questions

| # | Status | Question |
|---|---|---|
| 1 | ✅ Closed | RCON: ScaenaComposer file access = local repo checkout; RCON = separate live connection. File workflow: edit → save to local repo → push → SFTP to deploy. |
| 2 | ✅ Closed | Build order: Phase 1 schema+plugin → 2 Node server → 3 basic timeline → 4 spatial view → 5 sketch preview → 6 RCON preview → 7 ShowSprite → 8 library+tags |
| 3 | ✅ Closed | Entity world targeting: display-name scan at show load time; fail fast if >1 match; UUID cached in RunningShow for execution. |
| 4 | Open | Library-building session: schedule to author the seeded starter Cue set after Phase 1 is stable in-game |
| 5 | Open | `showsprite.context.md` initial draft: needed before first ShowSprite session (Phase 7). See `SCENA-003` in issues. |
| 6 | Open | GLOW + TAB API coordination: investigate TAB 5.x developer API for pausing team management per-player during show runtime. See `SCENA-002` in issues. |
| 7 | ✅ Closed | Unknown Cue ID reference: fail at load time. Error names the show and the missing ID. Same pattern as cycle detection. |
| 8 | ✅ Closed | Empty group behavior: silent skip. Events targeting an empty group execute with zero targets — no error, no log noise. |

---

## 18. Pattern Event Architecture

### Overview

A Pattern is a first-class event type in the ScaenaShows YAML schema. It defines an interpolated or pulsed sequence of events as a single authored unit. The engine expands a Pattern to N individual events at show-load time via `PatternExpander`. The author and Phase 2 editor work with the Pattern; the scheduler works with expanded events.

**Execution invariant:** Patterns are a parse-time concept only. `ShowScheduler`, `ExecutorRegistry`, and `RunningShow` never see Patterns — they receive only expanded individual events. The production execution path is unchanged.

`TechCueSession.raw_yaml` stores Patterns in unexpanded form. Expansion happens fresh each time `enterPreview()` is called. Raw YAML is always what was authored; RunningShow is always what was expanded from it.

---

### Behavioral modes

**Fade-type Pattern** — any param where `start ≠ end`: interpolates one or more params from start to end across N steps. Examples: volume fade, pitch glissando, time-of-day transition.

**Pulse-type Pattern** — all params where `start == end`: repeats a fixed config N times at calibrated cycle timing. Example: levitation HOVER/CLIMB/RELEASE cycles.

---

### Pattern field set

| Field | Required | Description |
|---|---|---|
| `type` | Yes | `SOUND_PATTERN` \| `EFFECT_PATTERN` \| `TIME_OF_DAY_PATTERN` |
| `interpolations` | Yes | Map of `param: {start, end, curve}` — one entry per interpolated param |
| `steps` | Yes | Number of events to distribute |
| `total_duration` | Yes | Total tick length of the Pattern |
| `curve` | No | Pattern-level default curve: `linear` (default) \| `ease_in` \| `ease_out` — overridden per-param |
| `step_duration` | No | For event types with internal duration (EFFECT): how long each step event lasts in ticks |
| `gap` | No | Ticks between step end and next step start. Interval = step_duration + gap. |

Plus all event-type-specific fields for the base event (e.g., `sound_id:` for SOUND_PATTERN).

**Step spacing:** `step_spacing = total_duration / (steps - 1)`. Must be an integer — non-integer spacing causes rounding drift. Phase 2 panel warns when non-integer spacing is detected and suggests corrected values.

---

### Per-param curve options

| Curve | Behavior | Use for |
|---|---|---|
| `linear` | Equal arithmetic steps (default) | Volume, amplifier, time |
| `ease_in` | Slow start, accelerating change | Swells, tension builds |
| `ease_out` | Fast start, decelerating change | Release, fading out |

Note: `equal_temperament` curve is MUSIC_PATTERN only — see §20.

---

### Phase 2 Pattern types

| Type | Department | Interpolatable params | Primary use |
|---|---|---|---|
| `SOUND_PATTERN` | Sound | `pitch`, `volume` | Simulated fade, crescendo |
| `EFFECT_PATTERN` | Effects | `amplifier` | Levitation cycles |
| `TIME_OF_DAY_PATTERN` | Lighting | `time` | Gradual time-of-day transition |

Additional Pattern types are deferred until a concrete show need drives them.

---

### EFFECT_PATTERN levitation presets

The three calibrated levitation patterns are named presets in `effect-configs.yml`:

| Preset ID | step_duration | gap | interval | Confirmed behavior |
|---|---|---|---|---|
| `effects.levitate.hover` | 20t | 8t | 28t | Gentle altitude hold |
| `effects.levitate.climb` | 24t | 0t | 24t | Gradual upward drift |
| `effects.levitate.release` | 20t | 24t | 44t | Slow descent |

---

### Examples

**Simulated volume fade:**
```yaml
type: SOUND_PATTERN
sound_id: minecraft:block.note_block.harp
steps: 6
total_duration: 120
interpolations:
  volume:
    start: 0.9
    end: 0.1
    curve: linear
```

**Levitation hover cycle (pulse-type):**
```yaml
type: EFFECT_PATTERN
effect_id: levitation
steps: 4
total_duration: 112    # 4 × 28t interval
step_duration: 20
gap: 8
interpolations:
  amplifier:
    start: 0
    end: 0             # pulse-type — start == end
```

---

### PATTERN vs. PHRASE — when to use each

| Content | Primitive | Reasoning |
|---|---|---|
| Volume fade | PATTERN | Single param interpolated |
| Levitation cycle | PATTERN | Repeating pulse |
| Time-of-day transition | PATTERN | Time param interpolated |
| Harp glissando | MUSIC_PATTERN | Pitch computed via equal_temperament (see §20) |
| Explicit melody or riff | PHRASE | Each note intentional — not computable |
| Firework salvo | PHRASE | Each burst explicitly placed |
| Levitation hover (calibrated) | EFFECT_PATTERN preset | Named preset |

---

## 19. PHRASE Event Architecture

### Overview

A PHRASE is a first-class event type in the ScaenaShows YAML schema — the explicit sequence counterpart to PATTERN. Where PATTERN computes its steps (interpolated from start/end), PHRASE contains explicitly authored steps: each event is intentional. The engine expands a PHRASE to individual events at show-load time via `PhraseExpander`. Scheduler and executors see only the expanded events.

**Cross-department:** PHRASE steps can include any event type from any department. There is no department restriction on step content. `PHRASE` is the single unified schema type — names such as "voice phrase" or "camera phrase" are authoring-convention labels only, not distinct YAML types.

`TechCueSession.raw_yaml` stores PHRASEs in unexpanded form. `PhraseExpander` runs at `enterPreview()`.

---

### PHRASE field set

| Field | Required | Description |
|---|---|---|
| `type` | Yes | `PHRASE` |
| `name` | Yes | Phrase identifier |
| `anchor` | No | `scene_origin` (default) \| `player` — spatial anchor for spatially-anchored phrases |
| `tempo_bpm` | No | Enables beat-based step addressing (`at_beat:`). Converted to ticks internally — may approximate for non-anchor BPM values. See §21. |
| `ticks_per_quarter` | No | Alternative to `tempo_bpm`. Always exact — no rounding. Mutually exclusive; takes precedence if both present. |
| `subdivision` | No | Smallest rhythmic unit in the beat grid: `4` = quarter, `8` = eighth (default), `16` = sixteenth. Subdivisions producing fractional ticks are unavailable in the Phase 2 panel. |
| `steps` | Yes | Ordered list of step entries |

**Step entry fields:**

| Field | Required | Description |
|---|---|---|
| `at` | Yes (if no tempo) | Step offset in ticks from PHRASE start |
| `at_beat` | Yes (if tempo set) | Step offset in beats (e.g., `1.5` = one eighth note after beat 1) |
| `events` | Yes | Array of one or more event configs — multiple entries = vertical grouping (fire simultaneously) |

---

### Vertical grouping

A step with multiple entries in `events:` fires all of them at the same tick. This is the mechanism for chords, volleys, and any multi-event simultaneous action. No special keyword required — the array length is the signal.

```yaml
# Single event
- at_beat: 1.0
  events:
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.0, volume: 0.8}

# Three events simultaneously (vertical grouping)
- at_beat: 2.0
  events:
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.0, volume: 0.7}
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.26, volume: 0.6}
    - {type: SOUND, sound_id: minecraft:block.note_block.harp, pitch: 1.5, volume: 0.5}
```

---

### Department vocabulary

| Department | Single event | Vertical grouping | Container |
|---|---|---|---|
| Sound | Note | Chord | Phrase |
| Fireworks | Burst | Volley | Salvo |
| Effects | Pulse | Cluster | Phrase |
| General | Event | Grouping | Phrase |

Vocabulary is documentation and UI language only — the YAML structure is identical across departments.

---

### Examples

**Cross-department PHRASE — voice line + sound hit + choreography simultaneous:**
```yaml
type: PHRASE
name: "arrival_sequence"
ticks_per_quarter: 12
steps:
  - at: 0
    events:
      - {type: TITLE, title: "<gold>Arise.</gold>", fade_in: 10, stay: 40, fade_out: 10}
      - {type: SOUND, sound_id: minecraft:block.bell.use, volume: 1.0, pitch: 1.0}
  - at: 20
    events:
      - {type: FACE, target: entity:spawned:Herald, look_at: player}
      - {type: ENTITY_AI, target: entity:spawned:Herald, enabled: false}
```

**Fireworks salvo:**
```yaml
type: PHRASE
name: "victory_salvo"
tempo_bpm: 80
steps:
  - at_beat: 1.0
    events:
      - {type: FIREWORK, preset_id: fireworks.burst.gold.high}
  - at_beat: 2.0
    events:
      - {type: FIREWORK, preset_id: fireworks.burst.gold.high}
      - {type: FIREWORK, preset_id: fireworks.burst.silver.mid}
      - {type: FIREWORK, preset_id: fireworks.burst.red.low}
  - at_beat: 3.0
    events:
      - {type: FIREWORK, preset_id: fireworks.burst.white.finale}
```

---

### Phase 2 PHRASE builder

Phase 2 displays PHRASEs as expandable groups in the cue panel. Each step is editable.

**Step builder UI:** When adding or editing a step event, a two-level picker opens:
1. Department picker (10 options — the production team roster)
2. Event type picker (event types available in that department)
3. Event panel opens for that type's fields

Multiple event slots per step (vertical grouping). Each slot has a **Change** action that re-opens the department/event type picker; fields clear on change. Phase 2 entry points (e.g., entering from the Voice department panel) pre-select that department for the first step but impose no restriction on subsequent steps.

---

## 20. MUSIC Event Type

### Overview

MUSIC is a first-class event type distinct from SOUND. Both live in the Sound department. The distinction is architectural:

- **SOUND** — any Minecraft sound ID; `pitch:` is a continuous multiplier (0.5–2.0) applied to the sound's natural pitch. General sound effects, ambient beds, point hits.
- **MUSIC** — one noteblock instrument per event or sequence; `pitch:` expressed in musical notation (note names: `A4`, `F#3`, `C5`) resolving to the chromatic scale. Produces actual musical intervals.

**Parallel consideration principle:** Changes to MUSIC type warrant explicit consideration of SOUND for parallel applicability, and vice versa. Sister types.

MUSIC has four forms: `MUSIC` (single event), `MUSIC_PATTERN` (interpolated sweep), `MUSIC_CYCLE` (arpeggiator), `MUSIC_PHRASE` (explicit authored sequence).

---

### Instrument shorthand

The `instrument:` field takes the last segment of the Minecraft note block sound ID.

| Shorthand | Resolves to | Register | Character |
|---|---|---|---|
| `harp` | `minecraft:block.note_block.harp` | Full range | Warm pluck — general purpose |
| `bell` | `minecraft:block.note_block.bell` | Mid-high | Ceremony, clarity, arrival |
| `flute` | `minecraft:block.note_block.flute` | Mid-high | Longing, pastoral, distance |
| `chime` | `minecraft:block.note_block.chime` | High | Wonder, delicacy, uncanny |
| `xylophone` | `minecraft:block.note_block.xylophone` | Mid-high | Joy, lightness, movement |
| `bit` | `minecraft:block.note_block.bit` | Full range | Digital, synthetic, retro |
| `pling` | `minecraft:block.note_block.pling` | Mid | Contemplative, jazz, interior |
| `iron_xylophone` | `minecraft:block.note_block.iron_xylophone` | Mid | Precise, mechanical |
| `guitar` | `minecraft:block.note_block.guitar` | Low-mid | Earthiness, folk, comfort |
| `banjo` | `minecraft:block.note_block.banjo` | Mid | Folk warmth, community |
| `bass` | `minecraft:block.note_block.bass` | Low | Foundation, grounding, weight |
| `didgeridoo` | `minecraft:block.note_block.didgeridoo` | Low | Earth, ancient, primal |
| `basedrum` | `minecraft:block.note_block.basedrum` | Percussion | Rhythmic downbeat |
| `snare` | `minecraft:block.note_block.snare` | Percussion | Accent, punctuation |
| `hat` | `minecraft:block.note_block.hat` | Percussion | Fine rhythmic texture |
| `cow_bell` | `minecraft:block.note_block.cow_bell` | Percussion | Quirky emphasis |

Percussion instruments (`basedrum`, `snare`, `hat`, `cow_bell`): `pitch:` is optional (defaults to `1.0`).

---

### Pitch notation

The `pitch:` field accepts either:
- **Note name** (authoring default): `A4`, `F#3`, `C5`, `Bb4`, `D#4` — resolves via the chromatic scale. Note names are preferred for authoring clarity.
- **Float value** (compatibility / precision): `1.189` — used directly.

Both forms are valid anywhere pitch appears in MUSIC types. The engine resolves names to floats at parse time.

**Two-octave range:** F#3 (0.500) to F#5 (2.000). 25 discrete semitone positions.

`pitch = 2^((semitone - 12) / 12)` where semitone 0 = F#3, 12 = F#4 (natural), 24 = F#5.

---

### Octave fold

When a computed or authored pitch falls outside [0.5, 2.0], the engine folds it to the nearest in-range octave: multiply or divide by 2 until in range. Note name is preserved; register shifts. This is always-on — no field or flag. Applies to all four MUSIC forms.

**Register displacement:** When fold changes the interval relationship between two notes (e.g., a fifth folds to a fourth), this is musically intentional when authoring near range boundaries. The Phase 2 panel shows the actual folded pitch alongside the authored value.

---

### MUSIC (single event)

```yaml
type: MUSIC
instrument: harp
pitch: A4                  # note name or float — resolves to 1.189
volume: 0.8
category: master           # same channel discipline as SOUND
max_duration_ticks: 40     # optional — hard cut, same behavior as SOUND
```

All fields parallel SOUND except `instrument:` replaces `sound_id:`, and `pitch:` accepts note names.

---

### MUSIC_PATTERN (interpolated sweep)

Generates an interpolated series of MUSIC events from start to end pitch. Uses the `equal_temperament` curve for pitch interpolation — this is the default and strongly recommended for pitch sweeps. Linear pitch spacing sounds musically uneven and should not be used.

**`equal_temperament` math:** PatternExpander multiplies the previous value by `(end/start)^(1/(steps-1))` at each step. This produces semitone-spaced values — an even musical glide.

```yaml
type: MUSIC_PATTERN
instrument: harp
category: master
steps: 13
total_duration: 130
interpolations:
  pitch:
    start: F#3             # note name — resolves to 0.500
    end: F#5               # note name — resolves to 2.000
    curve: equal_temperament
  volume:
    start: 0.3
    end: 1.0
    curve: ease_in
```

Field set: identical to SOUND_PATTERN (`interpolations:`, `steps:`, `total_duration:`, `curve:`, `step_duration:`, `gap:`) plus `instrument:` replacing `sound_id:`, and note names accepted in `interpolations.pitch.start` / `end`.

Note: `equal_temperament` is MUSIC_PATTERN only. SOUND_PATTERN uses `linear`, `ease_in`, `ease_out` for volume and non-musical pitch manipulation.

---

### MUSIC_CYCLE (arpeggiator)

Generates a repeating sequence of MUSIC events by cycling through a named or explicit interval pattern from a declared root.

```yaml
type: MUSIC_CYCLE
instrument: harp
category: master
root: Ab3                  # transposable anchor — note name or float
pattern: maj6_9            # named preset or explicit interval array
harpify: true              # false = unique pitches only; true = full 7-string pedal tuning
cycles: 3
step_duration: 15          # ticks per note
volume: 0.8                # scalar or envelope: {start, end, curve}
```

**`pattern:`** accepts a named preset string or an explicit semitone interval array: `[0, 4, 7, 9]`. Descending figures use negative intervals: `[0, -2, -4, -5]`.

**`root:`** transposes the entire pattern. Change root, the pattern follows.

**`harpify: false`** (default) — unique pitch classes only. Use for clean chord arpeggios, melodic figures, bass lines. Works on any instrument.

**`harpify: true`** — full 7-string-per-octave pedal tuning. One diatonic letter (C D E F G A B) assigned to match the scale. Wherever two adjacent strings produce the same pitch (enharmonic pair), both steps fire in sequence — the doubled pitch sounds twice. Doublings create texture, shimmer, and rhythmic emphasis. Most idiomatic on harp.

**Volume envelope:** `volume:` accepts either a scalar (`0.8`) or an envelope map with `start:`, `end:`, and `curve:`.

Fold applies to every computed step.

---

#### Named Pattern Library

All patterns are root-relative semitone interval arrays.

**Family 1 — Scales and Modes** (7 unique pitches; harpify has no effect)

| Pattern | Intervals | Character |
|---|---|---|
| `major` | [0,2,4,5,7,9,11] | Ionian |
| `natural_minor` | [0,2,3,5,7,8,10] | Aeolian |
| `harmonic_minor` | [0,2,3,5,7,8,11] | Raised 7th — tension + resolution |
| `melodic_minor` | [0,2,3,5,7,9,11] | Ascending — jazz, lyrical |
| `chromatic` | [0,1,2,3,4,5,6,7,8,9,10,11,12] | All semitones |
| `dorian` | [0,2,3,5,7,9,10] | Minor with bright 6th |
| `phrygian` | [0,1,3,5,7,8,10] | Dark, Spanish gravity |
| `lydian` | [0,2,4,6,7,9,11] | Raised 4th — otherworldly |
| `mixolydian` | [0,2,4,5,7,9,10] | Flat 7th — folk warmth |
| `locrian` | [0,1,3,5,6,8,10] | Diminished root — unstable |

**Family 2 — Whole-Tone** (6 unique pitches; 1 harp doubling)

| Pattern | Intervals | Notes |
|---|---|---|
| `whole_tone` | [0,2,4,6,8,10] | Floating, directionless (harpify: false) |
| `whole_tone_bc` | [0,2,4,6,8,10,12] | Doubling at top — cycle lands on repeated pitch (harpify: true) |
| `whole_tone_bcb` | [0,0,2,4,6,8,10] | Doubling at bottom — cycle launches from repeated pitch (harpify: true) |

**Family 3 — Pentatonic** (5 unique pitches; 2 harp doublings)

| Pattern | harpify: false | harpify: true | Character |
|---|---|---|---|
| `pentatonic_major` | [0,2,4,7,9] | [0,2,4,4,7,9,12] | Open, folk warmth |
| `pentatonic_minor` | [0,3,5,7,10] | [0,3,3,5,7,10,10] | Dark warmth |

**Family 4 — Chord Arpeggios** (harpify: false is natural default)

| Pattern | Intervals | Character |
|---|---|---|
| `major_triad` | [0,4,7] | Bright, resolved |
| `minor_triad` | [0,3,7] | Shadow, interiority |
| `sus2` | [0,2,7] | Open, floating |
| `sus4` | [0,5,7] | Tension without resolution |
| `dom_7th` | [0,4,7,10] | Wants resolution |
| `major_7th` | [0,4,7,11] | Lush, complex rest |
| `minor_7th` | [0,3,7,10] | Dark-warm, jazz |
| `dim_triad` | [0,3,6] | Instability, dread |
| `aug_triad` | [0,4,8] | Uneasy symmetry |
| `six_nine` | [0,2,4,7,9] | Open, no 4th or 7th |
| `maj6_9` | [0,4,7,9] | Lush, shimmering — defining harp voicing |
| `min6_9` | [0,3,7,9] | Darker resonance, modal depth |

**Family 5 — Motion Patterns**

| Pattern | Intervals | Character |
|---|---|---|
| `I_V` | [0,7] | Tonic-dominant bass pump |
| `major_arch` | [0,4,7,4,0] | Rise and return — major |
| `minor_arch` | [0,3,7,3,0] | Rise and return — minor |
| `do_ti_la_sol` | [0,-1,-3,-5] | Descending major — gravity, arrival |
| `pendulum` | [0,7,12,7] | Tonic-fifth-octave-fifth — open motion |
| `pedal` | [0,0,0,0] | Repeated root — insistence, pulse |

---

### MUSIC_PHRASE (explicit authored sequence)

Single-instrument authored sequence. Instrument and category declared once at the top; steps need only `pitch:` and `volume:`.

```yaml
type: MUSIC_PHRASE
instrument: harp
category: master
ticks_per_quarter: 12
subdivision: 8
steps:
  - at_beat: 1.0
    pitch: A4              # shorthand — single-note step
    volume: 0.8
  - at_beat: 1.5
    pitch: B4
    volume: 0.8
  - at_beat: 2.0           # chord — events array for vertical grouping
    events:
      - {pitch: A4, volume: 0.7}
      - {pitch: C5, volume: 0.6}
      - {pitch: E5, volume: 0.5}
  - at_beat: 3.0           # exception harmony — melody note + added interval
    events:
      - {pitch: C#5, volume: 0.8}
      - {pitch: A4,  volume: 0.6}
```

**Shorthand form:** `pitch:` + `volume:` at step level = single-event step. Any step can use `events:` instead.

**Exception harmonies:** `events:` array accepts any number of notes. The instrument declared at the top applies to all. This is how you add a doubled octave, sixth, or any interval to a specific step of an otherwise single-line melody.

**Fold on exception harmonies:** Same fold rules apply. Phase 2 panel shows folded pitch alongside authored value.

**Range note:** Doubling at the octave is only safe if the root note is in the lower register. Safe harmony intervals in the upper register: sixth (9 semitones), fifth (7), third (3–4).

**Existing motif cues:** `motif.*` and `gracie.*` cues remain as valid, tested authored content. MUSIC_PHRASE is the authoring primitive for new musical content going forward. Migration tracked as OPS-035.

---

## 21. Tempo Architecture

### Guiding principle: tick-first design

Minecraft's simulation clock runs at exactly 20 ticks per second. All timing in ScaenaShows resolves to whole tick offsets. BPM is a derived, human-readable label — not the primary quantity.

**Design for coherence over precision:** A tempo anchor that produces perfect integer subdivisions will feel better in play than a mathematically precise BPM that forces fractional tick rounding. Subdivision integrity is more important than BPM accuracy.

---

### Primary conversion

```
ticks_per_quarter = 1200 / BPM
BPM ≈ 1200 / ticks_per_quarter
```

Ticks are integers. BPM is frequently irrational. Choose `ticks_per_quarter` first; accept the resulting BPM as the "musical equivalent."

---

### Preferred tempo anchors

| ticks_per_quarter | Approx BPM | Character |
|---|---|---|
| 24 | 50 | Spacious, atmospheric |
| 20 | 60 | Slow, ceremonial |
| 16 | 75 | Lyrical, flowing |
| 15 | 80 | Moderate, flexible |
| **12** | **100** | **Highly versatile — recommended default** |
| 10 | 120 | Energetic, standard |
| 8 | 150 | Bright, rhythmic clarity |
| 6 | 200 | Pulse-driven, grid-heavy |

**Default recommendation:** 12 ticks/quarter. It is the only standard anchor where triplets produce exact integers, and it cleanly supports every common rhythmic structure.

---

### 12 ticks/quarter — full subdivision table

| Note value | Ticks | Status |
|---|---|---|
| Whole | 48 | exact |
| Half | 24 | exact |
| Quarter | 12 | exact |
| Eighth | 6 | exact |
| Sixteenth | 3 | exact |
| Dotted quarter | 18 | exact |
| Dotted eighth | 9 | exact |
| Triplet (quarter) | 8 | exact |
| Triplet (eighth) | 4 | exact |

---

### `ticks_per_quarter` as a PHRASE / MUSIC_PHRASE field

`ticks_per_quarter:` is an alternative to `tempo_bpm:` on any PHRASE or MUSIC_PHRASE. When specified, tick math is exact with no rounding. The two fields are mutually exclusive; `ticks_per_quarter` takes precedence if both are present (error logged). Phase 2 panel shows both representations and allows switching entry mode.

```yaml
# BPM form — familiar, may approximate at non-anchor values
type: MUSIC_PHRASE
instrument: harp
tempo_bpm: 100         # internally converts to 12 ticks/quarter — exact here

# Tick-first form — always exact
type: MUSIC_PHRASE
instrument: harp
ticks_per_quarter: 12  # declared directly — no conversion, no rounding
```

---

### PATTERN quantization rule

When PatternExpander generates N events over `total_duration` ticks:

```
step_spacing = total_duration / (steps - 1)
```

Step spacing must be an integer. If fractional, PatternExpander rounds each step tick to the nearest whole tick — small drift accumulates at the phrase end. Phase 2 panel warns when fractional spacing is detected and suggests the nearest `steps` or `total_duration` value that produces integer spacing.

---

### Loop integrity

For PHRASEs and PATTERNs intended to loop, total length in ticks should be divisible by a preferred loop unit:

| Loop unit (ticks) | Equivalent at 12t/q | Notes |
|---|---|---|
| 12 | 1 quarter | minimum stable loop unit |
| 24 | 1 half note | |
| 48 | 1 bar (4/4) | standard loop unit |
| 96 | 2 bars | |
| 192 | 4 bars | preferred for repeating content |

Phase 2 panel shows a loop-alignment indicator: green if divisible by 48 (1 bar at 12t/q), yellow if divisible by 12 (sub-bar clean), red if neither.

---

## 22. Preset Library

### Overview

The preset library is a collection of named, reusable configurations referenced by ID from events in cues and shows. Each department maintains its own preset file. The plugin loads all preset files at startup and indexes presets by ID — presets are immediately available to any show without per-session loading.

`fireworks.yml` is the existing model. All department preset files follow the same structure.

---

### Universal file format

```yaml
# src/main/resources/[department].yml
# [Department] presets — referenced by ID from any [DEPT_EVENT_TYPE] event.

presets:

  preset.subtype.slug:
    display_name: "Human-readable label"
    # preset-specific fields...

  another.preset.slug:
    display_name: "Another preset"
    # ...
```

**Top-level `presets:` map.** Each key is a preset ID; each value is the preset's field set. The plugin indexes the full map at startup. Unknown preset IDs referenced from show/cue YAML fail at load time (same contract as unknown CUE IDs).

---

### Preset ID naming convention

`[department].[subtype].[slug]`

Examples: `effects.levitate.hover`, `camera.phrase.reveal_tilt`, `voice.phrase.sprite_intro`, `fireworks.circle.tight_ring`, `sound.fade.slow_out`.

---

### Department preset files

All files live in `src/main/resources/`.

| Department | File | Phase 2 | What it holds |
|---|---|---|---|
| Fireworks | `fireworks.yml` ✅ exists | Yes | Rocket appearance presets; FIREWORK_PATTERN presets |
| Effects | `effects.yml` | Yes | EFFECT_PATTERN presets (levitate.hover, levitate.climb, levitate.release — see §18) |
| Camera | `camera.yml` | Yes | CAMERA_PHRASE presets (reveal_tilt, battle_sweep, etc.) |
| Voice | `voice.yml` | Yes | VOICE_PHRASE presets (sprite_intro, revelation, section_close) |
| Sound | `sound.yml` | Yes | SOUND_PATTERN presets (named fades, crescendi) |
| Lighting | `lighting.yml` | Yes | TIME_OF_DAY_PATTERN presets; named time-of-day snap values |
| Wardrobe | `wardrobe.yml` | Later | Named equipment sets |
| Set | `set.yml` | Later | Block configuration presets |
| Casting | `casting.yml` | Later | Named entity cast configurations |
| Choreography | `choreography.yml` | Later | Formation presets (post-calibration) |

Phase 2 ships the six "Yes" files. The remaining four are created when those departments require Phase 2 preset support.

---

### MUSIC named patterns

The MUSIC_CYCLE named pattern library (maj6_9, whole_tone_bc, chromatic, etc. — see §20) lives in Java as a built-in registry, not in a YAML file. These are algorithmic presets whose values are constants. MUSIC_PHRASE presets (authored sequences) are authored content and belong in `sound.yml` if and when named presets are needed.

---

### ShowYamlEditor integration

`ShowYamlEditor` reads each Phase 2 preset file at startup and makes the indexed presets available to department panels as dropdown/picker options. Phase 2 panels that reference presets (e.g., EFFECT_PATTERN panel, CAMERA_PHRASE panel) pull their picker options from these loaded maps. Presets authored during a TechSession via `[Save as Preset]` are written back to the appropriate file and the index is refreshed.

---

## 17. Vocabulary Reference

| Term | Meaning |
|---|---|
| **Cue** | The universal recursive container. Holds events and/or child Cue references. |
| **Show** | A Cue with show-level metadata (mode, groups, marks, sets, bossbar). Runtime entry point. |
| **Event** | A leaf action on a Cue's timeline. |
| **Pattern** | A parse-time generative primitive. Defines an interpolated or pulsed sequence of events (start, end, steps, curve). Expanded to individual events at show-load time by PatternExpander. Types: SOUND_PATTERN, EFFECT_PATTERN, TIME_OF_DAY_PATTERN, MUSIC_PATTERN, FIREWORK_PATTERN. See §18, §20. |
| **PHRASE** | A parse-time generative primitive. An explicitly authored sequence of steps, each at a tick or beat offset, each containing one or more events of any type from any department. Expanded at show-load time by PhraseExpander. See §19. |
| **MUSIC** | A noteblock-instrument event type distinct from SOUND. Pitch expressed in musical notation (note names). Four forms: MUSIC, MUSIC_PATTERN, MUSIC_CYCLE, MUSIC_PHRASE. See §20. |
| **Preset** | A named firework configuration (power + stars). Referenced by events and patterns. |
| **Star** | One firework explosion layer within a preset. Multiple stars detonate simultaneously. |
| **Chase** | Sequential firing of pattern positions at a defined tick interval. FL or LF direction. |
| **Group** | A set of players auto-assigned at show start. Enables per-group effects. |
| **Home** | A participant's captured world location at show invocation. Runtime value. |
| **Mark** | A named XZ offset position in the show's coordinate space. |
| **Set** | A named world-specific location used with PLAYER_TELEPORT. |
| **Portable** | A show whose effects are relative to the target and can run in any world. |
| **REST** | A silence block. Duration without events. |
| **Puppet** | An entity with AI disabled — under full show control. |
| **Chorus** | A named group of entities acting as visual ensemble. |
| **Cast member** | Any entity (player or mob) participating in a show. |
| **Set piece** | Armor stand or display entity used as a static prop. |
| **Snapshot capture** | CAPTURE_ENTITIES mode where the group is fixed at the moment of capture. |
| **Live capture** | CAPTURE_ENTITIES mode where the group re-sweeps each time a targeting event fires. |
| **ScaenaComposer** | The local authoring tool (Node.js server + browser UI). |
| **ShowSprite** | The AI creative assistant embedded in ScaenaComposer. It/they. |
| **showsprite.context.md** | Shared vocabulary and creator profiles. Loaded into ShowSprite's system prompt every session. |
