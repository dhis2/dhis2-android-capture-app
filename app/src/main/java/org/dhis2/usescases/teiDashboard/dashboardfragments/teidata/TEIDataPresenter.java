package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.ACTIVE_FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.FOLLOW_UP;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityOptionsCompat;

import com.google.gson.reflect.TypeToken;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.EventViewModelType;
import org.dhis2.commons.data.StageSection;
import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.form.data.FormValueStore;
import org.dhis2.form.data.OptionsRepository;
import org.dhis2.form.data.RuleUtilsProviderResult;
import org.dhis2.form.data.RulesUtilsProviderImpl;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.events.ScheduledEventActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.Result;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import timber.log.Timber;

public class TEIDataPresenter {

    private final D2 d2;
    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private final BehaviorProcessor<Boolean> groupingProcessor;
    private final PreferenceProvider preferences;
    private final TeiDataRepository teiDataRepository;
    private final String enrollmentUid;
    private final RuleEngineRepository ruleEngineRepository;
    private final FilterManager filterManager;
    private final String teiUid;
    private final TEIDataContracts.View view;
    private final CompositeDisposable compositeDisposable;
    private final FilterRepository filterRepository;
    private final FormValueStore valueStore;

    private final ResourceManager resources;

    private final OptionsRepository optionsRepository;

    private String programUid;
    private DashboardProgramModel dashboardModel;
    private String currentStage = null;
    private List<String> stagesToHide = Collections.emptyList();

    public TEIDataPresenter(TEIDataContracts.View view, D2 d2,
                            DashboardRepository dashboardRepository,
                            TeiDataRepository teiDataRepository,
                            RuleEngineRepository ruleEngineRepository,
                            String programUid, String teiUid, String enrollmentUid,
                            SchedulerProvider schedulerProvider,
                            PreferenceProvider preferenceProvider,
                            AnalyticsHelper analyticsHelper,
                            FilterManager filterManager,
                            FilterRepository filterRepository,
                            FormValueStore valueStore,
                            ResourceManager resources,
                            OptionsRepository optionsRepository) {
        this.view = view;
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.teiDataRepository = teiDataRepository;
        this.ruleEngineRepository = ruleEngineRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.enrollmentUid = enrollmentUid;
        this.schedulerProvider = schedulerProvider;
        this.preferences = preferenceProvider;
        this.analyticsHelper = analyticsHelper;
        this.filterManager = filterManager;
        this.resources = resources;
        this.compositeDisposable = new CompositeDisposable();
        this.groupingProcessor = BehaviorProcessor.create();
        this.filterRepository = filterRepository;
        this.valueStore = valueStore;
        this.optionsRepository = optionsRepository;
    }

    public void init() {
        compositeDisposable.add(
                filterManager.asFlowable().startWith(filterManager)
                        .flatMap(fManager -> Flowable.just(filterRepository.dashboardFilters(programUid)))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(filters -> {
                                    if (filters.isEmpty()) {
                                        view.hideFilters();
                                    } else {
                                        view.setFilters(filters);
                                    }
                                },
                                Timber::e
                        )
        );
        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get()
                        .map(tei -> {
                                    String defaultIcon = d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet().style().icon();
                                    return Pair.create(
                                            ExtensionsKt.profilePicturePath(tei, d2, programUid),
                                            defaultIcon != null ? defaultIcon : ""
                                    );
                                }
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                fileNameAndDefault -> view.showTeiImage(
                                        fileNameAndDefault.val0(),
                                        fileNameAndDefault.val1()
                                ),
                                Timber::e
                        )
        );

        if (programUid != null) {

            Flowable<StageSection> sectionFlowable = view.observeStageSelection(
                            d2.programModule().programs().uid(programUid).blockingGet(),
                            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
                    )
                    .startWith(new StageSection("", false))
                    .map(selectedStage -> {
                        currentStage = selectedStage.getStageUid().equals(currentStage) && !selectedStage.getShowOptions() ? "" : selectedStage.getStageUid();
                        return new StageSection(currentStage, selectedStage.getShowOptions());
                    });
            Flowable<Boolean> groupingFlowable = groupingProcessor.startWith(hasGrouping(programUid));

            compositeDisposable.add(
                    Flowable.combineLatest(
                                    filterManager.asFlowable().startWith(filterManager),
                                    sectionFlowable,
                                    groupingFlowable,
                                    Trio::create)
                            .doOnNext(data -> TeiDataIdlingResourceSingleton.INSTANCE.increment())
                            .switchMap(stageAndGrouping ->
                                    Flowable.zip(
                                            teiDataRepository.getTEIEnrollmentEvents(
                                                    stageAndGrouping.val1(),
                                                    stageAndGrouping.val2(),
                                                    filterManager.getPeriodFilters(),
                                                    filterManager.getOrgUnitUidsFilters(),
                                                    filterManager.getStateFilters(),
                                                    filterManager.getAssignedFilter(),
                                                    filterManager.getEventStatusFilters(),
                                                    filterManager.getCatOptComboFilters(),
                                                    filterManager.getSortingItem(),
                                                    false
                                            ).toFlowable(),
                                            ruleEngineRepository.updateRuleEngine()
                                                    .flatMap(ruleEngine -> ruleEngineRepository.reCalculate()),
                                            this::applyEffects)
                            )
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    events -> {
                                        view.setEvents(
                                                events,
                                                canAddNewEvents()
                                        );
                                        TeiDataIdlingResourceSingleton.INSTANCE.decrement();
                                    },
                                    Timber::d
                            )
            );

