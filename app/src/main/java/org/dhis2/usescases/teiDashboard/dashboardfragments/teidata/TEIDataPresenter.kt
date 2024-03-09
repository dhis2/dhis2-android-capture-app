package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.bindings.canCreateEventInEnrollment
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.usescases.events.ScheduledEventActivity.Companion.getIntent
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity.Companion.getActivityBundle
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton.decrement
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton.increment
import org.dhis2.usescases.teiDashboard.domain.GetNewEventCreationTypeOptions
import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions
import org.dhis2.utils.EventMode
import org.dhis2.utils.Result
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CREATE_EVENT_TEI
import org.dhis2.utils.analytics.FOLLOW_UP
import org.dhis2.utils.analytics.TYPE_EVENT_TEI
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class TEIDataPresenter(
    private val view: TEIDataContracts.View,
    private val d2: D2,
    private val dashboardRepository: DashboardRepository,
    private val teiDataRepository: TeiDataRepository,
    private val ruleEngineHelper: RuleEngineHelper?,
    private var programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val schedulerProvider: SchedulerProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val valueStore: FormValueStore,
    private val optionsRepository: OptionsRepository,
    private val getNewEventCreationTypeOptions: GetNewEventCreationTypeOptions,
    private val eventCreationOptionsMapper: EventCreationOptionsMapper,
    private val contractHandler: TeiDataContractHandler,
    private val dashboardActivityPresenter: TeiDashboardContracts.Presenter,
) {
    private val groupingProcessor: BehaviorProcessor<Boolean> = BehaviorProcessor.create()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var currentStage: String = ""
    private var stagesToHide: List<String> = emptyList()

    private val _shouldDisplayEventCreationButton = MutableLiveData(false)
    val shouldDisplayEventCreationButton: LiveData<Boolean> = _shouldDisplayEventCreationButton

    private val _events: MutableLiveData<List<EventViewModel>> = MutableLiveData()
    val events: LiveData<List<EventViewModel>> = _events

    fun init() {
        programUid?.let {
            val program = d2.program(it) ?: throw NullPointerException()
            val enrollment = d2.enrollment(enrollmentUid) ?: throw NullPointerException()
            val sectionFlowable = view.observeStageSelection(program, enrollment)
                .startWith(StageSection("", false, false))
                .map { (stageUid, showOptions, showAllEvents) ->
                    currentStage = if (stageUid == currentStage && !showOptions) "" else stageUid
                    StageSection(currentStage, showOptions, showAllEvents)
                }
            val programHasGrouping = dashboardRepository.getGrouping()
            val groupingFlowable = groupingProcessor.startWith(programHasGrouping)

            compositeDisposable.add(
                Flowable.combineLatest<StageSection?, Boolean?, Pair<StageSection, Boolean>>(
                    sectionFlowable,
                    groupingFlowable,
                    ::Pair,
                )
                    .doOnNext { increment() }
                    .switchMap { stageAndGrouping ->
                        Flowable.zip(
                            teiDataRepository.getTEIEnrollmentEvents(
                                stageAndGrouping.first,
                                stageAndGrouping.second,
                            ).toFlowable(),
                            Flowable.fromCallable {
                                ruleEngineHelper?.refreshContext()
                                (ruleEngineHelper?.evaluate() ?: emptyList())
                                    .let { ruleEffects -> Result.success(ruleEffects) }
                            },
                        ) { events, calcResult ->
                            applyEffects(
                                events,
                                calcResult,
                            )
                        }
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { events ->
                            _events.postValue(events)
                            decrement()
                        },
                        Timber.Forest::d,
                    ),
            )
            compositeDisposable.add(
                Single.zip(
                    teiDataRepository.getEnrollment(),
                    teiDataRepository.getEnrollmentProgram(),
                ) { val0, val1 -> Pair(val0, val1) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe({ data ->
                        view.setEnrollmentData(
                            data.second,
                            data.first,
                        )
                    }, Timber.Forest::e),
            )
            getEventsWithoutCatCombo()
        } ?: run {
            view.setEnrollmentData(null, null)
            _shouldDisplayEventCreationButton.value = false
        }

        updateCreateEventButtonVisibility(dashboardRepository.getGrouping())
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateCreateEventButtonVisibility(isGrouping: Boolean) {
        val enrollment = d2.enrollment(enrollmentUid)
        val showButton =
            enrollment != null &&
                !isGrouping && enrollment.status() == EnrollmentStatus.ACTIVE &&
                canAddNewEvents()
        _shouldDisplayEventCreationButton.value = showButton
    }

    private fun applyEffects(
        events: List<EventViewModel>,
        calcResult: Result<RuleEffect>,
    ): List<EventViewModel> {
        Timber.d("APPLYING EFFECTS")
        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            view.showProgramRuleErrorMessage()
            return emptyList()
        }
        val (_, _, _, _, _, _, stagesToHide1) = RulesUtilsProviderImpl(
            d2,
            optionsRepository,
        ).applyRuleEffects(
            false,
            HashMap(),
            calcResult.items(),
            valueStore,
        )
        stagesToHide = stagesToHide1
        return events.mapNotNull {
            it.applyHideStage(stagesToHide.contains(it.stage?.uid()))
        }
    }

    @VisibleForTesting
    fun getEventsWithoutCatCombo() {
        compositeDisposable.add(
            teiDataRepository.eventsWithoutCatCombo()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::displayCatComboOptionSelectorForEvents,
                    Timber.Forest::e,
                ),
        )
    }

    fun changeCatOption(eventUid: String?, catOptionComboUid: String?) {
        dashboardRepository.saveCatOption(eventUid, catOptionComboUid)
    }

    fun areEventsCompleted() {
        compositeDisposable.add(
            dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid)
                .flatMap { events ->
                    if (events.isEmpty()) {
                        dashboardRepository.getTEIEnrollmentEvents(
                            programUid,
                            teiUid,
                        )
                    } else {
                        Observable.just(events)
                    }
                }
                .map { events ->
                    Observable.fromIterable(events)
                        .all { event -> event.status() == EventStatus.COMPLETED }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view.areEventsCompleted(),
                    Timber.Forest::d,
                ),
        )
    }

    fun displayGenerateEvent(eventUid: String?) {
        compositeDisposable.add(
            dashboardRepository.displayGenerateEvent(eventUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ programStage ->
                    if (programStage.displayGenerateEventBox() == true || programStage.allowGenerateNextVisit() == true) {
                        view.displayScheduleEvent()
                    } else if (programStage.remindCompleted() == true) {
                        view.showDialogCloseProgram()
                    }
                }, Timber.Forest::d),
        )
    }

    fun completeEnrollment() {
        val hasWriteAccessInProgram =
            programUid?.let { d2.program(it)?.access()?.data()?.write() } == true

        if (hasWriteAccessInProgram) {
            compositeDisposable.add(
                Completable.fromCallable {
                    dashboardRepository.completeEnrollment(enrollmentUid).blockingFirst()
                }
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        {},
                        Timber.Forest::d,
                    ),
            )
        } else {
            view.displayMessage(null)
        }
    }

    fun onFollowUp(dashboardProgramModel: DashboardProgramModel) {
        val followup =
            dashboardRepository.setFollowUp(dashboardProgramModel.currentEnrollment.uid())
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, java.lang.Boolean.toString(followup), FOLLOW_UP)
        view.showToast(
            if (followup) {
                view.context.getString(R.string.follow_up_enabled)
            } else {
                view.context.getString(
                    R.string.follow_up_disabled,
                )
            },
        )
        view.switchFollowUp(followup)
    }

    fun onEventCreationClick(eventCreationId: Int) {
        createEventInEnrollment(eventCreationOptionsMapper.getActionType(eventCreationId))
    }

    fun onAcceptScheduleNewEvent(stageStandardInterval: Int) {
        createEventInEnrollment(EventCreationType.SCHEDULE, stageStandardInterval)
    }

    private fun createEventInEnrollment(
        eventCreationType: EventCreationType,
        scheduleIntervalDays: Int = 0,
    ) {
        analyticsHelper.setEvent(TYPE_EVENT_TEI, eventCreationType.name, CREATE_EVENT_TEI)
        val bundle = Bundle()
        bundle.putString(
            Constants.PROGRAM_UID,
            programUid,
        )
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid)
        teiDataRepository.getEnrollment().blockingGet()?.organisationUnit()
            ?.takeIf { enrollmentOrgUnitInCaptureScope(it) }?.let {
                bundle.putString(Constants.ORG_UNIT, it)
            }

        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid)
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays)
        val intent = Intent(view.context, ProgramStageSelectionActivity::class.java)
        intent.putExtras(bundle)
        contractHandler.createEvent(intent).observe(view.viewLifecycleOwner()) {
            view.updateEnrollment(true)
        }
    }

    fun onScheduleSelected(uid: String?, sharedView: View?) {
        uid?.let {
            val intent = getIntent(view.context, uid)
            val options = sharedView?.let { it1 ->
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    view.abstractActivity,
                    it1,
                    "shared_view",
                )
            } ?: ActivityOptionsCompat.makeBasic()
            view.openEventDetails(intent, options)
        }
    }

    fun onEventSelected(uid: String, eventStatus: EventStatus) {
        if (eventStatus == EventStatus.ACTIVE || eventStatus == EventStatus.COMPLETED) {
            val intent = Intent(view.context, EventCaptureActivity::class.java)
            intent.putExtras(
                getActivityBundle(
                    eventUid = uid,
                    programUid = programUid ?: throw IllegalStateException(),
                    eventMode = EventMode.CHECK,
                ),
            )
            view.openEventCapture(intent)
        } else {
            val event = d2.event(uid)
            val intent = Intent(view.context, EventInitialActivity::class.java)
            intent.putExtras(
                EventInitialActivity.getBundle(
                    programUid,
                    uid,
                    EventCreationType.DEFAULT.name,
                    teiUid,
                    null,
                    event?.organisationUnit(),
                    event?.programStage(),
                    enrollmentUid,
                    0,
                    teiDataRepository.getEnrollment().blockingGet()?.status(),
                ),
            )
            view.openEventInitial(intent)
        }
    }

    fun setProgram(program: Program, enrollmentUid: String?) {
        program.uid()?.let { uid ->
            programUid = uid
            enrollmentUid?.let { view.restoreAdapter(uid, teiUid, it) }
        }
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun showDescription(description: String?) {
        view.showDescription(description)
    }

    fun onGroupingChanged(shouldGroupBool: Boolean) {
        programUid?.let {
            groupingProcessor.onNext(shouldGroupBool)
            updateCreateEventButtonVisibility(shouldGroupBool)
        }
    }

    fun onSyncDialogClick(eventUid: String) {
        view.showSyncDialog(eventUid, enrollmentUid)
    }

    fun enrollmentOrgUnitInCaptureScope(enrollmentOrgUnit: String): Boolean {
        return !d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byUid().eq(enrollmentOrgUnit)
            .blockingIsEmpty()
    }

    private fun canAddNewEvents(): Boolean {
        return d2.canCreateEventInEnrollment(enrollmentUid, stagesToHide)
    }

    fun getOrgUnitName(orgUnitUid: String): String {
        return teiDataRepository.getOrgUnitName(orgUnitUid)
    }

    fun onAddNewEventOptionSelected(it: EventCreationType, stage: ProgramStage?) {
        if (stage != null) {
            view.goToEventInitial(it, stage)
        } else {
            createEventInEnrollment(it)
        }
    }

    fun getNewEventOptionsByStages(stage: ProgramStage?): List<EventCreationOptions> {
        val options = programUid?.let { getNewEventCreationTypeOptions(stage, it) }
        return options?.let { eventCreationOptionsMapper.mapToEventsByStage(it) } ?: emptyList()
    }

    fun fetchEvents(updateEnrollment: Boolean) {
        if (updateEnrollment) {
            groupingProcessor.onNext(dashboardRepository.getGrouping())
        }
    }

    fun getEnrollment(): Enrollment? {
        return teiDataRepository.getEnrollment().blockingGet()
    }

    fun filterAvailableStages(programStages: List<ProgramStage>): List<ProgramStage> =
        programStages
            .filter { it.access().data().write() }
            .filter { !stagesToHide.contains(it.uid()) }
            .filter { stage ->
                stage.repeatable() == true ||
                    events.value?.none { event ->
                        event.stage?.uid() == stage.uid() &&
                            event.type == EventViewModelType.EVENT
                    } == true
            }.sortedBy { stage -> stage.sortOrder() }

    fun isEventEditable(eventUid: String): Boolean {
        return teiDataRepository.isEventEditable(eventUid)
    }

    fun displayOrganisationUnit(): Boolean {
        return programUid?.let {
            teiDataRepository.displayOrganisationUnit(it)
        } ?: false
    }
}
