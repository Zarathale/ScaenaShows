---
show_id: showcase.01
document: Show Direction
updated: 2026-03-31
stage: Brief
---

# Show Direction — showcase.01 "Preparing for Battle"

> This document governs all department decisions. Read it before making any creative call.
> It is updated when the arc changes significantly. Check the stage field above.

---

## The Story

A devoted companion — small, earnest, not built for combat — is equipping a Hero for something that matters. The armor stand at home base stands empty at the show's open. The companion sets out five times, once per piece of gear: helmet, chestplate, leggings, boots, weapon. Each expedition is a procurement run — he knows where each piece is, and he goes to get it. Each return fills one more slot on the armor stand. At the final return, the armor stand is full — and the Hero arrives to wear it.

The show is not about the battle. It is about the preparation. The devotion in the doing.

**The player's role:** Observer in the antechamber. They are present in the Armorer's workshop — the working space where the companion does his craft. The armor stand is in front of them. The Vindicator is behind a wall. They are the witness between the maker and the made. They travel with the companion on each expedition, then return to the same room, the same stand, the same sound of a furnace running. At the end, they see who it was all for — and understand they were standing in the preparation the whole time.

---

## Non-Negotiables

**1. The armor stand is the show's spine.**
It is present at home base from the first moment. At show open, it is bare. On every A-section return, one slot is visibly filled — in sequence, bottom to top or in dramatic order as Wardrobe designs. By the penultimate return, four slots are full. The final piece — the weapon — is placed at A-Final, and then the Hero arrives. If the armor stand does not feel like it is becoming something real, the show has failed its central image.

**2. The companion is a character, not a delivery mechanism.**
Voice writes their lines. The companion goes out, finds the piece, brings it back. What they say along the way is specific to them — their scale, their devotion, their slight absurdity relative to the epic task they've taken on. They do not explain the story. They live it.

**3. Each expedition earns its piece.**
The location for each armor slot must feel like where that piece belongs. The helmet belongs somewhere high. The chestplate belongs somewhere forged. The weapon belongs somewhere that asked something of whoever left it. The connection between place and piece is the show's logic — Set and Voice must agree on it per expedition.

**4. The Hero reveal is the show's one peak.**
Everything else leads to it. Departments must not compete with it. No expedition builds a bigger visual moment than the finale. The question for every department at every scene: *does this serve the reveal, or crowd it?*

**5. The relationship is felt, not explained.**
Voice does not say "I am doing this for the Hero because I love them" or "this is what loyalty looks like." The devotion lives in the companion's decisions — going again, finding better, coming back. Departments do not annotate the emotional content. They deliver it and trust the player to receive it.

**6. Set is the first mover. All six locations are a fresh slate.**
The previous scouting brief is retired. Zarathale scouts six entirely new locations, each selected for its resonance with a specific armor slot. No YAML is written until real-world coordinates exist for home base and all six expedition sites.

**7. The armor set is a unified aesthetic statement.**
The Hero does not wear six random pieces. Wardrobe designs the complete set before any expedition is written — the aesthetic of the full kit determines what "finding the chestplate in a forge" looks like versus "finding it in a ruin." The set design of each expedition follows from the armor identity, not the other way around.

**8. Casting is locked. The Vindicator spawns at show open and is present the entire show.**
Companion: Armorer Villager. Hero: Vindicator. Both confirmed. Gate 1 is closed.

The Vindicator spawns at Scene A open (`SPAWN_ENTITY`, AI locked, no equipment). He
remains behind the wall — present, contained, waiting — for the full duration of the show.
He never despawns until the show ends. As each armor stand fill fires on A-section returns,
a corresponding `ENTITY_EQUIP` event equips him with the same piece behind the wall.
The player sees the stand filling. They do not see what is also being dressed on the other
side of the door.

The reveal at A-Final is the iron door opening. No `SPAWN_ENTITY` at the finale — he is
already there and already fully equipped. Stage Management ratifies the equip timing at intake:
each `ENTITY_EQUIP` on the Vindicator fires in the same tick window as the corresponding
armor stand fill, not as a separate scene beat.

