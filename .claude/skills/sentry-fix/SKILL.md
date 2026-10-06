---
name: sentry-fix
description: >
  Given a Sentry issue ID, fetches the full event and stack trace, attributes
  the crash to its owning repo (capture app, DHIS2 Android SDK, or mobile
  design system), reads the relevant sources at the shipped version, diagnoses
  the root cause, implements a fix in the owning repo following that repo's
  conventions, writes the failing unit test first, runs its lint/test tasks,
  and opens a draft PR there. For app-owned fixes it creates or reuses an
  ANDROAPP Jira Bug and opens the draft PR from an `ANDROAPP-<key>` branch
  against the branch it was triggered from, then schedules a check to
  auto-resolve the Sentry issue once that Jira ticket reaches Done. Invoke as
  /sentry-fix <issue-id> [--repo <slug>] or follow a /sentry-triage report.
---

# Sentry Fix Skill

Stack traces are deobfuscated (ProGuard mappings uploaded on release builds), so
library frames carry real class names. The fix — and its PR — belongs in the
repo that owns the bug, not necessarily this one.
`.claude/skills/sentry-triage/references/repo-map.md` is the single source of
truth for attribution, sibling-repo rules, per-repo commands, and PR
conventions. Load it before Step 2.

**Input**: one Sentry issue ID, e.g. `PROJ-1234` or the full numeric ID, plus an
optional `--repo <slug>` hint from a triage report (advisory — re-verified in
Step 2).

---

## Prerequisites — Sentry MCP plugin

This skill requires the `sentry@claude-plugins-official` plugin. Before running any step,
verify the plugin is available by checking whether `mcp__plugin_sentry_sentry__get_sentry_resource`
is listed as an available tool.

If the plugin is **not installed**, stop and tell the user:

> The Sentry MCP plugin is not enabled in this session. To install it locally, run:
> ```
> /config
> ```
> Then navigate to **Extensions → Plugins**, find **Sentry**, and enable it. Alternatively,
> add the following to your `~/.claude/settings.json` (user-level, not committed to the repo):
> ```json
> {
>   "enabledPlugins": {
>     "sentry@claude-plugins-official": true
>   }
> }
> ```
> Once enabled, restart the session and run `/sentry-fix <issue-id>` again.

If invoked from a `/sentry-triage` report, the issue ID is in the "To fix" line of
each issue entry.

---

## Step 0 — Discover Sentry org

Call `mcp__plugin_sentry_sentry__find_organizations` to list accessible orgs. If there is
only one, use it. If there are multiple, pick the one whose slug matches the GitHub org of
the current repo (run `gh repo view --json owner -q .owner.login` to get it).

Store the result as `ORG_SLUG` and the org's `regionUrl` as `REGION_URL`. These are used
for all subsequent Sentry tool calls and to construct the Sentry issue URL:
`https://<ORG_SLUG>.sentry.io/issues/<ISSUE-SHORT-ID>/`

If the input is a bare suffix with no project prefix (e.g. `83NS`), the issue
lookup rejects it. Resolve the project with the `find_projects` catalog tool
(via `execute_sentry_tool`, `query: "android"`) and retry as
`<PROJECT-SLUG-UPPERCASE>-<suffix>`, e.g. `DHIS2-ANDROID-CAPTURE-83NS`. Use that
full short ID everywhere below.

---

## Step 1 — Fetch full issue and recent events

Call `mcp__plugin_sentry_sentry__get_sentry_resource` with the issue ID and `ORG_SLUG`. Note: title, culprit, `firstSeen`,
`lastSeen`, `userCount`, `count`, any linked tags.

Then fetch recent events: discover the issue-events tool via
`mcp__plugin_sentry_sentry__search_sentry_tools(query: "issue events")` and run
it with `execute_sentry_tool` (limit: 5); if unavailable, work from the latest
event embedded in the `get_sentry_resource` response. For each event extract:
- Full stack trace (`exception.values[*].stacktrace.frames`) — all frames including SDK ones
- All breadcrumbs (last 20, in chronological order) — reconstruct what the user was doing
- `user`, `tags` (`release`, `environment`, `screen`), `extra`

