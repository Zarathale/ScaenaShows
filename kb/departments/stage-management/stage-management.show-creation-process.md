---
department: Stage Management & Production
document: Show Creation Process
kb_version: 1.0
updated: 2026-03-26
owner: Kendra
status: stub — to be expanded from show authoring practice
---

# Show Creation Process

> This document is Kendra's Production Manager reference — the complete spine from concept
> acceptance to in-game test and debrief. It is intentionally brief now and will be expanded
> as the Showcase Series establishes the workflow in practice.
>
> The authoritative short version lives in `stage-management.kb.md §Production Manager Role`.

---

## The Spine

```
1.  Concept accepted by Show Director
2.  Show folder scaffolded from _template
      src/main/resources/shows/[show_id]/
3.  Intake brief written → direction/[show_id].intake.md
4.  Show Director brief written → brief.md
5.  Per-department briefings issued → each dept's standing brief questions asked
6.  Department decisions filed → departments/[show_id].[dept].md (one file per dept)
7.  Kendra confirms blocking departments have filed
      → signals "YAML authoring can begin — [dept list] ready; [dept list] non-blocking for this pass"
8.  YAML authored (by Kendra / Claude-as-Kendra) from dept decisions
9.  Run sheet written in parallel with YAML
10. Version bumped in build.gradle.kts
11. Build: ./gradlew shadowJar
12. Deploy: stop server → replace JAR → delete cues/ and shows/ → start server
13. In-game test: /show play [show_id]
14. Debrief: each dept files revision notes → Director synthesizes → revision-log.md updated
15. Revision cycle: repeat from step 5 (dept briefings) or step 8 (YAML only) as scope dictates
```

---

## Pass Types and Blocking Departments

Each production pass has a defined set of **blocking departments** — departments whose decisions must be filed before that pass's YAML can be authored. Non-blocking departments can file decisions concurrently; their contributions go into the next pass.

| Pass | Name | Blocking departments | What gets authored |
|------|------|---------------------|-------------------|
| P1 | Set | Set | Stage registry, marks, set dressing, spatial anchor |
| P2 | Effects + Choreography | Effects, Choreography | Player body events, entity movement |
| P3 | Sound + Lighting | Sound, Lighting | Sound beds, music events, sky arc, weather |
| P4 | Voice + Wardrobe | Voice, Wardrobe | Sprite dialogue, entity equipment |
| P5 | Fireworks + Camera | Fireworks, Camera | Pyrotechnics, camera events |
| P6 | Dress | All departments | Full show assembled, all departments have filed |

---

## Department Briefing Template

When the Show Director issues a brief, Kendra distributes it to departments with their standard questions. Full per-department brief questions live in `kb/departments/show-director/show-director.kb.md §Standing Department Asks`.

**Minimum per-department brief package:**
1. Link to `brief.md`
2. Department's section from the Show Direction document (`direction/[show_id].show-direction.md`)
3. Standing questions for that department (from Show Director KB)
4. Deadline: when Kendra needs the decision filed to stay on schedule

---

## Kendra's Gates

**Gate 1 — Before YAML authoring begins:**
All blocking departments for the current pass have filed decisions in `departments/[show_id].[dept].md`.
Kendra signals explicitly: "YAML authoring can begin."

**Gate 2 — Before build:**
- Rehearsal safety checklist complete (all items checked off in run sheet)
- Version bumped or "no bump needed" explicitly stated
- Run sheet current and at correct revision

**Gate 3 — Before in-game test:**
- Tech check complete
- Alan and Zarathale have current run sheet open

---

## Revision Cycle Quick Reference

After each in-game test:

1. Alan debrief → raw notes by cue number (C3, C7, etc.)
2. Each department files its revision notes in `departments/[show_id].[dept].md`
3. Kendra logs structural issues in the run sheet
4. Show Director synthesizes into `direction/[show_id].revision-log.md`
5. Show Director decides revision scope: which departments get new briefing questions, which just re-author within existing brief
6. Kendra identifies YAML revision scope: full diff or section-targeted
7. YAML revised → run sheet updated → version bumped → build

---

## Placeholder — Sections to Expand From Practice

The following sections are intentionally thin. They will be written from direct experience during the Showcase Series (Phase 10/11):

- **Multi-participant show sequencing** — how briefing and YAML authorship changes when GROUP_ASSIGN is used
- **Cross-show cue reuse decisions** — when a cue from one show should be extracted to the library vs. left show-specific
- **Interruption recovery** — what Kendra does when a show is interrupted mid-rehearsal
- **Show deprecation** — how a show gets sunset or archived
- **Show import process** — migrating a flat-YAML show to the folder structure (see `skills/show-import-process/show-import-process.skill.md`)
