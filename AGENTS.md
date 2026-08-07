# DHIS2 Android Capture App - Agent Guidelines

This is a **Kotlin Multiplatform (KMP)** Android project migrating to Compose Multiplatform,
targeting Android, Desktop, and iOS. The app uses MVVM + Repository + Use Case architecture.

---

## Build & Lint Commands

```bash
# Full lint check (ktlint 1.7.1, ktlint_official style)
./gradlew ktlintCheck

# Auto-format all sources
./gradlew ktlintFormat

# Run all unit tests (debug + KMP host tests)
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest

# Shortcut: lint + all unit tests (mirrors CI)
./run_tests.sh

# Run a single test class (KMP commonTest)
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest"

# Run a single test method (KMP commonTest)
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest.initial screen is set correctly when starting"

# Build debug APK
./gradlew assembleDhis2Debug

# Build all modules
./gradlew assemble
```

**Gradle task naming by module type:**
- Legacy Android modules (`form`, `commons`, `tracker`, etc.): `testDebugUnitTest`
- KMP modules (`login`, `commonskmm`, `sync`, `aggregates`), `commonTest` source set: `testAndroidHostTest`
- KMP modules, `androidUnitTest` source set: `testAndroidDebugUnitTest`
- Desktop targets in KMP modules: `desktopTest`

---

## Project Structure

```
root/
├── app/                    # Main Android application
├── commonskmm/             # KMP shared utilities, base classes, DI helpers
├── login/                  # KMP login feature (Android + Desktop)
├── sync/                   # KMP sync feature
├── aggregates/             # KMP aggregate data feature
├── tracker/                # Android tracker feature
├── form/                   # Android form module
├── commons/                # Android shared utilities (legacy)
├── compose-table/          # Compose table component
├── dhis2-mobile-program-rules/ # KMP program rules engine
└── gradle/libs.versions.toml   # Central dependency catalog
```

**KMP module source sets:**
```
modulekmm/src/
├── commonMain/kotlin/      # Shared business logic, interfaces, use cases
├── commonTest/kotlin/      # Shared unit tests (kotlin-test + mockito-kotlin + turbine)
├── androidMain/kotlin/     # Android implementations, SDK access
├── androidUnitTest/kotlin/ # Android-specific unit tests
├── desktopMain/kotlin/     # Desktop implementations
└── composeResources/       # Shared Compose resources (strings, images)
```

---

## Code Style (enforced by ktlint 1.7.1)

Config in `.editorconfig`:
- **Style**: `ktlint_official`
- **No wildcard imports** (`ktlint_standard_no-wildcard-imports = enabled`)
- **No unused imports** (`ktlint_standard_no-unused-imports = enabled`)
- **Trailing commas required** on both call and declaration sites
- **Ordered imports** (`ktlint_standard_import-ordering = enabled`)
- **Function naming**: standard rule disabled — composables may use PascalCase per Compose conventions

**General Kotlin conventions:**
- JVM target: Java 17 (`sourceCompatibility = JavaVersion.VERSION_17`)
- Prefer `data class` over plain class for models
- Use `sealed class` / `sealed interface` for UI state
- Use `object` for singletons, companion objects for constants
- Prefer expression bodies for single-expression functions
- Document public APIs with KDoc

---

## Architecture Patterns

### Layer structure (per feature module)
```
domain/
  model/          # Pure data classes / sealed states
  usecase/        # Business logic, implements UseCase<R, T>
  repository/     # Repository interfaces
data/
  repository/     # Repository implementations (androidMain)
ui/
  state/          # UiState sealed classes
  viewmodel/      # ViewModels (expose StateFlow<UiState>)
  screen/         # @Composable screens
  component/      # Reusable composables
di/               # Koin module definitions
```

### UseCase interface (commonskmm)
All use cases must implement `UseCase<in R, out T>` from
`commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/UseCase.kt`:

