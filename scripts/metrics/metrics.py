#!/usr/bin/env python3
"""ANDROAPP flow metrics: fetch from Jira REST, compute current vs previous window.

Usage:  python3 metrics.py [--preflight | --census]

  --preflight  check every data source and say how to fix what is missing
  --census     list issue types and statuses actually present, before trusting a window
"""
import base64
import json
import os
import sys
import urllib.parse
import urllib.request
from collections import Counter, defaultdict
from datetime import datetime, timezone

BASE = "https://dhis2.atlassian.net"
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))  # repo root, two levels up
NOW = datetime.now(timezone.utc)
D = 86400.0

# Only the issue types the team actually works in. Story/Sub-task/Epic/Test are
# excluded: Epics are containers, Zephyr Tests sit in an AUTOMATED_TEST status
# that counts as Done with no resolution, and the rest are not part of the flow.
TYPES = ["Feature", "Task", "Bug"]

# Every status the project defines, classified. Names are case-sensitive and must
# match Jira exactly - verify with `--census`, which warns about anything observed
# in a changelog but missing here (unclassified time is silently dropped, which
# skews flow efficiency).
ACTIVE = {
    "In Analysis", "In Progress", "In Review", "Testing", "In Testing", "Retesting",
    "In Integration Testing", "In Pixel Perfect", "In Design", "In Development",
    "IN_REVIEW",
}
WAITING = {
    "To do", "To Do", "Open", "Waiting for analysis", "Prioritization", "Planned",
    "Ready to Start", "Needs Update", "NEEDS_UPDATE", "Needs info", "Needs Product",
    "Reopened", "Waiting for Testing", "Waiting for Pixel Perfect", "Ready to Merge",
    "Ready for Integration Testing", "Ready For Design", "Waiting for Design Review",
    "NEEDS DESIGN", "DRAFT", "Pending", "TO_AUTOMATE",
}
TERMINAL = {"Done", "Closed", "Resolved", "Manual", "AUTOMATED_TEST", "REDUNDANT"}

# Board order, for display.
ORDER = [
    "To do", "Waiting for analysis", "In Analysis", "Prioritization", "Ready to Start",
    "Needs Update", "In Progress", "In Review", "Waiting for Testing", "Testing",
    "Waiting for Pixel Perfect", "In Pixel Perfect", "Ready to Merge",
    "Ready for Integration Testing", "In Integration Testing",
]

# The delivery window is bounded positionally rather than by classifying statuses as
# pre/post commitment: work bounces back to Needs Update / Needs info mid-flow, so a
# static split misattributes that time.
#   commitment  = first entry to Ready to Start
#   merge       = first entry to Ready for Integration Testing. Verified as the merge
#                 marker: Automation for Jira drives 69 of 84 such transitions and 79
#                 of them come from Ready to Merge.
# Backlog = not yet committed, i.e. never reached Ready to Start.
BACKLOG = ["To do", "Waiting for analysis", "Prioritization", "Open"]

COMMIT_AT = "Ready to Start"
MERGE_AT = ["Ready for Integration Testing", "In Integration Testing"]

IN_FLIGHT = sorted(
    {"In Analysis", "In Progress", "In Review", "Testing", "In Integration Testing",
     "In Pixel Perfect", "Ready to Start", "Needs Update", "Waiting for Testing",
     "Waiting for Pixel Perfect", "Ready to Merge", "Ready for Integration Testing"})


def classify(name):
    if name in ACTIVE:
        return "active"
    if name in WAITING:
        return "wait"
    if name in TERMINAL:
        return "terminal"
    return None


MISSING_TOKEN = """
JIRA_AUTH is not set, so flow metrics cannot be computed.

Add this line to local.properties in the repo root (the file is gitignored, so the
token is never committed):

    JIRA_AUTH=your.name@dhis2.org:<api-token>

Create the token at https://id.atlassian.com/manage-profile/security/api-tokens
A read-scoped token is enough - this script only ever issues GET requests.
"""


