#!/usr/bin/env python3
"""Moves pull requests into the develop merge queue once they are ready.

A PR into develop is a stack of one. A stacked PR (one whose base is another open PR's
branch) belongs to the stack rooted at the PR into develop, and every stack lands on
develop as a single squash commit:

1. Once every PR of the stack has its ANDROAPP tickets in "Ready to Merge", the
   required approvals and a green CI, the stack is collapsed top-down by
   fast-forwarding each base branch to the top PR's head. No commit is created, so the
   authors' signatures are kept, and GitHub marks the upper PRs as merged.
2. The root PR, now carrying the whole stack, gets "[skip size]" in its body (each part
   was reviewed within the size limit) and auto-merge, so it joins the merge queue once
   its own checks pass.

A PR into develop that is part of a stack is never enqueued on its own. PRs without an
ANDROAPP ticket are never touched: they are merged manually.

Triggered by the merge-automation workflow: dispatched by Jira Automation with a ticket
key, or on completion of CI, Jira ready and review events for a PR branch. Every run
re-evaluates from scratch, so it is safe to run any number of times.

Environment:
  GITHUB_REPOSITORY   owner/repo.
  GH_TOKEN            GitHub App token for the gh CLI.
  JIRA_KEY            Ticket key, when dispatched from Jira.
  HEAD_BRANCH         PR head branch, when triggered by a workflow run.
  BASE_BRANCH         Defaults to develop.
  REQUIRED_APPROVALS  Defaults to 2, as in the develop ruleset.
  JIRA_URL            Defaults to https://dhis2.atlassian.net.
  DRY_RUN             "true" logs the changes instead of making them.
"""

import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.request

REPO = os.environ.get("GITHUB_REPOSITORY", "dhis2/dhis2-android-capture-app")
BASE_BRANCH = os.environ.get("BASE_BRANCH", "develop")
REQUIRED_APPROVALS = int(os.environ.get("REQUIRED_APPROVALS", "2"))
JIRA_URL = os.environ.get("JIRA_URL", "https://dhis2.atlassian.net")
DRY_RUN = os.environ.get("DRY_RUN", "").lower() == "true"

READY_STATUS = "Ready to Merge"
KEY_PATTERN = re.compile(r"\bANDROAPP-\d+\b")
JIRA_CHECK = "jira-ready"
JIRA_WORKFLOW = "jira-ready.yml"
SKIP_SIZE = "[skip size]"
COLLAPSED_MARKER = "<!-- merge-automation:collapsed -->"
DIVERGED_MARKER = "<!-- merge-automation:diverged:{sha} -->"
PR_FIELDS = "number,title,body,baseRefName,headRefName,headRefOid,isDraft,isCrossRepository,autoMergeRequest"
PASSING = {"SUCCESS", "SKIPPED", "NEUTRAL"}


def log(level, message):
    print(f"::{level}::{message}" if level else message, flush=True)


def gh(*args, parse=True):
    result = subprocess.run(["gh", *args], check=True, capture_output=True, text=True)
    if not parse:
        return result.stdout
    return json.loads(result.stdout) if result.stdout.strip() else None


def mutate(description, *args):
    if DRY_RUN:
        log("notice", f"[dry run] {description}")
        return
    log("", description)
    gh(*args, parse=False)


def jira_keys(prs):
    return sorted({key for pr in prs for key in KEY_PATTERN.findall(pr["title"])})


