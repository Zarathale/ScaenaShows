---
owner: dept-kb-builder skill
updated: 2026-03-25
notes: >
  Authoritative list of approved external sources for department KB research.
  The dept-kb-builder skill reads this file before doing any web research.
  New sources require Alan's approval before being added and used.
  Always check internal repo sources first — this file is only for what the
  repo cannot cover on its own.
---

# ScaenaShows — Approved External Research Sources

When building or auditing department knowledgebases, internal repo sources (cue-show-yaml-schema.md,
production-team.md, cue-library-survey.md, existing KBs, cue YAMLs) take priority.
Use sources from this list only when internal sources are insufficient.

If a needed source is not listed, propose it to Alan before fetching.

---

## Approved Sources

| Source | Base URL | Best for | Added |
|--------|----------|----------|-------|
| Minecraft Wiki | `https://minecraft.wiki` | Entity types, mob variants, sound IDs, particle names, biome differences, TIME_OF_DAY sky values, game mechanics | 2026-03-25 |
| PaperMC Javadoc | `https://jd.papermc.io/paper/1.21/` | Java API reference — entity class fields, event types, enum values, method signatures | 2026-03-25 |
| PaperMC Docs | `https://docs.papermc.io` | Plugin development patterns, API guides, server configuration | 2026-03-25 |

---

## Pending Approval

*Sources proposed but not yet approved — do not use until moved to Approved above.*

| Source | Proposed for | Proposed date | Status |
|--------|-------------|---------------|--------|
| | | | |

---

## Notes on Usage

**Minecraft Wiki:** Prefer `https://minecraft.wiki/w/[topic]` over the older fandom wiki
(`minecraft.fandom.com`) — the wiki.gg version is better maintained and has more accurate
technical data. For sound IDs specifically, the Sounds page lists all valid `minecraft:*`
identifiers and their in-game contexts.

**PaperMC Javadoc:** Useful for confirming exact enum names (e.g., `EntityType.VILLAGER`,
`Particle.END_ROD`, `Sound.ENTITY_LIGHTNING_BOLT_THUNDER`) that must match what the
ScaenaShows Java executor expects. When a YAML field value isn't working, cross-reference
here.

**When in doubt about a Minecraft version:** ScaenaShows targets Paper 1.21.x. Check that
any variant, sound, or particle you're documenting exists in 1.21.x — some are version-gated.
