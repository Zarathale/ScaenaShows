# Code Session Prompt — OPS-029 Group 5 Department

Use this template when handing off a Group 5 department implementation to Claude Code.
Fill in the bracketed slots from `kb/system/ops-029-impl-plan.md` before pasting.

---

## Template

```
We're continuing OPS-029 Phase 2 on the ScaenaShows plugin.

Current state:
- main is at v[CURRENT VERSION] — clean, no open branches
- Group 5 so far: [LIST COMPLETED DEPTS WITH VERSIONS, e.g. "Casting (v2.33.0/v2.35.0), Wardrobe (v2.34.0)"]
- Next: [DEPT NAME] dept edit session, target v[TARGET VERSION]
- Feature branch: claude/ops-029-group5-[dept-slug]

Before writing any code, read these files in order:
1. kb/system/ops-029-impl-plan.md — build sequence and current state
2. kb/system/ops-029-design-session-2026-04-05.md §9 — universal edit shell
   (boss bar, save/cancel, hotbar, auto-preview, text input rules)
3. kb/system/phase2-department-panels.md §[DEPT NAME] — panel spec for this dept
4. kb/departments/[dept-slug]/[dept-slug].kb.md — dept context

Then:
- git checkout -b claude/ops-029-group5-[dept-slug]
- Implement [DeptName]EditSession implements DeptEditSession
- Any supporting panel builder class if needed (follow the Casting/Wardrobe pattern)
- Wire into TechManager / TechCueSession as needed
- Bump build.gradle.kts to v[TARGET VERSION]
- Commit: "OPS-029 Group 5 [Dept]: [DeptName]EditSession (v[TARGET VERSION])"
- Push the branch. Stop there — Alan reviews and merges.

Do NOT run gradle or gradlew. Write the code, bump the version, commit, push. Stop.
```

---

## Slots to fill

All values come from the "Current State" and "Version Progression" sections of
`kb/system/ops-029-impl-plan.md`. Run a post-code-sync in Cowork first if the
docs might be behind the repo.

| Slot | Where to find it |
|------|-----------------|
| `[CURRENT VERSION]` | `build.gradle.kts` → `version = "x.y.z"` |
| `[LIST COMPLETED DEPTS]` | impl-plan "Current State" — Group 5 progress block |
| `[DEPT NAME]` | Next unchecked item in impl-plan Group 5 build sequence |
| `[TARGET VERSION]` | Version column for that dept in the impl-plan version table |
| `[dept-slug]` | Lowercase dept name, e.g. `sound`, `voice`, `effects` |
| `[DeptName]` | PascalCase, e.g. `Sound`, `Voice`, `Effects` |

---

## Sound session (filled in — current next)

```
We're continuing OPS-029 Phase 2 on the ScaenaShows plugin.

Current state:
- main is at v2.35.0 — clean, no open branches
- Group 5 so far: Casting (v2.33.0/v2.35.0), Wardrobe (v2.34.0)
- Next: Sound dept edit session, target v2.36.0
- Feature branch: claude/ops-029-group5-sound

Before writing any code, read these files in order:
1. kb/system/ops-029-impl-plan.md — build sequence and current state
2. kb/system/ops-029-design-session-2026-04-05.md §9 — universal edit shell
   (boss bar, save/cancel, hotbar, auto-preview, text input rules)
3. kb/system/phase2-department-panels.md §Sound — panel spec for this dept
4. kb/departments/sound/sound.kb.md — dept context

Then:
- git checkout -b claude/ops-029-group5-sound
- Implement SoundEditSession implements DeptEditSession
- Any supporting panel builder class if needed (follow the Casting/Wardrobe pattern)
- Wire into TechManager / TechCueSession as needed
- Bump build.gradle.kts to v2.36.0
- Commit: "OPS-029 Group 5 Sound: SoundEditSession (v2.36.0)"
- Push the branch. Stop there — Alan reviews and merges.

Do NOT run gradle or gradlew. Write the code, bump the version, commit, push. Stop.
```
