---
name: team-metrics
description: >
  Generate the monthly Android team metrics report and publish it as a Confluence
  page in the MOB space. Pulls flow metrics from Jira ANDROAPP changelogs (intake,
  delivery, post-merge, throughput, where time queues), production stability from
  Sentry, code quality from SonarCloud, and PR/CI health from GitHub; compares the
  rolling 90-day window against the preceding one; and reports back on whether last
  edition's actions worked. Use when asked to produce, refresh or publish the team
  metrics report, or for a mid-month check on flow and bottlenecks.
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

| Source | Needed for | If missing |
|---|---|---|
| Jira (`JIRA_AUTH` in `local.properties`) | all flow metrics | **Blocking.** Ask the user to add it; the script prints the line and the token URL. A read-scoped token is enough. |
| GitHub (`gh auth`) | PR cycle time, review latency, PR size, CI | Skip the Delivery section and say so |
| SonarCloud | code quality trend | Skip; no token needed, so failure means network |
| Sentry MCP | production stability | Check the Sentry tools are available in-session. If not, tell the user to authorize with `/mcp` — **you cannot run OAuth yourself.** Mark the section unavailable |

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

## 4. Compose

Structure, in order. Keep it short enough to read in a meeting; push detail into the collapsed
Method section.

1. **Since last time** — outcomes of ticked actions, carried-forward items
2. **Headline** — two bullet lists: *Going well* / *Needs attention*, ~5 each, no tables
3. **Trend at a glance** — metric, one-line definition, prev, now, change lozenge
4. **Where the time goes** — stage shares with cell shading, percentages only (no bar glyphs)
5. **Work in progress** — active, committed queues, backlog, open bugs
6. **Epics** — one row; they are excluded from flow and summarised separately
7. **Production stability** — regressions first, then top issues, then per-release load
8. **Quality** — type mix, closed-without-a-fix, SonarCloud trend
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

## 5. Publish

Create as a **draft** first, hand the user the link, and only set `status: current` once they
confirm. Confluence HTML notes:

- Panels: `<div data-type="panel-info|panel-warning|panel-note">`
- Status lozenges: `<span data-type="status" data-color="green|red|yellow|neutral">`
- Heatmap cells: `data-background="#hex"` on `<td>` (becomes `data-highlight-colour`)
- Tasks: `<ul data-type="task-list"><li data-type="task-item"><input type="checkbox"> …`
- Jira links become live issue macros automatically
- **No images, SVG or charts** — there is no attachment-upload tool, so tables, shading,
  lozenges and panels are the whole visual vocabulary
- Tables cannot nest inside table cells; panels cannot contain tables

Re-running for the same period should **update** the existing page, not create a duplicate title.

## Guardrails

- Read-only against Jira, GitHub, Sentry and SonarCloud. The only write is the Confluence page.
- Take every figure in one report from a **single snapshot** — Jira counts drift between runs.
  Re-run all compute steps after any re-fetch rather than mixing.
- Do not report per-person throughput. `assignee` is cleared when work completes, so the data
  cannot support it, and it is the wrong instrument for a flow review.
