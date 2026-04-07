package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Builds and sends the Phase 2 Fireworks department edit panels.
 *
 * Five top-level views dispatched by event mode:
 *   FIREWORK          — anchor, preset, offset {x/y/z}, y_mode + auto-preview
 *   CIRCLE            — subtype selector + anchor, preset, circle params, chase + auto-preview
 *   LINE              — subtype selector + anchor, preset, line params, chase + auto-preview
 *   RANDOM            — subtype selector + anchor, preset, random params, seed + auto-preview
 *   FAN_UNSUPPORTED   — informational "not supported in Phase 2" message
 *   PHRASE_UNSUPPORTED— informational "not supported in Phase 2" message
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Known-gap warnings shown inline:
 *   - anchor: player → OPS-034 dependency note
 *   - LINE + GRADIENT color_variation → gradient_from/to not applied by executor
 *
 * Spec: kb/system/phase2-department-panels.md §Fireworks
 */
public final class FireworksPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0xFF8800); // amber (Fireworks)
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (active)
    private static final TextColor COL_BTN_INCR = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_BTN_DIM  = TextColor.color(0x555555); // dark gray (unavailable)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_WARN     = TextColor.color(0xFF4444); // red (warning)

    private static final String SEP = "─────────────────────────────────────────────";

    private FireworksPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /** Send the appropriate panel for the session's current event mode. */
    public static void sendPanel(Player p, FireworksEditSession session) {
        switch (session.getMode()) {
            case FIREWORK          -> sendFireworkPanel(p, session);
            case CIRCLE            -> sendPatternPanel(p, session);
            case LINE              -> sendPatternPanel(p, session);
            case RANDOM            -> sendPatternPanel(p, session);
            case FAN_UNSUPPORTED   -> sendUnsupportedPanel(p, session, "FIREWORK_FAN",
                "FAN not yet supported in Phase 2 — deferred to Phase 3.");
            case PHRASE_UNSUPPORTED -> sendUnsupportedPanel(p, session, "FIREWORK_PHRASE",
                "FIREWORK_PHRASE not yet supported in Phase 2 — requires PHRASE step-list infrastructure.");
        }
    }

    // -----------------------------------------------------------------------
    // FIREWORK (single) panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * FIREWORK  fireworks.burst.victory_single
     *
     *   Anchor:   [scene_origin]  player
     *   Preset:   scae_star_warm    [Change ▸]
     *   Offset:   x 0.0  [−] [+]   y 2.0  [−] [+]   z 0.0  [−] [+]
     *   Y-mode:   [relative]  surface
     *
     *   Auto-preview: ON  [Toggle]    [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendFireworkPanel(Player p, FireworksEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("FIREWORK", session.cueId());

        Component anchorRow  = buildAnchorRow(session.getCurrentFwAnchor());
        Component anchorWarn = buildAnchorWarning(session.getCurrentFwAnchor());
        Component presetRow  = buildFieldRow("Preset:", session.getCurrentFwPreset().isEmpty() ? "(none)" : session.getCurrentFwPreset(),
            clickBtn("[Change ▸]", "/scaena tech2 editparam preset", COL_BTN, "Type: /scaena tech2 editparam preset <id>"));
        Component offsetRow  = buildOffsetXYZRow(
            session.getCurrentFwOffX(), session.getCurrentFwOffY(), session.getCurrentFwOffZ(),
            "off_x_down", "off_x_up", "off_y_down", "off_y_up", "off_z_down", "off_z_up");
        Component yModeRow   = buildYModeRow(session.getCurrentFwYMode(), "ymode");
        Component previewRow = buildAutoPreviewRow(session.isAutoPreview(),
            "preview_firework", "Fire a single test rocket at your location");

        if (anchorWarn != null) {
            p.sendMessage(buildPanel(sep, header, anchorRow, anchorWarn, presetRow, offsetRow, yModeRow,
                Component.empty(), previewRow));
        } else {
            p.sendMessage(buildPanel(sep, header, anchorRow, presetRow, offsetRow, yModeRow,
                Component.empty(), previewRow));
        }
    }

    // -----------------------------------------------------------------------
    // FIREWORK_PATTERN panel (CIRCLE / LINE / RANDOM with subtype selector)
    // -----------------------------------------------------------------------

    /**
     * Shared panel for all three PATTERN subtypes. The subtype selector row
     * at the top lets the designer switch between CIRCLE, LINE, and RANDOM
     * without leaving the edit session (shared fields are preserved across
     * subtype switches).
     *
     * FAN is shown as unavailable (greyed out, no click action).
     */
    private static void sendPatternPanel(Player p, FireworksEditSession session) {
        FireworksEditSession.EventMode mode = session.getMode();
        Component sep    = Component.text(SEP, COL_SEP);
        String modeName  = switch (mode) {
            case CIRCLE -> "FIREWORK_CIRCLE";
            case LINE   -> "FIREWORK_LINE";
            default     -> "FIREWORK_RANDOM";
        };
        Component header     = deptHeader(modeName, session.cueId());
        Component subtypeRow = buildSubtypeRow(mode);
        Component anchorRow  = buildAnchorRow(session.getCurrentPatAnchor());
        Component anchorWarn = buildAnchorWarning(session.getCurrentPatAnchor());
        Component presetRow  = buildFieldRow("Preset:", session.getCurrentPatPreset().isEmpty() ? "(none)" : session.getCurrentPatPreset(),
            clickBtn("[Change ▸]", "/scaena tech2 editparam preset", COL_BTN, "Type: /scaena tech2 editparam preset <id>"));
        Component yModeRow   = buildYModeRow(session.getCurrentPatYMode(), "ymode");
        Component yOffRow    = buildIncrRow("Y-offset:", formatDouble(session.getCurrentPatYOffset()),
            "/scaena tech2 editparam yoffset_down", "/scaena tech2 editparam yoffset_up", "−0.5", "+0.5");
        Component powerRow   = buildVariationRow("Power:", session.getCurrentPatPowerVar(),
            FireworksEditSession.POWER_VARIATIONS, "power_var");
        Component colorRow   = buildVariationRow("Color:", session.getCurrentPatColorVar(),
            FireworksEditSession.COLOR_VARIATIONS, "color_var");
        Component previewRow = buildAutoPreviewRow(session.isAutoPreview(),
            "preview_firework", "Fire a test burst at your location");

        // Build subtype-specific rows, then assemble
        switch (mode) {
            case CIRCLE -> {
                Component radiusRow  = buildIncrRow("Radius:", formatDouble(session.getCurrentCircleRadius()),
                    "/scaena tech2 editparam radius_down", "/scaena tech2 editparam radius_up", "−1", "+1");
                Component countRow   = buildIncrRow("Count:", String.valueOf(session.getCurrentCircleCount()),
                    "/scaena tech2 editparam count_down", "/scaena tech2 editparam count_up", "−1", "+1");
                Component origRow    = buildXZRow("Origin offset:", session.getCurrentCircleOriginX(), session.getCurrentCircleOriginZ(),
                    "origin_x_down", "origin_x_up", "origin_z_down", "origin_z_up");
                Component chaseRow   = buildChaseRow(session);
                Component gradRow    = buildGradientRowIfNeeded(session);

                Component[] rows = gradRow != null
                    ? new Component[]{subtypeRow, anchorRow, presetRow, radiusRow, countRow, origRow,
                        yModeRow, yOffRow, powerRow, colorRow, gradRow, chaseRow, Component.empty(), previewRow}
                    : new Component[]{subtypeRow, anchorRow, presetRow, radiusRow, countRow, origRow,
                        yModeRow, yOffRow, powerRow, colorRow, chaseRow, Component.empty(), previewRow};
                if (anchorWarn != null) rows = prepend(anchorWarn, rows, 2);
                p.sendMessage(buildPanel(sep, header, rows));
            }
            case LINE -> {
                Component lengthRow  = buildIncrRow("Length:", formatDouble(session.getCurrentLineLength()),
                    "/scaena tech2 editparam length_down", "/scaena tech2 editparam length_up", "−1", "+1");
                Component angleRow   = buildAngleRow(session.getCurrentLineAngle());
                Component countRow   = buildIncrRow("Count:", String.valueOf(session.getCurrentLineCount()),
                    "/scaena tech2 editparam count_down", "/scaena tech2 editparam count_up", "−1", "+1");
                Component startRow   = buildXZRow("Start offset:", session.getCurrentLineStartX(), session.getCurrentLineStartZ(),
                    "start_x_down", "start_x_up", "start_z_down", "start_z_up");
                Component chaseRow   = buildChaseRow(session);
                Component gradRow    = buildGradientRowIfNeeded(session);
                // LINE + GRADIENT known-gap warning
                Component lineGradWarn = ("GRADIENT".equals(session.getCurrentPatColorVar()))
                    ? Component.text("  \u26a0 gradient_from/to not applied on LINE \u2014 defaults to red\u2192blue (ops-inbox gap)", COL_WARN)
                    : null;

                Component[] rows = assembleLineRows(subtypeRow, anchorRow, presetRow,
                    lengthRow, angleRow, countRow, startRow, yModeRow, yOffRow, powerRow,
                    colorRow, gradRow, lineGradWarn, chaseRow, previewRow);
                if (anchorWarn != null) rows = prepend(anchorWarn, rows, 2);
                p.sendMessage(buildPanel(sep, header, rows));
            }
            case RANDOM -> {
                Component radiusRow  = buildIncrRow("Radius:", formatDouble(session.getCurrentRandomRadius()),
                    "/scaena tech2 editparam radius_down", "/scaena tech2 editparam radius_up", "−1", "+1");
                Component countRow   = buildIncrRow("Count:", String.valueOf(session.getCurrentRandomCount()),
                    "/scaena tech2 editparam count_down", "/scaena tech2 editparam count_up", "−1", "+1");
                Component origRow    = buildXZRow("Origin offset:", session.getCurrentRandomOriginX(), session.getCurrentRandomOriginZ(),
                    "origin_x_down", "origin_x_up", "origin_z_down", "origin_z_up");
                Component gradRow    = buildGradientRowIfNeeded(session);
                Component seedRow    = buildSeedRow(session.getCurrentRandomSeed());

                Component[] rows = gradRow != null
                    ? new Component[]{subtypeRow, anchorRow, presetRow, radiusRow, countRow, origRow,
                        yModeRow, yOffRow, powerRow, colorRow, gradRow, seedRow, Component.empty(), previewRow}
                    : new Component[]{subtypeRow, anchorRow, presetRow, radiusRow, countRow, origRow,
                        yModeRow, yOffRow, powerRow, colorRow, seedRow, Component.empty(), previewRow};
                if (anchorWarn != null) rows = prepend(anchorWarn, rows, 2);
                p.sendMessage(buildPanel(sep, header, rows));
            }
            default -> { /* unreachable */ }
        }
    }

    // -----------------------------------------------------------------------
    // Unsupported mode panel
    // -----------------------------------------------------------------------

    private static void sendUnsupportedPanel(Player p, FireworksEditSession session,
                                             String typeName, String reason) {
        Component sep  = Component.text(SEP, COL_SEP);
        Component header = deptHeader(typeName, session.cueId());
        Component msg  = Component.text("  " + reason, COL_WARN);
        Component hint = Component.text("  Use [Cancel] to exit without saving.", COL_HINT);
        p.sendMessage(buildPanel(sep, header, msg, hint));
    }

    // -----------------------------------------------------------------------
    // Row builders
    // -----------------------------------------------------------------------

    /** Anchor selector row: [scene_origin]  player */
    private static Component buildAnchorRow(String current) {
        Component row = Component.text("  ")
            .append(Component.text("Anchor:", COL_LABEL))
            .append(Component.text("  "));
        for (String opt : FireworksEditSession.ANCHORS) {
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
     * OPS-034 dependency warning — shown when player anchor is active.
     * Returns null when anchor is scene_origin (no warning needed).
     */
    private static Component buildAnchorWarning(String current) {
        if (!"player".equalsIgnoreCase(current)) return null;
        return Component.text(
            "  \u26a0 player anchor requires OPS-034 \u2014 save is valid; live execution may resolve to origin until fixed",
            COL_WARN);
    }

    /** Subtype selector row: CIRCLE  [LINE]  RANDOM  FAN(unavailable) */
    private static Component buildSubtypeRow(FireworksEditSession.EventMode current) {
        Component row = Component.text("  ")
            .append(Component.text("Subtype:", COL_LABEL))
            .append(Component.text("  "));

        String[] subtypes = {"CIRCLE", "LINE", "RANDOM"};
        FireworksEditSession.EventMode[] modes = {
            FireworksEditSession.EventMode.CIRCLE,
            FireworksEditSession.EventMode.LINE,
            FireworksEditSession.EventMode.RANDOM
        };
        for (int i = 0; i < subtypes.length; i++) {
            boolean active = current == modes[i];
            row = row.append(
                clickBtn(active ? "[" + subtypes[i] + "]" : subtypes[i],
                    "/scaena tech2 editparam subtype " + subtypes[i],
                    active ? COL_BTN_ON : COL_BTN,
                    "Switch to " + subtypes[i] + " pattern")
            ).append(Component.text("  "));
        }
        // FAN — unavailable, no click
        row = row.append(Component.text("FAN", COL_BTN_DIM))
            .append(Component.text("(unavailable)", COL_HINT));
        return row;
    }

    /** Y-mode selector row: [relative]  surface */
    private static Component buildYModeRow(String current, String paramKey) {
        Component row = Component.text("  ")
            .append(Component.text("Y-mode:", COL_LABEL))
            .append(Component.text("  "));
        for (String opt : FireworksEditSession.Y_MODES) {
            boolean active = opt.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + opt + "]" : opt,
                    "/scaena tech2 editparam " + paramKey + " " + opt,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set y_mode to " + opt)
            ).append(Component.text("  "));
        }
        return row;
    }

    /** Variation selector row (power or color): UNIFORM  [RAINBOW]  GRADIENT  ALTERNATE */
    private static Component buildVariationRow(String label, String current,
                                               java.util.List<String> options, String paramKey) {
        Component row = Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "));
        for (String opt : options) {
            boolean active = opt.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + opt + "]" : opt,
                    "/scaena tech2 editparam " + paramKey + " " + opt,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set " + paramKey + " to " + opt)
            ).append(Component.text("  "));
        }
        return row;
    }

    /**
     * Gradient pickers — only shown when color_variation is GRADIENT.
     * Returns null otherwise.
     */
    private static Component buildGradientRowIfNeeded(FireworksEditSession session) {
        if (!"GRADIENT".equals(session.getCurrentPatColorVar())) return null;
        return Component.text("  ")
            .append(Component.text("Gradient:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text("from ", COL_HINT))
            .append(Component.text(session.getCurrentPatGradFrom(), COL_VALUE))
            .append(Component.text("  \u2192  ", COL_HINT))
            .append(Component.text(session.getCurrentPatGradTo(), COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[from]", "/scaena tech2 editparam grad_from", COL_BTN,
                "Type: /scaena tech2 editparam grad_from #RRGGBB"))
            .append(Component.text(" "))
            .append(clickBtn("[to]", "/scaena tech2 editparam grad_to", COL_BTN,
                "Type: /scaena tech2 editparam grad_to #RRGGBB"));
    }

    /** Chase row: Chase:  [ON]  OFF  — dir: [FL]  LF  — interval: 4  [−] [+] */
    private static Component buildChaseRow(FireworksEditSession session) {
        boolean chaseOn  = session.isCurrentChaseEnabled();
        String  chaseDir = session.getCurrentChaseDir();
        int     interval = session.getCurrentChaseInterval();

        Component row = Component.text("  ")
            .append(Component.text("Chase:", COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn(chaseOn ? "[ON]" : "ON",
                "/scaena tech2 editparam chase_toggle",
                chaseOn ? COL_BTN_ON : COL_BTN, "Toggle chase"))
            .append(Component.text(" "))
            .append(clickBtn(chaseOn ? "OFF" : "[OFF]",
                "/scaena tech2 editparam chase_toggle",
                chaseOn ? COL_BTN_INCR : COL_BTN_ON, "Toggle chase"));

        if (chaseOn) {
            row = row.append(Component.text("  — dir: ", COL_HINT));
            for (String dir : FireworksEditSession.CHASE_DIRS) {
                boolean active = dir.equalsIgnoreCase(chaseDir);
                row = row.append(
                    clickBtn(active ? "[" + dir + "]" : dir,
                        "/scaena tech2 editparam chase_dir " + dir,
                        active ? COL_BTN_ON : COL_BTN,
                        "Set direction to " + dir)
                ).append(Component.text("  "));
            }
            row = row
                .append(Component.text("— interval: ", COL_HINT))
                .append(Component.text(interval + "t", COL_VALUE))
                .append(Component.text("  "))
                .append(clickBtn("[−]", "/scaena tech2 editparam chase_int_down", COL_BTN_INCR, "−1t"))
                .append(Component.text(" "))
                .append(clickBtn("[+]", "/scaena tech2 editparam chase_int_up", COL_BTN_INCR, "+1t"));
        }
        return row;
    }

    /** Angle row with compass bearing label and ±15° buttons */
    private static Component buildAngleRow(double angle) {
        String compassLabel = compassBearingLabel(angle);
        return Component.text("  ")
            .append(Component.text("Angle:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(String.format("%.0f\u00b0", angle), COL_VALUE))
            .append(Component.text("  (" + compassLabel + ")", COL_HINT))
            .append(Component.text("    "))
            .append(clickBtn("[−15\u00b0]", "/scaena tech2 editparam angle_down", COL_BTN_INCR, "−15°"))
            .append(Component.text(" "))
            .append(clickBtn("[+15\u00b0]", "/scaena tech2 editparam angle_up", COL_BTN_INCR, "+15°"));
    }

    /** Seed row: Seed:  12345678  [Seed it]  [Clear] or (random)  [Seed it] */
    private static Component buildSeedRow(Long seed) {
        Component seedDisplay = (seed != null)
            ? Component.text(Long.toString(seed), COL_VALUE)
            : Component.text("(random)", COL_HINT);
        Component row = Component.text("  ")
            .append(Component.text("Seed:", COL_LABEL))
            .append(Component.text("  "))
            .append(seedDisplay)
            .append(Component.text("    "))
            .append(clickBtn("[Seed it]", "/scaena tech2 editparam seed_randomize", COL_BTN,
                "Generate a new random seed for reproducible scatter"));
        if (seed != null) {
            row = row.append(Component.text("  "))
                .append(clickBtn("[Clear]", "/scaena tech2 editparam seed_clear", COL_BTN_INCR,
                    "Remove seed — scatter changes each run"));
        }
        return row;
    }

    /** XZ-only offset row (origin_offset / start_offset) */
    private static Component buildXZRow(String label, double x, double z,
                                        String xDn, String xUp, String zDn, String zUp) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text("x ", COL_HINT))
            .append(Component.text(formatDouble(x), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + xDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + xUp, COL_BTN_INCR, "+0.5"))
            .append(Component.text("   z ", COL_HINT))
            .append(Component.text(formatDouble(z), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + zDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + zUp, COL_BTN_INCR, "+0.5"));
    }

    /** XYZ offset row for FIREWORK (single) */
    private static Component buildOffsetXYZRow(double ox, double oy, double oz,
                                               String xDn, String xUp,
                                               String yDn, String yUp,
                                               String zDn, String zUp) {
        return Component.text("  ")
            .append(Component.text("Offset:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text("x ", COL_HINT))
            .append(Component.text(formatDouble(ox), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + xDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + xUp, COL_BTN_INCR, "+0.5"))
            .append(Component.text("   y ", COL_HINT))
            .append(Component.text(formatDouble(oy), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + yDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + yUp, COL_BTN_INCR, "+0.5"))
            .append(Component.text("   z ", COL_HINT))
            .append(Component.text(formatDouble(oz), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + zDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + zUp, COL_BTN_INCR, "+0.5"));
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    private static Component deptHeader(String modeName, String cueId) {
        return Component.text(modeName, COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  "))
            .append(Component.text(cueId, COL_HEADER));
    }

    private static Component buildFieldRow(String label, String value, Component actionBtn) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(value, COL_VALUE))
            .append(Component.text("    "))
            .append(actionBtn);
    }

    private static Component buildIncrRow(String label, String value,
                                          String cmdDown, String cmdUp,
                                          String tipDown, String tipUp) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(value, COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", cmdDown, COL_BTN_INCR, tipDown))
            .append(Component.text(" "))
            .append(clickBtn("[+]", cmdUp, COL_BTN_INCR, tipUp));
    }

    private static Component buildAutoPreviewRow(boolean autoOn, String previewCmd, String previewTooltip) {
        String    statusLabel = autoOn ? "ON" : "OFF";
        TextColor statusColor = autoOn ? COL_BTN_ON : COL_BTN_INCR;
        return Component.text("  ")
            .append(Component.text("Auto-preview:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(statusLabel, statusColor))
            .append(Component.text("    "))
            .append(clickBtn("[Toggle]",
                "/scaena tech2 editparam auto_preview_toggle",
                COL_BTN,
                autoOn
                    ? "Turn OFF auto-preview (manual [▶ Preview] only)"
                    : "Turn ON auto-preview (refires on every param change)"))
            .append(Component.text("    "))
            .append(clickBtn("[▶ Preview]",
                "/scaena tech2 editparam " + previewCmd,
                COL_BTN,
                previewTooltip));
    }

    /** Build and assemble a standard panel (separator + header + body rows). */
    private static Component buildPanel(Component sep, Component header, Component... rows) {
        Component panel = Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline());
        for (Component row : rows) {
            panel = panel.append(row).append(Component.newline());
        }
        panel = panel.append(sep);
        return panel;
    }

    private static Component clickBtn(String label, String command,
                                      TextColor color, String tooltip) {
        return Component.text(label, color, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, NamedTextColor.GRAY)));
    }

    /** Format a double for compact display: 1 decimal if clean, 2 otherwise. */
    private static String formatDouble(double v) {
        if (v == Math.floor(v) && Math.abs(v) < 1e6) return String.format("%.1f", v);
        return String.format("%.2f", v);
    }

    /** Human-readable compass label for a bearing angle (0–360, clockwise from north). */
    private static String compassBearingLabel(double deg) {
        deg = ((deg % 360) + 360) % 360;
        if (deg < 22.5 || deg >= 337.5)  return "north";
        if (deg < 67.5)                  return "NE";
        if (deg < 112.5)                 return "east";
        if (deg < 157.5)                 return "SE";
        if (deg < 202.5)                 return "south";
        if (deg < 247.5)                 return "SW";
        if (deg < 292.5)                 return "west";
        return "NW";
    }

    // -----------------------------------------------------------------------
    // LINE row assembly helper (handles null optional rows)
    // -----------------------------------------------------------------------

    private static Component[] assembleLineRows(
        Component subtypeRow, Component anchorRow, Component presetRow,
        Component lengthRow, Component angleRow, Component countRow, Component startRow,
        Component yModeRow, Component yOffRow, Component powerRow,
        Component colorRow, Component gradRow, Component lineGradWarn,
        Component chaseRow, Component previewRow
    ) {
        java.util.List<Component> list = new java.util.ArrayList<>();
        list.add(subtypeRow); list.add(anchorRow); list.add(presetRow);
        list.add(lengthRow); list.add(angleRow); list.add(countRow); list.add(startRow);
        list.add(yModeRow); list.add(yOffRow); list.add(powerRow); list.add(colorRow);
        if (gradRow != null)     list.add(gradRow);
        if (lineGradWarn != null) list.add(lineGradWarn);
        list.add(chaseRow); list.add(Component.empty()); list.add(previewRow);
        return list.toArray(new Component[0]);
    }

    /**
     * Insert {@code extra} at {@code pos} in {@code rows}, shifting the rest right.
     * Used to inject the anchor warning after the anchor row without restructuring
     * every conditional array.
     */
    private static Component[] prepend(Component extra, Component[] rows, int pos) {
        Component[] result = new Component[rows.length + 1];
        System.arraycopy(rows, 0, result, 0, pos);
        result[pos] = extra;
        System.arraycopy(rows, pos, result, pos + 1, rows.length - pos);
        return result;
    }
}
