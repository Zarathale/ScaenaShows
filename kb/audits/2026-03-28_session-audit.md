---
date: 2026-03-28
purpose: Session orientation — post-migration state check
status: findings — awaiting confirmation
prior_audit: 2026-03-26_pre-showcase-audit.md
---

# Repo Audit — 2026-03-28

## Overall Health

The repo is in its best shape yet. The department KB migration is complete — all 12 departments now live in proper folder structure, and two superseded docs have been archived. CLAUDE.md has been updated since the last audit. The cue library and show scaffolding are unchanged and solid. The main concern remains unchanged: R7 (`demo.archetype_sampler`) has not been tested in-game, and no Showcase show has started P1 (Set). The repo is well-organized and ready to do work — it just hasn't done the next piece of work yet.

## Delta Since Last Audit

Last audit: 2026-03-26. Five changes were proposed.

**Applied since last audit:**
- ✅ `kb/roadmap-planning-2026-03-26.md` archived to `kb/artifacts/` (Change 4)
- ✅ `kb/cue-library-survey.md` archived to `kb/artifacts/` (Change 5)
- ✅ All 12 department KBs migrated to folder structure — this was a recommended next step, not a numbered change, but it happened. Significant.
- ✅ `kb/departments/stage-management/show-creation-process.md` now exists (flagged as missing in last audit under Stage Management gap)
- ✅ CLAUDE.md has been updated — phase table is gone, structure simplified (Changes 1–3 addressed)

**New since last audit:**
- `kb/artifacts/style-guide.html` and `kb/artifacts/ui-mockup.html` — two HTML files in artifacts. Origin and purpose unclear; they don't obviously fit the plugin/show development context. Flagged below.
- `skills/dept-kb-builder/SKILL.md` — exists but still not listed in CLAUDE.md's repo structure diagram

**Still open:**
- R7 (`demo.archetype_sampler`) in-game test — still pending
- All three Showcase shows at Brief stage, P1 (Set) not started

---

## Layer 1 — Session State

| Check | Status | Notes |
|---|---|---|
| Current version | **2.9.0** | `build.gradle.kts` matches CLAUDE.md ✅ |
| Immediate next step (CLAUDE.md) | R7 in-game test | Still listed, still pending ⚠️ |
| R7 test | **Pending** | v2.9.0 built 2026-03-25; never run in-game |
| showcase-roadmap.md | ✅ Current | All three shows at 0/N passes; accurately reflects Brief stage |
| Show status files | ✅ Accurate | All three status.md files current as of 2026-03-26 |
| ROADMAP.md | ✅ Current | Phases 9/10/11 well-defined; demo table accurate |
| CLAUDE.md structure | ✅ Updated | Phase table removed; session state accurate; skills diagram partially current |

---

## Layer 2 — Show Inventory

| Show | Flat YAML | Folder | Stage | Passes | Notes |
|---|---|---|---|---|---|
| demo.archetype_sampler | ✅ | ✅ | R7 (v2.9.0) | N/A | Built, **not tested** |
| showcase.01 "The Cabinet" | ❌ | ✅ | Brief | 0 / 9 | P1 (Set) not started |
| showcase.02 "The Long Night" | ❌ | ✅ | Brief | 0 / 10 | P1 (Set) not started; no tone.md (intentional per last audit) |
| showcase.03 "Welcome" | ❌ | ✅ | Brief | 0 / 9 | P1 (Set) not started |
| intro.young_persons_guide | ✅ | ❌ | Legacy | N/A | Voice rewrite pending (SCENA-006) |
| intro.scaena_sprite | ✅ | ❌ | Legacy | N/A | No current work planned |
| test.showcase.full | ✅ | ❌ | Legacy | N/A | Phase 1 test artifact; superseded |
| demo.flight_modes | ✅ | ❌ | Calibration | N/A | Superseded; findings incorporated |
| demo.levitate_calibration | ✅ | ❌ | Calibration | N/A | Superseded by v2 and v3 |
| demo.levitate_calibration_2 | ✅ | ❌ | Calibration | N/A | Superseded by v3 |
| demo.levitate_calibration_3 | ✅ | ❌ | Calibration | N/A | Findings canonical — physics confirmed |

