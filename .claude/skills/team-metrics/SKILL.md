---
name: team-metrics
description: >
  Generate the monthly Android team metrics report and publish it as a Confluence
  page in the MOB space. Pulls flow metrics from Jira ANDROAPP changelogs (intake,
  delivery, post-merge, throughput, where time queues), production stability from
  Sentry, code quality from SonarCloud, and PR/CI health from GitHub; compares the
  rolling 90-day window against the preceding one; draws the four charts that carry the
  report's main findings; and reports back on whether last edition's actions worked. Use
  when asked to produce, refresh or publish the team metrics report, or for a mid-month
  check on flow and bottlenecks.
---

# Android Team Metrics

Produces `Android Metrics — Flow — YYYY-MM-DD` as a child of the
[Android Metrics](https://dhis2.atlassian.net/wiki/spaces/MOB/pages/1948057602/Android+Metrics)
parent page (id `1948057602`) in Confluence space **MOB** (spaceId `82280452`).

**Cadence:** monthly, over a **rolling 90-day window** compared with the preceding 90 days.
The window is wider than the interval on purpose — throughput is ~27 items/month, so monthly
percentiles move on noise rather than signal.

Load `references/metrics-reference.md` for definitions, the status taxonomy, the data-quality
caveats to restate, and the current baseline. Read it before interpreting any number.

---

## 1. Preflight

```bash
python3 scripts/metrics/metrics.py --preflight
```

It reports each source and prints exact remediation for anything missing. Relay that to the
user rather than working around it — a silently skipped source produces a misleading report.

| Source | Needed for | Auth | If missing |
|---|---|---|---|
| Jira | all flow metrics | **none** — ANDROAPP is world-readable over REST, `expand=changelog` included | Only fails if project permissions or the network changed. Blocking; investigate rather than working around |
| Confluence | reading the previous edition, creating and updating the page | **the Atlassian connector** (per-user OAuth, no token) | Confirm the Atlassian tools are in-session; if not, authorize with `/mcp`. **You cannot run OAuth yourself.** Without it, hand the report over as text |
| Charts | attaching the four PNGs — and *only* this | scoped `JIRA_AUTH`, optional: `read:content-details:confluence` + `write:attachment:confluence` | Publish the collapsed tables instead and say so. Nothing else is affected |
| GitHub (`gh auth`) | PR cycle time, review latency, PR size, CI | `gh` login | Skip the Delivery section and say so |
| SonarCloud | code quality trend | none | Skip; no token needed, so failure means network |
| Sentry MCP | production stability | per-user OAuth | Check the Sentry tools are available in-session. If not, tell the user to authorize with `/mcp` — **you cannot run OAuth yourself.** Mark the section unavailable |

**No credential is needed to produce or publish the report.** Jira is world-readable,
and Confluence read/write goes through the Atlassian connector. A token buys exactly two
things: attaching the chart PNGs, and covering permission-restricted Jira issues.

Anonymous Jira sees only public issues, so a restricted issue would drop out of every
count with no error. Say "anonymous" in the Method section when the run was — the numbers
are a floor, not a certainty. Note a *scoped* token does not change this: it cannot read
Jira at all, so those runs are still anonymous and `metrics.json` records them as such.

`--token-help` explains the two token types and which scopes to grant. Never print, echo
or commit the token value.

## 2. Close the loop on the previous edition

**Do this before generating anything.** It is what makes the report a feedback loop rather
than a dashboard.

1. List children of page `1948057602`; take the most recent `— Flow —` page.
2. `getConfluencePage` on it. Task state is in the body as
   `<ac:task-status>incomplete|complete</ac:task-status>`.
3. For each **ticked** action, verify the outcome in the data — a tick is a claim that
   something was done, and the metrics say whether it worked. Report the result either way,
   including when a completed action did not move the number.
4. Carry unticked actions forward and note how many editions they have survived. An action
   alive for three editions is either not real work or has no owner; say so plainly.
5. Open the report with a short **Since last time** section covering the above.

For the first edition there is no predecessor — skip this step and note it.

## 3. Gather

```bash
python3 scripts/metrics/metrics.py --census   # types and statuses actually present
python3 scripts/metrics/metrics.py            # full run, writes metrics.json
```

Run `--census` first and compare against the classification in the script. **If it warns about
an unclassified status, stop and classify it** — unclassified time is dropped silently and
inflates flow efficiency. This has already caused two wrong numbers historically (see the
reference).

The script prints a correctness gate on ANDROAPP-7679. Expected: **42.8 d lead, 8.2 d in
`In Review`, 15.0 d `Ready to Start` → merged**. If it does not match, the stage math is wrong —
do not publish, investigate first.

Then gather the non-Jira sources (commands in the reference): GitHub PRs and CI runs, and
SonarCloud measures and history pinned to `branch=develop`.

**For Sentry, run the `sentry-triage` skill rather than querying issues here.** It resolves
the production release, attributes each issue to its owning repo, scores Impact and Effort,
and returns the impact/effort quadrants — which is what makes the stability section
actionable instead of a leaderboard. Take its scores verbatim into the report and the chart;
do not re-derive them, or the page and the triage will disagree.

Two things the triage does not cover, so still query them here:

- **per-release load** (events per user across releases) — the 90-day trend, in the reference
- **whether a top issue is confined to one release** — what separates a regression from a
  long-standing problem, which aggregate averages hide

Mind the scope difference and state it on the page: triage is scoped to the **latest
production release**, the rest of the report to the **90-day window**. They answer different
questions — "what should we fix now" versus "how did stability move" — and an issue can
legitimately top one and be absent from the other.

## 4. Draw the four charts

```bash
python3 scripts/metrics/charts.py --as-of <window-end YYYY-MM-DD> --release <e.g. 3.4.2> \
  --issue "79TN|NPE — TEI dashboard|DashboardRepositoryImpl.kt:889|353|5.0|5|2|Q1" \
  --issue "7F4F|ANR in LocaleSelector|LocaleSelector.kt:47|506|7.1|5|3|Q2"
```

Flow numbers come from `metrics.json`, SonarCloud history the script fetches itself, and the
Sentry rows from the triage report — one `--issue` per row as
`ID|Title|CrashSite|Users|Reach%|Impact|Effort|Quadrant`, copied across rather than
re-derived. Nothing is written to disk but the charts.

It produces, in `scripts/metrics/charts/`:

| Chart | Replaces | Source |
|---|---|---|
| `01-journey` | the paragraph explaining that intake + delivery + post-merge sum to lead time | `metrics.json` |
| `02-where-time-goes` | the whole shaded stage-share table | `metrics.json` |
| `03-sonarcloud-trend` | the SonarCloud prose line in **Quality** | SonarCloud API |
| `04-sentry-issues` | the top-issues table in **Production stability** | `--issue` args, from `sentry-triage` |

plus `charts/tables.html` — the same numbers as a collapsed Confluence `expand` macro per
chart. **Paste those, never retype the figures**; hand-transcribing is how a chart and its
table drift apart.

`--as-of` is the window end, not today. Passing today's date silently produces charts that
disagree with the page they sit on. If a chart is skipped (SonarCloud unreachable, no
`--issue` given) the script says so — say it in the report too, and keep that section's
existing table rather than leaving a hole.

PNG rasterization needs Chrome (looked up under both Linux binary names and the macOS
`/Applications` paths). Without it the script emits SVG only, and there is nothing to attach —
fall back to the tables as above.

## 5. Compose

Structure, in order. Keep it short enough to read in a meeting; push detail into the collapsed
Method section.

1. **Since last time** — outcomes of ticked actions, carried-forward items
2. **Headline** — two bullet lists: *Going well* / *Needs attention*, ~5 each, no tables
3. **Trend at a glance** — metric, one-line definition, prev, now, change lozenge.
   Then **chart `01-journey`** in place of the paragraph about the three stages summing
   to lead time. The table stays: it carries seven metrics the chart does not.
4. **Where the time goes** — **chart `02-where-time-goes`**, one line of interpretation,
   then the collapsed table. No shaded stage table in the reading flow.
5. **Work in progress** — active, committed queues, backlog, open bugs
6. **Epics** — one row; they are excluded from flow and summarised separately
7. **Production stability** — lead with the **quadrant counts and what Q1 contains**, since
   that is the decision the section exists to support. Then **chart `04-sentry-issues`**, the
   collapsed table (quadrant, impact, effort, reach per issue), and per-release load.
   Name any regression in the sentence above the chart.

   **Group by root cause before recommending anything.** Triage scores issues one at a
   time, so a single architectural fault arrives as several separate rows — this period,
   seven of the top ten were the same blocking-SDK-call-on-the-main-thread shape, and three
   `!!`-on-null crashes shared one dialog builder. Reporting those as ten items overstates
   the work and hides the actual fix. Say which issues ride along with which.
8. **Quality** — type mix, closed-without-a-fix, then **chart `03-sonarcloud-trend`** in
   place of the prose trend line, then the collapsed table
9. **Delivery** — PR metrics, CI state
10. **Releases** — overdue or upcoming, cadence
11. **Actions** — checkboxes, each specific enough to verify next month
12. **Method and caveats** — inside `<details>`

Writing rules:

- **Percentiles, not averages.** Say p50/p85, and explain them as median / slowest 15%.
- Lead with what changed and what to do, not with the measurement apparatus.
- State every caveat that would change a decision. Never quietly drop a source.
- Give each metric a one-line definition inline — readers should not have to guess whether
  "throughput" means completed items or a status range.
- No unicode bar glyphs in tables; the cell shading carries the emphasis.
- **A chart never stands alone.** Each one gets a sentence above saying what it shows, and
  its collapsed table below. The sentence carries the finding; the chart shows the shape;
  the table holds the numbers.
- **Don't narrate the chart.** If the sentence above it just reads out the bars, cut it.
- **Only these four get charted.** Everything else in the report is a two-point comparison,
  where a two-bar chart carries nothing the sentence does not. Adding a fifth chart needs a
  reason written into the reference, not a spare afternoon.

## 6. Publish

**Publishing goes through the Atlassian connector, not a token.** `createConfluencePage` /
`updateConfluencePage` authenticate as the user over OAuth, which is why this works in a
worktree, in a cloud session, and for any teammate who has the connector — no credential in
the repo. The connector uses **HTML+ADF**, not storage format: `<div data-type="panel-info">`,
`<span data-type="status">`, `<details>`, `<ul data-type="task-list">`. Do not emit
`<ac:structured-macro>`; it renders as literal text.

Create as a **draft** first, hand the user the link, and only set `status: current` once they
confirm. Confluence HTML notes:

- Panels: `<div data-type="panel-info|panel-warning|panel-note">`
- Status lozenges: `<span data-type="status" data-color="green|red|yellow|neutral">`
- Heatmap cells: `data-background="#hex"` on `<td>` (becomes `data-highlight-colour`)
- Tasks: `<ul data-type="task-list"><li data-type="task-item"><input type="checkbox"> …`
- Jira links become live issue macros automatically
- Tables cannot nest inside table cells; panels cannot contain tables

**Attaching the charts — the one step a token is for.** The connector has no
attachment-upload tool, so run:

```bash
python3 scripts/metrics/attach_charts.py <pageId>
```

It handles either token type (scoped → Bearer via `api.atlassian.com`; classic → Basic via
the site host) and prints the exact missing scope if the grant is short. Re-running does not
duplicate: v1 `POST /child/attachment` versions a same-named file in place. **Attach only
after the page is `current`** — the API refuses attachments on a draft.

Upload is **v1 only**. The v2 attachment API has GET and DELETE but no create operation, so
`read:attachment:confluence` cannot upload anything despite its name — see the reference.

The script prints ready-to-paste figure HTML for each file, with the Media API fileId already
resolved — **paste that verbatim**. The body cannot reference an attachment by filename, the
fileId is not the id the upload returns, and `data-width` is read as pixels unless
`data-width-type="percentage"` is present. The reference has the details.

A page that will carry charts must be **created** with `status: current`: the connector
cannot publish an existing draft, because Confluence demands version 1 for a first publish
and the update call sends the draft's own version.

**With no token, there are no images — and that is a supported outcome, not a failure.**
Publish each chart's `expand` block from `charts/tables.html` *expanded* rather than
collapsed, note in the report that the charts could not be attached, and move on. No number
is lost: the tables carry all of them, which is exactly what they are for.

Do **not** drive the Confluence editor by simulated typing to place charts: the caret moves
between an upload and the next keystroke, so images and text land in the wrong blocks, and an
editor click can select and replace an image with whatever is typed next. Both have happened.

Every chart that *is* attached needs, in this order: a sentence above it, the figure, then
that chart's `expand` block copied verbatim from `charts/tables.html`.

Re-running for the same period should **update** the existing page, not create a duplicate
title, and should **replace** the attachments rather than adding a second copy.

## Guardrails

- Read-only against Jira, GitHub, Sentry and SonarCloud. The only writes are the Confluence
  page and its chart attachments. Never write to Jira — not a comment, not a transition.
- **Do not treat a missing token as a blocker.** Every step except chart attachment works
  without one. A run that stops short because `JIRA_AUTH` is unset has failed for no reason.
- Take every figure in one report from a **single snapshot** — Jira counts drift between runs.
  Re-run all compute steps after any re-fetch rather than mixing. **Charts count**: re-run
  `charts.py` after any re-fetch, or the page shows one snapshot in text and another in
  pictures, which is the hardest kind of error to spot.
- **Never let a number exist only inside a chart.** The PNG is invisible to Confluence search
  and to screen readers, and next month's edition reads this page to close the loop — Sentry
  and GitHub have no retrospective view, so a figure that is only pixels is a figure that is
  gone. That is what the collapsed tables are for; they are not optional.
- Do not report per-person throughput. `assignee` is cleared when work completes, so the data
  cannot support it, and it is the wrong instrument for a flow review.