After the Armorer steps back ("I'll leave you two to it."), the show holds, then
`PLAYER_CHOICE` fires — "What now?" — presenting two branches:
- **Fight** — countdown 5-4-3-2-1 GO, AI releases, boss health bossbar appears (OPS-026)
- **Walk away** — Vindicator says "LATER." (deep red, CHAT), then despawns

All combat parameters are in `show-params.md §Battle Sequence`. OPS-009 (PLAYER_CHOICE)
and OPS-026 (boss health bossbar) are the enabling capabilities.

**9. The Vindicator is present at home base from show open — locked 2026-03-29.**
The home base set includes a wall separating the workshop from the Vindicator's holding
space. An iron door in that wall is the show's central spatial mechanic:

- Default state: door open
- Show start (Scene A open): door closes on cue (REDSTONE event) — the clank is the
  show's first beat, before the Armorer's first line
- A-Final (post-choice): door opens at the start of whichever branch the player chooses.
  The Vindicator does not move — he stays in his holding position, AI-locked, the whole
  show. In the **fight branch**, the player steps through the open door and discovers
  him frozen — the countdown fires, GO releases his AI. In the **walk away branch**,
  the door opens, he says "LATER.", then despawns. The door is the branch's opening
  beat in both cases.

The player is enclosed in the workshop for the duration of the show. They hear the
Vindicator behind the door. They do not see him until the reveal.

The Vindicator may have a text voice — short, blunt lines in a distinct format (color,
caps) heard through the wall. The Armorer may address him directly. See `departments/showcase.01.voice.md`
for full direction notes on the Vindicator offstage voice.

Stage Management owns the iron door stop-safety cleanup cue — if the show is interrupted,
the door must be manually openable. This is a non-negotiable safety item.

**10. The find-firework pattern — locked 2026-03-29.**
A firework fires between the Armorer's line 2 and line 3 at every expedition scene.
Line 2 names the piece. The firework fires. Line 3 is the count and departure.
Simultaneous with or just after the item appearing in the Armorer's main hand.

This pattern fires at Sites B, C, D, and E. It does NOT fire at Site F (The Weapon).
The absence is intentional and now doubly powerful: Site F is the final expedition.
By Scene F, the player has internalized the pattern across four scenes. The silence
where the firework should be — at the show's most weighted stop, on the last piece
before the reveal — is the scene's emotional center. Departments must not compensate
for this absence. See `departments/showcase.01.fireworks.md`.

---

## Tonal Register

The show's voice is an expedition log written by someone who knows exactly what they're doing and why. Small creature, epic task. The companion is competent at procurement; they are not Aragorn. The stakes are real because the companion treats them as real — not because the show tells you they are.

The register is: *devoted, spare, slightly over their head but fully committed.* The humor, where it lives, comes from the gap between the companion's scale and the task's scale. A small armorer carrying a chestplate that is clearly too heavy. A procurer who knows the exact forge they need and goes to it without ceremony. The comedy is in the specific and the earnest, not in winking at the player.

This is not a natural history documentary. It is a mission log. Tone shifts slightly per expedition — the weapon scene has a different weight than the boots scene. Voice should feel the shape of each piece and write accordingly.

**The show's central dramatic irony — locked, not negotiable:**
The Armorer is preparing the Vindicator to fight the player. The player does not know this
until the finale. Every expedition the player witnesses, every piece of the kit they watch
go on the armor stand, is preparation for their own opponent.

The Armorer's "you" is addressed to the Vindicator throughout — but the player is standing
right there receiving it. The ambiguity is structural, not accidental. Departments must not
resolve it early. No staging, lighting, or sound choice should tip the player off before the
Vindicator arrives. The reveal belongs to the finale and to no scene before it.

The Armorer is not villainous. They are professional and devoted. The irony is in the
situation, not in the Armorer's intent. They prepared both sides as well as they could.
That's the job. The finale line — *"I'll leave you two to it"* — is warm, not menacing.

---

## Watch For

**The companion becoming the hero.** The companion's devotion is moving precisely because they are not the one going to battle. The moment the companion's arc eclipses the Hero's reveal, the show loses its payoff. Every Voice line should be asking: *am I writing the sidekick, or am I accidentally writing the protagonist?*

**Expedition length creep.** Each scene does ONE primary thing: find the piece, feel the place, come back. If a department adds a second beat, the question is: does this serve the piece's connection to this location, or is it a capability demonstration? Ask it out loud.

