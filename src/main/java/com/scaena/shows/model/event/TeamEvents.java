package com.scaena.shows.model.event;

import java.util.Map;

/** §6.4 — GROUP_ASSIGN, TEAM_COLOR, GROUP_EVENT */
public final class TeamEvents {

    private TeamEvents() {}

    // ------------------------------------------------------------------
    // GROUP_ASSIGN — uses show-level group_count and group_strategy.
    // Always placed at tick 0. Dispatched first at any tick.
    // ------------------------------------------------------------------
    public static final class GroupAssignEvent extends ShowEvent {
        public GroupAssignEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
        }

        @Override public EventType type() { return EventType.GROUP_ASSIGN; }
    }

    // ------------------------------------------------------------------
    // TEAM_COLOR
    // ------------------------------------------------------------------
    public static final class TeamColorEvent extends ShowEvent {
        public final String target; // group_1..4 | broadcast
        public final String color;

        public TeamColorEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target = str(m, "target", "broadcast");
            this.color  = str(m, "color", "WHITE");
        }

        @Override public EventType type() { return EventType.TEAM_COLOR; }
    }

    // ------------------------------------------------------------------
    // GROUP_EVENT — fires a named Cue scoped to a specific player group.
    // Audience resolution inside the Cue is restricted to the target group's
    // members for the duration of this invocation.
    // If the target group has zero members, this is a silent skip.
    // ------------------------------------------------------------------
    public static final class GroupEvent extends ShowEvent {
        public final String target; // group_1 | group_2 | group_3 | group_4
        public final String cueId;  // must exist in CueRegistry

        public GroupEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.target = str(m, "target", "group_1");
            this.cueId  = str(m, "cue_id", "");
        }

        @Override public EventType type() { return EventType.GROUP_EVENT; }
    }
}
