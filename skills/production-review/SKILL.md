---
name: production-review
description: >
  Production team review of a ScaenaShows show file. Use this skill whenever the user asks
  for a review, critique, notes, or feedback on a show YAML — before a major revision, after
  scope changes, before a first in-game test, or when Alan says "run this through the
  production team", "give me production notes", "what could be better", "is this ready to
  test". Convenes the Show Director and all nine department heads including Kendra (Stage
  Manager). The Director charges each department; departments respond with honest assessments
  and recommendations; Kendra adds a structural read (prompt book conventions, beat collision
  check, tech check sign-off); the Director closes with a synthesis, ranked path forward, and
  a ready-to-paste revision log entry. Output is a standalone review artifact — identifies
  problems and priorities; does not implement changes.
---

# ScaenaShows — Production Team Review

This skill runs a full production team review of an existing show file. The Show Director reads
the show, articulates a direction, and charges each department to respond. Every department head
then brings their honest assessment — specific, buildable, grounded in the actual YAML. The
Director closes by synthesizing what the team surfaced into a unified path forward.

The goal is not to validate the show. The goal is to make it better than any one department
could make it alone.

---

## Before you begin

Read these files. They are not optional:

1. `kb/production-team.md` — all ten roles (Show Director + 9 departments), their domains,
   their tools, their questions. This is the common brain.

2. `kb/departments/show-director.kb.md` — the Director's brief templates, arc evaluation
   questions, cross-department conflict protocol, and pre-flight checklist.

3. `kb/departments/voice/showsprite.context.md` — the voice layer. The Sprite Voice Director draws from here.

4. The **show YAML file** the user has specified (in `src/main/resources/shows/`).

5. Any **cue files** the show references (in `src/main/resources/cues/`). Read the cues the
   show actually uses — the review must be specific to the actual content, not generic.

6. `kb/cue-library-survey.md` — the current cue inventory. Departments can recommend cues
   by name from the library, or flag gaps where new cues would help.

7. If the show has a folder at `src/main/resources/shows/[show_id]/`, read these in order:
   - `brief.md` — the entry point. The Show Direction section (non-negotiables, watch-for
     risks, elevated departments) is the Director's prior statement of what must be true.
   - `direction/show-direction.md` — the current Show Direction; check whether the non-
     negotiables are being met, watch-for risks are still active, and elevated departments
     have delivered.
   - `direction/intake.md` — what each department committed to before authoring began.
     Use this to evaluate whether departments delivered on their intake promises.
   - `direction/revision-log.md` — the running history of the production. Understand where
     the show has been before evaluating where it is now.
   - Any `departments/*.md` files that exist — the review builds on prior decisions.

   If no brief or direction files exist, the Director's opening must write a brief and Show
   Direction before issuing department charges — the departments need a direction to respond to.

8. `kb/departments/stage-manager.kb.md` — Kendra's KB. Her section of the review includes
   a structural read of the YAML (prompt book conventions, beat collision check). Know what
   she's looking for before writing her section.

Do not begin writing the review until you have read all of the above.

---

## What a production team review is

The Show Director opens the meeting. They've read the show. They know what it's trying to be
and what it isn't yet. They give each department a charge — not instructions, but a frame:
here's the direction we're steering, here's what I'm asking of your department specifically.

Then each department head speaks in response to that charge. They don't just report on their
domain in isolation. They respond to the direction: what they see in the show right now, where
the show is and isn't aligned with the director's vision, and what they'd do to close that gap.

**Kendra (Stage Manager)** responds last among the departments. Her section includes her
creative/operational read *and* her structural read — the prompt book review that's
independent of tone or arc.

The Director closes by pulling the threads together — deciding, not just summarizing. What does
the team's collective input actually mean for the next revision? The Director's synthesis is
also the input to `direction/revision-log.md` — it's the record that the next session reads to
understand where the production is.

---

## Review format

Save the review as a markdown file in the user's workspace folder. Name it:
`[show-id].production-review.md` (e.g., `demo.archetype_sampler.production-review.md`)

Use this structure:

---

### Show Director — Opening

The Director speaks first. This section has three parts:

**Show read:**
The Director's honest read of the show as it currently exists. 3–5 sentences. What is this
show? What is its emotional arc? What is it trying to accomplish, and how close is it to
accomplishing that? This is the Director's assessment of the gap between what's on the page
and what the show wants to be. Not a summary — a judgment.

**Show Direction check:**
If `direction/show-direction.md` exists, evaluate it explicitly: do the non-negotiables still
hold? Are the watch-for risks still active? Have the elevated departments delivered on their
priority? Note any Show Direction items that are being met, and any that are at risk.
If intake records exist in `direction/intake.md`, check whether departments delivered on
their intake commitments — flag any significant departures.
If no brief or Show Direction exists, write one using the templates from show-director.kb.md
before issuing department charges — the departments need a direction to respond to.

**Direction:**
What is the Director steering toward for this show? State it clearly: the emotional
destination, the tone, the one thing the player should carry away. If the brief already
captures this well, confirm it. If it needs updating based on the current YAML, say so.

**Department charges:**
A brief charge to each of the nine departments — what is the Director specifically asking?
One or two sentences each. Not every department gets an equally complex charge — a show with
no performers warrants a short Casting charge. The charge should be precise enough that the
department head knows exactly what question they're being asked to answer.

---

### Department Reviews

One section per department, in this order: Casting Director, Wardrobe & Properties Director,
Choreographer / Movement Director, Set Director, Camera Director, Lighting & Atmosphere
Designer, Sound Designer, Sprite Voice Director, **Stage Manager (Kendra)**.

Each department section (except Stage Manager — see below) has four parts:

**Director's charge:**
Restate — in one sentence — what the Director asked of this department.

