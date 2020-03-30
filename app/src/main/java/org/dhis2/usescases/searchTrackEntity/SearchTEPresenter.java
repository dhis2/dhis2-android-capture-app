package org.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.paging.PagedList;

import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.customviews.OrgUnitDialog;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.maps.GeometryUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Unit;
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

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.ADD_RELATIONSHIP;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.SEARCH_TEI;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private static final int MAX_NO_SELECTED_PROGRAM_RESULTS = 5;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private final SearchTEContractsModule.View view;
    private Program selectedProgram;

    private CompositeDisposable compositeDisposable;
    private TrackedEntityType trackedEntity;
    private HashMap<String, String> queryData;

    private Date selectedEnrollmentDate;

    private FlowableProcessor<HashMap<String, String>> queryProcessor;
    private String trackedEntityType;
    private String initialProgram;
    private FlowableProcessor<Unit> mapProcessor;
    private FlowableProcessor<Unit> enrollmentMapProcessor;
    private Dialog dialogDisplayed;

    private boolean showList = true;

    public SearchTEPresenter(SearchTEContractsModule.View view,
                             D2 d2,
                             SearchRepository searchRepository,
                             SchedulerProvider schedulerProvider,
                             @Nullable String initialProgram) {
        this.view = view;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
        this.initialProgram = initialProgram;
        compositeDisposable = new CompositeDisposable();
        queryData = new HashMap<>();
        queryProcessor = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
        enrollmentMapProcessor = PublishProcessor.create();
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
                        .flatMap(trackedEntity ->
                        {
                            this.trackedEntity = trackedEntity;
                            return searchRepository.programsWithRegistration(trackedEntityType);
                        })
                        .subscribeOn(schedulerProvider.ui())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(programs -> {

                                    List<Program> programsWithTEType = new ArrayList<>();
                                    for (Program program : programs) {
                                        if (program.trackedEntityType().equals(trackedEntityType))
                                            programsWithTEType.add(program);
                                        if (program.uid().equals(initialProgram))
                                            this.selectedProgram = program;
                                    }
                                    Collections.sort(programs, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
                                    if (selectedProgram == null && programsWithTEType.size() == 1) {
                                        setProgram(programsWithTEType.get(0));
                                        view.setPrograms(programsWithTEType);
                                    } else if (selectedProgram != null) {
                                        setProgram(selectedProgram);
                                        view.setPrograms(programs);
                                    } else {
                                        setProgram(null);
                                        view.setPrograms(programs);
                                    }
                                    startFilterManager();

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

        compositeDisposable.add(
                mapProcessor
                        .flatMap(unit ->
                                queryProcessor.startWith(queryData)
                                        .flatMap(query ->
                                                searchRepository.searchTeiForMap(
                                                        selectedProgram, trackedEntityType,
                                                        FilterManager.getInstance().getOrgUnitUidsFilters(),
                                                        FilterManager.getInstance().getStateFilters(),
                                                        query, NetworkUtils.isOnline(view.getContext())))
                                        .map(GeometryUtils.INSTANCE::getSourceFromTeis)
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.setMap(),
                                Timber::e,
                                () -> Timber.d("COMPLETED")
                        ));

    }

    @Override
    public void initSearch() {

        compositeDisposable.add(view.rowActionss()
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(data -> {
                            Map<String, String> queryDataBU = new HashMap<>(queryData);
                            view.setFabIcon(true);
                            if (!isEmpty(data.value())) {
                                queryData.put(data.id(), data.value());
                                if (data.requiresExactMatch())
                                    if (data.value().equals("null_os_null"))
                                        queryData.remove(data.id());
                            } else {
                                queryData.remove(data.id());
                            }

                            if (!queryData.equals(queryDataBU)) { //Only when queryData has changed
                                if (!isEmpty(data.value()))
                                    queryData.put(data.id(), data.value());
                                else
                                    queryData.remove(data.id());
                            }
                        },
                        Timber::d)
        );

        compositeDisposable.add(
                queryProcessor
                        .doOnNext(queryData -> {
                            if (view.isMapVisible() && selectedProgram != null)
                                mapProcessor.onNext(new Unit());
                        })
                        .map(map -> {
                            HashMap<String, String> data = new HashMap<>(map);
                            return searchRepository.searchTrackedEntities(
                                    selectedProgram, trackedEntityType,
                                    FilterManager.getInstance().getOrgUnitUidsFilters(),
                                    FilterManager.getInstance().getStateFilters(),
                                    FilterManager.getInstance().getEventStatusFilters(),
                                    data, NetworkUtils.isOnline(view.getContext()));
                        })
                        .doOnError(this::handleError)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setLiveData, Timber::d)
        );

        compositeDisposable.add(
                queryProcessor
                        .startWith(queryData)
                        .subscribeOn(schedulerProvider.ui())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(data -> view.clearData(), Timber::d)
        );

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
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
        } else  if (selectedProgram != null && selectedProgram.displayFrontPageList()) {
            if (!showList && selectedProgram.minAttributesRequiredToSearch() > queryData.size()) {
                messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
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

//        Crashlytics.logException(throwable);
    }

    private void getTrackedEntityTypeAttributes() {
        compositeDisposable.add(searchRepository.trackedEntityTypeAttributes()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        data -> view.setForm(data, selectedProgram, queryData),
                        Timber::d)
        );
    }

    private void getProgramTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes(selectedProgram.uid())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        data -> view.setForm(data, selectedProgram, queryData),
                        Timber::d)
        );
    }

    private void startFilterManager() {
        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .startWith(FilterManager.getInstance())
                        .map(filterManager -> searchRepository.searchTrackedEntities(
                                selectedProgram, trackedEntityType,
                                filterManager.getOrgUnitUidsFilters(),
                                filterManager.getStateFilters(),
                                filterManager.getEventStatusFilters(),
                                queryData, NetworkUtils.isOnline(view.getContext())))
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setLiveData,
                                Timber::d)
        );
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
        view.clearList(programSelected == null ? null : programSelected.uid());
        view.clearData();
        view.setFabIcon(true);

        if (selectedProgram == null)
            getTrackedEntityTypeAttributes();
        else
            getProgramTrackedEntityAttributes();

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
    public void onFabClick(View view, boolean needsSearch) {
        if (!needsSearch)
            onEnrollClick(view);
        else {
            this.view.analyticsHelper().setEvent(SEARCH_TEI, CLICK, SEARCH_TEI);
            this.view.clearData();

            List<String> optionSetIds = new ArrayList<>();
            this.view.updateFiltersSearch(queryData.entrySet().size());
            for (Map.Entry<String, String> entry : queryData.entrySet()) {
                if (entry.getValue().equals("null_os_null"))
                    optionSetIds.add(entry.getKey());
            }
            for (String id : optionSetIds) {
                queryData.remove(id);
            }
            if (selectedProgram != null && selectedProgram.displayFrontPageList()) {
                if (queryData.size() < selectedProgram.minAttributesRequiredToSearch()) {
                    showList = false;
                    this.view.setFabIcon(true);
                    queryProcessor.onNext(new HashMap<>());
                } else {
                    showList = true;
                    this.view.setFabIcon(false);
                    queryProcessor.onNext(queryData);
                }
            }
        }
    }

    @Override
    public void onEnrollClick(View view) {
        if (selectedProgram != null)
            if (selectedProgram.access().data().write() != null && selectedProgram.access().data().write())
                enroll(selectedProgram.uid(), null);
            else
                this.view.displayMessage(view.getContext().getString(R.string.search_access_error));
        else
            this.view.displayMessage(view.getContext().getString(R.string.search_program_not_selected));
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
                                    if (view.fromRelationshipTEI() == null) {
                                        this.view.analyticsHelper().setEvent(CREATE_ENROLL, CLICK, CREATE_ENROLL);
                                        Intent intent = EnrollmentActivity.Companion.getIntent(view.getContext(), enrollmentAndTEI.val0(), selectedProgram.uid(), EnrollmentActivity.EnrollmentMode.NEW);
                                        view.getContext().startActivity(intent);
                                    } else {
                                        addRelationship(enrollmentAndTEI.val1(), null, false);
                                    }
                                },
                                Timber::d)
        );
    }

    @Override
    public void onTEIClick(String TEIuid, boolean isOnline) {
        if (!isOnline) {
            openDashboard(TEIuid);
        } else
            downloadTei(TEIuid);

    }

    @Override
    public void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online) {
        if (teiUid.equals(view.fromRelationshipTEI())) {
            view.displayMessage(view.getContext().getString(R.string.relationship_error_recursive));
        } else if (!online) {
            view.analyticsHelper().setEvent(ADD_RELATIONSHIP, CLICK, ADD_RELATIONSHIP);
            Intent intent = new Intent();
            intent.putExtra("TEI_A_UID", teiUid);
            if (relationshipTypeUid != null)
                intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipTypeUid);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            view.analyticsHelper().setEvent(ADD_RELATIONSHIP, CLICK, ADD_RELATIONSHIP);
            downloadTeiForRelationship(teiUid, relationshipTypeUid);
        }
    }

    @Override
    public void downloadTei(String teiUid) {
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
                                () -> openDashboard(teiUid))
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

    private void openDashboard(String TEIuid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", TEIuid);
        bundle.putString("PROGRAM_UID", selectedProgram != null ? selectedProgram.uid() : null);
        view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
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
                            if(hasChanged && view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                            else if(hasChanged)
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
}
