package org.dhis2.usescases.main

import dhis2.org.analytics.charts.Charts
import io.reactivex.Single
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataSet
import org.dhis2.commons.bindings.dataSetInstanceSummaries
import org.dhis2.commons.bindings.isStockProgram
import org.dhis2.commons.bindings.programs
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User

class HomeRepositoryImpl(
    private val d2: D2,
    private val charts: Charts?,
    private val preferences: PreferenceProvider,
    private val cryptographyManager: CryptographicActions,
    private val dispatcher: Dispatcher,
    private val domainErrorMapper: DomainErrorMapper,
) : HomeRepository {
    companion object {
        const val BIOMETRICS_PERMISSION = "biometrics_permission"
    }

    private suspend fun <T> execute(block: suspend () -> Result<T>): Result<T> =
        withContext(dispatcher.io) {
            try {
                block()
            } catch (d2Error: D2Error) {
                Result.failure(domainErrorMapper.mapToDomainError(d2Error))
            }
        }

    override fun user(): Single<User?> = d2.userModule().user().get()

    override fun defaultCatCombo(): Single<CategoryCombo?> =
        d2
            .categoryModule()
            .categoryCombos()
            .byIsDefault()
            .eq(true)
            .one()
            .get()

    override fun defaultCatOptCombo(): Single<CategoryOptionCombo?> =
        d2
            .categoryModule()
            .categoryOptionCombos()
            .byCode()
            .eq(DEFAULT)
            .one()
            .get()

    override suspend fun logOut(): Result<Unit> =
        execute {
            Result.success(d2.userModule().blockingLogOut())
        }

    override suspend fun clearSessionLock(): Result<Unit> =
        execute {
            preferences.setValue(Preference.SESSION_LOCKED, false)
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN)
                .blockingDeleteIfExist()
            Result.success(Unit)
        }

    override fun hasProgramWithAssignment(): Boolean =
        if (d2.userModule().isLogged().blockingGet()) {
            !d2
                .programModule()
                .programStages()
                .byEnableUserAssignment()
                .isTrue
                .blockingIsEmpty()
        } else {
            false
        }

    override fun checkDeleteBiometricsPermission() {
        val hasLessThanTwoAccounts =
            d2
                .userModule()
                .accountManager()
                .getAccounts()
                .count() <= 2
        if (hasLessThanTwoAccounts) {
            preferences.removeValue(BIOMETRICS_PERMISSION)
            cryptographyManager.deleteInvalidKey()
        }
    }

    override fun hasHomeAnalytics(): Boolean = charts?.getVisualizationGroups(null)?.isNotEmpty() == true

    override fun getServerVersion(): Single<SystemInfo?> = d2.systemInfoModule().systemInfo().get()

    override fun accountsCount() =
        d2
            .userModule()
            .accountManager()
            .getAccounts()
            .count()

    override fun isPinStored() =
        d2
            .dataStoreModule()
            .localDataStore()
            .value(PIN)
            .blockingExists()

    override fun homeItemCount(): Int = d2.programs().size + d2.dataSetInstanceSummaries().size

    override suspend fun singleHomeItemData(): HomeItemData? {
        val program = d2.programs().firstOrNull()
        val dataSetInstance = d2.dataSetInstanceSummaries().firstOrNull()

        return when {
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

            dataSetInstance != null -> {
                val dataSet = d2.dataSet(dataSetInstance.dataSetUid())
                HomeItemData.DataSet(
                    dataSetInstance.dataSetUid(),
                    dataSetInstance.dataSetDisplayName(),
                    dataSet?.access()?.data()?.write() == true,
                )
            }

            else -> null
        }
    }
}
