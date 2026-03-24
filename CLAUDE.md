# ScaenaShows — Claude Project Hub

> This file is the authoritative briefing for Claude on the ScaenaShows project.
> Read this first at the start of every session. It tells you where we are, what phase is active, and what to do next.

---

## Project Identity

**ScaenaShows** is a data-driven choreography engine for Minecraft Paper servers.
**Owner:** Alan (alytle@thearcoregon.org)
**Repo:** ScaenaShows (this repo)
**Design spec:** `docs/spec.md` — the authoritative source of truth. Read it before writing any Java or YAML.
**Build readiness audit:** `docs/preview-audit.md` — pre-build gaps and open questions.

---

## Current Phase

| Phase | Status | Description |
|-------|--------|-------------|
| **Phase 1** | 🟢 Active — Phase began 2026-03-23 | Java plugin rewrite (Cue schema) |
| Phase 2 | Pending | Node.js ScaenaComposer server |
| Phase 3 | Pending | Basic timeline UI |
| Phase 4 | Pending | Spatial view (2D canvas) |
| Phase 5 | Pending | Sketch preview (canvas animation + audio) |
| Phase 6 | Pending | RCON live preview |
| Phase 7 | Pending | ShowSprite (AI assistant in Composer) |
| Phase 8 | Pending | Cue library + tags — seeded starter set |

> See `ROADMAP.md` for full phase breakdown with acceptance criteria.

---

## Phase 1 Scope

Phase 1 delivers: **a working v2 plugin that loads Cue YAML files and runs shows in-game.**

Phase 1 does NOT include:
- ScaenaComposer (Phase 2+)
- ShowSprite (Phase 7)
- Cue library seeding (Phase 8)
- RCON (Phase 6)

### What "done" looks like for Phase 1

- [ ] Plugin loads and registers with Paper 1.21.x
- [ ] `fireworks.yml` loads preset library (v2 multi-star schema)
- [ ] `cues/*.yml` loads and validates (Cue model, recursive)
- [ ] `shows/*.yml` loads and validates (Show model extends Cue)
- [ ] `/show list` — lists loaded shows
- [ ] `/show play <showId> <target>` — runs a show; one RunningShow per invocation
- [ ] `/show stop <target>` — stops a show with safety cleanup (slow falling, bossbar hide)
- [ ] `/show stopall` — stops all running shows
- [ ] `/show reload` — hot-reloads all YAML without server restart
- [ ] All 11 event categories execute correctly (see spec §6)
- [ ] Cycle detection: show fails to load if CUE events form a cycle
- [ ] Unknown CUE ID: fail at load time, log name + missing ID
- [ ] GROUP_ASSIGN with fewer players than groups: silent skip on execution
- [ ] Multi-participant: one RunningShow, multiple participants, spatial anchor = first target
- [ ] Follow mode and static mode
- [ ] Audience targeting: broadcast vs. participants
- [ ] Permissions enforced (see plugin.yml)
- [ ] Cooldown enforced per invoker
- [ ] Pause on logout / resume on rejoin within resume_window_seconds
- [ ] Stop safety: levitation removed, slow falling applied, bossbar hidden

---

## Repo Structure

```
ScaenaShows/
├── _archive/
│   └── v1/
│       ├── src/               ← v1 plugin Java source (scenes/sequences model)
│       └── README.v1.md       ← v1 readme
├── docs/
│   ├── spec.md                ← v2 design spec (authoritative)
│   ├── preview-audit.md       ← pre-build audit (gaps + open questions)
│   ├── style-guide.html       ← ScaenaComposer design system
│   └── ui-mockup.html         ← ScaenaComposer UI mockup
├── src/
│   └── main/
│       ├── java/com/scaena/shows/   ← v2 plugin source (Phase 1)
│       └── resources/
│           ├── plugin.yml
│           ├── config.yml
│           ├── fireworks.yml
│           ├── cues/          ← v2 Cue YAML files (replaces v1 scenes/ + sequences/)
│           └── shows/         ← v2 Show YAML files
├── CLAUDE.md                  ← this file
├── ROADMAP.md                 ← phase tracker with acceptance criteria
├── README.md                  ← public-facing project description
├── build.gradle.kts           ← Gradle build (update for v2 main class)
└── docs/                      ← design docs (spec, audit, style guide, mockup)
```

---

## Key Architectural Decisions (Don't Revisit Without Alan)

These are closed decisions from the spec. Do not reopen them unilaterally.

| Decision | Choice |
|----------|--------|
| Container model | Universal recursive Cue — not scenes/sequences |
| Reuse model | Reference by ID — Cues are named assets stored independently |
| YAML library | SnakeYAML directly (bundled with Paper) — not Paper's FileConfiguration |
| Nesting | Unlimited depth (cycle detection prevents infinite loops) |
| Multi-participant | One RunningShow per /show play invocation |
| Spatial anchor | First named target (or invoker if selector used) |
| Schema versioning | No version field on Cue/Show YAML in v2 |
| Unknown CUE ID | Fail at load time, not at runtime |
| GROUP_ASSIGN underflow | Silent skip — empty group fires zero events |
| field naming | `origin_offset` (not `center_offset`) across all pattern types |

---

## Open Questions (Phase 1 relevant)

| # | Priority | Question |
|---|----------|---------- |
| 4 | Low | Library-building session: schedule to author seeded starter Cue set after Phase 1 is stable |
| 5 | Low | `showsprite.context.md` draft — needed before Phase 7. See SCENA-003. |
| 6 | Low | GLOW + TAB API coordination (TAB 5.x). See SCENA-002. |

---

## Known Issues / Tech Debt

- `build/` directory is tracked in git — `.gitignore` should exclude it (fixed in v2 setup)
- v1 `bday_one_per_second_12s.yml` and `scae_burst_pulses.yml` sequences not present in fireworks.yml — no presets reference them; verify before any v1 data migration
- Duplicate sections in spec §15 (Plugin Runtime) — merge before writing that code (see preview-audit.md)
- Player-facing plugin strings: use placeholder strings in Phase 1; replace with Scaena voice strings before production launch

---

## How to Work in This Repo

### Starting a session
1. Read `CLAUDE.md` (this file) — current phase and status
2. Read `ROADMAP.md` — see what's next in Phase 1
3. If writing Java: re-skim the relevant spec section before touching code
4. If working on YAML schema: `docs/spec.md §4` is the schema reference

### Writing Java
- Paper 1.21.x API
- Java 21
- SnakeYAML directly for all YAML parsing (do not use FileConfiguration)
- Package: `com.scaena.shows`
- Keep runtime classes in `runtime/`, model in `model/`, parsing in `registry/`, config in `config/`

### Writing Cue/Show YAML
- All Cue files go in `src/main/resources/cues/*.yml`
- All Show files go in `src/main/resources/shows/*.yml`
- Follow the naming convention in spec §9: `[category].[archetype].[variant]`
- Tags drawn from taxonomy in spec §10

### Building
```bash
./gradlew shadowJar   # or: ./gradlew build
# Output: build/libs/ScaenaShows-2.0.0.jar
```

---

## Contacts

- **Alan** — project owner, decisions, Scaena voice sign-off
- **Zarathale (Zara)** — primary show builder in-game
- **Smitty2020 (Smitty)** — secondary show builder
