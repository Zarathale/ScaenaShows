---
department: Casting Director
owner: Casting Director
kb_version: 1.0
updated: 2026-03-25
---

# Casting Director — Technical Knowledgebase

> Technical reference for the Casting Director department. Documents what the ScaenaShows Java plugin
> can do for entity selection, AI control, and group management — and how to access those capabilities
> through YAML.
>
> Creative direction for this role lives in `docs/production-team.md §1. Casting Director`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| SPAWN_ENTITY | Bar (spawn → despawn) | Summons a named entity at an offset from the anchor |
| DESPAWN_ENTITY | Point | Removes a named entity or group from the world |
| ENTITY_AI | Point | Switches entity between puppet (frozen) and performer (native AI) state |
| CAPTURE_ENTITIES | Point | Creates a named group from existing world entities within a radius |
| RELEASE_ENTITIES | Point | Releases a captured group from show control (entities remain in world) |
| ENTER | Bar | Semantic shorthand: spawn at a wing mark, move to destination |
| EXIT | Bar | Semantic shorthand: move entity to a wing mark, optionally despawn |

---

## Capabilities & YAML Syntax

---

### SPAWN_ENTITY

Spawns a named entity at an XYZ offset from the spatial anchor. The name is required for any subsequent targeting event. Entity lives until DESPAWN_ENTITY fires or the show ends (if `despawn_on_end: true`).

```yaml
type: SPAWN_ENTITY
entity_type: VILLAGER        # any Minecraft EntityType (VILLAGER, WOLF, ALLAY, ARMOR_STAND, etc.)
offset: {x: 5, y: 0, z: 5}  # XYZ from spatial anchor
name: "Herald"               # required for later targeting; must be unique within the show
baby: false                  # true = baby variant
variant: PLAINS              # ⚠️ PARSED BUT NOT APPLIED — see Gaps below
profession: CLERIC           # ⚠️ PARSED BUT NOT APPLIED — see Gaps below
equipment:                   # optional; applies at spawn
  helmet: GOLDEN_HELMET
  main_hand: STICK
despawn_on_end: true         # always use true unless entity is an intentional world fixture
```

**Behavioral notes:**
- `entity_type` accepts any Paper `EntityType` name in ALLCAPS (e.g., `IRON_GOLEM`, `ARMOR_STAND`, `ALLAY`, `VEX`).
- The entity is spawned at the anchor's current XYZ + offset. For static shows the anchor doesn't move; for follow shows the anchor is wherever the lead player stands at invocation.
- `name:` is used for targeting in all downstream events (`entity:spawned:Herald`). Pick names that are descriptive of the entity's role, not their type.
- `despawn_on_end: true` is the default safe choice. Setting it to `false` permanently places the entity in the world — use only for intentional world fixtures, documented in the run sheet.

---

### DESPAWN_ENTITY

Removes a named spawned entity or captured entity group from the world at the specified tick.

```yaml
type: DESPAWN_ENTITY
target: entity:spawned:Herald    # or entity_group:chorus
particle_burst: true             # optional: visual pop on despawn (default false)
```

**Behavioral notes:**
- `particle_burst: true` fires a brief particle effect at the entity's location before removal — useful for a "vanish" effect. `false` is a clean silent removal.
- If the target entity was already despawned (e.g., by an earlier DESPAWN_ENTITY), this is a safe no-op.

---

### ENTITY_AI

Switches a named entity between puppet state (AI disabled — entity freezes in place) and performer state (AI enabled — entity behaves naturally).

```yaml
type: ENTITY_AI
target: entity:spawned:Herald   # or entity_group:chorus
enabled: false   # false = puppet (frozen, full show control); true = performer (native AI)
```

**Behavioral notes:**
- **Puppet state** (`enabled: false`): the entity holds its position completely. Use for tableau, for figures that must hold a mark, or for any entity where unpredictable movement would break the scene.
- **Performer state** (`enabled: true`): the entity's natural Minecraft behavior resumes. An Allay will seek items; a Wolf will follow if tamed; a Phantom will swoop. Use deliberately — performer state adds authenticity but not control.
- Spawned entities default to AI-enabled unless `ENTITY_AI enabled: false` is fired immediately after spawn. If you need a puppet from the moment of spawn, fire ENTITY_AI at the same tick or 1 tick after SPAWN_ENTITY.
- `entity_group:` targeting applies the AI state to all members of the group simultaneously.

---

### CAPTURE_ENTITIES

Sweeps the world for existing entities of a specified type within a radius and creates a named group for show targeting.

