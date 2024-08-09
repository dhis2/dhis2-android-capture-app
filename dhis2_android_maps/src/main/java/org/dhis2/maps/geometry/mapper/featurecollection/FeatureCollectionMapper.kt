package org.dhis2.maps.geometry.mapper.featurecollection

import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.mapper.MapGeometryToFeature
import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature

class FeatureCollectionMapper {

    val eventToFeatureCollection: MapEventToFeatureCollection by lazy {
        MapEventToFeatureCollection(
            mapGeometryToFeature = geometryToFeature,
            bounds = bounds,
        )
    }

    val coordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection by lazy {
        MapCoordinateFieldToFeatureCollection(
            mapDataElementToFeature = dataElementToFeature,
            mapAttributeToFeature = attributeToFeature,
        )
    }

    private val geometryToFeature: MapGeometryToFeature by lazy {
        MapGeometryToFeature(
            pointMapper = pointToFeature,
            polygonMapper = polygonToFeature,
        )
    }

    private val pointToFeature: MapPointToFeature by lazy {
        MapPointToFeature()
    }

    private val polygonToFeature: MapPolygonToFeature by lazy {
        MapPolygonToFeature()
    }

    private val bounds: GetBoundingBox by lazy { GetBoundingBox() }

    private val dataElementToFeature by lazy {
        MapDataElementToFeature(
            mapCoordinateFieldToFeature = fieldToFeature,
        )
    }

    private val attributeToFeature by lazy {
        MapAttributeToFeature(
            mapCoordinateFieldToFeature = fieldToFeature,
        )
    }

    private val fieldToFeature by lazy {
        MapCoordinateFieldToFeature(
            mapGeometryToFeature = geometryToFeature,
        )
    }
}
