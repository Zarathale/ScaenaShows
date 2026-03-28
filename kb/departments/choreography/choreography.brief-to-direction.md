---
department: Choreographer / Movement Director
owner: Sharon
doc_type: brief-to-direction
version: 1.0
created: 2026-03-28
status: first-pass
---

# Choreography — Brief to Direction

> Sharon's standing requirements, intake questions, and requests for the Show Director.
> The authoritative technical reference for all movement instruments, AI state design,
> and NPC lifecycle lives in `kb/departments/choreography/choreography.kb.md`.

---

## Standing Requirements

*What Sharon cannot start without. The movement score doesn't get written until these are resolved.*

**The full cast list with confirmed entity types (from Casting).** Sharon cannot block a show without knowing who is performing. Entity type determines movement behavior — a Villager crosses to a mark reliably on flat terrain; a Vex phases through walls and is unreliable without puppet state; an Enderman should never be AI-enabled near the player. Sharon reads the Casting brief before writing a single movement event.

**All named marks defined by Set.** Movement is written to named marks (`mark:center`, `mark:wing_left`), never to inline XZ offsets. If marks aren't defined, they can't be referenced, and shows written with inline offsets don't travel. Set's mark definitions need to be in place before the movement score is authored.

**AI state intent for each performer.** Puppet state (AI disabled) or performer state (AI enabled) is the most consequential creative decision in the movement score — and it belongs to the Show Director at the brief level, not to Sharon at the authoring level. The brief should state whether each figure is meant to be controlled or authentic. Sharon defaults to puppet when the brief doesn't specify, but will flag the choice in the run sheet and ask if that's intentional.

**The player's altitude arc (from Effects), for any show with aerial sequences.** If the player is levitated at beat 7, the performers' positions are designed relative to where the player *actually is* — not where they started. Sharon cannot author a performer cross "toward the player" in an aerial sequence without knowing where the player will be.

---

## Intake Questions

*Questions Sharon asks at the intake meeting or in the brief. She doesn't assume answers to these.*

- Is there a solo performer, a duo/trio, or a chorus? The number changes the entire movement score — a chorus needs group choreography, a solo needs individual blocking.
- Does any performer make a formal entrance or exit — arriving from off-stage, crossing to a position — or are they discovered in place when the show begins?
- Are any performers meant to move toward or away from the player? And how close — intimate (3 blocks), near (8 blocks), distant (15 blocks)?
- Is there a section where the stage should be absolutely still? Sharon marks tableau moments as explicitly as she marks movement.
- Are there any moments where unpredictable, authentic creature behavior is part of the design — where a wolf actually running, or an allay actually hovering, is the point? Sharon needs to know so she can plan the rest of the show around those moments of performer freedom.

---

## Requests

*Nice-to-haves. These improve the work but don't stop it.*

- Knowing Sound's audio arc around performer arrivals. An entrance that isn't paired with a sound hit is an entrance that can go unnoticed. Sharon marks the tick of each arrival; Sound plans around it. Neither department should discover the other's key beats mid-authoring.
- Knowing Wardrobe's equipment decisions before the movement score is final. A figure in full iron armor reads differently moving than one carrying only a torch — the visual weight affects how the movement is perceived.
- Knowing Camera's orientation for any scene where a performer enters from the side or behind. Sharon prefers to know whether Mark will be pointing the player's attention toward an entrance before she authors it.

---

## Choreography's Palette

Sharon's instruments, in brief:

**Puppet vs. performer (ENTITY_AI)** — the most important creative decision for any cast member. Puppet state (AI disabled): the entity holds position, responds to scripted events, can be moved instantly or via pathfinder. Performer state (AI enabled): authentic Minecraft behavior, compelling but unpredictable. Sharon defaults to puppet; she earns every moment of performer freedom.

**The Cross (CROSS_TO)** — moves a named entity from their current position to a destination mark. Mob crosses use the pathfinder (arrival not guaranteed on complex terrain); player crosses use smooth absolute interpolation (tick-exact, reliable). Short distances on flat open terrain only for mobs.

**Entrances and exits (ENTER / EXIT)** — semantic shorthands for arriving from a wing and departing to one. ENTER spawns at a wing mark and pathfinds to the stage. EXIT pathfinds to a wing mark and optionally despawns on arrival.

**Hold and face (HOLD / FACE)** — HOLD zeroes velocity at the moment it fires (does NOT disable AI — for persistent stillness, use ENTITY_AI disabled). FACE orients an entity toward a mark, direction, or the player. Yaw only — no vertical pitch control on entities.

**Speed and impulse (ENTITY_SPEED / ENTITY_VELOCITY)** — ENTITY_SPEED scales movement speed permanently until reset. ENTITY_VELOCITY applies a one-time impulse (useful for floating arcs with slow_falling).

**Chorus management (CAPTURE_ENTITIES / RELEASE_ENTITIES)** — absorbs world-resident entities into show control as a named group. Useful when the village's actual inhabitants, or the cave's actual bats, should become performers.

For full YAML syntax, timing guides, and NPC type movement notes, see `choreography.kb.md`.

---

## What Choreography Can and Can't Do

**Verified and working:**
- Full NPC lifecycle: spawn, AI state toggle, scripted crosses, hold, face, exit, despawn
- Player crosses via smooth absolute interpolation (tick-exact)
- Mob crosses via pathfinder (best on flat terrain, short distances 5–10 blocks)
- Captured entity group management (HOLD, FACE, CROSS_TO work on full groups)

**Current gaps — flag at intake if they're relevant:**
- ENTITY_SPEED on entity groups affects only the first member (Java gap). ENTITY_SPEED only works reliably on individually named entities.
- ENTER doesn't apply equipment at entrance — use SPAWN_ENTITY at the wing offset + CROSS_TO when the entering performer needs wardrobe at arrival.
- No smooth rotation event (no ROTATE). Instant FACE snap only. Workaround: invisible spectate drone for gradual attention redirect (Camera dept's tool, not Sharon's).
- Mob pathfinding arrival is not guaranteed. Design mob crosses for flat, open surfaces. Keep cross distances short. Do not author tight timing that depends on a mob arriving at an exact tick.
- RETURN_HOME works on players only — spawned entities use CROSS_TO or DESPAWN_ENTITY for end-of-show cleanup.

---

## Document Status

*First pass — 2026-03-28*

Core structure is complete. What's still needed:

- Confirmed movement patterns from calibration (see `choreography.kb.md §Calibration Backlog`) — `placement.near`, `placement.far`, `sequence.arrive.hold`, etc. are proposed, not yet confirmed in-game. Once confirmed, this document should reference named patterns by name.
- Cross-reference from this doc to Casting's "What Casting Needs" section — the two departments share intake questions about entity type, AI state, and group size. Worth checking that neither department is asking the same questions twice.
