package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.EntityBehaviorEvents.EntityEquipEvent;
import com.scaena.shows.model.event.EntityMgmtEvents.SpawnEntityEvent;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.*;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Wardrobe.
 *
 * Supports editing the six equipment slots of an ENTITY_EQUIP cue:
 *   helmet, chestplate, leggings, boots, main_hand, off_hand
 *
 * Extended attributes per slot:
 *   leather_color — named DyeColor (auto-shown for leather items)
 *   enchanted     — visual glow (BINDING_CURSE at level 1, tooltip hidden)
 *   trimmed       — armor trim on/off (armor pieces only)
 *   trim_pattern  — lowercase NamespacedKey (e.g. "bolt", "flow")
 *   trim_material — lowercase NamespacedKey (e.g. "emerald", "diamond")
 *
 * Live swap: entity re-equips immediately on every param change.
 *
 * Territory: Wardrobe owns equipment only. Entity type and name live in Casting.
 *
 * Spec: kb/system/phase2-department-panels.md §Wardrobe
 */
public final class WardrobeEditSession implements DeptEditSession {

    // ------------------------------------------------------------------
    // Curated item lists per slot
    // ------------------------------------------------------------------

    public static final List<String> HELMET_ITEMS = List.of(
        "LEATHER_HELMET", "IRON_HELMET", "GOLDEN_HELMET", "DIAMOND_HELMET", "NETHERITE_HELMET",
        "CARVED_PUMPKIN", "CREEPER_HEAD", "SKELETON_SKULL", "ZOMBIE_HEAD",
        "WITHER_SKELETON_SKULL", "PLAYER_HEAD", "DRAGON_HEAD",
        "(none)"
    );

    public static final List<String> CHESTPLATE_ITEMS = List.of(
        "LEATHER_CHESTPLATE", "IRON_CHESTPLATE", "GOLDEN_CHESTPLATE",
        "DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE", "ELYTRA",
        "(none)"
    );

    public static final List<String> LEGGINGS_ITEMS = List.of(
        "LEATHER_LEGGINGS", "IRON_LEGGINGS", "GOLDEN_LEGGINGS",
        "DIAMOND_LEGGINGS", "NETHERITE_LEGGINGS",
        "(none)"
    );

    public static final List<String> BOOTS_ITEMS = List.of(
        "LEATHER_BOOTS", "IRON_BOOTS", "GOLDEN_BOOTS",
        "DIAMOND_BOOTS", "NETHERITE_BOOTS",
        "(none)"
    );

    public static final List<String> MAIN_HAND_ITEMS = List.of(
        // Swords
        "WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD",
        // Axes
        "IRON_AXE", "DIAMOND_AXE",
        // Tools
        "IRON_PICKAXE", "IRON_SHOVEL", "IRON_HOE",
        // Ranged
        "BOW", "CROSSBOW", "TRIDENT",
        // Light
        "TORCH", "LANTERN", "SOUL_TORCH", "SOUL_LANTERN",
        // Objects
        "STICK", "BOOK", "WRITTEN_BOOK", "ENDER_PEARL",
        "BANNER", "DEAD_BUSH", "APPLE", "EGG", "SNOWBALL", "SHIELD",
        "(none)"
    );

    public static final List<String> OFF_HAND_ITEMS = List.of(
        "TORCH", "LANTERN", "SOUL_TORCH", "SOUL_LANTERN",
        "SHIELD", "BOOK", "ENDER_PEARL", "APPLE",
        "ARROW", "TOTEM_OF_UNDYING",
        "(none)"
    );

    /** Returns the curated item list for the given slot name. */
    public static List<String> itemsForSlot(String slot) {
        return switch (slot.toLowerCase()) {
            case "helmet"      -> HELMET_ITEMS;
            case "chestplate"  -> CHESTPLATE_ITEMS;
            case "leggings"    -> LEGGINGS_ITEMS;
            case "boots"       -> BOOTS_ITEMS;
            case "main_hand"   -> MAIN_HAND_ITEMS;
            case "off_hand"    -> OFF_HAND_ITEMS;
            default            -> List.of();
        };
    }

    // ------------------------------------------------------------------
    // Curated leather colors (16 DyeColor names)
    // ------------------------------------------------------------------

