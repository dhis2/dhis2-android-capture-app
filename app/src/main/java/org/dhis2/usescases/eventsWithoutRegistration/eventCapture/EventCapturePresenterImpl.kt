package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.usescases.eventsWithoutRegistration.EventIdlingResourceSingleton
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCaptureInitialInfo
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.event.EventStatus
import timber.log.Timber
import java.util.Date

class EventCapturePresenterImpl(
    private val view: EventCaptureContract.View,
    private val eventUid: String,
    private val eventCaptureRepository: EventCaptureRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val pageConfigurator: NavigationPageConfigurator,
    private val resourceManager: ResourceManager,
) : ViewModel(), EventCaptureContract.Presenter {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var hasExpired = false
    private val notesCounterProcessor: PublishProcessor<Unit> = PublishProcessor.create()

    val actions = MutableLiveData<EventCaptureAction>()

    override fun observeActions(): LiveData<EventCaptureAction> = actions

    override fun emitAction(onBack: EventCaptureAction) {
        actions.value = onBack
    }

    override fun init() {
        compositeDisposable.add(
            eventCaptureRepository.eventIntegrityCheck()
                .filter { check -> !check }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.showEventIntegrityAlert() },
                    Timber::e,
                ),
        )
        compositeDisposable.add(
            Flowable.zip(
                eventCaptureRepository.programStageName(),
                eventCaptureRepository.orgUnit(),
                ::EventCaptureInitialInfo,
            ).defaultSubscribe(
                schedulerProvider,
                { initialInfo ->
                    preferences.setValue(
                        Preference.CURRENT_ORG_UNIT,
                        initialInfo.organisationUnit.uid(),
                    )
                    view.renderInitialInfo(
                        initialInfo.programStageName,
                    )
                },
                Timber::e,
            ),
        )
        checkExpiration()
    }

    private fun checkExpiration() {
        if (eventStatus == EventStatus.COMPLETED) {
            compositeDisposable.add(
                Flowable.fromCallable {
                    val isCompletedEventExpired =
                        eventCaptureRepository.isCompletedEventExpired(eventUid).blockingFirst()
                    val isEventEditable = eventCaptureRepository.isEventEditable(eventUid)
                    isCompletedEventExpired && !isEventEditable
                }
                    .defaultSubscribe(
                        schedulerProvider,
                        this::setHasExpired,
                        Timber::e,
                    ),
            )
        } else {
            setHasExpired(!eventCaptureRepository.isEventEditable(eventUid))
        }
    }

    private fun setHasExpired(expired: Boolean) {
        hasExpired = expired
    }

    override fun onBackClick() {
        view.goBack()
    }

    override fun saveAndExit(eventStatus: EventStatus?) {
        when (eventStatus) {
            EventStatus.OVERDUE -> {
                view.attemptToSkip()
            }
            EventStatus.SKIPPED -> {
                view.attemptToReschedule()
            }
            else -> {
                if (!hasExpired && !eventCaptureRepository.isEnrollmentCancelled) {
                    view.saveAndFinish()
                } else {
                    view.finishDataEntry()
                }
            }
        }
    }

    override fun isEnrollmentOpen(): Boolean {
        return eventCaptureRepository.isEnrollmentOpen
    }

    override fun completeEvent(addNew: Boolean) {
        EventIdlingResourceSingleton.increment()
        compositeDisposable.add(
            eventCaptureRepository.completeEvent()
                .defaultSubscribe(
                    schedulerProvider,
                    onNext = {
                        if (addNew) {
                            view.restartDataEntry()
                        } else {
                            preferences.setValue(Preference.PREF_COMPLETED_EVENT, eventUid)
                            view.finishDataEntry()
                        }
                        EventIdlingResourceSingleton.decrement()
                    },
                    onError = {
                        EventIdlingResourceSingleton.decrement()
                        Timber.e(it)
                    },
                ),
        )
    }

    override fun deleteEvent() {
        val programStage = programStage()
        EventIdlingResourceSingleton.increment()
        compositeDisposable.add(
            eventCaptureRepository.deleteEvent()
                .defaultSubscribe(
                    schedulerProvider,
                    onNext = { result ->
                        EventIdlingResourceSingleton.decrement()
                        if (result) {
                            view.showSnackBar(R.string.event_label_was_deleted, programStage)
                        }
                    },
                    onError = {
                        EventIdlingResourceSingleton.decrement()
                        Timber.e(it)
                    },
                    onComplete = {
                        EventIdlingResourceSingleton.decrement()
                        view.finishDataEntry()
                    },
                ),
        )
    }

    override fun skipEvent() {
        compositeDisposable.add(
            eventCaptureRepository.updateEventStatus(EventStatus.SKIPPED)
                .defaultSubscribe(
                    schedulerProvider,
                    { view.showSnackBar(R.string.event_label_was_skipped, programStage()) },
                    Timber::e,
                    view::finishDataEntry,
                ),
        )
    }

    override fun rescheduleEvent(time: Date) {
        compositeDisposable.add(
            eventCaptureRepository.rescheduleEvent(time)
                .defaultSubscribe(
                    schedulerProvider,
                    { view.finishDataEntry() },
                    Timber::e,
                ),
        )
    }

    override fun canWrite(): Boolean {
        return eventCaptureRepository.accessDataWrite
    }

    override fun hasExpired(): Boolean {
        return hasExpired
    }

    override fun onDettach() {
        compositeDisposable.clear()
    }

    override fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    override fun initNoteCounter() {
        if (!notesCounterProcessor.hasSubscribers()) {
            compositeDisposable.add(
                notesCounterProcessor.startWith(Unit())
                    .flatMapSingle { eventCaptureRepository.noteCount }
                    .defaultSubscribe(
                        schedulerProvider,
                        view::updateNoteBadge,
                        Timber::e,
                    ),
            )
        } else {
            notesCounterProcessor.onNext(Unit())
        }
    }

    override fun refreshTabCounters() {
        initNoteCounter()
    }

    override fun hideProgress() {
        view.hideProgress()
    }

    override fun showProgress() {
        view.showProgress()
    }

    override fun getCompletionPercentageVisibility(): Boolean {
        return eventCaptureRepository.showCompletionPercentage()
    }

    private val eventStatus: EventStatus
        get() = eventCaptureRepository.eventStatus().blockingFirst()

    override fun programStage(): String = eventCaptureRepository.programStage().blockingFirst()

    override fun getEnrollmentUid(): String? {
        return eventCaptureRepository.getEnrollmentUid()
    }
    override fun getTeiUid(): String? {
        return eventCaptureRepository.getTeiUid()
    }
}
