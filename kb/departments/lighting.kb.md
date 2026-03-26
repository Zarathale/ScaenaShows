---
department: Lighting & Atmosphere Designer
owner: Lighting & Atmosphere Designer
kb_version: 1.1
updated: 2026-03-25
---

# Lighting & Atmosphere Designer — Technical Knowledgebase

> Technical reference for the Lighting & Atmosphere department. Documents what the ScaenaShows
> Java plugin can do for time of day, weather, and lightning — the world-state events that affect
> all players — and how to access those capabilities through YAML.
>
> **Scope:** This department owns events that change the world state visible to all players.
> Particles, perceptual effects on the target player, and CAMERA screen distortion belong to
> the Effects department. See `kb/departments/effects.kb.md`.
>
> Creative direction for this role lives in `kb/production-team.md §6. Lighting & Atmosphere Designer`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| TIME_OF_DAY | Point | Snap world time to a specified value |
| WEATHER | Bar | Set weather state (clear / storm / thunder) |
| LIGHTNING | Point | Cosmetic lightning strike at an offset (no damage, no fire) |

**Not owned by this department:** PARTICLE (→ Effects), EFFECT on players (→ Effects), CAMERA screen distortion (→ Effects). If it is applied *to* a player rather than *to the world*, it belongs to Effects.

---

## Capabilities & YAML Syntax

---

### TIME_OF_DAY

Snaps the world clock to a specified time value instantly. Gradual transitions are composed by firing multiple TIME_OF_DAY events in sequence with short intervals between them.

```yaml
type: TIME_OF_DAY
time: 18000   # integer; 0–24000
```

**Time value reference:**

| Value | Time of day | Light quality |
|-------|-------------|---------------|
| 0 (= 24000) | Sunrise | First light, warm horizon |
| 1000 | Early morning | Clarity, long shadows |
| 6000 | Noon | Full daylight, maximum visibility, no shadows |
| 8000 | Mid-afternoon | Warm, slightly angled |
| 12000 | Sunset | Warmth fading, golden-orange |
| 13000 | Dusk | Liminal, the moment before dark |
| 18000 | Midnight | Full dark, deepest night |

**Gradual dusk transition (from R7 archetype sampler):**
```yaml
- at: 0
  type: TIME_OF_DAY
  time: 8000      # mid-afternoon

- at: 40
  type: TIME_OF_DAY
  time: 13000     # dusk

- at: 80
  type: TIME_OF_DAY
  time: 18000     # night
```
Three steps across 80 ticks reads as a continuous fade rather than discrete snaps. More steps = smoother transition. 40t intervals are perceptually gradual at show scale.

**Behavioral notes:**
- TIME_OF_DAY is server-wide — it affects all players on the server, not just show participants. This is a major consideration for shared-server shows.
- Underground or enclosed environments: sky time changes have no visible effect. The ambient light level comes from blocks, not sky.
- Outdoor environments: the sky and sun/moon position change immediately. Use this creatively — a sunset during an opening monologue is a free lighting effect.
- Consider restoring time at show end if the show significantly changes it. Document the original time in the run sheet and fire a TIME_OF_DAY restore event at show end (or use COMMAND: `/time set <original>`).

---

### WEATHER

Sets world weather state. Duration is optional; if omitted, weather persists until manually changed or another WEATHER event fires.

```yaml
type: WEATHER
state: clear    # clear | storm | thunder
duration_ticks: 400   # optional; how long this weather state lasts
```

**States:**
- `clear` — no precipitation, full sky visibility, natural ambient sound
- `storm` — rain (or snow in cold biomes), reduced visibility, continuous rain ambient sound
- `thunder` — storm + random thunder strikes from the server (distinct from scripted LIGHTNING events); dramatic ambient rumble

**Behavioral notes:**
- Weather changes the ambient audio layer significantly. A `storm` state immediately adds a bed of rain sound that the Sound Designer must account for. A `thunder` state adds random server-generated thunder that competes with scripted audio.
- Weather is server-wide (same consideration as TIME_OF_DAY).
- `clear` at T=0 is a common setup step — it ensures the show starts in a known weather state regardless of what the server weather was before play. The R7 archetype sampler uses this.
- If using WEATHER for a scene effect, pair with a `clear` restore at show end or after the effect passes.
- Duration: if set, the weather reverts to its previous state after `duration_ticks`. If omitted, the new state persists indefinitely.

---

### LIGHTNING

A cosmetic-only lightning strike at an XYZ offset from the spatial anchor. No damage, no fire, no block ignition. The visual strike and a single thunder crack play at the offset location.

```yaml
type: LIGHTNING
x_offset: 5
y: 64           # absolute Y in world coordinates
z_offset: 0
```

**Y is absolute world coordinates,** not relative to the anchor. To strike at ground level near the stage, use the terrain height at the anchor's XZ.

**Behavioral notes:**
- The thunder is tied to the visual strike and cannot be suppressed. Every LIGHTNING event contributes to the audio landscape — the Sound Designer must account for it.
- For dramatic timing: one strike at a specific mark as a punctuation beat.
- For ominous slow sequence: one every 60–120t with deliberate silence between — the sky threatening.
- For chaos: three or more at different offsets on the same tick — the world breaking.

**LIGHTNING lives here because it is an atmospheric and environmental event** — a change to the sky and world register, not a detonation. The Fireworks Director is aware that strikes can accompany a show's pyrotechnic moments, but LIGHTNING is not authored there.

---

## Fireworks as Light

Fireworks are owned by the Fireworks Director — the Lighting & Atmosphere Designer does not author FIREWORK_* events. But every burst illuminates the player's world for a moment, making pyrotechnic color palette a shared concern. The Lighting Designer is consulted on firework palette choices that affect the ambient scene register: a warm gold burst in a cool-lit scene makes a statement; a matching burst disappears into it. See `kb/departments/fireworks.kb.md` for firework authoring reference.

---

## Capability Awareness — Limitations & Gaps

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents what
> the Lighting department needs to know for show authoring. File new gaps via Stage Management.
> Full registry: `kb/departments/stage-manager.kb.md` → Active Gap Registry.

No gaps are currently filed specifically for the Lighting & Atmosphere department. All core tools (TIME_OF_DAY, WEATHER, LIGHTNING) are implemented and functional.

**Limitations to be aware of:**
- TIME_OF_DAY and WEATHER are server-wide — they affect all players on the server, not just show participants.
- Underground or enclosed environments: TIME_OF_DAY has no visible effect. Ambient light comes from blocks, not sky.
- LIGHTNING thunder cannot be suppressed — the sound is always tied to the visual strike. Coordinate with the Sound Designer on any scene that uses LIGHTNING.

**Capability Status Summary:**

| Instrument | Status | Notes |
|------------|--------|-------|
| TIME_OF_DAY | ✅ Verified | Server-wide; instant snap; gradual via multi-step sequences |
| WEATHER | ✅ Verified | Server-wide; duration optional |
| LIGHTNING | ✅ Verified | Cosmetic only; thunder always fires with strike |
