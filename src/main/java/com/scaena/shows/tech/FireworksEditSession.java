package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.FireworkEvents.FireworkCircleEvent;
import com.scaena.shows.model.event.FireworkEvents.FireworkEvent;
import com.scaena.shows.model.event.FireworkEvents.FireworkFanEvent;
import com.scaena.shows.model.event.FireworkEvents.FireworkLineEvent;
import com.scaena.shows.model.event.FireworkEvents.FireworkRandomEvent;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.PositionResolver;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Fireworks.
 *
 * Handles four event types found in fireworks.* cues:
 *
 *   FIREWORK         — single point launch; panel + auto-preview ON
 *   FIREWORK_CIRCLE  — circle pattern; subtype panel + auto-preview ON
 *   FIREWORK_LINE    — line pattern;   subtype panel + auto-preview ON
 *   FIREWORK_RANDOM  — scatter pattern; subtype panel + auto-preview ON
 *   FIREWORK_FAN     — deferred (Phase 3); shows unsupported message, falls through to [Cancel]
 *   FIREWORK_PHRASE  — deferred (step-list infra not built); shows unsupported message, falls through to [Cancel]
 *
 * Anchor model (all types):
 *   scene_origin (default) — offset computed from stage mark
 *   player                 — offset from player position at invocation (OPS-034 dep)
 *   Panel shows OPS-034 dependency warning when player anchor is selected.
 *
 * Auto-preview: fires a test burst at the designer's location using the event's
 * offset, count, and pattern geometry. Spawns generic white-star fireworks since
 * preset appearance lookup requires FireworkRegistry (not wired in this constructor).
 *
 * Known gap: LINE + GRADIENT colour variation is not applied in the executor —
 * always defaults to red→blue. Panel displays an inline warning.
 *
 * Preset naming when Save as Preset is called:
 *   FIREWORK:        fireworks.burst.[slug]
 *   FIREWORK_CIRCLE: fireworks.circle.[slug]
 *   FIREWORK_LINE:   fireworks.line.[slug]
 *   FIREWORK_RANDOM: fireworks.random.[slug]
 *
 * Spec: kb/system/phase2-department-panels.md §Fireworks
 */
public final class FireworksEditSession implements DeptEditSession {

    // -----------------------------------------------------------------------
    // Curated option lists (package-accessible for FireworksPanelBuilder)
    // -----------------------------------------------------------------------

    public static final List<String> ANCHORS = List.of("scene_origin", "player");
    public static final List<String> Y_MODES = List.of("relative", "surface");
    public static final List<String> POWER_VARIATIONS = List.of(
        "UNIFORM", "RAMP_UP", "RAMP_DOWN", "ALTERNATE", "RANDOM");
    public static final List<String> COLOR_VARIATIONS = List.of(
        "UNIFORM", "RAINBOW", "GRADIENT", "ALTERNATE");
    public static final List<String> CHASE_DIRS = List.of("FL", "LF");

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode {
        FIREWORK, CIRCLE, LINE, RANDOM, FAN_UNSUPPORTED, PHRASE_UNSUPPORTED
    }

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
    // Mode — mutable to allow CIRCLE/LINE/RANDOM subtype switching
    // -----------------------------------------------------------------------

