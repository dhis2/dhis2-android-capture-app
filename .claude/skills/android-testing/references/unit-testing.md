# Host (JVM) unit tests

Everything here runs on the JVM, with no device and no Robolectric. For device
tests see [instrumented-testing.md](instrumented-testing.md).

---

## 1. Where a unit test lives

| Module | Source set | Gradle task |
| --- | --- | --- |
| KMP (`login`, `sync`, `aggregates`, `commonskmm`, `tracker`) — pure logic | `src/commonTest/kotlin` | `:mod:testAndroidHostTest` |
| KMP — anything touching `org.hisp.dhis.android.core.*` | `src/androidHostTest/kotlin` | `:mod:testAndroidHostTest` |
| `app` | `src/test/java` | `:app:testDhis2DebugUnitTest` |
| Other AGP modules (`form`, `commons`, `compose-table`, `stock-usecase`, `dhis_android_analytics`, `dhis2_android_maps`, `dhis2-mobile-program-rules`) | `src/test/java` | `:mod:testDebugUnitTest` |

Both KMP source sets are driven by the **same** task. `androidHostTest` depends on
`commonTest`, so a dependency declared in `commonTest` is already on the
`androidHostTest` compile classpath — you do not need to declare it twice.
(`tracker` declares Turbine in both; that is redundant, not a requirement.)

The split is about **what the test can see**, not which task runs it: the DHIS2 SDK
is an `androidMain` dependency, so a test that mocks `D2` must live in
`androidHostTest`. Pure domain and ViewModel tests belong in `commonTest`.

**Turbine is not available everywhere.** Verified per module:

| Module | Turbine declared in | Usable from `commonTest`? |
| --- | --- | --- |
| `login` | `commonTest` | yes |
| `aggregates` | `commonTest` | yes |
| `tracker` | `commonTest` + `androidHostTest` | yes |
| `sync` | `androidHostTest` **only** | **no** — add `implementation(libs.test.turbine)` to `commonTest` |
| `commonskmm` | nowhere | **no** — add it to the source set you need |

## 1b. Canonical files to copy from

Rather than reproduce skeletons that drift, start from a real test in this repo:

| Kind | File |
| --- | --- |
| Use case | `login/src/commonTest/.../pin/domain/usecase/ValidatePinUseCaseTest.kt` |
| ViewModel (Turbine + `StateFlow`) | `login/src/commonTest/.../main/ui/viewmodel/LoginViewModelTest.kt` |
| Repository mocking `D2` | `login/src/androidHostTest/.../main/data/LoginRepositoryImplTest.kt` |
| JUnit4 `@get:Rule` in `commonTest` | `aggregates/src/commonTest/.../GetDataValueInputTest.kt` |

The repository one is the densest: it shows `setMain`/`resetMain`, the `Dispatcher`
triple, `RETURNS_DEEP_STUBS` and `thenAnswer { throw ... }` together.

## 2. Framework: JUnit4 + kotlin.test

Prefer `kotlin.test` annotations (`@Test`, `@BeforeTest`, `@AfterTest`,
`assertEquals`, `assertTrue`, `assertFailsWith`) — they compile in `commonTest` and
`androidHostTest` alike. JUnit4 annotations (`org.junit.Test`, `@Before`, `@Rule`)
are accepted, and are required when you need a `@get:Rule` (see `KoinTestRule` in
`aggregates/src/commonTest/.../GetDataValueInputTest.kt`).

**JUnit Jupiter is banned.** `org.junit.jupiter` is excluded from every test
configuration in the root `build.gradle.kts`, and `useJUnitPlatform()` is set
nowhere. Before that exclusion existed, a Jupiter `@Test` was **silently never
collected** — the class compiled, the build went green, and the test never ran.
Now it fails the build instead. Use `kotlin.test.assertFailsWith<T> { }`, never
`org.junit.jupiter.api.assertThrows`.

## 3. Coroutines

**Canonical file to copy from:**
`login/src/androidHostTest/kotlin/org/dhis2/mobile/login/main/data/LoginRepositoryImplTest.kt`
— it shows `setMain`/`resetMain`, the `Dispatcher` triple, `RETURNS_DEEP_STUBS`, and
`thenAnswer { throw ... }` in one place.

