package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.commons.data.CarouselItemModel
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel
import java.util.HashMap

data class TrackerMapData(
    val teiModels: MutableList<SearchTeiModel>,
    val eventFeatures: org.dhis2.maps.geometry.mapper.EventsByProgramStage,
    val teiFeatures: HashMap<String, FeatureCollection>,
    val teiBoundingBox: BoundingBox,
    val eventModels: MutableList<org.dhis2.maps.model.EventUiComponentModel>,
    val dataElementFeaturess: MutableMap<String, FeatureCollection>,
) {
    fun allItems() = mutableListOf<CarouselItemModel>().apply {
        addAll(teiModels)
        addAll(eventModels)
        teiModels.forEach {
            addAll(
                MapRelationshipToRelationshipMapModel().mapList(it.relationships),
            )
        }
    }
}
