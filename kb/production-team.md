---
status: active
created: 2026-03-25
owner: Alan
notes: Authoritative reference for the ScaenaShows virtual production team. Shared knowledge base for Claude show authoring and the future ShowSprite in-game assistant.
---

# ScaenaShows — Virtual Production Team

This document defines the ten creative and operational roles that form the ScaenaShows production team. It is the **common brain** for all show authoring work.

**Claude** reads this document before any show work begins and holds all ten roles active during authoring. The production team is always at the table — not just when a role is explicitly invoked.

The **Show Director** leads the team. Every show begins with a brief from the Show Director — a distillation of the creative intent into direction that each department can act on. The Show Director makes show-level decisions when departments face tradeoffs, holds the arc when individual scenes pull in different directions, and approves the show before any in-game test.

**ShowSprite** (the future in-game AI show guide) draws its creative domain knowledge from this document. Its voice, persona, and player interaction style are defined separately in `kb/departments/voice/showsprite.context.md`. The knowledge is shared; the communication layer is not.

---

## How the Production Team Works

### Named Department Heads

Department heads have names. A named department head is not just a role label — they carry an identity, a disposition, and a working style that shapes how they bring their expertise to the production. **Kendra** runs Stage Management & Production. Other department heads will be named as their roles are developed. When Claude authors for a named department head, it speaks and thinks from that person's perspective — not as a neutral function, but as a person with opinions, judgment, and a particular way of working.

### Tone and Direction

The Director sets tone through experiential language — what the player *feels*, not what the plugin *executes*. Each department translates that tone into their instruments using their own KB. The translation methodology lives in each department's KB; the Director does not prescribe the how, only the what. When a department's interpretation misses the tone, the Director gives experiential feedback: "this feels clinical rather than tender." Departments own the mechanics.

### The Show Folder Framework

Every show has a folder at `src/main/resources/shows/[show_id]/`. The folder contains:

- `brief.md` — the Show Director's brief; the entry point everyone reads first
- `run-sheet.md` — Kendra's document: the human-readable call script, updated each revision
- `direction/` — the Show Director's working files: `show-direction.md`, `tone.md`, `intake.md`, `revision-log.md`
- `departments/` — one file per department: brief received, decisions, beat collision log (SM), revision notes

The `direction/` folder is the Show Director's working space — same relationship to the show folder that any department's subfolder has. The Director is not above the folder structure; they have their own corner of it.

### Department Intake and Revision Accountability

Every show opens with a **default intake conversation** — the Show Director asks each department a standing set of questions before any YAML is written. The intake record lives in `direction/[show_id].intake.md`. After each in-game test, each active department files a **revision debrief** in their `departments/[show_id].[dept].md` file. The Director synthesizes into `direction/[show_id].revision-log.md`. See `show-director.kb.md` for the full intake and revision accountability templates.

### Escalation Discipline

Each department head resolves problems within their authority and knowledge. They only escalate when a resolution requires a creative call above their scope, or would require another department to change what their work was intended to do. **Kendra (Stage Management & Production)** applies this most visibly: she resolves beat collisions when she can (offset, reorder, consolidate), and escalates to the Show Director with analysis and options when she cannot. This principle applies to all department heads. Departments don't bring problems — they bring resolved problems, or problems with proposed resolutions attached.

---

## Department Knowledgebases

Each department head maintains a technical knowledgebase — a dedicated file that documents what the Java plugin can do for that department, how to access those capabilities through YAML, and what limitations currently exist. The KB is the technical layer; this document is the creative/directorial layer. Both are required for show authoring.

**When to read a KB:** Before authoring any cue or show section that involves a specific department's tools. The KB is the authoritative reference for YAML syntax, behavioral notes, and capability awareness for that department.

**Java capabilities — ownership vs. awareness:** All departments need *awareness* of what the Java plugin can and cannot do. Only **Stage Management** *owns* the full current state of capabilities, limitations, and development priorities. Stage Management maintains the ops-inbox and coordinates all communication with the Java review team on behalf of the production team. Department KBs document capability awareness; `kb/departments/stage-management/stage-management.kb.md` is the authoritative source for what's implemented, what's not, and what's in the queue.

| Department | Head | Knowledgebase |
|------------|------|---------------|
| **Show Director** | Claude | `kb/departments/show-director/show-director.kb.md` |
| Casting Director | — | `kb/departments/casting/casting.kb.md` |
| **Wardrobe & Properties Director** | **Margaret** | `kb/departments/wardrobe/wardrobe.kb.md` |
| **Choreographer / Movement Director** | **Sharon** | `kb/departments/choreography/choreography.kb.md` |
| **Set Director** | **Michael C.** | `kb/departments/set/set.kb.md` |
| **Effects Director** | **Felix** | `kb/departments/effects/effects.kb.md` |
| **Camera Director** | **Mark** | `kb/departments/camera/camera.kb.md` |
| **Lighting & Atmosphere Designer** | **Steve N.** | `kb/departments/lighting/lighting.kb.md` |
| **Sound Designer** | **Brian** | `kb/departments/sound/sound.kb.md` |
| **Sprite Voice Director** | **ShowSprite** | `kb/departments/voice/voice.kb.md` |
| **Stage Management & Production** | **Kendra** | `kb/departments/stage-management/stage-management.kb.md` |
| **Fireworks Director** | **Mira** | `kb/departments/fireworks/fireworks.kb.md` |

---

## The Team at a Glance

| Role | Owns | Core Question |
|------|------|---------------|
| **Show Director** | The show brief; Show Direction; cross-department decisions; the arc | What is this show, and is every department serving that? |
| Casting Director | Performer selection, mob identity | Who is on stage, and what do they bring? |
| Wardrobe & Properties Director | Appearance, equipment, variants | What do they look like, and what does that say? |
| Choreographer / Movement Director | Movement as composition | When and where does each performer move, and why? |
| Set Director | Space, marks, sets, environment modifications | Where does this happen, and what does the space look like? |
| Effects Director | Player body: forced movement, physics, potion effects, particles | What is happening to the player's body — and is every physical sensation intentional? |
| Camera Director (Mark) | Player viewpoint: orientation, cinematic perspective, screen effects | Where is the player looking — and is that where they should be? |
| Lighting & Atmosphere Designer | World-state changes visible to all | What does the world feel like from inside this scene? |
| Sound Designer | Audio arc and landscape | What does the player hear, and what does the silence say? |
| Sprite Voice Director | All on-screen text | What words reach the player, in what mode, and when? |
| Stage Management & Production (Kendra) | Prompt book, running order, cue naming, cleanup contract, capability registry, show creation workflow, pre-production coordination | Is the show structurally sound and safe — and is every beat in the right place? |
| Fireworks Director | Fireworks — every detonation in the show | What detonates, where, at what altitude, and what does that moment mean? |

---

## Role Definitions

---

### 0. Show Director

**Domain:** The show as a whole. The Show Director owns the creative brief, the Show Direction, and the arc. The Director translates Alan's concept into a clear vision each department can act on, issues the Show Direction as a statement of show-critical priorities, resolves cross-department conflicts, and is responsible for whether the finished show delivers what the brief promised. Claude holds this role.

**The question this role asks:** What is this show, and is every department serving that?

**Authority:** The show brief. The Show Direction. Department briefings. Show-level creative decisions. The decision to send a show to in-game test. Veto and final call on cross-department conflicts. The production review synthesis.

**Alan's role:** Concept originator and creative vision holder. Alan's stated intentions and feedback are non-negotiables that enter the brief directly. The Director's job is to serve Alan's vision, not substitute for it.

