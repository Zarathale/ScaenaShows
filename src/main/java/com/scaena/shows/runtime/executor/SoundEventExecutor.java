package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.SoundEvents;
import com.scaena.shows.model.event.SoundEvents.SoundEvent;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.RunningShow;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles SOUND and STOP_SOUND events.
 *
 * SOUND:      plays a named sound at each participant's location.
 * STOP_SOUND: immediately cuts all sounds from a given source (no fade).
 *             Pair with a transitional sound (chime, boom) at the same tick
 *             so the cut feels intentional rather than broken.
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
        if (event instanceof SoundEvents.StopSoundEvent e) {
            executeStop(e, show);
        } else if (event instanceof SoundEvent e) {
            executePlay(e, show);
        }
    }

    // ------------------------------------------------------------------
    // SOUND — play
    // ------------------------------------------------------------------
    private void executePlay(SoundEvent e, RunningShow show) {
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

    // ------------------------------------------------------------------
    // STOP_SOUND — cut
    // ------------------------------------------------------------------
    private void executeStop(SoundEvents.StopSoundEvent e, RunningShow show) {
        List<Player> audience = AudienceResolver.resolve("participants", show);
        for (Player p : audience) {
            stopSoundsForPlayer(p, e.source);
        }
    }

    private void stopSoundsForPlayer(Player p, String source) {
        if (source.equalsIgnoreCase("all")) {
            p.stopSound(SoundStop.all());
            return;
        }
        try {
            Sound.Source src = Sound.Source.valueOf(source.toUpperCase());
            p.stopSound(SoundStop.source(src));
        } catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] STOP_SOUND: unknown source '" + source
                + "'. Valid values: music, ambient, neutral, player, block, hostile, master, all");
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
