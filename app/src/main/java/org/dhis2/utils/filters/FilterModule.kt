package org.dhis2.utils.filters

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.utils.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.utils.filters.workingLists.RelativePeriodToStringMapper
import org.dhis2.utils.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.utils.resources.ResourceManager

@Module
class FilterModule {
    @Provides
    @Singleton
    fun filterManager(): FilterManager {
        return FilterManager.getInstance()
    }

    @Provides
    @Singleton
    fun eventWorkingListMapper(resourceManager: ResourceManager):
        EventFilterToWorkingListItemMapper {
            return EventFilterToWorkingListItemMapper(
                resourceManager.filterResources.defaultWorkingListLabel(),
                RelativePeriodToStringMapper(resourceManager.filterResources)
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
