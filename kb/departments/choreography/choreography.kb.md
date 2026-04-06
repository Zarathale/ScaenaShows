---
department: Choreographer / Movement Director
owner: Sharon
kb_version: 2.1
updated: 2026-03-27
notes: >
  v2.0: Full instrument inventory with Java verification. NPC lifecycle (spawn, AI state,
  movement, despawn), entity type movement notes, cue naming convention, tone translation,
  cross-department collaboration, and capability gaps with workarounds.
  v2.1: Sharon named head introduction added. Three gap statuses corrected from "should be filed"
  to "Filed in ops-inbox.md" (ENTITY_SPEED group, ENTER equipment, RETURN_HOME non-player).
  ENTER gap inline note updated. Capability status FACE-pitch row corrected to Gapped.
  Folder migration to kb/departments/choreography/choreography.kb.md.
---

# Choreographer / Movement Director — Technical Knowledgebase

> Technical reference for the Choreography department. Documents what the ScaenaShows Java plugin
> can do for cast and performer movement — entities, NPCs, participant cast members — and how to
> access those capabilities through YAML.
>
> **Scope:** This department owns movement of performers and cast entities — spawned NPCs,
> captured world-resident entities, and participant cast members. It does NOT own forced
> movement of the show's target player (levitation, teleportation, velocity impulses applied to
> the target) — those belong to the Effects department. See `kb/departments/effects.kb.md`.
>
> Creative direction for this role lives in `kb/production-team.md §3. Choreographer / Movement Director`.

---

## Sharon

Sharon runs the Choreography department with a director's eye for spatial composition and a stager's
precision about timing. Before she places a single performer, she knows the full cast list from
Casting and each entity type's natural movement behavior — she never fights the creature's nature
when she can work with it. Her first question on any show is: at tick 120, where is everyone on
stage? The answer to that question, repeated at every major beat, is her movement score.

Sharon thinks in puppets by default and earns every moment of performer freedom. She enables AI
selectively — a wolf that actually runs is more compelling than a scripted imitation of one, but
only when the show is designed to hold the wildness. She writes movement for open terrain and named
marks, never inline offsets.

The Choreographer coordinates and escalates through the Show Director when a cross decision requires
another department to fundamentally change their beat — for example, when a performer's arrival at
a key moment requires Sound to shift a climax cue. She brings the analysis: what the conflict is,
what the options are, and which one Choreography prefers.

---

## Role Summary

- Owns all spawning, despawning, and lifecycle management of show cast members (NPCs and entities)
- Owns all movement of cast members: scripted crosses, entrances, exits, holds, orientation
- Owns AI state — the puppet/performer decision for every entity in the show
- Coordinates with Casting on *who* is on stage; Choreography decides *where and when* they move
- Coordinates with Wardrobe on what cast members wear (Wardrobe owns equipment; Choreography owns when they appear and move)
- Does NOT own the target player's forced movement — that is Effects territory

---

## NPC Management — The Core Instruments

Before a performer can cross or hold, they must exist. The Choreography department manages the full NPC lifecycle: spawn, control, move, despawn.

---

### Instrument: Bringing a Performer On Stage

**Java grounding:** SPAWN_ENTITY, ENTER

**What it does:** Places a named entity in the world at a specific position, registers it with the show (so it can be targeted by name), and optionally sets its AI state, baby variant, and equipment.

**SPAWN_ENTITY — full placement control:**
```yaml
type: SPAWN_ENTITY
entity_type: VILLAGER         # any Bukkit EntityType — see Casting KB for dramatic register
name: "Herald"                # required for targeting; registers as entity:spawned:Herald
offset: {x: 0, y: 0, z: 0}  # relative to spatial anchor
baby: false
despawn_on_end: true          # ALWAYS true unless the entity is a permanent world fixture
equipment:                    # Wardrobe department values; see wardrobe.kb.md
  main_hand: TORCH
  helmet: IRON_HELMET
```

**ENTER — semantic shorthand for arrivals:**
```yaml
type: ENTER
entity_type: WOLF
name: "Companion"
from: mark:wing_left          # spawns here (off-stage)
destination: mark:stage_left  # pathfinds to here on spawn
baby: false
despawn_on_end: true
```

> ⚠️ **ENTER gap:** Equipment fields are not applied in ENTER (unlike SPAWN_ENTITY). If a
> performer needs to enter with equipment, use SPAWN_ENTITY at the wing mark + CROSS_TO
> instead. This is not documented in cue-show-yaml-schema.md — confirmed by reading EntityEventExecutor.java
> vs. StageEventExecutor.handleEnter(). Filed in ops-inbox.md (2026-03-25).

