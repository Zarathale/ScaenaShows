---
department: Fireworks Director
owner: Fireworks Director
kb_version: 1.0
updated: 2026-03-25
---

# Fireworks Director — Technical Knowledgebase

> Technical reference for the Fireworks department. Documents what the ScaenaShows Java plugin
> can do for fireworks — single launches, spatial patterns, and the preset library that names
> all rocket definitions.
>
> Creative direction for this role lives in `kb/production-team.md §10. Fireworks Director`.

---

## Role Summary

- Owns all firework event types: single launches, spatial patterns (circle, line, fan, random scatter)
- Owns the fireworks.yml preset library — authoring, naming, and curation of reusable rocket definitions
- Owns pyrotechnic composition: which pattern type serves the beat, at what density, at what altitude
- Coordinates with the Lighting & Atmosphere Designer on color palette (fireworks are light sources) and on LIGHTNING timing (strikes land in the ambient world Lighting owns)
- Coordinates with the Effects Director on player altitude (a player inside vs. below vs. above a burst is a fundamentally different experience)

---

## Capabilities & YAML Syntax

---

### FIREWORK — single launch

A single rocket at a specified offset from the spatial anchor. The simplest pyrotechnic tool.

```yaml
type: FIREWORK
preset: scae_star_warm       # ID from fireworks.yml
x_offset: 0                  # XZ position relative to anchor
z_offset: 3
y_mode: relative             # relative | surface
y_offset: 2                  # Y above anchor (relative) or above terrain surface
```

**y_mode: relative** — Y offset from the anchor's Y position. Good for aerial scenes where you want bursts at a fixed altitude above the stage floor regardless of terrain.

**y_mode: surface** — Y offset above the terrain surface at the XZ position. Good for outdoor scenes where the terrain is uneven and you want bursts to appear to rise from the ground.

**Power in the preset drives burst altitude.** Power 1 = bursts ~8–12 blocks up. Power 2 = ~16–20 blocks. Power 3 = high overhead. The player needs to be oriented upward (see Effects department — Camera specialty) to see high-power bursts.

---

### FIREWORK_CIRCLE — ring pattern

Launches N rockets equally spaced around a circle of a given radius.

```yaml
type: FIREWORK_CIRCLE
preset: scae_star_warm
radius: 8                    # blocks from origin_offset center
count: 8                     # number of launch positions
origin_offset: { x: 0, z: 0 }
y_mode: relative
y_offset: 0
chase:
  enabled: true
  interval_ticks: 3          # ticks between each successive launch
  direction: F               # F = clockwise order; LF = counter-clockwise
power_variation: UNIFORM     # UNIFORM | RAMP_UP | RAMP_DOWN | ALTERNATE | RANDOM
color_variation: UNIFORM     # UNIFORM | RAINBOW | GRADIENT | ALTERNATE
```

**Chase** is the primary expressive parameter for patterns. A chase ring feels like a rotating crown of fire — each position detonates in sequence. Simultaneous (chase disabled) reads as an explosion.

**color_variation: RAINBOW** — each successive position cycles through red → orange → yellow → green → cyan → blue → violet. Full circle of 7 positions = one complete rainbow cycle.

**color_variation: GRADIENT** — color interpolates between two hex values across the count:
```yaml
color_variation: GRADIENT
gradient_from: "#FF8800"
gradient_to: "#0044FF"
```

---

### FIREWORK_LINE — directed line pattern

Launches N rockets evenly spaced along a line at a compass bearing from a start offset.

```yaml
type: FIREWORK_LINE
preset: scae_ball_spark
count: 5
start_offset: { x: -6, z: 0 }  # where the line begins
angle: 90                        # compass bearing (0 = north, 90 = east, 180 = south, 270 = west)
length: 12                       # total length of the line in blocks
y_mode: relative
y_offset: 1
chase:
  enabled: true
  interval_ticks: 4
  direction: F                   # F = start → end; LF = end → start
power_variation: RAMP_UP         # good for building energy left-to-right
```

