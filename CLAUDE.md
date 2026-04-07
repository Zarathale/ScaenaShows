# ScaenaShows — Claude Project Hub

> Read this at the start of every session. It tells you where things are, what's active, and how to orient.

---

## Project Identity

**ScaenaShows** is a live directing tool for Minecraft Paper servers — a stage director's tool built on top of a reusable cue library. Players stage shows; they don't author YAML.
**Owner:** Alan (alytle@thearcoregon.org)
**Repo:** ScaenaShows
**Branch:** `main` ← active development branch
**YAML schema:** `kb/system/cue-show-yaml-schema.md` — authoritative source of truth for all Cue/Show/event-type syntax.

This is a hobby project and a live experiment in working with Claude. The process is part of the point. Keep the vibe casual, collaborative, and creative. Lean into the theatre metaphor — it holds up.

---

## Where We Are

The engine is built and the production team is in place. We're in the Showcase Series — three shows in parallel development, each moving through its own arc toward opening. Alongside that, calibration practice runs through demo shows.

All 12 department KBs are now in folder structure (`kb/departments/[dept-slug]/[dept-slug].kb.md`). Migration is complete.

**showcase.01 "Preparing for Battle" — active:**
- Gates 1 (Casting) ✅ and 2 (Wardrobe) ✅ closed. Kit locked, arc staged, script v2 complete.
- Gate 3 (Set Scouting) open. Zarathale scouts Sites B–F using `direction/showcase.01.scouting-field-guide.md`. Site A scouted 2026-03-30.
- Gate 4 (Intake) pending Gate 3. Two TBDs remain: victory coda fireworks (Mira) + victory levitation amplifier (Effects, pending ceiling clearance).
- Engine at 2.38.0 — OPS-027 (Phase 1) shipped. OPS-029 Groups 0–5 (Casting, Wardrobe, Sound, Voice, Effects) committed. Prompt Book is the authoritative committed-state artifact.

**Not show-specific:**
- R7 debrief ✅ complete (2026-03-28). R8 not yet scheduled.
- **OPS-029 Phase 2 — Groups 0–4 + Group 5 Casting/Wardrobe/Sound/Voice/Effects/Fireworks committed at v2.39.0. Git clean. Group 5 Lighting next.**
  Groups 0–1 at v2.29.0. Group 2 at v2.30.0. Group 3 at v2.31.0. Group 4 at v2.32.0.
  Group 5: Casting at v2.33.0 (revised v2.35.0), Wardrobe at v2.34.0, Sound at v2.36.0, Voice at v2.37.0, Effects at v2.38.0, Fireworks at v2.39.0 (PR #10).
  Read `kb/system/ops-029-impl-plan.md` for full build sequence — it has everything Code needs.
  Read `kb/system/ops-029-design-session-2026-04-05.md` for architecture decisions.
  **Next: Group 5 Lighting in Code (feature branch `claude/ops-029-group5-lighting`), target v2.40.0.**
  Note: `SetBuildSession.java`, `BlockBuildListener.java`, `SetBuildWriter.java` are fully
  shipped Phase 1 features — block diff capture is live and wired into `TechSession`/`TechManager`.
  Group 5 Set wraps this in a `DeptEditSession` for Phase 2; the hard capture work is already done.
  Detailed content in standalone docs:
  - `kb/system/phase2-department-panels.md` — all 10 department panel specs
  - `kb/system/pattern-phrase-spec.md` — PATTERN, PHRASE, Tempo Architecture
  - `kb/system/music-event-types.md` — MUSIC types, HARP_SWEEP, pattern library

*Last audit: 2026-03-31 — `kb/audits/2026-03-31_post-ops026-audit.md`*

---

## Active Shows

| Show | Stage | State File |
|------|-------|------------|
| showcase.01 "Preparing for Battle" | Brief | `src/main/resources/shows/showcase.01/direction/showcase.01.status.md` |
| showcase.02 "The Long Night" | Brief | `src/main/resources/shows/showcase.02/direction/showcase.02.status.md` |
| showcase.03 "Welcome" | Brief | `src/main/resources/shows/showcase.03/direction/showcase.03.status.md` |

Each show's `direction/[show_id].status.md` holds: current stage, last session summary, what's next, and Direction's open items. **After any session that touches a show, update that show's status file.**

Alan moves between shows freely. There's no lock on which show is primary.

---

## Session Startup

1. Read this file
2. Read `ROADMAP.md` — the full picture of how this project is structured
3. **If working on a specific show:** read that show's `direction/[show_id].status.md`, then `[show_id].prompt-book.yml` (if it exists — showcase.01 has one; it's the single source of truth for structural facts and committed params). Then read `direction/[show_id].show-direction.md` for creative context.
4. **If writing shows or cues:** read `kb/production-team.md`, then `kb/departments/show-director/show-director.kb.md`. Write the Show Director brief before any YAML. Read the relevant department KB(s) before writing YAML for their tools.
5. **If creating cues:** naming is `[category].[archetype].[variant]` per spec §9; add `tags:` per taxonomy spec §10
6. **If writing Java:** re-skim the relevant spec section first. Capability gaps go in `ops-inbox.md` — that's Alan's list, for plugin-level Java work only. **Do NOT attempt to run `gradle`, `gradlew`, or any build command** — the Claude Code environment cannot build this project (closed sandbox, no loopback). Alan builds locally. Write the code, commit, push — stop there.