**Choosing between SPAWN_ENTITY and ENTER:**
- Use SPAWN_ENTITY when you need equipment at spawn, need precise spawn Y, or when the entity starts on stage rather than arriving from a wing
- Use ENTER when the semantic of "someone is arriving" is clear, equipment isn't needed, and the from/destination model is sufficient

---

### Instrument: Puppet vs. Performer — AI State

**Java grounding:** ENTITY_AI

**What it does:** Switches a mob's AI on or off. The most important creative decision the Choreographer makes about any cast member.

```yaml
type: ENTITY_AI
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:groupname
enabled: false                  # false = puppet | true = performer
```

**The creative choice:**

| Mode | `enabled:` | What happens | Use when |
|------|-----------|--------------|----------|
| **Puppet** | `false` | Entity holds position; responds to CROSS_TO (instant), FACE, HOLD | You need precise blocking — marks, timing, spatial storytelling |
| **Performer** | `true` | Entity's natural Minecraft AI resumes — wanders, reacts, looks at things | You want genuine creature behavior as atmosphere |
| **Guided** | `true` + CROSS_TO | Pathfinder moves toward mark; arrival not guaranteed | Entrances, exits, dramatic approaches |

**Design principle:** Default to puppet. Enable AI selectively for moments where genuine wildness serves the story — a wolf that actually runs, an allay that genuinely follows, a bat chorus that genuinely swarms. Don't enable AI on anything dangerous (Creeper, Warden) near a player.

**Re-enabling AI after puppet use:**
```yaml
# Disable AI at spawn to place precisely
- at: 0
  type: SPAWN_ENTITY
  entity_type: IRON_GOLEM
  name: "Sentinel"
  offset: {x: 0, y: 0, z: 5}
  despawn_on_end: true

- at: 0
  type: ENTITY_AI
  target: entity:spawned:Sentinel
  enabled: false

# Later, release the golem into performer mode for a dramatic moment
- at: 240
  type: ENTITY_AI
  target: entity:spawned:Sentinel
  enabled: true
```

**ENTITY_AI only works on Mob subtypes.** Armor Stands and Display Entities have no AI — they are always puppets regardless of this event. Confirmed in EntityEventExecutor.java: `if (entity instanceof Mob mob) { mob.setAI(e.enabled); }`

---

### Instrument: The Cross — Moving a Performer

**Java grounding:** CROSS_TO

**What it does:** Moves a named entity (or group) from their current position to a destination mark or offset. With `duration_ticks > 0`, movement is gradual.

```yaml
type: CROSS_TO
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:groupname | group_1 | group_2 | player
destination: mark:stage_left    # mark:Name | home | home+{x:5,z:0} | {x:-6,z:0}
duration_ticks: 40
facing: mark:center             # optional: orient on arrival (yaw only)
```

**For mob targets — pathfinder crosses:**
- Mob pathfinder is used for gradual movement (`duration_ticks > 0`). Arrival is not guaranteed on complex terrain.
- Puppets (ENTITY_AI disabled) do not pathfind. CROSS_TO on a puppet with duration > 0 may not move. Use ENTITY_AI `enabled: true` before a mob cross, or use instant CROSS_TO (no `duration_ticks`) to teleport.
- `entity_group:` targeting moves all group members simultaneously. Each member pathfinds independently to the same destination — they may arrive at different times.
- Keep mob cross distances short (5–10 blocks). Open, flat terrain. Avoid complex obstacle courses.

**For player cast members — smooth move:**
- Uses `smoothMovePlayer` — tick-exact absolute interpolation. Reliable and precise.
- Player cannot override the move while it is in progress.

**Timing guide:**

| Distance | Normal speed | Slow/dramatic (0.4 speed) |
|----------|-------------|--------------------------|
| 5 blocks | ~25t | ~60t |
| 10 blocks | ~50t | ~120t |
| 20 blocks | ~100t | ~240t |

**CROSS_TO boundary with Effects:** When CROSS_TO targets a performer crossing *toward* the player's location = Choreography. When CROSS_TO is applied *to the show's target player themselves* = Effects.

---

### Instrument: Entrances and Exits

**Java grounding:** ENTER, EXIT

**What it does:** ENTER spawns a performer at a wing mark and immediately pathfinds them toward the stage. EXIT moves a performer to a wing mark and optionally removes them on arrival. These are semantic shorthands that communicate the dramaturgy clearly.

