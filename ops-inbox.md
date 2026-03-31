# ScaenaShows — Ops Inbox

Items here are queued for the Java review team. Each entry has enough context to pick up and work on independently. When resolved, move the item to the **Resolved** section with the version it shipped in.

**Filing protocol:** Any role — Claude, Zara, Smitty — can add items here. Label each with the area it affects. Java review team audits regularly for control surface gaps.

---

## Open

---

### [java-gap] Scout sidebar — human-readable display labels for mark positions

**Area:** Set Director, Stage Manager
**Feature:** Scout objectives sidebar (existing feature — label display enhancement)
**Filed:** 2026-03-30 (showcase.01 scouting session — marker quality feedback)

The scout sidebar currently displays mark codes (`1.1`, `2.1`) and raw names
(`home_base`, `companion_spawn`). The codes are useful for capture commands but are not
descriptive enough to read at a glance in-world, especially when a scout has multiple
mark types loaded simultaneously.

**Requested:** Add a `display_label` field to the objectives file format. The sidebar
uses `display_label` for human-readable role description when present, falling back to
`name`. Claude generates these labels when it builds the objectives file from show-params.

**Example objectives format with labels:**
```yaml
objectives:
  "1.1":
    name: home_base
    label: "Player Spawn : Site A"
  "2.1":
    name: companion_spawn
    label: "Armorer — Opening Position"
  "2.2":
    name: vindicator_spawn
    label: "Vindicator — Hold Position"
  "3.1":
    name: armor_stand
    label: "Armor Stand — Pedestal"
  "3.2":
    name: iron_door
    label: "Iron Door — Control Point"
  "3.3":
    name: blast_furnace
    label: "Blast Furnace — Stand On Top"
```

**Sidebar display:** Each entry shows as `[code]  [label]` — e.g., `1.1  Player Spawn — Site A`.
The code stays visible for capture command reference. The label replaces the raw name.

**Fix scope:** Extend the objectives file YAML schema to accept either a plain string
(existing format) or a map with `name:` + `label:`. The sidebar renderer checks for `label:`
and uses it if present. Backward compatible — objectives files without `label:` continue to
work as before. Claude regenerates the showcase.01 objectives file with labels once this
ships.

**Priority:** Low-medium — cosmetic but meaningfully improves the in-world scouting
experience, especially for sites with 4–6 simultaneous marks loaded.

---

### [future-capability] Preview Mode — in-world scene preview during scouting and production

**Area:** Stage Management (coordinator), Set, Casting, Wardrobe, Lighting, Camera, Voice,
Sound, Effects, Fireworks, Choreography
**Event:** New subsystem — `/scaena preview` command family
**Filed:** 2026-03-30

A production mode that materializes a show scene in-world so the builder or scout can see
what the show has designed for that location — entities at their marks, blocks in their show
state, optionally time of day set — without running the full show timeline. Stage Management
coordinates: preview stop-safety mirrors the show's cleanup contract.

**Why this matters:** During scouting, being able to see the Armorer Villager standing at
their mark and the Vindicator behind the wall answers staging questions that coordinates alone
can't. During production, standing in a scene with the full entity set and block state active
is the fastest way to catch problems before the show runs.

---

#### Command surface

```
/scaena preview load [show_id] [scene]   — materialize a scene's setup bundle in-world
/scaena preview dismiss                  — clean up all preview assets (full stop-safety)
/scaena preview status                   — list what's currently active in preview
```

Scene labels match the scout load nomenclature: `site_a`, `site_b`, … `site_f` for
showcase.01. The plugin resolves these to the appropriate scene definition.

---

#### Data source — two phases

**Phase 1 — Pre-YAML (scouting and brief stage):**
Data comes from `show-params.md` + `scout_captures/[show_id]/[date].yml`. Preview reads
scouted mark positions and entity role definitions from show-params and materializes what
it knows: entities spawned at their captured positions, block states applied, time of day
set to the show's opening tick.

This phase is explicitly limited to what show-params defines — no YAML authoring is
required to use preview mode during scouting.

