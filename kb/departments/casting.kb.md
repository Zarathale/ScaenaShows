---
department: Casting Director
owner: Casting Director
kb_version: 2.0
updated: 2026-03-25
---

# Casting Director — Technical Knowledgebase

> Technical reference for the Casting Director department. Documents what the ScaenaShows Java
> plugin can do for entity selection, AI control, and group management — and how to access those
> capabilities through YAML.
>
> Creative direction for this role lives in `kb/production-team.md §1. Casting Director`.

---

## Role Summary

- Owns all entity lifecycle events: when performers enter the world, how they are identified, and when they leave
- Owns the puppet/performer toggle (ENTITY_AI) — the fundamental choice between show control and authentic mob behavior
- Owns world-resident entity capture: sweeping existing entities into a show group without spawning
- Owns the dramatic entrance and exit shorthands (ENTER / EXIT)
- The question every decision traces back to: **who is on stage, and what does their nature do for free?**

---

## What Casting Needs Before Making Decisions

Before choosing a performer, Casting needs answers to these questions. These may come from the Show Director's brief, from the intake conversation, or from Casting's own questions to the table:

**About the show environment:**
- What biome / setting is the show happening in? (Some mobs read differently in their native biome vs. an alien one — a Strider in lava reads as home; in a forest it reads as intrusion)
- Are there existing world entities nearby that could be captured? (CAPTURE_ENTITIES is often better than spawning new ones when the world already has the right inhabitants)
- What is the time of day and weather? (Phantom AI, Phantom spawning, mob visibility, and atmosphere all shift with these)

**About the player's relationship to the performer:**
- Is the player a witness (observer, from a distance), a companion (nearby, performer reacts to them), or a subject (performer is attending to the player)?
- Should the performer feel placed (puppet) or alive (performer state)? Or should it begin placed and then release?
- Is the performer a solo figure, a duo/trio, or a chorus? The number changes the emotional read dramatically.

**About the show arc:**
- Does this performer need to do anything other than exist? (Pure tableau figures need zero movement design)
- Does the performer need to be removed at some point, and how — vanish (DESPAWN_ENTITY), walk off (EXIT), or simply release to the world (RELEASE_ENTITIES)?
- Is the variant or profession of this entity emotionally important? If yes, flag immediately — variant/profession are currently gapped (see §Gaps).

**About risk:**
- Is AI release safe in this context? Phantoms, Creepers, Wardens, and Elder Guardians can actively harm the player when AI is enabled. This requires the Show Director's sign-off.
- Is despawn_on_end required? If the show might be interrupted, is there a risk of leaving world-polluting entities behind?

---

## The Dramatis Personae — Mob Register

Every entity that can be spawned via `entity_type:` in SPAWN_ENTITY. Organized by dramatic register.

> This register covers mobs with meaningful dramatic/emotional potential. Any valid Bukkit
> `EntityType` can be spawned, but Casting cares about what the choice *communicates*.
> If an entity type isn't here, treat it as a walk-on extra — useful for texture, not for character.

---

### Figures of Loyalty and Companionship

**Wolf (tamed-appearance)**
Sitting wolf watching its owner is one of Minecraft's most emotionally complete images: devoted,
patient, present. Register: devotion, companionship, the bond that doesn't need language. Puppet
state makes it a composed, still figure; performer state adds the authentic follow-and-watch behavior
that no script can replicate. Multiple coat colors in 1.21+ (variant gap — see §Gaps). Tamed
wolf posture when AI-disabled is sitting; this is the emotionally resonant pose.

**Allay**
Small, hovering, follows and collects items, plays a musical note when delivering. Register: joy,
affection, service, the spirit that wants to help. A natural chorus member for joyful or
celebratory sequences. Puppet state holds it in position; performer state adds genuine wandering
delight. One Allay feels like a companion; several read as a shimmer of goodwill.

