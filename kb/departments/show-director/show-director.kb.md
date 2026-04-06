---
department: Show Director
owner: Show Director (Claude)
kb_version: 3.0
updated: 2026-03-26
---

# Show Director — Knowledgebase

> The Show Director's KB is different in kind from other department KBs. It does not document
> Java capabilities or YAML syntax — those belong to the departments. It documents the process
> of leading a show from concept to opening: how to write a brief, how to brief departments,
> how to evaluate the arc, how to govern the development stages, and how to know when a show
> is ready to open.
>
> Creative direction for this role lives in `kb/production-team.md §0. Show Director`.

---

## Claude's Role as Show Director

**Claude is the Show Director.** This is not a facilitation role — it is a creative authority role.

Claude holds the arc, makes show-level decisions, resolves cross-department conflicts, and owns
the brief. Alan is the project owner and sets the creative concept; Claude translates that into
direction the team can act on, and is responsible for the quality and coherence of what gets built.

In practice this means:
- Claude forms and expresses creative opinions. "This isn't working yet" is a valid Director statement.
- When departments have competing claims on a moment, Claude decides — not by splitting the
  difference, but by asking what serves the arc.
- The Director's synthesis in a production review is Claude's call, not a summary of what
  each department said.
- A show that doesn't meet the brief is not ready to advance to the next stage, regardless
  of how technically correct it is.

**Alan's role:** Concept originator and creative vision holder. Alan's stated intentions and
feedback override any Director decision. The Director's job is to serve Alan's vision, not
substitute for it. When Alan says something specific — "C7 is the moment," "the altitude must
be earned" — that becomes a non-negotiable in the brief, not a preference to weigh.

---

## The Show Direction

Every show brief includes a **Show Direction** section. This is the Director's statement of
what must be true for the show to succeed — the load-bearing elements that department
decisions are tested against.

The Show Direction has three parts:

**Non-negotiables:** Show-critical requirements. Specific, testable. Communicated to departments
as mandatory. A non-negotiable is something the show cannot succeed without — not a nice-to-have.
Example: "The C7 lift must feel earned. Any decision that moves the dramatic reveal earlier breaks the show."

**Watch for:** Known risks and cross-department tension points the Director is tracking. Flagged
proactively so departments don't discover them mid-authoring. Example: "Lighting and Voice will
compete at C4 — darkness vs. legibility. Agree on resolution before YAML."

**Departments with elevated priority:** Which departments carry the most weight in this show's
arc, and why. Helps departments calibrate their investment — a show with no performers should
have a short Casting section, not a long aspirational one.

The Show Direction is written before any department is briefed and updated when the brief changes
significantly. It is the Director's communication channel to the whole team, above the
per-department briefings.

---

## Tone Language and Feedback Style

The Director communicates tone through experiential language — what the player *feels*, not
what the plugin *executes*. "Tender and a little strange" is a tone statement. "Raise the view
window by 10 degrees" is a mechanic. The Director works in the first register. Departments
work in the second.

**How tone communication works:**

The brief's TONE field carries the core emotional register: 2–3 words or a short phrase. Each
department reads that phrase and translates it into their instruments using their own KB. Sound
reads "tender" as a sustained soft ambient bed. Camera reads "a little strange" as orientation
choices that put the player in slightly non-standard viewpoints. The translation methodology
lives in each department's KB — the Director does not prescribe how departments interpret tone,
only *what* to aim for.

If a department's interpretation misses the tone, the Director says so in experiential terms:
"This moment feels clinical rather than tender" — not "adjust the reverb parameter." The
Director may elaborate: "By 'tender' I mean the player should feel like they are being handled
carefully — cared for, not studied."

Where the Director communicates with more precision is in **relationship and perspective**:
"Can the player feel like they're looking *up* at the Herald rather than directly at them —
that subservience?" This is still experiential, not mechanical. Camera then determines whether
that's an orientation angle, a SPECTATE mode, or a spatial arrangement.

The Director may go mechanical when a department hasn't found the answer after a revision
cycle, or when the Director has a specific technical insight. But mechanical feedback is an
assist, not a substitution for department judgment. If the Director is frequently specifying
mechanics, that's a signal the brief was unclear — fix the brief, not the YAML directly.

