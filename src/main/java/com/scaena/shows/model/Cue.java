package com.scaena.shows.model;

import com.scaena.shows.model.event.ShowEvent;

import java.util.List;

/**
 * The universal recursive container type.
 * Everything is either a Cue or a leaf event inside a Cue.
 *
 * Loaded from cues/*.yml by CueRegistry.
 * Shows extend Cue with additional show-level fields.
 */
public class Cue {

    public final String id;
    public final String name;
    public final String description;
    public final int durationTicks;
    public final List<String> tags;
    public final CueMeta meta;

    /**
     * The ordered timeline of events. Events are sorted by at-tick at load time,
     * with GROUP_ASSIGN and CAPTURE_ENTITIES guaranteed first at each tick.
     */
    public final List<ShowEvent> timeline;

    public Cue(
        String id,
        String name,
        String description,
        int durationTicks,
        List<String> tags,
        CueMeta meta,
        List<ShowEvent> timeline
    ) {
        this.id           = id;
        this.name         = name;
        this.description  = description;
        this.durationTicks= durationTicks;
        this.tags         = List.copyOf(tags);
        this.meta         = meta;
        this.timeline     = List.copyOf(timeline);
    }

    @Override
    public String toString() {
        return "Cue{id='" + id + "', duration=" + durationTicks + "t, events=" + timeline.size() + "}";
    }
}
