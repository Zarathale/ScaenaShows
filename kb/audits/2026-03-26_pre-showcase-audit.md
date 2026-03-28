---
date: 2026-03-26
purpose: Pre-showcase departmental work — session orientation
status: findings — awaiting confirmation
prior_audit: 2026-03-24_phase1-preview-audit.md
---

# Repo Audit — 2026-03-26

## Overall Health

The repo is in strong structural shape for its phase. ROADMAP.md is current, all three
Showcase shows are fully scaffolded at Brief stage, the cue library clears its target,
and the production team framework is solid. The main concerns are: (1) CLAUDE.md's phase
table still shows Phase 7+8 as Active — they're complete; (2) 10 of 12 department KBs
remain as flat files and none have been formally assessed against the calibration-ready
checklist yet; (3) R7 (`demo.archetype_sampler` v2.9.0) has been built but not tested.
Severity: low — nothing is broken, but Phase 9 Track A hasn't started, and this session
is the right time to begin.

## Delta Since Last Audit

The prior audit (2026-03-24) was a Phase 1 pre-build spec audit — entirely different
scope. That audit is now historical record; all its 🔴 items were resolved.

Since then, significant new content has been built and is well-integrated:
- Full virtual production team framework (production-team.md, dept KBs for all 12 depts)
- All three Showcase shows scaffolded with brief, show-direction, intake, departments/,
  run-sheet — all at Brief stage
- `showcase-roadmap.md` defining pass sequences for all three shows
- ROADMAP.md rewritten for Phases 9/10/11
- New skills: `dept-kb-builder`, `audit-this-repo`
- Cue library: 37 production cues (from 25 at last recorded checkpoint)

All new content fits the repo's purpose. No orphaned or misplaced additions.

---

## Layer 1 — Session State

| Check | Status | Notes |
|---|---|---|
| Current version | 2.9.0 | Consistent with CLAUDE.md ✅ |
| Immediate next step (CLAUDE.md) | R7 in-game test — `/show play demo.archetype_sampler` | Not yet run ⚠️ |
| ROADMAP.md current? | ✅ Yes | Phases 9/10/11 documented; Phases 7+8 marked complete |
| Open Question A (single show vs. fresh each round) | ✅ Resolved | ROADMAP.md Phase 10 defines the model |
| Open Question B (show creation workflow location) | ✅ Resolved | `kb/departments/stage-management/show-creation-process.md` |
| CLAUDE.md "Current Phase" table | ⚠️ Stale | Still shows Phase 7+8 as 🟢 Active; should be ✅ Complete |
| CLAUDE.md session state | ✅ Accurate | Correctly records R7 as built but untested |
| `kb/roadmap-planning-2026-03-26.md` | ⚠️ Superseded | Planning doc served its purpose; ROADMAP.md was written. Candidate for archival. |

**R7 status:** v2.9.0 was built on 2026-03-25 with the altitude dramaturgy rewrite
(height withheld → earned arc: 0→2→6→7→9→14→25→18→8 blocks). Not yet tested in-game.
The run sheet is at `src/main/resources/shows/demo.archetype_sampler/run-sheet.md`.
Key watch questions from the last session: Does C2 hover feel like gentle bubbling near
the ground? Does C7 lift feel EARNED? Is the player inside the fireworks at C9?

---

## Layer 2 — Show Inventory

| Show | Flat YAML | Folder | Stage | Passes | Notes |
|---|---|---|---|---|---|
| demo.archetype_sampler | ✅ | ✅ | R7 (v2.9.0) | N/A — demo | Built, **not tested** |
| showcase.01 "The Cabinet" | ❌ | ✅ | Brief | 0 / 9 | P1 (Set) not started |
| showcase.02 "The Long Night" | ❌ | ✅ | Brief | 0 / 10 | P1 (Set) not started |
| showcase.03 "Welcome" | ❌ | ✅ | Brief | 0 / 9 | P1 (Set) not started |
| intro.young_persons_guide | ✅ | ❌ | Legacy | N/A | Voice rewrite pending (SCENA-006) |
| intro.scaena_sprite | ✅ | ❌ | Legacy | N/A | No current work planned |
| test.showcase.full | ✅ | ❌ | Legacy | N/A | Phase 1 test artifact |
| demo.flight_modes | ✅ | ❌ | Calibration | N/A | Superseded; findings incorporated |
| demo.levitate_calibration | ✅ | ❌ | Calibration | N/A | Superseded by v2 and v3 |
| demo.levitate_calibration_2 | ✅ | ❌ | Calibration | N/A | Superseded by v3 |
| demo.levitate_calibration_3 | ✅ | ❌ | Calibration | N/A | Findings canonical — physics confirmed |

