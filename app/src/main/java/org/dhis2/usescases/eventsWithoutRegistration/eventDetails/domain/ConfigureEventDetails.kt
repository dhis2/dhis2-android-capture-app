package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program

class ConfigureEventDetails(
    private val d2: D2,
    private val eventInitialRepository: EventInitialRepository,
    private val programStageId: String,
    private val eventId: String? = null,
    private val programId: String? = null
) {

    operator fun invoke(): EventDetails {
        return EventDetails(
            name = getProgramStageById().displayName(),
            description = getProgramStageById().displayDescription(),
            style = getStyleByProgramId(),
            enabled = isEnable()
        )
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
