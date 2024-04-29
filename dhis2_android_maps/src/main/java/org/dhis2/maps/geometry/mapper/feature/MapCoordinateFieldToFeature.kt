package org.dhis2.maps.geometry.mapper.feature

import com.mapbox.geojson.Feature
import org.dhis2.maps.extensions.FeatureSource
import org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE
import org.dhis2.maps.geometry.mapper.MapGeometryToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.EVENT
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.FIELD_NAME
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.STAGE
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.TEI
import org.dhis2.maps.utils.CoordinateAttributeInfo
import org.dhis2.maps.utils.CoordinateDataElementInfo

class MapCoordinateFieldToFeature(private val mapGeometryToFeature: MapGeometryToFeature) {

    fun map(coordinateDataElementInfo: CoordinateDataElementInfo): Feature? {
        return mapGeometryToFeature.map(
            coordinateDataElementInfo.geometry,
            hashMapOf(
                PROPERTY_FEATURE_SOURCE to FeatureSource.FIELD.name,
                FIELD_NAME to coordinateDataElementInfo.dataElement.displayFormName()!!,
                EVENT to coordinateDataElementInfo.event.uid()!!,
                STAGE to coordinateDataElementInfo.stage.displayName()!!,
            ).apply {
                coordinateDataElementInfo.enrollment?.let { enrollment ->
                    put(
                        TEI,
                        enrollment.trackedEntityInstance()!!,
                    )
                }
            },
        )
    }

    fun map(coordinateAttributeInfo: CoordinateAttributeInfo): Feature? {
        return mapGeometryToFeature.map(
            coordinateAttributeInfo.geometry,
            hashMapOf(
                PROPERTY_FEATURE_SOURCE to FeatureSource.FIELD.name,
                FIELD_NAME to coordinateAttributeInfo.attribute.displayFormName()!!,
                TEI to coordinateAttributeInfo.tei.uid()!!,
            ),
        )
    }
}
