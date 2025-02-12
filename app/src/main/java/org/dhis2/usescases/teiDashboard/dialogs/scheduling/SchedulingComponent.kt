package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [SchedulingModule::class])
fun interface SchedulingComponent {
    fun inject(schedulingDialog: SchedulingDialog)
}
