---
department: Wardrobe & Properties Director
created: 2026-03-25
notes: Reference for mob variants and their visual registers. Current status and future availability after Java gap is resolved.
---

# Wardrobe — Mob Variants Reference

Complete inventory of variant options for entities that support appearance customization. This reference serves as both current reality (workarounds) and future capability (once Java gaps are closed).

---

## Current Status

⚠️ **Java Gap — Open:** The `variant:` and `profession:` fields on SPAWN_ENTITY are parsed by the YAML layer but **silently ignored at runtime**. The plugin accepts these fields without error, but they have no effect on the entity's appearance.

**Workaround:** Use the `equipment:` block (equipment slots like helmet, chestplate, main_hand) to differentiate entities visually until the variant gap is resolved.

**When this gap is fixed:**
- `variant:` and `profession:` fields will apply correctly at spawn time
- Wardrobe's visual palette expands enormously
- Each mob type will have 5–100+ distinct appearance options
- Emotional tone mapping becomes part of show authoring (see `wardrobe.emotional-register.md`)

---

## Villagers

### Professions (9 types)

Once the Java gap is fixed, `profession:` will set the Villager's job, which is expressed through distinct visual characteristics (outfit, hat, accessory).

| Profession | Visual Register | Emotional Tone | Notes |
|-----------|-----------------|-----------------|-------|
| `FARMER` | Straw hat, tan/brown palette, hoe ready | Rural, agrarian, land-rooted, honest labor | Earth and growth |
| `LIBRARIAN` | Spectacles, purple/blue palette, formal bearing | Knowledge, scholarship, wisdom, record-keeping | The scholar; carries authority through learning |
| `CLERIC` | Purple robes, ceremonial bearing, mystical | Ritual, mystery, the sacred, healing | Spiritual authority; reads as otherworldly |
| `ARMORER` | Black apron, industrial tools, forge-ready | Craft, protection, industry, making | Maker of things; reads as skilled laborer |
| `BUTCHER` | White apron, pink hair (distinctive), practical | Labor, the ordinary, provision, everyday work | The common person; grounded reality |
| `CARTOGRAPHER` | Monocle, formal bearing, explorer's palette | Exploration, record-keeping, navigation, discovery | The mapper; reads as keen observer |
| `FLETCHER` | Feathered cap, precise bearing, craft-forward | Precision, craftsmanship, arrows, detail-work | Artisanal; the specialist |
| `TOOLSMITH` | Smithing apron, industrial tools, maker's bearing | Making things, tools, industry, craft | Variant of ARMORER; both signal industry |
| `WEAPONSMITH` | Smithing apron, martial bearing, warrior-adjacent | Making weapons, defense, martial art, craft | More aggressive than TOOLSMITH visually |
| `NITWIT` | Green robes, vacant bearing, no profession | The fool, innocence, outsider, one who doesn't belong | Reads as simple or disconnected from society |
| `NONE` | Default robes, no specialized outfit | No profession; generic inhabitant | Use for background villagers, commoners |

### Biome Variants (7 types)

Once the Java gap is fixed, `variant:` will set the Villager's biome type, which determines skin palette, clothing colors, and overall aesthetic.

| Biome Variant | Appearance | Emotional Register | Strategic Use |
|---------------|-----------|-------------------|----------------|
| `PLAINS` | Light tan palette, neutral clothing | Temperate, civilized, default | Standard villager; most universal |
| `DESERT` | Warm tan, sandy palette, lighter clothing | Arid, distant, exotic | Feels displaced in cold/forest settings |
| `JUNGLE` | Green-tinged palette, tropical aesthetic | Lush, exotic, growth-rich, wilderness | Tropical or overgrown settings |
| `SAVANNA` | Warm gold/brown palette, sun-baked | Vast, open, warm climate | Open-air or expansive scenes |
| `SNOW` | Pale, cold-toned palette, winter clothing | Frozen, isolated, cold, displaced | Surreal when placed in warm settings |
| `SWAMP` | Green, murky palette, damp aesthetic | Murky, hidden, mysterious, dark-rooted | Eerie or swampy settings |
| `TAIGA` | Cool-toned palette, forest clothing | Boreal, cool, evergreen, forested | Woodsy or northern settings |

