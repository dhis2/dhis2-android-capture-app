---
name: sentry-triage
description: >
  Fetches unresolved Sentry issues for the latest production release,
  attributes each issue to its owning repo (capture app, DHIS2 Android SDK,
  or mobile design system), scores it on Impact (1-5) and Effort (1-5), and
  outputs a prioritized impact/effort quadrant report with ready-to-run
  /sentry-fix commands. Use when you want to decide what to fix next.
---

# Sentry Triage Skill

Stack traces are deobfuscated (ProGuard mappings uploaded on release builds), so
library frames carry real class names. Crashes may originate in DHIS2-owned
libraries shipped inside the APK — `references/repo-map.md` is the single source
of truth for package→repo attribution and per-repo facts. Load it before Step 4.

---

## Prerequisites — Sentry MCP plugin

This skill requires the `sentry@claude-plugins-official` plugin. Before running any step,
verify the plugin is available by checking whether `mcp__plugin_sentry_sentry__find_organizations`
is listed as an available tool.

Tool names vary across plugin versions: newer versions expose a small core set
plus a catalog — discover anything not listed (releases, issue events, …) with
`mcp__plugin_sentry_sentry__search_sentry_tools` and run it with
`mcp__plugin_sentry_sentry__execute_sentry_tool`.

If the plugin is **not installed**, stop and tell the user:

> The Sentry MCP plugin is not enabled in this session. To install it locally, run:
> ```
> /config
> ```
> Then navigate to **Extensions → Plugins**, find **Sentry**, and enable it. Alternatively,
> add the following to your `~/.claude/settings.json` (user-level, not committed to the repo):
> ```json
> {
>   "enabledPlugins": {
>     "sentry@claude-plugins-official": true
>   }
> }
> ```
> Once enabled, restart the session and run `/sentry-triage` again.

---

## Step 0 — Discover Sentry org and project

Call `mcp__plugin_sentry_sentry__find_organizations` to list accessible orgs. If there is
only one, use it. If there are multiple, pick the one whose slug matches the GitHub org of
the current repo (run `gh repo view --json owner -q .owner.login` to get it).

Store the result as `ORG_SLUG` and the org's `regionUrl` as `REGION_URL`.

Then call `mcp__plugin_sentry_sentry__find_projects` with `organizationSlug: ORG_SLUG`.
Match the project whose slug or name most closely corresponds to this Android app (look for
`android`, `capture`, or the repo name). Store this as `PROJECT_SLUG`.

---

## Step 1 — Resolve the latest production version

Do **not** read `gradle/libs.versions.toml` — the working branch is always ahead of what is
shipped. Determine the last production release using one of these methods, in order:

1. Query the project's releases (tool `find_releases` if listed, else discover it
   via `search_sentry_tools(query: "list releases")` and run it with
   `execute_sentry_tool`) for `PROJECT_SLUG`, filter by environment `production`,
   sort by `date` descending, take the first entry's `version` string.
2. If that returns no results, run:
   ```
   gh repo view --json nameWithOwner -q .nameWithOwner | xargs -I{} gh release list --repo {} --limit 5
   ```
   and pick the most recent non-prerelease tag. Strip a leading `v` if present.

Store this as `PROD_VERSION` for use in all subsequent queries.

Then resolve the library versions that release shipped: find the git tag matching
`PROD_VERSION` (strip any `+<build>` metadata) and run

```bash
git show <tag>:gradle/libs.versions.toml | grep -E "dhis2sdk|designSystem"
```

Store `SDK_VERSION` and `DESIGN_SYSTEM_VERSION` — they go in the report header
and pin the diagnosis ref when reading library code (repo-map §5).

---

## Step 2 — Query top unresolved issues

Call `mcp__plugin_sentry_sentry__search_issues` with:
- `organizationSlug`: `ORG_SLUG`
- `regionUrl`: `REGION_URL`
- `projectSlug`: `PROJECT_SLUG`
- `query`: `is:unresolved release:<PROD_VERSION> !is:ignored`
- `sort`: `user` (the enum is `date` | `freq` | `new` | `user` — note: singular `user`, not `users`)
- `limit`: 10

