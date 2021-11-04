package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import java.util.HashMap
import org.dhis2.commons.data.SearchTeiModel

data class TrackerMapData(
    val teiModels: MutableList<SearchTeiModel>,
    val eventFeatures: org.dhis2.android_maps.geometry.mapper.EventsByProgramStage,
    val teiFeatures: HashMap<String, FeatureCollection>,
    val teiBoundingBox: BoundingBox,
    val eventModels: MutableList<org.dhis2.android_maps.model.EventUiComponentModel>,
    val dataElementFeaturess: MutableMap<String, FeatureCollection>
)
