package com.scaena.shows.tech;

import com.scaena.shows.model.Cue;
import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.TextEvents.ActionBarEvent;
import com.scaena.shows.model.event.TextEvents.BossbarEvent;
import com.scaena.shows.model.event.TextEvents.MessageEvent;
import com.scaena.shows.model.event.TextEvents.PhraseEvent;
import com.scaena.shows.model.event.TextEvents.TitleEvent;
import com.scaena.shows.registry.CueRegistry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Phase 2 department edit session — Voice.
 *
 * Handles five event types found in voice cues:
 *
 *   MESSAGE    — audience + chat text
 *   TITLE      — audience + title + subtitle + fade_in / stay / fade_out
 *   ACTION_BAR — audience + text + duration_ticks
 *   BOSSBAR    — title + color + overlay + audience + duration + fade timing
 *   PHRASE     — multi-step script editor (list of timing + event pairs)
 *
 * Mode is detected from the first voice event in the cue. Empty/stub cues default
 * to MESSAGE mode. PHRASE mode uses a list-editor panel rather than a single-param form.
 *
 * Preview: text events fire directly to the player as preview. No world side-effects;
 * Cancel does not need to revert world state. BOSSBAR preview is held live as long as
 * the edit session is active and dismissed on save or cancel.
 *
 * Preset naming: voice.[instrument].[slug]
 * e.g. voice.message.sprite_intro, voice.bossbar.battle_open, voice.phrase.revelation
 *
 * Spec: kb/system/phase2-department-panels.md §Voice
 */
public final class VoiceEditSession implements DeptEditSession {

    // -----------------------------------------------------------------------
    // Curated lists
    // -----------------------------------------------------------------------

    /** Audience pill options — the most commonly used values for Voice events. */
    public static final List<String> AUDIENCES = List.of(
        "participants", "broadcast", "invoker", "private", "group_1", "group_2"
    );

    /** BossBar color values supported by the BOSSBAR event. */
    public static final List<String> BOSSBAR_COLORS = List.of(
        "BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW"
    );

    /** BossBar overlay values supported by the BOSSBAR event. */
    public static final List<String> BOSSBAR_OVERLAYS = List.of(
        "PROGRESS", "NOTCHED_6", "NOTCHED_10", "NOTCHED_12", "NOTCHED_20"
    );

    // -----------------------------------------------------------------------
    // Event mode
    // -----------------------------------------------------------------------

    public enum EventMode { MESSAGE, TITLE, ACTION_BAR, BOSSBAR, PHRASE }

    // -----------------------------------------------------------------------
    // Identity
    // -----------------------------------------------------------------------

    private final String         cueId;
    private final Player         player;
    private final TechCueSession cueSession;
    private final ShowYamlEditor editor;
    private final CueRegistry    cueRegistry;
    private final Logger         log;

    // -----------------------------------------------------------------------
    // Mode
    // -----------------------------------------------------------------------

    private final EventMode mode;

    // -----------------------------------------------------------------------
    // Entry snapshots (for Cancel)
    // -----------------------------------------------------------------------

    // MESSAGE
    private final String entryMsgAudience;
    private final String entryMsgText;

    // TITLE
    private final String entryTitleAudience;
    private final String entryTitleText;
    private final String entryTitleSubtitle;
    private final int    entryTitleFadeIn;
    private final int    entryTitleStay;
    private final int    entryTitleFadeOut;

    // ACTION_BAR
    private final String entryAbAudience;
    private final String entryAbText;
    private final int    entryAbDurationTicks;

    // BOSSBAR
    private final String entryBbTitle;
    private final String entryBbColor;
    private final String entryBbOverlay;
    private final String entryBbAudience;
    private final int    entryBbDurationTicks;
    private final int    entryBbFadeInTicks;
    private final int    entryBbFadeOutTicks;

    // PHRASE
    private final String                   entryPhraseAudience;
    private final List<Map<String, Object>> entryPhraseSteps;   // deep copy for restore

    // -----------------------------------------------------------------------
    // Current mutable state
    // -----------------------------------------------------------------------

    // MESSAGE
    private String msgAudience;
    private String msgText;

    // TITLE
    private String titleAudience;
    private String titleText;
    private String titleSubtitle;
    private int    titleFadeIn;
    private int    titleStay;
    private int    titleFadeOut;

    // ACTION_BAR
    private String abAudience;
    private String abText;
    private int    abDurationTicks;

    // BOSSBAR
    private String bbTitle;
    private String bbColor;
    private String bbOverlay;
    private String bbAudience;
    private int    bbDurationTicks;
    private int    bbFadeInTicks;
    private int    bbFadeOutTicks;

    // PHRASE
    private String                   phraseAudience;
    private List<Map<String, Object>> phraseSteps;    // mutable; each step is a raw map
    /** Index of the step being edited inline. -1 = showing the step list. */
    private int    editingStepIndex = -1;

    // -----------------------------------------------------------------------
    // BOSSBAR preview handle
    // -----------------------------------------------------------------------

