package org.dhis2.uicomponents.map.geometry.line

import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import org.dhis2.uicomponents.map.geometry.areLngLatCorrect
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
                getPolygonCloserPoint(
                    GeometryHelper.getPolygon(toGeometry),
                    GeometryHelper.getPoint(fromGeometry)
                )
            )
        } else if (fromGeometry.type() == FeatureType.POLYGON &&
            toGeometry.type() == FeatureType.POINT
        ) {
            Pair(
                getPolygonCloserPoint(
                    GeometryHelper.getPolygon(fromGeometry),
                    GeometryHelper.getPoint(toGeometry)
                ),
                GeometryHelper.getPoint(toGeometry)
            )
        } else if (fromGeometry.type() == FeatureType.POLYGON &&
            toGeometry.type() == FeatureType.POLYGON
        ) {
            getCloserPoints(
                GeometryHelper.getPolygon(fromGeometry),
                GeometryHelper.getPolygon(toGeometry)
            )
        } else {
            return Pair(arrayListOf(0.0, 0.0), arrayListOf(0.0, 0.0))
        }
    }

    private fun getPolygonCloserPoint(
        polygonCoordinates: List<List<List<Double>>>,
        pointCoordinate: List<Double>
    ): List<Double> {
        val initPoint = LatLng(pointCoordinate[1], pointCoordinate[0])
        var closestPoint: List<Double>? = null
        var closestDistance: Double? = null
        for (points in polygonCoordinates[0]) {
            val distance = LatLng(points[1], points[0]).distanceTo(initPoint)
            if (closestDistance == null || distance < closestDistance) {
                closestPoint = points
                closestDistance = distance
            }
        }
        return closestPoint!!
    }

    private fun getCloserPoints(
        fromPolCoordinates: List<List<List<Double>>>,
        toPolCoordinates: List<List<List<Double>>>
    ): Pair<List<Double>, List<Double>> {
        return fromPolCoordinates[0].map { fromPoint ->
            val toPoint = getPolygonCloserPoint(toPolCoordinates, fromPoint)
            Pair(fromPoint, toPoint)
        }.minBy { fromToPoints ->
            LatLng(fromToPoints.first[1], fromToPoints.first[0]).distanceTo(
                LatLng(fromToPoints.second[1], fromToPoints.second[0])
            )
        } ?: Pair(arrayListOf(0.0, 0.0), arrayListOf(0.0, 0.0))
    }
}
