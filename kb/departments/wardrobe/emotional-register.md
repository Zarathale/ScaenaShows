---
department: Wardrobe & Properties Director
created: 2026-03-25
notes: Maps the Wardrobe department's palette to emotional demands in shows.
---

# Wardrobe & Properties — Emotional Register

What does this department bring to each emotional arc? Appearance is the first language of theatre — what a performer wears is read before they move or speak. This document maps the visual tools available to the Wardrobe department against the emotional demands a show can make.

---

## Arrival / Announcement

**What the department contributes:**

- **Herald/Authority through uniform.** Equip a Villager with a full armor set (helmet, chestplate, leggings, boots) in a single material to establish official presence. IRON reads as guard or officer. GOLD reads as royalty or ceremony. Apply at spawn.
- **Role clarity through profession silhouettes** (once the Java gap is resolved). A LIBRARIAN (spectacles) entering mid-show reads as knowledge-bearer. A CLERIC (purple robes) reads as ritual authority. A CARTOGRAPHER (monocle) reads as navigator or recorder. Each profession has an instant visual register.
- **Main-hand props as declaration.** A sword = threat or protection. A torch = guide or illumination. A banner = herald or announcement. A book = scholarship. Apply at spawn or via ENTITY_EQUIP mid-show.
- **Light from the off-hand torch.** A torch in off-hand creates ambient glow without taking up the main-hand storytelling slot. Useful for a figure who arrives bearing both a tool and warmth.
- **Silhouette shift via headgear.** A carved pumpkin helmet changes the entire read of a Villager — it becomes unsettling, or agricultural, or mysterious. A mob head (e.g., a creeper head on a Villager) is surreal and memorable.
- **Armor Stands as sentries.** Spawn multiple Armor Stands in full armor at key locations. They read as statues, sentinels, or the "absent guardians" of a space. Static, formal, establishing authority through presence.

**Key mechanics:**
- Equipment is set at spawn via `equipment:` in SPAWN_ENTITY. To change appearance mid-show, use ENTITY_EQUIP to swap armor or main-hand props.
- All equipment slots (helmet, chestplate, leggings, boots, main hand, off hand) apply simultaneously — no stagger between slots.
- Equipment works on any LivingEntity (Villagers, Armor Stands, Zombies, Skeletons, etc.). It also works on tamed animals (Wolves, Cats, Horses) — though variants (professions) are currently non-functional.

---

## Grief / Loss

**What the department contributes:**

- **Disembodied armor.** Apply ENTITY_INVISIBLE to an Armor Stand or entity equipped with a full suit of armor. The body vanishes; the armor remains, floating in space like an absence. This is the visual language of the missing person, the fallen soldier, the memorial.
- **Grief-worn appearance via color absence.** Once variants are live, a SNOW Villager in a warm biome reads as wrong, displaced, out of place. A black or dark-gray sheep (via variant) among white ones reads as sorrow or isolation.
- **Dark/heavy equipment palette.** Iron armor reads heavier than diamond. Black wool (on a sheep) reads darker than white. Dark colors in equipment signal weight and loss.
- **Floating objects as relics.** Spawn an entity (e.g., a Zombie or Skeleton), equip it with a single item (a torch, a book, a banner), apply ENTITY_INVISIBLE to the body, and let the item float. A torch floating in darkness reads as memory. A book reads as lost knowledge.
- **Still, silent presence via Armor Stands.** Unlike mobs, Armor Stands don't move unless commanded. A circle of Armor Stands in armor reads as a memorial, a gathering of witnesses, or the weight of the absence.

**Key mechanics:**
- ENTITY_INVISIBLE applies an invisibility effect with hidden particles. The entity's body disappears; all equipped items (armor, main hand, off hand) remain visible.
- Invisibility is duration-based. Set `duration_ticks` high (e.g., 9999) to maintain invisibility for a full show.
- Armor Stands have no body in the traditional sense — ENTITY_INVISIBLE doesn't apply meaningfully to them. Use them as pure display surfaces via equipment alone.

---

## Joy / Celebration

**What the department contributes:**

- **Festive uniform.** Full armor in bright materials (GOLD, DIAMOND, EMERALD if those slots accept them — check with Stage Manager on exact item availability). A golden Villager reads as celebratory, noble, or precious.
- **Colorful variants.** Once the variant gap is resolved: a mixed flock of colored sheep (all 16 dye colors), each with a different color variant. A Villager in tropical garb (JUNGLE biome variant) reads as exotic and celebratory. Multiple cat variants (SIAMESE, CALICO, JELLIE, ALL_BLACK) mixed in a scene reads as abundance or variety.
- **Prop-forward appearance.** Equip entities with bright or joyful props: flowers (if available as items), banners (which are colorful), torches (warm light). A Villager holding a banner in main hand reads as festivities or parade.
- **Visible light sources.** Off-hand torch on a celebratory figure adds warmth and visible joy — the light is both literal and emotional.
- **Color contrast.** Pale or white equipment (e.g., wool or leather armor) against darker backgrounds reads brightly and joyfully. Dark equipment against light backgrounds reads heavier.

