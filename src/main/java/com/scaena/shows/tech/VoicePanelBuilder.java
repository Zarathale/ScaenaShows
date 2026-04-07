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
 * Builds and sends the Phase 2 Voice department edit panels.
 *
 * Five top-level views dispatched by event mode:
 *   MESSAGE    — audience pill row + text field row + preview
 *   TITLE      — audience + title text + subtitle (optional) + timing rows + preview
 *   ACTION_BAR — audience + text + duration_ticks + preview
 *   BOSSBAR    — title + color pills + overlay pills + audience + timing rows + preview
 *   PHRASE     — script editor: numbered step list with timing / type / content + controls
 *
 * Sub-panel:
 *   Phrase line panel — inline editor for a single PHRASE step (opened by [Edit] on a step)
 *
 * All param changes route through /scaena tech2 editparam <key> <value>.
 * Text input for content fields: /scaena tech2 editparam message <text> (command-based,
 * not anvil — per OPS-029 design session §9 text input convention).
 *
 * Spec: kb/system/phase2-department-panels.md §Voice
 */
public final class VoicePanelBuilder {

    // ---- Colours ----
    private static final TextColor COL_HEADER   = TextColor.color(0xFFD700); // gold
    private static final TextColor COL_DEPT     = TextColor.color(0xDD88FF); // lavender (Voice)
    private static final TextColor COL_LABEL    = TextColor.color(0xAAAAAA); // mid gray
    private static final TextColor COL_VALUE    = TextColor.color(0xFFFFFF); // white
    private static final TextColor COL_BTN      = TextColor.color(0xFFAA00); // orange
    private static final TextColor COL_BTN_ON   = TextColor.color(0x55FF55); // green (active)
    private static final TextColor COL_BTN_INCR = TextColor.color(0x888888); // gray (±)
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_HINT     = TextColor.color(0x777777); // dim gray
    private static final TextColor COL_STEP_NUM = TextColor.color(0xAAAAAA); // step number
    private static final TextColor COL_TYPE     = TextColor.color(0x55FFFF); // aqua (event type)
    private static final TextColor COL_CONTENT  = TextColor.color(0xFFFFFF); // white (step text)
    private static final TextColor COL_TIMING   = TextColor.color(0xFFDD88); // amber (timing)

    private static final String SEP = "─────────────────────────────────────────────";

    private VoicePanelBuilder() {}

    // -----------------------------------------------------------------------
    // Dispatch
    // -----------------------------------------------------------------------

    /** Send the appropriate panel for the session's current event mode. */
    public static void sendPanel(Player p, VoiceEditSession session) {
        switch (session.getMode()) {
            case MESSAGE    -> sendMessagePanel(p, session);
            case TITLE      -> sendTitlePanel(p, session);
            case ACTION_BAR -> sendActionBarPanel(p, session);
            case BOSSBAR    -> sendBossbarPanel(p, session);
            case PHRASE     -> sendPhrasePanel(p, session);
        }
    }

    // -----------------------------------------------------------------------
    // MESSAGE panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * MESSAGE  voice.message.sprite_intro
     *
     *   Audience:  [participants] broadcast  invoker  ...
     *   Text:      "&d[Sprite]&f The ceiling opens."    [Set text ▸]
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    public static void sendMessagePanel(Player p, VoiceEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("MESSAGE", session.cueId());

        Component audienceRow = buildAudienceRow(session.getMsgAudience(), "audience");
        Component textRow     = buildTextFieldRow("Text:", session.getMsgText(),
            "/scaena tech2 editparam message ",
            "Type: /scaena tech2 editparam message <text>");

        Component previewRow = buildPreviewRow("/scaena tech2 editparam preview_voice",
            "Send this message to yourself once");

        p.sendMessage(buildPanel(sep, header,
            audienceRow,
            textRow,
            Component.empty(),
            previewRow));
    }

