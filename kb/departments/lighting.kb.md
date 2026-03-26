---
department: Lighting & Atmosphere Designer
owner: Lighting & Atmosphere Designer
kb_version: 2.0
updated: 2026-03-25
notes: >
  Full department KB — role summary, instrument inventory with Java verification,
  tone translation, department principles, and capability status table.
  v2.0: added structural sections; corrected LIGHTNING YAML (offset: map, all relative).
---

# Lighting & Atmosphere Designer — Technical Knowledgebase

> Technical reference for the Lighting & Atmosphere department. Documents what the ScaenaShows
> Java plugin can do for time of day, weather, and lightning — the world-state events that affect
> all players — and how to access those capabilities through YAML.
>
> **Scope:** This department owns events that change the world state visible to all players.
> Particles, perceptual effects on the target player, and CAMERA screen distortion belong to
> the Effects department. See `kb/departments/effects.kb.md`.
>
> Creative direction for this role lives in `kb/production-team.md §6. Lighting & Atmosphere Designer`.

---

## Role Summary

- **World-state lighting.** Owns TIME_OF_DAY, WEATHER, and LIGHTNING — the three events that change the environment for all players simultaneously.
- **Gradual transitions** are composed by sequencing multiple TIME_OF_DAY events with deliberate tick intervals. There is no native fade; smoothness is authored.
- **Weather changes the ambient sound layer** automatically — every weather event is also a sound design event. Sound Designer coordination is mandatory.
- **Server-wide authority** means Lighting events affect every player on the server, not just show participants. This is the department's central discipline constraint.
- **Joint awareness with Fireworks** on color palette and with Effects on `night_vision` — Lighting doesn't author those instruments, but coordinates on any scene where they interact with the ambient light state.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| TIME_OF_DAY | Point | Snaps world time to a specified value |
| WEATHER | Bar | Sets weather state (clear / storm / thunder) |
| LIGHTNING | Point | Cosmetic lightning strike at an offset (no damage, no fire) |

**Not owned by this department:** PARTICLE (→ Effects), EFFECT on players (→ Effects), CAMERA screen distortion (→ Effects). If it is applied *to* a player rather than *to the world*, it belongs to Effects.

---

## Instruments

---

### World Clock — TIME_OF_DAY

**Java grounding:** `WorldEventExecutor.handleTimeOfDay()` → `world.setTime(e.time)`. Server-wide.

**What it does:** Instantly moves the sun/moon to the specified time value, changing sky color, ambient brightness, and shadow angle. The world clock does not run between shows unless the plugin or server triggers it — TIME_OF_DAY gives full control over what the sky looks like at any show moment.

**How to dial it:**

```yaml
type: TIME_OF_DAY
time: 18000   # integer 0–24000
```

**Time value reference:**

| Value | Time of day | Light quality |
|-------|-------------|---------------|
| 0 (= 24000) | Sunrise | First light returning, warm horizon |
| 1000 | Early morning | Clarity, long shadows, gentle |
| 6000 | Noon | Full daylight, maximum visibility |
| 8000 | Mid-afternoon | Warm, slightly angled |
| 12000 | Sunset | Warmth fading, golden-orange |
| 13000 | Dusk | Liminal — the moment just before dark |
| 18000 | Midnight | Full dark, deepest night |

**Gradual transition — compose by sequencing:**

```yaml
# C1 — Gradual dusk across 80 ticks (R7 archetype sampler pattern)
- at: 0
  type: TIME_OF_DAY
  time: 8000       # mid-afternoon — warm, established

- at: 40
  type: TIME_OF_DAY
  time: 13000      # dusk — the world beginning to change

- at: 80
  type: TIME_OF_DAY
  time: 18000      # night — arrived
```

Three steps across 80 ticks reads as continuous fade at show scale. Finer transitions (5–6 steps) are nearly imperceptible as steps. Coarser (2 steps) read as abrupt shifts that can be used deliberately for a snap effect.

**Storytelling contexts:**
- Opening world dimming during dialogue — the sky doing the emotional work before anything else moves
- Sunrise at the earned moment — 18000 → 0 as the culminating lighting beat
- False noon: 6000 at a moment of exposure or confrontation — too bright, nowhere to hide
- Staying at 13000 through a long liminal scene — never fully dark, never day, permanently in-between

