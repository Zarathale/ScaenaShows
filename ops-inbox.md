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

### OPS-005 ~~[java-gap]~~ → **RESOLVED in 2.26.0** — see Resolved section below
---

### OPS-006 ~~[java-gap]~~ → **RESOLVED in 2.26.0** — see Resolved section below
---

---

---

---

---

---

### OPS-007 ~~[java-gap]~~ → **RESOLVED in 2.26.0** — see Resolved section below
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

**⚑ Prerequisite: Authoring Surface Architecture Decision** — Must be resolved before Phase 2 Java begins.

Two working design documents exist: `kb/system/timeline-editor-ux.md` (Director Mode model) and `kb/system/ux-review-2026-04-02.md` (the full UX audit that produced it). Together they describe not just a different hotbar — a different session architecture for the entire authoring surface:

| Dimension | Current spec (this entry) | `timeline-editor-ux.md` model |
|---|---|---|
| Session type | Extends `TechSession` | New Director Mode (separate session) |
| Entry command | `/scaena tech <showId>` | `/scaena <showId> timeline` |
| Hotbar layout | Prev / Hold / Go / Mark Capture / Param (slots 5–9) | 7 cue slots / Library Wand / Cue Sheet (slots 1–9) |
| Display surfaces | Sidebar + actionbar (Phase 1 pattern) | Sidebar + actionbar + boss bar |
| Show Dashboard | Not included | Phase 1 entry point (`/scaena <showId>`) |
| Cue library | Not included | First-class `CueLibraryBrowser` |

The `timeline-editor-ux.md` model is more developed and more aligned with the product framing (directing tool, not YAML editor). It is not yet confirmed. **Do not begin Phase 2 Java until this decision is made.**

Treat both documents as inputs to the decision, not the decision itself.

*See also: OPS-033 — the Phase 1 display noise issues are entangled with this decision. If Phase 2 adopts the Director Mode model, TechSidebarDisplay and TechActionbarTask get redesigned anyway. OPS-033's audit findings are direct input here.*

**Priority:** Medium — Phase 1 must be stable in production before Phase 2 Java begins. Authoring surface architecture decision is the immediate pre-work.

---

### OPS-030 [future-capability] Scaffold Generator — `/scaena scaffold [show_id]`

**Area:** Stage Management
**Filed:** 2026-04-02
**Depends on:** OPS-027 (Prompt Book, shipped v2.21.0)

#### What it is

The scaffold generator is a deterministic projection of the Prompt Book into a minimal valid show YAML. It is not creative. It is a bridge: it gives the director a runnable timeline with scene structure intact and cue slots empty, so staging can begin in-world without manually authoring skeleton YAML first.

**Input:** `[show_id].prompt-book.yml` — scenes, scene timing, and scene labels.
**Output:** `[show_id].yml` — a valid show YAML with one `CUE` stub per scene at the correct tick offset, labeled, and marked with a `# SCAFFOLD` comment so the author knows which stubs are placeholders.

If `[show_id].yml` already exists and contains non-scaffold content, the command refuses and prints a warning rather than overwriting. Once the timeline has real cues, the scaffold is no longer safe to regenerate.

#### Why it matters

Without the scaffold, the gap between "Prompt Book complete" and "first in-world preview" requires manual YAML authoring. The scaffold closes that gap automatically. It is the first step in the Layer 1 → Layer 3 transition described in the ROADMAP product model.

**Interim:** Claude can generate the scaffold manually from the Prompt Book. The plugin command is a quality-of-life improvement — not a blocker.

#### Schema note

No new event types needed. Scaffold output uses existing `CUE` references. The generated YAML is valid and plugin-loadable immediately.

**Priority:** Low — Claude handles this in the interim. Worth implementing once OPS-029 is stable, as part of the full Show Composition Surface.

---

### OPS-032 ~~[future-capability]~~ → **RESOLVED in 2.27.0** — see Resolved section below

---

### OPS-033 [java-gap] Tech Session display cleanup — noise audit and rework

**Area:** Stage Management (TechSidebarDisplay, action bar, scoreboard output)
**Filed:** 2026-04-04
**Priority:** Medium — functional but visually cluttered; defer until after Gate 3

**Problem observed (screenshots 2026-04-04):**
The Tech Session in-game display is generating several output layers that feel like noise rather than signal:

1. **Red scoreboard sidebar numbers** — A vertical column of red numbers (13 → 1) runs down the right edge of the screen. Source and intent unclear — may be a scoreboard sidebar being registered by the plugin unintentionally, or a side effect of a subsystem that hasn't been audited since TechMode shipped.

