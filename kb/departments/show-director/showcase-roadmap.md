---
document: Showcase Series Production Roadmap
scope: showcase.01 / showcase.02 / showcase.03
updated: 2026-03-26
status: active — governs all three Phase 10 shows
---

# Showcase Series — Production Roadmap

> This document is the authoritative sequencing guide for the Showcase Series (Phase 10).
> It defines each show's department pass order and the session agenda for each pass.
> Every department brief for showcase.01–03 cross-references this document.
> All departments align their decisions and authoring to the pass order defined here.
>
> **A "pass" is a working session between Alan and Claude** focused on a specific department
> or department cluster. Each pass has defined inputs (what it receives from prior passes)
> and defined outputs (what it produces for subsequent passes). No department authors YAML
> until their pass is complete and their inputs are confirmed.
>
> Sequencing methodology: `kb/departments/show-director/show-director.kb.md §Department Sequencing as Design Work`

---

## How to Read This Document

Each show has its own pass sequence table. Columns:

| Column | Meaning |
|---|---|
| **Pass** | Sequential working session number within this show |
| **Session type** | Solo (one dept), Joint (two or more depts in same session), or Director Review |
| **Department(s)** | Who is in focus |
| **Receives from prior passes** | What confirmed information this pass builds on |
| **Produces** | The locked decisions and outputs this pass delivers |
| **Gates** | Which subsequent pass(es) cannot proceed until this one is complete |

The pass sequence is the correct intake order. Do not skip passes or run them out of order.
If a later pass produces information that changes an earlier pass's decision, bring it to
the Director — do not revise silently.

---

## showcase.01 "The Cabinet"

*Rondo (ABACADAEAF) — 6 locations — Elevated: Set, Casting, Sound (Vignette F), Fireworks (Vignette E)*

| Pass | Session type | Department(s) | Receives from prior passes | Produces | Gates |
|---|---|---|---|---|---|
| **P1** | Solo | **Set** | Brief, show-direction, tone | All 6 location coordinates + facing; ceiling heights for D & E; contraption trigger XYZ; stage registry entries; sky clearance for E | All subsequent passes |
| **P2** | Joint | **Effects + Fireworks + Camera** | P1: Vignette E coordinates + sky clearance | Altitude target for Vignette E (locked); Mira's Vignette E y_offset range; Camera's Vignette E philosophy (looks into sky / downward / holds facing) | P4 (Lighting—biome E); P7 (Voice—Vignette E line) |
| **P3** | Solo | **Casting** | P1: all 6 locations + biomes; P2: Vignette E aerial presence decision | Allay management (persist/respawn); Home Base companion list; Vignette C creature pair; Vignette D Strider details; Vignette E aerial presence (yes/no + species); Vignette F punchline creature (after Set confirms contraption) | P5 (Choreography); P6 (Wardrobe); P8 (Sound—comedy hit) |
| **P4** | Parallel | **Lighting + Sound** | P1: all 6 biomes + locations; P3: Casting's Vignette C pair + Vignette F creature | Lighting: TIME_OF_DAY per vignette, WEATHER baseline, A-section reset cadence. Sound: ambient beds per biome; Vignette F pre-reveal texture. Sound's Vignette F comedy hit tick is PENDING until P8. | P9 (Stage Management—run sheet) |
| **P5** | Solo | **Choreography** | P1: all 6 coordinates; P3: all performer decisions | Initial placement for Allay + companions at Home Base; Vignette C initial formation; Vignette D Strider spawn; Vignette E aerial staging (if applicable) | P9 (Stage Management—run sheet) |
| **P6** | Solo | **Wardrobe** | P3: Home Base companion list | Collection visual language decision (Option A/B/C); ENTITY_GLOW decisions if applicable | P9 (Stage Management—run sheet) |
| **P7** | Solo | **Voice** | P1–P6: all confirmed location, performer, and atmosphere decisions | All A-section MESSAGE lines; Vignette B–E Sprite observations; BOSSBAR color + structure. Vignette F after-line is PENDING until P8. | P8 (Sound + Voice joint—final Vignette F) |
| **P8** | Joint | **Sound + Voice** | P3: Vignette F punchline creature confirmed; P1: contraption XYZ confirmed | Vignette F comedy hit sound identified + tick locked; Vignette F Sprite after-line written + Director-reviewed | P9 (Stage Management—run sheet) |
| **P9** | Solo | **Stage Management** | All prior passes complete | Full run sheet; REDSTONE ↔ SOUND tick alignment confirmed; entity cleanup protocol per vignette; nether world name confirmed | YAML authoring begins |

