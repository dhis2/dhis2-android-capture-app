package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Intent;
import android.os.Bundle;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
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

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class RelationshipPresenterImpl implements RelationshipContracts.Presenter {

    private final D2 d2;
    private final CompositeDisposable compositeDisposable;
    private final DashboardRepository dashboardRepository;
    private final String teiUid;
    private final String teiType;
    private final String programUid;
    private RelationshipContracts.View view;
    private FlowableProcessor<Boolean> updateRelationships;


    RelationshipPresenterImpl(D2 d2, String programUid, String teiUid, DashboardRepository dashboardRepository) {
        this.programUid = programUid;
        this.compositeDisposable = new CompositeDisposable();
        this.d2 = d2;
        this.teiUid = teiUid;
        this.dashboardRepository = dashboardRepository;
        this.updateRelationships = PublishProcessor.create();

        teiType = d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).withAllChildren().get().trackedEntityType();
    }

    @Override
    public void init(RelationshipContracts.View view) {
        this.view = view;

        compositeDisposable.add(
                updateRelationships.startWith(true)
                        .flatMap(update ->
                                Flowable.fromIterable(
                                        d2.relationshipModule().relationships.getByItem(
                                                RelationshipItem.builder().trackedEntityInstance(
                                                        RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teiUid).build()).build()
                                        ))
                                        .filter(relationship -> relationship.from().trackedEntityInstance().trackedEntityInstance().equals(teiUid))
                                        .map(relationship -> {
                                            RelationshipType relationshipType = null;
                                            for (RelationshipType type : d2.relationshipModule().relationshipTypes.get())
                                                if (type.uid().equals(relationship.relationshipType()))
                                                    relationshipType = type;
                                            return Pair.create(relationship, relationshipType);
                                        })
                                        .toList().toFlowable()
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.setRelationships(),
                                Timber::d
                        )
        );


        compositeDisposable.add(
                dashboardRepository.relationshipsForTeiType(teiType)
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
                                view.setRelationshipTypes(),
                                Timber::e
                        )
        );
    }

    @Override
    public void goToAddRelationship(String teiTypeToAdd) {
        if (d2.programModule().programs.uid(programUid).withAllChildren().get().access().data().write()) {
            Intent intent = new Intent(view.getContext(), SearchTEActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean("FROM_RELATIONSHIP", true);
            extras.putString("FROM_RELATIONSHIP_TEI", teiUid);
            extras.putString("TRACKED_ENTITY_UID", teiTypeToAdd);
            extras.putString("PROGRAM_UID", null);
            intent.putExtras(extras);
            view.goToAddRelationship(intent);
        } else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void deleteRelationship(Relationship relationship) {
        try {
            d2.relationshipModule().relationships.withAllChildren().uid(relationship.uid()).delete();
        } catch (D2Error e) {
            Timber.d(e);
        } finally {
            updateRelationships.onNext(true);
        }
    }

    @Override
    public void addRelationship(String trackEntityInstance_A, String relationshipType) {
        try {
            Relationship relationship = RelationshipHelper.teiToTeiRelationship(teiUid, trackEntityInstance_A, relationshipType);
            d2.relationshipModule().relationships.add(relationship);
        } catch (D2Error e) {
            view.displayMessage(e.errorDescription());
        } finally {
            updateRelationships.onNext(true);
        }
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

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid) {
        return dashboardRepository.mainTrackedEntityAttributes(teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public String getTeiUid() {
        return teiUid;
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}
