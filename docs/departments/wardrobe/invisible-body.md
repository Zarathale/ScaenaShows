---
department: Wardrobe & Properties Director
created: 2026-03-25
notes: Technical and creative guide to the invisible-body technique.
---

# Wardrobe — The Invisible Body Technique

One of the most powerful and versatile tools available to Wardrobe. A technique that makes an entity's body invisible while keeping held items and armor fully visible.

---

## What It Does

Apply ENTITY_INVISIBLE (an invisibility potion effect with hidden particles) to any LivingEntity. The results:

- **Body disappears:** Head, limbs, torso, all body parts vanish
- **Equipment remains visible:** All armor pieces stay visible — helmet, chestplate, leggings, boots
- **Held items remain visible:** Both main-hand and off-hand items float in space exactly where the entity is

**The effect:** An invisible entity with visible equipment creates profound visual and emotional impact. A suit of armor standing alone. A torch moving through darkness with no bearer. A book floating at eye height. A sword hanging in air.

---

## YAML Syntax

```yaml
type: ENTITY_INVISIBLE
target: entity:spawned:Herald         # Named entity or entity_group:name
duration_ticks: 200                   # How long the invisibility lasts
```

**Parameters:**
- `target`: The entity or group to make invisible. Use `entity:spawned:[name]` for a show-spawned entity or `entity_group:[name]` for a captured entity group.
- `duration_ticks`: How many ticks the invisibility lasts. After this duration, the body reappears.

**Duration guidance:**
- `200` ticks = 10 seconds — for brief moments
- `1200` ticks = 60 seconds — for longer scenes
- `9999` ticks (or higher) — effectively permanent for a show that runs 5–10 minutes

**For full-show invisibility:** Set `duration_ticks` to 9999 or higher. The invisibility will outlast the show and won't expire mid-performance.

---

## Setup Pattern: Invisible Body Workflow

To create an invisible-bodied figure effectively:

1. **Spawn the entity** with meaningful equipment:
   ```yaml
   type: SPAWN_ENTITY
   entity_type: ZOMBIE
   name: "FloatingSword"
   offset: {x: 0, y: 1, z: 3}
   despawn_on_end: true
   equipment:
     main_hand: IRON_SWORD
     helmet: ""
   ```

2. **Immediately apply invisibility** (at tick 0 or shortly after spawn):
   ```yaml
   type: ENTITY_INVISIBLE
   target: entity:spawned:FloatingSword
   duration_ticks: 9999
   ```

3. **Position or move the invisible-bodied entity** if needed (via CROSS_TO, ENTER, etc.):
   ```yaml
   type: CROSS_TO
   target: entity:spawned:FloatingSword
   mark: stage_left
   duration_ticks: 100
   ```

The entity is now an invisible body with a visible sword, moving through space. No body, just the weapon.

---

## Use Cases

### 1. Floating Weapons (Swords, Axes, Spears)

**What it creates:** A weapon moving or hovering in space with no wielder. Deeply unsettling and powerful.

**Emotional registers:**
- Threat floating in darkness (dread, danger, the invisible enemy)
- Memory of a battle or violence (grief, loss)
- Mystical weapon in motion (wonder, magic, otherworldliness)
- Precision instrument moving alone (technical spectacle, craft)

**Example YAML fragment:**
```yaml
# At spawn:
type: SPAWN_ENTITY
entity_type: SKELETON
name: "FloatingSword"
equipment:
  main_hand: DIAMOND_SWORD

# Immediately after:
type: ENTITY_INVISIBLE
target: entity:spawned:FloatingSword
duration_ticks: 9999

# Then move it:
type: CROSS_TO
target: entity:spawned:FloatingSword
mark: center
duration_ticks: 60
```

### 2. Disembodied Armor (Full Suits or Pieces)

**What it creates:** A suit of armor standing in space with no body inside. The medieval nightmare or the memorial.

**Emotional registers:**
- Absence and loss (who was inside this armor?)
- Formal witness (a statue, a sentinel)
- Majesty without person (divine or cosmic authority)
- The costume on a wire (pure aesthetic object)

**Example:** Spawn an Armor Stand (or Zombie) equipped with full iron armor, apply invisibility. If it's an Armor Stand, it stands still — perfect for a memorial. If it's a mob, it will move — the armor follows movement paths, haunting.

