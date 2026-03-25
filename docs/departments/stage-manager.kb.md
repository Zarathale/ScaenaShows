---
department: Stage Manager
owner: Stage Manager
kb_version: 1.0
updated: 2026-03-25
---

# Stage Manager — Technical Knowledgebase

> Technical reference for the Stage Manager department. Documents what the ScaenaShows Java plugin
> tracks and cleans up automatically, what it does not, the operational discipline required to
> keep every show safe for interrupted rehearsals, and Stage Management's ownership of the Java
> capability registry and ops-inbox workflow.
>
> Creative direction for this role lives in `docs/production-team.md §9. Stage Manager`.

---

## Role Summary

The Stage Manager has no direct YAML event ownership. This role owns three things:

1. **The cleanup contract** — the guarantee that every show, at every tick of interruption, leaves the world exactly as it found it.
2. **The Java capability registry** — the authoritative, current picture of what the plugin can do, what it cannot do, what is in development, and what is filed for future work. Stage Management is the single source of truth on this. All departments have *awareness* of capabilities; only Stage Management *owns* the state.
3. **The ops-inbox workflow** — Stage Management maintains `ops-inbox.md`, contributes items on behalf of any production team member who identifies a gap, and coordinates communication with the Java review team.

---

## Java Capability Registry — Stage Manager Owns This

Stage Management is the production team's facility manager. In a physical theatre, the Stage Manager knows every piece of equipment in the building, what works, what doesn't, and what's on the repair list. In ScaenaShows, that means:

**What Stage Management tracks:**
- Every event type currently implemented and working (the full YAML surface — see `docs/spec.md §6` for the authoritative list)
- Every known gap: features the team wants to author but cannot yet express in YAML
- Every gap's current status: open, in development, or resolved
- The development priority of each gap (informed by show authoring needs)
- Workarounds the team is using in place of gap items

**How other departments relate to this:**
Each department KB documents capability *awareness* — what their department needs to know to author correctly, including limitations that affect their work. But departments do not maintain that list or file items to the Java review team directly. If a Choreographer identifies that `FACE` doesn't set pitch, they note it for Stage Management. Stage Management files it, frames it, and tracks it.

**Why this matters:**
Without a single owner of the capability state, different departments will have different assumptions about what works, what doesn't, and what's coming. Stage Management prevents that drift.

---

## Ops-Inbox Workflow

`ops-inbox.md` is the production team's communication channel with the Java review team. Stage Management owns it end-to-end.

**The workflow:**

1. **Any team member identifies a gap** during show authoring, calibration, or in-game testing.
2. **They bring it to Stage Management** — in conversation, in a run sheet note, or in a debrief. They don't file it themselves.
3. **Stage Management evaluates the item:** Is this a gap (something that can't be done at all), a bug (something that should work but doesn't), or a limitation (an inherent Minecraft constraint)? Only the first two go to ops-inbox.
4. **Stage Management writes the ops-inbox entry** using the standard format (see below) with enough context for the Java review team to pick it up independently.
5. **Stage Management updates department KBs** when gaps are resolved. When a gap ships, remove the gap warning from the relevant department KB and update the capability section.

**Filing format for `ops-inbox.md`:**

```markdown
### [java-gap] Short description

**Area:** Department(s) affected
**Event:** Event type affected (or "new — does not exist")

What is missing or broken. Specific about the Java method, YAML field, or behavior.

**Impact:** What authoring is blocked or requires workaround.

**Fix scope:** (optional) Suggested implementation with enough detail for the review team.
```

