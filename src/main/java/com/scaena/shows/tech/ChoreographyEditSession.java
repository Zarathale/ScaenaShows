package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.EntityBehaviorEvents.EntityAiEvent;
import com.scaena.shows.model.event.EntityBehaviorEvents.EntityVelocityEvent;
import com.scaena.shows.model.event.EntityMgmtEvents.DespawnEntityEvent;
import com.scaena.shows.model.event.EntityMgmtEvents.SpawnEntityEvent;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.StageEvents.CrossToEvent;
import com.scaena.shows.model.event.StageEvents.EnterEvent;
import com.scaena.shows.model.event.StageEvents.ExitEvent;
import com.scaena.shows.model.event.StageEvents.FaceEvent;
import com.scaena.shows.model.event.TextEvents.PhraseEvent;
import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Choreography.
 *
 * Seven panel modes (detected from first event type in choreo.* cues):
 *
 *   ENTRANCE          — SPAWN_ENTITY (Appear) or ENTER (Arrive from wing)
 *   CHARACTER_EXIT    — DESPAWN_ENTITY (Vanish) or EXIT (path to wing + despawn)
 *   CHARACTER_CROSS   — CROSS_TO; sub-mode Instant / AI; anchor field; preset
 *   CHARACTER_LOOK    — FACE (entity target); look_at picker; pitch warning
 *   PERFORMER_STATE   — ENTITY_AI toggle (Puppet / Performer)
 *   CHARACTER_VELOCITY — ENTITY_VELOCITY; vector fields; named preset picker
 *   CHOREO_PHRASE     — PHRASE container; step editor stub; preset
 *
 * Preset naming:
 *   CHARACTER_CROSS:      choreo.cross.[mode].[slug]
 *   CHARACTER_VELOCITY:   choreo.velocity.[slug]
 *   CHOREO_PHRASE:        choreo.phrase.[slug]
 *   All others:           No preset per spec.
 *
 * Auto-preview: OFF for CHARACTER_CROSS, CHARACTER_LOOK, CHARACTER_VELOCITY
 * (physically moves / snaps entities — disruptive). [▶ Preview] fires on demand.
 * ENTRANCE and CHARACTER_EXIT write to YAML on each param change (no world action).
 * PERFORMER_STATE and CHOREO_PHRASE have no preview.
 *
 * Spec: kb/system/phase2-department-panels.md §Choreography
 */
public final class ChoreographyEditSession implements DeptEditSession {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // -----------------------------------------------------------------------
    // Curated option lists (package-accessible for ChoreographyPanelBuilder)
    // -----------------------------------------------------------------------

    static final List<String> ENTITY_TYPES = List.of(
        "VILLAGER", "WOLF", "IRON_GOLEM", "VEX", "ALLAY",
        "FOX", "ARMOR_STAND", "PHANTOM", "ENDERMAN", "BAT"
    );

    static final List<String> WING_MARKS = List.of(
        "mark:wing_left", "mark:wing_right", "mark:wing_back", "mark:wing_front"
    );

    static final List<String> STAGE_MARKS = List.of(
        "mark:stage_center", "mark:stage_left", "mark:stage_right",
        "mark:upstage", "mark:downstage", "mark:center"
    );

    static final List<String> SPEED_PRESETS = List.of("Creep", "Slow", "Normal", "Fast", "Sprint");

    static final List<String> VELOCITY_PRESETS = List.of(
        "gentle_bounce", "dramatic_launch", "float_arc"
    );

    static final List<String> LOOK_AT_SHORTCUTS = List.of(
        "mark:stage_center", "player",
        "compass:north", "compass:south", "compass:east", "compass:west"
    );

    static final List<String> ANCHORS = List.of("scene_origin", "player");

    /** AI speed preset → approximate numeric speed value. */
    static double speedFromPreset(String preset) {
        return switch (preset) {
            case "Creep"  -> 0.07;
            case "Slow"   -> 0.25;
            case "Fast"   -> 1.75;
            case "Sprint" -> 2.5;
            default       -> 1.0;   // "Normal"
        };
    }

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode {
        ENTRANCE, CHARACTER_EXIT, CHARACTER_CROSS,
        CHARACTER_LOOK, PERFORMER_STATE, CHARACTER_VELOCITY,
        CHOREO_PHRASE
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
    // Mode — mutable (type-switch on empty/stub cues via mode selector)
    // -----------------------------------------------------------------------

    private EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel) — ALL final; assigned in every constructor branch.
    // -----------------------------------------------------------------------

