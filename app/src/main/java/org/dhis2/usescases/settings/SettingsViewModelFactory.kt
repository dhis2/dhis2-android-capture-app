package org.dhis2.usescases.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
import org.dhis2.usescases.settings.domain.UpdateSyncSettings
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.utils.analytics.AnalyticsHelper

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
    private val getSettingsState: GetSettingsState,
    private val updateSyncSettings: UpdateSyncSettings,
    private val updateSmsResponse: UpdateSmsResponse,
    private val gatewayValidator: GatewayValidator,
    private val preferenceProvider: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val errorMapper: ErrorModelMapper,
    private val resourceManager: ResourceManager,
    private val versionRepository: VersionRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val networkUtils: NetworkUtils,
    private val fileHandler: FileHandler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyncManagerPresenter(
            getSettingsState = getSettingsState,
            updateSyncSettings = updateSyncSettings,
            updateSmsResponse = updateSmsResponse,
            gatewayValidator = gatewayValidator,
            preferenceProvider = preferenceProvider,
            workManagerController = workManagerController,
            settingsRepository = settingsRepository,
            analyticsHelper = analyticsHelper,
            errorMapper = errorMapper,
            resourceManager = resourceManager,
            versionRepository = versionRepository,
            dispatcherProvider = dispatcherProvider,
            networkUtils = networkUtils,
            fileHandler = fileHandler,
        ) as T
    }
}