**Showcase scaffolding completeness:**
All three shows have: brief.md, show-direction.md, intake.md (blank), revision-log.md (blank),
run-sheet.md (blank), and a full `departments/` set with populated briefs for all departments
(including effects.md and fireworks.md, which were added since the demo.archetype_sampler
template). showcase.01 and showcase.03 have `direction/tone.md`; showcase.02 does not — this
appears intentional (Lighting owns the tonal arc in that show, not a standalone tone doc).

**What's blocking all three Showcase shows:** Set (P1) has not been started for any show.
No in-world coordinates exist. No YAML can be authored until Set delivers. P1 is a
scouting/building task that happens in-game, not in Claude — it requires Alan to go
in-world as Zarathale and document real locations.

**Legacy show debt:** `demo.levitate_calibration`, `demo.levitate_calibration_2`,
`demo.flight_modes`, and `test.showcase.full` are superseded calibration/test artifacts
that remain loadable by the plugin. Their findings are captured elsewhere. These can be
removed from `shows/` when convenient — or left as-is; they're not causing harm.

---

## Layer 3 — Cue Library

**41 total cue files (including .gitkeep). 37 production cues across 10 families.**
**Count target (30+) was met and is no longer the relevant metric.** The measure going forward is expressive range — whether the team has the creative vocabulary to serve what a specific show needs. That gets assessed in the context of each production, not by counting files.

| Family | Count | Relative depth | Notes |
|---|---|---|---|
| atmos.* | 3 | Mid | amber drift, cool bloom, warm bloom |
| coda.* | 4 | Mid | bloom fade, quiet curtain, ember last, sound resolve |
| fx.* | 5 | Mid | confetti burst, levitate drift/pulse/surge, lift to height |
| gracie.* | 5 | Mid | Sound motifs — chord, glissando x2, plink x2 |
| still.* | 4 | Mid | cold bloom, still message, particle ash, cave sound |
| mood.* | 2 | **Thin** | Only mood.arrival and mood.wonder.single |
| motif.* | 5 | Mid | arrival bell, still chord, unease descend, warmth banjo, wonder chime |
| overture.* | 1 | **Thin** | Only overture.theme_teaser |
| ramp.* | 4 | Mid | gather particle, cool pulse, warm pulse, sound build |
| world.* | 4 | Mid | dawn/golden/night time, weather rain |
| **test.*** | **4** | — | **Not production cues** — circle_warm, fanfare, cheer_a/b |

**Families to watch:** `mood` (2 cues) and `overture` (1 cue) are the smallest families.
No action needed now — gaps in expressive range will surface when shows call for something
the library can't serve. That's the right time to author new cues.

**Test cues:** `test.*` family (4 cues) are calibration artifacts. They remain loadable but
are not production content. No action needed unless they clutter `/show list`.

**Naming:** All production cues follow `[category].[archetype].[variant]` convention ✅.
All have `tags:` arrays per spec §10 taxonomy ✅ (confirmed as of 2026-03-24).

---

## Layer 4 — KB Calibration Readiness

**Summary: 1/12 departments calibration-ready (Show Director). Phase 9 Track A not started.**

Calibration-ready definition per `ROADMAP.md §Phase 9`:
full instrument inventory + status tags + Tone Translation section + ≥1 YAML example
per instrument + gaps in ops-inbox.md + used in at least one show session.

