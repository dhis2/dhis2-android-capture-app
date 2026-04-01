---
name: android-testing
description: >
  Guidelines for writing unit tests (mockito-kotlin, Turbine, runTest) and
  UI instrumented tests (Robot pattern, CoroutineTracker, Compose test tags)
  for the DHIS2 Android KMP project. Load this when creating, fixing, or
  reviewing any test in the codebase.
---

# DHIS2 Android Testing Guidelines

## Testing Stack

- **Unit Tests**: `mockito-kotlin` (`mock()`, `whenever()`, `verify()`), JUnit / `kotlin.test`
- **Flow tests**: Turbine (`app.cash.turbine`) + `kotlinx-coroutines-test`
- **UI Tests**: Compose Testing + Espresso with Robot pattern
- **Test locations**:
  - `commonTest/` — platform-agnostic unit tests (`kotlin.test` annotations: `@Test`, `@BeforeTest`)
  - `androidUnitTest/` — Android-specific unit tests (JUnit `@Test`)
  - `androidInstrumentedTest/` / `androidTest/` — UI/instrumented tests

## Run Commands

```bash
# All unit tests
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest

# Single KMP module test class (commonTest source set)
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest"

# Single legacy Android module test class
./gradlew :form:testDebugUnitTest --tests "org.dhis2.form.ui.FormViewModelTest"

# Single test method (commonTest source set)
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest.initial screen is set correctly when starting"
```

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
```kotlin
class SavePinUseCaseTest {
    private val repository: SessionRepository = mock()
    private val useCase = SavePinUseCase(repository)

    @Test
    fun `should return success when pin is saved`() = runTest {
        whenever(repository.savePin("1234")).thenReturn(Unit)

        val result = useCase("1234")

        assertTrue(result.isSuccess)
        verify(repository).savePin("1234")
    }
}
```

### ViewModel test
```kotlin
class ExampleViewModelTest {
    private val useCase: GetDataUseCase = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ExampleViewModel

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
        whenever(useCase()).thenReturn(Result.success(flowOf(listOf(item))))

        viewModel = ExampleViewModel(useCase)

        viewModel.uiState.test {
            assertEquals(UiState.Success(listOf(item)), awaitItem())
        }
    }
}
```

### Repository test
```kotlin
class ExampleRepositoryTest {
    // D2 chains calls across multiple intermediate objects — RETURNS_DEEP_STUBS is required
    // so that d2.someModule().someRepository().blockingGet() doesn't NPE on the intermediates.
    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val domainErrorMapper: DomainErrorMapper = mock()
    private val repository = ExampleRepositoryImpl(d2, domainErrorMapper)

    @Test
    fun `should map D2Error to domain error`() = runTest {
        val d2Error = D2Error.builder().errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR).build()
        whenever(d2.exampleModule().examples().blockingGet()).thenThrow(d2Error)
        whenever(domainErrorMapper.mapToDomainError(d2Error)).thenReturn(DomainException("error"))

        val result = runCatching { repository.getData() }

        assertTrue(result.isFailure)
        verify(domainErrorMapper).mapToDomainError(d2Error)
    }
}
```

## UI Tests: Robot Pattern

All UI tests go in `androidInstrumentedTest/`. Always use the Robot pattern.

```kotlin
fun exampleRobot(rule: ComposeTestRule, body: ExampleRobot.() -> Unit) =
    ExampleRobot(rule).apply { body() }

class ExampleRobot(val rule: ComposeTestRule) : BaseRobot() {
    fun typeUsername(username: String) {
        rule.waitUntilExactlyOneExists(hasTestTag(USERNAME_TAG), TIMEOUT)
        rule.onNodeWithTag(USERNAME_TAG).performClick()
        rule.onAllNodesWithTag("INPUT_TEXT_FIELD")[0].performTextInput(username)
    }

    fun clickSubmitButton() {
        rule.waitUntilExactlyOneExists(hasTestTag(SUBMIT_TAG), TIMEOUT)
        rule.onNodeWithTag(SUBMIT_TAG).performClick()
    }
}

class ExampleTest : BaseTest() {
    @get:Rule val rule = createComposeRule()

    @Test
    fun shouldPerformSuccessfulAction() {
        mockWebServerRobot.addResponse(GET, "/api/endpoint", MOCK_RESPONSE, 200)
        exampleRobot(rule) {
            typeUsername("user")
            clickSubmitButton()
            verifySuccessMessageDisplayed()
        }
        cleanDatabase()
    }
}
```

## Test Tags

Export constants from the screen file. Format: `{SCREEN}_{COMPONENT}_TAG`.

```kotlin
const val LOGIN_BUTTON_TAG = "LOGIN_BUTTON_TAG"
const val USERNAME_INPUT_TAG = "USERNAME_INPUT_TAG"

@Composable
fun LoginScreen() {
    InputField(modifier = Modifier.testTag(USERNAME_INPUT_TAG))
    Button(modifier = Modifier.testTag(LOGIN_BUTTON_TAG)) { ... }
}
```

## DHIS2 Design System Inputs

DHIS2 input components are composite. Click the wrapper to focus, then target the
inner `"INPUT_TEXT_FIELD"` node. Use `performTextInput()`, never `performTextReplacement()`.

```kotlin
// ✅ CORRECT
rule.onNodeWithTag(USERNAME_TAG).performClick()
rule.onAllNodesWithTag("INPUT_TEXT_FIELD")[0].performTextInput(username)

// ❌ WRONG
rule.onNodeWithTag(USERNAME_TAG).performTextReplacement(username)
```

## Common Mistakes to Avoid

- Using `Thread.sleep()` or any hard-coded delays
- Using MockK (`mockk()`, `every {}`, `coEvery {}`) — use mockito-kotlin instead
- Using `performTextReplacement()` on DHIS2 design system components
- Not exporting test tag constants from screen files
- Not extending `BaseRobot` for robot classes
- Not cleaning up after tests (`cleanDatabase()`, clear preferences)
- Testing implementation details instead of user flows
