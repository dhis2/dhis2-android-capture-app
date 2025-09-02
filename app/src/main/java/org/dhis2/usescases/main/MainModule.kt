package org.dhis2.usescases.main

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.login.SyncIsPerformedInteractor
import org.dhis2.usescases.settings.DeleteUserData
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.D2

@Module
class MainModule(
    val view: MainView,
    private val forceToNotSynced: Boolean,
) {
    @Provides
    @PerActivity
    fun homePresenter(
        homeRepository: HomeRepository,
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
        workManagerController: WorkManagerController,
        filterManager: FilterManager,
        filterRepository: FilterRepository,
        matomoAnalyticsController: MatomoAnalyticsController,
        userManager: UserManager,
        deleteUserData: DeleteUserData,
        syncIsPerformedInteractor: SyncIsPerformedInteractor,
        syncStatusController: SyncStatusController,
        versionRepository: VersionRepository,
        dispatcherProvider: DispatcherProvider,
    ): MainPresenter =
        MainPresenter(
            view,
            homeRepository,
            schedulerProvider,
            preferences,
            workManagerController,
            filterManager,
            filterRepository,
            matomoAnalyticsController,
            userManager,
            deleteUserData,
            syncIsPerformedInteractor,
            syncStatusController,
            versionRepository,
            dispatcherProvider,
            forceToNotSynced,
        )

    @Provides
    @PerActivity
    fun provideSyncIsPerfomedInteractor(userManager: UserManager): SyncIsPerformedInteractor = SyncIsPerformedInteractor(userManager)

    @Provides
    @PerActivity
    fun provideHomeRepository(
        d2: D2,
        charts: Charts?,
    ): HomeRepository = HomeRepositoryImpl(d2, charts)

    @Provides
    @PerActivity
    fun provideNewFiltersAdapter(): FiltersAdapter = FiltersAdapter()

    @Provides
    @PerActivity
    fun providePageConfigurator(homeRepository: HomeRepository): NavigationPageConfigurator =
        HomePageConfigurator(homeRepository, ResourceManager(view.context, ColorUtils()))

    @Provides
    @PerActivity
    fun provideDeleteUserData(
        workManagerController: WorkManagerController,
        preferencesProvider: PreferenceProvider,
        filterManager: FilterManager,
    ): DeleteUserData =
        DeleteUserData(
            workManagerController,
            filterManager,
            preferencesProvider,
        )
}
