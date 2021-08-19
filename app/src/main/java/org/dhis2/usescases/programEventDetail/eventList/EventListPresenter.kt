package org.dhis2.usescases.programEventDetail.eventList

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.filter.TextFilter
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
    private  var textFilter: TextFilter = TextFilter("","")
    private val filterFlowable: FlowableProcessor<FilterManager> =  filterManager.asFlowableProcessor()


    fun init() {
        disposable.add(
            filterFlowable.startWith(filterManager)
                .map { eventRepository.filteredProgramEvents(textFilter) }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.setLiveData(it) },
                    { Timber.e(it) }
                )
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

    fun program(): Program {
        return eventRepository.program().blockingFirst()
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
