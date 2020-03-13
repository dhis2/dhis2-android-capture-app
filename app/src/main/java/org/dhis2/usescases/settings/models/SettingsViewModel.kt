package org.dhis2.usescases.settings.models

data class SettingsViewModel(
    val metadataSettingsViewModel: MetadataSettingsViewModel,
    val dataSettingsViewModel: DataSettingsViewModel,
    val syncParametersViewModel: SyncParametersViewModel,
    val reservedValueSettingsViewModel: ReservedValueSettingsViewModel,
    val smsSettingsViewModel: SMSSettingsViewModel
)
