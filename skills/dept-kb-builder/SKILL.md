---
name: dept-kb-builder
description: >
  Use this skill when building, updating, auditing, or restructuring a ScaenaShows department
  knowledge base file (the *.kb.md files in kb/departments/). This includes writing a KB from
  scratch for a department that doesn't have one, expanding or reorganizing an existing KB,
  verifying a department's capabilities against the Java source, auditing the sound palette,
  registering entities for casting, or aligning any KB with what the plugin actually does
  right now. Also triggers when Alan asks what a department "owns" or "can do", when a new Java
  capability needs to be documented, or when a KB needs to be structured before a new show enters
  pre-production. Use proactively any time the user mentions a specific department's capabilities,
  instruments, or tools in the context of show authoring. Also use when naming a department head,
  establishing a department's tone translation methodology, or building out the Stage Manager's
  prompt book and beat collision sections.
---

# ScaenaShows — Department KB Builder

Department knowledge base files are what each department brings to the virtual production table.
When Claude authors a show, reading the relevant KB is how it learns what tools a department
can actually play — and equally importantly, which ones are still in the shop.

A KB isn't documentation for its own sake. It's a department head briefing their team: here's
what we have, here's what we know how to do with it, here's what we can't do yet. The goal is
a document that another person (or Claude in a future session) can read and immediately know
what this department is capable of — and trust that it's accurate.

---

## Before you begin

Read these before writing or updating any KB:

1. **`kb/departments/README.md`** — The master department index. Know which department you're
   building for, whether it has a named head, and how the top-level KB relates to the per-show
   template file. This is the entry point for the entire KB system.

2. **`kb/production-team.md §[department section]`** — Creative direction and context for the
   department. The KB is a technical companion to this; know the creative role first. Note whether
   the department has a **named head** (e.g., Kendra for Stage Manager) — named heads get
   introduced in the KB's opening section and their perspective should inform the KB's voice.

3. **`docs/spec.md §4`** — The YAML event types and fields the plugin accepts. This is the
   input surface, not the full capability picture, but it tells you what events exist.

