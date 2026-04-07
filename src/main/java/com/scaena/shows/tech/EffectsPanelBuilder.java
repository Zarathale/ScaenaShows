package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Builds and sends the Phase 2 Effects department edit panels.
 *
 * Eight top-level views dispatched by event mode:
 *   EFFECT          — effect_id, amplifier, duration, hide_particles, audience + auto-preview
 *   PARTICLE        — particle_id, count, offset, extra, force, duration, interval + auto-preview
 *   PLAYER_TELEPORT — destination/offset, yaw, pitch, audience + preview button
 *   CROSS_TO        — destination, duration, facing, target + preview button
 *   PLAYER_VELOCITY — x/y/z vector + audience + preview button
 *   PLAYER_FLIGHT   — state (hover/release), release fields, audience + preview button
 *   EFFECT_PATTERN  — effect_id, amplifier, duration, cycle, total_duration + auto-preview
 *   EFFECT_PHRASE   — step-list editor (most complex)
 *
 * Sub-panels:
 *   Effect ID selector — curated list by KB category (Aerial, Perceptual)
 *   Particle ID selector — curated list by KB atmospheric category
 *   Step inline editor — single-step field editor (opened by [Edit] on a phrase step)
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Spec: kb/system/phase2-department-panels.md §Effects
 */
