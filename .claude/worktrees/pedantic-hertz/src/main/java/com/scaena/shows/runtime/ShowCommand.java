package com.scaena.shows.runtime;

import com.scaena.shows.ScaenaShowsPlugin;
import com.scaena.shows.config.ScaenaShowsConfig;
import com.scaena.shows.model.ShowMode;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class ShowCommand implements CommandExecutor, TabCompleter {

    private final ScaenaShowsPlugin plugin;
    private final ShowManager showManager;

    public ShowCommand(ScaenaShowsPlugin plugin, ShowManager showManager) {
        this.plugin = plugin;
        this.showManager = showManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Sender wrap = new Sender(sender);

        if (args.length == 0) {
            wrap.sendPlain("Usage: /show list | /show play <showId> <player|@a> [--follow|--static] [--private] | /show stop <player|@a> | /show stopall | /show reload");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        ScaenaShowsConfig c = showManager.config();

        switch (sub) {
            case "list" -> {
                Collection<String> ids = showManager.listShowIds();
                wrap.sendPlain("Shows: " + (ids.isEmpty() ? "(none)" : String.join(", ", ids)));
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission(c.adminPermission())) {
                    wrap.sendPlain("You do not have permission.");
                    return true;
                }
                showManager.reloadAll(wrap);
                return true;
            }

            case "stopall" -> {
                if (!sender.hasPermission(c.adminPermission())) {
                    wrap.sendPlain("You do not have permission.");
                    return true;
                }
                showManager.stopAll("Stopped by admin.");
                wrap.sendPlain("Stopped all running shows.");
                return true;
            }

            case "stop" -> {
                if (args.length < 2) {
                    wrap.sendPlain("Usage: /show stop <player|@a>");
                    return true;
                }
                Collection<Player> targets = resolveTargets(sender, args[1], c);
                if (targets == null) return true;
                showManager.stopTargets(wrap, targets);
                return true;
            }

            case "play" -> {
                if (args.length < 3) {
                    wrap.sendPlain("Usage: /show play <showId> <player|@a> [--follow|--static] [--private]");
                    return true;
                }
                if (!sender.hasPermission(c.playPermission()) && !sender.hasPermission(c.adminPermission())) {
                    wrap.sendPlain("You do not have permission to play shows.");
                    return true;
                }

                String showId = args[1];
                String targetArg = args[2];

                ShowMode mode = null;
                boolean privateFlag = false;
                for (int i = 3; i < args.length; i++) {
                    if ("--follow".equalsIgnoreCase(args[i])) mode = ShowMode.FOLLOW;
                    if ("--static".equalsIgnoreCase(args[i])) mode = ShowMode.STATIC;
                    if ("--private".equalsIgnoreCase(args[i])) privateFlag = true;
                }

                if (privateFlag && !sender.hasPermission(c.privateFlagPermission()) && !sender.hasPermission(c.adminPermission())) {
                    wrap.sendPlain("You do not have permission to use --private.");
                    return true;
                }

                Collection<Player> targets = resolveTargets(sender, targetArg, c);
                if (targets == null) return true;

                Player initiator = (sender instanceof Player p) ? p : null;
                showManager.play(wrap, initiator, showId, targets, mode, privateFlag);
                return true;
            }

            default -> {
                wrap.sendPlain("Unknown subcommand. Try /show list.");
                return true;
            }
        }
    }

    private Collection<Player> resolveTargets(CommandSender sender, String arg, ScaenaShowsConfig c) {
        boolean isAdmin = sender.hasPermission(c.adminPermission());

        if ("@a".equalsIgnoreCase(arg)) {
            if (!isAdmin && !sender.hasPermission(c.targetAllPermission())) {
                sender.sendMessage("You do not have permission to target @a.");
                return null;
            }
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        }

        Player target = Bukkit.getPlayerExact(arg);
        if (target == null) {
            sender.sendMessage("Player not found: " + arg);
            return null;
        }

        if (sender instanceof Player p) {
            if (!isAdmin && !p.hasPermission(c.targetOthersPermission()) && !p.getUniqueId().equals(target.getUniqueId())) {
                sender.sendMessage("You can only target yourself.");
                return null;
            }
        }
        return List.of(target);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ScaenaShowsConfig c = showManager.config();
        if (args.length == 1) {
            return prefix(args[0], List.of("list", "play", "stop", "stopall", "reload"));
        }
        if (args.length == 2 && "play".equalsIgnoreCase(args[0])) {
            return prefix(args[1], new ArrayList<>(showManager.listShowIds()));
        }
        if (args.length == 2 && "stop".equalsIgnoreCase(args[0])) {
            List<String> p = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            p.add("@a");
            return prefix(args[1], p);
        }
        if (args.length == 3 && "play".equalsIgnoreCase(args[0])) {
            List<String> p = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            p.add("@a");
            return prefix(args[2], p);
        }
        if (args.length >= 4 && "play".equalsIgnoreCase(args[0])) {
            List<String> flags = new ArrayList<>(List.of("--follow", "--static"));
            if (sender.hasPermission(c.privateFlagPermission()) || sender.hasPermission(c.adminPermission())) flags.add("--private");
            return prefix(args[args.length - 1], flags);
        }
        return List.of();
    }

    private static List<String> prefix(String token, List<String> options) {
        String t = token == null ? "" : token.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(t)).sorted().collect(Collectors.toList());
    }

    private static final class Sender implements CommandSenderLike {
        private final CommandSender sender;

        private Sender(CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void sendPlain(String message) {
            sender.sendMessage(message);
        }
    }
}