    public static final List<String> LEATHER_COLORS = List.of(
        "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME",
        "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE",
        "BROWN", "GREEN", "RED", "BLACK"
    );

    // ------------------------------------------------------------------
    // Curated trim patterns
    // ------------------------------------------------------------------

    public static final List<String> TRIM_PATTERNS = List.of(
        "bolt", "coast", "dune", "eye", "flow", "host",
        "raiser", "rib", "sentry", "shaper", "silence", "snout",
        "spire", "tide", "vex", "ward", "wayfinder", "wild"
    );

    // ------------------------------------------------------------------
    // Curated trim materials
    // ------------------------------------------------------------------

    public static final List<String> TRIM_MATERIALS = List.of(
        "emerald", "diamond", "gold", "iron", "lapis_lazuli",
        "amethyst", "copper", "quartz", "netherite", "resin"
    );

    // ------------------------------------------------------------------
    // Slot names (ordered for panel display)
    // ------------------------------------------------------------------

    public static final List<String> SLOT_NAMES = List.of(
        "helmet", "chestplate", "leggings", "boots", "main_hand", "off_hand"
    );

    // ------------------------------------------------------------------
    // SlotState — per-slot mutable equipment state
    // ------------------------------------------------------------------

    public static final class SlotState {
        /** Material name, or "" for empty. "(none)" is normalised to "" on set. */
        public String item;
        /** Named DyeColor for leather items, or "" if not leather. */
        public String leatherColor;
        /** Visual enchant glow (BINDING_CURSE hidden). */
        public boolean enchanted;
        /** Armor trim on/off (armor pieces only). */
        public boolean trimmed;
        public String trimPattern;   // lowercase NamespacedKey, e.g. "flow"
        public String trimMaterial;  // lowercase NamespacedKey, e.g. "emerald"

        public SlotState(String item) {
            this.item         = normaliseItem(item);
            this.leatherColor = "";
            this.enchanted    = false;
            this.trimmed      = false;
            this.trimPattern  = "flow";
            this.trimMaterial = "emerald";
        }

        public SlotState copy() {
            SlotState c = new SlotState(item);
            c.leatherColor = leatherColor;
            c.enchanted    = enchanted;
            c.trimmed      = trimmed;
            c.trimPattern  = trimPattern;
            c.trimMaterial = trimMaterial;
            return c;
        }

