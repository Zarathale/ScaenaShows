# ScaenaShows v2 — Roadmap

> **Revised 2026-03-26:** Phases 7 and 8 are complete. The production team framework now
> exists and is the governing model for all show work. The next three phases — department
> readiness, team calibration, and the Young People's Guide — build toward the first
> production-quality show series through iterative in-game development.

---

## Phase 1 — Java Plugin (Cue Schema)

**Status:** ✅ Complete — 2026-03-24
**Branch:** `main` (commit `73223ea`)

The v2 plugin is fully operational: Paper 1.21.x, Java 21, SnakeYAML, all 11 event
categories implemented, `/show list|play|stop|stopall|reload` commands, JAR-scanning
YAML extraction, and `smoothMovePlayer` absolute interpolation.

Known remaining items (not blocking current phases):

- `GLOW` and `CAMERA` events are stubbed — TAB 5.x API coordination needed (SCENA-002)
- Player-facing strings still placeholder — Scaena voice pass before production launch
- `build/` and `.gradle/` still tracked in git — remove with `git rm --cached` when convenient

---

## Phases 2–6 — ScaenaComposer (DEFERRED)

**Status:** ⏭ Skipped indefinitely

Node.js server, timeline UI, spatial canvas, sketch preview, RCON live preview.
Return to these phases only if a GUI proves valuable after the show library matures.

---

## Phase 7 — ShowSprite: AI Show Authoring

**Status:** ✅ Complete — 2026-03-24

ShowSprite is Claude's creative authoring identity for ScaenaShows. The persona document
is finalized and stable.

### Completed

- [x] `kb/departments/voice/showsprite.context.md` — voice characteristics, tone vocabulary,
  relationship to Alan/Zara/Smitty, good/bad narration contrast ✅
- [x] Show authoring workflow established in practice (concept → brief → dept decisions →
  YAML → in-game test → revision) — formal documentation is a Phase 9 deliverable

### Remaining (Phase 9 deliverables)

- [ ] Show authoring workflow formally documented — lives in `kb/departments/stage-management/`
- [ ] `intro.young_persons_guide` rewritten as artistic experience (SCENA-006)
- [ ] 3+ production-quality shows authored and opened through Phase 10

---

## Phase 8 — Cue Library Seeding

**Status:** ✅ Complete — 2026-03-24

### Completed

- [x] 37 production cues in `src/main/resources/cues/` across 10 families
- [x] All cues tagged per taxonomy (spec §10)
- [x] Naming follows `[category].[archetype].[variant]` (spec §9)
- [x] Coverage across arc roles: `ramp`, `peak`, `coda`, `breath`, `atmos`, `fx`, `mood`, `world`, `grief`
- [x] Archetype sampler demo series developed through R7 (in-game testing ongoing)

### Notes

Cue count is no longer the relevant measure. The library is a creative vocabulary — its
value is in the depth and precision of what each cue can do for a show's emotional arc,
not in how many cues exist. Coverage is uneven across families by design: gaps surface
organically as shows develop, and the library grows with the work, not ahead of it.

The correct question when assessing the cue library is: *does the team have the expressive
range to serve what this show needs?* That question gets answered in the context of
specific shows, not by counting families.

---

## Phase 9 — Department Readiness + Workflow Definition

**Status:** 🟢 Active — branch `feature/ai-show-generation`

Two parallel tracks that must both reach exit criteria before Phase 10 begins.

### Track A — KB Uplift

Bring every department KB to calibration-ready depth. The folder structure standard is
now defined: each department has a folder at `kb/departments/[dept-slug]/` containing a
main `[dept-slug].kb.md` and any supplementary files. Migration of existing flat KB files
into folders happens dept-by-dept during each uplift session.

**Calibration-ready definition** — a department KB meets this threshold when:
- Full instrument inventory documented (all known instruments, not just the easy ones)
- Every instrument has a capability status: ✅ Verified / ⚠️ Gapped / 📋 Aspirational
- Tone Translation section written
- At least one YAML example per instrument
- All known gaps filed in `ops-inbox.md`
- The KB has been read and used in at least one show session

**Priority order for uplift (thinnest / highest-impact first):**

| Priority | Department | Current gap |
|----------|-----------|-------------|
| 1 | Effects | Thinnest KB (260 lines); controls player body/physics — highest show impact |
| 2 | Set | Spatial design methodology underdeveloped |
| 3 | Lighting | Only 3 instruments documented |
| 4 | Wardrobe | Thin on creative/contextual examples |
| — | All others | Review for calibration-ready criteria; likely need minor uplift passes |

### Track B — Show Creation Workflow

Formally define the end-to-end show creation process. This is Kendra's process to own,
but it's a team-wide document — the Stage Management & Production department governs it.

**Document home:** `kb/departments/stage-management/show-creation-process.md`

