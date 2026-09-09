#!/usr/bin/env python3
"""ANDROAPP flow metrics: fetch from Jira REST, compute current vs previous window.

Usage:  python3 metrics.py [--preflight | --census | --token-help]

  --preflight   check every data source and say how to fix what is missing
  --census      list issue types and statuses actually present, before trusting a window
  --token-help  what a token is and is not needed for, and which kind to create

No credential is needed to compute the report: Jira is world-readable and
Confluence is reached through the Atlassian connector. A token is only for
attaching the chart PNGs. See --token-help.
"""
import base64
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter, defaultdict
from datetime import datetime, timedelta, timezone

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

# `Needs info` parks an item that cannot proceed until someone answers a question.
# It counts as WAITING in the flow numbers like any other queue, but it also gets its
# own review, because the question it raises is different: not how long items sit
# there, but whether the doubt ever gets resolved and what happens to the ones where
# it does not. Exact Jira spelling - a rename would silently empty the section, so
# the run warns about any case variant it sees in a changelog.
NEEDS_INFO = "Needs info"

# Reported in this order: resolved, abandoned, then the two kinds of still-open.
ORDER_OUTCOME = ["moved forward and done", "moved forward, still open",
                 "closed without a fix", "still in Needs info", "never moved on"]

# A patch release is a bug-fix release: the third number moves (3.4.1) or a fourth
# one is appended as a hotfix (3.4.0.1). `X.Y.0` is a feature release and is excluded -
# its bug count measures what the team happened to be doing, not patch quality.
PATCH_RE = re.compile(r"^(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?$")
PATCHES_SHOWN = 3


def classify(name):
    if name in ACTIVE:
        return "active"
    if name in WAITING:
        return "wait"
    if name in TERMINAL:
        return "terminal"
    return None


MISSING_TOKEN = """
Jira could not be read even anonymously, so flow metrics cannot be computed.

ANDROAPP is normally world-readable over the REST API, so this usually means the
project's permissions changed or the network is blocking the request. A token
restores access in the first case — see TOKEN_HELP below.
"""

TOKEN_HELP = """
No token is required to produce the report.

  Jira        world-readable, including expand=changelog. Every flow metric
              computes anonymously.
  Confluence  read the previous edition and create/update the page through the
              Atlassian connector in Claude Code (per-user OAuth, no token).
  Charts      the ONLY step that needs a credential: attaching the four PNGs,
              because the connector has no attachment-upload tool.

So set a token only to attach charts, or to stop anonymous Jira reads from
silently missing a permission-restricted issue. Add it to local.properties in
the repo root — gitignored, so it is never committed. A worktree inherits the
main checkout's file automatically:

    JIRA_AUTH=your.name@dhis2.org:<api-token>

Two token types exist and they behave differently:

  Scoped   "Create API token with scopes" — least privilege, preferred.
           Works ONLY as Bearer against api.atlassian.com/ex/confluence/...
           Grant exactly these two, and no Jira scopes:

               read:content-details:confluence
               write:attachment:confluence

           (the single classic equivalent is write:confluence-file)

           Upload is v1-only: the v2 attachment API has GET and DELETE but no
           create operation, so read:attachment:confluence — the obvious guess —
           cannot upload anything. It is not needed at all.
           A scoped token CANNOT read Jira over Basic auth. That is expected.

  Classic  "Create API token" — full impersonation of your account, read and
           write, every project and space, no scope list. Works with Basic auth
           against dhis2.atlassian.net. Simpler, and much broader than needed.

https://id.atlassian.com/manage-profile/security/api-tokens
"""

SITE = BASE                        # same host, named for the auth discussion above
API = "https://api.atlassian.com"  # scoped tokens are only accepted here
METRICS_PARENT = "1948057602"      # published "Android Metrics" page, used to probe access


def main_checkout():
    """The primary worktree's root, or None.

    local.properties is gitignored, so a git worktree does not inherit it from
    the clone it was made from — a token configured once in the main checkout is
    invisible to every worktree. `git rev-parse --git-common-dir` points at the
    shared .git directory, whose parent is that main checkout.
    """
    try:
        out = subprocess.run(["git", "-C", REPO, "rev-parse", "--git-common-dir"],
                             capture_output=True, text=True, timeout=10, check=True)
    except (OSError, subprocess.SubprocessError):
        return None
    common = os.path.abspath(os.path.join(REPO, out.stdout.strip()))
    root = os.path.dirname(common)
    return root if root != REPO else None


