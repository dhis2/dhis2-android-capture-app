package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.blockingGetValueCheck
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.LegendValue
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageSection

class EventCaptureFieldProvider(
    private val d2: D2,
    private val fieldFactory: FieldViewModelFactory,
    private val resourceManager: ResourceManager
) {

    fun provideEventFields(
        event: Event,
        programStageSections: List<ProgramStageSection>,
        isEventEditable: Boolean,
        actionProcessor: FlowableProcessor<RowAction>,
        cachedFields: List<FieldViewModel>
    ): Flowable<List<FieldViewModel>> {
        return if (cachedFields.isNotEmpty()) {
            updateEventFields(event, cachedFields, isEventEditable)
        } else {
            provideEventFields(event, programStageSections, isEventEditable, actionProcessor)
        }
    }

    private fun provideEventFields(
        event: Event,
        programStageSections: List<ProgramStageSection>,
        isEventEditable: Boolean,
        actionProcessor: FlowableProcessor<RowAction>
    ): Flowable<List<FieldViewModel>> {
        return Flowable.just(sortedStageDataElements(event.programStage()!!))
            .flatMapIterable { list -> list }
            .map { programStageDataElement ->
                stageDataElementToFieldViewModel(
                    programStageDataElement,
                    event.uid(),
                    programStageSections,
                    isEventEditable,
                    actionProcessor
                )
            }.toList().toFlowable()
    }

    private fun updateEventFields(
        event: Event,
        fields: List<FieldViewModel>,
        isEventEditable: Boolean
    ): Flowable<List<FieldViewModel>> {
        return Flowable.just(fields)
            .flatMapIterable { list -> list }
            .map { fieldViewModel ->

                val de = dataElement(fieldViewModel.uid())

                val (rawValue, friendlyValue) = dataValue(
                    event.uid(),
                    fieldViewModel.uid(),
                    fieldViewModel is OrgUnitViewModel
                )

                val error = checkConflicts(
                    event.uid(),
                    fieldViewModel.uid(),
                    rawValue
                )

                val legend = if (fieldViewModel.canHaveLegend()) {
                    getColorByLegend(rawValue, de)
                } else {
                    null
                }

                val editable = fieldViewModel.editable() ?: true

                val updatedFieldViewModel = fieldViewModel.withValue(friendlyValue)
                    .withEditMode(editable || isEventEditable)
                    .withLegend(legend)
                    .apply {
                        if (error.isNotEmpty()) {
                            withError(error)
                        } else {
                            withError(null)
                        }
                    }

                updatedFieldViewModel
            }.toList().toFlowable()
    }

    private fun sortedStageDataElements(stageUid: String): List<ProgramStageDataElement> {
        val stageDataElements = stageDataElements(stageUid)
        val stageSections = stageSections(stageUid)
        if (stageSections.isNotEmpty()) {
            val dataElementsOrder = arrayListOf<String>()
            stageSections.forEach { section ->
                dataElementsOrder.addAll(getUidsList(section.dataElements()!!))
            }
            stageDataElements.sortWith(
                Comparator { de1: ProgramStageDataElement, de2: ProgramStageDataElement ->
                    val pos1 = dataElementsOrder.indexOf(de1.dataElement()!!.uid())
                    val pos2 = dataElementsOrder.indexOf(de2.dataElement()!!.uid())
                    pos1.compareTo(pos2)
                }
            )
        }
        return stageDataElements
    }

    private fun stageDataElementToFieldViewModel(
        programStageDataElement: ProgramStageDataElement,
        eventUid: String,
        programStageSections: List<ProgramStageSection>,
        isEventEditable: Boolean,
        actionProcessor: FlowableProcessor<RowAction>
    ): FieldViewModel {
        val de = dataElement(programStageDataElement.dataElement()!!.uid())

        val programStageSection: ProgramStageSection? =
            programStageSections.firstOrNull { section ->
                getUidsList(section.dataElements()!!).contains(de.uid())
            }

        val optionSet = de.optionSetUid()

        val (rawValue, friendlyValue) = dataValue(
            eventUid,
            de.uid(),
            de.valueType() == ValueType.ORGANISATION_UNIT
        )

        val options = options(optionSet)

        val error: String = checkConflicts(eventUid, de.uid(), rawValue)

        val fieldViewModel: FieldViewModel =
            fieldFactory.create(
                de.uid(),
                de.formName() ?: de.displayName()!!,
                de.valueType()!!,
                programStageDataElement.compulsory() == true,
                de.optionSetUid(),
                friendlyValue,
                programStageSection?.uid(),
                programStageDataElement.allowFutureDate() == true,
                isEventEditable,
                programStageSection?.renderType()?.mobile()?.type(),
                de.displayDescription(),
                programStageDataElement.renderType()?.mobile(),
                options.size,
                de.style() ?: ObjectStyle.builder().build(),
                de.fieldMask(),
                getColorByLegend(rawValue, de),
                actionProcessor,
                options
            )

        return if (error.isNotEmpty()) {
            fieldViewModel.withError(error)
        } else {
            fieldViewModel
        }
    }

    private fun stageDataElements(stageUid: String) = d2.programModule().programStageDataElements()
        .byProgramStage().eq(stageUid)
        .withRenderType().blockingGet()

    private fun stageSections(stageUid: String) = d2.programModule().programStageSections()
        .byProgramStageUid().eq(stageUid)
        .withDataElements()
        .blockingGet()

    private fun dataElement(dataElementUid: String) = d2.dataElementModule().dataElements()
        .withLegendSets()
        .uid(dataElementUid)
        .blockingGet()

    private fun dataValue(
        eventUid: String,
        dataElementUid: String,
        isValueTypeOrgUnit: Boolean
    ): Pair<String?, String?> {
        val valueRepository = d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, dataElementUid)
        return if (valueRepository.blockingExists()) {
            val value = valueRepository.blockingGet().value()
            var friendlyValue =
                valueRepository.blockingGetValueCheck(d2, dataElementUid).userFriendlyValue(d2)
            if (value != null && isValueTypeOrgUnit) {
                friendlyValue = "%s_ou_%s".format(value, friendlyValue)
            }
            Pair(value, friendlyValue)
        } else {
            Pair(null, null)
        }
    }

    private fun checkConflicts(
        eventUid: String,
        dataElementUid: String,
        value: String?
    ): String {
        val conflicts = d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()

        return conflicts.firstOrNull { conflict ->
            conflict.event() == eventUid &&
                conflict.dataElement() == dataElementUid &&
                conflict.value() == value
        }?.displayDescription() ?: ""
    }

    private fun getColorByLegend(
        value: String?,
        dataElement: DataElement
    ): LegendValue? {
        return if (value == null) {
            null
        } else try {
            if (dataElement.valueType()!!.isNumeric &&
                dataElement.legendSets() != null &&
                dataElement.legendSets()!!.isNotEmpty()
            ) {
                val legendSet = dataElement.legendSets()!![0]
                var legend =
                    d2.legendSetModule().legends()
                        .byStartValue().smallerThan(java.lang.Double.valueOf(value))
                        .byEndValue().biggerThan(java.lang.Double.valueOf(value))
                        .byLegendSet().eq(legendSet.uid())
                        .one()
                        .blockingGet()
                if (legend == null) {
                    legend = d2.legendSetModule().legends()
                        .byEndValue().eq(java.lang.Double.valueOf(value))
                        .byLegendSet().eq(legendSet.uid())
                        .one()
                        .blockingGet()
                }
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

    private fun options(optionSetUid: String?): List<Option> =
        if (optionSetUid?.isNotEmpty() == true) {
            d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
        } else {
            emptyList()
        }
}
