package org.dhis2.usescases.eventswithoutregistration.eventCapture.eventInitialFragment

import dagger.Subcomponent

@Subcomponent(modules = [EventCaptureInitialModule::class])
interface EventCaptureInitialComponent {
    fun inject(fragment: EventCaptureInitialFragment)
}
