---
department: Lighting & Atmosphere Designer
owner: Lighting & Atmosphere Designer
kb_version: 1.0
updated: 2026-03-25
---

# Lighting & Atmosphere Designer — Technical Knowledgebase

> Technical reference for the Lighting & Atmosphere department. Documents what the ScaenaShows
> Java plugin can do for time of day, weather, particles, screen effects, and player perception —
> and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `docs/production-team.md §6. Lighting & Atmosphere Designer`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| TIME_OF_DAY | Point | Snap world time to a specified value |
| WEATHER | Bar | Set weather state (clear / storm / thunder) |
| LIGHTNING | Point | Cosmetic lightning strike at an offset (no damage, no fire) |
| PARTICLE | Point or Bar | Spawn particles at an offset; repeat for duration if configured |
| EFFECT (perceptual) | Bar | Apply potion effects to players for sensory alteration |
| CAMERA (atmospheric) | Bar | Screen-level perceptual distortion (blackout, sway, flash, float) |

CAMERA effects are a joint tool with the **Camera Director** — Lighting Designer owns them when used for atmospheric tone; Camera Director owns them for perspective/transition.

---

## Capabilities & YAML Syntax

---

### TIME_OF_DAY

Snaps the world clock to a specified time value instantly. Gradual transitions are composed by firing multiple TIME_OF_DAY events in sequence with short intervals between them.

```yaml
type: TIME_OF_DAY
time: 18000   # integer; 0–24000
```

**Time value reference:**

| Value | Time of day | Light quality |
|-------|-------------|---------------|
| 0 (= 24000) | Sunrise | First light, warm horizon |
| 1000 | Early morning | Clarity, long shadows |
| 6000 | Noon | Full daylight, maximum visibility, no shadows |
| 8000 | Mid-afternoon | Warm, slightly angled |
| 12000 | Sunset | Warmth fading, golden-orange |
| 13000 | Dusk | Liminal, the moment before dark |
| 18000 | Midnight | Full dark, deepest night |

**Gradual dusk transition (from R7 archetype sampler):**
```yaml
- at: 0
  type: TIME_OF_DAY
  time: 8000      # mid-afternoon

- at: 40
  type: TIME_OF_DAY
  time: 13000     # dusk

- at: 80
  type: TIME_OF_DAY
  time: 18000     # night
```
Three steps across 80 ticks reads as a continuous fade rather than discrete snaps. More steps = smoother transition. 40t intervals are perceptually gradual at show scale.

**Behavioral notes:**
- TIME_OF_DAY is server-wide — it affects all players on the server, not just show participants. This is a major consideration for shared-server shows.
- Underground or enclosed environments: sky time changes have no visible effect. The ambient light level comes from blocks, not sky.
- Outdoor environments: the sky and sun/moon position change immediately. Use this creatively — a sunset during an opening monologue is a free lighting effect.
- Consider restoring time at show end if the show significantly changes it. Document the original time in the run sheet and fire a TIME_OF_DAY restore event at show end (or use COMMAND: `/time set <original>`).

---

### WEATHER

Sets world weather state. Duration is optional; if omitted, weather persists until manually changed or another WEATHER event fires.

```yaml
type: WEATHER
state: clear    # clear | storm | thunder
duration_ticks: 400   # optional; how long this weather state lasts
```

**States:**
- `clear` — no precipitation, full sky visibility, natural ambient sound
- `storm` — rain (or snow in cold biomes), reduced visibility, continuous rain ambient sound
- `thunder` — storm + random thunder strikes from the server (distinct from scripted LIGHTNING events); dramatic ambient rumble

**Behavioral notes:**
- Weather changes the ambient audio layer significantly. A `storm` state immediately adds a bed of rain sound that the Sound Designer must account for. A `thunder` state adds random server-generated thunder that competes with scripted audio.
- Weather is server-wide (same consideration as TIME_OF_DAY).
- `clear` at T=0 is a common setup step — it ensures the show starts in a known weather state regardless of what the server weather was before play. The R7 archetype sampler uses this.
- If using WEATHER for a scene effect, pair with a `clear` restore at show end or after the effect passes.
- Duration: if set, the weather reverts to its previous state after `duration_ticks`. If omitted, the new state persists indefinitely.

---

### LIGHTNING

A cosmetic-only lightning strike at an XYZ offset from the spatial anchor. No damage, no fire, no block ignition. The visual strike and a single thunder crack play at the offset location.

```yaml
type: LIGHTNING
offset: {x: 5, y: 0, z: 0}
```

**Behavioral notes:**
- Fires a single visual strike + thunder sound. The thunder is tied to the strike — they cannot be separated.
- For dramatic timing: use a single LIGHTNING at a specific mark as a punctuation beat.
- For chaos: fire multiple LIGHTNING events at the same tick at different offsets.
- For ominous slow sequence: one LIGHTNING every 60–120t with silence between.
- The Y value in the offset is the strike's vertical position relative to the anchor. For lightning that appears to hit the ground, use Y=0 or the terrain height.

---

### PARTICLE

