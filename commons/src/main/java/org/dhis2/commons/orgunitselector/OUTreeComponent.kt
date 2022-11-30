package org.dhis2.commons.orgunitselector

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [OUTreeModule::class])
interface OUTreeComponent {
    fun inject(activity: OUTreeFragment)
}
