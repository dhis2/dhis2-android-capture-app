package org.dhis2.form.ui.mapper

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.FormSection
import org.dhis2.form.model.SectionUiModelImpl
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState

class FormSectionMapper {

    fun mapFromFieldUiModelList(items: List<FieldUiModel>): List<FormSection> {
        val sections = mutableListOf<FormSection>()
        items.forEach { item ->
            if (item is SectionUiModelImpl) {
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
                        fields = items.filterIsInstance<FieldUiModelImpl>()
                            .filter { it.programStageSection == item.uid },
                    ),
                )
            }
        }
        return sections
    }
}
