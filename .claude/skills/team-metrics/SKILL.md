---
name: team-metrics
description: >
  Generate the monthly Android team metrics report and publish it as a Confluence
  page in the MOB space. Pulls flow metrics from Jira ANDROAPP changelogs (intake,
  delivery, post-merge, throughput, where time queues), production stability from
  Sentry, code quality from SonarCloud, and PR/CI health from GitHub; compares the
  rolling 90-day window against the preceding one; reviews how the Needs info gate is
  performing; composes the concise report layout with its three charts; and reports back on what
  moved since last edition's recommendations. Use when
  asked to produce, refresh or publish the team metrics report, or for a mid-month check
  on flow and bottlenecks.
---

# Android Team Metrics

Produces `Metrics Report <Month> <Year>` — e.g. `Metrics Report September 2026` — as a child of the
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
| GitHub | PR cycle time, review latency, PR size, CI | `gh` login, or the GitHub MCP tools where `gh` is absent (cloud sessions have no `gh`) | Skip the **PR/CI** line and say so |
| SonarCloud | code quality trend, security (vulnerabilities + hotspots) | none | Skip; no token needed, so failure means network |
| Sentry MCP | production stability | per-user OAuth | Check the Sentry tools are available in-session. If not, tell the user to authorize with `/mcp` — **you cannot run OAuth yourself.** Mark the section unavailable |

**Running in a cloud session.** Everything works there except what the environment's network
policy blocks. Confluence (the connector) and Sentry (MCP) are fine. What cloud containers do not
ship is `gh`, so take PR and CI figures from the GitHub MCP tools instead of the CLI. And the
egress proxy allow-lists hosts: if `--preflight` reports Jira or SonarCloud as a *tunnel 403*,
that is the allow-list refusing the CONNECT, not an auth failure — no token changes it. The
environment needs `dhis2.atlassian.net` and `sonarcloud.io` added to its allowed domains;
until then, run the report from a local checkout rather than publishing one with the flow
metrics missing.

**No credential is needed to produce the report or a draft.** Jira is world-readable, and
Confluence read/write goes through the Atlassian connector. A scoped token buys two things:
attaching charts 01–03 — **required to publish** — and covering permission-restricted Jira
issues.

Anonymous Jira sees only public issues, so a restricted issue would drop out of every
count with no error. Say "anonymous" in the Method section when the run was — the numbers
are a floor, not a certainty. Note a *scoped* token does not change this: it cannot read
Jira at all, so those runs are still anonymous and `metrics.json` records them as such.

`--token-help` explains the two token types and which scopes to grant. Never print, echo
or commit the token value.

## 2. Close the loop on the previous edition

**Do this before generating anything.** It is what makes the report a feedback loop rather
than a dashboard.

1. List children of page `1948057602`; take the most recent `Metrics Report …` page. Editions
   before Sep 2026 are titled `Android Metrics — Flow — YYYY-MM-DD`; match either.
2. `getConfluencePage` on it. Task state is in the body as
   `<ac:task-status>incomplete|complete</ac:task-status>`.
3. For each **ticked** recommendation, check the data for movement in that area. A tick
   means the team picked it up, not that a task was completed, so report what the numbers
   did — including "picked up, but the figure has not moved yet", which is a normal and
   useful outcome rather than a failure.
4. Carry unticked recommendations forward and note how many editions they have survived.
   **Read a stale one as a problem with the recommendation, not with the team**: something
   nobody has picked up in three editions is probably not worth suggesting again, or was
   never as important as the number made it look. Drop it, or say why it is still here.
5. Open the report with a short **Since last time** section covering the above.

**If step 1 finds no prior edition, this is the first one.** Skip the whole step, omit the
**Since last time** section entirely, and say in one line near the top that it is the first
edition so there is nothing to report back on. Do not substitute a section that compares
against nothing, and do not reconstruct a predecessor from a page that has been deleted —
if it is not a live child of `1948057602`, it does not exist for this purpose. The section
returns automatically on the next edition, when there is a predecessor to read.

## 3. Gather

```bash
python3 scripts/metrics/metrics.py --census   # types and statuses actually present
python3 scripts/metrics/metrics.py            # full run, writes metrics.json
```

