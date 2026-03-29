---
show_id: showcase.01
department: Casting Director
document: Department Brief
updated: 2026-03-29
casting_status: LOCKED
---

# Casting — showcase.01 "Preparing for Battle"

## Confirmed Cast

**Gate 1 is closed.** Both roles are locked.

| Role | Species | Notes |
|------|---------|-------|
| **Companion** | Armorer Villager | Protagonist, narrator, all six expeditions |
| **Hero** | Vindicator | Reveal character, fully equipped at finale |

---

## The Companion — Armorer Villager

The companion is the show's narrator and the player's guide for all five expeditions.
Their voice is what we read in chat.

The Armorer Villager's professional identity is the show's register: this mob's entire
canon existence is equipping people for what's ahead. They know this armor. They know
why each piece matters. Their devotion is expressed through craft knowledge, not sentiment.

**Visual design:** No modification. The Armorer's brown robes and apron are already the
correct read. Wardrobe does not intervene on the companion's appearance. The point is
that they do epic work in work clothes.

**Relationship to the Hero:** The village armorer equipping the village's most dangerous
defender. The Armorer knows the Vindicator's fighting style, their reach, which shoulder
takes more impact. They have measured them. This knowledge is never stated — it lives in
the specificity of what the Armorer says and chooses.

**Voice register:** Craft knowledge as intimacy. Technically precise and emotionally
loaded without meaning to be. See `voice.md` for full development.

---

## The Hero — Vindicator

The Hero is revealed at the finale, fully equipped in the kit Wardrobe designed.

The Vindicator is already purposeful. They already carry an axe. When armor goes on a
Vindicator, the read is immediate: *this is someone who was already dangerous and is now
prepared.* The companion is not equipping someone helpless — they're equipping someone
who would have gone without the armor if the companion hadn't made sure they didn't.

**Java confirmation:** `SPAWN_ENTITY` with full `equipment:` block, or `SPAWN_ENTITY`
followed by `ENTITY_EQUIP` events. Vindicator supports all equipment slots via
Bukkit's `EntityEquipment` API. Confirmed. Kit is now five pieces (no shield).

**Equipment:** Per Wardrobe's confirmed kit. See `wardrobe.md`.

**Post-show behavior:** The Vindicator remains alive in the world after show end. The
player may choose to engage or not. The show does not manage this outcome — it simply
makes it possible. This is deliberate: the show's arc is preparation; what happens next
is the player's story. See `ops-inbox.md` for a future capability note on interactive
post-show choice prompts.

**Does the Hero appear before the finale?** No. The Hero is absent from all scenes
prior to the finale reveal. The armor stand fills without the Hero in frame. Their
arrival is the answer to a question the show has been building.

---

## Casting's Role Going Forward

Gate 1 is closed. Casting has no further open deliverables for this show until:

- **Intake conversation (Gate 4):** Confirm the reveal staging approach with Stage
  Management (spawn-equipped vs. sequential `ENTITY_EQUIP`). Casting's recommendation
  is spawn-equipped — the Hero arriving in full kit reads as arrival, not another equipping.
- **Post-show behavior:** If a future interactive choice mechanic is built (ops-inbox),
  Casting will need to confirm whether the Vindicator should be set to hostile AI
  immediately on spawn or held passive until the post-show choice fires.
