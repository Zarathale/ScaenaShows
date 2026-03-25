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

**Area:** Camera Director, Choreographer
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

**Area:** Camera Director
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

## Resolved

*(none yet)*
