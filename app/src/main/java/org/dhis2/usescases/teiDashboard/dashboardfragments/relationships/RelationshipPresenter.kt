package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.ArrayList
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Trio
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.mapper.MapRelationshipToRelationshipMapModel
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_RELATIONSHIP
import org.dhis2.utils.analytics.NEW_RELATIONSHIP
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import org.hisp.dhis.android.core.relationship.RelationshipType
import timber.log.Timber

class RelationshipPresenter internal constructor(
    private val view: RelationshipView,
    private val d2: D2,
    private val programUid: String,
    private val teiUid: String,
    private val dashboardRepository: DashboardRepository,
    private val schedulerProvider: SchedulerProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val mapRelationshipToRelationshipMapModel: MapRelationshipToRelationshipMapModel,
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection
) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val teiType: String? =
        d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues()
            .uid(teiUid)
            .blockingGet().trackedEntityType()
    private var updateRelationships: FlowableProcessor<Boolean> = PublishProcessor.create()

    fun init() {
        compositeDisposable.add(
            updateRelationships.startWith(true)
                .flatMap { dashboardRepository.listTeiRelationships() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        view.setRelationships(it)
                        val relationshipModel = mapRelationshipToRelationshipMapModel.mapList(it)
                        view.setFeatureCollection(mapRelationshipsToFeatureCollection.map(relationshipModel))
                    },
                    { Timber.d(it) }
                )
        )

        compositeDisposable.add(
            dashboardRepository.relationshipsForTeiType(teiType)
                .map { list ->
                    val finalList = ArrayList<Trio<RelationshipType, String, Int>>()
                    for (rType in list) {
                        val iconResId =
                            dashboardRepository.getObjectStyle(rType.val1())!!
                        finalList.add(Trio.create(rType.val0(), rType.val1(), iconResId))
                    }
                    finalList
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.initFab(it.toMutableList()) },
                    { Timber.e(it) }
                )
        )
    }

    fun goToAddRelationship(teiTypeToAdd: String) {
        if (d2.programModule()
            .programs().uid(programUid).blockingGet()!!.access().data().write()!!
        ) {
            analyticsHelper.setEvent(NEW_RELATIONSHIP, CLICK, NEW_RELATIONSHIP)
            view.goToAddRelationship(teiUid, teiTypeToAdd)
        } else {
            view.showPermissionError()
        }
    }

    fun deleteRelationship(relationship: Relationship) {
        try {
            d2.relationshipModule().relationships().withItems().uid(relationship.uid())
                .blockingDelete()
        } catch (e: D2Error) {
            Timber.d(e)
        } finally {
            analyticsHelper.setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP)
            updateRelationships.onNext(true)
        }
    }

    fun addRelationship(selectedTei: String, relationshipTypeUid: String) {
        val relationshipType =
            d2.relationshipModule().relationshipTypes().withConstraints().uid(relationshipTypeUid)
                .blockingGet()

        val fromTei: String
        val toTei: String
        if (relationshipType!!.bidirectional()!! &&
            relationshipType.toConstraint()!!.trackedEntityType()!!.uid() == teiType
        ) {
            fromTei = selectedTei
            toTei = teiUid
        } else {
            fromTei = teiUid
            toTei = selectedTei
        }

        try {
            val relationship =
                RelationshipHelper.teiToTeiRelationship(fromTei, toTei, relationshipTypeUid)
            d2.relationshipModule().relationships().blockingAdd(relationship)
        } catch (e: D2Error) {
            view.displayMessage(e.errorDescription())
        } finally {
            updateRelationships.onNext(true)
        }
    }

    fun openDashboard(teiUid: String) {
        if (d2.trackedEntityModule()
            .trackedEntityInstances().uid(teiUid).blockingGet()!!.state() !=
            State.RELATIONSHIP
        ) {
            if (d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid).blockingGet().isNotEmpty()
            ) {
                view.openDashboardFor(teiUid)
            } else {
                view.showTeiWithoutEnrollmentError(
                    d2.trackedEntityModule()
                        .trackedEntityTypes().uid(teiType).blockingGet()!!.displayName() ?: ""
                )
            }
        } else {
            view.showRelationshipNotFoundError(
                d2.trackedEntityModule()
                    .trackedEntityTypes().uid(teiType).blockingGet()!!.displayName() ?: ""
            )
        }
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }
}