```yaml
type: SPAWN_ENTITY
entity_type: ARMOR_STAND
name: "MemorialArmor"
offset: {x: 0, y: 0, z: 0}
despawn_on_end: true
equipment:
  helmet: IRON_HELMET
  chestplate: IRON_CHESTPLATE
  leggings: IRON_LEGGINGS
  boots: IRON_BOOTS
```

Armor Stands don't need invisibility applied — they have no meaningful body in the first place. Just equip and position them.

### 3. Floating Light Sources (Torches, Lanterns)

**What it creates:** A light source moving through space with no bearer. A ghost light, a guide, a search-light.

**Emotional registers:**
- Hope or guidance (a light in darkness leading the way)
- Searching or seeking (the light moving to find something)
- Memory or spirit (a torch carried by the dead or the absent)
- Wandering witness (the light is a watcher)

**Example:**
```yaml
type: SPAWN_ENTITY
entity_type: ZOMBIE
name: "WanderingLight"
equipment:
  off_hand: TORCH
  helmet: ""
  chestplate: ""
  leggings: ""
  boots: ""

type: ENTITY_INVISIBLE
target: entity:spawned:WanderingLight
duration_ticks: 9999

type: CROSS_TO
target: entity:spawned:WanderingLight
mark: mark_a
duration_ticks: 200
facing: north
```

The torch moves across the stage, illuminating as it goes, with no visible bearer.

### 4. Floating Objects (Books, Banners, Food, Maps)

**What it creates:** A single object floating in space. A relic, an artifact, a symbol made tangible.

**Emotional registers:**
- Sacred object or relic (a book floating at altar height)
- Trophy or treasure (a banner or crown held aloft)
- Memory made physical (a dead bush floating as sorrow)
- Offering or gift (flowers or apples held at eye level)

**Example (floating book as knowledge or memory):**
```yaml
type: SPAWN_ENTITY
entity_type: ARMOR_STAND
name: "FloatingBook"
offset: {x: 0, y: 1.5, z: 0}
equipment:
  main_hand: BOOK
```

Position the book at eye height, and it becomes a visual anchor — something to contemplate.

### 5. Ghostly Figures (Armor + Invisibility, Facing/Watching)

**What it creates:** A ghostly presence suggested by floating equipment. A spirit, a phantom, an absence with presence.

**Emotional registers:**
- The ghost or the dead (presence without body)
- Possession or haunting (armor floating as if worn by spirit)
- Memory or echo (the figure is no longer here, but equipment remains)
- Witnessing without presence (armor stands and watches but has no eyes)

**Example:**
```yaml
type: SPAWN_ENTITY
entity_type: ZOMBIE
name: "Ghost"
equipment:
  helmet: IRON_HELMET
  chestplate: IRON_CHESTPLATE
  leggings: IRON_LEGGINGS
  boots: IRON_BOOTS

type: ENTITY_INVISIBLE
target: entity:spawned:Ghost
duration_ticks: 9999

# Optional: make it face the player
type: FACE
target: entity:spawned:Ghost
facing: player
```

The empty armor hovers and turns to face the player. Profoundly eerie.

---

## Behavioral Notes

### Invisibility Duration

- **Duration expires:** After `duration_ticks`, the body reappears. To maintain invisibility for a full show, set duration high (9999+).
- **Reapply invisibility:** If a scene is very long and you want to ensure invisibility doesn't expire, reapply ENTITY_INVISIBLE at a safe tick value (e.g., reapply at tick 5000 with duration 9999).
- **No re-application needed for normal shows:** Most shows run 3–10 minutes. A single ENTITY_INVISIBLE event with duration 9999 will outlast any show.

### Collision and Interaction

- **The entity still has collision.** An invisible entity still occupies space and can be targeted by events, hit by projectiles, or collided with by the player.
- **The entity can still be targeted.** Use `entity:spawned:[name]` or `entity_group:` to issue further events to invisible entities — movement, equipment changes, damage, etc.
- **Despawn still applies.** If the entity has `despawn_on_end: true`, it will despawn when the show ends, invisibility or not.

### Armor Stands vs. Mobs

- **Armor Stands:** Have no body in the traditional sense. ENTITY_INVISIBLE is not needed for Armor Stands — equip them and they're purely display objects. AI is disabled by default.
- **Living mobs (Zombies, Skeletons, Villagers, etc.):** Have bodies and AI behaviors. ENTITY_INVISIBLE hides the body; the mob's AI is still active (they might try to move, attack, wander, etc.). Use AI control (ENTITY_AI set to false or via named entity defaults) to keep invisible mobs still if needed.

