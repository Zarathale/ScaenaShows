# ScaenaShows — Ops Inbox

Items here are queued for the Java review team. Each entry has enough context to pick up and work on independently. When resolved, move the item to the **Resolved** section with the version it shipped in.

**Filing protocol:** Any role — Claude, Zara, Smitty — can add items here. Label each with the area it affects. Java review team audits regularly for control surface gaps.

---

## Open

---

### [java-gap] SPAWN_ENTITY: `variant` and `profession` fields parsed but not applied

**Area:** Wardrobe, Casting Director
**Schema field:** `variant:` and `profession:` on `SPAWN_ENTITY`

`SpawnEntityEvent` parses both fields correctly (model layer is complete), but `EntityEventExecutor.handleSpawn()` never applies them to the spawned entity. They are silently ignored at runtime.

**Impact:** Villager professions, cat coat patterns, horse colorings, sheep wool colors, and wolf variants cannot be set via YAML. The fields appear to work but do nothing.

**Fix scope:** In `EntityEventExecutor.handleSpawn()`, after spawning, cast to the appropriate entity subtype and apply:
- `Villager` → `setProfession(Villager.Profession.valueOf(e.profession))` and `setVillagerType(Villager.Type.valueOf(e.variant))`
- `Cat` → `setCatType(Cat.Type.valueOf(e.variant))`
- `Horse` → `setColor(Horse.Color.valueOf(e.variant))`
- `Sheep` → `setColor(DyeColor.valueOf(e.variant))`
- `Wolf` (1.21+) → `setVariant(Wolf.Variant.valueOf(e.variant))`

Guard each cast; log and continue if the variant value is invalid.

---

### [java-gap] FACE: yaw only, no pitch control

**Area:** Effects Director (Camera specialty), Choreographer
**Event:** `FACE`

`StageEventExecutor.handleFace()` computes yaw (horizontal angle via atan2) but does not set pitch. The entity or player turns horizontally toward the target but is not oriented vertically.

**Impact:** Cannot orient players or entities to look up or down at specific targets. Looking up at aerial performers, overhead fireworks, or elevated marks requires a PLAYER_TELEPORT workaround that also changes position.

**Fix scope:** Add pitch computation alongside the existing yaw logic:
```java
double dy = lookTarget.getY() - from.getY();
double horizontalDist = Math.sqrt(dx*dx + dz*dz);
float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDist)));
newLoc.setPitch(pitch);
```

---

### [java-gap] No BLOCK_PLACE / BLOCK_REMOVE event type

**Area:** Set Director, Stage Manager
**Event:** (new — does not exist)

Block modifications currently require the `COMMAND` escape hatch. COMMAND-placed blocks are outside the show's stop-safety contract and are not restored if the show is interrupted.

**Impact:** Set Director cannot use block-based set dressing safely in rehearsal or production shows. Any block modification carries permanent-world risk on interruption.

**Proposed:** Add `BLOCK_PLACE` and `BLOCK_REMOVE` event types. On `BLOCK_PLACE`, record the original block type at the target location in `RunningShow`. On show end (natural or interrupted), `applyStopSafety` restores original blocks. This brings block modification inside the cleanup contract.

---

### [java-gap] No TITLE_CLEAR event

**Area:** Sprite Voice Director
**Event:** (new — does not exist)

No way to cleanly dismiss a `TITLE` before its `stay` timer expires. Current workaround — firing a new TITLE with empty strings — resets the fade clock and causes a visual pop.

**Proposed:** Add a `TITLE_CLEAR` point-in-time event that sends a title with empty strings and `fade_in: 0, stay: 0, fade_out: 10`. Clean fast fade-out, no pop.

---

### [java-gap] No smooth yaw rotation (ROTATE event)

**Area:** Effects Director (Camera specialty)
**Event:** (new — does not exist)

`FACE` is instant. No first-class primitive exists for gradual camera panning (yaw rotation without position movement). Current workaround is rapid PLAYER_TELEPORT sequences, which is imprecise.

