---
department: Set Director
owner: Michael C.
kb_version: 2.1
updated: 2026-03-26
notes: >
  v2.0: Role Summary, Tone Translation, Department Principles, Capability Status Summary.
  Java verification: PLAYER_TELEPORT (PlayerEventExecutor), REDSTONE Powerable.setPowered()
  (WorldEventExecutor), CROSS_TO/HOLD/FACE/ENTER/EXIT (StageEventExecutor). BLOCK_PLACE/BLOCK_REMOVE
  gap confirmed open. REDSTONE stop-safety gap confirmed — not inside cleanup contract.
  v2.1: Michael C. named head. P1 Scouting Process section. Environment Notes format defined.
  Stage registry cross-referenced. Folder migration complete.
---

# Set Director — Technical Knowledgebase

> Technical reference for the Set department. Documents what the ScaenaShows Java plugin
> can do for spatial structure, player teleportation, marks, sets, and block modification —
> and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §4. Set Director`.
>
> Stage Registry (all confirmed stages): `kb/departments/set/stage-registry.md`

---

## Michael C.

Michael C. approaches set design as an act of spatial argument: the space should do half
the work before the show starts. His first question on any brief is whether the environment
the Director is imagining actually exists — and if not, what exists that could serve the
same emotional function. He documents more than he needs during scouting, because gaps
discovered after Choreography has written crosses and Lighting has built an arc are
expensive to fix. The space is the container; everything else lives inside it.

Michael C. escalates to the Show Director when a spatial constraint will break another
department's planned work — an enclosed ceiling that makes TIME_OF_DAY invisible, a
location that won't support the planned levitation height, a space too small for the
intended performer crosses. These are not problems Michael C. resolves on his own. He
brings them with a proposed alternative.

---

## Role Summary

- **Spatial infrastructure.** Owns the mark grid (`marks:`), world-specific named locations (`sets:`), downstage orientation (`front:`), and player home capture — the entire coordinate vocabulary that other departments navigate within.
- **Set transitions.** Authors PLAYER_TELEPORT events for moving players between locations. Destination authority; Effects owns orientation (yaw/pitch on arrival). Coordinates at brief time.
- **Block modification discipline.** The only department that makes persistent world-state changes via COMMAND. Owns the full cleanup protocol: every block placed must be documented and every COMMAND modification must have a paired restore command — both at show end and in a manually-invocable cleanup cue.
- **REDSTONE state management.** Owns REDSTONE events for triggering in-world circuits. Not inside stop-safety — every REDSTONE on must have a paired REDSTONE off explicitly authored.
- **Environment documentation.** Files Environment Notes before other departments write their work: sky visibility, ceiling height, ambient light level, biome, sight lines. Lighting cannot arc without knowing the sky; Sound cannot score without knowing the enclosure; Effects cannot commit to an altitude without knowing the ceiling.

---

## P1 Scouting Process

P1 is Set's first production pass — and the gate for all other departments. No YAML can be
authored for any show until Set has scouted the locations, confirmed the coordinates, and
filed the Environment Notes. This is Michael C.'s first delivery.

### What Michael C. reads first

The show brief (`shows/[show_id]/brief.md`) and the Set department brief
(`shows/[show_id]/departments/set.md`). The Set department brief identifies:
- How many distinct locations the show needs
- What each location must support (player staging, performer crosses, levitation height, fireworks clearance)
- Any spatial constraints that carry cross-department implications

### What Zarathale does in-game

Zarathale executes all scouting and building. For each required location, Zarathale:
1. Finds or builds a candidate that meets the brief requirements
2. Stands at the intended player arrival point and records:
   - XYZ coordinates (F3 screen)
   - Yaw (facing direction, from F3)
   - World name (from F3 or `/locate` reference)
3. Notes the ceiling — is the sky open? How many blocks to the ceiling if enclosed?
4. Notes ambient light — what does this space look like at the relevant time of day?
5. Notes biome (visible in F3)
6. Walks the space — any obstruction, quirk, or sight line worth flagging

Zarathale reports findings back to Michael C. (in-game or in session notes). Michael C.
translates findings to stage registry entries and Environment Notes.

### What gets filed

**Stage Registry entry** — one entry per confirmed location in
`kb/departments/set/stage-registry.md`. This is the permanent record; once registered, a
stage is available to all future productions.

**Environment Notes** — one entry per scene/set in the show's Set department brief
(`shows/[show_id]/departments/set.md`). These are show-specific and are what other
departments actually read. Format defined below.

### When it's due

Environment Notes must be filed before the show brief's department briefing close. Other
departments cannot begin their passes without them — especially Lighting (needs sky status),
Sound (needs biome and enclosure), Effects (needs ceiling height for levitation planning),
and Fireworks (needs sky clearance and player altitude ceiling).

---

## Environment Notes Format

One entry per scene or set. Filed in `shows/[show_id]/departments/set.md` under Decisions,
before department briefing close.

```markdown
### Environment Notes — [Scene Name / Set Slug]
- **World:** [world name as Minecraft recognizes it]
- **Coordinates:** x: ___, y: ___, z: ___
- **Facing on arrival:** yaw: ___, pitch: ___
- **Sky access:** open sky / partially enclosed / fully enclosed / underground
- **Ceiling height:** open / N blocks to ceiling
- **Ambient light:** full sun / overworld day / dusk-dawn (~6000t) / night / cave (block light only) / nether glow / end void
- **Biome:** [biome name]
- **Weather visible:** yes / no
- **Key sight line:** [what the player sees looking in the arrival direction]
- **Special notes:** [redstone present, water, lava, mob spawns, quirks, build requirements]
```

**What each field serves:**

| Field | Who reads it | Why |
|-------|-------------|-----|
| Sky access + weather visible | Lighting, Fireworks | TIME_OF_DAY and WEATHER effects require open sky |
| Ceiling height | Effects | Levitation altitude target must fit inside the ceiling |
| Ambient light | Lighting | Block light governs brightness when sky is absent |
| Biome | Sound | Ambient audio baseline; outdoor vs. underground vs. nether |
| Key sight line | Camera, Show Director | First impression of the space from player arrival |

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

Marks are named XZ positions relative to the spatial anchor (the lead player's position at
show invocation). They have no Y coordinate — height must be hardcoded separately in
movement events.

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

**Marks are XZ only.** Vertical staging — placing entities or players at a specific height —
requires hardcoded Y values in PLAYER_TELEPORT, CROSS_TO, or SPAWN_ENTITY offset fields.
Document vertical positions in the show's Environment Notes.

---

### sets: — World-specific named locations

Sets are absolute world positions used for player teleportation between distinct playing
areas. Unlike marks, sets are world-specific and not portable.

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

**`return_on_end: true`** ensures players are returned to their pre-show `home` position
when the show ends naturally, stops, or the player disconnects. Use this on any set that
moves players away from where they started. Omitting it means players remain at the set
location after the show.

**Teleporting to a set:**
```yaml
type: PLAYER_TELEPORT
audience: participants
destination: set:main_stage
```

---

### PLAYER_TELEPORT

Moves players instantly to a set or to an offset from their current position.

```yaml
# Teleport to a named set:
type: PLAYER_TELEPORT
audience: private | group_1 | group_2 | participants | target
destination: set:main_stage

