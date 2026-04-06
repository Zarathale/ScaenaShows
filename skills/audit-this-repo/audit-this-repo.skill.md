---
name: audit-this-repo
description: >
  Structural and creative health check for the ScaenaShows repo. Load when Alan says any
  variation of "audit the repo," "audit this repo," "let's audit before we start," "health
  check," "what's the state of things," "catch me up," or "what do we have." Performs a
  methodical scan across six layers: session state sync, show inventory, cue library,
  KB calibration readiness, structural drift, and cross-document integrity. Always produces
  a findings report saved to kb/audits/ and waits for explicit confirmation before touching
  any files. Also surfaces what the session should do next based on CLAUDE.md session state.
---

# Repo Audit — ScaenaShows

## Purpose

Run a full structural and creative health check before beginning show or KB work. This is not about filling in missing content — it's about establishing a current-state picture: what's been built, what's in progress, what's drifted, and what should happen next in this session.

**Default mode: audit-then-confirm.** Never make changes during the audit phase. Present findings first, wait for explicit confirmation before touching anything.

---

## Before You Start

**Step 1 — Delta baseline.**

Check `kb/audits/` for the most recent prior audit. If one exists, note:
- Date of last audit
- What issues were found and which were resolved
- What was still open
This lets you focus on what's changed rather than re-auditing everything at equal depth.

**Step 2 — Read foundation files in this order.**

Read these before assessing anything:
1. `CLAUDE.md` — current phase, session state, open questions, versioning policy
2. `ROADMAP.md` — phase status (especially whether it reflects current phase structure)
3. `kb/departments/show-director/show-director.showcase-roadmap.md` — pass status for all three showcases

Then do a file tree scan:
- `find src/main/resources/cues -name "*.yml" | sort` — full cue inventory
- `find src/main/resources/shows -type f | sort` — full show inventory
- `find kb/departments -type f | sort` — KB folder structure + migration status
- `find skills -name "*.skill.md" | sort` — skill inventory

Assessment must be based on what's actually in the files, not on assumptions from prior sessions.

---

## The Scan: Six Layers

---

### Layer 1 — Session State Sync

This is the most important layer for orienting a fresh session. Before any show work begins, Claude needs to know exactly where things stand.

**Check these specifically:**

**CLAUDE.md session state:**
- Is the "Current Phase" table accurate? (Phases 7+8 should be ✅ Complete; Phase 9/10 should be active)
- What does "Session State" say is the immediate next step — and did it happen?
- Are the "Open Questions" still open, or have any been resolved since the last session?
- Does the current version in `build.gradle.kts` match what CLAUDE.md says the current version is?

**R7 test status:**
- CLAUDE.md records that `demo.archetype_sampler` R7 (v2.9.0) was built but not tested.
- If Alan mentions having run it, capture any debrief. If not, flag it as still pending.

**Roadmap alignment:**
- `ROADMAP.md` should describe Phases 9, 10, 11. If it still describes Phases 7/8 as active, flag it as obsolete.
- `kb/roadmap-planning-2026-03-26.md` contains two open questions (A and B) that must be answered before ROADMAP.md can be rewritten. Check if these were answered.

**Output for this layer:**
```
#### Session State
- Current version: [x.y.z] — matches CLAUDE.md: [yes/no]
- Immediate next step (per CLAUDE.md): [describe]
- Did it happen? [yes / no / unclear]
- Open Question A (roadmap): [resolved / still open]
- Open Question B (roadmap): [resolved / still open]
- ROADMAP.md current? [yes / no — if no, flag for rewrite]
- R7 test: [complete / pending]
```

---

### Layer 2 — Show Inventory

ScaenaShows shows live in `src/main/resources/shows/`. Audit both what exists and what stage each show is at.

**For each show:**

| Category | What to check |
|---|---|
| Flat YAML | Does a loadable `.yml` exist for plugin compatibility? |
| Folder | Does a `[show_id]/` folder exist with brief, run-sheet, direction/, departments/? |
| Development stage | What does `brief.md`'s `development_stage:` frontmatter say? |
| Pass status | For showcase.01/02/03 — check `show-director.showcase-roadmap.md` for completed passes (✅ vs [ ]) |
| Department briefs | Are `departments/*.md` files populated with real decisions, or still scaffold stubs? |

