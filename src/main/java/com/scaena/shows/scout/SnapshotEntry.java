package com.scaena.shows.scout;

import java.time.LocalDateTime;

/**
 * A single logged snapshot moment — created by /scaena snap [label].
 *
 * Captures the player's position and facing at the moment they ran the command
 * so the server-side log can be fuzzy-matched against client-side F2 screenshots
 * (±30 second window on timestamp).
 *
 * Retake discipline: the session accumulates all entries, including repeated labels.
 * When writing the log to disk, the last entry for each label is the keeper;
 * earlier entries for the same label are marked superseded: true.
 */
public record SnapshotEntry(
    String        label,      // freeform, e.g. "site_a_overview", "spawn_angle"
    LocalDateTime timestamp,  // logged at command time, not F2 press
    String        world,
    double        x,
    double        y,
    double        z,
    float         yaw,
    float         pitch,
    String        site        // activeScene at snap time; null if no scene filter was loaded
) {}
