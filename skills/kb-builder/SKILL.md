---
name: kb-builder
description: >
  Use this skill for any work involving the ScaenaShows knowledge base system — department
  KBs, production-team.md, system docs, or the kb/ tree as a whole. Three modes:
  (1) Build or update a single department's KB — writing from scratch, expanding, restructuring,
  verifying capabilities, adding instruments, tone translation, or calibration-readiness work;
  (2) Audit the full KB system — assess all departments for completeness, redundancy,
  calibration-readiness, and cross-document drift;
  (3) Quick summary — on-demand snapshot of KB health and prioritized recommendations.
  Triggers include: "build the [dept] KB", "audit the KB", "how are the departments doing",
  "are we ready for Phase 10", "what should I work on next in the KB", "check for redundancy",
  "what's the KB status", "add an instrument to [dept]", "update the [dept] KB", naming a
  department's capabilities or gaps in the context of show authoring, and any time a new Java
  capability needs to be documented or a new department head named.
---

# ScaenaShows — KB Builder

Department knowledge base files are what each department brings to the virtual production
table. When Claude authors a show, reading the relevant KB is how it learns what tools a
department can actually play — and equally importantly, which ones are still in the shop.

A KB isn't documentation for its own sake. It's a department head briefing their team:
here's what we have, here's what we know how to do with it, here's what we can't do yet.
The goal is a document that another person (or Claude in a future session) can read and
immediately know what this department is capable of — and trust that it's accurate.

---

## Mode Selection

Determine the right mode from the request before doing anything:

| Request type | Mode |
|---|---|
| Build, expand, restructure, or verify a single dept's KB | **Mode 1: Build / Update** |
| "audit the KB", "check for redundancy", "are we calibration-ready" | **Mode 2: Full KB Audit** |
| "KB status", "what should I work on next", "quick summary" | **Mode 3: Quick Summary** |

When in doubt between Audit and Quick Summary: if Alan needs to make a decision (what
to work on, whether the team is ready for Phase 10), Quick Summary is sufficient. If
Alan needs to understand the quality and accuracy of the KB content itself, use Audit.

---

## Mode 1: Build / Update (Single Department)

### Before you begin

Read these before writing or updating any KB:

1. **`kb/production-team.md §[department section]`** — The authoritative definition of
   the department: its domain, "the question this role asks," its authority, and its
   relationship to other departments. Required reading before writing any KB.

   `production-team.md` carries both the creative/directorial layer AND substantial
   operational content — mob registers, equipment tables, YAML event vocabulary, sound
   ID libraries, etc. The department KB is the *extension* layer, not a parallel document.
   Read the production-team.md section first to know what already exists before adding
   anything to the KB. Do not duplicate content that lives in production-team.md.

2. **`kb/system/spec.md §4`** — The YAML event types and fields the plugin accepts.
   This is the input surface, not the full capability picture, but it tells you what
   events exist.

3. **The existing KB file** (if updating) — Know what's there. Don't duplicate; do verify
   that existing content is still accurate. Check whether content properly extends
   production-team.md or unintentionally duplicates it.

4. **Relevant Java source** — For any capability marked ✅ Verified, trace it to the code.
   The executor classes in `src/main/java/com/scaena/shows/` are the ultimate source of
   truth. If you can't open the file, say so rather than guessing.

5. **`kb/departments/approved-sources.md`** — Before fetching any external documentation
   (Minecraft Wiki, PaperMC Javadoc), read this file. Only approved sources may be used.

---

### Division of labor: production-team.md vs. department KBs

**`kb/production-team.md`** is the common brain. It defines:
- Creative domain, authority, and "the question this role asks"
- Directorial vocabulary and tone language
- Key operational patterns (event tables, mob registers, sound IDs, etc.)
- Shared knowledge the whole team needs

**Department KB** is the extension layer. It adds:
- Capability status table (Verified / Gapped / Aspirational)
- Deeper YAML examples not already covered in production-team.md
- Instrument naming and department vocabulary
- Edge cases, behavioral notes, and known quirks traced to Java source
- Tone Translation methodology
- Named head introduction (if applicable)
- Cross-references to adjacent departments

