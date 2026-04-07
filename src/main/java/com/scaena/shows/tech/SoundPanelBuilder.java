package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Builds and sends the Phase 2 Sound department edit panels.
 *
 * Two top-level views:
 *   SOUND panel      — sound_id, category, volume, pitch, max_duration_ticks + auto-preview toggle
 *   STOP_SOUND panel — source channel selector only
 *
 * Sub-panel:
 *   Sound ID selector — curated list organized by Sound KB registers (Atmosphere, Presence,
 *   Warmth, Transition, Shadow, Tension) + hint to enter a custom ID via command.
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Spec: kb/system/phase2-department-panels.md §Sound
 */
public final class SoundPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER    = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT      = TextColor.color(0x55FFFF); // aqua
    private static final TextColor COL_DEPT_STOP = TextColor.color(0xFF9944); // warm orange for STOP_SOUND
    private static final TextColor COL_LABEL     = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE     = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN       = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON    = TextColor.color(0x55FF55); // green (active)
    private static final TextColor COL_BTN_INCR  = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_SEP       = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT      = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_REGISTER  = TextColor.color(0xAA88FF); // lavender (register headings)
    private static final TextColor COL_SOUND_BTN = TextColor.color(0x88FFCC); // teal (sound ID pills)
    private static final TextColor COL_PITCH_REG = TextColor.color(0xFFDD88); // amber (pitch register label)

    private static final String SEP = "─────────────────────────────────────────────";

    private SoundPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /**
     * Send the appropriate main panel for the active session's event mode.
     */
    public static void sendPanel(Player p, SoundEditSession session) {
        if (session.getMode() == SoundEditSession.EventMode.STOP_SOUND) {
            sendStopSoundPanel(p, session);
        } else {
            sendSoundPanel(p, session);
        }
    }

    // -----------------------------------------------------------------------
    // SOUND panel
    // -----------------------------------------------------------------------

    /**
     * Build and send the SOUND edit panel.
     *
     * ─────────────────────────────────────────────
     * SOUND  sound.hostile.low.warden_presence
     *
     *   Sound ID:   entity.warden.heartbeat    [Change ▸]
     *   Category:   [ambient] [hostile] [music] ...
     *   Volume:     0.8    [−] [+]
     *   Pitch:      0.60  (low)    [−] [+]
     *   Max dur:    (none)    [Set]
     *   Auto-preview: ON    [Toggle]    [▶ Hear it]
     * ─────────────────────────────────────────────
     */
    private static void sendSoundPanel(Player p, SoundEditSession session) {
        Component sep = Component.text(SEP, COL_SEP);

        // Header
        Component header = Component.text("SOUND", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  "))
            .append(Component.text(session.cueId(), COL_HEADER));

        // sound_id row — abbreviated display, full ID in hover tooltip
        String soundShort = stripMcPrefix(session.getCurrentSoundId());
        Component soundRow = buildFieldRow(
            "Sound ID:",
            soundShort,
            clickBtn("[Change ▸]",
                "/scaena tech2 editparam sound_id_panel open",
                COL_BTN,
                "Full ID: " + session.getCurrentSoundId() + "\nClick to open selector")
        );

        // category row — inline pills, active one highlighted
        Component catRow = buildCategoryRow(session.getCurrentCategory());

        // volume row — current value + [−] [+]
        String volDisplay = String.format("%.1f", session.getCurrentVolume());
        Component volumeRow = buildIncrRow(
            "Volume:",
            volDisplay,
            "/scaena tech2 editparam volume_down",
            "/scaena tech2 editparam volume_up",
            "−0.1",
            "+0.1"
        );

        // pitch row — value + register label in parentheses + [−] [+]
        float pitch = session.getCurrentPitch();
        String pitchDisplay = formatPitch(pitch);
        String regLabel     = SoundEditSession.pitchRegisterLabel(pitch);
        Component pitchValueComp = Component.text(pitchDisplay, COL_VALUE)
            .append(Component.text("  "))
            .append(Component.text("(" + regLabel + ")", COL_PITCH_REG));

        Component pitchRow = Component.text("  ")
            .append(Component.text("Pitch:", COL_LABEL))
            .append(Component.text("  "))
            .append(pitchValueComp)
            .append(Component.text("    "))
            .append(clickBtn("[−]",
                "/scaena tech2 editparam pitch_down",
                COL_BTN_INCR, "−0.05"))
            .append(Component.text(" "))
            .append(clickBtn("[+]",
                "/scaena tech2 editparam pitch_up",
                COL_BTN_INCR, "+0.05"));

        // max_duration_ticks row
        Component durRow = buildMaxDurRow(session.getCurrentMaxDurationTicks());

        // auto-preview row
        Component previewRow = buildPreviewRow(session.isAutoPreview());

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(soundRow).append(Component.newline())
            .append(catRow).append(Component.newline())
            .append(volumeRow).append(Component.newline())
            .append(pitchRow).append(Component.newline())
            .append(durRow).append(Component.newline())
            .append(previewRow).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // STOP_SOUND panel
    // -----------------------------------------------------------------------

    /**
     * Build and send the STOP_SOUND edit panel.
     *
     * ─────────────────────────────────────────────
     * STOP_SOUND  sound.transition.clear
     *
     *   Source:
     *   [ambient] [hostile] [music] ... [all]
     * ─────────────────────────────────────────────
     */
    private static void sendStopSoundPanel(Player p, SoundEditSession session) {
        Component sep = Component.text(SEP, COL_SEP);

        Component header = Component.text("STOP_SOUND", COL_DEPT_STOP, TextDecoration.BOLD)
            .append(Component.text("  "))
            .append(Component.text(session.cueId(), COL_HEADER));

        Component sourceLabel = Component.text("  Source:", COL_LABEL);

        Component sourcePills = buildSourcePills(session.getCurrentSource());

        Component note = Component.text(
            "  Note: STOP_SOUND cuts all sounds on the selected channel. No preset.", COL_HINT);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(sourceLabel).append(Component.newline())
            .append(sourcePills).append(Component.newline())
            .append(Component.newline())
            .append(note).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Sound ID sub-panel
    // -----------------------------------------------------------------------

    /**
     * Send the sound ID selector sub-panel, organized by Sound KB curated registers.
     * Clicking any entry immediately fires /scaena tech2 editparam sound_id <id>.
     */
    public static void sendSoundIdPanel(Player p, SoundEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Select sound ID:", COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("  "))
            .append(Component.text("(active: " + stripMcPrefix(session.getCurrentSoundId()) + ")",
                COL_HINT));

        Component body = Component.empty();

        for (int r = 0; r < SoundEditSession.REGISTER_LABELS.length; r++) {
            String registerLabel = SoundEditSession.REGISTER_LABELS[r];
            List<String> sounds  = SoundEditSession.REGISTER_SOUNDS.get(r);

            // Register heading
            body = body.append(
                Component.text("  " + registerLabel, COL_REGISTER, TextDecoration.BOLD)
            ).append(Component.newline());

            // Sound pills — 3 per row for readability
            int count = 0;
            Component row = Component.text("    ");
            for (String id : sounds) {
                String shortLabel = stripMcPrefix(id);
                boolean active = id.equalsIgnoreCase(session.getCurrentSoundId());
                TextColor color = active ? COL_BTN_ON : COL_SOUND_BTN;

                Component pill = clickBtn(shortLabel,
                    "/scaena tech2 editparam sound_id " + id,
                    color,
                    id);
                row = row.append(pill).append(Component.text("  "));
                count++;

                if (count % 3 == 0) {
                    body = body.append(row).append(Component.newline());
                    row  = Component.text("    ");
                }
            }
            // Flush remaining partial row
            if (count % 3 != 0) {
                body = body.append(row).append(Component.newline());
            }
            body = body.append(Component.newline());
        }

        Component customHint = Component.text(
            "  Or: /scaena tech2 editparam sound_id <custom_id>", COL_HINT);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(body)
            .append(customHint).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Row builders
    // -----------------------------------------------------------------------

    private static Component buildFieldRow(String label, String value, Component actionBtn) {
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(value, COL_VALUE))
            .append(Component.text("    "))
            .append(actionBtn);
    }

    /**
     * Build a "label: value [−] [+]" row for numeric params.
     */
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
            .append(clickBtn("[+]", cmdUp,   COL_BTN_INCR, tipUp));
    }

    /**
     * Build the category row: "Category:  [ambient] [hostile] ..."
     * The active category is green; others are orange.
     */
    private static Component buildCategoryRow(String currentCat) {
        Component row = Component.text("  ")
            .append(Component.text("Category:", COL_LABEL))
            .append(Component.text("  "));

        for (String cat : SoundEditSession.CATEGORIES) {
            boolean active = cat.equalsIgnoreCase(currentCat);
            TextColor color = active ? COL_BTN_ON : COL_BTN;
            String label    = active ? "[" + cat + "]" : cat;
            row = row.append(
                clickBtn(label,
                    "/scaena tech2 editparam category " + cat,
                    color,
                    "Set category to " + cat)
            ).append(Component.text(" "));
        }
        return row;
    }

    /**
     * Build the max_duration_ticks row.
     * When unset: shows "(none) [Set]"
     * When set:   shows "Nt (Xs) [−] [+] [Clear]"
     */
    private static Component buildMaxDurRow(int maxDur) {
        Component row = Component.text("  ")
            .append(Component.text("Max dur:", COL_LABEL))
            .append(Component.text("  "));

        if (maxDur > 0) {
            String durDisplay = maxDur + "t  (" + ticksToSeconds(maxDur) + ")";
            row = row
                .append(Component.text(durDisplay, COL_VALUE))
                .append(Component.text("    "))
                .append(clickBtn("[−]",
                    "/scaena tech2 editparam max_dur_down",
                    COL_BTN_INCR, "−5 ticks"))
                .append(Component.text(" "))
                .append(clickBtn("[+]",
                    "/scaena tech2 editparam max_dur_up",
                    COL_BTN_INCR, "+5 ticks"))
                .append(Component.text(" "))
                .append(clickBtn("[Clear]",
                    "/scaena tech2 editparam max_dur_clear",
                    COL_BTN, "Remove max_duration_ticks"));
        } else {
            row = row
                .append(Component.text("(none)", COL_HINT))
                .append(Component.text("    "))
                .append(clickBtn("[Set]",
                    "/scaena tech2 editparam max_dur_up",
                    COL_BTN, "Set max_duration_ticks (starts at 20t / 1s)"));
        }
        return row;
    }

    /**
     * Build the auto-preview row.
     * "Auto-preview: ON [Toggle]    [▶ Hear it]"
     */
    private static Component buildPreviewRow(boolean autoOn) {
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
                    ? "Turn OFF auto-preview (manual [▶ Hear it] only)"
                    : "Turn ON auto-preview (refires on every param change)"))
            .append(Component.text("    "))
            .append(clickBtn("[▶ Hear it]",
                "/scaena tech2 editparam preview_sound",
                COL_BTN,
                "Fire the current sound once at your location"));
    }

    /**
     * Build the STOP_SOUND source pill row.
     * "  [ambient] hostile music ... [all]"
     * Active source is green, others orange.
     */
    private static Component buildSourcePills(String currentSource) {
        Component row = Component.text("  ");
        for (String src : SoundEditSession.STOP_SOURCES) {
            boolean active = src.equalsIgnoreCase(currentSource);
            TextColor color = active ? COL_BTN_ON : COL_BTN;
            String label    = active ? "[" + src + "]" : src;
            row = row.append(
                clickBtn(label,
                    "/scaena tech2 editparam source " + src,
                    color,
                    "Set source to " + src)
            ).append(Component.text(" "));
        }
        return row;
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    /**
     * Strip "minecraft:" prefix for compact display.
     * e.g. "minecraft:entity.warden.heartbeat" → "entity.warden.heartbeat"
     */
    private static String stripMcPrefix(String id) {
        if (id == null) return "(none)";
        return id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
    }

    /**
     * Format a pitch value for display.
     * Shows 2 decimal places, but drops trailing zero: 1.00 → 1.0, 0.55 → 0.55.
     */
    private static String formatPitch(float pitch) {
        // Always show at least 1 decimal place
        String s = String.format("%.2f", pitch);
        // Strip trailing zero after decimal if it's X.X0
        if (s.endsWith("0") && s.indexOf('.') == s.length() - 3) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** Convert ticks to a human-readable seconds string: "40t (2s)", "25t (1.25s)". */
    private static String ticksToSeconds(int ticks) {
        float s = ticks / 20.0f;
        if (s == (int) s) return (int) s + "s";
        return String.format("%.2f", s).replaceAll("0+$", "").replaceAll("\\.$", "") + "s";
    }

    private static Component clickBtn(String label, String command,
                                       TextColor color, String tooltip) {
        return Component.text(label, color, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, NamedTextColor.GRAY)));
    }
}
