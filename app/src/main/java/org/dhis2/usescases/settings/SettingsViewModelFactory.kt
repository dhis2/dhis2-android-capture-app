package org.dhis2.usescases.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.settings.domain.CheckVersionUpdate
import org.dhis2.usescases.settings.domain.DeleteLocalData
import org.dhis2.usescases.settings.domain.ExportDatabase
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.domain.LaunchSync
import org.dhis2.usescases.settings.domain.SettingsMessages
import org.dhis2.usescases.settings.domain.UpdateSmsModule
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
import org.dhis2.usescases.settings.domain.UpdateSyncSettings

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
    private val getSettingsState: GetSettingsState,
    private val updateSyncSettings: UpdateSyncSettings,
    private val updateSmsResponse: UpdateSmsResponse,
    private val getSyncErrors: GetSyncErrors,
    private val updateSmsModule: UpdateSmsModule,
    private val deleteLocalData: DeleteLocalData,
    private val exportDatabase: ExportDatabase,
    private val checkVersionUpdate: CheckVersionUpdate,
    private val launchSync: LaunchSync,
    private val dispatcherProvider: DispatcherProvider,
    private val networkUtils: NetworkUtils,
    private val settingsMessages: SettingsMessages,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SyncManagerPresenter(
            getSettingsState = getSettingsState,
            updateSyncSettings = updateSyncSettings,
            updateSmsResponse = updateSmsResponse,
            getSyncErrors = getSyncErrors,
            updateSmsModule = updateSmsModule,
            deleteLocalData = deleteLocalData,
            exportDatabase = exportDatabase,
            checkVersionUpdate = checkVersionUpdate,
            launchSync = launchSync,
            dispatcherProvider = dispatcherProvider,
            networkUtils = networkUtils,
            settingsMessages = settingsMessages,
        ) as T
}
