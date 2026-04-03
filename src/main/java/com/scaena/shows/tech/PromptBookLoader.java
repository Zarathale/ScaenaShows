package com.scaena.shows.tech;

import com.scaena.shows.registry.YamlLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Reads [show_id].prompt-book.yml from the plugin data folder and returns a PromptBook.
 *
 * File path resolved as: plugins/ScaenaShows/shows/[showId]/[showId].prompt-book.yml
 *
 * Fails fast with clear log messages on missing required fields.
 * Optional/missing dept blocks are returned as null (not as empty records).
 */
public final class PromptBookLoader {

    private final Logger log;

    public PromptBookLoader(Logger log) {
        this.log = log;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Load the prompt book for the given showId.
     * @param dataFolder plugin data folder (plugins/ScaenaShows)
     * @param showId     e.g. "showcase.01"
     * @return PromptBook, or null on failure
     */
    public PromptBook load(File dataFolder, String showId) {
        File file = new File(dataFolder, "shows/" + showId + "/" + showId + ".prompt-book.yml");
        if (!file.exists()) {
            log.warning("[Tech] Prompt book not found: " + file.getPath());
            return null;
        }

        Map<String, Object> root;
        try {
            root = YamlLoader.load(file);
        } catch (IOException e) {
            log.severe("[Tech] Failed to read prompt book for " + showId + ": " + e.getMessage());
            return null;
        }

        if (root.isEmpty()) {
            log.warning("[Tech] Prompt book is empty for " + showId);
            return null;
        }

        return parseRoot(root, showId, file.getPath());
    }

    // -----------------------------------------------------------------------
    // Root parse
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook parseRoot(Map<String, Object> root, String showId, String path) {
        String title         = str(root, "title", showId);
        String stage         = str(root, "stage", "brief");
        String world         = str(root, "world", "overworld");
        String durationTarget = str(root, "duration_target", null);
        String updated       = str(root, "updated", null);

        PromptBook.ShowStructure structure = parseStructure(castMap(root.get("structure")));

        List<PromptBook.SceneSpec> scenes = parseScenes(root.get("scenes"), path);
        // Sort scenes by scene_number using decimal-aware comparison
        scenes.sort(Comparator.comparingDouble(PromptBookLoader::parseSceneNumber));

        Map<String, Object> mechanics = castMap(root.get("mechanics"));
        if (mechanics == null) mechanics = Map.of();

        List<PromptBook.ParamSpec> params = parseParams(root.get("params"), path);

        PromptBook.Readiness readiness = parseReadiness(castMap(root.get("readiness")));

        return new PromptBook(showId, title, stage, world, durationTarget, updated,
            structure, scenes, mechanics, params, readiness);
    }

    // -----------------------------------------------------------------------
    // Structure
    // -----------------------------------------------------------------------

    private PromptBook.ShowStructure parseStructure(Map<String, Object> m) {
        if (m == null) return new PromptBook.ShowStructure(null, null, 0, 0, 0, null);
        return new PromptBook.ShowStructure(
            str(m, "format", null),
            str(m, "sequence", null),
            intVal(m, "expedition_count"),
            intVal(m, "a_sections_total"),
            intVal(m, "armor_stand_fills"),
            str(m, "notes", null)
        );
    }

    // -----------------------------------------------------------------------
    // Scenes
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<PromptBook.SceneSpec> parseScenes(Object raw, String path) {
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        List<PromptBook.SceneSpec> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = castMap(item);
            if (m == null) continue;
            PromptBook.SceneSpec scene = parseScene(m, path);
            if (scene != null) result.add(scene);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private PromptBook.SceneSpec parseScene(Map<String, Object> m, String path) {
        String id = str(m, "id", null);
        if (id == null) {
            log.warning("[Tech] Scene missing 'id' field in " + path);
            return null;
        }

        String sceneNumber = str(m, "scene_number", "99");
        String label       = str(m, "label", id);
        String arrivalMark = str(m, "arrival_mark", null);
        String statusStr   = str(m, "status", "pending");
        String biome       = str(m, "biome", null);
        String world       = str(m, "world", null);

        PromptBook.StageStatus status = PromptBook.StageStatus.from(statusStr);

        // Departments block
        Map<String, Object> depts = castMap(m.get("departments"));

        PromptBook.DeptCasting  casting  = parseCasting(depts);
        PromptBook.DeptWardrobe wardrobe = parseWardrobe(depts);
        PromptBook.DeptSet      set      = parseSet(depts);
        PromptBook.DeptLighting lighting = parseLighting(depts);
        PromptBook.DeptFireworks fireworks = parseFireworks(depts);
        PromptBook.DeptScript   script   = parseScript(depts);

        return new PromptBook.SceneSpec(id, sceneNumber, label, arrivalMark, status,
            biome, world, casting, wardrobe, set, lighting, fireworks, script);
    }

    // -----------------------------------------------------------------------
    // Department: Casting
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.DeptCasting parseCasting(Map<String, Object> depts) {
        if (depts == null) return null;
        Object raw = depts.get("casting");
        if (!(raw instanceof List<?> list)) return null;
        List<PromptBook.CastingEntry> entries = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = castMap(item);
            if (m == null) continue;
            entries.add(new PromptBook.CastingEntry(
                str(m, "role", null),
                str(m, "mark", null),
                str(m, "entity_type", null),
                str(m, "subtype", null),
                str(m, "display_name", null),
                bool(m, "ai_locked"),
                str(m, "notes", null)
            ));
        }
        return entries.isEmpty() ? null : new PromptBook.DeptCasting(entries);
    }

    // -----------------------------------------------------------------------
    // Department: Wardrobe
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.DeptWardrobe parseWardrobe(Map<String, Object> depts) {
        if (depts == null) return null;
        Object raw = depts.get("wardrobe");
        if (!(raw instanceof List<?> list)) return null;
        List<PromptBook.WardrobeEntry> entries = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = castMap(item);
            if (m == null) continue;
            entries.add(new PromptBook.WardrobeEntry(
                str(m, "entity_mark", null),
                str(m, "helmet", null),
                str(m, "chestplate", null),
                str(m, "leggings", null),
                str(m, "boots", null),
                str(m, "boots_dye", null),
                str(m, "main_hand", null),
                str(m, "main_hand_enchant", null),
                str(m, "notes", null)
            ));
        }
        return entries.isEmpty() ? null : new PromptBook.DeptWardrobe(entries);
    }

