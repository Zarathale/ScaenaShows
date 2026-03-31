package com.scaena.shows.command;

import com.scaena.shows.scout.ScoutManager;
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

    public ScoutCommand(ScoutManager scoutManager) {
        this.scoutManager = scoutManager;
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
            case "scout" -> handleScout(player, args);
            case "set"   -> handleSet(player, args);
            default      -> sendHelp(player);
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
            return filter(List.of("scout", "set"), args[0]);
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

        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        return options.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .toList();
    }
}
