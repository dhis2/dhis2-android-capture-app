package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipHelper;
import org.hisp.dhis.android.core.relationship.RelationshipItem;
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class RelationshipPresenterImpl implements RelationshipPresenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private final D2 d2;
    private TeiDashboardContracts.View view;

    private String teiUid;
    private String teType;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    public RelationshipPresenterImpl(D2 d2, DashboardRepository dashboardRepository,
                                     MetadataRepository metadataRepository,
                                     String programUid, String teiUid) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        compositeDisposable = new CompositeDisposable();
        this.teiUid = teiUid;
        this.programUid = programUid;
    }

    @Override
    public void init(TeiDashboardContracts.View view) {
        this.view = view;
        dashboardRepository.setDashboardDetails(teiUid, programUid);
        getData();
    }

    @Override
    public LiveData<DashboardProgramModel> observeDashboardModel() {
        return dashboardProgramModelLiveData;
    }

    @Override
    public String getTeiUid() {
        return teiUid;
    }

    @SuppressLint({"CheckResult"})
    private void getData() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getEnrollment(programUid, teiUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teiUid),
                    metadataRepository.getTeiOrgUnit(teiUid, programUid),
                    metadataRepository.getTeiActivePrograms(teiUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.dashboardProgramModelLiveData.setValue(dashboardModel);
                                this.programWritePermission = dashboardProgramModel.getCurrentProgram().accessDataWrite();
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teiUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teiUid),
                    metadataRepository.getTeiOrgUnit(teiUid),
                    metadataRepository.getTeiActivePrograms(teiUid),
                    metadataRepository.getTEIEnrollments(teiUid),
                    DashboardProgramModel::new)
                    .flatMap(dashboardProgramModel1 -> metadataRepository.getObjectStylesForPrograms(dashboardProgramModel1.getEnrollmentProgramModels())
                            .map(stringObjectStyleMap -> {
                                dashboardProgramModel1.setProgramsObjectStyles(stringObjectStyleMap);
                                return dashboardProgramModel1;
                            }))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e)
            );
        }
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid) {
        return dashboardRepository.mainTrackedEntityAttributes(teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public void goToAddRelationship(String teiTypeToAdd) {
        if (programWritePermission) {
            Fragment relationshipFragment = RelationshipFragment.getInstance();
            Intent intent = new Intent(view.getContext(), SearchTEActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean("FROM_RELATIONSHIP", true);
            extras.putString("FROM_RELATIONSHIP_TEI", teiUid);
            extras.putString("TRACKED_ENTITY_UID", teiTypeToAdd);
            extras.putString("PROGRAM_UID", null);
            intent.putExtras(extras);
            relationshipFragment.startActivityForResult(intent, Constants.REQ_ADD_RELATIONSHIP);
        } else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void addRelationship(String trackEntityInstanceA, String relationshipType) {
        try {
            Relationship relationship = RelationshipHelper.teiToTeiRelationship(teiUid, trackEntityInstanceA, relationshipType);
            d2.relationshipModule().relationships.add(relationship);
        } catch (D2Error e) {
            view.displayMessage(e.errorDescription());
        }
    }


    @Override
    public void deleteRelationship(Relationship relationship) {
        try {
            d2.relationshipModule().relationships.withAllChildren().uid(relationship.uid()).delete();
        } catch (D2Error e) {
            Timber.d(e);
        } finally {
            subscribeToRelationships(RelationshipFragment.getInstance());
        }
    }

    @Override
    public void subscribeToRelationships(RelationshipFragment relationshipFragment) {
        compositeDisposable.add(
                Flowable.just(
                        d2.relationshipModule().relationships.getByItem(
                                RelationshipItem.builder().trackedEntityInstance(
                                        RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teiUid).build()).build()
                        ))
                        .flatMapIterable(list -> list)
                        .filter(relationship -> relationship.from().trackedEntityInstance().trackedEntityInstance().equals(teiUid))
                        .map(relationship -> {
                            RelationshipType relationshipType = null;
                            for (RelationshipType type : d2.relationshipModule().relationshipTypes.get())
                                if (type.uid().equals(relationship.relationshipType()))
                                    relationshipType = type;
                            return Pair.create(relationship, relationshipType);
                        })
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                relationshipFragment.setRelationships(),
                                Timber::d
                        )
        );
    }


    @Override
    public void subscribeToRelationshipTypes(RelationshipFragment relationshipFragment) {
        compositeDisposable.add(
                dashboardRepository.relationshipsForTeiType(teType)
                        .map(list -> {
                            List<Trio<RelationshipTypeModel, String, Integer>> finalList = new ArrayList<>();
                            for (Pair<RelationshipTypeModel, String> rType : list) {
                                int iconResId = dashboardRepository.getObjectStyle(view.getAbstracContext(), rType.val1());
                                finalList.add(Trio.create(rType.val0(), rType.val1(), iconResId));
                            }
                            return finalList;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                RelationshipFragment.getInstance().setRelationshipTypes(),
                                Timber::e
                        )
        );
    }

    @Override
    public void openDashboard(String teiUid) {
        Intent intent = new Intent(view.getContext(), TeiDashboardMobileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", teiUid);
        bundle.putString("PROGRAM_UID", null);
        intent.putExtras(bundle);
        view.getAbstractActivity().startActivity(intent);
    }
}