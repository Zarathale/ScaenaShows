# ScaenaShows v2 — Roadmap

> Phases are sequential. Each phase must reach its acceptance criteria before the next begins.
> Build order was decided in Open Question 2 (spec §16): Plugin → Node server → Timeline → Spatial → Preview → RCON → ShowSprite → Library.

---

## Phase 1 — Java Plugin (Cue Schema)

**Status:** 🟢 Active — started 2026-03-23
**Delivers:** A working v2 plugin that loads Cue YAML and runs shows in-game.

### Acceptance Criteria

- [ ] Plugin registers with Paper 1.21.x without errors
- [ ] `fireworks.yml` — preset library loads; multi-star schema parsed
- [ ] `cues/*.yml` — Cue model loads and validates; recursive CUE references resolve
- [ ] `shows/*.yml` — Show model loads; all show-level fields parsed
- [ ] Cycle detection — show fails to load, names the cycle path in the error
- [ ] Unknown CUE ID — fail at load time, log: `[ScaenaShows] Show 'X' references unknown Cue ID: 'Y'`
- [ ] `/show list` — lists all loaded shows by ID and name
- [ ] `/show play <showId> <target> [<target2>...] [--follow|--static] [--private]`
- [ ] `/show stop <target|@a>`
- [ ] `/show stopall`
- [ ] `/show reload` — hot-reload all YAML without restart
- [ ] All event types execute:
  - [ ] TEXT and DISPLAY (MESSAGE, TITLE, ACTION_BAR, BOSSBAR)
  - [ ] SOUND
  - [ ] VISUAL EFFECTS (PARTICLE, EFFECT, GLOW, CAMERA, LIGHTNING)
  - [ ] TEAM and GROUPS (GROUP_ASSIGN, TEAM_COLOR, GROUP_EVENT)
  - [ ] FIREWORKS and SPATIAL PATTERNS (FIREWORK, FIREWORK_CIRCLE, FIREWORK_LINE, FIREWORK_FAN, FIREWORK_RANDOM)
  - [ ] WORLD and ENVIRONMENT (WEATHER, TIME_OF_DAY, REDSTONE)
  - [ ] ENTITY MANAGEMENT (SPAWN_ENTITY, DESPAWN_ENTITY, CAPTURE_ENTITIES, RELEASE_ENTITIES)
  - [ ] ENTITY BEHAVIOR (ENTITY_AI, ENTITY_SPEED, ENTITY_EFFECT, ENTITY_EQUIP, ENTITY_INVISIBLE, ENTITY_VELOCITY)
  - [ ] STAGE DIRECTIONS (HOLD, FACE, CROSS_TO, RETURN_HOME, ENTER, EXIT)
  - [ ] PLAYER MOVEMENT (PLAYER_TELEPORT, PLAYER_VELOCITY, PLAYER_SPECTATE, PLAYER_MOUNT)
  - [ ] UTILITY (REST, COMMAND, CUE)
- [ ] GROUP_ASSIGN with fewer players than groups: silent skip, no error
- [ ] Multi-participant: one RunningShow; all named targets are participants
- [ ] Spatial anchor = first named target (or invoker if selector)
- [ ] Follow mode and static mode work correctly
- [ ] Audience: broadcast vs. participants enforced per event
- [ ] `--private` clamps all broadcast events to participants only
- [ ] Permissions enforced (play, target.others, target.all, private, scenes, admin)
- [ ] Cooldown per invoker enforced; admin bypass works
- [ ] Pause on logout / resume on rejoin within `resume_window_seconds`
- [ ] Stop safety: levitation removed, slow falling applied, bossbar hidden
- [ ] Show-level bossbar: fade in/out, progress tracks show duration
- [ ] Director mode (`--scenes`): scene name shown in action bar at each scene start

### Notes

- Do NOT use Paper's FileConfiguration. Use SnakeYAML directly.
- Reference spec §4 (schema), §6 (events), §5 (invocation), §15 (runtime) constantly.
- See `CLAUDE.md` for key decisions and known issues before writing code.
- Player-facing strings: use placeholders in Phase 1; Scaena voice pass before production.

