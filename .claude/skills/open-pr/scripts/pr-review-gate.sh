#!/usr/bin/env bash
# PreToolUse gate: blocks opening PRs until the user has evaluated a code review of each new PR's head.
# - `gh pr create`: the PR's head commit (--head, or HEAD) needs a review.
# - `gh stack submit`: creates one PR per stack branch without a PR yet; each one needs a review
#   against the branch below it in the stack (the trunk for the bottom branch).
# A review counts once a marker exists in <git-dir>/claude-pr-review/<sha>. The /open-pr skill
# writes markers after the user approves; a new commit needs a new review.
# Whenever the target cannot be determined, the gate denies (fails closed).
input=$(cat)

json_escape() {
  printf '%s' "$1" | tr '\r\n\t' '   ' | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'
}

decide() {
  printf '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"%s","permissionDecisionReason":"%s"}}\n' \
    "$1" "$(json_escape "$2")"
  exit 0
}

python_bin=""
for candidate in python3 python; do
  if command -v "$candidate" >/dev/null 2>&1 && "$candidate" -c 'import json' >/dev/null 2>&1; then
    python_bin="$candidate"
    break
  fi
done

# Only the command itself is matched, not the rest of the tool input. Without Python the whole
# input is matched instead, which can only block more, never less.
command_text="$input"
if [ -n "$python_bin" ]; then
  parsed="$(printf '%s' "$input" | "$python_bin" -c 'import json, sys; print(json.load(sys.stdin).get("tool_input", {}).get("command", ""))' 2>/dev/null)" &&
    command_text="$parsed"
fi

# gh, optionally gh.exe or a quoted path to it, optionally followed by global flags (--repo x).
gh_prefix='gh(\.exe)?["'"'"']?[[:space:]]+(-[^[:space:]]+[[:space:]]+([^-[:space:]][^[:space:]]*[[:space:]]+)?)*'
is_pr_create=false
is_stack_submit=false
printf '%s' "$command_text" | grep -qE "${gh_prefix}pr[[:space:]]+create" && is_pr_create=true
printf '%s' "$command_text" | grep -qE "${gh_prefix}stack[[:space:]]+submit" && is_stack_submit=true
$is_pr_create || $is_stack_submit || exit 0

howto="Use the /open-pr skill: it runs a code review of each PR, asks the user which findings to fix and records the user's approval. Do not record the approval yourself."

# The command may change directory to another repository or worktree first. One cd is followed;
# anything the gate cannot follow is denied.
cd_pattern='(^|[;&|({[:space:]])(cd|pushd|Set-Location|sl)[[:space:]]+'
cd_count="$(printf '%s' "$command_text" | grep -oE "$cd_pattern" | wc -l)"
repo_dir="."
if printf '%s' "$command_text" | grep -qE 'git[[:space:]]+-C[[:space:]]|--git-dir|GIT_DIR=|GIT_WORK_TREE='; then
  decide deny "PR blocked: the command points git at another directory, so the review gate cannot tell which commit the PR is opened from. Run it from the branch's working directory."
elif [ "$cd_count" -gt 1 ]; then
  decide deny "PR blocked: the command changes directory more than once, so the review gate cannot tell which commit the PR is opened from. Use a single cd, or none."
elif [ "$cd_count" -eq 1 ]; then
  repo_dir="$(printf '%s' "$command_text" | sed -nE "s/.*${cd_pattern}(-Path[[:space:]]+)?[\"']?([^\"';&|[:space:]]+).*/\4/p" | head -1)"
  repo_dir="${repo_dir/#\~/$HOME}"
  [ -n "$repo_dir" ] && [ -d "$repo_dir" ] ||
    decide deny "PR blocked: the review gate cannot resolve the directory the command changes to ($repo_dir)."
fi

git_in() { git -C "$repo_dir" "$@"; }

dir="$(git_in rev-parse --absolute-git-dir 2>/dev/null)" ||
  decide deny "PR blocked: the review gate cannot find the git repository of this command. Run it from the repository's working directory."
markers="$dir/claude-pr-review"

if $is_pr_create; then
  head_ref="$(printf '%s' "$command_text" | sed -nE 's/.*(--head|-H)[[:space:]=]+["'"'"']?([^"'"'"'[:space:]]+).*/\2/p' | head -1)"
  if [ -n "$head_ref" ]; then
    head_ref="${head_ref#*:}"  # owner:branch
    sha="$(git_in rev-parse --verify --quiet "refs/heads/$head_ref")" ||
      decide deny "PR blocked: the head branch $head_ref is not a local branch, so the review gate cannot check its review. Check it out locally first."
  else
    head_ref="HEAD"
    sha="$(git_in rev-parse --verify --quiet HEAD)" || decide deny "PR blocked: HEAD cannot be resolved."
  fi
  if [ -f "$markers/$sha" ]; then
    decide ask "Code review of $head_ref ($sha) evaluated by the user. Confirm to open the PR."
  fi
  decide deny "PR blocked: the user has not evaluated a code review of $head_ref ($sha). $howto"
fi

# gh stack submit: every stack branch without a PR yet, with the branch below it.
[ -n "$python_bin" ] ||
  decide deny "Stack submit blocked: the review gate needs Python to read the stack (gh stack view --json)."
stack_json="$(cd "$repo_dir" && gh stack view --json 2>/dev/null)" ||
  decide deny "Stack submit blocked: gh stack view --json failed, so the review gate cannot tell which PRs would be created."
pending="$(printf '%s' "$stack_json" | "$python_bin" -c '
import json, sys
stack = json.load(sys.stdin)
parent = stack["trunk"]
for branch in stack["branches"]:
    if not branch.get("isMerged") and not branch.get("pr"):
        print(branch["name"] + "\t" + parent)
    parent = branch["name"]
' 2>/dev/null)" ||
  decide deny "Stack submit blocked: the output of gh stack view --json could not be read."
[ -n "$pending" ] || exit 0

missing=""
reviewed=""
while IFS=$'\t' read -r branch base; do
  if ! sha="$(git_in rev-parse --verify --quiet "refs/heads/$branch")"; then
    missing="$missing $branch (no local branch; check it out);"
  elif [ -f "$markers/$sha" ]; then
    reviewed="$reviewed $branch ($sha);"
  else
    missing="$missing $branch ($sha, against $base);"
  fi
done <<< "$pending"

if [ -z "$missing" ]; then
  decide ask "Code review evaluated by the user for every new stacked PR:$reviewed Confirm to submit the stack."
fi
decide deny "Stack submit blocked: gh stack submit opens one PR per branch, and the user has not evaluated a code review of:$missing Each branch is reviewed against the branch below it. $howto"
