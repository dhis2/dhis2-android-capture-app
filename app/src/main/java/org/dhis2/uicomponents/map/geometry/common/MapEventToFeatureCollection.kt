package org.dhis2.uicomponents.map.geometry.common

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.uicomponents.map.geometry.bound.BoundsModel
import org.hisp.dhis.android.core.event.Event

class MapEventToFeatureCollection(private val mapGeometryToFeature: MapGeometryToFeature, private val boundsModel: BoundsModel) {

    fun map (eventList: List<Event>): Pair<FeatureCollection, BoundingBox> {

        val features = eventList.filter { it.geometry() != null }.mapNotNull {
            mapGeometryToFeature.map(it.geometry()!!, EVENT, it.uid()!!, boundsModel)
        }

        return Pair<FeatureCollection, BoundingBox>(
                FeatureCollection.fromFeatures(features),
                BoundingBox.fromLngLats(boundsModel.westBound, boundsModel.southBound, boundsModel.eastBound, boundsModel.northBound)
        )
    }

    companion object {
        const val EVENT = "eventUid"
    }
}