    // ENTRANCE
    private final String  entryEntranceSubMode;   // "appear" | "arrive"
    // Appear (SPAWN_ENTITY)
    private final String  entryAppearEntityType;
    private final String  entryAppearMark;
    private final String  entryAppearName;
    private final boolean entryAppearBaby;
    // Arrive (ENTER)
    private final String  entryArriveEntityType;
    private final String  entryArriveFrom;
    private final String  entryArriveDest;
    private final String  entryArriveSpeedPreset;
    private final String  entryArriveName;

    // CHARACTER_EXIT
    private final String  entryExitSubMode;        // "vanish" | "exit_wing"
    private final String  entryExitTarget;
    private final boolean entryExitParticleBurst;
    private final String  entryExitToMark;

    // CHARACTER_CROSS
    private final String  entryCrossTarget;
    private final String  entryCrossMode;           // "instant" | "ai"
    private final String  entryCrossDest;
    private final String  entryCrossSpeedPreset;
    private final String  entryCrossAnchor;

    // CHARACTER_LOOK
    private final String  entryLookTarget;
    private final String  entryLookAt;

    // PERFORMER_STATE
    private final String  entryStateTarget;
    private final boolean entryStateEnabled;

    // CHARACTER_VELOCITY
    private final String  entryVelTarget;
    private final double  entryVelX;
    private final double  entryVelY;
    private final double  entryVelZ;
    private final String  entryVelAnchor;

    // CHOREO_PHRASE — no editable fields in Phase 2 session (stub read-only)

    // -----------------------------------------------------------------------
    // Mutable current state
    // -----------------------------------------------------------------------

    private String  currentEntranceSubMode;
    private String  currentAppearEntityType;
    private String  currentAppearMark;
    private String  currentAppearName;
    private boolean currentAppearBaby;
    private String  currentArriveEntityType;
    private String  currentArriveFrom;
    private String  currentArriveDest;
    private String  currentArriveSpeedPreset;
    private String  currentArriveName;

    private String  currentExitSubMode;
    private String  currentExitTarget;
    private boolean currentExitParticleBurst;
    private String  currentExitToMark;

    private String  currentCrossTarget;
    private String  currentCrossMode;
    private String  currentCrossDest;
    private String  currentCrossSpeedPreset;
    private String  currentCrossAnchor;

    private String  currentLookTarget;
    private String  currentLookAt;

    private String  currentStateTarget;
    private boolean currentStateEnabled;

