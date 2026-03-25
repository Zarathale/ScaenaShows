package com.scaena.shows.command;

import com.scaena.shows.model.Show;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.registry.FireworkRegistry;
import com.scaena.shows.registry.ShowRegistry;
import com.scaena.shows.runtime.ShowManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the /show command and all subcommands:
 *   /show list
 *   /show play <showId> <target> [<target2>...] [--follow|--static] [--private] [--scenes]
 *   /show stop [player|@a]   (no arg = stop all)
 *   /show stopall
 *   /show reload
 */
public final class ShowCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final ShowRegistry showRegistry;
    private final CueRegistry cueRegistry;
    private final FireworkRegistry fireworkRegistry;
    private final ShowManager showManager;

    public ShowCommand(
        JavaPlugin plugin,
        ShowRegistry showRegistry,
        CueRegistry cueRegistry,
        FireworkRegistry fireworkRegistry,
        ShowManager showManager
    ) {
        this.plugin           = plugin;
        this.showRegistry     = showRegistry;
        this.cueRegistry      = cueRegistry;
        this.fireworkRegistry = fireworkRegistry;
        this.showManager      = showManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list"   -> cmdList(sender);
            case "play"   -> cmdPlay(sender, args);
            case "stop"   -> cmdStop(sender, args);
            case "stopall"-> cmdStopAll(sender);
            case "reload" -> cmdReload(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // /show list
    // ------------------------------------------------------------------
    private void cmdList(CommandSender sender) {
        if (!sender.hasPermission("scae.shows.play") && !sender.hasPermission("scae.shows.admin")) {
            sender.sendMessage(MM.deserialize("<red>You don't have permission to list shows.</red>"));
            return;
        }

        Map<String, Show> shows = showRegistry.getAll();
        if (shows.isEmpty()) {
            sender.sendMessage(MM.deserialize("<yellow>No shows are loaded.</yellow>"));
            return;
        }

        sender.sendMessage(MM.deserialize("<gold><bold>ScaenaShows</bold></gold> <gray>— " + shows.size() + " show(s):</gray>"));
        for (Show show : shows.values()) {
            String duration = show.durationTicks > 0
                ? String.format("%.1fs", show.durationTicks / 20.0)
                : "?";
            sender.sendMessage(MM.deserialize(
                "  <white>" + show.id + "</white> <gray>— " + show.name
                    + " (" + duration + ")</gray>"));
        }
    }

    // ------------------------------------------------------------------
    // /show play <showId> <target> [targets...] [--follow|--static] [--private] [--scenes]
    // ------------------------------------------------------------------
    private void cmdPlay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player invoker)) {
            sender.sendMessage("Only players can run /show play.");
            return;
        }

        if (!invoker.hasPermission("scae.shows.play") && !invoker.hasPermission("scae.shows.admin")) {
            invoker.sendMessage(MM.deserialize(
                "<red>You can't play shows. You may need additional access.</red>"));
            return;
        }

        if (args.length < 2) {
            invoker.sendMessage(MM.deserialize(
                "<red>Usage: /show play <showId> [target...] [--follow|--static] [--private] [--scenes]</red>"));
            return;
        }

        String showId = args[1];
        Show show = showRegistry.get(showId);
        if (show == null) {
            invoker.sendMessage(MM.deserialize("<red>Show not found: '" + showId + "'</red>"));
            return;
        }

        // Parse flags and targets from remaining args
        List<String> targetArgs = new ArrayList<>();
        boolean privateMode = false;
        boolean scenesMode  = false;
        String  forceMode   = null;

        for (int i = 2; i < args.length; i++) {
            String a = args[i];
            switch (a.toLowerCase()) {
                case "--private" -> {
                    if (!invoker.hasPermission("scae.shows.private") && !invoker.hasPermission("scae.shows.admin")) {
                        invoker.sendMessage(MM.deserialize("<red>You don't have permission to use --private.</red>"));
                        return;
                    }
                    privateMode = true;
                }
                case "--scenes" -> {
                    if (!invoker.hasPermission("scae.shows.scenes") && !invoker.hasPermission("scae.shows.admin")) {
                        invoker.sendMessage(MM.deserialize("<red>You don't have permission to use --scenes.</red>"));
                        return;
                    }
                    scenesMode = true;
                }
                case "--follow" -> forceMode = "follow";
                case "--static" -> forceMode = "static";
                default         -> targetArgs.add(a);
            }
        }

        // Resolve targets to players
        List<Player> participants = new ArrayList<>();
        for (String target : targetArgs) {
            if (target.startsWith("@")) {
                // Selector — requires target.all permission
                if (!invoker.hasPermission("scae.shows.target.all") && !invoker.hasPermission("scae.shows.admin")) {
                    invoker.sendMessage(MM.deserialize(
                        "<red>You can run shows for yourself. Targeting selectors requires additional access.</red>"));
                    return;
                }
                if (target.equals("@a")) {
                    participants.addAll(Bukkit.getOnlinePlayers());
                } else {
                    // @a[distance=..N] and other selectors
                    participants.addAll(Bukkit.selectEntities(invoker, target).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .toList());
                }
            } else {
                // Named player
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer == null) {
                    invoker.sendMessage(MM.deserialize("<red>Player not found: '" + target + "'</red>"));
                    return;
                }
                // Check targeting.others permission
                if (!targetPlayer.getUniqueId().equals(invoker.getUniqueId())) {
                    if (!invoker.hasPermission("scae.shows.target.others")
                            && !invoker.hasPermission("scae.shows.admin")) {
                        invoker.sendMessage(MM.deserialize(
                            "<red>You can run shows for yourself. Targeting others requires additional access.</red>"));
                        return;
                    }
                }
                participants.add(targetPlayer);
            }
        }

        // Deduplicate
        List<Player> unique = participants.stream().distinct().toList();

        // No targets specified or resolved — default to self
        if (unique.isEmpty()) {
            unique = List.of(invoker);
        }

        String error = showManager.startShow(show, unique, invoker, privateMode, scenesMode, forceMode);
        if (error != null) {
            invoker.sendMessage(MM.deserialize("<red>" + error + "</red>"));
        } else {
            invoker.sendMessage(MM.deserialize(
                "<gold>Starting show <white>" + show.name + "</white> for "
                    + unique.size() + " player(s).</gold>"));
        }
    }

    // ------------------------------------------------------------------
    // /show stop [player|@a]   (no arg = stop all)
    // ------------------------------------------------------------------
    private void cmdStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scae.shows.admin")) {
            sender.sendMessage(MM.deserialize("<red>You don't have permission to stop shows.</red>"));
            return;
        }

        // No target — stop everything
        if (args.length < 2) {
            showManager.stopAll();
            sender.sendMessage(MM.deserialize("<gold>All shows stopped.</gold>"));
            return;
        }

        String target = args[1];
        if (target.equals("@a")) {
            showManager.stopAll();
            sender.sendMessage(MM.deserialize("<gold>All shows stopped.</gold>"));
            return;
        }

        Player p = Bukkit.getPlayer(target);
        if (p == null) {
            sender.sendMessage(MM.deserialize("<red>Player not found: '" + target + "'</red>"));
            return;
        }

        boolean stopped = showManager.stopForPlayer(p);
        if (stopped) {
            sender.sendMessage(MM.deserialize("<gold>Stopped show for " + p.getName() + ".</gold>"));
        } else {
            sender.sendMessage(MM.deserialize("<yellow>" + p.getName() + " is not in a running show.</yellow>"));
        }
    }

    // ------------------------------------------------------------------
    // /show stopall
    // ------------------------------------------------------------------
    private void cmdStopAll(CommandSender sender) {
        if (!sender.hasPermission("scae.shows.admin")) {
            sender.sendMessage(MM.deserialize("<red>You don't have permission to stop shows.</red>"));
            return;
        }
        showManager.stopAll();
        sender.sendMessage(MM.deserialize("<gold>All shows stopped.</gold>"));
    }

    // ------------------------------------------------------------------
    // /show reload
    // ------------------------------------------------------------------
    private void cmdReload(CommandSender sender) {
        if (!sender.hasPermission("scae.shows.admin")) {
            sender.sendMessage(MM.deserialize("<red>You don't have permission to reload shows.</red>"));
            return;
        }

        sender.sendMessage(MM.deserialize("<yellow>Reloading ScaenaShows...</yellow>"));

        // Stop all running shows before reload
        showManager.stopAll();

        // Reload registries
        java.io.File dataFolder = plugin.getDataFolder();
        fireworkRegistry.load(new java.io.File(dataFolder, "fireworks.yml"));
        cueRegistry.load(new java.io.File(dataFolder, "cues"));
        showRegistry.load(new java.io.File(dataFolder, "shows"));

        sender.sendMessage(MM.deserialize(
            "<gold>Reloaded: <white>" + showRegistry.getAll().size() + " show(s), "
                + cueRegistry.getAll().size() + " cue(s), "
                + fireworkRegistry.getAll().size() + " firework preset(s).</white></gold>"));
    }

    // ------------------------------------------------------------------
    // Help
    // ------------------------------------------------------------------
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MM.deserialize("<gold><bold>ScaenaShows v2</bold></gold>"));
        sender.sendMessage(MM.deserialize("  <white>/show list</white> <gray>— list loaded shows</gray>"));
        sender.sendMessage(MM.deserialize("  <white>/show play <showId> [target]</white> <gray>— play a show (defaults to self)</gray>"));
        sender.sendMessage(MM.deserialize("  <white>/show stop [player|@a]</white> <gray>— stop a show (no arg = stop all)</gray>"));
        sender.sendMessage(MM.deserialize("  <white>/show stopall</white> <gray>— stop all running shows</gray>"));
        sender.sendMessage(MM.deserialize("  <white>/show reload</white> <gray>— hot-reload all YAML</gray>"));
    }

    // ------------------------------------------------------------------
    // Tab completion
    // ------------------------------------------------------------------
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(List.of("list", "play", "stop", "stopall", "reload"), args[0]);
        }
        if (args.length == 2 && "play".equalsIgnoreCase(args[0])) {
            return filterStartsWith(new ArrayList<>(showRegistry.getAll().keySet()), args[1]);
        }
        if (args.length >= 3 && "play".equalsIgnoreCase(args[0])) {
            List<String> completions = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
            completions.addAll(List.of("--follow", "--static", "--private", "--scenes"));
            return filterStartsWith(completions, args[args.length - 1]);
        }
        if (args.length == 2 && "stop".equalsIgnoreCase(args[0])) {
            List<String> completions = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
            completions.add("@a");
            return filterStartsWith(completions, args[1]);
        }
        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        return options.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .toList();
    }
}