def find_token():
    c = os.environ.get("JIRA_AUTH")
    if c:
        return c
    roots = [REPO]
    main = main_checkout()
    if main:
        roots.append(main)
    for root in roots:
        lp = os.path.join(root, "local.properties")
        if not os.path.exists(lp):
            continue
        for line in open(lp):
            if line.startswith("JIRA_AUTH="):
                return line.split("=", 1)[1].strip()
    return None


def auth():
    """Basic header for Jira, or None.

    ANDROAPP is world-readable over the REST API — including expand=changelog — so
    flow metrics work without a token. A classic token is still slightly preferred:
    it also sees any permission-restricted issue, which anonymous access would drop
    from every count silently.

    A scoped token returns None here on purpose. Scoped tokens are not accepted by
    Basic auth at all, so sending one produces a 401 on endpoints that would have
    answered anonymously — worse than not sending it.
    """
    c = find_token()
    if not c or classify_token(c)[0] != "classic":
        return None
    return "Basic " + base64.b64encode(c.encode()).decode()


_KIND = {}


def classify_token(cred):
    """('classic', display name) | ('scoped', None) | ('rejected', reason) | (None, None).

    Both types are ~192 chars and start ATATT, so the string cannot be told apart by
    inspection — only by what the two auth schemes say about it. Cached, because
    preflight and the attachment upload both ask.
    """
    if not cred:
        return (None, None)
    if cred in _KIND:
        return _KIND[cred]

    secret = cred.partition(":")[2]

    def probe(url, header):
        req = urllib.request.Request(url, headers={"Accept": "application/json",
                                                   "Authorization": header})
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                return r.status, json.load(r)
        except urllib.error.HTTPError as e:
            return e.code, e.read(300).decode(errors="replace")
        except Exception as e:                                        # noqa: BLE001
            return None, str(e)

    basic = "Basic " + base64.b64encode(cred.encode()).decode()
    bearer = "Bearer " + secret
    code, body = probe(f"{SITE}/rest/api/3/myself", basic)
    if code == 200 and isinstance(body, dict):
        out = ("classic", body.get("displayName"))
    else:
        # Bearer against api.atlassian.com is the scoped-token path. A 200 means the
        # scopes cover the call. "scope does not match" is the interesting case: it
        # proves the credential IS a real scoped token that the grant simply does not
        # cover — which must not be reported as a broken token, because the fix is to
        # add a scope, not to reissue.
        # "scope does not match" from ANY endpoint proves the credential is a real
        # scoped token — the grant simply does not cover that call. Do not try to
        # infer which scopes it has from an unrelated endpoint: the granular scopes
        # are per-resource, so probing e.g. /space reports "insufficient" for a token
        # that attaches files perfectly well. Whether the grant covers attachments is
        # answered by charts_access(), against the attachment API itself.
        code2, body2 = probe(f"{API}/oauth/token/accessible-resources", bearer)
        if code2 == 200 or "scope does not match" in str(body2):
            out = ("scoped", None)
        else:
            try:
                code3, body3 = probe(
                    f"{API}/ex/confluence/{cloud_id()}/wiki"
                    f"/api/v2/pages/{METRICS_PARENT}/attachments?limit=1", bearer)
            except Exception as e:                                   # noqa: BLE001
                code3, body3 = None, str(e)
            if code3 == 200 or "scope does not match" in str(body3):
                out = ("scoped", None)
            else:
                out = ("rejected", f"Basic {code}, Bearer {code2}/{code3}")

    _KIND[cred] = out
    return out


def cloud_id():
    """The site's cloudId, needed to address it through api.atlassian.com. Public."""
    with urllib.request.urlopen(f"{SITE}/_edge/tenant_info", timeout=30) as r:
        return json.load(r)["cloudId"]


