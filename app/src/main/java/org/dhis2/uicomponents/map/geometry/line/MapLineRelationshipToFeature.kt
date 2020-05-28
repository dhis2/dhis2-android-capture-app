package org.dhis2.uicomponents.map.geometry.line

import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import org.dhis2.uicomponents.map.geometry.areLngLatCorrect
import org.dhis2.uicomponents.map.model.RelationshipMapModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper

class MapLineRelationshipToFeature {

    fun map(relationshipMapModel: RelationshipMapModel): Feature? {
        val lineStartPoint = GeometryHelper.getPoint(relationshipMapModel.from.geometry!!)
        val lineEndPoint = GeometryHelper.getPoint(relationshipMapModel.To.geometry!!)

        val lonLineStart = lineStartPoint[0]
        val latLineStart = lineStartPoint[1]
        val lonLineEnd = lineEndPoint[0]
        val latLineEnd = lineEndPoint[1]

        if (areLngLatCorrect(lonLineStart, latLineStart) &&
            areLngLatCorrect(lonLineEnd, latLineEnd)) {
            val firstPoint = Point.fromLngLat(lonLineStart, latLineStart)
            val secondPoint = Point.fromLngLat(lonLineEnd, latLineEnd)
            return Feature.fromGeometry(LineString.fromLngLats(listOf(firstPoint, secondPoint)))
        }
        return null
    }
}