    private String  currentVelTarget;
    private double  currentVelX;
    private double  currentVelY;
    private double  currentVelZ;
    private String  currentVelAnchor;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public ChoreographyEditSession(
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

        ShowEvent found = findChoreographyEvent(cueId);

        // ------------------------------------------------------------------
        // Detect mode; assign ALL final fields in every branch.
        // Unused modes receive sensible defaults to satisfy definite-assignment.
        // ------------------------------------------------------------------

        if (found instanceof SpawnEntityEvent se) {
            mode = EventMode.ENTRANCE;
            entryEntranceSubMode  = "appear";
            entryAppearEntityType = se.entityType.isEmpty() ? "VILLAGER" : se.entityType;
            entryAppearMark       = "mark:stage_center";
            entryAppearName       = se.name;
            entryAppearBaby       = se.baby;
            // unused arrive
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused exit
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof EnterEvent ee) {
            mode = EventMode.ENTRANCE;
            entryEntranceSubMode  = "arrive";
            entryArriveEntityType = ee.entityType.isEmpty() ? "VILLAGER" : ee.entityType;
            entryArriveFrom       = ee.from.isEmpty() ? "mark:wing_left" : ee.from;
            entryArriveDest       = ee.destination.isEmpty() ? "mark:stage_center" : ee.destination;
            entryArriveSpeedPreset = "Slow";   // ENTER has no speed field; default
            entryArriveName       = ee.name;
            // unused appear
            entryAppearEntityType = "VILLAGER"; entryAppearMark = "mark:stage_center";
            entryAppearName = ""; entryAppearBaby = false;
            // unused exit
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof DespawnEntityEvent de) {
            mode = EventMode.CHARACTER_EXIT;
            entryExitSubMode       = "vanish";
            entryExitTarget        = de.target;
            entryExitParticleBurst = de.particleBurst;
            entryExitToMark        = "mark:wing_right";
            // unused entrance
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof ExitEvent xe) {
            mode = EventMode.CHARACTER_EXIT;
            entryExitSubMode       = "exit_wing";
            entryExitTarget        = xe.target;
            entryExitParticleBurst = false;
            entryExitToMark        = xe.to.isEmpty() ? "mark:wing_right" : xe.to;
            // unused entrance
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof CrossToEvent ce) {
            mode = EventMode.CHARACTER_CROSS;
            entryCrossTarget      = ce.target;
            entryCrossMode        = ce.durationTicks > 0 ? "ai" : "instant";
            entryCrossDest        = ce.destination.isEmpty() ? "mark:stage_center" : ce.destination;
            entryCrossSpeedPreset = "Slow";
            entryCrossAnchor      = "scene_origin";
            // unused entrance
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused exit
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof FaceEvent fe) {
            mode = EventMode.CHARACTER_LOOK;
            entryLookTarget = fe.target;
            entryLookAt     = fe.lookAt != null ? fe.lookAt : "mark:stage_center";
            // unused entrance
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused exit
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof EntityAiEvent ae) {
            mode = EventMode.PERFORMER_STATE;
            entryStateTarget  = ae.target;
            entryStateEnabled = ae.enabled;
            // unused entrance
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused exit
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused velocity
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else if (found instanceof EntityVelocityEvent ve) {
            mode = EventMode.CHARACTER_VELOCITY;
            entryVelTarget = ve.target;
            entryVelX      = ve.vecX;
            entryVelY      = ve.vecY;
            entryVelZ      = ve.vecZ;
            entryVelAnchor = "scene_origin";
            // unused entrance
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            // unused exit
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            // unused cross
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            // unused look
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            // unused state
            entryStateTarget = ""; entryStateEnabled = false;

        } else if (found instanceof PhraseEvent) {
            mode = EventMode.CHOREO_PHRASE;
            // all fields unused — assign defaults
            entryEntranceSubMode = "appear"; entryAppearEntityType = "VILLAGER";
            entryAppearMark = "mark:stage_center"; entryAppearName = ""; entryAppearBaby = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            entryStateTarget = ""; entryStateEnabled = false;
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";

        } else {
            // Default for empty/stub cues — open in ENTRANCE / Appear
            mode = EventMode.ENTRANCE;
            entryEntranceSubMode  = "appear";
            entryAppearEntityType = "VILLAGER";
            entryAppearMark       = "mark:stage_center";
            entryAppearName       = "Performer";
            entryAppearBaby       = false;
            entryArriveEntityType = "VILLAGER"; entryArriveFrom = "mark:wing_left";
            entryArriveDest = "mark:stage_center"; entryArriveSpeedPreset = "Slow";
            entryArriveName = "";
            entryExitSubMode = "vanish"; entryExitTarget = ""; entryExitParticleBurst = false;
            entryExitToMark = "mark:wing_right";
            entryCrossTarget = ""; entryCrossMode = "instant";
            entryCrossDest = "mark:stage_center"; entryCrossSpeedPreset = "Slow";
            entryCrossAnchor = "scene_origin";
            entryLookTarget = ""; entryLookAt = "mark:stage_center";
            entryStateTarget = ""; entryStateEnabled = false;
            entryVelTarget = ""; entryVelX = 0; entryVelY = 0.8; entryVelZ = 0;
            entryVelAnchor = "scene_origin";
        }

        // ---- Initialise current state from entry snapshots ----
        currentEntranceSubMode   = entryEntranceSubMode;
        currentAppearEntityType  = entryAppearEntityType;
        currentAppearMark        = entryAppearMark;
        currentAppearName        = entryAppearName;
        currentAppearBaby        = entryAppearBaby;
        currentArriveEntityType  = entryArriveEntityType;
        currentArriveFrom        = entryArriveFrom;
        currentArriveDest        = entryArriveDest;
        currentArriveSpeedPreset = entryArriveSpeedPreset;
        currentArriveName        = entryArriveName;

        currentExitSubMode       = entryExitSubMode;
        currentExitTarget        = entryExitTarget;
        currentExitParticleBurst = entryExitParticleBurst;
        currentExitToMark        = entryExitToMark;

        currentCrossTarget       = entryCrossTarget;
        currentCrossMode         = entryCrossMode;
        currentCrossDest         = entryCrossDest;
        currentCrossSpeedPreset  = entryCrossSpeedPreset;
        currentCrossAnchor       = entryCrossAnchor;

        currentLookTarget        = entryLookTarget;
        currentLookAt            = entryLookAt;

        currentStateTarget       = entryStateTarget;
        currentStateEnabled      = entryStateEnabled;

        currentVelTarget         = entryVelTarget;
        currentVelX              = entryVelX;
        currentVelY              = entryVelY;
        currentVelZ              = entryVelZ;
        currentVelAnchor         = entryVelAnchor;
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "choreography"; }

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
            case CHARACTER_CROSS    -> editor.saveAsPreset(cueId,
                "choreo.cross." + currentCrossMode + "." + slug);
            case CHARACTER_VELOCITY -> editor.saveAsPreset(cueId,
                "choreo.velocity." + slug);
            case CHOREO_PHRASE      -> editor.saveAsPreset(cueId,
                "choreo.phrase." + slug);
            default -> { /* no preset for ENTRANCE, CHARACTER_EXIT, CHARACTER_LOOK, PERFORMER_STATE */ }
        }
    }

    @Override
    public void onCancel() {
        currentEntranceSubMode   = entryEntranceSubMode;
        currentAppearEntityType  = entryAppearEntityType;
        currentAppearMark        = entryAppearMark;
        currentAppearName        = entryAppearName;
        currentAppearBaby        = entryAppearBaby;
        currentArriveEntityType  = entryArriveEntityType;
        currentArriveFrom        = entryArriveFrom;
        currentArriveDest        = entryArriveDest;
        currentArriveSpeedPreset = entryArriveSpeedPreset;
        currentArriveName        = entryArriveName;

        currentExitSubMode       = entryExitSubMode;
        currentExitTarget        = entryExitTarget;
        currentExitParticleBurst = entryExitParticleBurst;
        currentExitToMark        = entryExitToMark;

        currentCrossTarget       = entryCrossTarget;
        currentCrossMode         = entryCrossMode;
        currentCrossDest         = entryCrossDest;
        currentCrossSpeedPreset  = entryCrossSpeedPreset;
        currentCrossAnchor       = entryCrossAnchor;

        currentLookTarget        = entryLookTarget;
        currentLookAt            = entryLookAt;

        currentStateTarget       = entryStateTarget;
        currentStateEnabled      = entryStateEnabled;

        currentVelTarget         = entryVelTarget;
        currentVelX              = entryVelX;
        currentVelY              = entryVelY;
        currentVelZ              = entryVelZ;
        currentVelAnchor         = entryVelAnchor;
    }

    /**
     * Handle /scaena tech2 editparam <key> [value] commands.
     *
     * === ENTRANCE keys ===
     *   sub_mode <appear|arrive>         — switch Appear / Arrive sub-mode
     *   entity_type <TYPE>               — Appear: set entity type
     *   appear_mark <mark:Name>          — Appear: set spawn mark
     *   appear_name <name>               — Appear: set entity name
     *   appear_baby_toggle               — Appear: toggle baby flag
     *   arrive_entity_type <TYPE>        — Arrive: set entity type
     *   arrive_from <mark:Name>          — Arrive: set wing spawn mark
     *   arrive_dest <mark:Name>          — Arrive: set destination mark
     *   arrive_name <name>               — Arrive: set entity name
     *   arrive_speed <Creep|Slow|Normal|Fast|Sprint>  — Arrive: set speed preset
     *
     * === CHARACTER_EXIT keys ===
     *   exit_sub_mode <vanish|exit_wing> — switch Vanish / Exit-to-Wing sub-mode
     *   exit_target <target>             — set target entity ref
     *   particle_toggle                  — Vanish: toggle particle_burst
     *   exit_to_mark <mark:Name>         — Exit: set wing destination mark
     *
     * === CHARACTER_CROSS keys ===
     *   cross_mode <instant|ai>          — toggle cross mode
     *   cross_target <target>            — set entity target
     *   cross_dest <dest>                — set destination (mark or offset string)
     *   cross_speed <preset>             — AI mode: set speed preset
     *   cross_anchor <scene_origin|player> — set anchor
     *   cross_preview                    — fire preview (log message; no world action)
     *
     * === CHARACTER_LOOK keys ===
     *   look_target <target>             — set entity target
     *   look_at <value>                  — set look_at (mark/entity/player/compass/yaw)
     *   look_preview                     — fire preview (log message)
     *
     * === PERFORMER_STATE keys ===
     *   state_target <target>            — set entity target
     *   state_toggle                     — toggle Puppet (false) / Performer (true)
     *
     * === CHARACTER_VELOCITY keys ===
     *   vel_target <target>              — set entity target
     *   vel_x_up / vel_x_down            — ±0.1
     *   vel_x_shift_up / vel_x_shift_down — ±1.0
     *   vel_y_up / vel_y_down            — ±0.1
     *   vel_y_shift_up / vel_y_shift_down — ±1.0
     *   vel_z_up / vel_z_down            — ±0.1
     *   vel_z_shift_up / vel_z_shift_down — ±1.0
     *   vel_preset <gentle_bounce|dramatic_launch|float_arc>
     *   vel_anchor <scene_origin|player>
     *   vel_preview                      — fire preview (log message)
     *   auto_preview_toggle              — toggle session auto-preview flag
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        switch (mode) {

            case ENTRANCE -> {
                switch (lower) {
                    case "sub_mode" -> {
                        if ("appear".equals(value) || "arrive".equals(value)) {
                            currentEntranceSubMode = value;
                            ChoreographyPanelBuilder.sendPanel(player, this);
                        }
                        return true;
                    }
                    // Appear params
                    case "entity_type"   -> { currentAppearEntityType = value; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "appear_mark"   -> { currentAppearMark = value;       writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "appear_name"   -> { currentAppearName = value;       writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "appear_baby_toggle" -> { currentAppearBaby = !currentAppearBaby; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    // Arrive params
                    case "arrive_entity_type" -> { currentArriveEntityType = value; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "arrive_from"   -> { currentArriveFrom = value;        writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "arrive_dest"   -> { currentArriveDest = value;        writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "arrive_name"   -> { currentArriveName = value;        writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "arrive_speed"  -> { currentArriveSpeedPreset = value; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                }
            }

            case CHARACTER_EXIT -> {
                switch (lower) {
                    case "exit_sub_mode" -> {
                        if ("vanish".equals(value) || "exit_wing".equals(value)) {
                            currentExitSubMode = value;
                            ChoreographyPanelBuilder.sendPanel(player, this);
                        }
                        return true;
                    }
                    case "exit_target"    -> { currentExitTarget = value;        writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "particle_toggle"-> { currentExitParticleBurst = !currentExitParticleBurst; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "exit_to_mark"   -> { currentExitToMark = value;        writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                }
            }

            case CHARACTER_CROSS -> {
                switch (lower) {
                    case "cross_mode"    -> { currentCrossMode = value;         writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "cross_target"  -> { currentCrossTarget = value;       writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "cross_dest"    -> { currentCrossDest = value;         writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "cross_speed"   -> { currentCrossSpeedPreset = value;  writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "cross_anchor"  -> { currentCrossAnchor = value;       writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "cross_preview" -> { fireCrossPreview(); return true; }
                }
            }

            case CHARACTER_LOOK -> {
                switch (lower) {
                    case "look_target"   -> { currentLookTarget = value; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "look_at"       -> { currentLookAt = value;     writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "look_preview"  -> { fireLookPreview(); return true; }
                }
            }

            case PERFORMER_STATE -> {
                switch (lower) {
                    case "state_target"  -> { currentStateTarget = value; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "state_toggle"  -> { currentStateEnabled = !currentStateEnabled; writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                }
            }

            case CHARACTER_VELOCITY -> {
                switch (lower) {
                    case "vel_target"         -> { currentVelTarget = value;              writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_x_up"           -> { currentVelX = round1(currentVelX + 0.1); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_x_down"         -> { currentVelX = round1(currentVelX - 0.1); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_x_shift_up"     -> { currentVelX = round1(currentVelX + 1.0); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_x_shift_down"   -> { currentVelX = round1(currentVelX - 1.0); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_y_up"           -> { currentVelY = round1(currentVelY + 0.1); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_y_down"         -> { currentVelY = round1(currentVelY - 0.1); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_y_shift_up"     -> { currentVelY = round1(currentVelY + 1.0); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_y_shift_down"   -> { currentVelY = round1(currentVelY - 1.0); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_z_up"           -> { currentVelZ = round1(currentVelZ + 0.1); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_z_down"         -> { currentVelZ = round1(currentVelZ - 0.1); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_z_shift_up"     -> { currentVelZ = round1(currentVelZ + 1.0); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_z_shift_down"   -> { currentVelZ = round1(currentVelZ - 1.0); writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_preset"         -> { applyVelocityPreset(value);               writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_anchor"         -> { currentVelAnchor = value;                 writeToEditor(); ChoreographyPanelBuilder.sendPanel(player, this); return true; }
                    case "vel_preview"        -> { fireVelocityPreview(); return true; }
                    case "auto_preview_toggle" -> {
                        cueSession.setAutoPreview(!cueSession.isAutoPreview());
                        ChoreographyPanelBuilder.sendPanel(player, this);
                        return true;
                    }
                }
            }

            default -> { /* CHOREO_PHRASE: no editable params in Phase 2 */ }
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Getters (package-accessible for ChoreographyPanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode()                  { return mode; }
    public String  getCurrentEntranceSubMode()  { return currentEntranceSubMode; }
    public String  getCurrentAppearEntityType() { return currentAppearEntityType; }
    public String  getCurrentAppearMark()       { return currentAppearMark; }
    public String  getCurrentAppearName()       { return currentAppearName; }
    public boolean isCurrentAppearBaby()        { return currentAppearBaby; }
    public String  getCurrentArriveEntityType() { return currentArriveEntityType; }
    public String  getCurrentArriveFrom()       { return currentArriveFrom; }
    public String  getCurrentArriveDest()       { return currentArriveDest; }
    public String  getCurrentArriveSpeedPreset(){ return currentArriveSpeedPreset; }
    public String  getCurrentArriveName()       { return currentArriveName; }
    public String  getCurrentExitSubMode()      { return currentExitSubMode; }
    public String  getCurrentExitTarget()       { return currentExitTarget; }
    public boolean isCurrentExitParticleBurst() { return currentExitParticleBurst; }
    public String  getCurrentExitToMark()       { return currentExitToMark; }
    public String  getCurrentCrossTarget()      { return currentCrossTarget; }
    public String  getCurrentCrossMode()        { return currentCrossMode; }
    public String  getCurrentCrossDest()        { return currentCrossDest; }
    public String  getCurrentCrossSpeedPreset() { return currentCrossSpeedPreset; }
    public String  getCurrentCrossAnchor()      { return currentCrossAnchor; }
    public String  getCurrentLookTarget()       { return currentLookTarget; }
    public String  getCurrentLookAt()           { return currentLookAt; }
    public String  getCurrentStateTarget()      { return currentStateTarget; }
    public boolean isCurrentStateEnabled()      { return currentStateEnabled; }
    public String  getCurrentVelTarget()        { return currentVelTarget; }
    public double  getCurrentVelX()             { return currentVelX; }
    public double  getCurrentVelY()             { return currentVelY; }
    public double  getCurrentVelZ()             { return currentVelZ; }
    public String  getCurrentVelAnchor()        { return currentVelAnchor; }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /** Find the first choreography-relevant event in the named cue. */
    private ShowEvent findChoreographyEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null || cue.events().isEmpty()) return null;
        for (ShowEvent ev : cue.events()) {
            if (ev instanceof SpawnEntityEvent
             || ev instanceof EnterEvent
             || ev instanceof DespawnEntityEvent
             || ev instanceof ExitEvent
             || ev instanceof CrossToEvent
             || ev instanceof FaceEvent
             || ev instanceof EntityAiEvent
             || ev instanceof EntityVelocityEvent
             || ev instanceof PhraseEvent) {
                return ev;
            }
        }
        return null;
    }

    /** Write current state back to the in-memory YAML via ShowYamlEditor. */
    private void writeToEditor() {
        Map<String, Object> ev = new LinkedHashMap<>();
        switch (mode) {

            case ENTRANCE -> {
                if ("appear".equals(currentEntranceSubMode)) {
                    ev.put("type", "SPAWN_ENTITY");
                    ev.put("entity_type", currentAppearEntityType);
                    if (!currentAppearName.isEmpty()) ev.put("name", currentAppearName);
                    ev.put("baby", currentAppearBaby);
                    ev.put("despawn_on_end", true);
                } else {
                    ev.put("type", "ENTER");
                    ev.put("entity_type", currentArriveEntityType);
                    if (!currentArriveName.isEmpty()) ev.put("name", currentArriveName);
                    ev.put("from", currentArriveFrom);
                    ev.put("destination", currentArriveDest);
                    ev.put("baby", false);
                    ev.put("despawn_on_end", true);
                }
            }

            case CHARACTER_EXIT -> {
                if ("vanish".equals(currentExitSubMode)) {
                    ev.put("type", "DESPAWN_ENTITY");
                    ev.put("target", currentExitTarget);
                    ev.put("particle_burst", currentExitParticleBurst);
                } else {
                    ev.put("type", "EXIT");
                    ev.put("target", currentExitTarget);
                    ev.put("to", currentExitToMark);
                    ev.put("despawn_on_arrival", true);
                }
            }

            case CHARACTER_CROSS -> {
                ev.put("type", "CROSS_TO");
                ev.put("target", currentCrossTarget);
                ev.put("destination", currentCrossDest);
                if ("ai".equals(currentCrossMode)) {
                    // Non-zero duration activates the pathfinder cross
                    ev.put("duration_ticks", 40);
                }
                // anchor is stored in session state but CROSS_TO has no anchor field in schema;
                // it is saved for preset naming only
            }

            case CHARACTER_LOOK -> {
                ev.put("type", "FACE");
                ev.put("target", currentLookTarget);
                ev.put("look_at", currentLookAt);
            }

            case PERFORMER_STATE -> {
                ev.put("type", "ENTITY_AI");
                ev.put("target", currentStateTarget);
                ev.put("enabled", currentStateEnabled);
            }

            case CHARACTER_VELOCITY -> {
                ev.put("type", "ENTITY_VELOCITY");
                ev.put("target", currentVelTarget);
                Map<String, Object> vec = new LinkedHashMap<>();
                vec.put("x", currentVelX);
                vec.put("y", currentVelY);
                vec.put("z", currentVelZ);
                ev.put("vector", vec);
            }

            case CHOREO_PHRASE -> { return; }   // stub — raw YAML not mutated
        }
        editor.patchEventParam(cueId, 0, "__replace_event__", ev);
    }

    private void fireCrossPreview() {
        player.sendMessage(MM.deserialize(
            "<gray>[Choreography] Preview: CROSS_TO <white>" + currentCrossTarget
            + "</white> -> <white>" + currentCrossDest
            + "</white> (" + currentCrossMode + ")</gray>"));
    }

    private void fireLookPreview() {
        player.sendMessage(MM.deserialize(
            "<gray>[Choreography] Preview: FACE <white>" + currentLookTarget
            + "</white> look_at <white>" + currentLookAt + "</white></gray>"));
    }

    private void fireVelocityPreview() {
        player.sendMessage(MM.deserialize(
            "<gray>[Choreography] Preview: ENTITY_VELOCITY <white>" + currentVelTarget
            + "</white> {x:" + fmt(currentVelX) + " y:" + fmt(currentVelY)
            + " z:" + fmt(currentVelZ) + "}</gray>"));
    }

    private void applyVelocityPreset(String preset) {
        switch (preset) {
            case "gentle_bounce"   -> { currentVelX = 0.0; currentVelY = 0.4; currentVelZ = 0.0; }
            case "dramatic_launch" -> { currentVelX = 0.0; currentVelY = 1.5; currentVelZ = 0.0; }
            case "float_arc"       -> { currentVelX = 0.3; currentVelY = 1.0; currentVelZ = 0.0; }
        }
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static String fmt(double v) {
        return (v == Math.floor(v) && !Double.isInfinite(v))
            ? String.valueOf((long) v)
            : String.format("%.1f", v);
    }
}
