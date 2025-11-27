package org.dhis2.usescases.main.program

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.SyncStatusController

class ProgramViewModelFactory(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val featureConfigRepository: FeatureConfigRepository,
    private val dispatchers: DispatcherProvider,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val filterManager: FilterManager,
    private val syncStatusController: SyncStatusController,
    private val schedulerProvider: SchedulerProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProgramViewModel(
            view,
            programRepository,
            featureConfigRepository,
            dispatchers,
            matomoAnalyticsController,
            filterManager,
            syncStatusController,
            schedulerProvider,
        ) as T
}
