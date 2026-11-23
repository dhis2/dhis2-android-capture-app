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
./gradlew :login:testAndroidHostTest --tests "*.LoginViewModelTest.initial screen is set correctly when starting"
./gradlew assembleDhis2Debug                # debug APK
scripts/check_conventions.py                # the agreements below, on the lines your branch adds
scripts/check_harness.py                    # AGENTS.md / skills still match the repo (paths, modules)
git config core.hooksPath scripts/git-hooks # once per clone: ktlint + conventions before each commit
```

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

These hold for **all new code** in every module; existing code is changed only when the task is
about it. The `kmp-architecture` skill has the reasons, the API lists and the examples, and
`scripts/check_conventions.py` enforces most of them on the lines a branch adds. A line that must
break a rule on purpose ends with `// conventions:allow <reason>`.

1. **KMP first, iOS-ready:** business logic in `commonMain`, with no `java.*`, `javax.*`,
   `android.*` or `String.format`. When you touch a KMP module without iOS, propose adding it.
2. **SDK access through suspend or Flow APIs** (`suspendGet`, `flowDownload`…); no `rx*` or
   RxJava, and `blocking*` only where the SDK has no suspend version.
3. **Repositories own the dispatcher:** `withContext(dispatcher.io)` / `flowOn(dispatcher.io)`
   with the injected `commonskmm` `Dispatcher`; never `Dispatchers.*` or the legacy dispatchers.
4. **SDK errors become `DomainError` at the repository boundary** through
   `withDomainErrors { }` / `withDomainErrorsAsResult { }`, never an inline `catch (e: D2Error)`.
5. **Use cases never throw:** implement `UseCase<R, T>`, are named as an action (`SavePin`),
   rethrow `CancellationException` and then catch `Exception`; no `runCatching`, no
   `catch (Throwable)`.
6. **ViewModels** expose `StateFlow<UiState>` and launch work with `launchUseCase { }`, never
   `viewModelScope.launch`.
7. **Platform code:** repositories and stateful platform services are an interface in
   `commonMain` plus one Koin binding per platform. `expect`/`actual` only for small functions,
   Compose helpers and `expect val` Koin modules; no new `expect class`.
8. **Koin for DI in new code** (not Dagger), DHIS2 design system components inside
   `DHIS2Theme { }`, **offline-first** through the SDK; domain models in `domain/model/`, UI
   models in `ui/state/`.
9. **Tests:** mockito-kotlin and JUnit4 / `kotlin.test`; mock `D2` with `RETURNS_DEEP_STUBS`;
   no `Thread.sleep()`; in modules with iOS, `commonTest` uses fakes and Mockito goes in
   `androidHostTest`.

When you change AGENTS.md or a skill, keep `scripts/check_harness.py` green; when you add an
agreement, add its check. Task skills beyond the table above (Sentry triage and fixes, team
metrics, UI test planning) live in `.claude/skills/<name>/SKILL.md`, each described at its top.

---

## Code style, commits and PRs

- ktlint with `ktlint_official` (`.editorconfig`); run `./gradlew ktlintFormat`. Prefer `data class`
  for models, `sealed` types for UI state, expression bodies for single expressions, and KDoc on
  public APIs.
- Commits and PR titles: Conventional Commits with the ticket key first,
  `fix: [ANDROAPP-1234] short description`. No AI or tool attribution in commits or PRs.
- PRs are limited to 400 changed lines (`scripts/check_pr_size.sh`); `/open-pr` splits larger
  work into stacked PRs.
- Dependency changes: a bot commits regenerated `gradle/verification-metadata.xml` to your branch
  (`verify-dependency-metadata.yml`); pull before pushing again.

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
