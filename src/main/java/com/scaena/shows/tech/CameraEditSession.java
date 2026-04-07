package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.CameraStateEvents.*;
import com.scaena.shows.model.event.PlayerEvents.*;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.StageEvents.FaceEvent;
import com.scaena.shows.model.event.TextEvents.PhraseEvent;
import com.scaena.shows.model.event.VisualEvents.CameraEvent;
import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Camera.
 *
 * Nine instruments handled:
 *
 *   FACE             — look_at picker: show-relative shortcuts, scene marks, spawned entities
 *   CAMERA           — effect selector (sway/blackout/flash/float); auto-preview
 *   CAMERA_LOCK      — ON/OFF toggle
 *   MOVEMENT_LOCK    — ON/OFF toggle
 *   BOUNDARY_CHECK   — center mark picker + radius tuner
 *   VIEW_CHECK       — target picker + tolerance tuner + pan config
 *   PLAYER_SPECTATE  — spawn/entity mode; audience; duration shortcut
 *   PLAYER_SPECTATE_END — destination picker; audience
 *   PLAYER_MOUNT     — spawn/entity mode; audience; duration shortcut
 *   PLAYER_DISMOUNT  — audience only
 *   CAMERA_PHRASE    — PHRASE container; Camera-event step editor (Phase 2 stub)
 *
 * Preset naming:
 *   CAMERA:      camera.effect.[slug]
 *   BOUNDARY_CHECK: camera.boundary.[slug]
 *   VIEW_CHECK:  camera.view.[slug]
 *   FACE:        No preset per spec.
 *   CAMERA_LOCK / MOVEMENT_LOCK / PLAYER_*: No preset.
 *   CAMERA_PHRASE: camera.phrase.[direction].[slug]
 *
 * Stop-safety for CAMERA_LOCK / MOVEMENT_LOCK is implicit — RunningShow is discarded
 * at show end, taking the flags with it.
 *
 * Spec: kb/system/phase2-department-panels.md §Camera
 */
public final class CameraEditSession implements DeptEditSession {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // -----------------------------------------------------------------------
    // Curated option lists (package-accessible for CameraPanelBuilder)
    // -----------------------------------------------------------------------

    static final List<String> EFFECTS          = List.of("sway", "blackout", "flash", "float");
    static final List<String> LOCK_STATES      = List.of("ON", "OFF");
    static final List<String> VIEW_INTERPS     = List.of("EASE_OUT", "EASE_IN", "LINEAR");
    static final List<String> FACE_SHORTCUTS   = List.of("mark:stage_center", "compass:south",
                                                          "compass:north", "compass:east", "compass:west");
    static final List<String> AUDIENCES        = List.of("participants", "private",
                                                          "group_1", "group_2", "invoker");
    static final List<String> SPECTATE_DEST    = List.of("restore", "mark:", "entity:spawned:");

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode {
        FACE, CAMERA,
        CAMERA_LOCK, MOVEMENT_LOCK,
        BOUNDARY_CHECK, VIEW_CHECK,
        PLAYER_SPECTATE, PLAYER_SPECTATE_END,
        PLAYER_MOUNT, PLAYER_DISMOUNT,
        CAMERA_PHRASE
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
    // Mode — mutable (type-switch on empty/stub cues)
    // -----------------------------------------------------------------------

    private EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel) — ALL final
    // Unused modes use sensible defaults; definite-assignment required by Java.
    // -----------------------------------------------------------------------

    // FACE
    private final String  entryFaceLookAt;

    // CAMERA
    private final String  entryEffect;
    private final int     entryIntensity;
    private final int     entryCamDuration;
    private final String  entryCamAudience;

    // CAMERA_LOCK / MOVEMENT_LOCK (shared field — mode identifies which)
    private final String  entryLockState;

    // BOUNDARY_CHECK
    private final String  entryBoundaryCenter;
    private final double  entryBoundaryRadius;

    // VIEW_CHECK
    private final String  entryViewTarget;
    private final int     entryViewTolerance;
    private final int     entryViewOutDuration;
    private final String  entryViewOutInterp;

    // PLAYER_SPECTATE
    private final String  entrySpectateEntityMode;   // "spawn" | "entity"
    private final String  entrySpectateEntity;
    private final String  entrySpectateSpawnName;
    private final String  entrySpectateSpawnType;
    private final double  entrySpectateOffX;
    private final double  entrySpectateOffY;
    private final double  entrySpectateOffZ;
    private final boolean entrySpectateDespawnOnEnd;
    private final String  entrySpectateAudience;
    private final int     entrySpectateDurationTicks;

    // PLAYER_SPECTATE_END
    private final String  entrySpectateEndDestination;
    private final String  entrySpectateEndAudience;

