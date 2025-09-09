package org.dhis2.usescases.main

import dhis2.org.analytics.charts.Charts
import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.commons.bindings.dataSet
import org.dhis2.commons.bindings.dataSetInstanceSummaries
import org.dhis2.commons.bindings.isStockProgram
import org.dhis2.commons.bindings.programs
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User

class HomeRepositoryImpl(
    private val d2: D2,
    private val charts: Charts?,
) : HomeRepository {
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

    override fun logOut(): Completable = d2.userModule().logOut()

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
