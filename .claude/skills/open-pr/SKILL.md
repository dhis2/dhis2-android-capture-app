---
name: open-pr
description: >
  Opens the pull request(s) for the current branch. Measures the diff the way CI does and,
  when it exceeds the PR size limit (400 changed lines), splits the work into stacked PRs of
  at most 400 lines each with the gh-stack extension. Runs a code review of every PR, lets the
  user decide which findings to fix (and why not, for the rest), and writes the review result
  and those decisions into each PR description. Makes sure the PR review gate hook is installed
  first. Use whenever the user asks to open, create, submit or send a PR / pull request, or to
  split a branch into stacked PRs. Invoke as /open-pr [--base <branch>].
---

# Open PR

Opens one PR, or a stack of PRs, for the current branch. Every PR gets a code review the user
has evaluated, and the review outcome is recorded in its description.

Talk to the user in their language; write commits, PR titles and PR descriptions in English.

**Never type the literal PR-creation command in a command that does not open a PR** (a grep, an
echo, a test): the review gate hook matches the command text and denies it. Write PR bodies to a
file in the scratchpad and pass them with `--body-file`.

---

## 1. Preflight

Run these checks and stop with a clear message if one fails.

1. **gh is installed and authenticated**: `gh --version` and `gh auth status`. If gh is missing,
   tell the user to install the GitHub CLI; if it is not authenticated, suggest they run
   `! gh auth login` themselves (it is interactive).
2. **Repository state**: `git status --short`. If there are uncommitted changes, ask the user
   whether they belong in the PR (then commit them following the commit conventions below) or
   should stay out (then leave them alone and work from the committed head).
3. **Base branch**: use `--base` when given. Otherwise propose the branch the work was started
   from: compare `git merge-base HEAD origin/<candidate>` for `develop`, `main` and the
   `release/*` branches and pick the most recent one; confirm it with the user if more than one
   is plausible. Run `git fetch origin <base>` and always measure against `origin/<base>`, never
   the local copy, which may be stale.
