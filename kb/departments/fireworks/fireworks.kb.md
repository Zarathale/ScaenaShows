---
department: Fireworks Director
owner: Mira
kb_version: 1.3
updated: 2026-03-27
notes: >
  v1.2: Full instrument inventory (5 event types), preset library, tone translation, department
  principles, capability status table; all four known gaps filed in ops-inbox.md.
  v1.3: Folder migration to kb/departments/fireworks/fireworks.kb.md. production-team.md §10
  Fireworks Director section written (was missing entirely). KB header reference corrected.
---

# Fireworks Director — Technical Knowledgebase

> Technical reference for the Fireworks department. Documents what the ScaenaShows Java plugin
> can do for fireworks — single launches, spatial patterns, and the preset library that names
> all rocket definitions.
>
> Creative direction for this role lives in `kb/production-team.md §10. Fireworks Director`.

---

## Mira

Mira runs the pyrotechnic end of the house with a sculptor's instincts — she thinks about a burst the way a painter thinks about negative space, as much about what surrounds it as what it is. Her primary orientation is altitude and timing: where the player is standing relative to a detonation is, to her, the most consequential decision in the show. Get that wrong and it doesn't matter how beautiful the color palette is.

She resolves pattern design and preset selection independently. When a pyrotechnic choice requires Effects to shift the player's altitude, or Lighting to retune the ambient register around a burst, she brings the proposal to the table — not a problem.

---

## Role Summary

- Owns all firework event types: single launches, spatial patterns (circle, line, fan, random scatter)
- Owns the fireworks.yml preset library — authoring, naming, and curation of reusable rocket definitions
- Owns pyrotechnic composition: which pattern type serves the beat, at what density, at what altitude
- Coordinates with the Lighting & Atmosphere Designer on color palette (fireworks are light sources) and on LIGHTNING timing (strikes land in the ambient world Lighting owns)
- Coordinates with the Effects Director on player altitude (a player inside vs. below vs. above a burst is a fundamentally different experience)

---

## Instrument Inventory

---

### Instrument 1 — The Single Rocket

**Java grounding:** `FIREWORK` event type → `FireworkEventExecutor.handleFirework()`

**What the audience experiences:** A single rocket launches from a specific world position, climbs for a moment, then detonates in a burst of color and shape. One punctuation mark.

**How to dial it:**

```yaml
type: FIREWORK
at: 240
preset: scae_star_warm
offset:
  x: 0
  y: 2       # Y offset — placement height within the offset coordinate system
  z: 0
y_mode: relative   # relative: Y offset from anchor's feet
                   # surface: Y offset from highest block at this XZ
```

> **⚠️ YAML field note:** The FIREWORK single event uses a **nested `offset:` map** (x / y / z)
> rather than flat `x_offset` / `y_offset` / `z_offset` fields. The inline syntax also works:
> `offset: {x: 0, y: 18, z: 0}`. The `y_mode` field is top-level, not inside the offset map.
> This structure differs from the pattern events (CIRCLE, LINE, FAN, RANDOM), which use
> top-level `y_offset` + `y_mode` fields.

**y_mode: relative** — Y offset from the anchor's Y position. Use when you want bursts at a fixed altitude above the stage floor regardless of terrain (aerial shows, indoor scenes).

**y_mode: surface** — Y offset above the highest solid block at the XZ position. Use when the terrain varies and you want rockets to appear to rise from the ground.

**Power in the preset drives burst altitude.** Power 1 = bursts ~8–12 blocks up. Power 2 = ~16–20 blocks. Power 3 = high overhead. The player must be oriented upward (Camera dept — Mark) to see high-power bursts. A single at power 3 that the player can't see is wasted.

**Strengths:** Maximum precision. One rocket, one moment, one location. The intimate use — a single at close range, low power — is one of the most effective tools in the department.

**Limitations and gaps:**

- `min_clearance:` is parsed but **not applied at runtime** — the executor ignores it. See ops-inbox.md: *[java-gap] FIREWORK min_clearance parsed but not enforced*.
- Spawn position is entirely controlled by the event's `offset` and `y_mode` fields — the preset owns appearance only (power, stars, colors, trail, flicker).
- No power variation on single FIREWORK — that's a pattern feature.

**Storytelling contexts:** An arrival. A punctuation mark after a long silence. The only sound in the room. A single gold star in an otherwise dark sky. Use it when one burst means more than twelve.

