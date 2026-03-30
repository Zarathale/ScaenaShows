package com.scaena.shows.scout;

import org.bukkit.scoreboard.Scoreboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the state of a single player's active scouting session.
 * Created when /scaena scout load succeeds; removed on save/dismiss/disconnect.
 */
public final class ScoutSession {

    private final String showId;
    private final List<ScoutObjective> objectives;    // ordered as loaded (stable for sidebar)
    private final Map<String, ScoutCapture> captures; // name → capture, insertion-ordered
    private final Scoreboard previousScoreboard;      // restored on dismiss
    private final Scoreboard scoutScoreboard;         // the sidebar board we built

    public ScoutSession(
        String showId,
        List<ScoutObjective> objectives,
        Scoreboard previousScoreboard,
        Scoreboard scoutScoreboard
    ) {
        this.showId            = showId;
        this.objectives        = List.copyOf(objectives);
        this.captures          = new LinkedHashMap<>();
        this.previousScoreboard = previousScoreboard;
        this.scoutScoreboard   = scoutScoreboard;
    }

    public String showId()                    { return showId; }
    public List<ScoutObjective> objectives()  { return objectives; }
    public Map<String, ScoutCapture> captures(){ return captures; }
    public Scoreboard previousScoreboard()    { return previousScoreboard; }
    public Scoreboard scoutScoreboard()       { return scoutScoreboard; }

    /** Find an objective by its short code (e.g. "1.3"). Returns null if not present. */
    public ScoutObjective findByCode(String code) {
        return objectives.stream()
            .filter(o -> o.code().equals(code))
            .findFirst()
            .orElse(null);
    }

    /** Record a position capture for an objective. Overwrites any previous capture for the same name. */
    public void addCapture(ScoutCapture capture) {
        captures.put(capture.name(), capture);
    }

    /** How many objectives have been captured this session. */
    public int capturedCount() {
        return (int) objectives.stream()
            .filter(o -> captures.containsKey(o.name()))
            .count();
    }

    /** Whether every objective in this session has a capture. */
    public boolean allCaptured() {
        return capturedCount() == objectives.size();
    }
}
