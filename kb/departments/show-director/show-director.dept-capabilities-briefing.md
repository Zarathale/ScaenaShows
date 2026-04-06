---
document: Direction Briefing — Department Capabilities & Calibration Priorities
version: 1.0
updated: 2026-03-28
author: Show Director (Claude)
purpose: >
  Read before writing any show brief. Translates each department's current capability
  state and calibration backlog into show-design language. Replaces the need to read
  all 12 department KBs before a brief.
---

# Direction Briefing — Department Capabilities & Calibration Priorities

> **For the Show Director's use at brief time.**
> This document tells you what each department can do right now, what they want to develop,
> and what kinds of scenes or moments would give them a practice context. The goal is shows
> where storytelling and calibration reinforce each other — not competing priorities.
>
> **Updated:** 2026-03-28. Sources: individual department KBs, R7 debrief.

---

## How to Use This Document

When briefing each department, two questions guide the work:

1. **What does this department need from this show?** (Trust the confirmed capabilities.)
2. **What does this show give this department to practice?** (Name one calibration priority per department in the brief.)

A long dark arc isn't just a story choice — it's also Lighting practicing `atmosphere.hold.midnight`
and Sound practicing `bed.dark`. Design with both in mind.

---

## Calibration Status at a Glance

| Department | Confirmed Patterns | Proposed Backlog | Key Hunger |
|---|---|---|---|
| **Lighting** | 4 arc types + LIGHTNING | 10 named patterns | `houselights.down.gradual` (R7 was jarring), `atmosphere.hold` variants |
| **Sound** | All instruments; Gracie | 5 patterns + 3 motifs | `bed.dark`, `bed.wonder`, `button.arrival` |
| **Choreography** | All instruments | 7 placement + sequence patterns | Any show with performers |
| **Effects** | All instruments; `descent-through-fireworks` ✅ | Hover texture variation | Altitude arc design; texture between levitation sections |
| **Camera** | All instruments | 2 calibration questions | Drone pan speed; pre-lift orientation timing |
| **Fireworks** | All instruments; `descent-through-fireworks` ✅ | 6 named arrangements | `arrangement.punctuation`, `arrangement.crown`, `arrangement.burst.joy` |
| **Wardrobe** | Equipment + invisible-body | 4 costumes | `costume.ethereal` (invisible-body technique) |
| **Set** | All instruments | 3 space configurations | Confirm `space.stage.marked` in practice |
| **Voice** | All text instruments | 5 line types; 4 timing modes | Director must specify timing mode per beat |
| **Casting** | All instruments | 3 dramatic archetypes | No rounds needed — register is inherent to mob type |

---

## Department by Department

---

### Lighting & Atmosphere — Steve N.

**What he brings:** Lighting sets the emotional container everything else lives inside. Steve works at arc scale — opening light state, key transitions, closing state — before authoring any events. His instruments affect all players on the server, so weather changes and dramatic time transitions require Show Director clearance.

**Confirmed right now:**
- TIME_OF_DAY: instant snaps and multi-step gradual fades (3 steps across 80 ticks reads as continuous)
- WEATHER: clear / storm / thunder (note: storm automatically adds a rain ambient bed — Sound must account for this)
- LIGHTNING: single or multi-strike, cosmetic only (no damage)
- Full sky arc vocabulary (0=sunrise → 13000=dusk → 18000=midnight → 0=earned dawn)

**Calibration priorities:**

- `houselights.down.gradual` — smooth atmospheric opening dimming. The R7 C1 pass had visible "bumps" that read as mechanical, not atmospheric. Steve needs the version that feels like weather.
- `houselights.down.snap` / `houselights.up.snap` — deliberate dramatic snaps: darkness arriving, or daylight slamming in.
- `houselights.up.dawn` — earned dawn for a long dark arc. The culminating lighting beat. showcase.02 is the intended model for this.
- `atmosphere.hold.dusk` and `atmosphere.hold.midnight` — world locked in a sustained light state. Does it feel like a condition, or like a stuck transition?
- `storm.in` / `storm.out` — perceptual onset timing. How long from event fire to "the weather has arrived"?
- `lightning.beat` — strike timed to a dramatic moment. What tick offset makes the strike feel causal vs. coincidental?

**Show design implications:**
A show that opens in warmth and moves toward darkness gives Steve `houselights.down.gradual`. A sustained dark middle section is `atmosphere.hold.midnight`. An earned emotional climax is the natural context for `houselights.up.dawn`. Tension building toward release maps onto `storm.in` / `storm.out`. Any show with a high-drama moment can accommodate `lightning.beat`.