# Teleport to a relative offset from current position (portable):
type: PLAYER_TELEPORT
audience: participants
offset: {x: 0, y: 0, z: 20}
yaw: 180.0    # optional facing override — Effects authority
pitch: 0.0
```

**Behavioral notes:**
- `destination: set:Name` teleports to the set's absolute world coordinates and facing.
- `offset:` teleports relative to the player's current position. Portable — works regardless of where the show is invoked.
- `yaw:` and `pitch:` are Effects authority within this event. Set controls destination; Effects controls orientation. Coordinate at show design time.
- `audience` targets: `participants` moves all players; `group_1` / `group_2` moves only that group; `private` / `target` moves only the invoker.

**Using PLAYER_TELEPORT for vertical staging:**
When marks can't carry Y coordinates, use PLAYER_TELEPORT with an offset that includes a Y value:
```yaml
type: PLAYER_TELEPORT
audience: participants
offset: {x: 0, y: 15, z: 0}   # lift player 15 blocks up from current position
```

---

### `front:` — Stage orientation

Defines which compass direction is downstage. Without `front:`, stage_left and stage_right
have no meaning — mark names are just labels.

```yaml
# In show YAML:
front: south    # north | south | east | west | 0–359 (compass degrees)
```

**Default:** The primary player's initial facing direction at invocation. For portable shows,
relying on the default means the stage rotates to match wherever the player is looking. For
shows with a fixed venue, set `front:` explicitly to match the venue's architecture.

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
- REDSTONE state changes are **not covered by stop-safety**. If you toggle a redstone state on, write a corresponding REDSTONE off at show end, and document it in the run sheet.

---

### COMMAND — Block modification (escape hatch)

There is no native BLOCK_PLACE or BLOCK_REMOVE event type. All block modifications must go
through the COMMAND escape hatch.

```yaml
type: COMMAND
command: "/fill 100 64 200 105 64 205 minecraft:stone"
```

**⚠️ Block modifications via COMMAND are outside the stop-safety contract.** If the show is
interrupted before the cleanup COMMAND fires, the blocks remain permanently placed.

**Required protocol for any COMMAND block modification:**
1. At tick N: COMMAND fires to place/modify blocks.
2. At show end (natural): matching COMMAND fires to restore original block state.
3. Document in the run sheet: tick placed, world coordinates, original block type, replacement block type.
4. Consider a standalone cleanup cue (e.g., `fx.cleanup.[show_id]`) that can be invoked manually if the show is interrupted.

---

### Armor Stands and Display Entities as Set Pieces

The preferred alternative to block modifications for set dressing. Armor Stands use the
entity system — `despawn_on_end: true` keeps them inside the cleanup contract.

```yaml
type: SPAWN_ENTITY
entity_type: ARMOR_STAND
name: "Pedestal"
offset: {x: 0, y: 0, z: 3}
despawn_on_end: true
equipment:
  main_hand: DIAMOND
