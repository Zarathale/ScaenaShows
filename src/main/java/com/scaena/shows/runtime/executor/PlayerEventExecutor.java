package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.ShowSet;
import com.scaena.shows.model.event.PlayerEvents.*;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles player movement events:
 * PLAYER_TELEPORT, PLAYER_VELOCITY, PLAYER_SPECTATE,
 * PLAYER_SPECTATE_END, PLAYER_MOUNT, PLAYER_DISMOUNT
 */
public final class PlayerEventExecutor implements EventExecutor {

    private final JavaPlugin plugin;
    private final Logger log;

    public PlayerEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case PLAYER_TELEPORT      -> handleTeleport((PlayerTeleportEvent) event, show);
            case PLAYER_VELOCITY      -> handleVelocity((PlayerVelocityEvent) event, show);
            case PLAYER_SPECTATE      -> handleSpectate((PlayerSpectateEvent) event, show);
            case PLAYER_SPECTATE_END  -> handleSpectateEnd((PlayerSpectateEndEvent) event, show);
            case PLAYER_MOUNT         -> handleMount((PlayerMountEvent) event, show);
            case PLAYER_DISMOUNT      -> handleDismount((PlayerDismountEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_TELEPORT
    // ------------------------------------------------------------------
    private void handleTeleport(PlayerTeleportEvent e, RunningShow show) {
        List<Player> audience = AudienceResolver.resolve(e.audience, show);

        for (Player p : audience) {
            Location dest = null;

            if (e.destination != null && e.destination.startsWith("set:")) {
                String setName = e.destination.substring(4);
                ShowSet set = show.show.sets.get(setName);
                if (set != null) {
                    World world = Bukkit.getWorld(set.world());
                    if (world != null) {
                        dest = new Location(world, set.x(), set.y(), set.z(),
                            set.yaw(), set.pitch());
                    }
                }
                if (dest == null) {
                    log.warning("[ScaenaShows] PLAYER_TELEPORT: unknown set '" + setName + "'");
                    continue;
                }
            } else if (e.hasOffset) {
                dest = p.getLocation().clone().add(e.offsetX, e.offsetY, e.offsetZ);
            }

            if (dest == null) continue;

            if (!Float.isNaN(e.yaw))   dest.setYaw(e.yaw);
            if (!Float.isNaN(e.pitch)) dest.setPitch(e.pitch);
            p.teleport(dest);
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_VELOCITY
    // ------------------------------------------------------------------
    private void handleVelocity(PlayerVelocityEvent e, RunningShow show) {
        Vector v = new Vector(e.vecX, e.vecY, e.vecZ);
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            p.setVelocity(v);
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_SPECTATE
    // Temporarily switches players to SPECTATOR so setSpectatorTarget() works.
    // Records prior gamemode for restoration on show end or PLAYER_SPECTATE_END.
    // ------------------------------------------------------------------
    private void handleSpectate(PlayerSpectateEvent e, RunningShow show) {
        Entity target = show.getSpawnedEntity(
            e.entity.startsWith("entity:spawned:") ? e.entity.substring(15) : e.entity);
        if (target == null) {
            log.warning("[ScaenaShows] PLAYER_SPECTATE: entity not found: " + e.entity);
            return;
        }

        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            // Record prior gamemode so we can restore it
            show.recordSpectateRestore(p.getUniqueId(), p.getGameMode());
            p.setGameMode(GameMode.SPECTATOR);
            p.setSpectatorTarget(target);
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_SPECTATE_END
    // ------------------------------------------------------------------
    private void handleSpectateEnd(PlayerSpectateEndEvent e, RunningShow show) {
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            p.setSpectatorTarget(null);
            GameMode prior = show.getSpectateRestoreMap().remove(p.getUniqueId());
            p.setGameMode(prior != null ? prior : GameMode.SURVIVAL);
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_MOUNT
    // ------------------------------------------------------------------
    private void handleMount(PlayerMountEvent e, RunningShow show) {
        Entity mountEntity = show.getSpawnedEntity(
            e.entity.startsWith("entity:spawned:") ? e.entity.substring(15) : e.entity);
        if (mountEntity == null) {
            log.warning("[ScaenaShows] PLAYER_MOUNT: entity not found: " + e.entity);
            return;
        }
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            mountEntity.addPassenger(p);
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_DISMOUNT
    // ------------------------------------------------------------------
    private void handleDismount(PlayerDismountEvent e, RunningShow show) {
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            if (p.isInsideVehicle()) p.leaveVehicle();
        }
    }
}