---

## Phase 2 — ScaenaComposer: Node.js Server

**Status:** Pending Phase 1 completion
**Delivers:** Local Node.js web server that reads/writes the ScaenaShows repo.

### Acceptance Criteria (TBD in dedicated pre-build session)

- [ ] Express (or equivalent) server starts locally
- [ ] Reads cues/, shows/, fireworks.yml from local repo checkout path
- [ ] Writes files back on save
- [ ] `composer.config.json` used for local config (gitignored)
- [ ] API surface defined for Phase 3 timeline UI

---

## Phase 3 — ScaenaComposer: Basic Timeline UI

**Status:** Pending Phase 2 completion
**Delivers:** A usable timeline editor for shows and cues.

### Acceptance Criteria (TBD)

- [ ] Load and display show timeline
- [ ] Add/remove/reorder events
- [ ] Edit event properties via properties panel
- [ ] Save to disk
- [ ] Composer error display: inline red indicator + properties panel description (not modal)
- [ ] Unsaved-changes tracking; warn on tab close

---

## Phase 4 — ScaenaComposer: Spatial View

**Status:** Pending Phase 3 completion
**Delivers:** 2D top-down interactive canvas for spatial layout of shows.

### Acceptance Criteria (TBD)

- [ ] Canvas renders spatial anchor, marks, and firework pattern positions
- [ ] Interactive: drag marks, adjust pattern origins
- [ ] Stage directions (CROSS_TO, LOOK_AT) visualized

---

## Phase 5 — ScaenaComposer: Sketch Preview

**Status:** Pending Phase 4 completion
**Delivers:** In-tool preview with canvas animation and browser audio.

### Acceptance Criteria (TBD)

- [ ] Firework animations on canvas (approximate, not pixel-perfect)
- [ ] Sound playback using browser audio from local Minecraft asset cache
- [ ] Sound not found: silent no-op + `⚠ Sound not found in local asset cache` indicator
- [ ] Playback controls: play, pause, scrub

---

## Phase 6 — ScaenaComposer: RCON Live Preview

**Status:** Pending Phase 5 completion; RCON credentials confirmed available
**Delivers:** In-game live preview triggered from ScaenaComposer via RCON.

### Acceptance Criteria (TBD)

- [ ] RCON connection configured via `composer.config.json`
- [ ] "Preview live" button in Composer sends `/show play` via RCON
- [ ] "Stop preview" button sends `/show stop` via RCON
- [ ] Composer logs RCON command output

---

## Phase 7 — ShowSprite (AI Assistant)

**Status:** Pending Phase 6 completion; blocked on `showsprite.context.md` draft (SCENA-003)
**Delivers:** ShowSprite embedded in ScaenaComposer — AI creative assistant for Cue authoring.

### Acceptance Criteria (TBD — dedicated pre-build session needed first)

- [ ] `showsprite.context.md` drafted and committed
- [ ] ShowSprite can read show context and Cue library
- [ ] Proposals applied in memory until creator saves
- [ ] ShowSprite identity: it/they, distinct from ZaraSprite

---

## Phase 8 — Cue Library Seeding

**Status:** Pending Phase 1 completion in-game stability (Open Question 4)
**Delivers:** A seeded starter set of Cue files with proper naming, tags, and archetypes.

### Acceptance Criteria (TBD — dedicated library-building session)

- [ ] Starter Cues authored for each major arc type (ramp, peak, moment, coda, celebration)
- [ ] All Cues follow naming convention §9: `[category].[archetype].[variant]`
- [ ] All Cues tagged using taxonomy §10
- [ ] Cues tested in-game

---

## Open Issues

| ID | Phase | Description |
|----|-------|-------------|
| SCENA-001 | Phase 1 | Spec §15 has duplicate runtime sections — merge before writing runtime code |
| SCENA-002 | Phase 1+ | GLOW + TAB 5.x API coordination — investigate developer API for per-player team pause |
| SCENA-003 | Phase 7 | `showsprite.context.md` initial draft — needed before ShowSprite session |
