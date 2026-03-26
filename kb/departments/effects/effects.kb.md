---
department: Effects Director
owner: Felix
kb_version: 2.0
updated: 2026-03-26
---

# Effects Director — Technical Knowledgebase

> Technical reference for the Effects department. Documents what the ScaenaShows Java plugin
> can do to the target player: scripted movement, aerial physics, perceptual alteration,
> and particles — and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §5. Effects Director`.
>
> Camera is the closest peer department — orientation, cinematic perspective, and screen
> distortion reference: `kb/departments/camera/camera.kb.md`.

---

## Felix

Felix runs the Effects department with a bias toward restraint. His default instinct is to
ask what the *minimum intervention* would be — not because he is conservative, but because
he understands that every physical sensation the player feels raises the threshold for the
next one. He guards the altitude arc jealously: height is spent, not borrowed, and once the
player has been at altitude, you cannot manufacture wonder from it again. His work on the
archetype sampler — withholding the amp-9 lift through six sections to make it land at C7 —
is the clearest statement of his working philosophy.

Felix escalates to the Show Director when a tone phrase implies physical sensation without
specifying whether the player is aerial or grounded. The altitude context changes every
Effects decision, and he does not guess at it.

---

## Role Summary

- **Owns all forced movement of the show's target player** — teleportation, smooth horizontal crosses, velocity impulses, and aerial physics (levitation, flight lock, controlled descent)
- **Owns all physics-layer alteration** — potion effects applied to the player: levitation, slow_falling, perceptual states (blindness, darkness, nausea, speed, slowness)
- **Owns particle presence** applied to the player's space — repeating atmospheric layers and single-burst moments
- **Joint authority with Camera** on any event that moves the player's body *and* resets their orientation — Effects sets the destination, Camera sets the facing
- **Does not own** screen distortion events (CAMERA), player orientation (FACE, PLAYER_SPECTATE/MOUNT), or world-state changes (TIME_OF_DAY, WEATHER, LIGHTNING)

---

## Instrument Inventory

Effects instruments fall into four groups: scripted movement, aerial physics, perceptual
alteration, and atmospheric presence.

---

### Scripted Movement

---

#### PLAYER_TELEPORT — Set Transition

The primary tool for instant set changes. Moves the player to an absolute destination or
relative offset. Destination is Effects authority. `yaw` and `pitch` fields on arrival are
Camera authority — coordinate with Camera on any transition that also resets orientation.

**Java grounding:** `PlayerTeleportEvent` → `PlayerEventExecutor.handleTeleport()`

**What the player experiences:** An instant cut in space. Done well — paired with a Camera
blackout to cover the seam — this is invisible: the player opens their eyes somewhere new.
Done without Camera coordination, the teleport flash is perceptible.

**How to dial it:**

```yaml
# Set-based destination (from show's sets: block):
type: PLAYER_TELEPORT
audience: participants
destination: set:main_stage      # set name defined in show YAML sets: block
yaw: 180.0                       # Camera authority: 0=south, 90=west, 180=north, 270=east
pitch: -30.0                     # Camera authority: -90=up, 0=horizontal, 90=down

