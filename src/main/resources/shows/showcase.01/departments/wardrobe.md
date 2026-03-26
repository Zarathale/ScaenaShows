---
show_id: showcase.01
department: Wardrobe
document: Department Brief
updated: 2026-03-26
---

# Wardrobe — showcase.01 "The Cabinet"

## What This Department Serves

Wardrobe makes one decision for this show that has lasting impact: how does the Allay's
collection read visually? The Allay is a collector. It has companions. Do those companions
(the 2–3 collected creatures that arrive at Home Base) share any visual language that tells
the player they belong to the same collection?

The tone brief calls this the "collection visual language decision." Wardrobe answers it.

This is not a high-intervention show for Wardrobe. The nether (Vignette D) and the End/
high overworld (Vignette E) do not have wardrobe applications. The creature theater
(Vignette C) is two entities whose appearance is determined by Casting's selection —
Wardrobe notes but does not necessarily alter. The Contraption's punchline creature
(Vignette F) is a single figure whose appearance is determined by species.

The primary design question belongs entirely to Wardrobe: **the Allay's companions at Home
Base — is there a visual coherence to this collection?**

---

## The Collection Visual Language Decision

The Allay collects things it likes. Its companions at Home Base have presumably been collected
the same way. Three possible visual languages for the collection:

**Option A — No imposed visual logic.** The companions are just what they are — a Parrot, a
Cat, a Wolf — visually diverse, chosen by the Allay's eccentric taste. The collection reads
as idiosyncratic, which is true to the Allay's nature. Wardrobe makes no alterations.
The show's visual coherence comes from the Allay at center, not from visual matching.

**Option B — One shared attribute.** Not full matching, but one thing in common across the
companions — all small, or all with a cool color, or all creatures that glow/have ambient
luminance (Allay glows; companions chosen for similar ambient quality). This is visual
rhyming, not costume design. Still low-intervention for Wardrobe, but intentional.

**Option C — Named collection.** The companions represent a thematic category the Allay
has been assembling. All creatures of a particular type, all from a particular biome, all
with a specific behavior. Wardrobe names the collection's organizing principle and documents
it. Voice can reference it; the show's premise deepens.

Wardrobe brings a recommendation to intake. Casting must know this decision before finalizing
companion selections for Home Base.

---

## Plugin Capability Notes

Wardrobe's current capabilities in YAML are limited. The primary tools are:
- `ENTITY_GLOW` — adds an outline effect to an entity. Available for all entity types.
- Equipment/armor through SPAWN_ENTITY entity data (limited — entity type dependent).
- Visual variants are currently a plugin gap for most entity types (Wolf colors, Cat patterns,
  Axolotl variants, Horse colors) — these cannot be set via YAML. Note this when it affects
  the collection visual language decision.

For this show, `ENTITY_GLOW` is the most likely Wardrobe tool. If the collection visual
language decision involves visual distinction, a subtle glow on one or more companions may
be the most accessible implementation.

---

## Show-Level Constraints

- Wardrobe's collection language decision must be communicated to Casting before Casting
  finalizes Home Base companion choices. These two departments decide together.
- Any ENTITY_GLOW applied to companions must be cleared at the end of the A-section before
  departure to a vignette, and reapplied on return — or applied once and held for the show's
  duration if the entity persists. Confirm entity persistence strategy with Casting.
- Vignette D (Strider): no wardrobe. The Strider's saddle status is Casting's call; equipment
  state is not a Wardrobe instrument for this show.
- Vignette F punchline creature: no wardrobe. The joke relies on the creature being exactly
  what it is — unaltered, surprising, mundane.

---

## Intake Questions for Wardrobe

1. **Collection visual language:** Option A (no imposed logic), Option B (one shared attribute),
   or Option C (named collection)? Wardrobe brings a recommendation and rationale.
2. **ENTITY_GLOW usage:** If a glow is applied, which entities and what color? This requires
   Casting's companion list first.
3. **Companion persistence:** Do companions persist through vignette teleports or are they
   despawned and respawned per A-section? (Casting answers this; Wardrobe needs the answer
   to know how to manage glow states.)

---

## Decisions
*Filled at intake — after Casting confirms companion list.*

## Revision Notes
*Added after each in-game test.*
