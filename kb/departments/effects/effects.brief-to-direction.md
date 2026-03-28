---
department: Effects Director
owner: Felix
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Effects — Brief to Direction

> Felix's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for all player movement, aerial physics, perceptual
> effects, and atmospheric particles lives in `kb/departments/effects/effects.kb.md`.

---

## Standing Requirements

*What Felix cannot start without. These shape every Effects decision in the show.*

**The show's altitude arc.** This is the primary structural question for the Effects department, and it must be decided at brief time — not discovered mid-authoring. Is the player grounded throughout? Elevated partway through? Does altitude build to a single earned peak? The altitude arc determines what Effects can offer in every other section. A show that opens at height has nowhere to go. A show that withholds altitude gives Felix his most powerful instrument.

**Whether any WEATHER effects are planned.** A storm state adds an audio bed Effects didn't author and that Sound cannot stop via STOP_SOUND. Felix accounts for this in the atmospheric particle design. If weather is not planned, saying so is equally useful.

**Whether any LIGHTNING effects are planned.** Thunder is baked into the lightning strike and cannot be tuned separately. Felix builds the surrounding physical impact arc around LIGHTNING events — the concussive moment that LIGHTNING delivers needs to be anticipated, not reacted to.

**Fireworks positions and altitudes at each pyrotechnic moment.** Effects and Fireworks must agree on where the player is standing when each burst fires. A player at 25 blocks looking at fireworks 5 blocks off the ground is looking down at a spent moment. This coordination happens before either department authors events — not as a post-hoc fix.

---

## Intake Questions

*Questions Felix asks before authoring any major physical event. He doesn't guess at altitude context.*

- Where is the player when the show begins? Are they outdoors, in an interior, on elevated terrain?
- Is there a designed moment of physical transcendence — a lift, a launch, a peak altitude — in this show? If so, when in the arc is it? Felix's entire design strategy changes depending on whether the lift is early or late.
- Should the player's body feel anything in this show — or is this primarily a still, observational experience where physical sensation would distract from what the other departments are doing?
- Are there any sections meant to feel physically heavy, sluggish, or disorienting? (Slowness, darkness, nausea, blindness?) These are Effects instruments, but the decision to use them belongs to Direction.
- Does the show use `night_vision` to reveal a crafted dark space? If so, that's a joint call with Lighting — Felix can't apply night_vision without knowing what it reveals.

---

## Requests

*Nice-to-haves. These prevent the most common cross-department conflicts.*

- Early confirmation of Fireworks altitude and timing. Saying "there's a burst in the finale section" is a start; knowing the player will be at roughly 20 blocks at that moment lets Felix design the altitude arc to match.
- Camera's intended horizon at each major levitation moment. Felix moves the body; Camera moves the eyes. Knowing where Mark wants the player to be looking at liftoff — and what they should see as they rise — lets Effects design the climb with that view in mind. These two departments need to co-author aerial sequences, not hand off separately.
- Whether any dark sections are planned that should feel revelatory when light returns. That's an Effects + Lighting decision — the night_vision moment should be intentional, and both departments should agree on the timing.

---

## Effects' Palette

Felix's instruments, in brief:

**Scripted movement** — instant set transitions (PLAYER_TELEPORT) and smooth staged crosses (CROSS_TO). Destination is Effects authority; yaw and pitch on arrival are Camera authority. The standard transition technique: Camera fires a blackout 10 ticks before the teleport; player opens their eyes somewhere new.

**Aerial physics** — levitation at three calibrated patterns (HOVER: clean altitude hold; CLIMB: gradual upward drift; AMP-9: the earned single surge), PLAYER_FLIGHT hover to lock altitude after a lift, and PLAYER_FLIGHT release for controlled descent. These patterns are calibrated from in-game testing. See `effects.kb.md §Aerial Physics` for exact cycle timing.

**The altitude arc.** Altitude is currency, not backdrop. Once spent, it cannot be repurchased. The HOVER → CLIMB → AMP-9 LIFT sequence is designed to be used once per show. Felix protects the arc jealously.

**Perceptual suite** — slowness, speed, darkness, blindness, nausea, and night_vision, each with a distinct emotional register. Blindness for revelation flash or sustained fog. Darkness for dread. Nausea coordinates with Camera (both produce screen wobble — don't use both at the same moment without intent).

**Atmospheric particles** — repeating layers of ash, soul fire flame, end rods, enchantment glyphs, and more. Particles spawn at the anchor point (typically ground level), not at the player's current altitude — a player at 20 blocks sees ground-level particles below them. This is a feature when designed for, a miss when ignored.

For full YAML syntax, calibrated patterns, and the complete particle vocabulary, see `effects.kb.md`.

---

## What Effects Can and Can't Do

**Verified and working:**
- PLAYER_TELEPORT (instant transition, full yaw + pitch control on arrival)
- CROSS_TO for the target player (smooth absolute interpolation, tick-exact)
- PLAYER_VELOCITY impulse + slow_falling controlled arc
- PLAYER_FLIGHT hover and release with confirmed descent rate (~2 blocks/second under slow_falling)
- Full levitation pattern suite (HOVER / CLIMB / AMP-9), all calibrated in-game
- Full perceptual effect suite (slowness, speed, darkness, blindness, nausea, night_vision)
- Atmospheric particles — single burst and repeating layer modes

**Standing limitations — not bugs, just physics:**
- Altitude is spent once per arc. A show that uses the AMP-9 lift early cannot manufacture equivalent wonder from a second lift later. The arc is intentional; the restraint is the instrument.
- `night_vision` requires Lighting coordination. Felix cannot apply it without knowing what it reveals. Do not author night_vision sequences without a joint brief from Lighting.
- EFFECT `nausea` and CAMERA `sway` both produce screen wobble. They should not coexist at the same moment without explicit intent. One department owns it per moment.

**Currently aspirational (not yet implemented):**
- GIVE_ITEM to player
- GIVE_XP to player

---

## Document Status

*First pass — 2026-03-28*

Core structure is complete. What's still needed:

- The hover-pattern calibration work from R7 debrief (2026-03-28) is pending: distinguishing HOVER vs. CLIMB texture for the player, and the revised near-ground bubbling pattern. Once those patterns are confirmed, the palette section here should reference them by named pattern rather than describing them inline.
- The descent-through-fireworks arrangement is confirmed (C9 archetype sampler). That named pattern should eventually be linked from this document as the reference model for Effects + Fireworks coordination.
