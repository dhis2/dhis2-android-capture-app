package org.dhis2.form.model

import org.hisp.dhis.mobile.ui.designsystem.component.SectionState

data class FormSection(
    val uid: String,
    val title: String,
    val description: String? = null,
    val state: SectionState,
    val fields: List<FieldUiModel>,
) {
    fun completedFields() = fields.count { it.value != null }
    fun errorCount() = fields.count { it.error != null }
    fun warningCount() = fields.count { it.warning != null }
}
