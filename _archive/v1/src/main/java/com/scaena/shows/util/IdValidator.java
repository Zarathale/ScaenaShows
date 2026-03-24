package com.scaena.shows.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class IdValidator {

    private static final Pattern NS = Pattern.compile("^[a-z0-9_\\-\\.]+:[a-z0-9_\\-/\\.]+$");

    private IdValidator() {}

    public static boolean looksNamespaced(String id) {
        return id != null && NS.matcher(id).matches();
    }

    public static boolean validMaterial(String id) {
        if (id == null) return false;
        Material m = Material.matchMaterial(id, true);
        return m != null;
    }

    public static boolean validPotion(String id) {
        if (id == null) return false;
        NamespacedKey key = NamespacedKey.fromString(id);
        if (key == null) {
            // allow "levitation" etc
            key = NamespacedKey.minecraft(id.toLowerCase(Locale.ROOT));
        }
        return PotionEffectType.getByKey(key) != null;
    }

    public static boolean validParticle(String id) {
        if (id == null) return false;
        String raw = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        try {
            Particle.valueOf(raw.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean validSoundId(String id) {
        if (id == null) return false;
        // Bukkit string sounds are namespaced keys in modern versions; do a lightweight check.
        return looksNamespaced(id) || id.matches("^[a-z0-9_\\-\\.]+$");
    }

    public static Optional<NamespacedKey> parseKey(String id) {
        if (id == null) return Optional.empty();
        NamespacedKey key = NamespacedKey.fromString(id);
        if (key != null) return Optional.of(key);
        // fallback to minecraft namespace
        return Optional.of(NamespacedKey.minecraft(id.toLowerCase(Locale.ROOT)));
    }
}
