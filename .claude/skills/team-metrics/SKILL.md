---
name: team-metrics
description: >
  Generate the monthly Android team metrics report and publish it as a Confluence
  page in the MOB space. Pulls flow metrics from Jira ANDROAPP changelogs (intake,
  delivery, post-merge, throughput, where time queues), production stability from
  Sentry, code quality from SonarCloud, and PR/CI health from GitHub; compares the
  rolling 90-day window against the preceding one; reviews how the Needs info gate is
  performing; draws the four charts that carry the report's main
  findings; and reports back on what moved since last edition's recommendations. Use when
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
| Charts | attaching the four PNGs — and *only* this | scoped `JIRA_AUTH`, optional: `read:content-details:confluence` + `write:attachment:confluence` | Publish the collapsed tables instead and say so. Nothing else is affected |
| GitHub | PR cycle time, review latency, PR size, CI | `gh` login, or the GitHub MCP tools where `gh` is absent (cloud sessions have no `gh`) | Skip the **Pull requests & CI** section and say so |
| SonarCloud | code quality trend | none | Skip; no token needed, so failure means network |
| Sentry MCP | production stability | per-user OAuth | Check the Sentry tools are available in-session. If not, tell the user to authorize with `/mcp` — **you cannot run OAuth yourself.** Mark the section unavailable |

**Running in a cloud session.** Everything works there except what the environment's network
policy blocks. Confluence (the connector), Sentry (MCP) and chart rendering are fine — cloud
containers ship Playwright's Chromium, which the renderer finds on its own. What they do not
ship is `gh`, so take PR and CI figures from the GitHub MCP tools instead of the CLI. And the
egress proxy allow-lists hosts: if `--preflight` reports Jira or SonarCloud as a *tunnel 403*,
that is the allow-list refusing the CONNECT, not an auth failure — no token changes it. The
environment needs `dhis2.atlassian.net` and `sonarcloud.io` added to its allowed domains;
until then, run the report from a local checkout rather than publishing one with the flow
metrics missing.

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

Pass no other flags: unrecognised arguments are not rejected, they fall through to a full
run that overwrites `metrics.json`.

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
actionable instead of a leaderboard. Take its scores verbatim into the report and the chart;
do not re-derive them, or the page and the triage will disagree.

Two things the triage does not cover, so still query them here:

- **crash load by release** (events per user across releases) — the 90-day trend, in the reference
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
| `01-journey` | the paragraph explaining that intake + delivery + post-merge sum to lead time — and, since §5.3 now shows only four metrics, the intake and post-merge figures themselves | `metrics.json` |
| `02-where-time-goes` | the whole shaded stage-share table | `metrics.json` (`stages_delivery`) |
| `03-sonarcloud-trend` | the SonarCloud prose line in **Quality** | SonarCloud API |
| `04-sentry-issues` | the top-issues table in **Production stability** | `--issue` args, from `sentry-triage` |

plus `charts/tables.html` — the same numbers as a collapsed Confluence `expand` macro per
chart. **Paste those, never retype the figures**; hand-transcribing is how a chart and its
table drift apart.

**Pass chart 4 at most ten issues.** Triage now returns sixteen, and effort is a 1–5
integer, so the extra rows pile into one or two vertical columns and the label ladder runs
off the bottom of the plot. Ten is the most that stays legible. Send the ten highest by
reach — they are the decision-relevant ones and they still cover every quadrant — and name
the remainder in a sentence with their quadrants, so nothing scored is silently dropped.

`--as-of` is the window end, not today. Passing today's date silently produces charts that
disagree with the page they sit on. If a chart is skipped (SonarCloud unreachable, no
`--issue` given) the script says so — say it in the report too, and keep that section's
existing table rather than leaving a hole.

PNG rasterization needs a Chrome-family binary. The script looks under `CHROME_BIN`, the
Linux binary names, the macOS `/Applications` paths, and Playwright's browsers directory
(`PLAYWRIGHT_BROWSERS_PATH`, default `/opt/pw-browsers`) — which is what makes charts work in
a cloud session, where nothing is on `PATH`. Without any of them the script emits SVG only,
and there is nothing to attach; fall back to the tables as above.

It prefers `headless_shell` and `--headless=old` over `--headless=new`, and that ordering is
load-bearing rather than cosmetic: new headless treats `--window-size` as the *outer* window,
so the page lays out ~88px shorter than the screenshot it writes, and the bottom of every
chart — axis labels and the footnote line — comes out blank with no error. Where only new
headless is available the script measures that frame with a probe page and pads the window,
which leaves a white strip below the chart. A PNG noticeably taller than its SVG `viewBox`
is that padding, not a bug.

## 5. Compose

### The header

Three italic lines directly under the title, before **Since last time** — or before
**Headline**, on a first edition that has no such section — in this order and this wording. It is a hand-tuned format — reproduce it, do not re-invent it per edition:

