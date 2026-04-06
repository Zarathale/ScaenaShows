---
status: in-progress
created: 2026-03-26
owner: Alan
purpose: Session context for roadmap restructuring conversation. Read this to resume where we left off.
---

# Roadmap Planning — 2026-03-26

This document captures the full context of a strategic roadmap discussion that was interrupted mid-session. Read it before continuing the conversation about restructuring ROADMAP.md.

---

## What Triggered This Discussion

Alan asked for:
1. A KB depth/config assessment of the full production team
2. A roadmap read — it is significantly out of date
3. A collaborative, intentional ROADMAP.md rewrite
4. A discussion of how to build each department to meaningful working capacity
5. Design of an iterative in-game review process with structured feedback

---

## KB Depth Assessment (as of 2026-03-26)

**By line count — strongest to weakest:**

| Department | Lines | Size | State |
|---|---|---|---|
| Choreography | 617 | 31KB | Strong — largest KB, well-structured |
| Sound | 590 | 29KB | Strong — Gracie persona well-defined |
| Casting | 585 | 28KB | Strong — mob register, capability gaps documented |
| Camera (Mark) | 571 | 25KB | Strong — named head, Focus Point Doctrine, 6 instruments |
| Show Director | 514 | 27KB | Solid — templates, intake process, department briefings |
| Stage Manager (Kendra) | 452 | 25KB | Solid — named head, prompt book, ops-inbox workflow |
| Fireworks (Mira) | 381 | 21KB | Decent — named head, 5 instruments, preset library |
| Wardrobe | 347 | 21KB | Decent — gaps documented, thin on creative examples |
| Voice | 334 | 21KB | Decent — showsprite.context.md adds significant depth |
| Set | 309 | 19KB | Thin — has structure, shallow on spatial design methodology |
| Lighting | 312 | 18KB | Thin — 3 instruments only; fireworks-as-light is notable |
| **Effects** | **260** | **13KB** | **Thinnest** — controls player body/physics but least developed |

**Named department heads:** Kendra (Stage Manager), Mark (Camera), Mira (Fireworks). All others are role labels only — Alan confirmed naming is optional/secondary.

**Cue library:** ~40 yml files in `src/main/resources/cues/`. ~37 production-intended (3 are test cues). Clears the 30+ target from Phase 8. Coverage is uneven across families.

**Shows:** Everything deployed is a demo or calibration. No production shows exist.

---

## Why ROADMAP.md Is Obsolete

The current ROADMAP.md (last updated 2026-03-24) describes Phases 7 and 8 as "active" — but:
- Phase 7's primary deliverable (showsprite.context.md persona doc) is complete ✅
- Phase 8's primary deliverable (30+ tagged cues) is complete ✅
- Neither phase mentions the production team, department KBs, or iterative show testing
- The roadmap was written before the dept KB framework existed

The show authoring workflow referenced in Phase 7 ("workflow documented") is NOT done — and this is actually a significant gap we need to close.

---

## Alan's Strategic Direction (Q&A Summary)

**Q: What does "meaningful working capacity" mean to you?**
A: This phrase is NOT from the roadmap — it predates that framework. We are defining it fresh.
Working definition to be confirmed: *A department knows their full instrument palette, has their gaps documented, and has contributed to at least one show that went in-game.*

**Q: How important is naming unnamed department heads?**
A: Naming is just shorthand/reference. Identity is not a QA concern. Secondary/optional.

**Q: Calibration approach?**
A: Start with a purely **functional** (not creatively inspired) full-team show. Every department includes 2-3 prioritized things they want to calibrate/test/try. Not exhaustive — a prioritized first round.

**Q: Feedback format?**
A: Alan watches in-game multiple times and generates a verbal/written report with prioritized feedback. The team reviews that feedback, divides it by department ("I fix that; those departments fix that"), and iterates again.

**Q: Show creation workflow?**
A: Needs to be explicitly hammered out and iterated on. Stage Management writes the YAML — but the full process (concept → brief → dept decisions → YAML → review) isn't formally defined yet. Getting a few iterations in will help Stage Management and each department get better at making/receiving briefs and requirements.

