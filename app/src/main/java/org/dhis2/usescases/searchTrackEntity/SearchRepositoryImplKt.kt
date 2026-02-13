@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package org.dhis2.usescases.searchTrackEntity

import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.maps.model.MapItemModel
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.ui.input.action.FieldUid
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.searchTrackEntity.searchparameters.mapper.getInputTypeByValueType
import org.dhis2.usescases.searchTrackEntity.searchparameters.mapper.getInputTypeForOptionSetByRenderingType
import org.dhis2.usescases.searchTrackEntity.searchparameters.mapper.getOrientation
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.arch.repositories.scope.internal.TrackerSearchOperator
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
                SearchParametersModel(queryData = queryData, selectedProgram = searchRepositoryJava.getProgram(programUid))
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

    override suspend fun searchParameters(
        programUid: String?,
        teiTypeUid: String,
    ): List<TrackerInputModel> =
        withContext(dispatcher.io()) {
            val searchParameters =
                programUid?.let {
                    programTrackedEntityAttributes(programUid)
                } ?: trackedEntitySearchFields(teiTypeUid)

            sortSearchParameters(searchParameters)
        }

    fun sortSearchParameters(parameters: List<TrackerInputModel>): List<TrackerInputModel> =
        parameters.sortedWith(
            compareByDescending<TrackerInputModel> {
                isQrCodeOrBarCode(it.valueType) && isUnique(it.uid)
            }.thenByDescending {
                isQrCodeOrBarCode(it.valueType)
            }.thenByDescending { isUnique(it.uid) },
        )

    fun isQrCodeOrBarCode(renderingType: TrackerInputType?): Boolean =
        renderingType == TrackerInputType.QR_CODE || renderingType == TrackerInputType.BAR_CODE

    private fun isUnique(teaUid: String): Boolean =
        d2
            .trackedEntityModule()
            .trackedEntityAttributes()
            .uid(teaUid)
            .blockingGet()
            ?.unique() ?: false

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
            TrackerInputType.CHECKBOX,
            TrackerInputType.RADIO_BUTTON,
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

    private fun programTrackedEntityAttributes(programUid: String): List<TrackerInputModel> {
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
                        mapToTrackerModel(
                            trackedEntityAttribute = attribute,
                            programTrackedEntityAttribute = programAttribute,
                            optionSetConfiguration = optionSetConfiguration,
                            customIntent = customIntentModel,
                        )
                    }
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

    private fun trackedEntitySearchFields(teiTypeUid: String): List<TrackerInputModel> {
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
                        mapToTrackerModel(
                            trackedEntityAttribute = attribute,
                            programTrackedEntityAttribute = null,
                            optionSetConfiguration = optionSetConfiguration,
                        )
                    }
            }.filter { parameter ->
                parameter.valueType != TrackerInputType.NOT_SUPPORTED
            }
    }

    // TODO fielviewModelFactory should be removed from the module
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

    private fun mapToTrackerModel(
        trackedEntityAttribute: TrackedEntityAttribute,
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute?,
        optionSetConfiguration: OptionSetConfiguration?,
        customIntent: CustomIntentModel? = null,
    ): TrackerInputModel {
        val renderingType =
            uiEventTypesProvider.provideUiRenderType(
                featureType = null,
                valueTypeRenderingType = programTrackedEntityAttribute?.renderType()?.mobile()?.type(),
                sectionRenderingType = SectionRenderingType.LISTING,
            )

        val trackerInputType =
            when {
                trackedEntityAttribute.optionSet()?.uid() != null && trackedEntityAttribute.valueType() != ValueType.MULTI_TEXT -> {
                    getInputTypeForOptionSetByRenderingType(renderingType)
                }

                customIntent != null -> {
                    TrackerInputType.CUSTOM_INTENT
                }

                else -> getInputTypeByValueType(trackedEntityAttribute.valueType(), renderingType)
            }

        // TODO pass optionSetConfiguration

        val searchOperator = getSearchOperator(trackedEntityAttribute)

        return TrackerInputModel(
            uid = trackedEntityAttribute.uid(),
            label = trackedEntityAttribute.displayFormName() ?: "",
            value = null,
            focused = false,
            valueType = trackerInputType,
            optionSet = trackedEntityAttribute.optionSet()?.uid(),
            error = null,
            warning = null,
            description = null,
            mandatory = false,
            editable = true,
            legend = null,
            orientation = renderingType.getOrientation(),
            optionSetConfiguration = null,
            customIntentUid = customIntent?.uid,
            displayName = trackedEntityAttribute.displayFormName() ?: "",
            orgUnitSelectorScope = null,
            searchOperator = searchOperator,
        )
    }

    private fun getSearchOperator(attribute: TrackedEntityAttribute): SearchOperator? {
        val mainOperators = listOf(SearchOperator.LIKE, SearchOperator.SW, SearchOperator.EQ)
        val blockedOperators =
            attribute
                .blockedSearchOperators()
                ?.mapNotNull { sdkOperator ->
                    sdkOperator.toSearchOperator()
                } ?: emptyList()
        val preferredOperator: SearchOperator? = attribute.preferredSearchOperator()?.toSearchOperator()
        val valueType = attribute.valueType()
        val hasOptionSet = attribute.optionSet() != null
        val isUnique = attribute.unique() == true

        val alwaysEqValueTypes =
            listOf(
                ValueType.BOOLEAN,
                ValueType.TRUE_ONLY,
                ValueType.AGE,
                ValueType.ORGANISATION_UNIT,
            )

        val preferredOperatorValueTypes =
            listOf(
                ValueType.NUMBER,
                ValueType.INTEGER,
                ValueType.INTEGER_POSITIVE,
                ValueType.INTEGER_NEGATIVE,
                ValueType.INTEGER_ZERO_OR_POSITIVE,
                ValueType.DATE,
                ValueType.DATETIME,
                ValueType.TIME,
                ValueType.TEXT,
                ValueType.LONG_TEXT,
                ValueType.EMAIL,
                ValueType.PHONE_NUMBER,
                ValueType.PERCENTAGE,
            )

        return when {
            isUnique ||
                (hasOptionSet && valueType != ValueType.MULTI_TEXT) ||
                valueType in alwaysEqValueTypes -> {
                SearchOperator.EQ
            }
            valueType == ValueType.MULTI_TEXT -> {
                mainOperators.firstOrNull { it !in blockedOperators }
            }
            valueType in preferredOperatorValueTypes -> {
                if (preferredOperator != null && preferredOperator !in blockedOperators) {
                    preferredOperator
                } else {
                    mainOperators.firstOrNull { it !in blockedOperators }
                }
            }
            else -> null
        }
    }

    private fun TrackerSearchOperator.toSearchOperator() =
        when (this) {
            TrackerSearchOperator.LIKE -> SearchOperator.LIKE
            TrackerSearchOperator.SW -> SearchOperator.SW
            TrackerSearchOperator.EW -> SearchOperator.EW
            TrackerSearchOperator.EQ -> SearchOperator.EQ
        }
}