```html
<p><em>Rolling 90 days to </em><time datetime="2026-09-08">September 8, 2026</time><em>, compared with the preceding 90 days</em></p>
<p><em>Issue types: Feature, Task, Bug</em></p>
<p><em>Sources: Jira · GitHub · SonarCloud · Sentry</em></p>
```

The middots inside the Sources line separate items and stay. Lines 1 and 2 end with **no
trailing separator** — earlier editions carried one from when this was a single line, and it
renders as a dangling `·`.

The date is a Confluence `<time>` node, not plain text, and it is the **window end** — the
same `--as-of` the charts were drawn with, never today's date. The Sources line lists only
the sources that actually contributed: drop any that was skipped, and the Method section
says why.

### Body

**This report is read aloud in a monthly team meeting, not studied at a desk.** That is the
constraint every rule below serves: a section the team cannot absorb in the ~30 seconds it
gets on screen has failed, however correct its numbers are.

So, for every section outside **Method**:

- **At most three sentences of prose.** A finding that needs a fourth is two findings, or it
  belongs in Method. Paragraphs of four or five lines — the shape earlier editions used
  everywhere — do not survive the meeting; they get skipped and the finding inside them is
  lost.
- **Lead with the takeaway, in bold, on its own line.** If the reader stops after that line
  they should still have the point. Everything after it is support.
- **Prefer a bullet to a sentence and a table to a bullet list of numbers.** Tables and charts
  are read at a glance; prose is not.
- **Nothing is deleted, only moved.** Detail that no longer fits goes into the collapsed
  `<details>` under the section, or into Method — never off the page. The guardrails still
  hold: next edition reads this page, and Sentry, GitHub and SonarCloud have no retrospective
  view, so a number dropped here is a number gone.

Structure, in order.

1. **Since last time** — what moved on ticked recommendations, carried-forward ones. Omitted
   entirely on a first edition (see §2), replaced by a single line saying so. One line per
   ticked item: what was picked up, what the number did.
2. **Headline** — two bullet lists: *Going well* / *Needs attention*, ~5 each, no tables.
   One line each, no sub-clauses.
3. **Trend at a glance** — **four metrics, not ten.** The team reads this section to answer one
   question: is the part we control getting faster? So it carries, in this order:

   | Metric | One-line definition |
   |---|---|
   | Delivery p50 | `Ready to Start` → merged. The loop the team owns, median |
   | Delivery p85 | Same, slowest 15% — where the bottleneck shows up first |
   | Flow efficiency | Active ÷ total *inside* delivery. 15–40% is typical |
   | Throughput | Items completed in the window (resolution = Done) |

   Prev, now and a change lozenge for each, and nothing else in the reading flow. Then
   **chart `01-journey`**, which carries intake and post-merge visually, with one line
   above it.

   The other six metrics — intake p50/p85, lead time p50/p85, post-merge p50, closed without
   a fix — move into a collapsed **All flow metrics** table directly beneath, same columns.
   They are still measured, still published, still readable by next edition; they are just
   not what the meeting opens on. A ten-row table asks the reader to find the important row
   themselves, and in a meeting nobody does.

   **Delivery is the report's name for the downstream loop** — commitment to merge. Use that
   word for it everywhere, and never for the PR/CI section (see §9), which is why that
   section is no longer called Delivery.
4. **Where the time goes** — **scoped to delivery, not the whole lifecycle.** The question is
   where the team's own loop stalls, and lifecycle shares answer a different one: they are
   dominated by backlog dwell (`Open`, `Waiting for analysis`) that sits before commitment
   and drowns the delivery statuses out. `metrics.py` prints both; the section shows the
   delivery block, and `charts.py` draws it.

   One bold line naming **the largest queue inside delivery and whether it grew or shrank**,
   then **chart `02-where-time-goes`**, then the collapsed table. That queue is the bottleneck
   candidate — it is the one thing this section exists to hand the team.

   Keep the whole-lifecycle shares in the same collapsed block, as a second table under a
   **Whole lifecycle, for context** heading. A large move there (a status going from 7% to
   32% of all tracked time in one window) is worth a single line in **Recommendations**, but
   it is not a delivery finding and does not belong in the reading flow.
5. **Epics** — one row: open count, closed in the window, age p50/p85, oldest, split by
   status. They are excluded from flow and summarised separately.

   Keep it to that row and one line of trend. Epics matter because the pile needs cleaning
   up, but the pile itself is a Jira query anyone can run — listing 27 stale epics buries the
   report without telling the reader anything the count did not. What earns its place is
   whether open count and median age moved since last edition, and whether any were closed.
   If they have not moved for several editions, say so in that one line and put it in
   **Recommendations** — do not paste the list.
