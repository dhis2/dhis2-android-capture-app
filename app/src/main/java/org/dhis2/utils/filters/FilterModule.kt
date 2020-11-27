package org.dhis2.utils.filters

import dagger.Module
import dagger.Provides
import org.dhis2.utils.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.utils.filters.workingLists.RelativePeriodToStringMapper
import org.dhis2.utils.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.utils.resources.ResourceManager
import javax.inject.Singleton

@Module
class FilterModule {
    @Provides
    @Singleton
    fun filterManager(): FilterManager {
        return FilterManager.getInstance()
    }

    @Provides
    @Singleton
    fun eventWorkingListMapper(resourceManager: ResourceManager): EventFilterToWorkingListItemMapper {
        return EventFilterToWorkingListItemMapper(
            resourceManager.defaultWorkingListLabel(),
            RelativePeriodToStringMapper(resourceManager)
        )
    }

    @Provides
    @Singleton
    fun teiWorkingListMapper(resourceManager: ResourceManager): TeiFilterToWorkingListItemMapper {
        return TeiFilterToWorkingListItemMapper(resourceManager.defaultWorkingListLabel())
    }
}