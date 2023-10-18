package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType

class SearchRepository(
    private val d2: D2,
    private val fieldViewModelFactory: FieldViewModelFactory,
    override val programUid: String?,
    private val teiTypeUid: String,
    private val currentSearchValues: Map<String, String>,
) : DataEntryBaseRepository(d2, fieldViewModelFactory) {

    override fun list(): Flowable<List<FieldUiModel>> {
        return programUid?.let {
            programTrackedEntityAttributes()
        } ?: trackedEntitySearchFields()
    }

    override fun sectionUids(): Flowable<List<String>> {
        return Flowable.just(mutableListOf())
    }

    override fun isEvent(): Boolean {
        return false
    }

    private fun trackedEntitySearchFields(): Flowable<List<FieldUiModel>> {
        return Flowable.fromCallable {
            val teTypeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq(teiTypeUid)
                .bySearchable().isTrue
                .blockingGet()

            return@fromCallable teTypeAttributes.mapNotNull { typeAttribute ->
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
                            attribute,
                            null,
                            currentSearchValues[attribute.uid()],
                            true,
                            optionSetConfiguration,
                        )
                    }
            }.filter { item: FieldUiModel ->
                item.valueType !== ValueType.IMAGE &&
                    item.valueType !== ValueType.COORDINATE
            }
        }
    }

    private fun programTrackedEntityAttributes(): Flowable<List<FieldUiModel>> {
        return Flowable.fromCallable {
            val searchableAttributes = d2.programModule().programTrackedEntityAttributes()
                .withRenderType()
                .byProgram().eq(programUid)
                .blockingGet().filter { programAttribute ->
                    val isSearcheable = programAttribute.searchable()!!
                    val isUnique = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(programAttribute.trackedEntityAttribute()!!.uid())
                        .blockingGet()?.unique() === java.lang.Boolean.TRUE
                    isSearcheable || isUnique
                }
            searchableAttributes.mapNotNull { programAttribute ->
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
                            attribute,
                            programAttribute,
                            currentSearchValues[attribute.uid()],
                            true,
                            optionSetConfiguration,
                        )
                    }
            }.filter { item: FieldUiModel? ->
                item!!.valueType !== ValueType.IMAGE &&
                    item!!.valueType !== ValueType.COORDINATE
            }
        }
    }
}
