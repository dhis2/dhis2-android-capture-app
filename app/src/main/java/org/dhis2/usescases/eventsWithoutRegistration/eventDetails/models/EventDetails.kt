package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.common.ObjectStyle

data class EventDetails(
    val name: String?,
    val description: String?,
    val style: ObjectStyle?
)