`--census` reports **issue types and the in-flight list only** — use it to confirm the type
allow-list is still right. The **status** check comes from the full run, which prints either
`all observed statuses classified.` or a list of unclassified statuses. **If any status is
unclassified, stop and classify it before reading a single number** — unclassified time is
dropped silently and inflates flow efficiency. This has already caused two wrong numbers
historically (see the reference).

The one other recognised flag is **`--as-of YYYY-MM-DD`**, which pins the window end to a
past date instead of today — `w0/w1/w2` become `as_of-180d / as_of-90d / as_of`, matching
exactly how `charts.py --as-of` is already pinned. Use it to reproduce a specific prior
edition's numbers (e.g. when reconciling a draft built on live data against a published
report's frozen window) rather than re-deriving that window by hand. `--as-of` cannot move
forward of today, and it only pins the **flow** numbers — WIP, backlog and epic counts stay
current-state regardless, which the script says explicitly when the flag is used.

There is no `--help`: an unrecognised flag (including `--help`) is not rejected, it falls
through to a full run against today that overwrites `metrics.json` — don't pass anything
other than `--preflight`, `--census`, `--token-help`, or `--as-of` plus its date.

The full run also prints the Needs info review. Its stock list is current-state, so under
`--as-of` it describes today rather than the window end. If it
warns about a **Needs info spelling variant**, the status was renamed or a second spelling
exists — fix `NEEDS_INFO` before reading the section, because unmatched stays are simply
absent from it, with no gap to notice.

The script prints a correctness gate on ANDROAPP-7679. Expected: **42.8 d lead, 8.2 d in
`In Review`, 15.0 d `Ready to Start` → merged**. If it does not match, the stage math is wrong —
do not publish, investigate first.

Then gather the non-Jira sources (commands in the reference): GitHub PRs and CI runs, and
SonarCloud measures and history pinned to `branch=develop`.

**For Sentry, run the `sentry-triage` skill rather than querying issues here.** It resolves
the production release, attributes each issue to its owning repo, scores Impact and Effort,
and returns the impact/effort quadrants — which is what makes the stability section
actionable instead of a leaderboard. Take its scores verbatim into the report;
do not re-derive them, or the page and the triage will disagree.

Two things the triage does not cover, so still query them here:

- **crash load by release** (events per user across releases) — the 90-day trend, in the reference
- **whether a top issue is confined to one release** — what separates a regression from a
  long-standing problem, which aggregate averages hide

Mind the scope difference and state it on the page: triage is scoped to the **latest
production release**, the rest of the report to the **90-day window**. They answer different
questions — "what should we fix now" versus "how did stability move" — and an issue can
legitimately top one and be absent from the other.

## 4. Charts (mandatory for a published report)

Charts 01–03 are part of the report. **Every published (`current`) report must carry all
three.** The only exception is a **draft**, which may go without them when Sentry or Confluence
is not accessible (no Sentry means no fresh triage; no Confluence access or working token means
no attachments) — say so at the top of the draft and in Method. The tables beneath each chart
still carry every number, so a draft without images loses no data, but it is not publishable
until the charts are attached.

```bash
python3 scripts/metrics/charts.py --as-of <window-end YYYY-MM-DD> --release <e.g. 3.4.2>
```

| Chart | Sits under | Source |
|---|---|---|
| `01-journey` | **Trend at a glance** | `metrics.json` |
| `02-where-time-goes` | **Where the time goes** | `metrics.json` (`stages_delivery`) |
| `03-sonarcloud-trend` | **Code quality trend** | SonarCloud API |

`04-sentry-issues` is **not used**: Crash / ANR is a root-cause list, not a plot. Use `--as-of`
with the window end, never today. `charts/tables.html` holds each chart's collapsed table —
paste it, never retype figures.

Attaching needs a working scoped token **and a page with status `current`** (Confluence
refuses attachments on a draft); see §6. If the token is missing or rejected, stop and ask the
user to fix it rather than publishing without charts. The connector cannot copy another page's
images, so charts already on a published edition have to be pasted across in the Confluence
editor.

## 5. Compose

### The header

Three italic lines directly under the title, before **Since last time** — or before
**Crash / ANR exposure**, on a first edition that has no such section — in this order and this wording. It is a hand-tuned format — reproduce it, do not re-invent it per edition:

