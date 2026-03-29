# ScaenaShows — Roadmap

This is a hobby project, and the best kind: one where the process is the point. We're building something genuinely good — a choreography engine for Minecraft that creates real theatrical experiences — and we're figuring out how to build it in collaboration with Claude as we go.

No dates. No deadlines. Sequence matters; schedule doesn't. When in doubt, lean into the theatre metaphor. It has held up.

---

## The Foundation

### The Engine — Complete

The v2 plugin is fully operational on Paper 1.21.x / Java 21. Everything in the engine is a **Cue** — a named, reusable, recursively nestable event container. Shows are Cues with a runtime entry point.

Known remaining items (none are blocking show work):

- `GLOW` and `CAMERA` events stubbed — TAB 5.x API coordination deferred until needed in production
- Player-facing strings are placeholder — Scaena voice pass before any public-facing launch
- `build/` and `.gradle/` still tracked in git — clean up with `git rm --cached -r build/ .gradle/` when convenient

### The Production Team — Complete

Twelve departments are at the table. Each has a knowledge base at `kb/departments/[dept-slug]/[dept-slug].kb.md` covering the department's instruments, tone translation, calibration backlog, patterns, and principles.

The KBs grow with the work. The right question when assessing a department KB isn't "is it complete" — it's "can this department make good decisions from it?"

The KB standard (what a calibration-ready KB looks like):

- Full instrument inventory documented with capability status: ✅ Verified / ⚠️ Gapped / 📋 Aspirational
- Tone Translation section written
- Calibration Backlog section — instruments and techniques the dept wants to develop mastery over
- Patterns section — named, reusable configurations with notes on storytelling effect (grows with calibration work)
- At least one YAML example per instrument
- The KB has been read and used in at least one show session

**Reference:** `kb/production-team.md` — full team roster, role definitions, and cross-department relationships.

---

## Calibration Practice

Calibration is how each department develops real mastery over its instruments — not just "does this knob work" but "what happens when we turn it this way or that way, and how precisely can we control it?"

### How it works

**1. Backlog**
Each department maintains a Calibration Backlog in its KB. It starts as an extension of the tone translation and instrument inventory work — "here's what I have, here's what I want to learn to do well with it." Items might be specific to a single instrument or technique: *nail a clean group entrance on cue*, *build a three-event sound motif that establishes unease*, *confirm the exact levitation cycle that reads as hover vs. climb.*

**2. Calibration round**
Pick one or more items from the backlog. Build a `demo.*` show that isolates and exercises those items. Alan watches in-game, takes notes, brings the findings back.

**3. Findings → Patterns**
What did we learn? Record it in the department KB. The output is a **pattern**: a named configuration (or sequence of events) that achieves a specific storytelling effect, with notes on what it accomplishes and when to reach for it. Over time, the pattern library becomes the department's creative vocabulary — richer and more precise with every round.

Calibration is iterative and has no endpoint. It just gets better.

### The demo series

`demo.*` shows are the calibration lab. They're internal tooling — never audience-facing — and live in `src/main/resources/shows/` alongside production shows.

Current demo shows:

| Show | Purpose | Status |
|------|---------|--------|
| `demo.archetype_sampler` | 13-cue emotional arc archetypes | R7 tested 2026-03-28; findings filed to KBs; R8 not yet scheduled |
| `demo.flight_modes` | Five-section flight test | Complete |
| `demo.levitate_calibration` | Levitation physics — first pass | Complete; findings in Effects KB |
| `demo.levitate_calibration_2` | Wide-range amplitude calibration | Complete; findings in Effects KB |
| `demo.levitate_calibration_3` | Hover/climb/descent confirmation | Complete; hover at 28t cycle confirmed |

---

## Calibration Status — Pattern Library by Department

*Last updated: 2026-03-28. Confirmed = tested in-game, parameters known. Proposed = named but not yet tested. Cal. rounds = dedicated demo shows run for this department.*

| Department | Confirmed | Proposed | Cal. Rounds | Status |
|------------|-----------|----------|-------------|--------|
| Camera | 4 | 0 | 0 | 📋 Patterns confirmed; backlog written; no dedicated round yet |
| Casting | — | — | 0 | ✅ Dramatic archetypes documented; no calibration queue needed |
| Choreography | 0 | 7 | 0 | 📋 Backlog written; 7 sequences proposed; no round yet |
| Effects | 2 | 0 | 3 | ✅ Most calibrated dept; hover cycle confirmed; backlog active |
| Fireworks | 0 | 6 | 0 | 📋 Backlog written; 6 arrangements proposed; no round yet |
| Lighting | 4 arc patterns | 10 | 0 | 📋 Arc vocabulary confirmed via R7; backlog written; 10 patterns proposed |
| Set | 0 | 3 | 0 | 📋 Backlog written; 3 spatial configurations proposed |
| Sound | 0 | 8† | 0 | 📋 Backlog written; beds/buttons/motifs proposed; no round yet |
| Stage Mgmt | N/A | N/A | N/A | ✅ Operational; calibrates through show builds |
| Voice | 1 framework | 5 line types | 0 dedicated | 📋 Timing modes confirmed (R7); line sequence types proposed |
| Wardrobe | 0 | 4 | 0 | 📋 Backlog written; 4 costume configurations proposed |
| Show Director | N/A | N/A | N/A | ✅ Meta/structural; calibrates through show direction |

