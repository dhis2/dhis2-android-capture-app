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

## Epics

Epics get their own one-row summary (open count, closed in period, age p50/p85, oldest, breakdown
by status) because the team does not manage work at that level — 49 open, median age 21 months,
1 closed per quarter — so mixing them into flow makes any roll-up unrealistic.

**Do not list them on the page.** The pile needs cleaning up, but "which epics are stale" is a Jira
query anyone can run, and a 27-row table buries a report whose whole value is being readable in a
meeting. The count and its movement between editions are the signal; a cleanup drive belongs in
**Recommendations**, not in a table. An epic's own status carries almost nothing anyway — 33 of the 49 sit
in `Pending`.

```
project = ANDROAPP AND issuetype = Epic AND statusCategory != Done
project = ANDROAPP AND issuetype = Epic AND resolutiondate >= "<w1>" AND resolutiondate <= "<w2>"
```

## Needs info

`Needs info` parks an item that cannot proceed until someone answers a question. It is a
`WAITING` status in the flow numbers like every other queue — the stage chart already carries
its share of tracked time — and the review answers one narrower question: **do the items get
the information and move on, or do they get quietly closed?**

Measured over issues that **entered** in the window, not ones that left in it. Bucketing on
entry is what keeps the two windows comparable: an item still waiting has no exit date, and
bucketing on exit would flatter the resolution rate by counting only the ones that got out.

```
project = ANDROAPP AND issuetype in (Feature, Task, Bug)
  AND (status WAS "Needs info" AFTER "<w0>" OR status = "Needs info")
```

`status WAS ... AFTER` reads the changelog server-side and works anonymously; the `OR` picks up
long-parked items whose entry predates the window (one has been there since 2023).

**Outcome buckets**, one per issue, in report order:

| Bucket | Rule |
|---|---|
| `moved forward and done` | resolution is `Done` |
| `moved forward, still open` | unresolved, but reached `Ready to Start`, a merge marker or an active status *after* its last stay |
| `closed without a fix` | terminal on any resolution but `Done` |
| `still in Needs info` | current status is still the parked one |
| `never moved on` | left the status but never reached commitment or an active status |

**Leaving the status is not progress.** Every observed exit lands in `To do` or `Closed` and
nowhere else, so an exit means somebody cleared the flag — the item returns to the backlog to
be re-prioritised and pays intake a second time, which lead time cannot see. `progressed_after`
exists precisely so the forward buckets cannot be satisfied by an exit alone.

Three traps:

- **The current window is not settled.** Its issues have had less time to progress or to be
  closed, so "still open" is inflated and both terminal buckets are deflated relative to the
  previous window. A *rise* in the moved-forward share despite that bias is a safe claim; a
  fall is not.
- **Roughly 60% of stays last minutes** — the status is set and immediately reverted, which
  drags the raw dwell median to 0.0 d. Report percentiles over stays of **≥ 1 day** plus the
  `same-day flips` count, which is itself evidence the status doubles as a marker.
- **Deleted issues are invisible.** Jira drops them from the API entirely, so an item purged
  after a stay here leaves no trace and no gap to notice. The outcome mix covers what still
  exists; say so rather than presenting it as exhaustive.

`NEEDS_INFO` matches the exact string `"Needs info"`. A rename or second spelling would
silently empty the section, so the run scans changelogs for case variants and warns. Related
but **distinct** statuses that must not be folded in: `Needs Update` / `NEEDS_UPDATE` (rework
after review), `Needs Product`, `NEEDS DESIGN`.

## No work-in-progress section

WIP is a "now" number the dailies already own, and a monthly report reprinting it invites decisions
about today's board from data up to a month stale. `metrics.py` still prints it and keeps it in
`metrics.json` — use it to reconcile the run, not to fill a section. Its two genuinely monthly
counts, **open bugs** and **backlog depth**, belong in Quality.

## Bugs fixed per patch release

Replaces the completed-work type mix in **Quality**. The mix (Bug / Task / Feature in a window)
moves with what the release was for, not with quality, so comparing it period over period invites
a conclusion it cannot support. Patch releases exist to fix bugs, so their bug count means the
same thing every time.

**Patch = bug-fix release**: third number moves (`3.4.1`), or a fourth is appended as a hotfix
(`3.4.0.1`). `X.Y.0` is a feature release, excluded. Selection is the last three **shipped**
patches by release date plus the **next** unshipped one (earliest due after the last release),
flagged. Order by `releaseDate`, never by name — names do not sort numerically and the project
ships out of order.

