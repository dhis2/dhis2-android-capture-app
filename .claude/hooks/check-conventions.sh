#!/bin/bash
# PostToolUse hook: after Claude edits a Kotlin file, check the lines it added against the
# AGENTS.md agreements. Exit 2 sends the findings back to Claude so it fixes them right away.
file_path=$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input",{}).get("file_path",""))' 2>/dev/null)
[[ "$file_path" == *.kt && -f "$file_path" ]] || exit 0
# Run from the file's own checkout, so files in a git worktree are checked against that worktree.
root=$(cd "$(dirname "$file_path")" && git rev-parse --show-toplevel 2>/dev/null) || exit 0
[[ -f "$root/scripts/check_conventions.py" ]] || exit 0
cd "$root" || exit 0
output=$(python3 scripts/check_conventions.py --files "$file_path" 2>&1)
[[ $? -eq 1 ]] || exit 0  # 0 = clean; 2 = git problem, not the edit's fault
echo "$output" >&2
exit 2
