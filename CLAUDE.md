# ScaenaShows — Claude Project Hub

> This file is the authoritative briefing for Claude on the ScaenaShows project.
> Read this first at the start of every session. It tells you where we are, what phase is active, and what to do next.

---

## Project Identity

**ScaenaShows** is a data-driven choreography engine for Minecraft Paper servers.
**Owner:** Alan (alytle@thearcoregon.org)
**Repo:** ScaenaShows (this repo)
**Branch:** `feature/ai-show-generation` ← active development branch
**Design spec:** `docs/spec.md` — the authoritative source of truth for YAML schema.
**Build readiness audit:** `docs/preview-audit.md` — pre-build gaps (Phase 1 era, mostly resolved).

---

## Current Phase

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Complete (2026-03-24) | Java plugin rewrite (Cue schema) — fully working |
| Phases 2–6 | ⏭ Skipped | ScaenaComposer UI/RCON — deferred indefinitely |
| **Phase 7** | 🟢 Active | ShowSprite — AI-driven show generation (no UI required) |
| **Phase 8** | 🟢 Active | Cue library + tags — seeded starter set |

## Session State — 2026-03-24

**Where we stopped:** Archetype sampler R6 complete. Calibration v3 debrief done. Ready to build 2.8.1 and run R6.

**What was accomplished this session:**
- `docs/showsprite.context.md` — finalized and stable ✅
- `docs/cue-library-survey.md` — baseline survey written; gap analysis complete
- Cue library: **8 → 25 production cues**
  - All 8 original cues tagged retroactively
  - New families: `coda.*` (4), `ramp.*` (4), `grief.*` (4), `world.*` (4), `fx.lift_to_height`
- `demo.archetype_sampler` — first in-game test complete (R4). Consolidated feedback debrief applied.
- **Revision 5 (2026-03-24):**
  - Per-cue BOSSBAR events (C1–C13), color-coded by mood, progress bar fills over section
  - WEATHER clear forced at T=0
  - Levitation now primary staging: C2 → C3/C4 elevated → C9 burst → C11 pre-lift → C12 full lift
  - `atmos.ambient.ember_drift` v3: particle count 5→8, y-spread taller
  - `mood.arrival` v2: dragon growl → rolling distant thunder (lower pitch)
  - C11: "Look up." → "[Sprite] The ceiling opens." + levitation begins at section start
  - All ↳ section-label messages removed; PRE message shortened with version ID
- `docs/demo.archetype_sampler.runsheet.md` — updated for Revision 5
- **Aerial calibration work (v2.8.0):**
  - `PLAYER_FLIGHT` Java event type added (EventType, PlayerFlightEvent, PlayerEventExecutor, RunningShow, EventParser, ExecutorRegistry, ShowManager applyStopSafety)
  - `fx.levitate_pulse.yml` and `fx.levitate_surge.yml` — new cues (rhythmic pulse patterns)
  - `demo.flight_modes.yml` — 5-section flight test show + run sheet
  - `demo.levitate_calibration.yml` — first calibration show (v1, run and data collected)
  - **Calibration v1 findings:** slow_falling ≈ 2 b/s (much faster than assumed); 5 pulses of amp 0–2 all cluster at ~2–3s; only amp 9 burst distinct. Pulse aesthetics strong.
  - `demo.levitate_calibration_2.yml` — redesigned 6-section wide-range calibration (A=1 pulse, B=amp1×60t, C=amp3×120t, D=amp9×240t, E=20 rhythmic pulses, F=40 rapid micro-pulses)
  - `docs/demo.levitate_calibration_2.runsheet.md` — run sheet with 6-row data table and derived-constant math
  - `fx.levitate_pulse.yml` and `fx.levitate_surge.yml` — header comments updated to reflect actual physics (climb, not maintenance; slow_falling ~2 b/s)
  - `demo.levitate_calibration_3.yml` — 8-section quasi-hover search (4740t ~4min); **confirmed hover at Sec 2 (A=0 lev=20t gap=8t)**
  - `docs/demo.levitate_calibration_3.runsheet.md` — run sheet with balance-point derivation math
  - **Calibration v3 findings (debrief 2026-03-24):**
    - Sec 2 (A=0 lev=20t gap=8t, cycle=28t) = **clean hover** ✅
    - Sec 8 (A=1 lev=20t gap=24t, cycle=44t) = slow controlled descent — "blood pressure release" feel, liked aesthetically
    - Confirmed: HOVER=28t cycle, CLIMB=24t cycle, RELEASE=44t cycle
  - **`demo.archetype_sampler` R6 (this session, v2.8.1):**
    - Full aerial rewrite — player aloft from C2 through end of show
    - 68 inline levitation events across 8 sections using calibrated patterns
    - C9 redesigned: player descends THROUGH fireworks from above via pressure-release
    - C9 firework y_offsets redesigned (25/18/10/22/14) to wrap around 15-20b altitude
    - One persistent slow_falling (3200t from T=380) as whole-show baseline
  - `build.gradle.kts` → **v2.8.1** (show-content patch)

