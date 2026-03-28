---
department: Lighting & Atmosphere Designer
owner: Steve N.
kb_version: 2.1
updated: 2026-03-26
notes: >
  v2.0: Role summary, instrument inventory with Java verification, tone translation,
  department principles, capability status table. LIGHTNING YAML corrected (offset: map, all relative).
  v2.1: Steve N. named head. Sky Arc Reference expanded. Arc Design section added.
  Folder migration complete. Effects KB path corrected.
---

# Lighting & Atmosphere Designer — Technical Knowledgebase

> Technical reference for the Lighting & Atmosphere department. Documents what the ScaenaShows
> Java plugin can do for time of day, weather, and lightning — the world-state events that affect
> all players — and how to access those capabilities through YAML.
>
> **Scope:** This department owns events that change the world state visible to all players.
> Particles, perceptual effects on the target player, and CAMERA screen distortion belong to
> the Effects department. See `kb/departments/effects/effects.kb.md`.
>
> Creative direction for this role lives in `kb/production-team.md §6. Lighting & Atmosphere Designer`.

---

## Steve N.

Steve N. works at arc scale. His instinct is to establish the show's full light journey —
opening state, key transitions, closing state — before he authors a single event. Individual
cues are positioned in service of that arc; the arc is the instrument. He understands that
Lighting is the container for every other department's work, and he takes that seriously:
a Choreography cue that lands in daylight can dissolve in darkness; a Voice line that reads
in silence can be swamped by rain. Steve's arc decisions are made early and shared with the
full team at brief time.

Steve is the most server-wide-aware member of the production team. His instruments affect
every player on the server, not just show participants. He doesn't use dramatic time or
weather changes without Show Director clearance and a plan for what other players experience.

Steve escalates to the Director when a lighting decision has server-wide impact that hasn't
been cleared, or when another department's planned work depends on a light state that
conflicts with his arc. He brings the conflict with a proposed resolution.

---

## Role Summary

- **World-state lighting.** Owns TIME_OF_DAY, WEATHER, and LIGHTNING — the three events that change the environment for all players simultaneously.
- **Gradual transitions** are composed by sequencing multiple TIME_OF_DAY events with deliberate tick intervals. There is no native fade; smoothness is authored.
- **Weather changes the ambient sound layer** automatically — every weather event is also a sound design event. Sound Designer coordination is mandatory.
- **Server-wide authority** means Lighting events affect every player on the server, not just show participants. This is the department's central discipline constraint.
- **Joint awareness with Fireworks** on color palette and with Effects on `night_vision` — Lighting doesn't author those instruments, but coordinates on any scene where they interact with the ambient light state.

---

## Arc Design

Steve designs the lighting arc before authoring any events. The arc is the answer to: *what
does the world's light do across the full duration of this show?*

### Questions to answer at show design time

1. **Opening state:** What time and weather does the show begin in? Is it set deliberately, or inherited from the server state?
2. **Key transitions:** Where are the moments where light changes? What emotional function does each change serve?
3. **Lighting zones:** Does the show move through distinct lighting zones (day → dusk → night → dawn), or does the world hold a single state throughout?
4. **Closing state:** Does the show end in a different light state than it opened? Does that state need to be restored at show end?
5. **Weather arc:** Does weather change during the show? If so, when, and what does each state do for the ambient sound bed?
6. **LIGHTNING moments:** Are there scripted strikes? Where in the arc, and what do they punctuate?
7. **Restoration:** TIME_OF_DAY and WEATHER persist after show end unless reset. Document pre-show state and author restore events.

### Common arc patterns

**Static mood** — a single TIME_OF_DAY set at T=0 and held throughout. Use when the show's emotional register is consistent: a midnight show that never becomes day, a noon show that stays in relentless daylight. Simplest to manage; most risk if the server's current time is unexpected.

**Day-to-night** — building darkness as the show deepens. Typically: warm mid-afternoon opening → dusk transition at the emotional turn → full night at depth. The archetype sampler R7 uses this pattern at C1: 8000 → 13000 → 18000 across 80 ticks.

**Night-to-day (The Long Night pattern)** — sustained darkness → earned dawn. The show opens in or near midnight and holds through the dark arc. Dawn arrives as the culminating lighting beat — the world becoming light as the show resolves. Requires the full sky arc reference (see below). This is showcase.02's model.

