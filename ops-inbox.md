# ScaenaShows — Ops Inbox

Items here are queued for the Java review team. Each entry has enough context to pick up and work on independently. When resolved, move the item to the **Resolved** section with the version it shipped in.

**Filing protocol:** Any role — Claude, Zara, Smitty — can add items here. Label each with the area it affects. Java review team audits regularly for control surface gaps.

---

## Open

---

### [java-gap] FIREWORK: `min_clearance` field parsed but not enforced

**Area:** Fireworks Director
**Schema field:** `min_clearance:` on `FIREWORK`
**Filed:** 2026-03-26 (Fireworks KB build)

`FireworkEvent` parses `min_clearance` (stored as `int minClearance`, default −1), but
`FireworkEventExecutor.handleFirework()` never reads `e.minClearance`. The value is silently
ignored at runtime — rockets will launch even if the overhead clearance is below the minimum.

**Impact:** The field cannot be used to prevent rockets from detonating inside a ceiling or
low overhang. Authors may include it in YAML expecting a safety check that doesn't happen.

**Fix scope:** In `handleFirework()`, after resolving `loc`, check the world's highest
non-air block at `loc.getX(), loc.getZ()` against `anchor.getY() + minClearance`. If
clearance is insufficient, skip the launch (optionally log a debug warning). Only enforce
when `minClearance > 0` (the default −1 is the "not set" sentinel).

---

### [java-gap] FIREWORK preset `launch:` mode not applied by executor

**Area:** Fireworks Director
**Schema field:** `launch:` block in `fireworks.yml` presets
**Filed:** 2026-03-26 (Fireworks KB build)

`FireworkPreset` fully parses the `launch:` block (mode: above/random/feet, y_offset, spread)
and stores it in `FireworkLaunch`. However, `FireworkEventExecutor.spawnFirework()` receives
a pre-computed `Location` and never reads `preset.launch()`. The launch mode has no runtime
effect — rockets always spawn at the position determined by the event's `offset` and `y_mode`.

**Impact:** Authors adding `launch: mode: feet` or `launch: mode: random` to a preset expect
the rocket to spawn relative to the player's feet or at a random XZ spread, but the spawn
position is entirely controlled by the event's coordinate fields. The preset's `launch:` block
is a convincing-looking no-op.

**Fix scope:** Decision required: (a) remove `launch:` from the preset schema and document that
spawn position is always event-controlled, or (b) apply `launch.mode` in `spawnFirework()` as
a secondary adjustment — e.g., `mode: feet` overrides the Y of the resolved Location to the
anchor's foot Y + `launch.y_offset`, `mode: random` adds a random XZ spread using `launch.spread`.
Option (a) is simpler; option (b) enables preset-level spawn personality. Bring to Show Director
for a design decision before implementing.

---

---

### [java-gap] FIREWORK_FAN: no `power_variation` or `color_variation`

**Area:** Fireworks Director
**Schema field:** (fields do not exist on `FIREWORK_FAN`)
**Filed:** 2026-03-26 (Fireworks KB build)

`FireworkFanEvent` has no `powerVariation` or `colorVariation` fields. The fan executor does not
call `launchWithChase()` — it builds its own position loop and always fires `preset.power()` with
no color override. There is no way to add an energy arc or color shift across a fan's positions.

**Impact:** A fan with RAMP_UP intent must be approximated with multiple FIREWORK_LINE events.
Fans cannot use RAINBOW, GRADIENT, or ALTERNATE color schemes. Every arm position fires at its
preset's base power and colors.

**Fix scope:** Add `powerVariation` and `colorVariation` (+ `gradientFrom`/`gradientTo`) to
`FireworkFanEvent`. Refactor the fan executor to call `launchWithChase()` for the full cross-arm
position list (same as CIRCLE/LINE), passing the variation parameters. Confirm behavior: variation
runs across all positions combined (consistent with how chase already works across all arms).