Note: `PROD_VERSION` is the full release string including the package and build,
e.g. `com.dhis2@3.4.1+156`. The `sort: user` window is release-scoped, so the
returned order can differ from each issue's all-release user total shown in the
issue detail — use the per-issue `Users Impacted` for Impact scoring and note the
distinction.

If 0 results come back, retry in this order:
1. Try `release:<PROD_VERSION>+<build-number>` — the Sentry Gradle plugin sometimes uploads
   releases in `<vName>+<vCode>` format. Inspect the first few Sentry releases to find the
   matching string.
2. If still 0, remove the `release:` filter entirely. Note in the report that the filter was
   relaxed and which version was targeted.

---

## Step 3 — Fetch latest event per issue

For each issue, fetch its latest event with
`mcp__plugin_sentry_sentry__get_sentry_resource` (issue short ID; add
`resourceType: "breadcrumbs"` for the breadcrumb trail). If you need more than
the latest event, discover the issue-events tool via
`search_sentry_tools(query: "issue events")` and fetch up to 3. From the most
recent event extract:
- `exception.values[0].stacktrace.frames` — full frame list
- `breadcrumbs.values` — last 10 entries (reveals the user flow)
- `user` — for uniqueness; note if absent (unauthenticated session)
- `tags` — look for `release`, `environment`, `screen`, `flow`

If events vary significantly across the 3 fetched (different top frames), note it in the
Scoring Detail — it means the issue aggregates multiple distinct bugs.

---

## Step 4 — Attribute each issue to an owning repo and map frames

Load `references/repo-map.md` (classification table §1, attribution heuristic §2,
per-repo facts §3).

Walk the innermost exception's frames from the crash site outward, classifying
each frame by package prefix (app / SDK / design system / rule engine /
expression parser / platform). Apply the attribution heuristic to determine
**Thrown in**, **Owner**, and **Confidence**, with a one-clause reason.

- `org.hisp.dhis.rules.*` / `org.hisp.dhis.lib.expression.*` → attribute and
  mark "external DHIS2 lib — no automated fix flow".
- Low confidence → add "verify ownership during fix" to the issue entry.

Then read the **top 3-5 owned files** starting from the crash site upward in the
call chain:

- **App-owned frames**: map with the app module table in repo-map §3.
- **Library-owned frames**: read from the sibling clone at the shipped version —
  `git -C ../<repo> show <diagnosis-ref>:<path>` (ref resolution: repo-map §5,
  using `SDK_VERSION` / `DESIGN_SYSTEM_VERSION` from Step 1). If the sibling
  clone is missing, attribute by package only and note it — do not clone during
  triage.

If a frame's `absPath` or `filename` is `SourceFile:N` (unresolved), skip it and
continue to the next frame.

---

## Step 5 — Score each issue

### Impact (1–5)

Take the **highest** matching base score, then apply modifiers:

| Score | Base criteria |
|-------|---------------|
| 5 | Crash (unhandled exception / ANR) affecting ≥ 100 unique users |
| 4 | Crash affecting 10–99 unique users |
| 3 | Non-crash degradation (wrong data, feature disabled, blank screen) affecting ≥ 50 users |
| 2 | Non-crash affecting 10–49 users OR crash affecting < 10 users |
| 1 | Non-crash < 10 users OR cosmetic / UI glitch |

**Modifiers** (cap total at 5):
- +1 if the crash site is in the login flow (`org.dhis2.usescases.login`, `org.dhis2.mobile.login`)
  or sync flow (`org.dhis2.mobile.sync`, `org.dhis2.usescases.sync`)
- +1 if the crash site is in data-entry/enrollment/form flow (`org.dhis2.form`,
  `org.dhis2.usescases.eventsWithoutRegistration`, `org.dhis2.usescases.enrollment`)