**What's blocking all three Showcase shows:** Set (P1) is the gate. No in-world coordinates exist. No YAML can be authored until Zarathale scouts a location and files a scouting report. This is an in-game task, not a Claude session task.

**No changes to show inventory since last audit.** Same count, same stages.

---

## Layer 3 — Cue Library

**37 production cues across 10 families. 4 test cues. No change since last audit.**

| Family | Count | Notes |
|---|---|---|
| atmos.* | 3 | ember_drift, cool_bloom, warm_bloom |
| coda.* | 4 | bloom_fade, curtain.quiet, ember_last, sound.resolve |
| fx.* | 5 | confetti_burst, levitate_and_drift, levitate_pulse, levitate_surge, lift_to_height |
| gracie.* | 5 | chord.low_ominous, glissando.accent, glissando.dreamy, plink.fifth, plink.fourth |
| grief.* | 4 | bloom.cold, message.still, particle.ash, sound.cave |
| mood.* | 2 | arrival, wonder.single — **thin** |
| motif.* | 5 | arrival.bell, grief.chord, unease.descend, warmth.banjo, wonder.chime |
| overture.* | 1 | theme_teaser — **thin** |
| ramp.* | 4 | particle.gather, pulse.cool, pulse.warm, sound.build |
| world.* | 4 | time.dawn, time.golden, time.night, weather.rain |
| **test.*** | **4** | circle_warm, fanfare, cheer_a, cheer_b — not production cues |

**30+ target is met and no longer the relevant metric.** Expressive range gaps (`mood`, `overture`) will surface when a specific show calls for them. No action needed now.

**Naming convention:** All production cues follow `[category].[archetype].[variant]` ✅

---

## Layer 4 — KB Calibration Readiness

**Major change since last audit: all 12 departments are now in folder structure.**

The last audit found 10/12 as flat files. All are now in `kb/departments/[dept-slug]/[dept-slug].kb.md`. This is a structural win. However, folder migration and calibration-readiness are separate questions — migration means the files are organized correctly; calibration-readiness requires verifying content depth against the checklist (instrument inventory, tone translation, YAML examples, patterns, backlog). That content check was not done in this session and requires `skills/kb-builder/SKILL.md §Mode 2`.

**What was already known from last audit, updated for migration status:**

| Department | Migration | Calibration Ready (est.) | Notes |
|---|---|---|---|
| Show Director | ✅ Folder | ✅ Yes | Verified in last audit |
| Stage Management | ✅ Folder | ⚠️ Improving | `show-creation-process.md` now exists — gap from last audit addressed |
| Choreography | ✅ Folder | ⚠️ Likely — verify | Not formally checked |
| Sound | ✅ Folder | ⚠️ Likely — verify | `music-director.md` supplementary file present |
| Casting | ✅ Folder | ⚠️ Likely — verify | Not formally checked |
| Camera | ✅ Folder | ⚠️ Likely — verify | Not formally checked |
| Fireworks | ✅ Folder | ⚠️ Partial — verify | Not formally checked |
| Wardrobe | ✅ Folder | ⚠️ Partial — verify | Multiple supplementary files present |
| Voice | ✅ Folder | ⚠️ Partial — verify | `showsprite.context.md` supplementary present |
| Set | ✅ Folder | ❌ Not ready | Spatial methodology; `stage-registry.md` present but P1 requires in-world scouting |
| Lighting | ✅ Folder | ❌ Not ready | Only 3 instruments documented (per last audit) |
| **Effects** | ✅ Folder | ❌ **Not ready** | Thinnest KB; highest show impact |

**Phase 10 gate is unchanged:** Set must complete P1 (in-world) before any Showcase YAML can begin. Effects, Set, and Lighting being thin is a risk for when their passes run — KB uplift for those three should happen before their respective passes, not during.

**Recommended uplift priority (unchanged from last audit):**
1. Effects — thinnest, highest player-body impact
2. Lighting — 3 instruments, and showcase.02 requires Lighting to lead
3. Set — spatial methodology; secondary to in-world scouting which Alan does independently

---

## Layer 5 — Structural Drift

**One item found:**

