package org.dhis2.usescases.programEventDetail

import org.dhis2.maps.model.MapItemModel
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.FeatureCollection

data class ProgramEventMapData(
    val mapItems: List<MapItemModel>,
    val featureCollectionMap: MutableMap<String, FeatureCollection>,
    val boundingBox: BoundingBox,
)