**Steve's clarifying question:** *"Does this scene live in darkness, in transition, or in light?"*

---

### Sound — Brian

**What he brings:** Brian designs the audio architecture — what the player hears, when beds open, where silence falls, and which single-hit accents punctuate the key beats. He treats deliberate silence as one of his strongest instruments. Gracie the Harpist is on call throughout every production.

**Confirmed right now:**
- Looping ambient beds (cave, warped forest, soul sand valley, etc.) — established at scene open, cleared at scene end
- Single-hit accents at any tick (note blocks, bells, entity sounds, pitch-shifted for register)
- Simulated fade (descending volume sequence reads as fade)
- Deliberate silence via STOP_SOUND as a compositional instrument
- Gracie's gesture vocabulary: glissandos (dreamy / accent), plinks (fourth / fifth), low ominous chord

**Calibration priorities:**

- `bed.dark` — confirmed ambient configuration for sustained dark/still scenes. What loop, at what volume, feels like atmosphere without announcing itself?
- `bed.wonder` — lighter, suspended underscoring for wonder and elevation. Must be perceptibly distinct from `bed.dark`.
- `button.arrival` — single-hit musical punctuation for an entrance. Confirms something just happened.
- `button.reveal` — sharper or more upward than `button.arrival`. Distinguishable in register.
- `button.close` — the show's final sound before silence. Settling. Not mid-show energy.

**Motif library:** `motif.still.chord` ✅ (A minor, verified). Proposed: `motif.joy`, `motif.tension`, `motif.close`. See `kb/departments/sound/sound.music-director.md`.

**Show design implications:**
A show with a sustained dark register gives Brian `bed.dark`. A show with wonder/elevation gives him `bed.wonder`. Any show with performer entrances or set reveals gives him `button.arrival` and `button.reveal`. A show that ends cleanly gives him `button.close`. Sound calibrates best when sections have clear, distinct emotional registers — that's what makes the beds legible.

**Brian's clarifying question:** *"Is the sound announcing the moment, confirming it, or unsettling it?"*

---

### Choreography — Sharon

**What she brings:** Sharon designs the spatial picture — at every key tick, where is every performer, and does the arrangement tell a story? She defaults to puppet state (precise, controlled) and enables AI selectively for authentic creature behavior. She works with named marks from Set only; never hardcoded offsets.

**Confirmed right now:**
- Any mob/entity type, placed precisely at spawn
- ENTER / EXIT for theatrical arrivals and departures
- HOLD for tableau moments
- FACE for orientation (yaw only)
- ENTITY_AI toggle — the "coming alive" release moment is a high-value beat
- Short mob crosses on flat terrain (pathfinding is reliable under 10 blocks, open ground)
- Despawn with optional particle burst

**Calibration priorities:**

- `placement.near` — entity within 3–5 blocks. Does it clip? Does the presence register before anything else fires?
- `placement.far` — entity at 10–15 blocks. Does the distance read as intentional separation?
- `placement.wing` — entity at a wing mark, not yet in the scene. Does it hold without registering as "already present"?
- `sequence.arrive.hold` — spawns and holds in puppet state. Does it stay visually still for a full section?
- `sequence.arrive.wander` — spawns, holds briefly, AI released. Does the "coming alive" moment land?
- `sequence.arrive.hold.exit` — full visitor arc: arrives, holds, departs cleanly
- `sequence.arrive.wander.exit` — the complete lifecycle: spawn → hold → wander → exit

**Show design implications:**
Any show with a performer (even one) gives Sharon placement and sequence patterns to practice. A companion figure that arrives early and holds through the show tests `placement.near` + `sequence.arrive.hold`. A figure that arrives, wanders, and exits tests the full lifecycle. Multiple performers of different archetypes gives both Casting and Choreography a workout.

**Sharon's clarifying question:** *"Should this read as genuine (AI on) or composed (AI off)? Both are deliberate choices."*

---

### Camera — Mark

**What he brings:** Mark's job is to make sure the player is looking at the right thing at the right moment. He keeps a mental map of every active beat and every competing claim on attention. Camera and Effects are one system: Effects moves the body, Camera moves the eyes.