2. **Persistent lower-center action bar** — A bar reading `TECH · showcase.01 | Home Base — The Workshop` is refreshed continuously at the bottom-center of the screen. It overlaps the chat input box when chat is open. Its "you are here" context is already covered by the top-right panel; the persistent bar appears redundant.

3. **Overall density** — With the open-items checklist (top-left), department readiness grid + cast panel (top-right), action buttons (bottom-left), and the two noisy outputs above, the screen is crowded. No single element is wrong, but the combination is more control-panel than stage monitor.

**What is confirmed working and should be preserved:**
- Top-right panel: scene number, home base label, cast list with captured indicators
- Department readiness grid (CAST / WARDROBE / SET / LIGHTS / FX / SCRIPT with ✓ / · indicators)
- Top-left open-items checklist
- Bottom-left action buttons ([Enter Setup →], [Quick-play])
- Setup view: marks list with captured/missing/modified status indicators

**Suggested investigation (not yet decided):**
- Audit what is registering the red right-side scoreboard. Is it intentional? If so, what does it represent?
- Determine if the lower-center action bar serves a purpose not covered by the top-right panel. If not, suppress or remove.
- Consider whether the department readiness grid belongs in the persistent view or only in the Setup view.

**Dependency: OPS-029** — This ticket's redesign scope is entangled with OPS-029's pending authoring surface architecture decision. If Phase 2 adopts the Director Mode model (`kb/system/timeline-editor-ux.md`), TechSidebarDisplay and TechActionbarTask get redesigned as part of Phase 2 anyway. Cleaning up Phase 1 display surfaces that are about to be replaced would be wasted motion.

This ticket's scope is therefore split:

**Part A — Audit (independent, can proceed):** Identify what is registering the red right-side scoreboard and whether it is intentional. Determine whether the lower-center action bar serves a purpose not already covered by the top-right panel. These findings are useful input to OPS-029's architecture decision regardless of which model wins.

**Part B — Redesign (blocked on OPS-029):** Decisions about what to suppress, remove, or reorganize should wait until the authoring surface architecture decision is made.

---

### OPS-034 [java-gap] Player-anchored LIGHTNING executor — resolve target position at fire time

**Area:** Execution engine (`VisualEventExecutor.handleLightning()`)
**Filed:** 2026-04-05
**Priority:** Low — not blocking Phase 2 (scene-origin anchoring ships first); needed before player-anchored LIGHTNING presets can be used in production

**Problem:**
The current spatial anchor model locks the anchor at show invocation time (`Static mode: Anchor locked at invocation time`). LIGHTNING events currently resolve their offset from that single invocation-time anchor. This means a LIGHTNING strike cannot target a position relative to where the player is standing *when the event fires* — only relative to where they were when `/show play` was called.

**Desired capability:**
LIGHTNING events (and presets) should support a dual-anchor model:

| Anchor type | Behavior |
|---|---|
| `scene_origin` | Current behavior — offset from scene origin mark, locked at invocation time |
| `player` | New — offset resolved from the player's actual position at event-fire time |

**Use case:**
A `lightning.player.surprise_close` preset — `{anchor: player, x: 1, y: 0, z: 1}` — fires a cosmetic strike just in front of the player regardless of their current position. This is a repeatable, named pattern with legitimate show value.

**Design note:**
`anchor: player` requires the executor to call `player.getLocation()` at the moment the event fires (not at `RunningShow` init). This is a targeted change to `VisualEventExecutor.handleLightning()` only — no changes to `ShowScheduler`, `RunningShow`, or the spatial anchor model for other event types. The dual-anchor flag should be carried in the `LightningEvent` model.

**YAML field (proposed):**
```yaml
type: LIGHTNING
anchor: player   # scene_origin | player — default: scene_origin
offset:
  x: 1
  y: 0
  z: 1
```

**Preset field format (proposed):**
```yaml
id: lighting.lightning.player.surprise_close
anchor: player
offset: {x: 1, y: 0, z: 1}
```

**Dependency:** Phase 2 preset library file structure (OPS-029 §14 open item) — `lighting-configs.yml` is where these presets will live.

---

#### Part A Audit Findings — 2026-04-04

**Red scoreboard numbers (13→1)**

Source: `TechSidebarDisplay` — fully intentional, not a rogue system.

`TechSidebarDisplay.show()` assigns descending integer scores (N, N-1, … 1) to each sidebar line to control their vertical order. Minecraft's scoreboard sidebar always renders score values in the right column — in red — as standard behavior. There is no secondary scoreboard registered; the numbers are the line-ordering mechanism for the existing sidebar, visible as a rendering artifact of the Bukkit scoreboard API.

