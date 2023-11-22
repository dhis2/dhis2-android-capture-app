package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.FeatureCollection
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.data.uids
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.mapper.EventToEventUiComponent
import org.dhis2.maps.utils.CoordinateAttributeInfo
import org.dhis2.maps.utils.CoordinateDataElementInfo
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.usescases.searchTrackEntity.adapters.uids
import org.hisp.dhis.android.core.program.Program

class MapDataRepository(
    private val searchRepository: SearchRepository,
    private val mapTeisToFeatureCollection: MapTeisToFeatureCollection,
    private val mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection,
    private val mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
    private val eventToEventUiComponent: EventToEventUiComponent,
    private val mapUtils: DhisMapUtils,
) {
    fun getTrackerMapData(
        selectedProgram: Program?,
        queryData: MutableMap<String, String>,
    ): TrackerMapData {
        val teis = searchRepository.searchTeiForMap(
            SearchParametersModel(
                selectedProgram,
                queryData,
            ),
            true,
        ).blockingFirst()
        val events = searchRepository.getEventsForMap(teis)

        val coordinateDataElements = mapUtils.getCoordinateDataElementInfo(events.uids())
        val dataElements = mapCoordinateFieldToFeatureCollection.map(coordinateDataElements)
        val coordinateAttributes = mapUtils.getCoordinateAttributeInfo(teis.uids())
        val attributes = mapCoordinateFieldToFeatureCollection.map(coordinateAttributes)
        val coordinateFields = mutableMapOf<String, FeatureCollection>().apply {
            putAll(dataElements)
            putAll(attributes)
        }
        val eventsUi = eventToEventUiComponent.mapList(events, teis)
        val teiFeatureCollection =
            mapTeisToFeatureCollection.map(teis, selectedProgram != null)
        val eventsByProgramStage =
            mapTeiEventsToFeatureCollection.map(eventsUi).component1()
        return TrackerMapData(
            teiModels = teis.filter {
                hasCoordinates(it) or
                    hasEnrollmentCoordinates(it) or
                    hasAttributeCoordinates(it, coordinateAttributes) or
                    hasDataElementCoordinates(it, coordinateDataElements)
            }.toMutableList(),
            eventFeatures = eventsByProgramStage,
            teiFeatures = teiFeatureCollection.first,
            teiBoundingBox = teiFeatureCollection.second,
            eventModels = eventsUi.filter { it.event.geometry() != null }.toMutableList(),
            dataElementFeaturess = coordinateFields,
        )
    }

    private fun hasCoordinates(searchTeiModel: SearchTeiModel): Boolean {
        return searchTeiModel.tei.geometry() != null
    }

    private fun hasEnrollmentCoordinates(searchTeiModel: SearchTeiModel): Boolean {
        return searchTeiModel.selectedEnrollment?.geometry() != null
    }

    private fun hasAttributeCoordinates(
        searchTeiModel: SearchTeiModel,
        coordinateAttributes: List<CoordinateAttributeInfo>,
    ): Boolean {
        return coordinateAttributes.find { it.tei.uid() == searchTeiModel.uid() } != null
    }

    private fun hasDataElementCoordinates(
        searchTeiModel: SearchTeiModel,
        coordinateDataElements: List<CoordinateDataElementInfo>,
    ): Boolean {
        return coordinateDataElements.find {
            it.enrollment?.uid() == searchTeiModel.selectedEnrollment.uid()
        } != null
    }
}
