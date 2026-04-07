# Code Session Prompt — OPS-029 Group 5 Department

Use this template when handing off a Group 5 department implementation to Claude Code.
Fill in the bracketed slots from `kb/system/ops-029-impl-plan.md` before pasting.

---

## Critical: Read Strategy

Claude Code has a file-read token cap (roughly 10,000 tokens per session). The context
docs for this project total ~6,000+ lines if read in full — that blows the cap and forces
Code to rethink mid-session, which wastes time and introduces drift.

**Two rules for every Code prompt:**

1. **Batch all reads upfront** — instruct Code to issue all file reads in one parallel
   batch before writing any code. This is faster and avoids hitting the cap mid-task.

2. **Use targeted line ranges for large files** — never ask Code to read a 2,000-line
   file when it only needs 120 lines. Give explicit `offset`/`limit` values.

### Known large files and their relevant sections

| File | Total lines | What Code needs | How to find it |
|------|-------------|-----------------|----------------|
| `kb/system/cue-show-yaml-schema.md` | ~2,067 | Event type definitions for this dept | `grep -n "^\*\*[EVENT_TYPE]"` |
| `kb/system/phase2-department-panels.md` | ~1,339 | This dept's section only | `grep -n "^### ✅ [DeptName]"` → read ~120 lines from there |
| `kb/system/ops-029-design-session-2026-04-05.md` | ~463 | §9 Universal Shell only (lines 333–416) | Fixed — always these lines |
| `src/.../FireworksEditSession.java` | ~849 | First 80 lines (class header + constructor) as pattern reference | Fixed — first 80 lines only |

**`CLAUDE.md` (~260 lines) and `ops-029-impl-plan.md` (~400 lines) are small enough to read in full.**

Department KB files (`kb/departments/[dept]/[dept].kb.md`) vary. Check size before deciding;
most are under 300 lines and can be read in full.

### How to find line ranges before writing the prompt

Run these in the mounted folder to get exact numbers:

```bash
# Find a dept's section start in the panel spec:
grep -n "^### ✅ [DeptName]" kb/system/phase2-department-panels.md

# Find event type definitions in the schema:
grep -n "^\*\*TIME_OF_DAY\|^\*\*WEATHER\|^\*\*LIGHTNING" kb/system/cue-show-yaml-schema.md

# Check a file's total line count:
wc -l kb/system/cue-show-yaml-schema.md
```

Read ~120 lines from the section start for panel specs; read ~30–60 lines per event type
in the schema (enough to cover the full field table).

---

## Template

```
You are continuing OPS-029 Phase 2 on the ScaenaShows plugin.

Current state:
- main is at v[CURRENT VERSION] — clean, no open branches
- Group 5 so far: [LIST COMPLETED DEPTS WITH VERSIONS]
- Next: [DEPT NAME] dept edit session, target v[TARGET VERSION]
- Feature branch: claude/ops-029-group5-[dept-slug]

**READ STRATEGY — issue all file reads in one parallel batch before writing any code:**

Full reads (short files):
- CLAUDE.md
- kb/system/ops-029-impl-plan.md
- kb/departments/[dept-slug]/[dept-slug].kb.md

Targeted section reads (large files — use offset/limit):
- kb/system/ops-029-design-session-2026-04-05.md  lines 333–416  (§9 Universal Shell)
- kb/system/phase2-department-panels.md  lines [PANEL_START]–[PANEL_END]  ([Dept Name] section)
- kb/system/cue-show-yaml-schema.md  lines [SCHEMA_RANGE_1]  ([EVENT_TYPE_1])
- kb/system/cue-show-yaml-schema.md  lines [SCHEMA_RANGE_2]  ([EVENT_TYPE_2])  ← add more as needed
- src/main/java/com/scaena/shows/tech/[ClosestPriorDept]EditSession.java  lines 1–80  (pattern reference)

Then implement:
- git checkout -b claude/ops-029-group5-[dept-slug]
- New class: [DeptName]EditSession implements DeptEditSession
- New class: [DeptName]PanelBuilder  (if panel is non-trivial)
- Wire into TechManager: add `[dept-slug].` prefix dispatch routing (same pattern as existing depts)
- Bump build.gradle.kts: v[CURRENT VERSION] → v[TARGET VERSION]
- Commit: "OPS-029 Group 5 [Dept]: [DeptName]EditSession + [DeptName]PanelBuilder (v[TARGET VERSION])"
- Push the branch. Stop there — Alan reviews and merges.

Do NOT run gradle or gradlew. Write the code, bump the version, commit, push. Stop.
```

---

## Slots to fill

All values come from `kb/system/ops-029-impl-plan.md` (Current State + Version Progression).

