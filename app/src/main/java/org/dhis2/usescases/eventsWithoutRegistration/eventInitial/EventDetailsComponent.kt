package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [EventDetailsModule::class])
interface EventDetailsComponent {
    fun inject(eventDetailsFragment: EventDetailsFragment?)
}