# Relative offset (from current position):
type: PLAYER_TELEPORT
audience: participants
offset: {x: 0, y: 0, z: 0}      # relative offset from player's current position
# yaw/pitch can be set here when Camera wants to reset orientation on arrival
```

**Strengths:** Instant, reliable, supports both yaw and pitch control on arrival. Set-based
destination keeps show YAML readable; coordinates live in the show sets block.

**Limitations:** Instant only — no smooth movement (use CROSS_TO for gradual crosses). The
teleport flash is perceptible without a Camera blackout to cover it.

**Standard scene-transition technique:**
Camera fires `blackout` 10 ticks before a PLAYER_TELEPORT. Effects authors the teleport
during the black. Camera sets yaw/pitch on arrival. Player opens their eyes somewhere new.

---

#### CROSS_TO — Scripted Movement Through Space

Smooth movement of the target player to a named mark. Used for traveling across a stage or
moving through space as part of a scene — not for instant set changes (use PLAYER_TELEPORT
for those). Facing on arrival is Camera authority.

**Java grounding:** `CrossToEvent` → `StageEventExecutor.handleCrossTo()` →
`smoothMovePlayer()` — absolute position interpolation, no drift accumulation.

**What the player experiences:** A gliding movement through space. The player's orientation
is not changed during the cross. At slow duration, it reads as gentle drifting; at faster
duration, a definite scripted cross. Pairs naturally with aerial sequences — the player can
be levitating while being crossed horizontally.

**How to dial it:**

```yaml
type: CROSS_TO
target: participants             # or group_1, group_2
destination: mark:center         # mark:Name (defined in show YAML marks: block)
duration_ticks: 60               # 0 = instant teleport; 60t = 3-second cross
facing: compass:north            # Camera authority — yaw-only on arrival (optional)
```

**Strengths:** Smooth absolute interpolation — no position drift across the cross. Works
with marks defined by the Set department. Can run simultaneously with levitation for aerial
scripted movement.

**Limitations:** Yaw-only facing on arrival — no pitch control (use PLAYER_TELEPORT with
explicit pitch for full orientation reset). Player can partially counteract horizontal
movement with movement keys.

**CROSS_TO vs. PLAYER_TELEPORT:** CROSS_TO is for visible movement through space.
PLAYER_TELEPORT is for instant cuts where the transition is covered (blackout) or the
distance is too large for a smooth cross.

---

#### PLAYER_VELOCITY — Impulse

A single vector impulse applied to the player. Physics then takes over — gravity and drag
apply naturally. Best used as a launch event: the beginning of an arc, not a sustained state.

**Java grounding:** `PlayerVelocityEvent` → `PlayerEventExecutor.handleVelocity()`

**What the player experiences:** A sudden physical push in the vector direction. Without
follow-up, this is a ballistic arc — up, then down. With EFFECT `slow_falling` applied
immediately after, the descent is cushioned. With PLAYER_FLIGHT `hover` at the arc's peak,
altitude locks.

**How to dial it:**

```yaml
type: PLAYER_VELOCITY
audience: participants
vector: {x: 0.0, y: 1.2, z: 0.0}   # y > 0 = upward
```

**Y-value spectrum:**
- `y: 0.4` — gentle lift, barely noticeable
- `y: 1.2` — clear upward push
- `y: 2.0` — dramatic launch
- `y: 3.0+` — extreme; risk of overshooting intended altitude

**Standard controlled-arc pattern:**

```yaml
# Launch:
- {at: 0, type: PLAYER_VELOCITY, audience: participants, vector: {x: 0.0, y: 1.5, z: 0.0}}
# Cushion the descent immediately:
- {at: 2, type: EFFECT, audience: participants, effect_id: slow_falling, duration_ticks: 120, amplifier: 0, hide_particles: true}
```

**Limitations:** Player can partially counteract with movement keys. Velocity is applied in
world space — a large y-impulse may overshoot if the player is already elevated. Not
sustained; follow with PLAYER_FLIGHT hover to lock altitude after the arc settles.

---

### Aerial Physics

---

#### PLAYER_FLIGHT — Altitude Lock and Release

Server-side flight control. Two states used in sequence: `hover` locks the player at their
current altitude; `release` ends flight gracefully with a controlled transition effect.

**Java grounding:** `PlayerFlightEvent` → `PlayerEventExecutor.handleFlight()`

**What the player experiences:** During hover — an eerie stillness, held aloft, the world
below. During release — the floor slowly returning, a breath-releasing controlled descent
before gravity fully re-engages.

**How to dial it:**

```yaml
# Engage hover (lock current altitude):
type: PLAYER_FLIGHT
state: hover
audience: participants