| Slot | Where to find it |
|------|-----------------|
| `[CURRENT VERSION]` | `build.gradle.kts` → `version = "x.y.z"` |
| `[LIST COMPLETED DEPTS]` | impl-plan "Current State" — Group 5 progress block |
| `[DEPT NAME]` | Next unchecked item in impl-plan Group 5 build sequence |
| `[TARGET VERSION]` | Version column for that dept in the impl-plan version table |
| `[dept-slug]` | Lowercase dept name, e.g. `lighting`, `camera` |
| `[DeptName]` | PascalCase, e.g. `Lighting`, `Camera` |
| `[PANEL_START]` | Line number from: `grep -n "^### ✅ [DeptName]" kb/system/phase2-department-panels.md` |
| `[PANEL_END]` | PANEL_START + ~120 (or grep the next `### ✅` header and subtract 2) |
| `[SCHEMA_RANGE_*]` | Lines from: `grep -n "^\*\*[EVENT_TYPE]" kb/system/cue-show-yaml-schema.md` + ~30–60 lines per type |
| `[ClosestPriorDept]` | Most structurally similar shipped dept (e.g. Fireworks for anchor-based, Sound for param-panel) |

---

## Lighting session (filled in — current next)

```
You are continuing OPS-029 Phase 2 on the ScaenaShows plugin.

Current state:
- main is at v2.39.0 — clean, no open branches
- Group 5 so far: Casting (v2.33.0/v2.35.0), Wardrobe (v2.34.0), Sound (v2.36.0),
  Voice (v2.37.0), Effects (v2.38.0), Fireworks (v2.39.0)
- Next: Lighting dept edit session, target v2.40.0
- Feature branch: claude/ops-029-group5-lighting

**READ STRATEGY — issue all file reads in one parallel batch before writing any code:**

Full reads (short files):
- CLAUDE.md
- kb/system/ops-029-impl-plan.md
- kb/departments/lighting/lighting.kb.md

Targeted section reads (large files — use offset/limit):
- kb/system/ops-029-design-session-2026-04-05.md  lines 333–416  (§9 Universal Shell)
- kb/system/phase2-department-panels.md  lines 168–289  (Lighting section)
- kb/system/cue-show-yaml-schema.md  lines 461–475  (LIGHTNING event type)
- kb/system/cue-show-yaml-schema.md  lines 624–650  (TIME_OF_DAY + WEATHER event types)
- kb/system/cue-show-yaml-schema.md  lines 1388–1460  (§18 Pattern Architecture — TIME_OF_DAY_PATTERN)
- src/main/java/com/scaena/shows/tech/FireworksEditSession.java  lines 1–80  (anchor dispatch pattern reference)

Then implement:
- git checkout -b claude/ops-029-group5-lighting
- New class: LightingEditSession implements DeptEditSession
  - 4 instruments: TIME_OF_DAY, TIME_OF_DAY_PATTERN, WEATHER, LIGHTNING
  - TIME_OF_DAY: scroll wheel ±1000t/notch, Shift ±100t. Actionbar shows value + nearest
    named sky state (Dawn 23000, Morning 0, Noon 6000, Afternoon 9000, Dusk 13000,
    Night/Midnight 18000). Snap offer when within ~200t of a named point. World preview
    toggle (WorldPreviewMode on TechCueSession). In LIVE mode no preview button; in
    VALUES_ONLY mode show [▶ Apply to world]. No preset.
  - TIME_OF_DAY_PATTERN: scroll wheel fields for start_value, end_value (same sky labels),
    steps (actionbar shows step interval e.g. "every 27t (~1.3s)"), total_duration (±20t/
    notch, Shift ±100t, shown as "240t (12s)"). curve selector: linear/ease_in/ease_out.
    Auto-preview OFF — explicit [▶ Preview] fires full Pattern expansion. Preset ✅,
    naming: lighting.[direction].[slug] (direction inferred from start/end values).
  - WEATHER: state selector clear/storm/thunder. duration_ticks scroll wheel (optional;
    [Clear] removes field). World preview toggle. No preset.
  - LIGHTNING: anchor selector scene_origin/player. x/y/z scroll wheel ±1/notch, Shift ±5.
    [▶ Preview] fires test strike. Auto-preview toggle applies. OPS-034 warning inline when
    player anchor selected (same pattern as FireworksEditSession). Preset ✅ — anchor +
    x/y/z. Naming: lighting.lightning.[anchor_type].[slug].
- New class: LightingPanelBuilder
- Wire into TechManager: add `lighting.` prefix dispatch routing
- Bump build.gradle.kts: 2.39.0 → 2.40.0
- Commit: "OPS-029 Group 5 Lighting: LightingEditSession + LightingPanelBuilder (v2.40.0)"
- Push the branch. Stop there — Alan reviews and merges.

Do NOT run gradle or gradlew. Write the code, bump the version, commit, push. Stop.
```
