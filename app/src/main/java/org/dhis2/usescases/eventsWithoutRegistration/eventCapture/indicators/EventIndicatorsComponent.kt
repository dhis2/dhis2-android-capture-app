package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators

import dagger.Subcomponent
import org.dhis2.data.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [EventIndicatorsModule::class])
interface EventIndicatorsComponent {
    fun inject(eventIndicatorsDialogFragment: EventIndicatorsDialogFragment?)
}