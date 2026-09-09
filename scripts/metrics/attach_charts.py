#!/usr/bin/env python3
"""Attach the four chart PNGs to a published Confluence report page.

This is the ONLY step in the whole report that needs a credential. Everything
else runs either anonymously (Jira) or through the Atlassian connector in Claude
Code (reading the previous edition, creating and updating the page). The
connector has no attachment-upload tool, which is the entire reason this script
exists — see `metrics.py --token-help`.

Usage:
    python3 scripts/metrics/attach_charts.py <pageId> [--dry-run] [--charts DIR]

The page must already be published (status `current`). Attaching to a draft is
rejected by the API, so publish first, then attach, then the page body's
<ac:image ri:filename="..."> references resolve.

Upload is **v1 only**. The v2 attachment API has GET and DELETE but no create
operation, so `read:attachment:confluence` — the scope whose name suggests it is
the right one — cannot upload anything. The two scopes that work are
`read:content-details:confluence` + `write:attachment:confluence`.

Both token types are handled, because they need different hosts:

    scoped   Bearer  api.atlassian.com/ex/confluence/<cloudId>/wiki/rest/api/...
    classic  Basic   dhis2.atlassian.net/wiki/rest/api/...

Re-running replaces rather than duplicating, because the upload uses **PUT** on
the collection path — v1's "Create or update attachment" — which versions a
same-named file in place. Two neighbouring routes look right and are not:

    POST /child/attachment              refuses an existing filename outright
    PUT  /child/attachment/<id>/data    needs a scope beyond the two below

PUT /child/attachment is the one that works with
`read:content-details:confluence` + `write:attachment:confluence`.
"""
import json
import mimetypes
import os
import sys
import urllib.error
import urllib.request
import uuid

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import metrics  # noqa: E402  — shares find_token/classify_token/cloud_id

HERE = os.path.dirname(os.path.abspath(__file__))
CHARTS = ["01-journey.png", "02-where-time-goes.png",
          "03-sonarcloud-trend.png", "04-sentry-issues.png"]


def fresh(path, snapshot):
    """A chart older than the snapshot it illustrates must not be uploaded.

    charts.py deletes a chart it skips, so a leftover here means the file predates
    this run - publishing it would put one snapshot in the text and another in the
    picture. Missing is fine and reported; stale is refused.
    """
    if not os.path.exists(path):
        return False
    if os.path.exists(snapshot) and os.path.getmtime(path) < os.path.getmtime(snapshot):
        print(f"REFUSED {os.path.basename(path)}: older than metrics.json - "
              f"re-run charts.py for this snapshot before attaching")
        return False
    return True


def transport():
    """(base_url, headers) for whichever token is configured, or exit with guidance."""
    cred = metrics.find_token()
    kind, who = metrics.classify_token(cred)

    if kind is None:
        sys.exit("No JIRA_AUTH configured, so the charts cannot be attached.\n"
                 "Publish the collapsed tables instead and say so in the report.\n"
                 "Run `python3 scripts/metrics/metrics.py --token-help` for the options.")
    if kind == "rejected":
        sys.exit(f"JIRA_AUTH is set but no auth scheme accepts it ({who}).\n"
                 "Likely revoked, mistyped, or from a different Atlassian account.")
    base, header = metrics.confluence_base(kind, cred)
    return kind, base, {"Authorization": header}