def jira_status(key):
    request = urllib.request.Request(
        f"{JIRA_URL}/rest/api/3/issue/{key}?fields=status",
        headers={"Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            return json.load(response)["fields"]["status"]["name"]
    except urllib.error.HTTPError as error:
        return f"HTTP {error.code}"


def tickets_ready(keys):
    ready = True
    for key in keys:
        status = jira_status(key)
        if status != READY_STATUS:
            log("", f"{key} is in \"{status}\", not \"{READY_STATUS}\".")
            ready = False
    return ready


def required_checks():
    rules = gh("api", f"repos/{REPO}/rules/branches/{BASE_BRANCH}")
    return {
        check["context"]
        for rule in rules
        if rule["type"] == "required_status_checks"
        for check in rule["parameters"]["required_status_checks"]
    }


def check_results(number):
    details = gh("pr", "view", str(number), "--repo", REPO, "--json", "latestReviews,statusCheckRollup")
    results = {}
    for check in details["statusCheckRollup"]:
        if check.get("__typename") == "StatusContext":
            results[check["context"]] = check["state"]
        else:
            done = check["status"] == "COMPLETED"
            results[check["name"]] = check["conclusion"] if done else check["status"]
    return details["latestReviews"], results


def pr_blockers(pr, required, ignored):
    """Why the PR cannot be collapsed yet, or an empty list."""
    reviews, results = check_results(pr["number"])
    blockers = []
    approvals = sum(1 for review in reviews if review["state"] == "APPROVED")
    if approvals < REQUIRED_APPROVALS:
        blockers.append(f"{approvals}/{REQUIRED_APPROVALS} approvals")
    if any(review["state"] == "CHANGES_REQUESTED" for review in reviews):
        blockers.append("changes requested")
    for name in sorted(required - ignored):
        state = results.get(name)
        if state not in PASSING:
            blockers.append(f"{name} is {state or 'missing'}")
    return blockers


def contains(ancestor_sha, descendant_sha):
    status = gh("api", f"repos/{REPO}/compare/{ancestor_sha}...{descendant_sha}", "--jq", ".status", parse=False)
    return status.strip() in ("ahead", "identical")


def find_root(seed, prs):
    """The PR into BASE_BRANCH that the seed is stacked on (the seed itself if it targets
    BASE_BRANCH), or None."""
    by_head = {pr["headRefName"]: pr for pr in prs if not pr["isCrossRepository"]}
    root = seed
    seen = {root["number"]}
    while root["baseRefName"] != BASE_BRANCH:
        root = by_head.get(root["baseRefName"])
        if root is None or root["number"] in seen:
            log("notice", f"#{seed['number']} does not lead to {BASE_BRANCH}. Left for a manual merge.")
            return None
        seen.add(root["number"])
    return root


def build_stack(root, prs):
    """The linear chain of PRs from root up to the top, or None if the stack branches."""
    stack = [root]
    while True:
        children = [pr for pr in prs if pr["baseRefName"] == stack[-1]["headRefName"]]
        if not children:
            return stack
        if len(children) > 1:
            numbers = ", ".join(f"#{pr['number']}" for pr in children)
            log("notice", f"#{stack[-1]['number']} has several stacked PRs ({numbers}). Left for a manual merge.")
            return None
        stack.append(children[0])


def rerun_failed_jira_check(pr):
    runs = gh(
        "run", "list", "--repo", REPO, "--workflow", JIRA_WORKFLOW, "--commit", pr["headRefOid"],
        "--event", "pull_request", "--limit", "1", "--json", "databaseId,status,conclusion",
    )
    if runs and runs[0]["status"] == "completed" and runs[0]["conclusion"] != "success":
        mutate(f"#{pr['number']}: re-running {JIRA_CHECK}.", "run", "rerun", str(runs[0]["databaseId"]), "--repo", REPO)


def enqueue(pr):
    if pr["autoMergeRequest"]:
        log("", f"#{pr['number']}: auto-merge already enabled.")
        return
    # On a branch with a merge queue, --auto queues the PR once its requirements are met.
    mutate(
        f"#{pr['number']}: enabling auto-merge, it joins the merge queue once approved and green.",
        "pr", "merge", str(pr["number"]), "--repo", REPO, "--auto", "--squash",
    )


def comment_once(pr, marker, text):
    comments = gh("pr", "view", str(pr["number"]), "--repo", REPO, "--json", "comments")["comments"]
    if any(marker in comment["body"] for comment in comments):
        return
    mutate(f"#{pr['number']}: commenting.", "pr", "comment", str(pr["number"]), "--repo", REPO, "--body", f"{marker}\n{text}")


def process_single(pr):
    if not jira_keys([pr]):
        log("notice", f"#{pr['number']} has no ANDROAPP ticket. Merged manually.")
        return
    if not tickets_ready(jira_keys([pr])):
        return
    if pr["isDraft"]:
        log("notice", f"#{pr['number']} is a draft.")
        return
    rerun_failed_jira_check(pr)
    enqueue(pr)


def process_stack(stack):
    root, top = stack[0], stack[-1]
    numbers = " <- ".join(f"#{pr['number']}" for pr in stack)
    log("", f"Stack {numbers}")

    # The stack must reach develop as one commit: never let its root merge alone.
    if root["autoMergeRequest"]:
        mutate(
            f"#{root['number']}: disabling auto-merge until the stack is collapsed.",
            "pr", "merge", str(root["number"]), "--repo", REPO, "--disable-auto",
        )

    keys = jira_keys(stack)
    if not keys:
        log("notice", f"Stack {numbers} has no ANDROAPP ticket. Merged manually.")
        return
    if not tickets_ready(keys):
        return
    drafts = [f"#{pr['number']}" for pr in stack if pr["isDraft"]]
    if drafts:
        log("notice", f"Stack {numbers} waits for drafts: {', '.join(drafts)}.")
        return

    # jira-ready is answered by the Jira lookup above; upper PRs never run it.
    required = required_checks()
    waiting = False
    for pr in stack:
        blockers = pr_blockers(pr, required, ignored={JIRA_CHECK})
        if blockers:
            log("", f"#{pr['number']} not ready: {'; '.join(blockers)}.")
            waiting = True
    if waiting:
        return

    for lower, upper in zip(stack, stack[1:]):
        if not contains(lower["headRefOid"], upper["headRefOid"]):
            log("warning", f"#{upper['number']} does not contain the head of {lower['headRefName']}.")
            comment_once(
                upper,
                DIVERGED_MARKER.format(sha=upper["headRefOid"]),
                f"The stack is ready to merge but cannot be collapsed: `{upper['headRefName']}` does not contain the "
                f"latest `{lower['headRefName']}`. Rebase it on `{lower['headRefName']}` and push; "
                "the stack is collapsed and queued once CI is green again.",
            )
            return

    if COLLAPSED_MARKER not in (root["body"] or ""):
        parts = ", ".join(f"#{pr['number']}" for pr in stack[1:])
        body = (
            f"{root['body'] or ''}\n\n{COLLAPSED_MARKER}\n---\n"
            f"Stack collapsed into this PR: {parts}. Each part was reviewed on its own PR. {SKIP_SIZE}"
        )
        # Before moving the branch, so the CI run it starts already sees [skip size].
        mutate(
            f"#{root['number']}: marking the PR as a collapsed stack.",
            "api", "--method", "PATCH", f"repos/{REPO}/pulls/{root['number']}", "-f", f"body={body}",
        )

    # Top-down, so each step merges exactly one PR into the branch below it.
    for pr in reversed(stack[1:]):
        mutate(
            f"Fast-forwarding {pr['baseRefName']} to {top['headRefOid'][:9]}, merging #{pr['number']}.",
            "api", "--method", "PATCH", f"repos/{REPO}/git/refs/heads/{pr['baseRefName']}",
            "-f", f"sha={top['headRefOid']}", "-F", "force=false",
        )

    enqueue(dict(root, autoMergeRequest=None))


def main():
    prs = gh("pr", "list", "--repo", REPO, "--state", "open", "--limit", "200", "--json", PR_FIELDS)
    jira_key = os.environ.get("JIRA_KEY", "")
    head_branch = os.environ.get("HEAD_BRANCH", "")

    if jira_key:
        if not re.fullmatch(r"ANDROAPP-\d+", jira_key):
            log("error", f"Invalid Jira key '{jira_key}'. Expected ANDROAPP-<number>.")
            return 1
        seeds = [pr for pr in prs if jira_key in KEY_PATTERN.findall(pr["title"])]
    elif head_branch:
        seeds = [pr for pr in prs if pr["headRefName"] == head_branch and not pr["isCrossRepository"]]
    else:
        log("error", "Set JIRA_KEY or HEAD_BRANCH.")
        return 1

    if not seeds:
        log("notice", f"No open PR for {jira_key or head_branch}.")
        return 0

    handled = set()
    for seed in seeds:
        root = find_root(seed, prs)
        if root is None or root["number"] in handled:
            continue
        handled.add(root["number"])
        stack = build_stack(root, prs)
        if stack is None:
            continue
        if len(stack) == 1:
            process_single(stack[0])
        else:
            process_stack(stack)
    return 0


if __name__ == "__main__":
    sys.exit(main())
