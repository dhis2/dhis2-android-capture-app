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
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelationshipDirection
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.hisp.dhis.android.core.common.FeatureType
import org.jetbrains.annotations.NotNull

class MapRelationshipsToFeatureCollection(
    private val mapLineToFeature: @NotNull MapLineRelationshipToFeature,
    private val mapPointToFeature: @NotNull MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature,
    private val bounds: @NotNull GetBoundingBox,
) {
    @Deprecated("")
    fun mapLegacy(
        relationships: List<RelationshipUiComponentModel>,
    ): Pair<Map<String, FeatureCollection>, BoundingBox> {
        val relationshipByName = relationships
            .groupBy { it.displayName!! }
            .mapValues { relationModels ->
                val lineFeatures = relationModels.value.mapNotNull {
                    val feature = mapLineToFeature.map(it.from.geometry!!, it.to.geometry!!)
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

        return Pair<Map<String, FeatureCollection>, BoundingBox>(
            relationshipByName.mapValues {
                FeatureCollection.fromFeatures(
                    it.value,
                )
            },
            bounds.getEnclosingBoundingBox(latLongList),
        )
    }

    fun map(
        relationships: List<MapItemModel>,
    ): Pair<Map<String, FeatureCollection>, BoundingBox> {
        val relationshipByName = relationships
            .filter { it.relatedInfo?.relationship?.displayName != null }
            .groupBy { it.relatedInfo?.relationship?.displayName!! }
            .mapValues { relationModels ->

                val lineFeatures = relationModels.value.groupBy { it.relatedInfo?.relationship?.uid }.mapNotNull { relationships ->
                    if (relationships.key != null) {
                        val from = relationships.value.find { it.relatedInfo?.relationship?.relationshipDirection == RelationshipDirection.FROM }
                        val to = relationships.value.find { it.relatedInfo?.relationship?.relationshipDirection == RelationshipDirection.TO }
                        if (from?.geometry != null && to?.geometry != null) {
                            val feature = mapLineToFeature.map(
                                fromGeometry = from.geometry,
                                toGeometry = to.geometry,
                            )
//                            feature?.addRelationshipInfo(it)
                            feature
                        } 
                        else {
                            null
                        }
                    } else {
                        null
                    }
                }

                /*val lineFeatures = relationModels.value.mapNotNull {
                    val feature = mapLineToFeature.map(fromGeometry =, toGeometry = )
                    feature?.addRelationshipInfo(it)
                }*/
                val pointFeatures = relationModels.value.mapNotNull { relationModel ->
                    relationModel.geometry?.let {
                        val feature = if (it.type() == FeatureType.POINT) {
                            mapPointToFeature.map(it)
                        } else {
                            mapPolygonToFeature.map(it)
                        }
                        feature?.addRelationshipInfo(relationModel)
                    }
                }
                listOf(lineFeatures, pointFeatures).flatten()
            }

        val latLongList = relationshipByName.values.flatten().getLatLngPointList()

        return Pair<Map<String, FeatureCollection>, BoundingBox>(
            relationshipByName.mapValues {
                FeatureCollection.fromFeatures(
                    it.value,
                )
            },
            bounds.getEnclosingBoundingBox(latLongList),
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