**Knowledgebase:** `kb/departments/show-director/show-director.kb.md` (v2.1) — Director authority, Show Direction concept, tone language and feedback style, brief templates, standing department asks per department (intake + revision accountability), revision cycle guidance, arc evaluation questions, cross-department conflict protocol, and the pre-flight checklist. Effects / Camera intake and conflict patterns reflect current department structure.

---

#### The Brief and Show Direction

Every show begins with a brief. The brief is not a spec — it is a creative intent expressed clearly enough that each department head can ask the right questions and make the right choices independently.

A complete brief answers:
- **What is the emotional journey?** The arc, from the player's first moment to the last.
- **Who is the player in this show?** Witness, participant, subject, celebrant?
- **What is the tone?** (Not genre — the emotional register. Intimate vs. grand. Tender vs. unsettling.)
- **What does the player carry away?** The one thing they should feel or understand after.
- **What are the constraints?** Duration, setting, cue library access, known technical limits.

The **Show Direction** lives in `direction/[show_id].show-direction.md` within the show folder — a dedicated file, separate from the brief, that the Director keeps current throughout production. It is the Director's communication to all departments simultaneously: non-negotiables, known cross-department risks, and which departments carry elevated priority. See `show-director.kb.md` for the full Show Direction format and the `direction/` folder structure.

---

#### Department Briefings

Once the brief is written, the Show Director briefs each department head individually. Each briefing answers:
- What does this department's work serve in this show?
- What is the key decision this department needs to make?
- Are there any show-level constraints that narrow their choices?
- Are there any explicit creative requests from Alan?

Not every department needs a long briefing. A show with no performers needs a two-sentence Casting brief: "No performers in this show. Entity system is not used." Brevity is a sign of clarity.

---

#### Cross-Department Decisions

When two departments have competing claims on the same show moment, the Show Director decides — not by splitting the difference, but by asking which choice serves the arc. Common conflicts:

- Camera vs. Choreography: player needs to be in position X (choreography) but should be looking at Y (camera) — which matters more in this scene?
- Lighting vs. Voice: a scene requires darkness (lighting) but text won't read in the dark (voice) — does the text change, or does the lighting compromise?
- Sound vs. Voice: a sound cue lands at the same tick as a chat message — will they compete or complement?

The Show Director's answer always traces back to the brief and the Show Direction. "What serves the arc here?" If a non-negotiable from the Show Direction is in conflict, it wins.

---

### 1. Casting Director

**Domain:** Performer selection. Chooses which mob type appears in a scene based on what that mob *means* in the Minecraft world and what it contributes emotionally to the story. One Casting Director choice is worth a hundred lines of choreography.

**The question this role asks:** Who is this performer, and what does their nature do for free?

**Authority:** Entity type decisions in SPAWN_ENTITY and ENTER events. AI state decisions (puppet vs. performer). The choice of whether to use world-resident entities (CAPTURE_ENTITIES) or spawn new ones.

**Knowledgebase:** `kb/departments/casting/casting.kb.md` (v2.1) — Dramatis Personae mob register (organized by dramatic category), full instrument inventory (Fresh Spawn, Company Sweep, Theatrical Entrance/Exit, Puppet/Performer toggle), entity targeting reference, capability gaps with ops-inbox status, tone translation, department principles, and capability status table.

---

#### Mob Register — Dramatic Identity

Each entry covers: identity, behavior, dramatic register, known variants, and notes on puppet vs. performer use.

**Allay**
Small, hovering, follows and collects items. Plays a musical note when delivering. Visual language of joy, affection, and loyalty. A natural chorus member for joyful sequences. Puppet state keeps it in position; released AI causes wandering but adds genuine delight. No size variants.

**Bat**
Tiny, erratic flight, cave-ambient sounds. Near-invisible in dark environments. Registers as background texture — the sensation of being watched from darkness, secrets overhead. Most effective as a group (chorus). AI enabled = chaotic and atmospheric.

**Bee**
Small, buzzing, gentle glow. Day-active, returns to hive, stings once and dies. Visual register: industry, fragility, ecological dependence. A single bee near a flower reads very differently from a swarm. Tamed feeling when AI-disabled near a player.

**Cat**
Sits on beds, hisses at phantoms, gifts items after sleeping. Register: independence, mystery, comfort, the domestic uncanny. Multiple coat variants (see Wardrobe section). A sitting cat (ENTITY_AI disabled) is a composed, still presence. A walking cat introduces autonomous curiosity.

**Creeper**
Silent approach, brief hissing delay, explosion. Register: inevitable tension, the unannounced arrival of consequence. Best used as a visual element with AI disabled — the threat that holds perfectly still. Never release AI near players without extreme caution.

**Elder Guardian**
Floats, applies Mining Fatigue curse, emits ghostly scream. Register: an ancient force of affliction, the weight of the sea, something that sees you. The Mining Fatigue effect is a real player debuff — use deliberately as a storytelling tool (the curse moment), not accidentally.

**Enderman**
Tall, teleports when looked at, stares back, carries blocks. Register: the uncanny watcher, psychological tension, something that mirrors your attention. Teleportation behavior makes it unpredictable as a performer. Best used as a single figure at distance, AI disabled, placed to be noticed rather than to act.

**Fox**
Sneaks, steals items, leaps at prey. Register: cunning, the trickster, something not quite trustworthy. Baby fox: softens the read considerably — innocence or mischief.

**Iron Golem**
Large, slow, protector. Spawns in villages when population is threatened. Mourns dead villagers by placing poppies. Register: loyalty, weight as witness, the protector who outlived what it protected. A single Iron Golem standing still in a quiet scene carries enormous emotional weight without any dialogue.

**Phantom**
Aerial, swoops players who haven't slept, distinctive screech. Register: consequence, anxiety, the cost of sleeplessness. Most effective as a sound + silhouette in a dark scene. AI-released Phantoms will actively swoop — a calculated risk for tension sequences.

**Slime / Magma Cube**
Bouncing, splits on death, harmless in small sizes. Register: harmless individually, overwhelming in quantity. A single tiny slime is almost comedic. A room filling with them is claustrophobic. Size matters: large/medium/small via spawn mechanics (not currently YAML-controlled).

**Strider**
Walks on lava, trembles in cold. Register: alien landscape, isolation, the creature that lives in a place you cannot. Best in an environment with actual lava. Saddled variant adds a riding implication.

**Vex**
Tiny, phasing, conjured by Evokers. Sword-carrying ghost-form. Register: sorrow, intrusion, fragility with an edge. A chorus of Vex circling silently overhead reads as shadow made visible. Hard to control with pathfinding (phases through blocks).

**Villager**
Wandering, trading, schedule-following (work/sleep/wander). Register: community, labor, the ordinary world. *Profession is the primary expressive tool* — see Wardrobe section for profession visual register. Baby Villager: innocence, vulnerability. A crowd of Villagers with varied professions establishes a functioning society. A single Villager watching quietly is a witness.

**Warden**
Blind, sonic-detection, slow but nearly unkillable. Emits sonic boom attack. Register: inevitability, darkness incarnate, something that cannot be stopped — only avoided. AI disabled = the threat held perfectly still. Do not release AI near players.

**Wolf (tamed)**
Follows, attacks on command, shows loyalty visibly. Register: devotion, companionship, the bond between person and creature. A sitting wolf watching its owner is one of Minecraft's most emotionally loaded images. Various coat colors in 1.21+ (see Wardrobe).

**Armor Stand**
Not a mob — a static entity that holds equipment. Register: the absent body, the costume on a wire, the memorial. Can be posed. A suit of armor standing in a corner reads as presence without a person. AI disabled is the only mode. See Wardrobe section.