The line count (and thus the max score) varies with the scene's casting entries. For showcase.01's scenes with several cast members, the total reaches ~13 lines.

Fix path (Part B, when unblocked): Paper 1.20.4+ exposes `Objective#numberFormat(BlankFormat.blankFormat())` which suppresses the score column entirely. One call after `registerNewObjective` makes the numbers disappear without changing any line content or ordering logic.

**Persistent lower-center action bar**

Source: `TechActionbarTask` — a `BukkitRunnable` running every 5 ticks, started in `TechManager.enterTech()`, cancelled in `forceDismiss()`. Lifecycle is clean; it does not leak.

The task operates in four modes (priority order):
1. **Confirm flash** — brief success/error message; clears automatically
2. **Capture mode** — live `📍 [mark] | x, y, z | Use slot 8 to capture`
3. **Param scroll mode** — `⚙ [param] = [value]  [controls]`
4. **Normal mode** — `TECH · [showId] · [sceneLabel]  [✎ if dirty]`

Modes 1–3 are essential: they surface transient, high-priority state that has no other home. The coordinate display in capture mode is genuinely not redundant — the sidebar doesn't show live coordinates.

Normal mode (mode 4) is the redundancy concern. It shows show ID and scene label — both already visible in the sidebar. It also overlaps the chat input box when chat is open, which is a known Minecraft limitation (action bar renders at the same vertical position as the chat input).

**Assessment summary**

| Issue | Source | Intentional? | Redundant? | Resolution |
|---|---|---|---|---|
| Red sidebar numbers | `TechSidebarDisplay` score column | Yes — ordering mechanism | N/A — rendering artifact | ✅ Fixed in 2.28.0 — `NumberFormat.blank()` suppresses score column |
| Persistent action bar (capture/param/confirm modes) | `TechActionbarTask` modes 1–3 | Yes | No | ✅ Kept as-is — essential, not redundant |
| Persistent action bar (normal mode) | `TechActionbarTask` mode 4 | Yes | Yes — duplicates sidebar | ✅ Fixed in 2.28.0 — suppressed; actionbar now quiet in normal mode, shows `✎ unsaved changes` only when dirty |

**Part A complete — shipped in 2.28.0.** Part B (broader display redesign) remains blocked on OPS-029 authoring surface architecture decision.

---

## Resolved

---

### OPS-005 [resolved] ROTATE — smooth yaw rotation event ✓
**Resolved:** 2026-04-03 | **Filed:** 2026-03-25 | **Area:** Effects Director, Choreography
**Version:** 2.26.0

Implementation was already complete in the codebase; ticket not previously closed.

**What shipped:**
- `StageEvents.RotateEvent` — data class with `target`, `yaw` (absolute), `delta` (relative, signed), `durationTicks`; `isDelta()` helper; mutual exclusivity enforced (`delta` wins if both present)
- `StageEventExecutor.handleRotate()` — resolves all entities in target; instant snap if `durationTicks ≤ 0`; smooth pan via `BukkitRunnable` (1-tick interval) with linear interpolation across `durationTicks` steps; shorter-arc normalisation for absolute `yaw:` targets; signed `delta:` respected as-written
- `RunningShow.activeRotateTasks` + `addRotateTask()` + `cancelRotateTasks()` — task set registered for stop-safety cleanup
- `ShowManager.stopShow()` — calls `running.cancelRotateTasks()` before block/frame restore loops
- `EventType.ROTATE`, `EventParser` entry, `ExecutorRegistry` routing — all wired

**Contract:** `yaw:` (absolute) always takes the shorter arc via normalisation to [−180, +180]. `delta:` (relative) applies the signed rotation as-written — authors control direction explicitly. `duration_ticks: 0` or omitted = instant snap, identical to `FACE`. No state restoration on stop — yaw is left wherever the pan ends.

---

### OPS-006 [resolved] `capture_mode: live` for CAPTURE_ENTITIES ✓
**Resolved:** 2026-04-03 | **Filed:** 2026-03-25 | **Area:** Casting Director
**Version:** 2.26.0

Implementation was already complete in the codebase; ticket not previously closed.

