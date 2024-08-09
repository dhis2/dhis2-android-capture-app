package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.maps.model.MapItemModel
import java.util.HashMap

data class TrackerMapData(
    val eventFeatures: org.dhis2.maps.geometry.mapper.EventsByProgramStage,
    val mapItems: List<MapItemModel>,
    val teiFeatures: HashMap<String, FeatureCollection>,
    val teiBoundingBox: BoundingBox,
    val dataElementFeaturess: MutableMap<String, FeatureCollection>,
)