---

---

---

### [java-gap] No BLOCK_PLACE / BLOCK_REMOVE event type

**Area:** Set Director, Stage Manager
**Event:** (new — does not exist)

Block modifications currently require the `COMMAND` escape hatch. COMMAND-placed blocks are outside the show's stop-safety contract and are not restored if the show is interrupted.

**Impact:** Set Director cannot use block-based set dressing safely in rehearsal or production shows. Any block modification carries permanent-world risk on interruption.

**Proposed:** Add `BLOCK_PLACE` and `BLOCK_REMOVE` event types. On `BLOCK_PLACE`, record the original block type at the target location in `RunningShow`. On show end (natural or interrupted), `applyStopSafety` restores original blocks. This brings block modification inside the cleanup contract.

---

---

### [java-gap] No smooth yaw rotation (ROTATE event)

**Area:** Effects Director (Camera specialty)
**Event:** (new — does not exist)

`FACE` is instant. No first-class primitive exists for gradual camera panning (yaw rotation without position movement). Current workaround is rapid PLAYER_TELEPORT sequences, which is imprecise.

**Proposed:** Add a `ROTATE` bar event:
```yaml
type: ROTATE
target: player | entity:spawned:Name
yaw: 90.0           # absolute target yaw, or delta: +90 for relative
duration_ticks: 40
```
Implementation: BukkitRunnable interpolating yaw per tick, changing only the yaw component without altering XYZ — parallel to `smoothMovePlayer`.

---

### [java-gap] Show scanner reads flat `shows/*.yml` only — does not scan subdirectories

**Area:** Stage Manager, all shows
**Filed:** 2026-03-25 (show folder structure work, R7 session)

The plugin's show-loading code scans `src/main/resources/shows/*.yml` at build time via
`JarFile(getFile())`. The new show folder structure places YAMLs at
`shows/[show_id]/[show_id].yml`. The scanner does not descend into subdirectories and will
not find these files.

**Impact:** Blocks full adoption of the show folder structure. YAMLs must remain as flat
files alongside their folder counterparts until this is resolved. The folder structure
currently serves as documentation/production-team organization only.

**Fix scope:** Update the `JarFile` scan loop in the show-loading code to check one level
of subdirectories. For each entry matching `shows/*/`, check for a nested YAML whose name
matches the folder name (`shows/[id]/[id].yml`). Extract the show ID from the folder name.

Example pattern to match (in addition to existing `shows/*.yml`):
```
shows/demo.archetype_sampler/demo.archetype_sampler.yml  →  show ID: demo.archetype_sampler
```

**Priority:** Medium — not blocking current shows (flat YAMLs still load), but gates full
folder-structure adoption for all shows. See `docs/show-import-process.md` for the
migration process that depends on this fix.

---

### [java-gap] ENTITY_AI and behavior events resolve only first group member

**Area:** Casting Director, Wardrobe
**Events affected:** `ENTITY_AI`, `ENTITY_SPEED`, `ENTITY_EFFECT`, `ENTITY_EQUIP`, `ENTITY_INVISIBLE`, `ENTITY_VELOCITY`
**Filed:** 2026-03-25 (Casting KB build)

`EntityEventExecutor.resolveEntity()` returns only `group.get(0)` — the first UUID — when the
target is `entity_group:<name>`. All six behavior event handlers call this singular resolver, so
any group-targeted behavior event silently skips all members except the first.

By contrast, `StageEventExecutor.resolveEntities()` (plural) correctly iterates the full group
list — `HOLD`, `FACE`, and `CROSS_TO` work correctly on groups.

**Impact:** `ENTITY_AI enabled: false` on `entity_group:chorus` only puppets the first entity.
Any choreography that relies on group-wide AI control is silently broken.

**Fix scope:** In `EntityEventExecutor`, replace `resolveEntity()` calls in all six behavior
handlers with a loop over `resolveEntities()` (matching the pattern in StageEventExecutor), or
extract a shared group-resolution helper accessible to both executors.

