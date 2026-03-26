---
show_id: showcase.01
department: Fireworks Director
document: Department Brief
updated: 2026-03-26
---

# Fireworks — showcase.01 "The Cabinet"

## What This Department Serves

Fireworks has one primary vignette in this show: Vignette E, "The Suspension." The player is
elevated 15–25 blocks above the ground (or above the void, if End islands). They are above the
fireworks — or at least level with the top of them. The exhibit is what it feels like to be
*inside* a firework display rather than watching one from the ground.

This is Mira's show moment. Every other vignette belongs to a different department's primary
skill. This one belongs to pyrotechnics.

Secondary contribution: Mira may contribute a brief fireworks accent at the Home Base return
following Vignette E — not a full sequence, but one or two rockets that signal the player's
return to familiar ground with a small celebratory gesture. This is optional and discussed at
intake.

---

## Vignette E Design Brief

### The premise
The player is floating. The world below is visible. Fireworks fire around them, slightly below
and at their level — the intent is that they feel *surrounded* by the burst, not that they
look up at it.

This requires:
- **Altitude contract with Effects:** Effects determines where the player is. Mira cannot
  author y_offsets until that altitude number is known. This agreement must happen at intake
  before either department authors YAML.
- **Location clearance from Set:** How much downward space is available? The player's altitude
  minus the ground (or void) = Mira's available vertical canvas. Set must provide this.

### Spatial design
`y_mode: relative` offsets are calculated from the show anchor (the player at time of invocation).
If the player is at altitude Y, a rocket with `y: -5` fires from 5 blocks below them. A rocket
with `y: 0` fires at their feet.

For the "inside the fireworks" experience: the primary cluster should fire from y: -2 to y: -10
(below player level), with some at y: 0 to y: 2 (at feet level). The burst radius of each rocket
extends upward — the player perceives the detonation above them even when the launch point is below.

Use `FIREWORK_PATTERN` (spatial scatter) rather than single FIREWORK events for the main sequence.
`RANDOM_SCATTER` at appropriate radius places bursts organically around the player rather than
in a fixed geometric ring — more natural, less mechanical.

### Arc shape
A fireworks sequence with a shape: rise, peak, fall. Not a sustained burst.

Suggested arc:
- **Entrance (first 3–4 rockets):** Sparse. One or two to establish "this is happening."
  Cool or neutral palette. Player orients.
- **Build (mid-sequence):** Density increases. Palette warms. Larger bursts.
- **Peak:** Maximum density. Mira's fullest expression. The moment.
- **Fall:** Density decreases. Smaller bursts. One or two final rockets, clearly final.
- **Silence:** The last burst fires. Hold. The silence after is the end of the vignette.

Total arc length: approximately 600–800 ticks (30–40 seconds). The vignette doesn't need to be
longer — this is not about quantity.

### Color palette for Vignette E
The tone brief says this vignette is about altitude and strangeness — the world from a height.
Suggested palette direction: cool blues and golds, not warm celebration colors. A firework
palette that feels like night sky rather than carnival. One warm burst near the peak — contrast
makes it land harder.

All palette choices go through the preset library (`fireworks.yml`). Mira names the presets
for this show at intake.

---

## Secondary Contribution — A-Section Accent

After returning from Vignette E (the A-return that follows E), a single warm rocket at Home
Base signals the player has come back from something remarkable. Optional — bring a yes/no
recommendation to intake.

If used: one rocket, y: +5 or +8 above anchor, warm preset, fired 2–3 seconds after the
Home Base teleport. Keep it small. The vignette already landed; this is an echo, not a repeat.

---

## Show-Level Constraints

- **Altitude agreement is a prerequisite.** Mira cannot write y_offsets without knowing the
  player's elevation during Vignette E. This number comes from Effects. Do not author Vignette
  E YAML without it.
- **No fireworks in the Nether** (Vignette D). The nether suppresses some firework behaviors
  and the Strider vignette is not a pyrotechnic moment.
- **No fireworks at the Contraption** (Vignette F). That vignette's punch comes from restraint
  and precision. A firework after the reveal would over-explain the joke.
- Fireworks at End islands behave normally — there is no sky limit to worry about. At high
  overworld altitude, confirm clearance from Set (no cave ceiling, no cliff face nearby).

---

## Intake Questions for Fireworks (Mira)

1. **Altitude:** What is the player's elevation during Vignette E? (This answer comes from
   Effects. Mira cannot proceed without it.)
2. **Location type:** End islands or high overworld? (Answer comes from Set.) Does the location
   have clear downward space for 15–20 blocks?
3. **Arc duration:** Approximately how many ticks does Mira want for the full sequence?
   (Confirm against Lighting — TIME_OF_DAY may be set for this vignette and Lighting needs
   to know Mira's duration.)
4. **Preset names:** What presets will Mira author for this show? Name them at intake so other
   departments can reference them.
5. **A-section accent post-E:** Yes or no? If yes, what preset?

---

## Decisions
*Filled at intake — altitude agreement from Effects must come first.*

## Revision Notes
*Added after each in-game test.*