**Assessment:**
The department head's honest read of what currently exists. What is actually there? What is
working? What is absent or underdeveloped? Name specific moments by cue label or tick range
("at C3", "around T=240"). Credit what's working. Be direct about what isn't.

**Cross-department and WIP notes:**
What is this department seeing that involves another department's work, or that surfaces a
coordination need?

**Recommendations:**
1–4 specific suggestions. For each: what to change, why it serves the direction, where in
the show timeline, and a YAML sketch where the implementation is non-obvious.

---

### Stage Manager (Kendra) — Section

Kendra's section has two distinct parts:

**Creative/operational read** (standard department format):
Director's charge → Assessment → Cross-department notes → Recommendations

Kendra's assessment includes whether the show is safe to test right now: are all spawned
entities covered by `despawn_on_end: true`? Are all PLAYER_SPECTATE events paired? Are
looping sounds stopped? Are block modifications via COMMAND documented and cleaned up?
This is her cleanup contract review, not just her creative read.

**Structural read (Prompt Book Review):**
Kendra's structural pass is independent of tone, arc, and creative direction. She evaluates
the YAML as a call script:

- **Running order** — Does the show's timeline structure make sense? Are sections clearly
  delineated? Are REST events used for intentional breathing room or are pauses accidental?
- **Cue labels** — Are C1/C2/C3 YAML comments present for shows with more than ~5 sections?
  Are they accurate (do they match what actually fires)?
- **Cue naming** — Do any cue IDs in the show violate the `[category].[archetype].[variant]`
  convention? Are `CUE` type references using valid `cue_id:` values from the library?
- **Beat collisions** — Are there ticks where multiple departments are firing simultaneously
  in ways that create technical conflicts or a muddled player experience? If found: what are
  they, and what is Kendra's proposed resolution?
- **Revision continuity** — Does the current YAML match the run sheet? If revision notes
  exist in `direction/revision-log.md`, do the tick positions and cue numbers still align?

Kendra closes her section with a **pre-test sign-off**: "Structurally clear to test" or
"Hold — [specific issue must be resolved first]."

---

### Show Director — Synthesis

The Director closes. Three parts:

**What the team surfaced:**
2–3 sentences acknowledging the most important things the departments brought to the table.
This isn't praise — it's the Director demonstrating they heard the team.

**The path forward:**
The **3–5 highest-impact changes** for the next revision, ranked. For each:
- The change (specific enough to act on)
- Which department owns it, or which departments need to coordinate
- Why this one makes the list

This is editorial judgment, not a greatest-hits of every department's wishlist. Some
departments' suggestions will not make the priority list. That's the Director's call.

**Revision log entry:**
Write the Director's synthesis in the format used by `direction/revision-log.md`:

```markdown
## Revision N (vX.Y.Z — YYYY-MM-DD)

### Overall read
[1–3 sentences]

### What's working
[Named contributions]

### What isn't working
[Specific]

### Show Direction update
[Changes to non-negotiables or watch-for, or "No change"]

### Priority for Revision N+1
[One sentence]

### Department notes
[Specific redirects, approvals, escalations]
```

Tell the user: "This revision log entry is ready to paste into `direction/revision-log.md`
in the show folder."

**Reduction test:**
Apply the test from show-director.kb.md: if you removed one department's work entirely,
would the show still function? Call out any departments whose contribution is currently so
thin that the show wouldn't notice its absence. Name them honestly.

---

### Java Gap Flags

If any department identified something they want to do but can't because of a gap in the
Java control surface, list those gaps here. For each:
- What the department was trying to accomplish
- What's missing or broken
- Whether it's already filed in `ops-inbox.md` (reference the entry) or new (Kendra should
  file it after the review)

---

## Tone and voice

The review is written by the production team — the Director and nine department heads — not
by a tool. Write as people who care about the work. Direct, specific, willing to say "this
isn't working yet" when it isn't.

The Director's voice is decisive. The department heads' voices are expert and responsive.
Kendra's voice is operationally grounded — she has read every line of the YAML and she
notices what others miss. She's the person who says "the C9 fireworks are firing at T=480
but the levitation release doesn't begin until T=485 — the player is still ascending through
the burst window, which is correct, but the run sheet says they should be descending."

A section that says "The sound design in this show is absent — there is no audio arc"
is better than one that says "There may be opportunities to expand the audio landscape."

Every critique comes with a path forward.

---

## YAML sketch format

When proposing a YAML enhancement inline, use this format:

```yaml
# [Department] — [brief label]
# Suggested addition at ~T=240, after [existing event label]
- tick: 240
  type: SOUND
  sound_id: minecraft:ambient.cave
  category: ambient
  volume: 0.6
  pitch: 0.7
  audience: participants
```

Always include a comment line identifying the department and the intent. These sketches are
starting points, not final YAML — mark them clearly as proposed.

---

## Length and depth

A full review of a show like `demo.archetype_sampler` should be 2,000–3,500 words. Shorter
shows may be shorter. The goal is depth and specificity, not word count.

Do not pad sections where a department genuinely has little to say. A short, honest section
is better than a long inflated one. The Director's opening and synthesis carry the most weight.
Kendra's structural read is mandatory regardless of show length — it's the one section that
doesn't scale with show complexity.

---

## After saving the review

Tell the user where the file is (with a computer:// link) and give them a 2–4 sentence verbal
summary of the Director's synthesis — the path forward, not the full review. Do not recite
department sections in chat. Also remind the user that the revision log entry at the end of
the Director's synthesis is ready to paste into `direction/revision-log.md`.

This skill produces a review artifact only. It does not implement changes, update the show
YAML, or modify the show folder. If Alan wants to act on the review, that is a separate
authoring session.
