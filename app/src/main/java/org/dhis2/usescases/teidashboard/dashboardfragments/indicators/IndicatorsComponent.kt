package org.dhis2.usescases.teidashboard.dashboardfragments.indicators

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [IndicatorsModule::class])
interface IndicatorsComponent {
    fun inject(indicatorsFragment: IndicatorsFragment)
}
