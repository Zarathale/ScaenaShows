# Show Import Process — Migrating a Flat Show into the Folder Structure

> This document describes how to migrate an existing flat-YAML show into the full
> folder structure introduced in 2026-03-25. Follow this process when you want to
> bring an older show under the production team model — with a proper show folder,
> brief, run sheet, and per-department files.
>
> Do not apply this process to a show unless you are actively working on it.
> Migrating a show mid-revision creates a moving target. Complete the revision first,
> then import.

---

## Prerequisites

- The show's current flat YAML is stable (all recent revisions complete)
- The `_template/` scaffold exists at `src/main/resources/shows/_template/`
- The plugin scanner has been updated to read subdirectory YAMLs (see ops-inbox item).
  If the scanner has *not* been updated, the YAML must remain as a flat file until
  it is — the folder structure is documentation-only until then.

---

## Step 1 — Create the Show Folder

```bash
mkdir src/main/resources/shows/[show_id]
mkdir src/main/resources/shows/[show_id]/departments
```

Replace `[show_id]` with the show's canonical ID (e.g. `intro.young_persons_guide`).

---

## Step 2 — Copy the Flat YAML Into the Folder

```bash
cp src/main/resources/shows/[show_id].yml \
   src/main/resources/shows/[show_id]/[show_id].yml
```

Do not delete the flat YAML yet — keep both until the plugin scanner is updated and
the new path is verified to load correctly in-game.

---

## Step 3 — Scaffold from the Template

Copy all template files into the new show folder:

```bash
cp src/main/resources/shows/_template/brief.md \
   src/main/resources/shows/[show_id]/brief.md

cp src/main/resources/shows/_template/run-sheet.md \
   src/main/resources/shows/[show_id]/run-sheet.md

cp -r src/main/resources/shows/_template/departments/ \
      src/main/resources/shows/[show_id]/departments/
```

---

## Step 4 — Fill in the Brief

Open `[show_id]/brief.md` and replace the template stubs with the show's actual content.

Draw from:
- The show YAML itself (section structure, cues used, timing)
- Any existing run sheet docs in `docs/`
- Any revision notes or debrief records in `CLAUDE.md`

The brief has these sections to complete:

- **Arc** — the emotional journey in 2–3 sentences
- **Player** — who is the player in this experience?
- **Tone** — register, atmosphere, emotional mode
- **Carry-away** — what should they feel when it ends?
- **Constraints** — duration, environment requirements, limitations
- **Per-department briefings** — specific direction for each of the 9 departments

---

## Step 5 — Fill in the Run Sheet

Open `[show_id]/run-sheet.md` and port over content from any existing run sheet in `docs/`.

If no run sheet exists, build one from the show YAML section by section:
- Each section gets: Intention, Function, Mechanics, Watch question, Notes
- Number every section C1, C2, … for in-game annotation

Update the frontmatter (`version:`, `revised:`, `status:`).

---

## Step 6 — Write Department Files

For each department file in `[show_id]/departments/`:
1. Fill in the **Brief Received** section by extracting the relevant per-department brief
   from `brief.md`.
2. Document **Decisions** — the key design choices this department made for this show.
   For departments with no active role in the show, write a brief note explaining that.
3. Add any revision history or watch questions in **Notes**.

Department files to complete (in this order):
1. `casting.md`
2. `wardrobe.md`
3. `choreography.md`
4. `set.md`
5. `camera.md`
6. `lighting.md`
7. `sound.md`
8. `voice.md`
9. `stage-manager.md`

For shows where a department has no role (e.g. casting for a no-entity show), write:
```markdown
## Decisions

**Decision: No [Department] involvement in this show.** [Brief reason.]
```
This is intentional documentation — the absence of a decision is itself a decision.

---

## Step 7 — Update Status Fields

Once all files are complete, update the frontmatter in each department file:
- `status: complete` for departments with no planned future work
- `status: active` for departments that will have ongoing changes

Update `brief.md` frontmatter: `status: complete` or `status: active`.

---

## Step 8 — Verify and Remove the Flat YAML (After Scanner Update)

Once the plugin scanner has been updated to read `shows/[show_id]/[show_id].yml`:

1. Build and deploy the new JAR
2. Verify `/show list` shows the show ID correctly
3. Run `/show play [show_id]` and confirm full playback
4. If confirmed working: remove the flat YAML

```bash
rm src/main/resources/shows/[show_id].yml
```

Do not remove the flat YAML before verifying the folder-based path loads correctly.

---

## Reference

- Template scaffold: `src/main/resources/shows/_template/`
- Plugin scanner gap: `ops-inbox.md` (the scanner update that enables full folder adoption)
- Example completed import: `src/main/resources/shows/demo.archetype_sampler/`