```kotlin
// Interface definition
fun interface UseCase<in R, out T> {
    suspend operator fun invoke(input: R): Result<T>
}
// Parameterless convenience extension
suspend operator fun <T> UseCase<Unit, T>.invoke() = this(Unit)
```

Implementation pattern:
```kotlin
class SavePinUseCase(private val repo: SessionRepository) : UseCase<String, Unit> {
    override suspend fun invoke(input: String): Result<Unit> =
        try {
            repo.savePin(input)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
```

### ViewModel pattern
- Use `launchUseCase { }` (not `viewModelScope.launch`) — it wraps `CoroutineTracker`
  which integrates with Espresso's `IdlingResource` for reliable UI tests
- Expose state via `StateFlow`; collect in composables with `collectAsState()`

### Repository pattern (Android implementations)
- Translate `D2Error` → domain errors via `DomainErrorMapper`
- Required imports for Android impls:
  ```kotlin
  import org.dhis2.mobile.commons.error.DomainErrorMapper
  import org.hisp.dhis.android.core.maintenance.D2Error
  ```

### Dependency Injection (Koin 4.x)
```kotlin
val featureModule = module {
    single<MyRepository> { MyRepositoryImpl(get(), get()) }
    factory { MyUseCase(get()) }
    viewModel { MyViewModel(get()) }
}
```
- Define modules in `commonMain` where possible; use `expect`/`actual` for platform DI
- Inject ViewModels in composables with `koinViewModel()`

---

## UI Guidelines

- **Always prefer** DHIS2 design system components (`org.hisp.dhis.mobile.ui.designsystem.*`)
  over Material components
- Wrap screens in `DHIS2Theme { }` from `org.hisp.dhis.mobile.ui.designsystem.theme`
- Place shared Compose resources in `commonMain/composeResources/`
- Use multiplatform Compose Navigation (`org.jetbrains.androidx.navigation:navigation-compose`)
- Add `@Preview` annotations to validate composables in isolation

---

## Testing

- **Unit tests**: `mockito-kotlin` + `kotlin.test` in `commonTest`; `mockito-kotlin` + JUnit in `androidUnitTest` and legacy modules
- **Flow assertions**: Turbine (`app.cash.turbine`) + `kotlinx-coroutines-test`
- **UI tests**: Compose Testing + Espresso, Robot pattern, located in `androidInstrumentedTest/`
- **ViewModel coroutines**: always use `launchUseCase { }` — it wraps `CoroutineTracker` which integrates with Espresso's `IdlingResource`; never use `Thread.sleep()`

For patterns, examples, and common mistakes load the **android-testing** skill.

---

## Key Constraints

1. **Never create direct network or database calls** — use the DHIS2 Android SDK (`org.hisp.dhis.android.core.*`)
2. **Offline-first**: design features to work without connectivity; let the SDK handle sync
3. **KMP first**: put business logic in `commonMain`; keep `androidMain` to SDK/platform specifics
4. **No RxJava in new code**: migrate to Coroutines/Flow; wrap existing RxJava at boundaries
5. **ktlint must pass** before committing — run `./gradlew ktlintFormat` then `ktlintCheck`

---

## Contributing Conventions

### Jira

Work is tracked in the **ANDROAPP** project on `https://dhis2.atlassian.net`. Two fields are
**required** when creating an issue — the API rejects the request without them:

- **Components** — use `AndroidApp` for capture-app work (others: `datasets`, `LMIS`,
  `login`, `Mobile-UI`, `UX`)
- **Internal feature** — `General interest`, or `Internal to system` for refactors and
  dependency work users won't notice

### Branches

- Ticketed work: `ANDROAPP-<key>` or `ANDROAPP-<key>-<short-description>`
- Repo tooling, docs and dependency chores without a ticket: `<type>/<short-description>`,
  e.g. `chore/upgrade-gradle-9.5.0-agp-9.3.0`