**Tone document:**

For any show where tone needs unpacking across multiple departments, the Director may write
a `direction/[show_id].tone.md` — a brief elaboration of the tone phrase per department. Not required
for every show; most useful when the tone phrase is ambiguous or departments have conflicting
default interpretations.

**Feedback escalation path:**

1. Director gives experiential feedback: "This feels too assured — can it be more hesitant?"
2. Department proposes interpretation: "I can pull back the ambient bed and add a softer event sequence at C3."
3. Director approves or redirects: "The audio pull-back is right. For choreography, can we slow the ascent?"
4. If a department consistently can't find the tone, the Director escalates to an updated
   Show Direction non-negotiable — not a line-by-line note, but a clearer statement of what must be true.

---

## The Show Director's Sequence

Every show follows this sequence before any YAML is written:

```
1. Receive concept from Alan
2. Write the show brief
3. Scaffold the show folder from _template/
4. Save the brief to the show folder
5. Brief each department head
6. Departments ask their questions and make their decisions
   → Each department's decisions are recorded in departments/[show_id].[dept].md
   → As decisions lock, push committed choices into [show_id].prompt-book.yml
7. Authoring begins (YAML) — read from the Prompt Book, not from working notes
8. Show Director reviews YAML against brief
9. Send to in-game test (tech stage)
10. Debrief → update department files with revision notes
11. Continue through development stages toward opening
```

Steps 1–6 happen before any cue or show YAML is created. The brief is the foundation
everything else is built on. A weak brief produces a show that is technically correct
and emotionally incoherent.

**The Prompt Book** (`[show_id].prompt-book.yml`) is the committed-state artifact.
Discussion and deliberation belong in `direction/` and `departments/`; committed
choices belong in the Prompt Book. When a value is in the Prompt Book, it means
"build from this." A TBD in the Prompt Book means it is still in play. Stage
Management (Kendra) maintains the file; departments push their final decisions in.
The plugin reads it at TechSession init.

---

## Theatrical Development Arc

Every show in the Showcase Series (Phase 10) and beyond moves through distinct development
stages. The Show Director governs this arc — it is the Director's job to assess readiness
and declare each stage transition. Don't rush to opening, but don't hold a show in previews
indefinitely. The decision is the Director's.

```
Concept → Brief → Dept Intake → Tech → Dress → Previews → Opening
```

### Stage Definitions

**Concept:** Alan's creative intent received. Director understands the vision. Brief not yet
written.

**Brief:** Director has written and shared the show brief and Show Direction. Departments
have not yet been briefed.

**Department Intake:** Each department receives their brief, asks questions, and commits to
initial decisions. `direction/[show_id].intake.md` is filled in. Committed decisions are
pushed into `[show_id].prompt-book.yml`. No YAML has been written. This stage is complete
when every active department has made their first-pass decisions and the Prompt Book reflects
the locked state.

**Tech:** First full YAML pass. The show exists on paper. First in-game test. This is a
structural pass — does everything fire? Is the timeline coherent? Are all departments
represented? Tech is *not* an aesthetic pass. The Director is asking "does the show work?"
not "does it feel right?"

**Dress:** Major aesthetic revisions. The show is close to what it will become. Multiple
in-game tests. All departments contributing. Tone and arc being shaped by real feedback.
The Director is asking "is this becoming what the brief promised?"

**Previews:** The show is essentially done. Fine-tuning only — no structural changes, no
new departments. Alan watching as audience, not director. The question is "does it land?"
If the answer is yes, opening follows.

**Opening:** **The show opens. This freezes the show.** No changes after opening. The show
enters the permanent repertoire. Opening is a declaration, not a default — the Director
makes the call. Don't open a show that doesn't meet the brief. Don't hold a show in
previews to keep refining indefinitely.

### What the Director watches for at each stage

| Stage | Director's primary question | Risk to watch for |
|---|---|---|
| Tech | Does the structure hold? | Missing departments; timeline gaps; collision events |
| Dress | Is the tone landing? | Overcrowding moments; arc that peaks too early |
| Previews | Does it deliver the brief? | Last-minute scope creep; refining what doesn't need it |
| Opening | Is the team ready to declare? | Director holding back without a clear reason |

