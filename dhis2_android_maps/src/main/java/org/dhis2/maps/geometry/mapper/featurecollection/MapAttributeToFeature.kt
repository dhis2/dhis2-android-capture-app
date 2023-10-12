package org.dhis2.maps.geometry.mapper.featurecollection

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import javax.inject.Inject
import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.dhis2.maps.utils.CoordinateAttributeInfo

class MapAttributeToFeature @Inject constructor(
    private val mapCoordinateFieldToFeature: MapCoordinateFieldToFeature
) {

    fun mapAttribute(
        coordinateAttributeInfos: List<CoordinateAttributeInfo>
    ): Map<String, FeatureCollection> {
        return mutableMapOf<String, FeatureCollection>().apply {
            val featureMap = mutableMapOf<String, MutableList<Feature>>()
            coordinateAttributeInfos.forEach {
                val key = it.attribute.displayFormName()!!
                mapCoordinateFieldToFeature.map(it)?.let { feature ->
                    if (!featureMap.containsKey(key)) {
                        featureMap[key] = mutableListOf()
                    }
                    featureMap[key]?.add(feature)
                }
            }
            val finalMap = featureMap.map {
                it.key to FeatureCollection.fromFeatures(it.value)
            }
            putAll(finalMap)
        }
    }
}
