---
date: 2026-03-31
purpose: Post-OPS-026 state check — showcase.01 Brief progress, engine at 2.19.0
status: findings — awaiting confirmation
prior_audit: 2026-03-28_session-audit.md
---

# Repo Audit — 2026-03-31

## Overall Health

The repo is in excellent structural shape. The engine shipped a significant capability
upgrade in 2.19.0 (SPAWN_ENTITY attribute support + BOSS_HEALTH_BAR), and showcase.01
has made substantial Brief-stage progress across 6 sessions — Gates 1 and 2 closed, all
battle sequence params locked, arc fully staged. The main structural concern is
`showcase-roadmap.md §showcase.01`, which is entirely stale and describes the retired
"The Cabinet" concept rather than "Preparing for Battle." This is a medium-effort fix and
should happen before any formal pass work begins. Two small integrity items (OPS-009
Resolved housekeeping and a stale CLAUDE.md "Next" line) are quick cleanup.

## Delta Since Last Audit (2026-03-28)

**Applied since last audit:**
- ✅ `skills/dept-kb-builder/SKILL.md` — silently absent now (was present in 2026-03-28
  audit but never listed in CLAUDE.md; resolution unclear — flagged below)
- ✅ showcase.01 active development — 6 sessions completed, Gates 1–2 closed, scouting
  brief and field guide complete, full battle sequence locked
- ✅ OPS-026 shipped (2.19.0) — SPAWN_ENTITY attribute support + BOSS_HEALTH_BAR
- ✅ OPS-009 shipped — PLAYER_CHOICE implemented and live (not yet reflected in ops-inbox)
- ✅ Version: 2.9.0 → 2.19.0 (10 minor versions; significant engine growth)
- ✅ CLAUDE.md Active Shows table updated to correct showcase.01 title

**Still open from 2026-03-28 audit:**
- `kb/artifacts/style-guide.html` and `ui-mockup.html` — still present, still uncontextualized
- R8 not yet scheduled (not a blocker; calibration is behind showcase work)

**New issues not in prior audit:**
- showcase-roadmap.md §showcase.01 is entirely stale (retired show concept)
- OPS-009 shipped but not moved to Resolved in ops-inbox
- CLAUDE.md "Where We Are" section shows stale "Next" guidance

---

## Layer 1 — Session State

| Check | Status | Notes |
|---|---|---|
| Current version | **2.19.0** | `build.gradle.kts` ✅ matches CLAUDE.md ✅ |
| R7 test | **✅ Complete** | Debrief filed 2026-03-28; findings in run-sheet §General Findings |
| R8 scheduled | **Not yet** | Not a blocker; calibration deprioritized behind showcase work |
| OPS-026 | **✅ Shipped (2.19.0)** | SPAWN_ENTITY attrs + BOSS_HEALTH_BAR fully implemented |
| OPS-009 | **✅ Shipped** | PLAYER_CHOICE live — **but still in Open section of ops-inbox** ⚠️ |
| CLAUDE.md "Next" guidance | **⚠️ Stale** | Still says "pick one Showcase, open P1 (Set)" — showcase.01 has had 6 sessions |
| CLAUDE.md last audit date | **⚠️ Stale** | Still shows 2026-03-28; needs 2026-03-31 |
| showcase.01 active stage | Brief — Gate 3 open | Gates 1–2 closed; Set scouting is the live next step |
| showcase-roadmap.md | **⚠️ Stale for .01** | Shows 0/N passes complete (accurate numerically); content is wrong show |

---

## Layer 2 — Show Inventory

| Show | Flat YAML | Folder | Stage | Notes |
|---|---|---|---|---|
| showcase.01 "Preparing for Battle" | ❌ | ✅ | Brief — Gate 3 open | No `brief.md` at root; show-params serves this role. 6 sessions complete. |
| showcase.02 "The Long Night" | ❌ | ✅ | Brief — not started | `brief.md` ✅; no sessions yet |
| showcase.03 "Welcome" | ❌ | ✅ | Brief — not started | `brief.md` ✅; no sessions yet |
| demo.archetype_sampler | ✅ | ✅ | R7 ✅ complete | R8 not scheduled |
| test.tier1 | ❌ (inside folder) | ✅ | Test artifact | YAML is inside the folder; not a flat show. Not loadable by plugin scanner. |
| intro.young_persons_guide | ✅ | ❌ | Legacy | Voice rewrite pending (SCENA-006) |
| intro.scaena_sprite | ✅ | ❌ | Legacy | No current work planned |
| test.showcase.full | ✅ | ❌ | Legacy test artifact | Phase 1 verification; superseded |
| demo.flight_modes | ✅ | ❌ | Calibration legacy | Superseded; findings incorporated |
| demo.levitate_calibration (×3) | ✅ | ❌ | Calibration legacy | Levitation tuning artifacts; superseded |

