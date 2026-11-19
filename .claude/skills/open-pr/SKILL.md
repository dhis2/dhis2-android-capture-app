---
name: open-pr
description: >
  Opens the pull request(s) for the current branch. Measures the diff the way CI does and,
  when it exceeds the PR size limit (400 changed lines), splits the work into stacked PRs of
  at most 400 lines each with the gh-stack extension. Runs a code review of every PR, lets the
  user decide which findings to fix (and why not, for the rest), and writes the review result
  and those decisions into each PR description. Use whenever the user asks to open, create,
  submit or send a PR / pull request, or to split a branch into stacked PRs.
  Invoke as /open-pr [--base <branch>].
---

# Open PR

Opens one PR, or a stack of PRs, for the current branch. Every PR gets a code review the user
has evaluated, and the review outcome is recorded in its description. No PR is opened before the
user has seen its review and approved opening it: this skill is the only gate, so do not skip or
shorten any step.

Talk to the user in their language; write commits, PR titles and PR descriptions in English.

Whenever this skill says **ask the user**, stop and wait for an explicit answer. Offer the
choices as a short list when your environment has a way to present options, and always accept a
free-text answer.

Keep temporary files (PR bodies, notes) in a temporary directory outside the repository, never
in the working tree.

---

## 1. Preflight

Run these checks and stop with a clear message if one fails.

1. **gh is installed and authenticated**: `gh --version` and `gh auth status`. If gh is missing,
   tell the user to install the GitHub CLI; if it is not authenticated, ask them to run
   `gh auth login` themselves (it is interactive).
