package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.Mark;
import com.scaena.shows.model.event.CameraStateEvents.*;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.registry.EventParser;
import com.scaena.shows.runtime.AudienceResolver;
import com.scaena.shows.runtime.ParticipantState;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles CAMERA_LOCK, MOVEMENT_LOCK, BOUNDARY_CHECK, VIEW_CHECK.
 *
 * CAMERA_LOCK / MOVEMENT_LOCK — set cross-department state flags on RunningShow.
 * Stop-safety is implicit: RunningShow is discarded at show end, so the flags go with it.
 *
 * BOUNDARY_CHECK — evaluate each participant's distance from center; dispatch the
 * appropriate branch (out_of_range or in_range) using EventParser.parseTimeline()
 * + ExecutorRegistry.dispatch() called per-event.
 *
 * VIEW_CHECK — evaluate each participant's angular deviation from target; if > tolerance,
 * apply a smooth yaw pan (same mechanism as ROTATE) toward the target. Correction is
 * always a smooth pan — never a snap.
 *
 * Spec: kb/system/phase2-department-panels.md §Camera
 */
public final class CameraStateEventExecutor implements EventExecutor {

    private final JavaPlugin       plugin;
    private final Logger           log;
    private ExecutorRegistry       executors; // set via setExecutorRegistry() after construction

