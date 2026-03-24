package com.scaena.shows.runtime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves YAML audience strings to a live list of players for a given RunningShow.
 *
 * Audience values (spec §7):
 *   broadcast   — all online players (clamped to participants if show is --private)
 *   participants— all participants in this show
 *   invoker     — the invoker (same as private / target)
 *   private     — alias for invoker
 *   target      — alias for invoker (legacy)
 *   group_1–4   — subset of participants
 */
public final class AudienceResolver {

    private AudienceResolver() {}

    /**
     * Returns the live list of Players that should receive the event.
     * Only returns currently-online players.
     */
    public static List<Player> resolve(String audience, RunningShow show) {
        if (audience == null) audience = "broadcast";

        return switch (audience.toLowerCase()) {
            case "broadcast" -> {
                // GROUP_EVENT scope: restrict broadcast to one group's members
                if (show.getActiveGroupScope() > 0) {
                    yield show.getGroupPlayers(show.getActiveGroupScope());
                }
                if (show.isPrivate()) {
                    // --private clamps broadcast to participants only
                    yield participantsOnline(show);
                }
                yield new ArrayList<>(Bukkit.getOnlinePlayers());
            }
            case "participants" -> {
                // GROUP_EVENT scope: restrict participants to one group's members
                if (show.getActiveGroupScope() > 0) {
                    yield show.getGroupPlayers(show.getActiveGroupScope());
                }
                yield participantsOnline(show);
            }
            case "invoker", "private", "target" -> {
                Player inv = show.getInvoker();
                yield (inv != null && inv.isOnline()) ? List.of(inv) : List.of();
            }
            case "group_1" -> show.getGroupPlayers(1);
            case "group_2" -> show.getGroupPlayers(2);
            case "group_3" -> show.getGroupPlayers(3);
            case "group_4" -> show.getGroupPlayers(4);
            default -> {
                // Unrecognised audience — return empty, warn upstream
                yield List.of();
            }
        };
    }

    private static List<Player> participantsOnline(RunningShow show) {
        List<Player> out = new ArrayList<>();
        for (ParticipantState ps : show.getParticipants().values()) {
            if (!ps.isConnected()) continue;
            Player p = Bukkit.getPlayer(ps.uuid);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }
}
