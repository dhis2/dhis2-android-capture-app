package org.dhis2.uicomponents.map.geometry.line

import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import org.dhis2.uicomponents.map.geometry.areLngLatCorrect
import org.dhis2.uicomponents.map.geometry.closestPointTo
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class MapLineRelationshipToFeature {

    fun map(relationshipUiComponentModel: RelationshipUiComponentModel): Feature? {
        val (lineStartPoint, lineEndPoint) = getStartPoints(
            relationshipUiComponentModel.from.geometry!!,
            relationshipUiComponentModel.to.geometry!!
        )

        val lonLineStart = lineStartPoint[0]
        val latLineStart = lineStartPoint[1]
        val lonLineEnd = lineEndPoint[0]
        val latLineEnd = lineEndPoint[1]

        if (areLngLatCorrect(lonLineStart, latLineStart) &&
            areLngLatCorrect(lonLineEnd, latLineEnd)
        ) {
            val firstPoint = Point.fromLngLat(lonLineStart, latLineStart)
            val secondPoint = Point.fromLngLat(lonLineEnd, latLineEnd)
            return Feature.fromGeometry(LineString.fromLngLats(listOf(firstPoint, secondPoint)))
        }
        return null
    }

    private fun getPoint(geo: Geometry): List<Double> {
        return when (geo.type()) {
            FeatureType.POINT -> GeometryHelper.getPoint(geo)
            FeatureType.POLYGON -> GeometryHelper.getPolygon(geo)[0][0]
            else -> {
                arrayListOf(0.0, 0.0)
            }
        }
    }

    private fun getStartPoints(
        fromGeometry: Geometry,
        toGeometry: Geometry
    ): Pair<List<Double>, List<Double>> {
        return if (fromGeometry.type() == FeatureType.POINT &&
            toGeometry.type() == FeatureType.POINT
        ) {
            Pair(GeometryHelper.getPoint(fromGeometry), GeometryHelper.getPoint(toGeometry))
        } else if (fromGeometry.type() == FeatureType.POINT &&
            toGeometry.type() == FeatureType.POLYGON
        ) {
            Pair(
                GeometryHelper.getPoint(fromGeometry),
                GeometryHelper.getPolygon(toGeometry)
                    .closestPointTo(GeometryHelper.getPoint(fromGeometry))
            )
        } else if (fromGeometry.type() == FeatureType.POLYGON &&
            toGeometry.type() == FeatureType.POINT
        ) {
            Pair(
                GeometryHelper.getPolygon(fromGeometry)
                    .closestPointTo(GeometryHelper.getPoint(toGeometry)),
                GeometryHelper.getPoint(toGeometry)
            )
        } else if (fromGeometry.type() == FeatureType.POLYGON &&
            toGeometry.type() == FeatureType.POLYGON
        ) {
            GeometryHelper.getPolygon(fromGeometry)
                .closestPointTo(GeometryHelper.getPolygon(toGeometry))
        } else {
            return Pair(arrayListOf(0.0, 0.0), arrayListOf(0.0, 0.0))
        }
    }
}
