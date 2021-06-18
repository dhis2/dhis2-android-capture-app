package org.dhis2.usescases.about

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [AboutModule::class])
interface AboutComponent {
    fun inject(programFragment: AboutFragment?)
}
