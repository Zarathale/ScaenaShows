package com.scaena.shows.command;

import com.scaena.shows.runtime.ShowManager;
import com.scaena.shows.scout.ScoutManager;
import com.scaena.shows.tech.TechManager;
import com.scaena.shows.tech.TechPanelBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handles the /scaena command and all scouting subcommands.
 *
 * Command surface:
 *
 *   /scaena                               — help + session summary if active
 *   /scaena scout shows                   — list shows with objectives defined
 *   /scaena scout scenes <showId>         — scene overview: entry points, capture counts, click-to-load
 *   /scaena scout load <showId> [scene]   — load scene, teleport to entry, spawn markers
 *   /scaena scout next                    — auto-save and advance to the next scene
 *   /scaena scout status                  — detailed mark list for current session
 *   /scaena scout goto <code>             — teleport to a captured mark (yaw/pitch preserved)
 *   /scaena scout markers [on|off]        — toggle in-world TextDisplay markers
 *   /scaena scout save                    — write session captures to file
 *   /scaena scout dismiss                 — end session, restore flight and scoreboard
 *   /scaena set <code>                    — capture current position for a mark
 *
 * The /scaena set verb is intentionally top-level (not under scout) because it
 * is typed constantly during a scouting run and must be as short as possible.
 */
public final class ScoutCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM   = MiniMessage.miniMessage();
    private static final String      PERM = "scae.shows.scout";

    private final ScoutManager scoutManager;
    private final ShowManager  showManager;
    private final TechManager  techManager;

    public ScoutCommand(ScoutManager scoutManager, ShowManager showManager,
                        TechManager techManager) {
        this.scoutManager = scoutManager;
        this.showManager  = showManager;
        this.techManager  = techManager;
    }

    // -----------------------------------------------------------------------
    // Command dispatch
    // -----------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /scaena commands.");
            return true;
        }
        if (!player.hasPermission(PERM) && !player.hasPermission("scae.shows.admin")) {
            player.sendMessage(MM.deserialize(
                "<red>You don't have permission to use scouting commands.</red>"));
            return true;
        }

        if (args.length == 0) {
            scoutManager.summary(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "scout"  -> handleScout(player, args);
            case "set"    -> handleSet(player, args);
            case "snap"   -> handleSnap(player, args);
            case "choose" -> handleChoose(player, args);
            case "tech"   -> handleTech(player, args);
            default       -> sendHelp(player);
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // /scaena scout <subcommand>
    // -----------------------------------------------------------------------

    private void handleScout(Player player, String[] args) {
        if (args.length < 2) {
            scoutManager.summary(player);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "load"    -> cmdLoad(player, args);
            case "save"    -> scoutManager.save(player);
            case "status"  -> scoutManager.status(player);
            case "dismiss" -> scoutManager.dismiss(player);
            case "shows"   -> scoutManager.shows(player);
            case "scenes"  -> cmdScenes(player, args);
            case "goto"    -> cmdGoto(player, args);
            case "markers" -> cmdMarkers(player, args);
            case "next"    -> scoutManager.next(player);
            default        -> sendHelp(player);
        }
    }

    /** /scaena scout load <showId> [scene] */
    private void cmdLoad(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MM.deserialize(
                "<red>Usage: /scaena scout load <showId> [scene]</red>"));
            return;
        }
        String showId = args[2];
        String scene  = args.length >= 4 ? args[3] : null;
        scoutManager.load(player, showId, scene);
    }

    /** /scaena scout scenes <showId> */
    private void cmdScenes(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MM.deserialize(
                "<red>Usage: /scaena scout scenes <showId></red>"));
            return;
        }
        scoutManager.scenes(player, args[2]);
    }

    /** /scaena scout goto <code> */
    private void cmdGoto(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MM.deserialize(
                "<red>Usage: /scaena scout goto <code></red>"));
            return;
        }
        scoutManager.gotoMark(player, args[2]);
    }

    /** /scaena scout markers [on|off] — no argument toggles current state */
    private void cmdMarkers(Player player, String[] args) {
        String toggle = args.length >= 3 ? args[2].toLowerCase() : null;
        scoutManager.toggleMarkers(player, toggle);
    }

    // -----------------------------------------------------------------------
    // /scaena set <code>
    // -----------------------------------------------------------------------

    private void handleSet(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MM.deserialize(
                "<red>Usage: /scaena set <code></red>"));
            return;
        }
        scoutManager.capture(player, args[1]);
    }

    // -----------------------------------------------------------------------
    // /scaena snap [label | list]
    // -----------------------------------------------------------------------

    /**
     * /scaena snap [label]  — log a snapshot moment and prompt F2
     * /scaena snap list     — print the snapshot log for this session
     *
     * Label is freeform (space-separated words become underscored slug).
     * If omitted, an auto-generated name is used.
     */
    private void handleSnap(Player player, String[] args) {
        // /scaena snap list
        if (args.length >= 2 && "list".equalsIgnoreCase(args[1])) {
            scoutManager.snapList(player);
            return;
        }
        // /scaena snap [label parts...]
        String rawLabel = args.length >= 2
            ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
            : null;
        scoutManager.snap(player, rawLabel);
    }

    // -----------------------------------------------------------------------
    // /scaena choose <index|stop>
    // -----------------------------------------------------------------------

    /**
     * Routes a PLAYER_CHOICE selection from the clickable chat buttons.
     * The click event fires "/scaena choose 0", "/scaena choose 1", or "/scaena choose stop".
     *
     * This verb intentionally lives on /scaena (not /show) so it's available
     * even when the player doesn't have full admin permissions — only scae.shows.scout
     * is required, which show participants should have.
     */
    private void handleChoose(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MM.deserialize(
                "<red>Usage: /scaena choose <index|stop></red>"));
            return;
        }
        String rawChoice = args[1];
        String error = showManager.handleChoiceCommand(player, rawChoice);
        if (error != null) {
            player.sendMessage(MM.deserialize("<red>" + error + "</red>"));
        }
    }

    // -----------------------------------------------------------------------
    // /scaena tech <showId> [sceneId]  — enter / control tech session
    // -----------------------------------------------------------------------

    /**
     * Routes all /scaena tech ... commands.
     *
     * Entry:
     *   /scaena tech <showId> [sceneId]      — enter tech mode
     *
     * Session control (also sent by panel click events):
     *   /scaena tech dismiss                 — exit (prompts save if dirty)
     *   /scaena tech discard                 — discard changes + exit
     *   /scaena tech saveanddismiss          — save + exit
     *   /scaena tech save                    — save without dismissing
     *   /scaena tech panel                   — re-send main panel
     *   /scaena tech params                  — open param panel
     *   /scaena tech marklist                — open mark list panel
     *   /scaena tech capture <mark>          — enter capture mode for mark
     *   /scaena tech toggle <dept>           — toggle department on/off
     *   /scaena tech focusparam <name>       — focus a param for editing
     */
    private void handleTech(Player player, String[] args) {
        if (args.length < 2) {
            // Show session summary or brief help
            if (techManager.hasSession(player)) {
                var session = techManager.getSession(player);
                player.sendMessage(MM.deserialize(
                    "<gold>Tech active:</gold> <white>" + session.showId() + "</white>"
                    + (session.currentSceneId() != null
                        ? " @ " + session.currentSceneId() : "")
                    + (session.hasUnsavedChanges()
                        ? "  <yellow>✎ unsaved changes</yellow>" : "")));
                TechPanelBuilder.send(player, session);
            } else {
                player.sendMessage(MM.deserialize(
                    "<gold>Tech Rehearsal Mode</gold> <gray>— /scaena tech <showId> [sceneId]</gray>"));
            }
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "dismiss"        -> techManager.dismiss(player);
            case "discard"        -> techManager.discardAndDismiss(player);
            case "saveanddismiss" -> techManager.saveAndDismiss(player);
            case "save"           -> techManager.save(player);
            case "panel"          -> {
                var session = techManager.getSession(player);
                if (session != null) TechPanelBuilder.send(player, session);
            }
            case "params"         -> {
                var session = techManager.getSession(player);
                if (session != null) TechPanelBuilder.sendParamPanel(player, session);
            }
            case "marklist"       -> {
                var session = techManager.getSession(player);
                if (session != null) TechPanelBuilder.sendMarkList(player, session);
            }
            case "capture"        -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize(
                        "<red>Usage: /scaena tech capture <markName></red>"));
                    return;
                }
                techManager.startCaptureMode(player, args[2]);
            }
            case "captureexit"    -> techManager.exitCaptureMode(player);
            case "build"          -> techManager.toggleBuildMode(player);
            case "bbox"           -> {
                if (args.length < 3 || (!args[2].equalsIgnoreCase("min")
                                     && !args[2].equalsIgnoreCase("max"))) {
                    player.sendMessage(MM.deserialize(
                        "<red>Usage: /scaena tech bbox <min|max></red>"));
                    return;
                }
                techManager.setBboxCorner(player, args[2].equalsIgnoreCase("min"));
            }
            case "buildsave"      -> techManager.saveBuild(player);
            case "builddiscard"   -> techManager.discardBuild(player);
            case "buildrestore"   -> techManager.restoreBuildRemovals(player);
            case "builds"         -> techManager.sendBuildVersions(player);
            case "setbuild"       -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize(
                        "<red>Usage: /scaena tech setbuild <versionStem></red>"));
                    return;
                }
                techManager.setActiveBuild(player, args[2]);
            }
            case "toggle"         -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize(
                        "<red>Usage: /scaena tech toggle <dept></red>"));
                    return;
                }
                techManager.toggleDept(player, args[2].toLowerCase());
            }
            case "focusparam"     -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize(
                        "<red>Usage: /scaena tech focusparam <paramName></red>"));
                    return;
                }
                techManager.focusParam(player, args[2]);
            }
            default -> {
                // Treat as /scaena tech <showId> [sceneId]
                String showId  = args[1];
                String sceneId = args.length >= 3 ? args[2] : null;
                techManager.enterTech(player, showId, sceneId);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Help
    // -----------------------------------------------------------------------

    private void sendHelp(Player player) {
        player.sendMessage(MM.deserialize("<gold><bold>Scaena Scout — commands</bold></gold>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout shows</white> "
            + "<gray>— list shows with objectives defined</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout scenes <showId></white> "
            + "<gray>— scene overview: entry points, capture status</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout load <showId> [scene]</white> "
            + "<gray>— load scene, teleport to entry, show markers</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout next</white> "
            + "<gray>— auto-save and advance to the next scene</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena set <code></white> "
            + "<gray>— capture your position for that mark</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout goto <code></white> "
            + "<gray>— teleport to a captured mark</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout markers [on|off]</white> "
            + "<gray>— toggle in-world position markers</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout status</white> "
            + "<gray>— detailed mark list for the current session</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout save</white> "
            + "<gray>— write captures to file</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout dismiss</white> "
            + "<gray>— end session and restore state</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena snap [label]</white> "
            + "<gray>— log a snapshot moment, prompt F2 (label is optional)</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena snap list</white> "
            + "<gray>— show snapshot log for this session</gray>"));
    }

    // -----------------------------------------------------------------------
    // Tab completion
    // -----------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(
        CommandSender sender, Command command, String alias, String[] args
    ) {
        if (!(sender instanceof Player player)) return List.of();

        // /scaena <verb>
        if (args.length == 1) {
            return filter(List.of("scout", "set", "snap", "choose", "tech"), args[0]);
        }

        // /scaena scout <subcommand>
        if ("scout".equalsIgnoreCase(args[0]) && args.length == 2) {
            return filter(List.of(
                "load", "save", "status", "dismiss",
                "shows", "scenes", "goto", "markers", "next"
            ), args[1]);
        }

        // /scaena scout <subcommand> <arg...>
        if ("scout".equalsIgnoreCase(args[0]) && args.length >= 3) {
            return switch (args[1].toLowerCase()) {

                // /scaena scout load <showId> [scene]
                case "load" -> {
                    if (args.length == 3)
                        yield filter(scoutManager.getAvailableShowIds(), args[2]);
                    if (args.length == 4)
                        yield filter(scoutManager.getSceneNames(args[2]), args[3]);
                    yield List.of();
                }

                // /scaena scout scenes <showId>
                case "scenes" -> args.length == 3
                    ? filter(scoutManager.getAvailableShowIds(), args[2])
                    : List.of();

                // /scaena scout goto <code>
                case "goto" -> args.length == 3
                    ? filter(scoutManager.getActiveObjectiveCodes(player), args[2])
                    : List.of();

                // /scaena scout markers [on|off]
                case "markers" -> args.length == 3
                    ? filter(List.of("on", "off"), args[2])
                    : List.of();

                default -> List.of();
            };
        }

        // /scaena set <code>  — suggest pending codes from the active session
        if ("set".equalsIgnoreCase(args[0]) && args.length == 2) {
            return filter(scoutManager.getActiveObjectiveCodes(player), args[1]);
        }

        // /scaena snap list  — only first arg is completable
        if ("snap".equalsIgnoreCase(args[0]) && args.length == 2) {
            return filter(List.of("list"), args[1]);
        }

        // /scaena tech <showId> [sceneId]
        if ("tech".equalsIgnoreCase(args[0])) {
            if (args.length == 2) {
                List<String> subs = List.of("dismiss", "save", "discard", "saveanddismiss",
                    "panel", "params", "marklist", "toggle", "capture", "focusparam", "build");
                List<String> shows = techManager.getAvailableShowIds();
                List<String> combined = new java.util.ArrayList<>(subs);
                combined.addAll(shows);
                return filter(combined, args[1]);
            }
            if (args.length == 3) {
                String sub = args[1].toLowerCase();
                if (!List.of("dismiss","save","discard","saveanddismiss",
                        "panel","params","marklist","build").contains(sub)) {
                    // Could be showId + sceneId, or toggle <dept>, or capture <mark>
                    if ("toggle".equals(sub)) {
                        return filter(List.of("casting","wardrobe","set","lighting",
                            "fireworks","script"), args[2]);
                    }
                    if ("capture".equals(sub) || "focusparam".equals(sub)) {
                        return List.of(); // dynamic — skip for now
                    }
                    // Treat arg[1] as showId; complete scene IDs
                    return filter(techManager.getSceneIds(args[1]), args[2]);
                }
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        return options.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .toList();
    }
}
