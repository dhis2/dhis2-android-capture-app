---
name: sentry-fix
description: >
  Given a Sentry issue ID or the Jira key of a ticket linked to one, fetches
  the full event and stack trace, finds the issue's Jira ticket (or creates
  one) and claims it for the person running the skill, attributes the crash to
  its owning repo (capture app, DHIS2 Android SDK, or mobile design system),
  diagnoses it at the shipped version, writes the failing unit test first,
  implements the fix following that repo's conventions, runs lint/tests, and
  opens a draft PR there. The ticket is linked to the Sentry issue, so Sentry
  resolves it when the ticket reaches Done. Invoke as
  /sentry-fix <SENTRY-ID | ANDROAPP-key | ANDROSDK-key> [--repo <slug>], or
  follow a /sentry-triage report.
---

# Sentry Fix

## Flow at a glance

1. Setup and input — Sentry ID or Jira key
2. Fetch the issue and recent events
3. Find the Jira ticket and claim it
4. Attribute to the owning repo and read the code
5. Diagnose the root cause
6. Plan the fix
7. Write the failing test first
8. Implement the fix
9. Run lint and tests
10. Ticket, branch, commit, draft PR
11. Report

The fix and its PR belong in the repo that owns the bug, not necessarily this
one. References (in `.claude/skills/sentry-triage/references/`):
`repo-map.md` (attribution, sibling repos, per-repo commands and PR rules),
`jira-ticket.md` (ticket lookup, claim, creation, Sentry link),
`sentry-setup.md` (plugin, org, project).

---

## 1. Setup and input

Follow `sentry-setup.md` → `ORG_SLUG`, `REGION_URL`, `PROJECT_SLUG`.

Input is one of:
- **A Sentry short ID** (`DHIS2-ANDROID-CAPTURE-83NS`, or the bare suffix —
  see `sentry-setup.md`).
- **A Jira key** (`ANDROAPP-123`, `ANDROSDK-45`) — read it with `getJiraIssue`
  and take the Sentry short ID from the issue URL in its description. No URL →
  ask for the Sentry ID. The key becomes `JIRA_KEY`.

An optional `--repo <slug>` from a triage report is a hint, not a verdict
(repo-map §2).

## 2. Fetch the issue and events

`get_sentry_resource` on the short ID: title, culprit, first/last seen, users,
events. Then up to 5 recent events (`search_sentry_tools("issue events")`;
unavailable → use the event embedded in the issue). From each take the full
stack trace (all frames), the last 20 breadcrumbs in order, `user`, the
`release` / `environment` / `screen` tags, and `extra`.

Top frames differ across events → the issue groups several bugs: fix the most
common pattern and say what was skipped.

## 3. Find the Jira ticket and claim it

Unless the input was a Jira key, run `jira-ticket.md` §1. Then:

- **Ticket found** (or given as input) → `JIRA_KEY`. Claim it (§2):
  unassigned → assign to you and move to In Progress; someone else's → stop
  and ask.
- **None** → it is created in Step 10, once the diagnosis exists for its
  description.

Doing this first means two people don't fix the same bug, and the branch can
be named after the ticket from the start.

## 4. Attribute and read the code

Apply repo-map §1–§2 to every frame → **owning repo**. If the evidence
disagrees with a `--repo` hint, say so and follow the evidence. Skip
`SourceFile:N` frames, and never move a genuine library bug onto "the first app
frame".

- **Attribute-only lib** (rule engine, expression parser) → go to Step 11:
  diagnosis plus an upstream recommendation, no clone, no PR.
- **App** → map frames with repo-map §3 and read the crash-site file, the 3
  frames above it, and — where they exist — the repository interface, the
  UseCase calling it, and the feature's Koin module.
- **SDK or design system**:
  1. Sibling clone and canonical remote (repo-map §4.1–4.2). Missing → offer
     `gh repo clone`, stop if declined.
  2. Shipped version: the event's `release` → `git show <app-tag>:gradle/libs.versions.toml`
     → diagnosis ref (repo-map §5).
  3. Read files at that ref with `git -C ../<repo> show <ref>:<path>` — never
     check anything out in the user's clone.
  4. **Already fixed upstream** (repo-map §5.5)? Recommend the version bump
     and stop — no PR.
  5. Create the worktree (repo-map §4.3). All later work happens there.

## 5. Diagnose

Find the exact line that throws or creates the bad state, and the missing
precondition (null check, empty collection, bad state transition, scope used
after its lifecycle, unhandled `D2Error`…). Note the source set.

Re-check ownership: the app breaking a documented library precondition is an
app bug even if the throw is in the library; the library mishandling valid
input is a library bug. If it flips, say so and redo Step 4's setup.

State the root cause in **one sentence** before writing code.

## 6. Plan

State which files change and why.

