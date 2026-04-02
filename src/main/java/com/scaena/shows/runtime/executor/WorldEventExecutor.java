package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.WorldEvents.*;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles WEATHER, TIME_OF_DAY, REDSTONE, BLOCK_PLACE, BLOCK_REMOVE, BLOCK_STATE events.
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
            case WEATHER      -> handleWeather((WeatherEvent) event, show);
            case TIME_OF_DAY  -> handleTimeOfDay((TimeOfDayEvent) event, show);
            case REDSTONE     -> handleRedstone((RedstoneEvent) event, show);
            case BLOCK_PLACE  -> handleBlockPlace((BlockPlaceEvent) event, show);
            case BLOCK_REMOVE -> handleBlockRemove((BlockRemoveEvent) event, show);
            case BLOCK_STATE  -> handleBlockState((BlockStateEvent) event, show);
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