**`skills/dept-kb-builder/SKILL.md` exists but is not listed in CLAUDE.md's repo structure diagram.** The diagram lists:
```
├── skills/
│   ├── audit-this-repo/SKILL.md
│   ├── kb-builder/SKILL.md
│   ├── production-review/SKILL.md
│   └── show-import-process/SKILL.md
```
`dept-kb-builder/SKILL.md` is missing from this list. Its relationship to `kb-builder` is also still unresolved (replacement? complement? different mode?). This should be documented before a KB uplift session is run, to avoid ambiguity about which skill to use.

**No other structural drift.** All paths described in CLAUDE.md resolve correctly. Folder structure matches documentation.

---

## Layer 6 — Cross-Document Integrity

**Orphaned references:** None found.

**Unexplained files in `kb/artifacts/`:**

`kb/artifacts/style-guide.html` and `kb/artifacts/ui-mockup.html` are present and have no documentation. They don't appear to be plugin development artifacts (no plugin UI exists), and they're not referenced by any other file in the repo. Their origin is unclear. They may be from a prior session's exploratory work or an unrelated side project. They should be acknowledged and either documented (with a note in the artifacts folder about what they are) or removed.

**`production-team.md` KB paths:** All departments now in folder structure. The paths table in `production-team.md` should be verified to confirm it references the correct `[dept-slug]/[dept-slug].kb.md` paths — if it was written when KBs were flat, the paths may be outdated. Not read this session; flag for spot-check.

**Long-term storage quality:**

| File | Quality | Notes |
|---|---|---|
| CLAUDE.md | ✅ Good | Updated; session state current; dept-kb-builder gap in skills diagram |
| ROADMAP.md | ✅ Strong | Fully current |
| kb/production-team.md | ⚠️ Spot-check | KB paths may need updating after dept migration |
| kb/system/spec.md | Stable | No changes needed this phase |
| kb/artifacts/ | ⚠️ Two unexplained HTMLs | `style-guide.html` + `ui-mockup.html` need documentation or removal |

---

## Summary: Proposed Changes

| # | File | Change | Effort |
|---|---|---|---|
| 1 | `CLAUDE.md` | Add `dept-kb-builder/SKILL.md` to repo structure skills diagram | Small |
| 2 | `CLAUDE.md` | Update session state: this audit run, R7 still pending, dept migration complete | Small |
| 3 | `kb/production-team.md` | Spot-check KB paths table — verify all dept paths use `[dept-slug]/[dept-slug].kb.md` format | Small |
| 4 | `kb/artifacts/style-guide.html` + `ui-mockup.html` | Investigate: document what these are, or remove | Small |
| 5 | `skills/dept-kb-builder/SKILL.md` | Read and document its relationship to `kb-builder` — are they complementary or does one replace the other? | Small |

**Total: 5 proposed changes — all small**

Changes 1–2 are the priority: CLAUDE.md should accurately index the skills before any KB session starts.
Change 3 is worth doing quickly — a broken path in `production-team.md` would be a quiet landmine.
Changes 4–5 are housekeeping; defer if there's more interesting work to do.

---

## Recommended Session Focus

**Repo is clean and ready to work.** Two real choices:

**Option A — R7 first.** `demo.archetype_sampler` has been sitting built and untested since 2026-03-25. The altitude dramaturgy rewrite is substantive (earned arc: 0→2→6→7→9→14→25→18→8 blocks). Run it now — `/show play demo.archetype_sampler` on the test machine — and bring back a debrief. It will take 10–15 minutes of in-game time. The run sheet is at `src/main/resources/shows/demo.archetype_sampler/run-sheet.md`.

**Option B — Start a Showcase P1.** All three shows are equally blocked on Set scouting. Pick one, open `showcase.01` (or whichever calls to you), and begin the P1 (Set) scouting brief for Zarathale — the output is a brief that Alan takes into Minecraft to scout a location. This is a Claude session task; the actual in-world scouting is Alan's. Starting P1 unblocks everything downstream.

**Option C — KB uplift.** Effects and Lighting are the thinnest KBs with the highest show impact. Running a `dept-kb-builder` or `kb-builder` session on either would pay forward into showcase passes. Worth doing before those departments' passes run — ideally before any YAML authoring begins.

*R7 is the oldest open item. If it hasn't run yet, this is the session to close it.*

---

*Audit complete. 5 proposed changes. Awaiting confirmation — say "apply all," list numbers to apply, or "skip all."*
