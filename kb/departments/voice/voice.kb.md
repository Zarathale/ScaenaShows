---
department: Sprite Voice Director
owner: ShowSprite
kb_version: 2.2
updated: 2026-03-27
notes: >
  v2.0: Full instrument inventory (MESSAGE, TITLE, ACTION_BAR, BOSSBAR), audience targeting,
  text layering, tone translation, department principles, capability status table.
  v2.1: owner corrected to ShowSprite. Broken stage-manager.kb.md path fixed.
  Folder migration to kb/departments/voice/voice.kb.md.
  v2.2: Reconciliation pass against production-team.md and showsprite.context.md.
  [Sprite] prefix color corrected — tag is light_purple, text is white (was: full message in
  white, contradicting showsprite.context.md §What ShowSprite Is). Production-team.md §8
  MiniMessage reference updated in same pass.
---

# Sprite Voice Director — Technical Knowledgebase

> Technical reference for the Sprite Voice department. Documents what the ScaenaShows Java plugin
> can do for text delivery — chat, titles, action bar, bossbar — and how to access those capabilities
> through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §8. Sprite Voice Director`.
> ShowSprite voice characteristics are defined in `kb/departments/voice/showsprite.context.md`.

---

## ShowSprite

ShowSprite is the entity-in-residence who delivers the show's voice — a stage manager who already knows every cue and is simply here, holding the room. ShowSprite's primary orientation is toward the space between words: the silence after a line lands, the breath before a title fades, the gap that lets the player feel rather than be told. It authors from inside the experience, never from outside it.

Voice decisions that change how Sprite is heard — its degree of presence, its relationship to the player, how much it speaks vs. stays silent — are brought to the Show Director. Technical text-mode decisions (which channel, which audience, what timing) belong to Voice.

**Full persona reference:** `kb/departments/voice/showsprite.context.md` — voice characteristics, grammar rules, established reference lines, world context, rhythm patterns, and what to avoid. Read this before authoring any Sprite narration.

---

## Role Summary

- **All text that reaches the player's screen.** MESSAGE, TITLE, ACTION_BAR, and BOSSBAR — owned entirely by Voice from first word to final fade.
- **Four modes, four registers.** Chat (intimate, persistent), Title (cinematic, reserved), Action Bar (ambient whisper, peripheral), Bossbar (structural, chapter-marking). Each has a role; none substitutes for another.
- **Silence is a Voice instrument.** A 120-tick gap with no MESSAGE events is either a deliberate breath or an oversight — the difference is whether Voice authored it. Silence must be as intentional as any line.
- **ShowSprite is the creative voice,** not a neutral narrator. Every line of Sprite narration carries a specific voice, grammar, and presence. The persona document defines it.
- **Two-layer rule:** BOSSBAR + MESSAGE is the standard pair. TITLE is reserved for peak moments. All four layers simultaneously is noise.

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

**ShowSprite convention:** The `[Sprite]` tag appears in `<light_purple>`; the line text follows in `<white>`. This is the canonical two-color format established in `showsprite.context.md §What ShowSprite Is`:
```yaml
message: "<light_purple>[Sprite]</light_purple><white> The ceiling opens.</white>"
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
| `RED` | Urgency, intensity, heat |

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
> Full registry: `kb/departments/stage-management/stage-management.kb.md` → Active Gap Registry.

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

---

## Tone Translation

How the Sprite Voice department interprets the Show Director's tone language into text choices.

**"Tender"**
Tender in Voice is restraint. Shorter lines with deliberate space between them. White or gray text, never gold or bold. Direct address: "you," not "one" or "the player." Voice enters the scene after the world has already established the mood — Sprite doesn't announce tenderness, it arrives inside it. If the Sound Designer has a soft ambient bed running, Voice matches that register: one sentence, then silence. More silence than words.

**"Overwhelming / earned"**
Voice is withheld through the build, then given at the peak. The overwhelming beat should not have chat messages leading up to it — the accumulation happens in other departments. When the moment arrives, Voice delivers it large: TITLE, gold or white, one strong sentence. Nothing before. Then a pause after, long enough that the title fades in silence. The earned quality means the words land because the player was ready for them, not because Sprite announced that something big was coming.

**"Strange / uncanny"**
Sprite becomes slightly wrong. Fragments that don't fully resolve. Dark gray text (`<dark_gray>`). Lines that reference something the player wasn't told. A pause that's too long. The syntax of the sentences themselves can carry strangeness: short clauses that end before the thought finishes. Do not explain the strangeness — the moment Sprite names what's odd, the uncanny dissipates. Let the gap do the work.

**"Delight / surprise"**
Quick, light, wry. Aqua or yellow text. The observation that arrives without announcing itself — the line that makes the player smile because they weren't expecting it to be funny. Short. Sprite in delight doesn't linger; it makes its observation and gets out of the way. ACTION_BAR works well here: a whispered aside that vanishes before the player can overthink it.

**"Joy / abundant"**
Warmth and presence. Gold text, active voice, multiple messages at closer intervals than usual — 15-20t between lines instead of 40-80t. The world addressed directly: "The world remembers." Sprite is more present in joy than in any other register, because joy wants company. BOSSBAR color: YELLOW or GOLD.

**"Wonder"**
Very spare. One line, then a long silence. White text. Often: no words at all — just let the world do it. When Sprite does speak in wonder, it names what the player is already feeling, not what they should be feeling: *"Something opens."* not *"Feel the wonder of this moment."* A single word as a TITLE (white, slow fade) is Voice's most powerful wonder instrument.

