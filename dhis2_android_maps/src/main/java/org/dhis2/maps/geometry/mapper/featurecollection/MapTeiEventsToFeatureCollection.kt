package org.dhis2.maps.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.getLatLngPointList
import org.dhis2.maps.geometry.mapper.EventsByProgramStage
import org.dhis2.maps.geometry.mapper.addTeiEventInfo
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.model.MapItemModel
import org.hisp.dhis.android.core.common.FeatureType

class MapTeiEventsToFeatureCollection(
    private val mapPointToFeature: MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature,
    private val bounds: GetBoundingBox,
) {

    fun map(events: List<MapItemModel>): Pair<EventsByProgramStage, BoundingBox> {
        val eventsByProgramStage = events
            .filter { it.relatedInfo?.event?.stageDisplayName != null }
            .groupBy { it.relatedInfo?.event?.stageDisplayName!! }
            .mapValues { eventModel ->
                eventModel.value.mapNotNull {
                    val feature = it.geometry?.let { eventGeometry ->
                        if (eventGeometry.type() == FeatureType.POINT) {
                            mapPointToFeature.map(eventGeometry)
                        } else {
                            mapPolygonToFeature.map(eventGeometry)
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
            bounds.getEnclosingBoundingBox(latLngList),
        )
    }

    companion object {
        const val EVENT = "EVENT"
        const val EVENT_UID = "eventUid"
        const val STAGE_UID = "stageUid"
    }
}
