package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Builds and sends the Phase 2 Camera department edit panels.
 *
 * Nine top-level views dispatched by event mode:
 *   FACE             — look_at picker (show-relative, marks, entities; live-swap)
 *   CAMERA           — effect selector + intensity + duration; auto-preview
 *   CAMERA_LOCK      — ON/OFF toggle
 *   MOVEMENT_LOCK    — ON/OFF toggle
 *   BOUNDARY_CHECK   — center mark + radius tuner
 *   VIEW_CHECK       — target + tolerance + pan config
 *   PLAYER_SPECTATE  — spawn/entity mode; audience; duration shortcut
 *   PLAYER_SPECTATE_END — destination picker; audience
 *   PLAYER_MOUNT     — spawn/entity mode; audience; duration shortcut
 *   PLAYER_DISMOUNT  — audience only
 *   CAMERA_PHRASE    — stub: read-only label
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 *
 * Spec: kb/system/phase2-department-panels.md §Camera
 */
public final class CameraPanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0xAAFFCC); // mint (Camera)
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (active/ON)
    private static final TextColor COL_BTN_OFF  = TextColor.color(0xFF5555); // red (OFF state)
    private static final TextColor COL_BTN_INCR = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_ENTITY   = TextColor.color(0xFFCC55); // yellow (entity ref)

    private static final String SEP = "─────────────────────────────────────────────";

    private CameraPanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /** Send the appropriate panel for the session's current event mode. */
    public static void sendPanel(Player p, CameraEditSession session) {
        switch (session.getMode()) {
            case FACE              -> sendFacePanel(p, session);
            case CAMERA            -> sendCameraPanel(p, session);
            case CAMERA_LOCK       -> sendLockPanel(p, session, "Camera Lock", "CAMERA_LOCK");
            case MOVEMENT_LOCK     -> sendLockPanel(p, session, "Movement Lock", "MOVEMENT_LOCK");
            case BOUNDARY_CHECK    -> sendBoundaryCheckPanel(p, session);
            case VIEW_CHECK        -> sendViewCheckPanel(p, session);
            case PLAYER_SPECTATE   -> sendSpectatePanel(p, session);
            case PLAYER_SPECTATE_END -> sendSpectateEndPanel(p, session);
            case PLAYER_MOUNT      -> sendMountPanel(p, session);
            case PLAYER_DISMOUNT   -> sendDismountPanel(p, session);
            case CAMERA_PHRASE     -> sendPhrasePanel(p, session);
        }
    }

    // -----------------------------------------------------------------------
    // FACE panel
    // -----------------------------------------------------------------------
    private static void sendFacePanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("FACE", "Camera dept — look_at"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(
            Component.text("  Look at: ", COL_LABEL)
            .append(Component.text(s.getCurrentFaceLookAt(), COL_VALUE)));

        p.sendMessage(Component.empty());
        p.sendMessage(Component.text("  Quick picks:", COL_LABEL));

        for (String shortcut : CameraEditSession.FACE_SHORTCUTS) {
            boolean active = shortcut.equals(s.getCurrentFaceLookAt());
            p.sendMessage(
                Component.text("    ", COL_LABEL)
                .append(btn("[" + shortcut + "]",
                    "/scaena tech2 editparam look_at " + shortcut,
                    "Set look_at: " + shortcut))
                .color(active ? COL_BTN_ON : COL_BTN));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(
            Component.text("  ", COL_LABEL)
            .append(btn("[▶ Preview]",
                "/scaena tech2 editparam preview_face",
                "Apply face orientation to player now")));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CAMERA panel
    // -----------------------------------------------------------------------
    private static void sendCameraPanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CAMERA", "Screen distortion effect"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        // Effect selector
        p.sendMessage(Component.text("  Effect:", COL_LABEL));
        for (String eff : CameraEditSession.EFFECTS) {
            boolean active = eff.equals(s.getCurrentEffect());
            p.sendMessage(
                Component.text("    ", COL_LABEL)
                .append(btn("[" + eff + "]",
                    "/scaena tech2 editparam effect " + eff,
                    "Effect: " + eff))
                .color(active ? COL_BTN_ON : COL_BTN));
        }

        p.sendMessage(Component.empty());

        // Intensity
        p.sendMessage(
            Component.text("  Intensity: ", COL_LABEL)
            .append(Component.text(String.valueOf(s.getCurrentIntensity()), COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−]", "/scaena tech2 editparam intensity_down", "−1"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+]", "/scaena tech2 editparam intensity_up", "+1")));

        // Duration
        p.sendMessage(
            Component.text("  Duration: ", COL_LABEL)
            .append(Component.text(CameraEditSession.formatDuration(s.getCurrentCamDuration()), COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−20t]", "/scaena tech2 editparam dur_down", "−20t"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+20t]", "/scaena tech2 editparam dur_up", "+20t"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[−100t]", "/scaena tech2 editparam dur_shift_down", "−100t"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+100t]", "/scaena tech2 editparam dur_shift_up", "+100t")));

        // Audience
        p.sendMessage(
            Component.text("  Audience: ", COL_LABEL)
            .append(Component.text(s.getCurrentCamAudience(), COL_VALUE)));
        p.sendMessage(audienceRow("/scaena tech2 editparam audience"));

        p.sendMessage(Component.empty());

        // Auto-preview
        boolean apOn = s.isAutoPreview();
        p.sendMessage(
            Component.text("  Auto-preview: ", COL_LABEL)
            .append(Component.text(apOn ? "ON" : "OFF", apOn ? COL_BTN_ON : COL_BTN_OFF))
            .append(Component.text("  ", COL_LABEL))
            .append(btn("[Toggle]",
                "/scaena tech2 editparam auto_preview_toggle",
                "Toggle auto-preview")));

        if (!apOn) {
            p.sendMessage(
                Component.text("  ", COL_LABEL)
                .append(btn("[▶ Preview]",
                    "/scaena tech2 editparam preview_camera",
                    "Apply camera effect to player now")));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CAMERA_LOCK / MOVEMENT_LOCK panel (shared)
    // -----------------------------------------------------------------------
    private static void sendLockPanel(Player p, CameraEditSession s,
                                       String title, String typeLabel) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header(typeLabel, title));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        boolean isOn = "ON".equals(s.getCurrentLockState());
        p.sendMessage(
            Component.text("  State: ", COL_LABEL)
            .append(Component.text(s.getCurrentLockState(), isOn ? COL_BTN_ON : COL_BTN_OFF))
            .append(Component.text("  ", COL_LABEL))
            .append(btn("[ON]",  "/scaena tech2 editparam lock_state ON",  "Set ON")
                .color(isOn ? COL_BTN_ON : COL_BTN))
            .append(Component.text(" ", COL_LABEL))
            .append(btn("[OFF]", "/scaena tech2 editparam lock_state OFF", "Set OFF")
                .color(!isOn ? COL_BTN_OFF : COL_BTN)));

        p.sendMessage(Component.empty());
        p.sendMessage(Component.text(
            "  Stop-safety: both locks reset to OFF at show end.", COL_HINT));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // BOUNDARY_CHECK panel
    // -----------------------------------------------------------------------
    private static void sendBoundaryCheckPanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("BOUNDARY_CHECK", "Position conditional"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(
            Component.text("  Center: ", COL_LABEL)
            .append(Component.text(s.getCurrentBoundaryCenter(), COL_ENTITY)));

        p.sendMessage(
            Component.text("  Radius: ", COL_LABEL)
            .append(Component.text(String.format("%.0f blocks", s.getCurrentBoundaryRadius()), COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−1]", "/scaena tech2 editparam radius_down", "−1"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+1]", "/scaena tech2 editparam radius_up", "+1"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[−5]", "/scaena tech2 editparam radius_shift_down", "−5"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+5]", "/scaena tech2 editparam radius_shift_up", "+5")));

        p.sendMessage(Component.empty());
        p.sendMessage(Component.text(
            "  out_of_range branch: author in YAML directly.", COL_HINT));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // VIEW_CHECK panel
    // -----------------------------------------------------------------------
    private static void sendViewCheckPanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("VIEW_CHECK", "Orientation conditional — smooth pan"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(
            Component.text("  Target: ", COL_LABEL)
            .append(Component.text(s.getCurrentViewTarget(), COL_ENTITY)));

        p.sendMessage(
            Component.text("  Tolerance: ", COL_LABEL)
            .append(Component.text(s.getCurrentViewTolerance() + "°", COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−5°]", "/scaena tech2 editparam tolerance_down", "−5°"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+5°]", "/scaena tech2 editparam tolerance_up", "+5°"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[−15°]", "/scaena tech2 editparam tolerance_shift_down", "−15°"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+15°]", "/scaena tech2 editparam tolerance_shift_up", "+15°")));

        p.sendMessage(Component.empty());
        p.sendMessage(Component.text("  Correction pan (out_of_view):", COL_LABEL));

        p.sendMessage(
            Component.text("    Duration: ", COL_LABEL)
            .append(Component.text(CameraEditSession.formatDuration(s.getCurrentViewOutDuration()), COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−10t]", "/scaena tech2 editparam out_dur_down", "−10t"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+10t]", "/scaena tech2 editparam out_dur_up", "+10t")));

        p.sendMessage(Component.text("    Interpolation:", COL_LABEL));
        for (String interp : CameraEditSession.VIEW_INTERPS) {
            boolean active = interp.equals(s.getCurrentViewOutInterp());
            p.sendMessage(
                Component.text("      ", COL_LABEL)
                .append(btn("[" + interp + "]",
                    "/scaena tech2 editparam interp " + interp, interp))
                .color(active ? COL_BTN_ON : COL_BTN));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(Component.text(
            "  Correction is always a smooth pan — never a snap.", COL_HINT));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // PLAYER_SPECTATE panel
    // -----------------------------------------------------------------------
    private static void sendSpectatePanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("PLAYER_SPECTATE", "Cinematic camera attach"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        boolean isSpawn = "spawn".equals(s.getCurrentSpectateEntityMode());
        p.sendMessage(
            Component.text("  Camera entity: ", COL_LABEL)
            .append(btn("[Use existing]",
                "/scaena tech2 editparam entity_mode entity",
                "entity: mode — reference existing spawned entity")
                .color(isSpawn ? COL_BTN : COL_BTN_ON))
            .append(Component.text(" ", COL_LABEL))
            .append(btn("[Spawn new]",
                "/scaena tech2 editparam entity_mode spawn",
                "spawn: mode — spawn entity at spectate time")
                .color(isSpawn ? COL_BTN_ON : COL_BTN)));

        p.sendMessage(Component.empty());
        if (isSpawn) {
            p.sendMessage(
                Component.text("    Name: ", COL_LABEL)
                .append(Component.text(s.getCurrentSpectateSpawnName(), COL_VALUE)));
            p.sendMessage(
                Component.text("    Type: ", COL_LABEL)
                .append(Component.text(s.getCurrentSpectateSpawnType(), COL_VALUE)));
            p.sendMessage(offsetRow("    Offset:",
                s.getCurrentSpectateOffX(), s.getCurrentSpectateOffY(), s.getCurrentSpectateOffZ(),
                "off_x", "off_y", "off_z"));
            p.sendMessage(
                Component.text("    Despawn on END: ", COL_LABEL)
                .append(Component.text(s.isCurrentSpectateDespawnOnEnd() ? "✓" : "✗",
                    s.isCurrentSpectateDespawnOnEnd() ? COL_BTN_ON : COL_BTN_OFF))
                .append(Component.text("  ", COL_LABEL))
                .append(btn("[Toggle]",
                    "/scaena tech2 editparam despawn_toggle", "Toggle despawn_on_end")));
        } else {
            p.sendMessage(
                Component.text("    Entity: ", COL_LABEL)
                .append(Component.text(
                    s.getCurrentSpectateEntity().isEmpty() ? "(none)" : s.getCurrentSpectateEntity(),
                    COL_ENTITY)));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(
            Component.text("  Audience: ", COL_LABEL)
            .append(Component.text(s.getCurrentSpectateAudience(), COL_VALUE)));
        p.sendMessage(audienceRow("/scaena tech2 editparam audience"));

        p.sendMessage(Component.empty());

        String durLabel = s.getCurrentSpectateDurationTicks() < 0
            ? "manual"
            : CameraEditSession.formatDuration(s.getCurrentSpectateDurationTicks());
        p.sendMessage(
            Component.text("  Auto-create END cue: ", COL_LABEL)
            .append(Component.text(durLabel, COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−20t]", "/scaena tech2 editparam dur_down", "−20t"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+20t]", "/scaena tech2 editparam dur_up", "+20t"))
            .append(Component.text(" ", COL_LABEL))
            .append(btn("[Clear]", "/scaena tech2 editparam dur_clear", "Reset to manual")));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // PLAYER_SPECTATE_END panel
    // -----------------------------------------------------------------------
    private static void sendSpectateEndPanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("PLAYER_SPECTATE_END", "Return camera to player body"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(Component.text("  Return player to:", COL_LABEL));

        String dest = s.getCurrentSpectateEndDestination();
        boolean isRestore = "restore".equalsIgnoreCase(dest);
        boolean isMark    = dest.startsWith("mark:");
        boolean isEntity  = dest.startsWith("entity:spawned:");

        p.sendMessage(
            Component.text("    ", COL_LABEL)
            .append(btn("[Restore pre-spectate position]",
                "/scaena tech2 editparam destination restore",
                "Restore pre-spectate position")
                .color(isRestore ? COL_BTN_ON : COL_BTN)));

        p.sendMessage(
            Component.text("    ", COL_LABEL)
            .append(btn("[Mark]",
                "/scaena tech2 editparam destination mark:",
                "Teleport to a mark on END")
                .color(isMark ? COL_BTN_ON : COL_BTN)));

        p.sendMessage(
            Component.text("    ", COL_LABEL)
            .append(btn("[Entity position]",
                "/scaena tech2 editparam destination entity:spawned:",
                "Teleport to drone's position on END")
                .color(isEntity ? COL_BTN_ON : COL_BTN)));

        if (!isRestore) {
            p.sendMessage(
                Component.text("    Current: ", COL_LABEL)
                .append(Component.text(dest, COL_ENTITY)));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(
            Component.text("  Audience: ", COL_LABEL)
            .append(Component.text(s.getCurrentSpectateEndAudience(), COL_VALUE)));
        p.sendMessage(audienceRow("/scaena tech2 editparam audience"));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // PLAYER_MOUNT panel
    // -----------------------------------------------------------------------
    private static void sendMountPanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("PLAYER_MOUNT", "Riding perspective"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        boolean isSpawn = "spawn".equals(s.getCurrentMountEntityMode());
        p.sendMessage(
            Component.text("  Mount entity: ", COL_LABEL)
            .append(btn("[Use existing]",
                "/scaena tech2 editparam entity_mode entity",
                "entity: mode")
                .color(isSpawn ? COL_BTN : COL_BTN_ON))
            .append(Component.text(" ", COL_LABEL))
            .append(btn("[Spawn new]",
                "/scaena tech2 editparam entity_mode spawn",
                "spawn: mode")
                .color(isSpawn ? COL_BTN_ON : COL_BTN)));

        p.sendMessage(Component.empty());
        if (isSpawn) {
            p.sendMessage(
                Component.text("    Name: ", COL_LABEL)
                .append(Component.text(s.getCurrentMountSpawnName(), COL_VALUE)));
            p.sendMessage(
                Component.text("    Type: ", COL_LABEL)
                .append(Component.text(s.getCurrentMountSpawnType(), COL_VALUE)));
            p.sendMessage(offsetRow("    Offset:",
                s.getCurrentMountOffX(), s.getCurrentMountOffY(), s.getCurrentMountOffZ(),
                "off_x", "off_y", "off_z"));
            p.sendMessage(
                Component.text("    Invisible: ", COL_LABEL)
                .append(Component.text(s.isCurrentMountInvisible() ? "✓" : "✗",
                    s.isCurrentMountInvisible() ? COL_BTN_ON : COL_BTN_OFF))
                .append(Component.text("  ", COL_LABEL))
                .append(btn("[Toggle]",
                    "/scaena tech2 editparam invisible_toggle", "Toggle invisible")));
            p.sendMessage(
                Component.text("    Despawn on DISMOUNT: ", COL_LABEL)
                .append(Component.text(s.isCurrentMountDespawnOnDismount() ? "✓" : "✗",
                    s.isCurrentMountDespawnOnDismount() ? COL_BTN_ON : COL_BTN_OFF))
                .append(Component.text("  ", COL_LABEL))
                .append(btn("[Toggle]",
                    "/scaena tech2 editparam despawn_toggle", "Toggle despawn_on_dismount")));
        } else {
            p.sendMessage(
                Component.text("    Entity: ", COL_LABEL)
                .append(Component.text(
                    s.getCurrentMountEntity().isEmpty() ? "(none)" : s.getCurrentMountEntity(),
                    COL_ENTITY)));
        }

        p.sendMessage(Component.empty());
        p.sendMessage(
            Component.text("  Audience: ", COL_LABEL)
            .append(Component.text(s.getCurrentMountAudience(), COL_VALUE)));
        p.sendMessage(audienceRow("/scaena tech2 editparam audience"));

        p.sendMessage(Component.empty());

        String durLabel = s.getCurrentMountDurationTicks() < 0
            ? "manual"
            : CameraEditSession.formatDuration(s.getCurrentMountDurationTicks());
        p.sendMessage(
            Component.text("  Auto-create DISMOUNT cue: ", COL_LABEL)
            .append(Component.text(durLabel, COL_VALUE))
            .append(Component.text("  ", COL_LABEL))
            .append(incrBtn("[−20t]", "/scaena tech2 editparam dur_down", "−20t"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[+20t]", "/scaena tech2 editparam dur_up", "+20t"))
            .append(Component.text(" ", COL_LABEL))
            .append(btn("[Clear]", "/scaena tech2 editparam dur_clear", "Reset to manual")));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // PLAYER_DISMOUNT panel
    // -----------------------------------------------------------------------
    private static void sendDismountPanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("PLAYER_DISMOUNT", "End riding perspective"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(
            Component.text("  Audience: ", COL_LABEL)
            .append(Component.text(s.getCurrentDismountAudience(), COL_VALUE)));
        p.sendMessage(audienceRow("/scaena tech2 editparam audience"));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // CAMERA_PHRASE panel (stub)
    // -----------------------------------------------------------------------
    private static void sendPhrasePanel(Player p, CameraEditSession s) {
        p.sendMessage(Component.empty());
        p.sendMessage(sep());
        p.sendMessage(header("CAMERA_PHRASE", "PHRASE with Camera events"));
        p.sendMessage(sep());
        p.sendMessage(Component.empty());

        p.sendMessage(Component.text(
            "  CAMERA_PHRASE step editing is not yet available.", COL_HINT));
        p.sendMessage(Component.text(
            "  Author PHRASE steps directly in YAML.", COL_HINT));

        p.sendMessage(Component.empty());
        p.sendMessage(saveCancelRow());
        p.sendMessage(sep());
    }

    // -----------------------------------------------------------------------
    // Shared component helpers
    // -----------------------------------------------------------------------

    private static Component header(String eventType, String subtitle) {
        return Component.text("✎ ", COL_HEADER)
            .append(Component.text(eventType, COL_DEPT).decorate(TextDecoration.BOLD))
            .append(Component.text("  " + subtitle, COL_HINT));
    }

    private static Component sep() {
        return Component.text(SEP, COL_SEP);
    }

    private static Component btn(String label, String command, String tooltip) {
        return Component.text(label, COL_BTN)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, COL_HINT)));
    }

    private static Component incrBtn(String label, String command, String tooltip) {
        return Component.text(label, COL_BTN_INCR)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(tooltip, COL_HINT)));
    }

    private static Component saveCancelRow() {
        return Component.text("  ", COL_LABEL)
            .append(btn("[Save]", "/scaena tech2 save", "Save changes"))
            .append(Component.text("  ", COL_LABEL))
            .append(btn("[Save as Preset]", "/scaena tech2 saveaspreset", "Save as reusable preset"))
            .append(Component.text("  ", COL_LABEL))
            .append(btn("[Cancel]", "/scaena tech2 cancel", "Discard changes"));
    }

    private static Component audienceRow(String cmdBase) {
        return Component.text("    ", COL_LABEL)
            .append(incrBtn("[participants]", cmdBase + " participants", "All participants"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[private]", cmdBase + " private", "Private/solo"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[group_1]", cmdBase + " group_1", "Group 1"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[group_2]", cmdBase + " group_2", "Group 2"))
            .append(Component.text(" ", COL_LABEL))
            .append(incrBtn("[invoker]", cmdBase + " invoker", "Invoker only"));
    }

    private static Component offsetRow(String label,
                                        double x, double y, double z,
                                        String xKey, String yKey, String zKey) {
        return Component.text(label + " ", COL_LABEL)
            .append(Component.text("x", COL_HINT))
            .append(Component.text("[" + fmt(x) + "]", COL_VALUE)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + xKey + "_up"))
                .hoverEvent(HoverEvent.showText(Component.text("Click ±1", COL_HINT))))
            .append(Component.text("  y", COL_HINT))
            .append(Component.text("[" + fmt(y) + "]", COL_VALUE)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + yKey + "_up"))
                .hoverEvent(HoverEvent.showText(Component.text("Click ±1", COL_HINT))))
            .append(Component.text("  z", COL_HINT))
            .append(Component.text("[" + fmt(z) + "]", COL_VALUE)
                .clickEvent(ClickEvent.runCommand("/scaena tech2 editparam " + zKey + "_up"))
                .hoverEvent(HoverEvent.showText(Component.text("Click ±1", COL_HINT))));
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.format("%.1f", v);
    }
}
