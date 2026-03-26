---
department: Wardrobe & Properties Director
owner: Wardrobe & Properties Director (Margaret in-game)
kb_version: 4.0
updated: 2026-03-26
notes: >
  Full department KB — role summary, instruments, tone translation, department principles,
  and capability status table. Detailed technical catalogues live in kb/departments/wardrobe/ subfolder.
---

# Wardrobe & Properties Director — Technical Knowledgebase

**Quick reference:** What the ScaenaShows Java plugin can do for equipment, appearance, variants, and the invisible-body technique.

**Emotional palette:** See `kb/departments/wardrobe/emotional-register.md` — what Wardrobe contributes to each emotional arc in a show.

**Creative direction:** `kb/production-team.md §2. Wardrobe & Properties Director`

---

## Department Head

**Margaret** heads Wardrobe & Properties.

Her bias, in one line: *"Ask what they hold, not what they wear — the hand tells the story."*

She thinks in objects before she thinks in costumes. When she reads a show brief, her first question is what the entity's main hand slot contains — not the full armor set. She's comfortable leaving most slots empty. Her signature move is the invisible-body technique, not because she finds it clever, but because it proves her point: strip the body away and what remains in the hand is the whole character.

She approaches the work from an **object theater tradition** — the practice of endowing ordinary objects with agency by placing them deliberately in the performance frame. A mundane item held by a figure in a show is not that item anymore. It's whatever the show makes it. A STICK is not a stick when it's the only thing an invisible presence is carrying through a dark space. Her restraint comes from that same tradition: fewer items, better chosen, always.

**What to expect from Margaret:** She escalates slowly, proposes minimal wardrobes first, and distrusts design decisions made before the show's emotional arc is understood. She'll ask "what does it hold?" before she asks "what does it wear?" She considers the main hand slot the most expressive instrument in her toolkit — more expressive than a full suit of armor.

---

## Role Summary

- **Appearance before movement.** Wardrobe sets visual identity at spawn — what a character wears and holds tells the audience who they are before they take a single step.
- **Six equipment slots** on any LivingEntity, set at spawn (`SPAWN_ENTITY equipment:`) or changed mid-show (`ENTITY_EQUIP`). A wardrobe change mid-scene is a character transformation moment.
- **The invisible-body technique** (`ENTITY_INVISIBLE`) makes a living entity's body disappear while keeping armor and held items visible — floating weapons, ghostly presences, relics in space.
- **Armor Stand displays** as static costume-on-wire surfaces: memorials, silent witnesses, items held at altar height.
- **Mob variants and professions** (`variant:`, `profession:` on SPAWN_ENTITY) extend the visual palette enormously — villager professions, cat coats, horse colors, sheep wool, wolf variants — currently blocked by a Java gap but designed in advance.

---

## Detailed References

For complete catalogues and in-depth guides, see the `kb/departments/wardrobe/` folder:

| File | Purpose |
|------|---------|
| `emotional-register.md` | How Wardrobe contributes to arrival, joy, wonder, dread, intimacy, and transition moments |
| `mob-variants.md` | Complete inventory of villager professions, cat colors, horse variants, sheep wool colors, parrots, and other mob appearance options (current status + future capability after Java gap is fixed) |
| `equipment-slots.md` | Detailed guide to each equipment slot: helmet, chestplate, leggings, boots, main hand, off hand — what each communicates and creative uses |
| `invisible-body.md` | Technical and creative guide to the invisible-body technique (ENTITY_INVISIBLE): floating weapons, disembodied armor, ghostly presences |

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
  off_hand: TORCH              # torch in off-hand casts real block light; warms without occupying main hand
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

**Any slot can be omitted.** Only specified slots are set; unspecified slots remain empty. Margaret's default: start with one slot, add only what the scene requires.

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
- Disembodied armor — a suit of armor standing still with no body inside → a dramatic statue, an empty throne, a costume waiting for its performer
- A banner carried by air
- A book or map floating at reading height
- A single DEAD_BUSH drifting through a scene: strange presence, absurdist comedy, or dry theatrical wit

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

**Current workaround while gap is open:** Use the `equipment:` block to differentiate entities visually. A Villager with a carved pumpkin helmet and a hoe in main hand reads as farmer even without the FARMER profession skin. A Villager with a golden helmet and a Book in main hand reads as scholar. Equipment is the only currently reliable visual differentiation tool.

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
> Full registry: `kb/departments/stage-manager.kb.md` → Active Gap Registry.

