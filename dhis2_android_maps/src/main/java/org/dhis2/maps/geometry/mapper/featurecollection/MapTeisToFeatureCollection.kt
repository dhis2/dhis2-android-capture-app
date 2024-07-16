package org.dhis2.maps.geometry.mapper.featurecollection

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.dhis2.maps.geometry.bound.BoundsGeometry
import org.dhis2.maps.geometry.mapper.addTeiEnrollmentInfo
import org.dhis2.maps.geometry.mapper.addTeiInfo
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel
import org.dhis2.maps.model.MapItemModel
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class MapTeisToFeatureCollection(
    private val bounds: BoundsGeometry,
    private val mapPointToFeature: MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature,
    private val mapPolygonPointToFeature: MapPolygonPointToFeature,
    private val mapRelationshipToRelationshipMapModel: MapRelationshipToRelationshipMapModel,
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection,
) {

    fun map(
        mapItemList: List<MapItemModel>,
        shouldAddRelationships: Boolean,
        mapItemRelationshipList: List<MapItemModel>,
    ): Pair<HashMap<String, FeatureCollection>, BoundingBox> {
        val featureMap: HashMap<String?, ArrayList<Feature>> = HashMap()
        val featureCollectionMap = HashMap<String, FeatureCollection>()
        featureMap[TEI] = ArrayList()
        featureMap[ENROLLMENT] = ArrayList()
        bounds.initOrReset()

        mapItemList.forEach { mapItemModel ->
            if (mapItemModel.geometry != null) {
                val geometry = mapItemModel.geometry

                if (geometry.type() == FeatureType.POINT) {
                    mapToPointTei(geometry, mapItemModel, featureMap)
                } else if (geometry.type() == FeatureType.POLYGON) {
                    mapToPolygonTei(mapItemModel, featureMap, geometry)
                }
            }

            if (mapItemModel.relatedInfo?.enrollment?.geometry != null) {
                val geometry = mapItemModel.relatedInfo.enrollment.geometry

                if (geometry.type() == FeatureType.POINT) {
                    mapToPointEnrollment(geometry, mapItemModel, featureMap)
                } else if (geometry.type() == FeatureType.POLYGON) {
                    mapToPolygonEnrollment(geometry, mapItemModel, featureMap)
                }
            }

            if (shouldAddRelationships && mapItemRelationshipList.isNotEmpty()) {
                /*                val relationshipModels =
                                    mapRelationshipToRelationshipMapModel.mapList(searchTeiModel.relationships)*/
                val relationshipsFeatureCollections =
                    mapRelationshipsToFeatureCollection.map(mapItemRelationshipList)
                relationshipsFeatureCollections.first.forEach { (key, featureCollection) ->
                    featureCollectionMap[key]?.features()?.addAll(
                        featureCollection.features() ?: listOf(),
                    ) ?: run {
                        featureCollectionMap[key] = featureCollection
                    }
                }
            }
        }

        featureCollectionMap[TEI] = FeatureCollection.fromFeatures(featureMap[TEI] as ArrayList)
        featureCollectionMap[ENROLLMENT] =
            FeatureCollection.fromFeatures(featureMap[ENROLLMENT] as ArrayList)

        return Pair<HashMap<String, FeatureCollection>, BoundingBox>(
            featureCollectionMap,
            BoundingBox.fromLngLats(
                bounds.westBound,
                bounds.southBound,
                bounds.eastBound,
                bounds.northBound,
            ),
        )
    }

    private fun mapToPointTei(
        geometry: Geometry,
        mapItemModel: MapItemModel,
        featureMap: HashMap<String?, ArrayList<Feature>>,
    ) {
        val point = mapPointToFeature.map(geometry, bounds)?.first
        point?.addTeiInfo(mapItemModel)?.also { featureMap[TEI]?.add(it) }
    }

    private fun mapToPolygonTei(
        mapItemModel: MapItemModel,
        featureMap: HashMap<String?, ArrayList<Feature>>,
        geometry: Geometry,
    ) {
        val polygon = mapPolygonToFeature.map(geometry, bounds)?.first
        polygon?.addTeiInfo(mapItemModel)?.also { featureMap[TEI]?.add(it) }

        mapPolygonPointToFeature.map(geometry)?.apply {
            addStringProperty(TEI_UID, mapItemModel.uid)
            addStringProperty(TEI_IMAGE, mapItemModel.profilePicturePath())
        }?.also { featureMap[TEI]?.add(it) }
    }

    private fun mapToPointEnrollment(
        geometry: Geometry,
        mapItemModel: MapItemModel,
        featureMap: HashMap<String?, ArrayList<Feature>>,
    ) {
        val point = mapPointToFeature.map(geometry, bounds)?.first
        point?.addTeiEnrollmentInfo(mapItemModel)?.also {
            featureMap[ENROLLMENT]?.add(it)
        }
    }

    private fun mapToPolygonEnrollment(
        geometry: Geometry,
        mapItemModel: MapItemModel,
        featureMap: HashMap<String?, ArrayList<Feature>>,
    ) {
        val polygon = mapPolygonToFeature.map(geometry, bounds)?.first
        polygon?.addTeiEnrollmentInfo(mapItemModel)?.also {
            featureMap[ENROLLMENT]?.add(polygon)
        }

        val polygonPoint = mapPolygonPointToFeature.map(geometry)
        polygonPoint?.addTeiEnrollmentInfo(mapItemModel)?.also {
            featureMap[ENROLLMENT]?.add(it)
        }
    }

    companion object {
        const val TEI = "TEIS_SOURCE_ID"
        const val TEI_UID = "teiUid"
        const val TEI_IMAGE = "teiImage"
        const val ENROLLMENT = "ENROLLMENT_SOURCE_ID"
        const val ENROLLMENT_UID = "enrollmentUid"
    }
}
