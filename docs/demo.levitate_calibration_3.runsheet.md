# demo.levitate_calibration_3 — Run Sheet

**Version:** 2.8.0
**Command:** `/show play demo.levitate_calibration_3`
**Duration:** ~4 minutes (4740 ticks / 237s)
**Purpose:** Find the quasi-hover balance point. Eight iterations alternating short levitation pulses with slow_falling gaps at varying ratios. Looking for the cycle where rise equals descent.

---

## The Model

Each section loops this pattern continuously for ~22 seconds:

```
[LEVITATE for lev_t ticks] → player rises
[GAP: slow_falling only for gap_t ticks] → player falls at ~2 b/s
repeat →
```

**Balance theory:** If `blocks_risen_per_pulse == blocks_fallen_per_gap`, altitude stays constant.

Known from calibration v1: `slow_falling ≈ 2 b/s = 0.1 b/tick`. Estimated: `amp 0 × 20t ≈ 0.8 blocks rise`.

Balance equation: `gap_balance = rise / 0.1`. For amp 0 × 20t: `gap = 0.8 / 0.1 = 8 ticks`.

Section 2 (gap=8t) tests this directly. Sections around it bracket the real answer.

---

## Before You Run

Stand on open ground with clear sky above. Show opens with an amp-9 safety lift to get you to altitude, then each section repeats its own safety lift at the start. Open F3 if possible to track Y coordinate.

---

## What to Record

For each section: a single word + any detail.

| # | Color | Label | Predicted | Your observation |
|---|-------|-------|-----------|-----------------|
| 1 | White | A=0 lev=20t gap=4t | climbing | ___ |
| 2 | Aqua | A=0 lev=20t gap=8t | **predicted hover** | ___ |
| 3 | Green | A=0 lev=20t gap=20t | falling | ___ |
| 4 | Yellow | A=0 lev=40t gap=10t | climbing | ___ |
| 5 | Gold | A=0 lev=40t gap=20t | near hover? | ___ |
| 6 | Red | A=0 lev=40t gap=40t | falling | ___ |
| 7 | Blue | A=1 lev=20t gap=12t | near hover? | ___ |
| 8 | Purple | A=1 lev=20t gap=24t | falling? | ___ |

**Observation vocabulary:**
- `climbing fast` / `climbing slow` — net upward drift
- `hover` — roughly stable altitude (±5 blocks over 20s)
- `falling slow` / `falling fast` — net downward drift
- `landed` — hit the ground before section ended; note approximate seconds in (`~12s`)

**Also note (if visible):** Is the bob rhythm smooth, choppy, or jarring?

---

## Section Intentions

### Section 1 — A=0 lev=20t gap=4t (White)
18 pulses. Very short gap — slow_falling only gets 4 ticks (~0.4 blocks) between levitation bursts. Predicted: net climbing. Should feel rhythmic but aggressive upward drift.

### Section 2 — A=0 lev=20t gap=8t (Aqua) ← KEY TEST
16 pulses. Theory says this is the balance point: 0.8 blocks rise (20t amp 0) vs 0.8 blocks fall (8t × 0.1 b/t). If theory is correct, this is quasi-hover. Watch whether you drift up, hold, or sink.

### Section 3 — A=0 lev=20t gap=20t (Green)
11 pulses. Gap is 2.5× longer than predicted balance. Predicted: clear descent. Should confirm that wider gaps cause reliable falling.

### Section 4 — A=0 lev=40t gap=10t (Yellow)
9 pulses. Twice the levitation duration with a short gap. If amp 0 × 40t ≈ 1.6 blocks rise, balance would be gap=16t. At gap=10t, should be climbing.

### Section 5 — A=0 lev=40t gap=20t (Gold) ← KEY TEST
7 pulses. If amp 0 × 40t ≈ 1.6 blocks rise, balance gap = 16t. At 20t gap, should be slightly falling — or near hover. This resolves whether rise scales linearly with lev duration.

### Section 6 — A=0 lev=40t gap=40t (Red)
5 pulses. Equal lev and gap durations. 40 ticks of slow_fall = 4 blocks down. Should be clearly falling unless amp 0 × 40t rise is very large.