**The armor stand filling too fast or too slow.** The equipping rhythm at each A-section return is a Stage Management pacing problem. Too fast and it reads as bookkeeping. Too slow and it loses its visual spine function. Each fill should land as a small, satisfying beat — not a ceremony, not a footnote.

**The Hero underwhelming at reveal.** The Hero cannot be a visual afterthought. Casting must select a mob that reads as *worthy* of six expeditions when it appears fully equipped. If the player's reaction is "...oh, a zombie," the reveal has failed. Casting should propose with this in mind.

**Effects in the weapon scene.** The weapon scene (Scene F) has the show's most emotionally weighted stop. Effects, if present at all, must register as gravity — not spectacle. A levitation beat here would be tonally wrong. Read the room.

**Fireworks.** The find-firework pattern fires at four scenes (B, C, D, E). The finale is the candidate for an additional burst — scaled up from the expedition pattern, or absent entirely if the reveal lands without it. Mira proposes both options at intake. Fireworks at the weapon site (F) are explicitly off the table — the silence there is structural.

---

## Departments with Elevated Priority

**Casting (First narrative gate)**
Before Set can be fully briefed, Casting must answer: who is the companion, and who is the Hero? The companion identity shapes Voice and Wardrobe. The Hero identity shapes the finale mechanics. Casting proposes both. Alan confirms. Then Set scouts.

Casting also flags the Java constraint: the Hero mob must support all 6 equipment slots. `SPAWN_ENTITY` with a populated `equipment:` block handles this at spawn; `ENTITY_EQUIP` can equip post-spawn. Confirm that the chosen Hero mob species supports all six slots.

**Wardrobe (Unblocks Set and Voice)**
The complete armor set must be designed before Zarathale scouts. The aesthetic of the gear — iron and leather? Diamond and chain? Netherite and gold? — determines what "the place where this piece was found" feels like. Wardrobe delivers the armor set identity before Set's scouting brief is issued.

**Set (Second mover, after Casting and Wardrobe)**
All six locations are new. Zarathale scouts with the armor slot as the creative lens per location. Location and piece must belong together. Set also owns home base: the armor stand placement, the companion's home space, and whatever the player sees when they arrive for the first time.

**Voice (Shapes the companion)**
The companion's voice is the show's emotional current. Voice writes for someone who is not performing — they are doing. Spare lines. Specific observations. The companion knows this Hero; they do not explain that they know them.

**Stage Management (Owns the finale alignment)**
The reveal sequence is the show's most technically precise moment. Whether the Hero spawns fully equipped or is equipped in sequence on stage — Stage Management owns the tick alignment. This decision is made at intake with Casting. The call: does sequential `ENTITY_EQUIP` serve the drama of the moment? Or does spawning the Hero already wearing the full kit let the visual do the work instantly? Both are available. One is right for this show.

---

## Scene-Level Direction

### A — Home Base: "The Workshop"
*"The antechamber between the maker and the made."*

This is the Armorer's workspace. A craftsperson's shop — functional, specific, slightly worn. A blast furnace runs here. The armor stand is the centerpiece. The Vindicator is behind the wall, separated by an iron door. The player arrives into this space and stays in it — they are the witness in the antechamber.

**Vindicator spawn:** At Scene A open, the Vindicator spawns in his holding position (AI locked, no equipment). He is present from this moment forward. His spawn is not the show's reveal — it is its foundation. Everything that follows is preparation for what is already waiting.

The player is between two things they don't fully understand yet: an Armorer who is completely calm and professional, and something behind a closed door that is not. By the end of the show, they understand both.

The companion speaks once at opening (after the door clanks shut): a line that establishes the mission without narrating it. Each A-section return is a brief, functional beat. One slot fills. The companion's return lines are field notes — something specific to the expedition just concluded, not a recap. The armor stand does the narrative accumulation; the companion does not need to.

The final A-return is different from all others. See Scene A-Final below.

Camera orients the player toward the armor stand on arrival. Then releases.

### B — Scene 1: "The Helmet"
*"Start with the head."*

The helmet is protection for what you think with. The location should have height, exposure, or the quality of being seen-from — a watch point, a peak, a place where the sky is close. Whoever left this helmet here chose this place deliberately; it belongs at height.