---

### [java-gap] `capture_mode: live` parsed but not implemented

**Area:** Casting Director
**Event:** `CAPTURE_ENTITIES`
**Filed:** 2026-03-25 (Casting KB build)

`CaptureEntitiesEvent` stores `captureMode` as a string, but `EntityEventExecutor.handleCapture()`
always performs a one-time snapshot sweep into a UUID list, regardless of the field value.
There is no re-sweep logic for `live` mode.

**Impact:** `capture_mode: live` behaves identically to `capture_mode: snapshot`. The field is
a silent no-op.

**Fix scope:** Add a runtime mechanism for live-mode groups: store the original sweep parameters
(entityType, radius, anchor) alongside the group in `RunningShow`. When a live-mode group is
targeted, perform a fresh `getNearbyEntities()` sweep at the time of event execution rather than
resolving from the stored UUID list.

---

---

### [java-gap] ENTITY_SPEED does not address entity groups

**Area:** Choreography
**Event:** `ENTITY_SPEED`
**Filed:** 2026-03-25 (Choreography KB build)

`EntityEventExecutor.handleEntitySpeed()` calls `resolveEntity()`, which returns only the first
member of an entity group (UUID list position 0). When `target: entity_group:Name` is used,
only the first captured entity receives the speed change. All other group members are silently
unaffected.

**Impact:** Choreographers cannot apply a unified speed change to a chorus group with a single
ENTITY_SPEED event. Each member must be addressed individually by name, or the group speed
must be managed through ENTITY_AI + pathfinder behavior.

**Fix scope:** In `handleEntitySpeed()`, resolve the full entity list (same pattern as
`StageEventExecutor.resolveEntities()` which handles groups correctly) and apply the speed
attribute to each member.

---

### [java-gap] ENTER does not apply equipment fields

**Area:** Choreography, Wardrobe
**Event:** `ENTER`
**Filed:** 2026-03-25 (Choreography KB build)

`StageEventExecutor.handleEnter()` spawns the entity and sets name/baby variant, but does not
apply any equipment fields. The spec implies ENTER should behave like SPAWN_ENTITY for
equipment purposes, but the executor is missing the equipment-apply block that exists in
`EntityEventExecutor.handleSpawn()`.

**Impact:** A performer who enters via ENTER always appears unequipped. If equipment is needed
at entry, the workaround is SPAWN_ENTITY at the wing mark offset + CROSS_TO to destination.

**Fix scope:** After spawning in `handleEnter()`, add the same equipment-apply block as in
`handleSpawn()` — cast to LivingEntity, get EntityEquipment, apply all six slots if non-null.
The EnterEvent model class may need equipment fields added if they aren't already parsed.

---

### [java-gap] RETURN_HOME silently skips non-Player entities

**Area:** Choreography
**Event:** `RETURN_HOME`
**Filed:** 2026-03-25 (Choreography KB build)

`StageEventExecutor.handleReturnHome()` iterates `resolveEntities()` but immediately continues
on any target that is not a Player: `if (!(entity instanceof Player player)) continue;`

A `RETURN_HOME` targeting a spawned entity or entity group silently does nothing. The spec does
not document this restriction.

**Impact:** Choreographers cannot return spawned entities to a home position using RETURN_HOME.
The workaround is CROSS_TO (back to spawn offset) or DESPAWN_ENTITY.

**Fix scope:** Implement entity home tracking in RunningShow — record each spawned entity's
spawn location as its home at SPAWN_ENTITY time. In handleReturnHome(), add a branch for
non-Player entities that CROSS_TO's or teleports them to the recorded spawn location when
`return_home` is called.

---

---

### [java-gap] No native item frame content event (SET_ITEM_DISPLAY)