If the top frames differ significantly across the 5 events, note it before proceeding:
the issue may aggregate multiple distinct root causes. Fix the most common pattern first
and state what was skipped.

---

## Step 2 — Attribute to the owning repo and map frames to source files

Load `.claude/skills/sentry-triage/references/repo-map.md` — classification
table (§1), attribution heuristic (§2), per-repo facts (§3), sibling-repo rules
(§4), version-skew rules (§5).

1. Classify every frame by package prefix and apply the attribution heuristic to
   determine the **owning repo**. A `--repo` hint from triage is a prior, not a
   verdict: re-verify it, and if the evidence disagrees, say so and follow the
   evidence.
2. **Owner is an attribute-only lib** (rule engine, expression parser) → skip to
   Step 8 and report a diagnosis plus an upstream recommendation; no clone, no PR.
3. **Owner = app** → map frames with the app module table (repo-map §3) and read:
   1. The **crash-site file** (first app-owned frame)
   2. Files for the **3 frames above** the crash site in the call chain
   3. The **repository interface** if the crash is in a repository implementation
      (use `grep -r` to find the interface declaration)
   4. The **UseCase** that calls the crashing repository or ViewModel method
   5. The **Koin DI module** for the affected feature (injection and scope)
4. **Owner = SDK or design system** →
   1. Resolve the sibling clone and canonical remote (repo-map §4.1–4.2); if the
      clone is missing, offer `gh repo clone` and stop if declined.
   2. Resolve the **shipped lib version** from the crashing release — the
      event's `release` tag names the app version;
      `git show <app-tag>:gradle/libs.versions.toml` pins the lib — then the
      diagnosis ref (repo-map §5).
   3. Read the crash-site files at the diagnosis ref via
      `git -C ../<repo> show <ref>:<path>` — never check anything out in the
      user's clone.
   4. **Already fixed upstream?** Compare the crash site at the diagnosis ref
      against the PR base branch (repo-map §5.5). If fixed there, report
      "bump `<version key>` in `gradle/libs.versions.toml`" and stop — no PR.
   5. Create the isolated worktree (repo-map §4.3). All subsequent edits,
      builds, and commits happen inside it.

Skip frames whose `absPath` or `filename` is `SourceFile:N` (unresolvable).
Never redirect a genuine library bug to "the first app-owned frame" — fix it in
the owning repo.

---

## Step 3 — Diagnose root cause

Identify precisely:
- The exact line that throws / causes the bad state
- The missing precondition: null check, unhandled empty collection, wrong state machine
  transition, coroutine scope leaked after lifecycle end, unhandled `D2Error`, etc.
- Whether the crash is in `commonMain` or `androidMain` (or, for library fixes,
  which source set / module of that repo)
- Whether the owner from Step 2 still holds: if the app violates a documented
  library precondition, the fix belongs in the app even though the throw is in
  the library; if the library mishandles valid input, fix the library. If the
  diagnosis flips ownership, state it and redo Step 2's setup for the right repo.

State the root cause in one sentence before writing any code.

---

## Step 4 — Plan the fix

Before touching any file, state which files will change and why.

- **App-owned**: the architecture and code conventions are in `AGENTS.md` —
  follow it and don't restate it here. On top of it, for a crash fix:
  - Make the smallest fix in the existing style of the touched code; don't
    migrate a legacy screen as part of a crash fix.
  - Put the decision in a layer Step 5 can unit-test (ViewModel / UseCase /
    UiState), not in an Activity, Fragment or binding — `:app` has no
    Robolectric.
  - Add a defensive guard at the crash site (e.g. an early return instead of
    `!!`) so other paths into the same code cannot crash.
