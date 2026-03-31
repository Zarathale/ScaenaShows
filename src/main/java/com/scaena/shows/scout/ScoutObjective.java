package com.scaena.shows.scout;

import java.util.List;

/**
 * A single named position to be captured during a scouting session.
 * Loaded from a scout_objectives/[showId].yml file.
 *
 * The {@code note} field holds in-game hint text for the scout (e.g. positioning
 * guidance, measurement notes). It is optional — may be null. Surfaced in
 * /scaena scout status so the scout doesn't need an external reference for basics.
 */
public record ScoutObjective(String code, String name, List<String> tags, String note) {

    /**
     * Display label for the scoreboard sidebar — name with underscores
     * replaced by spaces for readability.
     */
    public String displayLabel() {
        return name.replace('_', ' ');
    }

    /**
     * Full sidebar entry string with legacy color codes.
     * Yellow code, gray name. Must be unique within the scoreboard.
     */
    public String sidebarEntry() {
        return "§e" + code + " §7" + displayLabel();
    }

    /**
     * Whether this objective matches the given tag filter.
     * A null filter matches everything.
     */
    public boolean matchesTag(String tagFilter) {
        if (tagFilter == null) return true;
        return tags.contains(tagFilter);
    }
}
