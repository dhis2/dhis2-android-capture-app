# Repo Attribution Map — DHIS2 mobile repositories

Single source of truth for multi-repo Sentry triage and fixes. Loaded by both
`/sentry-triage` and `/sentry-fix`. Everything repo-specific (attribution,
commands, PR conventions, constraints) lives here so the skills stay
orchestration-only.

The capture app APK ships code from several DHIS2-owned repositories. Stack
frames are deobfuscated (ProGuard mappings cover the whole APK), so library
frames carry real class/file names and are attributed by package prefix.

---

## 1. Classification table

| Package prefix | Repo | GitHub slug | Local clone | Support |
|---|---|---|---|---|
| `org.dhis2.*` | Capture app | `dhis2/dhis2-android-capture-app` | this repo | full |
| `org.hisp.dhis.android.*` | Android SDK | `dhis2/dhis2-android-sdk` | `../dhis2-android-sdk` | full |
| `org.hisp.dhis.mobile.ui.*` | Design system | `dhis2/dhis2-mobile-ui` | `../dhis2-mobile-ui` | full |
| `org.hisp.dhis.rules.*` | Rule engine | `dhis2/dhis2-rule-engine` | — | attribute-only |
| `org.hisp.dhis.lib.expression.*` | Expression parser | `dhis2/expression-parser` | — | attribute-only |
| `androidx.*`, `android.*`, `java.*`, `kotlin.*`, `kotlinx.*`, `io.sentry.*` | Platform / third-party | — | — | ignore |

- **full** — `/sentry-fix` implements, tests, and opens a draft PR in that repo.
- **attribute-only** — triage names the repo so the crash is not misattributed to
  the nearest app frame; `/sentry-fix` reports a diagnosis and recommends a manual
  upstream fix (no clone, no PR). Verify the slug with `gh repo view` before
  linking it in a report.
- **ignore** — never the owner; skip these frames when walking the stack.

---

## 2. Attribution heuristic — "thrown in" vs "owner"

Walk the innermost exception's frames from the crash site outward.

- **Thrown in** = repo of the innermost DHIS2-owned frame.
- **Owner** = the repo whose code is actually wrong. It differs from *thrown in*
  only when the innermost owned frame is a library frame at the app↔lib boundary
  (an app frame directly beneath it in the call chain) AND the exception is a
  precondition failure — `IllegalArgumentException`, `IllegalStateException`,
  NPE on a parameter, a `require(...)`/`checkNotNull(...)` message. That is API
  misuse: owner = the **calling** repo, confidence medium.
- ≥3 consecutive library frames beyond the last app frame, or no app frames at
  all (SDK-internal sync engine, background workers) → owner = the library,
  confidence high.
- Interleaved `app → designsystem → app` frames (composable content lambdas) →
  owner = app unless the crash is inside design-system internals, confidence medium.
- Anything unclear → owner = thrown-in repo, confidence low; `/sentry-fix` must
  re-verify before writing code.

Always record: `Owner`, `Thrown in`, `Confidence (high|medium|low)` plus a
one-clause reason. Triage's attribution is a **hypothesis**: `/sentry-fix`
re-derives ownership from real code at the shipped version and overrides it when
the evidence disagrees — and says so explicitly when that happens.

---

## 3. Per-repo facts

### dhis2/dhis2-android-capture-app — "app" (this repo)

- PR base: the branch the skill was triggered from — never `main`/`develop`
  unless explicitly told.
- PRs: **draft**, title `fix: <desc>` (or `fix: [ANDROAPP-XXXX] <desc>` when
  `/sentry-fix` linked a Jira ticket — see its Step 9a), `## Sentry issue`
  section, and the harness-provided `Co-Authored-By` trailer (never hardcode a
  model name).
- Jira: optional, app-repo fixes don't require a ticket. When the Atlassian
  MCP is connected, `/sentry-fix` searches `ANDROAPP` for an existing issue
  covering the crash and creates one if none exists (repo-map doesn't need
  updating when this changes — see the skill for current field requirements).
- Conventions: `AGENTS.md` (launchUseCase, DomainErrorMapper, KMP placement,
  ktlint, testing rules).
