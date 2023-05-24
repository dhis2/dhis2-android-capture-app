package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.blockingGetValueCheck
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.LegendValue
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
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
        cachedFields: List<FieldUiModel>
    ): Flowable<List<FieldUiModel>> {
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
    ): Flowable<List<FieldUiModel>> {
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
        fields: List<FieldUiModel>,
        isEventEditable: Boolean
    ): Flowable<List<FieldUiModel>> {
        return Flowable.just(fields)
            .flatMapIterable { list -> list }
            .map { fieldViewModel ->

                val de = dataElement(fieldViewModel.uid)

                val (rawValue, friendlyValue) = dataValue(
                    event.uid(),
                    fieldViewModel.uid,
                    fieldViewModel.valueType == ValueType.ORGANISATION_UNIT
                )

                val error = checkConflicts(
                    event.uid(),
                    fieldViewModel.uid,
                    rawValue
                )

                val legend = if (fieldViewModel.legend != null) {
                    getColorByLegend(rawValue, de)
                } else {
                    null
                }

                val updatedFieldViewModel = fieldViewModel.setValue(friendlyValue)
                    .setEditable(fieldViewModel.editable || isEventEditable)
                    .setLegend(legend)
                    .apply {
                        if (error.isNotEmpty()) {
                            setError(error)
                        } else {
                            setError(null)
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
    ): FieldUiModel {
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

        val optionSetConfiguration = options(optionSet)

        val error: String = checkConflicts(eventUid, de.uid(), rawValue)

        val fieldViewModel: FieldUiModel =
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
                de.style() ?: ObjectStyle.builder().build(),
                de.fieldMask(),
                optionSetConfiguration,
                FeatureType.POINT
            )

        return if (error.isNotEmpty()) {
            fieldViewModel.setError(error)
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

    private fun checkConflicts(eventUid: String, dataElementUid: String, value: String?): String {
        val conflicts = d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()

        return conflicts.firstOrNull { conflict ->
            conflict.event() == eventUid &&
                conflict.dataElement() == dataElementUid &&
                conflict.value() == value
        }?.displayDescription() ?: ""
    }

    private fun getColorByLegend(value: String?, dataElement: DataElement): LegendValue? {
        return if (value == null) {
            null
        } else {
            try {
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
    }

    private fun options(optionSetUid: String?): OptionSetConfiguration? = optionSetUid?.let {
        OptionSetConfiguration.config(
            d2.optionModule().options().byOptionSetUid().eq(it).blockingCount()
        ) {
            d2.optionModule().options().byOptionSetUid().eq(it)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
        }
    }
}
