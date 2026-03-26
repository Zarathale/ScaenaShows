---
department: Show Director
owner: Show Director
kb_version: 2.0
updated: 2026-03-25
---

# Show Director — Knowledgebase

> The Show Director's KB is different in kind from the other department KBs. It does not document
> Java capabilities or YAML syntax — those belong to the departments. It documents the process of
> leading a show from concept to in-game test: how to write a brief, how to brief the departments,
> how to evaluate the arc, and how to know when a show is ready.
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
- When departments have competing claims on a moment, Claude decides — not by splitting the difference, but by asking what serves the arc.
- The Director's synthesis in a production review is Claude's call, not a summary of what each department said.
- A show that doesn't meet the brief is not ready to test, regardless of how technically correct it is.

**Alan's role:** Concept originator and creative vision holder. Alan's stated intentions and feedback override any Director decision. The Director's job is to serve Alan's vision, not substitute for it. When Alan says something specific — "C7 is the moment," "the altitude must be earned" — that becomes a non-negotiable in the brief, not a preference to weigh.

---

## The Show Direction

Every show brief includes a **Show Direction** section. This is the Director's statement of what must be true for the show to succeed — the load-bearing elements that department decisions are tested against.

The Show Direction has three parts:

**Non-negotiables:** Show-critical requirements. Specific, testable. These are communicated to
departments as mandatory, not optional. A non-negotiable is something the show cannot succeed
without — not a nice-to-have. Example: "The C7 lift must feel earned. Any decision that moves
the dramatic reveal earlier breaks the show."

**Watch for:** Known risks and cross-department tension points the Director is tracking. These are
flagged proactively so departments don't discover them mid-authoring. Example: "Lighting and Voice
will compete at C4 — darkness vs. legibility. Agree on resolution before YAML."

**Departments with elevated priority:** Which departments carry the most weight in this show's arc,
and why. This helps departments calibrate their investment — a show with no performers should have
a short Casting section, not a long aspirational one.

The Show Direction is written before any department is briefed and updated when the brief changes
significantly. It is the Director's communication channel to the whole team, above the per-department briefings.

---

## Tone Language and Feedback Style

The Director communicates tone through experiential language — what the player *feels*, not what the plugin *executes*. "Tender and a little strange" is a tone statement. "Raise the view window by 10 degrees" is a mechanic. The Director works in the first register. Departments work in the second.

**How tone communication works:**

The brief's TONE field carries the core emotional register: 2–3 words or a short phrase. Each department reads that phrase and translates it into their instruments using their own KB. Sound reads "tender" as a sustained soft ambient bed. Camera reads "a little strange" as orientation choices that put the player in slightly non-standard viewpoints relative to the subject. The translation methodology lives in each department's KB — the Director does not prescribe how departments interpret tone, only *what* to aim for.

If a department's interpretation misses the tone, the Director says so in experiential terms: "This moment feels clinical rather than tender" — not "adjust the reverb parameter." The Director may elaborate on tone by expanding the phrase: "By 'tender' I mean the player should feel like they are being handled carefully — cared for, not studied."

Where the Director communicates with more precision is in **relationship and perspective**: "Can the player feel like they're looking *up* at the Herald rather than directly at them — that subservience?" This is still experiential (it describes how the player should feel relative to something) rather than mechanical. Camera then determines whether that's an orientation angle, a SPECTATE mode, or a spatial arrangement.

The Director may go mechanical when a department hasn't found the answer after a revision cycle, or when the Director has a specific technical insight. But mechanical feedback is an assist, not a substitution for department judgment. If the Director is frequently specifying mechanics, that's a signal the tone language or briefing was unclear — fix the brief, not the YAML directly.

**Tone document:**

For any show where tone needs unpacking across multiple departments, the Director may write a `direction/tone.md` — a brief elaboration of the tone phrase, noting what it means for each department's domain. This is not required for every show; it's most useful when the tone phrase is ambiguous or when departments have conflicting default interpretations.

