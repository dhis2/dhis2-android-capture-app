package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_RELATIONSHIP
import org.dhis2.utils.analytics.NEW_RELATIONSHIP
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import org.hisp.dhis.android.core.relationship.RelationshipType
import timber.log.Timber

class RelationshipPresenter internal constructor(
    private val view: RelationshipView,
    private val d2: D2,
    private val programUid: String?,
    private val teiUid: String?,
    private val eventUid: String?,
    private val relationshipRepository: RelationshipRepository,
    private val schedulerProvider: SchedulerProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val mapRelationshipToRelationshipMapModel: MapRelationshipToRelationshipMapModel,
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection,
    private val mapStyleConfig: MapStyleConfiguration,
) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val teiType: String? =
        d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues()
            .uid(teiUid)
            .blockingGet()?.trackedEntityType()
    private val programStageUid =
        d2.eventModule().events().uid(eventUid).blockingGet()?.programStage()

    var updateRelationships: FlowableProcessor<Boolean> = PublishProcessor.create()

    fun init() {
        compositeDisposable.add(
            updateRelationships.startWith(true)
                .flatMapSingle { relationshipRepository.relationships() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        view.setRelationships(it)
                        val relationshipModel = mapRelationshipToRelationshipMapModel.mapList(it)
                        view.setFeatureCollection(
                            teiUid,
                            relationshipModel,
                            mapRelationshipsToFeatureCollection.map(relationshipModel),
                        )
                    },
                    { Timber.d(it) },
                ),
        )

        compositeDisposable.add(
            relationshipRepository.relationshipTypes()
                .map { list ->
                    val finalList = ArrayList<Trio<RelationshipType, String, Int>>()
                    for (rType in list) {
                        val iconResId =
                            relationshipRepository.getTeiTypeDefaultRes(rType.second)
                        finalList.add(Trio.create(rType.first, rType.second, iconResId))
                    }
                    finalList
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.initFab(it.toMutableList()) },
                    { Timber.e(it) },
                ),
        )
    }

    fun goToAddRelationship(teiTypeToAdd: String, relationshipType: RelationshipType) {
        val writeAccess =
            d2.relationshipModule().relationshipService().hasAccessPermission(relationshipType)

        if (writeAccess) {
            analyticsHelper.setEvent(NEW_RELATIONSHIP, CLICK, NEW_RELATIONSHIP)
            if (teiUid != null) {
                view.goToAddRelationship(teiUid, teiTypeToAdd)
            } else if (eventUid != null) {
                view.goToAddRelationship(eventUid, teiTypeToAdd)
            }
        } else {
            view.showPermissionError()
        }
    }

    fun deleteRelationship(relationshipUid: String) {
        try {
            d2.relationshipModule().relationships().withItems().uid(relationshipUid)
                .blockingDelete()
        } catch (e: D2Error) {
            Timber.d(e)
        } finally {
            analyticsHelper.setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP)
            updateRelationships.onNext(true)
        }
    }

    fun addRelationship(selectedTei: String, relationshipTypeUid: String) {
        if (teiUid != null) {
            addTeiToTeiRelationship(teiUid, selectedTei, relationshipTypeUid)
        } else if (eventUid != null) {
            addEventToTeiRelationship(eventUid, selectedTei, relationshipTypeUid)
        }
    }

    private fun addTeiToTeiRelationship(
        teiUid: String,
        selectedTei: String,
        relationshipTypeUid: String,
    ) {
        try {
            val relationship =
                RelationshipHelper.teiToTeiRelationship(teiUid, selectedTei, relationshipTypeUid)
            d2.relationshipModule().relationships().blockingAdd(relationship)
        } catch (e: D2Error) {
            view.displayMessage(e.errorDescription())
        } finally {
            updateRelationships.onNext(true)
        }
    }

    private fun addEventToTeiRelationship(
        eventUid: String,
        selectedTei: String,
        relationshipTypeUid: String,
    ) {
        try {
            val relationship =
                RelationshipHelper.eventToTeiRelationship(
                    eventUid,
                    selectedTei,
                    relationshipTypeUid,
                )
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
                        .trackedEntityTypes().uid(teiType).blockingGet()?.displayName() ?: "",
                )
            }
        } else {
            view.showRelationshipNotFoundError(
                d2.trackedEntityModule()
                    .trackedEntityTypes().uid(teiType).blockingGet()?.displayName() ?: "",
            )
        }
    }

    fun openEvent(eventUid: String, eventProgramUid: String) {
        view.openEventFor(eventUid, eventProgramUid)
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun onRelationshipClicked(ownerType: RelationshipOwnerType, ownerUid: String) {
        when (ownerType) {
            RelationshipOwnerType.EVENT -> openEvent(
                ownerUid,
                relationshipRepository.getEventProgram(ownerUid),
            )
            RelationshipOwnerType.TEI -> openDashboard(ownerUid)
        }
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }
}