    // -----------------------------------------------------------------------
    // Department: Set
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.DeptSet parseSet(Map<String, Object> depts) {
        if (depts == null) return null;
        Object raw = depts.get("set");
        if (raw == null) return null;

        List<?>              entryList   = null;
        PromptBook.BboxPoint bboxMin     = null;
        PromptBook.BboxPoint bboxMax     = null;
        String               activeBuild = null;

        if (raw instanceof List<?> list) {
            // Legacy format: set: [- mark: ..., ...]
            entryList = list;
        } else if (raw instanceof Map<?, ?> rawMap) {
            // New format: set: {bbox_min: ..., bbox_max: ..., active_build: ..., entries: [...]}
            Map<String, Object> m = (Map<String, Object>) rawMap;
            Object rawEntries = m.get("entries");
            if (rawEntries instanceof List<?> el) entryList = el;

            bboxMin     = parseBboxPoint(m.get("bbox_min"));
            bboxMax     = parseBboxPoint(m.get("bbox_max"));
            activeBuild = str(m, "active_build", null);
        } else {
            return null;
        }

        List<PromptBook.SetEntry> entries = new ArrayList<>();
        if (entryList != null) {
            for (Object item : entryList) {
                Map<String, Object> m = castMap(item);
                if (m == null) continue;
                entries.add(new PromptBook.SetEntry(
                    str(m, "mark", null),
                    str(m, "block_type", null),
                    str(m, "block_state", null),
                    str(m, "notes", null)
                ));
            }
        }

        // Return null only if there are no entries AND no bbox — a set section with just
        // a bbox (no entries yet) is still valid and must survive the round-trip.
        if (entries.isEmpty() && bboxMin == null && bboxMax == null && activeBuild == null) {
            return null;
        }
        return new PromptBook.DeptSet(entries, bboxMin, bboxMax, activeBuild);
    }