- **SDK**: which `core/` classes change, and whether the public API surface
  changes (`:core:apiCheck` will fail → plan `:core:apiDump` + commit the dump).
- **Design system**: which source set (`commonMain` vs platform actuals), and
  whether any Paparazzi golden image could be affected — if so, plan for the
  "Generate Paparazzi Golden Images" CI workflow (repo-map §3); never regenerate
  goldens locally.

---

## Step 5 — Write the failing test first (TDD)

Write the test that reproduces the crash condition **before** touching
production code, and confirm it fails — on an assertion, or because the API the
fix introduces does not compile yet. If the crash cannot be reproduced in a unit
test, say so and move the decision into a testable layer (Step 4).

- **App-owned**: load the `android-testing` skill and follow it. Add the test to
  the existing test class of the touched class and assert the state that was
  wrong.
- **SDK**: `core/src/test/java/`, class named `<Class>Should`, mirroring the
  neighboring tests of the touched class.
- **Design system**: pure-Kotlin tests next to the existing tests of the same
  component (they run via `desktopTest`); a Paparazzi snapshot test only if a
  visual contract changed, and let CI generate the goldens.

---

## Step 6 — Implement the fix

Make the smallest change that turns the Step 5 test green without breaking
neighboring tests.

- **App-owned**: follow `AGENTS.md`.
- **Library-owned**: the app's conventions do NOT apply. Follow the target
  repo's own guidance — SDK: its committed `CLAUDE.md`; design system: its
  `CLAUDE.md` if present (untracked, may be absent), else `README.md` + `docs/`
  — plus the hard constraints in repo-map §3. Match the naming, idiom, and
  comment density of the surrounding code.
- **Never narrate the fix in the source** (any repo): no Sentry IDs, no
  before/after explanation, no "moved this because it crashed". That history
  belongs in the PR and Jira ticket. A comment, if warranted, is one line in
  the present tense describing what the code does.

---

## Step 7 — Run lint and tests

Run in this exact order, **inside the repo that owns the fix** (for libraries:
inside the worktree from Step 2). Fix any failures before moving on.

**App**:
```bash
# 1. Auto-fix formatting
./gradlew ktlintFormat

# 2. Verify no remaining violations
./gradlew ktlintCheck

# 3. Run tests for the affected module
# KMP module (commonTest source set):
./gradlew :<module>:testAndroidHostTest

# KMP module (androidUnitTest source set):
./gradlew :<module>:testAndroidDebugUnitTest

# Legacy Android module (form, commons, tracker, …):
./gradlew :<module>:testDebugUnitTest

# :app has product flavors — the task is flavor-qualified:
./gradlew :app:testDhis2DebugUnitTest --tests "<package>.*"
```
Per-module task names live in repo-map §3.

**SDK** (in the worktree):
```bash
./gradlew :core:ktlintFormat :core:ktlintCheck
./gradlew :core:testDebugUnitTest
./gradlew :core:apiCheck   # fails on public API change → :core:apiDump + commit the dump
```

**Design system** (in the worktree):
```bash
./gradlew ktlintFormat ktlintCheck
./gradlew desktopTest
./gradlew designsystem:testDebugUnitTest
```

If a Gradle build in a library worktree cannot locate the Android SDK, copy
`local.properties` from the sibling clone root into the worktree.

If tests fail, iterate on the fix. Do not skip or comment-out failing tests.

---

## Step 8 — Report

Output a summary in this format:

```
## Fix Summary — <Issue ID>

**Root cause**: <one sentence>
**Owner**: <repo> (<"as triaged" | "overridden from --repo hint because …">)
**Fix**: <what changed and why — 2-3 sentences>
**Files changed**:
- `path/to/File.kt` — <what changed>
- `path/to/FileTest.kt` — <tests added>
**Lint**: passed
**Tests**: passed (<test class>::<method>, ...)
**Ships via**: <library-owned only: lib release → gradle/libs.versions.toml bump → app release. If the crash is severe, propose an app-side defensive guard as a companion PR so shipped versions stop crashing sooner.>
```

