# ScaenaShows — Ops Inbox

Items here are queued for the Java review team. Each entry has enough context to pick up and work on independently. When resolved, move the item to the **Resolved** section with the version it shipped in.

**Filing protocol:** Any role — Claude, Zara, Smitty — can add items here. Label each with the area it affects. Java review team audits regularly for control surface gaps.

**Numbering:** Each item gets a permanent `OPS-###` identifier assigned at filing time. The number sticks with the item whether it's open or resolved — use `OPS-###` to refer to items in other docs, status files, and chat. New items take the next available number.

---

## Open

---

### OPS-001 [superseded] Scout sidebar — human-readable display labels for mark positions
> **Superseded by OPS-027.** Scout mode is retired when Tech Mode ships. The label
> display requirement is absorbed into the Tech Mode sidebar (§ Actionbar + Sidebar
> in the architecture doc). Do not implement OPS-001 as a standalone fix.


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

### OPS-002 [superseded] Preview Mode — in-world scene preview during scouting and production
> **Superseded by OPS-027 (Tech Rehearsal Mode).** `TechSession` replaces `PreviewSession`.
> There is no longer a separate preview subsystem. See `kb/system/tech-rehearsal-architecture.md`.


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

### OPS-003 [java-gap] FIREWORK preset `launch:` mode not applied by executor

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

### OPS-004 [java-gap] No BLOCK_PLACE / BLOCK_REMOVE event type

**Area:** Set Director, Stage Manager
**Event:** (new — does not exist)

Block modifications currently require the `COMMAND` escape hatch. COMMAND-placed blocks are outside the show's stop-safety contract and are not restored if the show is interrupted.

**Impact:** Set Director cannot use block-based set dressing safely in rehearsal or production shows. Any block modification carries permanent-world risk on interruption.

**Proposed:** Add `BLOCK_PLACE` and `BLOCK_REMOVE` event types. On `BLOCK_PLACE`, record the original block type at the target location in `RunningShow`. On show end (natural or interrupted), `applyStopSafety` restores original blocks. This brings block modification inside the cleanup contract.

---

---

### OPS-005 [java-gap] No smooth yaw rotation (ROTATE event)

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

### OPS-006 [java-gap] `capture_mode: live` parsed but not implemented

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

### OPS-007 [java-gap] No native item frame content event (SET_ITEM_DISPLAY)

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

### OPS-008 [java-gap] No BLOCK_STATE event — cannot set block lit/active states via YAML

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

### OPS-009 ~~[future-capability]~~ → **RESOLVED** — see Resolved section below

---

### OPS-026 ~~[future-capability]~~ → **RESOLVED in 2.19.0** — see Resolved section below

**Area:** Stage Management, Sprite Voice Director, Casting Director
**Event:** New subsystem — `BOSS_HEALTH_BAR` + `SPAWN_ENTITY` health attribute support
**Filed:** 2026-03-31 (showcase.01 A-Final battle sequence design)
**Parallel to:** OPS-009 (PLAYER_CHOICE) — implement OPS-009 first; OPS-026 builds on it

#### Need

The current `BOSSBAR` event is scripted — progress depletes on a timer, not tied to entity
health. A combat boss fight needs a reactive health bar: displays the foe's current HP as a
fraction of max HP, updating in real time as the entity takes damage.

Two capabilities are required together:

**1. `SPAWN_ENTITY` attribute support** (health, speed, scale)
Add optional attribute fields to the `SPAWN_ENTITY` event schema. All three are in the
same implementation family — resolve together:

```yaml
type: SPAWN_ENTITY
...
max_health: 36.0    # generic.max_health — 24 HP base × 1.5 multiplier for showcase.01
speed: 0.5          # generic.movement_speed — 0.5 for showcase.01 (heavy and slow)
scale: 1.5          # generic.scale — 1.5× visual/physical size for showcase.01
```

Implementation notes:
- `max_health`: `entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(value)` then `entity.setHealth(value)`
- `speed`: `entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value)`
- `scale`: `entity.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(value)` — Paper 1.20.5+ / MC 1.21 attribute. Verify Paper version supports `GENERIC_SCALE` before implementing. `generic.scale` affects both visual size and hitbox.

**Effects note (showcase.01):** A 1.5× Vindicator in the workshop (ceil 0–4) may clip the ceiling. Effects flags this at intake — levitation amplifier for the victory coda may need adjustment or the fight may naturally carry the player to a more open space through the iron door. Stage Management confirms fight space clearance.