**Confirmed right now:**
- FACE snap (yaw, instant)
- PLAYER_TELEPORT orientation-only (yaw + pitch, no position change) — the clean way to look up before a firework
- Camera modes: blackout, sway, flash, float
- PLAYER_SPECTATE drone (smooth pan via CROSS_TO on a drone entity)
- PLAYER_MOUNT for riding perspective
- Blackout to cover PLAYER_TELEPORT transitions

**Calibration priorities:**

- **Drone pan speed** — what CROSS_TO `duration_ticks` on a drone produces "cinematic" vs. "mechanical" vs. "too slow to register"? Three reference durations needed with labels.
- **Pre-lift orientation timing** — how many ticks before the levitation event fires should the pitch snap occur so it feels motivated rather than administrative?

**Show design implications:**
Camera calibrates *inside* normal show work — no dedicated shows needed. Any show with levitation is an opportunity for pre-lift timing. Any show with a panoramic reveal or elevated view is an opportunity for drone pan speed calibration.

**Mark's standing question:** *"Where is the player likely looking right now, and is that the right place?"*

---

### Effects — Felix

**What he brings:** Felix controls what the player's body experiences. He manages the altitude arc for the entire show — every physical sensation raises the threshold for the next, so he uses interventions deliberately and spends height once.

**Confirmed right now:**
- Levitation: HOVER (amp=0, 28t cycle), CLIMB (lev=24t), RELEASE, AMP-9 arrival surge
- Player flight lock (altitude hold)
- Slow_falling controlled descent (~2 blocks/sec)
- Velocity impulse (upward burst)
- `descent-through-fireworks` ✅ — confirmed pattern from archetype sampler C9. Player descends through y_offsets bracketing the descent path. Parameters known.
- Perceptual effects: blindness, darkness, nausea, night_vision, speed, slowness
- Particles: single burst and atmospheric repeating layer

**Calibration priorities:**

- **Hover texture variation** — archetype sampler R7 showed HOVER held through 5+ sections produces a "seasick" sensation. The next-step-up: initial velocity burst + high-frequency pulses with no net altitude change. Also open: how do you produce a perceptible texture change between HOVER and CLIMB so the sections feel distinct?

**Show design implications:**
Any show with a levitation sequence. The hover texture variation problem needs to be solved before any show can use sustained aerial sections without risking the seasick finding. A show with distinct levitation phases (introduction → floating → surge) gives Felix the most useful test.

**Felix's clarifying question:** *"Does this scene require aerial? If yes, when does the player first leave the ground — that determines where the whole arc has to go."*

---

### Fireworks — Mira

**What she brings:** Mira owns specific beats — arrivals, releases, finales, punctuation marks. Her orientation is altitude and timing: where the player is standing relative to a detonation is the most consequential decision. She coordinates with Effects on player altitude and with Lighting on color palette.

**Confirmed right now:**
- FIREWORK (single rocket, any offset)
- FIREWORK_CIRCLE (chase ring, simultaneous, power/color variation)
- FIREWORK_LINE (directional sweep)
- FIREWORK_FAN (asymmetric arms)
- FIREWORK_RANDOM (scatter, with seed for reproducibility)
- `descent-through-fireworks` ✅ — shared confirmed pattern with Effects (C9 reference)
- All preset families: scae.* (gold/green/white), bday.* (pastels), pride.* (rainbow)

**Calibration priorities:**

- `arrangement.punctuation` — single rocket at close range. Parameters for "one moment" vs. "background event" at different distances.
- `arrangement.crown` — CIRCLE with chase surrounding the player. Which radius reads as "surrounding" vs. "distant"?
- `arrangement.sweep` — LINE sweep across the stage. Fast/sparse vs. slow/dense reference configurations.
- `arrangement.burst.joy` — RANDOM scatter for celebration. Count and radius without performance lag.
- `arrangement.finale.intimate` — small but genuine culmination. Under 15 rockets. For quiet narrative peaks.
- `arrangement.finale.full` — full climactic multi-type arrangement. Tick spacing to avoid server spike.

**Show design implications:**
Any show with an arrival beat is `arrangement.punctuation` or `arrangement.crown`. A joyful show is `arrangement.burst.joy`. Any show with a finale is `arrangement.finale.intimate` or `arrangement.finale.full`. Fireworks benefit most from shows that create natural peak moments — that's what gives the arranged detonations their meaning.

**Mira's clarifying question:** *"Where is the player at altitude when this fires? We need to agree before either of us writes a tick."*

---

### Wardrobe — Margaret

**What she brings:** Margaret thinks in objects before costumes. Her first question is what a figure holds, not what it wears. Her signature instrument is the invisible-body technique — body disappears, held item remains. The hand tells the story.

