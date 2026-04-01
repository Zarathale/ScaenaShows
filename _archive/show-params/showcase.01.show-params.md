---
show_id: showcase.01
document: Show Parameters
updated: 2026-03-31
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
| A | Home Base — "The Workshop" | Near pillager outpost, enclosed outbuilding | ✓ Scouted 2026-03-30 |
| B | The Helmet — "High Ground" | Natural mountain peak, open sky | Pre-scout locked |
| C | The Chestplate — "The Forge" | Badlands, lava present, exposed mineshaft | Pre-scout locked |
| D | The Leggings — "The Long Road" | Savanna/plains village, armorer shop by road | Pre-scout locked |
| E | The Boots — "The Swamp Floor" | Swamp — mud/clay/water terrain | Pre-scout locked |
| F | The Weapon — "The Choice" | Ruin with cleared central flat space | Pre-scout direction given — scout to confirm |

---

## Scouted Positions

### Site A — Home Base "The Workshop"
*Scouted: 2026-03-30 · World: world · Biome: Sparse Jungle*

| Mark | Role | X | Y | Z | Yaw | Pitch | Facing | Sky type |
|------|------|---|---|---|-----|-------|--------|----------|
| home_base | Player arrival / sets.site_a | 192.98 | 80.0 | 306.16 | -133.65 | 0.00 | Northeast | underground (ceil: 2) |
| companion_spawn | Armorer Villager spawn | 196.55 | 80.0 | 302.31 | 53.25 | 13.65 | Southwest | open sky |
| vindicator_spawn | Vindicator offstage position | 193.52 | 84.0 | 296.38 | -64.35 | 28.50 | Southeast | open sky |
| armor_stand | Armor stand entity position | 194.54 | 81.0 | 300.49 | 25.50 | 41.25 | South-Southwest | enclosed (ceil: 4) |
| iron_door | Iron door block target | 190.57 | 80.0 | 300.51 | 4.20 | 40.80 | South | underground (ceil: 0) |
| blast_furnace | Blast furnace block target | 197.70 | 80.0 | 304.29 | 87.60 | 39.00 | West | open sky |

**Blast furnace block coords** (confirm in-game before BLOCK_STATE authoring): x=198, y=79, z=304

*Full yaw/pitch values in `scout_captures/showcase.01/2026-03-30.yml` — used by YAML authoring and `/scaena scout goto`.*

**Environmental notes:**
- Main play space (home_base, armor_stand, iron_door) is genuinely enclosed — ceiling of 0–4 blocks confirms the outbuilding reads as interior. No sky fireworks here.
- Armorer and Vindicator spawn positions are open sky — consistent with positions outside or above the structure.
- Blast furnace is open sky despite being the warm interior light source. This likely means it's at an exterior wall or courtyard position. Confirm against the light_level read (block_light: 9) before finalizing BLOCK_STATE cue.
- Biome: Sparse Jungle. No gameplay impact; logged for Set context.

*Sites B–F: coordinates pending scouting runs.*

---

## Firework Pattern

| Site | Firework | Notes |
|------|----------|-------|
| B | ✓ Find-firework | First burst; establishes pattern |
| C | ✓ Find-firework | Warmer, badlands sky |
| D | ✓ Find-firework | Understated, road-quality |
| E | ✓ Find-firework | Tactile, swamp-register |
| F | ✗ Absent | Intentional — final expedition, absence carries max force |
| A-Final | ✓ Grand finale burst | Locked 2026-03-29 — big payoff, maximum impact |

Timing: between Armorer line 2 (names piece) and line 3. Simultaneous with item in hand.

---

## Key Mechanics

