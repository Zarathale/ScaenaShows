---
show_id: showcase.03
department: Stage Management & Production
document: Department Brief
updated: 2026-03-26
---

# Stage Management & Production — showcase.03 "Welcome"

## What This Department Serves

Stage Management owns the most technically demanding coordination moment in the Showcase
Series: the Arrival. Six departments contribute simultaneously to the same tick. Stage
Management builds the tick map, confirms that all six contributions fire together, and
holds every department to the Arrival tick through all revisions.

The show has four sections. The Arrival is the hinge. Everything before it is building;
everything after it is response. If the Arrival sounds like departments taking turns, it
has failed.

Production owns the intake sequencing: convergence leads (Effects, Fireworks, Camera) brief
first, before any other department's Arrival decisions are made.

---

## Tick Architecture — Build → Arrival → Celebration → Coda

```
T=0         Show opens. PLAYER_TELEPORT fires to approach start.
            TIME_OF_DAY set (Lighting's Build value).
            WEATHER: clear (if not already).
            Committee members already spawned (see Pre-Show Staging below).
T=0–60      Arrival settling. Camera orients player toward the threshold.
T=60–120    BOSSBAR activates (Build section).
            Voice: optional Build line (if yes, fires around T=90–120).
            Sound: ambient bed established; Build layering begins.
T=60–T_arr  Build section. Sound layers in. Player walks approach.

[Build duration: ~1,200–1,800 ticks / 1–1.5 min]

T=ARRIVAL   The Arrival tick. All of these fire simultaneously or within ±5 ticks:
            - PLAYER_TELEPORT: player to welcome area (approach → welcome area crossing)
            - EFFECT [type]: Effects' threshold sensation (amplitude, duration per Effects)
            - FIREWORK: Mira's Arrival rocket(s) at agreed y_offsets
            - SOUND: Gracie's Arrival sound (the one that says "yes")
            - TIME_OF_DAY: Lighting's Arrival shift (if applicable)
            - FACE: Camera's Arrival orientation (if different from Set's yaw/pitch)
            - ENTITY_AI: committee AI release events (if hybrid, per Choreography)
            - MESSAGE: Sprite's Arrival line
            BOSSBAR updates to Celebration section.

[All of the above within ±5 ticks of T=ARRIVAL]

T_arr+60    Celebration begins. Mira's arc fires across the Celebration duration.
            Sound: celebratory layers at full.
            Lighting: TIME_OF_DAY at Celebration value.
            Choreography: committee in motion (AI on, if released at Arrival).

[Celebration duration: ~1,800–2,400 ticks / 1.5–2 min]

T_celeb_end Sound begins opening into Coda before Mira's last firework.
            Lighting begins Coda TIME_OF_DAY shift.

T_final_firework    Mira's last burst fires.
T_final_fw + 60–120    Sprite's Coda line fires.
            BOSSBAR updates to Coda section or clears.

[Coda duration: ~600–1,200 ticks / 0.5–1 min]

T=show_end  BOSSBAR cleared. Ambient bed holds.
            Entities managed per despawn_on_end.
            Show ends. return_on_end fires.
```

**Total target duration:** 6,000–7,200 ticks (5–6 minutes).

---

## Pre-Show Staging (Critical)

The welcoming committee must be staged BEFORE the show's initial player teleport. This is
Non-Negotiable #4 from the show direction: the committee is waiting for the player, not
responding to them.

Stage Management tracks this by ensuring SPAWN_ENTITY events for all committee members
fire before T=0 (the player's arrival at the approach start). Options:

**Option A — Hardcoded pre-T=0:** Committee entities are spawned at negative tick values
(if the plugin supports pre-T=0 events in the show YAML). Test and confirm.

**Option B — Show-start with immediate pre-teleport spawn:** Committee spawns at T=0 before
the player teleport fires. If the teleport fires at T=0+some minimum, committee spawning
at T=0 may work. Check the plugin's tick ordering for simultaneous events.

**Option C — Server-side pre-staging:** Committee is staged manually in-game before the
show runs, with the show's SPAWN_ENTITY managing only cleanup/despawn. Not YAML-clean but
functionally reliable. Stage Management flags if this is necessary and files an ops-inbox
note.

This must be resolved before YAML authoring. If the plugin cannot handle pre-T=0 staging,
file to ops-inbox.md immediately.

---

## The Arrival Tick Map

Stage Management owns this document. It is reviewed and signed off by Effects, Fireworks,
Camera, Lighting, Sound, and Voice before any of those six departments author Arrival YAML.

```
T=[ARRIVAL]     PLAYER_TELEPORT → welcome area (Set's coordinates)
T=[ARRIVAL]     EFFECT [type] [duration] [amplifier] (Effects)
T=[ARRIVAL]     FIREWORK preset=[x] offset y=[y] (Mira — Arrival rocket)
T=[ARRIVAL]     SOUND [arrival_sound_id] (Sound — Gracie's "yes" sound)
T=[ARRIVAL]     TIME_OF_DAY [value] (Lighting — if shift is at Arrival tick)
T=[ARRIVAL]±3   FACE yaw=[y] pitch=[p] (Camera — if needed at Arrival)
T=[ARRIVAL]±5   ENTITY_AI: false→true for committee members (Choreography — if hybrid)
T=[ARRIVAL]+0   MESSAGE [Arrival line] (Voice — Sprite)
```

The ±values are the acceptable windows. Stage Management confirms all events land within
this window in the final YAML. Events that drift outside ±5 ticks of Arrival are flagged
for revision.

---

## Production Intake Order

**Gate 1 — Convergence leads agree (Effects + Fireworks + Camera):**
- Effects proposes altitude at Arrival → Mira agrees or counters → locked
- Camera answers the Arrival facing question → Effects and Mira review → locked
- Set delivers welcome area coordinates + sky clearance → handed to Mira

**Gate 2 — Threshold definition (Set):**
- Set confirms the threshold's physical marker and coordinates
- Stage Management adds to the Arrival tick map

**Gate 3 — Voice writes Arrival line:**
- After Gates 1 and 2 are complete, Voice knows the altitude, the facing, and the threshold
- Voice drafts the Arrival line → Director reviews → locked

**Gate 4 — Remaining departments author:**
- Lighting, Sound, Choreography, Casting, Wardrobe can work in parallel
- Each delivers Arrival contributions to Stage Management who confirms ±5-tick compliance

**Gate 5 — Stage Management writes run sheet:**
→ `showcase.03/run-sheet.md`

---

## Technical Notes

- **Pre-T=0 committee staging:** Resolve before YAML authoring (see above).
- **Return behavior:** `return_on_end: true` — player returns to pre-show position at show end.
- **BOSSBAR section tracking:** Stage Management owns the BOSSBAR update ticks. Each section
  transition gets a BOSSBAR event coordinated with the actual content boundaries.
- **Fireworks + Effects altitude:** Once locked at intake, this number does not change without
  Director sign-off and Stage Management review of downstream effects.

---

## Ops-Inbox Items to File (if applicable)

- Pre-T=0 entity staging not possible → file to ops-inbox.md with workaround used
- Simultaneous event ordering guarantees needed at Arrival tick
- Any plugin behavior that causes Arrival events to fire out of sequence

---

## Run Sheet

*Written after all department YAML is drafted. Saved to `showcase.03/run-sheet.md`.*

---

## Decisions
*Pre-show staging method confirmed first. Arrival tick map reviewed by all six convergence departments before any of them author.*

## Revision Notes
*Added after each in-game test.*
