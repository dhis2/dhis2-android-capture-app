package org.dhis2.usescases.main.program

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.D2

@Module
@PerFragment
class ProgramModule(private val view: ProgramView) {

    @Provides
    @PerFragment
    internal fun programPresenter(
        programRepository: ProgramRepository,
        schedulerProvider: SchedulerProvider,
        preferenceProvider: PreferenceProvider,
        filterManager: FilterManager,
        matomoAnalyticsController: MatomoAnalyticsController
    ): ProgramPresenter {
        return ProgramPresenter(
            view,
            programRepository,
            schedulerProvider,
            preferenceProvider,
            filterManager,
            matomoAnalyticsController
        )
    }

    @Provides
    @PerFragment
    internal fun homeRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        dhisProgramUtils: DhisProgramUtils,
        dhisTrackedEntityInstanceUtils: DhisTrackedEntityInstanceUtils,
        schedulerProvider: SchedulerProvider,
        resourceManager: ResourceManager
    ): ProgramRepository {
        return ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            dhisTrackedEntityInstanceUtils,
            resourceManager,
            schedulerProvider
        )
    }

    @Provides
    @PerFragment
    internal fun providesAdapter(presenter: ProgramPresenter): ProgramModelAdapter {
        return ProgramModelAdapter(presenter)
    }

    @Provides
    @PerFragment
    fun provideAnimations(): ProgramAnimation {
        return ProgramAnimation()
    }
}
