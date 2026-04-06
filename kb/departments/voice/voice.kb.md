---
department: Sprite Voice Director
owner: ShowSprite
kb_version: 2.3
updated: 2026-04-06
notes: >
  v2.0: Full instrument inventory (MESSAGE, TITLE, ACTION_BAR, BOSSBAR), audience targeting,
  text layering, tone translation, department principles, capability status table.
  v2.1: owner corrected to ShowSprite. Broken stage-manager.kb.md path fixed.
  Folder migration to kb/departments/voice/voice.kb.md.
  v2.2: Reconciliation pass against production-team.md and showsprite.context.md.
  [Sprite] prefix color corrected — tag is light_purple, text is white.
  v2.3: All MiniMessage format tags replaced with & color codes (server standard).
  Text Formatting Reference section added with full color/format cheat sheet including
  server-specific &y (wavy) and &u (rainbow). spec.md §6.1 examples corrected in same pass.
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

## Text Formatting Reference

All text fields in Voice events use `&` color codes. `&r` resets all formatting to default. Codes can be combined: `&6&l` = bold gold.

**Colors:**

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&8` | Dark gray |
| `&1` | Dark blue | `&9` | Blue |
| `&2` | Dark green | `&a` | Green |
| `&3` | Dark aqua | `&b` | Aqua |
| `&4` | Dark red | `&c` | Red |
| `&5` | Dark purple | `&d` | Light purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

**Formatting:**

| Code | Effect |
|------|--------|
| `&k` | Obfuscated (scrambled) |
| `&l` | **Bold** |
| `&m` | ~~Strikethrough~~ |
| `&n` | Underline |
| `&o` | *Italic* |
| `&r` | Reset all |

**Server-specific extras:**

| Code | Effect |
|------|--------|
| `&y` | Wavy text |
| `&u` | Rainbow text |

**ShowSprite convention:** `[Sprite]` tag in `&d` (light purple), line text in `&f` (white):
```
&d[Sprite]&f The ceiling opens.
```

---

## Capabilities & YAML Syntax

---

### MESSAGE

Sends a line of chat text to the target audience. Appears in the chat stream; persists in chat history.

```yaml
type: MESSAGE
audience: broadcast | participants | private | group_1 | group_2 | group_3 | group_4 | target
message: "&d[Sprite]&f Something quiet this way comes."
```

**ShowSprite convention:** `[Sprite]` tag in `&d` (light purple), line text in `&f` (white). This is the canonical two-color format established in `showsprite.context.md §What ShowSprite Is`:
```yaml
message: "&d[Sprite]&f The ceiling opens."
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
title: "&6&lYou are here."
subtitle: "&7Or somewhere very close to it."
fade_in: 20    # ticks to fade from transparent to full
stay: 60       # ticks the text displays at full opacity
fade_out: 20   # ticks to fade from full to transparent
```

**Behavioral notes:**
- TITLE is the cinematic mode — the screen is the stage. Reserve it for moments that deserve the full screen. Overuse deflates its impact.
- `subtitle:` is optional. A title with no subtitle feels more declarative and sparse.
- Timing is craft: `fade_in: 0, stay: 10, fade_out: 60` reads as a flash of recognition. `fade_in: 40, stay: 80, fade_out: 40` is a slow revelation that the player sees coming.
- To erase a TITLE before its `stay` timer expires, use `TITLE_CLEAR` — a dedicated event that cleanly dismisses the title with a controlled fade-out (OPS-016, shipped). `fade_out` field controls the wipe speed.
- Only two lines available: `title:` and `subtitle:`. Do not try to add a third line — there is no mechanism for it.

---

### ACTION_BAR

Displays a single line of text above the hotbar. Auto-fades approximately 2 seconds (~40 ticks) after the last update. Refreshable by firing a new ACTION_BAR before it fades.

```yaml
type: ACTION_BAR
audience: participants | private | group_1 | group_2 | target
message: "&7Look up."
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
title: "&6Section title"
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
  title: "&6&lShow Title"
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

### TITLE_CLEAR — ✅ Resolved (OPS-016, shipped)

`TITLE_CLEAR` is a first-class event type. It cleanly dismisses an active TITLE with a
controlled fade-out — no workaround needed.

```yaml
type: TITLE_CLEAR
audience: participants
fade_out: 10    # ticks to fade the title out
```

