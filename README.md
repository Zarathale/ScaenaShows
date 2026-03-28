# ScaenaShows v2

ScaenaShows is a data-driven choreography engine for Minecraft Paper servers. It plays "shows" — timelines of coordinated events — targeted at one or more players simultaneously. Think of it as a theatrical production system: a virtual team of directors, designers, and technicians collaborating to create experiences in a Minecraft world.

This is a hobby project. It's also an ongoing experiment in creative collaboration with Claude — building something genuinely good while figuring out how to work together well. The process is part of the point.

**Platform:** Paper 1.21.x / Java 21
**Build:** Gradle Kotlin DSL (`build.gradle.kts`)

See `ROADMAP.md` for where the project is and where it's going. See `CLAUDE.md` for session startup instructions.

---

## Architecture

Everything in ScaenaShows v2 is a **Cue** — a named, reusable, recursively nestable event container. Shows are Cues with a runtime entry point. There are no separate "scene" or "sequence" types.

```
fireworks.yml   → named firework presets
cues/*.yml      → named Cue assets (reusable building blocks)
shows/*.yml     → show definitions (top-level runtime entry points)
```

For the full design specification, see `kb/system/spec.md`.

---

## Commands

```
/show list
/show play <showId> <target> [<target2>...] [--follow|--static] [--private] [--scenes]
/show stop <player|@a>
/show stopall
/show reload
```

**Targeting:** Multiple targets in a single `/show play` creates one shared show instance. All named targets are participants — simultaneously actors and audience. The first named target (or the invoker if a selector is used) is the spatial anchor.

**`--scenes`** — Director mode (admin-only): displays the current cue name in the action bar during playback.

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `scae.shows.admin` | op | Full access, bypass cooldown |
| `scae.shows.play` | true | Play shows |
| `scae.shows.target.others` | op | Target other players |
| `scae.shows.target.all` | op | Target `@a` |
| `scae.shows.private` | op | Use `--private` flag |
| `scae.shows.cooldown.bypass` | op | Bypass cooldown |
| `scae.shows.scenes` | op | Use `--scenes` director mode |

---

## Configuration

`config.yml` uses plugin defaults; only include keys you want to override:

```yaml
default_cooldown_seconds: 30
resume_window_seconds: 900
```

---

## Install

1. Build: `./gradlew shadowJar`
2. Drop the JAR from `build/libs/` into `plugins/`
3. Start the server — ScaenaShows creates:

```
plugins/ScaenaShows/
  config.yml
  fireworks.yml
  cues/
  shows/
```

Existing files are never overwritten on startup.

---

## Development

| Directory | Contents |
|---|---|
| `src/main/java/` | Plugin Java source (v2) |
| `src/main/resources/` | Default config, fireworks, bundled cues and shows |
| `kb/` | Knowledge base — spec, department KBs, production team |
| `_archive/v1/` | v1 plugin source (scenes/sequences model — retired) |
| `CLAUDE.md` | Session startup and project context for Claude |
| `ROADMAP.md` | How the project is structured and where it's going |

---

## v1 → v2 Breaking Changes

- **Schema:** `cues/*.yml` replaces `scenes/*.yml` and `sequences/*.yml`. All Cues share one universal schema.
- **Fireworks:** `fireworks.yml` now uses a `stars:` list per preset (multi-star support). The flat top-level star fields from v1 are removed.
- **No version field** on Cue or Show YAML files. v1 files are not compatible.
- **`scenes/` and `sequences/` directories** are no longer created or read by the plugin.
