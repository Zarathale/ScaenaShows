package com.scaena.shows.scout;

/**
 * A captured world position for a named mark.
 * Produced by /scaena set <code> and written to the capture output file.
 */
public record ScoutCapture(
    String name,
    String world,
    double x,
    double y,
    double z,
    float  yaw,
    float  pitch
) {}