**[show_id].prompt-book.yml:** Where it exists, this is the show's settled structural facts — cast, wardrobe, set, scenes, key mechanics, params, script lines, and readiness state. Replaces `show-params.md` entirely (OPS-027). A value in the prompt-book means "build from this." A TBD means it's still in play. The plugin reads it at TechSession init; the Parameter tool writes back to it on SAVE. Department briefs reference the prompt-book rather than restating facts.

---

## How Show Work Flows

### Starting a new show
Scaffold from `src/main/resources/shows/_template/`. The Show Director writes the brief and per-department briefings before any YAML is authored. Show files live in:
- `[show_id].prompt-book.yml` — authoritative committed state: cast, wardrobe, set, scenes, params, script lines, readiness (replaces show-params.md)
- `direction/` — Show Director's working files (status, show-direction, tone, revision-log)
- `departments/` — one file per department that has decisions or notes for this show

**There is no formal intake gate.** Decisions get made iteratively across sessions. The `readiness.open` section of the prompt-book tracks what's still TBD. When those items are resolved, YAML authoring begins.

### Calibration and demo shows
`demo.*` shows are the calibration lab. Each department maintains a **Calibration Backlog** in its KB — specific things they want to develop mastery over. A calibration round picks from that list, builds a demo show, Alan watches, and findings get recorded back in the KB as **patterns** (named configurations with notes on storytelling effect).

Before a calibration session: read the relevant department KB and check the backlog. After: update the patterns section.

### Round structure
Two kinds of working rounds, which alternate naturally:

**Watch Round** — Alan picks a focus angle, watches the current show in-game, and takes notes by cue number (C1, C7, etc.). Notes come back to Claude; Direction assigns issues to departments.

**Redesign Round** — Pick a bucket of issues. Work with the production team or specific departments. Batch the changes, publish show files, then head into a Watch Round.

### Issue tracking
- **Show-level issues** — Direction owns and coordinates. They live in the show's `direction/` folder.
- **Department issues (cross-show)** — Filed in the department KB under the calibration backlog or a separate inbox note.
- **Plugin-level Java gaps** — Filed in `ops-inbox.md` (repo root). This is Alan's list. Stage Management has no role here.

---

## Repo Structure

```
ScaenaShows/
├── kb/                            ← Plugin-wide knowledge base
│   ├── system/
│   │   └── cue-show-yaml-schema.md                ← Authoritative YAML schema (Cue/Show/event-type syntax)
│   ├── departments/               ← One folder per department
│   │   ├── [dept-slug]/
│   │   │   ├── [dept-slug].kb.md  ← Main KB: instruments, tone translation, calibration backlog, patterns
│   │   │   └── *.md               ← Supplementary files (registries, extended references, etc.)
│   │   └── approved-sources.md
│   └── production-team.md         ← Full team roster and role definitions
├── skills/                        ← Claude working skills (plain markdown)
│   ├── audit-this-repo/audit-this-repo.skill.md
│   ├── kb-builder/kb-builder.skill.md
│   ├── production-review/production-review.skill.md
│   └── show-import-process/show-import-process.skill.md
├── docs/                          ← Legacy run sheets (migration debt — move to show folders)
├── src/
│   └── main/
│       ├── java/com/scaena/shows/  ← v2 plugin source
│       └── resources/
│           ├── plugin.yml
│           ├── config.yml
│           ├── fireworks.yml
│           ├── cues/               ← Cue library
│           └── shows/
│               ├── *.yml           ← Flat shows (plugin-loadable)
│               ├── _template/      ← Scaffold for new show folders
│               └── [show_id]/      ← Show folder
│                   ├── [show_id].yml
│                   ├── [show_id].prompt-book.yml  ← Committed state: cast, set, params, script (OPS-027)
│                   ├── [show_id].run-sheet.md
│                   ├── [show_id].brief.md
│                   ├── direction/
│                   │   ├── [show_id].status.md        ← Current state, last session, what's next
│                   │   ├── [show_id].show-direction.md
│                   │   ├── [show_id].tone.md
│                   │   ├── [show_id].intake.md
│                   │   └── [show_id].revision-log.md
│                   └── departments/
│                       └── [show_id].[dept].md    ← One file per department
├── _archive/
│   └── v1/                        ← v1 plugin source (retired)
├── CLAUDE.md                       ← this file
├── ROADMAP.md                      ← how the project is structured and where it's going
├── README.md
├── ops-inbox.md                    ← Alan's list — Java plugin capability gaps only
└── build.gradle.kts
```

