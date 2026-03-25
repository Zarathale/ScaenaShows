# demo.flight_modes — Run Sheet

**Version:** 2.8.0
**Command:** `/show play demo.flight_modes`
**Duration:** ~56 seconds (1120 ticks)
**Purpose:** Calibration run. Tests three aerial approaches and the new PLAYER_FLIGHT event back-to-back at actual altitude. Notes here drive parameter decisions for archetype_sampler R6 and future productions.

---

## Before You Run

Stand on open ground with clear sky above. The show lifts you ~50 blocks — make sure there's nothing overhead. Keep notes by section number (S1–S5).

---

## S1 — Lift (T=0–160, ~8s)

**Intention:** Get to real altitude fast. amp 9 levitation for 120 ticks.
**Function:** Establishes the baseline height for the tests that follow.
**Mechanics:** `EFFECT levitation amplifier 9 duration 120`. Velocity-capped after ~2 ticks, then climbs at full speed for 6 seconds.
**Watch:** How high do you end up? Rough blocks above ground estimate. Does the ascent feel violent or theatrical?
**Notes field:** _____________

---

## S2 — Slow Fall Test (T=160–380, ~11s)

**Intention:** Isolate `slow_falling amp 0` at altitude with no other aerial effects.
**Function:** Answers the question: "Can you effectively stop movement?" This is the floor of what's possible without Java.
**Mechanics:** `EFFECT slow_falling amplifier 0 duration 2000` applied before levitation expires; no pulse. Player descends at ~0.2 blocks/sec.
**Watch question:** How fast does the ground appear to approach? Does 11 seconds feel like "stopped" or "drifting"? Does the altitude feel dramatic?
**Notes field:** _____________

---

## S3 — Levitate Pulse (T=380–700, ~16s)

**Intention:** Rhythmic pulsing to maintain altitude. This is the "keep player aloft" toolset Alan asked for — no Java required.
**Function:** `fx.levitate_pulse` — 14 cycles of amp 0 levitation (20 ticks on, 2-tick gap, every 22 ticks), baseline slow_falling throughout.
**Mechanics:** Net upward drift ~0.7 blocks/cycle = ~10 blocks over the section. Player should bob noticeably but feel held.
**Watch questions:**
1. Is the bob visible or subtle? (Aim: subtle but present)
2. Does the player feel held or just "not quite falling"?
3. Is the 22-tick cycle too slow, too fast, or right?
4. Could this be used in a real show without breaking immersion?
**Notes field:** _____________

---

## S4 — Levitate Surge (T=700–870, ~8.5s)

**Intention:** Emotional escalation — faster rhythm, higher amp, player visibly rises.
**Function:** `fx.levitate_surge` — 11 cycles of amp 1 levitation (12 ticks on, 2-tick gap, every 14 ticks). Upward drift is intentional.
**Mechanics:** ~10 blocks rise over the section. Levitation particles visible (hide_particles: false) — adds visual energy.
**Watch questions:**
1. Is the faster rhythm distinguishable from S3? Does it feel like escalation?
2. Does the visible upward drift feel like peak energy or disorientation?
3. Are the levitation particles welcome or distracting?
**Notes field:** _____________

---

## S5 — PLAYER_FLIGHT Hover + Release (T=870–1120, ~12.5s)

**Intention:** True altitude lock via server-side flight. Zero bobbing, zero drift — completely frozen.
**Function:** `PLAYER_FLIGHT state: hover` at T=880 engages `setAllowFlight(true)` + `setFlying(true)`. At T=1020, `state: release` with `release_effect: slow_falling` restores pre-show flight state and applies slow_falling for the descent.
**Mechanics:** Player can move horizontally while in hover — they're "flying" from the server's perspective. Only Y is frozen unless they move manually.
**Watch questions:**
1. Can you tell you're frozen vs. still drifting? (S2 vs. S5 contrast)
2. Do you instinctively try to move horizontally? (Slowness effect could discourage this)
3. Is the release into slow_falling seamless? Any gravity snap?
4. Did pre-show flight state restore correctly? (If you weren't in creative/flying before, flight should be disabled after.)
**Notes field:** _____________

---

## Debrief Questions

After running, answer these with Claude:

1. **S2 verdict:** Is 11 seconds of slow_falling "effectively stopped" for artistic purposes, or does it read as visibly descending?
2. **S3 vs S4 contrast:** Did the escalation from pulse to surge register as a meaningful change?
3. **S5 vs pulsing:** Does the zero-bob of hover feel better or worse than the rhythmic bob? What context would each serve?
4. **Preferred tool for archetype_sampler R6:** Pulse only? Hover + release? Hybrid (pulse early, hover mid, release at coda)?
5. **Slow_falling amplifier question:** Were higher amplifiers noticeably different from amp 0? (Expected: no — but empirically test.)
6. **Particle visibility in S4:** Levitation particles on during surge — keep or hide?
