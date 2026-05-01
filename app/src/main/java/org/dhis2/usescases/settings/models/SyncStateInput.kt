package org.dhis2.usescases.settings.models

import org.dhis2.usescases.settings.SettingItem

data class SyncStateInput(
    val openedItem: SettingItem?,
    val hasConnection: Boolean,
    val metadataSyncInProgress: Boolean,
    val dataSyncInProgress: Boolean,
)
