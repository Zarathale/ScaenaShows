package com.scaena.shows.registry;

import com.scaena.shows.model.FireworkPreset;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads fireworks.yml and makes presets available by ID.
 */
public final class FireworkRegistry {

    private final Logger log;
    private Map<String, FireworkPreset> presets = Map.of();

    public FireworkRegistry(Logger log) {
        this.log = log;
    }

    /**
     * Load or reload fireworks.yml from the plugin data folder.
     *
     * @param fireworksFile the fireworks.yml File
     */
    @SuppressWarnings("unchecked")
    public void load(File fireworksFile) {
        if (!fireworksFile.exists()) {
            log.warning("[ScaenaShows] fireworks.yml not found — no presets loaded.");
            presets = Map.of();
            return;
        }

        try {
            Map<String, Object> root = YamlLoader.load(fireworksFile);
            Object presetsRaw = root.get("presets");
            if (!(presetsRaw instanceof Map<?, ?> pMap)) {
                log.warning("[ScaenaShows] fireworks.yml has no 'presets' map.");
                presets = Map.of();
                return;
            }

            Map<String, FireworkPreset> loaded = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : pMap.entrySet()) {
                String id = entry.getKey().toString();
                if (!(entry.getValue() instanceof Map<?, ?> def)) continue;
                try {
                    FireworkPreset preset = FireworkPreset.from(id, (Map<String, Object>) def);
                    loaded.put(id, preset);
                } catch (Exception e) {
                    log.warning("[ScaenaShows] Failed to parse firework preset '" + id + "': " + e.getMessage());
                }
            }

            presets = Collections.unmodifiableMap(loaded);
            log.info("[ScaenaShows] Loaded " + presets.size() + " firework preset(s) from fireworks.yml.");

        } catch (IOException e) {
            log.severe("[ScaenaShows] Could not read fireworks.yml: " + e.getMessage());
        }
    }

    /** Returns the preset by ID, or null if not found. */
    public FireworkPreset get(String id) {
        return presets.get(id);
    }

    public Map<String, FireworkPreset> getAll() {
        return presets;
    }

    public boolean has(String id) {
        return presets.containsKey(id);
    }
}
