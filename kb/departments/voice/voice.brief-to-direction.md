---
department: Sprite Voice Director
owner: ShowSprite
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Voice — Brief to Direction

> ShowSprite's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for MESSAGE, TITLE, ACTION_BAR, and BOSSBAR events
> lives in `kb/departments/voice/voice.kb.md`. ShowSprite's persona — voice characteristics,
> grammar rules, established reference lines, rhythm patterns, and what to avoid — is in
> `kb/departments/voice/showsprite.context.md`. Read both before authoring any narration.

---

## Standing Requirements

*What Sprite cannot start without. No narration is authored until these are resolved.*

**Text density preference: talkative or listening show?** One answer to this question calibrates the entire voice arc. A talkative show means more messages at closer intervals, more active presence from Sprite, warmer and more frequent lines. A listening show means Sprite speaks rarely, gives more space to what the other departments are doing, and trusts the player to feel without being told. Sprite has no default here — both are valid — but it needs Direction's answer before writing a word.

**Timing mode for each major physical event: before, with, after, or silent.** This is a Show Director call, not a Voice default, and the R7 debrief confirmed that leaving it unspecified produces narration that lands at wrong moments. For each significant physical beat in the show — a levitation lift, a performer entrance, a fireworks burst, a world transformation — Direction specifies whether Sprite speaks before it (foreshadowing), with it (reinforcing), after it (interpreting), or not at all (silent). Sprite cannot author a narration arc without these timing decisions in the brief.

**Any sections that should be deliberate Voice silence.** Silence is a Voice instrument. A gap in the text arc is either authored intentional silence or an oversight — the difference is whether Voice marked it. If Direction knows there are beats where Sprite should stay completely quiet regardless of what's happening, name those sections in the brief. Sprite marks them "deliberate Voice silence — do not fill" in the run sheet.

**The show's opening register.** How does Sprite address the player at the very start? The first line sets the relationship — guide, witness, companion, absent presence, oblique narrator. It also establishes how formal or intimate the show's language is. That contract should come from the brief, not from Sprite's improvisation.

---

## Intake Questions

*Questions Sprite will ask at the intake meeting or in the brief. These are the ones it asks before writing a single line.*

- What is the player's relationship to Sprite in this show — guide who knows the plan, witness alongside the player, or observer who barely speaks?
- How much of this show is meant to be felt without words? Some shows are 90% what the other departments do; Sprite's restraint is the contribution. Some shows need language to anchor the experience. Which is this?
- Is there a single line or moment that carries the most emotional weight in the show? If so, where in the arc does it fall, and is that moment meant to be text-forward (words carry it) or text-adjacent (a line that arrives alongside something else landing)?
- Are there any dark sections — scenes at midnight or in enclosed spaces — where the player's screen will be significantly dimmed? Chat text (MESSAGE) can be missed in darkness; Action Bar and TITLE survive darkness better.
- Does Direction have any specific lines already in mind? Even a phrase, a single word, or a tone — "something about doors" — that would help Sprite find the show's language.

---

## Requests

*Nice-to-haves. These prevent the most common Voice-Sound and Voice-Effects conflicts.*

- Knowing Sound's audio arc around each major Voice moment. A significant sound hit and a text line that fire at the same tick compete for the player's attention — the player hears the sound, looks up, and misses the line. Standard offset: 5–10 ticks between them, unless the simultaneity is intentional (a chime announcing a line should be simultaneous; independent events should be staggered). Brian and Sprite should each know what the other is doing at high-impact moments.
- Effects' timeline for major player orientation changes. Any moment where the player's view changes dramatically — levitation peak, PLAYER_TELEPORT, PLAYER_SPECTATE transition — creates a disorientation window of roughly 40–80 ticks where text will be missed. Sprite goes quiet during those windows. Knowing Effects' timeline lets Voice leave the right gaps rather than guessing.
- Wardrobe's major visual beats — particularly invisible-body technique moments and costume transformations. These are natural Sprite reaction moments, but naming them (or deliberately staying silent) should be a Voice decision, not an oversight. Sprite needs to know when the big visual events are so it can choose consciously.

---

## Voice's Palette

Sprite's four instruments:

**MESSAGE (chat)** — text in the chat stream. Intimate, persistent, scrollable. The player can return to it. Use for lines the player should be able to re-read, for dialogue that unfolds across several beats, for text that should feel like it arrived rather than was displayed. The standard Sprite format: `<light_purple>[Sprite]</light_purple><white> The line.</white>`

**TITLE (fullscreen)** — large centered text with optional subtitle. Cinematic and reserved. Use only for peak moments that deserve the full screen. Overuse deflates it. Two lines maximum (title + subtitle). Timing is craft: fade_in, stay, and fade_out are all expressive parameters, not technical defaults.

**ACTION_BAR (above hotbar)** — a single peripheral whisper. Auto-fades after ~2 seconds unless the plugin refreshes it. Used for ambient guidance, quiet stage directions, or status that should disappear naturally. The established in-game example: `"Look up."` — present for a moment, gone before it feels like an instruction.

**BOSSBAR (top of screen)** — structural and persistent. The chapter-marking layer. Orients the player in the show's architecture; fills like a progress clock over the section's duration. Bossbar + chat is the standard pairing. Adding TITLE on top makes a three-layer moment — use only at genuine peaks. All four layers simultaneously is noise.

**Silence as instrument** — authored gaps in the text arc. Marked explicitly in the run sheet so they're not mistaken for missing content. Sprite's restraint in a moment where other departments are carrying the scene is a contribution, not an absence.

For complete YAML syntax, audience targeting, and the full text-layer visual diagram, see `voice.kb.md`. For the grammar rules, rhythm patterns, reference lines, and persona, see `showsprite.context.md`.

---

## What Voice Can and Can't Do

**Verified and working:**
- All four text modes (MESSAGE, TITLE, ACTION_BAR, BOSSBAR) with full MiniMessage formatting
- All audience targeting modes (broadcast, participants, private, group_1–4, individual entity-targeted MESSAGE)
- Show-level BOSSBAR (set in show YAML, persists for the full show duration)
- BOSSBAR stop-safety cleanup on `/show stop`

**Current gaps — flag at intake if relevant:**
- TITLE cannot be dismissed cleanly before its `stay` timer expires. Title timing must be designed upfront so early dismissal is never needed. Workaround exists (`title: " "`, `fade_in: 0, stay: 0, fade_out: 10`) but is imperfect.
- No scrolling or paginated text. Everything is immediate, single-display.
- TITLE stay and fade timings are authored in ticks — the duration must match the scene beat, because there's no mid-show adjustment.

---

## Document Status

*First pass — 2026-03-28*

Core structure is complete. What's still needed:

- The four timing modes (before / with / after / silent) are now named and their importance to show direction is documented. What's not yet here: examples of how these modes appear in actual show briefs — the templated language a Show Director would write when specifying voice timing for a given beat. That will develop show by show through the Showcase Series.
- The proposed line sequence types (`line.opening`, `line.transition`, `line.arrival`, `line.silence`, `line.close`) from the calibration backlog are named in `voice.kb.md` but not yet confirmed. Once at least one example of each exists in a show, this document should reference them as vocabulary the Director can use when briefing Voice.
- Sprite's voice characteristics are fully defined in `showsprite.context.md` but not summarized here. A short "here is Sprite's voice in four sentences" section would help Direction write briefs that work with the persona rather than against it.
