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
import org.dhis2.usescases.settings.domain.DeleteLocalData
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.domain.SettingsMessages
import org.dhis2.usescases.settings.domain.UpdateSmsModule
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
import org.dhis2.usescases.settings.domain.UpdateSyncSettings
import org.dhis2.utils.analytics.AnalyticsHelper

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
    private val getSettingsState: GetSettingsState,
    private val updateSyncSettings: UpdateSyncSettings,
    private val updateSmsResponse: UpdateSmsResponse,
    private val getSyncErrors: GetSyncErrors,
    private val updateSmsModule: UpdateSmsModule,
    private val deleteLocalData: DeleteLocalData,
    private val preferenceProvider: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val resourceManager: ResourceManager,
    private val versionRepository: VersionRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val networkUtils: NetworkUtils,
    private val fileHandler: FileHandler,
    private val settingsMessages: SettingsMessages,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyncManagerPresenter(
            getSettingsState = getSettingsState,
            updateSyncSettings = updateSyncSettings,
            updateSmsResponse = updateSmsResponse,
            getSyncErrors = getSyncErrors,
            updateSmsModule = updateSmsModule,
            deleteLocalData = deleteLocalData,
            preferenceProvider = preferenceProvider,
            workManagerController = workManagerController,
            settingsRepository = settingsRepository,
            analyticsHelper = analyticsHelper,
            resourceManager = resourceManager,
            versionRepository = versionRepository,
            dispatcherProvider = dispatcherProvider,
            networkUtils = networkUtils,
            fileHandler = fileHandler,
            settingsMessages = settingsMessages,
        ) as T
    }
}