**What's blocking showcase work:**
- **showcase.01:** Gate 3 (Set Scouting) open. Zarathale needs to scout Sites B–F using
  `showcase.01.scouting-field-guide.md`. Gate 4 (Intake) follows. YAML authoring blocked
  on Gate 4 + two remaining TBDs: `victory_cue` fireworks and `victory_levitation_amplifier`.
- **showcase.02, .03:** Not started. No Gate or pass work has begun.

**Notes:**
- showcase.01 has no `brief.md` at show root. The `_template/brief.md` scaffold was never
  populated for this show — the archived `direction/_archive/brief-cabinet-retired.md`
  covers the retired concept. For "Preparing for Battle," `show-params.md` and
  `show-direction.md` together serve the brief function. This is not a blocking issue —
  just a template deviation worth noting. A thin `brief.md` pointing to show-params could
  be added for consistency with .02 and .03.

---

## Layer 3 — Cue Library

| Family | Count | Notes |
|---|---|---|
| atmos.* | 3 | ember_drift, cool_bloom, warm_bloom |
| coda.* | 4 | bloom_fade, curtain.quiet, ember_last, sound.resolve |
| fx.* | 5 | confetti_burst, levitate_and_drift, levitate_pulse, levitate_surge, lift_to_height |
| gracie.* | 5 | chord.low_ominous, glissando.accent, glissando.dreamy, plink.fifth, plink.fourth |
| mood.* | 2 | arrival, wonder.single |
| motif.* | 5 | arrival.bell, still.chord, unease.descend, warmth.banjo, wonder.chime |
| overture.* | 1 | theme_teaser |
| ramp.* | 4 | particle.gather, pulse.cool, pulse.warm, sound.build |
| still.* | 4 | bloom.cold, message.breath, particle.ash, sound.cave |
| world.* | 4 | time.dawn, time.golden, time.night, weather.rain |
| **Total production** | **37** | ✅ 30+ target met |
| test.* | 4 | fireworks.circle_warm, fireworks.fanfare, group.cheer_a, group.cheer_b — not counted |

**Notable change since last audit:** `still.message.still.yml` renamed to
`still.message.breath.yml`. No naming convention violation — the new name follows
`[category].[archetype].[variant]` correctly.

**Thin families worth noting:** `overture.*` has 1 entry (theme_teaser). `mood.*` has 2.
Neither is a blocker — calibration backlog is the mechanism for growing these families.

---

## Layer 4 — KB Calibration Readiness

All 12 departments confirmed in folder structure (`kb/departments/[dept-slug]/[dept-slug].kb.md`).
Migration complete as of 2026-03-28. No regressions.

| Department | Folder | Supplementary files |
|---|---|---|
| camera | ✅ | brief-to-direction.md |
| casting | ✅ | — |
| choreography | ✅ | brief-to-direction.md |
| effects | ✅ | brief-to-direction.md |
| fireworks | ✅ | brief-to-direction.md |
| lighting | ✅ | — |
| set | ✅ | stage-registry.md |
| show-director | ✅ | direction-briefing.md, direction.commitments.md, showcase-roadmap.md |
| sound | ✅ | music-director.md, brief-to-direction.md |
| stage-management | ✅ | show-creation-process.md |
| voice | ✅ | showsprite.context.md, brief-to-direction.md |
| wardrobe | ✅ | emotional-register.md, equipment-slots.md, invisible-body.md, mob-variants.md, brief-to-direction.md |

**12/12 departments in folder structure ✅**

Depth-of-content calibration readiness is not fully audited in this pass (would require
reading each kb.md). The prior audit (2026-03-28) confirmed all 12 were substantively
populated at migration. A full kb-builder pass is the right tool for depth assessment —
see `skills/kb-builder/SKILL.md §Mode 1`.

---

## Layer 5 — Structural Drift

**`skills/dept-kb-builder/SKILL.md` — status unclear ⚠️**
The 2026-03-28 audit noted this file existed at `skills/dept-kb-builder/SKILL.md` but
was never listed in CLAUDE.md's repo structure diagram. Current file scan shows only 4
skills: `audit-this-repo`, `kb-builder`, `production-review`, `show-import-process`. The
`dept-kb-builder` folder and its SKILL.md are gone. Whether this was intentional deletion
or merge into `kb-builder` is unclear. CLAUDE.md never listed it, so the structural index
is clean — but if the skill had useful content, that content may be lost.

**`kb/artifacts/style-guide.html`, `ui-mockup.html` — origin still unresolved ⚠️**
Flagged in 2026-03-28 audit, no action taken. These files don't obviously fit the
plugin/show development context. Alan should confirm: keep (and add a note about their
purpose) or delete.

**All 4 current skills match CLAUDE.md skills diagram ✅**

**KB folder structure matches CLAUDE.md repo structure diagram ✅**

---

## Layer 6 — Cross-Document Integrity

