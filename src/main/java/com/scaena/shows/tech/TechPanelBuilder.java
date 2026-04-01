package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Builds and sends the clickable chat control panel for Tech Rehearsal Mode.
 *
 * Pure function: all methods are stateless — they take TechSession and return/send
 * Components. All click actions are runCommand("/scaena tech ...") so they survive
 * across reloads and don't require server-side state to parse.
 *
 * Panel anatomy:
 *
 *   ─────────────────────────────────────────────
 *   TECH · showcase.01  ·  Site A — The Workshop
 *   [CAST ✓] [WDRB ✓] [SET ✓] [LGTS ✓] [FX —]
 *   Marks: home_base  companion_spawn  vindicator_spawn
 *   [Focus Mark...]   [Params]   [Save]  [Discard]  [Exit]
 *   ─────────────────────────────────────────────
 */
public final class TechPanelBuilder {

    private static final MiniMessage MM   = MiniMessage.miniMessage();

    private static final TextColor COL_HEADER  = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_SCENE   = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_ACTIVE  = TextColor.color(0x55FF55); // green
    private static final TextColor COL_INACTIVE= TextColor.color(0x555555); // dark gray
    private static final TextColor COL_MARK    = TextColor.color(0x55FFFF); // aqua
    private static final TextColor COL_ACTION  = TextColor.color(0xAAAAAA); // gray
    private static final TextColor COL_SAVE    = TextColor.color(0x55FF55); // green
    private static final TextColor COL_DISCARD = TextColor.color(0xFF5555); // red
    private static final TextColor COL_SEP     = TextColor.color(0x444444);

    private TechPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Main panel
    // -----------------------------------------------------------------------

    /** Build and send the main tech panel to the player. */
    public static void send(Player player, TechSession session) {
        player.sendMessage(buildPanel(session));
    }

    public static Component buildPanel(TechSession session) {
        PromptBook book = session.book();
        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());

        Component sep = Component.text(
            "─────────────────────────────────────────", COL_SEP);

        // Header line: TECH · show · scene label
        String sceneLabel = scene != null ? scene.label() : "—";
        Component header = Component.text("TECH", COL_HEADER, TextDecoration.BOLD)
            .append(Component.text(" · ", COL_SEP))
            .append(Component.text(book.showId(), COL_SCENE))
            .append(Component.text("  ·  ", COL_SEP))
            .append(Component.text(sceneLabel, COL_SCENE));

        // Departments row
        Component depts = buildDeptRow(session, scene);

        // Marks row (arrival mark + all named marks for active depts in this scene)
        Component marks = buildMarksRow(session, scene);

        // Actions row
        Component actions = buildActionsRow(session);

