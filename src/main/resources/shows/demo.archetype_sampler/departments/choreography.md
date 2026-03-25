---
department: Choreographer / Movement Director
show: demo.archetype_sampler
status: active
---

# Choreographer / Movement Director — Archetype Sampler

## Brief Received

> Full aerial arc. Player starts on ground, hover begins at C2 (0–2 blocks), climbs through
> C3–C6 (2–14 blocks), dramatic lift at C7 to 25 blocks, pressure-release descent through
> fireworks at C9, returns to ground by C13. Calibrated levitation patterns in use throughout.

---

## Decisions

**Decision: Altitude is the primary dramatic instrument.** The player's height is the show's main compositional axis. The arc from ground to sky is not spectacle — it is the emotional journey the brief describes. Every other department's work is calibrated to serve this arc.

**Decision: Height withheld, then earned.** Early sections keep the player close to the ground so the lift at C7 reads as genuinely significant. R6 failed here — the C2 burst spent the reveal before the player had been prepared for it. R7 withholds the dramatic lift until C7.

**Decision: Calibrated patterns throughout.** All levitation uses the calibrated timing constants derived from in-game testing:
- **HOVER:** amp=0, lev=20t gap=8t, cycle=28t — altitude holds
- **CLIMB:** lev=24t per cycle — gradual upward drift
- **RELEASE:** amp=0, lev=20t gap=24t, cycle=44t — slow controlled descent ("blood pressure release")

---

## R7 Altitude Arc (current — v2.9.0)

| Section | Altitude | Mode | Notes |
|---------|----------|------|-------|
| C1 | 0 blocks | Ground | Opens at day (T=8000), dims to night |
| C2 | 0–2 blocks | HOVER near ground | Gentle bubbling — embers at body level. NO amp-9 burst. |
| C3 | 2–6 blocks | CLIMB begins | Warmth now genuinely below the player |
| C4 | ~6 blocks | HOVER | Cool shift is tonal, not vertical |
| C5 | ~7 blocks | HOVER | Cave sound ascends toward them from below |
| C6 | 9–14 blocks | CLIMB | Unannounced, silent foreshadowing of what's coming |
| C7 | 14→25 blocks | AMP-9 LIFT EARNED | First dramatic height — the arrival. This is the moment. |
| C8 | ~25 blocks | HOVER | Joy burst fires below — elevated observer |
| C9 | 25→18 blocks | RELEASE (descent) | Player descends THROUGH fireworks. Offsets 26/20/12/24/16 |
| C10 | 18→8 blocks | RELEASE continuing | Ramp build during descent |
| C11 | 8→14 blocks | CLIMB | Pre-finale rise |
| C12 | 14→35+ blocks | AMP-9 LIFT | Full finale lift — highest point |
| C13 | Drifting down | slow_falling | Show ends in the air |

**Full arc summary:** 0 → 2 → 6 → 7 → 9 → 14 → **25** → 18 → 8 → 14 → **35+** → 0

---

## R6 Feedback (in-game test 2026-03-25)

Two passes. Key issues identified:
- Altitude arc not yet narratively earned
- "Boof" lift at C2 spent the reveal too early
- Player often above effects rather than inside them
- Joy/daylight transition logic muddy
- **C10 (building section) was strongest — use as model for spatial design**

R7 response: altitude withheld, C2 redesigned to hover near ground, C7 is now the earned dramatic lift.

---

## Notes

**R7 watch questions:**
- Does C2 feel like "gentle bubbling" near the ground, or does it feel arbitrary?
- Does C3 climb feel like gradual separation from earth?
- Does C7 lift feel EARNED — like the arrival takes you?
- Is C9 inside the fireworks (y_offsets: 26/20/12/24/16 relative to altitude ~25)?
