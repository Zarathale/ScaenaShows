package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Tracks block changes made by the player while in tech build mode.
 *
 * Rules:
 *   - If the player has no active TechSession or build mode is off → pass through (no-op).
 *   - If the block is OUTSIDE the scene's bounding box → allow the change but do not track it.
 *     A brief actionbar note is shown so the player knows why it wasn't recorded.
 *   - If the block IS inside the bbox → record the change and allow it.
 *
 * Priority HIGH so we run after most protection plugins but before default handlers.
 * ignoreCancelled = true so we don't double-process already-blocked events.
 */
public final class BlockBuildListener implements Listener {

    private static final TextColor COL_WARN  = TextColor.color(0xFFFF55); // yellow
    private static final TextColor COL_INFO  = TextColor.color(0x55FF55); // green

    private final TechManager manager;

    public BlockBuildListener(TechManager manager) {
        this.manager = manager;
    }

    // -----------------------------------------------------------------------
    // Block place
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        TechSession session = manager.getSession(player);
        if (session == null || !session.buildMode()) return;

        Block block = event.getBlock();
        if (!isInBbox(session, block)) {
            // Outside bbox — allow but don't track; nudge the player
            player.sendActionBar(Component.text(
                "Outside set bounds — not tracked", COL_WARN));
            return;
        }

        SetBuildSession build = ensureBuildSession(session);

        // before = the block that was replaced (from the replaced block state)
        // after  = the newly placed block
        build.record(
            block.getWorld(), block.getX(), block.getY(), block.getZ(),
            event.getBlockReplacedState().getBlockData().clone(),
            block.getBlockData().clone()
        );
    }

    // -----------------------------------------------------------------------
    // Block break
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        TechSession session = manager.getSession(player);
        if (session == null || !session.buildMode()) return;

        Block block = event.getBlock();
        if (!isInBbox(session, block)) {
            player.sendActionBar(Component.text(
                "Outside set bounds — not tracked", COL_WARN));
            return;
        }

        SetBuildSession build = ensureBuildSession(session);

        // before = current block state; after = AIR (removal)
        build.record(
            block.getWorld(), block.getX(), block.getY(), block.getZ(),
            block.getBlockData().clone(),
            Bukkit.createBlockData(Material.AIR)
        );
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Returns true if the block falls within the scene's defined bounding box.
     * Returns false (don't track) if the scene has no bbox defined — this
     * shouldn't normally occur since TechManager checks before entering build mode,
     * but is safe as a fallback.
     */
    private static boolean isInBbox(TechSession session, Block block) {
        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        if (scene == null || !scene.hasSet()) return false;
        PromptBook.DeptSet set = scene.set();
        if (!set.hasBbox()) {
            // Check pending bbox corners (set this session, not yet saved)
            return isPendingBbox(session, block);
        }

        PromptBook.BboxPoint mn = set.bboxMin();
        PromptBook.BboxPoint mx = set.bboxMax();

        // World check
        if (!mn.world().equals(block.getWorld().getName())) return false;

        int x = block.getX(), y = block.getY(), z = block.getZ();
        return x >= Math.min(mn.x(), mx.x()) && x <= Math.max(mn.x(), mx.x())
            && y >= Math.min(mn.y(), mx.y()) && y <= Math.max(mn.y(), mx.y())
            && z >= Math.min(mn.z(), mx.z()) && z <= Math.max(mn.z(), mx.z());
    }

    /**
     * Fallback: check against pending (unsaved) bbox corners held in the session.
     */
    private static boolean isPendingBbox(TechSession session, Block block) {
        org.bukkit.Location mn = session.pendingBboxMin();
        org.bukkit.Location mx = session.pendingBboxMax();
        if (mn == null || mx == null) return false;
        if (!mn.getWorld().getName().equals(block.getWorld().getName())) return false;

        int x = block.getX(), y = block.getY(), z = block.getZ();
        return x >= Math.min(mn.getBlockX(), mx.getBlockX())
            && x <= Math.max(mn.getBlockX(), mx.getBlockX())
            && y >= Math.min(mn.getBlockY(), mx.getBlockY())
            && y <= Math.max(mn.getBlockY(), mx.getBlockY())
            && z >= Math.min(mn.getBlockZ(), mx.getBlockZ())
            && z <= Math.max(mn.getBlockZ(), mx.getBlockZ());
    }

    /** Lazily create the SetBuildSession if it doesn't exist yet. */
    private static SetBuildSession ensureBuildSession(TechSession session) {
        if (session.activeBuildSession() == null) {
            session.setActiveBuildSession(new SetBuildSession(session.currentSceneId()));
        }
        return session.activeBuildSession();
    }
}
