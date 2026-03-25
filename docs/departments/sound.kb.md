---
department: Sound Designer
owner: Sound Designer
kb_version: 1.0
updated: 2026-03-25
---

# Sound Designer — Technical Knowledgebase

> Technical reference for the Sound department. Documents what the ScaenaShows Java plugin
> can do for audio playback and control — and how to access those capabilities through YAML.
>
> Creative direction for this role lives in `docs/production-team.md §7. Sound Designer`.

---

## Owned Event Types

| Event | Type | What it does |
|-------|------|--------------|
| SOUND | Bar | Play a Minecraft sound from the anchor location |
| STOP_SOUND | Point | Stop a specific sound or all sounds for the audience |

---

## Capabilities & YAML Syntax

---

### SOUND

Plays a named Minecraft sound from the spatial anchor's location. Volume and pitch are configurable. Duration is the sound's natural length, optionally truncated.

```yaml
type: SOUND
sound_id: minecraft:entity.allay.ambient_with_item
category: ambient     # master | music | record | weather | block | hostile | neutral | player | ambient | voice
volume: 1.0           # 0.0–1.0+ (values > 1.0 increase broadcast range, not loudness beyond 1.0)
pitch: 1.0            # 0.5–2.0 (1.0 = natural pitch; lower = heavier/slower; higher = brighter/faster)
max_duration_ticks: 80   # optional; stops this sound after N ticks (truncation only — no fade)
```

**Behavioral notes:**
- Sound is positional in Minecraft. The server broadcasts from the anchor location; the client applies distance-based attenuation. Sounds played "at the anchor" feel intimate. Sounds at a large offset feel distant.
- `volume > 1.0` increases the broadcast range (the radius at which players can hear the sound), not the perceived loudness once inside that range. `volume: 2.0` doubles the range; useful for making an atmospheric sound audible from anywhere on a large stage.
- The Minecraft sound system does not support seek, start-offset, or fade-out. `max_duration_ticks` is a hard cut (calls `stopSound` after N ticks) — it does not fade. Plan sound layering with this in mind.
- Looping sounds: most Minecraft ambient sounds loop automatically (e.g., `ambient.cave`, `ambient.crimson_forest.loop`). Non-looping sounds play once. A looping ambient bed must be explicitly stopped with STOP_SOUND when the scene changes.

---

### Category Reference

Category controls which audio channel the sound routes through. Players can independently adjust each channel's volume in settings.

| Category | Player channel | Best for |
|----------|---------------|----------|
| `master` | Master volume | Sounds that must be heard regardless of player settings |
| `ambient` | Environmental | Cave sounds, rain beds, atmospheric loops |
| `music` | Music | Background scores, disc tracks |
| `block` | Blocks | Building sounds, note blocks |
| `hostile` | Hostile mobs | Monster sounds used atmospherically |
| `neutral` | Neutral mobs | Creature sounds |
| `player` | Players | Player-specific sounds |
| `voice` | Voice | (if client supports it) |
| `record` | Music disc | Record player sounds |

**Recommended categories for show work:**
- `ambient` — atmospheric loops, environmental beds. Respects player ambient volume setting.
- `master` — critical show sounds that must be heard. Bypasses per-channel settings.
- `hostile` or `neutral` — mob sounds used deliberately (e.g., Warden heartbeat in hostile for maximum impact, or Allay sounds in neutral).

---

### Pitch as a Creative Tool

Pitch changes the speed and frequency of the sound simultaneously.

| Pitch value | Effect |
|-------------|--------|
| 0.5 | Octave down, half speed — very heavy, ancient, subterranean |
| 0.6–0.7 | Deep and ominous — thunder becomes tectonic, presence becomes godlike |
| 0.8–0.9 | Slightly lower — gravity, weight, seriousness |
| 1.0 | Natural pitch |
| 1.2–1.3 | Slightly brighter — urgency, fragility, a touch of lightness |
| 1.5–1.6 | Higher, faster — excitement, alarm, something small and quick |
| 2.0 | Octave up, double speed — tiny, urgent, mechanical |

---

### STOP_SOUND

Stops a specific sound or all sounds for the target audience.

```yaml
# Stop a specific sound:
type: STOP_SOUND
sound_id: minecraft:ambient.cave
category: ambient
audience: participants

# Stop all sounds on a category:
type: STOP_SOUND
category: ambient
audience: participants

# Stop all sounds entirely (nuclear option):
type: STOP_SOUND
audience: participants
```

**Behavioral notes:**
- Always pair STOP_SOUND with its corresponding SOUND event. An ambient loop that isn't stopped continues playing past its intended scene.
- Use category-scoped stops to clear a specific audio layer without silencing other sounds.
- The full-stop (no sound_id, no category) silences everything for the audience. Use before a major scene change or deliberate silence beat.
- STOP_SOUND is instantaneous — no fade. For a "fading out" effect, lower volume progressively with multiple SOUND events before the stop.

---

## Sound ID Reference — Show-Specific Library

A curated selection organized by emotional function. Full list: `minecraft.wiki/w/Sounds.json`.

