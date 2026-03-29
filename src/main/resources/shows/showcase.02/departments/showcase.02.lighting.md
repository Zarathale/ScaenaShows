---
show_id: showcase.02
department: Lighting & Atmosphere Designer
document: Department Brief
updated: 2026-03-26
---

# Lighting — showcase.02 "The Long Night"

## What This Department Serves

This is Lighting's show. Full stop.

Every other department reads Lighting's plan before making decisions. When conflicts arise
between any department's choice and Lighting's direction, Lighting wins — unless the conflict
is surfaced to the Director before authoring. No department should author anything that fights
the light without clearing it first.

The show is a full arc — dusk to dawn — through a single location. Three instruments, all
exercised: TIME_OF_DAY drives the world's temporal state; WEATHER brings the storm; LIGHTNING
is the show's single peak event. The LIGHTNING instrument fires once and only once in this
show. Everything before it builds toward it. Everything after it lives in its aftermath.

---

## The Arc — Six Scenes

### A — Dusk Arrival (T=0)
**TIME_OF_DAY:** Early dusk — approximately 13,000. The world still has color: warm amber
and orange on the horizon, the sky beginning to darken above. This is not sunset drama; it's
the ordinary ending of a day.

Set this immediately at show open, before the player has oriented. The world should feel like
it was already this color before they arrived.

**WEATHER:** Clear. No rain. The dusk is a clean atmosphere — no cloud distortion.

### B — The Night Deepens
**TIME_OF_DAY:** Progress from ~13,000 toward full night (~18,000). This is a gradual movement,
not a snap. Consider two or three incremental TIME_OF_DAY steps across this section — let the
player feel the world getting later without being told.

The motion is slow. A 600–800 tick section with TIME_OF_DAY shifting by 1,500–2,000 units.

**WEATHER:** Still clear. The night deepens into its own quality, not into weather.

The first stars appear. The biome's dark texture becomes more present. Sound and Choreography
(nocturnal creatures) are adding texture in this section — Lighting's shift should make their
additions feel natural, not announced.

### C — Something Builds
**TIME_OF_DAY:** Full night, 18,000. Hold here — the darkness is the atmosphere for the
building pressure.

**WEATHER:** This is Lighting's decision — rain, or heavy cloud without rain, or both. The
show direction says "WEATHER event activates — rain or heavy cloud, Lighting's call." Make
the call at intake and hold it. Either is valid; heavy cloud without rain builds dread
differently than rain does (rain has sound consequences for Sound's department).

The key: the world's atmosphere changes *before* the LIGHTNING fires. The player should
notice something is different — the light has a different quality, the sky has closed — before
they're told something is coming. Camera should not announce the change; Sound should not
signal it dramatically. The change should precede any other department's response to it.

### D — The Storm Breaks
**LIGHTNING fires here.** This is the one. The instruction is absolute: it fires once,
it does not repeat, and the entire arc has been building to this tick.

The LIGHTNING event fires in the world at a specific XYZ near the player (Set's location
coordinates). Sound confirms the hit — thunder follows the flash. Effects may add brief
disorientation (Lighting coordinates with Effects on what amplifies the hit without competing
with it).

**TIME_OF_DAY:** Hold at night. The lightning illuminates the world in its own way — no
additional TIME_OF_DAY work needed for the flash itself. Minecraft's LIGHTNING event produces
light naturally.

**After the strike:** The world holds in storm state. No immediate TIME_OF_DAY change.
The aftermath is the world just after a lightning strike: dark, but changed.

### E — The Aftermath
**WEATHER:** Lighting decides — does the storm fully clear, or leave residual cloud? This
is the show's second most significant atmospheric decision. Clear skies after the storm
have a specific quality (washed, still). Persistent cloud has a different quality (ongoing,
unresolved). Both work; choose with the show's final emotional statement in mind.

**TIME_OF_DAY:** Lighting may hold full night or begin the first, barely-perceptible move
toward dawn. The direction says "the player waits" — whatever Lighting does here should
feel like the world recovering, not the show transitioning. Move is optional and slow if used.

### A' — Dawn
**TIME_OF_DAY:** The shift toward dawn is the show's most delicate lighting moment. The
direction is explicit: "Dawn arrives before you notice it was lighter." This means:

- No dramatic TIME_OF_DAY snap. Multiple small increments, spread across 1,500–2,000+ ticks.
- The threshold of "it's lighter now" should be something the player realizes, not something
  announced. Lighting should feel like it has been quietly changing for a while already.
- Dawn state: approximately TIME_OF_DAY 23,000–0 (just before sunrise). Do not proceed to
  full bright daylight — this show lives at the edge of night and morning, not in daytime.

**WEATHER:** Whatever the aftermath established — or clear, if the aftermath left residual
cloud and dawn deserves clarity. Lighting's call; make it intentionally.

**The A' difference:** This section must feel different from A (Dusk Arrival) even though both
are the same place in transitional light. The difference is in what has happened between them.
Lighting cannot author that difference directly — but it can ensure the dawn light is NOT a
mirror of the dusk light. Dusk is warm and ending; dawn is cool and beginning. Different
TIME_OF_DAY values produce different color temperatures. Use that.

---

## Intake Coordination

**Before briefing any other department:**
Lighting writes and presents the full arc plan to the Director:
- TIME_OF_DAY values for each section (exact or approximate)
- WEATHER decision (rain vs. cloud vs. hold for Section C)
- WEATHER aftermath decision (clear vs. residual for Section E)
- LIGHTNING tick (approximate — confirmed with Sound before final authoring)

Sound receives Lighting's arc before Sound briefs separately. Lighting and Sound co-design
the storm peak (Section D) together — Sound confirms the exact LIGHTNING tick with Lighting
before either authors, then Sound writes thunder as the causal consequence.

**LIGHTNING tick coordination with Sound:**
Flash first. Thunder follows. They are not simultaneous — they are causal. The offset (how
many ticks after the LIGHTNING event does the thunder sound fire?) must be agreed between
Lighting and Sound at intake. A realistic delay is 5–15 ticks per approximate in-world distance.
Agree on the number and both departments hold it through all revisions.

---

## Show-Level Constraints

- TIME_OF_DAY changes are Lighting's instrument — no other department alters it.
- WEATHER events are Lighting's instrument — no other department alters it.
- LIGHTNING fires once. Zero is wrong (the show has no peak). Two is wrong (the structure is broken).
- The A' section must feel demonstrably different from A. Not just lighter — carrying what happened.
- Time moves in this show but the player never leaves. All Lighting work is in one world, at
  one location, across approximately 10–12 minutes of tick time.

---

## Intake Questions for Lighting

1. **TIME_OF_DAY arc:** Write out the full arc plan — each section's starting value, any
   incremental changes within sections, end value of A'. Present to Director before other
   departments brief.
2. **Section C WEATHER:** Rain or heavy cloud? This decision affects Sound's department
   (rain has audio implications). Make this call and brief Sound directly.
3. **LIGHTNING tick (approximate):** Where in Section D does the LIGHTNING fire? What is
   the XYZ location of the strike? (Set's location provides the general area; Lighting
   chooses the specific nearby point.)
4. **Section E aftermath WEATHER:** Clear or residual? Why?
5. **A' dawn incrementals:** How many TIME_OF_DAY steps? At what tick intervals?

---

## Decisions
*Written by Lighting and presented to Director at intake — before any other department briefs.*

## Revision Notes
*Added after each in-game test.*