**Cat**
Sits on beds, hisses at phantoms, brings gifts. Register: independence, comfort, the domestic
uncanny. A sitting cat (puppet) is a composed and mysterious presence — it chose to stay. A
walking cat (performer) introduces autonomous curiosity that the show doesn't control. Multiple
coat variants (variant gap — see §Gaps).

**Bee**
Small, buzzing, soft glow. Register: industry, fragility, the small thing that matters enormously.
A single bee near a flower reads very differently from a swarm. Puppet state near a flower reads
as almost meditative.

---

### Figures of Weight and Authority

**Iron Golem**
Large, slow, protector. Mourns dead villagers by placing poppies. Register: loyalty, grief as
witness, the protector who outlived what it protected. A single Iron Golem standing motionless
in a quiet scene carries enormous emotional weight without any YAML at all beyond the spawn.
One of the most powerful solo casting choices available. Always use puppet state.

**Villager**
Wandering, trading, schedule-following. Register: community, the ordinary world, labor, bearing
witness. *Profession is the primary expressive tool* — a Cleric villager reads as ritual and
mystery; a Librarian reads as scholarship; a Farmer reads as the land. Profession is currently
gapped (see §Gaps), but equipment can substitute. Baby Villager: innocence, vulnerability. A
crowd of Villagers establishes a functioning society; a single Villager watching quietly is a
witness to whatever the player is going through.

**Warden**
Blind, sonic detection, slow, nearly unkillable. Register: inevitability, darkness incarnate, something
that cannot be stopped — only endured. AI disabled = the threat held perfectly still, which is
often more powerful than an active threat. **Never release AI near players without Show Director
sign-off.** The emotional register requires no action — just presence.

---

### Figures of the Uncanny and Strange

**Enderman**
Tall, teleports when looked at, carries blocks. Register: the uncanny watcher, psychological
tension, the mirror of your attention. Best as a single figure at distance, puppet state, placed
to be noticed rather than to act. Teleportation makes it unpredictable in performer state.

**Phantom**
Aerial, swoops players who haven't slept, distinctive screech. Register: consequence, anxiety, the
cost of something. Most effective as a sound + silhouette in a dark scene — the shape overhead
that the player is aware of. **AI-released Phantoms will actively swoop and deal damage — requires
Show Director sign-off.**

**Vex**
Tiny, phasing, ghost-form, sword-carrying. Register: sorrow made small, fragile grief with an
edge. A chorus of Vex circling silently overhead reads as grief made visible. Phases through blocks
when AI is enabled, making pathfinding unreliable — use puppet state for anything requiring
placement.

**Bat**
Tiny, erratic flight, cave-ambient sounds. Register: texture of being watched, secrets overhead,
the ambient dark. Most effective as a chorus. AI enabled = chaotic and atmospheric; puppet state
clusters them artificially and reads oddly. Let bats be bats.

**Elder Guardian**
Floats, applies Mining Fatigue, emits ghostly scream. Register: ancient affliction, the weight of
the sea, something that sees you. The Mining Fatigue effect is a real player debuff — the curse
moment, when used deliberately, is one of the most visceral player experiences possible. **Use
the debuff intentionally or not at all. Requires Show Director sign-off for AI release.**

**Strider**
Walks on lava, trembles in cold. Register: alien landscape, the creature that lives where you
cannot. Best in environments with actual lava. Without lava it reads as confused rather than
otherworldly. Saddled variant adds an implication of being ridden — the beast that was tamed
and then forgotten.

---

### Figures of Consequence and Tension

**Creeper**
Silent approach, brief hissing, explosion. Register: inevitable tension, the unannounced arrival
of consequence. Best as a visual element with AI disabled — the threat that holds perfectly still
is more unsettling than one that moves. **Never release AI near players. Full stop.**

**Fox**
Sneaks, steals items, leaps at prey. Register: cunning, the trickster. Baby fox softens
considerably — mischief rather than cunning. Best in performer state when the trickster quality
is the point.