```

An Armor Stand holding a Diamond at center stage reads as a relic on a pedestal — achievable
without block modification and fully safe for interrupted rehearsals.

---

## Spatial Hierarchy Reference

```
World
└── Sets (absolute XYZ — world-specific named locations)
    └── Stage (the playing area within a set)
        └── Marks (named XZ offsets relative to anchor — portable)
            └── Wings (off-stage entry/exit zones beyond playing area)
```

`home` — each participant's captured location at show invocation. A runtime value, not in
YAML. Always available as `destination: home` in movement events.

---

## Tone Translation

How the Set Director interprets the Show Director's tone language.

**"Tender"**
Set reads tender as close and contained. Small mark spread — 4–6 blocks between marks
rather than the full 9-position grid. No dramatic set transitions: the audience stays in
one place through the scene. Set dressing kept sparse — at most one entity-based prop at
center. The space should ask the player to stay, not look outward. Ceiling height low enough
to feel intimate; open sky works when it reads as shared rather than vast.

**"Overwhelming / earned"**
A set-reveal call. Begin the scene in a constrained or unmarked position — no expansive
crosses, no introduced width. The earned moment is a set transition or a sudden PLAYER_TELEPORT
that delivers the player into a space they couldn't see coming. Alternatively: the marks are
laid out wider than the player expects, and the first cross across the full stage reveals
how much room there is. The space is withheld, then given. Coordinate the reveal timing with
the Show Director — the spatial arrival should land at the same tick as the lighting and sound
arrivals.

**"Strange / uncanny"**
Spatial wrongness. An underground set where the show begins at Y < 20. An outdoor set at an
altitude that makes the horizon wrong. Marks laid asymmetrically — upstage_left much farther
from center than upstage_right. A set that uses `front: north` when the player naturally faces
south, inverting left and right. REDSTONE firing with no visible circuit context. The space
should make the player feel slightly displaced before they understand why.

**"Delight / surprise"**
The space doing something unexpected and charming. A sudden PLAYER_TELEPORT mid-show that
moves the audience sideways without warning. A mark at an offset the player didn't know was
there. A set transition that moves players to a dramatically different biome for one scene
and returns them. Set dressing that appears suddenly — an Armor Stand that wasn't there a
moment ago. The spatial surprise should land with a smile, not a flinch: the world is playing
along, not threatening.

**"Joy / abundant"**
Open, high-ceiling space. Wide mark spread. Full sky visible. Room to breathe in every
direction. If the show has been building toward this, the set for the joy section should feel
expansive compared to what came before — even if it's the same location, a TIME_OF_DAY change
to full noon in an open space changes the spatial register. The space should feel like a gift
arriving.

**"Wonder"**
Threshold environments. A set at the water's surface, just where the air begins. A mark at
the edge of two biomes. A set at the underside of the cloud layer (Y ~160 in vanilla). The
space between underground and sky — a show that begins below the surface and arrives there at
the wonder moment. The spatial register for wonder is the edge: the player should feel like
they are at the boundary of something.

**Signaling back to the Director:** When a tone phrase is ambiguous for Set, the clarifying
question is: *"Does this scene want the player to feel contained, placed, or at threshold?"*
That single answer resolves most spatial tonal ambiguity — tender and uncanny can both feel
tight, but tender holds the player gently while uncanny makes the space feel wrong.

---

## Department Principles

**What Set is ultimately for:** Set is the stage itself — the operating environment that
determines whether everyone else's work lands. A Lighting arc designed for open sky has no
effect in an enclosed underground set. A Sound bed scored for outdoor ambience reads
incorrectly underground. Set is not decoration: it is the container. File the Environment
Notes before other departments begin.

**The space should do half the work.** If Michael C. has chosen the right location, the
player's first impression carries the show's opening atmosphere before a single event fires.
Choosing a space that fights the show's intent is a problem that no amount of YAML can fix
later.

**What Set decides independently:**
- Mark layout and naming for each show (grid spread, custom marks beyond the 9-position standard)
- Set count and the spatial arc of set transitions through the show
- `front:` orientation — or the conscious choice to defer to player facing
- Entity-based set dressing: Armor Stands, Display Entities (inside cleanup contract via `despawn_on_end: true`)
- REDSTONE state management and cleanup pairing
- Environment Notes documentation for the show space

**What requires Show Director sign-off:**
- Block modifications via COMMAND — permanent-world risk in any interrupted rehearsal; Director must know before any COMMAND block modification is authored
- Set transitions that cross into different worlds during a show — multi-world navigation is production-level complexity
- Any set design that places players in an environment they cannot exit without show intervention (enclosed chamber, underwater, high altitude with no safe landing) — the Director must be aware the player is committed to the space

**Cross-department coordination:**

*With Choreography:* Mark names are spatial vocabulary shared by both departments.
Choreography authors CROSS_TO destinations using mark references; Set defines what those
marks mean and where they are. Coordinate mark layout before Choreography writes movement
sequences — a mark repositioned after movement is authored breaks all the crosses. File the
mark grid in the show brief before the Choreography department begins.

*With Effects:* PLAYER_TELEPORT is jointly operated: Set controls destination (`set:Name`
or `offset:`), Effects controls orientation (`yaw:` / `pitch:` on arrival). When authoring
a PLAYER_TELEPORT, agree at brief time whether this is primarily a spatial delivery (Set
driving) or a perceptual placement (Effects driving) — both departments should know which
one owns the event's intent. Ceiling height from Environment Notes is Effects' planning
constraint for levitation altitude.

*With Lighting:* Sky visibility is Set's site report to Lighting — the most important single
fact the Lighting Designer needs. Open sky = TIME_OF_DAY fully visible. Enclosed or
underground = TIME_OF_DAY has no effect; block light level governs ambient brightness instead.
File the show's sky access status in the Environment Notes before Lighting begins arc design.

*With Sound:* Biome and enclosure determine the ambient audio baseline. Outdoor open space
has different ambient than underground cave, ocean surface, or forest. Sound must know the
set environment to score against the existing ambient bed. File biome and enclosure type in
the Environment Notes.

*With Fireworks:* Sky clearance and player altitude ceiling are Fireworks' planning inputs.
Mira cannot place firework y_offsets without knowing how high the ceiling is and where the
player will be positioned. File ceiling height and sky access status before Fireworks
begins authoring.

*With Stage Manager:* Every world-state change Set makes that falls outside the cleanup
contract must be documented in the stop-safety checklist. COMMAND block modifications and
REDSTONE state changes both require run sheet documentation and paired cleanup events.
Stage Manager audits stop-safety at brief close — Set delivers Environment Notes and the
full modification registry at that point.

**Handling capability gaps:** The two active gaps (BLOCK_PLACE/BLOCK_REMOVE and REDSTONE
stop-safety) both require manual compliance. Do not assume the plugin covers cleanup for
block modifications or redstone state — it does not.

**Escalation discipline:** Set resolves spatial infrastructure decisions independently.
Escalates to Director when: (1) a block modification is required that carries permanent-world
risk; (2) a set transition will confine the player in a way they cannot self-exit; (3)
Environment Notes reveal a constraint that will break another department's planned work —
this must be resolved before the show arc is finalized, not after.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| marks: — portable XZ grid | ✅ Verified | Defined in show YAML; anchor-relative XZ offsets; used via `mark:Name` in CROSS_TO, FACE, ENTER, EXIT; XZ only (no Y) |
| sets: — world-specific named locations | ✅ Verified | Absolute XYZ + yaw/pitch; `return_on_end: true` for player return on show end/stop; teleport via PLAYER_TELEPORT |
| front: — downstage orientation | ✅ Verified | Compass direction or numeric degrees; defaults to player facing at invocation |
| home — player start capture | ✅ Verified | Runtime value captured at invocation; available as `destination: home` in CROSS_TO, RETURN_HOME, PLAYER_TELEPORT; not a YAML field |
| PLAYER_TELEPORT — set:Name destination | ✅ Verified | `PlayerEventExecutor.handleTeleport()`; resolves set by name; constructs Location from world/xyz/yaw/pitch |
| PLAYER_TELEPORT — offset: teleport | ✅ Verified | Offset from player's current position; portable; no set registration required |
| REDSTONE — Powerable block toggle | ✅ Verified | `WorldEventExecutor.handleRedstone()` → `Powerable.setPowered()`; absolute target XYZ |
| REDSTONE — stop-safety | ⚠️ Gapped | Not inside cleanup contract; every `state: on` must have an explicit `state: off` authored at show end; filed in ops-inbox.md |
| COMMAND — block modification (escape hatch) | ✅ Verified | Functional for any server command (`/fill`, `/setblock`, etc.); no cleanup contract coverage |
| BLOCK_PLACE / BLOCK_REMOVE (native events) | ⚠️ Gapped | Not implemented; all block modifications require COMMAND + manual cleanup protocol; filed in ops-inbox.md |
| SPAWN_ENTITY (Armor Stand as set piece) | ✅ Verified | `despawn_on_end: true` brings entity-based set dressing inside cleanup contract; preferred over block modification |
| Mark Y-axis coordinates | 📋 Aspirational | Marks are XZ only — vertical staging requires hardcoded Y values in PLAYER_TELEPORT/CROSS_TO |
| Per-player set isolation | 📋 Aspirational | All targeted players go to the same destination — per-player-distinct environments not available |
