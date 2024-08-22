package org.dhis2.usescases.main.program

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.SyncStatusController

@Suppress("UNCHECKED_CAST")
class ProgramViewModelFactory(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val featureConfigRepository: FeatureConfigRepository,
    private val dispatchers: DispatcherProvider,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val syncStatusController: SyncStatusController,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProgramViewModel(
            view,
            programRepository,
            featureConfigRepository,
            dispatchers,
            matomoAnalyticsController,
            syncStatusController,
        ) as T
    }
}