            compositeDisposable.add(
                    Single.zip(
                                    teiDataRepository.getEnrollment(),
                                    teiDataRepository.getEnrollmentProgram(),
                                    Pair::create)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    data -> view.setEnrollmentData(data.val1(), data.val0()),
                                    Timber::e
                            )
            );

            getEventsWithoutCatCombo();

            compositeDisposable.add(FilterManager.getInstance().getCatComboRequest()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                            view::showCatOptComboDialog,
                            Timber::e
                    ));

        } else {
            view.setEnrollmentData(null, null);
        }

        compositeDisposable.add(
                Single.zip(
                                teiDataRepository.getTrackedEntityInstance(),
                                teiDataRepository.enrollingOrgUnit(),
                                Pair::create)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                teiAndOrgUnit ->
                                        view.setTrackedEntityInstance(
                                                teiAndOrgUnit.val0(),
                                                teiAndOrgUnit.val1()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                filterManager.getPeriodRequest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest.getFirst()),
                                Timber::e
                        ));

        compositeDisposable.add(
                filterManager.ouTreeFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                orgUnitRequest -> view.openOrgUnitTreeSelector(programUid),
                                Timber::e
                        ));
    }

    private boolean hasGrouping(String programUid) {
        boolean hasGrouping = true;
        if (getGrouping().containsKey(programUid)) {
            hasGrouping = getGrouping().get(programUid);
        }
        return hasGrouping;
    }

    private List<EventViewModel> applyEffects(
            @NonNull List<EventViewModel> events,
            @NonNull Result<RuleEffect> calcResult) {

        Timber.d("APPLYING EFFECTS");

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            view.showProgramRuleErrorMessage(
                    resources.getString(R.string.error_applying_rule_effects)
            );
            return Collections.emptyList();
        }

        RuleUtilsProviderResult rulesResult = new RulesUtilsProviderImpl(d2, optionsRepository).applyRuleEffects(
                false,
                new HashMap<>(),
                calcResult.items(),
                valueStore);

        stagesToHide = rulesResult.getStagesToHide();

        Iterator<EventViewModel> iterator = events.iterator();
        while (iterator.hasNext()) {
            EventViewModel eventViewModel = iterator.next();
            if (eventViewModel.getType() == EventViewModelType.STAGE && stagesToHide.contains(eventViewModel.getStage().uid())) {
                iterator.remove();
            } else if (eventViewModel.getType() == EventViewModelType.EVENT && stagesToHide.contains(eventViewModel.getEvent().programStage())) {
                iterator.remove();
            }
        }

        return events;
    }

    @VisibleForTesting()
    public void getEventsWithoutCatCombo() {
        compositeDisposable.add(
                teiDataRepository.eventsWithoutCatCombo()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::displayCatComboOptionSelectorForEvents,
                                Timber::e
                        )
        );
    }

    public void changeCatOption(String eventUid, String catOptionComboUid) {
        dashboardRepository.saveCatOption(eventUid, catOptionComboUid);
    }

    public void areEventsCompleted() {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid) : Observable.just(events))
                        .map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.areEventsCompleted(),
                                Timber::d
                        )
        );
    }

    public void displayGenerateEvent(String eventUid) {
        compositeDisposable.add(
                dashboardRepository.displayGenerateEvent(eventUid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.displayGenerateEvent(),
                                Timber::d
                        )
        );
    }

    public void completeEnrollment() {
        if (Boolean.TRUE.equals(d2.programModule().programs().uid(programUid).blockingGet().access().data().write())) {
            compositeDisposable.add(dashboardRepository.completeEnrollment(dashboardModel.getCurrentEnrollment().uid())
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .map(Enrollment::status)
                    .subscribe(
                            view.enrollmentCompleted(),
                            Timber::d
                    )
            );
        } else
            view.displayMessage(null);
    }

    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, Boolean.toString(followup), FOLLOW_UP);
        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        view.switchFollowUp(followup);

    }

    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        view.seeDetails(EnrollmentActivity.Companion.getIntent(view.getContext(),
                dashboardProgramModel.getCurrentEnrollment().uid(),
                dashboardProgramModel.getCurrentProgram().uid(),
                EnrollmentActivity.EnrollmentMode.CHECK,
                false), options.toBundle());
    }

    public void onScheduleSelected(String uid, View sharedView) {
        Intent intent = ScheduledEventActivity.Companion.getIntent(view.getContext(), uid);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
        view.openEventDetails(intent, options.toBundle());
    }

    public void onEventSelected(String uid, EventStatus eventStatus) {
        if (eventStatus == EventStatus.ACTIVE || eventStatus == EventStatus.COMPLETED) {
            Intent intent = new Intent(view.getContext(), EventCaptureActivity.class);
            intent.putExtras(EventCaptureActivity.getActivityBundle(uid, programUid, EventMode.CHECK));
            view.openEventCapture(intent);
        } else {
            Event event = d2.eventModule().events().uid(uid).blockingGet();
            Intent intent = new Intent(view.getContext(), EventInitialActivity.class);
            intent.putExtras(EventInitialActivity.getBundle(
                    programUid, uid, EventCreationType.DEFAULT.name(), teiUid, null, event.organisationUnit(), event.programStage(), dashboardModel.getCurrentEnrollment().uid(), 0, dashboardModel.getCurrentEnrollment().status()
            ));
            view.openEventInitial(intent);
        }
    }

    public void setDashboardProgram(DashboardProgramModel dashboardModel) {
        this.dashboardModel = dashboardModel;
        this.programUid = dashboardModel.getCurrentProgram().uid();
    }

    public void setProgram(Program program, String enrollmentUid) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid, teiUid, enrollmentUid);
    }

    public void onDettach() {
        compositeDisposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    public void showDescription(String description) {
        view.showDescription(description);
    }

    public void onGroupingChanged(Boolean shouldGroupBool) {
        boolean shouldGroup = shouldGroupBool;
        if (programUid != null) {
            Map<String, Boolean> groups = getGrouping();
            groups.put(programUid, shouldGroup);
            preferences.saveAsJson(Preference.GROUPING, groups);
            groupingProcessor.onNext(shouldGroup);
        }
    }

    public void onAddNewEvent(@NonNull View anchor, @NonNull ProgramStage stage) {
        view.showNewEventOptions(anchor, stage);
        if (stage.hideDueDate() != null && stage.hideDueDate()) {
            view.hideDueDate();
        }
    }

    public void getEnrollment(String enrollmentUid) {
        compositeDisposable.add(
                d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(

                                enrollment -> {
                                    view.setEnrollment(enrollment);
                                    filterManager.publishData();
                                },
                                Timber::e
                        )
        );
    }

    public boolean hasAssignment() {
        return !isEmpty(programUid) && !d2.programModule().programStages()
                .byProgramUid().eq(programUid)
                .byEnableUserAssignment().isTrue().blockingIsEmpty();
    }

    private Map<String, Boolean> getGrouping() {
        TypeToken<HashMap<String, Boolean>> typeToken =
                new TypeToken<HashMap<String, Boolean>>() {
                };
        return preferences.getObjectFromJson(
                Preference.GROUPING,
                typeToken,
                new HashMap<>());
    }

    public void onSyncDialogClick(String eventUid) {
        view.showSyncDialog(eventUid, enrollmentUid);
    }

    public boolean enrollmentOrgUnitInCaptureScope(String enrollmentOrgUnit) {
        return !d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(enrollmentOrgUnit)
                .blockingIsEmpty();
    }

    public void setOpeningFilterToNone() {
        filterRepository.collapseAllFilters();
    }

    public void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits);
    }

    private boolean canAddNewEvents() {
        return d2.enrollmentModule()
                .enrollmentService()
                .blockingGetAllowEventCreation(
                        enrollmentUid,
                        stagesToHide
                );
    }

    public String getOrgUnitName(@NotNull String orgUnitUid) {
        return teiDataRepository.getOrgUnitName(orgUnitUid);
    }

    public void filterCatOptCombo(String selectedCatOptionCombo) {
        FilterManager.getInstance().addCatOptCombo(
                dashboardRepository.catOptionCombo(selectedCatOptionCombo)
        );
    }
}
