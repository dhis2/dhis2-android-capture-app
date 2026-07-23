#!/usr/bin/env python3
"""Transifex sync for the DHIS2 Android Capture App — pure REST API, no tx CLI.

Talks directly to the Transifex v3 API (https://rest.api.transifex.com), so the
only external tools needed are Python 3, git and gh. Config-driven: the resource
list, source files, `file_filter` patterns, `lang_map` and `source_lang` are all
read from ./.tx/config in the current working directory (the branch worktree).

Auth: a Transifex API token, looked up in this order:
  1. $TX_TOKEN environment variable
  2. TX_TOKEN=... in ./local.properties     (primary, gitignored)
  3. ~/.transifexrc                          (legacy fallback)
Because local.properties is gitignored (absent from worktrees), the orchestrator
should `export TX_TOKEN=$(...)` before spawning worktree agents.

Subcommands (all operate on the current working directory):

  push-sources --branch <develop|main>
      Upload every source file to its `<branch>--<slug>` resource via the async
      strings-upload API, waiting for each job. Reports created/updated/deleted/
      skipped and any server-side string errors. Exits non-zero on any failure.

  verify-push --branch <develop|main>
      Independent check: confirm every local (translatable) source key — plurals
      and string-arrays included — is present on the server. Exits non-zero if a
      key is missing (a silent push failure). Also lists stale orphan keys.

  lang-list --branch <develop|main>
      Print the comma-separated Transifex language codes with >=1 translated
      string on that branch's resources, source language excluded.

  pull --branch <develop|main> [--langs a,b,c] [--workers N]
      Download translations (default mode) for the chosen languages across all
      resources via the async download API and write them to the paths given by
      `file_filter` + `lang_map`. Skips creating brand-new empty stub files.
      With no --langs, uses the lang-list selection.

  postpull
      Clean up a freshly pulled tree: unescape `\\?`->`?` in KMP compose modules,
      delete new empty stubs, revert whitespace-only churn on already-empty
      files, restore any dropped in-source translation from HEAD, and validate
      (well-formed XML + keys subset of source). Exits non-zero on validation
      failure.

  push-translations --branch <develop|main> [--apply] [--workers N]
      Backfill: for every repo translation file, find keys that are in the
      source and translated in the repo but MISSING on the server, and upload
      only those gap keys (never overwriting an existing server translation).
      This closes the develop-vs-main divergence so future pulls stop dropping
      and re-restoring the same in-source translations. Dry-run by default;
      pass --apply to actually upload. On a per-language download error it skips
      that pair rather than risk an overwrite.
"""

import argparse
import base64
import glob
import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request
import xml.dom.minidom
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed

API = "https://rest.api.transifex.com"
JSON_CT = "application/vnd.api+json"
HTTP_TIMEOUT = 60  # seconds; guards every urlopen so a stalled connection can't hang

# ---------------------------------------------------------------------------
# token + config parsing
# ---------------------------------------------------------------------------


def load_token():
    tok = os.environ.get("TX_TOKEN")
    if tok:
        return tok.strip()
    lp = "local.properties"
    if os.path.exists(lp):
        with open(lp, encoding="utf-8") as fh:
            for line in fh:
                s = line.strip()
                if "=" in s and s.split("=", 1)[0].strip() == "TX_TOKEN":
                    return s.split("=", 1)[1].strip()
    rc = os.path.expanduser("~/.transifexrc")
    if os.path.exists(rc):
        section, in_www, fallback = None, None, None
        with open(rc, encoding="utf-8") as fh:
            for line in fh:
                s = line.strip()
                if s.startswith("[") and s.endswith("]"):
                    section = s[1:-1]
                elif s.startswith("token"):
                    val = s.split("=", 1)[1].strip()
                    fallback = fallback or val
                    if section == "https://www.transifex.com":
                        in_www = val
        if in_www or fallback:
            return in_www or fallback
    sys.exit("No token: set TX_TOKEN, or add TX_TOKEN=... to local.properties.")


