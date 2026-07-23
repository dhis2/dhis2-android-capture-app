---
name: update-translations
description: >
  Sync Transifex translations for the DHIS2 Android Capture App on BOTH the
  develop and main branches in parallel, talking directly to the Transifex REST
  API (no tx CLI). For each branch it spins up an isolated git worktree, pushes
  the branch's sources to its own Transifex resources (develop--* / main--*),
  pulls the translations for every language that has any key translated, applies
  the KMP escaped-character fix, cleans up empty stubs, and opens a pull request.
  Invoke for "update translations", "sync Transifex", "pull the latest
  translations", or "push sources to Transifex".
---

# Update Translations (Transifex sync)

Push sources and pull translations for **both** `develop` and `main`, each in its
own worktree, ending in a PR per branch. The two branches map to **separate**
Transifex resources — a `--branch develop` push only touches `develop--*`, a
`--branch main` push only touches `main--*` — so both runs are safe in parallel.

Everything goes through one config-driven helper that talks to the Transifex v3
REST API directly (no `tx` CLI, no `~/.transifexrc`):

```
.claude/skills/update-translations/scripts/transifex_sync.py \
    <push-sources|verify-push|lang-list|pull|postpull|push-translations>  --branch <develop|main>
```

It parses `.tx/config` for the resource list, source files, `file_filter`
patterns, `lang_map` and `source_lang` — nothing about the modules is hardcoded,
so it stays correct as resources are added or removed.

## Prerequisites (check first; stop and tell the user if any is missing)

- **Python 3** (standard library only — no pip installs).
- **Transifex API token** in `local.properties` as `TX_TOKEN=...` (already there
  alongside `SONAR_TOKEN` etc.). `local.properties` is gitignored, so it exists
  only in the primary checkout — see the token step below for worktrees. The
  helper also accepts `$TX_TOKEN` from the environment.
- **gh CLI** authenticated (`gh auth status`) for opening PRs.
- A clean primary working tree is NOT required — work happens in throwaway
  worktrees on fresh branches, never disturbing the user's checkout.

## Orchestration (you, the invoking agent)

1. Resolve the stable paths, the token, and a datestamp up front (scripts can't
   read the clock; you pass values in):
   ```bash
   REPO="$(git rev-parse --show-toplevel)"
   SCRIPT="$REPO/.claude/skills/update-translations/scripts/transifex_sync.py"
   DATE="$(date +%Y%m%d)"
   git -C "$REPO" fetch origin
   ```
2. Create one worktree per branch on a fresh sync branch off the **remote** tip
   (basing on `origin/<branch>` avoids the "branch already checked out" error and
   guarantees the newest sources):
   ```bash
   git -C "$REPO" worktree add -b "develop-transifex-$DATE" "$REPO/../tx-sync-develop-$DATE" origin/develop
   git -C "$REPO" worktree add -b "main-transifex-$DATE"    "$REPO/../tx-sync-main-$DATE"    origin/main
   ```
   > The `develop-transifex-*` name intentionally matches the existing
   > `fix-kmp-translations.yml` action, which then runs on that PR as a CI safety
   > net. `main` has no such action, which is exactly why this skill applies the
   > escaping fix itself for both branches.
3. Spawn **two `general-purpose` agents in parallel** (both Agent calls in one
   message), one per branch. Give each: `$REPO` (so it can read the token — the
   worktrees have no `local.properties`), its worktree path, its branch name, its
   sync-branch name, and `$SCRIPT`. Pass the per-branch procedure below verbatim.
4. When both return, report both PR URLs and each `postpull` summary. Then remove
   the worktrees (branches already live on origin):
   ```bash
   git -C "$REPO" worktree remove --force "$REPO/../tx-sync-develop-$DATE"
   git -C "$REPO" worktree remove --force "$REPO/../tx-sync-main-$DATE"
   ```

## Per-branch procedure (each worktree agent runs this from its worktree)

