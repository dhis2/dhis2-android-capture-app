package org.dhis2.usescases.searchTrackEntity

import org.dhis2.maps.model.MapItemModel
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.FeatureCollection
import java.util.HashMap

data class TrackerMapData(
    val eventFeatures: org.dhis2.maps.geometry.mapper.EventsByProgramStage,
    val mapItems: List<MapItemModel>,
    val teiFeatures: HashMap<String, FeatureCollection>,
    val teiBoundingBox: BoundingBox,
    val dataElementFeaturess: MutableMap<String, FeatureCollection>,
)
