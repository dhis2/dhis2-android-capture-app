package org.dhis2.usescases.searchTrackEntity

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.ui.toColor
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class SearchRepositoryImplKt(
    private val searchRepositoryJava: SearchRepository,
    private val d2: D2,
    private val dispatcher: DispatcherProvider,
    private val fieldViewModelFactory: FieldViewModelFactory,
    private val metadataIconProvider: MetadataIconProvider,
) : SearchRepositoryKt {

    private lateinit var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository

    override fun searchTrackedEntities(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItem>> {
        return trackedEntitySearchQuery(searchParametersModel, isOnline)
            .getPagingData(10)
    }

    private fun trackedEntitySearchQuery(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): TrackedEntitySearchCollectionRepository {
        trackedEntityInstanceQuery =
            searchRepositoryJava.getFilteredRepository(searchParametersModel)

        val allowCache = !(
            searchParametersModel != searchRepositoryJava.savedSearchParameters ||
                !FilterManager.getInstance().sameFilters(searchRepositoryJava.savedFilters)
            )

        if (
            searchRepositoryJava.fetchedTeiUIDs.isNotEmpty() &&
            searchParametersModel.selectedProgram == null
        ) {
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.excludeUids()
                .`in`(searchRepositoryJava.fetchedTeiUIDs.toList())
        }

        return if (isOnline && FilterManager.getInstance().stateFilters.isEmpty()) {
            trackedEntityInstanceQuery.allowOnlineCache().eq(allowCache).offlineFirst()
        } else {
            trackedEntityInstanceQuery.allowOnlineCache().eq(allowCache).offlineOnly()
        }
    }

    override suspend fun searchParameters(
        programUid: String?,
        teiTypeUid: String,
    ): List<FieldUiModel> =
        withContext(dispatcher.io()) {
            programUid?.let {
                programTrackedEntityAttributes(programUid)
            } ?: trackedEntitySearchFields(teiTypeUid)
        }

    override suspend fun searchTrackedEntitiesImmediate(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): List<TrackedEntitySearchItem> {
        return trackedEntitySearchQuery(searchParametersModel, isOnline)
            .blockingGet()
    }

    private fun programTrackedEntityAttributes(programUid: String): List<FieldUiModel> {
        val searchableAttributes = d2.programModule().programTrackedEntityAttributes()
            .withRenderType()
            .byProgram().eq(programUid).orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet().filter { programAttribute ->
                val isSearchable = programAttribute.searchable()!!
                val isUnique = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(programAttribute.trackedEntityAttribute()!!.uid())
                    .blockingGet()?.unique() === java.lang.Boolean.TRUE
                isSearchable || isUnique
            }

        val program = d2.programModule().programs().uid(programUid).blockingGet()

        return searchableAttributes.mapNotNull { programAttribute ->
            d2.trackedEntityModule().trackedEntityAttributes()
                .uid(programAttribute.trackedEntityAttribute()!!.uid())
                .blockingGet()?.let { attribute ->

                    val optionSetConfiguration = attribute.optionSet()?.let {
                        OptionSetConfiguration.config(
                            d2.optionModule().options()
                                .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                                .blockingCount(),
                        ) {
                            val options = d2.optionModule().options()
                                .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet()

                            val metadataIconMap =
                                options.associate {
                                    it.uid() to metadataIconProvider(
                                        it.style(),
                                        program?.style()?.color()?.toColor()
                                            ?: SurfaceColor.Primary,
                                    )
                                }

                            OptionSetConfiguration.OptionConfigData(
                                options = options,
                                metadataIconMap = metadataIconMap,
                            )
                        }
                    }
                    createField(
                        trackedEntityAttribute = attribute,
                        programTrackedEntityAttribute = programAttribute,
                        optionSetConfiguration = optionSetConfiguration,
                    )
                }
        }.filter { parameter ->
            parameter.valueType !== ValueType.IMAGE &&
                parameter.valueType !== ValueType.COORDINATE &&
                parameter.valueType !== ValueType.FILE_RESOURCE
        }
    }

    private fun trackedEntitySearchFields(teiTypeUid: String): List<FieldUiModel> {
        val teTypeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
            .byTrackedEntityTypeUid().eq(teiTypeUid)
            .bySearchable().isTrue
            .blockingGet()

        return teTypeAttributes.mapNotNull { typeAttribute ->
            d2.trackedEntityModule().trackedEntityAttributes()
                .uid(typeAttribute.trackedEntityAttribute()!!.uid())
                .blockingGet()?.let { attribute ->

                    val optionSetConfiguration = attribute.optionSet()?.let {
                        OptionSetConfiguration.config(
                            d2.optionModule().options()
                                .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                                .blockingCount(),
                        ) {
                            val options = d2.optionModule().options()
                                .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet()

                            val metadataIconMap =
                                options.associate {
                                    it.uid() to metadataIconProvider(
                                        it.style(),
                                        SurfaceColor.Primary,
                                    )
                                }

                            OptionSetConfiguration.OptionConfigData(
                                options = options,
                                metadataIconMap = metadataIconMap,
                            )
                        }
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
    ): FieldUiModel {
        return fieldViewModelFactory.create(
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
        )
    }


}