**Angle convention:** compass bearing. 0 = north (−Z), 90 = east (+X), 180 = south (+Z), 270 = west (−X). This is the same bearing convention as the stage's `front:` field.

**RAMP_UP / RAMP_DOWN power_variation** on a line creates a visible arc of altitude — rockets at the start are low, rockets at the end are high (or vice versa). The power cap in the engine is max(basePower, 3), so RAMP_UP scales between 1 and max(basePower, 3).

---

### FIREWORK_FAN — multi-arm pattern

Launches arms radiating from a common origin. Each arm has its own preset, count, angle, and length. A fan can be 2 arms (V shape), 4 arms (X or +), or any N-arm configuration.

```yaml
type: FIREWORK_FAN
origin_offset: { x: 0, z: 0 }
y_mode: relative
y_offset: 1
arms:
  - preset: scae_star_warm
    count: 4
    angle: 45        # northeast
    length: 8
  - preset: scae_star_warm
    count: 4
    angle: 135       # southeast
    length: 8
  - preset: scae_star_warm
    count: 4
    angle: 225       # southwest
    length: 8
  - preset: scae_star_warm
    count: 4
    angle: 315       # northwest
    length: 8
chase:
  enabled: true
  interval_ticks: 3
  direction: F               # sequential across all arm positions combined
```

**Behavioral note:** When `chase` is enabled and direction is F, positions are chased sequentially across all arms combined (arm 1 pos 1 → arm 1 pos 2 → ... → arm 2 pos 1 → ...). When chase is disabled, all positions launch simultaneously. The per-arm "simultaneous" mode specified in the schema is treated as a simultaneous launch for all arms — per-arm sequential chasing is not currently implemented. File to ops-inbox if per-arm independent chase is needed for a production.

---

### FIREWORK_RANDOM — scatter pattern

Launches N rockets at uniformly random XZ positions within a radius circle. All launch simultaneously. No chase.

```yaml
type: FIREWORK_RANDOM
preset: bday_confetti_ball
count: 12
radius: 6
origin_offset: { x: 0, z: 0 }
y_mode: surface
y_offset: 0
seed: 42                     # optional; omit for different pattern each run
```

**seed:** A fixed integer seed produces the same scatter pattern on every run — useful for shows that need to look the same each performance. Omit for organic variety.

**Distribution:** uses sqrt-weighted polar sampling, producing uniform density across the circle area. Without this correction, random points would cluster near the center.

---

## The Preset Library — fireworks.yml

Presets live in `src/main/resources/fireworks.yml`. They are named rocket definitions — power, star shapes, colors, trail, and flicker. Every FIREWORK_* event references a preset by ID.

### Preset structure

```yaml
preset_id:
  display_name: "Human Readable Name"
  power: 1            # 1–3; higher = higher burst altitude
  stars:
    - type: BALL      # BALL | LARGE_BALL | STAR | BURST | CREEPER
      colors: ["#RRGGBB", ...]    # primary burst colors (hex)
      fades: ["#RRGGBB", ...]     # fade-to colors (optional)
      trail: true                 # spark trail on ascent
      flicker: true               # twinkle on burst
  launch:
    mode: above       # above | random | feet
    y_offset: 1.2
    spread: 2.0       # only for mode: random
```

**Multi-star presets** are supported — each star in the list fires as a separate burst layer on the same rocket. Use sparingly; complex presets can be visually muddy.

### Star shape register

| Shape | Visual register |
|-------|----------------|
| `BALL` | Clean round burst — the workhorse; legible at any distance |
| `LARGE_BALL` | Larger, fuller sphere — finale energy, celebration |
| `STAR` | Geometric star points — ceremonial, sharp, precise |
| `BURST` | Radial spikes without a ball center — wild, scattering |
| `CREEPER` | Creeper-face silhouette — novelty, Minecraft-specific humor or menace |

### Current preset families

