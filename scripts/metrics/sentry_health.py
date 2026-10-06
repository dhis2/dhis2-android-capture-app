#!/usr/bin/env python3
"""Release health per production release: crash-free sessions and users, ANR rate.

Usage:  python3 sentry_health.py [--as-of YYYY-MM-DD] [--preflight]

The Sentry MCP has no release-health tool — get_release_details(includeHealth=true)
returns nothing and the metrics dataset has no session gauges — so this one figure
needs a Sentry API token. Session tracking itself has always been on: sentry-android
enables it by default and the app never turns it off. Reporting "crash-free rate is
unavailable" was a gap in this tooling, not in the app.

Token lookup, first hit wins:
    SENTRY_METRICS_TOKEN   env, or local.properties in this checkout or the main one
    ~/.sentryclirc         the [auth] token sentry-cli already uses, if you have one

Create one at https://dhis2.sentry.io/settings/account/api/auth-tokens/ with only
`org:read` — the sessions endpoint needs nothing more. Not SENTRY_AUTH_TOKEN: Gradle
reads that one to upload R8 mappings, and a read-only token there would break them.

Without a token the report still publishes; the Crash / ANR section says crash-free
rate was not fetched (no token) — never that session tracking is off.
"""

import configparser
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta, timezone

from metrics import HERE, main_checkout, REPO

ORG = "dhis2"
PROJECT_ID = "5676228"            # dhis2-android-capture
API = "https://us.sentry.io/api/0"
ENVIRONMENT = "production"        # debug builds report environment "debug"
MIN_USERS = 500                   # same noise floor as the crash-load table
FIELDS = ["sum(session)", "count_unique(user)",
          "crash_free_rate(session)", "crash_free_rate(user)",
          "anr_rate()", "foreground_anr_rate()"]
TOKEN_VAR = "SENTRY_METRICS_TOKEN"


def find_token():
    t = os.environ.get(TOKEN_VAR)
    if t:
        return t, "env"
    for root in [REPO, main_checkout()]:
        if not root:
            continue
        lp = os.path.join(root, "local.properties")
        if os.path.exists(lp):
            for line in open(lp):
                if line.startswith(f"{TOKEN_VAR}="):
                    return line.split("=", 1)[1].strip(), "local.properties"
    rc = os.path.expanduser("~/.sentryclirc")
    if os.path.exists(rc):
        cp = configparser.ConfigParser()
        cp.read(rc)
        t = cp.get("auth", "token", fallback=None)
        if t:
            return t.strip(), "~/.sentryclirc"
    return None, None


def sessions(token, start, end):
    params = [("project", PROJECT_ID), ("environment", ENVIRONMENT),
              ("groupBy", "release"), ("orderBy", "-sum(session)"), ("per_page", "100"),
              ("interval", "1d"),
              ("start", f"{start}T00:00:00Z"), ("end", f"{end}T00:00:00Z")]
    params += [("field", f) for f in FIELDS]
    url = f"{API}/organizations/{ORG}/sessions/?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {token}",
                                               "Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def short(release):
    """com.dhis2@3.4.2+157 -> 3.4.2"""
    return release.split("@", 1)[-1].split("+", 1)[0]


def preflight():
    token, src = find_token()
    if not token:
        print(f"NOTE  Sentry health  no token — crash-free rate will be omitted. Set "
              f"{TOKEN_VAR} (org:read) in local.properties.")
        return False
    end = datetime.now(timezone.utc).date()
    try:
        d = sessions(token, end - timedelta(days=1), end)
        print(f"OK    Sentry health  token from {src}, "
              f"{len(d.get('groups', []))} release(s) with sessions yesterday")
        return True
    except urllib.error.HTTPError as e:
        print(f"FAIL  Sentry health  token from {src} rejected: HTTP {e.code}. "
              f"It needs org:read on {ORG}.")
        return False


def main():
    if "--preflight" in sys.argv:
        sys.exit(0 if preflight() else 1)

    as_of = datetime.now(timezone.utc).date()
    for i, a in enumerate(sys.argv):
        if a == "--as-of" and i + 1 < len(sys.argv):
            as_of = datetime.strptime(sys.argv[i + 1], "%Y-%m-%d").date()
    # Sessions are kept for 90 days, so only the current window exists — there is no
    # previous-window comparison for this figure; compare releases instead.
    start = as_of - timedelta(days=90)

    token, src = find_token()
    if not token:
        sys.exit(f"no Sentry token: set {TOKEN_VAR} (scope org:read). See the docstring.")
    d = sessions(token, start, as_of)

    rows = []
    for g in d.get("groups", []):
        t = g["totals"]
        if (t.get("count_unique(user)") or 0) < MIN_USERS:
            continue
        rows.append({"release": short(g["by"]["release"]),
                     "sessions": t["sum(session)"], "users": t["count_unique(user)"],
                     "crash_free_sessions": t["crash_free_rate(session)"],
                     "crash_free_users": t["crash_free_rate(user)"],
                     "anr_users": t["anr_rate()"],
                     "foreground_anr_users": t["foreground_anr_rate()"]})

    print(f"=== RELEASE HEALTH {start} .. {as_of} (env {ENVIRONMENT}, "
          f">= {MIN_USERS} users, token from {src}) ===")
    print(f"  {'release':<10}{'users':>9}{'sessions':>11}{'CF sess':>9}{'CF users':>10}"
          f"{'ANR usr':>9}{'fg ANR':>8}")
    for r in rows:
        print(f"  {r['release']:<10}{r['users']:>9,}{r['sessions']:>11,}"
              f"{r['crash_free_sessions']*100:>8.2f}%{r['crash_free_users']*100:>9.2f}%"
              f"{r['anr_users']*100:>8.2f}%{r['foreground_anr_users']*100:>7.2f}%")

    json.dump({"window": {"from": str(start), "as_of": str(as_of)},
               "environment": ENVIRONMENT, "releases": rows},
              open(os.path.join(HERE, "sentry_health.json"), "w"), indent=1)
    print("\nwrote sentry_health.json")


if __name__ == "__main__":
    main()
