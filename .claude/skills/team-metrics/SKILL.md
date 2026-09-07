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
| Confluence | reading the previous edition, creating the draft, attaching charts | **`JIRA_AUTH` required** — anonymous reads are refused (404) | Compute the report and hand it over as text; say it could not be published |
| GitHub (`gh auth`) | PR cycle time, review latency, PR size, CI | `gh` login | Skip the Delivery section and say so |
| SonarCloud | code quality trend | none | Skip; no token needed, so failure means network |
| Sentry MCP | production stability | per-user OAuth | Check the Sentry tools are available in-session. If not, tell the user to authorize with `/mcp` — **you cannot run OAuth yourself.** Mark the section unavailable |

**A token is optional but preferred.** Anonymous sees only public issues, so a
permission-restricted issue would drop out of every count with no error. If the run was
anonymous, say so in the Method section — the numbers are a floor, not a certainty.

Never print, echo or commit the token value.

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

Then gather the non-Jira sources (commands in the reference): GitHub PRs and CI runs, SonarCloud
measures and history pinned to `branch=develop`, and Sentry top issues plus per-release load.

For Sentry, always check whether a top issue is confined to one release — that is what
distinguishes a regression from a long-standing problem, and aggregate averages hide it.

## 4. Draw the four charts

```bash
python3 scripts/metrics/charts.py --as-of <window-end YYYY-MM-DD> --release <e.g. 3.4.1> \
  --issue "89RF|NullPointerException|ProgramFragment.showSyncDialog|4500|3.4.1 only|new" \
  --issue "87NX|TooManyRequests|NetworkStatusProviderImpl|1971|since 3.4.0.1|old"
```

Flow numbers come from `metrics.json`, SonarCloud history the script fetches itself, and the
Sentry rows you pass on the command line from what the MCP tools just returned — one `--issue`
per row, highest users first, `new` meaning the issue exists **only** in this release. Nothing
is written to disk but the charts.

It produces, in `scripts/metrics/charts/`:

| Chart | Replaces | Source |
|---|---|---|
| `01-journey` | the paragraph explaining that intake + delivery + post-merge sum to lead time | `metrics.json` |
| `02-where-time-goes` | the whole shaded stage-share table | `metrics.json` |
| `03-sonarcloud-trend` | the SonarCloud prose line in **Quality** | SonarCloud API |
| `04-sentry-issues` | the top-issues table in **Production stability** | `--issue` args |

plus `charts/tables.html` — the same numbers as a collapsed Confluence `expand` macro per
chart. **Paste those, never retype the figures**; hand-transcribing is how a chart and its
table drift apart.

`--as-of` is the window end, not today. Passing today's date silently produces charts that
disagree with the page they sit on. If a chart is skipped (SonarCloud unreachable, no
`--issue` given) the script says so — say it in the report too, and keep that section's
existing table rather than leaving a hole.

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
7. **Production stability** — **chart `04-sentry-issues`**, then the collapsed table, then
   per-release load. Regressions are already red-and-marked in the chart; still name them
   in the sentence above it.
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

Create as a **draft** first, hand the user the link, and only set `status: current` once they
confirm. Confluence HTML notes:

- Panels: `<div data-type="panel-info|panel-warning|panel-note">`
- Status lozenges: `<span data-type="status" data-color="green|red|yellow|neutral">`
- Heatmap cells: `data-background="#hex"` on `<td>` (becomes `data-highlight-colour`)
- Tasks: `<ul data-type="task-list"><li data-type="task-item"><input type="checkbox"> …`
- Jira links become live issue macros automatically
- Tables cannot nest inside table cells; panels cannot contain tables

**Attaching the charts.** Upload each PNG to the page, then reference it by filename:

```html
<ac:image ac:align="center" ac:width="900">
  <ri:attachment ri:filename="02-where-time-goes.png"/>
</ac:image>
```

Upload with the REST API — `JIRA_AUTH` is already required for Jira and the same token works
here (see the reference for the exact call). Do **not** drive the Confluence editor by
simulated typing to place charts: the caret moves between an upload and the next keystroke,
so images and text land in the wrong blocks, and an editor click can select and replace an
image with whatever is typed next. Both have happened.

Every chart also needs, in this order: a sentence above it, the `<ac:image>`, then that
chart's `expand` block copied verbatim from `charts/tables.html`.

Re-running for the same period should **update** the existing page, not create a duplicate
title, and should **replace** the attachments rather than adding a second copy.

## Guardrails

- Read-only against Jira, GitHub, Sentry and SonarCloud. The only writes are the Confluence
  page and its chart attachments.
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
