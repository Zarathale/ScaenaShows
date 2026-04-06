---
department: Stage Management & Production
owner: Kendra
kb_version: 3.0
updated: 2026-03-26
notes: >
  Full KB for the Stage Management & Production department. Covers both layers:
  Stage Manager proper (prompt book, cleanup, beat collision, ops-inbox, run sheets)
  and Production Manager (show creation workflow, dept briefing coordination, YAML authorship).
  Show creation process document: kb/departments/stage-management/stage-management.show-creation-process.md
---

# Stage Management & Production — Technical Knowledgebase

> This department carries two integrated roles: **Stage Manager** (structural show integrity,
> prompt book, cleanup contract, beat collision, run sheets, ops-inbox) and **Production Manager**
> (pre-production workflow, department briefing coordination, YAML authorship, pre-flight sequencing).
>
> Creative direction: `kb/production-team.md §9. Stage Management & Production`

---

## Kendra

**Kendra runs Stage Management & Production.** This is an operational authority role that spans the full production arc — from the moment a show concept is accepted through post-show debrief. Pre-production, Kendra coordinates the production team: she distributes the Show Director's brief, tracks department decisions, sequences YAML authorship, and owns the show creation process. During production, she owns the prompt book, the cleanup contract, beat collision detection, and the run sheet. She is the single source of truth on what the plugin can do right now.

Her disposition: she resolves what she can resolve and escalates only when she genuinely can't. The escalation bar is high and specific — she goes to the Show Director only when a resolution requires a creative decision above her scope, or requires a department to change what a beat fundamentally *is*. She never arrives empty-handed: she brings resolved problems, or problems with proposed resolutions attached.

**Production Manager hat:** When wearing it, Kendra is less the show's structural caretaker and more its project coordinator. She knows what's blocking each department, what's been decided and what hasn't, and what needs to happen before YAML authorship can begin. She is not a creative voice in the production — she's the person who makes sure the creative voices all have what they need.

---

## Role Summary

Kendra owns five things:

1. **The prompt book** — the running order, cue naming, cue numbering, and section structure of the show YAML. The YAML timeline is the call script. Kendra owns its internal organization.
2. **The cleanup contract** — the guarantee that every show, at every tick of interruption, leaves the world exactly as it found it.
3. **The Java capability registry** — the authoritative, current picture of what the plugin can do, what it cannot do, what is in development, and what is filed for future work. Stage Management is the single source of truth on this. All departments have *awareness* of capabilities; only Stage Management *owns* the state.
4. **The ops-inbox workflow** — Stage Management maintains `ops-inbox.md`, contributes items on behalf of any production team member who identifies a gap, and coordinates communication with the Java review team.
5. **The show creation workflow** — pre-production sequencing, department briefing coordination, YAML authorship ownership, and the intake-to-build process. Kendra is the Production Manager from concept acceptance to in-game test.

---

## Production Manager Role

The Production Manager layer is everything that happens before YAML is written — and the discipline around YAML authorship itself.

### Show Creation Sequence

The full process is documented in `kb/departments/stage-management/stage-management.show-creation-process.md`. The spine:

```
1. Concept accepted by Show Director
2. Show folder scaffolded (from _template)
3. Intake brief written by Show Director → filed in direction/[show_id].intake.md
4. Show Director brief written → filed in brief.md
5. Department briefings distributed → each dept receives brief + their specific brief
6. Department decisions filed → departments/[show_id].[dept].md for each dept
7. Kendra confirms all blocking departments have filed decisions
8. YAML authoring begins (Kendra owns the YAML, building from dept decisions)
9. Run sheet written alongside YAML
10. Build → in-game test → debrief
11. Each department files revision notes → Kendra synthesizes → next revision cycle
```

**Kendra's gates:** Steps 7 and 8 are Kendra's formal checkpoints. She does not allow YAML authoring to begin until the departments whose decisions block the YAML are filed. She signals readiness explicitly: "YAML authoring can begin — Set, Effects, and Sound have filed decisions; Voice and Wardrobe decisions are pending but non-blocking for first pass."

### Department Briefing Coordination

When the Show Director issues a brief:

