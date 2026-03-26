---
show_id: showcase.02
department: Casting Director
document: Department Brief
updated: 2026-03-26
---

# Casting — showcase.02 "The Long Night"

## What This Department Serves

Casting's contribution to this show is atmosphere, not character. The nocturnal creatures
of Sections B and C are environmental — they deepen the sense of night without becoming
stories in their own right.

The show direction is explicit: "They are atmosphere, not story. A Bat flock overhead,
a Wolf on a distant patrol — these are part of the night, not characters in it. If a creature
starts to feel like it's trying to be interesting in its own right, pull it back."

This is unusual guidance for Casting, which typically develops creature character. Here, the
brief is to select creatures that add nocturnal texture and then disappear into the world's
background. The question is not "who is on stage?" but "what lives here at night?"

---

## Creature Selection

### Section B — The Night Deepens
One or two nocturnal presences. These creatures arrive as the night deepens — they don't
need to announce themselves. They simply appear, because that's what happens when it gets dark.

**Above the player:**
- Bats (performer state — AI on) are the natural choice for aerial nocturnal presence.
  Spawn a loose group (3–5) above the player at a height where they're visible against
  the sky. They will fly naturally. They are the night sky gaining inhabitants.
- Phantom (puppet state required if used — Phantoms attack with AI on) is a higher-drama
  choice. A single Phantom, AI disabled, visible at altitude above the player. More ominous,
  less natural. Only if the location and biome make this feel right.

**At distance:**
- A Wolf patrol — one or two Wolves spawned at 30–50 blocks distance, AI on, wandering.
  They are not approaching. They are part of the landscape. Their ambient sounds (`entity.wolf.ambient`)
  fire naturally at distance. The player hears something out there. That's the contribution.

**Recommendation:** Bats above + distant Wolf patrol. Performer state for both. The Bats
give the sky life; the Wolf gives the middle distance something that moves and sounds.

### Section C — Something Builds
Creatures from Section B may remain, or their presence may thin as the storm approaches.
This is Casting's decision — does the wildlife go quiet before the storm? Many players have
the intuition that animals sense weather. If Casting thins or removes the Section B creatures
as Section C begins, that intuitive recognition may serve the building pressure.

Options:
- **Thin:** Despawn the Bats (but not the Wolf) as the storm builds. The sky empties.
- **Remove all:** Both Bats and Wolf despawn. The space becomes very quiet before the strike.
  Amplifies the "the air changes before the weather does" quality.
- **Hold:** Creatures remain. The storm arrives into a world still populated. Different feel.

Bring a recommendation to intake. Coordinate with Sound — if the Wolf despawns, its ambient
sounds stop. If it remains, they continue into the storm section. Sound needs to know.

### Section D — The Storm Breaks / Section E — Aftermath
No new creatures during the storm or immediately after. The LIGHTNING strike is the event.
Creature presence during this moment competes with it.

Section E aftermath: if creatures were despawned before the storm, consider whether anything
returns in the aftermath. A lone entity reappearing after the strike can reinforce the sense
of the world recovering. Optional; discuss at intake.

### Section A' — Dawn
No creatures are required for dawn. The world's ambient sound and light carry the section.
If a creature's natural morning sounds contribute (birdsong from a parrot, if applicable —
though this is a Sound department decision for ambient audio), Casting may coordinate.
No creature staging is needed for this section.

---

## Show-Level Constraints

- All creatures are atmospheric only — no creature behavior should draw the player's attention
  in a way that competes with the show's pacing.
- Bats/Phantoms above in the dark: confirm that the show's ambient light level (Lighting's
  TIME_OF_DAY at night) is dark enough that aerial creatures are silhouetted visible but
  not brightly lit.
- Phantoms (if used) require puppet state and Director sign-off. Avoid them unless the
  specific location and visual argument supports it.
- No creatures interact with the player. Nothing approaches, attacks, or creates event-urgency.
- Despawn all creatures cleanly by show end (`despawn_on_end: true` or explicit DESPAWN_ENTITY
  events before show conclusion).

---

## Intake Questions for Casting

1. **Section B selection:** Bats + distant Wolf? Or different combination?
2. **Section B aerial creature count:** How many Bats? At what altitude?
3. **Section C thinning:** Thin, remove all, or hold? Reasoning?
4. **Section E aftermath:** Any creature return in aftermath? If yes, which?
5. **Coordinate with Sound:** Confirm Wolf presence/absence in Sections C and D so Sound
   knows whether wolf ambient sounds are present during the storm buildup.

---

## Decisions
*Filled at intake — after Set confirms the Night Location biome (affects which creatures are plausible).*

## Revision Notes
*Added after each in-game test.*
