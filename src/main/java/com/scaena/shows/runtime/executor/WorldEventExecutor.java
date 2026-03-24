package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.WorldEvents.*;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

/**
 * Handles WEATHER, TIME_OF_DAY, REDSTONE events.
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
            case WEATHER     -> handleWeather((WeatherEvent) event, show);
            case TIME_OF_DAY -> handleTimeOfDay((TimeOfDayEvent) event, show);
            case REDSTONE    -> handleRedstone((RedstoneEvent) event, show);
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
}
