---
show_id: showcase.03
department: Choreography Director
document: Department Brief
updated: 2026-03-26
---

# Choreography — showcase.03 "Welcome"

## What This Department Serves

Choreography is an elevated department for this show. The welcoming committee's positions and
movement during the Celebration are the show's second-most important image.

The show direction says: "Choreography's welcoming committee movement during the Celebration
should feel like people expressing joy, not performers executing choreography. If it looks like
a routine, it has crossed into production rather than feeling. The question: *what do people
actually do when they're glad someone is here?* Move toward them. Gather. Maybe not in
perfectly coordinated ways."

This means Choreography's job is to set up a dynamic that AI can resolve — not to script
every position. The initial staging is precise; the Celebration movement is released.

---

## The Committee Staging

### Before the player arrives
The welcoming committee is staged inside the welcome area, facing the approach axis (toward
where the player will walk from). They are waiting. They see the player coming.

**Staging configuration:**
- Spread loosely across the welcome area — not a line, not a cluster
- Each entity at a distinct position, creating a spatial arrangement that reads as "gathered"
  rather than "arranged"
- All facing the approach axis (the direction the player is walking from)
- Enough space between entities that each is individually visible from the approach

**Specific positions:** Determined by Choreography after Set confirms the welcome area's
geometry. Rule of thumb: spread across 60–70% of the welcome area's width, staggered at
varying depths (some closer to the threshold, some further back). This creates a sense of
depth in the welcome party.

### Initial AI state
Per Casting's recommendation (puppet or performer state during Build). Choreography executes
whatever Casting decides.

If puppet state: all entities are `ENTITY_AI: false` at staging. They hold position through
the approach.

If performer state: entities are placed with AI on. Choreography accepts that they may shift
slightly from initial positions before Arrival.

If hybrid: puppet state at staging; ENTITY_AI events fire at or just after Arrival to release
them. Choreography tracks the timing of these release events with Stage Management.

---

## The Celebration

The show direction says Celebration movement should feel like joy expressed genuinely —
"move toward them, gather, maybe not in perfectly coordinated ways."

**If AI is on (performer or hybrid):** After the AI release at Arrival, let the entities do
what they do. AI-on Allays will hover and drift toward the player. Wolves will look, wander,
possibly follow. Cats will stay put or move unpredictably. This authenticity is the point.
Choreography does not script the Celebration's movement.

**If AI stays off through Celebration:** Choreography may author a small number of
MOVE_ENTITY events — not choreography, but impulses. "Drift slightly closer to the player
over 400 ticks." "Two entities move toward each other." The movement should feel uneven,
not synchronized.

The question is: do you want the genuinely unpredictable (AI on) or a carefully authored
approximation of genuine (MOVE_ENTITY with AI off)? Bring a recommendation to intake.

---

## The Coda

Casting and Choreography decide together: do committee members stay through the Coda,
or do some withdraw?

**Option A — Stay:** The committee remains present through the Coda and the show's end.
The player is left in the world with the community that welcomed them. Welcome is permanent.

**Option B — Gradual withdrawal:** One or two entities drift away (AI-on) or despawn
quietly during the Coda. The player ends the show in a quieter version of the welcome area.
The community has celebrated and returned to their lives. Welcome is an act, not a state.

**Option C — Hold, then release:** Committee holds through Sprite's Coda line, then disperses.
The line is the committee's exit cue.

Discuss at intake with Casting. The Director reviews the decision.

---

## Show-Level Constraints

- **Committee is staged before the player's arrival.** Stage Management tracks that
  SPAWN_ENTITY events for all committee members fire before the initial player teleport
  to the approach start.
- If hybrid AI (puppet → release at Arrival), the release events fire at the Arrival tick
  or within 5 ticks after. They are part of the convergence.
- No scripted synchronized Celebration movement. The joy should look like joy, not like
  a show that was designed.
- All entities managed cleanly at show end (`despawn_on_end: true` or explicit despawn).

---

## Intake Questions for Choreography

1. **Committee positions:** After Set confirms welcome area geometry — where exactly does
   each entity start? (Choreography maps this at intake with Set's coordinates.)
2. **Celebration movement:** AI release (genuine unpredictable) or MOVE_ENTITY approximation?
3. **Coda decision:** Option A (stay), B (gradual withdrawal), or C (hold then release)?
   Decide with Casting; present to Director.
4. **AI release timing (if hybrid):** Exact tick of ENTITY_AI events at Arrival?
   (Coordinate with Stage Management.)

---

## Decisions
*Committee positions filled after Set confirms welcome area. Coda decision made with Casting.*

## Revision Notes
*Added after each in-game test.*
