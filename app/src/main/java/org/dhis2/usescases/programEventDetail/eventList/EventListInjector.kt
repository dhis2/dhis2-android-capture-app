package org.dhis2.usescases.programEventDetail.eventList

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.usescases.programEventDetail.ProgramEventMapper
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper

@PerFragment
@Subcomponent(modules = [EventListModule::class])
interface EventListComponent {
    fun inject(fragment: EventListFragment)
}

@Module
class EventListModule {
    @Provides
    @PerFragment
    fun providePresenterFactory(
        filterManager: FilterManager,
        programEventDetailRepository: ProgramEventDetailRepository,
        dispatcher: DispatcherProvider,
        mapper: ProgramEventMapper,
        cardMapper: EventCardMapper,
    ): EventListPresenterFactory =
        EventListPresenterFactory(
            filterManager,
            programEventDetailRepository,
            dispatcher,
            mapper,
            cardMapper,
        )
}