Substitute `<BRANCH>` (`develop`/`main`), `<SYNC_BRANCH>`
(`<BRANCH>-transifex-<DATE>`), `<WT>` (worktree path), `<REPO>` (primary repo),
`<SCRIPT>` (absolute helper path).

```bash
# 0. Token: worktrees have no local.properties, so read it from the primary repo.
export TX_TOKEN="$(grep -E '^TX_TOKEN=' "<REPO>/local.properties" | cut -d= -f2-)"
[ -n "$TX_TOKEN" ] || { echo "TX_TOKEN missing in <REPO>/local.properties"; exit 1; }
cd "<WT>"

# 1. Push this branch's sources to its own <BRANCH>--* resources (async upload).
#    Reports created/updated/deleted/skipped and any server-side string errors;
#    non-zero exit on failure.
python3 "<SCRIPT>" push-sources --branch <BRANCH>

# 2. Independent verification: every local source key (plurals/string-arrays
#    included, translatable="false" excluded) must be present on the server.
#    Non-zero exit = a key is missing (silent failure) -> STOP, do NOT pull.
python3 "<SCRIPT>" verify-push --branch <BRANCH>

# 3. Languages with >=1 translated string on this branch (source lang excluded).
LANGS="$(python3 "<SCRIPT>" lang-list --branch <BRANCH>)"
echo "pulling languages: $LANGS"

# 4. Pull translations (default mode, parallel async downloads; ~4-6 min for the
#    full set). Run it in the FOREGROUND with a long timeout (up to 10 min). Do
#    NOT background it inside a sub-agent — the agent may end its turn before the
#    pull finishes and leave the download orphaned. New empty-stub files are
#    skipped automatically.
python3 "<SCRIPT>" pull --branch <BRANCH> --langs "$LANGS"

# 5. Clean up + validate:
#    - unescape \? -> ? in KMP compose-resource modules (build breaker),
#    - delete new EMPTY (0-string) stub files + their folders,
#    - revert whitespace-only churn on already-empty files,
#    - RESTORE any live (in-source) translation the pull dropped, from HEAD,
#    - validate XML + that keys are a subset of source.
#    Non-zero exit = validation failed -> STOP and report.
python3 "<SCRIPT>" postpull

# 6. Sanity-check the diff, then commit, push, and open the PR.
git add -A
git status --short | head
git commit -m "chore(translations): sync <BRANCH> translations from Transifex"
git push -u origin <SYNC_BRANCH>

# Repo rule: a PR may not exceed 400 changed lines (insertions + deletions)
# unless its title ends with "[skip size]". Translation syncs almost always
# exceed this, so compute the total and append the tag when needed.
CHANGES=$(git diff --numstat "origin/<BRANCH>" HEAD | awk '{a+=$1; d+=$2} END {print a+d+0}')
TITLE="chore(translations): sync <BRANCH> translations from Transifex"
[ "$CHANGES" -gt 400 ] && TITLE="$TITLE [skip size]"
echo "changed lines: $CHANGES  ->  title: $TITLE"

gh pr create --base <BRANCH> --head <SYNC_BRANCH> \
  --title "$TITLE" \
  --body "Automated Transifex sync for \`<BRANCH>\`.

- Pushed \`<BRANCH>\` sources to the \`<BRANCH>--*\` resources (verified: no keys missing on the server).
- Pulled translations for every language with at least one translated key.
- Applied the KMP escaped-question-mark fix (\`\\?\` -> \`?\`).
- Removed empty stub files; preserved all in-source translations.

<paste the postpull summary + \`git diff --shortstat\` here>"
```

Report back to the orchestrator: the PR URL, the `postpull` summary, and
`git diff --shortstat`. If any step exits non-zero, report the failure instead of
opening a PR.

## Why each step exists (learned pitfalls — don't drop these)

- **Silent push failures**: `push-sources` waits for each async upload job and
  surfaces its `errors` + created/updated/deleted counts, and `verify-push` then
  independently confirms every local key reached the server. Count keys with a
  regex covering `<string>` **and** `<plurals>`/`<string-array>` (the helper
  does) — plain `grep '<string '` under-counts and produces false orphan reports.
