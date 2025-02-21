package org.dhis2.commons.filters.periods.di

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.filters.periods.ui.FilterPeriodsDialog

@PerActivity
@Subcomponent(modules = [FilterPeriodsDialogModule::class])
interface FilterPeriodsDialogComponent {
    fun inject(activity: FilterPeriodsDialog)
}