**Release-scoped, not window-scoped.** Like the Sentry triage and unlike everything else on the
page. A patch in the table can predate the window entirely; say so rather than letting a reader
assume the rows and the trend table cover the same period.

```
GET /rest/api/3/project/ANDROAPP/versions
project = ANDROAPP AND issuetype = Bug AND fixVersion in (<selected patches>)
```

Two traps, both of which make the number read as something it is not:

- **`fixVersion` is a target field, not a shipped-in stamp.** It is set when work is planned for a
  release and is never cleared when the work slips. 3.2.1 shipped in Jul 2025 and still carries 48
  open bugs; 3.3.1 shipped in Jan 2026 and carries 16. That is what the `still open` column is —
  stale tags, not release content. Never fold it into the fixed count; report it as hygiene.
- **The table is a floor.** Only bugs carrying a `fixVersion` appear at all, and coverage has run
  as low as ~57% historically (24 of 24 in the Sep 2026 window). The script prints the ratio;
  quote it, and treat a drop in it as under-counting rather than as fewer fixes.

And one interpretation trap: **a low count on an older patch is usually throughput, not quality.**
3.3.1 genuinely shipped with one tracked bug fix — bug throughput then was ~11 Done per half-year
against ~67 in H1 2026. Check the era's throughput before calling a row a regression.

## Sentry

Org `dhis2`, project `dhis2-android-capture`, region `https://us.sentry.io`.

**The issue list comes from the `sentry-triage` skill, not from queries here.** It scores
Impact and Effort and returns quadrants, which is what the stability section needs; copy its
numbers verbatim so the page and the triage cannot drift apart. What follows is the
supporting data triage does not produce, plus the traps.

**Scope: triage is release-scoped, the report is window-scoped.** Triage covers the latest
production release; every other figure in the report covers 90 days across all releases. Say
so on the page. An issue can top one and be absent from the other, and that is not a
contradiction — in Sep 2026, 89RF and 89RY led the 90-day view on volume accumulated under
3.4.1 while being entirely absent from 3.4.2, which is what proved the rollout fixed them.

**Impact was rescaled in Sep 2026 and the reason matters here too.** It was an absolute
count (≥100 users = 5) on an app with 7,000–19,000 users per release, so every top issue
scored 5 and the quadrants could not discriminate. It is now share-of-release-users
(`REACH`). If a future edition shows every issue at one impact score, the bands are stale
against the install base — fix the scale rather than publishing a ranking that does not rank.

- Legacy top-issue query, still useful for the 90-day view: `search_issues` with
  `is:unresolved environment:production`, `sort=freq`, `period=90d`
- **Crash load by release** = events ÷ users, per release, over the window. Query:
  `search_events` dataset `errors`, fields `release`, `count()`, `count_unique(user)` —
  **report the rate, not raw counts**, because install bases and time in the field differ, and
  raw counts are not comparable across releases where the rate is. Three things it is not:
  it is **not the crash-free-user rate** (one user stuck in a crash loop inflates it without
  many people being affected — that is why the user column stays beside it); it is not a
  per-issue figure; and a fresh release's rate is **unstable through its first week or two of
  adoption**, so read the column vertically as a trend and treat a barely-adopted release as
  provisional. Heading it "Load per release" confused readers and is not to be used again
- Confirm whether an issue is a regression: add `issue:[ID,...]` and group by `release`. An issue
  present in exactly one release is a regression in that release; one spread across many is
  long-standing.
- New issues per release: `get_release_details` via `execute_sentry_tool`
- **Crash-free rate is unavailable** — session tracking is not enabled in the Android SDK, so
  release health returns no session data. Use users-affected and events-per-user instead.
- Users-affected can overlap between issues, so never sum them.

Aggregate events-per-user was flat across 3.4.x (3.17–3.22) while two new NPEs affecting ~7,000
users appeared. Always check per-issue release scoping — the average hides regressions.

**Never score from an issue page's `Users Impacted`.** That is a lifetime, all-release total;
against a single release's denominator it overstates reach badly (79TN: 1,171 lifetime vs 353
on 3.4.2, a 3.3× overstatement). Release-scoped counts come from `search_events` grouped by
`issue` with a `release:` filter — the same call that gives the denominator.

**Collapse duplicates before recommending work.** Triage scores issue-by-issue, so one
architectural fault appears as several rows. Sep 2026: seven of the top ten were the same
blocking SDK call on the main thread (`LocaleSelector`, `ThemeManager`, `SMSSyncProvider`,
`D2.programs()`, `DashboardRepositoryImpl` — all `blockingGet`/`blockingFirst`), and three
crashes were the same `!!`-on-null in the sync-dialog builders. Ten tickets, two fixes.

