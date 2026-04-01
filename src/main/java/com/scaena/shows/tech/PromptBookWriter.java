package com.scaena.shows.tech;

import org.bukkit.Location;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Writes-back tech session changes to disk.
 *
 * Two responsibilities:
 *
 *   writeParams  — updates param value: lines in [show_id].prompt-book.yml in-place,
 *                  preserving all comments and surrounding structure.
 *
 *   writeCaptures — writes a new dated file to scout_captures/[show_id]/[date].yml,
 *                   merging with the most recent existing capture file so no prior
 *                   captures are lost.
 *
 * Both methods return the count of items actually written.
 */
public final class PromptBookWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Logger log;

    public PromptBookWriter(Logger log) {
        this.log = log;
    }

    // -----------------------------------------------------------------------
    // writeParams — line-scan approach, comment-preserving
    // -----------------------------------------------------------------------

    /**
     * Updates param values in the prompt-book.yml file.
     *
     * Algorithm: scan line-by-line through the params: section. When a
     * "- name: paramName" line is found, scan forward for the next "value:"
     * line at the same indent level and replace it.
     *
     * Preserves all comments, blank lines, and unrelated structure.
     *
     * @param bookFile       the prompt-book.yml file to update
     * @param modifiedParams map of param name → new value
     * @return count of params successfully written
     */
    public int writeParams(File bookFile, Map<String, Object> modifiedParams) {
        if (modifiedParams.isEmpty()) return 0;
        if (!bookFile.exists()) {
            log.warning("[Tech] Cannot write params — file not found: " + bookFile.getPath());
            return 0;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(bookFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.severe("[Tech] Failed to read prompt book for write-back: " + e.getMessage());
            return 0;
        }

        // Work on a mutable copy
        List<String> result = new ArrayList<>(lines);
        Set<String> remaining = new LinkedHashSet<>(modifiedParams.keySet());
        int written = 0;

        boolean inParamsSection = false;
        int     paramBlockStart = -1;
        String  currentParamName = null;
        int     nameIndent       = -1;

        for (int i = 0; i < result.size(); i++) {
            String line = result.get(i);
            String trimmed = line.stripLeading();

            // Detect params: section start
            if (trimmed.startsWith("params:")) {
                inParamsSection = true;
                continue;
            }

            // Detect end of params section (new top-level key)
            if (inParamsSection && !line.isBlank() && !line.startsWith(" ")
                    && !line.startsWith("\t") && !trimmed.startsWith("-")
                    && !trimmed.startsWith("#")) {
                break; // left the params section
            }

            if (!inParamsSection) continue;
            if (remaining.isEmpty()) break;

            // Detect "  - name: paramName" line
            if (trimmed.startsWith("- name:")) {
                String namePart = trimmed.substring("- name:".length()).trim();
                // Strip inline comments
                int hashIdx = namePart.indexOf('#');
                if (hashIdx >= 0) namePart = namePart.substring(0, hashIdx).trim();
                currentParamName = namePart;
                nameIndent       = line.length() - trimmed.length();
                paramBlockStart  = i;
                continue;
            }

            // Within a param block — look for value: line
            if (currentParamName != null && remaining.contains(currentParamName)) {
                if (trimmed.startsWith("value:")) {
                    int indent = line.length() - trimmed.length();
                    // Only update value: at the same or deeper indent than the param block
                    if (indent > nameIndent) {
                        Object newVal = modifiedParams.get(currentParamName);
                        String valStr = formatValue(newVal);
                        // Preserve any inline comment
                        String suffix = "";
                        int commentIdx = findInlineComment(trimmed, "value:".length());
                        if (commentIdx >= 0) {
                            suffix = "  " + trimmed.substring(commentIdx);
                        }
                        String newLine = " ".repeat(indent) + "value: " + valStr + suffix;
                        result.set(i, newLine);
                        remaining.remove(currentParamName);
                        written++;
                        currentParamName = null;
                    }
                }
            }
        }

        if (written == 0 && !modifiedParams.isEmpty()) {
            log.warning("[Tech] No param values updated in " + bookFile.getName()
                + " — check that param names match exactly.");
        }

        // Write back
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(bookFile), StandardCharsets.UTF_8))) {
            for (String l : result) {
                pw.println(l);
            }
        } catch (IOException e) {
            log.severe("[Tech] Failed to write prompt book: " + e.getMessage());
            return 0;
        }

        if (!remaining.isEmpty()) {
            log.warning("[Tech] Could not find value: lines for params: "
                + String.join(", ", remaining));
        }

        log.info("[Tech] Wrote " + written + " param update(s) to " + bookFile.getName());
        return written;
    }

    // -----------------------------------------------------------------------
    // writeCaptures — new dated file, merged with most recent captures
    // -----------------------------------------------------------------------

    /**
     * Writes modified mark positions to a new dated capture file.
     * Merges with the most recent existing capture file so no positions are lost.
     *
     * @param captureDir    scout_captures/[show_id]/ directory
     * @param showId        e.g. "showcase.01"
     * @param modifiedMarks mark name → new Location
     * @return count of marks written
     */
    public int writeCaptures(File captureDir, String showId,
                              Map<String, Location> modifiedMarks) {
        if (modifiedMarks.isEmpty()) return 0;

        captureDir.mkdirs();

        // Read prior captures to merge
        Map<String, Location> merged = new LinkedHashMap<>();
        File latestExisting = findLatestCaptureFile(captureDir);
        if (latestExisting != null) {
            Map<String, Location> prior = readExistingCaptures(latestExisting);
            merged.putAll(prior);
        }
        // New captures overwrite prior ones for the same mark names
        merged.putAll(modifiedMarks);

        String date   = DATE_FMT.format(LocalDate.now());
        File   outFile = uniqueFile(captureDir, date);

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
            pw.println("# Scout capture — " + showId + " — " + date);
            pw.println("# Generated by Tech Rehearsal Mode (SAVE command).");
            pw.println("# Modified marks written by /scaena tech save.");
            pw.println("show: " + showId);
            pw.println("captured_at: \"" + date + "\"");
            pw.println("source: tech_mode");
            pw.println("captures:");

            for (Map.Entry<String, Location> entry : merged.entrySet()) {
                String name  = entry.getKey();
                Location loc = entry.getValue();
                if (loc == null || loc.getWorld() == null) continue;

                pw.println("  " + name + ":");
                pw.println("    world: " + loc.getWorld().getName());
                pw.printf ("    x: %.4f%n", loc.getX());
                pw.printf ("    y: %.4f%n", loc.getY());
                pw.printf ("    z: %.4f%n", loc.getZ());
                pw.printf ("    yaw: %.2f%n",   (double) loc.getYaw());
                pw.printf ("    pitch: %.2f%n", (double) loc.getPitch());
                // Derived block coords
                pw.printf ("    block_x: %d%n",  (int) Math.round(loc.getX()));
                pw.printf ("    block_y: %d  # derived — confirm in-game%n",
                    (int) Math.floor(loc.getY()) - 1);
                pw.printf ("    block_z: %d%n",  (int) Math.round(loc.getZ()));
                pw.println("    env:");
                pw.println("      biome: UNKNOWN  # captured via tech mode");
                pw.println("      light_level: 0");
                pw.println("      sky_light: 0");
                pw.println("      block_light: 0");
                pw.println("      ceiling_height: -1  # open sky");
                pw.println("      sky_type: unknown");
            }
        } catch (IOException e) {
            log.severe("[Tech] Failed to write capture file: " + e.getMessage());
            return 0;
        }

        log.info("[Tech] Wrote " + merged.size() + " capture(s) to " + outFile.getName());
        return modifiedMarks.size();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String formatValue(Object val) {
        if (val == null) return "null";
        if (val instanceof Double d) {
            if (d == Math.floor(d)) return String.valueOf(d.longValue());
            // Round to 10 decimal places to avoid floating-point noise
            return String.valueOf(Math.round(d * 1e10) / 1e10);
        }
        if (val instanceof Number n) return n.toString();
        // String value — quote it
        String s = val.toString();
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    /**
     * Find the index of an inline comment (# character) in a YAML value portion,
     * skipping over quoted strings.
     * Returns -1 if no inline comment exists.
     */
    private static int findInlineComment(String trimmed, int startAfter) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = startAfter; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '\'' && !inDoubleQuote) { inSingleQuote = !inSingleQuote; continue; }
            if (c == '"'  && !inSingleQuote) { inDoubleQuote = !inDoubleQuote; continue; }
            if (c == '#' && !inSingleQuote && !inDoubleQuote) return i;
        }
        return -1;
    }

    private static File findLatestCaptureFile(File dir) {
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) return null;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        return files[files.length - 1];
    }

    /** Read locations from an existing capture file (best-effort; env fields ignored). */
    @SuppressWarnings("unchecked")
    private Map<String, Location> readExistingCaptures(File file) {
        Map<String, Location> result = new LinkedHashMap<>();
        try {
            Map<String, Object> root = com.scaena.shows.registry.YamlLoader.load(file);
            Object rawCaps = root.get("captures");
            if (!(rawCaps instanceof Map<?, ?> capMap)) return result;
            for (Map.Entry<?, ?> entry : capMap.entrySet()) {
                String name = entry.getKey().toString();
                if (!(entry.getValue() instanceof Map<?, ?> dm)) continue;
                Map<String, Object> d = (Map<String, Object>) dm;
                String worldName = d.containsKey("world") ? d.get("world").toString() : null;
                if (worldName == null) continue;
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                if (world == null) continue;
                double x    = num(d, "x");
                double y    = num(d, "y");
                double z    = num(d, "z");
                float  yaw  = (float) num(d, "yaw");
                float  pitch= (float) num(d, "pitch");
                result.put(name, new Location(world, x, y, z, yaw, pitch));
            }
        } catch (Exception e) {
            log.warning("[Tech] Could not read existing captures for merge: " + e.getMessage());
        }
        return result;
    }

    private static double num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    private static File uniqueFile(File dir, String date) {
        File f = new File(dir, date + ".yml");
        if (!f.exists()) return f;
        int n = 2;
        while (f.exists()) {
            f = new File(dir, date + "-" + n + ".yml");
            n++;
        }
        return f;
    }
}
