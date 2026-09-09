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
| `01-journey` | the paragraph explaining that intake + delivery + post-merge sum to lead time | `metrics.json` |
| `02-where-time-goes` | the whole shaded stage-share table | `metrics.json` |
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

PNG rasterization needs Chrome (looked up under both Linux binary names and the macOS
`/Applications` paths). Without it the script emits SVG only, and there is nothing to attach —
fall back to the tables as above.

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

Structure, in order. Keep it short enough to read in a meeting; push detail into the collapsed
Method section.

1. **Since last time** — what moved on ticked recommendations, carried-forward ones. Omitted
   entirely on a first edition (see §2), replaced by a single line saying so.
2. **Headline** — two bullet lists: *Going well* / *Needs attention*, ~5 each, no tables
3. **Trend at a glance** — metric, one-line definition, prev, now, change lozenge.
   Then **chart `01-journey`** in place of the paragraph about the three stages summing
   to lead time. The table stays: it carries seven metrics the chart does not.
4. **Where the time goes** — **chart `02-where-time-goes`**, one line of interpretation,
   then the collapsed table. No shaded stage table in the reading flow.
5. **Epics** — one row: open count, closed in the window, age p50/p85, oldest, split by
   status. They are excluded from flow and summarised separately.

   Keep it to that row. Epics matter because the pile needs cleaning up, but the pile
   itself is a Jira query anyone can run — listing 27 stale epics on the page buries the
   report without telling the reader anything the count did not. What earns its place is
   the **trend**: whether open count and median age moved since last edition, and whether
   any were closed. If the numbers have not moved for several editions, say that in a
   sentence and put it in **Recommendations** — do not paste the list.
6. **Needs info review** — keep it to roughly a table and four sentences. One question
   only: **of the items parked for missing information, how many got the information and
   moved on, and how many were quietly closed instead?** Everything else about the status
   is texture, and the stage table already carries its queue time.

   Lead with the outcome table, one row per bucket, current window against previous:

   | Bucket | Means |
   |---|---|
   | moved forward and done | reached commitment or beyond afterwards, resolved `Done` |
   | moved forward, still open | progressed downstream, not yet resolved |
   | closed without a fix | terminal on any resolution but `Done` |
   | still in Needs info | never left |
   | never moved on | left the status but never reached commitment or an active status |

   Then, in one line each: the **share that moved forward** and how it compares with the
   previous window; the **resolutions the closed ones carry**; one texture line (stays,
   repeat visitors, same-day flips, dwell p50/p85 for stays ≥ 1 day); and the **current
   stock** with its oldest item. No exit-target table, no type mix, no list of everything
   sitting there — the script prints more than the section should carry.

   Four things to get right:

   - **Moving forward is not the same as leaving the status.** Every exit lands in
     `To do`, so an exit alone means somebody cleared the flag, not that the question was
     answered. `progressed_after` therefore looks for a later entry to `Ready to Start`, a
     merge marker, or an active status. Do not describe an exit as progress.
   - **The closed bucket is the finding.** `Obsolete` / `Cannot Reproduce` / `Invalid`
     after a stay here means the question was asked and never answered, and the item aged
     out. Read it against the moved-forward share and say which way the status is working.
   - **The current window is younger, so it is not settled.** Its items have had less time
     to progress or to be closed, which inflates "still open" and deflates both terminal
     buckets. Say so when the comparison is close; a rise in the moved-forward share
     despite that bias is a safe claim, a fall is not.
   - **Quote the `>=1d` dwell percentiles.** Most stays are same-day flips in and out,
     which drags the raw median to zero. Report the flip count as its own fact — it says
     the status is partly used as a marker rather than a queue.

   Deleted issues cannot appear here at all — Jira drops them from the API entirely — so
   the outcome mix covers everything that still exists and nothing that was purged. State
   that; it is the one bucket the section genuinely cannot measure.
7. **Production stability** — lead with the **quadrant counts and what Q1 contains**, since
   that is the decision the section exists to support. Then **chart `04-sentry-issues`**, the
   collapsed table (quadrant, impact, effort, reach per issue), and **Crash load by
   release**. Name any regression in the sentence above the chart.

   **Crash load by release** closes the section. Readers could not tell what the old
   heading meant, so it carries three things and is **never titled "Load per release"**:

   - **The definition inline, on the same line as the heading:** events per user on each
     release over the window — a rate, so releases with different install bases and
     different time in the field are comparable.
   - **One explicit sentence on the newest release**: whether it regressed, held, or
     improved against its predecessor. Say which, in those terms; a table of rates with
     no verdict is the thing the team could not read.
   - **The weighting caveat**: a barely-adopted release has a noisy rate and is not yet a
     finding. Keep the user column beside the rate and say so, rather than letting a
     small denominator look like a result.

   **Group by root cause before recommending anything.** Triage scores issues one at a
   time, so a single architectural fault arrives as several separate rows — this period,
   seven of the top ten were the same blocking-SDK-call-on-the-main-thread shape, and three
   `!!`-on-null crashes shared one dialog builder. Reporting those as ten items overstates
   the work and hides the actual fix. Say which issues ride along with which.
8. **Quality** — open with **bugs fixed per patch release**, one row per version:
   version, release date, bugs fixed, closed without a fix, still tagged but open.
   Cover the last three shipped patches plus the one in flight, flagged as unreleased.
   Then closed-without-a-fix for the window, open bug count and backlog depth, then
   **chart `03-sonarcloud-trend`** in place of the prose trend line, then the collapsed
   table.

   **No completed-work type mix.** Whether a window delivered more features or more bugs
   is a function of what the release was for, so the split moves for reasons that have
   nothing to do with quality and invites a conclusion the number cannot support. Patch
   releases are the honest comparison: they exist to fix bugs, so their bug count means
   the same thing every time.

   A patch is a bug-fix release — the third number moves (`3.4.1`), or a fourth is
   appended as a hotfix (`3.4.0.1`). `X.Y.0` is a feature release and is excluded. This
   table is **release-scoped, not window-scoped**, like the Sentry triage and unlike
   everything else in the report; say so, since a patch can predate the window entirely.

   Two things to state, because the field is not what it looks like:

   - **`fixVersion` is a target, not a shipped-in stamp.** It is set when work is planned
     for a release and is not cleared when the work slips, so a released version can still
     carry open items — 3.3.1 shipped in Jan 2026 and 16 bugs still point at it. That is
     the `still open` column: not work in the release, but tags nobody cleaned up. Read it
     as a hygiene signal and do not add it to the fixed count.
   - **The table is a floor.** Only bugs carrying a `fixVersion` can appear. The script
     prints the coverage ratio for the window — quote it, and if it drops, the table is
     understating every row rather than showing a real decline.

   Do not read a low count on an old patch as a regression without checking throughput:
   3.3.1 really did ship with one tracked bug fix, because bug throughput in that era was
   ~11 per half-year against ~67 now.
9. **Delivery** — PR metrics, CI state
10. **Releases** — overdue or upcoming, cadence
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
    - Five to eight is plenty. A list of fourteen is a backlog, not a recommendation, and
      it buries the two that matter.

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
