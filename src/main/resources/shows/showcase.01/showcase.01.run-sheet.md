---
show_id: showcase.01.preview
document: Run Sheet
revision: R0 (sneak preview)
date: 2026-03-31
author: Kendra (Stage Management)
status: active — in-game testing ready
---

# Run Sheet — showcase.01 "Preparing for Battle" — Sneak Preview (R0)

> **Preview scope:** Scene A (Home Base Open) + compressed bridge + A4 Return + A-Final + PLAYER_CHOICE.
> Inner expedition scenes B–F are stubbed. This is a structural preview, not a performance.

---

## Pre-Show Tech Check

```
□ World: world
□ Player standing at home_base mark: (192.98, 80, 306.16)
□ Time of day: any — TIME_OF_DAY 12500 fires at T=0
□ Iron door at (190, 80, 300): confirm door is in open state before invoking
□ No previous-run entities still present (Armorer, Vindicator, armor stand)
□ Plugin loaded at 2.20.0: /show list includes showcase.01.preview
□ Cues loaded: showcase.01.preview.fight, showcase.01.preview.walk_away, showcase.01.coda.victory
□ Run sheet is R0 (this document)
□ Invoke: /show play showcase.01.preview [PlayerName]
```

---

## Block Modifications

| Tick | Event | Block | Coords | State | Cleanup |
|------|-------|-------|--------|-------|---------|
| T=0 | REDSTONE | Iron door | (190, 80, 300) | on (close) | T=630 or branch open |
| T=630 | REDSTONE | Iron door | (190, 80, 300) | off (open) | Natural — door stays open at show end |

**OPEN ITEM [2]: Confirm REDSTONE polarity with Set before first run.**
Assumption: `state: on` = door held closed, `state: off` = door opens. If the circuit is inverted, flip polarity in the YAML.

**Stop-safety note:** If the show is interrupted before T=630, the iron door remains closed. Manual cleanup: apply power manually to open the door.

---

## Spawned Entities

| Entity | Name | Spawn tick | Offset from home_base | despawn_on_end |
|--------|------|------------|----------------------|----------------|
| VINDICATOR | TheVindicator | T=0 | (+0.5, +4, -9.8) | true |
| VILLAGER | TheArmorer | T=0 | (+3.6, 0, -3.9) | true |
| ARMOR_STAND | TheArmorStand | T=0 | (+1.6, +1, -5.7) | true |

All three entities despawn automatically on show end (natural or `/show stop`).

---

## Cue Reference

| Cue | File | Role |
|-----|------|------|
| showcase.01.preview | shows/showcase.01.preview.yml | Main show |
| showcase.01.preview.fight | cues/showcase.01.preview.fight.yml | Fight branch |
| showcase.01.preview.walk_away | cues/showcase.01.preview.walk_away.yml | Walk away branch |
| showcase.01.coda.victory | cues/showcase.01.coda.victory.yml | Victory coda (placeholder) |

---

## C1 — Scene A Open

**Tick range:** T=0 – T=244
**Intention:** Twilight falls on the workshop. The door clanks shut. The player is enclosed. The Armorer turns and begins. The Vindicator is already there — they just don't know it.
**Function:** TIME_OF_DAY (12500), REDSTONE (door close), SPAWN three entities, ENTITY_AI lock ×2, FACE (Armorer → player), Vindicator grunt SOUND (placeholder), three Armorer lines.
**Watch:**
- REDSTONE at T=0: does the door close? Confirm polarity. [Open Item 2]
- Entity spawns: Vindicator behind wall, Armorer near companion_spawn, armor stand at empty-stand position.
- Vindicator grunt at T=15: does it fire before the first Armorer line (T=50)?
- Armorer line pacing (T=50, T=110, T=165): too fast? Too slow?
**Notes:**
_____

---

## C2 — Preview Bridge

**Tick range:** T=245 – T=334
**Intention:** Preview scaffolding only — not a show beat. Three pieces land on the stand and on the Vindicator simultaneously to set up A4 Return.
**Function:** MESSAGE (bridge label), ENTITY_EQUIP ×2 (boots + leggings + chestplate on stand and Vindicator simultaneously).
**Watch:**
- Do all three pieces appear on the armor stand at once? Note any slot that lags.
- Vindicator should now be wearing boots + leggings + chestplate (no helmet).
**Notes:**
_____

---

## C3 — A4 Return

