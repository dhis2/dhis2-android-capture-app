package org.dhis2.usescases.settings.models

import org.dhis2.data.service.SyncResult

data class DataSettingsViewModel(
    val dataSyncPeriod: Int,
    val lastDataSync: String,
    val syncHasErrors: Boolean,
    val dataHasErrors: Boolean,
    val dataHasWarnings: Boolean,
    val canEdit: Boolean,
    val syncResult: SyncResult? = null,
)