    private EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel) — all final, assigned in every branch.
    // Java's definite-assignment rules require every final field to be set
    // exactly once across all constructor branches.
    // -----------------------------------------------------------------------

    // FIREWORK (single)
    private final String entryFwAnchor;
    private final String entryFwPreset;
    private final double entryFwOffX;
    private final double entryFwOffY;
    private final double entryFwOffZ;
    private final String entryFwYMode;

    // Pattern common (CIRCLE / LINE / RANDOM)
    private final String entryPatAnchor;
    private final String entryPatPreset;
    private final String entryPatYMode;
    private final double entryPatYOffset;
    private final String entryPatPowerVar;
    private final String entryPatColorVar;
    private final String entryPatGradFrom;
    private final String entryPatGradTo;

    // Chase (CIRCLE + LINE)
    private final boolean entryChaseEnabled;
    private final String  entryChaseDir;
    private final int     entryChaseInterval;

    // CIRCLE-specific
    private final double entryCircleRadius;
    private final int    entryCircleCount;
    private final double entryCircleOriginX;
    private final double entryCircleOriginZ;

    // LINE-specific
    private final double entryLineLength;
    private final double entryLineAngle;
    private final int    entryLineCount;
    private final double entryLineStartX;
    private final double entryLineStartZ;

    // RANDOM-specific
    private final double entryRandomRadius;
    private final int    entryRandomCount;
    private final double entryRandomOriginX;
    private final double entryRandomOriginZ;
    private final Long   entryRandomSeed;

    // -----------------------------------------------------------------------
    // Current mutable state
    // -----------------------------------------------------------------------

    private String currentFwAnchor;
    private String currentFwPreset;
    private double currentFwOffX;
    private double currentFwOffY;
    private double currentFwOffZ;
    private String currentFwYMode;

    private String currentPatAnchor;
    private String currentPatPreset;
    private String currentPatYMode;
    private double currentPatYOffset;
    private String currentPatPowerVar;
    private String currentPatColorVar;
    private String currentPatGradFrom;
    private String currentPatGradTo;

    private boolean currentChaseEnabled;
    private String  currentChaseDir;
    private int     currentChaseInterval;

    private double currentCircleRadius;
    private int    currentCircleCount;
    private double currentCircleOriginX;
    private double currentCircleOriginZ;

    private double currentLineLength;
    private double currentLineAngle;
    private int    currentLineCount;
    private double currentLineStartX;
    private double currentLineStartZ;

    private double currentRandomRadius;
    private int    currentRandomCount;
    private double currentRandomOriginX;
    private double currentRandomOriginZ;
    private Long   currentRandomSeed;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public FireworksEditSession(
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

        ShowEvent found = findFireworkEvent(cueId);

        // -------------------------------------------------------------------
        // Detect mode; assign ALL final fields in every branch.
        // Each branch seeds its active mode from the found event and fills
        // every other mode's fields with sensible defaults.
        // -------------------------------------------------------------------
        if (found instanceof FireworkEvent fe) {
            mode = EventMode.FIREWORK;
            entryFwAnchor = fe.anchor;             entryFwPreset = fe.preset;
            entryFwOffX = fe.offsetX;              entryFwOffY = fe.offsetY;
            entryFwOffZ = fe.offsetZ;              entryFwYMode = fe.yMode;
            entryPatAnchor = "scene_origin";       entryPatPreset = "";
            entryPatYMode = "surface";             entryPatYOffset = 2.0;
            entryPatPowerVar = "UNIFORM";          entryPatColorVar = "UNIFORM";
            entryPatGradFrom = "#FF0000";          entryPatGradTo = "#0000FF";
            entryChaseEnabled = false;             entryChaseDir = "FL";
            entryChaseInterval = 4;
            entryCircleRadius = 8.0;               entryCircleCount = 8;
            entryCircleOriginX = 0;                entryCircleOriginZ = 0;
            entryLineLength = 10.0;                entryLineAngle = 0.0;
            entryLineCount = 6;                    entryLineStartX = 0; entryLineStartZ = 0;
            entryRandomRadius = 10.0;              entryRandomCount = 6;
            entryRandomOriginX = 0;                entryRandomOriginZ = 0;
            entryRandomSeed = null;

        } else if (found instanceof FireworkCircleEvent ce) {
            mode = EventMode.CIRCLE;
            entryFwAnchor = "scene_origin";        entryFwPreset = "";
            entryFwOffX = 0;                       entryFwOffY = 2.0; entryFwOffZ = 0;
            entryFwYMode = "relative";
            entryPatAnchor = ce.anchor;            entryPatPreset = ce.preset;
            entryPatYMode = ce.yMode;              entryPatYOffset = ce.yOffset;
            entryPatPowerVar = ce.powerVariation;  entryPatColorVar = ce.colorVariation;
            entryPatGradFrom = ce.gradientFrom;    entryPatGradTo = ce.gradientTo;
            entryChaseEnabled = ce.chase.enabled();entryChaseDir = ce.chase.direction();
            entryChaseInterval = ce.chase.intervalTicks();
            entryCircleRadius = ce.radius;         entryCircleCount = ce.count;
            entryCircleOriginX = ce.originOffset.x(); entryCircleOriginZ = ce.originOffset.z();
            entryLineLength = 10.0;                entryLineAngle = 0.0;
            entryLineCount = 6;                    entryLineStartX = 0; entryLineStartZ = 0;
            entryRandomRadius = 10.0;              entryRandomCount = 6;
            entryRandomOriginX = 0;                entryRandomOriginZ = 0;
            entryRandomSeed = null;

        } else if (found instanceof FireworkLineEvent le) {
            mode = EventMode.LINE;
            entryFwAnchor = "scene_origin";        entryFwPreset = "";
            entryFwOffX = 0;                       entryFwOffY = 2.0; entryFwOffZ = 0;
            entryFwYMode = "relative";
            entryPatAnchor = le.anchor;            entryPatPreset = le.preset;
            entryPatYMode = le.yMode;              entryPatYOffset = le.yOffset;
            entryPatPowerVar = le.powerVariation;  entryPatColorVar = le.colorVariation;
            entryPatGradFrom = le.gradientFrom;    entryPatGradTo = le.gradientTo;
            entryChaseEnabled = le.chase.enabled();entryChaseDir = le.chase.direction();
            entryChaseInterval = le.chase.intervalTicks();
            entryCircleRadius = 8.0;               entryCircleCount = 8;
            entryCircleOriginX = 0;                entryCircleOriginZ = 0;
            entryLineLength = le.length;           entryLineAngle = le.angle;
            entryLineCount = le.count;             entryLineStartX = le.startOffset.x();
            entryLineStartZ = le.startOffset.z();
            entryRandomRadius = 10.0;              entryRandomCount = 6;
            entryRandomOriginX = 0;                entryRandomOriginZ = 0;
            entryRandomSeed = null;

        } else if (found instanceof FireworkRandomEvent re) {
            mode = EventMode.RANDOM;
            entryFwAnchor = "scene_origin";        entryFwPreset = "";
            entryFwOffX = 0;                       entryFwOffY = 2.0; entryFwOffZ = 0;
            entryFwYMode = "relative";
            entryPatAnchor = re.anchor;            entryPatPreset = re.preset;
            entryPatYMode = re.yMode;              entryPatYOffset = re.yOffset;
            entryPatPowerVar = re.powerVariation;  entryPatColorVar = re.colorVariation;
            entryPatGradFrom = re.gradientFrom;    entryPatGradTo = re.gradientTo;
            entryChaseEnabled = false;             entryChaseDir = "FL";
            entryChaseInterval = 4;
            entryCircleRadius = 8.0;               entryCircleCount = 8;
            entryCircleOriginX = 0;                entryCircleOriginZ = 0;
            entryLineLength = 10.0;                entryLineAngle = 0.0;
            entryLineCount = 6;                    entryLineStartX = 0; entryLineStartZ = 0;
            entryRandomRadius = re.radius;         entryRandomCount = re.count;
            entryRandomOriginX = re.originOffset.x(); entryRandomOriginZ = re.originOffset.z();
            entryRandomSeed = re.seed;

        } else if (found instanceof FireworkFanEvent) {
            mode = EventMode.FAN_UNSUPPORTED;
            entryFwAnchor = "scene_origin";        entryFwPreset = "";
            entryFwOffX = 0;                       entryFwOffY = 2.0; entryFwOffZ = 0;
            entryFwYMode = "relative";
            entryPatAnchor = "scene_origin";       entryPatPreset = "";
            entryPatYMode = "surface";             entryPatYOffset = 2.0;
            entryPatPowerVar = "UNIFORM";          entryPatColorVar = "UNIFORM";
            entryPatGradFrom = "#FF0000";          entryPatGradTo = "#0000FF";
            entryChaseEnabled = false;             entryChaseDir = "FL";
            entryChaseInterval = 4;
            entryCircleRadius = 8.0;               entryCircleCount = 8;
            entryCircleOriginX = 0;                entryCircleOriginZ = 0;
            entryLineLength = 10.0;                entryLineAngle = 0.0;
            entryLineCount = 6;                    entryLineStartX = 0; entryLineStartZ = 0;
            entryRandomRadius = 10.0;              entryRandomCount = 6;
            entryRandomOriginX = 0;                entryRandomOriginZ = 0;
            entryRandomSeed = null;

        } else {
            // Default / empty stub → CIRCLE mode.
            // Also detects PHRASE (FIREWORK_PHRASE deferred) via override type check.
            String overrideType = editor.getCueOverrideFirstEventType(cueId);
            if ("PHRASE".equals(overrideType) || "FIREWORK_PHRASE".equals(overrideType)) {
                mode = EventMode.PHRASE_UNSUPPORTED;
            } else {
                mode = EventMode.CIRCLE;
            }
            entryFwAnchor = "scene_origin";        entryFwPreset = "";
            entryFwOffX = 0;                       entryFwOffY = 2.0; entryFwOffZ = 0;
            entryFwYMode = "relative";
            entryPatAnchor = "scene_origin";       entryPatPreset = "";
            entryPatYMode = "surface";             entryPatYOffset = 2.0;
            entryPatPowerVar = "UNIFORM";          entryPatColorVar = "UNIFORM";
            entryPatGradFrom = "#FF0000";          entryPatGradTo = "#0000FF";
            entryChaseEnabled = false;             entryChaseDir = "FL";
            entryChaseInterval = 4;
            entryCircleRadius = 8.0;               entryCircleCount = 8;
            entryCircleOriginX = 0;                entryCircleOriginZ = 0;
            entryLineLength = 10.0;                entryLineAngle = 0.0;
            entryLineCount = 6;                    entryLineStartX = 0; entryLineStartZ = 0;
            entryRandomRadius = 10.0;              entryRandomCount = 6;
            entryRandomOriginX = 0;                entryRandomOriginZ = 0;
            entryRandomSeed = null;
        }

        // ---- Initialise current state from entry snapshots ----
        currentFwAnchor     = entryFwAnchor;      currentFwPreset    = entryFwPreset;
        currentFwOffX       = entryFwOffX;         currentFwOffY      = entryFwOffY;
        currentFwOffZ       = entryFwOffZ;         currentFwYMode     = entryFwYMode;
        currentPatAnchor    = entryPatAnchor;      currentPatPreset   = entryPatPreset;
        currentPatYMode     = entryPatYMode;       currentPatYOffset  = entryPatYOffset;
        currentPatPowerVar  = entryPatPowerVar;    currentPatColorVar = entryPatColorVar;
        currentPatGradFrom  = entryPatGradFrom;    currentPatGradTo   = entryPatGradTo;
        currentChaseEnabled = entryChaseEnabled;   currentChaseDir    = entryChaseDir;
        currentChaseInterval = entryChaseInterval;
        currentCircleRadius = entryCircleRadius;   currentCircleCount   = entryCircleCount;
        currentCircleOriginX = entryCircleOriginX; currentCircleOriginZ = entryCircleOriginZ;
        currentLineLength   = entryLineLength;     currentLineAngle   = entryLineAngle;
        currentLineCount    = entryLineCount;      currentLineStartX  = entryLineStartX;
        currentLineStartZ   = entryLineStartZ;
        currentRandomRadius = entryRandomRadius;   currentRandomCount   = entryRandomCount;
        currentRandomOriginX = entryRandomOriginX; currentRandomOriginZ = entryRandomOriginZ;
        currentRandomSeed   = entryRandomSeed;
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "fireworks"; }

    @Override
    public void onSave() {
        writeToEditor();
    }

    @Override
    public void onSaveAsPreset() {
        writeToEditor();
        String slug = cueId.contains(".")
            ? cueId.substring(cueId.lastIndexOf('.') + 1)
            : cueId;
        switch (mode) {
            case FIREWORK -> editor.saveAsPreset(cueId, "fireworks.burst."   + slug);
            case CIRCLE   -> editor.saveAsPreset(cueId, "fireworks.circle."  + slug);
            case LINE     -> editor.saveAsPreset(cueId, "fireworks.line."    + slug);
            case RANDOM   -> editor.saveAsPreset(cueId, "fireworks.random."  + slug);
            default -> { /* FAN / PHRASE: no preset */ }
        }
    }

    @Override
    public void onCancel() {
        currentFwAnchor     = entryFwAnchor;      currentFwPreset    = entryFwPreset;
        currentFwOffX       = entryFwOffX;         currentFwOffY      = entryFwOffY;
        currentFwOffZ       = entryFwOffZ;         currentFwYMode     = entryFwYMode;
        currentPatAnchor    = entryPatAnchor;      currentPatPreset   = entryPatPreset;
        currentPatYMode     = entryPatYMode;       currentPatYOffset  = entryPatYOffset;
        currentPatPowerVar  = entryPatPowerVar;    currentPatColorVar = entryPatColorVar;
        currentPatGradFrom  = entryPatGradFrom;    currentPatGradTo   = entryPatGradTo;
        currentChaseEnabled = entryChaseEnabled;   currentChaseDir    = entryChaseDir;
        currentChaseInterval = entryChaseInterval;
        currentCircleRadius = entryCircleRadius;   currentCircleCount   = entryCircleCount;
        currentCircleOriginX = entryCircleOriginX; currentCircleOriginZ = entryCircleOriginZ;
        currentLineLength   = entryLineLength;     currentLineAngle   = entryLineAngle;
        currentLineCount    = entryLineCount;      currentLineStartX  = entryLineStartX;
        currentLineStartZ   = entryLineStartZ;
        currentRandomRadius = entryRandomRadius;   currentRandomCount   = entryRandomCount;
        currentRandomOriginX = entryRandomOriginX; currentRandomOriginZ = entryRandomOriginZ;
        currentRandomSeed   = entryRandomSeed;
    }

    /**
     * Handle /scaena tech2 editparam <key> [value...] commands.
     *
     * === FIREWORK keys ===
     *   anchor <val>         — scene_origin | player
     *   preset <val>         — set preset ID
     *   off_x_up / down      — ±0.5 offsetX
     *   off_y_up / down      — ±0.5 offsetY
     *   off_z_up / down      — ±0.5 offsetZ
     *   ymode <val>          — relative | surface
     *   preview_firework     — fire test burst at designer's location
     *   auto_preview_toggle  — toggle auto-preview
     *
     * === PATTERN common keys (CIRCLE / LINE / RANDOM) ===
     *   anchor <val>         — scene_origin | player
     *   preset <val>         — set preset ID
     *   ymode <val>          — relative | surface
     *   yoffset_up / down    — ±0.5 yOffset
     *   power_var <val>      — UNIFORM | RAMP_UP | RAMP_DOWN | ALTERNATE | RANDOM
     *   color_var <val>      — UNIFORM | RAINBOW | GRADIENT | ALTERNATE
     *   grad_from <val>      — gradient_from hex (#RRGGBB)
     *   grad_to <val>        — gradient_to hex (#RRGGBB)
     *   subtype <val>        — CIRCLE | LINE | RANDOM (switch pattern subtype)
     *   preview_firework
     *   auto_preview_toggle
     *
     * === CIRCLE keys ===
     *   radius_up / down     — ±1.0 radius
     *   count_up / down      — ±1 count
     *   origin_x_up / down   — ±0.5 originOffset.x
     *   origin_z_up / down   — ±0.5 originOffset.z
     *   chase_toggle         — toggle chaseEnabled
     *   chase_dir <val>      — FL | LF
     *   chase_int_up / down  — ±1 intervalTicks
     *
     * === LINE keys ===
     *   length_up / down     — ±1.0 length
     *   angle_up / down      — ±15° compass bearing
     *   count_up / down      — ±1 count
     *   start_x_up / down    — ±0.5 startOffset.x
     *   start_z_up / down    — ±0.5 startOffset.z
     *   chase_toggle, chase_dir, chase_int_up/down — same as CIRCLE
     *
     * === RANDOM keys ===
     *   radius_up / down     — ±1.0 radius
     *   count_up / down      — ±1 count
     *   origin_x_up / down   — ±0.5 originOffset.x
     *   origin_z_up / down   — ±0.5 originOffset.z
     *   seed_randomize       — generate new random long seed
     *   seed_clear           — clear seed (null = different every run)
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        // ---- FIREWORK (single) params ----
        if (mode == EventMode.FIREWORK) {
            switch (lower) {
                case "anchor" -> {
                    if (!value.isBlank()) currentFwAnchor = value.trim();
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preset" -> {
                    if (!value.isBlank()) { currentFwPreset = value.trim(); autoPreview(); }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "off_x_up"   -> { currentFwOffX += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "off_x_down" -> { currentFwOffX -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_up"   -> { currentFwOffY += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_down" -> { currentFwOffY -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_up"   -> { currentFwOffZ += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_down" -> { currentFwOffZ -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "ymode" -> {
                    if (!value.isBlank()) { currentFwYMode = value.trim(); autoPreview(); }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_firework"    -> { firePreview(); return true; }
                case "auto_preview_toggle" -> {
                    cueSession.setAutoPreview(!cueSession.isAutoPreview());
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
            }
        }

        // ---- PATTERN common params (CIRCLE / LINE / RANDOM) ----
        if (mode == EventMode.CIRCLE || mode == EventMode.LINE || mode == EventMode.RANDOM) {
            switch (lower) {
                case "anchor" -> {
                    if (!value.isBlank()) currentPatAnchor = value.trim();
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preset" -> {
                    if (!value.isBlank()) { currentPatPreset = value.trim(); autoPreview(); }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "ymode" -> {
                    if (!value.isBlank()) { currentPatYMode = value.trim(); autoPreview(); }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "yoffset_up"   -> { currentPatYOffset += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "yoffset_down" -> { currentPatYOffset -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                case "power_var" -> {
                    if (!value.isBlank()) { currentPatPowerVar = value.trim().toUpperCase(); autoPreview(); }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "color_var" -> {
                    if (!value.isBlank()) { currentPatColorVar = value.trim().toUpperCase(); autoPreview(); }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "grad_from" -> {
                    if (!value.isBlank()) currentPatGradFrom = value.trim();
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "grad_to" -> {
                    if (!value.isBlank()) currentPatGradTo = value.trim();
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "subtype" -> {
                    switch (value.trim().toUpperCase()) {
                        case "CIRCLE" -> mode = EventMode.CIRCLE;
                        case "LINE"   -> mode = EventMode.LINE;
                        case "RANDOM" -> mode = EventMode.RANDOM;
                    }
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_firework"    -> { firePreview(); return true; }
                case "auto_preview_toggle" -> {
                    cueSession.setAutoPreview(!cueSession.isAutoPreview());
                    FireworksPanelBuilder.sendPanel(player, this);
                    return true;
                }
            }

            // Chase keys — CIRCLE and LINE only
            if (mode == EventMode.CIRCLE || mode == EventMode.LINE) {
                switch (lower) {
                    case "chase_toggle"   -> { currentChaseEnabled = !currentChaseEnabled; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "chase_dir"      -> { if (!value.isBlank()) currentChaseDir = value.trim().toUpperCase(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "chase_int_up"   -> { currentChaseInterval = Math.min(currentChaseInterval + 1, 40); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "chase_int_down" -> { currentChaseInterval = Math.max(currentChaseInterval - 1, 1);  FireworksPanelBuilder.sendPanel(player, this); return true; }
                }
            }

            // CIRCLE-specific keys
            if (mode == EventMode.CIRCLE) {
                switch (lower) {
                    case "radius_up"     -> { currentCircleRadius = Math.min(currentCircleRadius + 1.0, 50.0); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "radius_down"   -> { currentCircleRadius = Math.max(currentCircleRadius - 1.0, 1.0); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "count_up"      -> { currentCircleCount = Math.min(currentCircleCount + 1, 32); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "count_down"    -> { currentCircleCount = Math.max(currentCircleCount - 1, 1); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_x_up"   -> { currentCircleOriginX += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_x_down" -> { currentCircleOriginX -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_z_up"   -> { currentCircleOriginZ += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_z_down" -> { currentCircleOriginZ -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                }
            }

            // LINE-specific keys
            if (mode == EventMode.LINE) {
                switch (lower) {
                    case "length_up"    -> { currentLineLength = Math.min(currentLineLength + 1.0, 80.0); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "length_down"  -> { currentLineLength = Math.max(currentLineLength - 1.0, 1.0); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "angle_up"     -> { currentLineAngle = (currentLineAngle + 15.0) % 360.0; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "angle_down"   -> { currentLineAngle = ((currentLineAngle - 15.0) % 360.0 + 360.0) % 360.0; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "count_up"     -> { currentLineCount = Math.min(currentLineCount + 1, 32); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "count_down"   -> { currentLineCount = Math.max(currentLineCount - 1, 1); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "start_x_up"   -> { currentLineStartX += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "start_x_down" -> { currentLineStartX -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "start_z_up"   -> { currentLineStartZ += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "start_z_down" -> { currentLineStartZ -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                }
            }

            // RANDOM-specific keys
            if (mode == EventMode.RANDOM) {
                switch (lower) {
                    case "radius_up"      -> { currentRandomRadius = Math.min(currentRandomRadius + 1.0, 50.0); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "radius_down"    -> { currentRandomRadius = Math.max(currentRandomRadius - 1.0, 1.0); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "count_up"       -> { currentRandomCount = Math.min(currentRandomCount + 1, 32); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "count_down"     -> { currentRandomCount = Math.max(currentRandomCount - 1, 1); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_x_up"    -> { currentRandomOriginX += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_x_down"  -> { currentRandomOriginX -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_z_up"    -> { currentRandomOriginZ += 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "origin_z_down"  -> { currentRandomOriginZ -= 0.5; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "seed_randomize" -> { currentRandomSeed = new Random().nextLong(); autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                    case "seed_clear"     -> { currentRandomSeed = null; autoPreview(); FireworksPanelBuilder.sendPanel(player, this); return true; }
                }
            }
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Accessors (package-accessible for FireworksPanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode() { return mode; }

    public boolean isAutoPreview() { return cueSession.isAutoPreview(); }

    // FIREWORK (single)
    public String getCurrentFwAnchor() { return currentFwAnchor; }
    public String getCurrentFwPreset() { return currentFwPreset; }
    public double getCurrentFwOffX()   { return currentFwOffX; }
    public double getCurrentFwOffY()   { return currentFwOffY; }
    public double getCurrentFwOffZ()   { return currentFwOffZ; }
    public String getCurrentFwYMode()  { return currentFwYMode; }

    // Pattern common
    public String  getCurrentPatAnchor()    { return currentPatAnchor; }
    public String  getCurrentPatPreset()    { return currentPatPreset; }
    public String  getCurrentPatYMode()     { return currentPatYMode; }
    public double  getCurrentPatYOffset()   { return currentPatYOffset; }
    public String  getCurrentPatPowerVar()  { return currentPatPowerVar; }
    public String  getCurrentPatColorVar()  { return currentPatColorVar; }
    public String  getCurrentPatGradFrom()  { return currentPatGradFrom; }
    public String  getCurrentPatGradTo()    { return currentPatGradTo; }

    // Chase
    public boolean isCurrentChaseEnabled()   { return currentChaseEnabled; }
    public String  getCurrentChaseDir()      { return currentChaseDir; }
    public int     getCurrentChaseInterval() { return currentChaseInterval; }

    // CIRCLE
    public double getCurrentCircleRadius()   { return currentCircleRadius; }
    public int    getCurrentCircleCount()    { return currentCircleCount; }
    public double getCurrentCircleOriginX()  { return currentCircleOriginX; }
    public double getCurrentCircleOriginZ()  { return currentCircleOriginZ; }

    // LINE
    public double getCurrentLineLength()  { return currentLineLength; }
    public double getCurrentLineAngle()   { return currentLineAngle; }
    public int    getCurrentLineCount()   { return currentLineCount; }
    public double getCurrentLineStartX()  { return currentLineStartX; }
    public double getCurrentLineStartZ()  { return currentLineStartZ; }

    // RANDOM
    public double getCurrentRandomRadius()   { return currentRandomRadius; }
    public int    getCurrentRandomCount()    { return currentRandomCount; }
    public double getCurrentRandomOriginX()  { return currentRandomOriginX; }
    public double getCurrentRandomOriginZ()  { return currentRandomOriginZ; }
    public Long   getCurrentRandomSeed()     { return currentRandomSeed; }

    // -----------------------------------------------------------------------
    // Auto-preview
    // -----------------------------------------------------------------------

    private void autoPreview() {
        if (cueSession.isAutoPreview()) firePreview();
    }

    /**
     * Fire a test burst at the designer's location using the current params.
     *
     * Spawns generic white large-ball fireworks — preset appearance is not reproduced
     * here because FireworkRegistry is not wired into this constructor. The preview
     * faithfully shows position, count, and pattern geometry.
     *
     * Chase timing is not reproduced in the preview — all rockets fire simultaneously
     * so the designer can see spatial distribution without waiting on intervals.
     */
    void firePreview() {
        Location anchor = player.getLocation();
        FireworkEffect previewEffect = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(Color.WHITE)
            .build();

        switch (mode) {
            case FIREWORK -> {
                Location loc = PositionResolver.fireworkLocation(
                    anchor, currentFwOffX, currentFwOffZ, currentFwYMode, currentFwOffY);
                spawnPreviewFirework(loc, previewEffect);
            }
            case CIRCLE -> {
                for (int i = 0; i < currentCircleCount; i++) {
                    double[] pt = PositionResolver.circlePoint(
                        currentCircleOriginX, currentCircleOriginZ,
                        currentCircleRadius, i, currentCircleCount);
                    spawnPreviewFirework(
                        PositionResolver.fireworkLocation(anchor, pt[0], pt[1], currentPatYMode, currentPatYOffset),
                        previewEffect);
                }
            }
            case LINE -> {
                int count = currentLineCount;
                for (int i = 0; i < count; i++) {
                    double t = count > 1 ? (double) i * currentLineLength / (count - 1) : 0;
                    double[] pt = PositionResolver.linePoint(
                        currentLineStartX, currentLineStartZ, currentLineAngle, t);
                    spawnPreviewFirework(
                        PositionResolver.fireworkLocation(anchor, pt[0], pt[1], currentPatYMode, currentPatYOffset),
                        previewEffect);
                }
            }
            case RANDOM -> {
                Random rand = (currentRandomSeed != null)
                    ? new Random(currentRandomSeed)
                    : new Random();
                for (int i = 0; i < currentRandomCount; i++) {
                    double ang  = rand.nextDouble() * 2 * Math.PI;
                    double dist = Math.sqrt(rand.nextDouble()) * currentRandomRadius;
                    double dx   = currentRandomOriginX + dist * Math.sin(ang);
                    double dz   = currentRandomOriginZ + dist * Math.cos(ang);
                    spawnPreviewFirework(
                        PositionResolver.fireworkLocation(anchor, dx, dz, currentPatYMode, currentPatYOffset),
                        previewEffect);
                }
            }
            default -> { /* FAN / PHRASE: no preview */ }
        }
    }

    private void spawnPreviewFirework(Location loc, FireworkEffect effect) {
        if (loc == null || loc.getWorld() == null) return;
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(effect);
        fw.setFireworkMeta(meta);
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        switch (mode) {
            case FIREWORK -> {
                eventMap.put("at", 0); eventMap.put("type", "FIREWORK");
                eventMap.put("anchor", currentFwAnchor);
                eventMap.put("preset", currentFwPreset);
                Map<String, Object> off = new LinkedHashMap<>();
                off.put("x", currentFwOffX); off.put("y", currentFwOffY); off.put("z", currentFwOffZ);
                eventMap.put("offset", off);
                eventMap.put("y_mode", currentFwYMode);
            }
            case CIRCLE -> {
                eventMap.put("at", 0); eventMap.put("type", "FIREWORK_CIRCLE");
                eventMap.put("anchor", currentPatAnchor);
                eventMap.put("preset", currentPatPreset);
                Map<String, Object> orig = new LinkedHashMap<>();
                orig.put("x", currentCircleOriginX); orig.put("z", currentCircleOriginZ);
                eventMap.put("origin_offset", orig);
                eventMap.put("radius", currentCircleRadius);
                eventMap.put("count", currentCircleCount);
                eventMap.put("y_mode", currentPatYMode);
                eventMap.put("y_offset", currentPatYOffset);
                eventMap.put("power_variation", currentPatPowerVar);
                eventMap.put("color_variation", currentPatColorVar);
                if ("GRADIENT".equals(currentPatColorVar)) {
                    eventMap.put("gradient_from", currentPatGradFrom);
                    eventMap.put("gradient_to",   currentPatGradTo);
                }
                Map<String, Object> chase = new LinkedHashMap<>();
                chase.put("enabled", currentChaseEnabled);
                if (currentChaseEnabled) {
                    chase.put("direction", currentChaseDir);
                    chase.put("interval_ticks", currentChaseInterval);
                }
                eventMap.put("chase", chase);
            }
            case LINE -> {
                eventMap.put("at", 0); eventMap.put("type", "FIREWORK_LINE");
                eventMap.put("anchor", currentPatAnchor);
                eventMap.put("preset", currentPatPreset);
                Map<String, Object> start = new LinkedHashMap<>();
                start.put("x", currentLineStartX); start.put("z", currentLineStartZ);
                eventMap.put("start_offset", start);
                eventMap.put("length", currentLineLength);
                eventMap.put("angle", currentLineAngle);
                eventMap.put("count", currentLineCount);
                eventMap.put("y_mode", currentPatYMode);
                eventMap.put("y_offset", currentPatYOffset);
                eventMap.put("power_variation", currentPatPowerVar);
                eventMap.put("color_variation", currentPatColorVar);
                if ("GRADIENT".equals(currentPatColorVar)) {
                    eventMap.put("gradient_from", currentPatGradFrom);
                    eventMap.put("gradient_to",   currentPatGradTo);
                }
                Map<String, Object> chase = new LinkedHashMap<>();
                chase.put("enabled", currentChaseEnabled);
                if (currentChaseEnabled) {
                    chase.put("direction", currentChaseDir);
                    chase.put("interval_ticks", currentChaseInterval);
                }
                eventMap.put("chase", chase);
            }
            case RANDOM -> {
                eventMap.put("at", 0); eventMap.put("type", "FIREWORK_RANDOM");
                eventMap.put("anchor", currentPatAnchor);
                eventMap.put("preset", currentPatPreset);
                Map<String, Object> orig = new LinkedHashMap<>();
                orig.put("x", currentRandomOriginX); orig.put("z", currentRandomOriginZ);
                eventMap.put("origin_offset", orig);
                eventMap.put("radius", currentRandomRadius);
                eventMap.put("count", currentRandomCount);
                eventMap.put("y_mode", currentPatYMode);
                eventMap.put("y_offset", currentPatYOffset);
                eventMap.put("power_variation", currentPatPowerVar);
                eventMap.put("color_variation", currentPatColorVar);
                if ("GRADIENT".equals(currentPatColorVar)) {
                    eventMap.put("gradient_from", currentPatGradFrom);
                    eventMap.put("gradient_to",   currentPatGradTo);
                }
                if (currentRandomSeed != null) eventMap.put("seed", currentRandomSeed);
            }
            default -> { return; /* FAN / PHRASE: no-op */ }
        }
        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[FireworksEdit] Saved cue override for " + cueId + " → mode=" + mode);
    }

    // -----------------------------------------------------------------------
    // Event finder
    // -----------------------------------------------------------------------

    private ShowEvent findFireworkEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof FireworkEvent
                || event instanceof FireworkCircleEvent
                || event instanceof FireworkLineEvent
                || event instanceof FireworkFanEvent
                || event instanceof FireworkRandomEvent) {
                return event;
            }
        }
        return null;
    }
}