If you cannot determine a safe fix — the owner is an attribute-only lib (rule
engine, expression parser), or the root cause is genuinely unclear — state that
with a recommended action: a diagnosis for a manual upstream fix, a defensive
guard in the app to stop the crash surfacing to users, or a Sentry breadcrumb to
improve future diagnosis.

---

## Step 9 — Create branch and open PR (in the owning repo)

Every PR is opened as a **draft**, and every PR body includes a `## Sentry issue`
section:

```
## Sentry issue
Fixes <SENTRY-SHORT-ID>
https://<ORG_SLUG>.sentry.io/issues/<SENTRY-SHORT-ID>/
```

- `Fixes <SENTRY-SHORT-ID>` — Sentry's GitHub integration scans PR bodies for it
  and auto-links the PR on the issue page. It only fires for repos connected in
  the Sentry org's GitHub integration (the app repo is; the library repos may
  not be) — include it anyway.
- The URL (from `ORG_SLUG` resolved in Step 0) always works regardless.

### Step 9a — Owner = app (this repo)

**CRITICAL**: The fix branch must be created FROM the branch where this skill is
triggered, and the PR must target that same branch. Never use `main`, `develop`,
or `origin/main` as the base unless you are explicitly told to. (This rule is
app-repo-only — library base branches are fixed in Step 9b.)

Once lint and tests pass (Step 7), run this whole step without pausing for
confirmation — Jira ticket, branch, commit, push, draft PR. The draft PR is the
review point.

**0. Jira (required — before the branch exists, because the key names it)**

- Check whether Atlassian/Jira MCP tools are connected: search for them (e.g.
  `ToolSearch` with a query like `"jira accessible resources create issue search"`).
  Tool names are prefixed with a connection-specific server ID, so match by
  keyword, not by a hardcoded name.
- **Not connected** → ask the user once to connect the Atlassian MCP. Declined,
  or still unavailable → continue with no ticket: branch
  `fix/sentry-<issue-id-lowercase>`, no `[ANDROAPP-XXXX]` title prefix, no
  `Related task:` line, and state "no Jira ticket" in the Step 8 report.
