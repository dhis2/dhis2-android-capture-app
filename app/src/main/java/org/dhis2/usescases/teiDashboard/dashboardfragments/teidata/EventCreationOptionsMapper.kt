package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.DEFAULT
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions
import org.dhis2.utils.dialFloatingActionButton.DialItem

class EventCreationOptionsMapper(val resources: ResourceManager) {

    fun mapToEventsByStage(availableOptions: List<EventCreationType>): List<EventCreationOptions> {
        return availableOptions.map { item ->
            EventCreationOptions(
                item,
                getOptionName(item),
            )
        }
    }

    private fun getOptionName(item: EventCreationType): String {
        return when (item) {
            SCHEDULE -> resources.getString(R.string.schedule_new)
            ADDNEW -> resources.getString(R.string.add_new)
            REFERAL -> resources.getString(R.string.referral)
            DEFAULT -> resources.getString(R.string.add_new)
        }
    }

    fun mapToEventsByTimeLine(availableOptions: List<EventCreationType>): List<DialItem> {
        return availableOptions.map { item ->
            DialItem(
                id = getItemId(item),
                label = getOptionName(item),
                icon = getIconResource(item),
            )
        }
    }

    private fun getItemId(item: EventCreationType): Int {
        return when (item) {
            SCHEDULE -> TEIDataFragment.SCHEDULE_ID
            ADDNEW -> TEIDataFragment.ADD_NEW_ID
            REFERAL -> TEIDataFragment.REFERAL_ID
            DEFAULT -> TEIDataFragment.ADD_NEW_ID
        }
    }

    private fun getIconResource(item: EventCreationType): Int {
        return when (item) {
            SCHEDULE -> R.drawable.ic_date_range
            ADDNEW -> R.drawable.ic_note_add
            REFERAL -> R.drawable.ic_arrow_forward
            DEFAULT -> R.drawable.ic_note_add
        }
    }
}