Without attribute support, `foe_health_multiplier`, `foe_speed_multiplier`, and `foe_scale_multiplier` in show-params have no runtime effect.

**2. `BOSS_HEALTH_BAR` event**
A new bar event that links a bossbar's progress to an entity's live health ratio:
```yaml
type: BOSS_HEALTH_BAR
target: entity:spawned:The Vindicator
title: "The Vindicator"
color: RED         # from show-params bossbar_color
overlay: PROGRESS  # from show-params bossbar_overlay
audience: participants
```

**Lifecycle:**
- Fires at start of the fight branch (at or just after the GO beat)
- Progress = `currentHP / maxHP` — updates on each `EntityDamageByEntityEvent` or
  `EntityRegainHealthEvent` targeting the tracked entity
- Dismisses on entity death (progress reaches 0, bar fades out)
- Dismissed on `/show stop` via stop-safety (same cleanup contract as BOSSBAR)

**Runtime model:**
`RunningShow` gains:
- `activeBossBars` map: entity UUID → `BossBar` instance
- `registerBossBar(UUID entityId, BossBar bar)` — adds to cleanup list
- `EntityDamageByEntityEvent` listener: if damaged entity UUID is in `activeBossBars`,
  recompute progress and update bar
- `EntityDeathEvent` listener: if killed entity UUID is in `activeBossBars`, animate bar
  to 0 then dismiss

**YAML hook (from fight branch cue):**
```yaml
- at: 0
  type: BOSS_HEALTH_BAR
  target: entity:spawned:The Vindicator
  title: "The Vindicator"
  color: RED
  overlay: PROGRESS
  audience: participants
```

#### Death line + victory hook

When the tracked entity dies, two things fire in sequence:

**1. Death line** (immediate, on `EntityDeathEvent`):
The show fires a `MESSAGE` event in the Vindicator's text format — ALL CAPS, deep red
`#CC2200`, CHAT — to all participants. The entity is dead; this is Sprite-delivered text
in his register, not entity speech. `death_line_to_coda_ticks` pause follows.

**2. Victory cue** (fires after the pause):
The `victory_cue` ID from show-params is injected as a branch cue into the running show
(same mechanism as `PLAYER_CHOICE` branch injection in OPS-009). It runs inside the same
`RunningShow` with full show context. For showcase.01, the victory coda is:
- `ENTITY_EFFECT: levitation` applied to the player for `victory_cue_duration` (160 ticks)
- Fireworks bursts during the levitation window (Fireworks Director designs the pattern)
- Show closes naturally at end of coda duration; stop-safety cleans up

**Runtime:** `BOSS_HEALTH_BAR` listener handles `EntityDeathEvent`:
1. Animate bar to 0 and dismiss
2. Fire death line MESSAGE to participants
3. Schedule `victory_cue` injection after `death_line_to_coda_ticks`
4. `injectBranchCue(victoryCue)` runs the coda; show closes at coda end

#### showcase.01 parameters (from show-params §Battle Sequence)

- `foe_name`: "The Vindicator" (entity custom name at spawn + bossbar title)
- `foe_health_multiplier`: `1.5` → 36 HP *(locked 2026-03-31)*
- `foe_speed_multiplier`: `0.5` — heavy and slow *(locked 2026-03-31)*
- `foe_scale_multiplier`: `1.5` — 1.5× size *(locked 2026-03-31)*
- `death_line`: "WORTHY." *(locked 2026-03-31)*
- `bossbar_color`: RED
- `bossbar_overlay`: PROGRESS

**Priority:** Medium — required for showcase.01 A-Final YAML authoring. Implement
after OPS-009 is in place. `SPAWN_ENTITY` health attribute is the simpler half;
implement it first as it unblocks setting `foe_health_multiplier` even before the
reactive bossbar is ready.

---

### OPS-010 [superseded] In-game scout capture — show-params as source of truth, bidirectional sync
> **Superseded by OPS-027 (Tech Rehearsal Mode).** Phase 1 of Tech Mode delivers the
> bidirectional show-params sync described here as a core feature of TechSession, including
> direct write-back to show-params.md on save. Scout mode and its command family are retired.


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

