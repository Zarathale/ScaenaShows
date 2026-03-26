---
show_id: showcase.01
department: Sound Designer
document: Department Brief
updated: 2026-03-26
---

# Sound — showcase.01 "The Cabinet"

## What This Department Serves

Sound in this show has two distinct jobs — and they require different instincts.

**Job 1: Environmental continuity.** Each vignette destination has its own biome and
atmosphere. The player teleports in and the world should immediately feel different — not just
visually but sonically. Sound establishes each space through its ambient bed: forest hush,
water movement, nether reverb, the specific absence of sound in high altitude void. These are
not events that happen — they are the condition of being in each place.

**Job 2: The comedy hit in Vignette F.** When the Contraption fires, Sound lands the beat.
This is the show's one precision-comedy moment. The audio hit must land on the same tick as
the REDSTONE reveal, or the comedy collapses. Sound leads this coordination.

The tone brief says "natural history documentary" — the audio should feel like a score that
knows it's accompanying something slightly absurd, but plays it completely straight.

---

## Per-Section Direction

### A-sections — Home Base (recurring)
The Allay has a musical note it plays when delivering items — this may or may not be accessible
as a triggered sound in YAML (confirm with Stage Management on `entity.allay.item_given`).
If accessible, consider whether a soft Allay note on each Home Base return reinforces the
space's identity.

Ambient bed for Home Base: whatever the biome is — established after Set confirms the location.
Day sounds, evening sounds, or the specific texture of whatever time of day Lighting sets.
Home Base is visited 5–6 times; the ambient bed should feel consistent across all returns.

### Vignette B — "The Still Water"
Water ambient is the primary bed: river/lake/waterfall sound depending on what Set found.
The player has free exploration. No events to hit — this vignette is entirely environmental.
Sound's job here is making the water feel present and specific.

Optional: one subtle SOUND hit on arrival — a bird call, a distant ambient sound — to mark
the entry into this space as different from Home Base. This is optional; keep it light if used.

### Vignette C — "The Theater"
Two creatures interact. Sound should not over-score this — the creatures generate their own
audio through AI behavior. Ambient bed appropriate to the biome.

If the creature theater moment produces a particularly good sound (a Goat's bleat landing at
a funny moment, two Cats yowling), that's AI-generated content that Sound has no control over.
Do not try to anticipate or score over it. Leave space.

### Vignette D — "The Nether Valley"
Nether ambient bed — the specific texture of which nether biome Set finds (basalt delta has
its own ambient register, distinct from soul sand valley). Confirm biome after Set reports.

Strider ambient sound is AI-generated (`entity.strider.ambient`). It will fire on its own if
AI is on — do not try to replicate or score over it. Establish the nether bed, then let the
Strider be itself.

Levitation will be active here (Effects owns altitude). Sound does not need to score the
levitation — the visual is sufficient. Keep the nether bed intact throughout.

### Vignette E — "The Suspension"
This is Mira's vignette. Sound's contribution should not compete with the fireworks.
During the fireworks sequence: near-silence or a very minimal ambient bed in the background.
Let the fireworks carry the sensory load.

The one sound worth authoring here: the sound of the lift (levitation onset). Consider a
soft, ascending tone — `block.note_block.harp` at rising pitch, or `entity.experience_orb.pickup`
at low volume — at the moment Effects activates the levitation. Confirm timing with Effects.

After Mira's sequence: the silence after the fireworks is itself a sound event. Let it breathe.

### Vignette F — "The Contraption"
Sound leads the comedy coordination. The sequence:

1. **Pre-reveal:** Whatever ambient texture the space has — this is the "before" state.
   A ticking mechanical sound if the contraption is redstone-visible? Optional, but consider
   whether a low ambient tick suggests something is about to happen.

2. **The REDSTONE reveal tick:** A single SOUND hit lands simultaneously with the REDSTONE
   trigger. This is the comedy beat. Options:
   - `block.note_block.bass` (low BONK — surprised, absurd)
   - `entity.chicken.egg` (if the punchline creature is a Chicken — thematically precise)
   - `ui.button.click` + `entity.player.levelup` stacked (small and triumphant)
   - Keep it one hit, not a chord. The restraint is the joke.

3. **Post-reveal:** A beat of silence. Then optionally: Sprite speaks. Then ambient returns.

Coordinate with Stage Management on exact tick alignment between REDSTONE event and SOUND event.

---

## Show-Level Constraints

- Each vignette's ambient bed must be cleared on departure (STOP_SOUND) before the next
  location's teleport. Ambient beds don't know they've moved.
- The nether biome's ambient sounds may include hostile mob calls — confirm these don't
  interfere with the Strider vignette's emotional register.
- The comedy hit in Vignette F is the show's precision-timing moment. Get the tick right.
- Do not over-score the A-section returns. Home Base should feel like a return, not a
  re-introduction. A brief ambient tone is enough; do not re-establish the full bed each time
  if it's already running.

---

## Intake Questions for Sound

1. **Home Base biome:** What ambient bed does this space call for? (Answered after Set confirms.)
2. **Allay note sound:** Is `entity.allay.item_given` triggerable as a SOUND event? Test
   and confirm with Stage Management.
3. **Vignette B water type:** Lake, river, or waterfall? Affects which ambient water sounds apply.
4. **Vignette D nether biome:** Basalt delta, soul sand valley, or nether wastes? Affects
   ambient choice significantly.
5. **Vignette E levitation sound:** Yes or no? If yes, which sound on which tick (coordinate
   with Effects for the exact onset tick)?
6. **Vignette F comedy hit:** What is the sound? What is the exact tick it fires?

---

## Decisions
*Filled at intake.*

## Revision Notes
*Added after each in-game test.*