**Rule of thumb:** "What tools does this department have and how exactly do they work?"
→ KB. "What is this department for and what does it do for the story?" → production-team.md.

---

### The Show Director's KB is different in kind

The Show Director's KB does not document Java capabilities or YAML syntax. It documents
the **process of leading a show**: how to write a brief, how to brief departments, how to
evaluate the arc, tone language and feedback style, standing department asks, and revision
cycle guidance.

For the Show Director's KB, the instrument inventory structure does not apply. Focus on:
brief templates, Show Direction format, the `direction/` folder structure, standing
department asks, arc evaluation questions, cross-department conflict protocol, and
pre-flight checklist.

The Show Director establishes **tone language discipline**: experiential language
("tender and a little strange") rather than mechanics. Each department's KB has a
corresponding Tone Translation section — how that department interprets tone phrases
into their instruments.

---

### Stage Management & Production — scope note

The Stage Management department (Kendra) carries both Stage Management and Production
Manager functions: show execution, prompt book, beat collision, run sheets, AND
pre-production process coordination, department briefing logistics, and YAML authorship.
The KB reflects this expanded scope.

**KB home:** `kb/departments/stage-management/stage-management.kb.md`
**Show creation process:** `kb/departments/stage-management/show-creation-process.md`

When building or updating the Stage Management KB, document both layers:
- Stage Manager proper: prompt book, cue naming, beat collision, revision continuity,
  run sheet ownership, pre-show tech check
- Production Manager layer: show creation workflow, department briefing coordination,
  pre-production sequencing, YAML authorship ownership

---

### KB folder structure — mandatory

Every department has a folder at `kb/departments/[dept-slug]/`. The main KB file lives
inside it as `[dept-slug].kb.md`. Supplementary reference files join the folder as needed.

**When doing a KB uplift session:** create the dept folder if it doesn't exist, move
the flat `.kb.md` file into it, and do all KB work inside the folder. Migration happens
dept-by-dept during each uplift session — not in a bulk pass.

**Established folder structure:**
```
kb/departments/
├── show-director/
├── stage-management/          ← includes show-creation-process.md
├── casting/
├── wardrobe/                  ← wardrobe.kb.md + 4 reference files
├── choreography/
├── set/
├── effects/
├── camera/
├── lighting/
├── sound/                     ← sound.kb.md + music-director.md
├── fireworks/
└── voice/                     ← voice.kb.md + showsprite.context.md
```

`approved-sources.md` stays at `kb/departments/` root — it applies across all departments.

---

### Calibration-ready definition

A department KB meets the calibration-ready threshold for Phase 10 when all of the
following are true:

- [ ] Full instrument inventory documented (all known instruments — not just the easy ones)
- [ ] Every instrument has a capability status: ✅ Verified / ⚠️ Gapped / 📋 Aspirational
- [ ] Tone Translation section written
- [ ] At least one YAML example per instrument
- [ ] All known gaps filed in `ops-inbox.md`
- [ ] KB has been read and used in at least one show session

**Phase 9 uplift priority order:**

| Priority | Department | Primary gap |
|---|---|---|
| 1 | Effects | Thinnest KB; controls player body/physics — highest show impact |
| 2 | Set | Spatial design methodology underdeveloped |
| 3 | Lighting | Only 3 instruments documented |
| 4 | Wardrobe | Thin on creative/contextual examples |
| — | All others | Review against calibration-ready checklist; likely minor uplift passes |

---

### Structure of a department KB

Every department KB (except Show Director — see above) is organized around these sections.

#### 1. Named Head Introduction (if applicable)

If the department has a named head, open the KB with a brief section introducing them.
The name gives the role a voice and a working style — nothing more. All ownership and
authority language uses the role title, not the name.

```markdown
## [Name]

[2–3 sentences on disposition and working style. What do they care most about getting right?
How do they approach their work?]

[One sentence on escalation posture: what the [Role] resolves independently vs. what gets
brought to the team — framed by role, not by name.]
```

#### 2. Role Summary

Three to five bullet points summarizing what this department owns.

#### 3. The Instrument Inventory

Each department has a set of **instruments** — named, defined capabilities it owns and
knows how to shape creatively. For each instrument, document:

- **Name** — Short name the department uses ("Equipment at spawn", "Ambient bed layering")
- **Java grounding** — The event type(s) behind it
- **What it does** — What the audience *experiences*, not just what the plugin executes
- **How to dial it** — Parameters, fields, ranges, combinations; representative YAML with
  comments; show the spectrum from subtle to dramatic
- **Strengths** — What this instrument does especially well; what moments call for it
- **Limitations and gaps** — What it cannot do; platform limit or plugin gap?
  Reference `ops-inbox.md` if filed
- **Storytelling contexts** — Concrete show moments where this instrument shines. Lead
  with energetic, beautiful, wondrous examples

#### 4. Tone Translation

How this department interprets the Director's tone phrase into their instruments.
Documents: the department's default interpretive instinct, worked examples ("when the
Director says 'tender', we..."), how to signal back when a tone phrase needs elaboration,
and what the department can and can't do for given emotional registers.

The Director gives experiential feedback ("this feels clinical, not tender"). The
department owns the translation back into YAML.

#### 5. Department Principles

Working rules, creative philosophy, and relationship to other departments:
- What this department is ultimately *for* in a production
- How it coordinates with adjacent departments
- What decisions require Show Director sign-off
- How the department handles capability gaps
- **Escalation discipline:** departments bring resolved problems, or problems with
  proposed resolutions attached — not raw problems

#### 6. Capability Status Summary

Quick-reference table listing every instrument and current status:

```
| Instrument | Status | Notes |
|---|---|---|
| Equipment at spawn | ✅ Verified | All 6 slots, any LivingEntity |
| Mob variant / profession | ⚠️ Gapped | Parsed but not applied (ops-inbox.md) |
| Parrot perch on shoulder | 📋 Aspirational | Not yet implemented |
```

Status codes:
- **✅ Verified** — Confirmed working in the current Java build
- **⚠️ Gapped** — Partially implemented, parsed-but-ignored, or known to fail. Must be
  filed in `ops-inbox.md`
- **📋 Aspirational** — Not yet implemented, not yet filed. Proposed only.

**This table is a commitment.** Mark ⚠️ with a note rather than false confidence.

---

### Accuracy and verification

Marking a capability ✅ Verified requires at least one of:
1. You've read the relevant Java executor class and confirmed the field is applied
2. The capability is used in an existing show YAML Alan has confirmed works in-game
3. Explicitly documented as working in `kb/system/spec.md`

If your only source is an external reference not traced to Java code, the capability is
at best ⚠️ Gapped. External sources tell you what the Minecraft API *offers* — not whether
the ScaenaShows plugin actually calls it.

When uncertain, show your reasoning. "I confirmed the Bukkit method exists via the PaperMC
Javadoc, but I haven't verified whether the YAML executor calls it" is legitimate.

---

### After building or updating a KB

- If any instrument status changed, note it in the commit message
- If a new gap was identified, update `ops-inbox.md`
- If a new instrument emerged that doesn't fit existing cue library categories, flag it
  to the Show Director as a candidate for a new cue family
- If a named department head was introduced or updated:
  1. Update `kb/production-team.md` — the department table (authoritative index)
  2. Update the department KB frontmatter — set `owner: [Name]`
  3. Update the show template at `src/main/resources/shows/_template/departments/[dept].md`
  All three must stay in sync.

---

### Reconciliation with production-team.md — required at every KB session

**This step is mandatory.** Every KB build and update session ends with a reconciliation
pass against `kb/production-team.md §[department]`.

**What to check:**

1. Does production-team.md reflect current capability reality? Update if the KB work
   uncovered new verified capabilities, confirmed gaps, or changed status.

2. Is the right content in the right place? Resolve duplication — production-team.md
   holds conceptual framing; the KB holds capability status + edge cases.

3. Does production-team.md's description still match how the department actually works?
   Check domain statement, "the question this role asks," and authority line.

4. Are cross-references bidirectional and consistent? Confirm the Knowledgebase entry
   in production-team.md matches the KB's actual file path.

**How to handle discrepancies:** minor drift — update in this session. Content in wrong
place — surface to Alan before moving. Genuine capability disagreements — update both
documents and file the gap in `ops-inbox.md`.

---

## Mode 2: Full KB Audit