---

### Atmosphere — environmental beds

| Sound ID | Register | Notes |
|----------|----------|-------|
| `minecraft:ambient.cave` | Unease, something below, the underground | Loops; iconic cave drips |
| `minecraft:ambient.basalt_deltas.loop` | Alien, volcanic, another world entirely | Loops; harsh texture |
| `minecraft:ambient.crimson_forest.loop` | Otherworldly, unsettling, corrupted | Loops; deep unease |
| `minecraft:ambient.warped_forest.loop` | Teal-shift, strange calm, alien stillness | Loops |
| `minecraft:ambient.soul_sand_valley.loop` | Howling void, the place of the dead | Loops; haunting |
| `minecraft:ambient.nether_wastes.loop` | Heat and emptiness | Loops |
| `minecraft:weather.rain` | Rain without weather change | Does not require WEATHER storm |

---

### Presence — arrival, weight, significance

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.elder_guardian.curse` | The curse lands — weight, significance, something unavoidable |
| `minecraft:entity.warden.heartbeat` | Dread proximity, something large is near |
| `minecraft:entity.warden.roar` | Primal, overwhelming, the deep darkness asserts itself |
| `minecraft:entity.ender_dragon.growl` | Ancient power, something that predates the player |
| `minecraft:entity.ender_dragon.ambient` | Continuous ancient presence overhead |
| `minecraft:block.portal.ambient` | The threshold, liminal space, in-between |
| `minecraft:entity.lightning_bolt.thunder` | Consequence, punctuation, the sky responding |

---

### Warmth — joy, community, comfort

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.allay.ambient_with_item` | Small delight, something attended to |
| `minecraft:entity.allay.item_given` | A gift exchanged — intimacy |
| `minecraft:entity.villager.celebrate` | Community, festivity |
| `minecraft:block.note_block.bell` | Clarity, ceremony, punctuation that lands clean |
| `minecraft:block.note_block.chime` | Lighter clarity, a reminder |
| `minecraft:entity.player.levelup` | Arrival, completion, a threshold crossed |
| `minecraft:block.amethyst_cluster.hit` | Crystalline brightness, something rare touched |

---

### Transition — things beginning or ending

| Sound ID | Register |
|----------|----------|
| `minecraft:block.beacon.activate` | Rising, something beginning, emergence |
| `minecraft:block.beacon.deactivate` | Withdrawal, something ending, a light going out |
| `minecraft:entity.experience_orb.pickup` | Completion, the small satisfying close |
| `minecraft:block.end_portal.frame.fill` | The uncanny, a threshold crossed, the irreversible |
| `minecraft:block.conduit.activate` | Underwater awakening, something ancient starting |
| `minecraft:ui.toast.challenge_complete` | Achievement, a moment of recognition |

---

### Grief — weight, loss, quiet devastation

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.iron_golem.hurt` | The protector wounded — loyalty under strain |
| `minecraft:entity.iron_golem.death` | Devotion extinguished |
| `minecraft:entity.wolf.whine` | Distress, loyalty through pain |
| `minecraft:entity.ghast.moan` | Something in anguish at great distance |
| `minecraft:entity.ghast.scream` | Raw grief erupting |
| `minecraft:block.glass.break` | Fragility shattered — the irreversible small loss |

---

### Tension — dread, the unannounced

| Sound ID | Register |
|----------|----------|
| `minecraft:entity.creeper.primed` | The countdown, the thing about to happen |
| `minecraft:entity.enderman.stare` | Being observed, the uncanny watcher |
| `minecraft:entity.phantom.ambient` | Consequence overhead, the cost of avoidance |
| `minecraft:entity.spider.ambient` | Unease, something in the dark |
| `minecraft:block.sculk_sensor.clicking` | Detection, proximity to consequence |

---

## Capability Awareness — System Limitations

> Stage Management owns the full gap registry and ops-inbox workflow. This section documents
> inherent Minecraft sound system constraints the Sound department needs to know. These are not
> ScaenaShows gaps (they cannot be fixed by plugin changes) — they are Minecraft platform limits.
> If you identify a ScaenaShows-specific audio gap, bring it to Stage Management.

| Limitation | Detail |
|------------|--------|
| No seek or start-offset | Minecraft's sound system plays sounds from the beginning. You cannot start a sound at a specific point in its playback. |
| No native fade-out | STOP_SOUND is an immediate cut. Simulate fade by playing progressively quieter SOUND events (volume 0.8 → 0.5 → 0.2 → STOP_SOUND). |
| Sound truncation only | `max_duration_ticks` calls stopSound after N ticks — it is a hard cut, not a fade. |
| Positional only | All sounds broadcast from the anchor location. There is no way to author a sound from a different XYZ offset currently. |
| Looping management | Looping ambient sounds persist until STOP_SOUND fires. Every SOUND that loops must have a corresponding STOP_SOUND in the show timeline. |

No gaps are currently filed for the Sound department. These are inherent Minecraft sound system constraints, not ScaenaShows implementation gaps.
