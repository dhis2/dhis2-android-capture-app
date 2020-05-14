package org.dhis2.uicomponents.map.geometry.common

import com.mapbox.geojson.Feature
import org.dhis2.uicomponents.map.geometry.bound.BoundsModel
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class MapGeometryToFeature (private val pointMapper: MapPointToFeature){

    fun map (geometry: Geometry, property: String, propertyValue: String,
             bounds: BoundsModel) : Feature? {
        return when {
            geometry.type() == FeatureType.POINT -> {
                val pairPointBounds = pointMapper.map(geometry, bounds)
                val point = pairPointBounds?.first
                point?.addStringProperty(property, propertyValue)
                return point
            }
            geometry.type() == FeatureType.POLYGON -> {
                null
            }
            else -> null
        }
    }

}