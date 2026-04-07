package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.EntityMgmtEvents.SpawnEntityEvent;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Casting.
 *
 * Supports editing the SPAWN_ENTITY event in a cue:
 *   entity_type  — selectable from a curated panel list
 *   name         — display name / show registration name
 *   baby         — boolean toggle
 *
 * Live swap: if a preview show is active and the entity is already spawned,
 * param changes immediately despawn and respawn the entity at the same location.
 *
 * Territory: Casting owns entity_type, name, and baby.
 * Equipment lives in Wardrobe (a separate edit session).
 *
 * Spec: kb/system/phase2-department-panels.md §Casting
 */
public final class CastingEditSession implements DeptEditSession {

    /** Curated entity type list shown in the type-selector sub-panel. */
    public static final List<String> ENTITY_TYPE_LIST = List.of(
        // Figures of Loyalty and Companionship
        "WOLF", "ALLAY", "CAT", "BEE",
        // Figures of Weight and Authority
        "IRON_GOLEM", "VILLAGER", "WARDEN",
        // Figures of the Uncanny and Strange
        "ENDERMAN", "PHANTOM", "VEX", "BAT", "ELDER_GUARDIAN", "STRIDER",
        // Figures of Consequence and Tension
        "CREEPER", "FOX", "SLIME", "MAGMA_CUBE",
        // Non-mob figures
        "ARMOR_STAND",
        // Common stage actors
        "ZOMBIE", "SKELETON", "HUSK", "STRAY", "DROWNED",
        "SPIDER", "CAVE_SPIDER", "WITCH",
        "BLAZE", "GHAST", "PIGLIN", "HOGLIN",
        "COW", "SHEEP", "PIG", "CHICKEN", "RABBIT"
    );

    // ---- Identity ----
    private final String         cueId;
    private final Player         player;
    private final TechCueSession cueSession;
    private final ShowYamlEditor editor;
    private final CueRegistry    cueRegistry;
    private final Logger         log;

    // ---- Entry snapshot (for Cancel restoration) ----
    private final String  entryEntityType;
    private final String  entryName;
    private final boolean entryBaby;
    private final double  entryOffsetX;
    private final double  entryOffsetY;
    private final double  entryOffsetZ;

    // ---- Current mutable state ----
    private String  currentEntityType;
    private String  currentName;
    private boolean currentBaby;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Build a CastingEditSession for the given cue.
     * Reads initial state from the CueRegistry.
     * If the cue has no SPAWN_ENTITY event, defaults are used.
     */
    public CastingEditSession(
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

        // Read initial state from the cue registry
        SpawnEntityEvent spawnEvent = findSpawnEvent(cueId);
        if (spawnEvent != null) {
            entryEntityType = spawnEvent.entityType;
            entryName       = spawnEvent.name;
            entryBaby       = spawnEvent.baby;
            entryOffsetX    = spawnEvent.offsetX;
            entryOffsetY    = spawnEvent.offsetY;
            entryOffsetZ    = spawnEvent.offsetZ;
        } else {
            entryEntityType = "VILLAGER";
            entryName       = "";
            entryBaby       = false;
            entryOffsetX    = 0;
            entryOffsetY    = 0;
            entryOffsetZ    = 0;
        }

        currentEntityType = entryEntityType;
        currentName       = entryName;
        currentBaby       = entryBaby;
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "casting"; }

    @Override
    public void onSave() {
        writeToEditor();
    }

    @Override
    public void onSaveAsPreset() {
        writeToEditor();
        // Preset ID: casting.[entity_type_lower].[slug]
        String slug = cueId.contains(".")
            ? cueId.substring(cueId.lastIndexOf('.') + 1)
            : cueId;
        String presetId = "casting." + currentEntityType.toLowerCase() + "." + slug;
        editor.saveAsPreset(cueId, presetId);
    }

    @Override
    public void onCancel() {
        // Restore entry state via live swap (don't touch the rawYaml — nothing was saved)
        liveSwap(entryEntityType, entryName, entryBaby);
    }

