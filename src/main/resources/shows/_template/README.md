# Show Folder — Template

This folder is the canonical scaffold for every new show. Copy it, rename it to the show's ID, and fill it in.

## Files in this folder

| File | Who owns it | Purpose |
|------|-------------|---------|
| `[show_id].yml` | Show Director / all depts | The show YAML — the compiled output the plugin runs |
| `brief.md` | Show Director | Creative brief issued before any YAML is written |
| `run-sheet.md` | Stage Manager | Operational guide used during in-game test |
| `departments/casting.md` | Casting Director | Brief received + decisions + notes |
| `departments/wardrobe.md` | Wardrobe & Properties Director | " |
| `departments/choreography.md` | Choreographer | " |
| `departments/set.md` | Set Director | " |
| `departments/camera.md` | Camera Director | " |
| `departments/lighting.md` | Lighting & Atmosphere Designer | " |
| `departments/sound.md` | Sound Designer | " |
| `departments/voice.md` | Sprite Voice Director | " |
| `departments/stage-manager.md` | Stage Manager | " |

> **Note:** There is no `departments/show-director.md`. The Show Director issues the brief
> (`brief.md`) — they do not receive one. `brief.md` is the Director's output for this show.

## How to start a new show

1. Copy this folder: `cp -r _template/ [show_id]/`
2. Rename `_template.show.yml` to `[show_id].yml` — fill in `id:`, `name:`, and `description:` at the top
3. Read `docs/departments/show-director.kb.md` — write the brief in `brief.md`
4. Brief each department — they record responses in `departments/[dept].md`
5. Author the YAML with the production team at the table
6. Write the run sheet in `run-sheet.md`
7. Deploy and test; record observations in department notes + run sheet

## Note on the YAML location

The show YAML currently lives in this folder at `[show_id].yml`.
The plugin scanner is being updated to load shows from subdirectories (see ops-inbox.md).
Until that update ships, the YAML is also maintained at the flat `shows/[show_id].yml` level for compatibility.
