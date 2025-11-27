package org.dhis2.maps.geometry.mapper

import org.maplibre.geojson.FeatureCollection

data class EventsByProgramStage(
    val tag: String,
    val featureCollectionMap: Map<String, FeatureCollection>,
)
