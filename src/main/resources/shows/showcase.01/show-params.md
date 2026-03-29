---
show_id: showcase.01
document: Show Parameters
updated: 2026-03-29
purpose: >
  Single source of truth for structural facts about this show. Department briefs
  reference this instead of restating shared facts. A value here means it's settled
  enough to build from. TBD means it's still in play. Updating a param here is the
  act of locking — no separate ceremony needed.
---

# Show Parameters — showcase.01 "Preparing for Battle"

---

## Identity

| Param | Value |
|-------|-------|
| Show ID | showcase.01 |
| Title | "Preparing for Battle" |
| Stage | Brief — pre-YAML |
| World | Overworld (all expedition sites) |
| Duration target | ~6,000–8,400 ticks (5–7 min) |

---

## Structure

| Param | Value |
|-------|-------|
| Format | Rondo |
| Sequence | `A → B → A₁ → C → A₂ → D → A₃ → E → A₄ → F → A-Final` |
| Expedition count | 5 |
| A-sections total | 6 (opening + 4 returns + finale) |
| Armor stand fills | 5 (one per A-section return, including A-Final) |

---

## Cast

| Role | Species | Notes |
|------|---------|-------|
| Companion | Armorer Villager | Narrator, all 5 expeditions |
| Hero | Vindicator | Reveal character, fully equipped at finale |

---

## Kit (Vindicator — 5 pieces, no shield)

| Slot | Item | YAML string | Notes |
|------|------|-------------|-------|
| Helmet | Chain helmet | `CHAINMAIL_HELMET` | Mobile, not ceremonial |
| Chestplate | Iron chestplate | `IRON_CHESTPLATE` | Weight and authority |
| Leggings | Iron leggings | `IRON_LEGGINGS` | Matched to chestplate |
| Boots | Dark leather boots | `LEATHER_BOOTS` (dye `#3B2A1A`) | Confirm dye support — fallback: undyed |
| Main hand | Iron axe | `IRON_AXE` + Sharpness I | Confirm enchant support — fallback: plain axe |
| Off hand | *none* | — | Shield retired |

---

## Armor Stand Fill Order

Fills in dramatic order, independent of expedition order:

| Fill | Slot | A-section | After expedition |
|------|------|-----------|------------------|
| 1 | Boots | A₁ return | B (Helmet) |
| 2 | Leggings | A₂ return | C (Chestplate) |
| 3 | Chestplate | A₃ return | D (Leggings) |
| 4 | Helmet | A₄ return | E (Boots) |
| 5 | Axe (main hand) | A-Final | F (Weapon) — immediately before reveal |

---

## Sites

| Site | Name | Biome / Setting | Status |
|------|------|-----------------|--------|
| A | Home Base — "The Workshop" | Near pillager outpost, enclosed outbuilding | Pre-scout locked |
| B | The Helmet — "High Ground" | Natural mountain peak, open sky | Pre-scout locked |
| C | The Chestplate — "The Forge" | Badlands, lava present, exposed mineshaft | Pre-scout locked |
| D | The Leggings — "The Long Road" | Savanna/plains village, armorer shop by road | Pre-scout locked |
| E | The Boots — "The Swamp Floor" | Swamp — mud/clay/water terrain | Pre-scout locked |
| F | The Weapon — "The Choice" | Ruin with cleared central flat space | Pre-scout direction given — scout to confirm |

---

## Firework Pattern

| Site | Firework | Notes |
|------|----------|-------|
| B | ✓ Find-firework | First burst; establishes pattern |
| C | ✓ Find-firework | Warmer, badlands sky |
| D | ✓ Find-firework | Understated, road-quality |
| E | ✓ Find-firework | Tactile, swamp-register |
| F | ✗ Absent | Intentional — final expedition, absence carries max force |
| A-Final | TBD | Mira proposes burst or silence — decide at first YAML session |

Timing: between Armorer line 2 (names piece) and line 3. Simultaneous with item in hand.

---

## Key Mechanics

| Mechanic | Value | Notes |
|----------|-------|-------|
| Iron door | Closes at show open (REDSTONE), opens at A-Final | SM owns stop-safety cleanup cue |
| Vindicator offstage | Present at home base from show open, behind wall | Heard/readable; not seen until reveal |
| Reveal approach | `SPAWN_ENTITY` with full `equipment:` block | Spawn-equipped, not sequential post-spawn |
| Player mode (expeditions) | Spectator | **TBD — confirm plugin support; may need ops-inbox** |
| Expedition camera | Drone spectate dolly-in template (all 5 scenes) | Reusable cue; Site F pacing slower |
| Item in hand at discovery | `ENTITY_EQUIP` main hand at find beat | **TBD — confirm Armorer Villager main-hand targeting** |
| Particle beat at Site F | Proposed — ground particles at axe discovery | **TBD — Effects confirms viability after scouting report** |
| Vindicator offstage voice | Text lines, distinct format (color/caps), short/blunt | **TBD — Voice session: format, timing, count per A-section** |

---

## Script

| Param | Value |
|-------|-------|
| Version | v1 (revised 2026-03-29) |
| Line count | ~30 lines across 10 scenes |
| Address register | Ambiguous "you" throughout — Vindicator referent, player receiver |
| Dramatic irony | Armorer is equipping the player's opponent |
| Finale line | "I'll leave you two to it." (warm complicity) |
| Opening line | "Five pieces. Everything I need is out there — I know where each one is." |
| F line 3 | **TBD — "Five. Let's go home." (placeholder) or silence; Voice decides** |
| A-section lines | **TBD — require revision with Vindicator-behind-wall staging as context** |

---

## YAML Readiness

YAML authoring begins when all TBD params above are resolved. Current blockers:

- [ ] Player spectator mode — plugin support confirmed or ops-inbox filed
- [ ] Armorer Villager main-hand targeting — ENTITY_EQUIP confirmed
- [ ] Scouting report delivered (Gate 3) — coordinates for all 6 locations
- [ ] Site F particle beat — Effects confirms viability
- [ ] Voice session — A-section revision, F line 3, Vindicator voice format
- [ ] Fireworks finale decision — burst or silence

When all boxes above are checked, YAML authoring begins. No separate intake meeting required.