**Name the branch correctly before opening the PR.** Renaming afterwards is not possible:
the GitHub rename API returns 403 (`Must have organization admin access to rename branches
protected by organization rulesets`), and GitHub cannot retarget an open PR's head branch —
so the only fix is a new branch and a new PR.

### Commits and PR titles

`<type>: [ANDROAPP-<key>] <subject>` for ticketed work, otherwise `<type>: <subject>`.
Types in use: `feat`, `fix`, `chore`, `test`, `build`, `deps`, `style`.

### PR size

`scripts/check_pr_size.sh` fails the `setup` job above **`MAX_LINES=400`** changed lines.
To bypass it deliberately, include `[skip size]` in the PR **title or body**.

---

## Verification Beyond the Standard Tasks

`./run_tests.sh` (ktlint + unit tests) does not cover everything CI does. Two gaps worth
knowing:

**compileSdk / AGP / SDK-surface changes** — also run what CI's `build-test-apks` job runs:

```bash
./gradlew :app:assembleDhis2Debug :app:assembleDhis2DebugAndroidTest :form:assembleAndroidTest
```

Raising `compileSdk` turns newly `@Nullable`-annotated platform APIs into hard Kotlin
nullables, and the breakage often lands only in `androidTest`, which neither
`assembleDhis2Debug` nor the unit tests compile. `app/build.gradle.kts` sets
`lint { abortOnError = false }`, so Android lint will not catch these either — only
compilation does. Takes ~17 min locally.

**Release/R8 changes** — `:app:assembleDhis2Release` cannot complete locally, because
`signingConfigs` reads `SIGNING_KEY_ALIAS`, `SIGNING_KEYSTORE_PATH` and friends from
`System.getenv()` and those are CI-only secrets; the build dies at signing for reasons
unrelated to your change. Use **`./gradlew :app:minifyDhis2ReleaseWithR8`** instead — R8 runs
before signing, so it exercises minification and keep rules without a keystore. This matters
because `.github/workflows/verify-dependency-metadata.yml` builds the three release variants.

> That workflow also **regenerates and auto-commits** `gradle/verification-metadata.xml` for
> same-repo PRs, so adding or bumping a dependency needs no manual checksum work. Locally the
> equivalent is `./gradlew --write-verification-metadata sha256 help`.

---

## Sentry Skills

Two custom skills for Sentry error triage and remediation. They are **multi-repo**:
crashes are attributed to the repo that owns the bug — this app, the DHIS2 Android SDK
(`dhis2/dhis2-android-sdk`), or the mobile design system (`dhis2/dhis2-mobile-ui`) —
using `.claude/skills/sentry-triage/references/repo-map.md` as the single source of
truth (package→repo attribution, per-repo commands, PR conventions). The two library
repos are expected as sibling clones (`../dhis2-android-sdk`, `../dhis2-mobile-ui`);
the skills offer to clone them if missing.

- **`/sentry-triage`** — Resolves the latest production release (and the SDK /
  design-system versions it pinned), queries the configured Sentry project for top
  unresolved issues, attributes each to its owning repo, scores Impact (1-5) and
  Effort (1-5), and outputs a prioritized impact/effort quadrant report. Requires the
  `sentry@claude-plugins-official` plugin installed locally in `~/.claude/settings.json`.

- **`/sentry-fix <issue-id> [--repo <slug>]`** — Fetches the full Sentry event and stack
  trace, re-verifies ownership, reads the relevant sources at the shipped version, and
  implements the fix **in the owning repo**: app fixes follow these AGENTS.md guidelines;
  library fixes run in an isolated git worktree of the sibling clone (never disturbing
  its checked-out branch), follow that repo's own conventions, and open the draft PR
  there. Usable standalone or from a `/sentry-triage` report.

Stack traces are deobfuscated (ProGuard mappings are uploaded on every release build),
so library frames carry real class names. Sentry org and project are resolved
dynamically from the plugin at runtime.
