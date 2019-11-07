package org.dhis2.usescases.main

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

/**
 * QUADRAM. Created by ppajuelo on 17/10/2017.
 */
@PerActivity
@Subcomponent(modules = [MainModule::class])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
}