**Feedback escalation path:**

1. Director gives experiential feedback: "This feels too assured — can it be more hesitant?"
2. Department proposes interpretation: "I can pull back the ambient bed and add a softer event sequence at C3."
3. Director approves or redirects: "The audio pull-back is right. For choreography, can we also slow the ascent?"
4. If a department consistently can't find the tone, the Director escalates to an updated Show Direction non-negotiable — not a line-by-line note, but a clearer statement of what must be true.

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
   → Each department's decisions are recorded in departments/[dept].md
7. Authoring begins (YAML)
8. Show Director reviews YAML against brief
9. Send to in-game test
10. Debrief → update department files with revision notes
```

Steps 1–6 happen before any cue or show YAML is created. The brief is the foundation everything else is built on. A weak brief produces a show that is technically correct and emotionally incoherent.

---

## The Show Folder

Every show gets its own folder at `src/main/resources/shows/[show_id]/`. This is where all production team communication and decisions live alongside the show itself.

```
shows/[show_id]/
├── [show_id].yml       ← the show YAML (plugin-loadable after scanner update)
├── brief.md            ← Show brief (entry point; everyone reads this first)
├── run-sheet.md        ← In-game test guide, revised each round
├── direction/          ← Show Director's working files (owned by Show Director)
│   ├── show-direction.md    ← Non-negotiables, watch-for, elevated departments
│   ├── tone.md              ← Tone phrase elaboration per department (optional; write when needed)
│   ├── intake.md            ← Default intake record: dept questions + initial answers
│   └── revision-log.md      ← Running debrief log, one entry per revision cycle
└── departments/
    ├── casting.md
    ├── wardrobe.md
    ├── choreography.md
    ├── set.md
    ├── camera.md
    ├── lighting.md
    ├── sound.md
    ├── voice.md
    └── stage-manager.md
