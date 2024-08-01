package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.FeatureCollection
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.maps.extensions.filterRelationshipsByLayerVisibility
import org.dhis2.maps.extensions.filterTeiByLayerVisibility
import org.dhis2.maps.extensions.filterTrackerEventsByLayerVisibility
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.MapLayer
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
            mapItems = mapTeis.filterTeiByLayerVisibility(layersVisibility, coordinateAttributes) +
                mapEvents.filterTrackerEventsByLayerVisibility(
                    layersVisibility,
                    coordinateDataElements,
                ) +
                mapRelationships.filterRelationshipsByLayerVisibility(layersVisibility),
            eventFeatures = eventsByProgramStage,
            teiFeatures = teiFeatureCollection.first,
            teiBoundingBox = teiFeatureCollection.second,
            dataElementFeaturess = coordinateFields,
        )
    }
}
