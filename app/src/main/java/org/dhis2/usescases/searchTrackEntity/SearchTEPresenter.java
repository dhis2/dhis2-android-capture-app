package org.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.paging.PagedList;

import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.customviews.OrgUnitDialog;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.maps.GeometryUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment.TEI_A_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.ADD_RELATIONSHIP;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.SEARCH_TEI;

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private static final int MAX_NO_SELECTED_PROGRAM_RESULTS = 5;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private final SearchTEContractsModule.View view;
    private final AnalyticsHelper analyticsHelper;
    private final BehaviorSubject<String> currentProgram;
    private Program selectedProgram;

    private CompositeDisposable compositeDisposable;
    private TrackedEntityType trackedEntity;
    private HashMap<String, String> queryData;

    private Date selectedEnrollmentDate;

    private FlowableProcessor<HashMap<String, String>> queryProcessor;
    private String trackedEntityType;
    private FlowableProcessor<Unit> mapProcessor;
    private FlowableProcessor<Unit> enrollmentMapProcessor;
    private Dialog dialogDisplayed;

    private boolean showList = true;

    public SearchTEPresenter(SearchTEContractsModule.View view,
                             D2 d2,
                             SearchRepository searchRepository,
                             SchedulerProvider schedulerProvider,
                             AnalyticsHelper analyticsHelper,
                             @Nullable String initialProgram) {
        this.view = view;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
        compositeDisposable = new CompositeDisposable();
        queryData = new HashMap<>();
        queryProcessor = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
        enrollmentMapProcessor = PublishProcessor.create();
        selectedProgram = initialProgram != null ? d2.programModule().programs().uid(initialProgram).blockingGet() : null;
        currentProgram = BehaviorSubject.createDefault(initialProgram != null ? initialProgram : "");
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init(String trackedEntityType) {
        this.trackedEntityType = trackedEntityType;

        compositeDisposable.add(
                searchRepository.getTrackedEntityType(trackedEntityType)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.io())
                        .flatMap(trackedEntity -> searchRepository.programsWithRegistration(trackedEntityType)
                                .map(programs -> new kotlin.Pair<>(trackedEntity, programs)))
                        .subscribeOn(schedulerProvider.ui())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(data -> {
                                    this.trackedEntity = data.component1();
                                    List<Program> programs = data.component2();
                                    Collections.sort(programs, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
                                    if (selectedProgram != null) {
                                        setProgram(selectedProgram);
                                        view.setPrograms(programs);
                                    } else {
                                        setProgram(null);
                                        view.setPrograms(programs);
                                    }
                                }, Timber::d
                        ));

        compositeDisposable.add(
                searchRepository.getTrackedEntityType(trackedEntityType)
                        .map(teiType -> teiType.featureType() != null ? teiType.featureType() : FeatureType.NONE)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.featureType(),
                                Timber::d
                        )
        );

        compositeDisposable.add(currentProgram
                .flatMap(programUid -> {
                    if (programUid.isEmpty())
                        return searchRepository.trackedEntityTypeAttributes()
                                .map(attributes -> Pair.create(attributes, new ArrayList<ValueTypeDeviceRendering>()));
                    else
                        return searchRepository.programAttributes(selectedProgram.uid())
                                .map(data -> Pair.create(data.getTrackedEntityAttributes(), data.getRendering()));
                })
                .subscribe(
                        data -> view.setForm(data.val0(), selectedProgram, queryData, data.val1()),
                        Timber::d)
        );

        compositeDisposable.add(view.rowActionss()
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(data -> {
                            Map<String, String> queryDataBU = new HashMap<>(queryData);
                            view.setFabIcon(true);
                            if (!DhisTextUtils.Companion.isEmpty(data.value())) {
                                queryData.put(data.id(), data.value());
                                if (data.requiresExactMatch())
                                    if (data.value().equals("null_os_null"))
                                        queryData.remove(data.id());
                            } else {
                                queryData.remove(data.id());
                            }

                            if (!queryData.equals(queryDataBU)) { //Only when queryData has changed
                                if (!DhisTextUtils.Companion.isEmpty(data.value()))
                                    queryData.put(data.id(), data.value());
                                else
                                    queryData.remove(data.id());
                            }
                        },
                        Timber::d)
        );


        compositeDisposable.add(
                currentProgram.distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST)
                        .switchMap(program ->
                                Flowable.combineLatest(
                                        queryProcessor.startWith(queryData),
                                        FilterManager.getInstance().asFlowable().startWith(FilterManager.getInstance()),
                                        Pair::create
                                ))
                        .onBackpressureLatest()
                        .observeOn(schedulerProvider.io())
                        .filter(data -> !view.isMapVisible())
                        .switchMap(map -> Flowable.just(searchRepository.searchTrackedEntities(
                                selectedProgram,
                                trackedEntityType,
                                FilterManager.getInstance().getOrgUnitUidsFilters(),
                                FilterManager.getInstance().getStateFilters(),
                                FilterManager.getInstance().getEventStatusFilters(),
                                queryData,
                                FilterManager.getInstance().getAssignedFilter(),
                                NetworkUtils.isOnline(view.getContext()))))
                        .doOnError(this::handleError)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setLiveData, Timber::d)
        );

        compositeDisposable.add(
                mapProcessor.switchMap(data ->
                        currentProgram.distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST)
                                .switchMap(program ->
                                        Flowable.combineLatest(
                                                queryProcessor.startWith(queryData),
                                                FilterManager.getInstance().asFlowable().startWith(FilterManager.getInstance()),
                                                Pair::create
                                        )))
                        .onBackpressureLatest()
                        .observeOn(schedulerProvider.io())
                        .filter(data -> view.isMapVisible())
                        .switchMap(unit ->
                                queryProcessor.startWith(queryData)
                                        .observeOn(schedulerProvider.io())
                                        .switchMap(query ->
                                                searchRepository.searchTeiForMap(
                                                        selectedProgram,
                                                        trackedEntityType,
                                                        FilterManager.getInstance().getOrgUnitUidsFilters(),
                                                        FilterManager.getInstance().getStateFilters(),
                                                        FilterManager.getInstance().getEventStatusFilters(),
                                                        query,
                                                        FilterManager.getInstance().getAssignedFilter(),
                                                        NetworkUtils.isOnline(view.getContext())))
                                        .map(GeometryUtils.INSTANCE::getSourceFromTeis)
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                map -> view.setMap(map.component1(), map.component2()),
                                Timber::e,
                                () -> Timber.d("COMPLETED")
                        ));

        compositeDisposable.add(
                queryProcessor
                        .startWith(queryData)
                        .subscribeOn(schedulerProvider.ui())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(data -> view.clearData(), Timber::d)
        );

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable().onBackpressureLatest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );


    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
    }
    //endregion

    //------------------------------------------
    //region DATA
    @Override
    public Trio<PagedList<SearchTeiModel>, String, Boolean> getMessage(PagedList<SearchTeiModel> list) {

        int size = list.size();

        String messageId = "";
        boolean canRegister = false;

        if (selectedProgram != null && !selectedProgram.displayFrontPageList()) {
            if (selectedProgram != null && selectedProgram.minAttributesRequiredToSearch() == 0 && queryData.size() == 0) {
                messageId = view.getContext().getString(R.string.search_attr);
            }
            if (selectedProgram != null && selectedProgram.minAttributesRequiredToSearch() > queryData.size()) {
                messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
            } else if (selectedProgram.maxTeiCountToReturn() != 0 && size > selectedProgram.maxTeiCountToReturn()) {
                messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), selectedProgram.maxTeiCountToReturn());
            } else if (size == 0 && !queryData.isEmpty()) {
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                canRegister = true;
            } else if (size == 0) {
                messageId = view.getContext().getString(R.string.search_init);
            }
        } else if (selectedProgram != null && selectedProgram.displayFrontPageList()) {
            if (!showList && selectedProgram.minAttributesRequiredToSearch() > queryData.size()) {
                messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
            } else if (size == 0) {
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                canRegister = true;
            }
        } else if (selectedProgram == null) {
            if (size == 0 && queryData.isEmpty() && view.fromRelationshipTEI() == null)
                messageId = view.getContext().getString(R.string.search_init);
            else if (size == 0) {
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                canRegister = true;
            } else if (size > MAX_NO_SELECTED_PROGRAM_RESULTS && view.fromRelationshipTEI() == null)
                messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), MAX_NO_SELECTED_PROGRAM_RESULTS);
        } else {
            if (size == 0 && !queryData.isEmpty()) {
                int realQuerySize = queryData.containsKey(Constants.ENROLLMENT_DATE_UID) ? queryData.size() - 1 : queryData.size();
                if (selectedProgram.minAttributesRequiredToSearch() > realQuerySize)
                    messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
                else
                    messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                canRegister = true;
            } else if (size == 0)
                messageId = view.getContext().getString(R.string.search_init);
        }

        if (messageId.isEmpty())
            canRegister = true;

        return Trio.create(list, messageId, canRegister);
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof D2Error) {
            D2Error exception = (D2Error) throwable;
            switch (exception.errorCode()) {
                case UNEXPECTED:
                    view.displayMessage(view.getContext().getString(R.string.online_search_unexpected));
                    break;
                case SEARCH_GRID_PARSE:
                    view.displayMessage(view.getContext().getString(R.string.online_search_parsing_error));
                    break;
                case API_RESPONSE_PROCESS_ERROR:
                    view.displayMessage(view.getContext().getString(R.string.online_search_error));
                    break;
                case API_UNSUCCESSFUL_RESPONSE:
                    view.displayMessage(view.getContext().getString(R.string.online_search_response_error));
                    break;
            }
        }
    }

    @Override
    public TrackedEntityType getTrackedEntityName() {
        return trackedEntity;
    }

    @Override
    public Program getProgram() {
        return selectedProgram;
    }

    //endregion

    @Override
    public void setProgram(Program programSelected) {
        boolean otherProgramSelected = selectedProgram == programSelected;
        selectedProgram = programSelected;
        currentProgram.onNext(programSelected != null ? programSelected.uid() : "");
        view.clearList(programSelected == null ? null : programSelected.uid());
        view.clearData();
        view.setFabIcon(true);
        showList = true;

        if (!otherProgramSelected)
            queryData.clear();

        if (queryData.isEmpty())
            queryProcessor.onNext(new HashMap<>());
        else
            queryProcessor.onNext(queryData);
    }

    @Override
    public void onClearClick() {
        queryData.clear();
        setProgram(selectedProgram);
    }


    //endregion

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onFabClick(boolean needsSearch) {
        if (!needsSearch)
            onEnrollClick();
        else {
            analyticsHelper.setEvent(SEARCH_TEI, CLICK, SEARCH_TEI);
            view.clearData();

            List<String> optionSetIds = new ArrayList<>();
            view.updateFiltersSearch(queryData.entrySet().size());
            for (Map.Entry<String, String> entry : queryData.entrySet()) {
                if (entry.getValue().equals("null_os_null"))
                    optionSetIds.add(entry.getKey());
            }
            for (String id : optionSetIds) {
                queryData.remove(id);
            }

            if (compliesWithMinAttributesToSearch()) {
                view.setFabIcon(false);
                queryProcessor.onNext(queryData);
            } else {
                if (selectedProgram.displayFrontPageList()) {
                    showList = false;
                }
                view.setFabIcon(true);
                queryProcessor.onNext(new HashMap<>());
            }
        }
    }

    private boolean compliesWithMinAttributesToSearch() {
        if (selectedProgram != null) {
            if (selectedProgram.displayFrontPageList() && queryData.size() < selectedProgram.minAttributesRequiredToSearch()) {
                return false;
            } else if (!selectedProgram.displayFrontPageList() && queryData.size() < selectedProgram.minAttributesRequiredToSearch()) {
                return false;
            } else if (!selectedProgram.displayFrontPageList() && queryData.size() == 0 && selectedProgram.minAttributesRequiredToSearch() == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onEnrollClick() {
        if (selectedProgram != null)
            if (selectedProgram.access().data().write() != null && selectedProgram.access().data().write())
                enroll(selectedProgram.uid(), null);
            else
                view.displayMessage(view.getContext().getString(R.string.search_access_error));
        else
            view.displayMessage(view.getContext().getString(R.string.search_program_not_selected));
    }

    private void enroll(String programUid, String uid) {
        selectedEnrollmentDate = Calendar.getInstance().getTime();

        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace().setMultiSelection(false);
        orgUnitDialog.setTitle("Enrollment Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null && !orgUnitDialog.getSelectedOrgUnit().isEmpty())
                        showEnrollmentDatePicker(orgUnitDialog.getSelectedOrgUnitModel(), programUid, uid);
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        compositeDisposable.add(getOrgUnits()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        allOrgUnits -> {
                            if (allOrgUnits.size() > 1) {
                                orgUnitDialog.setOrgUnits(allOrgUnits);
                                orgUnitDialog.setProgram(programUid);
                                if (!orgUnitDialog.isAdded())
                                    orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                            } else if (allOrgUnits.size() == 1)
                                showEnrollmentDatePicker(allOrgUnits.get(0), programUid, uid);
                        },
                        Timber::d
                )
        );
    }

    private void showNativeCalendar(OrganisationUnit selectedOrgUnit, String programUid, String uid) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(view.getContext(), (
                (datePicker, year1, month1, day1) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCalendar.set(Calendar.MINUTE, 0);
                    selectedCalendar.set(Calendar.SECOND, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);
                    selectedEnrollmentDate = selectedCalendar.getTime();

                    enrollInOrgUnit(selectedOrgUnit.uid(), programUid, uid, selectedEnrollmentDate);

                }),
                year,
                month,
                day);

        if (selectedOrgUnit.openingDate() != null)
            dateDialog.getDatePicker().setMinDate(selectedOrgUnit.openingDate().getTime());

        if (selectedOrgUnit.closedDate() == null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        } else if (selectedOrgUnit.closedDate() != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            if (selectedOrgUnit.closedDate().before(new Date(System.currentTimeMillis()))) {
                dateDialog.getDatePicker().setMaxDate(selectedOrgUnit.closedDate().getTime());
            } else {
                dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            }
        } else if (selectedOrgUnit.closedDate() != null && selectedProgram.selectEnrollmentDatesInFuture()) {
            dateDialog.getDatePicker().setMaxDate(selectedOrgUnit.closedDate().getTime());
        }

        dateDialog.setTitle(selectedProgram.enrollmentDateLabel());
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            dialog.dismiss();
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, view.getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                dateDialog.dismiss();
                showCustomCalendar(selectedOrgUnit, programUid, uid);
            });
        }

        dateDialog.show();
    }

    private void showCustomCalendar(OrganisationUnit selectedOrgUnit, String programUid, String uid) {

        if (dialogDisplayed == null || !dialogDisplayed.isShowing()) {
            LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
            WidgetDatepickerBinding binding = WidgetDatepickerBinding.inflate(layoutInflater);
            final DatePicker datePicker = binding.widgetDatepicker;

            Calendar c = Calendar.getInstance();
            datePicker.updateDate(
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext(), R.style.DatePickerTheme)
                    .setTitle(selectedProgram.enrollmentDateLabel());

            if (selectedOrgUnit.openingDate() != null)
                datePicker.setMinDate(selectedOrgUnit.openingDate().getTime());

            if (selectedOrgUnit.closedDate() == null && !selectedProgram.selectEnrollmentDatesInFuture()) {
                datePicker.setMaxDate(System.currentTimeMillis());
            } else if (selectedOrgUnit.closedDate() != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
                if (selectedOrgUnit.closedDate().before(new Date(System.currentTimeMillis()))) {
                    datePicker.setMaxDate(selectedOrgUnit.closedDate().getTime());
                } else {
                    datePicker.setMaxDate(System.currentTimeMillis());
                }
            } else if (selectedOrgUnit.closedDate() != null && selectedProgram.selectEnrollmentDatesInFuture()) {
                datePicker.setMaxDate(selectedOrgUnit.closedDate().getTime());
            }

            alertDialog.setView(binding.getRoot());
            dialogDisplayed = alertDialog.create();

            binding.changeCalendarButton.setOnClickListener(changeButton -> {
                showNativeCalendar(selectedOrgUnit, programUid, uid);
                dialogDisplayed.dismiss();
            });
            binding.clearButton.setOnClickListener(clearButton -> dialogDisplayed.dismiss());
            binding.acceptButton.setOnClickListener(acceptButton -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                selectedCalendar.set(Calendar.MINUTE, 0);
                selectedCalendar.set(Calendar.SECOND, 0);
                selectedCalendar.set(Calendar.MILLISECOND, 0);
                selectedEnrollmentDate = selectedCalendar.getTime();

                enrollInOrgUnit(selectedOrgUnit.uid(), programUid, uid, selectedEnrollmentDate);
                dialogDisplayed.dismiss();
            });
            dialogDisplayed.show();
        }
    }


    private void showEnrollmentDatePicker(OrganisationUnit selectedOrgUnit, String programUid, String uid) {
        showCustomCalendar(selectedOrgUnit, programUid, uid);
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String uid, Date enrollmentDate) {
        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), orgUnitUid, programUid, uid, queryData, enrollmentDate)
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(enrollmentAndTEI -> {
                                    analyticsHelper.setEvent(CREATE_ENROLL, CLICK, CREATE_ENROLL);
                                    view.goToEnrollment(
                                            enrollmentAndTEI.val0(),
                                            selectedProgram.uid()
                                    );
                                },
                                Timber::d)
        );
    }

    @Override
    public void onTEIClick(String TEIuid, String enrollmentUid, boolean isOnline) {
        if (!isOnline) {
            openDashboard(TEIuid, enrollmentUid);
        } else
            downloadTei(TEIuid, enrollmentUid);
    }

    @Override
    public void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online) {
        if (teiUid.equals(view.fromRelationshipTEI())) {
            view.displayMessage(view.getContext().getString(R.string.relationship_error_recursive));
        } else if (!online) {
            analyticsHelper.setEvent(ADD_RELATIONSHIP, CLICK, ADD_RELATIONSHIP);
            Intent intent = new Intent();
            intent.putExtra(TEI_A_UID, teiUid);
            if (relationshipTypeUid != null)
                intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipTypeUid);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            analyticsHelper.setEvent(ADD_RELATIONSHIP, CLICK, ADD_RELATIONSHIP);
            downloadTeiForRelationship(teiUid, relationshipTypeUid);
        }
    }

    @Override
    public void downloadTei(String teiUid, String enrollmentUid) {
        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstanceDownloader()
                        .byUid().in(Collections.singletonList(teiUid))
                        .overwrite(true)
                        .download()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.downloadProgress(),
                                Timber::d,
                                () -> openDashboard(teiUid, enrollmentUid))
        );
    }

    @Override
    public String nameOUByUid(String uid) {
        OrganisationUnit organisationUnit = d2.organisationUnitModule().organisationUnits().uid(uid).blockingGet();
        return organisationUnit != null ? organisationUnit.name() : null;
    }

    @Override
    public void downloadTeiForRelationship(String TEIuid, @Nullable String relationshipTypeUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(TEIuid);
        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstanceDownloader().byUid().in(teiUids).download()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> Timber.d("DOWNLOADING TEI %s : %s%", TEIuid, data.percentage()),
                                Timber::d,
                                () -> addRelationship(TEIuid, relationshipTypeUid, false))
        );
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return searchRepository.getOrgUnits(selectedProgram != null ? selectedProgram.uid() : null);
    }

    private void openDashboard(String teiUid, String enrollmentUid) {
        view.openDashboard(teiUid, selectedProgram != null ? selectedProgram.uid() : null, enrollmentUid);
    }

    @Override
    public String getProgramColor(String uid) {
        return searchRepository.getProgramColor(uid);
    }

    @Override
    public HashMap<String, String> getQueryData() {
        return queryData;
    }

    @Override
    public void onSyncIconClick(String teiUid) {
        view.showSyncDialog(
                new SyncStatusDialog.Builder()
                        .setConflictType(SyncStatusDialog.ConflictType.TEI)
                        .setUid(teiUid)
                        .onDismissListener(hasChanged -> {
                            if (hasChanged && view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                            else if (hasChanged)
                                queryProcessor.onNext(queryData);
                        })
                        .build()
        );
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @Override
    public void showFilterGeneral() {
        view.showHideFilterGeneral();
    }

    @Override
    public void clearFilterClick() {
        view.clearFilters();
    }

    @Override
    public void closeFilterClick() {
        view.closeFilters();
    }

    @Override
    public void getMapData() {
        mapProcessor.onNext(new Unit());
    }

    @Override
    public void getEnrollmentMapData() {
        enrollmentMapProcessor.onNext(new Unit());
    }

    @Override
    public Drawable getSymbolIcon() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().uid(trackedEntityType).blockingGet();

        if (teiType.style() != null && teiType.style().icon() != null) {
            return
                    ObjectStyleUtils.getIconResource(view.getContext(), teiType.style().icon(), R.drawable.mapbox_marker_icon_default);
        } else
            return AppCompatResources.getDrawable(view.getContext(), R.drawable.mapbox_marker_icon_default);
    }

    @Override
    public Drawable getEnrollmentSymbolIcon() {
        if (selectedProgram != null) {
            if (selectedProgram.style() != null && selectedProgram.style().icon() != null) {
                return ObjectStyleUtils.getIconResource(view.getContext(), selectedProgram.style().icon(), R.drawable.ic_program_default);
            } else
                return AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_program_default);
        }

        return null;
    }

    @Override
    public int getTEIColor() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().uid(trackedEntityType).blockingGet();

        if (teiType.style() != null && teiType.style().color() != null) {
            return ColorUtils.parseColor(teiType.style().color());
        } else
            return -1;
    }

    @Override
    public int getEnrollmentColor() {
        if (selectedProgram != null && selectedProgram.style() != null && selectedProgram.style().color() != null)
            return ColorUtils.parseColor(selectedProgram.style().color());
        else
            return -1;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void setProgramForTesting(Program program) {
        selectedProgram = program;
    }
}
