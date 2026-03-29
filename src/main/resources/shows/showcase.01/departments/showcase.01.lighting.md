---
show_id: showcase.01
department: Lighting & Atmosphere Designer
document: Department Brief
updated: 2026-03-29
---

# Lighting — showcase.01 "Preparing for Battle"

## What This Department Serves

Lighting establishes and preserves the atmosphere native to each location. The show
visits six distinct spaces across very different biomes — each one should feel like
where it actually is. Lighting does not impose a unified palette across the show;
it preserves each location's own light logic.

The one place where Lighting makes a consistent, authored choice: home base. Every
A-section return — six total, including the finale — the player comes back to the
same light. That constancy is part of what makes home base feel like home.

---

## Time-of-Day Arc

**Show start time:** MC tick 12,500 — twilight. Sun just below the horizon; sky still
holds faint color. The blast furnace is already the dominant light source at home base,
but the world is not yet fully dark. This is deliberate: the player enters a world that
is about to become night, not one that already is.

**Natural progression:** No authored TIME_OF_DAY events after T=0. The Minecraft clock
runs normally as the show progresses. Each expedition site receives the player at a
progressively darker sky — which is the show's designed effect.

**A-Final nudge:** At the hold beat in A-Final (immediately before the Vindicator
spawns, in the pause between the Armorer's last line and the reveal), fire
`TIME_OF_DAY: 22000`. This is nearly imperceptible to the player — they're watching
the armor stand — and guarantees sunrise falls during the fight rather than leaving it
to chance. Without the nudge, natural timing lands at ~MC 19700 at show end
(approximately 1:30 AM ingame), which leaves 3,300+ ticks before sunrise — too long
for the "sun cresting as the fight ends" effect. The nudge is subtle; the result is
reliable.

**YAML (two events only):**
```yaml
# T=0 — set show start time
- {at: 0, type: TIME_OF_DAY, time: 12500}

# A-Final hold — nudge to pre-dawn before Vindicator spawn
# Fire at the beat between Armorer's last line and Hero reveal
- {at: [A_FINAL_HOLD_BEAT], type: TIME_OF_DAY, time: 22000}
```

**Why this arc works:** The show begins at twilight and ends in pre-dawn darkness. The
player fights the Vindicator in near-darkness with the sky beginning to lighten. Sunrise
falls during or just after the fight — "the sun rising at the end of the quick fight."
No dramatic TIME_OF_DAY transitions mid-show; the world simply gets darker, naturally,
as the player ventures further from home.

---

## Scene Light Notes

Tracking table: expected sky conditions and block light sources at each moment.
Update as Set's scouting report comes in — coordinates and confirmed set pieces will
sharpen these estimates.

| Scene / Site | Approx MC tick | Sky light | Block light source(s) | Approximate ambient |
|---|---|---|---|---|
| A-open — Home base | 12,500 | ~10–11 | Blast furnace (L13) + lanterns (TBD) | Twilight; furnace warm dominant |
| Site B — Helmet | ~13,200 | ~7 | None proposed; Set to assess | Dusk; open sky; relatively clear |
| Site C — Chestplate | ~14,500 | ~4–5 | Lava adjacent (L15) if Set scouts it | Full night; lava-lit if present |
| Site D — Leggings | ~15,800 | 4 | Campfire (L15) — see below | Deep night; campfire primary |
| Site E — Boots (Swamp) | ~17,000 | 4 (reduced under canopy) | Soul Campfire (L10) — see below | Dark; cold blue-teal dominant |
| Site F — Weapon (Ruins) | ~18,200 | 4 | Campfire (L15) — see below | Near-midnight; campfire center |
| A-Final — Home base | ~18,900–22,000 | 4 | Blast furnace + lanterns (TBD) | Dark outside; furnace-warm inside |

*Swamp note: heavy canopy at Site E can reduce sky light below the open-air floor of 4.
Combined with no block light, this means viable mob spawning within the scene. The soul
campfire is doing double duty — atmosphere and mob containment. Its 9-block no-spawn
radius covers the Armorer's position when placed centrally.*

---

## Home Base

A craftsperson's workshop has a specific quality of light: warm, directional, functional.
Not a dramatic atmosphere — a working one.

**The blast furnace** is the home base anchor. It emits warm orange light at level 13 —
strong enough to dominate any interior, directional enough to create shadows. At MC 12,500
(twilight, show open), it competes with residual sky light; by the A-Final return (~MC
18,900–22,000), it is essentially the only light source in the space. This natural
progression — the furnace becoming more dominant as the world darkens — is the intended
effect. It should feel like the workshop is lit specifically when the world outside goes
dark.

**BLOCK_STATE constraint:** The blast furnace's `lit=true` state requires the BLOCK_STATE
event type, which is not yet implemented (see ops-inbox). Until resolved, showcase.01
uses `block.blastfurnace.fire_crackle` sound only. The visual glow is a future addition.
Lighting documents the glow as the intended home base anchor; Set places the furnace in
the correct position regardless.

**Additional home base light (TBD — await scouting):** Lanterns on the workshop walls are
a reasonable addition after the furnace is positioned. Steve reviews placement once Set
files the scouting report and confirms the interior dimensions.

**Home base light must be identical on every return.** If the time-of-day progression
means sky light changes between A-sections, the furnace still holds the interior constant.
No authored TIME_OF_DAY restore per return is needed — the interior warm is already
independent of the sky.

---

## Expedition Sites

### Site B — Helmet (~MC 13,200, dusk)

The sky is still contributing at dusk (~level 7). High-altitude site with open sky.

**Block light:** None proposed at this point. A single torch at the survey position could
imply someone has been here before — Set assesses during scouting. If the height reads
clearly in dusk light without augmentation, leave it alone. The helmet scene reads best
in working light, not mystery; dusk is close enough.

Steve holds this site's block light decision until the scouting report comes in.

### Site C — Chestplate (~MC 14,500, full night)

Full night — sky at ~4. If Set scouts a lava-adjacent site, lava (L15, hot orange-red)
is the scene's light source and the forge register. The lava glow IS the light — Lighting
does not augment it. If the lava position is wrong (too far from the player arrival point,
not visible), that is a scouting concern, not a Lighting fix.

If Set scouts a surface forge site without lava: Lighting and Set discuss whether any
block light addition serves the forge register. The furnace equivalent applies here.
Steve reviews once coordinates are in hand.

### Site D — Leggings (~MC 15,800, deep night)

**Campfire.** Regular campfire; warm orange.

A road at night with a campfire is a waystation — someone has traveled this road and
stopped here before. The Armorer has. One campfire along the road or at the roadside
near the leggings position. Warm, practical, unadorned.

- Light output: L15, ~8-block usable radius
- No-spawn radius: ~14 blocks (covers the Armorer's position)
- Color temperature: warm orange — the road has warmth in it

**Placement:** Set proposes position in scouting report; Steve confirms the campfire
lands in the player's sightline at arrival and that its warmth reads clearly against
the dark sky.

### Site E — Boots / Swamp (~MC 17,000, dark)

**Soul Campfire — pending Alan's decision.**

Two options:

**Option A — Soul Campfire (L10, cold blue-teal):** Blue firelight in a swamp at night
is uncanny in exactly the right way. "I know what you walk toward." The Armorer lit a
soul campfire in a swamp. That detail says something without saying it. The cold color
temperature also differentiates Site E visually from Sites D and F (both warm). This is
the recommendation.

**Option B — Regular Campfire (L15, warm orange):** Practical and warm. The scene is
already differentiated by biome and lower light output. Simpler.

*Decision deferred to scouting. Zarathale captures canopy density at the Armorer mark;
Steve decides after the scouting report is filed.*

Regardless of choice: the campfire is essential here, not optional. Sky light under
swamp canopy can reach 0. Without block light, this site has active mob spawn risk for
the Armorer. Campfire provides containment regardless of color.

### Site F — Weapon / Ruins (~MC 18,200, near-midnight)

**Campfire.** Single campfire; small; regular (warm orange).

"I kept coming back to this place." He lit a fire here. One campfire in the cleared
central space, the axe nearby in the firelight. Simple. The ruins in firelight. Nothing
more.

- Light output: L15, ~8-block usable radius
- Mood: stillness, not drama
- Placement: central to the cleared space; the player should see the campfire and the
  weapon position simultaneously on arrival

If Effects confirms a particle beat at the discovery moment: Steve ensures the campfire
placement does not wash out the particles. The cleared space should be dark enough that
a particle effect reads without competing with a nearby bright source. If conflict exists,
campfire moves to the edge of the cleared space (ring lighting the perimeter) rather than
the center.

---

## The Finale

**The 22,000 nudge fires here** — see Time-of-Day Arc above.

After the nudge, the world is at MC 22,000 (pre-dawn — still dark, cold, still). The
player faces the armor stand in furnace-lit darkness. The Hero arrives. By the time the
fight begins, the sky will be moving toward first light.

Lighting has one optional moment at the reveal itself: if Steve can find a clean single
state shift that makes the room feel like it recognized what arrived, deploy it. If it
feels manufactured: hold the home base baseline. The furnace-warm interior against the
dark pre-dawn sky is already the right state. The reveal does not require Lighting's help.
But if it earns it, it is worth doing.

Sunrise (MC 0) is the culminating beat — not authored by Lighting in this show, but
arriving naturally from the 22,000 nudge.

---

## What Lighting Does NOT Do

- Does not impose a unified atmosphere across all expedition sites
- Does not use lighting to signal emotional content ("this is sad, so it's blue")
- Does not add drama to the weapon scene beyond the campfire and native environment
- Does not compete with Fireworks in any scene where Mira is present
- Does not add light at Site C if lava is already doing the work

Lighting preserves. It does not impose.
