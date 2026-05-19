---
name: sentry-triage
description: >
  Fetches unresolved Sentry issues for the latest production release of
  dhis2-android-capture, scores each issue on Impact (1-5) and Effort (1-5),
  and outputs a prioritized impact/effort quadrant report with ready-to-run
  /sentry-fix commands. Use when you want to decide what to fix next.
---

# Sentry Triage Skill

Org: `dhis2` · Project: `dhis2-android-capture`
Stack traces are deobfuscated (ProGuard mappings uploaded on release builds).

---

## Step 1 — Resolve the latest production version

Do **not** read `gradle/libs.versions.toml` — the working branch is always ahead of what is
shipped. Determine the last production release using one of these methods, in order:

1. Call `mcp__sentry__list_releases` for project `dhis2-android-capture`, filter by environment
   `production`, sort by `date` descending, take the first entry's `version` string.
2. If that tool is unavailable or returns no results, run:
   ```
   gh release list --repo dhis2/dhis2-android-capture-app --limit 5
   ```
   and pick the most recent non-prerelease tag. Strip a leading `v` if present.

Store this as `PROD_VERSION` for use in all subsequent queries.

---

## Step 2 — Query top unresolved issues

Call `mcp__sentry__list_issues` with:
- `organization_slug`: `dhis2`
- `project_slug`: `dhis2-android-capture`
- `query`: `is:unresolved release:<PROD_VERSION> !is:ignored`
- `sort`: `users`
- `limit`: 10

If 0 results come back, retry in this order:
1. Try `release:<PROD_VERSION>+<build-number>` — the Sentry Gradle plugin sometimes uploads
   releases in `<vName>+<vCode>` format. Inspect the first few Sentry releases to find the
   matching string.
2. If still 0, remove the `release:` filter entirely. Note in the report that the filter was
   relaxed and which version was targeted.

---

## Step 3 — Fetch latest event per issue

For each issue, call `mcp__sentry__get_issue_events` (limit: 3). From the most recent event
extract:
- `exception.values[0].stacktrace.frames` — full frame list
- `breadcrumbs.values` — last 10 entries (reveals the user flow)
- `user` — for uniqueness; note if absent (unauthenticated session)
- `tags` — look for `release`, `environment`, `screen`, `flow`

If events vary significantly across the 3 fetched (different top frames), note it in the
Scoring Detail — it means the issue aggregates multiple distinct bugs.

---

## Step 4 — Map stack frames to source files

Filter to **app-owned Kotlin files** only. Exclude:
- `org.hisp.dhis.android.core.*` (DHIS2 Android SDK)
- `androidx.*`, `android.*`, `kotlin.*`, `kotlinx.*` (system/platform)
- Any frame from `node_modules`, Maven coordinates, or jar paths

Apply this package→source-path mapping:

| Package prefix | Source root |
|----------------|-------------|
| `org.dhis2.mobile.login.*` | `login/src/(commonMain\|androidMain)/kotlin/` |
| `org.dhis2.mobile.sync.*` | `sync/src/commonMain/kotlin/` |
| `org.dhis2.mobile.commons.*` | `commonskmm/src/commonMain/kotlin/` |
| `org.dhis2.mobile.aggregates.*` | `aggregates/src/commonMain/kotlin/` |
| `org.dhis2.tracker.*` | `tracker/src/(commonMain\|androidMain)/kotlin/` |
| `org.dhis2.form.*` | `form/src/main/java/` |
| `org.dhis2.commons.*` | `commons/src/main/java/` |
| `org.dhis2.*` (remaining) | `app/src/main/java/` |

Read the **top 3-5 app-owned files** starting from the first app-owned frame (crash site)
upward in the call chain. If a frame's `absPath` or `filename` is `SourceFile:N` (unresolved),
skip it and continue to the next frame.

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

Take the **highest** matching base score, then apply modifiers:

| Score | Base criteria |
|-------|---------------|
| 5 | Requires changes to DHIS2 Android SDK (D2 API), or > 4 source files, or a new full arch layer (UseCase + Repository + ViewModel) |
| 4 | 3–4 source files OR `androidMain`-only change with no `commonMain` path OR crash site uses RxJava that would need migration |
| 3 | 2 files, mixed `commonMain`/`androidMain`, or new UseCase only (no repo change) |
| 2 | 1–2 files entirely in `commonMain`, known pattern (null guard, default value, missing catch) |
| 1 | Single-line fix in any file |

**Modifiers** (cap total at 5):
- +1 if app-owned stack depth > 10 frames (deep call chains require careful tracing)
- +2 if `org.hisp.dhis.android.core` appears in the top 3 frames (crash is inside the SDK;
  the fix is a workaround, not a direct source edit)

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
## Sentry Triage Report — dhis2-android-capture
Production release: <PROD_VERSION>
Generated: <today's date>
[Note if release filter was relaxed and why]

### Q1: Fix ASAP (High Impact, Low Effort)
| Issue ID | Title | Impact | Effort | Crash site | To fix |
|----------|-------|--------|--------|------------|--------|
| SENTRY-X | ...   | 5      | 1      | Foo.kt:42  | `/sentry-fix SENTRY-X` |

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
- **Crash site**: `ClassName.kt:lineN`
- **Flow**: <login | sync | data-entry | tracker | dashboard | settings | other>
- **Users affected**: <count>
- **Events**: <count> (ratio <times_seen/users_seen>)
- **Root cause hint**: <one sentence from reading the crash-site file>
- **To fix**: `/sentry-fix SENTRY-X`
```

If no issues were found even after relaxing the filter, state that clearly and suggest the
user verify that the Sentry project name and org slug are correct.