### Section 7 — A=1 lev=20t gap=12t (Blue) ← KEY TEST
14 pulses. Amp 1 is expected to rise ~2× faster than amp 0. If amp 1 × 20t ≈ 1.5 blocks, balance gap ≈ 15t. At 12t, should be near hover or slight climb.

### Section 8 — A=1 lev=20t gap=24t (Purple)
10 pulses. If amp 1 × 20t ≈ 1.5 blocks, balance = 15t gap. At 24t, should be falling. Confirms amp 1 balance point from the other side.

---

## Key Questions to Answer

1. **Does section 2 hover?** This is the core prediction. If yes, theory is solid. If it climbs, real rise per pulse is higher than 0.8b. If it falls, rise is lower.

2. **Sections 1 vs 2 vs 3:** Does the behavior change monotonically (climbing → hover → falling) as gap increases? This validates that gap is the primary control variable.

3. **Sections 4 vs 5 vs 6:** Does 40t lev duration give ~2× the rise of 20t? If yes, rise scales linearly with duration. If the hover point is at gap=10t (not 16t), rise doesn't scale linearly.

4. **Sections 7 vs 8:** Where does amp 1 balance? If section 7 (gap=12t) hovers, then amp 1 × 20t ≈ 1.2 blocks. If section 8 (gap=24t) also hovers, amp 1 × 20t ≈ 2.4 blocks.

5. **Feel quality:** Which sections feel smooth vs choppy? Fast cycles (sections 1, 2) have 1.2–1.4s rhythm. Slow cycles (section 6) have 4s rhythm. Which feels most natural in motion?

---

## Derived Constants (fill in after run)

### Blocks per pulse — amp 0, 20t lev

From the balance section (the one that hovers, or bracketed between climbing and falling):

If section 2 hovers: `rise ≈ 0.8 blocks` ✓ (confirmed)
If section 2 climbs but section 3 falls: balance is between gap=8 and gap=20.
  → `rise ≈ V_slowfall × gap_balance`
  → Interpolate: `gap_balance = (8 + 20) / 2 = 14t` → `rise ≈ 1.4 blocks`

```
blocks_per_pulse (amp 0, 20t) = V_slowfall × gap_balance_ticks × (1/20)
                               = 2.0 b/s × gap_balance_s
```

| Observation | Implied gap_balance | Implied rise |
|-------------|--------------------|----|
| Sec 2 hovers | 8t | 0.8b |
| Sec 2 climbs, Sec 3 falls | ~14t | ~1.4b |
| Sec 3 hovers | 20t | 2.0b |

**Measured balance point (amp 0, 20t):** _____ ticks gap → _____ blocks rise

---

### Blocks per pulse — amp 0, 40t lev

From sections 4–6. Same approach — find which section hovers or brackets.

**Measured balance point (amp 0, 40t):** _____ ticks gap → _____ blocks rise

**Rise ratio (40t / 20t):** _____  ← tells us if rise is linear with duration

---

### Blocks per pulse — amp 1, 20t lev

From sections 7–8.

**Measured balance point (amp 1, 20t):** _____ ticks gap → _____ blocks rise

**Rise ratio (amp 1 / amp 0, same duration):** _____  ← tells us if rise scales with amplifier

---

### Optimal hover cycle (to use in authored cues)

Once balance points are known, the hover cycle for a given (amp, lev_t) is:

```
ideal_gap = balance_gap_ticks
cycle = lev_t + ideal_gap
pulses_per_second = 20 / cycle
```

For a given hover duration D (seconds):
```
n_pulses = ceil(D × pulses_per_second)
```

Example (fill in after run):
```
amp 0, lev=20t, gap=___t → cycle=___t → ___pulses for 30s of hover
amp 1, lev=20t, gap=___t → cycle=___t → ___pulses for 30s of hover
```

---

## Build and Deploy

No Java changes in v2.8.0 — same JAR:

```bash
./gradlew shadowJar
# → build/libs/ScaenaShows-2.8.0.jar
```

Stop server → swap JAR → delete `plugins/ScaenaShows/cues/` and `plugins/ScaenaShows/shows/` → restart → `/show list` to verify → `/show play demo.levitate_calibration_3`.