**Key mechanics:**
- Equipment colors and materials communicate tone before any movement or dialogue.
- Variants add enormous visual complexity — prepare variant selections early in the brief so Wardrobe can plan the palette.
- Mixed-variant groups (e.g., colored sheep) are powerful for abundance; uniform variants (all the same) are powerful for unity.

---

## Tension / Dread

**What the department contributes:**

- **Threat through weaponry.** Main-hand sword or axe on an entity reads as danger or menace. A Skeleton or Zombie equipped with a sword reads as active threat. A Villager with an axe reads as unsettling — the ordinary made threatening.
- **Armor that suggests violence.** Full armor in dark materials (IRON, darker leather if available) reads as military or dangerous. A Skeleton in full armor reads as something worse than a skeleton.
- **Occluded or hidden face.** A helmet that obscures features (e.g., a carved pumpkin on a Villager) makes the character less readable and more unsettling. A mob head as a helmet is deeply eerie.
- **Floating weapons.** Spawn an entity, equip it with a sword or axe, apply ENTITY_INVISIBLE. A floating sword in darkness is a threat with no bearer — pure menace.
- **Color void via invisible body.** Apply ENTITY_INVISIBLE to a Zombie, Skeleton, or other hostile mob equipped with weaponry. The body vanishes but the threat remains visible in the floating weapon.
- **Asymmetry and incompleteness.** Equip a figure with armor on one side but not the other. Equip one entity fully, leave another bare. Asymmetry reads as wrong and unsettling.

**Key mechanics:**
- Weapons and armor must be applied before the tension moment. Use SPAWN_ENTITY to establish threat at the start, or use ENTITY_EQUIP mid-show to transform a neutral figure into a threatening one.
- Invisible bodies with visible weapons are one of Wardrobe's most powerful horror tools.
- Dark color palettes dominate dread — darker armor, darker item colors, darkness in the scene itself.

---

## Wonder / Awe

**What the department contributes:**

