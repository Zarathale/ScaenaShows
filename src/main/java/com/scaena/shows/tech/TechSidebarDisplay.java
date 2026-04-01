package com.scaena.shows.tech;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * Manages the per-player scoreboard sidebar during a tech session.
 *
 * Sidebar layout (top-to-bottom):
 *
 *   TECH · showcase.01
 *   ──────────────────
 *   Site A — Active
 *   Entities: 2 / 2
 *   Marks modified: 0
 *   ──────────────────
 *   CAST   ✓
 *   WARDROBE  ✓
 *   LIGHTS  —
 *   SET    ✓
 *   FX     —
 *   SCRIPT  —
 *
 * Uses a fresh scoreboard per session. Original scoreboard restored on DISMISS.
 *
 * Scoreboard line approach: each line is a unique score entry (score = descending line order).
 * Lines use legacy § color codes for compact rendering — Adventure scoreboard API
 * is not universally available on all Paper builds; the objective displayName uses Adventure.
 */
public final class TechSidebarDisplay {

    private static final String OBJ_NAME = "scaena_tech";

    // Legacy color codes for sidebar lines (rendered server-side, not through Adventure)
    private static final String GOLD      = "§6";
    private static final String WHITE     = "§f";
    private static final String GRAY      = "§7";
    private static final String DARK_GRAY = "§8";
    private static final String GREEN     = "§a";
    private static final String YELLOW    = "§e";
    private static final String RED       = "§c";

    private TechSidebarDisplay() {}

    /**
     * Build (or rebuild) the tech sidebar for the player's current session state.
     * Called on LOAD, TOGGLE, CAPTURE, SAVE, and parameter changes.
     */
    public static void show(Player player, TechSession session) {
        ScoreboardManager sbm = Bukkit.getScoreboardManager();
        Scoreboard board = sbm.getNewScoreboard();

        // Create display objective
        Objective obj = board.registerNewObjective(
            OBJ_NAME, Criteria.DUMMY,
            GOLD + "TECH" + GRAY + " · " + WHITE + session.showId());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        String sceneLabel = scene != null ? scene.label() : "—";

        // Count active entities and total entity marks in scene
        int activeEntities = session.activeEntities().size();
        int totalMarks     = session.modifiedMarks().size();

        // Build lines top-to-bottom; assign descending scores (higher = top)
        String[] lines = buildLines(session, scene, sceneLabel, activeEntities, totalMarks);

        // Assign scores in descending order (line 0 = top = highest score)
        // We need unique strings — pad with invisible color codes if duplicates exist
        int score = lines.length;
        java.util.Set<String> used = new java.util.HashSet<>();
        for (String line : lines) {
            // Make unique if needed (scoreboard rejects duplicate entries)
            String entry = uniqueEntry(line, used);
            used.add(entry);
            obj.getScore(entry).setScore(score--);
        }

        player.setScoreboard(board);
    }

    // -----------------------------------------------------------------------
    // Line building
    // -----------------------------------------------------------------------

    private static String[] buildLines(TechSession session, PromptBook.SceneSpec scene,
                                        String sceneLabel, int entityCount, int markCount) {
        boolean dirty = session.hasUnsavedChanges();

        return new String[]{
            // Title area
            GRAY + "──────────────────",
            WHITE + sceneLabel,
            GRAY + "Entities: " + WHITE + entityCount,
            GRAY + "Marks mod: " + (markCount > 0 ? YELLOW : WHITE) + markCount
                + (dirty ? " " + YELLOW + "✎" : ""),
            DARK_GRAY + "──────────────────",
            // Department status
            deptLine("CAST",    "casting",   session, scene),
            deptLine("WARDROBE","wardrobe",  session, scene),
            deptLine("SET",     "set",       session, scene),
            deptLine("LIGHTS",  "lighting",  session, scene),
            deptLine("FX",      "fireworks", session, scene),
            deptLine("SCRIPT",  "script",    session, scene),
        };
    }

    private static String deptLine(String label, String dept,
                                    TechSession session, PromptBook.SceneSpec scene) {
        boolean hasAssets = scene != null && deptHasAssets(scene, dept);
        boolean active    = session.isDeptActive(dept);

        String check = active    ? GREEN + "✓"
                     : hasAssets ? GRAY  + "—"
                                 : DARK_GRAY + "·";

        // Pad label to 8 chars for alignment
        String padded = String.format("%-8s", label);
        return GRAY + padded + " " + check;
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

    /**
     * Returns the entry string made unique by appending invisible reset codes.
     * Scoreboards reject duplicate score entries; this avoids the crash.
     */
    private static String uniqueEntry(String base, java.util.Set<String> used) {
        if (!used.contains(base)) return base;
        // Append §r§r... until unique (each §r is invisible but makes the string distinct)
        String candidate = base;
        int i = 1;
        while (used.contains(candidate)) {
            candidate = base + "§r".repeat(i++);
            if (i > 20) break; // safety
        }
        return candidate;
    }
}
