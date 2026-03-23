package com.scaena.shows.runtime;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ShowListener implements Listener {

    private final ShowManager showManager;

    public ShowListener(ShowManager showManager) {
        this.showManager = showManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        showManager.pause(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Show broadcast bossbars to joiner
        showManager.showBossbarsToJoiner(e.getPlayer());
        // Resume if needed
        showManager.resume(e.getPlayer());
    }
}
