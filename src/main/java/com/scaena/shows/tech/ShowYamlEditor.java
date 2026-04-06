package com.scaena.shows.tech;

import com.scaena.shows.registry.YamlLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * In-memory mutation model for a show's YAML file.
 *
 * Two-layer scope (per OPS-029 design session §4):
 *
 *   Layer 1 — show timeline CUE reference mutations:
 *     Tick shift: adjust the `at` value of a CUE ref in the show's top-level timeline.
 *
 *   Layer 2 — event-level mutations within a cue:
 *     Written inline to rawYaml as an `events:` key on the CUE ref timeline entry.
 *     On first Layer 2 edit, the cue file's events are loaded and copied inline.
 *     [Save as Preset] promotes inline events to cues/[presetId].yml.
 *
 * One file in memory. All mutations target the show's own timeline. No silent
 * cross-show mutations.
 *
 * Note: SnakeYAML does not preserve YAML comments. Saving via saveToShowYaml()
 * will drop comments from the original file. This is acceptable for a rehearsal tool.
 */
public final class ShowYamlEditor {

    private final Map<String, Object> rawYaml;
    private final File                sourceFile;
    private final File                dataFolder;
    private final PromptBook          book;
    private final Logger              log;

    private ShowYamlEditor(Map<String, Object> rawYaml, File sourceFile,
                           File dataFolder, PromptBook book, Logger log) {
        this.rawYaml    = rawYaml;
        this.sourceFile = sourceFile;
        this.dataFolder = dataFolder;
        this.book       = book;
        this.log        = log;
    }

    // -----------------------------------------------------------------------
    // Factory
    // -----------------------------------------------------------------------