A `TestDispatcher` held as a class field does **not** share `runTest`'s
`TestCoroutineScheduler`. Symptom:

```
IllegalStateException: Detected use of different schedulers
```

Two fixes, either is fine:

```kotlin
// A — install the field dispatcher as Main; runTest then reuses its scheduler
private val testDispatcher = StandardTestDispatcher()

@BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
@AfterTest  fun tearDown() { Dispatchers.resetMain() }

// B — build the dispatcher inside runTest, from the test's own scheduler
runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
}
```

- `StandardTestDispatcher` queues work — you must `advanceUntilIdle()` or
  `runCurrent()` to let it run. Use it when ordering matters.
- `UnconfinedTestDispatcher` runs eagerly at the launch point. Use it when you just
  want the coroutine to have finished by the next line.

KMP classes take `org.dhis2.mobile.commons.coroutine.Dispatcher`, a data class of
three dispatchers (`io`, `main`, `default`). In a test, pass the same test
dispatcher for all three so everything shares one clock:

```kotlin
dispatcher = Dispatcher(testDispatcher, testDispatcher, testDispatcher)
```

## 4. Turbine and `StateFlow`

`StateFlow` replays its current value to every new collector, so **the first
`awaitItem()` is the state at subscription time**, not the first change. If you
subscribe after the work has already run, that first item is also the last one.

```kotlin
viewModel.uiState.test {
    assertEquals(UiState.Loading, awaitItem())   // replayed initial value
    assertEquals(UiState.Success(data), awaitItem())
    cancelAndIgnoreRemainingEvents()
}
```

- `StateFlow` **conflates**: intermediate states can be dropped if the producer
  outruns the collector. Don't assert on a state the flow only passes through.
- `expectMostRecentItem()` when you only care about where it settled.
- `cancelAndIgnoreRemainingEvents()` at the end of a block that doesn't drain.
- Check §1 before reaching for Turbine — two modules don't have it yet.

## 5. Mocking: mockito-kotlin only

`mock()`, `whenever()`, `verify()` from `org.mockito.kotlin`. **Never MockK** —
no `mockk()`, `every {}`, `coEvery {}`.

**`D2` needs `RETURNS_DEEP_STUBS`.** Its API is a chain of intermediate objects
(`d2.userModule().accountManager().getAccounts()`), and a plain mock returns `null`
at the first link, so the chain NPEs before your stub is reached.

```kotlin
private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
```

**`doThrow` fails on `DomainError`.** `DomainError` is declared
`sealed class DomainError : Throwable()`. Mockito checks a throwable against the
method's declared checked exceptions and rejects it:

```
Checked exception is invalid for this method
```

Stub with `thenAnswer` instead — it bypasses that check:

```kotlin
// ❌ fails at runtime
whenever(repository.logout()).doThrow(domainError)

// ✅
whenever(repository.logout()).thenAnswer { throw domainError }
```

The same applies to `D2Error`, which is also a checked exception.

## 6. SDK errors: two shapes, test both

The SDK's blocking RxJava operators (`blockingGet()`, `blockingAdd()`, …) rewrap
the checked `D2Error` in a `RuntimeException`. So an inline
`catch (d2Error: D2Error)` in a repository **misses the blocking-call case
entirely** — the error sails past the mapper and reaches the ViewModel unmapped.

Map with `withDomainErrors { }` / `withDomainErrorsAsResult { }` from
`commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/error/DomainErrorMapperExtensions.kt`.
They unwrap the cause chain (`Throwable.asD2Error()` walks causes up to a depth of
5, with a self-reference guard) before mapping, so both shapes are handled.

They are extensions on `DomainErrorMapper`, so the call reads
`domainErrorMapper.withDomainErrors { ... }`. `SyncDataSetRepositoryImpl` is a
worked example.

Whichever you use, a repository test must cover **both** shapes:

```kotlin
whenever(...).thenAnswer { throw d2Error }                        // direct
whenever(...).thenAnswer { throw RuntimeException(d2Error) }      // blocking-wrapped
```

A test that only covers the direct shape will pass against a repository that is
broken for every blocking call it makes.

## 7. `DomainError` in production code: `catch (e: Exception)` does not catch it

Because `DomainError` extends `Throwable` rather than `Exception`, this is a silent
hole:

```kotlin
try {
    sessionRepository.logout()          // throws DomainError
    Result.success(Unit)
} catch (e: Exception) {                // ← does not match DomainError
    Result.failure(e)
}
```

The error escapes the `try` entirely and propagates to the caller, which was
promised a `Result`. Catch `Throwable` (rethrowing `CancellationException`) when a
`DomainError` can reach the block. `ForgotPinUseCase` and `SavePinUseCase` in
`login` currently have this shape — treat them as the counter-example, not the
pattern.

## 8. Compose resources cannot resolve in a host test

There is no Robolectric in this project's host tests, so `getString` reaches
`Resources.getSystem()` and a `Res.string.*` lookup has nothing to resolve against.
This is a **design rule, not a test trick**:

> A branch you need to test must not resolve a UI string inline.

Put the string behind an injectable provider, which the test then mocks:
`D2ErrorMessageProvider`, `StringResourceProvider` (both
`commonskmm/src/commonMain/.../resources/`), `CredentialsResourceProvider`
(`login/src/commonMain/.../ui/provider/`).

Review heuristic: **`getString(` under `domain/` or `data/` is a smell.** If you hit
an unresolved-resource failure while testing, the fix is usually in the production
class, not the test.

## 9. Multi-step use cases: one test per failing step

When a repository reports failure as a returned `Result` rather than by throwing, a
use case that inspects only its **own** final `Result` silently swallows the
intermediate failures:

```kotlin
override suspend fun invoke(input: Unit): Result<Unit> = try {
    repo.stepA()      // returns Result — failure ignored
    repo.stepB()      // returns Result — failure ignored
    Result.success(Unit)
} catch (e: Exception) { Result.failure(e) }
```

Every step "succeeds" as far as the caller can tell. Write **one test per step**,
each stubbing that step to fail and asserting the use case surfaces it — a single
happy-path test plus a single "repository throws" test will not catch this.

## 10. Adding tests, naming, and what not to test

**First test in a module?** Check the module's `build.gradle.kts` has the test
dependencies for the source set you're using — `kotlin("test")`,
`libs.test.kotlinCoroutines`, `libs.test.mockitoKotlin`, and `libs.test.turbine` if
you assert on flows (§1). Copy the block from `login/build.gradle.kts`.

**Naming.** New tests use backticked `GIVEN … WHEN … THEN …`:

```kotlin
@Test
fun `GIVEN a stored pin WHEN validating a wrong pin THEN result is failure`() = runTest {
```

**What not to unit test:** composables (no Robolectric — those are instrumented
tests), generated code, Koin module wiring, and plain data-class accessors. A test
that only re-states a mock's stub verifies nothing.

## 11. Troubleshooting

| Symptom | Cause | Fix |
| --- | --- | --- |
| `IllegalStateException: Detected use of different schedulers` | field `TestDispatcher` doesn't share `runTest`'s scheduler | `Dispatchers.setMain(testDispatcher)` in `@BeforeTest`, or build the dispatcher from `testScheduler` inside `runTest` (§3) |
| No `TEST-*.xml` in `build/test-results/<task>/`, build still green | class was never collected — usually a Jupiter `@Test`, or the file is in a source set the task doesn't own | check the annotation import and the source set (§1, §2) |
| `Checked exception is invalid for this method` | `doThrow` with `DomainError` / `D2Error` | `whenever(...).thenAnswer { throw error }` (§5) |
| NPE partway through `d2.x().y().z()` | `D2` mocked without deep stubs | `Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)` (§5) |
| `awaitItem()` times out | flow never emits — a dispatcher never ran (`StandardTestDispatcher` without `advanceUntilIdle()`), or the collector subscribed to the wrong flow | `advanceUntilIdle()`, or switch to `UnconfinedTestDispatcher` (§3) |
| First `awaitItem()` is the *old* state | `StateFlow` replays its current value at subscription | expect the initial value first, or use `expectMostRecentItem()` (§4) |
| Missing-resource error resolving `Res.string.*` | UI string resolved inline in domain/data code | inject a `*ResourceProvider` and mock it (§8) |
| Mapper never called for an SDK failure | the SDK's blocking operators wrapped `D2Error` in `RuntimeException` | map via `withDomainErrors`; test the wrapped shape too (§6) |