**Storm-and-clear** — weather arc as emotional punctuation. Storm builds under tension; clear breaks at release or wonder. The Sound Designer must know this arc before scoring — rain and thunder beds change the ambient register dramatically.

**Liminal hold** — the world stuck in transition, never resolving. Holding at 13000 (dusk) throughout: not fully dark, not day. The sky permanently between states. Use for disorientation, strangeness, the uncanny.

---

## Sky Arc Reference — Full TIME_OF_DAY Vocabulary

Minecraft's world time runs 0–24000 (one full day). Values and their sky quality:

| Value | Real-world analog | Sky / light quality |
|-------|-------------------|---------------------|
| **0** | Sunrise | Sun cresting the horizon. Warm amber glow returning. First light — hope, beginning. |
| **500** | Early dawn | Sun just above horizon. Faint warmth, long shadows just forming. Fragile clarity. |
| **1000** | Morning (~7 AM) | Established daylight, still early. Shadows long, light warm and clear. Energy arriving. |
| **3000** | Mid-morning | Sun climbing. Full warm daylight, purposeful. |
| **6000** | Noon | Sun at zenith. Maximum brightness, harsh white-gold. No shadows. Nowhere to hide. |
| **8000** | Mid-afternoon | Warm, angled light. Softening. The most neutral "established" state. |
| **10000** | Late afternoon | Light thickening toward gold. The world beginning to slow. |
| **12000** | Sunset begins | Sun moving toward the horizon. Warmth fading, a coolness arriving. |
| **12500** | Golden hour | Rich amber-orange sky. The most visually dramatic warm state. |
| **13000** | Dusk | Sun at horizon. The liminal moment — neither day nor night. Sky in transition. |
| **13500** | Civil dusk | Sun just below horizon. Sky holding color — purple, deep blue-grey. Still not fully dark. |
| **14000** | Twilight | Stars becoming visible. Ambient brightness dropping sharply. |
| **15000** | Night established | Full night sky. Stars bright. Moon rising. The world quiet and dark. |
| **18000** | Midnight | Moon at zenith. Deepest dark. Maximum drama. |
| **20000** | After midnight | The long dark hours. Still, heavy. |
| **22000** | Pre-dawn (~4 AM) | The world at its coldest and most still. Darkness without any warmth returning yet. |
| **22500** | False dawn | A faint cold brightening at the horizon — not warmth, just the first absence of full dark. |
| **23000** | Early dawn | Sky beginning to lighten. The first hint of color returning — cool blue before warm. |
| **23500** | Dawn breaking | Horizon brightening. The world about to become visible again. The held breath. |
| **24000 / 0** | Sunrise | Full return of light. Same as 0 — the cycle completing. |

**The Long Night arc (showcase.02 reference):**
```yaml
# Opening state — deep dusk, the night beginning
- {at: 0, type: TIME_OF_DAY, time: 13500}

# Night establishing — world going fully dark
- {at: [section_2_start], type: TIME_OF_DAY, time: 15000}

# Midnight depth — the show's darkest point
- {at: [section_N_start], type: TIME_OF_DAY, time: 18000}

# Pre-dawn stillness — the cold quiet before light
- {at: [penultimate_start], type: TIME_OF_DAY, time: 22000}

# Dawn breaking — earned arrival of light
- {at: [final_section_start], type: TIME_OF_DAY, time: 23500}
- {at: [final_section_start + 40], type: TIME_OF_DAY, time: 0}    # sunrise
```

**Multi-step transition — smooth fade:**
```yaml
# Gradual dusk — 3 steps across 80 ticks reads as continuous fade
- {at: 0,  type: TIME_OF_DAY, time: 8000}
- {at: 40, type: TIME_OF_DAY, time: 13000}
- {at: 80, type: TIME_OF_DAY, time: 18000}

# Deliberate snap — 2 steps reads as a shift, usable for uncanny effect
- {at: 0,  type: TIME_OF_DAY, time: 6000}
- {at: 20, type: TIME_OF_DAY, time: 18000}   # world goes dark in one step
```

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| TIME_OF_DAY | Point | Snaps world time to a specified value |
| WEATHER | Bar | Sets weather state (clear / storm / thunder) |
| LIGHTNING | Point | Cosmetic lightning strike at an offset (no damage, no fire) |