| Mechanic | Value | Notes |
|----------|-------|-------|
| Iron door | Closes at show open (REDSTONE). Opens at the **start of whichever branch the player chooses** at A-Final — not at the reveal moment. Fight: door opens → player steps through → Vindicator discovered (still AI-locked) → countdown fires → GO. Walk away: door opens → Vindicator says "LATER." → despawns. | SM owns stop-safety cleanup cue |
| Vindicator spawn | `SPAWN_ENTITY` fires at **Scene A open** (AI locked, no equipment) | Behind wall, present the entire show — never despawns until the show ends |
| Vindicator equip | `ENTITY_EQUIP` mirrors each armor stand fill (same A-section return, same tick window) | Player sees the stand fill; Vindicator is being equipped simultaneously behind the wall |
| Reveal approach | Iron door opens at A-Final. No SPAWN_ENTITY at reveal — Vindicator already present and fully equipped | The reveal is the door, not a spawn |
| Player mode (expeditions) | Spectator | **TBD — confirm plugin support; may need ops-inbox** |
| Expedition camera | Drone spectate dolly-in template (all 5 scenes) | Reusable cue; Site F pacing slower |
| Item in hand at discovery | `ENTITY_EQUIP` main hand at find beat | **TBD — confirm Armorer Villager main-hand targeting** |
| Particle beat at Site F | Proposed — ground particles at axe discovery | **TBD — Effects confirms viability after scouting report** |
| Vindicator offstage voice | Sound grunt at show open; text "AXE." at A4 return | Locked 2026-03-29 — deep red `#CC2200`, all caps, CHAT event (confirm vs ACTIONBAR with Sound) |

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

## Lighting Arc

| Param | Value | Notes |
|-------|-------|-------|
| Show start time | MC tick 12,500 | Twilight — sun just below horizon, sky still holds faint color |
| Time progression | Natural clock | No authored TIME_OF_DAY events after T=0 until A-Final |
| A-Final nudge | TIME_OF_DAY: 22,000 at hold beat | Pre-dawn, fires between Armorer's last line and Vindicator spawn; guarantees sunrise during fight |
| Home base | Blast furnace (L13, warm orange) | Anchor light source; lanterns TBD after scouting. BLOCK_STATE not yet implemented — sound bridge only until then |
| Site B | No block light proposed | Open sky at dusk (~MC 13,200); Set assesses after scouting |
| Site C | Lava (L15) if adjacent | Set scouts lava proximity; lava IS the light source if present |
| Site D | Regular campfire (L15, warm) | Waystation atmosphere + mob containment (~14-block no-spawn radius) |
| Site E | Soul campfire (L10) recommended | **Decision deferred to scouting** — Zarathale captures canopy density; Steve N. decides soul vs. regular after report |
| Site F | Regular campfire (L15, warm) | Stillness + mob containment; central to cleared space; coordinates with Effects particle beat |

*Mob containment note: Java 1.18+ spawns on block light 0 only. Campfires at D, E, F protect the Armorer's mark at deep-night MC ticks.*

---

## Battle Sequence

Parameters for the post-choice combat encounter at A-Final.
**This block is the canonical pattern for Scaena battle sequences** — YAML authoring reads
these values directly rather than hardcoding. Future shows with combat define their own
block in the same format.

