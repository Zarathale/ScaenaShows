package com.scaena.shows;

import com.scaena.shows.command.ScoutCommand;
import com.scaena.shows.command.ShowCommand;
import com.scaena.shows.config.ScaenaConfig;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.registry.FireworkRegistry;
import com.scaena.shows.registry.ShowRegistry;
import com.scaena.shows.runtime.PlayerLifecycleListener;
import com.scaena.shows.runtime.ShowManager;
import com.scaena.shows.runtime.executor.ExecutorRegistry;
import com.scaena.shows.scout.ScoutManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * ScaenaShows v2 — main plugin entry point.
 *
 * Startup order:
 *  1. Save default resources (config.yml, fireworks.yml)
 *  2. Load config → ScaenaConfig
 *  3. Load fireworks.yml → FireworkRegistry
 *  4. Load cues/ → CueRegistry
 *  5. Load shows/ → ShowRegistry (validates CUE refs + cycle detection)
 *  6. Build ExecutorRegistry (wires all event executors)
 *  7. Build ShowManager
 *  8. Register /show command + tab completer
 *  9. Register player lifecycle listener
 */
public final class ScaenaShowsPlugin extends JavaPlugin {

    private ScaenaConfig      scaenaConfig;
    private FireworkRegistry  fireworkRegistry;
    private CueRegistry       cueRegistry;
    private ShowRegistry      showRegistry;
    private ExecutorRegistry  executors;
    private ShowManager       showManager;
    private ScoutManager      scoutManager;

    @Override
    public void onEnable() {
        // 0. Theatrical banner — first thing on screen
        ScaenaConsole.printBanner(getDescription().getVersion());

        // 1. Save default resources if they don't exist
        saveDefaultResource("config.yml");
        saveDefaultResource("fireworks.yml");
        saveDefaultResources("cues");
        saveDefaultResources("shows");
        saveDefaultResources("scout_objectives");

        // 2. Load config
        saveDefaultConfig();
        reloadConfig();
        scaenaConfig = new ScaenaConfig(getConfig());

        // 3. Registries
        fireworkRegistry = new FireworkRegistry(getLogger());
        cueRegistry      = new CueRegistry(getLogger());
        showRegistry     = new ShowRegistry(getLogger(), cueRegistry);

        // 4–5. Load all YAML data (fireworks → cues → shows)
        loadAllData();

        // Stage inventory summary
        ScaenaConsole.printStats(
            fireworkRegistry.getAll().size(),
            cueRegistry.getAll().size(),
            showRegistry.getAll().size()
        );

        // 6. Executor registry
        executors = new ExecutorRegistry(this, fireworkRegistry, cueRegistry);

        // 7. Show manager
        showManager = new ShowManager(this, scaenaConfig, cueRegistry, executors);

        // 8a. Scout manager
        scoutManager = new ScoutManager(this);

        // 8b. Register /show command
        PluginCommand showCmd = getCommand("show");
        if (showCmd != null) {
            ShowCommand handler = new ShowCommand(
                this, showRegistry, cueRegistry, fireworkRegistry, showManager);
            showCmd.setExecutor(handler);
            showCmd.setTabCompleter(handler);
        } else {
            ScaenaConsole.error("plugin.yml", "'show' command not found — check plugin.yml!");
        }

        // 8c. Register /scaena command
        PluginCommand scaenaCmd = getCommand("scaena");
        if (scaenaCmd != null) {
            ScoutCommand scoutHandler = new ScoutCommand(scoutManager);
            scaenaCmd.setExecutor(scoutHandler);
            scaenaCmd.setTabCompleter(scoutHandler);
        } else {
            ScaenaConsole.error("plugin.yml", "'scaena' command not found — check plugin.yml!");
        }

        // 9. Player lifecycle listener
        getServer().getPluginManager().registerEvents(
            new PlayerLifecycleListener(showManager, scoutManager), this);

        // Ready!
        ScaenaConsole.printReady();
    }

    @Override
    public void onDisable() {
        if (showManager != null) {
            showManager.shutdown();
        }
        ScaenaConsole.printCurtainDown();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Load fireworks.yml → cues/ → shows/ in the correct order. */
    private void loadAllData() {
        File dataFolder = getDataFolder();
        fireworkRegistry.load(new File(dataFolder, "fireworks.yml"));
        cueRegistry.load(new File(dataFolder, "cues"));
        showRegistry.load(new File(dataFolder, "shows"));
    }

    private void saveDefaultResource(String name) {
        File f = new File(getDataFolder(), name);
        if (!f.exists()) {
            saveResource(name, false);
        }
    }

    /**
     * Scans the plugin JAR for all .yml files under the given folder prefix
     * and extracts any that don't already exist in the plugin data folder.
     * This seeds cues/ and shows/ on first install without overwriting edits.
     */
    private void saveDefaultResources(String folder) {
        // Ensure the directory exists even if the JAR has no bundled files
        File dir = new File(getDataFolder(), folder);
        if (!dir.exists()) dir.mkdirs();

        try (JarFile jar = new JarFile(getFile())) {
            jar.stream()
               .filter(e -> !e.isDirectory()
                         && e.getName().startsWith(folder + "/")
                         && e.getName().endsWith(".yml"))
               .forEach(e -> saveDefaultResource(e.getName()));
        } catch (IOException e) {
            getLogger().warning("[ScaenaShows] Could not scan bundled " + folder + "/ resources: " + e.getMessage());
        }
    }
}
