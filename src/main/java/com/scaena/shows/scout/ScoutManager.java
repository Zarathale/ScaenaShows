package com.scaena.shows.scout;

import com.scaena.shows.registry.YamlLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages per-player scout sessions: loading objectives, driving the sidebar,
 * recording captures, and writing the output file.
 *
 * Objectives are read from:  plugins/ScaenaShows/scout_objectives/[showId].yml
 * Captures are written to:   plugins/ScaenaShows/scout_captures/[showId]/[date].yml
 */
public final class ScoutManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JavaPlugin plugin;
    private final Logger     log;

    /** Active sessions keyed by player UUID. */
    private final Map<UUID, ScoutSession> sessions = new HashMap<>();

    public ScoutManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    // -----------------------------------------------------------------------
    // /scaena scout load <showId> [tag]
    // -----------------------------------------------------------------------

    public void load(Player player, String showId, String tagFilter) {
        File objFile = new File(plugin.getDataFolder(), "scout_objectives/" + showId + ".yml");
        if (!objFile.exists()) {
            player.sendMessage(MM.deserialize(
                "<red>No objectives file found for <white>" + showId + "</white>.</red>\n"
                + "<gray>Expected at: plugins/ScaenaShows/scout_objectives/" + showId + ".yml</gray>"));
            return;
        }

        List<ScoutObjective> objectives;
        try {
            objectives = parseObjectives(objFile, tagFilter);
        } catch (Exception e) {
            player.sendMessage(MM.deserialize(
                "<red>Failed to parse objectives: " + e.getMessage() + "</red>"));
            log.warning("[Scout] Failed to parse objectives for " + showId + ": " + e.getMessage());
            return;
        }

        if (objectives.isEmpty()) {
            String msg = tagFilter != null
                ? "<yellow>No objectives match tag <white>" + tagFilter + "</white> for <white>" + showId + "</white>.</yellow>"
                : "<yellow>No objectives found for <white>" + showId + "</white>.</yellow>";
            player.sendMessage(MM.deserialize(msg));
            return;
        }

        // Replace any existing session cleanly
        if (sessions.containsKey(player.getUniqueId())) {
            dismissSession(player, false);
        }

        Scoreboard previousBoard = player.getScoreboard();
        Scoreboard board         = buildSidebar(showId, objectives);
        player.setScoreboard(board);

        ScoutSession session = new ScoutSession(showId, objectives, previousBoard, board);
        sessions.put(player.getUniqueId(), session);

        String filterNote = tagFilter != null ? " <gray>[" + tagFilter + "]</gray>" : "";
        player.sendMessage(MM.deserialize(
            "<gold>Scout loaded: <white>" + showId + "</white>" + filterNote + "</gold> "
            + "<gray>— " + objectives.size() + " mark(s).</gray>"));
        player.sendMessage(MM.deserialize(
            "<gray>Walk to each mark and run <white>/scaena set <code></white>.</gray>"));
    }

    // -----------------------------------------------------------------------
    // /scaena set <code>
    // -----------------------------------------------------------------------

    public void capture(Player player, String code) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize(
                "<red>No active scouting session. "
                + "Use <white>/scaena scout load <showId></white> first.</red>"));
            return;
        }

        ScoutObjective objective = session.findByCode(code);
        if (objective == null) {
            player.sendMessage(MM.deserialize(
                "<red>Unknown code <white>" + code + "</white>. "
                + "Use <white>/scaena scout status</white> to see available codes.</red>"));
            return;
        }

        Location loc = player.getLocation();
        ScoutCapture capture = new ScoutCapture(
            objective.name(),
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch()
        );
        session.addCapture(capture);

        // Tick the sidebar
        removeFromSidebar(session.scoutScoreboard(), objective.sidebarEntry());

        String coords = String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
        player.sendMessage(MM.deserialize(
            "<green>✓ <white>" + objective.code() + "</white> "
            + "<gray>" + objective.displayLabel() + " — " + coords + "</gray>"));

        if (session.allCaptured()) {
            player.sendMessage(MM.deserialize(
                "<gold>All marks captured! Run <white>/scaena scout save</white> to write the file.</gold>"));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena scout save
    // -----------------------------------------------------------------------

    public void save(Player player) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize("<red>No active scouting session.</red>"));
            return;
        }
        if (session.captures().isEmpty()) {
            player.sendMessage(MM.deserialize(
                "<yellow>Nothing captured yet. "
                + "Walk to a mark and use <white>/scaena set <code></white>.</yellow>"));
            return;
        }

        File captureDir = new File(plugin.getDataFolder(), "scout_captures/" + session.showId());
        captureDir.mkdirs();

        String date    = LocalDate.now().format(DATE_FMT);
        File   outFile = uniqueFile(captureDir, date);

        try (PrintWriter w = new PrintWriter(outFile, "UTF-8")) {
            writeCaptureYaml(w, session, date);
        } catch (IOException e) {
            player.sendMessage(MM.deserialize("<red>Save failed: " + e.getMessage() + "</red>"));
            log.severe("[Scout] Failed to write capture file: " + e.getMessage());
            return;
        }

        player.sendMessage(MM.deserialize(
            "<gold>Saved <white>" + session.captures().size() + "</white> mark(s) → "
            + "<white>" + outFile.getName() + "</white></gold>"));
        player.sendMessage(MM.deserialize(
            "<gray>Path: " + outFile.getAbsolutePath() + "</gray>"));
        player.sendMessage(MM.deserialize(
            "<gray>Session still active — keep capturing, or <white>/scaena scout dismiss</white> to end.</gray>"));
    }

    // -----------------------------------------------------------------------
    // /scaena scout status
    // -----------------------------------------------------------------------

    public void status(Player player) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize("<yellow>No active scouting session.</yellow>"));
            return;
        }

        player.sendMessage(MM.deserialize(
            "<gold>Scout: <white>" + session.showId() + "</white></gold> "
            + "<gray>(" + session.capturedCount() + "/" + session.objectives().size() + " captured)</gray>"));

        for (ScoutObjective obj : session.objectives()) {
            boolean done = session.captures().containsKey(obj.name());
            String marker = done ? "<green>✓</green>" : "<yellow>○</yellow>";
            String detail = "";
            if (done) {
                ScoutCapture c = session.captures().get(obj.name());
                detail = String.format(" <dark_gray>(%.1f, %.1f, %.1f)</dark_gray>",
                    c.x(), c.y(), c.z());
            }
            player.sendMessage(MM.deserialize(
                "  " + marker + " <white>" + obj.code() + "</white> "
                + "<gray>" + obj.displayLabel() + "</gray>" + detail));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena scout dismiss
    // -----------------------------------------------------------------------

    public void dismiss(Player player) {
        if (!sessions.containsKey(player.getUniqueId())) {
            player.sendMessage(MM.deserialize("<yellow>No active scouting session.</yellow>"));
            return;
        }
        dismissSession(player, true);
    }

    // -----------------------------------------------------------------------
    // Lifecycle — call from PlayerLifecycleListener
    // -----------------------------------------------------------------------

    /** Clean up any session when a player disconnects — no save, no message. */
    public void onPlayerQuit(Player player) {
        ScoutSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            log.info("[Scout] Session for " + player.getName() + " closed on disconnect "
                + "(captured " + session.capturedCount() + "/" + session.objectives().size() + " marks — not saved).");
        }
    }

    // -----------------------------------------------------------------------
    // Internals: sidebar
    // -----------------------------------------------------------------------

    private Scoreboard buildSidebar(String showId, List<ScoutObjective> objectives) {
        ScoreboardManager mgr = Objects.requireNonNull(Bukkit.getScoreboardManager());
        Scoreboard board = mgr.getNewScoreboard();

        // Objective internal name must be ≤ 16 chars
        Objective sidebarObj = board.registerNewObjective(
            "scaena_scout",
            "dummy",
            Component.text("Scouting: " + showId, NamedTextColor.GOLD)
        );
        sidebarObj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Assign scores so first objective renders at the top of the sidebar.
        // Higher score = higher position. We reverse-index so 1.1 is topmost.
        int total = objectives.size();
        for (int i = 0; i < total; i++) {
            ScoutObjective o = objectives.get(i);
            // Score = total - i, so first entry gets the highest score
            sidebarObj.getScore(o.sidebarEntry()).setScore(total - i);
        }

        return board;
    }

    private void removeFromSidebar(Scoreboard board, String entry) {
        board.resetScores(entry);
    }

    // -----------------------------------------------------------------------
    // Internals: session management
    // -----------------------------------------------------------------------

    private void dismissSession(Player player, boolean notify) {
        ScoutSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;
        // Restore whatever scoreboard they had before scouting
        player.setScoreboard(session.previousScoreboard());
        if (notify) {
            player.sendMessage(MM.deserialize(
                "<gray>Scouting session dismissed.</gray>"));
        }
    }

    // -----------------------------------------------------------------------
    // Internals: file I/O
    // -----------------------------------------------------------------------

    /** Returns a file path that doesn't collide — appends -2, -3, etc. if needed. */
    private File uniqueFile(File dir, String date) {
        File f = new File(dir, date + ".yml");
        if (!f.exists()) return f;
        int n = 2;
        while (f.exists()) {
            f = new File(dir, date + "-" + n + ".yml");
            n++;
        }
        return f;
    }

    private void writeCaptureYaml(PrintWriter w, ScoutSession session, String date) {
        w.println("# Scout capture — " + session.showId() + " — " + date);
        w.println("# Generated by /scaena scout save");
        w.println("# Merge coordinates into show-params, then commit and push.");
        w.println("show: " + session.showId());
        w.println("captured_at: \"" + date + "\"");
        w.println("captures:");
        for (ScoutCapture c : session.captures().values()) {
            w.println("  " + c.name() + ":");
            w.println("    world: " + c.world());
            w.printf ("    x: %.4f%n", c.x());
            w.printf ("    y: %.4f%n", c.y());
            w.printf ("    z: %.4f%n", c.z());
            w.printf ("    yaw: %.2f%n", (double) c.yaw());
            w.printf ("    pitch: %.2f%n", (double) c.pitch());
        }
    }

    // -----------------------------------------------------------------------
    // Internals: YAML parsing
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<ScoutObjective> parseObjectives(File file, String tagFilter) throws IOException {
        Map<String, Object> root = YamlLoader.load(file);
        Object rawList = root.get("objectives");
        if (!(rawList instanceof List<?> list)) return List.of();

        List<ScoutObjective> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> rawMap)) continue;
            Map<String, Object> entry = (Map<String, Object>) rawMap;

            String code = str(entry, "code");
            String name = str(entry, "name");
            if (code == null || name == null) continue;

            List<String> tags = new ArrayList<>();
            Object rawTags = entry.get("tags");
            if (rawTags instanceof List<?> tList) {
                for (Object t : tList) tags.add(t.toString());
            }

            ScoutObjective objective = new ScoutObjective(code, name, List.copyOf(tags));
            if (objective.matchesTag(tagFilter)) {
                result.add(objective);
            }
        }
        return result;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }
}
