package org.dhis2.usescases.programEventDetail

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection

data class ProgramEventMapData(
    val events: MutableList<out ProgramEventViewModel>,
    val featureCollectionMap: MutableMap<String, FeatureCollection>,
    val boundingBox: BoundingBox
)
