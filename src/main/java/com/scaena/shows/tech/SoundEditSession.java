package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.SoundEvents.SoundEvent;
import com.scaena.shows.model.event.SoundEvents.StopSoundEvent;
import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Sound.
 *
 * Handles two event types found in sound cues:
 *
 *   SOUND      — sound_id, category, volume, pitch, max_duration_ticks (optional)
 *   STOP_SOUND — source channel selector only
 *
 * Auto-preview: session-level toggle (TechCueSession.autoPreview). When ON, any SOUND
 * param change immediately refires the sound at the player's location so they hear it
 * in real time. When OFF, explicit [▶ Hear it] fires on demand. STOP_SOUND has no preview.
 *
 * Preset naming (SOUND only): sound.[category].[pitch_register].[slug]
 * STOP_SOUND is too trivial to preset (single field).
 *
 * Territory: Sound owns SOUND and STOP_SOUND events only.
 *
 * Spec: kb/system/phase2-department-panels.md §Sound
 */
public final class SoundEditSession implements DeptEditSession {

    // -----------------------------------------------------------------------
    // Curated lists
    // -----------------------------------------------------------------------

    public static final List<String> CATEGORIES = List.of(
        "ambient", "hostile", "music", "record", "weather",
        "block", "master", "player", "voice"
    );

    public static final List<String> STOP_SOURCES = List.of(
        "ambient", "hostile", "music", "record", "weather",
        "block", "master", "player", "voice", "all"
    );

    /**
     * Curated sound register labels — parallel to REGISTER_SOUNDS.
     * Organized by the Sound KB curated registers.
     */
    public static final String[] REGISTER_LABELS = {
        "Atmosphere", "Presence", "Warmth", "Transition", "Shadow", "Tension"
    };

    /**
     * Curated sound IDs per register, parallel to REGISTER_LABELS.
     * Sourced from kb/departments/sound/sound.kb.md §Sound ID Reference.
     */
    public static final List<List<String>> REGISTER_SOUNDS = List.of(
        // Atmosphere — environmental beds
        List.of(
            "minecraft:ambient.cave",
            "minecraft:ambient.basalt_deltas.loop",
            "minecraft:ambient.crimson_forest.loop",
            "minecraft:ambient.warped_forest.loop",
            "minecraft:ambient.soul_sand_valley.loop",
            "minecraft:ambient.nether_wastes.loop",
            "minecraft:weather.rain"
        ),
        // Presence — arrival, weight, significance
        List.of(
            "minecraft:entity.elder_guardian.curse",
            "minecraft:entity.warden.heartbeat",
            "minecraft:entity.warden.roar",
            "minecraft:entity.ender_dragon.growl",
            "minecraft:entity.ender_dragon.ambient",
            "minecraft:block.portal.ambient",
            "minecraft:entity.lightning_bolt.thunder"
        ),
        // Warmth — joy, community, comfort
        List.of(
            "minecraft:entity.allay.ambient_with_item",
            "minecraft:entity.allay.item_given",
            "minecraft:entity.villager.celebrate",
            "minecraft:block.note_block.bell",
            "minecraft:block.note_block.chime",
            "minecraft:entity.player.levelup",
            "minecraft:block.amethyst_cluster.hit"
        ),
        // Transition — things beginning or ending
        List.of(
            "minecraft:block.beacon.activate",
            "minecraft:block.beacon.deactivate",
            "minecraft:entity.experience_orb.pickup",
            "minecraft:block.end_portal.frame.fill",
            "minecraft:block.conduit.activate",
            "minecraft:ui.toast.challenge_complete"
        ),
        // Shadow — weight, stillness, quiet depth
        List.of(
            "minecraft:entity.iron_golem.hurt",
            "minecraft:entity.iron_golem.death",
            "minecraft:entity.wolf.whine",
            "minecraft:entity.ghast.moan",
            "minecraft:entity.ghast.scream",
            "minecraft:block.glass.break"
        ),
        // Tension — dread, the unannounced
        List.of(
            "minecraft:entity.creeper.primed",
            "minecraft:entity.enderman.stare",
            "minecraft:entity.phantom.ambient",
            "minecraft:entity.spider.ambient",
            "minecraft:block.sculk_sensor.clicking"
        )
    );

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode { SOUND, STOP_SOUND }

