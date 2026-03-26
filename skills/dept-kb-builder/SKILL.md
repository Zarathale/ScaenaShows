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

1. **`kb/production-team.md §[department section]`** — The authoritative definition of the
   department: its domain, "the question this role asks," its authority, and its relationship
   to other departments. This is required reading before writing any KB.

   **Important:** `production-team.md` now carries both the creative/directorial layer AND
   substantial operational content for each department — mob registers, equipment tables, YAML
   event vocabulary, sound ID libraries, etc. The department KB is the *extension* layer, not
   a parallel document. Read the production-team.md section first to know what already exists
   before adding anything to the KB. Do not duplicate content that lives in production-team.md.

   Note whether the department has a **named head** — if so, the name is introduced briefly in
   the KB's opening section to give the role a voice and working style. The name is personality,
   not authority. All ownership and authority language uses the role title, not the name.

2. **`kb/system/spec.md §4`** — The YAML event types and fields the plugin accepts. This is the
   input surface, not the full capability picture, but it tells you what events exist.

3. **The existing KB file** (if you're updating one) — Know what's already there. Don't
   duplicate, but do verify that existing content is still accurate. Also check whether the
   content properly extends production-team.md or unintentionally duplicates it.

4. **Relevant Java source** — For any capability you're marking as verified, trace it to
   the code. The executor classes in `src/main/java/com/scaena/shows/` are the ultimate
   source of truth. If you can't open the file, say so rather than guessing.

5. **`kb/departments/approved-sources.md`** — Before fetching any external documentation
   (Minecraft Wiki, PaperMC Javadoc), read this file. Only approved sources may be used
   for research.

---

## Division of labor: production-team.md vs. department KBs

These two documents do different jobs. Keeping them clean requires understanding the boundary.

**`kb/production-team.md`** is the common brain. It defines:
- Creative domain, authority, and "the question this role asks"
- Directorial vocabulary and tone language
- Key operational patterns for each department (event tables, mob registers, sound IDs, etc.)
- Shared knowledge the whole team needs: spatial hierarchy, camera modes, puppet vs. performer, etc.

**Department KB** (`kb/departments/[dept].kb.md`) is the extension layer. It adds:
- Capability status table (Verified / Gapped / Aspirational) — this does NOT live in production-team.md
- Deeper YAML examples not already covered in production-team.md
- Instrument naming and the department's own vocabulary for its tools
- Edge cases, behavioral notes, and known quirks traced to Java source
- Tone Translation methodology (how this department interprets the Director's tone language)
- Brief introduction of a named head (if the department has one) — working style and disposition only; ownership stays with the role title
- Cross-references to adjacent departments and their KBs

**Rule of thumb:** If it answers "what tools does this department have and how exactly do they work?" → KB. If it answers "what is this department for and what does it do for the story?" → production-team.md. When both documents cover the same tool, production-team.md has the conceptual framing; the KB has the capability status and implementation detail.

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

If this department has a named head, open the KB with a brief section introducing them.
The name gives the role a voice and a working style — nothing more. All ownership and
authority language that follows uses the role title, not the name.

```markdown
## [Name]

[2–3 sentences on their disposition and working style. What is their primary orientation —
what do they care most about getting right? How do they approach their work?]

[One sentence on their escalation posture: what the [Role] resolves independently vs. what
gets brought to the team — framed by role, not by name.]
```

Named heads are not personas in the theatrical sense — they're a way of making a role's
working style legible. The KB may speak in their voice, but authority always belongs to
the position.

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

The Stage Manager KB has broader scope than other department KBs because the **Stage Manager**
owns structural elements that cut across the production. In addition to the standard structure
above, the SM KB must include:

- **The Prompt Book** — ownership of the show YAML's running order, section structure,
  REST placement, and YAML comment labeling (the C1/C2/C3 convention)
- **Cue Naming and Numbering Conventions** — the `[category].[archetype].[variant]`
  library standard, show-internal C1/C2/C3 numbering, naming authority and taxonomy
  enforcement; departments propose names, SM approves
- **Beat Collision Detection and Resolution** — the Stage Manager's protocol: identify →
  assess → resolve if within authority (offset, reorder, consolidate) → escalate with
  analysis and options when not. The escalation format: what's happening, why it can't both
  resolve, options with effects, the Stage Manager's read
- **Revision Continuity** — how the Stage Manager ensures the timeline holds together
  between revision cycles; the revision diff read
- **Run Sheet Ownership** — the Stage Manager writes it, maintains it, updates after every
  revision; the run sheet format and what the Watch question is for
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

## Path conventions — never use docs/

All KB files live under `kb/`. All cross-references between KB files must use `kb/` paths.
**Never write `docs/departments/` or `docs/production-team.md` in a KB file.** The `docs/`
tree is for legacy run sheets and deferred UI artifacts. The KB system lives entirely under `kb/`.

Correct paths:
- `kb/production-team.md` — not `docs/production-team.md`
- `kb/departments/[dept].kb.md` — not `docs/departments/[dept].kb.md`
- `kb/departments/wardrobe/` — not `docs/departments/wardrobe/`
- `kb/system/spec.md` — not `docs/spec.md`
- `kb/departments/voice/showsprite.context.md` — not `docs/showsprite.context.md`

When editing any KB file, scan for `docs/` references and correct them before committing.

---

## After building or updating a KB

- If any instrument status changed, note it in the commit message
- If a new gap was identified during research, update `ops-inbox.md`
- If a new instrument emerged that doesn't fit existing cue library categories, flag it to
  the Show Director as a candidate for a new cue family
- If a named department head was introduced or updated:
  1. Update `kb/production-team.md` — the department table (the authoritative index)
  2. Update the department KB frontmatter — set `owner: [Name]`
  3. Update the department's `_template/departments/[dept].md` — set `owner: [Name]` in frontmatter
  All three must stay in sync. `kb/production-team.md` is the authoritative index.

---

## Reconciliation with production-team.md — required at every KB session

**This step is mandatory.** Every initial KB build and every strengthening session ends with
a reconciliation pass against `kb/production-team.md §[department]`. The KB and the production
team document are the two halves of a department's identity — they must stay aligned.

### What to check

**1. Does production-team.md reflect current capability reality?**

The production-team.md section was written at a point in time. If the KB work uncovered
new verified capabilities, confirmed gaps, or changed the status of something, the
production-team.md section may be outdated. Update it — specifically:

- Any capability table or vocabulary section that lists something as available that is
  actually gapped (or vice versa)
- The Knowledgebase line at the end of the department's section — confirm the KB path is
  current and the description still accurately describes what the KB contains
- Any named characters introduced in the KB who aren't yet mentioned in production-team.md

**2. Is the right content in the right place?**

Check the division of labor. Content that now exists in both documents should be resolved:

- If production-team.md has deep operational detail (full YAML, full parameter tables)
  that the KB also has, decide which is the authoritative home. Usually production-team.md
  holds the conceptual framing; the KB holds capability status + edge cases. Remove
  duplication — don't maintain two copies of the same table.
- If the KB has creative/directorial language that belongs in production-team.md ("what
  this department is for, what question it asks"), move it or add a reference.

**3. Does production-team.md's description of the department still match how it actually works?**

Read the domain statement, "the question this role asks," and the authority line for the
department in production-team.md. Ask:

- Has the department's actual scope expanded or contracted since this was written?
- Has a new named head been established who isn't in the table yet?
- Are there cross-department authority notes (e.g., Camera / Lighting joint ownership of
  CAMERA effects) that are no longer accurate?

**4. Are all cross-references bidirectional and consistent?**

If the KB references another department's KB, check whether that department's KB or
production-team.md section references back correctly. Also confirm:

- The Knowledgebase entry in production-team.md for this department matches the KB's
  actual file path and description
- Any forward references in production-team.md to a specific KB section (e.g., "see
  sound.kb.md §Sound Department Personnel") resolve to a section that actually exists

### How to handle found discrepancies

Minor language drift (wording is slightly off, description is outdated) — update
production-team.md in the same session. Don't leave it for later.

Content in the wrong place — surface it to Alan before moving. Say what you found,
which document it should live in, and why. Alan signs off; then move it.

Genuine capability disagreements (production-team.md says something works that the KB
found is gapped) — update both documents to reflect reality, and file the gap in
`ops-inbox.md` if it isn't already there.
