package com.scaena.shows.model.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * §6.10 Camera State — CAMERA_LOCK, MOVEMENT_LOCK, BOUNDARY_CHECK, VIEW_CHECK
 *
 * CAMERA_LOCK and MOVEMENT_LOCK are cross-department show-state flags on RunningShow.
 * Stop-safety always resets both to unlocked at show end.
 *
 * BOUNDARY_CHECK and VIEW_CHECK are the first conditional primitives in the engine.
 * They evaluate show state at fire-time and branch on the result.
 *
 * Spec: kb/system/phase2-department-panels.md §Camera
 */
public final class CameraStateEvents {

    private CameraStateEvents() {}

    // ------------------------------------------------------------------
    // CAMERA_LOCK — set or clear the camera-lock state flag on RunningShow
    // ------------------------------------------------------------------
    public static final class CameraLockEvent extends ShowEvent {
        /** "ON" | "OFF" */
        public final String state;

        public CameraLockEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.state = str(m, "state", "ON").toUpperCase();
        }

        @Override public EventType type() { return EventType.CAMERA_LOCK; }
    }

    // ------------------------------------------------------------------
    // MOVEMENT_LOCK — set or clear the movement-lock state flag on RunningShow
    // ------------------------------------------------------------------
    public static final class MovementLockEvent extends ShowEvent {
        /** "ON" | "OFF" */
        public final String state;

        public MovementLockEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.state = str(m, "state", "ON").toUpperCase();
        }

        @Override public EventType type() { return EventType.MOVEMENT_LOCK; }
    }

    // ------------------------------------------------------------------
    // BOUNDARY_CHECK — position-based conditional
    //
    // center: mark:Name | {x:N,y:N,z:N}
    // radius: N  (blocks)
    // out_of_range: list of event maps (fired when participant is > radius blocks from center)
    // in_range:     list of event maps (optional; fired when participant is <= radius from center)
    //
    // Sub-events are stored as raw YAML maps; EventParser.parseTimeline() is called
    // by the executor at fire time so that BOUNDARY_CHECK can be freely nested.
    // ------------------------------------------------------------------
    public static final class BoundaryCheckEvent extends ShowEvent {
        /** Mark reference or explicit xyz string for the boundary center. */
        public final String center;
        /** Radius in blocks. */
        public final double radius;
        /** Raw YAML maps for the out-of-range branch (never null, may be empty). */
        public final List<Map<String, Object>> outOfRangeRaw;
        /** Raw YAML maps for the in-range branch (optional; never null, may be empty). */
        public final List<Map<String, Object>> inRangeRaw;

        @SuppressWarnings("unchecked")
        public BoundaryCheckEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.center = str(m, "center", "mark:stage_center");
            this.radius = dblVal(m, "radius", 10.0);

            Object oor = m.get("out_of_range");
            List<Map<String, Object>> oorList = new ArrayList<>();
            if (oor instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> sm) oorList.add((Map<String, Object>) sm);
                }
            }
            this.outOfRangeRaw = List.copyOf(oorList);

            Object ir = m.get("in_range");
            List<Map<String, Object>> irList = new ArrayList<>();
            if (ir instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> sm) irList.add((Map<String, Object>) sm);
                }
            }
            this.inRangeRaw = List.copyOf(irList);
        }

        @Override public EventType type() { return EventType.BOUNDARY_CHECK; }
    }

    // ------------------------------------------------------------------
    // VIEW_CHECK — orientation-based conditional
    //
    // target:    mark:Name | entity:spawned:Name
    // tolerance: degrees of angular deviation allowed before corrective fires
    // out_of_view: config map — { duration_ticks: N, interpolation: EASE_OUT|EASE_IN|LINEAR }
    //              corrective action is always a smooth pan toward target (never a snap)
    // in_view:   optional config map (currently used as a presence flag only)
    // ------------------------------------------------------------------
    public static final class ViewCheckEvent extends ShowEvent {
        /** mark:Name or entity:spawned:Name */
        public final String target;
        /** Angular tolerance in degrees. */
        public final int    tolerance;
        /** Smooth pan duration when out of view. */
        public final int    outOfViewDurationTicks;
        /** Interpolation style: EASE_OUT | EASE_IN | LINEAR */
        public final String outOfViewInterpolation;
        /** Whether an in_view block was provided (currently no sub-action; used as marker). */
        public final boolean hasInView;

        @SuppressWarnings("unchecked")
        public ViewCheckEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target    = str(m, "target", "mark:center");
            this.tolerance = intVal(m, "tolerance", 30);

            Map<String, Object> oov = m.get("out_of_view") instanceof Map<?, ?> oovRaw
                ? (Map<String, Object>) oovRaw
                : Map.of();
            this.outOfViewDurationTicks = intVal(oov, "duration_ticks", 20);
            this.outOfViewInterpolation = str(oov, "interpolation", "EASE_OUT").toUpperCase();
            this.hasInView = m.containsKey("in_view");
        }

        @Override public EventType type() { return EventType.VIEW_CHECK; }
    }
}
