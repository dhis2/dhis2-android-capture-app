package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import org.dhis2.maps.model.MapItemModel
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.FeatureCollection

data class RelationshipMapData(
    val mapItems: List<MapItemModel>,
    val relationshipFeatures: Map<String, FeatureCollection>,
    val boundingBox: BoundingBox,
)