---

#### Puppet vs. Performer

| Mode | How | Effect | Use when |
|------|-----|--------|----------|
| Puppet | ENTITY_AI enabled: false | Holds position, full show control | Placing a figure, holding a tableau |
| Performer | ENTITY_AI enabled: true | Natural Minecraft behavior, unpredictable | Wildness, autonomy, chaos as atmosphere |
| Guided | CROSS_TO with pathfinder | Moves toward mark, arrival not guaranteed | Entrances, exits, slow crosses |
| Chorus | CAPTURE_ENTITIES group | Multiple entities as a unit | Crowds, swarms, ensemble atmosphere |

Natural behavior as a creative tool: an Allay that genuinely follows and collects reads as devotion in a way that scripted movement cannot replicate. Choose puppet when control is required; choose performer when authenticity is worth the unpredictability.

---

### 2. Wardrobe & Properties Director

**Department head: Margaret.** Her bias: *"Ask what they hold, not what they wear — the hand tells the story."* She thinks in objects before she thinks in costumes, approaches design from an object theater tradition, and escalates slowly. She will propose a minimal wardrobe first and add only what the scene requires. When something is unresolved, she asks the Director: *"Is this about the figure's relationship to their costume, or about an object's independent presence?"*

**Domain:** Appearance. Controls what performers wear and carry, which mob variants are used, and how visual appearance contributes to storytelling. Also manages Armor Stands as set pieces and the invisible-body technique.

**The question this role asks:** What do they look like, and what does that communicate before they take a single step?

**Authority:** All `equipment:` fields on SPAWN_ENTITY and ENTITY_EQUIP events. `variant:` and `profession:` fields on SPAWN_ENTITY. Armor Stand design and placement. Custom name visibility.

**Knowledgebase:** `kb/departments/wardrobe/wardrobe.kb.md` (v4.0) — Role summary, Margaret's voice and bias, instrument inventory, tone translation (10 entries including shadow, arrival, nocturnal, intimacy), department principles, cross-department coordination, and capability status table. Detailed technical catalogues in `kb/departments/wardrobe/` subfolder: `equipment-slots.md`, `invisible-body.md`, `mob-variants.md`, `emotional-register.md`.

---

#### Equipment Slots

All `LivingEntity` subtypes accept equipment. Fields:

| Slot | YAML key | Notes |
|------|----------|-------|
| Helmet | `helmet` | Any helmet material, or any item (pumpkin, mob heads, etc.) |
| Chestplate | `chestplate` | Armor materials only for full appearance |
| Leggings | `leggings` | |
| Boots | `boots` | |
| Main hand | `main_hand` | Any item: sword, hoe, torch, banner, food, map, etc. |
| Off hand | `off_hand` | Any item: torch for ambient light, shield, etc. |

Equipment can be set at spawn (`equipment:` in SPAWN_ENTITY) or changed mid-show (`ENTITY_EQUIP`). A wardrobe change mid-scene is a character transformation moment.

**Non-standard helmet uses:** Carved pumpkin gives a distinctive silhouette. Player heads (mob head items) create unusual character reads. A flower pot on a villager's head is absurd and memorable.

**Main hand as storytelling:** What a performer holds is the first thing the eye reads after their face. A sword reads as threat. A torch reads as guide or warmth. A book reads as scholar. A hoe reads as labor. A banner reads as herald. A dead bush held in hand reads as something quietly devastating. Design the main hand prop before anything else.

---

#### The Invisible Body Technique

Apply ENTITY_INVISIBLE (or ENTITY_EFFECT invisibility) to any LivingEntity. Their body disappears; their held items and armor remain visible. Results:

- Floating sword or tool in space
- Disembodied helmet or armor piece (atmospheric "ghost armor")
- A torch that moves on its own
- A banner carried by air

This technique is fully available with current tooling. Use it for ethereal, uncanny, or purely atmospheric purposes.

---

#### Armor Stand as Set Piece

Armor Stands accept all equipment slots and can be posed (pose currently requires COMMAND — see Java Gaps). They are defined in the spec glossary as "set pieces." Uses:

- A full suit of armor standing in a corner: the absent knight, the memorial, the empty throne
- An item displayed at eye height: a relic, an offering, a prop
- Multiple stands in a row: a chorus of silent witnesses

Always spawn with `despawn_on_end: true` unless the stand is a permanent world fixture.

---

#### Mob Variants

> ⚠️ **Java Gap — see Issue #[variant-gap]:** `variant:` and `profession:` are parsed by the YAML model but not yet applied in the executor. Fields are silently ignored at runtime until the gap is resolved.

Once resolved, the following variants will be available:

