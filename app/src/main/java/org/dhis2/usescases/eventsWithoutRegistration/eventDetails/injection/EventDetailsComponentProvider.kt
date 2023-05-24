package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection

interface EventDetailsComponentProvider {
    fun provideEventDetailsComponent(module: EventDetailsModule?): EventDetailsComponent?
}
