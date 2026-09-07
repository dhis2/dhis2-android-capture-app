#!/usr/bin/env python3
"""Render the report's four charts from the metrics snapshot.

Flow numbers come from metrics.json (written by metrics.py in the same run),
SonarCloud history is fetched here, and the Sentry figures are passed on the
command line — Sentry is only reachable through the MCP tools, so whoever ran
those tools already has the numbers and hands them straight over. Nothing is
written to disk except the charts themselves.

Each chart is emitted three ways:

  charts/NN-name.svg    vector source, colours as var(--role, #fallback)
  charts/NN-name.png    2x raster, the file attached to Confluence
  charts/tables.html    the collapsed data table for each chart, already in
                        Confluence storage format, so no number is retyped

Usage:
    python3 scripts/metrics/charts.py --as-of 2026-08-11 \
      --release 3.4.1 \
      --issue "89RF|NullPointerException|ProgramFragment.showSyncDialog|4500|3.4.1 only|new" \
      --issue "87NX|TooManyRequests|NetworkStatusProviderImpl|1971|since 3.4.0.1|old"

--issue is ID|Title|EntryPoint|Users|Scope|new or old, repeated, highest first;
"new" means the issue exists only in this release, i.e. a regression.

--as-of must be the report's window end, not today: the charts have to agree
with the page they illustrate. See references/metrics-reference.md.
"""
import argparse
import datetime as dt
import json
import os
import re
import subprocess
import sys
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "charts")
SONAR = ("https://sonarcloud.io/api/measures/search_history"
         "?component=dhis2_dhis2-android-capture-app&branch=develop"
         "&metrics=coverage,code_smells,sqale_index,duplicated_lines_density,ncloc&ps=1000")

# Palette: dataviz reference instance, validated for CVD in both modes.
# Written as var(--role, #light) so the SVG stands alone but can be re-themed.
C = {
    "ink": "var(--viz-ink, #0b0b0b)", "ink2": "var(--viz-ink2, #52514e)",
    "muted": "var(--viz-muted, #898781)", "grid": "var(--viz-grid, #e1e0d9)",
    "axis": "var(--viz-axis, #c3c2b7)", "surface": "var(--viz-surface, #fcfcfb)",
    "s1": "var(--viz-s1, #2a78d6)", "s2": "var(--viz-s2, #eb6834)",
    "s3": "var(--viz-s3, #1baf7a)", "crit": "var(--viz-crit, #d03b3b)",
    "long": "var(--viz-long, #898781)",
}
FONT = '"IBM Plex Sans", system-ui, -apple-system, "Segoe UI", sans-serif'
M = 20  # outer margin


def esc(s):
    return str(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def txt(x, y, s, size=12, fill=None, anchor="start", weight=400, tab=False):
    t = ' style="font-variant-numeric:tabular-nums"' if tab else ""
    return (f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" font-weight="{weight}" '
            f'fill="{fill or C["ink2"]}" text-anchor="{anchor}"{t}>{esc(s)}</text>')


def svg_open(w, h, title, subtitle=None):
    s = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" '
         f'height="{h}" font-family=\'{FONT}\' role="img" aria-label="{esc(title)}">',
         f'<style>text{{font-family:{FONT}}}</style>',
         f'<g transform="translate({M},12)">',
         txt(0, 18, title, size=15, fill=C["ink"], weight=600)]
    if subtitle:
        s.append(txt(0, 38, subtitle, size=12, fill=C["muted"]))
    return s


def bar(x, y, w, h, fill, side="right"):
    """Bar rounded 4px on the data end only, square at the baseline."""
    w = max(w, 0.6)
    r = min(4, w / 2, h / 2)
    if side == "right":
        d = (f"M{x:.1f},{y:.1f} H{x+w-r:.1f} A{r},{r} 0 0 1 {x+w:.1f},{y+r:.1f} "
             f"V{y+h-r:.1f} A{r},{r} 0 0 1 {x+w-r:.1f},{y+h:.1f} H{x:.1f} Z")
    else:
        d = f"M{x:.1f},{y:.1f} H{x+w:.1f} V{y+h:.1f} H{x:.1f} Z"
    return f'<path d="{d}" fill="{fill}"/>'