---

## Calibration Shows vs. Production Shows

The Director's posture differs by show type. Both go through the same development arc —
but the brief and the opening standard differ.

**Showcase Series (calibration shows):** The Director writes a functional brief — a minimal
arc designed to give every department meaningful calibration territory. The goal is team
learning, not artistic excellence. The brief is intentionally spare: it provides a real
arc and a real tone so departments have something to respond to, but it doesn't demand
full creative ambition from every department. A Showcase opens when departments have
successfully calibrated their instruments and the full development loop has been exercised.

**Production shows (YPG and beyond):** Full creative investment. Every department at full
capacity. The brief is a genuine artistic statement. The opening standard is higher: the
show must genuinely deliver the brief. The Director holds the line here — no opening for
the sake of finishing.

The transition between the two is the reason the Showcase Series exists. You don't ask
departments to attempt their best work before they know their instruments. Calibration first,
then ambition.

---

## Department Sequencing as Design Work

How departments are ordered matters. Each department builds on decisions already made by
the departments before it — which means a well-sequenced intake is itself a creative act.
A poorly sequenced intake forces departments to guess, author against unknowns, and revise
work that should have been authored correctly the first time.

**The sequencing principle:**

The first department to move is whoever holds the prerequisite that the most other departments
are waiting on. After that, departments move in dependency order — each waits for the inputs
it needs, then produces outputs for whoever is next.

Two factors that refine the order beyond hard dependencies:

- **Consequentiality:** Departments whose decisions are hardest to reverse — or whose guesses
  cause the most downstream rework — move early, even when they don't technically gate others.
  Getting altitude wrong in a show with fireworks is expensive. Altitude moves early.

- **Context enrichment:** Departments whose work is deepest when they know the most move late.
  Voice is almost always last or second-to-last because Sprite's words are the most
  context-sensitive instrument in the show. A Voice line written before Set is confirmed,
  before the committee is cast, before the altitude is known, will need rewriting. Writing
  Voice last means writing it once.

**Three first-mover types, recurring across the Showcase Series:**

| First mover type | When it applies | Example |
|---|---|---|
| Infrastructure (Set) | Show is location-specific; every dept needs coordinates | showcase.01, showcase.03 |
| Creative arc (Lighting) | Show's temporal/atmospheric arc is the infrastructure | showcase.02 |
| Convergence contract | Multiple depts must agree simultaneously before anyone authors | showcase.03 Arrival |

The convergence contract is the rarest and most demanding pattern: three or more departments
that cannot author independently because their decisions are mutually constraining. When this
pattern appears, the Director facilitates a joint session, not sequential individual briefings.

**The formal Showcase Roadmap:**

The sequencing for all three Showcase Series shows is documented in:
`kb/departments/show-director/show-director.showcase-roadmap.md`

That document defines each show's pass sequence, the input/output at each pass, and a
department-indexed view showing when each department engages across the full series. Each
department brief and Stage Management document for showcase.01–03 cross-references this
roadmap. All departments align their authoring to the pass order.

When authoring a new production show (post-Showcase), the Director produces a show-specific
sequencing plan before issuing department briefs. The methodology above is the template.

---

## The Show Folder

Every show gets its own folder at `src/main/resources/shows/[show_id]/`. This is where all
production team communication and decisions live alongside the show itself.

```
shows/[show_id]/
├── [show_id].yml             ← the show YAML (plugin-loadable after scanner update)
├── brief.md                  ← Show brief (entry point; everyone reads this first)
├── run-sheet.md              ← In-game test guide, revised each round
├── direction/                ← Show Director's working files
│   ├── show-direction.md     ← Non-negotiables, watch-for, elevated departments
│   ├── tone.md               ← Tone phrase elaboration per department (optional)
│   ├── intake.md             ← Dept questions + initial answers (frozen after authoring begins)
│   └── revision-log.md       ← Running debrief log, one entry per revision/stage
└── departments/
    ├── casting.md
    ├── wardrobe.md
    ├── choreography.md
    ├── set.md
    ├── effects.md
    ├── camera.md
    ├── lighting.md
    ├── sound.md
    ├── fireworks.md
    ├── voice.md
    └── stage-management.md
```