### OPS-011 [scope migrated] Scout session snapshot log — screenshot prompts bundled with Bisect export
> **Scout mode is retired (see OPS-027).** The `/scaena snap` command concept remains valid
> but is now a Tech Mode enhancement rather than a scout feature. Re-scope and re-file as
> an addition to OPS-027 when Phase 1 is stable.


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

Alan pulls both files from Bisect alongside each other. The `timestamp` field is used for **fuzzy matching** against `.minecraft/screenshots/` filenames — not precise sync. Screenshot filenames are timestamp-based, but player reaction time and system lag mean the F2 press happens some seconds after the prompt. Use a **±30 second window** around the logged snap timestamp when matching. Claude does the matching during post-session review; the log entry just needs to narrow the field.

**Retake discipline — last shot wins:**
Alan may take multiple shots from the same mark position within a session — different angles, retakes, corrections. The log accumulates all snaps for a given label rather than overriding. When Claude matches and exports, only the **last snap entry for each label** (latest timestamp) is used. Earlier entries for the same label are treated as discarded retakes. This means Alan can `/scaena snap door_angle`, adjust position, `/scaena snap door_angle` again, and the earlier one is automatically superseded — no explicit delete needed.

The log entry format reflects this: all entries are written, and a `superseded: true` flag (or equivalent) is applied by Claude at review time to any non-final duplicate label.

**Command surface:**
```
/scaena snap [label]     — log a snapshot moment, prompt F2 ("📷 Snap now — [label]")
/scaena snap list        — print snapshot log for this session to chat (shows all entries, flags superseded)
```

The label is freeform — `site_a_overview`, `spawn_angle`, `door_sight_line` — whatever is useful for Claude and Alan to identify what the image contains when doing post-session review. Repeated use of the same label is expected and handled.

**Priority:** Low — scouting works without it. High value-to-effort ratio since the server side is just a YAML write + title send. Implement alongside or just after `/scaena scout save`.

---

### OPS-012 [future-idea] Human as Designer — preamble layer for department KBs

**Area:** All department KBs, production team workflow
**Priority:** Low — not blocking anything
**Filed:** 2026-03-25

Add a "Human as Designer" preamble to each department KB clarifying the creative role split: the human designer (Alan/Zara, occasionally Smitty) sets the intention; Claude proposes the form. Each show has one human designer. The preamble would appear at the top of each of the 11 dept KBs with a universal statement plus a dept-specific one-liner. Not needed now — repo is working fine — but worth revisiting if onboarding new collaborators or if Claude starts overstepping design decisions.

---

### OPS-027 [shipped] Tech Rehearsal Mode Phase 1 — Prompt Book + in-world scene materialization

**Area:** Stage Management (coordinator), all departments
**Feature:** New subsystem — `/scaena tech` command family + TechSession
**Filed:** 2026-04-01
**Shipped:** 2026-04-01 — v2.21.0
**Building spec:** `kb/system/ops-027-building-spec.md`

**Phase 1 delivered:**
- `showcase.01.prompt-book.yml` — authoritative committed state (replaces show-params.md)
- `PromptBook` data model + `PromptBookLoader` + `PromptBookWriter`
- `TechSession` + `TechManager` — session lifecycle, LOAD, DISMISS, TOGGLE, CAPTURE, SAVE
- `TechHotbarListener` — hotbar routing + async chat interception for text params
- `TechPanelBuilder` — clickable chat panel; `TechActionbarTask` — live actionbar; `TechSidebarDisplay` — scoreboard sidebar
- `/scaena tech <showId> [sceneId]` entry command; full subcommand surface
- `show-params.md` retired; archived to `_archive/show-params/showcase.01.show-params.md`

**Phase 2 (not yet filed):** YAML cue navigation — `TechSession` stubs already declared (`currentCueIndex`, `holdActive`).

**OPS-028** (scene numbering convention) filed below per spec §12.

Supersedes `kb/system/tech-rehearsal-architecture.md` and OPS-001, OPS-002, OPS-010, OPS-011.

---

#### Summary

Tech Rehearsal Mode is a stateful, department-aware, interactive rehearsal surface. The
player enters tech mode from in-world, a scene materializes from Prompt Book data, and
they can toggle departments on and off, reposition marks, and adjust param values —
then write changes back to disk. No typing required for any action after the entry command.

This is Phase 1 (Prompt Book integration). Phase 2 (YAML cue navigation and authoring)
is a separate filing once Phase 1 is stable.