def table(headers, rows, note=None):
    """A chart's numbers as a collapsed expand, in Confluence HTML+ format.

    HTML+ (`<details>`, `data-type` nodes) is what both createConfluencePage and the
    editor accept. Storage-format XML (`<ac:structured-macro>`) renders as raw text
    through those paths — do not switch this back.
    """
    th = "".join(f"<th><p><strong>{esc(h)}</strong></p></th>" for h in headers)
    body = "".join("<tr>" + "".join(f"<td><p>{esc(c)}</p></td>" for c in r) + "</tr>"
                   for r in rows)
    n = f"<p><em>{esc(note)}</em></p>" if note else ""
    return ("<details><summary>Data table</summary>"
            f"<table><tbody><tr>{th}</tr>{body}</tbody></table>{n}</details>")


# --------------------------------------------------------------------------
# 1. The journey: intake + delivery + post-merge, previous vs current
# --------------------------------------------------------------------------
def chart_journey(m):
    cur, prev = m["cur"], m["prev"]
    keys = [("intake_p50", "Intake — created to committed", C["s1"]),
            ("deliv_p50", "Delivery — committed to merged", C["s2"]),
            ("post_merge_p50", "Post-merge — merged to done", C["s3"])]
    rows = [("Previous 90 days", [prev[k] for k, _, _ in keys]),
            ("Current 90 days", [cur[k] for k, _, _ in keys])]
    tot_prev, tot_now = sum(rows[0][1]), sum(rows[1][1])

    W, H, IW = 900, 300, 900 - 2 * M
    top, bar_h, gap = 96, 46, 34
    scale = (IW - 110) / max(tot_prev, tot_now, 1) * 0.92
    delta = tot_prev - tot_now
    # NB: this is the sum of three medians, which is NOT the median lead time —
    # never label it "lead time", the trend table carries that and they differ.
    s = svg_open(W, H, "The journey end to end",
                 f"Median days in each stage, summed. {abs(delta):.1f} days "
                 f"{'shorter' if delta >= 0 else 'longer'} than last window.")
    for k, (_, label, col) in enumerate(keys):
        lx = k * (IW / 3)
        s.append(f'<rect x="{lx:.0f}" y="58" width="10" height="10" rx="2" fill="{col}"/>')
        s.append(txt(lx + 16, 67, label, size=12, fill=C["ink2"]))
    for i, (label, vals) in enumerate(rows):
        y = top + i * (bar_h + gap)
        s.append(txt(0, y - 8, label, size=12, fill=C["ink2"], weight=600))
        cx = 0
        for j, v in enumerate(vals):
            w = v * scale
            last = j == len(vals) - 1
            s.append(bar(cx, y, w - (0 if last else 2), bar_h, keys[j][2],
                         "right" if last else "flat"))
            # aqua is sub-3:1 on the light surface, so every segment is labelled
            if w > 46:
                s.append(txt(cx + w / 2 - 1, y + bar_h / 2 + 4, f"{v:.1f} d", size=12,
                             fill=C["surface"], anchor="middle", weight=600, tab=True))
            cx += w
        s.append(txt(cx + 10, y + bar_h / 2 + 5, f"{sum(vals):.1f} d", size=13,
                     fill=C["ink"], weight=600, tab=True))
    rule = top + 2 * (bar_h + gap) - 22
    s.append(f'<line x1="0" y1="{rule}" x2="{IW}" y2="{rule}" stroke="{C["grid"]}" '
             f'stroke-width="1"/>')
    share = cur["intake_p50"] / tot_now * 100 if tot_now else 0
    big_k, big_lbl, _ = max(keys, key=lambda k: cur[k[0]])
    s.append(txt(0, H - 26, f"The largest single stage is {big_lbl.split(' — ')[0].lower()}, at "
                            f"{cur[big_k]:.1f} d. Intake is {share:.0f}% of the journey and the "
                            f"part the team does not control.", size=12, fill=C["muted"]))
    s.append("</g></svg>")

    rows_t = [[lbl, f"{prev[k]:.1f} d", f"{cur[k]:.1f} d"] for k, lbl, _ in keys]
    rows_t.append(["Sum of stage medians", f"{tot_prev:.1f} d", f"{tot_now:.1f} d"])
    return "\n".join(s), table(["Stage", "Previous 90 days", "Current 90 days"], rows_t,
                               "Medians (p50). The stages are disjoint, but a sum of medians is not the "
                               "median lead time — see the trend table for that.")


