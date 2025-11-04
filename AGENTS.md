# DHIS2 Android Capture App - Development Guidelines

This document serves as a comprehensive guide for developers working on the DHIS2 Android Capture
App, outlining best practices, architecture patterns, and implementation examples. It is designed to
be useful for both human developers and AI assistants like Copilot.

## Project Overview

This project is a DHIS2 Android application that is migrating from a traditional Android app to a *
*Kotlin Multiplatform (KMP)** project. The migration is ongoing with the goal of eventually becoming
a full Compose Multiplatform application supporting Android, iOS, and Desktop platforms.

## Technology Stack

### Core Technologies

- **Kotlin**: Primary programming language
- **Kotlin Multiplatform (KMP)**: Target platform for cross-platform development
- **Compose Multiplatform**: UI framework for all platforms
- **Gradle**: Build system with Kotlin DSL

### UI Framework

- **Primary UI**: Use `@dhis2/dhis2-mobile-ui` design system (based on Compose Multiplatform)
    - Use latest stable version (check `gradle/libs.versions.toml` for current version)
    - Import: `org.hisp.dhis.mobile.ui.designsystem.*`
    - Always prefer DHIS2 design system components over Material components when available

### Data Layer

- **DHIS2 Android SDK**: Use `@dhis2/dhis2-android-sdk` for all data operations
    - Use latest stable version (check `gradle/libs.versions.toml` for current version)
    - Import: `org.hisp.dhis.android.core.*`
    - Handles persistence, offline/online synchronization, and DHIS2 API communication
    - Never create direct network calls or database operations - use the SDK

## Architecture and Patterns

This section describes the core architecture patterns used in the project, along with implementation
examples and best practices.

### MVVM Architecture

The Model-View-ViewModel (MVVM) pattern is used to separate concerns between the UI (View), business
logic (ViewModel), and data (Model).

- **ViewModels**: Manage UI state and business logic
    - Use `androidx.lifecycle.ViewModel` or platform-specific equivalents
    - Expose state via `StateFlow` and `Flow`
    - Handle UI events and coordinate with repositories/use cases

- **Views/Composables**: UI layer that observes ViewModel state
    - Use `@Composable` functions for UI components
    - Collect state using `collectAsState()`
    - Keep composables pure and stateless when possible
    - Use Compose multiplatform previews (`@Preview`) to validate UI components

#### ViewModel Implementation Example

```kotlin
class ExampleViewModel(
    private val getDataUseCase: GetDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Use the `launchUseCase` ViewModel extension which wraps coroutine tracking and uses
        // a background dispatcher by default (it increments/decrements CoroutineTracker).
        launchUseCase {
            // `getDataUseCase()` is an extension for `UseCase<Unit, T>` that calls the use case with Unit
            val result = getDataUseCase()
            result.fold(
                onSuccess = { flow ->
                    flow
                        .catch { _uiState.value = UiState.Error(it.message ?: "Unknown error") }
                        .collect { _uiState.value = UiState.Success(it) }
                },
                onFailure = { throwable ->
                    _uiState.value = UiState.Error(throwable.message ?: "Unknown error")
                }
            )
        }
    }
}
```

#### Composable Implementation Example

```kotlin
@Composable
fun ExampleScreen(
    viewModel: ExampleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DHIS2Theme {
        when (uiState) {
            is UiState.Loading -> LoadingIndicator()
            is UiState.Success -> ExampleContent(uiState.data)
            is UiState.Error -> ErrorMessage(uiState.message)
        }
    }
}
```

### Repository Pattern

Repositories provide an abstraction layer over data sources, handling data access and mapping
between SDK models and domain models.

- **Repositories**: Abstract data access layer
    - Define interfaces in `commonMain`
    - Implement platform-specific versions in `androidMain`, `desktopMain`, etc.
    - Use DHIS2 SDK for data operations
    - Handle data mapping between SDK models and domain models
    - Map SDK exceptions to domain errors: repository implementations should translate platform/SDK
      exceptions into domain-level errors using the project's `DomainErrorMapper` (or equivalent).
      This keeps the domain layer SDK-agnostic and makes error handling consistent across the app.
        - Imports you will commonly need in Android implementations:
          ```
          import org.dhis2.mobile.commons.error.DomainErrorMapper
          import org.hisp.dhis.android.core.maintenance.D2Error
          ```
        - Recommendations:
            - Keep repository method signatures `suspend` and let them throw domain-level exceptions
              rather than returning raw SDK exceptions.
            - Catch/translate only SDK-specific exceptions; allow unexpected exceptions to bubble up
              or wrap them in a generic domain error if appropriate.
            - Write unit tests that mock `DomainErrorMapper` to assert error mapping behavior.

#### Repository Implementation Example

```kotlin
class ExampleRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper
) : ExampleRepository {
    override suspend fun getData(): Flow<List<ExampleData>> {
        return try {
            d2.exampleModule().examples()
                .get()
                .asFlow()
                .map { it.map { example -> example.toDomainModel() } }
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        }
    }
}
```