**Proposed:** Add a `ROTATE` bar event:
```yaml
type: ROTATE
target: player | entity:spawned:Name
yaw: 90.0           # absolute target yaw, or delta: +90 for relative
duration_ticks: 40
```
Implementation: BukkitRunnable interpolating yaw per tick, changing only the yaw component without altering XYZ — parallel to `smoothMovePlayer`.

---

### [java-gap] Show scanner reads flat `shows/*.yml` only — does not scan subdirectories

**Area:** Stage Manager, all shows
**Filed:** 2026-03-25 (show folder structure work, R7 session)

The plugin's show-loading code scans `src/main/resources/shows/*.yml` at build time via
`JarFile(getFile())`. The new show folder structure places YAMLs at
`shows/[show_id]/[show_id].yml`. The scanner does not descend into subdirectories and will
not find these files.

**Impact:** Blocks full adoption of the show folder structure. YAMLs must remain as flat
files alongside their folder counterparts until this is resolved. The folder structure
currently serves as documentation/production-team organization only.

**Fix scope:** Update the `JarFile` scan loop in the show-loading code to check one level
of subdirectories. For each entry matching `shows/*/`, check for a nested YAML whose name
matches the folder name (`shows/[id]/[id].yml`). Extract the show ID from the folder name.

Example pattern to match (in addition to existing `shows/*.yml`):
```
shows/demo.archetype_sampler/demo.archetype_sampler.yml  →  show ID: demo.archetype_sampler
```

**Priority:** Medium — not blocking current shows (flat YAMLs still load), but gates full
folder-structure adoption for all shows. See `docs/show-import-process.md` for the
migration process that depends on this fix.

---

### [java-gap] ENTITY_AI and behavior events resolve only first group member

**Area:** Casting Director, Wardrobe
**Events affected:** `ENTITY_AI`, `ENTITY_SPEED`, `ENTITY_EFFECT`, `ENTITY_EQUIP`, `ENTITY_INVISIBLE`, `ENTITY_VELOCITY`
**Filed:** 2026-03-25 (Casting KB build)

`EntityEventExecutor.resolveEntity()` returns only `group.get(0)` — the first UUID — when the
target is `entity_group:<name>`. All six behavior event handlers call this singular resolver, so
any group-targeted behavior event silently skips all members except the first.

By contrast, `StageEventExecutor.resolveEntities()` (plural) correctly iterates the full group
list — `HOLD`, `FACE`, and `CROSS_TO` work correctly on groups.

**Impact:** `ENTITY_AI enabled: false` on `entity_group:chorus` only puppets the first entity.
Any choreography that relies on group-wide AI control is silently broken.

**Fix scope:** In `EntityEventExecutor`, replace `resolveEntity()` calls in all six behavior
handlers with a loop over `resolveEntities()` (matching the pattern in StageEventExecutor), or
extract a shared group-resolution helper accessible to both executors.

---

### [java-gap] `capture_mode: live` parsed but not implemented

**Area:** Casting Director
**Event:** `CAPTURE_ENTITIES`
**Filed:** 2026-03-25 (Casting KB build)

`CaptureEntitiesEvent` stores `captureMode` as a string, but `EntityEventExecutor.handleCapture()`
always performs a one-time snapshot sweep into a UUID list, regardless of the field value.
There is no re-sweep logic for `live` mode.

**Impact:** `capture_mode: live` behaves identically to `capture_mode: snapshot`. The field is
a silent no-op.

**Fix scope:** Add a runtime mechanism for live-mode groups: store the original sweep parameters
(entityType, radius, anchor) alongside the group in `RunningShow`. When a live-mode group is
targeted, perform a fresh `getNearbyEntities()` sweep at the time of event execution rather than
resolving from the stored UUID list.

---

### [java-gap] `entity:world:Name` targeting prefix not implemented

**Area:** Casting Director
**Events affected:** All events that use `EntityEventExecutor.resolveEntity()`
**Filed:** 2026-03-25 (Casting KB build)

