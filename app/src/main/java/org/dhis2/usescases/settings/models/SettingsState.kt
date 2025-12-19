package org.dhis2.usescases.settings.models

import org.dhis2.usescases.settings.SettingItem

data class SettingsState(
    val openedItem: SettingItem?,
    val hasConnection: Boolean,
    val metadataSettingsViewModel: MetadataSettingsViewModel,
    val dataSettingsViewModel: DataSettingsViewModel,
    val syncParametersViewModel: SyncParametersViewModel,
    val reservedValueSettingsViewModel: ReservedValueSettingsViewModel,
    val smsSettingsViewModel: SMSSettingsViewModel,
    val isTwoFAConfigured: Boolean,
    val versionName: String,
    val deleteDataState: DeleteDataState = DeleteDataState.None,
) {
    fun canInitDataSync() = hasConnection && !dataSettingsViewModel.syncInProgress

    fun canInitMetadataSync() = hasConnection && !metadataSettingsViewModel.syncInProgress
}
