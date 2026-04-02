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
 * Responsibilities:
 *
 *   writeParams     — updates param value: lines in [show_id].prompt-book.yml in-place,
 *                     preserving all comments and surrounding structure.
 *
 *   writeCaptures   — writes a new dated file to scout_captures/[show_id]/[date].yml,
 *                     merging with the most recent existing capture file so no prior
 *                     captures are lost.
 *
 *   writeBbox       — writes/updates bbox_min + bbox_max fields in the scene's set:
 *                     section of the prompt-book. Handles both legacy list format
 *                     (auto-migrates to map format) and existing map format.
 *
 *   writeActiveBuild — updates active_build: in the scene's set: section.
 *
 * All prompt-book writes use a comment-preserving line-scan approach.
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
    // writeBbox — update/add bbox_min + bbox_max in the scene's set: section
    // -----------------------------------------------------------------------

    /**
     * Writes bbox_min and bbox_max into the target scene's set: department block.
     *
     * If the set: block is in legacy list format, this method migrates it to the
     * new map format first (wrapping entries under an `entries:` sub-key).
     *
     * @param bookFile  the prompt-book.yml file
     * @param sceneId   target scene id
     * @param min       bbox minimum corner
     * @param max       bbox maximum corner
     * @return true on success
     */
    public boolean writeBbox(File bookFile, String sceneId,
                              PromptBook.BboxPoint min, PromptBook.BboxPoint max) {
        return writeSetFields(bookFile, sceneId,
            "bbox_min: " + bboxStr(min),
            "bbox_max: " + bboxStr(max),
            null);
    }

    /**
     * Updates active_build: in the target scene's set: department block.
     */
    public boolean writeActiveBuild(File bookFile, String sceneId, String buildStem) {
        return writeSetFields(bookFile, sceneId, null, null, buildStem);
    }

    /**
     * Core line-scanner: finds the target scene's set: section and writes
     * bbox_min, bbox_max, and/or active_build as directed.
     * null arguments are skipped. At least one non-null argument is required.
     *
     * Handles both old (flat list) and new (map) set formats.
     * Old format is auto-migrated in-place.
     */
    private boolean writeSetFields(File bookFile, String sceneId,
                                    String bboxMinLine,   // e.g. "bbox_min: [world, 1, 2, 3]"
                                    String bboxMaxLine,
                                    String activeBuild) {
        if (bboxMinLine == null && bboxMaxLine == null && activeBuild == null) return false;
        if (!bookFile.exists()) {
            log.warning("[Tech] Cannot write set fields — file not found: " + bookFile.getPath());
            return false;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(bookFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.severe("[Tech] Failed to read prompt book: " + e.getMessage());
            return false;
        }

        List<String> result = new ArrayList<>(lines);

        // ── Phase 1: find the target scene block ──────────────────────────
        int sceneStart = -1;
        for (int i = 0; i < result.size(); i++) {
            String t = result.get(i).stripLeading();
            if (t.startsWith("- id:") || t.startsWith("-id:")) {
                String idVal = t.replaceFirst("^-\\s*id:\\s*", "").trim()
                                .replaceAll("\\s*#.*$", "").replace("\"","");
                if (sceneId.equals(idVal)) { sceneStart = i; break; }
            }
        }
        if (sceneStart < 0) {
            log.warning("[Tech] Scene not found in prompt book: " + sceneId);
            return false;
        }

        // The scene block's indent level (the "- id:" line)
        int sceneIndent = indentOf(result.get(sceneStart));

        // ── Phase 2: find "departments:" within this scene block ──────────
        int deptsLine = -1;
        for (int i = sceneStart + 1; i < result.size(); i++) {
            String line = result.get(i);
            if (isNewSceneOrTopLevel(line, sceneIndent)) break;
            String t = line.stripLeading();
            if (t.startsWith("departments:") && indentOf(line) > sceneIndent) {
                deptsLine = i; break;
            }
        }
        if (deptsLine < 0) {
            log.warning("[Tech] departments: not found in scene " + sceneId);
            return false;
        }
        int deptsIndent = indentOf(result.get(deptsLine));

        // ── Phase 3: find "set:" within departments ───────────────────────
        int setLine = -1;
        for (int i = deptsLine + 1; i < result.size(); i++) {
            String line = result.get(i);
            if (indentOf(line) <= deptsIndent && !line.isBlank() && !line.stripLeading().startsWith("#"))
                break;
            String t = line.stripLeading();
            if (t.startsWith("set:") && indentOf(line) > deptsIndent) {
                setLine = i; break;
            }
        }
        if (setLine < 0) {
            log.warning("[Tech] set: not found in scene " + sceneId + "'s departments");
            return false;
        }

        int setIndent    = indentOf(result.get(setLine));   // indent of "set:" key
        int childIndent  = setIndent + 2;                   // standard 2-space child indent

        // ── Phase 4: determine if set: is a list or map ───────────────────
        // Peek at the first non-blank child line after set:
        int firstChild = -1;
        for (int i = setLine + 1; i < result.size(); i++) {
            String line = result.get(i);
            if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
            if (indentOf(line) <= setIndent) break;
            firstChild = i; break;
        }

        boolean isListFormat = firstChild >= 0
            && result.get(firstChild).stripLeading().startsWith("- ");

        if (isListFormat) {
            // ── Migrate: wrap existing list items under "entries:" ────────
            // Collect all lines that belong to the set list (up to next same-or-lower-indent)
            int listStart = firstChild;
            int listEnd   = firstChild;
            for (int i = firstChild; i < result.size(); i++) {
                String line = result.get(i);
                if (line.isBlank() || line.stripLeading().startsWith("#")) { listEnd = i; continue; }
                if (indentOf(line) <= setIndent) break;
                listEnd = i;
            }

            // Build replacement: set: \n  entries: \n    - ... (re-indented +2)
            List<String> migrated = new ArrayList<>();
            migrated.add(" ".repeat(childIndent) + "entries:");
            for (int i = listStart; i <= listEnd; i++) {
                String orig = result.get(i);
                if (orig.isBlank()) { migrated.add(""); continue; }
                // Shift each line right by 2 spaces to sit under "entries:"
                migrated.add("  " + orig);
            }
            // Remove old list lines, insert migrated block
            for (int i = listEnd; i >= listStart; i--) result.remove(i);
            result.addAll(listStart, migrated);

            // Recompute setLine (unchanged), but firstChild is now the "entries:" line
        }

        // ── Phase 5: update/add bbox_min, bbox_max, active_build ─────────
        // Look for existing keys within set: block; update if found, else insert after set:
        String[] keysToWrite = { "bbox_min", "bbox_max", "active_build" };
        String[] valuesToWrite = {
            bboxMinLine  != null ? bboxMinLine  : null,
            bboxMaxLine  != null ? bboxMaxLine  : null,
            activeBuild  != null ? "active_build: " + activeBuild : null
        };

        // Determine the end of the set: block (exclusive)
        int setBlockEnd = result.size();
        for (int i = setLine + 1; i < result.size(); i++) {
            String line = result.get(i);
            if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
            if (indentOf(line) <= setIndent) { setBlockEnd = i; break; }
        }

        for (int ki = 0; ki < keysToWrite.length; ki++) {
            if (valuesToWrite[ki] == null) continue;
            String key      = keysToWrite[ki];
            String fullLine = " ".repeat(childIndent) + valuesToWrite[ki];

            // Search for existing key within set block
            boolean found = false;
            for (int i = setLine + 1; i < setBlockEnd; i++) {
                String t = result.get(i).stripLeading();
                if (t.startsWith(key + ":") && indentOf(result.get(i)) == childIndent) {
                    result.set(i, fullLine);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Insert after the set: line (before entries or other children)
                result.add(setLine + 1, fullLine);
                setBlockEnd++; // block grew by one line
            }
        }

        // ── Phase 6: write back ───────────────────────────────────────────
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(bookFile), StandardCharsets.UTF_8))) {
            for (String l : result) pw.println(l);
        } catch (IOException e) {
            log.severe("[Tech] Failed to write prompt book: " + e.getMessage());
            return false;
        }

        log.info("[Tech] Wrote set fields (bbox/active_build) for scene "
            + sceneId + " to " + bookFile.getName());
        return true;
    }

    private static String bboxStr(PromptBook.BboxPoint p) {
        return "[" + p.world() + ", " + p.x() + ", " + p.y() + ", " + p.z() + "]";
    }

    private static int indentOf(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') i++;
        return i;
    }

    private static boolean isNewSceneOrTopLevel(String line, int sceneIndent) {
        if (line.isBlank() || line.stripLeading().startsWith("#")) return false;
        return indentOf(line) <= sceneIndent
            && (line.stripLeading().startsWith("- id:") || !line.startsWith(" "));
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
