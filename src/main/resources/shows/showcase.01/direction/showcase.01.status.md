---
show_id: showcase.01
document: Status
stage: Brief
last_updated: 2026-03-30
---

# showcase.01 "Preparing for Battle" — Current Status

## Stage
**Brief** — Gates 1 and 2 closed. Scouting prep in progress. No YAML work has begun.

## Last Session
2026-03-30 (session 4). Site A scouted. Preview Mode filed to ops-inbox.

**Session 4 work (2026-03-30):**

- **Site A scouted.** Full coordinates in show-params: home_base, companion_spawn,
  vindicator_spawn, armor_stand, iron_door, blast_furnace. Environmental data captured.
  Biome: Sparse Jungle. Main play space confirmed enclosed (ceiling 0–4). Blast furnace
  reads as open sky — at exterior wall or courtyard position; confirm before BLOCK_STATE
  authoring. Armorer spawn also open sky. Vindicator at Y=84, 4 blocks above workshop
  floor. Sites B–F still pending.
- **Preview Mode filed to ops-inbox.** Two entries: scout sidebar display labels
  (small, cosmetic) and Preview Mode subsystem (Phase 1: pre-YAML entity + block
  spawn from show-params; Phase 2: YAML-driven snapshot). Stage Management owns the
  cleanup contract. All departments have preview scope defined. Filed 2026-03-30.

---

**Previously locked (session 3, 2026-03-29):**

- **Shield cut / Site G retired.** The show is now 5 expeditions: helmet, chestplate,
  leggings, boots, weapon. No shield. No Site G. Site F (The Weapon) is the final
  expedition and the show's climax. All department briefs updated.
- **Show structure:** A(open) → B → A1 → C → A2 → D → A3 → E → A4 → F → A-Final.
  Six A-sections total (opening + 4 regular returns + A-Final). Five expedition sites.
- **Script revised:** "Six pieces" → "Five pieces" throughout. F line 3 revised to
  "Five. Let's go home." (placeholder; Voice may propose silence at intake). A5 return
  and Scene G removed entirely. A-Final unchanged in structure; finale lines updated.
- **Site E — Boots: LOCKED as Swamp.** Overworld only for this show. Swamp terrain;
  earthy, distinctive underfoot. Effects assesses swamp sensation at intake.
- **Site F — Weapon: pre-scout direction.** Ruin with cleared central space, flat
  open area. Effects proposes particle beat at axe discovery moment as the scene's
  visual beat (substituting for the absent firework). Effects to confirm viability
  once Set scouts the specific site. Set must document cleared space dimensions and
  surrounding structure for Effects assessment.

**Session 3 work (2026-03-29):**

- **Lighting arc locked.** Show starts MC 12,500 (twilight). Natural clock progression
  through the show. TIME_OF_DAY: 22,000 nudge fires at A-Final hold beat to guarantee
  sunrise during the Vindicator fight.
- **Campfire decisions made** for Sites D and F. D = regular campfire (warm, waystation).
  F = regular campfire (small, he came back here). Site E (Swamp): soul campfire
  recommended; decision deferred to scouting — Zarathale captures canopy conditions,
  Steve decides after report is filed.
- **Lighting KB expanded** with Block Light Reference, Set Piece Light Authority, and
  Scene Light Estimation sections. Steve now tracks light environment per scene.
- **Set brief updated** with nighttime mob spawning note — campfires serve as
  atmosphere AND spawn containment at dark expedition sites.
- **ops-inbox updated** — BLOCK_STATE entry annotated with campfire cross-use note.

**Previously locked (session 1, 2026-03-29):**

- Site A–D pre-scout decisions (home base, helmet, chestplate, leggings) — all locked
- Find-firework pattern (fires at B, C, D, E; explicitly absent at F)
- Expedition camera template (drone spectate dolly-in, all 5 scenes)
- Armorer item-in-hand at discovery (ENTITY_EQUIP)
- Vindicator offstage presence / iron door staging
- Script v1 partial unlock (A-section lines need revision)

## Gate Sequence

**Gate 1 — Casting: ✅ CLOSED**
Armorer Villager (companion) + Vindicator (Hero). Both confirmed.

**Gate 2 — Wardrobe: ✅ CLOSED**
Five-piece kit designed and slot fill order confirmed. Shield removed. Two minor
technical questions remain (leather dye support, enchanted item support) — both have
clean fallbacks and do not block scouting.

**Voice — Script v1: ✅ LOCKED (revised)**
Revised 2026-03-29. Five expeditions. "Five pieces" throughout. F line 3 and
A-Final updated. G scene removed. See `showcase.01.script-v1.md`.

**Gate 3 — Set Scouting: OPEN — next step**
Zarathale scouts 6 locations (home base + 5 expedition sites B through F).
Pre-scout decisions locked for A–E. Site F not yet fully walked through — ruin
with cleared central space is the direction; Zarathale scouts with this as the
brief. Use `showcase.01.scouting-field-guide.md`. File report to
`showcase.01.scouting-report.md`.

**Gate 4 — Intake Conversation: pending Gate 3**
Key decisions at intake: equipping fill timing, reveal tick sequence, Hero spawn
position, Fireworks at finale (Mira proposes burst or silence), Effects at Sites
E and F (swamp sensation + weapon particle beat viability), Voice F line 3 (line
vs. silence).

**YAML authoring begins after Gate 4.**

## Direction's Open Items

- **Site F pre-scout prep** — direction given (ruin, cleared flat space, particle-viable).
  Zarathale scouts with this brief. No further prep session needed unless Site F
  presents unexpected terrain challenges.
- **Set scouting** — Zarathale ready to go. Field guide and blank report at
  `showcase.01.scouting-field-guide.md` and `showcase.01.scouting-report.md`.
  Drone start position required per site.
- **Intake conversation** — scheduled after scouting delivers coordinates.
- **Stage Management — iron door stop-safety:** Cleanup cue needed. Raise at intake.
- **Stage Management — spectator mode:** Confirm PLAYER_SPECTATE capability. Raise at intake.
- **Voice session** — A-section lines need revision with Vindicator staging active.
  F line 3 open: "Five. Let's go home." or silence. Voice decides at intake.
- **Effects — Site F particle beat:** Bring proposal to intake once Set confirms
  cleared space dimensions. If viable: particle event at axe discovery tick.
  If not viable: silence holds alone. Both are acceptable.
- **ops-inbox.md** — future: interactive post-show choice prompt. Noted, not blocking.