    public CameraStateEventExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    /** Wire in after ExecutorRegistry is fully constructed (breaks circular dep). */
    public void setExecutorRegistry(ExecutorRegistry executors) {
        this.executors = executors;
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case CAMERA_LOCK    -> handleCameraLock((CameraLockEvent) event, show);
            case MOVEMENT_LOCK  -> handleMovementLock((MovementLockEvent) event, show);
            case BOUNDARY_CHECK -> handleBoundaryCheck((BoundaryCheckEvent) event, show);
            case VIEW_CHECK     -> handleViewCheck((ViewCheckEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // CAMERA_LOCK
    // ------------------------------------------------------------------
    private void handleCameraLock(CameraLockEvent e, RunningShow show) {
        show.setCameraLocked("ON".equals(e.state));
    }

    // ------------------------------------------------------------------
    // MOVEMENT_LOCK
    // ------------------------------------------------------------------
    private void handleMovementLock(MovementLockEvent e, RunningShow show) {
        show.setMovementLocked("ON".equals(e.state));
    }

    // ------------------------------------------------------------------
    // BOUNDARY_CHECK
    //
    // For each participant: compute horizontal distance from center.
    // If > radius → dispatch out_of_range branch.
    // If <= radius AND in_range is non-empty → dispatch in_range branch.
    // ------------------------------------------------------------------
    private void handleBoundaryCheck(BoundaryCheckEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        Location center = resolveCenter(e.center, show, anchor);
        if (center == null) {
            log.warning("[ScaenaShows] BOUNDARY_CHECK: could not resolve center '" + e.center + "'");
            return;
        }

        // Parse branch events once; reuse for all participants
        List<ShowEvent> outEvents = e.outOfRangeRaw.isEmpty() ? List.of()
            : EventParser.parseTimeline(e.outOfRangeRaw);
        List<ShowEvent> inEvents  = e.inRangeRaw.isEmpty() ? List.of()
            : EventParser.parseTimeline(e.inRangeRaw);

        if (outEvents.isEmpty() && inEvents.isEmpty()) return; // nothing to do

        for (ParticipantState ps : show.getParticipants().values()) {
            Player p = Bukkit.getPlayer(ps.uuid);
            if (p == null || !p.isOnline()) continue;

            double dx = p.getLocation().getX() - center.getX();
            double dz = p.getLocation().getZ() - center.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            List<ShowEvent> branch = dist > e.radius ? outEvents : inEvents;
            for (ShowEvent subEvent : branch) {
                if (executors != null) executors.dispatch(subEvent, show);
            }
        }
    }

    /**
     * Resolve a center string to a world Location.
     * Supports: mark:Name, explicit {x:N,y:N,z:N} in the future (currently mark: only).
     */
    private Location resolveCenter(String center, RunningShow show, Location anchor) {
        if (center.startsWith("mark:")) {
            String markName = center.substring(5);
            Mark mark = show.show.marks.get(markName);
            if (mark == null) return null;
            return new Location(anchor.getWorld(),
                anchor.getX() + mark.x(),
                anchor.getY() + mark.y(),
                anchor.getZ() + mark.z());
        }
        // Fallback: use anchor itself
        return anchor.clone();
    }

    // ------------------------------------------------------------------
    // VIEW_CHECK
    //
    // For each participant: compute the yaw angle from the player's position to
    // the target. If the angular deviation from the player's current yaw exceeds
    // tolerance, schedule a smooth pan toward the target.
    //
    // VIEW_CHECK corrective is always a smooth pan — never a snap.
    // ------------------------------------------------------------------
    private void handleViewCheck(ViewCheckEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        Location targetLoc = resolveTarget(e.target, show, anchor);
        if (targetLoc == null) {
            log.warning("[ScaenaShows] VIEW_CHECK: could not resolve target '" + e.target + "'");
            return;
        }

        for (ParticipantState ps : show.getParticipants().values()) {
            Player p = Bukkit.getPlayer(ps.uuid);
            if (p == null || !p.isOnline()) continue;

            Location pLoc = p.getLocation();
            float currentYaw = pLoc.getYaw();

            // Compute target yaw from player XZ → target XZ
            double dx = targetLoc.getX() - pLoc.getX();
            double dz = targetLoc.getZ() - pLoc.getZ();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

            // Angular deviation — normalise to [−180, +180]
            float deviation = targetYaw - currentYaw;
            deviation = ((deviation + 180f) % 360f + 360f) % 360f - 180f;

            if (Math.abs(deviation) <= e.tolerance) {
                // Within tolerance — in_view case; currently no corrective action
                continue;
            }

            // Out of view: smooth pan toward target
            final float totalDelta = deviation;
            final float startYaw   = currentYaw;
            final int   steps      = Math.max(1, e.outOfViewDurationTicks);
            final Player target    = p;

            BukkitTask task = new BukkitRunnable() {
                int step = 0;
                @Override
                public void run() {
                    if (!show.isRunning() || !target.isOnline() || ++step > steps) {
                        cancel();
                        return;
                    }
                    float fraction = applyInterpolation(e.outOfViewInterpolation, (float) step / steps);
                    float newYaw = startYaw + totalDelta * fraction;
                    Location loc = target.getLocation();
                    loc.setYaw(newYaw);
                    target.teleport(loc);
                }
            }.runTaskTimer(plugin, 1L, 1L);

            show.addRotateTask(task); // reuse existing tracking for stop-safety cancel
        }
    }

    /**
     * Resolve a target expression to a world Location.
     * Supports: mark:Name, entity:spawned:Name
     */
    private Location resolveTarget(String target, RunningShow show, Location anchor) {
        if (target.startsWith("mark:")) {
            String markName = target.substring(5);
            Mark mark = show.show.marks.get(markName);
            if (mark == null) return null;
            return new Location(anchor.getWorld(),
                anchor.getX() + mark.x(),
                anchor.getY() + mark.y(),
                anchor.getZ() + mark.z());
        }
        if (target.startsWith("entity:spawned:")) {
            Entity entity = show.getSpawnedEntity(target.substring(15));
            return entity != null ? entity.getLocation() : null;
        }
        return null;
    }

    /**
     * Map an interpolation style name to a [0,1] fraction.
     * Input t is the linear fraction in [0,1]; output is the eased fraction.
     */
    private static float applyInterpolation(String style, float t) {
        return switch (style) {
            case "EASE_IN"  -> t * t;
            case "EASE_OUT" -> t * (2f - t);
            default         -> t; // LINEAR
        };
    }
}
