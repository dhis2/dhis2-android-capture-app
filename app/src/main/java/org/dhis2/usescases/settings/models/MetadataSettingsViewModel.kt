package org.dhis2.usescases.settings.models

data class MetadataSettingsViewModel(
    val metadataSyncPeriod: Int,
    val lastMetadataSync: String,
    val nextMetadataSync: String?,
    val nextSettingsSync: String?,
    val hasErrors: Boolean,
    val canEdit: Boolean,
    val syncInProgress: Boolean,
)
