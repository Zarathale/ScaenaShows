package com.scaena.shows.tech;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Builds and sends the Phase 2 (Timeline Editor) chat panels.
 *
 * Pure static; no state. All click events fire /scaena tech2 subcommands so they
 * survive panel scrollback and don't require server-side state to parse.
 *
 * Panel design reference: ops-029-design-session-2026-04-05 §4c
 *
 * Click command surface:
 *   /scaena tech2 panel            — re-send scene panel
 *   /scaena tech2 scene prev       — navigate to previous scene
 *   /scaena tech2 scene next       — navigate to next scene
 *   /scaena tech2 edit <cueId>     — enter department edit for a cue
 *   /scaena tech2 preview          — start preview mode
 *   /scaena tech2 previewexit      — exit preview mode
 *   /scaena tech2 go               — step forward (Go)
 *   /scaena tech2 hold             — hold
 *   /scaena tech2 prev             — step back (Back)
 *   /scaena tech2 save             — save YAML to disk
 */
public final class CuePanelBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Colours
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray rule
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_SCENE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_TICK     = TextColor.color(0x55FFFF); // aqua
    private static final TextColor COL_SLUG     = TextColor.color(0xDDDDDD); // light gray
    private static final TextColor COL_CUE_ID   = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_EMPTY    = TextColor.color(0x555555); // dark gray (stub label)
    private static final TextColor COL_EDIT_BTN = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_NAV_BTN  = TextColor.color(0x55AAFF); // light blue
    private static final TextColor COL_NOW      = TextColor.color(0x55FF55); // green (current pos)
    private static final TextColor COL_NEXT_POS = TextColor.color(0xFFFFAA); // pale yellow (next)
    private static final TextColor COL_SAVE_BTN = TextColor.color(0x55FF55); // green
    private static final TextColor COL_CANCEL   = TextColor.color(0xFF5555); // red
    private static final TextColor COL_BOSS_BAR = TextColor.color(0xFFAA00); // orange for edit bar

    private static final String SEP = "─────────────────────────────────────────────";

    private CuePanelBuilder() {}

    // -----------------------------------------------------------------------
    // Main scene view — Edit mode  (§4c mockup)
    // -----------------------------------------------------------------------

    /**
     * Build and send the Phase 2 cue panel for the session's current scene.
     *
     * Renders:
     *   header / Prev+Next scene buttons
     *   cue entries grouped by tick (slug as group label, cue_id per line with [Edit ▸])
     *   position bar (Now / Next) — shown when preview is active
     *
     * @param editor provides getSceneCueRefs, formatTick, isCueStub
     */
    public static void sendScenePanel(Player p, TechCueSession session, ShowYamlEditor editor) {
        p.sendMessage(buildScenePanel(session, editor));
    }

    public static Component buildScenePanel(TechCueSession session, ShowYamlEditor editor) {
        PromptBook book     = session.getBook();
        String     sceneId  = session.getCurrentSceneId();
        PromptBook.SceneSpec scene = (book != null) ? book.findScene(sceneId) : null;

        Component sep = Component.text(SEP, COL_SEP);

        // Header: "TECH REHEARSAL — Scene N: Label"
        String sceneLabel = (scene != null) ? scene.label() : (sceneId != null ? sceneId : "—");
        Component header = Component.text("TECH REHEARSAL", COL_HEADER, TextDecoration.BOLD)
            .append(Component.text("  —  ", COL_SEP))
            .append(Component.text(sceneLabel, COL_SCENE));

        // Scene navigation row
        Component sceneNav = buildSceneNavRow(session, book, scene);

        // Cue entries
        List<Map<String, Object>> cueRefs = (editor != null)
            ? editor.getSceneCueRefs(sceneId)
            : List.of();

        Component cueList = buildCueList(cueRefs, editor, session);

        // Position bar — only in preview
        Component posBar = session.isPreviewActive()
            ? buildPositionBar(session, cueRefs, editor)
            : Component.empty();

        // Assemble
        Component panel = Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(sceneNav).append(Component.newline())
            .append(sep).append(Component.newline());

        if (cueRefs.isEmpty()) {
            panel = panel.append(
                Component.text("  (no cues in this scene)", COL_EMPTY)
                    .append(Component.newline()));
        } else {
            panel = panel.append(cueList);
        }

        panel = panel.append(sep);

        if (session.isPreviewActive()) {
            panel = panel.append(Component.newline())
                .append(posBar)
                .append(Component.newline())
                .append(sep);
        }

        return panel;
    }

    // -----------------------------------------------------------------------
    // Scene navigation row  [◀ Prev Scene]          [Next Scene ▶]
    // -----------------------------------------------------------------------

    public static void sendSceneNav(Player p, TechCueSession session) {
        p.sendMessage(buildSceneNavRow(session,
            session.getBook(),
            session.getBook() != null ? session.getBook().findScene(session.getCurrentSceneId()) : null));
    }

    private static Component buildSceneNavRow(
        TechCueSession session,
        PromptBook book,
        PromptBook.SceneSpec scene
    ) {
        boolean hasPrev = hasPrevScene(book, scene);
        boolean hasNext = hasNextScene(book, scene);

        Component prevBtn = hasPrev
            ? clickBtn("[◀ Prev Scene]", "/scaena tech2 scene prev", COL_NAV_BTN,
                "Previous scene")
            : Component.text("[◀ Prev Scene]", COL_EMPTY);

        Component nextBtn = hasNext
            ? clickBtn("[Next Scene ▶]", "/scaena tech2 scene next", COL_NAV_BTN,
                "Next scene")
            : Component.text("[Next Scene ▶]", COL_EMPTY);

        return Component.text("  ")
            .append(prevBtn)
            .append(Component.text("                "))
            .append(nextBtn);
    }

    // -----------------------------------------------------------------------
    // Cue list
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Component buildCueList(
        List<Map<String, Object>> cueRefs,
        ShowYamlEditor editor,
        TechCueSession session
    ) {
        if (cueRefs.isEmpty()) return Component.empty();

        Component out = Component.empty();

        // Group consecutive entries by their `at` tick
        // (same-tick entries form one "beat" and share a slug)
        List<List<Map<String, Object>>> groups = groupByTick(cueRefs);

        long previewTick = session.isPreviewActive() && session.getPreviewShow() != null
            ? session.getPreviewShow().getCurrentTick()
            : -1L;

        for (List<Map<String, Object>> group : groups) {
            Map<String, Object> first = group.get(0);
            int  tick = intVal(first, "at", 0);
            String slug = str(first, "slug", "");

            // Tick + slug header line
            String tickStr = (editor != null) ? editor.formatTick(tick) : tick + "t";
            boolean isCurrent = (previewTick >= 0 && previewTick == tick);

            TextColor tickCol = isCurrent ? COL_NOW : COL_TICK;
            Component tickLabel = Component.text("  " + tickStr, tickCol, TextDecoration.BOLD);

            Component slugLabel = slug.isEmpty()
                ? Component.empty()
                : Component.text("  " + slug, COL_SLUG);

            out = out.append(tickLabel).append(slugLabel).append(Component.newline());

            // One line per cue_id in the group
            for (Map<String, Object> entry : group) {
                String cueId = str(entry, "cue_id", "");
                boolean isStub = (editor != null) && editor.isCueStub(cueId);

                Component cueIdText = Component.text("    " + cueId, COL_CUE_ID);
                if (isStub) {
                    cueIdText = cueIdText.append(
                        Component.text("  (empty)", COL_EMPTY, TextDecoration.ITALIC));
                }

                // [Edit ▸] button — right-aligned via padding
                Component editBtn = cueId.isEmpty()
                    ? Component.empty()
                    : clickBtn("[Edit ▸]", "/scaena tech2 edit " + cueId, COL_EDIT_BTN,
                        "Edit " + cueId);

                // Pad between cue id text and button
                int pad = Math.max(1, 44 - cueId.length() - (isStub ? 10 : 0));
                Component line = cueIdText
                    .append(Component.text(" ".repeat(pad)))
                    .append(editBtn)
                    .append(Component.newline());

                out = out.append(line);
            }
        }

        return out;
    }

    // -----------------------------------------------------------------------
    // Position bar  (preview mode footer)
    //   ▶ Now: 10s | 200t   Next: 17s | 340t
    // -----------------------------------------------------------------------

    private static Component buildPositionBar(
        TechCueSession session,
        List<Map<String, Object>> cueRefs,
        ShowYamlEditor editor
    ) {
        long currentTick = session.getPreviewShow() != null
            ? session.getPreviewShow().getCurrentTick()
            : 0L;

        String nowStr  = (editor != null) ? editor.formatTick((int) currentTick) : currentTick + "t";

        // Find next tick after current in the scene's cue refs
        String nextStr = "—";
        for (Map<String, Object> entry : cueRefs) {
            int t = intVal(entry, "at", 0);
            if (t > currentTick) {
                nextStr = (editor != null) ? editor.formatTick(t) : t + "t";
                break;
            }
        }

        return Component.text("  ")
            .append(Component.text("▶ Now: ", COL_NOW, TextDecoration.BOLD))
            .append(Component.text(nowStr, COL_NOW))
            .append(Component.text("   Next: ", COL_NEXT_POS))
            .append(Component.text(nextStr, COL_NEXT_POS));
    }

    // -----------------------------------------------------------------------
    // End-of-scene pause point
    // -----------------------------------------------------------------------

    /**
     * Send the end-of-scene panel that appears when Go reaches the last cue in a scene.
     *
     *   ─────────────────────────────────────────────
     *   End of Scene 2.
     *   [Exit Preview]              [Next Scene ▶]
     *   ─────────────────────────────────────────────
     */
    public static void sendEndOfScenePanel(Player p, TechCueSession session) {
        PromptBook book  = session.getBook();
        String sceneId   = session.getCurrentSceneId();
        PromptBook.SceneSpec scene = (book != null) ? book.findScene(sceneId) : null;

        Component sep = Component.text(SEP, COL_SEP);

        String label = (scene != null) ? scene.label() : (sceneId != null ? sceneId : "scene");
        Component heading = Component.text("  End of ", COL_SLUG)
            .append(Component.text(label, COL_SCENE))
            .append(Component.text(".", COL_SLUG));

        Component exitBtn = clickBtn("[Exit Preview]", "/scaena tech2 previewexit",
            COL_CANCEL, "Stop preview and return to edit mode");

        boolean hasNext = hasNextScene(book, scene);
        Component nextBtn = hasNext
            ? clickBtn("[Next Scene ▶]", "/scaena tech2 scene next", COL_NAV_BTN, "Next scene")
            : Component.text("[Next Scene ▶]", COL_EMPTY);

        Component buttons = Component.text("  ")
            .append(exitBtn)
            .append(Component.text("              "))
            .append(nextBtn);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(heading).append(Component.newline())
            .append(buttons).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Department edit session shell
    // -----------------------------------------------------------------------

    /**
     * Show the persistent boss bar for the duration of a department edit session.
     *
     *   ✎  casting.zombie.warrior_enter
     */
    public static void showEditBossBar(Player p, String fullCueId, TechCueSession session) {
        BossBar bar = BossBar.bossBar(
            Component.text("✎  " + fullCueId, COL_BOSS_BAR),
            1f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS
        );
        p.showBossBar(bar);
        session.setEditBossBar(bar);
    }

    /** Remove the edit-mode boss bar. */
    public static void clearEditBossBar(Player p, TechCueSession session) {
        BossBar bar = session.getEditBossBar();
        if (bar != null) {
            p.hideBossBar(bar);
            session.setEditBossBar(null);
        }
    }

    /**
     * Send the three universal edit-session action buttons.
     * These are re-sent periodically so they don't scroll off chat.
     *
     *   [Save]   [Save as Preset]   [Cancel]
     */
    public static void sendSaveCancelButtons(Player p) {
        Component save = clickBtn("[Save]", "/scaena tech2 editsave",
            COL_SAVE_BTN, "Commit changes to this cue");
        Component savePreset = clickBtn("[Save as Preset]", "/scaena tech2 editpreset",
            COL_NAV_BTN, "Promote to cue library");
        Component cancel = clickBtn("[Cancel]", "/scaena tech2 editcancel",
            COL_CANCEL, "Discard changes and exit edit mode");

        p.sendMessage(Component.text("  ")
            .append(save)
            .append(Component.text("   "))
            .append(savePreset)
            .append(Component.text("   "))
            .append(cancel));
    }

    // -----------------------------------------------------------------------
    // Preview mode sidebar (actionbar-based)
    // -----------------------------------------------------------------------

    /**
     * Update the actionbar with the current preview position.
     *
     * Shows: ▶  Xs | Nt  (▶ if advancing, ⏸ if hold active)
     *
     * Called after every Go/Back/Hold action.
     */
    public static void updatePreviewSidebar(Player p, TechCueSession session) {
        if (!session.isPreviewActive() || session.getPreviewShow() == null) return;

        long tick = session.getPreviewShow().getCurrentTick();
        double seconds = tick / 20.0;
        String secStr = (seconds == Math.floor(seconds))
            ? String.valueOf((long) seconds)
            : String.valueOf(seconds);
        String tickStr = secStr + "s | " + tick + "t";

        String icon = session.isHoldActive() ? "⏸" : "▶";
        p.sendActionBar(MM.deserialize(
            "<gray>" + icon + "  </gray><aqua>" + tickStr + "</aqua>"));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Group a flat list of timeline entries by their `at` tick value. */
    private static List<List<Map<String, Object>>> groupByTick(
        List<Map<String, Object>> entries
    ) {
        List<List<Map<String, Object>>> groups = new ArrayList<>();
        if (entries.isEmpty()) return groups;

        int prevTick = intVal(entries.get(0), "at", -1);
        List<Map<String, Object>> current = new ArrayList<>();

        for (Map<String, Object> entry : entries) {
            int tick = intVal(entry, "at", 0);
            if (tick != prevTick && !current.isEmpty()) {
                groups.add(current);
                current = new ArrayList<>();
                prevTick = tick;
            }
            current.add(entry);
        }
        if (!current.isEmpty()) groups.add(current);

        return groups;
    }

    private static boolean hasPrevScene(PromptBook book, PromptBook.SceneSpec scene) {
        if (book == null || scene == null || book.scenes() == null) return false;
        int idx = book.scenes().indexOf(scene);
        return idx > 0;
    }

    private static boolean hasNextScene(PromptBook book, PromptBook.SceneSpec scene) {
        if (book == null || scene == null || book.scenes() == null) return false;
        int idx = book.scenes().indexOf(scene);
        return idx >= 0 && idx < book.scenes().size() - 1;
    }

    /** Build a clickable text button with a hover tooltip. */
    private static Component clickBtn(String label, String command,
                                      TextColor color, String tooltip) {
        return Component.text(label, color, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, NamedTextColor.GRAY)));
    }

    private static int intVal(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return (v != null) ? v.toString() : def;
    }
}