`fade_in` and `stay` are both set to zero internally; `fade_out` controls the wipe speed.
Fire it at whatever tick you want the title to begin disappearing.

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

*With Stage Manager:* BOSSBAR events register with the show's active bossbar list and are cleaned up on `/show stop` via stop-safety. No extra cleanup needed. TITLE_CLEAR (OPS-016) handles early title dismissal cleanly — no workaround required.

**Escalation discipline:** Voice resolves text mode, timing, and narration choices independently. Voice escalates when: (1) the tone phrase in the brief is unclear enough that Sprite's degree of presence is genuinely ambiguous — ask the Director before writing any lines; (2) a text moment conflicts with another department's key beat and neither can easily move — bring to Stage Manager; (3) a line of narration implies a story decision that wasn't in the brief — surface it to the Director before authoring it in.

---

## Calibration Backlog

---

### Timing modes — early / with / after / silent
**Source:** `demo.archetype_sampler` R7 debrief, 2026-03-28

**Finding:** The archetype sampler showed the full range of voice timing approaches but without deliberate directorial placement. "Some timings are wonky." The show demonstrated timing modes effectively (that was its purpose), but suffered for lack of per-section timing direction.

Sprite needs to internalize four timing modes and understand that the correct mode for any given effect is a **Show Director call**, not a Voice default:

- **Early** — Sprite speaks before the effect happens. Announces or foreshadows. Risk: deflates the moment by naming it before it lands. Use sparingly and intentionally.
- **With** — Sprite speaks simultaneously with the effect. The words and the physical event reinforce each other. Works best when language and sensation share the same emotional register.
- **After** — Sprite speaks after the effect. The player has the experience first; language arrives as interpretation. Often the right choice for physical moments — let the body lead.
- **Silent** — Nothing. The effect needs no commentary. Voice restraint here is a contribution, not an absence.

**The Director's obligation:** Before any voice lines are authored for a show, the Show Director must specify the timing relationship between Sprite and each major physical event. "Voice before/with/after/silent the C7 lift" is a brief-level decision. If it's not in the brief, Voice must ask before writing.

**Not every moment needs commentary.** The archetype sampler over-commented. Voice should default to less speech, not more, and add language only when it serves something the other departments can't do on their own.

---

### Proposed line sequence types — 📋 Proposed
**What these are:** Named line roles that the Show Director can reference in a brief ("I need an opening address and a transition line before C4"). Not templates — Sprite writes to these roles for each show. Having the vocabulary makes briefing easier.

- **`line.opening`** — the show's first address to the player. Establishes register and presence. Can be early (before anything happens) or with (the world starting while Sprite speaks). Not an announcement — an arrival.
- **`line.transition`** — bridge between major sections. Acknowledges the shift without explaining it. Usually short. Gives the player permission to change what they're feeling.
- **`line.arrival`** — accompanies or follows a dramatic entrance (mob, effect, or the player themselves being moved). Depends on timing direction from the Director (before/with/after the entrance event).
- **`line.silence`** — the authored nothing. `<dark_gray>...</dark_gray>`. See `still.message.breath`. Not used by default — only when the Director has specified silence and Sprite's restraint is the contribution.
- **`line.close`** — the show's final address. Lands after all physical events have resolved. The room going quiet. Sprite's last word is never an explanation.

**Confirmed when:** Each line type has at least one tested example per show, with Director's timing mode specified.

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
| BOSSBAR start_progress / end_progress / static mode | ⚠️ Gapped | Always animates 0→1→0; no fixed start/end point. OPS-043 filed. |
| BOSS_HEALTH_BAR — entity-linked bossbar | ✅ Verified | OPS-026 shipped. Progress reflects live entity HP. Fields: target, title, color, overlay, audience, death_line, victory_cue. Owned by Voice; authored for combat/boss encounters. |
| TITLE_CLEAR — dismissing a TITLE early cleanly | ✅ Verified | OPS-016, shipped. `fade_out` field controls wipe speed. Clean dismiss, no workaround needed. |
| Per-player distinct ACTION_BAR in a single event | 📋 Aspirational | One text per event; use separate events with `audience: group_N` to target subsets |
| Scrolling / paginated text | 📋 Aspirational | No multi-page text event; not implemented or filed |