    // PLAYER_MOUNT
    private final String  entryMountEntityMode;
    private final String  entryMountEntity;
    private final String  entryMountSpawnName;
    private final String  entryMountSpawnType;
    private final double  entryMountOffX;
    private final double  entryMountOffY;
    private final double  entryMountOffZ;
    private final boolean entryMountInvisible;
    private final boolean entryMountDespawnOnDismount;
    private final String  entryMountAudience;
    private final int     entryMountDurationTicks;

    // PLAYER_DISMOUNT
    private final String  entryDismountAudience;

    // CAMERA_PHRASE — stub; no editable fields in this session
    // (raw YAML is displayed read-only in the panel)

    // -----------------------------------------------------------------------
    // Mutable current state
    // -----------------------------------------------------------------------

    private String  currentFaceLookAt;

    private String  currentEffect;
    private int     currentIntensity;
    private int     currentCamDuration;
    private String  currentCamAudience;

    private String  currentLockState;

    private String  currentBoundaryCenter;
    private double  currentBoundaryRadius;

    private String  currentViewTarget;
    private int     currentViewTolerance;
    private int     currentViewOutDuration;
    private String  currentViewOutInterp;

    private String  currentSpectateEntityMode;
    private String  currentSpectateEntity;
    private String  currentSpectateSpawnName;
    private String  currentSpectateSpawnType;
    private double  currentSpectateOffX;
    private double  currentSpectateOffY;
    private double  currentSpectateOffZ;
    private boolean currentSpectateDespawnOnEnd;
    private String  currentSpectateAudience;
    private int     currentSpectateDurationTicks;

    private String  currentSpectateEndDestination;
    private String  currentSpectateEndAudience;

    private String  currentMountEntityMode;
    private String  currentMountEntity;
    private String  currentMountSpawnName;
    private String  currentMountSpawnType;
    private double  currentMountOffX;
    private double  currentMountOffY;
    private double  currentMountOffZ;
    private boolean currentMountInvisible;
    private boolean currentMountDespawnOnDismount;
    private String  currentMountAudience;
    private int     currentMountDurationTicks;

