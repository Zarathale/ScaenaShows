---
document: OPS-027 Building Spec — Tech Rehearsal Mode Phase 1
status: ready-for-implementation
authored: 2026-04-01
supersedes: tech-rehearsal-architecture.md (v3)
---

# OPS-027 Building Spec — Tech Rehearsal Mode (Phase 1)

> Read this before touching any code. It is the output of a full architecture session
> and contains decisions that are **closed**. Do not re-open them without Alan.
>
> Open questions are in §8. Resolve those first, then build.

---

## 1. What Is Being Built

Tech Rehearsal Mode is a stateful, in-world rehearsal surface. The player enters tech
mode from in-game, a scene materializes, and they can toggle departments on and off,
reposition marks, and adjust show values — then write changes back to disk. No typing
required after the entry command.

Phase 1 scope: show-params integration (the Prompt Book). YAML cue navigation is Phase 2.

**Retires:** `/scaena scout` command family. TechSession replaces ScoutSession as the
primary in-world authoring surface. Mark capture workflow moves entirely into tech mode.

---

## 2. The Prompt Book — Core Architectural Decision

### What it is

`[show_id].prompt-book.yml` is the **single source of truth** for all committed
structural and content decisions about a show. It replaces `show-params.md` entirely.

Every department pushes their final decisions into it:
- Casting pushes entity choices
- Wardrobe pushes kit
- Set pushes block states and marks
- Lighting pushes time-of-day and sources
- Fireworks pushes patterns
- Sound pushes ambient cues
- Voice pushes committed script lines
- Stage Management pushes params and readiness state

Discussion and working history live in `direction/` and `departments/` folders.
The Prompt Book holds the **output** of those discussions — the committed state.

### What it replaces

`showcase.01.show-params.md` is retired. Its tables become structured YAML. Its prose
annotations become inline `notes:` fields. The YAML Readiness checklist moves to
`[show_id].status.md` (workflow state, wrong home in show-params anyway).

### Where it lives

```
src/main/resources/shows/[show_id]/[show_id].prompt-book.yml
```

Follows the existing `[show_id].[doctype]` naming convention. Deployed to
`plugins/ScaenaShows/shows/[show_id]/[show_id].prompt-book.yml` by the plugin on
first enable (via extended `saveDefaultResources` — see §6).

### Relationship to show YAML

- `[show_id].yml` = what happens and when (timing, cues, event sequence)
- `[show_id].prompt-book.yml` = what the show IS (entities, lines, marks, params)

The Prompt Book feeds YAML authoring. When Show Director writes the show YAML, they
read entity names, text strings, param values, and mark positions from the Prompt Book.
In Phase 2, TechSession also reads directly from show YAML; the Prompt Book remains
the source of truth for params and content.

### Write-back

On SAVE, the plugin writes to **one file only**: `[show_id].prompt-book.yml`. Pure YAML
key-path update. No Markdown parsing, no dual-write. This replaces the dual-write
design that was considered when show-params.md was still in play.

### Script integration

Script lines live in the Prompt Book — not externally. The actual text that gets pushed
to the show (dialogue, delivery method, speaker, timing) is committed here, just like
casting choices. Lines that are runtime-adjustable params (death_line, choice_prompt,
etc.) are defined in the `params:` section and referenced from the script section by
param name. Fixed dialogue lines are authored directly in `script.lines`.

---

## 3. Prompt Book Schema

Full annotated schema for showcase.01. Use this as the template for all future shows.