2. **Repository state**: `git status --short`. If there are uncommitted changes, ask the user
   whether they belong in the PR (then commit them following the conventions in
   [section 7](#7-commit-and-pr-title-conventions)) or should stay out (then leave them alone
   and work from the committed head).
3. **Base branch**: use `--base` when given. Otherwise propose the branch the work was started
   from: compare `git merge-base HEAD origin/<candidate>` for `develop`, `main` and the
   `release/*` branches and pick the most recent one; ask the user to confirm if more than one
   is plausible. Run `git fetch origin <base>` and always measure against `origin/<base>`, never
   the local copy, which may be stale.
4. **Ticket key**: take it from the branch name (`ANDROAPP-1234…`). If there is none, ask the
   user for it, or whether the PR has no ticket.

The gh-stack extension is only needed if the PR has to be split; it is checked in
[section 2](#2-measure-and-decide).

## 2. Measure and decide

Measure the diff with the bundled script, which counts like CI (`scripts/check_pr_size.sh`):
added + deleted lines since the merge base, renames detected, `verification-metadata.xml`
excluded:

```bash
bash <skill-dir>/scripts/pr_size.sh origin/<base> HEAD --files
```

The first line is `<total> <limit>`. With `--files`, one tab-separated line per file follows,
largest first: `<added> <deleted> <path>`, plus `<old-path>` for a rename. Exit code: 0 within the
limit, 1 over it, 2 on an error (bad ref, no merge base): never treat 2 as "over the limit".

- **Within the limit**: one PR. Go to [section 4](#4-code-review-of-each-pr).
- **Over the limit**: ask the user how to proceed:
  - **Split into stacked PRs** (the default), each within the limit. Check the extension first:
    `gh extension list` must list `gh stack` (`github/gh-stack`). If it is missing, ask the user
    before running `gh extension install github/gh-stack`. Then plan the split
    ([section 3](#3-split-into-stacked-prs)).
  - **Keep one PR and skip the size check**: only when the size is justified (for example, all
    new files that only make sense together). CI skips the check when the PR title or body
    contains `[skip size]`; add it to the title and say why in the description.

## 3. Split into stacked PRs

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
   intermediate state written by hand. Commit following [section 7](#7-commit-and-pr-title-conventions).
   If the current branch must be reset to become the bottom layer, ask the user first, and only
   after the backup exists.
4. Verify:
   - `git diff backup/<current-branch> <top-branch>` is empty: nothing was lost or added;
   - each layer is within the limit: `pr_size.sh <parent-branch> <branch>` (the trunk is
     `origin/<base>`);
   - each layer builds and passes lint and the tests of the modules it touches, using the
     commands documented in the repository (AGENTS.md: `ktlintCheck`, the unit test tasks of
     each module type). Confirm the tests ran, as AGENTS.md describes.

## 4. Code review of each PR

Review each PR separately, bottom to top, against its own base: the trunk for a single PR or the
bottom layer, the branch below it for the others. The scope is exactly
`git diff <base>...<branch>` (with `origin/<base>` for the trunk).

1. Review that diff for correctness bugs, security problems, regressions, missing or wrong
   tests, and violations of the repository conventions (AGENTS.md). If your environment provides
   a dedicated code-review tool or command, use it on that scope, at the effort level the user
   asks for; otherwise review the diff yourself, reading the surrounding code where a change
   depends on it. Drop any finding outside the diff, and anything you cannot point to a
   concrete failure for.
2. Present the findings to the user, numbered, with severity (High / Medium / Low), `file:line`
   and a one-line summary, and ask which ones to fix (any number of them). If there are no
   findings, say so and move on.
3. For each finding the user does **not** want fixed, ask why, suggesting reasons such as
   "False positive", "Intended behaviour" or "Out of scope, follow-up", and accepting any other.
   Record the user's reason; never write one on their behalf. If the user declines to give one,
   record "No reason given".
4. Apply the fixes the user chose, run lint and the affected tests, and commit them on that
   branch (one commit per review round, `fix: [KEY] address code review findings`). Record the
   commit sha for each fixed finding. In a stack, rebase the branches above with
   `gh stack rebase` and re-check their sizes and builds.
5. A fix can push a PR over the limit. Re-measure; if it does, tell the user and go back to the
   choice in [section 2](#2-measure-and-decide) for that PR.

Keep a record per PR: head sha, base, how it was reviewed (tool or manual, effort level), and
each finding with its decision (fixed in `<sha>` / not fixed + reason). It feeds the PR
description.

## 5. Approval

Show the user the final summary: one row per PR with branch, base, size and review outcome
(found / fixed / not fixed). Ask:

- whether to open the PRs now;
- as **draft** or **ready for review**.

Do not open anything without an explicit yes. If a branch's head changed after its review for
any reason other than the fix commit recorded in [section 4](#4-code-review-of-each-pr) (a new
commit, a rebase that brought new changes), review the new changes before asking.

## 6. Open the PRs

### PR description

Start from `.github/pull_request_template.md` and fill it in: Jira link
(`https://dhis2.atlassian.net/browse/<KEY>`), the problem and the solution. In a stack, say which
part of the stack this PR is and link the PRs below and above it once their numbers exist.
Then append:

```markdown
## Code review

Reviewed at <reviewed-sha> against `<base>` (<tool or "manual review">, <effort level if any>).

| # | Severity | Finding | Location | Decision |
|---|---|---|---|---|
| 1 | High | <one-line summary> | `path/File.kt:42` | Fixed in <sha> |
| 2 | Low | <one-line summary> | `path/Other.kt:10` | Not fixed: <user's reason> |
```

With no findings, replace the table with "No findings." Write the body to a temporary file.

### Single PR

```bash
git push -u origin HEAD
gh pr create --base <base> --title "<title>" --body-file <file> [--draft]
```

### Stack

1. `gh stack submit --auto` pushes every branch and creates the PRs (as drafts, with
   auto-generated titles) and the stack on GitHub.
2. For each PR: `gh pr edit <number> --title "<title>" --body-file <file>`, now with the links
   between the PRs of the stack.
3. If the user chose ready for review: `gh pr ready <numbers…>`.

### Check

`gh pr view <number> --json title,baseRefName,isDraft,url` for each PR, and `gh stack view` for a
stack. Report the URLs, base of each PR and the stack URL.

## 7. Commit and PR title conventions

Commit messages and PR titles follow [Conventional Commits](https://www.conventionalcommits.org/),
with this repository's ticket key at the start of the description:

```
<type>[optional scope][!]: [<KEY>] <description>
```

- **Read the current specification before choosing a type**, instead of relying on a fixed list:
  open https://www.conventionalcommits.org/, follow it to the latest version of the
  specification, and use the types it defines and the ones it points to as recommended. Note the
  version you used. If the site cannot be reached, use the types already present in
  `git log --oneline -50` and tell the user the specification could not be checked.
- Mark breaking changes the way the specification says (`!` before the colon, or a
  `BREAKING CHANGE:` footer).
- Without a ticket, leave out `[<KEY>]`.
- Do not add AI, agent or tool attribution (co-author trailers, "generated with" lines) to
  commits or PRs.
- Commit or push only within this flow, and never amend or force-push commits that are already
  on the remote without asking the user.
