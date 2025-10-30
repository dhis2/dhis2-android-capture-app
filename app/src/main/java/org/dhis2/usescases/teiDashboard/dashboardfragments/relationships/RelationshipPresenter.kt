package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.extensions.filterRelationshipsByLayerVisibility
import org.dhis2.maps.extensions.toStringProperty
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.ui.AvatarProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.NEW_RELATIONSHIP
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.maplibre.geojson.Feature

class RelationshipPresenter internal constructor(
    private val view: RelationshipView,
    private val d2: D2,
    private val teiUid: String?,
    private val eventUid: String?,
    private val relationshipMapsRepository: RelationshipMapsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection,
    private val mapStyleConfig: MapStyleConfiguration,
    private val relationshipsRepository: RelationshipsRepository,
    private val avatarProvider: AvatarProvider,
    private val dateLabelProvider: DateLabelProvider,
    dispatcherProvider: DispatcherProvider,
) {
    private lateinit var layersVisibility: Map<String, MapLayer>

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val teiType: String? =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .withTrackedEntityAttributeValues()
            .uid(teiUid)
            .blockingGet()
            ?.trackedEntityType()
    private var updateRelationships: FlowableProcessor<Boolean> = PublishProcessor.create()

    private val relationshipsModels = MutableLiveData<List<RelationshipModel>>()
    private val _relationshipMapData: MutableLiveData<RelationshipMapData> = MutableLiveData()
    val relationshipMapData: LiveData<RelationshipMapData> = _relationshipMapData
    private val _mapItemClicked = MutableLiveData<String>()
    val mapItemClicked: LiveData<String> = _mapItemClicked
    private val job = Job()
    private val scope = CoroutineScope(dispatcherProvider.ui() + job)

    fun init() {
        scope.launch {
            relationshipsRepository.getRelationships().let {
                relationshipsModels.postValue(it)

                val mapItems =
                    it.map { relationship ->
                        val mapItem =
                            MapItemModel(
                                uid = relationship.ownerUid,
                                avatarProviderConfiguration =
                                    avatarProvider.getAvatar(
                                        icon = relationship.ownerStyleIcon,
                                        color = relationship.ownerStyleColor,
                                        profilePath = relationship.getPicturePath(),
                                        firstAttributeValue = relationship.firstMainValue(),
                                    ),
                                title = relationship.displayRelationshipName(),
                                description = relationship.displayDescription(),
                                lastUpdated = dateLabelProvider.span(relationship.displayLastUpdated()),
                                additionalInfoList =
                                    relationship.displayAttributes().map {
                                        AdditionalInfoItem(
                                            key = it.first,
                                            value = it.second,
                                        )
                                    },
                                isOnline = false,
                                geometry =
                                    relationship.displayGeometry()?.let { relationshipGeometry ->
                                        Geometry
                                            .builder()
                                            .type(
                                                relationshipGeometry.featureType?.let { name ->
                                                    FeatureType.valueOf(
                                                        name,
                                                    )
                                                },
                                            ).coordinates(relationshipGeometry.coordinates)
                                            .build()
                                    },
                                relatedInfo =
                                    relationshipMapsRepository.getRelatedInfo(
                                        ownerType = relationship.ownerType,
                                        ownerUid = relationship.ownerUid,
                                    ),
                                state = State.valueOf(relationship.relationshipState),
                            )
                        relationshipMapsRepository.addRelationshipInfo(
                            mapItem,
                            relationship.relationshipUid,
                        )
                    }

                mapItems.let {
                    val featureCollection = mapRelationshipsToFeatureCollection.map(mapItems)
                    val relationshipMapData =
                        if (::layersVisibility.isInitialized) {
                            RelationshipMapData(
                                mapItems =
                                    mapItems.filterRelationshipsByLayerVisibility(
                                        layersVisibility,
                                    ),
                                relationshipFeatures = featureCollection.first,
                                boundingBox = featureCollection.second,
                            )
                        } else {
                            RelationshipMapData(
                                mapItems = mapItems,
                                relationshipFeatures = featureCollection.first,
                                boundingBox = featureCollection.second,
                            )
                        }
                    _relationshipMapData.postValue(relationshipMapData)
                }
            }
        }
    }

    fun goToAddRelationship(
        relationshipTypeUid: String,
        teiTypeToAdd: String?,
    ) {
        val writeAccess = relationshipsRepository.hasWritePermission(relationshipTypeUid)

        if (writeAccess) {
            analyticsHelper.setEvent(NEW_RELATIONSHIP, CLICK, NEW_RELATIONSHIP)

            val originUid = teiUid ?: eventUid

            if (originUid != null && teiTypeToAdd != null) {
                view.goToAddRelationship(originUid, teiTypeToAdd)
            }
        } else {
            view.showPermissionError()
        }
    }

    fun openDashboard(teiUid: String) {
        if (d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid(teiUid)
                .blockingGet()
                ?.aggregatedSyncState() !=
            State.RELATIONSHIP
        ) {
            if (d2
                    .enrollmentModule()
                    .enrollments()
                    .byTrackedEntityInstance()
                    .eq(teiUid)
                    .blockingGet()
                    .isNotEmpty()
            ) {
                view.openDashboardFor(teiUid)
            } else {
                view.showTeiWithoutEnrollmentError(
                    d2
                        .trackedEntityModule()
                        .trackedEntityTypes()
                        .uid(teiType)
                        .blockingGet()
                        ?.displayName() ?: "",
                )
            }
        } else {
            view.showRelationshipNotFoundError(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypes()
                    .uid(teiType)
                    .blockingGet()
                    ?.displayName() ?: "",
            )
        }
    }

    private fun openEvent(
        eventUid: String,
        eventProgramUid: String,
    ) {
        view.openEventFor(eventUid, eventProgramUid)
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun onRelationshipClicked(
        ownerType: RelationshipOwnerType,
        ownerUid: String,
    ) {
        when (ownerType) {
            RelationshipOwnerType.EVENT ->
                openEvent(
                    ownerUid,
                    relationshipMapsRepository.getEventProgram(ownerUid),
                )

            RelationshipOwnerType.TEI -> openDashboard(ownerUid)
        }
    }

    fun fetchMapStyles(): List<BaseMapStyle> = mapStyleConfig.fetchMapStyles()

    fun onFeatureClicked(feature: Feature) {
        feature.toStringProperty()?.let {
            _mapItemClicked.postValue(it)
        }
    }

    fun filterVisibleMapItems(layersVisibility: Map<String, MapLayer>) {
        this.layersVisibility = layersVisibility
    }

    fun onMapRelationshipClicked(uid: String) {
        val relationship = relationshipsModels.value?.firstOrNull { uid == it.ownerUid }
        relationship?.let {
            onRelationshipClicked(
                ownerType = it.ownerType,
                ownerUid = it.ownerUid,
            )
        }
    }
}
