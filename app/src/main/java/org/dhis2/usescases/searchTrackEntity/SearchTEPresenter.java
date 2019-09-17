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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.paging.PagedList;

import org.dhis2.R;
import org.dhis2.data.forms.FormActivity;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.main.program.SyncStatusDialog;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.dhis2.utils.filters.FilterManager;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private static final int MAX_NO_SELECTED_PROGRAM_RESULTS = 5;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private SearchTEContractsModule.View view;

    private Program selectedProgram;

    private CompositeDisposable compositeDisposable;
    private TrackedEntityType trackedEntity;
    private HashMap<String, String> queryData;
    private Map<String, String> queryDataEQ;

    private List<String> orgUnitsUid = new ArrayList<>();
    private Date selectedEnrollmentDate;

    private FlowableProcessor<HashMap<String, String>> queryProcessor;
    private FlowableProcessor<Boolean> processorDismissDialog;
    private String trackedEntityType;
    private String initialProgram;
    private FlowableProcessor<Unit> mapProcessor;
    private FlowableProcessor<Unit> enrollmentMapProcessor;
    private Dialog dialogDisplayed;

    public SearchTEPresenter(D2 d2, SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
        this.d2 = d2;
        queryData = new HashMap<>();
        queryDataEQ = new HashMap<>();
        queryProcessor = PublishProcessor.create();
        processorDismissDialog = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
        enrollmentMapProcessor = PublishProcessor.create();
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init(SearchTEContractsModule.View view, String trackedEntityType, String initialProgram) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        this.trackedEntityType = trackedEntityType;
        this.initialProgram = initialProgram;

        compositeDisposable.add(
                searchRepository.getTrackedEntityType(trackedEntityType)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap(trackedEntity ->
                        {
                            this.trackedEntity = trackedEntity;
                            return searchRepository.programsWithRegistration(trackedEntityType);
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.featureType(),
                                Timber::d
                        )
        );

        compositeDisposable.add(
                searchRepository.getOrganisationUnits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                orgUnits -> {
                                    for (OrganisationUnit orgUnit : orgUnits) {
                                        this.orgUnitsUid.add(orgUnit.uid());
                                    }
                                },
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.setMap(),
                                Timber::e,
                                () -> Timber.d("COMPLETED")
                        ));

        manageProcessorDismissDialog();
    }

    private void manageProcessorDismissDialog() {
        compositeDisposable.add(processorDismissDialog
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bool -> init(view, trackedEntityType, initialProgram),
                        Timber::d));
    }

    @Override
    public void initSearch(SearchTEContractsModule.View view) {

        compositeDisposable.add(view.rowActionss()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            Map<String, String> queryDataBU = new HashMap<>(queryData);
                            view.setFabIcon(true);
                            if (!isEmpty(data.value())) {
                                queryData.put(data.id(), data.value());
                                if (data.requiresExactMatch())
                                    if (data.value().equals("null_os_null")) {
                                        queryData.remove(data.id());
                                        queryDataEQ.remove(data.id());
                                    } else
                                        queryDataEQ.put(data.id(), data.value());
                            } else {
                                queryData.remove(data.id());
                                queryDataEQ.remove(data.id());
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
                        .map(map -> {
                            HashMap<String, String> data = new HashMap<>(map);
                            return searchRepository.searchTrackedEntities(
                                    selectedProgram, trackedEntityType,
                                    FilterManager.getInstance().getOrgUnitUidsFilters(),
                                    FilterManager.getInstance().getStateFilters(),
                                    data, NetworkUtils.isOnline(view.getContext()));
                        })
                        .doOnError(this::handleError)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::setLiveData, Timber::d)
        );

        compositeDisposable.add(
                queryProcessor
                        .startWith(queryData)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> view.clearData(), Timber::d)
        );

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setForm(data, selectedProgram, queryData),
                        Timber::d)
        );
    }

    private void getProgramTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes(selectedProgram.uid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                                queryData, NetworkUtils.isOnline(view.getContext())))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
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
            queryProcessor.onNext(queryData);
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

    @Override
    public void enroll(String programUid, String uid) {
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentAndTEI -> {
                                    if (view.fromRelationshipTEI() == null) {
                                        Intent intent = EnrollmentActivity.Companion.getIntent(view.getContext(),enrollmentAndTEI.val0(),selectedProgram.uid(), EnrollmentActivity.EnrollmentMode.NEW);
                                        view.getContext().startActivity(intent);
                                        /*FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(enrollmentAndTEI.val0());
                                        this.view.getContext().startActivity(FormActivity.create(this.view.getContext(), formViewArguments, true));*/
                                    } else {
                                        addRelationship(enrollmentAndTEI.val1(), false);
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
    public void addRelationship(String TEIuid, String relationshipTypeUid, boolean online) {
        if (!online) {
            Intent intent = new Intent();
            intent.putExtra("TEI_A_UID", TEIuid);
            intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipTypeUid);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            downloadTeiForRelationship(TEIuid, relationshipTypeUid);
        }
    }

    @Override
    public void addRelationship(String TEIuid, boolean online) {
        if (TEIuid.equals(view.fromRelationshipTEI())) {
            view.displayMessage(view.getContext().getString(R.string.relationship_error_recursive));
        } else if (!online) {
            Intent intent = new Intent();
            intent.putExtra("TEI_A_UID", TEIuid);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            downloadTeiForRelationship(TEIuid, null);
        }
    }

    @Override
    public void downloadTei(String teiUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(teiUid);
        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstanceDownloader.byUid().in(teiUids).download()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.downloadProgress(),
                                Timber::d,
                                () -> openDashboard(teiUid))
        );

    }

    @Override
    public String nameOUByUid(String uid){
        OrganisationUnit organisationUnit = d2.organisationUnitModule().organisationUnits.uid(uid).blockingGet();
        return organisationUnit != null ? organisationUnit.name() : null;
    }

    @Override
    public void downloadTeiForRelationship(String TEIuid, @Nullable String relationshipTypeUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(TEIuid);
        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstanceDownloader.byUid().in(teiUids).download()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> Timber.d("DOWNLOADING TEI %s : %s%", TEIuid, data.percentage()),
                                Timber::d,
                                () -> {
                                    if (relationshipTypeUid == null)
                                        addRelationship(TEIuid, false);
                                    else
                                        addRelationship(TEIuid, relationshipTypeUid, false);
                                })
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
        view.showSyncDialog(teiUid, SyncStatusDialog.ConflictType.TEI, processorDismissDialog);
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
    public Drawable getSymbolIcon() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes.uid(trackedEntityType).withAllChildren().blockingGet();

        if (teiType.style() != null && teiType.style().icon() != null) {
            return
                    ObjectStyleUtils.getIconResource(view.getContext(), teiType.style().icon(), R.drawable.mapbox_marker_icon_default);
        } else
            return AppCompatResources.getDrawable(view.getContext(), R.drawable.mapbox_marker_icon_default);
    }

    @Override
    public void getEnrollmentMapData() {
        enrollmentMapProcessor.onNext(new Unit());
    }

    @Override
    public Drawable getEnrollmentSymbolIcon() {
        if (selectedProgram != null) {
            if (selectedProgram.style() != null && selectedProgram.style().icon() != null) {
                return
                        ObjectStyleUtils.getIconResource(view.getContext(), selectedProgram.style().icon(), R.drawable.ic_program_default);
            } else
                return AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_program_default);
        }

        return null;
    }
}