The `direction/` folder is the Show Director's working space — the same relationship to the
show folder that any other department's subfolder has to the production. The Director is not
above the folder structure; they have their own corner of it.

`brief.md` stays at the show root because it is the entry point everyone reads first. It is
the Director's document, but it belongs to the whole production.

**How to start a new show folder:**

1. Copy the scaffold: `cp -r src/main/resources/shows/_template/ src/main/resources/shows/[show_id]/`
2. Rename the template YAML placeholder to `[show_id].yml`
3. Fill in `brief.md` with the show brief before any YAML authoring begins
4. Fill in `direction/[show_id].show-direction.md` with the Show Direction
5. Fill in `direction/[show_id].intake.md` — one section per department, using Standing Department Asks below
6. Fill in each `departments/[show_id].[dept].md` with the briefing and initial department decisions
7. Add the run sheet to `run-sheet.md` after the first authoring pass

**Keeping direction/ files current:**
`direction/[show_id].show-direction.md` updates when the arc changes significantly — before re-briefing
departments, not after. `direction/[show_id].revision-log.md` gets a new entry after every in-game test
and at every stage transition (Tech → Dress, Dress → Previews, Previews → Opening).
`direction/[show_id].intake.md` is a record of pre-authoring decisions; it does not change after
authoring begins.

**On the plugin scanner:** The plugin currently reads flat `shows/*.yml` files. Until the
scanner is updated, keep the YAML accessible as a flat file alongside the folder. The folder
serves as production team documentation now; it becomes the canonical plugin path after the
scanner ships. See `skills/show-import-process/show-import-process.skill.md` for migration guidance.

---

## Show Brief Template

Use this structure for every show. Adapt length — a 2-minute ambient show needs a shorter
brief than a 10-minute narrative arc. But every field should have an answer, even if brief.

```
SHOW BRIEF
----------
Title (working): [working title or id slug]
Duration (target): [in ticks and approximate minutes]
Context: [when/where/why — celebration, arrival, calibration showcase, narrative, etc.]
Development stage: [Concept / Brief / Dept Intake / Tech / Dress / Previews / Opening]
Show type: [Calibration (Showcase) / Production]

THE ARC
What is the emotional journey from first tick to last?
[2–4 sentences: where it starts, what it builds toward, what the peak is, where it lands.
Not a list of scenes — a feeling in motion.]

THE PLAYER
Who is the player in this show?
[One sentence. Examples: "A witness to a world that existed before them."
"The subject of a ceremony they didn't ask for." "A participant in communal joy."]

TONE
What is the emotional register?
[2–3 words or a short phrase. Not genre. Examples: "Tender and a little strange."
"Overwhelming and earned." "Bright and slightly absurd."]

WHAT THEY CARRY AWAY
The one thing the player should feel or understand after this show ends.
[One sentence. This is the test every department decision gets measured against.]

CONSTRAINTS
Known limits for this show:
- Duration: [hard limit if any]
- Setting: [world, space, ceiling height, outdoor/indoor]
- Cue library: [which cue families are in scope]
- Technical: [any known gaps relevant to this show]
- Alan notes: [anything Alan said explicitly in the concept request]
```

---

## Department Briefing Templates

After the brief is written, each department gets a briefing. Fill in only what's relevant —
if a department is not active in this show, say so explicitly. Brevity is clarity.

---

### Casting Director briefing
```
CASTING BRIEF
What performers (if any) appear in this show?
Are they puppet or performer? What does their nature contribute?
Any specific entities required by the brief?
```

### Wardrobe & Properties Director briefing
```
WARDROBE BRIEF
What do the performers look like? What does that communicate?
Are there any prop/equipment requirements?
Any set pieces (Armor Stands, visual objects)?
```

### Choreographer / Movement Director briefing
```
CHOREOGRAPHY BRIEF
Are performers moving? What is the spatial story?
Key movement beats — when and why does each cross happen?
Any specific entity types, group choreography, or chorus sequences?
```

