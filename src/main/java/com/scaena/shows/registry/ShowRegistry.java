package com.scaena.shows.registry;

import com.scaena.shows.ScaenaConsole;
import com.scaena.shows.model.*;
import com.scaena.shows.model.event.ShowEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Loads all shows/*.yml files into a registry.
 * Delegates CUE reference validation and cycle detection to CueRegistry.
 */
public final class ShowRegistry {

    private final Logger log;
    private final CueRegistry cueRegistry;
    private Map<String, Show> shows = Map.of();

    public ShowRegistry(Logger log, CueRegistry cueRegistry) {
        this.log         = log;
        this.cueRegistry = cueRegistry;
    }

    /**
     * Load or reload all .yml files from the shows/ directory.
     * Requires CueRegistry to already be loaded.
     *
     * @param showsDir the shows/ folder inside the plugin data folder
     */
    public void load(File showsDir) {
        if (!showsDir.exists() || !showsDir.isDirectory()) {
            log.info("[ScaenaShows] shows/ directory is empty or missing — no shows loaded.");
            shows = Map.of();
            return;
        }

        File[] files = showsDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) {
            log.info("[ScaenaShows] No show files found in shows/.");
            shows = Map.of();
            return;
        }

        Map<String, Show> loaded = new LinkedHashMap<>();
        for (File f : files) {
            try {
                Show show = parseFile(f);
                if (show == null) continue;

                // Fail fast: unknown CUE IDs
                cueRegistry.validateCueRefs(show.id, show.timeline);

                // Fail fast: cycle detection
                cueRegistry.detectCycles(show.id, show.timeline);

                if (loaded.containsKey(show.id)) {
                    ScaenaConsole.warn(show.id,
                        "Duplicate show ID in " + f.getName() + " — skipping.");
                    continue;
                }
                loaded.put(show.id, show);
                ScaenaConsole.showLoaded(show.id, show.name);

            } catch (IllegalStateException e) {
                // Cycle detection or unknown CUE ID — fail this show, not the whole server
                ScaenaConsole.error("load", e.getMessage());
            } catch (Exception e) {
                ScaenaConsole.warn(f.getName(),
                    "Failed to parse show: " + e.getMessage());
            }
        }

        shows = Collections.unmodifiableMap(loaded);
    }

    @SuppressWarnings("unchecked")
    private Show parseFile(File f) throws IOException {
        Map<String, Object> m = YamlLoader.load(f);
        if (m.isEmpty()) return null;

        String id = str(m, "id", "");
        if (id.isEmpty()) {
            log.warning("[ScaenaShows] Show file '" + f.getName() + "' is missing 'id' — skipping.");
            return null;
        }

        String name        = str(m, "name", id);
        String description = str(m, "description", "");
        int duration       = intVal(m, "duration_ticks", 0);
        List<String> tags  = toStringList(m.get("tags"));
        CueMeta meta       = CueMeta.from(m.get("meta"));

        List<ShowEvent> timeline = EventParser.parseTimeline(
            (List<?>) m.getOrDefault("timeline", List.of())
        );

        boolean portable      = bool(m, "portable", false);
        String defaultMode    = str(m, "default_mode", "static");
        String defaultAudience= str(m, "default_audience", "broadcast");
        int groupCount        = intVal(m, "group_count", 2);
        String groupStrategy  = str(m, "group_strategy", "join_order");
        String front          = str(m, "front", "south");
        BossbarDef bossbar    = BossbarDef.from(m.get("bossbar"));

        // Parse marks
        Map<String, Mark> marks = new LinkedHashMap<>();
        Object marksRaw = m.get("marks");
        if (marksRaw instanceof Map<?, ?> mMap) {
            for (Map.Entry<?, ?> entry : mMap.entrySet()) {
                String markName = entry.getKey().toString();
                marks.put(markName, Mark.from(markName, entry.getValue()));
            }
        }

        // Parse sets
        Map<String, ShowSet> sets = new LinkedHashMap<>();
        Object setsRaw = m.get("sets");
        if (setsRaw instanceof Map<?, ?> sMap) {
            for (Map.Entry<?, ?> entry : sMap.entrySet()) {
                String setName = entry.getKey().toString();
                ShowSet set = ShowSet.from(setName, entry.getValue());
                if (set != null) sets.put(setName, set);
            }
        }

        return new Show(id, name, description, duration, tags, meta, timeline,
            portable, defaultMode, defaultAudience, groupCount, groupStrategy,
            front, marks, sets, bossbar);
    }

    public Show get(String id)          { return shows.get(id); }
    public Map<String, Show> getAll()   { return shows; }
    public boolean has(String id)       { return shows.containsKey(id); }

    // ---- helpers ----
    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) if (item != null) out.add(item.toString());
            return out;
        }
        return new ArrayList<>();
    }
    private static String  str(Map<String, Object> m, String k, String  d) { Object v=m.get(k); return v!=null?v.toString():d; }
    private static int intVal(Map<String, Object> m, String k, int     d) { Object v=m.get(k); return v instanceof Number n?n.intValue():d; }
    private static boolean bool(Map<String, Object> m, String k, boolean d) { Object v=m.get(k); if(v==null)return d; if(v instanceof Boolean b)return b; return Boolean.parseBoolean(v.toString()); }
}
