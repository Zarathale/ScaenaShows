package com.scaena.shows.registry;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.CueMeta;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.TeamEvents;
import com.scaena.shows.model.event.UtilityEvents;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Loads all cues/*.yml files into a registry.
 * Enforces fail-fast on unknown CUE IDs and cycle detection at load time.
 */
public final class CueRegistry {

    private final Logger log;
    private Map<String, Cue> cues = Map.of();

    public CueRegistry(Logger log) {
        this.log = log;
    }

    /**
     * Load or reload all .yml files from the cues/ directory.
     *
     * @param cuesDir the cues/ folder inside the plugin data folder
     */
    public void load(File cuesDir) {
        if (!cuesDir.exists() || !cuesDir.isDirectory()) {
            log.info("[ScaenaShows] cues/ directory is empty or missing — no cues loaded.");
            cues = Map.of();
            return;
        }

        File[] files = cuesDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) {
            log.info("[ScaenaShows] No cue files found in cues/.");
            cues = Map.of();
            return;
        }

        Map<String, Cue> loaded = new LinkedHashMap<>();
        for (File f : files) {
            try {
                Cue cue = parseFile(f);
                if (cue == null) continue;
                if (loaded.containsKey(cue.id)) {
                    log.warning("[ScaenaShows] Duplicate cue ID '" + cue.id + "' in " + f.getName() + " — skipping.");
                    continue;
                }
                loaded.put(cue.id, cue);
            } catch (Exception e) {
                log.warning("[ScaenaShows] Failed to load cue file '" + f.getName() + "': " + e.getMessage());
            }
        }

        cues = Collections.unmodifiableMap(loaded);
        log.info("[ScaenaShows] Loaded " + cues.size() + " cue(s).");
    }

    /** Parse a single cue YAML file. Returns null if file is empty or missing id. */
    @SuppressWarnings("unchecked")
    private Cue parseFile(File f) throws IOException {
        Map<String, Object> m = YamlLoader.load(f);
        if (m.isEmpty()) return null;

        String id = str(m, "id", "");
        if (id.isEmpty()) {
            log.warning("[ScaenaShows] Cue file '" + f.getName() + "' is missing 'id' — skipping.");
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

        return new Cue(id, name, description, duration, tags, meta, timeline);
    }

    /**
     * Validates that all CUE references in the given timeline (and nested cues) resolve
     * to known IDs. Used by ShowRegistry after cues are loaded.
     *
     * @throws IllegalStateException if an unknown CUE ID is found, naming the show and the missing ID
     */
    public void validateCueRefs(String showId, List<ShowEvent> timeline) {
        validateCueRefs(showId, timeline, new HashSet<>());
    }

    private void validateCueRefs(String showId, List<ShowEvent> timeline, Set<String> visited) {
        for (ShowEvent e : timeline) {
            // CUE utility reference
            if (e instanceof UtilityEvents.CueRefEvent cueRef) {
                String refId = cueRef.cueId;
                if (!cues.containsKey(refId)) {
                    throw new IllegalStateException(
                        "[ERROR] Show '" + showId + "' references unknown Cue ID: '" + refId + "'"
                    );
                }
                // Only recurse into cues we haven't visited yet — prevents infinite recursion on cycles.
                // (Actual cycle detection with path reporting is handled by detectCycles.)
                if (visited.add(refId)) {
                    validateCueRefs(showId, cues.get(refId).timeline, visited);
                }
            }
            // GROUP_EVENT cue reference — same fail-fast rule
            if (e instanceof TeamEvents.GroupEvent groupEvent) {
                String refId = groupEvent.cueId;
                if (!cues.containsKey(refId)) {
                    throw new IllegalStateException(
                        "[ERROR] Show '" + showId + "' GROUP_EVENT references unknown Cue ID: '" + refId + "'"
                    );
                }
                // Recurse to catch unknown IDs inside the sub-cue
                if (visited.add(refId)) {
                    validateCueRefs(showId, cues.get(refId).timeline, visited);
                }
            }
        }
    }

    /**
     * Detects cycles in CUE references for the given show timeline.
     * A cycle means a Cue directly or transitively references itself.
     *
     * @throws IllegalStateException with a path description if a cycle is found
     */
    public void detectCycles(String showId, List<ShowEvent> timeline) {
        detectCycles(showId, timeline, new ArrayDeque<>(), new HashSet<>());
    }

    private void detectCycles(
        String showId,
        List<ShowEvent> timeline,
        Deque<String> path,
        Set<String> visited
    ) {
        for (ShowEvent e : timeline) {
            if (!(e instanceof UtilityEvents.CueRefEvent cueRef)) continue;
            String refId = cueRef.cueId;
            Cue cue = cues.get(refId);
            if (cue == null) continue; // already caught by validateCueRefs

            if (visited.contains(refId)) {
                // Build cycle path string
                List<String> pathList = new ArrayList<>(path);
                pathList.add(refId);
                int cycleStart = pathList.indexOf(refId);
                String cyclePath = String.join(" → ", pathList.subList(cycleStart, pathList.size()));
                throw new IllegalStateException(
                    "[ERROR] Show '" + showId + "' contains a Cue cycle: " + cyclePath
                );
            }

            visited.add(refId);
            path.addLast(refId);
            detectCycles(showId, cue.timeline, path, visited);
            path.removeLast();
            visited.remove(refId);
        }
    }

    /** Returns the cue by ID, or null. */
    public Cue get(String id) {
        return cues.get(id);
    }

    public Map<String, Cue> getAll() {
        return cues;
    }

    public boolean has(String id) {
        return cues.containsKey(id);
    }

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

    private static String str(Map<String, Object> m, String k, String d) {
        Object v = m.get(k); return v != null ? v.toString() : d;
    }

    private static int intVal(Map<String, Object> m, String k, int d) {
        Object v = m.get(k); return v instanceof Number n ? n.intValue() : d;
    }
}