**ENTER:**
```yaml
type: ENTER
entity_type: FOX
name: "Trickster"
from: mark:wing_right       # off-stage spawn point
destination: mark:SR        # stage_right — their first position
baby: false
despawn_on_end: true
```

**EXIT:**
```yaml
type: EXIT
target: entity:spawned:Trickster
to: mark:wing_right
despawn_on_arrival: true    # entity removed when it reaches the wing
```

**EXIT despawn check:** Polls every 5 ticks looking for distance < 2 blocks from destination. Fast, clean. `despawn_on_arrival: false` leaves the entity at the wing — only useful if you're bringing them back.

**Behavioral note — ENTER equipment gap:** See the SPAWN_ENTITY instrument above. If the entering performer needs equipment, use SPAWN_ENTITY + CROSS_TO.

---

### Instrument: Chorus Management — Captured World Entities

**Java grounding:** CAPTURE_ENTITIES, RELEASE_ENTITIES

**What it does:** Absorbs world-resident entities (already existing in the Minecraft world) into show control as a named group. Useful for animals, villagers, or ambient mobs that are already in the environment rather than spawned fresh.

```yaml
# Capture up to 8 bats within 15 blocks of the anchor
type: CAPTURE_ENTITIES
entity_type: BAT
group_name: "bat_chorus"
radius: 15          # blocks — cubic capture radius
max_count: 8        # limit; silently captures fewer if not enough in range
```

**Targeting a captured group:**
```yaml
# Move the whole chorus to a mark
type: CROSS_TO
target: entity_group:bat_chorus
destination: mark:UC
duration_ticks: 60

# Freeze them
type: HOLD
target: entity_group:bat_chorus

# Apply a speed change
type: ENTITY_SPEED
target: entity_group:bat_chorus    # only works on first member — see gap below
speed: 0.3
```

> ⚠️ **ENTITY_SPEED gap on entity groups:** `handleEntitySpeed` in EntityEventExecutor.java
> calls `resolveEntity()` which returns only the first group member. ENTITY_SPEED cannot
> address an entire group simultaneously. Apply ENTITY_SPEED to individually named entities,
> or use ENTITY_AI to control group movement through pathfinding.

**Release — returning world entities to natural behavior:**
```yaml
type: RELEASE_ENTITIES
target: entity_group:bat_chorus
restore_ai: true    # re-enables AI on all members before releasing them from show tracking
```

**CAPTURE_ENTITIES vs. SPAWN_ENTITY:** Use CAPTURE when the world-resident creatures are part of the show's environmental authenticity (the bats are *already there*, the villagers are *actually from this village*). Use SPAWN_ENTITY when you need a specific, named, controllable performer with a scripted role.

---

### Instrument: Hold — The Tableau

**Java grounding:** HOLD

**What it does:** Zeroes an entity's velocity at the moment it fires. Instant.

```yaml
type: HOLD
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:groupname | player
```

> ⚠️ **Behavioral correction:** HOLD does NOT disable AI. The code (`StageEventExecutor.handleHold`)
> only calls `living.setVelocity(Vector.getRandom().multiply(0))` — a velocity zero, nothing more.
> An AI-enabled mob that is HELD will immediately resume pathfinding after the zero-velocity tick.
>
> **For a persistent freeze:** Use `ENTITY_AI enabled: false` before or instead of HOLD. HOLD
> is appropriate for momentary pauses, not sustained stillness.

HOLD is most useful for: stopping a chorus mid-cross for a dramatic beat, punctuating a moment of recognition, or clearing velocity from a launched entity mid-arc.

---

### Instrument: Orientation — FACE

**Java grounding:** FACE

**What it does:** Turns an entity to face a mark, compass direction, another entity, or the player. Yaw only (horizontal). Instant.

```yaml
type: FACE
target: entity:spawned:Herald   # entity:spawned:Name | entity_group:groupname | player
look_at: mark:center            # mark:Name | player | compass:south | entity:spawned:OtherName
```

**Valid `look_at` values:**

| Value | What it targets |
|-------|----------------|
| `mark:Name` | Named mark in the show's mark table |
| `player` | The spatial anchor player |
| `compass:north/south/east/west` | Compass direction |
| `entity:spawned:Name` | A named spawned entity |

**Yaw only — the pitch gap:** FACE does not set vertical orientation. An entity turned with FACE faces the horizontal direction of the target but is not pitched up or down. See Gaps section for workaround on player pitch.

