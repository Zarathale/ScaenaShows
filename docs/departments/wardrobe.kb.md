---
department: Wardrobe & Properties Director
owner: Wardrobe & Properties Director
kb_version: 1.0
updated: 2026-03-25
---

# Wardrobe & Properties Director — Technical Knowledgebase

> Technical reference for the Wardrobe & Properties department. Documents what the ScaenaShows Java plugin
> can do for equipment, appearance, variants, and the invisible body technique — and how to access
> those capabilities through YAML.
>
> Creative direction for this role lives in `docs/production-team.md §2. Wardrobe & Properties Director`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| SPAWN_ENTITY (`equipment:`, `variant:`, `profession:`) | Bar | Sets appearance at spawn time |
| ENTITY_EQUIP | Point | Changes equipment on a live entity mid-show |
| ENTITY_INVISIBLE | Bar | Makes an entity's body invisible while keeping held items/armor visible |

Wardrobe also has joint authority with **Casting Director** over `variant:` and `profession:` on SPAWN_ENTITY.

---

## Capabilities & YAML Syntax

---

### Equipment at Spawn (SPAWN_ENTITY `equipment:` block)

All equipment slots can be set at the moment an entity is spawned. Equipment fields are nested inside the `equipment:` key in SPAWN_ENTITY.

```yaml
type: SPAWN_ENTITY
entity_type: VILLAGER
name: "Herald"
offset: {x: 0, y: 0, z: 3}
despawn_on_end: true
equipment:
  helmet: GOLDEN_HELMET        # any helmet material, or: CARVED_PUMPKIN, player head item
  chestplate: IRON_CHESTPLATE
  leggings: IRON_LEGGINGS
  boots: IRON_BOOTS
  main_hand: STICK             # any item: SWORD, TORCH, BOOK, HOE, BANNER, DEAD_BUSH, etc.
  off_hand: TORCH              # torch in off-hand casts light without occupying main hand
```

**Slot reference:**

| Slot | YAML key | Accepts |
|------|----------|---------|
| Helmet | `helmet` | Any armor helmet material; also CARVED_PUMPKIN, mob head items |
| Chestplate | `chestplate` | Armor chestplate materials only |
| Leggings | `leggings` | Armor leggings materials only |
| Boots | `boots` | Armor boots materials only |
| Main hand | `main_hand` | Any item: weapons, tools, torches, books, banners, food, etc. |
| Off hand | `off_hand` | Any item: torch for light, shield for blocking silhouette, etc. |

**Item name format:** Use Minecraft material names in ALLCAPS (e.g., `DIAMOND_SWORD`, `TORCH`, `OAK_SIGN`, `DEAD_BUSH`). These map directly to Bukkit `Material` enum values.

**Any slot can be omitted.** Only specified slots are set; unspecified slots remain empty.

---

### ENTITY_EQUIP — Mid-show wardrobe change

Changes one or more equipment slots on a live, named entity. A wardrobe change mid-scene is a character transformation moment — plan it as one.

```yaml
type: ENTITY_EQUIP
target: entity:spawned:Herald   # or entity_group:chorus
helmet: GOLDEN_HELMET           # set a slot
chestplate: GOLDEN_CHESTPLATE
leggings: ""                    # empty string = remove item from slot
boots: ""
main_hand: STICK
off_hand: ""
```

**Behavioral notes:**
- Empty string `""` explicitly clears a slot. Omitting a field leaves that slot unchanged.
- All slot changes apply simultaneously at the specified tick — there is no stagger between slots.
- Works on both show-spawned entities (`entity:spawned:`) and captured entity groups (`entity_group:`).
- Applying ENTITY_EQUIP to a captured group changes equipment on all group members simultaneously.

---

### ENTITY_INVISIBLE — The invisible body technique

Applies an invisibility effect to the entity's body. The body disappears; held items and worn armor remain fully visible. This is implemented as a shorthand for the INVISIBILITY potion effect with `hide_particles: true`.

```yaml
type: ENTITY_INVISIBLE
target: entity:spawned:Herald   # or entity_group:chorus
duration_ticks: 200             # how long the invisibility lasts
```

**What remains visible after applying ENTITY_INVISIBLE:**
- Main hand item
- Off-hand item
- All armor pieces (helmet, chestplate, leggings, boots)

**What disappears:**
- The entity's body, limbs, head, and face

