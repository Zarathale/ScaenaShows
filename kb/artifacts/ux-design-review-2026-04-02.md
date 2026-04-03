---
document: ScaenaShows — Full UX Design Review
status: delivered
authored: 2026-04-02
scope: ShowDesigner in-game UX — Phase 1 (current) and Phase 2 (spec)
context: Solo power user (Alan). Hybrid authoring model (rough YAML → in-game refinement). Primary pain: on-ramp is too steep.
---

# ScaenaShows — Full UX Design Review

> This review treats the current UX direction as a starting point, not a constraint.
> Some of what's here challenges decisions already in the spec. That's the job.

---

## 1. Reconstruction: The Real Player Journey

### 1.1 Starting from scratch

The first thing Alan does to start a show is not in-game at all — it's a Cowork session where Claude writes the Prompt Book and, later, the show YAML. The in-game tools are entered *after* the structural decisions have already been made externally.

This matters. The in-game UX is not a blank-canvas authoring environment. It is a **refinement tool** — which is exactly what Phase 2 spec says (Q2 resolution). But Phase 1 doesn't frame itself that way. It materializes a scene from the Prompt Book, but gives no clear indication of what refinement looks like, or when you're done.

### 1.2 Entering Tech Mode

```
/scaena tech showcase.01
```

What happens:
- Player is switched to ADVENTURE mode + flight
- 5 hotbar items are injected (slots 5–9): ◀ Prev, ⏸ Hold, ▶ Next, 📍 Capture, ⚙ Params
- A chat panel is sent — a block of clickable text that appears in the chat window
- A sidebar scoreboard appears
- An actionbar task starts (loops every 5 ticks)
- The first scene loads: entities spawn, blocks change, teleport fires if arrival mark is captured

If the arrival mark is NOT captured, the player is silently entered into capture mode for that mark. There's no explanation of why the hotbar item says "capture mode active" or what to do next.

### 1.3 The chat panel — the primary UI surface

The panel looks like this:

```
─────────────────────────────────────────
TECH · showcase.01  ·  Site A — The Workshop
[CAST ✓] [WDRB ✓] [SET ✓] [LGTS ✓] [FX —]
Marks: home_base  companion_spawn  vindicator_spawn
[Focus Mark...]   [Params]   [Save]  [Discard]  [Exit]
─────────────────────────────────────────
```

Every action is a clickable text button. Clicks run `/scaena tech <subcommand>` server-side. The panel is regenerated and re-sent as a new message on any state change.

**The fundamental problem with this model:** the panel is a chat message. It lives in the chat history. The moment Alan presses Escape, jumps, runs a command, or does anything that sends another message to chat, the panel scrolls up and out of view. To get it back, he has to either type `/scaena tech panel` or know that that command exists.

In practice: the panel is immediately out of reach after any in-world action. The primary interactive surface becomes inaccessible the moment you start using it.

### 1.4 Mark capture flow

Marks are the spatial anchors — where entities spawn, where blocks change, where the player arrives. Until marks are captured (position-stamped), the scene can't materialize correctly.

Current flow to capture a mark:
1. Click `[Focus Mark...]` in the chat panel — if the panel is still visible
2. A secondary mark list panel is sent to chat — another ephemeral panel
3. Click the mark name in that panel to focus it
4. Walk to where the mark should be
5. Right-click with slot 8 (Mark Capture item)
6. Confirmation appears in actionbar for 2 seconds
7. Entity teleports to new position immediately

Steps 1–3 require the player to look at chat, scroll back if needed, click two things. Steps 4–6 require the player to look at the world. This is a constant attention split: world → chat → world.

An uncaptured mark shows `⚠` in the secondary panel. A modified mark shows `✎`. A captured mark shows `✓`. This status is only visible in the secondary panel, which requires two clicks from the main panel to reach.

### 1.5 Param editing