**Architectural note:** `show-params.md` is retired as part of this implementation.
It is replaced by `[show_id].prompt-book.yml` — a well-organized YAML file that is the
single source of truth for all committed structural and content decisions about a show.
Every department pushes their final choices (casting, wardrobe, set, lighting, script
lines, params) into the Prompt Book. The plugin reads it at TechSession init and writes
back to it on SAVE. No Markdown parsing, no dual-write. See building spec §2 for full
Prompt Book architecture and §3 for the schema.

**Retires:** `/scaena scout` command family (OPS-001, OPS-010 superseded; OPS-011 migrated).
  Also retires `show-params.md` convention — content migrates to prompt-book.yml.
**Replaces:** `PreviewSession` (OPS-002 superseded). `TechSession` is the new stateful core.

---

#### Command surface

```
/scaena tech [show_id]             — enter tech mode, load first scene
/scaena tech [show_id] [scene]     — enter tech mode, load specific scene (e.g. site_a)
```

Everything after entry is hotbar items and clickable chat. No further typed commands
required during a session.

---

#### Hotbar layout (slots 5–9)

Slots 1–4 remain free for in-world block work. Slots 5–9 are dedicated to tech mode
while a TechSession is active. The plugin gives the player named items in these slots
on entry and restores their original inventory on dismiss.

| Slot | Item | Phase 1 function |
|------|------|-----------------|
| 5 | ← Prev | Navigate to previous scene |
| 6 | Hold | Freeze current state; hold before advancing |
| 7 | Go → | Navigate to next scene; load and materialize |
| 8 | Mark Capture | Right-click to capture current position as the focused mark |
| 9 | Parameter tool | Adjust non-positional param values (see below) |

---

#### Clickable chat panel

On session entry (and after any state change), the plugin sends a refreshed menu to
chat as a `TextComponent` with `ClickEvent.runCommand()` on each label. The player
clicks; no typing required.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  TECH  showcase.01 · Site A — Active
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Departments:
  [CAST ✓]  [SET ✓]  [WARDROBE ✓]  [LIGHTS ✓]
  [SOUND ✓]  [CAMERA ✓]  [EFFECTS ✓]  [FX ✓]

  Marks:
  [Focus Mark...]  [Save]  [Discard]

  [Exit Tech]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

`[Focus Mark...]` expands to a secondary panel listing all marks for the current scene
with capture status (✓ captured / pending). Clicking a mark name enters capture mode
for that mark.

---

#### TechSession — key fields

```
TechSession {
  show_id              String
  current_scene        SceneRef                  // "site_a", "site_b", etc.
  department_mask      Set<Department>           // which departments are active
  active_entities      Map<UUID, EntityRecord>   // spawned entities + owning dept
  block_snapshots      Map<Location, BlockData>  // original block state → restore on dismiss
  modified_params      Map<ParamName, Value>     // param values changed this session
  modified_marks       Map<MarkName, Position>   // marks repositioned this session
  player_state         PlayerStateSnapshot       // gamemode, flight → restore on exit
  focused_mark         String?                   // mark currently selected for capture
  capture_mode         Boolean
}
```

One TechSession per player at a time. Loading a new scene while one is active
auto-dismisses (with full cleanup) before loading the new scene.

---

#### Four core operations

**LOAD(scene, departments)**
For each active department in the mask: fire its setup assets against the live world
immediately (no tick delay) — SPAWN_ENTITY, ENTITY_EQUIP, BLOCK_STATE, TIME_OF_DAY.
Record spawned entity UUIDs and original block states in the session for stop-safety.
Player is teleported to the scene's arrival mark.

**DISMISS()**
Apply full stop-safety: despawn all session entities, restore all original block states,
restore player inventory and game mode. If modified_params or modified_marks is non-empty,
send the save prompt before completing cleanup:
```
  Tech session complete.
  [N] params updated  |  [N] marks repositioned
  [Save to show-params.md]  [Discard changes]
```
Choosing Save writes directly to show-params.md (see Write-back below).

**TOGGLE(department)**
- Currently active → despawn/restore that department's assets immediately. Remove from mask.
  Refresh clickable panel (✓ → ✗).
- Currently inactive → re-fire that department's setup events against the live world.
  Add to mask. Refresh panel (✗ → ✓).