### Visibility and Particles

- **Hidden particles:** ENTITY_INVISIBLE hides the invisibility potion particle effect, so it doesn't announce "this entity is invisible." Clean and professional.
- **Water and other visual effects:** A torch underwater creates realistic light refraction. A sword in a fireworks burst is visible through the effects.

---

## Creative Combinations

### Invisible Armor + Main-Hand Weapon = Floating Weapon with Full Battle Suit

```yaml
equipment:
  helmet: IRON_HELMET
  chestplate: IRON_CHESTPLATE
  leggings: IRON_LEGGINGS
  boots: IRON_BOOTS
  main_hand: IRON_SWORD
```

Apply invisibility. Now a full suit of armor floats with a sword. The "absent knight" or "ghost warrior."

### Invisible Armor + Off-Hand Torch = Ghostly Light-Bearer

```yaml
equipment:
  helmet: GOLDEN_HELMET
  chestplate: GOLDEN_CHESTPLATE
  off_hand: TORCH
```

Apply invisibility. Golden armor floats, holding a torch. Ethereal and ceremonial.

### Invisible Body, One Item, Moving Path = Guided Journey

```yaml
equipment:
  main_hand: ENDER_PEARL
```

Apply invisibility. An ender pearl floats and moves across the stage. Mystical, otherworldly, magical.

---

## Limitations & Gaps

### Current Limitations

- **No transparency option.** The body is either fully invisible or fully visible — no partial transparency or fade.
- **Duration-based only.** Invisibility runs for a set duration. Once it expires, it's off (unless reapplied). No "on/off toggle" mid-show.
- **Works only on LivingEntities.** Doesn't apply to items lying on the ground or items in inventories — only on entities wearing equipment.

### Workarounds

- **Long duration (9999):** For most shows, set invisibility duration to 9999 and it will last the entire performance.
- **Reapply before expiry:** If you want invisibility to definitely outlast a long show, reapply it mid-show at a safe tick.
- **Use Armor Stands for static displays:** If you want pure costume-on-wire (no AI behaviors), spawn an Armor Stand instead of a mob and equip it — no invisibility needed.

---

## Scene Planning with Invisible Bodies

### Checklist Before Using This Technique

1. **Is the absence important?** The technique works best when "no body" is narratively meaningful, not just technical.
2. **Is the equipment visible?** Make sure what's supposed to show (armor, torch, sword, etc.) is actually there.
3. **Duration plan:** Set `duration_ticks` high enough for your scene. 9999 is safe for any show.
4. **Position plan:** Is the invisible entity where you want it? Position it via offset at spawn, or move it via CROSS_TO after spawn.
5. **Lighting consideration:** If the entity is carrying a torch, will the light reach the player? Test in-game.
6. **Sound accompaniment:** Floating objects benefit greatly from sound design. Coordinate with Sound Designer.
7. **Voice reaction:** Should the player hear a Sprite reaction to the floating object? Coordinate with Voice Director.

---

## Examples From the Archive

*Once the cue library grows, this section will reference production cues that use the invisible-body technique.*

Currently: No production cues use ENTITY_INVISIBLE. This is an opportunity for new cues:

- `wardrobe.reveal.disembodied_armor` — a full suit of armor appearing without bearer
- `wardrobe.light.floating_torch` — a torch moving through space, light-bearer absent
- `wardrobe.ghost.witness` — ghostly armor, facing and watching the player
- `wardrobe.memory.floating_relic` — a meaningful object (book, banner, sword) floating as memory

---

## Notes for Show Planning

- **This is pure theatre.** The invisible-body technique is not about realism; it's about creating visual poetry and emotional impact.
- **Use sparingly.** Because it's powerful, overuse dulls the effect. Reserve invisible bodies for moments that truly need them.
- **Coordinate with other departments.** Floating objects benefit from:
  - Camera direction (making sure the player's eye finds the object)
  - Sound design (audio cues that draw attention to the floating object)
  - Voice design (if the Sprite comments on the impossible object)
  - Lighting (brightness or glow that highlights the object)
- **Test in-game.** The visual effect of floating objects can surprise you. Always test with actual equipment and lighting before the final show build.