Two paths:
- **Numeric params**: click `[Params]` in chat panel → click a param name → right-click with slot 9 to increment / shift+right-click to decrement
- **Text params**: click `[Params]` → click a text param → the game intercepts the NEXT thing you type in chat as the new value

The text param flow is particularly fragile. There is no visible mode indicator in the world — only the actionbar text (cycling on a 5-tick task). If Alan opens chat for any other reason — looking up something, sending a message — the first thing he types becomes the param value, silently.

### 1.6 Department toggles

Clicking `[CAST ✓]` in the chat panel despawns all cast entities. Clicking again re-spawns them. The toggle is immediate and correct. But it requires the panel to be visible, and every toggle re-sends the panel as a new chat message — pushing older content further up.

After 5–6 toggles, the chat history is a stack of re-sent panels with no clear indication of which is current.

### 1.7 Navigating scenes

Hotbar slot 5 (◀) goes to the previous scene; slot 7 (▶) goes to the next. These work. But "previous" and "next" are defined by scene_number order in the Prompt Book. There's no indication of how many scenes exist or where in the sequence Alan currently is. The sidebar shows the scene label; the actionbar shows capture mode details. Neither shows "scene 2 of 6."

### 1.8 Saving and exiting

`[Save]` in the chat panel writes to `[show_id].prompt-book.yml`. The save is confirmed in the actionbar. Then `[Exit]` dismisses the session: entities despawn, blocks restore, player state restores.

If there are unsaved changes on exit, a save prompt is sent to chat. The prompt itself is two clickable buttons: `[Save to prompt-book.yml]` and `[Discard changes]`. If Alan accidentally closes chat, the prompt is gone. There's no recovery path.

### 1.9 Phase 2 (spec — not yet built)

Phase 2 adds a cue-to-cue navigation mode layered on top of Phase 1. It introduces:
- Two explicit modes: **Edit Mode** and **Preview Mode**
- Additional hotbar semantics for existing slots (slot 5 becomes PREV cue, slot 7 becomes GO)
- A `TechCueSession` alongside the existing `TechSession`
- Sidebar timeline cursor showing what fired and what's next
- `ShowYamlEditor` for four targeted mutations (param patch, tick shift, event insert, event remove)

The Phase 2 spec is technically sound. But the UX specification inherits all Phase 1 problems (chat panel, ephemeral surface, text param input via chat) and adds mode confusion on top: now the same hotbar slots mean different things depending on whether you're in Edit or Preview mode.

---

## 2. UX Failure Diagnosis

### 2.1 The chat panel is the wrong surface

This is the root of most problems. The chat panel is:
- **Ephemeral** — scrolls away the moment anything else touches chat
- **Unreliable** — state shown in the panel may be stale (panel is only refreshed on specific events)
- **Not spatially anchored** — it lives in a different place than the scene Alan is looking at
- **Compositionally messy** — every toggle re-sends the entire panel, creating visual noise

A clickable chat UI works for low-frequency operations (confirm a purchase, acknowledge a warning). It does not work as a primary interactive control surface for a creative tool where Alan is constantly looking at the world and occasionally adjusting values.

### 2.2 Attention split kills creative flow

The current workflow forces constant context switching between two spaces:
- **The world** — where the scene lives, where spatial judgments are made
- **The chat window** — where controls live

This split is not incidental. It's structural. Every action that matters requires looking at chat. Looking at chat means not looking at the scene. For a tool whose entire value is spatial, in-world judgment, this is a self-defeating architecture.

### 2.3 Input surface fragmentation

Five hotbar slots, each with right-click semantics, some with shift modifier. Chat panel for department toggles. Chat window for text input. Actionbar for status. Sidebar for state. These are five different input/output surfaces, with no clear primary surface and overlapping responsibilities.

Examples of conflict:
- Slot 8 (Capture) requires being focused on the right mark AND being in the right position. No spatial indication of where to stand.
- Slot 9 (Params) requires first selecting a param via chat panel, then using the hotbar item. Split attention, two steps.
- Text param entry intercepts chat globally — any chat input during an active session may be consumed as a param value.

