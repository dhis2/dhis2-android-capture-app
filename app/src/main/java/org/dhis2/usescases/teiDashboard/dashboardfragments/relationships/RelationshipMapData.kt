package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.maps.model.MapItemModel

data class RelationshipMapData(
    val mapItems: List<MapItemModel>,
    val relationshipFeatures: Map<String, FeatureCollection>,
    val boundingBox: BoundingBox,
)
