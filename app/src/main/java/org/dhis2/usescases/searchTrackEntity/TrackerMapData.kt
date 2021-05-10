package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import java.util.HashMap
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel

data class TrackerMapData(
    val teiModels: MutableList<SearchTeiModel>,
    val eventFeatures: EventsByProgramStage,
    val teiFeatures: HashMap<String, FeatureCollection>,
    val teiBoundingBox: BoundingBox,
    val eventModels: MutableList<EventUiComponentModel>,
    val dataElementFeaturess: MutableMap<String, FeatureCollection>
)
