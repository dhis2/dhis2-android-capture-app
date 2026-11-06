---
name: sentry-triage
description: >
  Fetches unresolved Sentry issues for the latest production release,
  attributes each issue to its owning repo (capture app, DHIS2 Android SDK,
  or mobile design system), scores it on Impact (1-5) and Effort (1-5), and
  outputs a prioritized impact/effort quadrant report with ready-to-run
  /sentry-fix commands. Then offers to create the Jira Bugs for the chosen
  quadrants (ANDROAPP / ANDROSDK) and link them to their Sentry issues — no
  PRs. Use when you want to decide what to fix next.
---

# Sentry Triage

## Flow at a glance

1. Setup — plugin, org, project (`references/sentry-setup.md`)
2. Resolve the production release and the library versions it shipped
3. Fetch the top 10 unresolved issues for that release
4. Read each issue's latest event
5. Attribute each issue to its owning repo and read the crash-site code
6. Score Impact and Effort, place in a quadrant
7. Look up existing Jira tickets, then output the report
8. Offer to create Jira tickets for the chosen issues

References: `references/repo-map.md` (attribution, per-repo facts) and
`references/jira-ticket.md` (ticket lookup, creation, Sentry link).

---

## 1. Setup

Follow `references/sentry-setup.md` — plugin check, then `ORG_SLUG`,
`REGION_URL`, `PROJECT_SLUG`.

## 2. Production release

Never read `gradle/libs.versions.toml` on the working branch — it is ahead of
what shipped.

1. List the project's releases (`search_sentry_tools("list releases")`),
   environment `production`, newest first → `PROD_VERSION` (full string, e.g.
   `com.dhis2@3.4.1+156`).
2. No result → newest non-prerelease from `gh release list --limit 5`, leading
   `v` stripped.

Then pin the libraries that release shipped, from its git tag (drop any
`+<build>`):

```bash
git show <tag>:gradle/libs.versions.toml | grep -E "dhis2sdk|designSystem"
```

→ `SDK_VERSION`, `DESIGN_SYSTEM_VERSION` (report header, and the diagnosis ref
for library code — repo-map §5).

## 3. Top unresolved issues

`search_issues` with `organizationSlug`, `regionUrl`, `projectSlug`, `limit: 10`,
`sort: "user"` (singular), and
`query: "is:unresolved release:<PROD_VERSION> !is:ignored"`.

Zero results → try the `<versionName>+<versionCode>` form seen in the release
list; still zero → drop the `release:` filter and say so in the report.

**Then fetch the two numbers Impact scoring needs.** Do NOT score from the issue
detail's `Users Impacted`: it is a **lifetime, all-release** total, so comparing
it to one release's user base mixes two denominators and inflates every share
(observed: 79TN reported 1,171 lifetime users but only 353 on 3.4.2 — 3.3×).
Both come from `search_events` with `dataset: "errors"`:

```
# per issue, release-scoped
query:  environment:production release:<PROD_VERSION>
fields: ["issue", "count_unique(user)", "count()"]
sort:   -count_unique(user)

# the denominator: everyone Sentry saw on this release
query:  environment:production release:<PROD_VERSION>
fields: ["release", "count_unique(user)", "count()"]
```

Store the denominator as `RELEASE_USERS`. Impact is scored on
`REACH = issue_users / RELEASE_USERS`; both figures go in the Scoring Detail so
the share can be checked.

## 4. Latest event per issue

`get_sentry_resource` on each short ID (add `resourceType: "breadcrumbs"` for
the trail). Extract the frames of the innermost exception, the last 10
breadcrumbs, `user` (note if absent), and the `release` / `environment` /
`screen` tags.

If the issue looks like several bugs grouped together, fetch up to 3 more
events (`search_sentry_tools("issue events")`); differing top frames → say so
in the Scoring Detail.

## 5. Attribute and read code

Apply repo-map §1 (classification) and §2 (heuristic) → **Owner**,
**Thrown in**, **Confidence**, one-clause reason. Low confidence → add "verify
ownership during fix".

Read the top 3–5 owned files from the crash site upward:
- **App** — module table in repo-map §3.
- **Library** — `git -C ../<repo> show <diagnosis-ref>:<path>` (repo-map §5).
  Sibling clone missing → attribute by package only and say so; never clone
  during triage.

Skip frames whose file is `SourceFile:N`.

## 6. Score and classify

### Impact (1–5) — highest matching row, then modifiers

Scored on **share of the release's users** (`REACH`, Step 3), not an absolute
count. Crash = unhandled exception or ANR.

| Score | Criteria |
|---|---|
| 5 | Crash, REACH ≥ 5% |
| 4 | Crash, REACH 3–5% |
| 3 | Crash, REACH 1.5–3% — or non-crash degradation (wrong data, feature disabled, blank screen), REACH ≥ 5% |
| 2 | Crash, REACH 0.5–1.5% — or non-crash, REACH 1.5–5% |
| 1 | Crash, REACH < 0.5% — or non-crash < 1.5% — or cosmetic |

