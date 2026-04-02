package com.scaena.shows.tech;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.*;

/**
 * Records all block changes made by the player during a build-mode session
 * within the scene's bounding box.
 *
 * Each location is tracked by a canonical key (world:x:y:z). If the same
 * block is changed more than once in a session, the original `before` state
 * is preserved — only `after` is updated. This ensures the final diff always
 * describes the net change from the world's pre-session state.
 *
 * Two categories:
 *   additions  — blocks placed (before=AIR or prior block, after=new block)
 *   removals   — blocks broken (before=old block, after=AIR)
 *
 * A change that cancels out (placed then broken, or broken then the same block
 * placed back) is still retained but will write a no-op diff entry. The writer
 * skips entries where before.equals(after).
 */
public final class SetBuildSession {

    private final String sceneId;

    /** Ordered map so YAML output is stable / readable. */
    private final Map<String, BlockChange> changes = new LinkedHashMap<>();

    public SetBuildSession(String sceneId) {
        this.sceneId = sceneId;
    }

    public String sceneId() { return sceneId; }

    // -----------------------------------------------------------------------
    // Recording
    // -----------------------------------------------------------------------

    /**
     * Record a block change at the given location.
     * If this location was already changed earlier in the session, the original
     * `before` is preserved and only `after` is updated.
     */
    public void record(World world, int x, int y, int z,
                       BlockData before, BlockData after) {
        String key = key(world, x, y, z);
        BlockChange existing = changes.get(key);
        if (existing != null) {
            // Keep the original before; update only after
            changes.put(key, new BlockChange(world, x, y, z, existing.before(), after.clone()));
        } else {
            changes.put(key, new BlockChange(world, x, y, z, before.clone(), after.clone()));
        }
    }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    public Collection<BlockChange> changes() { return Collections.unmodifiableCollection(changes.values()); }

    /** Net changes only — entries where before.equals(after) are skipped. */
    public List<BlockChange> netChanges() {
        return changes.values().stream()
            .filter(c -> !c.before().getAsString().equals(c.after().getAsString()))
            .toList();
    }

    public List<BlockChange> removals() {
        return netChanges().stream()
            .filter(c -> c.after().getMaterial() == Material.AIR)
            .toList();
    }

    public List<BlockChange> additions() {
        return netChanges().stream()
            .filter(c -> c.after().getMaterial() != Material.AIR)
            .toList();
    }

    public boolean isEmpty()      { return netChanges().isEmpty(); }
    public boolean hasRemovals()  { return !removals().isEmpty(); }

    public int totalCount() { return netChanges().size(); }

    // -----------------------------------------------------------------------
    // Restore helpers
    // -----------------------------------------------------------------------

    /**
     * Physically restores all removed blocks to their before state.
     * Called when the player chooses "Restore" at build-mode exit.
     */
    public void restoreRemovals() {
        for (BlockChange c : removals()) {
            org.bukkit.block.Block block = c.world().getBlockAt(c.x(), c.y(), c.z());
            block.setBlockData(c.before(), false);
        }
    }

    // -----------------------------------------------------------------------
    // Inner record
    // -----------------------------------------------------------------------

    public record BlockChange(
        World     world,
        int       x,
        int       y,
        int       z,
        BlockData before,   // state before the session touched this block
        BlockData after     // state after the last change in this session
    ) {}

    // -----------------------------------------------------------------------
    // Static helpers
    // -----------------------------------------------------------------------

    public static String key(World world, int x, int y, int z) {
        return world.getName() + ":" + x + ":" + y + ":" + z;
    }
}