## Other sources

Stage durations come from `GET /rest/api/3/search/jql` with `expand=changelog` — 100 issues per
page, so a full run is ~3 requests. Do **not** fetch changelogs one issue at a time.
Versions: `GET /rest/api/3/project/ANDROAPP/versions`.

```bash
# PRs merged to develop
gh pr list --repo dhis2/dhis2-android-capture-app --state merged --limit 400 --base develop \
  --json number,title,createdAt,mergedAt,additions,deletions,author,reviews

# CI runs
gh api "/repos/dhis2/dhis2-android-capture-app/actions/runs?branch=develop&per_page=100"

# SonarCloud — branch=develop is REQUIRED, the default (main) reports coverage 0
curl "https://sonarcloud.io/api/measures/component?component=dhis2_dhis2-android-capture-app&branch=develop&metricKeys=coverage,tests,ncloc,vulnerabilities,code_smells,sqale_index,duplicated_lines_density"
curl "https://sonarcloud.io/api/qualitygates/project_status?projectKey=dhis2_dhis2-android-capture-app&branch=develop"
```

## Charts

`scripts/metrics/charts.py` draws four, and only four. The test for charting something is
whether it has **internal structure** — a composition, a distribution, a trend over many
points. Everything else in this report is one number now against one number then, and a
two-bar chart of that is strictly worse than the sentence: same information, more pixels,
slower to read.

| File | Form | Why it earns a chart |
|---|---|---|
| `01-journey` | stacked bar ×2 | intake + delivery + post-merge is a composition, and the two windows show whether the *shape* changed, not just the total |
| `02-where-time-goes` | grouped bars | eight stages × two windows; the point is which stages dominate, which no table conveys at a glance |
| `03-sonarcloud-trend` | small multiples | ~20 real monthly measurements, four metrics on four scales |
| `04-sentry-issues` | quadrant scatter | two continuous axes plus a categorical quadrant — a genuine 2D decision space |

Rules the script already encodes — do not undo them by hand:

- **Never two y-axes on one plot.** The four SonarCloud metrics get four panels. A shared axis
  invents a correlation that is not in the data.
- **Colour is never the only channel.** Every bar carries a value label, and every point in
  the quadrant scatter is labelled with its issue id, reach and quadrant in words. Required:
  the palette's aqua sits below 3:1 on the light surface.
- **Chart 4 plots reach against effort, not impact against effort.** Impact is derived from
  reach and then capped at 5, so several issues share the top score and an Impact axis
  collapses to a vertical line — a scatter that cannot separate its own points. Reach is the
  continuous quantity underneath. The quadrant, which *is* computed from impact, is carried
  by colour and written by every point.
- **Never jitter the effort axis.** A nudged point drifts across the cheap/costly divider,
  which is the one thing the divider exists to show. Nudge the label and draw a leader line.
- **Palette is fixed** and validated for colour-blindness in both light and dark
  (`#2a78d6` / `#eb6834` / `#1baf7a`, status red `#d03b3b`). Colours are emitted as
  `var(--role, #fallback)` so the SVG can be re-themed without editing the shapes.
- Bars are rounded on the data end only, separated by a 2px surface gap, never a border.

**Not charted, deliberately:** throughput, coverage, lead-time percentiles, flow efficiency,
WIP, epics, releases, and the Needs info review — its outcome mix is a handful of counts that a
table states more precisely than a stacked bar. **PR size against the 400-line gate** is no longer excluded for scope reasons — `--limit 150`
truncated the previous window at 18 Mar and undercounted it (25 PRs against a true 35), and
the limit is now 400, which covers both windows. It stays uncharted only because it is a
two-point comparison like the rest. **Check the oldest PR returned against the window start
every run**; if the limit ever truncates again, the comparison is silently wrong.

### Attaching them to Confluence

```bash
python3 scripts/metrics/attach_charts.py <pageId>            # add or replace all four
python3 scripts/metrics/attach_charts.py <pageId> --dry-run   # check auth without writing
```

Do not hand-roll the `curl`. The transport depends on which token type is configured, and
getting it wrong produces a 401 that looks like a permissions problem and is not:

| Token type | Scheme | Host |
|---|---|---|
| Scoped (`Create API token with scopes`) | `Bearer <secret>` | `api.atlassian.com/ex/confluence/<cloudId>/wiki/rest/api` |
| Classic (`Create API token`) | `Basic <email:token>` | `dhis2.atlassian.net/wiki/rest/api` |