**Immediate next step (other machine):**
Build 2.8.1 and run `demo.archetype_sampler` (R6). `/show play demo.archetype_sampler`
Key observations: Is the hover stable through each section? Does C9 feel like being inside/descending through the fireworks? Does C7 lift feel dramatic? Overall: does being aloft the whole show change the emotional arc?

**After R6 debrief:**
Author `fx.hover.sustained` and `fx.hover.descend` reusable cues using calibrated parameters. Retune `fx.levitate_pulse` and `fx.levitate_surge`. Begin `intro.young_persons_guide` voice pass.

> Alan's decision: bypass Phases 2–6 (the web interface). Claude can already generate shows
> directly as YAML. The goal is to get to high-quality, emotionally resonant show authoring
> ASAP — without waiting for a GUI.
>
> See `ROADMAP.md` for the revised phase plan.

---

## Phase 1 — What Was Built (Reference)

The plugin is working and deployed. Key facts for future Java work:

- **Build:** `./gradlew shadowJar` → `build/libs/ScaenaShows-<version>.jar`
- **Deploy:** Stop server; replace JAR; delete `plugins/ScaenaShows/cues/` and `plugins/ScaenaShows/shows/` so bundled YAMLs re-extract; start server.
- **Commands:** `/show list|play|stop|stopall|reload`
- **YAML loading:** Plugin scans JAR via `JarFile(getFile())` and calls `saveResource()` for each bundled YAML on first run
- **Known bugs fixed:** `CueRefEvent` reads `cue_id` (not `cue`); `resolveEntities()` handles `"participants"` and `"broadcast"`; `smoothMovePlayer` uses absolute interpolation
- **Bundled shows:** `test.showcase.full`, `intro.scaena_sprite`, `intro.young_persons_guide`
- **Bundled cues:** `atmos.*`, `fx.*`, `mood.*`, `overture.theme_teaser`

---

## Phase 7 + 8 — Active Work

### The Core Insight

Alan's critique of the current demo shows: they work when they have **Scaena voice** — atmospheric, poetic narration that sets tone. They fail when they become a mechanical feature listing. The goal for this phase is to make Claude an expert **show author**, not just a YAML generator.

This means:
- Claude understands show *dramaturgy* (arc, pacing, emotional beats)
- Claude authors shows with intentional voice, not feature demonstrations
- Cues in the library serve **emotional and atmospheric purposes**, not technical categories
- Shows feel like experiences, not tours of the engine

### What Phase 7/8 Means Without the UI

Without ScaenaComposer, the authoring workflow is:
1. Alan describes a show concept (mood, occasion, duration, tone)
2. Claude authors the full YAML directly — cues + show file
3. Alan deploys, plays in-game, gives feedback
4. Claude revises

Claude's role is a **creative collaborator and show author** — not a developer tool.

### ShowSprite Voice

ShowSprite speaks in `[Sprite]` chat messages throughout shows. Voice characteristics:
- Poetic, theatrical, not technical
- Uses white space (short messages, deliberate pauses)
- Earned wit — not jokes, but moments of wry observation
- Never lists features; always evokes experience
- Present tense, direct address: "You are standing in..." not "This show will demonstrate..."

