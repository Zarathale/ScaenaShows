package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.SoundEvents.SoundEvent;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles SOUND events.
 * Uses the player's location so every participant hears the sound at full volume.
 */
public final class SoundEventExecutor implements EventExecutor {

    private final JavaPlugin plugin;
    private final Logger log;

    public SoundEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        if (!(event instanceof SoundEvent e)) return;

        SoundCategory category = parseCategory(e.category);
        List<Player> audience = AudienceResolver.resolve("participants", show);

        for (Player p : audience) {
            p.playSound(p.getLocation(), e.soundId, category, e.volume, e.pitch);
        }

        // Optional: stop sound after maxDurationTicks
        if (e.maxDurationTicks > 0) {
            String soundId = e.soundId;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : AudienceResolver.resolve("participants", show)) {
                        p.stopSound(soundId, category);
                    }
                }
            }.runTaskLater(plugin, e.maxDurationTicks);
        }
    }

    private SoundCategory parseCategory(String raw) {
        try {
            return SoundCategory.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SoundCategory.MASTER;
        }
    }
}
