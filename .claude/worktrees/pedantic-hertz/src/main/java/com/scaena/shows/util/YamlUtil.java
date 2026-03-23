package com.scaena.shows.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class YamlUtil {

    private YamlUtil() {}

    public static YamlConfiguration load(File file, List<String> errors) {
        try {
            YamlConfiguration yml = new YamlConfiguration();
            yml.load(file);
            return yml;
        } catch (Exception e) {
            errors.add(file.getName() + ": failed to load YAML (" + e.getMessage() + ")");
            return null;
        }
    }

    public static String reqString(ConfigurationSection sec, String path, List<String> errors, String ctx) {
        String v = sec.getString(path, null);
        if (v == null || v.isBlank()) {
            errors.add(ctx + ": missing required string: " + path);
            return null;
        }
        return v;
    }

    public static int reqInt(ConfigurationSection sec, String path, List<String> errors, String ctx) {
        if (!sec.isInt(path)) {
            errors.add(ctx + ": missing required int: " + path);
            return 0;
        }
        return sec.getInt(path);
    }

    public static ConfigurationSection section(ConfigurationSection sec, String path) {
        return sec == null ? null : sec.getConfigurationSection(path);
    }

    @SuppressWarnings("unchecked")
    public static List<Map<?, ?>> listOfMaps(ConfigurationSection sec, String path) {
        Object o = sec.get(path);
        if (o instanceof List<?> l && !l.isEmpty() && l.getFirst() instanceof Map<?, ?>) {
            return (List<Map<?, ?>>) o;
        }
        if (o instanceof List<?> l && l.isEmpty()) return List.of();
        return List.of();
    }
}