def confluence_base(kind, cred):
    """(base_url, auth_header) for the Confluence API the given token type can use.

    The two token types do not merely differ in scheme — they land on different API
    versions. The granular scopes (read:/write:attachment:confluence) are only
    honoured on **v2**; v1 `/rest/api/content/...` answers 401 for them and wants the
    legacy read:confluence-content.summary / write:confluence-file instead. A classic
    token works on either, so it stays on v1 where upload is documented.
    """
    if kind == "scoped":
        return (f"{API}/ex/confluence/{cloud_id()}/wiki",
                "Bearer " + cred.partition(":")[2])
    return (f"{SITE}/wiki", "Basic " + base64.b64encode(cred.encode()).decode())


def charts_access(page_id=None):
    """Can this token upload an attachment? ('ok'|'no'|'none', detail).

    Probed against the route the uploader uses: v1 PUT /content/{id}/child/attachment
    ("create or update attachment").
    The v2 attachment API has no create operation, so there is nothing else to try —
    and read:attachment:confluence, which sounds right, grants v2 reads that are
    useless here.

    An empty body cannot succeed, so a 4xx that is *not* 401/403 means auth passed
    and only the payload was wrong. That is the signal we want; a real upload would
    otherwise be needed just to test the credential.
    """
    cred = find_token()
    kind = classify_token(cred)[0]
    if kind in (None, "rejected"):
        return ("none", "no usable token")

    base, header = confluence_base(kind, cred)
    pid = page_id or METRICS_PARENT
    url = f"{base}/rest/api/content/{pid}/child/attachment"
    req = urllib.request.Request(
        # PUT, matching what attach_charts.py actually uses: POST on this path needs
        # the same scopes but rejects existing filenames, so probing POST would pass
        # while the real upload failed.
        url, method="PUT", data=b"",
        headers={"Accept": "application/json", "Authorization": header,
                 "X-Atlassian-Token": "nocheck"})
    try:
        with urllib.request.urlopen(req, timeout=40) as r:
            return ("ok", f"upload route returned {r.status}")
    except urllib.error.HTTPError as e:
        body = e.read(200).decode(errors="replace")
        if e.code in (401, 403):
            return ("no", f"{e.code} {body[:90]}")
        if e.code == 404:
            return ("no", "404 — page is probably still a draft; publish it first")
        return ("ok", f"auth accepted ({e.code} on an empty body, as expected)")
    except Exception as e:                                           # noqa: BLE001
        return ("none", str(e))


def preflight():
    """Report which data sources are reachable, and how to fix the ones that aren't."""
    ok = True
    tok = find_token()
    kind, identity = classify_token(tok)

    # A token that is present is not a token that works, and a token that fails
    # Basic auth is not necessarily broken — a scoped token is *expected* to. Report
    # the three cases apart, or a correct least-privilege setup reads as an error.
    if kind == "rejected":
        print(f"FAIL  Token     JIRA_AUTH is set but neither auth scheme accepts it "
              f"({identity}).")
        print("      Likely revoked, mistyped, or from a different Atlassian account.")
        print("      Metrics still compute anonymously; charts cannot be attached.")
        ok = False
        tok = None

    mode = f"authenticated as {identity}" if kind == "classic" else "anonymous"
    try:
        d = get("/rest/api/3/search/jql",
                {"jql": "project = ANDROAPP", "fields": "key", "maxResults": 1})
        print(f"OK    Jira      {mode}, search returns {len(d.get('issues', []))} row(s)")
        if kind != "classic":
            print("      Reading anonymously — correct and expected. ANDROAPP is")
            print("      world-readable, so every flow metric computes; but a")
            print("      permission-restricted issue would be invisible and missing from")
            print("      the counts without warning. Say 'anonymous' in the Method section.")
    except Exception as e:
        print(f"FAIL  Jira      {mode} request rejected: {e}")
        print(MISSING_TOKEN)
        ok = False

    print("OK    Confluence via the Atlassian connector in Claude Code (no token).")
    print("      Reading the previous edition and creating/updating the page both go")
    print("      through the connector's per-user OAuth. Confirm the Atlassian tools are")
    print("      available in-session; if not, authorize with /mcp.")

    if kind in ("scoped", "classic"):
        state, detail = charts_access()
        label = "scoped" if kind == "scoped" else "classic"
        if state == "ok":
            print(f"OK    Charts    {label} token can upload attachments ({detail}).")
            if kind == "classic":
                print("      Note it is a full impersonation credential — every project and")
                print("      space, read and write. read:content-details:confluence +")
                print("      write:attachment:confluence would be enough.")
        else:
            print(f"WARN  Charts    token cannot upload attachments ({detail}).")
            print("      Upload is v1-only — the v2 attachment API has no create")
            print("      operation — so grant exactly these two, no Jira scopes:")
            print("        read:content-details:confluence   write:attachment:confluence")
            print("      read:attachment:confluence does NOT work: it grants v2 reads only.")
    elif kind == "rejected":
        print("NOTE  Charts    token unusable, so the PNGs cannot be attached. Publish the")
        print("      collapsed tables instead and say so in the report.")
    else:
        print("NOTE  Charts    no token, so the four PNGs cannot be attached — the")
        print("      connector has no attachment-upload tool. Publish the collapsed")
        print("      tables instead and say so in the report. Everything else is")
        print("      unaffected. See --token-help.")

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
    """Built lazily so --preflight can report the auth mode before any request."""
    global _HDR
    if _HDR is None:
        _HDR = {"Accept": "application/json"}
        a = auth()
        if a:
            _HDR["Authorization"] = a
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


