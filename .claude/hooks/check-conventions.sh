#!/bin/bash
# PostToolUse hook: after Claude edits a Kotlin file, check the lines it added against the
# AGENTS.md agreements. Exit 2 sends the findings back to Claude so it fixes them right away.
file_path=$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input",{}).get("file_path",""))' 2>/dev/null)
[[ "$file_path" == *.kt ]] || exit 0
cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
output=$(python3 scripts/check_conventions.py --files "$file_path" 2>&1) && exit 0
echo "$output" >&2
exit 2