**Slime / Magma Cube**
Bouncing, splits on death. Register: individually harmless, collectively claustrophobic. Size
matters dramatically: a single small slime is almost comedic; a room filling with them is
overwhelming. Size is currently not YAML-controllable.

---

### Non-Mob Figures

**Armor Stand**
Not a mob — a static entity that holds equipment. Register: the absent body, the memorial, the
costume on a wire, presence without a person. A full suit of armor standing in a corner reads
as whoever used to wear it. Posing requires COMMAND escape hatch (filed gap). Accepts all
equipment slots. AI disabled is the only mode. Wardrobe leads on equipment design; Casting
decides whether an Armor Stand is a performer or a set piece (this boundary is a standing
conversation with Set).

---

## Instrument Inventory

---

### Instrument 1: The Fresh Spawn
**Java grounding:** `SPAWN_ENTITY` + `DESPAWN_ENTITY`

A performer is brought into the world at a named offset from the spatial anchor. It has an
identity (`name:`), an optional initial wardrobe (`equipment:`), and a lifespan (`despawn_on_end:`).

**What it does:** Places a performer at a specific world position, registers it under a name for
all subsequent targeting, and optionally removes it at show end.

**How to dial it:**
```yaml
# Minimal — no targeting needed after spawn
- at: 0
  type: SPAWN_ENTITY
  entity_type: IRON_GOLEM
  offset: {x: 0, y: 0, z: 5}
  name: "Sentinel"
  despawn_on_end: true

# Full — targeted, equipped, immediately puppeted
- at: 0
  type: SPAWN_ENTITY
  entity_type: VILLAGER
  offset: {x: 3, y: 0, z: 3}
  name: "Herald"
  baby: false
  equipment:
    helmet: LEATHER_HELMET
    main_hand: STICK
  despawn_on_end: true

- at: 1
  type: ENTITY_AI
  target: entity:spawned:Herald
  enabled: false   # puppet immediately after spawn

# Despawn with dramatic burst
- at: 400
  type: DESPAWN_ENTITY
  target: entity:spawned:Herald
  particle_burst: true

# Despawn silently (preferred for quiet exits)
- at: 400
  type: DESPAWN_ENTITY
  target: entity:spawned:Herald
  particle_burst: false
```

**Strengths:**
- Precise placement relative to the spatial anchor
- Any Bukkit EntityType is valid — the range of possible performers is enormous
- `name:` registration makes the performer addressable by every other department
- `baby: true` is a separate emotional register for most mobs — use it deliberately

**Limitations and gaps:**
- `variant:` and `profession:` are parsed but NOT applied at runtime (see §Gaps). Default
  mob skin/profession always renders regardless of what YAML specifies.
- No size control for Slimes/Magma Cubes
- No facing/yaw control at spawn — entity spawns with default orientation; use FACE immediately after if orientation matters

**Storytelling contexts:**
- An Iron Golem spawned at T=0 and never given another instruction — just standing there — can anchor a quiet grief scene without any other work
- Spawn a Wolf, immediately puppet it, have it sit facing the player's position: the devoted companion who was there before you arrived
- Spawn multiple Allays with slight offset variance to create a small chorus of joy at a celebration moment

---

### Instrument 2: The Company Sweep
**Java grounding:** `CAPTURE_ENTITIES` + `RELEASE_ENTITIES`

Instead of spawning new performers, pull the existing inhabitants of the world into show control.
A group is named and becomes targetable as a unit.

**What it does:** Sweeps a radius around the spatial anchor for entities of a given type, registers
them as a named group, and later releases them back to normal world behavior.

**How to dial it:**
```yaml
# Sweep nearby sheep into a silent flock
- at: 0
  type: CAPTURE_ENTITIES
  entity_type: SHEEP
  radius: 20
  max_count: 12
  group_name: flock
  capture_mode: snapshot    # group membership fixed at this tick

# Puppet the entire flock immediately
- at: 1
  type: ENTITY_AI
  target: entity_group:flock   # ⚠️ SEE GAP — only affects first entity currently
  enabled: false

# Release them back to the world at show end
- at: 800
  type: RELEASE_ENTITIES
  target: entity_group:flock
  restore_ai: true
```

