package org.dhis2.usescases.settings.models

data class MetadataSettingsViewModel(
    val metadataSyncPeriod: Int,
    val lastMetadataSync: String,
    val hasErrors: Boolean,
    val canEdit: Boolean,
    val theme: Int,
    val flag: String
)