**Phase 2 — Post-YAML:**
Data comes from the show YAML. Preview executes the "setup bundle" — the events that
establish scene state (SPAWN_ENTITY, BLOCK_STATE, ENTITY_EQUIP, TIME_OF_DAY) — immediately
as a point-in-time snapshot rather than on the tick timeline. Sequence events (the show's
narrative progression) are not fired in preview mode.

---

#### Department scope

| Department | Phase | What preview shows |
|---|---|---|
| Set | Phase 1 | Scout markers already visible via sidebar; no additional preview behavior |
| Casting | Phase 1 | Entities spawned at scouted positions with correct species and profession |
| Wardrobe | Phase 1 | Entities equipped from show-params kit definition at spawn |
| Lighting | Phase 1 (partial) | TIME_OF_DAY set to show's opening tick; BLOCK_STATE when that event lands |
| Camera | Phase 1 (partial) | Drone entity (e.g., a named Bat with AI off) spawned at drone start mark |
| Voice | Phase 2 | Scene's lines printed to player chat in sequence while preview is active |
| Sound | Phase 2 | Scene's ambient sounds play once on preview load |
| Effects | Phase 2 | Particle events fire once at their positions |
| Fireworks | Phase 2 | One-shot test burst at Mira's computed position |
| Choreography | Phase 2 | Entities positioned as authored in YAML |

**Out of scope for all phases:** Tick-sequenced narrative events (the show progressing
through its arc). Preview mode is a snapshot of scene state, not a partial show run.

---

#### Stop-safety contract — Stage Management owns this

Preview is covered by the same cleanup contract as the show:

- All preview-spawned entities are tracked in `PreviewSession` with `despawn_on_dismiss: true`
- All block state changes record the original `BlockData` before modification; restored on dismiss
- Player game mode is recorded at preview load and restored on dismiss
- `/scaena preview dismiss` calls `applyStopSafety()` on the `PreviewSession` — identical
  behavior to `/show stop`
- If the server crashes or the player disconnects with preview active, the same safety
  mechanism applies as for interrupted shows

The Stage Manager owns the preview cleanup contract. A preview that cannot be fully cleaned
up should not ship.

---

#### Implementation notes

`PreviewSession` is a stripped-down analogue of `RunningShow`:
- Tracks spawned entities (UUID list with despawn flag), block change records (location →
  original `BlockData`), player state snapshot (gamemode, flight), and active preview scope
  (show ID + scene label)
- `previewLoad()` builds a `PreviewSession` by reading scene data (Phase 1: show-params
  parser; Phase 2: a filtered pass over the show YAML that extracts setup events) and
  fires those events immediately against the live world
- `previewDismiss()` calls `applyStopSafety()` on the session — despawn entities, restore
  blocks, restore player state
- Reuse existing executors (`EntityEventExecutor`, `BlockStateEventExecutor` when it
  exists, `TextEventExecutor` for Voice preview) — preview is not a separate execution
  path, it's a different scheduling mode

**One preview session per player at a time.** Loading a new scene while one is active
auto-dismisses the current one first (with cleanup) before loading the new scene.

---

**Priority:** Medium-high — meaningfully improves both the scouting workflow and production
review. Phase 1 (pre-YAML, entity + block spawn) is the high-value piece; Phase 2 (YAML
integration) can follow once Phase 1 is established. Raise with Java review for scoping
before showcase.01 enters YAML authoring.

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

---

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

---

---

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

### [future-capability] Scout session snapshot log — screenshot prompts bundled with Bisect export

**Area:** Stage Management, Set Director
**Command:** `/scaena snap [label]`
**Filed:** 2026-03-30
**Context:** Scouting workflow — on location at a set site (Site A, Site B, etc.)

When scouting a set, there's no record of what Alan was looking at when he took a screenshot. Screenshots land in `.minecraft/screenshots/` on the client — the server has no access to them and cannot trigger F2. The goal isn't to capture images server-side; it's to create a log entry that travels with the Bisect export bundle so Alan can match client-side screenshots to scout positions when he pulls the session data.

**How it works:**

