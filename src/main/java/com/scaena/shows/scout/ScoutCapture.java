package com.scaena.shows.scout;

/**
 * A captured world position for a named mark, with an environmental snapshot
 * taken at capture time. Produced by /scaena set <code>.
 *
 * Environmental fields eliminate the need to manually record biome, light levels,
 * and sky clearance from the F3 screen — they are captured automatically alongside
 * the coordinates.
 *
 * For block-targeted marks (tag: blocks), the block_x/y/z fields in the YAML output
 * give the derived block coordinates (rounded from player position). Confirm exact
 * block in-game before YAML authoring.
 */
public record ScoutCapture(
    String name,
    String world,
    double x,
    double y,
    double z,
    float  yaw,
    float  pitch,

    // ---- Environmental snapshot ----
    String biome,           // e.g. "SWAMP", "BADLANDS", "WINDSWEPT_HILLS"
    int    lightLevel,      // combined effective light (0–15)
    int    skyLight,        // sky-sourced light component (0–15)
    int    blockLight,      // block-emitted light component (0–15)
    int    ceilingHeight,   // blocks from player head to first solid block above; -1 = open sky
    String skyType          // "open_sky" | "partial" | "enclosed" | "underground"
) {}