- Lint/tests: `./gradlew ktlintFormat ktlintCheck` + per-module test task
  (`testDebugUnitTest` AGP modules · `testDhis2DebugUnitTest` for `app` ·
  `testAndroidHostTest` for KMP modules, covering both their `commonTest` and
  `androidHostTest` source sets).
- Module mapping for `org.dhis2.*` frames:

| Package prefix | Source root |
|----------------|-------------|
| `org.dhis2.mobile.login.*` | `login/src/(commonMain\|androidMain)/kotlin/` |
| `org.dhis2.mobile.sync.*` | `sync/src/commonMain/kotlin/` |
| `org.dhis2.mobile.commons.*` | `commonskmm/src/commonMain/kotlin/` |
| `org.dhis2.mobile.aggregates.*` | `aggregates/src/commonMain/kotlin/` |
| `org.dhis2.mobileProgramRules.*` | `dhis2-mobile-program-rules/src/main/java/` |
| `org.dhis2.tracker.*` | `tracker/src/(commonMain\|androidMain)/kotlin/` |
| `org.dhis2.form.*` | `form/src/main/java/` |
| `org.dhis2.commons.*` | `commons/src/main/java/` |
| `org.dhis2.*` (remaining) | `app/src/main/java/` |

### dhis2/dhis2-android-sdk — "SDK"

- Local sibling: `../dhis2-android-sdk` · toml version key: `dhis2sdk`
  (artifact `org.hisp.dhis:android-core`) · release tags look like `1.14.1`.
- PR base branch: **`develop`** (`origin/HEAD` points at `master` — do not use it).
- Module `core/`; sources `core/src/main/java/` (Kotlin + Java); unit tests
  `core/src/test/java/`, classes named `<Class>Should`.
- Verify:
  ```bash
  ./gradlew :core:ktlintFormat :core:ktlintCheck
  ./gradlew :core:testDebugUnitTest
  ./gradlew :core:apiCheck   # public API changed intentionally → :core:apiDump and commit the dump
  ```
- Conventions: committed `CLAUDE.md`; PR style per its own `.claude/skills/open-pr`
  skill: title `fix: [ANDROSDK-XXXX] <short imperative>`, body 1–2 paragraphs
  ending `Related task: [ANDROSDK-XXXX](https://dhis2.atlassian.net/browse/ANDROSDK-XXXX)`,
  **no Co-Authored-By, no 🤖 footer**. No ticket → `fix: <desc>` title + a body
  note that no Jira ticket exists yet.
- Jira: Sentry-originated SDK fixes ask the user for an existing ANDROSDK ticket
  or offer to create one via the Atlassian MCP; if declined, keep the
  `fix/sentry-<short-id>` branch name.
- Sentry-fix policy: PR opened as **draft** (agent-authored) even though the
  repo norm is non-draft.

### dhis2/dhis2-mobile-ui — "design system"

- Local sibling: `../dhis2-mobile-ui` · toml version key: `designSystem`
  (artifact `org.hisp.dhis.mobile:designsystem`) · tags may lag releases
  (0.7.1 shipped with no tag — see §5 fallback chain).
- PR base branch: **`develop`** — verified from merged-PR history: `fix:` PRs
  merge to `develop`; `main` only receives release PRs.
- Module `:designsystem`, package root `org.hisp.dhis.mobile.ui.designsystem`;
  KMP source sets `commonMain`/`androidMain`/`desktopMain`/`iosMain`; Kotlin
  tests run on the desktop target; Paparazzi snapshot tests live in that repo's
  Android unit-test source set (a layout this app does not share).
- Verify:
  ```bash
  ./gradlew ktlintFormat ktlintCheck
  ./gradlew desktopTest
  ./gradlew designsystem:testDebugUnitTest
  ```
- Hard constraints: **never regenerate Paparazzi golden images locally** — if a
  golden must change, push the branch and run the "Generate Paparazzi Golden
  Images" GitHub Actions workflow on it; `allWarningsAsErrors` is on, so a new
  compiler warning fails the build.
- Conventions: `CLAUDE.md` is untracked (may be absent in other clones) — read
  it if present, else `README.md` + `docs/` + probe recent merged PRs
  (`gh pr list --repo dhis2/dhis2-mobile-ui --state merged --limit 5 --json title,baseRefName`).
  Title style `fix: [ANDROAPP-XXXX] <desc>` (mobile-ui uses the app's Jira
  project); omit the code if there is no ticket. PR **draft**, `## Sentry issue`
  section, standard Co-Authored-By trailer.

