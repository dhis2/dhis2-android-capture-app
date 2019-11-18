package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.OnDialogClickListener;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipHelper;
import org.hisp.dhis.android.core.relationship.RelationshipItem;
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute;

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

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_RELATIONSHIP;
import static org.dhis2.utils.analytics.AnalyticsConstants.NEW_RELATIONSHIP;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class RelationshipPresenterImpl {

    private final D2 d2;
    private final CompositeDisposable compositeDisposable;
    private final DashboardRepository dashboardRepository;
    private final String teiUid;
    private final String teiType;
    private final String programUid;
    private final SchedulerProvider schedulerProvider;
    private RelationshipView view;
    public FlowableProcessor<Boolean> updateRelationships;


    public RelationshipPresenterImpl(D2 d2, String programUid, String teiUid,
                                     DashboardRepository dashboardRepository,
                                     SchedulerProvider schedulerProvider,
                                     RelationshipView view) {
        this.programUid = programUid;
        this.compositeDisposable = new CompositeDisposable();
        this.d2 = d2;
        this.teiUid = teiUid;
        this.dashboardRepository = dashboardRepository;
        this.schedulerProvider = schedulerProvider;
        this.updateRelationships = PublishProcessor.create();
        this.view = view;

        teiType = d2.trackedEntityModule().trackedEntityInstances().byUid().eq(teiUid)
                .withTrackedEntityAttributeValues().one().blockingGet().trackedEntityType();
    }

    public void init() {
        updateRelationships();

        relationshipForTeiType();
    }

    public void updateRelationships() {
        compositeDisposable.add(
                updateRelationships.startWith(true)
                        .flatMap(update ->
                                Flowable.fromIterable(
                                        d2.relationshipModule().relationships().getByItem(
                                                RelationshipItem.builder().trackedEntityInstance(
                                                        RelationshipItemTrackedEntityInstance
                                                                .builder()
                                                                .trackedEntityInstance(teiUid)
                                                                .build()).build()
                                        ))
                                        .map(relationship -> {
                                            RelationshipType relationshipType = null;
                                            for (RelationshipType type : d2.relationshipModule()
                                                    .relationshipTypes().blockingGet())
                                                if (type.uid()
                                                        .equals(relationship.relationshipType()))
                                                    relationshipType = type;

                                            String relationshipTEIUid;
                                            RelationshipViewModel.RelationshipDirection direction;
                                            if (!teiUid.equals(relationship.from()
                                                    .trackedEntityInstance()
                                                    .trackedEntityInstance())) {
                                                relationshipTEIUid = relationship.from()
                                                        .trackedEntityInstance()
                                                        .trackedEntityInstance();
                                                direction = RelationshipViewModel
                                                        .RelationshipDirection.FROM;
                                            } else {
                                                relationshipTEIUid = relationship.to()
                                                        .trackedEntityInstance()
                                                        .trackedEntityInstance();
                                                direction = RelationshipViewModel
                                                        .RelationshipDirection.TO;
                                            }

                                            TrackedEntityInstance tei = d2.trackedEntityModule()
                                                    .trackedEntityInstances()
                                                    .withTrackedEntityAttributeValues()
                                                    .uid(relationshipTEIUid).blockingGet();
                                            List<TrackedEntityTypeAttribute> typeAttributes =
                                                    d2.trackedEntityModule()
                                                            .trackedEntityTypeAttributes()
                                                            .byTrackedEntityTypeUid()
                                                            .eq(tei.trackedEntityType())
                                                            .byDisplayInList().isTrue()
                                                            .blockingGet();
                                            List<String> attributeUids = new ArrayList<>();
                                            for (TrackedEntityTypeAttribute typeAttribute :
                                                    typeAttributes)
                                                attributeUids.add(typeAttribute
                                                        .trackedEntityAttribute()
                                                        .uid());
                                            List<TrackedEntityAttributeValue> attributeValues =
                                                    d2.trackedEntityModule()
                                                            .trackedEntityAttributeValues()
                                                            .byTrackedEntityInstance()
                                                            .eq(tei.uid())
                                                            .byTrackedEntityAttribute()
                                                            .in(attributeUids)
                                                            .blockingGet();

                                            return RelationshipViewModel.create(
                                                    relationship,
                                                    relationshipType,
                                                    direction,
                                                    relationshipTEIUid,
                                                    attributeValues);
                                        })
                                        .toList().toFlowable()
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setRelationships,
                                Timber::d
                        )
        );
    }

    public void relationshipForTeiType(){
        compositeDisposable.add(
                dashboardRepository.relationshipsForTeiType(teiType)
                        .map(list -> {
                            List<Trio<RelationshipType, String, Integer>> finalList = new ArrayList<>();
                            for (Pair<RelationshipType, String> rType : list) {
                                int iconResId = dashboardRepository.getObjectStyle(view.getAbstracContext(), rType.val1());
                                finalList.add(Trio.create(rType.val0(), rType.val1(), iconResId));
                            }
                            return finalList;
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setRelationshipTypes,
                                Timber::e
                        )
        );
    }

    public void goToAddRelationship(String teiTypeToAdd) {//need to change to UI
        if (d2.programModule().programs().uid(programUid).blockingGet().access().data().write()) {
            view.analyticsHelper().setEvent(NEW_RELATIONSHIP, CLICK, NEW_RELATIONSHIP);
            view.goToAddRelationship(teiUid, teiTypeToAdd);
        } else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    public void deleteRelationship(Relationship relationship) {
        try {
            d2.relationshipModule().relationships().withItems().uid(relationship.uid()).blockingDelete();
        } catch (D2Error e) {
            Timber.d(e);
        } finally {
            view.analyticsHelper().setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP);
            updateRelationships.onNext(true);
        }
    }

    public void addRelationship(String trackEntityInstance_A, String relationshipType) {
        try {
            Relationship relationship = RelationshipHelper.teiToTeiRelationship(teiUid, trackEntityInstance_A, relationshipType);
            d2.relationshipModule().relationships().blockingAdd(relationship);
        } catch (D2Error e) {
            view.displayMessage(e.errorDescription());
        } finally {
            updateRelationships.onNext(true);
        }
    }

    public void openDashboard(String teiUid) {
        if (d2.trackedEntityModule().trackedEntityInstances().byUid().eq(teiUid).one().blockingGet().state() != State.RELATIONSHIP) {
            if(!d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid).blockingGet().isEmpty()) {
                view.goToTeiDashboard(teiUid);
            }else{
                view.showDialogRelationshipWithoutEnrollment(d2.trackedEntityModule()
                        .trackedEntityTypes().uid(teiType).blockingGet().displayName());
            }
        } else {
            view.showDialogRelationshipNotFoundMessage(d2.trackedEntityModule()
                    .trackedEntityTypes().uid(teiType).blockingGet().displayName());
        }
    }

    public Observable<List<TrackedEntityAttributeValue>> getTEIMainAttributes(String teiUid) {
        return dashboardRepository.mainTrackedEntityAttributes(teiUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui());
    }

    public String getTeiUid() {
        return teiUid;
    }

    public void onDettach() {
        compositeDisposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}
