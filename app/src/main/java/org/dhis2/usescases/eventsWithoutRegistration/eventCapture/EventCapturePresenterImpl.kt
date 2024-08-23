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
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.form.data.EventRepository
import org.dhis2.form.model.EventMode
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.usescases.eventsWithoutRegistration.EventIdlingResourceSingleton
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.AutoEnrollmentManager
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model.ExternalEnrollmentCaptureModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCaptureInitialInfo
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus
import timber.log.Timber
import java.util.Date

class EventCapturePresenterImpl(
    private val view: EventCaptureContract.View,
    private val eventUid: String,
    private val eventCaptureRepository: EventCaptureRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val configureEventCompletionDialog: ConfigureEventCompletionDialog,
    private val autoEnrollmentManager: AutoEnrollmentManager
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

    override fun attemptFinish(
        canComplete: Boolean,
        onCompleteMessage: String?,
        errorFields: List<FieldWithIssue>,
        emptyMandatoryFields: Map<String, String>,
        warningFields: List<FieldWithIssue>,
        eventMode: EventMode?,
    ) {
        val eventStatus = eventStatus
        if (eventStatus != EventStatus.ACTIVE) {
            setUpActionByStatus(eventStatus)
        } else {
            val canSkipErrorFix = canSkipErrorFix(
                hasErrorFields = errorFields.isNotEmpty(),
                hasEmptyMandatoryFields = emptyMandatoryFields.isNotEmpty(),
                hasEmptyEventCreationMandatoryFields = with(emptyMandatoryFields) {
                    containsValue(EventRepository.EVENT_DETAILS_SECTION_UID) ||
                        containsValue(EventRepository.EVENT_CATEGORY_COMBO_SECTION_UID)
                },
                eventMode = eventMode,
                validationStrategy = eventCaptureRepository.validationStrategy(),
            )
            val eventCompletionDialog = configureEventCompletionDialog.invoke(
                errorFields,
                emptyMandatoryFields,
                warningFields,
                canComplete,
                onCompleteMessage,
                canSkipErrorFix,
            )
            view.showCompleteActions(eventCompletionDialog)

        }
        view.showNavigationBar()

        compositeDisposable.add(
            Flowable.zip(
                autoEnrollmentManager.getCurrentEventTrackedEntityInstance(eventUid),
                autoEnrollmentManager.getCurrentEventDataValues(eventUid),
                autoEnrollmentManager.getAutoEnrollmentConfiguration(),
                eventCaptureRepository.orgUnit(),
                ::ExternalEnrollmentCaptureModel
            ).defaultSubscribe(schedulerProvider, { external ->
                val teiUid = external.teiUid
                val currentEventDataValues = external.currentEventDataValues
                val targetPrograms = external.configs.configurations.autoEnrollments.targetPrograms
                val userOrgUnit = external.orgUnit.uid()





                targetPrograms.forEach { targetItem ->
                    currentEventDataValues
                        .any {
                            targetItem.constraintsDataElements
                                .any { te -> te.id == it.dataElement() && te.requiredValue == it.value() }
                        }.let {
                            if (it) {
                                autoEnrollmentManager.createEnrollments(
                                    targetItem.ids,
                                    teiUid,
                                    userOrgUnit
                                ).defaultSubscribe(schedulerProvider)
                            }
                        }

                }
            })
        )

    }

    private fun canSkipErrorFix(
        hasErrorFields: Boolean,
        hasEmptyMandatoryFields: Boolean,
        hasEmptyEventCreationMandatoryFields: Boolean,
        eventMode: EventMode?,
        validationStrategy: ValidationStrategy,
    ): Boolean {
        return when (validationStrategy) {
            ValidationStrategy.ON_COMPLETE -> when (eventMode) {
                EventMode.NEW -> !hasEmptyEventCreationMandatoryFields
                else -> true
            }
            ValidationStrategy.ON_UPDATE_AND_INSERT -> !hasErrorFields && !hasEmptyMandatoryFields
        }
    }

    private fun setUpActionByStatus(eventStatus: EventStatus) {
        when (eventStatus) {
            EventStatus.COMPLETED ->
                if (!hasExpired && !eventCaptureRepository.isEnrollmentCancelled) {
                    view.saveAndFinish()
                } else {
                    view.finishDataEntry()
                }

            EventStatus.OVERDUE -> view.attemptToSkip()
            EventStatus.SKIPPED -> view.attemptToReschedule()
            else -> {
                // No actions for the remaining cases
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
                        EventIdlingResourceSingleton.decrement()
                        if (addNew) {
                            view.restartDataEntry()
                        } else {
                            preferences.setValue(Preference.PREF_COMPLETED_EVENT, eventUid)
                            view.finishDataEntry()
                        }
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
}