**Area:** Set Director, Stage Manager
**Event:** (new — does not exist)
**Filed:** 2026-03-28 (showcase.01 — original context was "The Cabinet"; show direction
has since changed, but the capability gap remains valid for any future show using
progressive item frame display as a mechanic)

Currently the only way to set an item frame's displayed item is via the COMMAND escape
hatch:
```
/data modify entity @e[type=item_frame,name="frame_B",limit=1] Item set value {id:"...",Count:1b}
```
This works, but COMMAND is outside the stop-safety contract and requires precise entity
naming discipline.

**Proposed:** Add a `SET_ITEM_FRAME` (or more general `SET_ENTITY_DISPLAY`) point-in-time
event that sets the displayed item of a targeted item frame entity:
```yaml
type: SET_ITEM_FRAME
target: entity:world:frame_B   # or entity:spawned:Name
item: minecraft:saddle
```
Implementation: resolve the target entity, cast to `ItemFrame`, call `setItem(ItemStack)`.
Alternatively, if `SPAWN_ENTITY` gains an `entity_data:` NBT passthrough field, item frames
could be spawned with items already in them — which would also solve Wardrobe and Casting's
need for entity variant control.

**Priority:** Low — no current show depends on this. Worth addressing before a show
uses progressive item frame display as a core mechanic.

---

### [java-gap] No BLOCK_STATE event — cannot set block lit/active states via YAML

**Area:** Sound Designer, Set Director
**Event:** (new — does not exist)
**Filed:** 2026-03-29 (showcase.01 "Preparing for Battle" — home base workshop design)

No event type currently allows the show to set a block's state (e.g., `lit=true` on a blast
furnace or furnace). Block state changes can only be done via the COMMAND escape hatch, which
is outside the stop-safety contract.

**showcase.01 use case:** The Armorer's home base workshop contains a blast furnace. The show
needs to activate it (set `lit=true`) at show open to produce both the glow (light emission)
and the ambient crackle sound in-world. On show end / stop-safety, the furnace should return
to its prior state (`lit=false`).

**Proposed:** Add a `BLOCK_STATE` point-in-time event:
```yaml
type: BLOCK_STATE
target: {x: 100, y: 64, z: 200}
world_specific: true
state:
  lit: true   # block-state key/value pairs; applied via block.setBlockData()
```
Implementation: resolve the target block, read current `BlockData`, apply the specified state
fields, call `block.setBlockData(data)`. At show start, record the original `BlockData` for
each targeted block in `RunningShow`. On show end (natural or interrupted), `applyStopSafety`
restores original block data. This brings block state changes inside the cleanup contract.

**Note:** The existing `BLOCK_PLACE` / `BLOCK_REMOVE` ops-inbox item handles adding/removing
blocks. This item is narrower and distinct — it only changes the state of an already-present
block (lit, open, powered, etc.) without altering block type.

**Priority:** Medium — showcase.01 will use sound-only (`block.blastfurnace.fire_crackle`) as
a bridge until this is implemented. Visual furnace glow at home base is blocked on this fix.

**Cross-use note:** When BLOCK_STATE is implemented for the blast furnace, campfire
lighting-on-arrival (setting `lit: true` on a pre-placed, unlit campfire) is a natural
secondary use case for the same event type. Any campfire-as-set-piece that should arrive
dark and then light when the scene begins can be handled by the same `BLOCK_STATE`
mechanism. Worth documenting as a second scenario when this item is scoped for
implementation.

---

### [future-capability] Post-show interactive choice prompt

**Area:** Stage Management, Casting
**Event:** (new — does not exist)
**Filed:** 2026-03-28 (showcase.01 "Preparing for Battle" — finale design)

showcase.01 ends with a fully equipped Vindicator alive in the world. The player can
fight it or walk away — no mechanic manages this. This is the deliberate design choice
for the current show (Option A: mob lives, player decides). However, a richer option
exists: present the player with a post-show choice prompt that branches to fight (leave
the Vindicator hostile and aware) or release (DESPAWN_ENTITY fires with particle_burst).

