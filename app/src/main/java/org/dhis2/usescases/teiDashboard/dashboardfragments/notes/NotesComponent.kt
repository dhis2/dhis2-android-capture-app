package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import dagger.Subcomponent
import org.dhis2.data.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [NotesModule::class])
interface NotesComponent {
    fun inject(notesFragment: NotesFragment)
}
