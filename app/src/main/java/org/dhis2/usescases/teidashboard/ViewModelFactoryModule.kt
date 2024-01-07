package org.dhis2.usescases.teidashboard

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@Module
class ViewModelFactoryModule (
        val teiUid: String?,
        val programUid: String?,
        private val enrollmentUid: String?
) {
    @Provides
    @PerActivity
    fun dashboardRepository(
            d2: D2,
            charts: Charts,
            resources: ResourceManager,
            teiAttributesProvider: TeiAttributesProvider,
    ): DashboardRepository {
        return DashboardRepositoryImpl(
                d2,
                charts,
                teiUid,
                programUid,
                enrollmentUid,
                resources,
                teiAttributesProvider,
        )
    }

    @Provides
    @PerActivity
    fun teiAttributesProvider(d2: D2): TeiAttributesProvider {
        return TeiAttributesProvider(d2)
    }
    @Provides
    @PerActivity
    fun providesViewModelFactory(
            repository: DashboardRepository,
            analyticsHelper: AnalyticsHelper,
    ): DashboardViewModelFactory {
        return DashboardViewModelFactory(repository, analyticsHelper)
    }
}