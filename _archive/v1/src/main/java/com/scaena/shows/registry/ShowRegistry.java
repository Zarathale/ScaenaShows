package com.scaena.shows.registry;

import com.scaena.shows.model.*;
import com.scaena.shows.util.LoadResult;
import com.scaena.shows.util.YamlUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class ShowRegistry extends AbstractRegistry<ShowDefinition> {

    public ShowRegistry(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "shows";
    }

    @Override
    protected LoadResult<ShowDefinition> load() {
        List<String> errors = new ArrayList<>();
        Map<String, ShowDefinition> out = new HashMap<>();

        File dir = new File(plugin.getDataFolder(), "shows");
        if (!dir.exists()) return new LoadResult<>(Map.of(), List.of("Missing shows/ folder"));

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return new LoadResult<>(Map.of(), List.of("Unable to list shows/ folder"));

        for (File f : files) {
            YamlConfiguration yml = YamlUtil.load(f, errors);
            if (yml == null) continue;

            String id = yml.getString("id", null);
            if (id == null || id.isBlank()) {
                errors.add(f.getName() + ": missing required 'id'");
                continue;
            }
            String ctx = "shows/" + f.getName() + " (" + id + ")";

            String name = yml.getString("name", id);
            String desc = yml.getString("description", "");

            ShowMode defaultMode = ShowMode.fromString(yml.getString("default_mode", "follow"), ShowMode.FOLLOW);
            AudienceMode defaultAudience = AudienceMode.fromString(yml.getString("default_audience", "broadcast"), AudienceMode.BROADCAST);

            // bossbar defaults
            var boss = yml.getConfigurationSection("bossbar");
            ShowBossbarDefaults bossbar = new ShowBossbarDefaults(
                    boss != null && boss.getBoolean("enabled", false),
                    boss != null ? boss.getString("title", "") : "",
                    boss != null ? boss.getString("color", "YELLOW") : "YELLOW",
                    boss != null ? boss.getString("overlay", "PROGRESS") : "PROGRESS",
                    AudienceMode.fromString(boss != null ? boss.getString("audience", "broadcast") : "broadcast", AudienceMode.BROADCAST)
            );

            List<Map<?, ?>> tl = YamlUtil.listOfMaps(yml, "timeline");
            List<ShowTimelineEntry> timeline = new ArrayList<>();
            for (int i = 0; i < tl.size(); i++) {
                Map<?, ?> m = tl.get(i);
                String ec = ctx + ":timeline[" + i + "]";
                Integer at = intObj(m.get("at"));
                if (at == null) { errors.add(ec + ": missing at"); continue; }
                String type = str(m.get("type"));
                if (type == null) { errors.add(ec + ": missing type"); continue; }

                if ("SCENE".equalsIgnoreCase(type)) {
                    String sceneId = str(m.get("scene"));
                    if (sceneId == null) { errors.add(ec + ": missing scene"); continue; }
                    timeline.add(new ShowTimelineEntry.SceneCall(at, sceneId));
                } else if ("SEQUENCE".equalsIgnoreCase(type)) {
                    String seqId = str(m.get("sequence"));
                    if (seqId == null) { errors.add(ec + ": missing sequence"); continue; }
                    // Represent top-level SEQUENCE as a SceneEvent.SequenceEvent with same atTick (payload)
                    timeline.add(new ShowTimelineEntry.EventEntry(at, new SceneEvent.SequenceEvent(0, new SequenceRefPayload(seqId))));
                } else {
                    // For other event types, reuse SceneRegistry parsing rules by faking a scene-event at=0, then wrap with top-level at
                    SceneEvent ev = SceneEventParser.parse(0, type, m, ec, errors);
                    if (ev != null) timeline.add(new ShowTimelineEntry.EventEntry(at, ev));
                }
            }

            if (out.containsKey(id)) {
                errors.add(ctx + ": duplicate id '" + id + "'");
                continue;
            }
            out.put(id, new ShowDefinition(id, name, desc, defaultMode, defaultAudience, bossbar, List.copyOf(timeline)));
        }

        return new LoadResult<>(Collections.unmodifiableMap(out), errors);
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static Integer intObj(Object o) {
        if (o instanceof Number n) return n.intValue();
        try { return o == null ? null : Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
