package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import java.util.ArrayList
import java.util.HashMap
import org.dhis2.data.forms.FormSectionViewModel
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.utils.DhisTextUtils.Companion.isEmpty

const val DISPLAY_FIELD_KEY = "DISPLAY_FIELD_KEY"

class EventFieldMapper(
    private val fieldFactory: FieldViewModelFactory,
    private val mandatoryFieldWarning: String
) {

    var totalFields: Int = 0
    var unsupportedFields: Int = 0
    private lateinit var visualDataElements: MutableList<String?>
    private lateinit var fieldMap: MutableMap<String?, MutableList<FieldUiModel>>
    private lateinit var eventSectionModels: MutableList<EventSectionModel>
    private lateinit var finalFieldList: MutableList<FieldUiModel>
    private lateinit var finalFields: MutableMap<String, Boolean>

    fun map(
        fields: MutableList<FieldUiModel>,
        sectionList: MutableList<FormSectionViewModel>,
        currentSection: String,
        errors: MutableMap<String, String>,
        warnings: MutableMap<String, String>,
        emptyMandatoryFields: MutableMap<String, FieldUiModel>,
        showErrors: Pair<Boolean, Boolean>
    ): Pair<MutableList<EventSectionModel>, MutableList<FieldUiModel>> {
        clearAll()
        setFieldMap(fields, sectionList, showErrors.first, emptyMandatoryFields)
        sectionList.forEach {
            handleSection(fields, sectionList, it, currentSection)
        }

        if (eventSectionModels.first().sectionName() == "NO_SECTION") {
            finalFieldList.add(SectionViewModel.createClosingSection())
        }
        if (fieldMap.containsKey(DISPLAY_FIELD_KEY) && fieldMap[DISPLAY_FIELD_KEY] != null) {
            finalFieldList.addAll(fieldMap[DISPLAY_FIELD_KEY] as Collection<FieldUiModel>)
        }

        val sections = finalFieldList.filterIsInstance<SectionViewModel>()

        sections.takeIf { showErrors.first || showErrors.second }?.forEach { section ->
            var errorCounter = 0
            var mandatoryCounter = 0
            if (showErrors.first) {
                repeat(
                    warnings.filter { warning ->
                        fields.firstOrNull { field ->
                            field.uid == warning.key && field.programStageSection == section.uid()
                        } != null
                    }.size +
                        emptyMandatoryFields
                            .filter { it.value.programStageSection == section.uid() }.size
                ) { mandatoryCounter++ }
            }
            if (showErrors.second) {
                repeat(
                    errors.filter { error ->
                        fields.firstOrNull { field ->
                            field.uid == error.key && field.programStageSection == section.uid()
                        } != null
                    }.size
                ) { errorCounter++ }
            }
            finalFieldList[finalFieldList.indexOf(section)] =
                section.withErrorsAndWarnings(
                    if (errorCounter != 0) {
                        errorCounter
                    } else {
                        null
                    },
                    if (mandatoryCounter != 0) {
                        mandatoryCounter
                    } else {
                        null
                    }
                )
        }

        return Pair(eventSectionModels, finalFieldList)
    }

    private fun clearAll() {
        totalFields = 0
        unsupportedFields = 0
        visualDataElements = mutableListOf()
        fieldMap = HashMap()
        eventSectionModels = mutableListOf()
        finalFieldList = mutableListOf()
        finalFields = HashMap()
    }

    private fun setFieldMap(
        fields: List<FieldUiModel>,
        sectionList: List<FormSectionViewModel>,
        showMandatoryErrors: Boolean,
        emptyMandatoryFields: MutableMap<String, FieldUiModel>
    ) {
        fields.forEach { field ->
            val fieldSection = getFieldSection(field)
            if (fieldSection.isNotEmpty() || sectionList.size == 1) {
                updateFieldMap(
                    fieldSection,
                    if (showMandatoryErrors &&
                        emptyMandatoryFields.containsKey(field.uid)
                    ) {
                        field.setWarning(mandatoryFieldWarning)
                    } else {
                        field
                    }
                )
                if (field !is DisplayViewModel) {
                    if (fieldIsNotVisualOptionSet(field)) {
                        totalFields++
                    } else if (!visualDataElements.contains(field.uid)) {
                        visualDataElements.add(field.uid)
                        totalFields++
                    }
                }
                if (field is UnsupportedViewModel) totalFields--
            }
        }
    }

    private fun getFieldSection(field: FieldUiModel): String {
        return if (field is DisplayViewModel) {
            DISPLAY_FIELD_KEY
        } else {
            return field.programStageSection ?: ""
        }
    }

    private fun updateFieldMap(fieldSection: String, field: FieldUiModel) {
        if (!fieldMap.containsKey(fieldSection)) {
            fieldMap[fieldSection] = ArrayList()
        }
        fieldMap[fieldSection]!!.add(field)
    }

    private fun handleSection(
        fields: List<FieldUiModel>,
        sectionList: List<FormSectionViewModel>,
        sectionModel: FormSectionViewModel,
        section: String
    ) {
        if (isValidMultiSection(sectionList, sectionModel)) {
            handleMultiSection(sectionModel, section)
        } else if (isValidSingleSection(sectionList, sectionModel)) {
            handleSingleSection(fields, sectionModel)
        }
    }

    private fun isValidMultiSection(
        sectionList: List<FormSectionViewModel>,
        sectionModel: FormSectionViewModel
    ): Boolean {
        return sectionList.isNotEmpty() && sectionModel.sectionUid()!!.isNotEmpty()
    }

    private fun isValidSingleSection(
        sectionList: List<FormSectionViewModel>,
        sectionModel: FormSectionViewModel
    ): Boolean {
        return sectionList.size == 1 && sectionModel.sectionUid()?.isEmpty() == true
    }

    private fun handleMultiSection(
        sectionModel: FormSectionViewModel,
        section: String
    ) {
        val fieldViewModels = mutableListOf<FieldUiModel>()
        if (fieldMap[sectionModel.sectionUid()] != null) {
            fieldViewModels.addAll(
                fieldMap[sectionModel.sectionUid()] as Collection<FieldUiModel>
            )
        }

        finalFields = HashMap()
        for (fieldViewModel in fieldViewModels) {
            finalFields[fieldViewModel.uid] =
                !isEmpty(fieldViewModel.value)
        }

        var cont = 0
        for (key in finalFields.keys) if (finalFields[key] == true) cont++
        eventSectionModels.add(
            EventSectionModel.create(
                sectionModel.label()!!,
                sectionModel.sectionUid()!!,
                cont,
                finalFields.keys.size
            )
        )
        val isOpen = sectionModel.sectionUid() == section
        if (fieldMap[sectionModel.sectionUid()]?.isNotEmpty() == true) {
            finalFieldList.add(
                fieldFactory.createSection(
                    sectionModel.sectionUid(),
                    sectionModel.label(),
                    "",
                    isOpen,
                    finalFields.keys.size,
                    cont,
                    sectionModel.renderType()
                )
            )
        }
        if (isOpen && fieldMap[sectionModel.sectionUid()] != null) {
            finalFieldList.addAll(fieldMap[sectionModel.sectionUid()] as Collection<FieldUiModel>)
        }
    }

    private fun handleSingleSection(
        fields: List<FieldUiModel>,
        sectionModel: FormSectionViewModel
    ) {
        for (fieldViewModel in fields) {
            if (fieldViewModel !is DisplayViewModel) {
                finalFields[fieldViewModel.uid] =
                    !isEmpty(fieldViewModel.value)
            }
        }

        var cont = 0
        for (key in finalFields.keys) if (finalFields[key] == true) cont++
        eventSectionModels.add(
            EventSectionModel.create(
                "NO_SECTION",
                "no_section",
                cont,
                finalFields.keys.size
            )
        )
        finalFieldList.addAll(fieldMap[sectionModel.sectionUid()] as Collection<FieldUiModel>)
    }

    private fun fieldIsNotVisualOptionSet(field: FieldUiModel): Boolean {
        return field.optionSet == null || field !is MatrixOptionSetModel
    }

    fun completedFieldsPercentage(): Float {
        val completedFields = eventSectionModels.sumBy { it.numberOfCompletedFields() }
        return calculateCompletionPercentage(completedFields, totalFields)
    }

    fun unsupportedFieldsPercentage(): Float {
        return calculateCompletionPercentage(unsupportedFields, totalFields)
    }

    private fun calculateCompletionPercentage(
        completedFields: Int,
        totals: Int
    ): Float {
        return if (totals == 0) {
            100f
        } else completedFields.toFloat() / totals.toFloat()
    }
}