**Cross-biome strategy:** A single SNOW Villager in a PLAINS scene reads as visually "wrong" — perfect for comic displacement, visual strangeness, or deliberate otherness. Mix biome variants to suggest diverse origins or a colorful crowd.

---

## Cats

### Variants (11 types)

Cats have the most visual variety among small mobs. Each variant has distinct coloring and pattern.

| Variant | Appearance | Emotional Register | Use Case |
|---------|-----------|-------------------|----------|
| `TABBY` | Orange/brown tabby stripes | Common, homely, familiar | Domestic, comfortable, ordinary |
| `BLACK` | Solid black | Mysterious, sleek, independent | Magic, night, mystery, independence |
| `RED` | Red/orange solid | Warm, playful, visible | Warmth, companionship, visibility |
| `SIAMESE` | Cream body, dark points (face, ears, tail) | Elegant, aloof, exotic | Refined, independent, otherworldly |
| `BRITISH_SHORTHAIR` | Gray/blue palette, stocky | Dignified, composed, solid | Authority, tradition, groundedness |
| `CALICO` | Multi-color patches (orange, black, white) | Chaotic, festive, abundant | Joy, variety, abundance |
| `PERSIAN` | Pale, fluffy appearance | Delicate, precious, vain | Vanity, delicacy, luxury |
| `RAGDOLL` | Light body, dark points, blue eyes | Ethereal, gentle, otherworldly | Gentleness, etherealness, quiet |
| `WHITE` | Pure white | Pure, clean, sacred | Purity, peace, transcendence |
| `JELLIE` | Orange/white pattern, unusual | Playful, quirky, endearing | Whimsy, lightness, quirk |
| `ALL_BLACK` | Solid black (variant of BLACK) | Sleek, mysterious, void | Void, darkness, mystery, deepening |

**Group strategy:** A mixed flock of cat variants reads as diversity and abundance. All the same variant reads as unity or single character repeated.

---

## Horses

### Color Variants (7 types)

Horses have base color variants plus optional marking overlays, giving enormous visual range.

| Color Variant | Appearance | Emotional Register | Notes |
|----------------|-----------|-------------------|-------|
| `WHITE` | Pure white | Pure, light, noble, celestial | Strongest visual presence |
| `CREAMY` | Pale cream | Soft, gentle, light | Subtle, reserved |
| `CHESTNUT` | Warm reddish-brown | Warm, earthy, common | Familiar, grounded |
| `BROWN` | Medium brown | Neutral, reliable, common | Default, trustworthy |
| `BLACK` | Solid black | Dark, formal, majestic | Powerful, solemn, majestic |
| `GRAY` | Gray/dappled | Wise, aged, ethereal | Suggests age or otherness |
| `DARK_BROWN` | Deep brown | Heavy, dark, somber | Weight, seriousness, darkness |

### Marking Overlays (5 types)

Applied on top of base color. Dramatically change visual impact.

| Marking | Appearance | Effect | Emotional Shift |
|---------|-----------|--------|-----------------|
| `NONE` | Solid color only | Unified, simple | Strength through simplicity |
| `SOCKS` | White on lower legs only | Formal, dressed | Formality, precision |
| `WHITE_DOTS` | White spots across body | Playful, patterned | Whimsy, intricacy |
| `BLACK_DOTS` | Black spots across body | Speckled, complex | Complexity, aging |
| `WHITE` | Blaze down face + white patches | Dramatic, marked | Drama, distinction, individuality |

**Example combinations:**
- WHITE + WHITE (bright, pure, celestial)
- BLACK + WHITE (formal, high-contrast, dramatic)
- CHESTNUT + SOCKS (formal horse, military or ceremonial)
- GRAY + WHITE_DOTS (aged, ethereal, ghostly)

