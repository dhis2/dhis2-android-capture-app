#!/bin/bash

MAX_LINES=400

echo "Current branch: $GIT_BRANCH"
echo "Target branch: $GIT_BRANCH_DEST"

# Ensure we fetch the current PR source branch and the target branch
echo "Fetching branches..."
git fetch --no-tags origin "$GIT_BRANCH" "$GIT_BRANCH_DEST"

# Get the target branch dynamically from Jenkins
TARGET_BRANCH="${GIT_BRANCH_DEST}"

# If there's no target branch, assume it's a CI build on a long-lived branch
if [[ -z "$TARGET_BRANCH" ]]; then
    echo "No target branch detected. Assuming this is a CI build on a long-lived branch. Skipping PR size check."
    exit 0
fi

# Ensure the target branch exists in the local repo, if not, fetch it explicitly
if ! git show-ref --verify --quiet "refs/heads/$TARGET_BRANCH"; then
    echo "Target branch '$TARGET_BRANCH' not found locally. Fetching..."
    git fetch --no-tags origin "$TARGET_BRANCH:$TARGET_BRANCH"

    # Check again after fetching
    if ! git show-ref --verify --quiet "refs/heads/$TARGET_BRANCH"; then
        echo "ERROR: Target branch '$TARGET_BRANCH' does not exist even after fetch."
        exit 1
    fi
fi

# Find the merge base (common ancestor) between target branch and HEAD
MERGE_BASE=$(git merge-base "$TARGET_BRANCH" HEAD)

if [[ -z "$MERGE_BASE" ]]; then
    echo "ERROR: Could not find merge base between $TARGET_BRANCH and HEAD"
    exit 1
fi

echo "Merge base: $MERGE_BASE"

# Print the diff statistics from merge base to HEAD (only your changes)
echo "Diff statistics from merge base to HEAD (PR changes only):"
git diff --stat "$MERGE_BASE"..HEAD

# Get the total number of lines changed in the PR from the merge base
CHANGED_LINES=$(git diff --stat "$MERGE_BASE"..HEAD | tail -n1 | awk '{print $4 + $6}')

# Handle cases where no changes are detected
if [[ -z "$CHANGED_LINES" ]]; then
    CHANGED_LINES=0
fi

# Validate against the max limit
if [[ "$CHANGED_LINES" -gt "$MAX_LINES" ]]; then
    echo "ERROR: PR too large ($CHANGED_LINES lines changed). Limit: $MAX_LINES."
    exit 1
fi

echo "PR size is acceptable: $CHANGED_LINES lines changed."
exit 0