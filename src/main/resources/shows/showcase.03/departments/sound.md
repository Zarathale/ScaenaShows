---
show_id: showcase.03
department: Sound Designer
document: Department Brief
updated: 2026-03-26
---

# Sound — showcase.03 "Welcome"

## What This Department Serves

Sound's role across this show's four sections: a gathering, a door opening, a full room,
and what remains when the room quiets.

The tone brief says: "The Build's audio should feel like a gathering — not a buildup of
volume, but a buildup of presence. More things arriving in the soundscape, not the same
things getting louder. The Arrival's sound is the one that says 'yes' — whatever that means
in Gracie's vocabulary. The Celebration is full and real; the Coda should be what remains
when you turn the music off but can still almost hear it."

---

## Per-Section Direction

### Build — "The Overture"
Begin with the ambient bed of the Welcome Stage's environment — whatever biome or architectural
setting Set has created. This is the baseline: the sound of a place before anything happens.

Then: more things arrive. Not louder — more present. Layer in sounds that feel like preparation,
like presence, like something gathering without announcing itself. Options:

- A soft note block motif that begins quiet and is joined by a second instrument partway through
- Distant animal ambient sounds that gradually become closer (not closer literally, but more
  present in the mix)
- The sound equivalent of a room filling up — not a crescendo but a density increase

The Build's audio arc ends at the Arrival threshold — wherever Gracie takes it, the Arrival
sound must feel like the resolution of what was building.

### Arrival — "The Threshold"
The sound that says "yes."

This is Sound's most specific creative decision for this show. The direction gives latitude:
"whatever that means in Gracie's vocabulary." But it must be singular, recognizable, and right.

Options in register:
- A chord — multiple note block instruments landing simultaneously, warm and full
- A door-opening sound as literal metaphor: `block.wooden_door.open` at elevated pitch
- An experiential sound — `entity.player.levelup` (too literal) or something in the same
  register but not quite that (explore the sound library)
- A held ambient tone that arrives and sustains — like a room pressure changing when a door opens

The constraint: not a fanfare. A fanfare is a performance event. This sound is a physical event
— the sound of a door that was always going to open, opening. It happens; it doesn't announce.

This sound fires simultaneously with the convergence tick. It is one of six simultaneous
department events at Arrival. It must add to the moment, not muffle it.

### Celebration — "The Occasion"
Sound is at its fullest here. What does fullness mean?

In the Celebration, Sound is not constrained to ambient texture. This is the one section where
a musical or rhythmic layer could be appropriate — not incidental music (that would feel like
a score) but a sound-world that expresses joy physically. Options:

- Note block ensemble at celebratory pitch and tempo (coordinate with any music-director
  capacity in Sound's KB)
- A layered bed of sounds that together feel like a celebration without being a pop song
- Natural sounds of celebration: bells, chimes, something that sounds like joy without
  narrative

Full means: all the layers present, the soundscape at its most populated. Not loud in isolation
— full in aggregate.

### Coda — "The After"
Sound opens into the Coda before Mira's last firework. The transition is Sound's responsibility.

The Coda's sound is what remains. This does not mean silence — it means the sound of a place
that has just had something wonderful happen in it, and is now returning to itself. The ambient
bed is still present, but the celebratory layers have withdrawn.

Sprite's Coda line fires 60–120 ticks after the final firework. Sound gives Sprite that
silence — no new SOUND events in the 60 ticks before the Coda line. After the Coda line:
the ambient bed holds, and the show ends in the world's natural sound.

---

## Show-Level Constraints

- The Arrival sound fires simultaneously with the convergence. Coordinate with Stage Management
  on the exact Arrival tick.
- Sound begins the Coda transition before Mira's last firework. Coordinate with Mira on the
  approximate tick of her final burst.
- No new SOUND events within 60 ticks of Sprite's Coda line.
- The Celebration's fullness should feel natural, not mechanically layered. Each sound layer
  in the Celebration should have a reason to be there.

---

## Intake Questions for Sound

1. **Build soundscape layers:** What arrives during the Build? Name each layer and its
   approximate introduction tick.
2. **The Arrival sound:** What is it? One specific choice with reasoning. Director reviews.
3. **Celebration layers:** What layers constitute "full"? What instruments/sounds?
4. **Coda transition tick:** When does Sound begin opening into the Coda? (Coordinate with
   Mira on the final firework tick.)
5. **Coda ambient bed:** After the Celebration layers withdraw, what remains?

---

## Decisions
*The Arrival sound is Director-reviewed before YAML is written.*

## Revision Notes
*Added after each in-game test.*