Spawns particles at an offset from the spatial anchor. With `duration_ticks` and `interval_ticks`, becomes a repeating atmospheric layer.

```yaml
# Single burst:
type: PARTICLE
particle_id: minecraft:heart
count: 12
offset: [0.5, 0.8, 0.5]   # x/y/z spread around the anchor
extra: 0.0                 # speed multiplier for directional particles
force: true                # true: render for all players regardless of particle settings

# Repeating atmospheric layer:
type: PARTICLE
particle_id: minecraft:ash
count: 8
offset: [3.0, 2.0, 3.0]
extra: 0.02
force: true
duration_ticks: 200
interval_ticks: 10   # fire every 10 ticks for 200 ticks = 20 bursts
```

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
| `minecraft:dripping_lava` | Slow drips — geological time, heat, pressure |
| `minecraft:warped_spore` | Teal drifting spores — alien landscape, the uncanny |
| `minecraft:crimson_spore` | Red drifting spores — corruption, blood, the hostile biome |

**`count:` and `offset:` interaction:**
- `count` is the number of particles per burst.
- `offset` is the XYZ spread radius — particles scatter randomly within this sphere.
- Low count + low offset = a few tight particles at a point. High count + high offset = a wide atmospheric cloud.

**Behavioral notes:**
- `force: true` bypasses client-side particle distance limits. Use for atmospheric particles that must be visible from across the stage.
- For the atmos.* cue library, PARTICLE is wrapped in reusable cues (e.g., `atmos.ambient.ember_drift`). See `docs/cue-library-survey.md` for current inventory.
- A PARTICLE bar (with duration + interval) runs in parallel with other events. It does not block the timeline.

---

### EFFECT — Perceptual alteration on players

Applies potion effects to players that alter how they perceive the world. These are owned by both the Lighting Designer (atmospheric use) and Camera Director (perspective use).

```yaml
type: EFFECT
effect_id: night_vision    # see table below
duration_ticks: 200
amplifier: 0
hide_particles: true       # suppress the particle indicators above the player's head
audience: participants | group_1 | group_2 | private
```

**Perceptual effects reference:**

| effect_id | What the player sees | Atmospheric use |
|-----------|---------------------|-----------------|
| `night_vision` | Full brightness in darkness | Reveal a dark space without lighting it; the cave that suddenly becomes visible |
| `blindness` | Severe dark vignette, short sight distance | Disorientation, the fog of experience, a transition |
| `darkness` | Pulsing dark vignette (deepens rhythmically) | Dread, weight, the Warden's domain, creeping unease |
| `nausea` | Screen wobble/spiral (same as CAMERA sway) | Disorientation, vertigo, unreality |
| `slow_falling` | Gravity reduced; float sensation | Used post-levitation to prevent hard landing |
| `levitation` | Upward drift | Aerial staging; amp 0 = hover, amp 1+ = climb |
| `speed` | Movement faster | Chase, urgency, tension |
| `slowness` | Movement slower | Weight, grief, time stretching |

**`hide_particles: true`** suppresses the particle puffs that normally appear around the player when an effect is active. Always set this for atmospheric effects — particle indicators break immersion.

**Levitation for aerial staging:** See `docs/departments/choreography.kb.md` for the calibrated levitation patterns (HOVER/CLIMB/RELEASE cycles derived from calibration sessions).

---

### CAMERA — Atmospheric screen effects

Screen-level perceptual tools. When used for atmospheric tone (not perspective transition), these are Lighting Designer tools.

```yaml
type: CAMERA
effect: blackout    # sway | blackout | flash | float
intensity: 1
duration_ticks: 40
audience: participants
```

| Effect | Lighting use case |
|--------|------------------|
| `blackout` | Scene transition in darkness; world disappears and returns |
| `sway` | Earthquake, vertigo, reality destabilizing |
| `flash` | Lightning acknowledgment, revelation flash |
| `float` | Brief weightlessness sensation, the moment before a fall |

See `docs/departments/camera.kb.md` for full CAMERA syntax reference.

---

## Fireworks as Light

Fireworks provide burst light and color in the sky. While the Fireworks domain is primarily owned by the Choreographer (spatial patterns) and the show author (composition), the Lighting Designer should consider firework color palettes as light sources in the scene. A burst of warm gold fireworks above the player during a joy moment IS the lighting for that beat.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Lighting department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `docs/departments/stage-manager.kb.md` → Active Gap Registry.

No gaps are currently filed specifically for the Lighting & Atmosphere department. All core tools (TIME_OF_DAY, WEATHER, LIGHTNING, PARTICLE, EFFECT, CAMERA) are implemented and functional.

**Limitations to be aware of:**
- TIME_OF_DAY and WEATHER are server-wide — they affect all players on the server, not just show participants.
- There is no way to set lighting per-player (e.g., give one group night vision but not another) without using EFFECT with group-scoped audience targeting.
- PARTICLE rendering is client-side distance-limited by player settings. `force: true` bypasses this limit for important effects.
- LIGHTNING thunder cannot be suppressed — the sound is always tied to the visual strike.
