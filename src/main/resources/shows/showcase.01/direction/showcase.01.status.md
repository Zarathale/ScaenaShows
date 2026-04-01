---
show_id: showcase.01
document: Status
stage: Brief
last_updated: 2026-04-01
---

# showcase.01 "Preparing for Battle" — Current Status

## Stage
**Brief / R0 Preview** — Gates 1 and 2 closed. Scouting in progress. Sneak preview YAML authored (R0). Tech Rehearsal Mode available (OPS-027, v2.21.0).

## Last Session
2026-04-01 (session 8). OPS-027 shipped — Tech Rehearsal Mode Phase 1 implemented. Prompt Book created and show-params.md retired.

**Session 8 work (2026-04-01):**

- **Tech Rehearsal Mode Phase 1 implemented (OPS-027).** New Java subsystem: `TechManager`,
  `TechSession`, `PromptBook`, `PromptBookLoader`, `PromptBookWriter`, `TechHotbarListener`,
  `TechPanelBuilder`, `TechSidebarDisplay`, `TechActionbarTask`, `PlayerStateSnapshot`.
  Entry point: `/scaena tech [show_id] [scene_id]`. Phase 1 scope: scene materialization
  (Casting, Wardrobe, Set, Lighting, Fireworks), mark capture, param adjustment, save-back
  to prompt-book.yml.
- **Prompt Book created.** `showcase.01.prompt-book.yml` authored — single source of truth
  for all committed structural and content decisions. Migrates all data from show-params.md:
  6 scenes (site_a–site_f), cast, wardrobe kit, set states, lighting arc, fireworks pattern,
  script lines, 15 params, readiness block.
- **show-params.md retired and archived.** `showcase.01.show-params.md` archived to
  `_archive/show-params/showcase.01.show-params.md`. Prompt Book is the authoritative
  committed-state file going forward.
- **Version bumped to 2.21.0.** MINOR bump — new subsystem (Tech Rehearsal Mode).

**What's next:**
1. **Build and test in-game.** Invoke from home_base: `/show play showcase.01.preview [PlayerName]`.
   Confirm REDSTONE polarity (OPEN ITEM [2]) before first run.
2. **Gate 3 scouting** — Zarathale scouts Sites B–F per `showcase.01.scouting-field-guide.md`.
   Site A already scouted 2026-03-30.
3. **Tech mode available.** After scouting delivers coordinates for Sites B–F, enter tech mode
   (`/scaena tech showcase.01 [scene_id]`) to verify materialization and capture marks.

## YAML Readiness

Tracked in `showcase.01.prompt-book.yml` under `readiness:`. Current state as of 2026-04-01:

- **Scouting (Gate 3):** Site A scouted (2026-03-30). Sites B–F pending.
- **Intake (Gate 4):** Pending Gate 3. Two TBDs remain:
  - `victory_fireworks_pattern` — Mira proposes at intake
  - `victory_levitation_amplifier` — Effects holds pending ceiling clearance confirmation
- **YAML authoring:** Begins after Gate 4. R0 sneak preview (Scene A + A-Final) authored
  ahead of full gate sequence — non-portable, static anchor.

---

**Previously: 2026-03-31 (sessions 6–7):**

**Session 7 (2026-03-31):**
- R0 sneak preview YAML authored. Four files created at 2.20.0: `showcase.01.preview.yml`, `showcase.01.preview.fight.yml`, `showcase.01.preview.walk_away.yml`, `showcase.01.coda.victory.yml`.
- Run sheet written. `showcase.01.run-sheet.md` fully populated for R0.
- Version bumped to 2.20.0. MINOR bump — new cues and show file added.
- REDSTONE polarity flagged as OPEN ITEM [2].

**Session 6 work (2026-03-31):**

- **Battle sequence params locked (all from Alan):**
  - `death_line`: "WORTHY." ✓
  - `foe_health_multiplier`: 1.5 → 36 HP ✓
  - `foe_speed_multiplier`: 0.5 — heavy, slow, intentional ✓
  - `foe_scale_multiplier`: 1.5 — physically imposing; new param, added to show-params
    and OPS-026 scope. Applied via `generic.scale` at `SPAWN_ENTITY`. Effects flagged:
    1.5× Vindicator in ceil 0–4 workshop may clip — confirm fight space clearance at intake.