**Strengths:**
- World-resident entities feel organic in a way spawned entities don't — they belong here
- The group abstraction is powerful when working with crowds or swarms
- `max_count` provides a ceiling; if fewer exist, no error — the group is simply smaller

**Limitations and gaps:**
- `ENTITY_AI`, `ENTITY_SPEED`, `ENTITY_EFFECT`, `ENTITY_EQUIP`, `ENTITY_INVISIBLE`, and
  `ENTITY_VELOCITY` all resolve only the **first member** of a group when targeting
  `entity_group:`. This is a Java gap — only RELEASE_ENTITIES and the stage direction events
  (HOLD, FACE, CROSS_TO) correctly iterate the full group. Filed — see §Gaps.
- `capture_mode: live` is parsed but not implemented — the executor always performs a snapshot
  sweep regardless of the value. Filed — see §Gaps.
- If no entities of the specified type exist within the radius, the group is silently empty —
  all events targeting it skip. Always verify the world state in the run sheet.

**Storytelling contexts:**
- A field of sheep captured and puppeted, standing absolutely still while a voice line plays —
  the ordinary world holding its breath
- Capture a flock of bats in a cave sequence and release their AI for chaotic atmosphere
- Capture village Villagers into a "witnesses" group to give them coordinated blocking via HOLD and FACE

---

### Instrument 3: The Theatrical Entrance / Exit
**Java grounding:** `ENTER` + `EXIT`

Semantic shorthands that combine spawning and movement into a single readable intent. The
Choreographer owns the movement shape; Casting owns the entity identity and whether the performer
cleans up on arrival.

**What it does:** ENTER spawns at a wing mark and moves toward a destination. EXIT moves an
existing performer to a wing mark and optionally despawns on arrival.

**How to dial it:**
```yaml
# Entrance from stage left
- at: 60
  type: ENTER
  entity_type: VILLAGER
  name: Messenger
  from: mark:wing_left
  destination: mark:center
  duration_ticks: 40
  facing: mark:center
  baby: false

# Exit to stage right, despawn on arrival
- at: 200
  type: EXIT
  target: entity:spawned:Messenger
  to: mark:wing_right
  duration_ticks: 30
  despawn_on_arrival: true
```

**Strengths:**
- Cleaner YAML than a SPAWN_ENTITY + CROSS_TO sequence
- EXIT's proximity-based despawn (`distanceSquared < 4`) is reliable for mobs walking across flat terrain

**Limitations and gaps:**
- ENTER uses the mob pathfinder (`pathfinder.moveTo()`), not the smooth interpolation used for
  players. Arrival is not guaranteed on complex terrain or through crowds.
- ENTER does not support `equipment:` — if the performer needs Wardrobe at entrance, use
  SPAWN_ENTITY + CROSS_TO instead, and equip in the SPAWN_ENTITY block.
- EXIT only works on `Mob` subclasses. Armor Stands and non-mob entities cannot EXIT.
- ENTER has no `variant:` or `profession:` fields — same gap as SPAWN_ENTITY.
- `duration_ticks:` in ENTER is parsed by the model but the executor passes it to
  `pathfinder.moveTo(dest)` without a speed/duration override — actual travel time depends on
  the mob's natural movement speed, not the YAML value. Use ENTITY_SPEED before ENTER to tune.

**Storytelling contexts:**
- A Villager entering from the wings, walking to center, and stopping to face the player —
  the arrival of news, the witness who came to speak
- A Wolf entering slowly from off-stage, arriving at the player's feet, sitting — the companion
  who found their way back

---

### Instrument 4: Puppet vs. Performer
**Java grounding:** `ENTITY_AI`

The most consequential toggle Casting controls. Puppet state (AI disabled) gives the show complete
control over placement; performer state (AI enabled) returns the entity to its natural Minecraft
behavior — authentic, but unpredictable.

