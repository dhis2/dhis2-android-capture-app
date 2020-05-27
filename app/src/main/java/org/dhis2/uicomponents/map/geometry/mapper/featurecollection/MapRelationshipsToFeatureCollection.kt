package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.line.MapLineToFeature
import org.dhis2.uicomponents.map.model.RelationshipMapModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper

class MapRelationshipsToFeatureCollection(
    private val mapLineToFeature: MapLineToFeature,
    private val bounds: BoundsGeometry
) {
    fun map(relationships: List<RelationshipMapModel>): Pair<FeatureCollection, BoundingBox> {
        val features = relationships.map {
            val lineStartPoint = GeometryHelper.getPoint(it.from.geometry!!)
            val lineEndPoint = GeometryHelper.getPoint(it.To.geometry!!)

            val lonLineStart = lineStartPoint[0]
            val latLineStart = lineStartPoint[1]
            val lonLineEnd = lineEndPoint[0]
            val latLineEnd = lineEndPoint[1]

            val firstPoint = Point.fromLngLat(lonLineStart, latLineStart)
            val secondPoint = Point.fromLngLat(lonLineEnd, latLineEnd)
            Feature.fromGeometry(LineString.fromLngLats(listOf(firstPoint, secondPoint)))
        }

        val featureCollection = FeatureCollection.fromFeatures(features)
        return Pair(featureCollection,BoundingBox.fromLngLats(0.0,0.0,0.0,0.0))
    }

    companion object {
        const val TEI = "teiUid"
    }
}