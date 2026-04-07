package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.registry.CueRegistry;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * In-memory show YAML editor for Phase 2.
 *
 * Holds the raw YAML map loaded from the show file on Phase 2 entry and
 * provides typed mutations at two layers:
 *
 *   Layer 1 — show timeline entries (tick shifts on CUE references)
 *   Layer 2 — event-level content within cues (param patches, tick shifts,
 *              event insert/remove); writes are made into rawYaml itself,
 *              treating the show YAML as the authoritative in-session store.
 *
 * Layer 2 edits are NOT written to cues/*.yml until saveAsPreset() is called
 * explicitly by the player.
 *
 * Thread model: all calls are on the main server thread.
 */
public final class ShowYamlEditor {

    private static final int TICKS_PER_SECOND = 20;

    private final Map<String, Object> rawYaml;
    private final File                sourceFile;
    private final PromptBook          book;
    private final CueRegistry         cueRegistry;
    private final Logger              log;

    public ShowYamlEditor(
        Map<String, Object> rawYaml,
        File                sourceFile,
        PromptBook          book,
        CueRegistry         cueRegistry,
        Logger              log
    ) {
        this.rawYaml    = rawYaml;
        this.sourceFile = sourceFile;
        this.book       = book;
        this.cueRegistry = cueRegistry;
        this.log        = log;
    }

    // ------------------------------------------------------------------
    // Layer 1 — show timeline mutations
    // ------------------------------------------------------------------

    /**
     * Shift the `at` tick of a CUE reference in the show's top-level timeline.
     *
     * @param timelineIndex index into the flat timeline list (0-based)
     * @param deltaTicks    positive = later; negative = earlier (clamped to 0)
     */
    @SuppressWarnings("unchecked")
    public void shiftCueRefTick(int timelineIndex, int deltaTicks) {
        List<Map<String, Object>> timeline = getTimeline();
        if (timelineIndex < 0 || timelineIndex >= timeline.size()) return;

        Map<String, Object> entry = timeline.get(timelineIndex);
        int current = intVal(entry, "at", 0);
        int updated = Math.max(0, current + deltaTicks);
        entry.put("at", updated);
    }

    // ------------------------------------------------------------------
    // Layer 2 — event-level mutations within cues
    // ------------------------------------------------------------------

    // NOTE: Layer 2 edits are applied to an inline events block stored inside
    // rawYaml under a "_cue_overrides" key (keyed by cue ID). On saveToShowYaml()
    // these overrides are written out as inline YAML alongside the CUE refs.
    // On saveAsPreset(), the override is promoted to a cues/*.yml file.
    //
    // Full implementation is Phase 2 Group 5 territory. Stubs are provided here
    // so the class compiles and Group 3/4 can wire against these signatures.

    /** Patch a single param key on the given event within the named cue. */
    public void patchEventParam(String cueId, int eventIndex, String paramKey, Object newValue) {
        Map<String, Object> events = ensureCueOverride(cueId);
        List<Map<String, Object>> list = getOrInitEventList(events);
        if (eventIndex >= 0 && eventIndex < list.size()) {
            list.get(eventIndex).put(paramKey, newValue);
        }
    }

    /** Shift the `at` tick of an event within the named cue. */
    public void shiftEventTick(String cueId, int eventIndex, int deltaTicks) {
        Map<String, Object> events = ensureCueOverride(cueId);
        List<Map<String, Object>> list = getOrInitEventList(events);
        if (eventIndex >= 0 && eventIndex < list.size()) {
            Map<String, Object> event = list.get(eventIndex);
            int current = intVal(event, "at", 0);
            event.put("at", Math.max(0, current + deltaTicks));
        }
    }

    /** Insert a new event into the named cue at the given tick. */
    public void insertEvent(String cueId, int atTick, Map<String, Object> eventYaml) {
        Map<String, Object> events = ensureCueOverride(cueId);
        List<Map<String, Object>> list = getOrInitEventList(events);
        Map<String, Object> copy = new LinkedHashMap<>(eventYaml);
        copy.put("at", atTick);
        // Insert in tick order
        int insertAt = list.size();
        for (int i = 0; i < list.size(); i++) {
            if (intVal(list.get(i), "at", 0) > atTick) {
                insertAt = i;
                break;
            }
        }
        list.add(insertAt, copy);
    }

    /**
     * Replace the entire event list for the given cue in the in-session override.
     * Used by department edit sessions that build their own event map (e.g. CastingEditSession).
     * Calling this with an empty list clears all events for the cue.
     */
    @SuppressWarnings("unchecked")
    public void setCueEvents(String cueId, List<Map<String, Object>> events) {
        Map<String, Object> override = ensureCueOverride(cueId);
        override.put("events", new ArrayList<>(events));
    }