# Release with soft landing:
type: PLAYER_FLIGHT
state: release
release_effect: slow_falling       # slow_falling (default) | levitate | none
release_duration_ticks: 300        # how long the transition effect lasts before gravity returns
audience: participants
```

**Critical rule — always lift before hovering.** Hover locks at *current* altitude. Fire a
PLAYER_VELOCITY burst or EFFECT levitation first to bring the player to target altitude,
then lock with hover. Hover at ground level is hover at ground level.

**Critical rule — always release before show end.** Author a `state: release` as an
explicit event — don't rely on stop-safety. Stop-safety cleans up PLAYER_FLIGHT on show
interruption, but the descent won't be choreographed. The release is part of the arc.

---

#### EFFECT: Levitation — Altitude Engine

The primary instrument for aerial staging. Works through repeating EFFECT events at
calibrated timing intervals. Three operational patterns, each producing a distinct physical
sensation. All calibrated from in-game sessions (2026-03-24).

**Java grounding:** `EffectEvent` → `VisualEventExecutor.handleEffect()` →
`PotionEffectType.LEVITATION`

**What the player experiences:** Amp 0 = gentle, nearly imperceptible upward drift. Higher
amp = faster climb. At `amplifier: 9`, a surge — the arrival moment.

**Calibrated patterns:**

| Pattern | Configuration | Effect |
|---------|---------------|--------|
| HOVER | amp 0, lev=20t, gap=8t, cycle=28t | Clean altitude hold — "gentle bubbling" |
| CLIMB | amp 0, lev=24t, no gap (re-fires before expiry) | Gradual upward drift — "separation from earth" |
| RELEASE | amp 0, lev=20t, gap=24t, cycle=44t | Slow controlled descent — "blood pressure release" |

**HOVER pattern (inline cue events):**

```yaml
events:
  - {at: 0,  type: EFFECT, audience: participants, effect_id: levitation, duration_ticks: 20, amplifier: 0, hide_particles: true}
  - {at: 28, type: EFFECT, audience: participants, effect_id: levitation, duration_ticks: 20, amplifier: 0, hide_particles: true}
  - {at: 56, type: EFFECT, audience: participants, effect_id: levitation, duration_ticks: 20, amplifier: 0, hide_particles: true}
  # Continue at 28t intervals for the section duration
```

**CLIMB pattern:**

```yaml
events:
  - {at: 0,  type: EFFECT, audience: participants, effect_id: levitation, duration_ticks: 24, amplifier: 0, hide_particles: true}
  - {at: 24, type: EFFECT, audience: participants, effect_id: levitation, duration_ticks: 24, amplifier: 0, hide_particles: true}
  # No gap — levitation re-fires before the previous expires; player drifts upward
```

**AMP-9 LIFT — the earned arrival:**

```yaml
# Single amp-9 surge, then lock altitude with PLAYER_FLIGHT hover:
- {at: 0,  type: EFFECT, audience: participants, effect_id: levitation, duration_ticks: 40, amplifier: 9, hide_particles: true}
- {at: 42, type: PLAYER_FLIGHT, state: hover, audience: participants}
```

**Altitude is currency.** The HOVER → CLIMB → AMP-9 LIFT progression is designed to be
spent once. The amp-9 arrival lands because altitude was withheld. Use HOVER near ground
for the long approach; CLIMB to build slowly and quietly; reserve the amp-9 for the single
moment of arrival per show. Resetting this arc costs the emotional payoff of the earlier
restraint.

**Always set `hide_particles: true`** for show EFFECT events. The particle puffs that
normally appear when an effect is active break immersion.

---

#### EFFECT: slow_falling — Transition and Safety Net

Reduces gravity to create a floating sensation. Used after levitation arcs, on descent, and
as the default `release_effect` on PLAYER_FLIGHT release.

```yaml
type: EFFECT
effect_id: slow_falling
duration_ticks: 300    # cover the entire section where gravity should be soft
amplifier: 0
hide_particles: true
audience: participants
```

**Calibrated descent rate:** approximately 2 blocks/second under slow_falling (confirmed
in calibration). Use for post-levitation and post-flight transitions. Combine with
`minecraft:end_rod` or `minecraft:enchant` particles for a visually supported descent.

---

### Perceptual Alteration

---

#### EFFECT: Perceptual Suite — Weight, Revelation, Disorientation

Potion effects that alter how the player perceives and moves without changing their position.
Each instrument targets a distinct emotional register.

**Java grounding:** `EffectEvent` → `VisualEventExecutor.handleEffect()`

| effect_id | Player experience | Emotional register | Coordination notes |
|-----------|-------------------|--------------------|-------------------|
| `slowness` | Movement slowed | Weight, tension, time stretching | — |
| `speed` | Movement faster | Chase, urgency, tension | — |
| `darkness` | Pulsing dark vignette (deepens rhythmically) | Dread, weight, the Warden's domain | — |
| `blindness` | Severe dark vignette, short sight distance | Disorientation, fog of experience, revelation flash | 10–20t for a flash; longer for sustained fog |
| `nausea` | Screen wobble/spiral | Disorientation, vertigo, unreality | Same visual effect as CAMERA `sway` — coordinate with Camera to avoid doubling |
| `night_vision` | Full brightness in darkness | Revelation into a crafted dark space | **Coordinate with Lighting** — reveals the environment Lighting designed |

**Standard syntax:**

```yaml
type: EFFECT
effect_id: slowness
duration_ticks: 200
amplifier: 0           # 0 = subtle; 1+ = pronounced (test for comfort)
hide_particles: true
audience: participants
```

**`night_vision` coordination rule:** This is an Effects instrument, but it fundamentally
reveals what the Lighting Designer has built. Any scene using `night_vision` requires joint
planning — Effects applies it; Lighting ensures the revealed environment is intentional.

**`nausea` vs. CAMERA `sway`:** Both apply `PotionEffectType.NAUSEA`. EFFECT `nausea` is
Felix applying a potion (ownership: Effects). CAMERA `sway` is Mark applying a screen
distortion shorthand (ownership: Camera). Don't use both in the same moment without intent.

---

### Atmospheric Presence

---

#### PARTICLE — Environmental Presence Around the Player

Spawns particles at an offset from the spatial anchor. With `duration_ticks` and
`interval_ticks`, becomes a repeating atmospheric layer that surrounds the player's space
throughout a section.

**Java grounding:** `ParticleEvent` → `VisualEventExecutor.handleParticle()`

**What the player experiences:** Visual texture in the space around them — drifting ash,
rising embers, floating spores. Particles spawn at the anchor point, not at the player's
current altitude. Design for this deliberately: particles that read as "surrounding" at
ground level will read as "below" at height. This is a feature, not a bug — embers at the
anchor point read as heat rising toward a player at 6 blocks.

**How to dial it:**

```yaml
# Single burst:
type: PARTICLE
particle_id: minecraft:end_rod
count: 12
offset: [0.5, 0.8, 0.5]     # x/y/z spread radius around the anchor
extra: 0.0                    # speed multiplier (directional particles only)
force: true                   # render regardless of client particle distance settings

