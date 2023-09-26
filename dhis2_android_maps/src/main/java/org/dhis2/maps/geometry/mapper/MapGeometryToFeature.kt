package org.dhis2.maps.geometry.mapper

import com.mapbox.geojson.Feature
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class MapGeometryToFeature(
    private val pointMapper: MapPointToFeature,
    private val polygonMapper: MapPolygonToFeature,
) {
    fun map(geometry: Geometry, propertyMap: Map<String, String>): Feature? {
        return when {
            geometry.type() == FeatureType.POINT -> {
                val point = pointMapper.map(geometry)
                propertyMap.entries.forEach {
                    point?.addStringProperty(it.key, it.value)
                }
                point
            }
            geometry.type() == FeatureType.POLYGON -> {
                val polygon = polygonMapper.map(geometry)
                propertyMap.entries.forEach {
                    polygon?.addStringProperty(it.key, it.value)
                }
                polygon
            }
            else -> null
        }
    }
}