**Use in sequence with CROSS_TO:** FACE at the moment of arrival is more expressive than `facing:` on the CROSS_TO. FACE can also be fired mid-cross to have a performer turn to look at something while walking.

---

### Instrument: Speed Modulation — ENTITY_SPEED

**Java grounding:** ENTITY_SPEED

**What it does:** Scales a LivingEntity's base movement speed by setting the MOVEMENT_SPEED attribute.

```yaml
type: ENTITY_SPEED
target: entity:spawned:Herald
speed: 0.4   # 0.0 = stopped | 0.05 = very slow creep | 0.4 = dramatic slow | 1.0 = normal | 2.0 = fast
```

**Speed is permanent until reset:** Once set, the speed stays at the new value for the entity's lifetime. If a performer needs to slow dramatically for a scene and then exit at normal speed, fire ENTITY_SPEED again at exit time.

**`speed: 0.0` vs. ENTITY_AI disabled:** Setting speed to 0 freezes movement but the pathfinder still tries to act. The entity will vibrate in place or rotate. For clean stillness, use ENTITY_AI disabled. Reserve speed 0 for freeze-frame moments where a tiny AI flicker is acceptable.

**Confirmed implementation:** `MOVEMENT_SPEED × 0.2` is the Bukkit base unit conversion. Setting speed 1.0 = 0.2 attribute value = normal Minecraft mob speed.

---

### Instrument: Impulse Launch — ENTITY_VELOCITY

**Java grounding:** ENTITY_VELOCITY

**What it does:** Applies a one-time vector impulse to any entity. Physics applies after.

```yaml
type: ENTITY_VELOCITY
target: entity:spawned:Herald   # or entity_group:chorus
vector: {x: 0.0, y: 0.8, z: 0.0}   # y > 0 = upward
```

**Impulse scale guide:**
- `y: 0.4` — gentle upward bounce
- `y: 0.8` — moderate launch; entity rises ~4–6 blocks
- `y: 1.5` — dramatic throw
- `y: 2.0+` — extreme launch; use with ENTITY_EFFECT slow_falling for a floating arc

**Controlled float arc pattern:**
```yaml
# Launch an entity upward into a gentle float
- at: 100
  type: ENTITY_VELOCITY
  target: entity:spawned:Vex
  vector: {x: 0.0, y: 1.0, z: 0.0}

- at: 101
  type: ENTITY_EFFECT
  target: entity:spawned:Vex
  effect_id: slow_falling
  duration_ticks: 80
  amplifier: 0
```

**Works on any Entity subtype** (not just LivingEntity) — confirmed in EntityEventExecutor.java.

---

### Instrument: Despawn

**Java grounding:** DESPAWN_ENTITY

**What it does:** Removes a named show-spawned entity from the world immediately.

```yaml
type: DESPAWN_ENTITY
target: entity:spawned:Herald
particle_burst: true   # optional: brief explosion particle on despawn
```

**`despawn_on_end: true`** on the SPAWN_ENTITY is the standard cleanup path — entities are automatically removed when the show ends. Use DESPAWN_ENTITY for mid-show removals only (a character leaves the story, a prop disappears dramatically).

**RETURN_HOME — players only:**
```yaml
type: RETURN_HOME
target: player          # or group_1 | group_2 | participants
duration_ticks: 20      # smooth move back to pre-show capture location; 0 = instant
```

> ⚠️ **RETURN_HOME is players only.** The executor (`StageEventExecutor.handleReturnHome`)
> explicitly skips non-Player entities: `if (!(entity instanceof Player player)) continue;`
> Spawned entities cannot be returned to a home position — use CROSS_TO or DESPAWN_ENTITY
> to manage their end-of-show cleanup instead.

---

## NPC Design Process — Cue Library for Performer Movement

### Naming Convention

Choreography cues follow the standard library naming: `[category].[archetype].[variant]`

**Suggested category families:**

| Family prefix | What it covers |
|---------------|---------------|
| `enter.*` | Arrival patterns — how a performer comes on stage |
| `exit.*` | Departure patterns — how a performer leaves |
| `cross.*` | Stage crosses — movement from one position to another |
| `hold.*` | Tableau patterns — the group holds while something happens |
| `orbit.*` | Circular movement sequences (multi-step CROSS_TO approximations) |
| `patrol.*` | Looping movement patterns for idle performers |
| `react.*` | Rapid orientation or velocity bursts (responding to an event) |
| `chorus.*` | Group movement patterns — entity_group coordination |

