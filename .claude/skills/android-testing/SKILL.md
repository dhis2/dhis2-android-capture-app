---
name: android-testing
description: >
  Guidelines for writing tests in the DHIS2 Android KMP project — host unit tests
  (commonTest / androidHostTest, mockito-kotlin, Turbine, runTest) and instrumented
  device tests (src/androidTest, Robot pattern, Compose test tags). Routes to
  references/unit-testing.md and references/instrumented-testing.md. Load this when
  creating, fixing, or reviewing any test in the codebase.
---

# DHIS2 Android Testing

Start here, then open the reference you need. This file is the router and the
invariants; the details live in `references/`.

## Which test am I writing?

| What you are testing | Module type | Source set | Gradle task |
| --- | --- | --- | --- |
| Domain logic, use case, ViewModel | KMP | `src/commonTest/kotlin` | `:mod:testAndroidHostTest` |
| Anything touching `org.hisp.dhis.android.core.*` (i.e. mocks `D2`) | KMP | `src/androidHostTest/kotlin` | `:mod:testAndroidHostTest` |
| Anything in `app` | AGP | `src/test/java` | `:app:testDhis2DebugUnitTest` |
| Anything in another AGP module | AGP | `src/test/java` | `:mod:testDebugUnitTest` |
| UI flow on a device | AGP | `src/androidTest/java` | BrowserStack matrix |

KMP modules: `login`, `sync`, `aggregates`, `commonskmm`, `tracker`.
AGP modules: `app`, `form`, `commons`, `compose-table`, `stock-usecase`,
`dhis_android_analytics`, `dhis2_android_maps`, `dhis2-mobile-program-rules`.

Both KMP source sets run under the **same** task — the split is about what the test
can see, not how it is run. The SDK is an `androidMain` dependency, so a test that
mocks `D2` must be in `androidHostTest`; `androidHostTest` depends on `commonTest`,
so `commonTest` dependencies are already on its classpath.

## These do not exist in this repo

Guidance elsewhere on the internet (and older copies of this file) will tell you to
use these. They are wrong **here** — a test placed in one of them is never compiled
and never run, and nothing fails to tell you so:

- `androidUnitTest/` — does not exist. Use `androidHostTest/`.
- `androidInstrumentedTest/` — does not exist. Use `src/androidTest/`.
- `src/desktopTest/` — no module has one, despite the desktop targets.
- `testAndroidDebugUnitTest` — **not a task.** Use `testAndroidHostTest`.

`commonskmm`, `login` and `sync` do declare an `androidDeviceTest` source set, but
no test has been written in one yet.

## Run commands

```bash
# lint + all unit tests (what CI runs)
./run_tests.sh

# all unit tests
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest

# one KMP test class (either source set)
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest"

# one test method
./gradlew :login:testAndroidHostTest --tests "org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModelTest.initial screen is set correctly when starting"

# one AGP module test class
./gradlew :form:testDebugUnitTest --tests "org.dhis2.form.ui.FormViewModelTest"
```

## Confirm your test actually ran

A green build does **not** mean your test ran. A class that was never collected —
wrong source set, wrong annotation — produces no result file at all, and the build
succeeds.

```bash
ls build/test-results/<task>/TEST-<FQCN>.xml
```

Check the file exists and its root element says `tests="N"` with the N you expect.
No file means the test never ran. Do this before reporting a test as passing.

## Invariants

1. **mockito-kotlin only** — `mock()`, `whenever()`, `verify()`. Never MockK.
2. **JUnit4 / `kotlin.test` annotations only.** JUnit Jupiter is excluded from every
   test configuration; an `org.junit.jupiter.api.Test` fails the build.
3. **`D2` must be mocked with `RETURNS_DEEP_STUBS`** — its call chains NPE otherwise.
4. **`DomainError` and `D2Error` need `thenAnswer { throw ... }`**, not `doThrow` —
   mockito rejects them as invalid checked exceptions.
5. **No `Thread.sleep()` or hard-coded delays**, in any test.
6. **ViewModels use `launchUseCase { }`**, not `viewModelScope.launch` — it wraps
   `CoroutineTracker`, which drives Espresso's `IdlingResource`.
7. **Pass one dispatcher everywhere** — `Dispatcher(testDispatcher, testDispatcher,
   testDispatcher)` — and install it with `Dispatchers.setMain`, or the schedulers
   diverge.
8. **Test both SDK error shapes** — the blocking RxJava operators rewrap `D2Error`
   in a `RuntimeException`.
9. **Never resolve a UI string inline in domain/data code** — it cannot resolve in a
   host test. Inject a `*ResourceProvider`.
10. **Assert through the UI in instrumented tests**, never by probing SDK state.

## References

- **[references/unit-testing.md](references/unit-testing.md)** — host (JVM) tests:
  source sets and Turbine availability per module, coroutine/dispatcher setup,
  Turbine and `StateFlow` semantics, mocking, SDK error mapping, `DomainError`
  pitfalls, resource providers, multi-step use cases, and a symptom → cause → fix
  troubleshooting table.
- **[references/instrumented-testing.md](references/instrumented-testing.md)** —
  device tests: Robot pattern, test tags, merged vs unmerged semantics,
  design-system inputs, fixtures and cleanup, and the landscape/CI-matrix rules.

## Keeping this true

If a test problem costs you more than about 30 minutes, add a row to the
troubleshooting table in `references/unit-testing.md` (or the relevant section of
`references/instrumented-testing.md`) **in the same PR that fixes it**. The tables
are the point of this skill; a lesson learned and not written down will be paid for
again.

To check that no document has drifted back to teaching the nonexistent source sets,
run the gate — it allows lines that *deny* the names and flags every other use:

```bash
grep -rn "androidUnitTest\|androidInstrumentedTest\|testAndroidDebugUnitTest" \
  --include="*.md" --include="*.kts" --include="*.yml" . \
  | grep -v "/build/" | grep -v "does not exist\|not a task"
```

The testing guidance lives here and only here. `.github/agents/testing.agent.md` and
the Testing section of `AGENTS.md` are deliberately thin pointers — put new
guidance in this skill, not in those.