**showcase.01 sequencing notes:**
- P2 is the only joint pass before P8. Effects, Fireworks, and Camera must all three be in the same session — the altitude, y_offset range, and Vignette E philosophy are mutually constraining.
- P3 (Casting) has one open item that cannot be closed at P3: the Vignette F punchline creature. It depends on the contraption design (P1) and the contraption is confirmed at P1, so this should be closable at P3 if Set has confirmed the contraption specifics. Flag if not.
- P4 (Lighting + Sound) run in parallel but are independent sessions. They do not need to be joint.
- P8 is the show's precision coordination pass. The comedy hit in Vignette F requires Sound and Voice to be in the same session to lock the tick and the after-line together.

---

## showcase.02 "The Long Night"

*Through-composed (ABCDEA') — Single location — Elevated: Lighting (show lead), Sound (co-lead)*

| Pass | Session type | Department(s) | Receives from prior passes | Produces | Gates |
|---|---|---|---|---|---|
| **P1** | Solo | **Set** | Brief, show-direction, tone | Night Location coordinates + facing; sky clearance confirmation; biome; visual layer documentation; LIGHTNING candidate XYZ options (1–2 candidates) | All subsequent passes |
| **P2** | Director Review | **Lighting → Director** | P1: location + biome + LIGHTNING XYZ candidates | Full arc plan presented to Director: TIME_OF_DAY progression per section; WEATHER decision (rain vs. cloud for C); LIGHTNING XYZ chosen; aftermath WEATHER; dawn increment plan | P3 (Sound); P4 (Effects, Camera) |
| **P3** | Joint | **Lighting + Sound** | P2: Lighting's full arc confirmed by Director | Thunder offset agreed (exact tick delta, LIGHTNING flash → thunder); Sound's Section C design (rain vs. cloud response); storm co-design finalized | P7 (Voice—both lines); P8 (Stage Management—LIGHTNING tick table) |
| **P4** | Parallel | **Effects + Camera** | P2: Lighting's full arc; P3: LIGHTNING tick approximate | Effects: Section D disorientation decision (Director-reviewed); Section E aftermath echo decision. Camera: storm philosophy (looking into it vs. found by it); A' dawn facing decision | P8 (Stage Management—LIGHTNING tick table) |
| **P5** | Solo | **Casting** | P1: biome + location; P2: TIME_OF_DAY/WEATHER at night confirmed | Nocturnal creature selection (Bats + Wolf or variant); creature count + altitude; Section C thinning recommendation; Section E aftermath creature (yes/no) | P6 (Choreography) |
| **P6** | Solo | **Choreography** | P1: night location coordinates; P5: creature list | Bat spawn altitude + cluster XYZ; Wolf spawn position (midground); Section A' dawn creature position (if applicable) | P8 (Stage Management—run sheet) |
| **P7** | Solo + Director Review | **Voice** | P2: full arc; P3: storm co-design; P4: Camera philosophy | Both Sprite lines drafted (storm line + dawn line); Director reviews and approves both before YAML | P8 (Stage Management—run sheet) |
| **P8** | Solo | **Fireworks (Mira)** | P2–P7 all complete; full arc known | Dawn firework: yes or no; if yes, preset + y_offset + tick after Voice dawn line | P9 (Stage Management—run sheet) |
| **P9** | Solo | **Wardrobe** | P5: creature list | Wolf glow assessment (probably no action); document and close | P10 (Stage Management—run sheet) |
| **P10** | Solo | **Stage Management** | All prior passes; P3 + P4 outputs for LIGHTNING tick | LIGHTNING tick table documented + reviewed by Lighting, Sound, Effects, Camera; run sheet written | YAML authoring begins |

**showcase.02 sequencing notes:**
- P2 is unusual: Lighting presents solo to the Director before any other department briefs. This is the show's defining sequencing choice — the arc is the infrastructure, so it must exist before anyone can adapt to it.
- P3 (Lighting + Sound joint) is the show's creative partnership pass. The thunder offset agreement must come out of this session; neither department authors around the LIGHTNING until both have agreed.
- P4 (Effects + Camera) are independent decisions but both depend on the arc from P2. They can be sequential or parallel sessions.
- P7 (Voice) is the show's most context-dependent pass — Voice reads the full arc (P2) and the storm co-design (P3) before drafting either line. Both lines are Director-reviewed before the pass closes.
- P10 includes the formal LIGHTNING tick table review — all four peak departments (Lighting, Sound, Effects, Camera) sign off on the exact tick sequence before any of them author Arrival YAML.

---

## showcase.03 "Welcome"

*Build → Arrival → Celebration → Coda — Permanent stage — Elevated: Effects + Fireworks + Camera (convergence leads), Voice (narrative lead)*

| Pass | Session type | Department(s) | Receives from prior passes | Produces | Gates |
|---|---|---|---|---|---|
| **P1** | Solo | **Set** | Brief, show-direction, tone | Welcome Stage confirmed: approach start coordinates, threshold marker + coordinates, welcome area center coordinates, proposed arrival yaw/pitch, sky clearance; approach corridor character documented; stage registry entry complete | P2 (convergence session) |
| **P2** | Joint (3-way) | **Effects + Fireworks + Camera** | P1: welcome area coordinates + sky clearance + proposed yaw/pitch | **Altitude at Arrival locked** (Effects proposes, Mira accepts/counters — one number); Mira's y_offset range confirmed against sky clearance; Camera Arrival facing locked (yaw/pitch confirmed or adjusted from P1); threshold sensation type decided (Effects) | P3 (Casting); P4 (Choreography); P5 (Wardrobe); P6 (Lighting+Fireworks); P7 (Sound); P8 (Voice—Arrival line) |
| **P3** | Solo | **Casting** | P1: welcome stage character + environment; P2: altitude at Arrival | Committee composition (3–5 species); AI state during Build (puppet/performer/hybrid); Coda presence decision flagged (resolved jointly with Choreography at P4) | P4 (Choreography); P5 (Wardrobe) |
| **P4** | Joint | **Choreography + Casting** | P1: welcome area geometry; P2: altitude; P3: committee species list | Committee staging positions (each entity's XYZ); Celebration movement approach (AI release vs. MOVE_ENTITY); Coda decision locked (stay / gradual withdrawal / hold-then-release) | P5 (Wardrobe—if glow needs position info); P9 (Stage Management—pre-staging method) |
| **P5** | Solo | **Wardrobe** | P3: committee species list; P4: staging positions | Visual language decision (no intervention / shared glow / species coherence sufficient); ENTITY_GLOW color + timing if applicable | P9 (Stage Management—run sheet) |
| **P6** | Joint | **Lighting + Fireworks** | P1: welcome stage environment; P2: altitude at Arrival; P2: Mira's preset list | Celebration TIME_OF_DAY agreed (Lighting proposes, Mira confirms fireworks work in that light); Build TIME_OF_DAY; Arrival light shift; Coda TIME_OF_DAY; Lighting's full four-section arc finalized | P7 (Sound—Celebration + Coda transition); P9 (Stage Management—run sheet) |
| **P7** | Solo + Director Review | **Sound** | P2: Arrival tick approximate; P6: Lighting arc + Celebration light; Mira's final firework tick (approximate) | Build soundscape layers (named + introduction ticks); Arrival sound identified (Director-reviewed); Celebration layers; Coda transition timing locked | P8 (Voice—coordinating Coda silence before Voice line); P9 (Stage Management—run sheet) |
| **P8** | Solo + Director Review | **Voice** | P1: welcome area character; P2: threshold sensation type; P3: committee composition; P7: Coda silence duration | Build line (yes/no + draft if yes); Arrival line — five candidates → Director selects → locked; Coda line — five candidates → Director selects → locked; BOSSBAR structure decided | P9 (Stage Management—Arrival tick map) |
| **P9** | Solo | **Stage Management** | All prior passes complete | Pre-show staging method confirmed (pre-T=0 or workaround); Arrival tick map drafted + reviewed by Effects, Fireworks, Camera, Lighting, Sound, Voice; run sheet written | YAML authoring begins |

**showcase.03 sequencing notes:**
- P2 is the most demanding session in the Showcase Series: three departments (Effects, Fireworks, Camera) with mutually constraining decisions must reach agreement in one joint session. The Director facilitates. Nothing moves until this session closes with three locked decisions: altitude, facing, threshold sensation.
- P4 (Choreography + Casting) is joint because the Coda decision requires both departments to be in the same conversation. Staging positions also require Casting's confirmed species list, so the joint session is more efficient than sequential handoffs.
- P6 (Lighting + Fireworks) is the second convergence point: Celebration TIME_OF_DAY and firework visibility are coupled. Mira and Lighting must agree before either authors Celebration YAML.
- P8 (Voice) is always the second-to-last pass. Both Arrival and Coda lines require all prior context — Set's welcome area, Casting's committee, Effects' threshold sensation — before they can be written with specificity. Director reviews both lines before the pass closes.
- P9's Arrival tick map review is a formal step: all six Arrival departments (Effects, Fireworks, Camera, Lighting, Sound, Voice) confirm their events are within ±5 ticks of the Arrival tick before any of them author.

---

## Department Engagement Index

Where each department has a pass in each show. Use this to understand your full engagement
across the Showcase Series and to cross-reference other shows' relevant decisions.

| Department | showcase.01 | showcase.02 | showcase.03 |
|---|---|---|---|
| **Set** | P1 (first mover) | P1 (first mover) | P1 (first mover) |
| **Effects** | P2 (joint — altitude E) | P4 (Section D disorientation) | P2 (joint — altitude contract) |
| **Fireworks** | P2 (joint — altitude E) | P8 (dawn rocket yes/no) | P2 + P6 (joint — altitude + Celebration light) |
| **Camera** | P2 (joint — Vignette E philosophy) | P4 (storm philosophy) | P2 (joint — Arrival facing) |
| **Casting** | P3 | P5 | P3 |
| **Lighting** | P4 (parallel) | P2 solo → P3 joint (show lead) | P6 (joint with Fireworks) |
| **Sound** | P4 (parallel) + P8 (joint) | P3 (joint — storm co-design) | P7 |
| **Choreography** | P5 | P6 | P4 (joint with Casting) |
| **Wardrobe** | P6 | P9 (minimal) | P5 |
| **Voice** | P7 + P8 (joint) | P7 (Director review) | P8 (Director review) |
| **Stage Management** | P9 | P10 | P9 |

**Patterns to note:**

- **Set is always P1.** Infrastructure before creativity, in all three shows. The one exception
  to this pattern (showcase.02) is that Lighting follows Set immediately at P2, before any
  other department — because the arc is the infrastructure in that show.

- **Voice is always second-to-last.** Both lines and all narration require the most context.
  Never brief Voice before the show's physical and casting reality is confirmed.

- **Stage Management is always last.** It writes the run sheet from completed passes, not
  during them. If Stage Management is writing the run sheet before all departments have closed
  their passes, something has been skipped.

- **The joint sessions are the creative core.** In showcase.01: P2 (altitude E) and P8
  (comedy hit). In showcase.02: P3 (storm peak co-design). In showcase.03: P2 (convergence
  contract) and P6 (Celebration light). These are where the show's central creative tensions
  are resolved in real time — they cannot be asynchronous.

- **Fireworks moves early when altitude matters, late when it doesn't.** showcase.01 P2
  (altitude contract = first creative pass); showcase.03 P2 (same reason). showcase.02 P8
  (Mira is nearly absent; dawn rocket is the last optional decision).

---

## Session Agenda Template

Each pass is a working session. To open a pass session, the Director states:

```
Opening [Show ID] Pass [N]: [Department(s)]

Inputs confirmed from prior passes:
- [list what was confirmed]

This pass needs to produce:
- [list the locked decisions]

Gates it removes:
- [list what can move after this pass closes]

Open questions for this session:
- [department-specific intake questions from the dept brief]
```

When the pass closes, decisions are recorded in the relevant `departments/[dept].md` file
under the `## Decisions` section. The Director notes the pass as complete in `direction/intake.md`.

---

## Status Tracking

*Updated as passes complete. Mark each pass ✅ when closed.*

### showcase.01
- [ ] P1 — Set
- [ ] P2 — Effects + Fireworks + Camera
- [ ] P3 — Casting
- [ ] P4 — Lighting + Sound
- [ ] P5 — Choreography
- [ ] P6 — Wardrobe
- [ ] P7 — Voice
- [ ] P8 — Sound + Voice (Vignette F final)
- [ ] P9 — Stage Management → YAML authoring begins

### showcase.02
- [ ] P1 — Set
- [ ] P2 — Lighting → Director review
- [ ] P3 — Lighting + Sound
- [ ] P4 — Effects + Camera
- [ ] P5 — Casting
- [ ] P6 — Choreography
- [ ] P7 — Voice → Director review
- [ ] P8 — Fireworks
- [ ] P9 — Wardrobe
- [ ] P10 — Stage Management → YAML authoring begins

### showcase.03
- [ ] P1 — Set
- [ ] P2 — Effects + Fireworks + Camera (3-way convergence)
- [ ] P3 — Casting
- [ ] P4 — Choreography + Casting
- [ ] P5 — Wardrobe
- [ ] P6 — Lighting + Fireworks
- [ ] P7 — Sound → Director review (Arrival sound)
- [ ] P8 — Voice → Director review (both lines)
- [ ] P9 — Stage Management → YAML authoring begins
