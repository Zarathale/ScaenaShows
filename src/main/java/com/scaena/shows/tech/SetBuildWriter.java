package com.scaena.shows.tech;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Writes a SetBuildSession to a dated YAML file under
 * plugins/ScaenaShows/set_builds/[showId]/[stem].yml
 *
 * File stem format: [sceneId]_[yyyyMMdd_HHmmss]  e.g. site_a_20260401_143022
 *
 * YAML format:
 *
 *   show: showcase.01
 *   scene: site_a
 *   version: site_a_20260401_143022
 *   recorded: 2026-04-01T14:30:22
 *   changes:
 *     - loc: [world, 198, 79, 304]
 *       before: "minecraft:air"
 *       after: "minecraft:blast_furnace[facing=north,lit=true]"
 *
 * Only net changes (before != after) are written.
 *
 * Returns the file stem so callers can update the active_build pointer.
 */
public final class SetBuildWriter {

    private static final DateTimeFormatter STAMP_FMT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter ISO_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Logger log;

    public SetBuildWriter(Logger log) {
        this.log = log;
    }

    /**
     * Write the build session to disk.
     *
     * @param buildDir  plugins/ScaenaShows/set_builds/[showId]/
     * @param showId    e.g. "showcase.01"
     * @param session   the completed SetBuildSession
     * @return the file stem (without .yml extension), or null on failure
     */
    public String write(File buildDir, String showId, SetBuildSession session) {
        if (session.isEmpty()) {
            log.info("[SetBuild] Nothing to write — no net changes in session.");
            return null;
        }

        buildDir.mkdirs();

        String stamp = LocalDateTime.now().format(STAMP_FMT);
        String stem  = session.sceneId() + "_" + stamp;
        File   out   = uniqueFile(buildDir, stem);

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))) {

            pw.println("# Set build — " + showId + " / " + session.sceneId());
            pw.println("# Recorded by Tech Rehearsal Mode (BUILD MODE → SAVE).");
            pw.println("# before: the world state before this session.");
            pw.println("# after:  the desired state when the show's APPLY_SET cue fires.");
            pw.println("show: "    + showId);
            pw.println("scene: "   + session.sceneId());
            pw.println("version: " + stem);
            pw.println("recorded: " + LocalDateTime.now().format(ISO_FMT));
            pw.println("changes:");

            for (SetBuildSession.BlockChange c : session.netChanges()) {
                pw.printf("  - loc: [%s, %d, %d, %d]%n",
                    c.world().getName(), c.x(), c.y(), c.z());
                pw.printf("    before: \"%s\"%n", c.before().getAsString());
                pw.printf("    after:  \"%s\"%n", c.after().getAsString());
            }

        } catch (IOException e) {
            log.severe("[SetBuild] Failed to write set build file: " + e.getMessage());
            return null;
        }

        log.info("[SetBuild] Wrote " + session.netChanges().size()
            + " change(s) to " + out.getName());
        return stem;
    }

    /**
     * List all saved version stems for a scene, sorted oldest-first.
     */
    public static java.util.List<String> listVersions(File buildDir, String sceneId) {
        File[] files = buildDir.listFiles((d, n) -> n.startsWith(sceneId + "_") && n.endsWith(".yml"));
        if (files == null) return java.util.List.of();
        java.util.Arrays.sort(files);
        java.util.List<String> stems = new java.util.ArrayList<>();
        for (File f : files) stems.add(f.getName().replace(".yml", ""));
        return stems;
    }

    private static File uniqueFile(File dir, String stem) {
        File f = new File(dir, stem + ".yml");
        if (!f.exists()) return f;
        int n = 2;
        while (f.exists()) {
            f = new File(dir, stem + "_" + n + ".yml");
            n++;
        }
        return f;
    }
}