    private String  currentDismountAudience;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public CameraEditSession(
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

        ShowEvent found = findCameraEvent(cueId);

        // ------------------------------------------------------------------
        // Detect mode and assign ALL final fields in every branch.
        // ------------------------------------------------------------------

        if (found instanceof FaceEvent fe) {
            mode = EventMode.FACE;
            entryFaceLookAt            = fe.lookAt != null ? fe.lookAt : "mark:stage_center";
            // unused
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof CameraEvent ce) {
            mode = EventMode.CAMERA;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = ce.effect != null ? ce.effect : "sway";
            entryIntensity = ce.intensity; entryCamDuration = ce.durationTicks;
            entryCamAudience = ce.audience != null ? ce.audience : "participants";
            // unused
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof CameraLockEvent cle) {
            mode = EventMode.CAMERA_LOCK;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = cle.state;
            // unused
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof MovementLockEvent mle) {
            mode = EventMode.MOVEMENT_LOCK;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = mle.state;
            // unused
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof BoundaryCheckEvent bce) {
            mode = EventMode.BOUNDARY_CHECK;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = bce.center;
            entryBoundaryRadius = bce.radius;
            // unused
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof ViewCheckEvent vce) {
            mode = EventMode.VIEW_CHECK;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = vce.target;
            entryViewTolerance = vce.tolerance;
            entryViewOutDuration = vce.outOfViewDurationTicks;
            entryViewOutInterp = vce.outOfViewInterpolation;
            // unused
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof PlayerSpectateEvent pse) {
            mode = EventMode.PLAYER_SPECTATE;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = pse.spawnMode ? "spawn" : "entity";
            entrySpectateEntity = pse.entity;
            entrySpectateSpawnName = pse.spawnName;
            entrySpectateSpawnType = pse.spawnType;
            entrySpectateOffX = pse.spawnOffX;
            entrySpectateOffY = pse.spawnOffY;
            entrySpectateOffZ = pse.spawnOffZ;
            entrySpectateDespawnOnEnd = pse.despawnOnEnd;
            entrySpectateAudience = pse.audience;
            entrySpectateDurationTicks = pse.durationTicks;
            // unused
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof PlayerSpectateEndEvent psee) {
            mode = EventMode.PLAYER_SPECTATE_END;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = psee.destination;
            entrySpectateEndAudience = psee.audience;
            // unused
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else if (found instanceof PlayerMountEvent pme) {
            mode = EventMode.PLAYER_MOUNT;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = pme.spawnMode ? "spawn" : "entity";
            entryMountEntity = pme.entity;
            entryMountSpawnName = pme.spawnName;
            entryMountSpawnType = pme.spawnType;
            entryMountOffX = pme.spawnOffX;
            entryMountOffY = pme.spawnOffY;
            entryMountOffZ = pme.spawnOffZ;
            entryMountInvisible = pme.spawnInvisible;
            entryMountDespawnOnDismount = pme.despawnOnDismount;
            entryMountAudience = pme.audience;
            entryMountDurationTicks = pme.durationTicks;
            entryDismountAudience = "participants";

        } else if (found instanceof PlayerDismountEvent pde) {
            mode = EventMode.PLAYER_DISMOUNT;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = pde.audience;

        } else if (found instanceof PhraseEvent) {
            mode = EventMode.CAMERA_PHRASE;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "entity"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "entity"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";

        } else {
            // Default for empty/stub cues — start in FACE mode
            mode = EventMode.FACE;
            entryFaceLookAt = "mark:stage_center";
            entryEffect = "sway"; entryIntensity = 1; entryCamDuration = 40;
            entryCamAudience = "participants";
            entryLockState = "ON";
            entryBoundaryCenter = "mark:stage_center"; entryBoundaryRadius = 10.0;
            entryViewTarget = "mark:center"; entryViewTolerance = 30;
            entryViewOutDuration = 20; entryViewOutInterp = "EASE_OUT";
            entrySpectateEntityMode = "spawn"; entrySpectateEntity = "";
            entrySpectateSpawnName = "CinematicCamera"; entrySpectateSpawnType = "ARMOR_STAND";
            entrySpectateOffX = 10; entrySpectateOffY = 5; entrySpectateOffZ = 0;
            entrySpectateDespawnOnEnd = true; entrySpectateAudience = "participants";
            entrySpectateDurationTicks = -1;
            entrySpectateEndDestination = "restore"; entrySpectateEndAudience = "participants";
            entryMountEntityMode = "spawn"; entryMountEntity = "";
            entryMountSpawnName = "GuideEntity"; entryMountSpawnType = "HORSE";
            entryMountOffX = 0; entryMountOffY = 0; entryMountOffZ = 0;
            entryMountInvisible = false; entryMountDespawnOnDismount = true;
            entryMountAudience = "participants"; entryMountDurationTicks = -1;
            entryDismountAudience = "participants";
        }

        // ---- Initialise current state from entry snapshots ----
        currentFaceLookAt             = entryFaceLookAt;
        currentEffect                 = entryEffect;
        currentIntensity              = entryIntensity;
        currentCamDuration            = entryCamDuration;
        currentCamAudience            = entryCamAudience;
        currentLockState              = entryLockState;
        currentBoundaryCenter         = entryBoundaryCenter;
        currentBoundaryRadius         = entryBoundaryRadius;
        currentViewTarget             = entryViewTarget;
        currentViewTolerance          = entryViewTolerance;
        currentViewOutDuration        = entryViewOutDuration;
        currentViewOutInterp          = entryViewOutInterp;
        currentSpectateEntityMode     = entrySpectateEntityMode;
        currentSpectateEntity         = entrySpectateEntity;
        currentSpectateSpawnName      = entrySpectateSpawnName;
        currentSpectateSpawnType      = entrySpectateSpawnType;
        currentSpectateOffX           = entrySpectateOffX;
        currentSpectateOffY           = entrySpectateOffY;
        currentSpectateOffZ           = entrySpectateOffZ;
        currentSpectateDespawnOnEnd   = entrySpectateDespawnOnEnd;
        currentSpectateAudience       = entrySpectateAudience;
        currentSpectateDurationTicks  = entrySpectateDurationTicks;
        currentSpectateEndDestination = entrySpectateEndDestination;
        currentSpectateEndAudience    = entrySpectateEndAudience;
        currentMountEntityMode        = entryMountEntityMode;
        currentMountEntity            = entryMountEntity;
        currentMountSpawnName         = entryMountSpawnName;
        currentMountSpawnType         = entryMountSpawnType;
        currentMountOffX              = entryMountOffX;
        currentMountOffY              = entryMountOffY;
        currentMountOffZ              = entryMountOffZ;
        currentMountInvisible         = entryMountInvisible;
        currentMountDespawnOnDismount = entryMountDespawnOnDismount;
        currentMountAudience          = entryMountAudience;
        currentMountDurationTicks     = entryMountDurationTicks;
        currentDismountAudience       = entryDismountAudience;
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "camera"; }

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
            case CAMERA          -> editor.saveAsPreset(cueId, "camera.effect." + slug);
            case BOUNDARY_CHECK  -> editor.saveAsPreset(cueId, "camera.boundary." + slug);
            case VIEW_CHECK      -> editor.saveAsPreset(cueId, "camera.view." + slug);
            case CAMERA_PHRASE   -> editor.saveAsPreset(cueId, "camera.phrase." + slug);
            default -> { /* No presets for FACE, locks, spectate, mount per spec */ }
        }
    }

    @Override
    public void onCancel() {
        currentFaceLookAt             = entryFaceLookAt;
        currentEffect                 = entryEffect;
        currentIntensity              = entryIntensity;
        currentCamDuration            = entryCamDuration;
        currentCamAudience            = entryCamAudience;
        currentLockState              = entryLockState;
        currentBoundaryCenter         = entryBoundaryCenter;
        currentBoundaryRadius         = entryBoundaryRadius;
        currentViewTarget             = entryViewTarget;
        currentViewTolerance          = entryViewTolerance;
        currentViewOutDuration        = entryViewOutDuration;
        currentViewOutInterp          = entryViewOutInterp;
        currentSpectateEntityMode     = entrySpectateEntityMode;
        currentSpectateEntity         = entrySpectateEntity;
        currentSpectateSpawnName      = entrySpectateSpawnName;
        currentSpectateSpawnType      = entrySpectateSpawnType;
        currentSpectateOffX           = entrySpectateOffX;
        currentSpectateOffY           = entrySpectateOffY;
        currentSpectateOffZ           = entrySpectateOffZ;
        currentSpectateDespawnOnEnd   = entrySpectateDespawnOnEnd;
        currentSpectateAudience       = entrySpectateAudience;
        currentSpectateDurationTicks  = entrySpectateDurationTicks;
        currentSpectateEndDestination = entrySpectateEndDestination;
        currentSpectateEndAudience    = entrySpectateEndAudience;
        currentMountEntityMode        = entryMountEntityMode;
        currentMountEntity            = entryMountEntity;
        currentMountSpawnName         = entryMountSpawnName;
        currentMountSpawnType         = entryMountSpawnType;
        currentMountOffX              = entryMountOffX;
        currentMountOffY              = entryMountOffY;
        currentMountOffZ              = entryMountOffZ;
        currentMountInvisible         = entryMountInvisible;
        currentMountDespawnOnDismount = entryMountDespawnOnDismount;
        currentMountAudience          = entryMountAudience;
        currentMountDurationTicks     = entryMountDurationTicks;
        currentDismountAudience       = entryDismountAudience;
    }

    /**
     * Handle a /scaena tech2 editparam <key> [value...] command.
     *
     * === FACE keys ===
     *   look_at <value>           — set look_at target
     *   preview_face              — apply FACE orientation directly to player
     *
     * === CAMERA keys ===
     *   effect <value>            — set effect: sway|blackout|flash|float
     *   intensity_up              — +1 (max 3)
     *   intensity_down            — -1 (min 0)
     *   dur_up / dur_down         — ±20t
     *   dur_shift_up / dur_shift_down — ±100t
     *   audience <value>          — set audience
     *   preview_camera            — fire effect on player
     *   auto_preview_toggle       — toggle session auto-preview flag
     *
     * === CAMERA_LOCK / MOVEMENT_LOCK keys ===
     *   lock_state <ON|OFF>       — set state
     *
     * === BOUNDARY_CHECK keys ===
     *   center <value>            — set center expression (mark:Name)
     *   radius_up                 — +1 block
     *   radius_down               — -1 block (min 1)
     *   radius_shift_up           — +5 blocks
     *   radius_shift_down         — -5 blocks
     *
     * === VIEW_CHECK keys ===
     *   target <value>            — set target (mark:Name | entity:spawned:Name)
     *   tolerance_up              — +5 degrees
     *   tolerance_down            — -5 degrees
     *   tolerance_shift_up        — +15 degrees
     *   tolerance_shift_down      — -15 degrees
     *   out_dur_up / out_dur_down — ±10t pan duration
     *   interp <value>            — EASE_OUT|EASE_IN|LINEAR
     *
     * === PLAYER_SPECTATE / PLAYER_MOUNT keys ===
     *   entity_mode <spawn|entity>
     *   entity_name <value>       — entity: mode target
     *   spawn_name <value>
     *   spawn_type <value>        — EntityType name
     *   off_x_up/down/shift_up/shift_down
     *   off_y_up/down/shift_up/shift_down
     *   off_z_up/down/shift_up/shift_down
     *   despawn_toggle            — flip despawn_on_end / despawn_on_dismount
     *   invisible_toggle          — flip spawnInvisible (MOUNT only)
     *   audience <value>
     *   dur_up / dur_down         — ±20t shortcut duration (-1 = manual)
     *   dur_clear                 — reset to -1 (manual placement)
     *
     * === PLAYER_SPECTATE_END keys ===
     *   destination <value>       — restore|mark:Name|entity:spawned:Name
     *   audience <value>
     *
     * === PLAYER_DISMOUNT keys ===
     *   audience <value>
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        switch (mode) {
            case FACE -> {
                switch (lower) {
                    case "look_at" -> {
                        currentFaceLookAt = value;
                        CameraPanelBuilder.sendPanel(player, this);
                        if (cueSession.isAutoPreview()) applyFacePreview();
                        return true;
                    }
                    case "preview_face" -> { applyFacePreview(); return true; }
                    default -> { return false; }
                }
            }

            case CAMERA -> {
                switch (lower) {
                    case "effect" -> {
                        if (!EFFECTS.contains(value.toLowerCase())) {
                            sendError("Unknown effect: " + value + ". Valid: " + String.join(", ", EFFECTS));
                            return true;
                        }
                        currentEffect = value.toLowerCase();
                        if (cueSession.isAutoPreview()) applyCamera();
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "intensity_up"   -> { currentIntensity = Math.min(currentIntensity + 1, 3); updateCamera(); return true; }
                    case "intensity_down" -> { currentIntensity = Math.max(currentIntensity - 1, 0); updateCamera(); return true; }
                    case "dur_up"         -> { currentCamDuration = Math.min(currentCamDuration + 20, 24000); updateCamera(); return true; }
                    case "dur_down"       -> { currentCamDuration = Math.max(currentCamDuration - 20, 1); updateCamera(); return true; }
                    case "dur_shift_up"   -> { currentCamDuration = Math.min(currentCamDuration + 100, 24000); updateCamera(); return true; }
                    case "dur_shift_down" -> { currentCamDuration = Math.max(currentCamDuration - 100, 1); updateCamera(); return true; }
                    case "audience" -> {
                        currentCamAudience = value;
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "preview_camera" -> { applyCamera(); return true; }
                    case "auto_preview_toggle" -> {
                        cueSession.setAutoPreview(!cueSession.isAutoPreview());
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    default -> { return false; }
                }
            }

            case CAMERA_LOCK, MOVEMENT_LOCK -> {
                if (lower.equals("lock_state")) {
                    String upper = value.toUpperCase();
                    if (!LOCK_STATES.contains(upper)) {
                        sendError("Unknown state: " + value + ". Valid: ON, OFF");
                        return true;
                    }
                    currentLockState = upper;
                    CameraPanelBuilder.sendPanel(player, this);
                    return true;
                }
                return false;
            }

            case BOUNDARY_CHECK -> {
                switch (lower) {
                    case "center" -> {
                        currentBoundaryCenter = value;
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "radius_up"         -> { currentBoundaryRadius = Math.min(currentBoundaryRadius + 1, 1000); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "radius_down"       -> { currentBoundaryRadius = Math.max(currentBoundaryRadius - 1, 1); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "radius_shift_up"   -> { currentBoundaryRadius = Math.min(currentBoundaryRadius + 5, 1000); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "radius_shift_down" -> { currentBoundaryRadius = Math.max(currentBoundaryRadius - 5, 1); CameraPanelBuilder.sendPanel(player, this); return true; }
                    default -> { return false; }
                }
            }

            case VIEW_CHECK -> {
                switch (lower) {
                    case "target" -> {
                        currentViewTarget = value;
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "tolerance_up"         -> { currentViewTolerance = Math.min(currentViewTolerance + 5, 180); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "tolerance_down"       -> { currentViewTolerance = Math.max(currentViewTolerance - 5, 1); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "tolerance_shift_up"   -> { currentViewTolerance = Math.min(currentViewTolerance + 15, 180); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "tolerance_shift_down" -> { currentViewTolerance = Math.max(currentViewTolerance - 15, 1); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "out_dur_up"    -> { currentViewOutDuration = Math.min(currentViewOutDuration + 10, 400); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "out_dur_down"  -> { currentViewOutDuration = Math.max(currentViewOutDuration - 10, 1); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "interp" -> {
                        String upper = value.toUpperCase();
                        if (!VIEW_INTERPS.contains(upper)) {
                            sendError("Unknown interpolation: " + value + ". Valid: " + String.join(", ", VIEW_INTERPS));
                            return true;
                        }
                        currentViewOutInterp = upper;
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    default -> { return false; }
                }
            }

            case PLAYER_SPECTATE -> {
                switch (lower) {
                    case "entity_mode" -> {
                        currentSpectateEntityMode = value.toLowerCase().equals("spawn") ? "spawn" : "entity";
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "entity_name"   -> { currentSpectateEntity = value;         CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "spawn_name"    -> { currentSpectateSpawnName = value;       CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "spawn_type"    -> { currentSpectateSpawnType = value.toUpperCase(); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_up"     -> { currentSpectateOffX += 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_down"   -> { currentSpectateOffX -= 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_shift_up"   -> { currentSpectateOffX += 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_shift_down" -> { currentSpectateOffX -= 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_up"     -> { currentSpectateOffY += 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_down"   -> { currentSpectateOffY -= 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_shift_up"   -> { currentSpectateOffY += 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_shift_down" -> { currentSpectateOffY -= 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_up"     -> { currentSpectateOffZ += 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_down"   -> { currentSpectateOffZ -= 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_shift_up"   -> { currentSpectateOffZ += 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_shift_down" -> { currentSpectateOffZ -= 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "despawn_toggle" -> { currentSpectateDespawnOnEnd = !currentSpectateDespawnOnEnd; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "audience"      -> { currentSpectateAudience = value; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "dur_up"        -> { currentSpectateDurationTicks = (currentSpectateDurationTicks < 0 ? 20 : Math.min(currentSpectateDurationTicks + 20, 24000)); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "dur_down"      -> {
                        if (currentSpectateDurationTicks > 20) currentSpectateDurationTicks -= 20;
                        else if (currentSpectateDurationTicks > 0) currentSpectateDurationTicks = -1;
                        CameraPanelBuilder.sendPanel(player, this); return true;
                    }
                    case "dur_clear"     -> { currentSpectateDurationTicks = -1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    default -> { return false; }
                }
            }

            case PLAYER_SPECTATE_END -> {
                switch (lower) {
                    case "destination" -> { currentSpectateEndDestination = value; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "audience"    -> { currentSpectateEndAudience = value; CameraPanelBuilder.sendPanel(player, this); return true; }
                    default -> { return false; }
                }
            }

            case PLAYER_MOUNT -> {
                switch (lower) {
                    case "entity_mode" -> {
                        currentMountEntityMode = value.toLowerCase().equals("spawn") ? "spawn" : "entity";
                        CameraPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                    case "entity_name"   -> { currentMountEntity = value;              CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "spawn_name"    -> { currentMountSpawnName = value;           CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "spawn_type"    -> { currentMountSpawnType = value.toUpperCase(); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_up"     -> { currentMountOffX += 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_down"   -> { currentMountOffX -= 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_shift_up"   -> { currentMountOffX += 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_x_shift_down" -> { currentMountOffX -= 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_up"     -> { currentMountOffY += 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_down"   -> { currentMountOffY -= 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_shift_up"   -> { currentMountOffY += 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_y_shift_down" -> { currentMountOffY -= 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_up"     -> { currentMountOffZ += 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_down"   -> { currentMountOffZ -= 1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_shift_up"   -> { currentMountOffZ += 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "off_z_shift_down" -> { currentMountOffZ -= 5; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "invisible_toggle" -> { currentMountInvisible = !currentMountInvisible; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "despawn_toggle"   -> { currentMountDespawnOnDismount = !currentMountDespawnOnDismount; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "audience"         -> { currentMountAudience = value; CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "dur_up"    -> { currentMountDurationTicks = (currentMountDurationTicks < 0 ? 20 : Math.min(currentMountDurationTicks + 20, 24000)); CameraPanelBuilder.sendPanel(player, this); return true; }
                    case "dur_down"  -> {
                        if (currentMountDurationTicks > 20) currentMountDurationTicks -= 20;
                        else if (currentMountDurationTicks > 0) currentMountDurationTicks = -1;
                        CameraPanelBuilder.sendPanel(player, this); return true;
                    }
                    case "dur_clear" -> { currentMountDurationTicks = -1; CameraPanelBuilder.sendPanel(player, this); return true; }
                    default -> { return false; }
                }
            }

            case PLAYER_DISMOUNT -> {
                if (lower.equals("audience")) {
                    currentDismountAudience = value;
                    CameraPanelBuilder.sendPanel(player, this);
                    return true;
                }
                return false;
            }

            case CAMERA_PHRASE -> {
                // Stub: CAMERA_PHRASE is read-only in this release
                return false;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Accessors (for CameraPanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode()                      { return mode; }
    public String    getCurrentFaceLookAt()         { return currentFaceLookAt; }
    public String    getCurrentEffect()             { return currentEffect; }
    public int       getCurrentIntensity()          { return currentIntensity; }
    public int       getCurrentCamDuration()        { return currentCamDuration; }
    public String    getCurrentCamAudience()        { return currentCamAudience; }
    public String    getCurrentLockState()          { return currentLockState; }
    public String    getCurrentBoundaryCenter()     { return currentBoundaryCenter; }
    public double    getCurrentBoundaryRadius()     { return currentBoundaryRadius; }
    public String    getCurrentViewTarget()         { return currentViewTarget; }
    public int       getCurrentViewTolerance()      { return currentViewTolerance; }
    public int       getCurrentViewOutDuration()    { return currentViewOutDuration; }
    public String    getCurrentViewOutInterp()      { return currentViewOutInterp; }
    public String    getCurrentSpectateEntityMode() { return currentSpectateEntityMode; }
    public String    getCurrentSpectateEntity()     { return currentSpectateEntity; }
    public String    getCurrentSpectateSpawnName()  { return currentSpectateSpawnName; }
    public String    getCurrentSpectateSpawnType()  { return currentSpectateSpawnType; }
    public double    getCurrentSpectateOffX()       { return currentSpectateOffX; }
    public double    getCurrentSpectateOffY()       { return currentSpectateOffY; }
    public double    getCurrentSpectateOffZ()       { return currentSpectateOffZ; }
    public boolean   isCurrentSpectateDespawnOnEnd(){ return currentSpectateDespawnOnEnd; }
    public String    getCurrentSpectateAudience()   { return currentSpectateAudience; }
    public int       getCurrentSpectateDurationTicks(){ return currentSpectateDurationTicks; }
    public String    getCurrentSpectateEndDestination(){ return currentSpectateEndDestination; }
    public String    getCurrentSpectateEndAudience(){ return currentSpectateEndAudience; }
    public String    getCurrentMountEntityMode()    { return currentMountEntityMode; }
    public String    getCurrentMountEntity()        { return currentMountEntity; }
    public String    getCurrentMountSpawnName()     { return currentMountSpawnName; }
    public String    getCurrentMountSpawnType()     { return currentMountSpawnType; }
    public double    getCurrentMountOffX()          { return currentMountOffX; }
    public double    getCurrentMountOffY()          { return currentMountOffY; }
    public double    getCurrentMountOffZ()          { return currentMountOffZ; }
    public boolean   isCurrentMountInvisible()      { return currentMountInvisible; }
    public boolean   isCurrentMountDespawnOnDismount(){ return currentMountDespawnOnDismount; }
    public String    getCurrentMountAudience()      { return currentMountAudience; }
    public int       getCurrentMountDurationTicks() { return currentMountDurationTicks; }
    public String    getCurrentDismountAudience()   { return currentDismountAudience; }
    public boolean   isAutoPreview()                { return cueSession.isAutoPreview(); }

    // -----------------------------------------------------------------------
    // Preview helpers
    // -----------------------------------------------------------------------

    /** Apply FACE orientation directly to the player (Camera is inherently live-swap). */
    private void applyFacePreview() {
        // Face the player toward the current look_at target
        String lookAt = currentFaceLookAt;
        if (lookAt == null || lookAt.isEmpty()) return;

        Location playerLoc = player.getLocation();
        if (lookAt.startsWith("compass:")) {
            String dir = lookAt.substring(8).toLowerCase();
            float yaw = switch (dir) {
                case "south" -> 0f;
                case "west"  -> 90f;
                case "north" -> 180f;
                case "east"  -> 270f;
                default      -> playerLoc.getYaw();
            };
            playerLoc.setYaw(yaw);
            player.teleport(playerLoc);
        }
        // mark: and entity:spawned: targets require show runtime data — skip in preview
    }

    /** Apply CAMERA potion effect directly to the player. */
    private void applyCamera() {
        PotionEffectType effectType = switch (currentEffect) {
            case "blackout" -> PotionEffectType.DARKNESS;
            case "flash"    -> PotionEffectType.BLINDNESS;
            case "float"    -> PotionEffectType.LEVITATION;
            default         -> PotionEffectType.NAUSEA; // sway
        };
        player.addPotionEffect(
            new PotionEffect(effectType, currentCamDuration, currentIntensity, false, true));
        if ("float".equals(currentEffect)) {
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW_FALLING, currentCamDuration, 0, false, false));
        }
    }

    /** Called after intensity/duration param changes — auto-preview if enabled. */
    private void updateCamera() {
        if (cueSession.isAutoPreview()) applyCamera();
        CameraPanelBuilder.sendPanel(player, this);
    }

    // -----------------------------------------------------------------------
    // Write to editor
    // -----------------------------------------------------------------------

    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        switch (mode) {
            case FACE -> {
                eventMap.put("at", 0);
                eventMap.put("type", "FACE");
                eventMap.put("target", "player");
                eventMap.put("look_at", currentFaceLookAt);
            }
            case CAMERA -> {
                eventMap.put("at", 0);
                eventMap.put("type", "CAMERA");
                eventMap.put("effect", currentEffect);
                eventMap.put("intensity", currentIntensity);
                eventMap.put("duration_ticks", currentCamDuration);
                eventMap.put("audience", currentCamAudience);
            }
            case CAMERA_LOCK -> {
                eventMap.put("at", 0);
                eventMap.put("type", "CAMERA_LOCK");
                eventMap.put("state", currentLockState);
            }
            case MOVEMENT_LOCK -> {
                eventMap.put("at", 0);
                eventMap.put("type", "MOVEMENT_LOCK");
                eventMap.put("state", currentLockState);
            }
            case BOUNDARY_CHECK -> {
                eventMap.put("at", 0);
                eventMap.put("type", "BOUNDARY_CHECK");
                eventMap.put("center", currentBoundaryCenter);
                eventMap.put("radius", currentBoundaryRadius);
                // out_of_range is authored via manual YAML for now — only center/radius are editable
            }
            case VIEW_CHECK -> {
                eventMap.put("at", 0);
                eventMap.put("type", "VIEW_CHECK");
                eventMap.put("target", currentViewTarget);
                eventMap.put("tolerance", currentViewTolerance);
                Map<String, Object> oov = new LinkedHashMap<>();
                oov.put("duration_ticks", currentViewOutDuration);
                oov.put("interpolation", currentViewOutInterp);
                eventMap.put("out_of_view", oov);
            }
            case PLAYER_SPECTATE -> {
                eventMap.put("at", 0);
                eventMap.put("type", "PLAYER_SPECTATE");
                eventMap.put("audience", currentSpectateAudience);
                if (currentSpectateDurationTicks > 0) {
                    eventMap.put("duration_ticks", currentSpectateDurationTicks);
                }
                if ("spawn".equals(currentSpectateEntityMode)) {
                    Map<String, Object> spawn = new LinkedHashMap<>();
                    spawn.put("name", currentSpectateSpawnName);
                    spawn.put("type", currentSpectateSpawnType);
                    Map<String, Object> off = new LinkedHashMap<>();
                    off.put("x", currentSpectateOffX);
                    off.put("y", currentSpectateOffY);
                    off.put("z", currentSpectateOffZ);
                    spawn.put("offset", off);
                    spawn.put("despawn_on_end", currentSpectateDespawnOnEnd);
                    eventMap.put("spawn", spawn);
                } else {
                    eventMap.put("entity", currentSpectateEntity);
                }
            }
            case PLAYER_SPECTATE_END -> {
                eventMap.put("at", 0);
                eventMap.put("type", "PLAYER_SPECTATE_END");
                eventMap.put("destination", currentSpectateEndDestination);
                eventMap.put("audience", currentSpectateEndAudience);
            }
            case PLAYER_MOUNT -> {
                eventMap.put("at", 0);
                eventMap.put("type", "PLAYER_MOUNT");
                eventMap.put("audience", currentMountAudience);
                if (currentMountDurationTicks > 0) {
                    eventMap.put("duration_ticks", currentMountDurationTicks);
                }
                if ("spawn".equals(currentMountEntityMode)) {
                    Map<String, Object> spawn = new LinkedHashMap<>();
                    spawn.put("name", currentMountSpawnName);
                    spawn.put("type", currentMountSpawnType);
                    Map<String, Object> off = new LinkedHashMap<>();
                    off.put("x", currentMountOffX);
                    off.put("y", currentMountOffY);
                    off.put("z", currentMountOffZ);
                    spawn.put("offset", off);
                    spawn.put("invisible", currentMountInvisible);
                    spawn.put("despawn_on_dismount", currentMountDespawnOnDismount);
                    eventMap.put("spawn", spawn);
                } else {
                    eventMap.put("entity", currentMountEntity);
                }
            }
            case PLAYER_DISMOUNT -> {
                eventMap.put("at", 0);
                eventMap.put("type", "PLAYER_DISMOUNT");
                eventMap.put("audience", currentDismountAudience);
            }
            case CAMERA_PHRASE -> {
                // CAMERA_PHRASE is read-only in this release; preserve existing YAML unchanged.
                // Don't call setCueEvents — leave the cue as-is.
                cueSession.markCueDirty(cueId);
                log.info("[CameraEdit] CAMERA_PHRASE save: preserving existing YAML for " + cueId);
                return;
            }
        }
        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[CameraEdit] Saved cue override for " + cueId + " → mode=" + mode);
    }

    // -----------------------------------------------------------------------
    // Cue event lookup
    // -----------------------------------------------------------------------

    /** Find the first camera-relevant event in the cue (scans all known camera types). */
    private ShowEvent findCameraEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof FaceEvent
                    || event instanceof CameraEvent
                    || event instanceof CameraLockEvent
                    || event instanceof MovementLockEvent
                    || event instanceof BoundaryCheckEvent
                    || event instanceof ViewCheckEvent
                    || event instanceof PlayerSpectateEvent
                    || event instanceof PlayerSpectateEndEvent
                    || event instanceof PlayerMountEvent
                    || event instanceof PlayerDismountEvent
                    || event instanceof PhraseEvent) {
                return event;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void sendError(String msg) {
        player.sendMessage(MM.deserialize("<red>" + msg + "</red>"));
    }

    /** Format duration ticks as "Nt (Xs)". */
    static String formatDuration(int ticks) {
        if (ticks < 0) return "manual";
        int seconds = ticks / 20;
        int rem = ticks % 20;
        String secStr = rem == 0 ? seconds + "s" : String.format("%.1fs", ticks / 20.0);
        return ticks + "t (" + secStr + ")";
    }
}