| Family | IDs | Palette | Register |
|--------|-----|---------|----------|
| Scaena | `scae_star_warm`, `scae_burst_leaf`, `scae_large_fanfare`, `scae_ball_spark` | Gold + green + white | Brand / ceremonial |
| Birthday Pastels | `bday_pastel_star`, `bday_pastel_burst`, `bday_confetti_ball`, `bday_finale_large` | Soft pink, blue, lavender, yellow | Celebration / warmth |
| Pride | `pride_wave_star`, `pride_burst_rainbow`, `pride_trans_glow_ball`, `pride_finale_large` | Rainbow / trans colors | Pride / joy / community |

### When to add a new preset vs. use color_variation

Add a preset when the color identity needs to be named and reusable — "the gold finale rocket" is a preset. Use `color_variation: GRADIENT` or `RAINBOW` on an existing preset when color is a composition choice within a single pattern event, not a named identity.

---

## Tone Translation

When the Show Director says a tone phrase, the Fireworks Director translates it into detonation choices:

**"Overwhelming"** → density and simultaneous timing. FIREWORK_RANDOM with count 12–20, or three pattern events on the same tick. The player cannot track individual bursts; they experience the whole field.

**"Building"** → RAMP_UP power_variation on a chase sequence, or a series of FIREWORK events at increasing y_offset over 60–120t. The eye is pulled upward.

**"Intimate"** → a single FIREWORK at close range (x_offset 0–3), low power, no pattern. One rocket, one moment.

**"Earned"** → withheld, then released. Nothing for several sections, then a full pattern at the show's turning point. The audience hasn't been desensitized. See `demo.archetype_sampler` C7–C9 as the model.

**"Strange"** → mismatched scale or position. A single rocket launching from under the player's feet (y_mode: feet) rather than overhead. A FIREWORK_CIRCLE at radius 2 — close enough to feel surrounded.

---

## Department Principles

**Fireworks owns the moment, not the scene.** Rockets are punctuation — they launch, they burst, they're gone. The Fireworks Director does not set the ambient world (that's Lighting) or build a sustained texture (that's Sound). The department owns specific beats: the arrival, the release, the finale, the threat.

**Fireworks are light.** Every burst illuminates the world at player-eye for a fraction of a second. The color palette of a firework IS the lighting for that beat. The Fireworks Director coordinates with the Lighting & Atmosphere Designer on color register — a warm gold burst in an otherwise cool-lit scene makes a statement. A burst that matches the ambient palette disappears into it.

**Altitude is dramaturgy.** Where a burst detonates relative to the player determines whether they look up at it (audience perspective), are surrounded by it (participant), or look down at it (elevated observer). The Effects department owns the player's position and altitude; the Fireworks Director designs the burst altitude relative to that position. These two departments must agree on where the player is when a pyrotechnic moment fires.

**Escalation discipline:** The Fireworks Director resolves pattern design and preset selection independently. Escalates to the Show Director when a pyrotechnic choice requires another department to change what a beat is — for example, if the designed burst altitude requires Effects to change player position.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| FIREWORK (single) | ✅ Verified | All fields implemented; confirmed working in-game |
| FIREWORK_CIRCLE with chase | ✅ Verified | F and LF direction confirmed; all color/power variations working |
| FIREWORK_LINE | ✅ Verified | Angle convention is compass bearing; RAMP_UP/DOWN power arc confirmed |
| FIREWORK_FAN | ✅ Verified | Cross-arm sequential chase working; see behavioral note on simultaneous mode |
| FIREWORK_RANDOM with seed | ✅ Verified | sqrt-weighted uniform distribution; reproducible with seed field |
| color_variation: RAINBOW | ✅ Verified | 7-color cycle confirmed in executor |
| color_variation: GRADIENT | ✅ Verified | hex interpolation confirmed in executor |
| Multi-star presets | ✅ Verified | Multiple stars per rocket loops correctly in spawnFirework |
| FIREWORK_FAN per-arm simultaneous chase | ⚠️ Behavioral note | Treated as all-positions-simultaneous; true per-arm independent sequencing not implemented |
| Firework despawn-on-stop | ✅ Verified | Show stop cancels any pending BukkitRunnable chase tasks |