- **Only pull real languages**: `lang-list` restricts to languages that actually
  have a translation on that branch. Do **not** add a percentage floor — it would
  drop languages with a single translated key (e.g. `fi`, `ko_KR`).
- **KMP escaped chars can't be fixed on the server**: Transifex stores the KMP
  compose modules (`tracker`, `aggregates`, `commonskmm`, `login`, `sync`) as
  ANDROID resources and re-escapes `?` to `\?` on every export; the Compose
  resource parser then fails to compile. So `\?`->`?` is a mandatory **local,
  post-pull** step every time. `postpull` fixes every KMP file, so it also
  self-heals any `\?` already committed on the branch.
- **Empty files are noise**: `pull` won't create a new empty stub, and `postpull`
  deletes any that slip through and reverts whitespace-only churn on modules that
  commit empty placeholders (e.g. `sync`).
- **develop vs main divergence**: translators work against `develop--`, so
  `main--` can be missing in-source translations the repo already has; a default
  pull would silently drop them. `postpull` restores any such dropped in-source
  translation from HEAD and lists them — if that list is non-empty, run the
  `push-translations` backfill (see below) so the gap stops recurring instead of
  being re-restored on every sync.
- **PR size limit**: this repo rejects PRs over **400 changed lines (insertions +
  deletions)** unless the title ends with **`[skip size]`**. A translation sync is
  almost always far larger than that, so step 6 computes the total and appends the
  tag automatically. Any PR the skill opens (or that you open by hand for this
  work) must follow the same rule.

## Compilation & tests (delegated to CI)

The skill does **static** validation only (`postpull`: well-formed XML, keys ⊆
source, and the `\?` fix). It does **not** build or run tests, by design:

- **CI is the compile gate.** `continuous-delivery.yml` runs
  `./gradlew assembleDhis2Debug` on every PR to `main`/`develop`/`release/*`,
  which compiles the KMP Compose resources — so any escaping/resource problem
  that slipped past the static checks fails the PR build automatically.
- **Worktrees can't build as-is:** `local.properties` is gitignored, so the
  throwaway worktrees have no `sdk.dir`; a full KMP build per branch would also
  add several minutes for little gain over CI.

If you want a local pre-PR compile check, fetch the sync branch into the primary
checkout (which has your SDK config) and run `./gradlew assembleDhis2Debug`.
Translation-only changes don't touch logic, so the unit-test suite isn't run.

## Backfilling the server (`push-translations`) — closes the divergence

When `postpull` reports "the server was missing these in-source translations",
the server and repo have drifted: the repo carries translations the `<branch>--`
resources don't have, so every sync re-drops and re-restores the same keys. The
`push-translations` subcommand fixes this at the source by pushing those repo
translations **up** to the server:

```bash
# dry-run first — lists every (resource, language) gap, uploads nothing
python3 <SCRIPT> push-translations --branch main

# then actually upload
python3 <SCRIPT> push-translations --branch main --apply
```

It is **gap-only and non-destructive**: for each repo translation file it
downloads the server's current translations and uploads *only* the keys that are
(a) in the source, (b) translated in the repo, and (c) absent on the server —
so it can never overwrite a translator's existing work. On a per-language
download error it skips that pair rather than risk clobbering it.

Run it from a checkout that has the branch's translations (e.g. a `main`
worktree) with `TX_TOKEN` exported. It is safe to re-run: once a gap is filled
the next dry-run reports zero. Typically only `main` needs it, since translators
already work against `develop--`.

## Running for a single branch

Skip the orchestration fan-out: create one worktree and run the per-branch
procedure for that branch only. Everything else is identical.

## Quick local checks (no worktree, no side effects)

From the primary checkout, the read-only subcommands are safe to run directly:

```bash
python3 .claude/skills/update-translations/scripts/transifex_sync.py lang-list   --branch main
python3 .claude/skills/update-translations/scripts/transifex_sync.py verify-push --branch develop
```