def find_token():
    c = os.environ.get("JIRA_AUTH")
    if c:
        return c
    lp = os.path.join(REPO, "local.properties")
    if os.path.exists(lp):
        for line in open(lp):
            if line.startswith("JIRA_AUTH="):
                return line.split("=", 1)[1].strip()
    return None


def auth():
    c = find_token()
    if not c:
        sys.exit(MISSING_TOKEN)
    return "Basic " + base64.b64encode(c.encode()).decode()


def preflight():
    """Report which data sources are reachable, and how to fix the ones that aren't."""
    ok = True
    tok = find_token()
    if not tok:
        print("FAIL  Jira      JIRA_AUTH not found in env or local.properties")
        print(MISSING_TOKEN)
        ok = False
    else:
        try:
            d = get("/rest/api/3/search/jql",
                    {"jql": "project = ANDROAPP", "fields": "key", "maxResults": 1})
            print(f"OK    Jira      authenticated, search returns "
                  f"{len(d.get('issues', []))} row(s)")
        except Exception as e:
            print(f"FAIL  Jira      token present but rejected: {e}")
            print("      Regenerate the token and update local.properties.")
            ok = False

    import shutil
    import subprocess
    if not shutil.which("gh"):
        print("WARN  GitHub    gh CLI not installed - PR and CI metrics will be skipped.")
        print("      Install from https://cli.github.com then run: gh auth login")
    else:
        r = subprocess.run(["gh", "auth", "status"], capture_output=True, text=True)
        if r.returncode == 0:
            print("OK    GitHub    gh authenticated")
        else:
            print("WARN  GitHub    gh installed but not authenticated - run: gh auth login")

    try:
        u = ("https://sonarcloud.io/api/measures/component?component="
             "dhis2_dhis2-android-capture-app&branch=develop&metricKeys=coverage")
        with urllib.request.urlopen(u, timeout=30) as r:
            json.load(r)
        print("OK    SonarCloud reachable (no token needed)")
    except Exception as e:
        print(f"WARN  SonarCloud unreachable: {e}")

    print("\nNOTE  Sentry     cannot be checked from this script. In Claude Code, confirm the")
    print("      Sentry MCP tools are available; if not, authorize the server with /mcp.")
    print("      Without it the report omits production stability.")
    return ok


_HDR = None


def hdr():
    """Built lazily so --preflight can report a missing token instead of exiting."""
    global _HDR
    if _HDR is None:
        _HDR = {"Accept": "application/json", "Authorization": auth()}
    return _HDR


def get(path, params):
    url = f"{BASE}{path}?{urllib.parse.urlencode(params)}"
    with urllib.request.urlopen(urllib.request.Request(url, headers=hdr()), timeout=90) as r:
        return json.load(r)


def search(jql, fields, expand=None):
    out, tok = [], None
    while True:
        p = {"jql": jql, "fields": fields, "maxResults": 100}
        if expand:
            p["expand"] = expand
        if tok:
            p["nextPageToken"] = tok
        d = get("/rest/api/3/search/jql", p)
        out.extend(d.get("issues", []))
        tok = d.get("nextPageToken")
        if not tok or d.get("isLast"):
            break
    return out


def q(xs):
    return ",".join('"%s"' % x for x in xs)


def ts(s):
    if not s:
        return None
    s = s.replace("Z", "+00:00")
    if len(s) > 5 and s[-5] in "+-" and s[-3] != ":":
        s = s[:-2] + ":" + s[-2:]
    return datetime.fromisoformat(s)


def pct(v, p):
    if not v:
        return None
    v = sorted(v)
    k = max(0, min(len(v) - 1, int(round((p / 100.0) * len(v) + 0.5)) - 1))
    return v[k]


def transitions(i):
    out = []
    for h in i.get("changelog", {}).get("histories", []):
        w = ts(h["created"])
        for it in h["items"]:
            if it.get("field") == "status":
                out.append((w, it.get("fromString"), it.get("toString")))
    out.sort(key=lambda x: x[0])
    return out


