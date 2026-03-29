---
department: Stage Manager
show: demo.archetype_sampler
status: active
---

# Stage Manager — Archetype Sampler

## Brief Received

> Portable show, outdoor, single player. No entities spawned, no block modifications.
> Full cleanup contract applies: slow_falling cleared on show end/interrupt.
> Monitor for any effects that outlast their intended duration across revisions.

---

## Decisions

**Decision: Cleanup contract confirmed — slow_falling is the only persistent effect.**
The show uses one persistent `slow_falling` effect (duration 3200t from T=380) as the
whole-show descent baseline. This is the only effect that must be explicitly cleared on
stop or interrupt. All other levitation pulses are short-duration and self-clearing.
The plugin's `applyStopSafety()` handles this automatically.

**Decision: WEATHER clear forced at T=0 — no cleanup required at end.**
The forced `TIME_OF_DAY` and `WEATHER` changes persist after show end. This is by design:
the show ends in night sky (22500 horizon glow), which is aesthetically appropriate.
No cleanup needed; these are world-state changes, not player-state changes.

**Decision: No entity spawning — no DESPAWN_ENTITY safety events needed.**
No cast. No entities to clean up. This simplifies the safety contract significantly.

**Decision: PLAYER_FLIGHT safety confirmed present.**
The `PLAYER_FLIGHT` event type added in v2.8.0 includes stop-safety — flight is cleared when
the show ends or is interrupted. Verified in `applyStopSafety()` implementation.

---

## Gap Items Filed (This Show)

| Gap | Revision identified | Status | Filed in ops-inbox |
|-----|---------------------|--------|-------------------|
| Plugin scanner reads `shows/*.yml` only — does not scan subdirectories | R7 (folder structure work, 2026-03-25) | Open | Pending (see Notes) |

---

## Notes

**Plugin scanner gap (to be filed):** The current Java scanner loads shows by scanning
`src/main/resources/shows/*.yml` at build time. The new show folder structure places YAMLs
at `shows/[show_id]/[show_id].yml`. The scanner must be updated to scan one level of
subdirectories. Until this is resolved, `demo.archetype_sampler.yml` must remain accessible
to the scanner (either as a flat file or via symlink). See `src/main/resources/shows/_template/README.md`
for full context on the folder transition plan.

**Action:** Stage Management to file this to `ops-inbox.md` with the following framing:
- **Type:** Gap (structural — affects all shows using folder format)
- **Need:** Scanner reads `shows/*/[show_id].yml` pattern; extracts show ID from folder name
- **Priority:** Medium — not blocking current shows (flat YAMLs still work), but gates folder adoption

**R7 cleanup review:** All 68 levitation events across C2–C13 use short-duration pulses
(20–24t lev durations) plus the one persistent slow_falling from T=380. This is clean.
No sound loops persist across sections (Sound Director confirmed). No particle emitters
persist beyond their intended sections (Lighting confirmed particle counts reset per section).

**Watch (R7):** If the player stops the show mid-run during active levitation, verify they
return to ground cleanly. The persistent slow_falling will clear immediately on stop via
`applyStopSafety()`. Levitation amplifiers drain naturally once the pulse sequence ends.