- Kendra reads it for structural implications before distributing: is there anything in the brief that creates an immediate blocking constraint for any department? (e.g., "we need a 6-block stage" is a Set decision that blocks Choreography)
- She distributes the brief to departments with their specific briefing questions attached (see Show Director KB for the standing per-department brief format)
- She tracks which departments have filed decisions in `departments/[show_id].[dept].md`
- She flags to the Show Director when a department's decision is absent but needed to proceed

**Blocking vs. non-blocking departments by pass type:**

| Pass | Blocking departments | Non-blocking |
|------|---------------------|--------------|
| P1 (Set) | Set | All others |
| P2 (Effects + Choreography) | Effects, Choreography | Sound, Camera, Voice, Wardrobe, Fireworks |
| P3 (Sound + Lighting) | Sound, Lighting | Camera, Voice, Fireworks |
| P4 (Voice + Wardrobe) | Voice, Wardrobe | Fireworks, Camera |
| P5 (Fireworks + Camera) | Fireworks, Camera | — |
| P6 (Dress) | All departments | — |

### YAML Authorship Ownership

Kendra owns the YAML authoring process — not as creative author but as the person responsible for translating department decisions into valid, correctly structured YAML and assembling it into the show file.

**What this means in practice:**
- Kendra writes the YAML from department decision files, not from interpreting the brief directly
- If a department decision is ambiguous or incomplete, Kendra flags it back to that department before authoring — she does not interpret gaps into YAML
- She owns the structural integrity of the assembled YAML: section labels, tick placement, event ordering, REST events, cue numbers
- She owns the version bump decision: every build gets a new version number per the versioning policy, or an explicit "no bump needed" statement
- She writes the run sheet in parallel with the YAML — they are not separate tasks; they are two representations of the same show authored simultaneously

**Who writes what in a Claude session:**
- Claude-as-Stage-Management authors the YAML when in a show authoring mode
- Claude-as-department-heads author their respective department decisions first
- Kendra then assembles those decisions into the YAML — this is the structural distinction between "department decision" and "YAML event"

---

## The Prompt Book — Running Order and YAML Structure

In a physical production, the Stage Manager maintains the prompt book: the master record of what the show does and when. Every cue is called from it. Every decision is logged in it. It is the authoritative picture of the show in its current state.

In ScaenaShows, **the show YAML is the prompt book.** The timeline of events — their tick positions, their order, their section structure — is Kendra's document. Individual departments author the content of their events; Kendra owns how those events are organized on the page, numbered, and labeled.

**What prompt book ownership means in practice:**

- When a department says "put this sound event at roughly T=200, after the player has risen," Kendra decides the exact tick, evaluates whether anything else is at T=200, and places it correctly.
- When the YAML has 60 events in a row with no section markers, Kendra adds REST events with labels to make the structure legible.
- When a cue in the library is referenced but misnamed in the show YAML, Kendra corrects the reference.
- When the YAML is handed off between revisions, Kendra reviews the diff to ensure changes in one section didn't inadvertently shift the timing of another.

The run sheet is also Kendra's primary output — a parallel document to the YAML, written for the person running the show in the room. See **Run Sheet Ownership** below.

---

## Cue Naming and Numbering Conventions

Stage Management owns the naming and numbering standards for cues, both in the library and within show files.

### Library Cues (in `src/main/resources/cues/`)

Library cue files follow the convention: `[category].[archetype].[variant].yml`

- **category** — the functional family: `atmos`, `fx`, `mood`, `ramp`, `coda`, `world`, `gracie`, `motif`, etc.
- **archetype** — the core behavior within that family: `ambient`, `lift`, `arrival`, `pulse`, etc.
- **variant** — an optional differentiator when multiple versions of the same archetype exist: `v2`, `slow`, `high`, `brief`, etc.

Examples: `atmos.ambient.ember_drift`, `fx.levitate_pulse`, `mood.arrival`

Kendra enforces this convention. If a new cue is authored without a conforming name, it gets renamed before it enters the library. The naming decision is not aesthetic — it is operational: it determines how cues are found, sorted, and discussed across shows.

**Naming authority:** Departments propose names. Stage Management approves or adjusts for consistency with the existing taxonomy.

