package com.dhis2.usescases.searchTrackEntity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dhis2.Bindings.Bindings;
import com.dhis2.R;
import com.dhis2.data.forms.FormActivity;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.utils.CustomViews.OrgUnitDialog;
import com.dhis2.utils.NetworkUtils;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.D2CallException;
import org.hisp.dhis.android.core.data.api.OuMode;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModelBuilder;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModelBuilder;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private List<OrganisationUnitModel> orgUnits;
    private Integer currentPage;

    public SearchTEPresenter(SearchRepository searchRepository, MetadataRepository metadataRepository, D2 d2) {
        Bindings.setMetadataRepository(metadataRepository);
        this.metadataRepository = metadataRepository;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        queryData = new HashMap<>();
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
                                orgUnits -> this.orgUnits = orgUnits,
                                Timber::d
                        )
        );


        compositeDisposable.add(view.rowActionss()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            view.clearData();
                            if (!isEmpty(data.value()))
                                queryData.put(data.id(), data.value());
                            else
                                queryData.remove(data.id());
                            getTrakedEntities();
                        },
                        Timber::d)
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
    public void getTrakedEntities() {
        if (!NetworkUtils.isOnline(view.getContext()) || selectedProgram == null)
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
                                    teiModels.add(new SearchTeiModel(tei, new ArrayList<>()));
                                return teiModels;
                            })
                            .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                            .map(this::getMessage)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(view.swapTeiListData(), Timber::d)
            );
        else
            compositeDisposable.add(
                    view.onlinePage()
                            .filter(page -> selectedProgram != null)
                            .filter(page -> page > 0)
                            .startWith(1)
                            .flatMap(page -> {
                                this.currentPage = page;
                                List<String> filterList = new ArrayList<>();
                                if (queryData != null) {
                                    for (String key : queryData.keySet()) {
                                        filterList.add(key + ":LIKE:" + queryData.get(key));
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
                                        .orgUnits(orgUnitsUids)
                                        .orgUnitMode(OuMode.ACCESSIBLE)
                                        .build();

                                return Flowable.defer(() -> Flowable.fromCallable(d2.queryTrackedEntityInstances(query)))
                                        .observeOn(Schedulers.io())
                                        .subscribeOn(Schedulers.io())
                                        .doOnError(this::handleError);

                            })
                            .map(trackedEntityInstances -> {
                                List<SearchTeiModel> teiList = new ArrayList<>();
                                TrackedEntityInstanceModelBuilder teiBuilder = new TrackedEntityInstanceModelBuilder();

                                for (TrackedEntityInstance tei : trackedEntityInstances) {
                                    List<TrackedEntityAttributeValueModel> attributeModels = new ArrayList<>();
                                    TrackedEntityAttributeValueModelBuilder attrValueBuilder = new TrackedEntityAttributeValueModelBuilder(tei);
                                    for (TrackedEntityAttributeValue attrValue : tei.trackedEntityAttributeValues()) {
                                        attributeModels.add(attrValueBuilder.buildModel(attrValue));
                                    }
                                    SearchTeiModel teiModel = new SearchTeiModel(teiBuilder.buildModel(tei), attributeModels);
                                    teiList.add(teiModel);
                                }
                                return teiList;
                            })
                            .flatMap(list -> searchRepository.transformIntoModel(list, selectedProgram))
                            .flatMap(list -> {
                                if (currentPage == 1)
                                    return searchRepository.trackedEntityInstancesToUpdate(trackedEntity.uid(), selectedProgram, queryData)
                                            .map(trackedEntityInstanceModels -> {
                                                for (TrackedEntityInstanceModel tei : trackedEntityInstanceModels)
                                                    list.add(new SearchTeiModel(tei, new ArrayList<>()));
                                                return list;
                                            }).toFlowable(BackpressureStrategy.LATEST);
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

    private Pair<List<SearchTeiModel>, String> getMessage(List<SearchTeiModel> teiList) {
        String messageId = "";
        if (currentPage == 0) {
            if (selectedProgram != null && !selectedProgram.displayFrontPageList()) {
                if (selectedProgram != null && selectedProgram.minAttributesRequiredToSearch() > queryData.size())
                    messageId = String.format(view.getContext().getString(R.string.search_min_num_attr), selectedProgram.minAttributesRequiredToSearch());
                else if (selectedProgram.maxTeiCountToReturn() != 0 && teiList.size() > selectedProgram.maxTeiCountToReturn())
                    messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), selectedProgram.maxTeiCountToReturn());
                else if (teiList.isEmpty() && !queryData.isEmpty())
                    messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                else if (teiList.isEmpty())
                    messageId = view.getContext().getString(R.string.search_init);
            } else if (selectedProgram == null) {
                if (queryData.isEmpty())
                    messageId = view.getContext().getString(R.string.search_init);
                else if (teiList.isEmpty())
                    messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                else if (teiList.size() > MAX_NO_SELECTED_PROGRAM_RESULTS) {
                    messageId = String.format(view.getContext().getString(R.string.search_max_tei_reached), MAX_NO_SELECTED_PROGRAM_RESULTS);
                }
            } else {
                if (teiList.isEmpty() && !queryData.isEmpty())
                    messageId = String.format(view.getContext().getString(R.string.search_criteria_not_met), getTrackedEntityName().displayName());
                else if (teiList.isEmpty())
                    messageId = view.getContext().getString(R.string.search_init);
            }
        }
        return Pair.create(teiList, messageId);
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof D2CallException) {
            D2CallException exception = (D2CallException) throwable;
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

    private void getTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes()
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
        if (selectedProgram.accessDataWrite())
            if (view.isEnabled()) {
                enroll(selectedProgram.uid(), null);
            } else
                this.view.displayMessage(view.getContext().getString(R.string.search_program_not_selected));
        else
            this.view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void enroll(String programUid, String uid) {
        OrgUnitDialog orgUnitDialog = OrgUnitDialog.newInstace(false);
        orgUnitDialog.setTitle("Enrollment Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null)
                        enrollInOrgUnit(orgUnitDialog.getSelectedOrgUnit(), programUid, uid);
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        compositeDisposable.add(getOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgUnits -> {
                            if (orgUnits.size() > 1) {
                                orgUnitDialog.setOrgUnits(orgUnits);
                                orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                            } else
                                enrollInOrgUnit(orgUnits.get(0).uid(), programUid, uid);
                        },
                        Timber::d
                )
        );
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String uid) {
        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), orgUnitUid, programUid, uid, queryData)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentUid -> {
                                    FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(enrollmentUid);
                                    this.view.getContext().startActivity(FormActivity.create(this.view.getAbstractActivity(), formViewArguments, true));
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
    public void addRelationship(String TEIuid, String relationshipTypeUid, boolean isA, boolean online) {
        if (!online) {
            String relationshipType;
            if (relationshipTypeUid == null)
                relationshipType = selectedProgram.relationshipType();
            else
                relationshipType = relationshipTypeUid;

            Intent intent = new Intent();
            if (isA)
                intent.putExtra("TEI_A_UID", TEIuid);
            else
                intent.putExtra("TEI_B_UID", TEIuid);
            intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipType);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            downloadTeiForRelationship(TEIuid, relationshipTypeUid, isA);
        }
    }

    @Override
    public void downloadTei(String teiUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(teiUid);
        compositeDisposable.add(
                Flowable.fromCallable(d2.downloadTrackedEntityInstancesByUid(teiUids))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> openDashboard(data.get(0).uid()),
                                t -> Log.d("ONLINE_SEARCH", t.getMessage()))
        );

    }

    @Override
    public void downloadTeiForRelationship(String TEIuid, String relationshipTypeUid, boolean isA) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(TEIuid);
        compositeDisposable.add(
                Flowable.fromCallable(d2.downloadTrackedEntityInstancesByUid(teiUids))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> addRelationship(TEIuid, relationshipTypeUid, isA, false),
                                t -> Log.d("ONLINE_SEARCH", t.getMessage()))
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
}