def patch_releases(versions, n=PATCHES_SHOWN):
    """The n most recent shipped patch releases, newest first, plus any unshipped one.

    Ordered by release date, not by name: version names do not sort numerically and
    the project ships out of order (3.4.0.1 came after 3.4.1 was opened).
    """
    pat = []
    for v in versions:
        m = PATCH_RE.match(v["name"].strip())
        if not m or (m.group(3) == "0" and not m.group(4)):
            continue
        pat.append({"name": v["name"].strip(), "released": bool(v.get("released")),
                    "date": v.get("releaseDate") or ""})
    pat.sort(key=lambda v: (v["date"], v["name"]), reverse=True)
    shipped = [v for v in pat if v["released"]][:n]
    # The patch currently in flight is worth a flagged row: it is the one the team can
    # still act on, and the Releases section already tracks whether it is overdue. That
    # is the EARLIEST unshipped patch due after the last release - the next one out, not
    # the furthest-out one on the roadmap.
    ahead = sorted((v for v in pat if not v["released"] and v["date"]
                    and (not shipped or v["date"] > shipped[0]["date"])),
                   key=lambda v: v["date"])
    return ahead[:1] + shipped


def bugs_per_patch(vers, issues):
    """Bug counts per patch release: fixed, closed without a fix, still open.

    `fixVersion` is a TARGET on this project, not a shipped-in stamp - items are
    tagged when they are planned for a release and are not retagged when they slip.
    So `still open` on an already-released version is not work in that release; it is
    a tag nobody cleaned up, and it is reported precisely because it says how far the
    field can be trusted.
    """
    rows = []
    for v in vers:
        mine = [i for i in issues
                if any(fv["name"].strip() == v["name"]
                       for fv in (i["fields"].get("fixVersions") or []))]
        res = Counter((i["fields"].get("resolution") or {}).get("name", "open")
                      for i in mine)
        rows.append(dict(v, fixed=res.get("Done", 0),
                         not_fixed=sum(n for k, n in res.items()
                                       if k not in ("Done", "open")),
                         still_open=res.get("open", 0), total=len(mine),
                         reasons=dict(Counter({k: n for k, n in res.items()
                                               if k not in ("Done", "open")}).most_common())))
    return rows


def dt(d):
    """Date -> UTC datetime, for comparing window bounds against changelog stamps."""
    return datetime(d.year, d.month, d.day, tzinfo=timezone.utc)


def visits(issue, status_name):
    """Every stay in `status_name`: (entered, left or None, status it left for)."""
    trs = transitions(issue)
    if not trs:
        return ([(ts(issue["fields"]["created"]), None, None)]
                if issue["fields"]["status"]["name"] == status_name else [])
    out, entered = [], None
    for when, frm, to in trs:
        if to == status_name and entered is None:
            entered = when
        elif frm == status_name and entered is not None:
            out.append((entered, when, to))
            entered = None
    if entered is not None:
        out.append((entered, None, None))
    return out


