package com.scaena.shows.model.event;

import java.util.Map;

/** §6.1 — MESSAGE, TITLE, ACTION_BAR, BOSSBAR */
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
}
