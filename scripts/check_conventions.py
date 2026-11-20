#!/usr/bin/env python3
"""Checks the architectural agreements in AGENTS.md on the Kotlin lines a change adds.

Only added lines are checked, so legacy code never fails the check: touching a legacy file does
not make you fix it, but nothing new may break the agreements.

Usage:
  scripts/check_conventions.py                 # lines added since the merge base with the base branch
  scripts/check_conventions.py --base origin/main
  scripts/check_conventions.py --staged        # lines added in the index vs HEAD (pre-commit)
  scripts/check_conventions.py --files a.kt b.kt   # lines added vs HEAD (whole file if untracked)
  scripts/check_conventions.py --full a.kt     # every line of the given files

Base branch: --base, else $CONVENTIONS_BASE, else the branch the work started from: whichever of
origin/develop, origin/main and origin/release/* has the fewest commits between its merge base and
HEAD (develop on a tie).

Moved and renamed files count only their changed lines (git rename detection).

A line that must break a rule on purpose (for example an SDK call with no suspend version) can be
allowed with a trailing comment that gives the reason:  // conventions:allow <reason>
"""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Iterable

ROOT = Path(
    subprocess.run(
        ["git", "rev-parse", "--show-toplevel"], capture_output=True, text=True, check=True
    ).stdout.strip()
)
ALLOW_MARKER = "conventions:allow"
TEST_SOURCE_SETS = {
    "test",
    "testDhis2",
    "androidTest",
    "commonTest",
    "androidHostTest",
    "jvmCommonTest",
    "desktopTest",
    "iosTest",
    "androidUnitTest",
}


def _module_build_file(module: str) -> str:
    path = ROOT / module / "build.gradle.kts"
    return path.read_text(encoding="utf-8") if path.is_file() else ""


KMP_MODULES = {
    m for m in (p.parent.name for p in ROOT.glob("*/build.gradle.kts"))
    if "multiplatform" in _module_build_file(m)
}
IOS_MODULES = {m for m in KMP_MODULES if "iosArm64()" in _module_build_file(m)}


@dataclass(frozen=True)
class Source:
    path: str  # repo-relative, forward slashes

    @property
    def module(self) -> str:
        return self.path.split("/", 1)[0]

    @property
    def source_set(self) -> str:
        parts = self.path.split("/")
        return parts[parts.index("src") + 1] if "src" in parts[:-1] else ""

    @property
    def is_test(self) -> bool:
        return self.source_set in TEST_SOURCE_SETS

    @property
    def is_main(self) -> bool:
        return not self.is_test and "/src/" in self.path

    @property
    def is_upper_layer(self) -> bool:
        """ViewModels, UI and domain (use cases): the layers that must not switch to io."""
        name = self.path.rsplit("/", 1)[-1]
        return "ViewModel" in name or any(
            f"/{part}/" in self.path for part in ("ui", "presentation", "domain")
        )


@dataclass(frozen=True)
class Rule:
    id: str
    pattern: re.Pattern[str]
    message: str
    applies: Callable[[Source], bool]


def rx(pattern: str) -> re.Pattern[str]:
    return re.compile(pattern)


SDK_OPS = r"(Get|GetUids|Count|IsEmpty|Exists|Add|Set|Delete|DeleteIfExist|Download|Upload)"