    /** Remove the event at the given index from the named cue. */
    public void removeEvent(String cueId, int eventIndex) {
        Map<String, Object> events = ensureCueOverride(cueId);
        List<Map<String, Object>> list = getOrInitEventList(events);
        if (eventIndex >= 0 && eventIndex < list.size()) {
            list.remove(eventIndex);
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    /**
     * Write rawYaml back to the show file on disk.
     *
     * @return true on success, false if an IOException occurred
     */
    public boolean saveToShowYaml() {
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setIndent(2);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(sourceFile), StandardCharsets.UTF_8)) {
            yaml.dump(rawYaml, writer);
            return true;
        } catch (IOException e) {
            log.warning("[ShowYamlEditor] Failed to save " + sourceFile.getName()
                + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Promote the in-session cue content for the given cue ID to the preset
     * library by writing it to cues/[presetId].yml.
     *
     * The preset file is written under the plugin's cues/ directory.
     *
     * @param cueId    the cue whose override (or original content) to promote
     * @param presetId the target preset ID (e.g. "casting.zombie.warrior_enter")
     * @return true on success
     */
    public boolean saveAsPreset(String cueId, String presetId) {
        // TODO (Group 5): Resolve the cue's current content (override or original),
        // construct a full cue YAML block, and write to cues/[presetId].yml.
        log.info("[ShowYamlEditor] saveAsPreset not yet implemented: cueId=" + cueId
            + ", presetId=" + presetId);
        return false;
    }

    // ------------------------------------------------------------------
    // Query helpers
    // ------------------------------------------------------------------

    /**
     * Return the timeline CUE reference entries that belong to the given scene.
     *
     * Entries are filtered by tick range: [scene.tickStart, nextScene.tickStart).
     * The last scene runs to the show's duration_ticks.
     *
     * Returns an empty list if the scene ID is unknown or the timeline is empty.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getSceneCueRefs(String sceneId) {
        if (sceneId == null || book == null) return List.of();

        PromptBook.SceneSpec scene = book.findScene(sceneId);
        if (scene == null) return List.of();

        int tickStart = scene.tickStart();
        int tickEnd   = tickEndForScene(sceneId);

        List<Map<String, Object>> timeline = getTimeline();
        List<Map<String, Object>> result   = new ArrayList<>();

        for (Map<String, Object> entry : timeline) {
            int at = intVal(entry, "at", 0);
            if (at >= tickStart && at < tickEnd) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns true if the named cue has no events (either in its original cue
     * file or in any in-session override). Stub cues render with "(empty)" in
     * the panel but still accept [Edit ▸].
     */
    @SuppressWarnings("unchecked")
    public boolean isCueStub(String cueId) {
        // Check in-session override first
        Map<String, Object> overrides = getCueOverrides();
        if (overrides.containsKey(cueId)) {
            List<?> events = (List<?>) ((Map<?, ?>) overrides.get(cueId)).get("events");
            if (events != null) return events.isEmpty();
        }
        // Fall back to the registered cue
        Cue cue = cueRegistry.get(cueId);
        return cue == null || cue.timeline.isEmpty();
    }

    /**
     * Format an absolute tick as the universal display string: "Xs | Nt".
     *
     * Drops the decimal when seconds is a clean integer: "6s | 120t"
     * Retains the decimal only when fractional: "6.25s | 125t"
     */
    public String formatTick(int tick) {
        double seconds = (double) tick / TICKS_PER_SECOND;
        String secStr = (seconds == Math.floor(seconds))
            ? String.valueOf((long) seconds)
            : String.valueOf(seconds);
        return secStr + "s | " + tick + "t";
    }

    /** Total number of entries in the flat timeline list. */
    public int getTimelineSize() {
        return getTimeline().size();
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTimeline() {
        Object raw = rawYaml.get("timeline");
        if (raw instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        List<Map<String, Object>> empty = new ArrayList<>();
        rawYaml.put("timeline", empty);
        return empty;
    }

    /**
     * Returns the first event map from the cue's in-session override, or an empty
     * map if no override or no events exist.
     * Used by EffectsEditSession to seed EFFECT_PATTERN fields from a prior save.
     */
    @SuppressWarnings("unchecked")
    Map<String, Object> getCueOverrideFirstEvent(String cueId) {
        Map<String, Object> overrides = getCueOverrides();
        Object override = overrides.get(cueId);
        if (!(override instanceof Map<?, ?> overMap)) return Map.of();
        Object eventsObj = overMap.get("events");
        if (!(eventsObj instanceof List<?> events) || events.isEmpty()) return Map.of();
        Object first = events.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) return Map.of();
        return (Map<String, Object>) firstMap;
    }

    /**
     * Returns the {@code type} string from the first event in the cue's in-session
     * override, or {@code null} if no override or no events exist.
     * Used by EffectsEditSession to detect EFFECT_PATTERN mode (not parseable by
     * EventParser since it is not yet in EventType).
     */
    @SuppressWarnings("unchecked")
    String getCueOverrideFirstEventType(String cueId) {
        Map<String, Object> overrides = getCueOverrides();
        Object override = overrides.get(cueId);
        if (!(override instanceof Map<?, ?> overMap)) return null;
        Object eventsObj = overMap.get("events");
        if (!(eventsObj instanceof List<?> events) || events.isEmpty()) return null;
        Object first = events.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) return null;
        return (String) firstMap.get("type");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCueOverrides() {
        return (Map<String, Object>) rawYaml.computeIfAbsent(
            "_cue_overrides", k -> new LinkedHashMap<String, Object>());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ensureCueOverride(String cueId) {
        Map<String, Object> overrides = getCueOverrides();
        return (Map<String, Object>) overrides.computeIfAbsent(
            cueId, k -> new LinkedHashMap<String, Object>());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOrInitEventList(Map<String, Object> cueOverride) {
        return (List<Map<String, Object>>) cueOverride.computeIfAbsent(
            "events", k -> new ArrayList<Map<String, Object>>());
    }

    /** The upper tick boundary (exclusive) for a scene. */
    private int tickEndForScene(String sceneId) {
        if (book == null || book.scenes() == null) return Integer.MAX_VALUE;
        List<PromptBook.SceneSpec> scenes = book.scenes();
        for (int i = 0; i < scenes.size(); i++) {
            if (sceneId.equals(scenes.get(i).id())) {
                if (i + 1 < scenes.size()) {
                    return scenes.get(i + 1).tickStart();
                } else {
                    // Last scene: runs to show duration
                    Object dur = rawYaml.get("duration_ticks");
                    return (dur instanceof Number n) ? n.intValue() : Integer.MAX_VALUE;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private static int intVal(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }
}
