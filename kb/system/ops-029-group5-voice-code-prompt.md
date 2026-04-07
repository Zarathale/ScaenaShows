# OPS-029 Group 5 Voice — Code Session Prompt

**Target version:** 2.37.0
**Feature branch:** `claude/ops-029-group5-voice`
**Commit message:** `OPS-029 Group 5 Voice: VoiceEditSession (v2.37.0)`

---

## Context

You are working on **ScaenaShows**, a Minecraft Paper plugin. Phase 2 (OPS-029) is a live tech-rehearsal tool. You are implementing one department edit session at a time. Groups 0–5 (Casting, Wardrobe, Sound) are committed on `main` at v2.36.0.

This session adds **Voice** — the fourth Group 5 department. The pattern is established: implement `DeptEditSession`, build a panel builder, wire into `TechManager`.

---

## Read Before Writing Any Code

Before touching Java, read these files in order:

1. `kb/system/ops-029-impl-plan.md` — build sequence and architecture overview
2. `kb/system/ops-029-design-session-2026-04-05.md` — architecture decisions (especially DeptEditSession contract)
3. `kb/system/phase2-department-panels.md §Voice` — the locked panel spec (line ~1177)
4. `kb/departments/voice/voice.kb.md` — Voice instruments, event types, capability summary
5. `src/main/java/com/scaena/shows/tech/SoundEditSession.java` — most recent dept session; use as structural template
6. `src/main/java/com/scaena/shows/tech/SoundPanelBuilder.java` — panel builder template
7. `src/main/java/com/scaena/shows/tech/TechManager.java` — find `buildDeptEditSession()` and the panel dispatch branch

---

## What to Build

### New files

**`VoiceEditSession.java`** — implements `DeptEditSession`

Voice covers five event types: `MESSAGE`, `TITLE`, `ACTION_BAR`, `BOSSBAR`, and `PHRASE`. Detect the first event type in the cue on entry to determine which mode to open. All five are non-preview (no world side-effect to refire — text events send to the player directly; PHRASE is multi-step).

Key behaviors:
- Entry snapshot captured for Cancel restoration (same pattern as Sound/Wardrobe)
- For single events (MESSAGE, TITLE, ACTION_BAR, BOSSBAR): in-place param editing with a `[▶ Preview]` button that refires the event to the player
- For PHRASE: script editor mode — list of steps, each with timing (at/after), event type, and content preview; Edit button opens the individual event panel inline; ↑/↓ reorders steps; ✕ deletes; `[+ Add Line]` appends
- Preset naming: `voice.[instrument].[slug]` (e.g. `voice.message.sprite_intro`, `voice.bossbar.battle_open`)
- `onSaveAsPreset()` saves the full cue as a preset using `ShowYamlEditor.saveAsPreset()`

**`VoicePanelBuilder.java`** — static panel methods

Build separate `sendPanel()` entry points per mode, or a single dispatch that reads `VoiceEditSession.getEventType()`:

- `sendMessagePanel(Player, VoiceEditSession)` — text field row + audience picker pill row + preview row
- `sendTitlePanel(Player, VoiceEditSession)` — title text + subtitle text (optional) + fade_in / stay / fade_out tick fields + preview
- `sendActionBarPanel(Player, VoiceEditSession)` — text field + duration_ticks + preview
- `sendBossbarPanel(Player, VoiceEditSession)` — title + color pill row (BLUE/GREEN/PINK/PURPLE/RED/WHITE/YELLOW) + overlay pill row (PROGRESS/NOTCHED_6/10/12/20) + duration_ticks + fade_in_ticks + fade_out_ticks + audience pill row + preview
- `sendPhrasePanel(Player, VoiceEditSession)` — the script editor (see spec mockup in `phase2-department-panels.md §Voice` — VOICE PHRASE panel)
- `sendPhraseLinePanel(Player, VoiceEditSession, int stepIndex)` — inline panel for editing a single PHRASE step (opened when player clicks [Edit] on a step)

Audience options for MESSAGE and BOSSBAR: `participants`, `invoker`, `all` — pill row, single-select.

---

## Wire into TechManager

In `buildDeptEditSession()`: add a branch for `voice.` prefix (alongside the existing `casting.`, `wardrobe.`, `sound.` branches) returning `new VoiceEditSession(...)`.

In the edit session panel dispatch (the `if/else if` block that calls `sendPanel()`): add:
```java
} else if (editSession instanceof VoiceEditSession voiceSession) {
    VoicePanelBuilder.sendPanel(player, voiceSession);
}
```

---

## Version Bump

In `build.gradle.kts`:
- Change `version = "2.36.0"` → `version = "2.37.0"`
- Update the `shadowJar` comment to match

Do NOT edit `plugin.yml` — Gradle injects the version automatically.

---

## Git Workflow

```bash
git checkout -b claude/ops-029-group5-voice
# ... write code ...
git add src/main/java/com/scaena/shows/tech/VoiceEditSession.java
git add src/main/java/com/scaena/shows/tech/VoicePanelBuilder.java
git add src/main/java/com/scaena/shows/tech/TechManager.java
git add build.gradle.kts
git commit -m "OPS-029 Group 5 Voice: VoiceEditSession (v2.37.0)"
git push origin claude/ops-029-group5-voice
```

Stop there. Alan reviews and merges via GitHub Desktop. Do NOT merge to main, do NOT run gradle.

---

## Notes and Gotchas

- **PHRASE is meaningfully different** from the other four. It's a multi-step container. The panel is a list editor, not a single-param form. The spec mockup in `phase2-department-panels.md §Voice` is the authoritative design — follow it.
- **BOSSBAR timing fields**: `duration_ticks` = total bar life. `fade_in_ticks` + `fade_out_ticks` come out of duration. Hold = duration - fade_in - fade_out. Don't conflate with the OPS-043 start_progress/end_progress fields (not yet shipped).
- **ACTION_BAR re-send**: The executor re-sends the ACTION_BAR every 20t to persist it for `duration_ticks`. The panel just edits the params; don't reimplement that logic here.
- **Text content**: Use MiniMessage format throughout. Text field rows should show the raw MiniMessage string, not rendered. Include a note or color-strip preview if feasible, but don't block on it.
- **No world preview** for Voice — text events fire to the player as preview. No world state to revert.
- Reference `SoundEditSession.java` for the entry-snapshot, dirty-tracking, cancel-restore, and save-as-preset patterns. Those are stable and should be followed consistently.
