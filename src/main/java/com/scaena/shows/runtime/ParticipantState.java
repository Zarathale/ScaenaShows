package com.scaena.shows.runtime;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Per-participant runtime state captured at show invocation.
 */
public final class ParticipantState {

    public final UUID uuid;
    public final String name;

    /** Home position captured at show invocation (before tick 0). */
    public Location home;

    /** Group number (1–N) assigned by GROUP_ASSIGN; 0 = not yet assigned. */
    public int groupNumber = 0;

    /** True if this participant is the spatial anchor (first named target). */
    public final boolean isSpatialAnchor;

    /** Gamemode before any PLAYER_SPECTATE — restored on cleanup. */
    public GameMode priorGameMode = null;

    /** Tick at which this participant disconnected; -1 = connected. */
    public long disconnectedAtTick = -1;

    /** System time (ms) of disconnect, for resume window calculation. */
    public long disconnectedAtMs = -1;

    public ParticipantState(Player player, boolean isSpatialAnchor) {
        this.uuid            = player.getUniqueId();
        this.name            = player.getName();
        this.home            = player.getLocation().clone();
        this.isSpatialAnchor = isSpatialAnchor;
    }

    public boolean isConnected() {
        return disconnectedAtTick < 0;
    }

    public void markDisconnected(long currentTick) {
        this.disconnectedAtTick = currentTick;
        this.disconnectedAtMs   = System.currentTimeMillis();
    }

    public void markReconnected(Player player) {
        // On rejoin, capture new home at reconnect location (spec §15 pause/resume)
        this.home               = player.getLocation().clone();
        this.disconnectedAtTick = -1;
        this.disconnectedAtMs   = -1;
    }
}