---

## 4. Working in a sibling repo

1. **Clone**: if the sibling path is missing, offer
   `gh repo clone dhis2/<name> ../<name>` and stop if declined.
2. **Canonical remote** = the remote whose URL matches `github.com[:/]dhis2/<name>`
   in `git -C ../<name> remote -v`. Never assume `origin` or the first listed —
   clones may have fork remotes configured before it.
3. **Never touch the user's checkout** — sibling clones are live workspaces
   (arbitrary branch, possibly dirty). Isolate all work in a worktree:
   ```bash
   git -C ../<name> fetch <remote> <base-branch>
   grep -qx ".claude/worktrees/" ../<name>/.git/info/exclude 2>/dev/null || \
     echo ".claude/worktrees/" >> ../<name>/.git/info/exclude
   git -C ../<name> worktree add .claude/worktrees/sentry-<short-id> \
     -b fix/sentry-<short-id> <remote>/<base-branch>
   ```
   All edits, builds, commits, and pushes happen inside
   `../<name>/.claude/worktrees/sentry-<short-id>`.
4. **Android SDK location**: if a Gradle build inside the worktree cannot locate
   the Android SDK, copy `local.properties` from the sibling clone root into the
   worktree (it is untracked, so worktrees do not inherit it).
5. **Cleanup**: keep the worktree after opening the PR (review iteration) and
   print the removal command:
   `git -C ../<name> worktree remove .claude/worktrees/sentry-<short-id>`.
   At skill start, remove leftover `sentry-*` worktrees whose branch's PR is
   merged or closed (`gh pr view <branch> --repo dhis2/<name> --json state`).
6. **Push rights**: `gh api repos/dhis2/<name> --jq .permissions.push` —
   `true` → push the canonical remote, then
   `gh pr create --repo dhis2/<name> --base <base-branch> --draft …`;
   `false` → `gh repo fork --remote`, push the fork, add `--head <login>:<branch>`.
7. First Bash commands in sibling directories may trigger one-time permission
   prompts — expected; file tools are pre-authorized via
   `permissions.additionalDirectories` in `.claude/settings.json`.

---

## 5. Version skew — diagnose at what shipped, fix on the base branch

The app's working branch is ahead of production; the library code in a Sentry
trace is the version **pinned by the crashing release**, not the library's base
branch.

1. Pinned versions:
   `git show <app-release-tag>:gradle/libs.versions.toml | grep -E "dhis2sdk|designSystem"`.
2. Diagnosis ref in the library clone — first match wins:
   exact tag (`X.Y.Z` or `vX.Y.Z`) → `release/X.Y.Z*` branch → the version-bump
   commit on the release line → `<remote>/<base-branch>` plus an explicit warning
   that the shipped code could not be pinned.
3. Timestamped SNAPSHOT (e.g. `1.15.0-20260709.080146-51`): parse the timestamp →
   `git -C ../<name> rev-list -1 --before="2026-07-09 08:01" <remote>/develop`.
4. Read files at the ref without checking anything out:
   `git -C ../<name> show <ref>:<path>`.
5. **Already fixed upstream?** Before implementing, compare the crash site at the
   diagnosis ref against the PR base branch. If the bug is already fixed there,
   do NOT open a PR — recommend bumping the version key in
   `gradle/libs.versions.toml` instead, naming the fixing commit/PR if findable.

---

## 6. Sentry ↔ GitHub linking

`Fixes <SHORT-ID>` in a PR body auto-links only if that GitHub repo is connected
in the Sentry org's GitHub integration (the app repo is; the library repos may
not be). Include it anyway, plus the full issue URL — the URL always works.
After opening a library PR, optionally note the PR URL on the Sentry issue via
the MCP update tool; failure is non-fatal.

---

## 7. Delivery note for library fixes

A library fix reaches users only after: library release → app bumps the version
key in `gradle/libs.versions.toml` → app release. Every library-fix report must
state this as a "Ships via" line and, for severe crashes, consider proposing a
defensive guard in the app as a companion PR so already-shipped versions stop
crashing sooner.