def progressed_after(issue, since):
    """Did the issue move DOWNSTREAM after `since` - i.e. was the doubt resolved?

    Downstream means commitment or beyond: first entry to `Ready to Start`, to a
    merge marker, or to any active status. Leaving Needs info is not itself
    progress - in practice every exit lands back in `To do`, so an exit alone says
    only that someone cleared the flag, not that the question got answered.
    """
    for when, _f, to in transitions(issue):
        if when <= since:
            continue
        if to == COMMIT_AT or to in MERGE_AT or classify(to) == "active":
            return True
    return False


def needs_info(issues, lo, hi):
    """What became of the issues that entered Needs info in [lo, hi).

    The question the section exists to answer is narrow: of the items parked for
    missing information, how many got the information and moved on, and how many
    were quietly closed instead. Everything else about the status is texture.

    Bucketing by entry date, not by exit, is what makes the two windows comparable:
    an item still waiting has no exit date to bucket on, and dropping it would
    flatter the resolution rate by counting only the ones that got out.
    """
    per_visit, per_issue = [], {}
    for i in issues:
        for entered, left, to in visits(i, NEEDS_INFO):
            if not (lo <= entered < hi):
                continue
            per_visit.append({"key": i["key"],
                              "days": ((left or NOW) - entered).total_seconds() / D})
            r = per_issue.setdefault(i["key"], {"n": 0, "last": entered})
            r["n"] += 1
            r["last"] = max(r["last"], entered)

    outcome, reasons = Counter(), Counter()
    for i in issues:
        r = per_issue.get(i["key"])
        if not r:
            continue
        st = i["fields"]["status"]["name"]
        rn = (i["fields"].get("resolution") or {}).get("name")
        if rn == "Done":
            k = "moved forward and done"
        elif rn or st in TERMINAL:
            k = "closed without a fix"
            reasons[rn or "closed with no resolution"] += 1
        elif st == NEEDS_INFO:
            k = "still in Needs info"
        elif progressed_after(i, r["last"]):
            k = "moved forward, still open"
        else:
            k = "never moved on"
        outcome[k] += 1
        r["outcome"] = k

    # A large share of stays last minutes: the status is flipped and flipped straight
    # back, so the raw median is 0 and says nothing about how long a real question
    # takes to answer. Percentiles are therefore reported twice - over every stay,
    # and over stays that actually lasted a day. Quote the second one.
    d = [v["days"] for v in per_visit]
    real = [x for x in d if x >= 1]
    fwd = outcome["moved forward and done"] + outcome["moved forward, still open"]
    return {"visits": len(per_visit), "issues": len(per_issue),
            "instant": len(d) - len(real),
            "repeat": sum(1 for r in per_issue.values() if r["n"] > 1),
            "dwell_real_p50": pct(real, 50), "dwell_real_p85": pct(real, 85),
            "dwell_max": max(d) if d else None,
            "outcome": {k: outcome[k] for k in ORDER_OUTCOME if outcome[k]},
            "moved_forward": fwd,
            "moved_forward_pct": fwd / len(per_issue) * 100 if per_issue else None,
            "closed_without_fix": outcome["closed without a fix"],
            "dropped_reasons": dict(reasons.most_common())}


def needs_info_stock(issues):
    """Everything sitting in Needs info right now, oldest first."""
    out = []
    for i in issues:
        if i["fields"]["status"]["name"] != NEEDS_INFO:
            continue
        vs = visits(i, NEEDS_INFO)
        entered = vs[-1][0] if vs else ts(i["fields"]["created"])
        out.append({"key": i["key"], "type": i["fields"]["issuetype"]["name"],
                    "days": (NOW - entered).total_seconds() / D, "n": len(vs),
                    "since": entered.date().isoformat(),
                    "summary": i["fields"]["summary"].strip()[:60]})
    return sorted(out, key=lambda x: -x["days"])