- Wardrobe dependency: if Casting is toggled off, Wardrobe assets despawn with it. When
  Casting is re-enabled, entities respawn and Wardrobe re-applies automatically.

**CAPTURE(mark_name, position)**
Update `modified_marks` with the new position. If an entity is currently spawned at this
mark, teleport it immediately to the new position. Confirm in actionbar. Does not write
to disk until SAVE is called.

---

#### Mark Capture workflow

1. Click `[Focus Mark...]` in the clickable panel → secondary list of marks for current scene
2. Click a mark name → enters capture mode; `focused_mark` set
3. Actionbar updates: `📍 CAPTURING: companion_spawn | (x, y, z) | right-click to capture`
   Live coordinates update as the player walks.
4. Right-click Mark Capture item (slot 8) → `CAPTURE(focused_mark, player.getLocation())`
5. Actionbar confirms: `✓ companion_spawn updated` — returns to normal mode

---

#### Parameter adjustment (slot 9 — Parameter tool)

**Numeric values** (multipliers, tick counts, durations):
Right-click Parameter tool → clickable panel lists all numeric params for the current
scene. Clicking a param enters scroll-wheel mode: scroll up/down increments/decrements
the value; any entity or effect reflecting that param updates live in-world. Click to
confirm and record in `modified_params`.

**Text strings** (`death_line`, `choice_prompt`, etc.):
Clicking a text param opens an anvil GUI. The current value is pre-filled as the item
name. Player types the new value and confirms. Recorded in `modified_params`.

---

#### Display surfaces

**Actionbar (always-on during session)**
Normal mode: `TECH · showcase.01 · Site A`
Capture mode: `📍 CAPTURING: [mark_name] | (x.x, y.y, z.z) | right-click to capture`
Post-action: brief confirmation message, then returns to normal

**Sidebar (scoreboard)**
```
TECH · showcase.01
──────────────────
Site A — Active
Entities: N / N
Marks modified: N
──────────────────
CAST   ✓
WARDROBE  ✓
LIGHTS  ✓
SET    —
```

---

#### Data sources (Phase 1)

Show-params.md + `scout_captures/[show_id]/[date].yml` (latest capture file).
No show YAML required. The plugin reads mark positions from the capture file and falls
back to defaults or omits unset marks with a visual indicator.

Phase 1 does not depend on any show YAML being authored.

---

#### Write-back contract

Changes accumulate in `modified_params` and `modified_marks` during the session.
Nothing writes to disk until explicit save (via `[Save]` in the panel or on dismiss).

On save: the plugin writes updated mark positions and param values **directly to
show-params.md**. No intermediate file, no Claude merge step.

Implementation note: the write modifies specific fields/table rows in show-params.md
in place. The file format is Markdown with YAML frontmatter tables — the writer targets
named fields rather than replacing the whole file.

---

#### Stop-safety contract — Stage Management owns this

TechSession carries the same cleanup contract as a running show:

- All tech-spawned entities tracked with `despawn_on_dismiss: true`
- All block state changes record original `BlockData` before modification; restored on dismiss
- Player inventory snapshot taken at session entry; restored on dismiss
- Player game mode recorded at entry; restored on dismiss
- `/scaena tech dismiss` (or `[Exit Tech]` panel button) calls `applyStopSafety()` on the session
- Server crash or player disconnect while session is active triggers the same safety
  mechanism as an interrupted show

A Tech Mode session that cannot be fully cleaned up should not ship.

---

#### Superseded by this entry

| OPS | Status |
|-----|--------|
| OPS-001 | Superseded — scout sidebar labels absorbed into tech mode sidebar |
| OPS-002 | Superseded — `TechSession` replaces `PreviewSession` |
| OPS-010 | Superseded — bidirectional show-params sync is Phase 1 TechSession core feature |

OPS-011 (`/scaena snap`) migrated: re-file as a Phase 1 enhancement to this entry when
Phase 1 is stable.

---

**Priority:** High — blocks showcase.01 scouting (Sites B–F) and full YAML authoring.

---

## Resolved

---

### OPS-028 [resolved] Scene Numbering Convention — Stage Management defines and owns ✓
**Delivered:** 2026-04-01 | **Filed:** 2026-04-01 | **Area:** Stage Management

Convention doc authored at `kb/departments/stage-management/scene-numbering-convention.md`.
Summary in `stage-management.kb.md §Scene Numbers`. Covers: zero-padded two-digit scheme,
subscene insertion rules, decimal-aware sort, renumbering protocol, visibility (players
never see `scene_number`), and assignment authority (Kendra only).

