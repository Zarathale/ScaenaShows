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
 * Handles the /scaena command and all scouting subcommands:
 *
 *   /scaena scout load <showId> [tag]   — load objectives, show sidebar
 *   /scaena scout save                  — write captures to file
 *   /scaena scout status                — show what's captured vs. outstanding
 *   /scaena scout dismiss               — hide sidebar and end session
 *   /scaena set <code>                  — capture current position for a mark
 *
 * The /scaena set verb is intentionally top-level (not nested under scout)
 * because it's typed constantly during scouting and must be as short as possible.
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
            sendHelp(player);
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
            sendHelp(player);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "load"    -> cmdLoad(player, args);
            case "save"    -> scoutManager.save(player);
            case "status"  -> scoutManager.status(player);
            case "dismiss" -> scoutManager.dismiss(player);
            default        -> sendHelp(player);
        }
    }

    /** /scaena scout load <showId> [tag] */
    private void cmdLoad(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MM.deserialize(
                "<red>Usage: /scaena scout load <showId> [tag]</red>"));
            return;
        }
        String showId    = args[2];
        String tagFilter = args.length >= 4 ? args[3] : null;
        scoutManager.load(player, showId, tagFilter);
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
        player.sendMessage(MM.deserialize("<gold><bold>Scaena Scout</bold></gold>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout load <showId> [tag]</white> <gray>— load objectives and show sidebar</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena set <code></white> <gray>— capture your current position for that mark</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout save</white> <gray>— write all captures to file</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout status</white> <gray>— show captured vs. outstanding marks</gray>"));
        player.sendMessage(MM.deserialize(
            "  <white>/scaena scout dismiss</white> <gray>— hide sidebar and end session</gray>"));
    }

    // -----------------------------------------------------------------------
    // Tab completion
    // -----------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(List.of("scout", "set"), args[0]);
        }
        if (args.length == 2 && "scout".equalsIgnoreCase(args[0])) {
            return filterStartsWith(List.of("load", "save", "status", "dismiss"), args[1]);
        }
        // args[2] for "scout load" would ideally suggest show IDs — deferred; requires ShowRegistry ref
        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        return options.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .toList();
    }
}
