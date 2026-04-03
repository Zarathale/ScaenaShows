# ScaenaShows — Ops Inbox

Items here are queued for the Java review team. Each entry has enough context to pick up and work on independently. When resolved, move the item to the **Resolved** section with the version it shipped in.

**Filing protocol:** Any role — Claude, Zara, Smitty — can add items here. Label each with the area it affects. Java review team audits regularly for control surface gaps.

**Numbering:** Each item gets a permanent `OPS-###` identifier assigned at filing time. The number sticks with the item whether it's open or resolved — use `OPS-###` to refer to items in other docs, status files, and chat. New items take the next available number.

---

## Open

---

### OPS-001 ~~[superseded]~~ → absorbed by OPS-027 — see Resolved section below

---

### OPS-002 ~~[superseded]~~ → absorbed by OPS-027 — see Resolved section below

---

### OPS-003 ~~[java-gap]~~ → **RESOLVED in 2.24.0** — see Resolved section below

---

---

---

---

---

### OPS-004 ~~[java-gap]~~ → **RESOLVED in 2.23.0** — see Resolved section below

---

---

### OPS-005 [java-gap] No smooth yaw rotation (ROTATE event)

**Area:** Effects Director (Camera specialty), Choreography
**Event:** (new — does not exist)
**Filed:** 2026-03-25

`FACE` is instant. No first-class primitive exists for gradual camera panning (yaw rotation without position movement). Current workaround is rapid PLAYER_TELEPORT sequences, which is imprecise.

#### Schema

**ROTATE** — bar; smoothly rotate target's yaw to a destination angle
```yaml
type: ROTATE
target: player | entity:spawned:Herald | group_1
yaw: 270.0          # absolute target yaw in degrees
                    # OR
delta: +90.0        # relative rotation from current yaw (+ = clockwise, – = counter-clockwise)
duration_ticks: 40  # 0 or omitted = instant (point-in-time; same effect as FACE compass:...)
```

`yaw` and `delta` are mutually exclusive. If both are present, `delta` wins.

**Yaw convention:** matches Minecraft's — 0.0 = south, 90.0 = west, 180.0 = north, 270.0 = east.

#### Implementation notes

`BukkitRunnable` fires once per tick from current yaw toward target yaw, linear interpolation across `duration_ticks`. XYZ is not touched — pure rotation only. On completion the task self-cancels; on `/show stop`, the RunningShow cleanup loop cancels any active rotate tasks.

**Wrap-around:** when `yaw:` (absolute) is used, normalize the delta to [−180, +180] before stepping so the interpolator always takes the shorter arc. With `delta:` the author has explicit control and the sign is respected as-written — a `delta: +350.0` really does spin 350° clockwise.

**For entities:** `entity.teleport(entity.getLocation())` with only yaw updated each tick — same approach as CROSS_TO's repeated-teleport path but yaw-only. **For players:** same repeated-teleport approach; no NMS required.

**Stop-safety:** active ROTATE tasks go in `RunningShow.activeRotateTasks` (Set<BukkitTask>), cancelled in `stopShow()` alongside other bar tasks. No state restoration needed — yaw is left wherever the pan ends.

**Relationship to FACE:** `ROTATE` with `duration_ticks: 0` or omitted is functionally identical to `FACE`. No deprecation — FACE remains the readable choice for snap cuts. ROTATE is for pans.

**Priority:** Low — no current show is blocked. High value-to-effort once camera work begins on showcase.02 or showcase.03.

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

### OPS-007 [java-gap] No native item frame content event (SET_ITEM_FRAME)

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

#### Schema

**SET_ITEM_FRAME** — point-in-time
```yaml
type: SET_ITEM_FRAME
target: entity:world:frame_east | entity:spawned:frame_east
item: minecraft:saddle          # namespaced item ID; minecraft:air clears the frame
# optional
visible: true                   # show/hide the frame border; default: no change
fixed: true                     # lock frame so players can't interact; default: no change
rotation: 0                     # 0–7, maps to Rotation enum (NONE through CLOCKWISE_315)
```

`visible`, `fixed`, and `rotation` are no-change when omitted — authors only touch what they care about. `item: minecraft:air` calls `frame.setItem(null)` (empty frame), consistent with Minecraft's own representation.

#### Implementation notes