```html
<p><em>Rolling 90 days to </em><time datetime="2026-09-08">September 8, 2026</time><em>, compared with the preceding 90 days</em></p>
<p><em>Issue types: Feature, Task, Bug</em></p>
<p><em>Sources: Jira · GitHub · SonarCloud · Sentry</em></p>
```

The middots inside the Sources line separate items and stay. Lines 1 and 2 end with **no
trailing separator** — earlier editions carried one from when this was a single line, and it
renders as a dangling `·`. Each source name in that line is a link to its home (the Jira
project, the GitHub repo, the SonarCloud dashboard — see **Source links** in the reference);
the wording is unchanged.

The date is a Confluence `<time>` node, not plain text, and it is the **window end** — the
same `--as-of` the numbers were pinned with, never today's date. The Sources line lists only
the sources that actually contributed: drop any that was skipped, and the Method section
says why.

### Body

**This report is read aloud in a monthly team meeting, not studied at a desk.** A section the
team cannot absorb in the ~30 seconds it gets on screen has failed, however correct its
numbers are. The layout is the concise one approved in the reference draft
([Android Metrics — Concise Draft](https://dhis2.atlassian.net/wiki/spaces/MOB/pages/2043543554));
reproduce its sections, order and shape, and fill them with the current window's data.

For every section outside **Method**: at most three sentences of prose; the takeaway first,
in bold; a bullet over a sentence and a table over a bullet list of numbers. **Nothing is
deleted, only moved** — a figure that no longer earns a line goes into **Method**, never off
the page, because Sentry, GitHub and SonarCloud have no retrospective view.

**There is no Headline section.** The numbers it carried live in the sections below or in
Method. **Trend at a glance**, **Where the time goes** and **Code quality trend** are sections
of their own, each with its chart (§4) above a collapsed table.

**Say a figure once.** If a number is already in a table or a section, do not repeat it in
another section or in Method; Method holds only what appears nowhere else. Link back
("see Trend at a glance") instead of restating.

Structure, in order. Only the five core sections — Crash / ANR, Security, Backlog growth, Needs-info triage,
Delivery predictability — carry heading numbers 1–5, in that relative order. **Since last
time**, **Trend at a glance**, **Where the time goes**, **Code quality trend**, **Carried
forward, unchanged**, **Recommendations**, **Parked** and **Method** are unnumbered. The page
order is: Since last time → Trend at a glance → Where the time goes → 1 Crash / ANR → Code
quality trend → 2 Security → 3 Backlog growth → 4 Needs-info triage → 5 Delivery
predictability → Carried forward → Recommendations → Parked → Method. The list below
describes each section; take the order from this paragraph.

1. **Since last time** — one bullet per recommendation of the previous edition, prefixed
   `DONE` or `PARTIAL` (unticked and untouched ones are carried forward or dropped per §2).
   Say what was picked up and what the number did, e.g. "PARTIAL Vulnerabilities 17→4 —
   rating stayed E; mechanism explained in Security below." Omitted on a first edition (see §2).
   **Trend at a glance** — the four-metric table (Delivery p50, Delivery p85, Flow
   efficiency, Throughput; columns metric, what it measures, prev, now, change as a status
   lozenge), one line ("Intake and the post-merge tail …; Delivery stayed the same"), then
   **chart `01-journey`**, then two collapsed blocks: **Data table** (stage medians: intake,
   delivery, post-merge, sum) and **All flow metrics** (intake p50/p85, lead time p50/p85,
   post-merge p50, closed without a fix, with the lead-time-p85 and clean-up note). Throughput
   links to its JQL.

   **Where the time goes** — **scoped to delivery, not the lifecycle.** One bold line naming
   the largest queue inside delivery and whether it grew, one line on the runner-up if it is an
   outlier, **chart `02-where-time-goes`**, then collapsed **Data table** (status, previous,
   current, p85 duration; remaining stages grouped) and **Whole lifecycle, for context** (a
   different scope, not comparable).

   **Code quality trend** — placed after Crash / ANR, before Security. One bold line (the
   12-month direction of smells and debt; flag a coverage jump that is a measurement change,
   with the PR), **chart `03-sonarcloud-trend`**, then collapsed **Data table** (coverage,
   smells, debt, duplication, LOC: first month → report date, direction).

2. **Crash / ANR exposure** — from the `sentry-triage` run. Open with one bold line: events
   per affected user on the newest production release against the previous one, with the
   change in percent. Then the issues **split by root cause, in priority order, only causes
   with direct evidence**, under two sub-headings carrying the combined reach:
   - `ANR rate — ~X% combined reach, N confirmed causes`
   - `NPE / crash rate — ~X% combined reach, N confirmed causes`

   Each cause is a numbered bold line naming the mechanism, then one line of crash sites with
   users and reach (`LocaleSelector.kt:52` — 1,019 users, 9.3%). Issues that share a
   mechanism are one cause, not several rows: triage scores them one at a time, so a single
   fault arrives as many. Reach is **per issue**; a "combined" figure is allowed only where the
   issues are named and the sum is labelled approximate, and user counts are never summed.
   Close with an italic pointer: the sync-path clustering and the full quadrant table are in
   the earlier edition. Triage is scoped to the latest production release, the rest of the
   report to the 90-day window — state it. **If Sentry is unavailable**, carry the section
   forward from the previous edition inside a warning panel that says so and gives the date.
3. **Security** — `Rating: **E**` and the mechanism in one sentence: SonarCloud sets the
   letter from the single worst open finding's severity, not the count, so it can sit flat
   while real fixes land. Then the **Top 3, ranked by severity tier** (Blocker vulnerability →
   High-probability hotspot → next tier down), grouping findings that share a file and
   pattern into one entry. Each: bold title, then one line of `file:line`, what is wrong, and
   the fix; add the ticket where one exists. Then one line naming the **full pool** (confirmed
   vulnerabilities + hotspots by probability) and what is left outside the top 3. Close with
   the tracked KPI: **open vulnerabilities by severity**, not the letter.
4. **Backlog growth** — bold: not a general trend, it is the type that grew. The
   created / closed / net table for **Bug, Feature, Task** (90 days), the growing row in bold,
   then one line on intake per 30-day bucket, most recent first, noting whether a driver is
   confirmed. If part of the growth is a known batch (an epic's children), say so here.
5. **Needs-info triage** — **three bullets, no table.** N closed without a fix, then:
   one bullet per distinct pattern, split by resolution **and dwell** (see the reference):
   `Cannot Reproduce` closed in days is a genuine attempt and healthy; `Obsolete` aged weeks
   or months is a stale item, not an unanswered question. A third bullet for the one thing
   unusual this window (a concrete intake-quality gap with the issue linked, the oldest
   stock, or repeat visits) — one of them, not all. Outcome counts, dwell percentiles and
   type mix go to Method. Deleted issues cannot appear (Jira drops them); say so in Method.
6. **Delivery predictability** — **one sentence**: whether a driver for the movement in delivery
   p85 and flow efficiency is confirmed, and any theory considered and parked (named in
   Parked). Do not restate the p85, flow-efficiency or queue figures — they are in Trend at a
   glance and Where the time goes. **Delivery** is the downstream loop — commitment to merge;
   never use the word for PR/CI.
7. **Carried forward, unchanged** — an italic line ("Full detail in the <previous> edition")
   and a bullet list, **one line per item, no tables**, each labelled with its date. Holds, in
   this order, whichever have something to say:
   - **PR/CI** — review coverage, CI pass rate on `develop` **split at a fix rather than
     averaged**, PR size p85 against the 400-line gate when breached. A metric that did not
     move or breach a gate gets no words.
   - **Epics** — open count with last edition's in brackets, median age, whether it moved.
   - **Releases** — nothing overdue / what is due, and any version whose bookkeeping skews
     the other numbers.
8. **Recommendations** — checkboxes, five to eight, ordered roughly most-surprising first.
   Each names the hot spot, the number that makes it checkable next edition, and where it
   might be worth looking. **Suggest, do not instruct;** never assume a cause; no deadlines
   or owners; two lines at most. Security Top 3 entries get one recommendation each. Ticking
   one is the team saying "we picked this up", which is what the next edition reads (§2).
9. **Parked / not included this edition** — theories considered and left unproven (with the
   reason), and anything gathered but not published. Keep the heading even when empty.
10. **Method** — inside `<details>`. Window and sources; every figure not already shown above
    (whole-life flow efficiency, the closed-without-a-fix resolution breakdown, PR cycle time / review latency / PR size, Needs-info outcome counts,
    epic age and status split, released-patch bug counts); which queries were live rather than
    pinned to the window end and how far they drift; whether Jira was read anonymously (the
    numbers are then a floor); and a link to the earlier edition for definitions, the status
    taxonomy and full caveats.

**There is no work-in-progress section.** WIP is a "now" number that the dailies already
own. The script still prints it as a sanity check — reconcile with it, do not publish it.

Writing rules:

- **Percentiles, not averages.** Say p50/p85, and explain them as median / slowest 15%.
- Lead with what changed and what to do, not with the measurement apparatus.
- State every caveat that would change a decision. Never quietly drop a source.
- Give each metric a one-line definition inline — readers should not have to guess whether
  "throughput" means completed items or a status range.
- **Cut the apparatus, keep the caveat.** A caveat that would change a decision stays in the
  section, in a clause. How something was measured goes to Method.
- **One heading, one finding.**
- **Link the source.** Wherever a figure comes from a query, link the number to it — a Jira
  count to its JQL, a SonarCloud finding to its page, PR and CI figures to GitHub, a Sentry
  issue ID to the issue — so the reader can check it in one click. Link at the place that
  owns the figure, not at every repeat. **Verify each link returns the number printed beside
  it before publishing**; patterns, encoding and traps are under **Source links** in the
  reference.

## 6. Publish

**Publishing goes through the Atlassian connector, not a token.** `createConfluencePage` /
`updateConfluencePage` authenticate as the user over OAuth, which is why this works in a
worktree, in a cloud session, and for any teammate who has the connector — no credential in
the repo. The connector uses **HTML+ADF**, not storage format: `<div data-type="panel-info">`,
`<span data-type="status">`, `<details>`, `<ul data-type="task-list">`. Do not emit
`<ac:structured-macro>`; it renders as literal text.

**Draft or current.** Only a `draft` may omit the charts, and only when Sentry or Confluence is
not accessible (§4). A page that is going to be **published** must carry charts 01–03, so it is
created `status: current` (Confluence refuses attachments on a draft, and the connector cannot
publish an existing draft). A chart-less draft cannot simply be promoted: the user publishes it
from the Confluence UI, and the charts are then attached with `attach_charts.py <pageId>` and
spliced in before it counts as published. Create `current` only when asked to publish directly. Put the real body in the first write — a placeholder leaves the cached
page excerpt showing placeholder text in listings.

**Charts.** With a working token, attach after the page is `current`:
`python3 scripts/metrics/attach_charts.py <pageId>`, then update the body with the figure HTML
it prints (paste verbatim; the Media API fileId is not the upload id). Without one, the report stays a
draft. A figure element referencing another page's media is silently dropped by the
connector — copy images across in the editor instead.

**Verify every write.** An update can return success and still not save. Re-read the page
afterwards (HTML format) and check the change is there before reporting it done.

**Never overwrite a page someone else published.** Before writing, list the children of the
parent: if a published edition for the same period already exists and is not yours, create a
separately titled draft instead of updating it.

Confluence HTML notes:

- Panels: `<div data-type="panel-info|panel-warning|panel-note">`
- Status lozenges: `<span data-type="status" data-color="green|red|yellow|neutral">`
- Tasks: `<ul data-type="task-list"><li data-type="task-item"><input type="checkbox"> …`
- Jira links become live issue macros automatically
- Tables cannot nest inside table cells; panels cannot contain tables
- Updates replace the whole body: re-read the page before overwriting it
- The markdown export shows panel contents as empty; verify panels with the HTML format

Re-running for the same period should **update your own** draft, not create a duplicate title.

## Guardrails

- Read-only against Jira, GitHub, Sentry and SonarCloud. The only write is the Confluence
  page. Never write to Jira — not a comment, not a transition.
- A missing token does not block the analysis or a draft, but it **does block publishing**:
  a published report needs charts 01–03, and attaching them needs a working token (§4, §6).
- Take every figure in one report from a **single snapshot** — Jira counts drift between runs.
  Re-run all compute steps after any re-fetch rather than mixing. Anything carried forward
  from an earlier edition is labelled with its date.
- Do not report per-person throughput. `assignee` is cleared when work completes, so the data
  cannot support it, and it is the wrong instrument for a flow review.
