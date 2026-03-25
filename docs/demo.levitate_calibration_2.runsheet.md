# demo.levitate_calibration_2 — Run Sheet

**Version:** 2.8.0
**Command:** `/show play demo.levitate_calibration_2`
**Duration:** ~3.2 minutes (3840 ticks)
**Purpose:** Wide-range empirical calibration of levitation + slow_falling. Six sections covering the full parameter space — from the absolute floor (1 pulse) to maximum sustained lift (amp 9 × 240t), plus two pulse-pattern sections. Results drive precise authoring constants for all future shows.

---

## Before You Run

Stand on open ground with clear sky above. The show lifts you up to ~120+ blocks — make sure nothing overhead. Have F3 open or watch your Y coordinate at each apex. Keep this sheet open; you'll note a landing second (or `>timer`) for each section.

---

## How It Works

Each section:
1. Fires a lift — single burst or pulse series
2. Applies `slow_falling` just before levitation expires
3. Starts a second-by-second timer with audio pings
4. You note the second you touch ground

**Audio reference:**
- Soft ping every second
- Louder bell at 5s, 10s, 15s, 20s, 25s
- ACTION_BAR counter always visible (`[ X ] ↓  Ns`)

**If still airborne when the timer ends:** note `>10s`, `>15s`, `>20s`, or `>25s` depending on the section.

---

## Data Fields

Fill in after the run. Report all six to Claude.

| Section | Color | Lift Method | Timer | Height Apex (Y or blocks) | Landing Second |
|---------|-------|-------------|-------|--------------------------|----------------|
| A | White | 1 × amp 0, 20t | 10s | ___ | ___ sec |
| B | Aqua | amp 1 × 60t (single burst) | 15s | ___ | ___ sec |
| C | Green | amp 3 × 120t (single burst) | 20s | ___ | ___ sec |
| D | Gold | amp 9 × 240t (single burst) | 25s | ___ | ___ sec |
| E | Red | 20 × amp 1, 20t on, 22t cycle | 15s | ___ | ___ sec |
| F | Purple | 40 × amp 0, 10t on, 12t cycle | 15s | ___ | ___ sec |

**Height apex:** Check F3 debug screen at your highest point before slow_falling takes over. If you can't read F3 in time, eyeball it relative to ground.

**Section A note:** You may land before the 1-second ping fires. That's expected — it's the floor. Note "0s" or "<1s" if so.

---

## Section Intentions

### A — Absolute Floor (White, T=80–340)
One pulse, amp 0, 20 ticks. The minimum possible lift. If v1 calibration showed 5 pulses of amp 0 landing in ~2 seconds, one pulse should land almost immediately. This anchors the bottom of the scale.

**Watch:** Do you visibly move at all? Does slow_falling engage before you touch down?

---

### B — Gentle Burst (Aqua, T=340–680)
Single amp 1 levitation for 60 ticks (3 seconds). A moderate burst — enough to feel intentional, not overwhelming.

**Watch:** How high do you reach? Does it feel like "lifted gently" or "thrown upward"?

---

### C — Strong Burst (Green, T=750–1240)
Single amp 3 levitation for 120 ticks (6 seconds). Significant sustained lift.

**Watch:** Is there a meaningful visible difference from B? Does the ascent feel long, or does it hit a ceiling?

---

### D — Maximum Sustained (Gold, T=1350–2060)
Single amp 9 levitation for 240 ticks (12 seconds). The ceiling — maximum amplifier, longest single burst in this suite. Timer runs 25 seconds.

**Watch:** How high do you go? Does the ascent feel violent or does it plateau? If you're still falling at 25s, note `>25s`.

---

### E — Rhythmic Pulse Climb (Red, T=2160–2960)
20 pulses of amp 1, 20t on / 2t gap, every 22 ticks. The pulse arrives every 1.1 seconds for ~22 seconds of climb. This is the pulse-pattern aesthetic from `fx.levitate_surge`, run long enough to observe cumulative height.

