---
department: Sprite Voice Director
owner: Sprite Voice Director
kb_version: 1.0
updated: 2026-03-25
---

# Sprite Voice Director — Technical Knowledgebase

> Technical reference for the Sprite Voice department. Documents what the ScaenaShows Java plugin
> can do for text delivery — chat, titles, action bar, bossbar — and how to access those capabilities
> through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §8. Sprite Voice Director`.
> ShowSprite voice characteristics are defined in `kb/departments/voice/showsprite.context.md`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| MESSAGE | Point | Send a chat message to the target audience |
| TITLE | Bar (fade_in + stay + fade_out) | Display large fullscreen text with optional subtitle |
| ACTION_BAR | Bar | Display a single line above the hotbar |
| BOSSBAR | Bar | Display a persistent bar with progress fill at the top of the screen |

---

## Capabilities & YAML Syntax

---

### MESSAGE

Sends a line of chat text to the target audience. Appears in the chat stream; persists in chat history.

```yaml
type: MESSAGE
audience: broadcast | participants | private | group_1 | group_2 | group_3 | group_4 | target
message: "<white>[Sprite] Something quiet this way comes.</white>"
```

**Formatting:** MiniMessage format is supported. Common tags:

| Tag | Effect |
|-----|--------|
| `<white>` | White text (ShowSprite standard) |
| `<gold>` | Gold text |
| `<gray>` | Gray / subdued |
| `<italic>` | Italic |
| `<bold>` | Bold |
| `<reset>` | Clear all formatting |
| `<dark_gray>` | Dark gray — barely visible, ominous |
| `<aqua>` | Cyan/aqua |
| `<light_purple>` | Lavender |

**ShowSprite convention:** Prefix all ShowSprite dialogue with `[Sprite]` in white. This is the established in-game voice pattern:
```yaml
message: "<white>[Sprite] The ceiling opens.</white>"
```

**Behavioral notes:**
- MESSAGE is the most intimate text mode — it arrives in the chat stream and can be scrolled back. Use for dialogue that the player should be able to return to.
- Multiple MESSAGE events at sequential ticks create a sense of unfolding thought. A single MESSAGE at one tick is a statement. Three messages at 20-tick intervals is a breath of three sentences.
- `private` / `target` audiences deliver the message only to the invoker. This creates personal address — "you, specifically, are seen" — that broadcast text cannot replicate.
- `group_1` / `group_2` enables split audience experiences in multi-player shows: different groups see different text at the same moment.

---

### TITLE

Displays large text centered on the screen with an optional subtitle below. Three timing parameters control the fade-in, display, and fade-out durations.

```yaml
type: TITLE
audience: broadcast | participants | private | group_1 | group_2 | target
title: "<gold><bold>You are here.</bold></gold>"
subtitle: "<gray>Or somewhere very close to it.</gray>"
fade_in: 20    # ticks to fade from transparent to full
stay: 60       # ticks the text displays at full opacity
fade_out: 20   # ticks to fade from full to transparent
```

**Behavioral notes:**
- TITLE is the cinematic mode — the screen is the stage. Reserve it for moments that deserve the full screen. Overuse deflates its impact.
- `subtitle:` is optional. A title with no subtitle feels more declarative and sparse.
- Timing is craft: `fade_in: 0, stay: 10, fade_out: 60` reads as a flash of recognition. `fade_in: 40, stay: 80, fade_out: 40` is a slow revelation that the player sees coming.
- To erase a TITLE before its `stay` timer expires, fire a new TITLE with empty strings: `title: " " subtitle: " "` — this resets the fade clock. See Gap: No TITLE_CLEAR below.
- Only two lines available: `title:` and `subtitle:`. Do not try to add a third line — there is no mechanism for it.

---

### ACTION_BAR

Displays a single line of text above the hotbar. Auto-fades approximately 2 seconds (~40 ticks) after the last update. Refreshable by firing a new ACTION_BAR before it fades.

```yaml
type: ACTION_BAR
audience: participants | private | group_1 | group_2 | target
message: "<gray>Look up.</gray>"
duration_ticks: 60   # plugin re-sends the message every 20 ticks to keep it visible
```

**Behavioral notes:**
- The plugin re-sends the ACTION_BAR every 20 ticks for `duration_ticks` to prevent it from auto-fading during an extended display.
- Subtle register — the player is reading it peripherally while watching the show. Use for ambient guidance, quiet stage directions, or status that should fade naturally when no longer relevant.
- Does not persist in chat history. If the player looks away, the text is gone. Do not use ACTION_BAR for important narrative text — use MESSAGE instead.
- Multiple sequential ACTION_BAR events at 20-tick intervals with no `duration_ticks` each will cascade naturally: each one appears and fades.

**ShowSprite use case:** `"Look up."` — a whispered stage direction, gone before it can feel like an instruction. The established in-game pattern from the archetype sampler.

---

### BOSSBAR

Displays a persistent bar at the top of the screen for the event's duration. Has a progress fill that animates from 0% to 100% over `duration_ticks`. Color-coded by emotional register. The structural layer of the text arc.

```yaml
type: BOSSBAR
title: "<gold>Section title</gold>"
color: YELLOW       # PINK | BLUE | RED | GREEN | YELLOW | PURPLE | WHITE
overlay: PROGRESS   # PROGRESS | NOTCHED_6 | NOTCHED_10 | NOTCHED_12 | NOTCHED_20
audience: broadcast | participants | private | group_1 | group_2 | target
duration_ticks: 200
fade_in_ticks: 10   # plugin animates progress 0 → 1 over this many ticks
fade_out_ticks: 20  # plugin animates progress 1 → 0 over this many ticks
```

**Color convention from the archetype sampler:**

| Color | Mood |
|-------|------|
| `WHITE` | Stillness, neutrality, the void before |
| `YELLOW` | Warmth, gentle, the everyday |
| `GOLD` / `YELLOW` | Joy, celebration |
| `GREEN` | Life, growth, the world |
| `BLUE` | Quiet, sky, distance |
| `PURPLE` | Mystery, power, the liminal |
| `PINK` | Tenderness, wonder |
| `RED` | Grief, urgency, intensity |

**`overlay:` reference:**
- `PROGRESS` — clean fill bar. Standard for most sections.
- `NOTCHED_6` / `NOTCHED_10` / `NOTCHED_12` / `NOTCHED_20` — segmented bar, n divisions. Use for structured sequences where the player should feel discrete steps.

**Behavioral notes:**
- The BOSSBAR is the structural register — it orients the player in the show's architecture. The chat carries the voice; the bossbar carries the chapter. These two layers should not compete.
- Multiple BOSSBAR events can be active simultaneously if they have different show-managed IDs. In practice, one per section is the established pattern.
- Progress fill: the bar fills from empty to full over `duration_ticks`. For a section BOSSBAR, set `duration_ticks` to match the section length. The fill becomes a visual clock.
- `fade_in_ticks` / `fade_out_ticks` animate the progress bar appearance, not the title text opacity.

**Show-level bossbar** (in show YAML, not timeline): sets a persistent bossbar for the entire show duration, independent of section bossbars.
```yaml
# In show YAML:
bossbar:
  enabled: true
  title: "<gold><bold>Show Title</bold></gold>"
  color: YELLOW
  overlay: PROGRESS
  audience: broadcast
  fade_in_ticks: 10
  fade_out_ticks: 20
