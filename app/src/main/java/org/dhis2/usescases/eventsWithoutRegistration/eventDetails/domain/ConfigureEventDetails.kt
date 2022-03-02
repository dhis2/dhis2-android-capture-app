package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus.NonEditable
import org.hisp.dhis.android.core.program.Program

class ConfigureEventDetails(
    private val d2: D2,
    private val eventInitialRepository: EventInitialRepository,
    private val programStageId: String,
    private val eventId: String? = null,
    private val programId: String? = null,
    private val resourcesProvider: EventDetailResourcesProvider,
    private val creationType: EventCreationType
) {

    operator fun invoke(
        selectedDate: Date?,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        isCatComboCompleted: Boolean,
        coordinates: String?,
        tempCreate: String?
    ): Flow<EventDetails> {
        return flowOf(
            EventDetails(
                name = getProgramStageById().displayName(),
                description = getProgramStageById().displayDescription(),
                style = getStyleByProgramId(),
                enabled = isEnable(),
                isEditable = isEditable(),
                editableReason = getEditableReason(),
                temCreate = tempCreate,
                selectedDate = selectedDate,
                selectedOrgUnit = selectedOrgUnit,
                catOptionComboUid = catOptionComboUid,
                coordinates = coordinates,
                isCompleted = isCompleted(
                    selectedDate = selectedDate,
                    selectedOrgUnit = selectedOrgUnit,
                    isCatComboCompleted = isCatComboCompleted,
                    tempCreate = tempCreate
                )
            )
        )
    }

    private fun isCompleted(
        selectedDate: Date?,
        selectedOrgUnit: String?,
        isCatComboCompleted: Boolean,
        tempCreate: String?
    ) = selectedDate != null &&
        !selectedOrgUnit.isNullOrEmpty() &&
        isCatComboCompleted &&
        (creationType != REFERAL || tempCreate != null)

    private fun isEditable(): Boolean {
        return eventId == null || getEditableReason() == null
    }

    private fun getEditableReason(): String? {
        if (eventId == null) return null
        eventInitialRepository.editableStatus.blockingFirst()?.let {
            if (it is NonEditable) {
                return resourcesProvider.provideEditionStatus(it.reason)
            }
        }
        return null
    }

    private fun getStyleByProgramId(): ObjectStyle? {
        return d2.programModule()
            .programs()
            .uid(getProgramStageById()?.program()?.uid())
            .blockingGet()
            .style()
    }

    private fun getProgramStageById() =
        d2.programModule().programStages().uid(programStageId).blockingGet()

    private fun isEnable(): Boolean {
        if (!isEditable()) return false
        getStoredEvent()?.let { event ->
            val program = getProgram()!!
            val isExpired = org.dhis2.utils.DateUtils.getInstance().isEventExpired(
                event.eventDate(),
                event.completedDate(),
                event.status(),
                program.completeEventsExpiryDays() ?: 0,
                program.expiryPeriodType(),
                program.expiryDays() ?: 0
            )
            return !isExpired
        }

        val canWrite = eventInitialRepository.accessDataWrite(programId).blockingFirst()
        val isEnrollmentOpen = eventInitialRepository.isEnrollmentOpen
        if (!canWrite || !isEnrollmentOpen) {
            return false
        }

        return true
    }

    private fun getStoredEvent(): Event? =
        eventId?.let { eventInitialRepository.event(it).blockingFirst() }

    private fun getProgram(): Program? =
        programId?.let { eventInitialRepository.getProgramWithId(it).blockingFirst() }
}
