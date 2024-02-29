package org.dhis2.commons.data

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

data class EventViewModel(
    val type: EventViewModelType,
    val stage: ProgramStage?,
    val event: Event?,
    val eventCount: Int,
    val lastUpdate: Date?,
    var isSelected: Boolean,
    val canAddNewEvent: Boolean,
    val orgUnitName: String,
    val catComboName: String?,
    val dataElementValues: List<Pair<String, String?>>?,
    val groupedByStage: Boolean? = false,
    var valueListIsOpen: Boolean = false,
    val showTopShadow: Boolean = false,
    val showBottomShadow: Boolean = false,
    val showAllEvents: Boolean = false,
    val maxEventsToShow: Int = 0,
    val displayDate: String?,
    val nameCategoryOptionCombo: String?,
    val metadataIconData: MetadataIconData,
) {
    fun toggleValueList() {
        this.valueListIsOpen = !valueListIsOpen
    }

    fun canShowAddButton(): Boolean {
        return if (type == EventViewModelType.STAGE) {
            canAddNewEvent && (stage?.repeatable() == true || eventCount == 0)
        } else {
            true
        }
    }

    fun isAfterToday(today: Date): Boolean {
        return type == EventViewModelType.EVENT && event?.eventDate() != null &&
            event.eventDate()?.after(today) == true
    }

    fun applyHideStage(hidden: Boolean): EventViewModel? {
        return when {
            type == EventViewModelType.STAGE && hidden -> when {
                eventCount > 0 -> copy(canAddNewEvent = false)
                else -> null
            }

            else -> this
        }
    }
}

fun List<EventViewModel>.uids(): List<String> {
    return map {
        it.event?.uid()!!
    }
}
