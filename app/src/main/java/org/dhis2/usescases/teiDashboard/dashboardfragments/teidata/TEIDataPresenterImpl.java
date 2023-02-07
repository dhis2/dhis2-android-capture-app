package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;

import com.google.gson.reflect.TypeToken;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.StageSection;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.form.data.FormValueStore;
import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.events.ScheduledEventActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.commons.data.EventViewModelType;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.Result;
import org.dhis2.form.data.RuleUtilsProviderResult;
import org.dhis2.form.data.RulesUtilsProviderImpl;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.commons.filters.FilterManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.ACTIVE_FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.FOLLOW_UP;

public class TEIDataPresenterImpl implements TEIDataContracts.Presenter {

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

    private String programUid;
    private DashboardProgramModel dashboardModel;
    private String currentStage = null;
    private List<String> stagesToHide;

    public TEIDataPresenterImpl(TEIDataContracts.View view, D2 d2, DashboardRepository dashboardRepository, TeiDataRepository teiDataRepository, RuleEngineRepository ruleEngineRepository, String programUid, String teiUid, String enrollmentUid, SchedulerProvider schedulerProvider, PreferenceProvider preferenceProvider, AnalyticsHelper analyticsHelper, FilterManager filterManager, FilterRepository filterRepository, FormValueStore valueStore) {
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
        this.filterRepository = filterRepository;
        this.valueStore = valueStore;
    }

    @Override
    public void init() {
        compositeDisposable.add(filterManager.asFlowable().startWith(filterManager).flatMap(fManager -> Flowable.just(filterRepository.dashboardFilters(programUid))).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(filters -> {
            if (filters.isEmpty()) {
                view.hideFilters();
            } else {
                view.setFilters(filters);
            }
        }, Timber::e));
        compositeDisposable.add(d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get().map(tei -> {
            String defaultIcon = d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet().style().icon();
            return Pair.create(ExtensionsKt.profilePicturePath(tei, d2, programUid), defaultIcon != null ? defaultIcon : "");
        }).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(fileNameAndDefault -> view.showTeiImage(fileNameAndDefault.val0(), fileNameAndDefault.val1()), Timber::e));

        if (programUid != null) {

            Flowable<StageSection> sectionFlowable = view.observeStageSelection(d2.programModule().programs().uid(programUid).blockingGet(), d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()).startWith(new StageSection("", false)).map(selectedStage -> {
                currentStage = selectedStage.getStageUid().equals(currentStage) && !selectedStage.getShowOptions() ? "" : selectedStage.getStageUid();
                return new StageSection(currentStage, selectedStage.getShowOptions());
            });
            Flowable<Boolean> groupingFlowable = groupingProcessor.startWith(getGrouping().containsKey(programUid) ? getGrouping().get(programUid) : true);

            compositeDisposable.add(Flowable.combineLatest(filterManager.asFlowable().startWith(filterManager), sectionFlowable, groupingFlowable, Trio::create).doOnNext(data -> TeiDataIdlingResourceSingleton.INSTANCE.increment()).switchMap(stageAndGrouping -> Flowable.zip(teiDataRepository.getTEIEnrollmentEvents(stageAndGrouping.val1(), stageAndGrouping.val2(), filterManager.getPeriodFilters(), filterManager.getOrgUnitUidsFilters(), filterManager.getStateFilters(), filterManager.getAssignedFilter(), filterManager.getEventStatusFilters(), filterManager.getCatOptComboFilters(), filterManager.getSortingItem()).toFlowable(), ruleEngineRepository.updateRuleEngine().flatMap(ruleEngine -> ruleEngineRepository.reCalculate()), this::applyEffects)).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(events -> {
                view.setEvents(events, canAddNewEvents());
                TeiDataIdlingResourceSingleton.INSTANCE.decrement();
            }, Timber::d));

            compositeDisposable.add(Single.zip(teiDataRepository.getEnrollment(), teiDataRepository.getEnrollmentProgram(), Pair::create).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(data -> view.setEnrollmentData(data.val1(), data.val0()), Timber::e));
        } else {
            view.setEnrollmentData(null, null);
        }

        System.out.println("this gets executed??");

        view.setAttributeValues(teiDataRepository.getAttributeValues(teiUid));

        compositeDisposable.add(Single.zip(teiDataRepository.getTrackedEntityInstance(), teiDataRepository.enrollingOrgUnit(),
                Pair::create).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(teiAndOrgUnit -> view.setTrackedEntityInstance(teiAndOrgUnit.val0(), teiAndOrgUnit.val1()
        ), Timber::e));

        compositeDisposable.add(filterManager.getPeriodRequest().subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(periodRequest -> view.showPeriodRequest(periodRequest.getFirst()), Timber::e));

        compositeDisposable.add(filterManager.ouTreeFlowable().subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(orgUnitRequest -> view.openOrgUnitTreeSelector(programUid), Timber::e));
    }

