package org.dhis2.usescases.settings.models

import org.hisp.dhis.android.core.settings.LimitScope

data class SyncParametersViewModel(
    val numberOfTeiToDownload: Int,
    val numberOfEventsToDownload: Int,
    val currentTeiCount: Int,
    val currentEventCount: Int,
    val limitScope: LimitScope,
    val teiNumberIsEditable: Boolean,
    val eventNumberIsEditable: Boolean,
    val limitScopeIsEditable: Boolean,
    val hasSpecificProgramSettings: Int
)
