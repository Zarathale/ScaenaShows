package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Builds and sends the Phase 2 Set department edit panel.
 *
 * Set is an in-world edit session: the player builds freely and uses
 * [Save] / [Save as Preset] / [Cancel] to commit or discard.
 * There are no param-change buttons — all interaction is in-world.
 *
 * Panel shows:
 *   - Cue ID and slug
 *   - Scene context
 *   - Edit mode instructions ("build freely — [Save] when done")
 *   - Live block change count (updates each time the panel is re-sent)
 *   - Save / Save as Preset / Cancel buttons
 *
 * Spec: kb/system/phase2-department-panels.md §Set
 */
public final class SetPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER  = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT    = TextColor.color(0xCC9966); // tan/adobe (Set)
    private static final TextColor COL_LABEL   = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE   = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN     = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON  = TextColor.color(0x55FF55); // green
    private static final TextColor COL_BTN_OFF = TextColor.color(0xFF5555); // red
    private static final TextColor COL_SEP     = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT    = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_COUNT   = TextColor.color(0xAAFFCC); // mint (change count)

    private static final String SEP = "─────────────────────────────────────────────";

    private SetPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    /** Send the Set edit panel for the active session. */
    public static void sendPanel(Player p, SetEditSession session) {
        String cueId      = session.cueId();
        String slug       = session.getSlug();
        String sceneId    = session.getSceneId();
        int    changes    = session.getCurrentChangeCount();

        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header(cueId));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Cue info
        p.sendMessage(
            label("Cue:   ").append(Component.text(cueId, COL_VALUE))
        );
        p.sendMessage(
            label("Slug:  ").append(Component.text(slug.isEmpty() ? "(none)" : slug, COL_VALUE))
        );
        if (sceneId != null) {
            p.sendMessage(
                label("Scene: ").append(Component.text(sceneId, COL_VALUE))
            );
        }
        p.sendMessage(Component.empty());

        // Change count
        p.sendMessage(
            label("Changes this session: ")
            .append(Component.text(changes + " block(s)", changes > 0 ? COL_COUNT : COL_HINT))
        );
        p.sendMessage(Component.empty());

        // Instructions
        p.sendMessage(
            Component.text("Build freely — place or break blocks within the scene bounds.", COL_HINT)
        );
        p.sendMessage(
            Component.text("Changes outside the bounding box are not tracked.", COL_HINT)
        );
        p.sendMessage(Component.empty());

        // Buttons
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    private static Component sep() {
        return Component.text(SEP, COL_SEP);
    }

    private static Component header(String cueId) {
        return Component.text("✎ Set: ", COL_DEPT)
            .append(Component.text(cueId, COL_HEADER).decorate(TextDecoration.BOLD))
            .append(Component.text("  build mode", COL_LABEL));
    }

    private static Component label(String text) {
        return Component.text(text, COL_LABEL);
    }

    /** Standard [Save] / [Save as Preset] / [Cancel] footer row. */
    private static Component saveRow() {
        return Component.text("[Save]")
            .color(COL_BTN_ON)
            .clickEvent(ClickEvent.runCommand("/scaena tech2 save"))
            .hoverEvent(HoverEvent.showText(Component.text("Write block diff to show YAML and close session")))
            .append(Component.text("  "))
            .append(
                Component.text("[Save as Preset]")
                    .color(COL_BTN)
                    .clickEvent(ClickEvent.runCommand("/scaena tech2 savepreset"))
                    .hoverEvent(HoverEvent.showText(Component.text("Promote cue to cues/*.yml preset library")))
            )
            .append(Component.text("  "))
            .append(
                Component.text("[Cancel]")
                    .color(COL_BTN_OFF)
                    .clickEvent(ClickEvent.runCommand("/scaena tech2 cancel"))
                    .hoverEvent(HoverEvent.showText(Component.text("Discard changes and restore world to entry state")))
            );
    }
}
