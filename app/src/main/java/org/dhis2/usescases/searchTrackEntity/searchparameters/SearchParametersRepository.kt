package org.dhis2.usescases.searchTrackEntity.searchparameters

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType

class SearchParametersRepository(
    private val d2: D2,
    private val dispatcher: DispatcherProvider,
    private val fieldViewModelFactory: FieldViewModelFactory,
) {

    suspend fun searchParameters(programUid: String?, teiTypeUid: String): List<FieldUiModel> =
        withContext(dispatcher.io()) {
            programUid?.let {
                programTrackedEntityAttributes(programUid)
            } ?: trackedEntitySearchFields(teiTypeUid)
        }

    private fun programTrackedEntityAttributes(programUid: String): List<FieldUiModel> {
        val searchableAttributes = d2.programModule().programTrackedEntityAttributes()
            .withRenderType()
            .byProgram().eq(programUid)
            .blockingGet().filter { programAttribute ->
                val isSearchable = programAttribute.searchable()!!
                val isUnique = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(programAttribute.trackedEntityAttribute()!!.uid())
                    .blockingGet()?.unique() === java.lang.Boolean.TRUE
                isSearchable || isUnique
            }

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
                            d2.optionModule().options()
                                .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet()
                        }
                    }
                    fieldViewModelFactory.createForAttribute(
                        trackedEntityAttribute = attribute,
                        programTrackedEntityAttribute = programAttribute,
                        value = null,
                        editable = true,
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
                            d2.optionModule().options()
                                .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet()
                        }
                    }

                    fieldViewModelFactory.createForAttribute(
                        trackedEntityAttribute = attribute,
                        programTrackedEntityAttribute = null,
                        value = null,
                        editable = true,
                        optionSetConfiguration = optionSetConfiguration,
                    )
                }
        }.filter { parameter ->
            parameter.valueType !== ValueType.IMAGE &&
                parameter.valueType !== ValueType.COORDINATE &&
                parameter.valueType !== ValueType.FILE_RESOURCE
        }
    }
}
