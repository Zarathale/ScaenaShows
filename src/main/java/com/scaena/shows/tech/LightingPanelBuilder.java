package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Builds and sends the Phase 2 Lighting department edit panels.
 *
 * Four top-level views dispatched by event mode:
 *   TIME_OF_DAY         — scroll wheel ±1000t / ±100t; arc label + snap offer;
 *                         world_preview toggle; [▶ Apply to world] in VALUES_ONLY
 *   TIME_OF_DAY_PATTERN — start/end/steps/duration fields + curve selector;
 *                         world_preview toggle; explicit [▶ Preview]
 *   WEATHER             — state selector + optional duration; world_preview toggle
 *   LIGHTNING           — anchor selector + x/y/z offsets; OPS-034 warning;
 *                         auto-preview toggle + [▶ Preview]
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Known-gap warning shown inline for LIGHTNING when anchor: player is selected.
 *
 * Spec: kb/system/phase2-department-panels.md §Lighting
 */
public final class LightingPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0x88CCFF); // sky blue (Lighting)
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (active)
    private static final TextColor COL_BTN_INCR = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_WARN     = TextColor.color(0xFF4444); // red (warning)
    private static final TextColor COL_SNAP     = TextColor.color(0xAAFFAA); // light green (snap)
    private static final TextColor COL_SKY      = TextColor.color(0xCCEEFF); // pale sky (sky state label)

    private static final String SEP = "─────────────────────────────────────────────";

    private LightingPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /** Send the appropriate panel for the session's current event mode. */
    public static void sendPanel(Player p, LightingEditSession session) {
        switch (session.getMode()) {
            case TIME_OF_DAY         -> sendTimeOfDayPanel(p, session);
            case TIME_OF_DAY_PATTERN -> sendTimeOfDayPatternPanel(p, session);
            case WEATHER             -> sendWeatherPanel(p, session);
            case LIGHTNING           -> sendLightningPanel(p, session);
        }
    }

    // -----------------------------------------------------------------------
    // TIME_OF_DAY panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * LIGHTING  lighting.time.dusk_settle
     *
     *   Time:   13200 — near Dusk (13000)    [−] [+]
     *           (Shift+scroll: fine ±100t)
     *
     *   [Snap to: Dusk (13000)]               ← only when within ~200t
     *
     *   World preview: LIVE  [Toggle]  [Remember this]
     *   (Server-wide: affects all players)
     * ─────────────────────────────────────────────
     * [Save]   [Save as Preset]   [Cancel]
     */
    private static void sendTimeOfDayPanel(Player p, LightingEditSession session) {
        long time = session.getCurrentTime();
        String arc = LightingEditSession.arcLabel(time);
        boolean isLive = session.getWorldPreview() == TechCueSession.WorldPreviewMode.LIVE;

        p.sendMessage(sep());
        p.sendMessage(Component.text("LIGHTING  ", COL_DEPT)
            .append(Component.text(session.cueId(), COL_HEADER)));
        p.sendMessage(Component.empty());

        // Time row
        Component timeRow = Component.text("  ", COL_LABEL)
            .append(Component.text("Time: ", COL_LABEL))
            .append(Component.text(arc, COL_VALUE))
            .append(Component.text("    "))
            .append(incrBtn("-", "/scaena tech2 editparam time_down", "−1000t"))
            .append(Component.text(" "))
            .append(incrBtn("+", "/scaena tech2 editparam time_up", "+1000t"));
        p.sendMessage(timeRow);

        p.sendMessage(Component.text("          ", COL_LABEL)
            .append(Component.text("(Shift+scroll: fine ±100t)", COL_HINT)));

        // Snap offer
        int snapIdx = LightingEditSession.snapCandidateIndex(time);
        if (snapIdx >= 0) {
            String snapName  = LightingEditSession.SKY_STATE_NAMES[snapIdx];
            long   snapValue = LightingEditSession.SKY_STATE_TICKS[snapIdx];
            p.sendMessage(Component.empty());
            p.sendMessage(
                clickBtn("[Snap to: " + snapName + " (" + snapValue + ")]",
                    "/scaena tech2 editparam time_snap " + snapValue,
                    COL_SNAP,
                    "Set time to exactly " + snapValue + "t (" + snapName + ")")
            );
        }

        p.sendMessage(Component.empty());

        // World preview toggle
        p.sendMessage(buildWorldPreviewLine(session));
        if (!isLive) {
            p.sendMessage(
                Component.text("  ", COL_LABEL)
                    .append(clickBtn("[▶ Apply to world]",
                        "/scaena tech2 editparam world_preview_apply",
                        COL_BTN, "Apply current time to world once"))
            );
        }
        p.sendMessage(Component.text("  ", COL_LABEL)
            .append(Component.text("(Server-wide: affects all players)", COL_HINT)));

        p.sendMessage(sep());
        p.sendMessage(buildSaveCancelRow(false)); // TIME_OF_DAY has no preset
    }

    // -----------------------------------------------------------------------
    // TIME_OF_DAY_PATTERN panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * LIGHTING (PATTERN)  lighting.dusk.battle_open
     *
     *   Start:   0 — Morning (0)          [−] [+]
     *   End:  13000 — Dusk (13000)        [−] [+]
     *   Steps:    6   (every 24t  ~1.2s)  [−] [+]
     *   Duration: 120t (6s)               [−] [+]
     *   Curve:   [linear]  ease_in  ease_out
     *
     *   World preview: LIVE  [Toggle]  [Remember this]
     *   (Server-wide: affects all players)
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     * [Save]   [Save as Preset]   [Cancel]
     */
    private static void sendTimeOfDayPatternPanel(Player p, LightingEditSession session) {
        long   start  = session.getCurrentStartValue();
        long   end    = session.getCurrentEndValue();
        int    steps  = session.getCurrentSteps();
        int    dur    = session.getCurrentTotalDuration();
        String curve  = session.getCurrentCurve();

        p.sendMessage(sep());
        p.sendMessage(Component.text("LIGHTING (PATTERN)  ", COL_DEPT)
            .append(Component.text(session.cueId(), COL_HEADER)));
        p.sendMessage(Component.empty());

        // Start value
        Component startRow = Component.text("  ", COL_LABEL)
            .append(Component.text("Start: ", COL_LABEL))
            .append(Component.text(LightingEditSession.arcLabel(start), COL_VALUE))
            .append(Component.text("    "))
            .append(incrBtn("-", "/scaena tech2 editparam start_down", "−1000t"))
            .append(Component.text(" "))
            .append(incrBtn("+", "/scaena tech2 editparam start_up", "+1000t"));
        p.sendMessage(startRow);

        // Snap offer for start
        int startSnap = LightingEditSession.snapCandidateIndex(start);
        if (startSnap >= 0) {
            String sn = LightingEditSession.SKY_STATE_NAMES[startSnap];
            long   sv = LightingEditSession.SKY_STATE_TICKS[startSnap];
            p.sendMessage(Component.text("         ").append(
                clickBtn("[Snap start: " + sn + " (" + sv + ")]",
                    "/scaena tech2 editparam start_snap " + sv,
                    COL_SNAP, "Snap start to " + sv + "t")));
        }

        // End value
        Component endRow = Component.text("  ", COL_LABEL)
            .append(Component.text("End:   ", COL_LABEL))
            .append(Component.text(LightingEditSession.arcLabel(end), COL_VALUE))
            .append(Component.text("    "))
            .append(incrBtn("-", "/scaena tech2 editparam end_down", "−1000t"))
            .append(Component.text(" "))
            .append(incrBtn("+", "/scaena tech2 editparam end_up", "+1000t"));
        p.sendMessage(endRow);

        // Snap offer for end
        int endSnap = LightingEditSession.snapCandidateIndex(end);
        if (endSnap >= 0) {
            String sn = LightingEditSession.SKY_STATE_NAMES[endSnap];
            long   sv = LightingEditSession.SKY_STATE_TICKS[endSnap];
            p.sendMessage(Component.text("         ").append(
                clickBtn("[Snap end: " + sn + " (" + sv + ")]",
                    "/scaena tech2 editparam end_snap " + sv,
                    COL_SNAP, "Snap end to " + sv + "t")));
        }

        // Steps
        String stepInterval = LightingEditSession.formatStepInterval(dur, steps);
        Component stepsRow = Component.text("  ", COL_LABEL)
            .append(Component.text("Steps: ", COL_LABEL))
            .append(Component.text(steps + "  ", COL_VALUE))
            .append(Component.text("(" + stepInterval + ")", COL_SKY))
            .append(Component.text("    "))
            .append(incrBtn("-", "/scaena tech2 editparam steps_down", "−1 step"))
            .append(Component.text(" "))
            .append(incrBtn("+", "/scaena tech2 editparam steps_up", "+1 step"));
        p.sendMessage(stepsRow);

        // Total duration
        Component durRow = Component.text("  ", COL_LABEL)
            .append(Component.text("Duration: ", COL_LABEL))
            .append(Component.text(LightingEditSession.formatDuration(dur), COL_VALUE))
            .append(Component.text("    "))
            .append(incrBtn("-", "/scaena tech2 editparam dur_down", "−20t"))
            .append(Component.text(" "))
            .append(incrBtn("+", "/scaena tech2 editparam dur_up", "+20t"));
        p.sendMessage(durRow);

        // Curve selector
        p.sendMessage(buildCurveRow(curve));

        p.sendMessage(Component.empty());
        p.sendMessage(buildWorldPreviewLine(session));
        p.sendMessage(Component.text("  ", COL_LABEL)
            .append(Component.text("(Server-wide: affects all players)", COL_HINT)));

        // Pattern preview button (always explicit — auto-preview OFF for patterns per spec)
        p.sendMessage(
            Component.text("  ")
                .append(clickBtn("[▶ Preview]",
                    "/scaena tech2 editparam preview_pattern",
                    COL_BTN, "Fire the full time-of-day transition in real time"))
        );

        p.sendMessage(sep());
        p.sendMessage(buildSaveCancelRow(true));
    }

    // -----------------------------------------------------------------------
    // WEATHER panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * LIGHTING WEATHER  lighting.weather.storm_enters
     *
     *   State:   [clear]  storm  thunder
     *   Duration: 400t (20s)    [−] [+]  [Clear]
     *
     *   World preview: LIVE  [Toggle]  [Remember this]
     *   (Server-wide: affects all players)
     * ─────────────────────────────────────────────
     * [Save]   [Cancel]
     */
    private static void sendWeatherPanel(Player p, LightingEditSession session) {
        String state = session.getCurrentWeatherState();
        int    dur   = session.getCurrentWeatherDuration();
        boolean isLive = session.getWorldPreview() == TechCueSession.WorldPreviewMode.LIVE;

        p.sendMessage(sep());
        p.sendMessage(Component.text("LIGHTING WEATHER  ", COL_DEPT)
            .append(Component.text(session.cueId(), COL_HEADER)));
        p.sendMessage(Component.empty());

        // State selector
        p.sendMessage(buildWeatherStateRow(state));

        // Duration row
        if (dur < 0) {
            Component durRow = Component.text("  ", COL_LABEL)
                .append(Component.text("Duration: ", COL_LABEL))
                .append(Component.text("(persistent)", COL_HINT))
                .append(Component.text("    "))
                .append(incrBtn("+", "/scaena tech2 editparam dur_up", "Set duration +20t"));
            p.sendMessage(durRow);
        } else {
            Component durRow = Component.text("  ", COL_LABEL)
                .append(Component.text("Duration: ", COL_LABEL))
                .append(Component.text(LightingEditSession.formatDuration(dur) + "  ", COL_VALUE))
                .append(incrBtn("-", "/scaena tech2 editparam dur_down", "−20t"))
                .append(Component.text(" "))
                .append(incrBtn("+", "/scaena tech2 editparam dur_up", "+20t"))
                .append(Component.text("  "))
                .append(clickBtn("[Clear]",
                    "/scaena tech2 editparam dur_clear",
                    COL_BTN, "Remove duration (persistent weather)"));
            p.sendMessage(durRow);
        }

        p.sendMessage(Component.empty());
        p.sendMessage(buildWorldPreviewLine(session));
        if (!isLive) {
            p.sendMessage(
                Component.text("  ").append(clickBtn("[▶ Apply to world]",
                    "/scaena tech2 editparam world_preview_apply",
                    COL_BTN, "Apply current weather to world once"))
            );
        }
        p.sendMessage(Component.text("  ", COL_LABEL)
            .append(Component.text("(Server-wide: affects all players)", COL_HINT)));

        p.sendMessage(sep());
        p.sendMessage(buildSaveCancelRow(false)); // WEATHER has no preset
    }

    // -----------------------------------------------------------------------
    // LIGHTNING panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * LIGHTNING  lighting.lightning.player.surprise_close
     *
     *   Anchor:  [scene_origin]  player
     *   ⚠ player anchor requires OPS-034...   ← only when anchor=player
     *
     *   Offset:  x: 1  [−] [+]   y: 0  [−] [+]   z: 1  [−] [+]
     *            (Shift: ±5 per notch)
     *
     *   Auto-preview: ON  [Toggle]    [▶ Preview]
     * ─────────────────────────────────────────────
     * [Save]   [Save as Preset]   [Cancel]
     */
    private static void sendLightningPanel(Player p, LightingEditSession session) {
        String anchor = session.getCurrentAnchor();
        double offX   = session.getCurrentOffX();
        double offY   = session.getCurrentOffY();
        double offZ   = session.getCurrentOffZ();
        boolean autoOn = session.isAutoPreview();

        p.sendMessage(sep());
        p.sendMessage(Component.text("LIGHTNING  ", COL_DEPT)
            .append(Component.text(session.cueId(), COL_HEADER)));
        p.sendMessage(Component.empty());

        // Anchor selector
        p.sendMessage(buildAnchorRow(anchor));

        // OPS-034 warning when player anchor selected
        if ("player".equalsIgnoreCase(anchor)) {
            p.sendMessage(Component.text(
                "  \u26a0 player anchor requires OPS-034 \u2014 save is valid; live execution may resolve to origin until fixed",
                COL_WARN));
        }

        p.sendMessage(Component.empty());

        // Offset rows
        p.sendMessage(buildOffsetRow("x", offX, "off_x"));
        p.sendMessage(buildOffsetRow("y", offY, "off_y"));
        p.sendMessage(buildOffsetRow("z", offZ, "off_z"));
        p.sendMessage(Component.text("           ", COL_LABEL)
            .append(Component.text("(Shift: ±5 per notch)", COL_HINT)));

        p.sendMessage(Component.empty());

        // Auto-preview + explicit preview button
        Component autoLine = Component.text("  ", COL_LABEL)
            .append(Component.text("Auto-preview: ", COL_LABEL))
            .append(Component.text(autoOn ? "ON " : "OFF", autoOn ? COL_BTN_ON : COL_BTN))
            .append(Component.text("  "))
            .append(clickBtn("[Toggle]",
                "/scaena tech2 editparam auto_preview_toggle",
                COL_BTN, autoOn ? "Disable auto-preview" : "Enable auto-preview"))
            .append(Component.text("    "))
            .append(clickBtn("[▶ Preview]",
                "/scaena tech2 editparam preview_strike",
                COL_BTN, "Fire a test lightning strike at current offset"));
        p.sendMessage(autoLine);

        p.sendMessage(sep());
        p.sendMessage(buildSaveCancelRow(true));
    }

    // -----------------------------------------------------------------------
    // Shared component builders
    // -----------------------------------------------------------------------

    /**
     * World preview toggle line — shown in TIME_OF_DAY, TIME_OF_DAY_PATTERN, WEATHER panels.
     *   World preview: LIVE  [Toggle]  [Remember this]
     */
    private static Component buildWorldPreviewLine(LightingEditSession session) {
        boolean isLive = session.getWorldPreview() == TechCueSession.WorldPreviewMode.LIVE;
        String modeLabel = isLive ? "LIVE" : "VALUES_ONLY";
        TextColor modeColor = isLive ? COL_BTN_ON : COL_BTN;

        return Component.text("  ", COL_LABEL)
            .append(Component.text("World preview: ", COL_LABEL))
            .append(Component.text(modeLabel + "  ", modeColor))
            .append(clickBtn("[Toggle]",
                "/scaena tech2 editparam world_preview_toggle",
                COL_BTN,
                isLive ? "Switch to VALUES_ONLY — edits update values only, no world change"
                       : "Switch to LIVE — every param change applies to the world immediately"))
            .append(Component.text("  "))
            .append(clickBtn("[Remember this]",
                "/scaena tech2 editparam world_preview_remember",
                COL_HINT,
                "Save " + modeLabel + " as the default for all future Phase 2 sessions"));
    }

    /** Weather state selector row: [clear]  storm  thunder */
    private static Component buildWeatherStateRow(String current) {
        Component row = Component.text("  ", COL_LABEL)
            .append(Component.text("State:  ", COL_LABEL));
        for (String opt : LightingEditSession.WEATHER_STATES) {
            boolean active = opt.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + opt + "]" : opt,
                    "/scaena tech2 editparam state " + opt,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set weather to " + opt)
            ).append(Component.text("  "));
        }
        return row;
    }

    /** Curve selector row: [linear]  ease_in  ease_out */
    private static Component buildCurveRow(String current) {
        Component row = Component.text("  ", COL_LABEL)
            .append(Component.text("Curve:  ", COL_LABEL));
        for (String opt : LightingEditSession.CURVES) {
            boolean active = opt.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + opt + "]" : opt,
                    "/scaena tech2 editparam curve " + opt,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set curve to " + opt)
            ).append(Component.text("  "));
        }
        return row;
    }

    /** Anchor selector row: [scene_origin]  player */
    private static Component buildAnchorRow(String current) {
        Component row = Component.text("  ", COL_LABEL)
            .append(Component.text("Anchor:  ", COL_LABEL));
        for (String opt : LightingEditSession.ANCHORS) {
            boolean active = opt.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + opt + "]" : opt,
                    "/scaena tech2 editparam anchor " + opt,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set anchor to " + opt)
            ).append(Component.text("  "));
        }
        return row;
    }

    /**
     * Offset field row: "  x: 1.0  [−] [+]"
     * paramBase is e.g. "off_x" → generates editparam keys "off_x_down", "off_x_up"
     */
    private static Component buildOffsetRow(String axis, double val, String paramBase) {
        String valStr = formatOffset(val);
        return Component.text("  ", COL_LABEL)
            .append(Component.text(axis + ": ", COL_LABEL))
            .append(Component.text(valStr + "  ", COL_VALUE))
            .append(incrBtn("-", "/scaena tech2 editparam " + paramBase + "_down", "−1"))
            .append(Component.text(" "))
            .append(incrBtn("+", "/scaena tech2 editparam " + paramBase + "_up", "+1"));
    }

    /**
     * Save / Save as Preset / Cancel footer row.
     * @param hasPreset whether to include [Save as Preset]
     */
    private static Component buildSaveCancelRow(boolean hasPreset) {
        Component row = clickBtn("[Save]",
            "/scaena tech2 editparam save",
            COL_BTN_ON, "Save changes to this cue");

        if (hasPreset) {
            row = row
                .append(Component.text("   "))
                .append(clickBtn("[Save as Preset]",
                    "/scaena tech2 editparam save_preset",
                    COL_BTN, "Save as a reusable preset in the cue library"));
        }
        row = row
            .append(Component.text("   "))
            .append(clickBtn("[Cancel]",
                "/scaena tech2 editparam cancel",
                COL_HINT, "Discard changes"));
        return row;
    }

    // -----------------------------------------------------------------------
    // Primitive component builders
    // -----------------------------------------------------------------------

    /** A clickable increment/decrement button with consistent styling. */
    private static Component incrBtn(String label, String cmd, String hoverText) {
        return Component.text("[" + label + "]", COL_BTN_INCR)
            .clickEvent(ClickEvent.runCommand(cmd))
            .hoverEvent(HoverEvent.showText(Component.text(hoverText, COL_HINT)));
    }

    /** A clickable labelled button with configurable colour. */
    private static Component clickBtn(String label, String cmd, TextColor color, String hoverText) {
        return Component.text(label, color)
            .clickEvent(ClickEvent.runCommand(cmd))
            .hoverEvent(HoverEvent.showText(Component.text(hoverText, COL_HINT)));
    }

    private static Component sep() {
        return Component.text(SEP, COL_SEP);
    }

    /** Format an offset double: show one decimal place only when non-integer. */
    private static String formatOffset(double v) {
        return (v == Math.floor(v) && !Double.isInfinite(v))
            ? String.valueOf((int) v)
            : String.format("%.1f", v);
    }
}