The workflow covers: concept intake → Show Director brief → department briefings →
department decisions → YAML authoring (Stage Management) → run sheet → in-game test →
structured feedback → revision cycle → preview → opening. It cannot be written entirely
in theory — it gets its final shape through the first few Showcase runs.

### Stage Management & Production — scope clarification

The Stage Manager department (Kendra) carries both Stage Management and Production
Manager functions: show execution, prompt book, beat collision, run sheets, AND
pre-production process coordination, department briefing logistics, and YAML authorship.
The KB and role title reflect this expanded scope:
`kb/departments/stage-management/stage-management.kb.md`

### Acceptance Criteria

- [ ] All 12 department KBs meet the calibration-ready definition
- [ ] `kb/departments/` folder structure applied to all departments
- [ ] `kb/departments/stage-management/show-creation-process.md` drafted
- [ ] Show creation workflow exercised at least once (first Showcase run counts)
- [ ] `kb-builder` skill updated to enforce folder structure and calibration-ready criteria

---

## Phase 10 — Showcase Series (Team Calibration)

**Status:** ⏭ Pending Phase 9 completion

Three original shows developed from scratch through the full theatrical arc. Purpose:
each department calibrates its instruments in context, with real in-game feedback driving
iteration. These are functional-first productions — not artistically driven — designed to
build team confidence and process fluency.

### Development Arc

Each Showcase moves through the standard theatrical stages:

```
Concept → Brief → Dept Decisions → YAML Draft → Tech → Dress → Previews → Opening
```

**Opening = the show is frozen.** Once a Showcase opens, it does not change. It becomes
part of the permanent repertoire and a reference point for future calibration.

### Round Structure (per iteration within a Showcase)

1. Each department submits 2–3 calibration priorities for this round (what they want to test)
2. Show Director writes a minimal functional brief with a defined arc
3. Each department makes their decisions; Stage Management writes the YAML
4. Alan watches in-game (multiple passes); generates structured verbal/written feedback
5. Team divides feedback by department — each dept owns their fixes
6. Iterate toward the next stage (Tech → Dress → Previews → Opening)

### Feedback Format

Agreed before Showcase 1 Round 1. Indexed by cue number (C1, C7, etc.) so debrief
maps directly to the run sheet. Alan generates the feedback; Stage Management assigns
it to departments; departments own resolution.

### The Three Showcases

Showcases are not titled or themed until the Show Director writes the brief. What's
established now is the count (3) and the developmental model. After Showcase 3, the
team asks: *do we need a 4th before YPG?* That assessment is made in context.

### Acceptance Criteria

- [ ] 3 Showcases developed and opened
- [ ] Each department has calibrated its primary instruments through at least one full loop
- [ ] Show creation workflow has been exercised multiple times and is documented as-practiced
- [ ] Team is confident in the brief → YAML → feedback → revision cycle
- [ ] Optional: 4th Showcase commissioned if the team decides it's warranted

---

## Phase 11 — Young People's Guide to ScaenaCraft Shows

**Status:** ⏭ Pending Phase 10 completion

The gold standard. Inspired by Bernstein's Young People's Concerts — a series of
vignettes where the player is teleported to a distinct set for each movement. Each
movement has its own look, feel, vibe, and emotional register. The show showcases
the beauty of the Minecraft builds and demonstrates full mastery of the craft.

This phase begins only when the team has earned it through Phase 10. The high bar
is the point — Phase 9 and 10 exist to get the team here.

### What makes this different from calibration shows

- Artistically driven, not functionally driven
- Every department is at the table from concept, not just calibration-priority driven
- `intro.young_persons_guide` is the predecessor — rewrite it (SCENA-006) as a
  rehearsal for this show's ambition
- Each movement is a discrete world; the whole is a guided experience

### Acceptance Criteria

- [ ] Show Director brief written and approved
- [ ] All department decisions made before YAML authoring begins
- [ ] Full development arc completed (Tech → Dress → Previews → Opening)
- [ ] SCENA-006 resolved: `intro.young_persons_guide` voice pass complete before YPG enters pre-production

---

## Open Issues

| ID | Phase | Priority | Description |
|----|-------|----------|-------------|
| SCENA-006 | 11 | **High** | `intro.young_persons_guide` voice pass — rewrite Sprite's narration to speak from inside each movement, not catalog features. Complete before YPG enters pre-production. |
| SCENA-007 | 9 | **High** | Effects KB uplift — thinnest dept KB, highest player-body impact. Priority 1 for Track A. |
| SCENA-008 | 9 | **High** | Stage Management KB rename + Production Manager scope expansion — update frontmatter, KB header, and `kb/production-team.md` entry. |
| SCENA-009 | 9 | Medium | Set KB uplift — spatial design methodology section needed. |
| SCENA-010 | 9 | Medium | Lighting KB uplift — only 3 instruments documented; expand. |
| SCENA-011 | 9 | Medium | Wardrobe KB uplift — add creative/contextual examples. |
| SCENA-002 | 1+ | Low | GLOW + TAB 5.x API — defer until GLOW needed in production. |
