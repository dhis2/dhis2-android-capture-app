# Metrics reference

Definitions, queries and traps for the `team-metrics` skill. The workflow lives in `SKILL.md`;
this file is what you need to interpret a number correctly.



## The three time metrics

The journey splits into three additive parts, bounded **positionally** by changelog milestones rather
than by labelling each status pre- or post-commitment (work bounces back to `Needs Update` and
`Needs info` mid-flow, which a static split misattributes):

| Metric | From | To |
|---|---|---|
| Intake | created | first entry to `Ready to Start` |
| Delivery | first entry to `Ready to Start` | merged |
| Post-merge tail | merged | resolved |

**Merge marker** = first entry to `Ready for Integration Testing`. Verified: `Automation for Jira`
drives 69 of 84 such transitions and 79 of them come from `Ready to Merge`. Fallback is
`In Integration Testing`, then the terminal transition. 93% of completed items carry both boundary
markers.

Flow efficiency is active ÷ (active + waiting) **inside the delivery window only**. The whole-life
figure is also computed but mostly reflects backlog dwell, so quote the delivery one.


## Stage taxonomy — classify every status, or lose time silently

The project defines **38 statuses**, not the 14 on the board. `ACTIVE` / `WAITING` / `TERMINAL` in
`scripts/metrics/metrics.py` must cover all of them, and the script raises a loud warning listing any
status seen in a changelog that is not classified.

This is not theoretical. Unclassified statuses had their durations dropped entirely, which produced
two wrong numbers in the first draft:

- **`To Do` (capital D) is a separate legacy status from `To do`** and held **58% of all tracked time**
  in the previous window at ~447 days per item. Invisible until classified.
- **`Open` holds ~139 backlog items** (median age 704 days) and was omitted, so the backlog read 218
  instead of 358.

Names are **case-sensitive**. Verify against
`GET /rest/api/3/project/ANDROAPP/statuses` rather than typing them from memory — `Needs Update`,
`Waiting for Pixel Perfect`, `In Pixel Perfect` and `Manual` all differ in case from the obvious guess.
Watch for near-duplicates: `To do`/`To Do`, `Needs Update`/`NEEDS_UPDATE`.

## Issue types — allow-list, not deny-list

Flow metrics cover **Feature, Task and Bug only** — the types the team actually works in. Use an
allow-list (`issuetype in (Feature, Task, Bug)`); a deny-list silently readmits retired types.

What is excluded and why:

| Type | Why excluded |
|---|---|
| `Story` | **Retired type.** The only items still carrying it are 9 legacy issues parked in `In Progress` since 2024, each with a single status change in its entire history. Including them inflated active WIP from 3 to 12 and produced a phantom "aging WIP" alarm. Excluding `Story` removes the problem at source — no special-case orphan rule needed. |
| `Epic` | Containers, not work items. Summarised separately. |
| `Test` | Zephyr cases sitting in an `AUTOMATED_TEST` status that counts as `statusCategory = Done` but carries no resolution or `resolutiondate`. Inflates throughput. |
| `Sub-task` | Not used in the current workflow; appeared only in older windows. |

Run `metrics.py --census` before trusting a window: it lists every type actually present in the
resolved and in-flight sets, so exclusions stay deliberate rather than accidental.

```
# delivered, with changelogs (flow metrics)
project = ANDROAPP AND issuetype in (Feature, Task, Bug) AND resolutiondate >= -90d

# preceding window, for the period-over-period comparison (changelogs too)
project = ANDROAPP AND issuetype in (Feature, Task, Bug)
  AND resolutiondate >= -180d AND resolutiondate < -90d

# in flight
project = ANDROAPP AND issuetype in (Feature, Task, Bug) AND status in (
  "In Analysis","In Progress","In Review","Testing","In Integration Testing",
  "Ready to Start","Waiting for Testing","Ready to Merge","Ready for Integration Testing")

# backlog — Open MUST be included; omitting it undercounted by 139 items
project = ANDROAPP AND issuetype in (Feature, Task, Bug)
  AND status in ("To do","Open","Waiting for analysis","Prioritization")

# open bugs
project = ANDROAPP AND issuetype = Bug AND statusCategory != Done

# epics — summarised separately, never mixed into flow metrics
project = ANDROAPP AND issuetype = Epic AND statusCategory != Done
```

Epics get their own block (open count, closed in period, age p50/p85, oldest, breakdown by status)
because the team does not manage work at that level — 49 open, median age 20 months, 1 closed per
quarter — so including them makes any roll-up unrealistic.

## Sentry

Org `dhis2`, project `dhis2-android-capture`, region `https://us.sentry.io`.

- Top issues: `search_issues` with `is:unresolved environment:production`, `sort=freq`, `period=90d`
- Per-release load: `search_events` dataset `errors`, fields `release`, `count()`,
  `count_unique(user)` — **report events per user, not raw counts**, because install bases differ
- Confirm whether an issue is a regression: add `issue:[ID,...]` and group by `release`. An issue
  present in exactly one release is a regression in that release; one spread across many is
  long-standing.
- New issues per release: `get_release_details` via `execute_sentry_tool`
- **Crash-free rate is unavailable** — session tracking is not enabled in the Android SDK, so
  release health returns no session data. Use users-affected and events-per-user instead.