UNKNOWN = Counter()


def timeline(issue):
    """Per-status seconds (all visits summed) plus the milestones we measure between.

    Returns dict(status -> seconds), and a milestones dict with:
      committed  first entry to Ready to Start
      merged     first entry to a merge marker
      terminal   first entry to a terminal status
    Segments are also tagged so active/wait time inside the delivery window can be
    summed without re-walking the changelog.
    """
    created = ts(issue["fields"]["created"])
    trs = transitions(issue)
    dur = defaultdict(float)
    segs = []  # (status, start, end)
    ms = {"committed": None, "merged": None, "terminal": None}

    if not trs:
        cur = issue["fields"]["status"]["name"]
        if cur not in TERMINAL:
            dur[cur] += (NOW - created).total_seconds()
            segs.append((cur, created, NOW))
        return dur, ms, segs

    cursor, status = created, trs[0][1] or "To do"
    for when, _f, to in trs:
        if status not in TERMINAL:
            dur[status] += max(0.0, (when - cursor).total_seconds())
            segs.append((status, cursor, when))
            if classify(status) is None:
                UNKNOWN[status] += 1
        cursor, status = when, to
        if to == COMMIT_AT and ms["committed"] is None:
            ms["committed"] = when
        if to in MERGE_AT and ms["merged"] is None:
            ms["merged"] = when
        if to in TERMINAL and ms["terminal"] is None:
            ms["terminal"] = when
    if ms["terminal"] is None and status not in TERMINAL:
        dur[status] += max(0.0, (NOW - cursor).total_seconds())
        segs.append((status, cursor, NOW))
    return dur, ms, segs


def window_split(segs, start, end):
    """Active and wait seconds falling inside [start, end]."""
    a = w = 0.0
    for status, s, e in segs:
        lo, hi = max(s, start), min(e, end)
        if hi <= lo:
            continue
        d = (hi - lo).total_seconds()
        k = classify(status)
        if k == "active":
            a += d
        elif k == "wait":
            w += d
    return a, w


def window(issues):
    done = [i for i in issues
            if (i["fields"].get("resolution") or {}).get("name") == "Done"]
    m = {"resolved": len(issues), "throughput": len(done)}
    leads, deliv, eff_d, eff_full, intake, post = [], [], [], [], [], []
    stage = defaultdict(list)
    for i in done:
        f = i["fields"]
        c, r = ts(f["created"]), ts(f["resolutiondate"])
        if c and r:
            leads.append((r - c).total_seconds() / D)
        dur, ms, segs = timeline(i)

        # delivery window: committed -> merged (falls back to terminal if never merged)
        start = ms["committed"]
        end = ms["merged"] or ms["terminal"]
        if start and end and end > start:
            deliv.append((end - start).total_seconds() / D)
            a, w = window_split(segs, start, end)
            if a + w:
                eff_d.append(a / (a + w) * 100)
        if start and c:
            intake.append((start - c).total_seconds() / D)
        # time after merge until resolution (integration testing tail)
        if ms["merged"] and r and r > ms["merged"]:
            post.append((r - ms["merged"]).total_seconds() / D)

        a = sum(v for k, v in dur.items() if classify(k) == "active")
        w = sum(v for k, v in dur.items() if classify(k) == "wait")
        if a + w:
            eff_full.append(a / (a + w) * 100)
        for k, v in dur.items():
            if classify(k) in ("active", "wait"):
                stage[k].append(v / D)

    m.update(lead_p50=pct(leads, 50), lead_p85=pct(leads, 85),
             deliv_p50=pct(deliv, 50), deliv_p85=pct(deliv, 85), deliv_n=len(deliv),
             eff_deliv=pct(eff_d, 50), eff_full=pct(eff_full, 50),
             intake_p50=pct(intake, 50), intake_p85=pct(intake, 85),
             post_merge_p50=pct(post, 50), post_merge_p85=pct(post, 85),
             types=Counter(i["fields"]["issuetype"]["name"] for i in done),
             resolutions=Counter((i["fields"].get("resolution") or {}).get("name", "None")
                                 for i in issues),
             no_fix=len(issues) - len(done))
    tot = sum(sum(v) for v in stage.values()) or 1
    m["stages"] = {k: {"p50": pct(v, 50), "p85": pct(v, 85), "share": sum(v) / tot * 100,
                       "kind": classify(k)}
                   for k, v in stage.items()}
    return m