6. **Needs info review** — **a table and three bullets. Nothing else.** The section answers
   whether the gate is worth having, and the numbers that answer it are few.

   The outcome table first, one row per bucket, current window against previous:

   | Bucket | Means |
   |---|---|
   | moved forward and done | reached commitment or beyond afterwards, resolved `Done` |
   | moved forward, still open | progressed downstream, not yet resolved |
   | closed without a fix | terminal on any resolution but `Done` |
   | still in Needs info | never left |
   | never moved on | left the status but never reached commitment or an active status |

   Then exactly three bullets:

   - **Is it working?** The share that moved forward, against the previous window, in one
     sentence. Add "and the current window is younger, so this is a floor" only when the
     comparison is close.
   - **What happened to the rest?** The closed-without-a-fix count and the resolutions they
     carry (`Obsolete`, `Cannot Reproduce`, `Invalid`). This is the finding: those are
     questions that were asked and never answered, and the item aged out.
   - **Where to look.** The current stock and its oldest item, or the repeat-visit count, or
     the same-day-flip count — **whichever one is actually unusual this window**, with a
     hint at what it might mean. One of them, not all three.

   Everything else the script prints — dwell percentiles, stay counts, type mix, exit
   targets — goes into Method if it goes anywhere. It is texture, and texture is what made
   this section too long to read.

   Four things to still get right, because they change what the numbers mean:

   - **Moving forward is not the same as leaving the status.** Every exit lands in `To do`,
     so an exit alone means somebody cleared the flag, not that the question was answered.
     `progressed_after` therefore looks for a later entry to `Ready to Start`, a merge
     marker, or an active status. Never describe an exit as progress.
   - **The closed bucket is the finding.** Read it against the moved-forward share and say
     which way the gate is working.
   - **The current window is younger, so it is not settled.** Its items have had less time to
     progress or to be closed, which inflates "still open" and deflates both terminal
     buckets. A rise in the moved-forward share despite that bias is a safe claim; a fall is
     not.
   - **Quote the `>=1d` dwell percentiles** if you quote dwell at all. Most stays are same-day
     flips, which drags the raw median to zero.

   Deleted issues cannot appear here at all — Jira drops them from the API entirely. That
   belongs in Method, in one clause.
7. **Production stability** — **the tables and the charts carry this section; the prose is a
   verdict, not an analysis.** Earlier editions ran six paragraphs here and the team read the
   tables anyway.

   In order:

   - **Two bold lines: what is good, what needs improving.** "3.4.2 improved on 3.4.1 by 19%
     on matched windows" is the first; "one line of code reaches 11.5% of users" is the
     second. That pair is the section's whole argument.
   - The quadrant counts and **what Q1 contains**, in one line — that is the decision the
     section exists to support.
   - **Chart `04-sentry-issues`**, with a sentence above naming any regression, then the
     collapsed table (quadrant, impact, effort, reach, crash site per issue).
   - **Root causes, as a short bullet list, not prose.** This is the one piece of analysis
     that earns its space: triage scores issues one at a time, so a single architectural
     fault arrives as several rows. One bullet per group — "7F4F + 7DKH + 83T7 = one line in
     `LocaleSelector`, 908 users" — says in a line what a paragraph said in ten. Reporting
     them as separate items overstates the work and hides the fix.
   - **Crash load by release**, closing the section: the definition inline on the heading
     line (events per user per release over the window — a rate, so releases with different
     install bases are comparable), the table, and **one sentence on the newest release**
     saying whether it regressed, held or improved. Never titled "Load per release" —
     readers could not tell what that meant. Keep the user column beside the rate and note
     in the table's caption that a barely-adopted release has a noisy rate; that caveat does
     not need its own paragraph.

   Ownership findings, crash mechanisms and the SDK-versus-app argument go into **Method**.
   They are correct and they matter to whoever picks the work up — they are not what the
   meeting decides.