| Param | Value | Notes |
|-------|-------|-------|
| `foe_name` | `"The Vindicator"` | Bossbar display name; also the entity's custom name at spawn |
| `foe_health_multiplier` | `1.5` | 36 HP (24 × 1.5). Requires OPS-026 `SPAWN_ENTITY` attribute support |
| `foe_speed_multiplier` | `0.5` | Half speed — heavy and slow, intentional. Requires OPS-026 `SPAWN_ENTITY` attribute support |
| `foe_scale_multiplier` | `1.5` | 1.5× default Vindicator size — physically imposing. Applied via `generic.scale` at `SPAWN_ENTITY`. Requires OPS-026 attribute support. **Effects: note that a 1.5× Vindicator in the workshop (ceil 0–4) may clip ceiling — flag at intake.** |
| `countdown_duration` | `5 seconds` | 5-4-3-2-1 countdown before AI releases. Each beat = 1 second (20 ticks) |
| `countdown_title_color` | `white` | TITLE color for countdown numbers 5–1 |
| `go_title_style` | `gold bold` | TITLE style for the GO beat |
| `bossbar_color` | `RED` | BOSSBAR color during fight — see Voice KB color convention |
| `bossbar_overlay` | `PROGRESS` | `PROGRESS` = clean fill bar; `NOTCHED_10` = phased (revisit if fight has phase structure) |
| `choice_prompt` | `"What now?"` | PLAYER_CHOICE prompt text (ShowSprite register) |
| `choice_timeout_ticks` | `300` | 15 seconds before default branch fires |
| `choice_default` | `walk_away` | Walk away fires on timeout — safer default for multiplayer audience |
| `hold_before_choice_ticks` | `120` | Hold after Armorer's last line before PLAYER_CHOICE fires (~6 seconds) |
| `walk_away_exit_line` | `"LATER."` | Vindicator exit line — ALL CAPS, deep red `#CC2200`, CHAT delivery |
| `walk_away_despawn_ticks` | `60` | Ticks after exit line before despawn (~3 seconds) |
| `death_line` | `"WORTHY."` | Vindicator's final text on death — ALL CAPS, deep red `#CC2200`, CHAT. Fires on EntityDeathEvent (OPS-026). Locked 2026-03-31. |
| `death_line_to_coda_ticks` | `20` | Pause between death line and victory coda firing (~1 second). Let the word land. |
| `victory_cue_duration` | `160 ticks` | Victory coda length — 8 seconds |
| `victory_levitation_amplifier` | **TBD** | Levitation strength during coda. `0` = gentle; `1` = moderate. **Effects: flag ceiling clearance** — fight space ceiling TBD pending scouting and staging confirmation (see iron door / branch note in §Key Mechanics). |
| `victory_fireworks` | **TBD** | Fireworks pattern during coda — Fireworks Director to propose. More celebratory/plural than find-firework. Multiple bursts or a fan. |
| `victory_cue` | `showcase.01.coda.victory` | Cue ID for the 8-second post-fight celebration (levitation + fireworks). Requires OPS-026. |
| `walk_away_sprite_close` | `"The stage is yours."` | ShowSprite closing line after Vindicator despawns — established ScaenaCraft MOTD line. Locked 2026-03-31. |

*Base Vindicator health reference: 24 HP (12 hearts) in vanilla Java. `foe_health_multiplier` is applied via `generic.max_health` attribute at spawn — see OPS-026.*

---

## YAML Readiness

YAML authoring begins when all TBD params above are resolved. Current blockers:

- [x] Player spectator mode — confirmed. Camera owns expedition transitions via `PLAYER_SPECTATE` (verified in Camera KB). Stop-safety restores prior game mode. No ops-inbox item needed.
- [ ] Armorer Villager main-hand targeting — ENTITY_EQUIP confirmed
- [ ] Scouting report delivered (Gate 3) — coordinates for all 6 locations
- [ ] Site F particle beat — Effects confirms viability after scouting report. Decision unblocks with #3 (Set scouts cleared space dimensions and ruin structure at Site F).
- [x] Voice session — complete 2026-03-29. Script v2 locked. A-sections revised for wall staging, Vindicator voice format set (text, deep red `#CC2200`, all caps, show open sound + A4 text "AXE."), F.3 = silence. TBA flags remain (Set-dependent word-level updates only). Two small items for Sound coordination before YAML: delivery event (CHAT vs ACTIONBAR) and Vindicator sound ID.
- [x] Fireworks finale — locked 2026-03-29. Grand finale burst. Big payoff, maximum impact.

- [x] Battle sequence — `foe_health_multiplier` locked: `1.5` (36 HP). OPS-026 implementation still required before A-Final YAML.
- [x] Battle sequence — `foe_speed_multiplier` locked: `0.5` (heavy and slow). OPS-026 implementation still required.
- [x] Battle sequence — `foe_scale_multiplier` locked: `1.5`. OPS-026 attribute support covers this alongside health and speed.
- [x] Battle sequence — `death_line` locked: `"WORTHY."`
- [ ] Battle sequence — `victory_cue` fireworks pattern (Mira proposes at intake); Effects levitation amplifier (pending ceiling clearance)
- [x] OPS-026 implementation (SPAWN_ENTITY attribute support + BOSS_HEALTH_BAR) — shipped in 2.19.0 ✓

When all boxes above are checked, YAML authoring begins. No separate intake meeting required.