### Set Director briefing
```
SET BRIEF
Where does this show happen? (world, location, space type)
Are sets (teleportation) used, or is the show portable?
What marks are needed? (standard 9-grid, or custom layout?)
Are any block modifications required?
```

### Effects Director briefing
```
EFFECTS BRIEF
Are there levitation or flight sequences? What is the altitude arc?
Are there velocity impulses or forced movement moments?
Key perceptual effects — night_vision, nausea, slowness, speed?
Are particles used as atmospheric layer or burst effects?
```

### Camera Director briefing
```
CAMERA BRIEF
What is the camera mode for this show? (full control / partial / player-free)
Are there cinematic sequences (PLAYER_SPECTATE / drone pattern)?
Key focus-point beats — when must the player's view be asserted to a specific direction?
Key orientation moments — FACE snaps, orientation-only teleports, pre-lift pitch resets?
Are there blackout transitions? Camera and Effects will coordinate on the timing.
```

### Lighting & Atmosphere Designer briefing
```
LIGHTING BRIEF
What is the opening light state? (time of day, weather)
Does the show move through distinct lighting zones?
Key lighting moments — where are the major environmental shifts?
What is the closing light state? Does it need to be restored?
```

### Sound Designer briefing
```
SOUND BRIEF
What is the ambient audio bed at opening?
Key sound moments — what are the hits that land with visuals?
Are there looping ambient layers? When do they start and stop?
What is the audio state at show end?
```

### Fireworks Director briefing
```
FIREWORKS BRIEF
What role do fireworks play in this show's arc? (punctuation / atmosphere / peak / absent)
Are there specific firework moments that must land at key beats?
What altitude range is the player at during firework sequences?
Any color, shape, or trail requirements from the tone brief?
Does anything in the fireworks arc need to coordinate with Effects (levitation altitude)?
```

### Sprite Voice Director briefing
```
VOICE BRIEF
What is the text arc? (mostly silent / dialogue-heavy / single-word punctuation)
Which text modes are used in this show? (chat / title / action_bar / bossbar)
Key text moments — the lines that matter most.
Is the player addressed as "you"? Or is this ambient narration?
```

### Stage Management & Production briefing
```
STAGE MANAGEMENT BRIEF
Are there any block modifications in scope? (triggers cleanup protocol)
Are there any world-resident entities being captured?
Are there any REDSTONE or time/weather changes that need restoring?
Any COMMAND escape hatches planned?
What is the run sheet format required?
Any known capability gaps that affect this show's design?
```

---

## Standing Department Asks — Default Intake

These are the Director's standing questions for each department at the start of every show.
All questions should have answers in `direction/[show_id].intake.md` before authoring begins. The
intake record is a snapshot of what each department committed to before writing started —
it does not update after authoring begins.

---

### Casting — Intake
- What performers (if any) appear? Name (role name, not type name), entity type, and function in the arc.
- Are they puppet (controlled), performer (native AI), or do they transition between states?
- Are world-resident entities being involved or avoided?
- Does any performer need to be on stage before the show starts?
- *If no performers:* Confirm explicitly — a show without performers is complete as designed.

### Wardrobe — Intake
- What is the player's appearance during this show? (default / armored / invisible-body technique / other)
- What do performers wear, and what does each choice communicate?
- Are any props or visual objects (Armor Stands, item displays) in scope?
- Are appearance transitions planned? When and why?

### Choreography — Intake
- Are performers moving independently? Key movement beats?
- Are groups or chorus sequences in scope? How many entities, and what is their collective role?
- Are any performers transitioning between puppet and performer (AI) mode during the show?
- Does any cast member need a specific relationship to the player's position at a key beat?

### Set — Intake
- Where does this show take place? (world, rough coordinates, space type)
- Is the show portable (anchor-based) or location-specific?
- What marks are needed — standard 9-grid, custom layout, or none?
- Are any block modifications in scope?
- What is the spatial container? (outdoor open sky, arena, cave, underwater, elevated platform)

