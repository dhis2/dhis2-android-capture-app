---
name: test-flow-planner
description: >
  Group Zephyr cases into automation flows, identify shared setup, propose
  Robots and fixtures, and surface gaps in the source cases. Produces a
  flow-plan.md artifact the user reviews before any test code is written.
  Plan-mode only — never generates Kotlin.
---

# Test Flow Planner

The plan-mode brain. Takes normalized Zephyr cases plus existing-test context
and produces a reviewable flow plan. Hands off to the `android-testing` skill
only after the user approves.

## Inputs

- Normalized Zephyr cases (from `zephyr-test-fetcher`).
- Existing Robots and tests in the capture-app repo. Grep paths:
  - `app/src/androidTest/java/org/dhis2/usescases/<feature>/robot/`
  - `app/src/androidTest/java/org/dhis2/common/`
  - `**/commonTest/` for integration-test precedents.
- Workflow examples (from `confluence-workflow-reader`).
- Test-tag conventions from the design system (see `android-testing` skill and
  the API docs link it references).
- **Current claim state of test programs on the testing server**
  (see "Test-program reservation" below).

## Test-program reservation (mandatory planning step)

Every flow plan **must** name the DHIS2 program (or tracker program /
dataset) on the testing server that the flow owns. Default is one
dedicated program per flow. The exception — and the only time sharing
is allowed — is when the new cases are an extension of an *existing*
flow's workflow; in that case the new cases ride along on the existing
flow's already-claimed program.

Before claiming anything fresh, **check whether the new cases fit an
existing flow** (see "Check existing flows first" below). Only claim
a new program when the cases don't fit any existing flow.

Server: **the user supplies the base URL at the start of every planning
session** (the testing-instance slug rotates per cycle). The agent must
ask for it before running any query below. Expected shape:
`https://android.im.dhis2.org/<slug>/`. Default credentials:
`system` / `System123` unless the user specifies otherwise. In every
example below replace `<BASE_URL>` with whatever the user provided.

### Check existing flows first (run before any new-program claim)

Before deciding to create a new flow, read the existing flow plans
published under the Automated Testing Confluence folder (parent
`644644869`) and grep `app/src/androidTest` for already-claimed
programs. For each new Zephyr case, decide whether it fits an existing
flow using the following tests:

- **Same screen** as the existing flow (e.g., both exercise the
  data-entry form, or both exercise the program-event list)?
- **Same setup** (program, intent helpers, mock fixtures) ≥ 70%
  overlap with the existing flow?
- **Same Robot** would be extended with one or two new methods rather
  than a whole new Robot class?

If yes to all three, **propose extending the existing flow** in the
plan instead of creating a new one. The plan should say explicitly
"extending Flow X" and reuse Flow X's claimed program. If no, propose
a new flow and claim a fresh program for it (procedure below).

State the verdict for every case in the plan: either
"covered by Flow X" or "new Flow Y (reason: …)". No silent assumptions.

### Procedure during planning

0. **Ask the user for the base URL** if it has not already been provided
   in the conversation. Phrase it as one short prompt:
   > "Which DHIS2 testing instance should I plan against? Please provide
   > the full base URL, e.g. `https://android.im.dhis2.org/<your-slug>/`."

   Cache the value as `<BASE_URL>` for the rest of the conversation. If
   you ever realise you need a different instance later, ask again —
   never guess.
1. **List candidate programs of the right type.** Filter by `programType`
   and look for ones whose `description` is empty (or does not start with
   `"Reserved for Android Capture App automated tests"`). Example for event
   programs:
   ```bash
   curl -s -u system:System123 \
     "<BASE_URL>/api/programs.json?fields=id,name,description&filter=programType:eq:WITHOUT_REGISTRATION&paging=false"
   ```
2. **Cross-check with the codebase.** Grep `app/src/androidTest` for each
   candidate UID — drop any UID already referenced in tests *unless* the
   matching flow is one the new cases are intended to extend (see
   "Check existing flows first" above).
3. **Pick one program per flow.** Prefer programs that are already close
   to what the new flow needs (small for list/details flows, configurable
   for form flows). Default is one program per flow; share an
   already-claimed program only when extending that flow's workflow.
4. **Surface the proposed mapping in the plan** under a "Programs claimed"
   section (schema below). Get explicit user approval before mutating
   anything on the server.