**Limitations:**
- Instant snap only — no native fade. Gradual transitions require manually sequenced steps.
- Server-wide: affects all players. Not suitable for private lighting in a multi-player server context.
- Underground or fully enclosed spaces: sky time has no visible effect. Block light level governs ambient light there; TIME_OF_DAY does nothing in caves.
- `world.setTime()` does not restore automatically. Document the show's original time and fire a restore TIME_OF_DAY (or COMMAND) at show end if needed.

---

### Weather System — WEATHER

**Java grounding:** `WorldEventExecutor.handleWeather()` → Bukkit `world.setStorm()` + `world.setThundering()`. Server-wide.

**What it does:** Changes the world's precipitation state and adds or removes the corresponding ambient sound layer. Three states:
- `clear` — no precipitation, full skybox, natural quiet ambient
- `storm` — rain (or snow in cold biomes), reduced visibility, continuous rain ambient bed
- `thunder` — storm + active thundering; adds random server-generated thunder strikes to the ambient sound (distinct from scripted LIGHTNING events)

**How to dial it:**

```yaml
type: WEATHER
state: clear        # clear | storm | thunder
duration_ticks: 400  # optional; omit for persistent
```

**Java implementation notes:**
- `clear` → `setStorm(false)` + `setThundering(false)`
- `storm` → `setStorm(true)` + `setThundering(false)` — rain only, no thunder rumble
- `thunder` → `setStorm(true)` + `setThundering(true)` — uses `setThunderDuration()` for duration
- `duration_ticks` defaults to `-1` (omitted = persistent). Duration is skipped if ≤ 0.

**Common setup pattern:**

```yaml
# T=0: guarantee known starting state
- at: 0
  type: WEATHER
  state: clear

# Later: build to storm
- at: 600
  type: WEATHER
  state: storm

# End of scene: weather clears
- at: 1200
  type: WEATHER
  state: clear
  duration_ticks: 200
```

**Storytelling contexts:**
- `clear` at T=0 as a ritual setup — the world made known before anything begins
- Storm building mid-show as tension rises — the ambient sound doing emotional work before any scripted sound fires
- A `clear` break after a storm section: the world opening, pressure releasing, wonder arriving
- `thunder` for large-scale dread — uncontrolled but powerful when the scene calls for a sky in chaos

**Limitations:**
- Server-wide: same scope consideration as TIME_OF_DAY.
- `thunder` adds random server-generated thunder strikes that cannot be controlled or timed. If the Sound Designer needs a clean ambient bed, use `storm` instead.
- Ambient rain/thunder sound competes with scripted SOUND events. Coordinate with Sound Designer on every weather state that introduces or removes a sound bed.
- No partial weather (fog, light drizzle) — only the three vanilla states are available.
- Duration: when set, the weather reverts after `duration_ticks`. When omitted, the new state persists until another WEATHER event or natural server behavior. Document at show end.

---

### Lightning Strike — LIGHTNING

**Java grounding:** `VisualEventExecutor.handleLightning()` → `world.strikeLightningEffect(strike)`. Cosmetic only — no damage, no block ignition, no fire.

**What it does:** Fires a single visual lightning bolt at an offset from the spatial anchor. The bolt appears and disappears instantly. A thunder crack tied to the strike plays at the offset location. Multiple LIGHTNING events on the same tick create simultaneous multi-strike effects.

**How to dial it:**

```yaml
type: LIGHTNING
offset:
  x: 5      # offset from anchor X
  y: 0      # offset from anchor Y (NOT absolute — relative to anchor)
  z: 0      # offset from anchor Z
```

> ⚠️ **All three offset fields are relative to the spatial anchor** — including Y. There is no absolute coordinate mode for LIGHTNING. To strike near the stage, use the terrain height *relative to the anchor's Y* — i.e., if the anchor is at Y=64 and the ground is at Y=64, use `y: 0`.

**Timing patterns:**

```yaml
# Single punctuation strike at a precise moment
- at: 480
  type: LIGHTNING
  offset: {x: 0, y: 0, z: 5}

# Ominous slow sequence — sky threatening
- at: 200
  type: LIGHTNING
  offset: {x: 4, y: 0, z: 2}
- at: 320
  type: LIGHTNING
  offset: {x: -3, y: 0, z: 6}
- at: 460
  type: LIGHTNING
  offset: {x: 1, y: 0, z: -4}

# Chaos burst — world breaking open (same tick, different positions)
- at: 800
  type: LIGHTNING
  offset: {x: 5, y: 0, z: 0}
- at: 800
  type: LIGHTNING
  offset: {x: -5, y: 0, z: 3}
- at: 800
  type: LIGHTNING
  offset: {x: 0, y: 0, z: -6}
```

