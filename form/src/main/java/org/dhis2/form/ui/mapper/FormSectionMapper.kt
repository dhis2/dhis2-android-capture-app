package org.dhis2.form.ui.mapper

import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.FormSection
import org.dhis2.form.model.SectionUiModelImpl
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState

class FormSectionMapper {

    fun mapFromFieldUiModelList(items: List<FieldUiModel>): List<FormSection> {
        val sections = mutableListOf<FormSection>()
        if (hasSections(items)) {
            items.forEach { item ->
                if (item is SectionUiModelImpl) {
                    val fields = items.filterIsInstance<FieldUiModelImpl>()
                        .filter { it.programStageSection == item.uid }
                    sections.add(
                        FormSection(
                            uid = item.uid,
                            title = item.label,
                            description = item.description,
                            state = when (item.isOpen) {
                                true -> SectionState.OPEN
                                false -> SectionState.CLOSE
                                null -> SectionState.FIXED
                            },
                            fields = fields,
                            warningMessage = if (fields.isEmpty()) {
                                R.string.form_without_fields
                            } else {
                                null
                            },
                        ),
                    )
                }
            }
        } else {
            sections.add(
                FormSection(
                    uid = "DUMMY",
                    title = "TITLE",
                    description = null,
                    state = SectionState.NO_HEADER,
                    fields = items.filterIsInstance<FieldUiModelImpl>(),
                ),
            )
        }
        return sections
    }

    private fun hasSections(items: List<FieldUiModel>): Boolean {
        return items.filterIsInstance<SectionUiModelImpl>().isNotEmpty()
    }
}
