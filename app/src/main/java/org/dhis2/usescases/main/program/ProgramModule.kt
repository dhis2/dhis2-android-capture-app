package org.dhis2.usescases.main.program

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.service.SyncStatusController
import org.hisp.dhis.android.core.D2

@Module
class ProgramModule(
    private val view: ProgramView,
) {
    @Provides
    @PerFragment
    internal fun programViewModelFactory(
        programRepository: ProgramRepository,
        dispatcherProvider: DispatcherProvider,
        featureConfigRepository: FeatureConfigRepository,
        matomoAnalyticsController: MatomoAnalyticsController,
        filterManager: FilterManager,
        syncStatusController: SyncStatusController,
        schedulerProvider: SchedulerProvider,
    ): ProgramViewModelFactory =
        ProgramViewModelFactory(
            view,
            programRepository,
            featureConfigRepository,
            dispatcherProvider,
            matomoAnalyticsController,
            filterManager,
            syncStatusController,
            schedulerProvider,
        )

    @Provides
    @PerFragment
    internal fun homeRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        dhisProgramUtils: DhisProgramUtils,
        schedulerProvider: SchedulerProvider,
        colorUtils: ColorUtils,
        metadataIconProvider: MetadataIconProvider,
    ): ProgramRepository =
        ProgramRepositoryImpl(
            d2,
            filterPresenter,
            dhisProgramUtils,
            ResourceManager(view.context, colorUtils),
            metadataIconProvider,
            schedulerProvider,
        )
}
