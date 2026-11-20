#!/usr/bin/env python3
"""Checks that the agent harness (AGENTS.md, skills, agents) still matches the repository.

Agent instructions rot silently: a renamed file or a new module leaves the docs pointing at
nothing, and agents follow them anyway. This fails when:
  - a backticked repository path in a harness document does not exist;
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

BACKTICK = re.compile(r"`([^`\s]+)`")
UUID_TOOL = re.compile(r"mcp__[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}__")
PLACEHOLDER = re.compile(r"[<>*{}$…()|\\]|\.\.\.|^https?:|^-")
FILE_LIKE = re.compile(r"\.(kt|kts|md|sh|py|yml|yaml|xml|json|toml|properties|java)$")


def harness_docs() -> list[Path]:
    docs = [ROOT / "AGENTS.md", ROOT / "CLAUDE.md"]
    docs += sorted((ROOT / ".claude" / "skills").glob("*/SKILL.md"))
    docs += sorted((ROOT / ".claude" / "skills").glob("*/references/*.md"))
    docs += sorted((ROOT / ".claude" / "agents").glob("*.md"))
    return [d for d in docs if d.is_file()]


def top_level_entries() -> set[str]:
    return {p.name for p in ROOT.iterdir()}


def looks_like_repo_path(token: str, top_level: set[str]) -> bool:
    if PLACEHOLDER.search(token) or "/" not in token and not FILE_LIKE.search(token):
        return False
    first = token.split("/", 1)[0]
    return first in top_level and (FILE_LIKE.search(token) is not None or token.endswith("/"))


def check_paths(errors: list[str]) -> None:
    top_level = top_level_entries()
    for doc in harness_docs():
        in_code_block = False
        for line_no, line in enumerate(doc.read_text(encoding="utf-8").splitlines(), start=1):
            if line.lstrip().startswith("```"):
                in_code_block = not in_code_block
                continue
            if in_code_block:
                continue
            for token in BACKTICK.findall(line):
                token = token.rstrip(".,:;)")
                if not looks_like_repo_path(token, top_level):
                    continue
                path = token.split(":", 1)[0]  # allow file.kt:42
                if not ((ROOT / path).exists() or (doc.parent / path).exists()):
                    errors.append(f"{doc.relative_to(ROOT)}:{line_no}: path `{token}` does not exist")


def check_modules(errors: list[str]) -> None:
    settings = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
    modules = re.findall(r'include\(\s*"[:]([^"]+)"', settings)
    modules += re.findall(r'"[:]([\w-]+)"', " ".join(re.findall(r"include\(([^)]*)\)", settings)))
    agents = (ROOT / "AGENTS.md").read_text(encoding="utf-8")
    for module in sorted(set(modules)):
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
        name = re.search(r"^name:\s*(\S+)", front, re.M)
        if not name or name.group(1) != skill.parent.name:
            errors.append(f"{where}: frontmatter `name` must equal the directory name `{skill.parent.name}`")
        if not re.search(r"^description:\s*\S", front, re.M):
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
