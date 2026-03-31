package com.scaena.shows.scout;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

/**
 * Holds the state of a single player's active scouting session.
 * Created when /scaena scout load succeeds; removed on dismiss/disconnect.
 *
 * Tracks:
 *  - The show and scene being scouted (activeScene = the tag filter used)
 *  - Objectives in scope (filtered by scene tag at load time)
 *  - Captures made this session (name → capture, insertion-ordered)
 *  - Prior captures loaded from the most recent save file (for markers + goto)
 *  - In-world TextDisplay markers keyed by objective name (auto-replace on recapture)
 *  - Pre-session flight state for clean restoration on dismiss
 */
public final class ScoutSession {

    private final String showId;
    private final String activeScene;                         // tag filter used to load; null = no filter
    private final List<ScoutObjective> objectives;            // ordered for this session
    private final Map<String, ScoutCapture> captures;         // captured this session
    private final Map<String, ScoutCapture> priorCaptures;    // from most recent save file
    private final Scoreboard previousScoreboard;
    private final Scoreboard scoutScoreboard;
    private final boolean priorAllowFlight;
    private final boolean priorFlying;

    /** In-world markers keyed by objective name — auto-replaces on recapture. */
    private final Map<String, TextDisplay> markersByName = new LinkedHashMap<>();
    private boolean markersVisible = true;

    /** Snapshot log — all /scaena snap entries this session, in order. */
    private final List<SnapshotEntry> snapshots = new ArrayList<>();

    public ScoutSession(
        String showId,
        String activeScene,
        List<ScoutObjective> objectives,
        Map<String, ScoutCapture> priorCaptures,
        Scoreboard previousScoreboard,
        Scoreboard scoutScoreboard,
        boolean priorAllowFlight,
        boolean priorFlying
    ) {
        this.showId             = showId;
        this.activeScene        = activeScene;
        this.objectives         = List.copyOf(objectives);
        this.priorCaptures      = Collections.unmodifiableMap(new LinkedHashMap<>(priorCaptures));
        this.captures           = new LinkedHashMap<>();
        this.previousScoreboard = previousScoreboard;
        this.scoutScoreboard    = scoutScoreboard;
        this.priorAllowFlight   = priorAllowFlight;
        this.priorFlying        = priorFlying;
    }

    // ---- Accessors ----

    public String showId()                          { return showId; }
    public String activeScene()                     { return activeScene; }
    public List<ScoutObjective> objectives()        { return objectives; }
    public Map<String, ScoutCapture> captures()     { return captures; }
    public Map<String, ScoutCapture> priorCaptures(){ return priorCaptures; }
    public Scoreboard previousScoreboard()          { return previousScoreboard; }
    public Scoreboard scoutScoreboard()             { return scoutScoreboard; }
    public boolean priorAllowFlight()               { return priorAllowFlight; }
    public boolean priorFlying()                    { return priorFlying; }
    public boolean markersVisible()                 { return markersVisible; }
    public void setMarkersVisible(boolean v)        { this.markersVisible = v; }
    public List<SnapshotEntry> snapshots()          { return Collections.unmodifiableList(snapshots); }
    public void addSnapshot(SnapshotEntry entry)    { snapshots.add(entry); }

    // ---- Objective lookup ----

    /** Find an objective by short code (e.g. "1.3"). Null if not found. */
    public ScoutObjective findByCode(String code) {
        return objectives.stream()
            .filter(o -> o.code().equals(code))
            .findFirst()
            .orElse(null);
    }

    // ---- Capture management ----

    /** Record a position capture for an objective. Overwrites any prior capture for the same name. */
    public void addCapture(ScoutCapture capture) {
        captures.put(capture.name(), capture);
    }

    /**
     * Returns the best available capture for a given objective name.
     * Current-session captures take precedence over prior captures.
     */
    public ScoutCapture findCapture(String name) {
        ScoutCapture current = captures.get(name);
        return current != null ? current : priorCaptures.get(name);
    }

    /**
     * Find a capture by objective code. Checks current session first, then prior captures.
     * Returns null if the code is unknown or neither source has a capture.
     */
    public ScoutCapture findCaptureByCode(String code) {
        ScoutObjective obj = findByCode(code);
        return obj != null ? findCapture(obj.name()) : null;
    }

    /** How many of this session's objectives have a current-session capture. */
    public int capturedCount() {
        return (int) objectives.stream()
            .filter(o -> captures.containsKey(o.name()))
            .count();
    }

    /** True if every objective in this session has a current-session capture. */
    public boolean allCaptured() {
        return capturedCount() == objectives.size();
    }

    // ---- Marker management ----

    /**
     * Register a marker for an objective name. If a marker already exists for that
     * name (e.g. replacing a prior-capture marker after a fresh capture), the old
     * entity is removed from the world before the new one is recorded.
     */
    public void addMarker(String objectiveName, TextDisplay display) {
        TextDisplay old = markersByName.put(objectiveName, display);
        if (old != null) old.remove();
    }

    /** Remove all tracked markers from the world and clear the registry. */
    public void despawnMarkers() {
        markersByName.values().forEach(Entity::remove);
        markersByName.clear();
    }

    /** How many markers are currently registered. */
    public int markerCount() { return markersByName.size(); }
}