    /** Parse a bbox point from a YAML list [world, x, y, z] or null. */
    private static PromptBook.BboxPoint parseBboxPoint(Object raw) {
        if (!(raw instanceof List<?> list) || list.size() < 4) return null;
        String world = list.get(0) != null ? list.get(0).toString() : null;
        if (world == null) return null;
        try {
            int x = ((Number) list.get(1)).intValue();
            int y = ((Number) list.get(2)).intValue();
            int z = ((Number) list.get(3)).intValue();
            return new PromptBook.BboxPoint(world, x, y, z);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Department: Lighting
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.DeptLighting parseLighting(Map<String, Object> depts) {
        if (depts == null) return null;
        Object raw = depts.get("lighting");
        if (!(raw instanceof Map<?, ?> rawMap)) return null;
        Map<String, Object> m = (Map<String, Object>) rawMap;

        Integer timeOfDay = null;
        Object tod = m.get("time_of_day");
        if (tod instanceof Number n) timeOfDay = n.intValue();

        String notes = str(m, "notes", null);

        List<PromptBook.LightSource> sources = new ArrayList<>();
        Object rawSrc = m.get("sources");
        if (rawSrc instanceof List<?> srcList) {
            for (Object item : srcList) {
                Map<String, Object> sm = castMap(item);
                if (sm == null) continue;
                sources.add(new PromptBook.LightSource(
                    str(sm, "mark", null),
                    intVal(sm, "level"),
                    str(sm, "quality", null),
                    str(sm, "role", null),
                    str(sm, "notes", null),
                    str(sm, "status", null)
                ));
            }
        }

        return new PromptBook.DeptLighting(timeOfDay, notes, sources);
    }

    // -----------------------------------------------------------------------
    // Department: Fireworks
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.DeptFireworks parseFireworks(Map<String, Object> depts) {
        if (depts == null) return null;
        Object raw = depts.get("fireworks");
        if (!(raw instanceof Map<?, ?> rawMap)) return null;
        Map<String, Object> m = (Map<String, Object>) rawMap;

        boolean present = bool(m, "present");
        String  pattern = str(m, "pattern", null);
        String  timing  = str(m, "timing", null);
        String  notes   = str(m, "notes", null);
        return new PromptBook.DeptFireworks(present, pattern, timing, notes);
    }

    // -----------------------------------------------------------------------
    // Department: Script
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.DeptScript parseScript(Map<String, Object> depts) {
        if (depts == null) return null;
        Object raw = depts.get("script");
        if (!(raw instanceof Map<?, ?> rawMap)) return null;
        Map<String, Object> m = (Map<String, Object>) rawMap;

        Object rawLines = m.get("lines");
        if (!(rawLines instanceof List<?> list)) return null;
        List<PromptBook.ScriptLine> lines = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> lm = castMap(item);
            if (lm == null) continue;
            lines.add(new PromptBook.ScriptLine(
                str(lm, "id", null),
                str(lm, "speaker", null),
                str(lm, "delivery", null),
                str(lm, "text", ""),
                str(lm, "timing", null),
                str(lm, "notes", null)
            ));
        }
        return new PromptBook.DeptScript(lines);
    }

    // -----------------------------------------------------------------------
    // Params
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<PromptBook.ParamSpec> parseParams(Object raw, String path) {
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        List<PromptBook.ParamSpec> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = castMap(item);
            if (m == null) continue;
            String name = str(m, "name", null);
            if (name == null) {
                log.warning("[Tech] Param missing 'name' in " + path);
                continue;
            }
            PromptBook.ParamType type = PromptBook.ParamType.from(str(m, "type", "numeric"));
            Object value = m.get("value");
            // Normalise numeric values to Double
            if (type == PromptBook.ParamType.NUMERIC && value instanceof Number n) {
                value = n.doubleValue();
            }
            Double step  = dblOrNull(m, "step");
            Double min   = dblOrNull(m, "min");
            Double max   = dblOrNull(m, "max");
            boolean locked = bool(m, "locked");
            String notes   = str(m, "notes", null);
            result.add(new PromptBook.ParamSpec(name, type, value, step, min, max, locked, notes));
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Readiness
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private PromptBook.Readiness parseReadiness(Map<String, Object> m) {
        if (m == null) return new PromptBook.Readiness(List.of(), List.of());
        List<String> open   = strList(m.get("open"));
        List<String> closed = strList(m.get("closed"));
        return new PromptBook.Readiness(open, closed);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Decimal-aware scene number comparator (parses "00.1" as 0.1, "01" as 1.0, etc.). */
    static double parseSceneNumber(PromptBook.SceneSpec scene) {
        if (scene == null || scene.sceneNumber() == null) return 999;
        try { return Double.parseDouble(scene.sceneNumber()); }
        catch (NumberFormatException e) { return 999; }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object raw) {
        if (raw instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return null;
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        if (v == null) return def;
        return v.toString().trim();
    }

    private static int intVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }

    private static boolean bool(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return "true".equalsIgnoreCase(s);
        return false;
    }

    private static Double dblOrNull(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Object raw) {
        if (!(raw instanceof List<?> list)) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (Object item : list) if (item != null) result.add(item.toString());
        return result;
    }
}
