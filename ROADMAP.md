# ScaenaShows v2 — Roadmap

> **Revised 2026-03-24:** Alan has directed skipping Phases 2–6 (the ScaenaComposer web UI).
> Claude can author shows directly as YAML without a GUI. The priority is high-quality,
> emotionally resonant show authoring immediately — not a visual editor.
>
> Active work is Phases 7 + 8, which are now unblockers rather than endpoints.

---

## Phase 1 — Java Plugin (Cue Schema)

**Status:** ✅ Complete — 2026-03-24
**Branch:** `main` (commit `73223ea`)

### What was built

- Full v2 plugin: Paper 1.21.x, Java 21, SnakeYAML throughout
- All 11 event categories implemented and tested in-game
- Commands: `/show list|play|stop|stopall|reload`
- JAR-scanning `saveDefaultResources()` for bundled YAML extraction
- `smoothMovePlayer` absolute interpolation (no drift)
- `resolveEntities` handles `participants` and `broadcast`
- `CueRefEvent` reads `cue_id` field
- Bundled shows: `test.showcase.full`, `intro.scaena_sprite`, `intro.young_persons_guide`
- Bundled cues: `atmos.*`, `fx.*`, `mood.*`, `overture.theme_teaser`

### Known remaining items (not blocking Phase 7/8)

- `GLOW` and `CAMERA` events are stubbed — TAB 5.x API coordination needed (SCENA-002)
- `REDSTONE`, `ENTER`, `EXIT`, `PLAYER_SPECTATE`, `PLAYER_MOUNT` — low-priority; stub or defer
- Player-facing strings still placeholder — Scaena voice pass before production launch
- `build/` and `.gradle/` still tracked in git — remove with `git rm --cached` when convenient

---

## Phases 2–6 — ScaenaComposer (DEFERRED)

**Status:** ⏭ Skipped indefinitely

Node.js server, timeline UI, spatial canvas, sketch preview, RCON live preview.

Alan's decision: these add significant complexity for a workflow that Claude can replicate
directly through YAML authoring. Return to these phases only if a GUI proves valuable
after the show library matures.

All Phase 2–6 design artifacts are preserved in `docs/` for future reference.

---

## Phase 7 — ShowSprite: AI Show Authoring

**Status:** 🟢 Active — branch `feature/ai-show-generation`
**Revised definition:** ShowSprite is not a UI widget — it's Claude's authoring identity
and context frame. We build the persona and working method directly, without a GUI host.

### What this phase is actually about

ShowSprite is the name for the creative collaborator role Claude plays when authoring shows.
Building Phase 7 means:
1. Defining the voice clearly enough that every show has a consistent, distinctive character
2. Establishing the show authoring workflow (concept → YAML → in-game test → revision)
3. Identifying the artistic principles that make a show feel like an *experience* rather than a feature demo

### Acceptance Criteria

- [ ] `docs/showsprite.context.md` — persona document committed and in good shape
  - ShowSprite's voice characteristics (tone, vocabulary, what it avoids)
  - The Scaena Theatre concept (what is this space, philosophically?)
  - ShowSprite's relationship to Alan, Zara, Smitty — collaborator, not assistant
  - Example good/bad narration lines (concrete contrast)
- [ ] Show authoring workflow documented — how a session goes from concept to deployed YAML
- [ ] `intro.young_persons_guide` rewritten as an *experience*, not a capability tour
  - The "mechanical listing" problem is solved
  - Pacing feels intentional, not encyclopedic
  - ShowSprite narration has genuine voice
- [ ] At least 2 additional production-quality shows authored and tested

### Notes

- The key critique: shows work when they have Scaena voice; they fail when they become
  mechanical feature listings. The fix is artistic, not technical.
- ShowSprite speaks in `[Sprite]` chat messages. Voice: poetic, present tense, never lists,
  uses silence deliberately, earns wit rather than attempting jokes.
- Direct address: "You are standing in..." not "This event type demonstrates..."

---

## Phase 8 — Cue Library Seeding

**Status:** 🟢 Active (parallel with Phase 7)

### What exists already

8 bundled cues, authored as needed during show development:
- `atmos.ambient.ember_drift` — persistent end_rod particle drift
- `atmos.lights.warm_bloom` / `cool_bloom` — FIREWORK_RANDOM fills
- `fx.confetti_burst` — sound + confetti scatter
- `fx.levitate_and_drift` — levitation → slow_falling sequence
- `mood.arrival` — lightning + thunder + quiet ender dragon
- `mood.wonder.single` — sound + firework + amethyst chime
- `overture.theme_teaser` — 600-tick compressed all-capability showcase

### What the library needs

The current cues were authored to serve specific show moments. The library needs
cues authored from the **emotional/atmospheric function** outward — what does this
cue *do to the audience*, not what Minecraft events does it use.

### Acceptance Criteria

- [ ] 30+ cues, organized by function not event type
- [ ] All cues have `tags:` per taxonomy in spec §10
- [ ] Cue naming follows `[category].[archetype].[variant]` from spec §9
- [ ] Coverage across arc roles: `ramp`, `peak`, `coda`, `breath`, `celebration`, `tension`, `wonder`, `grief`
- [ ] All cues tested in-game
- [ ] Library survey document: what we have, what's missing, what serves which show moments

---

## Open Issues

| ID | Phase | Priority | Description |
|----|-------|----------|-------------|
| SCENA-003 | Phase 7 | ~~**High**~~ ✅ | `showsprite.context.md` — drafted 2026-03-24 from scaenacraft-ops voice corpus |
| SCENA-005 | Phase 7 | **High** | **Source-material scan** — `docs/showsprite.context.md` was seeded from scaenacraft-ops MOTD corpus, scaena-voice skill, ADRs, and `intro.scaena_sprite`. As the server voice evolves (new MOTD lines, NPC dialogue, player-facing strings), scan `scaenacraft-ops/config/motd.md` and `scaenacraft-ops/skills/scaena-voice/SKILL.md` and bring forward any new reference lines or voice guidance. Also: process `intro.young_persons_guide` narration once it is rewritten — those revised lines become the new Sprite voice reference. |
| SCENA-006 | Phase 7 | Medium | **`intro.young_persons_guide` voice pass** — the structure (overture → chapters → fugue → reprise) is sound. The narration fails when it catalogs events instead of inhabiting them. Rewrite Sprite's chapter narration to speak from inside each movement, not from outside explaining it. The chapter experience, not the chapter heading. |
| SCENA-002 | Phase 1+ | Low | GLOW + TAB 5.x API — defer until GLOW needed in production |
| SCENA-004 | Phase 8 | Medium | Cue library survey — what exists vs. what's needed; plan the 30-cue set |