    /**
     * Handle a param change from /scaena tech2 editparam <key> <value>.
     *
     * Supported keys: entity_type, name, baby
     */
    @Override
    public boolean onEditParam(String key, String value) {
        switch (key.toLowerCase()) {
            case "entity_type" -> {
                String upperType = value.toUpperCase();
                try { EntityType.valueOf(upperType); }
                catch (IllegalArgumentException e) {
                    player.sendMessage(
                        net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Unknown entity type: " + value + "</red>"));
                    return true;
                }
                currentEntityType = upperType;
                liveSwap(currentEntityType, currentName, currentBaby);
                CastingPanelBuilder.sendPanel(player, this);
                return true;
            }
            case "name" -> {
                currentName = value.trim();
                liveSwap(currentEntityType, currentName, currentBaby);
                CastingPanelBuilder.sendPanel(player, this);
                return true;
            }
            case "baby" -> {
                currentBaby = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
                liveSwap(currentEntityType, currentName, currentBaby);
                CastingPanelBuilder.sendPanel(player, this);
                return true;
            }
            default -> { return false; }
        }
    }

    // -----------------------------------------------------------------------
    // Accessors (for CastingPanelBuilder)
    // -----------------------------------------------------------------------

    public String  getCurrentEntityType() { return currentEntityType; }
    public String  getCurrentName()       { return currentName; }
    public boolean isCurrentBaby()        { return currentBaby; }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Write current state as a SPAWN_ENTITY event map into the editor override. */
    private void writeToEditor() {
        Map<String, Object> eventMap = new LinkedHashMap<>();
        eventMap.put("at", 0);
        eventMap.put("type", "SPAWN_ENTITY");
        eventMap.put("entity_type", currentEntityType);
        if (!currentName.isEmpty()) eventMap.put("name", currentName);
        if (currentBaby) eventMap.put("baby", true);
        // Preserve original offset
        Map<String, Object> offset = new LinkedHashMap<>();
        offset.put("x", entryOffsetX);
        offset.put("y", entryOffsetY);
        offset.put("z", entryOffsetZ);
        eventMap.put("offset", offset);
        eventMap.put("despawn_on_end", true);

        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[CastingEdit] Saved cue override for " + cueId
            + " → entity_type=" + currentEntityType + ", name=" + currentName);
    }

    /**
     * Live swap: if the preview is active and the entity is spawned, despawn it
     * and respawn with the new configuration at the same location.
     * No-op if the preview is not active or the entity hasn't been spawned yet.
     */
    private void liveSwap(String entityType, String displayName, boolean baby) {
        if (!cueSession.isPreviewActive()) return;
        RunningShow previewShow = cueSession.getPreviewShow();
        if (previewShow == null) return;

        // Look up by entryName — that's the key used at spawn time
        String lookupName = entryName.isEmpty() ? currentName : entryName;
        if (lookupName.isEmpty()) return;

        Entity existing = previewShow.getSpawnedEntity(lookupName);
        Location spawnLoc = (existing != null) ? existing.getLocation().clone() : null;
        if (existing != null) existing.remove();

        if (spawnLoc == null) return;

        EntityType type;
        try { type = EntityType.valueOf(entityType.toUpperCase()); }
        catch (IllegalArgumentException e) { return; }

        Entity newEntity = spawnLoc.getWorld().spawnEntity(spawnLoc, type);

        String nameToShow = displayName.isEmpty() ? lookupName : displayName;
        newEntity.setCustomName(nameToShow);
        newEntity.setCustomNameVisible(true);

        if (baby && newEntity instanceof Ageable ageable) {
            ageable.setBaby();
        }

        // Register under the original name so subsequent targeting still works
        previewShow.registerSpawnedEntity(lookupName, newEntity);
    }

    /** Find the first SPAWN_ENTITY event in the named cue from the registry. */
    private SpawnEntityEvent findSpawnEvent(String cueId) {
        Cue cue = cueRegistry.get(cueId);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            if (event instanceof SpawnEntityEvent spawn) return spawn;
        }
        return null;
    }
}
