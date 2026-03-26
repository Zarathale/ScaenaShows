---
department: Set Director
owner: Set Director
kb_version: 1.0
updated: 2026-03-25
---

# Set Director — Technical Knowledgebase

> Technical reference for the Set department. Documents what the ScaenaShows Java plugin
> can do for spatial structure, player teleportation, marks, sets, and block modification —
> and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `docs/production-team.md §4. Set Director`.

---

## Owned Event Types / YAML Fields

| Event / Field | Where | What it does |
|---------------|-------|--------------|
| `marks:` | Show YAML | Named XZ positions relative to the anchor — portable spatial grid |
| `sets:` | Show YAML | Named world-specific locations for player teleportation |
| `front:` | Show YAML | Compass direction that defines downstage |
| PLAYER_TELEPORT | Point | Moves players to a set or offset; sets facing on arrival |
| REDSTONE | Point | Toggles a redstone component at absolute world coordinates |
| COMMAND | Point | Escape hatch — used for block modification until native events exist |

---

## Capabilities & YAML Syntax

---

### marks: — The spatial grid

Marks are named XZ positions relative to the spatial anchor (the lead player's position at show invocation). They have no Y coordinate — height must be hardcoded separately in movement events.

Marks are defined in the show YAML, not in cue files.

```yaml
# In show YAML:
marks:
  center:        {x:  0, z:  0}
  stage_left:    {x: -6, z:  0}
  stage_right:   {x:  6, z:  0}
  upstage:       {x:  0, z: -8}
  downstage:     {x:  0, z:  6}
  wing_left:     {x:-15, z:  0}
  wing_right:    {x: 15, z:  0}
  upstage_left:  {x: -6, z: -8}
  upstage_right: {x:  6, z: -8}
```

**The 9-position grid (standard layout):**
```
UL (upstage_left)  | UC (upstage)   | UR (upstage_right)
SL (stage_left)    | CC (center)    | SR (stage_right)
DL (down_left)     | DC (downstage) | DR (down_right)
```
Wings (`wing_left`, `wing_right`, `wing_up`, `wing_down`) go beyond the playing area boundary.

**Using marks in events:**
- `destination: mark:center` in CROSS_TO / RETURN_HOME
- `look_at: mark:upstage` in FACE
- `from: mark:wing_left` in ENTER
- `to: mark:wing_right` in EXIT

**Marks are XZ only.** Vertical staging — placing entities or players at a specific height — requires hardcoded Y values in PLAYER_TELEPORT, CROSS_TO, or SPAWN_ENTITY offset fields. Document vertical positions separately in the show's run sheet Environment Notes.

---

### sets: — World-specific named locations

Sets are absolute world positions used for player teleportation between distinct playing areas. Unlike marks, sets are world-specific and not portable.

```yaml
# In show YAML:
sets:
  main_stage:
    world: world           # Minecraft world name
    x: 142
    y: 68
    z: -304
    yaw: 180.0             # facing direction on arrival
    pitch: 0.0
    return_on_end: true    # ⚠️ ALWAYS use true for any set that moves players from their start
```

**`return_on_end: true`** ensures players are returned to their pre-show `home` position when the show ends naturally, stops, or the player disconnects. Use this on any set that moves players away from where they started. Omitting it means players remain at the set location after the show.

**Teleporting to a set:**
```yaml
type: PLAYER_TELEPORT
audience: participants
destination: set:main_stage
```

---

### PLAYER_TELEPORT

Moves players instantly to a set or to an offset from their current position. Also sets facing on arrival.

```yaml
# Teleport to a named set:
type: PLAYER_TELEPORT
audience: private | group_1 | group_2 | participants | target
destination: set:main_stage

# Teleport to a relative offset from current position (portable):
type: PLAYER_TELEPORT
audience: participants
offset: {x: 0, y: 0, z: 20}
yaw: 180.0    # optional facing override
pitch: 0.0
```

**Behavioral notes:**
- `destination: set:Name` teleports to the set's absolute world coordinates and facing.
- `offset:` teleports relative to the player's current position. Portable — works regardless of where the show is invoked.
- `yaw:` and `pitch:` are the Camera Director's tools within this event. The Set Director uses PLAYER_TELEPORT for location; Camera Director uses it for orientation. Coordinate these at show design time.
- `audience` targets: `participants` moves all players in the show; `group_1` / `group_2` moves only that group; `private` / `target` moves only the invoker.

**Using PLAYER_TELEPORT for vertical staging:**
When marks can't carry Y coordinates, use PLAYER_TELEPORT with an offset that includes a Y value:
```yaml
type: PLAYER_TELEPORT
audience: participants
offset: {x: 0, y: 15, z: 0}   # lift player 15 blocks up from current position
```

---

### `front:` — Stage orientation

Defines which compass direction is downstage. Without `front:`, stage_left and stage_right have no meaning — mark names are just labels.

```yaml
# In show YAML:
front: south    # north | south | east | west | 0–359 (compass degrees)
```

**Default:** The primary player's initial facing direction at invocation. For portable shows, relying on the default means the stage rotates to match wherever the player is looking. For shows with a fixed venue, set `front:` explicitly to match the venue's architecture.

---

### REDSTONE

Toggles a redstone component at absolute world coordinates. Always world-specific.

```yaml
type: REDSTONE
world_specific: true
target: {x: 100, y: 64, z: 200}
state: on    # on | off
```

**Behavioral notes:**
- Fires a redstone signal at the specified block location. Use to trigger or deactivate redstone circuits in the show environment.
- World-specific — not portable. The target coordinates must match the actual world location.
- REDSTONE state changes are **not covered by stop-safety** (see Stage Manager KB). If you toggle a redstone state on, write a corresponding REDSTONE off at show end, and document it in the run sheet.

---

### COMMAND — Block modification (escape hatch)

There is no native BLOCK_PLACE or BLOCK_REMOVE event type. All block modifications must go through the COMMAND escape hatch.

```yaml
type: COMMAND
command: "/fill 100 64 200 105 64 205 minecraft:stone"
```

**⚠️ Block modifications via COMMAND are outside the stop-safety contract.** If the show is interrupted before the cleanup COMMAND fires, the blocks remain permanently placed. See the Stage Manager KB for the full cleanup protocol.

**Required protocol for any COMMAND block modification:**
1. At tick N: COMMAND fires to place/modify blocks.
2. At show end (natural): matching COMMAND fires to restore original block state.
3. Document in the run sheet: tick placed, world coordinates, original block type, replacement block type.
4. Consider a standalone cleanup cue (e.g., `fx.cleanup.[show_id]`) that can be invoked manually if the show is interrupted.

---

### Armor Stands and Display Entities as Set Pieces

The preferred alternative to block modifications for set dressing. Armor Stands use the entity system — `despawn_on_end: true` keeps them inside the cleanup contract.

```yaml
type: SPAWN_ENTITY
entity_type: ARMOR_STAND
name: "Pedestal"
offset: {x: 0, y: 0, z: 3}
despawn_on_end: true
equipment:
  main_hand: DIAMOND
```

An Armor Stand holding a Diamond at center stage reads as a relic on a pedestal — achievable without block modification and fully safe for interrupted rehearsals.

---

## Spatial Hierarchy Reference

```
World
└── Sets (absolute XYZ — world-specific named locations)
    └── Stage (the playing area within a set)
        └── Marks (named XZ offsets relative to anchor — portable)
            └── Wings (off-stage entry/exit zones beyond playing area)
```

`home` — each participant's captured location at show invocation. A runtime value, not in YAML. Always available as `destination: home` in movement events.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Set department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `docs/departments/stage-manager.kb.md` → Active Gap Registry.

---

### Gap: No BLOCK_PLACE / BLOCK_REMOVE event type

**Status:** Open. Filed in `ops-inbox.md`.

Block modifications currently require the COMMAND escape hatch. COMMAND-placed blocks are not tracked by the show and are not restored on show interruption.

**Impact:** Block-based set dressing carries permanent-world risk in any show that might be interrupted. Block modification cannot be safely used in rehearsal without extra manual cleanup planning.

**Workaround:** Use Armor Stands and entity-based set pieces wherever possible. For unavoidable block changes, follow the COMMAND cleanup protocol above.

**Proposed fix (when resolved):** `BLOCK_PLACE` and `BLOCK_REMOVE` event types that record changes in `RunningShow` and restore them via `applyStopSafety`.

---

### Limitation: Marks carry no Y coordinate

Y-axis staging cannot be expressed in the mark system. Any vertical positioning requires hardcoded Y values in PLAYER_TELEPORT, CROSS_TO, or SPAWN_ENTITY offset fields.

**Best practice:** Document vertical positions in the show's run sheet Environment Notes section. When authoring a show with aerial staging, list the key altitude levels explicitly (e.g., "ground = Y 68, mid-air = Y 80, peak = Y 95").
