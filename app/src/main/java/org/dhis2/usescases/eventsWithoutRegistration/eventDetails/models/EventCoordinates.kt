package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.dhis2.form.model.FieldUiModel

data class EventCoordinates(
    val active: Boolean = true,
    val model: FieldUiModel? = null,
)
