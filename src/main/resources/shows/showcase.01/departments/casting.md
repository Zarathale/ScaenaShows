---
show_id: showcase.01
department: Casting Director
document: Department Brief
updated: 2026-03-26
---

# Casting — showcase.01 "The Cabinet"

## What This Department Serves

Casting shapes the show's identity more than any other single department. The Allay is the
through-line — host, guide, collector, eccentric neighbor — and the creature selections for
each vignette determine what kind of world the player is being shown. Choose wrong and the
vignettes feel like a zoo. Choose right and the player feels like they've been let in on
something.

The central challenge: each vignette features a different creature in a different biome, but
they should all feel like they belong to the same show. The unifying logic is the Allay's
curation — it has collected companions across biomes the way someone collects strange, beautiful
things. Casting must feel like a coherent collection, not a random assortment.

---

## Performer Roster

### Home Base — The Allay (anchor)
The Allay is present at the start of every A-section return. It is the show's host figure.

Key decisions for the Allay at Home Base:
- **Position:** Hovering near center of the Home Base space. Not performing — present.
- **State:** Performer state (AI on) is preferred — let the Allay's genuine wandering and
  hovering behavior do the work. Puppet state (AI off) only if the AI wanders too far from
  frame in early tests.
- **Number:** One Allay. The show's premise requires singular personality, not a chorus.
- **Items:** The Allay collects items. Consider whether it should have an item in hand at
  arrival — a small visual detail that implies it has been busy. This is optional; discuss at
  intake. Requires Casting to coordinate with Voice on whether this detail gets narrated.

### Vignette B — "The Still Water"
No creature is required. This vignette's exhibit IS the place itself — the player has free
exploration of a natural water space. Casting has no casting obligation here. If the location
organically has a creature nearby (a Squid in the water, a Drowned in the shallows), document
it but do not manage it. The show is not about creatures in this vignette.

### Vignette C — "The Theater"
**Two performers.** This is the creature theater vignette — the player watches two entities
interact in a natural amphitheater space.

Key casting considerations:
- The pair should create a dynamic purely through their AI behavior and proximity — comedy,
  tension, or strangeness that emerges from what they are, not what they're directed to do.
- **Safe pairing candidates:** Two Cats (independent, mutually ignoring, slightly absurd);
  an Axolotl and a Fish (predator/prey tension that is also visually funny); two Sheep of
  different colors (blankly present in a way that is somehow very funny); a Goat and almost
  anything (the Goat adds unpredictability).
- **Risk-review required if:** AI is enabled and either entity can harm the player.
- Deliver a primary recommendation and one backup option to intake.

### Vignette D — "The Nether Valley"
**One performer: a Strider.** This vignette's exhibit is the biome as a habitat — the Strider
reads as native, at home in lava, while the player is the alien visitor.

- Strider should be on or near lava — performer state (AI on) gives natural wandering on lava
  that no script can replicate.
- Confirm with Set that there is lava visible from the player's arrival point.
- The Strider's saddle status: either is valid, but an unsaddled Strider reads as wild and free
  — more consistent with the "natural habitat" premise.
- Despawn-on-end required: Strider must not be left as world pollution in the nether.

### Vignette E — "The Suspension"
**Optional: aerial presence.** The player is elevated 15–25 blocks; Mira owns this vignette
with fireworks. Casting has an optional contribution: a Phantom or Bat flying in the space
near or below the player adds scale and strangeness.

- If Phantoms are used: AI must be disabled (puppet state) — Phantoms will attack the player
  when AI is on. Show Director sign-off required.
- If Bats are used: performer state is safe. Multiple Bats in the void below the player while
  fireworks fire is a strong image.
- Casting's call: include aerial presence or leave the moment to Mira's fireworks alone. Either
  is defensible. Bring a recommendation to intake.

### Vignette F — "The Contraption"
**One performer: the punchline creature.** The contraption is a comedy beat. A creature is
the punchline — it was waiting behind the door, it is the thing the piston reveals, it is what
the dispenser fired.

- Must be legible as a punchline without narration. Small, specific, unexpected in context.
- **Candidates:** A single Chicken (the archetypal absurd creature); a Cat sitting on a
  pressure plate; a single Sheep looking directly at the player; a Parrot on a perch that
  wasn't there before.
- The Contraption's before/after state (Set's design) must be confirmed before Casting can
  lock the punchline creature — the creature's placement depends on where it is hidden in the
  "before" state.
- Coordinate with Sound: the comedy lands when Sound hits on the same beat as the reveal.

---

## Show-Level Constraints

- All creatures except the Home Base Allay are vignette-specific. They should not persist
  across teleports — `despawn_on_end: true` unless there is a specific reason to release.
- AI release in the Nether (Vignette D) is moderate-risk due to environment. Confirm the
  Strider's behavior won't push it into lava visible to the player as a distressing event.
- Entity variants (Wolf coat colors, Axolotl variants) are a known gap in the current plugin.
  Flag if a variant matters creatively — it is not currently accessible via YAML.
- The Allay at Home Base recurs across multiple A-sections. Confirm how it is managed between
  vignettes — is it despawned and re-spawned, or does it persist? Persistence is simpler but
  requires it to stay in-frame during teleports.

---

## Intake Questions for Casting

1. **Allay at Home Base:** Item in hand or empty? Performer state or puppet state?
2. **Vignette C pairing:** Primary recommendation and backup? AI on or off for each?
3. **Vignette E:** Aerial presence (Phantom puppet or Bat performer) or leave to Mira?
4. **Vignette F punchline:** What creature is the punchline? Where is it hidden in "before"
   state? This answer waits on Set confirming the contraption design.
5. **Allay management between vignettes:** Persist or despawn/respawn per A-section?

---

## Decisions
*Filled at intake.*

## Revision Notes
*Added after each in-game test.*
