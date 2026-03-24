package com.scaena.shows.runtime;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for player connect/disconnect events to handle pause/resume
 * of shows and spatial anchor fallback (spec §15 pause/resume).
 */
public final class PlayerLifecycleListener implements Listener {

    private final ShowManager showManager;

    public PlayerLifecycleListener(ShowManager showManager) {
        this.showManager = showManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        showManager.onPlayerDisconnect(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        showManager.onPlayerReconnect(event.getPlayer());
    }
}
