package org.dhis2.usescases.searchTrackEntity.mapView

import dagger.Module
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [SearchTEMapModule::class])
interface SearchTEMapComponent {
    fun inject(fragment: SearchTEMap)
}

@Module
class SearchTEMapModule()