**Not owned by this department:** PARTICLE (→ Effects), EFFECT on players (→ Effects),
CAMERA screen distortion (→ Effects). If it is applied *to* a player rather than *to the world*,
it belongs to Effects.

---

## Instruments

---

### World Clock — TIME_OF_DAY

**Java grounding:** `WorldEventExecutor.handleTimeOfDay()` → `world.setTime(e.time)`. Server-wide.

**What it does:** Instantly moves the sun/moon to the specified time value, changing sky color,
ambient brightness, and shadow angle. The world clock does not run between shows unless the
plugin or server triggers it — TIME_OF_DAY gives full control over what the sky looks like at
any show moment.

**How to dial it:**

```yaml
type: TIME_OF_DAY
time: 18000   # integer 0–24000
```

**See the full Sky Arc Reference above for the complete vocabulary of time values.**

**Key design values:**

| Value | Lighting character |
|-------|-------------------|
| 0 / 24000 | Sunrise — earned dawn, the culminating light beat |
| 6000 | Noon — maximum brightness, relentless, nowhere to hide |
| 8000 | Mid-afternoon — neutral established warmth, the default "open" state |
| 13000 | Dusk — the liminal moment; the world between states |
| 18000 | Midnight — deepest dark, maximum drama |
| 22000 | Pre-dawn — still and cold; the world before light returns |

**Gradual transition — compose by sequencing:**

```yaml
# Gradual dusk across 80 ticks (R7 archetype sampler pattern at C1):
- {at: 0,  type: TIME_OF_DAY, time: 8000}    # mid-afternoon — warm, established
- {at: 40, type: TIME_OF_DAY, time: 13000}   # dusk — the world beginning to change
- {at: 80, type: TIME_OF_DAY, time: 18000}   # night — arrived
```

Three steps across 80 ticks reads as a continuous fade at show scale. Five–six steps are
nearly imperceptible as individual snaps. Two steps read as a shift — usable as a deliberate
effect.

**Storytelling contexts:**
- Opening world dimming during dialogue — the sky doing emotional work before anything else moves
- Sunrise at the earned moment — 18000 → 23500 → 0 across three steps as the culminating lighting beat
- False noon: 6000 at a moment of exposure or confrontation — too bright, nowhere to hide
- Staying at 13000 through a long liminal scene — never fully dark, never day, permanently between
- Pre-dawn still: 22000 held before the light arrives — the coldest, most patient moment

**Limitations:**
- Instant snap only — no native fade; gradual transitions require manually sequenced steps
- Server-wide: affects all players on the server
- Underground or fully enclosed spaces: sky time has no visible effect; block light governs there
- `world.setTime()` does not restore automatically — document and restore at show end

---

### Weather System — WEATHER

**Java grounding:** `WorldEventExecutor.handleWeather()` → Bukkit `world.setStorm()` +
`world.setThundering()`. Server-wide.

**What it does:** Changes the world's precipitation state and the corresponding ambient sound
layer. Three states:
- `clear` — no precipitation, full skybox, natural quiet ambient
- `storm` — rain (snow in cold biomes), reduced visibility, continuous rain ambient bed
- `thunder` — storm + active thundering; adds random server-generated thunder strikes to
  ambient sound (distinct from scripted LIGHTNING events)

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
- `duration_ticks` defaults to `-1` (omitted = persistent)

**Standard setup — always establish known state at T=0:**

```yaml
# Guarantee known starting state:
- {at: 0,    type: WEATHER, state: clear}

# Build to storm mid-show:
- {at: 600,  type: WEATHER, state: storm}

# Clear as release beat:
- {at: 1200, type: WEATHER, state: clear, duration_ticks: 200}
```

**Storytelling contexts:**
- `clear` at T=0 as ritual setup — the world made known before anything begins
- Storm building as tension rises — ambient rain doing emotional work before scripted sound fires
- A `clear` break after storm: the world opening, pressure releasing, wonder arriving
- `thunder` for large-scale dread — uncontrolled but powerful when the sky needs to feel in chaos

