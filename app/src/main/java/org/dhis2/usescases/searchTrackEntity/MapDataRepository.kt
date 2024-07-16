package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.FeatureCollection
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.types.EnrollmentMapLayer
import org.dhis2.maps.layer.types.FieldMapLayer
import org.dhis2.maps.layer.types.RelationshipMapLayer
import org.dhis2.maps.layer.types.TeiEventMapLayer
import org.dhis2.maps.layer.types.TeiMapLayer
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.utils.CoordinateAttributeInfo
import org.dhis2.maps.utils.CoordinateDataElementInfo
import org.dhis2.maps.utils.DhisMapUtils
import org.hisp.dhis.android.core.program.Program

class MapDataRepository(
    private val searchRepositoryKt: SearchRepositoryKt,
    private val mapTeisToFeatureCollection: MapTeisToFeatureCollection,
    private val mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection,
    private val mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
    private val mapUtils: DhisMapUtils,
) {

    fun getTrackerMapData(
        selectedProgram: Program?,
        queryData: MutableMap<String, String>,
        layersVisibility: Map<String, MapLayer> = emptyMap(),
    ): TrackerMapData {
        val mapTeis = searchRepositoryKt.searchTeiForMap(
            SearchParametersModel(
                selectedProgram,
                queryData,
            ),
            true,
        )

        val mapEvents =
            searchRepositoryKt.searchEventForMap(mapTeis.map { it.uid }, selectedProgram)

        val mapRelationships =
            searchRepositoryKt.searchRelationshipsForMap(mapTeis, selectedProgram)

        val coordinateDataElements = mapUtils.getCoordinateDataElementInfo(mapEvents.map { it.uid })

        val dataElements = mapCoordinateFieldToFeatureCollection.map(coordinateDataElements)
        val coordinateAttributes = mapUtils.getCoordinateAttributeInfo(mapTeis.map { it.uid })
        val attributes = mapCoordinateFieldToFeatureCollection.map(coordinateAttributes)
        val coordinateFields = mutableMapOf<String, FeatureCollection>().apply {
            putAll(dataElements)
            putAll(attributes)
        }
        val teiFeatureCollection =
            mapTeisToFeatureCollection.map(mapTeis, selectedProgram != null, mapRelationships)
        val eventsByProgramStage =
            mapTeiEventsToFeatureCollection.map(mapEvents).component1()

        return TrackerMapData(
            mapItems = mapTeis.filter {
                (teiLayerIsVisible(layersVisibility) and hasCoordinates(it)) or
                    (enrollmentLayerIsVisible(layersVisibility) and hasEnrollmentCoordinates(it)) or
                    (
                        attributeLayerIsVisible(
                            it,
                            layersVisibility,
                            coordinateAttributes,
                        ) and hasAttributeCoordinates(
                            it,
                            coordinateAttributes,
                        )
                        )
            } + mapEvents.filter {
                (eventLayerIsVisible(it, layersVisibility) and hasCoordinates(it)) or
                    (
                        dataElementLayerIsVisible(
                            it,
                            layersVisibility,
                            coordinateDataElements,
                        ) and hasDataElementCoordinates(it, coordinateDataElements)
                        )
            } + mapRelationships.filter {
                relationshipLayerIsVisible(it, layersVisibility) and hasCoordinates(it)
            },
            eventFeatures = eventsByProgramStage,
            teiFeatures = teiFeatureCollection.first,
            teiBoundingBox = teiFeatureCollection.second,
            dataElementFeaturess = coordinateFields,
        )
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
        mapItemModel: MapItemModel,
        layersVisibility: Map<String, MapLayer>,
    ): Boolean {
        return layersVisibility.entries.filter { it.value is TeiEventMapLayer }
            .find { (sourceId, mapLayer) ->
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

    private fun hasCoordinates(mapTeiModel: MapItemModel): Boolean {
        return mapTeiModel.geometry != null
    }

    private fun hasEnrollmentCoordinates(mapTeiModel: MapItemModel): Boolean {
        return mapTeiModel.relatedInfo?.enrollment?.geometry != null
    }

    private fun hasAttributeCoordinates(
        mapTeiModel: MapItemModel,
        coordinateAttributes: List<CoordinateAttributeInfo>,
    ): Boolean {
        return coordinateAttributes.find { it.tei.uid() == mapTeiModel.uid } != null
    }

    private fun hasDataElementCoordinates(
        mapTeiModel: MapItemModel,
        coordinateDataElements: List<CoordinateDataElementInfo>,
    ): Boolean {
        return coordinateDataElements.find {
            it.enrollment?.uid() == mapTeiModel.relatedInfo?.enrollment?.uid
        } != null
    }
}
