package org.dhis2.usescases.searchTrackEntity

import com.mapbox.geojson.FeatureCollection
import org.dhis2.commons.data.uids
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.mapper.EventToEventUiComponent
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.usescases.searchTrackEntity.adapters.uids
import org.hisp.dhis.android.core.program.Program

class MapDataRepository(
    private val searchRepository: SearchRepository,
    private val mapTeisToFeatureCollection: MapTeisToFeatureCollection,
    private val mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection,
    private val mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
    private val eventToEventUiComponent: EventToEventUiComponent,
    private val mapUtils: DhisMapUtils
) {
    fun getTrackerMapData(
        selectedProgram: Program?,
        queryData: MutableMap<String, String>
    ): TrackerMapData {
        val teis = searchRepository.searchTeiForMap(
            SearchParametersModel(
                selectedProgram,
                selectedProgram?.trackedEntityType()?.uid(),
                queryData
            ),
            true
        ).blockingFirst()
        val events = searchRepository.getEventsForMap(teis)
        val dataElements = mapCoordinateFieldToFeatureCollection.map(
            mapUtils.getCoordinateDataElementInfo(events.uids())
        )
        val attributes = mapCoordinateFieldToFeatureCollection.map(
            mapUtils.getCoordinateAttributeInfo(teis.uids())
        )
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
            teiModels = teis,
            eventFeatures = eventsByProgramStage,
            teiFeatures = teiFeatureCollection.first,
            teiBoundingBox = teiFeatureCollection.second,
            eventModels = eventsUi.toMutableList(),
            dataElementFeaturess = coordinateFields
        )
    }
}
