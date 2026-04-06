---
department: Stage Management & Production
owner: Kendra
doc_version: 1.0
created: 2026-04-01
status: stable
notes: >
  Authoritative convention for scene_number fields in prompt-book.yml.
  Filed from OPS-028. Supersedes any inline comments in the building spec.
---

# Scene Numbering Convention

> **Kendra owns this convention.** Departments reference scenes by label. Only Stage
> Management assigns, inserts, and renumbers `scene_number` values.

---

## Purpose

The `scene_number` field in `prompt-book.yml` is a **sort key and navigation handle**
for Tech Mode. It determines scene order in the Tech Mode sidebar, scene navigation
commands, and any future scene-indexed features. It is not a visible identifier —
players never see it.

---

## The Scheme

Scene numbers are **zero-padded two-digit decimal strings**, with optional decimal
subscene suffixes:

```
"00"      # Prologue / pre-narrative setup
"01"      # First main scene
"02"      # Second main scene
...
"09"
"10"
"11"
...
```

**Zero-padding is required.** Single-digit bases must be written `"01"`, not `"1"`.
This ensures consistent sort behavior across all contexts.

**`"00"` is the reserved prologue slot.** Use it for home-base or setup scenes that
precede the main narrative arc. The main arc begins at `"01"`. There is only one `"00"`
per show.

---

## Sort Behavior

Scenes are sorted using **decimal-aware comparison**: split on `.`, parse each segment
as an integer, compare segment by segment. A number with fewer segments sorts before
one with more segments when all preceding segments are equal.

| Number | Parsed | Relative order |
|--------|--------|----------------|
| `"00"` | [0] | 1st |
| `"00.1"` | [0, 1] | 2nd |
| `"00.2"` | [0, 2] | 3rd |
| `"01"` | [1] | 4th |
| `"01.1"` | [1, 1] | 5th |
| `"09"` | [9] | ... |
| `"10"` | [10] | after "09" |
| `"10.1"` | [10, 1] | after "10" |

**This is not pure lexicographic sort.** `"09"` correctly precedes `"10"` because the
zero-padding makes lexicographic and decimal-aware results identical for the base number.
However, subscene comparison requires decimal-aware logic — do not rely on raw string
sort for subscenes.

---

## Inserting Subscenes

When a new scene needs to interleave between two existing scenes, use a **subscene
suffix** rather than renumbering:

```
"02"      existing
"02.1"    ← new scene inserted between "02" and "03"
"02.2"    ← another insert in the same gap, if needed
"03"      existing (unchanged)
```

**Rules:**
- Subscene suffixes run `".1"` through `".9"` (single digit). Up to 9 insertions per
  gap before restructuring is needed.
- Use whole-number increments for new scenes added at the end: `"06"` after `"05"`.
- Subscenes within a subscene slot (third-level decimals, e.g. `"02.1.1"`) are
  **not supported** in the current schema and should not be authored. If you need to
  split a subscene, schedule a renumber instead.
- Subscene numbers are stable once assigned — do not renumber a subscene mid-production
  without a revision log entry.

---

## Adding Scenes at the End

The next available whole-number increment:

```
Current last scene: "05"
New scene: "06"
```

No subscene suffix needed. Whole numbers are preferred over subscenes when inserting
at the tail of the running order.

---

## Renumbering

Renumbering is a **deliberate Stage Management decision**, not an incidental change.
It is appropriate when:

- The show structure has significantly changed and the existing numbers are misleading
  (e.g., `"04"` now precedes what was `"02"`)
- Subscene slots in a gap are exhausted and the gap still needs more insertions

**When renumbering:**

1. Assign new numbers to all scenes simultaneously — never partial renumber.
2. Update the prompt book YAML, run sheet, and any revision log entries that reference
   scene numbers by their old values.
3. Record the renumber in the revision log with the old → new mapping and the reason.
4. Notify the Show Director so any debrief notes (e.g., "site C felt long") can be
   re-anchored to the new number.

**Format for the revision log entry:**

```
Renumber (Stage Management) — R[N]:
  "00" → "00" (unchanged)
  "02" → "01"
  "02.1" → "02"
  "03" → "03"
  Reason: Site B removed from running order; gap between old 02 and 03 promoted.
```

---

## Visibility

`scene_number` values are **not visible to players** in any normal show path.

In Tech Mode, scene numbers appear only in the Stage Management navigation sidebar —
not in player-facing HUD elements. The `label` field is the human-readable scene name
used everywhere players or collaborators see scene identity.

Departments should reference scenes by label in their decisions and briefings, not by
number. Stage Management may use numbers internally when the order is what matters.

---

## Assignment Authority

Kendra assigns all `scene_number` values:

- At show scaffolding: initial numbers assigned when the scene list is first
  formalized (brief / intake stage)
- During production: insertions and subscene assignments as scenes are added
- At renumber events: full reassignment as described above

Departments do not propose or set scene numbers. If a department needs a new scene
inserted, they flag it to Stage Management with the requested position relative to
existing scenes (e.g., "needs a transition beat between Site B and Site C"). Kendra
assigns the number.

---

## Current Show Reference

| Show | Scene count | Number range | Notes |
|------|-------------|--------------|-------|
| showcase.01 "Preparing for Battle" | 6 | `"00"`–`"05"` | `"00"` = Home Base; `"01"`–`"05"` = five expedition sites |
| showcase.02 "The Long Night" | TBD | — | Not yet briefed |
| showcase.03 "Welcome" | TBD | — | Not yet briefed |
