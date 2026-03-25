---
department: Wardrobe & Properties Director
created: 2026-03-25
notes: Detailed reference for each equipment slot and its dramatic function.
---

# Wardrobe — Equipment Slots Reference

A complete guide to how each equipment slot on a LivingEntity communicates dramatic meaning. Equipment is applied via `equipment:` at spawn time (in SPAWN_ENTITY) or changed mid-show via ENTITY_EQUIP.

---

## The Six Equipment Slots

Every LivingEntity (mobs, Armor Stands, tamed animals) supports six equipment slots:

```yaml
type: SPAWN_ENTITY
entity_type: VILLAGER
name: "Herald"
equipment:
  helmet: GOLDEN_HELMET
  chestplate: IRON_CHESTPLATE
  leggings: IRON_LEGGINGS
  boots: IRON_BOOTS
  main_hand: STICK
  off_hand: TORCH
```

All slots are optional. Omitted slots remain empty. Equipment is set at spawn; to change mid-show, use ENTITY_EQUIP.

---

## Helmet

**What it does:** The first thing an observer sees when looking at a figure. The helmet frames and defines the head — probably the most emotionally loaded piece of armor.

**Dramatically:**
- Helmeted = protected, formal, military, official
- Bare-headed = vulnerable, intimate, civilian, informal

### Standard Armor Helmets

| Material | Register | Tone | Use |
|----------|----------|------|-----|
| LEATHER_HELMET | Common, light, civilian | Everyday, humble, practical | Farmers, workers, civilians |
| IRON_HELMET | Military, official, strong | Authority, defense, formality | Guards, soldiers, officials |
| DIAMOND_HELMET | Precious, powerful, rare | Nobility, strength, wealth | Heroes, nobles, champions |
| GOLDEN_HELMET | Ceremonial, regal, precious | Royalty, ritual, treasure | Kings, priests, treasure-keepers |
| NETHERITE_HELMET | Dark, powerful, otherworldly | Darkness, power, the abyss | Evil figures, dark magic, otherness |

### Non-Armor Head Pieces

| Item | Register | Effect | Best For |
|------|----------|--------|----------|
| CARVED_PUMPKIN | Unsettling, agricultural, distinctive | Changes entire silhouette; surreal | Eerie figures, scarecrows, the uncanny |
| Player head (mob heads: CREEPER, SKELETON, ZOMBIE, WITHER_SKELETON, DRAGON, etc.) | Grotesque, eerie, occult | Replacing a head with another being's head | Witches, dark magic, necromancy, horror |
| Any decorative head (flowers? — check availability) | Whimsical, festive, absurd | Lightens the tone; memorable | Celebration, absurdity, the ridiculous |

### No Helmet