        private static String normaliseItem(String raw) {
            if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("(none)")) return "";
            return raw.toUpperCase();
        }
    }

    // ------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------

    private final String         cueId;
    private final Player         player;
    private final TechCueSession cueSession;
    private final ShowYamlEditor editor;
    private final CueRegistry    cueRegistry;
    private final Logger         log;

    /** Target entity selector (e.g. "entity:spawned:Herald"). */
    private final String entityTarget;

    /** Entry snapshots for Cancel. */
    private final Map<String, SlotState> entrySlots;

    /** Current mutable slot states. */
    private final Map<String, SlotState> slots;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public WardrobeEditSession(
        String         cueId,
        Player         player,
        TechCueSession cueSession,
        ShowYamlEditor editor,
        CueRegistry    cueRegistry,
        Logger         log
    ) {
        this.cueId       = cueId;
        this.player      = player;
        this.cueSession  = cueSession;
        this.editor      = editor;
        this.cueRegistry = cueRegistry;
        this.log         = log;

        // Read initial state from the cue registry
        String[] init = readInitialState(cueId);
        // init[0] = target, [1]=helmet, [2]=chestplate, [3]=leggings, [4]=boots, [5]=main_hand, [6]=off_hand
        this.entityTarget = init[0];

        this.slots = new LinkedHashMap<>();
        for (int i = 0; i < SLOT_NAMES.size(); i++) {
            slots.put(SLOT_NAMES.get(i), new SlotState(init[i + 1]));
        }

        // Snapshot for Cancel
        this.entrySlots = new LinkedHashMap<>();
        for (String slot : SLOT_NAMES) {
            entrySlots.put(slot, slots.get(slot).copy());
        }
    }

    // ------------------------------------------------------------------
    // DeptEditSession contract
    // ------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "wardrobe"; }

    @Override
    public void onSave() {
        writeToEditor();
    }

    @Override
    public void onSaveAsPreset() {
        writeToEditor();
        // Preset naming: wardrobe.[primary_material].[slug]
        String primary = primaryMaterial();
        String slug = cueId.contains(".")
            ? cueId.substring(cueId.lastIndexOf('.') + 1)
            : cueId;
        String presetId = "wardrobe." + primary + "." + slug;
        editor.saveAsPreset(cueId, presetId);
    }

    @Override
    public void onCancel() {
        // Restore entry state via live swap — rawYaml was never modified
        for (String slot : SLOT_NAMES) {
            slots.put(slot, entrySlots.get(slot).copy());
        }
        applyLiveSwap();
    }

    /**
     * Handle /scaena tech2 editparam <key> <value>.
     *
     * Keys handled:
     *   wardrobe_items <slot>                  — send item selector sub-panel
     *   wardrobe_colors <slot>                 — send leather color sub-panel
     *   wardrobe_trim_patterns <slot>          — send trim pattern sub-panel
     *   wardrobe_trim_materials <slot>         — send trim material sub-panel
     *   <slot> <ITEM>                          — set item for slot
     *   wardrobe_color <slot> <COLOR>          — set leather color for slot
     *   wardrobe_enchant <slot>                — toggle enchanted for slot
     *   wardrobe_trim <slot>                   — toggle trim for slot
     *   wardrobe_trim_pattern <slot> <pattern> — set trim pattern for slot
     *   wardrobe_trim_material <slot> <mat>    — set trim material for slot
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        // Sub-panel navigation (no state change)
        if (lower.equals("wardrobe_items")) {
            if (isValidSlot(value)) {
                WardrobePanelBuilder.sendItemPanel(player, value.toLowerCase());
                return true;
            }
            return false;
        }
        if (lower.equals("wardrobe_colors")) {
            if (isValidSlot(value)) {
                WardrobePanelBuilder.sendLeatherColorPanel(player, value.toLowerCase());
                return true;
            }
            return false;
        }
        if (lower.equals("wardrobe_trim_patterns")) {
            if (isValidSlot(value)) {
                WardrobePanelBuilder.sendTrimPatternPanel(player, value.toLowerCase());
                return true;
            }
            return false;
        }
        if (lower.equals("wardrobe_trim_materials")) {
            if (isValidSlot(value)) {
                WardrobePanelBuilder.sendTrimMaterialPanel(player, value.toLowerCase());
                return true;
            }
            return false;
        }

        // Set item for a slot: key=<slot>, value=<ITEM>
        if (isValidSlot(lower) && !value.isBlank()) {
            SlotState state = slots.get(lower);
            if (state == null) return false;
            String newItem = value.equalsIgnoreCase("(none)") ? "" : value.toUpperCase();
            if (!newItem.isEmpty()) {
                try { Material.valueOf(newItem); }
                catch (IllegalArgumentException e) {
                    player.sendMessage(
                        net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Unknown material: " + value + "</red>"));
                    return true;
                }
            }
            state.item = newItem;
            // Auto-clear leather color if item is no longer leather
            if (!isLeatherArmor(newItem)) state.leatherColor = "";
            // Auto-clear trim if item is not trimmable armor
            if (!isTrimmableArmor(newItem)) state.trimmed = false;
            applyLiveSwap();
            WardrobePanelBuilder.sendPanel(player, this);
            return true;
        }

        // wardrobe_color <slot> <COLOR>
        if (lower.equals("wardrobe_color")) {
            // value = "<slot> <COLOR>"
            String[] parts = value.split("\\s+", 2);
            if (parts.length < 2) return false;
            String slot  = parts[0].toLowerCase();
            String color = parts[1].toUpperCase();
            SlotState state = slots.get(slot);
            if (state == null || !isValidSlot(slot)) return false;
            if (!LEATHER_COLORS.contains(color)) {
                player.sendMessage(
                    net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>Unknown color: " + color + "</red>"));
                return true;
            }
            state.leatherColor = color;
            applyLiveSwap();
            WardrobePanelBuilder.sendPanel(player, this);
            return true;
        }

        // wardrobe_enchant <slot>
        if (lower.equals("wardrobe_enchant")) {
            String slot = value.toLowerCase().trim();
            SlotState state = slots.get(slot);
            if (state == null || !isValidSlot(slot)) return false;
            state.enchanted = !state.enchanted;
            applyLiveSwap();
            WardrobePanelBuilder.sendPanel(player, this);
            return true;
        }

        // wardrobe_trim <slot>
        if (lower.equals("wardrobe_trim")) {
            String slot = value.toLowerCase().trim();
            SlotState state = slots.get(slot);
            if (state == null || !isValidSlot(slot)) return false;
            if (!isTrimmableArmor(state.item)) return true; // no-op silently
            state.trimmed = !state.trimmed;
            applyLiveSwap();
            WardrobePanelBuilder.sendPanel(player, this);
            return true;
        }

        // wardrobe_trim_pattern <slot> <pattern>
        if (lower.equals("wardrobe_trim_pattern")) {
            String[] parts = value.split("\\s+", 2);
            if (parts.length < 2) return false;
            String slot    = parts[0].toLowerCase();
            String pattern = parts[1].toLowerCase();
            SlotState state = slots.get(slot);
            if (state == null || !isValidSlot(slot)) return false;
            if (!TRIM_PATTERNS.contains(pattern)) {
                player.sendMessage(
                    net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>Unknown trim pattern: " + pattern + "</red>"));
                return true;
            }
            state.trimPattern = pattern;
            applyLiveSwap();
            WardrobePanelBuilder.sendPanel(player, this);
            return true;
        }

        // wardrobe_trim_material <slot> <material>
        if (lower.equals("wardrobe_trim_material")) {
            String[] parts = value.split("\\s+", 2);
            if (parts.length < 2) return false;
            String slot     = parts[0].toLowerCase();
            String material = parts[1].toLowerCase();
            SlotState state = slots.get(slot);
            if (state == null || !isValidSlot(slot)) return false;
            if (!TRIM_MATERIALS.contains(material)) {
                player.sendMessage(
                    net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>Unknown trim material: " + material + "</red>"));
                return true;
            }
            state.trimMaterial = material;
            applyLiveSwap();
            WardrobePanelBuilder.sendPanel(player, this);
            return true;
        }

        return false;
    }

    // ------------------------------------------------------------------
    // Accessors (for WardrobePanelBuilder)
    // ------------------------------------------------------------------

    public Map<String, SlotState> getSlots()         { return slots; }
    public String                 getEntityTarget()  { return entityTarget; }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    /**
     * Write current slot state as an ENTITY_EQUIP event map into the editor override.
     * Extended fields (leather_color, enchanted, trim_*) are written alongside for
     * future executor support; they do not affect current playback.
     */
    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        eventMap.put("at", 0);
        eventMap.put("type", "ENTITY_EQUIP");
        eventMap.put("target", entityTarget);

        for (String slotName : SLOT_NAMES) {
            SlotState state = slots.get(slotName);
            // Write base material (or omit for empty)
            if (!state.item.isEmpty()) {
                eventMap.put(slotName, state.item);
            }
            // Write extended fields only when non-default
            if (!state.leatherColor.isEmpty()) {
                eventMap.put(slotName + "_leather_color", state.leatherColor);
            }
            if (state.enchanted) {
                eventMap.put(slotName + "_enchanted", true);
            }
            if (state.trimmed) {
                eventMap.put(slotName + "_trim_pattern",  state.trimPattern);
                eventMap.put(slotName + "_trim_material", state.trimMaterial);
            }
        }

        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[WardrobeEdit] Saved cue override for " + cueId);
    }

    /** Live swap: re-equip the target entity with current slot state. No-op if no preview. */
    private void applyLiveSwap() {
        if (!cueSession.isPreviewActive()) return;
        RunningShow previewShow = cueSession.getPreviewShow();
        if (previewShow == null) return;

        Entity entity = resolveEntity(previewShow, entityTarget);
        if (!(entity instanceof LivingEntity living)) return;

        EntityEquipment eq = living.getEquipment();
        if (eq == null) return;

        eq.setHelmet(    buildItemStack("helmet"));
        eq.setChestplate(buildItemStack("chestplate"));
        eq.setLeggings(  buildItemStack("leggings"));
        eq.setBoots(     buildItemStack("boots"));
        eq.setItemInMainHand(buildItemStack("main_hand"));
        eq.setItemInOffHand( buildItemStack("off_hand"));
    }

    /**
     * Build an ItemStack for the given slot from current state.
     * Returns null (Air) for empty slots.
     */
    private ItemStack buildItemStack(String slot) {
        SlotState state = slots.get(slot);
        if (state == null || state.item.isEmpty()) return null;

        Material mat;
        try { mat = Material.valueOf(state.item); }
        catch (IllegalArgumentException e) { return null; }

        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        // Leather color
        if (!state.leatherColor.isEmpty() && meta instanceof LeatherArmorMeta leatherMeta) {
            DyeColor dyeColor;
            try { dyeColor = DyeColor.valueOf(state.leatherColor); }
            catch (IllegalArgumentException e) { dyeColor = DyeColor.WHITE; }
            leatherMeta.setColor(dyeColor.getColor());
        }

        // Enchant glow (visual only — binding_curse hidden from tooltip)
        if (state.enchanted) {
            Enchantment curse = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("binding_curse"));
            if (curse != null) meta.addEnchant(curse, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Armor trim
        if (state.trimmed && meta instanceof ArmorMeta armorMeta) {
            TrimPattern pattern  = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(state.trimPattern));
            TrimMaterial material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(state.trimMaterial));
            if (pattern != null && material != null) {
                armorMeta.setTrim(new ArmorTrim(material, pattern));
            }
        }

        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Resolve entity target string to a world entity.
     * Supports: entity:spawned:<name>, entity:world:<name>
     */
    private Entity resolveEntity(RunningShow show, String target) {
        if (target == null || target.isBlank()) return null;
        if (target.startsWith("entity:spawned:")) {
            String name = target.substring("entity:spawned:".length());
            return show.getSpawnedEntity(name);
        }
        if (target.startsWith("entity:world:")) {
            String name = target.substring("entity:world:".length());
            // Scan all entities in the anchor's world
            for (Entity e : show.getAnchorLocation().getWorld().getEntities()) {
                if (name.equals(e.getCustomName())) return e;
            }
        }
        return null;
    }

    /**
     * Read initial equipment state from the cue registry.
     * Returns String[7]: [target, helmet, chestplate, leggings, boots, main_hand, off_hand]
     */
    private String[] readInitialState(String cueId) {
        String[] result = new String[]{"", "", "", "", "", "", ""};
        Cue cue = cueRegistry.get(cueId);
        if (cue == null) return result;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof EntityEquipEvent eq) {
                result[0] = eq.target;
                result[1] = eq.helmet;
                result[2] = eq.chestplate;
                result[3] = eq.leggings;
                result[4] = eq.boots;
                result[5] = eq.mainHand;
                result[6] = eq.offHand;
                return result;
            }
            // Fallback: SPAWN_ENTITY equipment block
            if (event instanceof SpawnEntityEvent spawn) {
                result[0] = "entity:spawned:" + spawn.name;
                result[1] = spawn.helmetItem;
                result[2] = spawn.chestplateItem;
                result[3] = spawn.leggingsItem;
                result[4] = spawn.bootsItem;
                result[5] = spawn.mainHandItem;
                result[6] = spawn.offHandItem;
                return result;
            }
        }
        return result;
    }

    /** Determine primary material tier for preset naming. */
    private String primaryMaterial() {
        // Scan all slots for the highest-tier material
        String[] tiers = {"NETHERITE", "DIAMOND", "GOLDEN", "GOLD", "IRON", "LEATHER"};
        for (String tier : tiers) {
            for (String slot : SLOT_NAMES) {
                String item = slots.get(slot).item;
                if (item.contains(tier)) return tier.toLowerCase();
            }
        }
        return "misc";
    }

    private static boolean isValidSlot(String slot) {
        return SLOT_NAMES.contains(slot.toLowerCase());
    }

    /** True for leather armor pieces (support color). */
    public static boolean isLeatherArmor(String item) {
        return item.startsWith("LEATHER_HELMET")
            || item.startsWith("LEATHER_CHESTPLATE")
            || item.startsWith("LEATHER_LEGGINGS")
            || item.startsWith("LEATHER_BOOTS");
    }

    /** True for armor pieces that support trims. */
    public static boolean isTrimmableArmor(String item) {
        return item.endsWith("_HELMET")
            || item.endsWith("_CHESTPLATE")
            || item.endsWith("_LEGGINGS")
            || item.endsWith("_BOOTS");
    }
}
