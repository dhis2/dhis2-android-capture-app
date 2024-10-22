package org.dhis2.usescases.programEventDetail

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.maps.model.MapItemModel

data class ProgramEventMapData(
    val mapItems: List<MapItemModel>,
    val featureCollectionMap: MutableMap<String, FeatureCollection>,
    val boundingBox: BoundingBox,
)
