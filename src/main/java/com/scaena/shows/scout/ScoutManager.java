package com.scaena.shows.scout;

import com.scaena.shows.model.Show;
import com.scaena.shows.model.ShowSet;
import com.scaena.shows.registry.ShowRegistry;
import com.scaena.shows.registry.YamlLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
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
 * recording captures, spawning in-world markers, and writing output files.
 *
 * Objectives are read from:  plugins/ScaenaShows/scout_objectives/[showId].yml
 * Captures are written to:   plugins/ScaenaShows/scout_captures/[showId]/[date].yml
 *
 * Scene definition is authoritative in the show's sets: map (show.sets.keySet()).
 * The objectives file is the source of truth for what marks belong to each scene.
 */
public final class ScoutManager {

    private static final MiniMessage MM       = MiniMessage.miniMessage();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Category tags in priority order (most specific → least specific).
    // First matching tag wins for icon and color assignment.
    private static final List<String> CATEGORY_PRIORITY =
        List.of("armorer", "drone", "spawns", "props", "blocks", "sites");

    private static final Map<String, TextColor> CATEGORY_COLORS;
    private static final Map<String, String>    CATEGORY_ICONS;

    static {
        Map<String, TextColor> c = new LinkedHashMap<>();
        c.put("sites",   TextColor.color(0xFFAA00)); // gold
        c.put("spawns",  TextColor.color(0x55FFFF)); // aqua
        c.put("armorer", TextColor.color(0xFFFF55)); // yellow
        c.put("drone",   TextColor.color(0xAA55FF)); // light purple
        c.put("props",   TextColor.color(0x55FF55)); // green
        c.put("blocks",  TextColor.color(0xFFFFFF)); // white
        CATEGORY_COLORS = Collections.unmodifiableMap(c);

        Map<String, String> i = new LinkedHashMap<>();
        i.put("sites",   "★");
        i.put("spawns",  "◆");
        i.put("armorer", "⚔");
        i.put("drone",   "↑");
        i.put("props",   "■");
        i.put("blocks",  "⬡");
        CATEGORY_ICONS = Collections.unmodifiableMap(i);
    }

    // Known category/filter tags — used to exclude them when deriving scene names
    private static final Set<String> KNOWN_CATEGORY_TAGS =
        Set.of("sites", "spawns", "props", "blocks", "armorer", "drone");

    private final JavaPlugin   plugin;
    private final ShowRegistry showRegistry;
    private final Logger       log;

    /** Active sessions keyed by player UUID. */
    private final Map<UUID, ScoutSession> sessions = new HashMap<>();

    public ScoutManager(JavaPlugin plugin, ShowRegistry showRegistry) {
        this.plugin       = plugin;
        this.showRegistry = showRegistry;
        this.log          = plugin.getLogger();
    }

    // -----------------------------------------------------------------------
    // /scaena (no args) — summary if session active, otherwise brief help
    // -----------------------------------------------------------------------

    public void summary(Player player) {
        ScoutSession session = sessions.get(player.getUniqueId());

        if (session == null) {
            player.sendMessage(MM.deserialize("<gold><bold>Scaena Scout</bold></gold>"));
            player.sendMessage(MM.deserialize(
                "<gray>No active session. "
                + "Use <white>/scaena scout shows</white> to browse, "
                + "or <white>/scaena scout load <showId> [scene]</white> to begin.</gray>"));
            return;
        }

        String sceneStr = session.activeScene() != null
            ? " <gray>[" + formatSceneName(session.activeScene()) + "]</gray>" : "";
        String progress = session.capturedCount() + "/" + session.objectives().size();

        player.sendMessage(MM.deserialize(
            "<gold><bold>Scout active:</bold></gold> <white>" + session.showId() + "</white>"
            + sceneStr + "  <green>" + progress + " captured this session</green>"));

        long prior = session.objectives().stream()
            .filter(o -> !session.captures().containsKey(o.name())
                      && session.priorCaptures().containsKey(o.name()))
            .count();
        if (prior > 0) {
            player.sendMessage(MM.deserialize(
                "<dark_gray>  " + prior + " prior capture(s) visible as markers</dark_gray>"));
        }

        String markersState = session.markersVisible() ? "<green>on</green>" : "<gray>off</gray>";
        player.sendMessage(MM.deserialize(
            "<gray>  Markers: " + markersState + "  ·  "
            + "Run <white>/scaena scout status</white> for full detail."
            + "  <white>/scaena scout next</white> to advance to the next scene.</gray>"));
    }

    // -----------------------------------------------------------------------
    // /scaena scout shows — list all shows with objectives defined
    // -----------------------------------------------------------------------

