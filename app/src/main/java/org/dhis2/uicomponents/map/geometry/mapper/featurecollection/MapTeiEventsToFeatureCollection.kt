package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.addTeiEventInfo
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.hisp.dhis.android.core.common.FeatureType

class MapTeiEventsToFeatureCollection(
    private val bounds: BoundsGeometry,
    private val mapPointToFeature: MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature
) {

    fun map(
        events: List<EventUiComponentModel>
    ): Pair<HashMap<String, FeatureCollection>, BoundingBox> {
        val featureMap: HashMap<String, ArrayList<Feature>> = HashMap()
        featureMap[EVENT] = ArrayList()
        bounds.initOrReset()

        events.map { eventModel ->
            val feature: Feature? = eventModel.event.geometry()?.let {
                if (it.type() == FeatureType.POINT) {
                    mapPointToFeature.map(it)
                } else {
                    mapPolygonToFeature.map(it)
                }
            }
            feature?.addTeiEventInfo(eventModel)?.also {
                featureMap[EVENT]!!.add(it)
            }
        }

        val featureCollectionMap = HashMap<String, FeatureCollection>()
        featureCollectionMap[EVENT] = FeatureCollection.fromFeatures(featureMap[EVENT] as ArrayList)

        return Pair<HashMap<String, FeatureCollection>, BoundingBox>(
            featureCollectionMap,
            BoundingBox.fromLngLats(
                bounds.westBound,
                bounds.southBound,
                bounds.eastBound,
                bounds.northBound
            )
        )
    }

    companion object {
        const val EVENT = "EVENT"
        const val EVENT_UID = "eventUid"
    }
}