**What it does:** Calls `mob.setAI(boolean)` on the target entity.

**How to dial it:**
```yaml
# Puppet — freeze in place
- at: 1
  type: ENTITY_AI
  target: entity:spawned:Sentinel
  enabled: false

# Release to performer — authentic behavior resumes
- at: 240
  type: ENTITY_AI
  target: entity:spawned:Sentinel
  enabled: true
```

**The spectrum — what each mode gives:**

| Mode | When | Effect | Risk |
|------|------|--------|------|
| Puppet (enabled: false) | Immediately after spawn | Holds position; full show control | None for staging; none for safety |
| Performer (enabled: true) | Deliberate release moment | Natural behavior; can't be scripted further | Variable per mob type; some are dangerous |
| Delayed release | Mid-show transition | "Coming alive" moment — powerful if timed right | Same risk as performer, amplified by surprise |

**Strengths:**
- The release moment — an entity that was perfectly still suddenly resuming its nature — is one
  of the most affective things Casting can do with a single YAML event
- Puppet state is the bedrock of tableau work; any scene requiring stillness depends on it
- Performer state for Allays, Wolves, Cats, and Foxes adds authentic behavior that scripted
  movement cannot replicate

**Limitations and gaps:**
- Works only on `Mob` subclass — Armor Stands are unaffected by ENTITY_AI (they have no AI)
- When targeting `entity_group:`, only the **first group member** is affected (Java gap — see §Gaps)
- Entities spawn with AI enabled by default. For a puppet from birth, fire ENTITY_AI at T=1 (one
  tick after spawn), not T=0 — the entity needs to exist before its AI can be toggled

**Storytelling contexts:**
- Spawn a Wolf, puppet it immediately, run an entire quiet scene with it sitting at the player's
  side — then release it as the emotional climax arrives and watch it circle its companion
- A room full of puppeted Villagers holding still while the Voice Director speaks — then release
  them all as the scene breaks open
- A Phantom held perfectly still overhead for an entire sequence, released only when the tension
  is meant to land (requires Show Director sign-off)

---

## Entity Targeting Reference

| Target syntax | What it addresses | Notes |
|---|---|---|
| `entity:spawned:Name` | A specific entity spawned by this show via SPAWN_ENTITY or ENTER | Reliable; registered at spawn |
| `entity_group:groupname` | All members of a CAPTURE_ENTITIES group | ⚠️ Only first member for behavior events — see §Gaps |
| `entity:world:Name` | A named entity already present in the world | ⚠️ Not implemented — see §Gaps |

---

## Capability Gaps

> Stage Management owns the canonical gap registry in `ops-inbox.md`. This section documents what
> Casting needs to know for show authoring decisions.

---

### Gap 1: `variant` and `profession` on SPAWN_ENTITY — parsed but not applied
**Status:** Open. Filed in `ops-inbox.md`.
**Impact:** Villager profession, cat coat, horse color, sheep wool color, wolf variant (1.21+) cannot
be set via YAML. The fields are silently ignored. Default mob appearance always renders.
**Workaround:** Use `equipment:` to differentiate visually. A Villager with a LEATHER_HELMET and
HOE reads differently from one with GOLDEN_HELMET and BOOK. Design around the gap: choose mobs whose
default appearance serves the role without variant control.

---

### Gap 2: `ENTITY_AI` and behavior events resolve only first group member
**Status:** Open. **Not yet filed — flag to Stage Management.**
**Affected events:** `ENTITY_AI`, `ENTITY_SPEED`, `ENTITY_EFFECT`, `ENTITY_EQUIP`,
`ENTITY_INVISIBLE`, `ENTITY_VELOCITY`
**Root cause:** `EntityEventExecutor.resolveEntity()` returns only the first UUID from the group
list. The stage direction events (`HOLD`, `FACE`, `CROSS_TO`) correctly iterate all members via
`resolveEntities()` (plural), but none of the behavior events use that method.
**Impact:** Firing `ENTITY_AI enabled: false` on `entity_group:flock` only puppets the first
sheep in the flock. The rest remain in performer state.
**Workaround:** For small, named casts, use individually named entities rather than groups when
behavior events are needed. For large groups where individual targeting is impractical, note in
the run sheet that AI/behavior events are unreliable against groups.

