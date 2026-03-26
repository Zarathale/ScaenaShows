---
show_id: showcase.01
department: Sprite Voice Director
document: Department Brief
updated: 2026-03-26
---

# Voice — showcase.01 "The Cabinet"

## What This Department Serves

Sprite is the Allay's interpreter — not its ventriloquist. The Allay doesn't speak; Sprite
translates what the Allay's collecting behavior implies. The register for this show is field
notes: a naturalist describing a creature in its habitat, aware that the creature is slightly
absurd and completely serious about everything it does.

The tone brief says "field notes, not tour guide." The difference: a tour guide explains.
A field naturalist observes. Voice in this show observes — it notes, it wonders, it occasionally
loses its composure slightly at Vignette F and then gets it back.

The two-layer rule applies: BOSSBAR (section marker, sustained) + MESSAGE (Sprite's observations,
brief) is the standard pair. TITLE is reserved for moments that earn it — in this show, that
may be zero times, or it may be the Contraption reveal. Discuss at intake.

---

## Voice Architecture

### BOSSBAR — Section identity
A persistent bossbar gives each vignette its name and a progress fill. The show has six vignettes
plus the Home Base A-sections. BOSSBAR title options:

- A-sections: no bossbar (or a very quiet "— home —" bar at minimal fill, cleared on departure)
- Vignette B: "The Still Water" — fill progresses over the free exploration duration
- Vignette C: "The Theater" — fill progresses over the creature theater runtime
- Vignette D: "The Nether Valley" — fill progresses over the Strider observation period
- Vignette E: "The Suspension" — fill progresses over Mira's fireworks sequence
- Vignette F: "The Contraption" — fill progresses over the setup-to-reveal sequence

BOSSBAR color: consistent across the show (one color that feels right for the show's register).
The naturalist blue-green? The Allay's characteristic blue? Decide at intake and hold it.
BOSSBAR clears at the end of each vignette before the Home Base teleport.

### MESSAGE — Sprite's field notes
Sprite speaks in the A-sections (at Home Base) and at key moments in each vignette.

**At Home Base (A-sections):**
The Allay is present. Sprite observes it. Each A-section return can have a single message —
a new observation about the Allay, a notation about what was just seen, a brief wonder.

These lines are NOT recaps. Sprite does not say "that was remarkable" after every vignette.
It returns to the Home Base and notices something small: the Allay is holding something. The
Allay has moved slightly closer. The Allay has turned toward the direction of the last vignette.

Suggested cadence: one MESSAGE per A-section return, 2–3 lines maximum total across all
A-sections. The show is 7–8 minutes; Voice should not dominate it.

**At each vignette:**
One or two Sprite lines per vignette, maximum. The naturalist speaks briefly and then watches.

Voice line guidelines for this show:
- Short. Under 60 characters where possible. Sprite doesn't lecture.
- Observational, not explanatory. "It knows this water." Not "The Allay found this place."
- No punctuation at sentence ends in Sprite's voice — the period is a full stop that Sprite
  doesn't always need. ("It knows this water" not "It knows this water.")
- One line that notices. One line that wonders. Not both — usually just one.

**Vignette F (The Contraption):** Voice gets to be slightly less composed here. One line
before the reveal (the setup's absurdist potential), silence, the REDSTONE fires, then one
line after — if needed. The after-line should be the one that makes the whole thing land.
It might be nothing more than: "... yes." Or it might be the name of the creature that just
appeared, spoken with the gravity of a discovery.

---

## Specific Lines — Initial Draft Pool

These are candidates, not commitments. Voice refines at intake.

**Opening A-section (introducing the Allay):**
- "Something here has been busy."
- "The Allay has been making a survey."
- "It found things. It would like you to see them."

**Vignette B ("The Still Water") — arrival line:**
- "It comes here when the light does this."
- "There's something about water that the Allay keeps returning to."

**Vignette C ("The Theater") — observing the creature pair:**
- "They were here before we arrived."
- "This part is hard to explain."
- (silence is also valid — let the creatures' behavior be the only content)

**Vignette D ("The Nether Valley") — arriving in nether:**
- "The Strider is exactly where it should be."
- "Not everything here is dangerous. Some things are just — from here."

**Vignette E ("The Suspension") — during the lift:**
- "This is the part where the Allay shows off slightly."

**Vignette F ("The Contraption") — setup:**
- "It built something."
- "The Allay wanted you to see what it built."
  — (REDSTONE fires) —
- After-line: TBD at intake, after Set and Casting confirm what the contraption does and
  who the punchline creature is.

**A-section closes (show end, final Home Base):**
- "The survey is complete."
- "It will find more things. It always does."

---

## Show-Level Constraints

- Maximum two MESSAGE events per vignette section. Less is often more.
- TITLE reserved for exceptional moments — if used at all, probably Vignette E at the lift
  or Vignette F at the reveal. Not both.
- ACTION_BAR: optional ambient whisper on arrival at each vignette — a single word or phrase
  that fades. This layer sits below MESSAGE in register. Not required; discuss at intake.
- Sprite's voice is defined in `kb/departments/voice/showsprite.context.md`. Read it before
  writing any narration. The grammar rules, rhythm patterns, and what-to-avoid sections are
  all relevant to this show.
- All Sprite lines for A-sections are reviewed by the Director before YAML is written. All
  Vignette F comedy lines are reviewed by the Director — timing sensitivity requires it.

---

## Intake Questions for Voice

1. **BOSSBAR color:** What color fits this show's register? (Options per spec: pink, blue,
   red, green, yellow, purple, white.)
2. **A-section MESSAGE count:** How many A-section returns get a Sprite line? All five? Three?
   Or only the opening and closing? Establish the cadence at intake.
3. **Vignette F after-line:** This cannot be written until Set (contraption design) and Casting
   (punchline creature) are confirmed. Flag as pending; write last.
4. **TITLE usage:** Does any moment in this show earn a TITLE? Candidates: Vignette E lift,
   Vignette F reveal. Or none.
5. **ACTION_BAR:** Yes or no as a third layer? If yes, what tone — location names? Allay
   observations? Something else?

---

## Decisions
*Filled at intake.*

## Revision Notes
*Added after each in-game test.*