```

---

## Audience Targeting — Text Modes

All four text event types share the same audience targeting system.

| Audience value | Who receives the text |
|---|---|
| `broadcast` | All online players (server-wide) |
| `participants` | All players in this show instance |
| `invoker` / `private` / `target` | Only the player who ran /show play |
| `group_1` – `group_4` | Subset of participants assigned to that group |

**`entity:spawned:Name`** — MESSAGE also accepts entity targeting for unusual effects (text appearing to originate from a specific entity). Rare but available.

---

## Text Layering — The Four Modes Together

The four text modes occupy different visual real estate and have different registers:

```
┌─────────────────────────────────────────┐
│  BOSSBAR: [Section title]    ████░░░░░  │  ← structural, persistent
│                                          │
│                                          │
│           TITLE: Big moment              │  ← cinematic, reserved
│           subtitle: context              │
│                                          │
│                                          │
│  ACTION_BAR: whispered direction   │  ← ambient, peripheral
│  [hotbar items]                         │
└─────────────────────────────────────────┘
Chat stream:
  [Sprite] Message one.
  [Sprite] Message two.                    ← intimate, persistent
```

**Design rule:** Run at most two text layers simultaneously. Bossbar + chat is the standard pair. Adding TITLE on top of both is a three-layer moment — use only for peak beats. ACTION_BAR + chat is for quiet guidance. All four at once is noise.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Voice department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `kb/departments/stage-manager.kb.md` → Active Gap Registry.

---

### Gap: No TITLE_CLEAR event

**Status:** Open. Filed in `ops-inbox.md`.

There is no way to cleanly dismiss a TITLE before its `stay` timer expires. Firing a new TITLE with empty strings resets the fade clock but does not fade smoothly — it pops.

**Impact:** Title timing must be designed upfront so early dismissal is never needed. Constrains authoring when a scene demands a title cut short by action.

**Workaround:** Fire a new TITLE with `title: " "` (a space, not empty string) and `fade_in: 0, stay: 0, fade_out: 10`. This creates a fast fade-out. Not perfectly clean but better than an empty-string pop.

**Design rule until resolved:** Write TITLE timing so the natural `stay` + `fade_out` expiry matches the intended scene beat. Don't design shows that require early title dismissal.

---

### Limitation: Two-line maximum for TITLE

`title:` (line 1) and `subtitle:` (line 2) are the only available slots. There is no third line. Do not attempt to simulate multiple lines with subtitle newlines — the rendering is unpredictable.

### Limitation: No per-player distinct TITLE text in the same event

A single TITLE event sends the same text to its entire audience. To show different titles to different groups simultaneously, fire two TITLE events with `audience: group_1` and `audience: group_2` respectively.