†Sound proposed patterns: 2 beds + 3 buttons (dept KB) + 3 motifs (music-director KB).

**Reading the table:**
- ✅ Active — confirmed patterns exist or no calibration queue needed
- 📋 Proposed — backlog written, patterns named and waiting for a dedicated round
- ⚠️ Partial — some work done, backlog incomplete
- ❌ Not started — capability documented, no calibration work begun

---

## The Showcase Series

Three original shows developed in parallel. Each department calibrates its instruments in context — with real in-game feedback driving iteration — toward genuine craft. These shows are functional-first, but they should still be good.

Alan moves between shows freely at any point. What matters is that each show's current state is accurately captured in its folder at the end of every session.

### The lifecycle

```
Brief  →  In Development  →  Open
```

**Brief** — Show Director writes the brief and per-department briefings. The show folder is scaffolded. The show has a reason to exist.

**In Development** — The show iterates through Watch Rounds and Redesign Rounds. Direction tracks where it is and what's open.

**Open** — Alan declares it done. The show is frozen. It becomes part of the permanent repertoire. It doesn't change after opening.

*We'll know opening night when we're ready for it.*

### Round structure

**Watch Round**
- Alan picks a focus: which angles or themes to pay attention to this pass
- Alan watches the show in-game, takes notes indexed by cue (C1, C7, etc.)
- Notes come back to Claude; Direction assigns issues to departments
- Issues stack in the show's direction folder; departments own resolution

**Redesign Round**
- Pick a bucket of issues: one department, one theme, or whatever needs to move
- Work with the production team or an individual department to make specific changes
- Batch several changes into a single round; when ready, publish show files and head into a Watch Round

These two rounds alternate. A session may include both.

### Issue tracking

**Show-level issues** — Direction owns these. Cross-department issues are Direction's to coordinate. They live in the show's `direction/` folder.

**Department issues (cross-show)** — Filed in the department's KB, under the calibration backlog or a separate inbox note. These are patterns that keep surfacing, capabilities that need work across multiple shows, or calibration items that need dedicated rounds.

**Plugin-level issues** — Filed in `ops-inbox.md` at the repo root. This is Alan's list. It tracks Java capability gaps that need plugin-level work — not show work, not department work.

### The three shows

**showcase.01 — "The Cabinet"**
`src/main/resources/shows/showcase.01/`
*Current state: `direction/showcase.01.status.md`*

**showcase.02 — "The Long Night"**
`src/main/resources/shows/showcase.02/`
*Current state: `direction/showcase.02.status.md`*

**showcase.03 — "Welcome"**
`src/main/resources/shows/showcase.03/`
*Current state: `direction/showcase.03.status.md`*

---

## Young People's Guide to ScaenaCraft Shows

The ambitious one. Inspired by Bernstein's Young People's Concerts — a series of vignettes where the player is teleported to a distinct set for each movement. Each movement has its own look, feel, vibe, and emotional register. Every department is at the table from concept. The show demonstrates full mastery of the craft.

This begins only when the Showcases have earned it. The high bar is the point — the Showcases exist to get the team here.

`intro.young_persons_guide` is the predecessor. Rewrite its Sprite narration (SCENA-006) before YPG enters pre-production — it's a rehearsal for this show's ambition.

---

## Open Issues

| ID | Area | Priority | Description |
|----|------|----------|-------------|
| SCENA-002 | Plugin | Low | `GLOW` + TAB 5.x API coordination — defer until GLOW needed in production |
| SCENA-006 | YPG | High | `intro.young_persons_guide` Sprite narration rewrite — do before YPG pre-production |
| ~~SCENA-007~~ | ~~Effects Dept~~ | ~~Medium~~ | ~~Calibration backlog + patterns section~~ — ✅ Closed 2026-03-28. All 12 depts now have calibration backlogs or archetype documentation. |
| ~~SCENA-008~~ | ~~Stage Mgmt Dept~~ | ~~Medium~~ | ~~KB scope clarity~~ — ✅ Closed 2026-03-28. Stage Management KB v3.0 covers both SM and Production Manager roles fully. |