Resolve target entity via existing `entity:world` / `entity:spawned` path → cast to `ItemFrame` → `frame.setItem(new ItemStack(Material.matchMaterial(item)))`. Apply `visible`, `fixed`, and `rotation` fields when present.

**Stop-safety for `entity:world` targets:** before modifying, snapshot original item (`frame.getItem().clone()`), `visible`, and `rotation` into a new `RunningShow.itemFrameRestoreMap` (Map<UUID, ItemFrameSnapshot>). Restore all entries in `stopShow()`. For `entity:spawned` targets the frame is despawned at show end — no snapshot needed.

**Priority:** Low — no current show depends on this. Worth addressing before any show uses progressive item frame display as a core mechanic. Replaces the `COMMAND` NBT workaround and brings item frame state into the stop-safety contract.

---

### OPS-008 ~~[java-gap]~~ → **RESOLVED in 2.23.0** — see Resolved section below

---

### OPS-009 ~~[future-capability]~~ → **RESOLVED** — see Resolved section below

---

### OPS-026 ~~[future-capability]~~ → **RESOLVED in 2.19.0** — see Resolved section below

---

### OPS-010 ~~[superseded]~~ → absorbed by OPS-027 — see Resolved section below

---

### OPS-011 ~~[closed]~~ — `/scaena snap` concept not re-filed; scouting workflow absorbed by OPS-027 TechSession

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

**Phase 2:** Filed as OPS-029. YAML cue navigation, cue-to-cue step mode, `PAUSE` event type, YAML write-back. `TechSession` stubs already declared (`currentCueIndex`, `holdActive`).

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

### OPS-029 [java-gap] Tech Rehearsal Mode Phase 2 — Cue-to-Cue navigator and YAML authoring surface

**Area:** Stage Management (coordinator), all departments
**Feature:** Phase 2 of `/scaena tech` — step-mode show execution, cue-to-cue navigation, in-world param editing, YAML write-back
**Filed:** 2026-04-01
**Spec:** `kb/system/tech-rehearsal-phase2-spec.md`
**Depends on:** OPS-027 (Phase 1, shipped v2.21.0)

#### What Phase 2 adds

Phase 1 answered *"do we have the scene assembled correctly?"* — entities at marks,
blocks in place, params committed to the Prompt Book.

Phase 2 answers *"does the show play correctly, moment to moment?"* — step through
authored cues, preview event sequences live in-world, edit timing and params, and
write changes back to the show YAML without leaving the game.

#### The execution invariant

Phase 2 preview execution **is** the show. It uses `RunningShow`, `ShowScheduler`,
and `ExecutorRegistry` without modification or subclassing. No adapter. No
`TechRunningShow`. What changes is how the scheduler is driven: production uses a
BukkitRunnable game-tick clock; Phase 2 uses demand-driven dispatch.

#### Two modes

**Edit mode** — mutable in-memory YAML (`TechCueSession`), clickable panel for param
edits, nothing executing. Dirty-tracked. Explicit save writes back to disk.

**Preview mode** — `RunningShow` + `ShowScheduler` constructed from current in-memory
YAML, scheduler in step mode. Player navigates with hotbar items, sees results live.
Exit triggers full stop-safety (entity cleanup, block restore).

#### Cue-to-cue navigation model

Navigation jumps between **pause points**: top-level `CUE` references in the show
timeline, `PAUSE` events (new event type, see below), and end-of-show.

- **GO (slot 7):** dispatch all events up to the next pause point (`dispatchEventsUpTo(nextPauseTick)`). Auto-follow event bursts fire as a unit — same as tight cue sequences in theatre.
- **HOLD (slot 6):** interrupt mid-sequence; gate further dispatch.
- **PREV (slot 5):** rewind — stop RunningShow (stop-safety), construct fresh instance, fast-forward synchronously to previous pause point, hold.
- **Jump to cue:** from the clickable panel; same mechanism as PREV targeting a named cue.

#### New event type: PAUSE

`HOLD` retains its current meaning (entity freeze). A new `PAUSE` event type is the
show-level narrative pause point for Phase 2 navigation. It has a no-op executor —
safe to author in a production YAML, harmless if the scheduler is not in step mode.

```yaml
- at: 120
  type: PAUSE
  label: "After the declaration"   # optional — shown in sidebar and panel
```

`PAUSE` is recognized by the step scheduler as a navigation stop. `ExecutorRegistry`
registers it with a no-op executor.