---

### Instrument 2 — The Ring

**Java grounding:** `FIREWORK_CIRCLE` event type → `FireworkEventExecutor.handleCircle()`

**What the audience experiences:** N rockets arranged in a circle around the player, launching in sequence (chase) or simultaneously. Chase reads as a rotating crown of fire; simultaneous reads as an explosion.

**How to dial it:**

```yaml
type: FIREWORK_CIRCLE
at: 480
preset: scae_star_warm
radius: 8
count: 8
origin_offset: {x: 0, z: 0}
y_mode: relative
y_offset: 0
chase:
  enabled: true
  interval_ticks: 3          # ticks between each successive launch
  direction: FL              # FL = clockwise order; LF = counter-clockwise
power_variation: UNIFORM     # UNIFORM | RAMP_UP | RAMP_DOWN | ALTERNATE | RANDOM
color_variation: UNIFORM     # UNIFORM | RAINBOW | GRADIENT | ALTERNATE
```

**Power variations:**

| Value | Behavior |
|-------|----------|
| `UNIFORM` | All positions use the preset's base power |
| `RAMP_UP` | Power scales from 1 → max(basePower, 3) across positions |
| `RAMP_DOWN` | Power scales from max(basePower, 3) → 1 across positions |
| `ALTERNATE` | Alternates between basePower and max(1, basePower−1) per position — creates rhythmic high/low pattern |
| `RANDOM` | Each position gets a random power from 1 to max(basePower, 3) |

**Color variations:**

| Value | Behavior |
|-------|----------|
| `UNIFORM` | All positions use the preset's primary colors unchanged |
| `RAINBOW` | Cycles through 7-color array (red → orange → yellow → green → cyan → blue → violet) per position, overriding preset primary colors |
| `GRADIENT` | Interpolates between `gradient_from` and `gradient_to` hex values across N positions |
| `ALTERNATE` | Alternates between the preset's first and second star primary colors per position (falls back to UNIFORM if preset has only one star) |

**GRADIENT usage:**

```yaml
color_variation: GRADIENT
gradient_from: "#FF8800"     # warm orange at position 0
gradient_to: "#0044FF"       # cool blue at position N
```

`color_variation` overrides primary burst colors only. Trail, flicker, fade colors, and star shape are always taken from the preset unchanged.

**Strengths:** The most spatially expressive pattern. A chase ring envelops the player. Simultaneous reads as an event horizon. Small radius (2–4 blocks) with chase is the "surrounded" feeling; large radius (12–16 blocks) with simultaneous is the sky opening up.

**Limitations:** No per-arm independent color or power control — that's FAN territory. The ring is homogeneous across all positions (except via color/power variation).

**Storytelling contexts:** A coronation. The player standing at the center of something important. An overwhelming moment. A birthday ring at arm's length. The finale crown above the stage.

---

### Instrument 3 — The Line

**Java grounding:** `FIREWORK_LINE` event type → `FireworkEventExecutor.handleLine()`

**What the audience experiences:** N rockets along a directional line in the world. Chase creates a moving wall or wave of fire sweeping across the stage. Simultaneous creates a line of simultaneous detonations — a curtain of color.

**How to dial it:**

```yaml
type: FIREWORK_LINE
at: 600
preset: scae_ball_spark
count: 5
start_offset: {x: -6, z: 0}   # where the line begins (relative to anchor)
angle: 90                       # compass bearing: 0=north(-Z), 90=east(+X), 180=south(+Z), 270=west(-X)
length: 12                      # total line length in blocks
y_mode: relative
y_offset: 1
chase:
  enabled: true
  interval_ticks: 4
  direction: FL                 # FL = start→end; LF = end→start
power_variation: RAMP_UP        # ascending altitude left-to-right
color_variation: UNIFORM
```

**Angle convention:** Compass bearing, clockwise from north. 0 = north (−Z), 90 = east (+X), 180 = south (+Z), 270 = west (−X). This is the inverse of standard math/trig convention. Always use compass bearing here.

**RAMP_UP / RAMP_DOWN on a line** creates a visible arc of altitude — rockets at the start are low, rockets at the end are high (or vice versa). The power cap in the engine is max(basePower, 3), so RAMP_UP scales between 1 and max(basePower, 3) across count.

**Strengths:** Directional and theatrical. A sweep from stage left to stage right. A wave cresting over the player. The start→end directionality is legible to the player.

