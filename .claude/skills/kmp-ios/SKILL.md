---
name: kmp-ios
description: >
  Keep KMP code iOS-compliant and add the iOS target (iosArm64, iosSimulatorArm64) to a KMP module
  of the DHIS2 Android Capture App: the jvmCommon source set hierarchy, which commonMain and
  commonTest constructs break Kotlin/Native, per-module readiness (sync, login, aggregates,
  tracker), rewriting Mockito tests as fakes, Koin bindings and dependency verification for iOS,
  and how to compile and test the iOS target locally. Load when adding iOS to a module, when an
  iOS compilation fails, or when unsure whether commonMain code is iOS-safe.
---

# KMP iOS

The project is moving to KMP with iOS. `commonskmm` and `home` already compile for iOS; the other
KMP modules target Android and Desktop only. iOS is not shipped yet: `iosMain` holds stubs, so
modules can compile for it and catch platform leaks early.

## What breaks iOS in `commonMain`

| Construct | Use instead |
|---|---|
| `import java.*` / `javax.*` / `android.*` | kotlinx-datetime, kotlin stdlib, an interface with a platform binding, or `jvmCommonMain` |
| `java.util.Date`, `Calendar`, `SimpleDateFormat` | `kotlinx.datetime` / `kotlin.time.Instant` |
| `java.util.SortedMap`, `.toSortedMap()` | an ordered `Map` built with `sortedBy` / `associate` |
| `java.io.File` | an `expect fun` or a file service interface (`commonskmm` has `getFormattedFileSize`) |
| `String.format`, `"%s".format(x)` | string templates; in Compose `stringResource(Res.string.x, arg)` with `%1$s` |
| `Dispatchers.IO` without `import kotlinx.coroutines.IO` | the injected `Dispatcher` (`commonskmm/.../coroutine/Dispatcher.kt`) |
| `synchronized`, `@Volatile` from `kotlin.jvm`, `Thread` | `Mutex`, `atomicfu`, coroutines |
| `commonskmm` `jvmCommonMain` functions (`fileSizeLabel`, `getDateFromAge`, `hasDateFormat`, …) called from another module's `commonMain` | move the function to `commonskmm` `commonMain` (rewritten without JVM APIs) or add an `expect` |

The last row is the trap: a module **without** iOS sees `commonskmm`'s `jvmCommonMain` from its
`commonMain`, so the call compiles today and breaks the day the module gets iOS.

## What breaks iOS in `commonTest`

`commonTest` also runs on iOS, so in a module with iOS it cannot use Mockito, JUnit
(`org.junit.*`, `koin-test-junit4`'s `KoinTestRule`) or `java.*`.

- Write fakes for repository interfaces and use `kotlin.test` (`@Test`, `@BeforeTest`,
  `assertEquals`). Turbine, `koin-test` and `kotlinx-coroutines-test` work on iOS.
- Tests that need Mockito or a mocked `D2` go in `androidHostTest`. Tests of `jvmCommonMain` code
  go in `jvmCommonTest`.
- Backtick test names cannot contain `,` (Kotlin/Native rejects them).

## Adding iOS to a module

1. In the module's `build.gradle.kts`, next to `jvm("desktop")`, add the block used by
   `commonskmm` and `home` (it is copy-pasted, there is no convention plugin yet):

   ```kotlin
   // at the top of the file, if the module does not import them yet:
   import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
   import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

   iosArm64()
   iosSimulatorArm64()

   // jvmCommonMain holds the JVM-only code shared by android and desktop, so commonMain stays
   // compilable for iOS.
   @OptIn(ExperimentalKotlinGradlePluginApi::class)
   applyDefaultHierarchyTemplate {
       common {
           group("jvmCommon") {
               withJvm()
               withCompilations { it.platformType == KotlinPlatformType.androidJvm }
           }
       }
   }
   ```

   Move `mockitoKotlin` and `koin.test.junit4` from `commonTest.dependencies` to the
   `getByName("androidHostTest") { dependencies { … } }` block (add the block if the module has
   none, as in `aggregates`, or its Mockito tests stop compiling; `home` shows it), and add `-Xexpect-actual-classes` only if the module has `expect class`.
2. Fix the `commonMain` blockers (tables above). Move JVM-only code shared by Android and Desktop to
   `src/jvmCommonMain`.
3. Add an `iosMain` `actual` for every `expect` in the module. Koin bindings on iOS follow Desktop:
   stubs until the SDK is multiplatform. `commonskmm`'s iOS `commonsModule` includes
   `sharedCommonsModule` (no platform API) and `sdklessCommonsModule` (stubs).
4. Move JVM-bound tests out of `commonTest` (or rewrite them with fakes).
5. Verify:
   ```bash
   ./gradlew :<module>:compileKotlinIosSimulatorArm64 --dependency-verification lenient
   ./gradlew :<module>:iosSimulatorArm64Test --dependency-verification lenient   # needs Xcode
   ./gradlew :<module>:desktopTest :<module>:testAndroidHostTest
   ```
   iOS compilation downloads Kotlin/Native toolchains; if the sandbox blocks the download, run it
   outside the sandbox. `--dependency-verification lenient` is needed because
   `gradle/verification-metadata.xml` has no iOS artifacts yet (`designsystem-iosarm64`,
   `paging-compose-iosarm64`, `navigation-compose-uikitarm64`, …). CI does not compile iOS yet.
6. Update the module table in `AGENTS.md` (iOS ✓) and the readiness table below.

## Module readiness (as of 2026-11)

No module uses an Android-only library in `commonMain`: Compose Multiplatform, Koin, navigation,
lifecycle, paging-compose, ktor, kotlinx-datetime, coil3 and the design system all publish iOS.

| Module | `commonMain` blockers | `expect`s needing iOS actuals | `commonTest` files on Mockito/JUnit | Effort |
|---|---|---|---|---|
| `sync` | `model/GranularSyncType.kt` `"…%s".format(name)` | 2 (`platform()`, `syncModule`) | 8 of 8 | Easy, the next candidate |
| `login` | none | 7 (DI modules, `WebAuthenticator`, `WebRecovery`, `FilePicker`, `ServerQRReader`) | 20 of 22 | Easy main code, large test rewrite |
| `aggregates` | `SortedMap` (`GetDataSetSectionIndicators`, `DataSetInstanceRepository`), `java.io.File` and `jvmCommon` calls in `ui/inputs/InputProvider.kt`, `.format` in `ValidationRulesResult` and `DataSetTableScreen`, `Dispatchers.IO` import in `AggregateModule` | 2 (`platformModule`, `getScreenWidth`) | 6 of 6 (one uses `KoinTestRule`) | Medium. Also replace the broken `src/iosMain/.../ScreenWidth.kt` with the desktop version |
| `tracker` | `java.util.Date` in `relationships/model/RelationshipModel.kt` (the change reaches `androidMain` and `app`), `.format` in `RelationshipsScreen` | 1 (`trackerSearchRepositoryModule`) | 8 of 8 | Medium |