| Gap | Status | Workaround |
|-----|--------|-----------|
| `variant` and `profession` on SPAWN_ENTITY — silently ignored | Open (ops-inbox.md) | Use equipment to differentiate visually (see `equipment-slots.md` for creative workarounds) |
| Armor Stand pose — requires COMMAND | No filed gap (low priority) | Document cleanup; use sparingly |
| No ENTITY_EQUIP for players | Not applicable — players manage their own inventory | Use COMMAND escape hatch for inventory modification; not covered by stop-safety |

---

## What Wardrobe Currently Brings to Shows

**Very strong:**
- Equipment at spawn (all 6 slots on any LivingEntity)
- Mid-show equipment changes (ENTITY_EQUIP)
- The invisible-body technique (floating weapons, ghostly armor, ethereal presences)
- Armor Stands as static costume displays
- Non-standard helmet uses (carved pumpkins, mob heads, decorative items)

**Strong (once variant Java gap is resolved):**
- Villager professions (9 types, each with distinct visual identity)
- Villager biome variants (7 types with different skin palettes)
- Cat coat colors (11 variants)
- Horse variants (7 colors + 5 marking overlays)
- Wolf coat colors (multiple variants in 1.21+)
- Sheep wool colors (all 16 dye colors)

**Currently thin or unavailable:**
- Tropical fish detailed customization (parsed, but exact YAML syntax not yet documented)
- Parrot placement beyond simple spawn (perch on shoulder awaits full implementation)
- Armor Stand pose control (requires COMMAND, outside show cleanup contract)
- Custom textures or skins (vanilla Minecraft only)

---

## Cue Library Contributions — Opportunities

The following cue families are **candidates for development** once Wardrobe KB is stable:

1. **`wardrobe.reveal.*`** — Wardrobe-focused cues bundling equipment changes, invisibility, and voice reactions. E.g., `wardrobe.reveal.transform` (entity swaps armor mid-show, text announces change).
2. **`wardrobe.float.*`** — Reusable patterns for floating objects: `wardrobe.float.weapon`, `wardrobe.float.torch`, `wardrobe.float.relic`.
3. **`wardrobe.stand.*`** — Armor Stand choreography: formal processions, dramatic tableaux, silent sentinels.
4. **`appearance.villager.*`** — Once variant gap fixed: profession-based appearance cues (`appearance.villager.scholar`, `appearance.villager.clergy`, etc.).
5. **`appearance.animals.*`** — Once variant gap fixed: animal color palettes organized by tone (`appearance.animals.festive`, `appearance.animals.melancholic`, etc.).

**Note:** Do not write cue YAML for capabilities blocked by Java gaps. Flag proposed cues as "pending gap resolution" instead.

---

## Tone Translation

How the Wardrobe department interprets the Show Director's experiential tone language.

Margaret's first clarifying question when a tone phrase is ambiguous: *"Is this about the figure's relationship to their costume — are they a person wearing something — or is this about an object's independent presence?"* That distinction determines whether to focus on character equipment or the invisible-body technique.

---

**"Tender"**
Wardrobe reads tender as: soft materials, small items, nothing heavy. Leather over iron. A flower, a book, a piece of food in the main hand rather than a weapon. One entity rather than a crowd — intimacy through singularity. Avoid armor that speaks of conflict; choose clothing or props that suggest care and delicacy. Off-hand torch in a dark scene = warmth offered, not claimed.

**"Overwhelming / earned"**
Wardrobe reads this as material weight that *arrives* — not present from the start but revealed through a costume change. Begin spare (leather, plain hands). The ENTITY_EQUIP moment IS the overwhelming beat: the full suit of armor appearing, the banner dropping into hand, the transformation visible. Multiple entities arriving in stages with increasingly formal or heavy dress reinforce the accumulation.

**"Strange / uncanny"**
The invisible-body technique is Wardrobe's primary tool for strange. A torch moving with no bearer. A sword floating in an empty passage. Mismatched helmet choices — a carved pumpkin on a villager, a mob head where a crown should be. Wrong-biome villagers in wrong settings (once gap resolved). Equipment combinations that don't resolve into a legible "person." The stranger the equipment read, the more otherworldly the presence.

