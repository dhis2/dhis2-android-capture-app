@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package org.dhis2.usescases.searchTrackEntity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.maps.model.MapItemModel
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.ui.input.action.FieldUid
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemHelper.toTrackedEntityInstance
import timber.log.Timber

class SearchRepositoryImplKt(
    private val searchRepositoryJava: SearchRepository,
    private val d2: D2,
    private val dispatcher: DispatcherProvider,
    private val fieldViewModelFactory: FieldViewModelFactory,
    private val metadataIconProvider: MetadataIconProvider,
    private val trackedEntityInstanceInfoProvider: TrackedEntityInstanceInfoProvider,
    private val eventInfoProvider: EventInfoProvider,
    private val customIntentRepository: CustomIntentRepository,
    private val uiEventTypesProvider: UiEventTypesProvider,
) : SearchRepositoryKt {
    private lateinit var savedSearchParameters: SearchParametersModel

    private lateinit var savedFilters: FilterManager

    private lateinit var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository

    private val fetchedTeiUids = HashSet<String>()

    override fun saveSearchValuesAndGetAllowCache(
        queryData: MutableMap<String, List<String>?>?,
        programUid: String?,
    ): Boolean {
        if (!this::savedSearchParameters.isInitialized) {
            savedSearchParameters =
                SearchParametersModel(
                    queryData = queryData,
                    selectedProgram = searchRepositoryJava.getProgram(programUid),
                )
        }
        if (!this::savedFilters.isInitialized) {
            savedFilters = FilterManager.getInstance().copy()
        }
        val allowCache =
            queryData == savedSearchParameters.queryData &&
                FilterManager
                    .getInstance()
                    .sameFilters(savedFilters)
        savedSearchParameters = savedSearchParameters.copy(queryData = queryData)
        savedFilters = FilterManager.getInstance().copy()
        return allowCache
    }

    override fun getExcludeValues(): HashSet<String>? =
        fetchedTeiUids.ifEmpty {
            null
        }

    override fun searchTeiForMap(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): List<MapItemModel> {
        var allowCache = false
        if (searchParametersModel != savedSearchParameters || FilterManager.getInstance() != savedFilters) {
            trackedEntityInstanceQuery =
                searchRepositoryJava.getFilteredRepository(searchParametersModel)
        } else {
            allowCache = true
        }

        return if (isOnline && FilterManager.getInstance().stateFilters.isEmpty()) {
            trackedEntityInstanceQuery
                .allowOnlineCache()
                .eq(allowCache)
                .offlineFirst()
                .blockingGet()
                .map { tei ->
                    transformForMap(
                        tei,
                        searchParametersModel.selectedProgram,
                    )
                }
        } else {
            trackedEntityInstanceQuery
                .allowOnlineCache()
                .eq(allowCache)
                .offlineOnly()
                .blockingGet()
                .map { tei ->
                    transformForMap(
                        tei,
                        searchParametersModel.selectedProgram,
                    )
                }
        }
    }

    private fun transformForMap(
        searchItem: TrackedEntitySearchItem,
        selectedProgram: Program?,
    ): MapItemModel {
        fetchedTeiUids.add(searchItem.uid())
        val tei =
            if (searchItem.isOnline) {
                d2
                    .trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(searchItem.uid())
                    .blockingGet()!!
            } else {
                toTrackedEntityInstance(searchItem)
            }

        val attributeValues =
            trackedEntityInstanceInfoProvider.getTeiAdditionalInfoList(
                searchItem.attributeValues ?: emptyList(),
            )

        return MapItemModel(
            uid = searchItem.uid,
            avatarProviderConfiguration =
                trackedEntityInstanceInfoProvider.getAvatar(
                    tei,
                    selectedProgram?.uid(),
                    attributeValues.firstOrNull(),
                ),
            title =
                trackedEntityInstanceInfoProvider.getTeiTitle(
                    searchItem.header,
                    attributeValues,
                ),
            description = null,
            lastUpdated = trackedEntityInstanceInfoProvider.getTeiLastUpdated(searchItem),
            additionalInfoList = attributeValues,
            isOnline =
                d2
                    .trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(searchItem.uid)
                    .blockingGet() == null,
            geometry = searchItem.geometry,
            relatedInfo =
                trackedEntityInstanceInfoProvider.getRelatedInfo(
                    searchItem,
                    selectedProgram,
                ),
            state = searchItem.syncState ?: State.SYNCED,
        )
    }

    override fun trackerValueTypeToSDKValueType(trackerInputType: TrackerInputType): ValueType? =
        when (trackerInputType) {
            TrackerInputType.TEXT -> ValueType.TEXT
            TrackerInputType.LONG_TEXT -> ValueType.LONG_TEXT
            TrackerInputType.LETTER -> ValueType.LETTER
            TrackerInputType.PHONE_NUMBER -> ValueType.PHONE_NUMBER
            TrackerInputType.EMAIL -> ValueType.EMAIL
            TrackerInputType.URL -> ValueType.URL
            TrackerInputType.NUMBER -> ValueType.NUMBER
            TrackerInputType.INTEGER -> ValueType.INTEGER
            TrackerInputType.INTEGER_POSITIVE -> ValueType.INTEGER_POSITIVE
            TrackerInputType.INTEGER_NEGATIVE -> ValueType.INTEGER_NEGATIVE
            TrackerInputType.INTEGER_ZERO_OR_POSITIVE -> ValueType.INTEGER_ZERO_OR_POSITIVE
            TrackerInputType.PERCENTAGE -> ValueType.PERCENTAGE
            TrackerInputType.UNIT_INTERVAL -> ValueType.UNIT_INTERVAL
            TrackerInputType.AGE -> ValueType.AGE
            TrackerInputType.ORGANISATION_UNIT -> ValueType.ORGANISATION_UNIT
            TrackerInputType.DATE_TIME -> ValueType.DATETIME
            TrackerInputType.DATE -> ValueType.DATE
            TrackerInputType.TIME -> ValueType.TIME
            TrackerInputType.HORIZONTAL_CHECKBOXES,
            TrackerInputType.VERTICAL_CHECKBOXES,
            TrackerInputType.HORIZONTAL_RADIOBUTTONS,
            TrackerInputType.VERTICAL_RADIOBUTTONS,
            -> ValueType.BOOLEAN

            TrackerInputType.YES_ONLY_SWITCH,
            TrackerInputType.YES_ONLY_CHECKBOX,
            -> ValueType.TRUE_ONLY

            TrackerInputType.QR_CODE,
            TrackerInputType.BAR_CODE,
            -> ValueType.TEXT

            TrackerInputType.MULTI_SELECTION -> ValueType.MULTI_TEXT
            TrackerInputType.DROPDOWN,
            TrackerInputType.PERIOD_SELECTOR,
            TrackerInputType.MATRIX,
            TrackerInputType.SEQUENTIAL,
            TrackerInputType.NOT_SUPPORTED,
            TrackerInputType.CUSTOM_INTENT,
            -> ValueType.TEXT
        }

    override fun searchRelationshipsForMap(
        teis: List<MapItemModel>,
        selectedProgram: Program?,
    ): List<MapItemModel> =
        buildList {
            teis.forEach { tei ->
                d2
                    .relationshipModule()
                    .relationships()
                    .getByItem(
                        searchItem =
                            RelationshipItem
                                .builder()
                                .trackedEntityInstance(
                                    RelationshipItemTrackedEntityInstance
                                        .builder()
                                        .trackedEntityInstance(tei.uid)
                                        .build(),
                                ).build(),
                        includeDeleted = false,
                        onlyAccessible = false,
                    ).forEach { relationship ->
                        add(
                            trackedEntityInstanceInfoProvider.updateRelationshipInfo(
                                tei,
                                relationship,
                            ),
                        )

                        val relationshipTarget =
                            if (relationship
                                    .to()
                                    ?.trackedEntityInstance()
                                    ?.trackedEntityInstance() == tei.uid
                            ) {
                                relationship.from()
                            } else {
                                relationship.to()
                            }

                        when {
                            relationshipTarget?.trackedEntityInstance() != null &&
                                teis.none { it.uid == relationshipTarget.elementUid() } -> {
                                val trackedEntityType =
                                    d2
                                        .trackedEntityModule()
                                        .trackedEntityInstances()
                                        .uid(relationshipTarget.elementUid())
                                        .blockingGet()
                                        ?.trackedEntityType()
                                val relationshipTei =
                                    d2
                                        .trackedEntityModule()
                                        .trackedEntitySearch()
                                        .byTrackedEntityType()
                                        .eq(trackedEntityType)
                                        .uid(relationshipTarget.elementUid())
                                        .blockingGet()

                                relationshipTei?.let {
                                    add(
                                        trackedEntityInstanceInfoProvider.updateRelationshipInfo(
                                            transformForMap(it, null),
                                            relationship,
                                        ),
                                    )
                                }
                            }

                            relationshipTarget?.event() != null -> {
                                Timber
                                    .tag("MAP RELATIONS BUILDER")
                                    .d("Event need to be added and updated with relationship info")
                            }
                        }
                    }
            }
        }

    override fun validateValue(
        inputType: TrackerInputType,
        value: String,
    ): Any =
        {
            when (inputType) {
                TrackerInputType.DATE -> {
                    ValueType.DATE.validator.validate(value)
                }

                else -> {
                    false
                }
            }
        }

    override fun searchEventForMap(
        teiUids: List<String>,
        selectedProgram: Program?,
    ): List<MapItemModel> =
        d2
            .eventModule()
            .events()
            .byTrackedEntityInstanceUids(teiUids)
            .byProgramUid()
            .eq(selectedProgram?.uid())
            .byStatus()
            .`in`(listOf(EventStatus.ACTIVE, EventStatus.OVERDUE, EventStatus.COMPLETED))
            .byDeleted()
            .isFalse
            .blockingGet()
            .map { event ->
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

    override suspend fun getCustomIntent(fieldUid: FieldUid) =
        withContext(dispatcher.io()) {
            customIntentRepository.getCustomIntent(
                triggerUid = fieldUid,
                orgUnitUid = null,
                actionType = CustomIntentActionTypeModel.SEARCH,
            )
        }
}