### What "done" looks like for Phase 7/8

- [x] `docs/showsprite.context.md` — ShowSprite persona document — **complete 2026-03-24**
- [x] Cue tagging: spec §10 taxonomy applied; each cue has `tags:` array — **complete 2026-03-24**
- [ ] Cue library: 30+ cues organized by emotional function (at 25 — 5 more to reach target)
- [ ] At least 3 production-quality shows authored (not demos — actual experiences)
- [ ] `intro.young_persons_guide` rewritten as a genuine artistic piece, not a capability tour
- [ ] Show authoring guide: how to write for Scaena — see `docs/showsprite.context.md §Authoring Workflow`

---

## Repo Structure

```
ScaenaShows/
├── _archive/
│   └── v1/                        ← v1 plugin source (scenes/sequences model)
├── docs/
│   ├── spec.md                    ← v2 YAML schema (authoritative)
│   ├── showsprite.context.md      ← ShowSprite persona + voice guide (Phase 7 deliverable)
│   ├── preview-audit.md           ← Phase 1 era audit (mostly resolved)
│   ├── style-guide.html           ← ScaenaComposer design (deferred)
│   └── ui-mockup.html             ← ScaenaComposer UI (deferred)
├── src/
│   └── main/
│       ├── java/com/scaena/shows/  ← v2 plugin source
│       └── resources/
│           ├── plugin.yml
│           ├── config.yml
│           ├── fireworks.yml
│           ├── cues/               ← Cue library (growing)
│           └── shows/              ← Shows (growing)
├── CLAUDE.md                       ← this file
├── ROADMAP.md                      ← phase tracker
├── README.md                       ← public-facing description
└── build.gradle.kts                ← Gradle + shadow plugin
```

---

## Key Architectural Decisions (Closed)

Do not reopen these without Alan.

| Decision | Choice |
|----------|--------|
| Container model | Universal recursive Cue — not scenes/sequences |
| Reuse model | Reference by ID — Cues are named assets stored independently |
| YAML library | SnakeYAML directly (bundled with Paper) — not Paper's FileConfiguration |
| Nesting | Unlimited depth (cycle detection prevents infinite loops) |
| Multi-participant | One RunningShow per /show play invocation |
| Spatial anchor | First named target (or invoker if selector used) |
| Static mode | Anchor locked at invocation time — does NOT follow the player |
| Schema versioning | No version field on Cue/Show YAML in v2 |
| Unknown CUE ID | Fail at load time, not at runtime |
| GROUP_ASSIGN underflow | Silent skip — empty group fires zero events |
| Field naming | `origin_offset` (not `center_offset`) across all pattern types |
| Phases 2–6 | Deferred indefinitely — no ScaenaComposer GUI required for Phase 7/8 |

---

## Open Questions

| # | Priority | Question |
|---|----------|----------|
| 5 | ~~High~~ | ~~showsprite.context.md~~ — **Resolved 2026-03-24.** Document is stable. |
| 6 | Low | GLOW + TAB API coordination (TAB 5.x) — deferred to when GLOW events are needed in production |
| 7 | **High** | demo.archetype_sampler in-game test — which archetypes land, which need revision? Unblock before expanding library further. |
| 8 | Medium | `intro.young_persons_guide` voice pass (SCENA-006) — structure is sound, narration needs rewrite. Do after archetype baselines are confirmed. |

---

## Known Issues / Tech Debt

- `build/` and `.gradle/` directories are tracked in git (committed in early init). Clean up with `git rm --cached -r build/ .gradle/` when convenient — `.gitignore` already excludes them.
- Player-facing plugin strings are placeholder text — replace with Scaena voice strings before production launch
- `intro.young_persons_guide` is an effective capability demo but not yet an artistic experience — rewrite is Phase 7/8 work

---

## Versioning Policy

**Current version:** `2.8.1` (as of 2026-03-24)
**Version file:** `build.gradle.kts` — the `version = "x.y.z"` line

### Rules Claude must follow

Before telling Alan to build, Claude **must** either bump the version or explicitly state why no bump is needed. Never let Alan build the same version number twice.