- **Connected** → resolve `cloudId` via the accessible-resources tool, then:
  1. Search the `ANDROAPP` project for an issue that already covers this
     crash. Run several narrow JQL queries — the Sentry short-ID suffix, the
     crash-site method name, the crash-site class name plus a symptom word
     (`project = ANDROAPP AND text ~ "<term>"`, `fields: ["summary","status"]`)
     — rather than one broad one; an unscoped `text ~` search across the
     whole project can return an oversized result.
  2. **Found an existing open issue** covering the same crash → reuse its key
     as `JIRA_KEY`; do not create a duplicate. Leave its description alone
     except to append the `## How to test manually` section described in 3 if
     it has none. A matching issue that is already **closed** (same symptom,
     earlier fix) is not reused — create a new one and link it (see 4).
  3. **Nothing open found** → create a new `Bug` in `ANDROAPP`. Before
     creating, fetch this issue type's required fields
     (`getJiraIssueTypeMetaWithFields`) and set at least:
     - `components: [{"id": "10415"}]` (`AndroidApp`, unless a more specific
       component obviously fits)
     - `environment` — free text, release + platform, e.g.
       `"Android app 3.4.2 (build 157), production"`
     - `versions` — `Affects versions`: the crashing release from Step 1's
       `release` tag, matched by name against the field's allowed values
     - `customfield_10131: {"id": "10196"}` — Internal feature = General
       interest (creation returns 400 without it)
     - `customfield_10135: {"id": "10203"}` — Product Team = Android

     A new Bug starts in status **Open**; confirm the create response shows
     `Open` and do not transition it. Store the new key as `JIRA_KEY`.

     **Write the description short and scannable** — a developer opening the
     ticket should grasp the problem in about fifteen seconds:
     - Pass `contentFormat: "markdown"` and write **real Markdown**. Use `##`
       for section headers. Do **not** use Jira wiki markup (`h2.`, `{code}`,
       `*bold*`) — it renders literally and makes the ticket harder to read.
     - Aim for three short sections — what breaks, why, and impact — of a
       couple of sentences each, then the manual test below. Prose over
       bullets-of-bullets.
     - **Never paste a stack trace, event payload, or log dump.** Link the
       Sentry issue and let it hold the crash detail — it is always more
       current than a copy, and the dump is what makes these tickets
       unreadable. Name the crash site as `File.kt:line` in prose instead.
     - Include: the Sentry issue link(s) with user/event counts, the
       one-sentence root cause from Step 3, the affected release, and (once
       known) the PR link.
     - Close with a `## How to test manually` section for a field tester on a
       **release APK** (no adb, no debug build, no logcat), derived from the
       code read in Steps 2-6:
       1. Preconditions — user and metadata needed (never a hardcoded UID).
       2. Numbered steps using the labels visible on screen, including any
          state the crash needs (search form closed, offline, rotated…).
       3. **Expected** — one observable outcome on the fixed build, plus what
          the shipped build did instead.
       4. Variants where the code branches (landscape, offline), then a
          regression step showing the normal path still works.

       Self-check before writing it: walk the steps against the fixed code
       (Expected holds) and against the shipped release (it reproduces the
       bug). If either fails, rewrite the steps — a test a correct build would
       fail, or a broken build would pass, is worse than none.
     - Nothing else — no stack traces, no tool transcripts, no fix history.
  4. If the search in 1 (or a `/sentry-triage` report) surfaced a clearly
     related prior ticket — a closed issue with the same symptom, the same
     anti-pattern, an adjacent call site or repo — link `JIRA_KEY` to it
     (`createIssueLink`, type `Relates`) and mention the link when reporting
     back.
  5. After the PR exists, add a comment to `JIRA_KEY` with the PR URL
     (`addCommentToJiraIssue`).

**Branch naming** follows the team convention `ANDROAPP-<key>` (optionally
`ANDROAPP-<key>-<short-desc>`), see repo-map §3. Name it correctly **before**
opening the PR: GitHub cannot retarget an open PR's head branch, and renaming a
pushed branch is blocked by org rulesets.

```bash
# 1. Record the current branch BEFORE creating the fix branch
BASE_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# 2. Branch name: the Jira key; fix/sentry-<id> only when Jira was declined
BRANCH="<JIRA_KEY>"            # e.g. ANDROAPP-7860
# BRANCH="fix/sentry-<issue-id-lowercase>"   # no-ticket fallback

# 3. Create the fix branch FROM that base — never from main or origin/main
git checkout -b "$BRANCH" "$BASE_BRANCH"

# 4. Stage and commit — end the message with the Co-Authored-By trailer the
#    harness specifies for the current model (never hardcode a model name)
git add <files>
git commit -m "fix: [<JIRA_KEY>] <short description of fix>"

# 5. Push
git push -u origin "$BRANCH"

# 6. Open PR as draft targeting BASE_BRANCH (not main/develop)
gh pr create \
  --draft \
  --base "$BASE_BRANCH" \
  --head "$BRANCH" \
  --title "fix: [<JIRA_KEY>] <short description>" \
  --body "..."
# (commit/title are "fix: <short description>", no brackets, when there is no JIRA_KEY)
```

When `JIRA_KEY` exists, add one line to the `## Sentry issue` section of the PR
body (format below):
```
Related task: [<JIRA_KEY>](https://<atlassian-site>/browse/<JIRA_KEY>)
```

### Step 9b — Owner = SDK or design system

Work happens inside the worktree created in Step 2; the branch
`fix/sentry-<short-id>` already exists there. Base branches are fixed per repo
(repo-map §3): SDK → `develop`, design system → `develop`. Never `master`/`main`.