**Example cue IDs:**
- `enter.herald.slow_approach` — a single named Herald enters from wing_left, slow cross to stage_left
- `enter.chorus.scatter_in` — a group of NPCs fans in from multiple wings simultaneously
- `cross.performer.dramatic_close` — slow cross toward the player mark (approach)
- `hold.tableau.face_player` — whole cast freezes and faces the player
- `orbit.circle.slow_cw` — 12-step clockwise orbit pattern at radius 5
- `chorus.swell.approach` — group converges toward center from ring positions

### Building a Choreography Cue

The process for designing a new NPC choreography cue:

**1. Know your performer.** Before writing YAML, answer: What entity type? Puppet or performer? What is their dramatic register? (Check Casting KB.) What do they look like? (Check Wardrobe.) Where do they start and where do they go?

**2. Design the movement score.** Write the spatial story first: at T=0 the performer is at X. At T=N they need to be at Y. What does the journey between X and Y communicate? Quick = urgency. Slow = weight or reverence. Hesitant (start, pause, continue) = conflict.

**3. Identify coordination points.** Does the performer's arrival need a sound hit? (Sound.) Does their movement affect what the player is looking at? (Effects/Camera.) Does their exit mark the end of a lighting state? (Lighting.) Map these before writing the cue timeline.

**4. AI state management.** Default: ENTITY_AI disabled at spawn (puppet). Enable AI explicitly when performer mode is the creative choice for a specific beat.

**5. Build the demo show.** Any new cue family gets a demo show: quiet Sprite intro → cue fires → labeled in chat (C1, C2, etc.). Run sheet alongside. Debrief by cue number.

**6. Tag the cue.** Required tags per spec §10: movement intent (approach, departure, hold, orbit), tone register, appropriate occasions.

---

## NPC Type Vocabulary — Movement Notes by Entity Type

Not every entity type works equally well as a performer. These notes cover movement behavior — the Casting KB covers dramatic register (what each entity *means*).

| Entity type | Pathfinding | Puppet behavior | Best used as | Choreography notes |
|-------------|-------------|-----------------|--------------|-------------------|
| VILLAGER | Good — reliable on flat terrain | Clean and still | Named performers, witness figures | Slow down with ENTITY_SPEED 0.3 for dignified crosses; profession set via SPAWN_ENTITY variant (⚠️ gapped) |
| WOLF (tamed) | Good — follows, loyal AI | Still and attentive | Companion, devotion figure | AI performer mode: wolf turns to look at things genuinely; tame before show via COMMAND |
| IRON_GOLEM | Slow, wide pathfinding | Massive, still presence | Guardian, monument | Hard to cross quickly; use for deliberate slow approaches (ENTITY_SPEED 0.15); rotate with FACE for orientation |
| VEX | Phases through blocks — pathfinding unreliable | Floats in place | Aerial chorus, shadow figures | Best as puppet; ENTITY_VELOCITY upward + slow_falling for floating arc; avoid crossing through walls |
| ALLAY | Good — hovers, follows items | Hovers near spawn point | Joy chorus, guide | AI mode: genuine hovering curiosity; puppet: still floating presence; use group for swarming joy |
| BAT | Erratic in AI mode | Still but twitches | Background atmosphere | Best as captured chorus (CAPTURE_ENTITIES); enable AI for genuine cave atmosphere; terrible as precision performer |
| FOX | Sneaks, unpredictable | Still and watchful | Trickster, observer | ENTITY_SPEED 0.2 + AI enabled = slow stealthy approach; fast = bounding |
| PHANTOM | AI swoops at players — dangerous near target | Hovers with visible menace | Aerial threat, consequence | Puppet ONLY near players; distance performer at 10+ blocks; FACE toward player for intimidation |
| ENDERMAN | Teleports when looked at (player) | Still, stares | The watcher, tension | Puppet ONLY; FACE toward player for stare; never enable AI near player |
| ARMOR_STAND | No pathfinding (not a Mob) | Perfect stillness | Set piece, silent witness, relic | Always puppet; instant CROSS_TO for position change; FACE for orientation; no ENTITY_SPEED |

---

## Cross-Department Collaboration

### With Casting

Casting chooses *who* is on stage. Choreography decides *where and when* they move. The handoff:
- Casting confirms entity type and AI mode intent (puppet vs. performer)
- Choreography translates that into SPAWN_ENTITY/ENTITY_AI, then the movement score
- If Casting's entity type choice creates movement problems (Enderman teleporting, Vex phasing), Choreography flags to the Show Director before the show enters authoring

