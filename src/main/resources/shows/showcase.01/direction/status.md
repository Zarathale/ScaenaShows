---
show_id: showcase.01
document: Status
stage: Brief
last_updated: 2026-03-28
---

# showcase.01 "Preparing for Battle" — Current Status

## Stage
**Brief** — Full direction pivot applied and casting locked. No YAML work has begun.

## Last Session
2026-03-28. Two-part session.

**Part 1 — Direction pivot:** New working title "Preparing for Battle." Full show
concept replaced. Rondo armor-equipping story: Armorer Villager companion equips a
Vindicator Hero across 6 expeditions (one per armor slot). All direction files and
department briefs rewritten.

**Part 2 — Casting locked:** All 6 equipment slots confirmed via Java (both `SPAWN_ENTITY`
and `ENTITY_EQUIP`). Casting proposals evaluated with input from Set (Michael C.),
Voice (ShowSprite), and Wardrobe (Margaret). Option 1 locked: Armorer Villager +
Vindicator. Gate 1 closed.

Post-show behavior confirmed: Vindicator remains alive in the world after show end.
Player chooses to engage or not. No plugin mechanic required. Future interactive
post-show choice prompt added to `ops-inbox.md`.

Wardrobe delivered confirmed kit: chain helmet, iron chestplate and leggings, leather
boots (dark dye), iron axe (Sharpness I), plain shield. Fill order locked (boots first,
axe last). Gate 2 closed.

**Part 3 — Script v1 locked:** Dramatic irony confirmed — the Armorer is preparing the
Vindicator to fight the player. Ambiguous-address register through all 39 lines. Finale
beat: warm complicity ("I'll leave you two to it."). 3-line rhythm per scene established.
Script at `direction/script-v1.md`. TBA flags on 4 departure lines pending Set coordinates.

## Gate Sequence

**Gate 1 — Casting: ✅ CLOSED**
Armorer Villager (companion) + Vindicator (Hero). Both confirmed.

**Gate 2 — Wardrobe: ✅ CLOSED**
Full kit designed and slot fill order confirmed. Two minor technical questions remain
for Stage Management (leather dye support, enchanted item support) — both have clean
fallbacks and do not block scouting.

**Voice — Script v1: ✅ LOCKED**
39 lines across 13 scenes. Dramatic irony confirmed. 3-line rhythm per scene.
Finale: "I'll leave you two to it." See `direction/script-v1.md`.

**Gate 3 — Set Scouting: OPEN — next step**
Zarathale scouts all 7 locations (home base + 6 expedition sites) using the new
scouting brief. Wardrobe's kit informs the scouting lens per site. Coordinates filed
for all sites. Stage registry entries completed. Unlocks department authoring.

**Gate 4 — Intake Conversation: pending Gate 3**
All departments present proposals. Stage Management records commitments. Key decisions
locked at intake: equipping fill timing per A-section return, reveal tick sequence
and Hero spawn position, Fireworks scope (Mira's role in shield scene and/or finale),
Effects events per terrain site.

**YAML authoring begins after Gate 4.**

## Direction's Open Items

- **Set scouting** — Zarathale needs an in-world session. Scouting brief is ready.
  Six new expedition sites, one home base. Kit design in hand as the creative lens.
- **Intake conversation** — scheduled after scouting delivers coordinates
- **Stage Management technical questions** (from Wardrobe): leather boot dye color
  support; Sharpness I enchant support on `IRON_AXE` via equipment block. Both have
  fallbacks; neither blocks progress.
- **Voice line drafting** — opens at Gate 4, after intake closes and Set coordinates
  are confirmed. All lines written in one session as a set.
- **ops-inbox.md** — future capability: interactive post-show choice prompt
  (fight vs. despawn Vindicator). Noted, not blocking.
