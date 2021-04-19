package org.dhis2.usescases.programEventDetail.eventMap

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.defaultSubscribe
import org.dhis2.usescases.programEventDetail.ProgramEventDetailRepository
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.common.FeatureType
import timber.log.Timber

class EventMapPresenter(
    val view: EventMapFragmentView,
    val filterManager: FilterManager,
    val eventRepository: ProgramEventDetailRepository,
    val preferences: PreferenceProvider,
    val schedulerProvider: SchedulerProvider
) {

    val disposable = CompositeDisposable()
    private val eventInfoProcessor: FlowableProcessor<String> = PublishProcessor.create()

    fun init() {
        disposable.add(
            filterManager.asFlowable().startWith(filterManager)
                .switchMap { eventRepository.filteredEventsForMap() }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.setMap(it) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            eventInfoProcessor
                .flatMap { eventUid: String? -> eventRepository.getInfoForEvent(eventUid) }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.updateEventCarouselItem(it) },
                    { Timber.e(it) }
                )
        )
    }

    fun getEventInfo(eventUid: String) {
        if (preferences.getBoolean(Preference.EVENT_COORDINATE_CHANGED, false)) {
            filterManager.publishData()
        }
        eventInfoProcessor.onNext(eventUid)
    }

    fun programFeatureType(): FeatureType {
        return eventRepository.featureType().blockingGet()
    }

    fun onDestroy() {
        disposable.clear()
    }
}