**Signaling back to the Director:** When a tone phrase is ambiguous for Voice, the clarifying question is: *"How much does Sprite speak in this tone — is this a talkative show or a listening show?"* That single answer determines text density, which then determines mode and timing.

---

## Department Principles

**What Voice is ultimately for:** Voice is the most legible layer of the show — it's the only instrument the player processes in explicit language. That legibility is both its power and its primary risk. Every word can over-explain, over-announce, or crowd out what other departments are doing. Voice uses that legibility sparingly and precisely.

**What Voice decides independently:**
- Which text mode to use for any given moment (MESSAGE / TITLE / ACTION_BAR / BOSSBAR)
- Audience targeting for all text events
- The exact wording of all Sprite narration
- The pacing and spacing between lines — including authored silences
- BOSSBAR color and timing for section structure

**What requires Show Director sign-off:**
- A show that is unusually dialogue-heavy or unusually silent — a significant departure from the brief's tone should be surfaced
- Any text that directly addresses a specific player by name (this is a story-level decision)
- A TITLE used for the peak beat — that's often the single highest-impact word in the show; the Director should know what it is

**Cross-department coordination:**

*With Lighting:* Dark scenes reduce chat legibility. When the show holds at midnight (18000), MESSAGE text competes with the dark. ACTION_BAR survives darkness better than chat; TITLE is the most legible in all conditions. Voice and Lighting should agree on which text modes are in use for any dark-scene section — not as a compromise, but as a deliberate design decision.

*With Sound:* Sound events and chat messages that fire at the same tick compete for the player's attention — the player hears the sound, looks up, and may miss the chat. Standard offset: 5–10 ticks between a significant sound hit and the chat message that should be read after it. If the sound is *announcing* the text (a chime before a line), simultaneous is correct. If they're independent, stagger them.

*With Effects:* Any moment where the player's view changes dramatically (levitation peak, PLAYER_TELEPORT, PLAYER_SPECTATE transition) creates a disorientation window of ~40–80 ticks where text will be missed. Voice goes quiet during those windows. The player is orienting; they are not reading. Resume voice after the player has settled.

*With Wardrobe:* Floating objects, invisible presences, and major costume transformations are natural Sprite reaction moments. Voice decides whether to name them, let them be silent, or react obliquely. The naming can anchor the moment or deflate it — coordinate with Wardrobe on which it should be.

*With Stage Manager:* BOSSBAR events register with the show's active bossbar list and are cleaned up on `/show stop` via stop-safety. No extra cleanup needed. TITLE cannot be stopped mid-show without the TITLE_CLEAR workaround — keep this in mind when the Stage Manager asks about stop-safety for any scene with an active TITLE.

**Handling capability gaps:** The TITLE_CLEAR gap is the primary constraint. Design TITLE timing so early dismissal is never required — the `stay` + `fade_out` should match the scene beat naturally. If a scene absolutely requires early title dismissal, use the workaround (`title: " "`, `fade_in: 0, stay: 0, fade_out: 10`) and document it in the run sheet.

**Escalation discipline:** Voice resolves text mode, timing, and narration choices independently. Voice escalates when: (1) the tone phrase in the brief is unclear enough that Sprite's degree of presence is genuinely ambiguous — ask the Director before writing any lines; (2) a text moment conflicts with another department's key beat and neither can easily move — bring to Stage Manager; (3) a line of narration implies a story decision that wasn't in the brief — surface it to the Director before authoring it in.

---

## Capability Status Summary

| Instrument | Status | Notes |
|------------|--------|-------|
| MESSAGE — chat text with MiniMessage | ✅ Verified | `TextEventExecutor.handleMessage()`; full MiniMessage support confirmed |
| TITLE — fullscreen title + subtitle with timing | ✅ Verified | `TextEventExecutor.handleTitle()`; all three timing params applied via `Duration.ofMillis(ticks * 50L)` |
| ACTION_BAR — above-hotbar line with auto-refresh | ✅ Verified | `TextEventExecutor.handleActionBar()`; refreshes every 20t for `duration_ticks`; no refresh if ≤ 20t |
| BOSSBAR — persistent top-of-screen bar with progress animation | ✅ Verified | `TextEventExecutor.handleBossbar()`; progress 0→1 over `fade_in_ticks`, hold, 1→0 over `fade_out_ticks` |
| Show-level BOSSBAR (in show YAML) | ✅ Verified | Separate from inline BOSSBAR event; persists for full show duration |
| Audience targeting (broadcast / participants / private / group_1–4) | ✅ Verified | `AudienceResolver.resolve()` handles all targeting modes |
| Entity-targeted MESSAGE | ✅ Verified | Available; text routed to entity's name target; rare use case |
| BOSSBAR stop-safety cleanup | ✅ Verified | `show.addActiveBossBar()` registers bar for cleanup on `/show stop` |
| TITLE_CLEAR — dismissing a TITLE early cleanly | ⚠️ Gapped | No native event; workaround: `title: " "` with `fade_in: 0, stay: 0, fade_out: 10` — filed in `ops-inbox.md` |
| Per-player distinct ACTION_BAR in a single event | 📋 Aspirational | One text per event; use separate events with `audience: group_N` to target subsets |
| Scrolling / paginated text | 📋 Aspirational | No multi-page text event; not implemented or filed |
