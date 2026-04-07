package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.VisualEvents.LightningEvent;
import com.scaena.shows.model.event.WorldEvents.TimeOfDayEvent;
import com.scaena.shows.model.event.WorldEvents.WeatherEvent;
import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Lighting.
 *
 * Handles four event types found in lighting.* cues:
 *
 *   TIME_OF_DAY         — scroll wheel ±1000t / Shift ±100t; live world preview; snap offer
 *   TIME_OF_DAY_PATTERN — multi-field Pattern edit; explicit [▶ Preview] only
 *   WEATHER             — state selector; optional duration; live world preview
 *   LIGHTNING           — anchor + offset; auto-preview (test strike per notch)
 *
 * Server-wide constraint: TIME_OF_DAY and WEATHER affect all players. The world_preview
 * param (TechCueSession.WorldPreviewMode) is the mechanism for managing this.
 *
 * TIME_OF_DAY_PATTERN is not yet in EventType — detected via rawYaml override type.
 *
 * Preset naming:
 *   TIME_OF_DAY:         No preset (single field, too trivial).
 *   TIME_OF_DAY_PATTERN: lighting.[direction].[slug]
 *     direction inferred from start/end: dawn, dusk, midnight, noon
 *   WEATHER:             No preset.
 *   LIGHTNING:           lighting.lightning.[anchor_type].[slug]
 *
 * OPS-034 dependency: anchor: player in LIGHTNING is editable/saveable, but fires
 * relative to scene_origin in production until OPS-034 is resolved.
 *
 * Spec: kb/system/phase2-department-panels.md §Lighting
 */
public final class LightingEditSession implements DeptEditSession {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // -----------------------------------------------------------------------
    // Named sky states — used for arc labels and snap offers
    // Ordered by ascending tick value; modular distance handles 23000→0 wrap.
    // -----------------------------------------------------------------------

    /** Tick values for named sky states in ascending order. */
    static final long[] SKY_STATE_TICKS = {0L, 6000L, 9000L, 13000L, 18000L, 23000L};

    /** Human-readable names parallel to SKY_STATE_TICKS. */
    static final String[] SKY_STATE_NAMES = {"Morning", "Noon", "Afternoon", "Dusk", "Night", "Dawn"};

    /** Within this many ticks of a named state, offer a snap. */
    private static final int SNAP_THRESHOLD = 200;

    // -----------------------------------------------------------------------
    // Curated option lists (package-accessible for LightingPanelBuilder)
    // -----------------------------------------------------------------------

    static final List<String> ANCHORS       = List.of("scene_origin", "player");
    static final List<String> WEATHER_STATES= List.of("clear", "storm", "thunder");
    static final List<String> CURVES        = List.of("linear", "ease_in", "ease_out");

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode { TIME_OF_DAY, TIME_OF_DAY_PATTERN, WEATHER, LIGHTNING }

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
    // Mode — mutable to allow type-switching on empty/stub cues
    // -----------------------------------------------------------------------