def parse_tx_config(path=".tx/config"):
    """Return (project_id, source_lang, lang_map, resources[])."""
    if not os.path.exists(path):
        sys.exit(f"{path} not found — run from the repo/worktree root.")
    project_id, source_lang, lang_map, resources = None, "en", {}, []
    cur = None
    with open(path, encoding="utf-8") as fh:
        lines = fh.readlines()
    for line in lines:
        s = line.strip()
        m = re.match(r"^\[o:([^:]+):p:([^:]+):r:(.+)\]$", s)
        if m:
            if cur:
                resources.append(cur)
            project_id = f"o:{m.group(1)}:p:{m.group(2)}"
            cur = {"slug": m.group(3)}
            continue
        if s.startswith("[") and cur:
            resources.append(cur)
            cur = None
        if "=" in s:
            k, v = [x.strip() for x in s.split("=", 1)]
            if k == "lang_map":
                for pair in v.split(","):
                    if ":" in pair:
                        a, b = [x.strip() for x in pair.split(":", 1)]
                        lang_map[a] = b
            if cur is not None and k in ("source_file", "file_filter"):
                cur[k] = v
            if k == "source_lang":
                source_lang = v
                if cur is not None:
                    cur["source_lang"] = v
    if cur:
        resources.append(cur)
    if not project_id:
        sys.exit("Could not parse project id from .tx/config.")
    return project_id, source_lang, lang_map, resources


def android_folder(tx_code, lang_map):
    """Transifex language code -> Android values-<qualifier> suffix."""
    if tx_code in lang_map:
        return lang_map[tx_code]
    if "_" in tx_code:  # xx_YY -> xx-rYY (Android region convention)
        base, region = tx_code.split("_", 1)
        return f"{base}-r{region}"
    return tx_code


# ---------------------------------------------------------------------------
# HTTP
# ---------------------------------------------------------------------------


def _req(url, tok, method="GET", body=None, accept="*/*"):
    headers = {"Authorization": "Bearer " + tok, "Accept": accept}
    data = None
    if body is not None:
        data = json.dumps(body).encode()
        headers["Content-Type"] = JSON_CT
    return urllib.request.Request(url, data=data, headers=headers, method=method)


def api_get(url, tok):
    with urllib.request.urlopen(_req(url, tok, accept=JSON_CT), timeout=HTTP_TIMEOUT) as r:
        return json.load(r)


def api_post(path, body, tok):
    req = _req(API + path, tok, "POST", body, JSON_CT)
    with urllib.request.urlopen(req, timeout=HTTP_TIMEOUT) as r:
        return json.load(r)


def _sleep_backoff(attempt):
    time.sleep(min(1.0 + attempt * 0.5, 5.0))


# ---------------------------------------------------------------------------
# XML key helpers
# ---------------------------------------------------------------------------

_KEY_RE = re.compile(r'<(?:string|plurals|string-array)\s+[^>]*name="([^"]+)"')
_STRING_TAG_RE = re.compile(r"<string\s+[^>]*?/?>")


def keys_in_text(text):
    return set(_KEY_RE.findall(text))


def count_strings_text(text):
    return len(_KEY_RE.findall(text))


def source_keys(source_file):
    """(server_keys, all_keys): server_keys excludes translatable="false"."""
    with open(source_file, encoding="utf-8") as fh:
        text = fh.read()
    all_keys = keys_in_text(text)
    non_translatable = set()
    for tag in _STRING_TAG_RE.findall(text):
        if 'translatable="false"' in tag:
            m = re.search(r'name="([^"]+)"', tag)
            if m:
                non_translatable.add(m.group(1))
    return all_keys - non_translatable, all_keys


def git_show_head(path):
    r = subprocess.run(["git", "show", f"HEAD:{path}"], capture_output=True, text=True)
    return r.stdout if r.returncode == 0 else None


def is_tracked(path):
    return subprocess.run(
        ["git", "ls-files", "--error-unmatch", path], capture_output=True
    ).returncode == 0


# ---------------------------------------------------------------------------
# push-sources
# ---------------------------------------------------------------------------


def _res_id(project_id, branch, slug):
    return f"{project_id}:r:{branch}--{slug}"


