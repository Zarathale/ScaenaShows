---
department: Set Director
show: demo.archetype_sampler
status: complete
---

# Set Director — Archetype Sampler

## Brief Received

> Portable show. Standard 9-mark grid centered on player. Open outdoor space with sky access.
> No block modifications.

---

## Decisions

**Decision: Fully portable.** `portable: true`, `default_mode: follow`. The show travels with whoever plays it. No world-specific coordinates anywhere in the show YAML.

**Decision: No sets (no PLAYER_TELEPORT).** The player stays where they are. The world comes to them — through light, sound, time of day, and altitude. No teleportation.

**Decision: Standard mark grid.** The 9-position grid is sufficient. Firework patterns and particle effects use `origin_offset` from the anchor, not named marks.

**Decision: No block modifications.** The show must be safe to run on any outdoor surface. No COMMAND block work.

---

## Environment Requirements

The show is portable but has spatial requirements that should be confirmed before playing:
- **Sky access:** Player must be outdoors with clear sky above (40+ blocks clearance)
- **Flat-ish terrain:** The anchor/mark system works best on reasonably flat ground. Extreme slopes will offset firework patterns
- **No low ceiling:** Levitation reaches 35+ blocks at peak. An enclosed space will trap the player in ceiling

These are authoring expectations, not enforced constraints. The show will run indoors — it just won't work as intended.

---

## Notes

R1–R7: No set changes. The portable, world-neutral approach is a permanent design decision for this show.
