package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.form.model.FieldUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType.NONE

class ConfigureEventCoordinates(
    private val d2: D2,
    private val programId: String,
    private val programStageId: String,
    private val eventInitialRepository: EventInitialRepository
) {

    operator fun invoke(): EventCoordinates {
        return EventCoordinates(
            active = isActive(),
            model = getGeometryModel()
        )
    }

    private fun getGeometryModel(): FieldUiModel {
        return eventInitialRepository.getGeometryModel(programId).blockingGet()
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
