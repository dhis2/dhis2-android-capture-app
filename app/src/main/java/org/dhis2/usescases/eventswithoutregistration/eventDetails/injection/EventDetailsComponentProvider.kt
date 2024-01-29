package org.dhis2.usescases.eventswithoutregistration.eventDetails.injection

interface EventDetailsComponentProvider {
    fun provideEventDetailsComponent(module: EventDetailsModule?): EventDetailsComponent?
}
