package com.scaena.shows;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

final class ResourceCopier {

    // Keep this list in sync with the starter library shipped in src/main/resources/
    private static final List<String> SEQUENCE_FILES = List.of(
            "sequences/gentle_twinkles.yml",
            "sequences/scae_ramp.yml",
            "sequences/bday_sparkle_cascade.yml",
            "sequences/pride_wave.yml",
            "sequences/confetti_pop.yml",
            "sequences/spotlight_rise.yml",
            "sequences/orbit_spark.yml",
            "sequences/finale_hit.yml",
            "sequences/finale_staircase.yml",
            "sequences/triple_peaks.yml"
    );

    private static final List<String> SCENE_FILES = List.of(
            "scenes/opening.yml",
            "scenes/gratitude.yml",
            "scenes/wonder.yml",
            "scenes/float_moment.yml",
            "scenes/peak.yml",
            "scenes/coda.yml"
    );

    private static final List<String> SHOW_FILES = List.of(
            "shows/show_bday_short.yml",
            "shows/show_bday_medium.yml",
            "shows/show_bday_full.yml"
    );

    private ResourceCopier() {}

    static void copyIfMissing(JavaPlugin plugin, String resourcePath, File dest) {
        if (dest.exists()) return;

        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                plugin.getLogger().warning("Missing bundled resource: " + resourcePath);
                return;
            }
            Files.copy(in, dest.toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy resource " + resourcePath + " to " + dest.getAbsolutePath(), e);
        }
    }

    static void copyTreeIfMissing(JavaPlugin plugin, String folder, File destDir) {
        Objects.requireNonNull(destDir, "destDir");
        if (!destDir.exists() && !destDir.mkdirs()) return;

        List<String> files = switch (folder) {
            case "sequences" -> SEQUENCE_FILES;
            case "scenes" -> SCENE_FILES;
            case "shows" -> SHOW_FILES;
            default -> List.of();
        };

        for (String path : files) {
            File out = new File(plugin.getDataFolder(), path);
            copyIfMissing(plugin, path, out);
        }
    }
}
