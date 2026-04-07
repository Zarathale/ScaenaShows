package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.Mark;
import com.scaena.shows.model.ShowSet;
import com.scaena.shows.model.event.PlayerEvents.*;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles player movement events:
 * PLAYER_TELEPORT, PLAYER_VELOCITY, PLAYER_SPECTATE,
 * PLAYER_SPECTATE_END, PLAYER_MOUNT, PLAYER_DISMOUNT, PLAYER_FLIGHT
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
            case PLAYER_FLIGHT        -> handleFlight((PlayerFlightEvent) event, show);
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
    //
    // Phase 2 spawn: mode — spawns the camera entity at anchor+offset, makes it
    // invisible, registers it in the show's spawned entity map, then attaches spectate.
    // ------------------------------------------------------------------
    private void handleSpectate(PlayerSpectateEvent e, RunningShow show) {
        Entity target;
        if (e.spawnMode) {
            target = spawnDroneEntity(e.spawnName, e.spawnType,
                e.spawnOffX, e.spawnOffY, e.spawnOffZ, true, show);
        } else {
            String name = e.entity.startsWith("entity:spawned:") ? e.entity.substring(15) : e.entity;
            target = show.getSpawnedEntity(name);
        }

        if (target == null) {
            log.warning("[ScaenaShows] PLAYER_SPECTATE: entity not found/spawned: "
                + (e.spawnMode ? e.spawnName : e.entity));
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
    //
    // Phase 2 destination field: restore | mark:Name | entity:spawned:Name
    // ------------------------------------------------------------------
    private void handleSpectateEnd(PlayerSpectateEndEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();

        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            p.setSpectatorTarget(null);
            GameMode prior = show.getSpectateRestoreMap().remove(p.getUniqueId());
            p.setGameMode(prior != null ? prior : GameMode.SURVIVAL);

            String dest = e.destination;
            if (dest != null && !dest.isEmpty() && !"restore".equalsIgnoreCase(dest)) {
                Location teleportTo = null;
                if (dest.startsWith("mark:") && anchor != null) {
                    String markName = dest.substring(5);
                    com.scaena.shows.model.Mark mark = show.show.marks.get(markName);
                    if (mark != null) {
                        teleportTo = new Location(anchor.getWorld(),
                            anchor.getX() + mark.x(),
                            anchor.getY() + mark.y(),
                            anchor.getZ() + mark.z());
                    }
                } else if (dest.startsWith("entity:spawned:")) {
                    Entity droneLoc = show.getSpawnedEntity(dest.substring(15));
                    if (droneLoc != null) teleportTo = droneLoc.getLocation().clone();
                }
                if (teleportTo != null) p.teleport(teleportTo);
            }
            // destination == "restore" or empty: player body already restored by gamemode switch
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_MOUNT
    //
    // Phase 2 spawn: mode — spawns a rideable entity and mounts the player on it.
    // ------------------------------------------------------------------
    private void handleMount(PlayerMountEvent e, RunningShow show) {
        Entity mountEntity;
        if (e.spawnMode) {
            mountEntity = spawnDroneEntity(e.spawnName, e.spawnType,
                e.spawnOffX, e.spawnOffY, e.spawnOffZ, e.spawnInvisible, show);
        } else {
            String name = e.entity.startsWith("entity:spawned:") ? e.entity.substring(15) : e.entity;
            mountEntity = show.getSpawnedEntity(name);
        }

        if (mountEntity == null) {
            log.warning("[ScaenaShows] PLAYER_MOUNT: entity not found/spawned: "
                + (e.spawnMode ? e.spawnName : e.entity));
            return;
        }
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            mountEntity.addPassenger(p);
        }
    }

    /**
     * Spawn an entity for Phase 2 PLAYER_SPECTATE or PLAYER_MOUNT spawn: mode.
     * The entity is placed at anchor + offset, optionally made invisible, and
     * registered in the show's spawned entity map.
     *
     * @param name      name to register under (entity:spawned:Name key)
     * @param typeName  EntityType name (e.g. "ARMOR_STAND")
     * @param invisible if true, make the entity invisible via metadata
     */
    private Entity spawnDroneEntity(String name, String typeName,
                                     double offX, double offY, double offZ,
                                     boolean invisible, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) {
            log.warning("[ScaenaShows] spawn: mode — no anchor location available");
            return null;
        }

        org.bukkit.entity.EntityType entityType;
        try {
            entityType = org.bukkit.entity.EntityType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] spawn: mode — unknown EntityType: " + typeName);
            return null;
        }

        Location spawnLoc = anchor.clone().add(offX, offY, offZ);
        Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);

        if (invisible) {
            entity.setInvisible(true);
        }

        if (!name.isEmpty()) {
            show.registerSpawnedEntity(name, entity);
        }
        return entity;
    }

    // ------------------------------------------------------------------
    // PLAYER_DISMOUNT
    // ------------------------------------------------------------------
    private void handleDismount(PlayerDismountEvent e, RunningShow show) {
        for (Player p : AudienceResolver.resolve(e.audience, show)) {
            if (p.isInsideVehicle()) p.leaveVehicle();
        }
    }

    // ------------------------------------------------------------------
    // PLAYER_FLIGHT
    // hover  — record pre-show state, then setAllowFlight(true) + setFlying(true).
    //          Player is frozen at their current Y until release or show end.
    // release — apply transition effect (slow_falling or levitate) BEFORE disabling
    //           flight, ensuring no abrupt drop. Then restore pre-show flight state.
    // ------------------------------------------------------------------
    private void handleFlight(PlayerFlightEvent e, RunningShow show) {
        List<Player> audience = AudienceResolver.resolve(e.audience, show);

        if ("hover".equalsIgnoreCase(e.state)) {
            for (Player p : audience) {
                // Capture pre-show state once per player per show (putIfAbsent semantics)
                show.recordFlightRestore(p.getUniqueId(), p.getAllowFlight(), p.isFlying());
                p.setAllowFlight(true);
                p.setFlying(true);
            }

        } else if ("release".equalsIgnoreCase(e.state)) {
            for (Player p : audience) {
                // Apply transition effect first — player gets a soft landing before
                // flight is removed, so there is no hard gravity snap.
                if (!"none".equalsIgnoreCase(e.releaseEffect)) {
                    PotionEffectType type = "levitate".equalsIgnoreCase(e.releaseEffect)
                        ? PotionEffectType.LEVITATION
                        : PotionEffectType.SLOW_FALLING;
                    int amp = "levitate".equalsIgnoreCase(e.releaseEffect) ? 0 : 0;
                    p.addPotionEffect(new PotionEffect(type, e.releaseDurationTicks, amp, false, false));
                }

                // Restore pre-show flight state
                RunningShow.FlightState prior = show.getFlightRestore(p.getUniqueId());
                if (prior != null) {
                    // If player was flying before the show, restore flying.
                    // If not, disable flight — but only AFTER setting flying false first
                    // (calling setAllowFlight(false) while flying=true boots the player).
                    p.setFlying(prior.wasFlying());
                    p.setAllowFlight(prior.allowFlight());
                } else {
                    // No hover was ever fired for this player — safe default
                    p.setFlying(false);
                    p.setAllowFlight(false);
                }
            }
        } else {
            log.warning("[ScaenaShows] PLAYER_FLIGHT: unknown state '" + e.state
                + "' — expected 'hover' or 'release'");
        }
    }
}
