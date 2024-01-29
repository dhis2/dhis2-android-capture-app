package org.dhis2.usescases.eventswithoutregistration.eventDetails.injection

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.usescases.eventswithoutregistration.eventDetails.ui.EventDetailsFragment

@PerFragment
@Subcomponent(modules = [EventDetailsModule::class])
interface EventDetailsComponent {
    fun inject(eventDetailsFragment: EventDetailsFragment?)
}