| Department | Est. Depth | Migration | Calibration Ready | Primary Gap |
|---|---|---|---|---|
| Show Director | 514 lines | ✅ Folder | ✅ **Yes** | — |
| Choreography | 617 lines | ❌ Flat | ⚠️ Likely — verify | Folder migration; show session use |
| Sound | 590 lines | ❌ Flat | ⚠️ Likely — verify | Flat file + music-director.md in subfolder (inconsistent structure) |
| Casting | 585 lines | ❌ Flat | ⚠️ Likely — verify | Folder migration |
| Camera (Mark) | 571 lines | ❌ Flat | ⚠️ Likely — verify | Folder migration |
| Stage Manager (Kendra) | 452 lines | ❌ Flat | ⚠️ Partial | Must migrate to `stage-management/`; show-creation-process.md not yet written |
| Fireworks (Mira) | 381 lines | ❌ Flat | ⚠️ Partial | Folder migration; calibration-ready checklist not verified |
| Wardrobe | 347 lines | ❌ Flat* | ⚠️ Partial | Flat main KB + wardrobe/ subfolder (inconsistent) |
| Voice | 334 lines | ❌ Flat* | ⚠️ Partial | Flat main KB + voice/ subfolder (inconsistent) |
| Set | 309 lines | ❌ Flat* | ❌ Not ready | Spatial design methodology underdeveloped; flat KB + set/ subfolder |
| Lighting | 312 lines | ❌ Flat | ❌ Not ready | Only 3 instruments documented |
| **Effects** | **260 lines** | ❌ Flat | ❌ **Not ready** | Thinnest; highest show impact (player body/physics) |

*Flat KB file exists + supplementary files already in a subfolder = partial migration.
Full migration requires moving the flat `.kb.md` into the folder as `[dept]/[dept].kb.md`.

**Phase 9 Track A uplift priority (per ROADMAP.md):**
1. Effects — thinnest, highest impact
2. Set — spatial methodology needed before any in-world scouting output can be used
3. Lighting — only 3 instruments (and showcase.02 requires Lighting to lead)
4. Wardrobe — thin on creative examples

**Phase 10 gate:** Before any showcase pass can begin, Set must complete P1 (in-world
scouting). Before Stage Management can write YAML for any show, show-creation-process.md
must be drafted. Effects, Set, and Lighting being thin is a risk for the passes that
depend on them — ideally those three KBs are uplifted before those departments' passes run.

---

## Layer 5 — Structural Drift

**CLAUDE.md "Current Phase" table is stale:**
```
Phase 7 | 🟢 Active  ← should be ✅ Complete — 2026-03-24
Phase 8 | 🟢 Active  ← should be ✅ Complete — 2026-03-24
Phase 9 | not listed ← should be 🟢 Active
Phase 10 | not listed ← should be 🟢 Active (Showcase Series scaffolded)
```

**CLAUDE.md skills table is missing two skills:**
- `skills/dept-kb-builder/SKILL.md` — exists, not listed in CLAUDE.md
- `skills/audit-this-repo/SKILL.md` — just created, not listed in CLAUDE.md

**CLAUDE.md repo structure diagram** does not list `showcase.01`, `showcase.02`,
`showcase.03` show folders — only the `[show_id]/` pattern example. Not a problem
(the pattern covers them) but the current ROADMAP.md notes them as active work.

**No other structural drift found.** All paths described in CLAUDE.md resolve correctly.

---

## Layer 6 — Cross-Document Integrity

**Orphaned references:** None found. All KB-to-KB cross-references resolve.

**Redundant content:**

1. **`kb/roadmap-planning-2026-03-26.md`** — This was the planning context doc that recorded
   the conversation before ROADMAP.md could be written. Its two open questions have been
   resolved and ROADMAP.md reflects their answers. The doc is now superseded. Candidate for
   archival to `kb/artifacts/` or deletion.

2. **`kb/departments/show-director.kb.md` (flat tombstone)** — Correctly handled: it contains
   a redirect message pointing to the folder version. No action needed, but it represents
   the migration pattern to follow for other depts.