The companion arrives, assesses, collects. No ceremony. The work is the point.

Sound: the location's atmosphere is the score. This is the show's opening expedition — the companion is fresh, the task is just beginning. Tone: purposeful.

### C — Scene 2: "The Chestplate"
*"Where things are made."*

The chestplate is the largest piece and carries the most visual weight. The location is a forge, a smithy, a place of making — active or abandoned. Nether is an option (netherite calls for it). An overgrown foundry is an option. A place where the heat is still in the walls, or the memory of it is.

This scene has more gravity than B. The companion is further into the task and feels it. Voice has a little more weight here.

Lighting: if anywhere in the show benefits from forge-warm light, it is here.

### D — Scene 3: "The Leggings"
*"For the distance between here and there."*

Leggings protect movement. The location is a traversal space — a road, a river valley, a path through a biome that asks something of whoever walks it. The companion has come a long way. The location reflects that.

This is the show's middle beat. Not the most dramatic, not the lightest. Set should find a place with real distance in it — a horizon, a path that disappears into something.

### E — Scene 4: "The Boots"
*"The ground is different everywhere."*

The boots are tactile — they're about what's underfoot. The location should have distinctive terrain: magma coast, snowfield, desert, deep swamp. Whatever the terrain, the companion's relationship to it is physical. The piece and the place share a logic.

Effects may have an opportunity here if the terrain type supports a movement sensation (slow falling on deep snow, slight levitation on magma — only if Set confirms safe altitude). Effects: propose, do not assume.

### F — Scene 5: "The Weapon" *(final expedition)*
*"This one required a decision."*

The weapon is the piece that implies intent — and it is the last piece. This is the show's final expedition. The location is a place where something was decided — a ruin, a site of past conflict, a place that asks: *are you sure?* The companion does not answer out loud. They pick up the axe and come home.

This is the show's most weighted stop AND its conclusion. Voice has the show's most important single line here — or is silent. Silence is allowed if the location does the work. The departure from this scene leads directly to the reveal at home base.

No fireworks. No levitation. No effects that reach for spectacle. The weight of this scene is moral, not visual. Departments: do less here, not more.

The absence of the firework has accumulated force: the player has felt it at B, C, D, and E. The silence at F — the last expedition, the weapon, the piece that implies what this has all been for — is the scene's entire emotional content. Hold it.

### A-Final — The Reveal and The Choice
*"Here."*

The companion returns with the weapon. The armor stand is full. The Vindicator has been
there the whole time — spawned at show open, AI locked, equipped piece by piece behind the
wall as each armor stand fill fired. The reveal is not a spawn. The reveal is not even the
door yet. The reveal is the Armorer's last line.

Five expeditions. One purpose. The warrior those expeditions were for — already armed,
already waiting. The player doesn't know this until right now.

The Armorer has three lines. The rhythm breaks here — there is no "next quest," no departure.
The job is done.

    "Five pieces. Everything where it belongs."
    "You've been good company."
    "I'll leave you two to it."

The second line is the first moment in the show addressed explicitly to the player. The
third line acknowledges both parties — the Vindicator and the player — and is the show's
only moment of warm, explicit complicity. The Armorer steps back.

After the Armorer's last line: **hold** (~6 seconds, `hold_before_choice_ticks` in show-params).
The Vindicator is still behind a closed door — present, contained. Then `PLAYER_CHOICE`
fires: *"What now?"*

**Fight branch:** The iron door opens. The player steps through. The Vindicator is standing
there — frozen, AI-locked, already fully equipped. Then the countdown fires: 5, 4, 3, 2, 1,
GO (TITLE events). At GO: his AI releases. Boss health bossbar appears ("The Vindicator",
RED, PROGRESS). The fight is the show's final beat. Departments do not punctuate it.

**Walk away branch:** The iron door opens. The Vindicator speaks once — "LATER." (deep red
`#CC2200`, CHAT, ALL CAPS). Then despawns (~3 seconds). The show closes with one Sprite
line — "The stage is yours." (locked 2026-03-31).

All combat parameters (health multiplier, countdown timing, bossbar color, etc.) are in
`show-params.md §Battle Sequence`. Stage Management ratifies tick sequencing at intake.
