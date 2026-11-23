---
name: kmp-architecture
description: >
  Architecture patterns for production code in the DHIS2 Android Capture App: layer structure,
  UseCase, ViewModel and Repository patterns, which DHIS2 SDK suspend/Flow API replaces each
  blocking or Rx call (and the few with no suspend version), where dispatchers belong, mapping SDK
  errors to DomainError, Koin DI, platform code for Android/Desktop/iOS, and a new-feature
  checklist. Load before writing, migrating or reviewing any non-test Kotlin code in this repo.
---

# KMP architecture patterns

The agreements in `AGENTS.md` are the rules; this skill shows how to follow them. When existing
code disagrees with this skill, follow the skill for new code and leave the old code alone unless
the task is about it.

## Layer structure (per feature module)

```
domain/
  model/          # Pure data classes / sealed types. No SDK types.
  usecase/        # Business logic, implements UseCase<R, T>
data/             # Repository interfaces (commonMain); implementations per target
                  # (androidMain uses the SDK, desktopMain / iosMain are stubs or fakes)
ui/
  state/          # UiState and other UI models
  viewmodel/      # ViewModels exposing StateFlow<UiState>
  screen/         # @Composable screens
  component/      # Reusable composables
di/               # Koin modules
```

Data flows one way: ViewModel → use case → repository interface → platform implementation → SDK.
Only the repository implementation knows about `D2`, `D2Error` and dispatchers for I/O.

The app is offline-first: data comes from the SDK's local database, and the SDK does all network
and database access, so new code never opens either directly. No new top-level `model/` packages:
domain models go in `domain/model/`, UI models in `ui/state/`.

## Repository

The repository implementation owns three things: **the dispatcher**, **the SDK call**, and
**error mapping**. Every public function is main-safe.

```kotlin
internal class ProgramRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
    private val dispatcher: Dispatcher, // org.dhis2.mobile.commons.coroutine.Dispatcher
) : ProgramRepository {
    override suspend fun getProgramType(programUid: String): ProgramType =
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                d2.programModule().programs().uid(programUid).suspendGet()?.toDomain()
                    ?: ProgramType.None
            }
        }

    override fun observeUploads(): Flow<SyncProgress> =
        d2.eventModule().events().flowUpload()
            .map { it.toDomain() }
            .catch { error -> throw error.asD2Error()?.let { domainErrorMapper.mapToDomainError(it) } ?: error }
            .flowOn(dispatcher.io)
}
```

- `withDomainErrors { }` throws the mapped `DomainError`; `withDomainErrorsAsResult { }` is for a
  block that already returns `Result<T>` and turns SDK failures into `Result.failure(DomainError)`.
  Both live in `commonskmm/src/androidMain/kotlin/org/dhis2/mobile/commons/error/DomainErrorMapperExtensions.kt`
  and are **androidMain only**, like `DomainErrorMapper` (the SDK is an Android dependency).
- When a repository has many functions, wrap the two steps in one private helper instead of
  repeating them. `AndroidSyncRepository.execute { }`
  (`sync/src/androidMain/kotlin/org/dhis2/mobile/sync/data/AndroidSyncRepository.kt`) is the
  existing example:
  ```kotlin
  private suspend fun <T> execute(block: suspend () -> Result<T>): Result<T> =
      withContext(dispatcher.io) { domainErrorMapper.withDomainErrorsAsResult(block) }
  ```
- The SDK sometimes returns its own `Result<T, D2Error>` (login and 2FA handlers). Map its failure
  with `domainErrorMapper.mapToDomainError(error)` in the repository; never return it upward.
- Never `catch (e: D2Error)` inline: blocking SDK calls wrap `D2Error` in a `RuntimeException`, so
  the inline catch silently misses them. The wrappers walk the cause chain (`Throwable.asD2Error()`).
- `SyncProgramRepositoryImpl` in `sync` shows the right SDK and error calls but does not switch to
  `dispatcher.io`: do not copy that omission.

### AGP modules (app, form, stock-usecase, …)

The same rules apply. They all see `commonskmm` (`commons` re-exports it with `api`), so inject
`DomainErrorMapper` and `Dispatcher` from Koin. In Dagger-built classes, take them from the Koin
instance rather than adding another `@Provides` for them. Code that is still RxJava or Java and
cannot call a suspend mapper: leave it as it is unless the task is to migrate it, and do not add new
inline `D2Error` handling to it.

## DHIS2 SDK: suspend and Flow APIs

Every SDK operation exists as `suspendX()` (the real implementation), `rxX()`, `blockingX()`
(`runBlockingOnIO { suspendX() }`) and a deprecated `X()`. New code calls the suspend or Flow form.