**Tick range:** T=335 – T=609
**Intention:** The Armorer returns from expedition E. Fill 4 (helmet) goes on the stand. Four pieces showing. One slot left. Then: one word from behind the wall.
**Function:** ENTITY_EQUIP ×2 (helmet synchronized), MESSAGE "AXE." (deep red), three Armorer lines (A4.1–A4.3).
**Watch:**
- Helmet on stand and Vindicator fires at T=345 — synchronized?
- "AXE." at T=380: deep red (#CC2200) reads distinct from Armorer gray?
- Line pacing (T=420 / T=480 / T=540): note any gaps that feel too long or short.
**Notes:**
_____

---

## C4 — A-Final

**Tick range:** T=610 – T=919
**Intention:** The final piece. The stand is complete. The sky turns. The door opens. The reveal of who it was all for. Three final lines. Then silence before the choice.
**Function:** TIME_OF_DAY (22000), ENTITY_EQUIP ×2 (axe synchronized), REDSTONE (door opens), three FIREWORK placeholder bursts, three Armorer lines (A-Final 1–3), REST (75 ticks).
**Watch:**
- Axe fills at T=620: both stand and Vindicator?
- Door opens at T=630: Vindicator fully visible through doorway in kit?
- Three fireworks (T=638/643/649): do they fire in open sky or hit the ceiling? If ceiling hit, flag for Effects with ceiling height.
- "You've been good company." (T=730): this is the first line addressed explicitly to the player — does the weight of it land in the moment?
- 75-tick REST (T=845–920) before PLAYER_CHOICE: is the hold long enough?
**Notes:**
_____

---

## C5 — PLAYER_CHOICE

**Tick range:** T=920 (open-ended)
**Intention:** The choice is presented. The bossbar depletes. First click wins. Default: walk away.
**Function:** PLAYER_CHOICE "What now?" — Fight (index 0) / Walk away (index 1). Timeout: 300 ticks (15s). Default: 1.
**Watch:**
- YELLOW bossbar appears with "What now?" as title?
- Chat links [A] Fight / [B] Walk away / [■ Stop] render and are clickable?
- Sound pulse (chime) audible every ~2 seconds while waiting?
- Timeout behavior: walk away fires automatically at 15s without a click?
**Notes:**
_____

---

## C6-F — Fight Branch

**Injected at:** PLAYER_CHOICE resolution — Fight
**Intention:** 5-4-3-2-1 countdown. GO. He's released. The health bar tracks it all the way down.
**Function:** REDSTONE (door confirms open), 5-4-3-2-1 TITLE countdown (20 ticks/beat), "GO" TITLE (gold bold) + ENTITY_AI enable, BOSS_HEALTH_BAR (RED, PROGRESS, death_line "WORTHY." → victory coda inject).
**Watch:**
- Countdown fires cleanly at T=10/30/50/70/90/110?
- AI releases on the same tick as "GO"?
- BOSS_HEALTH_BAR appears in RED?
- HP bar updates in real time when Vindicator takes damage?
- On death: "WORTHY." appears in deep red?
- Victory coda injects after 20-tick pause?
**Notes:**
_____

---

## C6-W — Walk Away Branch

**Injected at:** PLAYER_CHOICE resolution — Walk away (or timeout)
**Intention:** One word. Then he's gone. Then the stage is theirs.
**Function:** REDSTONE (door confirms open), "LATER." MESSAGE (deep red, T=25), DESPAWN_ENTITY TheVindicator (T=85, no burst), "The stage is yours." MESSAGE (T=120), REST (85 ticks).
**Watch:**
- "LATER." fires 25 ticks after door opens — beat feels right?
- Vindicator despawns cleanly (no burst) at T=85?
- "The stage is yours." lands after despawn?
**Notes:**
_____

---

## Victory Coda (placeholder)

**Injected at:** Vindicator death event → 20-tick pause
**Intention:** Brief elevation. A moment of triumph before the world resumes.
**Function:** EFFECT levitation amp 0 (80 ticks), four FIREWORK placeholder bursts (near vindicator_spawn, open sky), EFFECT slow_falling (80 ticks), REST.
**Watch:**
- Levitation fires? How high does amp 0 take the player? [Open Item A]
- Fireworks fire in open sky or hit ceiling? Note height. [Open Item B]
- Slow_falling eases the player back down smoothly?
**Notes:**
_____

---

## Open Items

| # | Item | YAML location | Owner | Unblocked by |
|---|------|---------------|-------|--------------|
| [1] | Vindicator sound ID | showcase.01.preview.yml T=15 | Sound | P3 |
| [2] | REDSTONE polarity — confirm on/off for door | All REDSTONE events | Set | Gate 3 |
| [3] | Delivery event: CHAT vs ACTIONBAR | All Armorer + Vindicator MESSAGE events | Sound + Voice | P5 |
| [4] | Grand finale fireworks preset + pattern | showcase.01.preview.yml T=638/643/649 | Fireworks | Gate 4 |
| [5] | Leather boots dye (#3B2A1A) | All ENTITY_EQUIP boots | Wardrobe | Capability confirm |
| [6] | Iron axe Sharpness I | All ENTITY_EQUIP main_hand | Wardrobe | Capability confirm |
| [A] | Victory levitation amplifier | showcase.01.coda.victory.yml T=0 | Effects | Gate 4 |
| [B] | Victory fireworks preset + pattern | showcase.01.coda.victory.yml T=10–55 | Fireworks | Gate 4 |

---

## General Findings

_Fill in after in-game run. Promote to department KBs once confirmed._

---

## Revision Log

| Rev | Date | Changes |
|-----|------|---------|
| R0 | 2026-03-31 | Initial sneak preview build. Scene A + bridge + A4 Return + A-Final + PLAYER_CHOICE. 4 YAML files. |