public final class EffectsPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0x44FF88); // green (Effects)
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (active)
    private static final TextColor COL_BTN_INCR = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_WARN     = TextColor.color(0xFF4444); // red (warning)
    private static final TextColor COL_CAT      = TextColor.color(0xAA88FF); // lavender (category headings)
    private static final TextColor COL_PILL     = TextColor.color(0x88FFCC); // teal (ID pills)
    private static final TextColor COL_STEP_NUM = TextColor.color(0xAAAAAA); // step number
    private static final TextColor COL_TYPE_EFF = TextColor.color(0x44FF88); // green (EFFECT step)
    private static final TextColor COL_TYPE_PAR = TextColor.color(0x88FFCC); // teal (PARTICLE step)
    private static final TextColor COL_TIMING   = TextColor.color(0xFFDD88); // amber (timing)
    private static final TextColor COL_CAMERA   = TextColor.color(0xFFBB44); // amber (Camera authority note)

    private static final String SEP = "─────────────────────────────────────────────";

    private EffectsPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /** Send the appropriate panel for the session's current event mode. */
    public static void sendPanel(Player p, EffectsEditSession session) {
        switch (session.getMode()) {
            case EFFECT          -> sendEffectPanel(p, session);
            case PARTICLE        -> sendParticlePanel(p, session);
            case PLAYER_TELEPORT -> sendTeleportPanel(p, session);
            case CROSS_TO        -> sendCrossToPanel(p, session);
            case PLAYER_VELOCITY -> sendVelocityPanel(p, session);
            case PLAYER_FLIGHT   -> sendFlightPanel(p, session);
            case EFFECT_PATTERN  -> sendPatternPanel(p, session);
            case EFFECT_PHRASE   -> {
                if (session.getEditingStepIndex() >= 0) {
                    sendStepPanel(p, session, session.getEditingStepIndex());
                } else {
                    sendPhrasePanel(p, session);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // EFFECT panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * EFFECT  effects.levitate.high.warrior_rise
     *
     *   Effect:        levitation         [Change ▸]
     *   Amplifier:     0  (subtle)   [−] [+]
     *   Duration:      200t (10s)    [−] [+]
     *   Hide particles: [ON]  OFF
     *   Audience:      [participants]  group_1  group_2  invoker
     *
     *   Auto-preview: ON  [Toggle]    [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendEffectPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("EFFECT", session.cueId());

        Component effectRow = buildFieldRow(
            "Effect:",
            session.getCurrentEffectId(),
            clickBtn("[Change ▸]",
                "/scaena tech2 editparam effect_id_panel open",
                COL_BTN,
                "Open effect selector"));

        String ampLabel = EffectsEditSession.amplifierLabel(session.getCurrentEffectAmplifier());
        Component ampDisplay = Component.text("" + session.getCurrentEffectAmplifier(), COL_VALUE)
            .append(ampLabel.isEmpty() ? Component.empty()
                : Component.text("  (" + ampLabel + ")", COL_HINT));
        Component ampRow = Component.text("  ")
            .append(Component.text("Amplifier:", COL_LABEL))
            .append(Component.text("  "))
            .append(ampDisplay)
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam amplifier_down", COL_BTN_INCR, "−1"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam amplifier_up", COL_BTN_INCR, "+1"));

        Component durRow     = buildTickIncrRow("Duration:", session.getCurrentEffectDurationTicks(),
            "dur_down", "dur_up", "−10t", "+10t");
        Component hideRow    = buildToggleRow("Hide particles:", session.isCurrentEffectHideParticles(), "hide_toggle");
        Component audRow     = buildAudienceRow(session.getCurrentEffectAudience(), "audience");
        Component previewRow = buildAutoPreviewRow(session.isAutoPreview(),
            "preview_effect", "Re-apply effect to yourself once");

        p.sendMessage(buildPanel(sep, header,
            effectRow, ampRow, durRow, hideRow, audRow,
            Component.empty(), previewRow));
    }

    // -----------------------------------------------------------------------
    // PARTICLE panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * PARTICLE  effects.particle.ash_quiet
     *
     *   Particle:      ash             [Change ▸]
     *   Count:         8    [−] [+]   (shift: [−10] [+10])
     *   Offset:        x 3.0  [−] [+]   y 2.0  [−] [+]   z 3.0  [−] [+]
     *   Extra:         0.00   [−] [+]
     *   Force:         [ON]  OFF
     *   Duration:      200t (10s)    [−] [+]  (shift: [−100t] [+100t])  [Clear]
     *   Interval:      10t — every 10t   [−] [+]
     *
     *   Auto-preview: ON  [Toggle]    [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendParticlePanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("PARTICLE", session.cueId());

        Component particleRow = buildFieldRow(
            "Particle:",
            stripMcPrefix(session.getCurrentParticleId()),
            clickBtn("[Change ▸]",
                "/scaena tech2 editparam particle_id_panel open",
                COL_BTN,
                "Open particle selector"));

        Component countRow = Component.text("  ")
            .append(Component.text("Count:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text("" + session.getCurrentParticleCount(), COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam count_down", COL_BTN_INCR, "−1"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam count_up", COL_BTN_INCR, "+1"))
            .append(Component.text("   "))
            .append(clickBtn("[−10]", "/scaena tech2 editparam count_shift_down", COL_BTN_INCR, "−10"))
            .append(Component.text(" "))
            .append(clickBtn("[+10]", "/scaena tech2 editparam count_shift_up", COL_BTN_INCR, "+10"));

        Component offsetRow = buildOffsetRow(
            session.getCurrentParticleOffX(),
            session.getCurrentParticleOffY(),
            session.getCurrentParticleOffZ(),
            "off_x_down", "off_x_up",
            "off_y_down", "off_y_up",
            "off_z_down", "off_z_up");

        String extraFmt = String.format("%.2f", session.getCurrentParticleExtra());
        Component extraRow = Component.text("  ")
            .append(Component.text("Extra:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(extraFmt, COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam extra_down", COL_BTN_INCR, "−0.01"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam extra_up", COL_BTN_INCR, "+0.01"));

        Component forceRow    = buildToggleRow("Force:", session.isCurrentParticleForce(), "force_toggle");
        Component durRow      = buildParticleDurRow(session);
        Component intervalRow = buildParticleIntervalRow(session);
        Component previewRow  = buildAutoPreviewRow(session.isAutoPreview(),
            "preview_particle", "Fire a test burst at your location");

        p.sendMessage(buildPanel(sep, header,
            particleRow, countRow, offsetRow, extraRow, forceRow,
            durRow, intervalRow,
            Component.empty(), previewRow));
    }

    // -----------------------------------------------------------------------
    // PLAYER_TELEPORT panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * PLAYER_TELEPORT  effects.entrance.arrival_flash
     *
     *   Destination:   set:EntryMark      [set:Name]  [offset mode]
     *   Yaw:           90°  (Camera authority)   [−] [+]
     *   Pitch:         0°   (Camera authority)   [−] [+]
     *   Audience:      [participants]  group_1  ...
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendTeleportPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("PLAYER_TELEPORT", session.cueId());

        boolean useOffset = session.isCurrentTeleportUseOffset();
        Component destRow;
        if (useOffset) {
            destRow = buildOffsetRow(
                session.getCurrentTeleportOffX(),
                session.getCurrentTeleportOffY(),
                session.getCurrentTeleportOffZ(),
                "tp_off_x_down", "tp_off_x_up",
                "tp_off_y_down", "tp_off_y_up",
                "tp_off_z_down", "tp_off_z_up");
        } else {
            String dest = session.getCurrentTeleportDestination();
            destRow = buildFieldRow(
                "Destination:",
                dest.isEmpty() ? "(not set)" : dest,
                clickBtn("[Set ▸]",
                    "/scaena tech2 editparam dest ",
                    COL_BTN,
                    "Type: /scaena tech2 editparam dest set:MarkName"));
        }
        Component modeToggle = Component.text("  ")
            .append(clickBtn(useOffset ? "[offset mode]" : "offset mode",
                "/scaena tech2 editparam offset_mode_toggle",
                useOffset ? COL_BTN_ON : COL_BTN_INCR,
                "Toggle offset mode vs. set:Name destination"))
            .append(Component.text("  "))
            .append(clickBtn(useOffset ? "set:Name" : "[set:Name]",
                "/scaena tech2 editparam offset_mode_toggle",
                useOffset ? COL_BTN_INCR : COL_BTN_ON,
                "Toggle offset mode vs. set:Name destination"));

        // Yaw row with Camera authority note
        Component yawRow = Component.text("  ")
            .append(Component.text("Yaw:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(String.format("%.0f°", session.getCurrentTeleportYaw()), COL_VALUE))
            .append(Component.text("  "))
            .append(Component.text("(Camera authority)", COL_CAMERA))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam yaw_down", COL_BTN_INCR, "−5°"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam yaw_up", COL_BTN_INCR, "+5°"));

        Component pitchRow = Component.text("  ")
            .append(Component.text("Pitch:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(String.format("%.0f°", session.getCurrentTeleportPitch()), COL_VALUE))
            .append(Component.text("  "))
            .append(Component.text("(Camera authority)", COL_CAMERA))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam pitch_down", COL_BTN_INCR, "−5°"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam pitch_up", COL_BTN_INCR, "+5°"));

        Component audRow    = buildAudienceRow(session.getCurrentTeleportAudience(), "tp_audience");
        Component prevRow   = buildPreviewOnlyRow("preview_teleport",
            "Fire the teleport once (offset mode fires immediately; set: marks resolve at runtime)");

        p.sendMessage(buildPanel(sep, header,
            destRow, modeToggle, yawRow, pitchRow, audRow,
            Component.empty(), prevRow));
    }

    // -----------------------------------------------------------------------
    // CROSS_TO panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * CROSS_TO  effects.cross.warrior_enter
     *
     *   Destination:   mark:center        [Set ▸]
     *   Duration:      60t (3s)   [−] [+]
     *   Facing:        [compass:north]  compass:east  ... none
     *   Target:        [participants]   ...
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendCrossToPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("CROSS_TO", session.cueId());

        String dest = session.getCurrentCrossDestination();
        Component destRow = buildFieldRow(
            "Destination:",
            dest.isEmpty() ? "(not set)" : dest,
            clickBtn("[Set ▸]",
                "/scaena tech2 editparam cross_dest ",
                COL_BTN,
                "Type: /scaena tech2 editparam cross_dest mark:MarkName"));

        Component durRow = buildTickIncrRow("Duration:",
            session.getCurrentCrossDurationTicks(),
            "cross_dur_down", "cross_dur_up", "−20t", "+20t");

        Component facingRow = buildFacingRow(session.getCurrentCrossFacing());

        Component targetRow = Component.text("  ")
            .append(Component.text("Target:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(session.getCurrentCrossTarget(), COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[Set ▸]",
                "/scaena tech2 editparam cross_target ",
                COL_BTN,
                "Type: /scaena tech2 editparam cross_target <target>"));

        Component prevRow = buildPreviewOnlyRow("preview_cross",
            "Show destination info (executes against mark at runtime)");

        p.sendMessage(buildPanel(sep, header,
            destRow, durRow, facingRow, targetRow,
            Component.empty(), prevRow));
    }

    // -----------------------------------------------------------------------
    // PLAYER_VELOCITY panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * PLAYER_VELOCITY  effects.velocity.gentle_lift
     *
     *   X:   0.0   [−] [+]   (shift: [−0.5] [+0.5])
     *   Y:   0.4   [−] [+]   (shift: [−0.5] [+0.5])  — gentle lift
     *   Z:   0.0   [−] [+]   (shift: [−0.5] [+0.5])
     *   Audience:  [participants]  group_1  ...
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendVelocityPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("PLAYER_VELOCITY", session.cueId());

        Component xRow = buildVecAxisRow("X:", session.getCurrentVecX(),
            "vec_x_down", "vec_x_up", "vec_x_shift_down", "vec_x_shift_up", "");
        Component yLabel = EffectsEditSession.yVecLabel(session.getCurrentVecY());
        Component yRow = buildVecAxisRow("Y:", session.getCurrentVecY(),
            "vec_y_down", "vec_y_up", "vec_y_shift_down", "vec_y_shift_up", yLabel);
        Component zRow = buildVecAxisRow("Z:", session.getCurrentVecZ(),
            "vec_z_down", "vec_z_up", "vec_z_shift_down", "vec_z_shift_up", "");

        Component audRow  = buildAudienceRow(session.getCurrentVecAudience(), "vec_audience");
        Component prevRow = buildPreviewOnlyRow("preview_velocity",
            "Apply this vector impulse to yourself once");

        p.sendMessage(buildPanel(sep, header, xRow, yRow, zRow, audRow, Component.empty(), prevRow));
    }

    // -----------------------------------------------------------------------
    // PLAYER_FLIGHT panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * PLAYER_FLIGHT  effects.flight.hover
     *
     *   State:   [hover]  release
     *   Audience: [participants]  ...
     *
     *   (if state = release:)
     *   Release effect:   [slow_falling]  levitate  none
     *   Release duration: 300t (15s)   [−] [+]
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendFlightPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("PLAYER_FLIGHT", session.cueId());

        // State row — hover / release pills
        Component stateRow = Component.text("  ")
            .append(Component.text("State:", COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn(
                "hover".equals(session.getCurrentFlightState()) ? "[hover]" : "hover",
                "/scaena tech2 editparam flight_state hover",
                "hover".equals(session.getCurrentFlightState()) ? COL_BTN_ON : COL_BTN,
                "Freeze player at current altitude"))
            .append(Component.text(" "))
            .append(clickBtn(
                "release".equals(session.getCurrentFlightState()) ? "[release]" : "release",
                "/scaena tech2 editparam flight_state release",
                "release".equals(session.getCurrentFlightState()) ? COL_BTN_ON : COL_BTN,
                "Restore flight state with release effect"));

        Component audRow  = buildAudienceRow(session.getCurrentFlightAudience(), "flight_audience");

        Component body = Component.empty()
            .append(stateRow).append(Component.newline())
            .append(audRow).append(Component.newline());

        if ("release".equals(session.getCurrentFlightState())) {
            Component releaseEffRow = buildReleaseEffectRow(session.getCurrentFlightReleaseEffect());
            Component relDurRow = buildTickIncrRow("Release duration:",
                session.getCurrentFlightReleaseDurationTicks(),
                "release_dur_down", "release_dur_up", "−20t", "+20t");
            body = body
                .append(releaseEffRow).append(Component.newline())
                .append(relDurRow).append(Component.newline());
        }

        Component prevRow = buildPreviewOnlyRow("preview_flight",
            "Fire the flight state change once on yourself");

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(body)
            .append(Component.newline())
            .append(prevRow).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // EFFECT_PATTERN panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * EFFECT_PATTERN  effects.levitation.hover
     *
     *   Effect:        levitation         [Change ▸]
     *   Amplifier:     0  (subtle)   [−] [+]
     *   Pulse dur:     20t (1s) — each pulse lasts 1s   [−] [+]
     *   Cycle:         28t — fires every 1.4s (8t gap)  [−] [+]
     *   Total:         300t (15s)   [−] [+]  (shift: [−100t] [+100t])
     *   Hide particles: [ON]  OFF
     *   Audience:      [participants]  ...
     *
     *   Auto-preview: ON  [Toggle]    [▶ Preview]
     * ─────────────────────────────────────────────
     */
    private static void sendPatternPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("EFFECT_PATTERN", session.cueId());

        Component effectRow = buildFieldRow(
            "Effect:",
            session.getCurrentEffectId(),
            clickBtn("[Change ▸]",
                "/scaena tech2 editparam effect_id_panel open",
                COL_BTN,
                "Open effect selector"));

        String ampLabel = EffectsEditSession.amplifierLabel(session.getCurrentEffectAmplifier());
        Component ampDisplay = Component.text("" + session.getCurrentEffectAmplifier(), COL_VALUE)
            .append(ampLabel.isEmpty() ? Component.empty()
                : Component.text("  (" + ampLabel + ")", COL_HINT));
        Component ampRow = Component.text("  ")
            .append(Component.text("Amplifier:", COL_LABEL))
            .append(Component.text("  "))
            .append(ampDisplay)
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam amplifier_down", COL_BTN_INCR, "−1"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam amplifier_up", COL_BTN_INCR, "+1"));

        // Pulse duration row
        int pulseDur = session.getCurrentEffectDurationTicks();
        Component pulseDurRow = Component.text("  ")
            .append(Component.text("Pulse dur:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(pulseDur + "t — each pulse lasts " + ticksToSeconds(pulseDur), COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam dur_down", COL_BTN_INCR, "−4t"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam dur_up", COL_BTN_INCR, "+4t"));

        // Cycle row with cadence descriptor
        int cycle = session.getCurrentPatternCycleTicks();
        String cadenceDesc = buildCadenceDesc(pulseDur, cycle);
        TextColor cycleColor = cycle < pulseDur ? COL_WARN : COL_VALUE;
        Component cycleRow = Component.text("  ")
            .append(Component.text("Cycle:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(cycle + "t — " + cadenceDesc, cycleColor))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam cycle_down", COL_BTN_INCR, "−4t"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam cycle_up", COL_BTN_INCR, "+4t"));

        // Total duration row with shift buttons
        int total = session.getCurrentPatternTotalDuration();
        Component totalRow = Component.text("  ")
            .append(Component.text("Total:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(total + "t (" + ticksToSeconds(total) + ")", COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam total_down", COL_BTN_INCR, "−20t"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam total_up", COL_BTN_INCR, "+20t"))
            .append(Component.text("   "))
            .append(clickBtn("[−100t]", "/scaena tech2 editparam total_shift_down", COL_BTN_INCR, "−100t"))
            .append(Component.text(" "))
            .append(clickBtn("[+100t]", "/scaena tech2 editparam total_shift_up", COL_BTN_INCR, "+100t"));

        Component hideRow    = buildToggleRow("Hide particles:", session.isCurrentEffectHideParticles(), "hide_toggle");
        Component audRow     = buildAudienceRow(session.getCurrentEffectAudience(), "audience");
        Component previewRow = buildAutoPreviewRow(session.isAutoPreview(),
            "preview_pattern", "Run a 100t cadence preview on yourself");

        p.sendMessage(buildPanel(sep, header,
            effectRow, ampRow, pulseDurRow, cycleRow, totalRow,
            hideRow, audRow,
            Component.empty(), previewRow));
    }

    // -----------------------------------------------------------------------
    // EFFECT_PHRASE panel (step list)
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * EFFECT PHRASE  effects.phrase.revelation_arc
     *   Audience (default):  [participants]  group_1  ...
     *
     *   1.  [at:   0  ]  ⚗ EFFECT   levitation amp:0 — 40t   [Edit] [↓] [✕]
     *   2.  [at:  15  ]  ✦ PARTICLE  enchant ×30              [Edit] [↑] [↓] [✕]
     *   3.  [at:  30  ]  ⚗ EFFECT   blindness amp:0 — 15t     [Edit] [↑] [✕]
     *
     *   [ + Add Step ]
     *
     *   [▶ Preview Phrase]
     * ─────────────────────────────────────────────
     */
    private static void sendPhrasePanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("EFFECT PHRASE", session.cueId());

        Component audRow = buildAudienceRow(session.getCurrentPhraseAudience(), "phrase_audience");

        List<Map<String, Object>> steps = session.getCurrentPhraseSteps();
        Component body;
        if (steps.isEmpty()) {
            body = Component.text("  (no steps — click [ + Add Step ] to begin)", COL_HINT);
        } else {
            body = Component.empty();
            for (int i = 0; i < steps.size(); i++) {
                body = body.append(buildStepRow(i, steps.get(i), steps.size()));
                if (i < steps.size() - 1) body = body.append(Component.newline());
            }
        }

        Component addBtn = Component.text("  ")
            .append(clickBtn("[ + Add Step ]",
                "/scaena tech2 editparam step_add",
                COL_BTN,
                "Append a new EFFECT step"));

        Component prevRow = buildPreviewOnlyRow("preview_phrase",
            "Apply all effects/particles in sequence on yourself");

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(audRow).append(Component.newline())
            .append(Component.newline())
            .append(body).append(Component.newline())
            .append(Component.newline())
            .append(addBtn).append(Component.newline())
            .append(Component.newline())
            .append(prevRow).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // EFFECT_PHRASE step inline editor
    // -----------------------------------------------------------------------

    /**
     * Inline panel for editing a single EFFECT_PHRASE step.
     *
     * ─────────────────────────────────────────────
     * Step 2 of 3  [← Back to list]
     *   Timing:  [at]  after   0t   [−] [+]
     *   Type:    [EFFECT]  PARTICLE
     *   (EFFECT fields or PARTICLE fields)
     * ─────────────────────────────────────────────
     */
    public static void sendStepPanel(Player p, EffectsEditSession session, int stepIndex) {
        List<Map<String, Object>> steps = session.getCurrentPhraseSteps();
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            sendPanel(p, session);
            return;
        }
        Map<String, Object> step = steps.get(stepIndex);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) step.get("events");
        Map<String, Object> ev = (events != null && !events.isEmpty()) ? events.get(0) : Map.of();

        String evtType  = (String) ev.getOrDefault("type", "EFFECT");
        boolean isAfter = step.containsKey("after");
        int ticks       = isAfter ? intFromMap(step, "after", 0) : intFromMap(step, "at", 0);

        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Step " + (stepIndex + 1) + " of " + steps.size(),
                COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("    "))
            .append(clickBtn("[← Back to list]",
                "/scaena tech2 editparam step_done",
                COL_BTN,
                "Return to step list"));

        Component timingRow = Component.text("  ")
            .append(Component.text("Timing:", COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn(isAfter ? "at" : "[at]",
                "/scaena tech2 editparam step_timing_mode at",
                isAfter ? COL_BTN : COL_BTN_ON,
                "Absolute tick from cue start"))
            .append(Component.text(" "))
            .append(clickBtn(isAfter ? "[after]" : "after",
                "/scaena tech2 editparam step_timing_mode after",
                isAfter ? COL_BTN_ON : COL_BTN,
                "Relative: N ticks after previous step"))
            .append(Component.text("    "))
            .append(Component.text(ticks + "t", COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam step_ticks_down", COL_BTN_INCR, "−10t"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam step_ticks_up", COL_BTN_INCR, "+10t"));

        Component typeRow = Component.text("  ")
            .append(Component.text("Type:", COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn("EFFECT".equals(evtType) ? "[EFFECT]" : "EFFECT",
                "/scaena tech2 editparam step_type EFFECT",
                "EFFECT".equals(evtType) ? COL_BTN_ON : COL_BTN,
                "Switch to EFFECT step"))
            .append(Component.text(" "))
            .append(clickBtn("PARTICLE".equals(evtType) ? "[PARTICLE]" : "PARTICLE",
                "/scaena tech2 editparam step_type PARTICLE",
                "PARTICLE".equals(evtType) ? COL_BTN_ON : COL_BTN,
                "Switch to PARTICLE step"));

        Component fieldsBlock = buildStepFields(evtType, ev);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(timingRow).append(Component.newline())
            .append(typeRow).append(Component.newline())
            .append(fieldsBlock)
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Effect ID sub-panel
    // -----------------------------------------------------------------------

    /** Send the curated effect ID selector, organised by KB category. */
    public static void sendEffectIdPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Select effect ID:", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  (active: " + session.getCurrentEffectId() + ")", COL_HINT));

        Component body = Component.empty();
        for (int c = 0; c < EffectsEditSession.EFFECT_CATEGORY_LABELS.length; c++) {
            body = body.append(
                Component.text("  " + EffectsEditSession.EFFECT_CATEGORY_LABELS[c],
                    COL_CAT, TextDecoration.BOLD)
            ).append(Component.newline());

            Component row = Component.text("    ");
            int count = 0;
            for (String id : EffectsEditSession.EFFECT_CATEGORIES.get(c)) {
                boolean active = id.equalsIgnoreCase(session.getCurrentEffectId());
                row = row.append(
                    clickBtn(id,
                        "/scaena tech2 editparam effect_id " + id,
                        active ? COL_BTN_ON : COL_PILL,
                        "Set effect_id to " + id)
                ).append(Component.text("  "));
                count++;
                if (count % 3 == 0) {
                    body = body.append(row).append(Component.newline());
                    row = Component.text("    ");
                }
            }
            if (count % 3 != 0) body = body.append(row).append(Component.newline());
            body = body.append(Component.newline());
        }

        Component hint = Component.text(
            "  Or: /scaena tech2 editparam effect_id <custom_id>", COL_HINT);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(body)
            .append(hint).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Particle ID sub-panel
    // -----------------------------------------------------------------------

    /** Send the curated particle ID selector, organised by KB atmospheric category. */
    public static void sendParticleIdPanel(Player p, EffectsEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Select particle ID:", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  (active: " + stripMcPrefix(session.getCurrentParticleId()) + ")",
                COL_HINT));

        Component body = Component.empty();
        for (int c = 0; c < EffectsEditSession.PARTICLE_CATEGORY_LABELS.length; c++) {
            body = body.append(
                Component.text("  " + EffectsEditSession.PARTICLE_CATEGORY_LABELS[c],
                    COL_CAT, TextDecoration.BOLD)
            ).append(Component.newline());

            Component row = Component.text("    ");
            int count = 0;
            for (String id : EffectsEditSession.PARTICLE_CATEGORIES.get(c)) {
                boolean active = id.equalsIgnoreCase(session.getCurrentParticleId());
                String label   = stripMcPrefix(id);
                row = row.append(
                    clickBtn(label,
                        "/scaena tech2 editparam particle_id " + id,
                        active ? COL_BTN_ON : COL_PILL,
                        id)
                ).append(Component.text("  "));
                count++;
                if (count % 3 == 0) {
                    body = body.append(row).append(Component.newline());
                    row = Component.text("    ");
                }
            }
            if (count % 3 != 0) body = body.append(row).append(Component.newline());
            body = body.append(Component.newline());
        }

        Component hint = Component.text(
            "  Or: /scaena tech2 editparam particle_id minecraft:<custom_id>", COL_HINT);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(body)
            .append(hint).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Step row builder (phrase list view)
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Component buildStepRow(int i, Map<String, Object> step, int totalSteps) {
        boolean isAfter  = step.containsKey("after");
        int ticks        = isAfter ? intFromMap(step, "after", 0) : intFromMap(step, "at", 0);
        String timingKey = isAfter ? "after:" : "at:   ";
        String timingVal = String.format("%-3d", ticks);

        List<Map<String, Object>> evs = (List<Map<String, Object>>) step.get("events");
        String evtType    = "?";
        String descriptor = "(empty)";
        if (evs != null && !evs.isEmpty()) {
            Map<String, Object> ev = evs.get(0);
            evtType = (String) ev.getOrDefault("type", "?");
            if ("EFFECT".equals(evtType)) {
                String eid = (String) ev.getOrDefault("effect_id", "?");
                int amp    = intFromMap(ev, "amplifier", 0);
                int dur    = intFromMap(ev, "duration_ticks", 0);
                descriptor = eid + " amp:" + amp + " — " + dur + "t";
            } else if ("PARTICLE".equals(evtType)) {
                String pid = stripMcPrefix((String) ev.getOrDefault("particle_id", "?"));
                int cnt    = intFromMap(ev, "count", 0);
                descriptor = pid + " ×" + cnt;
            }
        }

        boolean isEffect  = "EFFECT".equals(evtType);
        TextColor typeCol = isEffect ? COL_TYPE_EFF : COL_TYPE_PAR;
        String typeIcon   = isEffect ? "⚗" : "✦";

        Component row = Component.text("  ")
            .append(Component.text((i + 1) + ". ", COL_STEP_NUM))
            .append(Component.text("[" + timingKey + " " + timingVal + "]", COL_TIMING))
            .append(Component.text("  "))
            .append(Component.text(typeIcon + " " + String.format("%-8s", evtType), typeCol))
            .append(Component.text("  "))
            .append(Component.text(descriptor, COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[Edit]",
                "/scaena tech2 editparam step_edit " + i,
                COL_BTN, "Edit this step"));

        if (i > 0) {
            row = row.append(Component.text(" "))
                .append(clickBtn("[↑]",
                    "/scaena tech2 editparam step_up " + i,
                    COL_BTN_INCR, "Move up"));
        }
        if (i < totalSteps - 1) {
            row = row.append(Component.text(" "))
                .append(clickBtn("[↓]",
                    "/scaena tech2 editparam step_down " + i,
                    COL_BTN_INCR, "Move down"));
        }
        row = row.append(Component.text(" "))
            .append(clickBtn("[✕]",
                "/scaena tech2 editparam step_delete " + i,
                COL_WARN, "Delete this step"));

        return row;
    }

    // -----------------------------------------------------------------------
    // Step-specific field rows (for step inline editor)
    // -----------------------------------------------------------------------

    private static Component buildStepFields(String evtType, Map<String, Object> ev) {
        Component body = Component.empty();
        if ("EFFECT".equals(evtType)) {
            String eid = (String) ev.getOrDefault("effect_id", "levitation");
            int amp    = intFromMap(ev, "amplifier", 0);
            int dur    = intFromMap(ev, "duration_ticks", 40);
            boolean hide = (Boolean) ev.getOrDefault("hide_particles", true);
            String ampLabel = EffectsEditSession.amplifierLabel(amp);
            body = body
                .append(buildFieldRow("Effect:", eid,
                    clickBtn("[Set ▸]",
                        "/scaena tech2 editparam step_effect_id ",
                        COL_BTN,
                        "Type: /scaena tech2 editparam step_effect_id <id>")))
                .append(Component.newline())
                .append(Component.text("  ")
                    .append(Component.text("Amplifier:", COL_LABEL))
                    .append(Component.text("  "))
                    .append(Component.text("" + amp + (ampLabel.isEmpty() ? "" : "  (" + ampLabel + ")"), COL_VALUE))
                    .append(Component.text("    "))
                    .append(clickBtn("[−]", "/scaena tech2 editparam step_amplifier_down", COL_BTN_INCR, "−1"))
                    .append(Component.text(" "))
                    .append(clickBtn("[+]", "/scaena tech2 editparam step_amplifier_up", COL_BTN_INCR, "+1")))
                .append(Component.newline())
                .append(buildIncrRow("Duration:", dur + "t  (" + ticksToSeconds(dur) + ")",
                    "/scaena tech2 editparam step_dur_down",
                    "/scaena tech2 editparam step_dur_up", "−10t", "+10t"))
                .append(Component.newline())
                .append(buildInlineToggleRow("Hide particles:", hide, "step_hide_toggle"))
                .append(Component.newline());
        } else if ("PARTICLE".equals(evtType)) {
            String pid  = (String) ev.getOrDefault("particle_id", "minecraft:end_rod");
            int cnt     = intFromMap(ev, "count", 10);
            double extra = dblFromMap(ev, "extra", 0.0);
            boolean force = (Boolean) ev.getOrDefault("force", true);
            body = body
                .append(buildFieldRow("Particle:", stripMcPrefix(pid),
                    clickBtn("[Set ▸]",
                        "/scaena tech2 editparam step_particle_id ",
                        COL_BTN,
                        "Type: /scaena tech2 editparam step_particle_id minecraft:<id>")))
                .append(Component.newline())
                .append(Component.text("  ")
                    .append(Component.text("Count:", COL_LABEL))
                    .append(Component.text("  "))
                    .append(Component.text("" + cnt, COL_VALUE))
                    .append(Component.text("    "))
                    .append(clickBtn("[−]", "/scaena tech2 editparam step_count_down", COL_BTN_INCR, "−1"))
                    .append(Component.text(" "))
                    .append(clickBtn("[+]", "/scaena tech2 editparam step_count_up", COL_BTN_INCR, "+1")))
                .append(Component.newline())
                .append(buildIncrRow("Extra:", String.format("%.2f", extra),
                    "/scaena tech2 editparam step_extra_down",
                    "/scaena tech2 editparam step_extra_up", "−0.01", "+0.01"))
                .append(Component.newline())
                .append(buildInlineToggleRow("Force:", force, "step_force_toggle"))
                .append(Component.newline());
        } else {
            body = Component.text("  (unknown step type: " + evtType + ")", COL_HINT).append(Component.newline());
        }
        return body;
    }

    // -----------------------------------------------------------------------
    // Row builders
    // -----------------------------------------------------------------------

    private static Component deptHeader(String modeName, String cueId) {
        return Component.text(modeName, COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  "))
            .append(Component.text(cueId, COL_HEADER));
    }

    private static Component buildAudienceRow(String current, String paramKey) {
        Component row = Component.text("  ")
            .append(Component.text("Audience:", COL_LABEL))
            .append(Component.text("  "));
        for (String aud : EffectsEditSession.AUDIENCES) {
            boolean active = aud.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + aud + "]" : aud,
                    "/scaena tech2 editparam " + paramKey + " " + aud,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set audience to " + aud)
            ).append(Component.text(" "));
        }
        return row;
    }

    private static Component buildFacingRow(String currentFacing) {
        Component row = Component.text("  ")
            .append(Component.text("Facing:", COL_LABEL))
            .append(Component.text("  "));
        for (String f : EffectsEditSession.COMPASS_FACINGS) {
            boolean active = f.equalsIgnoreCase(currentFacing);
            row = row.append(
                clickBtn(active ? "[" + f + "]" : f,
                    "/scaena tech2 editparam cross_facing " + f,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set facing to " + f)
            ).append(Component.text(" "));
        }
        // "none" option to clear
        boolean noneActive = currentFacing == null;
        row = row.append(
            clickBtn(noneActive ? "[none]" : "none",
                "/scaena tech2 editparam cross_facing none",
                noneActive ? COL_BTN_ON : COL_BTN_INCR,
                "No facing change")
        );
        return row;
    }

    private static Component buildReleaseEffectRow(String current) {
        Component row = Component.text("  ")
            .append(Component.text("Release effect:", COL_LABEL))
            .append(Component.text("  "));
        for (String eff : EffectsEditSession.RELEASE_EFFECTS) {
            boolean active = eff.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + eff + "]" : eff,
                    "/scaena tech2 editparam release_effect " + eff,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set release_effect to " + eff)
            ).append(Component.text(" "));
        }
        return row;
    }

    private static Component buildFieldRow(String label, String value, Component actionBtn) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(value, COL_VALUE))
            .append(Component.text("    "))
            .append(actionBtn);
    }

    private static Component buildTickIncrRow(String label, int ticks,
                                              String cmdDown, String cmdUp,
                                              String tipDown, String tipUp) {
        String display = ticks + "t  (" + ticksToSeconds(ticks) + ")";
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(display, COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + cmdDown, COL_BTN_INCR, tipDown))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + cmdUp, COL_BTN_INCR, tipUp));
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

    private static Component buildToggleRow(String label, boolean on, String paramKey) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn(on ? "[ON]" : "ON",
                "/scaena tech2 editparam " + paramKey,
                on ? COL_BTN_ON : COL_BTN,
                "Toggle to " + (on ? "OFF" : "ON")))
            .append(Component.text(" "))
            .append(clickBtn(on ? "OFF" : "[OFF]",
                "/scaena tech2 editparam " + paramKey,
                on ? COL_BTN_INCR : COL_BTN_ON,
                "Toggle to " + (on ? "OFF" : "ON")));
    }

    private static Component buildInlineToggleRow(String label, boolean on, String paramKey) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn(on ? "[ON]" : "ON",
                "/scaena tech2 editparam " + paramKey,
                on ? COL_BTN_ON : COL_BTN,
                "Toggle"))
            .append(Component.text(" "))
            .append(clickBtn(on ? "OFF" : "[OFF]",
                "/scaena tech2 editparam " + paramKey,
                on ? COL_BTN_INCR : COL_BTN_ON,
                "Toggle"));
    }

    private static Component buildAutoPreviewRow(boolean autoOn, String previewCmd, String previewTooltip) {
        String statusLabel = autoOn ? "ON" : "OFF";
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

    private static Component buildPreviewOnlyRow(String previewCmd, String tooltip) {
        return Component.text("  ")
            .append(clickBtn("[▶ Preview]",
                "/scaena tech2 editparam " + previewCmd,
                COL_BTN,
                tooltip));
    }

    private static Component buildOffsetRow(double ox, double oy, double oz,
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
            .append(Component.text("   y "))
            .append(Component.text(formatDouble(oy), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + yDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + yUp, COL_BTN_INCR, "+0.5"))
            .append(Component.text("   z "))
            .append(Component.text(formatDouble(oz), COL_VALUE))
            .append(Component.text("  "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + zDn, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + zUp, COL_BTN_INCR, "+0.5"));
    }

    private static Component buildVecAxisRow(String label, double val,
                                             String dnCmd, String upCmd,
                                             String dnShiftCmd, String upShiftCmd,
                                             String descriptor) {
        Component desc = descriptor.isEmpty() ? Component.empty()
            : Component.text("  — " + descriptor, COL_HINT);
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(formatDouble(val), COL_VALUE))
            .append(desc)
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam " + dnCmd, COL_BTN_INCR, "−0.1"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam " + upCmd, COL_BTN_INCR, "+0.1"))
            .append(Component.text("   "))
            .append(clickBtn("[−0.5]", "/scaena tech2 editparam " + dnShiftCmd, COL_BTN_INCR, "−0.5"))
            .append(Component.text(" "))
            .append(clickBtn("[+0.5]", "/scaena tech2 editparam " + upShiftCmd, COL_BTN_INCR, "+0.5"));
    }

    private static Component buildParticleDurRow(EffectsEditSession session) {
        int dur = session.getCurrentParticleDurationTicks();
        Component row = Component.text("  ")
            .append(Component.text("Duration:", COL_LABEL))
            .append(Component.text("  "));
        if (dur > 0) {
            row = row
                .append(Component.text(dur + "t  (" + ticksToSeconds(dur) + ")", COL_VALUE))
                .append(Component.text("    "))
                .append(clickBtn("[−]", "/scaena tech2 editparam particle_dur_down", COL_BTN_INCR, "−20t"))
                .append(Component.text(" "))
                .append(clickBtn("[+]", "/scaena tech2 editparam particle_dur_up", COL_BTN_INCR, "+20t"))
                .append(Component.text("   "))
                .append(clickBtn("[−100t]", "/scaena tech2 editparam particle_dur_shift_down", COL_BTN_INCR, "−100t"))
                .append(Component.text(" "))
                .append(clickBtn("[+100t]", "/scaena tech2 editparam particle_dur_shift_up", COL_BTN_INCR, "+100t"))
                .append(Component.text("   "))
                .append(clickBtn("[Clear]", "/scaena tech2 editparam particle_dur_clear", COL_BTN, "Remove duration → single burst"));
        } else {
            row = row
                .append(Component.text("(single burst)", COL_HINT))
                .append(Component.text("    "))
                .append(clickBtn("[Set]", "/scaena tech2 editparam particle_dur_up", COL_BTN,
                    "Add duration_ticks (enables repeating atmospheric mode)"));
        }
        return row;
    }

    private static Component buildParticleIntervalRow(EffectsEditSession session) {
        if (session.getCurrentParticleDurationTicks() <= 0) return Component.empty();
        int interval = session.getCurrentParticleIntervalTicks();
        int duration = session.getCurrentParticleDurationTicks();
        int bursts   = (interval > 0) ? (duration / interval) : 0;
        String desc  = "every " + interval + "t — " + bursts + " bursts over " + duration + "t";
        return Component.text("  ")
            .append(Component.text("Interval:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(interval + "t  (" + desc + ")", COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam interval_down", COL_BTN_INCR, "−2t"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam interval_up", COL_BTN_INCR, "+2t"));
    }

    /** Build and assemble a standard panel (separator + header + rows). */
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

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    private static Component clickBtn(String label, String command,
                                      TextColor color, String tooltip) {
        return Component.text(label, color, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, NamedTextColor.GRAY)));
    }

    /** Format a double for compact display: show 1 decimal if clean, 2 otherwise. */
    private static String formatDouble(double v) {
        if (v == Math.floor(v) && Math.abs(v) < 1e6) return String.format("%.1f", v);
        return String.format("%.2f", v);
    }

    private static String ticksToSeconds(int ticks) {
        float s = ticks / 20.0f;
        if (s == (int) s) return (int) s + "s";
        return String.format("%.2f", s).replaceAll("0+$", "").replaceAll("\\.$", "") + "s";
    }

    private static String stripMcPrefix(String id) {
        if (id == null) return "(none)";
        return id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
    }

    /**
     * Build the cycle cadence descriptor for EFFECT_PATTERN.
     * Examples: "fires every 1.4s (8t gap)" / "fires every 1.2s (continuous)"
     *           / "⚠ cycle < duration — overlapping pulses"
     */
    private static String buildCadenceDesc(int pulseDur, int cycle) {
        if (cycle <= 0) return "cycle must be > 0";
        if (cycle < pulseDur) return "⚠ cycle < duration — overlapping pulses";
        String freq = ticksToSeconds(cycle);
        if (cycle == pulseDur) return "fires every " + freq + " (continuous)";
        int gap = cycle - pulseDur;
        return "fires every " + freq + " (" + gap + "t gap)";
    }

    private static int intFromMap(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }

    private static double dblFromMap(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.doubleValue() : def;
    }
}
