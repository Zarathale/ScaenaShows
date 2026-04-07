package com.scaena.shows.tech;

import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Set (Oz).
 *
 * Wraps the Phase 1 block diff capture infrastructure
 * (SetBuildSession / BlockBuildListener / SetBuildWriter) into the Phase 2
 * DeptEditSession interface. Does NOT rewrite any Phase 1 classes.
 *
 * Entry: switches player to Survival, activates BlockBuildListener by enabling
 * build mode on the owning Phase 1 TechSession. Player builds freely in-world.
 *
 * Exit (Save / Save as Preset):
 *   1. Captures net block diff from the Phase 1 SetBuildSession
 *   2. Writes a dated set-build file via SetBuildWriter
 *   3. Converts diff to BLOCK_PLACE / BLOCK_REMOVE events and writes to
 *      the cue via ShowYamlEditor.setCueEvents()
 *   4. Stop-motion flash: briefly restores world to entry state (~1s),
 *      then re-applies the new state
 *   5. Prints confirmation; deactivates build mode; restores game mode
 *
 * Exit (Cancel): restores world to entry state; no YAML write.
 *
 * Slug enforcement: if the cue ID has no trailing slug (empty last segment),
 * TechManager blocks entry and prompts the player before this class is
 * constructed.
 *
 * Spec: kb/system/phase2-department-panels.md §Set
 */
public final class SetEditSession implements DeptEditSession {

    private static final TextColor COL_OK   = TextColor.color(0x55FF55); // green
    private static final TextColor COL_WARN = TextColor.color(0xFFAA00); // amber

    // ---- Identity ----
    private final String         cueId;
    private final Player         player;
    private final TechCueSession cueSession;
    private final ShowYamlEditor editor;
    @SuppressWarnings("unused")
    private final CueRegistry    cueRegistry; // reserved; consistent with other dept sessions
    private final TechManager    techManager;
    private final JavaPlugin     plugin;
    private final Logger         log;

    // ---- Phase 1 session (for BlockBuildListener routing) ----
    private final TechSession    phase1Session;

    // ---- Entry state snapshot ----
    private final GameMode       entryGameMode;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public SetEditSession(
        String         cueId,
        Player         player,
        TechCueSession cueSession,
        ShowYamlEditor editor,
        CueRegistry    cueRegistry,
        TechManager    techManager,
        JavaPlugin     plugin,
        Logger         log
    ) {
        this.cueId        = cueId;
        this.player       = player;
        this.cueSession   = cueSession;
        this.editor       = editor;
        this.cueRegistry  = cueRegistry;
        this.techManager  = techManager;
        this.plugin       = plugin;
        this.log          = log;

        this.entryGameMode = player.getGameMode();
        this.phase1Session = techManager.getSession(player);

        // Switch to Survival so the player can place/break blocks
        player.setGameMode(GameMode.SURVIVAL);

        // Activate Phase 1 build mode so BlockBuildListener starts tracking
        if (phase1Session != null) {
            String sceneId = cueSession.getCurrentSceneId();
            phase1Session.setBuildMode(true);
            phase1Session.setActiveBuildSession(
                new SetBuildSession(sceneId != null ? sceneId : "unknown")
            );
        } else {
            log.warning("[SetEditSession] No Phase 1 TechSession found for "
                + player.getName() + " — block tracking unavailable.");
        }
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "set"; }

    @Override
    public void onSave() {
        commitSession(false);
    }

    @Override
    public void onSaveAsPreset() {
        commitSession(true);
    }

    @Override
    public void onCancel() {
        restoreWorldToEntryState();
        deactivateBuildMode();
        player.setGameMode(entryGameMode);
    }

    // -----------------------------------------------------------------------
    // Internal — save flow
    // -----------------------------------------------------------------------