8. **Quality** — three blocks, in this order, each under its own heading:

   `### Bugs fixed per patch release` — **the table is the section.** One row per version:
   version, release date, bugs fixed, closed without a fix, still tagged but open. Cover the
   last three shipped patches plus the one in flight, flagged as unreleased. Add prose only
   when a row needs a warning the table cannot carry — at most one line. Everything the
   earlier editions explained here (that `fixVersion` is a target and not a shipped-in stamp,
   that the last column is hygiene rather than release content, that the table is a floor and
   what the coverage ratio was, that a low count on an old patch is throughput and not a
   regression) goes into **Method**, and the *still tagged but open* column carries a
   four-word caption saying it is stale tags.

   A patch is a bug-fix release — the third number moves (`3.4.1`), or a fourth is appended
   as a hotfix (`3.4.0.1`). `X.Y.0` is a feature release and is excluded. The table is
   **release-scoped, not window-scoped**; say that in the caption, since a patch can predate
   the window entirely.

   **No completed-work type mix.** Whether a window delivered more features or more bugs is a
   function of what the release was for, so the split moves for reasons that have nothing to
   do with quality and invites a conclusion the number cannot support.

   `### Closed without a fix, open bugs and backlog` — the window's closed-without-a-fix count
   with its top resolutions, open bug count, backlog depth. Two lines.

   `### Code quality trend` — **this heading is required.** The SonarCloud block used to open
   with a bare sentence ("Over the year on `develop`, code smells and technical debt are down
   by roughly half…") that read as a stray paragraph inside Quality; it is a distinct finding
   over a distinct window (12 months, not 90 days) and needs its own title to be found and
   skimmed. Under it: that one line, **chart `03-sonarcloud-trend`**, the collapsed table, and
   the security-rating line.
9. **Pull requests & CI** — **renamed from "Delivery", which was the problem with it.** The
   word already means `Ready to Start` → merged in §3, so a second section called Delivery
   measuring something else (PR open → merged, a different clock over a different population)
   read as a contradiction of the flow numbers rather than a different view.

   Keep it to **three bullets and the small table** — PR cycle time, review latency, PR size
   at p50/p85 against the previous window, then CI pass rate on `develop`. It earns its place
   for two reasons and only two: it is the only view of the *review* queue, which is usually
   the largest active stage inside delivery, and it is the only place CI health is measured
   at all. Both are things the team can act on the same week.

   Report a number here **only when it moved or breached a gate** — the PR size gate at 400
   lines, review coverage, a CI pass rate that splits at a fix rather than averaging. A
   stable metric gets no line; it stays in the table.

   If the section ever has nothing that moved, say that in one line and keep the table. Do
   not delete it — GitHub has no retrospective view, so an edition that omits it leaves a
   permanent hole in the series.
10. **Releases** — overdue or upcoming, cadence. Two or three lines: what shipped, what is
    next, and any version whose bookkeeping makes the other numbers wrong.
11. **Recommendations** — checkboxes, each naming a hot spot and why it stands out

    **Suggest, do not instruct.** The report's job is to point at where the numbers are
    unusual and hand the judgement back; the team decides what is worth doing, and an
    imperative list pre-empts exactly the conversation the report exists to start. Write
    each one as *what the data shows* → *where it might be worth looking*, not as a task
    to be executed.

    - Say what moved and why it caught attention, then suggest the place to look. "16
      bugs still carry `fixVersion` 3.3.1, which shipped in January — worth checking
      whether those tags are stale" reads as a recommendation; "Clear the 16 bugs" reads
      as an order.
    - **Never assume the cause.** A number is unusual; the reason for it usually lives
      outside the data. Offer the reading as a possibility and leave room for the team to
      know better — they generally do.
    - **No priority ordering, no deadlines, no owners.** Those are the team's to assign.
      Roughly most-surprising first is enough.
    - Keep each one **specific enough to check next edition**, since that is what makes
      ticking useful — a hot spot with no observable number attached cannot be followed up.
    - **Two lines each at most**, and five to eight of them. A list of fourteen is a backlog,
      not a recommendation, and it buries the two that matter.

    Keep the checkboxes. Ticking one is the team saying "we picked this up", which is what
    the next edition reads — see §2.
12. **Method and caveats** — inside `<details>`

**There is no work-in-progress section.** WIP is a "now" number that the dailies already
own, and a monthly report reprinting it invites decisions about today's board from data
that is up to a month stale. The script still prints WIP and keeps it in `metrics.json` as
a sanity check — use it to reconcile, do not publish it. The two counts in that block that
are genuinely monthly rather than momentary, open bugs and backlog depth, go into
**Quality** instead.

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
- **Cut the apparatus, keep the caveat.** A caveat that would change a decision stays in the
  section, in a clause. A caveat that explains how something was measured goes to Method.
  "8 of 33 were closed unanswered" is a finding; how `progressed_after` detects progress is
  apparatus.
- **One heading, one finding.** If a section has two, it needs two headings — that is what
  went wrong with the SonarCloud trend, which hid under Quality with no title of its own.
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

**Create the page with `status: current`.** Not as a draft: Confluence refuses attachments on
a draft, and the connector cannot publish an existing draft either — it demands version 1 for
a first publish and the update call sends the draft's own version. Every edition carries
charts, so draft-first is not an available workflow; attempting it costs a page you then have
to abandon. Hand the user the link as soon as it exists and iterate on the live page.

Because figure `fileId`s only exist after upload, the page is written in **two passes**:
create with the body minus the figures, run `attach_charts.py`, then update with the figures
spliced in. Put the real body in the first pass — a placeholder leaves Confluence's cached
page excerpt showing the placeholder text in every page listing, even after the update lands.

Confluence HTML notes:

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