**Watch questions:**
1. Do you reach comparable height to D (single max burst), or does the rhythm plateau well below it?
2. Is the bob visible during the climb, or does it read as smooth ascent?
3. Does the rhythm feel energetic, anxious, or meditative?

---

### F — Rapid Micro-Pulse (Purple, T=2980–3820)
40 pulses of amp 0, 10t on / 2t gap, every 12 ticks. A pulse every 0.6 seconds — much faster than E, but lower amplitude. Near-continuous low-level levitation.

**Watch questions:**
1. Does this read as "floating" or "climbing"? (Target behavior: slow continuous rise with subtle bob)
2. Does it feel different from Section A (one big pulse) or more like E (rhythmic but slower)?
3. Does it approach altitude maintenance at any point, or always obviously ascending?

---

## Comparison Questions to Answer

After the run, answer these with Claude:

1. **A→B→C→D height scaling:** Is the height increase roughly proportional to `(amp+1) × duration`? Or does higher amplitude plateau earlier?

2. **E vs D total height:** Does 20 pulses of amp 1 (cumulative) reach the same peak as a single amp 9 × 240t burst? Or is single-burst much more efficient for raw height?

3. **F behavior:** Did the rapid micro-pulse read as near-hover, steady climb, or still obviously ascending? This determines whether `fx.levitate_pulse` needs more aggressive tuning.

4. **slow_falling rate:** Was the descent speed similar across all sections? (Expected: yes — slow_falling is binary; amplifier has no meaningful effect.) Were any sections noticeably different?

5. **Pulse aesthetics E vs F:** Which felt more usable in a production show? E's 1.1s rhythm or F's 0.6s rapid flutter?

6. **Airtime surprise:** Which section surprised you most — either much shorter or much longer than you expected?

---

## Derived Constants (fill in post-run)

### Slow_falling terminal velocity

From any section where you have both height and landing second:

```
V_slowfall (blocks/sec) = height_apex / landing_second
```

Run this for 2–3 sections and average. Discard if you hit ground within 1 second (too short to measure cleanly).

**V_slowfall from this run:** _____ blocks/sec

---

### Blocks per tick for single bursts

For sections B, C, D (single burst, known duration):

```
blocks_per_tick ≈ height_apex / duration_ticks
```

Note: this is a rough average — actual velocity is not constant (it ramps up over the first several ticks).

| Section | Amp | Duration (t) | Height | Blocks/tick (approx) |
|---------|-----|-------------|--------|----------------------|
| B | 1 | 60 | ___ | ___ |
| C | 3 | 120 | ___ | ___ |
| D | 9 | 240 | ___ | ___ |

---

### Pulse cycle efficiency

For sections E and F, height per pulse:

```
blocks_per_pulse = total_height / pulse_count
```

| Section | Pulses | Total height | Blocks/pulse |
|---------|--------|-------------|--------------|
| E (amp 1, 20t) | 20 | ___ | ___ |
| F (amp 0, 10t) | 40 | ___ | ___ |

---

### Optimal tick interval for altitude maintenance

Once you have `blocks_per_pulse` and `V_slowfall`:

```
gap_to_balance = blocks_per_pulse / V_slowfall   (seconds)
tick_interval  = gap_to_balance × 20              (ticks)
```

This is the cycle length at which a pulse exactly cancels slow_falling descent — the threshold between "climbing" and "hovering."

Example: if F gives 0.4 blocks/pulse and V = 2 b/s → gap = 0.2s = 4 ticks per cycle. At 12t cycles (current F), it's climbing. A 4t cycle would approach hover.

**Maintenance interval for amp 0 (10t pulse):** _____ ticks
**Maintenance interval for amp 1 (20t pulse):** _____ ticks

---

## After the Run: Build Commands

No Java changes in v2.8.0 — same JAR. If not yet built and deployed:

```bash
./gradlew shadowJar
# Output: build/libs/ScaenaShows-2.8.0.jar
```

Deploy: stop server, swap JAR, delete `plugins/ScaenaShows/cues/` and `plugins/ScaenaShows/shows/`, restart.
