package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox
import org.dhis2.uicomponents.map.geometry.getLatLngPointList
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage
import org.dhis2.uicomponents.map.geometry.mapper.addTeiEventInfo
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.hisp.dhis.android.core.common.FeatureType

class MapTeiEventsToFeatureCollection(
    private val mapPointToFeature: MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature,
    private val bounds: GetBoundingBox
) {

    fun map(
        events: List<EventUiComponentModel>
    ): Pair<EventsByProgramStage, BoundingBox> {
        val eventsByProgramStage = events
            .groupBy { it.stageDisplayName }
            .mapValues { eventModel ->
                eventModel.value.map {
                    val feature = it.event.geometry()?.let { event ->
                        if (event.type() == FeatureType.POINT) {
                            mapPointToFeature.map(event)
                        } else {
                            mapPolygonToFeature.map(event)
                        }
                    }
                    feature?.addTeiEventInfo(it)
                }
            }

        val featureCollectionMap = eventsByProgramStage.mapValues {
            FeatureCollection.fromFeatures(it.value)
        }

        val featureCollection = EventsByProgramStage(EVENT, featureCollectionMap)

        val latLngList = eventsByProgramStage.values.flatten().getLatLngPointList()

        return Pair(
            featureCollection,
            bounds.getEnclosingBoundingBox(latLngList)
        )
    }

    companion object {
        const val EVENT = "EVENT"
        const val EVENT_UID = "eventUid"
        const val STAGE_UID = "stageUid"
    }
}
