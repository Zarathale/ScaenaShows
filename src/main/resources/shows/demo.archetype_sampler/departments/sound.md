---
department: Sound Designer
show: demo.archetype_sampler
status: active
---

# Sound Designer — Archetype Sampler

## Brief Received

> Opening audio state: silence (music killed at T=0). Key sound moments: thunder for
> arrival (C7), level-up for joy (C8), cave ambient for grief/mystery (C5). Looping layers:
> none held across sections. Closing audio state: coda resolves.

---

## Decisions

**Decision: Music killed at T=0.** The Minecraft background music channel is silenced at show start. This gives the ambient and event sounds clean space — no competing musical bed they have to fight against.

**Decision: Each section owns its own audio — no persistent loops.** Ambient beds are fired per section and either time out naturally or are explicitly stopped. This prevents audio from one section bleeding into the next and clouding the emotional transition.

**Decision: Pitch as a primary expressive tool.** The Sound Designer makes heavy use of pitch modification to create emotional register variation from a small set of sound IDs. Thunder at pitch 0.55 is subterranean and inevitable; cave ambient at pitch 0.55 is geological and slow. The same sound library reads very differently at different pitches.

---

## Audio Arc

| Section | Key sounds | Register |
|---------|-----------|---------|
| C1 | STOP_SOUND master (kills music) | Silence — the theatre going quiet |
| C2 | None (deliberate) | Particles arrive in silence; the audio absence is the first breath |
| C3 | Soft guitar note (0.4/1.1) | Light appears with a small sound — warmth confirmed |
| C4 | Soft harp note (0.4/0.9) | Contrast with C3: different texture for the cool shift |
| C5 | `ambient.cave` (vol 0.60 / pitch 0.55) | Slow, subterranean, below the player |
| C6 | Silence | Held breath after the cave sound |
| C7 | Thunder hit 1 (0.6/0.9) + thunder hit 2 (0.4/0.55) | Arrival — strike and echo rolling away |
| C8 | `entity.player.levelup` (0.8/1.1) | Joy confirmed with sound |
| C9 | `entity.ender_dragon.flap` (noon snap) + `ambient.cave` (night snap) | Intrusion, then return |
| C10 | Warm fireworks have inherent sound | Building through visual/audio together |
| C11 | Ascending tone + `block.amethyst_cluster.hit` (chime) | Launch + crystal peak |
| C12 | Soft tone (0.55/0.60) + amethyst chime (0.40/1.30) | The quiet before the peak |
| C13 | `coda.curtain.quiet` fires — resolves | Gentle close |

---

## Notes

**R5:** C7 thunder redesign — dragon growl removed, replaced with two-hit thunder pattern. Hit 1: `entity.lightning_bolt.thunder` (vol 0.6, pitch 0.9). Hit 2: same sound (vol 0.4, pitch 0.55) ~5 ticks later, simulating the sound rolling across the sky after the initial strike. The two-hit design creates spatial depth that the single dragon roar didn't have.

**Watch question (R7):** Does C2's deliberate audio silence (no sound at all during the first ember drift) feel like a meaningful presence, or like something was missing? This is the show's opening bet — that absence of sound is itself atmospheric.
