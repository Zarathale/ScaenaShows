# ScaenaShows — Claude Project Hub

> This file is the authoritative briefing for Claude on the ScaenaShows project.
> Read this first at the start of every session. It tells you where we are, what phase is active, and what to do next.

---

## Project Identity

**ScaenaShows** is a data-driven choreography engine for Minecraft Paper servers.
**Owner:** Alan (alytle@thearcoregon.org)
**Repo:** ScaenaShows (this repo)
**Branch:** `feature/ai-show-generation` ← active development branch
**YAML schema:** `kb/system/spec.md` — authoritative source of truth for all Cue/Show/event-type syntax.
**Build readiness audit:** `kb/audits/2026-03-24_phase1-preview-audit.md` — Phase 1 era audit (historical reference).

---

## Current Phase

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Complete (2026-03-24) | Java plugin rewrite (Cue schema) — fully working |
| Phases 2–6 | ⏭ Skipped | ScaenaComposer UI/RCON — deferred indefinitely |
| Phase 7 | ✅ Complete (2026-03-24) | ShowSprite persona + production team framework established |
| Phase 8 | ✅ Complete (2026-03-24) | Cue library seeded; creative language foundation laid |
| **Phase 9** | 🟢 Active | Department readiness + show creation workflow |
| **Phase 10** | 🟢 Active | Showcase Series — team calibration (3 shows scaffolded, 0 passes complete) |

## Session State — 2026-03-26

**Where we stopped:** Repo audit complete and all changes applied. R7 (`demo.archetype_sampler` v2.9.0) is built but has NOT been tested in-game. All three showcases are at Brief stage, P1 (Set) not started for any of them.

**What was accomplished this session (2026-03-26):**
- ROADMAP.md fully rewritten — Phases 9/10/11 defined, Phases 7+8 closed
- All three Showcase shows scaffolded: `showcase.01` "The Cabinet", `showcase.02` "The Long Night", `showcase.03` "Welcome" — full folder structure, briefs, show-direction, and department briefs written
- `kb/departments/show-director/showcase-roadmap.md` — pass sequences for all three shows
- `skills/audit-this-repo/SKILL.md` — new skill created
- Repo audit completed: `kb/audits/2026-03-26_pre-showcase-audit.md`
- CLAUDE.md updated: phase table (9+10 active), skills list, session state
- `kb/roadmap-planning-2026-03-26.md` — archived to `kb/artifacts/` (superseded by ROADMAP.md)
- `kb/cue-library-survey.md` — archived to `kb/artifacts/` (stale; direct cue scan preferred)
- **Strategic clarification:** Cue library is no longer measured by count. The measure is depth of creative language — departmental vocabulary, tone translation, and decision-making framework. The library grows with the work.

**Immediate next step:**
Run R7 in-game on the other machine. `/show play demo.archetype_sampler`
Watch questions: Does C2 hover feel like gentle bubbling near the ground? Does C3 climb feel like gradual separation from earth? Does C7 lift feel EARNED — like the arrival takes you? Is C9 inside the fireworks?

**After R7 debrief:**
Begin showcase departmental work. All three shows await P1 (Set). Set is the first mover for all three — P1 requires in-world scouting by Zarathale (in-game, not in Claude). Pick one show, open P1 (Set), begin the scouting brief and coordinate stage registry entries with `kb/departments/set/stage-registry.md`.

Phase 9 Track A: Effects, Set, and Lighting KBs are the thinnest and have critical roles in the showcases. Schedule KB uplift sessions before those departments' passes run.

---

## Session State — 2026-03-24 (archived)

**Where we stopped:** Archetype sampler R7 complete. Ready to build 2.9.0 and run R7.

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
  - `build.gradle.kts` → **v2.8.1** (show-content patch, R6)
- **R6 in-game test feedback (2026-03-25):** 2 passes. Altitude arc not yet narratively earned. "Boof" lift at C2 spends the reveal too early. Player often above effects rather than inside them. Text stacks over-layered. Joy/daylight transition logic muddy. "Building" section (C10) was strongest — use as model.
- **`demo.archetype_sampler` R7 (v2.9.0):**
  - Altitude dramaturgy rewrite — height withheld, then earned
  - C1: starts at day (TIME_OF_DAY 8000), three steps to night; dialogue fires during dimming
  - C2: NO amp-9 burst — hover near ground (0-2 blocks), embers surround at body level; text trimmed to 3 lines
  - C3: CLIMB begins (2-6 blocks) — warmth now genuinely below the player
  - C4: HOVER at ~6 blocks — cool shift is tonal not vertical
  - C5: HOVER at ~7 blocks — cave sound ascends toward them from below
  - C6: CLIMB to 12-14 blocks — unannounced, silent foreshadowing
  - C7: AMP-9 LIFT EARNED HERE — arrival takes player from 14 → 25 blocks; first dramatic height
  - C8: HOVER at 25 blocks — joy burst below, elevated observer
  - C9: pressure-release descent through fireworks (25→18 blocks); firework y_offsets 26/20/12/24/16
  - C10-C13: same pattern as R6 (ramp/descent/wonder/finale/coda)
  - Altitude arc: 0 → 2 → 6 → 7 → 9 → 14 → 25 → 18 → 8 → 14 → 35+ → 0
  - `build.gradle.kts` → **v2.9.0**

