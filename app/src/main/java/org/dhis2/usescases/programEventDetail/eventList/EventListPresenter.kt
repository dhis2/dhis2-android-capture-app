package org.dhis2.usescases.programEventDetail.eventList

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.hisp.dhis.android.core.program.Program
import timber.log.Timber

class EventListPresenter(
    val view: EventListFragmentView,
    val filterManager: FilterManager,
    val eventRepository: ProgramEventDetailRepository,
    val preferences: PreferenceProvider,
    val schedulerProvider: SchedulerProvider,
) {

    val disposable = CompositeDisposable()

    fun init() {
        disposable.add(
            filterManager.asFlowable().startWith(filterManager)
                .doOnEach { EventListIdlingResourceSingleton.increment() }
                .map { eventRepository.filteredProgramEvents() }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.setLiveData(it) },
                    { Timber.e(it) },
                ),
        )
    }

    fun program(): Program? {
        return eventRepository.program().blockingGet()
    }
}
