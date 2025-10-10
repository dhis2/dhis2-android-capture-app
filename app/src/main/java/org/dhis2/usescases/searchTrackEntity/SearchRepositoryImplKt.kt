@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package org.dhis2.usescases.searchTrackEntity

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.maps.model.MapItemModel
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemHelper.toTrackedEntityInstance
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
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
) : SearchRepositoryKt {
    private lateinit var savedSearchParameters: SearchParametersModel

    private lateinit var savedFilters: FilterManager

    private lateinit var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository

    private val fetchedTeiUids = HashSet<String>()

    override fun searchTrackedEntities(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItem>> =
        trackedEntitySearchQuery(searchParametersModel, isOnline)
            .getPagingData(10)

    private fun trackedEntitySearchQuery(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): TrackedEntitySearchCollectionRepository {
        var allowCache = false
        savedSearchParameters = searchParametersModel.copy()
        savedFilters = FilterManager.getInstance().copy()

        if (searchParametersModel != savedSearchParameters ||
            !FilterManager
                .getInstance()
                .sameFilters(savedFilters)
        ) {
            trackedEntityInstanceQuery =
                searchRepositoryJava.getFilteredRepository(searchParametersModel)
        } else {
            trackedEntityInstanceQuery =
                searchRepositoryJava.getFilteredRepository(searchParametersModel)
            allowCache = true
        }

        if (fetchedTeiUids.isNotEmpty() && searchParametersModel.selectedProgram == null) {
            trackedEntityInstanceQuery =
                trackedEntityInstanceQuery.excludeUids().`in`(fetchedTeiUids.toList())
        }

        val pagerFlow =
            if (isOnline && FilterManager.getInstance().stateFilters.isEmpty()) {
                trackedEntityInstanceQuery.allowOnlineCache().eq(allowCache).offlineFirst()
            } else {
                trackedEntityInstanceQuery.allowOnlineCache().eq(allowCache).offlineOnly()
            }

        return pagerFlow
    }

    override suspend fun searchParameters(
        programUid: String?,
        teiTypeUid: String,
    ): List<FieldUiModel> =
        withContext(dispatcher.io()) {
            val searchParameters =
                programUid?.let {
                    programTrackedEntityAttributes(programUid)
                } ?: trackedEntitySearchFields(teiTypeUid)

            sortSearchParameters(searchParameters)
        }

    fun sortSearchParameters(parameters: List<FieldUiModel>): List<FieldUiModel> =
        parameters.sortedWith(
            compareByDescending<FieldUiModel> {
                it.renderingType?.isQROrBarcode() == true && isUnique(it.uid)
            }.thenByDescending {
                it.renderingType?.isQROrBarcode() == true
            }.thenByDescending { isUnique(it.uid) },
        )

    private fun isUnique(teaUid: String): Boolean =
        d2
            .trackedEntityModule()
            .trackedEntityAttributes()
            .uid(teaUid)
            .blockingGet()
            ?.unique() ?: false

    override suspend fun searchTrackedEntitiesImmediate(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): List<TrackedEntitySearchItem> =
        trackedEntitySearchQuery(searchParametersModel, isOnline)
            .blockingGet()

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

    private fun programTrackedEntityAttributes(programUid: String): List<FieldUiModel> {
        val searchableAttributes =
            d2
                .programModule()
                .programTrackedEntityAttributes()
                .withRenderType()
                .byProgram()
                .eq(programUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
                .filter { programAttribute ->
                    val isSearchable = programAttribute.searchable()!!
                    val isUnique =
                        d2
                            .trackedEntityModule()
                            .trackedEntityAttributes()
                            .uid(programAttribute.trackedEntityAttribute()!!.uid())
                            .blockingGet()
                            ?.unique() === java.lang.Boolean.TRUE
                    isSearchable || isUnique
                }

        val program =
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()

        return searchableAttributes
            .mapNotNull { programAttribute ->
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(programAttribute.trackedEntityAttribute()!!.uid())
                    .blockingGet()
                    ?.let { attribute ->
                        val searchFlow = MutableStateFlow("")
                        val optionSetConfiguration =
                            attribute.optionSet()?.let {
                                OptionSetConfiguration(
                                    searchEmitter = searchFlow,
                                    optionFlow =
                                        searchFlow.debounce(300).flatMapLatest {
                                            d2
                                                .optionModule()
                                                .options()
                                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                                .byOptionSetUid()
                                                .eq(attribute.optionSet()!!.uid())
                                                .getPagingData(10)
                                                .map { pagingData ->
                                                    pagingData.map { option ->
                                                        OptionSetConfiguration.OptionData(
                                                            option,
                                                            metadataIconProvider(
                                                                option.style(),
                                                                program?.style()?.color()?.toColor()
                                                                    ?: SurfaceColor.Primary,
                                                            ),
                                                        )
                                                    }
                                                }
                                        },
                                    onSearch = { searchFlow.value = it },
                                )
                            }
                        val customIntentModel =
                            customIntentRepository.getCustomIntent(
                                triggerUid = attribute.uid(),
                                orgUnitUid = null,
                                actionType = CustomIntentActionTypeModel.SEARCH,
                            )
                        createField(
                            trackedEntityAttribute = attribute,
                            programTrackedEntityAttribute = programAttribute,
                            optionSetConfiguration = optionSetConfiguration,
                            customIntent = customIntentModel,
                        )
                    }
            }.filter { parameter ->
                parameter.valueType !== ValueType.IMAGE &&
                    parameter.valueType !== ValueType.COORDINATE &&
                    parameter.valueType !== ValueType.FILE_RESOURCE
            }
    }

    private fun trackedEntitySearchFields(teiTypeUid: String): List<FieldUiModel> {
        val teTypeAttributes =
            d2
                .trackedEntityModule()
                .trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid()
                .eq(teiTypeUid)
                .bySearchable()
                .isTrue
                .blockingGet()

        return teTypeAttributes
            .mapNotNull { typeAttribute ->
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(typeAttribute.trackedEntityAttribute()!!.uid())
                    .blockingGet()
                    ?.let { attribute ->
                        val searchEmitter = MutableStateFlow("")
                        val optionSetConfiguration =
                            attribute.optionSet()?.let {
                                OptionSetConfiguration(
                                    searchEmitter = searchEmitter,
                                    optionFlow =
                                        d2
                                            .optionModule()
                                            .options()
                                            .byOptionSetUid()
                                            .eq(attribute.optionSet()!!.uid())
                                            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                            .getPagingData(10)
                                            .map { pagingData ->
                                                pagingData.map { option ->
                                                    OptionSetConfiguration.OptionData(
                                                        option,
                                                        metadataIconProvider(
                                                            option.style(),
                                                            SurfaceColor.Primary,
                                                        ),
                                                    )
                                                }
                                            },
                                    onSearch = { searchEmitter.value = it },
                                )
                            }

                        createField(
                            trackedEntityAttribute = attribute,
                            programTrackedEntityAttribute = null,
                            optionSetConfiguration = optionSetConfiguration,
                        )
                    }
            }.filter { parameter ->
                parameter.valueType !== ValueType.IMAGE &&
                    parameter.valueType !== ValueType.COORDINATE &&
                    parameter.valueType !== ValueType.FILE_RESOURCE
            }
    }

    private fun createField(
        trackedEntityAttribute: TrackedEntityAttribute,
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute?,
        optionSetConfiguration: OptionSetConfiguration?,
        customIntent: CustomIntentModel? = null,
    ): FieldUiModel =
        fieldViewModelFactory.create(
            id = trackedEntityAttribute.uid(),
            label = trackedEntityAttribute.displayFormName() ?: "",
            valueType = trackedEntityAttribute.valueType()!!,
            mandatory = false,
            optionSet = trackedEntityAttribute.optionSet()?.uid(),
            value = null,
            programStageSection = null,
            allowFutureDates = programTrackedEntityAttribute?.allowFutureDate() ?: true,
            editable = true,
            renderingType = SectionRenderingType.LISTING,
            description = null,
            fieldRendering = programTrackedEntityAttribute?.renderType()?.mobile(),
            objectStyle = trackedEntityAttribute.style() ?: ObjectStyle.builder().build(),
            fieldMask = trackedEntityAttribute.fieldMask(),
            optionSetConfiguration = optionSetConfiguration,
            featureType = null,
            customIntentModel = customIntent,
        )
}