    /** Live BossBar shown to the player during an active BOSSBAR preview. Hidden on exit. */
    private BossBar previewBossBar;

    // -----------------------------------------------------------------------

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public VoiceEditSession(
        String         cueId,
        Player         player,
        TechCueSession cueSession,
        ShowYamlEditor editor,
        CueRegistry    cueRegistry,
        Logger         log
    ) {
        this.cueId       = cueId;
        this.player      = player;
        this.cueSession  = cueSession;
        this.editor      = editor;
        this.cueRegistry = cueRegistry;
        this.log         = log;

        // Detect the first voice event in the cue to determine mode and seed entry state.
        ShowEvent found = findVoiceEvent(cueId);

        // ---- Detect mode; assign ALL final fields in every branch ----
        // Java's definite-assignment rules require every final field to be set exactly once.
        // Each branch sets the active mode's fields from the event and everything else to defaults.
        if (found instanceof PhraseEvent pe) {
            mode                 = EventMode.PHRASE;
            entryMsgAudience     = "participants"; entryMsgText          = "";
            entryTitleAudience   = "participants"; entryTitleText        = ""; entryTitleSubtitle = "";
            entryTitleFadeIn     = 10;             entryTitleStay        = 40; entryTitleFadeOut  = 10;
            entryAbAudience      = "participants"; entryAbText           = ""; entryAbDurationTicks = 60;
            entryBbTitle         = "";             entryBbColor          = "YELLOW"; entryBbOverlay = "PROGRESS";
            entryBbAudience      = "participants"; entryBbDurationTicks  = 200;
            entryBbFadeInTicks   = 10;             entryBbFadeOutTicks   = 20;
            entryPhraseAudience  = pe.audience;    entryPhraseSteps      = deepCopySteps(pe.rawSteps);
        } else if (found instanceof MessageEvent me) {
            mode                 = EventMode.MESSAGE;
            entryMsgAudience     = me.audience;    entryMsgText          = me.message;
            entryTitleAudience   = "participants"; entryTitleText        = ""; entryTitleSubtitle = "";
            entryTitleFadeIn     = 10;             entryTitleStay        = 40; entryTitleFadeOut  = 10;
            entryAbAudience      = "participants"; entryAbText           = ""; entryAbDurationTicks = 60;
            entryBbTitle         = "";             entryBbColor          = "YELLOW"; entryBbOverlay = "PROGRESS";
            entryBbAudience      = "participants"; entryBbDurationTicks  = 200;
            entryBbFadeInTicks   = 10;             entryBbFadeOutTicks   = 20;
            entryPhraseAudience  = "participants"; entryPhraseSteps      = new ArrayList<>();
        } else if (found instanceof TitleEvent te) {
            mode                 = EventMode.TITLE;
            entryMsgAudience     = "participants"; entryMsgText          = "";
            entryTitleAudience   = te.audience;    entryTitleText        = te.title;
            entryTitleSubtitle   = te.subtitle;    entryTitleFadeIn      = te.fadeIn;
            entryTitleStay       = te.stay;        entryTitleFadeOut     = te.fadeOut;
            entryAbAudience      = "participants"; entryAbText           = ""; entryAbDurationTicks = 60;
            entryBbTitle         = "";             entryBbColor          = "YELLOW"; entryBbOverlay = "PROGRESS";
            entryBbAudience      = "participants"; entryBbDurationTicks  = 200;
            entryBbFadeInTicks   = 10;             entryBbFadeOutTicks   = 20;
            entryPhraseAudience  = "participants"; entryPhraseSteps      = new ArrayList<>();
        } else if (found instanceof ActionBarEvent ae) {
            mode                 = EventMode.ACTION_BAR;
            entryMsgAudience     = "participants"; entryMsgText          = "";
            entryTitleAudience   = "participants"; entryTitleText        = ""; entryTitleSubtitle = "";
            entryTitleFadeIn     = 10;             entryTitleStay        = 40; entryTitleFadeOut  = 10;
            entryAbAudience      = ae.audience;    entryAbText           = ae.message;
            entryAbDurationTicks = ae.durationTicks;
            entryBbTitle         = "";             entryBbColor          = "YELLOW"; entryBbOverlay = "PROGRESS";
            entryBbAudience      = "participants"; entryBbDurationTicks  = 200;
            entryBbFadeInTicks   = 10;             entryBbFadeOutTicks   = 20;
            entryPhraseAudience  = "participants"; entryPhraseSteps      = new ArrayList<>();
        } else if (found instanceof BossbarEvent be) {
            mode                 = EventMode.BOSSBAR;
            entryMsgAudience     = "participants"; entryMsgText          = "";
            entryTitleAudience   = "participants"; entryTitleText        = ""; entryTitleSubtitle = "";
            entryTitleFadeIn     = 10;             entryTitleStay        = 40; entryTitleFadeOut  = 10;
            entryAbAudience      = "participants"; entryAbText           = ""; entryAbDurationTicks = 60;
            entryBbTitle         = be.title;       entryBbColor          = be.color;
            entryBbOverlay       = be.overlay;     entryBbAudience       = be.audience;
            entryBbDurationTicks = be.durationTicks;
            entryBbFadeInTicks   = be.fadeInTicks; entryBbFadeOutTicks   = be.fadeOutTicks;
            entryPhraseAudience  = "participants"; entryPhraseSteps      = new ArrayList<>();
        } else {
            // Default: MESSAGE mode for empty/stub cues
            mode                 = EventMode.MESSAGE;
            entryMsgAudience     = "participants"; entryMsgText          = "";
            entryTitleAudience   = "participants"; entryTitleText        = ""; entryTitleSubtitle = "";
            entryTitleFadeIn     = 10;             entryTitleStay        = 40; entryTitleFadeOut  = 10;
            entryAbAudience      = "participants"; entryAbText           = ""; entryAbDurationTicks = 60;
            entryBbTitle         = "";             entryBbColor          = "YELLOW"; entryBbOverlay = "PROGRESS";
            entryBbAudience      = "participants"; entryBbDurationTicks  = 200;
            entryBbFadeInTicks   = 10;             entryBbFadeOutTicks   = 20;
            entryPhraseAudience  = "participants"; entryPhraseSteps      = new ArrayList<>();
        }

        // ---- Initialise current state from entry snapshots ----
        msgAudience      = entryMsgAudience;
        msgText          = entryMsgText;
        titleAudience    = entryTitleAudience;
        titleText        = entryTitleText;
        titleSubtitle    = entryTitleSubtitle;
        titleFadeIn      = entryTitleFadeIn;
        titleStay        = entryTitleStay;
        titleFadeOut     = entryTitleFadeOut;
        abAudience       = entryAbAudience;
        abText           = entryAbText;
        abDurationTicks  = entryAbDurationTicks;
        bbTitle          = entryBbTitle;
        bbColor          = entryBbColor;
        bbOverlay        = entryBbOverlay;
        bbAudience       = entryBbAudience;
        bbDurationTicks  = entryBbDurationTicks;
        bbFadeInTicks    = entryBbFadeInTicks;
        bbFadeOutTicks   = entryBbFadeOutTicks;
        phraseAudience   = entryPhraseAudience;
        phraseSteps      = deepCopySteps(entryPhraseSteps);
    }