### With Wardrobe

Wardrobe owns equipment fields. Choreography owns when the entity appears and moves. The handoff:
- Wardrobe supplies equipment values for the SPAWN_ENTITY block
- If using ENTER (equipment gap), Choreography uses SPAWN_ENTITY + CROSS_TO instead and coordinates with Wardrobe on spawn position
- Mid-show costume changes (ENTITY_EQUIP) are Wardrobe events that Choreography must account for in timing — a costume change at tick 200 may need the performer to be stationary (HOLD + ENTITY_AI disabled)

### With Set

Set owns marks and spatial structure. Choreography moves performers between those marks. The handoff:
- Set defines all marks (center, stage_left, wing_right, etc.) before Choreography can author movement
- Choreography should never hardcode XZ offsets when a named mark would serve — named marks are portable, inline offsets are not
- If a scene requires a new mark position, Choreography requests it from Set rather than working around it with offsets

### With Effects

The department boundary:
- A performer (NPC, entity, cast member) crossing *toward* the player = Choreography
- The target player being moved *to* a mark = Effects

When both happen simultaneously — a performer approaches while the player is levitated — the departments coordinate. Effects and Choreography must agree on the player's position at each key beat so the performer's crosses are authored relative to where the player *actually will be*, not where they started.

**night_vision awareness:** If Effects is applying `night_vision` to reveal a dark scene, Choreography should know — performers that were invisible in darkness are now visible. Timing of NPC reveals may need adjustment.

### With Sound

Most entrances and exits need a sound hit to land. Choreography marks the tick of first movement and the tick of arrival for each performer; Sound designs around those timestamps. The beat the performer crosses to center is Sound's moment to decide: silence, a sound cut, or a musical arrival.

### With Stage Manager

Beat collisions are most common at group movement events. When a chorus enters at the same tick as a text event, or two performers cross at the same time and may collide visually, Stage Manager resolves by offset, reorder, or consolidation. Choreography provides the tick timestamps and spatial positions; Stage Manager decides whether a collision exists.

Choreography's run sheet column: **Spatial State** — where every named performer is at the start of each section. This is Kendra's reference for collision detection.

---

## Tone Translation

When the Show Director gives a tone phrase, the Choreographer translates it into movement choices.

**"Tender"** → slow crosses (ENTITY_SPEED 0.2–0.3), short distances, stopping close to the player, facing the player. A single performer, not a group. Stillness more than movement.

**"Overwhelming"** → simultaneous group movement (entity_group chorus), all crosses firing at once, converging from multiple directions. Close together at destination.

**"Strange / Uncanny"** → movement that doesn't quite fit. Wrong speed for the context (very fast where slow is expected; very slow in a busy scene). A performer that faces away from the player when everyone else faces them. AI mode releasing at an unexpected moment.

**"Earned / Arrival"** → withheld, then given. No movement for several sections, then a cross that takes time and lands with weight. Speed 0.15–0.25. Pause before final position.

**"Delight / Play"** → quick, light-footed movement. ENTITY_SPEED 1.5–2.5, short cross distances, frequent direction changes. Allay behavior (AI enabled) is the natural archetype — bouncing, circling, darting. Multiple performers moving independently at the same time reads as energy, not chaos. A Wolf bounding toward the player with AI briefly enabled before returning to puppet is pure delight.

**"Joy / Release"** → high speed (ENTITY_SPEED 1.5–2.0), ENTITY_VELOCITY upward burst, Allay group AI enabled. Movement that spreads outward from center rather than converging.

**When a tone phrase needs elaboration:** If the Director says "chaotic" and Choreography can't tell whether that means genuine AI-enabled wildness or a scripted approximation of chaos, ask one question: "Should this be genuine entity behavior (AI on, unpredictable) or designed to look chaotic (timed crosses that don't quite sync)?" The answer determines whether Choreography reaches for AI mode or for tight scripted timing.

---

## Department Principles

**Choreography exists to make the player feel that something alive is sharing the stage with them.** That's it. Every CROSS_TO, every FACE, every held tableau is in service of that feeling. A well-choreographed scene makes the player instinctively track a performer the way they would a real creature — not because they're told to, but because the movement compels it.

**Default to puppet. Choose performer deliberately.** AI-enabled entities are unpredictable. The authentic behavior of a real wolf is more compelling than a scripted imitation of one — but only if the show is designed to accommodate wildness. Puppet mode gives control; performer mode gives authenticity. Pick intentionally for each entity in each scene.

