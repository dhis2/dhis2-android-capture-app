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

# Print the diff statistics
echo "Diff statistics between $TARGET_BRANCH and HEAD:"
git diff --stat "$TARGET_BRANCH"..HEAD

# Get the total number of lines changed in the PR against the target branch
CHANGED_LINES=$(git diff --stat "$TARGET_BRANCH"..HEAD | tail -n1 | awk '{print $4 + $6}')

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