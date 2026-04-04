package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Repeating task (every 5 ticks) that keeps the actionbar updated during a tech session.
 *
 * Display modes (priority order):
 *   Confirm      — brief confirmation flash, clears after 2 seconds
 *   Capture      — "📍 [mark] | x, y, z | Use slot 8 to [set arrival point | capture]"
 *   Param scroll — "⚙ [param] = [value]  [controls]"
 *   Normal       — "TECH · [show_id] · [scene_label]  [✎ if dirty]"
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
            Location loc  = session.player().getLocation();
            String mark   = session.focusedMark();
            String coords = String.format("%.1f, %.1f, %.1f",
                loc.getX(), loc.getY(), loc.getZ());
            boolean isArrival = isArrivalMark(mark);

            return Component.text("📍 ", COL_CAPTURE)
                .append(Component.text(mark != null ? mark : "?", COL_WARN))
                .append(Component.text(" | ", COL_SEP))
                .append(Component.text(coords, COL_SCENE))
                .append(Component.text(
                    isArrival ? " | Use slot 8 to set arrival point"
                              : " | Use slot 8 to capture",
                    COL_SEP));
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

        // Normal mode — sidebar already shows show ID and scene label.
        // Only surface the actionbar when there is genuinely new information: unsaved changes.
        // OPS-033: redundant context suppressed; transient modes (capture/param/confirm) unaffected.
        if (session.hasUnsavedChanges()) {
            return Component.text("✎  unsaved changes", COL_WARN);
        }

        return Component.empty();
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

    /**
     * Returns true if the given mark name is the arrival mark for the current scene.
     * Used to pick the right prompt text in capture mode.
     */
    private boolean isArrivalMark(String markName) {
        if (markName == null) return false;
        PromptBook.SceneSpec scene = session.book().findScene(session.currentSceneId());
        return scene != null && markName.equals(scene.arrivalMark());
    }
}