**⚠️ showcase-roadmap.md §showcase.01 is entirely stale — MEDIUM issue**

The `showcase-roadmap.md` file (last updated 2026-03-26) contains a showcase.01 section
titled "The Cabinet" with a pass sequence (P1–P9) designed for a completely different
show: an Allay collection show with 6 vignettes (A–F rondo), featuring Allay management,
a Strider at Site D, aerial presence at Site E, and a "punchline creature" at Site F.
None of this applies to "Preparing for Battle."

The pass sequence itself (P1-Set, P2-Effects+Fireworks+Camera, etc.) is the right
structural template, but every row's content — the "Receives from prior passes" and
"Produces" columns — is wrong. The status tracking table at the bottom (0/9 passes) is
numerically accurate but refers to the wrong pass design.

The show's current pre-production model uses Gates (not passes) for the pre-YAML phase.
Gates 1–4 are complete or in progress. Once Gate 4 closes, YAML authoring begins without
a separate formal pass sequence. The showcase-roadmap.md P1-P9 pass structure may still
apply as the post-Gate session model, but it needs to be written for this show.

This needs to be rewritten. The title alone creates confusion; the wrong pass content
is worse because it will misdirect future sessions that open the roadmap for reference.

**⚠️ OPS-009 shipped but still listed as Open in ops-inbox — SMALL issue**

PLAYER_CHOICE (OPS-009) has been implemented and is confirmed working (showcase.01
status.md notes it under YAML readiness ✅). The OPS-026 Resolved entry explicitly notes
"unblocked pending OPS-009 PLAYER_CHOICE — already shipped." However, OPS-009 itself
still appears in the Open section as `[future-capability]` — it has never been moved to
Resolved. This is a housekeeping inconsistency: any session scanning the Open section
will see PLAYER_CHOICE as a future item when it's actually live.

**⚠️ CLAUDE.md "Where We Are" section is stale — SMALL issue**

The section reads: *"Next: pick one Showcase, open P1 (Set), begin the scouting brief for
Zarathale."* This was accurate as of 2026-03-28. Since then, showcase.01 has had 6 sessions:
the scouting brief exists, Gates 1–2 are closed, the entire arc and battle sequence are
designed. The actual next step is Gate 3 (Zarathale scouts Sites B–F). The "last audit"
date also needs updating to 2026-03-31.

**showcase-roadmap.md Department Engagement Index — minor inconsistency**
The table for showcase.01 describes pass numbers (P3: Casting, P5: Choreography, etc.)
based on the old pass sequence. When the showcase.01 section is rewritten, the engagement
index row needs to be updated to match.

**showcase.01 `brief.md` absent — cosmetic deviation, not a blocker**
showcase.02 and showcase.03 both have `[show_id].brief.md` at folder root. showcase.01
does not (the Cabinet brief was archived). `show-params.md` and `show-direction.md` serve
this function effectively for "Preparing for Battle." Either add a thin brief.md that
points to show-params, or accept the deviation. No action required unless desired for
structural consistency.

---

## Summary: Proposed Changes

| # | File | Change | Effort |
|---|---|---|---|
| 1 | `CLAUDE.md` | Update "Where We Are" §Immediate pending: replace stale "Next" guidance with showcase.01 current state (Gate 3 open, Zarathale scouting). Update "last audit" date to 2026-03-31. | Small |
| 2 | `ops-inbox.md` | Move OPS-009 PLAYER_CHOICE from Open to Resolved section, matching OPS-026 format. | Small |
| 3 | `kb/departments/show-director/showcase-roadmap.md` | Rewrite showcase.01 section entirely for "Preparing for Battle" — correct title, show description, and P1–P9 pass table. Update Department Engagement Index row. Update status tracking. | Medium |
| 4 | `showcase.01/showcase.01.brief.md` (new file) | Add a thin brief.md pointing to show-params.md as the structural facts document (for template consistency with .02 and .03). | Small (optional) |
| 5 | `kb/artifacts/style-guide.html` + `ui-mockup.html` | Alan decides: add a purpose note, or delete. Flagged from prior audit — no action taken. | Alan's call |

**Total: 3 confirmed proposed changes (1–3); 2 deferred to Alan (4–5)**

---

To proceed: say which changes you want made (by number), or "apply all."
To skip: say "skip [number]" or "skip all."
For Change 3 (showcase-roadmap.md rewrite): I'll propose the content and show it to you before writing.

### Recommended Session Focus

Change 3 (showcase-roadmap.md rewrite) is the most time-sensitive: any session that opens
the roadmap to begin a showcase.01 pass will get a misdirecting answer from stale content.
Changes 1 and 2 are quick cleanup. Recommended order: 1 → 2 → 3.

After that, the session's natural next move is Gate 3: open `showcase.01.scouting-report.md`
and walk through the scouting pass with Zarathale (the field guide is already written at
`direction/showcase.01.scouting-field-guide.md`).