**When to bump:**
| Change type | Bump |
|-------------|------|
| New cues, shows, or docs added | `MINOR` (x.**y**.0) |
| Java plugin logic changed | `MINOR` (x.**y**.0) |
| Bug fix only, no new content | `PATCH` (x.y.**z**) |
| Breaking schema change or major rework | `MAJOR` (**x**.0.0) |

**Labeling convention:** append a short label to the commit message describing the milestone.
Examples: `cue-library-preview`, `grief-family`, `young-persons-rewrite`, `bugfix-cue-ref`.

**How to bump:**
1. Edit `version = "x.y.z"` in `build.gradle.kts`
2. Update the comment in `shadowJar { archiveClassifier... }` to match
3. Update the "Current version" line above in this section
4. Mention the new version and JAR filename when giving Alan the build command

**`plugin.yml` is automatic** — do NOT edit its `version:` field manually.
Gradle's `processResources` block injects `${version}` from `build.gradle.kts` at build time.
The startup banner reads from `plugin.yml`, so it stays in sync with no extra steps.

**If no bump is needed** (e.g. Alan is rebuilding the same content to verify a fix without any file changes), say so explicitly: *"No version change needed — content is identical to the last build."*

---

## How to Work in This Repo

### Starting a session
1. Read `CLAUDE.md` (this file) — current phase, session state, and what's next
2. Read `ROADMAP.md` — Phase 7/8 priorities and open issues
3. If writing shows: read `docs/spec.md §4` for schema; read `docs/showsprite.context.md` for voice
4. If creating cues: read `docs/cue-library-survey.md` for current inventory and gaps; follow naming `[category].[archetype].[variant]` per spec §9; add `tags:` per taxonomy spec §10
5. If writing Java: re-skim the relevant spec section first

### Cue Authoring — How Alan Collaborates

Before building new cues, Claude proposes a short list of archetypes and waits for alignment. Do not build until Alan confirms the list. See `docs/showsprite.context.md §Cue Library Authoring` for the full methodology.

When building new cues for review:
1. Build a **demo show** that introduces each archetype: quiet Sprite intro → cue fires → labeled in chat
2. **Always generate a run sheet** alongside the demo show (`docs/[show-id].runsheet.md`)
3. Each run sheet entry has: Intention, Function, Mechanics, Watch question, Notes field
4. Number every cue in the run sheet so Alan can take notes by number (C3, C7, etc.)
5. Alan reviews in-game on one screen, run sheet on another; debriefs with Claude by cue number

### Writing Cue/Show YAML
- All Cue files: `src/main/resources/cues/*.yml`
- All Show files: `src/main/resources/shows/*.yml`
- Naming: `[category].[archetype].[variant]` per spec §9
- Tags: free-form strings per spec §10 (not namespaced — `warm` not `tone:warm`)
- CUE references in show timelines: `type: CUE` with `cue_id:` field (not `cue:`)

### Building
```bash
./gradlew shadowJar
# Output: build/libs/ScaenaShows-<version>.jar
# Always check/bump version in build.gradle.kts before building — see Versioning Policy above.
```

### Deploying (after build)
```bash
# Stop server
# Replace JAR in plugins/
# Delete plugins/ScaenaShows/cues/ and plugins/ScaenaShows/shows/
# Start server — bundled YAMLs extract automatically
# Verify: /show list
```

### Resuming on Another Machine

Pull the branch, build, and deploy:

```bash
git checkout feature/ai-show-generation
git pull
./gradlew shadowJar
```

Then stop server, swap JAR, delete cues/ and shows/ from the plugin folder, restart.

**First show to run after deploy:**
```
/show play demo.archetype_sampler
```

Open `docs/demo.archetype_sampler.runsheet.md` on a second screen before running.
This is the first in-game test of the new cue library. Take notes by cue number (C1–C12).
Debrief with Claude after — every note drives the next revision.

---

## Contacts

- **Alan** — project owner, overall creative direction, Scaena voice sign-off
- **Zarathale (Zara)** — primary show builder; works directly with Claude on show authoring and cue development; the person most likely at the keyboard in a Cowork session
- **Smitty2020 (Smitty)** — secondary show builder in-game