```yaml
# [show_id].prompt-book.yml
# The Prompt Book — authoritative committed state of this show.
# Departments push their final decisions here.
# Discussion lives in direction/ and departments/. Committed choices live here.
# Plugin reads at TechSession init.

show: showcase.01
title: "Preparing for Battle"
stage: brief         # brief | scripted | scouted | yaml-ready | production
world: overworld
duration_target: "6000–8400 ticks"
updated: "2026-04-01"

structure:
  format: rondo
  sequence: "A → B → A₁ → C → A₂ → D → A₃ → E → A₄ → F → A-Final"
  expedition_count: 5
  a_sections_total: 6
  armor_stand_fills: 5
  notes: "Armor stand fills in dramatic order: boots, leggings, chestplate, helmet, axe."

# ─────────────────────────────────────────────────────────────────────────────
# SCENES
# Ordered by scene_number. Decimal scheme: "00", "00.1", "01", "10.1", etc.
# scene_number is the sort key — Stage Management owns the numbering convention.
# ─────────────────────────────────────────────────────────────────────────────
scenes:

  - id: site_a
    scene_number: "00"
    label: "Home Base — The Workshop"
    arrival_mark: home_base       # mark name in scout_captures; player teleports here on LOAD
    status: scouted               # pending | scouted | yaml-ready
    biome: Sparse Jungle
    world: world

    departments:

      casting:
        - role: companion
          mark: companion_spawn
          entity_type: VILLAGER
          subtype: ARMORER              # profession for Villagers; variant for Cats, Horses, etc.
          display_name: "Companion"
          ai_locked: false
          notes: >
            Narrator for all expeditions. Must be fenced with blast furnace job
            site nearby. Open-sky position despite interior scene.
        - role: hero
          mark: vindicator_spawn
          entity_type: VINDICATOR
          display_name: "The Vindicator"
          ai_locked: true
          notes: >
            Behind-wall holding position. Must NOT be visible from home_base
            or companion_spawn. Iron door between here and workshop.
            Present the entire show — SPAWN_ENTITY fires at Scene A open,
            never despawns until show ends.

      wardrobe:
        # Applied to the entity at the named mark. Requires Casting active.
        - entity_mark: vindicator_spawn
          helmet: CHAINMAIL_HELMET
          chestplate: IRON_CHESTPLATE
          leggings: IRON_LEGGINGS
          boots: LEATHER_BOOTS
          boots_dye: "#3B2A1A"          # confirm dye support; fallback: undyed
          main_hand: IRON_AXE
          main_hand_enchant: "SHARPNESS:1"  # confirm enchant support; fallback: plain axe
          notes: >
            ENTITY_EQUIP mirrors each armor stand fill at same A-section return.
            Player sees the stand fill; Vindicator is equipped simultaneously behind the wall.

      set:
        - mark: blast_furnace
          block_type: BLAST_FURNACE
          block_state: "lit=true"
          notes: "Block coords (confirm in-game): x=198, y=79, z=304. Open-sky position."
        - mark: iron_door
          block_type: IRON_DOOR
          block_state: "open=false"
          notes: >
            Closes at show open. Opens at branch resolution — fight or walk away.
            Not at the reveal moment. SM owns stop-safety cleanup cue.

      lighting:
        time_of_day: 12500
        notes: "Twilight. Natural clock after show open — no TIME_OF_DAY events until A-Final."
        sources:
          - mark: blast_furnace
            level: 13
            quality: warm orange
            role: anchor light source
          - mark: lanterns
            status: TBD
            notes: "Set assesses after scouting."

      fireworks:
        present: false
        notes: "No fireworks. Play space is enclosed (ceiling 0–4 blocks)."

      script:
        lines:
          - id: companion.a.open.1
            speaker: companion
            delivery: CHAT
            text: "Five pieces. Everything I need is out there — I know where each one is."
            timing: "Show open"
          # A-section lines: TBD — require Voice revision with Vindicator-behind-wall staging.
          # Commit lines here after Voice session delivers them.

  - id: site_b
    scene_number: "01"
    label: "High Ground — The Helmet"
    arrival_mark: high_ground
    status: pending               # scouting not yet run

    departments:
      casting:
        - role: companion
          mark: armorer_high_ground
          entity_type: VILLAGER
          subtype: ARMORER
          display_name: "Companion"
          notes: "2–4 blocks from arrival. Face toward player arrival."

      fireworks:
        present: true
        pattern: find-firework
        timing: "Between Armorer line 2 and line 3. Simultaneous with item in hand."

      script:
        lines: []   # TBD — pending scouting and Voice session

  - id: site_c
    scene_number: "02"
    label: "The Forge — The Chestplate"
    arrival_mark: the_forge
    status: pending
    departments:
      casting:
        - role: companion
          mark: armorer_the_forge
          entity_type: VILLAGER
          subtype: ARMORER
          display_name: "Companion"
      set:
        - notes: "Lava as light source if adjacent — Set scouts proximity."
      fireworks:
        present: true
        pattern: find-firework
        notes: "Warmer, badlands sky."
      script:
        lines: []

  - id: site_d
    scene_number: "03"
    label: "The Long Road — The Leggings"
    arrival_mark: the_long_road
    status: pending
    departments:
      casting:
        - role: companion
          mark: armorer_long_road
          entity_type: VILLAGER
          subtype: ARMORER
          display_name: "Companion"
      set:
        - mark: campfire_long_road
          block_type: CAMPFIRE
          block_state: "lit=true"
          notes: "Regular campfire. Mob containment anchor (~14-block no-spawn radius)."
      lighting:
        sources:
          - mark: campfire_long_road
            level: 15
            quality: warm
            role: waystation atmosphere + mob containment
      fireworks:
        present: true
        pattern: find-firework
        notes: "Understated, road-quality."
      script:
        lines: []

  - id: site_e
    scene_number: "04"
    label: "The Swamp Floor — The Boots"
    arrival_mark: the_swamp_floor
    status: pending
    departments:
      casting:
        - role: companion
          mark: armorer_swamp_floor
          entity_type: VILLAGER
          subtype: ARMORER
          display_name: "Companion"
      set:
        - mark: campfire_swamp_floor
          block_type: CAMPFIRE             # soul vs. regular TBD — Steve decides after canopy report
          block_state: "lit=true"
          notes: "Soul campfire (L10) recommended. Confirm type after canopy density report."
      fireworks:
        present: true
        pattern: find-firework
        notes: "Tactile, swamp-register."
      script:
        lines: []

  - id: site_f
    scene_number: "05"
    label: "The Choice — The Weapon"
    arrival_mark: the_choice
    status: pending
    departments:
      casting:
        - role: companion
          mark: armorer_the_choice
          entity_type: VILLAGER
          subtype: ARMORER
          display_name: "Companion"
          notes: "Slower pacing here. Space should feel weighted."
      set:
        - mark: campfire_the_choice
          block_type: CAMPFIRE
          block_state: "lit=true"
          notes: "Mob containment + particle beat anchor for Effects. Center of cleared space."
      fireworks:
        present: false
        notes: >
          Intentional absence. Final expedition — absence carries maximum force.
          The silence IS the firework moment.
      script:
        lines:
          - id: companion.f.3
            speaker: companion
            delivery: silence
            text: ""
            timing: "F line 3 — silence. Locked 2026-03-29."
            notes: "The most important line in the show."

# ─────────────────────────────────────────────────────────────────────────────
# MECHANICS
# Behavioral rules that span scenes or cross departments.
# ─────────────────────────────────────────────────────────────────────────────
mechanics:

  iron_door:
    summary: >
      Closes at show open via REDSTONE. Opens at the start of whichever branch the player
      chooses at A-Final — not at the reveal moment. The reveal is the door, not a spawn.
    fight_branch: "Door opens → player steps through → Vindicator discovered (AI locked) → countdown → GO."
    walk_away_branch: "Door opens → Vindicator says LATER. → despawns."
    owner: Stage Management
    notes: "SM owns stop-safety cleanup cue."

  vindicator_staging:
    rule: "SPAWN_ENTITY fires at Scene A open — AI locked, no equipment, behind wall."
    visibility: "Must NOT be visible from home_base or companion_spawn at any time."
    equip: "ENTITY_EQUIP mirrors each armor stand fill at same A-section tick window."
    reveal: "The reveal is the door opening. Never a fresh spawn."
    despawn: "Despawns only when the show ends (fight death or walk-away branch)."

  armor_stand_fills:
    rule: "Fills in dramatic order, independent of expedition order."
    order:
      - fill: 1
        slot: boots
        a_section: A₁
        after_expedition: B
      - fill: 2
        slot: leggings
        a_section: A₂
        after_expedition: C
      - fill: 3
        slot: chestplate
        a_section: A₃
        after_expedition: D
      - fill: 4
        slot: helmet
        a_section: A₄
        after_expedition: E
      - fill: 5
        slot: axe_main_hand
        a_section: A-Final
        after_expedition: F

  expeditions:
    player_mode: SPECTATOR
    camera: "Drone spectate dolly-in (all 5 scenes). Site F pacing intentionally slower."
    notes: >
      Camera KB owns expedition transitions via PLAYER_SPECTATE.
      Stop-safety restores prior game mode. Plugin support confirmed — no ops-inbox needed.

  lighting_arc:
    show_start: 12500
    progression: natural_clock
    a_final_nudge:
      time_of_day: 22000
      trigger: "Hold beat between Armorer's last line and Vindicator reveal"
      notes: "Pre-dawn — guarantees sunrise during fight if player chooses fight branch."

# ─────────────────────────────────────────────────────────────────────────────
# PARAMS
# Show-level adjustable values. Exposed via Parameter tool during tech sessions.
# type: numeric → right-click to increment, shift-right-click to decrement
# type: text    → chat input prompt (type value, press Enter)
# locked: true  → display-only in tech panel; not editable without explicit unlock
# ─────────────────────────────────────────────────────────────────────────────
params:

  # Battle Sequence
  - name: foe_health_multiplier
    type: numeric
    value: 1.5
    step: 0.1
    min: 0.5
    max: 5.0
    locked: true
    notes: "36 HP (24 × 1.5). Locked 2026-03-31."

  - name: foe_speed_multiplier
    type: numeric
    value: 0.5
    step: 0.1
    min: 0.1
    max: 2.0
    locked: true
    notes: "Half speed — heavy and slow. Locked 2026-03-31."

  - name: foe_scale_multiplier
    type: numeric
    value: 1.5
    step: 0.1
    min: 0.5
    max: 3.0
    locked: true
    notes: "1.5× size. Effects: flag ceiling clearance at A-Final space."

  - name: countdown_duration_seconds
    type: numeric
    value: 5
    step: 1
    min: 3
    max: 10
    notes: "5-4-3-2-1 countdown before AI releases."

  - name: hold_before_choice_ticks
    type: numeric
    value: 120
    step: 20
    min: 20
    max: 400
    notes: "~6 seconds after Armorer's last line before PLAYER_CHOICE fires."

  - name: choice_timeout_ticks
    type: numeric
    value: 300
    step: 20
    min: 60
    max: 1200
    notes: "15 seconds. Yellow bossbar depletes over this duration."

  - name: walk_away_despawn_ticks
    type: numeric
    value: 60
    step: 5
    min: 5
    max: 200
    notes: "~3 seconds after exit line before Vindicator despawns."

  - name: death_line_to_coda_ticks
    type: numeric
    value: 20
    step: 5
    min: 5
    max: 100
    notes: "~1 second pause after death line before victory coda fires. Let the word land."

  - name: victory_cue_duration
    type: numeric
    value: 160
    step: 20
    min: 40
    max: 400
    notes: "8 seconds."

  - name: victory_levitation_amplifier
    type: numeric
    value: 0
    step: 1
    min: 0
    max: 3
    notes: "TBD — Effects: flag ceiling clearance at A-Final fight space."

  # Choice surface
  - name: choice_prompt
    type: text
    value: "What now?"
    notes: "PLAYER_CHOICE prompt text. ShowSprite register."

  - name: choice_default
    type: text
    value: "walk_away"
    notes: "Branch that fires on timeout. walk_away is the safer multiplayer default."

  # Script params — adjustable lines that also appear in script section by reference
  - name: walk_away_exit_line
    type: text
    value: "LATER."
    notes: "ALL CAPS. Deep red #CC2200. CHAT delivery. Vindicator voice."

  - name: death_line
    type: text
    value: "WORTHY."
    locked: true
    notes: "ALL CAPS. Deep red #CC2200. CHAT delivery. Fires on EntityDeathEvent. Locked 2026-03-31."

  - name: walk_away_sprite_close
    type: text
    value: "The stage is yours."
    locked: true
    notes: "ShowSprite closing line. Locked 2026-03-31."

# ─────────────────────────────────────────────────────────────────────────────
# READINESS
# Open items tracked here, not in a separate checklist doc.
# Move items from open → closed as they resolve.
# ─────────────────────────────────────────────────────────────────────────────
readiness:
  open:
    - "Armorer Villager main-hand targeting — ENTITY_EQUIP confirm"
    - "Scouting report: Sites B–F (Gate 3)"
    - "Site F particle beat — Effects confirms viability post-scouting"
    - "Victory cue fireworks pattern — Mira proposes at intake"
    - "Victory levitation amplifier — ceiling clearance at A-Final fight space"
    - "A-section script lines — Voice session to deliver after wall-staging confirmation"

  closed:
    - "OPS-026 shipped: SPAWN_ENTITY attributes + BOSS_HEALTH_BAR ✓"
    - "OPS-009 shipped: PLAYER_CHOICE ✓"
    - "Voice session complete 2026-03-29 — script v2 locked ✓"
    - "Fireworks finale locked 2026-03-29: grand finale burst ✓"
    - "Battle sequence multipliers locked 2026-03-31 ✓"
    - "Death line locked 2026-03-31: WORTHY. ✓"
    - "Walk away sprite close locked 2026-03-31 ✓"
    - "Player spectator mode confirmed — Camera KB ✓"
```