**Movement is composition.** At tick 120, where is everyone? The Choreographer should be able to draw a stage picture at any moment in the show. If the answer is "they're somewhere between A and B depending on pathfinding," that's a design problem, not just a timing issue.

**Short crosses. Open terrain. Named marks.** The three working rules for mob choreography. Long crosses fail on complex terrain. Named marks are portable; inline offsets break when the show moves to a new space.

**Escalation:** Choreography resolves blocking, timing, and movement score decisions independently. Escalates to the Show Director when a cross decision requires another department to fundamentally change what their beat is — e.g., "the performer's arrival at T=200 requires Sound to delay their climax hit by 10 ticks." Bring the analysis: what the conflict is, what the options are, which one Choreography prefers and why.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Choreography department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `kb/departments/stage-management/stage-management.kb.md` → Active Gap Registry.

---

### Gap: FACE — yaw only, no pitch control

**Status:** Open. Filed in `ops-inbox.md`.

FACE computes horizontal yaw only. Vertical pitch is not set — an entity FACE'd toward an overhead firework still looks straight ahead horizontally.

**Workaround for players:** PLAYER_TELEPORT with explicit `pitch:` value (Effects department). For entities: no current workaround for pitch. An entity cannot be told to look up or down.

---

### Gap: No smooth yaw rotation (no ROTATE event)

**Status:** Open. Filed in `ops-inbox.md`.

FACE is instant. No first-class event for gradual horizontal rotation without position change.

**Workaround (player perspective only):** PLAYER_SPECTATE on a slowly moving entity. See Effects/Camera KB.

---

### Gap: ENTITY_SPEED does not address entity groups

**Status:** Open. Filed in ops-inbox.md (2026-03-25, Choreography KB build).

`EntityEventExecutor.resolveEntity()` returns only the first member of an entity group. ENTITY_SPEED on `entity_group:Name` silently affects only one entity.

**Workaround:** Apply ENTITY_SPEED to individually named entities. Or use ENTITY_AI + CROSS_TO to achieve synchronized group speed through pathfinding.

---

### Gap: ENTER does not apply equipment

**Status:** Open. Filed in ops-inbox.md (2026-03-25, Choreography KB build).

StageEventExecutor.handleEnter() does not apply equipment fields, unlike EntityEventExecutor.handleSpawn(). The ENTER event in cue-show-yaml-schema.md implies equipment should work, but it doesn't.

**Workaround:** Use SPAWN_ENTITY (at the wing mark offset) + CROSS_TO (to the destination) when the entering performer needs equipment.

---

### Gap: RETURN_HOME — players only

**Status:** Open. Filed in ops-inbox.md (2026-03-25, Choreography KB build).

StageEventExecutor.handleReturnHome() skips any target that is not a Player. Spawned entities cannot be returned to a home position.

**Workaround:** Use CROSS_TO or DESPAWN_ENTITY for spawned entity cleanup.

---

### Limitation: Mob pathfinding on complex terrain

Bukkit pathfinder arrival is not guaranteed. On complex terrain, mobs may get stuck or take indirect routes.

**Workaround:** Design mob crosses for open, flat surfaces. Keep cross distances short (5–10 blocks). Use instant CROSS_TO for position precision.

---

### Limitation: No ORBIT primitive

Smooth continuous circular movement is not a first-class event. Compose orbits from a sequence of CROSS_TO events targeting marks arranged around a circle (8–12 marks = one approximate orbit).

---

## Calibration Backlog

📋 Proposed = named, not yet tested in a dedicated round. ✅ Confirmed = tested, parameters known.

**Note on mob movement:** Mob pathfinding (CROSS_TO on mob targets) does not guarantee smooth arrival. Choreography patterns work with what the engine actually does well: placement, AI state, and timing — not transit choreography. Named patterns below reflect this constraint.

---

### `placement.near` — 📋 Proposed
**Intent:** Entity spawns within 3–5 blocks of the player. Immediate presence. The performer is already there when the player notices them.
**Confirmed when:** Entity spawns reliably at close range without clipping into the player. Presence registers before any other event fires.

---

### `placement.far` — 📋 Proposed
**Intent:** Entity spawns 10–15 blocks away. Visible but distant — there, but not yet here.
**Confirmed when:** Player sees the entity clearly. Distance reads as intentional separation, not just background.

---

### `placement.wing` — 📋 Proposed
**Intent:** Entity spawns at a named wing mark (off the performance area), ready to enter. Not yet visible to the player or at the edge of visibility.
**Confirmed when:** Entity at wing mark does not register as part of the scene until an ENTER event fires.