**Upload is API v1 only.** Verified 8 Sep 2026: the v2 attachment group
(`/api/v2/pages/{id}/attachments`) offers GET and DELETE and **no create operation**, so the
only upload route is v1 `POST /wiki/rest/api/content/{id}/child/attachment`. A v2-scoped
token reads attachments happily and cannot write one, which produces a `401 scope does not
match` on every write route and looks like a missing write scope when it is not.

Both are ~192 chars and start `ATATT`, so **the two cannot be told apart by looking at the
string** — only by what the auth schemes say about it. `metrics.py:classify_token` does that
probe and caches it; `--preflight` prints the verdict, including the case that matters most:
a valid scoped token whose grant is incomplete, which must be reported as "add a scope", not
"bad token".

Scopes for the scoped path — **no Jira scopes**:

    read:content-details:confluence     write:attachment:confluence

(the single classic equivalent is `write:confluence-file`)

`read:attachment:confluence` is the trap: it is the obvious-sounding name, it grants the v2
reads, and it cannot upload. A token holding only the two attachment scopes fails every
write with `scope does not match` — which reads as "write scope missing" even when the
consent screen plainly lists it. `read:content-details:confluence` is what the v1 create
route actually requires, to resolve the page container.

**Use `PUT` on the collection path.** Three neighbouring routes look interchangeable and are
not — all three were tried against a correctly-scoped token on 8 Sep 2026:

| Route | Result with `read:content-details` + `write:attachment` |
|---|---|
| `PUT /content/{id}/child/attachment` | **works** — creates, or versions a same-named file |
| `POST /content/{id}/child/attachment` | creates, but **400** on a filename that already exists: *"Cannot add a new attachment with same file name"* — so re-runs break |
| `PUT /content/{id}/child/attachment/{attachmentId}/data` | **401 scope does not match** — needs more than these two |
| `POST /api/v2/pages/{id}/attachments` | **401** — v2 has no create operation at all |

So a re-run for the same period versions the four files in place and the attachment count
stays at four. Verified by running twice: v2 → v3, still four attachments.

Other mechanics the script handles: `X-Atlassian-Token: nocheck` (the v1 attachment API
applies an XSRF check that otherwise 403s with no explanation), and **attaching only to a
published page** — the API refuses attachments on a draft, so publish to `current` first.
Note the connector cannot publish an existing draft: `updateConfluencePage` sends the
draft's version number and Confluence demands version 1 for a first publish, so create the
page with `status: current` when it is going to carry charts.

### Referencing an attachment from the body

The connector's HTML+ADF format cannot reference an attachment by filename — it needs the
**Media API fileId**, a UUID that appears only under `expand=extensions` on the v1
attachment listing (the `att…` id from the upload response is *not* it).
`attach_charts.py` prints ready-to-paste figure HTML for exactly this reason.

```html
<figure data-type="media-single" data-layout="center"
        data-width="100" data-width-type="percentage">
  <div data-type="media" data-media-type="file" data-id="<fileId>"
       data-collection="contentId-<pageId>" data-alt="02-where-time-goes.png"></div>
</figure>
```

**Replacing an attachment mints a new fileId.** After a re-attach, every `fileId` from the
previous run is stale, so re-read them (`attach_charts.py` prints them) before authoring a
new body. The already-published page is unaffected: Confluence stores the reference as
`<ri:attachment ri:filename="…">`, which resolves by name to the current version. Only the
authoring step needs fresh ids.

`data-width-type="percentage"` is not optional. Omit it and Confluence reads `data-width` as
**pixels** — the format guide's own `data-width="80"` example then renders an 80-pixel-wide
chart. Confluence rewrites the whole thing to `<ac:image ac:width="680">` +
`<ri:attachment ri:filename="…">` on save, and stamps the real intrinsic dimensions onto the
media node, which is the cheapest confirmation that the reference actually bound to a file.

If no token is configured the charts cannot be attached, and that is a supported outcome:
publish the `expand` tables *expanded*, say so in the report, and do not fall back to typing
into the editor to place images.

## Computation rules

- **Percentiles, not averages** (p50/p85). Averages hide the tail where bottlenecks live.
- Sum **every visit** to a status, so re-entries accumulate; stop the clock at first terminal entry.
- Handle issues that **skip stages** — not every item passes through every status.
- No orphan rule is needed: excluding the retired `Story` type removes all nine legacy parked
  issues at source. Keep the allow-list and this stays true.
- Calendar days, not working days.
- Filter bot authors (`dependabot`, `copilot`, `github-actions`, `dhis2-bot`) from PR figures.

## Auth model

Verified 7 Sep 2026 against `dhis2.atlassian.net`:

