package org.dhis2.usescases.programEventDetail.eventList

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.utils.filters.FilterManager

@PerFragment
@Subcomponent(modules = [EventListModule::class])
interface EventListComponent {
    fun inject(fragment: EventListFragment)
}

@Module
class EventListModule(
    val view: EventListFragmentView
) {
    @Provides
    @PerFragment
    fun providePresenter(
        filterManager: FilterManager,
        programEventDetailRepository: ProgramEventDetailRepository,
        preferences: PreferenceProvider,
        schedulers: SchedulerProvider
    ): EventListPresenter {
        return EventListPresenter(
            view,
            filterManager,
            programEventDetailRepository,
            preferences,
            schedulers
        )
    }
}