### 2.4 Mode confusion will compound in Phase 2

Phase 1 already has implicit modes: "is capture mode active?", "is scroll mode active?", "am I awaiting text input?" These are soft, unmarked modes tracked in TechSession state but not clearly surfaced to the player.

Phase 2 introduces hard modes (Edit vs Preview) with the same hotbar layout. The same slot 7 that advances to the next scene in Phase 1 fires GO (advances to next cue) in Phase 2 Preview mode, and does something else in Phase 2 Edit mode. The spec acknowledges this ("same slots, expanded semantics") but expanded semantics from a player's perspective means more things to remember.

### 2.5 No readiness signal

When Alan enters tech mode, there's no indication of whether the scene is "ready" (all marks captured, all departments materialized correctly) or what specifically is missing. The mark list panel shows `⚠` for uncaptured marks — but you have to know to look there, and it requires two panel navigations to find.

A scene isn't useful for spatial judgment until it's correctly materialized. The system gives no clear "here's what's missing before this scene is ready" status.

### 2.6 The vocabulary is engineering-facing, not creator-facing

Params are shown as dot-path keys: `role.entity_type`, `companion.entity_type`. Mark names use underscore slugs: `companion_spawn`, `vindicator_spawn`. Departments are shown as `[CAST ✓]` — abbreviated engineering terms.

For a tool designed around a theatrical metaphor ("you are the director"), the controls read like a database editor. The language is technically precise but creatively opaque. "companion_spawn" doesn't evoke the creative decision the way "The Companion's entrance mark" would.

### 2.7 Where the architecture is genuinely strong

Not everything needs fixing. Some things are working:

- **Stop-safety** is solid. Entities despawn, blocks restore, player state restores. The cleanup contract is correct and reliable.
- **Department toggle model** is a good idea. Isolating a single department (casting-only, lighting-only) is exactly how a director calls a spot check in rehearsal. The concept is right; the surface is wrong.
- **TextDisplay mark markers in-world** are the right move. Floating labels at mark positions are visible, spatially anchored, and interpretable without going to chat. These should be the *primary* UI surface for spatial operations, not a passive display.
- **Prompt Book architecture** is correct. Single source of truth for committed state, separate from working docs and show YAML. The distinction between "what the show IS" and "what happens when" is right.
- **Phase 1 / Phase 2 separation** is architecturally right. Scene setup (Phase 1) and cue-to-cue refinement (Phase 2) are genuinely different workflows. The code separation (TechSession / TechCueSession) is appropriate.

---

## 3. The Redesign

### A. Core Mental Model

**What is Alan doing when he's in Tech Mode?**

He's a director standing in a scene, making spatial and creative judgments. The scene is live around him. He's checking: Is the Companion facing the right direction? Does the lighting hit the armor stand correctly? Is there enough space for the Vindicator to be hidden behind the wall?

Between those judgments, he occasionally needs to make an adjustment: move a mark, toggle a department to see the scene without it, change a param. These adjustments should feel like directing — quick, decisive, then back to watching.

