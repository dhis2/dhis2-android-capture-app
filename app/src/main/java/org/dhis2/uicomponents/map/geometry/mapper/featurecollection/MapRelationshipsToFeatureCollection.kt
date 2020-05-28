package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox
import org.dhis2.uicomponents.map.geometry.line.MapLineToFeature
import org.dhis2.uicomponents.map.model.RelationshipMapModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper

class MapRelationshipsToFeatureCollection(
    private val mapLineToFeature: MapLineToFeature,
    private val bounds: GetBoundingBox
) {
    fun map(relationships: List<RelationshipMapModel>): Pair<FeatureCollection, BoundingBox> {
        val featuresList = relationships.map {
            val lineStartPoint = GeometryHelper.getPoint(it.from.geometry!!)
            val lineEndPoint = GeometryHelper.getPoint(it.To.geometry!!)

            val lonLineStart = lineStartPoint[0]
            val latLineStart = lineStartPoint[1]
            val lonLineEnd = lineEndPoint[0]
            val latLineEnd = lineEndPoint[1]

            val firstPoint = Point.fromLngLat(lonLineStart, latLineStart)
            val secondPoint = Point.fromLngLat(lonLineEnd, latLineEnd)

            Feature.fromGeometry(LineString.fromLngLats(listOf(firstPoint, secondPoint))).apply {
                addStringProperty(RELATIONSHIP, it.relationshipTypeUid)
                addBooleanProperty(BIDIRECTIONAL, it.bidirectional)
                addStringProperty(FROM_TEI, it.from.teiUid)
                addStringProperty(TO_TEI, it.To.teiUid)
            }
        }

        val latLongList = getLngLatAsCoordinateList(featuresList)
        val enclosingBoundingBox = bounds.getEnclosingBoundingBox(latLongList)

        val featureCollection = FeatureCollection.fromFeatures(featuresList)
        
        return Pair(featureCollection, enclosingBoundingBox)
    }

    private fun getLngLatAsCoordinateList(features : List<Feature>) : List<LatLng> {
        return features.map {
            val point = it.geometry() as Point
            LatLng(point.longitude(), point.latitude())
        }
    }

    companion object {
        const val FROM_TEI = "fromTeiUid"
        const val TO_TEI = "toTeiUid"
        const val RELATIONSHIP = "relationshipTypeUid"
        const val BIDIRECTIONAL = "bidirectional"
    }
}