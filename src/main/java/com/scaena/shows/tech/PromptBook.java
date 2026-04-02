package com.scaena.shows.tech;

import java.util.List;
import java.util.Map;

/**
 * Data model for [show_id].prompt-book.yml — the authoritative committed state of a show.
 *
 * Every record here is immutable (Java record). Loaded by PromptBookLoader at TechSession
 * init; written back (params only) by PromptBookWriter on SAVE.
 *
 * Mark positions are NOT stored here — they live in scout_captures/[showId]/[date].yml.
 * The Prompt Book stores mark names/roles and what to DO at each mark.
 */
public record PromptBook(
    String showId,
    String title,
    String stage,
    String world,
    String durationTarget,
    String updated,
    ShowStructure structure,
    List<SceneSpec> scenes,          // sorted by scene_number on load
    Map<String, Object> mechanics,   // raw — informational, not editable via tech panel
    List<ParamSpec> params,
    Readiness readiness
) {

    // -----------------------------------------------------------------------
    // Lookup helpers
    // -----------------------------------------------------------------------

    /** Find a scene by its id field. Returns null if not found. */
    public SceneSpec findScene(String sceneId) {
        if (sceneId == null || scenes == null) return null;
        return scenes.stream()
            .filter(s -> sceneId.equals(s.id()))
            .findFirst()
            .orElse(null);
    }

    /** Find a param spec by name. Returns null if not found. */
    public ParamSpec findParam(String name) {
        if (name == null || params == null) return null;
        return params.stream()
            .filter(p -> name.equals(p.name()))
            .findFirst()
            .orElse(null);
    }

    // -----------------------------------------------------------------------
    // Inner record: ShowStructure
    // -----------------------------------------------------------------------

    public record ShowStructure(
        String format,
        String sequence,
        int    expeditionCount,
        int    aSectionsTotal,
        int    armorStandFills,
        String notes
    ) {}

    // -----------------------------------------------------------------------
    // Enums
    // -----------------------------------------------------------------------

    public enum StageStatus {
        PENDING, SCOUTED, YAML_READY;

        public static StageStatus from(String s) {
            if (s == null) return PENDING;
            return switch (s.toLowerCase().replace("-", "_")) {
                case "scouted"    -> SCOUTED;
                case "yaml_ready" -> YAML_READY;
                default           -> PENDING;
            };
        }
    }

    public enum ParamType {
        NUMERIC, TEXT;

        public static ParamType from(String s) {
            if ("text".equalsIgnoreCase(s)) return TEXT;
            return NUMERIC;
        }
    }

    // -----------------------------------------------------------------------
    // Inner record: SceneSpec
    // -----------------------------------------------------------------------

    public record SceneSpec(
        String      id,
        String      sceneNumber,    // decimal string — sort key
        String      label,
        String      arrivalMark,
        StageStatus status,
        String      biome,
        String      world,
        DeptCasting  casting,
        DeptWardrobe wardrobe,
        DeptSet      set,
        DeptLighting lighting,
        DeptFireworks fireworks,
        DeptScript   script
    ) {
        /** True if this scene has at least one authored casting entry. */
        public boolean hasCasting()  { return casting  != null && !casting.entries().isEmpty(); }
        /** True if this scene has at least one authored wardrobe entry. */
        public boolean hasWardrobe() { return wardrobe != null && !wardrobe.entries().isEmpty(); }
        /** True if this scene has at least one authored set entry with a mark. */
        public boolean hasSet()      { return set      != null && !set.entries().isEmpty(); }
        /** True if this scene has lighting data (time_of_day or sources). */
        public boolean hasLighting() { return lighting != null; }
        /** True if fireworks.present is true for this scene. */
        public boolean hasFireworks(){ return fireworks != null && fireworks.present(); }
        /** True if this scene has at least one script line. */
        public boolean hasScript()   { return script   != null && !script.lines().isEmpty(); }
    }

    // -----------------------------------------------------------------------
    // Casting
    // -----------------------------------------------------------------------

    public record DeptCasting(List<CastingEntry> entries) {}

    public record CastingEntry(
        String  role,
        String  mark,
        String  entityType,
        String  subtype,        // profession (Villager), variant (Horse, Cat), etc.
        String  displayName,
        boolean aiLocked,
        String  notes
    ) {}

    // -----------------------------------------------------------------------
    // Wardrobe
    // -----------------------------------------------------------------------

    public record DeptWardrobe(List<WardrobeEntry> entries) {}

    public record WardrobeEntry(
        String entityMark,
        String helmet,
        String chestplate,
        String leggings,
        String boots,
        String bootsDye,           // hex string e.g. "#3B2A1A"; null = no dye
        String mainHand,
        String mainHandEnchant,    // "ENCHANT_NAME:level" e.g. "SHARPNESS:1"; null = plain
        String notes
    ) {}

    // -----------------------------------------------------------------------
    // Set
    // -----------------------------------------------------------------------

    /**
     * World-scoped integer coordinate triple used for bbox corners.
     * Stored in the prompt-book under set.bbox_min / set.bbox_max.
     */
    public record BboxPoint(String world, int x, int y, int z) {}

    /**
     * Set department for a scene.
     *
     * entries      — authored block changes (mark-positioned, always present if set is active)
     * bboxMin/Max  — bounding box corners for build-mode change tracking; null if not yet defined
     * activeBuild  — filename stem of the active set build version (no extension); null if none
     */
    public record DeptSet(
        List<SetEntry> entries,
        BboxPoint      bboxMin,
        BboxPoint      bboxMax,
        String         activeBuild
    ) {
        /** True if both bbox corners are defined. */
        public boolean hasBbox() { return bboxMin != null && bboxMax != null; }
    }

    /**
     * A block change at a named mark position.
     * mark may be null for note-only entries (e.g. site_c lava assessment note).
     */
    public record SetEntry(
        String mark,         // may be null for note-only entries
        String blockType,    // Material name e.g. "BLAST_FURNACE"
        String blockState,   // block data string e.g. "lit=true"
        String notes
    ) {}

    // -----------------------------------------------------------------------
    // Lighting
    // -----------------------------------------------------------------------

    public record DeptLighting(
        Integer       timeOfDay,   // null = no time change on LOAD
        String        notes,
        List<LightSource> sources
    ) {}

    public record LightSource(
        String mark,
        int    level,
        String quality,
        String role,
        String notes,
        String status    // may be "TBD"
    ) {}

    // -----------------------------------------------------------------------
    // Fireworks
    // -----------------------------------------------------------------------

    public record DeptFireworks(
        boolean present,
        String  pattern,
        String  timing,
        String  notes
    ) {}

    // -----------------------------------------------------------------------
    // Script
    // -----------------------------------------------------------------------

    public record DeptScript(List<ScriptLine> lines) {}

    public record ScriptLine(
        String id,
        String speaker,
        String delivery,
        String text,
        String timing,
        String notes
    ) {}

    // -----------------------------------------------------------------------
    // Params
    // -----------------------------------------------------------------------

    public record ParamSpec(
        String    name,
        ParamType type,
        Object    value,      // Double for NUMERIC, String for TEXT
        Double    step,       // null for TEXT params
        Double    min,        // null for TEXT params
        Double    max,        // null for TEXT params
        boolean   locked,
        String    notes
    ) {
        /** Returns the current value as a display string. */
        public String displayValue() {
            if (value == null) return "—";
            if (type == ParamType.NUMERIC && value instanceof Number n) {
                double d = n.doubleValue();
                // Show as integer if no fractional part
                return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            return value.toString();
        }
    }

    // -----------------------------------------------------------------------
    // Readiness
    // -----------------------------------------------------------------------

    public record Readiness(
        List<String> open,
        List<String> closed
    ) {}
}