**Limitations:**
- Server-wide: same scope as TIME_OF_DAY
- `thunder` adds random server-generated strikes that cannot be controlled or timed. For a clean ambient bed, use `storm` instead
- Ambient rain/thunder sound competes with scripted SOUND events — coordinate with Sound Designer on every weather state change
- No partial weather states (fog, light drizzle) — only three vanilla states available
- Persistent when `duration_ticks` omitted; document and restore at show end

---

### Lightning Strike — LIGHTNING

**Java grounding:** `VisualEventExecutor.handleLightning()` → `world.strikeLightningEffect(strike)`.
Cosmetic only — no damage, no block ignition, no fire.

**What it does:** Fires a single visual lightning bolt at an offset from the spatial anchor.
The bolt appears and disappears instantly. A thunder crack tied to the strike plays at the
offset location. Multiple LIGHTNING events on the same tick create simultaneous multi-strike effects.

**How to dial it:**

```yaml
type: LIGHTNING
offset:
  x: 5    # offset from anchor X
  y: 0    # offset from anchor Y — relative, not absolute
  z: 0    # offset from anchor Z
```

> ⚠️ **All three offset fields are relative to the spatial anchor** — including Y. There is
> no absolute coordinate mode for LIGHTNING. If the anchor is at Y=64 and terrain is at Y=64,
> use `y: 0` for a ground-level strike.

**Timing patterns:**

```yaml
# Single punctuation strike at a precise moment:
- {at: 480, type: LIGHTNING, offset: {x: 0, y: 0, z: 5}}

# Ominous slow sequence — sky threatening, installment by installment:
- {at: 200, type: LIGHTNING, offset: {x:  4, y: 0, z:  2}}
- {at: 320, type: LIGHTNING, offset: {x: -3, y: 0, z:  6}}
- {at: 460, type: LIGHTNING, offset: {x:  1, y: 0, z: -4}}

# Chaos burst — world breaking open (same tick, three positions):
- {at: 800, type: LIGHTNING, offset: {x:  5, y: 0, z:  0}}
- {at: 800, type: LIGHTNING, offset: {x: -5, y: 0, z:  3}}
- {at: 800, type: LIGHTNING, offset: {x:  0, y: 0, z: -6}}
```

**Storytelling contexts:**
- Single strike as sky punctuation at a climactic beat — the world registering the moment
- Slow ominous sequence during a long tension hold — the storm arriving in installments
- LIGHTNING in clear weather (`WEATHER: clear`) — atmospheric wrongness; the sky misbehaving
- Multi-strike chaos at a world-breaking arc moment
- Paired with Fireworks finale — sky + ground illumination simultaneously (coordinate with Fireworks)

**Limitations:**
- Thunder cannot be suppressed — every strike fires a crack; Sound Designer must treat it as an uncontrolled sound event
- Cosmetic only: `strikeLightningEffect()` — no damage, no fires, no charged creepers
- Offset-only positioning: no absolute coordinate targeting
- Visual strike is instantaneous — no duration or multi-frame animation

---

## Fireworks as Light

Fireworks are owned by the Fireworks Director — Lighting does not author FIREWORK_* events.
But every burst illuminates the player's world for a moment, making pyrotechnic color palette
a shared concern.

Steve is consulted on firework palette choices that affect the ambient scene register: a warm
gold burst in a cool-lit scene makes a statement; a matching burst disappears into it.
See `kb/departments/fireworks.kb.md` for firework authoring reference.

When LIGHTNING timing aligns with pyrotechnic moments, both departments agree on what that
beat is doing — neither overrides the other.

---

## Tone Translation

How the Lighting & Atmosphere Designer interprets the Show Director's tone language.

**"Tender"**
Lighting reads tender as warm, present, and unhurried. Time values in the range 8000–12000
(mid-afternoon to golden sunset) — light that exists without demanding attention. Clear weather
with no ambient sound bed competing. Avoid both harsh noon brightness and full darkness; the
light should feel like late afternoon through a window. If the scene needs intimacy, hold at
12000–13000 and let the warmth of the approaching dark do the work.

