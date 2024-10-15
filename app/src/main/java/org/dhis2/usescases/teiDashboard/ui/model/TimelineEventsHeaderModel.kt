package org.dhis2.usescases.teiDashboard.ui.model

import org.dhis2.commons.data.EventCreationType
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData

data class TimelineEventsHeaderModel(
    val displayEventCreationButton: Boolean = true,
    val eventCount: Int,
    val eventLabel: String,
    val options: List<MenuItemData<EventCreationType>>,
)
