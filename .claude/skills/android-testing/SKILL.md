---
name: android-testing
description: >
  Guidelines for writing unit tests (mockito-kotlin, Turbine, runTest, fakes for iOS commonTest)
  and, in references/ui-tests.md, UI instrumented tests (Robot pattern, CoroutineTracker, Compose
  test tags, fixtures, the CI device matrix) for the DHIS2 Android KMP project. Load this when
  creating, fixing, or reviewing any test in the codebase.
---

# DHIS2 Android Testing Guidelines

## Testing Stack

- **Unit Tests**: `mockito-kotlin` (`mock()`, `whenever()`, `verify()`), JUnit / `kotlin.test`
- **Flow tests**: Turbine (`app.cash.turbine`) + `kotlinx-coroutines-test`
- **UI Tests**: Compose Testing + Espresso with Robot pattern
- **Test locations**:
  - `commonTest/` — platform-agnostic unit tests (`kotlin.test` annotations: `@Test`, `@BeforeTest`).
    Also run on the desktop JVM by `desktopTest`, and on iOS in modules with an iOS target (module
    table in `AGENTS.md`): there, **no Mockito, JUnit or `java.*`** — write fakes (see
    [Fakes in commonTest](#fakes-in-commontest-modules-with-ios)). Modules without iOS still have
    Mockito in commonTest; prefer fakes for new tests there too, so the module can get iOS later.
  - `androidHostTest/` — KMP unit tests that need `androidMain`, e.g. a mocked `D2` (JUnit4 / `kotlin.test`)
  - `jvmCommonTest/` — tests of `jvmCommonMain` code in modules with iOS
  - `src/test/` — unit tests in AGP modules (`app`, `form`, `commons`, …); `app` also has `src/testDhis2/`
  - `src/androidTest/` — instrumented tests. User-flow UI tests live in `app`; `commons` and
    `compose-table` have component-level instrumented tests only

Commands, the test task for each module type, and how to confirm a test ran: `AGENTS.md`, Commands.

**Instrumented tests** (`src/androidTest/` in any module): read
[references/ui-tests.md](references/ui-tests.md) before writing, fixing or reviewing one. It covers
the Robot pattern, test tags, design system inputs, fixtures and the CI device matrix (portrait and
landscape).

## Critical Rule: No Hard-Coded Delays

ViewModels use `launchUseCase { }` which wraps `CoroutineTracker`. Espresso's
`IdlingResource` automatically waits for tracked coroutines. `Thread.sleep()` and
hard-coded timeouts are **forbidden**.

```kotlin
// ✅ CORRECT — IdlingResource waits automatically
@Test
fun shouldLoadData() {
    exampleRobot(composeTestRule) {
        clickLoadButton()
        verifyDataDisplayed()  // no delay needed
    }
}

// ❌ WRONG
@Test
fun shouldLoadData() {
    clickLoadButton()
    Thread.sleep(2000)  // FORBIDDEN
    verifyDataDisplayed()
}
```

## Mocking: mockito-kotlin only

Use `mock()`, `whenever()`, `verify()` from `org.mockito.kotlin`. Do **not** use MockK.

```kotlin
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
```

## Unit Test Patterns

### Use Case test

Use cases are named as actions (`SavePin`, not `SavePinUseCase`) and never throw, so test both
the success and the failure `Result`.

```kotlin
class SavePinTest {
    private val repository: SessionRepository = mock()
    private val savePin = SavePin(repository)

    @Test
    fun `should return success when pin is saved`() = runTest {
        whenever(repository.savePin("1234")).thenReturn(Unit)

        val result = savePin("1234")

        assertTrue(result.isSuccess)
        verify(repository).savePin("1234")
    }

    @Test
    fun `should return failure when repository throws a domain error`() = runTest {
        val error = DomainError.NetworkError("offline")
        // DomainError is a checked exception for Mockito: thenThrow fails on a suspend function.
        whenever(repository.savePin("1234")).thenAnswer { throw error }

        val result = savePin("1234")

        assertEquals(error, result.exceptionOrNull())
    }
}
```

### Fakes in commonTest (modules with iOS)

In a module that compiles for iOS, `commonTest` cannot use Mockito. Implement the repository
interface with a small fake instead, keeping state in plain fields. The example is `login`'s
`SessionRepository`, written the way it must be once `login` gets its iOS target.

```kotlin
private class FakeSessionRepository(
    var failure: Exception? = null,
) : SessionRepository {
    val savedPins = mutableListOf<String>()
    var locked = false

    override suspend fun savePin(pin: String) {
        failure?.let { throw it }
        savedPins += pin
    }

    override suspend fun verifyPin(pin: String) =
        when {
            savedPins.isEmpty() -> PinVerification.NoPinStored
            savedPins.last() == pin -> PinVerification.Match
            else -> PinVerification.Mismatch
        }

    override suspend fun deletePin() = savedPins.clear()

    override suspend fun setSessionLocked(locked: Boolean) {
        this.locked = locked
    }

    override suspend fun isSessionLocked() = locked

    override suspend fun logout() = Unit
}

class SavePinTest {
    private val repository = FakeSessionRepository()
    private val savePin = SavePin(repository)

    @Test
    fun savesThePin() = runTest {
        assertTrue(savePin("1234").isSuccess)
        assertEquals(listOf("1234"), repository.savedPins)
    }
}
```

### ViewModel test

```kotlin
class ExampleViewModelTest {
    private val getData: GetData = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ExampleViewModel

    // launchUseCase runs on viewModelScope (Main), so replace Main for the test.
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should emit success state when use case succeeds`() = runTest {
        whenever(getData()).thenReturn(Result.success(flowOf(listOf(item))))

        viewModel = ExampleViewModel(getData)

        viewModel.uiState.test {
            assertEquals(UiState.Success(listOf(item)), awaitItem())
        }
    }
}
```

### Repository test

Repository tests go in `androidHostTest` (they need the SDK). The repository owns its dispatcher,
so inject a `Dispatcher` built from the test dispatcher — never rely on `Dispatchers.IO`.
Most existing repository tests still stub `blocking*` or Rx calls and set `Dispatchers.Main`:
follow this template, not them.

```kotlin
class ExampleRepositoryTest {
    // D2 chains calls across multiple intermediate objects — RETURNS_DEEP_STUBS is required
    // so that d2.someModule().someRepository().suspendGet() doesn't NPE on the intermediates.
    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val domainErrorMapper: DomainErrorMapper = mock()
    private val testDispatcher = StandardTestDispatcher()
    private val repository = ExampleRepositoryImpl(
        d2 = d2,
        domainErrorMapper = domainErrorMapper,
        dispatcher = Dispatcher(io = testDispatcher, main = testDispatcher, default = testDispatcher),
    )

    @Test
    fun `should map D2Error to domain error`() = runTest(testDispatcher) {
        val d2Error = D2Error.builder().errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR).build()
        val domainError = DomainError.ServerError("error")
        whenever(d2.exampleModule().examples().suspendGet()).thenAnswer { throw d2Error }
        whenever(domainErrorMapper.mapToDomainError(d2Error)).thenReturn(domainError)

        val thrown = assertFailsWith<DomainError> { repository.getData() }

        assertEquals(domainError, thrown)
    }
}
```

Use `assertFailsWith` (or `try`/`catch`) to check a thrown error — never `runCatching`, which
also swallows `CancellationException`.

## Common Mistakes to Avoid

- Using `Thread.sleep()` or any hard-coded delays
- Using MockK (`mockk()`, `every {}`, `coEvery {}`) — use mockito-kotlin instead
- Using Mockito, JUnit or `java.*` in `commonTest` of a module with an iOS target — use fakes
- Using `runCatching` in a test or production code — use `assertFailsWith` / explicit catches
- Mocking `D2` without `RETURNS_DEEP_STUBS`
- Declaring a test done without checking `build/test-results/<task>/TEST-<FQCN>.xml` shows it ran
