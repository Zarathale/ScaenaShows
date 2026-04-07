package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.PlayerEvents.PlayerFlightEvent;
import com.scaena.shows.model.event.PlayerEvents.PlayerTeleportEvent;
import com.scaena.shows.model.event.PlayerEvents.PlayerVelocityEvent;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.StageEvents.CrossToEvent;
import com.scaena.shows.model.event.TextEvents.PhraseEvent;
import com.scaena.shows.model.event.VisualEvents.EffectEvent;
import com.scaena.shows.model.event.VisualEvents.ParticleEvent;
import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Effects (Felix).
 *
 * Handles seven event types found in effects cues:
 *
 *   EFFECT          — potion effect on participants; panel + auto-preview ON
 *   PARTICLE        — particle burst or atmospheric; panel + auto-preview ON
 *   PLAYER_TELEPORT — instant position change; panel + auto-preview OFF
 *   CROSS_TO        — entity/player path to mark; panel + auto-preview OFF
 *   PLAYER_VELOCITY — vector impulse; panel + auto-preview OFF
 *   PLAYER_FLIGHT   — hover / release; panel + auto-preview OFF
 *   EFFECT_PATTERN  — computed levitation cadence; panel + auto-preview ON (100t window)
 *   EFFECT_PHRASE   — authored EFFECT + PARTICLE sequence; step-list panel
 *
 * Auto-preview policy:
 *   EFFECT:         re-applies potion effect to designer on each param change
 *   PARTICLE:       spawns a test burst at the designer's location on each change
 *   EFFECT_PATTERN: runs a 100t cadence preview window on each change
 *   Movement types: explicit [▶ Preview] only
 *
 * EFFECT_PHRASE uses type: PHRASE in YAML (same as Voice PHRASE).
 * EFFECT_PATTERN is a new type (not yet in EventType enum); detected via YAML override.
 *
 * Preset naming:
 *   EFFECT:         effects.[effect_id].[slug]
 *   PARTICLE:       effects.particle.[slug]
 *   PLAYER_VELOCITY: effects.velocity.[slug]
 *   PLAYER_FLIGHT (release): effects.flight.release.[slug]
 *   EFFECT_PATTERN: effects.levitation.[slug]
 *   No presets: PLAYER_TELEPORT, CROSS_TO, EFFECT_PHRASE
 *
 * Spec: kb/system/phase2-department-panels.md §Effects
 */
public final class EffectsEditSession implements DeptEditSession {

    // -----------------------------------------------------------------------
    // Curated lists
    // -----------------------------------------------------------------------

    /** Effect IDs organised by KB category: Aerial, Perceptual. */
    public static final String[] EFFECT_CATEGORY_LABELS = { "Aerial", "Perceptual" };
    public static final List<List<String>> EFFECT_CATEGORIES = List.of(
        List.of("levitation", "slow_falling"),
        List.of("blindness", "darkness", "nausea", "slowness", "speed", "night_vision")
    );

    /** Particle IDs organised by KB atmospheric category. */
    public static final String[] PARTICLE_CATEGORY_LABELS = {
        "Quiet devastation", "Heat/danger", "Arrival/ethereal", "Nature/alien", "Expressive"
    };
    public static final List<List<String>> PARTICLE_CATEGORIES = List.of(
        List.of("minecraft:ash", "minecraft:smoke"),
        List.of("minecraft:flame", "minecraft:soul_fire_flame"),
        List.of("minecraft:end_rod", "minecraft:enchant", "minecraft:totem_of_undying"),
        List.of("minecraft:warped_spore", "minecraft:crimson_spore", "minecraft:snowflake"),
        List.of("minecraft:heart", "minecraft:note")
    );

    /** Audience options for effects events. */
    public static final List<String> AUDIENCES = List.of(
        "participants", "group_1", "group_2", "invoker"
    );

    /** Compass facing options for CROSS_TO. */
    public static final List<String> COMPASS_FACINGS = List.of(
        "compass:north", "compass:east", "compass:south", "compass:west"
    );

    /** Release effect options for PLAYER_FLIGHT. */
    public static final List<String> RELEASE_EFFECTS = List.of(
        "slow_falling", "levitate", "none"
    );

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode {
        EFFECT, PARTICLE, PLAYER_TELEPORT, CROSS_TO, PLAYER_VELOCITY,
        PLAYER_FLIGHT, EFFECT_PATTERN, EFFECT_PHRASE
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
    // Mode
    // -----------------------------------------------------------------------

    private final EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel) — all final, assigned in every branch
    // -----------------------------------------------------------------------

    // EFFECT + EFFECT_PATTERN shared potion fields
    private final String  entryEffectId;
    private final int     entryEffectAmplifier;
    private final int     entryEffectDurationTicks;
    private final boolean entryEffectHideParticles;
    private final String  entryEffectAudience;
    // EFFECT_PATTERN extras
    private final int     entryPatternCycleTicks;
    private final int     entryPatternTotalDuration;

    // PARTICLE
    private final String  entryParticleId;
    private final int     entryParticleCount;
    private final double  entryParticleOffX;
    private final double  entryParticleOffY;
    private final double  entryParticleOffZ;
    private final double  entryParticleExtra;
    private final boolean entryParticleForce;
    private final int     entryParticleDurationTicks;  // -1 = single burst
    private final int     entryParticleIntervalTicks;

    // PLAYER_TELEPORT
    private final String  entryTeleportDestination;
    private final boolean entryTeleportUseOffset;
    private final double  entryTeleportOffX;
    private final double  entryTeleportOffY;
    private final double  entryTeleportOffZ;
    private final float   entryTeleportYaw;
    private final float   entryTeleportPitch;
    private final String  entryTeleportAudience;

    // CROSS_TO
    private final String  entryCrossDestination;
    private final int     entryCrossDurationTicks;
    private final String  entryCrossFacing;   // null = not set
    private final String  entryCrossTarget;

    // PLAYER_VELOCITY
    private final double  entryVecX;
    private final double  entryVecY;
    private final double  entryVecZ;
    private final String  entryVecAudience;

    // PLAYER_FLIGHT
    private final String  entryFlightState;
    private final String  entryFlightReleaseEffect;
    private final int     entryFlightReleaseDurationTicks;
    private final String  entryFlightAudience;

    // EFFECT_PHRASE
    private final String                    entryPhraseAudience;
    private final List<Map<String, Object>> entryPhraseSteps;

    // -----------------------------------------------------------------------
    // Current mutable state
    // -----------------------------------------------------------------------

    private String  currentEffectId;
    private int     currentEffectAmplifier;
    private int     currentEffectDurationTicks;
    private boolean currentEffectHideParticles;
    private String  currentEffectAudience;
    private int     currentPatternCycleTicks;
    private int     currentPatternTotalDuration;

    private String  currentParticleId;
    private int     currentParticleCount;
    private double  currentParticleOffX;
    private double  currentParticleOffY;
    private double  currentParticleOffZ;
    private double  currentParticleExtra;
    private boolean currentParticleForce;
    private int     currentParticleDurationTicks;
    private int     currentParticleIntervalTicks;

