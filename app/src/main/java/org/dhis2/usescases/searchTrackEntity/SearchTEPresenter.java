package org.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import org.dhis2.R;
import org.dhis2.data.forms.FormActivity;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.appcompat.app.AlertDialog;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
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
    private final MetadataRepository metadataRepository;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private SearchTEContractsModule.View view;

    private ProgramModel selectedProgram;

    private CompositeDisposable compositeDisposable;
    private TrackedEntityTypeModel trackedEntity;
    private HashMap<String, String> queryData;
    private Map<String, String> queryDataEQ;

    private List<OrganisationUnitModel> orgUnits;
    private List<String> orgUnitsUid = new ArrayList<>();
    private Integer currentPage;
    private Date selectedEnrollmentDate;

    private FlowableProcessor<HashMap<String, String>> queryProcessor;

    public SearchTEPresenter(SearchRepository searchRepository, MetadataRepository metadataRepository, D2 d2) {
        this.metadataRepository = metadataRepository;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        queryData = new HashMap<>();
        queryDataEQ = new HashMap<>();
        queryProcessor = PublishProcessor.create();
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init(SearchTEContractsModule.View view, String trackedEntityType, String initialProgram) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                metadataRepository.getTrackedEntity(trackedEntityType)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap(trackedEntity ->
                        {
                            this.trackedEntity = trackedEntity;
                            return searchRepository.programsWithRegistration(trackedEntityType);
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(programModels -> {
                            for (ProgramModel programModel : programModels)
                                if (programModel.uid().equals(initialProgram))
                                    this.selectedProgram = programModel;
                            view.setPrograms(programModels);

                            if (selectedProgram != null)
                                return searchRepository.programAttributes(selectedProgram.uid());
                            else
                                return searchRepository.programAttributes();

                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.setForm(data, selectedProgram, queryData),
                                Timber::d)
        );

        compositeDisposable.add(
                metadataRepository.getOrganisationUnits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                orgUnits -> {
                                    this.orgUnits = orgUnits;
                                    for (OrganisationUnitModel orgUnit: orgUnits) {
                                        this.orgUnitsUid.add(orgUnit.uid());
                                    }
                                },
                                Timber::d
                        )
        );


        compositeDisposable.add(view.rowActionss()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            Map<String, String> queryDataBU = new HashMap<>(queryData);
                            if (!isEmpty(data.value())) {
                                queryData.put(data.id(), data.value());
                                if (data.requiresExactMatch())
                                    queryDataEQ.put(data.id(), data.value());
                            } else {
                                queryData.remove(data.id());
                                queryDataEQ.remove(data.id());
                            }

                            if (!queryData.equals(queryDataBU)) { //Only when queryData has changed
                                view.clearData();
                                if (!isEmpty(data.value()))
                                    queryData.put(data.id(), data.value());
                                else
                                    queryData.remove(data.id());
                                queryProcessor.onNext(queryData);
                                //getTrakedEntities();
                            }
                        },
                        Timber::d)
        );

        compositeDisposable.add(
                view.optionSetActions()
                        .flatMap(
                                data -> metadataRepository.searchOptions(data.val0(), data.val1(), data.val2(), new ArrayList<>(), new ArrayList<>()).toFlowable(BackpressureStrategy.LATEST)
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setListOptions,
                                Timber::e
                        ));

        compositeDisposable.add(
                queryProcessor
                        .map(map -> {
                            if(!NetworkUtils.isOnline(view.getContext()) || selectedProgram == null || Build.VERSION.SDK_INT <= 19)
                                return searchRepository.searchTrackedEntitiesOffline(selectedProgram, orgUnitsUid, map);
                            else
                                return searchRepository.searchTrackedEntitiesAll(selectedProgram, orgUnitsUid, map);
                        })
                        //.map(pagedListLiveData -> getMessage(pagedListLiveData))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::setLiveData, Timber::d)
        );

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
    }
    //endregion

    //------------------------------------------
    //region DATA
   /* @Override
    public void getTrakedEntities() {
        if (!NetworkUtils.isOnline(view.getContext()) || selectedProgram == null || Build.VERSION.SDK_INT <= 19)
            compositeDisposable.add(
                    queryProcessor
                            .map(map -> {
                                if(!NetworkUtils.isOnline(view.getContext()) || selectedProgram == null || Build.VERSION.SDK_INT <= 19)
                                    return searchRepository.searchTrackedEntitiesOffline(selectedProgram, orgUnitsUid, map);
                                else
                                    return searchRepository.searchTrackedEntitiesOffline(selectedProgram, orgUnitsUid, map);
                            })
                            .map(pagedListLiveData -> getMessage(pagedListLiveData))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(view::setLiveData, Timber::d)

                            *//*.map(trackedEntityInstanceModels -> {
                                List<SearchTeiModel> teiModels = new ArrayList<>();
                                for (TrackedEntityInstanceModel tei : trackedEntityInstanceModels)
                                    if (view.fromRelationshipTEI() == null || !tei.uid().equals(view.fromRelationshipTEI())) //If fetching for relationship, discard selected TEI
                                        teiModels.add(new SearchTeiModel(tei, new ArrayList<>()));
                                return teiModels;
                            })*//*

            );
        *//*else
            compositeDisposable.add(
                    view.onlinePage()
                            .filter(page -> selectedProgram != null)
                            .filter(page -> page > 0)
                            .startWith(1)
                            .flatMap(page -> {
                                this.currentPage = page;

                                return Flowable.defer(() -> searchRepository.trackedEntityInstances2(trackedEntity.uid(), selectedProgram, queryData, page, orgUnits).toFlowable(BackpressureStrategy.BUFFER))
                                        .observeOn(Schedulers.io())
                                        .subscribeOn(Schedulers.io())
                                        .doOnError(this::handleError)
                                        .onErrorReturn(data -> new ArrayList<>()); //If there is an error returns an empty list

                            })
                            .map(trackedEntityInstances -> {
                                List<SearchTeiModel> teiList = new ArrayList<>();
                                for (TrackedEntityInstance tei : trackedEntityInstances) {
                                    if (view.fromRelationshipTEI() == null || !tei.uid().equals(view.fromRelationshipTEI())) { //If fetching for relationship, discard selected TEI
                                        List<TrackedEntityAttributeValueModel> attributeModels = new ArrayList<>();
                                        TrackedEntityAttributeValueModel.Builder attrValueBuilder = TrackedEntityAttributeValueModel.builder();
                                        if (tei.trackedEntityAttributeValues() != null) {
                                            for (TrackedEntityAttributeValue attrValue : tei.trackedEntityAttributeValues()) {
                                                attrValueBuilder.value(attrValue.value())
                                                        .created(attrValue.created())
                                                        .lastUpdated(attrValue.lastUpdated())
                                                        .trackedEntityAttribute(attrValue.trackedEntityAttribute())
                                                        .trackedEntityInstance(tei.uid());
                                                attributeModels.add(attrValueBuilder.build());
                                            }
                                        }
                                        TrackedEntityInstanceModel model = TrackedEntityInstanceModel.builder()
                                                .created(tei.created())
                                                .id(tei.id())
                                                .lastUpdated(tei.lastUpdated())
                                                .state(tei.state())
                                                .coordinates(tei.coordinates())
                                                .createdAtClient(tei.createdAtClient())
                                                .featureType(tei.featureType())
                                                .lastUpdatedAtClient(tei.lastUpdatedAtClient())
                                                .organisationUnit(tei.organisationUnit())
                                                .uid(tei.uid())
                                                .trackedEntityType(tei.trackedEntityType())
                                                .build();
                                        SearchTeiModel teiModel = new SearchTeiModel(model, attributeModels);
                                        teiList.add(teiModel);
                                    }
                                }
                                return teiList;
                            })
                            .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                            .map(list -> {
                                List<SearchTeiModel> searchTeiModels = new ArrayList<>();
                                for (SearchTeiModel searchTeiModel : list)
                                    if (searchTeiModel.isOnline() || !searchTeiModel.getEnrollmentModels().isEmpty())
                                        searchTeiModels.add(searchTeiModel);
                                return searchTeiModels;
                            })
                            *//**//*.flatMap(list -> {
                                int minAttrToSearch = selectedProgram.minAttributesRequiredToSearch() != null ? selectedProgram.minAttributesRequiredToSearch() : 0;
                                if (currentPage == 1 && (minAttrToSearch <= queryData.size()))
                                    return searchRepository.trackedEntityInstancesToUpdate(trackedEntity.uid(), selectedProgram, queryData, list.size())
                                            .map(trackedEntityInstanceModels -> {
                                                List<SearchTeiModel> helperList = new ArrayList<>();

                                                for (SearchTeiModel searchTeiModel : list) {
                                                    boolean toUpdate = false;
                                                    for (TrackedEntityInstanceModel tei : trackedEntityInstanceModels) {
                                                        if (searchTeiModel.getTeiModel().uid().equals(tei.uid())) {
                                                            toUpdate = true;
                                                        }
                                                    }
                                                    if (!toUpdate)
                                                        helperList.add(searchTeiModel);
                                                }

                                                for (TrackedEntityInstanceModel tei : trackedEntityInstanceModels) {
                                                    if (view.fromRelationshipTEI() == null || !tei.uid().equals(view.fromRelationshipTEI()))
                                                        helperList.add(new SearchTeiModel(tei, new ArrayList<>()));
                                                }

                                                return helperList;
                                            }).toFlowable(BackpressureStrategy.LATEST);
                                else
                                    return Flowable.just(list);
                            })*//**//*
                            .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                            //.map(this::getMessage)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(view.swapTeiListData(), Timber::d)
            );*//*
    }*/

    private Pair<LiveData<PagedList<SearchTeiModel>>, String> getMessage(LiveData<PagedList<SearchTeiModel>> list) {

        int size;
        if(list.getValue() == null)
            size = 0;
        else
            size = list.getValue().size();

        String messageId = "";
        if (selectedProgram != null && !selectedProgram.displayFrontPageList()) {
            if (selectedProgram != null && selectedProgram.minAttributesRequiredToSearch() == 0 && queryData.size() == 0)
                messageId = view.getContext().getString(R.string.search_attr);
            if (selectedProgram != null && selectedProgram.minAttributesRequiredToSearch() > queryData.size())
                messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
            else if (selectedProgram.maxTeiCountToReturn() != 0 && size > selectedProgram.maxTeiCountToReturn())
                messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), selectedProgram.maxTeiCountToReturn());
            else if (size == 0 && !queryData.isEmpty())
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
            else if (size == 0)
                messageId = view.getContext().getString(R.string.search_init);
        } else if (selectedProgram == null) {
            if (queryData.isEmpty() && view.fromRelationshipTEI() == null)
                messageId = view.getContext().getString(R.string.search_init);
            else if (size == 0)
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
            else if (size > MAX_NO_SELECTED_PROGRAM_RESULTS && view.fromRelationshipTEI() == null) {
                messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), MAX_NO_SELECTED_PROGRAM_RESULTS);
            }
        } else {
            if (size == 0 && !queryData.isEmpty())
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
            else if (size == 0)
                messageId = view.getContext().getString(R.string.search_init);
        }

        return Pair.create(list, messageId);
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

    private void getTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes()
                .flatMap(list -> {
                    if (selectedProgram == null)
                        return searchRepository.trackedEntityTypeAttributes();
                    else
                        return Observable.just(list);
                })
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

    @Override
    public TrackedEntityTypeModel getTrackedEntityName() {
        return trackedEntity;
    }

    @Override
    public ProgramModel getProgramModel() {
        return selectedProgram;
    }

    //endregion

    @Override
    public void setProgram(ProgramModel programSelected) {
        selectedProgram = programSelected;
        view.clearList(programSelected == null ? null : programSelected.uid());
        view.clearData();

        if (selectedProgram == null)
            getTrackedEntityAttributes();
        else
            getProgramTrackedEntityAttributes();

        queryProcessor.onNext(new HashMap<>());
        //getTrakedEntities(); //TODO: Check if queryData dataElements are only those from the selectedProgram

    }

    @Override
    public void onClearClick() {
        queryData.clear();
        this.currentPage = 0;
        setProgram(selectedProgram);
    }


    //endregion

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onFabClick(View view) {
        onEnrollClick(view);
    }

    @Override
    public void onEnrollClick(View view) {
        if (selectedProgram != null && selectedProgram.accessDataWrite() != null && selectedProgram.accessDataWrite())
            if (view.isEnabled()) {
                enroll(selectedProgram.uid(), null);
            } else
                this.view.displayMessage(view.getContext().getString(R.string.search_program_not_selected));
        else {
            this.view.displayMessage(view.getContext().getString(R.string.search_access_error));
        }
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
                                if (!orgUnitDialog.isAdded())
                                    orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                            } else if (allOrgUnits.size() == 1)
                                showEnrollmentDatePicker(allOrgUnits.get(0), programUid, uid);
                        },
                        Timber::d
                )
        );
    }

    private void showNativeCalendar(OrganisationUnitModel selectedOrgUnitModel, String programUid, String uid) {
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

                    enrollInOrgUnit(selectedOrgUnitModel.uid(), programUid, uid, selectedEnrollmentDate);

                }),
                year,
                month,
                day);

        if (selectedOrgUnitModel.openingDate() != null)
            dateDialog.getDatePicker().setMinDate(selectedOrgUnitModel.openingDate().getTime());

        if (selectedOrgUnitModel.closedDate() == null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        } else if (selectedOrgUnitModel.closedDate() != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            if (selectedOrgUnitModel.closedDate().before(new Date(System.currentTimeMillis()))) {
                dateDialog.getDatePicker().setMaxDate(selectedOrgUnitModel.closedDate().getTime());
            } else {
                dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            }
        } else if (selectedOrgUnitModel.closedDate() != null && selectedProgram.selectEnrollmentDatesInFuture()) {
            dateDialog.getDatePicker().setMaxDate(selectedOrgUnitModel.closedDate().getTime());
        }

        dateDialog.setTitle(selectedProgram.enrollmentDateLabel());
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            dialog.dismiss();
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, view.getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                dateDialog.dismiss();
                showCustomCalendar(selectedOrgUnitModel, programUid, uid);
            });
        }

        dateDialog.show();
    }

    private void showCustomCalendar(OrganisationUnitModel selectedOrgUnitModel, String programUid, String uid) {

        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar c = Calendar.getInstance();
        datePicker.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext(), R.style.DatePickerTheme)
                .setTitle(selectedProgram.enrollmentDateLabel())
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                    selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCalendar.set(Calendar.MINUTE, 0);
                    selectedCalendar.set(Calendar.SECOND, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);
                    selectedEnrollmentDate = selectedCalendar.getTime();

                    enrollInOrgUnit(selectedOrgUnitModel.uid(), programUid, uid, selectedEnrollmentDate);
                })
                .setNeutralButton(view.getContext().getResources().getString(R.string.change_calendar),
                        (dialog, which) -> showNativeCalendar(selectedOrgUnitModel, programUid, uid))
                .setNegativeButton(view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> dialog.dismiss());

        if (selectedOrgUnitModel.openingDate() != null)
            datePicker.setMinDate(selectedOrgUnitModel.openingDate().getTime());

        if (selectedOrgUnitModel.closedDate() == null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            datePicker.setMaxDate(System.currentTimeMillis());
        } else if (selectedOrgUnitModel.closedDate() != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            if (selectedOrgUnitModel.closedDate().before(new Date(System.currentTimeMillis()))) {
                datePicker.setMaxDate(selectedOrgUnitModel.closedDate().getTime());
            } else {
                datePicker.setMaxDate(System.currentTimeMillis());
            }
        } else if (selectedOrgUnitModel.closedDate() != null && selectedProgram.selectEnrollmentDatesInFuture()) {
            datePicker.setMaxDate(selectedOrgUnitModel.closedDate().getTime());
        }

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
    }


    private void showEnrollmentDatePicker(OrganisationUnitModel selectedOrgUnitModel, String programUid, String uid) {
        showNativeCalendar(selectedOrgUnitModel, programUid, uid);
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String uid, Date enrollmentDate) {
        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), orgUnitUid, programUid, uid, queryData, enrollmentDate)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentUid -> {
                                    FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(enrollmentUid);
                                    this.view.getContext().startActivity(FormActivity.create(this.view.getContext(), formViewArguments, true));
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
            String relationshipType;
            if (relationshipTypeUid == null)
                relationshipType = selectedProgram.relationshipType();
            else
                relationshipType = relationshipTypeUid;

            Intent intent = new Intent();
            intent.putExtra("TEI_A_UID", TEIuid);
            intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipType);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            downloadTeiForRelationship(TEIuid, relationshipTypeUid);
        }
    }

    @Override
    public void addRelationship(String TEIuid, boolean online) {
        if (!online) {
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
                Flowable.fromCallable(d2.trackedEntityModule().downloadTrackedEntityInstancesByUid(teiUids))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> openDashboard(data.get(0).uid()),
                                Timber::d)
        );

    }

    @Override
    public void downloadTeiForRelationship(String TEIuid, @Nullable String relationshipTypeUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(TEIuid);
        compositeDisposable.add(
                Flowable.fromCallable(d2.trackedEntityModule().downloadTrackedEntityInstancesByUid(teiUids))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    if (relationshipTypeUid == null)
                                        addRelationship(TEIuid, false);
                                    else
                                        addRelationship(TEIuid, relationshipTypeUid, false);
                                },
                                Timber::d)
        );
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
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
}
