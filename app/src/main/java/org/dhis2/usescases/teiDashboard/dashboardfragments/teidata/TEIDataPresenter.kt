package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityOptionsCompat
import com.google.gson.reflect.TypeToken
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import org.dhis2.R
import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.usescases.events.ScheduledEventActivity.Companion.getIntent
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity.Companion.getActivityBundle
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton.decrement
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton.increment
import org.dhis2.usescases.teiDashboard.domain.GetNewEventCreationTypeOptions
import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions
import org.dhis2.utils.EventMode
import org.dhis2.utils.Result
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.FOLLOW_UP
import org.dhis2.utils.dialFloatingActionButton.DialItem
import org.hisp.dhis.android.core.D2
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
    private val ruleEngineRepository: RuleEngineRepository,
    private var programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val filterManager: FilterManager,
    private val filterRepository: FilterRepository,
    private val valueStore: FormValueStore,
    private val resources: ResourceManager,
    private val optionsRepository: OptionsRepository,
    private val getNewEventCreationTypeOptions: GetNewEventCreationTypeOptions,
    private val eventCreationOptionsMapper: EventCreationOptionsMapper,
) {
    private val groupingProcessor: BehaviorProcessor<Boolean> = BehaviorProcessor.create()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var dashboardModel: DashboardProgramModel? = null
    private var currentStage: String = ""
    private var stagesToHide: List<String> = emptyList()

    fun init() {
        compositeDisposable.add(
            filterManager.asFlowable().startWith(filterManager)
                .flatMap {
                    Flowable.just(
                        filterRepository.dashboardFilters(programUid!!),
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { filters ->
                        if (filters.isNotEmpty()) {
                            view.setFilters(filters)
                        }
                    },
                    Timber.Forest::e,
                ),
        )
        compositeDisposable.add(
            d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get()
                .map { tei ->
                    val defaultIcon = d2.trackedEntityModule().trackedEntityTypes()
                        .uid(tei.trackedEntityType()).blockingGet()
                        ?.style()?.icon()
                    org.dhis2.commons.data.tuples.Pair.create(
                        tei.profilePicturePath(d2, programUid),
                        defaultIcon ?: "",
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ fileNameAndDefault ->
                    view.showTeiImage(
                        fileNameAndDefault.val0(),
                        fileNameAndDefault.val1(),
                    )
                }, Timber.Forest::e),
        )
        programUid?.let {
            val program = d2.program(it) ?: throw NullPointerException()
            val enrollment = d2.enrollment(enrollmentUid) ?: throw NullPointerException()
            val sectionFlowable = view.observeStageSelection(program, enrollment)
                .startWith(StageSection("", false))
                .map { (stageUid, showOptions) ->
                    currentStage = if (stageUid == currentStage && !showOptions) "" else stageUid
                    StageSection(currentStage, showOptions)
                }
            val groupingFlowable = groupingProcessor.startWith(
                hasGrouping(it),
            )
            compositeDisposable.add(
                Flowable.combineLatest(
                    filterManager.asFlowable().startWith(filterManager),
                    sectionFlowable,
                    groupingFlowable,
                ) { _, stageSelection, isGrooping ->
                    Pair(stageSelection, isGrooping)
                }
                    .doOnNext { increment() }
                    .switchMap { stageAndGrouping ->
                        Flowable.zip(
                            teiDataRepository.getTEIEnrollmentEvents(
                                stageAndGrouping.first,
                                stageAndGrouping.second,
                            ).toFlowable(),
                            ruleEngineRepository.updateRuleEngine()
                                .flatMap { ruleEngineRepository.reCalculate() },
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
                            view.setEvents(events, canAddNewEvents())
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
            compositeDisposable.add(
                FilterManager.getInstance().catComboRequest
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        view::showCatOptComboDialog,
                        Timber.Forest::e,
                    ),
            )
        } ?: view.setEnrollmentData(null, null)

        compositeDisposable.add(
            Single.zip(
                teiDataRepository.getTrackedEntityInstance(),
                teiDataRepository.enrollingOrgUnit(),
            ) { tei, orgUnit ->
                Pair(tei, orgUnit)
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { teiAndOrgUnit ->
                        view.setTrackedEntityInstance(
                            teiAndOrgUnit.first,
                            teiAndOrgUnit.second,
                        )
                    },
                    Timber.Forest::e,
                ),
        )
        compositeDisposable.add(
            filterManager.periodRequest
                .map { it.first }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::showPeriodRequest,
                    Timber.Forest::e,
                ),
        )
        compositeDisposable.add(
            filterManager.ouTreeFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    programUid?.let { it1 -> view.openOrgUnitTreeSelector(it1) }
                }, Timber.Forest::e),
        )
    }

    private fun hasGrouping(programUid: String): Boolean {
        var hasGrouping = true
        if (grouping.containsKey(programUid)) {
            hasGrouping = grouping[programUid] ?: false
        }
        return hasGrouping
    }

    private fun applyEffects(
        events: List<EventViewModel>,
        calcResult: Result<RuleEffect>,
    ): List<EventViewModel> {
        Timber.d("APPLYING EFFECTS")
        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            view.showProgramRuleErrorMessage(
                resources.getString(R.string.error_applying_rule_effects),
            )
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
        return events.filter {
            when (it.type) {
                EventViewModelType.STAGE -> !stagesToHide.contains(it.stage?.uid())
                EventViewModelType.EVENT -> !stagesToHide.contains(it.event?.programStage())
            }
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
                .subscribe(view.displayGenerateEvent(), Timber.Forest::d),
        )
    }

    fun completeEnrollment() {
        val hasWriteAccessInProgram =
            programUid?.let { d2.program(it)?.access()?.data()?.write() } == true
        val currentEnrollmentUid =
            dashboardModel?.currentEnrollment?.uid()
        if (hasWriteAccessInProgram && currentEnrollmentUid != null) {
            compositeDisposable.add(
                dashboardRepository.completeEnrollment(currentEnrollmentUid)
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .map { obj -> obj.status() ?: EnrollmentStatus.ACTIVE }
                    .subscribe(
                        view.enrollmentCompleted(),
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
                    dashboardModel?.currentEnrollment?.uid(),
                    0,
                    dashboardModel?.currentEnrollment?.status(),
                ),
            )
            view.openEventInitial(intent)
        }
    }

    fun setDashboardProgram(dashboardModel: DashboardProgramModel) {
        this.dashboardModel = dashboardModel
        programUid = dashboardModel.currentProgram.uid()
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
            val groups = grouping
            groups[it] = shouldGroupBool
            preferences.saveAsJson<Map<String, Boolean>>(
                Preference.GROUPING,
                groups,
            )
            groupingProcessor.onNext(shouldGroupBool)
        }
    }

    fun getEnrollment(enrollmentUid: String?) {
        compositeDisposable.add(
            d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { enrollment ->
                        enrollment?.let { view.setEnrollment(enrollment) }
                        filterManager.publishData()
                    },
                    Timber.Forest::e,
                ),
        )
    }

    private val grouping: MutableMap<String, Boolean>
        get() {
            val typeToken: TypeToken<HashMap<String, Boolean>> =
                object : TypeToken<HashMap<String, Boolean>>() {}
            return preferences.getObjectFromJson(
                Preference.GROUPING,
                typeToken,
                HashMap(),
            )
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

    fun setOpeningFilterToNone() {
        filterRepository.collapseAllFilters()
    }

    fun setOrgUnitFilters(selectedOrgUnits: List<OrganisationUnit?>?) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits)
    }

    private fun canAddNewEvents(): Boolean {
        return d2.enrollmentModule()
            .enrollmentService()
            .blockingGetAllowEventCreation(
                enrollmentUid,
                stagesToHide,
            )
    }

    fun getOrgUnitName(orgUnitUid: String): String {
        return teiDataRepository.getOrgUnitName(orgUnitUid)
    }

    fun filterCatOptCombo(selectedCatOptionCombo: String?) {
        FilterManager.getInstance().addCatOptCombo(
            dashboardRepository.catOptionCombo(selectedCatOptionCombo),
        )
    }

    fun onAddNewEventOptionSelected(it: EventCreationType, stage: ProgramStage) {
        view.goToEventInitial(it, stage)
    }

    fun getNewEventOptionsByStages(stage: ProgramStage): List<EventCreationOptions> {
        val options = programUid?.let { getNewEventCreationTypeOptions(stage, it) }
        return options?.let { eventCreationOptionsMapper.mapToEventsByStage(it) } ?: emptyList()
    }

    fun newEventOptionsByTimeline(): List<DialItem> {
        val options = programUid?.let { getNewEventCreationTypeOptions.invoke(null, it) }
        return options?.let { eventCreationOptionsMapper.mapToEventsByTimeLine(it) }
            ?: emptyList()
    }

    fun getTeiProfilePath(): String? {
        return teiDataRepository.getTeiProfilePath()
    }

    fun getTeiHeader(): String? {
        return teiDataRepository.getTeiHeader()
    }
}
