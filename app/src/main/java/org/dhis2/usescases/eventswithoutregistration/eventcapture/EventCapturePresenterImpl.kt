package org.dhis2.usescases.eventswithoutregistration.eventcapture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.bindings.canSkipErrorFix
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.usescases.eventswithoutregistration.eventcapture.EventCaptureContract.EventCaptureRepository
import org.dhis2.usescases.eventswithoutregistration.eventcapture.domain.ConfigureEventCompletionDialog
import org.dhis2.usescases.eventswithoutregistration.eventcapture.model.EventCaptureInitialInfo
import org.dhis2.usescases.teidashboard.DashboardProgramModel
import org.dhis2.usescases.teidashboard.DashboardRepository
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber
import java.util.Date

class EventCapturePresenterImpl(
    private val view: EventCaptureContract.View,
    private val eventUid: String,
    private val teiUid: String?,
    private val programUid: String?,
    private val dashboardRepository: DashboardRepository,
    private val eventCaptureRepository: EventCaptureRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val configureEventCompletionDialog: ConfigureEventCompletionDialog,
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
                eventCaptureRepository.eventDate(),
                eventCaptureRepository.orgUnit(),
                eventCaptureRepository.catOption(),
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
                        initialInfo.eventDate,
                        initialInfo.organisationUnit.displayName(),
                        initialInfo.categoryOption,
                    )
                },
                Timber::e,
            ),
        )
        compositeDisposable.add(
            Observable.zip<TrackedEntityInstance, Enrollment, List<ProgramStage?>, List<Event?>, List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>, List<TrackedEntityAttributeValue?>, List<OrganisationUnit?>, List<Program?>, DashboardProgramModel>(
                dashboardRepository.getTrackedEntityInstance(teiUid ?: ""),
                dashboardRepository.getEnrollment(),
                dashboardRepository.getProgramStages(programUid ?: ""),
                dashboardRepository.getTEIEnrollmentEvents(programUid ?: "", teiUid ?: ""),
                dashboardRepository.getAttributesMap(programUid ?: "", teiUid ?: ""),
                dashboardRepository.getTEIAttributeValues(programUid ?: "", teiUid ?: ""),
                dashboardRepository.getTeiOrgUnits(teiUid ?: "", programUid ?: ""),
                dashboardRepository.getTeiActivePrograms(teiUid ?: "", false),
            ) { tei: TrackedEntityInstance?, currentEnrollment: Enrollment?, programStages: List<ProgramStage?>?, events: List<Event?>?, trackedEntityAttributes: List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>, trackedEntityAttributeValues: List<TrackedEntityAttributeValue?>?, orgsUnits: List<OrganisationUnit?>?, enrollmentPrograms: List<Program?>? -> DashboardProgramModel(tei, currentEnrollment, programStages, events, trackedEntityAttributes, trackedEntityAttributeValues, orgsUnits, enrollmentPrograms) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dashboardModel: DashboardProgramModel ->
                        view.setData(dashboardModel)
                    },
                ) { t: Throwable? -> Timber.e(t) },
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
    ) {
        val eventStatus = eventStatus
        if (eventStatus != EventStatus.ACTIVE) {
            setUpActionByStatus(eventStatus)
        } else {
            val validationStrategy = eventCaptureRepository.validationStrategy()
            val canSkipErrorFix = validationStrategy.canSkipErrorFix(
                hasErrorFields = errorFields.isNotEmpty(),
                hasEmptyMandatoryFields = emptyMandatoryFields.isNotEmpty(),
            )
            val eventCompletionDialog = configureEventCompletionDialog.invoke(
                errorFields,
                emptyMandatoryFields,
                warningFields,
                canComplete,
                onCompleteMessage,
                canSkipErrorFix,
            )
            view.showCompleteActions(
                canComplete && eventCaptureRepository.isEnrollmentOpen,
                emptyMandatoryFields,
                eventCompletionDialog,
            )
        }
        view.showNavigationBar()
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

    override fun programStageUid() {
        compositeDisposable.add(
            eventCaptureRepository.programStage().subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result: String? -> view.preselectStage(result) },
                ) { t: Throwable? -> Timber.e(t) },
        )
    }

    override fun getProgramStageUidString(): String? {
        return eventCaptureRepository.programStageUid
    }

    override fun completeEvent(addNew: Boolean) {
        compositeDisposable.add(
            eventCaptureRepository.completeEvent()
                .defaultSubscribe(
                    schedulerProvider,
                    {
                        if (addNew) {
                            view.restartDataEntry()
                        } else {
                            preferences.setValue(Preference.PREF_COMPLETED_EVENT, eventUid)
                            view.finishDataEntry()
                        }
                    },
                    Timber::e,
                ),
        )
    }

    override fun deleteEvent() {
        compositeDisposable.add(
            eventCaptureRepository.deleteEvent()
                .defaultSubscribe(
                    schedulerProvider,
                    { result ->
                        if (result) {
                            view.showSnackBar(R.string.event_was_deleted)
                        }
                    },
                    Timber::e,
                    view::finishDataEntry,
                ),
        )
    }

    override fun skipEvent() {
        compositeDisposable.add(
            eventCaptureRepository.updateEventStatus(EventStatus.SKIPPED)
                .defaultSubscribe(
                    schedulerProvider,
                    { view.showSnackBar(R.string.event_was_skipped) },
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
}
