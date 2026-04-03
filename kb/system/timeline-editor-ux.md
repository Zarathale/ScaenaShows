# ScaenaShows — Timeline Editor UX Design
**Date:** 2026-04-02
**Status:** Design document — pending review
**Scope:** In-game timeline editor (Phase 2 product surface)

---

## Core Mental Model

The timeline editor is a **cue sheet made physical**.

A stage manager's cue sheet is a sequential list of cues in order. Each cue has a number, a name, and a "standby" position — the moment just before it fires. The stage manager walks through the list, calls each cue, hears the result, makes notes, and iterates. The show exists on the paper before it exists in the room. Then you put it in the room and discover what's actually true.

ScaenaShows' timeline editor is that process, in Minecraft. The hotbar is your cue sheet — the current page of it. The world is the stage. You stand in the stage location, call cues, hear and see the result, and make adjustments. At every moment, the question you're answering is the same one a stage manager asks: *"What happens here, and does it feel right?"*

Three corollary principles follow from this:

**The show is always runnable.** Even a show with one cue is a show. The scaffold gives you a show with zero cues — that's valid. You don't author a show and then run it; you run it as you author it, from the first cue placed.

**The hotbar is your hands, the panels are your eyes.** You act through the hotbar (fire, confirm, select). You orient through panels (sidebar, timeline panel, library). The panels tell you what's there; the hotbar lets you do something about it.

**Time is navigation, not input.** You don't type "tick 420." You scroll to the right cue, adjust it with a scroll wheel, and the number follows. Numbers are feedback, not instruction.

---

## Interaction Model

### The Hotbar

Director Mode replaces the standard hotbar with a fixed layout. You enter Director Mode via `/scaena <showId> timeline` or the [Timeline] button from the show dashboard.

```
[ 1 ][ 2 ][ 3 ][ 4 ][ 5 ][ 6 ][ 7 ] | [ 8 ] | [ 9 ]
  ←————————— cue page (7 slots) ———————→  Lib   Sheet
```

**Slots 1–7 — The Cue Page**
Each slot represents one cue in the show, in sequence. Slot 1 is the earliest cue on the current page; slot 7 is the latest. A filled slot shows a named item: the cue's display name and its position in show-time. An empty slot — shown as a dim gray pane — appears when the show has fewer than 7 cues on this page.

| Slot state | Item appearance | What it means |
|---|---|---|
| Filled | Named item, colored by cue tag | A cue exists here |
| Empty | Gray pane, "[ — ]" | This position is open |
| Selected | Highlighted border | This is the active cue |

Interacting with a filled slot:
- **Left-click** — Spot preview: fire this cue immediately, from your position
- **Right-click** — Open cue detail panel (edit timing, replace, remove, full preview)
- **Shift+right-click** — Enter timing adjust mode for this cue

