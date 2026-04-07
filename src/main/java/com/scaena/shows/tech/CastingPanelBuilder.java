package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/**
 * Builds and sends the Phase 2 Casting department edit panel.
 *
 * Panel fields:
 *   Entity type — clickable [Change ▸] → entity type sub-panel
 *   Name        — current value + [Rename ▸] → instructs player to type command
 *   Baby        — current state + [Toggle]
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Spec: kb/system/phase2-department-panels.md §Casting
 */
public final class CastingPanelBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0x55FFFF); // aqua
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (baby=true)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_TYPE_BTN = TextColor.color(0xAA88FF); // lavender (type pills)

    private static final String SEP = "─────────────────────────────────────────────";

    // Type list column layout: 4 per row
    private static final int TYPES_PER_ROW = 4;

    private CastingPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Main panel
    // -----------------------------------------------------------------------

    /**
     * Build and send the casting edit panel for the active session.
     *
     * ─────────────────────────────────────────────
     * CASTING  casting.zombie.warrior_enter
     *
     *   Entity type:  ZOMBIE    [Change ▸]
     *   Name:         warrior_enter    [Rename ▸]
     *   Baby:         no    [Toggle]
     * ─────────────────────────────────────────────
     */
    public static void sendPanel(Player p, CastingEditSession session) {
        Component sep = Component.text(SEP, COL_SEP);

        // Header
        Component header = Component.text("CASTING", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  ", COL_SEP))
            .append(Component.text(session.cueId(), COL_HEADER));

        // entity_type row — [Change ▸] opens the type selector sub-panel
        Component entityTypeRow = buildFieldRow(
            "Entity type:",
            session.getCurrentEntityType(),
            clickBtn("[Change ▸]",
                "/scaena tech2 editparam entity_type_panel open",
                COL_BTN, "Click to open entity type selector")
        );

        // name row
        String nameDisplay = session.getCurrentName().isEmpty() ? "(none)" : session.getCurrentName();
        Component nameRow = buildFieldRow(
            "Name:",
            nameDisplay,
            clickBtn("[Rename ▸]",
                "/scaena tech2 editparam name ",
                COL_BTN, "Type: /scaena tech2 editparam name <value>")
        );

        // baby row
        String babyDisplay = session.isCurrentBaby() ? "yes" : "no";
        TextColor babyBtnCol = session.isCurrentBaby() ? COL_BTN_ON : COL_BTN;
        Component babyRow = buildFieldRow(
            "Baby:",
            babyDisplay,
            clickBtn("[Toggle]",
                "/scaena tech2 editparam baby " + (!session.isCurrentBaby()),
                babyBtnCol, "Toggle baby variant")
        );

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(entityTypeRow).append(Component.newline())
            .append(nameRow).append(Component.newline())
            .append(babyRow).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Entity type sub-panel
    // -----------------------------------------------------------------------

    /**
     * Send the entity type selector sub-panel.
     * Clicking any type immediately runs /scaena tech2 editparam entity_type TYPE.
     *
     * Grouped by dramatic register with a short label.
     */
    public static void sendEntityTypePanel(Player p) {
        Component sep = Component.text(SEP, COL_SEP);
        Component header = Component.text("Select entity type:", COL_DEPT, TextDecoration.BOLD);

        // Build rows of type pills
        Component rows = buildTypeRows();

        Component hint = Component.text(
            "  Or type: /scaena tech2 editparam entity_type <TYPE>", COL_HINT);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(rows)
            .append(hint).append(Component.newline())
            .append(sep));
    }

    private static Component buildTypeRows() {
        Component out = Component.empty();
        int count = 0;
        Component row = Component.text("  ");

        for (String type : CastingEditSession.ENTITY_TYPE_LIST) {
            String label = type.replace("_", " ");
            Component pill = clickBtn(label,
                "/scaena tech2 editparam entity_type " + type,
                COL_TYPE_BTN,
                "Set entity type to " + type);

            row = row.append(pill).append(Component.text("  "));
            count++;

            if (count % TYPES_PER_ROW == 0) {
                out = out.append(row).append(Component.newline());
                row = Component.text("  ");
            }
        }
        // flush remaining partial row
        if (count % TYPES_PER_ROW != 0) {
            out = out.append(row).append(Component.newline());
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Component buildFieldRow(String label, String value, Component actionBtn) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(value, COL_VALUE))
            .append(Component.text("    "))
            .append(actionBtn);
    }

    private static Component clickBtn(String label, String command,
                                       TextColor color, String tooltip) {
        return Component.text(label, color, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, NamedTextColor.GRAY)));
    }
}