# --------------------------------------------------------------------------
# 2. Where the time goes: stage shares, previous vs current
# --------------------------------------------------------------------------
def chart_stages(m, top_n=7):
    cur, prev = m["cur"]["stages"], m["prev"]["stages"]
    # rank by the larger of the two shares, so a stage that collapsed still shows
    ranked = sorted(set(cur) | set(prev),
                    key=lambda k: -max(cur.get(k, {}).get("share", 0),
                                       prev.get(k, {}).get("share", 0)))
    keep, rest = ranked[:top_n], ranked[top_n:]
    data = [(k, prev.get(k, {}).get("share", 0), cur.get(k, {}).get("share", 0),
             cur.get(k, {}).get("p85"), cur.get(k, {}).get("kind", "")) for k in keep]
    if rest:
        data.append((f"All other stages ({len(rest)})",
                     sum(prev.get(k, {}).get("share", 0) for k in rest),
                     sum(cur.get(k, {}).get("share", 0) for k in rest), None, ""))

    W, IW = 900, 900 - 2 * M
    top, row_h, bh = 96, 44, 15
    H = top + len(data) * row_h + 90
    lab_w, plot_w = 215, IW - 215 - 50
    step = 15 if max(max(p, c) for _, p, c, _, _ in data) > 30 else 5
    axis_max = (int(max(max(p, c) for _, p, c, _, _ in data) / step) + 1) * step
    scale = plot_w / axis_max
    queue = sum(c for _, _, c, _, k in data if k == "wait")
    s = svg_open(W, H, "Where the time goes",
                 "Share of all tracked time, by status. "
                 f"Queues account for {queue:.0f}% of the current window.")
    for i, (name, col) in enumerate([("Previous 90 days", C["s1"]), ("Current 90 days", C["s2"])]):
        lx = i * 170
        s.append(f'<rect x="{lx}" y="58" width="10" height="10" rx="2" fill="{col}"/>')
        s.append(txt(lx + 16, 67, name, size=12, fill=C["ink2"]))
    for g in range(0, axis_max + 1, step):
        x = lab_w + g * scale
        s.append(f'<line x1="{x:.1f}" y1="{top-6}" x2="{x:.1f}" '
                 f'y2="{top + len(data)*row_h - 12}" stroke="{C["grid"]}" stroke-width="1"/>')
        s.append(txt(x, top + len(data) * row_h + 6, f"{g}%", size=11, fill=C["muted"],
                     anchor="middle", tab=True))
    for i, (name, p, c, _, kind) in enumerate(data):
        y = top + i * row_h
        s.append(txt(lab_w - 12, y + 18, f"{name} ({kind})" if kind else name,
                     size=12, fill=C["ink2"], anchor="end"))
        for j, (v, col) in enumerate([(p, C["s1"]), (c, C["s2"])]):
            by = y + j * (bh + 2)      # 2px surface gap, never a border
            s.append(bar(lab_w, by, v * scale, bh, col))
            s.append(txt(lab_w + v * scale + 7, by + 12, f"{v:.1f}", size=11,
                         fill=C["muted"], tab=True))
    s.append(txt(0, H - 26, "A stage whose share collapses between windows is usually a cleanup "
                            "of old items, not a speed-up — check its p85 before reading it as "
                            "progress.", size=12, fill=C["muted"]))
    s.append("</g></svg>")

    rows_t = [[n, f"{p:.1f}%", f"{c:.1f}%", f"{p85:.1f} d" if p85 is not None else "—"]
              for n, p, c, p85, _ in data]
    return "\n".join(s), table(["Status", "Previous", "Current", "p85 duration"], rows_t,
                               "Queue stages are waiting time, active stages are worked time. "
                               "A high share with a low p85 comes from a few extreme outliers.")