Interacting with an empty slot:
- **Right-click** — Open the cue library browser (with this slot's position pre-selected for placement)

**Slot 8 — Library Wand**
The Library Wand opens the cue library browser. Right-click to browse the full library. Scroll wheel while holding the Wand pages through the library list without opening the full panel — one cue at a time, visible in the actionbar. Left-click while a cue is previewed in the actionbar places it at the end of the show.

**Slot 9 — Cue Sheet**
The Cue Sheet item is the timeline's anchor. Right-click opens the full timeline panel — the complete show cue list in chat. Left-click advances the hotbar to the next page (if there is one). Shift+left-click retreats one page. Shift+right-click enters **Advance Mode** (see Preview section).

### What You Always See

**Sidebar (scoreboard)** — Persistent throughout the session. Shows the full cue list as a compact strip, with the current page highlighted. The sidebar is read-only; it tells you where you are in the show. Scrolling the sidebar is not needed — it fits up to 20 entries in a compact format.

```
✦ showcase.01
─────────────
  1  GROUP_ASSIGN  0:00
  2  [entrance]    0:03
▸ 3  [ramp.warm]   0:12
  4  [tension]     0:24
  5  [fanfare]     0:38
─────────────
  5 cues · 3:12
─────────────
```

The ▸ marks the first cue on the current hotbar page.

**Actionbar** — Transient state. Normally shows: `Director Mode  ·  showcase.01  ·  page 1 of 3`. In any active mode (timing adjust, library scroll, advance mode), the actionbar shows mode-specific context instead.

**Boss bar** — Show duration as a gold bar, labeled "0:00 → 8:20" (start and end). If you're previewing, the bar fills as the show plays. Otherwise it sits at the position of the first cue on the current page — a rough progress indicator for where you are in the show.

---

## Timeline Representation

### Time in the Sidebar

The sidebar shows time as `M:SS` from show start. Cues that fire within 5 seconds of each other are visually grouped with a thin separator. This makes clusters (a burst of events at one moment) visible without listing every individual event tick.

### Time on the Hotbar

Each slot item's title line reads: `2  ramp.warm_gold  (0:12)`. The number is the cue's position in sequence; the time is its tick position rendered as minutes:seconds. On item hover (lore), you also see the tick value and the gap before the next cue: `t:240  →  next in 4.0s`.

### Navigating the Timeline

The full timeline panel (Cue Sheet, slot 9) is the primary navigation tool for shows larger than one page. It renders in chat as a scrollable list. Each entry is clickable. Clicking a cue in the timeline panel jumps the hotbar page to the page containing that cue and highlights it as the active slot.

Between every pair of cues in the timeline panel, there is an insertion point: a faint `[+ insert here]` link. Clicking it opens the library browser with that insertion point pre-selected. This is how you add cues between existing ones.

Pages in the hotbar advance and retreat automatically: as you left-click the Cue Sheet to advance, the hotbar slides forward through the sequence. The sidebar's ▸ indicator moves with it. You always know where you are.

---

## Cue Insertion Flow

### From an empty slot (open space at the end of the show)

Right-click any empty slot in the hotbar. The library browser panel opens immediately in chat:

```
─────────────────────────────────────────────
  CUE LIBRARY   ·   63 cues
  Placing at: after cue 4  (t:520, +0:26)
─────────────────────────────────────────────
  Filter:  [all ▾]  [warm]  [tense]  [combat]
           [ambient]  [fireworks]  [moment]
─────────────────────────────────────────────
  ramp.warm_gold.01       warm  ramp   9s
  ramp.pulse.bright.01    warm  ramp   7s
  fx.tension_ramp.med     tense ramp  10s
  intro.entrance.warm     warm  opening 9s
  combat.fanfare.short    combat peak  4s
  ...  [more]
─────────────────────────────────────────────
```

Tags are the primary filter. Clicking a tag chip immediately filters the list to matching cues. Multiple tags can be active at once. Clicking `[all ▾]` resets.

Clicking any cue name in the library opens its **preview card**:

```
─────────────────────────────────────────────
  ramp.warm_gold.01
  "Building fireworks sequence in warm gold tones.
   Designed for use before a peak."
  Duration: 9s (180t)   Tags: warm, ramp, fireworks
─────────────────────────────────────────────
  [Hear it now]   [Place here]   [Back]
─────────────────────────────────────────────
```

**[Hear it now]** fires the cue immediately, from the player's position, as a spot preview. No panel changes. The cue plays; you hear and see it. Then the preview card is still open.

**[Place here]** places the cue at the intended insertion point, butted up against the previous cue (previous cue end → this cue start, no gap). The panel closes. The cue appears in the hotbar slot. A short confirmation sound plays. The actionbar reads: `ramp.warm_gold.01 placed at 0:26`.

### From the timeline panel (inserting between existing cues)

Click `[+ insert here]` between cue 3 and cue 4. The library browser opens with: `Placing between cue 3 (ends 0:18) and cue 4 (starts 0:24)  ·  gap: 6s`. Same flow from there — filter, select, preview, place. The inserted cue is auto-timed to the center of the gap. Adjustment happens in the editing flow.

### Default timing logic

When a cue is placed:
- **At the end:** starts when the previous cue ends (zero gap, butted)
- **In a gap:** centered in the available space
- **Overlapping:** allowed — cues can overlap in time (the show fires them simultaneously). The sidebar marks overlapping cues with a `~` prefix.

---

## Editing Flow

### Adjusting timing

Right-click a filled slot → cue detail panel:

```
─────────────────────────────────────────────
  ramp.warm_gold.01  ·  cue 3 of 5
  "Building fireworks in warm gold..."
  Starts: 0:12 (t:240)   Duration: 9s
  Gap after: 3s  ·  Previous gap: 2s
─────────────────────────────────────────────
  [◀ Adjust timing ▶]   [Replace]   [Remove]
  [Spot preview]   [Play from here]
─────────────────────────────────────────────
```

Clicking **[◀ Adjust timing ▶]** enters timing adjust mode. The panel closes. The actionbar becomes:

```
  ramp.warm_gold.01  ·  0:12  [← scroll →]  confirm: right-click  cancel: left-click
```

In timing adjust mode:
- Scroll wheel: ±20 ticks (±1 second) per notch
- Shift+scroll: ±100 ticks (±5 seconds) per notch
- The actionbar updates live with the new time as you scroll
- Right-click confirms the new timing and writes it to the show
- Left-click cancels, restoring the original timing

Timing adjusts are bounded: a cue cannot be moved before show start or past show end. If an adjustment would push the cue into its neighbor, the actionbar shows a warning: `ramp.warm.01 → 0:11  ⚠ overlaps with cue 2` — but it still allows the move. Overlap is legal.

### Replacing a cue

From the cue detail panel, click **[Replace]**. The library browser opens, same as insertion, but with the current cue ID pre-filtered out of results. Selecting a replacement drops the new cue into the same tick position as the one it replaces. Duration may differ — the actionbar confirms: `Replaced with fx.tension_ramp.med  ·  gap after: now 1s (was 3s)`.

### Removing a cue

From the cue detail panel, click **[Remove]**. Instant, with a brief confirmation sound. The slot becomes empty (or disappears from the hotbar page if it was the last cue on that page). No "are you sure" dialog — removes are undoable within the session by clicking **[Undo]** in the timeline panel. The undo stack goes back 10 operations.

### Reordering

Direct drag-and-drop is not available in chat panels. Reordering is accomplished by adjusting timing: if you want cue 4 to fire before cue 3, scroll cue 4's start time earlier than cue 3's. The sidebar reorders the list automatically to reflect actual sequence. Cues are always shown in chronological order, regardless of insertion order.

For a clean swap (no timing logic), the timeline panel's `[+ insert here]` plus `[Remove]` pattern works: insert a copy where you want it, remove the original.

---

## Preview Flow

There are four preview modes, for four different creative questions.

### 1. Spot preview — "Does this cue work here?"

Left-click any filled slot in the hotbar. The cue fires immediately from the player's current position. Marks are used if they're captured; otherwise the player position is the anchor. The boss bar flashes briefly gold. No other state changes — you're still in Director Mode when it ends.

This is the fastest loop. Select a cue, left-click, see it, adjust, left-click again. Three seconds per iteration.

### 2. Section preview — "How does this section feel?"

From the cue detail panel: **[Play from here]**. The show runs from this cue's tick position forward, in real time, using the full RunningShow executor. All cues fire on schedule. The show plays until it ends or you stop it.

During section preview:
- The boss bar gold fills as time passes
- Each cue highlights briefly as it fires (the corresponding hotbar slot brightens)
- The actionbar shows the current tick count
- Right-click the Cue Sheet (slot 9) to stop playback at any point

After preview ends, you're back in Director Mode at the same page.

### 3. Full run — "How does the whole show feel?"

From the timeline panel: click **[Play from start]**. Equivalent to `/show play <showId>`. The full show runs from tick 0. All marks are used; all entities spawn. This is the real thing.

Full run can be stopped at any time with `/show stop` (or the Cue Sheet slot 9 right-click while running). After the show ends, Director Mode resumes automatically.

### 4. Advance mode — "Let me walk this scene by scene"

Shift+right-click the Cue Sheet (slot 9). The hotbar border turns gold. The actionbar reads: `⚑ Advance Mode  ·  next: ramp.warm_gold.01  ·  right-click to GO`.

In advance mode:
- The "next" cue is highlighted in the hotbar (bright border)
- Right-click the Cue Sheet: fires the next cue, immediately, and advances the highlight to the cue after it
- Left-click: skip the next cue without firing (move the highlight forward without acting)
- Shift+right-click again: exit advance mode

Advance mode is the closest thing to live calling a show. You stand in the scene, fire each cue when it feels right, and discover the natural pacing. It doesn't produce timing data automatically — you're just calling cues manually. After advance mode, you go back to the timeline and adjust timings based on what felt right. This is the core of the iterative creative loop.

---

## Scaling: Simple to Complex Shows

**1–7 cues (one page):** Everything fits on a single hotbar page. No navigation needed. The sidebar and timeline panel are supplementary. The author works entirely through the hotbar.

**8–21 cues (2–3 pages):** Page navigation becomes relevant. The Cue Sheet (slot 9) left-click/shift advances and retreats. The sidebar stays in view as a fixed anchor — you always know where you are. The timeline panel is used for structural moves (insert, reorder).

**22+ cues (many pages):** The timeline panel becomes the primary navigation surface. Click any cue in the panel to jump the hotbar to that page. Tag filtering in the library browser becomes essential. Advance mode is especially useful here — you don't need to page manually when walking the show if you're just calling it forward.

**Multiple scenes (teleport-based shows):** The timeline panel groups cues by scene when the show uses scene-based sets. Each scene is a collapsible section in the panel. Collapsed sections show their cue count and total duration. This gives shows like showcase.01 (6 scenes, many cues) a clear structural overview without overwhelming the list.

---

## Sound and Feedback Design

The editor communicates state through sound, not just text. This keeps the experience Minecraft-native — the world talks back.

| Action | Sound |
|---|---|
| Cue placed | Short bell tone (note block, high) |
| Cue removed | Dull thud (anvil, muffled) |
| Timing confirmed | Click (note block, click) |
| Timing cancelled | Soft brush (reverse) |
| Library browser opens | Page turn (book open) |
| Spot preview fires | The cue's own first sound (if any) |
| Advance mode enters | Subtle fanfare (2 notes, ascending) |
| Show save | Completion chime |

No sound is intrusive. All are distinct enough to understand without looking at the screen.

---

## Save and Exit

The timeline editor accumulates changes in memory throughout the session. Nothing is written to disk until you save. The sidebar shows a `●` indicator (like a document editor's unsaved dot) whenever there are unsaved changes.

Saving: **[Save]** button in the timeline panel, or `/scaena <showId> save`. Writes the modified timeline back to the show YAML. The actionbar confirms: `showcase.01 saved — 5 cues`.

Exiting without saving: the editor prompts once in chat (`[Save and exit]  [Exit without saving]  [Cancel]`). This is the only confirmation dialog in the entire editor.

After saving, the YAML is updated on disk. To see changes in the running game, `/show reload <showId>` — which the timeline panel offers as a one-click action after save.

---

## What This Editor Does Not Do

To keep the scope clear:

**Cue-level event editing** — the events inside a cue (what sounds play, what particles fire) are not editable here. Cues are library items. They're assembled in Cowork with Claude. The timeline editor places and times them; it doesn't author their contents.

**Creating new cues from scratch** — same reason. New archetypes go through the Cowork authoring session. The library browser is for selecting from existing cues, not building new ones.

**Firework preset editing** — handled via `fireworks.yml` externally.

**Multi-participant assignment** — show group configuration is a param, set through the prompt-book parameter editor, not the timeline editor.

These boundaries are intentional. The timeline editor is a compositor, not a full authoring environment. Its job is to ask: *what cue fires here, and when?* Everything else happens elsewhere.

---

*See also: `docs/ux-review-2026-04-02.md` — the UX audit this document extends. See `kb/system/ops-027-building-spec.md` and `ops-inbox.md` OPS-029 for plugin-level implementation notes.*
