package org.dhis2.usescases.main.program

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.service.SyncStatusController
import org.dhis2.ui.ThemeManager
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.D2

@Module
class ProgramModule(private val view: ProgramView) {

    @Provides
    @PerFragment
    internal fun programPresenter(
        programRepository: ProgramRepository,
        schedulerProvider: SchedulerProvider,
        themeManager: ThemeManager,
        filterManager: FilterManager,
        matomoAnalyticsController: MatomoAnalyticsController,
        syncStatusController: SyncStatusController
    ): ProgramPresenter {
        return ProgramPresenter(
            view,
            programRepository,
            schedulerProvider,
            themeManager,
            filterManager,
            matomoAnalyticsController,
            syncStatusController
        )
    }

    @Provides
    @PerFragment
    internal fun homeRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        dhisProgramUtils: DhisProgramUtils,
        dhisTrackedEntityInstanceUtils: DhisTrackedEntityInstanceUtils,
        schedulerProvider: SchedulerProvider
    ): ProgramRepository {
        return ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            dhisTrackedEntityInstanceUtils,
            ResourceManager(view.context),
            schedulerProvider
        )
    }

    @Provides
    @PerFragment
    fun provideAnimations(): ProgramAnimation {
        return ProgramAnimation()
    }
}
