---
department: Lighting & Atmosphere Designer
show: demo.archetype_sampler
status: active
---

# Lighting & Atmosphere Designer — Archetype Sampler

## Brief Received

> Opens at T=8000 (mid-afternoon). Three TIME_OF_DAY steps to night by end of C1.
> WEATHER clear forced at T=0. Night sky remains throughout until show end.

---

## Decisions

**Decision: WEATHER clear forced at T=0.** Previous revisions were sabotaged by server rain muddying particle visibility and bleeding into the sound bed. Clear is forced at the very first tick — no assumptions about server weather state.

**Decision: Three-step dusk transition at C1.** Single snap (R4) read as abrupt. Three TIME_OF_DAY steps (8000 → 13000 → 18000) across ~80 ticks reads as continuous dimming — a gradual "lights going down" that matches the theatrical framing of C1 ("Houselights down").

**Decision: Night maintained throughout.** Once night is established, it holds through C1–C8. The C9 noon snap (`TIME_OF_DAY: 6000`) is a deliberate shock — daylight as intrusion, disruption of the dark atmosphere. The snap back to night at C9's end closes the parenthesis.

**Decision: Atmospheric particles drive visual texture.** Since no entity-based lighting exists, `atmos.ambient.ember_drift` (warm, upward-drifting particles) provides the primary visual texture during the atmospheric sections. Particle count increased to 8 (from 5 in earlier revisions) for sufficient density at higher altitudes.

---

## Lighting Arc

| Section | World time | Weather | Notes |
|---------|-----------|---------|-------|
| C1 start | 8000 (mid-afternoon) | clear (forced) | Initial state |
| C1 T=40 | 13000 (dusk) | clear | Step 1 of dusk |
| C1 T=80 | 18000 (night) | clear | Step 2 — full dark |
| C2–C8 | 18000 (night) | clear | Held through atmospheric and joy sections |
| C9 | 6000 (noon) | clear | Shock — daylight burst. ~13s |
| C9 end | 18000 (night) | clear | Snap back to dark |
| C12–C13 | 22500 (horizon glow) | clear | Pre-dawn tint at peak altitude |

---

## Notes

**R5:** Dragon growl replaced by rolling thunder in C7. This is partly a Lighting concern — the thunder pairs more naturally with a lightning/night atmosphere than a dragon roar would.

**R7:** C1 now explicitly begins at day (TIME_OF_DAY 8000 set at T=0) rather than relying on server state. The dusk transition is now intentional and guaranteed, not dependent on what time it was when the show started.

**Watch question (R7):** Does the show's sustained night feel atmospheric rather than just dark? Does the C9 noon snap feel genuinely disorienting after the sustained dark — or has the night worn out its welcome by then?