def push_one_source(res_id, source_file, tok):
    with open(source_file, "rb") as fh:
        content = base64.b64encode(fh.read()).decode()
    body = {"data": {
        "type": "resource_strings_async_uploads",
        "attributes": {"content": content, "content_encoding": "base64"},
        "relationships": {"resource": {"data": {"type": "resources", "id": res_id}}},
    }}
    job = api_post("/resource_strings_async_uploads", body, tok)
    job_url = f"{API}/resource_strings_async_uploads/{job['data']['id']}"
    for attempt in range(120):
        d = api_get(job_url, tok)
        attrs = d["data"]["attributes"]
        st = attrs.get("status")
        if st == "succeeded":
            return attrs.get("details", {}), attrs.get("errors", [])
        if st == "failed":
            return None, attrs.get("errors", [{"detail": "upload failed"}])
        _sleep_backoff(attempt)
    return None, [{"detail": "timed out waiting for upload job"}]


def cmd_push_sources(branch):
    tok = load_token()
    project_id, _, _, resources = parse_tx_config()
    print(f"Pushing sources to {branch}-- resources\n")
    print(f"{'resource':28}{'created':>9}{'updated':>9}{'deleted':>9}{'skipped':>9}")
    problems = 0
    for res in resources:
        src = res.get("source_file")
        if not src or not os.path.exists(src):
            continue
        res_id = _res_id(project_id, branch, res["slug"])
        try:
            details, errors = push_one_source(res_id, src, tok)
        except urllib.error.HTTPError as e:
            detail = e.read().decode(errors="replace")[:200]
            print(f"{res['slug']:28}  HTTP {e.code}: {detail}")
            problems += 1
            continue
        if details is None:
            print(f"{res['slug']:28}  FAILED: {errors}")
            problems += 1
            continue
        print(f"{res['slug']:28}"
              f"{details.get('strings_created', 0):>9}"
              f"{details.get('strings_updated', 0):>9}"
              f"{details.get('strings_deleted', 0):>9}"
              f"{details.get('strings_skipped', 0):>9}")
        if errors:
            problems += 1
            for err in errors[:10]:
                print(f"      error: {err}")
    print()
    if problems:
        print(f"FAIL: {problems} resource(s) had upload errors.")
        sys.exit(1)
    print("OK: all source files uploaded without errors.")


# ---------------------------------------------------------------------------
# verify-push
# ---------------------------------------------------------------------------


def server_source_keys(res_id, tok):
    url = f"{API}/resource_strings?filter[resource]={res_id}&limit=1000"
    out = set()
    while url:
        d = api_get(url, tok)
        for it in d["data"]:
            out.add(it["attributes"]["key"])
        url = d.get("links", {}).get("next")
    return out


def cmd_verify_push(branch):
    tok = load_token()
    project_id, _, _, resources = parse_tx_config()
    problems = 0
    print(f"Verifying source push to {branch}-- resources\n")
    print(f"{'resource':28}{'local':>7}{'server':>8}{'missing':>9}{'orphan':>8}")
    for res in resources:
        src = res.get("source_file")
        if not src or not os.path.exists(src):
            continue
        res_id = _res_id(project_id, branch, res["slug"])
        local_keys, _ = source_keys(src)
        try:
            server = server_source_keys(res_id, tok)
        except urllib.error.HTTPError as e:
            print(f"{res['slug']:28}  ERROR fetching server keys: {e}")
            problems += 1
            continue
        missing = local_keys - server
        orphan = server - local_keys
        flag = "  <-- MISSING!" if missing else ""
        print(f"{res['slug']:28}{len(local_keys):>7}{len(server):>8}"
              f"{len(missing):>9}{len(orphan):>8}{flag}")
        if missing:
            problems += 1
            for k in sorted(missing)[:20]:
                print(f"      missing: {k}")
    print()
    if problems:
        print(f"FAIL: {problems} resource(s) have keys missing on the server.")
        sys.exit(1)
    print("OK: all local source keys are present on the server (no silent failure).")


# ---------------------------------------------------------------------------
# lang-list
# ---------------------------------------------------------------------------


def translated_languages(branch, tok, project_id, source_lang):
    prefix = f":r:{branch}--"
    url = f"{API}/resource_language_stats?filter[project]={project_id}"
    langs = defaultdict(int)
    while url:
        d = api_get(url, tok)
        for it in d["data"]:
            rid = it["id"]
            if prefix not in rid:
                continue
            lang = rid.split(":")[-1]
            if lang == source_lang:
                continue
            langs[lang] += it["attributes"].get("translated_strings", 0)
        url = d.get("links", {}).get("next")
    return sorted(l for l, n in langs.items() if n > 0)