```

The `direction/` folder is the Show Director's working space — the same relationship to the show folder that any other department's subfolder has to the production. The Director is not above the folder structure; they have their own corner of it.

`brief.md` stays at the show root because it is the entry point everyone reads first. It is the Director's document, but it belongs to the whole production.

**How to start a new show folder:**

1. Copy the scaffold: `cp -r src/main/resources/shows/_template/ src/main/resources/shows/[show_id]/`
2. Rename the template YAML placeholder to `[show_id].yml`
3. Fill in `brief.md` with the show brief before any YAML authoring begins
4. Fill in `direction/show-direction.md` with the Show Direction (non-negotiables, watch-for, elevated depts)
5. Fill in `direction/intake.md` using the Standing Department Asks (below) — one section per department
6. Fill in each `departments/[dept].md` with the briefing and initial department decisions
7. Add the run sheet to `run-sheet.md` after the first authoring pass

**Keeping direction/ files current:**
`direction/show-direction.md` should be updated any time the arc changes significantly — before re-briefing departments, not after. `direction/revision-log.md` gets a new entry after every in-game test. `direction/intake.md` is a record of the initial intake; it doesn't change after authoring begins (it documents what was decided before the show was written).

**On the plugin scanner:** The plugin currently reads flat `shows/*.yml` files. Until the scanner is updated (see `ops-inbox.md`), keep the YAML accessible as a flat file. The folder serves as production team documentation now; it becomes the canonical plugin path after the scanner ships. See `docs/show-import-process.md` for migration guidance.

---

## Show Brief Template

Use this structure to write the brief for every show. Adapt the length — a 2-minute ambient show needs a shorter brief than a 10-minute narrative arc. But every field should have an answer, even if it's one sentence.

```
SHOW BRIEF
----------
Title (working): [working title or id slug]
Duration (target): [in ticks and approximate minutes]
Context: [when/where/why is this show played — celebration, arrival, demo, narrative, etc.]

THE ARC
What is the emotional journey from first tick to last?
[2–4 sentences describing the shape of the experience: where it starts, what it builds toward,
what the peak is, and where it lands. Not a list of scenes — a feeling in motion.]

THE PLAYER
Who is the player in this show?
[One sentence. Examples: "A witness to a world that existed before them."
"The subject of a ceremony they didn't ask for." "A participant in communal joy."]

TONE
What is the emotional register?
[2–3 words or a short phrase. Not genre. Examples: "Tender and a little strange."
"Overwhelming and earned." "Quiet devastation." "Uncomplicated joy with a shadow."]

WHAT THEY CARRY AWAY
The one thing the player should feel or understand after this show ends.
[One sentence. This is the test every department decision gets measured against.]

CONSTRAINTS
Known limits for this show:
- Duration: [hard limit if any]
- Setting: [world, space, ceiling height, outdoor/indoor — if known]
- Cue library: [which cue families are in scope]
- Technical: [any known gaps relevant to this show]
- Alan notes: [anything Alan said explicitly in the concept request]
```

---

## Department Briefing Templates

After the brief is written, each department gets a briefing. Below is the standard briefing format for each department. Fill in only what's relevant — if a department is not active in this show, say so explicitly.

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
Is the player moving through this show, or stationary?
Are performers moving? What is the spatial story?
Key movement beats — when and why does each cross happen?
Are flight / levitation / velocity effects in scope?
```

### Set Director briefing
```
SET BRIEF
Where does this show happen? (world, location, space type)
Are sets (teleportation) used, or is the show portable?
What marks are needed? (standard 9-grid, or custom layout?)
Are any block modifications required?
```

### Effects Director briefing (Camera specialty)
```
EFFECTS / CAMERA BRIEF
What is the camera mode for this show? (full control / partial / player-free)
Are there cinematic sequences (PLAYER_SPECTATE)?
Key orientation beats — when should the player's view be directed?
Are there levitation or flight sequences? What is the altitude arc?
Key perceptual moments — night_vision, darkness, blackout transitions?
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

### Sprite Voice Director briefing
```
VOICE BRIEF
What is the text arc? (mostly silent / dialogue-heavy / single-word punctuation)
Which text modes are used in this show? (chat / title / action_bar / bossbar)
Key text moments — the lines that matter most.
Is the player addressed as "you"? Or is this ambient narration?
```

### Stage Manager briefing
```
STAGE MANAGER BRIEF
Are there any block modifications in scope? (triggers cleanup protocol)
Are there any world-resident entities being captured?
Are there any REDSTONE or time/weather changes that need restoring?
Any COMMAND escape hatches planned?
What is the run sheet format required?
```

---

## Standing Department Asks — Default Intake

These are the Director's standing questions for each department at the start of every show. They are templates — adapt phrasing for the show's context, but all questions should have answers in `direction/intake.md` before authoring begins.

The intake record is a snapshot of what each department committed to before writing began. It is not updated after authoring starts — that's what `departments/[dept].md` is for.

---

### Casting — Intake
- What performers (if any) appear? Name (role name, not type name), entity type, and function in the arc.
- Are they puppet (controlled), performer (native AI), or do they transition between states?
- Are world-resident entities being involved or avoided?
- Does any performer need to be on stage before the show starts?
- *If no performers:* Confirm. A show without performers is complete — state this explicitly so the department doesn't spend authoring time on aspirational casting.

### Wardrobe — Intake
- What is the player's appearance during this show? (default / armored / invisible-body technique / other)
- What do performers wear, and what does each choice communicate?
- Are any props or visual objects (Armor Stands, item displays) in scope?
- Are appearance transitions planned? When and why?

### Choreography — Intake
- Is the player stationary, moving through the world, or aloft during this show?
- What is the player's altitude arc? (ground / hover / graduated lift / full aerial / mixed) — sketch the shape if known.
- Are performers moving independently of the player? Key movement beats?
- Are flight/levitation effects in scope? If yes, which calibrated patterns apply?

### Set — Intake
- Where does this show take place? (world, rough coordinates, space type)
- Is the show portable (anchor-based) or location-specific?
- What marks are needed — standard 9-grid, custom layout, or none?
- Are any block modifications in scope?
- What is the spatial container? (outdoor open sky, arena, cave, underwater, elevated platform)

### Camera — Intake
- What is the camera philosophy for this show? (Director-directed / player-free / cinematic sequences / mixed)
- Is the player's view oriented at the start?
- Are there key beats where the Director needs the player looking at something specific?
- Any PLAYER_SPECTATE or cinematic transitions planned?

### Lighting — Intake
- What is the environmental state at first tick? (time of day, weather, particle atmosphere)
- Does the show move through distinct atmospheric zones? Name them.
- What are the key lighting events — the environmental shifts that serve the arc?
- What is the state at show end — does it restore to pre-show, or is the final state intentional?

### Sound — Intake
- What is the audio state at opening? (ambient bed running / silence / player's world audio unchanged)
- What are the key hit moments — sounds that punctuate visual or movement beats?
- Are looping ambient layers in scope? When do they start and stop?
- What is the audio state at show end? Does it need explicit cleanup?

### Voice — Intake
- How present is Sprite in this show? (dialogue-heavy / sparse punctuation / mostly silent)
- Which text modes are in scope? (CHAT / TITLE / ACTION_BAR / BOSSBAR)
- Is the player addressed directly ("you") or is this ambient narration?
- Where does Sprite speak, and where does Sprite go quiet — what is the silence doing?

### Stage Manager — Intake
- Are block modifications in scope? (triggers full cleanup protocol)
- Are world-resident entities being captured or modified?
- Are any world-state changes (time, weather, game rules) planned that need restoring?
- Any known capability gaps that affect this show's design? Name them now, before authoring begins.
- What must always be true if the player calls `/show stop` mid-show?

---

## Standing Department Asks — Revision Accountability

After each in-game test, the Director asks each active department for a debrief. Not every department is active in every revision — only departments whose domain was exercised that run need to report.

Department debriefs are written into `departments/[dept].md` under a `## Revision N (vX.Y.Z)` header. The Director reads all department debriefs and writes their own synthesis into `direction/revision-log.md`.

---

### Per-Department Debrief (asked after every revision where the department's work was in play)

1. **What worked** — What in your domain landed as intended?
2. **What didn't land** — What needs adjustment, and why? (Experiential first: "it felt too early / too loud / too present." Mechanical second: "the timing was off by ~10 ticks.")
3. **Capability gap?** — Did a plugin limitation affect your section? If yes, flag to Stage Management.
4. **Director question** — Is there anything you need direction on before the next revision? Don't work around a conflict without surfacing it.
5. **Next revision plan** — What specifically will you change, and what is the intended effect?

---

### Director's Synthesis (written into direction/revision-log.md after each debrief round)

```
## Revision N (vX.Y.Z — YYYY-MM-DD)

### Overall read
[1–3 sentences: is the show closer to or further from the brief after this run?
What is the primary thing that changed?]

### What's working
[Department contributions that are landing — name them, don't just say "good overall"]

### What isn't working
[Be specific. "The altitude arc isn't earned" is more useful than "needs work."]

### Show Direction update
[Has any non-negotiable changed? Has a new watch-for emerged?
Write "No change" if the direction holds.]

### Priority for Revision N+1
[One sentence. The single most important thing the next revision must address.]

### Department notes
[Any specific redirects, approvals, or escalations from the debrief round]
```

---

The revision log is the Director's running record of the production's current state. It is the document that answers the question: "Where did we leave off, and why does the show look the way it does right now?"

---

## Arc Evaluation Questions

Use these before sending a show to in-game test. Read through the full show YAML and ask each question.

**The brief test:**
- Does the opening tick establish the tone from the brief?
- Does the arc move from the starting emotional state toward the "carry away" statement?
- Is the peak earned — or does the show arrive at its biggest moment before the player is ready?
- Does the ending land, or does it just stop?

**The player test:**
- At every major beat: where is the player's attention? Is it where the show needs it to be?
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
- Voice: Is every word earned? Is every silence intentional?
- Stage Manager: Is this show safe to interrupt at any tick?

**The reduction test:**
If you removed one department's work entirely, would the show still function? If yes, that department may be underutilized. If the show collapses without every department's contribution, that's the target.

---

## Cross-Department Conflict Protocol

When two departments need the same moment to do different things, the Show Director resolves it by asking: **which choice serves the arc?** Not which choice is technically easier, and not which choice the individual department head prefers.

Common conflict patterns and resolution approaches:

**Camera vs. Choreography** — player position and player orientation conflict.
Ask: which is doing more work for the arc right now? If the visual (what they see) is the beat, Camera wins. If the spatial (where they are in relation to the performers) is the beat, Choreography wins.

**Lighting vs. Voice** — darkness and legible text conflict.
Ask: what is the player supposed to feel in this moment? If it's isolation or dread, let the darkness win and find text that works in dark (short, white, high-contrast). If the text is the beat, adjust the lighting (ACTION_BAR reads better in darkness than chat; TITLE is the most visible in all conditions).

**Sound vs. Voice** — a sound hit and a chat message land at the same tick.
Ask: is the sound announcing the text, or competing with it? If announcing: simultaneous is correct. If competing: offset by 5–10 ticks so the player hears the sound, then reads the message.

**Choreography vs. Stage Manager** — a movement choice creates a stop-safety risk.
Stage Manager has veto authority here. If a cross or block modification cannot be safely undone on interruption, it must be redesigned, not just documented.

---

## Pre-Flight Checklist

Run this before telling Alan to build and deploy.

```
□ Brief test passed (arc, player, departments)
□ All spawned entities have names appropriate to the story (not just type names)
□ Version bumped in build.gradle.kts per versioning policy
□ Run sheet written: one entry per major cue/section, with intention, function, watch question
□ Run sheet has Environment Notes (space, ceiling, ambient light)
□ Stage Manager rehearsal safety checklist reviewed
□ Any COMMAND block modifications: cleanup confirmed and tested
□ Show is safe to run with /show play [id] [player]
```

---

## Revision Cycle — When to Review

Not every revision requires the full production team. Use judgment:

**Full production review** — convene all departments, Director opens and closes:
- Major revisions (significant scope change, new act, structural rewrite of the arc)
- Feature enhancements that add new departments or new tools
- Any revision where the Show Direction itself may need updating
- First review of a new show before its initial in-game test
- Alan requests it

**Director's check** — lightweight arc pass without full department reports:
- Moderate revisions (2–3 departments affected, arc unchanged)
- A revision cycle where the brief is stable but specific elements are being tuned
- When a department flags a conflict that needs Director input to resolve

**Within-department only** — no Director involvement required:
- Minor adjustments within a single department's scope
- Bug fixes, timing tweaks, YAML corrections
- Changes that don't affect what the player experiences

**Escalation trigger:** Any within-department change that unexpectedly affects another department's work should be escalated to a Director's check before the revision is built. The department that made the change is responsible for flagging this.

---

## Show Authoring — Core Principles

- Start every new show with a brief, even when the concept is simple. A brief is not overhead — it is the thing that separates a show that works from a show that demonstrates features.
- Hold the "carry away" statement in mind throughout authoring. Every authoring decision gets tested against it.
- When two departments' choices conflict, decide — don't defer both into the YAML and hope they'll work together. They won't.
- The Show Director is allowed to tell a department that their contribution is not needed in this show. A show without performers is not incomplete. A show without music is not incomplete. What's incomplete is a show where the arc isn't clear.
- The Show Direction section of the brief is the Director's communication to all departments simultaneously. Keep it current. If the arc changes, the Show Direction changes first — before any department is re-briefed.
