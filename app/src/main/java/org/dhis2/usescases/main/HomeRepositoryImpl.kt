package org.dhis2.usescases.main

import dhis2.org.analytics.charts.Charts
import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User

class HomeRepositoryImpl(
    private val d2: D2,
    private val charts: Charts?
) : HomeRepository {
    override fun user(): Single<User> {
        return d2.userModule().user().get()
    }

    override fun defaultCatCombo(): Single<CategoryCombo> {
        return d2.categoryModule().categoryCombos().byIsDefault().eq(true).one().get()
    }

    override fun defaultCatOptCombo(): Single<CategoryOptionCombo> {
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

    override fun getServerVersion(): Single<SystemInfo> {
        return d2.systemInfoModule().systemInfo().get()
    }

    override fun accountsCount() = d2.userModule().accountManager().getAccounts().count()

    override fun isPinStored() = d2.dataStoreModule().localDataStore().value(PIN).blockingExists()
}
