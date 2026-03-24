package com.scaena.shows.runtime;

import com.scaena.shows.model.Mark;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves position expressions used in stage directions and firework patterns.
 *
 * Supported destination formats (spec §6.9, §11):
 *   mark:Name          — named mark relative to spatial anchor
 *   home               — participant's captured home position
 *   home+{x:N,z:M}     — home plus offset
 *   {x:N,z:M}          — raw XZ offset from spatial anchor
 */
public final class PositionResolver {

    private static final Pattern HOME_OFFSET = Pattern.compile(
        "home\\+\\{\\s*x:\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*,\\s*z:\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*\\}"
    );

    private PositionResolver() {}

    /**
     * Resolve a destination string to a world Location.
     *
     * @param destination  the raw destination string from YAML
     * @param show         the running show (for marks, anchor)
     * @param participant  the participant whose home to use for "home" references
     * @param anchorLoc    current spatial anchor location
     */
    public static Location resolve(
        String destination,
        RunningShow show,
        ParticipantState participant,
        Location anchorLoc
    ) {
        if (destination == null || anchorLoc == null) return anchorLoc;
        World world = anchorLoc.getWorld();

        // mark:Name
        if (destination.startsWith("mark:")) {
            String markName = destination.substring(5);
            Mark mark = show.show.marks.get(markName);
            if (mark == null) return anchorLoc;
            return new Location(world,
                anchorLoc.getX() + mark.x(),
                anchorLoc.getY(),
                anchorLoc.getZ() + mark.z(),
                anchorLoc.getYaw(),
                anchorLoc.getPitch());
        }

        // home (exact home position)
        if ("home".equalsIgnoreCase(destination)) {
            return participant != null ? participant.home.clone() : anchorLoc;
        }

        // home+{x:N,z:M}
        Matcher homeOffsetMatcher = HOME_OFFSET.matcher(destination);
        if (homeOffsetMatcher.matches()) {
            double dx = Double.parseDouble(homeOffsetMatcher.group(1));
            double dz = Double.parseDouble(homeOffsetMatcher.group(2));
            Location base = participant != null ? participant.home.clone() : anchorLoc.clone();
            return base.add(dx, 0, dz);
        }

        // Raw XZ offset — return as anchor-relative
        return anchorLoc;
    }

    /**
     * Compute the XZ world position for a firework pattern position
     * given the anchor location and XZ offsets.
     */
    public static Location fireworkLocation(
        Location anchor,
        double offsetX,
        double offsetZ,
        String yMode,
        double yOffset
    ) {
        World world = anchor.getWorld();
        double x = anchor.getX() + offsetX;
        double z = anchor.getZ() + offsetZ;
        double y;

        if ("surface".equalsIgnoreCase(yMode)) {
            // Y = highest non-air block at this XZ + yOffset
            y = world.getHighestBlockYAt((int) x, (int) z) + yOffset;
        } else {
            // relative: Y = anchor feet + yOffset
            y = anchor.getY() + yOffset;
        }

        return new Location(world, x, y, z);
    }

    /**
     * Convert a compass/bearing angle (clockwise from north) to radians.
     * Spec §15: radians = (90 - degrees) * PI / 180
     * 0=north(-Z), 90=east(+X), 180=south(+Z), 270=west(-X)
     */
    public static double compassToRadians(double degrees) {
        return (90.0 - degrees) * Math.PI / 180.0;
    }

    /**
     * Compute a point along a line defined by start + angle + distance.
     * Returns the XZ delta from anchor.
     */
    public static double[] linePoint(double startX, double startZ, double angleDeg, double t) {
        double radians = compassToRadians(angleDeg);
        return new double[]{
            startX + t * Math.cos(radians),
            startZ + t * Math.sin(radians)
        };
    }

    /**
     * Compute a point on a circle at radius r and angle index i out of count total.
     * Positions evenly distributed around the full circle.
     */
    public static double[] circlePoint(double originX, double originZ, double radius, int i, int count) {
        // Distribute from north (top) going clockwise — consistent with compass convention
        double angle = 2.0 * Math.PI * i / count;
        return new double[]{
            originX + radius * Math.sin(angle),
            originZ - radius * Math.cos(angle)
        };
    }
}