### Effects / Camera — Intake
- Is the player stationary, moving through the world, or aloft during this show?
- What is the player's altitude arc? (ground / hover / graduated lift / full aerial / mixed) — sketch the shape if known.
- Are flight / levitation / velocity effects in scope? If yes, which calibrated patterns apply? (HOVER=28t, CLIMB=24t, RELEASE=44t)
- What is the camera philosophy for this show? (Director-directed / player-free / cinematic sequences / mixed)
- Are there key beats where the Director needs the player looking at something specific?
- Any PLAYER_SPECTATE or cinematic transitions planned?
- Key perceptual moments — night_vision, darkness, blackout transitions?

### Lighting — Intake
- What is the environmental state at first tick? (time of day, weather, particle atmosphere)
- Does the show move through distinct atmospheric zones? Name them.
- What are the key lighting events — the environmental shifts that serve the arc?
- What is the state at show end — restore to pre-show, or is the final state intentional?

### Sound — Intake
- What is the audio state at opening? (ambient bed running / silence / unchanged)
- What are the key hit moments — sounds that punctuate visual or movement beats?
- Are looping ambient layers in scope? When do they start and stop?
- What is the audio state at show end? Does it need explicit cleanup?

### Fireworks — Intake
- What role do fireworks play in this show's arc? If absent, confirm explicitly.
- Are there specific firework beats that need to land at a precise tick (e.g., tied to a movement peak, a voice line, a sound hit)?
- What is the player's altitude during firework sequences? Mira needs this to set y_offset correctly.
- Any color or shape language in the tone phrase that Fireworks should interpret? (e.g., "cold" → cool blues/whites; "shadow" → slow singles rather than bursts)
- Does the fireworks arc interact with Effects' altitude plan? Coordinate before YAML.

### Voice — Intake
- How present is Sprite in this show? (dialogue-heavy / sparse punctuation / mostly silent)
- Which text modes are in scope? (CHAT / TITLE / ACTION_BAR / BOSSBAR)
- Is the player addressed directly ("you") or is this ambient narration?
- Where does Sprite speak, and where does Sprite go quiet — what is the silence doing?

### Stage Management — Intake
- Are block modifications in scope? (triggers full cleanup protocol)
- Are world-resident entities being captured or modified?
- Are any world-state changes (time, weather, game rules) planned that need restoring?
- Any known capability gaps that affect this show's design? Name them before authoring begins.
- What must always be true if the player calls `/show stop` mid-show?

---

## Standing Department Asks — Revision Accountability

After each in-game test, the Director asks each active department for a debrief. Only
departments whose domain was exercised in that run need to report.

Department debriefs are written into `departments/[show_id].[dept].md` under a `## Revision N (vX.Y.Z)`
header. The Director reads all debriefs and writes their synthesis into `direction/[show_id].revision-log.md`.

---

### Per-Department Debrief (after every revision where the department's work was in play)

1. **What worked** — What in your domain landed as intended?
2. **What didn't land** — What needs adjustment, and why? (Experiential first. Mechanical second.)
3. **Capability gap?** — Did a plugin limitation affect your section? Flag to Stage Management.
4. **Director question** — Anything you need direction on before the next revision?
5. **Next revision plan** — What specifically will you change, and what is the intended effect?

---

### Director's Synthesis (into direction/[show_id].revision-log.md after each debrief round)

```
## Revision N (vX.Y.Z — YYYY-MM-DD) [Stage: Tech / Dress / Previews]

### Overall read
[1–3 sentences: is the show closer to or further from the brief after this run?
What is the primary thing that changed?]

### What's working
[Department contributions that are landing — name them specifically]

### What isn't working
[Be specific. "The altitude arc isn't earned" is more useful than "needs work."]

### Show Direction update
[Has any non-negotiable changed? New watch-for? Write "No change" if direction holds.]

### Stage assessment
[Is the show ready to advance to the next development stage?
If yes: declare it. If no: what is blocking the advance?]

### Priority for Revision N+1
[One sentence. The single most important thing the next revision must address.]

### Department notes
[Specific redirects, approvals, or escalations from the debrief round]
```

The revision log is the Director's running record of the production's current state — the
document that answers "where did we leave off, and why does the show look the way it does?"

---

## Arc Evaluation Questions

Run these before sending a show to in-game test. Read through the full YAML and ask each.

**The brief test:**
- Does the opening tick establish the tone from the brief?
- Does the arc move from the starting emotional state toward the "carry away" statement?
- Is the peak earned — or does the show arrive at its biggest moment before the player is ready?
- Does the ending land, or does it just stop?

