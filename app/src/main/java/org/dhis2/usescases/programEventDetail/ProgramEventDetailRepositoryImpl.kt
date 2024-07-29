package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mapbox.geojson.FeatureCollection
import dhis2.org.analytics.charts.Charts
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.maps.extensions.filterEventsByLayerVisibility
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.managers.EventMapManager
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.usescases.events.EventInfoProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventFilter
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

class ProgramEventDetailRepositoryImpl internal constructor(
    private val programUid: String,
    private val d2: D2,
    private val mapper: ProgramEventMapper,
    private val mapEventToFeatureCollection: MapEventToFeatureCollection,
    private val mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
    private val mapUtils: DhisMapUtils,
    private val filterPresenter: FilterPresenter,
    private val charts: Charts?,
    private val eventInfoProvider: EventInfoProvider,
) : ProgramEventDetailRepository {

    private val programRepository = d2.programModule().programs().uid(programUid)
    private val stageRepository = d2.programModule().programStages().byProgramUid().eq(programUid)
    private val filterRepository = programRepository.blockingGet()?.let {
        filterPresenter.filteredEventProgram(it)
    }

    override fun filteredProgramEvents(): LiveData<PagedList<EventViewModel>> {
        val program = program().blockingGet() ?: throw NullPointerException()
        val dataSource = filterPresenter
            .filteredEventProgram(program)
            .dataSource
            .map { event ->
                mapper.eventToEventViewModel(event)
            }
        return LivePagedListBuilder(
            object : DataSource.Factory<Event, EventViewModel>() {
                override fun create(): DataSource<Event, EventViewModel> {
                    return dataSource
                }
            },
            20,
        ).build()
    }

    override fun filteredEventsForMap(layersVisibility: Map<String, MapLayer>): Flowable<ProgramEventMapData> {
        return filterRepository?.get()
            ?.map { listEvents ->
                val (first, second) = mapEventToFeatureCollection.map(listEvents)
                val programEventFeatures = HashMap<String, FeatureCollection>()
                programEventFeatures[EventMapManager.EVENTS] = first
                val coordinateDataElements =
                    mapUtils.getCoordinateDataElementInfo(getUidsList(listEvents))
                val deFeatureCollection =
                    mapCoordinateFieldToFeatureCollection.map(coordinateDataElements)
                programEventFeatures.putAll(deFeatureCollection)

                val mapEvents = listEvents.map { event ->
                    with(eventInfoProvider) {
                        MapItemModel(
                            uid = event.uid(),
                            avatarProviderConfiguration = getAvatar(event),
                            title = getEventTitle(event),
                            description = getEventDescription(event),
                            lastUpdated = getEventLastUpdated(event),
                            additionalInfoList = getAdditionInfoList(event),
                            isOnline = false,
                            geometry = event.geometry(),
                            relatedInfo = getRelatedInfo(event),
                            state = event.aggregatedSyncState() ?: State.SYNCED,
                        )
                    }
                }

                ProgramEventMapData(
                    mapEvents.filterEventsByLayerVisibility(
                        layersVisibility,
                        coordinateDataElements,
                    ),
                    programEventFeatures,
                    second,
                )
            }
            ?.toFlowable() ?: Flowable.empty()
    }

    override fun getInfoForEvent(eventUid: String): Flowable<ProgramEventViewModel> {
        return d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).get()
            .map { event ->
                mapper.eventToProgramEvent(event)
            }
            .toFlowable()
    }

    override fun featureType(): Single<FeatureType> {
        return d2.programModule().programStages()
            .byProgramUid().eq(programUid).one().get()
            .map { stage ->
                if (stage.featureType() != null) {
                    return@map stage.featureType()
                } else {
                    return@map FeatureType.NONE
                }
            }
    }

    override fun getCatOptCombo(selectedCatOptionCombo: String): CategoryOptionCombo? {
        return d2.categoryModule().categoryOptionCombos().uid(selectedCatOptionCombo).blockingGet()
    }

    override fun program(): Single<Program?> {
        return programRepository.get()
    }

    override fun getAccessDataWrite(): Boolean {
        var canWrite = programRepository.blockingGet()?.access()?.data()?.write() == true

        if (canWrite && stageRepository.one().blockingGet() != null) {
            canWrite = stageRepository.one().blockingGet()
                ?.access()?.data()?.write() == true
        } else if (stageRepository.one().blockingGet() == null) {
            canWrite = false
        }
        return canWrite
    }

    override fun workingLists(): Single<List<EventFilter>> {
        return d2.eventModule().eventFilters()
            .withEventDataFilters()
            .byProgram().eq(programUid)
            .get()
    }

    override fun programStage(): Single<ProgramStage?> {
        return stageRepository.one().get()
    }

    override fun programHasCoordinates(): Boolean {
        val programStageHasCoordinates = programStage()
            .map { stage ->
                stage.featureType() != null && stage.featureType() != FeatureType.NONE
            }
            .blockingGet()
        val eventDataElementHasCoordinates = filterRepository?.get()
            ?.map { events ->
                events.any { event -> event.geometry() != null }
            }?.blockingGet() == true
        return programStageHasCoordinates || eventDataElementHasCoordinates
    }

    override fun programHasAnalytics(): Boolean {
        return charts?.getVisualizationGroups(programUid)?.isNotEmpty() == true
    }

    override fun isEventEditable(eventUid: String): Boolean {
        return d2.eventModule().eventService().blockingIsEditable(eventUid)
    }

    override fun displayOrganisationUnit(programUid: String): Boolean {
        return d2.organisationUnitModule().organisationUnits()
            .byProgramUids(listOf(programUid))
            .blockingGet().size > 1
    }
}