# Repeating atmospheric layer:
type: PARTICLE
particle_id: minecraft:ash
count: 8
offset: [3.0, 2.0, 3.0]
extra: 0.02
force: true
duration_ticks: 200
interval_ticks: 10            # fires every 10t for 200t = 20 bursts
```

**Always use `force: true`** for atmospheric particles — bypasses client-side particle
distance limits so particles are visible across the full stage.

**Altitude and particle relationship:** Particles spawn at the anchor (typically ground
level), not at the player's current altitude. A player at 25 blocks sees particles as the
world far below. Coordinate with Fireworks on y-offset for any pyrotechnic burst intended
to wrap around the player — if the player is at altitude, the fireworks must also be at
altitude.

**Atmospheric particle vocabulary:**

| particle_id | Visual effect | Best for |
|-------------|---------------|----------|
| `minecraft:ash` | Drifting ash/dust | Quiet devastation, industrial, aftermath |
| `minecraft:smoke` | Thin smoke wisps | Fire aftermath, fog of memory |
| `minecraft:flame` | Flame sparks | Warmth, danger, the campfire below |
| `minecraft:soul_fire_flame` | Blue-tinted flame | Spectral, uncanny, the underworld |
| `minecraft:heart` | Hearts | Joy, affection — use sparingly; can read comic |
| `minecraft:note` | Musical notes | Playfulness, the sonic made visible |
| `minecraft:enchant` | Enchantment glyphs | Magic, mystery, arrival into the arcane |
| `minecraft:totem_of_undying` | Radiant burst | Survival, vitality, rebirth |
| `minecraft:end_rod` | White sparkle | Ethereal, arrival, the transcendent — reads well above the player |
| `minecraft:snowflake` | Snowfall texture | Cold, stillness, isolation |
| `minecraft:warped_spore` | Teal drifting spores | Alien landscape, the uncanny |
| `minecraft:crimson_spore` | Red drifting spores | Corruption, blood, the hostile biome |

---

## Screen Distortion — Camera's Territory

CAMERA screen effects (`blackout`, `sway`, `flash`, `float`) belong to Camera (Mark).
Felix does not author CAMERA events. When a section requires both a physical Effect and a
screen distortion simultaneously, both departments coordinate on timing — but Camera writes
the CAMERA event.

The one point of overlap: EFFECT `nausea` and CAMERA `sway` both produce screen wobble.
If the intent is a potion sensation applied to the player's body, Felix owns it. If the
intent is a cinematic screen effect tied to a camera move or scene beat, Mark owns it.
When in doubt, bring it to Camera before writing.

Reference: `kb/departments/camera/camera.kb.md`

---

## Tone Translation

The Show Director gives experiential language — "tender and a little strange", "weight
without resolution", "the moment the ceiling opens." Felix translates these into physical
sensations. The test for every Effects decision: **does the player's body agree with what
the Director is trying to make them feel?**

### Felix's Default Interpretive Instinct

Felix's first move with any tone phrase is restraint. The player's body registers withheld
effects as much as applied ones — the absence of altitude, the absence of momentum, the
absence of sensation is its own statement. Felix asks "what does the body *not* do in this
section?" before asking what it does.

Altitude is Felix's most powerful instrument and the one he spends most carefully. Height,
once given, changes the register of every subsequent section. Felix doesn't give altitude
without a plan for where it goes.

### Worked Examples

**"Tender" / "gentle" / "emerging"**
> The world is new and fragile. The player's body should not be asked to carry anything big yet.

Effects reach: Low hover near the ground (0–3 blocks). HOVER pattern at amp 0. Particles
at body level — not below, not above. No potion effects. No velocity. The player is barely
lifted; the world shifts around them, not beneath them.

---

**"Weight" / "earthbound" / "tension"**
> Something is pressing down. The player cannot rise — yet.

Effects reach: No levitation. EFFECT `slowness` (amp 0 = subtle; amp 1 = pronounced).
EFFECT `darkness` for sections of dread or deep underground. Ash or `soul_fire_flame` particles.
The contrast between earthbound and aerial sections is how the lift earns its drama — keep the player
grounded through the full tension section, so the later rise means something.

---

**"Building" / "rising" / "ascending"**
> The world is separating from the player. Something is pulling them up.

Effects reach: CLIMB pattern — levitation re-fires every 24t, no gap. Particles shift as
altitude changes: what surrounded the player at ground level now reads as below. This
section should feel *unannounced* — no dramatic text stacking, no announcement. The climb
is quiet. The player notices without being told.

---

**"Arrival" / "earned transcendence" / "the ceiling opens"**
> This is the moment. It was withheld for this.

Effects reach: Amp-9 EFFECT levitation (40t) immediately followed by PLAYER_FLIGHT hover
to lock altitude. This instrument is used once per show. Everything before it was restraint
in service of this moment. After arrival, the player is an observer at height — the world
is below them. See "AMP-9 LIFT" in the Levitation instrument section.

---

**"Joy" / "celebration" / "release from above"**
> The player is high, and the joy is below them. They witness it — they don't drown in it.

Effects reach: PLAYER_FLIGHT hover at established altitude. Particles (heart, note, end_rod)
burst below the player's current altitude. The player does not descend into the joy — joy
rises toward them. This preserves the altitude as a perspective shift, not a platform.

---

**"Pressure release" / "return" / "descent"**
> The player is coming back down. This is not a fall — it is a return.

Effects reach: RELEASE pattern (lev=20t gap=24t cycle=44t) — the "blood pressure release"
feel confirmed in calibration. Slow, controlled. If the scene requires the player to descend
*through* something (fireworks, particles), coordinate y-offset timing with Fireworks so
the effects are at the player's altitude during the fall, not at ground level. The archetype
sampler C9 is the reference pattern for this.

---

**"Disorientation" / "unreality" / "the uncanny"**
> The player's grip on space is loosening.

Effects reach: EFFECT `nausea` (40–60t at amp 0 = subtle; amp 1 = full wobble). Brief
EFFECT `blindness` (10–20t) at a moment of revelation or rupture. Small PLAYER_VELOCITY
impulse (y: 0.3–0.5) — a bump, not a lift. `warped_spore` particles at close range.
Disorientation works best ground-adjacent, where the player can feel the instability without
the safety of altitude.

---

**"Intimacy" / "stillness" / "held"**
> Nothing is happening to the player's body. This is the quiet.

Effects reach: Nothing. No potion effects, no levitation, no particles. If the player is
already aerial, hold them at hover with no new events. Effects restraint is the contribution.
"The quiet between notes is Effects restraint."

---

### When to Ask the Director for Clarification

Felix asks one question before authoring any major physical movement: **"What altitude is
the player carrying into this section?"**

Altitude context changes every Effects decision. "Tender" at ground level is an emergence.
"Tender" at 20 blocks is a suspension. The Director doesn't always specify, and Felix
doesn't guess.

### What Effects Cannot Do Alone

- Cannot manufacture **wonder** without altitude having been earned first — the amp-9 lift
  only lands if the player has been ground-adjacent through the preceding sections
- Cannot make **tension or weight** land if the player has been aerial throughout — gravity is emotional
  context; the earthbound section requires altitude to have been spent earlier
- Cannot serve **dread** through levitation — dread wants darkness and slowness, not height;
  altitude carries wonder associations that undercut dread
- Cannot support **revelation** via `night_vision` without Lighting knowing it's coming —
  the revealed space must be designed, or the revelation is into an accidental environment

---

## Department Principles

**Altitude is currency, not backdrop.** Height changes how every other instrument reads.
Effects manages the altitude arc for the whole show — not just individual sections. Before
authoring any aerial sequence, map the full arc: when does the player first leave the
ground, when do they arrive at maximum height, when do they return?

**Restraint until the moment calls for it.** Effects interventions raise the bar for the
next one. A show that puts the player at height in section one has nowhere to go. Hold.
Build. Spend once.

**Every physical sensation must be intentional.** The question isn't "what effect fits this
section?" It's "what does the player's body need to agree with what the Director is making
them feel?" If there's no clear answer, the right choice is nothing.

**Effects and Camera are one system.** Every teleport with orientation, every levitation
with a desired horizon, every CROSS_TO with a facing — Effects moves the body, Camera
moves the eyes. Author jointly or the player experiences the seam.

**Vertical alignment with Fireworks is non-negotiable.** The most common Effects-Fireworks
miss is altitude mismatch: the player is at 25 blocks, the fireworks burst at 5. Agree on
player altitude at every pyrotechnic moment before either department authors events.

**Escalation discipline.** Felix brings resolved problems or problems with proposed
resolutions — not raw problems. "The RELEASE descent puts the player at ground level
20 ticks before the next section's levitation begins — do we compress the descent or delay
the lift?" Not: "I'm not sure about the timing."

---

## Cross-Department Coordination

**Effects + Camera (primary partnership):** Effects decides where the body is; Camera
decides where the eyes point. On every PLAYER_TELEPORT with facing fields, every CROSS_TO
with `facing:`, and every levitation sequence, both departments coordinate. Camera asserts
the player's pitch before liftoff. Effects asserts the destination before Camera sets
orientation. Joint authorship is the standard, not post-hoc adjustment.

**Effects + Fireworks:** Effects owns the player's altitude at every firework detonation.
Fireworks designs burst altitude to match. Agree on player height at each pyrotechnic moment
upfront. The archetype sampler C9 (RELEASE descent through fireworks) is the reference model.

**Effects + Lighting:** `night_vision` is Effects authority but reveals the environment
Lighting is crafting. Any scene using `night_vision` requires joint planning — does the
revealed space support the intended atmosphere?

**Effects + Choreography:** CROSS_TO on a performer moving toward the player is
Choreography. CROSS_TO forcing the *target player* to a position is Effects. When both
happen simultaneously, Effects and Choreography coordinate so scripted movements don't
conflict in the same space.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| PLAYER_TELEPORT | ✅ Verified | All fields; set:Name + offset; yaw and pitch confirmed; Camera owns yaw/pitch |
| CROSS_TO (target player) | ✅ Verified | Smooth absolute interpolation; target: participants; yaw-only facing on arrival |
| PLAYER_VELOCITY | ✅ Verified | Vector impulse confirmed; player can partially counteract |
| PLAYER_FLIGHT hover | ✅ Verified | Altitude lock confirmed in calibration |
| PLAYER_FLIGHT release + slow_falling | ✅ Verified | Controlled descent confirmed |
| EFFECT levitation (HOVER/CLIMB patterns) | ✅ Verified | Calibration data 2026-03-24; calibrated cycles confirmed |
| EFFECT levitation amp 9 (arrival surge) | ✅ Verified | Used in archetype sampler R7 |
| EFFECT slow_falling | ✅ Verified | ~2 b/s descent rate confirmed |
| EFFECT night_vision | ✅ Verified | Cross-discipline coordination required (Lighting) |
| EFFECT blindness / darkness / nausea | ✅ Verified | |
| EFFECT speed / slowness | ✅ Verified | |
| PARTICLE (single burst) | ✅ Verified | |
| PARTICLE (repeating atmospheric layer) | ✅ Verified | force: true bypasses client particle distance limits |
| GIVE_ITEM to player | 📋 Aspirational | Not yet implemented |
| GIVE_XP to player | 📋 Aspirational | Not yet implemented |

> Camera department events (CAMERA, PLAYER_SPECTATE, PLAYER_MOUNT, PLAYER_DISMOUNT, FACE)
> are tracked in `kb/departments/camera/camera.kb.md`.