---

## 4. Plugin Architecture

### New package: `com.scaena.shows.tech`

**`PromptBook.java`** — data model. Record types for the full schema above.
Key records: `PromptBook`, `SceneSpec`, `DeptCasting`, `CastingEntry`, `DeptWardrobe`,
`WardrobeEntry`, `DeptSet`, `SetEntry`, `DeptLighting`, `DeptFireworks`, `DeptScript`,
`ScriptLine`, `ParamSpec`. Enums: `ParamType { NUMERIC, TEXT }`, `StageStatus`.

**`PromptBookLoader.java`** — reads `shows/[show_id]/[show_id].prompt-book.yml` from
the plugin data folder using SnakeYAML (existing pattern). Returns `PromptBook`.
Fails fast with clear log messages on missing required fields.

**`PromptBookWriter.java`** — writes updated param values and mark positions back to
`[show_id].prompt-book.yml` on TechSession SAVE. Updates specific keys by path;
does not rewrite the whole file. Preserves comments (use snakeyaml's block style).

**`TechSession.java`** — stateful core. Fields:

```java
TechSession {
  final String              showId
  final Player              player
  final PromptBook          book          // loaded at init
  String                    currentSceneId
  Set<String>               departmentMask   // active departments
  Map<UUID, EntityRecord>   activeEntities   // uuid → EntityRecord(markName, dept)
  Map<Location, BlockData>  blockSnapshots   // original → restore on dismiss
  Map<String, Object>       modifiedParams   // param.name → new value
  Map<String, Location>     modifiedMarks    // mark name → new position
  PlayerStateSnapshot       playerState      // gamemode, flight, inventory, held slot
  String                    focusedMark      // nullable; set during capture mode
  boolean                   captureMode
  boolean                   paramScrollMode  // true when player is in numeric param scroll
  String                    focusedParam     // nullable; the param currently being scrolled
  // Phase 2 stubs (declared, unused in Phase 1):
  int                       currentCueIndex
  boolean                   holdActive
}
```

**`TechManager.java`** — session lifecycle and the four core operations.

- `enterTech(Player, showId, sceneId?)` — load PromptBook, snapshot player state,
  give hotbar items slots 5–9, call LOAD, send panel, start actionbar task
- `LOAD(scene, departmentMask)` — for each active dept: materialize assets immediately.
  Record all entity UUIDs and original block states. Teleport player to arrival mark.
- `DISMISS(player)` — stop-safety: despawn all entities, restore block states, restore
  player state + inventory. If modifiedParams or modifiedMarks non-empty, send save prompt.
- `TOGGLE(player, department)` — off: despawn/restore that dept's assets; on: re-fire
  them. Wardrobe dependency: Casting off → Wardrobe despawns with it.
- `CAPTURE(player, markName, position)` — update modifiedMarks, teleport entity if
  present at that mark, confirm in actionbar.
- `SAVE(player)` — call PromptBookWriter. Confirm count of changes written.
- `onPlayerQuit(player)` — force DISMISS with discard (no save prompt on disconnect).

**`TechHotbarListener.java`** — `Listener` on `PlayerInteractEvent` and
`PlayerItemHeldEvent`. Detects:
- Right-click with slot 5 item → prev scene
- Right-click with slot 6 item → hold (toggle hold state)
- Right-click with slot 7 item → next scene
- Right-click with slot 8 item → CAPTURE(focusedMark, player.getLocation())
- Right-click with slot 9 item → open param panel / confirm numeric param
- Shift+right-click slot 9 → decrement focused numeric param

**`TechPanelBuilder.java`** — builds the clickable chat `Component`. Pure function:
takes `TechSession` state, returns a `Component`. Called after every state change.
Panel structure per spec: separator, header, departments row, marks section, exit button.
All actions are `ClickEvent.runCommand("/scaena tech ...")` internal routing commands.

**`TechActionbarTask.java`** — `BukkitRunnable` repeating every 5 ticks. Sends:
- Normal mode: `TECH · [show_id] · [scene_label]`
- Capture mode: `📍 CAPTURING: [mark_name] | (x.x, y.y, z.z) | right-click to capture`
- Post-action: brief confirmation, returns to normal after 2 seconds

**`TechSidebarDisplay.java`** — manages a per-player scoreboard sidebar. Lines:
```
TECH · showcase.01
──────────────────
Site A — Active
Entities: 2 / 2
Marks modified: 0
──────────────────
CAST   ✓
WARDROBE  ✓
LIGHTS  ✓
SET    —
```

### Modified files

**`ScaenaShowsPlugin.java`**
- Add `TechManager techManager` field
- Instantiate after `scoutManager` (TechManager needs PromptBook loading capability)
- Register `TechHotbarListener` as an event listener
- Wire `techManager` into `PlayerLifecycleListener`
- Extend `saveDefaultResources` to include `.md` files and any files in
  `shows/[show_id]/` subfolders (not just `.yml` flat files)

**`command/ScoutCommand.java`**
- Add `tech` case to top-level switch dispatch
- Add `handleTech(player, args)` — routes `/scaena tech [show_id] [scene?]` and
  `/scaena tech dismiss` to `TechManager`
- Add tab completion for `tech` verb: show IDs, then scene IDs for that show
- Add `techManager` constructor parameter

**`runtime/PlayerLifecycleListener.java`**
- Add `TechManager techManager` field and constructor parameter
- In `onPlayerQuit`: call `techManager.onPlayerQuit(event.getPlayer())`

---

## 5. Department Materialization in Phase 1

Each department's LOAD behavior, drawn from the Prompt Book scene data:

| Department | Phase 1 LOAD behavior |
|---|---|
| Casting | Spawn entity at mark (entity_type + subtype + display_name). Record UUID. |
| Wardrobe | Apply equipment to entity at entity_mark. Requires Casting active. |
| Set | Apply block_state to block at mark's coordinates (derived from scout_captures). Record original BlockData. |
| Lighting | Set TIME_OF_DAY to scene's time_of_day value. |
| Camera | Spawn camera proxy entity at drone mark (if capture exists). Record UUID. |
| Sound | One-shot ambient sound at arrival mark on load. (Skip if not authored.) |
| Effects | One-shot particle burst at arrival mark on load. (Skip if not authored.) |
| Fireworks | One-shot test burst if scene fireworks.present = true. |

**Wardrobe dependency:** If Casting is toggled off, Wardrobe assets despawn with it.
When Casting is re-enabled, entities respawn and Wardrobe re-applies automatically.

**No capture for a mark:** If a mark has no entry in the latest scout_captures file,
that asset is skipped. A visual indicator appears in the tech panel and actionbar
(`⚠ companion_spawn not captured`). LOAD does not abort — load what you can.

---

## 6. Data Flow

```
scout_captures/[show_id]/[date].yml   →  mark positions (x, y, z, yaw, pitch, world)
[show_id].prompt-book.yml            →  what to DO at each mark (entity types, block
                                         states, params, script lines)

TechSession INIT:
  1. Load PromptBook from prompt-book.yml
  2. Read mark positions from latest scout_captures file
  3. Snapshot player state
  4. Give hotbar items
  5. LOAD(first scene or named scene)

TechSession SAVE:
  1. Write modifiedMarks → update mark positions in prompt-book.yml (params section)
  2. Write modifiedParams → update param values in prompt-book.yml (params section)
  3. Note: scout_captures is NOT written to — mark position write-back goes to
     prompt-book.yml only. Scout captures remain the field-captured coordinate store.
```

Wait — clarification on mark write-back: the Prompt Book does not store mark coordinates
(those live in scout_captures). What the Prompt Book stores is mark *names* and *roles*.
Position updates from CAPTURE should write to a NEW capture file in scout_captures,
not to prompt-book.yml. The prompt-book write-back covers param value changes only.

**Revised write-back contract:**
- `modifiedParams` → write to `prompt-book.yml` params section
- `modifiedMarks` → write to a new dated `scout_captures/[show_id]/[date].yml` file
  (same format as existing capture files, merging with prior captures)

This keeps the two data sources clean: scout_captures = positions, prompt-book = everything else.

---

## 7. Resolved Design Decisions

All decisions below are **closed**. Do not reopen without Alan.

| # | Decision | Choice |
|---|----------|--------|
| Q1 | Department materialization source | Prompt Book YAML; Claude-maintained |
| Q2 | show-params.md write-back | Retired entirely — Prompt Book is the single source |
| Q3 | Numeric param adjustment mechanism | Right-click = increment, shift+right-click = decrement |
| Q4 | Text param input | Chat input: plugin sends prompt, intercepts next player chat message |
| Q5 | Scene numbering | Decimal string scheme ("00", "00.1", "01"); Stage Management owns convention |
| Q5b | Scene ordering | By `scene_number` field, decimal-aware string sort |
| — | Prompt Book location | `shows/[show_id]/[show_id].prompt-book.yml` in show folder |
| — | Write-back format | Single YAML file write (no Markdown) |
| — | Mark position write-back | New scout_captures file (not prompt-book) |
| — | show-params.md | Retired. Content migrated to prompt-book.yml. |
| — | Disconnect handling | Force DISMISS with discard — no save prompt |
| — | Player gamemode during session | Creative + flight enabled |

---

## 8. Open Questions — Resolve Before Building

**Q6 — Casting params in the Parameter tool**

entity_type and other per-entity values are in the Prompt Book's casting dept block.
Do these appear in the Parameter tool panel?

Option A: Yes — scene casting params appear in the param panel alongside show-level
params, labeled by role (e.g., "companion · entity_type"). These write back to the
casting block in prompt-book.yml. Changing entity_type despawns the current entity
and respawns one of the new type at the same mark.

Option B: Casting params are a separate sub-panel — clicking [CAST ✓] in the dept
row opens a secondary panel listing entities and their editable fields.

Recommendation: A. Less navigation, Phase 1 scope is narrow.

---

**Q7 — Department mask on session init**

Which departments start active when TechSession first loads?

Option A: All departments that have authored assets for the current scene (non-empty
dept block). Empty departments are grayed out rather than active-but-doing-nothing.

Option B: All departments by default regardless of what's authored.

Recommendation: A. Only show what's there.

---

**Q8 — No capture for arrival mark**

If the arrival_mark has no entry in scout_captures, what happens on LOAD?

Option A: No teleport — player stays put; actionbar says `⚠ [mark_name] not captured`.
Option B: LOAD aborts with an error message.

Recommendation: A. Load what you can, surface what's missing.

---

## 9. Implementation Plan — Ordered

Work in this order. Each step is independently testable.

**Step 1 — Data layer (no plugin logic)**
1. Create `showcase.01.prompt-book.yml` (migrate from show-params.md + script)
2. Create `PromptBook.java` data model records
3. Create `PromptBookLoader.java` — test: load showcase.01 prompt-book, log summary
4. Extend `saveDefaultResources` in plugin to handle show subfolder files

**Step 2 — TechSession skeleton**
5. Create `TechSession.java` with all fields
6. Create `TechManager.java` with `enterTech` + `DISMISS` (no LOAD yet)
7. Create `PlayerStateSnapshot` record
8. Wire into `ScaenaShowsPlugin` + `PlayerLifecycleListener`
9. Add `tech` verb routing to `ScoutCommand`
10. Test: `/scaena tech showcase.01` enters tech, `/scaena tech dismiss` restores state cleanly

**Step 3 — Scene materialization**
11. Create `TechHotbarListener.java` (hotbar items only — no events yet)
12. Implement `LOAD` in TechManager (spawn entities, apply block states, set time)
13. Implement `DISMISS` stop-safety (despawn, restore blocks, restore player)
14. Test: enter tech, scene materializes, dismiss cleans up completely

**Step 4 — Navigation and display**
15. Create `TechPanelBuilder.java` — clickable chat panel
16. Create `TechActionbarTask.java` — always-on actionbar
17. Create `TechSidebarDisplay.java` — scoreboard sidebar
18. Implement Prev/Next scene (slots 5/7) — calls LOAD(new_scene)
19. Test: navigate between scenes, panel refreshes, sidebar updates

**Step 5 — Department toggles**
20. Implement `TOGGLE` in TechManager
21. Wire [DEPT ✓/—] panel clicks to `/scaena tech toggle [dept]` command
22. Test: toggle casting off (entities despawn), back on (entities respawn + wardrobe re-applies)

**Step 6 — Mark capture**
23. Implement `CAPTURE` in TechManager
24. Wire [Focus Mark...] panel → secondary mark list → capture mode
25. Implement TechActionbarTask capture mode (live coordinates)
26. Wire slot 8 right-click → `CAPTURE(focusedMark, player.getLocation())`
27. Create `PromptBookWriter` for mark write-back to scout_captures
28. Test: focus a mark, walk to position, right-click, capture saves

**Step 7 — Parameter tool**
29. Implement numeric param panel (list params for current scene)
30. Wire right-click / shift+right-click slot 9 to increment/decrement
31. Implement text param chat input (intercept AsyncPlayerChatEvent)
32. Test: adjust a numeric param live, adjust a text param via chat

**Step 8 — Save and write-back**
33. Implement `PromptBookWriter` for param write-back to prompt-book.yml
34. Implement save prompt on DISMISS (if changes exist)
35. Wire [Save to prompt-book.yml] and [Discard] panel clicks
36. Test: change params, dismiss, save — verify prompt-book.yml updated correctly

**Step 9 — Version bump and cleanup**
37. Bump version to 2.21.0 (MINOR — new subsystem)
38. Update ops-inbox.md: mark OPS-027 references to show-params.md as resolved
39. Update CLAUDE.md: replace show-params.md references with prompt-book

---

## 10. Migration: showcase.01

The `showcase.01.show-params.md` file is retired as part of this implementation.

Migration steps (done **before** Step 1 code work):
1. Produce `shows/showcase.01/showcase.01.prompt-book.yml` using the schema in §3
2. Verify all values match show-params.md (no data loss)
3. Update `CLAUDE.md` — replace all `show-params.md` references with `prompt-book.yml`
4. Update `showcase.01.status.md` — add YAML Readiness items from show-params.md
5. Update any department briefs that reference show-params.md
6. Archive `showcase.01.show-params.md` → `_archive/show-params/showcase.01.show-params.md`
7. Update `_template/` show folder — add `[show_id].prompt-book.yml` template, remove
   `[show_id].show-params.md` from scaffold

---

## 11. Docs to Update

| File | Change |
|------|--------|
| `CLAUDE.md` | Replace show-params.md references with prompt-book.yml |
| `ROADMAP.md` | Update show stage description — prompt-book replaces show-params |
| `kb/production-team.md` | Note that Prompt Book is the committed-state artifact |
| `kb/departments/show-director/show-director.kb.md` | Update Show Director brief format |
| `_template/[show_id].show-params.md` | Retire; replace with `[show_id].prompt-book.yml` template |
| `src/main/resources/shows/showcase.01/showcase.01.show-params.md` | Archive → `_archive/show-params/` |
| `ops-inbox.md` | Update OPS-027 to reference prompt-book; add scene numbering item |

---

## 12. New Ops-Inbox Items

**OPS-028 [future-capability] Scene Numbering Convention — Stage Management owns**

Stage Management defines and documents the decimal scene numbering scheme used in
prompt-book.yml `scene_number` fields. The numbering determines scene sort order in
Tech Mode navigation and any future scene-indexed features.

Convention in use: decimal strings (`"00"`, `"00.1"`, `"01"`, `"10.1"`) sorted
lexicographically with decimal-aware comparison. Stage Management documents the
convention, the rules for inserting subscenes, and how renumbering works when scenes
are added or removed.

**Priority:** Medium — needed before prompt-book schema stabilizes across multiple shows.
