package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    operator fun invoke(value: String? = null): Flow<EventCoordinates> {
        return flowOf(
            EventCoordinates(
                active = isActive(),
                model = getGeometryModel(value)
            )
        )
    }

    private fun getGeometryModel(value: String?): FieldUiModel {
        var model = eventInitialRepository.getGeometryModel(programId).blockingGet()
        value?.let { model = model.setValue(it) }
        return model
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
