package com.scaena.shows.model.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** §6.1 — MESSAGE, TITLE, ACTION_BAR, BOSSBAR, BOSS_HEALTH_BAR */
public final class TextEvents {

    private TextEvents() {}

    // ------------------------------------------------------------------
    // MESSAGE
    // ------------------------------------------------------------------
    public static final class MessageEvent extends ShowEvent {
        public final String audience;
        public final String message;

        public MessageEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience = str(m, "audience", "broadcast");
            this.message  = str(m, "message", "");
        }

        @Override public EventType type() { return EventType.MESSAGE; }
    }

    // ------------------------------------------------------------------
    // TITLE
    // ------------------------------------------------------------------
    public static final class TitleEvent extends ShowEvent {
        public final String audience;
        public final String title;
        public final String subtitle;
        public final int fadeIn;
        public final int stay;
        public final int fadeOut;

        public TitleEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience = str(m, "audience", "broadcast");
            this.title    = str(m, "title", "");
            this.subtitle = str(m, "subtitle", "");
            this.fadeIn   = intVal(m, "fade_in", 10);
            this.stay     = intVal(m, "stay", 40);
            this.fadeOut  = intVal(m, "fade_out", 10);
        }

        @Override public EventType type() { return EventType.TITLE; }
    }

    // ------------------------------------------------------------------
    // TITLE_CLEAR — instant dismiss with optional fade-out; no pop
    // ------------------------------------------------------------------
    public static final class TitleClearEvent extends ShowEvent {
        public final String audience;
        public final int fadeOut;

        public TitleClearEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience = str(m, "audience", "broadcast");
            this.fadeOut  = intVal(m, "fade_out", 10);
        }

        @Override public EventType type() { return EventType.TITLE_CLEAR; }
    }

    // ------------------------------------------------------------------
    // ACTION_BAR
    // ------------------------------------------------------------------
    public static final class ActionBarEvent extends ShowEvent {
        public final String audience;
        public final String message;
        public final int durationTicks;

        public ActionBarEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.audience      = str(m, "audience", "broadcast");
            this.message       = str(m, "message", "");
            this.durationTicks = intVal(m, "duration_ticks", 60);
        }

        @Override public EventType type() { return EventType.ACTION_BAR; }
    }

    // ------------------------------------------------------------------
    // BOSSBAR (inline timeline event)
    // ------------------------------------------------------------------
    public static final class BossbarEvent extends ShowEvent {
        public final String title;
        public final String color;
        public final String overlay;
        public final String audience;
        public final int durationTicks;
        public final int fadeInTicks;
        public final int fadeOutTicks;

        public BossbarEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.title         = str(m, "title", "");
            this.color         = str(m, "color", "WHITE");
            this.overlay       = str(m, "overlay", "PROGRESS");
            this.audience      = str(m, "audience", "broadcast");
            this.durationTicks = intVal(m, "duration_ticks", 200);
            this.fadeInTicks   = intVal(m, "fade_in_ticks", 10);
            this.fadeOutTicks  = intVal(m, "fade_out_ticks", 20);
        }

        @Override public EventType type() { return EventType.BOSSBAR; }
    }

    // ------------------------------------------------------------------
    // BOSS_HEALTH_BAR  (OPS-026)
    // Entity-linked bossbar. Progress updates live via EntityCombatListener.
    // On entity death: fires death_line to participants, then injects victory_cue.
    // ------------------------------------------------------------------
    public static final class BossHealthBarEvent extends ShowEvent {
        public final String target;               // entity:spawned:<name>
        public final String title;                // bossbar display title (MiniMessage)
        public final String color;                // BossBar.Color name (e.g. "RED")
        public final String overlay;              // BossBar.Overlay name (e.g. "PROGRESS")
        public final String audience;             // audience string (e.g. "participants")
        public final String deathLine;            // optional — text sent on entity death
        public final String deathLineColor;       // hex color for death line (e.g. "#CC2200")
        public final int    deathLinePauseTicks;  // ticks between death line and victory cue
        public final String victoryCue;           // optional — cue ID injected after death

        public BossHealthBarEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target              = str(m, "target", "");
            this.title               = str(m, "title", "Boss");
            this.color               = str(m, "color", "RED");
            this.overlay             = str(m, "overlay", "PROGRESS");
            this.audience            = str(m, "audience", "participants");
            this.deathLine           = str(m, "death_line", "");
            this.deathLineColor      = str(m, "death_line_color", "#FFFFFF");
            this.deathLinePauseTicks = intVal(m, "death_line_pause_ticks", 20);
            this.victoryCue          = str(m, "victory_cue", "");
        }

        @Override public EventType type() { return EventType.BOSS_HEALTH_BAR; }
    }

    // ------------------------------------------------------------------
    // PLAYER_CHOICE — hard-fork branching event
    // ------------------------------------------------------------------

    /** A single branch option: the label shown in chat and the cue to fire if chosen. */
    public record ChoiceOption(String label, String cueId) {}

    /**
     * Suspends show execution and presents a branching prompt to participants.
     *
     * Stop is always auto-injected as the final option — never authored.
     * The first participant click resolves the choice for the whole show.
     * On timeout, the option at index {@code defaultOption} is auto-selected.
     */
    public static final class PlayerChoiceEvent extends ShowEvent {
        public final String             prompt;
        public final List<ChoiceOption> options;
        public final int                defaultOption;  // 0-indexed
        public final int                timeoutTicks;   // 0 = no timeout
        public final String             waitingSound;

        @SuppressWarnings("unchecked")
        public PlayerChoiceEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.prompt        = str(m, "prompt", "What do you do?");
            this.defaultOption = intVal(m, "default", 0);
            this.timeoutTicks  = intVal(m, "timeout_ticks", 300);
            this.waitingSound  = str(m, "waiting_sound", "block.note_block.chime");

            List<ChoiceOption> parsed = new ArrayList<>();
            Object rawOpts = m.get("options");
            if (rawOpts instanceof List<?> list) {
                for (Object item : list) {
                    if (!(item instanceof Map<?, ?> rawMap)) continue;
                    Map<String, Object> opt = (Map<String, Object>) rawMap;
                    String label = str(opt, "label", "?");
                    String cue   = str(opt, "cue", "");
                    if (!cue.isEmpty()) parsed.add(new ChoiceOption(label, cue));
                }
            }
            this.options = List.copyOf(parsed);
        }

        @Override public EventType type() { return EventType.PLAYER_CHOICE; }
    }
}
