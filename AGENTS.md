# DHIS2 Android Capture App - Agent Guidelines

A **Kotlin Multiplatform (KMP)** Android project migrating to Compose Multiplatform, built as
MVVM + Repository + Use Case. KMP modules target **Android and Desktop**; iOS is being added
module by module (see the module table). Legacy AGP modules are not migrated as drive-by changes.

This file is loaded in every session, so it holds only what must always hold. The how-to lives
in skills. **Read the matching skill before starting**; if your agent does not load
`.claude/skills/` itself, open the file:

| Before you… | Read |
|---|---|
| write or review production code | `.claude/skills/kmp-architecture/SKILL.md` |
| write, fix or review a test | `.claude/skills/android-testing/SKILL.md` |
| add the iOS target to a module, or fix an iOS compile error | `.claude/skills/kmp-ios/SKILL.md` |
| open a PR or split a branch into stacked PRs | `.claude/skills/open-pr/SKILL.md` |

---

## Commands

```bash
./gradlew ktlintFormat ktlintCheck          # ktlint_official style; must pass before committing
./run_tests.sh                              # conventions + harness checks, lint, all unit tests, desktop
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest   # all unit tests
./gradlew desktopTest                       # commonTest of every KMP module on the desktop JVM
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest"
./gradlew assembleDhis2Debug                # debug APK
scripts/check_conventions.py                # the agreements below, on the lines your branch adds*
scripts/check_harness.py                    # AGENTS.md / skills still match the repo (paths, modules)
git config core.hooksPath scripts/git-hooks # once per clone: ktlint + conventions before each commit
```

\* Measured from the branch your work started from (`develop`, `main` or `release/*`, detected
from the merge bases); pass `--base origin/<branch>` when the detection is wrong.

Test task by module type: `app` → `testDhis2DebugUnitTest` (flavoured); other AGP modules →
`testDebugUnitTest`; KMP modules → `testAndroidHostTest` (runs both `commonTest` and
`androidHostTest`) and `desktopTest` (runs `commonTest` on the desktop JVM; CI runs it, so a
common test must pass there too). `testAndroidDebugUnitTest` does not exist in this repo.

**Before you finish a change**, run `./run_tests.sh` (or, for a quick pass, the two `scripts/`
checks, `ktlintCheck` and the test tasks of the modules you touched) and fix what it reports.
CI does not run the convention or harness checks; `/open-pr` runs them again before a PR opens.

**Confirm a test actually ran**: check `build/test-results/<task>/TEST-<FQCN>.xml` for
`tests="N"`. A class that was never collected produces no file and the build still goes green.

---

## Modules

| Module | Type | Desktop | iOS | Notes |
|---|---|---|---|---|
| `app` | Android app | – | – | Legacy; flavours `dhis2`, `dhis2PlayServices`, `dhis2Training`; Dagger + Koin |
| `commonskmm` | KMP | ✓ | ✓ | Shared base: `UseCase`, `Dispatcher`, `DomainError`, `launchUseCase`, DI helpers |
| `home` | KMP | ✓ | ✓ | Home screen domain; its repository implementation still lives in `app` |
| `login` | KMP | ✓ | – | Uses multiplatform Compose Navigation |
| `sync` | KMP | ✓ | – | Easiest module to add iOS to |
| `aggregates` | KMP | ✓ | – | Data set entry |
| `tracker` | KMP | ✓ | – | Tracker search, relationships |
| `form`, `commons`, `compose-table`, `stock-usecase`, `dhis_android_analytics`, `dhis2_android_maps`, `dhis2-mobile-program-rules` | AGP library | – | – | Legacy (`commons` re-exports `commonskmm`) |

KMP source sets: `commonMain`, `commonTest`, `androidMain`, `androidHostTest` (depends on
`commonTest`), `desktopMain`, `composeResources`. Modules with iOS add `jvmCommonMain` (code
shared by Android and Desktop that uses JVM APIs) and `iosMain`. No module has a `desktopTest`
source set.

---

## Architectural agreements

These hold for **all new code** in every module. `scripts/check_conventions.py` enforces most of
them on the lines a branch adds. Patterns, examples and exceptions: `kmp-architecture` skill.

A line that must break a rule on purpose (an SDK call with no suspend version) ends with
`// conventions:allow <reason>`. When you change AGENTS.md or a skill, keep
`scripts/check_harness.py` green, and when you add an agreement, add its check.

1. **KMP first, iOS-ready.** Business logic goes in `commonMain`, written so it compiles for iOS:
   no `java.*`, `javax.*` or `android.*` imports and no `String.format` there (use
   kotlinx-datetime and Compose `stringResource(res, arg)`). When you touch a KMP module that
   has no iOS target, propose adding it (`kmp-ios` skill).
2. **SDK access uses suspend or Flow APIs**: `suspendGet`, `suspendGetUids`, `suspendCount`,
   `suspendExists`, `suspendAdd`, `suspendSet`, `suspendDelete`, `flowDownload`, `flowUpload`
   and the `suspend*` service methods. Not `blocking*`, `rx*`, deprecated Rx `get()`, or RxJava.
   A blocking call is allowed only where the SDK has no suspend version (list in the skill).
3. **Repositories own the dispatcher.** Every repository function is main-safe: its body runs in
   `withContext(dispatcher.io)`, and a returned Flow ends with `flowOn(dispatcher.io)`. Use cases
   and ViewModels never switch dispatchers for I/O; `dispatcher.default` only for CPU-heavy work
   they do themselves. Always inject the `commonskmm` `org.dhis2.mobile.commons.coroutine.Dispatcher`.
   Never `Dispatchers.*`, the legacy `commons` `DispatcherProvider` or the `aggregates` `Dispatcher`.
