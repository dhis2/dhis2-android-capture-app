package org.dhis2.maps.model

import androidx.compose.runtime.Stable
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.FeatureCollection

@Stable
data class MapData(
    val featureCollection: FeatureCollection,
    val boundingBox: BoundingBox?,
)