**What shipped:**
- `RunningShow.LiveGroupSpec` record — stores `entityType`, `radius`, `maxCount`, `captureAnchor` instead of a UUID list
- `RunningShow.liveEntityGroups` map — parallel to `entityGroups`; populated by `setLiveEntityGroup()`
- `RunningShow.resolveEntityGroup()` — checks `liveEntityGroups` first; if found, performs a fresh `getNearbyEntities()` sweep at the frozen capture-time anchor on every call; falls back to snapshot UUID list otherwise
- `RunningShow.releaseEntityGroup()` — clears both maps (covers live and snapshot modes)
- `EntityEventExecutor.handleCapture()` — branches on `"live".equalsIgnoreCase(captureMode)`: registers `LiveGroupSpec` via `setLiveEntityGroup()`, skips the UUID sweep entirely
- `resolveEntity()` and `resolveEntities()` in EntityEventExecutor — both call `show.resolveEntityGroup()` for `entity_group:` targets, transparently getting live or snapshot resolution

**Contract:** `capture_mode: snapshot` (default) — UUID list frozen at capture time, unchanged. `capture_mode: live` — no UUID list; fresh spatial sweep at the frozen anchor on every use, respecting `max_count`.

---

### OPS-007 [resolved] SET_ITEM_FRAME — native item frame content event ✓
**Resolved:** 2026-04-03 | **Filed:** 2026-03-28 | **Area:** Set Director, Stage Manager
**Version:** 2.26.0

Implementation was already complete in the codebase; ticket not previously closed.

**What shipped:**
- `WorldEvents.SetItemFrameEvent` — data class with `target`, `item`, `visible`, `fixed`, `rotation` (all optional except target)
- `WorldEventExecutor.handleSetItemFrame()` — resolves frame via `entity:world` or `entity:spawned`; applies item + optional state fields; snapshots originals for stop-safety
- `RunningShow.ItemFrameSnapshot` record + `itemFrameRestoreMap` — per-UUID snapshots taken before first modification (`putIfAbsent` = original state always wins)
- `ShowManager.stopShow()` restore loop — iterates `getItemFrameRestoreMap()`, restores item, visible, fixed, rotation on all world-targeted frames
- `ExecutorRegistry` routing: `SET_ITEM_FRAME → WorldEventExecutor`
- `EventType.SET_ITEM_FRAME` + `EventParser` entry — fully wired

**Contract:** `entity:world` targets fully stop-safe. `entity:spawned` targets despawned at show end, no restore needed. All optional fields use null = no-change semantics. `item: minecraft:air` clears the frame (`frame.setItem(null)`).

---

### OPS-032 [resolved] MarkVisualizationTask — in-world marks visible in tech mode ✓
**Resolved:** 2026-04-03 | **Filed:** 2026-04-02 | **Area:** Stage Management, Set Director
**Version:** 2.27.0

**Changes shipped:**
- Extended `TechManager.spawnMarkMarkers()` — now spawns two categories of markers: captured marks at their known positions (existing behavior, unchanged), and uncaptured scene marks as red `?` placeholders near the player's position, spread 1.5 blocks apart in X so they don't stack
- New `TechManager.allMarkNamesInScene(SceneSpec)` — collects every mark name referenced by the scene across arrival, casting entries, and set entries; used to identify the uncaptured gap
- New `TechManager.spawnUncapturedMarkMarker(TechSession, String, Location)` — spawns a red TextDisplay placeholder (`? markName`, dark red background) that registers via `session.addMarkMarker()`, so it's automatically replaced by a real marker when the mark is captured
- Teardown unchanged — `clearMarkMarkers()` in `tearDownScene()` handles both captured and uncaptured markers correctly via the existing `markMarkers` map

**Scope note:** The spec called for a standalone `MarkVisualizationTask` class. The actual implementation landed as extensions to the existing `spawnMarkMarkers()` / `spawnMarkMarkerForName()` pattern in `TechManager` — cleaner given that the state (markMarkers map) and lifecycle (loadScene / tearDownScene) were already fully wired. No new class needed.

**Not included:** `/scaena tech markers` toggle command — deferred; spatial feedback is on by default which covers the core need.

---

### OPS-031 [resolved] Show Status Dashboard — `/scaena <showId>` ✓
**Resolved:** 2026-04-03 | **Filed:** 2026-04-02 | **Area:** Stage Management
**Version:** 2.26.0

**Changes shipped:**
- New `ShowDashboardBuilder.java` — stateless panel builder parallel to `TechPanelBuilder`; renders scenes, marks, open readiness items, timeline cue count, and action buttons from the prompt-book
- `TechManager.sendDashboard(Player, showId)` — loads prompt-book, reads latest scout captures, pulls timeline cue count from ShowRegistry, delegates to builder
- `TechManager.setShowRegistry(ShowRegistry)` — optional wiring for cue count; called from plugin on enable
- `ScoutCommand`: bare show ID (`/scaena showcase.01`) routes to dashboard; tab completion at top level now includes show IDs alongside verbs

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