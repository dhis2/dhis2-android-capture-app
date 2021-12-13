package org.dhis2.maps.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.getLatLngPointList
import org.dhis2.maps.geometry.line.MapLineRelationshipToFeature
import org.dhis2.maps.geometry.mapper.addRelationFromInfo
import org.dhis2.maps.geometry.mapper.addRelationToInfo
import org.dhis2.maps.geometry.mapper.addRelationshipInfo
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.hisp.dhis.android.core.common.FeatureType
import org.jetbrains.annotations.NotNull

class MapRelationshipsToFeatureCollection(
    private val mapLineToFeature: @NotNull MapLineRelationshipToFeature,
    private val mapPointToFeature: @NotNull MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature,
    private val bounds: @NotNull GetBoundingBox
) {
    fun map(
        relationships: List<RelationshipUiComponentModel>
    ): Pair<Map<String?, FeatureCollection>, BoundingBox> {
        val relationshipByName = relationships
            .groupBy { it.displayName }
            .mapValues { relationModels ->
                val lineFeatures = relationModels.value.mapNotNull {
                    val feature = mapLineToFeature.map(it)
                    feature?.addRelationshipInfo(it)
                }
                val pointFromFeatures = relationModels.value.mapNotNull { relationModel ->
                    relationModel.from.geometry?.let {
                        val feature = if (it.type() == FeatureType.POINT) {
                            mapPointToFeature.map(it)
                        } else {
                            mapPolygonToFeature.map(it)
                        }
                        feature?.addRelationFromInfo(relationModel)
                    }
                }
                val pointToFeatures = relationModels.value.mapNotNull { relationModel ->
                    relationModel.to.geometry?.let {
                        val feature = if (it.type() == FeatureType.POINT) {
                            mapPointToFeature.map(it)
                        } else {
                            mapPolygonToFeature.map(it)
                        }
                        feature?.addRelationToInfo(relationModel)
                    }
                }
                listOf(lineFeatures, pointFromFeatures, pointToFeatures).flatten()
            }

        val latLongList = relationshipByName.values.flatten().getLatLngPointList()

        return Pair<Map<String?, FeatureCollection>, BoundingBox>(
            relationshipByName.mapValues {
                FeatureCollection.fromFeatures(
                    it.value
                )
            },
            bounds.getEnclosingBoundingBox(latLongList)
        )
    }

    companion object {
        const val FROM_TEI = "fromTeiUid"
        const val TO_TEI = "toTeiUid"
        const val RELATIONSHIP = "relationshipTypeUid"
        const val BIDIRECTIONAL = "bidirectional"
        const val RELATIONSHIP_UID = "relationshipUid"
    }
}
