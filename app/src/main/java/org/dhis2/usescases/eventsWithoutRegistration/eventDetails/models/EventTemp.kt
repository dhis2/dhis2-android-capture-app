package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

data class EventTemp(val active: Boolean = false, val status: EventTempStatus? = null)

enum class EventTempStatus{
    ONE_TIME,
    PERMANENT
}