**Confirmed right now:**
- All 6 equipment slots on any LivingEntity at spawn and mid-show via ENTITY_EQUIP
- ENTITY_INVISIBLE (body gone, held items and armor remain visible)
- Armor Stands as static costume/prop displays
- Non-standard helmet items (carved pumpkin, mob heads, food, decorative items)
- Note: `variant:` and `profession:` are gapped — use equipment to differentiate visually

**Calibration priorities:**

- `costume.bare` — default mob appearance, nothing added. Baseline confirmed across companion / authority / shadow figure types.
- `costume.weighted` — armor/heavy items register as authority or protection. Slot configuration confirmed and labeled.
- `costume.ethereal` — ENTITY_INVISIBLE with one significant held item. Body gone, object remains. Readable at 3, 6, and 10 blocks distance.
- `costume.relic` — Armor Stand as deliberate prop: one object, one meaning. Clean spawn, no artifacts.

**Show design implications:**
A show with a companion figure confirms `costume.bare` or `costume.weighted`. Any show with a mystery or uncanny presence is the natural context for `costume.ethereal`. A show built around a central object (a relic, a prop the player discovers) confirms `costume.relic`. Wardrobe benefits from shows where visual identity is part of the storytelling, not an afterthought.

**Margaret's clarifying question:** *"Is this about a figure's relationship to their costume, or about an object's independent presence?"*

---

### Set — Michael C.

**What he brings:** Michael C. owns the spatial infrastructure — marks, sets, player teleportation, and the Environment Notes that gate all other departments. His P1 scouting deliverable is the first production obligation on any show. No other department can begin their pass without it.

**Confirmed right now:**
- 9-position mark grid (anchor-relative, portable)
- Named sets (world-specific absolute locations, with `return_on_end: true`)
- PLAYER_TELEPORT (instant, set-based or offset-relative)
- REDSTONE (with required manual off pairing)
- Armor Stands as entity-based set dressing (inside cleanup contract)
- COMMAND escape hatch for block modifications (outside cleanup contract — requires documentation)

**Calibration priorities:**

- `space.portable.open` — no set registered, anchor-only. Confirms no positional artifacts across different start locations.
- `space.stage.marked` — standard 9-mark grid confirmed in-game. CROSS_TO and FACE targets resolve at expected positions. Grid diagram filed.
- `space.intimate` — tight performance area, performer at closest mark reads as "in the room with the player." Distance calibrated so details are visible.

**Show design implications:**
Set calibrates through normal show work. A portable show with no venue confirms `space.portable.open`. Any show with performer crosses confirms `space.stage.marked`. A close-quarters show (intimate register, one performer, short mark spread) confirms `space.intimate`.

**Michael C.'s deadline:** Environment Notes must be filed before department briefings close. This is the gate — no YAML begins without it.

---

### Voice — ShowSprite

**What he brings:** ShowSprite is the show's text presence — the only instrument the player processes in explicit language. That legibility is both its power and its primary risk. Voice uses it sparingly and precisely. Silence is as authored as speech.

**Confirmed right now:**
- MESSAGE (chat, intimate, persistent, scrollable)
- TITLE (fullscreen cinematic, reserved for peak moments)
- ACTION_BAR (peripheral whisper, ambient stage direction, vanishes naturally)
- BOSSBAR (structural chapter-marking, progress fill, color-coded by register)
- Full MiniMessage formatting; full audience targeting

**Calibration priorities:**

- **Timing modes** — the R7 archetype sampler showed timing was often undirected; some felt "wonky." ShowSprite needs the Director to specify for every major physical beat:
  - *Before* — Sprite speaks before the effect. Foreshadows. Risk: deflates the moment.
  - *With* — simultaneous. Words and sensation reinforce each other.
  - *After* — Sprite speaks after the effect. Player has the experience first; language arrives as interpretation. Often correct for physical moments.
  - *Silent* — Nothing. Voice restraint is a contribution.
- `line.opening` — the show's first address. Establishes register and presence. Not an announcement: an arrival.
- `line.transition` — bridge between major sections. Gives the player permission to feel differently.
- `line.arrival` — accompanies a dramatic entrance. Timing mode required from Director.
- `line.silence` — the authored nothing (`<dark_gray>...</dark_gray>`). Only when Director specifies it.
- `line.close` — the show's last word. Never an explanation.

