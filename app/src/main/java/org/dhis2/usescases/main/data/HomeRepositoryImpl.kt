package org.dhis2.usescases.main.data

import dhis2.org.analytics.charts.Charts
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataSet
import org.dhis2.commons.bindings.dataSetInstanceSummaries
import org.dhis2.commons.bindings.isStockProgram
import org.dhis2.commons.bindings.programs
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.usescases.main.HomeItemData
import org.dhis2.usescases.settings.deleteCache
import org.dhis2.usescases.sync.WAS_INITIAL_SYNC_DONE
import org.dhis2.utils.TRUE
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramType
import java.io.File

private const val NO_HOME_ITEM = "No home item found"

class HomeRepositoryImpl(
    private val d2: D2,
    private val charts: Charts?,
    private val preferences: PreferenceProvider,
    private val domainErrorMapper: DomainErrorMapper,
    private val dispatcher: Dispatcher,
) : HomeRepository {
    private suspend fun <T> execute(block: suspend () -> T): T =
        withContext(dispatcher.io) {
            try {
                block()
            } catch (d2Error: D2Error) {
                throw domainErrorMapper.mapToDomainError(d2Error)
            }
        }

    override suspend fun user() =
        execute {
            d2.userModule().user().blockingGet()
        }

    override suspend fun setInitialSyncDone() =
        execute {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(WAS_INITIAL_SYNC_DONE)
                .blockingSet(TRUE)
        }

    override suspend fun getInitialSyncDone() =
        execute {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(
                    WAS_INITIAL_SYNC_DONE,
                ).blockingExists()
        }

    override suspend fun isImportedDb(): Boolean =
        execute {
            val serverUrl =
                d2
                    .systemInfoModule()
                    .systemInfo()
                    .blockingGet()
                    ?.contextPath()
            val username =
                d2
                    .userModule()
                    .user()
                    .blockingGet()
                    ?.username()

            d2
                .userModule()
                .accountManager()
                .getCurrentAccount()
                ?.let {
                    it.serverUrl() == serverUrl && it.username() == username && it.importDB() != null
                } ?: false
        }

    override suspend fun logOut() =
        execute {
            d2.userModule().blockingLogOut()
        }

    override suspend fun clearPin() =
        execute {
            preferences.setValue(Preference.SESSION_LOCKED, false)
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN)
                .blockingDeleteIfExist()
        }

    override suspend fun hasHomeAnalytics(): Boolean =
        execute {
            charts?.getVisualizationGroups(null)?.isNotEmpty() == true
        }

    override suspend fun accountsCount() =
        execute {
            d2
                .userModule()
                .accountManager()
                .getAccounts()
                .count()
        }

    override suspend fun isPinStored() =
        execute {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN)
                .blockingExists()
        }

    override suspend fun homeItemCount(): Int =
        execute {
            d2.programs().size + d2.dataSetInstanceSummaries().size
        }

    override suspend fun singleHomeItemData(): HomeItemData =
        execute {
            val program = d2.programs().firstOrNull()
            val dataSetInstance = d2.dataSetInstanceSummaries().firstOrNull()

            require(program != null || dataSetInstance != null) { NO_HOME_ITEM }

            when {
                program?.programType() == ProgramType.WITH_REGISTRATION ->
                    HomeItemData.TrackerProgram(
                        program.uid(),
                        program.displayName() ?: program.uid(),
                        program.access().data().write() == true,
                        program.trackedEntityType()?.uid() ?: "",
                        isStockUseCase = d2.isStockProgram(program.uid()),
                    )

                program?.programType() == ProgramType.WITHOUT_REGISTRATION ->
                    HomeItemData.EventProgram(
                        program.uid(),
                        program.displayName() ?: program.uid(),
                        program.access().data().write() == true,
                    )

                else -> {
                    requireNotNull(dataSetInstance)
                    val dataSet = d2.dataSet(dataSetInstance.dataSetUid())
                    HomeItemData.DataSet(
                        dataSetInstance.dataSetUid(),
                        dataSetInstance.dataSetDisplayName(),
                        dataSet?.access()?.data()?.write() == true,
                    )
                }
            }
        }

    override suspend fun clearCache(cache: File) =
        execute {
            deleteCache(cache)
        }

    override suspend fun clearPreferences() =
        execute {
            preferences.clear()
        }

    override suspend fun wipeAll() =
        execute {
            d2.wipeModule().wipeEverything()
        }

    override suspend fun deleteCurrentAccount() =
        execute {
            d2
                .userModule()
                .accountManager()
                .deleteCurrentAccount()
        }
}
