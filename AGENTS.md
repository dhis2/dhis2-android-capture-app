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
