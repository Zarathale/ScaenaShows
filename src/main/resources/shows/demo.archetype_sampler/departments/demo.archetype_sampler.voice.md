---
department: Sprite Voice Director
show: demo.archetype_sampler
status: active
---

# Sprite Voice Director — Archetype Sampler

## Brief Received

> ShowSprite voice throughout. Theatrical, poetic — not technical. Short messages,
> deliberate pacing. BOSSBAR provides structural layer (color-coded by mood, fill as
> visual clock). Chat is the intimate layer. No TITLE sequences — this show uses
> altitude and atmosphere to speak, not screen text.

---

## Decisions

**Decision: No TITLE events in this show.** The show speaks through environment —
light, sound, altitude, particles. Fullscreen text would flatten the experiential register
into announcement. The player should feel arrived, not told they've arrived.

**Decision: BOSSBAR as structural layer only.** Each section has its own bossbar with
color-coded mood. The bossbar title is brief — 1–3 words, not narration. The progress
fill is the visual clock for each section. The player knows where they are without being
directed.

**Decision: Chat messages carry the voice — spare and deliberate.** Three or fewer
messages per section. Each message lands and ends. White text, `[Sprite]` prefix throughout.
Messages are timed to visual moments, not narrated on top of them — the sound or particle
effect fires first, then the word arrives to name what the player already felt.

**Decision: C2 text capped at three lines.** R6 had text stacking that competed with the
atmospheric opening. C2 is the first voice moment; it must breathe. Three messages at
deliberately spaced intervals is the maximum.

**Decision: ACTION_BAR used once — C11 "Look up."** This is the established in-game
pattern. The action bar is a whispered stage direction, peripheral and gone before it
feels like an instruction. Reserved for a single moment of spatial cuing when the player
needs to know where to look before the lift.

---

## Voice Arc

| Section | BOSSBAR | Chat messages | Register |
|---------|---------|---------------|----------|
| C1 | WHITE — "Houselights Down" | PRE: brief welcome + version ID | Preshow — stage setting |
| C2 | WHITE — "Stillness" | 3 lines: arrival, breath, waiting | The opening — intimate |
| C3 | YELLOW — "Warmth" | 1–2 lines: warmth acknowledged | Gentle confirmation |
| C4 | BLUE — "The Cool" | 1 line: tonal shift noted | Quiet contrast |
| C5 | PURPLE — "Below" | 1 line: cave sound named | Atmospheric, below |
| C6 | BLUE — "Ascending" | None (deliberate silence) | Silent foreshadowing |
| C7 | RED — "Arrival" | 1 line: arrival named after the lift | Thunder speaks first |
| C8 | YELLOW — "Joy" | 1 line: joy confirmed | Elevated observer |
| C9 | GREEN — "The World" | 1 line: descent as return | Inside the fireworks |
| C10 | GREEN — "Building" | 1–2 lines: ramp acknowledged | Growing momentum |
| C11 | PURPLE — "The Peak" | ACTION_BAR: "Look up." → 1 chat message | Pre-finale cue |
| C12 | BLUE — "Wonder" | 1 line: the quiet before the peak | Held breath |
| C13 | WHITE — "Coda" | 1 line: close | Gentle resolution |

---

## Notes

**R5:** Added per-cue BOSSBAR events C1–C13, color-coded by mood, progress bar fills over
section duration. This was the primary structural change that let chat messages stop carrying
orientation weight — the bossbar handles "where are we," chat handles "what does it feel like."

**R6:** Text stacking identified as a problem. Multiple overlapping messages in C2 competed
with the atmospheric opening. Capped to three lines with deliberate spacing.

**R7:** C1 now includes explicit day-to-night dimming. The PRE message (show opening) fires
during this transition — the player is reading while the light is changing. This double-action
(text + world change) is intentional: the message is part of the houselights-going-down moment,
not separate from it.

**Watch question (R7):** Does C2's silence-then-voice pacing feel like a considered breath,
or is the delay between opening and first words too long? The show's first chat message should
feel like something finally said after a moment of gathering — not like the system loading late.
