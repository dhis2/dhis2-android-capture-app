package org.dhis2.commons.filters.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerServer
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.ProgramStageToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.commons.resources.ResourceManager

@Module
class FilterModule {
    @Provides
    @PerServer
    fun filterManager(resourceManager: ResourceManager): FilterManager {
        return FilterManager.initWith(resourceManager)
    }

    @Provides
    @PerServer
    fun eventWorkingListMapper(
        resourceManager: FilterResources,
    ): EventFilterToWorkingListItemMapper {
        return EventFilterToWorkingListItemMapper(
            resourceManager.defaultWorkingListLabel(),
        )
    }

    @Provides
    @PerServer
    fun teiWorkingListMapper(resourceManager: FilterResources): TeiFilterToWorkingListItemMapper {
        return TeiFilterToWorkingListItemMapper(
            resourceManager.defaultWorkingListLabel(),
        )
    }

    @Provides
    @PerServer
    fun provideFilterResources(resourceManager: ResourceManager): FilterResources {
        return FilterResources(resourceManager)
    }

    @Provides
    @PerServer
    fun provideProgramStageToWorkingListItemMapper(
        resourceManager: FilterResources,
    ): ProgramStageToWorkingListItemMapper {
        return ProgramStageToWorkingListItemMapper(resourceManager.defaultWorkingListLabel())
    }
}