| Endpoint | Anonymous | Evidence |
|---|---|---|
| `GET /rest/api/3/search/jql` + `expand=changelog` | **200** | 100/100 issues returned with changelog histories |
| `GET /rest/api/3/issue/{key}?expand=changelog` | **200** | ANDROAPP-7679, 23 histories |
| `GET /rest/api/3/project/ANDROAPP` | **200** | |
| `GET /wiki/rest/api/content/{id}` | **404** `authorized:false` | MOB space is not public |

So flow metrics need no credentials at all, and `metrics.py` sends no `Authorization`
header when no token is configured. Cross-checked against the recorded baseline: the
anonymous count for 13 May – 11 Aug is exactly 100, matching that edition's 81 Done plus
19 closed-without-a-fix, so anonymous access sees the same issue set.

Three consequences to hold on to:

- **Anonymous is a floor, not a certainty.** A permission-restricted issue is invisible and
  drops out of every count silently. A classic token removes the doubt; without one, say
  "anonymous" in the Method section.
- **A scoped token does not authenticate Jira at all.** It is refused by Basic auth, so
  sending it turns a 200 into a 401. `auth()` deliberately returns `None` for one, and
  `metrics.json` still records `anonymous: true` — keyed off `auth()`, not off whether a
  token exists, or the Method section would claim coverage the run did not have.
- **Confluence needs no token.** Reading the previous edition (step 2) and creating or
  updating the page (step 6) both go through the Atlassian connector's per-user OAuth. Only
  chart attachment needs a credential, because the connector has no upload tool.

### Why not native Confluence chart macros

Tested 8 Sep 2026 by authoring them through the connector. The macros round-trip into
storage cleanly, so this looks viable and is not: Confluence Cloud renders the native Chart
macro as **"Chart (Deprecated)"**. Building a monthly report on a deprecated macro means it
works until some Atlassian release where it silently does not, on a page nobody re-reads
until the following month.

The Jira Chart macro (`jirachart`) is worse for this report for a different reason: it
re-queries JQL on every page view, so the picture drifts away from the prose around it as
work moves. That breaks the single-snapshot rule outright. Both are dead ends — keep the
PNG-or-table approach.

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

## Regression fixture — figures from a 2026-09 run

**This is a test fixture, not a previous edition.** The pages it was recorded from have been
deleted, and §2 of the skill is explicit that a page which is not a live child of `1948057602`
does not exist for loop-closing purposes. **Never cite these numbers in a report, never treat
them as a predecessor, and never close the loop against them.**

What they are for: re-running the pipeline over the same window should still reproduce them.
Several did so exactly across two independent runs — the epic counts, the whole patch table,
the entire Needs info block — which makes them a cheap check that a refactor has not changed
the arithmetic. If a figure here moves and the window has not, something in the computation
changed and you should find out what before publishing.

### Recorded figures

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

Bugs fixed per patch release (release-scoped, Sep 2026 run):

| Version | Released | Fixed | Closed, not fixed | Still tagged but open |
|---|---|---|---|---|
| 3.4.2 | due 10 Aug 2026, unreleased | 4 | 0 | 0 |
| 3.4.1 | 29 Jun 2026 | 43 | 6 | 0 |
| 3.4.0.1 | 26 May 2026 | 1 | 0 | 0 |
| 3.3.1 | 19 Jan 2026 | 1 | 5 | 16 |

`fixVersion` coverage in the window: 24 of 24 bugs fixed carry one.

Point-in-time:

| Metric | Value |
|---|---|
| Active work / committed queues | 3 / 11 |
| Backlog | 358 (`To do` 196, `Open` 139, `Waiting for analysis` 23) |
| Open bugs | 132 |
| In-flight by type | Feature 10, Bug 2, Task 2 |
| Aging beyond delivery p85 | none |
| Open epics / closed in 90d | 49 / 1 (age p50 638 d, oldest 3,243 d) |
| Needs info, issues entered in window | 33 (prev 41), from 37 stays, 21 same-day flips, 4 repeat visitors |
| Needs info outcomes (cur / prev) | moved forward and done 12/17 · moved forward still open 5/0 · closed without a fix 8/7 · still in Needs info 3/1 · never moved on 5/16 |
| Needs info moved forward | 17 of 33 = **52%** (prev 17 of 41 = 41%) |
| Needs info closed reasons | Obsolete 4, Cannot Reproduce 3, Invalid 1 |
| Needs info dwell p50 / p85 (≥1 d) | 6.7 d / 37.8 d, max 60 d |
| Sitting in Needs info now | 7 (age p50 135 d, oldest 1,146 d — ANDROAPP-2918, since Jul 2023) |
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
