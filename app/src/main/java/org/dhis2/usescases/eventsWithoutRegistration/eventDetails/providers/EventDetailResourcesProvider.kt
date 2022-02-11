package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager

class EventDetailResourcesProvider(
    private val resourceManager: ResourceManager
) {
    fun provideDueDate() = resourceManager.getString(R.string.due_date)

    fun provideEventDate() = resourceManager.getString(R.string.event_date)
}
