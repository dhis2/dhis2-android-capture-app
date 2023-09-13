package org.dhis2.form.model

import org.hisp.dhis.mobile.ui.designsystem.component.SectionState

data class FormSection(
    val uid: String,
    val title: String,
    val description: String? = null,
    val state: SectionState? = SectionState.FIXED,
    val errorCount: Int = 0,
    val warningCount: Int = 0,
    val fields: List<FieldUiModel>,
) {
    val totalFields = fields.size
    val completedFields = fields.count { it.value != null }
}
