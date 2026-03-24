package com.scaena.shows.model.event;

import java.util.Map;

/** §6.11 — REST, COMMAND, CUE (child-cue reference) */
public final class UtilityEvents {

    private UtilityEvents() {}

    // ------------------------------------------------------------------
    // REST — silent bar; holds timeline time with no effect
    // ------------------------------------------------------------------
    public static final class RestEvent extends ShowEvent {
        public final int durationTicks;
        public final String name;  // optional label

        public RestEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.durationTicks = intVal(m, "duration_ticks", 20);
            this.name          = str(m, "name", "");
        }

        @Override public EventType type() { return EventType.REST; }
    }

    // ------------------------------------------------------------------
    // COMMAND — raw server command escape hatch
    // ------------------------------------------------------------------
    public static final class CommandEvent extends ShowEvent {
        public final String command;

        public CommandEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.command = str(m, "command", "");
        }

        @Override public EventType type() { return EventType.COMMAND; }
    }

    // ------------------------------------------------------------------
    // CUE — reference to a child Cue by ID.
    // Resolved at load time; fails fast if the ID is unknown.
    // ------------------------------------------------------------------
    public static final class CueRefEvent extends ShowEvent {
        public final String cueId;

        public CueRefEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.cueId = str(m, "cue_id", "");
        }

        @Override public EventType type() { return EventType.CUE; }
    }
}
