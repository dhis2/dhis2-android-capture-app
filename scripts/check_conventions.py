#!/usr/bin/env python3
"""Checks the architectural agreements in AGENTS.md on the Kotlin lines a change adds.

Only added lines are checked, so legacy code never fails the check: touching a legacy file does
not make you fix it, but nothing new may break the agreements.

Usage:
  scripts/check_conventions.py                 # lines added since the merge base with the base branch
  scripts/check_conventions.py --base origin/main
  scripts/check_conventions.py --files a.kt b.kt   # lines added vs HEAD (whole file if untracked)
  scripts/check_conventions.py --full a.kt     # every line of the given files

Base branch: --base, else $CONVENTIONS_BASE, else origin/develop.

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
    def is_data_layer(self) -> bool:
        name = self.path.rsplit("/", 1)[-1]
        return (
            "/data/" in self.path
            or "/di/" in self.path
            or any(k in name for k in ("Repository", "DataSource", "Service", "Dispatcher"))
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
        rx(rf"\.blocking{SDK_OPS}\("),
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
        lambda s: s.is_main and not s.is_data_layer,
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
        lambda s: s.is_main
        and s.module in KMP_MODULES
        and not s.path.endswith("/ViewModelExtensions.kt"),
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
        ["git", *args], cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def parse_added_lines(diff: str) -> dict[str, list[tuple[int, str]]]:
    added: dict[str, list[tuple[int, str]]] = {}
    path: str | None = None
    line_no = 0
    for raw in diff.splitlines():
        if raw.startswith("+++ "):
            target = raw[4:]
            path = None if target == "/dev/null" else target[2:] if target.startswith("b/") else target
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
    return cli_base or os.environ.get("CONVENTIONS_BASE") or "origin/develop"


def lines_since_base(base: str) -> dict[str, list[tuple[int, str]]]:
    merge_base = git("merge-base", base, "HEAD").strip()
    # Against the working tree, so uncommitted edits are checked too.
    added = parse_added_lines(git("diff", "-U0", "--no-color", "--diff-filter=AMR", merge_base, "--", "*.kt"))
    for path in untracked_kotlin():
        added[path] = whole_file(path)
    return added


def lines_of_files(files: Iterable[str], full: bool) -> dict[str, list[tuple[int, str]]]:
    tracked = set(git("ls-files").splitlines())
    added: dict[str, list[tuple[int, str]]] = {}
    for file in files:
        absolute = Path(file).resolve()
        try:
            path = absolute.relative_to(ROOT).as_posix()
        except ValueError:
            continue
        if not path.endswith(".kt") or not absolute.is_file():
            continue
        if full or path not in tracked:
            added[path] = whole_file(path)
        else:
            added.update(parse_added_lines(git("diff", "-U0", "--no-color", "HEAD", "--", path)))
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
    parser.add_argument("--files", nargs="+", help="check lines added to these files vs HEAD")
    parser.add_argument("--full", nargs="+", help="check every line of these files")
    args = parser.parse_args()

    try:
        if args.full:
            added = lines_of_files(args.full, full=True)
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