- +1 if `times_seen / users_seen` ratio > 5 (the same users are hitting it repeatedly)

### Effort (1–5)

SDK and design-system bugs are fixed **directly in their repos** (see
`/sentry-fix`), so score them on the same complexity criteria as app code — the
old "SDK = workaround only" penalty no longer applies.

Take the **highest** matching base score, then apply modifiers:

| Score | Base criteria |
|-------|---------------|
| 5 | Fix spans two repos (lib fix + app adaptation), or breaks a public SDK API (`:core:apiCheck`), or needs a new full arch layer (UseCase + Repository + ViewModel) |
| 4 | 3–4 source files OR `androidMain`-only change with no `commonMain` path OR crash site uses RxJava that would need migration |
| 3 | 2 files, mixed source sets, or new UseCase only (no repo change) — in whichever repo owns the fix |
| 2 | 1–2 files, known pattern (null guard, default value, missing catch), in the owning repo |
| 1 | Single-line fix in any file |

**Modifiers** (cap total at 5):
- +1 if owned stack depth > 10 frames (deep call chains require careful tracing)
- +1 if the owner is a library repo — the fix only reaches users after a library
  release plus an app version bump (record a "Ships via" line in the Scoring Detail)

---

## Step 6 — Classify into quadrants

| Quadrant | Condition | Label |
|----------|-----------|-------|
| Q1 | Impact ≥ 4 AND Effort ≤ 2 | Fix ASAP |
| Q2 | Impact ≥ 4 AND Effort ≥ 3 | Plan carefully |
| Q3 | Impact ≤ 3 AND Effort ≤ 2 | Quick wins |
| Q4 | Impact ≤ 3 AND Effort ≥ 3 | Defer |

---

## Step 7 — Output the triage report

Produce a markdown report with this structure:

```
## Sentry Triage Report — <PROJECT_SLUG>
Production release: <PROD_VERSION> (SDK <SDK_VERSION>, design system <DESIGN_SYSTEM_VERSION>)
Generated: <today's date>
[Note if release filter was relaxed and why]

### Q1: Fix ASAP (High Impact, Low Effort)
| Issue ID | Title | Impact | Effort | Owner | Crash site | To fix |
|----------|-------|--------|--------|-------|------------|--------|
| SENTRY-X | ...   | 5      | 1      | app   | Foo.kt:42  | `/sentry-fix SENTRY-X` |
| SENTRY-Y | ...   | 4      | 2      | SDK   | Bar.kt:99  | `/sentry-fix SENTRY-Y --repo dhis2/dhis2-android-sdk` |

### Q2: Plan Carefully (High Impact, High Effort)
...

### Q3: Quick Wins (Low Impact, Low Effort)
...

### Q4: Defer (Low Impact, High Effort)
...

---

### Scoring Detail

#### SENTRY-X — <title>
- **Impact**: X/5 — <one-sentence rationale>
- **Effort**: X/5 — <one-sentence rationale>
- **Owner**: <repo> (thrown in <repo>; confidence high|medium|low — <one-clause reason>)
- **Crash site**: `ClassName.kt:lineN`
- **Flow**: <login | sync | data-entry | tracker | dashboard | settings | other>
- **Users affected**: <count>
- **Events**: <count> (ratio <times_seen/users_seen>)
- **Root cause hint**: <one sentence from reading the crash-site file>
- **Shipped lib version**: <only for library-owned issues, e.g. SDK 1.14.1>
- **Ships via**: <only for library-owned issues: lib release → libs.versions.toml bump → app release; note if an app-side defensive guard is worth a companion fix>
- **To fix**: `/sentry-fix SENTRY-X [--repo <slug>]`
```

The `--repo` hint is advisory — `/sentry-fix` re-verifies ownership against real
code and may override it. Issues owned by attribute-only libs (rule engine,
expression parser) get a "manual upstream fix" note instead of a `/sentry-fix`
command.

If no issues were found even after relaxing the filter, state clearly which org and project
were resolved in Step 0, and suggest the user verify they are correct.
