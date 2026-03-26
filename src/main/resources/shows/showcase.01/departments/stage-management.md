---
show_id: showcase.01
department: Stage Management & Production
document: Department Brief
updated: 2026-03-26
---

# Stage Management & Production — showcase.01 "The Cabinet"

## What This Department Serves

Stage Management owns the show's skeleton: tick structure, department sequencing, run sheet,
and the technical systems that make everything else possible. In a rondo show with six locations,
Stage Management's primary challenge is **transition integrity** — ensuring that each departure
from Home Base and each return is clean, sequenced correctly, and doesn't leave entities,
sound beds, or effects active in the wrong place.

Production manages the show's development arc from brief through opening. For showcase.01,
Production's job is to sequence the intake process correctly: Set is first mover, then
Casting + Effects + Fireworks (altitude agreement), then all remaining departments. No YAML
is written before coordinates are confirmed.

---

## Tick Architecture — Rondo (ABACADAEAF)

The show's structure in ticks (approximate — actual values locked at YAML authoring):

```
T=0       Show opens. Home Base teleport fires. TIME_OF_DAY set.
T=0–60    Arrival settling. BOSSBAR cleared. Allay spawns/activates.
T=60–300  A1 — Home Base opening. Voice speaks (opening line). Ambient established.

T=300     Departure for Vignette B. Sound clears ambient. TIME_OF_DAY adjustment if needed.
T=320     PLAYER_TELEPORT to Vignette B. BOSSBAR: "The Still Water" activates.
T=320–320+B_dur   Vignette B duration (free exploration). Voice line ~T+60. BOSSBAR fills.
T=+B_end  BOSSBAR cleared. TIME_OF_DAY reset to Home Base canonical. Sound clears.
T=+B_end+20   PLAYER_TELEPORT back to Home Base.
T=+A2_start   A2 — Home Base return. Allay active. Voice observation line. Brief beat.

[Pattern repeats for C, D, E, F]

T=last_A  Final Home Base return. Voice closing lines. Allay present. Show settles.
T=end     Show ends. Entities managed per despawn_on_end settings.
```

**Total target duration:** 8,400–9,600 ticks (7–8 minutes).

### Vignette duration targets (approximate)
- Vignette B: 800–1,200 ticks (40–60 sec) — free exploration, generous
- Vignette C: 600–900 ticks (30–45 sec) — creature theater, let it develop
- Vignette D: 600–800 ticks (30–40 sec) — Strider observation
- Vignette E: 800–1,200 ticks (40–60 sec) — Mira's fireworks sequence (Mira confirms)
- Vignette F: 400–600 ticks (20–30 sec) — setup + reveal + beat after
- A-sections: 200–400 ticks each (10–20 sec)

### Critical sequencing rules

1. **Entity cleanup before teleport.** Any entities spawned in a vignette are despawned
   OR explicitly managed before the teleport back to Home Base. Stage Management tracks
   this per-vignette. A Strider left in the nether after the show is world pollution.

2. **Sound clear before teleport.** STOP_SOUND fires before each PLAYER_TELEPORT event.
   A nether ambient bed playing at Home Base is disorienting.

3. **Levitation resolved before teleport.** If Effects has levitation active (Vignettes D
   and E), it must either be resolved (player back near floor) or handled by slow_falling
   that doesn't interfere with the teleport. Stage Management and Effects agree on the
   timing at intake.

4. **REDSTONE tick coordination (Vignette F).** The REDSTONE event and the SOUND comedy
   hit must fire on the same tick. Stage Management is responsible for confirming this
   alignment. If they drift by even 2–3 ticks, the comedy collapses.

5. **TIME_OF_DAY reset on each A-return.** Lighting issues the reset; Stage Management
   tracks that it fires within 60 ticks of each Home Base arrival.

---

## Production Sequencing — Intake Order

The intake process for this show follows a specific dependency order:

**Gate 1 — Set delivers:**
- All six stage coordinates confirmed and entered in stage registry
- Registry slugs named and stable
- Contraption trigger XYZ documented (Vignette F)
- Ceiling heights for Vignettes D and E documented

**Gate 2 — Altitude agreement (Effects + Fireworks + Camera):**
- Effects proposes Vignette E altitude target
- Mira accepts or counters — one number locked
- Camera answers Vignette E philosophy question
- All three departments confirm before any of the three author Vignette E YAML

**Gate 3 — Casting confirms performers:**
- Allay management decision (persist vs. respawn)
- Companion list for Home Base
- Vignette C creature pair (primary + backup)
- Vignette E aerial presence (yes/no + species)
- Vignette F punchline creature (after Set confirms contraption design)

**Gate 4 — All remaining departments author:**
- Lighting, Sound, Voice, Wardrobe, Choreography can work in parallel after Gates 1–3
- Each coordinates with Stage Management on tick values as they author

**Gate 5 — Stage Management writes run sheet:**
- Full tick-by-tick event list across all sections
- Run sheet saved to `showcase.01/run-sheet.md`
- C-numbers assigned for in-game test debrief

---

## Technical Coordination Items

**SOUND capability check:** Stage Management should test whether `entity.allay.item_given`
is a triggerable sound identifier on this server before Voice and Sound make decisions that
depend on it. File the result as a note in this document after testing.

**Nether world name:** The PLAYER_TELEPORT event for Vignette D requires the exact world
name as the server registers it (not just "nether"). Stage Management confirms this with
Set's scouting notes and verifies it against the plugin's world resolution behavior.

**End world name (if applicable):** Same as above for Vignette E if End islands are chosen.

**REDSTONE event syntax confirmation:** The REDSTONE event fires at an absolute XYZ coordinate
in a specific world. Stage Management confirms the event fires correctly in the test environment
and that the REDSTONE OFF cleanup (if needed) is also verified.

**Return behavior:** All sets in this show use `return_on_end: true`. Stage Management
confirms that the return fires correctly after show end and that it doesn't conflict with
any cleanup events that fire simultaneously.

---

## Ops-Inbox Items to File

If any of the following capabilities are discovered missing during show development, file
to `ops-inbox.md`:

- World name resolution failure on PLAYER_TELEPORT across dimensions (nether/end)
- REDSTONE event not firing at absolute coordinates
- `entity.allay.item_given` not registering as a valid SOUND identifier
- Any levitation/teleport sequencing interaction that produces unexpected player behavior

---

## Run Sheet

*Written after all department YAML is drafted. Saved to `showcase.01/run-sheet.md`.*

---

## Decisions
*Filled at intake — intake order matters. See Production Sequencing above.*

## Revision Notes
*Added after each in-game test.*
