package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType.NONE

class ConfigureEventCoordinates(
    private val d2: D2,
    private val programStageId: String
) {

    operator fun invoke(): EventCoordinates {
        return EventCoordinates(
            active = isActive()
        )
    }

    private fun isActive(): Boolean {
        getProgramStageById()?.let { programStage ->
            programStage.featureType()?.let {
                return it != NONE
            }
        }
        return false
    }

    private fun getProgramStageById() =
        d2.programModule().programStages().uid(programStageId).blockingGet()
}
