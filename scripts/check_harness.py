#!/usr/bin/env python3
"""Checks that the agent harness (AGENTS.md, skills, agents) still matches the repository.

Agent instructions rot silently: a renamed file or a new module leaves the docs pointing at
nothing, and agents follow them anyway. This fails when:
  - a repository or skill-relative path in a harness document (in backticks, or in a shell code
    block) is not a tracked or new file or folder, matched case-sensitively;
  - a module in settings.gradle.kts is not mentioned in AGENTS.md;
  - an agent or skill names an MCP tool by a per-install connector id (mcp__<uuid>__…);
  - a skill's SKILL.md lacks a `name` matching its directory, or a `description`.

Usage: scripts/check_harness.py
"""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(
    subprocess.run(
        ["git", "rev-parse", "--show-toplevel"], capture_output=True, text=True, check=True
    ).stdout.strip()
)

BACKTICK = re.compile(r"`([^`]+)`")
UUID_TOOL = re.compile(r"mcp__[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}__")
PLACEHOLDER = re.compile(r"[<>*{}$…()|\\\[\]=]|\.\.\.|^https?:|^-|^~")
SHELL_FENCE = re.compile(r"^\s*```(bash|sh|shell|zsh)?\s*$")


def harness_docs() -> list[Path]:
    docs = [ROOT / "AGENTS.md", ROOT / "CLAUDE.md"]
    docs += sorted((ROOT / ".claude" / "skills").glob("*/SKILL.md"))
    docs += sorted((ROOT / ".claude" / "skills").glob("*/references/*.md"))
    docs += sorted((ROOT / ".claude" / "agents").glob("*.md"))
    return [d for d in docs if d.is_file()]


def known_paths() -> set[str]:
    """Tracked and new (not ignored) files plus their folders, case-sensitive as on Linux."""
    files = subprocess.run(
        ["git", "-c", "core.quotepath=off", "ls-files", "--cached", "--others", "--exclude-standard"],
        cwd=ROOT, capture_output=True, text=True, check=True,
    ).stdout.splitlines()
    known = set()
    for file in files:
        if (ROOT / file).exists():  # skip deletions not yet staged
            parts = file.split("/")
            known.update("/".join(parts[:i]) for i in range(1, len(parts) + 1))
    return known


def resolve(token: str, doc_dir: str, known: set[str]) -> list[str]:
    """The repo-relative paths the token may mean (skill-relative or repository-relative)."""
    token = token.rstrip(".,:;").split(":", 1)[0]  # allow file.kt:42
    token = token[2:] if token.startswith("./") else token
    if "/" not in token or PLACEHOLDER.search(token):
        return []
    token = token.rstrip("/")
    first = token.split("/", 1)[0]
    candidates = []
    if doc_dir and f"{doc_dir}/{first}" in known:  # relative to the skill (references/, scripts/)
        candidates.append(f"{doc_dir}/{token}")
    if first in known:
        candidates.append(token)
    return candidates


def candidate_tokens(line: str, in_shell_block: bool) -> list[str]:
    spans = [line] if in_shell_block else BACKTICK.findall(line)
    return [word for span in spans for word in span.split()]


def check_paths(errors: list[str]) -> None:
    known = known_paths()
    for doc in harness_docs():
        doc_dir = doc.parent.relative_to(ROOT).as_posix()
        doc_dir = "" if doc_dir == "." else doc_dir
        fence: str | None = None
        for line_no, line in enumerate(doc.read_text(encoding="utf-8").splitlines(), start=1):
            if line.lstrip().startswith("```"):
                if fence is None:
                    match = SHELL_FENCE.match(line)
                    fence = "shell" if match and match.group(1) else "other"
                else:
                    fence = None
                continue
            if fence == "other":  # code examples name illustrative paths
                continue
            for token in candidate_tokens(line, fence == "shell"):
                candidates = resolve(token, doc_dir, known)
                if candidates and not any(path in known for path in candidates):
                    errors.append(f"{doc.relative_to(ROOT)}:{line_no}: path `{token}` does not exist")


def check_modules(errors: list[str]) -> None:
    settings = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
    settings = re.sub(r"//.*", "", re.sub(r"/\*.*?\*/", "", settings, flags=re.S))
    includes = " ".join(re.findall(r"include\(([^)]*)\)", settings))
    modules = {m.lstrip(":").replace(":", "/") for m in re.findall(r'"(:[\w:-]+)"', includes)}
    agents = (ROOT / "AGENTS.md").read_text(encoding="utf-8")
    for module in sorted(modules):
        if f"`{module}`" not in agents:
            errors.append(f"AGENTS.md: module `{module}` from settings.gradle.kts is not listed")


def check_tool_names(errors: list[str]) -> None:
    for doc in harness_docs():
        for line_no, line in enumerate(doc.read_text(encoding="utf-8").splitlines(), start=1):
            if UUID_TOOL.search(line):
                errors.append(
                    f"{doc.relative_to(ROOT)}:{line_no}: MCP tool named by a per-install connector id; "
                    "it resolves to nothing on other machines"
                )


def check_skill_frontmatter(errors: list[str]) -> None:
    for skill in sorted((ROOT / ".claude" / "skills").glob("*/SKILL.md")):
        text = skill.read_text(encoding="utf-8")
        match = re.match(r"^---\n(.*?)\n---\n", text, re.S)
        where = skill.relative_to(ROOT)
        if not match:
            errors.append(f"{where}: missing YAML frontmatter")
            continue
        front = match.group(1)
        name = re.search(r"^name:[ \t]*[\"']?([\w-]+)", front, re.M)
        if not name or name.group(1) != skill.parent.name:
            errors.append(f"{where}: frontmatter `name` must equal the directory name `{skill.parent.name}`")
        if not re.search(r"^description:[ \t]*\S", front, re.M):
            errors.append(f"{where}: frontmatter needs a `description` (it decides when the skill loads)")


def main() -> int:
    errors: list[str] = []
    check_paths(errors)
    check_modules(errors)
    check_tool_names(errors)
    check_skill_frontmatter(errors)
    if errors:
        print(f"check_harness: {len(errors)} problem(s):", file=sys.stderr)
        print("\n".join(errors), file=sys.stderr)
        return 1
    print("check_harness: harness documents match the repository.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