def main():
    base = f'project = ANDROAPP AND issuetype in ({q(TYPES)})'
    fields = ("summary,status,created,resolutiondate,resolution,issuetype,priority,"
              "components,fixVersions,updated")

    if "--token-help" in sys.argv:
        print(TOKEN_HELP)
        sys.exit(0)

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

    # Window end: today unless --as-of pins it to a past date, which is what lets an
    # edition be regenerated later and still match the page it was published on.
    as_of = NOW.date()
    for i, a in enumerate(sys.argv):
        if a == "--as-of" and i + 1 < len(sys.argv):
            as_of = datetime.strptime(sys.argv[i + 1], "%Y-%m-%d").date()
    if as_of > NOW.date():
        sys.exit("--as-of is in the future")
    w0, w1, w2 = (as_of - timedelta(days=180), as_of - timedelta(days=90), as_of)
    if as_of != NOW.date():
        print(f"window pinned to {w1} .. {w2} (--as-of). Work-in-progress, backlog and epic\n"
              f"counts are still current-state, so they describe today, not {w2}.",
              file=sys.stderr)

    data = {}
    for name, jql, exp in [
        ("cur", f'{base} AND resolutiondate >= "{w1}" AND resolutiondate <= "{w2}"', "changelog"),
        ("prev", f'{base} AND resolutiondate >= "{w0}" AND resolutiondate < "{w1}"', "changelog"),
        ("flight", f'{base} AND status in ({q(IN_FLIGHT)})', "changelog"),
        ("backlog", f'{base} AND status in ({q(BACKLOG)})', None),
        ("bugs", 'project = ANDROAPP AND issuetype = Bug AND statusCategory != Done', None),
        ("epics", 'project = ANDROAPP AND issuetype = Epic AND statusCategory != Done', None),
        ("epics_closed",
         f'project = ANDROAPP AND issuetype = Epic AND resolutiondate >= "{w1}" '
         f'AND resolutiondate <= "{w2}"', None),
        # Everything that touched Needs info since the start of the PREVIOUS window,
        # plus whatever sits there now however long it has been there. `WAS ... AFTER`
        # reads the changelog server-side and works anonymously.
        ("needsinfo",
         f'{base} AND (status WAS "{NEEDS_INFO}" AFTER "{w0}" '
         f'OR status = "{NEEDS_INFO}")', "changelog"),
    ]:
        data[name] = search(jql, fields, exp)
        print(f"{name}: {len(data[name])}", file=sys.stderr)

    # Bugs fixed per patch release. Release-scoped, not window-scoped: the question is
    # what each shipped patch actually carried, which no 90-day window answers.
    patches = patch_releases(get("/rest/api/3/project/ANDROAPP/versions", {}))
    patch_bugs = (search('project = ANDROAPP AND issuetype = Bug AND fixVersion in (%s)'
                         % q([v["name"] for v in patches]),
                         "issuetype,resolution,status,fixVersions,resolutiondate")
                  if patches else [])
    prows = bugs_per_patch(patches, patch_bugs)
    print(f"patch_bugs: {len(patch_bugs)}", file=sys.stderr)

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
    # Diagnostic only - the completed-work type mix is no longer a report section, since
    # it moves with what the release was for rather than with quality. Bugs fixed per
    # patch release, below, is what Quality opens with instead.
    print("  types (diagnostic)  prev:", dict(prev["types"]), " cur:", dict(cur["types"]))
    print("  resolutions cur:", dict(cur["resolutions"]))

    # Coverage: what share of bugs fixed in the window carry any fixVersion at all.
    # Whatever is missing is missing from every row of the patch table below.
    fixed_cur = [i for i in data["cur"] if i["fields"]["issuetype"]["name"] == "Bug"
                 and (i["fields"].get("resolution") or {}).get("name") == "Done"]
    tagged = sum(1 for i in fixed_cur if i["fields"].get("fixVersions"))
    print("\n=== BUGS FIXED PER PATCH RELEASE ===")
    print(f"  {'version':<12}{'released':<13}{'fixed':>6}{'not fixed':>11}"
          f"{'still open':>12}   closed-without-a-fix reasons")
    for r in prows:
        when = r["date"] if r["released"] else f"due {r['date']} (unreleased)"
        print(f"  {r['name']:<12}{when:<13}{r['fixed']:>6}{r['not_fixed']:>11}"
              f"{r['still_open']:>12}   {r['reasons'] or ''}")
    print(f"  fixVersion coverage: {tagged}/{len(fixed_cur)} bugs fixed in the window "
          f"carry one - the table is a floor")

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

    # Diagnostics, not a report section: work in progress is a "now" number that the
    # dailies already own, so it is printed for sanity-checking and kept in
    # metrics.json, but the published report does not carry a WIP section. The two
    # counts that are monthly rather than momentary - open bugs, backlog depth - go
    # into Quality instead.
    print("\n=== WIP (diagnostic - not a report section) ===")
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
    print(f"\n=== EPICS: {len(ep)} open, {len(data['epics_closed'])} closed in window, "
          f"age p50 {pct(ages,50):.0f}d p85 {pct(ages,85):.0f}d oldest {max(ages):.0f}d")
    print("  by status:", dict(Counter(e["fields"]["status"]["name"] for e in ep).most_common()))

    ni_cur = needs_info(data["needsinfo"], dt(w1), dt(w2) + timedelta(days=1))
    ni_prev = needs_info(data["needsinfo"], dt(w0), dt(w1))
    stock = needs_info_stock(data["needsinfo"])
    sd = [x["days"] for x in stock]
    variants = Counter(v for i in data["needsinfo"] for _w, f_, t_ in transitions(i)
                       for v in (f_, t_)
                       if v and v != NEEDS_INFO and v.lower() == NEEDS_INFO.lower())
    print("\n=== NEEDS INFO (issues that ENTERED in each window) ===")
    if variants:
        print(f"  !! spelling variants in changelogs: {dict(variants)} - "
              f"NEEDS_INFO matches only {NEEDS_INFO!r}, so those stays are invisible")
    print(f"  {'outcome':<28}{'prev':>6}{'cur':>6}")
    for k in ORDER_OUTCOME:
        c, pv = ni_cur["outcome"].get(k, 0), ni_prev["outcome"].get(k, 0)
        print(f"  {k:<28}{pv:>6}{c:>6}")
    for label, key in [("issues entered", "issues"), ("moved forward", "moved_forward"),
                       ("closed without a fix", "closed_without_fix")]:
        print(f"  {label:<28}{ni_prev[key]:>6}{ni_cur[key]:>6}")
    print(f"  {'moved forward %':<28}{ni_prev['moved_forward_pct'] or 0:>6.0f}"
          f"{ni_cur['moved_forward_pct'] or 0:>6.0f}")
    print("  why the closed ones went:", ni_cur["dropped_reasons"] or "none closed")
    print(f"  texture: {ni_cur['visits']} stays, {ni_cur['repeat']} repeat visitors, "
          f"{ni_cur['instant']} same-day flips; dwell >=1d p50 "
          f"{ni_cur['dwell_real_p50'] or 0:.1f}d p85 {ni_cur['dwell_real_p85'] or 0:.1f}d")
    print(f"  sitting there now: {len(stock)}  "
          f"age p50 {pct(sd,50) or 0:.0f}d oldest {max(sd) if sd else 0:.0f}d"
          + (f"  ({stock[0]['key']}, since {stock[0]['since']})" if stock else ""))

    json.dump({"window": {"as_of": str(w2), "cur_from": str(w1), "prev_from": str(w0),
                          "anonymous": auth() is None},
               "cur": cur, "prev": prev, "live": live, "orphans": orph,
               "backlog": len(data["backlog"]), "bugs": len(data["bugs"]),
               "patches": prows,
               "fixversion_coverage": {"tagged": tagged, "fixed": len(fixed_cur)},
               "epics": {"open": len(ep), "closed_in_window": len(data["epics_closed"]),
                         "age_p50": pct(ages, 50),
                         "age_p85": pct(ages, 85), "oldest": max(ages),
                         "by_status": dict(Counter(e["fields"]["status"]["name"] for e in ep))},
               "needs_info": {"cur": ni_cur, "prev": ni_prev,
                              "stock": len(stock), "stock_oldest": stock[:1],
                              "stock_age_p50": pct(sd, 50), "stock_age_p85": pct(sd, 85),
                              "spelling_variants": dict(variants)}},
              open(os.path.join(HERE, "metrics.json"), "w"), indent=1, default=str)
    print("\nwrote metrics.json")


if __name__ == "__main__":
    main()
