---
show_id: showcase.03
department: Fireworks Director
document: Department Brief
updated: 2026-03-26
---

# Fireworks — showcase.03 "Welcome"

## What This Department Serves

Mira is a convergence lead for this show. Her altitude agreement with Effects is the Arrival
Coordination Contract's first item — it must be locked before either department authors, and
before Camera, Lighting, or any other department makes decisions that depend on where the
player is and where fireworks fire.

Mira owns the Celebration. The arc — rise, peak, fall — is her primary design exercise for
this show. The Celebration is the one section where fullness is the right answer. Mira has
permission to be generous here.

But the show direction also says: "Joy has a shape and Mira defines it here. A display fires
evenly and maintains energy. An expression has a peak and knows when to quiet." Mira is
designing an expression, not a display.

---

## The Arrival Coordination Contract — Fireworks' Role

**The altitude agreement with Effects:**
Effects proposes the player's altitude at Arrival. Mira accepts or counters. One number is
locked. Neither department authors Arrival YAML until this is established.

The altitude is Mira's foundational calibration point. All y_offsets in the Arrival and
Celebration are calculated relative to the player's position at that altitude.

If the player is at +5 blocks at Arrival:
- Fireworks at `y: 0` fire at the player's feet level
- Fireworks at `y: 10` fire 5 blocks above the player's head
- Fireworks at `y: -3` fire slightly below the player

For a "Welcome" show, Mira should be firing above and around the player — not at them. The
sky responds; it does not attack. Initial recommendation: y_offsets in the range of +5 to +20
relative to the player's Arrival altitude, with Celebration's main cluster at +10 to +15.

**The sky clearance from Set:**
Mira needs the welcome area's sky clearance (how many blocks above the area before any
obstruction). Set provides this. If clearance is 25 blocks from the welcome area floor and
the player is at +5 blocks, Mira has 20 blocks of canvas above the player. All y_offsets
must stay within this canvas.

---

## Per-Section Direction

### Build — "The Overture"
No fireworks during the Build. The approach walk is anticipation — nothing has happened yet.
Mira waits.

Mira's most important work during the Build is preparation: knowing exactly when the Arrival
fires, knowing the altitude, knowing which preset fires first.

### Arrival — "The Threshold"
One or two rockets at the moment of Arrival — not a full sequence, but the sky's immediate
response to the player crossing the threshold.

These opening rockets should be warm, visible, and calibrated to the altitude. They signal
that the sky has noticed. They are not the Celebration; they are the sky saying yes.

Consider: one rocket at the exact Arrival tick, positioned above the welcome area at y_offset
+12 to +15. A burst that the player can see in their Arrival framing (Camera's choice of
facing determines whether the player is looking at it). Then the Celebration begins.

### Celebration — "The Occasion"
The full arc. Mira's expression of joy.

**Rise:** Increasing density and altitude over the first 600–800 ticks. Smaller bursts first;
build toward larger. The player has time to take in the committee, the space, the sky. The
fireworks are arriving, not overwhelming.

**Peak:** Maximum density. Mira's fullest palette. The burst that the player will remember.
This should feel like a moment, not a sustained state — 200–400 ticks of full expression.

**Fall:** Density decreases. Smaller bursts. One or two final rockets that feel clearly final.
The pace slows; the sky quiets. Sound should begin opening into the Coda before the last burst.

**Total Celebration arc:** 1,800–2,400 ticks (1.5–2 min). Generous but not endless.

**Palette for this show:** The tone is "warm, celebratory, earned." Not cool blues (that's
showcase.01 Vignette E's register). Warm golds, whites, and one bold saturated color for the
peak. A palette that feels like people expressing genuine happiness.

**Pattern types:** Mix single rockets (punctuation) with `FIREWORK_PATTERN` scatter (volume)
during the peak. The pattern variation prevents a metronome feeling.

**The final burst:** Should feel like a finale — a slightly larger, slower burst that hangs
in the air for a beat before fading. The player should know it was the last one before the
Coda begins.

### Coda — "The After"
No fireworks. The Coda is what remains after. Sound already knows this; Mira does too.
The silence after the last firework is itself the Coda's opening image.

---

## Show-Level Constraints

- **Altitude agreement with Effects is a hard prerequisite.** Mira cannot write y_offsets
  without knowing the player's elevation at Arrival.
- **Sky clearance from Set is required before authoring.** All y_offsets must stay within
  the confirmed clearance minus 2–3 block buffer.
- The Celebration must have a shape — rise, peak, fall. Not a sustained burst.
- Sound should begin opening into the Coda before Mira's last firework. Coordinate with
  Stage Management on the approximate tick of Mira's final burst so Sound can time the
  transition.
- The Arrival fireworks and the Celebration fireworks are different in density and intent.
  The Arrival is one or two; the Celebration is the full expression.

---

## Intake Questions for Fireworks (Mira)

1. **Altitude agreement:** What does Mira propose, or does she accept Effects' proposal?
   Lock the number.
2. **Arrival rocket(s):** How many at Arrival? What preset? What y_offset?
3. **Celebration arc duration:** Approximately how many ticks for rise / peak / fall?
   (Stage Management needs this to build the tick skeleton.)
4. **Palette:** What preset names will Mira author for this show?
5. **Final burst tick (approximate):** When does the last firework fire in the Celebration?
   (Sound needs to know so they can begin the Coda transition before it.)
6. **Sky clearance confirmation:** After Set provides the number, Mira confirms all
   y_offsets are within bounds.

---

## Decisions
*Altitude agreement with Effects comes first. All other decisions follow.*

## Revision Notes
*Added after each in-game test.*
