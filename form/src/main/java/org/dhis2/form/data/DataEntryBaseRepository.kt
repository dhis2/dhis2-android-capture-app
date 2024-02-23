package org.dhis2.form.data

import org.dhis2.form.data.metadata.FormBaseConfiguration
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.SectionRenderingType

abstract class DataEntryBaseRepository(
    private val conf: FormBaseConfiguration,
    private val fieldFactory: FieldViewModelFactory,
) : DataEntryRepository {

    abstract val programUid: String?
    override fun firstSectionToOpen(): String? {
        return sectionUids().blockingFirst().firstOrNull()
    }

    override fun updateSection(
        sectionToUpdate: FieldUiModel,
        isSectionOpen: Boolean?,
        totalFields: Int,
        fieldsWithValue: Int,
        errorCount: Int,
        warningCount: Int,
    ): FieldUiModel {
        return (sectionToUpdate as SectionUiModelImpl).copy(
            isOpen = isSectionOpen,
            totalFields = totalFields,
            completedFields = fieldsWithValue,
            errors = errorCount,
            warnings = warningCount,
        )
    }

    override fun updateField(
        fieldUiModel: FieldUiModel,
        warningMessage: String?,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): FieldUiModel {
        val optionsInGroupsToHide = optionsFromGroups(optionGroupsToHide)
        val optionsInGroupsToShow = optionsFromGroups(optionGroupsToShow)

        val item = when {
            fieldUiModel.optionSet != null -> {
                fieldUiModel.apply {
                    this.optionSetConfiguration =
                        optionSetConfiguration?.updateOptionsToHideAndShow(
                            optionsToHide = listOf(optionsToHide, optionsInGroupsToHide).flatten(),
                            optionsToShow = optionsInGroupsToShow,
                        )
                }
            }

            else -> {
                fieldUiModel
            }
        }

        return warningMessage?.let { item.setError(it) } ?: item
    }

    private fun optionsFromGroups(optionGroupUids: List<String>): List<String> {
        if (optionGroupUids.isEmpty()) return emptyList()
        val optionsFromGroups = arrayListOf<String>()
        val optionGroups = conf.optionGroups(optionGroupUids)
        for (optionGroup in optionGroups) {
            for (option in optionGroup.options()!!) {
                if (!optionsFromGroups.contains(option.uid())) {
                    optionsFromGroups.add(option.uid())
                }
            }
        }
        return optionsFromGroups
    }

    override fun dateFormatConfiguration(): String? {
        return conf.dateFormatConfiguration()
    }

    fun transformSection(
        sectionUid: String,
        sectionName: String?,
        sectionDescription: String? = null,
        isOpen: Boolean = false,
        totalFields: Int = 0,
        completedFields: Int = 0,
    ): FieldUiModel {
        return fieldFactory.createSection(
            sectionUid,
            sectionName,
            sectionDescription,
            isOpen,
            totalFields,
            completedFields,
            SectionRenderingType.LISTING.name,
        )
    }

    internal fun getError(conflict: TrackerImportConflict?, dataValue: String?) = conflict?.let {
        if (it.value() == dataValue) {
            it.displayDescription()
        } else {
            null
        }
    }

    override fun disableCollapsableSections(): Boolean? {
        return programUid?.let { conf.disableCollapsableSectionsInProgram(programUid = it) }
    }
}