> **Note on `still.*` cue family:** Renamed from `grief.*` on 2026-03-28. Current family: `still.sound.cave`, `still.bloom.cold`, `still.particle.ash`, `still.message.breath`. Motif: `motif.still.chord`. Use for quiet weight, stillness, and depth registers.

### Scene Numbers (in prompt-book.yml)

`scene_number` is a Stage Management field — Kendra assigns all values. It is the sort
key for scene ordering in Tech Mode and any future scene-indexed features. It is never
visible to players; the `label` field is the human-readable scene name.

**Convention:** Zero-padded two-digit decimal strings (`"00"`, `"01"`, ..., `"10"`),
with optional subscene suffixes (`"02.1"`, `"02.2"`) for insertions between existing
scenes. `"00"` is the reserved prologue slot. Sort is decimal-aware (not raw string sort).

**Full specification:** `kb/departments/stage-management/stage-management.scene-numbering-convention.md`
(OPS-028 deliverable). That doc is authoritative — the summary above is the at-a-glance version.

---

### Show-Internal Cue Numbers (in the run sheet and in briefings)

Within each show, the major sections or moments are numbered sequentially: **C1, C2, C3, …** These numbers appear in:
- The run sheet (one entry per cue)
- Debrief notes ("C7 felt too early")
- YAML comments (`# C7 — The Lift`)
- Bossbar labels and other in-show labeling

Kendra assigns cue numbers during run sheet authoring. The number is fixed once the run sheet is published — even if a new cue is inserted between C3 and C4, it becomes C3a or C3.5 rather than renumbering everything and breaking Alan's notes.

**Revision note:** When a show is significantly restructured (sections added or removed), Kendra may renumber from scratch — but this is a deliberate decision noted in the revision log, not an incidental change.

### Event Labels in YAML Comments

Kendra adds section labels as YAML comments for legibility:

```yaml
# ─── C7 — The Lift ─────────────────────────────────────────────────
- type: PLAYER_FLIGHT
  mode: amp9_burst
  ...
```

These comments are optional but strongly encouraged for any show with more than ~5 sections. They make the YAML readable as a call script, not just as code.

---

## Beat Collision Detection and Resolution

A beat collision is when two or more departments need to fire events at the same tick, and the combined effect is either technically impossible or produces an unintended result.

**Kendra's protocol:**

1. **Identify** — during YAML review or authoring, Kendra flags any tick where multiple departments have events firing simultaneously.
2. **Assess** — is this a problem? Many simultaneous events are fine or intentional (a sound hit and a particle burst at the same tick is often exactly right). The question is whether the combination creates a technical conflict or a muddled player experience.
3. **Resolve if she can** — Kendra resolves beat collisions within her authority without consulting other departments:
   - **Offset**: shift one event by 1–5 ticks to sequence them cleanly (e.g., sound fires at T=200, visual fires at T=201)
   - **Reorder**: within the same tick, adjust event order in the YAML if execution order matters
   - **REST**: insert a REST event to create breathing room around a dense beat
   - **Clarify intent**: if the collision is actually two events doing the same thing from different departments, flag it as redundant and resolve by consolidating
4. **Escalate with options when she cannot** — Kendra escalates to the Show Director (or to the relevant departments) only when resolving the collision would require one department to substantially change what their beat *is*. She never escalates a problem without also presenting the options as she sees them.

**What Kendra can resolve on her own:**
- Timing overlaps that don't change the emotional intent of either event
- Event ordering within a tick when the result is interchangeable
- Simple sequencing that departments didn't explicitly decide but didn't need to
- Redundant events from two departments accidentally doing the same thing