**Show design implications:**
Every show with a Voice arc gives ShowSprite line types to develop. The most important thing a show brief can give Voice is timing direction per beat. A brief that specifies "Voice is silent through the rise; speaks once at the peak; then nothing" is the clearest gift.

**What Direction MUST specify in the brief:** For every major physical beat (levitation, performer entrance, set reveal), state the timing relationship: *before / with / after / silent*. This is a Director call, not a Voice default. If it's not in the brief, Voice must ask before writing a line.

---

### Casting

**What they bring:** The entity choice is the first performance. An Iron Golem standing motionless at T=0 is already doing emotional work before any other department has fired a single event.

**No dedicated calibration rounds needed.** Dramatic archetypes are inherent to mob type:

- `figure.companion` — Wolf, Allay, Cat. Devotion, proximity, the one who chose to stay. Works best in intimate scenes and close placement.
- `figure.authority` — Iron Golem, Villager. Weight, permanence, scale. Most powerful when motionless.
- `figure.shadow` — Enderman, Vex, Phantom. Strange, peripheral, edge-of-frame. Use at distance or as chorus.

Placement distance (near / far) and sequence lifecycle calibration live in **Choreography**. Visual differentiation calibration lives in **Wardrobe**.

**Flag at brief time:** If a casting choice depends on `variant:` or `profession:` (Cleric Villager, Siamese Cat, wolf coat color) — say so immediately. That Java gap is open. Don't cast a Cleric and silently accept a Farmer.

---

### Stage Management

*Operational department — not a show design collaborator.* Kendra maintains the gap registry, stop-safety checklist, and run-sheet protocol.

**At brief time, notify Stage Manager when:**
- Block modifications via COMMAND are planned (permanent-world risk, outside cleanup contract)
- REDSTONE is in use (manual off pairing required)
- World time or weather is changed (restoration documentation required)
- Any performer AI poses safety risk to the player (Phantom, Warden, Creeper, Elder Guardian with AI enabled)

---

## Cross-Department Groupings — What Plays Well Together

Some scene types naturally serve multiple departments simultaneously. Design for these combinations when possible.

---

**Sustained dark arc (night held, tension building):**
→ Lighting: `atmosphere.hold.midnight`
→ Sound: `bed.dark`
→ Casting: `figure.shadow` (Vex / Enderman / Phantom in puppet state)
→ Wardrobe: `costume.ethereal` or `costume.bare`
→ Voice: silence timing; `line.silence` if the Director calls for it

---

**Performer arrival moment:**
→ Casting: archetype choice (companion / authority / shadow)
→ Choreography: `placement.near` or `placement.far`; `sequence.arrive.hold`
→ Wardrobe: `costume.weighted` or `costume.bare`
→ Sound: `button.arrival`
→ Camera: face-before-arrival orientation
→ Voice: timing mode specified by Director (before / with / after / silent)

---

**Climactic release / joy peak:**
→ Lighting: `houselights.up.dawn` or `houselights.up.snap`
→ Sound: `bed.wonder`; `button.close`
→ Effects: AMP-9 lift or velocity burst
→ Fireworks: `arrangement.burst.joy` or `arrangement.finale.intimate`
→ Casting: Allay group (AI released) or Wolf performer release
→ Voice: TITLE at peak; `line.close`

---

**Wonder / elevation (the view from above):**
→ Effects: HOVER or CLIMB pattern (altitude arc)
→ Camera: pre-lift orientation; drone pan post-lift
→ Fireworks: altitude-coordinated arrangement
→ Lighting: liminal time (dusk / false dawn) or earned dawn arrival
→ Sound: `bed.wonder`
→ Voice: sparse — one line *after* the lift, or silent

---

## Checklist — Using This Document in the Brief Process

Before writing department briefs, confirm:

1. **Read this document.** Know where each department stands before assigning priorities.
2. **Add a Calibration Priorities section to the brief.** For each department, name one pattern from their backlog that this show will develop. It doesn't need to be the only goal — just name it.
3. **Set P1 first.** No department brief is actionable until Set files Environment Notes. Michael C.'s deliverable is the gate.
4. **Specify Voice timing mode per major beat.** For every levitation, entrance, and reveal: before / with / after / silent. Don't leave this for ShowSprite to guess.
5. **Name which departments carry the most weight.** See the brief template's "Departments with elevated priority" field. This tells departments how much to invest.
6. **Flag AI safety risks.** If Casting proposes Phantom, Warden, Creeper, or Elder Guardian with AI enabled — that flag goes in the brief before any in-game test.