    // -----------------------------------------------------------------------
    // TITLE panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * TITLE  voice.title.silence_coming
     *
     *   Audience:    [participants] broadcast  invoker  ...
     *   Title:       "&6&lYou are here."                  [Set ▸]
     *   Subtitle:    "&7Or somewhere very close."          [Set ▸]  [Clear]
     *   Fade in:     10t (0.5s)   [−] [+]
     *   Stay:        40t (2s)     [−] [+]
     *   Fade out:    10t (0.5s)   [−] [+]
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    public static void sendTitlePanel(Player p, VoiceEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("TITLE", session.cueId());

        Component audienceRow  = buildAudienceRow(session.getTitleAudience(), "audience");
        Component titleRow     = buildTextFieldRow("Title:", session.getTitleText(),
            "/scaena tech2 editparam title_text ",
            "Type: /scaena tech2 editparam title_text <text>");

        // Subtitle row — includes [Clear] since subtitle is optional
        String sub = session.getTitleSubtitle();
        Component subtitleRow = Component.text("  ")
            .append(Component.text("Subtitle:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(sub.isEmpty() ? "(none)" : sub, sub.isEmpty() ? COL_HINT : COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[Set ▸]",
                "/scaena tech2 editparam subtitle_text ",
                COL_BTN,
                "Type: /scaena tech2 editparam subtitle_text <text>"));
        if (!sub.isEmpty()) {
            subtitleRow = subtitleRow
                .append(Component.text(" "))
                .append(clickBtn("[Clear]",
                    "/scaena tech2 editparam subtitle_text ",
                    COL_BTN_INCR,
                    "Remove subtitle"));
        }

        Component fadeInRow  = buildTickIncrRow("Fade in:",  session.getTitleFadeIn(),  "fade_in_down",  "fade_in_up",  "−5t", "+5t");
        Component stayRow    = buildTickIncrRow("Stay:",     session.getTitleStay(),    "stay_down",     "stay_up",     "−5t", "+5t");
        Component fadeOutRow = buildTickIncrRow("Fade out:", session.getTitleFadeOut(), "fade_out_down", "fade_out_up", "−5t", "+5t");
        Component previewRow = buildPreviewRow("/scaena tech2 editparam preview_voice",
            "Show this title to yourself once");

        p.sendMessage(buildPanel(sep, header,
            audienceRow,
            titleRow,
            subtitleRow,
            fadeInRow,
            stayRow,
            fadeOutRow,
            Component.empty(),
            previewRow));
    }

    // -----------------------------------------------------------------------
    // ACTION_BAR panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * ACTION_BAR  voice.action_bar.look_up
     *
     *   Audience:  [participants] broadcast  invoker  ...
     *   Text:      "&7Look up."                    [Set text ▸]
     *   Duration:  60t (3s)   [−] [+]
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    public static void sendActionBarPanel(Player p, VoiceEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("ACTION_BAR", session.cueId());

        Component audienceRow = buildAudienceRow(session.getAbAudience(), "audience");
        Component textRow     = buildTextFieldRow("Text:", session.getAbText(),
            "/scaena tech2 editparam message ",
            "Type: /scaena tech2 editparam message <text>");
        Component durRow      = buildTickIncrRow("Duration:", session.getAbDurationTicks(),
            "dur_down", "dur_up", "−20t", "+20t");
        Component hintRow     = Component.text(
            "  Note: plugin re-sends every 20t for duration_ticks to keep it visible.", COL_HINT);
        Component previewRow  = buildPreviewRow("/scaena tech2 editparam preview_voice",
            "Send this action bar to yourself once");

        p.sendMessage(buildPanel(sep, header,
            audienceRow,
            textRow,
            durRow,
            hintRow,
            Component.empty(),
            previewRow));
    }

