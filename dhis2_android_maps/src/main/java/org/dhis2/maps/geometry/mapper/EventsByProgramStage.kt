package org.dhis2.maps.geometry.mapper

import com.mapbox.geojson.FeatureCollection

data class EventsByProgramStage(
    val tag: String,
    val featureCollectionMap: Map<String, FeatureCollection>,
)
