# Sentry setup — shared by `/sentry-triage` and `/sentry-fix`

## 1. Plugin check

Both skills need the `sentry@claude-plugins-official` plugin. If
`mcp__plugin_sentry_sentry__find_organizations` is not an available tool, stop
and tell the user:

> The Sentry MCP plugin is not enabled in this session. Enable **Sentry** under
> `/config` → Extensions → Plugins, or add this to `~/.claude/settings.json`
> (user-level, not committed):
> ```json
> { "enabledPlugins": { "sentry@claude-plugins-official": true } }
> ```
> Then restart the session and run the command again.

The plugin exposes a small core tool set plus a catalog. Anything not listed as
a tool (releases, issue events, `link_issue`, …) is found with
`search_sentry_tools(query: "…")` and run with
`execute_sentry_tool(name, arguments)`.

## 2. Org and project

1. `find_organizations` — one org → use it; several → the one whose slug matches
   `gh repo view --json owner -q .owner.login`. Store `ORG_SLUG` and its
   `regionUrl` as `REGION_URL`.
2. `find_projects(organizationSlug: ORG_SLUG)` — pick the Android capture
   project (slug or name contains `android` / `capture`). Store `PROJECT_SLUG`.

Issue URL: `https://<ORG_SLUG>.sentry.io/issues/<SHORT-ID>/`.

**Bare suffix input** (e.g. `83NS`): the issue lookup rejects it. Retry as
`<PROJECT_SLUG uppercased>-<suffix>`, e.g. `DHIS2-ANDROID-CAPTURE-83NS`, and use
that full short ID everywhere.
