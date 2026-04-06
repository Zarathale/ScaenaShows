package com.scaena.shows.tech;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

// Phase 2
import com.scaena.shows.tech.TechCueSession;

/**
 * Handles all in-session input events for Tech Rehearsal Mode.
 *
 * Hotbar routing (right-click with each reserved slot):
 *   Slot 5 (index 4) — Prev Scene
 *   Slot 6 (index 5) — Hold (Phase 2 stub — no-op in Phase 1)
 *   Slot 7 (index 6) — Next Scene
 *   Slot 8 (index 7) — Capture (confirm if in capture mode; open mark list otherwise)
 *   Slot 9 (index 8) — Params: right-click = increment/open; shift+right-click = decrement
 *
 * Also:
 *   - Blocks item drop for tech hotbar items
 *   - Blocks off-hand swap for tech hotbar items
 *   - Intercepts AsyncChatEvent for text param input
 */
@SuppressWarnings("UnstableApiUsage")
public final class TechHotbarListener implements Listener {

    private final TechManager manager;
    private final JavaPlugin  plugin;

    public TechHotbarListener(TechManager manager, JavaPlugin plugin) {
        this.manager = manager;
        this.plugin  = plugin;
    }

    // -----------------------------------------------------------------------
    // Hotbar right-click routing
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only right-click actions
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        var player = event.getPlayer();
        TechSession session = manager.getSession(player);
        if (session == null) return;

        // Cancel the interaction to prevent block placement etc.
        event.setCancelled(true);

        int slot  = player.getInventory().getHeldItemSlot();
        boolean shift = player.isSneaking();

        // Phase 2: if a TechCueSession is active, re-route PREV/HOLD/NEXT
        TechCueSession cueSession = manager.getTechCueSession(player);
        if (cueSession != null) {
            // All Phase 2 navigation is suspended during a department edit session
            if (cueSession.isEditing()) return;

            switch (slot) {
                case TechManager.SLOT_PREV -> manager.stepBack(player);
                case TechManager.SLOT_HOLD -> manager.holdPreview(player);
                case TechManager.SLOT_NEXT -> manager.stepForward(player);
                case TechManager.SLOT_CAPTURE -> manager.confirmCapture(player);
                case TechManager.SLOT_PARAMS  -> {
                    if (shift) {
                        manager.decrementParam(player);
                    } else {
                        if (session.paramScrollMode()) {
                            manager.incrementParam(player);
                        } else {
                            manager.paramAction(player);
                        }
                    }
                }
                default -> {}
            }
            return; // Phase 2 handled — don't fall through to Phase 1
        }

        switch (slot) {
            case TechManager.SLOT_PREV    -> manager.prevScene(player);
            case TechManager.SLOT_HOLD    -> { /* no-op in Phase 1 */ }
            case TechManager.SLOT_NEXT    -> manager.nextScene(player);
            case TechManager.SLOT_CAPTURE -> manager.confirmCapture(player);
            case TechManager.SLOT_PARAMS  -> {
                if (shift) {
                    manager.decrementParam(player);
                } else {
                    // If in scroll mode, increment; otherwise open param panel
                    if (session.paramScrollMode()) {
                        manager.incrementParam(player);
                    } else {
                        manager.paramAction(player);
                    }
                }
            }
            default -> { /* Not a tech slot — let through */ }
        }
    }

    // -----------------------------------------------------------------------
    // Prevent dropping tech hotbar items
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        TechSession session = manager.getSession(event.getPlayer());
        if (session == null) return;
        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        if (isTechSlot(slot)) event.setCancelled(true);
    }

    // -----------------------------------------------------------------------
    // Prevent F swapping tech hotbar items to off-hand
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        TechSession session = manager.getSession(event.getPlayer());
        if (session == null) return;
        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        if (isTechSlot(slot)) event.setCancelled(true);
    }

    // -----------------------------------------------------------------------
    // Text param input via chat
    // -----------------------------------------------------------------------

    /**
     * Intercepts chat messages when a player has a pending text param input.
     * This event fires async — we cancel the chat and route to the main thread.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        TechSession session = manager.getSession(event.getPlayer());
        if (session == null || !session.awaitingTextInput()) return;

        event.setCancelled(true);
        String value = PlainTextComponentSerializer.plainText()
            .serialize(event.message());

        // Route to main thread before touching Bukkit API
        var player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () ->
            manager.applyTextParam(player, value));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean isTechSlot(int slot) {
        return slot >= TechManager.SLOT_PREV && slot <= TechManager.SLOT_PARAMS;
    }
}
