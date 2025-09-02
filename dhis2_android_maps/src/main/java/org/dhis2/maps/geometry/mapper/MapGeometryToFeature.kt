package org.dhis2.maps.geometry.mapper

import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.maplibre.geojson.Feature

class MapGeometryToFeature(
    private val pointMapper: MapPointToFeature,
    private val polygonMapper: MapPolygonToFeature,
) {
    fun map(
        geometry: Geometry,
        propertyMap: Map<String, String>,
    ): Feature? =
        when {
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