    // -----------------------------------------------------------------------
    // Identity
    // -----------------------------------------------------------------------

    private final String         cueId;
    private final Player         player;
    private final TechCueSession cueSession;
    private final ShowYamlEditor editor;
    private final CueRegistry    cueRegistry;
    private final Logger         log;

    // -----------------------------------------------------------------------
    // Mode
    // -----------------------------------------------------------------------

    private final EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel)
    // -----------------------------------------------------------------------

    // SOUND fields
    private final String entrySoundId;
    private final String entryCategory;
    private final float  entryVolume;
    private final float  entryPitch;
    private final int    entryMaxDurationTicks; // -1 = not set

    // STOP_SOUND field
    private final String entrySource;

    // -----------------------------------------------------------------------
    // Current mutable state
    // -----------------------------------------------------------------------

    private String currentSoundId;
    private String currentCategory;
    private float  currentVolume;
    private float  currentPitch;
    private int    currentMaxDurationTicks;

    private String currentSource;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Build a SoundEditSession for the given cue.
     * Reads initial state from the CueRegistry. If the cue has no SOUND or
     * STOP_SOUND event, defaults to SOUND mode with sensible defaults.
     */
    public SoundEditSession(
        String         cueId,
        Player         player,
        TechCueSession cueSession,
        ShowYamlEditor editor,
        CueRegistry    cueRegistry,
        Logger         log
    ) {
        this.cueId       = cueId;
        this.player      = player;
        this.cueSession  = cueSession;
        this.editor      = editor;
        this.cueRegistry = cueRegistry;
        this.log         = log;

        // Detect event type and read initial state
        Object found = findSoundEvent(cueId);
        if (found instanceof SoundEvent se) {
            this.mode                  = EventMode.SOUND;
            this.entrySoundId          = se.soundId;
            this.entryCategory         = se.category;
            this.entryVolume           = se.volume;
            this.entryPitch            = se.pitch;
            this.entryMaxDurationTicks = se.maxDurationTicks;
            this.entrySource           = "music"; // unused in SOUND mode
        } else if (found instanceof StopSoundEvent sse) {
            this.mode                  = EventMode.STOP_SOUND;
            this.entrySoundId          = "minecraft:block.note_block.harp"; // unused
            this.entryCategory         = "master"; // unused
            this.entryVolume           = 1.0f;     // unused
            this.entryPitch            = 1.0f;     // unused
            this.entryMaxDurationTicks = -1;        // unused
            this.entrySource           = sse.source;
        } else {
            // Default: SOUND mode
            this.mode                  = EventMode.SOUND;
            this.entrySoundId          = "minecraft:block.note_block.harp";
            this.entryCategory         = "master";
            this.entryVolume           = 1.0f;
            this.entryPitch            = 1.0f;
            this.entryMaxDurationTicks = -1;
            this.entrySource           = "music"; // unused
        }

        // Initialise current state from entry snapshot
        this.currentSoundId          = entrySoundId;
        this.currentCategory         = entryCategory;
        this.currentVolume           = entryVolume;
        this.currentPitch            = entryPitch;
        this.currentMaxDurationTicks = entryMaxDurationTicks;
        this.currentSource           = entrySource;
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "sound"; }

    @Override
    public void onSave() {
        writeToEditor();
    }

    @Override
    public void onSaveAsPreset() {
        writeToEditor();
        if (mode == EventMode.SOUND) {
            // Preset naming: sound.[category].[pitch_register].[slug]
            String reg  = pitchRegisterKey(currentPitch);
            String slug = cueId.contains(".")
                ? cueId.substring(cueId.lastIndexOf('.') + 1)
                : cueId;
            String presetId = "sound." + currentCategory + "." + reg + "." + slug;
            editor.saveAsPreset(cueId, presetId);
        }
        // STOP_SOUND: no preset — single field, too trivial to warrant naming (per spec)
    }

    @Override
    public void onCancel() {
        // Restore entry state — no world side-effects to undo for sound
        currentSoundId          = entrySoundId;
        currentCategory         = entryCategory;
        currentVolume           = entryVolume;
        currentPitch            = entryPitch;
        currentMaxDurationTicks = entryMaxDurationTicks;
        currentSource           = entrySource;
    }

    /**
     * Handle a param change from /scaena tech2 editparam <key> [value...].
     *
     * SOUND keys:
     *   sound_id <id>          — set sound ID
     *   sound_id_panel         — show sound ID selector sub-panel
     *   category <cat>         — set category
     *   volume_up              — volume + 0.1
     *   volume_down            — volume − 0.1
     *   volume <value>         — set volume directly
     *   pitch_up               — pitch + 0.05
     *   pitch_down             — pitch − 0.05
     *   pitch <value>          — set pitch directly
     *   max_dur_up             — max_duration_ticks + 5 (or set to 20 if unset)
     *   max_dur_down           — max_duration_ticks − 5 (or clear if ≤ 0)
     *   max_dur_clear          — remove max_duration_ticks
     *   max_duration_ticks <n> — set max_duration_ticks directly (or "clear")
     *   preview_sound          — fire current sound once at player
     *   auto_preview_toggle    — toggle session auto-preview flag
     *
     * STOP_SOUND keys:
     *   source <src>           — set source channel
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        // ---- SOUND params ----
        if (mode == EventMode.SOUND) {
            switch (lower) {
                case "sound_id" -> {
                    if (value.isBlank()) return false;
                    currentSoundId = value.trim();
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "sound_id_panel" -> {
                    SoundPanelBuilder.sendSoundIdPanel(player, this);
                    return true;
                }
                case "category" -> {
                    if (!CATEGORIES.contains(value.toLowerCase())) {
                        sendError("Unknown category: " + value
                            + ". Valid: " + String.join(", ", CATEGORIES));
                        return true;
                    }
                    currentCategory = value.toLowerCase();
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "volume" -> {
                    try {
                        float v = Float.parseFloat(value);
                        currentVolume = clampVolume(roundFloat(v, 1));
                    } catch (NumberFormatException e) {
                        sendError("Invalid volume: " + value);
                        return true;
                    }
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "volume_up" -> {
                    currentVolume = clampVolume(roundFloat(currentVolume + 0.1f, 1));
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "volume_down" -> {
                    currentVolume = clampVolume(roundFloat(currentVolume - 0.1f, 1));
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "pitch" -> {
                    try {
                        float v = Float.parseFloat(value);
                        currentPitch = clampPitch(roundFloat(v, 2));
                    } catch (NumberFormatException e) {
                        sendError("Invalid pitch: " + value);
                        return true;
                    }
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "pitch_up" -> {
                    currentPitch = clampPitch(roundFloat(currentPitch + 0.05f, 2));
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "pitch_down" -> {
                    currentPitch = clampPitch(roundFloat(currentPitch - 0.05f, 2));
                    autoPreviewSound();
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "max_duration_ticks" -> {
                    if (value.equalsIgnoreCase("clear") || value.equalsIgnoreCase("none")
                            || value.isBlank()) {
                        currentMaxDurationTicks = -1;
                    } else {
                        try {
                            int v = Integer.parseInt(value.trim());
                            currentMaxDurationTicks = Math.max(1, v);
                        } catch (NumberFormatException e) {
                            sendError("Invalid max_duration_ticks: " + value);
                            return true;
                        }
                    }
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "max_dur_up" -> {
                    if (currentMaxDurationTicks < 0) {
                        currentMaxDurationTicks = 20; // default starting point = 1 second
                    } else {
                        currentMaxDurationTicks = Math.min(currentMaxDurationTicks + 5, 6000);
                    }
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "max_dur_down" -> {
                    if (currentMaxDurationTicks > 5) {
                        currentMaxDurationTicks -= 5;
                    } else if (currentMaxDurationTicks > 0) {
                        currentMaxDurationTicks = -1; // drop to unset
                    }
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "max_dur_clear" -> {
                    currentMaxDurationTicks = -1;
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_sound" -> {
                    firePreviewSound();
                    return true;
                }
                case "auto_preview_toggle" -> {
                    cueSession.setAutoPreview(!cueSession.isAutoPreview());
                    SoundPanelBuilder.sendPanel(player, this);
                    return true;
                }
                default -> { return false; }
            }
        }

        // ---- STOP_SOUND params ----
        if (mode == EventMode.STOP_SOUND && lower.equals("source")) {
            if (!STOP_SOURCES.contains(value.toLowerCase())) {
                sendError("Unknown source: " + value
                    + ". Valid: " + String.join(", ", STOP_SOURCES));
                return true;
            }
            currentSource = value.toLowerCase();
            SoundPanelBuilder.sendPanel(player, this);
            return true;
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Accessors (for SoundPanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode()                 { return mode; }
    public String  getCurrentSoundId()         { return currentSoundId; }
    public String  getCurrentCategory()        { return currentCategory; }
    public float   getCurrentVolume()          { return currentVolume; }
    public float   getCurrentPitch()           { return currentPitch; }
    public int     getCurrentMaxDurationTicks(){ return currentMaxDurationTicks; }
    public String  getCurrentSource()          { return currentSource; }
    public boolean isAutoPreview()             { return cueSession.isAutoPreview(); }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /** Write current state as a single-event map into the editor override. */
    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        if (mode == EventMode.SOUND) {
            eventMap.put("at", 0);
            eventMap.put("type", "SOUND");
            eventMap.put("sound_id", currentSoundId);
            eventMap.put("category", currentCategory);
            eventMap.put("volume", currentVolume);
            eventMap.put("pitch", currentPitch);
            if (currentMaxDurationTicks > 0) {
                eventMap.put("max_duration_ticks", currentMaxDurationTicks);
            }
        } else {
            eventMap.put("at", 0);
            eventMap.put("type", "STOP_SOUND");
            eventMap.put("source", currentSource);
        }
        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[SoundEdit] Saved cue override for " + cueId + " → mode=" + mode);
    }

    /**
     * If auto-preview is ON, refire the current SOUND event so the designer
     * hears the param change in real time. No-op if auto-preview is OFF.
     */
    private void autoPreviewSound() {
        if (cueSession.isAutoPreview()) {
            firePreviewSound();
        }
    }

    /**
     * Fire the current SOUND event directly at the player's location.
     * Bypasses RunningShow and the scheduler — preview-only, no side-effects.
     */
    private void firePreviewSound() {
        SoundCategory cat;
        try {
            cat = SoundCategory.valueOf(currentCategory.toUpperCase());
        } catch (IllegalArgumentException e) {
            cat = SoundCategory.MASTER;
        }
        player.playSound(player.getLocation(), currentSoundId, cat, currentVolume, currentPitch);
    }

    /** Find the first SOUND or STOP_SOUND event in the named cue from the registry. */
    private Object findSoundEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof SoundEvent || event instanceof StopSoundEvent) return event;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Pitch register helpers (package-accessible for SoundPanelBuilder)
    // -----------------------------------------------------------------------

    /**
     * Key for preset naming: low / mid_low / natural / mid_high / high.
     * Ranges match kb/system/phase2-department-panels.md §Sound pitch register table.
     */
    static String pitchRegisterKey(float pitch) {
        if (pitch < 0.7f)  return "low";
        if (pitch < 0.9f)  return "mid_low";
        if (pitch <= 1.1f) return "natural";
        if (pitch <= 1.5f) return "mid_high";
        return "high";
    }

    /**
     * Display label (hyphenated): low / mid-low / natural / mid-high / high.
     */
    static String pitchRegisterLabel(float pitch) {
        if (pitch < 0.7f)  return "low";
        if (pitch < 0.9f)  return "mid-low";
        if (pitch <= 1.1f) return "natural";
        if (pitch <= 1.5f) return "mid-high";
        return "high";
    }

    // -----------------------------------------------------------------------
    // Numeric helpers
    // -----------------------------------------------------------------------

    private static float clampVolume(float v) { return Math.max(0.0f, Math.min(2.0f, v)); }
    private static float clampPitch(float v)  { return Math.max(0.5f, Math.min(2.0f, v)); }

    /** Round a float to {@code places} decimal places. */
    private static float roundFloat(float v, int places) {
        float scale = (float) Math.pow(10, places);
        return Math.round(v * scale) / scale;
    }

    private void sendError(String msg) {
        player.sendMessage(
            MiniMessage.miniMessage().deserialize("<red>" + msg + "</red>"));
    }
}
