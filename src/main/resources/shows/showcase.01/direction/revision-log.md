---
document: Revision Log
status: active — pre-production pivot recorded below
---

# Revision Log

> One entry per in-game test. Director writes the synthesis after each debrief round.
> See show-director.kb.md for the entry format.

---

## 2026-03-29 (session 2) — Shield Cut + Pre-Scout Completion

**Type:** Major structural revision (pre-YAML, no in-game test)

**Summary:**
Shield removed entirely. Site G retired. Show is now 5 expeditions. Site F (The Weapon)
is the final expedition and show climax. All department briefs updated to reflect the
change. Pre-scout prep continued: Site E locked as swamp, Site F direction given as
ruin with cleared flat central space.

**Structural decisions:**
- Kit: 5 pieces (chain helmet, iron chestplate, iron leggings, dark leather boots,
  iron axe). Shield (off-hand) removed from all documents.
- Show structure: A(open) → B → A1 → C → A2 → D → A3 → E → A4 → F → A-Final.
  Six A-sections, five expeditions, five armor stand fills.
- Firework pattern: fires at B, C, D, E. Absent at F (final expedition, climax). The
  absence now carries maximum force — the pattern breaks at the last piece.
- Script: "Six pieces" → "Five pieces" throughout. F line 3 revised ("Five. Let's go
  home." — placeholder; Voice may propose silence). A5 return removed. G scene removed.
  A-Final finale lines updated.

**Site decisions locked:**
- E (Boots): Swamp — staying overworld for this show. Effects assesses at intake.
- F (Weapon): Ruin with cleared flat central space. Effects proposes particle beat at
  axe discovery as the scene's visual beat (no firework substitute). Set must document
  cleared space dimensions for Effects assessment.

**Briefs updated this session:**
All department briefs updated: show-direction, script-v1, scouting-field-guide,
scouting-brief, fireworks, wardrobe, set, camera, stage-management, voice, effects,
sound, lighting, casting, choreography, tone, status.

**Next actions:**
1. Zarathale scouting session (Sites A–F, with Site F brief as ruin/cleared space)
2. Intake conversation (Gate 4) after scouting delivers
3. Voice session — A-section revision + F line 3 decision

---

## 2026-03-29 (session 1) — Scouting Prep + Cross-Show Creative Decisions

**Type:** Pre-scout decisions (no YAML authored, no in-game test)

**Summary:**
Scouting prep session. Walked through Sites A–D with Alan and locked pre-scout
decisions for each. Multiple significant cross-show creative decisions made that
affect department briefs.

**Site decisions locked:**
- A (Home Base): Pillager outpost setting; enclosed outbuilding; iron door + Vindicator
  holding space; fenced Armorer area with Blast Furnace.
- B (Helmet): Natural mountain peak; small divot at summit; two marks only.
- C (Chestplate): Badlands with lava and exposed mineshaft. Open sky. (Not nether.)
- D (Leggings): Savanna or open plains village; armorer shop beside road.
- E–G: Not yet discussed. Resume next session or scout with current brief.

**Cross-show decisions:**
- Find-firework pattern: burst between line 2 and line 3 at every expedition scene
  (B, C, D, E, G). Site F explicitly excluded — absence is intentional and structural.
- Expedition camera template: drone spectate dolly-in pattern for all 6 scenes.
  Player spectates drone moving from far position toward Armorer mark — smooth approach.
  Reusable template cue; drone start position added to scouting requirements.
- Armorer item-in-hand at discovery: ENTITY_EQUIP main hand at find moment, synchronized
  with firework. Wardrobe to confirm Villager targeting capability.
- Vindicator offstage presence at home base: behind wall, iron door (REDSTONE-controlled).
  Vindicator may have text voice + sounds — Voice and Sound crossover session needed.
- Script v1 partially unlocked: A-section lines need revision with Vindicator staging
  as active context. All other lines remain locked.
- Player in spectator/frozen mode during all expedition scenes. Spectator mode capability
  to be confirmed by Stage Management at intake.

**Briefs updated this session:**
- `departments/voice.md` — Vindicator offstage voice section; home base per-scene guidance
- `departments/wardrobe.md` — item-in-hand at discovery section
- `departments/camera.md` — expedition drone-spectate template section
- `departments/fireworks.md` — full rewrite; find-firework pattern as recurring structure
- `direction/scouting-field-guide.md` — updated with all locked decisions + drone start req.
- `direction/status.md` — full session update

**Next actions:**
1. Resume pre-scout prep for Sites E, F, G (next session)
2. Zarathale scouting session in-world
3. Intake conversation (Gate 4) after scouting delivers

---

## 2026-03-28 — Pre-Production Direction Pivot

**Type:** Full creative direction change (pre-YAML, no in-game test conducted)

**Summary:**
Show concept replaced entirely at the Brief stage. The Allay collection format
("The Cabinet") was retired before any YAML was authored. New direction:
"Preparing for Battle" — a rondo-format armor-equipping story featuring a devoted
companion protagonist and a Hero mob reveal finale.

**What changed:**
- Working title: "The Cabinet" → "Preparing for Battle"
- Core concept: Allay collection of curios → companion equipping a Hero for battle
- Structure: 5-vignette rondo → 6-expedition rondo (one per armor slot)
- Narrator: Sprite (plugin AI) → companion mob (species TBD by Casting)
- Reveal character: none → Hero mob in full armor kit (Casting TBD)
- All 6 expedition sites: retired, replaced with fresh slate
- All department briefs: fully rewritten to new direction
- Tonal register: "natural history documentary" → "mission log / devoted procurement"

**What did NOT change:**
- Rondo format (A-section returns with accumulating visual spine)
- Set as first-mover (scouting required before YAML)
- Non-negotiable: no YAML before all department gates close
- Stage registry requirement for all confirmed locations

**Next action:** Gate 1 — Casting proposes companion and Hero species for Alan's confirmation.
