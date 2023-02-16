package org.dhis2.usescases.settings.models

data class SyncButtonUIModel(
    val text: String,
    val enabled: Boolean,
    val onClick: () -> Unit
)