5. **After approval, claim each program** by PATCHing its `description`:
   ```bash
   curl -s -u system:System123 -X PATCH \
     -H 'Content-Type: application/json-patch+json' \
     "<BASE_URL>/api/programs/<UID>" \
     -d '[{"op":"add","path":"/description","value":"Reserved for Android Capture App automated tests — Flow X (<Title>). Owned by ANDROAPP-<TICKET>. Do not modify form structure or seeded events without coordinating with the Android team."}]'
   ```
6. **Apply any config tweaks** the flow needs (mandatory DEs, formName
   diffs, validation/program rules, seeded events). Document each change
   in the plan so future agents understand why the program looks the way
   it does.

### Hard rules

- **Always ask the user which testing instance to target.** The slug
  changes every cycle; never reuse a base URL from a previous
  conversation or session. Re-confirm at the start of implementation
  mode before any mutation.
- **Check existing flows before designing a new one.** For every batch
  of new Zephyr cases, evaluate whether they extend an existing flow
  (same screen, same setup, same Robot — see the "Check existing flows
  first" subsection above). If they do, propose extending that flow and
  reuse its already-claimed program. State the decision per case in the
  plan ("covered by Flow X" vs "new Flow Y").
- **Default: one program per flow. Sharing allowed only when extending
  an existing flow.** Two unrelated flows must not share a program.
- **Never claim or modify** a program whose description already starts with
  `"Reserved for Android Capture App automated tests"` and references a
  *different* flow — that means another flow owns it. The only exception
  is when the user explicitly approves overriding the claim (e.g.
  because the previous flow is being deprecated).
- **Never modify** Antenatal Care (`lxAQ7Zs9VYR`) or Information Campaign
  (`q04UBOqq3rp`) — those are used by legacy tests and changing them
  breaks unrelated suites.
- **Always seed data via the tracker import API**, not by tapping through
  the app, so the seed is reproducible from CI.

## Heuristics for grouping cases into flows

- **Shared setup ⇒ same flow.** Cases that share three or more Given/setup
  steps fold into one flow.
- **Same screen, same Robot.** Each screen the flow touches gets one Robot.
  Reuse an existing Robot if 70%+ of its methods already cover the
  interactions needed.
- **Each Then is one verification *method*.** Every distinct "Then"
  assertion becomes one verification method on the appropriate Robot —
  not one whole `@Test`. See "One workflow `@Test` per flow" below.
- **Independence preferred over speed.** When in doubt, split flows rather
  than coupling unrelated cases.

## One workflow `@Test` per flow — the journey-with-checkpoints pattern

**Default to a single workflow `@Test` per flow that walks the user
through a realistic journey, with each Zephyr case appearing as an
inline checkpoint (assertion) at the relevant step.** This matches the
manual-test pattern documented on
[Events Tests - Event and Tracker Programs](https://dhis2.atlassian.net/wiki/spaces/MOB/pages/564297735/),
where the drawio diagrams show one workflow with ANDROAPP boxes attached
to specific steps. Do **not** default to one `@Test` per Zephyr case —
that duplicates setup, slows the suite, and breaks the user-journey
narrative.

### What this looks like in Kotlin

```kotlin
@Test
fun shouldExerciseFooWorkflow() {
    // Step 0 — setup: launch the activity / seed the fixture event
    prepareFooAndLaunch(rule)

    // Step 1 — assert the screen renders in its initial state
    fooRobot(composeTestRule) {
        // [ANDROAPP-1234] First checkpoint
        checkSomething()
        // [ANDROAPP-1456] Second checkpoint at the same step
        checkSomethingElse()
    }

    // Step 2 — user action that advances the journey
    fooRobot(composeTestRule) {
        clickNextButton()
    }

    // Step 3 — assert post-action state
    fooRobot(composeTestRule) {
        // [ANDROAPP-1457] Third checkpoint, after the action
        checkPostActionState()
    }
}
```

### Required pattern

- **One workflow per flow.** Each flow has exactly one workflow `@Test`
  unless there's a concrete reason for more (recorded in the plan's
  "Workflow shape" line, with the reason).
- **Each checkpoint is one Zephyr case.** Label each assertion inline as
  `// [ANDROAPP-####] short description` so a failure message still
  points at the right case.
- **Maximise checkpoints per workflow.** During planning, look at every
  Zephyr case for the flow and ask: *can this be a checkpoint inside the
  shared workflow, or does it genuinely need its own journey?* If the
  case fits anywhere along the journey, fold it in. Only spin off a
  second workflow `@Test` when the cases need a structurally different
  starting state (e.g., a completed event vs an active one, when the
  same activity rule can't relaunch).
- **Defer, don't `@Ignore`.** If a Zephyr case needs a fixture or a SDK
  probe that isn't ready yet, document it as a *deferred checkpoint* in
  a comment block at the end of the workflow — do not create an empty
  `@Test @Ignore` stub. The flow plan must explain what's deferred and
  why.

### When to split into more than one workflow

Split a flow into multiple workflow `@Test`s only when one of the
following hold:

- A `LazyActivityScenarioRule` cannot relaunch in a single test, and the
  cases need different launch intents that can't be sequenced via
  in-app navigation.
- The user journeys for two case groups genuinely diverge (e.g.
  "happy path" vs "error path" of the same screen) and forcing them
  into one workflow would obscure the assertion that fails.

Record the split decision in the plan under each flow's "Workflow shape"
line so reviewers can sanity-check the call.

## Output: flow-plan.md schema

The plan must be a single markdown artifact with these sections, in this
order:

```markdown
# Flow Plan: <short name>

## Summary
<3–5 line summary of what's being automated and why>

## Source cases
| Key | Title | Automation status | Coverage |
|-----|-------|-------------------|----------|
| ANDROAPP-1234 | ... | Not Automated | Covered by Flow A |
| ANDROAPP-1456 | ... | Pending | Covered by Flow A |

## Flows

### Flow A — `<TestClassName>`
- **Source set**: androidInstrumentedTest | commonTest
- **Module path**: `app/src/androidTest/java/org/dhis2/usescases/<feature>/`
- **Claimed program**: `<UID>` `<Program Name>` (proposed / claimed)
- **Program config changes**: <e.g., add formName on DE X; add SHOWWARNING
  rule; seed 3 events>
- **Workflow shape**: One workflow `@Test` named
  `should<DescribeJourney>()` covering N checkpoints (ANDROAPP-…,
  ANDROAPP-…). If split into more than one workflow, list each with the
  reason (e.g., "needs a separate launch — completed event").
- **Workflow steps (ordered)**:
  1. <step description> — checkpoints: [ANDROAPP-####], [ANDROAPP-####]
  2. <step description> — checkpoints: [ANDROAPP-####]
  3. (deferred) <description> — [ANDROAPP-####] *(blocked by …)*
- **Robots**: <new or reused, with method list>
- **MockWebServer fixtures**: <endpoints and example payloads>
- **Test tag additions needed**: <`SCREEN_COMPONENT_TAG` constants to add and
  where>
- **Setup shared with**: <other flows / BaseTest, if any>
- **Cases covered**: ANDROAPP-1234, ANDROAPP-1456 (active) + ANDROAPP-1457
  (deferred — see step 3)

(repeat per flow)

## Programs claimed

| Flow | Program UID | Name | Status | Config changes needed |
|------|-------------|------|--------|-----------------------|
| A (new)            | `<UID>`     | …    | proposed / claimed              | … |
| B (extending Flow A) | `<UID>`   | (reusing Flow A's program) | reused — no new claim | none / minor |

## Gaps in source cases
- ANDROAPP-1457: "Then enrollment date is displayed" — date format not
  specified. **Decision needed.**
- ANDROAPP-1502: Missing Given (no precondition stated).

## Improve existing vs create new
- **Improve**: <list of existing tests/Robots to extend, with what changes>
- **Create new**: <list of new files to create>

## Decisions needed before implementation
1. Date format for ANDROAPP-1457 → ?
2. ...

## Approve to implement?
```

## Behavior rules

- **No Kotlin in plan mode.** The plan is markdown only.
- **Always end with the literal line `## Approve to implement?`** so the
  parent agent knows where to stop.
- **Name every flow** with a concrete TestClass-style name. No "TBD".
- **Be explicit about gaps.** If a case is missing Given/When/Then or has an
  ambiguous Then, list it under Gaps with the exact case key. Do not paper
  over gaps by inventing steps.
- **Quote source content verbatim** when surfacing ambiguity, so the user can
  cross-check with the Zephyr case.
- **Distinguish reuse from new work.** Every flow row says "new" or
  "reused (+ delta)".

## Handoff to implementation

When the user approves, hand the approved `flow-plan.md` to the
`android-testing` skill along with any decisions captured during approval
(e.g. "use dd/MM/yyyy"). The android-testing skill generates the actual Kotlin
following project conventions.

After implementation, ask whether the plan should be published as a child
page under the Automated Testing Confluence folder (parent page id
`644644869`). Do not publish without an explicit "yes".
