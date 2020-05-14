package org.dhis2.uicomponents.map.geometry.common

import com.mapbox.geojson.Feature
import org.dhis2.uicomponents.map.geometry.bound.BoundsModel
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class MapGeometryToFeature (private val pointMapper: MapPointToFeature,
                            private val polygonMapper: MapPolygonToFeature){

    fun map (geometry: Geometry, property: String, propertyValue: String,
             bounds: BoundsModel) : Feature? {
        return when {
            geometry.type() == FeatureType.POINT -> {
                val pairPointBounds = pointMapper.map(geometry, bounds)
                val point = pairPointBounds?.first
                point?.addStringProperty(property, propertyValue)
                point
            }

            geometry.type() == FeatureType.POLYGON -> {
                val polygon = polygonMapper.map(geometry, bounds)?.first
                polygon?.addStringProperty(property, propertyValue)
                polygon
            }
            else -> null
        }
    }
}