package com.scaena.shows.registry;

import com.scaena.shows.model.Sequence;
import com.scaena.shows.model.Shot;
import com.scaena.shows.model.SoundPayload;
import com.scaena.shows.util.IdValidator;
import com.scaena.shows.util.LoadResult;
import com.scaena.shows.util.YamlUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class SequenceRegistry extends AbstractRegistry<Sequence> {

    public SequenceRegistry(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "sequences";
    }

    @Override
    protected LoadResult<Sequence> load() {
        List<String> errors = new ArrayList<>();
        Map<String, Sequence> out = new HashMap<>();

        File dir = new File(plugin.getDataFolder(), "sequences");
        if (!dir.exists()) return new LoadResult<>(Map.of(), List.of("Missing sequences/ folder"));

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return new LoadResult<>(Map.of(), List.of("Unable to list sequences/ folder"));

        for (File f : files) {
            YamlConfiguration yml = YamlUtil.load(f, errors);
            if (yml == null) continue;

            String id = yml.getString("id", null);
            if (id == null || id.isBlank()) {
                errors.add(f.getName() + ": missing required 'id'");
                continue;
            }
            String ctx = "sequences/" + f.getName() + " (" + id + ")";

            String name = yml.getString("name", id);
            String desc = yml.getString("description", "");
            int duration = yml.getInt("duration_ticks", 0);

            List<Map<?, ?>> shotsList = YamlUtil.listOfMaps(yml, "shots");
            List<Shot> shots = new ArrayList<>();
            for (int i = 0; i < shotsList.size(); i++) {
                Map<?, ?> m = shotsList.get(i);
                String sc = ctx + ":shots[" + i + "]";
                int at = asInt(m.get("at"), sc + ".at", errors);
                String preset = asString(m.get("preset"));
                if (preset == null || preset.isBlank()) {
                    errors.add(sc + ": missing preset");
                    continue;
                }
                int count = asInt(m.containsKey("count") ? m.get("count") : 1, sc + ".count", errors);
                Double spreadOverride = m.containsKey("spread_override") ? asDoubleObj(m.get("spread_override"), sc + ".spread_override", errors) : null;

                SoundPayload sound = null;
                Object soundObj = m.get("sound");
                if (soundObj instanceof Map<?, ?> sm) {
                    String sid = asString(sm.get("id"));
                    Object catObj = sm.containsKey("category") ? sm.get("category") : "master";
                    Object volObj = sm.containsKey("volume") ? sm.get("volume") : 1.0;
                    Object pitObj = sm.containsKey("pitch") ? sm.get("pitch") : 1.0;
                    String cat = asString(catObj);
                    float vol = (float) asDouble(volObj, sc + ".sound.volume", errors);
                    float pit = (float) asDouble(pitObj, sc + ".sound.pitch", errors);
                    if (sid != null && IdValidator.validSoundId(sid)) {
                        sound = new SoundPayload(sid, cat == null ? "master" : cat, vol, pit);
                    } else if (sid != null) {
                        errors.add(sc + ": invalid sound id '" + sid + "'");
                    }
                }

                shots.add(new Shot(at, preset, Math.max(1, count), spreadOverride, sound));
            }

            if (out.containsKey(id)) {
                errors.add(ctx + ": duplicate id '" + id + "'");
                continue;
            }
            out.put(id, new Sequence(id, name, desc, duration, List.copyOf(shots)));
        }

        return new LoadResult<>(Collections.unmodifiableMap(out), errors);
    }

    private static int asInt(Object o, String ctx, List<String> errors) {
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            errors.add(ctx + ": expected int");
            return 0;
        }
    }

    private static double asDouble(Object o, String ctx, List<String> errors) {
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            errors.add(ctx + ": expected number");
            return 0.0;
        }
    }

    private static Double asDoubleObj(Object o, String ctx, List<String> errors) {
        if (o == null) return null;
        return asDouble(o, ctx, errors);
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
