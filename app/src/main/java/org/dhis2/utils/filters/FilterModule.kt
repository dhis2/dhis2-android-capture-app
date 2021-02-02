package org.dhis2.utils.filters

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.utils.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.utils.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.utils.resources.ResourceManager

@Module
class FilterModule {
    @Provides
    @Singleton
    fun filterManager(resourceManager: ResourceManager): FilterManager {
        return FilterManager.initWith(resourceManager)
    }

    @Provides
    @Singleton
    fun eventWorkingListMapper(
        resourceManager: ResourceManager
    ): EventFilterToWorkingListItemMapper {
        return EventFilterToWorkingListItemMapper(
            resourceManager.filterResources.defaultWorkingListLabel()
        )
    }

    @Provides
    @Singleton
    fun teiWorkingListMapper(resourceManager: ResourceManager): TeiFilterToWorkingListItemMapper {
        return TeiFilterToWorkingListItemMapper(
            resourceManager.filterResources.defaultWorkingListLabel()
        )
    }
}
