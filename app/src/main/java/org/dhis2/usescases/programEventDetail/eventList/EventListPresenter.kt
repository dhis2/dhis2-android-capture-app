package org.dhis2.usescases.programEventDetail.eventList

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import io.reactivex.processors.FlowableProcessor
import org.dhis2.commons.filters.data.TextFilter
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
    private  var textFilter: TextFilter = TextFilter("","")
    private val filterFlowable: FlowableProcessor<FilterManager> =  filterManager.asFlowableProcessor()


    fun init() {
        disposable.add(
            filterFlowable.startWith(filterManager)
                .map { eventRepository.filteredProgramEvents(textFilter) }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.setLiveData(it) },
                    { Timber.e(it) },
                ),
        )

        disposable.add(
            eventRepository.textTypeDataElements()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data ->
                        view.setTextTypeDataElementsFilter(data)
                        textFilter = TextFilter(data.first().uid(),"")
                    },
                    Timber::e
                )
        );
    }

    fun program(): Program? {
        return eventRepository.program().blockingGet()
    }

    fun setTextFilterValue(value: String) {
        textFilter = textFilter.copy(text = value)
        filterFlowable.onNext(filterManager)
    }

    fun setTextFilterDataElement(dataElement: String) {
        textFilter = textFilter.copy(dataElement = dataElement)
        filterFlowable.onNext(filterManager)
    }
}
