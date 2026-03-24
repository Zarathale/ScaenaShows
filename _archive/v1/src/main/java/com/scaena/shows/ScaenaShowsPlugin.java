package com.scaena.shows;

import com.scaena.shows.config.ConfigLoader;
import com.scaena.shows.registry.FireworkPresetRegistry;
import com.scaena.shows.registry.SceneRegistry;
import com.scaena.shows.registry.SequenceRegistry;
import com.scaena.shows.registry.ShowRegistry;
import com.scaena.shows.runtime.ShowManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ScaenaShowsPlugin extends JavaPlugin {

    private ConfigLoader configLoader;
    private FireworkPresetRegistry fireworkPresets;
    private SequenceRegistry sequences;
    private SceneRegistry scenes;
    private ShowRegistry shows;
    private ShowManager showManager;

    private int tickTaskId = -1;

    @Override
    public void onEnable() {
        // Ensure data folder exists + starter library
        saveDefaultConfig();
        getDataFolder().mkdirs();

        ResourceCopier.copyIfMissing(this, "fireworks.yml", new java.io.File(getDataFolder(), "fireworks.yml"));
        ResourceCopier.copyTreeIfMissing(this, "sequences", new java.io.File(getDataFolder(), "sequences"));
        ResourceCopier.copyTreeIfMissing(this, "scenes", new java.io.File(getDataFolder(), "scenes"));
        ResourceCopier.copyTreeIfMissing(this, "shows", new java.io.File(getDataFolder(), "shows"));

        this.configLoader = new ConfigLoader(this);
        this.fireworkPresets = new FireworkPresetRegistry(this);
        this.sequences = new SequenceRegistry(this);
        this.scenes = new SceneRegistry(this);
        this.shows = new ShowRegistry(this);

        reloadAll();

        this.showManager = new ShowManager(this, configLoader, fireworkPresets, sequences, scenes, shows);

        // Commands + events
        var cmd = getCommand("show");
        if (cmd != null) {
            cmd.setExecutor(new com.scaena.shows.runtime.ShowCommand(this, showManager));
            cmd.setTabCompleter(new com.scaena.shows.runtime.ShowCommand(this, showManager));
        }

        getServer().getPluginManager().registerEvents(new com.scaena.shows.runtime.ShowListener(showManager), this);

        // Global scheduler tick
        tickTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, showManager::tickAll, 1L, 1L);
    }

    @Override
    public void onDisable() {
        if (tickTaskId != -1) getServer().getScheduler().cancelTask(tickTaskId);
        if (showManager != null) showManager.stopAll("Server stopping.");
    }

    public void reloadAll() {
        configLoader.reload();
        fireworkPresets.reload();
        sequences.reload();
        scenes.reload();
        shows.reload();
    }
}