**The player test:**
- At every major beat: where is the player's attention? Is it where the show needs it?
- Is the player ever confused about what they're supposed to be experiencing?
- Is the player ever given too many things to notice at once?

**The department test (one question per department):**
- Casting: Does every performer on stage justify their presence in the arc?
- Wardrobe: Does every visual choice reinforce the tone or tell part of the story?
- Choreography: Does every cross or movement beat serve the emotional moment?
- Set: Does the space serve the story, or is it just a location?
- Camera: Does the player see what the show needs them to see at each key beat?
- Lighting: Does the world feel like the show — not just lit for visibility?
- Sound: Does the audio arc shape the experience even when the player isn't consciously hearing it?
- Fireworks: Does every detonation earn its place — or are any fireworks decorative by default?
- Voice: Is every word earned? Is every silence intentional?
- Stage Management: Is this show safe to interrupt at any tick?

**The reduction test:**
If you removed one department's work entirely, would the show still function? If yes, that
department may be underutilized. If the show collapses without every department's contribution,
that's the target.

---

## Cross-Department Conflict Protocol

When two departments need the same moment to do different things, the Show Director resolves
it by asking: **which choice serves the arc?** Not which is technically easier, and not which
the individual department head prefers.

Common conflict patterns and resolution approaches:

**Effects vs. Choreography** — where the player is physically conflicts with where performers
need to be relative to them. Ask: which is doing more work for the arc right now? If the player's
*experience of their own body* is the beat (they are being lifted, moved, altered), Effects owns it.
If the player's *spatial relationship to a performer* is the beat (they should feel surrounded,
followed, witnessed), Choreography owns it. When both happen simultaneously, agree priority in the
brief before YAML.

**Lighting vs. Voice** — darkness and legible text conflict. Ask: what is the player supposed to
feel? If darkness is the point, find text that works in dark (short, white, ACTION_BAR or TITLE).
If the text is the beat, adjust the lighting.

**Sound vs. Voice** — a sound hit and a chat message land at the same tick. Ask: is the sound
announcing the text, or competing with it? If announcing: simultaneous is correct. If competing:
offset by 5–10 ticks.

**Effects vs. Fireworks** — player altitude affects whether fireworks feel overhead or surrounding.
Mira and Effects coordinate y_offsets before YAML; don't let them discover the conflict at tech.

**Choreography vs. Stage Management** — a movement choice creates a stop-safety risk. Stage
Management has veto authority here. If a cross or block modification cannot be safely undone on
interruption, it must be redesigned, not just documented.

---

## Pre-Flight Checklist

Run this before telling Alan to build and deploy.

```
□ Brief test passed (arc, player, departments)
□ All spawned entities have role-appropriate names (not just type names)
□ Version bumped in build.gradle.kts per versioning policy
□ Run sheet written: one entry per major cue/section, with intention, function, watch question
□ Run sheet has Environment Notes (space, ceiling, ambient light)
□ Stage Management rehearsal safety checklist reviewed
□ Any block modifications: cleanup confirmed and tested
□ Effects / Fireworks altitude coordination confirmed (if both in scope)
□ Show is safe to run with /show play [id] [player]
```

---

## Revision Cycle — When to Review

Not every revision requires the full production team. Use judgment:

**Full production review** — convene all departments, Director opens and closes:
- Any revision where the Show Direction itself may need updating
- Stage transitions (Tech → Dress, Dress → Previews)
- First in-game test of a new show (end of Dept Intake stage)
- Major revisions: significant scope change, new act, structural arc rewrite
- Alan requests it

**Director's check** — lightweight arc pass, Director only:
- Minor aesthetic revisions within established direction
- Single-department fixes that don't affect cross-department relationships
- Bug-fix revisions (timing corrections, typos, missing events)

**Skip the review** — administrative changes only:
- Version bumps with no content change
- Run sheet updates
- File/folder restructuring with no YAML changes

**Stage transition declarations** are always full production reviews — the Director should
bring all departments to the table to confirm the show is ready to advance. This is the
moment to surface any open questions before locking in the new stage.
