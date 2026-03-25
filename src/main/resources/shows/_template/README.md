# Show Folder — Template

This folder is the canonical scaffold for every new show. Copy it, rename it to the show's ID, and fill it in.

## Files in this folder

| File | Who owns it | Purpose |
|------|-------------|---------|
| `[show_id].yml` | Show Director / all depts | The show YAML — the compiled output the plugin runs |
| `brief.md` | Show Director | Creative brief — entry point; everyone reads this first |
| `run-sheet.md` | Stage Manager | Operational guide used during in-game test |
| `direction/show-direction.md` | Show Director | Non-negotiables, watch-for, elevated departments |
| `direction/tone.md` | Show Director | Tone phrase elaboration per department (write when needed) |
| `direction/intake.md` | Show Director | Default intake record: dept questions + initial answers |
| `direction/revision-log.md` | Show Director | Running debrief synthesis, one entry per revision cycle |
| `departments/casting.md` | Casting Director | Brief received + decisions + revision notes |
| `departments/wardrobe.md` | Wardrobe & Properties Director | " |
| `departments/choreography.md` | Choreographer | " |
| `departments/set.md` | Set Director | " |
| `departments/camera.md` | Camera Director | " |
| `departments/lighting.md` | Lighting & Atmosphere Designer | " |
| `departments/sound.md` | Sound Designer | " |
| `departments/voice.md` | Sprite Voice Director | " |
| `departments/stage-manager.md` | Stage Manager | " |

> The `direction/` folder is the Show Director's working space — the same relationship to the
> show folder that any department subfolder has to the production. The Director owns `brief.md`
> (at root, because it's the entry point) and all files in `direction/`.

## How to start a new show

1. Copy this folder: `cp -r _template/ [show_id]/`
2. Rename `_template.show.yml` to `[show_id].yml` — fill in `id:`, `name:`, and `description:` at the top
3. Read `docs/departments/show-director.kb.md` — write the brief in `brief.md`
4. Fill in `direction/show-direction.md` with the Show Direction (non-negotiables, watch-for, elevated depts)
5. Fill in `direction/intake.md` using the Standing Department Asks from show-director.kb.md
6. Brief each department — they record responses in `departments/[dept].md`
7. Author the YAML with the production team at the table
8. Write the run sheet in `run-sheet.md`
9. Deploy and test; department debriefs go in `departments/[dept].md`; Director synthesis goes in `direction/revision-log.md`

## Note on the YAML location

The show YAML currently lives in this folder at `[show_id].yml`.
The plugin scanner is being updated to load shows from subdirectories (see ops-inbox.md).
Until that update ships, the YAML is also maintained at the flat `shows/[show_id].yml` level for compatibility.