4. **Ticket key**: take it from the branch name (`ANDROAPP-1234…`). If there is none, ask for it.
5. **Review gate hook**: follow [section 2](#2-review-gate-hook).

The gh-stack extension is only needed if the PR has to be split; it is checked in
[section 3](#3-measure-and-decide).

## 2. Review gate hook

The gate is a `PreToolUse` hook that denies PR creation until a marker file exists for the head
commit of each new PR (`<git-dir>/claude-pr-review/<sha>`). This skill writes those markers only
after the user has evaluated the review, so the hook is a backstop if the flow is skipped. The
hook fails closed: when it cannot tell which commits a command would open PRs for (a failing
`gh stack view`, several `cd`s, a branch with no local ref), it denies. It needs Python to read
JSON; without it, `gh stack submit` is always denied.

**Detect it.** Look for a hook command containing `pr-review-gate` in, in this order:
`.claude/settings.json`, `.claude/settings.local.json` (repo) and `~/.claude/settings.json`
(user). For the first match, check that the script it points to exists, and compare it with the
bundled copy `scripts/pr-review-gate.sh` in this skill's directory (`cmp`).

- **Installed and identical**: continue.
- **Installed but different**: check that it handles stacks
  (`grep -qF 'stack[[:space:]]+submit' <script>`). If it does not, or the user wants the bundled
  version, offer to replace it. Do not overwrite it without asking: the user may have local
  changes.
- **Not installed**: ask the user (AskUserQuestion) where to install it:
  - **Repository** (shared with the team through git): copy the script to
    `.claude/hooks/pr-review-gate.sh` and merge this entry into `.claude/settings.json`,
    keeping every existing key:
    ```json
    "hooks": {
      "PreToolUse": [
        {
          "matcher": "Bash|PowerShell",
          "hooks": [
            {
              "type": "command",
              "command": "bash \"$CLAUDE_PROJECT_DIR/.claude/hooks/pr-review-gate.sh\"",
              "timeout": 15,
              "statusMessage": "Checking PR review gate..."
            }
          ]
        }
      ]
    }
    ```
    These two files are a change to the repo: they go in their own commit, and the user decides
    whether that commit is part of this PR.
  - **User only** (`~/.claude`): copy the script to `~/.claude/hooks/pr-review-gate.sh` and
    merge the same entry into `~/.claude/settings.json`, with the command
    `bash ~/.claude/hooks/pr-review-gate.sh` (use the absolute path on Windows).

  Validate the JSON after editing it. Tell the user that a newly installed hook may only take
  effect in a new session; the flow below applies either way.

## 3. Measure and decide

Measure the diff with the bundled script, which counts like CI (`scripts/check_pr_size.sh`):
added + deleted lines since the merge base, renames detected, `verification-metadata.xml`
excluded:

```bash
bash <skill-dir>/scripts/pr_size.sh origin/<base> HEAD --files
```

The first line is `<total> <limit>`. With `--files`, one tab-separated line per file follows,
largest first: `<added> <deleted> <path>`, plus `<old-path>` for a rename. Exit code: 0 within the
limit, 1 over it, 2 on an error (bad ref, no merge base): never treat 2 as "over the limit".

- **Within the limit**: one PR. Go to [section 5](#5-code-review-of-each-pr).
- **Over the limit**: it must be split into stacked PRs, each within the limit.
  1. Check the extension: `gh extension list` must list `gh stack` (`github/gh-stack`). If it is
     missing, ask the user before running `gh extension install github/gh-stack`.
  2. Plan the split ([section 4](#4-split-into-stacked-prs)).

## 4. Split into stacked PRs

### Plan

Use the per-file list from `pr_size.sh --files` to group the changes into layers, bottom to top.
Each layer must:

- be within the limit **measured against the layer below it** (CI measures each stacked PR
  against its own base);
- build and pass its tests on its own: a layer can only depend on layers below it;
- make sense to review alone: setup or infrastructure first, then moves and renames, then
  behaviour changes, then the code that uses them.

Keep moves as renames (move with `git mv`, change little in the same commit) so they cost a few
lines instead of the whole file twice. If a single file is too large for any layer, say so and
propose how to split that change (for example, an interface first and its users next).

Present the plan to the user before touching any branch: layers, files and estimated lines of
each, and the branch names. Name the bottom branch after the current branch and the others
`<current-branch>-<short-slug>`. Wait for approval.

### Build the stack

1. Create a backup ref of the current head and tell the user its name:
   `git branch backup/<current-branch> HEAD`.
2. Create the branches with `gh stack init --base <base> <branch-1> <branch-2> …` (it adopts
   existing branches and creates the missing ones, each on top of the previous one).
3. For each layer, on its branch: bring in that layer's files from the backup head
   (`git restore --source=backup/<current-branch> --staged --worktree -- <paths>`, `git rm` for
   deletions, and `git mv` for moves so renames are kept). Files split across layers need their
   intermediate state written by hand. Commit with the commit conventions below.
   If the current branch must be reset to become the bottom layer, confirm it with the user
   first and only after the backup exists.
4. Verify:
   - `git diff backup/<current-branch> <top-branch>` is empty: nothing was lost or added;
   - each layer is within the limit: `pr_size.sh <parent-branch> <branch>` (the trunk is
     `origin/<base>`);
   - each layer builds and passes lint and the tests of the modules it touches, using the
     commands in AGENTS.md / CLAUDE.md (`ktlintCheck`, the unit test tasks of each module type).
     Confirm the tests ran, as AGENTS.md describes.

## 5. Code review of each PR

Review each PR separately, bottom to top, against its own base: the trunk for a single PR or the
bottom layer, the branch below it for the others.

1. Run the `code-review` skill on the branch. State the scope in the arguments: only
   `git diff <base>...<branch>` (with `origin/<base>` for the trunk). Use the effort level the
   user asks for; otherwise the skill's default. Discard any finding outside that diff.
2. Present the findings to the user, numbered, with severity, `file:line` and a one-line summary.
   Ask (AskUserQuestion, multiSelect) which ones to fix. If there are no findings, say so and
   move on.
3. For each finding the user does **not** want fixed, ask why (AskUserQuestion with options such
   as "False positive", "Intended behaviour", "Out of scope, follow-up", plus free text). Record
   the user's reason; never write one on their behalf. If the user declines to give one, record
   "No reason given".
4. Apply the fixes the user chose, run lint and the affected tests, and commit them on that
   branch (one commit per review round, `fix: [KEY] address code review findings`). Record the
   commit sha for each fixed finding. In a stack, rebase the branches above with
   `gh stack rebase` and re-check their sizes and builds.
5. A fix can push a layer over the limit. Re-measure; if it does, tell the user and move the
   overflow into a new or existing layer before going on.

Keep a record per PR: head sha, base, review effort level, and each finding with its decision
(fixed in `<sha>` / not fixed + reason). It feeds the PR description.

## 6. Approval and markers

Show the user the final summary: one row per PR with branch, base, size and review outcome
(found / fixed / not fixed). Ask (AskUserQuestion):

- whether to open the PRs now;
- as **draft** or **ready for review**.

Only after an explicit yes, write a marker for the head of each PR that is about to be created:

```bash
d="$(git rev-parse --absolute-git-dir)/claude-pr-review"; mkdir -p "$d" && touch "$d/<sha>"
```

A head that changed after its review (a new commit, a rebase) needs a new review: check that each
branch's current sha is the one that was reviewed before writing its marker.

## 7. Open the PRs

### PR description

Start from `.github/pull_request_template.md` and fill it in: Jira link
(`https://dhis2.atlassian.net/browse/<KEY>`), the problem and the solution. In a stack, say which
part of the stack this PR is and link the PRs below and above it once their numbers exist.
Then append:

```markdown
## Code review

Reviewed with `/code-review <level>` at <reviewed-sha> against `<base>`.

| # | Severity | Finding | Location | Decision |
|---|---|---|---|---|
| 1 | High | <one-line summary> | `path/File.kt:42` | Fixed in <sha> |
| 2 | Low | <one-line summary> | `path/Other.kt:10` | Not fixed: <user's reason> |
```

With no findings, replace the table with "No findings." Write the body to a file in the
scratchpad.

### Single PR

```bash
git push -u origin HEAD
gh pr create --base <base> --title "<type>: [<KEY>] <summary>" --body-file <file> [--draft]
```

### Stack

1. `gh stack submit --auto` pushes every branch and creates the PRs (as drafts, with
   auto-generated titles) and the stack on GitHub.
2. For each PR: `gh pr edit <number> --title "<type>: [<KEY>] <summary>" --body-file <file>`,
   now with the links between the PRs of the stack.
3. If the user chose ready for review: `gh pr ready <numbers…>`.

### Check

`gh pr view <number> --json title,baseRefName,isDraft,url` for each PR, and `gh stack view` for a
stack. Report the URLs, base of each PR and the stack URL.

## Commit and PR conventions

- Commits and PR titles: `<type>: [<KEY>] <summary>` (`feat`, `fix`, `refactor`, `chore`,
  `test`, `docs`), as in `git log`.
- No AI attribution in commits or PRs (the repo's `.claude/settings.json` disables it).
- Commit or push only within this flow: never amend or force-push commits that are already
  on the remote without asking.
