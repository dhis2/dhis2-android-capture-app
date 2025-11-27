package org.dhis2.commons.data

import org.dhis2.mobile.commons.model.MetadataIconData
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

data class EventModel(
    val type: EventViewModelType,
    val stage: ProgramStage?,
    val event: Event?,
    val eventCount: Int,
    val lastUpdate: Date?,
    var isSelected: Boolean,
    val canAddNewEvent: Boolean,
    val orgUnitName: String,
    val orgUnitIsInCaptureScope: Boolean = true,
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
    val editable: Boolean = true,
    val displayOrgUnit: Boolean = true,
) {
    var isClicked: Boolean = false

    fun toggleValueList() {
        this.valueListIsOpen = !valueListIsOpen
    }

    fun canShowAddButton(): Boolean =
        if (type == EventViewModelType.STAGE) {
            canAddNewEvent && (stage?.repeatable() == true || eventCount == 0)
        } else {
            true
        }

    fun isAfterToday(today: Date): Boolean =
        type == EventViewModelType.EVENT &&
            event?.eventDate() != null &&
            event.eventDate()?.after(today) == true

    fun applyHideStage(hidden: Boolean): EventModel? =
        when {
            type == EventViewModelType.STAGE && hidden ->
                when {
                    eventCount > 0 -> copy(canAddNewEvent = false)
                    else -> null
                }

            else -> this
        }
}

fun List<EventModel>.uids(): List<String> =
    map {
        it.event?.uid()!!
    }