| Instead of | Use |
|---|---|
| `.blockingGet()`, `.get()`, `.rxGet()` | `.suspendGet()` (collections → `List`, object repos → `M?`) |
| `.blockingGetUids()` | `.suspendGetUids()` |
| `.blockingCount()` / `.blockingIsEmpty()` | `.suspendCount()` / `.suspendIsEmpty()` |
| `.blockingExists()` | `.suspendExists()` |
| `.blockingAdd(o)` | `.suspendAdd(o)` |
| `.blockingSet(value)` (data values, attribute values, local datastore) | `.suspendSet(value)` |
| `.blockingDelete()` / `.blockingDeleteIfExist()` | `.suspendDelete()` / `.suspendDeleteIfExist()` |
| `.blockingDownload()` on event / TEI / aggregated / file resource / datastore downloaders | `.flowDownload()` (collect it) |
| `.blockingUpload()` | `.flowUpload()` (collect it) |
| paging with Rx | `.getPagingData(pageSize)` |
| `service.blockingX(...)` | `service.suspendX(...)`: `DataSetInstanceService`, `EventService`, `EnrollmentService`, `OrganisationUnitService`, `CategoryOptionComboService.suspendHasAccess`, `OptionService.suspendSearchForOptions`, `ValidationEngine.suspendValidate`, analytics `suspendEvaluate`, `TrackedEntityAttributeReservedValueManager.suspendGetValue/suspendCount` (downloads: `flowDownloadReservedValues`), `OwnershipManager`, `UserModule.suspendLogIn/LogOut/IsLogged` |

**No suspend version yet.** Call the blocking form inside `withContext(dispatcher.io)` and the
error wrapper, and do not switch to its Rx form:

- Field setters on object repositories: `EnrollmentObjectRepository.set*`,
  `EventObjectRepository.set*`, `TrackedEntityInstanceObjectRepository.setOrganisationUnitUid/setGeometry`,
  `DataValueObjectRepository.setFollowUp/setComment` (they are already blocking `Unit` functions).
- Metadata sync: `d2.metadataModule().blockingDownload()`.
- `TrackedEntityInstanceService.blockingInheritAttributes`, `DataSetIndicatorEngine.blockingEvaluate`,
  `CategoryOptionComboService.blockingHasWriteAccess`, `RelationshipCollectionRepository.getByItem`,
  `EventCollectionRepository.countTrackedEntityInstances`.
- `OpenIDConnectHandler` / `OAuth2Handler` `blockingLogIn`, `blockingHandleLogInResponse`,
  `blockingBuildEnrollmentUrl`, `blockingHandleEnrollmentResponse`, `blockingBuildLogoutUrl`
  (the `suspendSetPin`/`suspendChangePin` ones do exist).
- `WipeModule.wipeEverything/wipeData/wipeMetadata`.
- `DHISVersionManager.isVersion/getPatchVersion/getSmsVersion/getBypassVersion` (plain functions;
  `getVersion`, `isGreaterThan` and `isGreaterOrEqualThan` have `suspend` forms).

`scripts/check_conventions.py` flags every `.blockingX(` call, so end each of these lines with
`// conventions:allow <reason>` (for example `// conventions:allow no suspend metadata download`).
If you find another operation with no suspend form, check the SDK sources before using the
blocking one, and add it to this list in the same PR. Re-check the list whenever `dhis2sdk` is
bumped: drop the entries that gained a suspend form.

`suspendGet` relies on the SDK's database dispatcher but is not guaranteed main-safe, so keep the
`withContext(dispatcher.io)` around it.

## Dispatchers

| Layer | Dispatcher |
|---|---|
| Repository implementation | `withContext(dispatcher.io)` around every function body; `flowOn(dispatcher.io)` at the end of a returned Flow |
| Use case | None. `withContext(dispatcher.default)` only for CPU-heavy work it does itself (big sorts, mapping thousands of rows) |
| ViewModel | None. `launchUseCase { }` runs on Main, where state updates belong. `launchUseCase(dispatcher.default) { }` only for heavy in-ViewModel computation |

Why the repository: it is the only layer that knows a call blocks, so making it main-safe there
means no caller can get it wrong, and use cases can compose repositories freely. Always inject
`org.dhis2.mobile.commons.coroutine.Dispatcher` (it already handles `Dispatchers.IO` on iOS); tests
replace it with `Dispatcher(io = testDispatcher, main = testDispatcher, default = testDispatcher)`.
Do not use `Dispatchers.*`, `commons`' `DispatcherProvider` or `aggregates`' `Dispatcher` in new code.

## Use case

```kotlin
// commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/UseCase.kt
fun interface UseCase<in R, out T> {
    suspend operator fun invoke(input: R): Result<T>
}
suspend operator fun <T> UseCase<Unit, T>.invoke() = this(Unit)
```

```kotlin
class SavePin(private val repository: SessionRepository) : UseCase<String, Unit> {
    override suspend fun invoke(input: String): Result<Unit> =
        try {
            repository.savePin(input)
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
}
```

