package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle

class ConfigureEventDetails(
    private val d2: D2,
    private val programStageId: String
) {

    operator fun invoke(): EventDetails {
        return EventDetails(
            name = getProgramStageById().displayName(),
            description = getProgramStageById().displayDescription(),
            style = getStyleByProgramId()
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
}