def cmd_lang_list(branch):
    tok = load_token()
    project_id, source_lang, _, _ = parse_tx_config()
    print(",".join(translated_languages(branch, tok, project_id, source_lang)))


# ---------------------------------------------------------------------------
# pull
# ---------------------------------------------------------------------------


def download_translation(res_id, tx_code, tok):
    """Return the translation file bytes (default mode)."""
    body = {"data": {
        "type": "resource_translations_async_downloads",
        "attributes": {"content_encoding": "text", "file_type": "default",
                       "mode": "default", "pseudo": False},
        "relationships": {
            "resource": {"data": {"type": "resources", "id": res_id}},
            "language": {"data": {"type": "languages", "id": f"l:{tx_code}"}},
        },
    }}
    job = api_post("/resource_translations_async_downloads", body, tok)
    job_url = f"{API}/resource_translations_async_downloads/{job['data']['id']}"
    for attempt in range(120):
        with urllib.request.urlopen(_req(job_url, tok, accept="*/*"), timeout=HTTP_TIMEOUT) as r:
            ctype = r.headers.get("Content-Type", "")
            data = r.read()
        if "json" in ctype:  # still processing / status object
            attrs = json.loads(data)["data"]["attributes"]
            if attrs.get("status") == "failed":
                raise RuntimeError(f"download failed: {attrs.get('errors')}")
            _sleep_backoff(attempt)
            continue
        return data  # redirect followed -> file bytes
    raise TimeoutError("timed out waiting for download job")


def cmd_pull(branch, langs_csv, workers):
    tok = load_token()
    project_id, source_lang, lang_map, resources = parse_tx_config()
    if langs_csv:
        langs = [x for x in langs_csv.split(",") if x]
    else:
        langs = translated_languages(branch, tok, project_id, source_lang)
    resources = [r for r in resources if r.get("file_filter") and r.get("source_file")]
    tasks = [(r, lang) for r in resources for lang in langs]
    print(f"pulling {len(langs)} languages x {len(resources)} resources "
          f"= {len(tasks)} files (workers={workers})")

    written = skipped_empty = failed = 0

    def work(res, tx_code):
        res_id = _res_id(project_id, branch, res["slug"])
        try:
            content = download_translation(res_id, tx_code, tok).decode("utf-8")
        except (urllib.error.HTTPError, RuntimeError, TimeoutError) as e:
            return ("fail", res["slug"], tx_code, str(e))
        folder = android_folder(tx_code, lang_map)
        path = res["file_filter"].replace("<lang>", folder)
        n = count_strings_text(content)
        if n == 0 and not os.path.exists(path):
            return ("skip", path, tx_code, None)  # don't create empty stub
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(content)
        return ("write", path, tx_code, None)

    with ThreadPoolExecutor(max_workers=workers) as pool:
        futs = [pool.submit(work, r, lang) for r, lang in tasks]
        for fut in as_completed(futs):
            kind, a, b, err = fut.result()
            if kind == "write":
                written += 1
            elif kind == "skip":
                skipped_empty += 1
            else:
                failed += 1
                print(f"  FAIL {a} [{b}]: {err}")
    print(f"done: {written} written, {skipped_empty} empty-skipped, {failed} failed")
    if failed:
        sys.exit(1)


# ---------------------------------------------------------------------------
# postpull
# ---------------------------------------------------------------------------


def resource_translation_files(res):
    ff = res.get("file_filter")
    if not ff or "<lang>" not in ff:
        return []
    return sorted(glob.glob(ff.replace("<lang>", "*")))


def _extract_block(text, key):
    for pat in (
        rf'[ \t]*<string\s+name="{re.escape(key)}"[^>]*>.*?</string>',
        rf'[ \t]*<string\s+name="{re.escape(key)}"[^>]*/>',
        rf'[ \t]*<plurals\s+name="{re.escape(key)}"[^>]*>.*?</plurals>',
        rf'[ \t]*<string-array\s+name="{re.escape(key)}"[^>]*>.*?</string-array>',
    ):
        m = re.search(pat, text, re.DOTALL)
        if m:
            return m.group(0)
    return None


def _translation_patterns(resources):
    pats = []
    for res in resources:
        ff = res.get("file_filter")
        if ff and "<lang>" in ff:
            # re.escape (Py 3.7+) leaves <lang> untouched, so replace the plain
            # token — NOT the pre-3.7 "\<lang\>" form, which never matches here.
            pats.append((re.compile(re.escape(ff).replace("<lang>", r"[^/]+") + "$"),
                         res["slug"]))
    return pats