- Name as an action with no `UseCase` suffix. Rename an existing `…UseCase` class only when the task
  is about it.
- Catch order: rethrow `CancellationException`, then `Exception`. `DomainError` extends
  `Exception`, so mapped SDK errors are covered.
- A separate `catch (error: DomainError)` before `Exception` only when domain errors need their
  own handling (for example to signal network loss or session renewal). `SyncData` does this but
  does not rethrow `CancellationException` first: do not copy its catch block. To react to one error and
  otherwise pass it on, check inside the `Exception` catch, as the sync use cases do with
  `sessionRenewalNotifier.notifyIfRequired(error)`.
- Catching only `DomainError` (letting other exceptions escape) breaks "use cases never throw".
  Many existing use cases do it; do not copy them.
- Never `runCatching` (catches `CancellationException`) and never `catch (Throwable)` (turns JVM
  `Error`s into a `Result`). A plain `catch (Exception)` without the cancellation rethrow swallows
  cancellation too.
- A use case is plain Kotlin in `commonMain`: no Android, JVM or SDK types.

## ViewModel

```kotlin
class PinViewModel(
    private val savePin: SavePin,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PinUiState>(PinUiState.Idle)
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    fun onPinEntered(pin: String) = launchUseCase {
        _uiState.value = savePin(pin).fold(
            onSuccess = { PinUiState.Saved },
            onFailure = { PinUiState.Error(it.message) },
        )
    }
}
```

- `launchUseCase` (`commonskmm/.../extensions/ViewModelExtensions.kt`) wraps `CoroutineTracker`,
  which Espresso's idling resource waits on. `viewModelScope.launch` makes UI tests flaky. Leave
  existing `viewModelScope.launch` sites as they are unless the task is to migrate them.
- Show errors from `DomainError.message`: it is already localised by `DomainErrorMapper`. Do not
  parse SDK errors in the UI (`D2ErrorUtils`, `ResourceManager.parseD2Error` are legacy).
- Collect state in composables with `collectAsState()`.

## Dependency injection (Koin)

```kotlin
val featureModule = module {
    factory { SavePin(get()) }
    viewModel { PinViewModel(get()) }
}

// commonMain
expect val featureDataModule: Module

// androidMain
actual val featureDataModule = module {
    single<SessionRepository> { SessionRepositoryImpl(get(), get(), get()) }
}

// desktopMain (and iosMain)
actual val featureDataModule = module {
    single<SessionRepository> { StubSessionRepository() }
}
```

Define modules in `commonMain` where possible; platform bindings per target. Inject ViewModels in
composables with `koinViewModel()`. `DomainErrorMapper` and `Dispatcher` already come from the
`commonskmm` Koin modules.

## Platform code

- **Interface + one binding per platform** for repositories and platform services with state or
  dependencies (biometrics, network status, files, error messages). This lets the repository
  implementations move to `commonMain` unchanged once the SDK is multiplatform, and lets
  `commonTest` use fakes.
- **`expect`/`actual`** only for small dependency-free functions and Compose helpers tests never
  replace (`logDebug`, `getWindowSizeClass`, `getDrawableResource`), and for `expect val` Koin
  modules that hold the per-platform bindings (see Dependency injection). No new `expect class`.
- Code that needs JVM APIs and is shared by Android and Desktop goes in `jvmCommonMain` in modules
  with iOS. In modules without iOS do not call `commonskmm`'s `jvmCommonMain` functions from
  `commonMain`: that compiles today and breaks the day the module gets iOS.

## UI

- DHIS2 design system components (`org.hisp.dhis.mobile.ui.designsystem.*`) over Material;
  screens wrapped in `DHIS2Theme { }`.
- Strings and images in `src/commonMain/composeResources/`; format with
  `stringResource(Res.string.x, arg)` and `%1$s` placeholders, never `String.format`.
- Navigation in KMP modules: `org.jetbrains.androidx.navigation:navigation-compose`.
- Add `@Preview`s, and export test tag constants from the screen file
  (`{SCREEN}_{COMPONENT}_TAG`).

## New feature checklist

1. Domain model in `domain/model/` (no SDK types).
2. Repository interface in `commonMain` `data/`.
3. Android implementation in `androidMain`: suspend SDK APIs, `withContext(dispatcher.io)`,
   `withDomainErrors`.
4. Desktop stub in `desktopMain`, and `iosMain` when the module has iOS.
5. Use case in `domain/usecase/`, action name, `UseCase<R, T>`, catch order as above.
6. Koin bindings: common module plus one data module per platform.
7. UiState in `ui/state/`, ViewModel with `launchUseCase` and `StateFlow`.
8. Screen with design system components, test tags, previews.
9. Tests (`android-testing` skill): use case and ViewModel in `commonTest`, repository with a deep
   stubbed `D2` in `androidHostTest`.
10. The checks and test tasks under Commands in `AGENTS.md`.
