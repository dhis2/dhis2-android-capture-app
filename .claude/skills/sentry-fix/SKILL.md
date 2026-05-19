---
name: sentry-fix
description: >
  Given a Sentry issue ID, fetches the full event and stack trace, reads all
  relevant Kotlin source files, diagnoses the root cause, implements a fix
  following AGENTS.md guidelines, writes unit tests, and runs ktlintFormat,
  ktlintCheck, and the relevant test task. Invoke as /sentry-fix <issue-id>
  or follow a /sentry-triage report.
---

# Sentry Fix Skill

Org: `dhis2` · Project: `dhis2-android-capture`
Stack traces are deobfuscated (ProGuard mappings uploaded on release builds).

**Input**: one Sentry issue ID, e.g. `DHIS2-ANDROID-1234` or the full numeric ID.

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

## Step 1 — Fetch full issue and recent events

Call `mcp__sentry__get_issue` with the issue ID. Note: title, culprit, `firstSeen`,
`lastSeen`, `userCount`, `count`, any linked tags.

Then call `mcp__sentry__get_issue_events` with `limit: 5`. For each event extract:
- Full stack trace (`exception.values[*].stacktrace.frames`) — all frames including SDK ones
- All breadcrumbs (last 20, in chronological order) — reconstruct what the user was doing
- `user`, `tags` (`release`, `environment`, `screen`), `extra`

If the top frames differ significantly across the 5 events, note it before proceeding:
the issue may aggregate multiple distinct root causes. Fix the most common pattern first
and state what was skipped.

---

## Step 2 — Map stack frames to source files

Use the same package→path mapping as in `/sentry-triage`:

| Package prefix | Source root |
|----------------|-------------|
| `org.dhis2.mobile.login.*` | `login/src/(commonMain\|androidMain)/kotlin/` |
| `org.dhis2.mobile.sync.*` | `sync/src/commonMain/kotlin/` |
| `org.dhis2.mobile.commons.*` | `commonskmm/src/commonMain/kotlin/` |
| `org.dhis2.mobile.aggregates.*` | `aggregates/src/commonMain/kotlin/` |
| `org.dhis2.tracker.*` | `tracker/src/(commonMain\|androidMain)/kotlin/` |
| `org.dhis2.form.*` | `form/src/main/java/` |
| `org.dhis2.commons.*` | `commons/src/main/java/` |
| `org.dhis2.*` (remaining) | `app/src/main/java/` |

Read these files:
1. The **crash-site file** (first app-owned frame)
2. Files for the **3 frames above** the crash site in the call chain
3. The **repository interface** if the crash is in a repository implementation (use `grep -r`
   to find the interface declaration)
4. The **UseCase** that calls the crashing repository or ViewModel method
5. The **Koin DI module** for the affected feature (to understand injection and scope)

Skip frames whose `absPath` or `filename` is `SourceFile:N` (unresolvable). When
`org.hisp.dhis.android.core` is the top frame, the real fix location is the first
**app-owned** frame — treat that as the crash site.

---

## Step 3 — Diagnose root cause

Identify precisely:
- The exact line that throws / causes the bad state
- The missing precondition: null check, unhandled empty collection, wrong state machine
  transition, coroutine scope leaked after lifecycle end, unhandled `D2Error`, etc.
- Whether the crash is in `commonMain` or `androidMain`
- Whether the DHIS2 SDK (`D2`) is the proximate cause or just the site — if the app is
  calling D2 without checking a precondition, the fix belongs in the app

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

---

## Step 5 — Implement the fix

Follow all rules from `AGENTS.md`:

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

---

## Step 7 — Run lint and tests

Run in this exact order. Fix any failures before moving on.

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

If tests fail, iterate on the fix. Do not skip or comment-out failing tests.

---

## Step 8 — Report

Output a summary in this format:

```
## Fix Summary — <Issue ID>

**Root cause**: <one sentence>
**Fix**: <what changed and why — 2-3 sentences>
**Files changed**:
- `path/to/File.kt` — <what changed>
- `path/to/FileTest.kt` — <tests added>
**Lint**: passed
**Tests**: passed (<test class>::<method>, ...)
```

If you cannot determine a safe fix (e.g. the root cause is inside the DHIS2 Android SDK
with no workaround), state that clearly with a recommended action (e.g. file a bug against
the SDK, add a defensive guard to prevent the crash from surfacing to users, add a Sentry
breadcrumb to improve future diagnosis).

---

## Step 9 — Create branch and open PR

**CRITICAL**: The fix branch must be created FROM the branch where this skill is triggered,
and the PR must target that same branch. Never use `main`, `develop`, or `origin/main` as
the base unless you are explicitly told to.

```bash
# 1. Record the current branch BEFORE creating the fix branch
BASE_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# 2. Create fix branch FROM that base — never from main or origin/main
git checkout -b fix/sentry-<issue-id-lowercase> "$BASE_BRANCH"

# 3. Stage and commit
git add <files>
git commit -m "fix: <short description of fix>\n\nCo-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"

# 4. Push
git push -u origin fix/sentry-<issue-id-lowercase>

# 5. Open PR as draft targeting BASE_BRANCH (not main/develop)
gh pr create \
  --draft \
  --base "$BASE_BRANCH" \
  --title "fix: <short description>" \
  --body "..."
```

Sentry's GitHub integration scans PR bodies and commit messages for `Fixes <SHORT-ID>`
(e.g. `Fixes DHIS2-ANDROID-CAPTURE-83MK`) and automatically links the PR on the Sentry
issue page — no extra API call needed.

Always include both lines in the PR body under a `## Sentry issue` section:
```
Fixes <SENTRY-SHORT-ID>
https://dhis2.sentry.io/issues/<SENTRY-SHORT-ID>/
```

- `Fixes <SENTRY-SHORT-ID>` — triggers Sentry's GitHub integration to auto-link the PR on the issue page
- The URL — provides a direct clickable link from the PR to the Sentry issue