def cmd_postpull():
    project_id, _, _, resources = parse_tx_config()
    src_all = {}
    kmp_dirs = set()
    for res in resources:
        src = res.get("source_file")
        if src and os.path.exists(src):
            _, all_keys = source_keys(src)
            src_all[res["slug"]] = all_keys
            if "composeResources" in src:
                kmp_dirs.add(os.path.dirname(os.path.dirname(src)))

    report = {"unescaped": 0, "unescaped_files": 0, "empty_removed": 0,
              "ws_reverted": 0, "restored": 0, "restored_files": 0}
    live_dropped = defaultdict(list)

    # 1-3. per translation file cleanup / restore
    for res in resources:
        slug = res["slug"]
        if slug not in src_all:
            continue
        skeys = src_all[slug]
        for f in resource_translation_files(res):
            cur = ""
            if os.path.exists(f):
                with open(f, encoding="utf-8") as fh:
                    cur = fh.read()
            cur_n = count_strings_text(cur)
            tracked = is_tracked(f)

            if not tracked:
                if cur_n == 0 and os.path.exists(f):  # new empty stub -> delete
                    os.remove(f)
                    try:
                        os.rmdir(os.path.dirname(f))
                    except OSError:
                        pass
                    report["empty_removed"] += 1
                continue

            head = git_show_head(f)
            if head is None:
                continue
            head_n = count_strings_text(head)

            if head_n == 0 and cur_n == 0:  # empty<->empty: drop whitespace churn
                if subprocess.run(["git", "diff", "--quiet", "HEAD", "--", f]).returncode:
                    subprocess.run(["git", "checkout", "HEAD", "--", f])
                    report["ws_reverted"] += 1
                continue

            dropped_live = sorted((keys_in_text(head) & skeys) - keys_in_text(cur))
            if dropped_live:
                blocks, block_keys = [], []
                for k in dropped_live:
                    b = _extract_block(head, k)
                    if b:
                        blocks.append(b.rstrip("\n"))
                        block_keys.append(k)
                # Only record as "restored" once the blocks are actually written
                # back — a file with no </resources> can't be patched.
                if blocks:
                    m = re.search(r"\n?[ \t]*</resources>\s*$", cur)
                    if m:
                        cur = cur[: m.start()] + "\n" + "\n".join(blocks) + "\n" + cur[m.start():]
                        with open(f, "w", encoding="utf-8") as fh:
                            fh.write(cur)
                        live_dropped[slug].extend(block_keys)
                        report["restored"] += len(blocks)
                        report["restored_files"] += 1

    # 4. unescape \? -> ? in KMP compose-resource dirs. Do this LAST so any block
    #    restored from HEAD above (which may still carry \?) is covered too.
    for d in sorted(kmp_dirs):
        for root, _, files in os.walk(d):
            for fn in files:
                if not fn.endswith(".xml"):
                    continue
                p = os.path.join(root, fn)
                with open(p, encoding="utf-8") as fh:
                    txt = fh.read()
                if "\\?" in txt:
                    report["unescaped"] += txt.count("\\?")
                    report["unescaped_files"] += 1
                    with open(p, "w", encoding="utf-8") as fh:
                        fh.write(txt.replace("\\?", "?"))

    # 5. validate
    pats = _translation_patterns(resources)
    tracked = subprocess.run(["git", "diff", "--name-only"], capture_output=True, text=True).stdout.split()
    new = subprocess.run(["git", "ls-files", "--others", "--exclude-standard"],
                         capture_output=True, text=True).stdout.split()
    problems = 0
    for f in tracked + new:
        slug = next((s for p, s in pats if p.search(f)), None)
        if not slug or not os.path.exists(f):
            continue
        try:
            xml.dom.minidom.parse(f)
        except Exception as e:  # noqa: BLE001
            print(f"MALFORMED XML: {f}: {e}")
            problems += 1
            continue
        if slug in src_all:
            with open(f, encoding="utf-8") as fh:
                extra = keys_in_text(fh.read()) - src_all[slug]
            if extra:
                print(f"KEYS NOT IN SOURCE: {f}: {sorted(extra)[:10]}")
                problems += 1

    print("postpull summary")
    print(f"  \\?->? unescaped        : {report['unescaped']} occ / {report['unescaped_files']} files")
    print(f"  empty stub files removed: {report['empty_removed']}")
    print(f"  whitespace-only reverts : {report['ws_reverted']}")
    print(f"  live translations kept  : {report['restored']} in {report['restored_files']} files")
    if live_dropped:
        print("  NOTE: the server was missing these in-source translations "
              "(restored from HEAD; consider a translation backfill push):")
        for slug, ks in sorted(live_dropped.items()):
            uniq = sorted(set(ks))
            print(f"    {slug}: {len(ks)} entries, keys={uniq[:8]}")
    if problems:
        print(f"\nVALIDATION FAILED: {problems} problem(s).")
        sys.exit(1)
    print("  validation              : OK (well-formed, keys subset of source)")


