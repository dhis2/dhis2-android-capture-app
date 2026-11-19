#!/usr/bin/env bash
# PreToolUse gate: blocks opening PRs until the user has evaluated a code review of each new PR's head.
# - `gh pr create`: reviews HEAD against the PR's base branch.
# - `gh stack submit`: creates one PR per stack branch without a PR yet; each one is reviewed
#   against the branch below it in the stack (the trunk for the bottom branch).
# Marker per commit lives in <git-dir>/claude-pr-review/<sha>; a new commit requires a new review.
input=$(cat)

ask() {
  printf '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"ask","permissionDecisionReason":"%s"}}\n' "$1"
  exit 0
}

deny() {
  printf '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"%s"}}\n' "$1"
  exit 0
}

is_pr_create=false
is_stack_submit=false
printf '%s' "$input" | grep -qE 'gh(\.exe)?[[:space:]]+pr[[:space:]]+create' && is_pr_create=true
printf '%s' "$input" | grep -qE 'gh(\.exe)?[[:space:]]+stack[[:space:]]+submit' && is_stack_submit=true
$is_pr_create || $is_stack_submit || exit 0

dir="$(git rev-parse --absolute-git-dir 2>/dev/null)" || exit 0
markers="$dir/claude-pr-review"

steps="2) Present the findings to the user and ask (AskUserQuestion) which ones to fix and whether to open the PRs. 3) Apply requested fixes and commit them (a new commit changes the head and needs a new review; in a stack, rebase the branches above with gh stack rebase). 4) Only after the user explicitly approves, run: mkdir -p '$markers' && touch"

if $is_pr_create; then
  sha="$(git rev-parse HEAD)"
  if [ -f "$markers/$sha" ]; then
    ask "Code review of $sha evaluated by the user. Confirm to open the PR."
  fi
  deny "PR blocked: the user must evaluate a code review of HEAD ($sha) first. 1) Run the code-review skill on the branch diff against the PR's base branch (the --base value, or the repo default branch). $steps '$markers/$sha' and retry gh pr create."
fi

# gh stack submit: list the stack branches that do not have a PR yet, with their parent branch.
stack_json="$(gh stack view --json 2>/dev/null)" || exit 0
pending="$(printf '%s\n' "$stack_json" | awk '
  /"trunk":/ { parent = $0; sub(/.*"trunk": *"/, "", parent); sub(/".*/, "", parent) }
  /"branches":/ { in_branches = 1 }
  in_branches && /"name":/ {
    name = $0; sub(/.*"name": *"/, "", name); sub(/".*/, "", name)
    merged = 0; has_pr = 0
  }
  in_branches && /"isMerged": *true/ { merged = 1 }
  in_branches && /"pr": *\{/ { has_pr = 1 }
  in_branches && /^    \}/ {
    if (name != "") {
      if (!merged && !has_pr) print name " " parent
      parent = name; name = ""
    }
  }
')"
[ -n "$pending" ] || exit 0

missing=""
reviewed=""
while read -r branch base; do
  sha="$(git rev-parse "refs/heads/$branch" 2>/dev/null)" || continue
  if [ -f "$markers/$sha" ]; then
    reviewed="$reviewed $branch($sha)"
  else
    missing="$missing $branch($sha vs $base): git diff $base...$branch;"
    touches="$touches '$markers/$sha'"
  fi
done <<< "$pending"

if [ -z "$missing" ]; then
  ask "Code review evaluated by the user for every new stacked PR:$reviewed. Confirm to submit the stack."
fi
deny "Stack submit blocked: gh stack submit opens one PR per branch below, and the user must evaluate a code review of each one first. 1) Run the code-review skill on each branch against the branch below it in the stack (one review per branch, not the whole stack diff):$missing $steps$touches and retry gh stack submit."
