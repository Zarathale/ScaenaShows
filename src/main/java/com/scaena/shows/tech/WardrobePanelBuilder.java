package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Builds and sends the Phase 2 Wardrobe department edit panels.
 *
 * Main panel: six equipment slots, each showing current item + action buttons.
 * Sub-panels:
 *   Item selector   — curated list by slot type
 *   Leather color   — 16 named DyeColors (auto-shown when leather item selected)
 *   Trim pattern    — curated trim patterns
 *   Trim material   — curated trim materials
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Spec: kb/system/phase2-department-panels.md §Wardrobe
 */
public final class WardrobePanelBuilder {

    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0x55FFFF); // aqua
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_EMPTY    = TextColor.color(0x555555); // dark gray
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (toggle=on)
    private static final TextColor COL_BTN_OFF  = TextColor.color(0x777777); // dim gray (toggle=off)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_ITEM_BTN = TextColor.color(0xAA88FF); // lavender (item pills)
    private static final TextColor COL_COLOR_BTN= TextColor.color(0xFF88AA); // pink (color pills)
    private static final TextColor COL_TRIM_BTN = TextColor.color(0x88DDFF); // light blue (trim pills)
    private static final TextColor COL_LEATHER  = TextColor.color(0xFFBB77); // warm orange (leather label)

    private static final String SEP = "─────────────────────────────────────────────";
    private static final int    PILLS_PER_ROW = 4;

    private WardrobePanelBuilder() {}

    // ------------------------------------------------------------------
    // Main panel
    // ------------------------------------------------------------------

    /**
     * Build and send the main wardrobe edit panel.
     *
     *   ─────────────────────────────────────────────
     *   WARDROBE  wardrobe.iron.herald
     *
     *   Helmet:      IRON_HELMET    [Change ▸]  [✦] [◈]
     *   Chestplate:  IRON_CHEST...  [Change ▸]  [✦] [◈]
     *   Leggings:    (none)         [Change ▸]
     *   Boots:       IRON_BOOTS     [Change ▸]  [✦] [◈]
     *   Main hand:   IRON_SWORD     [Change ▸]  [✦]
     *   Off hand:    TORCH          [Change ▸]  [✦]
     *   ─────────────────────────────────────────────
     */
    public static void sendPanel(Player p, WardrobeEditSession session) {
        Component sep = Component.text(SEP, COL_SEP);

        Component header = Component.text("WARDROBE", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  ", COL_SEP))
            .append(Component.text(session.cueId(), COL_HEADER));

        Component out = Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline());

        for (String slot : WardrobeEditSession.SLOT_NAMES) {
            WardrobeEditSession.SlotState state = session.getSlots().get(slot);
            out = out.append(buildSlotRow(slot, state)).append(Component.newline());

            // Leather color sub-row (only when leather item and color set)
            if (WardrobeEditSession.isLeatherArmor(state.item) && !state.leatherColor.isEmpty()) {
                out = out.append(buildLeatherColorRow(slot, state)).append(Component.newline());
            }
            // Trim sub-row (only when trim is on)
            if (state.trimmed && WardrobeEditSession.isTrimmableArmor(state.item)) {
                out = out.append(buildTrimRow(slot, state)).append(Component.newline());
            }
        }

        out = out.append(sep);
        p.sendMessage(out);
    }

    /** One slot row: "  Helmet:     IRON_HELMET    [Change ▸]  [✦] [◈]" */
    private static Component buildSlotRow(String slot, WardrobeEditSession.SlotState state) {
        String label = slotLabel(slot) + ":";
        String itemDisplay = state.item.isEmpty() ? "(none)" : state.item.replace("_", " ");
        TextColor itemCol = state.item.isEmpty() ? COL_EMPTY : COL_VALUE;

        // [Change ▸] — opens item selector sub-panel
        Component changeBtn = clickBtn("[Change ▸]",
            "/scaena tech2 editparam wardrobe_items " + slot,
            COL_BTN, "Open item selector for " + slot);

        // [✦] enchant toggle
        Component enchantBtn = enchantToggle(slot, state.enchanted);

        // [◈] trim toggle — only for armor pieces
        Component trimBtn = WardrobeEditSession.isTrimmableArmor(state.item)
            ? trimToggle(slot, state.trimmed)
            : Component.empty();

        // Leather color [⬛] indicator — only for leather with color set
        Component colorPip = (WardrobeEditSession.isLeatherArmor(state.item) && !state.item.isEmpty())
            ? clickBtn("[color]",
                "/scaena tech2 editparam wardrobe_colors " + slot,
                COL_LEATHER, "Open leather color picker")
            : Component.empty();

        return Component.text("  ")
            .append(Component.text(padRight(label, 14), COL_LABEL))
            .append(Component.text(padRight(itemDisplay, 18), itemCol))
            .append(changeBtn)
            .append(Component.text("  "))
            .append(enchantBtn)
            .append(Component.text(" "))
            .append(trimBtn)
            .append(Component.text(" "))
            .append(colorPip);
    }

    /** Sub-row showing current leather color + [Change color] link. */
    private static Component buildLeatherColorRow(String slot, WardrobeEditSession.SlotState state) {
        return Component.text("    ")
            .append(Component.text("Color: ", COL_LEATHER))
            .append(Component.text(state.leatherColor, COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[Change ▸]",
                "/scaena tech2 editparam wardrobe_colors " + slot,
                COL_LEATHER, "Open leather color picker for " + slot));
    }

    /** Sub-row showing current trim pattern + material + change links. */
    private static Component buildTrimRow(String slot, WardrobeEditSession.SlotState state) {
        return Component.text("    ")
            .append(Component.text("Trim: ", COL_TRIM_BTN))
            .append(Component.text(state.trimPattern, COL_VALUE))
            .append(Component.text(" / ", COL_LABEL))
            .append(Component.text(state.trimMaterial, COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[Pattern ▸]",
                "/scaena tech2 editparam wardrobe_trim_patterns " + slot,
                COL_TRIM_BTN, "Change trim pattern for " + slot))
            .append(Component.text("  "))
            .append(clickBtn("[Material ▸]",
                "/scaena tech2 editparam wardrobe_trim_materials " + slot,
                COL_TRIM_BTN, "Change trim material for " + slot));
    }

    /** Enchant toggle button: [✦] green when on, dim when off. */
    private static Component enchantToggle(String slot, boolean enchanted) {
        TextColor col = enchanted ? COL_BTN_ON : COL_BTN_OFF;
        String tip = enchanted ? "Enchant glow: ON — click to remove" : "Enchant glow: OFF — click to add";
        return clickBtn("[✦]", "/scaena tech2 editparam wardrobe_enchant " + slot, col, tip);
    }

    /** Trim toggle button: [◈] green when on, dim when off. */
    private static Component trimToggle(String slot, boolean trimmed) {
        TextColor col = trimmed ? COL_BTN_ON : COL_BTN_OFF;
        String tip = trimmed ? "Trim: ON — click to remove" : "Trim: OFF — click to add";
        return clickBtn("[◈]", "/scaena tech2 editparam wardrobe_trim " + slot, col, tip);
    }

    // ------------------------------------------------------------------
    // Item selector sub-panel
    // ------------------------------------------------------------------

    /**
     * Send the item selector sub-panel for the given slot.
     * Clicking any item immediately sets it.
     */
    public static void sendItemPanel(Player p, String slot) {
        List<String> items = WardrobeEditSession.itemsForSlot(slot);
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Select item for ", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text(slotLabel(slot), COL_HEADER));

        Component rows = buildPillRows(items, slot, COL_ITEM_BTN,
            item -> "/scaena tech2 editparam " + slot + " " + item,
            item -> "Set " + slot + " to " + item);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(rows)
            .append(sep));
    }

    // ------------------------------------------------------------------
    // Leather color sub-panel
    // ------------------------------------------------------------------

    public static void sendLeatherColorPanel(Player p, String slot) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Leather color for ", COL_LEATHER, TextDecoration.BOLD)
            .append(Component.text(slotLabel(slot), COL_HEADER));

        Component rows = buildPillRows(
            WardrobeEditSession.LEATHER_COLORS, slot, COL_COLOR_BTN,
            color -> "/scaena tech2 editparam wardrobe_color " + slot + " " + color,
            color -> "Set leather color to " + color);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(rows)
            .append(sep));
    }

    // ------------------------------------------------------------------
    // Trim pattern sub-panel
    // ------------------------------------------------------------------

    public static void sendTrimPatternPanel(Player p, String slot) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Trim pattern for ", COL_TRIM_BTN, TextDecoration.BOLD)
            .append(Component.text(slotLabel(slot), COL_HEADER));

        Component rows = buildPillRows(
            WardrobeEditSession.TRIM_PATTERNS, slot, COL_TRIM_BTN,
            pattern -> "/scaena tech2 editparam wardrobe_trim_pattern " + slot + " " + pattern,
            pattern -> "Set trim pattern to " + pattern);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(rows)
            .append(sep));
    }

    // ------------------------------------------------------------------
    // Trim material sub-panel
    // ------------------------------------------------------------------

    public static void sendTrimMaterialPanel(Player p, String slot) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Trim material for ", COL_TRIM_BTN, TextDecoration.BOLD)
            .append(Component.text(slotLabel(slot), COL_HEADER));

        Component rows = buildPillRows(
            WardrobeEditSession.TRIM_MATERIALS, slot, COL_TRIM_BTN,
            material -> "/scaena tech2 editparam wardrobe_trim_material " + slot + " " + material,
            material -> "Set trim material to " + material);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(rows)
            .append(sep));
    }

    // ------------------------------------------------------------------
    // Shared pill-row renderer
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface PillCommand { String get(String item); }

    private static Component buildPillRows(
        List<String> items, String slot, TextColor color,
        PillCommand cmdFn, PillCommand tipFn
    ) {
        Component out  = Component.empty();
        int count      = 0;
        Component row  = Component.text("  ");

        for (String item : items) {
            String displayLabel = item.replace("_", " ");
            Component pill = clickBtn(displayLabel, cmdFn.get(item), color, tipFn.get(item));
            row   = row.append(pill).append(Component.text("  "));
            count++;
            if (count % PILLS_PER_ROW == 0) {
                out = out.append(row).append(Component.newline());
                row = Component.text("  ");
            }
        }
        if (count % PILLS_PER_ROW != 0) {
            out = out.append(row).append(Component.newline());
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Component clickBtn(String label, String command,
                                      TextColor color, String tooltip) {
        return Component.text(label, color, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, NamedTextColor.GRAY)));
    }

    private static String slotLabel(String slot) {
        return switch (slot.toLowerCase()) {
            case "helmet"     -> "Helmet";
            case "chestplate" -> "Chestplate";
            case "leggings"   -> "Leggings";
            case "boots"      -> "Boots";
            case "main_hand"  -> "Main hand";
            case "off_hand"   -> "Off hand";
            default           -> slot;
        };
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