**Storytelling contexts:**
- A single strike as sky punctuation at a climactic beat — the world registering the moment
- Slow ominous sequence during a long tension hold — the storm arriving in installments
- LIGHTNING in clear weather (`WEATHER: clear`) — atmospheric wrongness; the sky misbehaving without rain
- Multi-strike chaos at a world-breaking moment in the show arc
- Paired with Fireworks finale — sky + ground illumination simultaneously (coordinate with Fireworks Director)

**Limitations:**
- Thunder cannot be suppressed — every strike fires a thunder crack. The Sound Designer must treat every LIGHTNING event as an uncontrolled sound event.
- Cosmetic only: `strikeLightningEffect()` not `strikeLightning()` — no damage to entities, no block fires, no charged creepers.
- Offset-only positioning: no absolute coordinate targeting. Precision requires knowing the terrain height relative to the anchor.
- The visual strike is instantaneous — there is no multi-frame animation or duration.

---

## Fireworks as Light

Fireworks are owned by the Fireworks Director — the Lighting & Atmosphere Designer does not author FIREWORK_* events. But every burst illuminates the player's world for a moment, making pyrotechnic color palette a shared concern.

The Lighting Designer is consulted on firework palette choices that affect the ambient scene register: a warm gold burst in a cool-lit scene makes a statement; a matching burst disappears into it. See `kb/departments/fireworks.kb.md` for firework authoring reference.

When LIGHTNING timing aligns with pyrotechnic moments, both departments agree on what that beat is doing — neither overrides the other.

---

## Tone Translation

How the Lighting & Atmosphere Designer interprets the Show Director's tone language.

**"Tender"**
Lighting reads tender as warm, present, and unhurried. Time values in the range 8000–12000 (mid-afternoon to golden sunset) — light that exists without demanding attention. Clear weather with no ambient sound bed competing. Avoid both harsh noon brightness and full darkness; the light should feel like late afternoon through a window. If the scene needs intimacy, hold at 12000–13000 and let the warmth of the approaching dark do the work.

**"Overwhelming / earned"**
This is a lighting arc call. Darkness first: hold at 18000 (midnight) through the build. The earned moment arrives as light — either sunrise (0) breaking through, or a clear break after storm. The overwhelming beat in Lighting is the world becoming visible again. Time the transition to land with the show's key arrival moment; don't use it early. A storm breaking into clear is equally valid and often more abrupt — earned through release rather than gradual return.

**"Strange / uncanny"**
Lighting makes strange through mismatch: full daylight during an ominous monologue, or a LIGHTNING strike in clear weather with no storm. The world behaving incorrectly registers as wrong before the player consciously identifies why. A single LIGHTNING event during a still clear scene at a moment that doesn't earn it is one of the most cost-effective strange beats available. Alternatively: a scene that holds at 13000 (dusk) without ever resolving to night — permanently liminal.

**"Delight / surprise"**
Lighting reads delight as the world doing something unexpected and charming. A quick TIME_OF_DAY snap from night to bright noon mid-scene — the sudden blaze of full daylight where darkness was. A LIGHTNING strike in cloudless clear weather — the sky producing a flash with no reason. A storm that clears in two ticks rather than gradually, the world suddenly open and bright. Delight in Lighting is about the world catching the player off-guard in a way that makes them smile rather than flinch. Early morning (1000) carries a natural "good surprise" quality — first light, the world newly made and energized.

**"Joy / abundant"**
Full daylight (6000) or early morning (1000). Clear weather. The world at maximum visibility and warmth. If the show has been holding darkness, the return to daylight should feel like breathing out. Time the return to full light to land with — or slightly before — the joy beat, so the world is already celebrating when it arrives.

**"Wonder"**
Liminal time: dusk (13000) or false dawn (approaching 0). The sky in mid-transition is the most visually alive state. A weather shift — storm clearing to clear — paired with liminal time creates a sky that is moving and uncertain. LIGHTNING in clear weather for a single strange beat of the sky being alive. Wonder in Lighting is about the sky doing something the audience doesn't expect — moving when it should be still, bright when it should be dark.

