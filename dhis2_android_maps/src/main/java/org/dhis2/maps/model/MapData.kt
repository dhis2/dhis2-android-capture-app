package org.dhis2.maps.model

import androidx.compose.runtime.Stable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection

@Stable
data class MapData(
    val featureCollection: FeatureCollection,
    val boundingBox: BoundingBox?,
)
