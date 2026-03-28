---
department: Camera Director
owner: Mark
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Camera — Brief to Direction

> Mark's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for player orientation, cinematic perspective modes,
> and screen distortion lives in `kb/departments/camera/camera.kb.md`.

---

## Standing Requirements

*What Mark cannot start without. These come from the brief or the intake conversation; no Camera work begins without them.*

**Stage orientation — which compass direction is "front."** All of Mark's orientation calls are written relative to a consistent show front. If the player is meant to face north for most of the experience, Mark needs to know that before writing a single FACE event. A show with no declared front orientation forces Mark to guess, and he doesn't guess.

**Named marks defined by Set.** Camera uses marks as orientation targets (FACE `look_at: mark:center`, PLAYER_TELEPORT toward `mark:stage_left`, etc.). If the marks aren't defined, Mark can't point at them. Set's spatial structure needs to be in place before Camera authors anything that references a mark.

**Fireworks positions, burst altitudes, and timing.** A firework that fires while the player is facing the wrong direction is a firework the player doesn't see. Mark needs to know where bursts are happening and at what altitude so he can author orientation calls 5–10 ticks before each detonation. This information comes from Fireworks at the design stage, not from the run sheet.

**Any planned player position changes from Effects (PLAYER_TELEPORT, CROSS_TO).** Every time Effects moves the player's body, Camera has an opportunity — and responsibility — to set their orientation on arrival. Mark needs Effects' movement timeline upfront so he can plan the orientation reset at each landing point.

---

## Intake Questions

*Questions Mark asks at the intake meeting or in the brief. He flags focus-point conflicts early — not after something lands wrong.*

- What is the intended focus point at each major beat? Where does Direction want the player to be looking when the show's most important moments fire? Mark will work backward from those focus points.
- Is there a section in this show where free-look — the player choosing what to watch — is part of the design? Mark needs to know so he doesn't author orientation calls that fight that intention.
- Are there any moments requiring a cinematic perspective shift: the player spectating a moving entity (drone), the player riding a mob, a screen blackout to cover a scene transition?
- After the levitation lift (if there is one), what is the intended view? Looking out at a horizon? Looking up into a firework ring? Looking down at the world below? The answer determines Mark's pre-lift orientation strategy.
- For performer entrances: which direction are they coming from, and does Direction want the player actively oriented toward the entrance when it happens?

---

## Requests

*Nice-to-haves. These surface conflicts before they land wrong in-game.*

- Knowing Sound's major audio hits — when a significant sound fires, players instinctively look toward its direction or origin. Mark wants to either support that with an orientation call, or design against it deliberately. Sound and Camera should both know what the other department is doing at high-impact moments.
- Early notification from Choreography on all performer entrance directions. A FACE toward `mark:wing_left` needs to fire 5–10 ticks before the performer appears. If Mark doesn't know the entrance is coming, the player is looking elsewhere when it happens.
- From Effects: the planned pitch orientation at each altitude transition. Mark covers the yaw; Effects owns the vertical destination. The clearest model is joint authorship on each levitation sequence — Effects says what altitude, Mark says what horizon.

---

## Camera's Palette

Mark's instruments, in brief:

**Instant snap — horizontal (FACE)** — turns the player to face a mark, compass direction, or entity. Yaw only. One frame of change. Useful for horizontal focus-point assertions when the player needs to look left or right.

**Instant snap — with pitch (PLAYER_TELEPORT orientation-only)** — `offset: {x:0, y:0, z:0}` with explicit `yaw:` and `pitch:` fields. No position change; pure orientation reset. Mark's primary tool for "look up at something." This is how you make the player look up at a firework or look out at a horizon at liftoff.

**Smooth pan (PLAYER_SPECTATE + invisible drone)** — spawns an invisible entity, moves it via CROSS_TO, and temporarily spectates it. The player's view follows the drone's path. Smooth, cinematic, requires pre-planning the drone's route. Used when a snap would be too jarring.

**Scene transitions (CAMERA blackout)** — a full-screen blackout that covers the seam of a PLAYER_TELEPORT. The standard technique: Camera fires blackout 10 ticks before the teleport; Effects authors the teleport during the black; Camera sets orientation on arrival. The player opens their eyes somewhere new.

**Screen distortion (CAMERA sway, flash, float)** — sway: screen wobble (same visual as EFFECT nausea — coordinate with Effects so only one department owns it per moment). Flash: a brief bright flash. Float: a gentle screen drift.

**Riding perspective (PLAYER_MOUNT / PLAYER_DISMOUNT)** — player rides a mob entity; camera follows with it. Requires a rideable entity type. Limited and specific-use.

For full YAML syntax, the Focus Point Doctrine, drone pattern construction, and the honest accounting of what camera moves actually exist, see `camera.kb.md`.

---

## What Camera Can and Can't Do

**Verified and working:**
- Instant horizontal snap (FACE) and instant full-orientation snap (PLAYER_TELEPORT orientation-only)
- Smooth pan via invisible drone + PLAYER_SPECTATE
- Scene transition blackout (CAMERA blackout)
- Screen distortion effects (sway, flash, float)
- Riding perspective (PLAYER_MOUNT) for rideable entity types

**Current gaps — flag at intake if relevant:**
- FACE is yaw only — no pitch control. "Make the player look up" requires PLAYER_TELEPORT orientation-only with a `pitch:` value.
- No smooth ROTATE event (first-class). The drone spectate pattern is the workaround; it requires scripting the drone's path in advance.
- Auto-follow (camera tracking a moving subject in real time) does not exist. The drone pattern scripts the camera path in advance; for unscripted emergent action, the camera is fixed at whatever orientation Mark last set.
- Zoom (FOV control) is not available in Paper 1.21.x. This is a platform limit, not a plugin gap.

**Camera + Effects joint ownership:**
Destination is Effects authority; orientation on arrival is Camera authority. These two departments co-author every PLAYER_TELEPORT with facing fields, every aerial sequence, and every scene transition. They do not hand off separately and adjust later.

---

## Document Status

*First pass — 2026-03-28*

Core structure is complete. What's still needed:

- The pre-lift orientation pattern (what angle to set before levitation begins so the player's view fills correctly as they rise) isn't named and confirmed yet. R7 showed this gap — levitation happened without Camera asserting pitch beforehand, and the player ended up looking in undesigned directions. A named "pre-lift orientation" pattern should be added to the calibration backlog and confirmed in a calibration round.
- The drone spectate technique is documented in the KB but not tested as a named arrangement. A calibration round specifically for "smooth pan" technique would let Mark confirm the drone construction parameters.
