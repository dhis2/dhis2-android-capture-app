package org.dhis2.uicomponents.map.model

import java.util.Date
import org.hisp.dhis.android.core.event.Event

data class EventUiComponentModel(
    val stageUid: String,
    val stageDisplayName: String,
    val eventUid: String,
    val event: Event,
    val lastUpdated: Date?
)