1. **Jira**
   - **SDK**: ask the user for an existing ANDROSDK ticket or offer to create
     one via the Atlassian MCP. Ticket → rename the branch to it
     (`git -C <worktree> branch -m ANDROSDK-XXXX`); declined → keep
     `fix/sentry-<short-id>`.
   - **Design system**: reference an ANDROAPP code in the title only if one
     exists; do not create tickets for it.
2. **Commit** — per-repo trailer rules: SDK commits carry **no Co-Authored-By
   and no 🤖 footer** (its `open-pr` skill forbids both); design-system commits
   keep the standard harness trailer.
3. **Push** — check push rights first (repo-map §4.6): push the canonical dhis2
   remote, or fall back to `gh repo fork --remote` + `--head <login>:<branch>`.
4. **Open the draft PR** with title/body per repo-map §3:
   - SDK: `fix: [ANDROSDK-XXXX] <short imperative>` (or `fix: <desc>` + body
     note when no ticket), body 1–2 paragraphs + `Related task:` Jira link +
     the Sentry issue section.
   - Design system: `fix: [ANDROAPP-XXXX] <desc>` matching recent merged PRs +
     the Sentry issue section.
   ```bash
   gh pr create --repo dhis2/<name> --base develop --draft \
     --title "<per repo-map>" --body "..."
   ```
5. **After the PR**: keep the worktree for review iteration and print its
   removal command (repo-map §4.5); optionally note the PR URL on the Sentry
   issue via the MCP update tool (non-fatal if unavailable).
6. **Delivery note**: end with the "Ships via" line from Step 8 — the fix
   reaches users only after a library release plus an app version bump; for
   severe crashes propose an app-side defensive guard as a companion PR.

---

## Step 10 — Auto-resolve the Sentry issue once the Jira ticket is Done

Only applies when Step 9a's Jira sub-step produced a `JIRA_KEY` (skip this
step entirely if there is none — nothing to close the loop with).

Jira polling can't be a background shell script — there is no CLI for the
Atlassian MCP the way `gh` is a CLI for GitHub, so a detached process cannot
call `getJiraIssue`. Only the agent itself can, which means this has to run as
scheduled **prompts** (`CronCreate`), not a background script.

Run this right after the draft PR is opened (a ticket created minutes earlier
is never Done yet, so there is no immediate check):

1. **Schedule a recurring check** — `CronCreate` with a
   self-contained prompt (it must carry everything needed, since it runs as a
   fresh turn with no memory of this conversation):
   > Check Jira issue `<JIRA_KEY>`'s status via the Atlassian MCP
   > (`getJiraIssue`, `fields: ["status"]`). If `status.statusCategory.key` is
   > `"done"`, mark Sentry issue `<SENTRY-SHORT-ID>` resolved via the Sentry
   > MCP (`update_issue`, `status: "resolved"`, `reason: "Closed via Jira
   > <JIRA_KEY>"`, `organizationSlug: "<ORG_SLUG>"`), then delete this cron
   > job with `CronDelete`. Otherwise do nothing — the next scheduled run will
   > check again.

   A cadence of every few hours (e.g. `"17 */6 * * *"`) is plenty — Jira
   tickets move on the order of days, not minutes. Tell the user the cadence
   chosen and both caveats up front: recurring cron jobs auto-expire after 7
   days, and none of this survives the session ending. If either limit is hit
   before the ticket is closed, the Sentry issue is left as-is with no
   automatic follow-up — say so plainly rather than implying it will
   eventually resolve on its own.
2. **Resolve**: when the cron prompt finds the ticket Done, it calls the
   Sentry MCP's `update_issue` with `status: "resolved"` and
   `reason: "Closed via Jira <JIRA_KEY>"` on the original Sentry issue. That
   report is the cron prompt's own final message (possibly in a different
   session).
