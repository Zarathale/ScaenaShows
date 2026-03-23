# ScaenaShows (Paper 1.21.11 / Java 21)

ScaenaShows is a data‑driven choreography engine for Minecraft Paper servers. It plays “shows” (timelines of events) targeted at a player or all online players.

Events supported:

- MiniMessage chat messages
- Titles/subtitles (MiniMessage)
- BossBar (MiniMessage + color/overlay)
- Sounds
- Particles
- Potion effects (including levitation + slow falling safety)
- Item grants (validated: must always notify with message + sound)
- High‑quality fireworks using presets + sequences (with optional nested sequences)

## Install

1. Build the plugin:
   - This repo uses **Gradle Kotlin DSL** (`build.gradle.kts`).
   - If you don’t have Gradle installed, build from an IDE (IntelliJ) or install Gradle 8+.
2. Drop the jar into `plugins/`
3. Start the server once. ScaenaShows will create:

```
plugins/ScaenaShows/
  config.yml
  fireworks.yml
  sequences/*.yml
  scenes/*.yml
  shows/*.yml
```

If files already exist, ScaenaShows does **not** overwrite them.

## Commands

- `/show list`
- `/show play <showId> <player|@a> [--follow|--static] [--private]`
- `/show stop <player|@a>`
- `/show stopall`
- `/show reload`

### Targeting rules

- Default players can only target themselves unless they have:
  - `scae.shows.target.others` (target other players)
  - `scae.shows.target.all` (target `@a`)
- Admin permission `scae.shows.admin` can do everything and bypass cooldown.

### Cooldown

- Default players have a **60s** cooldown per **initiator** (the command runner).
- Admin (or `scae.shows.cooldown.bypass`) bypasses cooldown.
- Config: `default_cooldown_seconds`

## Behavior notes

- **One active show per target player.** If a show is already running for that target, `/show play` refuses:
  - `Show already running. Use /show stop <player>.`
- **Pause/resume:** shows pause on logout and resume if the player rejoins within `resume_window_seconds` (default 900s / 15 minutes). Otherwise the show is canceled with cleanup.
- **Server restart:** no persistence; pending tasks are cleared when the server stops.
- **Stop safety:** `/show stop` immediately cancels and:
  - removes levitation
  - applies slow falling for `stop_safety_slow_falling_seconds` (default 10s)
  - hides bossbar created by show
  - plays stop sound/message (configurable)

## File formats (schemas)

ScaenaShows implements the schemas exactly as described in your brief:

### `fireworks.yml`

```yml
version: 1
presets:
  scaena_gold:
    display_name: "Scaena Gold"
    power: 1
    type: BALL
    colors: ["#FECB00", "#FFFFFF"]
    fades:  ["#EA7125"]
    trail: true
    flicker: true
    launch:
      mode: above
      y_offset: 1.0
      spread: 0.0
```

### `sequences/*.yml`

```yml
id: birthday_pastel_pop
name: "Birthday Pastel Pop"
description: "A short celebratory burst"
duration_ticks: 80
shots:
  - at: 0
    preset: pastel_pop
    count: 2
  - at: 20
    preset: pastel_pop
    count: 2
    sound:
      id: minecraft:entity.firework_rocket.launch
      category: master
      volume: 1.0
      pitch: 1.0
```

**Optional nesting:** if `preset` matches a sequence id, it is treated as a nested sequence call.

### `scenes/*.yml`

```yml
id: intro_scene
name: "Intro"
duration_ticks: 60
events:
  - at: 0
    type: MESSAGE
    audience: broadcast
    message: "<gold>Welcome!</gold>"
  - at: 0
    type: TITLE
    audience: private
    title:
      title: "<gold><bold>Scaena</bold></gold>"
      subtitle: "<gray>Enjoy the show</gray>"
      fade_in: 10
      stay: 40
      fade_out: 10
```

### `shows/*.yml`

```yml
id: sample_show
name: "Sample Show"
description: "Demonstrates scenes + sequences"
default_mode: follow
default_audience: broadcast
bossbar:
  enabled: true
  title: "<gold><bold>Sample Show</bold></gold>"
  color: YELLOW
  overlay: PROGRESS
  audience: broadcast
timeline:
  - at: 0
    type: SCENE
    scene: intro_scene
  - at: 40
    type: SEQUENCE
    sequence: birthday_pastel_pop
```

## Troubleshooting

- Run `/show reload` and watch console output for:
  - missing ids
  - unknown preset/sequence/scene references
  - invalid material/sound/particle identifiers
  - ITEM events missing required `notify` payload

Invalid YAML files are skipped; ScaenaShows will not crash the server.

## Notes on “broadcast vs private”

- By default, **messages/titles/bossbar** are broadcast.
- `--private` clamps those to the target player(s).
- Individual events may also specify `audience: broadcast|private`. If the show is `--private`, broadcasts are clamped to private.