    // -----------------------------------------------------------------------
    // BOSSBAR panel
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * BOSSBAR  voice.bossbar.battle_open
     *
     *   Title:     "&6Preparing for Battle"         [Set ▸]
     *   Color:     [BLUE] GREEN  PINK  PURPLE  RED  WHITE  [YELLOW]
     *   Overlay:   [PROGRESS] NOTCHED_6 ...
     *   Audience:  [participants] broadcast  invoker  ...
     *   Duration:  200t (10s)   [−] [+]
     *   Fade in:    10t (0.5s)  [−] [+]
     *   Fade out:   20t (1s)    [−] [+]
     *   Hold:      170t (8.5s)  (duration − fade_in − fade_out)
     *
     *   [▶ Preview]
     * ─────────────────────────────────────────────
     */
    public static void sendBossbarPanel(Player p, VoiceEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("BOSSBAR", session.cueId());

        Component titleRow    = buildTextFieldRow("Title:", session.getBbTitle(),
            "/scaena tech2 editparam title_text ",
            "Type: /scaena tech2 editparam title_text <text>");
        Component colorRow    = buildPillRow("Color:", session.getBbColor(),
            VoiceEditSession.BOSSBAR_COLORS, "color");
        Component overlayRow  = buildPillRow("Overlay:", session.getBbOverlay(),
            VoiceEditSession.BOSSBAR_OVERLAYS, "overlay");
        Component audienceRow = buildAudienceRow(session.getBbAudience(), "audience");
        Component durRow      = buildTickIncrRow("Duration:", session.getBbDurationTicks(), "dur_down", "dur_up", "−20t", "+20t");
        Component fadeInRow   = buildTickIncrRow("Fade in:",  session.getBbFadeInTicks(),  "fadein_down", "fadein_up", "−5t", "+5t");
        Component fadeOutRow  = buildTickIncrRow("Fade out:", session.getBbFadeOutTicks(), "fadeout_down", "fadeout_up", "−5t", "+5t");

        int hold = session.getBbDurationTicks() - session.getBbFadeInTicks() - session.getBbFadeOutTicks();
        Component holdRow = Component.text("  ")
            .append(Component.text("Hold:", COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(hold + "t  (" + ticksToSeconds(hold) + ")", hold >= 0 ? COL_HINT : TextColor.color(0xFF4444)))
            .append(Component.text("   (duration − fade_in − fade_out)", COL_HINT));

        Component previewRow = buildPreviewRow("/scaena tech2 editparam preview_voice",
            "Show this bossbar live (stays until you Save or Cancel)");

        p.sendMessage(buildPanel(sep, header,
            titleRow,
            colorRow,
            overlayRow,
            audienceRow,
            durRow,
            fadeInRow,
            fadeOutRow,
            holdRow,
            Component.empty(),
            previewRow));
    }

    // -----------------------------------------------------------------------
    // PHRASE panel (script editor)
    // -----------------------------------------------------------------------

    /**
     * ─────────────────────────────────────────────
     * VOICE PHRASE  voice.phrase.revelation
     *   Audience (default):  [participants] broadcast ...
     *
     *   1.  [at: 0     ]  BOSSBAR    "Preparing for Battle"         [Edit] [↓] [✕]
     *   2.  [after: 40 ]  MESSAGE    "[Sprite] The ceiling opens."  [Edit] [↑] [↓] [✕]
     *   3.  [after: 80 ]  MESSAGE    "[Sprite] Something has been…" [Edit] [↑] [↓] [✕]
     *   4.  [after: 60 ]  TITLE      "You are here."                [Edit] [↑] [✕]
     *
     *   [ + Add Line ]
     *
     *   [▶ Preview Phrase]
     * ─────────────────────────────────────────────
     */
    public static void sendPhrasePanel(Player p, VoiceEditSession session) {
        Component sep    = Component.text(SEP, COL_SEP);
        Component header = deptHeader("VOICE PHRASE", session.cueId());

        Component audienceRow = buildAudienceRow(session.getPhraseAudience(), "phrase_audience");

        Component body = Component.empty();
        List<Map<String, Object>> steps = session.getPhraseSteps();

        if (steps.isEmpty()) {
            body = Component.text("  (no steps — click [ + Add Line ] to begin)", COL_HINT);
        } else {
            for (int i = 0; i < steps.size(); i++) {
                body = body.append(buildStepRow(i, steps.get(i), steps.size()));
                if (i < steps.size() - 1) body = body.append(Component.newline());
            }
        }

        Component addLine = Component.text("  ")
            .append(clickBtn("[ + Add Line ]",
                "/scaena tech2 editparam step_add",
                COL_BTN,
                "Append a new MESSAGE step"));

        Component previewRow = buildPreviewRow("/scaena tech2 editparam preview_phrase",
            "Preview all steps as flat chat messages");

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(audienceRow).append(Component.newline())
            .append(Component.newline())
            .append(body).append(Component.newline())
            .append(Component.newline())
            .append(addLine).append(Component.newline())
            .append(Component.newline())
            .append(previewRow).append(Component.newline())
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Phrase line panel (step edit sub-panel)
    // -----------------------------------------------------------------------

    /**
     * Inline panel for editing a single PHRASE step.
     * Shows timing fields + the step event's own fields.
     *
     * ─────────────────────────────────────────────
     * Step 2 of 4  [← Back to list]
     *   Timing:    [after] at    40t   [−] [+]
     *   Type:      [MESSAGE] TITLE  ACTION_BAR  BOSSBAR
     *   Text:      "&d[Sprite]&f The ceiling opens."   [Set ▸]
     *   Audience:  [participants] broadcast ...
     * ─────────────────────────────────────────────
     */
    @SuppressWarnings("unchecked")
    public static void sendPhraseLinePanel(Player p, VoiceEditSession session, int stepIndex) {
        List<Map<String, Object>> steps = session.getPhraseSteps();
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            sendPanel(p, session);
            return;
        }
        Map<String, Object> step = steps.get(stepIndex);
        List<Map<String, Object>> events = (List<Map<String, Object>>) step.get("events");
        Map<String, Object> evt = (events != null && !events.isEmpty()) ? events.get(0) : Map.of();

        String evtType = (String) evt.getOrDefault("type", "MESSAGE");
        boolean isAfter = step.containsKey("after");
        int ticks = isAfter ? intFromMap(step, "after", 0) : intFromMap(step, "at", 0);

        Component sep    = Component.text(SEP, COL_SEP);
        Component header = Component.text("Step " + (stepIndex + 1) + " of " + steps.size(),
                COL_DEPT, TextDecoration.BOLD)
            .append(Component.text("    "))
            .append(clickBtn("[← Back to list]",
                "/scaena tech2 editparam step_done",
                COL_BTN,
                "Return to the PHRASE step list"));

        // Timing row
        Component timingModeBtn = Component.text("  ")
            .append(Component.text("Timing:", COL_LABEL))
            .append(Component.text("  "))
            .append(clickBtn(isAfter ? "[after]" : "after",
                "/scaena tech2 editparam step_timing_mode after",
                isAfter ? COL_BTN_ON : COL_BTN,
                "Relative: N ticks after previous step"))
            .append(Component.text(" "))
            .append(clickBtn(isAfter ? "at" : "[at]",
                "/scaena tech2 editparam step_timing_mode at",
                isAfter ? COL_BTN : COL_BTN_ON,
                "Absolute: N ticks from cue start"))
            .append(Component.text("    "))
            .append(Component.text(ticks + "t", COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[−]", "/scaena tech2 editparam step_ticks_down", COL_BTN_INCR, "−10t"))
            .append(Component.text(" "))
            .append(clickBtn("[+]", "/scaena tech2 editparam step_ticks_up", COL_BTN_INCR, "+10t"));

        // Type row — pills for the four supported step types
        Component typeRow = buildStepTypePills(evtType);

        // Event-specific fields
        Component fieldsBlock = buildStepFields(evtType, evt);

        p.sendMessage(Component.empty()
            .append(sep).append(Component.newline())
            .append(header).append(Component.newline())
            .append(Component.newline())
            .append(timingModeBtn).append(Component.newline())
            .append(typeRow).append(Component.newline())
            .append(fieldsBlock)
            .append(sep));
    }

    // -----------------------------------------------------------------------
    // Step row builder (used in the PHRASE list view)
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Component buildStepRow(int i, Map<String, Object> step, int totalSteps) {
        boolean isAfter = step.containsKey("after");
        int ticks = isAfter ? intFromMap(step, "after", 0) : intFromMap(step, "at", 0);
        String timingLabel = isAfter ? "after:" : "at:   ";
        String timingVal   = String.format("%-3d", ticks);

        List<Map<String, Object>> evs = (List<Map<String, Object>>) step.get("events");
        String evtType = "?";
        String content = "(empty)";
        if (evs != null && !evs.isEmpty()) {
            Map<String, Object> ev = evs.get(0);
            evtType = (String) ev.getOrDefault("type", "?");
            String text = "";
            if (ev.containsKey("message")) text = (String) ev.get("message");
            else if (ev.containsKey("title")) text = (String) ev.get("title");
            if (!text.isEmpty()) {
                content = truncate(stripFormatCodes(text), 36);
            }
        }

        Component row = Component.text("  ")
            .append(Component.text((i + 1) + ". ", COL_STEP_NUM))
            .append(Component.text("[" + timingLabel + " " + timingVal + "]", COL_TIMING))
            .append(Component.text("  "))
            .append(Component.text(String.format("%-10s", evtType), COL_TYPE))
            .append(Component.text("  "))
            .append(Component.text("\"" + content + "\"", COL_CONTENT))
            .append(Component.text("  "))
            .append(clickBtn("[Edit]",
                "/scaena tech2 editparam step_edit " + i,
                COL_BTN, "Edit this step inline"));

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
                TextColor.color(0xFF4444), "Delete this step"));

        return row;
    }

    // -----------------------------------------------------------------------
    // Step type pill row
    // -----------------------------------------------------------------------

    private static Component buildStepTypePills(String currentType) {
        Component row = Component.text("  ").append(Component.text("Type:", COL_LABEL)).append(Component.text("  "));
        for (String t : List.of("MESSAGE", "TITLE", "ACTION_BAR", "BOSSBAR")) {
            boolean active = t.equals(currentType);
            row = row.append(
                clickBtn(active ? "[" + t + "]" : t,
                    "/scaena tech2 editparam step_type " + t,
                    active ? COL_BTN_ON : COL_BTN,
                    "Switch to " + t)
            ).append(Component.text(" "));
        }
        return row;
    }

    // -----------------------------------------------------------------------
    // Step-specific field rows
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Component buildStepFields(String evtType, Map<String, Object> evt) {
        Component body = Component.empty();
        switch (evtType) {
            case "MESSAGE" -> {
                String text = (String) evt.getOrDefault("message", "");
                String aud  = (String) evt.getOrDefault("audience", "participants");
                body = body
                    .append(buildTextFieldRow("Text:", text, "/scaena tech2 editparam step_text ", "Type: /scaena tech2 editparam step_text <text>"))
                    .append(Component.newline())
                    .append(buildStepAudienceRow(aud))
                    .append(Component.newline());
            }
            case "TITLE" -> {
                String title    = (String) evt.getOrDefault("title", "");
                String subtitle = (String) evt.getOrDefault("subtitle", "");
                String aud      = (String) evt.getOrDefault("audience", "participants");
                int fadeIn  = intFromMap(evt, "fade_in", 10);
                int stay    = intFromMap(evt, "stay", 40);
                int fadeOut = intFromMap(evt, "fade_out", 10);
                body = body
                    .append(buildTextFieldRow("Title:", title, "/scaena tech2 editparam step_text ", "Type: /scaena tech2 editparam step_text <text>"))
                    .append(Component.newline())
                    .append(buildTextFieldRow("Subtitle:", subtitle.isEmpty() ? "(none)" : subtitle, "/scaena tech2 editparam step_subtitle ", "Type: /scaena tech2 editparam step_subtitle <text> (blank to clear)"))
                    .append(Component.newline())
                    .append(buildStepAudienceRow(aud)).append(Component.newline())
                    .append(buildTickIncrRow("Fade in:", fadeIn, "step_fade_in_down", "step_fade_in_up", "−5t", "+5t")).append(Component.newline())
                    .append(buildTickIncrRow("Stay:", stay, "step_stay_down", "step_stay_up", "−5t", "+5t")).append(Component.newline())
                    .append(buildTickIncrRow("Fade out:", fadeOut, "step_fade_out_down", "step_fade_out_up", "−5t", "+5t")).append(Component.newline());
            }
            case "ACTION_BAR" -> {
                String text = (String) evt.getOrDefault("message", "");
                String aud  = (String) evt.getOrDefault("audience", "participants");
                int dur     = intFromMap(evt, "duration_ticks", 60);
                body = body
                    .append(buildTextFieldRow("Text:", text, "/scaena tech2 editparam step_text ", "Type: /scaena tech2 editparam step_text <text>"))
                    .append(Component.newline())
                    .append(buildStepAudienceRow(aud)).append(Component.newline())
                    .append(buildTickIncrRow("Duration:", dur, "step_dur_down", "step_dur_up", "−20t", "+20t")).append(Component.newline());
            }
            case "BOSSBAR" -> {
                String title   = (String) evt.getOrDefault("title", "");
                String color   = (String) evt.getOrDefault("color", "YELLOW");
                String overlay = (String) evt.getOrDefault("overlay", "PROGRESS");
                String aud     = (String) evt.getOrDefault("audience", "participants");
                int dur        = intFromMap(evt, "duration_ticks", 200);
                int fadeIn     = intFromMap(evt, "fade_in_ticks", 10);
                int fadeOut    = intFromMap(evt, "fade_out_ticks", 20);
                body = body
                    .append(buildTextFieldRow("Title:", title, "/scaena tech2 editparam step_text ", "Type: /scaena tech2 editparam step_text <text>"))
                    .append(Component.newline())
                    .append(buildPillRow("Color:", color, VoiceEditSession.BOSSBAR_COLORS, "step_color"))
                    .append(Component.newline())
                    .append(buildPillRow("Overlay:", overlay, VoiceEditSession.BOSSBAR_OVERLAYS, "step_overlay"))
                    .append(Component.newline())
                    .append(buildStepAudienceRow(aud)).append(Component.newline())
                    .append(buildTickIncrRow("Duration:", dur, "step_dur_down", "step_dur_up", "−20t", "+20t")).append(Component.newline())
                    .append(buildTickIncrRow("Fade in:", fadeIn, "step_fade_in_down", "step_fade_in_up", "−5t", "+5t")).append(Component.newline())
                    .append(buildTickIncrRow("Fade out:", fadeOut, "step_fade_out_down", "step_fade_out_up", "−5t", "+5t")).append(Component.newline());
            }
            default -> {
                body = Component.text("  (unknown step type: " + evtType + ")", COL_HINT).append(Component.newline());
            }
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

    private static Component buildAudienceRow(String currentAudience, String paramKey) {
        Component row = Component.text("  ")
            .append(Component.text("Audience:", COL_LABEL))
            .append(Component.text("  "));
        for (String aud : VoiceEditSession.AUDIENCES) {
            boolean active = aud.equalsIgnoreCase(currentAudience);
            row = row.append(
                clickBtn(active ? "[" + aud + "]" : aud,
                    "/scaena tech2 editparam " + paramKey + " " + aud,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set audience to " + aud)
            ).append(Component.text(" "));
        }
        return row;
    }

    private static Component buildStepAudienceRow(String currentAudience) {
        Component row = Component.text("  ")
            .append(Component.text("Audience:", COL_LABEL))
            .append(Component.text("  "));
        for (String aud : VoiceEditSession.AUDIENCES) {
            boolean active = aud.equalsIgnoreCase(currentAudience);
            row = row.append(
                clickBtn(active ? "[" + aud + "]" : aud,
                    "/scaena tech2 editparam step_audience " + aud,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set audience to " + aud)
            ).append(Component.text(" "));
        }
        return row;
    }

    private static Component buildPillRow(String label, String current,
                                          List<String> options, String paramKey) {
        Component row = Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "));
        for (String opt : options) {
            boolean active = opt.equalsIgnoreCase(current);
            row = row.append(
                clickBtn(active ? "[" + opt + "]" : opt,
                    "/scaena tech2 editparam " + paramKey + " " + opt,
                    active ? COL_BTN_ON : COL_BTN,
                    "Set to " + opt)
            ).append(Component.text(" "));
        }
        return row;
    }

    private static Component buildTextFieldRow(String label, String currentValue,
                                               String cmdPrefix, String tooltip) {
        String display = currentValue.isEmpty() ? "(empty)" : truncate(stripFormatCodes(currentValue), 40);
        return Component.text("  ")
            .append(Component.text(label, COL_LABEL))
            .append(Component.text("  "))
            .append(Component.text(display, currentValue.isEmpty() ? COL_HINT : COL_VALUE))
            .append(Component.text("    "))
            .append(clickBtn("[Set ▸]", cmdPrefix, COL_BTN, tooltip));
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

    private static Component buildPreviewRow(String cmd, String tooltip) {
        return Component.text("  ")
            .append(clickBtn("[▶ Preview]", cmd, COL_BTN, tooltip));
    }

    /** Build a panel component that assembles separator + header + rows. */
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

    private static String ticksToSeconds(int ticks) {
        float s = ticks / 20.0f;
        if (s == (int) s) return (int) s + "s";
        return String.format("%.2f", s).replaceAll("0+$", "").replaceAll("\\.$", "") + "s";
    }

    private static String stripFormatCodes(String s) {
        return s.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("<[^>]+>", "");
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    private static int intFromMap(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }
}
