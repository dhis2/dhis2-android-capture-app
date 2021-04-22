package org.dhis2.usescases.programEventDetail.eventList

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.defaultSubscribe
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.program.Program
import timber.log.Timber

class EventListPresenter(
    val view: EventListFragmentView,
    val filterManager: FilterManager,
    val eventRepository: ProgramEventDetailRepository,
    val preferences: PreferenceProvider,
    val schedulerProvider: SchedulerProvider
) {

    val disposable = CompositeDisposable()

    fun init() {
        disposable.add(
            filterManager.asFlowable().startWith(filterManager)
                .map { eventRepository.filteredProgramEvents() }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.setLiveData(it) },
                    { Timber.e(it) }
                )
        )
    }

    fun program(): Program {
        return eventRepository.program().blockingFirst()
    }
}