### Use Cases

Use cases encapsulate complex business logic, providing a clear interface for ViewModels to interact
with the domain layer.

- **Use Cases**: Encapsulate complex business logic
    - Single responsibility principle
    - Implement the shared `UseCase` interface: `UseCase<in R, out T>` (see
      `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/UseCase.kt`). Use cases must
      implement/extend this interface so they return a `Result<T>` from their `invoke` method.
    - For parameterless use cases use `UseCase<Unit, T>` and the provided extension
      `suspend operator fun <T> UseCase<Unit, T>.invoke() = this(Unit)` to call them without passing
      `Unit` explicitly.
    - Return `Result<T>` (often wrapping a `Flow<T>` when the use case emits a stream). Handle
      exceptions inside the use case and wrap success/failure using `Result.success` /
      `Result.failure`.
    - Coordinate between multiple repositories if needed
    - Return `Flow` or `suspend` functions for async operations
    - Place in domain layer

#### Use Case Implementation Example

```kotlin
class GetDataUseCase(
    private val repository: ExampleRepository
) : UseCase<Unit, Flow<List<ExampleData>>> {
    override suspend operator fun invoke(input: Unit): Result<Flow<List<ExampleData>>> {
        return try {
            val flow = repository.getData()
                .map { data -> data.filter { it.isValid } }
            Result.success(flow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### State Management

Use sealed classes to represent different UI states in a type-safe manner.

- **Sealed Classes**: Use for representing different states
  ```kotlin
  sealed class UiState {
      object Loading : UiState()
      data class Success(val data: T) : UiState()
      data class Error(val message: String) : UiState()
  }
  ```

### Reactive Programming

Leverage Kotlin's Flow and Coroutines for reactive and asynchronous programming.

- **Flow**: For reactive data streams
    - Use `StateFlow` for state that can be observed
    - Use `Flow` for data streams
    - Combine flows using operators like `combine`, `flatMapLatest`

- **Coroutines**: For asynchronous programming
    - Use `viewModelScope` in ViewModels
    - Handle errors with try-catch blocks
    - Use `Dispatchers.IO` for I/O operations

### Dependency Injection

Koin is used for dependency injection, supporting multiplatform development.

- **Koin**: Dependency injection framework
    - Use latest stable version (check `gradle/libs.versions.toml` for current version)
    - Define modules in `commonMain` when possible
    - Use `expect`/`actual` pattern for platform-specific dependencies
    - Module structure: `val commonModule: Module = module { ... }`

#### Koin Module Definition Example

```kotlin
val exampleModule = module {
    single<ExampleRepository> { ExampleRepositoryImpl(get()) }
    single<GetDataUseCase> { GetDataUseCase(get()) }
    viewModel { ExampleViewModel(get()) }
}
```

## Project Structure

### Multiplatform Module Organization

```
modulekmm/
├── src/
│   ├── commonMain/kotlin/     # Shared code
│   ├── commonTest/kotlin/     # Shared tests
│   ├── androidMain/kotlin/    # Android-specific code
│   ├── androidUnitTest/kotlin/# Android unit tests
│   ├── desktopMain/kotlin/    # Desktop-specific code
│   └── iosMain/kotlin/        # iOS-specific code (when applicable)
```

### Code Organization

- **Domain Layer**: Models, use cases, repository interfaces
- **Data Layer**: Repository implementations, data sources
- **UI Layer**: Composables, ViewModels, navigation
- **DI Layer**: Dependency injection modules

### Platform-Specific Code

- Use `expect`/`actual` pattern for platform differences
- Keep platform-specific code minimal
- Prefer shared implementations in `commonMain`

## Development Guidelines

### Compose Multiplatform

- **Components**: Always check DHIS2 design system first
  - Use components from `org.hisp.dhis.mobile.ui.designsystem.component.*`
  - Use theme from `org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme`
- **Navigation**: Use Compose Navigation for multiplatform
- **Resources**: Place in `commonMain/composeResources/`
- **Theming**: Use DHIS2Theme wrapper

### Data Operations

- **Never bypass the SDK**: Always use DHIS2 Android SDK for all data operations
  - Use components from `org.hisp.dhis.android.core.*`
- **Offline-first**: Design with offline capabilities in mind
- **Sync handling**: Let the SDK handle synchronization
- **Error handling**: Handle SDK exceptions appropriately

### Testing

- **Unit Tests**: Place in appropriate test directories
- **Shared Tests**: Use `commonTest` for platform-agnostic tests
- **Mocking**: Use MockK for Kotlin-friendly mocking
- **Repository Tests**: Mock DHIS2 SDK components
- **UI Tests**: Follow the Robot pattern for instrumented tests (see detailed section below)

### UI Testing Guidelines

- **Location**: Place UI tests in `androidInstrumentedTest`
- **Pattern**: Use Robot pattern for test actions and assertions
- **Async handling**: Use `CoroutineTracker` with `launchUseCase` - never use hard-coded delays
    - Espresso's `IdlingResource` automatically waits for tracked operations to complete
    - This enables faster, more reliable tests without manual wait mechanisms
- **Test tags**: Add `Modifier.testTag()` to interactive UI components
    - Format: `{SCREEN}_{COMPONENT}_TAG` (e.g., `LOGIN_BUTTON_TAG`)
    - Export constants from screen files for test imports
- **DHIS2 design system components**: These are composite components
    - Click the wrapper with your test tag to focus it
    - Use `"INPUT_TEXT_FIELD"` tag to find inner fields
    - Use `performTextInput()` (not `performTextReplacement()`)
- **Mock server**: Use `MockWebServerRobot` for API mocking
- **Best practices**:
    1. Use `waitUntilExactlyOneExists()` for element visibility
    2. Use descriptive robot method names (e.g., `clickLoginButton()`)
    3. Keep robots focused on actions, not assertions
    4. Test user flows, not isolated components
    5. Mock all external dependencies (network, SDK responses)
    6. Clean up after tests (databases, preferences)

#### Example: Robot Class

```kotlin
fun exampleRobot(
    composeTestRule: ComposeTestRule,
    robotBody: ExampleRobot.() -> Unit
) {
    ExampleRobot(composeTestRule).apply { robotBody() }
}

class ExampleRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {
    fun typeUsername(username: String) {
        composeTestRule.waitUntilExactlyOneExists(hasTestTag(USERNAME_TAG), TIMEOUT)
        composeTestRule.onNodeWithTag(USERNAME_TAG).performClick()
        composeTestRule.onAllNodesWithTag("INPUT_TEXT_FIELD")[0].performTextInput(username)
    }
    
    fun clickSubmitButton() {
        composeTestRule.waitUntilExactlyOneExists(hasTestTag(SUBMIT_TAG), TIMEOUT)
        composeTestRule.onNodeWithTag(SUBMIT_TAG).performClick()
    }
}
```

#### Example: Test Structure

```kotlin
class ExampleTest : BaseTest() {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun shouldPerformSuccessfulAction() {
        mockWebServerRobot.addResponse(GET, "/api/endpoint", MOCK_RESPONSE, 200)
        
        exampleRobot(composeTestRule) {
            typeUsername("user")
            clickSubmitButton()
            // IdlingResource handles async automatically
            checkSuccessMessageDisplayed()
        }
        
        cleanDatabase()
    }
}
```

### Code Style

- **Kotlin conventions**: Follow official Kotlin coding conventions
- **ktlint**: Project uses ktlint for formatting
- **Imports**: Organize imports, prefer explicit imports
- **Documentation**: Document public APIs with KDoc

## Best Practices & Migration Guidelines

### Core Development Practices

1. **Always use DHIS2 design system components** before falling back to Material components
2. **Never create direct database or network operations** - use DHIS2 SDK exclusively
3. **Keep business logic in ViewModels or Use Cases**, not in Composables
4. **Use sealed classes for state representation**
5. **Prefer composition over inheritance**
6. **Write tests for business logic and repositories**
7. **Handle loading and error states appropriately**
8. **Follow offline-first design principles**
9. **Keep platform-specific code minimal** - use `expect`/`actual` pattern
10. **Use meaningful commit messages and follow Git flow**

### Important Warnings

- **Coroutine cancellation**: Remember to handle coroutine cancellation properly
- **SDK exception handling**: DHIS2 SDK operations might throw exceptions - always handle them
- **Component availability**: Some DHIS2 design system components might not be available yet - check documentation
- **RxJava migration**: When migrating from RxJava to Coroutines/Flow, ensure proper error handling
- **Platform-specific resources**: Handle resources differently in multiplatform (not all platforms support identical APIs)

### Migrating from Android to KMP

- **Code organization**: Move shared logic to `commonMain`; extract platform-specific code to `androidMain`, `desktopMain`, etc.
- **UI conversion**: Convert View-based UI to Compose Multiplatform
- **Dependency injection**: Update to use Koin multiplatform
- **Libraries**: Prefer multiplatform libraries over platform-specific ones
- **Compatibility**: Check compatibility with Compose Multiplatform before selecting dependencies
- **Documentation**: Document public APIs with KDoc


## Resources

- [DHIS2 Mobile UI Documentation](https://ui.dhis2.nu/components)
- [DHIS2 Android SDK Documentation](https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/android-sdk.html)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
- [Koin Multiplatform Documentation](https://insert-koin.io/docs/reference/koin-mp/kmp)
- [Android Architecture Guidelines](https://developer.android.com/topic/architecture)
- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing)
- [Espresso Idling Resources](https://developer.android.com/training/testing/espresso/idling-resource)
