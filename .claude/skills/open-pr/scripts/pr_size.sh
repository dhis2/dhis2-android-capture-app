#!/usr/bin/env bash
# Measures a PR the way CI does (scripts/check_pr_size.sh): added + deleted lines between the
# merge base of <base> and <head>, with rename detection, excluding verification-metadata.xml.
#
# Usage: pr_size.sh <base-ref> [<head-ref>=HEAD] [--files]
#   Prints "<total> <limit>" on the first line; with --files, then one "<added> <deleted> <path>"
#   line per file, largest first. Exit code 0 when total <= limit, 1 when over, 2 on error.
set -u

base="${1:-}"
head="${2:-HEAD}"
show_files=false
for arg in "$@"; do [ "$arg" = "--files" ] && show_files=true; done
[ "$head" = "--files" ] && head=HEAD

if [ -z "$base" ]; then
  echo "usage: pr_size.sh <base-ref> [<head-ref>] [--files]" >&2
  exit 2
fi

root="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "not a git repository" >&2; exit 2; }

# Same limit as CI when the repo defines one.
limit=400
if [ -f "$root/scripts/check_pr_size.sh" ]; then
  found="$(grep -m1 -E '^MAX_LINES=[0-9]+' "$root/scripts/check_pr_size.sh" | cut -d= -f2)"
  [ -n "$found" ] && limit="$found"
fi

# CI diffs against the remote branch. A stale local copy of the trunk inflates the count with
# everything the trunk gained since, so warn when the local and remote copies differ.
if git show-ref --verify --quiet "refs/heads/$base" && git show-ref --verify --quiet "refs/remotes/origin/$base"; then
  if [ "$(git rev-parse "refs/heads/$base")" != "$(git rev-parse "refs/remotes/origin/$base")" ]; then
    echo "warning: local $base differs from origin/$base; pass origin/$base for the trunk (after git fetch origin $base)" >&2
  fi
fi

merge_base="$(git merge-base "$base" "$head" 2>/dev/null)" || { echo "no merge base between $base and $head" >&2; exit 2; }

numstat="$(git diff -M --numstat "$merge_base".."$head" -- . ':(exclude)**/verification-metadata.xml')"
# Binary files report "-"; count them as 0 like CI's awk does.
total="$(printf '%s\n' "$numstat" | awk '{a = ($1 == "-") ? 0 : $1; d = ($2 == "-") ? 0 : $2; s += a + d} END {print s + 0}')"

echo "$total $limit"
if $show_files; then
  printf '%s\n' "$numstat" | awk 'NF {a = ($1 == "-") ? 0 : $1; d = ($2 == "-") ? 0 : $2; $1 = ""; $2 = ""; sub(/^  /, ""); print a + d, a, d, $0}' |
    sort -rn | cut -d' ' -f2-
fi

[ "$total" -le "$limit" ]
