package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [TEIDataModule::class])
interface TEIDataComponent {
    fun inject(notesFragment: TEIDataFragment?)
}
