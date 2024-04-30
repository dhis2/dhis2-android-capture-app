package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.form.model.FieldUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.hisp.dhis.android.core.common.FeatureType.NONE

class ConfigureEventCoordinates(
    private val repository: EventDetailsRepository,
) {

    operator fun invoke(value: String? = ""): Flow<EventCoordinates> {
        return flowOf(
            EventCoordinates(
                active = isActive(),
                model = getGeometryModel(value),
            ),
        )
    }

    private fun getGeometryModel(value: String?): FieldUiModel {
        var model = repository.getGeometryModel()
        if (value?.isNotEmpty() != false) {
            model = model.setValue(value)
        }
        return model
    }

    private fun isActive(): Boolean {
        repository.getProgramStage().let { programStage ->
            programStage?.featureType()?.let {
                return it != NONE
            }
        }
        return false
    }
}