- Users-affected can overlap between issues, so never sum them.

Aggregate events-per-user was flat across 3.4.x (3.17–3.22) while two new NPEs affecting ~7,000
users appeared. Always check per-issue release scoping — the average hides regressions.

## Other sources

Stage durations come from `GET /rest/api/3/search/jql` with `expand=changelog` — 100 issues per
page, so a full run is ~3 requests. Do **not** fetch changelogs one issue at a time.
Versions: `GET /rest/api/3/project/ANDROAPP/versions`.

```bash
# PRs merged to develop
gh pr list --repo dhis2/dhis2-android-capture-app --state merged --limit 150 --base develop \
  --json number,title,createdAt,mergedAt,additions,deletions,author,reviews

# CI runs
gh api "/repos/dhis2/dhis2-android-capture-app/actions/runs?branch=develop&per_page=100"

# SonarCloud — branch=develop is REQUIRED, the default (main) reports coverage 0
curl "https://sonarcloud.io/api/measures/component?component=dhis2_dhis2-android-capture-app&branch=develop&metricKeys=coverage,tests,ncloc,vulnerabilities,code_smells,sqale_index,duplicated_lines_density"
curl "https://sonarcloud.io/api/qualitygates/project_status?projectKey=dhis2_dhis2-android-capture-app&branch=develop"
```

## Computation rules

- **Percentiles, not averages** (p50/p85). Averages hide the tail where bottlenecks live.
- Sum **every visit** to a status, so re-entries accumulate; stop the clock at first terminal entry.
- Handle issues that **skip stages** — not every item passes through every status.
- No orphan rule is needed: excluding the retired `Story` type removes all nine legacy parked
  issues at source. Keep the allow-list and this stays true.
- Calendar days, not working days.
- Filter bot authors (`dependabot`, `copilot`, `github-actions`, `dhis2-bot`) from PR figures.

## Known data-quality limits — restate these in every report

1. `assignee` is routinely cleared on completion, so per-person throughput is not reportable.
2. `Product Field`/`Product Team` is populated on roughly half of issues only.
3. Priority is set at triage, not creation.
4. Only ~42% of commits carry an `ANDROAPP-` key, so Jira↔git joins are partial.
5. Story points exist but are uniformly zero, and no issue has been in a sprint for 400+ days —
   velocity, burndown and sprint reports are unavailable for this project.
6. Coverage is a **trend only**: `jacoco/jacoco.gradle.kts` excludes broad class categories
   (`*Activity*`, `*Fragment*`, `*View*`, `*Adapter*`…), so the absolute number is not comparable.
7. **Release slip is not recoverable retrospectively** — Jira keeps one mutable `releaseDate` per
   version. Observed live: 3.4.2 moved from 2026-08-05 to 2026-08-10 during the first report run.
   Each run must snapshot version dates so later runs can diff them.

## Baseline — edition 1, 2026-08-11

Window 13 May – 11 Aug 2026 vs preceding 90 days. Feature/Task/Bug only.

| Metric | Previous | Current |
|---|---|---|
| Throughput (done) | 40 | 81 |
| Intake p50 / p85 (created→committed) | 14.1 d / 124.0 d | 8.3 d / 109.9 d |
| Delivery p50 / p85 (committed→merged) | 13.1 d / 30.0 d | 10.9 d / 33.2 d |
| Flow efficiency (delivery window) | 47.5% | 45.0% |
| Flow efficiency (whole life) | 14.7% | 24.1% |
| Post-merge tail p50 | 4.0 d | 3.2 d |
| Lead time p50 / p85 | 57.4 d / 353.7 d | 33.8 d / 190.0 d |
| Closed without a fix | 34 of 74 (46%) | 19 of 100 (19%) |
| Type mix | Bug 28, Task 12 | Bug 45, Task 24, Feature 12 |

Point-in-time:

| Metric | Value |
|---|---|
| Active work / committed queues | 3 / 11 |
| Backlog | 358 (`To do` 196, `Open` 139, `Waiting for analysis` 23) |
| Open bugs | 132 |
| In-flight by type | Feature 10, Bug 2, Task 2 |
| Aging beyond delivery p85 | none |
| Open epics / closed in 90d | 49 / 1 (age p50 609 d, oldest 3,214 d) |
| PR cycle p50 / p85 | 2.9 d / 10.0 d |
| PR review latency p50 | 0.7 d |
| PRs over the 400-line gate | 35 of 121 (29%) |
| CI pass rate on `develop` | **0%** — cause fixed in PR #5033 (open) |
| SonarCloud (develop) | coverage 10.5%, 548 smells, security E, 17 vulnerabilities |
| Sentry 3.4.1 | 3.17 events/user, 508 new issues, 2 NPE regressions (~4,500 / ~3,180 users) |
| Releases | 3.4.2 overdue (due 10 Aug, unreleased) |

Gate values for the stage math: ANDROAPP-7679 = 42.8 d lead, 8.2 d in `In Review`, 15.0 d
`Ready to Start` → merged.

**Drift:** Jira counts move between runs as work progresses. Take every figure in one report from a
single snapshot and re-run all compute steps after any re-fetch rather than mixing.
