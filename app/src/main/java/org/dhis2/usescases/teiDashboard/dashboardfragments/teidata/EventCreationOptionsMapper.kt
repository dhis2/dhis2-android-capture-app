package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Event
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.DEFAULT
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.icons.DHIS2Icons
import org.dhis2.ui.icons.DataEntryFilled
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement

class EventCreationOptionsMapper(
    val resources: ResourceManager,
) {
    companion object {
        const val REFERRAL_ID = 3
        const val ADD_NEW_ID = 2
        const val SCHEDULE_ID = 1
    }

    fun mapToEventsByStage(
        availableOptions: List<EventCreationType>,
        displayEventLabel: String?,
    ): List<MenuItemData<EventCreationType>> =
        availableOptions.map { item ->
            MenuItemData(
                id = item,
                label = getOptionName(item, displayEventLabel ?: resources.getString(R.string.event)),
                leadingElement = getMenuItemIcon(item),
            )
        }

    private fun getMenuItemIcon(item: EventCreationType): MenuLeadingElement =
        when (item) {
            ADDNEW -> MenuLeadingElement.Icon(icon = DHIS2Icons.DataEntryFilled)
            SCHEDULE -> MenuLeadingElement.Icon(icon = Icons.Outlined.Event)
            REFERAL -> MenuLeadingElement.Icon(icon = Icons.AutoMirrored.Outlined.ArrowForward)
            DEFAULT -> MenuLeadingElement.Icon(icon = Icons.Outlined.Event)
        }

    private fun getOptionName(
        item: EventCreationType,
        displayEventLabel: String,
    ): String =
        when (item) {
            SCHEDULE -> resources.getString(R.string.schedule) + " " + displayEventLabel
            ADDNEW -> resources.getString(R.string.enter) + " " + displayEventLabel
            REFERAL -> resources.getString(R.string.refer) + " " + displayEventLabel
            DEFAULT -> resources.getString(R.string.enter) + " " + displayEventLabel
        }

    fun getActionType(eventCreationId: Int): EventCreationType =
        when (eventCreationId) {
            SCHEDULE_ID -> SCHEDULE
            ADD_NEW_ID -> ADDNEW
            REFERRAL_ID -> REFERAL
            else -> throw UnsupportedOperationException(
                "id %s is not supported as an event creation".format(
                    eventCreationId,
                ),
            )
        }
}