---

### `sequence.arrive.hold` — 📋 Proposed
**Intent:** Entity spawns (at near or far placement) and holds position in puppet state. No movement. Presence only.
**Confirmed when:** Entity does not drift or pathfind. Holds visually still for the full section.

---

### `sequence.arrive.wander` — 📋 Proposed
**Intent:** Entity spawns, holds briefly (puppet), then AI is released. Begins wandering naturally with no further direction.
**Confirmed when:** Transition from puppet to AI reads as the entity "coming alive." Wander feels organic, not erratic.

---

### `sequence.arrive.hold.exit` — 📋 Proposed
**Intent:** Entity spawns, holds position throughout a section, then despawns on cue (DESPAWN_ENTITY with optional particle burst). The visitor who was there and is now gone.
**Confirmed when:** Despawn timing is precise to the cue. Particle burst (if used) reads as exit, not destruction.

---

### `sequence.arrive.wander.exit` — 📋 Proposed
**Intent:** The full arc. Entity spawns → brief hold → AI released → wanders freely for a section → despawns on cue. The complete performer lifecycle.
**Confirmed when:** Each phase transition is readable. Wander phase feels purposeful, not aimless. Exit is clean.

---

### `formation.rotate.clockwise` — 📋 Proposed
**Intent:** N entities positioned at marks arranged in a geometric formation (square, circle). All fire AI CROSS to the next mark clockwise simultaneously. Does imprecise individual pathfinding read as organic collective movement or as chaos? What's the right follow-up at arrival — hold, brief wander, or continuous next rotation?
**Setup requirements:** Open flat terrain, named marks arranged around a clear geometric path, entities pre-placed at marks (via ENTRANCE), all AI CROSS events firing at the same tick.
**Variables to test:** Entity count (start at 4); mark spacing (tight vs. generous); speed (slow = procession, fast = scatter); follow-up behavior (ENTITY_AI off on a timer vs. continuous rotation vs. free wander).
**Also test:** Two entities moving side by side to adjacent marks simultaneously — do they jostle naturally or fight for space? At what mark separation does collision stop being a problem?
**Confirmed when:** Simultaneous clockwise rotation reads as intentional movement, not chaos. Know the conditions (terrain, spacing, speed) under which it holds together.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| SPAWN_ENTITY (all fields) | ✅ Verified | Equipment, baby, name, offset — all applied in EntityEventExecutor |
| SPAWN_ENTITY variant/profession | ⚠️ Gapped | Parsed but not applied — see ops-inbox.md |
| ENTITY_AI (puppet/performer toggle) | ✅ Verified | Works on Mob subtypes only; Armor Stands always puppet |
| CROSS_TO — player (smooth move) | ✅ Verified | Absolute interpolation; tick-exact; confirmed in StageEventExecutor |
| CROSS_TO — mob (pathfinder) | ✅ Verified | Arrival not guaranteed on complex terrain |
| CROSS_TO — instant (no duration) | ✅ Verified | Teleport with yaw preserved |
| ENTER (spawn + cross) | ✅ Verified | ⚠️ Equipment not applied — see gap above |
| EXIT (cross + despawn) | ✅ Verified | despawn_on_arrival polls every 5t; triggers at < 2 blocks |
| CAPTURE_ENTITIES | ✅ Verified | Cubic radius; max_count respected; group registered |
| RELEASE_ENTITIES + restore_ai | ✅ Verified | Group cleared from show tracking; AI restored if restore_ai: true |
| HOLD (velocity zero) | ✅ Verified | ⚠️ Does NOT disable AI — persistent freeze requires ENTITY_AI disabled |
| FACE (yaw only) | ✅ Verified | Pitch gap open in ops-inbox.md |
| RETURN_HOME | ✅ Verified | Players only — entities silently skipped |
| ENTITY_SPEED | ✅ Verified | MOVEMENT_SPEED × 0.2; single entity only (group gap) |
| ENTITY_VELOCITY | ✅ Verified | Any Entity subtype; one-time impulse |
| DESPAWN_ENTITY + particle_burst | ✅ Verified | Confirmed in EntityEventExecutor |
| ORBIT (multi-step cross approximation) | 📋 Aspirational | No first-class primitive; compose from CROSS_TO sequence |
| FACE with pitch (vertical) | ⚠️ Gapped | Filed in ops-inbox.md under FACE gap entry |
