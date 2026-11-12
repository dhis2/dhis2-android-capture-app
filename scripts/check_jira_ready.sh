#!/bin/bash
#
# Passes only when every ANDROAPP ticket named in a pull request title is in the
# "Ready to Merge" Jira status, or "TO_AUTOMATE" for a Zephyr Test ticket, whose
# workflow has no "Ready to Merge". Backs the required `jira-ready` check, which runs
# both on the pull request and on its merge queue group, so a ticket moved back
# out of its ready status also ejects the PR from the queue.
#
# Input (environment):
#   PR_TITLE              Title to read the ticket keys from. When empty, the title is
#                         looked up from PR_NUMBER or MERGE_GROUP_HEAD_REF.
#   PR_NUMBER             Pull request number (optional).
#   EVENT_BASE_REF        Base branch the pull_request event reports (optional).
#   MERGE_GROUP_HEAD_REF  merge_group head ref, refs/heads/gh-readonly-queue/<base>/pr-<n>-<sha>.
#   GITHUB_REPOSITORY     owner/repo, set by GitHub Actions; needed for the PR lookup.
#   GH_TOKEN              Token for the gh CLI; needed for the PR lookup.
#   JIRA_URL              Defaults to https://dhis2.atlassian.net.
#
# A title without an ANDROAPP key (dependabot, Transifex, ...) passes with a notice:
# those PRs are added to the merge queue by hand.
#
# A PR stacked on another PR passes with a notice too: GitHub reports the stack's trunk
# as the event's base branch, so the workflow's branch filter lets it through, but it
# never enters the merge queue itself. Its tickets are checked by merge_automation.py,
# and again by this check on the stack's root PR.
#
# The Jira project is world-readable, so no credentials are used. A ticket that is not
# publicly visible cannot be verified and fails the check.

set -euo pipefail

READY_STATUS="Ready to Merge"
# Zephyr Test tickets follow their own workflow (TO_AUTOMATE -> AUTOMATED_TEST) and are
# mergeable while being automated.
TEST_ISSUE_TYPE="Test"
TEST_READY_STATUS="TO_AUTOMATE"
JIRA_URL="${JIRA_URL:-https://dhis2.atlassian.net}"
PR_TITLE="${PR_TITLE:-}"
PR_NUMBER="${PR_NUMBER:-}"
EVENT_BASE_REF="${EVENT_BASE_REF:-}"

summary() {
    if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
        echo "$1" >> "$GITHUB_STEP_SUMMARY"
    fi
}

if [[ -n "$PR_NUMBER" && -n "$EVENT_BASE_REF" ]]; then
    BASE_REF=$(gh pr view "$PR_NUMBER" --repo "$GITHUB_REPOSITORY" --json baseRefName --jq .baseRefName)
    if [[ "$BASE_REF" != "$EVENT_BASE_REF" ]]; then
        echo "::notice::Stacked on $BASE_REF: not gated here. Its tickets are checked on the stack's root PR into $EVENT_BASE_REF."
        summary "Stacked on \`$BASE_REF\`: not gated here, its tickets are checked on the stack's root PR."
        exit 0
    fi
fi

if [[ -z "$PR_TITLE" ]]; then
    if [[ -z "$PR_NUMBER" && -n "${MERGE_GROUP_HEAD_REF:-}" ]]; then
        PR_NUMBER=$(sed -nE 's#^refs/heads/gh-readonly-queue/.+/pr-([0-9]+)-[0-9a-f]+$#\1#p' <<< "$MERGE_GROUP_HEAD_REF")
    fi
    if [[ -z "$PR_NUMBER" ]]; then
        echo "::error::No PR title, PR number or merge group ref to read the Jira key from."
        exit 1
    fi
    PR_TITLE=$(gh pr view "$PR_NUMBER" --repo "$GITHUB_REPOSITORY" --json title --jq .title)
fi

echo "PR title: $PR_TITLE"

mapfile -t KEYS < <(grep -oE '\bANDROAPP-[0-9]+\b' <<< "$PR_TITLE" | sort -u || true)

if [[ ${#KEYS[@]} -eq 0 ]]; then
    echo "::notice::No ANDROAPP ticket in the PR title. Not gated by Jira: add it to the merge queue manually."
    summary "No ANDROAPP ticket in the PR title: not gated by Jira, enqueue manually."
    exit 0
fi

BODY=$(mktemp)
trap 'rm -f "$BODY"' EXIT

FAILED=0
for KEY in "${KEYS[@]}"; do
    HTTP_CODE=$(curl -sS -o "$BODY" -w '%{http_code}' \
        -H 'Accept: application/json' \
        "$JIRA_URL/rest/api/3/issue/$KEY?fields=status,issuetype") || HTTP_CODE="000"

    case "$HTTP_CODE" in
        200)
            STATUS=$(jq -r '.fields.status.name' "$BODY")
            ISSUE_TYPE=$(jq -r '.fields.issuetype.name' "$BODY")
            EXPECTED="$READY_STATUS"
            if [[ "$ISSUE_TYPE" == "$TEST_ISSUE_TYPE" ]]; then
                EXPECTED="$TEST_READY_STATUS"
            fi
            if [[ "$STATUS" == "$EXPECTED" ]]; then
                echo "$KEY is in \"$STATUS\"."
                summary "- ✅ $KEY: $STATUS"
            else
                echo "::error::$KEY ($ISSUE_TYPE) is in \"$STATUS\", not \"$EXPECTED\"."
                summary "- ❌ $KEY: $STATUS (needs $EXPECTED)"
                FAILED=1
            fi
            ;;
        404)
            echo "::error::$KEY was not found, or is not publicly visible in Jira."
            summary "- ❌ $KEY: not found or not publicly visible"
            FAILED=1
            ;;
        *)
            echo "::error::Jira answered HTTP $HTTP_CODE for $KEY. Re-run the check once Jira is reachable."
            summary "- ❌ $KEY: Jira answered HTTP $HTTP_CODE"
            FAILED=1
            ;;
    esac
done

exit "$FAILED"
