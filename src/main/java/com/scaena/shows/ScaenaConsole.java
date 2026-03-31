package com.scaena.shows;

import org.bukkit.Bukkit;

/**
 * Theatrical console output for ScaenaShows.
 *
 * Uses Bukkit § color codes which Paper renders with ANSI colors in the server
 * console. All methods are static and safe to call from onEnable / onDisable.
 *
 * Intended output flow:
 *   onEnable()   → printBanner(version)
 *                → [registries log their individual load lines via Logger]
 *                → printStats(fw, cues, shows)
 *                → printCapabilities()
 *                → printReady()
 *   onDisable()  → printCurtainDown()
 *   /show reload → separator() before + printStats() + separator() after
 */
public final class ScaenaConsole {

    private ScaenaConsole() {}

    // ── Color palette ──────────────────────────────────────────────────────────
    private static final String P  = "§5";  // dark purple  – brand / body
    private static final String M  = "§d";  // light purple – headlines
    private static final String G  = "§6";  // gold         – accents / bullets
    private static final String Y  = "§e";  // yellow       – numbers / highlights
    private static final String GR = "§7";  // gray         – body text
    private static final String DG = "§8";  // dark gray    – structure / borders
    private static final String W  = "§f";  // white        – emphasis
    private static final String GN = "§a";  // green        – ready / success
    private static final String RD = "§c";  // red          – errors
    private static final String YL = "§e";  // yellow       – warnings (alias)

    // ── Reusable separator lines ───────────────────────────────────────────────
    private static final String SEP  =
        DG + "  ══════════════════════════════════════════════════════";
    private static final String LINE =
        DG + "  ──────────────────────────────────────────────────────";

    // ── Core output ───────────────────────────────────────────────────────────
    private static void c(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * ASCII-art startup banner. Call first thing in onEnable().
     */
    public static void printBanner(String version) {
        c(SEP);
        c(M + "   ____   ____    _    _____  _   _    _    ");
        c(M + "  / ___| / ___|  / \\  | ____|| \\ | |  / \\  ");
        c(P + "  \\___ \\| |     / _ \\ |  _|  |  \\| | / _ \\ ");
        c(P + "   ___) | |___ / ___ \\| |___ | |\\  |/ ___ \\");
        c(P + "  |____/ \\____/_/   \\_|_____|_| \\_/_/   \\_\\");
        c("");
        c(G  + "             S H O W S   " + DG + "·   " + G + "v" + version);
        c(SEP);
    }

    /**
     * Stage inventory summary. Call after all registries finish loading.
     *
     * @param fireworks number of firework presets loaded
     * @param cues      number of cues loaded
     * @param shows     number of shows ready
     */
    public static void printStats(int fireworks, int cues, int shows) {
        c(LINE);
        c(GR + "    Stage Inventory");
        c(G  + "    ►  " + Y + fireworks + "  " + GR + "firework presets");
        c(G  + "    ►  " + Y + cues      + "  " + GR + "cues");
        c(G  + "    ►  " + Y + shows     + "  " + GR + "shows ready");
        c(LINE);
    }

    /**
     * Capability summary — stable feature listing with a per-version revision note.
     * Call after printStats(), before printReady().
     *
     * Engine and Events sections grow only when new capability categories ship.
     * Tooling section grows when production workflow tools are added.
     * The revision line at the bottom is updated with each version bump.
     */
    public static void printCapabilities() {
        c(LINE);
        c(GR + "    Engine");
        c(G  + "    ►  " + GR + "Universal recursive Cue — unlimited nesting, cycle-safe");
        c(G  + "    ►  " + GR + "Reference-by-ID cue library  ·  tag taxonomy (§9–10)");
        c(G  + "    ►  " + GR + "Multi-participant  ·  GROUP_ASSIGN  ·  static spatial anchor");
        c(LINE);
        c(GR + "    Events");
        c(G  + "    ►  " + GR + "Teleport  ·  smooth movement  ·  stage direction  ·  return home");
        c(G  + "    ►  " + GR + "Fireworks  ·  sound  ·  chat  ·  title  ·  actionbar  ·  bossbar");
        c(G  + "    ►  " + GR + "Block state  ·  mob spawn  ·  glow");
        c(LINE);
        c(GR + "    Tooling");
        c(G  + "    ►  " + GR + "Scout — scene nav  ·  in-world markers  ·  entry TP  ·  auto-next");
        c(G  + "    ►  " + GR + "Show folder — direction  ·  departments  ·  run sheets");
        c(G  + "    ►  " + GR + "Calibration lab — archetype library  ·  pattern registry");
        c(LINE);
        c(DG + "    " + Y + "2.16.0" + DG + "  —  " + GR
            + "Scout workflow redesign: scene overview, TextDisplay markers, goto, next");
        c(SEP);
    }

    /**
     * "Live" confirmation. Call at the very end of onEnable().
     */
    public static void printReady() {
        c(GN + "  ⬡  " + W + "ScaenaShows is live.   " + GR + "Take the stage.");
        c("");
    }

    /**
     * Curtain-down message. Call in onDisable().
     */
    public static void printCurtainDown() {
        c("");
        c(DG + "  [ " + P + "ScaenaShows" + DG + " ]  " + GR + "Curtain down.");
    }

    // ── Per-show load line ─────────────────────────────────────────────────────

    /**
     * One line per show as it is registered — gives the show listing a stage-call feel.
     */
    public static void showLoaded(String showId, String showName) {
        c(DG + "    ✦  " + G + showId + DG + "  —  " + GR + showName);
    }

    // ── Reload ────────────────────────────────────────────────────────────────

    /**
     * Print a labeled separator — useful before and after a hot reload.
     */
    public static void separator(String label) {
        c(DG + "  ──  " + GR + label + "  " + DG
            + "─".repeat(Math.max(0, 46 - label.length())));
    }

    // ── Warnings and errors ────────────────────────────────────────────────────

    /**
     * Yellow warning line — for load-time issues that skip but don't halt.
     *
     * @param context short tag, e.g. the show ID or registry name
     * @param message human-readable description
     */
    public static void warn(String context, String message) {
        c(YL + "  ⚠  " + DG + "[" + GR + context + DG + "]  " + GR + message);
    }

    /**
     * Red error line — for hard failures (cycle detected, unknown CUE ID, etc.).
     *
     * @param context short tag, e.g. the show ID
     * @param message human-readable description
     */
    public static void error(String context, String message) {
        c(RD + "  ✘  " + DG + "[" + GR + context + DG + "]  " + GR + message);
    }
}