Invoked when the goal is to assess the quality, completeness, and health of the entire
`kb/` system — not to build a specific department's content.

### What to read

Read in this order:
1. `kb/production-team.md` — establishes the authoritative dept list and what each
   department claims to own
2. Each `kb/departments/[dept]/[dept].kb.md` (or flat `.kb.md` for depts not yet migrated)
3. Root KB files: `kb/system/spec.md` (skim for any forward references to dept KBs),
   `kb/departments/approved-sources.md`

### Per-department assessment

For each department, score against the calibration-ready checklist:

```
| Check | Status | Notes |
|---|---|---|
| Instrument inventory complete | ✅ / ⚠️ / ❌ | |
| Every instrument has status tag | ✅ / ⚠️ / ❌ | |
| Tone Translation section present | ✅ / ⚠️ / ❌ | |
| At least one YAML example per instrument | ✅ / ⚠️ / ❌ | |
| All known gaps in ops-inbox.md | ✅ / ⚠️ / ❌ | |
| Used in at least one show session | ✅ / ⚠️ / ❌ | |
| In folder structure (not flat file) | ✅ / ⚠️ | |
```

### Cross-document checks

After per-dept assessment, check the system as a whole:

- **production-team.md drift** — does production-team.md describe departments accurately,
  or has the KB work moved ahead of it?
- **Duplication** — is the same content (capability tables, YAML examples, sound IDs)
  maintained in both production-team.md and a dept KB? Flag which is authoritative.
- **Broken references** — any KB file referencing a path that doesn't exist?
- **Orphaned files** — any files in `kb/` that aren't referenced anywhere?
- **ops-inbox.md coverage** — do all ⚠️ Gapped items across all KBs have ops-inbox
  entries? Surface any that are missing.

### Audit output format

```markdown
## KB Audit — [Date]

### Calibration-Readiness Summary

| Department | Ready | Blockers |
|---|---|---|
| Effects | ❌ No | [list what's missing] |
| Set | ⚠️ Partial | [list what's missing] |
| Choreography | ✅ Yes | — |
...

**Ready for Phase 10:** [Yes / Not yet — N of 12 depts calibration-ready]

### Cross-Document Issues

[List each issue: what's wrong, which files are involved, recommended fix]

### Recommended Next Actions

1. [Highest priority — most blocking]
2.
3.
```

---

## Mode 3: Quick Summary

Invoked when Alan wants a fast read on KB health without a full audit. Useful at the
start of a session to orient, or when deciding what to work on next.

### What to read

Read `kb/production-team.md` for the dept list, then skim each dept KB — enough to
assess calibration-readiness status and identify the most obvious gaps. No need to
trace every capability to Java source or check cross-references deeply.

### Quick Summary output format

```markdown
## KB Quick Summary — [Date]

**Phase 9 Track A progress:** N/12 departments calibration-ready

| Department | Status | Primary gap |
|---|---|---|
| Effects | ❌ Not ready | [one-line gap description] |
| Set | ⚠️ Partial | [one-line gap description] |
| Choreography | ✅ Ready | — |
...

**Recommended next 1–2 sessions:**
1. [Most impactful uplift session]
2. [Second most impactful]

**Phase 10 gate:** [What's blocking the team from entering the Showcase Series?]
```

Keep the summary scannable. One line per dept. The recommendations are the most
important output — make them specific and actionable.

---

## Shared: File and path conventions

- **Folder location:** `kb/departments/[dept-slug]/[dept-slug].kb.md`
- **YAML frontmatter:** `department:`, `owner:` (named head or role title), `kb_version:`,
  `updated:`
- Increment `kb_version:` for significant changes (structural, new instruments, status changes)
- `approved-sources.md` stays at `kb/departments/` root

**Never use `docs/` paths.** All KB files live under `kb/`. All cross-references
between KB files must use `kb/` paths. The `docs/` tree is for legacy run sheets and
deferred UI artifacts.

Correct paths:
- `kb/production-team.md`
- `kb/departments/[dept]/[dept].kb.md`
- `kb/system/spec.md`
- `kb/departments/voice/showsprite.context.md`

When editing any KB file, scan for `docs/` references and correct them before committing.
