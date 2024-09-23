package org.dhis2.usescases.teiDashboard.ui.model

import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions

data class TimelineEventsHeaderModel(
    val displayEventCreationButton: Boolean = true,
    val eventCount: Int,
    val options: List<EventCreationOptions>,
)
