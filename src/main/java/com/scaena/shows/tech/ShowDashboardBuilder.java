package com.scaena.shows.tech;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds and sends the Show Status Dashboard panel for a single show.
 *
 * Entry point: /scaena <showId>   (bare show ID, no subcommand)
 *
 * Stateless query — reads PromptBook + scout captures + timeline cue count.
 * No active TechSession required. All data assembled by TechManager.sendDashboard().
 *
 * Panel anatomy:
 *
 *   ─────────────────────────────────────────────────────
 *     ✦  showcase.01  ·  Preparing for Battle
 *     Stage: scouted   Duration: ~12 min
 *   ─────────────────────────────────────────────────────
 *     SCENES                                  5 / 6 ready
 *     [00] Home Base — The Workshop        ✓  scouted
 *     [01] The Road North                  ✓  scouted
 *     [02] The Ruined Village              ⚠  pending
 *   ─────────────────────────────────────────────────────
 *     MARKS                                  11 / 14 set
 *     companion_spawn ✓   vindicator_spawn ✓   blast_furnace ⚠
 *   ─────────────────────────────────────────────────────
 *     OPEN ITEMS
 *     ⬜ victory coda fireworks preset — Mira (pending)
 *     ⬜ foe_health_multiplier — TBD
 *   ─────────────────────────────────────────────────────
 *     TIMELINE                              12 cues
 *     [Browse & Edit Timeline...]
 *   ─────────────────────────────────────────────────────
 *     [Enter Setup →]   [Quick-play]
 *   ─────────────────────────────────────────────────────
 */
public final class ShowDashboardBuilder {

    // Color palette — aligned with TechPanelBuilder conventions
    private static final TextColor COL_SEP      = TextColor.color(0x444444); // dark gray
    private static final TextColor COL_GOLD     = TextColor.color(0xFFD700); // gold (section labels)
    private static final TextColor COL_HEADER   = TextColor.color(0xFFFFFF); // white (show title)
    private static final TextColor COL_META     = TextColor.color(0x888888); // medium gray (stage/duration)
    private static final TextColor COL_SECTION  = TextColor.color(0xCCCCCC); // light gray (section header words)
    private static final TextColor COL_COUNT    = TextColor.color(0x55FFFF); // aqua (fractions)
    private static final TextColor COL_SCENE    = TextColor.color(0xFFFFFF); // white (scene label)
    private static final TextColor COL_OK       = TextColor.color(0x55FF55); // green (✓)
    private static final TextColor COL_WARN     = TextColor.color(0xFFAA00); // orange (⚠)
    private static final TextColor COL_MARK     = TextColor.color(0x55FFFF); // aqua (mark names)
    private static final TextColor COL_OPEN     = TextColor.color(0xAAAAAA); // gray (open items text)
    private static final TextColor COL_INACTIVE = TextColor.color(0x555555); // dark gray (missing)
    private static final TextColor COL_SETUP    = TextColor.color(0x55FF55); // green (enter setup btn)
    private static final TextColor COL_PLAY     = TextColor.color(0xFFFF55); // yellow (quick-play btn)
    private static final TextColor COL_TIMELINE = TextColor.color(0xAA88FF); // purple (timeline link)

    private static final String SEP = "─────────────────────────────────────────────";

