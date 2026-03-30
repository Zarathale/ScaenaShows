# test.tier1 — Run Sheet
**Show:** v2.13.0 Feature Verification
**Duration:** 525 ticks (~26s)
**Command:** `/show play test.tier1 <yourname>`
**Purpose:** One-pass checklist for all 6 fixes in v2.13.0. Mark each cue pass/fail.

**Setup:** Stand in an open flat area. At least 20 blocks clearance overhead for fireworks. No other cows within 20 blocks before C2 (they'll be swept by CAPTURE_ENTITIES).

---

## Checklist

| Cue | Fix | Pass | Fail | Notes |
|-----|-----|------|------|-------|
| C1  | FIX 6 — scanner | | | |
| C2  | FIX 1 — group targeting | | | |
| C3  | FIX 2 — ENTER equipment | | | |
| C4  | FIX 3 — entity RETURN_HOME | | | |
| C5a | FIX 4 — min_clearance bypass | | | |
| C5b | FIX 4 — min_clearance suppress | | | |
| C6  | FIX 5 — FAN variation | | | |

---

## C1 — Subdirectory Scanner (T0–T20)

**Intention:** Confirm that `ShowRegistry` loads shows from nested `shows/[id]/[id].yml` paths (Fix 6).

**Function:** No active event. The show loading successfully IS the test.

**Mechanics:** `ShowRegistry.load()` now runs a two-pass file collection — flat `shows/*.yml` first, then `shows/[id]/[id].yml` one level deep. `test.tier1` lives in `shows/test.tier1/test.tier1.yml`, so if it appears on `/show list`, the scanner works.

**Watch question:** Did the show appear on `/show list`? Did `/show play test.tier1` succeed without "unknown show" error? Does the title card appear in-game?

**Pass:** Title card appears. Bossbar shows "test.tier1 — v2.13.0 Verify".

**Fail:** `[ScaenaShows] Unknown show: test.tier1` on play command. Check that the jar was rebuilt after ShowRegistry.java change.

---

## C2 — Entity Group Targeting (T30–T185)

**Intention:** Verify that ENTITY_AI and ENTITY_SPEED correctly target all members of an `entity_group:` (Fix 1). Previously these events only affected the first entity in the group.

**Function:** Three cows spawn at offsets (~5 blocks from anchor), are captured as `test_herd`, then ENTITY_AI false → all 3 freeze, ENTITY_SPEED 0.3 → all 3 get speed attribute set, ENTITY_AI true → all 3 resume moving slowly.

**Mechanics:**
- T30: Three COWs spawn (despawn_on_end:true)
- T50: CAPTURE_ENTITIES radius:12 → `test_herd` group (3 UUIDs)
- T60: `ENTITY_AI target:entity_group:test_herd enabled:false` — `resolveEntities()` returns all 3, `mob.setAI(false)` on each
- T90: `ENTITY_SPEED target:entity_group:test_herd speed:0.3` — sets MOVEMENT_SPEED attribute to 0.06 on all 3
- T100: `ENTITY_AI target:entity_group:test_herd enabled:true` — all 3 resume with reduced speed
- T175: Cows despawned individually by name (particle burst)

**Watch question:** At T60, do all 3 cows freeze simultaneously? At T100, do all 3 resume moving (sluggishly, not at normal cow speed)?

**Pass:** All 3 cows freeze together at T60, all 3 move slowly after T100. No cow is unaffected.

**Fail:** Only 1 cow responds (old behavior — resolveEntity singular). If this happens, the EntityEventExecutor.resolveEntities() change didn't compile into the jar.

**Notes:** Speed 0.3 = 0.06 attribute base value (normal = 0.2). Cows at 0.06 look like they're wading through mud — visually distinct from normal cow walking.

---

## C3 — ENTER with Equipment (T200–T285)

**Intention:** Verify that ENTER can apply inventory equipment (armor + weapon) at spawn time (Fix 2). Previously EnterEvent had no equipment fields.

**Function:** Zombie spawns at wing_R (x+15, z+0 from anchor) and paths to center while wearing full iron armor with iron sword in main hand.

**Mechanics:**
- T200: ENTER entity_type:ZOMBIE name:armored_zombie from:mark:wing_R destination:mark:center duration_ticks:80
- Equipment applied in handleEnter() after spawn registration: helmet/chestplate/leggings/boots/main_hand
- Zombie paths via pathfinder.moveTo(centerLoc)

**Watch question:** Does the zombie appear at wing_R already wearing iron armor? Is the iron sword visible in its hand as it walks toward center?

**Pass:** Zombie is visibly armored when it spawns at wing_R. Iron sword visible. Armor persists to center.

**Fail:** Zombie arrives naked (no armor/weapon). EnterEvent equipment fields not parsed — check StageEvents.java EnterEvent constructor and StageEventExecutor.handleEnter().

**Notes:** Zombie may burn in daylight — that's fine, armor absorbs some damage. The test only needs the equipment to be visible on spawn. Duration 80 ticks = 4s; a zombie at normal speed covers 15 blocks in ~5s, so it may not reach center exactly but will be clearly en route.

---

## C4 — Entity RETURN_HOME (T305–T385)

**Intention:** Verify that RETURN_HOME correctly identifies a spawned entity's origin and paths it back (Fix 3). Previously entities had no home location stored — RETURN_HOME would silently fail for non-players.

**Function:** After the zombie reached center in C3, RETURN_HOME sends it back to wing_R — its spawn location from the ENTER event.

**Mechanics:**
- Home captured in `RunningShow.registerSpawnedEntity()` → `spawnedEntityHomes.put(name, entity.getLocation().clone())`
- ENTER spawns the zombie at wing_R → home = anchor + {x:15, z:0}
- T305: RETURN_HOME target:entity:spawned:armored_zombie duration_ticks:60
- `handleReturnHome()` in StageEventExecutor: non-player branch → `getSpawnedName()` reverse-lookup → `show.getEntityHome(name)` → `mob.getPathfinder().moveTo(home)`
- T380: DESPAWN_ENTITY entity:spawned:armored_zombie particle_burst

**Watch question:** Does the zombie turn around and start walking back toward wing_R (+X direction from anchor)? If standing at center looking east, the zombie should move away from you.

**Pass:** Zombie visibly changes direction and moves eastward (toward +X). Despawn particle burst appears near wing_R at T380.

**Fail:** Zombie stands still or walks randomly. Check RunningShow.registerSpawnedEntity() records home, and StageEventExecutor.getSpawnedName() reverse-lookup is present.

**Notes:** `duration_ticks: 60` (3s) gives the pathfinder a time budget. The zombie may not reach wing_R in 3s from center — what matters is that it's moving in the right direction when despawned.

---

## C5 — Firework min_clearance (T400–T448)

**Intention:** Verify that `min_clearance` correctly suppresses fireworks in low-clearance environments (Fix 4).

**Function:** Two FIREWORK events with the same preset offset. Test A uses `min_clearance:-1` (bypass sentinel — always fires). Test B uses `min_clearance:300` (threshold impossible to meet outdoors — always suppressed).

**Mechanics:**
- FireworkEvent.minClearance parsed from YAML (default: -1)
- In handleFirework(): `if (e.minClearance > 0) { check clearance... if clearance < minClearance: return; }`
- Test A: minClearance = -1 → condition false → skip check → firework fires
- Test B: minClearance = 300 → condition true → clearance = highestBlockY - anchorY ≈ 0 outdoors → 0 < 300 → return (suppressed)

**Test A (T400) watch question:** Does a scae_star_warm firework launch and explode above you?

**Test B (T435) watch question:** Is there complete silence? No rocket sound, no explosion. Nothing.

**Pass A:** Firework launches and explodes with gold/green/white star pattern.
**Pass B:** Nothing happens. Chat message arrives at T430 and T448 but no firework.

**Fail A:** No firework at T400. Check min_clearance: -1 parsing and the `> 0` guard condition.
**Fail B:** Firework fires anyway. Check that the clearance calculation correctly computes near-zero clearance outdoors.

**Notes:** This test is designed for outdoor/flat terrain where `getHighestBlockYAt()` returns approximately the player's Y or just above. In a deep cave or underground, Test B might behave differently. Run this test above ground.

---

## C6 — FIREWORK_FAN Variation (T462–T510)

**Intention:** Verify that FIREWORK_FAN correctly applies `power_variation` and `color_variation` across all arm positions (Fix 5). Previously FireworkFanEvent had no variation fields; variation was only available on FIREWORK_CIRCLE and FIREWORK_LINE.

**Function:** V-shape fan (NW=315°, NE=45°), 5 positions per arm, chase FL (first-to-last across combined 10 positions). `power_variation: RAMP_UP` → power increases from 1 to 3 across positions 0–9. `color_variation: RAINBOW` → each position cycles through red/orange/yellow/green/cyan/blue/violet.

**Mechanics:**
- T462: FIREWORK_FAN fires
- Arms: [315°, len:8, count:5] + [45°, len:8, count:5] = 10 positions total
- Chase FL: fires 0→9 with 5t interval → last launch at T462 + 45 = T507
- resolvePower(RAMP_UP, i, 10, 1): smoothly maps [0..9] to power [1..3]
- resolveColorVariation(RAINBOW, i, 10, ...): RAINBOW[i % 7] → per-position color override

**Watch question:** Do the two arms fire in V-shape (converging toward anchor)? Do the colors visibly cycle (not all the same color)? Do the later fireworks burst higher than the early ones (ramp up)?

**Pass:** V-shape fireworks with rainbow color variation (at least 3–4 distinct colors visible). Outer positions burst noticeably higher than inner positions (power ramp).

**Fail:** All fireworks same color (RAINBOW not applied). All fireworks same height (RAMP_UP not applied). These indicate FireworkFanEvent.powerVariation / colorVariation fields aren't parsed, or the executor isn't reading them.

**Notes:** With only 10 positions and 7 rainbow colors, colors will cycle: positions 0–6 get all 7 colors, positions 7–9 repeat red/orange/yellow. This is visible but subtle. The power ramp (height difference from inner to outer) is more visually obvious.

---

## General Notes

- **All 6 cues are independent.** A failure in C2 doesn't affect C3–C6.
- **Zombie persistence:** If the zombie isn't despawned by T380 (e.g. it burned and died in daylight), C4's RETURN_HOME result may be inconclusive. Run at night or in a covered area for clean C3/C4 results.
- **Cow capture:** C2 requires no other cows within 12 blocks of anchor at T50. If wild cows are present, CAPTURE_ENTITIES may capture the wrong ones.
- **Version check:** Before running, confirm server loaded `ScaenaShows-2.13.0.jar`. Check server startup log for `[ScaenaShows] v2.13.0`.
