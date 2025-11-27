package org.dhis2.maps.geometry.mapper.featurecollection

import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.dhis2.maps.utils.CoordinateDataElementInfo
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import javax.inject.Inject

class MapDataElementToFeature
    @Inject
    constructor(
        private val mapCoordinateFieldToFeature: MapCoordinateFieldToFeature,
    ) {
        fun mapDataElement(coordinateDataElementInfos: List<CoordinateDataElementInfo>): Map<String, FeatureCollection> =
            mutableMapOf<String, FeatureCollection>().apply {
                val featureMap = mutableMapOf<String, MutableList<Feature>>()
                coordinateDataElementInfos.forEach {
                    val key = it.dataElement.displayFormName()!!
                    mapCoordinateFieldToFeature.map(it)?.let { feature ->
                        if (!featureMap.containsKey(key)) {
                            featureMap[key] = mutableListOf()
                        }
                        featureMap[key]?.add(feature)
                    }
                }
                val finalMap =
                    featureMap.map {
                        it.key to FeatureCollection.fromFeatures(it.value)
                    }
                putAll(finalMap)
            }
    }
