---
status: in-development
date: 2026-03-23
purpose: Pre-build spec audit — Preview Performance #1
---

# ScaenaShows v2 — Preview Audit

This is the second full read of the spec after the Final Dress Rehearsal pass. The spec is in excellent shape. What follows is a surgical list of gaps, minor inconsistencies, and open questions that should be resolved before or during Phase 1 build.

Items are rated:
- 🔴 **Resolve before build** — could cause schema or runtime ambiguity
- 🟡 **Resolve during Phase 1** — low-risk but needs a clear answer before the affected code is written
- 🟢 **Nice to have** — improvements or clarifications that don't block build

---

## Schema & Runtime Gaps

### 🔴 Missing Cue reference error handling

The spec describes cycle detection (Section 15) clearly. It does not specify what happens when a `type: CUE` event references an ID that doesn't exist in the cues folder.

**Question:** Does the show fail to load? Does it skip the event silently? Does it warn at reload time or at execution time?

**Recommendation:** Fail fast at load time — same behavior as cycle detection. Error: `[ERROR] Show 'show_celebration' references unknown Cue ID: 'ramp.warm_gold.missing'`. Matches the pattern already established.

---

### 🔴 GROUP_ASSIGN with fewer players than groups

The spec says: "If fewer players are online than groups defined, extra group events fire silently." (Section 6.4)

**Problem:** This is underspecified. If `group_count: 2` and only 1 player is present, are all events targeting `group_2` silently skipped, or does the show fail? And if a `GROUP_ASSIGN` targets `group_3` when `group_count: 2`, what happens?

**Recommendation:** Silent skip on execution — any event targeting an empty group fires with zero targets, no error. Document this clearly. Matches the behavior of targeting `@a[distance=..20]` when no players are in range.

---

### 🔴 Duplicate sections in Section 15 (Plugin Runtime)

Section 15 contains two copies of both "One show per target player" and "Pause/resume" — appearing first at lines ~1276 and again at ~1300. These are near-identical but have small differences in wording.

**Action required:** Merge into single canonical entries. The earlier versions (1276–1284) are slightly more detailed and should be kept. The later duplicates (1300–1305) should be deleted.

---

### 🟡 FIREWORK_CIRCLE: `gradient_from` / `gradient_to` field presence

In the FIREWORK_CIRCLE schema example, `gradient_from` and `gradient_to` appear unconditionally in the YAML block. These fields are only relevant when `color_variation: GRADIENT`.

**Question:** Are these fields required when color_variation is GRADIENT? Silently ignored otherwise?

**Recommendation:** Treat as optional; only used when `color_variation: GRADIENT`. Add a validation note: if `color_variation: GRADIENT` and `gradient_from`/`gradient_to` are absent, log a warning and fall back to `UNIFORM`.

---

### 🟡 Show-level bossbar behavior not fully specified

The inline `BOSSBAR` event (Section 6.1) specifies that `fade_in_ticks` animates the bar from 0→1 and `fade_out_ticks` from 1→0.

The show-level bossbar (Section 3.2) also has `fade_in_ticks` and `fade_out_ticks` but doesn't state whether the same animation logic applies, or whether this is a simple fade of the bar visibility.

**Recommendation:** Confirm same behavior as inline BOSSBAR. Document explicitly.

---

### 🟡 `entity:world:Name` targeting — what counts as a "name"?

Section 7 says `entity:world:Name` resolves by "display-name scan at show load time; fails fast if >1 match."

**Questions:**
1. Does "display name" mean the entity's custom name (set by `/name` or plugin), or can it match entity type names?
2. What if the entity has no custom name?
3. What if the scan produces 0 matches?

**Recommendation:** "Display name" = Bukkit `Entity.getCustomName()`. 0 matches = warn and skip the show (same as >1 match, fail fast). No-name entities are not targetable by this mechanism — they must be addressed by type + selector in a future version.

---

### 🟡 Naming inconsistency: `center_offset` vs `origin_offset`

- `FIREWORK_CIRCLE` uses `center_offset`
- `FIREWORK_FAN` uses `origin_offset`

These refer to the same concept (the origin point of the pattern). One should be standardized across all pattern types.

**Recommendation:** Standardize to `origin_offset` across FIREWORK_CIRCLE, FIREWORK_LINE, and FIREWORK_FAN. Update spec and composer UI accordingly.

---

### 🟡 `ORBIT` stage direction in vocabulary table (Section 11) vs. "not implemented"

Section 11 (Stage directions vocabulary table) lists `orbit` as a defined term. Section 6.9 explicitly states "ORBIT not implemented in v2."

The vocabulary table entry will confuse Smitty or ShowSprite, who may reference a term that doesn't work as an event type.

**Recommendation:** Add a note to the vocabulary table entry: `orbit — v3 candidate; not available as an event type in v2. Use a sequence of CROSS_TO events.`

---

### 🟢 No `max_stars` bound defined

The firework preset spec allows multiple stars per rocket with no upper limit stated. In practice, Minecraft has no hard star limit per rocket, but very high counts may have performance implications.

**Recommendation:** No hard cap in the schema. Add a soft warning in the composer if a preset has more than 4 stars. Document the practical ceiling in a comment.

