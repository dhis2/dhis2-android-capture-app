package org.dhis2.usescases.searchTrackEntity.listView

import dagger.Module
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [SearchTEListModule::class])
interface SearchTEListComponent {
    fun inject(fragment: SearchTEList)
}

@Module
class SearchTEListModule()