---

## Sheep

### Wool Colors (16 variants)

Each DyeColor maps to a sheep wool variant. The 16 colors of Minecraft wool.

| Color | Variant Name | Emotional Register | Use Case |
|-------|-------------|-------------------|----------|
| White | `WHITE` | Pure, clean, light, default | Standard; most common |
| Orange | `ORANGE` | Warm, energetic, earthy | Warmth, autumn, rustic |
| Magenta | `MAGENTA` | Magical, theatrical, unusual | Fantasy, magic, otherness |
| Light Blue | `LIGHT_BLUE` | Calm, sky-like, peaceful | Sky, peace, transcendence |
| Yellow | `YELLOW` | Bright, cheerful, sunny | Joy, warmth, light |
| Lime | `LIME` | Acidic, eerie, neon | Wrongness, unnatural, tech |
| Pink | `PINK` | Soft, delicate, vulnerable | Gentleness, femininity, sweetness |
| Gray | `GRAY` | Neutral, understated, cool | Subtlety, cool contrast, the quiet one |
| Light Gray | `LIGHT_GRAY` | Pale, ghostly, faded | Fading, ghostliness, age |
| Cyan | `CYAN` | Cool, aquatic, techno | Water, cool, digital |
| Purple | `PURPLE` | Regal, mysterious, ritual | Ritual, mystery, nobility |
| Blue | `BLUE` | Deep, calm, water-like | Depth, coolness, water |
| Brown | `BROWN` | Earthy, warm, grounded | Earth, grounding, warmth |
| Green | `GREEN` | Natural, growth, fertile | Nature, growth, life |
| Red | `RED` | Intense, hot, passionate | Passion, heat, intensity |
| Black | `BLACK` | Dark, dramatic, bold | Mystery, dramatic contrast, the striking one |

**Group strategy:**
- **All white:** Unity, uniformity, innocence
- **All black:** Dramatic, bold, theatrical mystery
- **Mixed colors:** Abundance, festivity, chaos (depending on context)
- **Gradient (light to dark):** Journey, transition
- **Single odd color among whites:** Otherness, the outlier, the different one (powerful for narrative)

---

## Wolves

### Coat Colors (variants, 1.21+)

Minecraft 1.21 introduced multiple wolf coat variants beyond the classic white/gray.

| Variant | Appearance | Emotional Register | Notes |
|---------|-----------|-------------------|-------|
| Pale Wolf | Default pale gray-white | Classic, familiar, default | The standard wolf; most common |
| Woods Wolf | Brown/tan variant | Earthy, grounded, forest-rooted | Blends with natural settings |
| Ashy Wolf | Dark gray/charcoal | Shadowy, mysterious, somber | Reads darker than pale |
| Black Wolf | Solid black | Dark, mysterious, wild, dangerous | Most dramatic visual contrast |
| Rusty Wolf | Red-brown variant | Warm, earthy, aged | Rustic, weathered aesthetic |
| Snowy Wolf | White/pale variant | Pure, cold, arctic | Snow and cold settings |
| Striped Wolf | Banded/striped pattern | Wild, feral, untamed | Reinforces wildness |

**Note:** Exact wolf variant names and availability should be verified against 1.21.x Minecraft documentation. The above names are indicative based on game design patterns.

---

## Parrots

### Color Variants (5 types)

Parrots perched on a figure's shoulder (or floating via invisibility) immediately read as adventure, exploration, or whimsy.

| Variant | Appearance | Emotional Register | Use Case |
|---------|-----------|-------------------|----------|
| `RED` | Bright red plumage | Bold, warm, energetic | Warmth, adventure, boldness |
| `BLUE` | Bright blue plumage | Cool, mysterious, elegant | Mystery, cool elegance, sky |
| `GREEN` | Bright green plumage | Natural, growth, vitality | Life, growth, nature, vitality |
| `CYAN` | Cyan/turquoise plumage | Techno, digital, cool | Modernity, cool, water/sky |
| `GRAY` | Gray plumage, understated | Somber, quieter, less flashy | Subtlety, age, reserve |