    private ShowDashboardBuilder() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Build and send the Show Status Dashboard to the player.
     *
     * @param player            recipient
     * @param book              loaded PromptBook for the show
     * @param capturedMarkNames set of mark names with confirmed captures (from scout_captures/)
     * @param timelineCueCount  number of top-level events in the show's timeline, or -1 if unknown
     */
    public static void send(Player player, PromptBook book,
                            Set<String> capturedMarkNames, int timelineCueCount) {
        Component sep = Component.text(SEP, COL_SEP);

        player.sendMessage(sep);
        player.sendMessage(buildHeaderLine(book));
        player.sendMessage(buildMetaLine(book));
        player.sendMessage(sep);
        player.sendMessage(buildSectionHeader("SCENES", countReadyScenes(book), book.scenes().size(), "ready"));
        for (PromptBook.SceneSpec scene : book.scenes()) {
            player.sendMessage(buildSceneLine(scene, book.showId()));
        }
        player.sendMessage(sep);

        // Collect all marks across all scenes
        Set<String> allMarks = collectAllMarks(book);
        long capturedCount   = allMarks.stream().filter(capturedMarkNames::contains).count();
        player.sendMessage(buildSectionHeader("MARKS", (int) capturedCount, allMarks.size(), "set"));
        player.sendMessage(buildMarksRow(allMarks, capturedMarkNames, book.showId()));
        player.sendMessage(sep);

        // Open items — only show section if there are any
        List<String> openItems = book.readiness() != null ? book.readiness().open() : List.of();
        if (!openItems.isEmpty()) {
            player.sendMessage(Component.text("  OPEN ITEMS", COL_SECTION, TextDecoration.BOLD));
            for (String item : openItems) {
                player.sendMessage(Component.text("  ⬜ ", COL_WARN)
                    .append(Component.text(item, COL_OPEN)));
            }
            player.sendMessage(sep);
        }

        // Timeline row
        String cueLabel = timelineCueCount >= 0 ? timelineCueCount + " cues" : "—";
        player.sendMessage(
            Component.text("  TIMELINE", COL_SECTION, TextDecoration.BOLD)
                .append(Component.text("  " + cueLabel, COL_COUNT)));
        player.sendMessage(buildTimelineLink(book.showId()));
        player.sendMessage(sep);

        // Action buttons
        player.sendMessage(buildActionsRow(book.showId()));
        player.sendMessage(sep);
    }

    // -----------------------------------------------------------------------
    // Header lines
    // -----------------------------------------------------------------------

    private static Component buildHeaderLine(PromptBook book) {
        return Component.text("  ", COL_SEP)
            .append(Component.text("✦", COL_GOLD))
            .append(Component.text("  " + book.showId(), COL_GOLD))
            .append(Component.text("  ·  ", COL_SEP))
            .append(Component.text(book.title() != null ? book.title() : book.showId(), COL_HEADER));
    }

    private static Component buildMetaLine(PromptBook book) {
        StringBuilder sb = new StringBuilder("  Stage: ");
        sb.append(book.stage() != null ? book.stage() : "brief");
        if (book.durationTarget() != null) {
            sb.append("   Duration: ").append(book.durationTarget());
        }
        return Component.text(sb.toString(), COL_META);
    }

    // -----------------------------------------------------------------------
    // Section header with fraction
    // -----------------------------------------------------------------------

    private static Component buildSectionHeader(String label, int count, int total, String noun) {
        String fraction = total > 0 ? count + " / " + total + " " + noun : "";
        return Component.text("  " + label, COL_SECTION, TextDecoration.BOLD)
            .append(Component.text("  " + fraction, COL_COUNT));
    }

    // -----------------------------------------------------------------------
    // Scene rows
    // -----------------------------------------------------------------------

    private static Component buildSceneLine(PromptBook.SceneSpec scene, String showId) {
        boolean ready = scene.status() == PromptBook.StageStatus.SCOUTED
                     || scene.status() == PromptBook.StageStatus.YAML_READY;

        String statusSymbol = ready ? " ✓" : " ⚠";
        String statusText   = switch (scene.status()) {
            case SCOUTED    -> "scouted";
            case YAML_READY -> "yaml ready";
            default         -> "pending";
        };
        TextColor symbolColor = ready ? COL_OK : COL_WARN;
        TextColor labelColor  = ready ? COL_SCENE : COL_META;

        String numStr = scene.sceneNumber() != null ? scene.sceneNumber() : "?";
        // Zero-pad to 2 digits for alignment (handles "0", "1", "01", etc.)
        try {
            int num = (int) Double.parseDouble(numStr);
            numStr = String.format("%02d", num);
        } catch (NumberFormatException ignored) {}

        String clickCmd = "/scaena tech " + showId + " " + scene.id();
        String hoverTxt = ready
            ? "Enter setup for this scene"
            : "Scene not yet scouted — enter tech to begin";

        return Component.text("  [" + numStr + "] ", COL_INACTIVE)
            .append(Component.text(scene.label() != null ? scene.label() : scene.id(), labelColor)
                .clickEvent(ClickEvent.runCommand(clickCmd))
                .hoverEvent(HoverEvent.showText(Component.text(hoverTxt))))
            .append(Component.text(statusSymbol, symbolColor))
            .append(Component.text("  " + statusText, symbolColor));
    }

