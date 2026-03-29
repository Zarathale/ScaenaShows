---
show_id: showcase.01
department: Wardrobe & Properties Director
document: Department Brief
updated: 2026-03-28
casting_locked: true
---

# Wardrobe — showcase.01 "Preparing for Battle"

## Status

Casting is locked: Hero = Vindicator, Companion = Armorer Villager. Margaret now has
a specific assignment. The kit design below is the active brief — not a proposal.

---

## The Hero's Kit — Vindicator

The Vindicator is a medium-large biped with a standard humanoid rig and a strong forward
stance. Armor reads clean and authoritative on them. The brief from Set, Voice, and the
show's register all point in the same direction: **a practical soldier's kit, not a
royal one.** Earned, not bestowed.

### Confirmed kit design

| Slot | Item | Notes |
|------|------|-------|
| Helmet | `CHAINMAIL_HELMET` | Chain reads as mobile, not ceremonial |
| Chestplate | `IRON_CHESTPLATE` | Weight and authority; the show's central piece |
| Leggings | `IRON_LEGGINGS` | Matched to chestplate |
| Boots | `LEATHER_BOOTS` (dyed dark — `#3B2A1A`) | Someone paid attention here |
| Main hand | `IRON_AXE` + Sharpness I enchant | The Vindicator's native weapon, made deliberate |
| Off hand | `SHIELD` | Plain shield — no banner pattern; function over sigil |

**The logic of this kit:** Iron is earned material. Chain at the head says this warrior
moves. The leather boots in a dark earthy tone are the Armorer's mark — they chose
something specific where they could have chosen nothing. The Sharpness enchant on the
axe is the kit's one glint; it tells you the Armorer took the weapon seriously. The
plain shield says the point was not decoration.

The player sees this kit in full on the Vindicator at the finale. It should read as
coherent and intentional from across the room — not miscellaneous.

### Slot fill order (recommendation to Stage Management)

The armor stand fills one slot per A-section return, in this sequence:

1. **Boots** — first return; the most specific piece, sets the Armorer's eye
2. **Leggings** — second
3. **Chestplate** — third; visual center, midpoint of the show
4. **Helmet** — fourth
5. **Shield** — fifth; the protective close
6. **Axe (main hand)** — sixth, final; weapon last, before the Vindicator arrives

The weapon fills the stand's last slot immediately before the reveal. That slot filling
is the show's penultimate beat. One moment of the stand holding all six pieces — then
the Vindicator arrives. Stage Management ratifies the timing at intake.

### YAML item strings (for Stage Management)

```
helmet:     CHAINMAIL_HELMET
chestplate: IRON_CHESTPLATE
leggings:   IRON_LEGGINGS
boots:      LEATHER_BOOTS
main_hand:  IRON_AXE
off_hand:   SHIELD
```

Enchantment on the axe (`Sharpness I`) — confirm with Stage Management whether
the plugin's `ENTITY_EQUIP` supports enchanted items via item string or requires
a separate mechanism. If not currently supported, the plain `IRON_AXE` is the
fallback; the kit holds without the enchant.

---

## The Companion — Armorer Villager

**No wardrobe intervention.** The Armorer Villager's brown robes and apron are exactly
correct. The companion should not look more interesting than the Hero. Wardrobe does
not touch them.

---

## The Armor Stand

The armor stand at home base displays the filling sequence above across all six
A-section returns. At show open: fully bare. At the penultimate A-return: five slots
filled (boots through shield). Final slot (axe) fills on the last A-return, just
before the Vindicator spawns.

The stand's visual arc is the show's spine. The kit must read as a unified object
when complete — the player should see the fully stocked stand and understand immediately
that this belongs to one person, prepared by one person, for one purpose.

---

## Wardrobe's Open Items

- Confirm with Stage Management whether `LEATHER_BOOTS` supports custom dye color
  via the plugin's `equipment:` block, or whether dye requires a separate mechanism.
  If unsupported, `LEATHER_BOOTS` (undyed) is the fallback.
- Confirm enchanted item support for main hand axe (as above).
- Both are low-priority fallback questions — the kit is coherent without them.

---

## New Direction — 2026-03-29: Item Visible in Armorer's Hand at Discovery

**Concept (added in scouting prep session):**

At the moment the Armorer "finds" each piece during an expedition, the item should
appear visibly in his hand. The intended beat per scene:

> Armorer speaks line 2 (names the piece) → item appears in main hand
> → firework fires → Armorer speaks line 3 (count + departure)

The item materializes as part of the discovery beat, simultaneous with or just before
the firework. He is visibly holding the piece when he speaks the count. He carries it
on departure.

**What Wardrobe needs to confirm:**

Can `ENTITY_EQUIP` target the Armorer Villager's main hand slot? The event is confirmed
for the Vindicator (Hero), but the Companion (Armorer Villager) has not been tested as
a target for equipment events. Confirm at intake.

If confirmed: Wardrobe authors one `ENTITY_EQUIP` per expedition scene, placing the
relevant kit piece into the Armorer's `main_hand` slot at the discovery tick. Stage
Management owns the tick alignment with the firework event.

**The return implication (raise at intake):**

If the Armorer is carrying the item on departure from the expedition site, he could
arrive at home base with it visibly in hand — then the A-section armor stand fill
doubles as the item leaving his hand and appearing on the stand. That's a
Wardrobe + Stage Management coordination question. Not required, but available
if the sequencing works cleanly.
