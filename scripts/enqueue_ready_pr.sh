#!/bin/bash
#
# Adds the open pull requests of a Jira ticket that reached "Ready to Merge" to the
# merge queue. Called by the enqueue-ready-pr workflow, which the Jira Automation rule
# dispatches on that transition.
#
# For each open, non-draft PR into BASE_BRANCH whose title names the ticket:
#   1. re-runs its `jira-ready` check, which failed while the ticket was still in review;
#   2. enables auto-merge, so the PR joins the merge queue as soon as it is approved and
#      every required check is green.
#
# Input (environment):
#   JIRA_KEY           Ticket key, e.g. ANDROAPP-1234.
#   BASE_BRANCH        Only PRs into this branch are enqueued. Defaults to develop.
#   GITHUB_REPOSITORY  owner/repo, set by GitHub Actions.
#   GH_TOKEN           GitHub App token. Not GITHUB_TOKEN: events it causes do not start
#                      other workflows, so the merge group would get no CI.

set -euo pipefail

JIRA_KEY="${JIRA_KEY:-}"
BASE_BRANCH="${BASE_BRANCH:-develop}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ ! "$JIRA_KEY" =~ ^ANDROAPP-[0-9]+$ ]]; then
    echo "::error::Invalid Jira key '$JIRA_KEY'. Expected ANDROAPP-<number>."
    exit 1
fi

# The workflow can also be dispatched by hand: confirm the ticket really is ready.
PR_TITLE="$JIRA_KEY" "$SCRIPT_DIR/check_jira_ready.sh"

# `in:title` is a word search, so filter again for the exact key: ANDROAPP-12 must not
# match ANDROAPP-123.
PRS=$(gh pr list --repo "$GITHUB_REPOSITORY" --state open --limit 50 \
    --search "$JIRA_KEY in:title" \
    --json number,title,baseRefName,headRefOid,isDraft \
    --jq "[.[] | select(.title | test(\"(^|[^0-9A-Za-z])$JIRA_KEY(\$|[^0-9])\"))]")

if [[ $(jq 'length' <<< "$PRS") -eq 0 ]]; then
    echo "::notice::No open PR names $JIRA_KEY in its title. Nothing to enqueue."
    exit 0
fi

FAILED=0
while read -r PR; do
    NUMBER=$(jq -r '.number' <<< "$PR")
    BASE=$(jq -r '.baseRefName' <<< "$PR")
    SHA=$(jq -r '.headRefOid' <<< "$PR")
    DRAFT=$(jq -r '.isDraft' <<< "$PR")

    if [[ "$BASE" != "$BASE_BRANCH" ]]; then
        echo "::notice::#$NUMBER targets $BASE, not $BASE_BRANCH. Left for a manual merge."
        continue
    fi
    if [[ "$DRAFT" == "true" ]]; then
        echo "::warning::#$NUMBER is a draft. Mark it ready for review and dispatch again."
        continue
    fi

    RUN=$(gh run list --repo "$GITHUB_REPOSITORY" --workflow jira-ready.yml \
        --commit "$SHA" --event pull_request --limit 1 \
        --json databaseId,status --jq '.[0] // empty')
    if [[ -z "$RUN" ]]; then
        echo "::warning::#$NUMBER has no jira-ready run on $SHA. Push or edit the PR to start one."
    elif [[ $(jq -r '.status' <<< "$RUN") == "completed" ]]; then
        gh run rerun "$(jq -r '.databaseId' <<< "$RUN")" --repo "$GITHUB_REPOSITORY"
        echo "#$NUMBER: re-running jira-ready."
    else
        echo "#$NUMBER: jira-ready is already running."
    fi

    # On a branch protected by a merge queue, --auto queues the PR once its
    # requirements are met; the queue applies its own merge method.
    if gh pr merge "$NUMBER" --repo "$GITHUB_REPOSITORY" --auto --squash; then
        echo "#$NUMBER: will join the merge queue once approved and green."
    else
        echo "::error::Could not enable auto-merge on #$NUMBER."
        FAILED=1
    fi
done < <(jq -c '.[]' <<< "$PRS")

exit "$FAILED"