---

### OPS-009 [resolved] PLAYER_CHOICE — interactive branching / CYOA foundation ✓
**Shipped:** 2.19.0 or earlier | **Filed:** 2026-03-28 | **Area:** Stage Management, Show Direction, all departments

**Hard fork model** — when `PLAYER_CHOICE` fires, timeline execution stops, a bossbar + sound pulse waiting loop begins, and the first participant click resolves the choice for all. The chosen branch cue fires inside the same `RunningShow` with full access to show context (spawned entities, groups, anchor). Control never returns to the parent cue.

**Runtime additions:** `RunningShow.suspended` flag + `activeChoice` (ChoiceSession) + `durationOverride`. `ShowScheduler` suspension check + `injectBranchCue(Cue)`. `ChoiceSession` owns bossbar, pulse task, timeout task, option list, and idempotent `resolve()`. `/scaena choose <n>` command routes through `ShowManager.resolveChoice()`.

**Waiting loop:** YELLOW bossbar depletes over `timeout_ticks`; sound pulse every 40 ticks; chat links displayed once on choice fire. `Stop` always injected as final option.

**`[show_id].story-map.md` convention** (Mermaid flowchart, `direction/`) deferred until first multi-branch show enters production.

*showcase.01 A-Final uses this for the fight/walk-away branch.*

---

### OPS-026 [resolved] SPAWN_ENTITY attribute support + BOSS_HEALTH_BAR event ✓
**Shipped:** 2.19.0 | **Filed:** 2026-03-31 | **Area:** Stage Management, Casting, Effects

**SPAWN_ENTITY attributes** — added optional `attributes:` block to `SpawnEntityEvent` with three fields:
- `max_health:` — absolute `generic.max_health` override; syncs `setHealth()` to match
- `speed:` — absolute `generic.movement_speed` override
- `scale:` — `generic.scale` override (1.0 = default, 1.5 = 1.5× size)

Applied in `EntityEventExecutor.handleSpawn()` after spawn, equipment, and name. All three only fire when the authored value is > 0.

**BOSS_HEALTH_BAR event** — new event type that creates an entity-linked bossbar whose progress tracks live HP in real time. Fields: `target`, `title`, `color`, `overlay`, `audience`, `death_line`, `death_line_color`, `death_line_pause_ticks`, `victory_cue`. On entity death: bar animates to 0, death line is sent to participants, victory cue is injected via `ShowManager.injectCue()` after the configured pause.

**New files:**
- `TextEvents.BossHealthBarEvent` — event model
- `EntityCombatListener` — Bukkit `EntityDamageByEntityEvent` + `EntityDeathEvent` listener; registered in `ScaenaShowsPlugin.onEnable()`

**Other changes:** `RunningShow.BossHealthBarTracker` record + `bossHealthBars` map; `ShowManager.injectCue()` public method; `ExecutorRegistry` routes `BOSS_HEALTH_BAR` to `EntityEventExecutor`; `EventType.BOSS_HEALTH_BAR` added; `EventParser` case added.

*showcase.01 A-Final YAML authoring is now unblocked (pending OPS-009 PLAYER_CHOICE — already shipped).*

---

### OPS-013 [resolved] SPAWN_ENTITY: `variant` and `profession` fields applied ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Wardrobe, Casting Director
Added entity subtype dispatch in `EntityEventExecutor.handleSpawn()`. Villager gets `setProfession` + `setVillagerType`; Cat, Horse, Sheep, Wolf each get their typed variant setter. All casts are guarded with warning logs on invalid values.

---

### OPS-014 [resolved] FACE: pitch added alongside yaw ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Effects Director, Choreographer
Added `dy`, `horizontalDist`, and `pitch` computation to `StageEventExecutor.handleFace()`. Entities and players now orient vertically toward the look target.

---

### OPS-015 [resolved] FIREWORK_LINE: `gradient_from` / `gradient_to` parsed and passed through ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
Added `gradientFrom` and `gradientTo` fields to `FireworkLineEvent`. Executor now passes them to `launchWithChase()` instead of hardcoded `null, null`. GRADIENT color variation on FIREWORK_LINE now uses the authored palette.

---

