package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.TeiEventCaptureFormFragment

@PerFragment
@Subcomponent(modules = [EventCaptureFormModule::class])
interface EventCaptureFormComponent {
    fun inject(fragment: EventCaptureFormFragment)
// TODO:  reversed
    fun inject(fragment: TeiEventCaptureFormFragment)
}