RULES: list[Rule] = [
    Rule(
        "sdk-blocking",
        rx(r"\.blocking[A-Z]\w*\("),
        "use the SDK suspend/flow API (suspendGet, suspendSet, flowDownload, flowUpload…); "
        "blocking only where no suspend version exists (kmp-architecture skill), marked with "
        "// conventions:allow <reason>",
        lambda s: s.is_main,
    ),
    Rule(
        "sdk-rx",
        rx(rf"\.rx{SDK_OPS}\(|^\s*import\s+io\.reactivex"),
        "no RxJava in new code: use the SDK suspend/flow API and coroutines",
        lambda s: s.is_main,
    ),
    Rule(
        "d2error-catch",
        rx(r"catch\s*\(\s*\w+\s*:\s*D2Error\s*\)"),
        "map SDK errors with domainErrorMapper.withDomainErrors { } / withDomainErrorsAsResult { }; "
        "an inline catch misses D2Errors wrapped in RuntimeException",
        lambda s: s.is_main,
    ),
    Rule(
        "run-catching",
        rx(r"\brunCatching\b"),
        "runCatching swallows CancellationException: rethrow it, then catch Exception "
        "(tests: assertFailsWith)",
        lambda s: True,
    ),
    Rule(
        "catch-throwable",
        rx(r"catch\s*\(\s*\w+\s*:\s*Throwable\s*\)"),
        "catch Exception, not Throwable (it turns JVM Errors into results)",
        lambda s: s.is_main,
    ),
    Rule(
        "dispatchers-direct",
        rx(r"\bDispatchers\.(IO|Default|Main|Unconfined)\b"),
        "inject org.dhis2.mobile.commons.coroutine.Dispatcher instead of using Dispatchers.*",
        lambda s: s.is_main and "/di/" not in s.path and not s.path.endswith("/Dispatcher.kt"),
    ),
    Rule(
        "dispatcher-layer",
        rx(r"\b(withContext|flowOn)\(\s*[\w.]*\.io\b"),
        "repositories own the I/O dispatcher; use cases and ViewModels do not switch to io",
        lambda s: s.is_main and s.is_upper_layer,
    ),
    Rule(
        "launch-usecase-io",
        rx(r"\blaunchUseCase\(\s*[\w.]*\.io\b"),
        "launchUseCase runs on Main; repositories switch to io themselves",
        lambda s: s.is_main,
    ),
    Rule(
        "legacy-dispatcher",
        rx(
            r"^\s*import\s+(org\.dhis2\.commons\.viewmodel\.DispatcherProvider"
            r"|org\.dhis2\.mobile\.aggregates\.ui\.dispatcher\.Dispatcher)\b"
        ),
        "use org.dhis2.mobile.commons.coroutine.Dispatcher in new code",
        lambda s: s.is_main,
    ),
    Rule(
        "viewmodel-scope-launch",
        rx(r"\bviewModelScope\.launch\b"),
        "use launchUseCase { } so the Espresso idling resource tracks the coroutine",
        lambda s: s.is_main and not s.path.endswith("/ViewModelExtensions.kt"),
    ),
    Rule(
        "mockk",
        rx(r"\bio\.mockk\b|\bmockk\s*[<(]|\bcoEvery\s*\{"),
        "use mockito-kotlin, never MockK",
        lambda s: True,
    ),
    Rule("thread-sleep", rx(r"\bThread\.sleep\("), "no hard-coded delays", lambda s: True),
    Rule(
        "junit-jupiter",
        rx(r"org\.junit\.jupiter"),
        "JUnit Jupiter is excluded from the build: use JUnit4 / kotlin.test",
        lambda s: True,
    ),
    Rule(
        "usecase-suffix",
        rx(r"\b(class|object|interface)\s+\w+UseCase\b"),
        "name use cases as an action without the UseCase suffix (SavePin)",
        lambda s: s.is_main,
    ),
    Rule(
        "commonmain-jvm-import",
        rx(r"^\s*import\s+(java|javax|android)\."),
        "commonMain must compile for iOS: no java/javax/android imports (kmp-ios skill)",
        lambda s: s.source_set == "commonMain",
    ),
    Rule(
        "commonmain-format",
        rx(r"\bString\.format\(|\"\s*\.format\(|stringResource\([^()]*\)\.format\("),
        "String.format is JVM-only: use templates or stringResource(res, arg) (kmp-ios skill)",
        lambda s: s.source_set == "commonMain",
    ),
    Rule(
        "ios-commontest",
        rx(r"^\s*import\s+(org\.mockito|org\.junit|java\.)"),
        "commonTest of an iOS module runs on iOS: use fakes and kotlin.test",
        lambda s: s.source_set == "commonTest" and s.module in IOS_MODULES,
    ),
]