### OPS-016 [resolved] TITLE_CLEAR: new event type added ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-26 | **Area:** Sprite Voice Director
Added `TITLE_CLEAR` to `EventType`, `TitleClearEvent` to `TextEvents`, handler in `TextEventExecutor`, and case in `EventParser`. Sends empty title with `fade_in: 0, stay: 0, fade_out: <n>` — clean wipe, no pop.

---

### OPS-017 [resolved] STOP_SOUND: `sound_id` field for per-sound stop ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-27 | **Area:** Sound Designer
Added `sound_id:` field to `StopSoundEvent`. When set, executor calls `p.stopSound(soundId, category)` for targeted stop. When omitted, channel-wide behavior is unchanged.

---

### OPS-018 [resolved] `entity:world:Name` targeting implemented ✓
**Shipped:** 2.12.0 | **Filed:** 2026-03-25 | **Area:** Casting Director
Added `entity:world:` branch to `EntityEventExecutor.resolveEntity()`. Scans world entities by custom name. Unblocks `SET_ITEM_FRAME` and any other event using world-entity targeting.

---

### OPS-019 [resolved] ENTITY_AI / behavior events — group resolution fixed ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Casting Director, Wardrobe
Added `resolveEntities()` (plural) to `EntityEventExecutor`. All six behavior handlers (`ENTITY_AI`, `ENTITY_SPEED`, `ENTITY_EFFECT`, `ENTITY_EQUIP`, `ENTITY_INVISIBLE`, `ENTITY_VELOCITY`) now loop over the full group list instead of calling the singular `resolveEntity()`.

---

### OPS-020 [resolved] ENTITY_SPEED group resolution fixed ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Choreography
Covered by the group resolution fix above — `handleEntitySpeed()` is now one of the six looped handlers.

---

### OPS-021 [resolved] ENTER equipment fields added ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Choreography, Wardrobe
Added six equipment fields (`helmet`, `chestplate`, `leggings`, `boots`, `main_hand`, `off_hand`) to `EnterEvent`, parsed from an `equipment:` sub-map. Added equipment-apply block and `itemOf()` helper to `StageEventExecutor.handleEnter()`.

---

### OPS-022 [resolved] RETURN_HOME supports non-Player entities ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Choreography
Added `spawnedEntityHomes` map to `RunningShow`; `registerSpawnedEntity()` now records spawn location as home automatically. `handleReturnHome()` now has a non-Player branch: instant teleport or pathfinder.moveTo() for mobs depending on `duration_ticks`.

---

### OPS-023 [resolved] FIREWORK: `min_clearance` enforced ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
Added clearance check in `handleFirework()` after resolving `loc`. Compares `world.getHighestBlockYAt(loc)` against `anchor.getY() + minClearance`. Skips launch with `log.fine()` debug entry when clearance is insufficient. Sentinel value −1 bypasses the check.

---

### OPS-024 [resolved] FIREWORK_FAN: `power_variation` and `color_variation` added ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
Added `powerVariation`, `colorVariation`, `gradientFrom`, `gradientTo` fields to `FireworkFanEvent`. Refactored `handleFan()` to apply variation via `resolvePower()` and `resolveColorVariation()` across the full combined arm position sequence, matching CIRCLE/LINE behavior.

---

### OPS-025 [resolved] Show scanner descends into subdirectories ✓
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Stage Manager, all shows
`ShowRegistry.load()` now collects both flat `shows/*.yml` and nested `shows/[id]/[id].yml` files before the parse loop. Enables full show-folder-structure adoption. Flat files still load normally; duplicate ID detection prevents double-loading if both exist.

---

### OPS-028 [open] Scene Numbering Convention — Stage Management owns

**Area:** Stage Management
**Filed:** 2026-04-01
**Priority:** Medium — needed before prompt-book schema stabilizes across multiple shows

Stage Management defines and documents the decimal scene numbering scheme used in
`prompt-book.yml` `scene_number` fields. The numbering determines scene sort order in
Tech Mode navigation and any future scene-indexed features.

Convention currently in use: decimal strings (`"00"`, `"00.1"`, `"01"`, `"10.1"`) sorted
via `Double.parseDouble()` comparison (so "00.1" = 0.1 < "01" = 1.0 < "10.1" = 10.1).
Stage Management documents the convention, the rules for inserting subscenes, and how
renumbering works when scenes are added or removed.

Unblock before: adding a second show with complex scene structure to the prompt-book schema.
