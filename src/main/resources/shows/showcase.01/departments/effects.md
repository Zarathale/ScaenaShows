---
show_id: showcase.01
department: Effects Director
document: Department Brief
updated: 2026-03-26
---

# Effects — showcase.01 "The Cabinet"

## What This Department Serves

Effects manages the physical experience of being in each space — what the player's body feels,
not just what they see. In a rondo show with six distinct locations, Effects has two primary jobs:

**1. The teleport experience between locations.** Each A→B, A→C, A→D, A→E, A→F teleport is
a transition. Effects can add brief sensation to the departure or arrival — a moment of slow
falling, a brief particle flourish — that makes each destination feel physically distinct from
the Home Base. This is optional but worth considering for some transitions more than others.

**2. Vignette E altitude — the contract with Fireworks.** This is Effects' most critical
contribution to this show. The player must be levitated to 15–25 blocks above the arrival
point during Vignette E. Effects owns this. Mira cannot author her pyrotechnic y_offsets until
Effects names the exact target altitude. This agreement happens at intake, before either
department authors YAML.

---

## Per-Section Direction

### A-sections — Home Base
No effects by default. The A-sections are grounding moments — the player returns to a familiar
space. Adding sensation to every Home Base return would dilute the contrast with the vignettes.
If a return specifically calls for effect (e.g., the return after Vignette E — coming down from
altitude), Effects can author a brief slow_falling sequence on that specific return.

**Post-E Home Base return:** Consider `EFFECT slow_falling` for 40–60 ticks after the player
teleports back from the Suspension vignette. The sensation of still-floating-slightly reinforces
the height that just happened. Optional — discuss at intake.

### Vignette B — "The Still Water"
No effects required. The player has free exploration of a natural space. Any environmental
sensation (floating, mist, visual distortion) would intrude on what should feel like unmediated
contact with the world.

### Vignette C — "The Theater"
Minimal effects. The creature theater does its work through AI behavior, not physical sensation.
If the Set location has a particular quality (a slightly sunken amphitheater, a space that feels
enclosed) that could be reinforced by a subtle effect, discuss at intake. Otherwise: nothing.

### Vignette D — "The Nether Valley"
Light levitation: 2–4 blocks above the safe floor. The player is slightly elevated — a visitor
floating above the Strider's terrain, not walking on it. This small height difference does
significant emotional work: the player is present in the nether without being fully in it.

- Target altitude: 2–4 blocks above the safe floor (exact value after Set confirms floor
  elevation and ceiling clearance).
- Levitation pattern: sustained hover at low amplitude. Use calibrated HOVER cycle (A=0,
  lev=20t, gap=8t, 28t cycle) — the player should feel gently suspended, not bouncing.
- Confirm no ceiling obstruction at this altitude (nether biomes often have low ceilings).
  Set's scouting notes will include ceiling height — check before setting altitude target.
- Maintain levitation for the full Vignette D duration. Slow_falling on departure if the
  teleport-back creates an undesirable drop.

### Vignette E — "The Suspension"
This is Effects' primary contribution. The player is lifted to high altitude — 15–25 blocks
above arrival — for the duration of Mira's fireworks sequence.

**Altitude design:**
- Target altitude: 18–22 blocks above arrival (exact to be agreed with Mira at intake).
- The lift should feel earned — not an instant snap to height. Consider a 3–4 second
  progressive climb using CLIMB cycle (24t cycle, amp 1–3) before settling into HOVER.
- At HOVER altitude: use calibrated HOVER cycle (A=0, lev=20t, gap=8t, 28t cycle) for the
  duration of Mira's sequence. Player floats stably while fireworks fire.
- After Mira's final burst: begin slow descent using RELEASE cycle (A=1, lev=20t, gap=24t,
  44t cycle) or a slow_falling for 200–300 ticks. The descent happens before the teleport
  back to Home Base, or the teleport itself handles it — confirm with Stage Management.

**The altitude number handed to Mira:** Once Effects confirms the target altitude (e.g., 20
blocks above arrival), Mira calibrates all y_offsets from that height. Lock this number at
intake. Do not change it after Mira has authored without Director sign-off.

### Vignette F — "The Contraption"
No altitude effects. The comedy relies on the player being grounded, present, and unaltered —
a normal person watching something absurd happen. Effects stays out of this vignette.

---

## Show-Level Constraints

- **Altitude agreement with Mira is a hard prerequisite for Vignette E.** Neither department
  authors until this number is locked.
- Nether altitude (Vignette D) requires ceiling clearance from Set before setting target height.
- All levitation ends cleanly before each teleport. A player levitated at the end of a vignette
  teleports from wherever they are — if they've drifted, this could be a problem. Effects must
  ensure levitation is resolved (player back near floor, or slow_falling active) before the
  teleport event fires.
- `slow_falling` after high-altitude vignettes: adds physical poetry to the return. Optional
  for all vignettes; strongly recommended for Vignette E specifically.

---

## Intake Questions for Effects

1. **Vignette D altitude target:** 2 blocks? 3 blocks? 4 blocks? (After Set confirms floor
   elevation and ceiling height.)
2. **Vignette E altitude target:** What height does Effects propose? This becomes the number
   handed to Mira.
3. **Vignette E lift pattern:** Instant snap to altitude, or progressive climb + settle into hover?
4. **Post-E descent:** Does the player descend before teleporting back, or does slow_falling
   carry over through the teleport?
5. **Post-E Home Base return:** `slow_falling` for 40–60 ticks after the Home Base teleport?
   Yes or no?

---

## Decisions
*Filled at intake — altitude agreement with Fireworks comes first.*

## Revision Notes
*Added after each in-game test.*
