package org.dhis2.maps.extensions

import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.types.EnrollmentMapLayer
import org.dhis2.maps.layer.types.EventMapLayer
import org.dhis2.maps.layer.types.FieldMapLayer
import org.dhis2.maps.layer.types.RelationshipMapLayer
import org.dhis2.maps.layer.types.TeiEventMapLayer
import org.dhis2.maps.layer.types.TeiMapLayer
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.utils.CoordinateAttributeInfo
import org.dhis2.maps.utils.CoordinateDataElementInfo

fun List<MapItemModel>.filterTeiByLayerVisibility(
    layersVisibility: Map<String, MapLayer>,
    coordinateAttributes: List<CoordinateAttributeInfo>,
) = filter { mapItemModel ->
    (teiLayerIsVisible(layersVisibility) and mapItemModel.hasCoordinates()) or
        (enrollmentLayerIsVisible(layersVisibility) and mapItemModel.hasEnrollmentCoordinates()) or
        (
            attributeLayerIsVisible(
                mapItemModel,
                layersVisibility,
                coordinateAttributes,
            ) and mapItemModel.hasAttributeCoordinates(coordinateAttributes)
            )
}

fun List<MapItemModel>.filterTrackerEventsByLayerVisibility(
    layersVisibility: Map<String, MapLayer>,
    coordinateDataElements: List<CoordinateDataElementInfo>,
) = filter { mapItemModel ->
    (trackerEventLayerIsVisible(mapItemModel, layersVisibility) and mapItemModel.hasCoordinates()) or
        (
            dataElementLayerIsVisible(
                mapItemModel,
                layersVisibility,
                coordinateDataElements,
            ) and
                mapItemModel.hasDataElementCoordinates(coordinateDataElements)
            )
}

fun List<MapItemModel>.filterEventsByLayerVisibility(
    layersVisibility: Map<String, MapLayer>,
    coordinateDataElements: List<CoordinateDataElementInfo>,
) = filter { mapItemModel ->
    (eventLayerIsVisible(layersVisibility) and mapItemModel.hasCoordinates()) or
        (
            dataElementLayerIsVisible(
                mapItemModel,
                layersVisibility,
                coordinateDataElements,
            ) and
                mapItemModel.hasDataElementCoordinates(coordinateDataElements)
            )
}
fun List<MapItemModel>.filterRelationshipsByLayerVisibility(layersVisibility: Map<String, MapLayer>) =
    filter { mapItemModel ->
        relationshipLayerIsVisible(mapItemModel, layersVisibility) and mapItemModel.hasCoordinates()
    }

private fun MapItemModel.hasCoordinates(): Boolean {
    return geometry != null
}

private fun MapItemModel.hasEnrollmentCoordinates(): Boolean {
    return relatedInfo?.enrollment?.geometry != null
}

private fun MapItemModel.hasAttributeCoordinates(
    coordinateAttributes: List<CoordinateAttributeInfo>,
): Boolean {
    return coordinateAttributes.any { it.tei.uid() == uid }
}

private fun MapItemModel.hasDataElementCoordinates(
    coordinateDataElements: List<CoordinateDataElementInfo>,
): Boolean {
    return coordinateDataElements.any {
        it.enrollment?.uid() == relatedInfo?.enrollment?.uid
    }
}

private fun teiLayerIsVisible(layersVisibility: Map<String, MapLayer>): Boolean {
    return layersVisibility.values.find { it is TeiMapLayer }?.visible == true
}

private fun enrollmentLayerIsVisible(layersVisibility: Map<String, MapLayer>): Boolean {
    return layersVisibility.values.find { it is EnrollmentMapLayer }?.visible == true
}

private fun attributeLayerIsVisible(
    mapTeiModel: MapItemModel,
    layersVisibility: Map<String, MapLayer>,
    coordinateAttributes: List<CoordinateAttributeInfo>,
): Boolean {
    return layersVisibility.entries.any { (_, mapLayer) ->
        (mapLayer is FieldMapLayer) and
            mapLayer.visible and
            (coordinateAttributes.find { it.tei.uid() == mapTeiModel.uid } != null)
    }
}

private fun dataElementLayerIsVisible(
    mapTeiModel: MapItemModel,
    layersVisibility: Map<String, MapLayer>,
    coordinateDataElements: List<CoordinateDataElementInfo>,
): Boolean {
    return layersVisibility.entries.any { (_, mapLayer) ->
        (mapLayer is FieldMapLayer) and
            mapLayer.visible and
            (coordinateDataElements.find { it.event.uid() == mapTeiModel.uid } != null)
    }
}

private fun eventLayerIsVisible(
    layersVisibility: Map<String, MapLayer>,
): Boolean {
    return layersVisibility.entries.filter {
        it.value is EventMapLayer
    }.find { (_, mapLayer) ->
        mapLayer.visible
    } != null
}

private fun trackerEventLayerIsVisible(
    mapItemModel: MapItemModel,
    layersVisibility: Map<String, MapLayer>,
): Boolean {
    return layersVisibility.entries.filter {
        it.value is TeiEventMapLayer
    }.find { (sourceId, mapLayer) ->
        mapLayer.visible and (mapItemModel.relatedInfo?.event?.stageDisplayName == sourceId)
    } != null
}

private fun relationshipLayerIsVisible(
    mapItemModel: MapItemModel,
    layersVisibility: Map<String, MapLayer>,
): Boolean {
    return layersVisibility.entries.filter { it.value is RelationshipMapLayer }
        .find { (sourceId, mapLayer) ->
            mapLayer.visible and (mapItemModel.relatedInfo?.relationship?.displayName == sourceId)
        } != null
}