def call(url, headers, method="GET", body=None, content_type=None):
    h = dict(headers)
    h["Accept"] = "application/json"
    # The v1 attachment API applies an XSRF check that this header opts out of.
    # Without it the upload is refused with a bare 403 and no explanation.
    h["X-Atlassian-Token"] = "nocheck"
    if content_type:
        h["Content-Type"] = content_type
    req = urllib.request.Request(url, data=body, headers=h, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            raw = r.read()
            return r.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        return e.code, e.read(600).decode(errors="replace")


def multipart(path):
    """Hand-rolled multipart body — the script stays stdlib-only, like metrics.py."""
    boundary = "----metrics" + uuid.uuid4().hex
    name = os.path.basename(path)
    ctype = mimetypes.guess_type(name)[0] or "application/octet-stream"
    pre = (f"--{boundary}\r\n"
           f'Content-Disposition: form-data; name="file"; filename="{name}"\r\n'
           f"Content-Type: {ctype}\r\n\r\n").encode()
    mid = (f"\r\n--{boundary}\r\n"
           'Content-Disposition: form-data; name="minorEdit"\r\n\r\ntrue\r\n'
           f"--{boundary}--\r\n").encode()
    with open(path, "rb") as f:
        return pre + f.read() + mid, f"multipart/form-data; boundary={boundary}"


def attachments(base, headers, page_id):
    """{filename: {id, fileId, collection}} already on the page.

    One call serves two purposes: deciding POST vs PUT per file, and resolving the
    Media API fileIds the page body needs. `expand=extensions` is what carries
    fileId; without it the body cannot reference the attachment at all.
    """
    code, body = call(f"{base}/rest/api/content/{page_id}/child/attachment"
                      f"?limit=100&expand=extensions", headers)
    if code != 200:
        sys.exit(f"Could not list attachments on page {page_id}: {code} {body}\n"
                 "401 'scope does not match' — grant read:content-details:confluence.\n"
                 "404 — the page is probably still a draft; publish it first.")
    out = {}
    for r in body.get("results", []):
        ext = r.get("extensions", {})
        out[r["title"]] = {"id": r["id"], "fileId": ext.get("fileId"),
                           "collection": ext.get("collectionName",
                                                  f"contentId-{page_id}")}
    return out


def emit_figures(base, headers, page_id, names):
    """Print the figure HTML to paste into the page body.

    The body cannot reference an attachment by filename through the connector's
    HTML+ADF format — it needs the Media API fileId, which only appears under
    `expand=extensions`. Emitting it here keeps the placement step from needing a
    second lookup, and from guessing.

    Note `data-width-type="percentage"`: without it Confluence reads data-width as
    PIXELS, so the documented `data-width="80"` renders an 80px-wide chart.
    """
    by_name = attachments(base, headers, page_id)

    print("\nfigure HTML — paste each above its collapsed table:\n")
    for n in names:
        r = by_name.get(n)
        if not r:
            continue
        fid, coll = r["fileId"], r["collection"]
        print(f'<!-- {n} -->\n'
              f'<figure data-type="media-single" data-layout="center" '
              f'data-width="100" data-width-type="percentage">\n'
              f'  <div data-type="media" data-media-type="file" data-id="{fid}" '
              f'data-collection="{coll}" data-alt="{n}"></div>\n'
              f'</figure>\n')


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if not args:
        sys.exit(__doc__)
    page_id = args[0]
    dry = "--dry-run" in sys.argv
    cdir = os.path.join(HERE, "charts")
    if "--charts" in sys.argv:
        cdir = sys.argv[sys.argv.index("--charts") + 1]

    files = [os.path.join(cdir, c) for c in CHARTS]
    missing = [os.path.basename(f) for f in files if not os.path.exists(f)]
    if missing:
        print(f"!  not generated, will be skipped: {', '.join(missing)}", file=sys.stderr)
        print("   run charts.py first; without Chrome it emits SVG only and no PNG.",
              file=sys.stderr)
    snapshot = os.path.join(HERE, "metrics.json")
    files = [f for f in files if fresh(f, snapshot)]
    if not files:
        sys.exit("No chart PNGs to attach.")

    kind, base, headers = transport()
    state, detail = metrics.charts_access(page_id)
    print(f"token: {kind}   endpoint: {base}")
    print(f"upload access: {state} ({detail})")
    if state != "ok":
        sys.exit("Cannot upload. Grant read:content-details:confluence + "
                 "write:attachment:confluence\non the token (no Jira scopes), or publish "
                 "the collapsed tables instead and say so in the report.")
    if dry:
        for f in files:
            print(f"  would attach {os.path.basename(f)} ({os.path.getsize(f)//1024} KB)")
        return
    have = attachments(base, headers, page_id)
    ok = []
    for f in files:
        name = os.path.basename(f)
        body, ctype = multipart(f)
        # PUT on the collection path creates or versions by filename. POST would be
        # refused for a name that already exists, and PUT .../<id>/data needs a
        # scope these two do not include.
        url = f"{base}/rest/api/content/{page_id}/child/attachment"
        verb = "replaced" if name in have else "added"
        code, resp = call(url, headers, "PUT", body, ctype)
        if code in (200, 201):
            print(f"  {verb:9} {name}")
            ok.append(name)
        else:
            print(f"  FAIL      {name}: {code} {str(resp)[:220]}")

    if ok:
        emit_figures(base, headers, page_id, ok)


if __name__ == "__main__":
    main()