A parrot on a Villager's shoulder reads immediately as: explorer, adventurer, pirate, or magical companion. A flock of mixed-color parrots is pure fantasia.

---

## Tropical Fish

### Pattern & Color Customization

Tropical Fish have the most granular variant control in vanilla Minecraft — nearly infinite configuration. Each tropical fish variant is individually defined by:
- **Base color** (16 DyeColors)
- **Pattern** (STRIPEY, FLOPPED, BLOCKED, SPOTTY, POWDER_SNOW, RAINBOW, BLOCKSTRIPE, STRIPED)
- **Pattern color** (16 DyeColors)

**Strategic use:** Tropical fish variants are best used for visual texture in aquatic scenes. They rarely have narrative meaning individually, but a school of mixed-variant tropical fish reads as abundance, life, and natural beauty.

⚠️ **Note:** Exact configuration syntax for tropical fish variants in YAML is not yet documented. Flag with Stage Manager if detailed tropical fish variant control is needed for a show.

---

## Other Entities with Equipment Support (but limited or no variants)

The following entities accept equipment via the `equipment:` block but have limited or no variant options:

| Entity Type | Equipment Support | Variants Available | Notes |
|------------|------------------|-------------------|-------|
| Zombie | Yes (all slots) | None (yet) | Can be equipped with armor; reads as undead soldier if fully armored |
| Skeleton | Yes (all slots) | None | Weaponed Skeleton reads as threat; full armor reads as warrior-skeleton |
| Armor Stand | Yes (all slots) | None (not applicable — no "body") | Perfect for costume-on-a-wire displays; static unless posed |
| Pillager | Yes (main hand: crossbow) | None | Crossbow-equipped is default; limited customization |
| Vindicator | Yes (main hand: axe) | None | Axe-equipped is default; add armor for effect |
| Drowned | Yes (equipment) | None | Can be equipped; reads as drowned warrior if armored |
| Husk | Yes (equipment) | None | Desert zombie variant; limited visual customization |
| Wither Skeleton | Yes (equipment) | None | Naturally hostile; armor adds weight but not safety |

---

## Notes for Show Planning

### When Planning Mob Appearance:

1. **Decide profession/variant early.** These choices shape emotional register. Flag with Wardrobe during the brief.
2. **Account for the Java gap.** Until variants are live, use equipment (armor, main-hand props) to differentiate mobs visually.
3. **Plan color palettes by scene.** If a scene uses multiple mobs, group variants by emotional tone, not randomly.
4. **Use Armor Stands for static displays.** If you need a costume on a wire or a dramatic tableau, Armor Stands are the tool.
5. **Remember the invisible body technique.** A figure with variant clothing + invisibility + floating held item is profoundly ethereal.

### Variant-Dependent Show Concepts:

These show concepts become possible once the variant gap is fixed:

- **"The Society" (Villagers with varied professions in one scene)** — a bustling community reads as diverse and alive
- **"The Travelers" (Villagers from different biomes)** — a colorful delegation, diversity through visual mismatch
- **"The Menagerie" (mixed animal variants)** — abundance, festivity, or chaos depending on context
- **"The Guard" (Armor Stands in varied armor but unified stance)** — witnesses, sentinels, the dramatic pageant
- **"The Flock" (colored sheep, all one variant or all different)** — unity or chaos; abundance
- **"The Hunt" (wolves with distinct coat colors, moving with purpose)** — wildness, danger, or strange beauty

---

## Future Work

- [ ] Verify exact wolf variant names and enum values for 1.21.x (PaperMC Javadoc)
- [ ] Document tropical fish variant YAML syntax once it's needed in a show
- [ ] Test variant appearance in-game once the Java gap is resolved
- [ ] Update this reference with any visual register surprises discovered in-game
- [ ] Create cue families organized by variant palettes (e.g., `appearance.villager.authority`, `appearance.animals.festive`)
