package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.StageEvents.*;
import com.scaena.shows.runtime.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles stage direction events: HOLD, FACE, CROSS_TO, RETURN_HOME, ENTER, EXIT.
 */
public final class StageEventExecutor implements EventExecutor {

    private final JavaPlugin plugin;
    private final Logger log;

    public StageEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case HOLD        -> handleHold((HoldEvent) event, show);
            case FACE        -> handleFace((FaceEvent) event, show);
            case ROTATE      -> handleRotate((RotateEvent) event, show);
            case CROSS_TO    -> handleCrossTo((CrossToEvent) event, show);
            case RETURN_HOME -> handleReturnHome((ReturnHomeEvent) event, show);
            case ENTER       -> handleEnter((EnterEvent) event, show);
            case EXIT        -> handleExit((ExitEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // HOLD — freeze entity at current position
    // ------------------------------------------------------------------
    private void handleHold(HoldEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            if (entity instanceof LivingEntity living) {
                // Freeze by setting velocity to zero
                living.setVelocity(Vector.getRandom().multiply(0));
            }
        }
    }

    // ------------------------------------------------------------------
    // FACE — turn to look at a target
    // ------------------------------------------------------------------
    private void handleFace(FaceEvent e, RunningShow show) {
        Location lookTarget = resolveLookAt(e.lookAt, show);
        if (lookTarget == null) return;

        for (Entity entity : resolveEntities(e.target, show)) {
            Location from = entity.getLocation();
            double dx = lookTarget.getX() - from.getX();
            double dz = lookTarget.getZ() - from.getZ();
            float yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
            double dy = lookTarget.getY() - from.getY();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDist)));
            Location newLoc = from.clone();
            newLoc.setYaw(yaw);
            newLoc.setPitch(pitch);
            entity.teleport(newLoc);
        }
    }

    // ------------------------------------------------------------------
    // ROTATE — smoothly rotate target's yaw to a destination angle (OPS-005)
    //
    // Supports absolute yaw: or relative delta: (sign = direction; spec §OPS-005).
    // duration_ticks: 0 or omitted = instant snap (same effect as FACE compass:...).
    // Wrap-around: for absolute yaw, normalise the interpolation delta to [−180, +180]
    // so the route is always the shorter arc.
    // With delta: the sign is respected as-written — authors control the direction.
    // ------------------------------------------------------------------
    private void handleRotate(RotateEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            float currentYaw = entity.getLocation().getYaw();
            float targetYaw;

            if (e.isDelta()) {
                // Relative: apply signed delta directly
                targetYaw = currentYaw + e.delta;
            } else {
                // Absolute: take the shorter arc to reach the target yaw
                targetYaw = e.yaw;
            }

            if (e.durationTicks <= 0) {
                // Instant snap
                Location loc = entity.getLocation();
                loc.setYaw(targetYaw);
                entity.teleport(loc);
            } else {
                // Smooth pan: compute per-tick yaw step, linear interpolation
                float rawDelta = targetYaw - currentYaw;
                if (!e.isDelta()) {
                    // Normalise to [−180, +180] for the shorter arc
                    rawDelta = ((rawDelta + 180f) % 360f + 360f) % 360f - 180f;
                }
                final float totalDelta = rawDelta;
                final int steps = e.durationTicks;
                final float startYaw = currentYaw;
                final Entity target = entity;

                BukkitTask task = new BukkitRunnable() {
                    int step = 0;
                    @Override
                    public void run() {
                        if (!show.isRunning() || !target.isValid() || ++step > steps) {
                            cancel();
                            return;
                        }
                        float fraction = (float) step / steps;
                        float newYaw = startYaw + totalDelta * fraction;
                        Location loc = target.getLocation();
                        loc.setYaw(newYaw);
                        target.teleport(loc);
                    }
                }.runTaskTimer(plugin, 1L, 1L);

                show.addRotateTask(task);
            }
        }
    }

    // ------------------------------------------------------------------
    // CROSS_TO — move to a destination
    // ------------------------------------------------------------------
    private void handleCrossTo(CrossToEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        for (Entity entity : resolveEntities(e.target, show)) {
            ParticipantState ps = entity instanceof Player p
                ? show.getParticipant(p.getUniqueId()) : null;

            Location dest = PositionResolver.resolve(e.destination, show, ps, anchor);
            if (dest == null) continue;

            if (e.durationTicks <= 0) {
                // Instant teleport
                dest.setYaw(entity.getLocation().getYaw());
                entity.teleport(dest);
                applyFacing(entity, e.facing, show, anchor);
            } else {
                // Gradual movement: repeated relative teleports for players,
                // pathfinder for mobs.
                if (entity instanceof Player player) {
                    smoothMovePlayer(player, dest, e.durationTicks, e.facing, show, anchor);
                } else if (entity instanceof Mob mob) {
                    mob.getPathfinder().moveTo(dest);
                }
            }
        }
    }

    private void smoothMovePlayer(Player player, Location dest, int durationTicks,
                                   String facing, RunningShow show, Location anchor) {
        // Capture start position once — interpolate absolutely to prevent drift.
        // Incremental (cur + delta) accumulates error if the player moves between ticks.
        Location start = player.getLocation().clone();
        int steps = Math.max(1, durationTicks);
        double startX = start.getX(), startY = start.getY(), startZ = start.getZ();
        double dx = (dest.getX() - startX) / steps;
        double dy = (dest.getY() - startY) / steps;
        double dz = (dest.getZ() - startZ) / steps;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (!show.isRunning() || !player.isOnline() || ++step > steps) {
                    cancel();
                    if (step > steps) {
                        applyFacing(player, facing, show, anchor);
                    }
                    return;
                }
                // Absolute position each tick — no drift accumulation
                Location cur = player.getLocation();
                player.teleport(new Location(cur.getWorld(),
                    startX + dx * step,
                    startY + dy * step,
                    startZ + dz * step,
                    cur.getYaw(), cur.getPitch()));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void applyFacing(Entity entity, String facing, RunningShow show, Location anchor) {
        if (facing == null) return;
        Location lookAt = resolveLookAt(facing, show);
        if (lookAt == null) return;
        Location from = entity.getLocation();
        double dx = lookAt.getX() - from.getX();
        double dz = lookAt.getZ() - from.getZ();
        float yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
        from.setYaw(yaw);
        entity.teleport(from);
    }

    // ------------------------------------------------------------------
    // RETURN_HOME
    // ------------------------------------------------------------------
    private void handleReturnHome(ReturnHomeEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            if (entity instanceof Player player) {
                // Player: return to pre-show home stored in ParticipantState
                ParticipantState ps = show.getParticipant(player.getUniqueId());
                if (ps == null || ps.home == null) continue;
                if (e.durationTicks <= 0) {
                    player.teleport(ps.home);
                } else {
                    smoothMovePlayer(player, ps.home, e.durationTicks, null, show, show.getAnchorLocation());
                }
            } else {
                // Non-Player entity: return to spawn-time home recorded in RunningShow
                String spawnedName = getSpawnedName(entity, show);
                if (spawnedName == null) continue;
                Location home = show.getEntityHome(spawnedName);
                if (home == null) continue;
                if (e.durationTicks <= 0 || !(entity instanceof Mob mob)) {
                    entity.teleport(home);
                } else {
                    mob.getPathfinder().moveTo(home);
                }
            }
        }
    }

    /**
     * Reverse-lookup the registered name for a spawned entity, so we can
     * find its home in RunningShow. Returns null if the entity wasn't
     * registered by name (e.g. captured via CAPTURE_ENTITIES).
     */
    private String getSpawnedName(Entity entity, RunningShow show) {
        for (Map.Entry<String, org.bukkit.entity.Entity> entry
                : show.getSpawnedEntities().entrySet()) {
            if (entry.getValue().equals(entity)) return entry.getKey();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // ENTER — semantic shorthand: spawn + cross to
    // ------------------------------------------------------------------
    private void handleEnter(EnterEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        EntityType type;
        try { type = EntityType.valueOf(e.entityType.toUpperCase()); }
        catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] ENTER: unknown entity type: " + e.entityType); return;
        }

        Location fromLoc = PositionResolver.resolve(e.from, show, null, anchor);
        Location destLoc = PositionResolver.resolve(e.destination, show, null, anchor);
        if (fromLoc == null || destLoc == null) return;

        Entity entity = fromLoc.getWorld().spawnEntity(fromLoc, type);
        if (!e.name.isEmpty()) {
            entity.setCustomName(e.name);
            entity.setCustomNameVisible(true);
            show.registerSpawnedEntity(e.name, entity);
        }
        if (e.baby && entity instanceof Ageable ageable) ageable.setBaby();

        // Equipment — mirrors handleSpawn() in EntityEventExecutor
        if (entity instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) {
                if (itemOf(e.helmetItem)     != null) eq.setHelmet(itemOf(e.helmetItem));
                if (itemOf(e.chestplateItem) != null) eq.setChestplate(itemOf(e.chestplateItem));
                if (itemOf(e.leggingsItem)   != null) eq.setLeggings(itemOf(e.leggingsItem));
                if (itemOf(e.bootsItem)      != null) eq.setBoots(itemOf(e.bootsItem));
                if (itemOf(e.mainHandItem)   != null) eq.setItemInMainHand(itemOf(e.mainHandItem));
                if (itemOf(e.offHandItem)    != null) eq.setItemInOffHand(itemOf(e.offHandItem));
            }
        }

        if (entity instanceof Mob mob) mob.getPathfinder().moveTo(destLoc);
    }

    private ItemStack itemOf(String material) {
        if (material == null || material.isEmpty()) return null;
        try { return new ItemStack(Material.valueOf(material.toUpperCase())); }
        catch (IllegalArgumentException ex) { return null; }
    }

    // ------------------------------------------------------------------
    // EXIT — move to wing + optionally despawn
    // ------------------------------------------------------------------
    private void handleExit(ExitEvent e, RunningShow show) {
        Entity entity = show.getSpawnedEntity(
            e.target.startsWith("entity:spawned:") ? e.target.substring(15) : e.target);
        if (entity == null) return;

        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;
        Location toLoc = PositionResolver.resolve(e.to, show, null, anchor);
        if (toLoc == null) return;

        if (entity instanceof Mob mob) {
            mob.getPathfinder().moveTo(toLoc);
            if (e.despawnOnArrival) {
                new BukkitRunnable() {
                    @Override public void run() {
                        if (!show.isRunning()) { cancel(); return; }
                        if (entity.getLocation().distanceSquared(toLoc) < 4) {
                            entity.remove();
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 5L, 5L);
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private List<Entity> resolveEntities(String target, RunningShow show) {
        if (target == null) return List.of();

        if (target.startsWith("entity:spawned:")) {
            Entity e = show.getSpawnedEntity(target.substring(15));
            return e != null ? List.of(e) : List.of();
        }
        if (target.startsWith("entity_group:")) {
            String name = target.substring(13);
            List<Entity> out = new java.util.ArrayList<>();
            for (UUID uid : show.resolveEntityGroup(name)) {   // OPS-006: handles live + snapshot
                Entity e = Bukkit.getEntity(uid);
                if (e != null) out.add(e);
            }
            return out;
        }
        if (target.startsWith("group_")) {
            try {
                int g = Integer.parseInt(target.substring(6));
                return new java.util.ArrayList<>(show.getGroupPlayers(g));
            } catch (NumberFormatException ignored) {}
        }
        if ("player".equalsIgnoreCase(target)
                || "participants".equalsIgnoreCase(target)
                || "broadcast".equalsIgnoreCase(target)) {
            return new java.util.ArrayList<>(show.getOnlineParticipants());
        }
        return List.of();
    }

    private Location resolveLookAt(String lookAt, RunningShow show) {
        if (lookAt == null) return null;
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return null;
        if (lookAt.startsWith("mark:")) {
            var mark = show.show.marks.get(lookAt.substring(5));
            if (mark == null) return null;
            return anchor.clone().add(mark.x(), 0, mark.z());
        }
        if (lookAt.startsWith("compass:")) {
            // compass:south → face south (+Z)
            String dir = lookAt.substring(8).toLowerCase();
            return switch (dir) {
                case "north" -> anchor.clone().add(0, 0, -10);
                case "south" -> anchor.clone().add(0, 0, 10);
                case "east"  -> anchor.clone().add(10, 0, 0);
                case "west"  -> anchor.clone().add(-10, 0, 0);
                default      -> null;
            };
        }
        if (lookAt.startsWith("entity:spawned:")) {
            Entity e = show.getSpawnedEntity(lookAt.substring(15));
            return e != null ? e.getLocation() : null;
        }
        if ("player".equalsIgnoreCase(lookAt)) {
            List<? extends Entity> ps = show.getOnlineParticipants();
            return ps.isEmpty() ? null : ps.get(0).getLocation();
        }
        return null;
    }
}
