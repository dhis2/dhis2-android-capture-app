package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.InputDateValues

data class EventInputDateUiModel(
    val eventDate: EventDate,
    val detailsEnabled: Boolean,
    val onDateClick: () -> Unit,
    val allowsManualInput: Boolean = true,
    val onDateSet: (InputDateValues) -> Unit,
    val onClear: () -> Unit,
    val required: Boolean = false,
    val showField: Boolean = true,
)