**No current event type supports player-input branching.** All show timelines are
deterministic and linear. A choice prompt would require: (a) a clickable chat or simple
GUI event that pauses show execution and waits for player input, and (b) a branching
mechanism (conditional CUE execution based on that input).

**Proposed (high level):**
```yaml
type: PLAYER_CHOICE
prompt: "Fight, or let them go?"
options:
  - label: "Fight"
    cue: showcase.01.epilogue.fight
  - label: "Walk away"
    cue: showcase.01.epilogue.release
timeout_ticks: 200   # auto-selects "Walk away" if no input
```

**Priority:** Low — showcase.01 works without it. High value for future shows where
branching endings are part of the design. Would benefit any show with a player-agency
finale. Flag for design discussion before implementation — the branching model has
implications for show authoring architecture (linear timeline assumption).

---

### [future-capability] In-game scout capture — show-params as source of truth, bidirectional sync

**Area:** Set Director, Stage Manager
**Priority:** Low — aspirational quality-of-life for the scouting workflow
**Filed:** 2026-03-29

Currently, scouting a set requires manually noting coordinates from the F3 screen and transferring them into show-params by hand. This is error-prone and breaks the in-world flow.

**Architecture:**

The objectives file is not separately authored — it is **generated from show-params**. Every named mark or site defined in show-params becomes an objective. The in-game interface is a persistent, always-accurate reflection of what show-params expects. The capture save is the return path: positions set in-game flow back into show-params via a file Claude can pull from the server.

```
show-params (repo)
    ↓  Claude generates
scout_objectives/[show_id].yml  (pushed to Bisect server)
    ↓  plugin loads, scoreboard sidebar shown in-game
Zarathale walks and captures marks
    ↓  /scaena scout save
scout_captures/[show_id]/[date].yml  (on Bisect server)
    ↓  Claude pulls, merges
show-params (repo) — updated coordinates
```

**The objectives file (generated, not authored)**
Claude reads show-params, extracts all named marks/sites, assigns short numeric codes, and writes:
```yaml
# AUTO-GENERATED from showcase.01.show-params.md — do not edit by hand
# Regenerate by asking Claude to sync scout objectives for showcase.01
show: showcase.01
objectives:
  "1.1": stage_center
  "1.2": mark_A
  "1.3": entry_wing_L
  "1.4": entry_wing_R
  "1.5": companion_spawn
  "1.6": vindicator_spawn
```
When show-params gains a new mark, Claude regenerates this file and pushes to the server. The codes are stable as long as the mark names don't change — order is alphabetical by mark name within each site group so codes are predictable across regenerations.

**In-game sidebar display (scoreboard)**
`/scaena scout load showcase.01` loads the objectives file and registers a scoreboard sidebar for the player. The sidebar title is the show ID; each line shows the shortcode and mark name. The native Minecraft scoreboard sidebar appears on the right side of the screen — no mod required.

Zarathale can load this at any time, not just during initial scouting. If a position needs to change mid-production, he loads, walks to the new location, captures the one mark, saves. This is the **persistent interface** — it stays valid across the whole life of the show.

**Capture command**
With the sidebar loaded, Zarathale walks to each location and enters:
```
/scaena set 1.1
```
The plugin records `player.getLocation()` (X/Y/Z + yaw/pitch) against objective `1.1`, removes (or strikes through) that entry from the sidebar, and confirms in chat. Partial captures are fine — only changed marks need to be captured on a given session.

**Full command surface:**
```
/scaena scout load <show_id>    # load objectives from file, show sidebar
/scaena set <code>              # capture current position for that objective code
/scaena scout save              # write all captures this session to file on server
/scaena scout status            # list captured vs. still outstanding
/scaena scout dismiss           # hide sidebar without saving
```

