package com.scaena.shows.config;

import org.bukkit.configuration.ConfigurationSection;

public record SoundSpec(String id, String category, float volume, float pitch) {

    public static SoundSpec fromSection(ConfigurationSection sec) {
        if (sec == null) return null;
        String id = sec.getString("id", null);
        String category = sec.getString("category", "master");
        float volume = (float) sec.getDouble("volume", 1.0);
        float pitch = (float) sec.getDouble("pitch", 1.0);
        if (id == null || id.isBlank()) return null;
        return new SoundSpec(id, category, volume, pitch);
    }
}