**"Overwhelming / earned"**
This is a lighting arc call. Darkness first: hold at 18000 (midnight) through the build. The
earned moment arrives as light — either sunrise (0) breaking through, or a clear break after
storm. The overwhelming beat in Lighting is the world becoming visible again. Time the
transition to land with the show's key arrival moment. A storm breaking into clear is equally
valid and often more abrupt — earned through release rather than gradual return.

**"Strange / uncanny"**
Lighting makes strange through mismatch: full daylight during an ominous scene, or a LIGHTNING
strike in clear weather. The world behaving incorrectly registers as wrong before the player
consciously identifies why. A single LIGHTNING during a still clear scene that hasn't earned it
is one of the most cost-effective strange beats available. Alternatively: a scene that holds
at 13000 (dusk) without ever resolving to night — permanently liminal, never quite arriving.

**"Delight / surprise"**
A quick TIME_OF_DAY snap from night to bright noon mid-scene — the sudden blaze of full daylight
where darkness was. A LIGHTNING strike in cloudless clear weather — the sky producing a flash
for no reason. A storm that clears in two ticks, the world suddenly open. Delight in Lighting
is about the world catching the player off-guard in a way that makes them smile. Early morning
(1000) carries a natural "good surprise" quality — first light, the world newly made.

**"Joy / abundant"**
Full daylight (6000) or early morning (1000). Clear weather. The world at maximum visibility
and warmth. If the show has been holding darkness, the return to daylight should feel like
breathing out. Time the return to full light to land with — or slightly before — the joy beat,
so the world is already celebrating when it arrives.

**"Wonder"**
Liminal time: dusk (13000) or false dawn (approaching 0). The sky in mid-transition is the
most visually alive state. A weather shift — storm clearing to clear — paired with liminal
time creates a sky that is moving and uncertain. Wonder in Lighting is about the sky doing
something unexpected — moving when it should be still, brightening when it should be dark.

**"Weight / tension"**
Sustained darkness — 18000 held through the scene. No transitions. Weather `clear` (not storm —
storm is active, dramatic; this is still). The world unmoving, the night unbroken. The absence
of light is not dramatic here; it is simply present, like pressure before a release.

**Signaling back to the Director:** When a tone phrase is ambiguous for Lighting, the clarifying
question is: *"Does this scene live primarily in darkness, in transition, or in light?"* That
single answer resolves most tonal ambiguity — delight and wonder can both arrive as surprise,
but delight is quick and immediate while wonder is slow and uncertain.

---

## Department Principles

**What Lighting is ultimately for:** Lighting is the emotional context that everything else
lives inside. It doesn't draw attention to itself — it makes or breaks the register of every
other department's work. Lighting decides the container.

**The arc before the events.** Steve establishes the full lighting arc before authoring any
YAML. Individual cues are positioned in service of that arc. A lighting arc without a plan
is a lighting accident waiting to happen at a key moment in the show.

**What Lighting decides independently:**
- The lighting arc: opening light state, key transition moments, and closing state
- Step intervals and sequence design for gradual TIME_OF_DAY transitions
- LIGHTNING placement, timing, and pattern (single, sequence, chaos burst)
- Whether to reset world state at show end

**What requires Show Director sign-off:**
- Ending a show with the world permanently in a changed state (leaving midnight or storm is a story call)
- Any weather state that will be audible to the full server — Sound Designer and Director both aware
- A dramatic LIGHTNING sequence at a pivotal arc moment — placement is a Director call

**Cross-department coordination:**

*With Sound Designer:* Every weather state change is an unsolicited sound event. `storm` adds
a rain bed; `thunder` adds random rumbles; `clear` removes both. File the full weather arc with
Sound at brief time — not after YAML is written. This is Lighting's most critical coordination
relationship.

*With Effects:* `night_vision` (Effects authority) makes darkness visible — which may or may
not be what Lighting intended. Any scene where `night_vision` is active requires both departments
to agree the revealed environment is intentional.

*With Fireworks:* FIREWORK bursts are transient light sources. Coordinate on color palette and
whether LIGHTNING timing aligns with any pyrotechnic peak.

*With Set:* Sky access status from Environment Notes is Steve's planning constraint. An enclosed
or underground set means TIME_OF_DAY has no effect and Lighting must work within block light
levels only. Steve reads the Environment Notes before designing the arc.