        return Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(depts).append(Component.newline())
            .append(marks).append(Component.newline())
            .append(actions).append(Component.newline())
            .append(sep);
    }

    // -----------------------------------------------------------------------
    // Departments row
    // -----------------------------------------------------------------------

    private static Component buildDeptRow(TechSession session, PromptBook.SceneSpec scene) {
        Component row = Component.empty();

        String[] depts = {"casting", "wardrobe", "set", "lighting", "fireworks", "script"};
        String[] labels= {"CAST",    "WDRB",     "SET", "LGTS",     "FX",        "SCRPT"};

        for (int i = 0; i < depts.length; i++) {
            boolean hasAssets = scene != null && deptHasAssets(scene, depts[i]);
            boolean active    = session.isDeptActive(depts[i]);

            String checkmark  = active ? " ✓" : " —";
            TextColor col     = !hasAssets ? COL_INACTIVE
                                : active   ? COL_ACTIVE
                                           : COL_ACTION;

            String cmd        = "/scaena tech toggle " + depts[i];
            String hoverText  = (hasAssets ? (active ? "Deactivate " : "Activate ") : "No assets for ") + depts[i];

            Component btn = Component.text("[" + labels[i] + checkmark + "]", col)
                .clickEvent(ClickEvent.runCommand(cmd))
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)));

            row = row.append(btn).append(Component.text(" "));
        }
        return row;
    }

    // -----------------------------------------------------------------------
    // Marks row
    // -----------------------------------------------------------------------

    private static Component buildMarksRow(TechSession session, PromptBook.SceneSpec scene) {
        if (scene == null) return Component.text("No scene loaded.", COL_INACTIVE);

        List<String> marks = collectMarkNames(scene);
        if (marks.isEmpty()) {
            return Component.text("No marks defined.", COL_INACTIVE);
        }

        Component row = Component.text("Marks: ", COL_ACTION);
        for (String mark : marks) {
            boolean modified = session.modifiedMarks().containsKey(mark);
            TextColor col = modified ? TextColor.color(0xFFFF55) : COL_MARK; // yellow if modified

            Component btn = Component.text(mark, col)
                .clickEvent(ClickEvent.runCommand("/scaena tech capture " + mark))
                .hoverEvent(HoverEvent.showText(
                    Component.text("Focus mark for capture: " + mark
                        + (modified ? " [modified]" : ""))));
            row = row.append(btn).append(Component.text("  "));
        }
        return row;
    }

    // -----------------------------------------------------------------------
    // Actions row
    // -----------------------------------------------------------------------

    private static Component buildActionsRow(TechSession session) {
        boolean dirty = session.hasUnsavedChanges();

        Component focusMark = Component.text("[Focus Mark...]", COL_MARK)
            .clickEvent(ClickEvent.runCommand("/scaena tech marklist"))
            .hoverEvent(HoverEvent.showText(Component.text("Open mark list for capture")));

        Component params = Component.text("[Params]", COL_ACTIVE)
            .clickEvent(ClickEvent.runCommand("/scaena tech params"))
            .hoverEvent(HoverEvent.showText(Component.text("Open parameter panel")));

        Component save = Component.text("[Save]",
                dirty ? COL_SAVE : COL_INACTIVE)
            .clickEvent(ClickEvent.runCommand("/scaena tech save"))
            .hoverEvent(HoverEvent.showText(Component.text(
                dirty ? "Save changes to prompt-book.yml + captures"
                      : "No unsaved changes")));

        Component discard = Component.text("[Discard]",
                dirty ? COL_DISCARD : COL_INACTIVE)
            .clickEvent(ClickEvent.runCommand("/scaena tech discard"))
            .hoverEvent(HoverEvent.showText(Component.text(
                dirty ? "Discard all unsaved changes" : "No unsaved changes")));

        Component exit = Component.text("[Exit]", COL_ACTION)
            .clickEvent(ClickEvent.runCommand("/scaena tech dismiss"))
            .hoverEvent(HoverEvent.showText(Component.text("Exit Tech Rehearsal Mode")));

        return focusMark
            .append(Component.text("   "))
            .append(params)
            .append(Component.text("   "))
            .append(save)
            .append(Component.text("  "))
            .append(discard)
            .append(Component.text("   "))
            .append(exit);
    }

    // -----------------------------------------------------------------------
    // Mark list panel — secondary panel for focusing a mark
    // -----------------------------------------------------------------------

    public static void sendMarkList(Player player, TechSession session) {
        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        if (scene == null) {
            player.sendMessage(MM.deserialize("<yellow>No scene loaded.</yellow>"));
            return;
        }

        List<String> marks = collectMarkNames(scene);
        if (marks.isEmpty()) {
            player.sendMessage(MM.deserialize("<yellow>No marks defined for this scene.</yellow>"));
            return;
        }

        player.sendMessage(Component.text("Mark list — click to enter capture mode:", COL_HEADER));
        for (String mark : marks) {
            boolean modified = session.modifiedMarks().containsKey(mark);
            Component btn = Component.text("  [" + mark + "]",
                    modified ? TextColor.color(0xFFFF55) : COL_MARK)
                .clickEvent(ClickEvent.runCommand("/scaena tech capture " + mark))
                .hoverEvent(HoverEvent.showText(
                    Component.text("Focus \"" + mark + "\" — walk to position and right-click slot 8")));
            player.sendMessage(btn);
        }
        player.sendMessage(
            Component.text("  [Cancel]", COL_DISCARD)
                .clickEvent(ClickEvent.runCommand("/scaena tech panel"))
                .hoverEvent(HoverEvent.showText(Component.text("Return to main panel"))));
    }

    // -----------------------------------------------------------------------
    // Parameter panel
    // -----------------------------------------------------------------------

    public static void sendParamPanel(Player player, TechSession session) {
        PromptBook book = session.book();
        List<PromptBook.ParamSpec> params = book.params();

        player.sendMessage(Component.text(
            "─── Parameters — " + book.showId() + " ───", COL_SEP));
        player.sendMessage(Component.text(
            "Click a param to edit. Locked params are read-only.", COL_ACTION));

        // Also show scene casting params (Q6: Option A)
        PromptBook.SceneSpec scene = book.findScene(session.currentSceneId());
        if (scene != null && scene.hasCasting()) {
            player.sendMessage(Component.text("  — Scene: " + scene.label() + " —", COL_SCENE));
            for (PromptBook.CastingEntry e : scene.casting().entries()) {
                if (e.entityType() == null) continue;
                String paramKey = e.role() + ".entity_type";
                String display  = e.entityType()
                    + (e.subtype() != null ? "/" + e.subtype() : "");
                Object modified = session.modifiedParams().get(paramKey);
                String value    = modified != null ? modified.toString() : display;
                TextColor col   = modified != null
                    ? TextColor.color(0xFFFF55) : COL_ACTION;

                Component btn = Component.text(
                        "  " + paramKey + " = " + value, col)
                    .clickEvent(ClickEvent.runCommand("/scaena tech focusparam " + paramKey))
                    .hoverEvent(HoverEvent.showText(
                        Component.text("Edit casting: " + e.role() + " entity type")));
                player.sendMessage(btn);
            }
        }

        // Show-level params
        if (params != null) {
            player.sendMessage(Component.text("  — Show-level params —", COL_SCENE));
            for (PromptBook.ParamSpec p : params) {
                Object modified = session.modifiedParams().get(p.name());
                String value    = modified != null
                    ? (modified instanceof Number n
                        ? formatDouble(n.doubleValue()) : modified.toString())
                    : p.displayValue();

                TextColor col = p.locked()    ? COL_INACTIVE
                              : modified != null ? TextColor.color(0xFFFF55)
                              : COL_SCENE;

                String editHint = p.locked()            ? " 🔒"
                                : p.type() == PromptBook.ParamType.NUMERIC
                                    ? " [right-click +, shift+rc −]"
                                    : " [chat input]";

                Component btn = Component.text(
                        "  " + p.name() + " = " + value + editHint, col)
                    .clickEvent(p.locked()
                        ? ClickEvent.runCommand("/scaena tech panel")
                        : ClickEvent.runCommand("/scaena tech focusparam " + p.name()))
                    .hoverEvent(HoverEvent.showText(Component.text(
                        p.locked() ? "Locked — " + (p.notes() != null ? p.notes() : "no note")
                                   : (p.notes() != null ? p.notes() : "No note"))));
                player.sendMessage(btn);
            }
        }

        player.sendMessage(
            Component.text("[← Back to panel]", COL_ACTION)
                .clickEvent(ClickEvent.runCommand("/scaena tech panel"))
                .hoverEvent(HoverEvent.showText(Component.text("Return to main panel"))));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static List<String> collectMarkNames(PromptBook.SceneSpec scene) {
        java.util.LinkedHashSet<String> marks = new java.util.LinkedHashSet<>();
        if (scene.arrivalMark() != null) marks.add(scene.arrivalMark());
        if (scene.hasCasting()) {
            scene.casting().entries().stream()
                .map(PromptBook.CastingEntry::mark)
                .filter(m -> m != null).forEach(marks::add);
        }
        if (scene.hasSet()) {
            scene.set().entries().stream()
                .map(PromptBook.SetEntry::mark)
                .filter(m -> m != null).forEach(marks::add);
        }
        return List.copyOf(marks);
    }

    private static boolean deptHasAssets(PromptBook.SceneSpec scene, String dept) {
        return switch (dept) {
            case "casting"   -> scene.hasCasting();
            case "wardrobe"  -> scene.hasWardrobe();
            case "set"       -> scene.hasSet();
            case "lighting"  -> scene.hasLighting();
            case "fireworks" -> scene.hasFireworks();
            case "script"    -> scene.hasScript();
            default          -> false;
        };
    }

    private static String formatDouble(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
    }
}
