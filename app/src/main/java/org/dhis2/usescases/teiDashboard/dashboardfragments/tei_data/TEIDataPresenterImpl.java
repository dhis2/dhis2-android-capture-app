package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;

import com.google.gson.reflect.TypeToken;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.prefs.Preference;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.events.ScheduledEventActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.qrCodes.QrActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModelType;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.Result;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleActionHideProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
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

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.ACTIVE_FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHARE_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_QR;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_SHARE;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
class TEIDataPresenterImpl implements TEIDataContracts.Presenter {

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
    private String programUid;
    private final String teiUid;
    private TEIDataContracts.View view;
    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardModel;
    private String currentStage = null;

    public TEIDataPresenterImpl(TEIDataContracts.View view, D2 d2,
                                DashboardRepository dashboardRepository,
                                TeiDataRepository teiDataRepository,
                                RuleEngineRepository ruleEngineRepository,
                                String programUid, String teiUid, String enrollmentUid,
                                SchedulerProvider schedulerProvider,
                                PreferenceProvider preferenceProvider,
                                AnalyticsHelper analyticsHelper,
                                FilterManager filterManager) {
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
        this.compositeDisposable = new CompositeDisposable();
        this.groupingProcessor = BehaviorProcessor.create();
    }

    @Override
    public void init() {
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

            Flowable<String> sectionFlowable = view.observeStageSelection(
                    d2.programModule().programs().uid(programUid).blockingGet(),
                    d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
            )
                    .startWith("")
                    .map(selectedStage -> {
                        if (!selectedStage.equals(currentStage)) {
                            currentStage = selectedStage;
                            return selectedStage;
                        } else {
                            currentStage = "";
                            return "";
                        }
                    });
            Flowable<Boolean> groupingFlowable = groupingProcessor.startWith(
                    getGrouping().containsKey(programUid) ? getGrouping().get(programUid) : false
            );

            compositeDisposable.add(
                    Flowable.combineLatest(
                            filterManager.asFlowable().startWith(filterManager),
                            sectionFlowable,
                            groupingFlowable,
                            Trio::create)
                            .switchMap(stageAndGrouping ->
                                    Flowable.zip(
                                            teiDataRepository.getTEIEnrollmentEvents(
                                                    stageAndGrouping.val1().isEmpty() ? null : stageAndGrouping.val1(),
                                                    stageAndGrouping.val2(),
                                                    filterManager.getPeriodFilters(),
                                                    filterManager.getOrgUnitUidsFilters(),
                                                    filterManager.getStateFilters(),
                                                    filterManager.getAssignedFilter(),
                                                    filterManager.getEventStatusFilters(),
                                                    filterManager.getCatOptComboFilters()
                                            ).toFlowable(),
                                            ruleEngineRepository.updateRuleEngine()
                                                    .flatMap(ruleEngine -> ruleEngineRepository.reCalculate()),
                                            this::applyEffects)
                            )
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    view.setEvents(),
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
                                periodRequest -> view.showPeriodRequest(periodRequest),
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

    private List<EventViewModel> applyEffects(
            @NonNull List<EventViewModel> events,
            @NonNull Result<RuleEffect> calcResult) {

        Timber.d("APPLYING EFFECTS");

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return events;
        }

        List<String> stagesToHide = new ArrayList<>();
        for (RuleEffect ruleEffect : calcResult.items()) {
            if (ruleEffect.ruleAction() instanceof RuleActionHideProgramStage) {
                RuleActionHideProgramStage hideStageAction =
                        (RuleActionHideProgramStage) ruleEffect.ruleAction();
                stagesToHide.add(hideStageAction.programStage());
            }
        }

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

    @Override
    public void getCatComboOptions(Event event) {
        compositeDisposable.add(
                dashboardRepository.catComboForProgram(event.program())
                        .flatMap(categoryCombo -> dashboardRepository.catOptionCombos(categoryCombo.uid()),
                                Pair::create
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(categoryComboListPair -> {
                                    for (ProgramStage programStage : dashboardModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), categoryComboListPair.val0(), categoryComboListPair.val1());
                                    }
                                },
                                Timber::e));
    }

    @Override
    public void setDefaultCatOptCombToEvent(String eventUid) {
        dashboardRepository.setDefaultCatOptCombToEvent(eventUid);
    }

    @Override
    public void changeCatOption(String eventUid, String catOptionComboUid) {
        dashboardRepository.saveCatOption(eventUid, catOptionComboUid);
    }

    @Override
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

    @Override
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

    @Override
    public void completeEnrollment() {
        if (d2.programModule().programs().uid(programUid).blockingGet().access().data().write()) {
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

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, Boolean.toString(followup), FOLLOW_UP);
        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        view.switchFollowUp(followup);

    }

    @Override
    public void onShareClick(View mView) {
        analyticsHelper.setEvent(TYPE_SHARE, TYPE_QR, SHARE_TEI);
        Intent intent = new Intent(view.getContext(), QrActivity.class);
        intent.putExtra("TEI_UID", teiUid);
        view.showQR(intent);
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        view.seeDetails(EnrollmentActivity.Companion.getIntent(view.getContext(),
                dashboardProgramModel.getCurrentEnrollment().uid(),
                dashboardProgramModel.getCurrentProgram().uid(),
                EnrollmentActivity.EnrollmentMode.CHECK,
                false), options.toBundle());
    }

    @Override
    public void onScheduleSelected(String uid, View sharedView) {
        Intent intent = ScheduledEventActivity.Companion.getIntent(view.getContext(), uid);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
        view.openEventDetails(intent, options.toBundle());
    }

    @Override
    public void onEventSelected(String uid, EventStatus eventStatus, View sharedView) {
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

    @Override
    public void setDashboardProgram(DashboardProgramModel dashboardModel) {
        this.dashboardModel = dashboardModel;
        this.programUid = dashboardModel.getCurrentProgram().uid();
    }

    @Override
    public void setProgram(Program program, String enrollmentUid) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid, teiUid, enrollmentUid);
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

    @Override
    public void onGroupingChanged(Boolean shouldGroup) {
        if (programUid != null) {
            Map<String, Boolean> groups = getGrouping();
            if (shouldGroup) {
                groups.put(programUid, true);
            } else {
                groups.remove(programUid);
            }
            preferences.saveAsJson(Preference.GROUPING, groups);
            groupingProcessor.onNext(shouldGroup);
        }
    }

    @Override
    public void onAddNewEvent(@NonNull View anchor, @NonNull ProgramStage stage) {
        view.showNewEventOptions(anchor, stage);
    }

    @Override
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

    @Override
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
}