def main():
    base = f'project = ANDROAPP AND issuetype in ({q(TYPES)})'
    fields = ("summary,status,created,resolutiondate,resolution,issuetype,priority,"
              "components,fixVersions,updated")

    if "--preflight" in sys.argv:
        sys.exit(0 if preflight() else 1)

    if "--census" in sys.argv:
        # what types exist at all, so exclusions are deliberate not accidental
        allt = search('project = ANDROAPP AND resolutiondate >= -90d',
                      "issuetype,status")
        print("ALL types resolved in 90d:",
              dict(Counter(i["fields"]["issuetype"]["name"] for i in allt).most_common()))
        flight = search(f'project = ANDROAPP AND status in ({q(IN_FLIGHT)})',
                        "issuetype,status")
        print("ALL types in flight     :",
              dict(Counter(i["fields"]["issuetype"]["name"] for i in flight).most_common()))
        for i in flight:
            print(f"   {i['fields']['issuetype']['name']:<12}"
                  f"{i['fields']['status']['name']:<32}{i['key']}")
        return

    data = {}
    for name, jql, exp in [
        ("cur", f'{base} AND resolutiondate >= -90d', "changelog"),
        ("prev", f'{base} AND resolutiondate >= -180d AND resolutiondate < -90d', "changelog"),
        ("flight", f'{base} AND status in ({q(IN_FLIGHT)})', "changelog"),
        ("backlog", f'{base} AND status in ({q(BACKLOG)})', None),
        ("bugs", 'project = ANDROAPP AND issuetype = Bug AND statusCategory != Done', None),
        ("epics", 'project = ANDROAPP AND issuetype = Epic AND statusCategory != Done', None),
    ]:
        data[name] = search(jql, fields, exp)
        print(f"{name}: {len(data[name])}", file=sys.stderr)

    cur, prev = window(data["cur"]), window(data["prev"])

    # gate
    g = next((i for i in data["cur"] if i["key"] == "ANDROAPP-7679"), None)
    if g:
        dur, ms, _ = timeline(g)
        lead = (ts(g["fields"]["resolutiondate"]) - ts(g["fields"]["created"])).total_seconds() / D
        dl = ((ms["merged"] - ms["committed"]).total_seconds() / D
              if ms["merged"] and ms["committed"] else None)
        print(f"\nGATE ANDROAPP-7679: lead {lead:.1f} (expect 42.8)  "
              f"InReview {dur.get('In Review',0)/D:.1f} (expect 8.2)  "
              f"delivery {dl:.1f} (Ready to Start -> merged)")

    if UNKNOWN:
        print("\n!! UNCLASSIFIED STATUSES - their time is being dropped:")
        for n, c in UNKNOWN.most_common():
            print(f"   {n!r}: {c} segments")
    else:
        print("\nall observed statuses classified.")

    print("\n=== TREND (90d vs preceding 90d) ===")
    for label, key, lower in [
            ("Throughput", "throughput", False), ("Lead p50", "lead_p50", True),
            ("Lead p85", "lead_p85", True),
            ("Delivery p50", "deliv_p50", True), ("Delivery p85", "deliv_p85", True),
            ("Flow eff delivery", "eff_deliv", False),
            ("Flow eff full", "eff_full", False),
            ("Intake p50", "intake_p50", True), ("Intake p85", "intake_p85", True),
            ("Post-merge p50", "post_merge_p50", True)]:
        c, p = cur[key], prev[key]
        d = (c - p) / p * 100 if p else 0
        print(f"  {label:<16}{p:>8.1f}{c:>8.1f}   {d:+.0f}%  "
              f"{'GOOD' if ((d<0)==lower and abs(d)>=5) else ('BAD' if abs(d)>=5 else '~')}")
    print(f"  no-fix          {prev['no_fix']}/{prev['resolved']}   "
          f"{cur['no_fix']}/{cur['resolved']}")
    print("  types  prev:", dict(prev["types"]), " cur:", dict(cur["types"]))
    print("  resolutions cur:", dict(cur["resolutions"]))

    print("\n=== STAGES (share of total; prev -> cur) ===")
    for s in ORDER:
        c = cur["stages"].get(s)
        if not c:
            continue
        p = prev["stages"].get(s, {}).get("share", 0)
        print(f"  {s:<32}{c['kind']:<7}{p:>6.1f}{c['share']:>7.1f}"
              f"{c['p50']:>8.1f}{c['p85']:>8.1f}")
    other = {k: v for k, v in cur["stages"].items() if k not in ORDER}
    if other:
        print("  -- not on the board order --")
        for k, v in sorted(other.items(), key=lambda x: -x[1]["share"]):
            p = prev["stages"].get(k, {}).get("share", 0)
            print(f"  {k:<32}{v['kind']:<7}{p:>6.1f}{v['share']:>7.1f}"
                  f"{v['p50']:>8.1f}{v['p85']:>8.1f}")

    print("\n=== WIP ===")
    live, orph = [], []
    for i in data["flight"]:
        f = i["fields"]
        trs = transitions(i)
        entered = trs[-1][0] if trs else ts(f["created"])
        days = (NOW - entered).total_seconds() / D
        rec = {"key": i["key"], "type": f["issuetype"]["name"],
               "status": f["status"]["name"], "days": days, "n": len(trs),
               "summary": f["summary"].strip()[:60],
               "moved": entered.date().isoformat()}
        (orph if (len(trs) <= 1 and days > 365) else live).append(rec)
    print("  live by status:", dict(Counter(x["status"] for x in live)))
    print("  live by type  :", dict(Counter(x["type"] for x in live)))
    print(f"  active {sum(1 for x in live if classify(x['status'])=='active')}  "
          f"queued {sum(1 for x in live if classify(x['status'])=='wait')}  "
          f"backlog {len(data['backlog'])}  bugs {len(data['bugs'])}")
    print(f"  aging over cycle p85 ({cur['deliv_p85']:.0f}d):",
          [f"{x['key']}({x['days']:.0f}d)" for x in live if x["days"] > cur["deliv_p85"]] or "none")
    print(f"  orphans ({len(orph)}):")
    for x in sorted(orph, key=lambda y: -y["days"]):
        print(f"    {x['key']:<16}{x['type']:<10}{x['days']:>5.0f}d  {x['moved']}  {x['summary']}")

    ep = data["epics"]
    ages = [(NOW - ts(e["fields"]["created"])).total_seconds() / D for e in ep]
    print(f"\n=== EPICS: {len(ep)} open, age p50 {pct(ages,50):.0f}d "
          f"p85 {pct(ages,85):.0f}d oldest {max(ages):.0f}d")
    print("  by status:", dict(Counter(e["fields"]["status"]["name"] for e in ep).most_common()))

    json.dump({"cur": cur, "prev": prev, "live": live, "orphans": orph,
               "backlog": len(data["backlog"]), "bugs": len(data["bugs"]),
               "epics": {"open": len(ep), "age_p50": pct(ages, 50),
                         "age_p85": pct(ages, 85), "oldest": max(ages),
                         "by_status": dict(Counter(e["fields"]["status"]["name"] for e in ep))}},
              open(os.path.join(HERE, "metrics.json"), "w"), indent=1, default=str)
    print("\nwrote metrics.json")


if __name__ == "__main__":
    main()