**Flag specifically:**
- Shows with a folder but no flat YAML (not loadable by plugin)
- Shows with a flat YAML but no folder (no departmental docs)
- Showcases with populated department decisions vs. empty scaffolds
- Calibration/demo shows that are superseded and taking up space

**Output format:**

```
#### Show Inventory

| Show | Flat YAML | Folder | Stage | Notes |
|---|---|---|---|---|
| demo.archetype_sampler | ✅ | ✅ | R7 built, untested | |
| showcase.01 | ❌ | ✅ (Brief) | 0/9 passes complete | P1 (Set) not started |
| showcase.02 | ❌ | ✅ (Brief) | 0/10 passes complete | |
| showcase.03 | ❌ | ✅ (Brief) | 0/9 passes complete | |
| intro.young_persons_guide | ✅ | ❌ | Legacy flat | Voice rewrite pending |
...

**What's blocking showcase work:** [summarize the blocker for each show]
```

---

### Layer 3 — Cue Library Inventory

Cues live in `src/main/resources/cues/`. The production target is 30+ tagged production cues.

**Count and categorize by family:**

| Family | Count | Notes |
|---|---|---|
| atmos.* | N | |
| coda.* | N | |
| fx.* | N | |
| gracie.* | N | |
| still.* | N | |
| mood.* | N | |
| motif.* | N | |
| overture.* | N | |
| ramp.* | N | |
| world.* | N | |
| test.* | N | Don't count toward production target |
| **Total production** | **N** | |

**Flag:**
- Whether the 30+ production cue target is met
- Any families that are thin relative to their intended use
- Test cues (prefix `test.*`) — are they needed, or candidates for removal?
- Any cue files with naming that doesn't follow `[category].[archetype].[variant]` convention

---

### Layer 4 — KB Calibration Readiness

Department KBs live in `kb/departments/`. The skill `skills/kb-builder/kb-builder.skill.md` defines the calibration-ready checklist — use it as the scoring rubric.

**Calibration-ready checklist (per dept):**

| Check | Status |
|---|---|
| Instrument inventory complete | ✅ / ⚠️ / ❌ |
| Every instrument has status tag (✅ Verified / ⚠️ Gapped / 📋 Aspirational) | ✅ / ⚠️ / ❌ |
| Tone Translation section present | ✅ / ⚠️ / ❌ |
| At least one YAML example per instrument | ✅ / ⚠️ / ❌ |
| All known gaps in ops-inbox.md | ✅ / ⚠️ / ❌ |
| Used in at least one show session | ✅ / ⚠️ / ❌ |
| In folder structure (not flat file) | ✅ / ⚠️ |

**Migration status check:**
The skill requires every dept to have a folder at `kb/departments/[dept-slug]/[dept-slug].kb.md`. A flat `.kb.md` file at root level means the migration hasn't happened yet. Note: `show-director.kb.md` at root is a tombstone redirect (correct); others that are flat are unmigrated.

**Output format:**

```
#### KB Calibration Readiness

| Department | Calibration Ready | Migration | Primary Gap |
|---|---|---|---|
| Show Director | ✅ | ✅ folder | — |
| Stage Management | ⚠️ Partial | ❌ flat | No stage-management.show-creation-process.md yet |
| Casting | ✅ | ❌ flat | folder migration pending |
...

**N/12 departments calibration-ready**
**Phase 10 gate:** [what's still blocking the team from starting Showcase passes]
```

For a full depth read on specific KBs flagged as thin, defer to `skills/kb-builder/kb-builder.skill.md §Mode 2` in a follow-up session.

---

### Layer 5 — Structural Drift

Check whether the actual repo structure matches what `CLAUDE.md` and `README.md` describe.

- Folders or files that exist but aren't mentioned in either index?
- Folders or files described in `CLAUDE.md` that don't actually exist?
- Skills listed in `CLAUDE.md`'s skills table that don't match what's actually in `/skills/`?
- Any new show folders or KB folders that exist in the file tree but aren't cross-referenced in the right index?

Flag each mismatch.

---

### Layer 6 — Cross-Document Integrity

**Orphaned references:**
- File A references File B — does B exist at that path?
- KBs or skills referencing files that have moved or been renamed
- `production-team.md` KB paths table — do all paths resolve?
- Show department templates in `_template/` — do their references still hold?

