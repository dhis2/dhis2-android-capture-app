package org.dhis2.usescases.main

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.filter.FilterRepository
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.FiltersAdapter
import org.hisp.dhis.android.core.D2

@Module
class MainModule(val view: MainView) {

    @Provides
    @PerActivity
    fun homePresenter(
        d2: D2,
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
        workManagerController: WorkManagerController,
        filterManager: FilterManager,
        filterRepository: FilterRepository,
        matomoAnalyticsController: MatomoAnalyticsController
    ): MainPresenter {
        return MainPresenter(
            view,
            d2,
            schedulerProvider,
            preferences,
            workManagerController,
            filterManager,
            filterRepository,
            matomoAnalyticsController
        )
    }

    @Provides
    @PerActivity
    fun providesNewFilterAdapter(): FiltersAdapter {
        return FiltersAdapter()
    }
}