#### ShowScheduler additions

```java
// Dispatch all events at the next scheduled tick; advance cursor. Returns tick or -1 if done.
public long dispatchNextEventTick();

// Fast-forward: dispatch all events from current tick up to and including targetTick. Synchronous.
public void dispatchEventsUpTo(long targetTick);

// Gates the BukkitRunnable tick-advance loop when true.
boolean steppingMode;
```

#### New classes

| Class | Role |
|-------|------|
| `TechCueSession` | Editor state: mutable raw YAML, cursor tick, source file, dirty flag, preview RunningShow ref |
| `ShowYamlEditor` | Targeted refinement helper — four operations only: param patch, tick shift, event insert (into existing cue), event remove. Does not create new cues. |
| `TechCuePanel` | Clickable chat panel renderer for Phase 2 state (timeline list, params, mode buttons) |

`TechManager` gains Phase 2 lifecycle methods: `startPhase2()`, `enterPreview()`,
`stepForward()`, `stepBack()`, `exitPreview()`, `saveYaml()`.

#### Sidebar — preview mode

Rolling timeline cursor: last events fired, current hold position, next on GO.

```
─ CUE-TO-CUE ─────────────────
  ✓ cast.arrival.zarathale   C2
  ✓ ENTITY_EQUIP × 4
  ✓ ACTION_BAR  "Warrior."
  ━ HOLDING ─────────────────
  ▷ lights.snap.battle_ready  C3
────────────────────────────────
  Events: 12   Tick: 340
```

Actionbar in preview: `[PREVIEW]  C2 · cast.arrival.zarathale  ·  tick 340`

#### Open questions (resolved before Java)

Q1 — HOLD duality: **resolved** — new `PAUSE` event type (see above).

Q2 — In-game cue *creation*: **out of scope for Phase 2 — resolved 2026-04-02.**
Phase 2 is a refinement tool for YAML from a prior authoring pass. Blank-canvas cue
creation belongs to Phase 2.1. See spec §8 Q2 for full rationale.

Q4 — Partial/early YAML handling: **open — to explore next session.** Does Phase 2
handle sparse show YAMLs gracefully? What are the edge cases when the file is a rough
first-pass skeleton rather than a complete authored timeline? See spec §8 Q4.

Q3 — PREV rewind cost: **accept the stutter.** Tech mode is not performance-critical.
Revisit if it becomes a real problem in a long show.

#### Deliverables checklist

- [ ] `EventType` — add `PAUSE`
- [ ] `EventParser` — map `PAUSE` to minimal model (tick + optional label)
- [ ] `ExecutorRegistry` — register `PAUSE` with no-op executor
- [ ] `ShowScheduler` — `steppingMode`, `dispatchNextEventTick()`, `dispatchEventsUpTo()`
- [ ] `TechCueSession` — editor state model
- [ ] `ShowYamlEditor` — YAML mutation helper
- [ ] `TechManager` — Phase 2 lifecycle methods
- [ ] `TechCuePanel` — panel renderer for Phase 2 state
- [ ] Sidebar scoreboard — timeline cursor display in preview mode

**Priority:** Medium — Phase 1 must be stable in production before Phase 2 Java begins.

---

## Resolved

---

### OPS-003 [resolved] Remove `launch:` from firework preset schema ✓
**Resolved:** 2026-04-02 | **Filed:** 2026-03-26 | **Area:** Fireworks Director
**Version:** 2.24.0

`launch:` block was a convincing-looking no-op — parsed into `FireworkLaunch` and stored in `FireworkPreset`, but `spawnFirework()` received a pre-resolved `Location` and never consulted `preset.launch()`. Removed per Option A decision: presets own appearance (power, stars, colors, trail, flicker); spawn position is the event's responsibility via `offset` + `y_mode`.

**Changes shipped:**
- Deleted `FireworkLaunch.java` model class
- Removed `FireworkLaunch launch` field and `FireworkLaunch.from()` call from `FireworkPreset.java`
- Stripped all `launch:` blocks from `fireworks.yml` (12 presets cleaned)
- Updated `fireworks.kb.md`: preset structure example, limitations note, capability table row
- Updated `kb/system/spec.md`: removed `launch:` from both preset schema examples

---

### OPS-001 [resolved] Scout sidebar display labels — superseded by OPS-027 ✓
**Resolved:** 2026-04-01 | **Filed:** 2026-03-30 | **Area:** Set Director, Stage Manager