- **A-Final arc staging corrected.** Vindicator never moves — he is frozen/AI-locked in
  his holding position the entire show. The iron door opens at the **start of the chosen
  branch**, not at the reveal beat. Fight branch: door opens → player steps through →
  discovers Vindicator frozen → countdown fires → GO releases AI. Walk away branch:
  door opens → "LATER." → despawn. show-direction.md §9 and §A-Final updated.
  show-params.md §Key Mechanics updated.
- **OPS-026 expanded.** `SPAWN_ENTITY` attribute support now covers health, speed, and
  scale as a unified family. All three implement via `entity.getAttribute(...).setBaseValue()`.
  showcase.01 locked values added to the OPS-026 parameters block.

---

**Previously: 2026-03-31 (session 5):**

**Session 5 work (2026-03-31):**

- **Decision point designed.** A-Final now ends with `PLAYER_CHOICE` ("What now?") presenting
  two branches: Fight and Walk away. Full voice direction settled:
  - Choice prompt: "What now?" (ShowSprite register)
  - **Fight branch:** 5-4-3-2-1 countdown (TITLE events, white → gold bold GO), AI releases,
    boss health bossbar appears. Sprite stays silent during the fight.
  - **Walk away branch:** Vindicator says "LATER." (ALL CAPS, deep red `#CC2200`, CHAT),
    then despawns (~60 ticks). Walk away fires on timeout (15s default). Voice to propose
    a closing Sprite line for this branch.
- **Vindicator spawn clarified — locked.** Vindicator spawns at Scene A open (AI locked,
  no equipment) and is present the whole show. Never despawns until show end. Each armor stand
  fill (A₁–A₄, A-Final) has a corresponding `ENTITY_EQUIP` on the Vindicator behind the wall.
  The A-Final reveal is the iron door opening — no `SPAWN_ENTITY` at the finale. Show-params
  and show-direction updated accordingly.
- **Battle sequence pattern established.** `show-params.md §Battle Sequence` is now the
  canonical show-level block for combat parameters. TBD values: `foe_health_multiplier`,
  `foe_speed_multiplier`, `victory_cue`. Alan sets these.
- **OPS-026 filed.** Boss health bossbar (`BOSS_HEALTH_BAR` event + `SPAWN_ENTITY` health
  attribute support). Parallel to OPS-009. Implement OPS-009 first; OPS-026 follows.
  Both required before A-Final YAML authoring.
- **Victory coda designed.** On Vindicator death: death line fires → 20-tick pause →
  8-second coda (levitation + fireworks). Death line proposed: "WORTHY." (TBD — Alan
  confirms). Victory coda cue ID: `showcase.01.coda.victory`. Fireworks and Effects
  both briefed.
- **Walk-away Sprite close locked:** *"The stage is yours."* — established ScaenaCraft
  MOTD line. In register.

---

**Previously: 2026-03-30 (session 4):**

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

**YAML authoring begins after Gate 4.** — R0 sneak preview (Scene A + A-Final) authored 2026-03-31 ahead of full gate sequence. Full YAML pass still pending Gate 4.

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
- **Voice — Walk away closing line:** *"The stage is yours."* — locked 2026-03-31.
- **Effects — Site F particle beat:** Bring proposal to intake once Set confirms
  cleared space dimensions. If viable: particle event at axe discovery tick.
  If not viable: silence holds alone. Both are acceptable.
- **Battle sequence — remaining TBDs:** `victory_cue` fireworks pattern (Mira proposes
  at intake); `victory_levitation_amplifier` (Effects holds pending ceiling clearance
  confirmation — fight space confirmed through iron door staging). Health, speed, scale,
  and death line all locked 2026-03-31.
- **OPS-009** ✅ shipped. PLAYER_CHOICE implemented.
- **OPS-026** ✅ shipped (v2.19.0). SPAWN_ENTITY attribute support + BOSS_HEALTH_BAR.
  A-Final YAML authoring unblocked pending scouting and intake.
- **OPS-027** ✅ shipped (v2.21.0). Tech Rehearsal Mode Phase 1. Use `/scaena tech showcase.01
  [scene_id]` to enter. Phase 2 (YAML cue navigation) deferred.
