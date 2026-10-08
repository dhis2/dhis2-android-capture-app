# Jira ticket for a Sentry issue — shared by `/sentry-triage` and `/sentry-fix`

One procedure, used by both skills: **find → (claim) → create → link**. Triage
runs it in bulk without opening PRs; `/sentry-fix` runs it for one issue.

**Atlassian MCP**: tool names carry a connection-specific prefix, so find them
by keyword (`ToolSearch` "jira search create issue transition"). Get `cloudId`
and the site (e.g. `dhis2.atlassian.net`) from the accessible-resources tool.
Not connected → ask the user once to connect it; declined → continue without a
ticket and say so in the report.

**Which project**

| Owner (repo-map §1) | Project | Issue type |
|---|---|---|
| app, design system | `ANDROAPP` | `Bug` |
| SDK | `ANDROSDK` | `Bug` |
| attribute-only lib | — no ticket — | |

---

## 1. Find an existing ticket

Every ticket for a Sentry issue contains its URL (tickets created from Sentry's
UI and by these skills alike), so search by the full short ID:

```
jql: project in (ANDROAPP, ANDROSDK) AND text ~ "<SENTRY-SHORT-ID>"
fields: ["summary", "status", "assignee"]
```

| Result | Action |
|---|---|
| An open ticket | **Reuse** it as `JIRA_KEY` |
| Only Done tickets | The crash **regressed**: create a new ticket (§3) and link it to the newest Done one (`createIssueLink`, type `Relates`) |
| Nothing | Check `search_issues(query: "issue:<SENTRY-SHORT-ID> is:linked")`. Linked → a ticket exists that the search can't see: **stop and ask** for its key, never create a duplicate. Not linked → create (§3) |

`/sentry-fix` only, before creating: also search for a ticket filed under a
*different* Sentry issue for the same crash — a few narrow queries
(`project = ANDROAPP AND text ~ "<crash-site class>"`, the method name, the
class plus a symptom word), never one broad unscoped `text ~`. A clearly
related closed ticket gets a `Relates` link from the new one.

## 2. Claim it — `/sentry-fix` only

Whoever runs `/sentry-fix` owns the ticket. Triage never claims.

| Assignee | Action |
|---|---|
| Nobody | Assign to the current user (`atlassianUserInfo` → `editJiraIssue`) and move to **In Progress** (`getTransitionsForJiraIssue` → `transitionJiraIssue`) |
| The current user | Continue |
| Someone else | **Stop and ask** — they may already be working on it |

## 3. Create

Fetch the required fields first (`getJiraIssueTypeMetaWithFields`) — 400s come
from missing required fields.

Every ticket either skill creates — ANDROAPP or ANDROSDK — gets
`labels: ["sentry-triager"]`. Reused tickets are left as they are.

- **ANDROAPP** — set at least:
  - `components: [{"id": "10415"}]` (`AndroidApp`, unless a more specific one obviously fits)
  - `environment`: release + platform, e.g. `"Android app 3.4.2 (build 157), production"`
  - `versions` (Affects versions): the crashing release, matched by name
  - `customfield_10131: {"id": "10196"}` — Internal feature = General interest
  - `customfield_10135: {"id": "10203"}` — Product Team = Android
- **ANDROSDK** — fill exactly what the metadata reports as required; never
  reuse ANDROAPP's IDs. If a required field has no obvious value, ask.

New tickets start **Open and unassigned** (triage) — `/sentry-fix` then claims
it (§2). Store the key as `JIRA_KEY`.

### Description

A developer should grasp the problem in about fifteen seconds.

- `contentFormat: "markdown"`, real Markdown with `##` headers — never Jira wiki
  markup (`h2.`, `{code}`), it renders literally.
- Three short sections of a couple of sentences each — **What breaks**,
  **Why**, **Impact** — then **How to test manually**. Prose over nested bullets.
- Include: the Sentry issue URL(s) with user/event counts, the one-sentence
  root cause, the affected release, the crash site as `File.kt:line`.
- **Never** paste a stack trace, event payload, or log dump — the Sentry link
  holds them and stays current.

**How to test manually** — for a field tester on a **release APK** (no adb, no
debug build, no logcat):
1. Preconditions — user and metadata needed (never a hardcoded UID).
2. Numbered steps using on-screen labels, including any state the crash needs
   (offline, rotated, search form closed…).
3. **Expected** — one observable outcome on the fixed build, and what the
   shipped build did instead.
4. Variants where the code branches, then one regression step on the normal path.

Self-check: the steps must reproduce the bug on the shipped release and pass on
the fixed code. Otherwise rewrite them.

Triage has not diagnosed the bug deeply, so it writes this section as one line:
`_Added by /sentry-fix once the fix is implemented._` — `/sentry-fix` replaces
it.

## 4. Link it to the Sentry issue

For every Sentry issue the ticket covers:

```
execute_sentry_tool(name: "link_issue", arguments: {
  organizationSlug: ORG_SLUG,
  issueId: "<SENTRY-SHORT-ID>",
  externalIssueUrl: "https://<site>/browse/<JIRA_KEY>"
})
```

This link is what makes Sentry resolve the issue when the ticket reaches Done
(the org's Jira integration syncs status) — nothing else does, so never
schedule polling. `already_linked` is success. Pass `integrationId` only if the
call says several installations match. Any other failure: continue, report
`Sentry link: failed (<reason>)`, and tell the user to link it by hand (Sentry
issue → Linked Issues → Jira → Link).

If a linked ticket reaches Done but the issue stays unresolved, the Jira
integration's "Sync Sentry status with Jira" mapping is missing for that
project (Sentry → Settings → Integrations → Jira → Configure).

## 5. After the PR — `/sentry-fix` only

- Replace the manual-test placeholder (or append the section if missing).
- Add a comment with the PR URL (`addCommentToJiraIssue`).