# ---------------------------------------------------------------------------
# push-translations (backfill: fill server gaps from the repo, never overwrite)
# ---------------------------------------------------------------------------


def _folder_from_path(file_filter, path):
    """Recover the values-<qualifier> folder token from a translation path."""
    pat = re.compile(re.escape(file_filter).replace("<lang>", r"([^/]+)") + "$")
    m = pat.search(path)
    return m.group(1) if m else None


def folder_to_tx(folder, inv_lang_map):
    """Android values-<folder> suffix -> Transifex language code (inverse of
    android_folder). Uses the config lang_map first, then the xx-rYY -> xx_YY
    convention, else the folder verbatim."""
    if folder in inv_lang_map:
        return inv_lang_map[folder]
    m = re.match(r"^([A-Za-z]{2,3})-r([A-Za-z0-9]+)$", folder)
    if m:
        return f"{m.group(1)}_{m.group(2)}"
    return folder


def _minimal_translation_xml(repo_text, keys):
    """Build a standalone Android <resources> file containing only `keys`,
    lifted verbatim from the repo translation file. Returns None if none of the
    keys yield a block."""
    blocks = []
    for k in keys:
        b = _extract_block(repo_text, k)
        if b:
            blocks.append(b.rstrip("\n"))
    if not blocks:
        return None
    return "<resources>\n" + "\n".join(blocks) + "\n</resources>\n"


def upload_translations(res_id, tx_code, content, tok):
    """Upload a translation file for one (resource, language) via the async API,
    waiting for the job. Returns (details, errors)."""
    b64 = base64.b64encode(content.encode("utf-8")).decode()
    body = {"data": {
        "type": "resource_translations_async_uploads",
        "attributes": {"content": b64, "content_encoding": "base64",
                       "file_type": "default"},
        "relationships": {
            "resource": {"data": {"type": "resources", "id": res_id}},
            "language": {"data": {"type": "languages", "id": f"l:{tx_code}"}},
        },
    }}
    job = api_post("/resource_translations_async_uploads", body, tok)
    job_url = f"{API}/resource_translations_async_uploads/{job['data']['id']}"
    for attempt in range(120):
        d = api_get(job_url, tok)
        attrs = d["data"]["attributes"]
        st = attrs.get("status")
        if st == "succeeded":
            return attrs.get("details", {}), attrs.get("errors", [])
        if st == "failed":
            return None, attrs.get("errors", [{"detail": "upload failed"}])
        _sleep_backoff(attempt)
    return None, [{"detail": "timed out waiting for upload job"}]


