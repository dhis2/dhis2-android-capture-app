package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp

class ConfigureEventTemp(
    private val creationType: EventCreationType
) {

    operator fun invoke(): EventTemp {
        return EventTemp(
            active = isActive()
        )
    }

    private fun isActive(): Boolean {
        return creationType == REFERAL
    }
}