    private void commitSession(boolean asPreset) {
        SetBuildSession buildSession = phase1Session != null
            ? phase1Session.activeBuildSession() : null;

        if (buildSession == null || buildSession.isEmpty()) {
            deactivateBuildMode();
            player.setGameMode(entryGameMode);
            player.sendMessage(Component.text(
                "No block changes recorded — nothing to save.", COL_WARN));
            return;
        }

        // 1. Write dated set-build file
        File buildDir = new File(
            plugin.getDataFolder(),
            "set_builds/" + cueSession.getShowId() + "/"
        );
        SetBuildWriter writer = new SetBuildWriter(log);
        String stem = writer.write(buildDir, cueSession.getShowId(), buildSession);

        // 2. Build BLOCK_PLACE / BLOCK_REMOVE event list and push to the cue
        List<Map<String, Object>> events = buildBlockEventList(buildSession);
        editor.setCueEvents(cueId, events);
        cueSession.markCueDirty(cueId);

        // 3. Save as preset if requested
        if (asPreset) {
            String slug    = extractSlug(cueId);
            String sceneId = cueSession.getCurrentSceneId();
            String presetId = "set." + (sceneId != null ? sceneId : "scene") + "." + slug;
            editor.saveAsPreset(cueId, presetId);
        }

        // 4. Deactivate build mode before the flash so the player can't
        //    accidentally interact with blocks during the stop-motion review
        deactivateBuildMode();
        player.setGameMode(entryGameMode);

        // 5. Stop-motion flash — snapshot changes BEFORE deactivation above clears them
        List<SetBuildSession.BlockChange> snapshot = List.copyOf(buildSession.netChanges());
        int changeCount = snapshot.size();

        // Apply "before" state (entry state) — player sees the world as it was
        for (SetBuildSession.BlockChange c : snapshot) {
            c.world().getBlockAt(c.x(), c.y(), c.z()).setBlockData(c.before(), false);
        }

        // After ~1s, restore the new state and send confirmation
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (SetBuildSession.BlockChange c : snapshot) {
                c.world().getBlockAt(c.x(), c.y(), c.z()).setBlockData(c.after(), false);
            }
            String label = asPreset ? "Preset saved" : "Cue saved";
            String buildNote = stem != null ? "  [build: " + stem + "]" : "";
            player.sendMessage(Component.text(
                label + " — " + changeCount + " block change(s) written to "
                + cueId + "." + buildNote,
                COL_OK
            ));
        }, 20L);
    }

    // -----------------------------------------------------------------------
    // Internal — world restore
    // -----------------------------------------------------------------------

    /**
     * Restores all blocks changed during this session to their pre-session state.
     * Called on Cancel only — onSave handles restoration via the flash sequence.
     */
    private void restoreWorldToEntryState() {
        if (phase1Session == null) return;
        SetBuildSession buildSession = phase1Session.activeBuildSession();
        if (buildSession == null) return;
        for (SetBuildSession.BlockChange c : buildSession.netChanges()) {
            c.world().getBlockAt(c.x(), c.y(), c.z()).setBlockData(c.before(), false);
        }
    }

    private void deactivateBuildMode() {
        if (phase1Session != null) {
            phase1Session.setBuildMode(false);
            phase1Session.clearBuildSession();
        }
    }

    // -----------------------------------------------------------------------
    // Internal — YAML event generation
    // -----------------------------------------------------------------------

    /**
     * Convert SetBuildSession net changes to BLOCK_PLACE / BLOCK_REMOVE event maps
     * suitable for ShowYamlEditor.setCueEvents().
     *
     * Additions and replacements → BLOCK_PLACE (carries full blockstate string).
     * Removals (after == AIR)    → BLOCK_REMOVE.
     * Both fire at tick 0 so all changes apply atomically at show cue time.
     */
    private static List<Map<String, Object>> buildBlockEventList(SetBuildSession session) {
        List<Map<String, Object>> events = new ArrayList<>();
        for (SetBuildSession.BlockChange c : session.netChanges()) {
            Map<String, Object> event  = new LinkedHashMap<>();
            Map<String, Integer> target = new LinkedHashMap<>();
            target.put("x", c.x());
            target.put("y", c.y());
            target.put("z", c.z());

            if (c.after().getMaterial() == Material.AIR) {
                // Block removed — BLOCK_REMOVE
                event.put("type", "BLOCK_REMOVE");
                event.put("at", 0);
                event.put("world_specific", true);
                event.put("target", target);
            } else {
                // Block placed or type changed — BLOCK_PLACE
                event.put("type", "BLOCK_PLACE");
                event.put("at", 0);
                event.put("world_specific", true);
                event.put("target", target);
                event.put("block", c.after().getAsString());
            }
            events.add(event);
        }
        return events;
    }

    // -----------------------------------------------------------------------
    // Accessors (for SetPanelBuilder)
    // -----------------------------------------------------------------------

    /** Current net change count tracked in this session (0 if Phase 1 session unavailable). */
    public int getCurrentChangeCount() {
        if (phase1Session == null) return 0;
        SetBuildSession buildSession = phase1Session.activeBuildSession();
        return buildSession != null ? buildSession.totalCount() : 0;
    }

    public String getSceneId() {
        return cueSession.getCurrentSceneId();
    }

    public String getSlug() {
        return extractSlug(cueId);
    }

    // -----------------------------------------------------------------------
    // Static helpers
    // -----------------------------------------------------------------------

    /** Extract the trailing slug segment from a dot-separated cue ID. */
    static String extractSlug(String cueId) {
        int dot = cueId.lastIndexOf('.');
        return (dot >= 0 && dot < cueId.length() - 1)
            ? cueId.substring(dot + 1) : "";
    }
}