    private List<EventViewModel> applyEffects(@NonNull List<EventViewModel> events, @NonNull Result<RuleEffect> calcResult) {

        System.out.println("rule results");
        System.out.println(calcResult.items().get(0));

        Boolean followpuSet = false;

        for (RuleEffect ruleResult : calcResult.items()) {

            System.out.println(ruleResult.ruleId());

            if (ruleResult.ruleId().toString().equals("ra0NoOXo4rW")) {

                System.out.println("----------------------------------");
                System.out.println(ruleResult.ruleAction());
                System.out.println(ruleResult.ruleAction());
                System.out.println(ruleResult.data());

                followpuSet = true;
                onSetSpecificFollowup(this.dashboardModel, followpuSet);

                view.setRiskColor("High Risk");
//                TODO: add checks for output of
//                view.setRiskColor("Low Risk");


            }

        }

        if (!followpuSet) {
            onSetSpecificFollowup(this.dashboardModel, followpuSet);
        }

        Timber.d("APPLYING EFFECTS");

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return events;
        }

        RuleUtilsProviderResult rulesResult = new RulesUtilsProviderImpl(d2).applyRuleEffects(false, new HashMap<>(), calcResult.items(), valueStore);

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

    @Override
    public void getCatComboOptions(Event event) {
        if (dashboardRepository.isStageFromProgram(event.programStage())) {
            compositeDisposable.add(dashboardRepository.catComboForProgram(event.program()).filter(categoryCombo -> categoryCombo.isDefault() != Boolean.TRUE && !categoryCombo.name().equals("default")).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(categoryCombo -> view.showCatComboDialog(event.uid(), event.eventDate() == null ? event.dueDate() : event.eventDate(), categoryCombo.uid()), Timber::e));
        }
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
        compositeDisposable.add(dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid).flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid) : Observable.just(events)).map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED)).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(view.areEventsCompleted(), Timber::d));
    }

    @Override
    public void displayGenerateEvent(String eventUid) {
        compositeDisposable.add(dashboardRepository.displayGenerateEvent(eventUid).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(view.displayGenerateEvent(), Timber::d));
    }

    @Override
    public void completeEnrollment() {
        if (d2.programModule().programs().uid(programUid).blockingGet().access().data().write()) {
            compositeDisposable.add(dashboardRepository.completeEnrollment(dashboardModel.getCurrentEnrollment().uid()).subscribeOn(schedulerProvider.computation()).observeOn(schedulerProvider.ui()).map(Enrollment::status).subscribe(view.enrollmentCompleted(), Timber::d));
        } else view.displayMessage(null);
    }

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {

        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, Boolean.toString(followup), FOLLOW_UP);
        view.showToast(followup ? view.getContext().getString(R.string.follow_up_enabled) : view.getContext().getString(R.string.follow_up_disabled));

        view.switchFollowUp(followup);

    }

    public void onSetSpecificFollowup(DashboardProgramModel dashboardProgramModel, Boolean setFollowup) {


        String enrollmentUid = dashboardProgramModel != null ? dashboardProgramModel.getCurrentEnrollment().uid() : this.enrollmentUid;
        Boolean followUp = dashboardRepository.getFollowupStatus(enrollmentUid);

        if (followUp == null || followUp == false) {
            dashboardRepository.setSpecificFollowupStatus(true, enrollmentUid);
//            view.showToast(followUp ? view.getContext().getString(R.string.follow_up_enabled) : view.getContext().getString(R.string.follow_up_disabled));

            view.switchFollowUp(true);
        }

        System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        System.out.println(setFollowup);
        if (setFollowup == false) {
            dashboardRepository.setSpecificFollowupStatus(false, enrollmentUid);
//            view.showToast(view.getContext().getString(R.string.follow_up_disabled));

            view.switchFollowUp(false);
        }
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        view.seeDetails(EnrollmentActivity.Companion.getIntent(view.getContext(), dashboardProgramModel.getCurrentEnrollment().uid(), dashboardProgramModel.getCurrentProgram().uid(), EnrollmentActivity.EnrollmentMode.CHECK, false), options.toBundle());
    }

    @Override
    public void onScheduleSelected(String uid, View sharedView) {
        Intent intent = ScheduledEventActivity.Companion.getIntent(view.getContext(), uid);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
        view.openEventDetails(intent, options.toBundle());
    }

    @Override
    public void onEventSelected(String uid, EventStatus eventStatus, View sharedView) {

        List<ProgramTrackedEntityAttribute> attributes = dashboardRepository.getProgramTrackedEntityAttributes(programUid).blockingFirst();

        System.out.println("attribures hereree???");
        System.out.println(attributes);

        List<String> listOfAttributeNames = new ArrayList<>();

        for (ProgramTrackedEntityAttribute attribute : attributes) {
            String[] x = attribute.displayName().split("Mother program ");
            String y;

            if (x.length > 1) {
                y = x[1];
            } else {
                y = x[0];
            }

            x = y.split("ANC.A7. ");

            if (x.length > 1) {
                y = x[1];
            } else {
                y = x[0];
            }

            listOfAttributeNames.add(y);
        }

        revlist(listOfAttributeNames);

        // TODO: Softcode arrangement of attribute names to use fetched attributenames
        List<String> f = new ArrayList<>();
        f.add("Unique ID");
        f.add("Given name");
        f.add("Family name");
        f.add("Date of birth");
        f.add("Age");
        f.add("Mobile number");
        f.add("Woman wants to receive reminders during pregnancy");


//        Set<String> attributeNames = new HashSet<>(listOfAttributeNames);
        Set<String> attributeNames = new HashSet<>(f);

        System.out.println("do i get the attributes?");
        System.out.println(attributeNames);

        if (eventStatus == EventStatus.ACTIVE || eventStatus == EventStatus.COMPLETED) {
            Intent intent = new Intent(view.getContext(), EventCaptureActivity.class);
            intent.putExtras(EventCaptureActivity.getActivityBundle(uid, programUid, EventMode.CHECK, teiUid, enrollmentUid, attributeNames));
            view.openEventCapture(intent);
        } else {
            Event event = d2.eventModule().events().uid(uid).blockingGet();
            Intent intent = new Intent(view.getContext(), EventInitialActivity.class);

            ArrayList<String> x = null;
            if (attributeNames != null) {

                System.out.println("hellooooooo");
                System.out.println(attributeNames);
                x = new ArrayList<>(attributeNames);

            }


            intent.putExtras(EventInitialActivity.getBundle(programUid, uid, EventCreationType.DEFAULT.name(), teiUid, null, event.organisationUnit(), event.programStage(), dashboardModel.getCurrentEnrollment().uid(), 0, dashboardModel.getCurrentEnrollment().status(), x));
            view.openEventInitial(intent);
        }
    }

    public static <T> void revlist(List<T> list) {
        // base condition when the list size is 0
        if (list.size() <= 1 || list == null) return;


        T value = list.remove(0);

        // call the recursive function to reverse
        // the list after removing the first element
        revlist(list);

        // now after the rest of the list has been
        // reversed by the upper recursive call,
        // add the first value at the end
        list.add(value);
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
                groups.put(programUid, false);
            }
            preferences.saveAsJson(Preference.GROUPING, groups);
            groupingProcessor.onNext(shouldGroup);
        }
    }

    @Override
    public void onAddNewEvent(@NonNull View anchor, @NonNull ProgramStage stage) {
        view.showNewEventOptions(anchor, stage);
        if (stage.hideDueDate() != null && stage.hideDueDate()) {
            view.hideDueDate();
        }
    }

    @Override
    public void getEnrollment(String enrollmentUid) {
        compositeDisposable.add(d2.enrollmentModule().enrollments().uid(enrollmentUid).get().subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(

                enrollment -> {
                    view.setEnrollment(enrollment);
                    filterManager.publishData();
                }, Timber::e));
    }

    @Override
    public boolean hasAssignment() {
        return !isEmpty(programUid) && !d2.programModule().programStages().byProgramUid().eq(programUid).byEnableUserAssignment().isTrue().blockingIsEmpty();
    }

    private Map<String, Boolean> getGrouping() {
        TypeToken<HashMap<String, Boolean>> typeToken = new TypeToken<HashMap<String, Boolean>>() {
        };
        return preferences.getObjectFromJson(Preference.GROUPING, typeToken, new HashMap<>());
    }

    @Override
    public void onSyncDialogClick() {
        view.showSyncDialog(enrollmentUid);
    }

    @Override
    public boolean enrollmentOrgUnitInCaptureScope(String enrollmentOrgUnit) {
        return !d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).byUid().eq(enrollmentOrgUnit).blockingIsEmpty();
    }

    @Override
    public void setOpeningFilterToNone() {
        filterRepository.collapseAllFilters();
    }

    @Override
    public void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits);
    }

    private boolean canAddNewEvents() {
        return d2.enrollmentModule().enrollmentService().blockingGetAllowEventCreation(enrollmentUid, stagesToHide);
    }
}
