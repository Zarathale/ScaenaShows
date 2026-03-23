package com.scaena.shows.registry;

import com.scaena.shows.model.Scene;
import com.scaena.shows.model.SceneEvent;
import com.scaena.shows.util.LoadResult;
import com.scaena.shows.util.YamlUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class SceneRegistry extends AbstractRegistry<Scene> {

    public SceneRegistry(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "scenes";
    }

    @Override
    protected LoadResult<Scene> load() {
        List<String> errors = new ArrayList<>();
        Map<String, Scene> out = new HashMap<>();

        File dir = new File(plugin.getDataFolder(), "scenes");
        if (!dir.exists()) return new LoadResult<>(Map.of(), List.of("Missing scenes/ folder"));

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return new LoadResult<>(Map.of(), List.of("Unable to list scenes/ folder"));

        for (File f : files) {
            YamlConfiguration yml = YamlUtil.load(f, errors);
            if (yml == null) continue;

            String id = yml.getString("id", null);
            if (id == null || id.isBlank()) {
                errors.add(f.getName() + ": missing required 'id'");
                continue;
            }
            String ctx = "scenes/" + f.getName() + " (" + id + ")";

            String name = yml.getString("name", id);
            String sceneText = yml.getString("scene_text", null);
            int duration = yml.getInt("duration_ticks", 0);

            List<Map<?, ?>> evs = YamlUtil.listOfMaps(yml, "events");
            List<SceneEvent> events = new ArrayList<>();
            for (int i = 0; i < evs.size(); i++) {
                Map<?, ?> m = evs.get(i);
                String ec = ctx + ":events[" + i + "]";
                Integer at = intObj(m.get("at"));
                if (at == null) { errors.add(ec + ": missing at"); continue; }
                String type = str(m.get("type"));
                if (type == null) { errors.add(ec + ": missing type"); continue; }

                SceneEvent event = SceneEventParser.parse(at, type, m, ec, errors);
                if (event != null) events.add(event);
            }

            if (out.containsKey(id)) {
                errors.add(ctx + ": duplicate id '" + id + "'");
                continue;
            }
            out.put(id, new Scene(id, name, sceneText, duration, List.copyOf(events)));
        }

        return new LoadResult<>(Collections.unmodifiableMap(out), errors);
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static Integer intObj(Object o) {
        if (o instanceof Number n) return n.intValue();
        try { return o == null ? null : Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