**Save output**
`/scaena scout save` writes to `plugins/ScaenaShows/scout_captures/[show_id]/[date].yml`:
```yaml
# Scout capture — showcase.01 — 2026-03-29
show: showcase.01
captured:
  stage_center:    {x: 112, y: 64, z: -88,  yaw: 0.0,   pitch: 0.0}
  entry_wing_L:    {x: 108, y: 64, z: -91,  yaw: 90.0,  pitch: 0.0}
```
Only marks captured this session appear — a partial save is valid and merges cleanly with show-params (existing values for uncaptured marks are preserved).

Claude pulls this file from Bisect (access granted per session as needed), merges updated coordinates into show-params, and commits. That's the full loop — no manual transcription at any point.

**Sync discipline:**
- show-params is the canonical source. Objectives file is derived.
- When show-params marks change: regenerate objectives file, push to server.
- When in-game positions change: pull capture file, merge into show-params, commit.
- The objectives file has a `# AUTO-GENERATED` header to prevent accidental hand-edits.

**Implementation notes:**
- Sidebar: `Bukkit.getScoreboardManager()`, new `Scoreboard` with `DisplaySlot.SIDEBAR`. Remove entries on capture via `score.resetScore()`.
- `/scaena set <code>` is intentionally short — no `scout` prefix once objectives are loaded.
- Plugin reads objectives file on `load` command — no restart required.
- Captures held in memory (per-player map) until `save`.

---

### [future-idea] Human as Designer — preamble layer for department KBs

**Area:** All department KBs, production team workflow
**Priority:** Low — not blocking anything
**Filed:** 2026-03-25

Add a "Human as Designer" preamble to each department KB clarifying the creative role split: the human designer (Alan/Zara, occasionally Smitty) sets the intention; Claude proposes the form. Each show has one human designer. The preamble would appear at the top of each of the 11 dept KBs with a universal statement plus a dept-specific one-liner. Not needed now — repo is working fine — but worth revisiting if onboarding new collaborators or if Claude starts overstepping design decisions.

---

## Resolved

---

### [resolved] SPAWN_ENTITY: `variant` and `profession` fields applied ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Wardrobe, Casting Director
Added entity subtype dispatch in `EntityEventExecutor.handleSpawn()`. Villager gets `setProfession` + `setVillagerType`; Cat, Horse, Sheep, Wolf each get their typed variant setter. All casts are guarded with warning logs on invalid values.

---

### [resolved] FACE: pitch added alongside yaw ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Effects Director, Choreographer
Added `dy`, `horizontalDist`, and `pitch` computation to `StageEventExecutor.handleFace()`. Entities and players now orient vertically toward the look target.

---

### [resolved] FIREWORK_LINE: `gradient_from` / `gradient_to` parsed and passed through ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
Added `gradientFrom` and `gradientTo` fields to `FireworkLineEvent`. Executor now passes them to `launchWithChase()` instead of hardcoded `null, null`. GRADIENT color variation on FIREWORK_LINE now uses the authored palette.

---

### [resolved] TITLE_CLEAR: new event type added ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Sprite Voice Director
Added `TITLE_CLEAR` to `EventType`, `TitleClearEvent` to `TextEvents`, handler in `TextEventExecutor`, and case in `EventParser`. Sends empty title with `fade_in: 0, stay: 0, fade_out: <n>` — clean wipe, no pop.

---

### [resolved] STOP_SOUND: `sound_id` field for per-sound stop ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-27 | **Area:** Sound Designer
Added `sound_id:` field to `StopSoundEvent`. When set, executor calls `p.stopSound(soundId, category)` for targeted stop. When omitted, channel-wide behavior is unchanged.

---

### [resolved] `entity:world:Name` targeting implemented ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-25 | **Area:** Casting Director
Added `entity:world:` branch to `EntityEventExecutor.resolveEntity()`. Scans world entities by custom name. Unblocks `SET_ITEM_FRAME` and any other event using world-entity targeting.