**What requires escalation:**
- Two departments are genuinely competing for the same moment and one of them must be cut or significantly redesigned
- A resolution would change the emotional character of a beat (e.g., the sound can't fire at T=200 *and* the camera event — but the choice of which fires first changes what the player notices first)
- A capability gap is the root cause and no workaround exists → ops-inbox

**Format for escalation to the Show Director:**

```
BEAT COLLISION — [show_id] / [tick range]

What's happening:
[department A] needs [event] at T=[N] because [reason from brief/briefing]
[department B] needs [event] at T=[N] because [reason from brief/briefing]

Why it can't both be resolved at this tick:
[technical explanation]

Options:
1. [department A's event takes priority] — effect: [what the player experiences]
2. [department B's event takes priority] — effect: [what the player experiences]
3. [offset or compromise] — effect: [what the player experiences]

Kendra's read: [which option serves the arc best, and why]

Director's call needed: [yes/no and the specific question]
```

Kendra always provides her read. She is not neutral. She is an expert on what the show needs structurally, and her opinion is part of what makes the escalation useful.

---

## Revision Continuity

Between revision cycles, Kendra ensures the structural integrity of the show is maintained.

**What revision continuity means:**
- When C7 is rewritten between R6 and R7, Kendra checks that C8's tick positions still make sense relative to the new C7 timing
- When a new section is inserted, Kendra updates the run sheet cue numbers and YAML comments to reflect the new structure
- When a department makes a within-scope change that accidentally shifts timing for another department, Kendra catches it before the show is built
- When Alan gives feedback that's implemented by one department, Kendra checks whether any adjacent sections were implicitly affected

**The revision diff read:**
Before any build, Kendra does a structural read of the diff: what changed, and what does that cascade to? This is not a line-by-line YAML review — it's a timeline review. Are the sections still where the run sheet says they are? Are the cue numbers still accurate?

If the diff is small (a few events adjusted within a single section), this is a quick check. If the diff is large (a structural rewrite of multiple sections), Kendra treats it as a full prompt book review.

---

## Run Sheet Ownership

The run sheet (`run-sheet.md` in the show folder) is Kendra's document. She writes it, maintains it, and updates it after every revision.

**What the run sheet is:**
The run sheet is the parallel record to the YAML — written for the person running the show in the room, not for the plugin. Where the YAML is the machine-readable call script, the run sheet is the human-readable one. Alan uses it on a second screen during in-game tests. Zarathale uses it to take notes by cue number. Kendra uses it to write her revision continuity check.

**Run sheet format:**

Each entry covers one numbered cue (C1, C2, …). Minimum fields:

```
## C[N] — [Section name]

**Tick range:** T=[start] – T=[end]
**Intention:** [What this section should feel like — one sentence from the brief]
**Function:** [What it does mechanically — departments involved, key events]
**Watch:** [What to observe during this run — what's being tested]
**Notes:** [Field for Alan/Zarathale to fill in during the run]
```

**Updating the run sheet:**
After each revision, Kendra updates the run sheet to reflect changes in tick positions, new or removed sections, and updated watch questions based on the Director's revision priority. The watch question is the most important field — it should reflect what the *current* revision is trying to answer, not what was asked in a previous run.

**General Findings — accumulation and graduation:**
During active work, the run sheet is the right place for findings to land. A General Findings section (or inline cue notes) keeps observations from getting lost when there isn't time to file them to department KBs mid-session. This is intentional — the run sheet is the safe landing zone.

Cleanup happens only after findings are *confirmed* filed to the appropriate KB. Until then, they stay in the run sheet. Once promoted, the run sheet entry can be trimmed to a pointer or removed entirely. Never clean the run sheet before the KB has received the finding.

---

## Tone Translation

How Stage Management's structural decisions serve the arc. Kendra doesn't author emotional content — but the timeline she builds is itself an emotional argument. REST placement, tick density, and beat spacing are structural tone.

**"Tender" / "intimate"**
Kendra builds sparse timelines. Generous REST events between beats. Low tick-density — nothing stacked, nothing rushing. Each section has room to breathe before the next event fires. A tender section is one where the YAML almost looks unfinished — not because content is missing, but because the space between events IS the content.

**"Overwhelming / accumulated"**
Events compress. REST durations shorten progressively through the section. As the accumulation builds, the timeline tightens — multiple departments' events arrive in the same tick window. The structural compression IS the overwhelming. Kendra architects this consciously: the event density in the climactic section should look visibly denser than any prior section when you scroll the YAML.

**"Strange / uncanny"**
Irregular REST lengths. An asymmetric section duration — one section is 3 seconds, the next is 11. Events that don't land where the rhythm predicts them. Kendra introduces structural unpredictability that mirrors the emotional unease. The timeline feels "off" in the same register the show is trying to produce.

**"Building / ascending"**
REST durations progressively shorten. Tick density increases steadily. Kendra architects a visible structural arc: the section at T=0 has long gaps; the section at T=300 has almost none. The shape of the timeline IS the shape of the ascent.

**"Joy / abundant"**
Dense, overlapping event clusters. Multiple departments arriving in tight windows. REST events that are short but present — brief breaths between beats rather than long pauses. Joy in the YAML is busy in a way that feels alive rather than cluttered. Kendra distinguishes: cluttered is events colliding without intention; alive is events clustering with shared purpose.

**"Wonder"**
Isolated events with long sustained REST after. One moment per section. The timeline breathes — a single event fires and then the world is still. Wonder in the YAML is spacious. Kendra's structural principle for wonder: one well-placed event at the right tick, followed by enough silence that the player has time to find it.

**Signaling back to the Director:** When a structural decision feels ambiguous, Kendra's clarifying question is: *"Does this moment need to accumulate or arrive? Accumulation requires a longer approach. Arrival can be point-in-time."* That distinction usually resolves tick density questions.

---

## Java Capability Registry — Stage Manager Owns This

Stage Management is the production team's facility manager. In a physical theatre, the Stage Manager knows every piece of equipment in the building, what works, what doesn't, and what's on the repair list. In ScaenaShows, that means:

**What Stage Management tracks:**
- Every event type currently implemented and working (the full YAML surface — see `kb/system/cue-show-yaml-schema.md §6` for the authoritative list)
- Every known gap: features the team wants to author but cannot yet express in YAML
- Every gap's current status: open, in development, or resolved
- The development priority of each gap (informed by show authoring needs)
- Workarounds the team is using in place of gap items

**How other departments relate to this:**
Each department KB documents capability *awareness* — what their department needs to know to author correctly, including limitations that affect their work. But departments do not maintain that list or file items to the Java review team directly. If a Choreographer identifies that `FACE` doesn't set pitch, they note it for Stage Management. Stage Management files it, frames it, and tracks it.

**Why this matters:**
Without a single owner of the capability state, different departments will have different assumptions about what works, what doesn't, and what's coming. Stage Management prevents that drift.

---

## Ops-Inbox Workflow

`ops-inbox.md` is the production team's communication channel with the Java review team. Stage Management owns it end-to-end.

**The workflow:**

1. **Any team member identifies a gap** during show authoring, calibration, or in-game testing.
2. **They bring it to Stage Management** — in conversation, in a run sheet note, or in a debrief. They don't file it themselves.
3. **Stage Management evaluates the item:** Is this a gap (something that can't be done at all), a bug (something that should work but doesn't), or a limitation (an inherent Minecraft constraint)? Only the first two go to ops-inbox.
4. **Stage Management writes the ops-inbox entry** using the standard format (see below) with enough context for the Java review team to pick it up independently.
5. **Stage Management updates department KBs** when gaps are resolved. When a gap ships, remove the gap warning from the relevant department KB and update the capability section.

**Filing format for `ops-inbox.md`:**

```markdown
### [java-gap] Short description

**Area:** Department(s) affected
**Event:** Event type affected (or "new — does not exist")

What is missing or broken. Specific about the Java method, YAML field, or behavior.

**Impact:** What authoring is blocked or requires workaround.

**Fix scope:** (optional) Suggested implementation with enough detail for the review team.
```

**What does NOT go to ops-inbox:**
- Inherent Minecraft constraints (e.g., sound system doesn't support seek — this is a Minecraft limitation, not a ScaenaShows gap)
- Aesthetic preferences ("I wish fireworks had more color options")
- Future feature requests not yet needed for active show work (file in GitHub issues instead)

**Gap lifecycle:**
```
Identified → Evaluated by SM → Filed in ops-inbox (open) → Java review picks it up
→ Implemented → SM confirms → Moved to Resolved in ops-inbox → Department KBs updated
```

---

## What Stop-Safety Covers (Automatic)

When a show ends — naturally, via `/show stop`, or via player disconnect — `applyStopSafety()` runs automatically. The following items are cleaned up without any authoring action required:

| Item | How it's handled |
|------|-----------------|
| Named spawned entities with `despawn_on_end: true` | Removed from world |
| Player SPECTATOR mode (from PLAYER_SPECTATE) | Restored to pre-show game mode |
| Player flight state (from PLAYER_FLIGHT hover) | Restored to pre-show flight state |
| Slow_falling effect (from PLAYER_FLIGHT release) | Applied transitionally during restoration |
| Show-owned teams (GLOW, TEAM_COLOR) | Unregistered from server team list |
| Show-owned bossbar instances | Removed from player HUD |

---

## Pre-Show State Captured at Invocation

At show invocation (before tick 0), the following are recorded per participant. These values form the restoration baseline:

| Value | What's recorded |
|-------|----------------|
| `home` | Player's XYZ world location at invocation |
| `gamemode` | Player's game mode (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR) |
| `flight state` | Whether the player was flying at invocation |

**Stage Manager must know this list.** Any show operation that alters player state outside this list (e.g., inventory modification via COMMAND, potion effects on world-resident entities) is outside the automatic restoration baseline and requires explicit cleanup authoring.

---

## What Stop-Safety Does NOT Cover

These items require explicit cleanup authored in the show YAML or documented in the run sheet:

| Item | Risk | Required action |
|------|------|----------------|
| Blocks placed via COMMAND | Permanent world modification if show is interrupted before cleanup | Cleanup COMMAND at show end + run sheet documentation |
| REDSTONE state changes | Circuit left in wrong state | Paired REDSTONE restore event at show end |
| Entities from CAPTURE_ENTITIES | Released to world on show end — AI restored if `restore_ai: true` | If capture is destructive, pair with RELEASE_ENTITIES + DESPAWN_ENTITY |
| ENTITY_EFFECT on world-resident mobs | Potion effects persist on non-show entities after show ends | Use conservative durations; do not apply permanent effects to world mobs |
| Named entities with `despawn_on_end: false` | Persist in world permanently | Use only for intentional world fixtures; document in run sheet |
| Inventory modifications via COMMAND | Items given/removed persist after show | Document changes; write cleanup COMMAND or accept as intentional |
| TIME_OF_DAY / WEATHER changes | World time/weather left in show state | Write TIME_OF_DAY / WEATHER restore event at show end if significant |

---

## Block Modification Protocol

No BLOCK_PLACE or BLOCK_REMOVE event type exists (gap filed in ops-inbox). All block modifications go through COMMAND. COMMAND-placed blocks are not tracked by the show and are not restored by stop-safety.

**Required protocol for any COMMAND block modification:**

1. At tick N: COMMAND fires to place/modify blocks.
2. At show end (natural): matching COMMAND fires to restore original block state.
3. Document in the run sheet: tick placed, world coordinates, original block type, replacement block type.
4. Create a standalone cleanup cue (e.g., `fx.cleanup.[show_id]`) that can be invoked manually if the show is interrupted mid-run.

**Format for run sheet block modification log:**

```
Block Modifications:
  T=40: COMMAND placed minecraft:stone at (142, 64, -304). Original: minecraft:air. Cleanup at T=END.
  T=40: COMMAND placed minecraft:stone at (143, 64, -304). Original: minecraft:air. Cleanup at T=END.
  Manual cleanup: /show play fx.cleanup.[show_id]
```

**The Stage Manager rule:** No show with COMMAND block modifications runs in rehearsal until the cleanup COMMAND is authored, tested, and confirmed working.

---

## Pre-Show Tech Check

Before every in-game test, Kendra runs a tech check — distinct from the rehearsal safety checklist. The safety checklist asks "will this show leave the world clean?" The tech check asks "is the stage set correctly for this show to begin?"

```
□ World is in expected state: time of day, weather, ambient conditions match the show's opening
□ Player is in expected location: anchor point confirmed (show is portable) or player is at the correct mark
□ Any world-resident entities the show captures are present and in expected state
□ No entities from a previous run are still present that shouldn't be
□ Plugin is loaded and current version: /show list shows the expected show ID
□ Show loads without errors: no missing cue IDs, no unresolved CUE references
□ Run sheet is current: cue numbers match the revision being tested
□ Alan / Zarathale have the correct run sheet open (current revision, not a previous one)
□ Mob spawning risk assessed: if the show runs in darkness (TIME_OF_DAY ≥ 14000) at or near ground level, hostile mobs may spawn and accumulate below an airborne player during the show. When the player descends at show end, mobs will be present. Mitigate with one of: (1) COMMAND `/gamerule doMobSpawning false` at show start + `/gamerule doMobSpawning true` at show end [document in run sheet as block-modification protocol applies]; (2) run the show in a pre-lit, mob-safe location (player spawn, torchlit area, etc.). *(Identified in demo.archetype_sampler R7, 2026-03-28)*
```

If any tech check item fails, Kendra notes it before the show runs — not after. A failed tech check can invalidate the test results.

---

## Rehearsal Safety Checklist

Run this checklist before any in-game test. Mark each item in the run sheet.

```
□ All spawned entities have despawn_on_end: true (or intentional fixture noted)
□ All PLAYER_SPECTATE events have a corresponding PLAYER_SPECTATE_END
□ All PLAYER_FLIGHT hover events have a corresponding PLAYER_FLIGHT release before show end
□ All looping SOUND events have a corresponding STOP_SOUND
□ All REDSTONE state changes have a corresponding restore event at show end
□ Block modifications via COMMAND: cleanup COMMAND authored and tested
□ ENTITY_EFFECT on world-resident mobs: durations are bounded (not infinite)
□ TIME_OF_DAY / WEATHER: restore event at show end if changed significantly
□ Any COMMAND inventory changes: documented and accepted or reversed
```

---

## Utility Events — Stage Manager Domain

Stage Management owns the operational use of these utility events:

### REST

Holds time on the timeline with no effect. The deliberate pause — silence, space, breath.

```yaml
type: REST
duration_ticks: 40
name: "Breath between C3 and C4"   # optional label; appears in --scenes director mode
```

**Use:** Any intentional pause in the show timeline should be a REST, not an implied gap. A REST with a label makes the intent visible to the next author who reads the YAML.

### GROUP_ASSIGN

Distributes participants into groups at the start of the show. Must fire at tick 0.

```yaml
type: GROUP_ASSIGN
# Uses show-level group_count and group_strategy.
# group_count: 2  (in show YAML)
# group_strategy: join_order | alphabetical | random
```

**Stage Manager note:** GROUP_ASSIGN underflow is a silent skip — if fewer players exist than groups defined, extra groups have zero members, and any event targeting an empty group silently does nothing. This is correct and intentional behavior.

### COMMAND — Escape hatch

```yaml
type: COMMAND
command: "/say Hello world"
```

COMMAND is the escape hatch for anything not natively supported. Every COMMAND use in a show should be noted in the run sheet — either because it modifies the world (block protocol applies) or because it does something outside the show's tracking system.

---

## Preview Mode — Stage Management Coordinates

Preview Mode is a new subsystem (filed to ops-inbox 2026-03-30) that materializes a show
scene in-world without running the full timeline — entities at their marks, blocks in show
state, time of day set. Stage Management owns three things in preview:

**1. The cleanup contract.** Preview stop-safety mirrors show stop-safety. A `PreviewSession`
tracks all spawned entities, block changes, and player state. `/scaena preview dismiss` is
the equivalent of `/show stop` — full restore. No preview ships without a confirmed cleanup.

**2. The scope definition.** Stage Management defines which events are "setup events" (fire
in preview) vs. "sequence events" (do not fire in preview). Setup events establish scene
state: SPAWN_ENTITY, BLOCK_STATE, ENTITY_EQUIP, TIME_OF_DAY. Sequence events are narrative
progression — they belong to the show timeline, not preview.

**3. Phase ownership.** Two phases are defined:
- **Phase 1 (pre-YAML):** Show-params + scout_captures drive the preview. Entities spawn at
  scouted positions with kit from show-params. No YAML required. This is the scouting-phase
  tool.
- **Phase 2 (post-YAML):** Show YAML drives the preview. Setup events are extracted and fired
  as a snapshot. Voice lines print to chat, sounds play once, particles fire.

Full department scope table and implementation notes: `ops-inbox.md §Preview Mode`.

---

## Active Gap Registry

Stage Management maintains this registry. All open items are in `ops-inbox.md` with full filing detail. This table is Stage Management's operational view — safety implications and department impact.

| Gap | Affected departments | Safety implication | ops-inbox status |
|-----|---------------------|--------------------|-----------------|
| SPAWN_ENTITY variant/profession not applied | Wardrobe, Casting | None — field silently ignored | Open |
| FACE pitch gap | Camera, Choreography | None — workaround available (PLAYER_TELEPORT) | Open |
| No BLOCK_PLACE/BLOCK_REMOVE | Set Director, Stage Manager | **High** — COMMAND blocks not covered by stop-safety | Open |
| No TITLE_CLEAR | Voice | None — cosmetic workaround available | Open |
| No smooth ROTATE | Camera, Choreography | None — PLAYER_SPECTATE workaround available | Open |
| Show scanner — flat `shows/*.yml` only; no subdirectory scan | Stage Manager, all shows | None — flat YAMLs still load | Open |
| ENTITY_AI/behavior events resolve only first group member | Casting, Wardrobe, Choreography | None — workaround: address by name | Open |
| `capture_mode: live` parsed but not implemented | Casting | None — snapshot mode works | Open |
| `entity:world:Name` targeting not implemented | Casting | None — use entity:spawned: | Open |
| ENTITY_SPEED group targeting broken | Choreography | None — workaround: address by name | Open |
| ENTER does not apply equipment fields | Choreography, Wardrobe | None — workaround: SPAWN_ENTITY + CROSS_TO | Open |
| RETURN_HOME skips non-Player entities | Choreography | None — workaround: CROSS_TO | Open |
| FIREWORK `min_clearance` not enforced | Fireworks | None — safety check just doesn't apply | Open |
| FIREWORK preset `launch:` mode not applied | Fireworks | None — spawn position is event-controlled | Open |
| FIREWORK_LINE gradient fields not parsed | Fireworks | None — always red→blue | Open |
| FIREWORK_FAN — no power/color variation | Fireworks | None — workaround: multiple FIREWORK_LINE | Open |

**When a gap is resolved:** Remove from this table, move the ops-inbox entry to Resolved with the version it shipped in, update the affected department KBs.

---

## Version Bump Reminder

The Stage Manager tracks version policy on behalf of all departments:

| Change type | Bump required |
|-------------|--------------|
| New cues, shows, or docs added | MINOR |
| Java plugin logic changed | MINOR |
| Bug fix only, no new content | PATCH |
| Breaking schema change or major rework | MAJOR |

Always bump before building. Never build the same version number twice. Version file: `build.gradle.kts` (`version = "x.y.z"`).

---

## Capability Status Summary

| Instrument / Function | Status | Notes |
|-----------------------|--------|-------|
| Prompt book / YAML structure ownership | ✅ Active | Core SM function |
| Run sheet authoring and maintenance | ✅ Active | Kendra's document; updated every revision |
| Beat collision detection and resolution | ✅ Active | Protocol defined; escalation format defined |
| Cleanup contract / stop-safety oversight | ✅ Active | Automatic items documented; manual checklist defined |
| Ops-inbox workflow | ✅ Active | `ops-inbox.md` maintained; 16 open items |
| Java capability registry | ✅ Active | Active Gap Registry table above; full detail in `ops-inbox.md` |
| Cue naming convention enforcement | ✅ Active | `[category].[archetype].[variant]` standard |
| REST event (deliberate pause) | ✅ Verified | Confirmed working in all show revisions |
| GROUP_ASSIGN (participant grouping) | ✅ Verified | Tick-0 requirement; underflow = silent skip (by design) |
| COMMAND escape hatch | ✅ Verified | Working; outside stop-safety contract |
| Show creation workflow coordination | ✅ Active | Process defined; `stage-management.show-creation-process.md` for full detail |
| Department briefing coordination | ✅ Active | Blocking/non-blocking table defined per pass type |
| YAML authorship sequencing | ✅ Active | Kendra gates on dept decisions before authoring begins |
| BLOCK_PLACE / BLOCK_REMOVE | ⚠️ Gapped | Requires COMMAND; outside cleanup contract — **High safety risk** |
| Scout sidebar display labels | ⚠️ Gapped | Sidebar shows raw codes/names; `display_label` field not yet supported — ops-inbox filed 2026-03-30 |
| Preview Mode (`/scaena preview`) | 📋 Designed | Full spec in ops-inbox 2026-03-30; Phase 1 (pre-YAML entity + block spawn) is priority |