4. **SDK errors become `DomainError` at the repository boundary, in every module**, through
   `domainErrorMapper.withDomainErrors { }` / `withDomainErrorsAsResult { }`. Never an inline
   `catch (e: D2Error)`: the SDK rewraps `D2Error` in a `RuntimeException`, which the wrappers
   unwrap and an inline catch misses. Nothing above the data layer sees an SDK type.
5. **Use cases never throw.** Implement `UseCase<R, T>` (`commonskmm/.../domain/UseCase.kt`),
   name them as an action without the `UseCase` suffix (`SavePin`), rethrow
   `CancellationException`, then catch `Exception`. Never `runCatching` (it swallows
   cancellation) and never `catch (Throwable)`.
6. **ViewModels** launch work with `launchUseCase { }`, never `viewModelScope.launch` (it feeds
   the Espresso idling resource), and expose `StateFlow<UiState>`. Leave existing
   `viewModelScope.launch` sites as they are unless the task is to migrate them.
7. **Platform code: interfaces first.** Repositories and stateful platform services are an
   interface in `commonMain` plus one Koin binding per platform (a stub where a platform lacks
   it). `expect`/`actual` only for small dependency-free functions, Compose helpers and the
   `expect val` Koin modules that hold those per-platform bindings; no new `expect class`.
8. **Other rules:** Koin for DI in new code; DHIS2 design system components
   (`org.hisp.dhis.mobile.ui.designsystem.*`) inside `DHIS2Theme { }` over Material; no RxJava in
   new code; no direct network or database access (use the DHIS2 SDK); offline-first; domain
   models in `domain/model/`, UI models in `ui/state/`, no new top-level `model/` packages.
9. **Tests:** mockito-kotlin only, never MockK; JUnit4 / `kotlin.test` only (Jupiter is excluded
   and fails the build); mock `D2` with `RETURNS_DEEP_STUBS`; never `Thread.sleep()`. In modules
   with an iOS target, `commonTest` uses fakes and `kotlin.test` only (no Mockito, JUnit or
   `java.*`); Mockito goes in `androidHostTest`.

---

## Code style

ktlint with `ktlint_official` (`.editorconfig`): no wildcard imports, no unused imports,
ordered imports, trailing commas on call and declaration sites, PascalCase allowed for
composables. Prefer `data class` for models, `sealed` types for UI state, expression
bodies for single expressions, KDoc on public APIs. Backtick test names cannot contain `,` in
modules with iOS (Kotlin/Native rejects them).

---

## CI and merging

- **On every PR** (`.github/workflows/ci.yml`): PR size (max 400 changed lines, counted by
  `scripts/check_pr_size.sh`, `verification-metadata.xml` excluded), `ktlintCheck`, unit tests +
  `desktopTest` + JaCoCo, test APKs, BrowserStack UI tests in **portrait and landscape**,
  SonarQube.
- **Title markers:** `[skip size]` (justified large PR, e.g. a test DB), `[skip unitTest]`,
  `[skip ci]`. Put them in the title or body.
- **PRs to `develop`** also need every ANDROAPP key in the title to be in "Ready to Merge"
  (`jira-ready.yml`). Merge automation queues a PR once it has 2 approvals and green checks.
- **Titles and commits:** Conventional Commits with the ticket key first in the description:
  `fix: [ANDROAPP-1234] short description`. No AI or tool attribution in commits or PRs.
  PR body from `.github/pull_request_template.md`.
- **Dependency changes:** a bot commits regenerated `gradle/verification-metadata.xml` to your
  branch (`verify-dependency-metadata.yml`); pull before pushing again.

---

## Build environment

- `ANDROID_HOME` must be exported in a git worktree (worktrees have no `local.properties`).
- `mavenLocal()` comes first. The design system is a `-SNAPSHOT` version, so a locally published
  one silently replaces the remote one. The SDK is pinned to a timestamped snapshot in
  `gradle/libs.versions.toml`: to test a local SDK build, publish it and point `dhis2sdk` at its
  `-SNAPSHOT` version.
- `app` still uses **Dagger** (KSP and kapt) next to Koin; Koin starts in
  `app/src/main/java/org/dhis2/di/KoinInitialization.kt`. Flavour-specific classes exist in
  `app/src/dhis2*` source sets (e.g. `DownloadNewVersion`): change every copy.
- Strings: edit only the default `values/strings.xml` / `composeResources/values/strings.xml`;
  translations come from Transifex.
- A release build needs CI signing secrets. To check R8 locally run
  `./gradlew :app:minifyDhis2ReleaseWithR8`.
- After a `compileSdk` bump also run `./gradlew assembleDhis2DebugAndroidTest`: new `@Nullable`
  platform APIs break instrumented test sources.
- Tasks for the iOS targets need `--dependency-verification lenient` locally (the metadata has no
  iOS artifacts yet).

---

## Skills

- **`/open-pr [--base <branch>]`**: opens the PR(s) for the branch, splitting into stacked PRs
  over 400 lines, with a code review whose outcome goes into the PR description.
- **`/sentry-triage`**, **`/sentry-fix <id>`**: rank production crashes and fix one in the repo
  that owns it (this app, `../dhis2-android-sdk` or `../dhis2-mobile-ui`).
- **`/team-metrics`**: the monthly team metrics report on Confluence.
- **`/test-flow-architect`** (with `zephyr-test-fetcher`, `confluence-workflow-reader`,
  `test-flow-planner`): plan and implement UI test automation from Zephyr test cases.
- **`kmp-architecture`**, **`kmp-ios`**, **`android-testing`**: see the table at the top.

Each skill documents its own requirements (plugins, tokens, sibling clones) in its `SKILL.md`.