---

### 🟢 Sound preview fallback not specified

Section 14 describes the sound preview system (serving OGG files from the local Minecraft asset cache). No behavior is specified for what happens when a sound ID isn't found in the asset index.

**Recommendation:** Silent no-op (no audio played), with a small indicator in the properties panel: `⚠ Sound not found in local asset cache`. No blocking behavior.

---

## Composer UI Gaps

### 🔴 `showsprite.context.md` — no draft or template exists

The spec lists this file as a prerequisite for Phase 7 (ShowSprite), but no draft or template is included.

**Action:** Draft the initial `showsprite.context.md` as a dedicated pre-build session before Phase 7. This is Open Question 5 — keeping it open is correct. Flag it as a blocker for Phase 7 specifically, not for earlier phases.

---

### 🟡 Composer error display pattern — not designed

The spec describes what errors can occur (cycle detection, missing cue ID, portability warnings) but doesn't specify how errors surface in the composer UI.

**Recommendation:** See the style guide (Section 9, UI-3). Inline red indicator on the offending cue ref bar in the timeline + error description in the properties panel when that event is selected. No modal or blocking dialog.

---

### 🟡 Save behavior — no unsaved-changes contract defined

The spec says ShowSprite proposals are applied "in memory" and not persisted until the creator saves. But the overall save contract for the composer is not specified:

- Does Save write all open cue/show files?
- Does the composer track dirty state per-file?
- What happens if the creator closes the browser tab with unsaved changes?

**Recommendation:** See style guide (Section 9, UI-4). Track dirty state. Warn on tab/browser close via `beforeunload`. Save writes all modified files in the current session. One save = all pending changes committed.

---

## Scaena Voice — Plugin-Facing Strings

### 🟡 Plugin error messages not yet written

The spec describes error conditions clearly (cycle detected, unknown Cue ID, permission denied, show already running, cooldown active) but doesn't specify the exact player-facing strings.

**Recommendation:** Write these in a dedicated scaena-voice session before Phase 1 lands in production. They don't block build — use placeholder strings initially, then replace before the plugin goes live. Examples of the right register:

```
# Cooldown
The stage isn't ready yet. You can run another show in {N} seconds.

# Already running
{player} is already in a running show. Call /show stop {player} first.

# Permission: play others
You can run shows for yourself. Targeting others requires additional access.

# Cycle detected (log only, not player-facing)
[ScaenaShows] Show 'show_celebration' contains a Cue cycle: ...
```

---

## Open Questions to Add (Spec Section 16)

| # | Priority | Question |
|---|---|---|
| 7 | 🔴 | Missing Cue ID reference: fail at load or skip at runtime? |
| 8 | 🔴 | GROUP_ASSIGN empty-group behavior: skip events or error? |
| 9 | 🟡 | Standardize `center_offset` vs `origin_offset` across pattern types |
| 10 | 🟡 | Plugin player-facing string drafts — needed before production launch |
| 11 | 🟡 | Composer save contract — dirty state tracking and tab-close behavior |

---

## What the Spec Gets Right (Don't Touch)

These are working well and should not be reconsidered:

- **Universal Cue type** — clean, recursive, no special cases
- **Reference-by-ID reuse model** — correct and scalable
- **SnakeYAML directly** — right call; Paper's FileConfiguration would fail this schema
- **Compass/bearing angle convention** with the inversion note and the `(90 - degrees) * PI / 180` formula — critical; the note in the spec is already doing the right thing
- **Tick 0 reservation** for setup events (GROUP_ASSIGN, CAPTURE_ENTITIES) — eliminates a whole class of race conditions
- **Stop safety contract** — idempotent cleanup is exactly right
- **ShowSprite identity** (it/they, distinct from ZaraSprite) — well-defined
- **`showsprite.context.md` as shared repo file** — the right architectural choice for a two-person authoring team
- **Phase ordering** (schema+plugin → Node server → timeline → spatial → preview → RCON → ShowSprite → library) — sound; each phase delivers usable capability

---

## Build Readiness

| Area | Status | Notes |
|---|---|---|
| YAML schema (Cue, Show, Firework) | ✅ Ready | Minor field naming to resolve (origin_offset) |
| Event type reference | ✅ Ready | 11 categories, all specified |
| Plugin runtime contract | ✅ Ready | Fix duplicate sections first |
| Invocation / permissions | ✅ Ready | |
| Composer UI layout | ✅ Ready | See ui-mockup.html |
| Composer design system | ✅ Ready | See style-guide.html |
| ShowSprite architecture | ✅ Ready | context.md draft needed before Phase 7 |
| Cue library seeds | 🟡 Deferred | Phase 8 / dedicated session |
| Player-facing strings | 🟡 Deferred | Before production launch |
| GLOW + TAB coordination | 🔶 Open | See SCENA-002 |
| RCON setup | 🔶 Deferred | Credentials confirmed; setup deferred to Phase 6 |

**Verdict: Ready for Phase 1 build** once the 3 🔴 items above are addressed (duplicate sections, missing Cue ID behavior, GROUP_ASSIGN empty-group behavior). These are 15-minute spec edits, not architectural concerns.
