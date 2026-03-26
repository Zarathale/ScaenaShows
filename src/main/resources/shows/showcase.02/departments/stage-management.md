---
show_id: showcase.02
department: Stage Management & Production
document: Department Brief
updated: 2026-03-26
---

# Stage Management & Production — showcase.02 "The Long Night"

## What This Department Serves

Stage Management holds the tick skeleton for a show that has no teleports, no set changes,
and a single climactic event — LIGHTNING — that fires once, at a specific tick, and cannot
be moved without restructuring the entire arc.

The complexity here is not transition management (there are no transitions) but **sequencing
precision** — particularly around the LIGHTNING tick and the coordinated response of Sound,
Effects, Camera, Voice, and Lighting in a tight window around that event.

Production's job is to sequence the intake correctly: Lighting presents the arc plan first.
Everything else builds from that plan.

---

## Tick Architecture — Through-Composed (ABCDEA')

```
T=0         Show opens. PLAYER_TELEPORT (if player is not already at location) fires.
            TIME_OF_DAY set to dusk (~13,000). WEATHER: clear.
T=0–60      Arrival settling. Camera establishes arrival facing.
T=60–120    BOSSBAR activates (Section A: "Dusk" or show title bar).
T=120+      A — Dusk Arrival. Ambient bed established. Player orients.

[Section A duration: ~1,200–1,800 ticks / 1–1.5 min]

T=A_end     B — Night Deepens begins. TIME_OF_DAY progresses (incremental steps).
            BOSSBAR updates ("The Night" if section-named).
            Casting/Choreography: nocturnal creatures spawn.
            Sound: ambient bed deepens.

[Section B duration: ~2,400–3,000 ticks / 2–2.5 min]

T=B_end     C — Something Builds. WEATHER event fires (Lighting's decision).
            BOSSBAR updates ("Something Builds" or equivalent).
            TIME_OF_DAY: full night, hold.
            Sound: atmosphere shifts per Lighting's weather call.
            Casting: creature thinning if Casting recommends.

[Section C duration: ~1,800–2,400 ticks / 1.5–2 min]

T=LIGHTNING_tick    D — The Storm Breaks.
            LIGHTNING fires (specific XYZ, Lighting's target).
            Effects: disorientation event (simultaneous with LIGHTNING, if used).
            Sound: thunder fires at T=LIGHTNING_tick + [agreed offset].
            Camera: repositions if Option A philosophy (fires 60–120t before LIGHTNING).
            Sprite: MESSAGE fires at T=LIGHTNING_tick or within +20 ticks.
            BOSSBAR updates ("The Storm").

[Section D: the strike and its immediate aftermath — ~600–1,200 ticks / 0.5–1 min]

T=D_end     E — The Aftermath. WEATHER adjusts (Lighting's aftermath decision).
            BOSSBAR updates ("Aftermath" or equivalent).
            Sound: settles per aftermath design.
            Casting: creature return if applicable.

[Section E duration: ~1,800–2,400 ticks / 1.5–2 min]

T=E_end     A' — Dawn. TIME_OF_DAY incremental shift begins.
            BOSSBAR updates ("Dawn").
            Sound: dawn ambient emerges.
            Camera: faces dawn horizon if Camera Option 1.
            Sprite: dawn MESSAGE fires when dawn is perceptibly established.
            [Optional: Mira's single dawn rocket, if yes.]

[Section A' duration: ~1,800–2,400 ticks / 1.5–2 min]

T=show_end  BOSSBAR cleared. Sound fades. Show ends.
```

**Total target:** 12,000–14,400 ticks (10–12 minutes).

---

## Critical Coordination: The LIGHTNING Tick

The LIGHTNING tick is the most precisely coordinated moment in the show. Stage Management
owns the tick map and ensures all departments are authoring to the same number.

**Pre-LIGHTNING window (Camera, if Option A):** `FACE` fires at LIGHTNING_tick − 60 to − 120.
**LIGHTNING fires:** At LIGHTNING_tick (Lighting's event at Lighting's XYZ).
**Thunder fires:** At LIGHTNING_tick + [Sound/Lighting agreed offset — typically 5–20 ticks].
**Effects fires:** At LIGHTNING_tick (simultaneous with flash, if disorientation is used).
**Sprite fires:** At LIGHTNING_tick to LIGHTNING_tick + 20 (after flash, before or with thunder).

Stage Management documents the exact tick sequence:
```
T=[X]     LIGHTNING fires at XYZ [coordinates]
T=[X]     EFFECT [type] fires (if Effects says yes)
T=[X]+[n] SOUND thunder fires
T=[X]+[m] MESSAGE [Sprite Line 1]
```
This table is reviewed by all four departments (Lighting, Sound, Effects, Voice) and confirmed
before any of them authors YAML.

---

## Intake Order

1. **Lighting presents arc to Director** (TIME_OF_DAY values, WEATHER decisions, LIGHTNING XYZ candidate)
2. **Sound reads Lighting's arc; co-designs storm peak with Lighting** (thunder offset agreed)
3. **Effects decides Section D disorientation** (in consultation with Director)
4. **Camera answers storm philosophy question** (consults Lighting's arc)
5. **Casting confirms creature selection** (after Set confirms location biome)
6. **Stage Management documents LIGHTNING tick table** (all four departments sign off)
7. **All remaining departments author** (Voice, Choreography, Wardrobe, Fireworks — all can
   proceed after the tick table is locked)
8. **Stage Management writes run sheet** → `showcase.02/run-sheet.md`

---

## Technical Notes

- **LIGHTNING event:** Fires at a specific absolute XYZ in the current world. Stage Management
  confirms the event fires correctly and visibly from the player's arrival facing before intake
  closes. Test with a throwaway show if needed.
- **No teleports:** This show has no dimension changes or SET events. World name from stage
  registry is confirmed once (the Night Location world) and used in the show header.
- **Return behavior:** `return_on_end: true` for the one location. Player returns to their
  pre-show position when the show ends.
- **BOSSBAR management:** If section-named bars are used (Voice's Option B), Stage Management
  tracks the BOSSBAR section transitions in the tick skeleton and ensures they fire at the
  right section boundaries.

---

## Run Sheet

*Written after all department YAML is drafted. Saved to `showcase.02/run-sheet.md`.*

---

## Decisions
*Filled after LIGHTNING tick table is established and reviewed.*

## Revision Notes
*Added after each in-game test.*