**Immediate next step (other machine):**
Build 2.9.0 and run `demo.archetype_sampler` (R7). `/show play demo.archetype_sampler`
Key observations: Does the C2 hover feel like "gentle bubbling" near the ground? Does C3 climb feel like gradual separation from earth? Does C7 lift feel EARNED — like the arrival takes you? Is C9 inside the fireworks?

**After R7 debrief:**
If altitude arc feels right, begin authoring `fx.hover.sustained` / `fx.hover.descend` reusable cues. Then `intro.young_persons_guide` voice pass (open question #8).

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

## Phase 7 + 8 — Reference (Complete)

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
2. **Claude convenes the production team** — before any YAML is written, Claude brings each of the twelve roles to the table. The Show Director writes the brief and per-department briefings. Casting, Wardrobe, Choreography, Set, Effects, Camera, Lighting, Sound, Fireworks, Voice, and Stage Management each have questions to ask and decisions to make. These decisions shape the YAML that follows. Each department's decisions are tracked in the show folder under `departments/`.
3. Claude authors the full YAML directly — cues + show file
4. Alan deploys, plays in-game, gives feedback
5. Claude revises

Claude's role is a **creative collaborator and show author** — not a developer tool.

The production team knowledge base lives in `kb/production-team.md`. ShowSprite's voice and persona are defined in `kb/departments/voice/showsprite.context.md`.

Each department maintains a **technical knowledgebase** in `kb/departments/` — one file per department. Read the relevant KB before writing YAML for that department's tools.

### What "done" looks like for Phase 7/8

- [x] ShowSprite persona document — **complete 2026-03-24** (`kb/departments/voice/showsprite.context.md`)
- [x] Cue tagging: spec §10 taxonomy applied; each cue has `tags:` array — **complete 2026-03-24**
- [x] Cue library seeded — 37 production cues across 10 families — **complete 2026-03-24**
  - Note: count is no longer the relevant metric. The measure going forward is depth of creative language — departmental vocabulary, tone translation capability, and the team's ability to make decisions from a shared design framework. The library grows with the shows.
- [ ] At least 3 production-quality shows authored — this is Phase 10/11 work
- [ ] `intro.young_persons_guide` rewritten as a genuine artistic piece — deferred to Phase 11

---

## Repo Structure

```
ScaenaShows/
├── kb/                            ← Plugin-wide knowledge base
│   ├── system/
│   │   └── spec.md                ← Authoritative YAML schema (Cue/Show/event-type syntax)
│   ├── departments/               ← Technical KBs — one per department
│   │   ├── show-director.kb.md
│   │   ├── casting.kb.md
│   │   ├── wardrobe.kb.md         (+ wardrobe/ subfolder with extended references)
│   │   ├── choreography.kb.md
│   │   ├── set.kb.md
│   │   ├── effects.kb.md
│   │   ├── camera.kb.md           ← Camera Director (Mark) — full peer department
│   │   ├── lighting.kb.md
│   │   ├── sound.kb.md
│   │   ├── fireworks.kb.md
│   │   ├── voice.kb.md            (+ voice/ subfolder: showsprite.context.md)
│   │   ├── stage-manager.kb.md
│   │   └── approved-sources.md
│   ├── production-team.md         ← Virtual production team — common brain
│   ├── cue-library-survey.md      ← Historical cue survey (out-of-date; reference only)
│   ├── audits/                    ← Audit outputs, date-stamped
│   │   └── 2026-03-24_phase1-preview-audit.md
│   └── artifacts/                 ← Historical/unaccepted files
│       ├── style-guide.html       ← ScaenaComposer UI draft (unaccepted, deferred)
│       └── ui-mockup.html         ← ScaenaComposer UI draft (unaccepted, deferred)
├── skills/                        ← Claude working skills (plain markdown)
│   ├── audit-this-repo/SKILL.md   ← Full repo health check; session orientation
│   ├── dept-kb-builder/SKILL.md   ← DEPRECATED — superseded by kb-builder (tombstone only)
│   ├── kb-builder/SKILL.md        ← Build/update a dept KB; audit full KB; quick summary
│   ├── production-review/SKILL.md ← Full production team show review
│   └── show-import-process/SKILL.md ← Migrate a flat show to folder structure
├── docs/                          ← Legacy run sheets (migration debt — move to show folders)
│   └── *.runsheet.md
├── src/
│   └── main/
│       ├── java/com/scaena/shows/  ← v2 plugin source
│       └── resources/
│           ├── plugin.yml
│           ├── config.yml
│           ├── fireworks.yml
│           ├── cues/               ← Cue library (growing)
│           └── shows/              ← Shows (growing)
│               ├── *.yml           ← Existing flat shows (plugin-loadable)
│               ├── _template/      ← Scaffold for new show folders
│               └── [show_id]/      ← Show folder
│                   ├── [show_id].yml
│                   ├── brief.md
│                   ├── run-sheet.md
│                   ├── direction/
│                   │   ├── show-direction.md
│                   │   ├── tone.md
│                   │   ├── intake.md
│                   │   └── revision-log.md
│                   └── departments/
│                       └── *.md    ← One file per department
├── _archive/
│   └── v1/                        ← v1 plugin source (scenes/sequences model)
├── CLAUDE.md                       ← this file
├── ROADMAP.md                      ← phase tracker
├── README.md                       ← public-facing description
├── ops-inbox.md                    ← Java capability gap queue (Stage Management)
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
| 5 | ~~High~~ | ~~showsprite.context.md~~ — **Resolved 2026-03-24.** Stable at `kb/departments/voice/showsprite.context.md`. |
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

**Current version:** `2.9.0` (as of 2026-03-25)
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
2. Read `ROADMAP.md` — Phase 9/10 priorities, acceptance criteria, and open issues
3. **If writing shows or cues (any show work):** Read `kb/production-team.md` — the full production team is at the table for all show work. Read `kb/departments/show-director/show-director.kb.md` and write the show brief before any YAML is authored. The brief, Show Direction, intake record, and all department decisions live in the show folder (`src/main/resources/shows/[show_id]/`) — the Director's working files under `direction/`, department files under `departments/`. Then read `kb/system/spec.md §4` for schema; read `kb/departments/voice/showsprite.context.md` for voice. For any specific department's tools, read the relevant KB file from `kb/departments/` before writing YAML. For a new show, scaffold from `src/main/resources/shows/_template/`.
4. If creating cues: follow naming `[category].[archetype].[variant]` per spec §9; add `tags:` per taxonomy spec §10. Assess current cue inventory directly from `src/main/resources/cues/`. The measure of the cue library is creative language depth, not count.
5. If writing Java: re-skim the relevant spec section first. If a Java control surface gap is identified, bring it to Stage Management — see `kb/departments/stage-manager.kb.md §Ops-Inbox Workflow`. Stage Management files to ops-inbox.md and coordinates with the Java review team.

### Cue Authoring — How Alan Collaborates

Before building new cues, Claude proposes a short list of archetypes and waits for alignment. Do not build until Alan confirms the list. See `kb/departments/voice/showsprite.context.md §Cue Library Authoring` for the full methodology.

When building new cues for review:
1. Build a **demo show** that introduces each archetype: quiet Sprite intro → cue fires → labeled in chat
2. **Always generate a run sheet** alongside the demo show — save inside the show folder at `[show_id]/run-sheet.md`
3. Each run sheet entry has: Intention, Function, Mechanics, Watch question, Notes field
4. Number every cue in the run sheet so Alan can take notes by number (C3, C7, etc.)
5. Alan reviews in-game on one screen, run sheet on another; debriefs with Claude by cue number

### Writing Cue/Show YAML
- All Cue files: `src/main/resources/cues/*.yml`
- Show YAML files: `src/main/resources/shows/[show_id].yml` (flat, plugin-loadable) OR `src/main/resources/shows/[show_id]/[show_id].yml` (folder structure — see note below)
- Show folder structure: `src/main/resources/shows/[show_id]/` — contains YAML, `brief.md`, `run-sheet.md`, Show Director's `direction/` subfolder, and `departments/*.md`
- **Note on show folders:** The plugin scanner currently reads only flat `shows/*.yml` files. Show folder YAMLs are not loaded by the plugin until the scanner is updated (see `ops-inbox.md`). Keep flat YAMLs for plugin compatibility during transition; use the folder for all production team documentation.
- To migrate an existing show into folder structure: use `skills/show-import-process/SKILL.md`
- Naming: `[category].[arc