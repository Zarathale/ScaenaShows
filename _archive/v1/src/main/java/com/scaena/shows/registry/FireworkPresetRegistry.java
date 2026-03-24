package com.scaena.shows.registry;

import com.scaena.shows.model.FireworkLaunch;
import com.scaena.shows.model.FireworkPreset;
import com.scaena.shows.util.LoadResult;
import com.scaena.shows.util.YamlUtil;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class FireworkPresetRegistry extends AbstractRegistry<FireworkPreset> {

    public FireworkPresetRegistry(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "firework presets";
    }

    @Override
    protected LoadResult<FireworkPreset> load() {
        List<String> errors = new ArrayList<>();
        Map<String, FireworkPreset> out = new HashMap<>();

        File file = new File(plugin.getDataFolder(), "fireworks.yml");
        YamlConfiguration yml = YamlUtil.load(file, errors);
        if (yml == null) return new LoadResult<>(Map.of(), errors);

        ConfigurationSection presets = yml.getConfigurationSection("presets");
        if (presets == null) {
            errors.add("fireworks.yml: missing 'presets' section");
            return new LoadResult<>(Map.of(), errors);
        }

        for (String id : presets.getKeys(false)) {
            ConfigurationSection sec = presets.getConfigurationSection(id);
            if (sec == null) continue;

            String ctx = "fireworks.yml:presets." + id;

            String display = sec.getString("display_name", id);
            int power = sec.getInt("power", 1);
            String typeStr = sec.getString("type", "BALL");
            FireworkEffect.Type type;
            try {
                type = FireworkEffect.Type.valueOf(typeStr.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                errors.add(ctx + ": invalid type '" + typeStr + "'");
                continue;
            }

            List<Color> colors = parseColors(sec.getStringList("colors"), errors, ctx + ".colors", true);
            List<Color> fades = parseColors(sec.getStringList("fades"), errors, ctx + ".fades", false);

            boolean trail = sec.getBoolean("trail", false);
            boolean flicker = sec.getBoolean("flicker", false);

            ConfigurationSection launchSec = sec.getConfigurationSection("launch");
            FireworkLaunch launch = parseLaunch(launchSec);

            if (colors.isEmpty()) {
                errors.add(ctx + ": requires at least 1 color");
                continue;
            }

            out.put(id, new FireworkPreset(id, display, power, type, colors, fades, trail, flicker, launch));
        }

        return new LoadResult<>(Collections.unmodifiableMap(out), errors);
    }

    private static List<Color> parseColors(List<String> hexes, List<String> errors, String ctx, boolean required) {
        if ((hexes == null || hexes.isEmpty()) && required) return List.of();
        if (hexes == null) return List.of();

        List<Color> out = new ArrayList<>();
        for (String h : hexes) {
            Color c = parseHex(h);
            if (c == null) {
                errors.add(ctx + ": invalid color '" + h + "' (expected #RRGGBB)");
                continue;
            }
            out.add(c);
        }
        return out;
    }

    private static Color parseHex(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (t.length() != 6) return null;
        try {
            int r = Integer.parseInt(t.substring(0,2),16);
            int g = Integer.parseInt(t.substring(2,4),16);
            int b = Integer.parseInt(t.substring(4,6),16);
            return Color.fromRGB(r,g,b);
        } catch (Exception e) {
            return null;
        }
    }

    private static FireworkLaunch parseLaunch(ConfigurationSection sec) {
        if (sec == null) return new FireworkLaunch(FireworkLaunch.Mode.FEET, 0.0, 0.0);

        String modeStr = sec.getString("mode", "feet");
        FireworkLaunch.Mode mode = switch (modeStr.toLowerCase(Locale.ROOT)) {
            case "above" -> FireworkLaunch.Mode.ABOVE;
            case "random" -> FireworkLaunch.Mode.RANDOM;
            default -> FireworkLaunch.Mode.FEET;
        };

        double y = sec.getDouble("y_offset", 0.0);
        double spread = sec.getDouble("spread", 0.0);
        return new FireworkLaunch(mode, y, spread);
    }
}
