package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Builds and sends the Phase 2 Choreography department edit panels.
 *
 * Seven top-level views dispatched by event mode:
 *   ENTRANCE          — Appear (SPAWN_ENTITY at mark) / Arrive (ENTER from wing)
 *   CHARACTER_EXIT    — Vanish (DESPAWN_ENTITY) / Exit to Wing (EXIT)
 *   CHARACTER_CROSS   — Instant / AI mode; entity + dest + speed preset + anchor
 *   CHARACTER_LOOK    — FACE entity target; look_at picker; pitch warning
 *   PERFORMER_STATE   — ENTITY_AI toggle (Puppet / Performer)
 *   CHARACTER_VELOCITY — ENTITY_VELOCITY; vector x/y/z; named presets; anchor
 *   CHOREO_PHRASE     — stub: read-only label
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Spec: kb/system/phase2-department-panels.md §Choreography
 */
public final class ChoreographyPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0xAAFFCC); // mint (Choreography)
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (active/ON)
    private static final TextColor COL_BTN_OFF  = TextColor.color(0xFF5555); // red (OFF state)
    private static final TextColor COL_BTN_INCR = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_WARN     = TextColor.color(0xFFAA00); // amber (warnings)

    private static final String SEP = "─────────────────────────────────────────────";

    private ChoreographyPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /** Send the appropriate panel for the session's current event mode. */
    public static void sendPanel(Player p, ChoreographyEditSession session) {
        switch (session.getMode()) {
            case ENTRANCE           -> sendEntrancePanel(p, session);
            case CHARACTER_EXIT     -> sendCharacterExitPanel(p, session);
            case CHARACTER_CROSS    -> sendCharacterCrossPanel(p, session);
            case CHARACTER_LOOK     -> sendCharacterLookPanel(p, session);
            case PERFORMER_STATE    -> sendPerformerStatePanel(p, session);
            case CHARACTER_VELOCITY -> sendCharacterVelocityPanel(p, session);
            case CHOREO_PHRASE      -> sendChoreoPhrasePanel(p, session);
        }
    }

    // -----------------------------------------------------------------------
    // ENTRANCE panel
    // -----------------------------------------------------------------------

    private static void sendEntrancePanel(Player p, ChoreographyEditSession s) {
        boolean appear = "appear".equals(s.getCurrentEntranceSubMode());

        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("ENTRANCE", appear ? "Appear (spawn at mark)" : "Arrive (enter from wing)"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Sub-mode toggle
        p.sendMessage(
            label("Mode: ")
            .append(subModeBtn("Appear", appear, "sub_mode appear"))
            .append(Component.text("  "))
            .append(subModeBtn("Arrive", !appear, "sub_mode arrive"))
        );
        p.sendMessage(Component.empty());

        if (appear) {
            // Appear (SPAWN_ENTITY)
            p.sendMessage(label("Entity type: ").append(valueText(s.getCurrentAppearEntityType())));
            p.sendMessage(buildPickerRow("entity_type", ChoreographyEditSession.ENTITY_TYPES, s.getCurrentAppearEntityType()));

            p.sendMessage(Component.empty());
            p.sendMessage(label("Spawn mark: ").append(valueText(s.getCurrentAppearMark())));
            p.sendMessage(buildPickerRow("appear_mark", ChoreographyEditSession.STAGE_MARKS, s.getCurrentAppearMark()));

            p.sendMessage(Component.empty());
            p.sendMessage(
                label("Name: ").append(valueText(s.getCurrentAppearName().isEmpty() ? "(none)" : s.getCurrentAppearName()))
                .append(Component.text("  "))
                .append(btn("[Set Name]", "appear_name "))
            );
            p.sendMessage(
                label("Baby: ").append(boolBtn(s.isCurrentAppearBaby(), "appear_baby_toggle"))
            );

        } else {
            // Arrive (ENTER)
            p.sendMessage(label("Entity type: ").append(valueText(s.getCurrentArriveEntityType())));
            p.sendMessage(buildPickerRow("arrive_entity_type", ChoreographyEditSession.ENTITY_TYPES, s.getCurrentArriveEntityType()));

            p.sendMessage(Component.empty());
            p.sendMessage(label("From (wing): ").append(valueText(s.getCurrentArriveFrom())));
            p.sendMessage(buildPickerRow("arrive_from", ChoreographyEditSession.WING_MARKS, s.getCurrentArriveFrom()));

            p.sendMessage(Component.empty());
            p.sendMessage(label("Destination: ").append(valueText(s.getCurrentArriveDest())));
            p.sendMessage(buildPickerRow("arrive_dest", ChoreographyEditSession.STAGE_MARKS, s.getCurrentArriveDest()));

            p.sendMessage(Component.empty());
            p.sendMessage(label("Speed: ").append(valueText(s.getCurrentArriveSpeedPreset())));
            p.sendMessage(buildPickerRow("arrive_speed", ChoreographyEditSession.SPEED_PRESETS, s.getCurrentArriveSpeedPreset()));

            p.sendMessage(Component.empty());
            p.sendMessage(
                label("Name: ").append(valueText(s.getCurrentArriveName().isEmpty() ? "(none)" : s.getCurrentArriveName()))
                .append(Component.text("  "))
                .append(btn("[Set Name]", "arrive_name "))
            );

            p.sendMessage(Component.empty());
            p.sendMessage(hint("Note: ENTER does not apply equipment. Use SPAWN_ENTITY + CROSS_TO if the performer needs gear on arrival."));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CHARACTER EXIT panel
    // -----------------------------------------------------------------------

    private static void sendCharacterExitPanel(Player p, ChoreographyEditSession s) {
        boolean vanish = "vanish".equals(s.getCurrentExitSubMode());

        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CHARACTER EXIT", vanish ? "Vanish (instant despawn)" : "Exit to Wing (path + despawn)"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Sub-mode toggle
        p.sendMessage(
            label("Mode: ")
            .append(subModeBtn("Vanish", vanish, "exit_sub_mode vanish"))
            .append(Component.text("  "))
            .append(subModeBtn("Exit to Wing", !vanish, "exit_sub_mode exit_wing"))
        );
        p.sendMessage(Component.empty());

        // Target
        p.sendMessage(
            label("Target: ").append(valueText(s.getCurrentExitTarget().isEmpty() ? "(none)" : s.getCurrentExitTarget()))
            .append(Component.text("  "))
            .append(btn("[Set Target]", "exit_target "))
        );
        p.sendMessage(Component.empty());

        if (vanish) {
            // Vanish: particle toggle
            p.sendMessage(
                label("Particle burst: ")
                .append(boolBtn(s.isCurrentExitParticleBurst(), "particle_toggle"))
            );
        } else {
            // Exit to Wing: destination mark
            p.sendMessage(label("To (wing): ").append(valueText(s.getCurrentExitToMark())));
            p.sendMessage(buildPickerRow("exit_to_mark", ChoreographyEditSession.WING_MARKS, s.getCurrentExitToMark()));
            p.sendMessage(Component.empty());
            p.sendMessage(hint("Performer pathfinds to wing mark, then despawns on arrival."));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CHARACTER CROSS panel
    // -----------------------------------------------------------------------

    private static void sendCharacterCrossPanel(Player p, ChoreographyEditSession s) {
        boolean ai = "ai".equals(s.getCurrentCrossMode());

        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CHARACTER CROSS", ai ? "AI Walk (pathfinder)" : "Instant (teleport)"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Mode toggle
        p.sendMessage(
            label("Mode: ")
            .append(subModeBtn("Instant", !ai, "cross_mode instant"))
            .append(Component.text("  "))
            .append(subModeBtn("AI Walk", ai, "cross_mode ai"))
        );
        p.sendMessage(Component.empty());

        // Target
        p.sendMessage(
            label("Target: ").append(valueText(s.getCurrentCrossTarget().isEmpty() ? "(none)" : s.getCurrentCrossTarget()))
            .append(Component.text("  "))
            .append(btn("[Set Target]", "cross_target "))
        );
        p.sendMessage(Component.empty());

        // Destination
        p.sendMessage(label("Destination: ").append(valueText(s.getCurrentCrossDest())));
        p.sendMessage(buildPickerRow("cross_dest", ChoreographyEditSession.STAGE_MARKS, s.getCurrentCrossDest()));
        p.sendMessage(Component.empty());

        // Speed (AI mode only)
        if (ai) {
            p.sendMessage(label("Speed: ").append(valueText(s.getCurrentCrossSpeedPreset())));
            p.sendMessage(buildPickerRow("cross_speed", ChoreographyEditSession.SPEED_PRESETS, s.getCurrentCrossSpeedPreset()));
            p.sendMessage(Component.empty());
            p.sendMessage(hint("AI Walk: pathfinder navigates to mark. Arrival not guaranteed on complex terrain. Keep distances short (5-10 blocks)."));
            p.sendMessage(Component.empty());
        } else {
            p.sendMessage(hint("Instant: teleports entity to mark. Reliable, precise, works on puppets."));
            p.sendMessage(Component.empty());
        }

        // Anchor
        p.sendMessage(label("Anchor: ").append(valueText(s.getCurrentCrossAnchor())));
        p.sendMessage(buildPickerRow("cross_anchor", ChoreographyEditSession.ANCHORS, s.getCurrentCrossAnchor()));
        p.sendMessage(Component.empty());

        // Preview (no auto-preview — moving entities is disruptive)
        p.sendMessage(
            Component.text("[▶ Preview]")
                .color(COL_BTN_ON)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam cross_preview"))
                .hoverEvent(HoverEvent.showText(Component.text("Fire a preview CROSS_TO in the session")))
        );
        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CHARACTER LOOK panel
    // -----------------------------------------------------------------------

    private static void sendCharacterLookPanel(Player p, ChoreographyEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CHARACTER LOOK", "FACE — snap entity yaw"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Target
        p.sendMessage(
            label("Target: ").append(valueText(s.getCurrentLookTarget().isEmpty() ? "(none)" : s.getCurrentLookTarget()))
            .append(Component.text("  "))
            .append(btn("[Set Target]", "look_target "))
        );
        p.sendMessage(Component.empty());

        // look_at value
        p.sendMessage(label("Look at: ").append(valueText(s.getCurrentLookAt())));
        p.sendMessage(buildPickerRow("look_at", ChoreographyEditSession.LOOK_AT_SHORTCUTS, s.getCurrentLookAt()));
        p.sendMessage(
            hint("Also accepts: entity:spawned:Name  |  compass:north/south/east/west  |  raw yaw (e.g. 90)")
        );
        p.sendMessage(Component.empty());

        // Pitch warning
        p.sendMessage(
            Component.text("⚠ Pitch unreliable on entities — yaw only is recommended.")
                .color(COL_WARN)
        );
        p.sendMessage(Component.empty());

        // Preview (no auto-preview — snaps entity head)
        p.sendMessage(
            Component.text("[▶ Preview]")
                .color(COL_BTN_ON)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam look_preview"))
                .hoverEvent(HoverEvent.showText(Component.text("Fire a preview FACE in the session")))
        );
        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // PERFORMER STATE panel
    // -----------------------------------------------------------------------

    private static void sendPerformerStatePanel(Player p, ChoreographyEditSession s) {
        boolean puppet = !s.isCurrentStateEnabled();

        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("PERFORMER STATE", "ENTITY_AI — Puppet / Performer"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Target
        p.sendMessage(
            label("Target: ").append(valueText(s.getCurrentStateTarget().isEmpty() ? "(none)" : s.getCurrentStateTarget()))
            .append(Component.text("  "))
            .append(btn("[Set Target]", "state_target "))
        );
        p.sendMessage(Component.empty());

        // State toggle
        p.sendMessage(
            label("State: ")
            .append(subModeBtn("Puppet", puppet, "state_toggle"))
            .append(Component.text("  "))
            .append(subModeBtn("Performer", !puppet, "state_toggle"))
        );
        p.sendMessage(Component.empty());

        // Contextual note
        if (puppet) {
            p.sendMessage(hint("Puppet (AI off): entity holds position. Use for precise blocking, marks, tableau."));
        } else {
            p.sendMessage(hint("Performer (AI on): entity's natural Minecraft AI resumes. Use when genuine creature behaviour serves the story."));
        }
        p.sendMessage(
            Component.text("For sustained stillness, use Puppet. HOLD only zeroes velocity at a single tick.")
                .color(COL_WARN)
        );
        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CHARACTER VELOCITY panel
    // -----------------------------------------------------------------------

    private static void sendCharacterVelocityPanel(Player p, ChoreographyEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CHARACTER VELOCITY", "ENTITY_VELOCITY — impulse launch"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Target
        p.sendMessage(
            label("Target: ").append(valueText(s.getCurrentVelTarget().isEmpty() ? "(none)" : s.getCurrentVelTarget()))
            .append(Component.text("  "))
            .append(btn("[Set Target]", "vel_target "))
        );
        p.sendMessage(Component.empty());

        // Vector x/y/z
        p.sendMessage(vectorRow("X", s.getCurrentVelX(), "vel_x"));
        p.sendMessage(vectorRow("Y", s.getCurrentVelY(), "vel_y"));
        p.sendMessage(vectorRow("Z", s.getCurrentVelZ(), "vel_z"));
        p.sendMessage(Component.empty());

        p.sendMessage(hint("Scroll ±0.1 per click, Shift ±1.0. Y > 0 = upward launch."));
        p.sendMessage(Component.empty());

        // Named velocity presets
        p.sendMessage(label("Presets:"));
        p.sendMessage(buildPickerRow("vel_preset", ChoreographyEditSession.VELOCITY_PRESETS, ""));
        p.sendMessage(hint("gentle_bounce: y=0.4  |  dramatic_launch: y=1.5  |  float_arc: x=0.3 y=1.0"));
        p.sendMessage(Component.empty());

        // Anchor
        p.sendMessage(label("Anchor: ").append(valueText(s.getCurrentVelAnchor())));
        p.sendMessage(buildPickerRow("vel_anchor", ChoreographyEditSession.ANCHORS, s.getCurrentVelAnchor()));
        p.sendMessage(Component.empty());

        // Auto-preview toggle + [▶ Preview]
        boolean autoOn = s.getMode() == ChoreographyEditSession.EventMode.CHARACTER_VELOCITY
            && false; // auto-preview is OFF for velocity per spec
        p.sendMessage(
            Component.text("[▶ Preview]")
                .color(COL_BTN_ON)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam vel_preview"))
                .hoverEvent(HoverEvent.showText(Component.text("Fire a preview ENTITY_VELOCITY in the session")))
                .append(Component.text("  "))
                .append(
                    Component.text("[auto-preview: OFF]")
                        .color(COL_BTN_INCR)
                        .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam auto_preview_toggle"))
                        .hoverEvent(HoverEvent.showText(Component.text("Auto-preview is off for velocity (disruptive). Toggle if needed.")))
                )
        );
        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CHOREO PHRASE panel (stub)
    // -----------------------------------------------------------------------

    private static void sendChoreoPhrasePanel(Player p, ChoreographyEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CHOREO PHRASE", "PHRASE container — step editor"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(
            Component.text("PHRASE step editor is not yet available in Phase 2.")
                .color(COL_HINT)
        );
        p.sendMessage(
            Component.text("Edit steps in the raw YAML for now. Use [Save as Preset] to promote this phrase.")
                .color(COL_HINT)
        );
        p.sendMessage(Component.empty());
        p.sendMessage(saveRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    private static Component sep() {
        return Component.text(SEP).color(COL_SEP);
    }

    private static Component header(String mode, String subtitle) {
        return Component.text("✎ Choreography: ", COL_DEPT)
            .append(Component.text(mode, COL_HEADER).decorate(TextDecoration.BOLD))
            .append(Component.text("  " + subtitle, COL_LABEL));
    }

    private static Component label(String text) {
        return Component.text(text, COL_LABEL);
    }

    private static Component valueText(String text) {
        return Component.text(text, COL_VALUE);
    }

    private static Component hint(String text) {
        return Component.text(text, COL_HINT);
    }

    private static Component btn(String label, String cmd) {
        return Component.text(label)
            .color(COL_BTN)
            .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + cmd))
            .hoverEvent(HoverEvent.showText(Component.text("/scaena tech2 editparam " + cmd)));
    }

    /**
     * Sub-mode button: green when active, gray when inactive.
     * Both states run the same command — clicking active is a no-op (idempotent).
     */
    private static Component subModeBtn(String label, boolean active, String cmd) {
        return Component.text("[" + label + "]")
            .color(active ? COL_BTN_ON : COL_LABEL)
            .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + cmd))
            .hoverEvent(HoverEvent.showText(Component.text(active ? "Active" : "Switch to " + label)));
    }

    /** Boolean toggle button: green ON / red OFF. */
    private static Component boolBtn(boolean value, String cmd) {
        return Component.text(value ? "[ON]" : "[OFF]")
            .color(value ? COL_BTN_ON : COL_BTN_OFF)
            .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + cmd))
            .hoverEvent(HoverEvent.showText(Component.text("Toggle")));
    }

    /**
     * Build a row of picker buttons for a list of options.
     * Active option shown in gold, others in orange.
     */
    private static Component buildPickerRow(String param, java.util.List<String> options, String current) {
        Component row = Component.empty();
        for (String opt : options) {
            boolean active = opt.equals(current);
            Component cell = Component.text("[" + opt + "]")
                .color(active ? COL_HEADER : COL_BTN)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + param + " " + opt))
                .hoverEvent(HoverEvent.showText(Component.text(active ? "Selected" : "Set to " + opt)));
            row = row.append(cell).append(Component.text(" "));
        }
        return row;
    }

    /**
     * Build a vector field row with ± increment buttons.
     * Produces:  X:  [-1] [-0.1] [value] [+0.1] [+1]
     */
    private static Component vectorRow(String axis, double value, String prefix) {
        String display = (value == Math.floor(value) && !Double.isInfinite(value))
            ? String.valueOf((long) value)
            : String.format("%.1f", value);

        return label(axis + ": ")
            .append(incrBtn("-1",   prefix + "_shift_down"))
            .append(Component.text(" "))
            .append(incrBtn("-0.1", prefix + "_down"))
            .append(Component.text("  "))
            .append(Component.text(display, COL_VALUE))
            .append(Component.text("  "))
            .append(incrBtn("+0.1", prefix + "_up"))
            .append(Component.text(" "))
            .append(incrBtn("+1",   prefix + "_shift_up"));
    }

    private static Component incrBtn(String label, String cmd) {
        return Component.text("[" + label + "]")
            .color(COL_BTN_INCR)
            .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + cmd))
            .hoverEvent(HoverEvent.showText(Component.text(cmd)));
    }

    /** Standard [Save] / [Save as Preset] / [Cancel] footer row. */
    private static Component saveRow() {
        return Component.text("[Save]")
            .color(COL_BTN_ON)
            .clickEvent(ClickEvent.runCommand("/scaena tech2 save"))
            .hoverEvent(HoverEvent.showText(Component.text("Commit changes to show YAML")))
            .append(Component.text("  "))
            .append(
                Component.text("[Save as Preset]")
                    .color(COL_BTN)
                    .clickEvent(ClickEvent.runCommand("/scaena tech2 savepreset"))
                    .hoverEvent(HoverEvent.showText(Component.text("Promote cue to cues/*.yml preset library")))
            )
            .append(Component.text("  "))
            .append(
                Component.text("[Cancel]")
                    .color(COL_BTN_OFF)
                    .clickEvent(ClickEvent.runCommand("/scaena tech2 cancel"))
                    .hoverEvent(HoverEvent.showText(Component.text("Discard changes and restore entry state")))
            );
    }
}