    public void shows(Player player) {
        List<String> showIds = getAvailableShowIds();
        if (showIds.isEmpty()) {
            player.sendMessage(MM.deserialize(
                "<yellow>No scout objectives files found in scout_objectives/.</yellow>"));
            return;
        }

        player.sendMessage(MM.deserialize("<gold><bold>Scout-ready shows:</bold></gold>"));
        player.sendMessage(MM.deserialize(
            "<dark_gray>Click a show name to view its scenes.</dark_gray>"));

        for (String showId : showIds) {
            Show show = showRegistry.get(showId);
            String loadStatus = show != null ? "<green>✓ loaded</green>" : "<red>✗ not loaded</red>";

            String sceneLabel;
            if (show != null && !show.sets.isEmpty()) {
                int n = show.sets.size();
                sceneLabel = n + " scene" + (n != 1 ? "s" : "");
            } else {
                // Parse objectives as fallback for scene count
                try {
                    File objFile = new File(plugin.getDataFolder(), "scout_objectives/" + showId + ".yml");
                    List<ScoutObjective> objectives = parseObjectives(objFile, null);
                    List<String> derived = deriveSceneTagsFromObjectives(objectives);
                    int n = derived.size();
                    sceneLabel = n > 0
                        ? n + " scene" + (n != 1 ? "s" : "") + " <dark_gray>(derived — load show YAML for entry points)</dark_gray>"
                        : "<dark_gray>no scenes defined</dark_gray>";
                } catch (Exception e) {
                    sceneLabel = "<red>parse error</red>";
                }
            }

            Component showName = Component.text(showId)
                .color(NamedTextColor.WHITE)
                .clickEvent(ClickEvent.runCommand("/scaena scout scenes " + showId))
                .hoverEvent(HoverEvent.showText(Component.text("View scenes for " + showId)));

            player.sendMessage(
                Component.text("  ").append(showName)
                    .append(MM.deserialize(
                        "  <gray>" + sceneLabel + "</gray>  " + loadStatus)));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena scout scenes <showId> — per-scene overview
    // -----------------------------------------------------------------------

    public void scenes(Player player, String showId) {
        File objFile = new File(plugin.getDataFolder(), "scout_objectives/" + showId + ".yml");
        if (!objFile.exists()) {
            player.sendMessage(MM.deserialize(
                "<red>No objectives file for <white>" + showId + "</white>.</red>"));
            return;
        }

        List<ScoutObjective> allObjectives;
        try {
            allObjectives = parseObjectives(objFile, null);
        } catch (Exception e) {
            player.sendMessage(MM.deserialize(
                "<red>Failed to parse objectives: " + e.getMessage() + "</red>"));
            return;
        }

        Show show = showRegistry.get(showId);
        List<String> sceneNames;
        String showTitle;

        if (show != null && !show.sets.isEmpty()) {
            sceneNames = new ArrayList<>(show.sets.keySet());
            showTitle  = show.name;
        } else {
            sceneNames = deriveSceneTagsFromObjectives(allObjectives);
            showTitle  = showId;
        }

        if (sceneNames.isEmpty()) {
            String reason = show == null
                ? "Show YAML not loaded and no site tags found in objectives."
                : "No sets defined in show YAML.";
            player.sendMessage(MM.deserialize(
                "<yellow>No scenes found for <white>" + showId + "</white>. " + reason + "</yellow>"));
            return;
        }

        // Load prior captures for capture-count display
        Map<String, ScoutCapture> captureData = readCaptures(showId);

        String noShowNote = show == null ? " <red>(show not loaded — no entry points)</red>" : "";
        player.sendMessage(MM.deserialize(
            "<gold><bold>" + showTitle + "</bold></gold>" + noShowNote));
        player.sendMessage(MM.deserialize(
            "<dark_gray>──────────────────────────────────</dark_gray>"));

        for (String scene : sceneNames) {
            boolean hasSet = show != null && show.sets.containsKey(scene);
            String entryStatus = hasSet ? "<green>✓ entry</green>" : "<red>✗ no entry</red>";

            long total    = allObjectives.stream().filter(o -> o.tags().contains(scene)).count();
            long captured = allObjectives.stream()
                .filter(o -> o.tags().contains(scene))
                .filter(o -> captureData.containsKey(o.name()))
                .count();

            String captureStatus;
            if (total == 0) {
                captureStatus = "<dark_gray>0 marks</dark_gray>";
            } else if (captured == 0) {
                captureStatus = "<red>○ 0/" + total + "</red>";
            } else if (captured == total) {
                captureStatus = "<green>✓ " + captured + "/" + total + "</green>";
            } else {
                captureStatus = "<yellow>◑ " + captured + "/" + total + "</yellow>";
            }

            Component loadBtn = Component.text("[LOAD]")
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/scaena scout load " + showId + " " + scene))
                .hoverEvent(HoverEvent.showText(
                    Component.text("Teleport and load " + formatSceneName(scene))));

            Component line = MM.deserialize(
                "  " + entryStatus + "  "
                + captureStatus + "  "
                + "<white>" + formatSceneName(scene) + "</white>  ");

            player.sendMessage(line.append(loadBtn));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena scout load <showId> [scene]
    // -----------------------------------------------------------------------

    public void load(Player player, String showId, String scene) {
        File objFile = new File(plugin.getDataFolder(), "scout_objectives/" + showId + ".yml");
        if (!objFile.exists()) {
            player.sendMessage(MM.deserialize(
                "<red>No objectives file found for <white>" + showId + "</white>.</red>\n"
                + "<gray>Expected at: plugins/ScaenaShows/scout_objectives/" + showId + ".yml</gray>"));
            return;
        }

        List<ScoutObjective> objectives;
        try {
            objectives = parseObjectives(objFile, scene);
        } catch (Exception e) {
            player.sendMessage(MM.deserialize(
                "<red>Failed to parse objectives: " + e.getMessage() + "</red>"));
            log.warning("[Scout] Failed to parse objectives for " + showId + ": " + e.getMessage());
            return;
        }

        if (objectives.isEmpty()) {
            String msg = scene != null
                ? "<yellow>No objectives match scene <white>" + scene + "</white> for <white>" + showId + "</white>.</yellow>"
                : "<yellow>No objectives found for <white>" + showId + "</white>.</yellow>";
            player.sendMessage(MM.deserialize(msg));
            return;
        }

        // Load prior captures from the most recent save file for this show
        Map<String, ScoutCapture> priorCaptures = readCaptures(showId);

        // Clean up any existing session (silent — we're replacing it)
        if (sessions.containsKey(player.getUniqueId())) {
            dismissSession(player, false);
        }

        // Snapshot and grant flight
        boolean priorAllowFlight = player.getAllowFlight();
        boolean priorFlying      = player.isFlying();
        player.setAllowFlight(true);
        player.setFlying(true);

        // Build sidebar and create session
        Scoreboard previousBoard = player.getScoreboard();
        String sidebarTitle      = scene != null ? formatSceneName(scene) : showId;
        Scoreboard board         = buildSidebar(sidebarTitle, objectives);
        player.setScoreboard(board);

        ScoutSession session = new ScoutSession(
            showId, scene, objectives, priorCaptures,
            previousBoard, board, priorAllowFlight, priorFlying
        );
        sessions.put(player.getUniqueId(), session);

        // ---- Teleport to entry point ----
        boolean teleported = false;
        if (scene != null) {
            Show show = showRegistry.get(showId);
            if (show != null) {
                ShowSet set = show.sets.get(scene);
                if (set != null) {
                    World world = Bukkit.getWorld(set.world());
                    if (world != null) {
                        player.teleport(new Location(
                            world, set.x(), set.y(), set.z(), set.yaw(), set.pitch()));
                        teleported = true;
                    }
                }
            }
        }

        // ---- Confirmation messages ----
        String filterNote = scene != null ? " <gray>[" + formatSceneName(scene) + "]</gray>" : "";
        player.sendMessage(MM.deserialize(
            "<gold>Scout loaded: <white>" + showId + "</white>" + filterNote + "</gold> "
            + "<gray>— " + objectives.size() + " mark(s).</gray>"));

        if (teleported) {
            player.sendMessage(MM.deserialize(
                "<aqua>→ Teleported to entry point for " + formatSceneName(scene) + ".</aqua>"));
        } else if (scene != null) {
            Show show = showRegistry.get(showId);
            if (show == null) {
                player.sendMessage(MM.deserialize(
                    "<yellow>No entry point — show YAML not loaded. "
                    + "Add <white>sets." + scene + "</white> to " + showId + ".yml when ready.</yellow>"));
            } else {
                player.sendMessage(MM.deserialize(
                    "<yellow>No entry point defined for <white>" + scene + "</white>. "
                    + "Add <white>sets." + scene + "</white> to " + showId + ".yml.</yellow>"));
            }
        }

        if (!priorAllowFlight) {
            player.sendMessage(MM.deserialize(
                "<aqua>✈ Flight enabled for scouting. Will be restored when session ends.</aqua>"));
        }

        player.sendMessage(MM.deserialize(
            "<gray>Walk to each mark and run <white>/scaena set <code></white>. "
            + "Use <white>/scaena scout next</white> when done.</gray>"));

        // ---- Spawn markers for prior captures ----
        int markerCount = 0;
        for (ScoutObjective obj : objectives) {
            ScoutCapture prior = priorCaptures.get(obj.name());
            if (prior != null && spawnOneMarker(session, obj, prior, true)) {
                markerCount++;
            }
        }

        if (markerCount > 0) {
            player.sendMessage(MM.deserialize(
                "<dark_gray>" + markerCount
                + " prior capture position(s) shown as markers. "
                + "Recapture to update.</dark_gray>"));
        }
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

        Location loc   = player.getLocation();
        org.bukkit.block.Block block = loc.getBlock();

        // getBiome().getKey().getKey() gives the namespaced path ("sparse_jungle");
        // .toString() in Paper 1.21+ returns a verbose CraftBiome{...} object string.
        String biome        = block.getBiome().getKey().getKey().toUpperCase();
        int    lightLevel   = block.getLightLevel();
        int    skyLight     = block.getLightFromSky();
        int    blockLight   = block.getLightFromBlocks();
        int    ceilHeight   = scanCeiling(loc);
        String skyType      = deriveSkyType(ceilHeight);

        ScoutCapture capture = new ScoutCapture(
            objective.name(),
            loc.getWorld().getName(),
            loc.getX(), loc.getY(), loc.getZ(),
            loc.getYaw(), loc.getPitch(),
            biome, lightLevel, skyLight, blockLight, ceilHeight, skyType
        );
        session.addCapture(capture);

        // Update sidebar — remove the pending entry for this objective
        removeFromSidebar(session.scoutScoreboard(), objective.sidebarEntry());

        // Spawn (or replace) this mark's in-world marker
        if (session.markersVisible()) {
            spawnOneMarker(session, objective, capture, false);
        }

        // Confirmation
        String coords  = String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
        String ceilStr = ceilHeight < 0 ? "open" : ceilHeight + " blk";
        player.sendMessage(MM.deserialize(
            "<green>✓ <white>" + objective.code() + "</white> "
            + "<gray>" + objective.displayLabel() + " — " + coords + "</gray>"));
        player.sendMessage(MM.deserialize(
            "   <dark_gray>biome:" + biome
            + "  light:" + lightLevel + " (sky:" + skyLight + " blk:" + blockLight + ")"
            + "  ceiling:" + ceilStr + " [" + skyType + "]</dark_gray>"));

        if (session.allCaptured()) {
            player.sendMessage(MM.deserialize(
                "<gold>All marks captured! "
                + "Run <white>/scaena scout save</white> then <white>/scaena scout next</white>.</gold>"));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena scout goto <code>
    // -----------------------------------------------------------------------

    public void gotoMark(Player player, String code) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize("<red>No active scouting session.</red>"));
            return;
        }

        ScoutObjective obj = session.findByCode(code);
        if (obj == null) {
            player.sendMessage(MM.deserialize(
                "<red>Unknown code <white>" + code + "</white>. "
                + "Use <white>/scaena scout status</white> to see available codes.</red>"));
            return;
        }

        ScoutCapture capture = session.findCapture(obj.name());
        if (capture == null) {
            player.sendMessage(MM.deserialize(
                "<yellow>Mark <white>" + code + "</white> not yet captured. "
                + "Walk there and run <white>/scaena set " + code + "</white> first.</yellow>"));
            return;
        }

        World world = Bukkit.getWorld(capture.world());
        if (world == null) {
            player.sendMessage(MM.deserialize(
                "<red>World '<white>" + capture.world() + "</white>' is not loaded.</red>"));
            return;
        }

        // Teleport with exact yaw/pitch from capture — restores the original framing angle
        player.teleport(new Location(
            world, capture.x(), capture.y(), capture.z(),
            capture.yaw(), capture.pitch()));

        boolean isPrior = !session.captures().containsKey(obj.name());
        String  source  = isPrior ? " <dark_gray>(prior capture)</dark_gray>" : "";
        String  coords  = String.format("(%.1f, %.1f, %.1f)", capture.x(), capture.y(), capture.z());
        player.sendMessage(MM.deserialize(
            "<aqua>→ Teleported to <white>" + code + "</white> "
            + "<gray>" + obj.displayLabel() + " " + coords + "</gray>" + source + "</aqua>"));
    }

    // -----------------------------------------------------------------------
    // /scaena scout markers [on|off]
    // -----------------------------------------------------------------------

    public void toggleMarkers(Player player, String toggle) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize("<red>No active scouting session.</red>"));
            return;
        }

        boolean targetVisible;
        if ("on".equals(toggle)) {
            targetVisible = true;
        } else if ("off".equals(toggle)) {
            targetVisible = false;
        } else {
            targetVisible = !session.markersVisible(); // plain toggle
        }

        if (targetVisible == session.markersVisible()) {
            player.sendMessage(MM.deserialize(
                "<gray>Markers are already " + (targetVisible ? "on" : "off") + ".</gray>"));
            return;
        }

        if (!targetVisible) {
            session.despawnMarkers();
            session.setMarkersVisible(false);
            player.sendMessage(MM.deserialize(
                "<gray>Markers hidden. Run <white>/scaena scout markers on</white> to show again.</gray>"));
        } else {
            spawnMarkers(session);
            session.setMarkersVisible(true);
            player.sendMessage(MM.deserialize(
                "<gray>Markers shown — " + session.markerCount() + " position(s).</gray>"));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena scout next — advance to the next scene in show.sets order
    // -----------------------------------------------------------------------

    public void next(Player player) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize(
                "<red>No active session. Use <white>/scaena scout load <showId> [scene]</white> first.</red>"));
            return;
        }

