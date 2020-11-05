package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.dhis2.data.dhislogic.CoordinateDataElementInfo
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature

class MapDataElementToFeatureCollection(
    private val mapGeometryToFeature: MapGeometryToFeature,
    private val bounds: BoundsGeometry
) {
    private fun map(coordinateDataElementInfo: CoordinateDataElementInfo): Feature? {
        bounds.initOrReset()

        return mapGeometryToFeature.map(
            coordinateDataElementInfo.geometry,
            hashMapOf(
                DE_NAME to coordinateDataElementInfo.dataElement.displayFormName()!!,
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

    fun map(
        coordinateDataElementInfos: List<CoordinateDataElementInfo>
    ): Map<String, FeatureCollection> {
        return mutableMapOf<String, FeatureCollection>().apply {
            val featureMap = mutableMapOf<String, MutableList<Feature>>()
            coordinateDataElementInfos.forEach {
                val key = when (it.enrollment) {
                    null -> it.dataElement.displayFormName()!!
                    else -> it.dataElement.displayFormName()!!
                }
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
        const val DE_NAME = "dataElementName"
    }
}
