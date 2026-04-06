---
show: SHOW_ID
status: draft          # draft | briefed | in-production | in-test | complete
version: 1.0
created: YYYY-MM-DD
director: Show Director   # creative director — always Claude acting as Show Director
builder: BUILDER_NAME     # primary in-game builder/tester (e.g., Zara, Smitty)
---

# Show Brief — SHOW_NAME

> Written by the Show Director before any YAML is authored.
> This document is the creative foundation every department works from.
> Once briefed, each department records their response in `departments/[dept].md`.

---

## The Arc

What is the emotional journey from first tick to last?

*[2–4 sentences describing the shape of the experience: where it starts, what it builds toward,
what the peak is, and where it lands. Not a list of scenes — a feeling in motion.]*

---

## The Player

Who is the player in this show?

*[One sentence. Examples: "A witness to a world that existed before them."
"The subject of a ceremony they didn't ask for." "A participant in communal joy."]*

---

## Tone

What is the emotional register?

*[2–3 words or a short phrase. Not genre. Examples: "Tender and a little strange."
"Overwhelming and earned." "Quiet devastation." "Uncomplicated joy with a shadow."]*

---

## What They Carry Away

The one thing the player should feel or understand after this show ends.

*[One sentence. This is the test every department decision gets measured against.]*

---

## Show Direction

The Director's statement of show-critical priorities — the load-bearing elements every
department decision must support. Not a wish list: these are the things that must work
for the show to achieve its purpose.

**Non-negotiables:**
*[2–4 items. Each is a specific, testable requirement: "The C7 lift must feel earned, not
mechanical." "Sound must be absent at the open so the first audio hit lands." These are
communicated to departments as mandatory, not optional.]*

**Watch for:**
*[1–3 known risks or cross-department tension points the Director is tracking. Things like:
"Lighting and Voice will compete at C4 — darkness vs. legibility. Resolve before YAML."
"Choreography and Camera share the C7 moment — agree on what the player is looking at."]*

**Departments with elevated priority this show:**
*[Name which departments carry the most weight in this show's arc — and briefly why.
Example: "Sound and Voice carry the emotional arc here — Casting and Wardrobe are minimal."
This helps departments calibrate their investment.]*

---

## Constraints

- **Duration:** [target tick count / approximate minutes]
- **Setting:** [world, space description, ceiling height, outdoor/indoor]
- **Cue library:** [which families are in scope — atmos.*, mood.*, ramp.*, etc.]
- **Technical:** [any known gaps relevant to this show]
- **Alan notes:** [anything Alan said explicitly in the concept request]

---

## Calibration Priorities

> What does this show give each department to practice? Name one pattern from the calibration
> backlog per department. See `kb/departments/show-director/show-director.dept-capabilities-briefing.md` for the full
> backlog per department.

| Department | Calibration Goal This Show |
|---|---|
| Lighting | *[e.g., `atmosphere.hold.midnight` — sustained dark section]* |
| Sound | *[e.g., `bed.dark` + `button.arrival`]* |
| Choreography | *[e.g., `sequence.arrive.hold` — one performer, full section hold]* |
| Effects | *[e.g., hover texture variation — sustained aerial with section distinction]* |
| Camera | *[e.g., pre-lift orientation timing — tick offset test]* |
| Fireworks | *[e.g., `arrangement.punctuation` — single close-range rocket]* |
| Wardrobe | *[e.g., `costume.ethereal` — invisible-body technique]* |
| Set | *[e.g., `space.stage.marked` — confirm 9-mark grid]* |
| Voice | *[e.g., timing mode test — specify before/with/after/silent per major beat]* |
| Casting | *[e.g., `figure.companion` — Wolf or Allay in companion register]* |

---

## Voice Timing Directions

> For every major physical beat, specify the timing relationship. ShowSprite does not default
> to a mode — this is a Director call. See `kb/departments/show-director/show-director.dept-capabilities-briefing.md
> §Voice — calibration priorities` for mode definitions.

| Beat | Timing Mode | Notes |
|---|---|---|
| *[e.g., player lift at C7]* | *[before / with / after / silent]* | *[optional note]* |

---

## Department Briefings

Brief statements to each department head. Each department records their response and decisions in `departments/[dept].md`.

**Casting:** *[Who is on stage, if anyone. Or: "No performers in this show." Flag any AI safety risks (Phantom, Warden, Creeper, Elder Guardian).]*

**Wardrobe:** *[Appearance direction, if applicable. Or: "No wardrobe work in this show." Note if visual read depends on gapped variant/profession fields.]*

**Choreography:** *[Movement brief — player movement, performer movement, flight/aerial scope. Specify puppet vs. performer intent per entity.]*

**Set:** *[Where this happens, what marks are needed, any spatial constraints. P1 scouting required before other departments begin — Environment Notes are the gate.]*

**Camera:** *[Camera mode: full control / partial / player-free. Key orientation beats. List any beats requiring a camera assertion.]*

**Lighting:** *[Opening light state, arc, key lighting moments, closing state. Note weather changes — Sound must plan around rain bed.]*

**Sound:** *[Opening audio state, key sound moments, looping layers, closing audio state. Confirm any weather changes filed with Sound.]*

**Voice:** *[Text arc — mostly silent / dialogue-heavy / single-word punctuation. Key lines. Timing modes specified in the Voice Timing Directions table above.]*

**Stage Manager:** *[Block modifications? World-resident entities? COMMAND use? REDSTONE? Any stop-safety concerns? AI safety risks confirmed above?]*
