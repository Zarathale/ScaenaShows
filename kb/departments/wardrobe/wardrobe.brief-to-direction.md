---
department: Wardrobe & Properties Director
owner: Margaret
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Wardrobe — Brief to Direction

> Margaret's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for equipment slots, the invisible-body technique,
> mob variants, and armor stands lives in `kb/departments/wardrobe/wardrobe.kb.md`.
> Detailed catalogue references: `wardrobe.equipment-slots.md`, `wardrobe.invisible-body.md`,
> `wardrobe.mob-variants.md`, `wardrobe.emotional-register.md`.

---

## Standing Requirements

*What Margaret cannot start without. Wardrobe doesn't open without these.*

**Entity types confirmed by Casting.** Margaret can't design wardrobe without knowing who she's dressing. Different entity types have different equipment behaviors — a Villager accepts all six slots; an Armor Stand displays but has no AI; an Allay with no equipment is often already the right costume. Casting's decisions come first.

**The emotional register for each figure in the show.** Not a costume description — a relationship description. "Tender companion," "uncanny watcher," "formal authority," "strange presence" — these phrases are more useful to Margaret than "give her a hat." The costume serves the register. Margaret reads the tone first and then decides what the hand holds.

**Any figures that undergo a visible mid-show transformation.** A wardrobe change mid-scene (ENTITY_EQUIP) is a character transformation moment and needs to be designed that way, not added as an afterthought. Margaret needs to know about costume change beats at the brief stage: when they happen, what they're communicating, and what state the character is in before and after.

---

## Intake Questions

*Questions Margaret asks at the intake meeting or in the brief. Her first question is always about hands.*

- What does each figure hold? The main hand slot is the most expressive instrument Wardrobe has, and Margaret starts there before thinking about armor or full costumes.
- Are any of the figures meant to be read as people wearing something — or as objects and presences in their own right? That distinction determines whether the design goes toward character equipment or the invisible-body technique.
- Should the figures look like they belong in this world, or should they look wrong? "Belonging" suggests matched materials and natural mob appearance; "wrong" suggests mismatched equipment, unexpected items, biome-variant conflicts (pending gap resolution).
- Are there any visual reads that Direction has already committed to? ("A knight," "a scholar," "something like a priest.") Margaret designs toward those commitments; she doesn't discover them after the costume is authored.
- Does the show include any scene where the environment changes significantly — a dark section, a weather change, a new biome? Wardrobe palette needs to read correctly against whatever the player will actually be looking at.

---

## Requests

*Nice-to-haves. These improve the work but don't block it.*

- Knowing the Set palette and material register before finalizing equipment choices. Gold armor in a golden-hour world disappears; iron armor reads cold in snow in a useful way. Material conflicts between wardrobe and set should be found at design time, not after in-game.
- Knowing whether Lighting plans any dark or night-sky sections. Some materials and items disappear in darkness; torch-bearing or glow-adjacent equipment choices change significantly if a scene is going to be very dark.
- Knowing whether Wardrobe's invisible-body technique will be used as a major show beat — when Direction has plans to "spend" it, Margaret wants to know so she designs the reveal deliberately rather than casually. The technique carries significant weight when it's the first time the player sees it in a show.

---

## Wardrobe's Palette

Margaret's instruments, in brief:

**Equipment at spawn** — any of six slots (helmet, chestplate, leggings, boots, main hand, off hand) can be set on any LivingEntity at spawn. Only specified slots are set. Margaret's default is one slot first, more only when the scene requires it.

**Mid-show costume change (ENTITY_EQUIP)** — changes one or more slots on a live entity simultaneously. An empty string `""` clears a slot; omitting a key leaves it unchanged. Works on individual entities and on captured entity groups.

**The invisible-body technique (ENTITY_INVISIBLE)** — applies invisibility to the entity's body while keeping worn armor and held items fully visible. A body disappears; the items float. The torch, the sword, the relic, the banner — carried by nothing. This is Wardrobe's most distinctive instrument. It should be spent deliberately.

**Armor Stands** — static entity type that accepts all six equipment slots and displays them without movement or AI. Use as a costume-on-a-wire: a memorial, a silent sentinel, a prop placed deliberately in the performance space.

For full slot-by-slot creative reference, see `wardrobe.equipment-slots.md`. For the invisible-body technique in depth, see `wardrobe.invisible-body.md`. For the emotional palette — what Wardrobe brings to arrival, wonder, dread, intimacy — see `wardrobe.emotional-register.md`.

---

## What Wardrobe Can and Can't Do

**Verified and working:**
- All 6 equipment slots on any LivingEntity, at spawn or via mid-show ENTITY_EQUIP
- ENTITY_EQUIP on captured entity groups (applies simultaneously to all group members)
- The invisible-body technique (floating items, ghostly armor, disembodied presence)
- Armor Stands as static display surfaces
- Non-standard helmet items (carved pumpkins, mob heads, decorative items in the helmet slot)

**Current gaps — flag these at intake if they're relevant:**
- `variant:` and `profession:` on SPAWN_ENTITY are parsed but NOT applied at runtime. Villager profession, cat coat, horse color, sheep wool color, and wolf variants (1.21+) cannot currently be set via YAML. These fields are silently ignored. Equipment-based workarounds exist — a Villager with a GOLDEN_HELMET and BOOK reads as scholar without the LIBRARIAN profession skin. When a casting choice critically depends on a specific variant, flag it early and confirm whether the workaround serves the creative intent.
- Armor Stand pose control (angling the limbs) requires a COMMAND escape hatch and is outside the standard show cleanup contract. Document any command-placed pose changes in the run sheet with a corresponding cleanup COMMAND.
- ENTITY_INVISIBLE does not apply to Armor Stands (they have no body in the traditional sense).

---

## Document Status

*First pass — 2026-03-28*

Core structure is complete. What's still needed:

- Confirmed costume patterns from calibration (see `wardrobe.kb.md §Calibration Backlog`) — `costume.bare`, `costume.weighted`, `costume.ethereal`, and `costume.relic` are proposed but not yet tested in-game. Once confirmed, the palette section here should reference them by name.
- The "variant gap resolution" upgrade path — when the Java gap closes, this document should be updated to unlock the variant/profession design space. A note in the run sheet for any show currently using equipment workarounds will make the upgrade straightforward.
