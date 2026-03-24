package com.scaena.shows.registry;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Thin wrapper around SnakeYAML for loading files into Map<String, Object>. */
public final class YamlLoader {

    private YamlLoader() {}

    /**
     * Loads a YAML file and returns the root map.
     * Returns an empty map if the file is empty or has no root mapping.
     *
     * @throws IOException if the file cannot be read
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> load(File file) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            Object result = yaml.load(reader);
            if (result instanceof Map<?, ?> m) {
                return (Map<String, Object>) m;
            }
            return Map.of();
        }
    }
}
