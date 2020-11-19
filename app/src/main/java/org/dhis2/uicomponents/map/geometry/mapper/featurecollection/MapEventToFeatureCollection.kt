package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox
import org.dhis2.uicomponents.map.geometry.getLatLngPointList
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.hisp.dhis.android.core.event.Event

class MapEventToFeatureCollection(
    private val mapGeometryToFeature: MapGeometryToFeature,
    private val bounds: GetBoundingBox
) {
    fun map(eventList: List<Event>): Pair<FeatureCollection, BoundingBox> {
        val features = eventList.filter { it.geometry() != null }.mapNotNull {
            mapGeometryToFeature.map(
                it.geometry()!!,
                mapOf(EVENT to it.uid())
            )
        }

        return Pair<FeatureCollection, BoundingBox>(
            FeatureCollection.fromFeatures(features),
            bounds.getEnclosingBoundingBox(features.getLatLngPointList())
        )
    }

    companion object {
        const val EVENT = "eventUid"
    }
}
