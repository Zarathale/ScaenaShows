---
show_id: showcase.02
working_title: "The Long Night"
show_type: Calibration (Showcase 2 of 3)
development_stage: Brief
version: —
updated: 2026-03-26
---

# Show Brief — showcase.02 "The Long Night"

```
SHOW BRIEF
----------
Title (working):  The Long Night
Show ID:          showcase.02
Duration (target): ~12,000–14,400 ticks (10–12 minutes)
Context:          Calibration Showcase. Second of three. Educational focus: cumulative
                  arc-building, departments thinking across the full show rather than
                  isolated moments. Lighting leads; all other departments respond to the
                  world Lighting creates.
Development stage: Brief
Show type:        Calibration (Showcase)

THE ARC
The player arrives in a particular place at dusk. The world settles into night around them.
Creatures emerge. The atmosphere builds. A storm rolls in. Lightning falls — once, and it
matters. The storm passes. What follows is quieter than what came before. Dawn comes. The
player is still in the same place, but the night has moved through them and there is no
mistaking that something happened here.

THE PLAYER
A witness. Someone who stayed when most people would have gone inside. They did not make
the night happen — they were simply present for it. That presence is the whole show.

TONE
Environmental, patient, strange in the way any night is strange if you pay attention to it.

WHAT THEY CARRY AWAY
The feeling of having stayed through something and come out the other side.

CONSTRAINTS
- Duration: patient — do not rush any zone; Stage Management sets the clock
- Structure: Through-composed with bookend (A B C D E A') — one location, no TP
- Location: one place, established and characterized before YAML begins
- Teleportation: none. The player does not leave. The world comes to them.
- Sprite lines: two maximum — one at the storm's peak, one at dawn (A')
- Lighting leads: when Lighting's choices conflict with any other dept, Lighting wins
  unless brought to the Director
- Effects: one moment of disorientation during the storm only; stay in verified lane
- Alan notes: Lighting and Sound co-lead and should agree on the storm moment before
              briefing other departments. The A' section must feel different from A
              without being told to.
```

---

## Scene Arc Overview

| Section | Name | Time of Day | Emotional Zone | Lead Departments |
|---|---|---|---|---|
| A | Dusk Arrival | TIME_OF_DAY: dusk (~12000) | Settling, orienting | Lighting, Sound, Camera |
| B | The Night Deepens | TIME_OF_DAY: deep night | Nocturnal, atmospheric | Sound, Casting, Choreography |
| C | Something Builds | Weather begins | Tension, pre-storm | Lighting (WEATHER), Sound |
| D | The Storm Breaks | LIGHTNING + storm peak | Peak — the whole show here | Lighting, Sound, Camera, Effects |
| E | The Aftermath | Storm passing, still dark | Quiet, aftermath | Sound, Lighting, Camera |
| A' | Dawn | TIME_OF_DAY: early dawn (~23000) | Recognition, changed | Lighting, Sound, Voice |