**Limitations and gaps:**

- `color_variation: GRADIENT` on a line **always defaults to red→blue**, ignoring any `gradient_from` / `gradient_to` fields. `FireworkLineEvent` does not parse those fields; the executor passes `null, null` to the gradient interpolator, which falls back to its hardcoded defaults. Use RAINBOW or ALTERNATE for reliable multi-color line effects. See ops-inbox.md: *[java-gap] FIREWORK_LINE: gradient color variation defaults to red→blue*.
- No FAN-style per-segment presets — the line is always one preset.

**Storytelling contexts:** A procession. A reveal sweeping across the audience's field of view. A cascade of light running the length of the stage. An approach.

---

### Instrument 4 — The Fan

**Java grounding:** `FIREWORK_FAN` event type → `FireworkEventExecutor.handleFan()`

**What the audience experiences:** Multiple arms radiating from a common origin, each with its own preset, angle, count, and length. Asymmetric, directional, more complex than a circle.

**How to dial it:**

```yaml
type: FIREWORK_FAN
at: 720
origin_offset: {x: 0, z: 0}
y_mode: relative
y_offset: 1
arms:
  - preset: scae_star_warm
    count: 4
    angle: 45        # northeast
    length: 8
  - preset: scae_burst_leaf
    count: 4
    angle: 135       # southeast
    length: 8
  - preset: scae_star_warm
    count: 4
    angle: 225       # southwest
    length: 8
  - preset: scae_burst_leaf
    count: 4
    angle: 315       # northwest
    length: 8
chase:
  enabled: true
  interval_ticks: 3
  direction: FL               # sequential across all arm positions combined
```

**Behavioral note on simultaneous mode:** When `chase` is disabled (or `mode: simultaneous`), the executor fires all positions at once — the "per-arm simultaneous" distinction from the spec is collapsed into a global simultaneous launch. True per-arm independent sequencing is not implemented.

**Behavioral note on chase:** When `chase` is enabled, positions are chased sequentially across all arms combined (arm 1 pos 1 → arm 1 pos 2 → … → arm 2 pos 1 → …).

**Limitations and gaps:**

- **No `power_variation` or `color_variation` on FAN.** `FireworkFanEvent` has neither field. The executor always uses `preset.power()` with no color override. Each arm position launches at its preset's unmodified power and colors. There is no energy arc or color shift across the fan. See ops-inbox.md: *[java-gap] FIREWORK_FAN: no power_variation or color_variation*.
- Per-arm independent chase (arm 1 chases while arm 2 chases) is not implemented.

**Strengths:** The most compositionally flexible pattern. Each arm can use a different preset, allowing multi-color asymmetric designs. V-shapes (2 arms), X/+ shapes (4 arms), or irregular configurations are all possible.

**Storytelling contexts:** A bloom. A sun. An asymmetric burst that reads as motion or direction. The moment when "everywhere at once" needs to feel deliberate rather than random.

---

### Instrument 5 — The Scatter

**Java grounding:** `FIREWORK_RANDOM` event type → `FireworkEventExecutor.handleRandom()`

**What the audience experiences:** N rockets at uniformly random XZ positions within a circle. All launch simultaneously. Organic, unpredictable, festive.

**How to dial it:**

```yaml
type: FIREWORK_RANDOM
at: 840
preset: bday_confetti_ball
count: 12
radius: 6
origin_offset: {x: 0, z: 0}
y_mode: surface
y_offset: 0
seed: 42          # optional; omit for different pattern each run
```

**`seed`:** A fixed integer seed produces the same scatter pattern on every run — useful for shows that need repeatable visual design. Omit the field for organic variety each performance.

**Distribution:** Uses sqrt-weighted polar sampling, producing uniform density across the circle area. Without this correction, random points would cluster near the center — Mira says the executor handles this correctly.

**Strengths:** The fastest way to fill a space with energy. Celebrations. Confetti moments. A chaotic world-state. Works well at small radius for intimacy and large radius for visual field saturation.

**Limitations:** No chase (all simultaneous). No power_variation or color_variation. If you need sequential scatter or varied color, use multiple FIREWORK_CIRCLE or FIREWORK_LINE events instead.

