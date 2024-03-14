package org.dhis2.commons.filters.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.ProgramStageToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.commons.resources.ResourceManager
import javax.inject.Singleton

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
        resourceManager: FilterResources,
    ): EventFilterToWorkingListItemMapper {
        return EventFilterToWorkingListItemMapper(
            resourceManager.defaultWorkingListLabel(),
        )
    }

    @Provides
    @Singleton
    fun teiWorkingListMapper(resourceManager: FilterResources): TeiFilterToWorkingListItemMapper {
        return TeiFilterToWorkingListItemMapper(
            resourceManager.defaultWorkingListLabel(),
        )
    }

    @Provides
    @Singleton
    fun provideFilterResources(resourceManager: ResourceManager): FilterResources {
        return FilterResources(resourceManager)
    }

    @Provides
    @Singleton
    fun provideProgramStageToWorkingListItemMapper(
        resourceManager: FilterResources,
    ): ProgramStageToWorkingListItemMapper {
        return ProgramStageToWorkingListItemMapper(resourceManager.defaultWorkingListLabel())
    }
}
