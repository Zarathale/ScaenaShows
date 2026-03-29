---
show_id: showcase.01
department: Stage Management & Production
document: Department Brief
updated: 2026-03-29
---

# Stage Management — showcase.01 "Preparing for Battle"

## What This Department Serves

Stage Management owns the show's sequence logic, timing, and the two most technically
precise moments in "Preparing for Battle": the armor stand fill sequence on each
A-section return, and the Hero reveal sequence in the finale.

Stage Management also owns the intake process — collecting commitments from all
departments, sequencing gates, and ensuring no YAML is authored before its dependencies
are resolved.

---

## The Armor Stand Fill Sequence

On each A-section return, one slot on the armor stand is filled. This is the show's
visual spine and must land as a small, satisfying beat — not a ceremony, not a footnote.

**Mechanics:**
The armor stand is an entity. Stage Management uses `ENTITY_EQUIP` events to populate
one slot at a time, sequenced to the appropriate A-section return. The equipping fires
during or just before the player's TP back to home base — so the player arrives and
the change is already present, or arrives and sees it happen within the first few seconds.

**Timing decision for intake:**
- Option A: The slot fills *before* the TP home fires — player arrives and the stand
  is already updated. Silent, structural.
- Option B: The slot fills *just after* the player arrives — the player watches it
  happen. Small event, specific beat.

Direction decides at intake based on what register fits the show. The brief suggests
Option B for the first two fills (novelty) and Option A for the middle fills
(the filling becomes routine, the stand just accumulates), with Option B returning
for the final fill before the Hero arrives. Stage Management proposes a specific schedule.

**Slot filling order:**
Wardrobe recommends the sequence. Stage Management executes it. Dramatic order:
boots → leggings → chestplate → helmet → weapon (axe). Five slots. The weapon is
last — the final expedition piece, arriving right before the Hero does. The slot
fill now aligns with the expedition: Armorer returns from Site F with the axe,
the stand's fifth slot fills, the reveal fires.

---

## The Finale Reveal Sequence

This is the show's most technically precise moment. Stage Management owns tick alignment.

**The decision: spawn-equipped vs. sequential equip**

Both are confirmed available:
- `SPAWN_ENTITY` with a fully populated `equipment:` block spawns the Hero in full kit
  instantaneously. The reveal is visual — the mob appears and it is already the image.
- Sequential `ENTITY_EQUIP` events after spawn equip the Hero slot by slot in the
  player's view. The reveal is a process — the player watches the armor go on.

**The brief recommendation:**
Spawn-equipped is likely the stronger choice for this show. The companion has already
placed each piece on the armor stand; the reveal should be *arrival*, not *another
equipping*. The Hero appearing in full kit says "this was always who this was for."
Sequential post-spawn equipping would repeat the show's spine structure in miniature
rather than conclude it.

Stage Management proposes; Casting co-decides; Direction confirms. This decision is
locked at intake and does not change.

**Reveal sequence beat structure (proposed):**

1. A-Final: companion returns from Site F (the weapon expedition). Armor stand
   fills its final slot — the axe. Five pieces. The stand is complete.
2. Hold: 40–60 ticks. The companion is still. The armor stand is full.
3. Voice's three finale lines fire ("Five pieces. Everything where it belongs." /
   "You've been good company." / "I'll leave you two to it.").
4. Hero spawn fires at the confirmed position. `SPAWN_ENTITY` with full `equipment:`.
5. Camera redirect fires if needed (if the spawn position is outside the arrival
   facing — one FACE event at the moment of spawn).
6. Fireworks fire if Mira is in the finale — at or just after the Hero's appearance.
7. Hold: 60+ ticks. The show does not rush its ending.

Exact tick values confirmed at intake based on Hero mob, lighting state, and Sound's
recommendation for the silence duration.

---

## Gate Sequence

Production gates in dependency order:

**Gate 1 (Casting):** Companion and Hero confirmed. Unlocks Wardrobe and Voice.

**Gate 2 (Wardrobe):** Armor kit designed and slot fill order confirmed. Unlocks
Set scouting brief and Stage Management's equipping sequence design.

**Gate 3 (Set):** All five expedition sites and home base scouted and coordinates
documented. Unlocks Effects, Camera, Lighting, and the bulk of YAML authoring.

**Gate 4 (Intake conversation):** All departments present proposals. Stage Management
records commitments. The following decisions are locked at Gate 4:
- Fill timing (before or after TP-home, per slot)
- Fill order (Wardrobe's recommended sequence confirmed)
- Reveal mechanic (spawn-equipped vs. sequential post-spawn)
- Hero spawn position relative to player arrival facing
- Finale sequence beat structure with tick values
- Fireworks presence and position in finale
- Effects events per site, including Site F particle beat (with Set coordinates confirmed)

No YAML is authored before Gate 4 closes.

---

## Show Structure Reference

The show's rondo structure — for Stage Management's sequencing reference:

```
A (open) → B → A₁ → C → A₂ → D → A₃ → E → A₄ → F → A-FINAL (reveal)
```

Six A-sections total: one opening, four regular returns, one finale. Five expeditions.
Five `ENTITY_EQUIP` calls on the armor stand (one per A-section return, A₁ through
A-FINAL). One `SPAWN_ENTITY` (or `SPAWN_ENTITY` + `ENTITY_EQUIP` sequence) for the
Hero at the finale. The A-FINAL section carries both the final slot fill and the reveal.

---

## YAML Items Owned by Stage Management

- Armor stand `ENTITY_EQUIP` events (five, one per A-section return)
- All teleport events (TP-out for each expedition, TP-home for each return)
- Hero `SPAWN_ENTITY` event in the finale (in coordination with Casting's confirmed
  mob type and Wardrobe's confirmed item strings)
- Finale hold timing (tick delays between armor stand full → Voice line → Hero spawn)
- Camera `FACE` event at finale if warranted (in coordination with Camera)
- Any departmental timing alignment required for the finale sequence

---

## What Stage Management Does NOT Do

- Does not make creative decisions about armor aesthetics (Wardrobe)
- Does not decide which Hero mob to cast (Casting)
- Does not author expedition content (department-owned per scene)
- Does not file capability gaps in this document — those go to `ops-inbox.md`