    // -----------------------------------------------------------------------
    // DeptEditSession contract
    // -----------------------------------------------------------------------

    @Override public String cueId()      { return cueId; }
    @Override public String department() { return "voice"; }

    @Override
    public void onSave() {
        hideBossBarPreview();
        writeToEditor();
    }

    @Override
    public void onSaveAsPreset() {
        hideBossBarPreview();
        writeToEditor();
        // Preset naming: voice.[instrument].[slug]
        String instrument = mode.name().toLowerCase();
        String slug = cueId.contains(".")
            ? cueId.substring(cueId.lastIndexOf('.') + 1)
            : cueId;
        String presetId = "voice." + instrument + "." + slug;
        editor.saveAsPreset(cueId, presetId);
    }

    @Override
    public void onCancel() {
        hideBossBarPreview();
        // Restore entry state
        msgAudience      = entryMsgAudience;
        msgText          = entryMsgText;
        titleAudience    = entryTitleAudience;
        titleText        = entryTitleText;
        titleSubtitle    = entryTitleSubtitle;
        titleFadeIn      = entryTitleFadeIn;
        titleStay        = entryTitleStay;
        titleFadeOut     = entryTitleFadeOut;
        abAudience       = entryAbAudience;
        abText           = entryAbText;
        abDurationTicks  = entryAbDurationTicks;
        bbTitle          = entryBbTitle;
        bbColor          = entryBbColor;
        bbOverlay        = entryBbOverlay;
        bbAudience       = entryBbAudience;
        bbDurationTicks  = entryBbDurationTicks;
        bbFadeInTicks    = entryBbFadeInTicks;
        bbFadeOutTicks   = entryBbFadeOutTicks;
        phraseAudience   = entryPhraseAudience;
        phraseSteps      = deepCopySteps(entryPhraseSteps);
        editingStepIndex = -1;
    }