**Villager professions** (visual register in parentheses):
- `FARMER` — straw hat (rural, land)
- `LIBRARIAN` — spectacles (knowledge, scholarship)
- `CLERIC` — purple robes (ritual, mystery, the sacred)
- `ARMORER` — black apron (craft, protection, industry)
- `BUTCHER` — white apron, pink hair (labor, the ordinary)
- `CARTOGRAPHER` — monocle (exploration, record-keeping)
- `FLETCHER` — feathered cap (craft, precision)
- `TOOLSMITH` / `WEAPONSMITH` — smithing apron (making things)
- `NITWIT` — green robes (the fool, innocence, the one who doesn't belong)

**Villager biome types:** PLAINS, DESERT, JUNGLE, SAVANNA, SNOW, SWAMP, TAIGA — each has a distinct skin palette. A Snow Villager in a desert scene is subtly wrong in a useful way.

**Cat variants:** TABBY, BLACK, RED, SIAMESE, BRITISH_SHORTHAIR, CALICO, PERSIAN, RAGDOLL, WHITE, JELLIE, ALL_BLACK.

**Horse variants:** WHITE, CREAMY, CHESTNUT, BROWN, BLACK, GRAY, DARK_BROWN. Plus marking overlays (NONE, SOCKS, WHITE_DOTS, BLACK_DOTS, WHITE).

**Wolf coat colors (1.21+):** Multiple variants available. The pale wolf is the default; others carry distinct visual registers.

**Sheep wool colors:** All 16 dye colors. A black sheep is a classic; a chorus of mixed-color sheep reads as abundance or chaos depending on context.

**Tropical Fish:** Enormous pattern and color variety — each fish is individually configurable. Useful for visual texture in aquatic scenes.

**Parrot colors:** RED, BLUE, GREEN, CYAN, GRAY. A parrot on a figure's shoulder immediately reads as pirate, explorer, or companion.

---

### 3. Choreographer / Movement Director

**Domain:** Performer movement. The Choreographer owns the movement score for cast members and NPCs in the show — when each performer moves, where they go, how they move relative to each other, and what the spatial story looks like tick by tick. Casting Director chose *who* — Choreographer decides *where and when*.

**This department does not own the target player's movement.** Levitation, teleportation, velocity impulses, and any other forced movement applied *to the show's target player* belongs to the Effects department. The Choreographer designs for entities and participant cast members; Effects designs what the audience experiences in their own body.

**The question this role asks:** What does the movement say, and does every performer cross serve the story?

**Authority:** SPAWN_ENTITY and DESPAWN_ENTITY for cast member lifecycle. ENTITY_AI (puppet vs. performer decision). CAPTURE_ENTITIES and RELEASE_ENTITIES for chorus groups. CROSS_TO, ENTER, EXIT, RETURN_HOME, HOLD, FACE events targeting entities and non-target players. ENTITY_SPEED and ENTITY_VELOCITY for movement dynamics. The full NPC lifecycle and the timing and sequence of movement events for cast and chorus members across the show timeline.

**CROSS_TO boundary note:** When a CROSS_TO targets a performer crossing *toward* the target player's location, that is Choreography. When a CROSS_TO is applied *to the target player themselves* as forced movement, that is Effects.

**Knowledgebase:** `kb/departments/choreography/choreography.kb.md` (v2.1, Sharon named head) — Full NPC lifecycle (spawn, AI state, movement, despawn), instrument inventory with Java verification, NPC type movement notes by entity type, cue library naming structure and design process, tone translation methodology, cross-department collaboration points, and all known gaps with workarounds.

---

#### Movement Vocabulary

| Event | Type | Notes |
|-------|------|-------|
| CROSS_TO | Point or bar | Move entity to a mark. `duration_ticks` > 0 = gradual (pathfinder for mobs). `facing:` sets orientation on arrival. When applied to the target player as forced movement, see Effects. |
| ENTER | Bar | Spawn at a wing mark and move to destination. Semantic shorthand for arrival. |
| EXIT | Bar | Move to a wing mark, optionally despawn on arrival. Semantic shorthand for departure. |
| HOLD | Point | Freeze entity at current position (zeroes velocity). |
| FACE | Point | Turn entity to face a mark, compass direction, or entity. Yaw only — see Camera Director for pitch limitations. |
| RETURN_HOME | Point or bar | Return each participant to their captured invocation location. |
| ENTITY_SPEED | Point | Scale entity movement speed. 0.0 = stopped, 1.0 = normal, 2.0 = fast. |
| ENTITY_VELOCITY | Point | Launch entity in a vector. Combine with slow_falling for controlled arcs. Does not apply to the target player — see PLAYER_VELOCITY in Effects. |

**Pathfinder note:** CROSS_TO for mobs uses Bukkit's pathfinder API — arrival is not guaranteed on complex terrain. Pathfinding mobs will navigate around obstacles but may get stuck. Design mob crosses for open space or use shorter distances. Puppet (AI disabled) mobs do not pathfind at all.

---

#### Movement as Storytelling

A cross from upstage to downstage is an approach — a choice to come closer. A cross away from a mark mid-scene is withdrawal, hesitation, rejection. Two performers crossing simultaneously in opposite directions creates collision energy. A figure that holds while everything moves around it has weight and stillness as a statement.

**Entrances and exits** are never neutral. When the performer enters, what is the first thing they see? Where does the player's eye go? When they exit, does the world close around where they stood, or does the space immediately fill with something else?

**The player's movement** is part of the score even when the show doesn't control it. If the player can move freely, the Choreographer should design for the most likely path of attention. If the show uses CROSS_TO on the player, those movements are scripted and the Choreographer owns them fully.

**Simultaneous movement:** Multiple entities moving at once creates composition. The Choreographer should think in frames: at tick 120, where is everyone on stage?

---

#### Approximate Movement Timing

These are rough guides for design; actual timing depends on terrain and entity type:

| Distance (blocks) | Speed (normal) | Approximate ticks |
|-------------------|---------------|-------------------|
| 5 (stage cross) | 1.0 | ~25t |
| 10 (half stage) | 1.0 | ~50t |
| 20 (full stage) | 1.0 | ~100t |
| 5 (slow, dramatic) | 0.4 | ~60t |

Player smooth movement (smoothMovePlayer) is tick-exact. Mob pathfinding timing is approximate.

---

### 4. Set Director

**Domain:** Space and environment. The Set Director owns where shows happen, how the playing space is structured, how players move between locations, and what physical modifications are made to the world — including the discipline of cleaning them up.

**The question this role asks:** Where does this happen, and does the space serve the story?

**Authority:** `marks:` and `sets:` in show YAML. PLAYER_TELEPORT for set transitions. The `front:` orientation of the show. Block modification planning and cleanup. Spatial documentation of the show environment.

**Named head:** Michael C. See `kb/departments/set/set.kb.md §Michael C.` for working style and escalation posture. Zarathale executes all in-game scouting and building; Michael C. owns the design voice and documentation.

**Knowledgebase:** `kb/departments/set/set.kb.md` (v2.1) — Michael C. introduction, P1 Scouting Process, Environment Notes format, full instrument reference with Java verification (PLAYER_TELEPORT, REDSTONE, COMMAND, marks, sets), Tone Translation, Department Principles, Capability Status Summary. Active gaps: BLOCK_PLACE/BLOCK_REMOVE (filed), REDSTONE stop-safety (filed). Stage Registry: `kb/departments/set/stage-registry.md`.

---

#### The Spatial Hierarchy

```
World
└── Sets (named world-specific locations — absolute coordinates)
    └── Stage (the playing area within a set)
        └── Marks (named XZ positions relative to anchor)
            └── Wings (off-stage entry/exit zones, beyond playing area)
```

**Sets** (`sets:` in show YAML) are world-specific named locations with absolute XYZ, yaw, and pitch. Players teleport between sets via PLAYER_TELEPORT. The `return_on_end: true` flag ensures players return to their pre-show location when the show ends, stops, or the player disconnects. Always use `return_on_end: true` on any set that moves players away from their starting position.

**Marks** (`marks:` in show YAML) are named XZ offsets relative to the show anchor. They are portable — the same show plays correctly regardless of where it is invoked. Standard 9-position grid:

```
UL (upstage_left) | UC (upstage)  | UR (upstage_right)
SL (stage_left)   | CC (center)   | SR (stage_right)
DL (down_left)    | DC (downstage)| DR (down_right)
```

Wings are placed beyond the playing area boundary: `wing_left`, `wing_right`, `wing_up`, `wing_down`.

**Important: marks are XZ only** — they have no Y coordinate. Vertical staging (positioning entities or players at a specific height) requires hardcoded Y values in PLAYER_TELEPORT or CROSS_TO events, not mark references. Document vertical positions separately in the show's spatial notes.

**Home** is each participant's captured location at show invocation — a runtime value, not a YAML value. Available as `destination: home` in any positional event.

**Stage front** (`front:` in show YAML) defines which compass direction is downstage. Without it, stage_left/stage_right have no meaning. Default is the primary player's facing direction at invocation.

---

#### Block Modifications and the Cleanup Contract

> ⚠️ **Java Gap — see Issue #[block-modification-gap]:** There is no BLOCK_PLACE or BLOCK_REMOVE event type. All block modifications must currently be executed via COMMAND (escape hatch). COMMAND-placed blocks are outside the show's stop-safety contract — they are not automatically restored if the show is interrupted.

**Until the gap is resolved, the Set Director's protocol is:**

1. For any block modification placed via COMMAND at tick N, write a corresponding cleanup COMMAND that restores the original block state.
2. The cleanup COMMAND must appear in two places: at the natural show end, AND in a dedicated cleanup cue (e.g., `fx.set.cleanup.[show_id]`) that can be invoked manually if the show is interrupted.
3. Document every block modification in the show's run sheet with: tick placed, world coordinates, original block type, replacement block type.
4. Never use block modifications in rehearsal-mode shows unless cleanup is fully scripted and confirmed working.

**Armor Stands and Display Entities as set pieces** are the preferred alternative to block modifications when possible — they use the entity system (despawn_on_end: true) and are fully covered by the cleanup contract.

---

#### Spatial Documentation

Before authoring any show, the Set Director should document:
- What is built in the show space? (key structures, barriers, ceiling height)
- What are the sight lines? (what can the player see from center stage, from each wing)
- Are there unintended pathfinding obstacles that could disrupt mob crosses?
- What is the sky like? (open, enclosed, underground) — affects TIME_OF_DAY legibility
- What is the ambient light level? — affects particle and effect visibility

This lives in the show's run sheet under "Environment Notes," not in the YAML.

---

### 5. Effects Director

**Domain:** The player's body. The Effects Director owns all forced movement of the show's target player (teleportation, levitation, velocity impulses, flight), all physics-layer alteration (potion effects), and particles applied to the player's space. If it moves, lifts, slows, or physically changes the player — Effects owns it.

**The question this role asks:** What is happening to the player's body at each moment — and is every physical sensation intentional?

**Authority:** PLAYER_TELEPORT destination (position authority; yaw/pitch fields are Camera's), PLAYER_FLIGHT (hover and release states), PLAYER_VELOCITY (impulse), CROSS_TO targeting the show's target player (position authority; `facing:` field is Camera's), EFFECT events on players (levitation, slow_falling, night_vision, blindness, darkness, nausea, speed, slowness), PARTICLE events.

**Relationship to Camera:** Effects and Camera are the closest coordination pair. Effects decides where the player's body is; Camera decides which direction their eyes are pointing. Every PLAYER_TELEPORT with facing fields and every levitation sequence is a joint production. See `kb/departments/camera/camera.kb.md`.

**Named head:** Felix. See `kb/departments/effects/effects.kb.md §Felix` for working style and escalation posture.

**Knowledgebase:** `kb/departments/effects/effects.kb.md` — Java capabilities, YAML syntax, calibrated levitation patterns, particle vocabulary, tone translation, and known gaps.

**CAMERA boundary note:** CAMERA screen distortion events (`blackout`, `sway`, `flash`, `float`) belong to Camera (Mark), not Effects. Felix does not author CAMERA events. The overlap point: EFFECT `nausea` and CAMERA `sway` both produce screen wobble — Effects owns the potion application; Camera owns the screen-effect shorthand. Coordinate when both are in proximity.

---

#### What Effects Owns vs. What It Doesn't

| Belongs to Effects | Belongs to Camera | Belongs to Choreography | Belongs to Lighting |
|-------------------|------------------|------------------------|---------------------|
| Forced player movement (TP destination, levitate, velocity) | Player orientation (FACE, yaw/pitch on TP) | Performer/NPC movement | TIME_OF_DAY, WEATHER, LIGHTNING |
| EFFECT on target players | PLAYER_SPECTATE / MOUNT | ENTITY_VELOCITY for entity impulses | — |
| PARTICLE (applied to player space) | CAMERA screen effects | ENTITY_SPEED | — |

**night_vision cross-discipline note:** `night_vision` is an EFFECT applied to the player (Effects authority), but it makes darkness visible — fundamentally altering the ambient light state the Lighting designer is crafting. Any scene that uses `night_vision` requires coordination between Effects and Lighting to ensure the revealed environment is intentional.

---

#### Forced Player Movement

**PLAYER_TELEPORT** — the primary set-transition tool. Moves the player to an absolute destination or relative offset. Destination is Effects authority. The `yaw` and `pitch` fields on arrival are Camera authority — coordinate with Mark on any transition that also resets orientation.

**PLAYER_FLIGHT** — server-side flight control. `hover` engages flight and locks the player's altitude at their current position. `release` disengages flight and applies a transition effect (slow_falling by default) before restoring the player's pre-show flight state.

**PLAYER_VELOCITY** — one-time vector impulse to the player. Combine with EFFECT `slow_falling` immediately after for a controlled float arc. The player can partially counteract the impulse by moving.

**Calibrated levitation patterns** (derived from in-game calibration sessions):

| Pattern | Configuration | Effect |
|---------|--------------|--------|
| HOVER | levitation amp 0, lev=20t gap=8t cycle=28t | Clean altitude hold |
| CLIMB | levitation amp 0, lev=24t per cycle | Gradual upward drift |
| RELEASE | levitation amp 0, lev=20t gap=24t cycle=44t | Slow controlled descent |

Always use a lift event (EFFECT levitation or PLAYER_VELOCITY) before PLAYER_FLIGHT hover — hover freezes at *current* altitude. Get the player to the right height first, then lock with hover.

---

#### Perceptual Effects on Players

These EFFECT events alter how the player perceives the world:

| Effect | Impact | Use |
|--------|--------|-----|
| `night_vision` | Sees in darkness at full brightness | Reveal a dark space without lighting it — coordinate with Lighting |
| `blindness` | Severe vignette, short sight distance | Disorientation, transition, fog of experience |
| `darkness` | Pulsing dark vignette | Dread, weight, the Warden's domain |
| `nausea` | Screen wobble | Disorientation (same as CAMERA sway) |
| `levitation` | Upward drift; amp 0 = gentle, amp 9 = surge | Aerial staging; primary levitation instrument |
| `slow_falling` | Gravity reduced | Transition from flight; prevents hard landing after levitation |
| `speed` | Movement faster | Chase, urgency, tension |
| `slowness` | Movement slower | Weight, stillness, time stretching |

---

#### Camera Specialty — Orientation and Perspective

Full YAML reference lives in `kb/departments/camera/camera.kb.md`. Summary of what Camera brings to Effects:

**Camera modes** (establish at show design time, before authoring):
- **Full control** — camera managed throughout: FACE, PLAYER_SPECTATE, teleport facing
- **Partial control** — camera handed over for specific scenes only
- **Player-free** — player controls their own camera; show sets initial facing only

**FACE** — instant yaw rotation toward a target. Yaw only (no pitch). For pitch control, use PLAYER_TELEPORT with explicit pitch value.

**PLAYER_TELEPORT with facing** — most reliable way to set both yaw and pitch at once.

**PLAYER_SPECTATE + drone pattern** — attach the player's camera to an invisible entity, move the entity with CROSS_TO, achieve a smooth cinematic traveling shot. The established technique for smooth camera pans.

**CAMERA event** — screen-level perceptual distortion: `blackout` (scene transition), `sway` (vertigo), `flash` (revelation), `float` (weightlessness sensation).

> ⚠️ **Java Gap — see Issue #[face-pitch-gap]:** FACE only computes yaw. Pitch is not set. Use PLAYER_TELEPORT with explicit pitch as the workaround.

> ⚠️ **Java Gap — see Issue #[smooth-rotate-gap]:** No gradual yaw rotation primitive. Use PLAYER_SPECTATE on a moving entity for smooth pans.

---

### 6. Lighting & Atmosphere Designer

**Domain:** World-state changes visible to everyone. The Lighting & Atmosphere Designer controls the time of day, weather, and lightning — the environmental conditions that form the backdrop of the entire scene. Lighting is not decoration — it is the emotional context that everything else lives inside.

**The question this role asks:** What does the world feel like from inside this scene, and is the lighting arc serving the emotional arc?

**Authority:** TIME_OF_DAY, WEATHER, and LIGHTNING events. These are server-wide events that affect all players, not just show participants. Firework color palette as a lighting consideration (joint awareness with Fireworks — the Lighting Designer does not author firework events but is consulted on palette choices that affect the ambient scene register).

**What does NOT belong here:** Particles applied to the player's space, perceptual effects on the player (night_vision, blindness, darkness), and CAMERA screen distortion all belong to the Effects department. The boundary is: if it changes the world state visible to all players, it's Lighting. If it's applied onto a specific player, it's Effects.

**night_vision coordination:** `night_vision` is an Effects instrument, but it reveals the environment that Lighting is crafting. Any scene that uses `night_vision` requires joint awareness — Effects applies it; Lighting ensures the revealed environment is intentional.

**Named head:** Steve N. See `kb/departments/lighting/lighting.kb.md §Steve N.` for working style and escalation posture.

**Knowledgebase:** `kb/departments/lighting/lighting.kb.md` (v2.1) — Steve N. introduction, Arc Design methodology, full Sky Arc Reference (all TIME_OF_DAY values with sky quality descriptions), instrument inventory with Java verification (TIME_OF_DAY, WEATHER, LIGHTNING), tone translation including "Weight / shadow", department principles, and capability status table. Note: LIGHTNING uses `offset: {x, y, z}` (all relative to anchor) — not absolute coordinates.

---

#### Time of Day

TIME_OF_DAY is a point-in-time event — it snaps the world clock to the specified value instantly. Gradual transitions are composed by firing multiple TIME_OF_DAY events in sequence.

```
0 (or 24000) = sunrise — light returning, new beginning
1000         = early morning — clarity, hope
6000         = noon — full daylight, maximum visibility, no shadows
12000        = sunset — warmth fading, transition
13000        = dusk — liminal, the moment before dark
18000        = midnight — full dark, the deepest night
```

**Design note:** The R7 archetype sampler uses three TIME_OF_DAY steps at C1 (8000 → 13000 → 18000) to create gradual dusk during the opening dialogue. Multiple events across 40–80 ticks each reads as a continuous fade rather than snaps.

Darkness itself (values 13000–23000) matters differently depending on the environment: underground, a night sky makes no difference. Outdoors, it fundamentally changes the world's visual register.

---

#### Weather

WEATHER fires as a bar event (duration_ticks optional). Three states:
- `clear` — full visibility, natural skybox
- `storm` — rain/snow, reduced visibility, ambient sound
- `thunder` — storm + thunder + lightning strikes (random, from server — distinct from scripted LIGHTNING events)

Weather changes the ambient sound layer significantly even before any scripted sound is added. A `storm` state creates a bed of rain sound that the Sound Designer should account for.

---

#### LIGHTNING

Cosmetic-only lightning strike at an offset from the anchor. No damage, no fire (per spec §15). Uses: a single dramatic flash at a specific location, multiple simultaneous strikes for chaos, a slow sequence for ominous punctuation. LIGHTNING lives here because it is an atmospheric event — a change to the sky and world register. The thunder is tied to the visual strike and cannot be separated; coordinate with the Sound Designer on any scene that uses LIGHTNING.

---

#### Lighting Arc

As with all other creative dimensions, the Lighting Designer should establish the arc before authoring events. Questions to answer at show design:

- What is the opening light state? (time + weather)
- Does the show move through distinct lighting zones, or is the light world static?
- Where are the key lighting moments? (the moment the sun rises, the storm breaks, the world goes dark)
- What is the closing light state, and is it different from opening? Does it need to be restored?
- Is the final state restored to the player's original environment, or does the show intentionally leave the world changed?

---

### 7. Sound Designer

**Domain:** The audio landscape. Owns what the player hears, how sounds layer, when silence is used, and how the arc of audio across the show serves the story. Sound is the layer the player feels even when they are not consciously noticing it.

**The question this role asks:** What does the player hear at each moment, and what does the silence between sounds mean?

**Authority:** SOUND and STOP_SOUND events throughout the show timeline. The audio arc — decisions about layering, sequencing, and deliberate silence.

**Named head:** Brian. See `kb/departments/sound/sound.kb.md §Brian` for working style and escalation posture.

**Knowledgebase:** `kb/departments/sound/sound.kb.md` (v2.2) — Brian introduction, Java capabilities, YAML syntax, behavioral notes, sound ID reference, known limitations of the Minecraft sound system, and the full roster of named instrumentalists with their gesture vocabularies. Extended reference: `kb/departments/sound/music-director.md`.

**The Music Director** is a specialist within the Sound department who advises on and authors the musical layer specifically — note block arrangements, motifs, riffs, and ensemble deployment. The Sound Designer owns the audio arc; the Music Director is called when a scene needs something with pitch, rhythm, and musical identity. The Music Director calls the ensemble. See `kb/departments/sound/music-director.md` for the note block instrument palette, pitch/harmony reference, world-built redstone arrangement workflow, and starter motif library (`motif.*` namespace, 5 cues).

**The ensemble:** The Sound department is staffed by named theatrical musicians who stand by throughout every production. They respond live to the show's needs — punctuating moments, supporting transitions, providing live musical texture beneath scripted SOUND events. Their gestures are available as named cues and can be summoned by the Show Director, Sound Designer, or Music Director at any point in the show.

- **Gracie the Harpist** — the resident harpist. Always in the wings. Her vocabulary: high-register whole-tone glissandi (dreamy swirl or sharp accent), a low ominous sustained whole-note, and a two-note percussive plink in ascending fourth or fifth. See `kb/departments/sound/sound.kb.md §Sound Department Personnel` for full gesture reference and YAML patterns. Her cues are in the library under the `gracie.*` namespace.

---

#### SOUND Event Parameters

```yaml
type: SOUND
sound_id: minecraft:entity.allay.ambient_with_item
category: ambient       # master | music | record | weather | block | hostile | neutral | player | ambient | voice
volume: 1.0             # 0.0–1.0 controls loudness; > 1.0 expands broadcast radius for nearby non-participants
pitch: 1.0              # 0.5–2.0 (1.0 = natural pitch; 2.0 = octave up, double speed)
max_duration_ticks: 80  # optional; stops the sound after N ticks (hard cut, no fade)
```

Sound plays at each participant's location (`p.getLocation()`). Every participant hears the sound as if it originates from where they are standing — there is no anchor-relative offset. Volume (0.0–1.0) controls perceived loudness; values above 1.0 expand the broadcast radius so nearby non-participants may also hear it.

**Pitch as a tool:** Lowering pitch (0.6–0.8) makes a sound feel heavier, more ancient, more ominous. Raising pitch (1.2–1.5) makes it brighter, more urgent, more fragile. A distant thunder sound pitched down to 0.6 becomes subterranean. An allay note pitched up to 1.8 becomes a tiny, urgent chime.

---

#### Sound Categories

Category controls which player audio channel the sound goes through. Players can have individual channel volumes. Key categories:

- `ambient` — environmental sounds (cave, rain, birds)
- `hostile` — mob threat sounds
- `neutral` — neutral mob sounds
- `music` — music disc / background music channel
- `master` — overrides all channel settings

For show sounds, `ambient` and `master` are most common. Use `master` for sounds that must be heard regardless of player settings.

---

#### Useful Sound IDs

A selection for common show situations (full list at minecraft.wiki/sounds):

**Atmosphere:**
- `minecraft:ambient.cave` — cave ambience, unease
- `minecraft:ambient.basalt_deltas.loop` — alien, volcanic
- `minecraft:ambient.crimson_forest.loop` — otherworldly, unsettling
- `minecraft:weather.rain` — rain without actual weather change

**Presence / arrival:**
- `minecraft:entity.elder_guardian.curse` — the curse sound, weight, significance
- `minecraft:entity.warden.heartbeat` — dread, proximity to something large
- `minecraft:entity.ender_dragon.growl` — ancient, powerful
- `minecraft:block.portal.ambient` — the threshold, the in-between

**Warmth / joy:**
- `minecraft:entity.allay.ambient_with_item` — small delight
- `minecraft:entity.villager.celebrate` — community, festivity
- `minecraft:block.note_block.bell` — clarity, punctuation, ceremony

**Transition:**
- `minecraft:entity.lightning_bolt.thunder` — punctuation, consequence
- `minecraft:block.beacon.activate` — rising, a thing beginning
- `minecraft:entity.experience_orb.pickup` — arrival, completion
- `minecraft:block.end_portal.frame.fill` — the uncanny, a threshold crossed

---

#### STOP_SOUND

Stops all sounds on a given channel (or all channels) for participants. Does **not** stop a specific sound by ID — stopping is by `source` channel only. Critical for:
- Ending a looping ambient bed before a scene transition
- Cutting sound abruptly for dramatic silence
- Clearing a sound layer before a new one begins

Always plan STOP_SOUND events alongside their corresponding SOUND events in the show timeline. An ambient loop that isn't stopped will continue playing past its intended scene.

---

#### Silence as Composition

The absence of sound is a creative decision. A 40-tick REST with no sounds firing, following a loud moment, is a held breath. A quiet entrance — no sound cue, just a figure appearing — can read as more unsettling than a dramatic sound hit.

The Sound Designer should mark deliberate silences in the run sheet, not just let them happen by default. If a moment of silence is intentional, it should be noted as "deliberate audio silence — do not fill."

---

#### Audio Arc

Before authoring SOUND events, establish:
- What is the ambient audio bed at opening? (no sound is also a choice)
- Where are the key sound moments? (the hit that lands with a visual)
- How do sounds layer? (is there ever more than one thing playing at once, and is that intentional?)
- Where are the silences, and are they deliberate?
- What is the audio state at show end? (all sounds stopped, or one thing lingering?)

---

### 8. Sprite Voice Director

**Domain:** All text delivered to the player on behalf of the show. Manages what is said, which mode it's delivered in, who receives it, when it appears, and the deliberate spaces between words. Owns the text arc across the whole show.

**The question this role asks:** What reaches the player's eyes, in which mode, and why does this moment deserve words at all?

**Authority:** MESSAGE, TITLE, ACTION_BAR, BOSSBAR events throughout the show timeline. Audience targeting decisions for text. The timing of text including deliberate silent passages.

**Knowledgebase:** `kb/departments/voice/voice.kb.md` (v2.2) — ShowSprite named head introduction, role summary, instrument inventory with Java verification (MESSAGE, TITLE, ACTION_BAR, BOSSBAR), audience targeting reference, tone translation, department principles, cross-department coordination, and capability status table. ShowSprite persona: `kb/departments/voice/showsprite.context.md`.

---

#### The Four Text Modes

**MESSAGE — Chat**
- Persists in chat history; the player can scroll back
- Intimate register — reads as something said, not proclaimed
- Supports MiniMessage format (`<white>`, `<gold>`, `<dark_gray>`, `<italic>`, etc.) — this is the authoring standard for all show text
- Audience targeting: full range including `private`, `target`, `group_1` — private text is a storytelling tool
- Cannot be dismissed mid-show; it exists in the log

*Use for:* ShowSprite dialogue, intimate address, text the player should be able to return to, multi-line thoughts that unfold one message at a time.

**TITLE — Fullscreen**
- Large centered text with subtitle below
- Two text lines maximum: `title:` and `subtitle:`
- Three timing parameters: `fade_in:`, `stay:`, `fade_out:` (in ticks)
- Cinematic register — the screen is the stage for this moment
- Erasing before natural expiry requires a new TITLE with empty strings (resets the fade clock)

*Use for:* Cinematic scene titles, a single arrival word ("You are here"), the name of something important, the final word of a show. Reserve it — overuse deflates its impact.

*Timing as craft:* `fade_in: 0, stay: 10, fade_out: 60` reads as a flash of recognition. `fade_in: 40, stay: 80, fade_out: 40` is a slow revelation. These choices are as important as the words themselves.

**ACTION_BAR — Above Hotbar**
- Single line, auto-fades approximately 2 seconds after last update
- Refreshable: fire a new ACTION_BAR within 2 seconds to keep it present
- Subtle register — reads as ambient guidance, quiet real-time awareness
- Does not persist; vanishes without a trace

*Use for:* Stage directions whispered to the player ("Look up."), status that should fade naturally, ambient ongoing information that shouldn't linger. Not for important text the player might miss.

**BOSSBAR — Top of Screen**
- Persistent bar across the top of the screen for the event's full `duration_ticks`
- Has `color:` (PINK/BLUE/RED/GREEN/YELLOW/PURPLE/WHITE) and a progress bar that fills over the duration
- Structural register — reads as the show's running context, the chapter marker

*Use for:* Section/scene headers that orient the player across a long show, structural labels that should persist across multiple cue firings, the "act title" that lingers. The archetype sampler uses BOSSBAR for section identification (C1–C13) with color-coded mood. This is the established pattern.

---

#### Text Arc — The Bossbar as Structure

The canonical use: one BOSSBAR per section, color-coded by emotional register, progress bar fills over the section duration. The chat carries the voice. The bossbar carries the structure. These two layers should never compete — the bossbar establishes context so the chat can speak freely within it.

---

#### Audience Targeting for Text

All text modes support audience targeting. This is underused as a storytelling tool:

- `private` / `target` — only this player sees it. Creates a sense of personal address that broadcast text cannot replicate. "You, specifically, are seen."
- `group_1` / `group_2` — different players in the same show see different text. Possible divergent experiences in a multi-player show.
- `participants` — everyone in this show instance, no one outside it

MESSAGE also supports `entity:spawned:Name` targeting — text appears to reach a named entity. Unusual, but available.

---

#### Silence as Voice

The deliberate absence of text is a creative decision that must be made, not defaulted into. A 120-tick passage with no MESSAGE events is either a thoughtful breath or an empty gap — the difference is intentionality.

When designing a show's text arc, mark the silences: "No text from T=240 to T=380 — the player is watching, not reading." This belongs in the run sheet, not the YAML.

> ⚠️ **Java Gap — see Issue #[text-clear-gap]:** There is no TITLE_CLEAR event. To cut a TITLE before its stay timer expires, fire a new TITLE with empty strings, which resets the fade clock. This is a workaround, not a tool. Until resolved, design TITLE timing so early dismissal is not needed.

---

### 9. Stage Management & Production

**Kendra runs Stage Management & Production.** This role carries two integrated functions: Stage Manager (structural show integrity, prompt book, cleanup contract, beat collision, ops-inbox, run sheets) and Production Manager (pre-production workflow, department briefing coordination, YAML authorship sequencing, show creation process).

**Domain:** Show integrity, operational discipline, and the pre-production workflow. Kendra owns the show YAML's running order and structure — the prompt book — and the cleanup contract: the guarantee that every show, including interrupted and rehearsal performances, leaves the world exactly as it found it. She also owns the Java capability registry, the ops-inbox workflow, and the show creation process from concept acceptance through in-game test.

**The question this role asks:** Is the show structurally sound and safe — and is every beat in the right place?

**Authority:** The prompt book (running order, cue naming, cue numbering, YAML structure). The cleanup contract. Pre-show state recording and tech check. Block modification protocol enforcement. Beat collision detection and resolution. Run sheet authoring and maintenance. Java gap escalation to the ops-inbox. Show creation workflow. Department briefing coordination. YAML authorship sequencing and gates.

**Kendra does not bring problems — she brings resolved problems, or problems with proposed resolutions attached.** Escalation to the Show Director happens only when a resolution requires a creative decision she isn't authorized to make, or requires a department to change what a beat *is*.

**Knowledgebase:** `kb/departments/stage-management/stage-management.kb.md` (v3.0) — Prompt book ownership, cue naming conventions, beat collision protocol, revision continuity, run sheet format, what stop-safety covers, what it does not, pre-show tech check, rehearsal safety checklist, ops-inbox workflow, Production Manager role, show creation workflow, department briefing coordination, YAML authorship sequencing, and Tone Translation (structural/pacing). Show creation process: `kb/departments/stage-management/show-creation-process.md`.

---

#### What Stop-Safety Covers

The show's built-in stop safety (`applyStopSafety`) handles the following automatically on any show end (natural, stopped, or interrupted):

| Item | How it's handled |
|------|-----------------|
| Named spawned entities with `despawn_on_end: true` | Removed |
| Player SPECTATOR mode from PLAYER_SPECTATE | Restored to pre-show gamemode |
| Player flight state from PLAYER_FLIGHT hover | Restored to pre-show flight state |
| Slow_falling effect after PLAYER_FLIGHT release | Applied transitionally |
| Show-owned teams (GLOW, TEAM_COLOR) | Unregistered |

---

#### What Stop-Safety Does NOT Cover

These items require explicit cleanup by the authoring team:

| Item | Risk | Mitigation |
|------|------|-----------|
| Blocks placed via COMMAND | Permanent world modification | Cleanup COMMAND + run sheet documentation (Set Director protocol) |
| REDSTONE state changes via REDSTONE event | Redstone circuit left in wrong state | Paired REDSTONE restore event at show end |
| Entities from CAPTURE_ENTITIES | Released to world, not removed | If capture is destructive, pair with DESPAWN or RELEASE + cleanup |
| ENTITY_EFFECT on world-resident mobs | Potion effects persist on non-show entities | Use conservative durations; do not apply permanent effects |
| Named entities without `despawn_on_end: true` | Persists in world | Always use `despawn_on_end: true` unless intentional |

---

#### Pre-Show State Recording

At show invocation (before tick 0), the following are recorded per participant:
- World location (stored as `home`)
- Gamemode
- Flight state

This is the restore baseline. The Stage Manager should know this list and flag any authoring choice that alters player state in a way outside this list (e.g., inventory modification via COMMAND — not currently tracked).

---

#### Rehearsal Discipline

Every show must be safe to interrupt at any tick during rehearsal. This means:

1. COMMAND-based block modifications must have cleanup scripted before the show is used in rehearsal.
2. Any experimental show section that modifies the world must be tagged in the run sheet: "WORLD MODIFICATION — confirm cleanup before rehearsal."
3. The Stage Manager reviews the run sheet before any in-game test and confirms cleanup coverage.

---

#### Java Gap Escalation

When any role identifies a gap in show control surface — a thing Minecraft can do that ScaenaShows cannot author via YAML — the Stage Manager escalates it as a GitHub issue on the ScaenaShows repo. Issue format: see Appendix A.

The Java review team audits the control surface regularly. The goal: every meaningful Minecraft API knob should be reacha

---

### 10. Fireworks Director

**Mira runs the Fireworks department.** She thinks about a burst the way a painter thinks about negative space — as much about what surrounds it as what it is. Her primary orientation is altitude and timing: where the player is standing relative to a detonation is the most consequential decision in the show.

**Domain:** Every firework detonation in the show. Owns single launches, spatial patterns, the preset library, and pyrotechnic composition — which pattern serves the beat, at what density, at what altitude. Fireworks are punctuation: they launch, burst, and are gone. The department does not build sustained texture or set ambient world state. It owns specific moments.

**The question this role asks:** What detonates, where, at what altitude — and what does that moment mean?

**Authority:** FIREWORK, FIREWORK_CIRCLE, FIREWORK_LINE, FIREWORK_FAN, FIREWORK_RANDOM events throughout the show timeline. The fireworks.yml preset library: authoring, naming, and curation of all reusable rocket definitions.

**Named head:** Mira. See `kb/departments/fireworks/fireworks.kb.md §Mira` for working style and escalation posture.

**Knowledgebase:** `kb/departments/fireworks/fireworks.kb.md` (v1.3) — Mira introduction, full instrument inventory with YAML syntax for all five event types, power and color variation reference, preset library structure and current preset families, tone translation, department principles, and capability status table with all known gaps filed.

---

#### Firework Event Types — Overview

Five event types, each with a distinct spatial and compositional role:

| Event | Pattern | Chase available? | Power/color variation? |
|---|---|---|---|
| `FIREWORK` | Single rocket at a point | No | No |
| `FIREWORK_CIRCLE` | N rockets in a ring around origin | Yes | Yes (full) |
| `FIREWORK_LINE` | N rockets along a directional line | Yes | Yes (GRADIENT gapped — see KB) |
| `FIREWORK_FAN` | Multi-arm, each arm its own preset | Yes (cross-arm) | No (gapped — see KB) |
| `FIREWORK_RANDOM` | N rockets at random XZ within radius | No | No |

All event types share `y_mode: relative` (offset from anchor Y) or `y_mode: surface` (offset from highest block at XZ). Power in the preset drives burst altitude — power 1 ≈ 8–12 blocks, power 2 ≈ 16–20 blocks, power 3 = high overhead.

> **Note on single FIREWORK offset:** Single FIREWORK uses a **nested `offset: {x, y, z}` map**, not flat fields. Pattern events (CIRCLE, LINE, FAN, RANDOM) use top-level `y_offset` + `y_mode`. These structures differ — check the KB before authoring.

---

#### The Preset Library

Presets live in `src/main/resources/fireworks.yml`. Each is a named rocket definition: power, star shape(s), colors, trail, and flicker. All FIREWORK_* events reference a preset by ID.

**Current preset families:**

| Family | Register |
|--------|----------|
| `scae_*` (4 presets) | Brand / ceremonial — gold, green, white |
| `bday_*` (4 presets) | Celebration / warmth — soft pastels |
| `pride_*` (4 presets) | Pride / joy / community — rainbow and trans palette |

**Star shapes:** BALL (workhorse, legible at any distance), LARGE_BALL (finale energy), STAR (ceremonial, sharp), BURST (wild, scattering), CREEPER (novelty).

Add a preset when a color identity needs to be named and reusable. Use `color_variation` on a pattern event when color is a composition choice within a single scene beat.

---

#### Altitude is Dramaturgy

Where a burst detonates relative to the player is the Fireworks Director's most consequential decision:

- **Player below the burst** — audience perspective. They are watching. Ceremonial, observed.
- **Player inside the burst** — participant. Surrounded. Overwhelming or intimate depending on radius.
- **Player above the burst** — elevated observer. Looking down at fire. Rare and powerful.

The **Effects department owns the player's altitude** at any given moment. The **Fireworks Director designs burst altitude relative to that position.** These two departments must agree before YAML is authored for any pyrotechnic moment.

---

#### Key Coordination

**With Effects:** Confirm where the player is (altitude, position) when any pyrotechnic moment fires. A burst at y+20 is invisible if the player is on the ground. A burst at y+0 is overwhelming if the player is hovering at y+25 looking down.

**With Lighting:** Every burst illuminates the world at player-eye for a fraction of a second. The color palette of a firework IS the lighting for that beat. A warm gold burst in a cool-lit scene makes a statement. Coordinate on color register — presets should complement or deliberately contrast the ambient palette Lighting has established.

**With Sound:** Fireworks have no built-in sound event — the rocket launch and burst are client-side effects and their audio varies by client distance. For shows where the pyrotechnic moment must land with a specific sound, Sound authors a SOUND event at the same tick or offset. Coordinate when a burst needs an authored audio hit.

**Known gaps in this department:** See `kb/departments/fireworks/fireworks.kb.md §Capability Status Summary` — four gapped items currently in ops-inbox.md: `min_clearance` not enforced, preset `launch:` mode not applied, FIREWORK_LINE GRADIENT defaults to red→blue, and FIREWORK_FAN has no power/color variation.