**Storytelling contexts:** A celebration that has let go of precision. A birthday, a first day of spring, a crowd throwing their hats in the air.

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
```

> **Presets own appearance, not position.** Spawn position is entirely determined by the event's
> `offset` and `y_mode` fields. There is no `launch:` block — that field was removed in OPS-003.

### Star shape register

| Shape | Visual register |
|-------|----------------|
| `BALL` | Clean round burst — the workhorse; legible at any distance |
| `LARGE_BALL` | Larger, fuller sphere — finale energy, celebration |
| `STAR` | Geometric star points — ceremonial, sharp, precise |
| `BURST` | Radial spikes without a ball center — wild, scattering |
| `CREEPER` | Creeper-face silhouette — novelty, Minecraft-specific humor or menace |

**Multi-star presets** are supported — each star in the list fires as a separate burst layer on the same rocket. Use sparingly; complex presets can be visually muddy. Two carefully chosen stars are usually more effective than four.

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

When the Show Director says a tone phrase, Mira translates it into detonation choices:

**"Overwhelming"** → density and simultaneous timing. FIREWORK_RANDOM with count 12–20, or three pattern events on the same tick. The player cannot track individual bursts; they experience the whole field.

**"Building"** → RAMP_UP power_variation on a chase sequence, or a series of FIREWORK events at increasing `offset.y` over 60–120t. The eye is pulled upward.

**"Intimate"** → a single FIREWORK at close range (offset x/z 0–3), low power, no pattern. One rocket, one moment.

**"Earned"** → withheld, then released. Nothing for several sections, then a full pattern at the show's turning point. The audience hasn't been desensitized. See `demo.archetype_sampler` C7–C9 as the model.

**"Strange"** → mismatched scale or position. A single rocket launching from very close (`offset: {x: 0, y: 0, z: 0}`, y_mode: relative) rather than overhead. A FIREWORK_CIRCLE at radius 2 — close enough to feel surrounded rather than watched.

**"Rhythmic"** → ALTERNATE power_variation on a chase circle at short interval_ticks. The high/low alternation creates a pulse that the ear and eye track together.

**"Warm" vs. "Cool"** → palette first. Warm gold presets (scae_*) for earned, ceremonial, homey moments. Pride palette or custom GRADIENT blue→cyan for wonder, strangeness, shadow-adjacent.

---

## Department Principles

**Fireworks owns the moment, not the scene.** Rockets are punctuation — they launch, they burst, they're gone. The Fireworks Director does not set the ambient world (that's Lighting) or build a sustained texture (that's Sound). The department owns specific beats: the arrival, the release, the finale, the threat.

**Fireworks are light.** Every burst illuminates the world at player-eye for a fraction of a second. The color palette of a firework IS the lighting for that beat. Mira coordinates with the Lighting & Atmosphere Designer on color register — a warm gold burst in an otherwise cool-lit scene makes a statement. A burst that matches the ambient palette disappears into it.

**Altitude is dramaturgy.** Where a burst detonates relative to the player determines whether they look up at it (audience perspective), are surrounded by it (participant), or look down at it (elevated observer). The Effects department owns the player's position and altitude; the Fireworks Director designs the burst altitude relative to that position. These two departments must agree on where the player is when a pyrotechnic moment fires.

**Pattern choice is compositional, not structural.** A CIRCLE at radius 8 and a RANDOM with radius 8 fire the same number of rockets into roughly the same space — but CIRCLE is a ceremony and RANDOM is a celebration. The geometry of the pattern is an artistic choice, not a technical default.

**Escalation discipline:** Mira resolves pattern design and preset selection independently. She escalates to the Show Director when a pyrotechnic choice requires another department to change what a beat is — for example, if the designed burst altitude requires Effects to change player position, or if the required color palette is outside the current preset library's range.

---

## Calibration Backlog

Fireworks has five instrument types fully documented (SINGLE, CIRCLE, LINE, FAN, RANDOM). The calibration gap is named show-ready *arrangements* — specific configurations with tested parameters and storytelling intent. 📋 Proposed = named but not yet tested. ✅ Confirmed = tested, parameters known.

The `descent-through-fireworks` arrangement is the one confirmed pattern to date, documented in the Effects KB.

---

### `arrangement.punctuation` — 📋 Proposed
**Intent:** A single FIREWORK_RANDOM or FIREWORK (single), intimate use. One rocket, one moment. The most restrained tool in the department.
**Confirmed when:** Tested at close range (radius 4–8 blocks) and distant range (radius 16–20). Parameters that produce "one punctuation mark" vs. "background event" are labeled.

---

### `arrangement.crown` — 📋 Proposed
**Intent:** FIREWORK_CIRCLE with chase, radius 6–10 blocks, surrounding the player. The coronation feeling — the player at the center of something.
**Confirmed when:** Chase direction (FL vs. LF) tested. Radius that reads as "surrounding" vs. "distant" confirmed. Optimal count and power for the register documented.

---

### `arrangement.sweep` — 📋 Proposed
**Intent:** FIREWORK_LINE sweep across the stage — a moving wall or wave of fire crossing the scene. Direction, speed, and count calibrated for theatrical sweep vs. busy clutter.
**Confirmed when:** At least two reference sweeps tested (fast/sparse vs. slow/dense). Angle convention confirmed against compass bearing.

---

### `arrangement.burst.joy` — 📋 Proposed
**Intent:** FIREWORK_RANDOM scatter — celebration, abundance, the sky full of color. Not a directed pattern, a field. Archetype sampler C8 is the reference context.
**Confirmed when:** Count and radius that reads as "joyful abundance" without performance lag identified. C9 of the archetype sampler is the closest existing reference.

---

### `arrangement.finale.intimate` — 📋 Proposed
**Intent:** Layered close-range burst for a small but genuine culmination. Fewer than 15 total rockets. Designed for a scene where the moment is real but not overwhelming.
**Confirmed when:** Player reads the burst as finale without it overshadowing a quieter narrative beat.

---

### `arrangement.finale.full` — 📋 Proposed
**Intent:** The full climactic arrangement — multi-type, multi-event, all presets live at once. The archetype sampler C9 fireworks are a close reference; a confirmed "full finale" is larger and designed as a peak, not a descent sequence.
**Confirmed when:** Full arrangement tested in-game without lag. Optimal tick spacing between event types to avoid server spike documented.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| FIREWORK (single) | ✅ Verified | Uses nested `offset: {x, y, z}` map — not flat fields. Confirmed working in-game |
| FIREWORK_CIRCLE with chase | ✅ Verified | FL and LF direction confirmed; all color/power variations working |
| FIREWORK_LINE | ✅ Verified | Angle convention is compass bearing; RAMP_UP/DOWN power arc confirmed |
| FIREWORK_FAN | ✅ Verified | Cross-arm sequential chase working; per-arm independent sequencing not implemented |
| FIREWORK_RANDOM with seed | ✅ Verified | sqrt-weighted uniform distribution; reproducible with seed field |
| power_variation: RAMP_UP / RAMP_DOWN | ✅ Verified | Confirmed on CIRCLE and LINE; scales between 1 and max(basePower, 3) |
| power_variation: ALTERNATE | ✅ Verified | Alternates basePower / max(1, basePower−1) per position; confirmed in executor |
| power_variation: RANDOM | ✅ Verified | Per-position random between 1 and max(basePower, 3) |
| color_variation: RAINBOW | ✅ Verified | 7-color fixed cycle (R→O→Y→G→C→B→V); confirmed in executor |
| color_variation: GRADIENT | ✅ Verified (CIRCLE only) | hex interpolation confirmed on CIRCLE; not available on LINE or FAN |
| color_variation: ALTERNATE | ✅ Verified | Alternates between preset's first and second star primary colors per position |
| Multi-star presets | ✅ Verified | Multiple stars per rocket loops correctly in spawnFirework |
| Firework despawn-on-stop | ✅ Verified | Show stop cancels any pending BukkitRunnable chase tasks |
| FIREWORK_LINE color_variation: GRADIENT | ⚠️ Gapped | gradient_from/to not parsed on LINE; always defaults to red→blue. Filed: ops-inbox.md |
| FIREWORK_FAN power_variation / color_variation | ⚠️ Gapped | Not parsed or applied on FAN. All arm positions fire at preset.power() with no color override. Filed: ops-inbox.md |
| FIREWORK_FAN per-arm independent chase | ⚠️ Behavioral note | Simultaneous mode fires all positions at once; per-arm independent sequencing not implemented |
| FIREWORK min_clearance | ⚠️ Gapped | Field parsed but never read in handleFirework(). Silently ignored. Filed: ops-inbox.md |
| FIREWORK preset launch mode | ✅ Resolved | launch: block removed (OPS-003). Spawn position is the event's responsibility via offset/y_mode. |