3. **Inconsistent dept structure (wardrobe, voice, set, sound):** These depts have a flat
   `.kb.md` at root AND a subfolder with supplementary files (`wardrobe/emotional-register.md`,
   etc.). The flat file is the live KB; the subfolder holds references. This is a partially-
   migrated state — functional but inconsistent with the target folder structure. Each needs
   to migrate the flat `.kb.md` INTO the folder on its next uplift session.

**Long-term storage quality:**

| File | Quality | Notes |
|---|---|---|
| CLAUDE.md | Good | Phase table stale; session state current; otherwise solid |
| ROADMAP.md | ✅ Strong | Fully current; Phases 9/10/11 well-defined with acceptance criteria |
| kb/production-team.md | ✅ Strong | Authoritative; dept table paths mostly current |
| kb/system/spec.md | Stable | Phase 1 era; no changes needed for current phase |
| kb/roadmap-planning-2026-03-26.md | ⚠️ Superseded | Served its purpose; candidate for archival |
| kb/cue-library-survey.md | ⚠️ Stale | CLAUDE.md flags as out-of-date; provides no value over direct cue scan |
| docs/*.runsheet.md | ⚠️ Migration debt | 4 legacy runsheets; CLAUDE.md acknowledges these. Low priority. |

**Skill file quality:**

| Skill | Size | Quality | Notes |
|---|---|---|---|
| kb-builder | ~450 lines | ✅ Strong | Well-structured; Mode 1/2/3 clear; calibration-ready checklist authoritative |
| dept-kb-builder | Unknown | ⚠️ Not listed | Not indexed in CLAUDE.md; relationship to kb-builder unclear |
| production-review | Not read | — | Not assessed this session |
| show-import-process | Not read | — | Not assessed this session |
| audit-this-repo | ~230 lines | ✅ New — just created | |

`dept-kb-builder` is the main flag: it exists, it's not listed in CLAUDE.md, and its
relationship to `kb-builder` is unclear. Is it a separate skill or a replacement? This
should be clarified before the next KB uplift session.

---

## Summary: Proposed Changes

| # | File | Change | Effort |
|---|---|---|---|
| 1 | `CLAUDE.md` §Current Phase | Update phase table: mark Phase 7+8 ✅ Complete; add Phase 9 🟢 Active, Phase 10 🟢 Active | Small |
| 2 | `CLAUDE.md` §Skills table | Add `audit-this-repo` and `dept-kb-builder` entries | Small |
| 3 | `CLAUDE.md` §Session State | Update session state to reflect this audit session and that R7 is still pending | Small |
| 4 | `kb/roadmap-planning-2026-03-26.md` | Archive to `kb/artifacts/` (superseded by ROADMAP.md) | Small |
| 5 | `kb/cue-library-survey.md` | Archive to `kb/artifacts/` (flagged as out-of-date; direct cue scan is better) | Small |

**Total: 5 proposed changes — all small**

Changes 1–3 are high priority: CLAUDE.md should accurately reflect the current phase
and skill inventory before any subsequent session work begins.
Changes 4–5 clean up superseded docs; defer if you'd rather keep them accessible.

---

## Recommended Session Focus

**Immediate (this session):**
Apply changes 1–3 to CLAUDE.md. Then decide: run R7 first, or begin showcase
departmental work?

**If R7 hasn't run yet** — that's the right first move. It's v2.9.0 sitting built
and unvalidated. The altitude dramaturgy rewrite is substantive. It should be confirmed
before the archetype sampler is considered stable.

**If R7 is deferred** — the next best move is showcases. All three shows are at the
same stage (Brief, P1 not started). Set (P1) is the gate for all of them. The most
productive next session work is: pick one show, open P1 (Set), and begin the scouting
brief for Zarathale.

**Phase 9 Track A note:** Effects, Set, and Lighting KBs are thin and those departments
have critical roles in the showcases. Consider scheduling KB uplift sessions for Effects
and Lighting before those departments' passes run — otherwise Claude will be authoring
YAML from underdeveloped KBs.

---

*Audit complete. 5 proposed changes. Awaiting confirmation — say "apply all," list numbers to apply, or "skip all."*