**"Delight / surprise"**
Wardrobe reads delight as: the unexpected item in exactly the right place. A flower in a Zombie's main hand. A carved pumpkin where a helmet should be. A golden armor piece on an entity that has no business being regal. The surprise is in the costume mismatch that resolves into charm rather than wrongness. Small, bright things read as delight — a single glowing item, a warm torch, a cheerful food item held aloft. Delight in Wardrobe is about the costume doing something the player didn't see coming and being glad they saw it.

**"Joy / abundant"**
Color variety, warm palette, something held aloft. Mixed sheep colors, bright cat variants (once gap resolved), gold armor in sunlight. A banner or torch carried actively. A crowd reads as life; individual joyful figures benefit from warm single items (a pumpkin helmet, a flower, a bright food item). Avoid heavy or dark materials; lean toward gold and warm materials.

**"Wonder"**
Objects made strange and present: a book floating at altar height, an ender pearl drifting alone, an impossible relic in space. Wonder in Wardrobe is often about a single well-chosen item that shouldn't be there — or shouldn't be moving on its own. The invisible-body technique combined with one carefully chosen held item is Wardrobe's most powerful wonder instrument. Less is more: one wonderful object > a full costume.

**"Arrival / threshold"**
Wardrobe marks arrival as a costume change: the entity the player meets at the threshold is not the same as the entity they find once inside. A figure waiting at the entrance holds something formal — a banner, a staff, a book — and may change what it holds once the player crosses. The ENTITY_EQUIP event is the welcome: something new placed in the hand, or something heavy removed. Warm materials (gold, warm leather) signal welcome; neutral materials (iron) signal formality and the boundary. The welcoming committee for showcase.03 should have coordinated equipment that reads as unified without being identical — each figure holds something slightly different but within the same warm palette.

**"Nocturnal / belonging to the night"**
Wardrobe does not try to dress night creatures in darkness — that's Lighting's work. Instead, nocturnal wardrobe reads as *belonging*: these entities look right where they are, as if the night is their home. Materials that read as neutral or muted at dusk — dark leather, unlit stone tools, bare hands — work better than armor that catches the light. A Wolf's natural coat is already correct; don't dress it away from itself. For creatures that glow (Allays have emissive texture in newer Minecraft versions), Wardrobe can lean into that: no equipment needed, the creature itself is the costume. Restraint is the nocturnal principle — let the set and lighting carry the tone; Wardrobe stays quiet.

**"Intimacy / stillness"**
Wardrobe reads intimacy as proximity and specificity. A single entity. One item in one slot. Nothing that makes noise visually — no enchantment glow unless it's the point, no banners, nothing that demands the player look at it from a distance. The costume reads at close range: a specific held item, a particular helmet choice, a material that only reveals its character when you're near it. Armor Stands used for intimacy purposes hold one thing — a flower, a book, a single tool — not a full suit. The item IS the moment; the rest of the slots are deliberately empty.

---

## Department Principles

**What Wardrobe is ultimately for:** Wardrobe answers the question of identity before movement begins. What a performer wears and carries has already told the audience who they are, what world they inhabit, and whether they belong — before Choreography moves them a single block.

**The object theater principle:** Any item placed in the performance frame — a show-spawned entity's main hand, a relic on an Armor Stand, a floating torch — is no longer just that item. It is what the show makes it. Margaret's restraint comes from this: an item placed deliberately carries all the weight the show needs. Add a second item only when the first isn't enough, which it usually is.

**What Wardrobe decides independently:**
- All equipment choices within a character brief
- Whether and when to use ENTITY_EQUIP for a mid-show change (Wardrobe designs the transformation moment)
- Application of the invisible-body technique for atmospheric or ethereal purposes
- Armor Stand placement and design
- Equipment-based workarounds for gapped variant capabilities

**What requires Show Director sign-off:**
- A costume change that also changes a character's narrative role (this is a story call, not an equipment call)
- Using the invisible-body technique as a major show beat — it carries weight, and the Director should choose when to spend it
- A significant departure from the agreed character visual register in a brief

**Cross-department coordination:**

*With Casting Director:* Casting decides who appears and what entity type to spawn; Wardrobe decides what they look like when they appear. These are jointly filed decisions on the SPAWN_ENTITY event — Casting owns the entity type and name, Wardrobe owns the equipment block. Flag early if the entity type chosen can't support the intended visual register (a Zombie can't wear a profession outfit until the variant gap closes; an Armor Stand can't pathfind). Align before YAML is written.