def cmd_push_translations(branch, apply, workers):
    tok = load_token()
    project_id, source_lang, lang_map, resources = parse_tx_config()
    inv_lang_map = {v: k for k, v in lang_map.items()}
    resources = [r for r in resources
                 if r.get("file_filter") and r.get("source_file")
                 and os.path.exists(r["source_file"])]
    skeys = {r["slug"]: source_keys(r["source_file"])[0] for r in resources}
    pairs = [(r, p) for r in resources for p in resource_translation_files(r)]
    print(f"scanning {len(pairs)} repo translation files for gaps on {branch}-- "
          f"(workers={workers})\n")

    plan, skipped = [], []

    def detect(res, path):
        folder = _folder_from_path(res["file_filter"], path)
        if not folder:
            return None
        tx_code = folder_to_tx(folder, inv_lang_map)
        if tx_code == source_lang:
            return None
        with open(path, encoding="utf-8") as fh:
            repo_text = fh.read()
        repo_source_keys = keys_in_text(repo_text) & skeys[res["slug"]]
        if not repo_source_keys:
            return None
        res_id = _res_id(project_id, branch, res["slug"])
        try:
            server_text = download_translation(res_id, tx_code, tok).decode("utf-8")
        except (urllib.error.HTTPError, RuntimeError, TimeoutError) as e:
            # Do NOT assume the server is empty — that would risk overwriting
            # real translations. Skip and report instead.
            return ("skip", res["slug"], tx_code, path, str(e))
        missing = sorted(repo_source_keys - keys_in_text(server_text))
        if not missing:
            return None
        return ("gap", res["slug"], tx_code, path, missing, repo_text)

    with ThreadPoolExecutor(max_workers=workers) as pool:
        futs = [pool.submit(detect, r, p) for r, p in pairs]
        res_by_slug = {r["slug"]: r for r in resources}
        for fut in as_completed(futs):
            got = fut.result()
            if not got:
                continue
            if got[0] == "skip":
                skipped.append(got[1:])
            else:
                _, slug, tx_code, path, missing, repo_text = got
                plan.append((res_by_slug[slug], tx_code, path, missing, repo_text))

    total = sum(len(m) for _, _, _, m, _ in plan)
    print(f"{'resource':28}{'lang':10}{'gap-keys':>9}")
    for res, tx_code, _, missing, _ in sorted(plan, key=lambda x: (x[0]['slug'], x[1])):
        print(f"{res['slug']:28}{tx_code:10}{len(missing):>9}")
    print(f"\n{total} translation(s) missing on the server across "
          f"{len(plan)} (resource, language) pair(s).")
    if skipped:
        print(f"{len(skipped)} pair(s) skipped (download error — not touched):")
        for slug, tx_code, _, err in skipped[:20]:
            print(f"    {slug} [{tx_code}]: {err}")

    if not apply:
        print("\nDRY RUN — nothing uploaded. Re-run with --apply to backfill.")
        return
    if not plan:
        print("\nNothing to upload.")
        return

    print("\nuploading gap translations ...")
    uploaded = failed = 0
    for res, tx_code, path, missing, repo_text in plan:
        content = _minimal_translation_xml(repo_text, missing)
        if not content:
            continue
        res_id = _res_id(project_id, branch, res["slug"])
        try:
            details, errors = upload_translations(res_id, tx_code, content, tok)
        except urllib.error.HTTPError as e:
            print(f"  FAIL {res['slug']} [{tx_code}]: HTTP {e.code}")
            failed += 1
            continue
        if details is None or errors:
            print(f"  FAIL {res['slug']} [{tx_code}]: {errors}")
            failed += 1
            continue
        created = details.get("translations_created", 0)
        print(f"  {res['slug']:28}{tx_code:10} created={created}")
        uploaded += 1
    print(f"\ndone: {uploaded} pair(s) backfilled, {failed} failed.")
    if failed:
        sys.exit(1)


# ---------------------------------------------------------------------------
# main
# ---------------------------------------------------------------------------


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    for name in ("push-sources", "verify-push", "lang-list"):
        sp = sub.add_parser(name)
        sp.add_argument("--branch", required=True)
    pp = sub.add_parser("pull")
    pp.add_argument("--branch", required=True)
    pp.add_argument("--langs", default="")
    pp.add_argument("--workers", type=int, default=8)
    sub.add_parser("postpull")
    ptr = sub.add_parser("push-translations")
    ptr.add_argument("--branch", required=True)
    ptr.add_argument("--apply", action="store_true",
                     help="actually upload; without it, dry-run only")
    ptr.add_argument("--workers", type=int, default=8)
    args = ap.parse_args()

    if args.cmd == "push-sources":
        cmd_push_sources(args.branch)
    elif args.cmd == "verify-push":
        cmd_verify_push(args.branch)
    elif args.cmd == "lang-list":
        cmd_lang_list(args.branch)
    elif args.cmd == "pull":
        cmd_pull(args.branch, args.langs, args.workers)
    elif args.cmd == "postpull":
        cmd_postpull()
    elif args.cmd == "push-translations":
        cmd_push_translations(args.branch, args.apply, args.workers)


if __name__ == "__main__":
    main()