# --------------------------------------------------------------------------
# 3. SonarCloud: 12-month trend, one panel per metric (never a shared axis)
# --------------------------------------------------------------------------
def chart_sonar(as_of):
    try:
        with urllib.request.urlopen(SONAR, timeout=30) as r:
            raw = json.load(r)
    except Exception as e:                                    # noqa: BLE001
        print(f"  ! SonarCloud unreachable ({e}) — skipping chart 3", file=sys.stderr)
        return None, None
    cut = as_of.isoformat()
    # search_history returns the full retained series, so window it explicitly —
    # otherwise the panel says "12 months" and plots two years, and a metric can
    # trend the opposite way over the longer span.
    start = (as_of - dt.timedelta(days=365)).isoformat()
    ser = {m["metric"]: [(dt.datetime.strptime(p["date"][:10], "%Y-%m-%d"), float(p["value"]))
                         for p in m["history"]
                         if p.get("value") and start <= p["date"][:10] <= cut]
           for m in raw["measures"]}
    need = ("coverage", "code_smells", "sqale_index", "duplicated_lines_density")
    if not all(ser.get(k) for k in need):
        print("  ! SonarCloud history empty for the window — skipping chart 3", file=sys.stderr)
        return None, None
    panels = [("Test coverage", ser["coverage"], "{:.1f}%", "higher is better"),
              ("Code smells", ser["code_smells"], "{:.0f}", "lower is better"),
              ("Technical debt", [(d, v / 60) for d, v in ser["sqale_index"]], "{:.0f} h",
               "lower is better"),
              ("Duplicated lines", ser["duplicated_lines_density"], "{:.1f}%",
               "lower is better")]
    W, H = 940, 520
    pw, ph, oy = 400, 130, 92
    s = svg_open(W, H, "Code quality on develop, 12 months",
                 "Four separate scales, four separate panels — never two y-axes on one plot. "
                 f"Series end at the report date, {as_of.strftime('%d %b %Y').lstrip('0')}.")
    for k, (name, pts, fmt, dirn) in enumerate(panels):
        px, py = (k % 2) * (pw + 60), oy + (k // 2) * (ph + 86)
        xs = [d.timestamp() for d, _ in pts]
        ys = [v for _, v in pts]
        lo, hi = min(ys), max(ys)
        pad = (hi - lo) * 0.18 or 1
        lo, hi = lo - pad, hi + pad
        spanx = (max(xs) - min(xs)) or 1
        fx = lambda t: px + (t - min(xs)) / spanx * pw            # noqa: E731
        fy = lambda v: py + ph - (v - lo) / (hi - lo) * ph        # noqa: E731
        s.append(txt(px, py - 26, name, size=13, fill=C["ink"], weight=600))
        s.append(txt(px + pw, py - 26, dirn, size=11, fill=C["muted"], anchor="end"))
        s.append(f'<line x1="{px}" y1="{py+ph:.1f}" x2="{px+pw}" y2="{py+ph:.1f}" '
                 f'stroke="{C["axis"]}" stroke-width="1"/>')
        d = " ".join(("M" if i == 0 else "L") + f"{fx(t):.1f},{fy(v):.1f}"
                     for i, (t, v) in enumerate(zip(xs, ys)))
        s.append(f'<path d="{d}" fill="none" stroke="{C["s1"]}" stroke-width="2" '
                 f'stroke-linejoin="round" stroke-linecap="round"/>')
        for t, v, anc in ((xs[0], ys[0], "start"), (xs[-1], ys[-1], "end")):
            s.append(f'<circle cx="{fx(t):.1f}" cy="{fy(v):.1f}" r="4" fill="{C["s1"]}" '
                     f'stroke="{C["surface"]}" stroke-width="2"/>')
            s.append(txt(fx(t), fy(v) - 12, fmt.format(v), size=12, fill=C["ink"],
                         anchor=anc, weight=600, tab=True))
        s.append(txt(px, py + ph + 18, pts[0][0].strftime("%b %Y"), size=11, fill=C["muted"]))
        s.append(txt(px + pw, py + ph + 18, pts[-1][0].strftime("%b %Y"), size=11,
                     fill=C["muted"], anchor="end"))
    s.append(txt(0, H - 26, "Read each panel on its own scale; the four are not comparable with "
                            "each other.", size=12, fill=C["muted"]))
    s.append("</g></svg>")

    rows_t = []
    for name, pts, fmt, dirn in panels:
        first, last = pts[0][1], pts[-1][1]
        better = (last > first) if dirn.startswith("higher") else (last < first)
        rows_t.append([name, fmt.format(first), fmt.format(last),
                       "improving" if better else ("flat" if last == first else "worsening")])
    if ser.get("ncloc"):
        n0, n1 = ser["ncloc"][0][1], ser["ncloc"][-1][1]
        rows_t.append(["Lines of code", f"{n0:,.0f}", f"{n1:,.0f}", f"{(n1-n0)/n0*100:+.0f}%"])
    start = panels[0][1][0][0].strftime("%b %Y")
    return "\n".join(s), table(["Metric", start, as_of.strftime("%d %b %Y").lstrip("0"),
                                "Direction"], rows_t,
                               "Series are cut at the report date, not today, so they agree with "
                               "the rest of the page.")


# --------------------------------------------------------------------------
# 4. Sentry: users affected, split by whether the issue is a regression
# --------------------------------------------------------------------------
def chart_sentry(issues, release):
    if not issues:
        print("  ! no --issue given — skipping chart 4", file=sys.stderr)
        return None, None
    issues = issues[:6]
    W, IW = 900, 900 - 2 * M
    top, row_h, bh = 104, 48, 18
    H = top + len(issues) * row_h + 60
    lab_w, plot_w = 250, IW - 250 - 200
    scale = plot_w / max(i["users"] for i in issues)
    n_reg = sum(1 for i in issues if i["regression"])
    s = svg_open(W, H, "Production issues by users affected",
                 f"Top {len(issues)} unresolved issues on {release}. Counts overlap between "
                 "issues and must never be summed.")
    for k, (col, lbl) in enumerate([(C["crit"], f"New in {release} — regression"),
                                    (C["long"], "Pre-existing")]):
        lx = k * 230
        s.append(f'<rect x="{lx}" y="62" width="10" height="10" rx="2" fill="{col}"/>')
        s.append(txt(lx + 16, 71, lbl, size=12, fill=C["ink2"]))
    for i, it in enumerate(issues):
        y = top + i * row_h
        reg = it["regression"]
        s.append(txt(lab_w - 12, y + 10, f"{it['id']}  {it['title']}", size=12, fill=C["ink"],
                     anchor="end", weight=600))
        s.append(txt(lab_w - 12, y + 25, it["where"], size=11, fill=C["muted"], anchor="end"))
        s.append(bar(lab_w, y, it["users"] * scale, bh, C["crit"] if reg else C["long"]))
        s.append(txt(lab_w + it["users"] * scale + 10, y + 14, f"{it['users']:,} users",
                     size=12, fill=C["ink"], weight=600, tab=True))
        # a status colour never carries meaning on its own: mark + written scope
        s.append(txt(IW, y + 14, ("▲ " if reg else "") + it["scope"], size=11,
                     fill=C["crit"] if reg else C["muted"], anchor="end",
                     weight=600 if reg else 400))
    s.append(f'<line x1="{lab_w}" y1="{top-14}" x2="{lab_w}" y2="{top+len(issues)*row_h-18}" '
             f'stroke="{C["axis"]}" stroke-width="1"/>')
    tail = (f"{n_reg} of the top {len(issues)} exist only in this release."
            if n_reg else "No issue in the top list is confined to this release.")
    s.append(txt(0, H - 26, tail + " Aggregate error rates hide that; per-release scoping is "
                                   "what surfaces it.", size=12, fill=C["muted"]))
    s.append("</g></svg>")

    rows_t = [[f"{i['id']} {i['title']}", f"{i['users']:,}", i["where"], i["scope"]]
              for i in issues]
    return "\n".join(s), table(["Issue", "Users affected", "Entry point", "Releases"], rows_t,
                               "Users-affected counts overlap between issues and must never be "
                               "added together.")


def parse_issue(spec):
    """ID|Title|EntryPoint|Users|Scope|new or old"""
    p = [x.strip() for x in spec.split("|")]
    if len(p) != 6:
        raise argparse.ArgumentTypeError(
            f"--issue needs 6 fields separated by |, got {len(p)}: {spec}")
    if p[5] not in ("new", "old"):
        raise argparse.ArgumentTypeError("last field must be 'new' (regression) or 'old'")
    return {"id": p[0], "title": p[1], "where": p[2], "users": int(p[3].replace(",", "")),
            "scope": p[4], "regression": p[5] == "new"}


# --------------------------------------------------------------------------
def render_png(svg_path):
    """2x PNG via headless Chrome, with the webfont loaded. Optional."""
    body = open(svg_path).read()
    w, h = re.search(r'viewBox="0 0 (\d+) (\d+)"', body).groups()
    wrap = ('<!doctype html><meta charset="utf-8">'
            '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?'
            'family=IBM+Plex+Sans:wght@400;500;600&display=swap">'
            "<style>html,body{margin:0;padding:0;background:#fff}svg{display:block}</style>"
            + body)
    tmp, png = svg_path + ".html", svg_path[:-4] + ".png"
    open(tmp, "w").write(wrap)
    try:
        for exe in ("google-chrome", "chromium", "chromium-browser"):
            try:
                subprocess.run([exe, "--headless=new", "--disable-gpu", "--no-sandbox",
                                "--hide-scrollbars", "--force-device-scale-factor=2",
                                "--default-background-color=FFFFFFFF",
                                "--virtual-time-budget=5000", f"--window-size={w},{h}",
                                f"--screenshot={png}", f"file://{tmp}"],
                               capture_output=True, check=True, timeout=90)
                return png
            except (FileNotFoundError, subprocess.SubprocessError):
                continue
    finally:
        os.unlink(tmp)
    print(f"  ! no Chrome found — {os.path.basename(svg_path)} has no PNG; attach the SVG "
          "or install Chrome", file=sys.stderr)
    return None


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--as-of", required=True, help="report window end, YYYY-MM-DD")
    ap.add_argument("--release", default="the current release",
                    help="production release the Sentry issues are scoped to, e.g. 3.4.1")
    ap.add_argument("--issue", action="append", type=parse_issue, default=[],
                    metavar="ID|Title|Where|Users|Scope|new|old",
                    help="repeat, highest users first; 'new' means regression")
    ap.add_argument("--metrics", default=os.path.join(HERE, "metrics.json"))
    ap.add_argument("--no-png", action="store_true", help="emit SVG only")
    a = ap.parse_args()

    as_of = dt.datetime.strptime(a.as_of, "%Y-%m-%d").date()
    if as_of > dt.date.today():
        sys.exit("--as-of is in the future")
    if not os.path.exists(a.metrics):
        sys.exit(f"{a.metrics} not found — run metrics.py first")
    m = json.load(open(a.metrics))
    os.makedirs(OUT, exist_ok=True)

    built = [("01-journey", *chart_journey(m)),
             ("02-where-time-goes", *chart_stages(m)),
             ("03-sonarcloud-trend", *chart_sonar(as_of)),
             ("04-sentry-issues", *chart_sentry(a.issue, a.release))]

    tables, made = [], []
    for name, svg, tbl in built:
        if svg is None:
            continue
        p = os.path.join(OUT, name + ".svg")
        open(p, "w").write(svg)
        made.append(name)
        png = None if a.no_png else render_png(p)
        print(f"  {name}.svg" + (f" + {name}.png" if png else ""))
        tables.append(f"<!-- {name} -->\n{tbl}")
    open(os.path.join(OUT, "tables.html"), "w").write("\n\n".join(tables))

    print(f"\nwrote {len(made)}/4 charts to {OUT}")
    print("collapsed data tables, in Confluence storage format: charts/tables.html")
    if len(made) < 4:
        print("say in the report which chart is missing and why — never drop one silently")


if __name__ == "__main__":
    main()