```yaml
type: CAPTURE_ENTITIES
entity_type: COW       # entity type to capture
radius: 15             # sweep radius in blocks from spatial anchor
max_count: 8           # maximum entities to capture (oldest by proximity order)
group_name: chorus     # name for the group; use as entity_group:chorus in targets
capture_mode: snapshot # snapshot: group fixed at this tick | live: re-sweeps on each target use
```

**Behavioral notes:**
- `snapshot` captures the group membership once and holds it. Use this for most cases — the group is stable for the rest of the show.
- `live` re-sweeps the radius each time the group is targeted. Use when entities may move into or out of range and you want the group to reflect whoever is present at the moment of each event. More expensive; use sparingly.
- If fewer entities than `max_count` exist in the radius, the group has fewer members — not an error. If zero entities are found, the group is empty; all events targeting it silently skip.
- Captured entities are not removed from the world. After `RELEASE_ENTITIES` (or show end), they revert to normal world behavior.
- Use `ENTITY_AI enabled: false` immediately after CAPTURE_ENTITIES to puppet the group.

---

### RELEASE_ENTITIES

Releases a captured entity group from show control. Entities remain in the world.

```yaml
type: RELEASE_ENTITIES
target: entity_group:chorus
restore_ai: true     # true (default): re-enables AI for all group members
                     # false: releases tracking only, does not touch AI state
```

**Behavioral notes:**
- After RELEASE_ENTITIES, the group name can no longer be used as a target in this show.
- `restore_ai: true` is the safe default — it ensures puppeted group members return to natural behavior when released.
- `restore_ai: false` is for cases where you want to release the group from show control but have already set their AI state explicitly and don't want it touched.

---

### ENTER

Semantic shorthand: spawns an entity at a wing mark and moves it to a destination. Cleaner to read than a SPAWN_ENTITY + CROSS_TO sequence.

```yaml
type: ENTER
entity_type: VILLAGER
name: Herald
from: mark:wing_left         # spawn location (typically a wing mark)
destination: mark:stage_left # where the entity walks to
duration_ticks: 30           # travel time
facing: mark:center          # optional: orientation on arrival
baby: false
```

---

### EXIT

Semantic shorthand: moves a named entity to a wing mark (off stage) and optionally despawns it on arrival.

```yaml
type: EXIT
target: entity:spawned:Herald
to: mark:wing_right
duration_ticks: 30
despawn_on_arrival: true   # true: entity despawns when it reaches the wing mark
```

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Casting Director needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `docs/departments/stage-manager.kb.md` → Active Gap Registry.

---

### Gap: `variant` and `profession` on SPAWN_ENTITY — parsed but not applied

**Status:** Open. Filed in `ops-inbox.md`.

The `variant:` and `profession:` fields are read by the YAML parser and exist in the SpawnEntityEvent model, but `EntityEventExecutor.handleSpawn()` never applies them to the spawned entity. Writing `variant: BLACK` or `profession: CLERIC` in a SPAWN_ENTITY block currently has no effect at runtime.

**Affected capabilities (currently non-functional):**
- Villager profession (FARMER, LIBRARIAN, CLERIC, ARMORER, BUTCHER, CARTOGRAPHER, etc.)
- Villager biome type (PLAINS, DESERT, JUNGLE, SAVANNA, SNOW, SWAMP, TAIGA)
- Cat coat pattern (TABBY, BLACK, RED, SIAMESE, BRITISH_SHORTHAIR, CALICO, PERSIAN, RAGDOLL, WHITE, JELLIE, ALL_BLACK)
- Horse color (WHITE, CREAMY, CHESTNUT, BROWN, BLACK, GRAY, DARK_BROWN)
- Sheep wool color (all 16 dye colors)
- Wolf variant (1.21+ multiple coat colors)

**Workaround:** Use equipment to differentiate entities visually when variant is critical to the scene. A Villager with a GOLDEN_HELMET and SPECTRAL_ARROW in main hand reads differently than one with a LEATHER_HELMET and HOE. This is a partial substitute — the skin palette is unchanged. For scenes where the specific mob skin matters, design around the gap: choose a mob type whose default appearance serves the role without variant control.

---

## Entity Targeting Reference

| Target syntax | What it addresses |
|---|---|
| `entity:spawned:Name` | A specific entity spawned by this show via SPAWN_ENTITY or ENTER |
| `entity_group:groupname` | All members of a group created by CAPTURE_ENTITIES |
| `entity:world:Name` | A named entity already present in the world (not show-spawned) |

`entity:world:Name` targets entities whose in-game custom name matches exactly. Use with caution — if the entity doesn't exist or the name doesn't match, the event silently skips.