- **Ethereal or unusual appearance.** Once variants are live: parrot on a figure's shoulder (RED, BLUE, GREEN, CYAN, or GRAY) reads immediately as wonder, exploration, or otherworldliness. A multicolored flock of parrots is pure fantasia.
- **Delicate or precious equipment.** Light-colored armor (DIAMOND, GOLD, lighter materials) reads as precious and wondrous. A figure in all gold reads as celestial.
- **Floating items as objects of fascination.** A torch floating alone in darkness reads as mystery. A book floating at eye height reads as knowledge or revelation. A banner floating without bearer reads as signal or prophecy.
- **Still, composed figures.** Armor Stands in formal poses (once pose control is available) read as dignity and awe. An Armor Stand in full armor, standing still and composed, reads as majesty or the presence of something greater.
- **Silhouette majesty.** A tall entity (e.g., a Wither, Ender Dragon, Enderman — check Casting for what's available) equipped with shimmering or light-catching armor reads as awe-inspiring.
- **Transparency and lightness.** Once ENTITY_INVISIBLE is used: a figure whose body is invisible but whose armor and weapons float around them reads as ghostly, divine, or transcendent. Armor without body is one of Wardrobe's most effective awe tools.

**Key mechanics:**
- Wonder benefits from *contrast* — the unusual standing out against the ordinary. A single colored sheep among white ones. A single parrot in a scene of normal mobs. A floating item where none was before.
- Light colors and light sources contribute to awe. Darkness and mystery contribute to wonder-tinged dread.

---

## Intimacy / Quiet

**What the department contributes:**

- **Simplicity and modesty.** A single Villager with minimal equipment — just a name, no armor, no fancy props. A figure with only a torch (off-hand) and nothing else. Lack of ornamentation reads as vulnerability and intimacy.
- **Worn appearance.** Dark or weathered equipment reads as lived-in. An old sword (if age can be expressed via material choice). Leather armor instead of plate. The *unpretentious* reads as intimate.
- **Familiar animals with variants.** A single cat (via variant: TABBY or CALICO) sitting quiet reads as companionship and domesticity. A wolf (pale variant or dark variant) watching silently reads as devotion and intimacy.
- **Armor Stands as mirrors or confessionals.** A single Armor Stand, unmoving, with minimal equipment. It reads as something to speak to, a witness, a presence in the room with you.
- **Off-hand torch as the only light.** A figure bearing only an off-hand torch — no armor, no main-hand prop, no flash. The torch is intimate, personal, a light you carry into darkness.
- **Invisible body, visible held item.** Apply ENTITY_INVISIBLE to a figure holding only a single meaningful object — a torch, a book, a small weapon. The absence of the body reads as spirit or memory; the held object is what remains.

**Key mechanics:**
- Intimacy benefits from *restraint* — less equipment, fewer colors, minimal visual noise.
- A figure with a single held item reads more intimately than a figure in full armor.
- Quiet figures should have minimal equipment at spawn; no need for mid-show changes unless transformation is the point.

---

## Transition

**What the department contributes:**

- **Costume change via ENTITY_EQUIP.** A character swaps armor or main-hand prop mid-show to signal transformation. From rags to crown. From peasant to soldier. From civilian to ritual authority. Plan these changes as narrative beats, not as technical updates.
- **Color shift.** Swap from dark armor to light armor. From bare to fully equipped. Color change reads as emotional shift.
- **Prop swap as role change.** A Villager holding a sword becomes a soldier. A Villager holding a hoe becomes a farmer. The single main-hand prop carries enormous narrative weight.
- **Visibility shift.** Apply ENTITY_INVISIBLE mid-show to transition a figure from present to ghostly, real to memory, embodied to transcendent. Remove invisibility (via a second ENTITY_INVISIBLE event with duration 0, or via re-spawn) to transition back.
- **Armor Stand static pose as resting point.** Position an Armor Stand as a "station" the show transitions through — the character stands still, fully armed, between movements.

**Key mechanics:**
- ENTITY_EQUIP works on named entities and entity groups. Multiple entities can swap equipment simultaneously.
- Plan transition moments as major scene beats. Don't sprinkle small wardrobe changes throughout; use them to mark emotional turning points.
- Visibility transitions (invisibility on/off) are powerful — use them sparingly and with narrative intent.

---

## What This Department Cannot Contribute

**Limitations:**

- **Animated armor or moving limbs.** Armor Stands can be posed, but pose control currently requires COMMAND, which is outside show cleanup contract. Dynamic limb animation isn't available.
- **Mob variants are currently non-functional.** The `variant:` and `profession:` fields on SPAWN_ENTITY are parsed but ignored at runtime (Java gap). Workaround: differentiate visually via equipment (a Villager with spectacles and a book reads as scholar even without the LIBRARIAN profession).
- **Subtle skin tone differences.** Biome variants (SNOW, DESERT, etc.) are parsed but not applied. Once the gap is fixed, Wardrobe will gain enormous control over visual register, but for now, use equipment and profession as workarounds.
- **No custom textures or skins.** The plugin works with vanilla Minecraft mobs only. If a show needs a completely custom appearance (e.g., branded outfit, custom face), that's outside current capabilities. Flag with Stage Manager.
- **No retroactive equipment changes to spawned players.** ENTITY_EQUIP doesn't work on the player character — only on mobs and entities. Use COMMAND escape hatch if inventory modification is needed (not covered by show cleanup).
- **Limited inventory control.** You can't reach into a player's inventory to swap items. Equipment on the player character is their responsibility, not Wardrobe's.

---

## Cue Library Opportunities

The following emotional registers are **underserved in the current cue library** and should be candidate areas for new Wardrobe-focused cues:

1. **Appearance transformation sequences.** A cue family (e.g., `wardrobe.reveal.*`) that bundles ENTITY_INVISIBLE, ENTITY_EQUIP, and text messaging to create narrative wardrobe moments.
2. **Floating object aesthetics.** Reusable patterns for spawning invisible-bodied entities with held items (floating torches, floating books, floating weapons).
3. **Armor Stand choreography.** Static formations of Armor Stands used as stage pictures — memorial circles, formal processions, throne-room setups.
4. **Mob variant palettes.** Once the variant gap is resolved, cue families organized by emotional tone (e.g., `appearance.villager.authority`, `appearance.animals.festive`, `appearance.animals.melancholic`).

---

## Notes for Show Directors

When briefing Wardrobe:

- **Decide mob identity early.** Which creatures are in this show, and what role do they play? Villagers? Armor Stands? Tamed animals? This shapes everything downstream.
- **Plan main-hand props deliberately.** What does each character *hold*? This is Wardrobe's most immediate storytelling tool.
- **Use variants strategically.** Once the gap is fixed, variant choices (profession, biome, color) become major creative decisions. Group them by emotional tone, not by random variety.
- **Flag invisibility moments.** If the show uses ENTITY_INVISIBLE (floating objects, ghostly presences), brief Wardrobe early. Plan what disappears and what remains visible.
- **Costume changes are scene beats.** Don't sprinkle ENTITY_EQUIP throughout the timeline casually. Each costume change should mark a narrative turning point. Plan them with the Show Director and other departments.