**Note on show folders:** The plugin scanner currently reads only flat `shows/*.yml` files. Show folder YAMLs are not loaded by the plugin until the scanner is updated (tracked in `ops-inbox.md`). Keep flat YAMLs for plugin compatibility during transition; use the folder for all production team documentation.

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
| Phases 2–6 | Deferred indefinitely — no ScaenaComposer GUI required |

---

## Open Questions

| # | Priority | Question |
|---|----------|----------|
| 6 | Low | GLOW + TAB API coordination (TAB 5.x) — deferred to when GLOW events are needed in production |
| 7 | **High** | `demo.archetype_sampler` in-game test (R7) — which archetypes land, which need revision? Unblock before expanding the library. |
| 8 | Medium | `intro.young_persons_guide` voice pass (SCENA-006) — do after archetype baselines confirmed |

---

## Known Issues / Tech Debt

- `build/` and `.gradle/` directories are tracked in git — clean up with `git rm --cached -r build/ .gradle/` when convenient (`.gitignore` already excludes them)
- `.claude/worktrees/` directories (`pedantic-hertz`, `interesting-kirch`) are tracked in git — clean up with `git rm --cached -r .claude/` when convenient (`.gitignore` already excludes `.claude/`)
- Many Java files show as "modified" in git status due to CRLF/LF line-ending differences — add `.gitattributes` with `*.java text=auto eol=lf` to normalize
- Player-facing plugin strings are placeholder text — replace with Scaena voice strings before any public-facing launch
- `intro.young_persons_guide` is an effective capability demo but not yet an artistic experience — rewrite is SCENA-006
- Legacy run sheets in `docs/` belong in their show folders — migration debt, low priority

---

## File Naming Conventions

**Audit files:** Use `YYYY-MM-DDTHHMM_description.md` (datetime, not date-only). Example: `2026-04-06T1430_post-session-audit.md`. Existing audit files predate this convention and are not being retroactively renamed.

**Dept sub-docs:** Prefix with `[dept-slug].` to match the KB and brief files. Example: `set.stage-registry.md`, `sound.music-director.md`. Files named `[dept-slug].kb.md` and `[dept-slug].brief-to-direction.md` already follow this pattern — all supplementary docs should too.

**Project skill files:** Named `[skill-name].skill.md` inside their folder. Example: `skills/kb-builder/kb-builder.skill.md`.

---

## Versioning Policy

**Current version:** `2.43.0`
**Version file:** `build.gradle.kts` — the `version = "x.y.z"` line

**Alan builds locally. Claude Code never runs the build.** The Claude Code environment is a closed sandbox with no loopback — `gradle`/`gradlew` always fails here. Write the code, bump the version, commit, push. Alan runs the build and confirms. Do not attempt `gradle` or `gradlew` under any circumstances.

Before Alan builds, either bump the version or explicitly state why no bump is needed.

| Change type | Bump |
|-------------|------|
| New cues, shows, or docs added | `MINOR` (x.**y**.0) |
| Java plugin logic changed | `MINOR` (x.**y**.0) |
| Bug fix only, no new content | `PATCH` (x.y.**z**) |
| Breaking schema change or major rework | `MAJOR` (**x**.0.0) |

**`plugin.yml` is automatic** — do NOT edit its `version:` field manually. Gradle's `processResources` injects `${version}` from `build.gradle.kts` at build time.

How to bump:
1. Edit `version = "x.y.z"` in `build.gradle.kts`
2. Update the comment in `shadowJar { archiveClassifier... }` to match
3. Update "Current version" above

If no bump is needed (rebuilding identical content), say so explicitly.

---

## Git Workflow

Three agents touch this repo. Keep them in their lanes.

**Cowork (Claude desktop)** — edits docs, YAML, KB files, and CLAUDE.md directly in the mounted folder. Does not use git. Alan stages and commits from GitHub Desktop after reviewing.

**Claude Code** — writes and compiles Java. Always works on a **feature branch** (e.g. `claude/ops-029-group5-casting`). When done: commits to that branch, pushes. Stops there. Alan reviews the diff and merges via GitHub Desktop or GitHub web. **Code never commits directly to `main`.**

**GitHub Desktop** — Alan's merge/push surface. The source of truth for what's actually on `main`.

**Git hygiene rules for Code sessions:**
- Always `git checkout -b claude/[description]` before any Java work
- Commit with a meaningful message: `OPS-029 Group N: [what] (vX.Y.Z)`
- Bump the version in `build.gradle.kts` *before* the commit
- Push the branch; do not merge
- Never `git push origin main` directly

**Cleaning up tracked artifacts:**
- `.claude/worktrees/` gets created by Code for isolated tasks. `.claude/` is in `.gitignore` but old worktrees may still be tracked. Fix: `git rm --cached -r .claude/` then commit.
- Line-ending noise (CRLF/LF): add `.g