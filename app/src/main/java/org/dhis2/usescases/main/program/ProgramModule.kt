package org.dhis2.usescases.main.program

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.service.SyncStatusController
import org.hisp.dhis.android.core.D2

@Module
class ProgramModule(private val view: ProgramView) {

    @Provides
    @PerFragment
    internal fun programPresenter(
        programRepository: ProgramRepository,
        schedulerProvider: SchedulerProvider,
        filterManager: FilterManager,
        matomoAnalyticsController: MatomoAnalyticsController,
        syncStatusController: SyncStatusController,
    ): ProgramPresenter {
        return ProgramPresenter(
            view,
            programRepository,
            schedulerProvider,
            filterManager,
            matomoAnalyticsController,
            syncStatusController,
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
        colorUtils: ColorUtils,
        metadataIconProvider: MetadataIconProvider,
    ): ProgramRepository {
        return ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            dhisTrackedEntityInstanceUtils,
            ResourceManager(view.context, colorUtils),
            metadataIconProvider,
            schedulerProvider,
        )
    }

    @Provides
    @PerFragment
    fun provideAnimations(): ProgramAnimation {
        return ProgramAnimation()
    }
}