    private EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel) — ALL final; assigned in every constructor
    // branch to satisfy Java's definite-assignment rules.
    // -----------------------------------------------------------------------

    // TIME_OF_DAY
    private final long   entryTime;

    // TIME_OF_DAY_PATTERN
    private final long   entryStartValue;
    private final long   entryEndValue;
    private final int    entrySteps;
    private final int    entryTotalDuration;
    private final String entryCurve;

    // WEATHER
    private final String entryWeatherState;
    private final int    entryWeatherDuration; // -1 = persistent (no duration_ticks field)

    // LIGHTNING
    private final String entryAnchor;
    private final double entryOffX;
    private final double entryOffY;
    private final double entryOffZ;

    // -----------------------------------------------------------------------
    // Current mutable state
    // -----------------------------------------------------------------------

    private long   currentTime;
    private long   currentStartValue;
    private long   currentEndValue;
    private int    currentSteps;
    private int    currentTotalDuration;
    private String currentCurve;
    private String currentWeatherState;
    private int    currentWeatherDuration;
    private String currentAnchor;
    private double currentOffX;
    private double currentOffY;
    private double currentOffZ;

    // -----------------------------------------------------------------------
    // Pattern preview task (TIME_OF_DAY_PATTERN only)
    // -----------------------------------------------------------------------

    private BukkitTask patternPreviewTask;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public LightingEditSession(
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

        ShowEvent found        = findLightingEvent(cueId);
        String    overrideType = editor.getCueOverrideFirstEventType(cueId);

        // -------------------------------------------------------------------
        // Detect mode; assign ALL final fields in every branch.
        // -------------------------------------------------------------------

        if (found instanceof TimeOfDayEvent te) {
            mode             = EventMode.TIME_OF_DAY;
            entryTime        = te.time;
            entryStartValue  = 0L;    entryEndValue  = 13000L;
            entrySteps       = 6;     entryTotalDuration = 120;
            entryCurve       = "linear";
            entryWeatherState = "clear"; entryWeatherDuration = -1;
            entryAnchor      = "scene_origin";
            entryOffX = 0;   entryOffY = 0;  entryOffZ = 0;

        } else if ("TIME_OF_DAY_PATTERN".equals(overrideType)) {
            // TIME_OF_DAY_PATTERN is not in EventType — detected via rawYaml override
            mode = EventMode.TIME_OF_DAY_PATTERN;
            Map<String, Object> overMap = getOverrideEventMap(cueId);
            entryTime        = 6000L;
            entryStartValue  = longFromMap(overMap, "start_value", 0L);
            entryEndValue    = longFromMap(overMap, "end_value", 13000L);
            entrySteps       = intFromMap(overMap, "steps", 6);
            entryTotalDuration = intFromMap(overMap, "total_duration", 120);
            entryCurve       = strFromMap(overMap, "curve", "linear");
            entryWeatherState = "clear"; entryWeatherDuration = -1;
            entryAnchor      = "scene_origin";
            entryOffX = 0;   entryOffY = 0;  entryOffZ = 0;

        } else if (found instanceof WeatherEvent we) {
            mode             = EventMode.WEATHER;
            entryTime        = 6000L;
            entryStartValue  = 0L;    entryEndValue  = 13000L;
            entrySteps       = 6;     entryTotalDuration = 120;
            entryCurve       = "linear";
            entryWeatherState = we.state; entryWeatherDuration = we.durationTicks;
            entryAnchor      = "scene_origin";
            entryOffX = 0;   entryOffY = 0;  entryOffZ = 0;

        } else if (found instanceof LightningEvent le) {
            mode             = EventMode.LIGHTNING;
            entryTime        = 6000L;
            entryStartValue  = 0L;    entryEndValue  = 13000L;
            entrySteps       = 6;     entryTotalDuration = 120;
            entryCurve       = "linear";
            entryWeatherState = "clear"; entryWeatherDuration = -1;
            entryAnchor      = "scene_origin"; // LightningEvent has no anchor field yet
            entryOffX = le.offsetX; entryOffY = le.offsetY; entryOffZ = le.offsetZ;

        } else {
            // Default for empty/stub cues — start in TIME_OF_DAY
            mode             = EventMode.TIME_OF_DAY;
            entryTime        = 6000L;
            entryStartValue  = 0L;    entryEndValue  = 13000L;
            entrySteps       = 6;     entryTotalDuration = 120;
            entryCurve       = "linear";
            entryWeatherState = "clear"; entryWeatherDuration = -1;
            entryAnchor      = "scene_origin";
            entryOffX = 0;   entryOffY = 0;  entryOffZ = 0;
        }

        // ---- Initialise current state from entry snapshots ----
        currentTime            = entryTime;
        currentStartValue      = entryStartValue;
        currentEndValue        = entryEndValue;
        currentSteps           = entrySteps;
        currentTotalDuration   = entryTotalDuration;
        currentCurve           = entryCurve;
        currentWeatherState    = entryWeatherState;
        currentWeatherDuration = entryWeatherDuration;
        currentAnchor          = entryAnchor;
        currentOffX            = entryOffX;
        currentOffY            = entryOffY;
        currentOffZ            = entryOffZ;
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "lighting"; }

    @Override
    public void onSave() {
        cancelPatternPreview();
        writeToEditor();
    }

    @Override
    public void onSaveAsPreset() {
        cancelPatternPreview();
        writeToEditor();
        String slug = cueId.contains(".")
            ? cueId.substring(cueId.lastIndexOf('.') + 1)
            : cueId;
        switch (mode) {
            case TIME_OF_DAY_PATTERN -> {
                String dir = inferDirection(currentStartValue, currentEndValue);
                editor.saveAsPreset(cueId, "lighting." + dir + "." + slug);
            }
            case LIGHTNING -> {
                String anchorKey = currentAnchor.equals("player") ? "player" : "scene";
                editor.saveAsPreset(cueId, "lighting.lightning." + anchorKey + "." + slug);
            }
            default -> { /* TIME_OF_DAY, WEATHER: no preset per spec */ }
        }
    }

    @Override
    public void onCancel() {
        cancelPatternPreview();
        currentTime            = entryTime;
        currentStartValue      = entryStartValue;
        currentEndValue        = entryEndValue;
        currentSteps           = entrySteps;
        currentTotalDuration   = entryTotalDuration;
        currentCurve           = entryCurve;
        currentWeatherState    = entryWeatherState;
        currentWeatherDuration = entryWeatherDuration;
        currentAnchor          = entryAnchor;
        currentOffX            = entryOffX;
        currentOffY            = entryOffY;
        currentOffZ            = entryOffZ;
    }

    /**
     * Handle a /scaena tech2 editparam <key> [value...] command.
     *
     * === TIME_OF_DAY keys ===
     *   time_up                — +1000 ticks
     *   time_down              — -1000 ticks
     *   time_shift_up          — +100 ticks (Shift+scroll fine-tune)
     *   time_shift_down        — -100 ticks
     *   time_snap <value>      — snap to a specific tick value
     *   world_preview_toggle   — toggle LIVE ↔ VALUES_ONLY
     *   world_preview_apply    — apply current value to world (VALUES_ONLY)
     *   world_preview_remember — persist world_preview setting to plugin config
     *
     * === TIME_OF_DAY_PATTERN keys ===
     *   start_up / start_down / start_shift_up / start_shift_down
     *   end_up / end_down / end_shift_up / end_shift_down
     *   steps_up / steps_down  — ±1 step (min 2)
     *   dur_up / dur_down      — ±20t total_duration
     *   dur_shift_up / dur_shift_down — ±100t
     *   curve <value>          — set curve: linear | ease_in | ease_out
     *   preview_pattern        — fire full pattern preview
     *   world_preview_toggle, world_preview_remember
     *
     * === WEATHER keys ===
     *   state <clear|storm|thunder>
     *   dur_up / dur_down      — ±20t duration_ticks
     *   dur_shift_up / dur_shift_down — ±100t
     *   dur_clear              — remove duration_ticks (persistent weather)
     *   world_preview_toggle, world_preview_apply, world_preview_remember
     *
     * === LIGHTNING keys ===
     *   anchor <scene_origin|player>
     *   off_x_up / off_x_down / off_x_shift_up / off_x_shift_down  — ±1 / ±5
     *   off_y_up / off_y_down / off_y_shift_up / off_y_shift_down
     *   off_z_up / off_z_down / off_z_shift_up / off_z_shift_down
     *   preview_strike         — fire test strike at current offset
     *   auto_preview_toggle    — toggle session auto-preview flag
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        // ---- Shared world_preview keys (TIME_OF_DAY, TIME_OF_DAY_PATTERN, WEATHER) ----
        if (mode != EventMode.LIGHTNING) {
            switch (lower) {
                case "world_preview_toggle" -> {
                    TechCueSession.WorldPreviewMode next =
                        (cueSession.getWorldPreview() == TechCueSession.WorldPreviewMode.LIVE)
                        ? TechCueSession.WorldPreviewMode.VALUES_ONLY
                        : TechCueSession.WorldPreviewMode.LIVE;
                    cueSession.setWorldPreview(next);
                    LightingPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "world_preview_remember" -> {
                    persistWorldPreviewDefault(cueSession.getWorldPreview());
                    player.sendMessage(MM.deserialize(
                        "<green>[Lighting] World preview default saved: <white>"
                        + cueSession.getWorldPreview().name() + "</white></green>"));
                    return true;
                }
                case "world_preview_apply" -> {
                    // Apply current value to world regardless of mode
                    if (mode == EventMode.TIME_OF_DAY) applyWorldTime();
                    else if (mode == EventMode.WEATHER) applyWorldWeather();
                    return true;
                }
            }
        }

        // ---- TIME_OF_DAY keys ----
        if (mode == EventMode.TIME_OF_DAY) {
            switch (lower) {
                case "time_up"         -> { currentTime = clampTime(currentTime + 1000); onTimeChanged(); return true; }
                case "time_down"       -> { currentTime = clampTime(currentTime - 1000); onTimeChanged(); return true; }
                case "time_shift_up"   -> { currentTime = clampTime(currentTime + 100);  onTimeChanged(); return true; }
                case "time_shift_down" -> { currentTime = clampTime(currentTime - 100);  onTimeChanged(); return true; }
                case "time_snap"       -> {
                    try {
                        long snap = Long.parseLong(value.trim());
                        currentTime = clampTime(snap);
                        onTimeChanged();
                    } catch (NumberFormatException e) {
                        sendError("Invalid time value: " + value);
                    }
                    return true;
                }
                default -> { return false; }
            }
        }

        // ---- TIME_OF_DAY_PATTERN keys ----
        if (mode == EventMode.TIME_OF_DAY_PATTERN) {
            switch (lower) {
                case "start_up"         -> { currentStartValue = clampTime(currentStartValue + 1000); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "start_down"       -> { currentStartValue = clampTime(currentStartValue - 1000); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "start_shift_up"   -> { currentStartValue = clampTime(currentStartValue + 100);  LightingPanelBuilder.sendPanel(player, this); return true; }
                case "start_shift_down" -> { currentStartValue = clampTime(currentStartValue - 100);  LightingPanelBuilder.sendPanel(player, this); return true; }
                case "start_snap"       -> {
                    try { currentStartValue = clampTime(Long.parseLong(value.trim())); LightingPanelBuilder.sendPanel(player, this); }
                    catch (NumberFormatException e) { sendError("Invalid value: " + value); }
                    return true;
                }
                case "end_up"           -> { currentEndValue = clampTime(currentEndValue + 1000); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "end_down"         -> { currentEndValue = clampTime(currentEndValue - 1000); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "end_shift_up"     -> { currentEndValue = clampTime(currentEndValue + 100);  LightingPanelBuilder.sendPanel(player, this); return true; }
                case "end_shift_down"   -> { currentEndValue = clampTime(currentEndValue - 100);  LightingPanelBuilder.sendPanel(player, this); return true; }
                case "end_snap"         -> {
                    try { currentEndValue = clampTime(Long.parseLong(value.trim())); LightingPanelBuilder.sendPanel(player, this); }
                    catch (NumberFormatException e) { sendError("Invalid value: " + value); }
                    return true;
                }
                case "steps_up"   -> { currentSteps = Math.min(currentSteps + 1, 200); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "steps_down" -> { currentSteps = Math.max(currentSteps - 1, 2);   LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_up"          -> { currentTotalDuration = Math.min(currentTotalDuration + 20, 24000);  LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_down"        -> { currentTotalDuration = Math.max(currentTotalDuration - 20, 20);     LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_shift_up"    -> { currentTotalDuration = Math.min(currentTotalDuration + 100, 24000); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_shift_down"  -> { currentTotalDuration = Math.max(currentTotalDuration - 100, 20);   LightingPanelBuilder.sendPanel(player, this); return true; }
                case "curve" -> {
                    if (!CURVES.contains(value.toLowerCase())) {
                        sendError("Unknown curve: " + value + ". Valid: " + String.join(", ", CURVES));
                        return true;
                    }
                    currentCurve = value.toLowerCase();
                    LightingPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_pattern" -> {
                    firePreviewPattern();
                    return true;
                }
                default -> { return false; }
            }
        }

        // ---- WEATHER keys ----
        if (mode == EventMode.WEATHER) {
            switch (lower) {
                case "state" -> {
                    if (!WEATHER_STATES.contains(value.toLowerCase())) {
                        sendError("Unknown state: " + value + ". Valid: clear, storm, thunder");
                        return true;
                    }
                    currentWeatherState = value.toLowerCase();
                    if (cueSession.getWorldPreview() == TechCueSession.WorldPreviewMode.LIVE) applyWorldWeather();
                    LightingPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "dur_up"       -> { currentWeatherDuration = (currentWeatherDuration < 0 ? 20 : Math.min(currentWeatherDuration + 20, 24000)); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_down"     -> {
                    if (currentWeatherDuration > 20) currentWeatherDuration -= 20;
                    else if (currentWeatherDuration > 0) currentWeatherDuration = -1;
                    LightingPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "dur_shift_up"   -> { currentWeatherDuration = (currentWeatherDuration < 0 ? 100 : Math.min(currentWeatherDuration + 100, 24000)); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_shift_down" -> { currentWeatherDuration = (currentWeatherDuration <= 100 ? -1 : currentWeatherDuration - 100); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "dur_clear"    -> { currentWeatherDuration = -1; LightingPanelBuilder.sendPanel(player, this); return true; }
                default -> { return false; }
            }
        }

        // ---- LIGHTNING keys ----
        if (mode == EventMode.LIGHTNING) {
            switch (lower) {
                case "anchor" -> {
                    if (!ANCHORS.contains(value.toLowerCase())) {
                        sendError("Unknown anchor: " + value + ". Valid: scene_origin, player");
                        return true;
                    }
                    currentAnchor = value.toLowerCase();
                    LightingPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "off_x_up"         -> { currentOffX += 1; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_x_down"       -> { currentOffX -= 1; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_x_shift_up"   -> { currentOffX += 5; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_x_shift_down" -> { currentOffX -= 5; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_up"         -> { currentOffY += 1; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_down"       -> { currentOffY -= 1; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_shift_up"   -> { currentOffY += 5; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_shift_down" -> { currentOffY -= 5; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_up"         -> { currentOffZ += 1; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_down"       -> { currentOffZ -= 1; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_shift_up"   -> { currentOffZ += 5; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_shift_down" -> { currentOffZ -= 5; autoPreviewStrike(); LightingPanelBuilder.sendPanel(player, this); return true; }
                case "preview_strike"   -> { firePreviewStrike(); return true; }
                case "auto_preview_toggle" -> {
                    cueSession.setAutoPreview(!cueSession.isAutoPreview());
                    LightingPanelBuilder.sendPanel(player, this);
                    return true;
                }
                default -> { return false; }
            }
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Accessors (for LightingPanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode()                { return mode; }
    public long   getCurrentTime()            { return currentTime; }
    public long   getCurrentStartValue()      { return currentStartValue; }
    public long   getCurrentEndValue()        { return currentEndValue; }
    public int    getCurrentSteps()           { return currentSteps; }
    public int    getCurrentTotalDuration()   { return currentTotalDuration; }
    public String getCurrentCurve()           { return currentCurve; }
    public String getCurrentWeatherState()    { return currentWeatherState; }
    public int    getCurrentWeatherDuration() { return currentWeatherDuration; }
    public String getCurrentAnchor()          { return currentAnchor; }
    public double getCurrentOffX()            { return currentOffX; }
    public double getCurrentOffY()            { return currentOffY; }
    public double getCurrentOffZ()            { return currentOffZ; }
    public boolean isAutoPreview()            { return cueSession.isAutoPreview(); }
    public TechCueSession.WorldPreviewMode getWorldPreview() { return cueSession.getWorldPreview(); }

    // -----------------------------------------------------------------------
    // Internal helpers — time change handling
    // -----------------------------------------------------------------------

    /**
     * Called after any TIME_OF_DAY change. If LIVE mode, applies to world.
     * Re-sends panel (which includes snap offer when applicable).
     */
    private void onTimeChanged() {
        if (cueSession.getWorldPreview() == TechCueSession.WorldPreviewMode.LIVE) {
            applyWorldTime();
        }
        LightingPanelBuilder.sendPanel(player, this);
    }

    /** Apply current time to the player's world. */
    private void applyWorldTime() {
        World world = player.getWorld();
        world.setTime(currentTime);
    }

    /** Apply current weather state to the player's world. */
    void applyWorldWeather() {
        World world = player.getWorld();
        switch (currentWeatherState.toLowerCase()) {
            case "clear" -> {
                world.setStorm(false);
                world.setThundering(false);
                if (currentWeatherDuration > 0) world.setWeatherDuration(currentWeatherDuration);
            }
            case "storm" -> {
                world.setStorm(true);
                world.setThundering(false);
                if (currentWeatherDuration > 0) world.setWeatherDuration(currentWeatherDuration);
            }
            case "thunder" -> {
                world.setStorm(true);
                world.setThundering(true);
                if (currentWeatherDuration > 0) world.setThunderDuration(currentWeatherDuration);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Preview helpers — LIGHTNING
    // -----------------------------------------------------------------------

    /** Fire preview if auto-preview is ON. */
    private void autoPreviewStrike() {
        if (cueSession.isAutoPreview()) firePreviewStrike();
    }

    /**
     * Fire a cosmetic lightning strike at player's location + current offset.
     * Uses player position as origin in preview mode (no scene_origin mark here).
     */
    void firePreviewStrike() {
        org.bukkit.Location origin = player.getLocation().clone();
        origin.add(currentOffX, currentOffY, currentOffZ);
        player.getWorld().strikeLightningEffect(origin);
    }

    // -----------------------------------------------------------------------
    // Preview helpers — TIME_OF_DAY_PATTERN
    // -----------------------------------------------------------------------

    private void cancelPatternPreview() {
        if (patternPreviewTask != null) {
            patternPreviewTask.cancel();
            patternPreviewTask = null;
        }
    }

    /**
     * Fire the full TIME_OF_DAY_PATTERN expansion in real time.
     * Interpolates world time from startValue → endValue over totalDuration ticks
     * using currentSteps steps distributed at equal intervals.
     *
     * Steve watches the sky move and judges whether it reads as atmospheric.
     */
    void firePreviewPattern() {
        cancelPatternPreview();

        org.bukkit.plugin.Plugin pluginRef = Bukkit.getPluginManager().getPlugin("ScaenaShows");
        if (!(pluginRef instanceof JavaPlugin jp)) {
            log.warning("[LightingEdit] Cannot find ScaenaShows plugin for pattern preview");
            return;
        }

        final long   startV  = currentStartValue;
        final long   endV    = currentEndValue;
        final int    steps   = currentSteps;
        final int    dur     = currentTotalDuration;
        final String curve   = currentCurve;
        final World  world   = player.getWorld();

        // Compute how many game ticks between each step fire
        final int stepInterval = (steps > 1) ? Math.max(1, dur / (steps - 1)) : dur;

        // Build array of world times to set at each step
        final long[] times = new long[steps];
        for (int i = 0; i < steps; i++) {
            double t = (steps > 1) ? (double) i / (steps - 1) : 1.0;
            double curved = applyCurve(t, curve);
            times[i] = clampTime(Math.round(startV + (endV - startV) * curved));
        }

        patternPreviewTask = new BukkitRunnable() {
            int step = 0;
            @Override public void run() {
                if (!player.isOnline() || step >= steps) {
                    cancel();
                    patternPreviewTask = null;
                    return;
                }
                world.setTime(times[step]);
                step++;
            }
        }.runTaskTimer(jp, 0L, stepInterval);
    }

    /**
     * Apply a named curve function to parameter t in [0,1].
     * Returns a value in [0,1] representing the interpolated progress.
     */
    private static double applyCurve(double t, String curve) {
        return switch (curve) {
            case "ease_in"  -> t * t;
            case "ease_out" -> 1.0 - (1.0 - t) * (1.0 - t);
            default         -> t; // linear
        };
    }

    // -----------------------------------------------------------------------
    // Write to editor
    // -----------------------------------------------------------------------

    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        switch (mode) {
            case TIME_OF_DAY -> {
                eventMap.put("at", 0);
                eventMap.put("type", "TIME_OF_DAY");
                eventMap.put("time", currentTime);
            }
            case TIME_OF_DAY_PATTERN -> {
                // Stored flat in the Phase 2 override. PatternExpander will handle
                // this format when TIME_OF_DAY_PATTERN runtime support is added.
                eventMap.put("at", 0);
                eventMap.put("type", "TIME_OF_DAY_PATTERN");
                eventMap.put("start_value", currentStartValue);
                eventMap.put("end_value", currentEndValue);
                eventMap.put("steps", currentSteps);
                eventMap.put("total_duration", currentTotalDuration);
                eventMap.put("curve", currentCurve);
            }
            case WEATHER -> {
                eventMap.put("at", 0);
                eventMap.put("type", "WEATHER");
                eventMap.put("state", currentWeatherState);
                if (currentWeatherDuration > 0) {
                    eventMap.put("duration_ticks", currentWeatherDuration);
                }
            }
            case LIGHTNING -> {
                eventMap.put("at", 0);
                eventMap.put("type", "LIGHTNING");
                eventMap.put("anchor", currentAnchor);
                Map<String, Object> off = new LinkedHashMap<>();
                off.put("x", currentOffX);
                off.put("y", currentOffY);
                off.put("z", currentOffZ);
                eventMap.put("offset", off);
            }
        }
        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[LightingEdit] Saved cue override for " + cueId + " → mode=" + mode);
    }

    // -----------------------------------------------------------------------
    // Sky state helpers (package-accessible for LightingPanelBuilder)
    // -----------------------------------------------------------------------

    /**
     * Returns the index of the named sky state nearest to {@code time} using
     * modular arithmetic on the 24000-tick day cycle.
     */
    static int nearestSkyStateIndex(long time) {
        int best = 0;
        long bestDist = Long.MAX_VALUE;
        for (int i = 0; i < SKY_STATE_TICKS.length; i++) {
            long d = modDist(time, SKY_STATE_TICKS[i]);
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return best;
    }

    /**
     * Returns a display string for the given time value, e.g.:
     *   "13200 — near Dusk (13000)"
     *   "13000 — Dusk"
     */
    static String arcLabel(long time) {
        int idx = nearestSkyStateIndex(time);
        long dist = modDist(time, SKY_STATE_TICKS[idx]);
        if (dist == 0) {
            return time + " — " + SKY_STATE_NAMES[idx];
        }
        return time + " — near " + SKY_STATE_NAMES[idx] + " (" + SKY_STATE_TICKS[idx] + ")";
    }

    /**
     * Returns the index of a named sky state within SNAP_THRESHOLD of {@code time},
     * or -1 if none is close enough.
     */
    static int snapCandidateIndex(long time) {
        int idx = nearestSkyStateIndex(time);
        long dist = modDist(time, SKY_STATE_TICKS[idx]);
        if (dist > 0 && dist <= SNAP_THRESHOLD) return idx;
        return -1;
    }

    /** Modular distance between two tick values on the 24000-tick day cycle. */
    private static long modDist(long a, long b) {
        long diff = Math.abs(a - b) % 24000L;
        return Math.min(diff, 24000L - diff);
    }

    /** Wrap a time value into the valid [0, 23999] range. */
    static long clampTime(long t) {
        t = t % 24000L;
        if (t < 0) t += 24000L;
        return t;
    }

    // -----------------------------------------------------------------------
    // Preset direction inference
    // -----------------------------------------------------------------------

    /**
     * Infers a direction component for the TIME_OF_DAY_PATTERN preset name.
     *   dawn     — end is closer to Morning (0) than start
     *   midnight — end is in the Night range (17000–19000)
     *   dusk     — end is moving toward Night
     *   noon     — end is in the Noon range (5000–7000)
     */
    static String inferDirection(long startValue, long endValue) {
        long distToNoon     = modDist(endValue, 6000L);
        long distToNight    = modDist(endValue, 18000L);
        long distToMorning  = modDist(endValue, 0L);

        if (distToNight <= 1000) return "midnight";
        if (distToNoon  <= 1500) return "noon";
        if (distToMorning <= 1500) return "dawn";
        // Default: direction relative to start
        // Moving later in the day → dusk; earlier → dawn
        long startMod = startValue % 24000;
        long endMod   = endValue   % 24000;
        if (endMod > startMod || (startMod > 20000 && endMod < 4000)) return "dusk";
        return "dawn";
    }

    // -----------------------------------------------------------------------
    // Misc helpers
    // -----------------------------------------------------------------------

    /** Persist the world_preview default to plugin config. */
    private void persistWorldPreviewDefault(TechCueSession.WorldPreviewMode m) {
        org.bukkit.plugin.Plugin pluginRef = Bukkit.getPluginManager().getPlugin("ScaenaShows");
        if (pluginRef instanceof JavaPlugin jp) {
            jp.getConfig().set("tech2.world_preview_default", m.name());
            jp.saveConfig();
        }
    }

    private Map<String, Object> getOverrideEventMap(String id) {
        return editor.getCueOverrideFirstEvent(id);
    }

    /** Find the first lighting-relevant event in the cue (TIME_OF_DAY, WEATHER, LIGHTNING). */
    private ShowEvent findLightingEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof TimeOfDayEvent
                    || event instanceof WeatherEvent
                    || event instanceof LightningEvent) {
                return event;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Map reading helpers
    // -----------------------------------------------------------------------

    private static int intFromMap(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }

    private static long longFromMap(Map<String, Object> m, String key, long def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.longValue() : def;
    }

    private static String strFromMap(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return (v instanceof String s) ? s : def;
    }

    private void sendError(String msg) {
        player.sendMessage(MM.deserialize("<red>" + msg + "</red>"));
    }

    /** Format a tick duration as "Nt (Xs)". */
    static String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int rem = ticks % 20;
        String secStr = rem == 0 ? seconds + "s" : String.format("%.1fs", ticks / 20.0);
        return ticks + "t (" + secStr + ")";
    }

    /**
     * Format the step interval annotation for TIME_OF_DAY_PATTERN:
     * "every 27t (~1.4s)"
     */
    static String formatStepInterval(int totalDuration, int steps) {
        if (steps <= 1) return totalDuration + "t total";
        int interval = totalDuration / (steps - 1);
        double secs = interval / 20.0;
        // Warn if non-integer spacing
        boolean exact = (totalDuration % (steps - 1)) == 0;
        String label = exact
            ? "every " + interval + "t (~" + String.format("%.1fs", secs) + ")"
            : "every ~" + interval + "t (~" + String.format("%.1fs", secs) + ") \u26a0 non-integer";
        return label;
    }
}
