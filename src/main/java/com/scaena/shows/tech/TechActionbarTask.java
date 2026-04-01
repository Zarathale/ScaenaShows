package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Repeating task (every 5 ticks) that keeps the actionbar updated during a tech session.
 *
 * Three display modes:
 *   Normal    — "TECH · [show_id] · [scene_label]"
 *   Capture   — "📍 CAPTURING: [mark_name] | (x.x, y.y, z.z) | right-click to capture"
 *   Confirm   — brief confirmation message, clears after 2 seconds (managed by confirmTask)
 */
public final class TechActionbarTask extends BukkitRunnable {

    private static final TextColor COL_TECH    = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_SEP     = TextColor.color(0x555555);
    private static final TextColor COL_SCENE   = TextColor.color(0xFFFFFF);
    private static final TextColor COL_CAPTURE = TextColor.color(0x55FFFF); // aqua
    private static final TextColor COL_CONFIRM = TextColor.color(0x55FF55); // green
    private static final TextColor COL_WARN    = TextColor.color(0xFFFF55); // yellow

    private final TechManager  manager;
    private final TechSession  session;

    public TechActionbarTask(TechManager manager, TechSession session) {
        this.manager = manager;
        this.session = session;
    }

    @Override
    public void run() {
        if (!session.player().isOnline()) {
            cancel();
            return;
        }

        Component bar = buildBar();
        session.player().sendActionBar(bar);
    }

    private Component buildBar() {
        // Confirm flash takes priority
        String confirm = session.confirmMessage();
        if (confirm != null) {
            return Component.text(confirm, COL_CONFIRM);
        }

        // Capture mode — live coordinates
        if (session.captureMode()) {
            Location loc = session.player().getLocation();
            String mark  = session.focusedMark();
            String coords = String.format("%.1f, %.1f, %.1f",
                loc.getX(), loc.getY(), loc.getZ());

            return Component.text("📍 CAPTURING: ", COL_CAPTURE)
                .append(Component.text(mark != null ? mark : "?", COL_WARN))
                .append(Component.text(" | ", COL_SEP))
                .append(Component.text(coords, COL_SCENE))
                .append(Component.text(" | right-click slot 8 to capture", COL_SEP));
        }

        // Param scroll mode
        if (session.paramScrollMode() && session.focusedParam() != null) {
            String name    = session.focusedParam();
            PromptBook.ParamSpec spec = session.book().findParam(name);
            String value   = spec != null
                ? currentValue(spec)
                : "?";

            return Component.text("⚙ ", COL_TECH)
                .append(Component.text(name, COL_WARN))
                .append(Component.text(" = ", COL_SEP))
                .append(Component.text(value, COL_SCENE))
                .append(Component.text("  [right-click +, shift+right-click −, right-click confirm]",
                    COL_SEP));
        }

        // Normal mode
        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        String sceneLabel = scene != null ? scene.label() : "—";
        boolean dirty = session.hasUnsavedChanges();

        Component bar = Component.text("TECH", COL_TECH)
            .append(Component.text(" · ", COL_SEP))
            .append(Component.text(session.showId(), COL_SCENE))
            .append(Component.text(" · ", COL_SEP))
            .append(Component.text(sceneLabel, COL_SCENE));

        if (dirty) {
            bar = bar.append(Component.text("  ✎", COL_WARN));
        }

        return bar;
    }

    private String currentValue(PromptBook.ParamSpec spec) {
        Object modified = session.modifiedParams().get(spec.name());
        if (modified instanceof Number n) return formatDouble(n.doubleValue());
        if (modified != null) return modified.toString();
        return spec.displayValue();
    }

    private static String formatDouble(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
    }
}