*With Stage Manager:* TIME_OF_DAY and WEATHER persist after show end unless restored. Every
show that changes time or weather must document pre-show state and fire a restore event at show
end. Flag restoration needs to Stage Manager at brief time.

**Handling capability gaps:** The three core instruments are all verified and functional. No
filed gaps for Lighting. If a future need arises (per-player lighting, fog, gradual fade), file
with Stage Management before authoring any workaround.

**Houselights-down principle (Alan feedback, R7 debrief 2026-03-28):** Avoid multiple TIME_OF_DAY step-downs in quick succession for the show's opening transition. Two visible bumps in a short span reads as mechanical — the world is clearly being controlled, not experiencing something. The principle: design transitions so they read as atmospheric rather than commanded. If steps are needed, spread them across more increments over more time so no individual snap is perceptible. If a single deliberate snap is needed, coordinate it with something else happening simultaneously — a sound hit, a levitation event, a voice line — so the light change is motivated and connected to the show, not administrative. *"Let the sky do the work."*

**Escalation discipline:** Lighting resolves arc and instrument decisions independently.
Escalates when: (1) a scene's light state conflicts with another department's authored work in
a way timing adjustments can't resolve — bring to Director; (2) a weather event will have
server-wide audience impact not cleared with Director; (3) world-state restoration at show end
conflicts with another show's expected start state — Stage Manager arbitrates.

---

## Calibration Backlog

Items the Lighting department wants to develop mastery over. 📋 Proposed = named but not yet tested in a dedicated calibration round. ✅ Confirmed = tested, parameters known, ready to use.

---

### `houselights.down.snap` — 📋 Proposed
**Intent:** Single TIME_OF_DAY cut to full night. Deliberate darkness, reads as a choice.
**Confirmed when:** Player perceives a clean houselights-down, not a jarring snap.

---

### `houselights.down.gradual` — 📋 Proposed
**Intent:** Multi-step sky fade from afternoon to night — atmospheric not mechanical. Minimum steps and tick spacing so no individual bump is perceptible. The R7 C1 finding is the failure reference.
**Confirmed when:** Player experiences dimming as atmosphere. No individual step registers as a discrete command.

---

### `houselights.up.dawn` — 📋 Proposed
**Intent:** Night to full day — the earned dawn. Showcase.02 model. The culminating lighting beat of a long dark arc.
**Confirmed when:** Dawn reads as earned resolution, not a time snap.

---

### `houselights.up.snap` — 📋 Proposed
**Intent:** Instant daylight — shock/reveal. The world suddenly bright.
**Confirmed when:** Player registers the snap as dramatic, not as a reset.

---

### `atmosphere.hold.dusk` — 📋 Proposed
**Intent:** World locked at ~13000 (dusk). Liminal, permanently between states — uncanny register.
**Confirmed when:** Player feels the world is suspended, not mid-transition.

---

### `atmosphere.hold.midnight` — 📋 Proposed
**Intent:** World locked at 18000. Sustained deep night throughout the show.
**Confirmed when:** Darkness reads as ambient condition, not event.

---

### `storm.in` — 📋 Proposed
**Intent:** Clear to storm. Perceptual onset of weather — how long does the sound transition take? Does it read as weather arriving or just ambient noise changing?
**Confirmed when:** Storm onset feels motivated, not mechanical. Time from event fire to player perception measured.

---

### `storm.out` — 📋 Proposed
**Intent:** Storm to clear. Resolution. The ceiling lifts.
**Confirmed when:** Clearing reads as relief, not just state reset.

---

### `lightning.strike` — 📋 Proposed
**Intent:** Single cosmetic strike at a specific offset. No damage, no fire.
**Confirmed when:** Strike fires at intended position. World unchanged. Player startles, does not take damage.

---

### `lightning.beat` — 📋 Proposed
**Intent:** Strike timed to a specific dramatic beat — same tick as a levitation event, sound hit, or entrance. R7 observation: lightning "fully pulls focus to the moment." What tick offset makes the strike feel causal vs. coincidental?
**Confirmed when:** Player attributes the lightning to what just arrived, not as weather noise.

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