`resolveEntity()` handles `entity:spawned:` and `entity_group:` prefixes. There is no branch
for `entity:world:`. Any target string beginning with `entity:world:` returns null and all
events against it silently skip. The targeting prefix is documented in the Casting KB as valid,
but has never been implemented.

**Fix scope:** Add a branch in `resolveEntity()`:
```java
if (target.startsWith("entity:world:")) {
    String customName = target.substring("entity:world:".length());
    for (Entity ent : anchor.getWorld().getEntities()) {
        if (customName.equals(ent.getCustomName())) return ent;
    }
    return null;
}
```
Note: this world scan should be bounded (or replaced with a tagged lookup) for performance on
large worlds.

---

### [java-gap] ENTITY_SPEED does not address entity groups

**Area:** Choreography
**Event:** `ENTITY_SPEED`
**Filed:** 2026-03-25 (Choreography KB build)

`EntityEventExecutor.handleEntitySpeed()` calls `resolveEntity()`, which returns only the first
member of an entity group (UUID list position 0). When `target: entity_group:Name` is used,
only the first captured entity receives the speed change. All other group members are silently
unaffected.

**Impact:** Choreographers cannot apply a unified speed change to a chorus group with a single
ENTITY_SPEED event. Each member must be addressed individually by name, or the group speed
must be managed through ENTITY_AI + pathfinder behavior.

**Fix scope:** In `handleEntitySpeed()`, resolve the full entity list (same pattern as
`StageEventExecutor.resolveEntities()` which handles groups correctly) and apply the speed
attribute to each member.

---

### [java-gap] ENTER does not apply equipment fields

**Area:** Choreography, Wardrobe
**Event:** `ENTER`
**Filed:** 2026-03-25 (Choreography KB build)

`StageEventExecutor.handleEnter()` spawns the entity and sets name/baby variant, but does not
apply any equipment fields. The spec implies ENTER should behave like SPAWN_ENTITY for
equipment purposes, but the executor is missing the equipment-apply block that exists in
`EntityEventExecutor.handleSpawn()`.

**Impact:** A performer who enters via ENTER always appears unequipped. If equipment is needed
at entry, the workaround is SPAWN_ENTITY at the wing mark offset + CROSS_TO to destination.

**Fix scope:** After spawning in `handleEnter()`, add the same equipment-apply block as in
`handleSpawn()` — cast to LivingEntity, get EntityEquipment, apply all six slots if non-null.
The EnterEvent model class may need equipment fields added if they aren't already parsed.

---

### [java-gap] RETURN_HOME silently skips non-Player entities

**Area:** Choreography
**Event:** `RETURN_HOME`
**Filed:** 2026-03-25 (Choreography KB build)

`StageEventExecutor.handleReturnHome()` iterates `resolveEntities()` but immediately continues
on any target that is not a Player: `if (!(entity instanceof Player player)) continue;`

A `RETURN_HOME` targeting a spawned entity or entity group silently does nothing. The spec does
not document this restriction.

**Impact:** Choreographers cannot return spawned entities to a home position using RETURN_HOME.
The workaround is CROSS_TO (back to spawn offset) or DESPAWN_ENTITY.

**Fix scope:** Implement entity home tracking in RunningShow — record each spawned entity's
spawn location as its home at SPAWN_ENTITY time. In handleReturnHome(), add a branch for
non-Player entities that CROSS_TO's or teleports them to the recorded spawn location when
`return_home` is called.

---

---

### [future-idea] Human as Designer — preamble layer for department KBs

**Area:** All department KBs, production team workflow
**Priority:** Low — not blocking anything
**Filed:** 2026-03-25

Add a "Human as Designer" preamble to each department KB clarifying the creative role split: the human designer (Alan/Zara, occasionally Smitty) sets the intention; Claude proposes the form. Each show has one human designer. The preamble would appear at the top of each of the 11 dept KBs with a universal statement plus a dept-specific one-liner. Not needed now — repo is working fine — but worth revisiting if onboarding new collaborators or if Claude starts overstepping design decisions.

---

## Resolved

*(none yet)*