**What does NOT go to ops-inbox:**
- Inherent Minecraft constraints (e.g., sound system doesn't support seek — this is a Minecraft limitation, not a ScaenaShows gap)
- Aesthetic preferences ("I wish fireworks had more color options")
- Future feature requests not yet needed for active show work (file in GitHub issues instead)

**Gap lifecycle:**
```
Identified → Evaluated by SM → Filed in ops-inbox (open) → Java review picks it up
→ Implemented → SM confirms → Moved to Resolved in ops-inbox → Department KBs updated
```

---

## What Stop-Safety Covers (Automatic)

When a show ends — naturally, via `/show stop`, or via player disconnect — `applyStopSafety()` runs automatically. The following items are cleaned up without any authoring action required:

| Item | How it's handled |
|------|-----------------|
| Named spawned entities with `despawn_on_end: true` | Removed from world |
| Player SPECTATOR mode (from PLAYER_SPECTATE) | Restored to pre-show game mode |
| Player flight state (from PLAYER_FLIGHT hover) | Restored to pre-show flight state |
| Slow_falling effect (from PLAYER_FLIGHT release) | Applied transitionally during restoration |
| Show-owned teams (GLOW, TEAM_COLOR) | Unregistered from server team list |
| Show-owned bossbar instances | Removed from player HUD |

---

## Pre-Show State Captured at Invocation

At show invocation (before tick 0), the following are recorded per participant. These values form the restoration baseline:

| Value | What's recorded |
|-------|----------------|
| `home` | Player's XYZ world location at invocation |
| `gamemode` | Player's game mode (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR) |
| `flight state` | Whether the player was flying at invocation |

**Stage Manager must know this list.** Any show operation that alters player state outside this list (e.g., inventory modification via COMMAND, potion effects on world-resident entities) is outside the automatic restoration baseline and requires explicit cleanup authoring.

---

## What Stop-Safety Does NOT Cover

These items require explicit cleanup authored in the show YAML or documented in the run sheet:

| Item | Risk | Required action |
|------|------|----------------|
| Blocks placed via COMMAND | Permanent world modification if show is interrupted before cleanup | Cleanup COMMAND at show end + run sheet documentation |
| REDSTONE state changes | Circuit left in wrong state | Paired REDSTONE restore event at show end |
| Entities from CAPTURE_ENTITIES | Released to world on show end — AI restored if `restore_ai: true` | If capture is destructive, pair with RELEASE_ENTITIES + DESPAWN_ENTITY |
| ENTITY_EFFECT on world-resident mobs | Potion effects persist on non-show entities after show ends | Use conservative durations; do not apply permanent effects to world mobs |
| Named entities with `despawn_on_end: false` | Persist in world permanently | Use only for intentional world fixtures; document in run sheet |
| Inventory modifications via COMMAND | Items given/removed persist after show | Document changes; write cleanup COMMAND or accept as intentional |
| TIME_OF_DAY / WEATHER changes | World time/weather left in show state | Write TIME_OF_DAY / WEATHER restore event at show end if significant |

---

## Block Modification Protocol

No BLOCK_PLACE or BLOCK_REMOVE event type exists. All block modifications go through COMMAND. COMMAND-placed blocks are not tracked by the show and are not restored by stop-safety.

**Required protocol for any COMMAND block modification:**

1. At tick N: COMMAND fires to place/modify blocks.
2. At show end (natural): matching COMMAND fires to restore original block state.
3. Document in the run sheet: tick placed, world coordinates, original block type, replacement block type.
4. Create a standalone cleanup cue (e.g., `fx.cleanup.[show_id]`) that can be invoked manually if the show is interrupted mid-run.

**Format for run sheet block modification log:**

```
Block Modifications:
  T=40: COMMAND placed minecraft:stone at (142, 64, -304). Original: minecraft:air. Cleanup at T=END.
  T=40: COMMAND placed minecraft:stone at (143, 64, -304). Original: minecraft:air. Cleanup at T=END.
  Manual cleanup: /show play fx.cleanup.[show_id]
```

**The Stage Manager rule:** No show with COMMAND block modifications runs in rehearsal until the cleanup COMMAND is authored, tested, and confirmed working.

---

## Rehearsal Safety Checklist

Run this checklist before any in-game test. Mark each item in the run sheet.

```
□ All spawned entities have despawn_on_end: true (or intentional fixture noted)
□ All PLAYER_SPECTATE events have a corresponding PLAYER_SPECTATE_END
□ All PLAYER_FLIGHT hover events have a corresponding PLAYER_FLIGHT release before show end
□ All looping SOUND events have a corresponding STOP_SOUND
□ All REDSTONE state changes have a corresponding restore event at show end
□ Block modifications via COMMAND: cleanup COMMAND authored and tested
□ ENTITY_EFFECT on world-resident mobs: durations are bounded (not infinite)
□ TIME_OF_DAY / WEATHER: restore event at show end if changed significantly
□ Any COMMAND inventory changes: documented and accepted or reversed
```

---

## Java Gap Escalation Protocol

When any role identifies a gap in show control surface — a thing Minecraft can do that ScaenaShows cannot author via YAML — the Stage Manager escalates it to `ops-inbox.md`.

**Filing format for `ops-inbox.md`:**

```markdown
### [java-gap] Short description of the gap

**Area:** Department(s) affected
**Event:** Event type affected (or "new — does not exist")

Description of what's missing or broken. Be specific about the Java method, YAML field, or behavior.

**Impact:** What authoring is blocked or requires workaround.

**Fix scope:** (optional) Suggested Java change with enough detail for the review team.
```

**Who can file:** Claude, Zara, Smitty2020 — any show authoring role. The Stage Manager is responsible for making sure gaps are filed, not for fixing them.

---

## Utility Events — Stage Manager Domain

The Stage Manager owns the operational use of these utility events:

### REST

Holds time on the timeline with no effect. The deliberate pause — silence, space, breath.

```yaml
type: REST
duration_ticks: 40
name: "Breath between C3 and C4"   # optional label; appears in --scenes director mode
```

**Use:** Any intentional pause in the show timeline should be a REST, not an implied gap. A REST with a label makes the intent visible to the next author who reads the YAML.

### GROUP_ASSIGN

Distributes participants into groups at the start of the show. Must fire at tick 0.

```yaml
type: GROUP_ASSIGN
# Uses show-level group_count and group_strategy.
# group_count: 2  (in show YAML)
# group_strategy: join_order | alphabetical | random
```

**Stage Manager note:** GROUP_ASSIGN underflow is a silent skip — if fewer players exist than groups defined, extra groups have zero members, and any event targeting an empty group silently does nothing. This is correct and intentional behavior.

### COMMAND — Escape hatch

```yaml
type: COMMAND
command: "/say Hello world"
```

COMMAND is the escape hatch for anything not natively supported. Every COMMAND use in a show should be noted in the run sheet — either because it modifies the world (block protocol applies) or because it does something outside the show's tracking system.

---

## Active Gap Registry

Stage Management maintains this registry. All open items are in `ops-inbox.md` with full filing detail. This table is Stage Management's operational view — safety implications and department impact.

| Gap | Affected departments | Safety implication | ops-inbox status |
|-----|---------------------|--------------------|-----------------|
| SPAWN_ENTITY variant/profession not applied | Wardrobe, Casting | None — field silently ignored | Open |
| FACE pitch gap | Camera, Choreography | None — workaround available (PLAYER_TELEPORT) | Open |
| No BLOCK_PLACE/BLOCK_REMOVE | Set Director, Stage Manager | **High** — COMMAND blocks not covered by stop-safety | Open |
| No TITLE_CLEAR | Voice | None — cosmetic workaround available | Open |
| No smooth ROTATE | Camera, Choreography | None — PLAYER_SPECTATE workaround available | Open |

**When a gap is resolved:** Remove from this table, move the ops-inbox entry to Resolved with the version it shipped in, update the affected department KBs.

---

## Version Bump Reminder

The Stage Manager tracks version policy on behalf of all departments:

| Change type | Bump required |
|-------------|--------------|
| New cues, shows, or docs added | MINOR |
| Java plugin logic changed | MINOR |
| Bug fix only, no new content | PATCH |
| Breaking schema change or major rework | MAJOR |

Always bump before building. Never build the same version number twice. Version file: `build.gradle.kts` (`version = "x.y.z"`).
