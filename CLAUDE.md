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

> Alan's decision: bypass Phases 2–6 (the web interface). Claude can already generate shows
> directly as YAML. The goal is to get to high-quality, emotionally resonant show authoring
> ASAP — without waiting for a GUI.
>
> See `ROADMAP.md` for the revised phase plan.

---

## Phase 1 — What Was Built (Reference)

The plugin is working and deployed. Key facts for future Java work:

- **Build:** `./gradlew shadowJar` → `build/libs/ScaenaShows-2.0.0.jar`
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

- [ ] `docs/showsprite.context.md` — ShowSprite persona document (voice, constraints, examples)
- [ ] Cue library: 30+ cues organized by emotional function, not event type
- [ ] Cue tagging: spec §10 taxonomy applied; each cue has `tags:` array
- [ ] At least 3 production-quality shows authored (not demos — actual experiences)
- [ ] `intro.young_persons_guide` rewritten as a genuine artistic piece, not a capability tour
- [ ] Show authoring guide: how to write for Scaena (pacing, spatial design, emotional arc)

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
| 5 | **High** | `showsprite.context.md` — draft this before deep authoring work. What is ShowSprite's full persona? What can it say about itself? What can't it? |
| 6 | Low | GLOW + TAB API coordination (TAB 5.x) — deferred to when GLOW events are needed in production |

---

## Known Issues / Tech Debt

- `build/` and `.gradle/` directories are tracked in git (committed in early init). Clean up with `git rm --cached -r build/ .gradle/` when convenient — `.gitignore` already excludes them.
- Player-facing plugin strings are placeholder text — replace with Scaena voice strings before production launch
- `intro.young_persons_guide` is an effective capability demo but not yet an artistic experience — rewrite is Phase 7/8 work

---

## How to Work in This Repo

### Starting a session
1. Read `CLAUDE.md` (this file) — current phase and direction
2. Read `ROADMAP.md` — see Phase 7/8 priorities
3. If writing shows: read `docs/spec.md §4` for schema, then author with voice (not feature lists)
4. If creating cues: follow naming convention `[category].[archetype].[variant]` per spec §9; add `tags:` per taxonomy spec §10
5. If writing Java: re-skim the relevant spec section first

### Writing Cue/Show YAML
- All Cue files: `src/main/resources/cues/*.yml`
- All Show files: `src/main/resources/shows/*.yml`
- Naming: `[category].[archetype].[variant]` per spec §9
- Tags: taxonomy from spec §10

### Building
```bash
./gradlew shadowJar
# Output: build/libs/ScaenaShows-2.0.0.jar
```

### Deploying (after build)
```
# Stop server
# Replace JAR in plugins/
# Delete plugins/ScaenaShows/cues/ and plugins/ScaenaShows/shows/
# Start server — bundled YAMLs extract automatically
```

---

## Contacts

- **Alan** — project owner, overall creative direction, Scaena voice sign-off
- **Zarathale (Zara)** — primary show builder; works directly with Claude on show authoring and cue development; the person most likely at the keyboard in a Cowork session
- **Smitty2020 (Smitty)** — secondary show builder in-game
