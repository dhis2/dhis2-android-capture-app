package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox
import org.dhis2.uicomponents.map.geometry.line.MapLineRelationshipToFeature
import org.dhis2.uicomponents.map.geometry.mapper.addRelationshipInfo
import org.dhis2.uicomponents.map.model.RelationshipMapModel

class MapRelationshipsToFeatureCollection(
    private val mapLineToFeature: MapLineRelationshipToFeature,
    private val bounds: GetBoundingBox
) {
    fun map(relationships: List<RelationshipMapModel>): Pair<Map<String?, FeatureCollection>, BoundingBox> {
        val relationshipByName =
            relationships.groupBy { it.displayName }.mapValues { relationModels ->
                relationModels.value.map {
                    val feature = mapLineToFeature.map(it)
                    feature?.addRelationshipInfo(it)
                }
            }

        val featuresList = relationshipByName.values.flatten()
        val latLongList = getLngLatAsCoordinateList(featuresList)

        return Pair<Map<String?, FeatureCollection>, BoundingBox>(
            relationshipByName.mapValues { FeatureCollection.fromFeatures(it.value) },
            bounds.getEnclosingBoundingBox(latLongList)
        )
    }

    private fun getLngLatAsCoordinateList(features: List<Feature?>): List<LatLng> {
        return features.map {
            val point = it?.geometry() as Point
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