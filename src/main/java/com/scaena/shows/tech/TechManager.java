package com.scaena.shows.tech;

import com.scaena.shows.model.Show;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.registry.ShowRegistry;
import com.scaena.shows.registry.YamlLoader;
import com.scaena.shows.runtime.RunningShow;
import com.scaena.shows.runtime.ShowManager;
import com.scaena.shows.runtime.ShowScheduler;
import com.scaena.shows.runtime.executor.ExecutorRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages per-player TechSession lifecycle and the four core operations:
 * LOAD, DISMISS, TOGGLE, CAPTURE, SAVE.
 *
 * One session per player. A player cannot have both a ScoutSession and a
 * TechSession simultaneously — the entry command checks and rejects if active.
 *
 * Thread model: all public methods that touch Bukkit API are called on the
 * main server thread. The async chat listener routes through
 * Bukkit.getScheduler().runTask() before calling applyTextParam().
 */
public final class TechManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Hotbar slot indices (0-indexed) reserved by tech mode
    static final int SLOT_PREV    = 4;   // ◀ Prev Scene
    static final int SLOT_HOLD    = 5;   // ⏸ Hold
    static final int SLOT_NEXT    = 6;   // ▶ Next Scene
    static final int SLOT_CAPTURE = 7;   // 📍 Capture
    static final int SLOT_PARAMS  = 8;   // ⚙ Params

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JavaPlugin      plugin;
    private final PromptBookLoader loader;
    private final Logger          log;
    private ShowRegistry          showRegistry;   // optional — set after construction via setShowRegistry()
    private ShowManager           showManager;    // optional — set via setShowManager()
    private CueRegistry           cueRegistry;    // optional — set via setCueRegistry()
    private ExecutorRegistry      executorRegistry; // optional — set via setExecutorRegistry()

    /** Active Phase 1 sessions keyed by player UUID. */
    private final Map<UUID, TechSession>    sessions    = new HashMap<>();
    /** Active Phase 2 sessions keyed by player UUID. */
    private final Map<UUID, TechCueSession> cueSessions = new HashMap<>();

    public TechManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loader = new PromptBookLoader(plugin.getLogger());
        this.log    = plugin.getLogger();
    }

    /** Wire in the ShowRegistry so the dashboard can report timeline cue counts. */
    public void setShowRegistry(ShowRegistry registry) {
        this.showRegistry = registry;
    }

    /** Wire in ShowManager for Phase 2 preview show lifecycle. */
    public void setShowManager(ShowManager manager) {
        this.showManager = manager;
    }

    /** Wire in CueRegistry for Phase 2 stub detection and preview show construction. */
    public void setCueRegistry(CueRegistry registry) {
        this.cueRegistry = registry;
    }

    /** Wire in ExecutorRegistry for Phase 2 preview ShowScheduler. */
    public void setExecutorRegistry(ExecutorRegistry registry) {
        this.executorRegistry = registry;
    }

    // -----------------------------------------------------------------------
    // Session lookup
    // -----------------------------------------------------------------------

    public TechSession getSession(Player player) {
        return player == null ? null : sessions.get(player.getUniqueId());
    }

    public boolean hasSession(Player player) {
        return getSession(player) != null;
    }

    // -----------------------------------------------------------------------
    // sendDashboard — /scaena <showId>  (OPS-031)
    // -----------------------------------------------------------------------

    /**
     * Build and send the Show Status Dashboard for the given showId.
     * Reads the prompt-book, scout captures, and timeline cue count.
     * No active TechSession required — this is a read-only status query.
     */
    public void sendDashboard(Player player, String showId) {
        // Load prompt book
        PromptBook book = loader.load(plugin.getDataFolder(), showId);
        if (book == null) {
            player.sendMessage(MM.deserialize(
                "<red>No prompt book found for <white>" + showId + "</white>. "
                + "Expected: shows/" + showId + "/" + showId + ".prompt-book.yml</red>"));
            return;
        }

        // Read captured mark names from the latest scout_captures file for this show
        Map<String, CaptureEntry> captures = readCaptures(showId);
        Set<String> capturedMarkNames = captures.keySet();

        // Timeline cue count — from ShowRegistry if the flat YAML is loaded
        int timelineCueCount = -1;
        if (showRegistry != null) {
            var show = showRegistry.get(showId);
            if (show != null) {
                timelineCueCount = show.timeline.size();
            }
        }

        ShowDashboardBuilder.send(player, book, capturedMarkNames, timelineCueCount);
    }

    // -----------------------------------------------------------------------
    // enterTech — /scaena tech <showId> [sceneId]
    // -----------------------------------------------------------------------

    public void enterTech(Player player, String showId, String sceneId) {
        if (hasSession(player)) {
            player.sendMessage(MM.deserialize(
                "<yellow>Tech session already active. Run <white>/scaena tech dismiss</white> first.</yellow>"));
            return;
        }

        // Load prompt book
        PromptBook book = loader.load(plugin.getDataFolder(), showId);
        if (book == null) {
            player.sendMessage(MM.deserialize(
                "<red>No prompt book found for <white>" + showId + "</white>. "
                + "Expected: shows/" + showId + "/" + showId + ".prompt-book.yml</red>"));
            return;
        }

        // Resolve entry scene
        String targetScene = sceneId;
        if (targetScene == null && !book.scenes().isEmpty()) {
            targetScene = book.scenes().get(0).id();   // first scene (already sorted)
        }
        if (targetScene != null && book.findScene(targetScene) == null) {
            player.sendMessage(MM.deserialize(
                "<red>Scene <white>" + targetScene + "</white> not found in " + showId + ".</red>"));
            return;
        }

        // Snapshot player state
        PlayerStateSnapshot snapshot = snapshotPlayer(player);

        // Build session
        TechSession session = new TechSession(showId, player, book, snapshot);
        sessions.put(player.getUniqueId(), session);

        // Switch to adventure + flight
        // Adventure: prevents accidental block break/place; right-click hotbar still works.
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);

        // Give hotbar items
        giveHotbarItems(player);

        // Start display
        BukkitTask task = new TechActionbarTask(this, session).runTaskTimer(plugin, 0L, 5L);
        session.setActionbarTask(task);
        TechSidebarDisplay.show(player, session);

        // LOAD first scene
        if (targetScene != null) {
            loadScene(session, targetScene);
        }

        // Send panel
        TechPanelBuilder.send(player, session);

        log.info("[Tech] " + player.getName() + " entered tech mode for " + showId
            + (targetScene != null ? " @ " + targetScene : ""));
    }

    // -----------------------------------------------------------------------
    // DISMISS — stop-safety: clean up everything
    // -----------------------------------------------------------------------

    public void dismiss(Player player) {
        dismiss(player, true);
    }

    /**
     * @param promptSave if true and unsaved changes exist, sends save/discard prompt
     *                   instead of hard-dismissing. Pass false for disconnect cleanup.
     */
    public void dismiss(Player player, boolean promptSave) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(MM.deserialize("<yellow>No active tech session.</yellow>"));
            return;
        }

        if (promptSave && session.hasUnsavedChanges()) {
            sendSavePrompt(player, session);
            return;
        }

        forceDismiss(session, player);
    }

    /** Force-dismisses without a save prompt. Used for discard and disconnect. */
    public void forceDismiss(TechSession session, Player player) {
        // Stop display tasks
        if (session.actionbarTask() != null) {
            session.actionbarTask().cancel();
        }
        if (session.confirmTask() != null) {
            session.confirmTask().cancel();
        }

        // Despawn all entities and mark markers
        despawnAll(session);
        session.clearMarkMarkers();

        // Restore all blocks
        restoreAllBlocks(session);

        // Restore player state
        PlayerStateSnapshot snap = session.playerState();
        player.setGameMode(snap.gameMode());
        player.setAllowFlight(snap.allowFlight());
        player.setFlying(snap.allowFlight() && snap.flying());

        // Restore hotbar slots 4–8
        ItemStack[] restored = snap.hotbarItems();
        for (int i = 0; i < 5; i++) {
            player.getInventory().setItem(SLOT_PREV + i, restored[i]);
        }
        player.getInventory().setHeldItemSlot(snap.heldItemSlot());

        // Restore scoreboard
        player.setScoreboard(snap.scoreboard());

        // Clear actionbar
        player.sendActionBar(Component.empty());

        sessions.remove(player.getUniqueId());
        player.sendMessage(MM.deserialize("<gray>Tech session ended.</gray>"));
        log.info("[Tech] " + player.getName() + " dismissed tech mode for " + session.showId());
    }

    // -----------------------------------------------------------------------
    // LOAD — materialize scene assets
    // -----------------------------------------------------------------------

    /** Load a new scene, replacing the current scene's assets. */
    public void loadScene(TechSession session, String sceneId) {
        // Tear down current scene
        tearDownScene(session);

        PromptBook.SceneSpec scene = session.book().findScene(sceneId);
        if (scene == null) {
            session.player().sendMessage(MM.deserialize(
                "<red>Scene not found: " + sceneId + "</red>"));
            return;
        }
        session.setCurrentSceneId(sceneId);
        session.exitCaptureMode();

        // Build department mask — only departments with authored assets (Q7: Option A)
        session.departmentMask().clear();
        if (scene.hasCasting())  session.activateDept("casting");
        if (scene.hasWardrobe()) session.activateDept("wardrobe");
        if (scene.hasSet())      session.activateDept("set");
        if (scene.hasLighting()) session.activateDept("lighting");
        if (scene.hasFireworks())session.activateDept("fireworks");
        if (scene.hasScript())   session.activateDept("script");

        // Load scout captures for this show
        Map<String, CaptureEntry> captures = readCaptures(session.showId());
        session.setCapturedMarkNames(captures.keySet());

        // Resolve player before any operations that need it
        Player player = session.player();

        // Teleport to arrival mark
        String arrivalMark = scene.arrivalMark();
        if (arrivalMark != null) {
            CaptureEntry arrival = captures.get(arrivalMark);
            if (arrival != null) {
                Location dest    = captureToLocation(arrival);
                Location current = player.getLocation();
                // Teleport only if in a different world or outside the ~10-block perimeter
                boolean sameWorld        = java.util.Objects.equals(current.getWorld(), dest.getWorld());
                boolean outsidePerimeter = !sameWorld || current.distanceSquared(dest) > 100.0;
                if (outsidePerimeter) {
                    player.teleport(dest);
                }
            } else {
                // Arrival mark not captured — auto-enter capture mode so the player can set it now
                session.enterCaptureMode(arrivalMark);
            }
        }

        // Materialize active departments
        for (String dept : session.departmentMask()) {
            materializeDept(session, scene, dept, captures, player);
        }

        // Spawn in-world markers at captured mark positions
        spawnMarkMarkers(session, scene, captures);

        // Update displays
        TechSidebarDisplay.show(player, session);
        TechPanelBuilder.send(player, session);

        log.info("[Tech] " + player.getName() + " loaded scene " + sceneId
            + " for " + session.showId());
    }

    // -----------------------------------------------------------------------
    // TOGGLE — turn a department on or off
    // -----------------------------------------------------------------------

    public void toggleDept(Player player, String dept) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        if (scene == null) return;

        if (session.isDeptActive(dept)) {
            // Toggle OFF — despawn/restore that dept's assets
            despawnDept(session, dept);
            restoreBlocksDept(session, dept);

            // Wardrobe dependency: Casting off → Wardrobe off with it
            if ("casting".equals(dept) && session.isDeptActive("wardrobe")) {
                despawnDept(session, "wardrobe");
                session.deactivateDept("wardrobe");
                player.sendMessage(MM.deserialize(
                    "<gray>Wardrobe deactivated with Casting.</gray>"));
            }

            session.deactivateDept(dept);
            player.sendMessage(MM.deserialize(
                "<gray>Dept <white>" + dept + "</white> deactivated.</gray>"));
        } else {
            // Check authored assets exist for this dept in this scene
            if (!deptHasAssets(scene, dept)) {
                player.sendMessage(MM.deserialize(
                    "<yellow>No authored assets for <white>" + dept
                    + "</white> in this scene.</yellow>"));
                return;
            }

            Map<String, CaptureEntry> captures = readCaptures(session.showId());
            materializeDept(session, scene, dept, captures, player);
            session.activateDept(dept);

            // If Casting just turned on and wardrobe has assets, re-apply wardrobe
            if ("casting".equals(dept) && deptHasAssets(scene, "wardrobe")
                    && !session.isDeptActive("wardrobe")) {
                materializeDept(session, scene, "wardrobe", captures, player);
                session.activateDept("wardrobe");
                player.sendMessage(MM.deserialize(
                    "<gray>Wardrobe re-applied with Casting.</gray>"));
            }

            player.sendMessage(MM.deserialize(
                "<gray>Dept <white>" + dept + "</white> activated.</gray>"));
        }

        TechSidebarDisplay.show(player, session);
        TechPanelBuilder.send(player, session);
    }

    // -----------------------------------------------------------------------
    // CAPTURE — store a mark position
    // -----------------------------------------------------------------------

    public void startCaptureMode(Player player, String markName) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.enterCaptureMode(markName);
        TechPanelBuilder.send(player, session);
        // Actionbar task picks up captureMode flag and shows live coords
    }

    public void exitCaptureMode(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.exitCaptureMode();
        TechPanelBuilder.send(player, session);
    }

    /** Called when player right-clicks slot 8 (capture slot) in capture mode. */
    public void confirmCapture(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (!session.captureMode() || session.focusedMark() == null) {
            // Not in capture mode — open mark list panel instead
            TechPanelBuilder.sendMarkList(player, session);
            return;
        }

        Location loc = player.getLocation();
        String markName = session.focusedMark();
        session.modifiedMarks().put(markName, loc.clone());
        session.addCapturedMark(markName);

        // Spawn/update in-world marker at the captured position
        PromptBook.SceneSpec currentScene = session.book().findScene(session.currentSceneId());
        spawnMarkMarkerForName(session, currentScene, markName, loc.clone());

        // If an entity is at this mark, teleport it
        UUID entityUuid = session.findEntityAtMark(markName);
        if (entityUuid != null) {
            Entity entity = Bukkit.getEntity(entityUuid);
            if (entity != null && entity.isValid()) {
                entity.teleport(loc);
            }
        }

        session.exitCaptureMode();

        String msg = String.format("📍 Captured %s (%.1f, %.1f, %.1f)",
            markName, loc.getX(), loc.getY(), loc.getZ());
        flashConfirm(session, msg);

        TechPanelBuilder.send(player, session);
        TechSidebarDisplay.show(player, session);
    }

    // -----------------------------------------------------------------------
    // SAVE — write changes to disk
    // -----------------------------------------------------------------------

    public void save(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        int paramCount = 0;
        int markCount  = 0;

        // Write param changes to prompt-book.yml
        if (!session.modifiedParams().isEmpty()) {
            PromptBookWriter writer = new PromptBookWriter(log);
            File bookFile = new File(plugin.getDataFolder(),
                "shows/" + session.showId() + "/" + session.showId() + ".prompt-book.yml");
            paramCount = writer.writeParams(bookFile, session.modifiedParams());
            session.modifiedParams().clear();
        }

        // Write pending bbox corners to the prompt-book (stored separately from mark captures)
        boolean hasBbox = session.pendingBboxMin() != null && session.pendingBboxMax() != null;
        if (hasBbox) {
            Location mn = session.pendingBboxMin();
            Location mx = session.pendingBboxMax();
            PromptBook.BboxPoint bboxMin = new PromptBook.BboxPoint(
                mn.getWorld().getName(), mn.getBlockX(), mn.getBlockY(), mn.getBlockZ());
            PromptBook.BboxPoint bboxMax = new PromptBook.BboxPoint(
                mx.getWorld().getName(), mx.getBlockX(), mx.getBlockY(), mx.getBlockZ());
            PromptBookWriter writer = new PromptBookWriter(log);
            File bookFile = promptBookFile(session.showId());
            writer.writeBbox(bookFile, session.currentSceneId(), bboxMin, bboxMax);
            session.setPendingBboxMin(null);
            session.setPendingBboxMax(null);
            // Remove the sentinel mark used to flag dirty state
            session.modifiedMarks().remove("__bbox__");
            paramCount++; // count it so the "saved N changes" message is accurate
        }

        // Write mark captures to a new dated scout_captures file
        if (!session.modifiedMarks().isEmpty()) {
            PromptBookWriter writer = new PromptBookWriter(log);
            File captureDir = new File(plugin.getDataFolder(),
                "scout_captures/" + session.showId());
            markCount = writer.writeCaptures(captureDir, session.showId(),
                session.modifiedMarks());
            session.modifiedMarks().clear();
        }

        int total = paramCount + markCount;
        if (total == 0) {
            player.sendMessage(MM.deserialize("<gray>No changes to save.</gray>"));
        } else {
            player.sendMessage(MM.deserialize(
                "<green>Saved " + total + " change(s): "
                + paramCount + " param(s), " + markCount + " mark(s).</green>"));
        }

        TechSidebarDisplay.show(player, session);
    }

    // Discard all pending changes and proceed with dismiss
    public void discardAndDismiss(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.modifiedParams().clear();
        session.modifiedMarks().clear();
        forceDismiss(session, player);
    }

    // Save, then dismiss
    public void saveAndDismiss(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        save(player);
        forceDismiss(session, player);
    }

    // -----------------------------------------------------------------------
    // Scene navigation
    // -----------------------------------------------------------------------

    public void prevScene(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        List<PromptBook.SceneSpec> scenes = session.book().scenes();
        int idx = findSceneIndex(scenes, session.currentSceneId());
        if (idx <= 0) {
            flashConfirm(session, "Already at the first scene.");
            return;
        }
        loadScene(session, scenes.get(idx - 1).id());
    }

    public void nextScene(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        List<PromptBook.SceneSpec> scenes = session.book().scenes();
        int idx = findSceneIndex(scenes, session.currentSceneId());
        if (idx < 0 || idx >= scenes.size() - 1) {
            flashConfirm(session, "Already at the last scene.");
            return;
        }
        loadScene(session, scenes.get(idx + 1).id());
    }

    // -----------------------------------------------------------------------
    // Parameter tool
    // -----------------------------------------------------------------------

    /** Right-click slot 9 — focus a param or open the param panel. */
    public void paramAction(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        if (session.paramScrollMode() && session.focusedParam() != null) {
            // Confirm: exit scroll mode, stay on param panel
            session.setParamScrollMode(false, null);
            flashConfirm(session, "Param confirmed.");
        } else {
            // Open param panel
            TechPanelBuilder.sendParamPanel(player, session);
        }
    }

    /** Right-click slot 9 — increment focused numeric param. */
    public void incrementParam(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null || session.focusedParam() == null) return;
        adjustParam(session, player, +1);
    }

    /** Shift+right-click slot 9 — decrement focused numeric param. */
    public void decrementParam(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null || session.focusedParam() == null) return;
        adjustParam(session, player, -1);
    }

    /** Focus a specific param for scrolling (called from panel click). */
    public void focusParam(Player player, String paramName) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        PromptBook.ParamSpec spec = session.book().findParam(paramName);
        if (spec == null) return;
        if (spec.locked()) {
            player.sendMessage(MM.deserialize(
                "<yellow>Param <white>" + paramName + "</white> is locked.</yellow>"));
            return;
        }
        if (spec.type() == PromptBook.ParamType.TEXT) {
            // Prompt for chat input
            session.setPendingTextParam(paramName);
            player.sendMessage(MM.deserialize(
                "<gold>Enter new value for <white>" + paramName + "</white> "
                + "<gray>(current: " + spec.displayValue() + ")</gray>"));
            player.sendMessage(MM.deserialize(
                "<gray>Type in chat and press Enter.</gray>"));
        } else {
            session.setParamScrollMode(true, paramName);
            flashConfirm(session,
                "⚙ " + paramName + " = " + currentParamDisplayValue(session, paramName)
                + "  [right-click +, shift+right-click −]");
        }
    }

    /** Called from async chat event handler (already routed to main thread). */
    public void applyTextParam(Player player, String value) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.awaitingTextInput()) return;
        String paramName = session.pendingTextParam();
        session.modifiedParams().put(paramName, value);
        session.clearPendingTextParam();
        flashConfirm(session, "✓ " + paramName + " = \"" + value + "\"");
        TechPanelBuilder.sendParamPanel(player, session);
        TechSidebarDisplay.show(player, session);
    }

    // -----------------------------------------------------------------------
    // onPlayerQuit — force DISMISS, no save prompt
    // -----------------------------------------------------------------------

    public void onPlayerQuit(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        // Discard changes — no prompt on disconnect
        session.modifiedParams().clear();
        session.modifiedMarks().clear();
        forceDismiss(session, player);
    }

    // -----------------------------------------------------------------------
    // Utility: available show IDs with prompt books
    // -----------------------------------------------------------------------

    public List<String> getAvailableShowIds() {
        File showsDir = new File(plugin.getDataFolder(), "shows");
        if (!showsDir.exists() || !showsDir.isDirectory()) return List.of();
        List<String> result = new ArrayList<>();
        File[] subdirs = showsDir.listFiles(File::isDirectory);
        if (subdirs == null) return List.of();
        for (File dir : subdirs) {
            String name = dir.getName();
            File pb = new File(dir, name + ".prompt-book.yml");
            if (pb.exists()) result.add(name);
        }
        Collections.sort(result);
        return result;
    }

    public List<String> getSceneIds(String showId) {
        PromptBook book = loader.load(plugin.getDataFolder(), showId);
        if (book == null) return List.of();
        return book.scenes().stream().map(PromptBook.SceneSpec::id).toList();
    }

    // -----------------------------------------------------------------------
    // Private: department materialization
    // -----------------------------------------------------------------------

    private void materializeDept(TechSession session, PromptBook.SceneSpec scene,
                                  String dept, Map<String, CaptureEntry> captures,
                                  Player player) {
        switch (dept) {
            case "casting"  -> materializeCasting(session, scene, captures, player);
            case "wardrobe" -> materializeWardrobe(session, scene, player);
            case "set"      -> materializeSet(session, scene, captures, player);
            case "lighting" -> materializeLighting(session, scene, player);
            case "fireworks" -> materializeFireworks(session, scene, captures, player);
            // camera, sound, effects: skip in Phase 1 (no authored dept data in schema)
        }
    }

    // ---- Casting ----

    private void materializeCasting(TechSession session, PromptBook.SceneSpec scene,
                                     Map<String, CaptureEntry> captures, Player player) {
        if (!scene.hasCasting()) return;
        for (PromptBook.CastingEntry entry : scene.casting().entries()) {
            if (entry.mark() == null || entry.entityType() == null) continue;
            CaptureEntry cap = captures.get(entry.mark());
            if (cap == null) {
                player.sendMessage(MM.deserialize(
                    "<yellow>⚠ Mark <white>" + entry.mark()
                    + "</white> not captured — " + entry.role() + " skipped.</yellow>"));
                continue;
            }

            World world = Bukkit.getWorld(cap.world());
            if (world == null) {
                log.warning("[Tech] Unknown world: " + cap.world());
                continue;
            }

            EntityType type;
            try {
                type = EntityType.valueOf(entry.entityType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warning("[Tech] Unknown entity type: " + entry.entityType());
                continue;
            }

            Location loc = new Location(world, cap.x(), cap.y(), cap.z(),
                cap.yaw(), cap.pitch());
            Entity entity = world.spawnEntity(loc, type);

            // Display name
            if (entry.displayName() != null) {
                entity.customName(Component.text(entry.displayName())
                    .color(NamedTextColor.WHITE));
                entity.setCustomNameVisible(true);
            }

            // AI lock
            if (entry.aiLocked() && entity instanceof Mob mob) {
                mob.setAI(false);
            }

            // Villager profession / subtype
            if (entity instanceof Villager villager && entry.subtype() != null) {
                try {
                    villager.setProfession(
                        Villager.Profession.valueOf(entry.subtype().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warning("[Tech] Unknown Villager profession: " + entry.subtype());
                }
            }

            session.addEntity(entity.getUniqueId(),
                new TechSession.EntityRecord(entry.mark(), "casting"));
        }
    }

    // ---- Wardrobe ----

    private void materializeWardrobe(TechSession session, PromptBook.SceneSpec scene,
                                      Player player) {
        if (!scene.hasWardrobe()) return;
        for (PromptBook.WardrobeEntry entry : scene.wardrobe().entries()) {
            if (entry.entityMark() == null) continue;
            UUID uuid = session.findEntityAtMark(entry.entityMark());
            if (uuid == null) {
                player.sendMessage(MM.deserialize(
                    "<yellow>⚠ No entity at mark <white>" + entry.entityMark()
                    + "</white> — wardrobe entry skipped. Is Casting active?</yellow>"));
                continue;
            }
            Entity entity = Bukkit.getEntity(uuid);
            if (!(entity instanceof LivingEntity living)) continue;
            EntityEquipment eq = living.getEquipment();
            if (eq == null) continue;

            if (entry.helmet()     != null) eq.setHelmet(makeItem(entry.helmet(), null, null));
            if (entry.chestplate() != null) eq.setChestplate(makeItem(entry.chestplate(), null, null));
            if (entry.leggings()   != null) eq.setLeggings(makeItem(entry.leggings(), null, null));
            if (entry.boots()      != null) eq.setBoots(makeItem(entry.boots(), entry.bootsDye(), null));
            if (entry.mainHand()   != null) eq.setItemInMainHand(
                makeItem(entry.mainHand(), null, entry.mainHandEnchant()));

            // Drop chances to 0 so items don't drop on entity death
            eq.setHelmetDropChance(0f);
            eq.setChestplateDropChance(0f);
            eq.setLeggingsDropChance(0f);
            eq.setBootsDropChance(0f);
            eq.setItemInMainHandDropChance(0f);
        }
    }

    // ---- Set ----

    private void materializeSet(TechSession session, PromptBook.SceneSpec scene,
                                 Map<String, CaptureEntry> captures, Player player) {
        if (!scene.hasSet()) return;
        for (PromptBook.SetEntry entry : scene.set().entries()) {
            // Skip note-only entries (no mark or no block type)
            if (entry.mark() == null || entry.blockType() == null) continue;
            CaptureEntry cap = captures.get(entry.mark());
            if (cap == null) {
                player.sendMessage(MM.deserialize(
                    "<yellow>⚠ Mark <white>" + entry.mark()
                    + "</white> not captured — block change skipped.</yellow>"));
                continue;
            }

            World world = Bukkit.getWorld(cap.world());
            if (world == null) {
                log.warning("[Tech] Unknown world for Set mark " + entry.mark()
                    + ": " + cap.world());
                continue;
            }

            int bx = cap.blockX != null ? cap.blockX : (int) Math.round(cap.x());
            int by = cap.blockY != null ? cap.blockY : (int) Math.floor(cap.y()) - 1;
            int bz = cap.blockZ != null ? cap.blockZ : (int) Math.round(cap.z());
            org.bukkit.block.Block block = world.getBlockAt(bx, by, bz);

            // Snapshot original
            String snapKey = TechSession.BlockSnapshot.key(world, bx, by, bz);
            if (!session.blockSnapshots().containsKey(snapKey)) {
                session.addBlockSnapshot(new TechSession.BlockSnapshot(
                    snapKey, world, bx, by, bz, block.getBlockData().clone(), "set"));
            }

            // Build and apply BlockData
            try {
                String dataStr = "minecraft:" + entry.blockType().toLowerCase();
                if (entry.blockState() != null && !entry.blockState().isBlank()) {
                    dataStr += "[" + entry.blockState() + "]";
                }
                BlockData bd = Bukkit.createBlockData(dataStr);
                block.setBlockData(bd, false);
            } catch (IllegalArgumentException e) {
                log.warning("[Tech] Invalid block data for " + entry.mark()
                    + ": " + entry.blockType() + " / " + entry.blockState()
                    + " — " + e.getMessage());
            }
        }
    }

    // ---- Lighting ----

    private void materializeLighting(TechSession session, PromptBook.SceneSpec scene,
                                      Player player) {
        if (!scene.hasLighting()) return;
        PromptBook.DeptLighting lighting = scene.lighting();
        if (lighting.timeOfDay() != null) {
            World world = player.getWorld();
            world.setTime(lighting.timeOfDay());
        }
    }

    // ---- Fireworks (one-shot test burst) ----

    private void materializeFireworks(TechSession session, PromptBook.SceneSpec scene,
                                       Map<String, CaptureEntry> captures, Player player) {
        if (!scene.hasFireworks()) return;
        // One-shot test burst at arrival mark
        String arrival = scene.arrivalMark();
        if (arrival == null) return;
        CaptureEntry cap = captures.get(arrival);
        if (cap == null) return;
        World world = Bukkit.getWorld(cap.world());
        if (world == null) return;

        Location loc = new Location(world, cap.x(), cap.y() + 1, cap.z());
        Firework fw = world.spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL)
            .withColor(Color.WHITE)
            .withFade(Color.AQUA)
            .trail(false)
            .flicker(false)
            .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    // -----------------------------------------------------------------------
    // BUILD MODE — SURVIVAL toggle for set-dressing with change tracking
    // -----------------------------------------------------------------------

    public void toggleBuildMode(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        if (!session.buildMode()) {
            // ── Entering build mode ──────────────────────────────────────────
            PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
            boolean hasBbox = scene != null && scene.hasSet() && scene.set().hasBbox();
            boolean hasPendingBbox = session.pendingBboxMin() != null && session.pendingBboxMax() != null;

            if (!hasBbox && !hasPendingBbox) {
                // No bbox defined — prompt the player to set one before tracking anything
                player.sendMessage(MM.deserialize(
                    "<yellow>⚠ No bounding box set for this scene's set.</yellow>"));
                player.sendMessage(MM.deserialize(
                    "<gray>Stand at one corner of the set area and run "
                    + "<white>/scaena tech bbox min</white>, "
                    + "then at the opposite corner run "
                    + "<white>/scaena tech bbox max</white>.</gray>"));
                player.sendMessage(MM.deserialize(
                    "<gray>Then enter build mode again. "
                    + "Changes outside the box are allowed but won't be tracked.</gray>"));
                return;
            }

            session.setBuildMode(true);
            session.setActiveBuildSession(new SetBuildSession(session.currentSceneId()));
            player.setGameMode(GameMode.SURVIVAL);
            flashConfirm(session, "🔨 BUILD MODE — tracking changes in set bounds");

        } else {
            // ── Exiting build mode ──────────────────────────────────────────
            player.setGameMode(GameMode.ADVENTURE);
            session.setBuildMode(false);

            SetBuildSession build = session.activeBuildSession();
            if (build == null || build.isEmpty()) {
                session.clearBuildSession();
                flashConfirm(session, "🔒 TECH MODE — no changes recorded");
            } else if (build.hasRemovals()) {
                // Removals present → ask what to do with them before offering save
                sendRemovalPrompt(player, session, build);
            } else {
                // Only additions → go straight to save prompt
                sendBuildSavePrompt(player, session, build);
            }
        }
        TechSidebarDisplay.show(player, session);
    }

    /**
     * Set one corner of the bounding box for the current scene's set.
     * Stored in-session immediately; written to the prompt-book on SAVE.
     *
     * @param isMin true for bbox_min, false for bbox_max
     */
    public void setBboxCorner(Player player, boolean isMin) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        Location loc = player.getLocation();
        if (isMin) {
            session.setPendingBboxMin(loc);
            flashConfirm(session, "📐 bbox_min set — " + locStr(loc));
        } else {
            session.setPendingBboxMax(loc);
            flashConfirm(session, "📐 bbox_max set — " + locStr(loc));
        }

        // If both corners are now set, confirm and mark session dirty
        if (session.pendingBboxMin() != null && session.pendingBboxMax() != null) {
            player.sendMessage(MM.deserialize(
                "<green>✓ Bounding box complete.</green> "
                + "<gray>Run <white>/scaena tech save</white> to write it to the prompt-book.</gray>"));
            // Mark dirty so the save prompt catches it
            session.modifiedMarks().put("__bbox__", loc); // sentinel to flag dirty state
        }

        TechSidebarDisplay.show(player, session);
    }

    /**
     * Save the current build session as a new versioned file.
     * Called from the "Save" option in the build-exit prompt, or directly.
     */
    public void saveBuild(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        SetBuildSession build = session.activeBuildSession();
        if (build == null || build.isEmpty()) {
            player.sendMessage(MM.deserialize("<yellow>No build changes to save.</yellow>"));
            return;
        }

        File buildDir = new File(plugin.getDataFolder(),
            "set_builds/" + session.showId());
        SetBuildWriter writer = new SetBuildWriter(log);
        String stem = writer.write(buildDir, session.showId(), build);

        if (stem != null) {
            // Update active_build pointer in the prompt-book (via writer)
            File bookFile = promptBookFile(session.showId());
            PromptBookWriter pbWriter = new PromptBookWriter(log);
            pbWriter.writeActiveBuild(bookFile, session.currentSceneId(), stem);

            flashConfirm(session, "💾 Set build saved: " + stem);
            player.sendMessage(MM.deserialize(
                "<green>✓ Saved as <white>" + stem + "</white>. "
                + "This is now the active set build for this scene.</green>"));
        }

        session.clearBuildSession();
        TechSidebarDisplay.show(player, session);
    }

    /**
     * Discard the current build session without saving.
     * Does NOT restore removed blocks — that must be done first via restoreBuildRemovals().
     */
    public void discardBuild(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.clearBuildSession();
        flashConfirm(session, "🗑 Build session discarded");
        TechSidebarDisplay.show(player, session);
    }

    /**
     * Restore all blocks removed during the current build session, then
     * proceed to the save/discard prompt.
     */
    public void restoreBuildRemovals(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        SetBuildSession build = session.activeBuildSession();
        if (build == null) return;

        int count = build.removals().size();
        build.restoreRemovals();
        flashConfirm(session, "↩ " + count + " block(s) restored");

        // After restoring, offer save/discard for remaining additions
        if (!build.isEmpty()) {
            sendBuildSavePrompt(player, session, build);
        } else {
            player.sendMessage(MM.deserialize(
                "<gray>No remaining changes. Build session cleared.</gray>"));
            session.clearBuildSession();
        }
        TechSidebarDisplay.show(player, session);
    }

    /**
     * Send the version list panel for the current scene's set builds.
     */
    public void sendBuildVersions(Player player) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        File buildDir = new File(plugin.getDataFolder(),
            "set_builds/" + session.showId());
        java.util.List<String> versions =
            SetBuildWriter.listVersions(buildDir, session.currentSceneId());

        if (versions.isEmpty()) {
            player.sendMessage(MM.deserialize(
                "<yellow>No saved set builds for this scene yet.</yellow>"));
            return;
        }

        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        String active = (scene != null && scene.hasSet())
            ? scene.set().activeBuild() : null;

        player.sendMessage(MM.deserialize(
            "<gold>Set builds — " + session.currentSceneId() + "</gold>"));
        for (String stem : versions) {
            boolean isCurrent = stem.equals(active);
            net.kyori.adventure.text.Component line =
                net.kyori.adventure.text.Component.text(
                    (isCurrent ? "  ✓ " : "  ○ ") + stem,
                    isCurrent
                        ? net.kyori.adventure.text.format.TextColor.color(0x55FF55)
                        : net.kyori.adventure.text.format.TextColor.color(0x55FFFF))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
                    "/scaena tech setbuild " + stem))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                    net.kyori.adventure.text.Component.text(
                        isCurrent ? "Active version — click to re-confirm"
                                  : "Click to make this the active version")));
            player.sendMessage(line);
        }
        player.sendMessage(MM.deserialize(
            "<gray>Click a version to make it active. "
            + "Active version is applied when the show's SET cue fires.</gray>"));
    }

    /**
     * Set the active build version for the current scene.
     */
    public void setActiveBuild(Player player, String stem) {
        TechSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        File buildDir = new File(plugin.getDataFolder(),
            "set_builds/" + session.showId());
        File buildFile = new File(buildDir, stem + ".yml");
        if (!buildFile.exists()) {
            player.sendMessage(MM.deserialize(
                "<red>Set build not found: <white>" + stem + "</white></red>"));
            return;
        }

        File bookFile = promptBookFile(session.showId());
        PromptBookWriter pbWriter = new PromptBookWriter(log);
        pbWriter.writeActiveBuild(bookFile, session.currentSceneId(), stem);

        flashConfirm(session, "✓ Active build set: " + stem);
        TechSidebarDisplay.show(player, session);
    }

    // -----------------------------------------------------------------------
    // Private: build mode prompts
    // -----------------------------------------------------------------------

    private void sendRemovalPrompt(Player player, TechSession session, SetBuildSession build) {
        int removals  = build.removals().size();
        int additions = build.additions().size();
        player.sendMessage(MM.deserialize(
            "<gold>Build session complete.</gold> "
            + "<gray>" + build.totalCount() + " net change(s): "
            + additions + " placed, " + removals + " removed.</gray>"));
        player.sendMessage(MM.deserialize(
            "<gray>Removed blocks are currently gone from the world.</gray>"));

        player.sendMessage(
            net.kyori.adventure.text.Component.text("[Restore removed blocks]",
                net.kyori.adventure.text.format.TextColor.color(0x55FFFF))
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
                "/scaena tech buildrestore"))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                net.kyori.adventure.text.Component.text(
                    "Put the " + removals + " removed block(s) back now.\n"
                    + "They will be removed again when the show's SET cue fires.")))
            .append(net.kyori.adventure.text.Component.text("   "))
            .append(net.kyori.adventure.text.Component.text("[Keep removed + Save]",
                net.kyori.adventure.text.format.TextColor.color(0x55FF55))
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
                "/scaena tech buildsave"))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                net.kyori.adventure.text.Component.text(
                    "Leave the world as-is and save the full diff.\n"
                    + "The show engine will restore these blocks at show end."))))
            .append(net.kyori.adventure.text.Component.text("   "))
            .append(net.kyori.adventure.text.Component.text("[Discard]",
                net.kyori.adventure.text.format.TextColor.color(0xFF5555))
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
                "/scaena tech builddiscard"))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                net.kyori.adventure.text.Component.text(
                    "Discard this build session without saving.\n"
                    + "World stays as-is (removed blocks remain gone)."))))
        );
    }

    private void sendBuildSavePrompt(Player player, TechSession session, SetBuildSession build) {
        int additions = build.additions().size();
        player.sendMessage(MM.deserialize(
            "<gold>Build session complete.</gold> "
            + "<gray>" + additions + " block(s) placed.</gray>"));

        player.sendMessage(
            net.kyori.adventure.text.Component.text("[Save as new version]",
                net.kyori.adventure.text.format.TextColor.color(0x55FF55))
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
                "/scaena tech buildsave"))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                net.kyori.adventure.text.Component.text("Save this as a new versioned set build.")))
            .append(net.kyori.adventure.text.Component.text("   "))
            .append(net.kyori.adventure.text.Component.text("[Discard]",
                net.kyori.adventure.text.format.TextColor.color(0xFF5555))
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
                "/scaena tech builddiscard"))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                net.kyori.adventure.text.Component.text("Discard without saving."))))
        );
    }

    private static String locStr(Location loc) {
        return String.format("%d, %d, %d",
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /** Resolve the prompt-book file for a given showId. */
    private File promptBookFile(String showId) {
        return new File(plugin.getDataFolder(),
            "shows/" + showId + "/" + showId + ".prompt-book.yml");
    }

    // -----------------------------------------------------------------------
    // Private: mark markers — TextDisplay entities at captured positions
    // -----------------------------------------------------------------------

    /**
     * Spawn TextDisplay markers for every mark in the current scene.
     *
     * Captured marks (those present in scout_captures) are shown at their known
     * positions with role-based colors (gold = arrival, lime = mob, aqua = set/other).
     *
     * Uncaptured marks (defined in the scene but never captured) are shown as red
     * "? markName" placeholders near the player's current position, offset slightly
     * so they don't stack. These placeholders exist only to surface the gap — they
     * carry no spatial meaning until the mark is actually captured.
     */
    private void spawnMarkMarkers(TechSession session, PromptBook.SceneSpec scene,
                                   Map<String, CaptureEntry> captures) {
        if (scene == null) return;

        // Captured marks — spawn at known position with role-based coloring
        for (String markName : session.capturedMarkNames()) {
            CaptureEntry cap = captures.get(markName);
            if (cap == null) continue;
            spawnMarkMarkerForName(session, scene, markName, captureToLocation(cap));
        }

        // Uncaptured marks — spawn red placeholder near player so the gap is visible
        Set<String> allSceneMarks = allMarkNamesInScene(scene);
        Location anchor = session.player().getLocation().clone().add(0, 0.5, 0);
        int offset = 0;
        for (String markName : allSceneMarks) {
            if (session.capturedMarkNames().contains(markName)) continue;
            // Spread placeholders 1.5 blocks apart in X so they don't overlap
            Location placeholderLoc = anchor.clone().add(offset * 1.5, 0, 0);
            spawnUncapturedMarkMarker(session, markName, placeholderLoc);
            offset++;
        }
    }

    /**
     * Returns all mark names explicitly referenced by this scene, across all departments.
     * Does not include wardrobe entries — those reference casting marks that are already
     * collected from the casting department.
     */
    private Set<String> allMarkNamesInScene(PromptBook.SceneSpec scene) {
        Set<String> marks = new LinkedHashSet<>();
        if (scene.arrivalMark() != null) marks.add(scene.arrivalMark());
        if (scene.hasCasting()) {
            for (PromptBook.CastingEntry mob : scene.casting().entries()) {
                if (mob.mark() != null) marks.add(mob.mark());
            }
        }
        if (scene.hasSet()) {
            for (PromptBook.SetEntry entry : scene.set().entries()) {
                if (entry.mark() != null) marks.add(entry.mark());
            }
        }
        return marks;
    }

    /**
     * Spawn (or replace) a single TextDisplay marker.
     * Determines label and color from the mark's role in the scene.
     */
    void spawnMarkMarkerForName(TechSession session, PromptBook.SceneSpec scene,
                                String markName, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        // Determine label + color from mark role
        String label = "◼ " + markName;
        TextColor color = TextColor.color(0x55FFFF); // aqua default (set/block marks)

        if (scene != null) {
            if (markName.equals(scene.arrivalMark())) {
                label = "📍 " + markName;
                color = TextColor.color(0xFFD700); // gold — arrival
            } else if (scene.hasCasting()) {
                for (PromptBook.CastingEntry mob : scene.casting().entries()) {
                    if (markName.equals(mob.mark())) {
                        String name = mob.displayName() != null ? mob.displayName() : mob.role();
                        label = "👤 " + name;
                        color = TextColor.color(0x55FF55); // lime — mob spawn
                        break;
                    }
                }
            }
        }

        Location dispLoc = loc.clone().add(0, 0.5, 0);
        TextDisplay display = (TextDisplay) world.spawnEntity(dispLoc, EntityType.TEXT_DISPLAY);
        display.setPersistent(false);
        display.setInvulnerable(true);
        display.setBillboard(Display.Billboard.CENTER);
        display.setViewRange(0.5f); // ~32 blocks
        display.text(Component.text(label).color(color));
        display.setBackgroundColor(Color.fromARGB(80, 0, 0, 0));

        session.addMarkMarker(markName, display);
    }

    /**
     * Spawn a red placeholder marker for a mark that exists in the scene definition
     * but has never been captured. The location is near the player (anchor-relative),
     * not the true mark position (which is unknown).
     *
     * Visual: dim red `? markName` with a dark red background — clearly a placeholder,
     * not a real captured position. Replaced by a proper marker when the mark is captured.
     */
    private void spawnUncapturedMarkMarker(TechSession session, String markName, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        TextDisplay display = (TextDisplay) world.spawnEntity(loc, EntityType.TEXT_DISPLAY);
        display.setPersistent(false);
        display.setInvulnerable(true);
        display.setBillboard(Display.Billboard.CENTER);
        display.setViewRange(0.5f); // ~32 blocks
        display.text(Component.text("? " + markName).color(TextColor.color(0xFF5555))); // red
        display.setBackgroundColor(Color.fromARGB(140, 80, 0, 0)); // dark red bg

        session.addMarkMarker(markName, display);
    }

    // -----------------------------------------------------------------------
    // Private: tear-down helpers
    // -----------------------------------------------------------------------

    /** Remove current scene's entities and blocks (called before loading a new scene). */
    private void tearDownScene(TechSession session) {
        despawnAll(session);
        restoreAllBlocks(session);
        session.clearMarkMarkers();
        session.departmentMask().clear();
    }

    private void despawnAll(TechSession session) {
        List<UUID> uuids = new ArrayList<>(session.activeEntities().keySet());
        for (UUID uuid : uuids) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null && entity.isValid()) entity.remove();
            session.removeEntity(uuid);
        }
    }

    private void despawnDept(TechSession session, String dept) {
        List<UUID> uuids = session.entitiesForDept(dept);
        for (UUID uuid : uuids) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null && entity.isValid()) entity.remove();
            session.removeEntity(uuid);
        }
    }

    private void restoreAllBlocks(TechSession session) {
        for (TechSession.BlockSnapshot snap : session.blockSnapshots().values()) {
            snap.world().getBlockAt(snap.x(), snap.y(), snap.z())
                .setBlockData(snap.originalData(), false);
        }
        session.blockSnapshots().clear();
    }

    private void restoreBlocksDept(TechSession session, String dept) {
        List<TechSession.BlockSnapshot> toRestore = session.blockSnapshotsForDept(dept);
        for (TechSession.BlockSnapshot snap : toRestore) {
            snap.world().getBlockAt(snap.x(), snap.y(), snap.z())
                .setBlockData(snap.originalData(), false);
            session.blockSnapshots().remove(snap.locationKey());
        }
    }

    // -----------------------------------------------------------------------
    // Private: save prompt
    // -----------------------------------------------------------------------

    private void sendSavePrompt(Player player, TechSession session) {
        player.sendMessage(MM.deserialize(
            "<gold>You have unsaved changes ("
            + session.modifiedParams().size() + " param(s), "
            + session.modifiedMarks().size() + " mark(s)).</gold>"));
        player.sendMessage(
            MM.deserialize(
                "  <green><click:run_command:'/scaena tech saveanddismiss'>"
                + "[Save to prompt-book]</click></green>"
                + "  <red><click:run_command:'/scaena tech discard'>"
                + "[Discard]</click></red>"));
    }

    // -----------------------------------------------------------------------
    // Private: param adjustment
    // -----------------------------------------------------------------------

    private void adjustParam(TechSession session, Player player, int direction) {
        String paramName = session.focusedParam();
        PromptBook.ParamSpec spec = session.book().findParam(paramName);
        if (spec == null || spec.locked()) return;
        if (spec.type() != PromptBook.ParamType.NUMERIC) return;

        double step = spec.step() != null ? spec.step() : 1.0;
        double current = currentParamNumericValue(session, paramName, spec);
        double newVal = current + (direction * step);

        // Clamp to min/max
        if (spec.min() != null) newVal = Math.max(spec.min(), newVal);
        if (spec.max() != null) newVal = Math.min(spec.max(), newVal);

        // Round to avoid floating point noise (snap to nearest step)
        if (step > 0) {
            newVal = Math.round(newVal / step) * step;
        }

        session.modifiedParams().put(paramName, newVal);

        String display = spec.displayValue().equals(formatDouble(current))
            ? formatDouble(newVal)
            : formatDouble(newVal);
        flashConfirm(session, "⚙ " + paramName + " = " + display);
        TechSidebarDisplay.show(player, session);
    }

    private double currentParamNumericValue(TechSession session, String paramName,
                                             PromptBook.ParamSpec spec) {
        Object modified = session.modifiedParams().get(paramName);
        if (modified instanceof Number n) return n.doubleValue();
        if (spec.value() instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private String currentParamDisplayValue(TechSession session, String paramName) {
        PromptBook.ParamSpec spec = session.book().findParam(paramName);
        if (spec == null) return "?";
        Object modified = session.modifiedParams().get(paramName);
        if (modified != null) {
            if (modified instanceof Number n) return formatDouble(n.doubleValue());
            return modified.toString();
        }
        return spec.displayValue();
    }

    // -----------------------------------------------------------------------
    // Private: item construction
    // -----------------------------------------------------------------------

    private ItemStack makeItem(String materialName, String dyeHex, String enchantStr) {
        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warning("[Tech] Unknown material: " + materialName);
            return new ItemStack(Material.AIR);
        }

        ItemStack item = new ItemStack(mat);

        // Leather dye
        if (dyeHex != null && item.getItemMeta() instanceof LeatherArmorMeta lm) {
            try {
                int rgb = Integer.parseInt(dyeHex.replace("#", ""), 16);
                lm.setColor(Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                item.setItemMeta(lm);
            } catch (NumberFormatException e) {
                log.warning("[Tech] Invalid dye color: " + dyeHex);
            }
        }

        // Enchantment
        if (enchantStr != null) {
            String[] parts = enchantStr.split(":");
            if (parts.length == 2) {
                try {
                    NamespacedKey key = NamespacedKey.minecraft(parts[0].toLowerCase());
                    Enchantment enc = Registry.ENCHANTMENT.get(key);
                    int level = Integer.parseInt(parts[1]);
                    if (enc != null) {
                        item.addUnsafeEnchantment(enc, level);
                    } else {
                        log.warning("[Tech] Unknown enchantment: " + parts[0]);
                    }
                } catch (NumberFormatException e) {
                    log.warning("[Tech] Invalid enchantment string: " + enchantStr);
                }
            }
        }

        return item;
    }

    // -----------------------------------------------------------------------
    // Private: hotbar setup
    // -----------------------------------------------------------------------

    private void giveHotbarItems(Player player) {
        setHotbarItem(player, SLOT_PREV,
            Material.SPECTRAL_ARROW, "§e◀ Prev Scene");
        setHotbarItem(player, SLOT_HOLD,
            Material.BLAZE_ROD,      "§7⏸ Hold");
        setHotbarItem(player, SLOT_NEXT,
            Material.SPECTRAL_ARROW, "§e▶ Next Scene");
        setHotbarItem(player, SLOT_CAPTURE,
            Material.COMPASS,        "§b📍 Capture");
        setHotbarItem(player, SLOT_PARAMS,
            Material.COMPARATOR,     "§a⚙ Params");
    }

    private void setHotbarItem(Player player, int slot, Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        player.getInventory().setItem(slot, item);
    }

    // -----------------------------------------------------------------------
    // Private: player state snapshot
    // -----------------------------------------------------------------------

    private PlayerStateSnapshot snapshotPlayer(Player player) {
        ItemStack[] hotbar = new ItemStack[5];
        for (int i = 0; i < 5; i++) {
            ItemStack original = player.getInventory().getItem(SLOT_PREV + i);
            hotbar[i] = original != null ? original.clone() : null;
        }
        return new PlayerStateSnapshot(
            player.getGameMode(),
            player.getAllowFlight(),
            player.isFlying(),
            hotbar,
            player.getInventory().getHeldItemSlot(),
            player.getScoreboard()
        );
    }

    // -----------------------------------------------------------------------
    // Private: scout captures reader
    // -----------------------------------------------------------------------

    /** Reads the most recent capture file for a show. Returns empty map if none exists. */
    @SuppressWarnings("unchecked")
    Map<String, CaptureEntry> readCaptures(String showId) {
        File captureDir = new File(plugin.getDataFolder(), "scout_captures/" + showId);
        if (!captureDir.exists() || !captureDir.isDirectory()) return Map.of();

        File[] files = captureDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) return Map.of();

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        File latest = files[files.length - 1];

        try {
            Map<String, Object> root = YamlLoader.load(latest);
            Object rawCaps = root.get("captures");
            if (!(rawCaps instanceof Map<?, ?> capMap)) return Map.of();

            Map<String, CaptureEntry> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : capMap.entrySet()) {
                String name = entry.getKey().toString();
                if (!(entry.getValue() instanceof Map<?, ?> dm)) continue;
                Map<String, Object> d = (Map<String, Object>) dm;

                String world  = strField(d, "world");
                if (world == null) continue;
                double x      = dblField(d, "x");
                double y      = dblField(d, "y");
                double z      = dblField(d, "z");
                float  yaw    = (float) dblField(d, "yaw");
                float  pitch  = (float) dblField(d, "pitch");

                Integer bx = intFieldOrNull(d, "block_x");
                Integer by = intFieldOrNull(d, "block_y");
                Integer bz = intFieldOrNull(d, "block_z");

                result.put(name, new CaptureEntry(world, x, y, z, yaw, pitch, bx, by, bz));
            }
            return result;
        } catch (IOException e) {
            log.warning("[Tech] Failed to read captures for " + showId
                + " from " + latest.getName() + ": " + e.getMessage());
            return Map.of();
        }
    }

    private static Location captureToLocation(CaptureEntry cap) {
        World world = Bukkit.getWorld(cap.world());
        return new Location(world, cap.x(), cap.y(), cap.z(), cap.yaw(), cap.pitch());
    }

    // -----------------------------------------------------------------------
    // Private: confirm flash
    // -----------------------------------------------------------------------

    void flashConfirm(TechSession session, String message) {
        session.setConfirmMessage(message);
        if (session.confirmTask() != null) session.confirmTask().cancel();
        BukkitTask ct = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            session.clearConfirmMessage();
        }, 40L); // 2 seconds
        session.setConfirmTask(ct);
    }

    // -----------------------------------------------------------------------
    // Private: helpers
    // -----------------------------------------------------------------------

    private int findSceneIndex(List<PromptBook.SceneSpec> scenes, String id) {
        if (id == null) return -1;
        for (int i = 0; i < scenes.size(); i++) {
            if (id.equals(scenes.get(i).id())) return i;
        }
        return -1;
    }

    private boolean deptHasAssets(PromptBook.SceneSpec scene, String dept) {
        return switch (dept) {
            case "casting"   -> scene.hasCasting();
            case "wardrobe"  -> scene.hasWardrobe();
            case "set"       -> scene.hasSet();
            case "lighting"  -> scene.hasLighting();
            case "fireworks" -> scene.hasFireworks();
            case "script"    -> scene.hasScript();
            default          -> false;
        };
    }

    private static String formatDouble(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    private static String strField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private static double dblField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    private static Integer intFieldOrNull(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.intValue() : null;
    }

    // =======================================================================
    // Phase 2 — Timeline Editor (OPS-029 Group 3)
    // =======================================================================

    // -----------------------------------------------------------------------
    // Phase 2 session lookup
    // -----------------------------------------------------------------------

    /** Returns the active TechCueSession for the player, or null if Phase 2 is not active. */
    public TechCueSession getTechCueSession(Player player) {
        return player == null ? null : cueSessions.get(player.getUniqueId());
    }

    public boolean hasCueSession(Player player) {
        return getTechCueSession(player) != null;
    }

    // -----------------------------------------------------------------------
    // enterPhase2 — /scaena tech2 <showId>  (entry command wired in Group 4)
    // -----------------------------------------------------------------------

    /**
     * Enter Phase 2 for the given show.
     *
     * Requires an active Phase 1 TechSession. Loads the show YAML from disk
     * into an in-memory rawYaml map, creates a TechCueSession, and sends the
     * initial cue panel for the first scene.
     */
    public void enterPhase2(Player player, String showId) {
        TechSession phase1 = getSession(player);
        if (phase1 == null) {
            player.sendMessage(MM.deserialize(
                "<red>Phase 2 requires an active tech session. Run <white>/scaena tech " + showId
                + "</white> first.</red>"));
            return;
        }
        if (hasCueSession(player)) {
            player.sendMessage(MM.deserialize(
                "<yellow>Phase 2 already active. Use <white>/scaena tech2 exit</white> first.</yellow>"));
            return;
        }

        // Locate the show YAML file
        File showFile = resolveShowYamlFile(showId);
        if (showFile == null || !showFile.exists()) {
            player.sendMessage(MM.deserialize(
                "<red>Show YAML not found for <white>" + showId + "</white>. "
                + "Expected: shows/" + showId + "/" + showId + ".yml or shows/" + showId + ".yml</red>"));
            return;
        }

        // Load raw YAML
        Map<String, Object> rawYaml;
        try {
            rawYaml = YamlLoader.load(showFile);
        } catch (IOException e) {
            player.sendMessage(MM.deserialize(
                "<red>Failed to load show YAML for <white>" + showId + "</white>: " + e.getMessage() + "</red>"));
            return;
        }

        // Build the Phase 2 session
        PromptBook book = phase1.book();
        TechCueSession cueSession = new TechCueSession(showId, player, book);
        cueSession.setRawYaml(rawYaml);
        cueSession.setSourceFile(showFile);

        // Start at the same scene as Phase 1
        String sceneId = phase1.currentSceneId();
        if (sceneId == null && book != null && !book.scenes().isEmpty()) {
            sceneId = book.scenes().get(0).id();
        }
        cueSession.setCurrentSceneId(sceneId);

        cueSessions.put(player.getUniqueId(), cueSession);

        player.sendMessage(MM.deserialize(
            "<aqua>Phase 2 active — <white>" + showId + "</white>. "
            + "Scene: <white>" + (sceneId != null ? sceneId : "none") + "</white></aqua>"));

        CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
        log.info("[Tech2] " + player.getName() + " entered Phase 2 for " + showId
            + (sceneId != null ? " @ " + sceneId : ""));
    }

    // -----------------------------------------------------------------------
    // exitPhase2
    // -----------------------------------------------------------------------

    /**
     * Exit Phase 2. If a preview show is active, stops it first (full stop-safety).
     * If there are unsaved changes and promptSave is true, asks before discarding.
     */
    public void exitPhase2(Player player) {
        TechCueSession cueSession = cueSessions.remove(player.getUniqueId());
        if (cueSession == null) {
            player.sendMessage(MM.deserialize("<yellow>No active Phase 2 session.</yellow>"));
            return;
        }

        // Stop any running preview
        if (cueSession.isPreviewActive()) {
            stopPreviewInternal(cueSession);
        }

        cueSession.cleanup();

        if (cueSession.isDirty()) {
            player.sendMessage(MM.deserialize(
                "<yellow>Phase 2 exited. Unsaved changes were discarded.</yellow>"));
        } else {
            player.sendMessage(MM.deserialize("<gray>Phase 2 exited.</gray>"));
        }
        log.info("[Tech2] " + player.getName() + " exited Phase 2");
    }

    // -----------------------------------------------------------------------
    // Preview
    // -----------------------------------------------------------------------

    /**
     * Start a step-mode preview of the show from the beginning of the current scene.
     *
     * Builds a RunningShow from the show currently in ShowRegistry (using the
     * on-disk version — rawYaml edits are not yet reflected until save). Puts the
     * ShowScheduler into step mode immediately so no events fire until the player
     * presses Go.
     */
    public void startPreview(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) {
            player.sendMessage(MM.deserialize("<red>No active Phase 2 session.</red>"));
            return;
        }
        if (cueSession.isPreviewActive()) {
            player.sendMessage(MM.deserialize("<yellow>Preview already running.</yellow>"));
            return;
        }
        if (showManager == null || cueRegistry == null || executorRegistry == null) {
            player.sendMessage(MM.deserialize(
                "<red>Phase 2 preview is not yet wired — missing runtime dependencies.</red>"));
            return;
        }
        if (showRegistry == null) {
            player.sendMessage(MM.deserialize("<red>ShowRegistry not available.</red>"));
            return;
        }

        Show show = showRegistry.get(cueSession.getShowId());
        if (show == null) {
            player.sendMessage(MM.deserialize(
                "<red>Show <white>" + cueSession.getShowId()
                + "</white> is not loaded in ShowRegistry. Build and reload first.</red>"));
            return;
        }

        // Create RunningShow with the tech player as sole participant
        RunningShow previewShow = new RunningShow(
            show, List.of(player), player,
            /*privateMode=*/ true, /*scenesMode=*/ false, /*followMode=*/ "static"
        );

        // Create scheduler in step mode before start() so no events auto-fire
        ShowScheduler scheduler = new ShowScheduler(
            plugin, cueRegistry, executorRegistry, showManager, previewShow);
        scheduler.setSteppingMode(true);
        scheduler.start();

        cueSession.setPreviewShow(previewShow);
        cueSession.setPreviewScheduler(scheduler);
        cueSession.clearVisitedTicks();

        player.sendMessage(MM.deserialize(
            "<green>Preview started. Press <white>Go</white> to step through cues.</green>"));
        CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
    }

    /**
     * Exit preview mode. Cancels the scheduler and stops the RunningShow.
     * World state changes made by the preview remain — player controls when to reset
     * via the normal Phase 1 DISMISS flow.
     */
    public void exitPreview(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isPreviewActive()) {
            player.sendMessage(MM.deserialize("<yellow>No active preview.</yellow>"));
            return;
        }
        stopPreviewInternal(cueSession);
        player.sendMessage(MM.deserialize("<gray>Preview stopped.</gray>"));
        CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
    }

    private void stopPreviewInternal(TechCueSession cueSession) {
        ShowScheduler scheduler = cueSession.getPreviewScheduler();
        if (scheduler != null) {
            scheduler.setSteppingMode(false); // let task self-cancel via isRunning()
            scheduler.cancel();
        }
        RunningShow preview = cueSession.getPreviewShow();
        if (preview != null) {
            preview.stop();
            // Bossbar cleanup
            if (scheduler != null) scheduler.hideBossBar();
        }
        cueSession.setPreviewShow(null);
        cueSession.setPreviewScheduler(null);
        cueSession.clearVisitedTicks();
    }

    // -----------------------------------------------------------------------
    // Step navigation (hotbar Go / Hold / Back)
    // -----------------------------------------------------------------------

    /**
     * Advance to the next event tick in the preview show.
     * Pushes the dispatched tick onto the visitedTicks stack for Back support.
     * If there are no more events, ends the preview and offers scene navigation.
     */
    public void stepForward(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isPreviewActive()) return;

        ShowScheduler scheduler = cueSession.getPreviewScheduler();
        long dispatched = scheduler.dispatchNextEventTick();

        if (dispatched < 0) {
            CuePanelBuilder.sendEndOfScenePanel(player, cueSession);
        } else {
            cueSession.pushVisitedTick(dispatched);
            CuePanelBuilder.updatePreviewSidebar(player, cueSession);
        }
    }

    /**
     * Step back to the previous event tick.
     *
     * Rebuilds the preview show from scratch and re-dispatches all events up to
     * the previous tick. This is a full restart — world state changes from
     * events fired after the target tick are not undone.
     */
    public void stepBack(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isPreviewActive()) return;

        // Pop current tick, then peek at the one before it
        cueSession.popVisitedTick(); // discard current position
        long prevTick = cueSession.popVisitedTick(); // become current

        if (prevTick < 0) {
            player.sendMessage(MM.deserialize("<gray>Already at the beginning.</gray>"));
            // Put back if nothing to go back to
            return;
        }

        // Rewind: dispatchEventsUpTo requires tick >= current; we need to reset to 0.
        // Restart the preview and seek forward to the target tick.
        stopPreviewInternal(cueSession);
        startPreview(player);

        // Re-seek to prevTick if the preview restarted successfully
        cueSession = getTechCueSession(player);
        if (cueSession != null && cueSession.isPreviewActive()) {
            cueSession.getPreviewScheduler().dispatchEventsUpTo(prevTick);
            cueSession.pushVisitedTick(prevTick);
            player.sendActionBar(MM.deserialize(
                "<gray>◀  " + formatTickForDisplay(prevTick) + "</gray>"));
        }
    }

    /**
     * Hold — signal that the player wants to pause at the current position.
     * In step mode this is already the natural state; Hold is a no-op unless
     * a continuous playback mode is introduced later.
     */
    public void holdPreview(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isPreviewActive()) return;
        cueSession.setHoldActive(true);
        player.sendActionBar(MM.deserialize("<yellow>⏸  Hold</yellow>"));
    }

    // -----------------------------------------------------------------------
    // Save / preset
    // -----------------------------------------------------------------------

    /** Write the current rawYaml back to the show file on disk. */
    public void saveYaml(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) {
            player.sendMessage(MM.deserialize("<red>No active Phase 2 session.</red>"));
            return;
        }
        ShowYamlEditor editor = editorFor(cueSession);
        if (editor.saveToShowYaml()) {
            cueSession.clearDirty();
            player.sendMessage(MM.deserialize(
                "<green>Saved → <white>" + cueSession.getSourceFile().getName() + "</white></green>"));
        } else {
            player.sendMessage(MM.deserialize(
                "<red>Save failed — check server console.</red>"));
        }
    }

    /** Promote a cue's in-session content to the preset library. */
    public void saveAsPreset(Player player, String cueId, String presetId) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) {
            player.sendMessage(MM.deserialize("<red>No active Phase 2 session.</red>"));
            return;
        }
        ShowYamlEditor editor = editorFor(cueSession);
        if (editor.saveAsPreset(cueId, presetId)) {
            player.sendMessage(MM.deserialize(
                "<green>Preset saved: <white>" + presetId + "</white></green>"));
        } else {
            player.sendMessage(MM.deserialize(
                "<red>Preset save failed — check server console.</red>"));
        }
    }

    // -----------------------------------------------------------------------
    // Panel / scene navigation
    // -----------------------------------------------------------------------

    /** Re-send the cue panel for the current scene (panel re-draw command). */
    public void sendCuePanel(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) {
            player.sendMessage(MM.deserialize("<red>No active Phase 2 session.</red>"));
            return;
        }
        CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
    }

    /** Navigate to the next scene in the prompt book. */
    public void nextCueScene(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) return;

        PromptBook book = cueSession.getBook();
        if (book == null || book.scenes().isEmpty()) return;

        String current = cueSession.getCurrentSceneId();
        List<PromptBook.SceneSpec> scenes = book.scenes();
        for (int i = 0; i < scenes.size() - 1; i++) {
            if (scenes.get(i).id().equals(current)) {
                // Stop any active preview before changing scene
                if (cueSession.isPreviewActive()) stopPreviewInternal(cueSession);

                cueSession.setCurrentSceneId(scenes.get(i + 1).id());
                cueSession.setCurrentCueIndex(-1);
                cueSession.clearVisitedTicks();
                CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
                return;
            }
        }
        player.sendMessage(MM.deserialize("<gray>Already at the last scene.</gray>"));
    }

    /** Navigate to the previous scene in the prompt book. */
    public void prevCueScene(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) return;

        PromptBook book = cueSession.getBook();
        if (book == null || book.scenes().isEmpty()) return;

        String current = cueSession.getCurrentSceneId();
        List<PromptBook.SceneSpec> scenes = book.scenes();
        for (int i = 1; i < scenes.size(); i++) {
            if (scenes.get(i).id().equals(current)) {
                if (cueSession.isPreviewActive()) stopPreviewInternal(cueSession);

                cueSession.setCurrentSceneId(scenes.get(i - 1).id());
                cueSession.setCurrentCueIndex(-1);
                cueSession.clearVisitedTicks();
                CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
                return;
            }
        }
        player.sendMessage(MM.deserialize("<gray>Already at the first scene.</gray>"));
    }

    // -----------------------------------------------------------------------
    // Department edit sessions
    // -----------------------------------------------------------------------

    /**
     * Enter a department edit session for the given cue ID.
     * Shows the edit boss bar and sends the save/cancel buttons.
     * Full per-department edit panels are Group 5.
     */
    public void enterDeptEdit(Player player, String cueId) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null) {
            player.sendMessage(MM.deserialize("<red>No active Phase 2 session.</red>"));
            return;
        }
        if (cueSession.isEditing()) {
            player.sendMessage(MM.deserialize(
                "<yellow>An edit session is already active. Use [Save] or [Cancel] first.</yellow>"));
            return;
        }

        // Route to department-specific edit session based on cue ID prefix.
        DeptEditSession editSession = buildDeptEditSession(cueId, cueSession);
        cueSession.setActiveEditSession(editSession);

        CuePanelBuilder.showEditBossBar(player, cueId, cueSession);

        // Send the department panel (or fallback message for unimplemented departments)
        if (editSession instanceof CastingEditSession castingSession) {
            CastingPanelBuilder.sendPanel(player, castingSession);
        } else if (editSession instanceof WardrobeEditSession wardrobeSession) {
            WardrobePanelBuilder.sendPanel(player, wardrobeSession);
        } else if (editSession instanceof SoundEditSession soundSession) {
            SoundPanelBuilder.sendPanel(player, soundSession);
        } else if (editSession instanceof VoiceEditSession voiceSession) {
            VoicePanelBuilder.sendPanel(player, voiceSession);
        } else {
            player.sendMessage(MM.deserialize(
                "<aqua>Editing: <white>" + cueId + "</white></aqua>"
                + "  <gray>(department panel not yet implemented)</gray>"));
        }

        CuePanelBuilder.sendSaveCancelButtons(player);

        // Start periodic button refresh (re-send every 5s so buttons don't scroll away)
        var task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (cueSession.isEditing()) {
                CuePanelBuilder.sendSaveCancelButtons(player);
            }
        }, 100L, 100L);
        cueSession.setButtonRefreshTask(task);

        log.info("[Tech2] " + player.getName() + " entered edit mode for cue: " + cueId);
    }

    /**
     * Commit or promote the active department edit session.
     *
     * @param saveAsPreset if true, triggers [Save as Preset]; otherwise [Save]
     */
    public void commitDeptEdit(Player player, boolean saveAsPreset) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isEditing()) {
            player.sendMessage(MM.deserialize("<yellow>No active edit session.</yellow>"));
            return;
        }

        DeptEditSession editSession = cueSession.getActiveEditSession();
        if (editSession != null) {
            if (saveAsPreset) {
                editSession.onSaveAsPreset();
            } else {
                editSession.onSave();
            }
        }

        cleanupDeptEdit(player, cueSession);
        cueSession.markDirty();
        player.sendMessage(MM.deserialize(saveAsPreset
            ? "<green>Changes saved as preset.</green>"
            : "<green>Changes saved.</green>"));
        CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
    }

    /** Discard the active department edit session. */
    public void cancelDeptEdit(Player player) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isEditing()) {
            player.sendMessage(MM.deserialize("<yellow>No active edit session.</yellow>"));
            return;
        }

        DeptEditSession editSession = cueSession.getActiveEditSession();
        if (editSession != null) editSession.onCancel();

        cleanupDeptEdit(player, cueSession);
        player.sendMessage(MM.deserialize("<gray>Edit cancelled.</gray>"));
        CuePanelBuilder.sendScenePanel(player, cueSession, editorFor(cueSession));
    }

    private void cleanupDeptEdit(Player player, TechCueSession cueSession) {
        CuePanelBuilder.clearEditBossBar(player, cueSession);
        var refreshTask = cueSession.getButtonRefreshTask();
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
            cueSession.setButtonRefreshTask(null);
        }
        cueSession.setActiveEditSession(null);
    }

    // -----------------------------------------------------------------------
    // Phase 2 helpers
    // -----------------------------------------------------------------------

    /**
     * Handle a /scaena tech2 editparam <key> <value> command.
     * Delegates to the active DeptEditSession's onEditParam().
     * If the key is "entity_type_panel", sends the Casting type selector sub-panel.
     */
    public void dispatchEditParam(Player player, String key, String value) {
        TechCueSession cueSession = getTechCueSession(player);
        if (cueSession == null || !cueSession.isEditing()) {
            player.sendMessage(MM.deserialize("<yellow>No active edit session.</yellow>"));
            return;
        }
        // Special: open entity type sub-panel without changing state
        if ("entity_type_panel".equalsIgnoreCase(key)) {
            CastingPanelBuilder.sendEntityTypePanel(player);
            return;
        }
        DeptEditSession editSession = cueSession.getActiveEditSession();
        if (editSession == null || !editSession.onEditParam(key, value)) {
            player.sendMessage(MM.deserialize(
                "<yellow>Unknown param '" + key + "' for this edit session.</yellow>"));
        }
    }

    /**
     * Build the appropriate DeptEditSession for the given cue ID.
     * Routing is by cue ID prefix: "casting." → CastingEditSession.
     * Returns null for unimplemented departments (edit shell still shown).
     */
    private DeptEditSession buildDeptEditSession(String cueId, TechCueSession cueSession) {
        ShowYamlEditor editor = editorFor(cueSession);
        if (cueId.startsWith("casting.")) {
            return new CastingEditSession(
                cueId, cueSession.getPlayer(), cueSession, editor, cueRegistry, log);
        }
        if (cueId.startsWith("wardrobe.")) {
            return new WardrobeEditSession(
                cueId, cueSession.getPlayer(), cueSession, editor, cueRegistry, log);
        }
        if (cueId.startsWith("sound.")) {
            return new SoundEditSession(
                cueId, cueSession.getPlayer(), cueSession, editor, cueRegistry, log);
        }
        if (cueId.startsWith("voice.")) {
            return new VoiceEditSession(
                cueId, cueSession.getPlayer(), cueSession, editor, cueRegistry, log);
        }
        if (cueId.startsWith("effects.")) {
            return new EffectsEditSession(
                cueId, cueSession.getPlayer(), cueSession, editor, cueRegistry, log);
        }
        // TODO (Group 5): add Fireworks, Lighting,
        //   Camera, Choreography, Set as each department is implemented.
        return null;
    }

    /** Build a ShowYamlEditor for the given session. */
    private ShowYamlEditor editorFor(TechCueSession cueSession) {
        return new ShowYamlEditor(
            cueSession.getRawYaml(),
            cueSession.getSourceFile(),
            cueSession.getBook(),
            cueRegistry,
            log
        );
    }

    /** Locate the show YAML file for a given showId (folder or flat layout). */
    private File resolveShowYamlFile(String showId) {
        File dataFolder = plugin.getDataFolder();
        // Prefer show folder: shows/[showId]/[showId].yml
        File folderYaml = new File(dataFolder, "shows/" + showId + "/" + showId + ".yml");
        if (folderYaml.exists()) return folderYaml;
        // Fall back to flat: shows/[showId].yml
        File flatYaml = new File(dataFolder, "shows/" + showId + ".yml");
        if (flatYaml.exists()) return flatYaml;
        return null;
    }

    /** Format an absolute tick as "Xs | Nt" for player-facing messages. */
    private static String formatTickForDisplay(long tick) {
        double seconds = tick / 20.0;
        String secStr = (seconds == Math.floor(seconds))
            ? String.valueOf((long) seconds)
            : String.valueOf(seconds);
        return secStr + "s | " + tick + "t";
    }

    // -----------------------------------------------------------------------
    // Inner record: capture file entry (richer than ScoutCapture for block coords)
    // -----------------------------------------------------------------------

    record CaptureEntry(
        String  world,
        double  x,
        double  y,
        double  z,
        float   yaw,
        float   pitch,
        Integer blockX,   // explicit block coords from capture file; null = derive from xyz
        Integer blockY,
        Integer blockZ
    ) {}
}