Superseded by OPS-027. Scout mode retired; display label requirement absorbed into the Tech Mode sidebar (TechSidebarDisplay). No standalone implementation.

---

### OPS-002 [resolved] Preview Mode — in-world scene preview — superseded by OPS-027 ✓
**Resolved:** 2026-04-01 | **Filed:** 2026-03-30 | **Area:** Stage Management, all departments

Superseded by OPS-027. `TechSession` replaces `PreviewSession` as the stateful in-world materialization surface. The PreviewSession design was the direct predecessor; its stop-safety contract and department-scope model carried forward into TechSession.

---

### OPS-010 [resolved] In-game scout capture / bidirectional show-params sync — superseded by OPS-027 ✓
**Resolved:** 2026-04-01 | **Filed:** 2026-03-29 | **Area:** Set Director, Stage Manager

Superseded by OPS-027. Bidirectional write-back to the Prompt Book is Phase 1 TechSession's core SAVE operation. Scout command family retired.

---

### OPS-011 [closed] `/scaena snap` screenshot log — not re-filed ✓
**Closed:** 2026-04-02 | **Filed:** 2026-03-30 | **Area:** Stage Management, Set Director

Concept was migrated when scout mode retired, pending re-filing as a Tech Mode enhancement. Closed without re-filing — scouting workflow is now covered by TechSession; the screenshot-to-position matching use case did not surface as a real need during Phase 1. Can be re-opened if it becomes relevant to the Tech Mode UX.

---

### OPS-004 [resolved] BLOCK_PLACE / BLOCK_REMOVE — block modification inside stop-safety contract ✓
**Shipped:** 2.23.0 | **Filed:** (pre-existing) | **Area:** Set Director, Stage Manager

Two new event types bring block placement and removal inside the show's stop-safety contract.

**`BLOCK_PLACE`** — places a fully specified blockstate at an absolute world coordinate. Block type and all state properties in a single Minecraft blockstate string.
```yaml
type: BLOCK_PLACE
at: 10
world_specific: true
target: {x: 100, y: 64, z: 200}
block: "minecraft:blast_furnace[facing=north,lit=false]"
```

**`BLOCK_REMOVE`** — sets the target block to AIR.
```yaml
type: BLOCK_REMOVE
at: 20
world_specific: true
target: {x: 100, y: 64, z: 200}
```

**Stop-safety:** Before any modification, the original `BlockData` is recorded in `RunningShow.blockStateRestoreMap` using `putIfAbsent` semantics — the true pre-show state is preserved even if the same block is targeted multiple times. On `stopShow()` (natural end or `/show stop`), all recorded blocks are restored via `block.setBlockData(original, false)`.

**Implementation:** `WorldEvents.BlockPlaceEvent` + `BlockRemoveEvent` (model), `WorldEventExecutor.handleBlockPlace/Remove()` (execution), `RunningShow.recordBlockRestore()` / `getBlockStateRestoreMap()` (tracking), `ShowManager.stopShow()` restore loop. Wired in `EventParser` and `ExecutorRegistry`.

---

### OPS-008 [resolved] BLOCK_STATE — set block lit/active states via YAML ✓
**Shipped:** 2.23.0 | **Filed:** 2026-03-29 | **Area:** Sound Designer, Set Director

New event type for patching state properties on an existing block without changing its type (e.g., `lit=true` on a blast furnace, `powered=true` on a pressure plate).

```yaml
type: BLOCK_STATE
at: 5
world_specific: true
target: {x: 100, y: 64, z: 200}
state:
  lit: true
```

Multiple state keys can be patched in a single event. Implementation uses a blockstate string round-trip: decompose the current `getAsString()` result, merge in the overrides, rebuild the string, and call `Bukkit.createBlockData()`. Works generically across all block types without interface casting. Invalid merged states log a warning and skip the change.

**Stop-safety:** Same `recordBlockRestore()` / restore loop as OPS-004 — the original `BlockData` is captured before the patch is applied and restored on show end.

**Unblocked:** blast furnace `lit=true` at Scene A open (home base workshop glow + crackle sound). Campfire `lit=true` on arrival at expedition sites is a direct secondary use case — same event, same stop-safety.

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
**Shipped:** 2.13.0 | **Filed:** 2026-03-25 | **Area:** Castin