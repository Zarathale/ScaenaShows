package com.scaena.shows.model;

import com.scaena.shows.model.event.ShowEvent;

import java.util.List;
import java.util.Map;

/**
 * A Show is a Cue with additional show-level fields.
 * The plugin treats it as a runtime entry point.
 *
 * Loaded from shows/*.yml by ShowRegistry.
 */
public final class Show extends Cue {

    public final boolean portable;
    public final String defaultMode;        // follow | static
    public final String defaultAudience;    // broadcast | participants
    public final int groupCount;
    public final String groupStrategy;      // join_order | alphabetical | random
    public final String front;              // compass direction or degrees
    public final Map<String, Mark> marks;
    public final Map<String, ShowSet> sets;
    public final BossbarDef bossbar;

    public Show(
        String id,
        String name,
        String description,
        int durationTicks,
        List<String> tags,
        CueMeta meta,
        List<ShowEvent> timeline,
        boolean portable,
        String defaultMode,
        String defaultAudience,
        int groupCount,
        String groupStrategy,
        String front,
        Map<String, Mark> marks,
        Map<String, ShowSet> sets,
        BossbarDef bossbar
    ) {
        super(id, name, description, durationTicks, tags, meta, timeline);
        this.portable        = portable;
        this.defaultMode     = defaultMode;
        this.defaultAudience = defaultAudience;
        this.groupCount      = groupCount;
        this.groupStrategy   = groupStrategy;
        this.front           = front;
        this.marks           = Map.copyOf(marks);
        this.sets            = Map.copyOf(sets);
        this.bossbar         = bossbar;
    }

    @Override
    public String toString() {
        return "Show{id='" + id + "', name='" + name + "', duration=" + durationTicks + "t}";
    }
}