**Q: Effects KB being thinnest?**
A: Confirmed — keep Effects on the list of KBs needing improvement. That's an output of this planning conversation.

**Q: What's the first production show?**
A: A collective reimagination of **"Young People's Guide to ScaenaCraft Shows"** — inspired by Bernstein's Young People's Concerts. A series of vignettes where the player is tp'd to a different set for each movement. Different look/feel/vibe per section. Shows off the beauty of the Minecraft builds and demonstrates mastery of the craft. High bar. We're not ready for it yet — that's the point. The calibration work comes first.

---

## Proposed New Phase Structure

### Phase 9 — Department Readiness + Workflow Definition
Two parallel tracks:

**Track A — KB Uplift:**
Bring every dept KB to a common threshold of "meaningful working capacity." Priority gaps:
- Effects (most urgent — thinnest, highest player-body impact)
- Set (spatial design methodology underdeveloped)
- Lighting (only 3 instruments documented)
- Wardrobe (thin on creative/contextual examples)

**Track B — Show Creation Workflow:**
Formally define the end-to-end show creation process. Who does what, in what order, documented as a Kendra-owned process doc. Needs to be iterated in practice — can't just be written in theory. Target home: new section in `kb/departments/stage-manager.kb.md` or a standalone `kb/stage-management.show-creation-process.md`.

**Exit criteria:** Every dept KB meets the working capacity definition. Show creation workflow is documented and has been exercised at least once.

---

### Phase 10 — Team Calibration Series
A series of functional (not artistically inspired) full-team shows. Purpose: departments learning their knobs in context, with real human feedback driving iteration.

**Round structure:**
1. Each department submits 2-3 calibration priorities (what they want to test this round)
2. Show Director writes a minimal functional brief
3. All departments make their decisions; Stage Management writes the YAML
4. Alan watches in-game multiple times; generates structured verbal/written feedback
5. Team divides feedback by department; each dept owns their fixes
6. Iterate

**Feedback format:** To be agreed before Round 1 — the format shapes what feedback is useful. Proposal held for conversation.

**Open question A (unresolved):** Is Phase 10 a single evolving show revised across rounds, or a fresh show each round? (Instinct: single evolving show is better for calibration — you can track what changed; fresh show each round forces creative flexibility. Alan's call.)

**Exit criteria:** Each department has calibrated their primary instruments; the team has completed the full loop (brief → YAML → in-game → feedback → revision) at least 3 times.

---

### Phase 11 — Young People's Guide to ScaenaCraft Shows (Production)
The gold standard. Bernstein-inspired vignettes. Player tp'd between distinct sets for different movements. Each movement has its own look, feel, vibe. Showcases the builds and demonstrates mastery of craft. High bar — this is where the team needs to arrive, not where they are now.

This phase begins only after Phase 10 establishes calibrated dept confidence.

---

## Two Open Questions Before Writing ROADMAP.md

**Open Question A:** Is the Phase 10 calibration series a single evolving show (revised round-to-round) or a fresh show each round?

**Open Question B:** Does the show creation workflow definition live inside `stage-manager.kb.md` (as Kendra's process) or as a standalone doc (e.g., `kb/stage-management.show-creation-process.md`)? Alan's instinct needed.

---

## How to Resume This Conversation

1. Read this file
2. Read CLAUDE.md session state
3. Tell Alan: "I've restored context from the planning doc. We have two open questions before I can write the new ROADMAP.md — here they are: [A] and [B]. Once you answer those, I'll draft the full roadmap for your review."
4. After Alan answers → draft ROADMAP.md → get Alan approval → commit

---

## Files to Update After Roadmap Is Written

- `ROADMAP.md` — full structural rewrite (Phases 7/8 closed, Phases 9/10/11 defined)
- `CLAUDE.md` — update "Current Phase" table and "Session State"
- `kb/departments/effects.kb.md` — flag for uplift (Phase 9, Track A)
- `kb/departments/set.kb.md` — flag for uplift
- `kb/departments/lighting.kb.md` — flag for uplift
- `kb/departments/wardrobe.kb.md` — flag for uplift
- `kb/departments/stage-manager.kb.md` — add show creation workflow section (Phase 9, Track B)