---

### Gap 3: `capture_mode: live` parsed but not implemented
**Status:** Open. **Not yet filed — flag to Stage Management.**
**Root cause:** `CaptureEntitiesEvent` stores `captureMode` but `handleCapture()` always performs
a one-time UUID snapshot regardless of the value. There is no re-sweep logic.
**Impact:** `capture_mode: live` behaves identically to `capture_mode: snapshot`. The field is
a no-op.
**Workaround:** Use multiple `CAPTURE_ENTITIES` events at different ticks to simulate re-sweeping
if the group needs updating. Document the expected world state in the run sheet.

---

### Gap 4: `entity:world:Name` targeting not implemented
**Status:** Open. **Not yet filed — flag to Stage Management.**
**Root cause:** `EntityEventExecutor.resolveEntity()` handles `entity:spawned:` and
`entity_group:` prefixes only. No handler exists for `entity:world:`. The method returns null for
any `entity:world:` target, and all events against it silently skip.
**Impact:** Cannot target pre-existing named world entities by custom name through show YAML.
CAPTURE_ENTITIES is the only way to bring world-resident entities under show control.
**Workaround:** Use CAPTURE_ENTITIES with a radius of 1–2 and max_count: 1 to effectively single-
target a known world entity. Name the group to match its role.

---

## Tone Translation

The Show Director communicates tone as experience: "something lives here that you can't quite
place," or "joy that doesn't know it will end." Casting translates those phrases into entity
choices.

**When the Director says "tender":**
A small creature in puppet state, close to the player. Allay (stationary, near) or a sitting
Wolf. The creature that chose to stay.

**When the Director says "overwhelming":**
A group — many entities, a chorus. Allays for overwhelming joy; Vex for overwhelming grief;
bats for overwhelming unease. Scale is the instrument.

**When the Director says "ancient" or "heavy":**
Iron Golem. Warden (puppet). Elder Guardian (puppet, from distance). Something large that has
been here longer than the player.

**When the Director says "uncanny" or "strange":**
Enderman at distance, puppet, not engaging. Armor Stand in an unexpected context. An Allay
that should read as joyful but is instead held absolutely still.

**When the Director says "earned" or "worth the wait":**
A performer that was puppeted for the entire rising action, released at the peak. The Allay
that finally moves. The Wolf that finally approaches.

**When the Director says "domestic" or "ordinary":**
Villagers. Cats. Chickens in the periphery. The world that was already here.

**When a tone phrase can't be translated without the variant gap resolved:**
Say so clearly. "The emotional read here depends on profession — a Cleric Villager vs. a Farmer
Villager reads very differently. This scene is dependent on the variant gap being resolved before
this casting choice will land." Don't silently substitute.

---

## Department Principles

**Casting is ultimately about meaning, not mechanics.** The question is never "what entity can do
this?" but "what entity carries this meaning into the scene for free, before a single other event
fires?"

**The mob's nature is the first script.** An Iron Golem doesn't need choreography to read as grief.
A sitting Wolf doesn't need dialogue to read as devotion. Cast the entity that already *is* what
the scene needs, and then layer only what's necessary.

**Puppet state is the default; performer state is a choice.** Defaulting to AI-disabled is not
timidity — it's craft. The decision to release a performer's AI is a dramatic event, not an
oversight. It should appear in the run sheet with a Watch question attached.

**Casting and Wardrobe share the performer.** Casting chooses the entity type and the AI state;
Wardrobe dresses them. A Casting decision that depends heavily on variant or profession requires
an immediate coordination flag: "this read depends on Wardrobe being able to set the profession,
which is currently gapped." Don't cast a Cleric and then silently accept a PLAINS farmer.

