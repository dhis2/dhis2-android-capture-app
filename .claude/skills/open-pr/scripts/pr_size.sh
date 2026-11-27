#!/usr/bin/env bash
# Measures a PR the way CI does (scripts/check_pr_size.sh): added + deleted lines between the
# merge base of <base> and <head>, with rename detection, excluding verification-metadata.xml.
#
# Usage: pr_size.sh [--files] <base-ref> [<head-ref>]     (head defaults to HEAD)
#   First line: "<total> <limit>".
#   With --files, then one line per file, largest first, tab-separated:
#     <added>\t<deleted>\t<path>              for a changed, added or deleted file
#     <added>\t<deleted>\t<new-path>\t<old-path>   for a rename
# Exit code: 0 when total <= limit, 1 when over the limit, 2 on a usage or git error.
set -u

usage() {
  echo "usage: pr_size.sh [--files] <base-ref> [<head-ref>]" >&2
  exit 2
}

show_files=false
refs=()
for arg in "$@"; do
  case "$arg" in
    --files) show_files=true ;;
    -*) echo "unknown option: $arg" >&2; usage ;;
    *) refs+=("$arg") ;;
  esac
done
[ "${#refs[@]}" -ge 1 ] && [ "${#refs[@]}" -le 2 ] || usage
base="${refs[0]}"
head="${refs[1]:-HEAD}"

root="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "error: not a git repository" >&2; exit 2; }

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

merge_base="$(git merge-base "$base" "$head" 2>/dev/null)" ||
  { echo "error: no merge base between $base and $head" >&2; exit 2; }

# -z keeps paths verbatim (spaces, tabs, renames as separate old/new paths).
# Each record is "<added>\t<deleted>\t<path>\0", or "<added>\t<deleted>\t\0<old>\0<new>\0" for a rename.
rows=""
total=0
while IFS= read -r -d '' record; do
  added="${record%%$'\t'*}"
  rest="${record#*$'\t'}"
  deleted="${rest%%$'\t'*}"
  path="${rest#*$'\t'}"
  [ "$added" = "-" ] && added=0    # binary files count as 0, like CI
  [ "$deleted" = "-" ] && deleted=0
  if [ -z "$path" ]; then
    IFS= read -r -d '' old_path
    IFS= read -r -d '' path
    line="$added"$'\t'"$deleted"$'\t'"$path"$'\t'"$old_path"
  else
    line="$added"$'\t'"$deleted"$'\t'"$path"
  fi
  total=$((total + added + deleted))
  rows="$rows$((added + deleted))"$'\t'"$line"$'\n'
done < <(git diff -M --numstat -z "$merge_base".."$head" -- . ':(exclude)**/verification-metadata.xml')

echo "$total $limit"
if $show_files && [ -n "$rows" ]; then
  printf '%s' "$rows" | sort -t$'\t' -k1,1 -rn | cut -f2-
fi

[ "$total" -le "$limit" ]