def git(*args: str) -> str:
    return subprocess.run(
        ["git", "-c", "core.quotepath=off", *args], cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


# Fixed diff format whatever the user's git config says (prefixes, external diff, renames).
DIFF = ("diff", "-U0", "--no-color", "--no-ext-diff", "-M", "--src-prefix=a/", "--dst-prefix=b/")


def diff_path(target: str) -> str | None:
    target = target.rstrip("\t")  # git appends a tab to paths that contain spaces
    if target.startswith('"') and target.endswith('"'):  # quoted: `"`, `\` or control characters
        target = target[1:-1].replace('\\"', '"').replace("\\\\", "\\")
    if target == "/dev/null":
        return None
    return target[2:] if target.startswith("b/") else target


def parse_added_lines(diff: str) -> dict[str, list[tuple[int, str]]]:
    added: dict[str, list[tuple[int, str]]] = {}
    path: str | None = None
    line_no = 0
    for raw in diff.splitlines():
        if raw.startswith("+++ "):
            path = diff_path(raw[4:])
            continue
        if raw.startswith("@@"):
            match = re.search(r"\+(\d+)", raw)
            line_no = int(match.group(1)) if match else 0
            continue
        if path is None or raw.startswith("---"):
            continue
        if raw.startswith("+"):
            added.setdefault(path, []).append((line_no, raw[1:]))
            line_no += 1
        elif not raw.startswith("-") and not raw.startswith("\\"):
            line_no += 1
    return added


def whole_file(path: str) -> list[tuple[int, str]]:
    text = (ROOT / path).read_text(encoding="utf-8", errors="replace")
    return list(enumerate(text.splitlines(), start=1))


def untracked_kotlin() -> list[str]:
    return [p for p in git("ls-files", "--others", "--exclude-standard").splitlines() if p.endswith(".kt")]


def resolve_base(cli_base: str | None) -> str:
    if cli_base or os.environ.get("CONVENTIONS_BASE"):
        return cli_base or os.environ["CONVENTIONS_BASE"]
    candidates = ["origin/develop", "origin/main"] + sorted(
        git("for-each-ref", "--format=%(refname:short)", "refs/remotes/origin/release/").split()
    )
    distances = []
    for order, ref in enumerate(candidates):
        try:
            merge_base = git("merge-base", ref, "HEAD").strip()
            distances.append((int(git("rev-list", "--count", f"{merge_base}..HEAD")), order, ref))
        except subprocess.CalledProcessError:
            continue
    if not distances:
        raise SystemExit("check_conventions: no origin/develop, origin/main or origin/release/*; pass --base")
    return min(distances)[2]


def lines_since_base(base: str) -> dict[str, list[tuple[int, str]]]:
    merge_base = git("merge-base", base, "HEAD").strip()
    # Against the working tree, so uncommitted edits are checked too.
    added = parse_added_lines(git(*DIFF, "--diff-filter=AMR", merge_base))
    for path in untracked_kotlin():
        added[path] = whole_file(path)
    return added


def staged_lines() -> dict[str, list[tuple[int, str]]]:
    return parse_added_lines(git(*DIFF, "--cached", "--diff-filter=AMR", "HEAD"))


def lines_of_files(files: Iterable[str], full: bool) -> dict[str, list[tuple[int, str]]]:
    tracked = set(git("ls-files").splitlines())
    wanted = []
    for file in files:
        absolute = Path(file).resolve()
        try:
            path = absolute.relative_to(ROOT.resolve()).as_posix()
        except ValueError:
            continue
        if path.endswith(".kt") and absolute.is_file():
            wanted.append(path)
    added: dict[str, list[tuple[int, str]]] = {}
    to_diff = []
    for path in wanted:
        if full or path not in tracked:
            added[path] = whole_file(path)
        else:
            to_diff.append(path)
    if to_diff:
        # Diff the whole tree, not just these paths, so a moved file pairs with its old path.
        changed = parse_added_lines(git(*DIFF, "--diff-filter=AMR", "HEAD"))
        added.update({path: changed[path] for path in to_diff if path in changed})
    return added


def check(added: dict[str, list[tuple[int, str]]]) -> list[str]:
    findings = []
    for path, lines in sorted(added.items()):
        if not path.endswith(".kt") or path.startswith(".claude/") or "/build/" in path:
            continue
        source = Source(path)
        rules = [r for r in RULES if r.applies(source)]
        for line_no, text in lines:
            if ALLOW_MARKER in text or text.lstrip().startswith(("//", "*", "/*")):
                continue
            for rule in rules:
                if rule.pattern.search(text):
                    findings.append(f"{path}:{line_no}: [{rule.id}] {rule.message}\n    {text.strip()}")
    return findings


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base", help="base ref to diff against (default: see above)")
    parser.add_argument("--staged", action="store_true", help="check lines added in the index vs HEAD")
    parser.add_argument("--files", nargs="+", help="check lines added to these files vs HEAD")
    parser.add_argument("--full", nargs="+", help="check every line of these files")
    args = parser.parse_args()

    try:
        if args.full:
            added = lines_of_files(args.full, full=True)
        elif args.staged:
            added = staged_lines()
        elif args.files:
            added = lines_of_files(args.files, full=False)
        else:
            added = lines_since_base(resolve_base(args.base))
    except subprocess.CalledProcessError as error:
        print(f"check_conventions: git failed: {error.stderr.strip()}", file=sys.stderr)
        return 2

    findings = check(added)
    if not findings:
        print("check_conventions: no violations in the added Kotlin lines.")
        return 0
    print(f"check_conventions: {len(findings)} violation(s) of the AGENTS.md agreements:\n", file=sys.stderr)
    print("\n".join(findings), file=sys.stderr)
    print(
        "\nFix them, or end the line with `// conventions:allow <reason>` when breaking the rule "
        "is intended (e.g. an SDK call with no suspend version).",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())
