package org.dhis2.uicomponents.map.geometry.mapper

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class MapTeisToFeatureCollection(
    private val bounds: BoundsGeometry,
    private val mapPointToFeature: MapPointToFeature,
    private val mapPolygonToFeature: MapPolygonToFeature,
    private val mapPolygonPointToFeature: MapPolygonPointToFeature
) {
    fun map(teiList: List<SearchTeiModel>): Pair<HashMap<String, FeatureCollection>, BoundingBox>? {
        val featureMap: HashMap<String, ArrayList<Feature>> = HashMap()
        featureMap[TEI] = ArrayList()
        featureMap[ENROLLMENT] = ArrayList()
        bounds.initOrReset()

        teiList.forEach { searchTeiModel ->
            if (teiHasCoordinates(searchTeiModel)) {
                val geometry = searchTeiModel.tei.geometry()!!

                if (geometry.type() == FeatureType.POINT) {
                    mapToPointTei(geometry, searchTeiModel, featureMap)
                } else if (geometry.type() == FeatureType.POLYGON) {
                    mapToPolygonTei(searchTeiModel, featureMap, geometry)
                }
            }

            if (teiEnrollmentHasCoordinates(searchTeiModel)) {
                val geometry = searchTeiModel.selectedEnrollment.geometry()!!

                if (geometry.type() == FeatureType.POINT) {
                    mapToPointEnrollment(geometry, searchTeiModel, featureMap)
                } else if (geometry.type() == FeatureType.POLYGON) {
                    mapToPolygonEnrollment(geometry, searchTeiModel, featureMap)
                }
            }
        }

        val featureCollectionMap = HashMap<String, FeatureCollection>()
        featureCollectionMap[TEI] = FeatureCollection.fromFeatures(featureMap[TEI] as ArrayList)
        featureCollectionMap[ENROLLMENT] =
            FeatureCollection.fromFeatures(featureMap[ENROLLMENT] as ArrayList)

        return Pair<HashMap<String, FeatureCollection>, BoundingBox>(
            featureCollectionMap,
            BoundingBox.fromLngLats(
                bounds.westBound, bounds.southBound,
                bounds.eastBound, bounds.northBound
            )
        )
    }

    private fun mapToPointTei(
        geometry: Geometry,
        searchTeiModel: SearchTeiModel,
        featureMap: HashMap<String, ArrayList<Feature>>
    ) {
        val point = mapPointToFeature.map(geometry, bounds)?.first
        point?.addTeiInfo(searchTeiModel)?.also { featureMap[TEI]!!.add(it) }
    }

    private fun mapToPolygonTei(
        searchTeiModel: SearchTeiModel,
        featureMap: HashMap<String, ArrayList<Feature>>,
        geometry: Geometry
    ) {
        val polygon = mapPolygonToFeature.map(geometry, bounds)?.first
        polygon?.addTeiInfo(searchTeiModel)?.also { featureMap[TEI]!!.add(it) }

        mapPolygonPointToFeature.map(geometry).apply {
            addStringProperty(TEI_UID, searchTeiModel.tei.uid())
            addStringProperty(TEI_IMAGE, searchTeiModel.profilePicturePath)
        }.also { featureMap[TEI]!!.add(it) }
    }

    private fun mapToPointEnrollment(
        geometry: Geometry,
        searchTeiModel: SearchTeiModel,
        featureMap: HashMap<String, ArrayList<Feature>>
    ) {
        val point = mapPointToFeature.map(geometry, bounds)?.first
        point.addTeiEnrollmentInfo(searchTeiModel)?.also { featureMap[ENROLLMENT]!!.add(it) }
    }

    private fun mapToPolygonEnrollment(
        geometry: Geometry,
        searchTeiModel: SearchTeiModel,
        featureMap: HashMap<String, ArrayList<Feature>>
    ) {
        val polygon = mapPolygonToFeature.map(geometry, bounds)?.first
        polygon.addTeiEnrollmentInfo(searchTeiModel)
            ?.also { featureMap[ENROLLMENT]!!.add(polygon!!) }

        val polygonPoint = mapPolygonPointToFeature.map(geometry)
        polygonPoint.addStringProperty(TEI_UID, searchTeiModel.tei.uid())
        featureMap[ENROLLMENT]!!.add(polygonPoint)
    }

    private fun teiHasCoordinates(searchTeiModel: SearchTeiModel) =
        searchTeiModel.tei.geometry() != null

    private fun teiEnrollmentHasCoordinates(searchTeiModel: SearchTeiModel) =
        searchTeiModel.selectedEnrollment != null &&
            searchTeiModel.selectedEnrollment.geometry() != null

    companion object {
        const val TEI = "TEI"
        const val TEI_UID = "teiUid"
        const val TEI_IMAGE = "teiImage"
        const val ENROLLMENT = "ENROLLMENT"
        const val ENROLLMENT_UID = "enrollmentUid"
    }
}
