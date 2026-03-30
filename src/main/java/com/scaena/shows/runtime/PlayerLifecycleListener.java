package com.scaena.shows.runtime;

import com.scaena.shows.scout.ScoutManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for player connect/disconnect events to handle pause/resume
 * of shows and spatial anchor fallback (spec §15 pause/resume), and
 * to clean up any active scout sessions on disconnect.
 */
public final class PlayerLifecycleListener implements Listener {

    private final ShowManager  showManager;
    private final ScoutManager scoutManager;

    public PlayerLifecycleListener(ShowManager showManager, ScoutManager scoutManager) {
        this.showManager  = showManager;
        this.scoutManager = scoutManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        showManager.onPlayerDisconnect(event.getPlayer());
        scoutManager.onPlayerQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        showManager.onPlayerReconnect(event.getPlayer());
    }
}