An entity with no helmet reads as:
- Vulnerable and intimate
- Civilian and unprepared
- Trusting (no defense needed because they're safe, or because they belong here)

---

## Chestplate

**What it does:** The torso armor. Large, visible, defines the figure's bulk and posture. Signals protection and role.

**Dramatically:**
- Full chestplate = complete protection, full commitment to role, armored and ready
- No chestplate = open, vulnerable, civilian

### Armor Chestplates

| Material | Register | Tone | Use |
|----------|----------|------|-----|
| LEATHER_CHESTPLATE | Light, flexible, civilian | Labor, humility, practicality | Workers, farmers, civilians |
| IRON_CHESTPLATE | Solid, military, reliable | Guardianship, defense, duty | Guards, soldiers, protectors |
| DIAMOND_CHESTPLATE | Precious, strong, noble | Nobility, power, excellence | Warriors, nobles, heroes |
| GOLDEN_CHESTPLATE | Ceremonial, royal, glowing | Royalty, ritual, treasure | Kings, priests, divine figures |
| NETHERITE_CHESTPLATE | Dark, powerful, final | Darkness, ultimate power, abyss | Evil figures, dark lords, final forms |

### Mixed Armor Strategies

Mismatching armor pieces creates interesting effects:

- **Iron helmet + leather chestplate:** Military head, civilian body — the soldier out of place or transition
- **Golden helmet + iron chestplate:** Ceremonial authority over practical defense — the monarch who also fights
- **Leather helmet + iron chestplate:** Vulnerable head, armored body — the protected but exposed official
- **No helmet + full chestplate:** Torso protected, head exposed — trusting but prepared

---

## Leggings

**What it does:** Covers the legs and hips. Often overlooked, but visible from certain angles. Signals completeness of armor or informality.

**Dramatically:**
- Full leggings = complete armor set, formal, finished
- No leggings = incomplete armor (odd), civilian clothes, or bare legs (vulnerable)

### Armor Leggings

| Material | Register | Tone | Notes |
|----------|----------|------|-------|
| LEATHER_LEGGINGS | Light, practical, civilian | Labor, humility, movement | Farmers, workers |
| IRON_LEGGINGS | Solid, military, complete | Full protection, duty | Soldiers, guards |
| DIAMOND_LEGGINGS | Precious, noble, complete | Nobility, power | Heroes, nobles |
| GOLDEN_LEGGINGS | Ceremonial, royal, complete | Royalty, ritual | Kings, priests |
| NETHERITE_LEGGINGS | Dark, powerful, complete | Darkness, ultimate power | Dark figures |

### Visual Effect

Leggings are less immediately visible than helmet or chestplate, so their emotional impact is subtle. They contribute to "completeness" — a figure in full matching armor (all same material) reads as unified and formal. A figure with mismatched pieces reads as scrounged armor, makeshift, or transitional.

---

## Boots

**What it does:** The feet and foundation. Often unseen from above, but visible up close. Signals completeness and groundedness.

**Dramatically:**
- Full boots = complete armor, formal, grounded, stable
- No boots = casual, vulnerable at foundation, informal or intimate

### Armor Boots

| Material | Register | Tone | Notes |
|----------|----------|------|-------|
| LEATHER_BOOTS | Light, practical, civilian | Humility, practicality | Workers, farmers |
| IRON_BOOTS | Solid, military, grounded | Stability, duty, defense | Soldiers, guards |
| DIAMOND_BOOTS | Precious, noble, stable | Nobility, strength | Heroes, nobles |
| GOLDEN_BOOTS | Ceremonial, royal, precious | Royalty, ceremony | Kings, priests |
| NETHERITE_BOOTS | Dark, powerful, final | Darkness, finality | Dark figures |

### Visual Strategy

Boots complete the armor set. A figure in helmet + chestplate + leggings + boots reads as fully committed and formal. A figure in helmet + chestplate but no leggings or boots reads as makeshift or transitional (the soldier stripping down, the civilian putting on what armor they could find).

---

## Main Hand

**What the entity carries in their dominant hand.** This is the primary storytelling slot after the helmet. The main-hand item is read before almost anything else.

**Dramatically:**
- Main hand is IDENTITY
- What you hold is who you are, momentarily

### Weapons

| Item | Register | Tone | Use |
|------|----------|------|-----|
| SWORD (any material) | Martial, threat, protection, authority | Danger, power, defense | Soldiers, warriors, commanders |
| AXE (any material) | Martial, brutal, labor | Raw power, labor, axes don't fit neatly into role | Executioners, woodcutters, berserkers |
| PICKAXE | Labor, mining, industry | Industry, extraction, miners | Workers, miners, industrial figures |
| SPEAR / TRIDENT | Martial, reach, noble | Nobility, reach, guardianship | Elite guards, nobles, spear-throwers |
| STAFF (any long stick) | Wisdom, magic, authority | Authority, knowledge, wizardry | Mages, elders, authority figures |
| BOW | Precision, range, hunter, sniper | Hunting, precision, distance | Hunters, snipers, archers |

### Tools and Implements

| Item | Register | Tone | Use |
|------|----------|------|-----|
| HOE | Farming, rural, labor | Agriculture, groundedness, simple labor | Farmers, workers |
| SHOVEL | Digging, labor, industrial | Labor, extraction, practical work | Workers, grave-diggers |
| TORCH | Light, guide, warmth, hope | Guidance, illumination, warmth, hope, searching in darkness | Guides, pilgrims, hope-bearers |
| LANTERN | Light, formal, structure | More formal light than torch; structure, order | Officials carrying light, organized light-bringers |
| BOOK | Knowledge, scholarship, record | Learning, knowledge, history, memory | Scholars, librarians, record-keepers |
| BUCKET | Practicality, water-carrying, humility | Practical labor, service, humility | Workers, servants, water-carriers |
| STICK | Default, humble, simple | Simplicity, ordinariness, walking stick | Ordinary people, elders with walking sticks |

### Ritual Objects

| Item | Register | Tone | Use |
|------|----------|------|-----|
| BANNER | Herald, announcement, ceremony | Announcement, heraldry, ceremony, declaration | Heralds, ceremonial figures, declarations |
| DEAD_BUSH | Withering, sorrow, desolation | Sorrow, withering, desolation, death | Figures of mourning, death, loss |
| APPLE | Innocence, simplicity, sustenance | Innocence, temptation, sustenance | Innocents, temptresses, nurturers |
| EGG | Fragility, beginning, potential | Fragility, new beginnings, potential | Figures of hope, mothers, builders |
| SNOWBALL | Playfulness, winter, innocence | Playfulness, childishness, winter | Children, playful spirits |
| ENDER_PEARL | Magic, otherness, the beyond | Magic, the otherworldly, dimensions, unreality | Mages, otherworldly figures, reality-benders |

### Non-use

**No main-hand item** (empty hands):
- Reads as vulnerable, peaceful, open
- Hands available for gesturing, climbing, or reaching
- Unthreatening and intimate
- Often used with off-hand torch to create "bearer of light, not weapon" effect

---

## Off Hand

**What the entity carries in their non-dominant hand.** Often used for light sources or supporting items.

**Dramatically:**
- Off-hand is SUPPORT or ACCENT
- Not the primary identity, but adds dimension

### Common Off-Hand Uses

| Item | Register | Effect | Best For |
|------|----------|--------|----------|
| TORCH | Ambient light | Illuminates surroundings; warm glow without taking main-hand prop slot | Light-bearers, guides, warmth, intimate scenes |
| LANTERN | Formal light | Structured, contained light; more formal than torch | Officials, formal ceremonies, structured light |
| SHIELD | Defense | Visual barrier; blocking silhouette; adds protection feel | Defensive figures, guards, wall-builders |
| BOOK | Support | Secondary knowledge or reference; scholar's backup | Scholars with tools, wisdom-keepers |
| ENDER_PEARL | Magic accent | Adds otherworldly feel without replacing main tool | Mages, otherworldly figures |
| APPLE | Sustenance accent | Adds nurturing feel | Healers, mothers, nurturers |
| (Empty) | No accent | Frees both hands symbolically (but no items can be held) | Peaceful figures, those ready to receive |

### Strategic Off-Hand Lighting

A torch in off-hand is one of Wardrobe's most efficient narrative tools:

- **Main hand weapon + off-hand torch:** "I carry both danger and warmth"
- **Main hand tool + off-hand torch:** "I work, but I also light the way"
- **No main hand + off-hand torch:** "I am only a light-bearer" (intimate, supporting role)

---

## ENTITY_EQUIP — Changing Equipment Mid-Show

Equipment can be set at spawn via SPAWN_ENTITY `equipment:` block, or changed mid-show via ENTITY_EQUIP event.

```yaml
type: ENTITY_EQUIP
target: entity:spawned:Herald
helmet: IRON_HELMET          # Set new helmet
chestplate: IRON_CHESTPLATE  # Set new chestplate
leggings: ""                 # Empty string = remove item from slot
boots: ""                    # Remove boots
main_hand: SWORD             # Set new main-hand item
off_hand: SHIELD             # Set new off-hand item
```

**Behavioral notes:**
- Empty string `""` explicitly removes an item from a slot
- Omitted fields are left unchanged — their current items stay equipped
- All slot changes apply simultaneously; there's no stagger between slots
- ENTITY_EQUIP works on named entities and entity groups (applies to all members simultaneously)

**Narrative use:** Plan costume changes as scene beats, not casual updates. A transformation from unarmed to sword-wielding is a character moment. Use it to mark turning points.

---

## Equipping Specific Entity Types

### Humanoid Mobs (Villagers, Zombies, Skeletons)

All equipment slots work. Armor pieces fit naturally. Mobs look like they're wearing whatever you give them.

### Armor Stands

All equipment slots work. Armor Stands are perfect display surfaces — a suit of armor on an Armor Stand is a "costume on a wire" with no body underneath. This is intentional and powerful.

**Note:** Armor Stand limb posing (angling arms, legs, head) requires COMMAND and is outside the show cleanup contract. Use sparingly; document cleanup in run sheet.

### Tamed Animals (Wolves, Cats, Horses)

Equipment works, but the visual effect is different:
- Wolves and Cats: Armor pieces sit on their bodies, not naturally fitted (they have a different build than humanoids)
- Horses: Armor pieces are designed for horses and fit much more naturally than on other animals
- Parrots: Equipment is not applicable (too small)

**Visual effect:** A sword-wielding cat or wolf is surreal and memorable. A fully armored horse is imposing and noble.

---

## Material Value and Emotional Hierarchy

Equipment materials signal value and power tier:

**Tier 1 (Humble):** LEATHER — accessible, common, civilian
**Tier 2 (Strong):** IRON — military, official, reliable
**Tier 3 (Precious):** DIAMOND — rare, powerful, noble
**Tier 4 (Royal):** GOLD — ceremonial, treasure, regal
**Tier 5 (Dark/Final):** NETHERITE — dark, powerful, otherworldly, endgame

Mismatching tiers creates visual tension:
- Iron helmet + leather chestplate: Downgrade mid-body, signals transition or loss
- Golden helmet + iron chestplate: Formal head, practical body, signals authority mixed with work
- Netherite helmet + golden boots: Darkness above, ceremony below, unsettling mix

---

## Non-Standard Equipment Uses

Creative applications beyond literal armor:

### Helmet Creativity

- **CARVED_PUMPKIN:** Jack-o'-lantern effect; unsettling and memorable
- **Mob heads (creeper, skeleton, dragon, etc.):** Replace the head with another being's — profound and eerie
- **Flower pot (if available as item):** Absurdist humor on a Villager's head
- **LANTERN:** A lantern worn as a helmet (if physics allows) — unusual light source placement

### Main-Hand Creativity

- **DEAD_BUSH:** A withered plant as main-hand prop — sorrow and desolation in object form
- **BANNER (dyed):** A flag or banner — heraldry, announcement, color statement
- **REDSTONE:** Raw redstone held — magic, industry, power
- **ENDER_PEARL:** Held otherworldly object — magic, danger, dimensional access
- **WRITTEN_BOOK:** A book with text (if readable) — knowledge made specific

---

## Planning Equipment for a Scene

### Costume Design Checklist

Before equipping entities for a show:

1. **Helmet first:** What should the head communicate? Is it protected or vulnerable? Formal or intimate?
2. **Main hand second:** What does this figure *do*? What's their primary identity?
3. **Full armor or partial?** Is completeness important, or does incompleteness tell a story?
4. **Color palette:** Do all entities share a material tier, or is mismatch intentional?
5. **Off-hand light:** Do any figures need to carry warmth or illumination?
6. **Changes mid-show:** Are there costume transformation moments? Plan them as scene beats.

### Scene Palette Example

**Arrival scene with four Villagers:**
- **Herald:** Golden helmet (authority), banner in main hand (announcement), torch in off-hand (ceremony)
- **Guard 1:** Iron helmet & chestplate, sword in main hand, empty off-hand (ready and alert)
- **Guard 2:** Iron helmet & chestplate, sword in main hand, empty off-hand (ready and alert)
- **Villager:** No helmet (vulnerability), torch in main hand (light-bearer), empty off-hand (peaceful)

Each figure communicates role and emotional stance before moving.

---

## Notes for Show Planning

- **Equipment is visible from spawn.** Design it as part of the first impression, not an afterthought.
- **Changes matter.** If you ENTITY_EQUIP mid-show, that change is a narrative moment. Plan it with the Show Director.
- **Less is often more.** A figure with just a torch in main hand reads more powerfully than a figure in full armor carrying five items.
- **Consistency builds world.** If all guards have matching armor, the scene feels organized. If they have random mismatches, it feels chaotic (useful for that effect, but intentional).
- **Test in-game.** Armor appearance can be surprising on different mob types. Always test equipment visuals before final show build.
