package org.dhis2.android_maps.geometry.mapper.featurecollection

import com.mapbox.geojson.FeatureCollection
import org.dhis2.android_maps.utils.CoordinateAttributeInfo
import org.dhis2.android_maps.utils.CoordinateDataElementInfo
import org.dhis2.android_maps.utils.CoordinateFieldInfo

class MapCoordinateFieldToFeatureCollection(
    private val mapDataElementToFeature: MapDataElementToFeature,
    private val mapAttributeToFeature: MapAttributeToFeature

) {

    fun map(coordinateFieldInfos: List<CoordinateFieldInfo>): Map<String, FeatureCollection> {
        return when {
            coordinateFieldInfos.any { it is CoordinateDataElementInfo } -> {
                mapDataElementToFeature.mapDataElement(
                    coordinateFieldInfos as List<CoordinateDataElementInfo>
                )
            }
            coordinateFieldInfos.any { it is CoordinateAttributeInfo } -> {
                mapAttributeToFeature.mapAttribute(
                    coordinateFieldInfos as List<CoordinateAttributeInfo>
                )
            }
            else -> {
                emptyMap()
            }
        }
    }

    companion object {
        const val EVENT = "eventUid"
        const val STAGE = "stageUid"
        const val TEI = "teiUid"
        const val FIELD_NAME = "fieldName"
    }
}