**Use cases:**
- Floating sword or torch — spawn an entity, equip a weapon or torch, apply ENTITY_INVISIBLE → a weapon or light source that moves through space on its own
- Disembodied armor — a suit of armor standing still with no body inside → the absent knight, the memorial
- A banner carried by air
- A book or map floating at reading height

**Behavioral notes:**
- The invisibility is duration-based. After `duration_ticks` elapses, the body reappears. To maintain invisibility for the full show, set `duration_ticks` generously (e.g., 9999).
- The entity still has collision and can be targeted by show events while invisible.
- ENTITY_INVISIBLE does not affect Armor Stands — they have no body in the traditional sense. Apply ENTITY_EQUIP to an Armor Stand and leave ENTITY_INVISIBLE off; the stand itself is the display surface.

---

### Mob Variants and Professions

> ⚠️ **CURRENTLY NON-FUNCTIONAL — Gap filed in `ops-inbox.md`**

The `variant:` and `profession:` fields on SPAWN_ENTITY are parsed by the YAML layer but silently ignored at runtime. Writing them has no effect until the Java gap is resolved.

Once the gap is resolved, these fields will work as follows:

```yaml
type: SPAWN_ENTITY
entity_type: VILLAGER
variant: SNOW          # biome skin (PLAINS | DESERT | JUNGLE | SAVANNA | SNOW | SWAMP | TAIGA)
profession: CLERIC     # (FARMER | LIBRARIAN | CLERIC | ARMORER | BUTCHER | CARTOGRAPHER |
                       #  FLETCHER | TOOLSMITH | WEAPONSMITH | NITWIT | NONE)
name: "Priest"
offset: {x: 0, y: 0, z: 3}
despawn_on_end: true
```

```yaml
type: SPAWN_ENTITY
entity_type: CAT
variant: SIAMESE   # (TABBY | BLACK | RED | SIAMESE | BRITISH_SHORTHAIR | CALICO |
                   #  PERSIAN | RAGDOLL | WHITE | JELLIE | ALL_BLACK)
name: "WatchingCat"
offset: {x: -2, y: 0, z: 0}
despawn_on_end: true
```

```yaml
type: SPAWN_ENTITY
entity_type: SHEEP
variant: BLACK     # any DyeColor (WHITE | ORANGE | MAGENTA | LIGHT_BLUE | YELLOW | LIME |
                   #  PINK | GRAY | LIGHT_GRAY | CYAN | PURPLE | BLUE | BROWN | GREEN | RED | BLACK)
name: "BlackSheep"
offset: {x: 3, y: 0, z: 0}
despawn_on_end: true
```

**Current workaround while gap is open:** Use the `equipment:` block to differentiate entities visually. A Villager with a carved pumpkin helmet and a hoe in main hand reads as farmer even without the FARMER profession skin. A Villager with spectacles (golden helmet with a Book in main hand) reads as scholar. Equipment is the only currently reliable visual differentiation tool.

---

## Armor Stand Notes

Armor Stands accept all equipment slots and behave as static display surfaces. They are not living entities — ENTITY_INVISIBLE does not apply. Spawning a bare Armor Stand and equipping it creates a "costume on a wire" at that position.

```yaml
type: SPAWN_ENTITY
entity_type: ARMOR_STAND
name: "Memorial"
offset: {x: 0, y: 0, z: 2}
despawn_on_end: true
equipment:
  helmet: IRON_HELMET
  chestplate: IRON_CHESTPLATE
  leggings: IRON_LEGGINGS
  boots: IRON_BOOTS
```

**Pose control:** Armor Stand pose (angling limbs) requires a COMMAND event and is outside the show's cleanup contract. Use sparingly and document any command-placed pose changes in the run sheet with a corresponding cleanup COMMAND. This is a known gap — no first-class YAML pose event exists.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Wardrobe department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `docs/departments/stage-manager.kb.md` → Active Gap Registry.

| Gap | Status | Workaround |
|-----|--------|-----------|
| `variant` and `profession` on SPAWN_ENTITY — silently ignored | Open (ops-inbox.md) | Use equipment to differentiate visually |
| Armor Stand pose — requires COMMAND | No filed gap (low priority) | Document cleanup; use sparingly |
| No ENTITY_EQUIP for players | Not applicable — players manage their own inventory | Use COMMAND escape hatch for inventory modification; not covered by stop-safety |