    /**
     * Load the show's YAML file into a ShowYamlEditor.
     *
     * Search order: shows/[showId].yml, then shows/[showId]/[showId].yml.
     * Returns null if neither file exists or the file cannot be read.
     */
    public static ShowYamlEditor load(String showId, File dataFolder,
                                      PromptBook book, Logger log) {
        File flat   = new File(dataFolder, "shows/" + showId + ".yml");
        File nested = new File(dataFolder, "shows/" + showId + "/" + showId + ".yml");

        File source = flat.exists() ? flat : (nested.exists() ? nested : null);
        if (source == null) {
            log.warning("[Tech] ShowYamlEditor: no show YAML found for " + showId);
            return null;
        }

        try {
            Map<String, Object> raw = new LinkedHashMap<>(YamlLoader.load(source));
            ensureMutableTimeline(raw);
            return new ShowYamlEditor(raw, source, dataFolder, book, log);
        } catch (IOException e) {
            log.warning("[Tech] ShowYamlEditor: failed to load " + source + ": " + e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Query helpers
    // -----------------------------------------------------------------------

    /**
     * Return all CUE ref timeline entries for the given scene, sorted by tick.
     * Scene boundaries come from PromptBook.SceneSpec.tickStart.
     */
    public List<Map<String, Object>> getSceneCueRefs(String sceneId) {
        PromptBook.SceneSpec scene = book.findScene(sceneId);
        if (scene == null) return List.of();

        long sceneStart = scene.tickStart();
        long sceneEnd   = nextSceneTickStart(sceneId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> entry : getTimeline()) {
            if (!"CUE".equals(entry.get("type"))) continue;
            long at = toLong(entry.get("at"));
            if (at >= sceneStart && at < sceneEnd) {
                result.add(entry);
            }
        }
        // Already in timeline order; sort by at for safety
        result.sort(Comparator.comparingLong(e -> toLong(e.get("at"))));
        return result;
    }

    /**
     * True if the cue file on disk has no events (empty or missing timeline).
     * Checks the original cue file — not any inline Layer 2 overrides.
     */
    public boolean isCueStub(String cueId) {
        File cueFile = new File(dataFolder, "cues/" + cueId + ".yml");
        if (!cueFile.exists()) return true;
        try {
            Map<String, Object> cueRaw = YamlLoader.load(cueFile);
            Object timeline = cueRaw.get("timeline");
            return !(timeline instanceof List<?> list) || list.isEmpty();
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Format a tick as "Xs | Nt".
     * Drops the decimal when seconds is a clean integer (6s not 6.0s).
     */
    public static String formatTick(int tick) {
        double seconds = tick / 20.0;
        String secStr;
        if (seconds == Math.floor(seconds)) {
            secStr = String.valueOf((int) seconds);
        } else {
            secStr = String.format("%.2f", seconds)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
        }
        return secStr + "s | " + tick + "t";
    }

    // -----------------------------------------------------------------------
    // Layer 1 — show timeline CUE reference mutations
    // -----------------------------------------------------------------------

    /**
     * Shift the `at` tick of the timeline entry at the given index.
     *
     * @param timelineIndex 0-based index into the show's top-level timeline
     * @param deltaTicks    positive = later, negative = earlier; clamped to 0
     */
    public void shiftCueRefTick(int timelineIndex, int deltaTicks) {
        List<Map<String, Object>> timeline = getTimeline();
        if (timelineIndex < 0 || timelineIndex >= timeline.size()) return;
        Map<String, Object> entry = timeline.get(timelineIndex);
        long current = toLong(entry.get("at"));
        entry.put("at", (int) Math.max(0, current + deltaTicks));
    }

    // -----------------------------------------------------------------------
    // Layer 2 — event-level mutations within a cue (written inline to rawYaml)
    // -----------------------------------------------------------------------

    /**
     * Patch a single param on an event within a cue.
     * Loads the cue's events inline from disk on the first Layer 2 edit.
     *
     * @param cueId      the cue ID as it appears in the timeline's cue_id field
     * @param eventIndex 0-based index into the cue's event list
     * @param paramKey   the YAML field name to update
     * @param newValue   the new value (String, Integer, Double, Boolean, etc.)
     */
    public void patchEventParam(String cueId, int eventIndex,
                                String paramKey, Object newValue) {
        List<Map<String, Object>> events = getOrLoadInlineEvents(cueId);
        if (eventIndex < 0 || eventIndex >= events.size()) return;
        events.get(eventIndex).put(paramKey, newValue);
    }

    /**
     * Shift the `at` tick of an event within a cue's inline event list.
     */
    public void shiftEventTick(String cueId, int eventIndex, int deltaTicks) {
        List<Map<String, Object>> events = getOrLoadInlineEvents(cueId);
        if (eventIndex < 0 || eventIndex >= events.size()) return;
        Map<String, Object> event = events.get(eventIndex);
        long current = toLong(event.get("at"));
        event.put("at", (int) Math.max(0, current + deltaTicks));
    }

    /**
     * Insert a new event into a cue's inline event list, maintaining tick order.
     *
     * @param cueId     the cue to insert into
     * @param atTick    the tick at which the new event fires
     * @param eventYaml the full event map (type + fields); `at` will be overwritten
     */
    public void insertEvent(String cueId, int atTick, Map<String, Object> eventYaml) {
        List<Map<String, Object>> events = getOrLoadInlineEvents(cueId);
        Map<String, Object> entry = new LinkedHashMap<>(eventYaml);
        entry.put("at", atTick);

        // Insert in tick-sorted order
        int insertAt = events.size();
        for (int i = 0; i < events.size(); i++) {
            if (toLong(events.get(i).get("at")) > atTick) {
                insertAt = i;
                break;
            }
        }
        events.add(insertAt, entry);
    }

    /**
     * Remove an event from a cue's inline event list.
     *
     * @param cueId      the cue to remove from
     * @param eventIndex 0-based index into the cue's event list
     */
    public void removeEvent(String cueId, int eventIndex) {
        List<Map<String, Object>> events = getOrLoadInlineEvents(cueId);
        if (eventIndex >= 0 && eventIndex < events.size()) {
            events.remove(eventIndex);
        }
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    /**
     * Write rawYaml back to the source show file.
     *
     * @return true on success, false if an IOException occurred
     */
    public boolean saveToShowYaml() {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(sourceFile), StandardCharsets.UTF_8)) {
            buildDumper().dump(rawYaml, writer);
            log.info("[Tech] ShowYamlEditor: saved " + sourceFile.getName());
            return true;
        } catch (IOException e) {
            log.warning("[Tech] ShowYamlEditor: save failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Promote a cue's inline Layer 2 events to a standalone preset file.
     *
     * The resulting file lives at cues/[presetId].yml with:
     *   id: [presetId], name: "", timeline: [inline events]
     *
     * Requires that the cue has been Layer 2-edited (inline events loaded).
     * If no inline events exist, the call is a no-op and returns false.
     *
     * @return true on success
     */
    @SuppressWarnings("unchecked")
    public boolean saveAsPreset(String cueId, String presetId) {
        Map<String, Object> timelineEntry = findTimelineEntry(cueId);
        if (timelineEntry == null) {
            log.warning("[Tech] saveAsPreset: no timeline entry for cue " + cueId);
            return false;
        }

        Object eventsObj = timelineEntry.get("events");
        if (!(eventsObj instanceof List<?> list) || list.isEmpty()) {
            log.warning("[Tech] saveAsPreset: no inline events for cue " + cueId
                + " — load and edit first");
            return false;
        }

        List<Map<String, Object>> inlineEvents = (List<Map<String, Object>>) list;

        Map<String, Object> presetMap = new LinkedHashMap<>();
        presetMap.put("id", presetId);
        presetMap.put("name", "");
        presetMap.put("timeline", inlineEvents);

        File presetFile = new File(dataFolder, "cues/" + presetId + ".yml");
        presetFile.getParentFile().mkdirs();

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(presetFile), StandardCharsets.UTF_8)) {
            buildDumper().dump(presetMap, writer);
            log.info("[Tech] ShowYamlEditor: saved preset " + presetId);
            return true;
        } catch (IOException e) {
            log.warning("[Tech] ShowYamlEditor: preset save failed: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Package-private accessors (for TechManager)
    // -----------------------------------------------------------------------

    /** The raw parsed show YAML — used for RunningShow construction in preview mode. */
    Map<String, Object> rawYaml() { return rawYaml; }

    /** The file this editor loaded from and will write back to. */
    File sourceFile() { return sourceFile; }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTimeline() {
        Object t = rawYaml.get("timeline");
        if (t instanceof List<?> list) return (List<Map<String, Object>>) list;
        return List.of();
    }

    /** Find the first CUE ref timeline entry with the given cue_id. */
    private Map<String, Object> findTimelineEntry(String cueId) {
        for (Map<String, Object> entry : getTimeline()) {
            if ("CUE".equals(entry.get("type")) && cueId.equals(entry.get("cue_id"))) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Return the inline `events:` list for the given cue ref.
     *
     * On first access: loads the cue file's timeline and copies it into the
     * timeline entry as `events:`. Subsequent accesses return the same list.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOrLoadInlineEvents(String cueId) {
        Map<String, Object> entry = findTimelineEntry(cueId);
        if (entry == null) {
            log.warning("[Tech] ShowYamlEditor: no timeline entry for cue " + cueId);
            return new ArrayList<>();
        }

        Object existing = entry.get("events");
        if (existing instanceof List<?> list) return (List<Map<String, Object>>) list;

        // Load from the cue file and copy inline
        File cueFile = new File(dataFolder, "cues/" + cueId + ".yml");
        List<Map<String, Object>> loaded = new ArrayList<>();
        if (cueFile.exists()) {
            try {
                Map<String, Object> cueRaw = YamlLoader.load(cueFile);
                Object timeline = cueRaw.get("timeline");
                if (timeline instanceof List<?> tl) {
                    for (Object e : tl) {
                        if (e instanceof Map<?, ?> m) {
                            loaded.add(new LinkedHashMap<>((Map<String, Object>) m));
                        }
                    }
                }
            } catch (IOException e) {
                log.warning("[Tech] ShowYamlEditor: failed to load cue file "
                    + cueId + ": " + e.getMessage());
            }
        }

        entry.put("events", loaded);
        return loaded;
    }

    /**
     * The tickStart of the scene following sceneId, or show duration_ticks for the last scene.
     * Returns Long.MAX_VALUE if duration_ticks is not set.
     */
    private long nextSceneTickStart(String sceneId) {
        List<PromptBook.SceneSpec> scenes = book.scenes();
        for (int i = 0; i < scenes.size() - 1; i++) {
            if (sceneId.equals(scenes.get(i).id())) {
                return scenes.get(i + 1).tickStart();
            }
        }
        // Last scene — use show's duration_ticks
        Object dur = rawYaml.get("duration_ticks");
        return (dur instanceof Number n) ? n.longValue() : Long.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    private static void ensureMutableTimeline(Map<String, Object> raw) {
        Object t = raw.get("timeline");
        if (t instanceof List<?> list) {
            List<Map<String, Object>> mutable = new ArrayList<>();
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> m) {
                    mutable.add(new LinkedHashMap<>((Map<String, Object>) m));
                }
            }
            raw.put("timeline", mutable);
        } else {
            raw.put("timeline", new ArrayList<>());
        }
    }

    private static long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return 0L;
    }

    private static Yaml buildDumper() {
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setIndent(2);
        opts.setPrettyFlow(true);
        return new Yaml(opts);
    }
}
