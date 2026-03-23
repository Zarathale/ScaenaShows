package com.scaena.shows.registry;

import com.scaena.shows.util.LoadResult;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractRegistry<T> {

    protected final JavaPlugin plugin;
    protected volatile Map<String, T> items = Collections.emptyMap();

    protected AbstractRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public final void reload() {
        LoadResult<T> result = load();
        this.items = result.items();

        if (!result.errors().isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "[ScaenaShows] Loaded with errors (" + getName() + "):");
            for (String err : result.errors()) {
                plugin.getLogger().log(Level.WARNING, " - " + err);
            }
        } else {
            plugin.getLogger().log(Level.INFO, "[ScaenaShows] Loaded " + items.size() + " " + getName());
        }
    }

    public Map<String, T> all() {
        return items;
    }

    public T get(String id) {
        return items.get(id);
    }

    public abstract String getName();

    protected abstract LoadResult<T> load();
}
