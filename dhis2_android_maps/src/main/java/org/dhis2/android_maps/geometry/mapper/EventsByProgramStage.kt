package org.dhis2.android_maps.geometry.mapper

import com.mapbox.geojson.FeatureCollection

data class EventsByProgramStage(
    val tag: String,
    val featureCollectionMap: Map<String, FeatureCollection>
)
