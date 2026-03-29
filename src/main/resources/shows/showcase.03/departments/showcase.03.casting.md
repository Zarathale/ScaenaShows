---
show_id: showcase.03
department: Casting Director
document: Department Brief
updated: 2026-03-26
---

# Casting — showcase.03 "Welcome"

## What This Department Serves

Casting chooses the welcoming committee. This decision is the show's second-most important
image, after the Arrival convergence moment itself. Who is waiting for the player when they
walk through the threshold tells them what kind of world they're entering.

The show direction says: "The entities Casting chooses should feel like residents — creatures
or figures that belong here, that are glad to have you. Not an impressive ensemble. A genuine one."

This is a calibration show for how Casting thinks about this question. The answer here will
inform how future welcome sequences are cast. Choose as if you're setting a precedent.

---

## The Welcoming Committee

### Size
3–5 entities. The show direction specifies this range. Fewer than 3 feels sparse; more than 5
risks reading as a crowd rather than a gathering. The middle of this range (4) is probably right.

### Species selection — the principle
The show direction says "residents" — creatures that belong on this server, in this world,
at this location. They are glad to have you. They are not impressive performers; they are
the people who live here.

Consider: which creatures would actually be at home near the Welcome Stage's location (after
Set confirms the environment)? Which creatures, gathered together, read as "this is the
community that is welcoming you" rather than "these are interesting animals"?

**Candidates to consider:**
- **Allays:** One or two Allays in a welcoming committee reads as pure joy and goodwill.
  They hover; they're glad. A natural welcoming figure.
- **Cats:** Specific, opinionated, and the fact that they stayed says something. Two Cats
  in a welcoming committee says "we chose to be here."
- **Wolves (tamed appearance, AI off — sitting):** Devoted, patient. A sitting Wolf looks
  like it has been there waiting.
- **Villagers:** If the Welcome Stage has any architectural quality, Villagers add a human-
  adjacent register that reads as community.
- **Parrots:** Colorful, vocal, celebratory in an organic way. A Parrot at the Celebration
  might generate its own audio texture.

The committee should not be uniform — all the same species reads as a battalion, not a gathering.
Mix species intentionally. A pairing of "joyful and small" (Allay) with "devoted and grounded"
(Wolf) with "independent and present" (Cat) covers different emotional registers of welcome.

### Staging position
Committee members are staged before the player arrives. They are waiting, not assembling.
This distinction is everything — see show direction Non-Negotiable #4.

Staging area: inside the welcome area (past the threshold), facing the approach axis (toward
where the player will walk from). They are looking toward the approach. They see the player
coming before the player reaches the threshold.

Precise positions determined by Choreography after Set confirms the welcome area coordinates.

### AI state
Committee members during the Build: performer state (AI on) or puppet state (AI off)?

**Case for performer state during Build:** Genuine wandering, looking around, the natural
quality of creatures who are present and not directed. This is the most honest version of
"they're waiting." The risk: they wander out of the welcome area's frame before the Arrival.

**Case for puppet state during Build:** They stay in their positions. They are composed,
present, legible from the approach. The risk: they look staged.

**Hybrid option:** Puppet state during Build, release to performer state at or after Arrival.
The committee is composed when the player is approaching (legible) and then genuinely alive
when the player arrives (responsive). This requires two ENTITY_AI events per entity.

Bring a recommendation to intake. Choreography and the Director need to know.

---

## Show-Level Constraints

- **Committee is staged before the player arrives.** This is a hard non-negotiable.
  SPAWN_ENTITY events for committee members fire before the show's initial player teleport,
  or at minimum before the player can see the welcome area. Stage Management tracks this.
- Species choices should feel like residents of the server world, not like the most visually
  impressive options. Impressive ≠ genuine.
- Committee stays through the Celebration. They are present for the Coda — Casting and
  Choreography decide together whether they remain in the Coda or begin to withdraw. That
  decision says something about what welcome means: if they stay, welcome is permanent;
  if they drift away, welcome was an act, and now the player is on their own (in a good way).
- `despawn_on_end: true` or explicit management. Committee members don't persist as world
  pollution after the show ends.

---

## Intake Questions for Casting

1. **Committee composition:** What 3–5 species? Why these? What emotional register does
   each contribute to the gathering?
2. **AI state during Build:** Performer, puppet, or hybrid? Reasoning?
3. **Coda presence:** Do committee members stay through the Coda, or begin to withdraw?
   Casting decides with Choreography.

---

## Decisions
*Filled at intake — after Set confirms Welcome Stage location and character.*

## Revision Notes
*Added after each in-game test.*