    // -----------------------------------------------------------------------
    // Marks row
    // -----------------------------------------------------------------------

    private static Component buildMarksRow(Set<String> allMarks, Set<String> capturedMarkNames,
                                            String showId) {
        if (allMarks.isEmpty()) {
            return Component.text("  No marks defined.", COL_INACTIVE);
        }

        Component row = Component.text("  ");
        boolean first = true;
        for (String mark : allMarks) {
            if (!first) row = row.append(Component.text("   "));
            first = false;

            boolean captured = capturedMarkNames.contains(mark);
            TextColor markCol = captured ? COL_MARK : COL_WARN;
            String symbol     = captured ? " ✓" : " ⚠";
            TextColor symCol  = captured ? COL_OK : COL_WARN;

            String clickCmd = "/scaena tech " + showId;  // enter tech to capture missing marks
            String hoverMsg = captured ? "Captured" : "Not yet captured — enter setup to set this mark";

            row = row
                .append(Component.text(mark, markCol)
                    .clickEvent(ClickEvent.runCommand(clickCmd))
                    .hoverEvent(HoverEvent.showText(Component.text(hoverMsg))))
                .append(Component.text(symbol, symCol));
        }
        return row;
    }

    // -----------------------------------------------------------------------
    // Timeline link
    // -----------------------------------------------------------------------

    private static Component buildTimelineLink(String showId) {
        return Component.text("  ")
            .append(Component.text("[Browse & Edit Timeline...]", COL_TIMELINE)
                .clickEvent(ClickEvent.suggestCommand("/scaena " + showId + " timeline"))
                .hoverEvent(HoverEvent.showText(
                    Component.text("Timeline editor — coming in a future update\n"
                        + "(/scaena " + showId + " timeline)"))));
    }

    // -----------------------------------------------------------------------
    // Action buttons
    // -----------------------------------------------------------------------

    private static Component buildActionsRow(String showId) {
        Component enterSetup = Component.text("[Enter Setup →]", COL_SETUP)
            .clickEvent(ClickEvent.runCommand("/scaena tech " + showId))
            .hoverEvent(HoverEvent.showText(
                Component.text("Enter Tech Rehearsal Mode for this show")));

        Component quickPlay = Component.text("[Quick-play]", COL_PLAY)
            .clickEvent(ClickEvent.runCommand("/show play " + showId))
            .hoverEvent(HoverEvent.showText(
                Component.text("Run the show now: /show play " + showId)));

        return Component.text("  ")
            .append(enterSetup)
            .append(Component.text("   "))
            .append(quickPlay);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static int countReadyScenes(PromptBook book) {
        if (book.scenes() == null) return 0;
        return (int) book.scenes().stream()
            .filter(s -> s.status() == PromptBook.StageStatus.SCOUTED
                      || s.status() == PromptBook.StageStatus.YAML_READY)
            .count();
    }

    /**
     * Collect all unique mark names defined across all scenes:
     * arrival marks + casting marks + set marks.
     * Order: arrival marks first, then casting, then set — deduped with insertion order.
     */
    static Set<String> collectAllMarks(PromptBook book) {
        Set<String> marks = new LinkedHashSet<>();
        if (book.scenes() == null) return marks;
        for (PromptBook.SceneSpec scene : book.scenes()) {
            if (scene.arrivalMark() != null) marks.add(scene.arrivalMark());
            if (scene.hasCasting()) {
                scene.casting().entries().stream()
                    .map(PromptBook.CastingEntry::mark)
                    .filter(m -> m != null)
                    .forEach(marks::add);
            }
            if (scene.hasSet()) {
                scene.set().entries().stream()
                    .map(PromptBook.SetEntry::mark)
                    .filter(m -> m != null)
                    .forEach(marks::add);
            }
        }
        return marks;
    }
}
