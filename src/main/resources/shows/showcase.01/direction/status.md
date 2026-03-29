---
show_id: showcase.01
document: Status
stage: Brief
last_updated: 2026-03-28
---

# showcase.01 "Preparing for Battle" — Current Status

## Stage
**Brief** — Gates 1 and 2 closed. Scouting prep in progress. No YAML work has begun.

## Last Session
2026-03-29. Scouting prep session — pre-scout decisions for all 7 locations.

Walked through Sites A–D in detail. Multiple cross-show decisions locked. Several
department briefs updated. Paused mid-session at Site E (boots terrain type TBD).
Sites F and G not yet discussed.

**Sites decided this session:**

**Site A — Home Base:** Near a naturally-generated pillager outpost. Tower visible
as background context. Small enclosed outbuilding with windows oriented toward tower.
Armor stand on 1-block pedestal, 6–8 blocks from player arrival. Armorer Villager
contained in a fenced area with Blast Furnace job site block. **New staging addition:**
the Vindicator waits behind a wall at home base from show open — separated from the
player by a closed iron door. Iron door default state: open. Show start: door closes
on cue (REDSTONE). Finale: door opens on cue — Vindicator steps through. Player is
contained in the workshop during the show. Iron door stop-safety cleanup cue required
(Stage Management). Spawning safety approach: 72-block distance from tower + lit
enclosure + pre-show pillager clear cue.

**Site B — The Helmet:** Natural mountain peak. Open sky. Small divot at summit
carved for precise marks. Two marks: player arrival and Armorer position.

**Site C — The Chestplate:** Badlands biome, overworld. Lava present. Exposed
mineshaft nearby as the "old work" visual anchor. Open sky.

**Site D — The Leggings:** Savanna or open plains village. Small armorer shop
beside an open road. Open sky, natural world.

**Cross-show decisions locked this session:**

- **Find-firework pattern:** A firework fires between Armorer line 2 and line 3 at
  every expedition scene — at the moment of item discovery. EXCEPTION: Site F (The
  Weapon) — no firework. The break in pattern is intentional and powerful. Fireworks
  brief updated; Mira has a recurring role in all 5 firework-eligible scenes plus
  the finale.
- **Armorer item-in-hand at discovery:** At the find moment (simultaneous with firework),
  item appears in Armorer's main hand via ENTITY_EQUIP. Wardrobe to confirm Villager
  main-hand targeting. Carry-through to A-section return (item in hand → transfers to
  armor stand) is available if sequencing works. Wardrobe brief updated.
- **Expedition camera template (all 6 scenes):** Drone spectate pattern. Invisible drone
  spawns at "far" position, moves toward Armorer via scripted CROSS_TO (smooth, no jerk).
  Player spectates drone — camera "walks up" on the Armorer already working. Drone
  arrives at close position by the find beat. Firework fires. PLAYER_SPECTATE_END,
  then TP back to home base. Mark designs this as a reusable template cue. Zarathale
  documents drone start position per site during scouting. Camera brief updated.
- **Player in spectator/frozen mode:** All expedition scenes, player does not move.
  Spectator mode is the preferred approach; confirm plugin support at intake (may be
  a Stage Management/ops-inbox item).
- **Vindicator offstage voice:** Vindicator may speak from behind the wall in text
  (different format/color, short, blunt). Armorer may address him directly. Sound
  crossover: well-timed Vindicator sounds at home base scenes — a grunt between lines
  1 and 2. Voice brief updated with full direction notes for the Voice session.
- **Script v1 partial unlock:** A-section lines require revision with Vindicator
  staging as active context. All expedition lines and finale remain locked. Voice
  brief updated.

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
Zarathale scouts all 7 locations (home base + 6 expedition sites). Pre-scout decisions
now locked for Sites A–D. Sites E–G need one more prep session before scouting, or
scout with the current brief and adjust. Key new scouting note: every site now requires
drone start position documented in addition to player arrival mark. Use the updated
`direction/scouting-field-guide.md` as the in-game reference. File report to
`direction/scouting-report.md`.

**Gate 4 — Intake Conversation: pending Gate 3**
All departments present proposals. Stage Management records commitments. Key decisions
locked at intake: equipping fill timing per A-section return, reveal tick sequence
and Hero spawn position, Fireworks scope (Mira's role in shield scene and/or finale),
Effects events per terrain site.

**YAML authoring begins after Gate 4.**

## Direction's Open Items

- **Complete pre-scout prep** — Sites E (boots), F (weapon), G (shield) not yet
  walked through. Resume in next session before scouting, OR scout with current brief
  and add specificity after. Site E was paused mid-discussion: terrain type (magma /
  soul sand / snowfield / swamp) not yet decided.
- **Set scouting** — Zarathale needs an in-world session. Updated field guide and
  blank report are at `direction/scouting-field-guide.md` and `direction/scouting-report.md`.
  New: drone start position required per site in addition to player arrival mark.
- **Intake conversation** — scheduled after scouting delivers coordinates
- **Stage Management — iron door stop-safety:** Cleanup cue needed (manually invocable)
  to open the door if show is interrupted mid-run. Player could be trapped otherwise.
  Raise at intake; SM owns.
- **Stage Management — spectator mode:** Confirm whether PLAYER_SPECTATE supports
  full player freeze during expedition scenes, or if this requires a plugin addition
  (ops-inbox). Raise at intake.
- **Voice session** — A-section lines require revision with Vindicator-behind-wall
  staging as context. Vindicator voice format and line register to be decided.
  Sound crossover (Vindicator sounds at home base) also on the Voice session agenda.
  See `departments/voice.md` for full direction notes.
- **Camera brief** — expedition drone-spectate template added today. Mark designs
  the reusable expedition camera cue at YAML time.
- **Fireworks brief** — find-firework pattern added today. Mira authors one burst
  per eligible scene. Site F: confirmed no firework.
- **Wardrobe** — confirm ENTITY_EQUIP on Armorer Villager main hand (item-in-hand
  at discovery beat). Raise at intake.
- **ROADMAP.md** — still lists this show as "The Cabinet." Update when convenient.
- **ops-inbox.md** — future capability: interactive post-show choice prompt
  (fight vs. despawn Vindicator). Noted, not blocking.