**Why shares.** The old absolute scale (≥ 100 users = 5) saturated: with
7,000–19,000 users per production release every top-10 issue scored 5, and the
quadrants collapsed to an effort split. The bands are calibrated to 3.4.2
(8 Sep 2026, 7,104 release users, top 25 issues at REACH 1.7%–7.1%). **Re-check
them if the install base changes by an order of magnitude** — if scores cluster
on one value again, the bands are stale.

Modifiers (cap 5): **+1** crash site in login or sync
(`org.dhis2.usescases.login`, `org.dhis2.mobile.login`, `org.dhis2.mobile.sync`,
`org.dhis2.usescases.sync`, `org.dhis2.utils.granularsync` — granular sync is the
sync flow to users, and has repeatedly supplied several top issues at once) ·
**+1** in data entry / enrollment / forms
(`org.dhis2.form`, `org.dhis2.usescases.eventsWithoutRegistration`,
`org.dhis2.usescases.enrollment`) · **+1** events/users > 5.

### Effort (1–5) — highest matching row, then modifiers

Library bugs are fixed in their own repo, so score them like app code.

| Score | Criteria |
|---|---|
| 5 | Spans two repos, breaks public SDK API (`:core:apiCheck`), or needs a new UseCase + Repository + ViewModel |
| 4 | 3–4 files, `androidMain`-only with no `commonMain` path, or RxJava that would need migrating |
| 3 | 2 files, mixed source sets, or a new UseCase only |
| 2 | 1–2 files with a known pattern (null guard, default value, missing catch) |
| 1 | Single-line fix |

Modifiers (cap 5): **+1** owned stack depth > 10 frames · **+1** library-owned
(ships only via a library release — repo-map §7).

### Quadrant

| | Effort ≤ 2 | Effort ≥ 3 |
|---|---|---|
| **Impact ≥ 4** | Q1 — Fix ASAP | Q2 — Plan carefully |
| **Impact ≤ 3** | Q3 — Quick wins | Q4 — Defer |

**Sanity-check the spread.** If every issue lands in one or two quadrants, or
one axis takes a single value across all issues, the scale is not
discriminating: say so at the top of the report and give the distribution,
rather than presenting the quadrants as a ranking.

**Collapse duplicates.** Issues sharing a crash site or root cause (two ANRs in
the same blocking call, several `!!` in the same dialog builder) are one fix.
Score them individually, but say which ride along with which — five tickets
that are one change is the most common way this report misleads.

## 7. Jira lookup and report

For every issue that could get a ticket (not attribute-only), run the lookup in
`references/jira-ticket.md` §1 — all searches in one parallel batch. It is
read-only; record the key and status, or "none".

```
## Sentry Triage Report — <PROJECT_SLUG>
Production release: <PROD_VERSION> (SDK <SDK_VERSION>, design system <DESIGN_SYSTEM_VERSION>)
Generated: <date>   [release filter relaxed: <why>]

### Q1: Fix ASAP
| Issue | Title | Impact | Effort | Owner | Crash site | Jira | To fix |
|---|---|---|---|---|---|---|---|
| SENTRY-X | … | 5 | 1 | app | Foo.kt:42 | ANDROAPP-123 (Open) | `/sentry-fix SENTRY-X` |
| SENTRY-Y | … | 4 | 2 | SDK | Bar.kt:99 | — | `/sentry-fix SENTRY-Y --repo dhis2/dhis2-android-sdk` |

### Q2: Plan carefully …   ### Q3: Quick wins …   ### Q4: Defer …

### Scoring detail
#### SENTRY-X — <title>
- **Impact** X/5 — <why> · **Effort** X/5 — <why>
- **Owner** <repo> (thrown in <repo>; confidence <level> — <reason>)
- **Crash site** `File.kt:N` · **Flow** <login | sync | data-entry | tracker | dashboard | settings | other>
- **Users** <release-scoped n> of <RELEASE_USERS> (REACH <x.x>%) · **Events** <release-scoped n> (ratio <events/users>)
- **Lifetime users** <all-release total, when it differs materially — it usually does>
- **Root cause hint** <one sentence from the crash-site code>
- **Library only**: shipped version, and the Ships via line (repo-map §7)
```

Attribute-only owners get "manual upstream fix" instead of a `/sentry-fix`
command. No issues even after relaxing the filter → name the org and project
used so the user can check them.

## 8. Offer Jira tickets

Ask with AskUserQuestion which issues should get a ticket now — no PRs are
opened:
- **Q1 only** (recommended)
- **Q1 + Q2**
- **Let me choose** — then ask for the issue IDs
- **No tickets**

For the chosen issues, apply `references/jira-ticket.md` in stages, sending each
stage's calls in parallel in one message: the §1 result is already known
(reuse open tickets, handle Done/linked cases as it says) → §3 create the
missing ones → §4 link every ticket to its Sentry issue. Do not claim tickets
(§2): they stay **Open and unassigned** so whoever picks one up runs
`/sentry-fix <JIRA_KEY>` and owns the PR.

Finish with one line per issue: `SENTRY-X → ANDROAPP-123 (new | existing |
regression of ANDROAPP-99) · Sentry link: linked | failed (<reason>)`.
