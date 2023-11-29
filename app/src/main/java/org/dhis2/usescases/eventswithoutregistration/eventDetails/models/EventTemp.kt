package org.dhis2.usescases.eventswithoutregistration.eventDetails.models

data class EventTemp(
    val active: Boolean = false,
    val status: EventTempStatus? = null,
    val completed: Boolean = true,
)