**Signaling back to the Director:** When a tone phrase is ambiguous for Lighting, the clarifying question is: *"Does this scene live primarily in darkness, in transition, or in light?"* That single answer resolves most tonal ambiguity — delight and wonder can both arrive as surprise, but delight is quick and immediate while wonder is slow and uncertain.

---

## Department Principles

**What Lighting is ultimately for:** Lighting is the emotional context that everything else lives inside. It doesn't draw attention to itself — it makes or breaks the register of every other department's work. A Choreography cue that works in daylight can dissolve in darkness; a Voice line that reads in silence can be swamped by rain ambience. Lighting decides the container.

**What Lighting decides independently:**
- The lighting arc: the opening light state, key transition moments, and closing state
- Step intervals and sequence design for gradual TIME_OF_DAY transitions
- LIGHTNING placement, timing, and pattern (single, sequence, chaos burst)
- Whether to reset world state at show end

**What requires Show Director sign-off:**
- Ending a show with the world permanently in a changed state (leaving midnight or storm is a story call)
- Any weather state that will be audible to the full server during the show — Sound Designer and Director should both be aware
- A dramatic LIGHTNING sequence at a pivotal moment — the strike pattern is an authoring decision but its placement in the arc is a Director call

**Cross-department coordination:**

*With Sound Designer:* Every weather state change is an unsolicited sound event. `storm` adds a rain bed; `thunder` adds random rumbles; `clear` removes both. The Sound Designer must know the full weather sequence before writing their score. This is Lighting's most important coordination relationship — file the weather arc with Sound at brief time, not after YAML is written.

*With Effects:* The `night_vision` effect (Effects authority) makes darkness visible — which may or may not be what the Lighting Designer intended for a dark scene. Any scene where `night_vision` is active requires both departments to agree that the revealed environment is intentional. Lighting designs the dark; Effects decides whether the player can see it.

*With Fireworks:* FIREWORK bursts are transient light sources. Color palette coordination means the Lighting Designer is consulted on whether a burst color reinforces or disrupts the ambient light register. LIGHTNING timing: if a LIGHTNING strike accompanies a pyrotechnic peak, both Fireworks and Lighting agree on that beat together.

*With Stage Manager:* TIME_OF_DAY and WEATHER are world-state changes that persist after show end unless restored. Every show that changes time or weather must document the pre-show state and fire a restore event (WEATHER clear + TIME_OF_DAY to original) at show end. This is part of the Stop Safety cleanup contract. Flag to Stage Manager at brief time if world-state restoration is needed.

*With Show Director:* Because Lighting's instruments are server-wide, any dramatic use during a shared-server event needs Director awareness. This is an operational call, not just a creative one.

**Handling capability gaps:** The three core instruments are all verified and functional. There are currently no filed gaps for Lighting. If a future need arises (e.g., per-player lighting, fog, gradual fade), file with Stage Management before authoring any workaround.

**Escalation discipline:** Lighting resolves arc and instrument decisions independently. Lighting escalates when: (1) a scene's light state conflicts with another department's authored work in a way that can't be resolved through timing adjustments — bring to Director and Stage Manager; (2) a weather event will have server-wide audience impact that hasn't been cleared with the Director; (3) world-state restoration at show end conflicts with another show's expected start state — Stage Manager arbitrates.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| TIME_OF_DAY — world clock snap | ✅ Verified | `world.setTime()` in `WorldEventExecutor`; server-wide; instant |
| TIME_OF_DAY — gradual transition (multi-step) | ✅ Verified | Composed by sequencing multiple events; no native fade |
| WEATHER — clear / storm / thunder states | ✅ Verified | `world.setStorm()` + `world.setThundering()` in `WorldEventExecutor` |
| WEATHER — duration_ticks (auto-revert) | ✅ Verified | Calls `setWeatherDuration()` / `setThunderDuration()` when > 0; -1 = persistent |
| LIGHTNING — cosmetic strike at offset | ✅ Verified | `world.strikeLightningEffect()` in `VisualEventExecutor`; no damage, no fire |
| Per-player time of day or weather | 📋 Aspirational | Not available in vanilla Minecraft — world-state only |
| Gradual fade (native) | 📋 Aspirational | Not implemented; must be composed via multi-step sequences |
| Fog or atmospheric haze | 📋 Aspirational | Not available without resource pack; vanilla Minecraft only |
