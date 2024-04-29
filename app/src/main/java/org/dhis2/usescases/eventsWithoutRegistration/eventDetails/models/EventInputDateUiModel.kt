package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.InputDateValues
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates

data class EventInputDateUiModel(
    val eventDate: EventDate,
    val detailsEnabled: Boolean,
    val onDateClick: (() -> Unit)?,
    val allowsManualInput: Boolean = true,
    val onDateSelected: (InputDateValues) -> Unit?,
    val onClear: (() -> Unit)? = null,
    val required: Boolean = false,
    val showField: Boolean = true,
    val is24HourFormat: Boolean = true,
    val selectableDates: SelectableDates? = null,
)