    /**
     * Handle a /scaena tech2 editparam <key> [value...] command.
     *
     * === MESSAGE keys ===
     *   audience <val>           — set audience
     *   message <text>           — set message text
     *   preview_voice            — fire current text event once at player
     *
     * === TITLE keys ===
     *   audience <val>           — set audience
     *   title_text <text>        — set title line
     *   subtitle_text <text>     — set subtitle line (empty string to clear)
     *   fade_in_up / fade_in_down / fade_in <n>
     *   stay_up / stay_down / stay <n>
     *   fade_out_up / fade_out_down / fade_out <n>
     *   preview_voice            — show title to player
     *
     * === ACTION_BAR keys ===
     *   audience <val>           — set audience
     *   message <text>           — set action bar text
     *   dur_up / dur_down / duration_ticks <n>
     *   preview_voice            — send action bar to player
     *
     * === BOSSBAR keys ===
     *   title_text <text>        — set bossbar title
     *   color <val>              — set color (one of BOSSBAR_COLORS)
     *   overlay <val>            — set overlay (one of BOSSBAR_OVERLAYS)
     *   audience <val>           — set audience
     *   dur_up / dur_down / duration_ticks <n>
     *   fadein_up / fadein_down / fade_in_ticks <n>
     *   fadeout_up / fadeout_down / fade_out_ticks <n>
     *   preview_voice            — show/refresh live bossbar preview
     *
     * === PHRASE (list view, editingStepIndex < 0) keys ===
     *   phrase_audience <val>    — set phrase-level audience default
     *   step_add                 — append a blank MESSAGE step
     *   step_delete <i>          — delete step at 0-based index i
     *   step_up <i>              — move step i one position up
     *   step_down <i>            — move step i one position down
     *   step_edit <i>            — enter step-edit sub-panel for step i
     *   preview_phrase           — send each step as a flat chat preview
     *
     * === PHRASE (step edit sub-panel, editingStepIndex >= 0) keys ===
     *   step_type <type>         — change step event type (clears event fields)
     *   step_text <text>         — set text / title field for the step event
     *   step_subtitle <text>     — set subtitle (TITLE step only)
     *   step_audience <val>      — set audience for the step event
     *   step_timing_mode at|after — switch timing between absolute and relative
     *   step_ticks_up / step_ticks_down / step_ticks <n>
     *   step_fade_in_up / step_fade_in_down / step_fade_in <n>
     *   step_stay_up / step_stay_down / step_stay <n>
     *   step_fade_out_up / step_fade_out_down / step_fade_out <n>
     *   step_color <val>         — BOSSBAR color for step
     *   step_overlay <val>       — BOSSBAR overlay for step
     *   step_dur_up / step_dur_down / step_duration_ticks <n>
     *   step_done                — return to phrase list view
     */
    @Override
    public boolean onEditParam(String key, String value) {
        String lower = key.toLowerCase();

        // ---- Universal ----
        if (lower.equals("preview_voice")) {
            firePreview();
            return true;
        }

        // ---- Mode-specific dispatch ----
        switch (mode) {
            case MESSAGE    -> { return handleMessageParam(lower, value); }
            case TITLE      -> { return handleTitleParam(lower, value); }
            case ACTION_BAR -> { return handleActionBarParam(lower, value); }
            case BOSSBAR    -> { return handleBossbarParam(lower, value); }
            case PHRASE     -> {
                if (editingStepIndex >= 0) {
                    return handleStepParam(lower, value);
                } else {
                    return handlePhraseParam(lower, value);
                }
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Accessors (for VoicePanelBuilder)
    // -----------------------------------------------------------------------

    public EventMode getMode()             { return mode; }

    // MESSAGE
    public String getMsgAudience()         { return msgAudience; }
    public String getMsgText()             { return msgText; }

    // TITLE
    public String getTitleAudience()       { return titleAudience; }
    public String getTitleText()           { return titleText; }
    public String getTitleSubtitle()       { return titleSubtitle; }
    public int    getTitleFadeIn()         { return titleFadeIn; }
    public int    getTitleStay()           { return titleStay; }
    public int    getTitleFadeOut()        { return titleFadeOut; }

    // ACTION_BAR
    public String getAbAudience()          { return abAudience; }
    public String getAbText()              { return abText; }
    public int    getAbDurationTicks()     { return abDurationTicks; }

    // BOSSBAR
    public String getBbTitle()             { return bbTitle; }
    public String getBbColor()             { return bbColor; }
    public String getBbOverlay()           { return bbOverlay; }
    public String getBbAudience()          { return bbAudience; }
    public int    getBbDurationTicks()     { return bbDurationTicks; }
    public int    getBbFadeInTicks()       { return bbFadeInTicks; }
    public int    getBbFadeOutTicks()      { return bbFadeOutTicks; }

    // PHRASE
    public String                    getPhraseAudience()  { return phraseAudience; }
    public List<Map<String, Object>> getPhraseSteps()     { return phraseSteps; }
    public int                       getEditingStepIndex(){ return editingStepIndex; }

    // -----------------------------------------------------------------------
    // Param handlers
    // -----------------------------------------------------------------------

    private boolean handleMessageParam(String key, String value) {
        switch (key) {
            case "audience" -> {
                if (!AUDIENCES.contains(value)) {
                    sendError("Unknown audience: " + value);
                    return true;
                }
                msgAudience = value;
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "message" -> {
                if (value.isBlank()) return false;
                msgText = value.trim();
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
        }
        return false;
    }

    private boolean handleTitleParam(String key, String value) {
        switch (key) {
            case "audience" -> {
                if (!AUDIENCES.contains(value)) { sendError("Unknown audience: " + value); return true; }
                titleAudience = value;
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "title_text" -> {
                if (value.isBlank()) return false;
                titleText = value.trim();
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "subtitle_text" -> {
                titleSubtitle = value.trim();   // empty string is valid (clears subtitle)
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "fade_in_up"   -> { titleFadeIn = Math.min(titleFadeIn + 5, 200);  VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_in_down" -> { titleFadeIn = Math.max(0, titleFadeIn - 5);     VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_in"      -> { titleFadeIn = parseNonNegInt(value, titleFadeIn); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "stay_up"      -> { titleStay = Math.min(titleStay + 5, 600);      VoicePanelBuilder.sendPanel(player, this); return true; }
            case "stay_down"    -> { titleStay = Math.max(1, titleStay - 5);         VoicePanelBuilder.sendPanel(player, this); return true; }
            case "stay"         -> { titleStay = parsePositiveInt(value, titleStay); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_out_up"  -> { titleFadeOut = Math.min(titleFadeOut + 5, 200); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_out_down"-> { titleFadeOut = Math.max(0, titleFadeOut - 5);   VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_out"     -> { titleFadeOut = parseNonNegInt(value, titleFadeOut); VoicePanelBuilder.sendPanel(player, this); return true; }
        }
        return false;
    }

    private boolean handleActionBarParam(String key, String value) {
        switch (key) {
            case "audience" -> {
                if (!AUDIENCES.contains(value)) { sendError("Unknown audience: " + value); return true; }
                abAudience = value;
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "message" -> {
                if (value.isBlank()) return false;
                abText = value.trim();
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "dur_up"          -> { abDurationTicks = Math.min(abDurationTicks + 20, 6000); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "dur_down"        -> { abDurationTicks = Math.max(20, abDurationTicks - 20);   VoicePanelBuilder.sendPanel(player, this); return true; }
            case "duration_ticks"  -> { abDurationTicks = parsePositiveInt(value, abDurationTicks); VoicePanelBuilder.sendPanel(player, this); return true; }
        }
        return false;
    }

    private boolean handleBossbarParam(String key, String value) {
        switch (key) {
            case "title_text" -> {
                if (value.isBlank()) return false;
                bbTitle = value.trim();
                refreshBossBarPreview();
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "color" -> {
                if (!BOSSBAR_COLORS.contains(value.toUpperCase())) {
                    sendError("Unknown color: " + value + ". Valid: " + String.join(", ", BOSSBAR_COLORS));
                    return true;
                }
                bbColor = value.toUpperCase();
                refreshBossBarPreview();
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "overlay" -> {
                if (!BOSSBAR_OVERLAYS.contains(value.toUpperCase())) {
                    sendError("Unknown overlay: " + value + ". Valid: " + String.join(", ", BOSSBAR_OVERLAYS));
                    return true;
                }
                bbOverlay = value.toUpperCase();
                refreshBossBarPreview();
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "audience" -> {
                if (!AUDIENCES.contains(value)) { sendError("Unknown audience: " + value); return true; }
                bbAudience = value;
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "dur_up"          -> { bbDurationTicks = Math.min(bbDurationTicks + 20, 6000); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "dur_down"        -> { bbDurationTicks = Math.max(20, bbDurationTicks - 20);   VoicePanelBuilder.sendPanel(player, this); return true; }
            case "duration_ticks"  -> { bbDurationTicks = parsePositiveInt(value, bbDurationTicks); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fadein_up"       -> { bbFadeInTicks = Math.min(bbFadeInTicks + 5, bbDurationTicks); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fadein_down"     -> { bbFadeInTicks = Math.max(0, bbFadeInTicks - 5);              VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_in_ticks"   -> { bbFadeInTicks = parseNonNegInt(value, bbFadeInTicks); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fadeout_up"      -> { bbFadeOutTicks = Math.min(bbFadeOutTicks + 5, bbDurationTicks); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fadeout_down"    -> { bbFadeOutTicks = Math.max(0, bbFadeOutTicks - 5);               VoicePanelBuilder.sendPanel(player, this); return true; }
            case "fade_out_ticks"  -> { bbFadeOutTicks = parseNonNegInt(value, bbFadeOutTicks); VoicePanelBuilder.sendPanel(player, this); return true; }
            case "preview_voice"   -> { refreshBossBarPreview(); return true; }
        }
        return false;
    }

    private boolean handlePhraseParam(String key, String value) {
        switch (key) {
            case "phrase_audience" -> {
                if (!AUDIENCES.contains(value)) { sendError("Unknown audience: " + value); return true; }
                phraseAudience = value;
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "step_add" -> {
                Map<String, Object> newStep = new LinkedHashMap<>();
                // Default: MESSAGE step with "after: 40" (natural dialogue breathing)
                if (phraseSteps.isEmpty()) {
                    newStep.put("at", 0);
                } else {
                    newStep.put("after", 40);
                }
                Map<String, Object> msgEvent = new LinkedHashMap<>();
                msgEvent.put("type", "MESSAGE");
                msgEvent.put("message", "");
                List<Map<String, Object>> evList = new ArrayList<>();
                evList.add(msgEvent);
                newStep.put("events", evList);
                phraseSteps.add(newStep);
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "step_delete" -> {
                int i = parseIndex(value);
                if (i >= 0 && i < phraseSteps.size()) {
                    phraseSteps.remove(i);
                    VoicePanelBuilder.sendPanel(player, this);
                } else {
                    sendError("Step index out of range: " + value);
                }
                return true;
            }
            case "step_up" -> {
                int i = parseIndex(value);
                if (i > 0 && i < phraseSteps.size()) {
                    Map<String, Object> moved = phraseSteps.remove(i);
                    phraseSteps.add(i - 1, moved);
                    VoicePanelBuilder.sendPanel(player, this);
                }
                return true;
            }
            case "step_down" -> {
                int i = parseIndex(value);
                if (i >= 0 && i < phraseSteps.size() - 1) {
                    Map<String, Object> moved = phraseSteps.remove(i);
                    phraseSteps.add(i + 1, moved);
                    VoicePanelBuilder.sendPanel(player, this);
                }
                return true;
            }
            case "step_edit" -> {
                int i = parseIndex(value);
                if (i >= 0 && i < phraseSteps.size()) {
                    editingStepIndex = i;
                    VoicePanelBuilder.sendPhraseLinePanel(player, this, i);
                } else {
                    sendError("Step index out of range: " + value);
                }
                return true;
            }
            case "preview_phrase" -> {
                previewPhrase();
                return true;
            }
        }
        return false;
    }

    /** Handle params when a PHRASE step is open for inline editing. */
    @SuppressWarnings("unchecked")
    private boolean handleStepParam(String key, String value) {
        if (editingStepIndex < 0 || editingStepIndex >= phraseSteps.size()) {
            editingStepIndex = -1;
            VoicePanelBuilder.sendPanel(player, this);
            return true;
        }
        Map<String, Object> step = phraseSteps.get(editingStepIndex);
        List<Map<String, Object>> events = (List<Map<String, Object>>) step.get("events");
        if (events == null || events.isEmpty()) {
            events = new ArrayList<>();
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "MESSAGE");
            e.put("message", "");
            events.add(e);
            step.put("events", events);
        }
        Map<String, Object> evt = events.get(0);

        switch (key) {
            case "step_done" -> {
                editingStepIndex = -1;
                VoicePanelBuilder.sendPanel(player, this);
                return true;
            }
            case "step_type" -> {
                String t = value.toUpperCase();
                if (!List.of("MESSAGE","TITLE","ACTION_BAR","BOSSBAR").contains(t)) {
                    sendError("Step type must be one of: MESSAGE, TITLE, ACTION_BAR, BOSSBAR");
                    return true;
                }
                // Reset event map to just the new type
                evt.clear();
                evt.put("type", t);
                switch (t) {
                    case "MESSAGE"    -> { evt.put("message", ""); evt.put("audience", "participants"); }
                    case "TITLE"      -> { evt.put("title", ""); evt.put("fade_in", 10); evt.put("stay", 40); evt.put("fade_out", 10); evt.put("audience", "participants"); }
                    case "ACTION_BAR" -> { evt.put("message", ""); evt.put("duration_ticks", 60); evt.put("audience", "participants"); }
                    case "BOSSBAR"    -> { evt.put("title", ""); evt.put("color", "YELLOW"); evt.put("overlay", "PROGRESS"); evt.put("duration_ticks", 200); evt.put("fade_in_ticks", 10); evt.put("fade_out_ticks", 20); evt.put("audience", "participants"); }
                }
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_text" -> {
                if (value.isBlank()) return false;
                String evtType = (String) evt.getOrDefault("type", "MESSAGE");
                if (evtType.equals("TITLE")) {
                    evt.put("title", value.trim());
                } else {
                    evt.put("message", value.trim());
                }
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_subtitle" -> {
                evt.put("subtitle", value.trim());
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_audience" -> {
                if (!AUDIENCES.contains(value)) { sendError("Unknown audience: " + value); return true; }
                evt.put("audience", value);
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_timing_mode" -> {
                int currentTicks = getStepTicks(step);
                if (value.equalsIgnoreCase("after")) {
                    step.remove("at");
                    step.put("after", currentTicks);
                } else {
                    step.remove("after");
                    step.put("at", currentTicks);
                }
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_ticks_up"  -> { setStepTicks(step, getStepTicks(step) + 10); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_ticks_down"-> { setStepTicks(step, Math.max(0, getStepTicks(step) - 10)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_ticks"     -> { setStepTicks(step, parseNonNegInt(value, getStepTicks(step))); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_fade_in_up"  -> { evt.put("fade_in", clampInt((int) evt.getOrDefault("fade_in", 10) + 5, 0, 200)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_fade_in_down"-> { evt.put("fade_in", clampInt((int) evt.getOrDefault("fade_in", 10) - 5, 0, 200)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_fade_in"     -> { evt.put("fade_in", parseNonNegInt(value, (int) evt.getOrDefault("fade_in", 10))); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_stay_up"   -> { evt.put("stay", clampInt((int) evt.getOrDefault("stay", 40) + 5, 1, 600)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_stay_down" -> { evt.put("stay", clampInt((int) evt.getOrDefault("stay", 40) - 5, 1, 600)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_stay"      -> { evt.put("stay", parsePositiveInt(value, (int) evt.getOrDefault("stay", 40))); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_fade_out_up"  -> { evt.put("fade_out", clampInt((int) evt.getOrDefault("fade_out", 10) + 5, 0, 200)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_fade_out_down"-> { evt.put("fade_out", clampInt((int) evt.getOrDefault("fade_out", 10) - 5, 0, 200)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_fade_out"     -> { evt.put("fade_out", parseNonNegInt(value, (int) evt.getOrDefault("fade_out", 10))); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_color"   -> {
                if (!BOSSBAR_COLORS.contains(value.toUpperCase())) { sendError("Unknown color: " + value); return true; }
                evt.put("color", value.toUpperCase());
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_overlay" -> {
                if (!BOSSBAR_OVERLAYS.contains(value.toUpperCase())) { sendError("Unknown overlay: " + value); return true; }
                evt.put("overlay", value.toUpperCase());
                VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex);
                return true;
            }
            case "step_dur_up"         -> { evt.put("duration_ticks", clampInt((int) evt.getOrDefault("duration_ticks", 60) + 20, 1, 6000)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_dur_down"       -> { evt.put("duration_ticks", clampInt((int) evt.getOrDefault("duration_ticks", 60) - 20, 1, 6000)); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
            case "step_duration_ticks" -> { evt.put("duration_ticks", parsePositiveInt(value, (int) evt.getOrDefault("duration_ticks", 60))); VoicePanelBuilder.sendPhraseLinePanel(player, this, editingStepIndex); return true; }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Preview
    // -----------------------------------------------------------------------

    private void firePreview() {
        switch (mode) {
            case MESSAGE    -> player.sendMessage(MM.deserialize(msgText.isEmpty() ? "(empty)" : msgText));
            case TITLE      -> {
                Component titleComp    = MM.deserialize(titleText.isEmpty() ? "(empty)" : titleText);
                Component subtitleComp = titleSubtitle.isEmpty() ? Component.empty() : MM.deserialize(titleSubtitle);
                player.showTitle(Title.title(titleComp, subtitleComp,
                    Title.Times.times(
                        Duration.ofMillis(titleFadeIn * 50L),
                        Duration.ofMillis(titleStay   * 50L),
                        Duration.ofMillis(titleFadeOut * 50L))));
            }
            case ACTION_BAR -> player.sendActionBar(MM.deserialize(abText.isEmpty() ? "(empty)" : abText));
            case BOSSBAR    -> refreshBossBarPreview();
            case PHRASE     -> previewPhrase();
        }
    }

    private void refreshBossBarPreview() {
        hideBossBarPreview();
        if (bbTitle.isEmpty()) return;
        BossBar.Color  color;
        BossBar.Overlay overlay;
        try {
            color   = BossBar.Color.valueOf(bbColor);
            overlay = BossBar.Overlay.valueOf(bbOverlay);
        } catch (IllegalArgumentException e) {
            color   = BossBar.Color.YELLOW;
            overlay = BossBar.Overlay.PROGRESS;
        }
        previewBossBar = BossBar.bossBar(MM.deserialize(bbTitle), 0.5f, color, overlay);
        player.showBossBar(previewBossBar);
    }

    private void hideBossBarPreview() {
        if (previewBossBar != null) {
            player.hideBossBar(previewBossBar);
            previewBossBar = null;
        }
    }

    /** Send each PHRASE step as a flat chat preview so the designer can read the script. */
    @SuppressWarnings("unchecked")
    private void previewPhrase() {
        if (phraseSteps.isEmpty()) {
            player.sendMessage(MM.deserialize("<gray>(phrase is empty)</gray>"));
            return;
        }
        player.sendMessage(MM.deserialize("<gold>── PHRASE preview ──</gold>"));
        for (int i = 0; i < phraseSteps.size(); i++) {
            Map<String, Object> step = phraseSteps.get(i);
            boolean isAfter = step.containsKey("after");
            int ticks = isAfter ? intFromMap(step, "after", 0) : intFromMap(step, "at", 0);
            String timingStr = isAfter ? "after " + ticks + "t" : "at " + ticks + "t";

            List<Map<String, Object>> evs = (List<Map<String, Object>>) step.get("events");
            String text = "(empty)";
            String type = "?";
            if (evs != null && !evs.isEmpty()) {
                Map<String, Object> ev = evs.get(0);
                type = (String) ev.getOrDefault("type", "?");
                // Extract content for display
                if (ev.containsKey("message")) {
                    text = (String) ev.get("message");
                } else if (ev.containsKey("title")) {
                    text = (String) ev.get("title");
                }
            }

            String preview = text.isEmpty() ? "(no text)" : truncate(stripFormatCodes(text), 40);
            player.sendMessage(MM.deserialize(
                "<gray>" + (i + 1) + ". [" + timingStr + "]</gray>"
                + "  <aqua>" + type + "</aqua>"
                + "  <white>" + preview + "</white>"));
        }
    }

    // -----------------------------------------------------------------------
    // Write to editor
    // -----------------------------------------------------------------------

    private void writeToEditor() {
        Map<String, Object> eventMap = buildEventMap();
        editor.setCueEvents(cueId, List.of(eventMap));
        cueSession.markCueDirty(cueId);
        log.info("[VoiceEdit] Saved cue override for " + cueId + " → mode=" + mode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildEventMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("at", 0);
        switch (mode) {
            case MESSAGE -> {
                m.put("type", "MESSAGE");
                m.put("audience", msgAudience);
                m.put("message", msgText);
            }
            case TITLE -> {
                m.put("type", "TITLE");
                m.put("audience", titleAudience);
                m.put("title", titleText);
                if (!titleSubtitle.isEmpty()) m.put("subtitle", titleSubtitle);
                m.put("fade_in", titleFadeIn);
                m.put("stay",    titleStay);
                m.put("fade_out", titleFadeOut);
            }
            case ACTION_BAR -> {
                m.put("type", "ACTION_BAR");
                m.put("audience", abAudience);
                m.put("message", abText);
                m.put("duration_ticks", abDurationTicks);
            }
            case BOSSBAR -> {
                m.put("type", "BOSSBAR");
                m.put("title", bbTitle);
                m.put("color", bbColor);
                m.put("overlay", bbOverlay);
                m.put("audience", bbAudience);
                m.put("duration_ticks", bbDurationTicks);
                m.put("fade_in_ticks",  bbFadeInTicks);
                m.put("fade_out_ticks", bbFadeOutTicks);
            }
            case PHRASE -> {
                m.put("type", "PHRASE");
                m.put("audience", phraseAudience);
                // Deep-copy steps so the saved map is independent of live state
                List<Map<String, Object>> stepsCopy = new ArrayList<>();
                for (Map<String, Object> step : phraseSteps) {
                    Map<String, Object> stepCopy = new LinkedHashMap<>(step);
                    Object evs = step.get("events");
                    if (evs instanceof List<?> evList) {
                        List<Map<String, Object>> evCopies = new ArrayList<>();
                        for (Object ev : evList) {
                            if (ev instanceof Map<?, ?> evMap) {
                                evCopies.add(new LinkedHashMap<>((Map<String, Object>) evMap));
                            }
                        }
                        stepCopy.put("events", evCopies);
                    }
                    stepsCopy.add(stepCopy);
                }
                m.put("steps", stepsCopy);
            }
        }
        return m;
    }

    // -----------------------------------------------------------------------
    // Registry scan
    // -----------------------------------------------------------------------

    /** Find the first MESSAGE, TITLE, ACTION_BAR, BOSSBAR, or PHRASE event in the cue. */
    private ShowEvent findVoiceEvent(String id) {
        Cue cue = cueRegistry.get(id);
        if (cue == null) return null;
        for (ShowEvent event : cue.timeline) {
            switch (event.type()) {
                case MESSAGE, TITLE, ACTION_BAR, BOSSBAR, PHRASE -> { return event; }
                default -> {}
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // PHRASE step helpers
    // -----------------------------------------------------------------------

    private static int getStepTicks(Map<String, Object> step) {
        if (step.containsKey("after")) return intFromMap(step, "after", 0);
        return intFromMap(step, "at", 0);
    }

    private static void setStepTicks(Map<String, Object> step, int ticks) {
        if (step.containsKey("after")) step.put("after", ticks);
        else step.put("at", ticks);
    }

    /**
     * Deep-copy a list of step maps so that Cancel can restore independently of live edits.
     * Each step map's "events" list is also deep-copied.
     */
    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> deepCopySteps(List<Map<String, Object>> steps) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> step : steps) {
            Map<String, Object> sCopy = new LinkedHashMap<>(step);
            Object evs = step.get("events");
            if (evs instanceof List<?> evList) {
                List<Map<String, Object>> evCopies = new ArrayList<>();
                for (Object ev : evList) {
                    if (ev instanceof Map<?, ?> evMap) {
                        evCopies.add(new LinkedHashMap<>((Map<String, Object>) evMap));
                    }
                }
                sCopy.put("events", evCopies);
            }
            copy.add(sCopy);
        }
        return copy;
    }

    // -----------------------------------------------------------------------
    // Numeric helpers
    // -----------------------------------------------------------------------

    private static int parseNonNegInt(String s, int fallback) {
        try { return Math.max(0, Integer.parseInt(s.trim())); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static int parsePositiveInt(String s, int fallback) {
        try { return Math.max(1, Integer.parseInt(s.trim())); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static int parseIndex(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int intFromMap(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return (v instanceof Number n) ? n.intValue() : def;
    }

    /** Strip & color codes from a string for plain-text display. */
    private static String stripFormatCodes(String s) {
        return s.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("<[^>]+>", "");
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    private void sendError(String msg) {
        player.sendMessage(MM.deserialize("<red>" + msg + "</red>"));
    }
}