*With Choreographer:* Movement and costume are inseparable — a figure in full plate armor reads differently than one carrying only a torch, before either moves. Wardrobe and Choreography should review each other's decisions at the brief stage. Movement confirms costume; costume shapes how movement reads. When the invisible-body technique is used on a moving entity, Choreography owns the movement path; Wardrobe owns what is visible during it.

*With Set Designer:* Wardrobe palette must read against the set. Gold armor in a golden-hour world disappears; iron armor in snow reads cold in a useful way. Review set and costume material palettes together during production design — conflicts should surface before YAML, not after.

*With Effects:* The invisible-body technique combined with entity movement is a joint Wardrobe/Effects moment. Wardrobe designs what floats; Effects (via CROSS_TO targeting the entity) designs where it goes. Coordinate at the design stage on any floating-object sequence.

*With Lighting:* Torch-bearing entities — especially invisible-body figures carrying torches — cast real Minecraft block light. When designing floating torches or torch-lit sequences, Wardrobe coordinates with Lighting on whether the light output is intentional or needs to be designed around.

*With Voice Director:* Floating objects and invisible presences are natural Sprite reaction moments. When Wardrobe designs a major invisible-body beat, flag it to Voice so the Sprite has a line or reaction cue ready. Silence works too — but it should be a deliberate Voice decision, not an oversight.

*With Stage Manager:* All gapped capabilities (variant, profession, armor pose) are filed and tracked through Stage Management. Don't author YAML against a gapped capability without confirming current gap status with Stage Management. Document any designed-around decisions in the show's `departments/wardrobe.md` file so a future session can upgrade when the gap closes.

**Handling capability gaps:** When a variant gap prevents a needed visual effect, Wardrobe designs around it with equipment — main-hand props, helmet choices, and material palette are the workaround toolkit. A Villager with a Book in main hand and golden helmet reads as scholar without requiring the LIBRARIAN profession skin. Document the designed-around decision; it should be revisited when the gap closes.

**Escalation discipline:** Wardrobe resolves equipment, appearance, and invisible-body decisions independently. Wardrobe escalates when: (1) a costume choice conflicts with how a character is written in the brief — this is a story call for the Director; (2) a visual effect requires a Java capability that isn't available — flag to Stage Management; (3) a costume change creates a timing conflict with another department's cue — coordinate through Stage Manager.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| Equipment at spawn — all 6 slots, any LivingEntity | ✅ Verified | All slots applied in `EntityEventExecutor.handleSpawn()` |
| ENTITY_EQUIP — mid-show equipment change | ✅ Verified | All 6 slots; confirmed in `EntityEventExecutor.handleEntityEquip()` |
| ENTITY_EQUIP on entity groups | ✅ Verified | Applies to all group members simultaneously |
| ENTITY_INVISIBLE — invisible body technique | ✅ Verified | INVISIBILITY potion, hide_particles=true; LivingEntity only; confirmed in `EntityEventExecutor.handleEntityInvisible()` |
| Armor Stand as display surface (costume on a wire) | ✅ Verified | Accepts all equipment; no invisibility needed; static by default |
| Non-standard helmet items (carved pumpkin, mob heads) | ✅ Verified | Bukkit `Material` enum accepts any item in the helmet slot |
| ENTITY_EQUIP on player inventory | ⚠️ Gapped | Players manage own inventory; no stop-safety; use COMMAND escape hatch only |
| `variant:` on SPAWN_ENTITY — cat, sheep, horse, wolf, parrot colors | ⚠️ Gapped | Parsed but silently ignored at runtime — filed in `ops-inbox.md` |
| `profession:` on SPAWN_ENTITY — villager professions | ⚠️ Gapped | Parsed but silently ignored at runtime — filed in `ops-inbox.md` |
| Armor Stand pose control | ⚠️ Gapped | Requires COMMAND event; outside show cleanup contract; low priority |
| Tropical fish variant YAML syntax | ⚠️ Gapped | Not yet documented; exact YAML syntax unknown; flag to Stage Manager if needed |
| Parrot perch on shoulder | 📋 Aspirational | Not implemented; would require a custom event or mount mechanic |
| Custom textures / resource pack skins | 📋 Aspirational | Vanilla Minecraft only; no resource pack integration in current plugin |
