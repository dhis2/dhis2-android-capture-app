package org.dhis2.usescases.eventswithoutregistration.eventcapture.eventcapturefragment

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.teievents.TeiEventCaptureFormFragment

@PerFragment
@Subcomponent(modules = [EventCaptureFormModule::class])
interface EventCaptureFormComponent {
    fun inject(fragment: EventCaptureFormFragment)
    fun inject(fragment: TeiEventCaptureFormFragment)
}
