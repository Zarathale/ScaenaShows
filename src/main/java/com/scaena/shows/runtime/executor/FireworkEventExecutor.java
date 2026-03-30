package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.FireworkPreset;
import com.scaena.shows.model.FireworkStar;
import com.scaena.shows.model.event.FireworkEvents.*;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.registry.FireworkRegistry;
import com.scaena.shows.runtime.PositionResolver;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.logging.Logger;

/**
 * Handles FIREWORK, FIREWORK_CIRCLE, FIREWORK_LINE, FIREWORK_FAN events.
 * Resolves pattern positions using compass/bearing angle convention.
 */
public final class FireworkEventExecutor implements EventExecutor {

    /** Rainbow color cycle for RAINBOW color_variation */
    private static final Color[] RAINBOW = {
        Color.fromRGB(0xFF0000), Color.fromRGB(0xFF8000),
        Color.fromRGB(0xFFFF00), Color.fromRGB(0x00FF00),
        Color.fromRGB(0x00FFFF), Color.fromRGB(0x0000FF),
        Color.fromRGB(0x8B00FF)
    };

    private final JavaPlugin plugin;
    private final FireworkRegistry fireworkRegistry;
    private final Logger log;
    private final Random rng = new Random();

    public FireworkEventExecutor(JavaPlugin plugin, FireworkRegistry fireworkRegistry) {
        this.plugin           = plugin;
        this.fireworkRegistry = fireworkRegistry;
        this.log              = plugin.getLogger();
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case FIREWORK        -> handleFirework((FireworkEvent) event, show);
            case FIREWORK_CIRCLE -> handleCircle((FireworkCircleEvent) event, show);
            case FIREWORK_LINE   -> handleLine((FireworkLineEvent) event, show);
            case FIREWORK_FAN    -> handleFan((FireworkFanEvent) event, show);
            case FIREWORK_RANDOM -> handleRandom((FireworkRandomEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // FIREWORK (single)
    // ------------------------------------------------------------------
    private void handleFirework(FireworkEvent e, RunningShow show) {
        FireworkPreset preset = fireworkRegistry.get(e.preset);
        if (preset == null) {
            log.warning("[ScaenaShows] Unknown firework preset: '" + e.preset + "'");
            return;
        }
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        Location loc = PositionResolver.fireworkLocation(anchor, e.offsetX, e.offsetZ, e.yMode, e.offsetY);
        spawnFirework(loc, preset, preset.power(), null);
    }

    // ------------------------------------------------------------------
    // FIREWORK_CIRCLE
    // ------------------------------------------------------------------
    private void handleCircle(FireworkCircleEvent e, RunningShow show) {
        FireworkPreset preset = fireworkRegistry.get(e.preset);
        if (preset == null) { log.warning("[ScaenaShows] Unknown preset: " + e.preset); return; }

        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        // Build per-position locations
        List<Location> positions = new ArrayList<>();
        for (int i = 0; i < e.count; i++) {
            double[] pt = PositionResolver.circlePoint(
                e.originOffset.x(), e.originOffset.z(), e.radius, i, e.count);
            positions.add(PositionResolver.fireworkLocation(anchor, pt[0], pt[1], e.yMode, e.yOffset));
        }

        launchWithChase(positions, e.chase, preset, e.powerVariation, e.colorVariation,
            e.gradientFrom, e.gradientTo, show);
    }

    // ------------------------------------------------------------------
    // FIREWORK_LINE
    // ------------------------------------------------------------------
    private void handleLine(FireworkLineEvent e, RunningShow show) {
        FireworkPreset preset = fireworkRegistry.get(e.preset);
        if (preset == null) { log.warning("[ScaenaShows] Unknown preset: " + e.preset); return; }

        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        List<Location> positions = new ArrayList<>();
        for (int i = 0; i < e.count; i++) {
            double t = e.count > 1 ? (double) i * e.length / (e.count - 1) : 0;
            double[] pt = PositionResolver.linePoint(e.startOffset.x(), e.startOffset.z(), e.angle, t);
            positions.add(PositionResolver.fireworkLocation(anchor, pt[0], pt[1], e.yMode, e.yOffset));
        }

        launchWithChase(positions, e.chase, preset, e.powerVariation, e.colorVariation, e.gradientFrom, e.gradientTo, show);
    }

    // ------------------------------------------------------------------
    // FIREWORK_FAN
    // ------------------------------------------------------------------
    private void handleFan(FireworkFanEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        // Build all positions across all arms
        List<Location> allPositions = new ArrayList<>();
        List<String> armPresets    = new ArrayList<>();
        for (FireworkFanEvent.FanArm arm : e.arms) {
            FireworkPreset preset = fireworkRegistry.get(arm.preset());
            if (preset == null) { log.warning("[ScaenaShows] Unknown preset: " + arm.preset()); continue; }
            for (int i = 0; i < arm.count(); i++) {
                double t = arm.count() > 1 ? (double) i * arm.length() / (arm.count() - 1) : 0;
                double[] pt = PositionResolver.linePoint(
                    e.originOffset.x(), e.originOffset.z(), arm.angle(), t);
                allPositions.add(PositionResolver.fireworkLocation(anchor, pt[0], pt[1], e.yMode, e.yOffset));
                armPresets.add(arm.preset());
            }
        }

        if (allPositions.isEmpty()) return;

        boolean simultaneous = "simultaneous".equalsIgnoreCase(e.chase.mode());

        if (!e.chase.enabled() || simultaneous) {
            // Fire all at once (or simultaneous per-arm — each arm chases independently)
            // For simplicity, treat simultaneous as: launch all with per-position delay by arm index
            for (int i = 0; i < allPositions.size(); i++) {
                FireworkPreset preset = fireworkRegistry.get(armPresets.get(i));
                if (preset == null) continue;
                Location loc = allPositions.get(i);
                spawnFirework(loc, preset, preset.power(), null);
            }
        } else {
            // Sequential chase across all arms
            for (int i = 0; i < allPositions.size(); i++) {
                int idx = i;
                FireworkPreset preset = fireworkRegistry.get(armPresets.get(idx));
                if (preset == null) continue;
                Location loc = allPositions.get(idx);
                long delay = (long) idx * e.chase.intervalTicks();
                new BukkitRunnable() {
                    @Override public void run() {
                        if (!show.isRunning()) { cancel(); return; }
                        spawnFirework(loc, preset, preset.power(), null);
                    }
                }.runTaskLater(plugin, delay);
            }
        }
    }

    // ------------------------------------------------------------------
    // FIREWORK_RANDOM — scatter N fireworks at random XZ positions within radius.
    // All fireworks launch simultaneously. Optional seed for reproducible patterns.
    // ------------------------------------------------------------------
    private void handleRandom(FireworkRandomEvent e, RunningShow show) {
        FireworkPreset preset = fireworkRegistry.get(e.preset);
        if (preset == null) { log.warning("[ScaenaShows] Unknown preset: " + e.preset); return; }

        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        // Use seeded RNG if specified, otherwise use the instance RNG for variety
        Random rand = (e.seed != null) ? new Random(e.seed) : rng;

        for (int i = 0; i < e.count; i++) {
            // Uniform scatter within a circle: use polar coordinates with sqrt for uniform density
            double angle  = rand.nextDouble() * 2 * Math.PI;
            double dist   = Math.sqrt(rand.nextDouble()) * e.radius;
            double dx     = dist * Math.sin(angle); // sin for X (east)
            double dz     = dist * Math.cos(angle); // cos for Z (north)
            double ox     = e.originOffset.x() + dx;
            double oz     = e.originOffset.z() + dz;

            Location loc = PositionResolver.fireworkLocation(anchor, ox, oz, e.yMode, e.yOffset);
            spawnFirework(loc, preset, preset.power(), null);
        }
    }

    // ------------------------------------------------------------------
    // Chase launch helper
    // ------------------------------------------------------------------
    private void launchWithChase(
        List<Location> positions,
        Chase chase,
        FireworkPreset basePreset,
        String powerVariation,
        String colorVariation,
        String gradientFrom,
        String gradientTo,
        RunningShow show
    ) {
        int count = positions.size();

        if (!chase.enabled()) {
            // Fire all at once
            for (int i = 0; i < count; i++) {
                Color overrideColor = resolveColorVariation(colorVariation, i, count,
                    basePreset, gradientFrom, gradientTo);
                int power = resolvePower(powerVariation, i, count, basePreset.power());
                spawnFirework(positions.get(i), basePreset, power, overrideColor);
            }
            return;
        }

        // Chase with staggered delay
        boolean reverse = "LF".equalsIgnoreCase(chase.direction());
        for (int i = 0; i < count; i++) {
            int seqIdx = reverse ? (count - 1 - i) : i;
            Location loc = positions.get(seqIdx);
            Color overrideColor = resolveColorVariation(colorVariation, seqIdx, count,
                basePreset, gradientFrom, gradientTo);
            int power = resolvePower(powerVariation, seqIdx, count, basePreset.power());
            long delay = (long) i * chase.intervalTicks();

            new BukkitRunnable() {
                @Override public void run() {
                    if (!show.isRunning()) { cancel(); return; }
                    spawnFirework(loc, basePreset, power, overrideColor);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    // ------------------------------------------------------------------
    // Firework spawning
    // ------------------------------------------------------------------
    private void spawnFirework(Location loc, FireworkPreset preset, int power, Color colorOverride) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(power);

        for (FireworkStar star : preset.stars()) {
            FireworkEffect.Builder builder = FireworkEffect.builder();

            // Shape
            FireworkEffect.Type shape = parseShape(star.shape());
            builder.with(shape);

            // Colors: override primary if colorOverride is provided
            if (colorOverride != null) {
                builder.withColor(colorOverride);
            } else {
                for (String hex : star.colors()) {
                    try { builder.withColor(parseHex(hex)); }
                    catch (Exception ignored) {}
                }
                if (star.colors().isEmpty()) builder.withColor(Color.WHITE);
            }

            // Fade colors (always from preset)
            for (String hex : star.fades()) {
                try { builder.withFade(parseHex(hex)); }
                catch (Exception ignored) {}
            }

            builder.trail(star.trail());
            builder.flicker(star.flicker());
            meta.addEffect(builder.build());
        }

        fw.setFireworkMeta(meta);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private FireworkEffect.Type parseShape(String shape) {
        return switch (shape.toUpperCase()) {
            case "BALL_LARGE", "LARGE_BALL" -> FireworkEffect.Type.BALL_LARGE;
            case "STAR"    -> FireworkEffect.Type.STAR;
            case "BURST"   -> FireworkEffect.Type.BURST;
            case "CREEPER" -> FireworkEffect.Type.CREEPER;
            default        -> FireworkEffect.Type.BALL;
        };
    }

    private Color parseHex(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        int rgb = Integer.parseInt(clean, 16);
        return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private int resolvePower(String variation, int index, int count, int basePower) {
        int max = Math.max(basePower, 3);
        return switch (variation.toUpperCase()) {
            case "RAMP_UP"   -> Math.max(1, (int) Math.round(1.0 + (double) index / Math.max(1, count - 1) * (max - 1)));
            case "RAMP_DOWN" -> Math.max(1, (int) Math.round(max - (double) index / Math.max(1, count - 1) * (max - 1)));
            case "ALTERNATE" -> (index % 2 == 0) ? basePower : Math.max(1, basePower - 1);
            case "RANDOM"    -> rng.nextInt(max) + 1;
            default          -> basePower; // UNIFORM
        };
    }

    private Color resolveColorVariation(
        String variation, int index, int count,
        FireworkPreset preset, String gradientFrom, String gradientTo
    ) {
        return switch (variation.toUpperCase()) {
            case "RAINBOW"  -> RAINBOW[index % RAINBOW.length];
            case "GRADIENT" -> interpolateGradient(gradientFrom, gradientTo, index, count);
            case "ALTERNATE" -> {
                List<String> colors = preset.stars().isEmpty() ? List.of() : preset.stars().get(0).colors();
                if (colors.size() < 2) yield null;
                try { yield parseHex(colors.get(index % 2)); }
                catch (Exception e) { yield null; }
            }
            default -> null; // UNIFORM — use preset colors unchanged
        };
    }

    private Color interpolateGradient(String fromHex, String toHex, int index, int count) {
        try {
            String fc = fromHex != null ? fromHex.replace("#", "") : "FF0000";
            String tc = toHex   != null ? toHex.replace("#", "")   : "0000FF";
            int fr = Integer.parseInt(fc.substring(0, 2), 16);
            int fg = Integer.parseInt(fc.substring(2, 4), 16);
            int fb = Integer.parseInt(fc.substring(4, 6), 16);
            int tr = Integer.parseInt(tc.substring(0, 2), 16);
            int tg = Integer.parseInt(tc.substring(2, 4), 16);
            int tb = Integer.parseInt(tc.substring(4, 6), 16);
            double t = count > 1 ? (double) index / (count - 1) : 0;
            int r = (int) (fr + t * (tr - fr));
            int g = (int) (fg + t * (tg - fg));
            int b = (int) (fb + t * (tb - fb));
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return null;
        }
    }
}
