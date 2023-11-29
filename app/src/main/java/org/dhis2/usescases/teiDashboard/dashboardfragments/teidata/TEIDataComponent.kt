package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.usescases.eventswithoutregistration.eventteidetails.EventTeiDetailsFragment

@PerFragment
@Subcomponent(modules = [TEIDataModule::class])
interface TEIDataComponent {
    fun inject(notesFragment: TEIDataFragment?)
    fun inject(eventTeiDetailsFragment: EventTeiDetailsFragment?)
}
