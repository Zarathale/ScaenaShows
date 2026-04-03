package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.WorldEvents.*;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles WEATHER, TIME_OF_DAY, REDSTONE, BLOCK_PLACE, BLOCK_REMOVE, BLOCK_STATE,
 * SET_ITEM_FRAME events.
 */
public final class WorldEventExecutor implements EventExecutor {

    private final JavaPlugin plugin;
    private final Logger log;

    public WorldEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case WEATHER        -> handleWeather((WeatherEvent) event, show);
            case TIME_OF_DAY    -> handleTimeOfDay((TimeOfDayEvent) event, show);
            case REDSTONE       -> handleRedstone((RedstoneEvent) event, show);
            case BLOCK_PLACE    -> handleBlockPlace((BlockPlaceEvent) event, show);
            case BLOCK_REMOVE   -> handleBlockRemove((BlockRemoveEvent) event, show);
            case BLOCK_STATE    -> handleBlockState((BlockStateEvent) event, show);
            case SET_ITEM_FRAME -> handleSetItemFrame((SetItemFrameEvent) event, show);  // OPS-007
            default -> {}
        }
    }

    private void handleWeather(WeatherEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        World world = anchor.getWorld();

        switch (e.state.toLowerCase()) {
            case "clear" -> {
                world.setStorm(false);
                world.setThundering(false);
                if (e.durationTicks > 0) world.setWeatherDuration(e.durationTicks);
            }
            case "storm" -> {
                world.setStorm(true);
                world.setThundering(false);
                if (e.durationTicks > 0) world.setWeatherDuration(e.durationTicks);
            }
            case "thunder" -> {
                world.setStorm(true);
                world.setThundering(true);
                if (e.durationTicks > 0) world.setThunderDuration(e.durationTicks);
            }
        }
    }

    private void handleTimeOfDay(TimeOfDayEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        anchor.getWorld().setTime(e.time);
    }

    private void handleRedstone(RedstoneEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        World world = anchor.getWorld();
        Block block = world.getBlockAt(e.targetX, e.targetY, e.targetZ);

        if (block.getBlockData() instanceof Powerable powerable) {
            powerable.setPowered(e.state);
            block.setBlockData(powerable);
        } else {
            log.warning("[ScaenaShows] REDSTONE target at " + e.targetX + "," + e.targetY + "," + e.targetZ
                + " is not a Powerable block (" + block.getType() + ")");
        }
    }

    // -----------------------------------------------------------------------
    // BLOCK_PLACE  (OPS-004)
    // -----------------------------------------------------------------------

    private void handleBlockPlace(BlockPlaceEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        World world = anchor.getWorld();
        Block block = world.getBlockAt(e.targetX, e.targetY, e.targetZ);

        // Record original state before we touch the block
        show.recordBlockRestore(world, e.targetX, e.targetY, e.targetZ,
                block.getBlockData());

        try {
            BlockData newData = Bukkit.createBlockData(e.block);
            block.setBlockData(newData, false); // false = no physics update
        } catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] BLOCK_PLACE: invalid block string '"
                    + e.block + "': " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // BLOCK_REMOVE  (OPS-004)
    // -----------------------------------------------------------------------

    private void handleBlockRemove(BlockRemoveEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        World world = anchor.getWorld();
        Block block = world.getBlockAt(e.targetX, e.targetY, e.targetZ);

        // Record original state before we touch the block
        show.recordBlockRestore(world, e.targetX, e.targetY, e.targetZ,
                block.getBlockData());

        block.setBlockData(Bukkit.createBlockData(Material.AIR), false);
    }

    // -----------------------------------------------------------------------
    // BLOCK_STATE  (OPS-008)
    // Patches state properties (e.g., lit=true) on an existing block without
    // changing its block type.
    // -----------------------------------------------------------------------

    private void handleBlockState(BlockStateEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        World world = anchor.getWorld();
        Block block = world.getBlockAt(e.targetX, e.targetY, e.targetZ);

        // Record original state before we touch the block
        show.recordBlockRestore(world, e.targetX, e.targetY, e.targetZ,
                block.getBlockData());

        BlockData patched = applyStateOverrides(block.getBlockData(), e.state);
        if (patched != null) {
            block.setBlockData(patched, false);
        } else {
            log.warning("[ScaenaShows] BLOCK_STATE: could not apply state overrides to "
                    + block.getType() + " at " + e.targetX + "," + e.targetY + "," + e.targetZ);
        }
    }

    // -----------------------------------------------------------------------
    // SET_ITEM_FRAME  (OPS-007)
    // Point-in-time: set displayed item, and optionally visibility, fixed state,
    // and rotation on a named item frame.
    //
    // entity:world targets: snapshot original state for restore on show end.
    // entity:spawned targets: no restore needed (despawned at show end).
    // -----------------------------------------------------------------------

    private void handleSetItemFrame(SetItemFrameEvent e, RunningShow show) {
        boolean isWorldTarget = e.target.startsWith("entity:world:");

        ItemFrame frame = resolveItemFrame(e.target, show);
        if (frame == null) {
            log.warning("[ScaenaShows] SET_ITEM_FRAME: item frame not found: " + e.target);
            return;
        }

        // Snapshot original state before first modification (entity:world targets only)
        if (isWorldTarget) {
            ItemStack originalItem = frame.getItem().clone(); // AIR if empty
            show.recordItemFrameRestore(
                frame.getUniqueId(),
                new RunningShow.ItemFrameSnapshot(
                    originalItem,
                    frame.isVisible(),
                    frame.isFixed(),
                    frame.getRotation()
                )
            );
        }

        // Apply item — minecraft:air clears the frame
        if (e.item != null) {
            if ("minecraft:air".equalsIgnoreCase(e.item)) {
                frame.setItem(null);
            } else {
                String matName = e.item.startsWith("minecraft:")
                    ? e.item.substring("minecraft:".length()).toUpperCase()
                    : e.item.toUpperCase();
                Material mat = Material.matchMaterial(matName);
                if (mat == null) {
                    log.warning("[ScaenaShows] SET_ITEM_FRAME: unknown material '" + e.item + "'");
                } else {
                    frame.setItem(new ItemStack(mat));
                }
            }
        }

        // Apply optional frame state fields
        if (e.visible  != null) frame.setVisible(e.visible);
        if (e.fixed    != null) frame.setFixed(e.fixed);
        if (e.rotation != null) {
            ItemFrame.Rotation[] rotations = ItemFrame.Rotation.values();
            int idx = Math.max(0, Math.min(e.rotation, rotations.length - 1));
            frame.setRotation(rotations[idx]);
        }
    }

    /**
     * Resolve an entity:world or entity:spawned target string to an ItemFrame.
     * Returns null if not found or the resolved entity is not an ItemFrame.
     */
    private ItemFrame resolveItemFrame(String target, RunningShow show) {
        if (target == null) return null;

        if (target.startsWith("entity:world:")) {
            String customName = target.substring("entity:world:".length());
            Location anchor = show.getAnchorLocation();
            if (anchor == null || anchor.getWorld() == null) return null;
            for (Entity ent : anchor.getWorld().getEntities()) {
                if (ent instanceof ItemFrame frame && customName.equals(ent.getCustomName())) {
                    return frame;
                }
            }
            return null;
        }

        if (target.startsWith("entity:spawned:")) {
            String name = target.substring("entity:spawned:".length());
            Entity ent = show.getSpawnedEntity(name);
            return ent instanceof ItemFrame frame ? frame : null;
        }

        return null;
    }

    /**
     * Merge the given state overrides into a copy of {@code current} by rebuilding
     * the blockstate string.  Returns null on parse failure.
     *
     * Strategy: decompose the getAsString() result (e.g.
     * {@code "minecraft:blast_furnace[facing=north,lit=false]"}), replace matching
     * keys, then call {@code Bukkit.createBlockData()} on the result.  Using the
     * string API means we work generically across all block types without needing
     * to cast to specific interfaces.
     */
    private BlockData applyStateOverrides(BlockData current,
                                          Map<String, String> overrides) {
        if (overrides.isEmpty()) return current;

        String raw = current.getAsString(false); // e.g. "minecraft:blast_furnace[facing=north,lit=false]"
        int bracketOpen = raw.indexOf('[');

        String blockId;
        Map<String, String> stateMap = new LinkedHashMap<>();

        if (bracketOpen < 0) {
            // Block has no state properties at all — overrides cannot be applied
            blockId = raw;
        } else {
            blockId = raw.substring(0, bracketOpen);
            String stateStr = raw.substring(bracketOpen + 1, raw.length() - 1);
            if (!stateStr.isEmpty()) {
                for (String pair : stateStr.split(",")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) stateMap.put(kv[0], kv[1]);
                }
            }
        }

        stateMap.putAll(overrides); // apply overrides (add or replace)

        // Rebuild blockstate string
        StringBuilder sb = new StringBuilder(blockId);
        if (!stateMap.isEmpty()) {
            sb.append('[');
            boolean first = true;
            for (Map.Entry<String, String> entry : stateMap.entrySet()) {
                if (!first) sb.append(',');
                sb.append(entry.getKey()).append('=').append(entry.getValue());
                first = false;
            }
            sb.append(']');
        }

        try {
            return Bukkit.createBlockData(sb.toString());
        } catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] BLOCK_STATE: invalid merged blockstate '"
                    + sb + "': " + ex.getMessage());
            return null;
        }
    }
}