**Casting and Set share the Armor Stand.** An Armor Stand used as a pure set piece (the absent
knight) belongs to Set; an Armor Stand that is a show performer with a named identity, AI state,
and scheduled appearances belongs to Casting. When in doubt, put the question to the Show Director
at intake.

**AI risk requires Show Director sign-off.** Phantoms, Creepers, Wardens, and Elder Guardians with
AI enabled can harm the player. This is never an accidental Casting choice — flag it in intake,
note it in the run sheet, and confirm explicitly with the Show Director before any in-game test.

**Capability gaps are flagged at intake, not discovered at runtime.** If a casting choice depends
on `variant:` or `profession:`, say so at the production meeting, not after the in-game test.
The director and Wardrobe both need to know.

**Escalation:** Casting resolves entity type, AI state, group configuration, and entrance/exit
design independently. Escalate to the Show Director when: a casting choice conflicts with the arc
established in the brief; a performer's natural behavior is a safety risk; or the scene requires
a capability that is currently gapped and no workaround serves the creative intent.

---

## Capability Status Summary

| Instrument / Feature | Status | Notes |
|---|---|---|
| SPAWN_ENTITY — entity type, offset, name | ✅ Verified | Any Bukkit EntityType; name required for targeting |
| SPAWN_ENTITY — baby variant | ✅ Verified | setBaby() on Ageable; traced to EntityEventExecutor.java |
| SPAWN_ENTITY — equipment at spawn | ✅ Verified | All 6 slots; traced to EntityEventExecutor.java |
| SPAWN_ENTITY — despawn_on_end | ✅ Verified | Model field parsed; runtime cleanup confirmed |
| SPAWN_ENTITY — variant / profession | ⚠️ Gapped | Parsed, not applied. Filed in ops-inbox.md |
| DESPAWN_ENTITY — silent | ✅ Verified | entity.remove(); traced to EntityEventExecutor.java |
| DESPAWN_ENTITY — particle_burst | ✅ Verified | EXPLOSION particle; traced to EntityEventExecutor.java |
| CAPTURE_ENTITIES — snapshot sweep | ✅ Verified | UUID list built from getNearbyEntities(); traced to EntityEventExecutor.java |
| CAPTURE_ENTITIES — live re-sweep | ⚠️ Gapped | Parsed, not implemented. Not yet filed. |
| RELEASE_ENTITIES — restore_ai | ✅ Verified | Iterates all group UUIDs; traced to EntityEventExecutor.java |
| ENTITY_AI — named entity | ✅ Verified | mob.setAI(enabled); traced to EntityEventExecutor.java |
| ENTITY_AI — entity_group (all members) | ⚠️ Gapped | resolveEntity() returns only first member. Not yet filed. |
| ENTER — spawn + pathfinder move | ✅ Verified | Spawns at mark, pathfinder.moveTo destination; traced to StageEventExecutor.java |
| ENTER — duration_ticks honored | ⚠️ Gapped | Parsed; pathfinder moveTo ignores duration. Use ENTITY_SPEED to tune. |
| ENTER — equipment at entrance | 📋 Aspirational | Not in model — use SPAWN_ENTITY + CROSS_TO for equipped entrances |
| EXIT — pathfinder move + despawn | ✅ Verified | distanceSquared < 4 proximity check; traced to StageEventExecutor.java |
| EXIT — Armor Stand / non-mob | ⚠️ Gapped | EXIT only works on Mob subclass |
| entity:world:Name targeting | ⚠️ Gapped | No handler in resolveEntity(). Not yet filed. Use CAPTURE_ENTITIES workaround. |
| Slime / Magma Cube size control | 📋 Aspirational | Not YAML-controllable |
| Armor Stand pose control | 📋 Aspirational | Requires COMMAND escape hatch (Set's domain) |