        Show show = showRegistry.get(session.showId());
        if (show == null || show.sets.isEmpty()) {
            player.sendMessage(MM.deserialize(
                "<yellow>Show YAML not loaded — can't determine scene order. "
                + "Use <white>/scaena scout scenes " + session.showId()
                + "</white> and load a scene manually.</yellow>"));
            return;
        }

        List<String> sceneNames  = new ArrayList<>(show.sets.keySet());
        String       currentScene = session.activeScene();
        int          idx          = currentScene != null ? sceneNames.indexOf(currentScene) : -1;

        // If at the last scene, wrap up
        if (idx >= sceneNames.size() - 1) {
            player.sendMessage(MM.deserialize(
                "<gold>All scenes complete for <white>" + session.showId() + "</white>!</gold>"));
            player.sendMessage(MM.deserialize(
                "<gray>Run <white>/scaena scout save</white> to write any remaining captures, "
                + "then <white>/scaena scout dismiss</white>.</gray>"));
            return;
        }

        // Auto-save current captures before moving on
        if (!session.captures().isEmpty()) {
            player.sendMessage(MM.deserialize(
                "<gray>Auto-saving captures before advancing...</gray>"));
            save(player);
        }

        String nextScene = sceneNames.get(idx + 1);
        player.sendMessage(MM.deserialize(
            "<gold>Advancing to <white>" + formatSceneName(nextScene) + "</white>...</gold>"));
        load(player, session.showId(), nextScene);
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
            "<gray>Session still active — keep capturing, "
            + "or <white>/scaena scout next</white> to advance.</gray>"));
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

        int  captured = session.capturedCount();
        int  total    = session.objectives().size();
        long prior    = session.objectives().stream()
            .filter(o -> !session.captures().containsKey(o.name())
                      && session.priorCaptures().containsKey(o.name()))
            .count();

        String sceneStr = session.activeScene() != null
            ? " <gray>[" + formatSceneName(session.activeScene()) + "]</gray>" : "";

        player.sendMessage(MM.deserialize(
            "<gold>Scout: <white>" + session.showId() + "</white>" + sceneStr + "</gold> "
            + "<gray>(" + captured + "/" + total + " captured"
            + (prior > 0 ? ", " + prior + " prior" : "")
            + ")</gray>"));

        for (ScoutObjective obj : session.objectives()) {
            ScoutCapture current = session.captures().get(obj.name());
            ScoutCapture priorC  = session.priorCaptures().get(obj.name());

            String marker;
            String detail     = "";
            String sourceNote = "";

            if (current != null) {
                marker = "<green>✓</green>";
                detail = String.format(
                    " <dark_gray>(%.1f, %.1f, %.1f) [%s]</dark_gray>",
                    current.x(), current.y(), current.z(), current.skyType());
            } else if (priorC != null) {
                // Prior capture from file — indicated with ~ (confirmed position from a previous run)
                marker     = "<dark_green>~</dark_green>";
                detail     = String.format(
                    " <dark_gray>(%.1f, %.1f, %.1f) [%s]</dark_gray>",
                    priorC.x(), priorC.y(), priorC.z(), priorC.skyType());
                sourceNote = " <dark_gray>(prior)</dark_gray>";
            } else {
                marker = "<yellow>○</yellow>";
            }

            player.sendMessage(MM.deserialize(
                "  " + marker + " <white>" + obj.code() + "</white> "
                + "<gray>" + obj.displayLabel() + "</gray>"
                + detail + sourceNote));

            // Show positioning hint only for uncaptured marks — no need to re-read notes for done ones
            if (current == null && priorC == null && obj.note() != null && !obj.note().isBlank()) {
                player.sendMessage(MM.deserialize(
                    "     <dark_gray>↳ " + obj.note() + "</dark_gray>"));
            }
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
    // Lifecycle — called from PlayerLifecycleListener
    // -----------------------------------------------------------------------

    /** Clean up any session on disconnect — no save, no chat message. */
    public void onPlayerQuit(Player player) {
        ScoutSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.despawnMarkers();
            if (!session.priorFlying())      player.setFlying(false);
            if (!session.priorAllowFlight()) player.setAllowFlight(false);
            log.info("[Scout] Session for " + player.getName()
                + " closed on disconnect (captured "
                + session.capturedCount() + "/" + session.objectives().size()
                + " marks — not saved). Flight and markers restored.");
        }
    }

    // -----------------------------------------------------------------------
    // Tab-completion helpers (called by ScoutCommand)
    // -----------------------------------------------------------------------

    /** Returns showIds for which a scout_objectives/*.yml file exists. */
    public List<String> getAvailableShowIds() {
        File dir = new File(plugin.getDataFolder(), "scout_objectives");
        if (!dir.exists()) return List.of();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return List.of();
        return Arrays.stream(files)
            .map(f -> f.getName().replace(".yml", ""))
            .sorted()
            .toList();
    }

    /**
     * Returns scene names for a show, for tab-completing the [scene] argument.
     * Authoritative source is show.sets.keySet(); falls back to deriving from objectives.
     */
    public List<String> getSceneNames(String showId) {
        Show show = showRegistry.get(showId);
        if (show != null && !show.sets.isEmpty()) {
            return new ArrayList<>(show.sets.keySet());
        }
        File objFile = new File(plugin.getDataFolder(), "scout_objectives/" + showId + ".yml");
        if (!objFile.exists()) return List.of();
        try {
            List<ScoutObjective> objectives = parseObjectives(objFile, null);
            return deriveSceneTagsFromObjectives(objectives);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Returns objective codes from the active session, for tab-completing goto/set. */
    public List<String> getActiveObjectiveCodes(Player player) {
        ScoutSession session = sessions.get(player.getUniqueId());
        if (session == null) return List.of();
        return session.objectives().stream().map(ScoutObjective::code).toList();
    }

    // -----------------------------------------------------------------------
    // Internals: markers
    // -----------------------------------------------------------------------

    /**
     * Spawn in-world TextDisplay markers for all objectives in the session
     * that have either a current-session or prior capture.
     * Current-session captures take precedence over prior.
     */
    private void spawnMarkers(ScoutSession session) {
        for (ScoutObjective obj : session.objectives()) {
            ScoutCapture current = session.captures().get(obj.name());
            ScoutCapture prior   = session.priorCaptures().get(obj.name());
            if (current != null) {
                spawnOneMarker(session, obj, current, false);
            } else if (prior != null) {
                spawnOneMarker(session, obj, prior, true);
            }
        }
    }

    /**
     * Spawn a single TextDisplay marker. Returns true if successful.
     * Uses session.addMarker() which auto-removes any existing marker for this objective.
     *
     * @param isPrior true = from a save file (darker style); false = captured this session (bright)
     */
    private boolean spawnOneMarker(
        ScoutSession session, ScoutObjective obj, ScoutCapture capture, boolean isPrior
    ) {
        World world = Bukkit.getWorld(capture.world());
        if (world == null) return false;

        Location loc = new Location(world, capture.x(), capture.y(), capture.z());

        TextDisplay display = (TextDisplay) world.spawnEntity(loc, EntityType.TEXT_DISPLAY);
        display.setPersistent(false);     // don't write to world save
        display.setInvulnerable(true);
        display.setBillboard(Display.Billboard.CENTER); // always faces player
        display.setViewRange(0.75f);      // ~48 blocks (base 64 × 0.75)

        TextColor color = isPrior
            ? TextColor.color(0x888888)           // muted gray for prior captures
            : categoryColor(obj.tags());           // full category color for fresh captures

        String icon = categoryIcon(obj.tags());
        display.text(
            Component.text(icon + " [" + obj.code() + "] " + obj.displayLabel())
                .color(color));

        // Prior captures get a darker background to visually distinguish them
        display.setBackgroundColor(isPrior
            ? Color.fromARGB(160, 30, 30, 30)
            : Color.fromARGB(64, 0, 0, 0));

        session.addMarker(obj.name(), display);
        return true;
    }

    private TextColor categoryColor(List<String> tags) {
        for (String cat : CATEGORY_PRIORITY) {
            if (tags.contains(cat)) return CATEGORY_COLORS.getOrDefault(cat, NamedTextColor.GRAY);
        }
        return NamedTextColor.GRAY;
    }

    private String categoryIcon(List<String> tags) {
        for (String cat : CATEGORY_PRIORITY) {
            if (tags.contains(cat)) return CATEGORY_ICONS.getOrDefault(cat, "●");
        }
        return "●";
    }

    // -----------------------------------------------------------------------
    // Internals: sidebar
    // -----------------------------------------------------------------------

    private Scoreboard buildSidebar(String title, List<ScoutObjective> objectives) {
        ScoreboardManager mgr   = Objects.requireNonNull(Bukkit.getScoreboardManager());
        Scoreboard        board = mgr.getNewScoreboard();

        Objective sidebarObj = board.registerNewObjective(
            "scaena_scout", "dummy",
            Component.text(title, NamedTextColor.GOLD));
        sidebarObj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Higher score = higher sidebar position; first objective gets the highest score
        int total = objectives.size();
        for (int i = 0; i < total; i++) {
            sidebarObj.getScore(objectives.get(i).sidebarEntry()).setScore(total - i);
        }

        return board;
    }

    private void removeFromSidebar(Scoreboard board, String entry) {
        board.resetScores(entry);
    }

    // -----------------------------------------------------------------------
    // Internals: session lifecycle
    // -----------------------------------------------------------------------

    private void dismissSession(Player player, boolean notify) {
        ScoutSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        session.despawnMarkers();
        player.setScoreboard(session.previousScoreboard());

        // Restore flight — must set flying=false before allowFlight=false to prevent kick
        if (!session.priorFlying())      player.setFlying(false);
        if (!session.priorAllowFlight()) player.setAllowFlight(false);

        if (notify) {
            player.sendMessage(MM.deserialize(
                "<gray>Scouting session dismissed. Flight and markers restored.</gray>"));
        }
    }

    // -----------------------------------------------------------------------
    // Internals: capture file I/O
    // -----------------------------------------------------------------------

    /**
     * Read captures from the most recently modified file in scout_captures/[showId]/.
     * Returns an empty map if the directory or files don't exist.
     */
    private Map<String, ScoutCapture> readCaptures(String showId) {
        File captureDir = new File(plugin.getDataFolder(), "scout_captures/" + showId);
        if (!captureDir.exists() || !captureDir.isDirectory()) return Map.of();

        File[] files = captureDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) return Map.of();

        // Most recently modified file wins
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        File latest = files[files.length - 1];

        try {
            return parseCaptureFile(latest);
        } catch (Exception e) {
            log.warning("[Scout] Failed to read captures from "
                + latest.getName() + ": " + e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, ScoutCapture> parseCaptureFile(File file) throws IOException {
        Map<String, Object> root = YamlLoader.load(file);
        Object rawCaptures = root.get("captures");
        if (!(rawCaptures instanceof Map<?, ?> captureMap)) return Map.of();

        Map<String, ScoutCapture> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : captureMap.entrySet()) {
            String name = entry.getKey().toString();
            if (!(entry.getValue() instanceof Map<?, ?> rawData)) continue;
            Map<String, Object> data = (Map<String, Object>) rawData;

            String worldName = str(data, "world");
            if (worldName == null) continue;

            double x     = dbl(data, "x");
            double y     = dbl(data, "y");
            double z     = dbl(data, "z");
            float  yaw   = (float) dbl(data, "yaw");
            float  pitch = (float) dbl(data, "pitch");

            // env block — optional (older capture files may not have it)
            String biome      = "UNKNOWN";
            int    light      = 0, skyLight = 0, blockLight = 0;
            int    ceilHeight = -1;
            String skyType    = "unknown";

            Object rawEnv = data.get("env");
            if (rawEnv instanceof Map<?, ?> envRaw) {
                Map<String, Object> env = (Map<String, Object>) envRaw;
                if (str(env, "biome")    != null) biome    = str(env, "biome");
                light      = intVal(env, "light_level");
                skyLight   = intVal(env, "sky_light");
                blockLight = intVal(env, "block_light");
                // ceiling_height defaults to -1 (open sky) if key absent
                Object cv = env.get("ceiling_height");
                if (cv instanceof Number n) ceilHeight = n.intValue();
                if (str(env, "sky_type") != null) skyType = str(env, "sky_type");
            }

            result.put(name, new ScoutCapture(
                name, worldName, x, y, z, yaw, pitch,
                biome, light, skyLight, blockLight, ceilHeight, skyType));
        }
        return result;
    }

    /** Returns a file path that won't collide — appends -2, -3, etc. if needed. */
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

        Map<String, ScoutObjective> objByName = new HashMap<>();
        for (ScoutObjective o : session.objectives()) objByName.put(o.name(), o);

        for (ScoutCapture c : session.captures().values()) {
            ScoutObjective obj     = objByName.get(c.name());
            boolean        isBlock = obj != null && obj.tags().contains("blocks");

            w.println("  " + c.name() + ":");
            w.println("    world: " + c.world());
            w.printf ("    x: %.4f%n", c.x());
            w.printf ("    y: %.4f%n", c.y());
            w.printf ("    z: %.4f%n", c.z());
            w.printf ("    yaw: %.2f%n",   (double) c.yaw());
            w.printf ("    pitch: %.2f%n", (double) c.pitch());

            if (isBlock) {
                w.printf ("    block_x: %d%n",  (int) Math.round(c.x()));
                w.printf ("    block_y: %d  # derived — confirm in-game%n",
                    (int) Math.floor(c.y()) - 1);
                w.printf ("    block_z: %d%n",  (int) Math.round(c.z()));
            }

            w.println("    env:");
            w.println("      biome: " + c.biome());
            w.println("      light_level: " + c.lightLevel());
            w.println("      sky_light: " + c.skyLight());
            w.println("      block_light: " + c.blockLight());
            String ceilStr = c.ceilingHeight() < 0
                ? "-1  # open sky" : String.valueOf(c.ceilingHeight());
            w.println("      ceiling_height: " + ceilStr);
            w.println("      sky_type: " + c.skyType());
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

            String note = str(entry, "note");
            ScoutObjective objective = new ScoutObjective(code, name, List.copyOf(tags), note);
            if (objective.matchesTag(tagFilter)) {
                result.add(objective);
            }
        }
        return result;
    }

    /**
     * Derive a list of scene tag names from a flat objective list when the show
     * YAML isn't loaded. Scene tags are identified as non-category tags that appear
     * on objectives also tagged "sites" (the player-arrival marker convention).
     *
     * Falls back to all non-category tags if no "sites"-tagged objectives exist.
     */
    private List<String> deriveSceneTagsFromObjectives(List<ScoutObjective> objectives) {
        Set<String> sceneTags = new LinkedHashSet<>();

        for (ScoutObjective obj : objectives) {
            if (obj.tags().contains("sites")) {
                for (String tag : obj.tags()) {
                    if (!KNOWN_CATEGORY_TAGS.contains(tag)) sceneTags.add(tag);
                }
            }
        }

        if (sceneTags.isEmpty()) {
            // Fallback: any non-category tag
            for (ScoutObjective obj : objectives) {
                for (String tag : obj.tags()) {
                    if (!KNOWN_CATEGORY_TAGS.contains(tag)) sceneTags.add(tag);
                }
            }
        }

        return new ArrayList<>(sceneTags);
    }

    // -----------------------------------------------------------------------
    // Internals: environmental scanning
    // -----------------------------------------------------------------------

    private int scanCeiling(Location loc) {
        int startY = loc.getBlockY() + 2;
        int maxY   = loc.getWorld().getMaxHeight();
        for (int y = startY; y < maxY; y++) {
            org.bukkit.block.Block above =
                loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (!above.getType().isAir() && above.getType().isSolid()) {
                return y - startY;
            }
        }
        return -1;
    }

    private String deriveSkyType(int ceilingHeight) {
        if (ceilingHeight < 0)   return "open_sky";
        if (ceilingHeight <= 3)  return "underground";
        if (ceilingHeight <= 15) return "enclosed";
        if (ceilingHeight <= 63) return "partial";
        return "open_sky";
    }

    // -----------------------------------------------------------------------
    // Internals: display formatting
    // -----------------------------------------------------------------------

    /**
     * Convert a scene tag to a human-readable name.
     * site_a → "Site A", site_b → "Site B", my_scene → "my scene".
     */
    private String formatSceneName(String scene) {
        if (scene == null) return "all";
        if (scene.startsWith("site_") && scene.length() == 6) {
            return "Site " + Character.toUpperCase(scene.charAt(5));
        }
        return scene.replace('_', ' ');
    }

    // -----------------------------------------------------------------------
    // Internals: YAML field helpers
    // -----------------------------------------------------------------------

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private double dbl(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    private int intVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.intValue() : 0;
    }
}
