package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.dhis2.data.dhislogic.CoordinateAttributeInfo
import org.dhis2.data.dhislogic.CoordinateDataElementInfo
import org.dhis2.data.dhislogic.CoordinateFieldInfo
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature

class MapCoordinateFieldToFeatureCollection(
    private val mapGeometryToFeature: MapGeometryToFeature,
    private val bounds: BoundsGeometry
) {
    private fun map(coordinateDataElementInfo: CoordinateDataElementInfo): Feature? {
        bounds.initOrReset()

        return mapGeometryToFeature.map(
            coordinateDataElementInfo.geometry,
            hashMapOf(
                FIELD_NAME to coordinateDataElementInfo.dataElement.displayFormName()!!,
                EVENT to coordinateDataElementInfo.event.uid()!!,
                STAGE to coordinateDataElementInfo.stage.displayName()!!
            ).apply {
                coordinateDataElementInfo.enrollment?.let { enrollment ->
                    put(TEI, enrollment.trackedEntityInstance()!!)
                }
            },
            bounds
        )
    }

    private fun map(coordinateAttributeInfo: CoordinateAttributeInfo): Feature? {
        bounds.initOrReset()

        return mapGeometryToFeature.map(
            coordinateAttributeInfo.geometry,
            hashMapOf(
                FIELD_NAME to coordinateAttributeInfo.attribute.displayFormName()!!,
                TEI to coordinateAttributeInfo.tei.uid()!!
            ),
            bounds
        )
    }

    fun map(coordinateFieldInfos: List<CoordinateFieldInfo>): Map<String, FeatureCollection> {
        return when {
            coordinateFieldInfos.any { it is CoordinateDataElementInfo } -> {
                mapDataElement(coordinateFieldInfos as List<CoordinateDataElementInfo>)
            }
            coordinateFieldInfos.any { it is CoordinateAttributeInfo } -> {
                mapAttribute(coordinateFieldInfos as List<CoordinateAttributeInfo>)
            }
            else -> {
                emptyMap()
            }
        }
    }

    private fun mapDataElement(
        coordinateDataElementInfos: List<CoordinateDataElementInfo>
    ): Map<String, FeatureCollection> {
        return mutableMapOf<String, FeatureCollection>().apply {
            val featureMap = mutableMapOf<String, MutableList<Feature>>()
            coordinateDataElementInfos.forEach {
                val key = it.dataElement.displayFormName()!!
                map(it)?.let { feature ->
                    if (!featureMap.containsKey(key)) {
                        featureMap[key] = mutableListOf()
                    }
                    featureMap[key]!!.add(feature)
                }
            }
            val finalMap = featureMap.map {
                it.key to FeatureCollection.fromFeatures(it.value)
            }
            putAll(finalMap)
        }
    }

    private fun mapAttribute(
        coordinateAttributeInfos: List<CoordinateAttributeInfo>
    ): Map<String, FeatureCollection> {
        return mutableMapOf<String, FeatureCollection>().apply {
            val featureMap = mutableMapOf<String, MutableList<Feature>>()
            coordinateAttributeInfos.forEach {
                val key = it.attribute.displayFormName()!!
                map(it)?.let { feature ->
                    if (!featureMap.containsKey(key)) {
                        featureMap[key] = mutableListOf()
                    }
                    featureMap[key]!!.add(feature)
                }
            }
            val finalMap = featureMap.map {
                it.key to FeatureCollection.fromFeatures(it.value)
            }
            putAll(finalMap)
        }
    }

    companion object {
        const val EVENT = "eventUid"
        const val STAGE = "stageUid"
        const val TEI = "teiUid"
        const val FIELD_NAME = "fieldName"
    }
}