**Redundant or duplicated content:**
- Same content maintained in both `production-team.md` and a dept KB — which is canonical?
- Flat `.kb.md` file AND folder `[dept]/[dept].kb.md` both present — which is active?
- Multiple docs describing the same process (e.g., show creation workflow across stage-manager.kb.md, CLAUDE.md §How to Work, and any planning docs)

**Long-term storage quality:**
- For the primary context files (`CLAUDE.md`, `ROADMAP.md`, `kb/production-team.md`, `kb/system/cue-show-yaml-schema.md`) — is each skimmable, well-organized, and sized appropriately?
- Is `CLAUDE.md`'s session state current, or is it still describing a past session's "where we stopped"?
- Is `kb/roadmap-planning-2026-03-26.md` still needed, or has its content been moved to ROADMAP.md?

---

## Findings Report Format

Save to `kb/audits/YYYY-MM-DD_[purpose]-audit.md` (e.g., `2026-03-26_pre-showcase-audit.md`). Also present in chat.

```markdown
---
date: YYYY-MM-DD
purpose: [brief label]
status: findings — awaiting confirmation
---

## Repo Audit — [Date]

### Overall Health
[2–4 sentences: what's solid, what's the main concern, severity of issues]

### Delta Since Last Audit
[What changed since the last audit — does it fit the repo's purpose?]
[Or: "First audit for this purpose."]

---

### Layer 1 — Session State
[session state table as defined above]

### Layer 2 — Show Inventory
[show inventory table + blockers]

### Layer 3 — Cue Library
[cue family table + production count vs. target]

### Layer 4 — KB Calibration Readiness
[dept table + N/12 ready + Phase 10 gate]

### Layer 5 — Structural Drift
[FILE → ISSUE → PROPOSED ACTION, or: "None found."]

### Layer 6 — Cross-Document Integrity
[Orphaned refs, duplicates, storage quality — FILE → ISSUE → PROPOSED ACTION]

---

### Summary: Proposed Changes

| # | File | Change | Effort |
|---|---|---|---|
| 1 | path/to/file | Description | Small / Medium / Large |

**Total: N proposed changes**

---

To proceed: say which changes you want made (by number), or "apply all."
To skip: say "skip [number]" or "skip all."

### Recommended Session Focus
[Based on session state + audit findings, what should this session do first?]
[Be specific — name the pass, the KB, or the file.]
```

---

## After the Report: Making Changes

When confirmed:

1. Add one todo item per change as `pending`, work through them one at a time
2. After each change: "Done — [brief description]"
3. For CLAUDE.md session state updates: write the new session state and show the diff before applying
4. For deletions: confirm the specific file before removing
5. After all changes: do a consistency pass — update `CLAUDE.md`, `ROADMAP.md`, and `production-team.md` KB paths if structure changed; verify cross-references still hold

### Compacting Guidelines

When condensing a verbose file:
- Preserve all decisions, constraints, and factual specifics
- Cut filler, redundant framing, and over-explained context
- Keep YAML front matter intact
- Note the change: "Reduced from ~N lines to ~N lines"

### Never Do Without Explicit Confirmation

- Delete a file entirely
- Remove a "Confirmed" or "Closed" decision entry from any KB
- Rename a show folder or cue file
- Change the `CLAUDE.md` architectural decisions table
- Modify another skill's `.skill.md` file
- Rewrite `ROADMAP.md` (propose the content, confirm before writing)

---

## Closing the Session

```
## Audit Complete — [Date]

Changes made: N
  ✓ [Each change]

Files updated: [list]

Open items deferred: [list titles]

Repo health: [one-line assessment]

Next session should: [specific action — "start showcase.01 P1 (Set)" etc.]
```

If no changes were needed: "Audit complete. [N] issues found, no changes needed without your direction — here's what I'd recommend tackling first."

---

## What This Skill Is Not For

- Filling in missing KB content or authoring show YAML (that's show work — use production-team.md and the relevant dept KBs)
- Running department KB uplifts in depth (that's `skills/kb-builder/kb-builder.skill.md §Mode 1`)
- Making judgment calls about whether KB content is creatively correct — only whether it's organized and referenced correctly
- Rewriting ROADMAP.md autonomously — always propose content and wait for approval
