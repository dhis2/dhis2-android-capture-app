package org.dhis2.data.forms.dataentry

import android.text.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.ArrayList
import org.dhis2.Bindings.blockingGetValueCheck
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.LegendValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageSection

class EventRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val eventUid: String,
    private val d2: D2,
    private val resourceManager: ResourceManager
) : DataEntryBaseRepository(d2, fieldFactory) {

    private val event by lazy {
        d2.eventModule().events().uid(eventUid)
            .blockingGet()
    }

    private val sectionMap by lazy {
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(event.programStage())
            .withDataElements()
            .blockingGet()
            .map { section -> section.uid() to section }
            .toMap()
    }

    private val isEventEditable by lazy {
        d2.eventModule().eventService().blockingIsEditable(eventUid)
    }

    override fun sectionUids(): Flowable<MutableList<String>> {
        return Flowable.just(sectionMap.keys.toMutableList())
    }

    override fun list(): Flowable<MutableList<FieldUiModel>> {
        return d2.programModule().programStageSections()
            .byProgramStageUid().eq(event.programStage())
            .withDataElements()
            .get()
            .flatMap { programStageSection ->
                if (programStageSection.isEmpty()) {
                    getFieldsForSingleSection()
                } else {
                    getFieldsForMultipleSections()
                }
            }.map { list ->
                val fields = list.toMutableList()
                fields.add(fieldFactory.createClosingSection())
                fields
            }.toFlowable()
    }

    override fun isEvent(): Boolean {
        return true
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val stageDataElements = d2.programModule().programStageDataElements().withRenderType()
                .byProgramStage().eq(event.programStage())
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()

            stageDataElements.map { programStageDataElement ->
                transform(programStageDataElement)
            }
        }
    }

    private fun getFieldsForMultipleSections(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val fields = mutableListOf<FieldUiModel>()
            sectionMap.values.forEach { programStageSection ->
                fields.add(
                    transformSection(
                        programStageSection.uid(),
                        programStageSection.displayName()
                    )
                )
                programStageSection.dataElements()?.forEach { dataElement ->
                    d2.programModule().programStageDataElements().withRenderType()
                        .byProgramStage().eq(event.programStage())
                        .byDataElement().eq(dataElement.uid())
                        .one().blockingGet()?.let {
                        fields.add(
                            transform(it)
                        )
                    }
                }
            }
            return@fromCallable fields
        }
    }

    private fun transform(programStageDataElement: ProgramStageDataElement): FieldUiModel {
        val de = d2.dataElementModule().dataElements().uid(
            programStageDataElement.dataElement()!!.uid()
        ).blockingGet()
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, de.uid())
        var programStageSection: ProgramStageSection? = null
        for (section in sectionMap.values) {
            if (getUidsList<DataElement>(section.dataElements()!!).contains(de.uid())) {
                programStageSection = section
                break
            }
        }
        val uid = de.uid()
        val displayName = de.displayName()!!
        val valueType = de.valueType()
        val mandatory = programStageDataElement.compulsory() ?: false
        val optionSet = de.optionSetUid()
        var dataValue =
            if (valueRepository.blockingExists()) valueRepository.blockingGet()
                .value() else null
        val rawValue = dataValue
        val friendlyValue =
            if (dataValue != null) valueRepository.blockingGetValueCheck(d2, uid)
                .userFriendlyValue(d2) else null
        val allowFutureDates = programStageDataElement.allowFutureDate() ?: false
        val formName = de.displayFormName()
        val description = de.displayDescription()
        var optionCount = 0
        val options: List<Option>
        if (!TextUtils.isEmpty(optionSet)) {
            if (!TextUtils.isEmpty(dataValue)) {
                if (d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode()
                    .eq(dataValue)
                    .one().blockingExists()
                ) {
                    dataValue =
                        d2.optionModule().options().byOptionSetUid().eq(optionSet)
                            .byCode()
                            .eq(dataValue).one().blockingGet().displayName()
                }
            }
            optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .blockingCount()
            options = d2.optionModule().options().byOptionSetUid().eq(optionSet)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
        } else {
            options = ArrayList()
        }
        val fieldRendering =
            if (programStageDataElement.renderType() != null) programStageDataElement.renderType()!!
                .mobile() else null
        val objectStyle =
            if (de.style() != null) de.style() else ObjectStyle.builder().build()
        val error: String = checkConflicts(de.uid(), dataValue)
        val isOrgUnit =
            valueType === ValueType.ORGANISATION_UNIT
        val isDate = valueType != null && valueType.isDate
        if (!isOrgUnit && !isDate) {
            dataValue = friendlyValue
        }
        val legendValue = getColorByLegend(rawValue, uid)
        val renderingType = if (programStageSection?.renderType() != null &&
            programStageSection.renderType()!!.mobile() != null
        ) {
            programStageSection.renderType()!!.mobile()!!.type()
        } else {
            null
        }

        val fieldViewModel = fieldFactory.create(
            uid,
            formName ?: displayName,
            valueType!!,
            mandatory,
            optionSet,
            dataValue,
            programStageSection?.uid(),
            allowFutureDates,
            isEventEditable,
            renderingType,
            description,
            fieldRendering,
            optionCount,
            objectStyle,
            de.fieldMask(),
            legendValue,
            options,
            FeatureType.POINT
        )
        return if (error.isNotEmpty()) {
            fieldViewModel.setError(error)
        } else {
            fieldViewModel
        }
    }

    private fun checkConflicts(dataElementUid: String, value: String?): String {
        return d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()
            .firstOrNull { conflict ->
                conflict.event() == eventUid &&
                    conflict.dataElement() == dataElementUid &&
                    conflict.value() == value
            }?.displayDescription() ?: ""
    }

    private fun getColorByLegend(value: String?, dataElementUid: String): LegendValue? {
        return if (value == null) {
            null
        } else try {
            val dataElement = d2.dataElementModule().dataElements()
                .byUid().eq(dataElementUid)
                .withLegendSets()
                .one().blockingGet()
            if (dataElement?.valueType()?.isNumeric == true &&
                dataElement.legendSets()?.isNotEmpty() == true
            ) {
                val legendSet = dataElement.legendSets()!![0]
                val legend = d2.legendSetModule().legends()
                    .byStartValue().smallerThan(java.lang.Double.valueOf(value))
                    .byEndValue().biggerThan(java.lang.Double.valueOf(value))
                    .byLegendSet().eq(legendSet.uid())
                    .one()
                    .blockingGet() ?: d2.legendSetModule().legends()
                    .byEndValue().eq(java.lang.Double.valueOf(value))
                    .byLegendSet().eq(legendSet.uid())
                    .one()
                    .blockingGet()
                if (legend != null) {
                    return LegendValue(
                        resourceManager.getColorFrom(legend.color()),
                        legend.displayName()
                    )
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
