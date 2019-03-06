package org.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.dhis2.R;
import org.dhis2.data.forms.FormActivity;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.data.api.OuMode;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModelBuilder;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenterImpl implements SearchTEContractsModule.SearchTEPresenter {

    private static final int MAX_NO_SELECTED_PROGRAM_RESULTS = 5;
    private final MetadataRepository metadataRepository;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private SearchTEContractsModule.SearchTEView view;

    private ProgramModel selectedProgram;

    private CompositeDisposable compositeDisposable;
    private TrackedEntityTypeModel trackedEntity;
    private HashMap<String, String> queryData;

    private List<OrganisationUnitModel> orgUnits;
    private Integer currentPage;
    private Date selectedEnrollmentDate;

    public SearchTEPresenterImpl(SearchRepository searchRepository, MetadataRepository metadataRepository, D2 d2) {
        this.metadataRepository = metadataRepository;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        queryData = new HashMap<>();
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init(SearchTEContractsModule.SearchTEView view, String trackedEntityType, String initialProgram) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        addGetTEIs(trackedEntityType, initialProgram);
        addGetOrgUnits();
        addRowActions();
        addOptionSetActions();

    }

    private void addGetTEIs(String trackedEntityType, String initialProgram) {
        compositeDisposable.add(
                metadataRepository.getTrackedEntity(trackedEntityType)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap(trackedEntityResult -> {
                            this.trackedEntity = trackedEntityResult;
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
    }

    private void addGetOrgUnits() {
        compositeDisposable.add(
                metadataRepository.getOrganisationUnits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                orgUnitsResult -> orgUnits = orgUnitsResult,
                                Timber::d
                        )
        );
    }

    private void addRowActions() {
        compositeDisposable.add(view.rowActionss()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            HashMap<String, String> queryDataBU = new HashMap(queryData);
                            if (!isEmpty(data.value()))
                                queryData.put(data.id(), data.value());
                            else
                                queryData.remove(data.id());

                            if (!queryData.equals(queryDataBU)) { //Only when queryData has changed
                                view.clearData();
                                if (!isEmpty(data.value()))
                                    queryData.put(data.id(), data.value());
                                else
                                    queryData.remove(data.id());
                                getTrakedEntities();
                            }
                        },
                        Timber::d)
        );
    }

    private void addOptionSetActions() {
        compositeDisposable.add(
                view.optionSetActions()
                        .flatMap(
                                data -> metadataRepository.searchOptions(data.val0(), data.val1(), data.val2()).toFlowable(BackpressureStrategy.LATEST)
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setListOptions,
                                Timber::e
                        ));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
    }
    //endregion

    //------------------------------------------
    //region DATA

    private void getOfflinePage() {
        compositeDisposable.add(
                view.offlinePage()
                        .startWith(0)
                        .flatMap(page -> {
                            this.currentPage = page;
                            return searchRepository.trackedEntityInstances(trackedEntity.uid(), selectedProgram, queryData, page).toFlowable(BackpressureStrategy.BUFFER);
                        })
                        .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
                        .map(trackedEntityInstanceModels -> {
                            List<SearchTeiModel> teiModels = new ArrayList<>();
                            for (TrackedEntityInstanceModel tei : trackedEntityInstanceModels)
                                if (view.fromRelationshipTEI() == null || !tei.uid().equals(view.fromRelationshipTEI())) //If fetching for relationship, discard selected TEI
                                    teiModels.add(new SearchTeiModel(tei, new ArrayList<>()));
                            return teiModels;
                        })
                        .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                        .map(this::getMessage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view.swapTeiListData(), Timber::d)
        );
    }

    private List<SearchTeiModel> parseTEIList(List<TrackedEntityInstance> trackedEntityInstances) {
        List<SearchTeiModel> teiList = new ArrayList<>();
        TrackedEntityInstanceModelBuilder teiBuilder = new TrackedEntityInstanceModelBuilder();

        for (TrackedEntityInstance tei : trackedEntityInstances) {
            if (view.fromRelationshipTEI() == null || !tei.uid().equals(view.fromRelationshipTEI())) { //If fetching for relationship, discard selected TEI
                List<TrackedEntityAttributeValueModel> attributeModels = new ArrayList<>();
                TrackedEntityAttributeValueModel.Builder attrValueBuilder = TrackedEntityAttributeValueModel.builder();
                for (TrackedEntityAttributeValue attrValue : tei.trackedEntityAttributeValues()) {
                    attrValueBuilder.value(attrValue.value())
                            .created(attrValue.created())
                            .lastUpdated(attrValue.lastUpdated())
                            .trackedEntityAttribute(attrValue.trackedEntityAttribute())
                            .trackedEntityInstance(tei.uid());
                    attributeModels.add(attrValueBuilder.build());
                }
                SearchTeiModel teiModel = new SearchTeiModel(teiBuilder.buildModel(tei), attributeModels);
                teiList.add(teiModel);
            }
        }
        return teiList;
    }

    private List<SearchTeiModel> parseTEISearchModels(List<SearchTeiModel> list) {
        List<SearchTeiModel> searchTeiModels = new ArrayList<>();
        for (SearchTeiModel searchTeiModel : list)
            if (searchTeiModel.isOnline() || !searchTeiModel.getEnrollments().isEmpty())
                searchTeiModels.add(searchTeiModel);
        return searchTeiModels;
    }

    private void getOnlinePage() {
        compositeDisposable.add(
                view.onlinePage()
                        .filter(page -> selectedProgram != null)
                        .filter(page -> page > 0)
                        .startWith(1)
                        .flatMap(page -> {
                            this.currentPage = page;
                            List<String> filterList = new ArrayList<>();
                            Date enrollementDate = null;
                            if (queryData != null) {
                                for (Map.Entry<String, String> entry : queryData.entrySet()) {
                                    if (entry.getKey().equalsIgnoreCase(Constants.ENROLLMENT_DATE_UID)) {
                                        enrollementDate = DateUtils.uiDateFormat().parse(entry.getValue());
                                    } else if (!entry.getKey().equals(Constants.INCIDENT_DATE_UID)) { //TODO: HOW TO INCLUDE INCIDENT DATE IN ONLINE SEARCH
                                        filterList.add(entry.getKey() + ":LIKE:" + entry.getValue());
                                    }
                                }
                            }
                            List<String> orgUnitsUids = new ArrayList<>();
                            if (orgUnits != null) {
                                orgUnitsUids.add(orgUnits.get(0).uid());
                            }
                            TrackedEntityInstanceQuery query = TrackedEntityInstanceQuery.builder()
                                    .program(selectedProgram.uid())
                                    .page(page)
                                    .pageSize(20)
                                    .paging(true)
                                    .filter(filterList)
                                    .programStartDate(enrollementDate)
                                    .orgUnits(orgUnitsUids)
                                    .orgUnitMode(OuMode.ACCESSIBLE)
                                    .build();

                            return Flowable.defer(() -> Flowable.fromCallable(d2.trackedEntityModule().queryTrackedEntityInstances(query)))
                                    .observeOn(Schedulers.io())
                                    .subscribeOn(Schedulers.io())
                                    .doOnError(this::handleError)
                                    .onErrorReturn(data -> new ArrayList<>()); //If there is an error returns an empty list

                        })
                        .map(this::parseTEIList)
                        .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                        .map(this::parseTEISearchModels)
                        .flatMap(list -> {
                            if (currentPage == 1)
                                return searchRepository.trackedEntityInstancesToUpdate(trackedEntity.uid(), selectedProgram, queryData, list.size())
                                        .map(trackedEntityInstanceModels -> getTEIToUpdate(list, trackedEntityInstanceModels))
                                        .toFlowable(BackpressureStrategy.LATEST);
                            else
                                return Flowable.just(list);
                        })
                        .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                        .map(this::getMessage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view.swapTeiListData(), Timber::d)
        );
    }

    private List<SearchTeiModel> getTEIToUpdate(List<SearchTeiModel> list, List<TrackedEntityInstanceModel> trackedEntityInstanceModels){
        List<SearchTeiModel> helperList = new ArrayList<>();

        for (SearchTeiModel searchTeiModel : list) {
            boolean toUpdate = false;
            for (TrackedEntityInstanceModel tei : trackedEntityInstanceModels) {
                if (searchTeiModel.getTei().uid().equals(tei.uid())) {
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
    }

    @Override
    public void getTrakedEntities() {
        if (!NetworkUtils.isOnline(view.getContext()) || selectedProgram == null || Build.VERSION.SDK_INT <= 19) {
            getOfflinePage();
        } else {
            getOnlinePage();
        }
    }

    private String showNoResults(List<SearchTeiModel> teiList){
        String messageId = "";
        if (selectedProgram.minAttributesRequiredToSearch() > queryData.size())
            messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
        else if (selectedProgram.maxTeiCountToReturn() != 0 && teiList.size() > selectedProgram.maxTeiCountToReturn())
            messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), selectedProgram.maxTeiCountToReturn());
        else if (teiList.isEmpty() && !queryData.isEmpty())
            messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
        else if (teiList.isEmpty())
            messageId = view.getContext().getString(R.string.search_init);

        return messageId;
    }

    private String showNoSelectedProgram(List<SearchTeiModel> teiList){
        String messageId = "";
        if (queryData.isEmpty() && view.fromRelationshipTEI() == null)
            messageId = view.getContext().getString(R.string.search_init);
        else if (teiList.isEmpty())
            messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
        else if (teiList.size() > MAX_NO_SELECTED_PROGRAM_RESULTS && view.fromRelationshipTEI() == null) {
            messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), MAX_NO_SELECTED_PROGRAM_RESULTS);
        }
        return messageId;
    }

    private Pair<List<SearchTeiModel>, String> getMessage(List<SearchTeiModel> teiList) {

        String messageId = "";
        if (selectedProgram != null && !selectedProgram.displayFrontPageList()) {
            messageId = showNoResults(teiList);
        } else if (selectedProgram == null) {
            messageId = showNoSelectedProgram(teiList);
        } else {
            if (teiList.isEmpty() && !queryData.isEmpty())
                messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
            else if (teiList.isEmpty())
                messageId = view.getContext().getString(R.string.search_init);
        }

        return Pair.create(teiList, messageId);
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
                default:
                    break;
            }
        }
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

        getTrakedEntities(); //TODO: Check if queryData dataElements are only those from the selectedProgram

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

    private OrgUnitDialog setUpOrgUnitDialog(String programUid, String uid) {
        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace().setMultiSelection(false);
        orgUnitDialog.setTitle("Enrollment Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null)
                        enrollInOrgUnit(orgUnitDialog.getSelectedOrgUnit(), programUid, uid, selectedEnrollmentDate);
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());
        return orgUnitDialog;
    }

    private DatePickerDialog setUpDateDialog(String programUid, String uid, OrgUnitDialog orgUnitDialog) {

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(view.getContext(), (
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

                    compositeDisposable.add(getOrgUnits()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    allOrgUnits -> selectOrgUnits(allOrgUnits, orgUnitDialog, programUid, uid),
                                    Timber::d
                            )
                    );


                }),
                year,
                month,
                day);
    }

    private void selectOrgUnits(List<OrganisationUnitModel> allOrgUnits, OrgUnitDialog orgUnitDialog, String programUid, String uid) {
        ArrayList<OrganisationUnitModel> mOrgUnits = new ArrayList<>();
        for (OrganisationUnitModel orgUnit : allOrgUnits) {
            boolean afterOpening = false;
            boolean beforeClosing = false;
            if (orgUnit.openingDate() == null || !selectedEnrollmentDate.before(orgUnit.openingDate()))
                afterOpening = true;
            if (orgUnit.closedDate() == null || !selectedEnrollmentDate.after(orgUnit.closedDate()))
                beforeClosing = true;
            if (afterOpening && beforeClosing)
                mOrgUnits.add(orgUnit);
        }
        if (mOrgUnits.size() > 1) {
            orgUnitDialog.setOrgUnits(mOrgUnits);
            if (!orgUnitDialog.isAdded())
                orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
        } else
            enrollInOrgUnit(mOrgUnits.get(0).uid(), programUid, uid, selectedEnrollmentDate);
    }

    @Override
    public void enroll(String programUid, String uid) {
        selectedEnrollmentDate = Calendar.getInstance().getTime();

        OrgUnitDialog orgUnitDialog = setUpOrgUnitDialog(programUid, uid);
        DatePickerDialog dateDialog = setUpDateDialog(programUid, uid, orgUnitDialog);

        if (selectedProgram != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        if (selectedProgram != null && selectedProgram.enrollmentDateLabel() != null) {
            dateDialog.setTitle(selectedProgram.enrollmentDateLabel());
        }
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> dialog.dismiss());
        dateDialog.show();
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String uid, Date enrollmentDate) {
        if (trackedEntity != null && trackedEntity.uid() != null) {
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
    }

    @Override
    public void onTEIClick(String teiUid, boolean isOnline) {
        if (!isOnline) {
            openDashboard(teiUid);
        } else
            downloadTei(teiUid);

    }

    @Override
    public void addRelationship(String teiUid, String relationshipTypeUid, boolean online) {
        if (!online) {
            String relationshipType;
            if (relationshipTypeUid == null)
                relationshipType = selectedProgram.relationshipType();
            else
                relationshipType = relationshipTypeUid;

            Intent intent = new Intent();
            intent.putExtra("TEI_A_UID", teiUid);
            intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipType);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            downloadTeiForRelationship(teiUid, relationshipTypeUid);
        }
    }

    @Override
    public void addRelationship(String teiUid, boolean online) {
        if (!online) {
            Intent intent = new Intent();
            intent.putExtra("TEI_A_UID", teiUid);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            downloadTeiForRelationship(teiUid, null);
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
    public void downloadTeiForRelationship(String teiUid, @Nullable String relationshipTypeUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(teiUid);
        compositeDisposable.add(
                Flowable.fromCallable(d2.trackedEntityModule().downloadTrackedEntityInstancesByUid(teiUids))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    if (relationshipTypeUid == null)
                                        addRelationship(teiUid, false);
                                    else
                                        addRelationship(teiUid, relationshipTypeUid, false);
                                },
                                Timber::d)
        );
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return searchRepository.getOrgUnits(selectedProgram != null ? selectedProgram.uid() : null);
    }

    private void openDashboard(String teiUid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", teiUid);
        bundle.putString("PROGRAM_UID", selectedProgram != null ? selectedProgram.uid() : null);
        view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }

    @Override
    public String getProgramColor(String uid) {
        return searchRepository.getProgramColor(uid);
    }
}