4. **The existing KB file** (if you're updating one) — Know what's already there. Don't
   duplicate, but do verify that existing content is still accurate.

5. **Relevant Java source** — For any capability you're marking as verified, trace it to
   the code. The executor classes in `src/main/java/com/scaena/shows/` are the ultimate
   source of truth. If you can't open the file, say so rather than guessing.

6. **`kb/departments/approved-sources.md`** — Before fetching any external documentation
   (Minecraft Wiki, PaperMC Javadoc), read this file. Only approved sources may be used
   for research.

---

## The Show Director's KB is different in kind

The Show Director's KB (`kb/departments/show-director.kb.md`) does not document Java
capabilities or YAML syntax — those belong to the other departments. It documents the
**process of leading a show**: how to write a brief, how to brief departments, how to
evaluate the arc, the tone language and feedback style, standing department asks, and
revision cycle guidance.

When building or updating the Show Director's KB, the instrument inventory structure does
not apply. Instead, focus on: brief templates, Show Direction format, the `direction/`
folder structure, standing department asks (intake + revision accountability), arc
evaluation questions, cross-department conflict protocol, and pre-flight checklist.

The Show Director's KB also establishes **tone language discipline**: how the Director
communicates tone through experiential language ("tender and a little strange") rather
than mechanics ("raise the view window 10 degrees"). Each department's KB should have a
corresponding **tone translation section** — how that department interprets tone phrases
into their instruments. These two halves work together: the Director speaks tone;
departments translate.

---

## The structure of a department KB

Every department KB (except Show Director — see above) is organized around these sections.

### 1. Named Head Introduction (if applicable)

If this department has a named head, open the KB with a section introducing them:

```markdown
## [Name]

**[Name] is the [Role].** [2–3 sentences on their disposition, working style, and authority.
What is their primary orientation — what do they care most about getting right?]

[Their escalation posture: what they resolve on their own vs. what they bring to the team.]
```

Named heads are not personas in the theatrical sense — they're a way of making a department's
working style legible. The KB speaks in their voice: expert, opinionated, direct.

### 2. Role Summary

Three to five bullet points summarizing what this department owns. For most departments this is
a set of YAML event types. For Stage Manager it includes structural ownership (prompt book,
cue naming, beat collision, cleanup contract).

### 3. The Instrument Inventory

Each department has a set of **instruments** — the specific capabilities it owns and knows
how to shape creatively. An instrument is a named, defined capability: one Java event type,
or a combination of event types working together, that the department understands and controls.

The word "instrument" is intentional. A saxophone player doesn't just know that the saxophone
exists; they know what it can do at different dynamics, in different registers, in different
musical contexts. That's the level of knowledge a department KB captures for each of its tools.

For each instrument, document:

- **Name** — A short name the department uses for this capability ("Equipment at spawn",
  "The invisible body technique", "Ambient bed layering"). Named by the department, not
  just the Java event name.

- **Java grounding** — The event type(s) behind it. Be specific.

- **What it does** — What the audience experiences, not just what the plugin executes.

- **How to dial it** — Parameters, fields, ranges, combinations. Include representative
  YAML with comments. Show the spectrum from subtle to dramatic.

- **Strengths** — What this instrument does especially well. What moments call for it?

- **Limitations and gaps** — What it cannot do. Is this a Minecraft platform limit or a
  current plugin gap? Reference `ops-inbox.md` if a gap is already filed.

- **Storytelling contexts** — Concrete show moments where this instrument shines. Lead
  with energetic, beautiful, wondrous examples. Grief and dread are valid registers but
  shouldn't be the default illustration of a capability.

### 4. Tone Translation

Each department KB should include a **Tone Translation** section — how this department
interprets the Director's tone phrase into their instruments.

The Director communicates tone through experiential language: "tender and a little strange",
"overwhelming and earned", "quiet devastation". Each department is the specialist at
translating that phrase into their domain. Sound decides what "tender" sounds like. Camera
decides what "a little strange" looks like in a viewpoint choice.

This section documents:
- How the department approaches a tone phrase (their default interpretive instinct)
- Examples: "when the Director says 'tender', we..." / "when the Director says 'strange', we..."
- How they signal back to the Director when the tone phrase needs elaboration
- What the department can and can't do for any given emotional register

This section does NOT receive mechanical directions from the Director. The Director gives
experiential feedback ("this feels clinical, not tender") and the department owns the
translation back into YAML.

### 5. Department Principles

How this department approaches show design — its working rules, creative philosophy, and
relationship to other departments.

Include:
- What this department is ultimately *for* in a production (one sentence)
- How it coordinates with adjacent departments — where authority overlaps
- What decisions require the Show Director's sign-off
- How the department handles capability gaps: design around them, flag to Stage Management?
- **Escalation discipline**: departments resolve problems they have the authority and
  knowledge to resolve. They escalate when a resolution requires a creative call above
  their scope or would require another department to change what their work was intended
  to do. They don't bring problems — they bring resolved problems, or problems with
  proposed resolutions attached.

### 6. Capability Status Summary

A quick-reference table listing every instrument and its current status.

```
| Instrument | Status | Notes |
|------------|--------|-------|
| Equipment at spawn | ✅ Verified | All 6 slots, any LivingEntity |
| Mob variant / profession | ⚠️ Gapped | Parsed but not applied (ops-inbox.md) |
| Parrot perch on shoulder | 📋 Aspirational | Not yet implemented or filed |
```

Status codes:

- **✅ Verified** — Confirmed working in the current Java build. Traced to running code
  or confirmed in-game.
- **⚠️ Gapped** — Partially implemented, parsed-but-ignored, or known to fail at runtime.
  Must be filed in `ops-inbox.md` or reference an existing filing.
- **📋 Aspirational** — Not yet implemented, not yet filed. Proposed capability only.

**This table is a commitment.** If uncertain, mark ⚠️ with a note — that's more useful
than false confidence.

---

## Stage Manager KB — special considerations

The Stage Manager KB has broader scope than other department KBs because **Kendra** owns
structural elements that cut across the production. In addition to the standard structure
above, the SM KB must include:

- **The Prompt Book** — ownership of the show YAML's running order, section structure,
  REST placement, and YAML comment labeling (the C1/C2/C3 convention)
- **Cue Naming and Numbering Conventions** — the `[category].[archetype].[variant]`
  library standard, show-internal C1/C2/C3 numbering, naming authority and taxonomy
  enforcement; departments propose names, SM approves
- **Beat Collision Detection and Resolution** — Kendra's protocol: identify → assess →
  resolve if she can (offset, reorder, consolidate) → escalate with analysis and options
  when she cannot. The escalation format: what's happening, why it can't both resolve,
  options with effects, Kendra's read
- **Revision Continuity** — how Kendra ensures the timeline holds together between
  revision cycles; the revision diff read
- **Run Sheet Ownership** — Kendra writes it, maintains it, updates after every revision;
  the run sheet format and what the Watch question is for
- **Pre-Show Tech Check** — "is the stage set?" check before each in-game test, distinct
  from the rehearsal safety checklist

---

## Accuracy and verification

Marking a capability ✅ Verified requires at least one of:

1. You've read the relevant Java executor class and confirmed the field is applied at runtime
2. The capability is used in an existing show YAML that Alan has confirmed works in-game
3. It's explicitly documented as working in `docs/spec.md`

If your only source is an external reference without tracing it to Java code, the capability
is at best ⚠️ Gapped or 📋 Aspirational. External sources tell you what the Minecraft API
*offers* — not whether the ScaenaShows plugin actually calls it.

When uncertain, show your reasoning. "I confirmed the Bukkit method exists via the PaperMC
Javadoc, but I haven't verified whether the YAML executor calls it" is legitimate and useful.

Note that Stage Management owns the canonical gap registry. If you identify a gap during KB
work that isn't already in `ops-inbox.md`, flag it — the Stage Manager should file it.

---

## KB file conventions

- Location: `kb/departments/[department-slug].kb.md`
- YAML frontmatter: `department:`, `owner:` (named head or role title), `kb_version:`, `updated:`
- Increment `kb_version:` for significant changes (structural, new instruments, status changes)
- For departments with extensive instrument inventories, a subfolder (`kb/departments/[slug]/`)
  is appropriate — see Wardrobe's subfolder structure as a model
- Always cite `ops-inbox.md` when referencing a filed gap

---

## After building or updating a KB

- If any instrument status changed, note it in the commit message
- If a new gap was identified during research, update `ops-inbox.md`
- If a new instrument emerged that doesn't fit existing cue library categories, flag it to
  the Show Director as a candidate for a new cue family
- If a named department head was introduced or updated:
  1. Update `kb/departments/README.md` — the department roster table
  2. Update `kb/production-team.md` — the department table
  3. Update the department's `_template/departments/[dept].md` — set `owner: [Name]` in frontmatter
  All three must stay in sync. `kb/departments/README.md` is the authoritative index.
