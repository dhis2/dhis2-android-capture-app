---
name: sentry-fix
description: >
  Given a Sentry issue ID, fetches the full event and stack trace, attributes
  the crash to its owning repo (capture app, DHIS2 Android SDK, or mobile
  design system), reads the relevant sources at the shipped version, diagnoses
  the root cause, implements a fix in the owning repo following that repo's
  conventions, writes unit tests, runs its lint/test tasks, and opens a draft
  PR there. Invoke as /sentry-fix <issue-id> [--repo <slug>] or follow a
  /sentry-triage report.
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

Before touching any file, state:
- Which files will change and why
- KMP placement decision:
  - `commonMain` — if the fix is pure Kotlin logic with no Android API dependency
  - `androidMain` — if it requires Android `Context`, DHIS2 `D2` object,
    `CrashReportController`, or Android SDK APIs
- If the crash site uses RxJava (`Observable`, `Single`, `Completable`): **do not add more
  RxJava**. Wrap the existing RxJava call at the nearest boundary using a coroutine adapter
  (`suspendCancellableCoroutine` or an existing wrapper in the codebase).
- If new business logic is needed: create a new `UseCase<in R, out T>` from
  `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/UseCase.kt`

The KMP/UseCase bullets above apply to **app-owned** fixes. For **library-owned**
fixes state the equivalent per target repo:
- **SDK**: which `core/` classes change, and whether the public API surface
  changes (`:core:apiCheck` will fail → plan `:core:apiDump` + commit the dump)
- **Design system**: which source set (`commonMain` vs platform actuals), and
  whether any Paparazzi golden image could be affected — if so, plan for the
  "Generate Paparazzi Golden Images" CI workflow (repo-map §3); never regenerate
  goldens locally

---

## Step 5 — Implement the fix

For **app-owned** fixes follow all rules from `AGENTS.md`:

- **ViewModels**: use `launchUseCase { }`, never `viewModelScope.launch` directly —
  `launchUseCase` wraps `CoroutineTracker` for Espresso `IdlingResource` integration
- **Repositories**: translate `D2Error` → domain errors via `DomainErrorMapper`
  ```kotlin
  import org.dhis2.mobile.commons.error.DomainErrorMapper
  import org.hisp.dhis.android.core.maintenance.D2Error
  ```
- **Models**: `data class` for new data models; `sealed interface` for new UiState variants
- **Style** (`ktlint_official`):
  - No wildcard imports
  - Trailing commas on every multi-line parameter/argument list
  - Expression bodies for single-expression functions
- **No comments** unless the WHY is non-obvious (hidden constraint, workaround for a
  specific upstream bug)

For **library-owned** fixes the app's conventions do NOT apply (no
`launchUseCase`, no `DomainErrorMapper`). Follow the target repo's own guidance:
- **SDK**: its committed `CLAUDE.md`
- **Design system**: its `CLAUDE.md` if present (it is untracked and may be
  absent), else `README.md` + `docs/`
- Plus the hard constraints in repo-map §3 (SDK: `apiCheck`/`apiDump`; design
  system: no local golden regeneration, `allWarningsAsErrors`)
- Match the naming, idiom, and comment density of the surrounding code in that repo

---

## Step 6 — Write unit tests

Load the `android-testing` skill for full patterns. At minimum write:

**UseCase test** (if the UseCase was created or modified):
- Success path
- Failure path (wraps exception in `Result.failure`)
- The specific edge case that caused the crash (e.g. empty list, null return from D2)

**ViewModel test** (if the ViewModel was modified):
- The state transition that was failing (use `app.cash.turbine` to assert `StateFlow` emissions)
- Use `launchUseCase` / `CoroutineTracker` idiom — never `Thread.sleep()`

**Repository test** (if the repository was modified):
- Mock D2 with `mock(defaultAnswer = RETURNS_DEEP_STUBS)`
- The `D2Error` → domain error mapping path

**Placement**:
- `commonTest/` — for classes in `commonMain`
- `androidUnitTest/` — for classes in `androidMain`
- Existing module test source set — for legacy Android modules (`form`, `commons`, `tracker`, `app`)
- **SDK** — `core/src/test/java/`, class named `<Class>Should`, mirroring the
  neighboring tests of the touched class
- **Design system** — put pure-Kotlin tests next to the existing tests of the
  same component (they run via `desktopTest`); add a Paparazzi snapshot test only
  if a visual contract changed, and let CI generate the goldens

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

# Legacy Android module:
./gradlew :<module>:testDebugUnitTest
```

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

**0. Jira (optional, before creating the branch)**

- Check whether Atlassian/Jira MCP tools are connected: search for them (e.g.
  `ToolSearch` with a query like `"jira accessible resources create issue search"`).
  Tool names are prefixed with a connection-specific server ID, so match by
  keyword, not by a hardcoded name.
- **Not connected** → ask the user once: "I can create/link a Jira issue for
  this fix if you connect the Atlassian MCP — want me to, or should I skip
  Jira for this fix?" Declined, or still unavailable after asking → skip Jira
  entirely and continue to step 1 below with no ticket reference (the PR
  title/body stay exactly as documented further down, no `[ANDROAPP-XXXX]`
  prefix, no `Related task:` line).
- **Connected** → resolve `cloudId` via the accessible-resources tool, then:
  1. Search the `ANDROAPP` project for an issue that already covers this
     crash — narrow JQL by the Sentry short-ID, the crash-site class name, or
     a distinctive phrase from the culprit (`project = ANDROAPP AND text ~
     "<term>"`, `fields: ["summary","status"]`). Prefer several narrow queries
     over one broad one — an unscoped `text ~` search across the whole
     project can return an oversized result.
  2. **Found an existing open issue** covering the same crash → reuse its key
     as `JIRA_KEY`; do not create a duplicate.
  3. **Nothing found** → create a new `Bug` in `ANDROAPP`: summary matching
     the Sentry issue title, description with the Sentry issue link, a
     trimmed stack trace, the one-sentence root cause from Step 3, and (once
     known) the PR link. Before creating, fetch this issue type's required
     fields (`getJiraIssueTypeMetaWithFields`) — at the time of writing `Bug`
     requires `components` (use `AndroidApp` unless a more specific component
     obviously fits), `environment` (free text — release + platform is
     enough), and `versions` (`Affects versions` — the crashing release, e.g.
     the `release` tag from Step 1). Store the new key as `JIRA_KEY`.
  4. If the search in 1 (or a `/sentry-triage` report) surfaced a clearly
     related prior ticket — same anti-pattern, adjacent call site or repo —
     link `JIRA_KEY` to it (`createIssueLink`, type `Relates`) and mention the
     link when reporting back.

```bash
# 1. Record the current branch BEFORE creating the fix branch
BASE_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# 2. Create fix branch FROM that base — never from main or origin/main
git checkout -b fix/sentry-<issue-id-lowercase> "$BASE_BRANCH"

# 3. Stage and commit — end the message with the Co-Authored-By trailer the
#    harness specifies for the current model (never hardcode a model name)
git add <files>
git commit -m "fix: <short description of fix>"

# 4. Push
git push -u origin fix/sentry-<issue-id-lowercase>

# 5. Open PR as draft targeting BASE_BRANCH (not main/develop). Title gets the
#    [JIRA_KEY] prefix only if step 0 produced one:
gh pr create \
  --draft \
  --base "$BASE_BRANCH" \
  --title "fix: [<JIRA_KEY>] <short description>" \
  --body "..."
# (title is "fix: <short description>", no brackets, when there is no JIRA_KEY)
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