    private String  currentTeleportDestination;
    private boolean currentTeleportUseOffset;
    private double  currentTeleportOffX;
    private double  currentTeleportOffY;
    private double  currentTeleportOffZ;
    private float   currentTeleportYaw;
    private float   currentTeleportPitch;
    private String  currentTeleportAudience;

    private String  currentCrossDestination;
    private int     currentCrossDurationTicks;
    private String  currentCrossFacing;
    private String  currentCrossTarget;

    private double  currentVecX;
    private double  currentVecY;
    private double  currentVecZ;
    private String  currentVecAudience;

    private String  currentFlightState;
    private String  currentFlightReleaseEffect;
    private int     currentFlightReleaseDurationTicks;
    private String  currentFlightAudience;

    private String                    currentPhraseAudience;
    private List<Map<String, Object>> currentPhraseSteps;
    /** -1 = showing step list; ≥0 = editing that step inline. */
    private int editingStepIndex = -1;

    /** Running EFFECT_PATTERN cadence preview; cancelled on param change or session exit. */
    private BukkitTask patternPreviewTask;

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public EffectsEditSession(
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

        ShowEvent found      = findEffectsEvent(cueId);
        String    overrideType = editor.getCueOverrideFirstEventType(cueId);

        // -------------------------------------------------------------------
        // Detect mode; assign ALL final fields in every branch.
        // Java's definite-assignment rules require every final field to be set
        // exactly once. Each branch seeds the active mode from the found event
        // and fills every other mode with sensible defaults.
        // -------------------------------------------------------------------
        if (found instanceof EffectEvent ee) {
            mode = EventMode.EFFECT;
            entryEffectId = ee.effectId;                entryEffectAmplifier = ee.amplifier;
            entryEffectDurationTicks = ee.durationTicks; entryEffectHideParticles = ee.hideParticles;
            entryEffectAudience = ee.audience;
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else if (found instanceof ParticleEvent pe) {
            mode = EventMode.PARTICLE;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = pe.particleId;            entryParticleCount = pe.count;
            entryParticleOffX = pe.offsetX;             entryParticleOffY = pe.offsetY;
            entryParticleOffZ = pe.offsetZ;             entryParticleExtra = pe.extra;
            entryParticleForce = pe.force;
            entryParticleDurationTicks = pe.durationTicks;
            entryParticleIntervalTicks = pe.intervalTicks;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else if (found instanceof PlayerTeleportEvent te) {
            mode = EventMode.PLAYER_TELEPORT;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = te.destination != null ? te.destination : "";
            entryTeleportUseOffset = te.hasOffset;
            entryTeleportOffX = te.offsetX;             entryTeleportOffY = te.offsetY;
            entryTeleportOffZ = te.offsetZ;
            entryTeleportYaw = Float.isNaN(te.yaw) ? 0f : te.yaw;
            entryTeleportPitch = Float.isNaN(te.pitch) ? 0f : te.pitch;
            entryTeleportAudience = te.audience;
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else if (found instanceof CrossToEvent ce) {
            mode = EventMode.CROSS_TO;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = ce.destination;     entryCrossDurationTicks = ce.durationTicks;
            entryCrossFacing = ce.facing;               entryCrossTarget = ce.target;
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else if (found instanceof PlayerVelocityEvent ve) {
            mode = EventMode.PLAYER_VELOCITY;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = ve.vecX; entryVecY = ve.vecY;  entryVecZ = ve.vecZ;
            entryVecAudience = ve.audience;
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else if (found instanceof PlayerFlightEvent fe) {
            mode = EventMode.PLAYER_FLIGHT;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = fe.state;
            entryFlightReleaseEffect = fe.releaseEffect;
            entryFlightReleaseDurationTicks = fe.releaseDurationTicks;
            entryFlightAudience = fe.audience;
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else if (found instanceof PhraseEvent pe) {
            mode = EventMode.EFFECT_PHRASE;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = pe.audience;          entryPhraseSteps = deepCopySteps(pe.rawSteps);

        } else if ("EFFECT_PATTERN".equals(overrideType)) {
            // EFFECT_PATTERN is not in EventType — detected via rawYaml override
            mode = EventMode.EFFECT_PATTERN;
            // Read fields from the override map
            Map<String, Object> overMap = getOverrideEventMap(cueId);
            entryEffectId = strFromMap(overMap, "effect_id", "levitation");
            entryEffectAmplifier = intFromMap(overMap, "amplifier", 0);
            entryEffectDurationTicks = intFromMap(overMap, "duration_ticks", 20);
            entryEffectHideParticles = boolFromMap(overMap, "hide_particles", true);
            entryEffectAudience = strFromMap(overMap, "audience", "participants");
            entryPatternCycleTicks = intFromMap(overMap, "cycle_ticks", 28);
            entryPatternTotalDuration = intFromMap(overMap, "total_duration", 300);
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();

        } else {
            // Default: EFFECT mode for empty/stub cues
            mode = EventMode.EFFECT;
            entryEffectId = "levitation";               entryEffectAmplifier = 0;
            entryEffectDurationTicks = 200;             entryEffectHideParticles = true;
            entryEffectAudience = "participants";
            entryPatternCycleTicks = 28;                entryPatternTotalDuration = 300;
            entryParticleId = "minecraft:end_rod";      entryParticleCount = 10;
            entryParticleOffX = 1.0; entryParticleOffY = 1.0; entryParticleOffZ = 1.0;
            entryParticleExtra = 0.0;                   entryParticleForce = true;
            entryParticleDurationTicks = -1;            entryParticleIntervalTicks = 10;
            entryTeleportDestination = "";              entryTeleportUseOffset = false;
            entryTeleportOffX = 0; entryTeleportOffY = 0; entryTeleportOffZ = 0;
            entryTeleportYaw = 0;                       entryTeleportPitch = 0;
            entryTeleportAudience = "participants";
            entryCrossDestination = "";                 entryCrossDurationTicks = 60;
            entryCrossFacing = null;                    entryCrossTarget = "participants";
            entryVecX = 0; entryVecY = 0.4; entryVecZ = 0; entryVecAudience = "participants";
            entryFlightState = "hover";                 entryFlightReleaseEffect = "slow_falling";
            entryFlightReleaseDurationTicks = 300;      entryFlightAudience = "participants";
            entryPhraseAudience = "participants";       entryPhraseSteps = new ArrayList<>();
        }

        // ---- Initialise current state from entry snapshots ----
        currentEffectId               = entryEffectId;
        currentEffectAmplifier        = entryEffectAmplifier;
        currentEffectDurationTicks    = entryEffectDurationTicks;
        currentEffectHideParticles    = entryEffectHideParticles;
        currentEffectAudience         = entryEffectAudience;
        currentPatternCycleTicks      = entryPatternCycleTicks;
        currentPatternTotalDuration   = entryPatternTotalDuration;
        currentParticleId             = entryParticleId;
        currentParticleCount          = entryParticleCount;
        currentParticleOffX           = entryParticleOffX;
        currentParticleOffY           = entryParticleOffY;
        currentParticleOffZ           = entryParticleOffZ;
        currentParticleExtra          = entryParticleExtra;
        currentParticleForce          = entryParticleForce;
        currentParticleDurationTicks  = entryParticleDurationTicks;
        currentParticleIntervalTicks  = entryParticleIntervalTicks;
        currentTeleportDestination    = entryTeleportDestination;
        currentTeleportUseOffset      = entryTeleportUseOffset;
        currentTeleportOffX           = entryTeleportOffX;
        currentTeleportOffY           = entryTeleportOffY;
        currentTeleportOffZ           = entryTeleportOffZ;
        currentTeleportYaw            = entryTeleportYaw;
        currentTeleportPitch          = entryTeleportPitch;
        currentTeleportAudience       = entryTeleportAudience;
        currentCrossDestination       = entryCrossDestination;
        currentCrossDurationTicks     = entryCrossDurationTicks;
        currentCrossFacing            = entryCrossFacing;
        currentCrossTarget            = entryCrossTarget;
        currentVecX                   = entryVecX;
        currentVecY                   = entryVecY;
        currentVecZ                   = entryVecZ;
        currentVecAudience            = entryVecAudience;
        currentFlightState            = entryFlightState;
        currentFlightReleaseEffect    = entryFlightReleaseEffect;
        currentFlightReleaseDurationTicks = entryFlightReleaseDurationTicks;
        currentFlightAudience         = entryFlightAudience;
        currentPhraseAudience         = entryPhraseAudience;
        currentPhraseSteps            = deepCopySteps(entryPhraseSteps);
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "effects"; }

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
            case EFFECT -> editor.saveAsPreset(cueId, "effects." + currentEffectId + "." + slug);
            case PARTICLE -> editor.saveAsPreset(cueId, "effects.particle." + slug);
            case PLAYER_VELOCITY -> editor.saveAsPreset(cueId, "effects.velocity." + slug);
            case PLAYER_FLIGHT -> {
                if ("release".equals(currentFlightState)) {
                    editor.saveAsPreset(cueId, "effects.flight.release." + slug);
                }
                // hover has no params; no preset
            }
            case EFFECT_PATTERN -> editor.saveAsPreset(cueId, "effects.levitation." + slug);
            // PLAYER_TELEPORT, CROSS_TO, EFFECT_PHRASE: no presets per spec
            default -> { /* no-op */ }
        }
    }

    @Override
    public void onCancel() {
        cancelPatternPreview();
        currentEffectId               = entryEffectId;
        currentEffectAmplifier        = entryEffectAmplifier;
        currentEffectDurationTicks    = entryEffectDurationTicks;
        currentEffectHideParticles    = entryEffectHideParticles;
        currentEffectAudience         = entryEffectAudience;
        currentPatternCycleTicks      = entryPatternCycleTicks;
        currentPatternTotalDuration   = entryPatternTotalDuration;
        currentParticleId             = entryParticleId;
        currentParticleCount          = entryParticleCount;
        currentParticleOffX           = entryParticleOffX;
        currentParticleOffY           = entryParticleOffY;
        currentParticleOffZ           = entryParticleOffZ;
        currentParticleExtra          = entryParticleExtra;
        currentParticleForce          = entryParticleForce;
        currentParticleDurationTicks  = entryParticleDurationTicks;
        currentParticleIntervalTicks  = entryParticleIntervalTicks;
        currentTeleportDestination    = entryTeleportDestination;
        currentTeleportUseOffset      = entryTeleportUseOffset;
        currentTeleportOffX           = entryTeleportOffX;
        currentTeleportOffY           = entryTeleportOffY;
        currentTeleportOffZ           = entryTeleportOffZ;
        currentTeleportYaw            = entryTeleportYaw;
        currentTeleportPitch          = entryTeleportPitch;
        currentTeleportAudience       = entryTeleportAudience;
        currentCrossDestination       = entryCrossDestination;
        currentCrossDurationTicks     = entryCrossDurationTicks;
        currentCrossFacing            = entryCrossFacing;
        currentCrossTarget            = entryCrossTarget;
        currentVecX                   = entryVecX;
        currentVecY                   = entryVecY;
        currentVecZ                   = entryVecZ;
        currentVecAudience            = entryVecAudience;
        currentFlightState            = entryFlightState;
        currentFlightReleaseEffect    = entryFlightReleaseEffect;
        currentFlightReleaseDurationTicks = entryFlightReleaseDurationTicks;
        currentFlightAudience         = entryFlightAudience;
        currentPhraseAudience         = entryPhraseAudience;
        currentPhraseSteps            = deepCopySteps(entryPhraseSteps);
        editingStepIndex              = -1;
    }

    /**
     * Handle a /scaena tech2 editparam <key> [value...] command.
     *
     * === EFFECT keys ===
     *   effect_id_panel           — open effect ID selector sub-panel
     *   effect_id <id>            — set effect_id
     *   amplifier_up / down       — ±1 amplifier
     *   dur_up / down             — ±10t duration_ticks
     *   hide_toggle               — toggle hide_particles
     *   audience <val>            — set audience
     *   preview_effect            — fire preview once
     *   auto_preview_toggle       — toggle session auto-preview
     *
     * === PARTICLE keys ===
     *   particle_id_panel         — open particle ID selector sub-panel
     *   particle_id <id>          — set particle_id
     *   count_up / down           — ±1 count
     *   count_shift_up / down     — ±10 count
     *   off_x_up / down           — ±0.5 offset.x
     *   off_y_up / down           — ±0.5 offset.y
     *   off_z_up / down           — ±0.5 offset.z
     *   extra_up / down           — ±0.01 extra
     *   force_toggle              — toggle force
     *   particle_dur_up / down    — ±20t duration_ticks (adds if absent)
     *   particle_dur_shift_up     — +100t
     *   particle_dur_shift_down   — -100t
     *   particle_dur_clear        — remove duration (→ single burst)
     *   interval_up / down        — ±2t interval_ticks
     *   preview_particle          — fire preview burst once
     *   auto_preview_toggle       — toggle session auto-preview
     *
     * === PLAYER_TELEPORT keys ===
     *   dest <val>                — set destination (set:Name or offset_mode)
     *   offset_mode_toggle        — toggle useOffset
     *   tp_off_x_up / down        — ±0.5 offset.x
     *   tp_off_y_up / down        — ±0.5 offset.y
     *   tp_off_z_up / down        — ±0.5 offset.z
     *   yaw_up / down             — ±5° yaw
     *   pitch_up / down           — ±5° pitch
     *   tp_audience <val>         — set audience
     *   preview_teleport          — fire teleport once
     *
     * === CROSS_TO keys ===
     *   cross_dest <val>          — set destination mark
     *   cross_dur_up / down       — ±20t duration_ticks
     *   cross_facing <val>        — set facing (compass:* or "none" to clear)
     *   cross_target <val>        — set target
     *   preview_cross             — fire cross once
     *
     * === PLAYER_VELOCITY keys ===
     *   vec_x_up / down           — ±0.1 x
     *   vec_x_shift_up / down     — ±0.5 x
     *   vec_y_up / down           — ±0.1 y
     *   vec_y_shift_up / down     — ±0.5 y
     *   vec_z_up / down           — ±0.1 z
     *   vec_z_shift_up / down     — ±0.5 z
     *   vec_audience <val>        — set audience
     *   preview_velocity          — apply impulse once
     *
     * === PLAYER_FLIGHT keys ===
     *   flight_state <val>        — "hover" | "release"
     *   release_effect <val>      — set release_effect
     *   release_dur_up / down     — ±20t release_duration_ticks
     *   flight_audience <val>     — set audience
     *   preview_flight            — fire flight event once
     *
     * === EFFECT_PATTERN keys ===
     *   (effect_id_panel, effect_id, amplifier_up/down, hide_toggle, audience — same as EFFECT)
     *   pat_dur_up / down         — ±4t duration_ticks (per-pulse)
     *   cycle_up / down           — ±4t cycle_ticks
     *   total_up / down           — ±20t total_duration
     *   total_shift_up / down     — ±100t total_duration
     *   preview_pattern           — run 100t cadence preview
     *   auto_preview_toggle
     *
     * === EFFECT_PHRASE keys ===
     *   phrase_audience <val>     — set phrase audience
     *   step_add                  — append a new EFFECT step
     *   step_edit <i>             — open inline step editor for step i
     *   step_done                 — return to step list from inline editor
     *   step_delete <i>           — delete step i
     *   step_up <i>               — move step i up
     *   step_down <i>             — move step i down
     *   step_type <t>             — change active step type (EFFECT | PARTICLE)
     *   step_timing_mode <at|after> — change timing mode for active step
     *   step_ticks_up / down      — ±10t tick for active step
     *   step_effect_id <id>       — set effect_id on active step
     *   step_amplifier_up / down  — ±1 amplifier on active EFFECT step
     *   step_dur_up / down        — ±10t duration on active step
     *   step_hide_toggle          — toggle hide_particles on active EFFECT step
     *   step_particle_id <id>     — set particle_id on active PARTICLE step
     *   step_count_up / down      — ±1 count on active PARTICLE step
     *   step_extra_up / down      — ±0.01 extra on active PARTICLE step
     *   step_force_toggle         — toggle force on active PARTICLE step
     *   preview_phrase            — run full phrase sequence on designer
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        // ---- EFFECT params ----
        if (mode == EventMode.EFFECT || mode == EventMode.EFFECT_PATTERN) {
            switch (lower) {
                case "effect_id_panel" -> {
                    EffectsPanelBuilder.sendEffectIdPanel(player, this);
                    return true;
                }
                case "effect_id" -> {
                    if (value.isBlank()) return false;
                    currentEffectId = value.trim();
                    autoPreviewEffect();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "amplifier_up" -> {
                    currentEffectAmplifier = Math.min(currentEffectAmplifier + 1, 9);
                    autoPreviewEffect();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "amplifier_down" -> {
                    currentEffectAmplifier = Math.max(currentEffectAmplifier - 1, 0);
                    autoPreviewEffect();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "dur_up" -> {
                    currentEffectDurationTicks = Math.min(
                        currentEffectDurationTicks + (mode == EventMode.EFFECT_PATTERN ? 4 : 10), 6000);
                    autoPreviewEffect();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "dur_down" -> {
                    currentEffectDurationTicks = Math.max(
                        currentEffectDurationTicks - (mode == EventMode.EFFECT_PATTERN ? 4 : 10), 1);
                    autoPreviewEffect();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "hide_toggle" -> {
                    currentEffectHideParticles = !currentEffectHideParticles;
                    autoPreviewEffect();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "audience" -> {
                    currentEffectAudience = value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_effect" -> { firePreviewEffect();   return true; }
                case "preview_pattern" -> { firePreviewPattern(); return true; }
                case "auto_preview_toggle" -> {
                    cueSession.setAutoPreview(!cueSession.isAutoPreview());
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
            }
            // EFFECT_PATTERN-only keys
            if (mode == EventMode.EFFECT_PATTERN) {
                switch (lower) {
                    case "cycle_up" -> {
                        currentPatternCycleTicks = Math.min(currentPatternCycleTicks + 4, 200);
                        autoPreviewEffect();
                        EffectsPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "cycle_down" -> {
                        currentPatternCycleTicks = Math.max(currentPatternCycleTicks - 4, 1);
                        autoPreviewEffect();
                        EffectsPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "total_up" -> {
                        currentPatternTotalDuration = Math.min(currentPatternTotalDuration + 20, 6000);
                        EffectsPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "total_down" -> {
                        currentPatternTotalDuration = Math.max(currentPatternTotalDuration - 20, 20);
                        EffectsPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "total_shift_up" -> {
                        currentPatternTotalDuration = Math.min(currentPatternTotalDuration + 100, 6000);
                        EffectsPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "total_shift_down" -> {
                        currentPatternTotalDuration = Math.max(currentPatternTotalDuration - 100, 20);
                        EffectsPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                }
            }
        }

        // ---- PARTICLE params ----
        if (mode == EventMode.PARTICLE) {
            switch (lower) {
                case "particle_id_panel" -> {
                    EffectsPanelBuilder.sendParticleIdPanel(player, this);
                    return true;
                }
                case "particle_id" -> {
                    if (value.isBlank()) return false;
                    currentParticleId = value.trim();
                    autoPreviewParticle();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "count_up" -> {
                    currentParticleCount = Math.min(currentParticleCount + 1, 256);
                    autoPreviewParticle();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "count_down" -> {
                    currentParticleCount = Math.max(currentParticleCount - 1, 1);
                    autoPreviewParticle();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "count_shift_up" -> {
                    currentParticleCount = Math.min(currentParticleCount + 10, 256);
                    autoPreviewParticle();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "count_shift_down" -> {
                    currentParticleCount = Math.max(currentParticleCount - 10, 1);
                    autoPreviewParticle();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "off_x_up" -> { currentParticleOffX = roundDouble(currentParticleOffX + 0.5, 1); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "off_x_down" -> { currentParticleOffX = Math.max(0, roundDouble(currentParticleOffX - 0.5, 1)); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_up" -> { currentParticleOffY = roundDouble(currentParticleOffY + 0.5, 1); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "off_y_down" -> { currentParticleOffY = Math.max(0, roundDouble(currentParticleOffY - 0.5, 1)); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_up" -> { currentParticleOffZ = roundDouble(currentParticleOffZ + 0.5, 1); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "off_z_down" -> { currentParticleOffZ = Math.max(0, roundDouble(currentParticleOffZ - 0.5, 1)); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "extra_up" -> { currentParticleExtra = roundDouble(currentParticleExtra + 0.01, 2); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "extra_down" -> { currentParticleExtra = Math.max(0, roundDouble(currentParticleExtra - 0.01, 2)); autoPreviewParticle(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "force_toggle" -> {
                    currentParticleForce = !currentParticleForce;
                    autoPreviewParticle();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "particle_dur_up" -> {
                    currentParticleDurationTicks = (currentParticleDurationTicks < 0)
                        ? 20 : Math.min(currentParticleDurationTicks + 20, 6000);
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "particle_dur_down" -> {
                    if (currentParticleDurationTicks > 20) currentParticleDurationTicks -= 20;
                    else if (currentParticleDurationTicks > 0) currentParticleDurationTicks = -1;
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "particle_dur_shift_up" -> {
                    currentParticleDurationTicks = (currentParticleDurationTicks < 0)
                        ? 100 : Math.min(currentParticleDurationTicks + 100, 6000);
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "particle_dur_shift_down" -> {
                    if (currentParticleDurationTicks > 100) currentParticleDurationTicks -= 100;
                    else if (currentParticleDurationTicks > 0) currentParticleDurationTicks = -1;
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "particle_dur_clear" -> {
                    currentParticleDurationTicks = -1;
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "interval_up" -> {
                    currentParticleIntervalTicks = Math.min(currentParticleIntervalTicks + 2, 200);
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "interval_down" -> {
                    currentParticleIntervalTicks = Math.max(currentParticleIntervalTicks - 2, 2);
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_particle" -> { firePreviewParticle(); return true; }
                case "auto_preview_toggle" -> {
                    cueSession.setAutoPreview(!cueSession.isAutoPreview());
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                default -> { return false; }
            }
        }

        // ---- PLAYER_TELEPORT params ----
        if (mode == EventMode.PLAYER_TELEPORT) {
            switch (lower) {
                case "dest" -> {
                    currentTeleportDestination = value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "offset_mode_toggle" -> {
                    currentTeleportUseOffset = !currentTeleportUseOffset;
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "tp_off_x_up"   -> { currentTeleportOffX = roundDouble(currentTeleportOffX + 0.5, 1); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "tp_off_x_down" -> { currentTeleportOffX = roundDouble(currentTeleportOffX - 0.5, 1); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "tp_off_y_up"   -> { currentTeleportOffY = roundDouble(currentTeleportOffY + 0.5, 1); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "tp_off_y_down" -> { currentTeleportOffY = roundDouble(currentTeleportOffY - 0.5, 1); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "tp_off_z_up"   -> { currentTeleportOffZ = roundDouble(currentTeleportOffZ + 0.5, 1); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "tp_off_z_down" -> { currentTeleportOffZ = roundDouble(currentTeleportOffZ - 0.5, 1); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "yaw_up"   -> { currentTeleportYaw = (currentTeleportYaw + 5f) % 360f;  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "yaw_down" -> { currentTeleportYaw = (currentTeleportYaw - 5f + 360f) % 360f; EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "pitch_up"   -> { currentTeleportPitch = Math.min(currentTeleportPitch + 5f, 90f);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "pitch_down" -> { currentTeleportPitch = Math.max(currentTeleportPitch - 5f, -90f); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "tp_audience" -> { currentTeleportAudience = value.trim(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "preview_teleport" -> { firePreviewTeleport(); return true; }
                default -> { return false; }
            }
        }

        // ---- CROSS_TO params ----
        if (mode == EventMode.CROSS_TO) {
            switch (lower) {
                case "cross_dest" -> {
                    currentCrossDestination = value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "cross_dur_up"   -> { currentCrossDurationTicks = Math.min(currentCrossDurationTicks + 20, 6000); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "cross_dur_down" -> { currentCrossDurationTicks = Math.max(currentCrossDurationTicks - 20, 0);    EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "cross_facing" -> {
                    currentCrossFacing = "none".equalsIgnoreCase(value.trim()) ? null : value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "cross_target" -> {
                    currentCrossTarget = value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_cross" -> { firePreviewCross(); return true; }
                default -> { return false; }
            }
        }

        // ---- PLAYER_VELOCITY params ----
        if (mode == EventMode.PLAYER_VELOCITY) {
            switch (lower) {
                case "vec_x_up"         -> { currentVecX = roundDouble(currentVecX + 0.1, 2);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_x_down"       -> { currentVecX = roundDouble(currentVecX - 0.1, 2);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_x_shift_up"   -> { currentVecX = roundDouble(currentVecX + 0.5, 1);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_x_shift_down" -> { currentVecX = roundDouble(currentVecX - 0.5, 1);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_y_up"         -> { currentVecY = roundDouble(currentVecY + 0.1, 2);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_y_down"       -> { currentVecY = roundDouble(currentVecY - 0.1, 2);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_y_shift_up"   -> { currentVecY = roundDouble(currentVecY + 0.5, 1);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_y_shift_down" -> { currentVecY = roundDouble(currentVecY - 0.5, 1);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_z_up"         -> { currentVecZ = roundDouble(currentVecZ + 0.1, 2);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_z_down"       -> { currentVecZ = roundDouble(currentVecZ - 0.1, 2);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_z_shift_up"   -> { currentVecZ = roundDouble(currentVecZ + 0.5, 1);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_z_shift_down" -> { currentVecZ = roundDouble(currentVecZ - 0.5, 1);  EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "vec_audience" -> { currentVecAudience = value.trim(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "preview_velocity" -> { firePreviewVelocity(); return true; }
                default -> { return false; }
            }
        }

        // ---- PLAYER_FLIGHT params ----
        if (mode == EventMode.PLAYER_FLIGHT) {
            switch (lower) {
                case "flight_state" -> {
                    if ("hover".equals(value.trim()) || "release".equals(value.trim())) {
                        currentFlightState = value.trim();
                        EffectsPanelBuilder.sendPanel(player, this);
                    }
                    return true;
                }
                case "release_effect" -> {
                    currentFlightReleaseEffect = value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "release_dur_up"   -> { currentFlightReleaseDurationTicks = Math.min(currentFlightReleaseDurationTicks + 20, 6000); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "release_dur_down" -> { currentFlightReleaseDurationTicks = Math.max(currentFlightReleaseDurationTicks - 20, 0);    EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "flight_audience" -> { currentFlightAudience = value.trim(); EffectsPanelBuilder.sendPanel(player, this); return true; }
                case "preview_flight" -> { firePreviewFlight(); return true; }
                default -> { return false; }
            }
        }

        // ---- EFFECT_PHRASE params ----
        if (mode == EventMode.EFFECT_PHRASE) {
            switch (lower) {
                case "phrase_audience" -> {
                    currentPhraseAudience = value.trim();
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "step_add" -> {
                    Map<String, Object> newStep = new LinkedHashMap<>();
                    newStep.put("at", 0);
                    List<Map<String, Object>> events = new ArrayList<>();
                    Map<String, Object> ev = new LinkedHashMap<>();
                    ev.put("type", "EFFECT");
                    ev.put("effect_id", "levitation");
                    ev.put("amplifier", 0);
                    ev.put("duration_ticks", 40);
                    ev.put("hide_particles", true);
                    events.add(ev);
                    newStep.put("events", events);
                    currentPhraseSteps.add(newStep);
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "step_done" -> {
                    editingStepIndex = -1;
                    EffectsPanelBuilder.sendPanel(player, this);
                    return true;
                }
                case "preview_phrase" -> { firePreviewPhrase(); return true; }
                default -> { /* fall through to step-indexed keys */ }
            }

            // Keys with an integer argument: step_edit/delete/up/down <i>
            if (lower.startsWith("step_edit ") || lower.startsWith("step_delete ")
                || lower.startsWith("step_up ") || lower.startsWith("step_down ")) {
                String[] parts = lower.split(" ", 2);
                int idx = parseIndex(parts[1]);
                if (idx < 0 || idx >= currentPhraseSteps.size()) return true;
                switch (parts[0]) {
                    case "step_edit"   -> { editingStepIndex = idx; EffectsPanelBuilder.sendStepPanel(player, this, idx); }
                    case "step_delete" -> { currentPhraseSteps.remove(idx); if (editingStepIndex >= currentPhraseSteps.size()) editingStepIndex = -1; EffectsPanelBuilder.sendPanel(player, this); }
                    case "step_up"     -> { if (idx > 0) { Collections.swap(currentPhraseSteps, idx, idx - 1); } EffectsPanelBuilder.sendPanel(player, this); }
                    case "step_down"   -> { if (idx < currentPhraseSteps.size() - 1) { Collections.swap(currentPhraseSteps, idx, idx + 1); } EffectsPanelBuilder.sendPanel(player, this); }
                }
                return true;
            }

            // Step-level edits (active step = editingStepIndex)
            if (editingStepIndex >= 0 && editingStepIndex < currentPhraseSteps.size()) {
                Map<String, Object> step = currentPhraseSteps.get(editingStepIndex);
                Map<String, Object> ev   = getOrCreateStepEvent(step);
                String stepType = (String) ev.getOrDefault("type", "EFFECT");
                switch (lower) {
                    case "step_type" -> {
                        String t = value.trim().toUpperCase();
                        if ("EFFECT".equals(t) || "PARTICLE".equals(t)) {
                            ev.clear();
                            ev.put("type", t);
                            if ("EFFECT".equals(t)) {
                                ev.put("effect_id", "levitation");
                                ev.put("amplifier", 0);
                                ev.put("duration_ticks", 40);
                                ev.put("hide_particles", true);
                            } else {
                                ev.put("particle_id", "minecraft:end_rod");
                                ev.put("count", 10);
                                ev.put("offset", List.of(1.0, 1.0, 1.0));
                                ev.put("extra", 0.0);
                                ev.put("force", true);
                            }
                        }
                        EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex);
                        return true;
                    }
                    case "step_timing_mode" -> {
                        boolean isAfter = "after".equalsIgnoreCase(value.trim());
                        int ticks = isAfter ? intFromMap(step, "at", 0) : intFromMap(step, "after", 0);
                        step.remove("at"); step.remove("after");
                        step.put(isAfter ? "after" : "at", ticks);
                        EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex);
                        return true;
                    }
                    case "step_ticks_up" -> {
                        String key2 = step.containsKey("after") ? "after" : "at";
                        step.put(key2, Math.max(0, intFromMap(step, key2, 0) + 10));
                        EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex);
                        return true;
                    }
                    case "step_ticks_down" -> {
                        String key2 = step.containsKey("after") ? "after" : "at";
                        step.put(key2, Math.max(0, intFromMap(step, key2, 0) - 10));
                        EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex);
                        return true;
                    }
                    case "step_effect_id" -> {
                        if ("EFFECT".equals(stepType)) { ev.put("effect_id", value.trim()); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_amplifier_up" -> {
                        if ("EFFECT".equals(stepType)) { ev.put("amplifier", Math.min(intFromMap(ev, "amplifier", 0) + 1, 9)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_amplifier_down" -> {
                        if ("EFFECT".equals(stepType)) { ev.put("amplifier", Math.max(intFromMap(ev, "amplifier", 0) - 1, 0)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_dur_up" -> {
                        ev.put("duration_ticks", Math.min(intFromMap(ev, "duration_ticks", 40) + 10, 6000));
                        EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex);
                        return true;
                    }
                    case "step_dur_down" -> {
                        ev.put("duration_ticks", Math.max(intFromMap(ev, "duration_ticks", 40) - 10, 1));
                        EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex);
                        return true;
                    }
                    case "step_hide_toggle" -> {
                        if ("EFFECT".equals(stepType)) { ev.put("hide_particles", !(Boolean) ev.getOrDefault("hide_particles", true)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_particle_id" -> {
                        if ("PARTICLE".equals(stepType)) { ev.put("particle_id", value.trim()); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_count_up" -> {
                        if ("PARTICLE".equals(stepType)) { ev.put("count", Math.min(intFromMap(ev, "count", 1) + 1, 256)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_count_down" -> {
                        if ("PARTICLE".equals(stepType)) { ev.put("count", Math.max(intFromMap(ev, "count", 1) - 1, 1)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_extra_up" -> {
                        if ("PARTICLE".equals(stepType)) { ev.put("extra", roundDouble(dblFromMap(ev, "extra", 0.0) + 0.01, 2)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_extra_down" -> {
                        if ("PARTICLE".equals(stepType)) { ev.put("extra", Math.max(0, roundDouble(dblFromMap(ev, "extra", 0.0) - 0.01, 2))); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                    case "step_force_toggle" -> {
                        if ("PARTICLE".equals(stepType)) { ev.put("force", !(Boolean) ev.getOrDefault("force", true)); EffectsPanelBuilder.sendStepPanel(player, this, editingStepIndex); }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Accessors (for EffectsPanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode()                    { return mode; }
    public String  getCurrentEffectId()           { return currentEffectId; }
    public int     getCurrentEffectAmplifier()    { return currentEffectAmplifier; }
    public int     getCurrentEffectDurationTicks(){ return currentEffectDurationTicks; }
    public boolean isCurrentEffectHideParticles() { return currentEffectHideParticles; }
    public String  getCurrentEffectAudience()     { return currentEffectAudience; }
    public int     getCurrentPatternCycleTicks()  { return currentPatternCycleTicks; }
    public int     getCurrentPatternTotalDuration(){ return currentPatternTotalDuration; }

    public String  getCurrentParticleId()         { return currentParticleId; }
    public int     getCurrentParticleCount()      { return currentParticleCount; }
    public double  getCurrentParticleOffX()       { return currentParticleOffX; }
    public double  getCurrentParticleOffY()       { return currentParticleOffY; }
    public double  getCurrentParticleOffZ()       { return currentParticleOffZ; }
    public double  getCurrentParticleExtra()      { return currentParticleExtra; }
    public boolean isCurrentParticleForce()       { return currentParticleForce; }
    public int     getCurrentParticleDurationTicks(){ return currentParticleDurationTicks; }
    public int     getCurrentParticleIntervalTicks(){ return currentParticleIntervalTicks; }

    public String  getCurrentTeleportDestination(){ return currentTeleportDestination; }
    public boolean isCurrentTeleportUseOffset()   { return currentTeleportUseOffset; }
    public double  getCurrentTeleportOffX()       { return currentTeleportOffX; }
    public double  getCurrentTeleportOffY()       { return currentTeleportOffY; }
    public double  getCurrentTeleportOffZ()       { return currentTeleportOffZ; }
    public float   getCurrentTeleportYaw()        { return currentTeleportYaw; }
    public float   getCurrentTeleportPitch()      { return currentTeleportPitch; }
    public String  getCurrentTeleportAudience()   { return currentTeleportAudience; }

    public String  getCurrentCrossDestination()   { return currentCrossDestination; }
    public int     getCurrentCrossDurationTicks() { return currentCrossDurationTicks; }
    public String  getCurrentCrossFacing()        { return currentCrossFacing; }
    public String  getCurrentCrossTarget()        { return currentCrossTarget; }

    public double  getCurrentVecX()               { return currentVecX; }
    public double  getCurrentVecY()               { return currentVecY; }
    public double  getCurrentVecZ()               { return currentVecZ; }
    public String  getCurrentVecAudience()        { return currentVecAudience; }

    public String  getCurrentFlightState()             { return currentFlightState; }
    public String  getCurrentFlightReleaseEffect()     { return currentFlightReleaseEffect; }
    public int     getCurrentFlightReleaseDurationTicks(){ return currentFlightReleaseDurationTicks; }
    public String  getCurrentFlightAudience()          { return currentFlightAudience; }

    public String                    getCurrentPhraseAudience() { return currentPhraseAudience; }
    public List<Map<String, Object>> getCurrentPhraseSteps()    { return currentPhraseSteps; }
    public int                       getEditingStepIndex()      { return editingStepIndex; }
    public boolean                   isAutoPreview()            { return cueSession.isAutoPreview(); }

    // -----------------------------------------------------------------------
    // Internal helpers — write to editor
    // -----------------------------------------------------------------------

    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        switch (mode) {
            case EFFECT -> {
                eventMap.put("at", 0); eventMap.put("type", "EFFECT");
                eventMap.put("effect_id", currentEffectId);
                eventMap.put("amplifier", currentEffectAmplifier);
                eventMap.put("duration_ticks", currentEffectDurationTicks);
                eventMap.put("hide_particles", currentEffectHideParticles);
                eventMap.put("audience", currentEffectAudience);
            }
            case PARTICLE -> {
                eventMap.put("at", 0); eventMap.put("type", "PARTICLE");
                eventMap.put("particle_id", currentParticleId);
                eventMap.put("count", currentParticleCount);
                eventMap.put("offset", List.of(currentParticleOffX, currentParticleOffY, currentParticleOffZ));
                eventMap.put("extra", currentParticleExtra);
                eventMap.put("force", currentParticleForce);
                if (currentParticleDurationTicks > 0) {
                    eventMap.put("duration_ticks", currentParticleDurationTicks);
                    eventMap.put("interval_ticks", currentParticleIntervalTicks);
                }
            }
            case PLAYER_TELEPORT -> {
                eventMap.put("at", 0); eventMap.put("type", "PLAYER_TELEPORT");
                eventMap.put("audience", currentTeleportAudience);
                if (currentTeleportUseOffset) {
                    Map<String, Object> off = new LinkedHashMap<>();
                    off.put("x", currentTeleportOffX); off.put("y", currentTeleportOffY); off.put("z", currentTeleportOffZ);
                    eventMap.put("offset", off);
                } else {
                    if (!currentTeleportDestination.isEmpty())
                        eventMap.put("destination", currentTeleportDestination);
                }
                eventMap.put("yaw", currentTeleportYaw);
                eventMap.put("pitch", currentTeleportPitch);
            }
            case CROSS_TO -> {
                eventMap.put("at", 0); eventMap.put("type", "CROSS_TO");
                eventMap.put("target", currentCrossTarget);
                eventMap.put("destination", currentCrossDestination);
                eventMap.put("duration_ticks", currentCrossDurationTicks);
                if (currentCrossFacing != null) eventMap.put("facing", currentCrossFacing);
            }
            case PLAYER_VELOCITY -> {
                eventMap.put("at", 0); eventMap.put("type", "PLAYER_VELOCITY");
                eventMap.put("audience", currentVecAudience);
                Map<String, Object> vec = new LinkedHashMap<>();
                vec.put("x", currentVecX); vec.put("y", currentVecY); vec.put("z", currentVecZ);
                eventMap.put("vector", vec);
            }
            case PLAYER_FLIGHT -> {
                eventMap.put("at", 0); eventMap.put("type", "PLAYER_FLIGHT");
                eventMap.put("audience", currentFlightAudience);
                eventMap.put("state", currentFlightState);
                if ("release".equals(currentFlightState)) {
                    eventMap.put("release_effect", currentFlightReleaseEffect);
                    eventMap.put("release_duration_ticks", currentFlightReleaseDurationTicks);
                }
            }
            case EFFECT_PATTERN -> {
                eventMap.put("at", 0); eventMap.put("type", "EFFECT_PATTERN");
                eventMap.put("effect_id", currentEffectId);
                eventMap.put("amplifier", currentEffectAmplifier);
                eventMap.put("duration_ticks", currentEffectDurationTicks);
                eventMap.put("cycle_ticks", currentPatternCycleTicks);
                eventMap.put("total_duration", currentPatternTotalDuration);
                eventMap.put("hide_particles", currentEffectHideParticles);
                eventMap.put("audience", currentEffectAudience);
            }
            case EFFECT_PHRASE -> {
                eventMap.put("at", 0); eventMap.put("type", "PHRASE");
                eventMap.put("audience", currentPhraseAudience);
                List<Map<String, Object>> steps = new ArrayList<>();
                for (Map<String, Object> step : currentPhraseSteps) steps.add(new LinkedHashMap<>(step));
                eventMap.put("steps", steps);
            }
        }
        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[EffectsEdit] Saved cue override for " + cueId + " → mode=" + mode);
    }

    // -----------------------------------------------------------------------
    // Auto-preview helpers
    // -----------------------------------------------------------------------

    private void autoPreviewEffect() {
        if (!cueSession.isAutoPreview()) return;
        if (mode == EventMode.EFFECT_PATTERN) firePreviewPattern();
        else firePreviewEffect();
    }

    private void autoPreviewParticle() {
        if (cueSession.isAutoPreview()) firePreviewParticle();
    }

    private void firePreviewEffect() {
        PotionEffectType pet = resolvePotionEffectType(currentEffectId);
        if (pet == null) { sendError("Unknown effect_id: " + currentEffectId); return; }
        player.addPotionEffect(new PotionEffect(
            pet, currentEffectDurationTicks, currentEffectAmplifier,
            false, !currentEffectHideParticles));
    }

    private void firePreviewParticle() {
        try {
            String name = currentParticleId.startsWith("minecraft:")
                ? currentParticleId.substring("minecraft:".length())
                : currentParticleId;
            // Convert to Particle key via NamespacedKey
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(name.toLowerCase());
            org.bukkit.Particle particle = org.bukkit.Registry.PARTICLE_TYPE.get(key);
            if (particle == null) { sendError("Unknown particle_id: " + currentParticleId); return; }
            player.getWorld().spawnParticle(particle, player.getLocation(),
                currentParticleCount,
                currentParticleOffX, currentParticleOffY, currentParticleOffZ,
                currentParticleExtra, null, currentParticleForce);
        } catch (Exception e) {
            sendError("Cannot spawn particle: " + currentParticleId);
        }
    }

    private void firePreviewPattern() {
        cancelPatternPreview();
        PotionEffectType pet = resolvePotionEffectType(currentEffectId);
        if (pet == null) { sendError("Unknown effect_id: " + currentEffectId); return; }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ScaenaShows");
        if (plugin == null) { log.warning("[EffectsEdit] Cannot find ScaenaShows plugin for pattern preview"); return; }

        final PotionEffectType effectType   = pet;
        final int              amp          = currentEffectAmplifier;
        final int              pulseDur     = currentEffectDurationTicks;
        final boolean          hide         = currentEffectHideParticles;
        final int              cycleTicks   = currentPatternCycleTicks;
        final int              previewTicks = Math.min(currentPatternTotalDuration, 100);

        patternPreviewTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                if (!player.isOnline() || elapsed > previewTicks) {
                    cancel();
                    patternPreviewTask = null;
                    return;
                }
                if (cycleTicks > 0 && elapsed % cycleTicks == 0) {
                    player.addPotionEffect(new PotionEffect(
                        effectType, pulseDur, amp, false, !hide));
                }
                elapsed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void firePreviewTeleport() {
        // Teleport preview fires directly on the designer
        // (Movement is disruptive — explicit [▶ Preview] only, not auto-preview)
        if (currentTeleportUseOffset) {
            org.bukkit.Location loc = player.getLocation().clone();
            loc.add(currentTeleportOffX, currentTeleportOffY, currentTeleportOffZ);
            loc.setYaw(currentTeleportYaw);
            loc.setPitch(currentTeleportPitch);
            player.teleport(loc);
        } else {
            // Destination is a show mark reference — can't resolve in Phase 2 without show context.
            // Show a hint instead of silently doing nothing.
            player.sendMessage(MM.deserialize(
                "<yellow>[Effects] Teleport destination: <white>"
                + (currentTeleportDestination.isEmpty() ? "(not set)" : currentTeleportDestination)
                + "</white>. Set and mark must resolve at runtime.</yellow>"));
        }
    }

    private void firePreviewCross() {
        player.sendMessage(MM.deserialize(
            "<yellow>[Effects] CROSS_TO destination: <white>"
            + (currentCrossDestination.isEmpty() ? "(not set)" : currentCrossDestination)
            + "</white> over <white>" + currentCrossDurationTicks + "t</white>."
            + " Executes at runtime against the mark.</yellow>"));
    }

    private void firePreviewVelocity() {
        player.setVelocity(new org.bukkit.util.Vector(currentVecX, currentVecY, currentVecZ));
    }

    private void firePreviewFlight() {
        if ("hover".equals(currentFlightState)) {
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            // release — apply the release effect then drop back to survival flight state
            PotionEffectType pet = resolvePotionEffectType(currentFlightReleaseEffect);
            if (pet != null && currentFlightReleaseDurationTicks > 0) {
                player.addPotionEffect(new PotionEffect(
                    pet, currentFlightReleaseDurationTicks, 0, false, false));
            }
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    private void firePreviewPhrase() {
        // Preview: apply each EFFECT step in order with a 1-tick delay between steps
        // (simplified — no at/after timing; just shows the sequence of effects)
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ScaenaShows");
        if (plugin == null) return;
        for (Map<String, Object> step : currentPhraseSteps) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> events = (List<Map<String, Object>>)
                step.getOrDefault("events", List.of());
            for (Map<String, Object> ev : events) {
                String type = (String) ev.getOrDefault("type", "");
                if ("EFFECT".equals(type)) {
                    PotionEffectType pet = resolvePotionEffectType((String) ev.getOrDefault("effect_id", "levitation"));
                    if (pet != null) {
                        player.addPotionEffect(new PotionEffect(
                            pet,
                            intFromMap(ev, "duration_ticks", 40),
                            intFromMap(ev, "amplifier", 0),
                            false,
                            !(Boolean) ev.getOrDefault("hide_particles", true)));
                    }
                } else if ("PARTICLE".equals(type)) {
                    try {
                        String pId = (String) ev.getOrDefault("particle_id", "minecraft:end_rod");
                        String pName = pId.startsWith("minecraft:") ? pId.substring("minecraft:".length()) : pId;
                        org.bukkit.Particle p = org.bukkit.Registry.PARTICLE_TYPE.get(org.bukkit.NamespacedKey.minecraft(pName.toLowerCase()));
                        if (p != null) {
                            player.getWorld().spawnParticle(p, player.getLocation(),
                                intFromMap(ev, "count", 10), 1.0, 1.0, 1.0, 0.0, null, true);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void cancelPatternPreview() {
        if (patternPreviewTask != null) {
            patternPreviewTask.cancel();
            patternPreviewTask = null;
        }
    }

    // -----------------------------------------------------------------------
    // Event finder
    // -----------------------------------------------------------------------

    private ShowEvent findEffectsEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof EffectEvent
                || event instanceof ParticleEvent
                || event instanceof PlayerTeleportEvent
                || event instanceof CrossToEvent
                || event instanceof PlayerVelocityEvent
                || event instanceof PlayerFlightEvent
                || event instanceof PhraseEvent) {
                return event;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Override map reader (for EFFECT_PATTERN detection)
    // -----------------------------------------------------------------------

    private Map<String, Object> getOverrideEventMap(String id) {
        return editor.getCueOverrideFirstEvent(id);
    }

    // -----------------------------------------------------------------------
    // Phrase step helpers
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateStepEvent(Map<String, Object> step) {
        Object evs = step.get("events");
        List<Map<String, Object>> events;
        if (evs instanceof List<?> list && !list.isEmpty()) {
            events = (List<Map<String, Object>>) list;
        } else {
            events = new ArrayList<>();
            Map<String, Object> def = new LinkedHashMap<>();
            def.put("type", "EFFECT"); def.put("effect_id", "levitation");
            def.put("amplifier", 0); def.put("duration_ticks", 40); def.put("hide_particles", true);
            events.add(def);
            step.put("events", events);
        }
        return events.get(0);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> deepCopySteps(List<Map<String, Object>> steps) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> step : steps) {
            Map<String, Object> stepCopy = new LinkedHashMap<>(step);
            Object evs = step.get("events");
            if (evs instanceof List<?> list) {
                List<Map<String, Object>> evCopy = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> m) evCopy.add(new LinkedHashMap<>((Map<String, Object>) m));
                }
                stepCopy.put("events", evCopy);
            }
            copy.add(stepCopy);
        }
        return copy;
    }

    // -----------------------------------------------------------------------
    // Amplifier descriptor label (package-accessible for EffectsPanelBuilder)
    // -----------------------------------------------------------------------

    static String amplifierLabel(int amp) {
        return switch (amp) {
            case 0 -> "subtle";
            case 1 -> "pronounced";
            case 9 -> "surge (arrival)";
            default -> "";
        };
    }

    /** KB-derived y-velocity descriptor label. */
    static String yVecLabel(double y) {
        if (y < 0.1) return "";
        if (y < 0.8) return "gentle lift";
        if (y < 1.6) return "clear push";
        if (y < 2.5) return "dramatic launch";
        return "extreme";
    }

    // -----------------------------------------------------------------------
    // Numeric helpers
    // -----------------------------------------------------------------------

    private static double roundDouble(double v, int places) {
        double scale = Math.pow(10, places);
        return Math.round(v * scale) / scale;
    }

    private static int parseIndex(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private static int intFromMap(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }

    private static double dblFromMap(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.doubleValue() : def;
    }

    private static String strFromMap(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return (v instanceof String s) ? s : def;
    }

    private static boolean boolFromMap(Map<String, Object> m, String key, boolean def) {
        Object v = m.get(key);
        return (v instanceof Boolean b) ? b : def;
    }

    @SuppressWarnings("deprecation")
    private static PotionEffectType resolvePotionEffectType(String id) {
        if (id == null || id.isBlank()) return null;
        // Strip minecraft: prefix if present
        String name = id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
        return PotionEffectType.getByName(name.toUpperCase());
    }

    private void sendError(String msg) {
        player.sendMessage(MM.deserialize("<red>" + msg + "</red>"));
    }
}
