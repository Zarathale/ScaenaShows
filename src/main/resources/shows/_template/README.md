# Show Folder — Template

This folder is the canonical scaffold for every new show. Copy it, rename it to the show's ID, and fill it in.

## Files in this folder

| File | Who owns it | Purpose |
|------|-------------|---------|
| `[show_id].yml` | Show Director / all depts | The show YAML — the compiled output the plugin runs |
| `[show_id].brief.md` | Show Director | Creative brief — entry point; everyone reads this first |
| `[show_id].run-sheet.md` | Stage Manager | Operational guide used during in-game test |
| `[show_id].show-params.md` | Show Director | Settled structural facts (kit, sites, mechanics) |
| `direction/[show_id].show-direction.md` | Show Director | Non-negotiables, watch-for, elevated departments |
| `direction/[show_id].tone.md` | Show Director | Tone phrase elaboration per department (write when needed) |
| `direction/[show_id].intake.md` | Show Director | Default intake record: dept questions + initial answers |
| `direction/[show_id].revision-log.md` | Show Director | Running debrief synthesis, one entry per revision cycle |
| `direction/[show_id].status.md` | Show Director | Current state, last session summary, what's next |
| `departments/[show_id].casting.md` | Casting Director | Brief received + decisions + revision notes |
| `departments/[show_id].wardrobe.md` | Wardrobe & Properties Director | " |
| `departments/[show_id].choreography.md` | Choreographer | " |
| `departments/[show_id].set.md` | Set Director | " |
| `departments/[show_id].camera.md` | Camera Director | " |
| `departments/[show_id].lighting.md` | Lighting & Atmosphere Designer | " |
| `departments/[show_id].sound.md` | Sound Designer | " |
| `departments/[show_id].voice.md` | Sprite Voice Director | " |
| `departments/[show_id].stage-manager.md` | Stage Manager | " |

> The `direction/` folder is the Show Director's working space — the same relationship to the
> show folder that any department subfolder has to the production. The Director owns `brief.md`
> (at root, because it's the entry point) and all files in `direction/`.

## How to start a new show

1. Copy this folder: `cp -r _template/ [show_id]/`
2. Rename all template files: add `[show_id].` prefix to each file (e.g. `brief.md` → `[show_id].brief.md`, `direction/intake.md` → `direction/[show_id].intake.md`)
3. Rename `_template.show.yml` to `[show_id].yml` — fill in `id:`, `name:`, and `description:` at the top
4. Read `kb/departments/show-director/show-director.kb.md` — write the brief in `[show_id].brief.md`
5. Fill in `direction/[show_id].show-direction.md` with the Show Direction (non-negotiables, watch-for, elevated depts)
6. Fill in `direction/[show_id].intake.md` using the Standing Department Asks from show-director.kb.md
7. Brief each department — they record responses in `departments/[show_id].[dept].md`
8. Author the YAML with the production team at the table
9. Write the run sheet in `[show_id].run-sheet.md`
10. Deploy and test; department debriefs go in `departments/[show_id].[dept].md`; Director synthesis goes in `direction/[show_id].revision-log.md`

## Note on the YAML location

The show YAML currently lives in this folder at `[show_id].yml`.
The plugin scanner is being updated to load shows from subdirectories (see ops-inbox.md).
Until that update ships, the YAML is also maintained at the flat `shows/[show_id].yml` level for compatibility.