`/scaena snap [label]` does two things:
1. Sends a prominent title/actionbar cue to the player — "📷 Snap now — [label]" — so Alan knows to hit F2 at that exact moment
2. Logs the moment to `snapshot_log.yml` in the session folder:

```yaml
# Scout snapshot log — showcase.01 — 2026-03-30
show: showcase.01
snapshots:
  - label: "site_a_overview"
    timestamp: "2026-03-30T14:22:11"
    position: {x: 112, y: 68, z: -88, yaw: 180.0, pitch: -22.0}
    site: site_a
  - label: "companion_spawn_angle"
    timestamp: "2026-03-30T14:23:44"
    position: {x: 108, y: 64, z: -91, yaw: 90.0, pitch: 0.0}
    site: site_a
```

**Bisect export bundle — session folder layout:**
```
plugins/ScaenaShows/sessions/[show_id]/[date]/
  scout_captures.yml     ← mark positions (existing)
  snapshot_log.yml       ← new: one entry per /scaena snap call
```

Alan pulls both files from Bisect alongside each other. The `timestamp` field cross-references directly to the `.minecraft/screenshots/` filename (which is timestamp-based), so matching is unambiguous.

**Command surface:**
```
/scaena snap [label]     — log a snapshot moment and prompt F2
/scaena snap list        — print snapshot log entries for this session to chat
```

The label is freeform — `site_a_overview`, `spawn_angle`, `door_sight_line` — whatever is useful for Claude and Alan to identify what the image contains when doing post-session review.

**Priority:** Low — scouting works without it. High value-to-effort ratio since the server side is just a YAML write + title send. Implement alongside or just after `/scaena scout save`.

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

---

### [resolved] ENTITY_AI / behavior events — group resolution fixed ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Casting Director, Wardrobe
Added `resolveEntities()` (plural) to `EntityEventExecutor`. All six behavior handlers (`ENTITY_AI`, `ENTITY_SPEED`, `ENTITY_EFFECT`, `ENTITY_EQUIP`, `ENTITY_INVISIBLE`, `ENTITY_VELOCITY`) now loop over the full group list instead of calling the singular `resolveEntity()`.

---

### [resolved] ENTITY_SPEED group resolution fixed ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Choreography
Covered by the group resolution fix above — `handleEntitySpeed()` is now one of the six looped handlers.

---

### [resolved] ENTER equipment fields added ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Choreography, Wardrobe
Added six equipment fields (`helmet`, `chestplate`, `leggings`, `boots`, `main_hand`, `off_hand`) to `EnterEvent`, parsed from an `equipment:` sub-map. Added equipment-apply block and `itemOf()` helper to `StageEventExecutor.handleEnter()`.

---

### [resolved] RETURN_HOME supports non-Player entities ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Choreography
Added `spawnedEntityHomes` map to `RunningShow`; `registerSpawnedEntity()` now records spawn location as home automatically. `handleReturnHome()` now has a non-Player branch: instant teleport or pathfinder.moveTo() for mobs depending on `duration_ticks`.

---

### [resolved] FIREWORK: `min_clearance` enforced ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
Added clearance check in `handleFirework()` after resolving `loc`. Compares `world.getHighestBlockYAt(loc)` against `anchor.getY() + minClearance`. Skips launch with `log.fine()` debug entry when clearance is insufficient. Sentinel value −1 bypasses the check.

---

### [resolved] FIREWORK_FAN: `power_variation` and `color_variation` added ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
Added `powerVariation`, `colorVariation`, `gradientFrom`, `gradientTo` fields to `FireworkFanEvent`. Refactored `handleFan()` to apply variation via `resolvePower()` and `resolveColorVariation()` across the full combined arm position sequence, matching CIRCLE/LINE behavior.

---

### [resolved] Show scanner descends into subdirectories ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Stage Manager, all shows
`ShowRegistry.load()` now collects both flat `shows/*.yml` and nested `shows/[id]/[id].yml` files before the parse loop. Enables full show-folder-structure adoption. Flat files still load normally; duplicate ID detection prevents double-loading if both exist.