**Primary objects (from player's perspective):**

- **The Show** — the experience being built. Has scenes, has a timeline.
- **The Scene** — a specific place in the world, assembled for a specific act. Has entities, blocks, marks.
- **Marks** — named positions in the world. They're already visible as floating labels.
- **Departments** — categories of scene elements. Toggle them to isolate and inspect.
- **Cues** (Phase 2) — moments in the timeline. Step through them to see the sequence.

**What does NOT need to be a first-class concept for the player:**

- The Prompt Book (an internal authoring artifact, not a player-facing concept)
- The TechSession / TechCueSession distinction (implementation detail)
- The difference between captures and marks (a mark is a position; it's either set or it isn't)

### B. Interaction Model

**The core principle: keep attention in the world.**

Actions that are spatial should be done spatially. Actions that are structural (toggling departments, navigating scenes, saving) should be accessible without looking at chat.

**Three components replace the current five:**

**1. The Console (inventory GUI)**

Replaces the chat panel. Opened by clicking the Director's Baton item (hotbar, slot 5 — replaces the current Prev Scene which moves to a slot within the Console). Minecraft's chest inventory GUI is a well-understood, persistent, closeable surface. It doesn't scroll away.

Layout sketch:
```
┌─────────────────────────────────────────────────────┐
│ TECH · showcase.01 · Site A — The Workshop           │
├──────────────────────────────────────────────────────┤
│  [CAST ✓]  [WDRB ✓]  [SET ✓]  [LGTS ✓]  [FX —]     │  Row 1: Dept toggles
│            (green glass panes / gray panes)           │
├──────────────────────────────────────────────────────┤
│  [← Prev]  [Site A ▾]  [→ Next]                      │  Row 2: Scene nav
│  2 / 6 scenes                                         │
├──────────────────────────────────────────────────────┤
│  Marks:                                               │  Row 3: Marks
│  [home_base ✓]  [companion_spawn ✓]  [vindicator ⚠]  │
│            (gold = captured, red = missing)           │
├──────────────────────────────────────────────────────┤
│  Params:                                              │  Row 4: Key params
│  [entity_type: VILLAGER/ARMORER]  [time_of_day: 12500]│
├──────────────────────────────────────────────────────┤
│  [Save ✓]  [Discard]  [Build Mode]  [Exit Tech]       │  Row 5: Actions
└─────────────────────────────────────────────────────┘
```

Each item in the grid is a named ItemStack with appropriate display name and lore. Clicking a department item toggles it (no command needed). Clicking a mark item focuses it for capture (actionbar shows coordinates as you walk). Clicking a param item opens a secondary inventory panel or book GUI for editing.

The Console is opened and closed with the Baton. It persists until explicitly closed. State shown in the Console is always current because it's rendered at open time.

**2. The Mark Tool (hotbar slot 7)**

Spatially anchored interaction. When held:
- Actionbar shows the focused mark name and current coordinates
- Right-click confirms capture at current position
- Scroll up/down cycles through marks
- The in-world TextDisplay markers pulse/glow to indicate which one is focused

The spatial flow is: walk toward the floating label → watch coordinates in actionbar → step to the right position → right-click. Zero chat involvement.

**3. The Param Dial (hotbar slot 8)**

Numeric param adjustment only. When held:
- Sidebar shows the param list with current focused param highlighted
- Scroll wheel increments/decrements the focused param value
- Right-click selects the next param to focus
- The world updates live (entity responds to the change immediately)

For text params: clicking the item in the Console (not the hotbar dial) opens a Book & Quill GUI with the current value pre-filled. Alan types the new value, closes the book. No chat interception.

**Hotbar summary (4 slots, not 5):**

| Slot | Item | Function |
|------|------|---------|
| 5 | Director's Baton | Toggle the Console open/closed |
| 6 | *(free for block work)* | — |
| 7 | Mark Tool | Spatial mark capture (scroll to cycle, right-click to set) |
| 8 | Param Dial | Numeric param scroll (scroll to adjust, right-click to cycle params) |

Phase 2 adds:
| Slot | Item | Function |
|------|------|---------|
| 5 | Director's Baton | Opens Console (now includes Timeline tab) |
| 6 | HOLD | Interrupt mid-sequence |
| 7 | Mark Tool / GO | Context-sensitive: in Edit mode = mark tool; in Preview mode = advance cue |
| 8 | Param Dial | Same as Phase 1 |

The mode switch (Edit vs Preview) is explicit and visible — the Console's header shows `[EDIT ✎]` or `[PREVIEW ▶]` as a colored indicator, and slot 7's item changes visually (a wrench vs. a play button head). Alan never has to guess which mode he's in.

### C. Full Workflow: Create → Structure → Edit → Rehearse → Perform

**Create & Structure (external, pre-game):**

This stays where it is: Cowork session with Claude. Prompt Book is written. Show YAML skeleton is authored. This is the right place for this work. No redesign needed here.

**Edit / Scene Setup (Phase 1):**

1. `/scaena tech showcase.01` — enters tech mode
2. Console opens automatically on entry
3. Console header shows scene readiness: "2/3 marks missing" in amber
4. Alan closes Console (presses Baton or Escape), walks to a mark position
5. Draws Mark Tool (slot 7), follows the actionbar to position, right-clicks to set
6. Repeats for remaining marks — sidebar shows the progress count ticking up
7. When all marks are green, opening the Console shows a "Scene ready" indicator
8. Toggles departments via Console to spot-check: turns off Lighting, looks at the scene in natural light
9. Adjusts a param via Console → Param Dial scroll
10. Saves via Console `[Save]` → confirmation in actionbar
11. Navigates to next scene (Console, row 2)

Every step involves either looking at the world or looking at the Console. No chat scrolling.

**Cue-to-Cue Rehearsal (Phase 2):**

1. From Phase 1 scene, select `[Cue-to-Cue]` in the Console
2. Console switches to Timeline tab: cue list from the show timeline, current cue highlighted
3. Alan closes Console, walks around the live scene
4. Presses GO (slot 7 in Preview mode) — next cue fires, world updates
5. Watches the result: "the levitation starts too late"
6. Holds PREV (slot 6 held) to rewind to that cue — world resets to that moment
7. Console reopens on that cue's params automatically (the PREV editorial intent from the spec)
8. Adjusts the tick offset via Param Dial
9. GO again — watches the fix
10. `[Save YAML]` in Console

**Perform:**

```
/show play showcase.01 Zarathale
```

Optional `--scenes` flag shows cue names in actionbar during playback (already in spec). No UX changes needed here — the playback system is not the problem.

### D. In-World Authoring Model

**Does "clickable chat + hotbar" actually work?**

No. As diagnosed above, the chat panel is fundamentally wrong as a primary control surface for an in-world spatial tool. The hotbar model is right; the chat panel is wrong.

**Should interaction shift toward inventory GUIs?**

Yes, for structural operations (toggles, navigation, save/discard). The inventory GUI is:
- Persistent — stays on screen, doesn't scroll away
- Minecraft-idiomatic — every player knows how to use an inventory
- Visually clear — items can be colored, named, and arranged in a grid

For spatial operations (mark capture), the in-world model (hotbar item + world movement) is correct. The problem is not the spatial model; it's that accessing the spatial operations currently requires the chat panel as a prerequisite.

**The mark markers should do more work:**

The `TextDisplay` mark markers already float in the world at each position. They are currently passive (display-only). They should be interactive:

- When player right-clicks a mark marker (while in ADVENTURE mode), it focuses that mark for capture. No console navigation required. Walk up, right-click the label, walk to the right position, right-click with the Mark Tool.

This makes the spatial model truly spatial: the mark is in the world, you interact with it in the world.

**Should spatial interaction replace the console entirely?**

Not entirely. Structural actions (department toggles, save, exit) don't have a natural spatial representation. The console handles those. But mark capture and entity positioning should be driven by in-world interaction as much as possible.

### E. Progressive Complexity

Because Alan is the sole user, "progressive complexity" means: easy to do the common thing, accessible to do the uncommon thing, never requiring documentation for either.

**Tier 1 — The first session:**
- Enter `/scaena tech showcase.01`
- Console opens automatically, shows what's missing
- Walk to each mark, draw slot 7, right-click
- Toggle departments to check the scene
- Save and exit

No documentation required if the Console is well-labeled and the actionbar provides positional guidance.

**Tier 2 — Regular refinement sessions:**
- Scene is already scouted
- Scene materializes on entry, marks are all captured
- Alan opens Console only to toggle departments or check a param
- Mark Tool is used to reposition a mark that's slightly off
- Most sessions don't require opening the Console at all

**Tier 3 — Phase 2 cue work:**
- Alan has authored show YAML in a prior Cowork session
- Enters Phase 2 via Console → Cue-to-Cue
- Steps through the timeline using GO
- Uses PREV to return to a moment and adjust a param
- Saves YAML

The system scales naturally because each tier adds one concept (console → timeline) without removing the concepts from the previous tier.

---

## 4. Architectural Alignment

### Where the architecture supports good UX

**TechSession** is a clean, well-encapsulated state model. It already tracks everything needed (entities, blocks, marks, params, modified state). None of this needs to change.

**TechManager** methods (`loadScene`, `toggleDept`, `confirmCapture`, `saveChanges`, etc.) are already correctly isolated. The console just calls these methods instead of the chat panel.

**Stop-safety** is implemented correctly and independently of the UI surface. It will work with the new UI.

**Phase 1 / Phase 2 lifecycle separation** (TechSession / TechCueSession) is architecturally right. The Console just needs a way to display both layers (scene context + cue context) in the same UI.

### Where the architecture forces bad UX

**TechPanelBuilder** is a pure rendering class with no state. It is the only thing that needs to change to implement the inventory GUI redesign. All other TechManager logic is already correct; only the output is wrong.

**The chat listener for text params** (`TechHotbarListener.onAsyncChat`) is the wrong approach. This can be replaced with a `PlayerEditBookEvent` listener that activates only when a "param edit" book is open. The async complication goes away.

**The lack of a persistent primary UI surface** (a problem in the current design) is purely a rendering artifact, not an architectural constraint. TechSession doesn't depend on the chat panel. Replacing it doesn't touch the session model.

### Recommended refactors

**Immediate (to fix the root problem):**

1. **Replace `TechPanelBuilder` with `TechConsoleBuilder`** — renders an inventory GUI instead of a chat message. All the same data (departments, marks, params, actions) exposed through item display names and lore instead of clickable text. Click events are handled via `InventoryClickEvent` instead of command runs.

2. **Replace chat text param input with Book GUI** — when a text param is focused, give the player a writable book with current value as initial content. Handle `PlayerEditBookEvent` to commit the value. Remove the `AsyncChatEvent` interception for params.

3. **Make mark markers interactive** — register a right-click listener on TextDisplay entities that are tech-session markers. Right-clicking a marker focuses it for capture, eliminating the two-step click-chain through the Console.

4. **Add scene progress indicator to sidebar** — "Marks: 2/3" in amber, "Marks: 3/3" in green. Always visible, no navigation required.

**For Phase 2 (before building, not after):**

5. **Extend the Console to a tabbed view** — Scene tab (Phase 1) and Timeline tab (Phase 2). The tab is a row of named items at the top of the GUI. Cue list and navigation live in the Timeline tab. This avoids the "Phase 1 and Phase 2 panels are separate" confusion.

6. **Hotbar slot 7 is context-sensitive** — Mark Tool in Edit mode, GO in Preview mode. The item itself changes (different item type/name/texture) to reflect the current mode. This makes mode state physically visible in the player's hand.

**Architectural simplifications to consider:**

7. **Should Cue remain the only user-facing concept?** — Yes, for YAML authoring. No, for the console display. The console should use the show's scene and cue labels, not YAML slugs. "Site A — The Workshop" rather than "site_a". This is a display-layer translation, not a schema change.

8. **Should `PAUSE` events be visible in the console?** — Yes, with labels. The Timeline tab in the console should show PAUSE labels ("After the declaration") so Alan can jump to named moments without counting ticks. The spec already supports this (the `label:` field on PAUSE events). Wire it to the cue list display.

---

## 5. Summary

### Recommended UX Model

**The Director's Console** replaces the chat panel as the primary control surface. It is:
- An inventory GUI opened by a Baton item in the hotbar
- Persistent — stays on screen, can be closed with Escape or the Baton
- Tabbed: Scene tab (Phase 1) and Timeline tab (Phase 2)
- Self-explanatory: colored items, clear labels, visible readiness indicators

**Spatial operations stay spatial.** Mark capture is driven by walking to mark positions and right-clicking with the Mark Tool (or right-clicking the in-world marker label). No chat navigation required.

**Numeric params stay on the hotbar.** Param Dial + scroll wheel is the right model; it just needs better focus management (which param is active is shown in the sidebar, not in chat).

**Text params move to Book GUI.** No chat interception. Alan opens the Console, clicks a text param, a book opens with the current value. He edits and closes. Committed.

**The hotbar shrinks from 5 to 4 items.** Baton (console), and two spatial tools (Mark Tool, Param Dial). Phase 2 adds HOLD and the GO behavior to slot 7.

### Key Changes

**Must change immediately:**
- `TechPanelBuilder` → `TechConsoleBuilder` (inventory GUI renderer)
- Chat text param input → Book GUI
- Mark marker right-click interaction (focus on click)
- Sidebar progress indicator for mark capture completeness

**Should evolve over time:**
- Scene/cue labels in console (display names, not YAML slugs)
- Timeline tab with PAUSE labels for named navigation
- Slot 7 context-sensitivity (mode-aware item)
- Console header mode indicator (Edit vs Preview) in Phase 2

### Migration Strategy

**What can remain intact:** Everything. The migration is purely a UI surface replacement. TechSession, TechManager, PromptBook, stop-safety, and all the command/event infrastructure stay unchanged. The session model is correct.

**Step 1:** Remove `TechPanelBuilder` and replace with `TechConsoleBuilder`. Map the same data to inventory items. Wire click events. Test that all existing operations work through the new surface.

**Step 2:** Replace chat text param input with Book GUI. Remove `AsyncChatEvent` text param interception. Add `PlayerEditBookEvent` listener.

**Step 3:** Register interactive right-click on TextDisplay mark markers.

**Step 4:** Add scene readiness to the sidebar.

These are four isolated changes, any of which can be built and shipped without the others. Step 1 is the one that removes the most friction. Start there.

---

## Optional: Alternative Directions

### Alternative A — Preserve the chat panel but fix the persistence problem

Use a **persistent BossBar** as the always-visible status strip (scene name, mark progress, active departments as symbols), and keep the chat panel for the full control surface — but trigger it to re-send on a fixed timer (every 10 seconds) AND whenever state changes. The panel stays visible by staying fresh.

**Tradeoff:** Fixes the "scrolled away" problem partially, but doesn't fix the attention-split problem (Alan still has to look down at chat to interact), and the BossBar is a very limited display surface. The chat panel model is fundamentally not interactive enough for this use case. This is a patch, not a solution.

### Alternative B — Fully spatial, no inventory GUI

No Console. No chat panel. All controls are in-world. Department toggles are handled by colored marker blocks in the scene (walk up and punch to toggle). Params are edited by adjusting in-world values (numeric sliders as armor stand position → maps to param value). Scene navigation is a direction-facing + hotbar gesture.

**Tradeoff:** More immersive, closer to the "director in a scene" feeling. But significantly more complex to build and significantly harder to use for structural operations (save/discard, scene navigation, cue list). Appropriate for a fully in-game creative tool; less appropriate for a refinement tool that connects back to YAML files. The investment is not proportional to the use case.

The inventory GUI model (primary recommendation) hits the right balance: it's spatially accessible (Baton item, always at hand), Minecraft-idiomatic, requires no documentation, and doesn't compete with the world for Alan's attention except when explicitly invoked.

---

*Review complete. The architecture is strong. The surface is the problem. Fix the surface.*
