package org.dhis2.usescases.main

import dhis2.org.analytics.charts.Charts
import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.commons.bindings.dataSet
import org.dhis2.commons.bindings.dataSetInstanceSummaries
import org.dhis2.commons.bindings.isStockProgram
import org.dhis2.commons.bindings.programs
import org.dhis2.commons.bindings.stockUseCase
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.usescases.main.program.toAppConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User

class HomeRepositoryImpl(
    private val d2: D2,
    private val charts: Charts?,
    private val featureConfig: FeatureConfigRepository,
) : HomeRepository {
    override fun user(): Single<User?> {
        return d2.userModule().user().get()
    }

    override fun defaultCatCombo(): Single<CategoryCombo?> {
        return d2.categoryModule().categoryCombos().byIsDefault().eq(true).one().get()
    }

    override fun defaultCatOptCombo(): Single<CategoryOptionCombo?> {
        return d2
            .categoryModule()
            .categoryOptionCombos().byCode().eq(DEFAULT).one().get()
    }

    override fun logOut(): Completable {
        return d2.userModule().logOut()
    }

    override fun hasProgramWithAssignment(): Boolean {
        return if (d2.userModule().isLogged().blockingGet()) {
            !d2.programModule().programStages().byEnableUserAssignment()
                .isTrue.blockingIsEmpty()
        } else {
            false
        }
    }

    override fun hasHomeAnalytics(): Boolean {
        return charts?.getHomeVisualizations(null)?.isNotEmpty() == true
    }

    override fun getServerVersion(): Single<SystemInfo?> {
        return d2.systemInfoModule().systemInfo().get()
    }

    override fun accountsCount() = d2.userModule().accountManager().getAccounts().count()

    override fun isPinStored() = d2.dataStoreModule().localDataStore().value(PIN).blockingExists()
    override fun homeItemCount(): Int {
        return d2.programs().size + d2.dataSetInstanceSummaries().size
    }

    override fun singleHomeItemData(): HomeItemData? {
        val program = d2.programs().firstOrNull()
        val dataSetInstance = d2.dataSetInstanceSummaries().firstOrNull()

        return when {
            program?.programType() == ProgramType.WITH_REGISTRATION ->
                HomeItemData.TrackerProgram(
                    program.uid(),
                    program.displayName() ?: program.uid(),
                    program.access().data().write() == true,
                    program.trackedEntityType()?.uid() ?: "",
                    stockConfig = if (d2.isStockProgram(program.uid())) {
                        d2.stockUseCase(program.uid())?.toAppConfig()
                    } else {
                        null
                    },
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
