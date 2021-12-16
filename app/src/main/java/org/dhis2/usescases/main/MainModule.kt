package org.dhis2.usescases.main

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.filter.FilterRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.FiltersAdapter
import org.hisp.dhis.android.core.D2

@Module
class MainModule(val view: MainView) {

    @Provides
    @PerActivity
    fun homePresenter(
        homeRepository: HomeRepository,
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
        workManagerController: WorkManagerController,
        filterManager: FilterManager,
        filterRepository: FilterRepository,
        matomoAnalyticsController: MatomoAnalyticsController
    ): MainPresenter {
        return MainPresenter(
            view,
            homeRepository,
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
    fun provideHomeRepository(d2: D2, charts: Charts?): HomeRepository {
        return HomeRepositoryImpl(d2, charts)
    }

    @Provides
    @PerActivity
    fun providePageConfigurator(
        homeRepository: HomeRepository,
        featureConfigRepository: FeatureConfigRepository
    ): NavigationPageConfigurator {
        return HomePageConfigurator(homeRepository, featureConfigRepository)
    }

    @Provides
    @PerActivity
    fun providesNewFilterAdapter(): FiltersAdapter {
        return FiltersAdapter()
    }
}
