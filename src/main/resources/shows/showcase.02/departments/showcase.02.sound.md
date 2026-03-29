---
show_id: showcase.02
department: Sound Designer
document: Department Brief
updated: 2026-03-26
---

# Sound — showcase.02 "The Long Night"

## What This Department Serves

Sound co-leads this show with Lighting. The audio arc — from dusk ambient to night to storm
peak to aftermath to dawn — is Gracie's primary exercise across all three instruments. Sound
must read Lighting's arc before making any decisions.

The show direction says: "Sound layers beneath what is already present — Gracie does not
introduce new elements abruptly, she adds texture to what was always there." This describes
the aesthetic. The night's soundscape isn't built up; it deepens. Something that was at the
edge of hearing moves closer. The storm doesn't announce itself; it arrives.

The critical precision coordination: Sound and Lighting co-design the LIGHTNING strike peak
before either authors YAML. The flash fires; the thunder follows. The exact tick offset between
them is a decision made at intake and held through all revisions.

---

## Per-Scene Direction

### A — Dusk Arrival
Establish the biome's evening ambient bed. What does this specific location sound like at
early dusk? Wind, if it's exposed. Leaves, if there's tree cover. Distant water, if water
is nearby. Whatever the honest sound of this place is at this time of day — establish it.

This is the baseline that everything else will be measured against. The player must feel
this sound as "the world before anything happened" by the end of the show.

No musical scoring here. No SOUND events that are artistic — only the ambient bed. The
arrival is not an event. It just is.

### B — The Night Deepens
Ambient bed shifts. This is not a replacement — it's a deepening. Evening insects, if
biome-appropriate. Distant wolf howl. Phantom wing sounds above (if Casting and Choreography
have placed Phantoms/Bats). The bed becomes more nighttime without breaking from what was
already there.

A bat flying overhead (if Choreography stages one) generates its own sound through AI.
Sound does not replicate this — let the creature be itself. Add texture *around* what
the creatures generate naturally.

This section should feel patient. The temptation to add layers rapidly should be resisted.
Each layer that arrives in the soundscape in this section makes the next arrival feel more
inevitable. Sparse is right; the night has not fully arrived yet.

### C — Something Builds
The atmosphere changes. Lighting's WEATHER decision determines whether rain is present —
if rain, the ambient shifts dramatically (rain sound is significant). If heavy cloud without
rain: the ambient shifts subtly. Sound's response depends on Lighting's call (answer at intake).

**If rain:** Rain ambient activates. Thunder is NOT yet present — that belongs to Section D,
on the LIGHTNING tick. The rain is building pressure without releasing it. The audio is
heavier, wetter, more enclosed.

**If cloud only:** Sound's job is harder here. No dramatic weather audio. The building must
come from something more subtle — a drop in wind, a change in insect texture, a low ambient
hum that sits beneath the normal bed. Think: the air before a storm, not the storm itself.

Camera should not call attention to the change. Sound's texture should shift before the
player consciously registers it.

### D — The Storm Breaks
**The LIGHTNING tick.** Sound's coordination with Lighting:
- LIGHTNING fires at Tick X (Lighting's call, agreed with Sound at intake)
- Thunder (`entity.lightning_bolt.thunder`) fires at Tick X + [offset]
- The offset represents causal distance — not simultaneous, but consequence
- Recommended range: 5–20 ticks (short enough to feel like local strike, long enough
  to feel like physics)
- Both departments agree on the offset and neither changes it without Director sign-off

After the thunder: the rain may intensify or weather behavior may shift (Lighting's call).
Sound responds to whatever Lighting has specified for post-strike weather.

Sprite speaks here — the thing the night has been trying to say. Sound does not score over
Sprite's line. Full stop on sound events in the tick window around Sprite speaking (at minimum:
no new SOUND events firing within 30 ticks before or after the MESSAGE).

Effects may introduce a brief disorientation effect after the strike (darkness, slow_falling
for 2–3 seconds). Sound coordinates on whether any sound reinforces this moment — a deep
rumble, a drone, something that gives physical weight to the flash's aftermath. Optional;
coordinate with Effects at intake.

### E — The Aftermath
The storm passes. Sound begins to settle. What was present before the storm may or may not
still be present — this is a sound decision that says something about what happened.

Departments may make a choice about the aftermath's atmosphere that differs from the
pre-storm night. Sound has latitude here. Options:
- **Silence:** Remove almost all ambient. Let the world be quiet after the storm.
  The player waits in something close to silence. This is bold and effective.
- **Different texture:** What was there before comes back, but changed. The insects
  return but at a different register. The wind is different. Something is absent.
- **Residual storm:** Rain lingers (if Lighting decides residual weather). Rain sound
  continues at reduced intensity.

Coordinate with Lighting. The aftermath's visual and audio qualities should be decided together.

### A' — Dawn
The biome's morning ambient. This is the A' equivalent of the A arrival — but carrying what happened.

For many biomes: birdsong emerges at dawn. If this biome supports it, birdsong is Sound's
single most effective tool for dawn's arrival. It should not announce itself — like Lighting's
incremental TIME_OF_DAY shift, birdsong should begin barely audible and grow into presence.

The A' soundscape should feel like the same place, but different. The dusk ambient was "the
world before anything happened." The dawn ambient is the same world, after.

Sprite speaks here — the second and final line. Same treatment as Section D: no new SOUND
events within 30 ticks of Sprite's word.

After Sprite's line: the ambient bed settles into what it will be when the show ends. This
is the world the player is left in. Make it feel like something a person could live in.

---

## Show-Level Constraints

- Sound reads Lighting's arc before briefing separately. Lighting and Sound co-design the
  storm peak before either writes YAML.
- LIGHTNING tick offset with thunder: agreed at intake, held through all revisions.
- No new SOUND events within 30 ticks of any MESSAGE (Sprite line). Give the words room.
- Fireworks (Mira) are not used in this show. Sound has no pyrotechnic coordination.
- One location, entire show. Ambient bed management is cumulative — STOP_SOUND discipline
  required as the arc shifts. Don't leave residual layers active when the atmosphere changes.

---

## Intake Questions for Sound (after reading Lighting's arc)

1. **Section C WEATHER:** What is Lighting's call — rain or cloud? Sound's Section C
   design depends on this answer.
2. **LIGHTNING tick offset:** What is the agreed tick delay between flash and thunder?
3. **Section D post-strike:** Does Sound author a resonance/drone/weight effect after the
   strike? If yes, what sound and duration? Coordinate with Effects.
4. **Section E aftermath:** Silence, different texture, or residual storm? Confirm with
   Lighting's weather aftermath decision.
5. **Section A' dawn:** Is birdsong appropriate for this biome? If not, what marks dawn
   sonically?

---

## Decisions
*Filled after Lighting presents arc to Director — Sound reads that arc first.*

## Revision Notes
*Added after each in-game test.*