- **App** — `AGENTS.md` holds the architecture and conventions. For a crash fix
  also:
  - the smallest fix in the touched code's existing style — no migrating a
    legacy screen as part of a crash fix;
  - put the decision in a unit-testable layer (ViewModel / UseCase / UiState),
    not an Activity, Fragment or binding — `:app` has no Robolectric;
  - add a defensive guard at the crash site (e.g. early return instead of
    `!!`) so other paths cannot crash there.
- **SDK / design system** — the hard constraints in repo-map §3 (public API
  dump; Paparazzi goldens only via CI).

## 7. Failing test first

Write the test that reproduces the crash **before** touching production code
and watch it fail — on an assertion, or because the API the fix adds doesn't
compile yet. Can't reproduce it in a unit test → say so and move the decision
into a testable layer (Step 6).

- **App** — load the `android-testing` skill; add to the touched class's
  existing test class and assert the state that was wrong.
- **SDK / design system** — test location and naming in repo-map §3.

## 8. Implement

The smallest change that turns the test green without breaking its
neighbours.

- **App** — follow `AGENTS.md`.
- **Library** — the app's conventions do not apply: follow that repo's own
  guidance (repo-map §3) and match the surrounding code.
- **Never narrate the fix in source**: no Sentry IDs, no before/after story.
  That belongs in the PR and ticket. A comment, if needed, is one line saying
  what the code does.

## 9. Lint and tests

In the owning repo (the worktree for libraries); fix every failure before
moving on, and never skip or comment out a test.

- **App** — `./gradlew ktlintFormat ktlintCheck`, then the affected module's
  test task from `AGENTS.md` (`:app` is flavored:
  `:app:testDhis2DebugUnitTest --tests "<package>.*"`).
- **SDK / design system** — the Verify block in repo-map §3.

## 10. Ticket, branch, commit, draft PR

Once Step 9 passes, do all of this without pausing — the draft PR is the review
point.

**Ticket.** No `JIRA_KEY` yet → create one (`jira-ticket.md` §3) with the full
description including the manual test, then claim it (§2). Existing ticket →
replace its manual-test placeholder or append the section (§5). Then link it to
the Sentry issue (§4) — this is what auto-resolves Sentry on Done.

**PR body** (every repo) contains:

```
## Sentry issue
Fixes <SENTRY-SHORT-ID>
https://<ORG_SLUG>.sentry.io/issues/<SENTRY-SHORT-ID>/
Related task: [<JIRA_KEY>](https://<site>/browse/<JIRA_KEY>)
```

(`Fixes` only links where the GitHub integration is connected — repo-map §6;
drop `Related task` when there is no ticket.)

### App (this repo)

Branch **from the branch the skill was triggered on** and target it with the
PR — never `main` / `develop` unless told. Branch, title and size rules:
repo-map §3. Name the branch correctly before pushing: pushed branches can't be
renamed.

```bash
BASE_BRANCH=$(git rev-parse --abbrev-ref HEAD)
BRANCH="<JIRA_KEY>"   # fix/sentry-<short-id> only when there is no ticket
git checkout -b "$BRANCH" "$BASE_BRANCH"
git add <files>
git commit -m "fix: [<JIRA_KEY>] <short description>"   # + harness Co-Authored-By trailer
git push -u origin "$BRANCH"
gh pr create --draft --base "$BASE_BRANCH" --head "$BRANCH" \
  --title "fix: [<JIRA_KEY>] <short description>" --body "..."
```

### SDK or design system (in the worktree)

- Base `develop`, never `master`/`main`.
- SDK ticket created only now: rename the still-unpushed worktree branch to it
  (`git -C <worktree> branch -m <JIRA_KEY>`). Design-system branches stay
  `fix/sentry-<short-id>`.
- Commit trailers, push rights / fork fallback, and title/body style: repo-map
  §3 and §4.6. (SDK commits carry **no** Co-Authored-By and no 🤖 footer.)
- `gh pr create --repo dhis2/<name> --base develop --draft --title "…" --body "…"`
- Keep the worktree for review and print its removal command (repo-map §4.5).

Finally comment the PR URL on the ticket (`jira-ticket.md` §5).

## 11. Report

```
## Fix Summary — <SENTRY-SHORT-ID>
**Root cause**: <one sentence>
**Owner**: <repo> (<as triaged | overridden from --repo because …>)
**Fix**: <what changed and why, 2–3 sentences>
**Files changed**: `path/File.kt` — <what> · `path/FileTest.kt` — <tests>
**Lint / Tests**: passed (<test class>::<method>, …)
**Jira**: <JIRA_KEY> (<reused | created | regression of KEY>) · Sentry link: <linked | failed (<reason>)> — or "no ticket"
**PR**: <url>
**Ships via**: <library only — repo-map §7>
```

No safe fix (attribute-only owner, unclear root cause) → say so and recommend
one of: a diagnosis for a manual upstream fix, a defensive guard in the app, or
a Sentry breadcrumb to improve the next diagnosis.
