package org.dhis2.uicomponents.map.geometry.mapper

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.hisp.dhis.android.core.event.Event

class MapEventToFeatureCollection(
    private val mapGeometryToFeature: MapGeometryToFeature,
    private val bounds: BoundsGeometry
) {
    fun map(eventList: List<Event>): Pair<FeatureCollection, BoundingBox> {
        bounds.initOrReset()
        val features = eventList.filter { it.geometry() != null }.mapNotNull {
            mapGeometryToFeature.map(it.geometry()!!, EVENT, it.uid()!!, bounds)
        }

        return Pair<FeatureCollection, BoundingBox>(
            FeatureCollection.fromFeatures(features),
            BoundingBox.fromLngLats(
                bounds.westBound,
                bounds.southBound,
                bounds.eastBound,
                bounds.northBound
            )
        )
    }

    companion object {
        const val EVENT = "eventUid"
    }
}
