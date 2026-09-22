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
- `app`: `testDhis2DebugUnitTest` (it has flavours; the others do not)
- Other AGP modules (`form`, `commons`, `compose-table`, `stock-usecase`,
  `dhis_android_analytics`, `dhis2_android_maps`, `dhis2-mobile-program-rules`):
  `testDebugUnitTest`
- KMP modules (`login`, `sync`, `aggregates`, `commonskmm`, `tracker`) — **both** the
  `commonTest` and `androidHostTest` source sets: `testAndroidHostTest`

`testAndroidDebugUnitTest` is **not a task in this repo**, and there is no
`desktopTest` source set in any module despite the desktop targets.

---

## Project Structure

```
root/
├── app/                    # Main Android application
├── commonskmm/             # KMP shared utilities, base classes, DI helpers
├── login/                  # KMP login feature (Android + Desktop)
├── sync/                   # KMP sync feature
├── aggregates/             # KMP aggregate data feature
├── tracker/                # KMP tracker feature
├── form/                   # Android form module
├── commons/                # Android shared utilities (legacy)
├── compose-table/          # Compose table component
├── stock-usecase/          # Android stock management feature
├── dhis_android_analytics/ # Android analytics/charts
├── dhis2_android_maps/     # Android maps
├── dhis2-mobile-program-rules/ # Android (com.android.library) program rules engine
└── gradle/libs.versions.toml   # Central dependency catalog
```

**KMP module source sets:**
```
modulekmm/src/
├── commonMain/kotlin/      # Shared business logic, interfaces, use cases
├── commonTest/kotlin/      # Shared unit tests (kotlin-test + mockito-kotlin + turbine)
├── androidMain/kotlin/     # Android implementations, SDK access
├── androidHostTest/kotlin/ # Unit tests that need androidMain (e.g. mock D2)
├── desktopMain/kotlin/     # Desktop implementations
└── composeResources/       # Shared Compose resources (strings, images)
```

`androidHostTest` **depends on** `commonTest`, so a test dependency declared in
`commonTest` is already on its compile classpath — no need to declare it twice.
Both source sets are run by the same `testAndroidHostTest` task.

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
New use cases implement `UseCase<in R, out T>` from
`commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/UseCase.kt`:

```kotlin
// Interface definition
fun interface UseCase<in R, out T> {
    suspend operator fun invoke(input: R): Result<T>
}
// Parameterless convenience extension
suspend operator fun <T> UseCase<Unit, T>.invoke() = this(Unit)
```

It is the dominant pattern already — 33 implementations across `app` (14), `sync` (9),
`login` (6), `tracker` (3) and `commonskmm` (1). Where it is not used, a plain
`suspend operator fun invoke` returning `Result<T>` is accepted.

Implementation pattern:
```kotlin
class SavePinUseCase(private val repo: SessionRepository) : UseCase<String, Unit> {
    override suspend fun invoke(input: String): Result<Unit> =
        try {
            repo.savePin(input)
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Result.failure(error)
        }
}
```

**Catch `Throwable`, not `Exception`.** `DomainError` is declared
`sealed class DomainError : Throwable()`, so `catch (e: Exception)` does **not**
match it: a mapped SDK error escapes the `try` entirely and propagates to a caller
that was promised a `Result`. Rethrow `CancellationException` first, as coroutine
convention requires.

### ViewModel pattern
- Use `launchUseCase { }` (not `viewModelScope.launch`) — it wraps `CoroutineTracker`
  which integrates with Espresso's `IdlingResource` for reliable UI tests.
  **Required** in KMP-module ViewModels and in any ViewModel with instrumented
  coverage. The ~140 existing `viewModelScope.launch` sites in legacy modules are
  not being migrated; do not "fix" them as drive-by changes
- Expose state via `StateFlow`; collect in composables with `collectAsState()`

### Repository pattern (Android implementations)
Wrap SDK calls in `withDomainErrors { }` / `withDomainErrorsAsResult { }` from
`org.dhis2.mobile.commons.error` rather than catching `D2Error` inline:

They are extension functions on `DomainErrorMapper`, so call them on the injected
mapper:

```kotlin
suspend fun getData(): List<Item> = domainErrorMapper.withDomainErrors {
    d2.someModule().someRepository().blockingGet().map(::toDomain)
}
```

Why: the SDK's blocking RxJava operators rewrap the checked `D2Error` in a
`RuntimeException`, so an inline `catch (d2Error: D2Error)` misses every blocking
call. The wrappers walk the cause chain (`Throwable.asD2Error()`, depth 5, with a
self-reference guard) before mapping, so both shapes are handled.

Use `withDomainErrorsAsResult { }` when the call reports failure as a `Result`
instead of throwing. `SyncDataSetRepositoryImpl` is a worked example.

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

**The source of truth is `.claude/skills/android-testing/`** — load it before writing
any test. `SKILL.md` routes you to the right source set and Gradle task,
`references/unit-testing.md` covers host tests, `references/instrumented-testing.md`
covers device tests.

The rules that hold everywhere:
- **mockito-kotlin only** (`mock()`, `whenever()`, `verify()`). Never MockK
- **JUnit4 / `kotlin.test` annotations only** — `org.junit.jupiter` is excluded from
  every test configuration and a Jupiter `@Test` fails the build
- **`D2` must be mocked with `RETURNS_DEEP_STUBS`** — its call chains NPE otherwise
- **Never `Thread.sleep()`** or any hard-coded delay
- **Confirm the test actually ran** — check `build/test-results/<task>/TEST-<FQCN>.xml`
  for `tests="N"`. A class that was never collected produces no file, and the build
  still goes green

---

## Key Constraints

1. **Never create direct network or database calls** — use the DHIS2 Android SDK (`org.hisp.dhis.android.core.*`)
2. **Offline-first**: design features to work without connectivity; let the SDK handle sync
3. **KMP first**: put business logic in `commonMain`; keep `androidMain` to SDK/platform specifics
4. **No RxJava in new code**: migrate to Coroutines/Flow; wrap existing RxJava at boundaries
5. **ktlint must pass** before committing — run `./gradlew ktlintFormat` then `ktlintCheck`

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
