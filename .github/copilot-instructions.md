# DHIS2 Android Capture App - Copilot Instructions

## Project Overview

This project is a DHIS2 Android application that is migrating from a traditional Android app to a **Kotlin Multiplatform (KMP)** project. The migration is ongoing with the goal of eventually becoming a full Compose Multiplatform application supporting Android, iOS, and Desktop platforms.

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

## Architecture Patterns

### MVVM Architecture
- **ViewModels**: Manage UI state and business logic
  - Use `androidx.lifecycle.ViewModel` or platform-specific equivalents
  - Expose state via `StateFlow` and `Flow`
  - Handle UI events and coordinate with repositories/use cases
  
- **Views/Composables**: UI layer that observes ViewModel state
  - Use `@Composable` functions for UI components
  - Collect state using `collectAsState()`
  - Keep composables pure and stateless when possible
  - Use Compose multiplatform previews (`@Preview`) to validate UI components

### Repository Pattern
- **Repositories**: Abstract data access layer
  - Define interfaces in `commonMain`
  - Implement platform-specific versions in `androidMain`, `desktopMain`, etc.
  - Use DHIS2 SDK for data operations
  - Handle data mapping between SDK models and domain models

### Use Cases
- **Use Cases**: Encapsulate complex business logic
  - Single responsibility principle
  - Implement the shared `UseCase` interface: `UseCase<in R, out T>` (see `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/UseCase.kt`). Use cases must implement/extend this interface so they return a `Result<T>` from their `invoke` method.
  - For parameterless use cases use `UseCase<Unit, T>` and the provided extension `suspend operator fun <T> UseCase<Unit, T>.invoke() = this(Unit)` to call them without passing `Unit` explicitly.
  - Return `Result<T>` (often wrapping a `Flow<T>` when the use case emits a stream). Handle exceptions inside the use case and wrap success/failure using `Result.success` / `Result.failure`.
  - Coordinate between multiple repositories if needed
  - Return `Flow` or `suspend` functions for async operations
  - Place in domain layer

### Dependency Injection
- **Koin**: Dependency injection framework
  - Version: `4.1.1`
  - Define modules in `commonMain` when possible
  - Use `expect`/`actual` pattern for platform-specific dependencies
  - Module structure: `val commonModule: Module = module { ... }`

### State Management
- **Sealed Classes**: Use for representing different states
  ```kotlin
  sealed class UiState {
      object Loading : UiState()
      data class Success(val data: T) : UiState()
      data class Error(val message: String) : UiState()
  }
  ```

### Reactive Programming
- **Flow**: For reactive data streams
  - Use `StateFlow` for state that can be observed
  - Use `Flow` for data streams
  - Combine flows using operators like `combine`, `flatMapLatest`

- **Coroutines**: For asynchronous programming
  - Use `viewModelScope` in ViewModels
  - Handle errors with try-catch blocks
  - Use `Dispatchers.IO` for I/O operations

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
```
import org.hisp.dhis.mobile.ui.designsystem.component.*
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
```
- **Navigation**: Use Compose Navigation for multiplatform
- **Resources**: Place in `commonMain/composeResources/`
- **Theming**: Use DHIS2Theme wrapper

### Data Operations
- **Never bypass the SDK**: Always use DHIS2 Android SDK for data operations
- **Offline-first**: Design with offline capabilities in mind
- **Sync handling**: Let the SDK handle synchronization
- **Error handling**: Handle SDK exceptions appropriately

### Testing
- **Unit Tests**: Place in appropriate test directories
- **Shared Tests**: Use `commonTest` for platform-agnostic tests
- **Mocking**: Use MockK for Kotlin-friendly mocking
- **Repository Tests**: Mock DHIS2 SDK components

### Code Style
- **Kotlin conventions**: Follow official Kotlin coding conventions
- **ktlint**: Project uses ktlint for formatting
- **Imports**: Organize imports, prefer explicit imports
- **Documentation**: Document public APIs with KDoc

## Common Patterns

### ViewModel with Use Cases
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
        viewModelScope.launch {
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

### Repository Implementation
```kotlin
class ExampleRepositoryImpl(
    private val d2: D2
) : ExampleRepository {
    override fun getData(): Flow<List<ExampleData>> {
        return d2.exampleModule().examples()
            .get()
            .asFlow()
            .map { it.map { example -> example.toDomainModel() } }
    }
}
```

### Use Case Implementation
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

### Koin Module Definition
```kotlin
val exampleModule = module {
    single<ExampleRepository> { ExampleRepositoryImpl(get()) }
    single<GetDataUseCase> { GetDataUseCase(get()) }
    viewModel { ExampleViewModel(get()) }
}
```

### Composable with ViewModel
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

## Migration Guidelines

### From Android to KMP
- Move shared logic to `commonMain`
- Extract platform-specific code to appropriate platform directories
- Convert View-based UI to Compose
- Update dependency injection to use Koin multiplatform

### Dependencies
- Prefer multiplatform libraries over platform-specific ones
- Check compatibility with Compose Multiplatform
- Use expect/actual for platform-specific dependencies

## Best Practices

1. **Always use DHIS2 design system components** before falling back to Material components
2. **Never create direct database or network operations** - use DHIS2 SDK
3. **Keep business logic in ViewModels or Use Cases**, not in Composables
4. **Use sealed classes for state representation**
5. **Prefer composition over inheritance**
6. **Write tests for business logic and repositories**
7. **Use meaningful commit messages and follow Git flow**
8. **Handle loading and error states appropriately**
9. **Follow offline-first design principles**
10. **Keep platform-specific code minimal and well-documented**

## Common Gotchas

- Remember to handle coroutine cancellation properly
- DHIS2 SDK operations might throw exceptions - always handle them
- Some DHIS2 design system components might not be available yet - check documentation
- When migrating from RxJava to Coroutines/Flow, ensure proper error handling
- Platform-specific resources need to be handled differently in multiplatform

## Resources

- [DHIS2 Mobile UI Documentation](https://ui.dhis2.nu/components)
- [DHIS2 Android SDK Documentation](https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/android-sdk.html)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
- [Koin Multiplatform Documentation](https://insert-koin.io/docs/reference/koin-mp/kmp)
- [Android Architecture Guidelines](https://developer.android.com/topic/architecture)